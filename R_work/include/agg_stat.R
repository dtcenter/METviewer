.libPaths("/common/data/web/metviewer/dev/r-lib");
library(boot);
library(gsl);
library(stats);

# parse the command line arguments
strInputInfoFile = "~/plot_00124_20130923_082001.agg_stat.info";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("agg_stat.R\ninput file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblBootTime		= 0;
dblBootCITime	= 0;
intNumBoots		= 0;

# read the input data file into a data frame
dfStatsRec = read.delim(strInputDataFile);
intYMax = 1;
if( 0 < length(listSeries2Val) ){ intYMax = 2; }

# sort the dataset by init time, lead time and independent variable
listFields = names(dfStatsRec);

listGroupToValue=c();
if(length(names(listSeries1Val)) > 1){
  for (index  in 1: length(names(listSeries1Val)) ){
    strSeriesVar=names(listSeries1Val)[[index]];
    valSeries = listSeries1Val[[strSeriesVar]];
    for(strVar in valSeries){
      if( grepl(',', strVar)) {
        newName = paste('Group_y1_',index,sep = "");
        listGroupToValue[[newName]]= strVar;
      }
    }
  }
}

if(length(names(listSeries2Val)) > 1){
  for (index  in 1: length(names(listSeries2Val)) ){
    strSeriesVar=names(listSeries2Val)[[index]];
    valSeries = listSeries2Val[[strSeriesVar]];
    for(strVar in valSeries){
      if( grepl(',', strVar)) {
        newName = paste('Group_y2_',index,sep = "");
        listGroupToValue[[newName]]= strVar;
      }
    }
  }
}


dfStatsRecAxis1= data.frame();
dfStatsRecAxis2= data.frame();
# build a list for output permutations


# run event equalizer, if requested
if( boolEventEqual  ){
  boolMulti=FALSE;
  #for SSVAR use equalization of mulitple events
  if(boolAggSsvar){
    boolMulti=TRUE;
  }
  #run event equalizer on Y1
  dfPlot1 = data.frame();
  #list all fixed variables
  listFixVars=c();
  listFixVarVals = c();
  if(length(listFixedValEx) > 0){
    for(index in 1:length(listFixedValEx)){
      if(is.null(listFixVars)){
        listFixVars = names(listFixedValEx[index]);
        listFixVarVals = permute(listFixedValEx[index]);
      }else{
        listFixVars =  append(listFixVars, names(listFixedValEx[index]));
        listFixVarVals = append(listFixVarVals, permute(listFixedValEx[index]));
      }
    }
  }
  for( strDep1Name in names(listDep1Plot) ){
    for( strDep1Stat in listDep1Plot[[strDep1Name]] ){
      fPlot = dfStatsRec;
      for(strSeriesVal in names(listSeries1Val)){
        vectValPerms = c();
        for(index in 1:length(listSeries1Val[[strSeriesVal]])){
          vectValPerms= append(vectValPerms, strsplit(listSeries1Val[[strSeriesVal]][index], ",")[[1]]);
        }
        fPlot = fPlot[fPlot$fcst_var == strDep1Name & fPlot[[strSeriesVal]] %in% vectValPerms & fPlot$stat_name %in% strDep1Stat,  ];
      }

      fPlot = eventEqualize(fPlot, strIndyVar, listIndyVal, listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, boolMulti);
      dfPlot1 = rbind(dfPlot1, fPlot);
    }
  }


  #if the second Y axis is present - run event equalizer on Y1
  # and then run event equalizer on Y1 and Y2 equalized data
  if(length(listSeries2Val) > 0){
    dfPlot2 = data.frame();
    for( strDep2Name in names(listDep2Plot) ){
      for( strDep2Stat in listDep2Plot[[strDep2Name]] ){
        fPlot = dfStatsRec;
        for(strSeriesVal in names(listSeries2Val)){
          vectValPerms = c();
          for(index in 1:length(listSeries2Val[[strSeriesVal]])){
            vectValPerms= append(vectValPerms, strsplit(listSeries2Val[[strSeriesVal]][index], ",")[[1]]);
          }
          fPlot = fPlot[fPlot$fcst_var == strDep1Name & fPlot[[strSeriesVal]] %in% vectValPerms & fPlot$stat_name %in% strDep1Stat,  ];
        }

        fPlot = eventEqualize(fPlot, strIndyVar, listIndyVal, listSeries2Val, listFixVars,listFixVarVals, boolEqualizeByIndep, boolMulti);
        dfPlot2 = rbind(dfPlot2, fPlot);
      }
    }

    dfStatsRec = rbind(dfPlot1, dfPlot2);
    listSeriesVal=list();
    for( seriesVal in names(listSeries1Val) ){
      listSeriesVal[[seriesVal]] = append(listSeries1Val[[seriesVal]], listSeries2Val[[seriesVal]]);
    }
    listSeriesVal$model = append(listSeries1Val$model,listSeries2Val$model)
    dfStatsRec = eventEqualize(dfPlot, strIndyVar, listIndyVal, listSeriesVal, listFixVars,listFixVarVals,boolEqualizeByIndep, TRUE);
  }else{
    dfStatsRec = dfPlot1;
  }
  strAfrerEqualizeFile = sub("\\.agg_stat", ".dataAfterEq", strInputDataFile, perl=TRUE);
  write.table(dfStatsRec, file=strAfrerEqualizeFile, quote=FALSE, row.names=FALSE, col.names=TRUE, sep = "\t");
}

for(intY in 1:intYMax){
  dfStatsRecAxis=data.frame();
  if( 1 == intY ){
    listSeriesVal = listSeries1Val;
    listStat = listStat1;
    listDiffSeries = listDiffSeries1;
  }
  if( 2 == intY ){
    listSeriesVal = listSeries2Val;
    listStat = listStat2;
    listDiffSeries = listDiffSeries2;
  }

  # add the series variables and values, including a difference series if appropriate
  listOut = listSeriesVal;
  listSeriesVar = names(listSeriesVal);
  if( length(listDiffSeries) >0){
    strDiffVar = listSeriesVar[length(listSeriesVar)];
  }

  # store the CI permutations for each series group
  if( 1 == intY ){ matCIPerm1 = permute(listOut); }
  if( 2 == intY ){ matCIPerm2 = permute(listOut); }

  # add the independent variable and statistics to create the list for the output data frame
  listOut[[strIndyVar]] = listIndyVal;
  listOut$stat_name = listStat;
  matOutSeries = permute(listOut);
  if( !exists("matOut") ){ matOut = matrix(nrow=0, ncol=length(names(listOut))); }
  matOut = rbind(matOut, matOutSeries);

  #add diff series
  if(length(listDiffSeries) > 0){
    for( diffSeriesName in 1: length(listDiffSeries) ){ #1,2....
      listDiffVal = list();
      for(var in listSeriesVar){
        listDiffVal[[var]]="";
      }
      diffSeriesVec = listDiffSeries[[diffSeriesName]];
      listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
      listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];

      strStat1 = listSeriesDiff1[length(listSeriesDiff1)];
      strStat2 = listSeriesDiff2[length(listSeriesDiff2)];
      derivedCurveName = getDerivedCurveName(diffSeriesVec);
      listDiffVal[[strDiffVar]] = derivedCurveName;
      listDiffVal[[strIndyVar]] = listIndyVal;
      listDiffVal$stat_name = paste(strStat1,strStat2,collapse="", sep=",");
      matOut = rbind(matOut, permute(listDiffVal));
    }

  }

}



if( 1 > nrow(dfStatsRec) ){
  if( boolEventEqual  ){
    stop("ERROR: eventEqualize() removed all data");
  }else{
    stop("ERROR: Database query returned empy set");
  }
}


# build a dataframe (dfOut) to store the bootstrapped statistics
listOutPerm = list();
intNumOut = nrow(matOut);
for(strStaticVar in names(listStaticVal)){
  listOutPerm[[strStaticVar]] = rep(listStaticVal[[strStaticVar]], intNumOut);
}
if(strIndyVar != ""){
  for(intOutCol in 1:ncol(matOut)){
    if ( intOutCol <  ncol(matOut) - 1 ){
      listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
    } else if ( intOutCol == ncol(matOut) - 1 ){
        listOutPerm[[ strIndyVar ]] = matOut[,intOutCol];
    } else {
      listOutPerm$stat_name = matOut[,intOutCol];
    }
  }
}else{
  for(intOutCol in 1:ncol(matOut)){
    if ( intOutCol ==   ncol(matOut)   ){
      listOutPerm$stat_name = matOut[,intOutCol];
    }else{
      listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
    }
  }

}
listOutPerm$stat_value = rep(NA, intNumOut);
listOutPerm$stat_bcl = rep(NA, intNumOut);
listOutPerm$stat_bcu = rep(NA, intNumOut);
dfOut = data.frame(listOutPerm);
dfOut$nstats=rep(0, intNumOut);

# initialize the histogram array
listHist = c();

# SL1L2 stat calculations
calcStdDev		= function(sum, sum_sq, n){
  if ( 1 > n ){
    return(NA);
  }
  v = (sum_sq - sum*sum/n)/(n - 1);
  if( 0 > v ){
    return(NA);
  } else {
    return( sqrt(v) );
  }
}
calcFBAR		= function(d){ return( d$fbar ); }
calcOBAR		= function(d){ return( d$obar ); }
calcFSTDEV		= function(d){ return( calcStdDev(d$fbar * d$total, d$ffbar * d$total, d$total) ); }
calcOSTDEV		= function(d){ return( calcStdDev(d$obar * d$total, d$oobar * d$total, d$total) ); }
calcFOBAR		= function(d){ return( d$fobar ); }
calcFFBAR		= function(d){ return( d$ffbar ); }
calcOOBAR		= function(d){ return( d$oobar ); }
calcMAE		= function(d){ return( d$mae ); }
calcMBIAS		= function(d){ if( 0 == d$obar ){ return (NA); } else { return( d$fbar / d$obar ); } }
calcPR_CORR		= function(d){
  if( is.na(d$total) || is.na(d$ffbar) || is.na(d$fbar) || is.na(d$oobar) || is.na(d$obar) ){
    return(NA);
  } else{
    v =  (d$total^2 * d$ffbar - d$total^2 * d$fbar^2) * (d$total^2 * d$oobar - d$total^2 * d$obar^2);
    pr_corr = (d$total^2 * d$fobar - d$total^2 * d$fbar * d$obar) / sqrt(v);
    if( 0 >= v || 1 < pr_corr ){
      return(NA);
    } else {
      return( pr_corr );
    }
  }
}

calcANOM_CORR		= function(d){
  if( is.na(d$total) || is.na(d$ffbar) || is.na(d$fbar) || is.na(d$oobar) || is.na(d$obar) ){
    return(NA);
  } else{
    v =  (d$total^2 * d$ffbar - d$total^2 * d$fbar^2) * (d$total^2 * d$oobar - d$total^2 * d$obar^2);
    anom_corr = (d$total^2 * d$fobar - d$total^2 * d$fbar * d$obar) / sqrt(v);
    if( 0 >= v || 1 < anom_corr ){
      return(NA);
    } else {
      return( anom_corr );
    }
  }
}
calcME			= function(d){ return( d$fbar - d$obar ); }
calcME2			= function(d){
  me = d$fbar - d$obar;
  return( me * me );
}
calcMSE			= function(d){ return( d$ffbar + d$oobar - 2 * d$fobar ); }
calcMSESS			= function(d){
  ostdev=calcOSTDEV( d );
  mse=calcMSE( d );
  return( 1.0 - mse/(ostdev*ostdev) );
}
calcRMSE		= function(d){ return( sqrt(calcMSE(d)) ); }
calcESTDEV		= function(d){ return( calcStdDev( calcME(d) * d$total, calcMSE(d) * d$total, d$total) ); }
calcBCMSE		= function(d){ return( calcMSE(d) - (d$fbar - d$obar)^2 ); }
calcBCRMSE		= function(d){ return( sqrt(calcBCMSE(d)) ); }

# SSVAR stat calculations
calcSSVAR_FBAR		= function(d){ return( calcFBAR(d) ); }
calcSSVAR_OBAR		= function(d){ return( calcOBAR(d) ); }
calcSSVAR_FSTDEV		= function(d){ return( calcFSTDEV(d) ); }
calcSSVAR_OSTDEV		= function(d){ calcOSTDEV(d ); }
calcSSVAR_FOBAR		= function(d){ return( calcFOBAR(d) ); }
calcSSVAR_FFBAR		= function(d){ return( calcFFBAR(d) ); }
calcSSVAR_OOBAR		= function(d){ return( calcOOBAR(d) ); }
calcSSVAR_MBIAS		= function(d){ return( calcMBIAS(d) ); }
calcSSVAR_PR_CORR		= function(d){ return( calcPR_CORR(d) ); }

calcSSVAR_ANOM_CORR		= function(d){ return( calcANOM_CORR(d) ); }
calcSSVAR_ME			= function(d){ return( calcME(d) ); }
calcSSVAR_ME2			= function(d){ return( calcME2(d) ); }
calcSSVAR_MSE			= function(d){ return( calcMSE(d) ); }
calcSSVAR_MSESS			= function(d){ return( calcMSESS(d) ); }
calcSSVAR_RMSE		= function(d){ return( calcRMSE(d) ); }
calcSSVAR_ESTDEV		= function(d){ return( calcESTDEV(d) ); }
calcSSVAR_BCMSE		= function(d){ return( calcBCMSE(d)); }
calcSSVAR_BCRMSE		= function(d){ return( calcBCRMSE(d) ); }

calcSSVAR_Spread = function(d){
  if( length( d$varmean ) < 1 ){ return (NA); }
  return( sqrt(weighted.mean(d$varmean, d$binn)) );
}


# CTC stat calculations
calcBASER		= function(d){ if( 0 == d$total )                      { return (NA); } else { return( (d$fy_oy + d$fn_oy) / d$total ); }             }
calcACC			= function(d){ if( 0 == d$total )                      { return (NA); } else { return( (d$fy_oy + d$fn_on) / d$total ); }             }
calcFBIAS		= function(d){ if( 0 == (d$fy_oy + d$fn_oy) )          { return (NA); } else { return( (d$fy_oy + d$fy_on) / (d$fy_oy + d$fn_oy) ); } }
calcFMEAN		= function(d){ if( 0 == d$total )                      { return (NA); } else { return( (d$fy_oy + d$fy_on) / d$total ); }             }
calcPODY		= function(d){ if( 0 == (d$fy_oy + d$fn_oy) )          { return (NA); } else { return( d$fy_oy / (d$fy_oy + d$fn_oy) ); }             }
calcPOFD		= function(d){ if( 0 == (d$fy_on + d$fn_on) )          { return (NA); } else { return( d$fy_on / (d$fy_on + d$fn_on) ); }             }
calcPODN		= function(d){ if( 0 == (d$fy_on + d$fn_on) )          { return (NA); } else { return( d$fn_on / (d$fy_on + d$fn_on) ); }             }
calcFAR			= function(d){ if( 0 == (d$fy_oy + d$fy_on) )          { return (NA); } else { return( d$fy_on / (d$fy_oy + d$fy_on) ); }             }
calcCSI			= function(d){ if( 0 == (d$fy_oy + d$fy_on + d$fn_oy) ){ return (NA); } else { return( d$fy_oy / (d$fy_oy + d$fy_on + d$fn_oy) ); }   }
calcGSS = function(d){
  if( 0 == d$total ){ return (NA); }
  dblC = ( (d$fy_oy + d$fy_on) / d$total ) * (d$fy_oy + d$fn_oy);
  gss = ( (d$fy_oy - dblC) / (d$fy_oy + d$fy_on + d$fn_oy - dblC) )
  return( round(gss, digits=5) );
}
calcHK = function(d){ if( is.na(calcPODY(d)) || is.na(calcPOFD(d)) ){ return (NA); } else { return( calcPODY(d) - calcPOFD(d) ); } }
calcHSS = function(d){
  if( 0 == d$total ){ return (NA); }
  #dblC = (
  #		 as.numeric( (d$fy_oy + d$fy_on)*(d$fy_oy + d$fn_oy) ) +
  #		 as.numeric( (d$fn_oy + d$fn_on)*(d$fy_on + d$fn_on) )
  #	   ) / d$total;
  dblC = ( ((d$fy_oy + d$fy_on) / d$total) *(d$fy_oy + d$fn_oy)  +  ((d$fn_oy + d$fn_on) / d$total) * (d$fy_on + d$fn_on)  ) ;
  hss = ( (d$fy_oy + d$fn_on - dblC) / (d$total - dblC) );
  return( round(hss, digits=5) );

}
calcODDS = function(d){
  if( is.na(calcPODY(d)) || is.na(calcPOFD(d)) ){ return (NA); }
  dblPOD = calcPODY(d);
  dblPOFD = calcPOFD(d);
  return( (dblPOD * (1 - dblPOFD)) / (dblPOFD * (1 - dblPOD)) );
}
# BAGSS Reference:
# Bias Adjusted Precipitation Threat Scores
# F. Mesinger, Adv. Geosci., 16, 137-142, 2008
calcBAGSS = function(d){
  if( 0 == d$total ){ return (NA); }
  dblF  = d$fy_oy + d$fy_on;
  dblO  = d$fy_oy + d$fn_oy;
  dblLf = log(dblO / d$fn_oy);
  dblHa = tryCatch({
    dblO - (d$fy_on / dblLf) * lambert_W0(dblO / d$fy_on * dblLf);
  }, warning = function(w) {
    return (NA)
  }, error = function(e) {
    return (NA)
  });
  return( (dblHa - (dblO^2 / d$total)) / (2*dblO - dblHa - (dblO^2 / d$total)) );
}



# NBR_CNT "calculations"
calcNBR_FBS = function(d){ return ( d$fbs ); }
calcNBR_FSS = function(d){ return ( d$fss ); }
calcNBR_AFSS = function(d){ return ( d$afss ); }
calcNBR_UFSS = function(d){ return ( d$ufss ); }
calcNBR_F_RATE = function(d){ return ( d$f_rate ); }
calcNBR_O_RATE = function(d){ return ( d$o_rate ); }

#VL1L2 "calculations"
calcVL1L2_FBAR = function(d){ return ( sqrt(d$uvffbar) ); }
calcVL1L2_OBAR = function(d){ return ( sqrt(d$uvoobar) ); }
calcVL1L2_ME = function(d){
  me = sqrt(d$ufbar^2 - 2 * d$ufbar * d$uobar + d$uobar^2 + d$vfbar^2 - 2 * d$vfbar * d$vobar + d$vobar^2);
  return ( round(me, digits=5)) ;
}
calcVL1L2_BIAS = function(d){
  bias = sqrt(d$uvffbar) - sqrt(d$uvoobar);
  return ( round(bias, digits=5)) ;
}
calcVL1L2_MSE = function(d){
  mse = d$uvffbar -2 * d$uvfobar + d$uvoobar;
  if (mse < 0 ){
    return (NA);
  }
  return ( round(mse, digits=5)) ;
}
calcVL1L2_RMSE = function(d){
  rmse = sqrt( calcVL1L2_MSE(d ) );
  return ( round(rmse, digits=5)) ;
}
calcVL1L2_MAE = function(d){
  mae = sqrt(d$ufbar^2 -2 * d$ufbar * d$uobar + d$uobar^2 + d$vfbar^2 - 2 * d$vfbar * d$vobar + d$vobar^2);
  return ( round(mae, digits=5)) ;
}

calcVL1L2_FVAR = function(d){
  fvar = d$total * (d$uvffbar - d$ufbar^2 - d$vfbar^2 )/ d$total;
  return ( round(fvar, digits=5)) ;
}
calcVL1L2_OVAR = function(d){
  ovar = d$total * ( d$uvoobar - d$uobar^2 - d$vobar^2 )/ d$total;
  return ( round(ovar, digits=5)) ;
}
calcVL1L2_FSTDEV = function(d){
  fstdev = sqrt( calcVL1L2_FVAR (d));
  return ( round(fstdev, digits=5)) ;
}
calcVL1L2_OSTDEV = function(d){
  ostdev = sqrt( calcVL1L2_OVAR (d));
  return ( round(ostdev, digits=5)) ;
}
calcVL1L2_FOSTDEV = function(d){
  ostdev =  sqrt( d$total * (d$uvffbar - d$ufba^2 +d$uvoobar-d$uobar^2-d$vobar^2 -2*(d$uvfobar-d$ufbar*d$uobar - d$vfbar*d$vobar))/d$total);
  return ( round(ostdev, digits=5)) ;
}
calcVL1L2_COV = function(d){
  cov =    d$total * (d$uvfobar -d$ufbar*d$uobar - d$vfbar * d$vobar) / (d$total-0.);

  return ( round(cov, digits=5)) ;
}
calcVL1L2_CORR = function(d){
  corr = (d$total * (d$uvfobar - d$ufbar * d$uobar - d$vfbar * d$vobar) / d$total) /
                         (sqrt(d$total*(d$uvffbar-d$ufbar*d$ufbar-d$vfbar*d$vfbar)/d$total) *
                         sqrt( d$total*(d$uvoobar-d$uobar*d$uobar-d$vobar*d$vobar)/d$total))
  return ( round(corr, digits=5)) ;
}


# booter function
booter.iid = function(d, i){
  # initialize storage
  listRet = list();
  listRetTest = list();
  for(strStat in listStat){
    listRet[[strStat]] = c();
    listRetTest[[strStat]] = c();
  }

  # for each series permutation, build a combined table and calculate statistics
  for(intPerm in 1:nrow(matPerm)){

    # create the permutation column name string
    #strPerm = escapeStr(paste(matPerm[intPerm,], sep="_", collapse="_"));
    strPerm = escapeStr(paste(intPerm, sep="_", collapse="_"));

    # create the permutation column name string

    # perform the aggregation of the sampled CTC lines
    if( boolAggCtc ){
      dfSeriesSums = data.frame(
        total	= sum( as.numeric(d[i,][[ paste(strPerm, "total", sep="_") ]]), na.rm=TRUE ),
        fy_oy	= sum( as.numeric(d[i,][[ paste(strPerm, "fy_oy", sep="_") ]]), na.rm=TRUE ),
        fy_on	= sum( as.numeric(d[i,][[ paste(strPerm, "fy_on", sep="_") ]]), na.rm=TRUE ),
        fn_oy	= sum( as.numeric(d[i,][[ paste(strPerm, "fn_oy", sep="_") ]]), na.rm=TRUE ),
        fn_on	= sum( as.numeric(d[i,][[ paste(strPerm, "fn_on", sep="_") ]]), na.rm=TRUE )
      );
    }  else if ( boolAggSl1l2 ){ # perform the aggregation of the sampled SL1L2 lines
      listTotal	= d[i,][[ paste(strPerm, "total", sep="_") ]];
      total		= sum(listTotal, na.rm=TRUE);
      dfSeriesSums = data.frame(
        total	= total,
        fbar	= sum( as.numeric( d[i,][[ paste(strPerm, "fbar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total,
        obar	= sum( as.numeric( d[i,][[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        fobar	= sum( as.numeric( d[i,][[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        ffbar	= sum( as.numeric( d[i,][[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        oobar	= sum( as.numeric( d[i,][[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        mae   = sum( as.numeric( d[i,][[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
      );
    }  else if ( boolAggVl1l2 ){ # perform the aggregation of the sampled VL1L2 lines
        listTotal  = d[i,][[ paste(strPerm, "total", sep="_") ]];
        total    = sum(listTotal, na.rm=TRUE);
        dfSeriesSums = data.frame(
        total  = total,
        ufbar  = sum( as.numeric( d[i,][[ paste(strPerm, "ufbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        vfbar  = sum( as.numeric( d[i,][[ paste(strPerm, "vfbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        uobar  = sum( as.numeric( d[i,][[ paste(strPerm, "uobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        vobar  = sum( as.numeric( d[i,][[ paste(strPerm, "vobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        uvfobar  = sum( as.numeric( d[i,][[ paste(strPerm, "uvfobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        uvffbar   = sum( as.numeric( d[i,][[ paste(strPerm, "uvffbar", sep="_") ]] )   * listTotal, na.rm=TRUE ) / total,
        uvoobar   = sum( as.numeric( d[i,][[ paste(strPerm, "uvoobar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
      );
    }  else if ( boolAggSal1l2 ){ # perform the aggregation of the sampled SAL1L2 lines
    listTotal  = d[i,][[ paste(strPerm, "total", sep="_") ]];
      total		= sum(listTotal, na.rm=TRUE);
      dfSeriesSums = data.frame(
        total	= total,
        fbar	= sum( as.numeric( d[i,][[ paste(strPerm, "fabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        obar	= sum( as.numeric( d[i,][[ paste(strPerm, "oabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        fobar	= sum( as.numeric( d[i,][[ paste(strPerm, "faobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        ffbar	= sum( as.numeric( d[i,][[ paste(strPerm, "ffabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        oobar	= sum( as.numeric( d[i,][[ paste(strPerm, "ooabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        mae   = sum( as.numeric( d[i,][[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
      );
    }  else if ( boolAggSsvar ){ # perform the aggregation of the sampled SSVAR lines
      listTotal  = d[i,][[ paste(strPerm, "bin_n", sep="_") ]];
      total    = sum(listTotal, na.rm=TRUE);
      dfSeriesSums = data.frame(
        total  = total,
        fbar	= sum( as.numeric( d[i,][[ paste(strPerm, "fbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        obar	= sum( as.numeric( d[i,][[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        fobar	= sum( as.numeric( d[i,][[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        ffbar	= sum( as.numeric( d[i,][[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        oobar	= sum( as.numeric( d[i,][[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        varmean	= sum( as.numeric( d[i,][[ paste(strPerm, "var_mean", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
        binn	=  total
      );
    } else if( boolAggNbrCnt ){ # perform the aggregation of the sampled NBR_CNT lines
      listTotal = d[i,][[ paste(strPerm, "total", sep="_") ]];
      total = sum(listTotal, na.rm=TRUE);
      listFbs = d[i,][[ paste(strPerm, "fbs", sep="_") ]];
      listFss = d[i,][[ paste(strPerm, "fss", sep="_") ]];
      listAFss = d[i,][[ paste(strPerm, "afss", sep="_") ]];
      listUFss = d[i,][[ paste(strPerm, "ufss", sep="_") ]];
      listFRate = d[i,][[ paste(strPerm, "f_rate", sep="_") ]];
      listORate = d[i,][[ paste(strPerm, "o_rate", sep="_") ]];

      listFss[listFss == -9999] = NA;

      dblFbs = sum(listTotal * listFbs, na.rm=TRUE) / total;

      dblFssDen = sum( (listFbs / (1.0 - listFss)) * listTotal, na.rm=TRUE) / total;
      dblFss = 1.0 - dblFbs / dblFssDen;
      if( !is.finite(dblFss) ){ dblFss = NA; }

      dblFRate = sum(listTotal * listFRate, na.rm=TRUE) / total;
      dblORate = sum(listTotal * listORate, na.rm=TRUE) / total;

      dblAFssNum = 2.0*dblFRate*dblORate;
      dblAFssDen = dblFRate*dblFRate + dblORate*dblORate;
      dblAFss = dblAFssNum / dblAFssDen;
      if( !is.finite(dblAFss) ){ dblAFss = NA; }

      dblUFss = 0.5 + dblORate/2.0;

      dfSeriesSums = data.frame(
        total = total,
        fbs = dblFbs,
        fss = dblFss,
        afss = dblAFss,
        ufss = dblUFss,
        f_rate = dblFRate,
        o_rate = dblORate
      );
    }


    # return a value for each statistic
    for(strStat in listStat){
      # calculate and store the statistic
      dblStat = do.call( paste("calc", strStat, sep=""), list(d=dfSeriesSums) );
      listRet[[strStat]] = append(listRet[[strStat]], dblStat);
    }
  }
  if(length(listDiffSeries) > 0){
    for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
      #get  names of DIFF series
      diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];

      listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
      listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];

      strStat1 = listSeriesDiff1[length(listSeriesDiff1)];
      strStat2 = listSeriesDiff2[length(listSeriesDiff2)];

      listSeriesDiff1Short = listSeriesDiff1[1:(length(listSeriesDiff1)-2)];
      listSeriesDiff2Short = listSeriesDiff2[1:(length(listSeriesDiff2)-2)];

      strSeriesDiff1Short = escapeStr(paste(listSeriesDiff1Short,sep="_", collapse="_"));# number for model
      strSeriesDiff2Short = escapeStr(paste(listSeriesDiff2Short,sep="_", collapse="_"));
      indPerm1=0;
      indPerm2=0;
      derivedCurveName = getDerivedCurveName(diffSeriesVec);

      #remove groups from the series vars
      for(groupName in names(listGroupToValue)){
        for(index in 1:length( listSeriesDiff1Short)){
          if(groupName == listSeriesDiff1Short[index]){
            listSeriesDiff1Short[index] = listGroupToValue[[groupName]];
            break;
          }
        }
        for(index in 1:length( listSeriesDiff2Short)){
          if(groupName == listSeriesDiff2Short[index]){
            listSeriesDiff2Short[index] = listGroupToValue[[groupName]];
            break;
          }
        }
      }

      for(intPerm in 1:nrow(matPerm)){
        d1 = listSeriesDiff1Short %in% matPerm[intPerm,];
        d2 = listSeriesDiff2Short %in% matPerm[intPerm,];
        if( all(d1) ){
          indPerm1=intPerm;
        }
        if( all(d2) ){
          indPerm2=intPerm;
        }
      }
      if(indPerm1 != 0 && indPerm2 !=0){
        listRet[[paste(strStat1,strStat2, indPerm1, indPerm2,diffSeriesVec[length(diffSeriesVec)],sep = "_")]] = calcDerivedCurveValue(listRet[[strStat1]][indPerm1],listRet[[strStat2]][indPerm2], derivedCurveName);
      }
    }
  }
  return( unlist(listRet) );
}

if(is.null(listIndyVal)){
  listIndyVal = c(NaN);
}

# run the bootstrap flow for each independent variable value
for(strIndyVal in listIndyVal){
  if(is.nan(strIndyVal)){
    dfStatsIndy = dfStatsRec;
  }else{
    if(strIndyVar == 'fcst_valid_beg' || strIndyVar == 'fcst_init_beg'){
      dfStatsIndy = dfStatsRec[as.character(dfStatsRec[[strIndyVar]]) == strIndyVal,];
    } else {
      dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
    }
  }
  if( 1 > nrow(dfStatsIndy) ){ next; }

  # for each series group, bootstrap the statistics
  for(intY in 1:intYMax){

    # build permutations for each plot series
    if( 1 == intY ){
      matPerm = permute(listSeries1Val);
      listStat = listStat1;
      matCIPerm = matCIPerm1;
      listDiffSeries = listDiffSeries1;
    }
    if( 2 == intY ){
      matPerm = permute(listSeries2Val);
      listStat = listStat2;
      matCIPerm = matCIPerm2;
      listDiffSeries = listDiffSeries2;
    }
    listBoot = list();

    # run the bootstrap flow for each series permutation
    for(intPerm in 1:nrow(matPerm)){
      listPerm = matPerm[intPerm,];

      # build the data set pertinent to this series permutation
      dfStatsPerm = dfStatsIndy;
      for(intSeriesVal in 1:length(listSeriesVar)){
        strSeriesVar = listSeriesVar[intSeriesVal];
        strSeriesVal = listPerm[intSeriesVal];
        if( grepl("^[0-9]+$", strSeriesVal) ){
          strSeriesVal = as.numeric(strSeriesVal);
          vectValPerms = strSeriesVal;
        }else{
          vectValPerms= strsplit(strSeriesVal, ",")[[1]];
        }
        vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.numeric(x); }else{x=x} })
        dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] %in% vectValPerms,];
      }
      if( 1 > nrow(dfStatsPerm) ){ next; }

      # add the contingency table constituents for this series permutation to the boot list
      strPerm = escapeStr(paste(listPerm, sep="_"));
      if ( boolAggCtc    ){
        listFields = c("total", "fy_oy", "fy_on", "fn_oy", "fn_on");
      } else if( boolAggSl1l2  ){
        listFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "mae");
      } else if( boolAggSal1l2  ){
        listFields = c("total", "fabar", "oabar", "foabar", "ffabar", "ooabar", "mae");
      } else if( boolAggVl1l2  ){
        listFields = c("total", "ufbar", "vfbar", "uobar", "vobar", "uvfobar", "uvffbar","uvoobar");
      } else if( boolAggSsvar  ){
        listFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "var_mean", "bin_n");
      } else if( boolAggNbrCnt ){
        listFields = c("total", "fbs", "fss");
      }
      for(strCount in listFields){
        listCounts = dfStatsPerm[[strCount]];
        strCountName = paste(paste(strPerm, sep = "_", collapse = "_"), strCount, sep = "_", collapse = "_");
        strCountName1 = paste(paste(intPerm, sep = "_", collapse = "_"), strCount, sep = "_", collapse = "_");

        listBoot[[strCountName1]] = listCounts;
      }
    }

    # ensure that all count lists have the same length, appending with NAs where necessary
    intCountLength = -1;
    for(strCountName in names(listBoot)){
      if( intCountLength < length(listBoot[[strCountName]]) ){ intCountLength = length(listBoot[[strCountName]]); }
    }
    for(strCountName in names(listBoot)){
      if( intCountLength > length(listBoot[[strCountName]]) ){
        listBoot[[strCountName]] = append( listBoot[[strCountName]], rep(NA, intCountLength - length(listBoot[[strCountName]])) );
      }
    }

    # bootstrap the series data
    dfBoot = data.frame(listBoot, check.names=FALSE);
    stBoot = Sys.time();
    bootStat = try(boot(dfBoot, booter.iid, intNumReplicates));

    dblBootTime = dblBootTime + as.numeric(Sys.time() - stBoot, units="secs");
    intNumBoots = intNumBoots + 1;

    # to verify the statistics for "smoothness", use code like the following
    #browser();
    #par( mfrow=c(1,3) ); hist(bootStat$t[,1]); hist(bootStat$t[,2]); hist(bootStat$t[,3]);

    # for each series permutation and statistic, generate confidence intervals and store the output
    intBootIndex = 1;
    for(strStat in listStat){
      for(intPerm in 1:nrow(matCIPerm)){
        listPerm = matCIPerm[intPerm,];

        # build a indicator list for the pertinent rows in the output dataframe
        listOutInd = rep(TRUE, nrow(dfOut));
        for(intSeriesVal in 1:length(listSeriesVar)){
          strSeriesVar = listSeriesVar[intSeriesVal];
          strSeriesVal = listPerm[intSeriesVal];
          listOutInd = listOutInd & (dfOut[[strSeriesVar]] == strSeriesVal);
        }
        if(strIndyVar == ""){
          listOutInd = listOutInd & (dfOut$stat_name == strStat);
        }else{
          listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut[[strIndyVar]] == strIndyVal);
        }
        if( 1 < intNumReplicates ){

          # calculate the confidence interval for the current stat and series permutation
          stBootCI = Sys.time();
          bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
          dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");

        }

        # store the bootstrapped stat value and CI values in the output dataframe
        dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
        dfOut[listOutInd,]$nstats = nrow(dfStatsPerm[dfStatsPerm$stat_name == strStat,]);
        strCIParm = strCIType;
        if( strCIType == "perc" ){ strCIParm = "percent"; }
        if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
          dfOut[listOutInd,]$stat_bcl = bootCI[[strCIParm]][4];
          dfOut[listOutInd,]$stat_bcu = bootCI[[strCIParm]][5];
        } else {
          dfOut[listOutInd,]$stat_bcl = NA;
          dfOut[listOutInd,]$stat_bcu = NA;
        }
        intBootIndex = intBootIndex + 1;
      }
    }
    if(length(listDiffSeries) > 0){
      for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
        if( 1 < intNumReplicates ){
          stBootCI = Sys.time();
          bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
          dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");
        }

        diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];
        listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
        listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];
        strStat1= listSeriesDiff1[length(listSeriesDiff1)];
        strStat2= listSeriesDiff2[length(listSeriesDiff2)];

        derivedCurveName = getDerivedCurveName(diffSeriesVec);

        # build a indicator list for the pertinent rows in the output dataframe
        listOutInd = rep(TRUE, nrow(dfOut));
        strDiffVar = listSeriesVar[length(listSeriesVar)];
        listOutInd = listOutInd & (dfOut[[strDiffVar]] == derivedCurveName);

        listOutInd1 = listOutInd & (dfOut$stat_name == paste(strStat1,strStat2,collapse = "", sep=","))  & (dfOut[[strIndyVar]] == strIndyVal);

        dfOut[listOutInd1,]$stat_value = bootStat$t0[intBootIndex];
        dfOut[listOutInd1,]$nstats = 0;
        strCIParm = strCIType;
        if( strCIType == "perc" ){ strCIParm = "percent"; }
        if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
          dfOut[listOutInd1,]$stat_bcl = bootCI[[strCIParm]][4];
          dfOut[listOutInd1,]$stat_bcu = bootCI[[strCIParm]][5];
        } else {
          dfOut[listOutInd1,]$stat_bcl = NA;
          dfOut[listOutInd1,]$stat_bcu = NA;
        }

        intBootIndex = intBootIndex + 1;
      }
    }

  } # end for(intY in 1:intYMax)
} # end for(strIndy in listIndy)

#remove rows with stat_value=NA
#dfOut = dfOut[complete.cases(dfOut$stat_value),];




write.table(dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t");

cat(
  "    boot time: ", formatTimeSpan(dblBootTime), "\n",
  " boot.ci time: ", formatTimeSpan(dblBootCITime), "\n",
  "   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
  "      # boots: ", intNumBoots, "\n",
  " # replicates: ", intNumReplicates, "\n",
  "      # stats: ", length(listStat), "\n",
  sep="");

# clean up
cat("agg_stat.R done\n");
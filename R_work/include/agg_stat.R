.libPaths("/common/data/web/metviewer/dev/r-lib");
library(boot);
library(gsl);

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


dfStatsRecAxis1= data.frame();
dfStatsRecAxis2= data.frame();
# build a list for output permutations
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
      listDiffVal[[strDiffVar]] = paste("DIFF(",diffSeriesVec[1],"-",diffSeriesVec[2],")" ,collapse="");
      listDiffVal[[strIndyVar]] = listIndyVal;
      listDiffVal$stat_name = listStat;
      matOut = rbind(matOut, permute(listDiffVal));
    }
    
  }
  # run event equalizer either if requested or automatically if bootstrapping is enabled
  if( boolEventEqual ){
    dfStatsRecAxisTemp = dfStatsRec;
    for(name in names(listOut)){
      dfStatsRecAxisTemp=dfStatsRecAxisTemp[dfStatsRecAxisTemp[[name]] %in% listOut[[name]], ];
      
    }
    dfStatsRecAxisTemp = dfStatsRecAxisTemp[order(dfStatsRecAxisTemp$fcst_valid_beg),];
    dfStatsRecAxisTemp = dfStatsRecAxisTemp[order(dfStatsRecAxisTemp$fcst_init_beg),];
    dfStatsRecAxisTemp = dfStatsRecAxisTemp[order(dfStatsRecAxisTemp[[strIndyVar]]),];
    if( 1 == intY ){ dfStatsRecAxis1 = eventEqualize(dfStatsRecAxisTemp, strIndyVar, listIndyVal, listSeriesVal, FALSE); }
    if( 2 == intY ){ dfStatsRecAxis2 = eventEqualize(dfStatsRecAxisTemp, strIndyVar, listIndyVal, listSeriesVal, FALSE); }
    
  }
}


if( boolEventEqual ){
  dfStatsRec = rbind(dfStatsRecAxis1, dfStatsRecAxis2);
}
if( 1 > nrow(dfStatsRec) ){ stop("ERROR: eventEqualize() removed all data"); }


# build a dataframe (dfOut) to store the bootstrapped statistics
listOutPerm = list();
intNumOut = nrow(matOut);
for(strStaticVar in names(listStaticVal)){
  listOutPerm[[strStaticVar]] = rep(listStaticVal[[strStaticVar]], intNumOut);
}
for(intOutCol in 1:ncol(matOut)){
  if ( intOutCol <  ncol(matOut) - 1 ){
    listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
  } else if ( intOutCol == ncol(matOut) - 1 ){
    listOutPerm[[ strIndyVar ]] = matOut[,intOutCol];
  } else {
    listOutPerm$stat_name = matOut[,intOutCol];
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
calcME			= function(d){ return( d$fbar - d$obar ); }
calcMSE			= function(d){ return( d$ffbar + d$oobar - 2 * d$fobar ); }
calcRMSE		= function(d){ return( sqrt(calcMSE(d)) ); }
calcESTDEV		= function(d){ return( calcStdDev( calcME(d) * d$total, calcMSE(d) * d$total, d$total) ); }
calcBCMSE		= function(d){ return( calcMSE(d) - (d$fbar - d$obar)^2 ); }
calcBCRMSE		= function(d){ return( sqrt(calcBCMSE(d)) ); }


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
        total	= sum( d[i,][[ paste(strPerm, "total", sep="_") ]], na.rm=TRUE ),
        fy_oy	= sum( d[i,][[ paste(strPerm, "fy_oy", sep="_") ]], na.rm=TRUE ),
        fy_on	= sum( d[i,][[ paste(strPerm, "fy_on", sep="_") ]], na.rm=TRUE ),
        fn_oy	= sum( d[i,][[ paste(strPerm, "fn_oy", sep="_") ]], na.rm=TRUE ),
        fn_on	= sum( d[i,][[ paste(strPerm, "fn_on", sep="_") ]], na.rm=TRUE )
      );
    }  else if ( boolAggSl1l2 ){ # perform the aggregation of the sampled SL1L2 lines
      listTotal	= d[i,][[ paste(strPerm, "total", sep="_") ]];
      total		= sum(listTotal, na.rm=TRUE);
      dfSeriesSums = data.frame(
        total	= total,
        fbar	= sum( d[i,][[ paste(strPerm, "fbar", sep="_") ]]  * listTotal, na.rm=TRUE ) / total,
        obar	= sum( d[i,][[ paste(strPerm, "obar", sep="_") ]]  * listTotal, na.rm=TRUE ) / total,
        fobar	= sum( d[i,][[ paste(strPerm, "fobar", sep="_") ]] * listTotal, na.rm=TRUE ) / total,
        ffbar	= sum( d[i,][[ paste(strPerm, "ffbar", sep="_") ]] * listTotal, na.rm=TRUE ) / total,
        oobar	= sum( d[i,][[ paste(strPerm, "oobar", sep="_") ]] * listTotal, na.rm=TRUE ) / total,
        mae   = sum( d[i,][[ paste(strPerm, "mae", sep="_") ]]   * listTotal, na.rm=TRUE ) / total
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
      listRetTest[[strStat]] = append(listRetTest[[strStat]], dblStat);
    }
    listRetTest[[strStat]] = append(listRetTest[[strStat]], strPerm);

  }
  if(length(listDiffSeries) > 0){
    for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
      #get  names of DIFF series
      diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];

      listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
      listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];
      if(listSeriesDiff1[length(listSeriesDiff1)] == strStat && listSeriesDiff2[length(listSeriesDiff2)] == strStat){
        listSeriesDiff1Short = listSeriesDiff1[1:(length(listSeriesDiff1)-2)];
        listSeriesDiff2Short = listSeriesDiff2[1:(length(listSeriesDiff2)-2)];

        strSeriesDiff1Short = paste(listSeriesDiff1Short,sep="_", collapse="_");
        strSeriesDiff2Short = paste(listSeriesDiff2Short,sep="_", collapse="_");

        for(ind in seq(from=1, to=length(listRetTest[[strStat]]), by=2) ) {
          if( matPerm[strtoi(listRetTest[[strStat]][ind+1])] == strSeriesDiff1Short){
            dblStat1=listRetTest[[strStat]][ind];
          }
          if( matPerm[strtoi(listRetTest[[strStat]][ind+1])] == strSeriesDiff2Short){
            dblStat2=listRetTest[[strStat]][ind];
          }
          ind=ind+1;
        }
        if(TRUE == exists("dblStat1") && TRUE == exists("dblStat2")){
          listRet[[strStat]] = append(listRet[[strStat]], as.numeric(dblStat1) - as.numeric(dblStat2));
        }
      }
    }
  }

  return( unlist(listRet) );
}

# run the bootstrap flow for each independent variable value
for(strIndyVal in listIndyVal){

  dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
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
    listCount=list();

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
      listCount[[ paste(listPerm, collapse=',' ) ]]=nrow(dfStatsPerm);
      if( 1 > nrow(dfStatsPerm) ){ next; }

      # add the contingency table constituents for this series permutation to the boot list
      strPerm = escapeStr(paste(listPerm, sep="_"));
      if ( boolAggCtc    ){
        listFields = c("total", "fy_oy", "fy_on", "fn_oy", "fn_on");
      } else if( boolAggSl1l2  ){
        listFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "mae");
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
        listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut[[strIndyVar]] == strIndyVal);
        if( 1 < intNumReplicates ){

          # calculate the confidence interval for the current stat and series permutation
          stBootCI = Sys.time();
          bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
          dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");

        }

        # store the bootstrapped stat value and CI values in the output dataframe
        dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
        dfOut[listOutInd,]$nstats = listCount[[ paste(listPerm, collapse=',' ) ]];
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
        diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];
        diffSeriesName = paste("DIFF(",diffSeriesVec[1],"-",diffSeriesVec[2],")" ,collapse="");
        # build a indicator list for the pertinent rows in the output dataframe
        listOutInd = rep(TRUE, nrow(dfOut));
        strDiffVar = listSeriesVar[length(listSeriesVar)];
        listOutInd = listOutInd & (dfOut[[strDiffVar]] == diffSeriesName);
        listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut[[strIndyVar]] == strIndyVal);
        if( 1 < intNumReplicates ){

          # calculate the confidence interval for the current stat and series permutation
          stBootCI = Sys.time();
          bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
          dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");

        }
        # store the bootstrapped stat value and CI values in the output dataframe
        dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
        dfOut[listOutInd,]$nstats = nrow(dfStatsPerm)
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
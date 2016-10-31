
# parse the command line arguments
strInputInfoFile = "data/plot_00473_20110926_131630.agg_pct.info";
listArgs = commandArgs(TRUE);
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("agg_stat.R\ninput file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblStatTime		= 0;

# read the input data file into a data frame
dfStatsRec = read.delim(strInputDataFile);
intYMax = 1;
if( 0 < length(listSeries2Val) ){ intYMax = 2; }

# sort the dataset by init time, lead time and independent variable
listFields = names(dfStatsRec);
dfStatsRec = dfStatsRec[order(dfStatsRec$fcst_valid_beg),];
dfStatsRec = dfStatsRec[order(dfStatsRec$fcst_init_beg),];
dfStatsRec = dfStatsRec[order(dfStatsRec[[strIndyVar]]),];

dfStatsRecAxis1= data.frame();
dfStatsRecAxis2= data.frame();

# build a list for output permutations



# run event equalizer, if requested
if( boolEventEqual  ){
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
      for(strSeriesVal in names(listSeries1Val)){
        vectValPerms = c();
        for(index in 1:length(listSeries1Val[[strSeriesVal]])){
          vectValPerms= append(vectValPerms, strsplit(listSeries1Val[[strSeriesVal]][index], ",")[[1]]);
        }
        fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep1Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms & dfStatsRec$stat_name %in% strDep1Stat,  ];
        fPlot = eventEqualize(fPlot, strIndyVar, listIndyVal, listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
        dfPlot1 = rbind(dfPlot1, fPlot);
      }
    }
  }

  #if the second Y axis is present - run event equalizer on Y1
  # and then run event equalizer on Y1 and Y2 equalized data
  if(length(listSeries2Val) > 0){
    dfPlot2 = data.frame();
    for( strDep2Name in names(listDep2Plot) ){
      for( strDep2Stat in listDep2Plot[[strDep2Name]] ){
        for(strSeriesVal in names(listSeries2Val)){
          vectValPerms = c();
          for(index in 1:length(listSeries2Val[[strSeriesVal]])){
            vectValPerms= append(vectValPerms, strsplit(listSeries2Val[[strSeriesVal]][index], ",")[[1]]);
          }
          fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep2Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms & dfStatsRec$stat_name %in% strDep2Stat,  ];
          fPlot = eventEqualize(fPlot, strIndyVar, listIndyVal, listSeries2Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
          dfPlot2 = rbind(dfPlot2, fPlot);
        }
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
strAfrerEqualizeFile = sub("\\.data.agg_pct$", ".dataAfterEq", strInputDataFile, perl=TRUE);
write.table(dfStatsRec, file=strAfrerEqualizeFile, quote=FALSE, row.names=FALSE, col.names=TRUE, sep = "\t");

}

for(intY in 1:intYMax){
  dfStatsRecAxis=data.frame();

  if( 1 == intY ){
    listSeriesVal = listSeries1Val;
    listDiffSeries = listDiffSeries1;
    listStat = listStat1;
  }
  if( 2 == intY ){
    listSeriesVal = listSeries2Val;
    listDiffSeries = listDiffSeries2;
    listStat = listStat2;
  }

  # add the series variables and values, including a difference series if appropriate
  listOut = listSeriesVal;
  listSeriesVar = names(listSeriesVal);


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


}
if( 1 > nrow(dfStatsRec) ){
  if( boolEventEqual  ){
    stop("ERROR: eventEqualize() removed all data");
  }else{
    stop("ERROR: Database query returned empy set");
  }
}


# build a dataframe (dfOut) to store the aggregated statistics
listOutPerm = list();
intNumOut = nrow(matOut);
for(strStaticVar in names(listStaticVal)){
  #strStaticValFmt = formatR(listStaticVal[[strStaticVar]]);
  #listOutPerm[[strStaticVar]] = rep(strStaticValFmt, intNumOut);
  listOutPerm[[strStaticVar]] = rep(listStaticVal[[strStaticVar]], intNumOut);
}
for(intOutCol in 1:ncol(matOut)){
  if     ( intOutCol <  ncol(matOut) - 1 ){
    listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
  } else if( intOutCol == ncol(matOut) - 1 ){
    listOutPerm[[ strIndyVar ]] = matOut[,intOutCol];
  }else                  {
    listOutPerm$stat_name = matOut[,intOutCol];
  }
}
listOutPerm$stat_value = rep(NA, intNumOut);
listOutPerm$stat_bcl = rep(NA, intNumOut);
listOutPerm$stat_bcu = rep(NA, intNumOut);
dfOut = data.frame(listOutPerm);


#remove extra columns
dfStatsRec$equalize <- NULL;
for(fixedVar in names(listFixedVal)){
  dfStatsRec[[fixedVar]] <- NULL;
}
# run the aggregation flow for each independent variable value
for(strIndyVal in listIndyVal){

  dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
  if( 1 > nrow(dfStatsIndy) ){ next; }

  # for each series group, aggregate the PCTs
  for(intY in 1:intYMax){

    # build permutations for each plot series
    if( 1 == intY ){
      matPerm = permute(listSeries1Val);
      listStat = listStat1;
      listDiffSeries = listDiffSeries1;
    }
    if( 2 == intY ){
      matPerm = permute(listSeries2Val);
      listStat = listStat2;
      listDiffSeries = listDiffSeries2;
    }

    # run the aggregation flow for each series permutation
    for(intPerm in 1:nrow(matPerm)){
      listPerm = matPerm[intPerm,];

      # build the data set pertinent to this series permutation
      dfStatsPerm = dfStatsIndy;
      for(intSeriesVal in 1:length(listSeriesVar)){
        strSeriesVar = listSeriesVar[intSeriesVal];
        strSeriesVal = listPerm[intSeriesVal];
        if( grepl("^[0-9]+$", strSeriesVal) ){ strSeriesVal = as.numeric(strSeriesVal); }
        dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] == strSeriesVal,];
      }
      if( 1 > nrow(dfStatsPerm) ){ next; }

      # record the starting time before calculating stats
      stStat = Sys.time();

      # aggregate the data set and format it into a single PCT table
      dfAggPerm = dfStatsPerm[1,];
      for(oy_i in seq(11, ncol(dfAggPerm), 3)){ dfAggPerm[1,oy_i] = sum(dfStatsPerm[,oy_i]); }
      for(on_i in seq(12, ncol(dfAggPerm), 3)){ dfAggPerm[1,on_i] = sum(dfStatsPerm[,on_i]); }
      dfPctPerm = data.frame(
        thresh_i	= c( t( dfAggPerm[1,seq(10, ncol(dfAggPerm), 3)] ) ),
        oy_i		= c( t( dfAggPerm[1,seq(11, ncol(dfAggPerm), 3)] ) ),
        on_i		= c( t( dfAggPerm[1,seq(12, ncol(dfAggPerm), 3)] ) )
      );

      # calculate vectors and constants to use below
      dfPctPerm$n_i = dfPctPerm$oy_i + dfPctPerm$on_i;		# n_j.
      dfPctPerm = dfPctPerm[0 != dfPctPerm$n_i,];
      T = sum(dfPctPerm$n_i);									# T
      oy_total = sum(dfPctPerm$oy_i);							# n_.1
      o_bar = oy_total / T;									# n_.1 / T
      dfPctPerm$o_bar_i = dfPctPerm$oy_i / dfPctPerm$n_i;		# o_bar_i

      # row-based calculations
      dfPctPerm$oy_tp			= dfPctPerm$oy_i / T;
      dfPctPerm$on_tp			= dfPctPerm$on_i / T;
      dfPctPerm$calibration	= dfPctPerm$oy_i / dfPctPerm$n_i;
      dfPctPerm$refinement	= dfPctPerm$n_i / T;
      dfPctPerm$likelihood	= dfPctPerm$oy_i / oy_total;
      dfPctPerm$baserate		= dfPctPerm$o_bar_i;

      # table-based stat calculations
      listStat = list(
        reliability	= sum( dfPctPerm$n_i * (dfPctPerm$thresh - dfPctPerm$o_bar_i)^2 ) / T,
        resolution	= sum( dfPctPerm$n_i * (dfPctPerm$o_bar_i - o_bar)^2 ) / T,
        uncertainty	= o_bar * (1 - o_bar),
        baser		= o_bar
      );
      listStat$brier	= listStat$reliability - listStat$resolution + listStat$uncertainty;
      listStat$b_ci	= calcBrierCI(dfPctPerm, listStat$brier, dblAlpha);

      # build the dataframe for calculating and use the trapezoidal method roc_auc
      dfROC = calcPctROC(dfPctPerm);
      dfAUC = rbind(data.frame(thresh=0, n11=0, n10=0, n01=0, n00=0, pody=1, pofd=1), dfROC);
      dfAUC = rbind(dfAUC, data.frame(thresh=0, n11=0, n10=0, n01=0, n00=0, pody=0, pofd=0));
      listStat$roc_auc = 0;
      for(r in 2:nrow(dfAUC)){
        listStat$roc_auc = listStat$roc_auc + 0.5*(dfAUC[r-1,]$pody + dfAUC[r,]$pody)*(dfAUC[r-1,]$pofd - dfAUC[r,]$pofd);
      }

      # add the stat calculation time to the total stat time
      dblStatTime = dblStatTime + as.numeric(Sys.time() - stStat, units="secs");

      # build a indicator list for the pertinent rows in the output dataframe
      listOutInd = rep(TRUE, nrow(dfOut));
      for(intSeriesVal in 1:length(listSeriesVar)){
        strSeriesVar = listSeriesVar[intSeriesVal];
        strSeriesVal = listPerm[intSeriesVal];
        listOutInd = listOutInd & (dfOut[[strSeriesVar]] == strSeriesVal);
      }
      listOutInd = listOutInd & (dfOut[[strIndyVar]] == strIndyVal);

      # for each stat in the current permutation, populate the stat_value and stat_cis
      for(strStat in unique(dfOut[listOutInd,]$stat_name)){
        listStatInd = listOutInd & dfOut$stat_name == strStat;
        strStatFmt = tolower(sub("PSTD_", "", strStat));
        dfOut[listStatInd,]$stat_value = listStat[[strStatFmt]];
        if( "brier" == strStatFmt ){
          dfOut[listStatInd,]$stat_bcl = listStat$brier - listStat$b_ci;
          dfOut[listStatInd,]$stat_bcu = listStat$brier + listStat$b_ci;
        }
      }
    }
    #add diff series
    if(length(listDiffSeries) > 0){
    }
  }
}

cat("    stat time: ", formatTimeSpan(dblStatTime), "\n",
    "   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
    sep="");

# write the output dataframe to the specified output file
write.table(dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t");


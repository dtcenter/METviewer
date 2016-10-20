library(boot);

#event equalisation against previously calculated cases for boolModeRatioPlot

# parse the command line arguments
strInputInfoFile = "";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
  strInputInfoFile = listArgs[1];
}

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# read the plot data
dfStatsRec = read.delim(strInputDataFile);

if(boolEventEqual){
  eeStats = read.delim(strInputEeDataFile);
  dfPlot1 = data.frame();

  for( strDep1Name in names(listDep1Plot) ){
    for(strSeriesVal in names(listSeries1Val)){
      vectValPerms = c();
      for(index in 1:length(listSeries1Val[[strSeriesVal]])){
        vectValPerms= append(vectValPerms, strsplit(listSeries1Val[[strSeriesVal]][index], ",")[[1]]);
      }
      fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep1Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms,  ];
      eeStatsEqualize = eeStats[eeStats$fcst_var == strDep1Name & eeStats[[strSeriesVal]] %in% vectValPerms,  ];
      eeStatsEqualizeUnique = unique(eeStatsEqualize$equalize);

      fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,    eeStatsEqualizeUnique);
      dfPlot1 = rbind(dfPlot1, fPlot);
    }
  }

  if(length(listSeries2Val) > 0){
    dfPlot2 = data.frame();
    for( strDep2Name in names(listDep2Plot) ){
      for(strSeriesVal in names(listSeries2Val)){
        vectValPerms = c();
        for(index in 1:length(listSeries2Val[[strSeriesVal]])){
          vectValPerms= append(vectValPerms, strsplit(listSeries2Val[[strSeriesVal]][index], ",")[[1]]);
        }
        fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep2Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms,  ];
        eeStatsEqualize = eeStats[eeStats$fcst_var == strDep1Name & eeStats[[strSeriesVal]] %in% vectValPerms,  ];
        eeStatsEqualizeUnique = unique(eeStatsEqualize$equalize);
        fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,   eeStatsEqualizeUnique);
        dfPlot2 = rbind(dfPlot2, fPlot);
      }

    }
    dfStatsRec = rbind(dfPlot1, dfPlot2);

  } else{
    dfStatsRec = dfPlot1;
  }

}



write.table( dfStatsRec, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );



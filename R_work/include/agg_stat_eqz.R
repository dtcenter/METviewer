library(boot);
library(reticulate)

use_virtualenv("/d3/projects/METViewer/METviewer_py3.6.3")
source_python('/d3/projects/METViewer/METcalcpy/metcalcpy/event_equalize_against_values.py')
sys = import('sys')
py_config()
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


#read the input data file into a data frame
#if fcst_var is a special char - read it as-is and do not convert
sampleData = read.delim(strInputDataFile,nrows=5);
if ( nrow(sampleData) > 0){
  classes <- sapply(sampleData, class);
  numeric_columns <- c('stat_value');
  integer_columns <- c('fcst_lead');
  character_columns <- c('fcst_var', 'model', 'stat_name', 'object_id', 'cluster_id');
  for(col in 1:ncol(sampleData)){
    if ( is.element( names(classes)[col] , numeric_columns) ){
      classes[col]='numeric';
    } else if ( is.element( names(classes)[col] , integer_columns) ){
      classes[col]='integer';
    } else if ( is.element( names(classes)[col] , character_columns) ){
      classes[col]='character';
    }
  }

  # read the plot data
  dfStatsRec = read.delim(strInputDataFile,colClasses = classes);

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

        #fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,    eeStatsEqualizeUnique);
        fPlot = event_equalize_against_values(fPlot, strIndyVar,    eeStatsEqualizeUnique);
        sys$stdout$flush()
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
          #fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,   eeStatsEqualizeUnique);
          fPlot = event_equalize_against_values(fPlot, strIndyVar,   eeStatsEqualizeUnique);
          sys$stdout$flush()
          dfPlot2 = rbind(dfPlot2, fPlot);
        }

      }
      dfStatsRec = rbind(dfPlot1, dfPlot2);

    } else{
      dfStatsRec = dfPlot1;
    }

  }



  write.table( dfStatsRec, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );
}



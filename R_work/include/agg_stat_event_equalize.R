#performe event equalisation and srore the result in the file for the future usage by agg_stat calculations
# parse the command line arguments
strInputInfoFile = "";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
  strInputInfoFile = listArgs[1];
}
cat("agg_stat_event_equalize.R\ninput file: ", strInputInfoFile, "\n", sep="");

library(reticulate)

#use_python("/Volumes/d1/tatiana/miniconda3/envs/METviewer/bin/python", required = TRUE)
#use_virtualenv("/Volumes/d1/tatiana/miniconda3/envs/METviewer")
#source_python('/Users/tatiana/PycharmProjects/METviewer/event_equalize.py')

use_virtualenv("/d3/projects/METViewer/METviewer_py3.6.3")
source_python('/d3/projects/METViewer/METcalcpy/metcalcpy/event_equalize.py')


sys = import('sys')

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");



#if fcst_var is a special char - read it as-is and do not convert
sampleData = read.delim(strInputDataFile,nrows=5);
classes <- sapply(sampleData, class);
numeric_columns <- c('stat_value', 'stat_bcl', 'stat_bcu', 'stat_ncu', 'stat_ncl','fbs', 'fss');
integer_columns <- c('fcst_lead','nstats');
character_columns <- c('fcst_var', 'model', 'stat_name')
for(col in 1:ncol(sampleData)){
  if ( is.element( names(classes)[col] , numeric_columns) ){
    classes[col]='numeric';
  } else if ( is.element( names(classes)[col] , integer_columns) ){
    classes[col]='integer';
  } else if ( is.element( names(classes)[col] , character_columns) ){
    classes[col]='character';
  }
}

dfPlot = read.delim(strInputDataFile,colClasses = classes);

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
  for(strSeriesVal in names(listSeries1Val)){
    fPlot = dfPlot;
    for(strSeriesVal in names(listSeries1Val)){
      vectValPerms = c();
      for(index in 1:length(listSeries1Val[[strSeriesVal]])){
        vectValPerms= append(vectValPerms, strsplit(listSeries1Val[[strSeriesVal]][index], ",")[[1]]);
      }
      fPlot = fPlot[fPlot$fcst_var == strDep1Name & fPlot[[strSeriesVal]] %in% vectValPerms ,  ];

    }
    #fPlot = eventEqualize(fPlot, strIndyVar, listIndy, listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
    fPlot = event_equalize(fPlot, strIndyVar, listIndy, listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
    sys$stdout$flush()
    dfPlot1 = rbind(dfPlot1, fPlot);
  }

}

#if the second Y axis is present - run event equalizer on Y1
# and then run event equalizer on Y1 and Y2 equalized data
if(length(listSeries2Val) > 0){
  dfPlot2 = data.frame();
  for( strDep2Name in names(listDep2Plot) ){
    for(strSeriesVal in names(listSeries2Val)){
      fPlot = dfPlot;
      for(strSeriesVal in names(listSeries2Val)){
        vectValPerms = c();
        for(index in 1:length(listSeries2Val[[strSeriesVal]])){
          vectValPerms= append(vectValPerms, strsplit(listSeries2Val[[strSeriesVal]][index], ",")[[1]]);
        }
        fPlot = fPlot[dfPlot$fcst_var == strDep1Name & fPlot[[strSeriesVal]] %in% vectValPerms ,  ];

      }
      #fPlot = eventEqualize(fPlot, strIndyVar, listIndy, listSeries2Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
      fPlot = event_equalize(fPlot, strIndyVar, listIndy, listSeries2Val, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
      sys$stdout$flush()
      dfPlot2 = rbind(dfPlot2, fPlot);
    }

  }
  dfPlot = rbind(dfPlot1, dfPlot2);
  listSeriesVal=list();
  for( seriesVal in names(listSeries1Val) ){
    listSeriesVal[[seriesVal]] = append(listSeries1Val[[seriesVal]], listSeries2Val[[seriesVal]]);
  }
  listSeriesVal$model = append(listSeries1Val$model,listSeries2Val$model)
  #dfPlot = eventEqualize(dfPlot, strIndyVar, listIndy, listSeriesVal, listFixVars,listFixVarVals,boolEqualizeByIndep, FALSE);
  dfPlot = event_equalize(fPlot, strIndyVar, listIndy, listSeriesVal, listFixVars,listFixVarVals, boolEqualizeByIndep, FALSE);
  sys$stdout$flush()
} else{
  dfPlot = dfPlot1;
}
write.table( dfPlot, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );
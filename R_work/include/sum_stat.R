#web .libPaths("/common/data/web/metviewer/dev/r-lib");
#batch .libPaths("/common/data/apps/metviewer/dev/web/r-lib");
library(parallel)
library(data.table)


strInputInfoFile = "~/plot_00124_20130923_082001.agg_stat.info";

listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");
source("statistics.R");

prepareCalc = function(d){
  row = setNames(as.list(d), names(dfStatsRec));
  dblStat = do.call( paste("calc", row$stat_name, sep=""), list(d=lapply(row[statFields], as.numeric)) );
  if(!is.na(dblStat)){
    row$stat_value = round(dblStat, 6);
  }else{
    row$stat_value = dblStat
  }
  for(field in  statFields){
      row[[field]] = NULL;
  }
  if( !is.null(row$equalize) ){
    row$equalize = NULL;
  }
  fwrite(row, file=strOutputFile,append=TRUE,quote=FALSE,sep="\t",row.names=FALSE,col.names=FALSE, na = "NA");
}

# variables for performance bookkeeping
stStart      = Sys.time();

if ( boolSumCtc    ){
  statFields = c("total", "fy_oy", "fy_on", "fn_oy", "fn_on");
} else if( boolSumSl1l2  ){
  statFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "mae");
} else if( boolSumGrad  ){
  statFields = c("total", "fgbar", "ogbar", "mgbar", "egbar");
} else if( boolSumSal1l2  ){
  statFields = c("total", "fabar", "oabar", "foabar", "ffabar", "ooabar", "mae");
} else if( boolSumVl1l2  ){
  statFields = c("total", "ufbar", "vfbar", "uobar", "vobar", "uvfobar", "uvffbar","uvoobar","f_speed_bar","o_speed_bar");
} else if( boolSumVal1l2  ){
  statFields = c("total", "ufabar", "vfabar", "uoabar", "voabar", "uvfoabar", "uvffabar","uvooabar");
}

# read the input data file into a data frame
#if fcst_var is a special char - read it as-is and do not convert
sampleData = read.delim(strInputDataFile,nrows=5);
ind = names(sampleData) %in% statFields;

newColumns = t(names(sampleData)[!ind]);
write.table(newColumns, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t", append=FALSE, col.names=FALSE);

if ( nrow(sampleData) > 0){
  classes <- sapply(sampleData, class);
  numeric_columns <- c('stat_value', 'stat_bcl', 'stat_bcu', 'stat_ncu', 'stat_ncl','fbs', 'fss', 'fbar',  'obar',  'fobar',  'ffbar',  'oobar',  'var_mean');
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

  dfStatsRec = fread(strInputDataFile,colClasses = classes,data.table=FALSE);
  #change names for sal1l2
  if( boolSumSal1l2  ){
    colnames(dfStatsRec)[colnames(dfStatsRec)=="fabar"] <- "fbar";
    colnames(dfStatsRec)[colnames(dfStatsRec)=="oabar"] <- "obar";
    colnames(dfStatsRec)[colnames(dfStatsRec)=="foabar"] <- "fobar";
    colnames(dfStatsRec)[colnames(dfStatsRec)=="ffabar"] <- "ffbar";
    colnames(dfStatsRec)[colnames(dfStatsRec)=="ooabar"] <- "oobar";
    statFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "mae");
  }
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
            fPlot = fPlot[fPlot$fcst_var == strDep1Name & fPlot[[strSeriesVal]] %in% vectValPerms & fPlot$stat_name %in% strDep2Stat,  ];
          }

          fPlot = eventEqualize(fPlot, strIndyVar, listIndyVal, listSeries2Val, listFixVars,listFixVarVals, boolEqualizeByIndep, boolMulti);
          dfPlot2 = rbind(dfPlot2, fPlot);
        }
      }

      dfStatsRec = rbind(dfPlot1, dfPlot2);
      listSeriesVal=list();
      listSeriesVal$model = append(listSeries1Val$model,listSeries2Val$model)
      for( seriesVal in names(listSeries1Val) ){
        listSeriesVal[[seriesVal]] = unique(append(listSeries1Val[[seriesVal]], listSeries2Val[[seriesVal]]));
      }
      dfStatsRec = eventEqualize(dfStatsRec, strIndyVar, listIndyVal, listSeriesVal, listFixVars,listFixVarVals,boolEqualizeByIndep, TRUE);
    }else{
      dfStatsRec = dfPlot1;
    }
    strAfrerEqualizeFile = sub("\\.agg_stat", ".dataAfterEq", strInputDataFile, perl=TRUE);
    #fwrite(dfStatsRec, append=FALSE,file=strAfrerEqualizeFile, quote=FALSE, row.names=FALSE, col
  # .names=TRUE,sep = "\t", fwrite);
  }


  if( 1 > nrow(dfStatsRec) ){
    if( boolEventEqual  ){
      stop("ERROR: eventEqualize() removed all data");
    }else{
      stop("ERROR: Database query returned empy set");
    }
  }

  dfStatsRecTranspose=data.frame(t(format(dfStatsRec, digits=10)),stringsAsFactors = FALSE);
  a=mclapply( dfStatsRecTranspose ,prepareCalc, mc.cores=6, mc.set.seed=1);
  #a=lapply( dfStatsRecTranspose,prepareCalc);



}
cat(
  "   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
  sep="");


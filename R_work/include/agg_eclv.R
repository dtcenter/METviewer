#.libPaths("/common/data/web/metviewer/dev/r-lib");
library(boot);
library(gsl);
library(stats);



# parse the command line arguments
strInputInfoFile = "~/plot_00124_20130923_082001.agg_stat.info";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("agg_eclv.R\ninput file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblBootTime		= 0;
dblBootCITime	= 0;
intNumBoots		= 0;


steps = seq(clStep, 1-clStep, clStep);
# read the input data file into a data frame
#if fcst_var is a special char - read it as-is and do not convert
sampleData = read.delim(strInputDataFile,nrows=5);

if ( nrow(sampleData) > 0){
  classes <- sapply(sampleData, class);
  numeric_columns <- c('stat_value', 'stat_bcl', 'stat_bcu', 'stat_ncu', 'stat_ncl','fbs', 'fss', 'fbar',	'obar',	'fobar',	'ffbar',	'oobar',	'var_mean');
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

  dfStatsRec = read.delim(strInputDataFile,colClasses = classes);


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


  # run event equalizer, if requested
  if( boolEventEqual  ){
    library(reticulate)

    #use_python("/Volumes/d1/tatiana/miniconda3/envs/METviewer/bin/python", required = TRUE)
    #use_virtualenv("/Volumes/d1/tatiana/miniconda3/envs/METviewer")
    #source_python('/Users/tatiana/PycharmProjects/METviewer/event_equalize.py')

    use_virtualenv("/d3/projects/METViewer/METviewer_py3.6.3")
    source_python('/d3/projects/METViewer/METcalcpy/metcalcpy/event_equalize.py')


    sys = import('sys')
    boolMulti=FALSE;
    #run event equalizer on Y1
    #list all fixed variables
    listFixVars=c();
    listFixVarVals = c();
    if(length(listFixedValEx) > 0){
      for(index in 1:length(listFixedValEx)){
        if(is.null(listFixVars)){
          listFixVars = names(listFixedValEx[index]);
          listFixVarVals = permute(listFixedValEx[index]);
        }else{
          listFixVars = append(listFixVars, names(listFixedValEx[index]));
          listFixVarVals = append(listFixVarVals, permute(listFixedValEx[index]));
        }
      }
    }
    #dfStatsRec = eventEqualize(dfStatsRec, "stat_name", c("ECLV"), listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, boolMulti);
    dfStatsRec = event_equalize(dfStatsRec, "stat_name", c("ECLV"), listSeries1Val, listFixVars,listFixVarVals, boolEqualizeByIndep, boolMulti);
    sys$stdout$flush()
    strAfrerEqualizeFile = sub("\\.agg_stat", ".dataAfterEq", strInputDataFile, perl=TRUE);
    write.table(dfStatsRec, file=strAfrerEqualizeFile, quote=FALSE, row.names=FALSE, col.names=TRUE, sep = "\t");
  }

  listBoot = list();
  x=c('thresh_i', 'x_pnt_i', 'y_pnt_i', 'stat_bcl', 'stat_bcu', 'nstats');

  if(is.null(names(listSeries1Val))){
    listSeriesVar=c();
    matOut = matrix(nrow=0, ncol=0);
    matPerm = matrix();
    matCIPerm = matrix();
  }else{
    listSeriesVar = names(listSeries1Val);
    matOutSeries = permute(listSeries1Val);
    matOut = matrix(nrow=0, ncol=length(names(listSeries1Val)));
    matOut = rbind(matOut, matOutSeries);
    # build permutations for each plot series
    matPerm = permute(listSeries1Val);
    matCIPerm = permute(listSeries1Val);
    x=c( listSeriesVar,x);
  }





  if( 1 > nrow(dfStatsRec) ){
    if( boolEventEqual  ){
      stop("ERROR: eventEqualize() removed all data");
    }else{
      stop("ERROR: Database query returned empy set");
    }
  }



  calcECLV = function(d){
    # generate the ECLV scores from the contingency tables
    # ECLV = value(c(d$n11, d$n01, d$n10, d$n00), cl = steps, plot = FALSE);
    ECLV = value(c(d$n11,  d$n10, d$n01, d$n00), cl = steps, plot = FALSE);
    return(ECLV);
  }



  # booter function
  booter.iid = function(d, i){

    strStat = "ECLV";

    dfSeriesSums = data.frame();
      if(boolAggCtc == TRUE){
        dfSeriesSums = data.frame(
          total	= sum( as.numeric(d[i,][[  "total" ]]), na.rm=TRUE ),
          n11	= sum( as.numeric(d[i,][[ "fy_oy" ]]), na.rm=TRUE ), #fy_oy
          n10	= sum( as.numeric(d[i,][[ "fy_on"]]), na.rm=TRUE ),  #fy_on
          n01	= sum( as.numeric(d[i,][[ "fn_oy" ]]), na.rm=TRUE ), #fn_oy
          n00	= sum( as.numeric(d[i,][[ "fn_on" ]]), na.rm=TRUE )  #fn_on
        );
      }else{
        dfSeriesSums = data.frame(
          n11  = sum( as.numeric ( d[d$thresh_i >  thresh,]$oy_i ) ),
          n10  = sum( as.numeric ( d[d$thresh_i >  thresh,]$on_i ) ),
          n01  = sum( as.numeric ( d[d$thresh_i <= thresh,]$oy_i ) ),
          n00  = sum( as.numeric ( d[d$thresh_i <= thresh,]$on_i ) )
        );
      }
      dblStat = do.call( paste("calc", strStat, sep=""), list(d=dfSeriesSums) );

    return( unlist(dblStat) );
  }


  dfOut = data.frame(matrix(ncol = length(x), nrow = 0));
  colnames(dfOut) = x;

  # run the bootstrap flow for each series permutation
  for(intPerm in 1:nrow(matPerm)){
    if(all(is.na(matPerm[intPerm,]))){
      listPerm=c();
    }else{
      listPerm = matPerm[intPerm,];
    }

    dfOutLocal = data.frame(matrix(ncol = length(x), nrow = length(steps)+1));
    colnames(dfOutLocal) = x;

    # build the data set pertinent to this series permutation
    dfStatsPerm = dfStatsRec;

    if(length(listSeriesVar)>0){

      for(intSeriesVal in 1:length(listSeriesVar)){
        strSeriesVar = listSeriesVar[intSeriesVal];
        strSeriesVal = listPerm[intSeriesVal];

        dfOutLocal[ strSeriesVar ] = rep(strSeriesVal, length(steps)+1);
        if( grepl("^[0-9]+$", strSeriesVal) ){
          strSeriesVal = as.integer(strSeriesVal);
          vectValPerms = strSeriesVal;
        }else{
          vectValPerms= strsplit(strSeriesVal, ",")[[1]];
        }
        vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.integer(x); }else{x=x} })
        dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] %in% vectValPerms,];
      }
    }
    if("thresh_i" %in% colnames(dfStatsPerm)){
      listThresh = unique( sort( dfStatsPerm$thresh_i ) );
    }else{
      listThresh = c(0);
    }


    dfOutLocal$thresh_i = rep(NA, length(steps)+1);
    dfOutLocal$x_pnt_i = rep(NA, length(steps)+1);
    dfOutLocal$y_pnt_i = rep(NA, length(steps)+1);
    dfOutLocal$stat_bcl = rep(NA, length(steps)+1);
    dfOutLocal$stat_bcu = rep(NA, length(steps)+1);
    dfOutLocal$nstats=rep(0, length(steps)+1);

    if( 1 > nrow(dfStatsPerm) ){ next; }

    if(boolAggCtc == "TRUE"){
      listFields = c("total", "fy_oy", "fy_on", "fn_oy", "fn_on");
    }else{
      listFields = c( "thresh_i", "oy_i", "on_i");
    }

    for(strCount in listFields){
      listCounts = dfStatsPerm[[strCount]];
      listBoot[[strCount]] = listCounts;
    }


    # ensure that all count lists have the same length, appending with NAs where necessary
    intCountLength = -1;
    for(strCountName in names(listBoot)){
      if( intCountLength < length(listBoot[[strCountName]]) ){
        intCountLength = length(listBoot[[strCountName]]);
      }
    }
    for(strCountName in names(listBoot)){
      if( intCountLength > length(listBoot[[strCountName]]) ){
        listBoot[[strCountName]] = append( listBoot[[strCountName]], rep(NA, intCountLength - length(listBoot[[strCountName]])) );
      }
    }

    # bootstrap the series data
    dfBoot = data.frame(listBoot, check.names=FALSE);
    stBoot = Sys.time();
    if( !is.na(intRandomSeed) ){
      set.seed(intRandomSeed);
    }


    bootStatThresh = c();
    for( threshInd in 1:length(listThresh) ){
      thresh = listThresh[[threshInd]];
      bootStat = try( boot(dfBoot, booter.iid, R=intNumReplicates , parallel = 'multicore', ncpus=4 ) );
      bootStatThresh[[threshInd]] = bootStat;
    }

    dblBootTime = dblBootTime + as.numeric(Sys.time() - stBoot, units="secs");
    intNumBoots = intNumBoots + 1;


    # for each series permutation , generate confidence intervals and store the output
    for(intPerm in 1:nrow(matCIPerm)){
      if(all(is.na(matCIPerm[intPerm,]))){
        listPerm = c();
      }else{
        listPerm = matCIPerm[intPerm,];
      }

      # build a indicator list for the pertinent rows in the output dataframe
      listOutInd = rep(TRUE, nrow(dfOut));
      if(length(listSeriesVar) > 0){
        for(intSeriesVal in 1:length(listSeriesVar)){
          strSeriesVar = listSeriesVar[intSeriesVal];
          strSeriesVal = listPerm[intSeriesVal];
          listOutInd = listOutInd & (dfOut[[strSeriesVar]] == strSeriesVal);
        }
      }
      if(strIndyVar != ""){
        listOutInd = listOutInd &  (dfOut[[strIndyVar]] == strIndyVal);
      }

      for( threshInd in 1:length(listThresh) ){
        thresh = listThresh[[threshInd]];

        stepsWithBaser = bootStatThresh[[threshInd]]$t0[substring(names(bootStatThresh[[threshInd]]$t0), 1, nchar('c')) == 'c'];
        valuesWithBaser = bootStatThresh[[threshInd]]$t0[substring(names(bootStatThresh[[threshInd]]$t0), 1, nchar('V')) == 'V'];
        if( 1 < intNumReplicates ){

          # calculate the confidence interval for the current stat and series permutation for the each step
          stBootCI = Sys.time();

          stat_bcl <- vector("list", length(stepsWithBaser));
          stat_bcu <- vector("list", length(stepsWithBaser));
          for(index in 1:length(stepsWithBaser)){
            #the first index points to "vmax" so add 1
            if( length(valuesWithBaser) != sum(is.na(valuesWithBaser)) ){
              bootCI = boot.ci(bootStatThresh[[threshInd]], conf=(1 - dblAlpha), type=strCIType, index=index+1);
            }else{
              bootCI=NA;
            }
            strCIParm = strCIType;
            if( strCIType == "perc" ){
              strCIParm = "percent";
            }
            if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
              stat_bcl[index] = bootCI[[strCIParm]][4];
              stat_bcu[index] = bootCI[[strCIParm]][5];
            }
          }
          dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");

        }

        # store the bootstrapped stat value and CI values in the output dataframe
        for(stepInd in 1:length(stepsWithBaser)){
          dfOutLocal$thresh_i[stepInd] = thresh;
          dfOutLocal$x_pnt_i[stepInd] = stepsWithBaser[[stepInd]];
          dfOutLocal$y_pnt_i[stepInd] = valuesWithBaser[[stepInd]];
          dfOutLocal$nstats=nrow(dfBoot);
          if( 1 < intNumReplicates ){
            if( !is.null(stat_bcl[[stepInd]])){
              dfOutLocal$stat_bcl[stepInd] = stat_bcl[[stepInd]];
            }
            if( !is.null(stat_bcu[[stepInd]])){
              dfOutLocal$stat_bcu[stepInd] = stat_bcu[[stepInd]];
            }
          }
        }
        dfOut = merge(dfOut, dfOutLocal, all=TRUE);
      }
    }
  }
}


write.table(dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t", append=boolAppend, col.names=!boolAppend);


cat(
  "    boot time: ", formatTimeSpan(dblBootTime), "\n",
  " boot.ci time: ", formatTimeSpan(dblBootCITime), "\n",
  "   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
  "        boots: ", intNumBoots, "\n",
  "   replicates: ", intNumReplicates, "\n",
  "         seed: ", intRandomSeed, "\n",
  "        stats: ", length(listStat1), "\n",
  sep="");

# clean up
cat("agg_eclv.R done\n");
#web .libPaths("/common/data/web/metviewer/dev/r-lib");
#batch .libPaths("/common/data/apps/metviewer/dev/web/r-lib");
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
source("statistics.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblBootTime		= 0;
dblBootCITime	= 0;
intNumBoots		= 0;

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

  intYMax = 1;
  if( 0 < length(listSeries2Val) ){ intYMax = 2; }

  # sort the dataset by init time, lead time and independent variable
  listFields = names(dfStatsRec);

  listGroupToValue=c();
  if(length(names(listSeries1Val)) > 0){
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
  if(length(names(listSeries2Val)) > 0){
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
    if( isContourDiff ){
      diffSeriesVec = unlist(listDiffSeries1[1]);
      derivedCurveName = getDerivedCurveName(diffSeriesVec);
      #check if the input frame already has diff series  ( from calculation agg stats )
      listSeriesVar = names(listFixedValEx);
      derivedVar1=''
      derivedVar2=''
      derivedVarVal1=tail(unlist(strsplit(diffSeriesVec[1], ' ')), n=1)
      derivedVarVal2=tail(unlist(strsplit(diffSeriesVec[2], ' ')), n=1)
      for ( index in 1:length(listFixedValEx)) {
        name =names(listFixedValEx[index])
        if(derivedVarVal1 %in%  unlist(permute(listFixedValEx[index]))){
          derivedVar1 = name;
        }
        if(derivedVarVal2 %in%  unlist(permute(listFixedValEx[index]))){
          derivedVar2 = name;
        }
      }
      listOut[[derivedVar1]] = listFixedValEx[[derivedVar1]][[names(listFixedValEx[[derivedVar1]])]];
      if( derivedVar1 != derivedVar2){
        listOut[[derivedVar2]] = listFixedValEx[[derivedVar2]][[names(listFixedValEx[[derivedVar2]])]];
      }
    }
    listSeriesVar = names(listSeriesVal);
    if( length(listDiffSeries) >0){
      if(isContourDiff){
        strDiffVar=derivedVar1;
      }else{
        strDiffVar = listSeriesVar[length(listSeriesVar)];
      }
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
        listDiffVal = listSeriesVal;

        listDiffVal[[strDiffVar]]=NULL;
        diffSeriesVec = listDiffSeries[[diffSeriesName]];
        listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
        listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];
        if(isContourDiff){
          strStat1 = listSeriesDiff1[length(listSeriesDiff1) - 1];
          strStat2 = listSeriesDiff2[length(listSeriesDiff2) - 1];
        }else{
          strStat1 = listSeriesDiff1[length(listSeriesDiff1)];
          strStat2 = listSeriesDiff2[length(listSeriesDiff2)];

          for(var in listSeriesVar){
            if( !is.null(listDiffVal[[var]]) && intersect(listDiffVal[[var]], listSeriesDiff1) == intersect(listDiffVal[[var]], listSeriesDiff2) ){
              listDiffVal[[var]] = intersect(listDiffVal[[var]], listSeriesDiff1);
            }
          }
        }
        derivedCurveName = getDerivedCurveName(diffSeriesVec);
        listDiffVal[[strDiffVar]] = derivedCurveName;
        listDiffVal[[strIndyVar]] = listIndyVal;
        if(strStat1 == strStat2){
          listDiffVal$stat_name = strStat1;
        }else{
          listDiffVal$stat_name = paste(strStat1,strStat2,collapse="", sep=",");
        }
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
        if(isContourDiff){
          listOutPerm[[ names(listOut)[intOutCol] ]] = matOut[,intOutCol];
        }else{
          listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
        }
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




  findIndexes = function(diffSeriesVec, listGroupToValue, matPerm){
    listSeriesDiff1 <- strsplit(trim(diffSeriesVec[1]), " ")[[1]];
    listSeriesDiff2 <- strsplit(trim(diffSeriesVec[2]), " ")[[1]];

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
    return ( c(indPerm1, indPerm2));

  }
  findIndexesContourDiff = function(diffSeriesVec, var, matPerm){
    listSeriesDiff1 <- strsplit(trim(diffSeriesVec[1]), " ")[[1]];
    listSeriesDiff2 <- strsplit(trim(diffSeriesVec[2]), " ")[[1]];


    strSeriesDiff1Short = tail(listSeriesDiff1, n=1);
    strSeriesDiff2Short = tail(listSeriesDiff2, n=1);
    listSeriesDiff1Short = c(var, strSeriesDiff1Short);
    listSeriesDiff2Short = c(var, strSeriesDiff2Short);
    indPerm1=0;
    indPerm2=0;


    for(intPerm in 1:nrow(matPerm)){
      d1 = listSeriesDiff1Short %in% matPerm[intPerm,];
      d2 = listSeriesDiff2Short %in% matPerm[intPerm,];
      if( all(d1) ){
        indPerm1=intPerm;
      }
      if( all(d2) ){
        indPerm2=intPerm;
      }
      if(indPerm1 != 0 && indPerm2 != 0)
        break;
    }
    return ( c(indPerm1, indPerm2));

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
        dfSeriescustom_sums = data.frame(
          total	= custom_sum( as.numeric(d[i,][[ paste(strPerm, "total", sep="_") ]]), na.rm=TRUE ),
          fy_oy	= custom_sum( as.numeric(d[i,][[ paste(strPerm, "fy_oy", sep="_") ]]), na.rm=TRUE ),
          fy_on	= custom_sum( as.numeric(d[i,][[ paste(strPerm, "fy_on", sep="_") ]]), na.rm=TRUE ),
          fn_oy	= custom_sum( as.numeric(d[i,][[ paste(strPerm, "fn_oy", sep="_") ]]), na.rm=TRUE ),
          fn_on	= custom_sum( as.numeric(d[i,][[ paste(strPerm, "fn_on", sep="_") ]]), na.rm=TRUE )
        );
      }  else if ( boolAggSl1l2 ){ # perform the aggregation of the sampled SL1L2 lines
        listTotal	= d[i,][[ paste(strPerm, "total", sep="_") ]];
        total		= custom_sum(listTotal, na.rm=TRUE);
        dfSeriescustom_sums = data.frame(
          total	= total,
          fbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fbar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total,
          obar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          fobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          ffbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          oobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          mae   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
        );
      }  else if ( boolAggGrad ){ # perform the aggregation of the sampled grad lines
        listTotal	= d[i,][[ paste(strPerm, "total", sep="_") ]];
        total		= custom_sum(listTotal, na.rm=TRUE);

        dfSeriescustom_sums = data.frame(
          total	= total,
          fgbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fgbar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total,
          ogbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "ogbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          mgbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "mgbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          egbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "egbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total
        );
      }  else if ( boolAggVl1l2 ){ # perform the aggregation of the sampled VL1L2 lines
        listTotal  = d[i,][[ paste(strPerm, "total", sep="_") ]];
        total    = custom_sum(listTotal, na.rm=TRUE);
        dfSeriescustom_sums = data.frame(
          total  = total,
          ufbar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "ufbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          vfbar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "vfbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uobar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          vobar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "vobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uvfobar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvfobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uvffbar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvffbar", sep="_") ]] )   * listTotal, na.rm=TRUE ) / total,
          uvoobar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvoobar", sep="_") ]] )  *listTotal, na.rm=TRUE ) / total,
          f_speed_bar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "f_speed_bar", sep="_") ]] )*listTotal, na.rm=TRUE ) / total,
          o_speed_bar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "o_speed_bar", sep="_") ]] )*listTotal, na.rm=TRUE ) / total
        );
      }  else if ( boolAggVal1l2 ){ # perform the aggregation of the sampled VAL1L2 lines
        listTotal  = d[i,][[ paste(strPerm, "total", sep="_") ]];
        total    = custom_sum(listTotal, na.rm=TRUE);
        dfSeriescustom_sums = data.frame(
          total  = total,
          ufabar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "ufabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          vfabar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "vfabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uoabar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uoabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          voabar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "voabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uvfoabar  = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvfoabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          uvffabar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvffabar", sep="_") ]] )   * listTotal, na.rm=TRUE ) / total,
          uvooabar   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "uvooabar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
        );
      }  else if ( boolAggSal1l2 ){ # perform the aggregation of the sampled SAL1L2 lines
        listTotal  = d[i,][[ paste(strPerm, "total", sep="_") ]];
        total		= custom_sum(listTotal, na.rm=TRUE);
        dfSeriescustom_sums = data.frame(
          total	= total,
          fbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          obar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "oabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          fobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "foabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          ffbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "ffabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          oobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "ooabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          mae   = custom_sum( as.numeric( d[i,][[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
        );
      }  else if ( boolAggSsvar ){ # perform the aggregation of the sampled SSVAR lines
        listTotal  = d[i,][[ paste(strPerm, "bin_n", sep="_") ]];
        total    = custom_sum(listTotal, na.rm=TRUE);
        dfSeriescustom_sums = data.frame(
          total  = total,
          fbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          obar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          fobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          ffbar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          oobar	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          varmean	= custom_sum( as.numeric( d[i,][[ paste(strPerm, "var_mean", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
          binn	=  total
        );
      } else if( boolAggNbrCnt ){ # perform the aggregation of the sampled NBR_CNT lines
        listTotal = d[i,][[ paste(strPerm, "total", sep="_") ]];
        total = custom_sum( as.numeric(listTotal), na.rm=TRUE);
        listFbs = d[i,][[ paste(strPerm, "fbs", sep="_") ]];
        listFss = d[i,][[ paste(strPerm, "fss", sep="_") ]];
        listAFss = d[i,][[ paste(strPerm, "afss", sep="_") ]];
        listUFss = d[i,][[ paste(strPerm, "ufss", sep="_") ]];
        listFRate = d[i,][[ paste(strPerm, "f_rate", sep="_") ]];
        listORate = d[i,][[ paste(strPerm, "o_rate", sep="_") ]];

        listFss[listFss == -9999] = NA;

        dblFbs = custom_sum(listTotal * listFbs, na.rm=TRUE) / total;

        dblFssDen = custom_sum( (listFbs / (1.0 - listFss)) * listTotal, na.rm=TRUE) / total;
        dblFss = 1.0 - dblFbs / dblFssDen;
        if( !is.finite(dblFss) ){ dblFss = NA; }

        dblFRate = custom_sum(listTotal * listFRate, na.rm=TRUE) / total;
        dblORate = custom_sum(listTotal * listORate, na.rm=TRUE) / total;

        dblAFssNum = 2.0*dblFRate*dblORate;
        dblAFssDen = dblFRate*dblFRate + dblORate*dblORate;
        dblAFss = dblAFssNum / dblAFssDen;
        if( !is.finite(dblAFss) ){ dblAFss = NA; }

        dblUFss = 0.5 + dblORate/2.0;

        dfSeriescustom_sums = data.frame(
          total = total,
          fbs = dblFbs,
          fss = dblFss,
          afss = dblAFss,
          ufss = dblUFss,
          f_rate = dblFRate,
          o_rate = dblORate
        );
      } else if( boolAggPct ){
        dfPerm = d[i,][substring(colnames(d[i,][1,]), 1, nchar(strPerm)) == strPerm];
        dfAggPerm = dfPerm[1,];
        #drop equalize column
        if( paste(strPerm, "equalize", sep="_") %in% colnames(dfAggPerm) ){
          dfAggPerm = dfAggPerm[ , -which(colnames(dfAggPerm) %in% c(paste(strPerm, "equalize", sep="_")))]
        }
        oy_i_index = grep("oy_i", colnames(dfAggPerm), value=FALSE);
        on_i_index = grep("on_i", colnames(dfAggPerm), value=FALSE);
        thresh_i_index = grep("thresh_i", colnames(dfAggPerm), value=FALSE);

        for(oy_i in oy_i_index){
          dfAggPerm[1,oy_i] = custom_sum(dfPerm[,oy_i], na.rm = TRUE);
        }
        for(on_i in on_i_index){
          dfAggPerm[1,on_i] = custom_sum(dfPerm[,on_i], na.rm = TRUE);
        }



        if(!is.na(dfAggPerm[1,thresh_i_index][[1]])){
          dfPctPerm = data.frame(
            thresh_i	= c( t( dfAggPerm[1,thresh_i_index] ) ),
            oy_i		= c( t( dfAggPerm[1,oy_i_index] ) ),
            on_i		= c( t( dfAggPerm[1, on_i_index] ) )
          );

          # calculate vectors and constants to use below
          dfPctPerm$n_i = dfPctPerm$oy_i + dfPctPerm$on_i;		# n_j.
          dfPctPerm = dfPctPerm[0 != dfPctPerm$n_i,];

          dfPctPerm$o_bar_i = dfPctPerm$oy_i / dfPctPerm$n_i;		# o_bar_i

          # row-based calculations
          dfPctPerm$oy_tp			= dfPctPerm$oy_i / T;
          dfPctPerm$on_tp			= dfPctPerm$on_i / T;
          dfPctPerm$calibration	= dfPctPerm$oy_i / dfPctPerm$n_i;
          dfPctPerm$refinement	= dfPctPerm$n_i / T;
          dfPctPerm$likelihood	= dfPctPerm$oy_i / oy_total;
          dfPctPerm$baserate		= dfPctPerm$o_bar_i;


          # table-based stat calculations
          dfSeriescustom_sums = list(
            reliability	= custom_sum( dfPctPerm$n_i * (dfPctPerm$thresh - dfPctPerm$o_bar_i)^2 ) / T,
            resolution	= custom_sum( dfPctPerm$n_i * (dfPctPerm$o_bar_i - o_bar)^2 ) / T,
            uncertainty	= o_bar * (1 - o_bar),
            baser		= o_bar,
            calibration = dfPctPerm$calibration,
            n_i = dfPctPerm$n_i
          );
          #dfSeriescustom_sums$b_ci	= calcBrierCI(dfPctPerm, dfSeriescustom_sums$brier, dblAlpha);

          # build the dataframe for calculating and use the trapezoidal method roc_auc
          dfROC = calcPctROC(dfPctPerm);
          dfAUC = rbind(data.frame(thresh=0, n11=0, n10=0, n01=0, n00=0, pody=1, pofd=1), dfROC);
          dfAUC = rbind(dfAUC, data.frame(thresh=0, n11=0, n10=0, n01=0, n00=0, pody=0, pofd=0));
          dfSeriescustom_sums$roc_auc = 0;
          for(r in 2:nrow(dfAUC)){
            dfSeriescustom_sums$roc_auc = dfSeriescustom_sums$roc_auc + 0.5*(dfAUC[r-1,]$pody + dfAUC[r,]$pody)*(dfAUC[r-1,]$pofd - dfAUC[r,]$pofd);
          }

        }else{
          dfSeriescustom_sums = list(
            reliability	= NA,
            resolution	= NA,
            uncertainty	= NA,
            baser		= NA,
            roc_auc=NA
          );
        }

      }

      # return a value for each statistic
      for(strStat in listStat){
        # calculate and store the statistic
        dblStat = do.call( paste("calc", strStat, sep=""), list(d=dfSeriescustom_sums) );
        listRet[[strStat]] = append(listRet[[strStat]], dblStat);
      }
    }
    if(length(listDiffSeries) > 0){
      for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
        #get  names of DIFF series
        diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];

        if(isContourDiff){
          for(var in unique(matPerm[,1])){
            indexes = findIndexesContourDiff(diffSeriesVec, var, matPerm);
            if(indexes[1] != 0 && indexes[2] != 0){
              listRet[[paste(strStat1,strStat2, indexes[1], indexes[2],diffSeriesVec[length(diffSeriesVec)],sep = "_")]] = calcDerivedCurveValue(listRet[[strStat1]][indexes[1]],listRet[[strStat2]][indexes[2]], derivedCurveName);
            }
          }
        }else{
          indexes = findIndexes(diffSeriesVec, listGroupToValue, matPerm);
          if(indexes[1] != 0 && indexes[2] != 0){
            listRet[[paste(strStat1,strStat2, indexes[1], indexes[2],diffSeriesVec[length(diffSeriesVec)],sep = "_")]] = calcDerivedCurveValue(listRet[[strStat1]][indexes[1]],listRet[[strStat2]][indexes[2]], derivedCurveName);
          }
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
        if(isContourDiff ){
          diffSeriesVec = unlist(listDiffSeries1[1]);
          derivedCurveName = getDerivedCurveName(diffSeriesVec);
          #check if the input frame already has diff series  ( from calculation agg stats )
          derivedVar1=''
          derivedVar2=''
          derivedVarVal1=tail(unlist(strsplit(diffSeriesVec[1], ' ')), n=1)
          derivedVarVal2=tail(unlist(strsplit(diffSeriesVec[2], ' ')), n=1)
          for ( index in 1:length(listFixedValEx)) {
            name =names(listFixedValEx[index])
            if(derivedVarVal1 %in%  unlist(permute(listFixedValEx[index]))){
              derivedVar1 = name;
            }
            if(derivedVarVal2 %in%  unlist(permute(listFixedValEx[index]))){
              derivedVar2 = name;
            }
          }
          listSeriesVal = listSeries1Val;
          listSeriesVal[[derivedVar1]] = listFixedValEx[[derivedVar1]][[names(listFixedValEx[[derivedVar1]])]];
          if( derivedVar1 != derivedVar2){
            listSeriesVal[[derivedVar2]] = listFixedValEx[[derivedVar2]][[names(listFixedValEx[[derivedVar2]])]];
          }
          matPerm = permute(listSeriesVal);
          listSeriesVar = names(listSeriesVal);
        }else{
          matPerm = permute(listSeries1Val);
        }
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
        dfStatsPermAllIndy = NaN;

        for(intSeriesVal in 1:length(listSeriesVar)){
          strSeriesVar = listSeriesVar[intSeriesVal];
          strSeriesVal = listPerm[intSeriesVal];
          if( grepl("^[0-9]+$", strSeriesVal) ){
            strSeriesVal = as.integer(strSeriesVal);
            vectValPerms = strSeriesVal;
          }else{
            vectValPerms= strsplit(strSeriesVal, ",")[[1]];
          }
          vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.integer(x); }else{x=x} })
          dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] %in% vectValPerms,];
          if( boolAggPct ){
            if( is.nan(dfStatsPermAllIndy) ){
              dfStatsPermAllIndy = dfStatsRec;
            }
            dfStatsPermAllIndy = dfStatsPermAllIndy[dfStatsPermAllIndy[[strSeriesVar]] %in% vectValPerms,];
          }
        }
        if( 1 > nrow(dfStatsPerm) ){ next; }



        # add the contingency table constituents for this series permutation to the boot list
        strPerm = escapeStr(paste(listPerm, sep="_"));
        if ( boolAggCtc    ){
          listFields = c("total", "fy_oy", "fy_on", "fn_oy", "fn_on");
        } else if( boolAggSl1l2  ){
          listFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "mae");
        } else if( boolAggGrad  ){
          listFields = c("total", "fgbar", "ogbar", "mgbar", "egbar");
        } else if( boolAggSal1l2  ){
          listFields = c("total", "fabar", "oabar", "foabar", "ffabar", "ooabar", "mae");
        } else if( boolAggVl1l2  ){
          listFields = c("total", "ufbar", "vfbar", "uobar", "vobar", "uvfobar", "uvffbar","uvoobar", "f_speed_bar", "o_speed_bar");
        } else if( boolAggVal1l2  ){
          listFields = c("total", "ufabar", "vfabar", "uoabar", "voabar", "uvfoabar", "uvffabar","uvooabar");
        } else if( boolAggSsvar  ){
          listFields = c("total", "fbar", "obar", "fobar", "ffbar", "oobar", "var_mean", "bin_n");
        } else if( boolAggNbrCnt ){
          listFields = c("total", "fbs", "fss");
        }else if( boolAggPct ){
          #calc T abd o_bar for pct
          n_i = dfStatsPermAllIndy$oy_i + dfStatsPermAllIndy$on_i;
          T = custom_sum(n_i);									# T
          oy_total = custom_sum(dfStatsPermAllIndy$oy_i);							# n_.1
          o_bar = oy_total / T;
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
      if( !is.na(intRandomSeed) ){
        set.seed(intRandomSeed);
      }
      bootStat = try(boot(dfBoot, booter.iid, R=intNumReplicates , parallel = 'multicore', ncpus=4 ));

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
          if( !isContourDiff ){
            if( 1 < intNumReplicates ){

              # calculate the confidence interval for the current stat and series permutation
              stBootCI = Sys.time();
              bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
              dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");
            }

            # store the bootstrapped stat value and CI values in the output dataframe
            dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
            dfOut[listOutInd,]$nstats = nrow(dfStatsPerm[dfStatsPerm$stat_name == strStat,]);
            if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
              if( strCIType == "perc" && !is.null(bootCI[["percent"]]) ){
                dfOut[listOutInd,]$stat_bcl = bootCI[["percent"]][4];
                dfOut[listOutInd,]$stat_bcu = bootCI[["percent"]][5];

              }else if( strCIType == "norm" && !is.null(bootCI[["normal"]]) ){
                dfOut[listOutInd,]$stat_bcl = bootCI[["normal"]][2];
                dfOut[listOutInd,]$stat_bcu = bootCI[["normal"]][3];

              }else if( strCIType == "basic" && !is.null(bootCI[["basic"]]) ){
                dfOut[listOutInd,]$stat_bcl = bootCI[["basic"]][4];
                dfOut[listOutInd,]$stat_bcu = bootCI[["basic"]][5];

              }else if( strCIType == "bca" && !is.null(bootCI[["bca"]]) ){
                dfOut[listOutInd,]$stat_bcl = bootCI[["bca"]][4];
                dfOut[listOutInd,]$stat_bcu = bootCI[["bca"]][5];

              }else if( strCIType == "stud" && !is.null(bootCI[["student"]]) ){
                dfOut[listOutInd,]$stat_bcl = bootCI[["student"]][4];
                dfOut[listOutInd,]$stat_bcu = bootCI[["student"]][5];
              }else{
                dfOut[listOutInd,]$stat_bcl = NA;
                dfOut[listOutInd,]$stat_bcu = NA;
              }
            } else {
              dfOut[listOutInd,]$stat_bcl = NA;
              dfOut[listOutInd,]$stat_bcu = NA;
            }
          } else{
            dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
          }
          intBootIndex = intBootIndex + 1;
        }
      }
      if(length(listDiffSeries) > 0){
        if(isContourDiff){
          for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
            for(var in unique(matPerm[,1])){
              diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];
              listSeriesDiff1 <- strsplit(trim(diffSeriesVec[1]), " ")[[1]];
              listSeriesDiff2 <- strsplit(trim(diffSeriesVec[2]), " ")[[1]];

              strStat1= listSeriesDiff1[2];
              strStat2= listSeriesDiff2[2];


              derivedCurveName = getDerivedCurveName(diffSeriesVec);

              # build a indicator list for the pertinent rows in the output dataframe
              listOutInd = rep(TRUE, nrow(dfOut));
              strDiffVar = listSeriesVar[length(listSeriesVar)];
              listOutInd = listOutInd & (dfOut[[strDiffVar]] == derivedCurveName);

              if(strStat1 == strStat2){
                stat_name = strStat1;
              }else{
                stat_name = paste(strStat1,strStat2,collapse = "", sep=",");
              }
              listOutInd1 = listOutInd & (dfOut$stat_name == stat_name)  & (dfOut[[strIndyVar]] == strIndyVal) & (dfOut[[listSeriesVar[1]]] == var);
              dfOut[listOutInd1,]$stat_value = bootStat$t0[intBootIndex];
              dfOut[listOutInd1,]$nstats = 0;
              dfOut[listOutInd1,]$stat_bcl = NA;
              dfOut[listOutInd1,]$stat_bcu = NA;
              intBootIndex = intBootIndex + 1;
            }
          }
        } else{
          for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....


            diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];
            listSeriesDiff1 <- strsplit(trim(diffSeriesVec[1]), " ")[[1]];
            listSeriesDiff2 <- strsplit(trim(diffSeriesVec[2]), " ")[[1]];

            strStat1= listSeriesDiff1[length(listSeriesDiff1)];
            strStat2= listSeriesDiff2[length(listSeriesDiff2)];


            derivedCurveName = getDerivedCurveName(diffSeriesVec);

            # build a indicator list for the pertinent rows in the output dataframe
            listOutInd = rep(TRUE, nrow(dfOut));
            strDiffVar = listSeriesVar[length(listSeriesVar)];
            listOutInd = listOutInd & (dfOut[[strDiffVar]] == derivedCurveName);

            if(strStat1 == strStat2){
              stat_name = strStat1;
            }else{
              stat_name = paste(strStat1,strStat2,collapse = "", sep=",");
            }

            listOutInd1 = listOutInd & (dfOut$stat_name == stat_name)  & (dfOut[[strIndyVar]] == strIndyVal);

            diff_sig = NA;
            # Use the empirical distribution and just compute the ratio of differences < 0 if needed
            if(length(diffSeriesVec) == 3 && diffSeriesVec[3] == 'DIFF_SIG'){
              if( !all(is.na(bootStat$t[,intBootIndex])) ){
                indexes = findIndexes(diffSeriesVec, listGroupToValue, matPerm);

                mean_bootStat = mean(bootStat$t[,intBootIndex], na.rm = TRUE);
                bootStat_under_H0 = bootStat$t[,intBootIndex] - mean_bootStat;

                pval = mean( abs( bootStat_under_H0 ) <= abs( bootStat$t0[intBootIndex] ),na.rm = TRUE );
                diff_sig = perfectScoreAdjustment(bootStat$t0[indexes[1]], bootStat$t0[indexes[2]], listDep1Plot[[1]], pval);

              }
              dfOut[listOutInd1,]$stat_value = diff_sig;
            }else{
              if( 1 < intNumReplicates ){
                stBootCI = Sys.time();
                bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
                dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");
              }
              dfOut[listOutInd1,]$stat_value = bootStat$t0[intBootIndex];
            }

            dfOut[listOutInd1,]$nstats = 0;
            if( exists("bootCI") == TRUE && class(bootCI) == "bootci" && is.na(diff_sig)){
              if( strCIType == "perc" && !is.null(bootCI[["percent"]]) ){
                dfOut[listOutInd1,]$stat_bcl = bootCI[["percent"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["percent"]][5];

              }else if( strCIType == "norm" && !is.null(bootCI[["normal"]]) ){
                dfOut[listOutInd1,]$stat_bcl = bootCI[["normal"]][2];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["normal"]][3];

              }else if( strCIType == "basic" && !is.null(bootCI[["basic"]]) ){
                dfOut[listOutInd1,]$stat_bcl = bootCI[["basic"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["basic"]][5];

              }else if( strCIType == "bca" && !is.null(bootCI[["bca"]]) ){
                dfOut[listOutInd1,]$stat_bcl = bootCI[["bca"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["bca"]][5];

              }else if( strCIType == "stud" && !is.null(bootCI[["student"]]) ){
                dfOut[listOutInd1,]$stat_bcl = bootCI[["student"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["student"]][5];
              }else{
                dfOut[listOutInd1,]$stat_bcl = NA;
                dfOut[listOutInd1,]$stat_bcu = NA;
              }
            } else {
              dfOut[listOutInd1,]$stat_bcl = NA;
              dfOut[listOutInd1,]$stat_bcu = NA;
            }

            intBootIndex = intBootIndex + 1;
          }
        }
      }

    } # end for(intY in 1:intYMax)
  } # end for(strIndy in listIndy)

  #remove rows with stat_value=NA
  #dfOut = dfOut[complete.cases(dfOut$stat_value),];




  write.table(dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t", append=boolAppend, col.names=!boolAppend);

}
cat(
  "    boot time: ", formatTimeSpan(dblBootTime), "\n",
  " boot.ci time: ", formatTimeSpan(dblBootCITime), "\n",
  "   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
  "        boots: ", intNumBoots, "\n",
  "   replicates: ", intNumReplicates, "\n",
  "         seed: ", intRandomSeed, "\n",
  "        stats: ", length(listStat), "\n",
  sep="");

# clean up
cat("agg_stat.R done\n");
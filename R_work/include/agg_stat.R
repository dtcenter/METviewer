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
cat("input file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");
source("statistics.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblBootTime		= 0;
dblBootCITime	= 0;
intNumBoots		= 0;

exemptedVars <- c('SSVAR_Spread', 'SSVAR_RMSE')

options(stringsAsFactors = FALSE)
# read the input data file into a data frame
#if fcst_var is a special char - read it as-is and do not convert
sampleData = read.delim(strInputDataFile,nrows=5);
listStat = NA;
if ( nrow(sampleData) > 0){
  classes <- sapply(sampleData, class);
  numeric_columns <- c('stat_value', 'stat_bcl', 'stat_bcu', 'stat_ncu', 'stat_ncl','fbs', 'fss','afss', 'ufss', 'f_rate', 'o_rate',
                       'fbar',	'obar',	'fobar',	'ffbar',	'oobar',	'var_mean','total', 'me', 'rmse', 'crps',
                       'crpss', 'ign', 'spread', 'me_oerr','rmse_oerr','spread_oerr', 'spread_plus_oerr', 'mae', 'crpscl', 'crps_emp', 'crpscl_emp', 'crpss_emp'
  );
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

  #replace thresh_i values for reliability plot
  if(strIndyVar == "thresh_i" && boolAggPct){
    listIndyVal = unique(sort(dfStatsRec$thresh_i));
  }

  lineTypes = list(boolCtc=boolAggCtc, boolNbrCtc=boolAggNbrCtc, boolSl1l2=boolAggSl1l2, boolGrad=boolAggGrad, boolVl1l2=boolAggVl1l2,
                   boolVal1l2=boolAggVal1l2, boolSal1l2=boolAggSal1l2, boolSsvar=boolAggSsvar,boolEcnt=boolAggEcnt,
                   boolNbrCnt=boolAggNbrCnt, boolPct=boolAggPct, boolRps=boolAggRps)

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
        if( grepl(':', strVar)) {
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
        if( grepl(':', strVar)) {
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
              regexPr=gregexpr("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})", listSeries1Val[[strSeriesVal]][index]);
              matches=regmatches(listSeries1Val[[strSeriesVal]][index], regexPr)
              vectValPerms1=matches[[1]]
              if (length(vectValPerms1) == 0){
                vectValPerms1= strsplit(listSeries1Val[[strSeriesVal]][index], ":")[[1]];
              }
              vectValPerms= append(vectValPerms, vectValPerms1);
          }
          for (i in 1:length(vectValPerms)){
            if (vectValPerms[i] == 'NA'){
              vectValPerms[i] = NA
            }
          }
          fPlot = fPlot[ fPlot[[strSeriesVal]] %in% vectValPerms ,  ];
          if('fcst_var' %in%  names(fPlot)){
            fPlot = fPlot[ fPlot$fcst_var == strDep1Name,  ];
          }
          if('stat_name' %in%  names(fPlot)){
            fPlot = fPlot[ fPlot$stat_name %in% strDep1Stat,  ];
          }
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
              regexPr=gregexpr("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})", listSeries2Val[[strSeriesVal]][index]);
              matches=regmatches(listSeries2Val[[strSeriesVal]][index], regexPr)
              vectValPerms1=matches[[1]]
              if (length(vectValPerms1) == 0){
                vectValPerms1= strsplit(listSeries2Val[[strSeriesVal]][index], ":")[[1]];
              }
            }
            for (i in 1:length(vectValPerms)){
              if (vectValPerms[i] == 'NA'){
                vectValPerms[i] = NA
              }
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
      if(length(listDep1Plot) > 0 ){
        fcst_var=names(listDep1Plot)[1]
      }else{
        fcst_var=NA
      }
    }
    if( 2 == intY ){
      listSeriesVal = listSeries2Val;
      listStat = listStat2;
      listDiffSeries = listDiffSeries2;
      if(length(listDep2Plot) > 0 ){
        fcst_var=names(listDep2Plot)[1]
      }else{
        fcst_var=NA
      }
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

    if(is.null(names(listSeriesVal))){
      listSeriesVar = c();
    }else{
      listSeriesVar = names(listSeriesVal);
    }
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
    listOut$fcst_var = fcst_var;
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
        listDiffVal$fcst_var = fcst_var;
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
  if(strIndyVar != ""){
    for(intOutCol in 1:ncol(matOut)){
      if ( intOutCol <  ncol(matOut) - 2 ){
        if(isContourDiff){
          listOutPerm[[ names(listOut)[intOutCol] ]] = matOut[,intOutCol];
        }else{
          listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
        }
      } else if ( intOutCol == ncol(matOut) - 2 ){
        listOutPerm[[ strIndyVar ]] = matOut[,intOutCol];
      } else if ( intOutCol == ncol(matOut) - 1 ){
        listOutPerm$stat_name = matOut[,intOutCol];
      }else{
        listOutPerm$fcst_var = matOut[,intOutCol];
      }
    }
  }else{
    for(intOutCol in 1:ncol(matOut)){
      if ( intOutCol ==   ncol(matOut)-1   ){
        listOutPerm$stat_name = matOut[,intOutCol];
      }else if ( intOutCol ==   ncol(matOut)   ){
        listOutPerm$fcst_var = matOut[,intOutCol];
      }else{
        listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol];
      }
    }

  }
  listOutPerm$stat_value = rep(NA, intNumOut);
  listOutPerm$stat_btcl = rep(NA, intNumOut);
  listOutPerm$stat_btcu = rep(NA, intNumOut);
  dfOut = data.frame(listOutPerm);
  dfOut$nstats=rep(0, intNumOut);

  # initialize the histogram array
  listHist = c();




  findIndexes = function(diffSeriesVec, listGroupToValue, matPerm){
    listSeriesDiff1 <- strsplit(trim(diffSeriesVec[1]), " ")[[1]];
    listSeriesDiff2 <- strsplit(trim(diffSeriesVec[2]), " ")[[1]];

    #remove empty strings
    listSeriesDiff1 = listSeriesDiff1[listSeriesDiff1 != ""];
    listSeriesDiff2 = listSeriesDiff2[listSeriesDiff2 != ""];

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

  SUPER_STAT <- c('CTC_FY_OY',
                  'CTC_FY_ON',
                  'CTC_FN_OY',
                  'CTC_FN_ON',
                  'CTC_OY',
                  'CTC_ON',
                  'CTC_FY',
                  'CTC_FN',
                  'SL1L2_TOTAL',
                  'GRAD_TOTAL',
                  'SAL1L2_TOTAL',
                  'SSVAR_TOTAL',
                  'CTC_TOTAL',
                  'VL1L2_TOTAL',
                  'VAL1L2_TOTAL',
                  'ECNT_TOTAL',
                  'NBR_CTC_TOTAL',
                  'NBR_CNT_TOTAL',
                  'PCT_TOTAL',
                  'RPS_TOTAL')

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
      dfSeriescustom_sums = calcSeriesSums (d[i,], strPerm, lineTypes, intPerm, T,oy_total, o_bar);

      # return a value for each statistic
      for(strStat in listStat){
        # calculate and store the statistic
        dblStat = do.call( paste("calc", strStat, sep=""), list(d=dfSeriescustom_sums) );
        # if this is a special stat - need to devide to the number of stats in case more than one stat is selected
        if (strStat %in% SUPER_STAT){
          dblStat = dblStat/length(listStat)
        }
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
      if( is.numeric(strIndyVal) ){
        vectValIndy = strIndyVal
      }else{
        vectValIndy = strsplit(strIndyVal, ":")[[1]];
      }
      if(strIndyVar == 'fcst_valid_beg' || strIndyVar == 'fcst_init_beg'){
        dfStatsIndy = dfStatsRec[as.character(dfStatsRec[[strIndyVar]]) %in% vectValIndy,];
      } else if ( is.na(strIndyVal) || strIndyVal == 'NA') {
        dfStatsIndy = dfStatsRec[is.na(dfStatsRec[[strIndyVar]]),]
      } else {
        dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] %in% vectValIndy,];
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
        if(length(listDep1Plot) >0 ){
          fcst_var=names(listDep1Plot)[1]
        }else{
          fcst_var=NA
        }
      }
      if( 2 == intY ){
        matPerm = permute(listSeries2Val);
        listStat = listStat2;
        matCIPerm = matCIPerm2;
        listDiffSeries = listDiffSeries2;
        if(length(listDep2Plot) >0 ){
          fcst_var=names(listDep2Plot)[1]
        }else{
          fcst_var=NA
        }
      }
      listBoot = list();

      # run the bootstrap flow for each series permutation

      #for pct stats
      T=c();
      o_bar=c();
      oy_total=c();

      hasAggFieldSeries = FALSE
      # look if there is a field that need to be aggregated first - the field with ';'
      for(i in 1:dim(matPerm)[1]) {
        for(j in 1:dim(matPerm)[2]) {
          if( grepl(':', matPerm[i,j]) ){
            hasAggFieldSeries = TRUE
            break
          }
        }
      }



      for(intPerm in 1:nrow(matPerm)){
        listPerm = matPerm[intPerm,];

        # build the data set pertinent to this series permutation
        dfStatsPerm = dfStatsIndy;
        dfStatsPermAllIndy = dfStatsRec;
        listSeriesVarLenght = length(listSeriesVar);
        if(listSeriesVarLenght == 0){
          listSeriesVarLenght = 1;
        }

        for (intSeriesVal in 1 : listSeriesVarLenght) {
          strSeriesVar = listSeriesVar[intSeriesVal];
          if (! is.null(strSeriesVar)) {

            strSeriesVal = listPerm[intSeriesVal];

            if (grepl("^[0-9]+$", strSeriesVal)) {
              strSeriesVal = as.integer(strSeriesVal);
              vectValPerms = strSeriesVal;
            }else {
                # check if the value contains date(s) and groups and parse it accordingly
                regexPr=gregexpr("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})", strSeriesVal);
                matches=regmatches(strSeriesVal, regexPr)
                vectValPerms=matches[[1]]
                if (length(vectValPerms) == 0){
                  vectValPerms= strsplit(strSeriesVal, ":")[[1]];
                }
            }
            vectValPerms = lapply(vectValPerms, function(x) {if (grepl("^[0-9]+$", x)) { x = as.integer(x);}else {x = x}})
            if (vectValPerms[1] == 'NA'){
              dfStatsPerm = dfStatsPerm[is.na(dfStatsPerm[[strSeriesVar]]),];
            } else{
              dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] %in% vectValPerms,];
            }
            # add fcst var
            if (!is.na(fcst_var)){
              dfStatsPerm = dfStatsPerm[dfStatsPerm[['fcst_var']] == fcst_var,];
            }
            if (boolAggPct) {
              if (vectValPerms[1] == 'NA'){
                dfStatsPermAllIndy = dfStatsPermAllIndy[is.na(dfStatsPermAllIndy[[strSeriesVar]]),];
              } else{
                dfStatsPermAllIndy = dfStatsPermAllIndy[dfStatsPermAllIndy[[strSeriesVar]] %in% vectValPerms,];
              }
            }
          }
        }

        if (1 > nrow(dfStatsPerm)) { next;}

        # add the contingency table constituents for this series permutation to the boot list
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
        } else if( boolAggEcnt  ){
          listFields = c("total", "me", "rmse", "crps","crpss", "ign", "spread", "me_oerr","rmse_oerr","spread_oerr", "spread_plus_oerr",
                         "crpscl", "crps_emp", "crpscl_emp", "crpss_emp");
        } else if( boolAggNbrCnt ){
          listFields = c("total", "fbs", "fss",'afss', 'ufss', 'f_rate', 'o_rate');
        } else if( boolAggRps ){
          listFields = c("total", 'rps', 'rpss', 'rps_comp');
        }else if( boolAggPct ){
          #calc T and o_bar for pct
          oy_i_index = grep("oy_i", colnames(dfStatsPermAllIndy), value = FALSE);
          on_i_index = grep("on_i", colnames(dfStatsPermAllIndy), value = FALSE);

          dfPctPerm = data.frame(
            oy_i = c(t(dfStatsPermAllIndy[, oy_i_index])),
            on_i = c(t(dfStatsPermAllIndy[, on_i_index]))
          );
          dfPctPerm$n_i = dfPctPerm$oy_i + dfPctPerm$on_i;
          dfPctPerm$o_bar_i = dfPctPerm$oy_i / dfPctPerm$n_i;        # o_bar_i
          T[intPerm] = sum(dfPctPerm$n_i);
          oy_total[intPerm] = sum(dfPctPerm$oy_i);
          o_bar[intPerm] = oy_total[intPerm] / T[intPerm];
        }

        #aggregate series vals if needed by fcst_valid_beg and fcst_lead
        strPerm = escapeStr(paste(intPerm, sep="_", collapse="_"));
        if( hasAggFieldSeries && any(grepl(':', listPerm)) ){
          dfStatsPerm = aggregateFieldValues(listSeries1Val, dfStatsPerm, strPerm, lineTypes, listFields, intPerm);
        }else if(grepl(':', strIndyVal)){
          listSetiesIndyVal = listSeries1Val
          listSetiesIndyVal[[strIndyVar]] = strIndyVal
          dfStatsPerm = aggregateFieldValues(listSetiesIndyVal, dfStatsPerm, strPerm, lineTypes, listFields, intPerm);
        }
        #can't calculate differences if  multiple values for one valid date/fcst_lead
        # don't check for SINGLE
        if (length(listDiffSeries) > 0 && !'SINGLE' %in% listDiffSeries[[1]]) {
          listFields = names(dfStatsPerm);
          if ("fcst_valid_beg" %in% listFields) {
            uniqueDates = nrow(unique(dfStatsPerm[c("fcst_valid_beg", "fcst_lead", "stat_name")]))
          } else if ("fcst_valid" %in% listFields) {
            uniqueDates = nrow(unique(dfStatsPerm[c("fcst_valid", "fcst_lead", "stat_name")]))
          } else if ("fcst_init_beg" %in% listFields) {
            uniqueDates = nrow(unique(dfStatsPerm[c("fcst_init_beg", "fcst_lead", "stat_name")]))
          } else {
            uniqueDates = nrow(unique(dfStatsPerm[c("fcst_init", "fcst_lead", "stat_name")]))
          }
          if (nrow(dfStatsPerm) != uniqueDates && !listStat %in% exemptedVars) {
            stop("Derived curve can't be calculated. Multiple values for one valid date/fcst_lead")
          }
          #sort data
          if ("fcst_valid_beg" %in% listFields) {
            dfStatsPerm = dfStatsPerm[order(dfStatsPerm$fcst_valid_beg, dfStatsPerm$fcst_lead, dfStatsPerm$stat_name),];
          }
          if ("fcst_valid" %in% listFields) {
            dfStatsPerm = dfStatsPerm[order(dfStatsPerm$fcst_valid, dfStatsPerm$fcst_lead, dfStatsPerm$stat_name),];
          }
          if ("fcst_init_beg" %in% listFields) {
            dfStatsPerm = dfStatsPerm[order(dfStatsPerm$fcst_init_beg, dfStatsPerm$fcst_lead, dfStatsPerm$stat_name),];
          }
          if ("fcst_init" %in% listFields) {
            dfStatsPerm = dfStatsPerm[order(dfStatsPerm$fcst_init, dfStatsPerm$fcst_lead, dfStatsPerm$stat_name),];
          }
        }

        strPerm = escapeStr(paste(listPerm, sep="_"));

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
          if(is.factor(listBoot[[strCountName]])){
            listBoot[[strCountName]]= droplevels(listBoot[[strCountName]]);
            #if(length(levels(listBoot[[strCountName]])) == 1){
            listBoot[[strCountName]] =  rep(levels(listBoot[[strCountName]])[1], intCountLength ) ;
            #}else{
            #  stop("Can't fill factor column");
            #}
          }else if(grepl('thresh_i$',strCountName) &&  strIndyVar == 'thresh_i'){
            listBoot[[strCountName]] = append( listBoot[[strCountName]], rep(listBoot[[strCountName]][1], intCountLength - length(listBoot[[strCountName]])) );
          }else{
            listBoot[[strCountName]] = append( listBoot[[strCountName]], rep(NA, intCountLength - length(listBoot[[strCountName]])) );
          }
        }
      }

      # bootstrap the series data
      dfBoot = data.frame(listBoot, check.names=FALSE);
      stBoot = Sys.time();
      if( !is.na(intRandomSeed) ){
        set.seed(intRandomSeed);
      }


      bootStat = try(boot(dfBoot, booter.iid, R = intNumReplicates , parallel = 'multicore', ncpus = 4));

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
          if( !is.null(listSeriesVar)){
            for(intSeriesVal in 1:length(listSeriesVar)){
              strSeriesVar = listSeriesVar[intSeriesVal];
              strSeriesVal = listPerm[intSeriesVal];
              listOutInd = listOutInd & (dfOut[[strSeriesVar]] == strSeriesVal);
            }
          }
          if(strIndyVar == ""){
            listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut$fcst_var == fcst_var);
          }else{
            listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut[[strIndyVar]] == strIndyVal) & (dfOut$fcst_var == fcst_var);
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
            dfOut[listOutInd,]$fcst_var = fcst_var;
            if( !is.null(strSeriesVar) ){
              a=strsplit(strSeriesVal, ":")[[1]];
              dfOut[listOutInd,]$nstats = nrow(dfStatsIndy[dfStatsIndy[[strSeriesVar]] %in% a  & dfStatsIndy$stat_name == strStat,  ]);
            }else{
              dfOut[listOutInd,]$nstats = nrow(dfStatsIndy[ dfStatsIndy$stat_name == strStat,  ]);
            }
            if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
              if( strCIType == "perc" && !is.null(bootCI[["percent"]]) ){
                dfOut[listOutInd,]$stat_btcl = bootCI[["percent"]][4];
                dfOut[listOutInd,]$stat_btcu = bootCI[["percent"]][5];

              }else if( strCIType == "norm" && !is.null(bootCI[["normal"]]) ){
                dfOut[listOutInd,]$stat_btcl = bootCI[["normal"]][2];
                dfOut[listOutInd,]$stat_btcu = bootCI[["normal"]][3];

              }else if( strCIType == "basic" && !is.null(bootCI[["basic"]]) ){
                dfOut[listOutInd,]$stat_btcl = bootCI[["basic"]][4];
                dfOut[listOutInd,]$stat_btcu = bootCI[["basic"]][5];

              }else if( strCIType == "bca" && !is.null(bootCI[["bca"]]) ){
                dfOut[listOutInd,]$stat_btcl = bootCI[["bca"]][4];
                dfOut[listOutInd,]$stat_btcu = bootCI[["bca"]][5];

              }else if( strCIType == "stud" && !is.null(bootCI[["student"]]) ){
                dfOut[listOutInd,]$stat_btcl = bootCI[["student"]][4];
                dfOut[listOutInd,]$stat_btcu = bootCI[["student"]][5];
              }else{
                dfOut[listOutInd,]$stat_btcl = NA;
                dfOut[listOutInd,]$stat_btcu = NA;
              }
            } else {
              dfOut[listOutInd,]$stat_btcl = NA;
              dfOut[listOutInd,]$stat_btcu = NA;
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
              dfOut[listOutInd1,]$stat_btcl = NA;
              dfOut[listOutInd1,]$stat_btcu = NA;
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
                dfOut[listOutInd1,]$stat_btcl = bootCI[["percent"]][4];
                dfOut[listOutInd1,]$stat_btcu = bootCI[["percent"]][5];

              }else if( strCIType == "norm" && !is.null(bootCI[["normal"]]) ){
                dfOut[listOutInd1,]$stat_btcl = bootCI[["normal"]][2];
                dfOut[listOutInd1,]$stat_btcu = bootCI[["normal"]][3];

              }else if( strCIType == "basic" && !is.null(bootCI[["basic"]]) ){
                dfOut[listOutInd1,]$stat_btcl = bootCI[["basic"]][4];
                dfOut[listOutInd1,]$stat_btcu = bootCI[["basic"]][5];

              }else if( strCIType == "bca" && !is.null(bootCI[["bca"]]) ){
                dfOut[listOutInd1,]$stat_btcl = bootCI[["bca"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["bca"]][5];

              }else if( strCIType == "stud" && !is.null(bootCI[["student"]]) ){
                dfOut[listOutInd1,]$stat_btcl = bootCI[["student"]][4];
                dfOut[listOutInd1,]$stat_bcu = bootCI[["student"]][5];
              }else{
                dfOut[listOutInd1,]$stat_btcl = NA;
                dfOut[listOutInd1,]$stat_btcu = NA;
              }
            } else {
              dfOut[listOutInd1,]$stat_btcl = NA;
              dfOut[listOutInd1,]$stat_btcu = NA;
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
if(all(is.na(listStat))){
  listStat = listStat1;
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
# parseLev() assumes that the input is a list of pressure level strings of one of the
#   following formats: Z0, P250 or P200-350 and attempts to parse the value.  If 
#   successful, an integer is returned representing the input pressure level.  For Z0, 
#   800 is returned.  For P250, 250 is returned.  For P200-350, 275 is returned - the 
#   midpoint of the range.  If the format is unrecognized, -1 is returned.
parseLev = function(listLev){
  listRet = c();
  for(strLev in listLev){
    intLev = numeric();
    if( grepl("P\\d+\\-\\d+", strLev, perl=TRUE) ){
      intLev = mean(as.numeric( unlist(strsplit(strLev, "[P\\-]", perl=TRUE))[2:3] ));
    } else if( grepl("P\\d+", strLev, perl=TRUE) ){
      intLev = as.numeric(sub("P", "", strLev));
    } else if( grepl("Z\\d", strLev, perl=TRUE) ){
      intLev = 800;
    } else {
      intLev = -1;
    }
    listRet[length(listRet)+1] = intLev;
  }
  return( listRet );
}

# seriesMinMax() assumes that the input series contains lists of numerics representing
#   the series values, low error bar values and high error bar values in 3-tuples.  
#   If so, seriesMinMax will return the minimum value of the low error bar values and
#   the maximum value of the high error bar values.  This method is called by plotting
#   functions to determine sensible axis bounds.  For exponential bounds, set log=TRUE.
#   If log=TRUE, then only the bounding integer exponents of 10 will be returned.
seriesMinMax = function(series, numModels, log=FALSE,listPlotDisp){
  dblMin = Inf; dblMax = -Inf;
  for( i in 1:numModels ) {
    if(listPlotDisp[i] == TRUE){
      listData = append(series[[3*(i-1)+2]], series[[3*(i-1)+3]]);
      listData = listData[!is.na(listData)];
      if( TRUE == log ){ listData = listData[listData > 0]; }
      if( 0 < length(listData) ){
        if( TRUE == log ){
          dblMinCur = floor( log10(min(listData)) );
          dblMaxCur = ceiling( log10(max(listData)) );
        } else {
          dblMinCur = min(listData);
          dblMaxCur = max(listData);
        }

        if( dblMinCur < dblMin ){ dblMin = dblMinCur; }
        if( dblMaxCur > dblMax ){ dblMax = dblMaxCur; }
      }
    }
  }
  if( Inf == dblMin  ){
    dblMin = 0; dblMax = 1;
  }
  if(dblMin == dblMax){
    if(dblMin > 0){
      dblMin = 0;
    }else if(dblMin < 0){
      dblMax = 0;
    }else{
      dblMin = 0; dblMax = 1;
    }
  }
  return( list(min=dblMin, max=dblMax) );
}

# numSeries() calculates the number of series based on the information in the listSeriesVal
#   and listDepVal input.  Each permutation of series values yields a series, plus an extra 
#   series for the difference associated with the last series.
numSeries = function(listSeriesVal, listDepVal, boolDiff = FALSE){
  intNumStats = 0;
  for( strDep1Name in names(listDep1Plot) ){
    for( strDep1Stat in listDep1Plot[[strDep1Name]] ){
      intNumStats = intNumStats + 1;
    }
  }

  intNumSeries = 1;
  listSeriesVar = names(listSeriesVal);
  for(intSeriesVar in 1:length(listSeriesVar)){
    strSeriesVar = listSeriesVar[intSeriesVar];
    intNumSeriesVal = length( listSeriesVal[[ strSeriesVar ]] );
    if( intSeriesVar == length(listSeriesVar) & boolDiff == TRUE ){
      intNumSeriesVal = intNumSeriesVal + 1;
    }
    intNumSeries = intNumSeries * intNumSeriesVal;
  }

  return( intNumSeries * intNumStats );
}

# eventEqualize() assumes that the input dfStats contains data indexed by fcst_valid_beg, series
#   values and the independent variable values.  It builds a new dataframe which contains the same 
#   data except for records that don't have corresponding fcst_valid_beg values in each other
#   series record with the same independent variable value.  For example, if the series_a has data
#   valid at 20111010_000000 for F12 and series_b does not, the series_a record is removed.
#
# INPUTS:
#         dfStats: data frame containing the records to equalize, including fcst_valid_beg, series
#                  values and independent variable values
#      strIndyVar: name of the independent variable
#     listIndyVal: independent variable values to equalize over
#   listSeriesVal: series variable names and values
#     listFixVars: name of the fixed variable
# listFixVarVals:  fixed variable values to equalize over
# boolEqualizeByIndep:  include or not the independant variable to EE
#       boolMulti: FALSE for normal event equalization, TRUE for equalization of mulitple events 
#                  at each combination of fcst_valid_beg, series values and independent value - 
#                  for example with MODE objects
#
eventEqualize = function(dfStats, strIndyVar, listIndyVal, listSeriesVal,listFixVars, listFixVarVals, boolEqualizeByIndep ,boolMulti){

  # convert the dates from strings to POSIXct, and create a unique member to use for equalization
  if( "fcst_valid_beg" %in% names(dfStats) ){
    dfStats$fcst_valid_beg = as.POSIXct(dfStats$fcst_valid_beg, format="%Y-%m-%d %H:%M:%S", tz="GMT");

    #always use fcst_valid_beg and fcst_lead for equalization
    equalize_by = paste(dfStats$fcst_valid_beg, dfStats$fcst_lead);
    dfStats$equalize = equalize_by;
  } else if( "fcst_valid" %in% names(dfStats) ) {
    dfStats$fcst_valid = as.POSIXct(dfStats$fcst_valid, format="%Y-%m-%d %H:%M:%S", tz="GMT");

    #always use fcst_valid_beg and fcst_lead for equalization
    equalize_by = paste(dfStats$fcst_valid, dfStats$fcst_lead);
    dfStats$equalize = equalize_by;

  } else {
    cat("  WARNING: eventEqualize() did not run due to lack of valid time field\n");
    return( dfStats );
  }

  listVarsForEE=list();

  #remove groups from the series vars
  for(strSeriesVar in names(listSeriesVal)){
    #do not include case members
    if(strSeriesVar != "fcst_valid_beg" && strSeriesVar != 'fcst_lead' && strSeriesVar != 'fcst_valid'){
      valSeries = listSeriesVal[[strSeriesVar]];
      valSeriesNew = c();
      for(strVar in valSeries){
        vectValPerms= strsplit(strVar, ",")[[1]];
        valSeriesNew = append(valSeriesNew, vectValPerms);
      }
      listVarsForEE[[strSeriesVar]] = valSeriesNew;
    }
  }

  #add independent variable if needed
  if(boolEqualizeByIndep && strIndyVar!= "" && strIndyVar != "fcst_valid_beg" && strIndyVar != 'fcst_lead' && strIndyVar != 'fcst_valid'){
    listVarsForEE[[strIndyVar]] = listIndyVal;
  }

  #add fixed variables if present
  if(length(listFixVars) > 0){
    for( index in 1:length(listFixVars) ){
      if(listFixVars[index] != "fcst_valid_beg" && listFixVars[index] != 'fcst_lead'  && listFixVars[index] != 'fcst_valid'){
        listVarsForEE[[ listFixVars[index] ]] = listFixVarVals[[index]];
      }
    }
  }


  # create a list of permutations representing the all variables for EE
  dfVarsForEEPerm =  permute(listVarsForEE );

  dfStatsEq = dfStats[array(FALSE,nrow(dfStats)),];

  listEqualize = vector();
  for(index in 1:nrow(dfVarsForEEPerm)){
    #for each permutation  - filter data and get unique cases
    dfComp = dfStats;
    for(strVarIndex in 1:ncol(dfVarsForEEPerm)){
      if(names(listVarsForEE)[strVarIndex] == 'fcst_init_beg'){
        dfComp = dfComp[as.character(dfComp[[names(listVarsForEE)[strVarIndex]]]) == dfVarsForEEPerm[index,strVarIndex],];
      }else{
        dfComp = dfComp[dfComp[[names(listVarsForEE)[strVarIndex]]] == dfVarsForEEPerm[index,strVarIndex],];
      }
    }

    # if the list contains repetetive values, show a warning
    if( FALSE == boolMulti &  length(dfComp$equalize) != length(unique(dfComp$equalize)) ){
      warning("\nWARNING: eventEqualize() detected non-unique events   for ", dfVarsForEEPerm[index,], " using [fcst_valid_beg,fcst_lead)]" );
    }

    if( 0 < sum(is.na(listEqualize) ) || length(listEqualize) == 0 ){
      #init the equalization list
      listEqualize = unique(dfComp$equalize);
    } else {
      # if there is an equalization list, equalize the current series data

      #find indexses of common cases that are not on the current cases list
      listInd = listEqualize %in% unique(dfComp$equalize);

      #identify discarded cases for this permutation
      listDiscard = listEqualize[ !listInd ];

      #add cases that are in current permutation but not in the common cases and add them to the discarded list
      listDiscard = append(listDiscard, dfComp$equalize[ !(dfComp$equalize %in% listEqualize) ]);

      # report the discarded records
      for(strDiscard in listDiscard){ cat("\n    WARNING: discarding series member with case", strDiscard, " for ", dfVarsForEEPerm[index,] ); }

      # update the equalization list by remowing records
      listEqualize = listEqualize[listInd];
    }

  }
  #remove data with discarded cases from the main frame
  dfStatsEq = dfStats[dfStats$equalize %in% listEqualize,];

  if( nrow(dfStatsEq) != nrow(dfStats) ){
    cat("\n    WARNING: event equalization removed ", (nrow(dfStats) - nrow(dfStatsEq)), " rows\n", sep="");
  }

  return( dfStatsEq );
}


# eventEqualizeAgainstValues() assumes that the input dfStats contains data indexed by fcst_valid, series
#   values and the independent variable values.  It builds a new dataframe which contains the same
#   data except for records that don't have corresponding fcst_valid_beg and fcst_lead values from eeStatsEqualize
#
# INPUTS:
#         dfStats: data frame containing the records to equalize, including fcst_valid_beg, series
#                  values and independent variable values
#      strIndyVar: name of the independent variable
# eeStatsEqualize: unique sases to equalize against
#
eventEqualizeAgainstValues = function(dfStats, strIndyVar,   eeStatsEqualize){

  if( "fcst_valid" %in% names(dfStats) ) {
    #always use fcst_valid and fcst_lead for equalization

    # convert the dates from strings to POSIXct, and create a unique member to use for equalization
    dfStats$fcst_valid = as.POSIXct(dfStats$fcst_valid, format="%Y-%m-%d %H:%M:%S", tz="GMT");
    equalize_by = paste(dfStats$fcst_valid, dfStats$fcst_lead);
    dfStats$equalize = equalize_by;
  } else {
    cat("  WARNING: eventEqualize() did not run due to lack of valid time field\n");
    return( array() );
  }


  # create an equalized set of data for the minimal list of dates
  dfStatsEq = dfStats[dfStats$equalize %in% eeStatsEqualize,];
  intEqRow = nrow(dfStatsEq);
  if( 0 == intEqRow ){
    cat("\n    WARNING: discarding all members");
  }

  if( intEqRow != nrow(dfStats) ){
    cat("\n    WARNING: event equalization removed ", (nrow(dfStats) - intEqRow), " rows", sep="");
  }
  dfStatsEq$equalize = NULL;
  return( dfStatsEq );
}




# permute() builds a permutation of all of the values in the input listVals, which are
#   indexed by the variable names in listVars.  The permutations are returned as a
#   matrix, with columns corresponding to the variable names in listVars.
permute = function(listVals){
  listVars = names(listVals);

  # if there is only one variable left, return the single permutation matrix
  if( 1 == length(listVars) ){
    listValsSub = listVals[[ listVars[1] ]];
    matPerm = matrix(nrow=length(listValsSub), ncol=1);
    matPerm[1:length(listValsSub),] = listValsSub;
    return( matPerm );
  }

  # permute over the lower-order variables
  intVarsLen = length(listVars);
  matPermSub = permute(listVals[2:intVarsLen]);
  intSubNrow = nrow(matPermSub);

  # build the permutation matrix by copying the lower-order matrix
  strVar = listVars[1];
  listValsSub = listVals[[strVar]];
  intValsLen = length(listValsSub);
  matPerm = matrix(nrow=(intSubNrow*intValsLen), ncol=intVarsLen);
  for( intVal in 1:length(listValsSub) ){
    intRowStart = (intVal-1)*intSubNrow + 1;
    intRowStop = intRowStart + intSubNrow - 1;
    matPerm[intRowStart:intRowStop,1] = rep(listValsSub[intVal], intSubNrow);
    matPerm[intRowStart:intRowStop,2:ncol(matPerm)] = matPermSub;
  }
  return( matPerm );
}



# calcPctROC() assumes that the specified dataframe contains PCT table data with columns
#   named thresh_i, oy_i and on_i.  The ROC points pody and pofd are calculated for each
#   threshold and returned in a dataframe.
calcPctROC = function(dfData){
  # create a data frame to hold the aggregated contingency table and ROC data
  listThresh = unique( sort( dfData$thresh_i ) );
  dfROC = data.frame(thresh = listThresh, n11 = NA, n10 = NA, n01 = NA, n00 = NA);

  # build the ROC contingency data table
  for( thresh in listThresh ){
    dfROC[dfROC$thresh == thresh,]$n11  = sum( dfData[dfData$thresh_i >  thresh,]$oy_i );
    dfROC[dfROC$thresh == thresh,]$n10  = sum( dfData[dfData$thresh_i >  thresh,]$on_i );
    dfROC[dfROC$thresh == thresh,]$n01  = sum( dfData[dfData$thresh_i <= thresh,]$oy_i );
    dfROC[dfROC$thresh == thresh,]$n00  = sum( dfData[dfData$thresh_i <= thresh,]$on_i );
  }

  # generate the pody and pofd scores from the contingency tables
  dfROC$pody = dfROC$n11 / (dfROC$n11 + dfROC$n01);
  dfROC$pofd = dfROC$n10 / (dfROC$n10 + dfROC$n00);

  return(dfROC);
}

# padLeft() adds spaces to the left of the input string, str, for formatting purposes.
padLeft = function(str, len=16){
  while( len > nchar(str) ){ str = paste(" ", str, sep=""); }
return(str);
}

# padRight() adds spaces to the right of the input string, str, for formatting purposes.
padRight = function(str, len=16){
  while( len > nchar(str) ){ str = paste(str, " ", sep=""); }
return(str);
}

# scaleStats() assumes that the input data frame contains statistics that require scaling and 
#   that the listDepScale input contains the scaling factor, indexed by strScaleName.  If the
#   scaling factor is present, the stat_value member and CI members of dfPlotStat will be
#   scaled accordingly.
scaleStats = function(dfPlotStat, listDepScale, strScaleName){
  if( !is.null(listDepScale[[strScaleName]]) ){
    dblScale = as.numeric(listDepScale[[strScaleName]]);
    dfPlotStat$stat_value = dfPlotStat$stat_value * dblScale;

    if( !is.null(dfPlotStat$stat_ncl) & !is.null(dfPlotStat$stat_ncu) ){
      dfPlotStat$stat_ncl = dfPlotStat$stat_ncl * dblScale;
      dfPlotStat$stat_ncu = dfPlotStat$stat_ncu * dblScale;
    }
    if( !is.null(dfPlotStat$stat_bcl) & !is.null(dfPlotStat$stat_bcu) ){
      dfPlotStat$stat_bcl = dfPlotStat$stat_bcl * dblScale;
      dfPlotStat$stat_bcu = dfPlotStat$stat_bcu * dblScale;
    }
  }
return( dfPlotStat );
}

# formatTimeSpan() assumes the input is a numeric representing the number of seconds.
#   A string in the format Dd HH:mm:ss.mmmm is returned.
formatTimeSpan = function(s){
  intDays = floor(s / (24 * 60 * 60));
  s = s - (intDays * 24 * 60 * 60);
  intHours = floor(s / (60 * 60));
  s = s - (intHours * 60 * 60);
  intMins = floor(s / 60);
  s = s - (intMins * 60);
  dblSec = s;

  strFormat = "";
  if( 0 < intDays ){ strFormat = paste(strFormat, intDays, "d ", sep=""); }
  if( 10 > intHours ){ strFormat = paste(strFormat, "0", sep=""); }
    strFormat = paste(strFormat, intHours, ":", sep="");
  if( 10 > intMins ){ strFormat = paste(strFormat, "0", sep=""); }
    strFormat = paste(strFormat, intMins, ":", sep="");
  if( 10 > dblSec ){ strFormat = paste(strFormat, "0", sep=""); }
    strFormat = paste(strFormat, format(dblSec, digits=6), sep="");

  return( strFormat );
}

# logLim() calculates the appropriate minimum and maximum exponents of 10 that bound
#   the input values, passed as an array.
logLim = function(x){
  listData = x[x > 0 & !is.na(x)];
return( list(min=floor( log10(min(listData)) ), max=ceiling( log10(max(listData)) )) );
}

# escapeStr() and unescapeStr() swap all non-alpha-numeric characters in the input string
#   with escape codes, with the direction of the swap differing depending on the function
escapeStr = function(s){
  if( "character" != class(s) ){ return(s); }
  s = gsub(" ",   "x20x", s); s = gsub("!",   "x21x", s); s = gsub("\"",  "x22x", s); s = gsub("#",   "x23x", s);
  s = gsub("\\$", "x24x", s); s = gsub("%",   "x25x", s); s = gsub("&",   "x26x", s); s = gsub("'",   "x27x", s);
  s = gsub("\\(", "x28x", s); s = gsub(")",   "x29x", s); s = gsub("\\*", "x2Ax", s); s = gsub("\\+", "x2Bx", s);
  s = gsub(",",   "x2Cx", s); s = gsub("-",   "x2Dx", s); s = gsub("\\.", "x2Ex", s); s = gsub("/",   "x2Fx", s);
  s = gsub(":",   "x3Ax", s); s = gsub(";",   "x3Bx", s); s = gsub("<",   "x3Cx", s); s = gsub("=",   "x3Dx", s);
  s = gsub(">",   "x3Ex", s); s = gsub("\\?", "x3Fx", s); s = gsub("@",   "x40x", s); s = gsub("\\[", "x5Bx", s);
  s = gsub("\\\\","x5Cx", s); s = gsub("]",   "x5Dx", s); s = gsub("\\^", "x5Ex", s); s = gsub("`",   "x60x", s);
  s = gsub("\\{", "x7Bx", s); s = gsub("\\|", "x7Cx", s); s = gsub("}",   "x7Dx", s); s = gsub("~",   "x7Ex", s);
  return(s);
}
unescapeStr = function(s){
  if( "character" != class(s) ){ return(s); }
  s = gsub("x20x", " ",  s); s = gsub("x21x", "!",  s); s = gsub("x22x", "\"", s); s = gsub("x23x", "#",  s);
  s = gsub("x24x", "$",  s); s = gsub("x25x", "%",  s); s = gsub("x26x", "&",  s); s = gsub("x27x", "'",  s);
  s = gsub("x28x", "(",  s); s = gsub("x29x", ")",  s); s = gsub("x2Ax", "*",  s); s = gsub("x2Bx", "+",  s);
  s = gsub("x2Cx", ",",  s); s = gsub("x2Dx", "-",  s); s = gsub("x2Ex", ".",  s); s = gsub("x2Fx", "/",  s);
  s = gsub("x3Ax", ":",  s); s = gsub("x3Bx", ";",  s); s = gsub("x3Cx", "<",  s); s = gsub("x3Dx", "=",  s);
  s = gsub("x3Ex", ">",  s); s = gsub("x3Fx", "?",  s); s = gsub("x40x", "@",  s); s = gsub("x5Bx", "[",  s);
  s = gsub("x5Cx", "\\", s); s = gsub("x5Dx", "]",  s); s = gsub("x5Ex", "^",  s); s = gsub("x60x", "`",  s);
  s = gsub("x7Bx", "{",  s); s = gsub("x7Cx", "|",  s); s = gsub("x7Dx", "}",  s); s = gsub("x7Ex", "~",  s);
  return(s);
}

# formatR() mimics the identically named java function which formats string that
#   are used as R dataframe/list column names.  The formatted version of the
#   specified unformatted string is returned.
formatR = function(s){
  if( "character" != class(s) ){ return(s); }
  s = gsub("\\(", "",    s);
  s = gsub("\\)", "",    s);
  s = gsub("\\.", "_d_", s);
  s = gsub("<=",  "le",  s);
  s = gsub(">=",  "ge",  s);
  s = gsub("=",   "eq",  s);
  s = gsub("<",   "lt",  s);
  s = gsub(">",   "gt",  s);
  return(s);
}

# calcBrierCI() computes a confidence interval for the Brier Score.  The specified
#   pct data should contain the columns n_i, oy_i and on_i.  See Tressa Fowler email
#   on 09/20/2011 Re: Brier CI calculation. 
#
# Reference:
#   Bradley, A.A, S.S. Schwartz, and T. Hashino, 2008:
#   Sampling Uncertainty and Confidence Intervals for the Brier Score
#     and Brier Skill Score.  Weather and Forecasting, 23, 992-1006.
calcBrierCI = function(dfPct, brier, alpha){

  # calculate PCT table attributes
  T = sum(dfPct$n_i);
  o_bar = sum(dfPct$oy_i) / T;

  # build the terms of the CI formula for each row
  dfPct$ee  = dfPct$oy_i / T;
  dfPct$ne  = dfPct$on_i / T;

  # sum the terms
  af1 = sum( dfPct$ee );
  sf2 = sum( dfPct$ee^2 );
  sf3 = sum( dfPct$ee^3 );
  af4 = sum( (dfPct$ee + dfPct$ne)^4 );

  # determine the quantile for the specified alpha and total
  t = qt(1 - (0.5*alpha), T - 1);

  # calculate the halfwidth CI using the summed terms
  var = ( (af4/T) + o_bar*(1 - (4*sf3) + (6*sf2) + (4*af1/T)) - brier^2 ) / T;
  halfwidth = t * sqrt(var);

  return ( halfwidth );
}

# buildAllStats() assumes that the input dfStats contains stat data for one or more series
#   for ploting.   listSeriesVal specifies field names and
#   values which correspond to the plot series, e.g. vx_mask and model.  dfStats must
#   contain the values in listSeriesVal.  Each permutation of values in listSeriesVar will
#   be plotted.    buildAllStats() will return one structures:
#   hhe series list contains series data to pass to the plot function, in the format of
#   median, upper std error and lower std error for each series.
#
#     INPUTS:
#           dfStats: contains independent, series and stat data for one or more plot series
#     listSeriesVal: series variable values, one series per permutation
#        listPlotCI: list of confidence interval types to use for each series
#          dblAlpha: alpha value to use when calculating confidence intervals
#    boolVarianceInflationFactor: include or not Variance Inflation Factor to Compute_STDerr_from_median
#       strPlotStat: use mean , median or sum value
#
#    RETURNS:
#            series: contains series data, in sets of three vectors: median, upper and
#                    lower std error values; intended to be passed to plot function
#
buildAllStats = function(dfStats, listSeriesVal, strDepStat,strDepName){

  # get the list of value permutations

  matPermVal = permute(listSeriesVal);
  listAllStats=list();
  listSeriesVar = names(listSeriesVal);
  for(intPermVal in 1:nrow(matPermVal)){
    listPermVal = matPermVal[intPermVal,];
    # build a dataset that pertains to the permutation values
    dfStatsVal = dfStats;
    listPermValMirror=c();
    for(intVar in length(listSeriesVar): 1){ # length of names in the list - number of columns in matPermVal
      # parse the perm value as an integer, if possible
      valPerm = listPermVal[intVar];
      listPermValMirror = append(listPermValMirror, valPerm);

      #check if the input frame already has group series  ( from calculation agg stats )
      if( is.element(valPerm, unique(dfStatsVal[[ listSeriesVar[intVar] ]]))  ){
        #group is in
        vectValPerms = valPerm;
      }else{
        vectValPerms= strsplit(valPerm, ",")[[1]];
        vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.numeric(x); }else{x=x} })
      }

      dfStatsVal = dfStatsVal[dfStatsVal[[ listSeriesVar[intVar] ]] %in% vectValPerms,];
    }

    # sort the dataset by init time and valid time
    listFields = names(dfStatsVal);
    if( "fcst_valid_beg" %in% listFields ){ dfStatsVal = dfStatsVal[order(dfStatsVal$fcst_valid_beg),]; }
    if( "fcst_valid"     %in% listFields ){ dfStatsVal = dfStatsVal[order(dfStatsVal$fcst_valid),];     }
    if( "fcst_init_beg"  %in% listFields ){ dfStatsVal = dfStatsVal[order(dfStatsVal$fcst_init_beg),];  }
    if( "fcst_init"      %in% listFields ){ dfStatsVal = dfStatsVal[order(dfStatsVal$fcst_init),];      }


    name=paste(listPermValMirror,collapse = " ");

    name = paste(name,strDepName,strDepStat)
    listAllStats[[name]] = dfStatsVal;
  }
  return( listAllStats );
}


# buildSeriesData() assumes that the input dfStats contains stat data for one or more series
#   for ploting.  The independent variables and values are specified in strIndyVar and
#   listIndyVal.  strIndyVar must be a valid field name and the listIndyVal values must
#   be present in dfStats.  strStatGroup contains the name of the dependent variable,
#   which indicates how to handle the stat data.  listSeriesVal specifies field names and
#   values which correspond to the plot series, e.g. vx_mask and model.  dfStats must
#   contain the values in listSeriesVal.  Each permutation of values in listSeriesVar will
#   be plotted.
# buildSeriesData() will return the list of data for each series. The nstats list
#   contains the number of stats  to compute the std error at each point.
#
#     INPUTS:
#           dfStats: contains independent, series and stat data for one or more plot series
#        strIndyVar: field name of independent variable, e.g. fcst_lev or fcst_lead
#       listIndyVal: independent variable values
#      strStatGroup: field name of dependent variable, e.g. TMP or BCRMSE
#     listSeriesVal: series variable values, one series per permutation
#      listPlotDisp: list of TRUE/FALSE values indicating series visibility
#        listPlotCI: list of confidence interval types to use for each series
#          dblAlpha: alpha value to use when calculating confidence intervals
#    boolVarianceInflationFactor: include or not Variance Inflation Factor to Compute_STDerr_from_median
#       strPlotStat: use mean ,  median or sum value
#
#    RETURNS:
#            series: contains series data
#            nstats: contains the number of stats  to compute std error at each point
#

buildSeriesData=function(dfStats, strIndyVar, listIndyVal, strStatGroup, listSeriesVal, listPlotDisp,
listPlotCI, dblAlpha=.05, boolVarianceInflationFactor=TRUE, strPlotStat="median"){

  intNumSeries = 1;
  listNStats = c();
  # storage for the levels and statistics of each plot series
  intNumIndy = length(listIndyVal);
  listSeries = list();

  listSeries[[1]] = rep(NA, intNumIndy);
  listSeries[[2]] = rep(NA, intNumIndy);
  listSeries[[3]] = rep(NA, intNumIndy);

  # get the list of value permutations
  matPermVal = permute(listSeriesVal);


  # build the amplification for the selected value of alpha for standard error
  dblZ = qnorm(1 - (dblAlpha/2));
  dblZVal = (dblZ + dblZ/sqrt(2)) / 2;

  # build a series for each stat curve
  intIndyIndex = 1;
  for(indy in listIndyVal){
    dfStatsIndy = dfStats[dfStats[[strIndyVar]] == indy,];
    intSeriesIndex = 1;
    intNStatsIndy = 0;


    # calculate the indexes for this series
    intMedIndex  = (3*(intSeriesIndex-1)) + 1;
    intLoIndex  = (3*(intSeriesIndex-1)) + 2;
    intUpIndex  = (3*(intSeriesIndex-1)) + 3;

    # build a dataset that pertains to the permutation values
    dfStatsVal = dfStatsIndy;
    # if there is no data for this case, skip to the next permutation
    if( 1 > nrow(dfStatsVal) ){
      rm(dfStatsVal);
    intSeriesIndex = intSeriesIndex + 1;
    intIndyIndex = intIndyIndex + 1;
    listNStats = append(listNStats, intNStatsIndy);
    next;
    }

    # add the median value to the current series point
    listStats = dfStatsVal$stat_value;
    if("mean" == strPlotStat){
      dblMed = mean(listStats, na.rm = TRUE);
    }else if("sum" == strPlotStat){
      dblMed = sum(listStats,na.rm = TRUE);
    } else {
      # use median if strPlotStat = 'median' or anything else since 'median' is the default
      dblMed = median(listStats,na.rm = TRUE);
    }
    if( TRUE == listPlotDisp[intSeriesIndex] ){
      if(length(listStats) == 1 && 'nstats' %in% names(dfStatsVal)){
        intNStatsIndy = intNStatsIndy + dfStatsVal$nstats[1];
      }else{
        intNStatsIndy = intNStatsIndy + length(listStats);
      }
    }

    #  apply the requested type of confidence interval to the current series point
    strPlotCI = listPlotCI[intSeriesIndex];
    dblLoCI = dblMed;
    dblUpCI = dblMed;



    if( "std" == strPlotCI & 0 < sum(listStats != 0, na.rm = TRUE) ){
      dblStdErr = 0;
      if("mean" == strPlotStat){
        seModel = try(Compute_STDerr_from_mean( listStats, method = 'ML' ));
      }else if("sum" == strPlotStat){
        seModel = try(Compute_STDerr_from_sum( listStats, method = 'ML' ));
      } else {
        if(TRUE == boolVarianceInflationFactor){
        seModel = try(Compute_STDerr_from_median_variance_inflation_factor( listStats, method = 'ML' ));
      } else {
        seModel = try(Compute_STDerr_from_median_no_variance_inflation_factor( listStats, method = 'ML' ));
      }
    }
    if( 1 < length(seModel) && 0 == seModel[2] ){ dblStdErr = dblZVal * seModel[1]; }
    dblLoCI = dblMed - dblStdErr;
    dblUpCI  = dblMed + dblStdErr;
    } else if( "norm" == strPlotCI ){
      if( !is.na(dfStatsVal$stat_ncl) & !is.na(dfStatsVal$stat_ncu) &-9999 != dfStatsVal$stat_ncl & -9999 != dfStatsVal$stat_ncu ){
        dblLoCI = dfStatsVal$stat_ncl;
        dblUpCI = dfStatsVal$stat_ncu;
      }
    } else if( "boot" == strPlotCI | "brier" == strPlotCI ){
      if( !is.na(dfStatsVal$stat_bcl) & !is.na(dfStatsVal$stat_bcu) & -9999 != dfStatsVal$stat_bcl & -9999 != dfStatsVal$stat_bcu ){
        dblLoCI = dfStatsVal$stat_bcl;
        dblUpCI = dfStatsVal$stat_bcu;
      }
    } else if( "q98" == strPlotCI & 0 < sum(listStats != 0, na.rm = TRUE) ){
      q = quantile(listStats, probs=c(0.01, 0.99));
      dblLoCI = q[["1%"]];
      dblUpCI = q[["99%"]];
    } else if( "q90" == strPlotCI & 0 < sum(listStats != 0, na.rm = TRUE) ){
      q = quantile(listStats, probs=c(0.05, 0.95));
      dblLoCI = q[["5%"]];
      dblUpCI = q[["95%"]];
    } else if( "q80" == strPlotCI & 0 < sum(listStats != 0, na.rm = TRUE) ){
      q = quantile(listStats, probs=c(0.10, 0.90));
      dblLoCI = q[["10%"]];
      dblUpCI = q[["90%"]];
    } else if( "q50" == strPlotCI & 0 < sum(listStats != 0, na.rm = TRUE) ){
      q = quantile(listStats, probs=c(0.25, 0.75));
      dblLoCI = q[["25%"]];
      dblUpCI = q[["75%"]];
    }

    listSeries[[intMedIndex]][intIndyIndex] = dblMed;
    listSeries[[intLoIndex]][intIndyIndex] = dblLoCI;
    listSeries[[intUpIndex]][intIndyIndex] = dblUpCI;

    intSeriesIndex = intSeriesIndex + 1;



    listNStats = append(listNStats, intNStatsIndy);

    intIndyIndex = intIndyIndex + 1;

  }  # end: for(indy in listIndyVal)

  # build the legend for the list of curves

  return( list(series=listSeries[], nstats=listNStats) );
}



getDerivedCurveName = function(diffSeriesVec){
  operation='DIFF';
  operationSign = '-';
  if(length(diffSeriesVec) == 3){
    operation=diffSeriesVec[3];
  }
  if(operation == 'RATIO'){
    operationSign = '/';
  }else if(operation == 'SS'){
    operationSign = 'and';
  }
  name = paste(operation,"(",diffSeriesVec[1],operationSign,diffSeriesVec[2],")" ,collapse="", sep = "");
  return(name);
}

calcDerivedCurveValue = function(val1, val2, derivedCurveName){
    if(  grepl('^DIFF', derivedCurveName) ){
    result = as.numeric(val1)  - as.numeric(val2);
  }else if( grepl('^RATIO', derivedCurveName) ){
    result = as.numeric(val1)  / as.numeric(val2);
  }else if( grepl('^SS', derivedCurveName) ){
    result = ( as.numeric(val1)  - as.numeric(val2) ) / as.numeric(val1);
  }
  result[result == Inf ] = NA;
  return( result );
}




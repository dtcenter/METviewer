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
  for( strDepName in names(listDepVal) ){
    for( strDepStat in listDepVal[[strDepName]] ){
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
  
  dateTimeVars = c("", "fcst_valid_beg", 'fcst_lead', 'fcst_valid', 'fcst_init', 'fcst_init_beg');
  
  # convert the dates from strings to POSIXct, and create a unique member to use for equalization
  
  if( "fcst_valid_beg" %in% names(dfStats)  ){
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
  
  #add independent variable if needed
  if(boolEqualizeByIndep && !(strIndyVar %in% dateTimeVars) ) {
    equalize_by = paste(dfStats$equalize,  dfStats[[strIndyVar]]);
    dfStats$equalize = equalize_by;
  }

  
  listVarsForEE=list();

  #remove groups from the series vars
  for(strSeriesVar in names(listSeriesVal)){
    #do not include case members
    if( !(strSeriesVar %in% dateTimeVars) ){
      valSeries = listSeriesVal[[strSeriesVar]];
      valSeriesNew = c();
      for(strVar in valSeries){
        if( grepl(':', strVar) ){
          vectValPerms= strsplit(strVar, ":")[[1]];
        }else{
          vectValPerms= strsplit(strVar, ",")[[1]];
        }
        valSeriesNew = append(valSeriesNew, vectValPerms);
      }
      listVarsForEE[[strSeriesVar]] = valSeriesNew;
    }
  }

  

  #add fixed variables if present
  if(length(listFixVars) > 0){
    for( index in 1:length(listFixVars) ){
      if( !(listFixVars[index] %in% dateTimeVars) ){
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
        if(is.na( strtoi(dfVarsForEEPerm[index,strVarIndex]))){
          #for strings
          dfComp = dfComp[dfComp[[names(listVarsForEE)[strVarIndex]]] == dfVarsForEEPerm[index,strVarIndex],];
        }else{
          #for integer string
          dfComp = dfComp[dfComp[[names(listVarsForEE)[strVarIndex]]] == strtoi(dfVarsForEEPerm[index,strVarIndex]) ,];
        }
      }
    }

    # if the list contains repetetive values, show a warning
    if( FALSE == boolMulti &  length(dfComp$equalize) != length(unique(dfComp$equalize)) ){
      warning("\nWARNING: eventEqualize() detected non-unique events   for ", dfVarsForEEPerm[index,], " using [fcst_valid_beg,fcst_lead)]" );
    }

    #if(  0 < sum(is.na(listEqualize) ) || length(listEqualize) == 0 ){
    if(  index == 1 ){
      #init the equalization list
      listEqualize = unique(dfComp$equalize);
    } else {
      # if there is an equalization list, equalize the current series data

      #find indexses of common cases that are not on the current cases list
      listInd = listEqualize %in% unique(dfComp$equalize);

      #identify discarded cases for this permutation
      listDiscard = listEqualize[ !listInd ];

      #add cases that are in current permutation but not in the common cases and add them to the discarded list
      listDiscard = append(listDiscard, unique(dfComp$equalize[ !(dfComp$equalize %in% listEqualize) ]));

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
  #convert date back to string
  if( "fcst_valid_beg" %in% names(dfStatsEq) ){
    dfStatsEq$fcst_valid_beg =  format(dfStatsEq$fcst_valid_beg, format="%Y-%m-%d %H:%M:%S", tz="GMT");
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

  #convert date back to string
  if( "fcst_valid_beg" %in% names(dfStatsEq) ){
    dfStatsEq$fcst_valid_beg =  format(dfStatsEq$fcst_valid_beg, format="%Y-%m-%d %H:%M:%S", tz="GMT");
  }
  return( dfStatsEq );
}




# permute() builds a permutation of all of the values in the input listVals, which are
#   indexed by the variable names in listVars.  The permutations are returned as a
#   matrix, with columns corresponding to the variable names in listVars.
permute = function(listVals){
  listVars = names(listVals);
  if(is.null(listVars)){
    return (matrix());
  }

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
    dfROC[dfROC$thresh == thresh,]$n11  = sum( as.numeric (dfData[dfData$thresh_i >  thresh,]$oy_i ) );
    dfROC[dfROC$thresh == thresh,]$n10  = sum( as.numeric (dfData[dfData$thresh_i >  thresh,]$on_i ) );
    dfROC[dfROC$thresh == thresh,]$n01  = sum( as.numeric (dfData[dfData$thresh_i <= thresh,]$oy_i ) );
    dfROC[dfROC$thresh == thresh,]$n00  = sum( as.numeric (dfData[dfData$thresh_i <= thresh,]$on_i ) );
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
    strFormat = paste(strFormat, format(dblSec, digits=3), sep="");

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
        vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.integer(x); }else{x=x} })
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
       if( !is.null(dfStatsVal$stat_ncl) & !is.null(dfStatsVal$stat_ncu)){
         if(!is.na(dfStatsVal$stat_ncl) & !is.na(dfStatsVal$stat_ncu) &-9999 != dfStatsVal$stat_ncl & -9999 != dfStatsVal$stat_ncu ){
          dblLoCI = dfStatsVal$stat_ncl;
          dblUpCI = dfStatsVal$stat_ncu;
          }
      }
    } else if( "boot" == strPlotCI | "brier" == strPlotCI ){
      if( !is.null(dfStatsVal$stat_bcl) & !is.null(dfStatsVal$stat_bcu)){
        if( !is.na(dfStatsVal$stat_bcl) & !is.na(dfStatsVal$stat_bcu) & -9999 != dfStatsVal$stat_bcl & -9999 != dfStatsVal$stat_bcu ){
          dblLoCI = dfStatsVal$stat_bcl;
         dblUpCI = dfStatsVal$stat_bcu;
        }
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
  }else if( grepl('^SINGLE', derivedCurveName) ){
    result = as.numeric(val1);
  }
  result[result == Inf ] = NA;
  return( result );
}


atan2d <- function (y, x) {
  #returns a range [-180, 180]
  if (nargs() != 2) {
    stop("There should only be two arguments.")
  }
  stopifnot(is.numeric((c(y, x)))) # from pracma trisolve
  y <- 180 / pi * atan2 (y, x)
  return(y)
}

sameElements <- function (a,b){
  l <- Map(table,list(a, b)) # Compute frequencies - returns ordered table
  Reduce(identical,l) # Check if frequencies are the same for all input vectors
}

perfectScoreAdjustment <- function(meanStats1, meanStats2, statistic, pval){
  na_perf_score_stats <- c('BASER','FMEAN','FBAR','FSTDEV', 'OBAR', 'OSTDEV', 'FRANK_TIES', 'ORANK_TIES',
    'FBAR',  'FSTDEV', 'OBAR', 'OSTDEV', 'RANKS', 'FRANK_TIES', 'ORANK_TIES','VL1L2_FBAR', 'VL1L2_OBAR',
    'VL1L2_FSTDEV','VL1L2_OSTDEV','VL1L2_FOSTDEV', 'PSTD_BASER','PSTD_RESOLUTION', 'PSTD_UNCERTAINTY',
                           'PSTD_ROC_AUC',  'NBR_UFSS', 'NBR_F_RATE', 'NBR_O_RATE','NBR_BASER','NBR_FMEAN',
                           'RPS_RES', 'RPS_UNC'
  );

  zero_perf_score_stats <- c('POFD','FAR','ESTDEV','MAE', 'MSE', 'BCMSE', 'RMSE', 'E10', 'E25', 'E50', 'E75',
    'E90', 'EIQR', 'MAD', 'ME2', 'ME', 'ESTDEV', 'ODDS','LODDS','VL1L2_MSE','VL1L2_RMSE',
'VL1L2_RMSVE','PSTD_BRIER', 'PSTD_RELIABILITY', 'NBR_FBS' ,'VL1L2_SPEED_ERR', 'NBR_POFD', 'NBR_FAR','NBR_ODDS',
  'RPS', 'RPS_REL', 'BCRMSE');

  one_perf_score_stats <- c('ACC', 'FBIAS', 'PODY','PODN', 'CSI', 'GSS', 'HK', 'HSS', 'ORSS', 'EDS', 'SEDS',
    'EDI', 'SEDI', 'BAGSS','PR_CORR', 'SP_CORR', 'KT_CORR', 'MBIAS', 'ANOM_CORR','ANOM_CORR_RAW', 'VL1L2_BIAS','VL1L2_CORR',
    'PSTD_BSS', 'PSTD_BSS_SMPL', 'NBR_FSS', 'NBR_AFSS',  'VAL1L2_ANOM_CORR', 'NBR_ACC','NBR_FBIAS', 'NBR_PODY',
  'NBR_PODN', 'NBR_CSI', 'NBR_GSS','NBR_HK','NBR_HSS', 'RPSS', 'RPSS_SMPL');

  if( statistic %in% na_perf_score_stats ){
    result = NA;
  }else if( statistic %in% zero_perf_score_stats && abs(meanStats1 - 0) > abs(meanStats2 - 0) ){
    result = pval * -1;
  }else if( statistic %in% one_perf_score_stats && abs(meanStats1 - 1) > abs(meanStats2 - 1) ){
    result = pval * -1;
  }else{
    result = pval;
  }
  return(result);
}


value <- function (obs, pred = NULL, baseline = NULL, cl = seq(0.05, 0.95,0.05), plot = TRUE,
                    all = FALSE, thresholds = seq(0.05, 0.95, 0.05), ylim = c(-0.05, 1), xlim = c(0, 1), ...)
{
    if (!is.null(pred)) {
        id <- is.finite(obs) & is.finite(pred)
        obs <- obs[id]
        pred <- pred[id]
    }
    else {
        obs <- obs[is.finite(obs)]
    }
    if (is.null(pred) & length(obs) == 4) {
        #print(" Assume data entered as c(n11, n01, n10, n00) Obs*Forecast")
        n <- sum(obs)
        a <- obs[1]
        b <- obs[2]
        c <- obs[3]
        d <- obs[4]
        F <- b/(b + d)
        H <- a/(a + c)
        if (is.null(baseline)) {
            s <- (a + c)/n
            baseline.tf <- FALSE
        }
        else {
            s <- baseline
            baseline.tf <- TRUE
        }
        cl <- sort(c(cl, s))
        V1 <- (1 - F) - s/(1 - s) * (1 - cl)/cl * (1 - H)
        V2 <- H - (1 - s)/s * cl/(1 - cl) * F
        V <- numeric(length(cl))
        V[cl < s] <- V1[cl < s]
        V[cl >= s] <- V2[cl >= s]
        V <- matrix(V, ncol = 1)
        Vmax <- H - F
        positive <- c(c/(c + d), a/(a + b))
        type <- "binary"
    }
    else {
        if (prod(unique(pred) %in% c(0, 1))) {
            if (is.null(baseline)) {
                s <- mean(obs)
                baseline.tf <- FALSE
            }
            else {
                s <- baseline
                baseline.tf <- TRUE
            }
            cl <- sort(c(cl, s))
            F <- numeric()
            H <- numeric()
            Vmax <- numeric()
            V <- matrix(nrow = length(cl), ncol = 1)
            A <- table(data.frame(obs = obs, pred = pred))
            a <- A[2, 2]
            b <- A[1, 2]
            c <- A[2, 1]
            d <- A[1, 1]
            n <- a + b + c + d
            F[1] <- b/(b + d)
            H[1] <- a/(a + c)
            V1 <- (1 - F[1]) - s/(1 - s) * (1 - cl)/cl * (1 -
                H[1])
            V2 <- H[1] - (1 - s)/s * cl/(1 - cl) * F[1]
            VV <- numeric(length(cl))
            VV[cl < s] <- V1[cl < s]
            VV[cl >= s] <- V2[cl >= s]
            V[, 1] <- VV
            Vmax[1] <- H[1] - F[1]
            positive <- c(c/(c + d), a/(a + b))
        }
        else {
            if (max(pred) > 1 | min(pred) < 0) {
                stop("Predictions outside [0,1] range.  \n I am a bit confused. \n")
            }
            if (is.null(baseline)) {
                s <- mean(obs)
                baseline.tf <- FALSE
            }
            else {
                s <- baseline
                baseline.tf <- TRUE
            }
            cl <- sort(c(cl, s))
            NCOL <- length(thresholds)
            PRED <- matrix(NA, nrow = length(pred), ncol = NCOL)
            for (i in 1:NCOL) PRED[, i] <- pred > thresholds[i]
            F <- numeric()
            H <- numeric()
            Vmax <- numeric()
            V <- matrix(nrow = length(cl), ncol = ncol(PRED))
            n <- length(pred)
            for (i in 1:ncol(PRED)) {
                A <- table(data.frame(obs, PRED[, i]))
                a <- try(A[2, 2], silent = TRUE)
                b <- try(A[1, 2], silent = TRUE)
                c <- try(A[2, 1], silent = TRUE)
                d <- try(A[1, 1], silent = TRUE)
                if (class(a) == "try-error")
                  a <- NA
                if (class(b) == "try-error")
                  b <- NA
                if (class(c) == "try-error")
                  c <- NA
                if (class(d) == "try-error")
                  d <- NA
                F[i] <- b/(b + d)
                H[i] <- a/(a + c)
                V1 <- (1 - F[i]) - s/(1 - s) * (1 - cl)/cl *
                  (1 - H[i])
                V2 <- H[i] - (1 - s)/s * cl/(1 - cl) * F[i]
                VV <- numeric(length(cl))
                VV[cl < s] <- V1[cl < s]
                VV[cl >= s] <- V2[cl >= s]
                V[, i] <- VV
                Vmax[i] <- H[i] - F[i]
            }
        }
    }
    if (plot) {
        if (!all)
            V <- apply(V, 1, max)
        matplot(cl, V, type = "l", ylim = ylim, xlim = xlim,
            ...)
        if (all)
            lines(cl, apply(V, 1, max), lwd = 2)
        abline(h = 0)
        abline(v = s, lty = 2, lwd = 0.4)
    }
    (aa <- list(vmax = Vmax, V = V, F = F, H = H, cl = cl, s = s,
        n = n))
    invisible(aa)
}

filled.contour3 <-
  function (x = seq(0, 1, length.out = nrow(z)),
            y = seq(0, 1, length.out = ncol(z)), z, xlim = range(x, finite = TRUE),
            ylim = range(y, finite = TRUE), zlim = range(z, finite = TRUE),
            levels = pretty(zlim, nlevels), nlevels = 20, color.palette = cm.colors,
            col = color.palette(length(levels) - 1), plot.title, plot.axes,
            key.title, key.axes, asp = NA, xaxs = "i", yaxs = "i", las = 1,
            axes = TRUE, frame.plot = axes,mar, ...)
  {
    # modification by Ian Taylor of the filled.contour function
    # to remove the key and facilitate overplotting with contour()
    # further modified by Carey McGilliard and Bridget Ferris
    # to allow multiple plots on one page

    if (missing(z)) {
      if (!missing(x)) {
        if (is.list(x)) {
          z <- x$z
          y <- x$y
          x <- x$x
        }
        else {
          z <- x
          x <- seq.int(0, 1, length.out = nrow(z))
        }
      }
      else stop("no 'z' matrix specified")
    }
    else if (is.list(x)) {
      y <- x$y
      x <- x$x
    }
    
    #!!!! COMMENTED THIS because some x or values can be strings or in reverse order
    if (any(diff(x) <= 0) || any(diff(y) <= 0))
      stop("increasing 'x' and 'y' values expected")
    
    
    
    
    # mar.orig <- (par.orig <- par(c("mar", "las", "mfrow")))$mar
    # on.exit(par(par.orig))
    # w <- (3 + mar.orig[2]) * par("csi") * 2.54
    # par(las = las)
    # mar <- mar.orig
    plot.new()
    # par(mar=mar)
    plot.window(xlim, ylim, "", xaxs = xaxs, yaxs = yaxs, asp = asp)
    if(!is.matrix(z))
      stop("no proper 'z' matrix specified")
    if(nrow(z) <= 1)
      stop("X axis should have more than one value")
    if(ncol(z) <= 1)
      stop("Y axis should have more than one value")

    if (!is.double(z))
      storage.mode(z) <- "double"
    .filled.contour(as.double(x), as.double(y), z, as.double(levels),
                            col = col)
    if (missing(plot.axes)) {
      if (axes) {
        title(main = "", xlab = "", ylab = "")
        Axis(x, side = 1)
        Axis(y, side = 2)
      }
    }
    else plot.axes
    if (frame.plot)
      box()
    if (missing(plot.title))
      title(...)
    else plot.title
    invisible()
  }


filled.legend <-
   function (x = seq(0, 1, length.out = nrow(z)), y = seq(0, 1,
                                                          length.out = ncol(z)), z, xlim = range(x, finite = TRUE),
             ylim = range(y, finite = TRUE), zlim = range(z, finite = TRUE),
             levels = pretty(zlim, nlevels), nlevels = 20, color.palette = cm.colors,
             col = color.palette(length(levels) - 1), plot.title, plot.axes,
             key.title, key.axes, asp = NA, xaxs = "i", yaxs = "i", las = 1,
             axes = TRUE, frame.plot = axes, ...)
   {
     # modification of filled.contour by Carey McGilliard and Bridget Ferris
     # designed to just plot the legend
     if (missing(z)) {
       if (!missing(x)) {
         if (is.list(x)) {
           z <- x$z
           y <- x$y
           x <- x$x
         }
         else {
           z <- x
           x <- seq.int(0, 1, length.out = nrow(z))
         }
       }
       else stop("no 'z' matrix specified")
     }
     else if (is.list(x)) {
       y <- x$y
       x <- x$x
     }
     if (any(diff(x) <= 0) || any(diff(y) <= 0))
       stop("increasing 'x' and 'y' values expected")
     #  mar.orig <- (par.orig <- par(c("mar", "las", "mfrow")))$mar
     #  on.exit(par(par.orig))
     #  w <- (3 + mar.orig[2L]) * par("csi") * 2.54
     #layout(matrix(c(2, 1), ncol = 2L), widths = c(1, lcm(w)))
     #  par(las = las)
     #  mar <- mar.orig
     #  mar[4L] <- mar[2L]
     #  mar[2L] <- 1
     #  par(mar = mar)
     # plot.new()
     plot.window(xlim = c(0, 1), ylim = range(levels), xaxs = "i",
                 yaxs = "i")
     rect(0, levels[-length(levels)], 1, levels[-1L], col = col)
     if (missing(key.axes)) {
       if (axes)
         axis(4)
     }
     else key.axes
     box()
   }

# returns string w/o leading or trailing whitespace
trim <- function (x) gsub("^\\s+|\\s+$", "", x)

green.red<-colorRampPalette(c("#E6FFE2","#B3FAAD", "#74F578", "#30D244", "#00A01E", "#F6A1A2", "#E26667", "#C93F41", "#A42526"),
                            interpolate="linear")

blue.white.brown<-colorRampPalette(c("#1962CF", "#3E94F2","#B4F0F9", "#00A01E", "#4AF058",
                                         "#C7FFC0", "#FFFFFF", "#FFE97F", "#FF3A20", "#A50C0F", "#E1BFB5",
                                         "#A0786F", "#643D34"), interpolate="linear")


# Wald-Wolfowitz Runs Test
# Performs the Wald-Wolfowitz runs test of randomness for continuous data.
#
#x	- a numeric vector containing the observations
#alternative	- a character string with the alternative hypothesis. Must be one of "two.sided" (default), "left.sided" or "right.sided". You can specify just the initial letter.
#threshold	- the cut-point to transform the data into a dichotomous vector
#pvalue	- a character string specifying the method used to compute the p-value. Must be one of normal (default), or exact.
#plot	- a logic value to select whether a plot should be created. If 'TRUE', then the graph will be plotted.
runs.test = function (x, alternative = "two.sided", threshold = median(x), 
          pvalue = "normal", plot = FALSE) 
{
  dname <- deparse(substitute(x))
  if (alternative == "t") {
    alternative <- "two.sided"
  }
  if (alternative == "l") {
    alternative <- "left.sided"
  }
  if (alternative == "r") {
    alternative <- "right.sided"
  }
  if (alternative != "two.sided" & alternative != "left.sided" & 
      alternative != "right.sided") {
    stop("must give a valid alternative")
  }
  x <- na.omit(x)
  stopifnot(is.numeric(x))
  x <- x[x != threshold]
  s <- sign(x - threshold)
  n1 <- length(s[s > 0])
  n2 <- length(s[s < 0])
  runs <- rle(s)
  r1 <- length(runs$lengths[runs$values == 1])
  r2 <- length(runs$lengths[runs$values == -1])
  n <- n1 + n2
  mu <- 1 + 2 * n1 * n2/(n1 + n2)
  vr <- 2 * n1 * n2 * (2 * n1 * n2 - n1 - n2)/(n^2 * (n - 1))
  rr <- r1 + r2
  if (plot) {
    plot((1:n)[s > 0], x[s > 0], xlim = c(1, n), ylim = c(min(x), 
                                                          max(x)), xlab = "", ylab = dname)
    points((1:n)[s < 0], x[s < 0], col = "red")
    abline(h = threshold, col = gray(0.4))
    for (i in 1:(n - 1)) {
      if (s[i] * s[i + 1] < 0) {
        abline(v = i + 0.5, lty = 2)
      }
    }
  }
  pv <- 0
  if (pvalue == "exact") {
    if (alternative == "two.sided") {
      pv1 <- sum(druns(1:rr, n1, n2))
      pv2 <- sum(druns(rr:(n1 + n2), n1, n2))
      pv <- 2 * min(pv1, pv2)
    }
    if (alternative == "left.sided") {
      pv <- sum(druns(2:rr, n1, n2))
    }
    if (alternative == "right.sided") {
      pv <- sum(druns(rr:(n1 + n2), n1, n2))
    }
  }
  if (pvalue == "normal") {
    pv0 <- pnorm((rr - mu)/sqrt(vr))
    if (alternative == "two.sided") {
      pv <- 2 * min(pv0, 1 - pv0)
    }
    if (alternative == "left.sided") {
      pv <- pv0
    }
    if (alternative == "right.sided") {
      pv <- 1 - pv0
    }
  }
  if (alternative == "two.sided") {
    alternative <- "nonrandomness"
  }
  if (alternative == "left.sided") {
    alternative <- "trend"
  }
  if (alternative == "right.sided") {
    alternative <- "first-order negative autocorrelation"
  }
  rval <- list(statistic = c(statistic = (rr - mu)/sqrt(vr)), 
               p.value = pv, runs = rr, mu = mu, var = vr, method = "Runs Test", 
               data.name = dname, parameter = c(runs = rr, n1 = n1, 
                                                n2 = n2, n = n), alternative = alternative)
  class(rval) <- "htest"
  return(rval)
}

custom_sum = function(input, na.rm = FALSE){
  # if all elements are NA - return 0
  if( sum(is.na(input)) == length(input) ){
    return (NA)
  }
  return( sum(input, na.rm=na.rm) )
}

calcSeriesSums = function( d , strPerm, lineTypes, intPerm=1,  T=c(), oy_total=c(), o_bar=c()){
  if( lineTypes$boolCtc || lineTypes$boolNbrCtc){
    dfSeriescustom_sums = data.frame(
    total	= custom_sum( as.numeric(d[[ paste(strPerm, "total", sep="_") ]]), na.rm=TRUE ),
    fy_oy	= custom_sum( as.numeric(d[[ paste(strPerm, "fy_oy", sep="_") ]]), na.rm=TRUE ),
    fy_on	= custom_sum( as.numeric(d[[ paste(strPerm, "fy_on", sep="_") ]]), na.rm=TRUE ),
    fn_oy	= custom_sum( as.numeric(d[[ paste(strPerm, "fn_oy", sep="_") ]]), na.rm=TRUE ),
    fn_on	= custom_sum( as.numeric(d[[ paste(strPerm, "fn_on", sep="_") ]]), na.rm=TRUE )
    );
  }  else if ( lineTypes$boolSl1l2 ){ # perform the aggregation of the sampled SL1L2 lines
    listTotal	= d[[ paste(strPerm, "total", sep="_") ]];
    total		= custom_sum(listTotal, na.rm=TRUE);
    dfSeriescustom_sums = data.frame(
    total	= total,
    fbar	= custom_sum( as.numeric( d[[ paste(strPerm, "fbar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total,
    obar	= custom_sum( as.numeric( d[[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    fobar	= custom_sum( as.numeric( d[[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    ffbar	= custom_sum( as.numeric( d[[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    oobar	= custom_sum( as.numeric( d[[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    mae   = custom_sum( as.numeric( d[[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
    );
  }  else if ( lineTypes$boolGrad ){ # perform the aggregation of the sampled grad lines
    listTotal	= d[[ paste(strPerm, "total", sep="_") ]];
    total		= custom_sum(listTotal, na.rm=TRUE);

    dfSeriescustom_sums = data.frame(
    total	= total,
    fgbar	= custom_sum( as.numeric( d[[ paste(strPerm, "fgbar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total,
    ogbar	= custom_sum( as.numeric( d[[ paste(strPerm, "ogbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    mgbar	= custom_sum( as.numeric( d[[ paste(strPerm, "mgbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    egbar	= custom_sum( as.numeric( d[[ paste(strPerm, "egbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total
    );
  }  else if ( lineTypes$boolVl1l2 ){ # perform the aggregation of the sampled VL1L2 lines
    listTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    dfSeriescustom_sums = data.frame(
    total  = total,
    ufbar  = custom_sum( as.numeric( d[[ paste(strPerm, "ufbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    vfbar  = custom_sum( as.numeric( d[[ paste(strPerm, "vfbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uobar  = custom_sum( as.numeric( d[[ paste(strPerm, "uobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    vobar  = custom_sum( as.numeric( d[[ paste(strPerm, "vobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uvfobar  = custom_sum( as.numeric( d[[ paste(strPerm, "uvfobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uvffbar   = custom_sum( as.numeric( d[[ paste(strPerm, "uvffbar", sep="_") ]] )   * listTotal, na.rm=TRUE ) / total,
    uvoobar   = custom_sum( as.numeric( d[[ paste(strPerm, "uvoobar", sep="_") ]] )  *listTotal, na.rm=TRUE ) / total,
    f_speed_bar   = custom_sum( as.numeric( d[[ paste(strPerm, "f_speed_bar", sep="_") ]] )*listTotal, na.rm=TRUE ) / total,
    o_speed_bar   = custom_sum( as.numeric( d[[ paste(strPerm, "o_speed_bar", sep="_") ]] )*listTotal, na.rm=TRUE ) / total
    );
    }  else if ( lineTypes$boolVal1l2 ){ # perform the aggregation of the sampled VAL1L2 lines
    listTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    dfSeriescustom_sums = data.frame(
    total  = total,
    ufabar  = custom_sum( as.numeric( d[[ paste(strPerm, "ufabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    vfabar  = custom_sum( as.numeric( d[[ paste(strPerm, "vfabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uoabar  = custom_sum( as.numeric( d[[ paste(strPerm, "uoabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    voabar  = custom_sum( as.numeric( d[[ paste(strPerm, "voabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uvfoabar  = custom_sum( as.numeric( d[[ paste(strPerm, "uvfoabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    uvffabar   = custom_sum( as.numeric( d[[ paste(strPerm, "uvffabar", sep="_") ]] )   * listTotal, na.rm=TRUE ) / total,
    uvooabar   = custom_sum( as.numeric( d[[ paste(strPerm, "uvooabar", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
    );
  }  else if ( lineTypes$boolSal1l2 ){ # perform the aggregation of the sampled SAL1L2 lines
    listTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total		= custom_sum(listTotal, na.rm=TRUE);
    dfSeriescustom_sums = data.frame(
    total	= total,
    fbar	= custom_sum( as.numeric( d[[ paste(strPerm, "fabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    obar	= custom_sum( as.numeric( d[[ paste(strPerm, "oabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    fobar	= custom_sum( as.numeric( d[[ paste(strPerm, "foabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    ffbar	= custom_sum( as.numeric( d[[ paste(strPerm, "ffabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    oobar	= custom_sum( as.numeric( d[[ paste(strPerm, "ooabar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    mae   = custom_sum( as.numeric( d[[ paste(strPerm, "mae", sep="_") ]] )  * listTotal, na.rm=TRUE ) / total
    );
  }  else if ( lineTypes$boolSsvar ){ # perform the aggregation of the sampled SSVAR lines
    listTotal  = d[[ paste(strPerm, "bin_n", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    listTotalTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    totalTotal    = custom_sum(listTotalTotal, na.rm=TRUE);
    dfSeriescustom_sums = data.frame(
    total  = total,
    total_tital = totalTotal,
    fbar	= custom_sum( as.numeric( d[[ paste(strPerm, "fbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    obar	= custom_sum( as.numeric( d[[ paste(strPerm, "obar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    fobar	= custom_sum( as.numeric( d[[ paste(strPerm, "fobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    ffbar	= custom_sum( as.numeric( d[[ paste(strPerm, "ffbar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    oobar	= custom_sum( as.numeric( d[[ paste(strPerm, "oobar", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    varmean	= custom_sum( as.numeric( d[[ paste(strPerm, "var_mean", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
    binn	=  total
    );
  }  else if ( lineTypes$boolRps ){ # perform the aggregation of the sampled RPS lines
    listTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    d_rps_climo = d[[ paste(strPerm, "rps", sep="_") ]] / ( 1 - d[[ paste(strPerm, "rpss", sep="_") ]])
    dfSeriescustom_sums = data.frame(
      total  = total,
      rps	= custom_sum( as.numeric( d[[ paste(strPerm, "rps", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
      rps_comp	= custom_sum( as.numeric( d[[ paste(strPerm, "rps_comp", sep="_") ]] ) * listTotal, na.rm=TRUE ) / total,
      rps_climo	= custom_sum( d_rps_climo * listTotal, na.rm=TRUE ) / total
    );

  }  else if ( lineTypes$boolEcnt ){ # perform the aggregation of the sampled ECNT lines
    listTotal  = d[[ paste(strPerm, "total", sep="_") ]];
    total    = custom_sum(listTotal, na.rm=TRUE);
    mse = as.numeric( d[[ paste(strPerm, "rmse", sep="_") ]] ) * as.numeric( d[[ paste(strPerm, "rmse", sep="_") ]] )
    mse_oerr = as.numeric( d[[ paste(strPerm, "rmse_oerr", sep="_") ]] ) * as.numeric( d[[ paste(strPerm, "rmse_oerr", sep="_") ]] )
    crps_climo = as.numeric( d[[ paste(strPerm, "crps", sep="_") ]] ) / (1.0 - as.numeric( d[[ paste(strPerm, "crpss", sep="_") ]] ))
    
    variance= as.numeric( d[[ paste(strPerm, "spread", sep="_") ]] ) * as.numeric( d[[ paste(strPerm, "spread", sep="_") ]] )
    variance_oerr= as.numeric( d[[ paste(strPerm, "spread_oerr", sep="_") ]] ) * as.numeric( d[[ paste(strPerm, "spread_oerr", sep="_") ]] )
    variance_plus_oerr= as.numeric( d[[ paste(strPerm, "spread_plus_oerr", sep="_") ]] ) * as.numeric( d[[ paste(strPerm, "spread_plus_oerr", sep="_") ]] )


    dfSeriescustom_sums = data.frame(
    total  = total,
    mse	= custom_sum( mse * listTotal, na.rm=TRUE ) / total,
    mse_oerr	= custom_sum( mse_oerr * listTotal, na.rm=TRUE ) / total,
    crps_climo	= custom_sum( crps_climo * listTotal, na.rm=TRUE ) / total,
    me	= custom_sum( as.numeric( d[[ paste(strPerm, "me", sep="_") ]] ) * listTotal,na.rm=TRUE) / total,
    crps	= custom_sum( as.numeric( d[[ paste(strPerm, "crps", sep="_") ]] ) * listTotal,na.rm=TRUE) / total,
    ign	= custom_sum( as.numeric( d[[ paste(strPerm, "ign", sep="_") ]] ) * listTotal,na.rm=TRUE) / total,
    me_oerr	= custom_sum( as.numeric( d[[ paste(strPerm, "me_oerr", sep="_") ]] ) * listTotal,na.rm=TRUE) / total,
    
    spread	= sqrt(custom_sum( variance * listTotal, na.rm=TRUE ) / total),
    spread_oerr	= sqrt(custom_sum( variance_oerr * listTotal, na.rm=TRUE ) / total),
    spread_plus_oerr	= sqrt(custom_sum( variance_plus_oerr * listTotal, na.rm=TRUE ) / total)
    );
  } else if( lineTypes$boolNbrCnt ){ # perform the aggregation of the sampled NBR_CNT lines
    listTotal = d[[ paste(strPerm, "total", sep="_") ]];
    total = custom_sum( as.numeric(listTotal), na.rm=TRUE);
    listFbs = d[[ paste(strPerm, "fbs", sep="_") ]];
    listFss = d[[ paste(strPerm, "fss", sep="_") ]];
    listAFss = d[[ paste(strPerm, "afss", sep="_") ]];
    listUFss = d[[ paste(strPerm, "ufss", sep="_") ]];
    listFRate = d[[ paste(strPerm, "f_rate", sep="_") ]];
    listORate = d[[ paste(strPerm, "o_rate", sep="_") ]];

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
  } else if( lineTypes$boolPct ){
    dfPerm = d[substring(colnames(d[1,]), 1, nchar(strPerm)) == strPerm];
    dfAggPerm = dfPerm[1,];
    #drop equalize column
    if (paste(strPerm, "equalize", sep = "_") %in% colnames(dfAggPerm)) {
      dfAggPerm = dfAggPerm[, -which(colnames(dfAggPerm) %in% c(paste(strPerm, "equalize", sep = "_")))]
    }
    if (paste(strPerm, "total", sep = "_") %in% colnames(dfAggPerm)) {
      listTotal = d[[ paste(strPerm, "total", sep = "_") ]];
      total = custom_sum(listTotal, na.rm = TRUE);

    }else {
      total = NA
    }

    oy_i_index = grep("oy_i", colnames(dfAggPerm), value = FALSE);
    on_i_index = grep("on_i", colnames(dfAggPerm), value = FALSE);
    thresh_i_index = grep("thresh_i", colnames(dfAggPerm), value = FALSE);

    for (oy_i in oy_i_index) {
      dfAggPerm[1, oy_i] = custom_sum(dfPerm[, oy_i], na.rm = TRUE);
    }
    for (on_i in on_i_index) {
      dfAggPerm[1, on_i] = custom_sum(dfPerm[, on_i], na.rm = TRUE);
    }


    if (ncol(dfAggPerm) != 0 && !is.na(dfAggPerm[1, thresh_i_index][[1]])) {
      dfPctPerm = data.frame(
        thresh_i = c(t(dfAggPerm[1, thresh_i_index])),
        oy_i = c(t(dfAggPerm[1, oy_i_index])),
        on_i = c(t(dfAggPerm[1, on_i_index]))
      );

      # calculate vectors and constants to use below
      dfPctPerm$n_i = dfPctPerm$oy_i + dfPctPerm$on_i;        # n_j.
      dfPctPerm = dfPctPerm[0 != dfPctPerm$n_i,];
      if (nrow(dfPctPerm) == 0) {
        dfSeriescustom_sums = list(
          reliability = NA,
          resolution = NA,
          uncertainty = NA,
          baser = NA,
          calibration = NA,
          n_i = NA,
          roc_auc = NA,
          total = NA
        );
      } else {

        dfPctPerm$o_bar_i = dfPctPerm$oy_i / dfPctPerm$n_i;        # o_bar_i
        T = sum(dfPctPerm$n_i);
        oy_total = sum(dfPctPerm$oy_i);   
        o_bar = oy_total / T; 
        dfPctPerm$o_bar_i = dfPctPerm$oy_i / dfPctPerm$n_i

        # row-based calculations
        dfPctPerm$oy_tp = dfPctPerm$oy_i / T[intPerm];
        dfPctPerm$on_tp = dfPctPerm$on_i / T[intPerm];
        dfPctPerm$calibration = dfPctPerm$oy_i / dfPctPerm$n_i;
        dfPctPerm$refinement = dfPctPerm$n_i / T[intPerm];
        dfPctPerm$likelihood = dfPctPerm$oy_i / oy_total[intPerm];
        dfPctPerm$baserate = dfPctPerm$o_bar_i;


        # table-based stat calculations

        dfSeriescustom_sums = list(
          reliability = custom_sum(dfPctPerm$n_i * (dfPctPerm$thresh - dfPctPerm$o_bar_i)^2) / T[intPerm],
          resolution = custom_sum(dfPctPerm$n_i * (dfPctPerm$o_bar_i - o_bar[intPerm])^2) / T[intPerm],
          uncertainty = o_bar[intPerm] * (1 - o_bar[intPerm]),
          baser = o_bar[intPerm],
          calibration = dfPctPerm$calibration,
          n_i = dfPctPerm$n_i,
          total = total
        );

        # build the dataframe for calculating and use the trapezoidal method roc_auc
        dfROC = calcPctROC(dfPctPerm);
        dfAUC = rbind(data.frame(thresh = 0, n11 = 0, n10 = 0, n01 = 0, n00 = 0, pody = 1, pofd = 1), dfROC);
        dfAUC = rbind(dfAUC, data.frame(thresh = 0, n11 = 0, n10 = 0, n01 = 0, n00 = 0, pody = 0, pofd = 0));
        dfSeriescustom_sums$roc_auc = 0;
        for (r in 2:nrow(dfAUC)) {
          dfSeriescustom_sums$roc_auc = dfSeriescustom_sums$roc_auc + 0.5 *
            (dfAUC[r - 1,]$pody + dfAUC[r,]$pody) *
            (dfAUC[r - 1,]$pofd - dfAUC[r,]$pofd);
        }
      }
    }else {
      dfSeriescustom_sums = list(
        reliability = NA,
        resolution = NA,
        uncertainty = NA,
        baser = NA,
        calibration = NA,
        n_i = NA,
        roc_auc = NA,
        total = NA
      );
    }



  }
  return ( dfSeriescustom_sums );
}

aggregateFieldValues = function(listSeries1Val, dfStatsPerm, strPerm, lineTypes, listFields, intPerm){
  listBoot = list();
  for (strSeriesVal in names(listSeries1Val)) {
    for (index in 1 : length(listSeries1Val[[strSeriesVal]])) {
      if (grepl(';', listSeries1Val[[strSeriesVal]][index])) {
        #found the aggregated field
        vectVal = strsplit(listSeries1Val[[strSeriesVal]][index], ";")[[1]];
        uniqueValid = unique(dfStatsPerm$fcst_valid_beg)
        uniqueLead = unique(dfStatsPerm$fcst_lead)

        for (valid in uniqueValid) {
          if (strSeriesVal != 'fcst_lead') {
            for (lead in uniqueLead) {
              aggRows = dfStatsPerm[dfStatsPerm$fcst_valid_beg == valid &
                dfStatsPerm$fcst_lead == lead &
                dfStatsPerm[[strSeriesVal]] %in% vectVal,];
              rowsNumbers = which(dfStatsPerm$fcst_valid_beg == valid &
                dfStatsPerm$fcst_lead == lead &
                dfStatsPerm[[strSeriesVal]] %in% vectVal)
              #remove these rows
              dfStatsPerm = dfStatsPerm[- rowsNumbers,]
              #create a list to pass to the aggregation function
              for (strCount in listFields) {
                listCounts = aggRows[[strCount]];
                strCountName = paste(paste(intPerm, sep = "_", collapse = "_"), strCount, sep = "_", collapse = "_");
                listBoot[[strCountName]] = listCounts;
              }
              # aggregate
              dfSeriescustom_sums = calcSeriesSums (listBoot, strPerm, lineTypes);
              
              #record the result as a first row in the old selection
              for (field in listFields) {
                if (field %in% names(dfSeriescustom_sums)) {
                  aggRows[1,][[field]] = dfSeriescustom_sums[[field]]
                }
              }
              #replace the aggregated field name
              aggRows[1,][[strSeriesVal]] = listSeries1Val[[strSeriesVal]][index];
              #add this row
              dfStatsPerm <- rbind(dfStatsPerm, aggRows[1,])
            }
          }else {
            # if the aggregated field is 'fcst_lead'
            aggRows = dfStatsPerm[dfStatsPerm$fcst_valid_beg == valid & dfStatsPerm[[strSeriesVal]] %in% vectVal,];
            rowsNumbers = which(dfStatsPerm$fcst_valid_beg == valid & dfStatsPerm[[strSeriesVal]] %in% vectVal)
            #remove these rows
            dfStatsPerm = dfStatsPerm[- rowsNumbers,]
            #create a list to pass to the aggregation function
            for (strCount in listFields) {
              listCounts = aggRows[[strCount]];
              strCountName = paste(paste(intPerm, sep = "_", collapse = "_"), strCount, sep = "_", collapse = "_");
              listBoot[[strCountName]] = listCounts;
            }
            # aggregate
            dfSeriescustom_sums = calcSeriesSums (listBoot, strPerm, lineTypes);
            for (field in listFields) {
              if (field %in% names(dfSeriescustom_sums)) {
                aggRows[1,][[field]] = dfSeriescustom_sums[[field]]
              }
            }
            #replace the aggregated field name
            aggRows[1,][[strSeriesVal]] = listSeries1Val[[strSeriesVal]][index];
            #add this row
            dfStatsPerm <- rbind(dfStatsPerm, aggRows[1,])
          }
        }
      }
    }
  }
  return( dfStatsPerm );
}

calculateMtdRevisionStats = function(data){
  unique_ids = unique(data$revision_id)
  data_for_stats = c();
  
  for (id  in unique_ids){
    data_for_id = data[data$revision_id == id ,]$stat_value;
    data_for_stats = append(data_for_stats, data_for_id);
    data_for_stats = append(data_for_stats, NA);
  }
  if(length(data_for_stats) > 0){
    data_for_stats = data_for_stats[-length(data_for_stats)]; 
  }
  acf_value=acf(data_for_stats,type='correlation',na.action = na.pass, plot=FALSE);
  r= acf_value$acf[2];
  p = qnorm((1 + 0.05)/2)/sqrt(acf_value$n.used)
  revisionAc = c(r,p);
  ww_run = runs.test(data_for_stats,alternative='left.sided')$p.value;
  if (is.nan(ww_run)) {
    ww_run = 'N/A';
  } else{
    ww_run = round(ww_run, 2);
  }
  
  
  p_value = revisionAc[2];
  if (is.nan(p_value)) {
    p_value = 'N/A';
  } else{
    p_value = round(p_value, 2);
  }
  r=revisionAc[1];
  if (is.nan(r)) {
    r = 'N/A';
  } else{
    r = round(r, 2);
  }
  
  
  return(list(ww_run = ww_run, 
              auto_cor_p = p_value,
              auto_cor_r = r
  ))
}
#custom function that was udapted from the standard performance.diagram()
# added the posibility to provide custom axis names
performance.diagram.custom <- function(xl="Success Ratio", yl="Probability of Detection", ...){
  far <- seq(1, 0, length = 501)
  h <- seq(0, 1, length = 501)
  f <- function(far, h) {
    (1 - far) * h/(1 - far * (1 - h))
  }
  g <- function(far, h) {
    h/(1 - far)
  }
  hh <- function(h, b) {
    h/b
  }
  TS <- B <- matrix(NA, nrow = 501, ncol = 501)
  for (i in 1:501) {
    for (j in 1:501) {
      TS[i, j] <- f(far[i], h[j])
    }
  }
  if(yl == ''){
    yl <- "Probability of Detection"
  }
  if(xl == ''){
    xl <- "Success Ratio"
  }
  contour(t(TS), xlim = c(0, 1), ylim = c(0, 1), xlab = xl,
          ylab = yl, ...)
  BB <- c(0.3, 0.5, 0.8, 1, 1.3, 1.5, 2, 3, 5, 10)
  x0 <- 0
  y0 <- 0
  x1 <- 1
  y1 <- hh(1, 1/BB)
  segments(x0, y0, x1, y1, lty = 2, col = 1)
  id <- y1 <= 1
  mtext(side = 4, text = y1[id], at = y1[id], line = 0.3, cex = 0.7,
        las = 2)
  id <- y1 > 1
  mtext(side = 3, text = y1[id], at = 1/y1[id], line = 0.3,
        cex = 0.7)
}

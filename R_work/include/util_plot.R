#
## mapStatGroupId contains the mapping from stat_name to stat_group_lu_id
#mapStatGroupId	= list(BASER=0, FMEAN=1, ACC=2, FBIAS=3, PODY=4, PODN=5, POFD=6, FAR=7, CSI=8, GSS=9,
#                       HK=10, HSS=11, ODDS=12, FBAR=13, FSTDEV=14, OBAR=15, OSTDEV=16, PR_CORR=17,
#                       ME=18, ESTDEV=19, MBIAS=20, MAE=21, MSE=22, BCMSE=23, BCRMSE=23, RMSE=24,
#                       E10=25, E25=26, E50=27, E75=28, E90=29, BRIER=30, BASER=31, FMEAN=32, ACC=33, 
#					   FBIAS=34, PODY=35, PODN=36, POFD=37, FAR=38, CSI=39, GSS=40, HK=41, HSS=42, 
#					   ODDS=43, FBS=44, FSS=45);
#
## mapFcstLevSurf contains a mapping from fcst_var to fcst_lev surface level values 
#mapFcstLevSurf  = list(TMP="Z2", DPT="Z2", WIND="Z10", APCP_03="A3", APCP_24="A24");
#
## buildQueryList() formats the list of input values into a single string list suitable
##   for use in a SQL query.  For example, if values = c(1, 2, 3), then buildQueryList
##   will return the string "1, 2, 3" if ticks is FALSE and "'1', '2', '3'" if ticks is
##   TRUE.
#buildQueryList = function(values, ticks=TRUE){
#	strQueryList = "";
#	for(strValue in values){
#		if("" != strQueryList){ strQueryList = paste(strQueryList, ",", sep=""); }
#		if( TRUE == ticks ){
#			strQueryList = paste(strQueryList, "'", strValue, "'", sep="") ;
#		}else{
#			strQueryList = paste(strQueryList, strValue, sep="");
#		}
#	}
#	return(strQueryList);
#}

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
seriesMinMax = function(series, numModels, log=FALSE){	
	dblMin = Inf; dblMax = -Inf;
	for( i in 1:numModels ) {				
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
	if( Inf == dblMin | dblMin == dblMax ){ dblMin = 0; dblMax = 1; }
	return( list(min=dblMin, max=dblMax) );
}

# numSeries() calculates the number of series based on the information in the listSeriesVal
#   input.  Each permutation of series values yields a series, plus an extra series for the
#   difference associated with the last series.
numSeries = function(listSeriesVal, boolDiff){
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
	
	return( (intNumSeries) );
}

# eventEqualize() assumes that the input dfStats contains data indexed by fcst_valid_beg 
#   and fcst_lead fields.  It builds a new dataframe which contains the same data except
#   for records that don't have corresponding fcst_valid_beg values for a given fcst_lead.
#   This ensures that incomplete or missing data sets are not used in plots.
eventEqualize = function(dfStats, listFcstLead, strIndyVar){
	
	# convert the dates from strings to POSIXct
	dfStats$fcst_valid_beg = as.POSIXct(dfStats$fcst_valid_beg, format="%Y-%m-%d %H:%M:%S", tz="GMT");
	dfStats$equalize = paste(dfStats$fcst_valid_beg, dfStats[[strIndyVar]]);
	#dfStats$equalize = dfStats$fcst_valid_beg;
	
	# create a list of permutations representing the plot series
	dfSeriesPerm = data.frame( permute(listSeriesVal) );
	names(dfSeriesPerm) = names(listSeriesVal);
	
	# for each fcst_lead value, equalize the plot series by fcst_valid_beg
	cat("  event equalization...");
	boolWarnings = FALSE;
	dfStatsEq = dfStats[array(FALSE,nrow(dfStats)),];
	for(intFcstLead in listFcstLead){
		
		# examine the stats for the current lead time
		dfFcstLead = dfStats[dfStats$fcst_lead == intFcstLead,];
		if( 1 > nrow(dfFcstLead) ){
			#boolWarnings = TRUE;
			#cat("\n    WARNING: no stats found for fcst_lead =", intFcstLead);
			next;
		}
		
		# find the minimal list of dates which all series have in common
		listDates = array();
		listEqualize = array();
		for(intSeries in 1:nrow(dfSeriesPerm)){
			for(strSeriesVar in names(listSeriesVal)){
				valSeries = array(dfSeriesPerm[[strSeriesVar]])[intSeries];
				dfComp = dfFcstLead[dfFcstLead[[strSeriesVar]] == valSeries,];
			}
			
			if( 0 < sum(is.na(listEqualize)) ){
				listEqualize = dfComp$equalize;
			} else {
				listInd = listEqualize %in% dfComp$equalize;
				
				listDiscard = listEqualize[ !listInd ];
				for(strDiscard in listDiscard){
					boolWarnings = TRUE;
					cat("\n    WARNING: discarding", paste(dfSeriesPerm[intSeries,]), "series member", strDiscard);
				}
				
				listEqualize = listEqualize[listInd];
			}			
		}
		
		# create an equalized set of data for the minimal list of dates
		dfFcstLeadEq = dfFcstLead[dfFcstLead$equalize %in% listEqualize,];
		intEqRow = nrow(dfStatsEq);
		dfStatsEq[(intEqRow+1):(intEqRow+nrow(dfFcstLeadEq)),] = dfFcstLeadEq;
	}
	
	if( nrow(dfStatsEq) != nrow(dfStats) ){
		boolWarnings = TRUE;
		cat("\n    WARNING: event equalization removed ", (nrow(dfStats) - nrow(dfStatsEq)), " rows", sep="");
	}
	if( TRUE == boolWarnings){		cat("\n  event equalization done\n");		}
	else{							cat(" done\n");								}
	dfStats = dfStatsEq;
	
	return( dfStats );
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

# buildSeries() assumes that the input dfStats contains stat data for one or more series
#   for ploting.  The independent variables and values are specified in strIndyVar and 
#   listIndyVal.  strIndyVar must be a valid field name and the listIndyVal values must 
#   be present in dfStats.  strStatGroup contains the name of the dependent variable,
#   which indicates how to handle the stat data.  listSeriesVal specifies field names and
#   values which correspond to the plot series, e.g. vx_mask and model.  dfStats must 
#   contain the values in listSeriesVal.  Each permutation of values in listSeriesVar will
#   be plotted.  If boolPlotDiff is TRUE, a plot series will be constructed from the
#   difference between the last two series.  buildSeries() will return three structures. 
#   The series list contains series data to pass to the plot function, in the format of
#   median, upper std error and lower std error for each series.  Second, the nstats list
#   contains the number of stats used to compute the std error at each point.  Finally,
#   the legend information is returned as a list.
#
#     INPUTS:
#           dfStats: contains independent, series and stat data for one or more plot series
#      intNumSeries: number of series to be built
#        strIndyVar: field name of independent variable, e.g. fcst_lev or fcst_lead
#       listIndyVal: independent variable values
#      strStatGroup: field name of dependent variable, e.g. TMP or BCRMSE
#     listSeriesVal: series variable values, one series per permutation
#      boolPlotDiff: indicates whether to construct a difference plot series
#        listPlotCI: list of confidence interval types to use for each series
#          dblAlpha: alpha value to use when calculating confidence intervals 
#
#    RETURNS:
#            series: contains series data, in sets of three vectors: median, upper and
#                    lower std error values; intended to be passed to plot function
#            nstats: contains the number of stats used to compute std error at each point
#            legend: list of plot series descriptions
#
buildSeries = function(dfStats, strIndyVar, listIndyVal, strStatGroup, listSeriesVal, boolDiff,
					   listPlotCI, dblAlpha=.05){

	# calculate the number of series and add the __DIFF__ marker, if necessary
	intNumSeries = 1;
	listSeriesVar = names(listSeriesVal);
	for(intSeriesVar in 1:length(listSeriesVar)){
		strSeriesVar = listSeriesVar[intSeriesVar];
		intNumSeriesVal = length( listSeriesVal[[ strSeriesVar ]] );
		if( intSeriesVar == length(listSeriesVar) & boolDiff == TRUE ){
			listSeriesVal[[ strSeriesVar ]] = append(listSeriesVal[[ strSeriesVar ]], "__DIFF__");
			intNumSeriesVal = intNumSeriesVal + 1;
		}
		intNumSeries = intNumSeries * intNumSeriesVal;
	}

	# storage for the levels and statistics of each plot series	
	intNumIndy = length(listIndyVal);
	listSeries = list();
	for(intSeries in 1:intNumSeries){
		listSeries[[(3*(intSeries-1)) + 1]] = rep(NA, intNumIndy);
		listSeries[[(3*(intSeries-1)) + 2]] = rep(NA, intNumIndy);
		listSeries[[(3*(intSeries-1)) + 3]] = rep(NA, intNumIndy);
	}
	listNStats = c();
	
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
		for(intPermVal in 1:nrow(matPermVal)){
			listPermVal = matPermVal[intPermVal,];

			# calculate the indexes for this series
			intMedIndex	= (3*(intSeriesIndex-1)) + 1;
			intLoIndex	= (3*(intSeriesIndex-1)) + 2;
			intUpIndex	= (3*(intSeriesIndex-1)) + 3;

			# if the last perm val is diff, then set the diff flag
			if( "__DIFF__" == listPermVal[length(listPermVal)] ){
				listSeries[[intMedIndex]][intIndyIndex] = listSeries[[intMedIndex - 3]][intIndyIndex];
				listSeries[[intLoIndex]][intIndyIndex] = listSeries[[intLoIndex - 3]][intIndyIndex];
				listSeries[[intUpIndex]][intIndyIndex] = listSeries[[intUpIndex - 3]][intIndyIndex];
				intMedIndex = intMedIndex - 3;
				intLoIndex = intLoIndex - 3;
				intUpIndex = intUpIndex - 3;

				if( "BCRMSE" == strStatGroup ){ listStats = sqrt(dfStatsVal$stat_value - dfStatsComp$stat_value); }
				else                          { listStats = dfStatsVal$stat_value - dfStatsComp$stat_value;       }
				
			} else {
				
				# build a dataset that pertains to the permutation values
				if( TRUE == exists("dfStatsVal") ){ dfStatsComp = dfStatsVal; }
				dfStatsVal = dfStatsIndy;
				for(intVar in 1:length(listSeriesVar)){
					dfStatsVal = dfStatsVal[dfStatsVal[[ listSeriesVar[intVar] ]] == listPermVal[intVar],];
				}
				
				# if there is no data for this case, skip to the next permutation
				if( 1 > nrow(dfStatsVal) ){
					rm(dfStatsVal);
					intSeriesIndex = intSeriesIndex + 1;
					next;
				}
			
				# add the median value to the current series point
				if( "BCRMSE" == strStatGroup ){ listStats = sqrt(dfStatsVal$stat_value); }
				else                          { listStats = dfStatsVal$stat_value;       }
			}
			dblMed = median(listStats);

			#  apply the requested type of confidence interval to the current series point
			strPlotCI = listPlotCI[intSeriesIndex];
			dblLoCI = dblMed;
			dblUpCI = dblMed;
			if( "std" == strPlotCI & 0 < sum(listStats != 0) ){
				dblStdErr = 0;
				seModel = try(Compute_STDerr_from_median( listStats, method = 'ML' ));
				if( 1 < length(seModel) && 0 == seModel[2] ){ dblStdErr = dblZVal * seModel[1]; }
				dblLoCI = dblMed - dblStdErr;
				dblUpCI  = dblMed + dblStdErr;
			} else if( "norm" == strPlotCI ){
				if( !is.na(dfStatsVal$stat_ncl) & !is.na(dfStatsVal$stat_ncu) &
					-9999 != dfStatsVal$stat_ncl & -9999 != dfStatsVal$stat_ncu ){
					dblLoCI = dfStatsVal$stat_ncl;
					dblUpCI = dfStatsVal$stat_ncu;
				}
			} else if( "boot" == strPlotCI ){
				if( !is.na(dfStatsVal$stat_bcl) & !is.na(dfStatsVal$stat_bcu) &
					-9999 != dfStatsVal$stat_bcl & -9999 != dfStatsVal$stat_bcu ){
					dblLoCI = dfStatsVal$stat_bcl;
					dblUpCI = dfStatsVal$stat_bcu;
				}
			}
			
			listSeries[[intMedIndex]][intIndyIndex] = dblMed;
			listSeries[[intLoIndex]][intIndyIndex] = dblLoCI;
			listSeries[[intUpIndex]][intIndyIndex] = dblUpCI;
				
			intSeriesIndex = intSeriesIndex + 1;
		}
		listNStats = append(listNStats, nrow(dfStatsIndy));
		
		intIndyIndex = intIndyIndex + 1;
	}
	
	# build the legend for the list of curves
	listLegend = c();
	for(intPermVal in 1:nrow(matPermVal)){
		listPermVal = matPermVal[intPermVal,]	;		
		strLegend = "";
		for(strVal in listPermVal){ strLegend = paste(strLegend, " ", strVal, sep=""); }
		listLegend = append(listLegend, strLegend);
	}
	
	return( list(series=listSeries[], nstats=listNStats, legend=listLegend) );
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

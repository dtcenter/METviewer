
# mapStatGroupId contains the mapping from stat_name to stat_group_lu_id
mapStatGroupId	= list(BASER=0, FMEAN=1, ACC=2, FBIAS=3, PODY=4, PODN=5, POFD=6, FAR=7, CSI=8, GSS=9,
                       HK=10, HSS=11, ODDS=12, FBAR=13, FSTDEV=14, OBAR=15, OSTDEV=16, PR_CORR=17,
                       ME=18, ESTDEV=19, MBIAS=20, MAE=21, MSE=22, BCMSE=23, BCRMSE=23, RMSE=24,
                       E10=25, E25=26, E50=27, E75=28, E90=29, BRIER=30, BASER=31, FMEAN=32, ACC=33, 
					   FBIAS=34, PODY=35, PODN=36, POFD=37, FAR=38, CSI=39, GSS=40, HK=41, HSS=42, 
					   ODDS=43, FBS=44, FSS=45) 

# mapFcstLevSurf contains a mapping from fcst_var to fcst_lev surface level values 
mapFcstLevSurf  = list(TMP="Z2", DPT="Z2", WIND="Z10", APCP_03="A3", APCP_24="A24")

# parseLev() assumes that the input is a list of pressure level strings of one of the
#   following formats: Z0, P250 or P200-350 and attempts to parse the value.  If 
#   successful, an integer is returned representing the input pressure level.  For Z0, 
#   800 is returned.  For P250, 250 is returned.  For P200-350, 275 is returned - the 
#   midpoint of the range.  If the format is unrecognized, -1 is returned.
parseLev = function(listLev){
	listRet = c()
	for(strLev in listLev){
		intLev = numeric()
		if( grepl("P\\d+\\-\\d+", strLev, perl=TRUE) ){
			intLev = mean(as.numeric( unlist(strsplit(strLev, "[P\\-]", perl=TRUE))[2:3] ))
		} else if( grepl("P\\d+", strLev, perl=TRUE) ){
			intLev = as.numeric(sub("P", "", strLev))
		} else if( grepl("Z\\d", strLev, perl=TRUE) ){
			intLev = 800
		} else {
			intLev = -1
		}
		listRet[length(listRet)+1] = intLev
	}
	(listRet)
}

# seriesMinMax() assumes that the input list contains lists of numerics representing
#   the series values, low error bar values and high error bar values in 3-tuples.  
#   If so, seriesMinMax will return the minimum value of the low error bar values and
#   the maximum value of the high error bar values.  This method is called by plotting
#   functions to determine sensible axis bounds.
seriesMinMax = function(listSeries, Nmodels){	
	min = Inf; max = -Inf
	for( i in 1:Nmodels ) {
		listMin = na.omit( listSeries[[3*(i-1)+2]] )
		if( 0 < length(listMin) ){
			min2 = min(listMin)
			if( min2 < min ){ min = min2 }
		}
		
		listMax = na.omit( listSeries[[3*(i-1)+3]] )
		if( 0 < length(listMax) ){
			max2 = max(listMax)
			if( max2 > max ){ max = max2 }
		}
	}
	list(min=min, max=max)
}

# eventEqualize() assumes that the input dfStats contains data indexed by fcst_valid_beg 
#   and fcst_lead fields.  It builds a new dataframe which contains the same data except
#   for records that don't have corresponding fcst_valid_beg values for a given fcst_lead.
#   This ensures that incomplete or missing data sets are not used in plots.
eventEqualize = function(dfStats, listFcstLead, strIndyVar){
	# convert the dates from strings to POSIXct
	dfStats$fcst_valid_beg = as.POSIXct(dfStats$fcst_valid_beg, format="%Y-%m-%d %H:%M:%S", tz="GMT")	
	dfStats$equalize = paste(dfStats$fcst_valid_beg, dfStats[[strIndyVar]])
	#dfStats$equalize = dfStats$fcst_valid_beg
	
	# create a list of permutations representing the plot series
	dfSeriesPerm = data.frame( permute(listSeriesVal) )
	names(dfSeriesPerm) = names(listSeriesVal)
	
	# for each fcst_lead value, equalize the plot series by fcst_valid_beg
	cat("  event equalization...")
	boolWarnings = FALSE
	dfStatsEq = dfStats[array(FALSE,nrow(dfStats)),]
	for(intFcstLead in listFcstLead){
		
		# examine the stats for the current lead time
		dfFcstLead = dfStats[dfStats$fcst_lead == intFcstLead,]
		if( 1 > nrow(dfFcstLead) ){
			#boolWarnings = TRUE
			#cat("\n    WARNING: no stats found for fcst_lead =", intFcstLead)
			next	
		}
		
		# find the minimal list of dates which all series have in common
		listDates = array()
		listEqualize = array()
		for(intSeries in 1:nrow(dfSeriesPerm)){
			for(strSeriesVar in names(listSeriesVal)){
				valSeries = array(dfSeriesPerm[[strSeriesVar]])[intSeries]
				dfComp = dfFcstLead[dfFcstLead[[strSeriesVar]] == valSeries,]
			}
			
			if( 0 < sum(is.na(listEqualize)) ){
				listEqualize = dfComp$equalize
			} else {
				listInd = listEqualize %in% dfComp$equalize
				
				listDiscard = listEqualize[ !listInd ]
				for(strDiscard in listDiscard){
					boolWarnings = TRUE
					cat("\n    WARNING: discarding", paste(dfSeriesPerm[intSeries,]), "series member", strDiscard)
				}
				
				listEqualize = listEqualize[listInd]				
			}			
		}
		
		# create an equalized set of data for the minimal list of dates
		dfFcstLeadEq = dfFcstLead[dfFcstLead$equalize %in% listEqualize,]		
		intEqRow = nrow(dfStatsEq)
		dfStatsEq[(intEqRow+1):(intEqRow+nrow(dfFcstLeadEq)),] = dfFcstLeadEq
	}
	
	if( nrow(dfStatsEq) != nrow(dfStats) ){
		boolWarnings = TRUE
		cat("\n    WARNING: event equalization removed ", (nrow(dfStats) - nrow(dfStatsEq)), " rows", sep="")	
	}
	if( TRUE == boolWarnings){		cat("\n  event equalization done\n")		}
	else{							cat(" done\n")								}
	dfStats = dfStatsEq	
}

# permute() builds a permutation of all of the values in the input listVals, which are
#   indexed by the variable names in listVars.  The permutations are returned as a
#   matrix, with columns corresponding to the variable names in listVars.
permute = function(listVals){
	listVars = names(listVals)
	
	# if there is only one variable left, return the single permutation matrix
	if( 1 == length(listVars) ){
		listValsSub = listVals[[ listVars[1] ]]
		matPerm = matrix(nrow=length(listValsSub), ncol=1)
		matPerm[1:length(listValsSub),] = listValsSub
		return(matPerm)
	}
	
	# permute over the lower-order variables
	intVarsLen = length(listVars)
	matPermSub = permute(listVals[2:intVarsLen])
	intSubNrow = nrow(matPermSub)		
	
	# build the permutation matrix by copying the lower-order matrix
	strVar = listVars[1]
	listValsSub = listVals[[strVar]]
	intValsLen = length(listValsSub)
	matPerm = matrix(nrow=(intSubNrow*intValsLen), ncol=intVarsLen)
	for( intVal in 1:length(listValsSub) ){
		intRowStart = (intVal-1)*intSubNrow + 1
		intRowStop = intRowStart + intSubNrow - 1
		matPerm[intRowStart:intRowStop,1] = rep(listValsSub[intVal], intSubNrow)
		matPerm[intRowStart:intRowStop,2:ncol(matPerm)] = matPermSub
	}
	return(matPerm)
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
#        strIndyVar: field name of independent variable, e.g. fcst_lev or fcst_lead
#       listIndyVal: independent variable values
#      strStatGroup: field name of dependent variable, e.g. TMP or BCRMSE
#     listSeriesVal: series variable values, one series per permutation
#      boolPlotDiff: indicates whether to construct a difference plot series
#        listPlotCI: list of confidence interval types to use for each series
#
#    RETURNS:
#            series: contains series data, in sets of three vectors: median, upper and
#                    lower std error values; intended to be passed to plot function
#            nstats: contains the number of stats used to compute std error at each point
#            legend: list of plot series descriptions
#
buildSeries = function(dfStats, strIndyVar, listIndyVal, strStatGroup, listSeriesVal, boolPlotDiff,
					   listPlotCI){
	# calculate the number of series
	intNumSeries = 1
	listSeriesVar = names(listSeriesVal)
	for(strSeriesVar in names(listSeriesVal)){
		intNumSeries = intNumSeries * length( listSeriesVal[[ strSeriesVar ]] )
	}
	
	# storage for the levels and statistics of each plot series	
	intNumIndy = length(listIndyVal)
	listSeries = list()
	for(intSeries in 1:intNumSeries){
		listSeries[[(3*(intSeries-1)) + 1]] = rep(NA, intNumIndy)
		listSeries[[(3*(intSeries-1)) + 2]] = rep(NA, intNumIndy)
		listSeries[[(3*(intSeries-1)) + 3]] = rep(NA, intNumIndy)
	}
	listNStats = c()
	
	# get the list of value permutations
	matPermVal = permute(listSeriesVal)

	# build the amplification for the selected value of alpha for standard error
	dblAlpha = .05
	dblZ = qnorm(1 - (dblAlpha/2));
	dblZVal = (dblZ + dblZ/sqrt(2)) / 2
	
	# build a series for each stat curve
	intIndyIndex = 1
	for(indy in listIndyVal){
		dfStatsIndy = dfStats[dfStats[[strIndyVar]] == indy,]
		
		intSeriesIndex = 1
		for(intPermVal in 1:nrow(matPermVal)){
			
			# calculate the indexes for this series
			intMedIndex	= (3*(intSeriesIndex-1)) + 1
			intLoIndex	= (3*(intSeriesIndex-1)) + 2
			intUpIndex	= (3*(intSeriesIndex-1)) + 3
			
			# build a dataset that pertains to the permutation values
			listPermVal = matPermVal[intPermVal,]			
			if( TRUE == exists("dfStatsVal") ){		dfStatsComp = dfStatsVal		}
			dfStatsVal = dfStatsIndy
			for(intVar in 1:length(listSeriesVar)){
				dfStatsVal = dfStatsVal[dfStatsVal[[ listSeriesVar[intVar] ]] == listPermVal[intVar],]
			}
			
			# if there is no data for this case, skip to the next permutation
			if( 1 > nrow(dfStatsVal) ){
				intSeriesIndex = intSeriesIndex + 1
				next
			}
		
			# add the median value to the current series point
			if( "BCRMSE" == strStatGroup ){ listStats = sqrt(dfStatsVal$stat_value) }
			else                          { listStats = dfStatsVal$stat_value       }
			dblMed = median(listStats)		
			listSeries[[intMedIndex]][intIndyIndex] = dblMed


			#  apply the requested type of confidence interval to the current series point
			strPlotCI = listPlotCI[intSeriesIndex]
			dblLoCI = dblMed
			dblUpCI = dblMed
			if( "std" == strPlotCI & 0 < sum(listStats != 0) ){
				dblStdErr = 0				
				seModel = try(Compute_STDerr_from_median( listStats, method = 'ML' ))
				if( 1 < length(seModel) && 0 == seModel[2] ){ dblStdErr = dblZVal * seModel[1] }
				dblLoCI = dblMed - dblStdErr
				dblUpCI  = dblMed + dblStdErr
			} else if( "norm" == strPlotCI ){
				if( -9999 != dfStatsVal$stat_ncl & -9999 != dfStatsVal$stat_ncu ){
					dblLoCI = dfStatsVal$stat_ncl
					dblUpCI = dfStatsVal$stat_ncu
				}
			} else if( "boot" == strPlotCI ){
				if( -9999 != dfStatsVal$stat_bcl & -9999 != dfStatsVal$stat_bcu ){
					dblLoCI = dfStatsVal$stat_bcl
					dblUpCI = dfStatsVal$stat_bcu
				}
			}
			listSeries[[intLoIndex]][intIndyIndex] = dblLoCI
			listSeries[[intUpIndex]][intIndyIndex] = dblUpCI
				
			intSeriesIndex = intSeriesIndex + 1
		}
		listNStats = append(listNStats, nrow(dfStatsIndy))
		
		intIndyIndex = intIndyIndex + 1
	}
	
	# build the legend for the list of curves
	listLegend = c()
	for(intPermVal in 1:nrow(matPermVal)){
		listPermVal = matPermVal[intPermVal,]			
		strLegend = ""
		for(strVal in listPermVal){ strLegend = paste(strLegend, " ", strVal, sep="") }
		listLegend = append(listLegend, strLegend)
	}
	
	list(series=listSeries[], nstats=listNStats, legend=listLegend) 
}

# padSpaces() adds spaces to the right of the input string, str, for formatting purposes.
padSpaces = function(str, len=16){
	while( len > nchar(str) ){		str = paste(" ", str, sep="")		}
	return(str)
}

# scaleStats() assumes that the input data frame contains statistics that require scaling and 
#   that the listDepScale input contains the scaling factor, indexed by strScaleName.  If the
#   scaling factor is present, the stat_value member and CI members of dfPlotStat will be
#   scaled accordingly.
scaleStats = function(dfPlotStat, listDepScale, strScaleName){
	if( !is.null(listDepScale[[strScaleName]]) ){
		dblScale = as.numeric(listDepScale[[strScaleName]])
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
	(dfPlotStat);
}
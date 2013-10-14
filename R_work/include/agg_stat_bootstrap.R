library(boot);

# parse the command line arguments
strInputInfoFile = "/d1/pgoldenb/var/gfs_nam/R_work/data/plot_fss_ci_NAM_15km.agg_stat.info";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("agg_stat_bootstrap.R\ninput file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# read the plot data
dfStatsRec = read.delim(strInputDataFile);
intYMax = 1;
if( 0 < length(listSeries2Val) ){ intYMax = 2; }

# build the case information for each record
dfStatsRec$case = paste(dfStatsRec$fcst_valid, dfStatsRec[[strIndyVar]], sep="#");


# buildPermData() extracts data for only the specified permutation from the specified stats data 
#   frame and returns it.  The input data frame must have columns for each of the permutation
#   fields whose values correspond.  This information is used to extract and return the rows
#   for the specified permutation.
#
# INPUTS:
#         dfStats: data frame containing the permutation records to extract
#        listPerm: list containing the perm variable/value pairs to extract
buildPermData = function(dfStats, listPerm){
	dfStatsPerm = dfStats;
	for(intSeriesVal in 1:length(listSeriesVar)){
		strSeriesVar = listSeriesVar[intSeriesVal];
		strSeriesVal = listPerm[intSeriesVal];
		if( grepl("^[0-9]+$", strSeriesVal) ){ strSeriesVal = as.numeric(strSeriesVal); }
		dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] == strSeriesVal,];
	}
	return (dfStatsPerm);
}

calcRATIO_FSA_ASA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) / nrow(d[ d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSA_ASA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
               			nrow(d[ d$simple_flag == 1, ]) ) ;
}

calcRATIO_ASM_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) /
               			nrow(d[ d$simple_flag == 1, ]) ) ;
}

calcRATIO_ASU_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 0, ]) /
               			nrow(d[ d$simple_flag == 1, ]) ) ;
}

calcRATIO_FSM_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
               			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_FSU_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
               			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSM_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
                 			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSU_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
                 			nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_FSM_ASM=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
                 			nrow(d[ d$matched_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSM_ASM=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
                    			nrow(d[ d$matched_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_FSU_ASU=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
                        			nrow(d[ d$matched_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSU_ASU=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
                           			nrow(d[ d$matched_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_FSA_AAA=function(d){
##!!!!!!!! This is the division by the count of all object_id
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
                         nrow(d) ) ;
}

calcRATIO_OSA_AAA=function(d){
##!!!!!!!! This is the division by the count of all object_id
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
                         nrow(d) ) ;
}

calcRATIO_FSA_FAA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$fcst_flag == 1, ]) ) ;
}

calcRATIO_FCA_FAA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
                              			nrow(d[ d$fcst_flag == 1, ]) ) ;
}

calcRATIO_OSA_OAA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$fcst_flag == 0, ]) ) ;
}

calcRATIO_OCA_OAA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
                             			nrow(d[ d$fcst_flag == 0, ]) ) ;
}

calcRATIO_FCA_ACA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
                             			nrow(d[ d$simple_flag == 0, ]) ) ;
}

calcRATIO_OCA_ACA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
                             			nrow(d[ d$simple_flag == 0, ]) ) ;
}
calcRATIO_FSA_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$simple_flag == 1, ]) ) ;
}
calcRATIO_OSA_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}
calcRATIO_ACA_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 0, ]) /
                             			nrow(d[ d$simple_flag == 1, ]) ) ;
}
calcRATIO_ASA_ACA=function(d){
  return ( nrow(d[ d$simple_flag == 1, ]) /
                             			nrow(d[ d$simple_flag == 0, ]) ) ;
}
calcRATIO_FCA_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
                             			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}
calcRATIO_FSA_FCA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) ) ;
}

calcRATIO_OCA_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
                             			nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calcRATIO_OSA_OCA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
                             			nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) ) ;
}

calcOBJHITS=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) / 2 ) ;
}
calcOBJMISSES=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ])  ) ;
}

calcOBJFAS=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ])  ) ;
}

calcOBJCSI=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$matched_flag == 0, ]) /
                 ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) + 2 *
                 nrow(d[ d$simple_flag == 1 & d$matched_flag == 0, ]) ) ) ;
}

calcOBJPODY=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) /
                  (nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) + 2 *
                 nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) ) ) ;
}
calcOBJFAR=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
                  (nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) +
                 nrow(d[  d$simple_flag == 1 & d$matched_flag == 1, ]) / 2) ) ;
}

calcAREARAT_FSA_ASA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
             					  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSA_ASA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
         					  sum( d[ d$simple_flag == 1 , ]$area ) ) ;

}

calcAREARAT_ASM_ASA=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
         					  sum( d[ d$simple_flag == 1, ]$area ) ) ;

}
calcAREARAT_ASU_ASA=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
         					  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_FSM_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
                     sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_FSU_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
                     sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSM_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
                     sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}
calcAREARAT_OSU_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
                       sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_FSM_ASM=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
                      sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSM_ASM=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
                      sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) ) ;
}

calcAREARAT_FSU_ASU=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
                      sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area ) ) ;
}

calcAREARAT_OSU_ASU=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
                      sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area ) ) ;
}

calcAREARAT_FSA_AAA=function(d){
#!!!!!!!! This is the division by the count of all object_id
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
                       nrow(d) ) ;
}

calcAREARAT_OSA_AAA=function(d){
#!!!!!!!! This is the division by the count of all object_id
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
                   nrow(d) ) ;
}

calcAREARAT_FSA_FAA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
                      sum( d[ d$fcst_flag == 1, ]$area ) ) ;
}

calcAREARAT_FCA_FAA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
                         sum( d[ d$fcst_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSA_OAA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
                           sum( d[ d$fcst_flag == 0, ]$area ) ) ;
}

calcAREARAT_OCA_OAA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
                             sum( d[ d$fcst_flag == 0, ]$area ) ) ;
}

calcAREARAT_FCA_ACA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
                               sum( d[ d$simple_flag == 0, ]$area ) ) ;
}
calcAREARAT_OCA_ACA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
                                 sum( d[ d$simple_flag == 0, ]$area ) ) ;
}

calcAREARAT_FSA_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
         					  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSA_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
         					  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_ACA_ASA=function(d){
  return ( sum( d[ d$simple_flag == 0, ]$area ) /
         					  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_ASA_ACA=function(d){
  return ( sum( d[ d$simple_flag == 1, ]$area ) /
         					  sum( d[ d$simple_flag == 0, ]$area ) ) ;
}

calcAREARAT_FCA_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
         					  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_FSA_FCA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
         					  sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) ) ;
}

calcAREARAT_OCA_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
           					  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSA_OCA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
             					  sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) ) ;
}

calcOBJAHITS=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) / 2 ) ;
}

calcOBJAMISSES=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )  ) ;
}

calcOBJAFAS=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )  ) ;
}

calcOBJACSI=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
             			(sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) + 2 *
             			 sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area )) ) ;
}

calcOBJAPODY=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
             			(sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) + 2 *
             			 sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )) ) ;

}
calcOBJAFAR=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
             			(sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) + 2 *
             			 sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area )) ) ;
}



booter.iid = function(d, i){

	# build a data frame with the sampled data
	dfBoot = dfStatsIndy[ dfStatsIndy$case %in% d[i,], ]	

	# for each permutation calculate the stat from the sampled data set
	listStatVal = c();
	for(intPerm in 1:nrow(matPerm)){
		listPerm = matPerm[intPerm,];
		
		# build the data set pertinent to this series permutation
		dfStatsPerm = buildPermData(dfBoot, listPerm);
		if( 1 > nrow(dfStatsPerm) ){ listStatVal = append(listStatVal, NA); next; }
		
		# calculate the stat and add it to the list (AREARAT_FSA_OSA)
		for(strStat in listStat){
        dblStat=do.call( paste("calc", strStat, sep=""), list(d=dfStatsPerm) );	
		  	listStatVal = append(listStatVal, dblStat);
		}
	}
	return (listStatVal);
}

dfOut = data.frame();

# run the bootstrap flow for each independent variable value
for(strIndyVal in listIndyVal){
	
cat("PROCESSING:", strIndyVal, "\n");
	stBoot = Sys.time();	

	# extract the records for the current indy value
	dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
	if( 1 > nrow(dfStatsIndy) ){ next; }

	# for each series group, bootstrap the statistics
	for(intY in 1:intYMax){
	
		# build permutations for each plot series
		if( 1 == intY ){ listSeriesVal = listSeries1Val; listStat = listStat1; }
		if( 2 == intY ){ listSeriesVal = listSeries2Val; listStat = listStat2; }
		listSeriesVar = names(listSeriesVal);
		matPerm = permute(listSeriesVal);
		
		# run the bootstrap flow for each series permutation
		listCases = c();
		for(intPerm in 1:nrow(matPerm)){
			listPerm = matPerm[intPerm,];

			# build the data set pertinent to this series permutation
			dfStatsPerm = buildPermData(dfStatsIndy, listPerm);
			if( 1 > nrow(dfStatsPerm) ){ next; }
			
			# build a list of cases to sample
			listCasesCur = unique( paste( dfStatsPerm$fcst_valid, dfStatsPerm[[strIndyVar]], sep="#" ) );
			listCases = sort( unique( append(listCases, listCasesCur) ) );			
			
		}  #  END:  for intPerm

		# bootstrap the series data
		bootStat = try(boot(data.frame(case=listCases), booter.iid, intNumReplicates));

		# for each series permutation and statistic, generate confidence intervals and store the output
		intBootIndex = 1;
		for(strStat in listStat){
			
			for(intPerm in 1:nrow(matPerm)){
				listPerm = matPerm[intPerm,];
						
				# build an output entry for the current case
				listOutPerm = data.frame(listPerm);
				names(listOutPerm) = names(listSeriesVal);
				for(strStaticVar in names(listStaticVal)){
					listOutPerm[[strStaticVar]] = listStaticVal[[strStaticVar]];
				}
				listOutPerm[[strIndyVar]]	= strIndyVal;
				listOutPerm$stat_name		= strStat;
				listOutPerm$stat_value		= bootStat$t0[intBootIndex];

				# calculate the bootstrap CIs, if appropriate
				if( 1 < intNumReplicates ){
					
					# calculate the confidence interval for the current stat and series permutation
					bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
					
				}
				
				# store the bootstrapped stat value and CI values in the output dataframe
				strCIParm = strCIType;
				if( strCIType == "perc" ){ strCIParm = "percent"; }
				if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
					listOutPerm$stat_bcl = bootCI[[strCIParm]][4];
					listOutPerm$stat_bcu = bootCI[[strCIParm]][5];
				} else {
					listOutPerm$stat_bcl = NA;
					listOutPerm$stat_bcu = NA;
				}
				dfOut = rbind(dfOut, listOutPerm);
				intBootIndex = intBootIndex + 1;						
				
			}  #  END:  for intPerm
			
		}  #  END:  for strStat
		
	}  #  END:  for intY
	
	cat("  PROC TIME: ", formatTimeSpan(as.numeric(Sys.time() - stBoot, units="secs")), "\n")
		
}  #  END:  for strIndyVal

write.table( dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );


# clean up
cat("agg_arearat.R done\n");


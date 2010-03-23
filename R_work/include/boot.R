library(boot);

# parse the command line arguments
strInputInfoFile = "/d1/pgoldenb/var/qnse/R_work/data/year/thresh_series/APCP_03_GSS_NWC_00Zf12_UW_MEAN.boot.info";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}
cat("boot.R\ninput file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# variables for performance bookkeeping
stStart			= Sys.time();
dblBootTime		= 0;
dblBootCITime	= 0;
intNumBoots		= 0;

# read the input data file into a data frame
dfStatsRec = read.delim(strInputDataFile);

# build a list for output permutations
for(intSeries in 1:2){
	if( 1 == intSeries ){ listSeriesVal = listSeries1Val; boolDiff = boolDiff1; strDiffSeries = "__BOOT_DIFF1__"; listStat = listStat1; } 
	if( 2 == intSeries ){ listSeriesVal = listSeries2Val; boolDiff = boolDiff2; strDiffSeries = "__BOOT_DIFF2__"; listStat = listStat2; } 

	# add the series variables and values, including a difference series if appropriate
	listOut = listSeriesVal;
	listSeriesVar = names(listSeriesVal);
	if( TRUE == boolDiff ){
		strDiffVar = listSeriesVar[length(listSeriesVar)];
		listOut[[strDiffVar]] = append(listOut[[strDiffVar]], strDiffSeries);
	}
	
	# store the CI permutations for each series group
	if( 1 == intSeries ){ matCIPerm1 = permute(listOut); }
	if( 2 == intSeries ){ matCIPerm2 = permute(listOut); }
	
	# add the independent variable and statistics to create the list for the output data frame
	listOut[[strIndyVar]] = listIndyVal;
	listOut$stat_name = listStat;
	matOutSeries = permute(listOut);
	if( !exists("matOut") ){ matOut = matrix(nrow=0, ncol=length(names(listOut))); }
	matOut = rbind(matOut, matOutSeries);
}

# build a dataframe (dfOut) to store the bootstrapped statistics
listOutPerm = list();
intNumOut = nrow(matOut);
for(strStaticVar in names(listStaticVal)){ listOutPerm[[strStaticVar]] = rep(listStaticVal[[strStaticVar]], intNumOut); }
for(intOutCol in 1:ncol(matOut)){
	if     ( intOutCol <  ncol(matOut) - 1 ){ listOutPerm[[ listSeriesVar[intOutCol] ]] = matOut[,intOutCol]; }
	else if( intOutCol == ncol(matOut) - 1 ){ listOutPerm[[ strIndyVar ]] = matOut[,intOutCol]; }
	else									{ listOutPerm$stat_name = matOut[,intOutCol]; }
}
listOutPerm$stat_value = rep(NA, intNumOut);
listOutPerm$stat_bcl = rep(NA, intNumOut);
listOutPerm$stat_bcu = rep(NA, intNumOut);
dfOut = data.frame(listOutPerm);

# stat calculations
calcGSS = function(d){
	dblC = ( (d$fy_oy + d$fy_on) / d$total ) * (d$fy_oy + d$fn_oy);
	return( (d$fy_oy - dblC) / (d$fy_oy + d$fy_on + d$fn_oy - dblC) );
}
calcFBIAS		= function(d){ return( (d$fy_oy + d$fy_on) / (d$fy_oy + d$fn_oy) ); }
calcPODY		= function(d){ return( d$fy_oy / (d$fy_oy + d$fn_oy) );             }
calcFAR			= function(d){ return( d$fy_on / (d$fy_oy + d$fy_on) );             }
calcCSI			= function(d){ return( d$fy_oy / (d$fy_oy + d$fy_on + d$fn_oy) );   }
calcBASER		= function(d){ return( (d$fy_oy + d$fn_oy) / d$total );             }

# booter function
booter.iid = function(d, i){

	# initialize storage
	boolPermDiff = FALSE;
	listPrev = list();
	listRet = list();
	for(strStat in listStat){
		listPrev[[strStat]] = 0;
		listRet[[strStat]] = c();
	}

	# for each series permutation, build a summed contingency table and calculate statistics
	for(intPerm in 1:nrow(matPerm)){
		
		# if the difference stat is requested, calculate it during the last permutation
		if( intPerm == nrow(matPerm) & TRUE == boolDiff ){ boolPermDiff = TRUE; }
		
		# build a dataframe containing sums of the contingency table constituents for this series 
		strPerm = paste(matPerm[intPerm,], sep="_", collapse="_");
		dfSeriesSums = data.frame(
			total	= sum( d[i,][[ paste(strPerm, "total", sep="_") ]] ),
			fy_oy	= sum( d[i,][[ paste(strPerm, "fy_oy", sep="_") ]] ),
			fy_on	= sum( d[i,][[ paste(strPerm, "fy_on", sep="_") ]] ),
			fn_oy	= sum( d[i,][[ paste(strPerm, "fn_oy", sep="_") ]] ),
			fn_on	= sum( d[i,][[ paste(strPerm, "fn_on", sep="_") ]] )
		);
		
		# return a value for each statistic
		for(strStat in listStat){
			
			# calculate and store the statistic
			dblStat = do.call( paste("calc", strStat, sep=""), list(d=dfSeriesSums) );
			listRet[[strStat]] = append(listRet[[strStat]], dblStat);
		
			# either calculate the stat difference, or store it for the next loop 
			if( TRUE == boolPermDiff )	{ listRet[[strStat]] = append(listRet[[strStat]], dblStat - listPrev[[strStat]]); }
			else						{ listPrev[[strStat]] = dblStat; }
			
		}
	
	}
	return( unlist(listRet) );
}

# run the bootstrap flow for each independent variable value
for(strIndyVal in listIndyVal){
	
	dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
	
	# for each series group, bootstrap the statistics
	for(intSeries in 1:2){

		# build permutations for each plot series
		if( 1 == intSeries ){ matPerm = permute(listSeries1Val); listStat = listStat1; matCIPerm = matCIPerm1; boolDiff = boolDiff1; }
		if( 2 == intSeries ){ matPerm = permute(listSeries2Val); listStat = listStat2; matCIPerm = matCIPerm2; boolDiff = boolDiff2; }

		listBoot = list();
		
		# run the bootstrap flow for each series permutation
		for(intPerm in 1:nrow(matPerm)){
			listPerm = matPerm[intPerm,];
			
			# build the data set pertinent to this series permutation
			dfStatsPerm = dfStatsIndy;
			for(intSeriesVal in 1:length(listSeriesVar)){
				strSeriesVar = listSeriesVar[intSeriesVal];
				strSeriesVal = listPerm[intSeriesVal];
				dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] == strSeriesVal,];
			}
			
			# add the contingency table constituents for this series permutation to the boot list
			strPerm = paste(listPerm, sep="_");		
			for(strCount in c("total", "fy_oy", "fy_on", "fn_oy", "fn_on")){
				listCounts = dfStatsPerm[[strCount]];
				strCountName = paste(strPerm, strCount, sep="_");
				listBoot[[strCountName]] = listCounts;
			}		
		}

		# bootstrap the series data
		dfBoot = data.frame(listBoot);
		stBoot = Sys.time();
		bootStat = try(boot(dfBoot, booter.iid, intNumReplicates));
		dblBootTime = dblBootTime + as.numeric(Sys.time() - stBoot, units="secs");
		intNumBoots = intNumBoots + 1;

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
				listOutInd = listOutInd & (dfOut$stat_name == strStat) & (dfOut[[strIndyVar]] == strIndyVal);
				
				# calculate the confidence interval for the current stat and series permutation
				stBootCI = Sys.time();
				bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));
				dblBootCITime = dblBootCITime + as.numeric(Sys.time() - stBootCI, units="secs");

				# store the bootstrapped stat value and CI values in the output dataframe
				if( class(bootCI) == "bootci" ){
					dfOut[listOutInd,]$stat_value = bootStat$t0[intBootIndex];
					dfOut[listOutInd,]$stat_bcl = bootCI[[strCIType]][4];
					dfOut[listOutInd,]$stat_bcu = bootCI[[strCIType]][5];
					intBootIndex = intBootIndex + 1;
				}
			}
		}
	
	} # end for(intSeries in 1:2)
} # end for(strIndy in listIndy)

write.table(dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t");

cat(
	"    boot time: ", formatTimeSpan(dblBootTime), "\n",
	" boot.ci time: ", formatTimeSpan(dblBootCITime), "\n",
	"   total time: ", formatTimeSpan(as.numeric(Sys.time() - stStart, units="secs")), "\n",
	"      # boots: ", intNumBoots, "\n",
	"      # stats: ", length(listStat), "\n",
	sep="");

# clean up
cat("boot.R done\n");

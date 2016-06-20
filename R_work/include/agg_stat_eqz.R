library(boot);

#event equalisation against previously calculated cases for boolModeRatioPlot

# parse the command line arguments
strInputInfoFile = "";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
	strInputInfoFile = listArgs[1];
}

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# read the plot data
dfStatsRec = read.delim(strInputDataFile);

if(boolEventEqual){
  dfStatsRec = equalizeAggStatsAgainstValues(strInputEeDataFile,listFixedValEx,listDep1Plot,listDep2Plot,listSeries1Val,listSeries2Val,dfStatsRec);
}



write.table( dfStatsRec, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );



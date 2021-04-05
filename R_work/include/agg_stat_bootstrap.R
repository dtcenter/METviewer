library(boot);

# parse the command line arguments
strInputInfoFile = "/d1/pgoldenb/var/gfs_nam/R_work/data/plot_fss_ci_NAM_15km.agg_stat.info";
listArgs = commandArgs(TRUE)
if( 0 <  length(listArgs) ) {
  strInputInfoFile = listArgs[1];
}
cat("input file: ", strInputInfoFile, "\n", sep="");

source(strInputInfoFile);
setwd(strWorkingDir);
source("util_plot.R");

# read the plot data
dfStatsRec = read.delim(strInputDataFile);
dfPlot1 = data.frame();

if(boolEventEqual){
  eeStats = read.delim(strInputEeDataFile);
  dfPlot1 = data.frame();

  for( strDep1Name in names(listDep1Plot) ){
    for(strSeriesVal in names(listSeries1Val)){
      vectValPerms = c();
      for(index in 1:length(listSeries1Val[[strSeriesVal]])){
        vectValPerms= append(vectValPerms, strsplit(listSeries1Val[[strSeriesVal]][index], ",")[[1]]);
      }
      fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep1Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms,  ];
      eeStatsEqualize = eeStats[eeStats$fcst_var == strDep1Name & eeStats[[strSeriesVal]] %in% vectValPerms,  ];
      eeStatsEqualizeUnique = unique(eeStatsEqualize$equalize);

      fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,    eeStatsEqualizeUnique);
      dfPlot1 = rbind(dfPlot1, fPlot);
    }
  }

  if(length(listSeries2Val) > 0){
    dfPlot2 = data.frame();
    for( strDep2Name in names(listDep2Plot) ){
      for(strSeriesVal in names(listSeries2Val)){
        vectValPerms = c();
        for(index in 1:length(listSeries2Val[[strSeriesVal]])){
          vectValPerms= append(vectValPerms, strsplit(listSeries2Val[[strSeriesVal]][index], ",")[[1]]);
        }
        fPlot = dfStatsRec[dfStatsRec$fcst_var == strDep2Name & dfStatsRec[[strSeriesVal]] %in% vectValPerms,  ];
        eeStatsEqualize = eeStats[eeStats$fcst_var == strDep1Name & eeStats[[strSeriesVal]] %in% vectValPerms,  ];
        eeStatsEqualizeUnique = unique(eeStatsEqualize$equalize);
        fPlot = eventEqualizeAgainstValues(fPlot, strIndyVar,   eeStatsEqualizeUnique);
        dfPlot2 = rbind(dfPlot2, fPlot);
      }

    }
    dfStatsRec = rbind(dfPlot1, dfPlot2);

  } else{
    dfStatsRec = dfPlot1;
  }
  strAfrerEqualizeFile = sub("\\.agg_stat_bootstrap", ".dataAfterEq", strInputDataFile, perl=TRUE);
  write.table(dfStatsRec, file=strAfrerEqualizeFile, quote=FALSE, row.names=FALSE, col.names=TRUE, sep = "\t");
}


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
    vectValPerms= strsplit(strSeriesVal, ",")[[1]];
    vectValPerms=lapply(vectValPerms,function(x) {if( grepl("^[0-9]+$", x) ){ x=as.integer(x); }else{x=x} })
    dfStatsPerm = dfStatsPerm[dfStatsPerm[[strSeriesVar]] %in% vectValPerms,];
  }
  return (dfStatsPerm);
}

calcRATIO_FSA_ASA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) / nrow(d[ d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSA_ASA=function(d){
  return ( calcRATIO_FSA_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSA_ASA=function(d){
  return ( calcRATIO_FSA_ASA(d[d$object_type == '3d',]) ) ;
}


calcRATIO_OSA_ASA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
  nrow(d[ d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSA_ASA=function(d){
  return ( calcRATIO_OSA_ASA(d[d$object_type == '2d',] ) ) ;
}

calc3d_RATIO_OSA_ASA=function(d){
  return ( calcRATIO_OSA_ASA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_ASM_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) /
  nrow(d[ d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_ASM_ASA=function(d){
  return ( calcRATIO_ASM_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_ASM_ASA=function(d){
  return ( calcRATIO_ASM_ASA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_ASU_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 0, ]) /
  nrow(d[ d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_ASU_ASA=function(d){
  return ( calcRATIO_ASU_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_ASU_ASA=function(d){
  return ( calcRATIO_ASU_ASA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSM_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSM_FSA=function(d){
  return ( calcRATIO_FSM_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSM_FSA=function(d){
  return ( calcRATIO_FSM_FSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSU_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSU_FSA=function(d){
  return ( calcRATIO_FSU_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSU_FSA=function(d){
  return ( calcRATIO_FSU_FSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSM_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSM_OSA=function(d){
  return ( calcRATIO_OSM_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSM_OSA=function(d){
  return ( calcRATIO_OSM_OSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSU_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSU_OSA=function(d){
  return ( calcRATIO_OSU_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSU_OSA=function(d){
  return ( calcRATIO_OSU_OSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSM_ASM=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
  nrow(d[ d$matched_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSM_ASM=function(d){
  return ( calcRATIO_FSM_ASM(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSM_ASM=function(d){
  return ( calcRATIO_FSM_ASM(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSM_ASM=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]) /
  nrow(d[ d$matched_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSM_ASM=function(d){
  return ( calcRATIO_OSM_ASM(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSM_ASM=function(d){
  return ( calcRATIO_OSM_ASM(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSU_ASU=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
  nrow(d[ d$matched_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSU_ASU=function(d){
  return ( calcRATIO_FSU_ASU(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSU_ASU=function(d){
  return ( calcRATIO_FSU_ASU(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSU_ASU=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
  nrow(d[ d$matched_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSU_ASU=function(d){
  return ( calcRATIO_OSU_ASU(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSU_ASU=function(d){
  return ( calcRATIO_OSU_ASU(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSA_AAA=function(d){
  ##!!!!!!!! This is the division by the count of all object_id
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
  nrow(d) ) ;
}

calc2d_RATIO_FSA_AAA=function(d){
  return ( calcRATIO_FSA_AAA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSA_AAA=function(d){
  return ( calcRATIO_FSA_AAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSA_AAA=function(d){
  ##!!!!!!!! This is the division by the count of all object_id
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
  nrow(d) ) ;
}

calc2d_RATIO_OSA_AAA=function(d){
  return ( calcRATIO_OSA_AAA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSA_AAA=function(d){
  return ( calcRATIO_OSA_AAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSA_FAA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 1, ]) ) ;
}

calc2d_RATIO_FSA_FAA=function(d){
  return ( calcRATIO_FSA_FAA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSA_FAA=function(d){
  return ( calcRATIO_FSA_FAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FCA_FAA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 1, ]) ) ;
}

calc2d_RATIO_FCA_FAA=function(d){
  return ( calcRATIO_FCA_FAA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FCA_FAA=function(d){
  return ( calcRATIO_FCA_FAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSA_OAA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 0, ]) ) ;
}

calc2d_RATIO_OSA_OAA=function(d){
  return ( calcRATIO_OSA_OAA(d[d$object_type == '2d',] ) );
}

calc3d_RATIO_OSA_OAA=function(d){
  return ( calcRATIO_OSA_OAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OCA_OAA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 0, ]) ) ;
}

calc2d_RATIO_OCA_OAA=function(d){
  return ( calcRATIO_OCA_OAA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OCA_OAA=function(d){
  return ( calcRATIO_OCA_OAA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FCA_ACA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
  nrow(d[ d$simple_flag == 0, ]) ) ;
}

calc2d_RATIO_FCA_ACA=function(d){
  return ( calcRATIO_FCA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FCA_ACA=function(d){
  return ( calcRATIO_FCA_ACA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OCA_ACA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
  nrow(d[ d$simple_flag == 0, ]) ) ;
}

calc2d_RATIO_OCA_ACA=function(d){
  return ( calcRATIO_OCA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OCA_ACA=function(d){
  return ( calcRATIO_OCA_ACA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FSA_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FSA_OSA=function(d){
  return ( calcRATIO_FSA_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSA_OSA=function(d){
  return ( calcRATIO_FSA_OSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSA_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OSA_FSA=function(d){
  return ( calcRATIO_OSA_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSA_FSA=function(d){
  return ( calcRATIO_OSA_FSA(d[d$object_type == '3d',]) ) ;
}


calcRATIO_ACA_ASA=function(d){
  return ( nrow(d[ d$simple_flag == 0, ]) /
  nrow(d[ d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_ACA_ASA=function(d){
  return ( calcRATIO_ACA_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_ACA_ASA=function(d){
  return ( calcRATIO_ACA_ASA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_ASA_ACA=function(d){
  return ( nrow(d[ d$simple_flag == 1, ]) /
  nrow(d[ d$simple_flag == 0, ]) ) ;
}

calc2d_RATIO_ASA_ACA=function(d){
  return ( calcRATIO_ASA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_ASA_ACA=function(d){
  return ( calcRATIO_ASA_ACA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_FCA_FSA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_FCA_FSA=function(d){
  return ( calcRATIO_FCA_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FCA_FSA=function(d){
  return ( calcRATIO_FCA_FSA(d[d$object_type == '3d',]) ) ;
}
calcRATIO_FSA_FCA=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 1 & d$simple_flag == 0, ]) ) ;
}

calc2d_RATIO_FSA_FCA=function(d){
  return ( calcRATIO_FSA_FCA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_FSA_FCA=function(d){
  return ( calcRATIO_FSA_FCA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OCA_OSA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) /
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) ) ;
}

calc2d_RATIO_OCA_OSA=function(d){
  return ( calcRATIO_OCA_OSA(d[d$object_type == '2d',]) ) ;
}
calc3d_RATIO_OCA_OSA=function(d){
  return ( calcRATIO_OCA_OSA(d[d$object_type == '3d',]) ) ;
}

calcRATIO_OSA_OCA=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1, ]) /
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 0, ]) ) ;
}

calc2d_RATIO_OSA_OCA=function(d){
  return ( calcRATIO_OSA_OCA(d[d$object_type == '2d',]) ) ;
}

calc3d_RATIO_OSA_OCA=function(d){
  return ( calcRATIO_OSA_OCA(d[d$object_type == '3d',]) ) ;
}

calcOBJHITS=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) / 2 ) ;
}

calc2d_OBJHITS=function(d){
  return ( calcOBJHITS(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJHITS=function(d){
  return ( calcOBJHITS(d[d$object_type == '3d',]) ) ;
}

calcOBJMISSES=function(d){
  return ( nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ])  ) ;
}

calc2d_OBJMISSES=function(d){
  return ( calcOBJMISSES(d[d$object_type == '2d',])  ) ;
}

calc3d_OBJMISSES=function(d){
  return ( calcOBJMISSES(d[d$object_type == '3d',])  ) ;
}

calcOBJFAS=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ])  ) ;
}

calc2d_OBJFAS=function(d){
  return ( alcOBJFAS(d[d$object_type == '2d',])  ) ;
}

calc3d_OBJFAS=function(d){
  return ( alcOBJFAS(d[d$object_type == '3d',])  ) ;
}

calcOBJCSI=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$matched_flag == 0, ]) /
  ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) + 2 *
  nrow(d[ d$simple_flag == 1 & d$matched_flag == 0, ]) ) ) ;
}

calc2d_OBJCSI=function(d){
  return ( calcOBJCSI(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJCSI=function(d){
  return ( calcOBJCSI(d[d$object_type == '3d',]) ) ;
}

calcOBJPODY=function(d){
  return ( nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) /
  (nrow(d[ d$simple_flag == 1 & d$matched_flag == 1, ]) + 2 *
  nrow(d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]) ) ) ;
}

calc2d_OBJPODY=function(d){
  return ( calcOBJPODY(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJPODY=function(d){
  return ( calcOBJPODY(d[d$object_type == '3d',]) ) ;
}

calcOBJFAR=function(d){
  return ( nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) /
  (nrow(d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]) +
  nrow(d[  d$simple_flag == 1 & d$matched_flag == 1, ]) / 2) ) ;
}

calc2d_OBJFAR=function(d){
  return ( calcOBJFAR(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJFAR=function(d){
  return ( calcOBJFAR(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSA_ASA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSA_ASA=function(d){
  return ( calcAREARAT_FSA_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSA_ASA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSA_ASA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSA_ASA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1 , ]$area ) ) ;

}

calc2d_AREARAT_OSA_ASA=function(d){
  return ( calcAREARAT_OSA_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSA_ASA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSA_ASA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_ASM_ASA=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1, ]$area ) ) ;

}

calc2d_AREARAT_ASM_ASA=function(d){
  return ( calcAREARAT_ASM_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_ASM_ASA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_ASM_ASA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_ASU_ASA=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_ASU_ASA=function(d){
  return ( calcAREARAT_ASU_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_ASU_ASA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_ASU_ASA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSM_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSM_FSA=function(d){
  return ( calcAREARAT_FSM_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSM_FSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSM_FSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSU_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSU_FSA=function(d){
  return ( calcAREARAT_FSU_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSU_FSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSU_FSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSM_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_OSM_OSA=function(d){
  return ( calcAREARAT_OSM_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSM_OSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSM_OSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSU_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_OSU_OSA=function(d){
  return ( calcAREARAT_OSU_OSA(d[d$object_type == '2d',])) ;
}

calc3d_VOLRAT_OSU_OSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSU_OSA(d[d$object_type == '3d',])) ;
}

calcAREARAT_FSM_ASM=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSM_ASM=function(d){
  return ( calcAREARAT_FSM_ASM(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSM_ASM=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSM_ASM(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSM_ASM=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) ) ;
}

calcAREARAT_OSM_ASM=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSU_ASU=function(d){
  return ( calcAREARAT_OSM_ASM(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSU_ASU=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSM_ASM(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSU_ASU=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
  sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area ) ) ;
}

calcAREARAT_FSA_AAA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
  sum(d$area) ) ;
}

calc2d_AREARAT_FSA_AAA=function(d){
  return ( calcAREARAT_FSA_AAA(d[d$object_type == '2d',])) ;
}

calc3d_VOLRAT_FSA_AAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSA_AAA(d[d$object_type == '3d',])) ;
}

calcAREARAT_OSA_AAA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
  sum(d$area) ) ;
}

calc2d_AREARAT_OSA_AAA=function(d){
  return ( calcAREARAT_OSA_AAA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSA_AAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSA_AAA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSA_FAA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSA_FAA=function(d){
  return ( calcAREARAT_FSA_FAA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSA_FAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSA_FAA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FCA_FAA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FCA_FAA=function(d){
  return ( calcAREARAT_FCA_FAA(d[d$object_type == '2d',])) ;
}

calc3d_VOLRAT_FCA_FAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FCA_FAA(d[d$object_type == '3d',])) ;
}

calcAREARAT_OSA_OAA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_OSA_OAA=function(d){
  return ( calcAREARAT_OSA_OAA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSA_OAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSA_OAA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OCA_OAA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_OCA_OAA=function(d){
  return ( calcAREARAT_OCA_OAA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OCA_OAA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OCA_OAA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FCA_ACA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$simple_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_FCA_ACA=function(d){
  return ( calcAREARAT_FCA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FCA_ACA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FCA_ACA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OCA_ACA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$simple_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_OCA_ACA=function(d){
  return ( calcAREARAT_OCA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OCA_ACA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OCA_ACA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSA_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FSA_OSA=function(d){
  return ( calcAREARAT_FSA_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSA_OSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSA_OSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSA_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_OSA_FSA=function(d){
  return ( calcAREARAT_OSA_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSA_FSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSA_FSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_ACA_ASA=function(d){
  return ( sum( d[ d$simple_flag == 0, ]$area ) /
  sum( d[ d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_ACA_ASA=function(d){
  return ( calcAREARAT_ACA_ASA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_ACA_ASA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_ACA_ASA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_ASA_ACA=function(d){
  return ( sum( d[ d$simple_flag == 1, ]$area ) /
  sum( d[ d$simple_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_ASA_ACA=function(d){
  return ( calcAREARAT_ASA_ACA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_ASA_ACA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_ASA_ACA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FCA_FSA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_FCA_FSA=function(d){
  return ( calcAREARAT_FCA_FSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FCA_FSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FCA_FSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_FSA_FCA=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 1 & d$simple_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_FSA_FCA=function(d){
  return ( calcAREARAT_FSA_FCA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_FSA_FCA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_FSA_FCA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OCA_OSA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) /
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) ) ;
}

calc2d_AREARAT_OCA_OSA=function(d){
  return ( calcAREARAT_OCA_OSA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OCA_OSA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OCA_OSA(d[d$object_type == '3d',]) ) ;
}

calcAREARAT_OSA_OCA=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1, ]$area ) /
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 0, ]$area ) ) ;
}

calc2d_AREARAT_OSA_OCA=function(d){
  return ( calcAREARAT_OSA_OCA(d[d$object_type == '2d',]) ) ;
}

calc3d_VOLRAT_OSA_OCA=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcAREARAT_OSA_OCA(d[d$object_type == '3d',]) ) ;
}

calcOBJAHITS=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) / 2 ) ;
}

calc2d_OBJAHITS=function(d){
  return ( calcOBJAHITS(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJVHITS=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAHITS(d[d$object_type == '3d',]) ) ;
}

calcOBJAMISSES=function(d){
  return ( sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )  ) ;
}

calc2d_OBJAMISSES=function(d){
  return ( calcOBJAMISSES(d[d$object_type == '2d',])  ) ;
}

calc3d_OBJVMISSES=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAMISSES(d[d$object_type == '3d',])  ) ;
}

calc2d_OBJAMISSES=function(d){
  return ( calcOBJAMISSES(d[d$object_type == '2d',])  ) ;
}

calc3d_OBJVMISSES=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAMISSES(d[d$object_type == '3d',])  ) ;
}

calcOBJAFAS=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )  ) ;
}

calc2d_OBJAFAS=function(d){
  return ( calcOBJAFAS(d[d$object_type == '2d',])  ) ;
}

calc3d_OBJVFAS=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAFAS(d[d$object_type == '3d',])  ) ;
}

calcOBJACSI=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  (sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) + 2 *
  sum( d[ d$simple_flag == 1 & d$matched_flag == 0, ]$area )) ) ;
}

calc2d_OBJACSI=function(d){
  return ( calcOBJACSI(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJVCSI=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJACSI(d[d$object_type == '3d',]) ) ;
}

calcOBJAPODY=function(d){
  return ( sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) /
  (sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area ) + 2 *
  sum( d[ d$fcst_flag == 0 & d$simple_flag == 1 & d$matched_flag == 0, ]$area )) ) ;

}

calc2d_OBJAPODY=function(d){
  return ( calcOBJAPODY(d[d$object_type == '2d',]) ) ;

}

calc3d_OBJVPODY=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAPODY(d[d$object_type == '3d',]) ) ;

}
calcOBJAFAR=function(d){
  return ( sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) /
  (sum( d[ d$fcst_flag == 1 & d$simple_flag == 1 & d$matched_flag == 0, ]$area ) + 2 *
  sum( d[ d$simple_flag == 1 & d$matched_flag == 1, ]$area )) ) ;
}

calc2d_OBJAFAR=function(d){
  return ( calcOBJAFAR(d[d$object_type == '2d',]) ) ;
}

calc3d_OBJVFAR=function(d){
  names(d)[names(d) == 'volume'] <- 'area'
  return ( calcOBJAFAR(d[d$object_type == '3d',]) ) ;
}

booter.iid = function(d, i){

  # build a data frame with the sampled data
  dfBoot = dfStatsIndy[ dfStatsIndy$case %in% d[i,], ]

  # for each permutation calculate the stat from the sampled data set
  listStatVal = c();
  # initialize storage
  listRet = list();
  listRetTest = list();
  for(strStat in listStat){
    listRet[[strStat]] = c();
    listRetTest[[strStat]] = c();
  }
  for(intPerm in 1:nrow(matPerm)){
    listPerm = matPerm[intPerm,];
    strPerm = escapeStr(paste(matPerm[intPerm,], sep="_", collapse="_"));

    # build the data set pertinent to this series permutation
    dfStatsPerm = buildPermData(dfBoot, listPerm);
    if( 1 > nrow(dfStatsPerm) ){ listStatVal = append(listStatVal, NA); next; }

    # calculate the stat and add it to the list
    for(strStat in listStat){
      dblStat=do.call( paste("calc", strStat, sep=""), list(d=dfStatsPerm) );
      listStatVal = append(listStatVal, dblStat);
      listRet[[strStat]] = append(listRet[[strStat]], dblStat);
      listRetTest[[strStat]] = append(listRetTest[[strStat]], dblStat);
      listRetTest[[strStat]] = append(listRetTest[[strStat]], strPerm);
    }
  }
  if(length(listDiffSeries) > 0){
    for( diffSeriesNameInd in 1: length(listDiffSeries) ){ #1,2....
      #get  names of defived series
      diffSeriesVec = listDiffSeries[[diffSeriesNameInd]];

      listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
      listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];

      strStat1 = listSeriesDiff1[length(listSeriesDiff1)];
      strStat2 = listSeriesDiff2[length(listSeriesDiff2)];

      listSeriesDiff1Short = listSeriesDiff1[1:(length(listSeriesDiff1)-2)];
      listSeriesDiff2Short = listSeriesDiff2[1:(length(listSeriesDiff2)-2)];

      strSeriesDiff1Short = escapeStr(paste(listSeriesDiff1Short,sep="_", collapse="_"));
      strSeriesDiff2Short = escapeStr(paste(listSeriesDiff2Short,sep="_", collapse="_"));

      derivedCurveName = getDerivedCurveName(diffSeriesVec);


      for(ind in seq(from=1, to=length(listRetTest[[strStat1]]), by=2) ) {
        if(listRetTest[[strStat1]][ind+1] == strSeriesDiff1Short){
          dblStat1=listRetTest[[strStat1]][ind];
        }
        ind=ind+1;
      }
      for(ind in seq(from=1, to=length(listRetTest[[strStat2]]), by=2) ) {
        if(listRetTest[[strStat2]][ind+1] == strSeriesDiff2Short){
          dblStat2=listRetTest[[strStat2]][ind];
        }
        ind=ind+1;
      }
      if(TRUE == exists("dblStat1") && TRUE == exists("dblStat2")){
        derivedValues = calcDerivedCurveValue(dblStat1, dblStat2, derivedCurveName);
        listRet[[paste(strStat1,strStat1,sep=",")]] = append(listRet[[paste(strStat1,strStat1,sep=",")]], derivedValues);
      }
    }
  }
  #return (listStatVal);
  return( unlist(listRet) );
}

dfOut = data.frame();

# run the bootstrap flow for each independent variable value
for(strIndyVal in listIndyVal){

  stBoot = Sys.time();

  # extract the records for the current indy value
  dfStatsIndy = dfStatsRec[dfStatsRec[[strIndyVar]] == strIndyVal,];
  if( 1 > nrow(dfStatsIndy) ){ next; }

  # for each series group, bootstrap the statistics
  for(intY in 1:intYMax){

    # build permutations for each plot series
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
        dfStatsPerm = buildPermData(dfStatsIndy, listPerm);
        if( 1 > nrow(dfStatsPerm) ){ next; }

        # build an output entry for the current case
        listOutPerm = data.frame(listPerm);
        names(listOutPerm) = names(listSeriesVal);
        for(strStaticVar in names(listStaticVal)){
          listOutPerm[[strStaticVar]] = listStaticVal[[strStaticVar]];
        }
        listOutPerm[[strIndyVar]]	= strIndyVal;
        listOutPerm$stat_name		= strStat;
        listOutPerm$stat_value		=  try(bootStat$t0[intBootIndex],silent=TRUE);
        if(is.null(listOutPerm$stat_value)){
          listOutPerm$stat_value=NA;
        }

        # calculate the bootstrap CIs, if appropriate
        if( 1 < intNumReplicates ){

          # calculate the confidence interval for the current stat and series permutation
          bootCI = try(boot.ci(bootStat, conf=(1 - dblAlpha), type=strCIType, index=intBootIndex));

        }

        # store the bootstrapped stat value and CI values in the output dataframe
        strCIParm = strCIType;
        if( strCIType == "perc" ){ strCIParm = "percent"; }
        if( exists("bootCI") == TRUE && class(bootCI) == "bootci" ){
          listOutPerm$stat_btcl = bootCI[[strCIParm]][4];
          listOutPerm$stat_btcu = bootCI[[strCIParm]][5];
        } else {
          listOutPerm$stat_btcl = NA;
          listOutPerm$stat_btcu = NA;
        }
        dfOut = rbind(dfOut, listOutPerm);
        intBootIndex = intBootIndex + 1;

      }  #  END:  for intPerm

    }  #  END:  for strStat
    #add diff series
    if(length(listDiffSeries) > 0){
      for( diffSeriesName in 1: length(listDiffSeries) ){ #1,2....
        diffSeriesVec = listDiffSeries[[diffSeriesName]];
        listSeriesDiff1 <- strsplit(diffSeriesVec[1], " ")[[1]];
        listSeriesDiff2 <- strsplit(diffSeriesVec[2], " ")[[1]];

        strStat1 = listSeriesDiff1[length(listSeriesDiff1)];
        strStat2 = listSeriesDiff2[length(listSeriesDiff2)];
        derivedCurveName = getDerivedCurveName(diffSeriesVec);

        # build an output entry for the current case
        listOutPerm = data.frame(derivedCurveName);
        names(listOutPerm) = names(listSeriesVal);
        for(strStaticVar in names(listStaticVal)){
          listOutPerm[[strStaticVar]] = listStaticVal[[strStaticVar]];
        }
        listOutPerm[[strIndyVar]]  = strIndyVal;
        listOutPerm$stat_name		= paste(strStat1,strStat2,sep=',',collapse="");
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
          listOutPerm$stat_btcl = bootCI[[strCIParm]][4];
          listOutPerm$stat_btcu = bootCI[[strCIParm]][5];
        } else {
          listOutPerm$stat_btcl = NA;
          listOutPerm$stat_btcu = NA;
        }
        dfOut = rbind(dfOut, listOutPerm);
        intBootIndex = intBootIndex + 1;
      }
    }

  }  #  END:  for intY

  #cat("  PROC TIME: ", formatTimeSpan(as.numeric(Sys.time() - stBoot, units="secs")), "\n")

}  #  END:  for strIndyVal

write.table( dfOut, file=strOutputFile, row.names=FALSE, quote=FALSE, sep="\t" );




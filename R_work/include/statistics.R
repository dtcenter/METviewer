# SL1L2 stat calculations
calcStdDev    = function(sum, sum_sq, n){
  if ( 1 > n ){
    return(NA);
  }
  v = (sum_sq - sum*sum/n)/(n - 1);
  if( 0 > v ){
    return(NA);
  } else {
    return( sqrt(v) );
  }
}
calcSL1L2_TOTAL    = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total );
}
calcGRAD_TOTAL    = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total );
}
calcFBAR    = function(d){ return( d$fbar ); }
calcOBAR    = function(d){ return( d$obar ); }
calcFSTDEV    = function(d){ return( calcStdDev(d$fbar * d$total, d$ffbar * d$total, d$total) ); }
calcOSTDEV    = function(d){ return( calcStdDev(d$obar * d$total, d$oobar * d$total, d$total) ); }
calcFOBAR    = function(d){ return( d$fobar ); }
calcFFBAR    = function(d){ return( d$ffbar ); }
calcOOBAR    = function(d){ return( d$oobar ); }
calcMAE    = function(d){ return( d$mae ); }
calcMBIAS    = function(d){ if( 0 == d$obar ){ return (NA); } else { return( d$fbar / d$obar ); } }
calcPR_CORR    = function(d){
  if( is.na(d$total) || is.na(d$ffbar) || is.na(d$fbar) || is.na(d$oobar) || is.na(d$obar) ){
    return(NA);
  } else{
    v =  (d$total^2 * d$ffbar - d$total^2 * d$fbar^2) * (d$total^2 * d$oobar - d$total^2 * d$obar^2);
    pr_corr = (d$total^2 * d$fobar - d$total^2 * d$fbar * d$obar) / sqrt(v);
    if( 0 >= v || 1 < pr_corr ){
      return(NA);
    } else {
      return( pr_corr );
    }
  }
}

calcANOM_CORR    = function(d){
  if( is.na(d$total) || is.na(d$ffbar) || is.na(d$fbar) || is.na(d$oobar) || is.na(d$obar) ){
    return(NA);
  } else{
    v =  (d$total^2 * d$ffbar - d$total^2 * d$fbar^2) * (d$total^2 * d$oobar - d$total^2 * d$obar^2);
    if( 0 >= v){
      return(NA);
    }
    anom_corr = (d$total^2 * d$fobar - d$total^2 * d$fbar * d$obar) / sqrt(v);
    if(  1 < anom_corr ){
      return(NA);
    } else {
      return( anom_corr );
    }
  }
}

calcANOM_CORR_UNCNTR    = function(d){
  if(  is.na(d$ffbar)  || is.na(d$oobar) || is.na(d$fobar) ){
    return(NA);
  } else{
    v =  d$ffbar * d$oobar;
    # Check for square root of negative number
    if( v < 0){
      return(NA);
    }
    anom_corr_uncntr = d$fobar / sqrt(v);
    # Check the computed range
    if(anom_corr_uncntr >  1){
      anom_corr_uncntr =  1.0;
    } else if(anom_corr_uncntr < -1){
      anom_corr_uncntr = -1.0;
    }
    return( anom_corr_uncntr );
  }
}

calcSAL1L2_TOTAL    = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total);
}

calcRMSFA    = function(d){
  if (is.na(d$ffbar) ||  0 > d$ffbar ){
    return (NA);
  }
  return ( sqrt(d$ffbar) );
}

calcRMSOA    = function(d){
  if (is.na(d$oobar) ||  0 > d$oobar ){
    return (NA);
  }
  return ( sqrt(d$oobar) );
}
calcME      = function(d){ return( d$fbar - d$obar ); }
calcME2      = function(d){
  me = d$fbar - d$obar;
  return( me * me );
}
calcMSE      = function(d){ return( d$ffbar + d$oobar - 2 * d$fobar ); }
calcMSESS      = function(d){
  ostdev=calcOSTDEV( d );
  mse=calcMSE( d );
  return( 1.0 - mse/(ostdev*ostdev) );
}
calcRMSE    = function(d){ return( sqrt(calcMSE(d)) ); }
calcESTDEV    = function(d){ return( calcStdDev( calcME(d) * d$total, calcMSE(d) * d$total, d$total) ); }
calcBCMSE    = function(d){ return( calcMSE(d) - (d$fbar - d$obar)^2 ); }
calcBCRMSE    = function(d){ return( sqrt(calcBCMSE(d)) ); }

# SSVAR stat calculations
calcSSVAR_FBAR    = function(d){ return( calcFBAR(d) ); }
calcSSVAR_OBAR    = function(d){ return( calcOBAR(d) ); }
calcSSVAR_FSTDEV    = function(d){ return( calcFSTDEV(d) ); }
calcSSVAR_OSTDEV    = function(d){ calcOSTDEV(d ); }
calcSSVAR_FOBAR    = function(d){ return( calcFOBAR(d) ); }
calcSSVAR_FFBAR    = function(d){ return( calcFFBAR(d) ); }
calcSSVAR_OOBAR    = function(d){ return( calcOOBAR(d) ); }
calcSSVAR_MBIAS    = function(d){ return( calcMBIAS(d) ); }
calcSSVAR_PR_CORR    = function(d){ return( calcPR_CORR(d) ); }

calcSSVAR_ANOM_CORR    = function(d){ return( calcANOM_CORR(d) ); }
calcSSVAR_ME      = function(d){ return( calcME(d) ); }
calcSSVAR_ME2      = function(d){ return( calcME2(d) ); }
calcSSVAR_MSE      = function(d){ return( calcMSE(d) ); }
calcSSVAR_MSESS      = function(d){ return( calcMSESS(d) ); }
calcSSVAR_RMSE    = function(d){ return( calcRMSE(d) ); }
calcSSVAR_ESTDEV    = function(d){ return( calcESTDEV(d) ); }
calcSSVAR_BCMSE    = function(d){ return( calcBCMSE(d)); }
calcSSVAR_BCRMSE    = function(d){ return( calcBCRMSE(d) ); }

calcSSVAR_TOTAL   = function(d){
  if(is.na(d$total_tital) ){
    return (NA);
  }
  return( d$total_tital  );
}

# CTS stat calculations
calcCTS_TOTAL   = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total  );
}

# CTC stat calculations

calcCTC_TOTAL   = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total  );
}

calcCTC_FY_OY   = function(d){
  if(is.na(d$fy_oy) ){
    return (NA);
  }
  return( d$fy_oy  );
}

calcCTC_FY_ON   = function(d){
  if(is.na(d$fy_on) ){
    return (NA);
  }
  return( d$fy_on  );
}

calcCTC_FN_OY   = function(d){
  if(is.na(d$fn_oy) ){
    return (NA);
  }
  return( d$fn_oy  );
}

calcCTC_FN_ON   = function(d){
  if(is.na(d$fn_on) ){
    return (NA);
  }
  return( d$fn_on  );
}

calcCTC_OY   = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) ){
    return (NA);
  }
  return( d$fy_oy +  d$fn_oy );
}

calcCTC_ON   = function(d){
  if(is.na(d$fy_on) || is.na(d$fn_on) ){
    return (NA);
  }
  return( d$fy_on + d$fn_on );
}

calcCTC_FY   = function(d){
  if(is.na(d$fy_on) || is.na(d$fy_oy) ){
    return (NA);
  }
  return( d$fy_on + d$fy_oy );
}

calcCTC_FN   = function(d){
  if(is.na(d$fn_on) || is.na(d$fn_oy) ){
    return (NA);
  }
  return( d$fn_on + d$fn_oy );
}

calcBASER    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total )                      {
    return (NA);
  } else {
    return( (d$fy_oy + d$fn_oy) / d$total );
  }
}

calcLODDS = function(d){
  if(is.na(d$fy_oy) || is.na(d$fy_on) || is.na(d$fn_oy) || is.na(d$fn_on)){
    return (NA);
  }
  if(d$fy_oy == 0 || d$fy_on ==0  || d$fn_oy==0 || d$fn_on==0){
    return (NA);
  }
  v = log( d$fy_oy()) + log(d$fn_on()) - log(d$fy_on()) - log(d$fn_oy());
  return (v)

}
pod_yes = function(d){
  num = d$fy_oy
  den = (d$fy_oy + d$fn_oy)
  if (den == 0){
    return (NA);
  }
  return (num/den)
}
pod_no = function (d){
  num = d$fn_on
  den = (d$fn_on + d$fy_on)
  if (den == 0){
    return (NA);
  }
  return(num/den)
}

pofd = function (d){
  p_no = pod_no(d);

  if(is.na(p_no)){
    return(NA)
  }
  return (1-p_no)
}




calcSEDI = function(d){
  f = d$fy_on / (d$fy_on + d$fn_on);
  h = pod_yes(d);

  if(is.na(f) || is.na(h) || f == 0.0  || h == 0.0  || f== 1.0  || h == 1.0) {
    return(NA)
  }

  num = (log(f) - log(h) - log(1 - f) + log(1 - h));
  den = (log(f) + log(h) + log(1 - f) + log(1 - h));
  if(den == 0.0){
    return(NA)
  }
  return (num/den);
}

calcSEDS = function(d){
  if(d$fy_oy == 0 || d$total == 0 || d$fy_oy + d$fn_oy == 0 || d$fy_oy + d$fy_on == 0) {
    return(NA)
  }

  num = log((d$fy_oy + d$fy_on) / d$total) + log( (d$fy_oy + d$fn_oy) / d$total);
  den = log( d$fy_oy / d$total);

  if(den == 0.0) {
    return(NA)
  }
  return(num / den - 1.0);
}

calcEDI = function(d){
  f = d$fy_on / (d$fy_on + d$fn_on);
  h = pod_yes(d);

  if(is.na(f) || is.na(h) || f == 0.0 || h == 0.0) {
    return(NA)
  }
  num = log(f) - log(h);
  den = log(f) + log(h);
  if(den == 0.0){
    return(NA)
  }
  return(num/den);
}

calcEDS = function(d){
  if(d$fy_oy == 0 || d$fy_oy + d$fn_oy == 0 || d$total == 0) {
    return(NA)
  }

  num = log( (d$fy_oy + d$fn_oy) / d$total);
  den = log( d$fy_oy / d$total);

  if(den == 0.0) {
     return  ( NA )
  }
  return( 2.0 * num / den - 1.0 );
}


calcORSS = function(d){

  num = d$fy_oy * d$fn_on - d$fy_on * d$fn_oy
  den = d$fy_oy * d$fn_on + d$fy_on * d$fn_oy
  if(den == 0){
    return (NA)
  }
  return(num/den)
}

calcACC      = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_on) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total )                      {
    return (NA); } else {
    return( (d$fy_oy + d$fn_on) / d$total );
  }
}
calcFBIAS    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) ){
    return (NA);
  }
  if( 0 == (d$fy_oy + d$fn_oy) )          {
    return (NA); } else {
    return( (d$fy_oy + d$fy_on) / (d$fy_oy + d$fn_oy) );
  }
}
calcFMEAN    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total )
  { return (NA); } else {
    return( (d$fy_oy + d$fy_on) / d$total );
  }
}
calcPODY    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == (d$fy_oy + d$fn_oy) ){
    return (NA);
  } else {
    return( d$fy_oy / (d$fy_oy + d$fn_oy) );
  }
}
calcPOFD    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy)  ){
    return (NA);
  }
  if( 0 == (d$fy_on + d$fn_on) )          {
    return (NA);
  } else {
    return( d$fy_on / (d$fy_on + d$fn_on) );
  }
}
calcPODN    = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy)  ){
    return (NA);
  }
  if( 0 == (d$fy_on + d$fn_on) )          {
    return (NA);
  } else {
    return( d$fn_on / (d$fy_on + d$fn_on) );
  }
}
calcFAR      = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy)  ){
    return (NA);
  }
  if( 0 == (d$fy_oy + d$fy_on) ){
    return (NA);
  } else {
    return( d$fy_on / (d$fy_oy + d$fy_on) );
  }             }
calcCSI      = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) ){
    return (NA);
  }
  if( 0 == (d$fy_oy + d$fy_on + d$fn_oy) ){
    return (NA);
  } else {
    return( d$fy_oy / (d$fy_oy + d$fy_on + d$fn_oy) );
  }
}
calcGSS = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total ){
    return (NA);
  }
  dblC = ( (d$fy_oy + d$fy_on) / d$total ) * (d$fy_oy + d$fn_oy);
  gss = ( (d$fy_oy - dblC) / (d$fy_oy + d$fy_on + d$fn_oy - dblC) )
  return( round(gss, digits=5) );
}
calcHK = function(d){
  if( is.na(calcPODY(d)) || is.na(calcPOFD(d)) ){
    return (NA); } else { return( calcPODY(d) - calcPOFD(d) );
  }
}
calcHSS = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total ){ return (NA); }
  dblC = ( ((d$fy_oy + d$fy_on) / d$total) *(d$fy_oy + d$fn_oy)  +  ((d$fn_oy + d$fn_on) / d$total) * (d$fy_on + d$fn_on)  ) ;
  hss = ( (d$fy_oy + d$fn_on - dblC) / (d$total - dblC) );
  return( round(hss, digits=5) );

}
calcODDS = function(d){
  if( is.na(calcPODY(d)) || is.na(calcPOFD(d)) ){ return (NA); }
  dblPOD = calcPODY(d);
  dblPOFD = calcPOFD(d);
  return( (dblPOD * (1 - dblPOFD)) / (dblPOFD * (1 - dblPOD)) );
}

calcLODDS = function(d){
  if(is.na(d$fy_oy) || is.na(d$fy_on) || is.na(d$fn_oy) || is.na(d$fn_on)) {
    return (NA);
  }

  if(d$fy_oy == 0 || d$fy_on == 0 || d$fn_oy == 0 || d$fn_on == 0) {
    return (NA);
  }

  v= log( d$fy_oy) + log(d$fn_on) - log(d$fy_on) - log(d$fn_oy);
  return( round(v, digits=5) );
}

# BAGSS Reference:
# Bias Adjusted Precipitation Threat Scores
# F. Mesinger, Adv. Geosci., 16, 137-142, 2008
calcBAGSS = function(d){
  if(is.na(d$fy_oy) || is.na(d$fn_oy) || is.na(d$total) ){
    return (NA);
  }
  if( 0 == d$total || d$fn_oy == 0 || d$fy_on == 0){ return (NA); }
  dblF  = d$fy_oy + d$fy_on;
  dblO  = d$fy_oy + d$fn_oy;
  dblLf = log(dblO / d$fn_oy);
  dblHa = tryCatch({
    dblO - (d$fy_on / dblLf) * lambert_W0(dblO / d$fy_on * dblLf);
  }, warning = function(w) {
    return (NA)
  }, error = function(e) {
    return (NA)
  });
  return( (dblHa - (dblO^2 / d$total)) / (2*dblO - dblHa - (dblO^2 / d$total)) );
}

calcECLV = function(d){
  # Build list of X-axis points between 0 and 1
  CL_PTS = seq(clStep, 1-clStep, clStep)
  ECLV = value(c(d$fy_oy, d$fy_on, d$fn_oy, d$fn_on), cl = CL_PTS);
  #find indexses of common steps
  listInd =ECLV$cl %in% CL_PTS;
  V = ECLV$V[listInd];
  return (V);
}


# NBR_CNT "calculations"
calcNBR_FBS = function(d){ return ( d$fbs ); }
calcNBR_FSS = function(d){ return ( d$fss ); }
calcNBR_AFSS = function(d){ return ( d$afss ); }
calcNBR_UFSS = function(d){ return ( d$ufss ); }
calcNBR_F_RATE = function(d){ return ( d$f_rate ); }
calcNBR_O_RATE = function(d){ return ( d$o_rate ); }

#VL1L2 "calculations"
calcVCNT_FS_RMS = function(d){ return ( sqrt(d$uvffbar) ); }
calcVCNT_OS_RMS = function(d){ return ( sqrt(d$uvoobar) ); }

calcVL1L2_TOTAL = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total) ;
}

calcVL1L2_WIND_DIFF = function(d){
  bias = d$f_speed_bar - d$o_speed_bar;
  if ( is.na(bias) ){
    return (NA);
  }
  return ( round(bias, digits=5)) ;
}
calcVL1L2_BIAS = function(d){
  bias = sqrt(d$uvffbar) - sqrt(d$uvoobar);
  return ( round(bias, digits=5)) ;
}

calcVL1L2_FVAR = function(d){
  fvar = d$uvffbar - d$f_speed_bar^2 ;
  return ( round(fvar, digits=5)) ;
}
calcVL1L2_OVAR = function(d){
  ovar =  d$uvoobar - d$o_speed_bar^2 ;
  return ( round(ovar, digits=5)) ;
}

calcVL1L2_FSPD = function(d){
  fspd = calc_spd( d$ufbar, d$vfbar );
  return ( round(fspd, digits=5)) ;
}

calcVL1L2_OSPD = function(d){
  ospd = calc_spd( d$uobar, d$vobar );
  return ( round(ospd, digits=5)) ;
}

calcVL1L2_SPEED_ERR = function(d){
  speed_bias = calcVL1L2_FSPD(d) - calcVL1L2_OSPD(d);
  return ( round(speed_bias, digits=5)) ;
}

calcVL1L2_RMSVE = function(d){
  rmsve = sqrt ( calcVL1L2_MSVE(d) );
  return ( round(rmsve, digits=5) );
}

calcVL1L2_MSVE = function(d){
  msve = d$uvffbar - 2.0*d$uvfobar + d$uvoobar;
  if (msve < 0 ){
    return (NA);
  }
  return ( round(msve, digits=5) );
}

calc_spd = function(u,v){
  if( is.na(u) || is.na(v) ) {
    return (NA);
  }else{
    return ( sqrt( u^2 + v^2 ) );
  }
}

calc_dir = function(u,v){
  tolerance = 1e-5;
  if( abs(u-0)< tolerance && abs(v-0)< tolerance ){
    return (NA);
  } else {
    dir= atan2d( u, v )
    #convert to [0,360]
    dir = dir - 360 * floor( dir/360 )
    return ( dir ) ;
  }
}


#VCNT "calculations"
calcVCNT_FBAR = function(d){
  fbar = d$f_speed_bar;
  return ( round(fbar, digits=5)) ;
}

calcVCNT_OBAR = function(d){
  obar = d$o_speed_bar;
  return ( round(obar, digits=5)) ;
}

calcVCNT_MSVE = function(d){
  mse = d$uvffbar -2 * d$uvfobar + d$uvoobar;
  if ( is.na(mse) || mse < 0 ){
    return (NA);
  }
  return ( round(mse, digits=5)) ;
}

calcVCNT_RMSVE = function(d){
  rmse = sqrt( calcVCNT_MSVE(d ) );
  return ( round(rmse, digits=5)) ;
}

calcVCNT_FSTDEV = function(d){
  fstdev = sqrt( calcVL1L2_FVAR (d));
  return ( round(fstdev, digits=5)) ;
}

calcVCNT_OSTDEV = function(d){
  ostdev = sqrt( calcVL1L2_OVAR (d));
  return ( round(ostdev, digits=5)) ;
}

calcVCNT_FBAR_SPEED = function(d){
  fspd = calc_spd( d$ufbar, d$vfbar );
  return ( round(fspd, digits=5)) ;
}

calcVCNT_OBAR_SPEED = function(d){
  ospd = calc_spd( d$uobar, d$vobar );
  return ( round(ospd, digits=5)) ;
}

calcVCNT_FDIR = function(d){
  fdir = calc_dir(-d$ufbar, -d$vfbar);

  if(is.na(fdir)){
    return (NA);
  } else {
    return ( round(fdir, digits=5)) ;
  }
}

calcVCNT_ODIR = function(d){
  odir = calc_dir(-d$uobar, -d$vobar);

  if(is.na(odir)){
    return (NA);
  } else {
    return ( round(odir, digits=5)) ;
  }
}

calcVCNT_VDIFF_SPEED = function(d){
  vdiff_spd = calc_spd( d$ufbar-d$uobar, d$vfbar-d$vobar );
  return ( round(vdiff_spd, digits=5)) ;
}

calcVCNT_VDIFF_DIR = function(d){
  vdiff_dir = calc_dir( -(d$ufbar-d$uobar), -(d$vfbar-d$vobar) );
  return ( round(vdiff_dir, digits=5)) ;
}

calcVCNT_SPEED_ERR = function(d){
  speed_bias = calcVCNT_FBAR_SPEED (d) - calcVCNT_OBAR_SPEED (d);
  return ( round(speed_bias, digits=5)) ;
}

calcVCNT_SPEED_ABSERR = function(d){
  spd_abserr = abs( calcVCNT_SPEED_ERR(d) );
  return ( spd_abserr ) ;
}

calcVCNT_DIR_ABSERR = function(d){
  ang_btw = abs( calcVCNT_DIR_ERR(d) );
  return ( ang_btw ) ;
}


calcVCNT_DIR_ERR = function(d){
  f_len = calcVCNT_FBAR_SPEED (d);
  if( !is.na(f_len) && f_len != 0){
    uf = d$ufbar / f_len;
    vf = d$vfbar / f_len;

    o_len = calcVCNT_OBAR_SPEED (d);
    uo = d$uobar / o_len;
    vo = d$vobar / o_len;

    a = vf*uo - uf*vo;
    b = uf*uo + vf*vo;
    dir_err = calc_dir(a, b);
  } else{
    dir_err = NA;
  }

  if(is.na(dir_err)){
    return (NA);
  } else {
    return ( round(dir_err, digits=5)) ;
  }
}

#VAL1L2 "calculations"
calcVAL1L2_ANOM_CORR = function(d){
  return ( calc_wind_corr(d$total, d$ufabar, d$vfabar, d$uoabar, d$voabar, d$uvfoabar, d$uvffabar, d$uvooabar) );
}
calcVAL1L2_TOTAL = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total) ;
}

calc_wind_corr = function( total, uf, vf, uo, vo, uvfo, uvff, uvoo ){
  if(  is.na(uvfo) || is.na(uf) || is.na(uo) || is.na(vf) || is.na(vo) ){
    return(NA);
  } else{
    corr = ( uvfo - uf * uo - vf * vo ) /
    ( sqrt( uvff - uf * uf - vf * vf ) * sqrt( uvoo - uo * uo - vo * vo) )
    return ( round(corr, digits=5)) ;
  }
}


calcPSTD_BRIER = function(d){
  return ( d$reliability - d$resolution + d$uncertainty );
}
calcPSTD_BSS_SMPL = function(d){
  return ( ( d$resolution - d$reliability ) / d$uncertainty );
}
calcPSTD_BASER = function(d){
  return ( d$baser );
}
calcPSTD_RELIABILITY = function(d){
  return ( d$reliability );
}
calcPSTD_RESOLUTION = function(d){
  return ( d$resolution );
}
calcPSTD_ROC_AUC = function(d){
  return ( d$roc_auc );
}

calcPSTD_UNCERTAINTY = function(d){
  return ( d$uncertainty );
}


calcPSTD_CALIBRATION = function(d){
  return ( d$calibration );
}

calcPSTD_NI = function(d){
  return ( d$n_i );
}


calcFGBAR = function(d){
  return  (d$fgbar);
}

calcOGBAR = function(d){
  return  (d$ogbar);
}

calcMGBAR = function(d){
  return  (d$mgbar);
}

calcEGBAR = function(d){
  return  (d$egbar);
}

calcS1 = function(d){
  if(is.na(d$egbar) || is.na(d$mgbar) || d$mgbar == 0)
  return ( NA );
  s1 = 100 * d$egbar / d$mgbar ;
  return ( round(s1, digits=5) ) ;
}

calcS1_OG = function(d){
  if(is.na(d$egbar) || is.na(d$ogbar) || d$ogbar == 0)
  return ( NA );
  s1_og = 100 * d$egbar / d$ogbar ;
  return ( round(s1_og, digits=5) ) ;
}

calcFGOG_RATIO = function(d){
  if(is.na(d$fgbar) || is.na(d$ogbar) || d$ogbar == 0)
  return ( NA );
  fgog_ratio = 100 * d$fgbar / d$ogbar ;
  return ( round(fgog_ratio, digits=5) ) ;
}

calcSSVAR_Spread = function(d){
  if( length( d$varmean ) < 1 ){ return (NA); }
  return( sqrt(weighted.mean(d$varmean, d$binn)) );
}

#ECNT "calculations"
calcECNT_CRPSS = function(d){
  if( is.na(d$crps_climo) || d$crps_climo == 0 || is.na(d$crps) )
  return ( NA );
  crpss = ( d$crps_climo - d$crps ) / d$crps_climo;
  return ( round(crpss, digits=5) )
}

calcECNT_CRPS = function(d){
  if( is.na(d$crps) )
    return ( NA );
  return ( round(d$crps, digits=5) )
}

calcECNT_CRPSCL = function(d){
  if( is.na(d$crpscl) )
    return ( NA );
  return ( round(d$crpscl, digits=5) )
}

calcECNT_CRPS_EMP = function(d){
  if( is.na(d$crps_emp) )
    return ( NA );
  return ( round(d$crpscl_emp, digits=5) )
}

calcECNT_CRPSCL_EMP = function(d){
  if( is.na(d$crpscl_emp) )
    return ( NA );
  return ( round(d$crpscl_emp, digits=5) )
}

calcECNT_CRPSS_EMP = function(d){
  if( is.na(d$crps_climo) || d$crps_climo == 0 || is.na(d$crps) )
    return ( NA );
  crpss = ( d$crps_climo - d$crps ) / d$crps_climo;
  return ( round(crpss, digits=5) )
}



calcECNT_RMSE = function(d){
  if( is.na(d$mse) )
  return ( NA );
  rmse = sqrt(d$mse);
  return ( round(rmse, digits=5) )
}

calcECNT_RMSE_OERR = function(d){
  if( is.na(d$mse_oerr) )
  return ( NA );
  rmse_oerr = sqrt(d$mse_oerr);
  return ( round(rmse_oerr, digits=5) )
}

calcECNT_ME = function(d){
  if( is.na(d$me) )
  return ( NA );
  return ( round(d$me, digits=5) )
}


calcECNT_IGN = function(d){
  if( is.na(d$ign) )
  return ( NA );
  return ( round(d$ign, digits=5) )
}
calcECNT_SPREAD = function(d){
  if( is.na(d$spread) )
  return ( NA );
  return ( round(d$spread, digits=5) )
}
calcECNT_ME_OERR = function(d){
  if( is.na(d$me_oerr) )
  return ( NA );
  return ( round(d$me_oerr, digits=5) )
}
calcECNT_SPREAD_OERR = function(d){
  if( is.na(d$spread_oerr) )
  return ( NA );
  return ( round(d$spread_oerr, digits=5) )
}
calcECNT_SPREAD_OERR = function(d){
  if( is.na(d$spread_oerr) )
  return ( NA );
  return ( round(d$spread_oerr, digits=5) )
}

calcECNT_SPREAD_PLUS_OERR = function(d){
  if( is.na(d$spread_plus_oerr) )
  return ( NA );
  return ( round(d$spread_plus_oerr, digits=5) )
}

calcECNT_TOTAL = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total )
}



# NBR_CTC stat calculations

calcNBR_CTC_TOTAL    = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total );
}

calcNBR_BASER    = function(d){
  return( calcBASER(d) );
}
calcNBR_ACC      = function(d){
  return( calcACC(d) );
}
calcNBR_FBIAS    = function(d){
  return( calcFBIAS(d) );
}
calcNBR_FMEAN    = function(d){
  return( calcFMEAN(d) );
}
calcNBR_PODY    = function(d){
  return( calcPODY(d) );
}
calcNBR_POFD    = function(d){
  return( calcPOFD(d) );
}
calcNBR_PODN    = function(d){
  return( calcPODN(d) );
}
calcNBR_FAR      = function(d){
  return( calcFAR(d) );
}
calcNBR_CSI      = function(d){
  return( calcCSI(d) );
}
calcNBR_GSS = function(d){
  return( calcGSS(d) );
}
calcNBR_HK = function(d){
  return( calcHK(d) );
}
calcNBR_HSS = function(d){
  return( calcHSS(d) );
}
calcNBR_ODDS = function(d){
  return( calcODDS(d) );
}

calcNBR_CNT_TOTAL = function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return( d$total );
}

# RPS stat calculations
calcRPS = function(d){
  return  (d$rps);
}

calcRPS_COMP = function(d){
  return  (d$rps_comp);
}

calcRPSS = function(d){
  #RPSS = 1.0 - ( RPS / RPS_CLIMO )
  if( is.na(d$rps_climo) || d$rps_climo == 0  )
      return ( NA );
  rpss = 1 - d$rps / d$rps_climo
  return ( round(rpss, digits=5) )
}

calcRPS_TOTAL= function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total )
}

# PCT stat calculations
calcPCT_TOTAL= function(d){
  if(is.na(d$total) ){
    return (NA);
  }
  return ( d$total )
}






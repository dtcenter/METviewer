## Function to compute the Standard Error of an uncorrelated time series.
##
## Remove the correlated portion of a time series, using a first order auto-correlation coefficient
## to help compute an inflated variance factor for the uncorrelated portion of the time series.
##
## method = "ML" or "CSS".
##
## Originator:  Eric Gilleland and Andrew Loughe, 08 JUL 2008
##

Compute_STDerr_from_median <- function ( data, method )
{

   RATIO_flag = 0;     ## Problem computing the vif ratio?

   if ( IQR(data) > 0.0 & length(data) > 2 ) {

      ## Compute the first order auto-correlation coefficient
      ## using a vector that is the same size as "data", but represents 
      ## represents excusions from the median of the data.

      ## Use excursions from the median to compute the first order auto-correlation coefficient.
      data_excursions = c();
      for( i in 1:length(data) ) { data_excursions <- c( data_excursions, as.numeric( data[i]>=median(data) ) ); }

      data.arima <- arima( data_excursions, order=c(1,0,0), method=method )

## Use higher order arima coefficients.
##       data.arima <- arima( data_excursions, order=c(5,0,0), method=method )
##      for (i in 1:5) {
##           p <- coef(data.arima)[i];
##           z <- 2.0 * sqrt(diag ( data.arim$var.coef ))[i]
##           if ( ((abs(p)+z) > 0. & (abs(p)-z) < 0.) ) {
##              break
##           }
##      }
##      ORD <- i-1;
##      if ( ORD < 1 ) { variance_inflation_factor = 1.0; }
      

      ## If the AR1 coefficient is out-of-bounds, try a different ARIMA method.
      ## What about higher-order coefficients?  Should we fit a higher-order function
      ## if those coefficients are large?
      if ( coef(data.arima)[1] < 0.3 | coef(data.arima)[1] > 1.0 ) { 
         if ( method == 'ML' ) { method = "CSS"; } else { method = "ML"; }
         data.arima <- arima( data_excursions, order=c(1,0,0), method=method )
      }

      ## Compute an variance inflation factor (having removed that portion of the time series that was correlated).
      RATIO = ( 1 + coef(data.arima)[1] ) / ( 1 - coef(data.arima)[1] )

      ## Check for a zero RATIO, that will then be operated on by SQRT.
      ## If necessary, try a different arima method, or just set RATIO to zero.
      if ( RATIO < 0.0 ) {
         if ( method == 'ML' ) { method = "CSS"; } else { method = "ML"; }
         data.arima <- arima( data_excursions, order=c(1,0,0), method=method )
         RATIO = ( 1 + coef(data.arima)[1] ) / ( 1 - coef(data.arima)[1] )
      }
      if ( RATIO < 0.0 ) { RATIO = 1.0; RATIO_flag = 1; }

      variance_inflation_factor = sqrt( RATIO );

      ## If the AR1 coefficient is less than 0.3, then don't even use a vif!  Set vif = 1.0
      if ( coef(data.arima)[1] < 0.3 | coef(data.arima)[1] > 1.0  ) { variance_inflation_factor = 1.0; }

      ## Compute the Standard Error using the variance inflation factor.
      STDerr_data <- variance_inflation_factor * ( IQR(data) * sqrt(pi/2.) ) / ( 1.349 * sqrt( length(data) ) )
      ## STDerr_data <- ( 1.58 * IQR(data) / sqrt( length(data) ) );

      AR1 = coef(data.arima)[1];

   } else {

      STDerr_data = 0.0;
      AR1 = 0.0;

   }

   return ( c(STDerr_data, RATIO_flag, AR1, length(data)) );

}



Compute_STDerr_from_mean <- function ( data, method )
{

   RATIO_flag = 0;     ## Problem computing the vif ratio?

   if ( var(data) > 0.0 & length(data) > 2 ) {

      ## Compute the first order auto-correlation coefficient.
      data.arima <- arima( data, order=c(1,0,0), method=method )

      ## If the AR1 coefficient is out-of-bounds, try a different ARIMA method.
      ## What about higher-order coefficients?  Should we fit a higher-order function
      ## if those coefficients are large?
      if ( coef(data.arima)[1] < 0.3 | coef(data.arima)[1] > 1.0 ) { 
         if ( method == 'ML' ) { method = "CSS"; } else { method = "ML"; }
         data.arima <- arima( data, order=c(1,0,0), method=method )
      }

      ## Compute a variance inflation factor (having removed that portion of the time series that was correlated).
      RATIO = ( 1 + coef(data.arima)[1] ) / ( 1 - coef(data.arima)[1] )

      ## Check for a zero RATIO, that will then be operated on by SQRT.
      ## If necessary, try a different arima method, or just set RATIO to one.
      if ( RATIO < 0.0 ) {
         if ( method == 'ML' ) { method = "CSS"; } else { method = "ML"; }
         data.arima <- arima( data, order=c(1,0,0), method=method )
         RATIO = ( 1 + coef(data.arima)[1] ) / ( 1 - coef(data.arima)[1] )
      }
      if ( RATIO < 0.0 ) { RATIO = 1.0; RATIO_flag = 1; }

      variance_inflation_factor = sqrt( RATIO );

      ## If the AR1 coefficient is less than 0.3, then don't even use a vif!  Set vif = 1.0
      if ( coef(data.arima)[1] < 0.3 | coef(data.arima)[1] > 1.0  ) { variance_inflation_factor = 1.0; }

      ## Compute the Standard Error using the variance inflation factor.
      STDerr_data <- variance_inflation_factor * sqrt( var(data) ) / sqrt( length(data) );

      AR1 = coef(data.arima)[1];

   } else {

      STDerr_data = 0.0;
      AR1 = 0.0;
   }

   return ( c(STDerr_data, RATIO_flag, AR1, length(data)) );

}



Output_to_DB <- function ( EXISTING_BW_DBtable, data, STDerr, Zval, fhr, season, level, param, grid, init, stat, stat_type, mycon )
{

   query0 <- paste( "select Estim from ", EXISTING_BW_DBtable, sep="");

   ## query1 <- paste( "update ", EXISTING_BW_DBtable, " set Estim_new=ROUND(", mean(data), ",18) , StErr_new=ROUND(", STDerr[1], ",18) , Z_Val_new=ROUND(", Zval, ",18), vif_flag='", STDerr[2], "'", sep="");


   if ( stat_type == 'mean' ) {
      query1 <- paste( "update ", EXISTING_BW_DBtable, " set Estim_new=", mean(data), ", StErr_new=", STDerr[1], ", Z_Val_new=", Zval, ", vif_flag='", STDerr[2], "'", ", AR1_coef=", STDerr[3], ", N=", STDerr[4], sep="");
   }

   if ( stat_type == 'median' ) {
      query1 <- paste( "update ", EXISTING_BW_DBtable, " set Estim_new=", median(data), ", StErr_new=", STDerr[1], ", Z_Val_new=", Zval, ", vif_flag='", STDerr[2], "'", ", AR1_coef=", STDerr[3], ", N=", STDerr[4], sep="");
   }

   query2 <- paste( " where Forec=", fhr, " and TimePer='", season, "' and level='", level, "' and param='", param, "' and domain='", grid, sep="");

   ## query3 <- paste( query0, query2,  "' and init=", init, " and stat='", stat, "' limit 1", sep="");         

   query4 <- paste( query1, query2,  "' and init=", init, " and stat='", stat, "' limit 1", sep="");

   ## rs     <- dbSendQuery(mycon, query3) ; data_rs <- fetch(rs, n=-1);
   ## if ( length(data_rs) > 0 ) { rs <- dbSendQuery(mycon, query4); } else { print ('No corresponding database record exists!!!'); }

   rs <- dbSendQuery(mycon, query4);

   return ( 1 );

}




Output_to_NEW_DB_table <- function ( DBtableOUT, Estim, STDerr, Zval, fhr, season, level, param, grid, init, stat, stat_type, mycon )
{

   ## Translate this variable name.
   init2 <- init;
   if ( init ==  "'0'" ) { init2 <-  '0'; }
   if ( init == "'12'" ) { init2 <- '12'; }

   ## Database insert statement.
   insert_query <- paste( 'INSERT INTO ', DBtableOUT, ' (season,stat_type,grid,param,init,fhr,level,stat,Estim,STDerr,Zval) ', 
'VALUES(', "'",season,"',", "'",stat_type,"',", "'",grid,"',", "'",param,"',", "'",init2,"',", "'",fhr,"',", "'",level,"',", "'",stat,"',", Estim,",", STDerr[1],",", Zval,")", sep='');

   ## print (insert_query);

   rs <- dbSendQuery( mycon, insert_query );

   return ( 1 );

}

Output_to_NEW_DB_table2 <- function ( DBtableOUT, Estim, STDerr, Zval, fhr, season, level, param, physic, grid, init, stat, stat_type, mycon )
{

   ## Translate this variable name.
   init2 <- init;
   if ( init ==  "'0'" ) { init2 <-  '0'; }
   if ( init == "'12'" ) { init2 <- '12'; }

   ## Database insert statement.
   insert_query <- paste( 'INSERT INTO ', DBtableOUT, ' (season,stat_type,physics,grid,param,init,fhr,level,stat,Estim,STDerr,Zval) ', 
'VALUES(', "'",season,"',", "'",stat_type,"',", "'",physic,"',", "'",grid,"',", "'",param,"',", "'",init2,"',", "'",fhr,"',", "'",level,"',", "'",stat,"',", Estim,",", STDerr[1],",", Zval,")", sep='');

   ## print (insert_query);

   rs <- dbSendQuery( mycon, insert_query );

   return ( 1 );

}



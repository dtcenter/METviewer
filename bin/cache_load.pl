#!/usr/bin/perl

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
# cache_load.pl
#
# This utility steps through a list of database and requests a list of fcst_var values 
# from each.  For each fcst_var, a list of statistics is requested.  The desired effect
# is the caching of the list of statistics for each fcst_var for each database.  The
# cache for each database can be cleared using a command line parameter.  A subset of
# all databases on a particular server can also be specified using a command line
# parameter.
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

use LWP::UserAgent;
use HTTP::Request::Common;
use POSIX qw(floor);
use Getopt::Long;

sub usage {
	if( -1 < $#ARGV ){ print "$_\n"; }
	print "
usage: $0 [-h] [-v] [-s {server}] [-c {db_name(s)}] [-d {db_name(s)}]

  where:
         -h: (optional) prints this message and exits         
         -v: (optional) verbose output, default no
         -s: (optional) use the web service at the host name {server}, defaults to orval         
         -c: (optional) clears the cache for the databases {db_name(s)} prior to loading, default no
         -d: (optional) load the cache for the database {db_name(s)}, defaults to all databases
                  
         note: {db_name(s)} should be a space-delimited list
";
	exit 0;
}

#  parse the input options
chomp($host = qx/hostname/);
$verbose = 0;
@clear = ();
@db = ();
GetOptions(
	'help'        => \$help,
	'verbose'     => \$verbose,
	'server=s'    => \$host, 
	'clears=s{,}' => \@clear,
	'db=s{,}'     => \@db
)
or usage("invalid input option");

if( $help ){ usage(); }

if( $verbose ){ 
	print "host: $host\n";
	if( -1 < $#clear ){ print "clear: @clear\n"; }
	if( -1 < $#db )   { print "db: @db\n\n"; } 
}

$url = "http://$host:8080/metviewer/servlet";

$intStartLoad = time();

# get the list of supported databases, if none are specified
if( 0 > $#db ){
	$xmlDBList = getRequest("<request><list_db/></request>");
	$xmlDBList =~ s/<list_db><val>(.*)<\/val><\/list_db>/$1/;
	@db = split(/<\/val><val>/, $xmlDBList);
}

# handle all FCST_VARs for each database
foreach $db (@db){

	if( $verbose ){ print "$db"; }
	
	# clear the database cache, if requested
	@clearDB = grep(/$db/, @clear);
	if( 0 <= $#clearDB ){
	 	if( $verbose ){ print "\n  clearing caches...\n"; }
		$xmlClearVal = getRequest("<request><db_con>$db</db_con><list_val_clear_cache/></request>");
		if( $verbose ){ print "    $xmlClearVal\n"; }
		$xmlClearStat = getRequest("<request><db_con>$db</db_con><list_stat_clear_cache/></request>");		
		if( $verbose ){ print "    $xmlClearStat\n  caches clear\n"; }
	} elsif( $verbose ){ print " -"; }
	
	# get the list of FCST_VARs for the current database	
	$intStart = time();
	$xmlFcstVar = getRequest("<request><db_con>$db</db_con><list_val><id>0</id><stat_field>FCST_VAR</stat_field></list_val></request>");
	$xmlFcstVar =~ s/<list_val><id>0<\/id><val>(.*)<\/val><\/list_val>/$1/;
	@listFcstVar = split(/<\/val><val>/, $xmlFcstVar);
	if( $verbose ){ print "  retrieved " . ($#listFcstVar + 1) . " FCST_VARs in " . formatTimeInc(time() - $intStart) . "\n"; }
	
	# request stats for each FCST_VAR
	$intNumStat = 0;
	foreach $strFcstVar (@listFcstVar){
		if( $verbose ){ print "  " . padRight($strFcstVar, 16); }		
		$intStart = time();
		$xmlStat = getRequest("<request><db_con>$db</db_con><list_stat><id>0</id><stat_fcst_var>$strFcstVar</stat_fcst_var></list_stat></request>");	
		$xmlStat =~ s/<list_stat><id>0<\/id><val>(.*)<\/val><\/list_stat>/$1/;
		@listStat = split(/<\/val><val>/, $xmlStat);		
		if( $verbose ){ print " - " . padRight($#listStat + 1, 3) . " stats in " . formatTimeInc(time() - $intStart) . "\n"; }
	}	
	if( $verbose ){ print "\n"; }
}

if( $verbose ){ print "Total time: " . formatTimeInc(time() - $intStartLoad) . "\n\n"; }

# getRequest() assumes a single XML string input, which constitutes a request to the
#   web service specified in the global $url.  The request is posted to the service
#   and the response is returned if successful, otherwise the error message from the
#   web server is returned 
sub getRequest {
	$userAgent = LWP::UserAgent->new(agent => "perl post");
	$userAgent->timeout(12000);
	$response = $userAgent->request(POST $url,
									Content_Type => "text/xml",
	  								Content => $_[0]);
	  								
	if( $response->is_success ){
		chomp($ret = $response->content);
		$ret =~ s/[^[:print:]]//g;
	}
	else { $ret = $response->error_as_HTML;  }
	#print "request: $_[0]\nresponse: $ret\n\n";
	return $ret; 
}

# formatTimeInc() assumes that the single positive integer input is a time span in seconds
#   and returns a string containing the span in H:mm:ss format
sub formatTimeInc {
	$run = $_[0];
	$h = floor($run / 3600);
	$run -= ($h * 3600);
	$m = floor($run / 60);
	$run -= ($m * 60);
	$s = $run;
	return "$h:" . (10 > $m? "0" : "") . "$m:" . (10 > $s? "0" : "") . "$s";
}

# padRight() appends spaces to the end of the first input argument string until it reaches
#   the length specified by the second argument integer and returns the result
sub padRight {
	$s = $_[0];	
	while( $_[1] > length($s) ){ $s .= " "; }
	return $s
}

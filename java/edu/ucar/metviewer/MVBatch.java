package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import edu.ucar.metviewer.hmt.*;
//import org.apache.log4j.*;

public class MVBatch extends MVUtil {
	
	//private static final Logger _logger = Logger.getLogger(MVBatch.class);
	//private static final PrintStream _logStream = System.out;
	
	public static boolean _boolSQLOnly			= false;
	
	public static String _strHost				= "kemosabe";
	public static String _strPort				= "3306";	
	public static String _strDatabase			= "metvdb3_hmt";
	public static String _strUser				= "pgoldenb";
	public static String _strPwd				= "pgoldenb";
	
	public static String _strRtmplFolder		= "/home/pgoldenb/apps/verif/metviewer/R_tmpl/";
	public static String _strRworkFolder		= "/d1/pgoldenb/var/hmt/R_work/";
	public static String _strPlotsFolder		= "/d1/pgoldenb/var/hmt/plots/";

	public static String _strInitdateField		= "fcst_init_beg"; //"initdate";
	
	public static boolean _boolProcWait			= true;

	public static final Pattern _patRTmpl		= Pattern.compile("#<(\\w+)>#");
	public static final Pattern _patDateRange	= Pattern.compile("(?i)\\s*between\\s+'([^']+)'\\s+and\\s+'([^']+)'\\s*");
	
	public static final boolean _boolPlot		= true;
	public static boolean _boolSQLSort			= true;
	public static boolean _boolCacheBoot		= true;
	
	public static String[] _list24				= {};
	public static String[] _list06				= {};
	public static String[] _listBase			= {};
	public static String _strBaseDate			= "";
	public static String _strBaseDateDefault	= "2010-02-21 0:00:00";	
	
	public static int _intNumPlots				= 0;
	public static int _intPlotIndex				= 0;
	public static int _intNumPlotsRun			= 0;

	public static boolean _boolTheWorks			= false;
	
	public static boolean _boolWindows			= false;
		
	public static String getUsage(){
		return	"Usage:  mv_batch\n" +
				"          plot_spec_file\n" +
				"          [job_name]\n" +
				"          [-list | -sql]\n" +
				"\n" +
				"          where   \"plot_spec_file\" specifies the XML plot specification document\n" +
				"                  \"job_name\" specifies the name of the job from the plot specification to run\n" +
				"                  \"-list\" indicates that the available plot jobs should be listed and no plots run\n" +
				"                  \"-sql\" indicates that the queries for each plot jobs should be listed and no plots run\n";
	}	
	
	public static void main(String[] argv) {
		System.out.println("----  MVBatch  ----\n");
		Connection con = null;

		//  windows settings
		if( _boolWindows ){
			_strRtmplFolder = "c:/src/apps/verif/metviewer/R_tmpl/";
			_strRworkFolder = "c:/src/metv/R_work/";
			_strPlotsFolder = "c:/src/metv/plots/";
			_boolProcWait = false;
		}
		
		try{
		
			MVPlotJob[] jobs = {};
		
			//  if no input file is present, bail
			if( 1 > argv.length ){
				System.out.println(getUsage() + "\n----  MVBatch Done  ----");
				try{ if( con != null )	con.close(); }catch(SQLException e){}
				return;
			}
			
			if( argv[0].equals("-hmt") ){
				
				ArrayList listArgvHMT = new ArrayList();
				listArgvHMT.addAll( Arrays.asList(argv) );
				listArgvHMT.remove(0);
				
				jobs = buildHMTJobs(con, toArray(listArgvHMT));
				
			} else {
				
				//  parse the input file
				String strXMLInput = argv[0];
				System.out.println("input file: " + strXMLInput + "\n");				
				MVPlotJobParser parser = new MVPlotJobParser(strXMLInput, con);
				MVOrderedMap mapJobs = parser.getJobsMap();
				
				//  parse the remaining input arguments
				ArrayList listJobNamesInput = new ArrayList();
				boolean boolList = false;
				if( 1 < argv.length ){
					for(int i=1; i < argv.length; i++){
						if     ( argv[i].equals("-list") ){ boolList = true; }
						else if( argv[i].equals("-sql") ) { _boolSQLOnly = true; }
						else {
							listJobNamesInput.add(argv[i]);
						}
					}
				}
				String[] listJobNames = mapJobs.keyList();
				if( 0 < listJobNamesInput.size() ){
					listJobNames = toArray(listJobNamesInput);
				}
								
				System.out.println( (boolList? "" : "processing ") + listJobNames.length + " jobs:");
				for(int i=0; i < listJobNames.length; i++){
					System.out.println("  " + listJobNames[i]);
				}
				
				//  if only a list of plot jobs is requested, return
				if( boolList ){
					System.out.println("\n----  MVBatch Done  ----");
					try{ if( con != null )	con.close(); }catch(SQLException e){}
					return;
				}
				
				//  if a job name is present, run only that job, otherwise run all jobs
				if( 1 > listJobNames.length ){
					jobs = parser.getJobsList();
				} else {
					ArrayList listJobs = new ArrayList();
					for(int i=0; i < listJobNames.length; i++){
						if( !mapJobs.containsKey(listJobNames[i]) ){
							System.out.println("  **  WARNING: unrecognized job \"" + listJobNames[i] + "\"");
							continue;
						}
						listJobs.add( mapJobs.get(listJobNames[i]) );
					}
					jobs = (MVPlotJob[])listJobs.toArray(new MVPlotJob[]{});
				}
				
				//  get the path information for the job
				if( !parser.getRtmplFolder().equals("") ){ _strRtmplFolder = parser.getRtmplFolder(); }
				if( !parser.getRworkFolder().equals("") ){ _strRworkFolder = parser.getRworkFolder(); }
				if( !parser.getPlotsFolder().equals("") ){ _strPlotsFolder = parser.getPlotsFolder(); }

			}  //  end: else - HMT Code
			
			//  if on windows, change all plot image types to jpeg
			if( _boolWindows ){
				for(int i=0; i < jobs.length; i++){ jobs[i].setPlotType("jpeg"); }
			}
			
			//  calculate the number of plots
			_intNumPlots = 0;
			for(int intJob=0; intJob < jobs.length; intJob++){
				
				//  add a job for each permutation of plot fixed values
				Map.Entry[] listPlotFix = jobs[intJob].getPlotFixVal().getOrderedEntries();
				int intNumJobPlots = 1;
				for(int j=0; j < listPlotFix.length; j++){
					Object objFixVal = listPlotFix[j].getValue();
					if     ( objFixVal instanceof String[] )    { intNumJobPlots *= ((String[])objFixVal).length;     }
					else if( objFixVal instanceof MVOrderedMap ){ intNumJobPlots *= ((MVOrderedMap)objFixVal).size(); }
				}
				
				//  add a job for each permutation of aggregation values
				Map.Entry[] listAgg = jobs[intJob].getAggVal().getOrderedEntries();
				for(int j=0; j < listAgg.length; j++){
					Object objAggVal = listAgg[j].getValue();
					if     ( objAggVal instanceof String[] )    { intNumJobPlots *= ((String[])objAggVal).length;     }
					else if( objAggVal instanceof MVOrderedMap ){ intNumJobPlots *= ((MVOrderedMap)objAggVal).size(); }
				}
								
				//  add a job for each member of mode group
				MVOrderedMap[] listDep = jobs[intJob].getDepGroups();
				for(int j=0; j < listDep.length; j++){
					Object objDep1 = listDep[j].get("dep1");
					if( objDep1 instanceof MVOrderedMap[] ){ intNumJobPlots *= ((MVOrderedMap[])objDep1).length; }
				}

				_intNumPlots += intNumJobPlots;
			}
			java.util.Date dateStart = new java.util.Date();
			System.out.println("Running " + _intNumPlots + " plots\n" + 
							   "Begin time: " + _formatDB.format(dateStart) + "\n");
			
			for(int intJob=0; intJob < jobs.length; intJob++){
				runJob(jobs[intJob]);
			}

			java.util.Date dateEnd = new java.util.Date();
			long intPlotTime = dateEnd.getTime() - dateStart.getTime();
			long intPlotAvg = (0 < _intNumPlots? intPlotTime / (long)_intNumPlots : 0);
			System.out.println("\n" + 
							   padBegin("End time: ") + _formatDB.format(dateEnd) + "\n" +
							   padBegin("Plots run: ") + _intNumPlotsRun + " of " + _intNumPlots + "\n" +
							   padBegin("Total time: ") + formatTimeSpan(intPlotTime) + "\n" +
							   padBegin("Avg plot time: ") + formatTimeSpan(intPlotAvg) + "\n");


		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null )	con.close(); }catch(SQLException e){}
		}

		System.out.println("\n----  MVBatch Done  ----");
	}
	
	/**
	 * Temporary function that uses classes in the edu.ucar.metviewer.hmt package to build jobs for the HMT project.
	 * @param con Database connection
	 * @param argv List of input arguments passed to MVBatch
	 * @return List of plot jobs
	 * @throws Exception
	 */
	public static MVPlotJob[] buildHMTJobs(Connection con, String[] argv) throws Exception{
		
		MVPlotJob[] jobs = {};

		if( 2 > argv.length ){
			System.out.println("usage:\n% java [jvm_args] MVBatch {db_host} {works} [{plot_type} {date1} [date2]...]\n\n----  MVBatch Done  ----");
			try{ if( con != null )	con.close(); }catch(SQLException e){}
			return jobs;
		}
		
		_strHost = argv[0];
		_boolTheWorks = argv[1].equalsIgnoreCase("true");
		
		//  connect to the database
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection("jdbc:mysql://" + _strHost + ":" + _strPort + "/" + _strDatabase, _strUser, _strPwd);
		if( con.isClosed() )	throw new Exception("METViewer error: database connection failed");
		
		System.out.println("connected to " + _strDatabase + "@" + _strHost + "\nthe works: " + _boolTheWorks);


		//  parse the command line input to determine the job
		boolean boolInput = false;
		String strJob = "";
		if( 2 < argv.length ){
			//  the first argument should be the plot type
			strJob = argv[2];
			System.out.println("input: '" + strJob + "'");
			boolInput = true;
			
			//  parse the input folder list, if present
			ArrayList listDates24 = new ArrayList();
			ArrayList listDates06 = new ArrayList();
			ArrayList listDatesBase = new ArrayList();
			for(int i=3; i < argv.length; i++){
				String strFolder = argv[i];
				if( strFolder.matches(".+_24h$") ){ listDates24.add( _formatDB.format( _formatPlot.parse(strFolder.substring(0,10)) ) ); }
				if( strFolder.matches(".+_06h$") ){ listDates06.add( _formatDB.format( _formatPlot.parse(strFolder.substring(0,10)) ) ); }
				if( strFolder.matches("\\d{8}b$") ){ listDatesBase.add( _formatDB.format( _formatBase.parse(strFolder) ) ); }
			}
			
			_list24 = toArray( listDates24 );
			_list06 = toArray( listDates06 );
			_listBase = toArray( listDatesBase );
			
			//  print out the lists that will be applied
			for(int i=0; i < _list24.length; i++){ System.out.println("list24[" + i + "] = " + _list24[i]); }
			if( 0 < _list24.length ){ System.out.println(); }
			for(int i=0; i < _list06.length; i++){ System.out.println("list06[" + i + "] = " + _list06[i]); }
			if( 0 < _list06.length ){ System.out.println(); }
			for(int i=0; i < _listBase.length; i++){ System.out.println("listBase[" + i + "] = " + _listBase[i]); }
			if( 0 < _listBase.length ){ System.out.println(); }
		}

		if( boolInput ){
			if     ( strJob.equals("init24") )     { jobs = MVPlotJobInit24.getJobs(con);      } 
			else if( strJob.equals("init06") )     { jobs = MVPlotJobInit06.getJobs(con);      } 
			else if( strJob.equals("valid24") )    { jobs = MVPlotJobValid24.getJobs(con);     } 
			else if( strJob.equals("valid06") )    { jobs = MVPlotJobValid06.getJobs(con);     }
			else {
			
				for(int i=0; 1 > jobs.length || i < _listBase.length; i++){
					if( 0 < _listBase.length ){ _strBaseDate = _listBase[i]; }						
					if( strJob.equals("30day24") )         { jobs = append(jobs, MVPlotJob30Day24.getJobs(con));        }
					else if( strJob.equals("30day06") )    { jobs = append(jobs, MVPlotJob30Day06.getJobs(con));        }
					else if( strJob.equals("thresh06day") ){
						//jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
						MVPlotJobThresh06Day._strDay = "Day1"; jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
						MVPlotJobThresh06Day._strDay = "Day2"; jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
						MVPlotJobThresh06Day._strDay = "Day3"; jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
						MVPlotJobThresh06Day._strDay = "Day4"; jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
						MVPlotJobThresh06Day._strDay = "Day5"; jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
					}
					else if( strJob.equals("bar06day") )   { 
						//jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
						MVPlotJobThresh06DayBar._strDay = "Day1"; jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
						MVPlotJobThresh06DayBar._strDay = "Day2"; jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
						MVPlotJobThresh06DayBar._strDay = "Day3"; jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
						MVPlotJobThresh06DayBar._strDay = "Day4"; jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
						MVPlotJobThresh06DayBar._strDay = "Day5"; jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
					}
					else if( strJob.equals("agg24jobs") ){
						jobs = append(jobs, MVPlotJobThresh24.getJobs(con));
						jobs = append(jobs, MVPlotJobThresh24Bar.getJobs(con));
						jobs = append(jobs, MVPlotJobThresh24Box.getJobs(con));
						jobs = append(jobs, MVPlotJobAgg24.getJobs(con));
					}
					else if( strJob.equals("agg06jobs") ){							
						jobs = append(jobs, MVPlotJobThresh06.getJobs(con));
						jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));
						jobs = append(jobs, MVPlotJobThresh06Box.getJobs(con));
						jobs = append(jobs, MVPlotJobAgg06High.getJobs(con));
						jobs = append(jobs, MVPlotJobAgg06Low.getJobs(con));
					}

					//  these jobs are included in the agg24jobs and agg06jobs
					else if( strJob.equals("bar24") )      { jobs = append(jobs, MVPlotJobThresh24Bar.getJobs(con));    } 
					else if( strJob.equals("bar06") )      { jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));    }
					else if( strJob.equals("box24") )      { jobs = append(jobs, MVPlotJobThresh24Box.getJobs(con));    } 
					else if( strJob.equals("box06") )      { jobs = append(jobs, MVPlotJobThresh06Box.getJobs(con));    }
					else if( strJob.equals("agg24") )      { jobs = append(jobs, MVPlotJobAgg24.getJobs(con));          }
					else if( strJob.equals("agg06high") )  { jobs = append(jobs, MVPlotJobAgg06High.getJobs(con));      }
					else if( strJob.equals("agg06low") )   { jobs = append(jobs, MVPlotJobAgg06Low.getJobs(con));       }
					else if( strJob.equals("thresh24") )   { jobs = append(jobs, MVPlotJobThresh24.getJobs(con));       } 
					else if( strJob.equals("thresh06") )   { jobs = append(jobs, MVPlotJobThresh06.getJobs(con));       }
				}
			}
		} else {
		
//				jobs = append(jobs, MVPlotJobInit24.getJobs(con));
//				jobs = append(jobs, MVPlotJobInit06.getJobs(con));
//				jobs = append(jobs, MVPlotJobValid24.getJobs(con));
//				jobs = append(jobs, MVPlotJobValid06.getJobs(con));
			jobs = append(jobs, MVPlotJob30Day24.getJobs(con));
//				jobs = append(jobs, MVPlotJob30Day06.getJobs(con));
//				jobs = append(jobs, MVPlotJobAgg24.getJobs(con));
//				jobs = append(jobs, MVPlotJobAgg06.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh24.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh24Bar.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh24Box.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06Box.getJobs(con));
//				jobs = append(jobs, MVPlotJobMode.getJobs(con));
			
//				MVPlotJobParser parser = new MVPlotJobParser("plot_lead_series_24.xml", con);
//				jobs = parser.parsePlotJobSpec();
		}
		
		return jobs;
	}
	
	public static void runJob(MVPlotJob job) throws Exception {
		
		//  build a list of fixed value permutations for all plots
		MVOrderedMap[] listPlotFixPerm = {new MVOrderedMap()};
		MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
		if( 0 < mapPlotFixVal.size() ){
			MVDataTable tabPlotFixPerm = permute(mapPlotFixVal);
			listPlotFixPerm = tabPlotFixPerm.getRows();
		}

		//  run the plot jobs once for each permutation of plot fixed values
		MVOrderedMap mapTmplVals = job.getTmplVal();
		for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++){
			Map.Entry[] listPlotFixVal = listPlotFixPerm[intPlotFix].getOrderedEntries();
			
			//  add the fixed values to the template value map
			for(int i=0; i < listPlotFixVal.length; i++){
				String strFixVal = listPlotFixVal[i].getValue().toString();
				Matcher matDateRange = _patDateRange.matcher(strFixVal);
				if( matDateRange.matches() ){
					strFixVal = _formatPlot.format( _formatDB.parse(matDateRange.group(2)) );					
				}
				mapTmplVals.putStr(listPlotFixVal[i].getKey().toString(), strFixVal);
			}
			
		
			/*
			 * Run a query and build a set of plots for each group of dependent variables
			 */
			
			MVOrderedMap[] listDep = job.getDepGroups();
			for(int intDep=0; intDep < listDep.length; intDep++){
			
				//  get axis y1 dependent variables for the current group
				MVOrderedMap mapDep1 = null;
				MVOrderedMap[] listMapDep1Mode = {};
				Object objDep1 = listDep[intDep].get("dep1");
				if( objDep1 instanceof MVOrderedMap[] ){
					listMapDep1Mode = (MVOrderedMap[])objDep1;
					mapDep1 = listMapDep1Mode[0]; 
				} else {
					mapDep1 = (MVOrderedMap)objDep1;
				}
				
				//  get axis y2 dependent variables for the current group
				MVOrderedMap mapDep2 = null;
				MVOrderedMap[] listMapDep2Mode = {};
				Object objDep2 = listDep[intDep].get("dep2");
				if( objDep2 instanceof MVOrderedMap[] ){
					listMapDep2Mode = (MVOrderedMap[])objDep2;
					mapDep2 = listMapDep2Mode[0]; 
				} else {
					mapDep2 = (MVOrderedMap)objDep2;
				}
				
				//  get the dependent variable fixed values for this group
				MVOrderedMap mapFix = (MVOrderedMap)listDep[intDep].get("fix");

				//  build the aggregation variable permutations
				MVOrderedMap[] listAggPerm = permute(job.getAggVal()).getRows();

				//  establish lists of entires for each group of variables and values
				Map.Entry[] listAggVal		= job.getAggVal().getOrderedEntries();
				Map.Entry[] listSeries1Val	= job.getSeries1Val().getOrderedEntries();
				Map.Entry[] listSeries2Val	= (null != job.getSeries2Val()? job.getSeries2Val().getOrderedEntries() : new Map.Entry[]{});
				Map.Entry[] listSeriesNobs	= job.getSeriesNobs().getOrderedEntries();
				Map.Entry[] listDep1Plot	= mapDep1.getOrderedEntries();
				Map.Entry[] listDep2Plot	= (null != mapDep2 ? mapDep2.getOrderedEntries() : new Map.Entry[]{});
				
				//  if there are y2 dependent values, but no y2 series values, error out
				if( 0 < listDep2Plot.length && 1 > listSeries2Val.length ){
					throw new Exception("METViewer error: dep2 values present, but no series2 values in job " + job.getJobName());
				}
				
				//  combine the dependent variables for each axis into one list
				ArrayList listDepAll = new ArrayList( Arrays.asList(listDep1Plot) );
				listDepAll.addAll( Arrays.asList(listDep2Plot) );
				Map.Entry[] listDepPlot = (Map.Entry[])listDepAll.toArray(new Map.Entry[]{});
				
				//  build a list containing all mode dependent variable stats
				ArrayList listDepModeAll = new ArrayList();
				if( 0 < listMapDep1Mode.length ){ for(int i=0; i < listMapDep1Mode.length; i++){ listDepModeAll.addAll( Arrays.asList(listMapDep1Mode[i].getOrderedEntries()) ); } }
				else                            { listDepModeAll.addAll( Arrays.asList(listDep1Plot) ); }
				if( 0 < listMapDep2Mode.length ){ for(int i=0; i < listMapDep2Mode.length; i++){ listDepModeAll.addAll( Arrays.asList(listMapDep2Mode[i].getOrderedEntries()) ); } }
				else                            { listDepModeAll.addAll( Arrays.asList(listDep2Plot) ); }
				Map.Entry[] listDepPlotMode = (Map.Entry[])listDepModeAll.toArray(new Map.Entry[]{});
				if( 1 > listDepPlotMode.length ){ listDepPlotMode = listDepPlot; }
	
				//  determine if the stats are point_stat/grid_stat or mode
				boolean boolModePlot = false;
				boolean boolModeACOV = false;
				for(int i=0; i < listDepPlot.length; i++){
					String[] listStats = (String[])listDepPlot[i].getValue();
					for(int j=0; j < listStats.length; j++){
						if( _tableModeStatIndex.containsKey(listStats[j]) ){ boolModePlot = true; }
						if( listStats[j].equalsIgnoreCase("ACOV") )        { boolModeACOV = true; }
					}
				}
				
				//  flag for probabilistic fcst_var scrubbing (e.g. for probabilistic thresh_series)
				boolean boolProbScrub = false;
	
				
				/*
				 *  Build the plot query SQL
				 */
										
				//  build a comma delimited lists of the query fields as the select and sort lists
				String strSelectList = "", strSortList = "";
				Map.Entry[] listQueryFields = append( append(listAggVal, listSeries1Val), listSeries2Val );
				Hashtable tableFields = new Hashtable();
				for(int i=0; i < listQueryFields.length; i++){
					String strSelectVar = (String)listQueryFields[i].getKey();
					String strSortVar = strSelectVar;
					if( tableFields.containsKey(strSelectVar) ){ continue; }
					tableFields.put(strSelectVar, "true");
					if( strSelectVar.equals("inithour") ){
						if( boolModePlot ){
							//strSelectVar = "HOUR( SUBTIME( h.fcst_valid, CONCAT('0 ', FORMAT(h.fcst_lead/10000, 0), ':00:00') ) ) initdate";
							strSelectVar = "HOUR( h.fcst_init ) initdate";
						} else {
							strSelectVar = "HOUR(h." + _strInitdateField + ") inithour";
						}
						strSortVar = "inithour";
					} else if( strSelectVar.equals("initdate") ){
						if( boolModePlot ){
							//strSelectVar = getSQLDateFormat("SUBTIME( h.fcst_valid, CONCAT('0 ', FORMAT(h.fcst_lead/10000, 0), ':00:00') )") + " initdate";
							strSelectVar = getSQLDateFormat("h.fcst_init") + " initdate";
						} else {
							strSelectVar = getSQLDateFormat("h." + _strInitdateField) + " initdate";
						}
						strSortVar = "initdate";
					} else if( strSelectVar.equals("validhour") ){
						if( boolModePlot ){ strSelectVar = "HOUR(h.fcst_valid) validhour"; }
						else              { strSelectVar = "HOUR(h.fcst_valid_beg) validhour"; }
						strSortVar = "validhour";
					} else if( strSelectVar.equals("fcst_valid_beg") ){
						strSelectVar = getSQLDateFormat("h.fcst_valid_beg") + " fcst_valid_beg";
						strSortVar = "fcst_valid_beg";
					} else if( strSelectVar.equals("fcst_valid") ){
						strSelectVar = getSQLDateFormat("h.fcst_valid") + " fcst_valid";
						strSortVar = "fcst_valid";
					} else {
						strSelectVar = "h." + strSelectVar;
						strSortVar = strSelectVar;
					}
					strSelectList += (0 < i? ",\n" : "") + "  " + strSelectVar;
					strSortList += (0 < i? ",\n" : "") + "  " + strSortVar;
				}
				
				//  add valid time, forecast variable, independent variable and stat group to the list
				if( boolModePlot ){
					strSelectList += ",\n  " + getSQLDateFormat("h.fcst_valid") + " fcst_valid";
				} else {
					strSelectList += ",\n  " + getSQLDateFormat("h.fcst_valid_beg") + " fcst_valid_beg";
				}
				strSelectList += ",\n  h.fcst_var,\n";
				strSelectList += "  " + formatField(job.getIndyVar(), boolModePlot) + " " + job.getIndyVar() + ",\n";
				
				if( job.getBootstrapping() ){
					strSelectList += "  ldctc.total,\n  ldctc.fy_oy,\n  ldctc.fy_on,\n  ldctc.fn_oy,\n  ldctc.fn_on";
				} else if( boolModePlot ){
					strSelectList += "  mos.object_id,\n  mos.object_cat,\n  mos.area,\n  mos.intensity_50,\n  mos.intensity_90,\n" +
									 "  mop.object_id object_id_p,\n  mop.interest,\n  mop.intersection_area,\n  mop.area_ratio,\n" +
									 "  mop.centroid_dist,\n  mop.angle_diff" + (boolModeACOV? ",\n  mc.total" : "");
				} else {
					strSelectList += "  sg.stat_group_lu_id,\n  sg.stat_value";
	
					//  determine if the job calls for confidence intervals and add the fields if necessary
					boolean boolNormalCI = false, boolBootCI = false;
					String[] listPlotCI = parseRCol(job.getPlotCI()); 
					for(int i=0; i < listPlotCI.length; i++){
						if     ( listPlotCI[i].equals("norm") ){ boolNormalCI = true; }
						else if( listPlotCI[i].equals("boot") ){ boolBootCI   = true; }
					}
					if( boolNormalCI ){ strSelectList += ",\n  sg.stat_ncl,\n  sg.stat_ncu"; }
					if( boolBootCI )  { strSelectList += ",\n  sg.stat_bcl,\n  sg.stat_bcu"; }
					
					//  if nobs is requested, add baserate and total
					if( 0 < listSeriesNobs.length ){
						strSelectList += ",\n  (ldcts.total * sgb.stat_value) nobs,\n  ldcts.total";
					}
				}
	
				//  build the list of tables for the FROM clause
				String strFromList = "";			
				if( boolModePlot ){
					strFromList = "  mode_header h,\n  mode_obj_pair mop,\n  mode_obj_single mos" + (boolModeACOV? ",\n  mode_cts mc" : "");
				} else {
					strFromList = "  stat_header h";
					if( job.getBootstrapping() ){
						strFromList += ",\n  line_data_ctc ldctc";
					} else {
						strFromList += ",\n  stat_group sg";
					}
									
					//  if nobs is requested, add baserate and total
					if( 0 < listSeriesNobs.length ){
						strFromList += ",\n  stat_group sgb,\n  line_data_cts ldcts";
					}
				}
				
				//  build the where clause from the tables of field names and values
				String strWhere = "";
				
				//  build the aggregate fields where clause
				Map.Entry[] listFixAggFields = append(listAggVal, listPlotFixVal);
				for(int i=0; i < listFixAggFields.length; i++){
					String strField = (String)listFixAggFields[i].getKey();
					String strCondition = "";
					Object objValue = listFixAggFields[i].getValue();
					if( objValue instanceof String[] ){
						strCondition = "IN (" + buildValueList( (String[])objValue ) + ")";
					} else if( objValue instanceof MVOrderedMap ){
						Map.Entry[] listSets = ((MVOrderedMap)objValue).getOrderedEntries();
						for(int j=0; j < listSets.length; j++){
							strCondition += (0 == j? "" : ", ") + buildValueList( (String[])listSets[j].getValue() );
						}
						strCondition = "IN (" + strCondition + ")";
					} else if( objValue instanceof String ){
						if( objValue.toString().startsWith("BETWEEN") ){ strCondition = objValue.toString(); }
						else                                           { strCondition = "IN ('" + objValue.toString() + "')"; }
					}
					strWhere += "  " + (0 < i? "AND " : "") + formatField(strField, boolModePlot) + " " + strCondition + "\n";
				}
				
				//  add the independent variable values, if necessary
				if( 0 < job.getIndyVal().length ){
					strWhere += "  AND h." + job.getIndyVar() + " IN (" + buildValueList(job.getIndyVal()) + ")\n";
				}
	
				//  build the dependent variable where clause
				strWhere += "  AND\n  (\n";
				for(int i=0; i < listDepPlot.length; i++){
					String strFcstVar = (String)listDepPlot[i].getKey();
					String[] listStatGroupName = (String[])listDepPlot[i].getValue();
					String[] listStatGroupLuId = new String[listStatGroupName.length];
					String strDepStatClause = "";
					if( !boolModePlot && !job.getBootstrapping() ){
						for(int j=0; j < listStatGroupName.length; j++){ listStatGroupLuId[j] = (String)_tableStatIndex.get(listStatGroupName[j]); }
						strDepStatClause = "      AND sg.stat_group_lu_id IN (" +	buildValueList(listStatGroupLuId) + ")\n";
					}
					
					//  build the fcst_var criteria
					String strFcstVarClause = " = '" + strFcstVar + "'";
					Matcher matProb = _patProb.matcher(strFcstVar);
					if( matProb.matches() && strFcstVar.contains("{thr}") ){
						boolProbScrub = true;
						strFcstVarClause = " LIKE '" + strFcstVar.replace("{thr}", "%") + "'";
					}
										
					//  fixed field sql
					String strFixed = "";
					if( null != mapFix && mapFix.containsKey(strFcstVar) ){
						MVOrderedMap mapFixed = (MVOrderedMap)mapFix.get(strFcstVar);
						Map.Entry[] listFixed = mapFixed.getOrderedEntries();
						for(int j=0; j < listFixed.length; j++){
							String strField = (String)listFixed[j].getKey();
							String strValue = (String)listFixed[j].getValue();
							strFixed += "      AND " + formatField(strField, boolModePlot) + formatSQLConstraint(strValue) + "\n";
						}
					}
					
					//  build the series fields where clause
					String strSeries = "";
					Map.Entry[] listSeriesVal = (i < listDep1Plot.length? listSeries1Val : listSeries2Val);
					for(int j=0; j < listSeriesVal.length; j++){
						String strField = (String)listSeriesVal[j].getKey();
						String[] listValues = (String[])listSeriesVal[j].getValue();
						strSeries += "      AND " + formatField(strField, boolModePlot) + " IN (" + buildValueList(listValues) + ")\n";
					}
					
					strWhere += (0 < i? "    OR\n" : "") + "    (\n      h.fcst_var" + strFcstVarClause + "\n" +
								strDepStatClause + strFixed + strSeries + "    )\n";
				}
	
				if( boolModePlot ){
					strWhere += "  )\n  AND mop.mode_header_id = h.mode_header_id\n" +
								"  AND (mop.mode_obj_fcst_id = mos.mode_obj_id OR\n       mop.mode_obj_obs_id = mos.mode_obj_id)" + 
								(boolModeACOV? "\n  AND mc.field = 'OBJECT'\n  AND mc.mode_header_id = mop.mode_header_id" : "");
				} else {
					//  add the table joining clauses
					if( job.getBootstrapping() ){
						strWhere += "  )\n  AND h.stat_header_id = ldctc.stat_header_id";
					} else {
						strWhere += "  )\n  AND h.stat_header_id = sg.stat_header_id\n  AND sg.stat_value != -9999";
	
						if( 0 < listSeriesNobs.length ){
							strWhere += "\n  AND sgb.stat_group_lu_id = '0'\n" +
										  "  AND h.stat_header_id = sgb.stat_header_id\n" +
										  "  AND h.stat_header_id = ldcts.stat_header_id";
						}			
					}
				}
	
				//  put the query components together
				String strQuery = "SELECT\n" + strSelectList + "\n" +
								  "FROM\n" + strFromList + "\n" +
								  "WHERE\n" + strWhere + 
								  (_boolSQLSort? "\nORDER BY\n" + strSortList : "") + ";";
				
				System.out.println("strQuery:\n\n" + strQuery + "\n");
				
				if( _boolSQLOnly ){ continue; }
				
				
				/*
				 *  Run the query
				 */

				//  make sure the database connection is functional (because Connection.isValid() throws an AbstractMethodError)
				try{
					Statement stmt = job.getConnection().createStatement();
					stmt.executeQuery("SELECT COUNT(*) FROM stat_group_lu");
					stmt.close();				
				}catch(Exception e){
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection con = DriverManager.getConnection("jdbc:mysql://" + job.getDBHost() + "/" + job.getDBName(), job.getDBUser(), job.getDBPassword());
					if( con.isClosed() )	throw new Exception("METViewer error: database re-connection failed");
					job.setConnection(con);
				}
				
				//  run the query against the database connection and parse the results
				long intStartTime = (new java.util.Date()).getTime();
				Statement stmt = job.getConnection().createStatement();
				ResultSet res = stmt.executeQuery(strQuery);
				MVDataTable tab = new MVDataTable(res);
				stmt.close();				
				System.out.println("query returned " + tab.getNumRows() + " rows in " + formatTimeSpan( (new java.util.Date()).getTime() - intStartTime ));
				
				//  if there is no data, do not try to plot it
				if( 1 > tab.getNumRows() ){
					System.out.println("  **  WARNING: query returned no data");
					int intNumModeGroupPlots = (0 < listMapDep1Mode.length? listMapDep1Mode.length : (0 < listMapDep2Mode.length? listMapDep2Mode.length : 1));
					int intNumQueryPlots = (intNumModeGroupPlots * listAggPerm.length);
					_intPlotIndex += intNumQueryPlots;
					continue;
				}

				//  reformat the field names in the data table
				String[] listFields = tab.getFields();
				for(int i=0; i < listFields.length; i++){
					if( listFields[i].equals("HOUR(h." + _strInitdateField + ")") ){ 
						tab.setFieldName(i, "inithour");
					}
				}
				
				//  scrub the probabilistic fcst_var values, if appropriate
				if( boolProbScrub ){
					String[] listFcstVar = tab.getColumn("fcst_var");
					for(int i=0; i < listFcstVar.length; i++){
						Matcher matProb = _patProb.matcher( listFcstVar[i] );
						if( matProb.matches() ){ listFcstVar[i] = "PROB_" + matProb.group(1); }
					}
					tab.setColumn("fcst_var", listFcstVar);
				}				
				
				//  convert the stat_group_lu_id to stat_name, if present
				if( tab.containsField("stat_group_lu_id") ){
					tab.addField("stat_name");
					for(int i=0; i < listDepPlot.length; i++){
						String[] listStatName = (String[])listDepPlot[i].getValue();
						
						String strFcstVar = (String)listDepPlot[i].getKey();
						Matcher matProb = _patProb.matcher(strFcstVar);
						if( boolProbScrub && matProb.matches() ){ strFcstVar = "PROB_" + matProb.group(1); }
						final String strFcstVarComp = strFcstVar;
						
						for(int j=0; j < listStatName.length; j++){
							final String strStatGroupLuId = (String)_tableStatIndex.get(listStatName[j]);
							MVRowComp c = new MVRowComp(){
								public boolean equals(MVOrderedMap row){
									String strFcstVarRow = (String)row.get("fcst_var"); 
									String strStatGroupLuIdRow = (String)row.get("stat_group_lu_id");
									return (strFcstVarRow.equals(strFcstVarComp) && strStatGroupLuIdRow.equals(strStatGroupLuId)); 
								}
							};
							tab.set("stat_name", c, listStatName[j]); 
						}				
					}
				}
				
				//  reformat the fcst_var
				String[] listFcstVar = tab.getColumn("fcst_var");
				for(int i=0; i < listFcstVar.length; i++){ listFcstVar[i] = formatR(listFcstVar[i]); }
				tab.setColumn("fcst_var", listFcstVar);				
				
				//  add set fields to the table to handle aggregating over sets of values
				for(int i=0; i < listAggVal.length; i++){
					
					//  if the aggregate values are not sets, continue
					Object objAggVal = listAggVal[i].getValue();
					if( objAggVal instanceof String[] ){ continue; }
					
					//  if the aggregate values are sets, add a set field to the table
					final String strAggVar = listAggVal[i].getKey().toString();
					String strSetField = strAggVar + "_set"; 
					tab.addField(strSetField);
					Map.Entry[] listSetVal = ((MVOrderedMap)objAggVal).getOrderedEntries();
					
					for(int j=0; j < listSetVal.length; j++){
						final String[] listSet = (String[])listSetVal[j].getValue();
						MVRowComp c = new MVRowComp(){
							public boolean equals(MVOrderedMap row){
								return contains(listSet, row.getStr(strAggVar));
							}
						};
						tab.set(strSetField, c, listSetVal[j].getKey().toString());
					}
				}
	
				//printFormattedTable(tab); System.out.println("" + tab.getNumRows() + " rows\n");
				
				
				/*
				 *  MODE calculations
				 */
				
				//  calculate the mode statistics, if appropriate
				if( boolModePlot ){
					
					//  break the mode data into cases for calculating stats
					String[] listModeCase = {"model", "fcst_lead", "fcst_valid", "fcst_init", "fcst_accum", "obs_lead", "obs_valid", "obs_accum",
											 "fcst_rad", "fcst_thr", "obs_rad", "obs_thr", "fcst_var", "fcst_lev", "obs_var", "obs_lev"};
					MVOrderedMap mapModeCase = new MVOrderedMap();
					for(int i=0; i < listModeCase.length; i++){
						if( !tab.containsField(listModeCase[i]) ){ continue; }
						mapModeCase.put(listModeCase[i], unique( tab.getColumn(listModeCase[i]) ));
					}
					
					//  add the aggregation variable values to the cases
					for(int i=0; i < listAggVal.length; i++){
						String strAggVar = listAggVal[i].getKey().toString();					
						if( contains(listModeCase, strAggVar) ){ continue; }
						mapModeCase.put(strAggVar, (String[])listAggVal[i].getValue());
					}
					//System.out.println("\nmapModeCase:\n" + mapModeCase.getRDecl() + "\n");
						
					//  build a list of all cases for which to calculate mode stats
					MVDataTable tabModePerm = permute(mapModeCase);
					MVOrderedMap[] listModePerm = tabModePerm.getRows();
					listModeCase = mapModeCase.keyList();				
					//System.out.println("\ntabModePerm:"); printFormattedTable(tabModePerm);
					
					long intStartTimeMode = (new java.util.Date()).getTime();
					System.out.println("building mode stats: " + tabModePerm.getNumRows() + " permutations\n");
					TxtProgBar bar = new TxtProgBar((double)listModePerm.length - 1);
					int[] listSingleObjCounts = new int[listModePerm.length];
	
					//  create a table to store mode statistics
					MVDataTable tabModeStat = new MVDataTable(tabModePerm);
					String[] listModeStatFields = {"nsimp", "nsimpf", "nsimpfm", "nsimpfu", "nsimpo", "nsimpom", "nsimpou", "asimp", "asimpf", "asimpfm", "asimpfu", "asimpo",
												   "asimpom", "asimpou", "nclus", "nclusf", "ncluso", "ACOV", "pom", "pam", "awcsi", "MMI", "MMIF", "MMIO", "MIA", "MAR", "MCD", 
												   "MAD", "P50", "P90"};
					for(int i=0; i < listModeStatFields.length; i++){ tabModeStat.addField(listModeStatFields[i]); }
					int intModeStatIndex = 0;
					//System.out.println("\ntabModeStat:"); printFormattedTable(tabModeStat);
					
					//  calculate mode stats for each permutation
					for(int i=0; i < listModePerm.length; i++){						
						MVDataTable tabModeCase = tab;
						//System.out.println("\nmode perm:\n" + listModePerm[i].getRDecl() + "\n");
						for(int j=0; j < listModeCase.length; j++){						
							final String strModeCase = listModeCase[j];
							final String strModeVal = listModePerm[i].getStr( listModeCase[j] );
							tabModeCase = tabModeCase.getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return row.getStr(strModeCase).equals(strModeVal); } });
							int intNumCaseRows = tabModeCase.getNumRows();
							intNumCaseRows = intNumCaseRows + 0;
						}
						if( 0 == tabModeCase.getNumRows() ){
							//System.out.println("  ****  missing mode case  ****\n");
							tabModeStat.removeRow(intModeStatIndex);
							tabModePerm.removeRow(intModeStatIndex);
							continue;
						}
						//System.out.println("mode perm:\n" + listModePerm[i].getRDecl() + "\n\ntabModeCase:");	tabModeCase.sort("object_id"); tabModeCase.sort("fcst_valid"); printFormattedTable(tabModeCase);
						
						//  build tables for each line type
						MVDataTable tabSimpFcst  = getUniqueModeObjRows(tabModeCase, "object_id", "^F\\d{3}$"); tabSimpFcst.sort("object_id"); tabSimpFcst.sort("fcst_valid");
						MVDataTable tabSimpObs   = getUniqueModeObjRows(tabModeCase, "object_id", "^O\\d{3}$"); tabSimpObs. sort("object_id"); tabSimpObs. sort("fcst_valid");

						MVDataTable tabSimpFcstM = tabSimpFcst.getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return !row.getStr("object_cat").matches("^C[FO]000$"); } });
						MVDataTable tabSimpFcstU = tabSimpFcst.getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return row.getStr("object_cat").matches("^C[FO]000$"); } });
						MVDataTable tabSimpObsM  = tabSimpObs. getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return !row.getStr("object_cat").matches("^C[FO]000$"); } });
						MVDataTable tabSimpObsU  = tabSimpObs. getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return row.getStr("object_cat").matches("^C[FO]000$"); } });
						
						MVDataTable tabClusFcst  = getUniqueModeObjRows(tabModeCase, "object_id", "^CF\\d{3}$"); tabClusFcst.sort("object_id"); tabClusFcst.sort("fcst_valid");
						MVDataTable tabClusObs   = getUniqueModeObjRows(tabModeCase, "object_id", "^CO\\d{3}$"); tabClusObs. sort("object_id"); tabClusObs. sort("fcst_valid");
						MVDataTable tabClusPair  = tabModeCase.getRows(new MVRowComp(){ public boolean equals(MVOrderedMap row){ return row.getStr("object_id_p").matches("^CF\\d{3}_CO\\d{3}$"); } });
						//System.out.println("\ntabClusFcst:\n"); printFormattedTable(tabClusFcst); System.out.println("\ntabClusObs:\n"); printFormattedTable(tabClusObs);

						//  counts of simple/matched/unmatched forecast/observation objects
						MVOrderedMap mapCaseData = tabModeStat.getRow(intModeStatIndex++);
						mapCaseData.putStr("nsimp",		tabSimpFcst.getNumRows() + tabSimpObs.getNumRows());
						mapCaseData.putStr("nsimpf",	tabSimpFcst.getNumRows());
						mapCaseData.putStr("nsimpfm",	tabSimpFcstM.getNumRows());
						mapCaseData.putStr("nsimpfu",	tabSimpFcstU.getNumRows());
						mapCaseData.putStr("nsimpo",	tabSimpObs.getNumRows());
						mapCaseData.putStr("nsimpom",	tabSimpObsM.getNumRows());
						mapCaseData.putStr("nsimpou",	tabSimpObsU.getNumRows());
						
						//  areas of simple/matched/unmatched forecast/observation objects
						mapCaseData.putStr("asimp",		sum(tabSimpFcst.getIntColumn("area")) + sum(tabSimpObs.getIntColumn("area")) );
						mapCaseData.putStr("asimpf",	sum(tabSimpFcst.getIntColumn("area")) );
						mapCaseData.putStr("asimpfm",	sum(tabSimpFcstM.getIntColumn("area")) );
						mapCaseData.putStr("asimpfu",	sum(tabSimpFcstU.getIntColumn("area")) );
						mapCaseData.putStr("asimpo",	sum(tabSimpObs.getIntColumn("area")) );
						mapCaseData.putStr("asimpom",	sum(tabSimpObsM.getIntColumn("area")) );
						mapCaseData.putStr("asimpou",	sum(tabSimpObsU.getIntColumn("area")) );
						
						//  counts of cluster forecast/observation objects
						mapCaseData.putStr("nclus",		tabClusFcst.getNumRows() + tabClusObs.getNumRows());
						mapCaseData.putStr("nclusf",	tabClusFcst.getNumRows());
						mapCaseData.putStr("ncluso",	tabClusObs.getNumRows());
						
						//  compute the aerial coverage of observation points
						if( boolModeACOV ){
							double dblCaseTotal = median(tabModeCase.getDoubleColumn("total"));
							mapCaseData.put("ACOV",	new double[]{ mapCaseData.getDouble("asimpo") / (dblCaseTotal * (double)tabSimpObs.getNumRows()) });
						}
						
						//  percentage of simple objects and area matched
						mapCaseData.putStr("pom",	(mapCaseData.getDouble("nsimpfm") + mapCaseData.getDouble("nsimpom")) / mapCaseData.getDouble("nsimp")); 
						mapCaseData.putStr("pam",	(mapCaseData.getDouble("asimpfm") + mapCaseData.getDouble("asimpom")) / mapCaseData.getDouble("asimp"));
						
						//  compute the area-weighted CSI using hits, misses, and false-alarms
						double dblHits = (mapCaseData.getDouble("asimpfm") + mapCaseData.getDouble("asimpom")) / 2;
						mapCaseData.putStr("awcsi",	dblHits / (dblHits + mapCaseData.getDouble("asimpou") + mapCaseData.getDouble("asimpfu")));
	
						//  find maximum interest among the forecast objects
						MVOrderedMap tableMIFcst = new MVOrderedMap();
						MVOrderedMap[] listSimpFcst = tabSimpFcst.getRows();
						for(int j=0; j < listSimpFcst.length; j++){
							String strObjectId = listSimpFcst[j].getStr("object_id");
							double dblInt = listSimpFcst[j].getDouble("interest");
							if( !tableMIFcst.containsKey(strObjectId) || dblInt > tableMIFcst.getDouble(strObjectId)){
								tableMIFcst.putStr(strObjectId, dblInt);
							}
						}
						
						//  find the maximum interest among the observed objects
						MVOrderedMap tableMIObs = new MVOrderedMap();
						MVOrderedMap[] listSimpObs = tabSimpObs.getRows();
						for(int j=0; j < listSimpObs.length; j++){
							String strObjectId = listSimpObs[j].getStr("object_id");
							double dblInt = listSimpObs[j].getDouble("interest");
							if( !tableMIObs.containsKey(strObjectId) || dblInt > tableMIObs.getDouble(strObjectId)){
								tableMIObs.putStr(strObjectId, dblInt);
							}
						}
	
						//  determine the maximum interest for forecast, observed and both
						Map.Entry[] listMIFcstObj = tableMIFcst.getOrderedEntries();
						double[] listMIFcst = new double[tableMIFcst.size()];
						double[] listMI = new double[tableMIFcst.size() + tableMIObs.size()];
						int intMIIndex = 0;
						for(int j=0; j < listMIFcstObj.length; j++){
							double dblMIFcst = Double.parseDouble(listMIFcstObj[j].getValue().toString()); 
							listMIFcst[j] = dblMIFcst;
							listMI[intMIIndex++] = dblMIFcst;
						}
						Map.Entry[] listMIObsObj = tableMIObs.getOrderedEntries();
						double[] listMIObs = new double[tableMIObs.size()];
						for(int j=0; j < listMIObsObj.length; j++){
							double dblMIObs = Double.parseDouble(listMIObsObj[j].getValue().toString()); 
							listMIObs[j] = dblMIObs;
							listMI[intMIIndex++] = dblMIObs;
						}
						
						//  add the maximum interest scores to the data table
						mapCaseData.put("MMI",  listMI);
						mapCaseData.put("MMIF", listMIFcst);
						mapCaseData.put("MMIO", listMIObs);						 
						
						//  add the scores for clustered objects to the data table
						mapCaseData.put("MIA", tabClusPair.getDoubleColumn("intersection_area"));
						mapCaseData.put("MAR", tabClusPair.getDoubleColumn("area_ratio"));
						mapCaseData.put("MCD", tabClusPair.getDoubleColumn("centroid_dist"));
						mapCaseData.put("MAD", tabClusPair.getDoubleColumn("angle_diff"));						
	
						//  calculate the median intensity difference between fcst and obs for the 50th and 90th percentiles
						int intNClus = mapCaseData.getInt("nclusf");
						if( 0 < intNClus ){
							double[] listP50 = new double[intNClus];
							double[] listP90 = new double[intNClus];
							
							for(int j=0; j < intNClus; j++){
								listP50[j] = tabClusFcst.getDbl("intensity_50", j) - tabClusObs.getDbl("intensity_50", j);
								listP90[j] = tabClusFcst.getDbl("intensity_90", j) - tabClusObs.getDbl("intensity_90", j);
							}
							
							mapCaseData.put("P50", listP50);
							mapCaseData.put("P90", listP90);
						} else {
							mapCaseData.put("P50", new double[]{});
							mapCaseData.put("P90", new double[]{});
						}
						
						bar.updateProgress((double)i);
						listSingleObjCounts[i] = tabSimpFcst.getNumRows();
						
						//System.out.println("\ntabModeStat after calcs:"); printFormattedTable(tabModeStat); System.out.println("" + tabModeStat.getNumRows() + " rows\n");					
					} // end: for(int i=0; i < listModePerm.length; i++)
					//System.out.println("\ntabModeStat:"); printFormattedTable(tabModeStat); System.out.println("" + tabModeStat.getNumRows() + " rows\n");
	
					//  build a data table with the stats
					MVOrderedMap mapModeStats = new MVOrderedMap();
					MVDataTable tabModePlot = new MVDataTable();
					tabModePlot.addFields(tabModePerm.getFields());
					tabModePlot.addField("stat_name");
					tabModePlot.addField("stat_value");
					for(int i=0; i < listDepPlotMode.length; i++){
						
						//  build a small table for each fcst_var and stat
						String[] listStatName = (String[])listDepPlotMode[i].getValue();
						for(int j=0; j < listStatName.length; j++){
							if( mapModeStats.containsKey(listStatName[j]) ){ continue; }
							mapModeStats.put(listStatName[j], "true");
							MVDataTable tabModePlotVar = new MVDataTable(tabModePerm);

							tabModePlotVar.addField("stat_name");
							tabModePlotVar.addField("stat_value");
							for(int k=0; k < tabModePlotVar.getNumRows(); k++){
								MVOrderedMap mapPerm = tabModePlotVar.getRow(k);
								MVOrderedMap mapModeStat = tabModeStat.getRow(k);
								double[] listStat = (double[])mapModeStat.get(listStatName[j]);
								if( 1 > listStat.length ){ continue; }
								for(int l=0; l < listStat.length; l++){
									MVOrderedMap mapPermStat = new MVOrderedMap(mapPerm);
									mapPermStat.putStr("stat_name", listStatName[j]);
									mapPermStat.putStr("stat_value", "" + listStat[l]);
									tabModePlot.addRow(mapPermStat);
								}
							}
							
							
						}
					}
					//System.out.println("\ntabModePlot:"); printFormattedTable(tabModePlot, -1); System.out.println("" + tabModePlot.getNumRows() + " rows\n");
					tabModeStat.clear();	tabModeStat = null;
					tabModePerm.clear();	tabModePerm = null;
					tab.clear();
					tab = tabModePlot;
					
					System.out.println("\n\nmode stats completed in " + formatTimeSpan( (new java.util.Date()).getTime() - intStartTimeMode ) + "\n" +
									   "mode object counts:\n" +
							   		   "      min objs: " + min(listSingleObjCounts) + "\n" +
							   		   "  meadian objs: " + median(listSingleObjCounts) + "\n" +
							   		   "      max objs: " + max(listSingleObjCounts) + "\n\n");
				
				}  // end: if( boolModePlot )
				
				//  determine if the indy values require tick marks
				boolean boolIndyValTick = false;
				String[] listIndyVal = job.getIndyVal();
				for(int i=0; i < listIndyVal.length; i++){
					try{ Double.parseDouble(listIndyVal[i]); }catch(Exception e){ boolIndyValTick = true; }
				}
								
				/*
				 *  Run a plot for each mode group
				 */
				
				//  set up the mode group dep list and remember which dependent variable (1 or 2) has the set
				MVOrderedMap[] listMapDepMode = new MVOrderedMap[]{mapDep1};
				int intDepNMode = 0;
				if (0 < listMapDep1Mode.length ){
					intDepNMode = 1;
					listMapDepMode = listMapDep1Mode;
				} else if( 0 < listMapDep2Mode.length ){
					intDepNMode = 2;
					listMapDepMode = listMapDep2Mode;
				}
								
				for(int intDepMode = 0; intDepMode < listMapDepMode.length; intDepMode++){
					
					//  set the correct dep group for the current plot
					MVOrderedMap mapDep1Plot = mapDep1;
					MVOrderedMap mapDep2Plot = mapDep2;
					if( 1 == intDepNMode ){
						listDep1Plot = listMapDepMode[intDepMode].getOrderedEntries();
						mapDep1Plot = listMapDepMode[intDepMode];
					} else if( 2 == intDepNMode ){
						listDep2Plot = listMapDepMode[intDepMode].getOrderedEntries();
						mapDep2Plot = listMapDepMode[intDepMode];
					}
				
					/*
					 *  Run a plot for each permutation of aggregate values
					 */
					
					//MVOrderedMap[] listAggPerm = permute(job.getAggVal()).getRows();
					for(int intPerm=0; intPerm < listAggPerm.length; intPerm++){
						
						System.out.println("\n* * * * * * * * * * * *\n  PLOT - " + (_intPlotIndex++ + 1) + " / " + _intNumPlots + "\n* * * * * * * * * * * *\n");
						
						/*
						 *  Build a data table that contains the data specific to this permutation 
						 */
		
						//  pull out data corresponding to the aggregate values for this plot
						MVDataTable tabPerm = tab;
						String[] listKeys = listAggPerm[intPerm].keyList();
						for(int i=0; i < listKeys.length; i++){
							final String strKey = listKeys[i];
							final String strVal = (String)listAggPerm[intPerm].get(strKey);
							tabPerm = tabPerm.getRows(new MVRowComp(){
								public boolean equals(MVOrderedMap row){ return row.get(strKey).equals(strVal);	}
							});
						}
						
						//  if a mode group is being used, pull out data for only the current dependent variables
						if( 0 != intDepNMode ){
							MVDataTable tabPermMode = new MVDataTable();
							tabPermMode.addFields( tabPerm.getFields() );
							Map.Entry[] listDepPlotAll = listDep1Plot;
							listDepPlotAll = append(listDepPlotAll, listDep2Plot);
							for(int i=0; i < listDepPlotAll.length; i++){
								final String strFcstVar = listDepPlotAll[i].getKey().toString();
								final String[] listStats = (String[])listDepPlotAll[i].getValue();
								for(int j=0; j < listStats.length; j++){
									final String strStat = listStats[j];
									tabPermMode.addRows( tabPerm.getRows(new MVRowComp(){
										public boolean equals(MVOrderedMap row){										
											return row.get("fcst_var").equals(strFcstVar) && row.get("stat_name").equals(strStat);
										}
									}).getRows() );
								}
							}
							tabPerm = tabPermMode;
						}						
						//System.out.println("\ntabPerm:"); printFormattedTable(tabPerm, -1); System.out.println("" + tabPerm.getNumRows() + " rows\n");
						
							
						/*
						 *  Build a map of all plot-specific values to use with the templates 
						 */
		
						//MVOrderedMap mapTmplVals = job.getTmplVal();
						MVOrderedMap mapPlotTmplVals = new MVOrderedMap( mapTmplVals );
		
						//  bootstrap data
						MVOrderedMap mapBootStatic = new MVOrderedMap( listAggPerm[intPerm] );
						MVOrderedMap mapSeries1Val = new MVOrderedMap( job.getSeries1Val() );
						MVOrderedMap mapSeries2Val = new MVOrderedMap( job.getSeries2Val() );
						
						//  add the independent and dependent variables to the template value map
						mapPlotTmplVals.put("indy_var", job.getIndyVar());
						Map.Entry[][] listDepPlotList = {listDep1Plot, listDep2Plot};
						ArrayList listBootStats1 = new ArrayList();
						ArrayList listBootStats2 = new ArrayList();
						for(int intDepPlot = 0; intDepPlot < 2; intDepPlot++){
							Map.Entry[] listDepCur = listDepPlotList[intDepPlot];
							String strDepName = "dep" + (intDepPlot+1);
							
							//  add the stats for each fcst_var
							for(int i=0; i < listDepCur.length; i++){
								
								//  add the stat names
								String strFcstVar = (String)listDepCur[i].getKey();
								mapPlotTmplVals.put(strDepName + "_" + (i+1), strFcstVar);
								mapBootStatic.put("fcst_var", strFcstVar);
								String[] listStats = (String[])listDepCur[i].getValue();						
								for(int j=0; j < listStats.length; j++){
									mapPlotTmplVals.put(strDepName + "_" + (i+1) + "_stat" + (j+1), listStats[j]);
									if( job.getBootstrapping() && 0 == intDepPlot ){ listBootStats1.add(listStats[j]); }
									if( job.getBootstrapping() && 1 == intDepPlot ){ listBootStats2.add(listStats[j]); }
								}
								
								//  add the fixed fields and values
								if( null != mapFix && mapFix.containsKey(strFcstVar) ){
									MVOrderedMap mapFixCur = (MVOrderedMap)mapFix.get(strFcstVar);
									Map.Entry[] listFixCurVal = mapFixCur.getOrderedEntries();
									for(int j=0; j < listFixCurVal.length; j++){
										String strFixVar = (String)listFixCurVal[j].getKey();
										String strFixVal = (String)listFixCurVal[j].getValue();
										mapPlotTmplVals.put(strFixVar, strFixVal);
										if( job.getBootstrapping() && !strFixVal.contains(" ") ){ mapBootStatic.put(strFixVar, strFixVal); }
									}
								}
							}
						}
		
						//  add the aggregate values to the template values map
						mapPlotTmplVals.putAll(listAggPerm[intPerm]);
						System.out.println(mapPlotTmplVals.getRDecl() + "\n");
		
						if( 1 > tabPerm.getNumRows() ){
							System.out.println("no plot data found");
							continue;
						}
						
						//printFormattedResults(tabPerm); System.out.println("" + tabPerm.getNumRows() + " rows\n");
						System.out.println("Plotting " + tabPerm.getNumRows() + " rows\n");
		
						
						/*
						 *  Print the data file in the R_work subfolder and file specified by the data file template
						 */
						
						_strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/")? "" : "/");
						_strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/")? "" : "/");
						_strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/")? "" : "/");
		
						String strDataFile	= _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
						if( job.getBootstrapping() ){ strDataFile = strDataFile + ".boot"; }
						(new File(strDataFile)).getParentFile().mkdirs();
						printFormattedTable(tabPerm, new PrintStream(strDataFile), "\t");
						tabPerm = null;
						
										
						/*
						 *  If bootstrapping is requested, generate the bootstrapped data 
						 */
										
						if( job.getBootstrapping() ){
		
							//  construct and create the path for the bootstrap data output file
							String strBootInfo = strDataFile.replaceFirst("\\.data.boot$", ".boot.info");
							String strBootOutput = strDataFile.replaceFirst("\\.boot$", "");
							File fileBootOutput = new File(strBootOutput); 
		
							//  build the map containing tag values for the boot info template
							Hashtable tableBootInfo = new Hashtable();
							tableBootInfo.put("boot_diff1",		job.getBootDiff1()? "TRUE" : "FALSE");
							tableBootInfo.put("boot_diff2",		job.getBootDiff2()? "TRUE" : "FALSE");
							tableBootInfo.put("boot_repl",		job.getBootRepl());
							tableBootInfo.put("boot_ci",		job.getBootCI());
							tableBootInfo.put("ci_alpha",		job.getCIAlpha());
							tableBootInfo.put("indy_var",		job.getIndyVar());
							tableBootInfo.put("indy_list",		(0 < job.getIndyVal().length? printRCol(job.getIndyVal(), boolIndyValTick) : "c()"));
							tableBootInfo.put("series1_list",	job.getSeries1Val().getRDecl());
							tableBootInfo.put("series2_list",	job.getSeries2Val().getRDecl());
							tableBootInfo.put("boot_stat1",		printRCol(toArray(listBootStats1), true));
							tableBootInfo.put("boot_stat2",		printRCol(toArray(listBootStats2), true));
							tableBootInfo.put("boot_static",	mapBootStatic.getRDecl());
							tableBootInfo.put("boot_input",		strDataFile);
							tableBootInfo.put("boot_output",	strBootOutput);
							tableBootInfo.put("working_dir",	_strRworkFolder + "include");
						
							//  populate the boot info file
							populateTemplateFile(_strRtmplFolder + "boot.info_tmpl", strBootInfo, tableBootInfo);
															
							//  run boot.R to generate the data file for plotting
							if( !fileBootOutput.exists() || !_boolCacheBoot ){
								fileBootOutput.getParentFile().mkdirs();
								runRscript(_strRworkFolder + "include/boot.R", new String[]{strBootInfo});
							}
		
							//  if boot_diffN is turned on, add __BOOT_DIFFN__ to the plot series
							for(int i=0; i < 2; i++){
								MVOrderedMap mapSeriesVal = null;
								String strDiffSeries = "";
								if     ( i == 0 && job.getBootDiff1() ){ mapSeriesVal = mapSeries1Val; strDiffSeries = "__BOOT_DIFF1__"; }
								else if( i == 1 && job.getBootDiff2() ){ mapSeriesVal = mapSeries2Val; strDiffSeries = "__BOOT_DIFF2__"; }
								else                                   { continue; }						
								String[] listSeriesVar = mapSeriesVal.keyList();
								ArrayList listDiffVal = new ArrayList( Arrays.asList( ((String[])mapSeriesVal.get(listSeriesVar[listSeriesVar.length - 1])) ) );
								listDiffVal.add(listDiffVal.size() - 1, strDiffSeries);
								mapSeriesVal.put(listSeriesVar[listSeriesVar.length - 1], toArray(listDiffVal));
							}					
							
							//  remove the .boot suffix from the data file
							strDataFile = strBootOutput;
						}
		
						
						/*
						 *  Generate filenames and plot labels from the templates 
						 */
		
						//  use the map of all plot values to populate the template strings
						String strPlotFile	= _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
						String strRFile		= _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
						String strTitle		= buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps());
						String strXLabel	= buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
						String strY1Label	= buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());				
						String strY2Label	= buildTemplateString(job.getY2LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());				
		
						//  create the plot and R script output folders, if necessary
						(new File(strPlotFile)).getParentFile().mkdirs();
						(new File(strRFile)).getParentFile().mkdirs();
										
						/*
						 *  Generate the map of R template tags for the plot
						 */
						
						Hashtable tableRTags = new Hashtable();
		
						tableRTags.put("r_work",		_strRworkFolder);
						tableRTags.put("indy_var",		job.getIndyVar());
						tableRTags.put("indy_list",		(0 < job.getIndyVal().length? printRCol(job.getIndyVal(), boolIndyValTick) : "c()"));
						tableRTags.put("indy_label",	(0 < job.getIndyLabel().length? printRCol(job.getIndyLabel(), true) : "c()"));
						tableRTags.put("dep1_plot",		mapDep1Plot.getRDecl());				
						tableRTags.put("dep2_plot",		(null != mapDep2Plot? mapDep2Plot.getRDecl() : "c()"));
						tableRTags.put("agg_list",		listAggPerm[intPerm].getRDecl());
						tableRTags.put("series1_list",	mapSeries1Val.getRDecl());
						tableRTags.put("series2_list",	mapSeries2Val.getRDecl());
						tableRTags.put("series_nobs",	job.getSeriesNobs().getRDecl());
						tableRTags.put("dep1_scale",	job.getDep1Scale().getRDecl());
						tableRTags.put("dep2_scale",	job.getDep2Scale().getRDecl());
						tableRTags.put("plot_file",		strPlotFile);
						tableRTags.put("data_file",		strDataFile);
						tableRTags.put("plot_title",	strTitle);
						tableRTags.put("x_label",		strXLabel);
						tableRTags.put("y1_label",		strY1Label);
						tableRTags.put("y2_label",		strY2Label);
						tableRTags.put("plot_cmd", 		job.getPlotCmd());
						tableRTags.put("event_equal",	(job.getEventEqual()?	"TRUE" : "FALSE"));
						tableRTags.put("plot1_diff",	(job.getPlot1Diff()?	"TRUE" : "FALSE"));
						tableRTags.put("plot2_diff",	(job.getPlot2Diff()?	"TRUE" : "FALSE"));
						tableRTags.put("show_nstats",	(job.getShowNStats()?	"TRUE" : "FALSE"));
						tableRTags.put("indy1_stagger",	(job.getIndy1Stagger()?	"TRUE" : "FALSE"));
						tableRTags.put("indy2_stagger",	(job.getIndy2Stagger()?	"TRUE" : "FALSE"));
						tableRTags.put("grid_on",		(job.getGridOn()?		"TRUE" : "FALSE"));
						tableRTags.put("sync_axes",		(job.getSyncAxes()?		"TRUE" : "FALSE"));
						tableRTags.put("dump_points1",	(job.getDumpPoints1()?	"TRUE" : "FALSE"));
						tableRTags.put("dump_points2",	(job.getDumpPoints2()?	"TRUE" : "FALSE"));
						tableRTags.put("log_y1",		(job.getLogY1()?		"TRUE" : "FALSE"));
						tableRTags.put("log_y2",		(job.getLogY2()?		"TRUE" : "FALSE"));
						
						// calculate the number of plot curves
						int intNumDep1 = 0;
						for(int i=0; i < listDep1Plot.length; i++){
							intNumDep1 += ((String[])listDep1Plot[i].getValue()).length;
						}
						int intNumDep2 = 0;
						for(int i=0; i < listDep2Plot.length; i++){
							intNumDep2 += ((String[])listDep2Plot[i].getValue()).length;
						}
						int intNumSeries1Perm = 1;
						for(int i=0; i < listSeries1Val.length; i++){
							String[] listVal = (String[])listSeries1Val[i].getValue();
							intNumSeries1Perm *= listVal.length;
						}				
						int intNumSeries2Perm = 1;
						for(int i=0; i < listSeries2Val.length; i++){
							intNumSeries2Perm *= ((String[])listSeries2Val[i].getValue()).length;
						}
						
						tableRTags.put("plot_type",		job.getPlotType());
						tableRTags.put("plot_width",	job.getPlotWidth());
						tableRTags.put("plot_height",	job.getPlotHeight());
						tableRTags.put("plot_res",		job.getPlotRes());
						tableRTags.put("plot_units",	job.getPlotUnits());
						tableRTags.put("mar",			job.getMar());
						tableRTags.put("mgp",			job.getMgp());
						tableRTags.put("cex",			job.getCex());
						tableRTags.put("title_weight",	job.getTitleWeight());
						tableRTags.put("title_size",	job.getTitleSize());
						tableRTags.put("title_offset",	job.getTitleOffset());
						tableRTags.put("title_align",	job.getTitleAlign());
						tableRTags.put("xtlab_orient",	job.getXtlabOrient());
						tableRTags.put("xtlab_perp",	job.getXtlabPerp());
						tableRTags.put("xtlab_horiz",	job.getXtlabHoriz());
						tableRTags.put("xlab_weight",	job.getXlabWeight());
						tableRTags.put("xlab_size",		job.getXlabSize());
						tableRTags.put("xlab_offset",	job.getXlabOffset());
						tableRTags.put("xlab_align",	job.getXlabAlign());
						tableRTags.put("ytlab_orient",	job.getYtlabOrient());
						tableRTags.put("ytlab_perp",	job.getYtlabPerp());
						tableRTags.put("ytlab_horiz",	job.getYtlabHoriz());
						tableRTags.put("ylab_weight",	job.getYlabWeight());
						tableRTags.put("ylab_size",		job.getYlabSize());
						tableRTags.put("ylab_offset",	job.getYlabOffset());
						tableRTags.put("ylab_align",	job.getYlabAlign());
						tableRTags.put("grid_lty",		job.getGridLty());
						tableRTags.put("grid_col",		job.getGridCol());
						tableRTags.put("grid_lwd",		job.getGridLwd());
						tableRTags.put("grid_x",		job.getGridX());
						tableRTags.put("x2tlab_orient",	job.getX2tlabOrient());
						tableRTags.put("x2tlab_perp",	job.getX2tlabPerp());
						tableRTags.put("x2tlab_horiz",	job.getX2tlabHoriz());
						tableRTags.put("x2lab_weight",	job.getX2labWeight());
						tableRTags.put("x2lab_size",	job.getX2labSize());
						tableRTags.put("x2lab_offset",	job.getX2labOffset());
						tableRTags.put("x2lab_align",	job.getX2labAlign());
						tableRTags.put("y2tlab_orient",	job.getY2tlabOrient());
						tableRTags.put("y2tlab_perp",	job.getY2tlabPerp());
						tableRTags.put("y2tlab_horiz",	job.getY2tlabHoriz());
						tableRTags.put("y2lab_weight",	job.getY2labWeight());
						tableRTags.put("y2lab_size",	job.getY2labSize());
						tableRTags.put("y2lab_offset",	job.getY2labOffset());
						tableRTags.put("y2lab_align",	job.getY2labAlign());
						tableRTags.put("legend_size",	job.getLegendSize());
						tableRTags.put("legend_box",	job.getLegendBox());
						tableRTags.put("legend_inset",	job.getLegendInset());
						tableRTags.put("legend_ncol",	job.getLegendNcol());
						tableRTags.put("box_boxwex",	job.getBoxBoxwex());
						tableRTags.put("box_notch",		job.getBoxNotch());
						tableRTags.put("ci_alpha",		job.getCIAlpha());
						
						int intNumDep1Series = intNumDep1 * (intNumSeries1Perm + (job.getPlot1Diff()? 1 : 0));
						int intNumDep2Series = intNumDep2 * (intNumSeries2Perm + (job.getPlot2Diff()? 1 : 0));
						int intNumDepSeries = intNumDep1Series + intNumDep2Series;
						
						tableRTags.put("plot_ci",	job.getPlotCI().equals("")? printRCol( rep("none", intNumDepSeries) )	: job.getPlotCI());
						tableRTags.put("colors",	job.getColors().equals("")?	"rainbow(" + intNumDepSeries + ")"		: job.getColors());
						tableRTags.put("pch",		job.getPch().equals("")?	printRCol( rep(20, intNumDepSeries) )	: job.getPch());
						tableRTags.put("type",		job.getType().equals("")?	printRCol( rep("b",	intNumDepSeries) )	: job.getType());
						tableRTags.put("lty",		job.getLty().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLty());
						tableRTags.put("lwd",		job.getLwd().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLwd());
						tableRTags.put("con_series",job.getConSeries().equals("")? printRCol( rep(0, intNumDepSeries) )	: job.getConSeries());
						tableRTags.put("legend",	job.getLegend().equals("")? "c()" : job.getLegend());
						tableRTags.put("y1_lim",	job.getY1Lim().equals("")?	"c()" : job.getY1Lim());
						tableRTags.put("y1_bufr",	job.getY1Bufr());
						tableRTags.put("y2_lim",	job.getY2Lim().equals("")?	"c()" : job.getY2Lim());
						tableRTags.put("y2_bufr",	job.getY2Bufr());
						
						
						/*
						 *  Read the template in, replacing the appropriate tags with generated R code
						 */
		
						populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);
			
						
						/*
						 *  Attempt to run the generated R script
						 */			
		
						if( _boolPlot ){
							runRscript(strRFile);
							_intNumPlotsRun++;
						}
						
					} // end: for(int intPerm=0; intPerm < listAggPerm.length; intPerm++)
				
				} // end: for(int intDepMode = 0; intDepMode < listMapDepMode.length; intDepMode++)
	
				//  try to throw memory back onto the heap
				res = null;
				tab.clear();
				tab = null;
				//System.gc();
				
			} // end: for(int intDep=0; intDep < listDep.length; intDep++)
		
		} // end: for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++)

	}
		
	/**
	 * Populate the template tags in the input template file named tmpl with values from the input
	 * table vals and write the result to the output file named output.
	 * @param tmpl Template file to populate
	 * @param output Output file to write
	 * @param vals Table containing values corresponding to tags in the input template
	 * @throws Exception
	 */
	public static void populateTemplateFile(String tmpl, String output, Hashtable vals) throws Exception{
		BufferedReader reader = new BufferedReader( new FileReader(tmpl) );
		PrintStream writer = new PrintStream(output);
		while( reader.ready() ){
			String strTmplLine = reader.readLine();
			String strOutputLine = strTmplLine;
			
			Matcher matRtmplLine = _patRTmpl.matcher(strTmplLine);
			while( matRtmplLine.find() ){
				String strRtmplTag = matRtmplLine.group(1);
				if( !vals.containsKey(strRtmplTag) ){ continue; }
				String strRTagVal = (String)vals.get(strRtmplTag);
				strOutputLine = strOutputLine.replace("#<" + strRtmplTag + ">#", strRTagVal);
			}
			
			writer.println(strOutputLine);
		}
		reader.close();
		writer.close();
	}
	
	/**
	 * Run the input R script named r using the Rscript command.  The output and error output will
	 * be written to standard output.
	 * @param r R script to run
	 * @param args (optional) Arguments to pass to the R script
	 * @throws Exception
	 */
	public static void runRscript(String r, String[] args) throws Exception{
		
		String strArgList = "";
		for(int i=0; null != args && i < args.length; i++){ strArgList += " " + args[i]; }
		
		System.out.println("\nRunning 'Rscript " + r + "'");
		Process proc = Runtime.getRuntime().exec("Rscript " + r + strArgList);
		if( _boolProcWait ){
			proc.waitFor();
		} else {
			try{
				Thread.sleep(10000);
			}catch(InterruptedException ie){}
		}

		String strRscriptOut = "";
		BufferedReader readerProcIn = new BufferedReader( new InputStreamReader(proc.getInputStream()) );		
		while( readerProcIn.ready() ){
			strRscriptOut += readerProcIn.readLine() + "\n";
		}
		readerProcIn.close();			
		if( !"".equals(strRscriptOut) ){
			System.out.println("\n==== Start Rscript output  ====\n" + strRscriptOut + "====   End Rscript output  ====");
		}
		
		String strRscriptErr = "";
		BufferedReader readerProcError = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );		
		while( readerProcError.ready() ){
			strRscriptErr += readerProcError.readLine() + "\n";
		}
		readerProcError.close();
		if( !"".equals(strRscriptErr) ){
			System.out.println("\n==== Start Rscript error  ====\n" + strRscriptErr + "====   End Rscript error  ====");
		}
	}
	public static void runRscript(String r) throws Exception{ runRscript(r, new String[]{}); }
	
	/**
	 * Prints a textual representation of the input {@link MVDataTable} with the field names in the 
	 * first row to the specified {@link PrintStream} destination.  
	 * @param res The MVDataTable to print
	 * @param str The stream to write the formatted results to (defaults to System.out)
	 * @param delim The delimiter to insert between field headers and values (defaults to ' ')
	 * @param maxRows The max number of rows to print, -1 to print all rows
	 */
	public static void printFormattedTable(MVDataTable tab, PrintStream str, String delim, int maxRows){
		String[] fields = tab.getFields();
		int[] intFieldWidths = new int[tab.getNumFields()];
		for(int i=0; i < fields.length; i++){
			intFieldWidths[i] = tab.getMaxFieldLength(fields[i]) + 2;
		}
		
		for(int i=0; i < fields.length; i++){
			str.print( delim.equals(" ")? padEnd(fields[i], intFieldWidths[i]) : (0 < i? delim : "") + fields[i] );
		}
		str.println();					
		
		MVOrderedMap[] rows = tab.getRows();
		int intPrintRows = (0 < maxRows? (maxRows < rows.length? maxRows : rows.length) : rows.length);
		for(int i=0; i < intPrintRows; i++){
			for(int j=0; j < fields.length; j++){
				String strVal = (String)rows[i].get(fields[j]); 
				str.print( delim.equals(" ")? padEnd(strVal, intFieldWidths[j]) : (0 < j? delim : "") + strVal );
			}
			str.println();
		}
		if( 0 < maxRows && maxRows < rows.length ){ str.println("(" + (rows.length - maxRows) + " more rows...)"); }
	}
	public static void printFormattedTable(MVDataTable tab){ printFormattedTable(tab, System.out, " ", 40); }
	public static void printFormattedTable(MVDataTable tab, int maxRows){ printFormattedTable(tab, System.out, " ", maxRows); }
	public static void printFormattedTable(MVDataTable tab, PrintStream str, String delim){ printFormattedTable(tab, str, delim, -1); }

	public static String formatField(String field, boolean mode){
		if( field.equals("inithour") ){
			if( mode ){ return "HOUR( h.fcst_init )"; }
			else      { return "HOUR(h." + _strInitdateField + ")"; }
		}
		else if( field.equals("initdate") ){
			if( mode ){ return getSQLDateFormat("h.fcst_init"); }
			else      { return getSQLDateFormat("h." + _strInitdateField); }
		}
		else if( field.equals("fcst_init_beg") ) { return getSQLDateFormat("h.fcst_init_beg");  }
		else if( field.equals("fcst_init") )     { return getSQLDateFormat("h.fcst_init");      }
		else if( field.equals("fcst_valid_beg") ){ return getSQLDateFormat("h.fcst_valid_beg"); }
		else if( field.equals("fcst_valid") )    { return getSQLDateFormat("h.fcst_valid");     }
		else									 { return "h." + field;                         }
	}
	
	public static String formatSQLConstraint(String value){
		String strRet = " = '" + value + "'";
		
		Matcher matBetween = _patDateRange.matcher(value);
		if( matBetween.matches() ){ strRet = " " + value; }
		
		return strRet;
	}

	public static MVDataTable getUniqueModeObjRows(MVDataTable table, String field, String objPattern){
		final String strField = field;
		final String strObjPattern = objPattern;
		final MVOrderedMap mapObjRow = new MVOrderedMap();
		return table.getRows(
				new MVRowComp(){
					public boolean equals(MVOrderedMap row){
						String strSimpFcstVal = row.getStr("fcst_valid") + "#" + row.getStr(strField);
						if( !row.getStr(strField).matches(strObjPattern) || mapObjRow.containsKey(strSimpFcstVal) ){ return false; }
						mapObjRow.put(strSimpFcstVal, "TRUE");
						return true;
					}
				}
		);
	}
	
	
	/**
	 * Mapping from stat_name to stat_group_lu_id, used in SQL queries
	 */
	public static final Hashtable _tableStatIndex = new Hashtable();
	static{
		_tableStatIndex.put("BASER", 		"0");
		_tableStatIndex.put("FMEAN", 		"1");
		_tableStatIndex.put("ACC", 			"2");
		_tableStatIndex.put("FBIAS", 		"3");
		_tableStatIndex.put("PODY", 		"4");
		_tableStatIndex.put("PODN", 		"5");
		_tableStatIndex.put("POFD", 		"6");
		_tableStatIndex.put("FAR", 			"7");
		_tableStatIndex.put("CSI", 			"8");
		_tableStatIndex.put("GSS", 			"9");
		_tableStatIndex.put("HK", 			"10");
		_tableStatIndex.put("HSS", 			"11");
		_tableStatIndex.put("ODDS", 		"12");
		_tableStatIndex.put("FBAR", 		"13");
		_tableStatIndex.put("FSTDEV", 		"14");
		_tableStatIndex.put("OBAR", 		"15");
		_tableStatIndex.put("OSTDEV",		"16");
		_tableStatIndex.put("PR_CORR",		"17");
		_tableStatIndex.put("ME", 			"18");
		_tableStatIndex.put("ESTDEV", 		"19");
		_tableStatIndex.put("MBIAS", 		"20");
		_tableStatIndex.put("MAE", 			"21");
		_tableStatIndex.put("MSE", 			"22");
		_tableStatIndex.put("BCMSE", 		"23");
		_tableStatIndex.put("BCRMSE", 		"23");
		_tableStatIndex.put("RMSE", 		"24");
		_tableStatIndex.put("E10", 			"25");
		_tableStatIndex.put("E25", 			"26");
		_tableStatIndex.put("E50", 			"27");
		_tableStatIndex.put("E75", 			"28");
		_tableStatIndex.put("E90", 			"29");
		_tableStatIndex.put("BRIER", 		"30");
		_tableStatIndex.put("RELIABILITY",	"31");
		_tableStatIndex.put("RESOLUTION",	"32");
		_tableStatIndex.put("UNCERTAINTY",	"33");
		_tableStatIndex.put("ROC_AUC", 		"34");
		_tableStatIndex.put("NBR_BASER",	"35");
		_tableStatIndex.put("NBR_FMEAN",	"36");
		_tableStatIndex.put("NBR_ACC", 		"37");
		_tableStatIndex.put("NBR_FBIAS",	"38");
		_tableStatIndex.put("NBR_PODY", 	"39");
		_tableStatIndex.put("NBR_PODN", 	"40");
		_tableStatIndex.put("NBR_POFD",		"41");
		_tableStatIndex.put("NBR_FAR", 		"42");
		_tableStatIndex.put("NBR_CSI", 		"43");
		_tableStatIndex.put("NBR_GSS", 		"44");
		_tableStatIndex.put("NBR_HK", 		"45");
		_tableStatIndex.put("NBR_HSS", 		"46");
		_tableStatIndex.put("NBR_ODDS",		"47");
		_tableStatIndex.put("NBR_FBS", 		"48");
		_tableStatIndex.put("NBR_FSS", 		"49");
	}

	public static final Hashtable _tableModeStatIndex = new Hashtable();
	static{
		_tableModeStatIndex.put("ACOV",		"0");
		_tableModeStatIndex.put("PERC", 	"1");
		_tableModeStatIndex.put("MIA", 		"2");
		_tableModeStatIndex.put("MAR", 		"3");
		_tableModeStatIndex.put("MCD", 		"4");
		_tableModeStatIndex.put("MAD", 		"5");
		_tableModeStatIndex.put("P50", 		"6");
		_tableModeStatIndex.put("P90", 		"7");
		_tableModeStatIndex.put("MMI", 		"8");
		_tableModeStatIndex.put("MMIF",		"9");
		_tableModeStatIndex.put("MMIO",		"10");
	}
	
	public static final Hashtable _tableStatLine = new Hashtable();
	static{
		_tableStatLine.put("BASER", 	"CTS");
		_tableStatLine.put("FMEAN", 	"CTS");
		_tableStatLine.put("ACC", 		"CTS");
		_tableStatLine.put("FBIAS", 	"CTS");
		_tableStatLine.put("PODY", 		"CTS");
		_tableStatLine.put("PODN", 		"CTS");
		_tableStatLine.put("POFD", 		"CTS");
		_tableStatLine.put("FAR", 		"CTS");
		_tableStatLine.put("CSI", 		"CTS");
		_tableStatLine.put("GSS", 		"CTS");
		_tableStatLine.put("HK", 		"CTS");
		_tableStatLine.put("HSS", 		"CTS");
		_tableStatLine.put("ODDS", 		"CTS");
		_tableStatLine.put("FBAR", 		"CNT");
		_tableStatLine.put("FSTDEV", 	"CNT");
		_tableStatLine.put("OBAR", 		"CNT");
		_tableStatLine.put("OSTDEV",	"CNT");
		_tableStatLine.put("PR_CORR",	"CNT");
		_tableStatLine.put("ME", 		"CNT");
		_tableStatLine.put("ESTDEV", 	"CNT");
		_tableStatLine.put("MBIAS", 	"CNT");
		_tableStatLine.put("MAE", 		"CNT");
		_tableStatLine.put("MSE", 		"CNT");
		_tableStatLine.put("BCMSE", 	"CNT");
		_tableStatLine.put("BCRMSE", 	"CNT");
		_tableStatLine.put("RMSE", 		"CNT");
		_tableStatLine.put("E10", 		"CNT");
		_tableStatLine.put("E25", 		"CNT");
		_tableStatLine.put("E50", 		"CNT");
		_tableStatLine.put("E75", 		"CNT");
		_tableStatLine.put("E90", 		"CNT");
		_tableStatLine.put("BRIER", 	"PSTD");
		_tableStatLine.put("NBR_BASER",	"NBRCTS");
		_tableStatLine.put("NBR_FMEAN",	"NBRCTS");
		_tableStatLine.put("NBR_ACC", 	"NBRCTS");
		_tableStatLine.put("NBR_FBIAS",	"NBRCTS");
		_tableStatLine.put("NBR_PODY",	"NBRCTS");
		_tableStatLine.put("NBR_PODN",	"NBRCTS");
		_tableStatLine.put("NBR_POFD",	"NBRCTS");
		_tableStatLine.put("NBR_FAR", 	"NBRCTS");
		_tableStatLine.put("NBR_CSI", 	"NBRCTS");
		_tableStatLine.put("NBR_GSS", 	"NBRCTS");
		_tableStatLine.put("NBR_HK", 	"NBRCTS");
		_tableStatLine.put("NBR_HSS", 	"NBRCTS");
		_tableStatLine.put("NBR_ODDS",	"NBRCTS");
		_tableStatLine.put("NBR_FBS", 	"NBRCNT");
		_tableStatLine.put("NBR_FSS", 	"NBRCNT");
	}

	/**
	 * Mapping from fcst_var to fcst_lev surface values
	 */
	public static final Hashtable _tableFcstLevSurface = new Hashtable();
	static{
		_tableFcstLevSurface.put("TMP", "Z2");
		_tableFcstLevSurface.put("DPT", "Z2");
		_tableFcstLevSurface.put("WIND", "Z10");
		_tableFcstLevSurface.put("APCP_03", "A3");
		_tableFcstLevSurface.put("APCP_24", "APCP_03");
	}
}



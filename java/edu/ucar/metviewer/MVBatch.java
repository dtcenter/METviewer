package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
//import edu.ucar.metviewer.hmt.*;
//import org.apache.log4j.*;

public class MVBatch extends MVUtil {
	
	public PrintStream _out				= System.out;
	
	public boolean _boolSQLOnly			= false;
	public boolean _boolVerbose			= false;
	
	public String _strRtmplFolder		= "/home/pgoldenb/apps/verif/metviewer/R_tmpl/";
	public String _strRworkFolder		= "/d1/pgoldenb/var/hmt/R_work/";
	public String _strPlotsFolder		= "/d1/pgoldenb/var/hmt/plots/";

	public boolean _boolProcWait		= true;
	public long _intProcSleep			= 500;

	public static final Pattern _patRTmpl		= Pattern.compile("#<(\\w+)>#");
	public static final Pattern _patDateRange	= Pattern.compile("(?i)\\s*between\\s+'([^']+)'\\s+and\\s+'([^']+)'\\s*");
	
	public final boolean _boolPlot		= true;
	public boolean _boolSQLSort			= true;
	public boolean _boolCacheAggStat	= true;
	
	public int _intNumPlots				= 0;
	public int _intPlotIndex			= 0;
	public int _intNumPlotsRun			= 0;

	public static boolean _boolWindows	= false;
	
	public MVBatch(PrintStream log, boolean boolWindows){
		_out = log;
		_boolWindows = boolWindows;
		
		//  windows settings
		if( _boolWindows ){
			_strRtmplFolder = "c:/src/apps/verif/metviewer/R_tmpl/";
			_strRworkFolder = "c:/src/metv/R_work/";
			_strPlotsFolder = "c:/src/metv/plots/";
			_boolProcWait = false;
		}
	}
	public MVBatch(PrintStream log){ this(log, false); }
	public MVBatch(){ this(System.out, false); }
	
	public static String getUsage(){
		return	"Usage:  mv_batch\n" +
				"          [-list]\n" +
				"          [-v]\n" +
				"          [-sql]\n" +
				"          plot_spec_file\n" +
				"          [job_name]\n" +
				"\n" +
				"        where     \"-list\" indicates that the available plot jobs should be listed and no plots run\n" +
				"                  \"-v\" indicates verbose output\n" +
				"                  \"-sql\" indicates that the queries for each plot jobs should be listed and no plots run\n" +
				"                  \"plot_spec_file\" specifies the XML plot specification document\n" +
				"                  \"job_name\" specifies the name of the job from the plot specification to run\n";
	}	
	
	public static void main(String[] argv) {
		MVBatch bat = new MVBatch();
		
		bat._out.println("----  MVBatch  ----\n");
		Connection con = null;
		
		try{
		
			MVPlotJob[] jobs = {};
		
			//  if no input file is present, bail
			if( 1 > argv.length ){
				bat._out.println(getUsage() + "\n----  MVBatch Done  ----");
				try{ if( con != null )	con.close(); }catch(SQLException e){}
				return;
			}

			//  parse the command line options
			boolean boolList = false;
			int intArg = 0;
			for(; intArg < argv.length && !argv[intArg].matches(".*\\.xml$"); intArg++){
				if     ( argv[intArg].equals("-list") ){ boolList = true; }
				else if( argv[intArg].equals("-sql")  ){ bat._boolSQLOnly = true; }
				else if( argv[intArg].equals("-v")    ){ bat._boolVerbose = true; }
				else{
					System.out.println("  **  ERROR: unrecognized option '" + argv[intArg] + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
					return;
				}
			}
			bat._boolVerbose = (bat._boolSQLOnly? true : bat._boolVerbose);

			//  parse the input file
			String strXMLInput = argv[intArg++];
			if( !bat._boolSQLOnly ){ bat._out.println("input file: " + strXMLInput + "\n"); }				
			MVPlotJobParser parser = new MVPlotJobParser(strXMLInput, con);
			MVOrderedMap mapJobs = parser.getJobsMap();

			//  build a list of jobs to run
			ArrayList listJobNamesInput = new ArrayList();
			for(; intArg < argv.length; intArg++){ listJobNamesInput.add(argv[intArg]); }
			String[] listJobNames = mapJobs.getKeyList();
			if( 0 < listJobNamesInput.size() ){
				listJobNames = toArray(listJobNamesInput);
			}
			if( !bat._boolSQLOnly ){ 
				bat._out.println( (boolList? "" : "processing ") + listJobNames.length + " jobs:");
				for(int i=0; i < listJobNames.length; i++){ bat._out.println("  " + listJobNames[i]); }
			}
			
			//  if only a list of plot jobs is requested, return
			if( boolList ){
				bat._out.println("\n----  MVBatch Done  ----");
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
						bat._out.println("  **  WARNING: unrecognized job \"" + listJobNames[i] + "\"");
						continue;
					}
					listJobs.add( mapJobs.get(listJobNames[i]) );
				}
				jobs = (MVPlotJob[])listJobs.toArray(new MVPlotJob[]{});
			}
			
			//  get the path information for the job
			if( !parser.getRtmplFolder().equals("") ){ bat._strRtmplFolder = parser.getRtmplFolder(); }
			if( !parser.getRworkFolder().equals("") ){ bat._strRworkFolder = parser.getRworkFolder(); }
			if( !parser.getPlotsFolder().equals("") ){ bat._strPlotsFolder = parser.getPlotsFolder(); }

			//  if on windows, change all plot image types to jpeg
			if( _boolWindows ){
				for(int i=0; i < jobs.length; i++){ jobs[i].setPlotType("jpeg"); }
			}
			
			//  calculate the number of plots
			bat._intNumPlots = 0;
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

				bat._intNumPlots += intNumJobPlots;
			}
			java.util.Date dateStart = new java.util.Date();
			if( !bat._boolSQLOnly ){
				bat._out.println("Running " + bat._intNumPlots + " plots\n" + "Begin time: " + _formatDB.format(dateStart) + "\n");
			}
			
			for(int intJob=0; intJob < jobs.length; intJob++){
				if( jobs[intJob].getPlotTmpl().equals("rhist.R_tmpl") ){
					bat.runRhistJob(jobs[intJob]);
				} else {
					bat.runJob(jobs[intJob]);
				}
			}

			java.util.Date dateEnd = new java.util.Date();
			long intPlotTime = dateEnd.getTime() - dateStart.getTime();
			long intPlotAvg = (0 < bat._intNumPlots? intPlotTime / (long)bat._intNumPlots : 0);
			if( !bat._boolSQLOnly ){ 
				bat._out.println("\n" + 
								   padBegin("End time: ") + _formatDB.format(dateEnd) + "\n" +
								   padBegin("Plots run: ") + bat._intNumPlotsRun + " of " + bat._intNumPlots + "\n" +
								   padBegin("Total time: ") + formatTimeSpan(intPlotTime) + "\n" +
								   padBegin("Avg plot time: ") + formatTimeSpan(intPlotAvg) + "\n");
			}


		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null )	con.close(); }catch(SQLException e){}
		}

		bat._out.println("\n----  MVBatch Done  ----");
	}
	
	public void runJob(MVPlotJob job) throws Exception {
		
		MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
		MVOrderedMap mapTmplVals = job.getTmplVal();

		//  build a list of fixed value permutations for all plots
		MVOrderedMap[] listPlotFixPerm = buildFixValList(mapPlotFixVal);

		//  run the plot jobs once for each permutation of plot fixed values
		for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++){

			//  populate the plot template values with fixed values
			Map.Entry[] listPlotFixVal = buildFixValTmplMap(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);
			
			//  if the independent variable uses a dependency, populate the values
			MVPlotDep depIndy = job.getIndyDep();
			if( null != depIndy ){
				String strDep = "";
				if( mapTmplVals.containsKey(depIndy.getDepVar()) ){
					strDep = _formatDB.format( _formatPlot.parse( mapTmplVals.getStr(depIndy.getDepVar()) ) );
				}
				String[][] listIndy = MVPlotJobParser.parseIndyNode(depIndy.getSpec(), strDep);
				job.setIndyVal(listIndy[0]);
				job.setIndyLabel(listIndy[1]);
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

				//  build the aggregation variable permutations, ensuring there is at least one permutation
				MVOrderedMap[] listAggPerm = permute(job.getAggVal()).getRows();
				if( 1 > listAggPerm.length ){ listAggPerm = new MVOrderedMap[]{new MVOrderedMap()}; }

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
				for(int i=0; i < listDepPlot.length; i++){
					String[] listStats = (String[])listDepPlot[i].getValue();
					for(int j=0; j < listStats.length; j++){
						if( _tableModeStatIndex.containsKey( parseModeStat(listStats[j])[0] ) ){ boolModePlot = true; }
						else if( !_tableStatIndex.containsKey(listStats[j]) ){ throw new Exception("statistic " + listStats[j] + " not found"); }
					}
				}
				
				
				/*
				 *  Build the plot query SQL
				 */
										
				//  build a comma delimited lists of the query fields as the select and sort lists
				String strSelectList = "", strSortList = "", strTempList = "";
				Map.Entry[] listQueryFields = append( append(listAggVal, listSeries1Val), listSeries2Val );
				Hashtable tableFields = new Hashtable();
				for(int i=0; i < listQueryFields.length; i++){
					String strQueryField = (String)listQueryFields[i].getKey();
					String strSelectVar = strQueryField;
					if( tableFields.containsKey(strSelectVar) ){ continue; }
					tableFields.put(strSelectVar, "true");
					
					//  format the field, depending on its type
					if( strSelectVar.equals("init_hour") ){
						strSelectVar = (boolModePlot? "HOUR(h.fcst_init) init_hour" : "HOUR(h.fcst_init_beg) init_hour");
					} else if( strSelectVar.equals("valid_hour") ){
						strSelectVar = (boolModePlot? "HOUR(h.fcst_valid) valid_hour" : "HOUR(h.fcst_valid_beg) valid_hour");
					} else if( strSelectVar.equals("fcst_valid_beg") ){
						strSelectVar = getSQLDateFormat("h.fcst_valid_beg") + " fcst_valid_beg";
					} else if( strSelectVar.equals("fcst_valid") ){
						strSelectVar = getSQLDateFormat("h.fcst_valid") + " fcst_valid";
					} else if( strSelectVar.equals("fcst_init_beg") ){
						strSelectVar = getSQLDateFormat("h.fcst_init_beg") + " fcst_init_beg";
					} else if( strSelectVar.equals("fcst_init") ){
						strSelectVar = getSQLDateFormat("h.fcst_init") + " fcst_init";
					} else {
						strSelectVar = "h." + strSelectVar;
					}
					strSelectList += (0 < i? ",\n" : "") + "  " + strSelectVar;
					strSortList += (0 < i? ",\n" : "") + "  " + strQueryField;
					
					//  add the field to the temp table field list
					String strTempType = "";
					if( boolModePlot ){
						if( !_tableModeHeaderSQLType.containsKey(strQueryField) ){ throw new Exception("unrecognized mode_header field: " + strQueryField); }
						strTempType = _tableModeHeaderSQLType.get(strQueryField).toString();
					} else {
						if( !_tableStatHeaderSQLType.containsKey(strQueryField) ){ throw new Exception("unrecognized stat_header field: " + strQueryField); }
						strTempType = _tableStatHeaderSQLType.get(strQueryField).toString();
					}
					strTempList += "    " + padEnd(strQueryField, 20) + strTempType + ",\n";
				}
				String strSelectListModeTemp = strSelectList;
				
				//  add contingency table stats or partial sums for agg_stat calculations, if necessary
				boolean boolAggCtc = job.getAggCtc();
				boolean boolAggSl1l2 = job.getAggSl1l2();
				boolean boolAggStat = boolAggCtc || boolAggSl1l2;
				if( boolAggCtc ){
					strSelectList += ",\n  ldctc.total,\n  ldctc.fy_oy,\n  ldctc.fy_on,\n  ldctc.fn_oy,\n  ldctc.fn_on";
					strTempList += "    total               INT UNSIGNED,\n" +
								   "    fy_oy               DOUBLE,\n" +
								   "    fy_on               DOUBLE,\n" +
								   "    fn_oy               DOUBLE,\n" +
								   "    fn_on               DOUBLE,\n";
				} else if( boolAggSl1l2 ){
					strSelectList += ",\n  ldsl1l2.total,\n  ldsl1l2.fbar,\n  ldsl1l2.obar,\n  ldsl1l2.fobar,\n  ldsl1l2.ffbar,\n  ldsl1l2.oobar";
					strTempList += "    total               INT UNSIGNED,\n" +
								   "    fbar                DOUBLE,\n" +
								   "    obar                DOUBLE,\n" +
								   "    fobar               DOUBLE,\n" +
								   "    ffbar               DOUBLE,\n" +
								   "    oobar               DOUBLE,\n";
				}
				
				//  add confidence interval stats, if necessary				
				else {
					boolean boolNormalCI = false, boolBootCI = false;
					String[] listPlotCI = parseRCol(job.getPlotCI()); 
					for(int i=0; i < listPlotCI.length; i++){
						if     ( listPlotCI[i].equals("norm") ){ boolNormalCI = true; }
						else if( listPlotCI[i].equals("boot") ){ boolBootCI   = true; }
					}
					if( boolNormalCI ){
						strSelectList += ",\n  sg.stat_ncl,\n  sg.stat_ncu";
						strTempList += "    stat_ncl            DOUBLE,\n" +
									   "    stat_ncu            DOUBLE,\n";
					}
					if( boolBootCI ){
						strSelectList += ",\n  sg.stat_bcl,\n  sg.stat_bcu";
						strTempList += "    stat_bcl            DOUBLE,\n" +
									   "    stat_bcu            DOUBLE,\n";
					}
					
					//  if nobs is requested, add baserate and total
					if( 0 < listSeriesNobs.length ){
						strSelectList += ",\n  (ldcts.total * sgb.stat_value) nobs,\n  ldcts.total";
						strTempList += "    nobs                INT UNSIGNED,\n" +
									   "    total               INT UNSIGNED,\n";
					}
				}
	
				//  add init time, valid time, forecast variable, independent variable and stat group to the list
				strSelectList += ",\n  h.fcst_var";
				strSelectList += ",\n  " + formatField(job.getIndyVar(), boolModePlot) + " " + job.getIndyVar();
				//if( !strSelectList.contains("fcst_init") ){
				if( !strSelectList.matches("(?s).*fcst_init\\w*,.*") ){
					if( boolModePlot ){ strSelectList += ",\n  " + getSQLDateFormat("h.fcst_init") + " fcst_init"; }
					else              { strSelectList += ",\n  " + getSQLDateFormat("h.fcst_init_beg") + " fcst_init_beg"; }
				}
				if( !strSelectList.matches("(?s).*fcst_valid\\w*,.*") ){
					if( boolModePlot ){ strSelectList += ",\n  " + getSQLDateFormat("h.fcst_valid") + " fcst_valid"; }
					else              { strSelectList += ",\n  " + getSQLDateFormat("h.fcst_valid_beg") + " fcst_valid_beg"; }
				}
				if( boolAggStat ){ strSelectList += ",\n  0 stat_group_lu_id,\n  '' stat_name,\n  0 stat_value"; }
				else             { strSelectList += ",\n  sg.stat_group_lu_id,\n  '' stat_name,\n  sg.stat_value"; }
				
				//  build the list of tables for the FROM clause, not used by mode plots
				String strFromList = "";			
				if( !boolModePlot ){
					strFromList = "  stat_header h";
					if     ( boolAggCtc )  { strFromList += ",\n  line_data_ctc ldctc";     }
					else if( boolAggSl1l2 ){ strFromList += ",\n  line_data_sl1l2 ldsl1l2"; }
					else                   { strFromList += ",\n  stat_group sg";           }
									
					//  if nobs is requested, add baserate and total
					if( 0 < listSeriesNobs.length ){ strFromList += ",\n  stat_group sgb,\n  line_data_cts ldcts"; }
				}
				
				//  build the where clause from the tables of field names and values
				String strWhere = buildAggWhere(new Map.Entry[]{}, listPlotFixVal, boolModePlot);

				//  add the independent variable values, if necessary
				if( 0 < job.getIndyVal().length ){
					strWhere += (!strWhere.equals("")? "  AND " : "") + formatField(job.getIndyVar(), boolModePlot, false) + 
								" IN (" + buildValueList(job.getIndyVal()) + ")\n";
				}
	
				//  build the dependent variable where clause
				MVOrderedMap mapFcstVarPat = new MVOrderedMap();
				strWhere += "  AND\n  (\n";				
				for(int i=0; i < listDepPlot.length; i++){
					String strFcstVar = (String)listDepPlot[i].getKey();
					String[] listStatGroupName = (String[])listDepPlot[i].getValue();
					String[] listStatGroupLuId = new String[listStatGroupName.length];
					String strDepStatClause = "";
					if( !boolModePlot && !boolAggStat ){
						for(int j=0; j < listStatGroupName.length; j++){ listStatGroupLuId[j] = (String)_tableStatIndex.get(listStatGroupName[j]); }
						strDepStatClause = "      AND sg.stat_group_lu_id IN (" +	buildValueList(listStatGroupLuId) + ")\n";
					}
					
					//  build the fcst_var criteria
					String strFcstVarClause = " = '" + strFcstVar + "'";
					Matcher matProb = _patProb.matcher(strFcstVar);
					if( matProb.matches() && strFcstVar.contains("*") ){
						mapFcstVarPat.put( Pattern.compile(strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)")), formatR(strFcstVar) );
						strFcstVarClause = " LIKE '" + strFcstVar.replace("*", "%") + "'";
					}
										
					//  fixed field sql
					String strFixed = "";
					if( null != mapFix && mapFix.containsKey(strFcstVar) ){
						MVOrderedMap mapFixed = (MVOrderedMap)mapFix.get(strFcstVar);
						Map.Entry[] listFixed = mapFixed.getOrderedEntries();
						for(int j=0; j < listFixed.length; j++){
							String strField = (String)listFixed[j].getKey();
							String strValue = (String)listFixed[j].getValue();
							strFixed += "      AND " + formatField(strField, boolModePlot, false) + formatSQLConstraint(strValue) + "\n";
						}
					}
					
					//  build the series fields where clause
					String strSeries = "";
					Map.Entry[] listSeriesVal = (i < listDep1Plot.length? listSeries1Val : listSeries2Val);
					for(int j=0; j < listSeriesVal.length; j++){
						String strField = (String)listSeriesVal[j].getKey();
						String[] listValues = (String[])listSeriesVal[j].getValue();
						strSeries += "      AND " + formatField(strField, boolModePlot, false) + " IN (" + buildValueList(listValues) + ")\n";
					}
					
					strWhere += (0 < i? "    OR\n" : "") + "    (\n      h.fcst_var" + strFcstVarClause + "\n" +
								strDepStatClause + strFixed + strSeries + "    )\n";
				}
				String strWhereModeTemp = strWhere + "  )";
								
				//  add the table joining clauses, not used by mode plots
				if( !boolModePlot ){
					if( boolAggCtc ){
						strWhere += "  )\n  AND h.stat_header_id = ldctc.stat_header_id";
					} else if( boolAggSl1l2 ){
						strWhere += "  )\n  AND h.stat_header_id = ldsl1l2.stat_header_id";
					} else {
						strWhere += "  )\n  AND h.stat_header_id = sg.stat_header_id\n  AND sg.stat_value != -9999";
	
						if( 0 < listSeriesNobs.length ){
							strWhere += "\n  AND sgb.stat_group_lu_id = '0'\n" +
										  "  AND h.stat_header_id = sgb.stat_header_id\n" +
										  "  AND h.stat_header_id = ldcts.stat_header_id";
						}			
					}
				}

				//  add date information, the fcst_var and the independent field to the temp table field list
				String strIndyVarType = "";
				String strIndyVar = job.getIndyVar();
				if( boolModePlot ){
					if( !_tableModeHeaderSQLType.containsKey(strIndyVar) ){ throw new Exception("unrecognized indep mode_header field: " + strIndyVar); }
					strIndyVarType = _tableModeHeaderSQLType.get(strIndyVar).toString();
				} else {
					if( !_tableStatHeaderSQLType.containsKey(strIndyVar) ){ throw new Exception("unrecognized indep stat_header field: " + strIndyVar); }
					strIndyVarType = _tableStatHeaderSQLType.get(strIndyVar).toString();
				}
				strTempList += "    fcst_var            VARCHAR(64),\n" +
							   "    " + padEnd(job.getIndyVar(), 20) + strIndyVarType + ",\n";
				if( boolModePlot ){
					strTempList += ( !strTempList.contains("fcst_init")?  "    fcst_init           DATETIME,\n" : "");
					strTempList += ( !strTempList.contains("fcst_valid")? "    fcst_valid          DATETIME,\n" : "");
				} else {
					strTempList += ( !strTempList.contains("fcst_init")?  "    fcst_init_beg       DATETIME,\n" : "");
					strTempList += ( !strTempList.contains("fcst_valid")? "    fcst_valid_beg      DATETIME,\n" : "");
				}
	
				//  build the temp table for the plot data
				ArrayList listQuery = new ArrayList();
				listQuery.add("DROP TEMPORARY TABLE IF EXISTS job_data;");
				listQuery.add(
					"CREATE TEMPORARY TABLE job_data\n" +
					"(\n" +
					strTempList +
					"    stat_group_lu_id    INT UNSIGNED,\n" +
					"    stat_name           VARCHAR(32),\n" +
					"    stat_value          DOUBLE\n" +
					");");
					
				//  if the plots are not mode stats, combine the query components into a single SELECT
				if( !boolModePlot ){					
					listQuery.add( 
						"INSERT INTO job_data\n" +
						"SELECT\n" + strSelectList + "\n" +
						"FROM\n" + strFromList + "\n" +
						"WHERE\n" + strWhere + 
						(_boolSQLSort? "\nORDER BY\n" + strSortList : "") + ";");
				}
				
				//  if the plots are mode plots, build a series of temp tables to aggregate the data 
				else {
					
					//  add the object information to the temp table field list
					String strTempListMode = strTempList + 
						"    object_id           VARCHAR(128),\n" +
						"    object_cat          VARCHAR(128),\n";
					
					//  build the list of fields for the mode stats
					strSelectListModeTemp += ",\n";
					strSelectListModeTemp += "  h.fcst_var,\n  " + formatField(job.getIndyVar(), boolModePlot) + ",\n";
					strSelectListModeTemp += ( !strSelectListModeTemp.contains("fcst_init,")?  "  h.fcst_init,\n" : "");
					strSelectListModeTemp += ( !strSelectListModeTemp.contains("fcst_valid,")? "  h.fcst_valid,\n" : "");					
					
					
					String[] listModeSingleTables = {"mode_single", "mode_single2"};
					for(int i=0; i < listModeSingleTables.length; i++){
						listQuery.add("DROP TEMPORARY TABLE IF EXISTS " + listModeSingleTables[i] + ";");
						listQuery.add(
							"CREATE TEMPORARY TABLE " + listModeSingleTables[i] + "\n" +
							"(\n" +
							strTempListMode +
							"    centroid_x          DOUBLE,\n" +
							"    centroid_y          DOUBLE,\n" +
							"    centroid_lat        DOUBLE,\n" +
							"    centroid_lon        DOUBLE,\n" +
							"    axis_avg            DOUBLE,\n" +
							"    length              DOUBLE,\n" +
							"    width               DOUBLE,\n" +
							"    area                INT UNSIGNED,\n" +
							"    area_filter         INT UNSIGNED,\n" +
							"    area_thresh         INT UNSIGNED,\n" +
							"    curvature           DOUBLE,\n" +
							"    curvature_x         DOUBLE,\n" +
							"    curvature_y         DOUBLE,\n" +
							"    complexity          DOUBLE,\n" +
							"    intensity_10        DOUBLE,\n" +
							"    intensity_25        DOUBLE,\n" +
							"    intensity_50        DOUBLE,\n" +
							"    intensity_75        DOUBLE,\n" +
							"    intensity_90        DOUBLE,\n" +
							"    intensity_nn        DOUBLE,\n" +
							"    intensity_sum       DOUBLE,\n" +
							"    total               INT UNSIGNED,\n" +
						    "    fcst_flag           BOOLEAN,\n" +
						    "    simple_flag         BOOLEAN,\n" +
						    "    matched_flag        BOOLEAN,\n" +
							"    INDEX (fcst_valid),\n" +
							"    INDEX (object_id),\n" +
							"    INDEX (object_cat)\n" +
							");");
					}

					//  insert information from mode_obj_single into the temp tables with header data
					listQuery.add(
						"INSERT INTO mode_single\n" +
						"SELECT\n" + strSelectListModeTemp +
						"  mos.object_id,\n" +
						"  mos.object_cat,\n" +
						"  mos.centroid_x,\n" +
						"  mos.centroid_y,\n" +
						"  mos.centroid_lat,\n" +
						"  mos.centroid_lon,\n" +
						"  mos.axis_avg,\n" +
						"  mos.length,\n" +
						"  mos.width,\n" +
						"  mos.area,\n" +
						"  mos.area_filter,\n" +
						"  mos.area_thresh,\n" +
						"  mos.curvature,\n" +
						"  mos.curvature_x,\n" +
						"  mos.curvature_y,\n" +
						"  mos.complexity,\n" +
						"  mos.intensity_10,\n" +
						"  mos.intensity_25,\n" +
						"  mos.intensity_50,\n" +
						"  mos.intensity_75,\n" +
						"  mos.intensity_90,\n" +
						"  mos.intensity_nn,\n" +
						"  mos.intensity_sum,\n" +
						"  mc.total,\n" +
						"  IF(mos.object_id REGEXP '^C?F[[:digit:]]{3}$', 1, 0) fcst_flag,\n" +
						"  IF(mos.object_id REGEXP '^[FO][[:digit:]]{3}$', 1, 0) simple_flag,\n" +
						"  IF(mos.object_cat REGEXP '^C[FO]000$', 0, 1) matched_flag\n" +
						"FROM\n" +
						"  mode_header h,\n" +
						"  mode_obj_single mos,\n" +
						"  mode_cts mc\n" +
						"WHERE\n" + strWhereModeTemp + "\n" +
						"  AND mos.mode_header_id = h.mode_header_id\n" +
						"  AND mc.mode_header_id = mos.mode_header_id\n" +
						"  AND mc.field = 'OBJECT';");
					listQuery.add("INSERT INTO mode_single2 SELECT * FROM mode_single;");
							
					listQuery.add("DROP TEMPORARY TABLE IF EXISTS mode_pair;");
					listQuery.add(
						"CREATE TEMPORARY TABLE mode_pair\n" +
						"(\n" +
						strTempListMode +
						"    centroid_dist       DOUBLE,\n" +
						"    boundary_dist       DOUBLE,\n" +
						"    convex_hull_dist    DOUBLE,\n" +
						"    angle_diff          DOUBLE,\n" +
						"    area_ratio          DOUBLE,\n" +
						"    intersection_area   INT UNSIGNED,\n" +
						"    union_area          INT UNSIGNED,\n" +
						"    symmetric_diff      INTEGER,\n" +
						"    intersection_over_area DOUBLE,\n" +
						"    complexity_ratio    DOUBLE,\n" +
						"    percentile_intensity_ratio DOUBLE,\n" +
						"    interest            DOUBLE,\n" +
						"    simple_flag         BOOLEAN,\n" +
						"    matched_flag        BOOLEAN,\n" +
						"    INDEX (fcst_valid),\n" +
						"    INDEX (object_id),\n" +
						"    INDEX (object_cat)\n" +
						");");

					listQuery.add(
							"INSERT INTO mode_pair\n" +
							"SELECT\n" + strSelectListModeTemp +
							"  mop.object_id,\n" +
							"  mop.object_cat,\n" +
							"  mop.centroid_dist,\n" +
							"  mop.boundary_dist,\n" +
							"  mop.convex_hull_dist,\n" +
							"  mop.angle_diff,\n" +
							"  mop.area_ratio,\n" +
							"  mop.intersection_area,\n" +
							"  mop.union_area,\n" +
							"  mop.symmetric_diff,\n" +
							"  mop.intersection_over_area,\n" +
							"  mop.complexity_ratio,\n" +
							"  mop.percentile_intensity_ratio,\n" +
							"  mop.interest,\n" +
							"  IF(mop.object_id REGEXP '^F[[:digit:]]{3}_O[[:digit:]]{3}$', 1, 0) simple_flag,\n" +
							//"  IF(mop.interest >= 0.7, 1, 0) matched_flag\n" +
							"  IF(mop.interest >= 0, 1, 0) matched_flag\n" +
							"FROM\n" +
							"  mode_header h,\n" +
							"  mode_obj_pair mop\n" +
							"WHERE\n" + strWhereModeTemp + "\n" +
							"  AND mop.mode_header_id = h.mode_header_id;");

					//  build a table to store the calculated mode statistics 
					listQuery.add("DROP TEMPORARY TABLE IF EXISTS mode_stat;");
					listQuery.add(
						"CREATE TEMPORARY TABLE mode_stat\n" +
						"(\n" +
						strTempListMode +
						"    stat_name           VARCHAR(32),\n" +
						"    stat_value          DOUBLE\n" +
						");");

					//  construct a query for the dependent variable statistics for the y1 and y2 axes 
					for(int intY=1; intY <= 2; intY++){
						Map.Entry[] listSeriesMode = (1 == intY? listSeries1Val : listSeries2Val);
						MVOrderedMap[] listMapDepMode = (1 == intY? listMapDep1Mode : listMapDep2Mode);
						if( 1 >  listMapDepMode.length ){ listMapDepMode = new MVOrderedMap[]{ (1 == intY? mapDep1 : mapDep2) }; }
						
						//  build a list of the group by variables, the independent variable plus series variables
						ArrayList listGroupFields = new ArrayList();
						listGroupFields.add(job.getIndyVar());
						for(int i=0; i < listSeriesMode.length; i++){
							listGroupFields.add( listSeriesMode[i].getKey().toString() );
						}
						String[] listGroupBy = (String[])listGroupFields.toArray(new String[]{});

						//  for each member of the dep list, build queries for all the statistics
						for(int intDepMode=0; intDepMode < listMapDepMode.length; intDepMode++){ 
							Map.Entry[] listDepMode = listMapDepMode[intDepMode].getOrderedEntries();						
							for(int intFcstVar=0; intFcstVar < listDepMode.length; intFcstVar++){
								String[] listModeStats =  (String[])listDepMode[intFcstVar].getValue();
								for(int intStatMode=0; intStatMode < listModeStats.length; intStatMode++){
									
									//  build the appropriate type of query, depending on the statistic
									String[] listStatComp = parseModeStat(listModeStats[intStatMode]);
									if( _tableModeSingleStatField.containsKey(listStatComp[0]) ){
										if( !listStatComp[1].startsWith("D") ){
											listQuery.add( buildModeSingleStatTable(strSelectListModeTemp, listModeStats[intStatMode], listGroupBy) );
										} else {
											listQuery.add( buildModeSingleStatDiffTable(strSelectListModeTemp, listModeStats[intStatMode]) );
										}
									} else if( _tableModePairStatField.containsKey(listStatComp[0]) ){
										if( listStatComp[0].equals("MAXINT") ){
											String[] listMaxintQueries = {
												buildModePairStatTable(strSelectListModeTemp, "MAXINTF_" + listStatComp[1]),
												buildModePairStatTable(strSelectListModeTemp, "MAXINTO_" + listStatComp[1])
											};
											listMaxintQueries[0] = listMaxintQueries[0].replace("MAXINTF", "MAXINT");
											listMaxintQueries[1] = listMaxintQueries[1].replace("MAXINTO", "MAXINT");
											listQuery.addAll( Arrays.asList(listMaxintQueries) );
										} else {
											listQuery.add( buildModePairStatTable(strSelectListModeTemp, listModeStats[intStatMode]) );
										}
									} else if( listStatComp[0].equals("RATIO") || listStatComp[0].equals("AREARAT") || listModeStats[intStatMode].startsWith("OBJ") ){
										listQuery.add( buildModeSingleStatRatioTable(strSelectListModeTemp, listModeStats[intStatMode], listGroupBy) );
									} else if( listStatComp[0].equals("ACOV") ){
										listQuery.add( buildModeSingleAcovTable(strSelectListModeTemp, listModeStats[intStatMode], listGroupBy) );
									}
								}
							}					
						}
					}
										
					//  build a select that will pull the data table for the plots
					strSelectListModeTemp = strSelectListModeTemp.replaceAll("h\\.", "");
					strSelectListModeTemp = strSelectListModeTemp.replaceAll("fcst_init,",  getSQLDateFormat("fcst_init")  + " fcst_init,");
					strSelectListModeTemp = strSelectListModeTemp.replaceAll("fcst_valid,", getSQLDateFormat("fcst_valid") + " fcst_valid,");
					listQuery.add(
						"INSERT INTO job_data\n" +
						"SELECT\n" +
						strSelectListModeTemp +
						"  0 stat_group_lu_id,\n" +
						"  stat_name,\n" +
						"  stat_value\n" +  
						"FROM mode_stat;");

				}
				
				//  reformat the select list
				strSelectList = strSelectList.replaceAll("\\w+\\.", "");
				strSelectList = strSelectList.replaceAll("\\([^\\)]+\\) nobs", "nobs");
				strSelectList = strSelectList.replaceAll("'' stat_name", "stat_name");
				listQuery.add("SELECT\n" + strSelectList + "\nFROM job_data;");
				
				
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
					if( con.isClosed() ){ throw new Exception("METViewer error: database re-connection failed"); }
					job.setConnection(con);
				}
				
				//  run the query against the database connection and parse the results
				long intStartTime = (new java.util.Date()).getTime();
				Statement stmt = null;
				ResultSet res = null;
				String[] listSQL = toArray(listQuery);				
				for(int i=0; i < listSQL.length; i++){
					if( _boolVerbose ){ _out.println(listSQL[i] + "\n"); }
					if( _boolSQLOnly ){ continue; }
					
					stmt = job.getConnection().createStatement();
					stmt.execute(listSQL[i]);
					stmt.close();
				}
				if( _boolSQLOnly ){ return; }
				
				//  get the number of rows in the job data set
				stmt = job.getConnection().createStatement();
				stmt.execute("SELECT COUNT(*) FROM job_data;");
				res = stmt.getResultSet();
				int intNumJobDataRows = -1;
				if( res.next() ){ intNumJobDataRows = res.getInt(1); }
				stmt.close();
				if( !_boolSQLOnly ){ 
					_out.println("query returned " + intNumJobDataRows + " job_data rows in " + formatTimeSpan( (new java.util.Date()).getTime() - intStartTime ) + "\n");
				}
				
				//  if there is no data, do not try to plot it
				if( 1 > intNumJobDataRows ){
					_out.println("  **  WARNING: query returned no data");
					int intNumModeGroupPlots = (0 < listMapDep1Mode.length? listMapDep1Mode.length : (0 < listMapDep2Mode.length? listMapDep2Mode.length : 1));
					int intNumQueryPlots = (intNumModeGroupPlots * listAggPerm.length);
					_intPlotIndex += intNumQueryPlots;
					continue;
				}
				
				//  get a list of the fcst_vars
				stmt = job.getConnection().createStatement();
				stmt.execute("SELECT DISTINCT fcst_var FROM job_data;");
				res = stmt.getResultSet();
				MVOrderedMap mapFcstVar = new MVOrderedMap();
				while( res.next() ){
					String strFcstVar = res.getString(1);
					String strFcstVarProc = strFcstVar;
					Map.Entry[] listFcstVarPat = mapFcstVarPat.getOrderedEntries();
					for(int i=0; i < listFcstVarPat.length; i++){
						Matcher matFcstVar = ( (Pattern)listFcstVarPat[i].getKey() ).matcher(strFcstVar);
						if( matFcstVar.matches() ){ strFcstVarProc = listFcstVarPat[i].getValue().toString(); }
					}
					
					mapFcstVar.put(strFcstVar, formatR(strFcstVarProc)); 
				}					
				stmt.close();
				
				//  update the fcst_var values with the new values
				if( _boolVerbose && !_boolSQLOnly ){ _out.println("Updating fcst_var values..."); }
				Map.Entry[] listFcstVarProc = mapFcstVar.getOrderedEntries();
				for(int i=0; i < listFcstVarProc.length; i++){
					String strFcstVarOld = listFcstVarProc[i].getKey().toString();
					String strFcstVarNew = listFcstVarProc[i].getValue().toString();
					stmt = job.getConnection().createStatement();
					String strFcstVarUpdate = "UPDATE job_data SET fcst_var='" + strFcstVarNew + "' WHERE fcst_var='" + strFcstVarOld + "';";
					if( _boolVerbose ){ _out.println(strFcstVarUpdate); }
					stmt.execute(strFcstVarUpdate);
					stmt.close();
				}
				if( _boolVerbose && !_boolSQLOnly ){ _out.println("Done\n"); }

				//  add the stat_names, if appropriate
				if( !boolModePlot ){
					
					if( _boolVerbose && !_boolSQLOnly ){ _out.println("Updating stat_name values..."); }
					for(int i=0; i < listDepPlot.length; i++){
						
						//  resolve the components of the stat name update
						String[] listStatName = (String[])listDepPlot[i].getValue();						
						String strFcstVar = (String)listDepPlot[i].getKey();
						String[] listFcstVarComp = mapFcstVar.getStrPattern(strFcstVar);
						if( 1 > listFcstVarComp.length ){ continue; }
						String strFcstVarComp = buildValueList(listFcstVarComp);						
						
						//  for each stat name, build a job_data update
						for(int j=0; j < listStatName.length; j++){
							String strStatGroupLuId = boolAggStat ? "0" : (String)_tableStatIndex.get(listStatName[j]);				
							stmt = job.getConnection().createStatement();
							String strStatNameUpdate = 
								"UPDATE job_data " +
								"SET stat_name='" + listStatName[j] + "' " +
								"WHERE stat_group_lu_id=" + strStatGroupLuId + " AND fcst_var IN (" + strFcstVarComp + ");";
							if( _boolVerbose ){ _out.println(strStatNameUpdate); }
							stmt.execute(strStatNameUpdate);
							stmt.close();
						}				
					}
					if( _boolVerbose && !_boolSQLOnly ){ _out.println("Done\n"); }
				}				

				//  add set fields to the table to handle aggregating over sets of values
				int intAggUpdates = 0;
				for(int i=0; i < listAggVal.length; i++){
					
					//  if the aggregate values are not sets, continue
					Object objAggVal = listAggVal[i].getValue();
					if( objAggVal instanceof String[] ){ continue; }
					
					if( 0 == intAggUpdates++ && _boolVerbose && !_boolSQLOnly ){ _out.println("Updating set values..."); }
				
					//  if the aggregate values are sets, add a set field to the table
					final String strAggVar = listAggVal[i].getKey().toString();
					String strSetField = strAggVar + "_set";
					strTempList += ",\n    " + strSetField + "          VARCHAR(64)";
					strSelectList += ",\n  " + strSetField;
					stmt = job.getConnection().createStatement();
					String strSetAlter = "ALTER TABLE job_data ADD COLUMN " + strSetField + " VARCHAR(64);";
					if( _boolVerbose ){ _out.println(strSetAlter); }
					stmt.execute(strSetAlter);
					stmt.close();
					
					
					//  replace the list of field values with a set value
					Map.Entry[] listSetVal = ((MVOrderedMap)objAggVal).getOrderedEntries();					
					for(int j=0; j < listSetVal.length; j++){
						String strSetName = listSetVal[j].getKey().toString();
						String strSetList = buildValueList( (String[])listSetVal[j].getValue() );						
						stmt = job.getConnection().createStatement();
						String strSetUpdate = "UPDATE job_data SET " + strSetField + "='" + strSetName + "' WHERE " + strAggVar + " IN (" + strSetList + ");";
						if( _boolVerbose ){ _out.println(strSetUpdate); }
						stmt.execute(strSetUpdate);
						stmt.close();
					}
				}
				if( 0 < intAggUpdates && _boolVerbose && !_boolSQLOnly ){ _out.println("Done\n"); }

				
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
					
					for(int intPerm=0; intPerm < listAggPerm.length; intPerm++){
						
						_out.println("* * * * * * * * * * * *\n  PLOT - " + (_intPlotIndex++ + 1) + " / " + _intNumPlots + "\n* * * * * * * * * * * *\n");
						
						/*
						 *  Build a data table that contains the data specific to this permutation 
						 */

						//  create a temp table for the plot
						ArrayList listQueryPlot = new ArrayList();
						listQueryPlot.add("DROP TEMPORARY TABLE IF EXISTS plot_data;");
						listQueryPlot.add(
							"CREATE TEMPORARY TABLE plot_data\n" +
							"(\n" +
							strTempList +
							"    stat_group_lu_id    INT UNSIGNED,\n" +
							"    stat_name           VARCHAR(32),\n" +
							"    stat_value          DOUBLE\n" +
							");");
						
						//  create a where clause for selecting plot data
						String strPlotWhere = "";
						String[] listKeysAgg = listAggPerm[intPerm].getKeyList();
						for(int i=0; i < listKeysAgg.length; i++){
							String strKey = listKeysAgg[i];
							String strVal = (String)listAggPerm[intPerm].get(strKey);
							strPlotWhere += (0 < i? "\n  AND " : "") + strKey + " = '" + strVal + "'"; 
						}

						//  if a mode group is being used, add criteria to the where clause to pull only relevant stats
						if( 0 != intDepNMode ){
							Map.Entry[] listDepPlotAll = listDep1Plot;
							listDepPlotAll = append(listDepPlotAll, listDep2Plot);
							strPlotWhere += "  AND\n  (\n";
							for(int i=0; i < listDepPlotAll.length; i++){
								String strFcstVar = listDepPlotAll[i].getKey().toString();
								String listStats = buildValueList( (String[])listDepPlotAll[i].getValue() );
								strPlotWhere += (0 < i? "    OR\n" : "") + "    (\n      fcst_var='" + strFcstVar + "'\n      AND stat_name IN (" + listStats + ")\n    )\n";
							}
							strPlotWhere += "  )";
						}
						
						//  build the plot query and run the sql to generate the plot data table
						listQueryPlot.add(
							"INSERT INTO plot_data\n" +
							"SELECT\n" + strSelectList + "\n" +
							"FROM job_data" +
							(!strPlotWhere.matches("\\s*")? "\nWHERE\n" + strPlotWhere : "") + ";"
						);

						listSQL = toArray(listQueryPlot);
						for(int i=0; i < listSQL.length; i++){
							if( _boolVerbose ){ _out.println(listSQL[i] + "\n"); }
							stmt = job.getConnection().createStatement();
							stmt.execute(listSQL[i]);
							stmt.close();
						}
						
							
						/*
						 *  Build a map of all plot-specific values to use with the templates 
						 */
		
						//MVOrderedMap mapTmplVals = job.getTmplVal();
						MVOrderedMap mapPlotTmplVals = new MVOrderedMap( mapTmplVals );
		
						//  bootstrap data
						MVOrderedMap mapAggStatStatic = new MVOrderedMap( listAggPerm[intPerm] );
						MVOrderedMap mapSeries1Val = new MVOrderedMap( job.getSeries1Val() );
						MVOrderedMap mapSeries2Val = new MVOrderedMap( job.getSeries2Val() );
						
						//  add the independent and dependent variables to the template value map
						mapPlotTmplVals.put("indy_var", job.getIndyVar());
						Map.Entry[][] listDepPlotList = {listDep1Plot, listDep2Plot};
						ArrayList listAggStats1 = new ArrayList();
						ArrayList listAggStats2 = new ArrayList();
						for(int intDepPlot = 0; intDepPlot < 2; intDepPlot++){
							Map.Entry[] listDepCur = listDepPlotList[intDepPlot];
							String strDepName = "dep" + (intDepPlot+1);
							
							//  add the stats for each fcst_var
							for(int i=0; i < listDepCur.length; i++){
																
								//  add the stat names
								String strFcstVar = (String)listDepCur[i].getKey();
								String strFcstVarProc = formatR(strFcstVar);
								mapPlotTmplVals.put(strDepName + "_" + (i+1), strFcstVarProc);
								mapAggStatStatic.put("fcst_var", strFcstVarProc);
								String[] listStats = (String[])listDepCur[i].getValue();						
								for(int j=0; j < listStats.length; j++){
									mapPlotTmplVals.put(strDepName + "_" + (i+1) + "_stat" + (j+1), listStats[j]);
									if( boolAggStat && 0 == intDepPlot ){ listAggStats1.add(listStats[j]); }
									if( boolAggStat && 1 == intDepPlot ){ listAggStats2.add(listStats[j]); }
								}
								
								//  add the fixed fields and values
								if( null != mapFix && mapFix.containsKey(strFcstVar) ){
									MVOrderedMap mapFixCur = (MVOrderedMap)mapFix.get(strFcstVar);
									Map.Entry[] listFixCurVal = mapFixCur.getOrderedEntries();
									for(int j=0; j < listFixCurVal.length; j++){
										String strFixVar = (String)listFixCurVal[j].getKey();
										String strFixVal = (String)listFixCurVal[j].getValue();
										mapPlotTmplVals.put(strFixVar, strFixVal);
										if( boolAggStat && !strFixVal.contains(" ") ){ mapAggStatStatic.put(strFixVar, strFixVal); }
									}
								}
							}
						}
		
						//  add the aggregate values to the template values map
						mapPlotTmplVals.putAll(listAggPerm[intPerm]);
						if( !_boolSQLOnly ){ _out.println(mapPlotTmplVals.getRDecl() + "\n"); }
		
						//  gather the data for the current plot
						stmt = job.getConnection().createStatement();
						stmt.execute("SELECT COUNT(*) FROM plot_data;");
						res = stmt.getResultSet();
						int intNumPlotDataRows = -1;
						if( res.next() ){ intNumPlotDataRows = res.getInt(1); }
						stmt.close();

						if( 1 > intNumPlotDataRows ){
							if( !_boolSQLOnly ){ _out.println("no plot data found\n"); }
							continue;
						}

						 if( !_boolSQLOnly ){ _out.println("Plotting " + intNumPlotDataRows + " rows"); }
		
						
						/*
						 *  Print the data file in the R_work subfolder and file specified by the data file template
						 */
						
						//  construct the file system paths for the files used to build the plot 
						_strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/")? "" : "/");
						_strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/")? "" : "/");
						_strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/")? "" : "/");		
						String strDataFile	= _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
						if( boolAggStat ){ strDataFile = strDataFile + ".agg_stat"; }
						(new File(strDataFile)).getParentFile().mkdirs();

						//  get the data for the current plot from the plot_data temp table and write it to a data file
						stmt = job.getConnection().createStatement();
						String strPlotDataSelect = "SELECT\n" + strSelectList + "\nFROM plot_data;";
						if( _boolVerbose ){ _out.println(strPlotDataSelect); }
						stmt.execute(strPlotDataSelect);
						printFormattedTable(stmt.getResultSet(), new PrintStream(strDataFile), "\t");
						stmt.close();
										
						/*
						 *  If agg_stat is requested, generate the agg_stat data files and run agg_stat.R 
						 */
										
						if( boolAggStat ){
		
							//  construct and create the path for the agg_stat data output file
							String strAggStatInfo = strDataFile.replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
							String strAggStatOutput = strDataFile.replaceFirst("\\.agg_stat$", "");
							File fileAggStatOutput = new File(strAggStatOutput); 
		
							//  build the map containing tag values for the agg_stat info template
							Hashtable tableAggStatInfo = new Hashtable();
							tableAggStatInfo.put("agg_ctc",			job.getAggCtc()?     "TRUE" : "FALSE");
							tableAggStatInfo.put("agg_sl1l2",		job.getAggSl1l2()?   "TRUE" : "FALSE");
							tableAggStatInfo.put("event_equal",		job.getEventEqual()? "TRUE" : "FALSE");
							tableAggStatInfo.put("agg_diff1",		job.getAggDiff1()?   "TRUE" : "FALSE");
							tableAggStatInfo.put("agg_diff2",		job.getAggDiff2()?   "TRUE" : "FALSE");
							tableAggStatInfo.put("boot_repl",		job.getAggBootRepl());
							tableAggStatInfo.put("boot_ci",			job.getAggBootCI());
							tableAggStatInfo.put("ci_alpha",		job.getCIAlpha());
							tableAggStatInfo.put("indy_var",		job.getIndyVar());
							tableAggStatInfo.put("indy_list",		(0 < job.getIndyVal().length? printRCol(job.getIndyVal(), true) : "c()"));
							tableAggStatInfo.put("series1_list",	job.getSeries1Val().getRDecl());
							tableAggStatInfo.put("series2_list",	job.getSeries2Val().getRDecl());
							tableAggStatInfo.put("agg_stat1",		printRCol(toArray(listAggStats1), true));
							tableAggStatInfo.put("agg_stat2",		printRCol(toArray(listAggStats2), true));
							tableAggStatInfo.put("agg_stat_static",	mapAggStatStatic.getRDecl());
							tableAggStatInfo.put("agg_stat_input",	strDataFile);
							tableAggStatInfo.put("agg_stat_output",	strAggStatOutput);
							tableAggStatInfo.put("working_dir",		_strRworkFolder + "include");
						
							//  populate the agg_stat info file
							populateTemplateFile(_strRtmplFolder + "agg_stat.info_tmpl", strAggStatInfo, tableAggStatInfo);
															
							//  run agg_stat.R to generate the data file for plotting
							if( !fileAggStatOutput.exists() || !_boolCacheAggStat ){
								fileAggStatOutput.getParentFile().mkdirs();
								runRscript(job.getRscript(), _strRworkFolder + "include/agg_stat.R", new String[]{strAggStatInfo});
								
								//if( !fileAggStatOutput.exists() ){ throw new Exception("agg_stat.R failed"); }
								if( !fileAggStatOutput.exists() ){ return; }
							}
		
							//  if agg_diffN is turned on, add __AGG_DIFFN__ to the plot series
							for(int i=0; i < 2; i++){
								MVOrderedMap mapSeriesVal = null;
								String strDiffSeries = "";
								if     ( i == 0 && job.getAggDiff1() ){ mapSeriesVal = mapSeries1Val; strDiffSeries = "__AGG_DIFF1__"; }
								else if( i == 1 && job.getAggDiff2() ){ mapSeriesVal = mapSeries2Val; strDiffSeries = "__AGG_DIFF2__"; }
								else                                  { continue; }						
								String[] listSeriesVar = mapSeriesVal.getKeyList();
								ArrayList listDiffVal = new ArrayList( Arrays.asList( ((String[])mapSeriesVal.get(listSeriesVar[listSeriesVar.length - 1])) ) );
								listDiffVal.add(listDiffVal.size() - 1, strDiffSeries);
								mapSeriesVal.put(listSeriesVar[listSeriesVar.length - 1], toArray(listDiffVal));
							}					
							
							//  remove the .agg_stat suffix from the data file
							strDataFile = strAggStatOutput;
							
							//  turn off the event equalizer
							job.setEventEqual(false);
							
						} //  end: if( boolAggStat )
		
						
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
						String strCaption	= buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps());
		
						//  create the plot and R script output folders, if necessary
						(new File(strPlotFile)).getParentFile().mkdirs();
						(new File(strRFile)).getParentFile().mkdirs();
						
						//  trim the number of indy_lables, if necessary
						String[] listIndyLabel = job.getIndyLabel();
						if( "3".equals(job.getXtlabOrient()) && 16 < listIndyLabel.length ){
							listIndyLabel = decimate(listIndyLabel, (int)(listIndyLabel.length / 16));
						}
										
						/*
						 *  Generate the map of R template tags for the plot
						 */
						
						Hashtable tableRTags = new Hashtable();
						
						//  populate the plot settings in the R script template
						tableRTags.put("r_work",		_strRworkFolder);
						tableRTags.put("indy_var",		job.getIndyVar());
						tableRTags.put("indy_list",		(0 < job.getIndyVal().length? printRCol(job.getIndyVal(), true) : "c()"));
						tableRTags.put("indy_label",	(0 < listIndyLabel.length? printRCol(listIndyLabel, true) : "c()"));
						tableRTags.put("indy_plot_val",	(0 < job.getIndyPlotVal().length? printRCol(job.getIndyPlotVal(), false) : "c()"));
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
						tableRTags.put("plot_caption",	strCaption);
						tableRTags.put("plot_cmd", 		job.getPlotCmd());
						tableRTags.put("event_equal",	(job.getEventEqual()?	"TRUE" : "FALSE"));
						tableRTags.put("vert_plot",		(job.getVertPlot()?		"TRUE" : "FALSE"));
						tableRTags.put("x_reverse",		(job.getXReverse()?		"TRUE" : "FALSE"));
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
						
						int intNumDep1Series = intNumDep1 * (intNumSeries1Perm + (job.getPlot1Diff()? 1 : 0));
						int intNumDep2Series = intNumDep2 * (intNumSeries2Perm + (job.getPlot2Diff()? 1 : 0));
						int intNumDepSeries = intNumDep1Series + intNumDep2Series;

						//  populate the formatting information in the R script template
						populatePlotFmtTmpl(tableRTags, job);
						
						tableRTags.put("plot_ci",	job.getPlotCI().equals("")? printRCol( rep("none", intNumDepSeries) )	: job.getPlotCI());
						tableRTags.put("colors",	job.getColors().equals("")?	"rainbow(" + intNumDepSeries + ")"		: job.getColors());
						tableRTags.put("pch",		job.getPch().equals("")?	printRCol( rep(20, intNumDepSeries) )	: job.getPch());
						tableRTags.put("type",		job.getType().equals("")?	printRCol( rep("b",	intNumDepSeries) )	: job.getType());
						tableRTags.put("lty",		job.getLty().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLty());
						tableRTags.put("lwd",		job.getLwd().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLwd());
						tableRTags.put("con_series",job.getConSeries().equals("")? printRCol( rep(0, intNumDepSeries) )	: job.getConSeries());
						tableRTags.put("legend",	job.getLegend().equals("")? "c()" : job.getLegend());
						tableRTags.put("y1_lim",	job.getY1Lim().equals("")?	"c()" : job.getY1Lim());
						tableRTags.put("y1_bufr",	job.getY1Bufr().equals("")? "0" : job.getY1Bufr());
						tableRTags.put("y2_lim",	job.getY2Lim().equals("")?	"c()" : job.getY2Lim());
						tableRTags.put("y2_bufr",	job.getY2Bufr().equals("")? "0" : job.getY2Bufr());
						
						
						/*
						 *  Read the template in, replacing the appropriate tags with generated R code
						 */
		
						populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);
			
						
						/*
						 *  Attempt to run the generated R script
						 */			
		
						if( _boolPlot ){
							boolean boolSuccess = runRscript(job.getRscript(), strRFile);
							_intNumPlotsRun++;
							_out.println( (boolSuccess? "Created" : "Failed to create") + " plot " + strPlotFile);
						}
						
					} // end: for(int intPerm=0; intPerm < listAggPerm.length; intPerm++)
				
				} // end: for(int intDepMode = 0; intDepMode < listMapDepMode.length; intDepMode++)
	
			} // end: for(int intDep=0; intDep < listDep.length; intDep++)
		
		} // end: for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++)

	}
	
	public void runRhistJob(MVPlotJob job) throws Exception {
		
		//  build a list of fixed value permutations for all plots
		MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
		MVOrderedMap[] listPlotFixPerm = buildFixValList(mapPlotFixVal);

		//  run the plot jobs once for each permutation of plot fixed values
		for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++){

			//  populate the template map with fixed values 
			Map.Entry[] listPlotFixVal = buildFixValTmplMap(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);
			
			boolean boolModePlot = false;
			Statement stmt = null;
			
			//  build the stat_header where clauses of the sql  
			String strWhere = buildAggWhere(new Map.Entry[]{}, listPlotFixVal, boolModePlot);
			
			//  if the n_rank is present, replace the table
			strWhere = strWhere.replaceAll("h\\.n_rank", "ldr.n_rank");
			
			//  build a query for the number of ranks among selected rhist records
			String strRankNumSelect = 
				"SELECT DISTINCT\n" +
				"  ldr.n_rank\n" +
				"FROM\n" +
				"  stat_header h,\n" +
				"  line_data_rhist ldr\n" +
				"WHERE\n" +
				strWhere +
				"  AND h.stat_header_id = ldr.stat_header_id;";
			if( _boolVerbose ){ _out.println(strRankNumSelect); }
			
			//  run the rank number query and warn, if necessary
			stmt = job.getConnection().createStatement();
			stmt.execute(strRankNumSelect);
			ResultSet res = stmt.getResultSet();
			ArrayList listRankNum = new ArrayList();
			while(res.next()){ listRankNum.add(res.getString(1)); }
			String strMsg = "";
			if     ( 0 == listRankNum.size() ){ throw new Exception("no rank data found"); }
			else if( 1 <  listRankNum.size() ){
				strMsg = "  **  WARNING: multiple n_rank values found for search criteria: ";
				for(int i=0; i < listRankNum.size(); i++){ strMsg += (0 < i? ", " : "") + listRankNum.get(i).toString(); }
				_out.println(strMsg);
			}
			stmt.close();
			
			//  build a query for the rank data
			String strPlotDataSelect = 
				"SELECT\n" +
				"  ldrr.i_value,\n" +
				"  SUM(ldrr.rank_i)\n" +
				"FROM\n" +
				"  stat_header h,\n" +
				"  line_data_rhist ldr,\n" +
				"  line_data_rhist_rank ldrr\n" +
				"WHERE\n" +
				strWhere +
				"  AND h.stat_header_id = ldr.stat_header_id\n" +
				"  AND ldr.line_data_id = ldrr.line_data_id\n" +
				"GROUP BY i_value;";

			if( _boolVerbose ){ _out.println(strPlotDataSelect); }
			if( _boolSQLOnly ){ return; }
						

			/*
			 *  Print the data file in the R_work subfolder and file specified by the data file template
			 */
			
			//  construct the file system paths for the files used to build the plot
			MVOrderedMap mapPlotTmplVals = new MVOrderedMap( job.getTmplVal() );
			_strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/")? "" : "/");
			_strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/")? "" : "/");
			_strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/")? "" : "/");		
			String strDataFile	= _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
			(new File(strDataFile)).getParentFile().mkdirs();

			//  get the data for the current plot from the plot_data temp table and write it to a data file
			stmt = job.getConnection().createStatement();
			stmt.execute(strPlotDataSelect);
			printFormattedTable(stmt.getResultSet(), new PrintStream(strDataFile), "\t");
			stmt.close();
			
			//  build the template strings using the current template values
			String strPlotFile	= _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
			String strRFile		= _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
			String strTitle		= buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps());
			String strXLabel	= buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
			String strY1Label	= buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
			String strCaption	= buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps());
			
			//  create a table containing all template values for populating the R_tmpl
			Hashtable tableRTags = new Hashtable();
			
			tableRTags.put("r_work",		_strRworkFolder);
			tableRTags.put("plot_file",		strPlotFile);
			tableRTags.put("data_file",		strDataFile);
			tableRTags.put("plot_title",	strTitle);
			tableRTags.put("x_label",		strXLabel);
			tableRTags.put("y1_label",		strY1Label);
			tableRTags.put("plot_caption",	strCaption);
			tableRTags.put("plot_cmd", 		job.getPlotCmd());
			tableRTags.put("grid_on",		(job.getGridOn()? "TRUE" : "FALSE"));
			tableRTags.put("colors",		job.getColors().equals("")?	"\"gray\"" : job.getColors());
			tableRTags.put("y1_lim",		job.getY1Lim().equals("")?	"c()" : job.getY1Lim());
			populatePlotFmtTmpl(tableRTags, job);

			//  populate the R_tmpl with the template values
			populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);

			
			/*
			 *  Attempt to run the generated R script
			 */			

			if( _boolPlot ){
				boolean boolSuccess = runRscript(job.getRscript(), strRFile);
				if( !strMsg.equals("") ){
					_out.println("\n==== Start Rscript error  ====\n" + strMsg + "\n====   End Rscript error  ====");
				}				
				_intNumPlotsRun++;
				_out.println( (boolSuccess? "Created" : "Failed to create") + " plot " + strPlotFile);
			}

		}		
	}
	
	/**
	 * Build where clauses for each of the input aggregation field/value entries and return the clauses as
	 * a String
	 * @param listAggVal list of &lt;agg&gt; field/value pairs
	 * @param listPlotFixVal list of &lt;plot_fix&gt; field/value pairs
	 * @param boolModePlot specifies MODE plot
	 * @return generated SQL where clauses
	 */
	public static String buildAggWhere(Map.Entry[] listAggVal, Map.Entry[] listPlotFixVal, boolean boolModePlot){
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
			strWhere += "  " + (0 < i? "AND " : "") + formatField(strField, boolModePlot, false) + " " + strCondition + "\n";
		}
		
		return strWhere;
	}
	
	/**
	 * Build the list of fixed field/value permutations for all jobs
	 * @param mapPlotFixVal map of field/value pairs to permute
	 * @return list of permutations
	 */
	public static MVOrderedMap[] buildFixValList(MVOrderedMap mapPlotFixVal){
		
		//  build a list of fixed value permutations for all plots
		MVOrderedMap[] listPlotFixPerm = {new MVOrderedMap()};
		if( 0 < mapPlotFixVal.size() ){
			MVDataTable tabPlotFixPerm = permute(mapPlotFixVal);
			listPlotFixPerm = tabPlotFixPerm.getRows();
		}

		return listPlotFixPerm;
	}
	
	/**
	 * Construct the template map for the specified permutation of fixed values, using the specified 
	 * set values.   
	 * @param job job whose template values are used
	 * @param mapPlotFix fixed field/value pairs to use in populating the template values
	 * @param mapPlotFixVal values used for sets
	 * @throws Exception
	 */
	public Map.Entry[] buildFixValTmplMap(MVPlotJob job, MVOrderedMap mapPlotFix, MVOrderedMap mapPlotFixVal)
	throws Exception {
		MVOrderedMap mapTmplVals = job.getTmplVal();
		Map.Entry[] listPlotFixVal = mapPlotFix.getOrderedEntries();
			
		//  add the fixed values to the template value map, and insert set values for this permutation
		for(int i=0; i < listPlotFixVal.length; i++){
			String strFixVar = listPlotFixVal[i].getKey().toString();
			String strFixVal = listPlotFixVal[i].getValue().toString();
			MVOrderedMap mapTmpl = job.getTmplMap(strFixVar);
			if( null != mapTmpl && mapTmpl.containsKey(strFixVal) ){
				strFixVal = mapTmpl.getStr(strFixVal);
			} else { 
				Matcher matDateRange = _patDateRange.matcher(strFixVal);
				if( matDateRange.matches() ){
					strFixVal = _formatPlot.format( _formatDB.parse(matDateRange.group(2)) );					
				}
			}
			mapTmplVals.putStr(listPlotFixVal[i].getKey().toString(), strFixVal);
			
		}
		
		//  replace fixed value set names with their value maps
		ArrayList listPlotFixValAdj = new ArrayList();
		for(int i=0; i < listPlotFixVal.length; i++){
			String strFixVar = listPlotFixVal[i].getKey().toString();
			if( !strFixVar.endsWith("_set") ){
				listPlotFixValAdj.add(listPlotFixVal[i]);
				continue;
			}
			
			String strFixVarAdj = strFixVar.replaceAll("_set$", "");
			MVOrderedMap mapFixSet = (MVOrderedMap)mapPlotFixVal.get( strFixVarAdj );
			listPlotFixValAdj.add(new MVMapEntry(strFixVarAdj, mapFixSet) );
		}
		listPlotFixVal = (Map.Entry[])listPlotFixValAdj.toArray(new Map.Entry[]{});
		
		return listPlotFixVal;
	}
	
	/**
	 * Populate the input table with the plot formatting tag values stored in the input job.
	 * @param tableRTags template value table to receive plot formatting values
	 * @param job source for plot formatting values
	 */
	public static void populatePlotFmtTmpl(Hashtable tableRTags, MVPlotJob job){
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
		tableRTags.put("caption_weight",job.getCaptionWeight());
		tableRTags.put("caption_col",	job.getCaptionCol());
		tableRTags.put("caption_size",	job.getCaptionSize());
		tableRTags.put("caption_offset",job.getCaptionOffset());
		tableRTags.put("caption_align",	job.getCaptionAlign());
		tableRTags.put("box_pts",		job.getBoxPts());
		tableRTags.put("box_outline",	job.getBoxOutline());
		tableRTags.put("box_boxwex",	job.getBoxBoxwex());
		tableRTags.put("box_notch",		job.getBoxNotch());
		tableRTags.put("box_avg",		job.getBoxAvg());
		tableRTags.put("ci_alpha",		job.getCIAlpha());
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
	 * @param Rscript Rscript command
	 * @param script R script to run
	 * @param args (optional) Arguments to pass to the R script
	 * @throws Exception
	 */
	public boolean runRscript(String Rscript, String script, String[] args) throws Exception{
		
		String strArgList = "";
		for(int i=0; null != args && i < args.length; i++){ strArgList += " " + args[i]; }
		
		if( !_boolSQLOnly ){ _out.println("\nRunning '" + Rscript + " " + script + "'"); }
		Process proc = Runtime.getRuntime().exec(Rscript + " " + script + strArgList);
		if( _boolProcWait ){
			proc.waitFor();
		} else {
			try{
				Thread.sleep(_intProcSleep);
			}catch(InterruptedException ie){}
		}

		String strRscriptOut = "";
		BufferedReader readerProcIn = new BufferedReader( new InputStreamReader(proc.getInputStream()) );		
		while( readerProcIn.ready() ){
			strRscriptOut += readerProcIn.readLine() + "\n";
		}
		readerProcIn.close();			
		if( !"".equals(strRscriptOut) && !_boolSQLOnly ){
			_out.println("\n==== Start Rscript output  ====\n" + strRscriptOut + "====   End Rscript output  ====");
		}
		
		String strRscriptErr = "";
		BufferedReader readerProcError = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );		
		while( readerProcError.ready() ){
			strRscriptErr += readerProcError.readLine() + "\n";
		}
		readerProcError.close();
		if( !"".equals(strRscriptErr)  && !_boolSQLOnly ){
			_out.println("\n==== Start Rscript error  ====\n" + strRscriptErr + "====   End Rscript error  ====");
		}
		if( !_boolSQLOnly ){ _out.println(); }
		return !strRscriptErr.contains("Execution halted");
	}
	public boolean runRscript(String Rscript, String script) throws Exception{ return runRscript(Rscript, script, new String[]{}); }
	
	/**
	 * Prints a textual representation of the input {@link MVDataTable} with the field names in the 
	 * first row to the specified {@link PrintStream} destination.  
	 * @param res The MVDataTable to print
	 * @param str The stream to write the formatted results to (defaults to _out)
	 * @param delim The delimiter to insert between field headers and values (defaults to ' ')
	 * @param maxRows The max number of rows to print, -1 to print all rows
	 */
	public static void printFormattedTable(MVDataTable tab, PrintStream str, String delim, int maxRows){
		String[] fields = tab.getFields();
		int[] intFieldWidths = new int[tab.getNumFields()];
		for(int i=0; i < fields.length; i++){ intFieldWidths[i] = tab.getMaxFieldLength(fields[i]) + 2; }
		
		for(int i=0; i < fields.length; i++){
			str.print( delim.equals(" ")? padEnd(fields[i], intFieldWidths[i]) : (0 < i? delim : "") + fields[i] );
		}
		str.println();					
		
		MVOrderedMap[] rows = tab.getRows();
		int intPrintRows = (0 < maxRows? (maxRows < rows.length? maxRows : rows.length) : rows.length);
		int intLine = 1;
		for(int i=0; i < intPrintRows; i++){
			for(int j=0; j < fields.length; j++){
				String strVal = (String)rows[i].get(fields[j]); 
				str.print( delim.equals(" ")? padEnd(strVal, intFieldWidths[j]) : (0 < j? delim : "") + strVal );
			}
			str.println();
			if( 0 == 100 % intLine++ ){ str.flush(); }
		}
		if( 0 < maxRows && maxRows < rows.length ){ str.println("(" + (rows.length - maxRows) + " more rows...)"); }
	}
	public void printFormattedTable(MVDataTable tab){ printFormattedTable(tab, _out, " ", 40); }
	public void printFormattedTable(MVDataTable tab, int maxRows){ printFormattedTable(tab, _out, " ", maxRows); }
	public void printFormattedTable(MVDataTable tab, PrintStream str, String delim){ printFormattedTable(tab, str, delim, -1); }
	
	/**
	 * Prints a textual representation of the input {@link ResultSet} with the field names in the 
	 * first row to the specified {@link PrintStream} destination.  
	 * @param res The ResultSet to print
	 * @param str The stream to write the formatted results to (defaults to _out)
	 * @param delim The delimiter to insert between field headers and values (defaults to ' ')
	 * @param maxRows The max number of rows to print, -1 to print all rows
	 */
	public void printFormattedTable(ResultSet res, PrintStream str, String delim, int maxRows){
		try{
			ResultSetMetaData met = res.getMetaData();
	
			//  get the column display widths
			int[] intFieldWidths = new int[met.getColumnCount()];
			for(int i=1; i <= met.getColumnCount(); i++){ intFieldWidths[i-1] = met.getColumnDisplaySize(i) + 2; }
			
			//  print out the column headers
			for(int i=1; i <= met.getColumnCount(); i++){
				str.print(delim.equals(" ")? padEnd(met.getColumnLabel(i), intFieldWidths[i-1]) : (1 < i? delim : "") + met.getColumnLabel(i));
			}
			str.println();

			//  print out the table of values
			int intLine = 1;
			while( res.next() ){
				for(int i=1; i <= met.getColumnCount(); i++){
					String strVal = res.getString(i); 
					strVal = (strVal.equalsIgnoreCase("null")? "NA" : strVal);
					str.print( delim.equals(" ")? padEnd(strVal, intFieldWidths[i-1]) : (1 < i? delim : "") + strVal );
				}
				str.println();
				if( 0 == 100 % intLine++ ){ str.flush(); }
			}
			
		}catch(Exception e){
			_out.println("  **  ERROR: Caught " + e.getClass() + " in printFormattedTable(ResultSet res): " + e.getMessage());
		}
	}
	public void printFormattedTable(ResultSet res){ printFormattedTable(res, _out, " ", 40); }
	public void printFormattedTable(ResultSet res, int maxRows){ printFormattedTable(res, _out, " ", maxRows); }
	public void printFormattedTable(ResultSet res, PrintStream str, String delim){ printFormattedTable(res, str, delim, -1); }

	public static String formatField(String field, boolean mode, boolean fmtDate){
		if( field.equals("init_hour") )                     { return (mode? "HOUR(h.fcst_init)"  : "HOUR(h.fcst_init_beg)");  }
		else if( field.equals("valid_hour") )               { return (mode? "HOUR(h.fcst_valid)" : "HOUR(h.fcst_valid_beg)"); }
		else if( field.equals("fcst_init_beg")  && fmtDate ){ return getSQLDateFormat("h.fcst_init_beg");  }
		else if( field.equals("fcst_init")      && fmtDate ){ return getSQLDateFormat("h.fcst_init");      }
		else if( field.equals("fcst_valid_beg") && fmtDate ){ return getSQLDateFormat("h.fcst_valid_beg"); }
		else if( field.equals("fcst_valid")     && fmtDate ){ return getSQLDateFormat("h.fcst_valid");     }
		else                                                { return "h." + field;                         }
	}
	public static String formatField(String field, boolean mode){ return formatField(field, mode, true); }
	
	public static String formatSQLConstraint(String value){
		String strRet = " = '" + value + "'";
		
		Matcher matBetween = _patDateRange.matcher(value);
		if( matBetween.matches() ){ strRet = " " + value; }
		
		return strRet;
	}

	public static String buildModePairStatTable(String strSelectList, String stat){
		
		//  parse the stat into the stat name and the object flags
		String[] listStatParse = parseModeStat(stat);
		if( 2 != listStatParse.length ){ return ""; }
		String strStatName = listStatParse[0];
		String strStatFlag = listStatParse[1];
		
		//  build the object flag where clause
		String strWhere = "";		
		if( strStatFlag.charAt(0) != 'A' ){ strWhere +=                                            "  simple_flag = " +  ('S' == strStatFlag.charAt(0)? "1" : "0"); }
		if( strStatFlag.charAt(1) != 'A' ){ strWhere += (strWhere.equals("")? "  " : "\n  AND ") + "  matched_flag = " +  ('M' == strStatFlag.charAt(1)? "1" : "0"); }
		strWhere = (strWhere.equals("")? "" : "\nWHERE\n" + strWhere);
		
		//  build the list of fields involved in the computations
		String strSelectListStat = strSelectList.replaceAll("h\\.", "");
		String strGroupListMMI = strSelectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");
		
		//  set the object_id field, depending on the stat
		String strObjectId = "object_id";
		String strObjectIdName = "object_id";
		String strGroupBy = "";
		if( strStatName.startsWith("MAXINT") ){
			if     ( strStatName.equals("MAXINTF") ){ strObjectId = "SUBSTR(object_id, 1, LOCATE('_', object_id)-1) fcst_id"; strObjectIdName = "fcst_id"; }
			else if( strStatName.equals("MAXINTO") ){ strObjectId = "SUBSTR(object_id, LOCATE('_', object_id)+1) obs_id";     strObjectIdName = "obs_id"; }
			strGroupBy = "\nGROUP BY\n" + strGroupListMMI + "  " + strObjectIdName;
		}
		
		//  set the table stat field, object_id pattern and group by clause, depending on the stat
		String strTableStat = _tableModePairStatField.get(strStatName).toString();
		
		//  build the query
		return 
			"INSERT INTO mode_stat\n" +
			"SELECT\n" + strSelectListStat +
			"  " + strObjectId + ",\n" +
			"  object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  " + strTableStat + " stat_value\n" +  
			"FROM mode_pair" + strWhere + strGroupBy + ";";
	}	
	
	public static final Pattern _patModeSingle = Pattern.compile("\\s+h\\.([^,]+),");
	public static String buildModeSingleStatDiffTable(String strSelectList, String stat){
		
		//  parse the stat into the stat name and the object flags
		String[] listStatParse = parseModeStat(stat);
		if( 2 != listStatParse.length ){ return ""; }
		String strStatName = listStatParse[0];
		String strStatFlag = listStatParse[1];

		//  build the list of fields involved in the computations
		String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");
		
		//  build the where clause using the input select fields
		String strWhere = "";
		Matcher mat = _patModeSingle.matcher(strSelectList);
		while( mat.find() ){
			strWhere += "\n  AND s." + mat.group(1) + " = s2." + mat.group(1);
		}
		if( strStatFlag.charAt(1) != 'A' ){ 
			strWhere += "\n  AND s.simple_flag = " +  ('S' == strStatFlag.charAt(1)? "1" : "0") +
						"\n  AND s2.simple_flag = " + ('S' == strStatFlag.charAt(1)? "1" : "0");		
		}
		if( strStatFlag.charAt(2) != 'A' ){
			strWhere += "\n  AND s.matched_flag = " +  ('M' == strStatFlag.charAt(2)? "1" : "0") +
						"\n  AND s2.matched_flag = " + ('M' == strStatFlag.charAt(2)? "1" : "0");
		}		
		
		//  set the table stat field, object_id pattern and group by clause, depending on the stat
		String strTableStat = _tableModeSingleStatField.get(strStatName).toString();
		
		//  build the query
		return
			"INSERT INTO mode_stat\n" +
			"SELECT\n" + strSelectListStat +
			"  s.object_id,\n" +
			"  s.object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  s." + strTableStat + " - s2." + strTableStat + " stat_value\n" +  
			"FROM mode_single s, mode_single2 s2\n" +
			"WHERE\n" +
			"  s.fcst_flag = 1\n" +
			"  AND s2.fcst_flag = 0\n" +
			"  AND RIGHT(s.object_id, 3) = RIGHT(s2.object_id, 3)" +
			strWhere + ";";
	}
	
	public static String buildModeSingleStatTable(String selectList, String stat, String[] groups){

		//  parse the stat into the stat name and the object flags
		String[] listStatParse = parseModeStat(stat);
		if( 2 != listStatParse.length ){ return ""; }
		String strStatName = listStatParse[0];
		String strStatFlag = listStatParse[1];
		
		//  build the list of fields involved in the computations
		String strSelectListStat = selectList.replaceAll("h\\.", "");
		
		//  set the table stat field
		String strTableStat = _tableModeSingleStatField.get(strStatName).toString();
		
		//  build the object flag where clause		
		String strWhere = "";
		if( strStatFlag.charAt(0) != 'A' ){ strWhere +=                                          "  fcst_flag = " +    ('F' == strStatFlag.charAt(0)? "1" : "0"); }
		if( strStatFlag.charAt(1) != 'A' ){ strWhere += (strWhere.equals("")? "  " : "\n  AND ") + "simple_flag = " +  ('S' == strStatFlag.charAt(1)? "1" : "0"); }
		if( strStatFlag.charAt(2) != 'A' ){ strWhere += (strWhere.equals("")? "  " : "\n  AND ") + "matched_flag = " + ('M' == strStatFlag.charAt(2)? "1" : "0"); }
		strWhere = (strWhere.equals("")? "" : "\nWHERE\n" + strWhere);
		
		//  build the group by clause
		String strGroupBy = "";
		if( strStatName.startsWith("CNT") ){ 
			strGroupBy = "\nGROUP BY\n";			
			for(int i=0; i < groups.length; i++){ strGroupBy += (0 < i? ",\n" : "") + "  " + groups[i]; }
			if( !strStatName.equals("CNTSUM") ){ strGroupBy += ",\n  fcst_valid"; }
		}
		
		//  build the query
		return
			"INSERT INTO mode_stat\n" +
			"SELECT\n" + strSelectListStat +
			"  object_id,\n" +
			"  object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  " + strTableStat + " stat_value\n" +  
			"FROM mode_single" + strWhere + strGroupBy + ";";
	}

	public static String buildModeSingleStatRatioTable(String selectList, String stat, String[] groups){

		//  build the list of fields involved in the computations
		String strSelectListStat = selectList.replaceAll("h\\.", "");
		
		//  set the table stat field
		String strTableStat = _tableModeRatioField.get(stat).toString();
		
		//  build the group by clause
		String strGroupBy = "";
		for(int i=0; i < groups.length; i++){ strGroupBy += (0 < i? ",\n" : "") + "  " + groups[i]; }
		
		//  build the query
		return
			"INSERT INTO mode_stat\n" +
			"SELECT\n" + strSelectListStat +
			"  object_id,\n" +
			"  object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  " + strTableStat + " stat_value\n" +  
			"FROM mode_single\n" + 
			"GROUP BY\n" + strGroupBy + ";";
	}

	public static String buildModeSingleAcovTable(String selectList, String stat, String[] groups){

		//  parse the stat into the stat name and the object flags
		String[] listStatParse = parseModeStat(stat);
		if( 2 != listStatParse.length ){ return ""; }
		String strStatFlag = listStatParse[1];
		
		//  build the query components
		String strSelectListStat = selectList.replaceAll("h\\.", "").replaceAll(",\\s+$", "");
		String strStat = "SUM(area) / (2*total)";
		String strGroupBy = strSelectListStat;
		String strWhere = "";
		if( strStatFlag.charAt(0) != 'A' ){
			strStat = "SUM(area) / total";
			strGroupBy += ",\n  fcst_flag";
			strWhere = "  AND fcst_flag = " + ('F' == strStatFlag.charAt(0)? "1" : "0") + "\n";
		}

		//  build the query
		return
			"INSERT INTO mode_stat\n" +
			"SELECT\n" + strSelectListStat + ",\n" +
			"  '' object_id,\n" +
			"  '' object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  " + strStat + " stat_value\n" +  
			"FROM mode_single\n" + 
			"WHERE simple_flag = 1\n" + strWhere +
			"GROUP BY\n" + strGroupBy + ";";
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
		
		_tableModeStatIndex.put("CNT",		"100");
		_tableModeStatIndex.put("CNTSUM",	"101");
		_tableModeStatIndex.put("CENTX",	"102");
		_tableModeStatIndex.put("CENTY",	"103");
		_tableModeStatIndex.put("CENTLAT",	"104");
		_tableModeStatIndex.put("CENTLON",	"105");
		_tableModeStatIndex.put("AXAVG",	"106");
		_tableModeStatIndex.put("LEN",		"107");
		_tableModeStatIndex.put("WID",		"108");
		_tableModeStatIndex.put("ASPECT",	"109");
		_tableModeStatIndex.put("AREA",		"110");
		_tableModeStatIndex.put("AREAFIL",	"111");
		_tableModeStatIndex.put("AREATHR",	"112");
		_tableModeStatIndex.put("CURV",		"113");
		_tableModeStatIndex.put("CURVX",	"114");
		_tableModeStatIndex.put("CURVY",	"115");
		_tableModeStatIndex.put("CPLX",		"116");
		_tableModeStatIndex.put("INT10",	"117");
		_tableModeStatIndex.put("INT25",	"118");
		_tableModeStatIndex.put("INT50",	"119");
		_tableModeStatIndex.put("INT75",	"120");
		_tableModeStatIndex.put("INT90",	"121");
		_tableModeStatIndex.put("INTN",		"122");
		_tableModeStatIndex.put("INTSUM",	"123");
		_tableModeStatIndex.put("RATIO",	"124");
		_tableModeStatIndex.put("AREARAT",	"125");
		_tableModeStatIndex.put("OBJHITS",	"126");
		_tableModeStatIndex.put("OBJMISSES","127");
		_tableModeStatIndex.put("OBJFAS",	"128");
		_tableModeStatIndex.put("OBJCSI",	"129");
		_tableModeStatIndex.put("OBJPODY",	"130");
		_tableModeStatIndex.put("OBJFAR",	"131");
		_tableModeStatIndex.put("OBJAHITS",	"132");
		_tableModeStatIndex.put("OBJAMISSES","133");
		_tableModeStatIndex.put("OBJAFAS",	"134");
		_tableModeStatIndex.put("OBJACSI",	"135");
		_tableModeStatIndex.put("OBJAPODY",	"136");
		_tableModeStatIndex.put("OBJAFAR",	"137");
				
		_tableModeStatIndex.put("CENTDIST",		"150");
		_tableModeStatIndex.put("BOUNDDIST",	"151");
		_tableModeStatIndex.put("HULLDIST",		"152");
		_tableModeStatIndex.put("ANGLEDIFF",	"153");
		_tableModeStatIndex.put("AREARATIO",	"154");
		_tableModeStatIndex.put("INTAREA",		"155");
		_tableModeStatIndex.put("UNIONAREA",	"156");
		_tableModeStatIndex.put("SYMDIFF",		"157");
		_tableModeStatIndex.put("INTOVERAREA",	"158");
		_tableModeStatIndex.put("CMPLXRATIO",	"159");
		_tableModeStatIndex.put("PERCINTRATIO",	"160");
		_tableModeStatIndex.put("INT",			"161");
		_tableModeStatIndex.put("MAXINT",		"162");
		_tableModeStatIndex.put("MAXINTF",		"163");
		_tableModeStatIndex.put("MAXINTO",		"164");
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
	
	public static final Hashtable _tableStatHeaderSQLType = new Hashtable();
	static{
		_tableStatHeaderSQLType.put("model",			"VARCHAR(64)");
		_tableStatHeaderSQLType.put("fcst_lead",		"INT UNSIGNED");
		_tableStatHeaderSQLType.put("fcst_valid_beg",	"DATETIME");
		_tableStatHeaderSQLType.put("fcst_valid_end",	"DATETIME");
		_tableStatHeaderSQLType.put("fcst_init_beg",	"DATETIME");
		_tableStatHeaderSQLType.put("obs_lead",			"INT UNSIGNED");
		_tableStatHeaderSQLType.put("obs_valid_beg",	"DATETIME");
		_tableStatHeaderSQLType.put("obs_valid_end",	"DATETIME");
		_tableStatHeaderSQLType.put("init_hour",		"INT UNSIGNED");
		_tableStatHeaderSQLType.put("valid_hour",		"INT UNSIGNED");
		_tableStatHeaderSQLType.put("fcst_var",			"VARCHAR(64)");
		_tableStatHeaderSQLType.put("fcst_lev",			"VARCHAR(16)");
		_tableStatHeaderSQLType.put("obs_var",			"VARCHAR(64)");
		_tableStatHeaderSQLType.put("obs_lev",			"VARCHAR(16)");
		_tableStatHeaderSQLType.put("obtype",			"VARCHAR(32)");
		_tableStatHeaderSQLType.put("vx_mask",			"VARCHAR(32)");
		_tableStatHeaderSQLType.put("interp_mthd",		"VARCHAR(16)");
		_tableStatHeaderSQLType.put("interp_pnts",		"INT UNSIGNED");
		_tableStatHeaderSQLType.put("fcst_thresh",		"VARCHAR(16)");
		_tableStatHeaderSQLType.put("obs_thresh",		"VARCHAR(16)");
	}	
	
	public static final Hashtable _tableModeHeaderSQLType = new Hashtable();
	static{
		_tableModeHeaderSQLType.put("model",		"VARCHAR(64)");
		_tableModeHeaderSQLType.put("fcst_lead",	"INT UNSIGNED");
		_tableModeHeaderSQLType.put("fcst_valid",	"DATETIME");
		_tableModeHeaderSQLType.put("fcst_accum",	"INT UNSIGNED");
		_tableModeHeaderSQLType.put("fcst_init",	"DATETIME");
		_tableModeHeaderSQLType.put("obs_lead",		"INT UNSIGNED");
		_tableModeHeaderSQLType.put("obs_valid",	"DATETIME");
		_tableModeHeaderSQLType.put("obs_accum",	"INT UNSIGNED");
		_tableModeHeaderSQLType.put("init_hour",	"INT UNSIGNED");
		_tableModeHeaderSQLType.put("valid_hour",	"INT UNSIGNED");
		_tableModeHeaderSQLType.put("fcst_rad",		"INT UNSIGNED");
		_tableModeHeaderSQLType.put("fcst_thr",		"VARCHAR(16)");
		_tableModeHeaderSQLType.put("obs_rad",		"INT UNSIGNED");
		_tableModeHeaderSQLType.put("obs_thr",		"VARCHAR(16)");
		_tableModeHeaderSQLType.put("fcst_var",		"VARCHAR(64)");
		_tableModeHeaderSQLType.put("fcst_lev",		"VARCHAR(16)");
		_tableModeHeaderSQLType.put("obs_var",		"VARCHAR(64)");
		_tableModeHeaderSQLType.put("obs_lev", 		"VARCHAR(16)");
	}
	
	public static final Hashtable _tableModePairSQLTypes = new Hashtable();
	static{
		_tableModePairSQLTypes.put("centroid_dist",					"DOUBLE");
		_tableModePairSQLTypes.put("boundary_dist",					"DOUBLE");
		_tableModePairSQLTypes.put("convex_hull_dist",				"DOUBLE");
		_tableModePairSQLTypes.put("angle_diff",					"DOUBLE");
		_tableModePairSQLTypes.put("area_ratio",					"DOUBLE");
		_tableModePairSQLTypes.put("intersection_area",				"INT UNSIGNED");
		_tableModePairSQLTypes.put("union_area",					"INT UNSIGNED");
		_tableModePairSQLTypes.put("symmetric_diff",				"INTEGER");
		_tableModePairSQLTypes.put("intersection_over_area",		"DOUBLE");
		_tableModePairSQLTypes.put("complexity_ratio",				"DOUBLE");
		_tableModePairSQLTypes.put("percentile_intensity_ratio",	"DOUBLE");
		_tableModePairSQLTypes.put("interest",						"DOUBLE");
	}
	
	public static final Hashtable _tableModeSingleSQLTypes = new Hashtable();
	static{
		_tableModeSingleSQLTypes.put("centroid_x",		"DOUBLE");
		_tableModeSingleSQLTypes.put("centroid_y",		"DOUBLE");
		_tableModeSingleSQLTypes.put("centroid_lat",	"DOUBLE");
		_tableModeSingleSQLTypes.put("centroid_lon",	"DOUBLE");
		_tableModeSingleSQLTypes.put("axis_avg",		"DOUBLE");
		_tableModeSingleSQLTypes.put("length",			"DOUBLE");
		_tableModeSingleSQLTypes.put("width",			"DOUBLE");
		_tableModeSingleSQLTypes.put("area",			"INT UNSIGNED");
		_tableModeSingleSQLTypes.put("area_filter",		"INT UNSIGNED");
		_tableModeSingleSQLTypes.put("area_thresh",		"INT UNSIGNED");
		_tableModeSingleSQLTypes.put("curvature",		"DOUBLE");
		_tableModeSingleSQLTypes.put("curvature_x",		"DOUBLE");
		_tableModeSingleSQLTypes.put("curvature_y",		"DOUBLE");
		_tableModeSingleSQLTypes.put("complexity",		"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_10",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_25",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_50",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_75",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_90",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_nn",	"DOUBLE");
		_tableModeSingleSQLTypes.put("intensity_sum",	"DOUBLE");
	}
	
	public static final Hashtable _tableModeSingleStatField = new Hashtable();
	static{
		_tableModeSingleStatField.put("CNT",			"COUNT(object_id)");
		_tableModeSingleStatField.put("CNTSUM",			"COUNT(object_id)");
		_tableModeSingleStatField.put("CENTX",			"centroid_x");
		_tableModeSingleStatField.put("CENTY",			"centroid_y");
		_tableModeSingleStatField.put("CENTLAT",		"centroid_lat");
		_tableModeSingleStatField.put("CENTLON",		"centroid_lon");
		_tableModeSingleStatField.put("AXAVG",			"axis_avg");
		_tableModeSingleStatField.put("LEN",			"length");
		_tableModeSingleStatField.put("WID",			"width");
		_tableModeSingleStatField.put("ASPECT",			"IF((length/width) < (width/length), length/width, width/length)");
		_tableModeSingleStatField.put("AREA",			"area");
		_tableModeSingleStatField.put("AREAFIL",		"area_filter");
		_tableModeSingleStatField.put("AREATHR",		"area_threshold");
		_tableModeSingleStatField.put("CURV",			"curvature");
		_tableModeSingleStatField.put("CURVX",			"curvature_x");
		_tableModeSingleStatField.put("CURVY",			"curvature_y");
		_tableModeSingleStatField.put("CPLX",			"complexity");
		_tableModeSingleStatField.put("INT10",			"intensity_10");
		_tableModeSingleStatField.put("INT25",			"intensity_25");
		_tableModeSingleStatField.put("INT50",			"intensity_50");
		_tableModeSingleStatField.put("INT75",			"intensity_75");
		_tableModeSingleStatField.put("INT90",			"intensity_90");
		_tableModeSingleStatField.put("INTN",			"intensity_nn");
		_tableModeSingleStatField.put("INTSUM",			"intensity_sum");
	}
	
	public static final Hashtable _tableModePairStatField = new Hashtable();
	static{
		_tableModePairStatField.put("CENTDIST",			"centroid_dist");
		_tableModePairStatField.put("BOUNDDIST",		"boundary_dist");
		_tableModePairStatField.put("HULLDIST",			"convex_hull_dist");
		_tableModePairStatField.put("ANGLEDIFF",		"angle_diff");
		_tableModePairStatField.put("AREARATIO",		"area_ratio");
		_tableModePairStatField.put("INTAREA",			"intersection_area");
		_tableModePairStatField.put("UNIONAREA",		"union_area");
		_tableModePairStatField.put("SYMDIFF",			"symmetric_diff");
		_tableModePairStatField.put("INTOVERAREA",		"intersection_over_area");
		_tableModePairStatField.put("CMPLXRATIO",		"complexity_ratio");
		_tableModePairStatField.put("PERCINTRATIO",		"percentile_intensity_ratio");
		_tableModePairStatField.put("INT",				"interest");
		_tableModePairStatField.put("MAXINT",			"MAX(interest)");
		_tableModePairStatField.put("MAXINTF",			"MAX(interest)");
		_tableModePairStatField.put("MAXINTO",			"MAX(interest)");
	}
	
	public static final Hashtable _tableModeRatioField = new Hashtable();
	static{
		_tableModeRatioField.put("RATIO_FSA_ASA",	"SUM(fcst_flag = 1 && simple_flag = 1) / SUM(simple_flag = 1)"); 
		_tableModeRatioField.put("RATIO_OSA_ASA",	"SUM(fcst_flag = 0 && simple_flag = 1) / SUM(simple_flag = 1)");
		_tableModeRatioField.put("RATIO_ASM_ASA",	"SUM(simple_flag = 1 && matched_flag = 1) / SUM(simple_flag = 1)");
		_tableModeRatioField.put("RATIO_ASU_ASA",	"SUM(simple_flag = 1 && matched_flag = 0) / SUM(simple_flag = 1)");
		_tableModeRatioField.put("RATIO_FSM_FSA",	"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 1) / SUM(fcst_flag = 1 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_FSU_FSA",	"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0) / SUM(fcst_flag = 1 && simple_flag = 1)");  
		_tableModeRatioField.put("RATIO_OSM_OSA",	"SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 1) / SUM(fcst_flag = 0 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_OSU_OSA",	"SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0) / SUM(fcst_flag = 0 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_FSM_ASM",	"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 1) / SUM(simple_flag = 1 && matched_flag = 1)");
		_tableModeRatioField.put("RATIO_OSM_ASM",	"SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 1) / SUM(simple_flag = 1 && matched_flag = 1)");
		_tableModeRatioField.put("RATIO_FSU_ASU",	"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0) / SUM(simple_flag = 1 && matched_flag = 0)");  
		_tableModeRatioField.put("RATIO_OSU_ASU",	"SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0) / SUM(simple_flag = 1 && matched_flag = 0)");  		
		_tableModeRatioField.put("RATIO_FSA_AAA",	"SUM(fcst_flag = 1 && simple_flag = 1) / count(object_id)");  
		_tableModeRatioField.put("RATIO_OSA_AAA",	"SUM(fcst_flag = 0 && simple_flag = 1) / count(object_id)");  
		_tableModeRatioField.put("RATIO_FSA_FAA",	"SUM(fcst_flag = 1 && simple_flag = 1) / SUM(fcst_flag = 1)"); 
		_tableModeRatioField.put("RATIO_FCA_FAA",	"SUM(fcst_flag = 1 && simple_flag = 0) / SUM(fcst_flag = 1)");
		_tableModeRatioField.put("RATIO_OSA_OAA",	"SUM(fcst_flag = 0 && simple_flag = 1) / SUM(fcst_flag = 0)");
		_tableModeRatioField.put("RATIO_OCA_OAA",	"SUM(fcst_flag = 0 && simple_flag = 0) / SUM(fcst_flag = 0)");
		_tableModeRatioField.put("RATIO_FCA_ACA",	"SUM(fcst_flag = 1 && simple_flag = 0) / SUM(simple_flag = 0)");
		_tableModeRatioField.put("RATIO_OCA_ACA",	"SUM(fcst_flag = 0 && simple_flag = 0) / SUM(simple_flag = 0)");
		_tableModeRatioField.put("RATIO_FSA_OSA",	"SUM(fcst_flag = 1 && simple_flag = 1) / SUM(fcst_flag = 0 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_OSA_FSA",	"SUM(fcst_flag = 0 && simple_flag = 1) / SUM(fcst_flag = 1 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_ACA_ASA",	"SUM(simple_flag = 0) / SUM(simple_flag = 1)");
		_tableModeRatioField.put("RATIO_ASA_ACA",	"SUM(simple_flag = 1) / SUM(simple_flag = 0)");
		_tableModeRatioField.put("RATIO_FCA_FSA",	"SUM(fcst_flag = 1 && simple_flag = 0) / SUM(fcst_flag = 1 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_FSA_FCA",	"SUM(fcst_flag = 1 && simple_flag = 1) / SUM(fcst_flag = 1 && simple_flag = 0)");
		_tableModeRatioField.put("RATIO_OCA_OSA",	"SUM(fcst_flag = 0 && simple_flag = 0) / SUM(fcst_flag = 0 && simple_flag = 1)");
		_tableModeRatioField.put("RATIO_OSA_OCA",	"SUM(fcst_flag = 0 && simple_flag = 1) / SUM(fcst_flag = 0 && simple_flag = 0)");
		
		_tableModeRatioField.put("OBJHITS",		"SUM(simple_flag = 1 && matched_flag = 1) / 2");
		_tableModeRatioField.put("OBJMISSES",	"SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0)");
		_tableModeRatioField.put("OBJFAS",		"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0)");
		_tableModeRatioField.put("OBJCSI",		"SUM(simple_flag = 1 && matched_flag = 1) / (2 * SUM(simple_flag = 1 && matched_flag = 0)");
		_tableModeRatioField.put("OBJPODY",		"SUM(simple_flag = 1 && matched_flag = 1) / " +
															"( SUM(simple_flag = 1 && matched_flag = 1) + 2 * SUM(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0) )");
		_tableModeRatioField.put("OBJFAR",		"SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0) / " +
												"( SUM(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0) + SUM(simple_flag = 1 && matched_flag = 1) / 2 )");

		_tableModeRatioField.put("AREARAT_FSA_ASA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) ) / SUM( IF(simple_flag = 1, area, 0) )"); 
		_tableModeRatioField.put("AREARAT_OSA_ASA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) ) / SUM( IF(simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_ASM_ASA",	"SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) / SUM( IF(simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_ASU_ASA",	"SUM( IF(simple_flag = 1 && matched_flag = 0, area, 0) ) / SUM( IF(simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSM_FSA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSU_FSA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) )");  
		_tableModeRatioField.put("AREARAT_OSM_OSA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_OSU_OSA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSM_ASM",	"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 1, area, 0) ) / SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_OSM_ASM",	"SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 1, area, 0) ) / SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSU_ASU",	"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0, area, 0) ) / SUM( IF(simple_flag = 1 && matched_flag = 0, area, 0) )");  
		_tableModeRatioField.put("AREARAT_OSU_ASU",	"SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0, area, 0) ) / SUM( IF(simple_flag = 1 && matched_flag = 0, area, 0) )");  		
		_tableModeRatioField.put("AREARAT_FSA_AAA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) ) / count(object_id)");  
		_tableModeRatioField.put("AREARAT_OSA_AAA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) ) / count(object_id)");  
		_tableModeRatioField.put("AREARAT_FSA_FAA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 1, area, 0) )"); 
		_tableModeRatioField.put("AREARAT_FCA_FAA",	"SUM( IF(fcst_flag = 1 && simple_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_OSA_OAA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_OCA_OAA",	"SUM( IF(fcst_flag = 0 && simple_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_FCA_ACA",	"SUM( IF(fcst_flag = 1 && simple_flag = 0, area, 0) ) / SUM( IF(simple_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_OCA_ACA",	"SUM( IF(fcst_flag = 0 && simple_flag = 0, area, 0) ) / SUM( IF(simple_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSA_OSA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_OSA_FSA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_ACA_ASA",	"SUM( IF(simple_flag = 0, area, 0) ) / SUM( IF(simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_ASA_ACA",	"SUM( IF(simple_flag = 1, area, 0) ) / SUM( IF(simple_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_FCA_FSA",	"SUM( IF(fcst_flag = 1 && simple_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_FSA_FCA",	"SUM( IF(fcst_flag = 1 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 1 && simple_flag = 0, area, 0) )");
		_tableModeRatioField.put("AREARAT_OCA_OSA",	"SUM( IF(fcst_flag = 0 && simple_flag = 0, area, 0) ) / SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) )");
		_tableModeRatioField.put("AREARAT_OSA_OCA",	"SUM( IF(fcst_flag = 0 && simple_flag = 1, area, 0) ) / SUM( IF(fcst_flag = 0 && simple_flag = 0, area, 0) )");
		
		_tableModeRatioField.put("OBJAHITS",		"SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) / 2");
		_tableModeRatioField.put("OBJAMISSES",		"SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0, area, 0) )");
		_tableModeRatioField.put("OBJAFAS",			"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0, area, 0) )");
		_tableModeRatioField.put("OBJACSI",			"SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) / (2 * SUM( IF(simple_flag = 1 && matched_flag = 0, area, 0) )");
		_tableModeRatioField.put("OBJAPODY",		"SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) / " +
													"( SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) + 2 * SUM( IF(fcst_flag = 0 && simple_flag = 1 && matched_flag = 0, area, 0) ) )");
		_tableModeRatioField.put("OBJAFAR",			"SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0, area, 0) ) / " +
													"( SUM( IF(fcst_flag = 1 && simple_flag = 1 && matched_flag = 0, area, 0) ) + SUM( IF(simple_flag = 1 && matched_flag = 1, area, 0) ) / 2 )");
	}
}

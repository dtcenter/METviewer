package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import org.apache.log4j.*;
import edu.ucar.metviewer.hmt.*;

public class MVBatch extends MVUtil {
	
	//private static final Logger _logger = Logger.getLogger(MVBatch.class);
	//private static final PrintStream _logStream = System.out;
	
	public static String _strHost				= "kemosabe";
	public static String _strPort				= "3306";	
	public static String _strDatabase			= "metvdb_hmt";
	public static String _strUser				= "pgoldenb";
	public static String _strPwd				= "pgoldenb";
	
	public static String _strRtmplFolder		= "/home/pgoldenb/apps/verif/metviewer/R_tmpl/";
	public static String _strRworkFolder		= "/d1/pgoldenb/var/hmt/R_work/";
	public static String _strPlotFolder			= "/d1/pgoldenb/var/hmt/plots/";
	
	public static boolean _boolProcWait			= true;

	public static final Pattern _patRTmpl		= Pattern.compile("#<(\\w+)>#");
	
	public static final boolean _boolPlot		= true;
	public static boolean _boolSQLSort			= false;
	
	public static String[] _list24				= {};
	public static String[] _list06				= {};
	public static String[] _listBase			= {};
	public static String _strBaseDate			= "";
	public static String _strBaseDateDefault	= "2010-02-14 12:00:00";
	
	public static int _intNumPlots				= 0;
	public static int _intPlotIndex				= 0;
	public static boolean _boolTheWorks			= false;
	
	public static boolean _boolWindows			= false;
		
	public static void main(String[] argv) {
		System.out.println("----  MVBatch  ----\n");
		Connection con = null;

		//  windows settings
		if( _boolWindows ){
			_strRtmplFolder = "c:/src/apps/verif/metviewer/R_tmpl/";
			_strRworkFolder = "c:/src/metv/R_work/";
			_strPlotFolder = "c:/src/metv/plots/";
			_boolProcWait = false;
		}

		try {

			if( 2 > argv.length ){
				System.out.println("usage:\n% java [jvm_args] MVBatch {db_host} {works} [{plot_type} {date1} [date2]...]\n\n----  MVBatch Done  ----");
				try{ if( con != null )	con.close(); }catch(SQLException e){}
				return;
			}
			
			_strHost = argv[0];
			_boolTheWorks = argv[1].equalsIgnoreCase("true");
			
			//  connect to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + _strHost + ":" + _strPort + "/" + _strDatabase, _strUser, _strPwd);
			if( con.isClosed() )	throw new Exception("database connection failed");
			
			System.out.println("connected to " + _strDatabase + "@" + _strHost + "\nthe works: " + _boolTheWorks);

			MVPlotJob[] jobs = {};

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
							jobs = append(jobs, MVPlotJobAgg24.getJobs(con));
						}
						else if( strJob.equals("agg06jobs") ){							
							jobs = append(jobs, MVPlotJobThresh06.getJobs(con));
							jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));
							jobs = append(jobs, MVPlotJobAgg06.getJobs(con));
						}

						//  these jobs are included in the agg24jobs and agg06jobs
						else if( strJob.equals("bar24") )      { jobs = append(jobs, MVPlotJobThresh24Bar.getJobs(con));    } 
						else if( strJob.equals("bar06") )      { jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));    }
						else if( strJob.equals("agg24") )      { jobs = append(jobs, MVPlotJobAgg24.getJobs(con));          }
						else if( strJob.equals("agg06") )      { jobs = append(jobs, MVPlotJobAgg06.getJobs(con));          }
						else if( strJob.equals("thresh24") )   { jobs = append(jobs, MVPlotJobThresh24.getJobs(con));       } 
						else if( strJob.equals("thresh06") )   { jobs = append(jobs, MVPlotJobThresh06.getJobs(con));       }
					}
				}
			} else {
//				jobs = append(jobs, MVPlotJobInit24.getJobs(con));
//				jobs = append(jobs, MVPlotJobInit06.getJobs(con));
//				jobs = append(jobs, MVPlotJobValid24.getJobs(con));
//				jobs = append(jobs, MVPlotJobValid06.getJobs(con));
//				jobs = append(jobs, MVPlotJob30Day24.getJobs(con));
//				jobs = append(jobs, MVPlotJob30Day06.getJobs(con));
//				jobs = append(jobs, MVPlotJobAgg24.getJobs(con));
//				jobs = append(jobs, MVPlotJobAgg06.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh24.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06Day.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh24Bar.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06Bar.getJobs(con));
//				jobs = append(jobs, MVPlotJobThresh06DayBar.getJobs(con));
				jobs = append(jobs, MVPlotJobThresh24Box.getJobs(con));
			}
			
			//  if on windows, change all plot image types to jpeg
			if( _boolWindows ){
				for(int i=0; i < jobs.length; i++){ jobs[i].setPlotType("jpeg"); }
			}
			
			//  calculate the number of plots
			_intNumPlots = 0;
			for(int intJob=0; intJob < jobs.length; intJob++){
				Map.Entry[] listAgg = jobs[intJob].getAggVal().getOrderedEntries();
				int intNumJobPlots = 1;
				for(int j=0; j < listAgg.length; j++){
					Object objAggVal = listAgg[j].getValue();
					if     ( objAggVal instanceof String[] )    { intNumJobPlots *= ((String[])objAggVal).length;     }
					else if( objAggVal instanceof MVOrderedMap ){ intNumJobPlots *= ((MVOrderedMap)objAggVal).size(); }
				}
				_intNumPlots += intNumJobPlots;
			}
			System.out.println("Running " + _intNumPlots + " plots");
			
			for(int intJob=0; intJob < jobs.length; intJob++){
				runJob(jobs[intJob]);
			}

		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null )	con.close(); }catch(SQLException e){}
		}

		System.out.println("\n----  MVBatch Done  ----");
	}
	
	public static void runJob(MVPlotJob job) throws SQLException, IOException, InterruptedException {
		
		/*
		 * Run a query and build a set of plots for each group of dependent variables
		 */
		
		MVOrderedMap[] listDep = job.getDepGroups();
		for(int intDep=0; intDep < listDep.length; intDep++){
		
			//  get the dependent variable and fixed value maps for this group
			MVOrderedMap mapDep1 = (MVOrderedMap)listDep[intDep].get("dep1");
			MVOrderedMap mapDep2 = (MVOrderedMap)listDep[intDep].get("dep2");
			MVOrderedMap mapFix = (MVOrderedMap)listDep[intDep].get("fix");
			
			//  establish lists of entires for each group of variables and values
			Map.Entry[] listAggVal		= job.getAggVal().getOrderedEntries();
			Map.Entry[] listSeries1Val	= job.getSeries1Val().getOrderedEntries();
			Map.Entry[] listSeries2Val	= ( null != job.getSeries2Val()? job.getSeries2Val().getOrderedEntries() : new Map.Entry[]{}); 
			Map.Entry[] listDep1Plot	= mapDep1.getOrderedEntries();
			Map.Entry[] listDep2Plot	= ( null != mapDep2 ? mapDep2.getOrderedEntries() : new Map.Entry[]{});
			
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
					strSelectVar = "HOUR(sh.initdate) inithour";
					strSortVar = "inithour";
				} else if( strSelectVar.equals("initdate") ){
					strSelectVar = getSQLDateFormat("sh.initdate") + " initdate";
					strSortVar = "initdate";
				} else if( strSelectVar.equals("validhour") ){
					strSelectVar = "HOUR(sh.fcst_valid_beg) validhour";
					strSortVar = "validhour";
				} else if( strSelectVar.equals("fcst_valid_beg") ){
					strSelectVar = getSQLDateFormat("sh.fcst_valid_beg") + " fcst_valid_beg";
					strSortVar = "fcst_valid_beg";
				} else {
					strSelectVar = "sh." + strSelectVar;
					strSortVar = strSelectVar;
				}
				strSelectList += (0 < i? ",\n" : "") + "  " + strSelectVar;
				strSortList += (0 < i? ",\n" : "") + "  " + strSortVar;
			}
			
			//  add valid time, forecast variable, independent variable and stat group to the list
			strSelectList += ",\n  " + getSQLDateFormat("sh.fcst_valid_beg") + " fcst_valid_beg,\n  sh.fcst_var,\n";			
			if( job.getIndyVar().equals("initdate") || job.getIndyVar().equals("fcst_valid_beg") ){
				strSelectList += "  " + getSQLDateFormat("sh." + job.getIndyVar()) + " " + job.getIndyVar() + ",\n";
			} else {
				strSelectList += "  sh." + job.getIndyVar() + ",\n";
			}
			strSelectList += "  sg.stat_group_lu_id";
			
			//  build the where clause from the tables of field names and values
			String strWhere = "";
			
			//  build the aggregate fields where clause
			for(int i=0; i < listAggVal.length; i++){
				String strField = (String)listAggVal[i].getKey();
				if     ( strField.equals("inithour") )		{ strField = "HOUR(sh.initdate)"; 					}
				else if( strField.equals("initdate") )		{ strField = getSQLDateFormat("sh.initdate");		}
				else if( strField.equals("inithour") )		{ strField = "HOUR(sh.fcst_valid_beg)"; 			}
				else if( strField.equals("fcst_valid_beg") ){ strField = getSQLDateFormat("sh.fcst_valid_beg"); }
				else										{ strField = "sh." + strField;						}

				String strValueList = "";
				Object objValue = listAggVal[i].getValue();
				if( objValue instanceof String[] ){
					strValueList = buildValueList( (String[])objValue );
				} else if( objValue instanceof MVOrderedMap ){
					Map.Entry[] listSets = ((MVOrderedMap)objValue).getOrderedEntries();					
					for(int j=0; j < listSets.length; j++){
						strValueList += (0 == j? "" : ", ") + buildValueList( (String[])listSets[j].getValue() );
					} 					
				}
				strWhere += "  " + (0 < i? "AND " : "") + strField + " IN (" + strValueList + ")\n";
			}
			
			//  add the independent variable values, if necessary
			if( 0 < job.getIndyVal().length ){
				strWhere += "  AND sh." + job.getIndyVar() + " IN (" + buildValueList(job.getIndyVal()) + ")\n";
			}

			/*
			//  build the series fields where clause
			int intNumSeries = 0;
			for(int i=0; i < listSeries1Val.length; i++){
				String strField = (String)listSeries1Val[i].getKey();
				String[] listValues = (String[])listSeries1Val[i].getValue();
				strWhere += "  AND sh." + strField + " IN (" + buildValueList(listValues) + ")\n";
				intNumSeries += listValues.length;
			}
			*/
			
			//  combine the dependent variables for each axis into one list
			ArrayList listDepAll = new ArrayList( Arrays.asList(listDep1Plot) );
			listDepAll.addAll( Arrays.asList(listDep2Plot) );
			Map.Entry[] listDepPlot = (Map.Entry[])listDepAll.toArray(new Map.Entry[]{});			
			
			//  build the dependent variable where clause
			strWhere += "  AND\n  (\n";
			for(int i=0; i < listDepPlot.length; i++){
				String strFcstVar = (String)listDepPlot[i].getKey();
				String[] listStatGroupName = (String[])listDepPlot[i].getValue();
				String[] listStatGroupLuId = new String[listStatGroupName.length];
				for(int j=0; j < listStatGroupName.length; j++){
					listStatGroupLuId[j] = (String)_tableFcstVarIndex.get(listStatGroupName[j]);
				}
				
				//  fixed field sql
				String strFixed = "";
				MVOrderedMap mapFixed = (MVOrderedMap)mapFix.get(strFcstVar);
				Map.Entry[] listFixed = mapFixed.getOrderedEntries();
				for(int j=0; j < listFixed.length; j++){
					String strField = (String)listFixed[j].getKey();
					String strValue = (String)listFixed[j].getValue();
					strFixed += "      AND sh." + strField + formatSQLConstraint(strValue) + "\n";
				}
				
				//  build the series fields where clause
				String strSeries = "";
				Map.Entry[] listSeriesVal = (i < listDep1Plot.length? listSeries1Val : listSeries2Val);
				for(int j=0; j < listSeriesVal.length; j++){
					String strField = (String)listSeriesVal[j].getKey();
					String[] listValues = (String[])listSeriesVal[j].getValue();
					strSeries += "      AND sh." + strField + " IN (" + buildValueList(listValues) + ")\n";
				}
				
				strWhere += (0 < i? "    OR\n" : "") + "    (\n      sh.fcst_var = '" + strFcstVar + "'\n" +
							"      AND sg.stat_group_lu_id IN (" +	buildValueList(listStatGroupLuId) + ")\n" +
							strFixed + strSeries + "    )\n";
			}
			strWhere += "  )\n  AND sh.stat_header_id = sg.stat_header_id\n  AND sg.stat_value != -9999\n";

			//  determine if the job calls for confidence intervals and add the fields if necessary
			boolean boolNormalCI = false, boolBootCI = false;
			String[] listPlotCI = parseRCol(job.getPlotCI()); 
			for(int i=0; i < listPlotCI.length; i++){
				if     ( listPlotCI[i].equals("norm") ){ boolNormalCI = true; }
				else if( listPlotCI[i].equals("boot") ){ boolBootCI   = true; }
			}
			String strPlotCIFields = "";
			if( boolNormalCI ){ strPlotCIFields += ",\n  sg.stat_ncl,\n  sg.stat_ncu"; }
			if( boolBootCI )  { strPlotCIFields += ",\n  sg.stat_bcl,\n  sg.stat_bcu"; }
			
			//  put the query components together
			String strQuery = "SELECT\n" + strSelectList + ",\n  sg.stat_value" + strPlotCIFields + "\n" +
							  "FROM\n  stat_header sh,\n  stat_group sg\n" +
							  "WHERE\n" + strWhere + 
							  (_boolSQLSort? "ORDER BY\n" + strSortList : "") + ";";
			
			System.out.println("strQuery:\n\n" + strQuery + "\n");
			
			
			/*
			 *  Run the query
			 */

			//  run the query against the database connection and parse the results
			long intStartTime = (new java.util.Date()).getTime();
			PreparedStatement stmt = job.getConnection().prepareStatement(strQuery);
			ResultSet res = stmt.executeQuery();
			MVDataTable tab = new MVDataTable(res);
			System.out.println("query returned " + tab.getNumRows() + " rows in " + 
								formatTimeSpan( (new java.util.Date()).getTime() - intStartTime ));
			
			//  reformat the field names in the data table
			String[] listFields = tab.getFields();
			for(int i=0; i < listFields.length; i++){
				if( listFields[i].equals("HOUR(sh.initdate)") ){ 
					tab.setFieldName(i, "inithour");
				}
			}
			
			//  if there is no data, do not try to plot it
			if( 1 > tab.getNumRows() ){
				System.out.println("  **  WARNING: query returned no data");
				continue;
			}
						
			//  convert the stat_group_lu_id to stat_name
			tab.addField("stat_name");
			for(int i=0; i < listDepPlot.length; i++){
				final String strFcstVar = (String)listDepPlot[i].getKey();
				String[] listStatName = (String[])listDepPlot[i].getValue();
				
				for(int j=0; j < listStatName.length; j++){
					final String strStatGroupLuId = (String)_tableFcstVarIndex.get(listStatName[j]);
					MVRowComp c = new MVRowComp(){
						public boolean equals(MVOrderedMap row){
							String strFcstVarRow = (String)row.get("fcst_var"); 
							String strStatGroupLuIdRow = (String)row.get("stat_group_lu_id");
							return (strFcstVarRow.equals(strFcstVar) && strStatGroupLuIdRow.equals(strStatGroupLuId)); 
						}
					};
					tab.set("stat_name", c, listStatName[j]); 
				}				
			}
			
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

			//printFormattedResults(tab); System.out.println("" + tab.getNumRows() + " rows\n");
			
			//  determine if the indy values require tick marks
			boolean boolIndyValTick = false;
			String[] listIndyVal = job.getIndyVal();
			for(int i=0; i < listIndyVal.length; i++){
				try{ Double.parseDouble(listIndyVal[i]); }catch(Exception e){ boolIndyValTick = true; }
			}
			
			/*
			 *  Build a list of plot permutations to run
			 */
			MVOrderedMap[] listAggPerm = permute(job.getAggVal()).getRows();
			for(int intPerm=0; intPerm < listAggPerm.length; intPerm++){
				
				System.out.println("\n* * * * * * * * * * * *\n  PLOT - " + (_intPlotIndex++ + 1) + " / " + _intNumPlots + "\n* * * * * * * * * * * *\n");
				
				/*
				 *  Build a data table that contains the data specific to this permutation 
				 */

				MVDataTable tabPerm = tab;
				String[] listKeys = listAggPerm[intPerm].keyList();
				for(int i=0; i < listKeys.length; i++){
					final String strKey = listKeys[i];
					final String strVal = (String)listAggPerm[intPerm].get(strKey);
					tabPerm = tabPerm.subset(new MVRowComp(){
						public boolean equals(MVOrderedMap row){
							return row.get(strKey).equals(strVal);
						}
					});
				}
				
					
				/*
				 *  Build a map of all plot-specific values to use with the templates 
				 */

				MVOrderedMap mapTmplVals = new MVOrderedMap();

				//  add the independent and dependent variables 
				mapTmplVals.put("indy_var", job.getIndyVar());
				Map.Entry[][] listDepPlotList = {listDep1Plot, listDep2Plot};
				for(int intDepPlot = 0; intDepPlot < 2; intDepPlot++){
					Map.Entry[] listDepCur = listDepPlotList[intDepPlot];
					String strDepName = "dep" + (intDepPlot+1);
					
					//  add the stats for each fcst_var
					for(int i=0; i < listDepCur.length; i++){
						String strFcstVar = (String)listDepCur[i].getKey();
						mapTmplVals.put(strDepName + "_" + (i+1), strFcstVar);
						String[] listStats = (String[])listDepCur[i].getValue();
						for(int j=0; j < listStats.length; j++){
							mapTmplVals.put(strDepName + "_" + (i+1) + "_stat" + (j+1), listStats[j]);
						}
						
						MVOrderedMap mapFixCur = (MVOrderedMap)mapFix.get(strFcstVar);
						Map.Entry[] listFixCurVal = mapFixCur.getOrderedEntries();
						for(int j=0; j < listFixCurVal.length; j++){
							mapTmplVals.put((String)listFixCurVal[j].getKey(), (String)listFixCurVal[j].getValue());
						}
					}
				}

				//  add the aggregate values to the template values map
				mapTmplVals.putAll(listAggPerm[intPerm]);
				System.out.println(mapTmplVals.getRDecl() + "\n");

				if( 1 > tabPerm.getNumRows() ){
					System.out.println("no plot data found");
					continue;
				}
				
				//printFormattedResults(tabPerm); System.out.println("" + tabPerm.getNumRows() + " rows\n");
				System.out.println("Plotting " + tabPerm.getNumRows() + " rows\n");

				
				/*
				 *  Print the data file in the R_work subfolder and file specified by the data file template
				 */
				_strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/")? "" : "/");
				_strPlotFolder = _strPlotFolder + (_strPlotFolder.endsWith("/")? "" : "/");

				String strDataFile	= _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapTmplVals, job.getTmplMaps());
				(new File(strDataFile)).getParentFile().mkdirs();
				printFormattedResults(tabPerm, new PrintStream(strDataFile), "\t");
				tabPerm = null;
				
				
				/*
				 *  Generate filenames and plot labels from the templates 
				 */

				//  use the map of all plot values to populate the template strings
				String strPlotFile	= _strPlotFolder + buildTemplateString(job.getPlotFileTmpl(), mapTmplVals, job.getTmplMaps());
				String strRFile		= _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapTmplVals, job.getTmplMaps());
				String strTitle		= buildTemplateString(job.getTitleTmpl(), mapTmplVals, job.getTmplMaps());
				String strXLabel	= buildTemplateString(job.getXLabelTmpl(), mapTmplVals, job.getTmplMaps());
				String strY1Label	= buildTemplateString(job.getY1LabelTmpl(), mapTmplVals, job.getTmplMaps());				
				String strY2Label	= buildTemplateString(job.getY2LabelTmpl(), mapTmplVals, job.getTmplMaps());				

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
				tableRTags.put("dep1_plot",		mapDep1.getRDecl());
				tableRTags.put("dep2_plot",		(null != mapDep2? mapDep2.getRDecl() : "c()"));
				tableRTags.put("agg_list",		listAggPerm[intPerm].getRDecl());
				tableRTags.put("series1_list",	job.getSeries1Val().getRDecl());
				tableRTags.put("series2_list",	(null != job.getSeries2Val()? job.getSeries2Val().getRDecl() : "c()"));
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
				tableRTags.put("indy_stagger",	(job.getIndyStagger()?	"TRUE" : "FALSE"));
				tableRTags.put("grid_on",		(job.getGridOn()?		"TRUE" : "FALSE"));
				tableRTags.put("sync_axes",		(job.getSyncAxes()?		"TRUE" : "FALSE"));
				
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
				tableRTags.put("box_boxwex",	job.getBoxBoxwex());
				tableRTags.put("box_notch",		job.getBoxNotch());
				
				int intNumDep1Series = intNumDep1 * (intNumSeries1Perm + (job.getPlot1Diff()? 1 : 0));
				int intNumDep2Series = intNumDep2 * (intNumSeries2Perm + (job.getPlot2Diff()? 1 : 0));
				int intNumDepSeries = intNumDep1Series + intNumDep2Series;
				
				tableRTags.put("plot_ci",	job.getPlotCI().equals("")? printRCol( rep("none", intNumDepSeries) )	: job.getPlotCI());
				tableRTags.put("colors",	job.getColors().equals("")?	"rainbow(" + intNumDepSeries + ")"		: job.getColors());
				tableRTags.put("pch",		job.getPch().equals("")?	printRCol( rep(20, intNumDepSeries) )	: job.getPch());
				tableRTags.put("type",		job.getType().equals("")?	printRCol( rep("b",	intNumDepSeries) )	: job.getType());
				tableRTags.put("lty",		job.getLty().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLty());
				tableRTags.put("lwd",		job.getLwd().equals("")?	printRCol( rep(1, intNumDepSeries) )	: job.getLwd());
				tableRTags.put("y1_lim",	"c(" + job.getY1Lim() + ")");
				tableRTags.put("y2_lim",	"c(" + job.getY2Lim() + ")");
				
				
				/*
				 *  Read the template in, replacing the appropriate tags with generated R code
				 */
				
				BufferedReader reader = new BufferedReader( new FileReader(_strRtmplFolder + job.getPlotTmpl()) );
				PrintStream writer = new PrintStream(strRFile);
				while( reader.ready() ){
					String strRtmplLine = reader.readLine();
					String strRPlotLine = strRtmplLine;
					
					Matcher matRtmplLine = _patRTmpl.matcher(strRtmplLine);
					while( matRtmplLine.find() ){
						String strRtmplTag = matRtmplLine.group(1);
						if( !tableRTags.containsKey(strRtmplTag) ){ continue; }
						String strRTagVal = (String)tableRTags.get(strRtmplTag);
						strRPlotLine = strRPlotLine.replace("#<" + strRtmplTag + ">#", strRTagVal);
					}
					
					writer.println(strRPlotLine);
				}
				reader.close();
				writer.close();
				tableRTags = null;
	
				
				/*
				 *  Attempt to run the generated R script
				 */			

				if( _boolPlot ){
					System.out.println("\nRunning 'Rscript " + strRFile + "'");
					Process proc = Runtime.getRuntime().exec("Rscript " + strRFile);
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
					
					System.out.println("\nRunning Rscript complete\n");
				}
			} // end: for(int intPerm=0; intPerm < listAggPerm.length; intPerm++)

			res = null;
			tab.clear();
			tab = null;
			//System.gc();
			
		} // end: for(int intDep=0; intDep < listDep.length; intDep++)

	}

	/*
	public static final SimpleDateFormat m_formatDB = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
	public static final SimpleDateFormat m_formatDBms = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S", Locale.US);
	public static final SimpleDateFormat m_formatPlot = new SimpleDateFormat("yyyyMMddHH", Locale.US);
	*/

	/**
	 * Prints a textual representation of the input {@link ResultSet} with the column names in the 
	 * first row to the specified {@link PrintStream} destination.  
	 * @param res The ResultSet to print
	 * @param str The stream to write the formatted results to (defaults to System.out)
	 * @param delim The delimiter to insert between field headers and values (defaults to ' ')
	 * @throws SQLException
	 */
	public static void printFormattedResults(ResultSet res, PrintStream str, String delim) throws SQLException{
		ResultSetMetaData met = res.getMetaData();

		for(int i=1; i < met.getColumnCount()+1; i++){
			String strField = (delim.equals(" ")? padEnd(met.getColumnName(i)) : (1 < i? delim : "") + met.getColumnName(i));
			str.print( strField );
		}
		str.println();					
		while( res.next() ){
			for(int i=1; i < met.getColumnCount()+1; i++){
				String strVal = (delim.equals(" ")? padEnd(res.getString(i)) : (1 < i? delim : "") + res.getString(i));
				str.print( strVal );
			}
			str.println();
		}
	}
	public static void printFormattedResults(ResultSet res) throws SQLException{ printFormattedResults(res, System.out, " "); }
	
	/**
	 * Prints a textual representation of the input {@link MVDataTable} with the field names in the 
	 * first row to the specified {@link PrintStream} destination.  
	 * @param res The MVDataTable to print
	 * @param str The stream to write the formatted results to (defaults to System.out)
	 * @param delim The delimiter to insert between field headers and values (defaults to ' ')
	 */
	public static void printFormattedResults(MVDataTable tab, PrintStream str, String delim){
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
		for(int i=0; i < rows.length; i++){
			for(int j=0; j < fields.length; j++){
				String strVal = (String)rows[i].get(fields[j]); 
				str.print( delim.equals(" ")? padEnd(strVal, intFieldWidths[j]) : (0 < j? delim : "") + strVal );
			}
			str.println();
		}
	}
	public static void printFormattedResults(MVDataTable tab){ printFormattedResults(tab, System.out, " "); }

	public static String formatSQLConstraint(String value){
		String strRet = " = '" + value + "'";
		
		Matcher matBetween = _patDateRange.matcher(value);
		if( matBetween.matches() ){ strRet = " BETWEEN '" + matBetween.group(1) + "' AND '" + matBetween.group(2) + "'"; }
		
		return strRet;
	}
	
	public static final Pattern _patDateRange	= Pattern.compile("\\s*between\\(\\s*([^,]+)\\s*,\\s*([^\\)]+)\\s*\\)\\s*");
	
	/**
	 * Mapping from stat_name to stat_group_lu_id, used in SQL queries
	 */
	public static final Hashtable _tableFcstVarIndex = new Hashtable();
	static{
		_tableFcstVarIndex.put("BASER", 	"0");
		_tableFcstVarIndex.put("FMEAN", 	"1");
		_tableFcstVarIndex.put("ACC", 		"2");
		_tableFcstVarIndex.put("FBIAS", 	"3");
		_tableFcstVarIndex.put("PODY", 		"4");
		_tableFcstVarIndex.put("PODN", 		"5");
		_tableFcstVarIndex.put("POFD", 		"6");
		_tableFcstVarIndex.put("FAR", 		"7");
		_tableFcstVarIndex.put("CSI", 		"8");
		_tableFcstVarIndex.put("GSS", 		"9");
		_tableFcstVarIndex.put("HK", 		"10");
		_tableFcstVarIndex.put("HSS", 		"11");
		_tableFcstVarIndex.put("ODDS", 		"12");
		_tableFcstVarIndex.put("FBAR", 		"13");
		_tableFcstVarIndex.put("FSTDEV", 	"14");
		_tableFcstVarIndex.put("OBAR", 		"15");
		_tableFcstVarIndex.put("OSTDEV",	"16");
		_tableFcstVarIndex.put("PR_CORR",	"17");
		_tableFcstVarIndex.put("ME", 		"18");
		_tableFcstVarIndex.put("ESTDEV", 	"19");
		_tableFcstVarIndex.put("MBIAS", 	"20");
		_tableFcstVarIndex.put("MAE", 		"21");
		_tableFcstVarIndex.put("MSE", 		"22");
		_tableFcstVarIndex.put("BCMSE", 	"23");
		_tableFcstVarIndex.put("BCRMSE", 	"23");
		_tableFcstVarIndex.put("RMSE", 		"24");
		_tableFcstVarIndex.put("E10", 		"25");
		_tableFcstVarIndex.put("E25", 		"26");
		_tableFcstVarIndex.put("E50", 		"27");
		_tableFcstVarIndex.put("E75", 		"28");
		_tableFcstVarIndex.put("E90", 		"29");
		_tableFcstVarIndex.put("BRIER", 	"30");
		_tableFcstVarIndex.put("NBR_BASER",	"31");
		_tableFcstVarIndex.put("NBR_FMEAN",	"32");
		_tableFcstVarIndex.put("NBR_ACC", 	"33");
		_tableFcstVarIndex.put("NBR_FBIAS", "34");
		_tableFcstVarIndex.put("NBR_PODY", 	"35");
		_tableFcstVarIndex.put("NBR_PODN", 	"36");
		_tableFcstVarIndex.put("NBR_POFD", 	"37");
		_tableFcstVarIndex.put("NBR_FAR", 	"38");
		_tableFcstVarIndex.put("NBR_CSI", 	"39");
		_tableFcstVarIndex.put("NBR_GSS", 	"40");
		_tableFcstVarIndex.put("NBR_HK", 	"41");
		_tableFcstVarIndex.put("NBR_HSS", 	"42");
		_tableFcstVarIndex.put("NBR_ODDS", 	"43");
		_tableFcstVarIndex.put("NBR_FBS", 	"44");
		_tableFcstVarIndex.put("NBR_FSS", 	"45");
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



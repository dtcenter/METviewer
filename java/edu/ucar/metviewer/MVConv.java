package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class MVConv extends MVUtil {
	
	public static Connection _conSrc		= null;
	public static Connection _conDest		= null;
	
	public static String _strSrc			= "";
	public static String _strDest			= "";
	
	public static String _strDBHost			= "orval.rap.ucar.edu";
	public static String _strDBUser			= "pgoldenb";
	public static String _strDBPassword		= "pgoldenb";
	
	public static Hashtable _tableStatHeaders = new Hashtable();
	public static int _intStatHeaderIdNext = 0;
	
	public static String getUsage(){
		return	"Usage:  mv_conv\n" +
				"          {src_db}\n" +
				"          {dest_db}\n" +				
				"\n" +
				"          where   src_db specifies the source database which has the old schema\n" +
				"                  dest_db specifies the destination database which has the new schema\n";
	}

	public static void main(String[] argv) {
		System.out.println("----  MVConv  ----\n");
		
		//  validate the input
		if( 2 != argv.length ){
			System.out.println("  **  ERROR: unexpected number of arguments\n\n" + getUsage() + "\n----  MVConv Complete  ----\n");
			return;
		}
		
		try {
			
			//  connect to the source database
			_strSrc = argv[0];
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			_conSrc = DriverManager.getConnection("jdbc:mysql://" + _strDBHost + "/" + _strSrc, _strDBUser, _strDBPassword);
			if( _conSrc.isClosed() ){ throw new Exception("failed to connect to source database " + _strSrc); }
			System.out.println("   src db: " + _strSrc);

			//  connect to the source database
			_strDest = argv[1];
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			_conDest = DriverManager.getConnection("jdbc:mysql://" + _strDBHost + "/" + _strDest, _strDBUser, _strDBPassword);
			if( _conDest.isClosed() ){ throw new Exception("failed to connect to destination database " + _strDest); }
			System.out.println("  dest db: " + _strDest + "\n");
			
			//  populate the local data members with stat_group_lu information
			buildStatDescTables();
			
			//  copy the data file information
			System.out.print("  copying datafiles...  ");
			Statement stmtDataFile = _conSrc.createStatement(); 
			int intDataFiles = stmtDataFile.executeUpdate("INSERT INTO " + _strDest + ".data_file SELECT * FROM " + _strSrc + ".data_file");
			System.out.println("done: copied " + intDataFiles + " records\n");
			stmtDataFile.close();
		
			//  determine the number of stat_header rows
			int intNumStatId = getTableSize(_conSrc, "stat_header");
			System.out.println("  processing " + intNumStatId + " stat_header rows\n");
			
			//  bookkeeping for performance information
			long intStartTime = (new java.util.Date()).getTime();
			System.out.println("  start time: " + _formatDB.format(new java.util.Date()) + "\n");
			
			//  gather the list of stat_header_ids from the source database
			Statement stmtId = _conSrc.createStatement();
			ResultSet resId = stmtId.executeQuery("SELECT stat_header_id FROM stat_header ORDER BY stat_header_id;");
			
			//  for each stat_header record, gather the line_data information
			int intIdNum = 0;
			while( resId.next() ){
				int intStatId = resId.getInt(1);
				System.out.print( "\r  Progress: " + formatPerc(intIdNum, intNumStatId) + " - " + intStatId + " / " + intNumStatId );
				System.out.flush();

				//  convert the stat_header information and get the lead/init/valid time information
				MVConvStatHeader sh = convertStatHeader(intStatId);
				
				//  copy all line_data records for the current stat_header
				for(int i=0; i < _listLineDataTables.length; i++){ copyTableRecord(_listLineDataTables[i], sh); }
				
				intIdNum++;
			}
			stmtId.close();
			
			//  copy the mode tables
			System.out.print("\r  Progress: all stat_header_id values copied              \n\n  copying mode tables...  ");
			Statement stmtMode = _conSrc.createStatement();
			int intModeCopy = stmtMode.executeUpdate("INSERT INTO " + _strDest + ".mode_header     SELECT * FROM " + _strSrc + ".mode_header;");
			intModeCopy    += stmtMode.executeUpdate("INSERT INTO " + _strDest + ".mode_cts        SELECT * FROM " + _strSrc + ".mode_cts;");
			intModeCopy    += stmtMode.executeUpdate("INSERT INTO " + _strDest + ".mode_obj_single SELECT * FROM " + _strSrc + ".mode_obj_single;");
			intModeCopy    += stmtMode.executeUpdate("INSERT INTO " + _strDest + ".mode_obj_pair   SELECT * FROM " + _strSrc + ".mode_obj_pair;");
			System.out.println("done: copied " + intModeCopy + " mode records\n");			

			//  determine the number of stat_headers in the new database
			int intNumSrc = intNumStatId;
			int intNumDest = getTableSize(_conDest, "stat_header");
			
			//  print out a report line for the stat_header table
			System.out.println("  " + padEnd("table", 18) +       padEnd("source", 12) +                       "dest\n  " +
							   padEnd("stat_header", 18) + padEnd(padBegin("" + intNumSrc, 10), 12) + padBegin("" + intNumDest, 10));
	
			//  print out a report of the number of lines moved for all line_data tables
			String[] listLineDataTables = _tableLineCounts.getKeyList();
			for(int i=0; i < _listLineDataTables.length; i++){
				
				//  determine the number of source and destination records
				intNumSrc = getTableSize(_conSrc, _listLineDataTables[i]);
				intNumDest = getTableSize(_conDest, _listLineDataTables[i]);
				
				//  print the report line for the table
				System.out.println("  " + padEnd(_listLineDataTables[i], 18) + padEnd(padBegin("" + intNumSrc, 10), 12) + padBegin("" + intNumDest, 10));
			}

			//  report the mode table sizes
			System.out.println("  " + padEnd("mode_header", 18) + 
							   padEnd(padBegin("" + getTableSize(_conSrc, "mode_header"), 10), 12) + 
							   padBegin("" + getTableSize(_conDest, "mode_header"), 10));
			System.out.println("  " + padEnd("mode_cts", 18) + 
					   		   padEnd(padBegin("" + getTableSize(_conSrc, "mode_cts"), 10), 12) +
					   		   padBegin("" + getTableSize(_conDest, "mode_cts"), 10));
			System.out.println("  " + padEnd("mode_obj_single", 18) + 
					   		   padEnd(padBegin("" + getTableSize(_conSrc, "mode_obj_single"), 10), 12) +
					   		   padBegin("" + getTableSize(_conDest, "mode_obj_single"), 10));
			System.out.println("  " + padEnd("mode_obj_pair", 18) + 
					   		   padEnd(padBegin("" + getTableSize(_conSrc, "mode_obj_pair"), 10), 12) +
					   		   padBegin("" + getTableSize(_conDest, "mode_obj_pair"), 10));
			
			System.out.println("\n\n" +
							   "      end time: " + _formatDB.format(new java.util.Date()) + "\n" +
							   "  time elapsed: " + formatTimeSpan( (new java.util.Date()).getTime() - intStartTime ) + "\n");
			
			//  copy all mode information
			
			
		} catch(Exception ex){
			System.out.println("  **  ERROR: caught " + ex.getClass() + ": " + ex.getMessage() + "\n");
			ex.printStackTrace();
		}
		System.out.println("----  MVConv Complete  ----\n");
	}
		
	public static final DecimalFormat _formatPerc = new DecimalFormat("0.000");
	public static String formatPerc(int val, int max){
		double dblPerc = (double)val * 100 / (double)max;
		String strPerc = _formatPerc.format(dblPerc);
		return (3 > strPerc.length()? " " : "") + (2 > strPerc.length()? " " : "") + strPerc + "%";
	}
	
	/**
	 * Read the stat_header record from the src database with the input stat_header_id and either build
	 * or retrieve the stat_header record in the destination database.  Create a data structure that contains
	 * the time information from the src stat_header record and return it.
	 * @param intStatId stat_header_id of the src stat_header record
	 * @return data structure containing stat_header_id and valid/init/lead time information
	 * @throws Exception
	 */
	public static MVConvStatHeader convertStatHeader(int intStatId) throws Exception{

		MVConvStatHeader ret = new MVConvStatHeader();
		ret._intStatHeaderIdSrc = intStatId;
		
		//  build the select statement for the src stat_header table
		String strStatSqlSel = "SELECT " +
		    "  version, " +
		    "  model, " +
		    "  fcst_lead, " +
		    "  fcst_valid_beg, " +
		    "  fcst_valid_end, " +
		    "  fcst_init_beg, " +
		    "  obs_lead, " +
		    "  obs_valid_beg, " +
		    "  obs_valid_end, " +
		    "  fcst_var, " +
		    "  fcst_lev, " +
		    "  obs_var, " +
		    "  obs_lev, " +
		    "  obtype, " +
		    "  vx_mask, " +
		    "  interp_mthd, " +
		    "  interp_pnts, " +
		    "  fcst_thresh, " +
		    "  obs_thresh " +
		    "FROM stat_header WHERE stat_header_id = " + intStatId + ";";

		//  get the stat_header information
		Statement stmtStat = _conSrc.createStatement(); 
		ResultSet resStat = stmtStat.executeQuery(strStatSqlSel);
		
		if( !resStat.next() ){ throw new Exception("src stat_header select failed for stat_header_id " + intStatId); }
		
		//  build a list of dest stat_header values
		String strStatValues = 
		    "'" + resStat.getString(1) + "', " +		//  version
		    "'" + resStat.getString(2) + "', " +		//  model
		    "'" + resStat.getString(10) + "', " +		//  fcst_var
		    "'" + resStat.getString(11) + "', " +		//  fcst_lev
		    "'" + resStat.getString(12) + "', " +		//  obs_var
		    "'" + resStat.getString(13) + "', " +		//  obs_lev
		    "'" + resStat.getString(14) + "', " +		//  obtype
		    "'" + resStat.getString(15) + "', " +		//  vx_mask
		    "'" + resStat.getString(16) + "', " +		//  interp_mthd
		    "'" + resStat.getString(17) + "', " +		//  interp_pnts
		    "'" + resStat.getString(18) + "', " +		//  fcst_thresh
		    "'" + resStat.getString(19) + "'";			//  obs_thresh
		
		//  determine the stat_header_id for the current src stat_header info
		boolean boolStatHeaderExists = _tableStatHeaders.containsKey(strStatValues);
		if( boolStatHeaderExists ){
			ret._intStatHeaderIdDest = ((Integer)_tableStatHeaders.get(strStatValues)).intValue();
		} else {
			ret._intStatHeaderIdDest = _intStatHeaderIdNext++;
			_tableStatHeaders.put(strStatValues, new Integer(ret._intStatHeaderIdDest));
		}
		
		//  if the src stat_header info needs to be inserted into the dest stat_header, do so 
		if( !boolStatHeaderExists ){
			String strStatSqlIns = "INSERT INTO stat_header VALUES (" + ret._intStatHeaderIdDest + ", " + strStatValues + ");";

			//  get the stat_header information
			Statement stmtStatIns = _conDest.createStatement(); 
			int intStatIns = stmtStatIns.executeUpdate(strStatSqlIns);
			if( 1 != intStatIns ){ throw new Exception("unexpected number of records inserted into dest stat_header: " + intStatIns); }					
			stmtStatIns.close();
		}
		
		//  store the src stat_header time information
		ret._strFcstLead		= resStat.getString(3);
		ret._strFcstValidBeg	= resStat.getString(4);
		ret._strFcstValidEnd	= resStat.getString(5);
		ret._strFcstInitBeg		= resStat.getString(6);
		ret._strObsLead			= resStat.getString(7);
		ret._strObsValidBeg		= resStat.getString(8);
		ret._strObsValidEnd		= resStat.getString(9);
		
		stmtStat.close();
		
		return ret;
	}
	
	/**
	 * Create and execute a SELECT COUNT(*) for the input table against the input database connection.  If no
	 * result is found, -1 is returned.  Otherwise, the size of the table is returned. 
	 * @param con database connection to execute query against
	 * @param strTable database table name whose size to retrieve
	 * @return number of records in the input table
	 * @throws Exception
	 */
	public static int getTableSize(Connection con, String strTable) throws Exception{
		Statement stmt = con.createStatement();
		ResultSet resSize = stmt.executeQuery("SELECT COUNT(*) FROM " + strTable + ";");
		int intSize = -1;
		while( resSize.next() ){ intSize = resSize.getInt(1); }
		stmt.close();
		return intSize;
	}
	
	/**
	 * Query the source database for a stat_group with the input stat_header_id and stat_group_lu_id.  Build
	 * a string of values intended for an insert statement for the stat, including CI information, if
	 * appropriate.
	 * @param intStatHeaderId stat_header_id of the desired stat_group
	 * @param intStatLuId stat_group_lu_id of the desired stat_group
	 * @return String representation of the values in the stat_group
	 * @throws Exception
	 */
	public static String buildStatValueList(int intStatHeaderId, int intStatLuId) throws Exception{

		//  build the stat_group sql
		String strStatSql = "SELECT stat_value, stat_ncl, stat_ncu, stat_bcl, stat_bcu FROM stat_group " +
							"WHERE stat_header_id = " + intStatHeaderId + " AND stat_group_lu_id = " + intStatLuId + ";";
		
		//  get the stat_header information
		Statement stmtStat = _conSrc.createStatement(); 
		ResultSet resStat = stmtStat.executeQuery(strStatSql);
		
		//  determine if the current stat has normal or bootstrap CIs
		boolean boolStatNc = Boolean.parseBoolean( _tableStatNc.get(new Integer(intStatLuId)).toString() );
		boolean boolStatBc = Boolean.parseBoolean( _tableStatBc.get(new Integer(intStatLuId)).toString() );
		
		//  build strings containing the database stat values
		String strStatVal = "-9999";
		String strStatNc = "-9999, -9999";
		String strStatBc = "-9999, -9999";
		if( resStat.next() ){
			strStatVal = resStat.getString(1);
			strStatNc = resStat.getString(2) + ", " + resStat.getString(3);
			strStatBc = resStat.getString(4) + ", " + resStat.getString(5);
		}
		stmtStat.close();
		
		//  return an insert statement string that contains the stat value and appropriate CIs
		return strStatVal + 
			   (boolStatNc? ", " + strStatNc : "") + 
			   (boolStatBc? ", " + strStatBc : "");
	}
	
	public static Hashtable _tableStatNc = new Hashtable();
	public static Hashtable _tableStatBc = new Hashtable();
	
	/**
	 * Build tables of stat_group_lu_id / CI usage pairs for normal and bootstrap CIs.  The information
	 * to build the tables is gathered from the source database stat_group_lu table and stored in the
	 * _tableStatNc and _tableStatBc data members.
	 * @throws Exception
	 */
	public static void buildStatDescTables() throws Exception{

		//  get the stat_header_lu information
		Statement stmtStat = _conSrc.createStatement(); 
		ResultSet resStat = stmtStat.executeQuery("SELECT * FROM stat_group_lu;");
		
		//  build the CI tables with the information
		while( resStat.next() ){
			Integer intStatGroupLuId = new Integer(resStat.getInt(1));
			_tableStatNc.put(intStatGroupLuId, "" + resStat.getBoolean(4));
			_tableStatBc.put(intStatGroupLuId, "" + resStat.getBoolean(5));
		}
		stmtStat.close();
	}
	
	public static final SimpleDateFormat _formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Copy all line_data tables which point at the input source stat_header_id to the destination database.
	 * The records in the destination database will all point to the appropriate stat_header record there.  Variable
	 * length records will also be copied.
	 * @param strTable line_data table to copy
	 * @param sh stat_header information from the source and destination databases
	 * @throws Exception
	 */
	public static void copyTableRecord(String strTable, MVConvStatHeader sh) throws Exception{

		//  get the stat_header_lu information
		Statement stmtStat = _conSrc.createStatement(); 
		ResultSet resStat = stmtStat.executeQuery("SELECT * FROM " + strTable + " WHERE stat_header_id = " + sh._intStatHeaderIdSrc + ";");
		
		while( resStat.next() ){
		
			//  build the common section of the new record
			String strValueList = "";
			
			//  if the table contains a line_data_id field, use the original value
			String strLineDataId = "";
			if( _tableLineVarLength.containsKey(strTable) ){
				strLineDataId = resStat.getString(1);
				strValueList += strLineDataId + ", ";
			}
			
			//  fill in the common values for the line
			strValueList += 
				sh._intStatHeaderIdDest + ", " +			//  stat_header_id
				resStat.getString(3) + ", " +				//  data_file_id
				resStat.getString(4) + ", " +				//  line_num
				sh._strFcstLead + ", " +					//  fcst_lead
				"'" + sh._strFcstValidBeg + "', " +			//  fcst_valid_beg
				"'" + sh._strFcstValidEnd + "', " +			//  fcst_valid_end
				"'" + sh._strFcstInitBeg + "', " +			//  fcst_init_beg
				sh._strObsLead + ", " +						//  obs_lead
				"'" + sh._strObsValidBeg + "', " +			//  obs_valid_beg
				"'" + sh._strObsValidEnd + "'";				//  obs_valid_end
			
			//  add the cov_thresh and total fields, if necessary
			int intFieldIndex = 6;
			if( _tableLineCovThresh.containsKey(strTable) ){ strValueList += ", '" + resStat.getString(intFieldIndex++) + "'"; }
			if( _tableLineAlpha.containsKey(strTable)     ){ strValueList += ", " + resStat.getString(intFieldIndex++); }
			
			//  add the total
			strValueList += ", " + resStat.getString(5);					//  total
			
			//  add the non-stat data section to the new record
			if( !strTable.equals("line_data_cnt") && !strTable.equals("line_data_rhist") ){
				int intNumFields = resStat.getMetaData().getColumnCount();
				for(int i=intFieldIndex; i <= intNumFields; i++){ strValueList += ", '" + resStat.getString(i) + "'"; }
			}
			
			//  determine if the table has stat_group stats, and build the value list accordingly
			if( _tableLineStats.containsKey(strTable) ){
				
				//  gather the stats from the stat_group table
				int[] listStatId = (int[])_tableLineStats.get(strTable);
				for(int i=0; i < listStatId.length; i++){
					if( 5 == i && strTable.equals("line_data_cnt") ){
						strValueList += ", " +
							resStat.getString(7) + ", " +					//	sp_corr
							resStat.getString(8) + ", " +					//	dt_corr
							resStat.getString(9) + ", " +					//	ranks
							resStat.getString(10) + ", " +					//	frank_ties
							resStat.getString(11);							//	orank_ties
					}
					strValueList += ", " + buildStatValueList(sh._intStatHeaderIdSrc, listStatId[i]);
				}
				if( strTable.equals("line_data_rhist") ){ strValueList += ", " + resStat.getString(6); }
			}
			
			//  build an insert statement for the line_data record
			Statement stmtDest = _conDest.createStatement();
			int intDestRes = stmtDest.executeUpdate("INSERT INTO " + strTable + " VALUES (" + strValueList + ");");
			if( 1 != intDestRes ){ throw new Exception("unexpected result from " + strTable + " insert: " + intDestRes); }
			stmtDest.close();
			
			//  if there are variable length components to the data line, move them
			if( _tableLineVarLength.containsKey(strTable) ){
				
				//  build the insert statement for the variable length line data
				String strTableVar = _tableLineVarLength.get(strTable).toString();
				String strVarSql = "INSERT INTO " + _strDest + "." + strTableVar + " " +
								   "SELECT * FROM " + _strSrc + "." + strTableVar + " WHERE line_data_id = " + strLineDataId + ";";
				
				//  execute the insert and complain if no data was moved
				Statement stmtVar = _conSrc.createStatement();
				int intVarRes = stmtVar.executeUpdate(strVarSql);
				if( 1 > intVarRes ){
					throw new Exception("unexpected result populating table " + strTableVar + " for line_data_id " + strLineDataId);
				}
				stmtVar.close();
				
				//  record the number of variable length records added
				int intLineVarCount = ((Integer)_tableLineCounts.get(strTableVar)).intValue();
				_tableLineCounts.put(strTableVar, new Integer(intLineVarCount + intVarRes));
			}
			
			//  update the counter for the current line type
			int intLineCount = ((Integer)_tableLineCounts.get(strTable)).intValue();
			_tableLineCounts.put(strTable, new Integer(intLineCount + 1));

		}	//  end: while( resStat.next() )
		stmtStat.close();
	}
	
	public static final String[] _listLineDataTables = {
		"line_data_fho",
		"line_data_ctc",
		"line_data_cts",
		"line_data_cnt",
		"line_data_pct",
		"line_data_pstd",
		"line_data_pjc",
		"line_data_prc",
		"line_data_sl1l2",
		"line_data_sal1l2",
		"line_data_vl1l2",
		"line_data_val1l2",
		"line_data_mpr",
		"line_data_nbrctc",
		"line_data_nbrcts",
		"line_data_nbrcnt",
		"line_data_isc",
		"line_data_mctc",
		"line_data_mcts",
		"line_data_rhist",
		"line_data_orank"
	};
	public static final MVOrderedMap _tableLineCounts = new MVOrderedMap();
	static {
		for(int i=0; i < _listLineDataTables.length; i++){
			_tableLineCounts.put(_listLineDataTables[i], new Integer(0));
		}
		_tableLineCounts.put("line_data_mctc_cnt",		new Integer(0));
		_tableLineCounts.put("line_data_pct_thresh",	new Integer(0));
		_tableLineCounts.put("line_data_pstd_thresh",	new Integer(0));
		_tableLineCounts.put("line_data_pjc_thresh",	new Integer(0));
		_tableLineCounts.put("line_data_prc_thresh",	new Integer(0));
		_tableLineCounts.put("line_data_rhist_rank",	new Integer(0));
		_tableLineCounts.put("line_data_orank_ens",		new Integer(0));
	}
	
	public static final Hashtable _tableLineStats = new Hashtable();
	static {
		_tableLineStats.put("line_data_cts",		new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
		_tableLineStats.put("line_data_cnt",		new int[]{13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29});
		_tableLineStats.put("line_data_pstd",		new int[]{50, 31, 32, 33, 34, 30});
		_tableLineStats.put("line_data_nbrcts",		new int[]{35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47});
		_tableLineStats.put("line_data_nbrcnt",		new int[]{48, 49});
		_tableLineStats.put("line_data_mcts",		new int[]{51, 52, 53, 54});
		_tableLineStats.put("line_data_rhist",		new int[]{55, 56});
	}
	
	public static final Hashtable _tableLineVarLength = new Hashtable();
	static {
		_tableLineVarLength.put("line_data_mctc", "line_data_mctc_cnt");
		_tableLineVarLength.put("line_data_pct", "line_data_pct_thresh");
		_tableLineVarLength.put("line_data_pstd", "line_data_pstd_thresh");
		_tableLineVarLength.put("line_data_pjc", "line_data_pjc_thresh");
		_tableLineVarLength.put("line_data_prc", "line_data_prc_thresh");
		_tableLineVarLength.put("line_data_rhist", "line_data_rhist_rank");
		_tableLineVarLength.put("line_data_orank", "line_data_orank_ens");
	}
	
	public static final Hashtable _tableLineCovThresh = new Hashtable();
	static {
		_tableLineCovThresh.put("line_data_nbrctc", "true");
		_tableLineCovThresh.put("line_data_nbrcts", "true");
	}
	
	public static final Hashtable _tableLineAlpha = new Hashtable();
	static {
		_tableLineAlpha.put("line_data_cts", "true");
		_tableLineAlpha.put("line_data_cnt", "true");
		_tableLineAlpha.put("line_data_mcts", "true");
		_tableLineAlpha.put("line_data_pstd", "true");
		_tableLineAlpha.put("line_data_nbrcts", "true");
		_tableLineAlpha.put("line_data_nbrcnt", "true");
	}
	
}

class MVConvStatHeader{
	public int _intStatHeaderIdSrc = -1;
	public int _intStatHeaderIdDest	= -1;
	
	public String _strFcstLead = "";
	public String _strFcstValidBeg = null;
	public String _strFcstValidEnd = null;
	public String _strFcstInitBeg = null;
	public String _strObsLead = "";
	public String _strObsValidBeg = null;
	public String _strObsValidEnd = null;		
}

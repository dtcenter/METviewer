package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class MVLoad extends MVUtil {

	public static String _strMetVersion				= "V3.0";

	public static boolean _boolVerbose				= false;
	public static int _intInsertSize				= 1;
	public static boolean _boolModeHeaderDBCheck	= false;
	public static boolean _boolDropIndexes			= false;
	public static boolean _boolApplyIndexes			= false;
	public static boolean _boolIndexOnly			= false;

	public static boolean _boolLineTypeLoad			= false;
	public static Hashtable _tableLineTypeLoad		= new Hashtable();
	public static boolean _boolLoadStat				= true;
	public static boolean _boolLoadMode				= true;
	public static boolean _boolLoadMpr				= false;
	public static boolean _boolLoadOrank			= false;

	public static DecimalFormat _formatPerf			= new DecimalFormat("0.000");

	public static final Pattern _patModeSingle		= Pattern.compile("^(C?[FO]\\d{3})$");
	public static final Pattern _patModePair		= Pattern.compile("^(C?F\\d{3})_(C?O\\d{3})$");
	
	public static Hashtable _tableStatHeaders		= new Hashtable(1024);
	public static Hashtable _tableModeHeaders		= new Hashtable(1024);

	public static long _intStatHeaderSearchTime		= 0;
	public static long _intStatHeaderTableTime		= 0;
	public static long _intModeHeaderSearchTime		= 0;
	
	public static int _intNumStatFiles				= 0;
	public static int _intStatLinesTotal			= 0;
	public static int _intStatHeaderRecords			= 0;
	public static int _intStatHeaderInserts			= 0;
	public static int _intLineDataRecords			= 0;
	public static int _intLineDataInserts			= 0;
	public static int _intStatGroupRecords			= 0;
	public static int _intStatGroupInserts			= 0;
	public static int _intVarLengthRecords			= 0;
	public static int _intVarLengthInserts			= 0;
	public static int _intNumModeFiles				= 0;
	public static int _intModeLinesTotal			= 0;
	public static int _intModeHeaderRecords			= 0;
	public static int _intModeCtsRecords			= 0;
	public static int _intModeObjSingleRecords		= 0;
	public static int _intModeObjPairRecords		= 0;

	/*
	 * data_file_lu_id values for each MET output type
	 */
	public static final Hashtable _tableDataFileLU = new Hashtable();
	static {
		_tableDataFileLU.put("point_stat",		"0");
		_tableDataFileLU.put("grid_stat",		"1");
		_tableDataFileLU.put("mode_cts",		"2");
		_tableDataFileLU.put("mode_obj",		"3");
		_tableDataFileLU.put("wavelet_stat",	"4");
		_tableDataFileLU.put("ensemble_stat",	"5");
	}
	
	/*
	 * variable length table names for each variable length output line type
	 */
	public static final Hashtable _tableVarLengthTable = new Hashtable();
	static {			
		_tableVarLengthTable.put("PCT",   "line_data_pct_thresh");
		_tableVarLengthTable.put("PSTD",  "line_data_pstd_thresh");
		_tableVarLengthTable.put("PJC",   "line_data_pjc_thresh");
		_tableVarLengthTable.put("PRC",   "line_data_prc_thresh");
		_tableVarLengthTable.put("MCTC",  "line_data_mctc_cnt");
		_tableVarLengthTable.put("RHIST", "line_data_rhist_rank");
		_tableVarLengthTable.put("ORANK", "line_data_orank_ens");
	}
	
	/*
	 * variable length group data indices for lines with an arbitrary number of fields
	 *   - index of field containing number of sets
	 *   - index of first repeating field(s)
	 *   - number of fields in each repeating set
	 */
	public static final Hashtable _tableVarLengthGroupIndices = new Hashtable();
	static {			
		_tableVarLengthGroupIndices.put("PCT",  new int[]{22, 23, 3});
		_tableVarLengthGroupIndices.put("PSTD", new int[]{22, 33, 1});
		_tableVarLengthGroupIndices.put("PJC",	new int[]{22, 23, 7});
		_tableVarLengthGroupIndices.put("PRC",  new int[]{22, 23, 3});
		_tableVarLengthGroupIndices.put("MCTC", new int[]{22, 23, 1});
		_tableVarLengthGroupIndices.put("RHIST", new int[]{24, 25, 1});
		_tableVarLengthGroupIndices.put("ORANK", new int[]{32, 33, 1});
	}	
	
	public static Hashtable _tableVarLengthLineDataId = new Hashtable();	
		
	public static void main(String[] argv) {
		System.out.println("----  MVLoad  ----\n");
		Connection con = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
		
			String strXML = "";
			
			//  parse the input arguments
			if( 1 > argv.length || 2 < argv.length ){
				System.out.println(getUsage() + "\n\n----  MVLoad Done  ----");
				return;
			}
			if( 2 == argv.length ){
				if( argv[0].equalsIgnoreCase("-index") ){ _boolIndexOnly = true; }				
				else{
					System.out.println("ERROR: unrecognized argument \"" + argv[1] + "\"\n" + getUsage() + "\n\n----  MVLoad Done  ----");
					return;
				}
				strXML = argv[1];
			} else {
				strXML = argv[0];
			}
			
			//  parse the plot job
			System.out.println("Begin time: " + format.format(new java.util.Date()) + "\n" +
							   "Parsing: " + strXML + "\n" +
							   (_boolIndexOnly? "Applying Index Settings Only\n" : ""));
			MVLoadJobParser parser = new MVLoadJobParser(strXML);
			MVLoadJob job = parser.getLoadJob();

			//  process the elements of the job
			con = job.getConnection();
			System.out.println("Database Connection:\n" +
					   		   "      host: " + job.getDBHost() + "\n" +
					   		   "  database: " + job.getDBName() + "\n" +
					   		   "      user: " + job.getDBUser() + "\n" +
					   		   "  password: " + job.getDBPassword() + "\n");
			
			_strMetVersion				= job.getMetVersion();
			
			_boolVerbose				= job.getVerbose();
			_intInsertSize				= job.getInsertSize();
			_boolModeHeaderDBCheck		= job.getModeHeaderDBCheck();
			_boolDropIndexes			= job.getDropIndexes();
			_boolApplyIndexes			= job.getApplyIndexes();
			
			_boolLineTypeLoad			= job.getLineTypeLoad();
			_tableLineTypeLoad			= job.getLineTypeLoadMap();
			
			_boolLoadStat				= job.getLoadStat();
			_boolLoadMode				= job.getLoadMode();
			
			_boolLoadMpr				= job.getLoadMpr();
			_boolLoadOrank				= job.getLoadOrank();
			
			//  update the var length tree with information for METv2.0, if necessary
			if( "V2.0".equals(_strMetVersion) ){
				_tableVarLengthGroupIndices.put("PSTD", new int[]{22, 30, 1});
			}
			
			//  if the insert size is greater than 1, ensure that the db header check is off
			if( 1 < _intInsertSize ){
				throw new Exception("METViewer load error: insert size (" + _intInsertSize + ") > 1 and database header check turned on");
			}
			
			long intLoadTimeStart = (new java.util.Date()).getTime();
			
			//  drop the database indexes, if requested
			if( _boolDropIndexes ){
				dropIndexes(con);
			}
			
			//  if the job involves only applying indexes, do so and return
			if( _boolIndexOnly && _boolApplyIndexes ){
				applyIndexes(con);
				System.out.println("\n----  MVLoad Done  ----");
				return;
			}
			
			//  initialize the line_data_id table
			initVarLengthLineDataIds(con);
			
			//  if there are <load_file> files specified, load them
			String[] listLoadFiles = job.getLoadFiles();
			if( !_boolIndexOnly && 0 < listLoadFiles.length ){				
				for(int i=0; i < listLoadFiles.length; i++){
					try{
						processFile(new File(listLoadFiles[i]), con);
					}catch(Exception e){
						System.out.println("  **  ERROR: caught " + e.getClass() + " loading file " + listLoadFiles[i] + ": " + e.getMessage());
						e.printStackTrace();
						System.out.println("  **  WARNING: error(s) encountered loading file " + listLoadFiles[i] + " - skipping file");
					}
				}
			}
			
			//  if there is a file template specified, load it
			if( !_boolIndexOnly && null != job.getFolderTmpl() && !job.getFolderTmpl().equals("") ){
				int intStatLinesPrev = 0;
				int intModeLinesPrev = 0;
				
				//  build a folder with each permutation of load values and load the data therein
				MVOrderedMap[] listPerm = permute(job.getLoadVal()).getRows();
				for (int intPerm = 0; intPerm < listPerm.length; intPerm++) {
					
					//  determine the name of the current folder
					String strBaseFolder = buildTemplateString(job.getFolderTmpl(), listPerm[intPerm]);				
					System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " - " + strBaseFolder /* + "\n" + listPerm[intPerm].getRDecl() */);
					long intPermStart = (new java.util.Date()).getTime();

					//  try to access the folder and its contents, and continue if it does not exist
					File fileBaseFolder = new File(strBaseFolder);
					if (!fileBaseFolder.exists()) {
						//System.out.println("  **  WARNING: base folder not found: " + fileBaseFolder);
						continue;
					}
					
					//  process each fine in the folder
					File[] listDataFiles = fileBaseFolder.listFiles();
					for (int j = 0; j < listDataFiles.length; j++) {
						try{
							processFile(listDataFiles[j], con);
						}catch(Exception e){
							System.out.println("  **  ERROR: caught " + e.getClass() + " in processFile()\n" + 
											   "  **  WARNING: error(s) encountered loading file " + listDataFiles[j] + " - skipping file");
							e.printStackTrace();
						}
					}					
					_tableModeHeaders.clear();
									
					//  bookkeeping
					int intStatLinesPerm = _intStatLinesTotal - intStatLinesPrev;
					int intModeLinesPerm = _intModeLinesTotal - intModeLinesPrev;
					intStatLinesPrev = _intStatLinesTotal;
					intModeLinesPrev = _intModeLinesTotal;
					System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " complete - insert time: " + 
									   formatTimeSpan( (new java.util.Date()).getTime() - intPermStart ) + "  stat lines: " + intStatLinesPerm + 
									   "  mode lines: " + intModeLinesPerm + "\n");
				}
			}
						
			//  print a performance report
			long intLoadTime = (new java.util.Date()).getTime() - intLoadTimeStart;
			double dblLinesPerMSec =  (double)_intStatLinesTotal / (double)(intLoadTime);			
			if( !_boolIndexOnly ){
				System.out.println("\n    ==== grid_stat ====\n\n" +
								   padBegin("stat_header search time total: ", 36) + formatTimeSpan(_intStatHeaderSearchTime) + "\n" +
								   padBegin("stat_header table time total: ", 36) + formatTimeSpan(_intStatHeaderTableTime) + "\n" +
								   padBegin("stat header records: ", 36) + _intStatHeaderRecords + "\n" +
								   padBegin("stat header inserts: ", 36) + _intStatHeaderInserts + "\n" +
								   padBegin("line data records: ", 36) + _intLineDataRecords + "\n" +
								   padBegin("line data inserts: ", 36) + _intLineDataInserts + "\n" +
								   padBegin("stat group records: ", 36) + _intStatGroupRecords + "\n" +
								   padBegin("stat group inserts: ", 36) + _intStatGroupInserts + "\n" +
								   padBegin("var length records: ", 36) + _intVarLengthRecords + "\n" +
								   padBegin("var length inserts: ", 36) + _intVarLengthInserts + "\n" +
								   padBegin("total lines: ", 36) + _intStatLinesTotal + "\n" +
								   padBegin("insert size: ", 36) + _intInsertSize + "\n" +
								   padBegin("lines / msec: ", 36) + _formatPerf.format(dblLinesPerMSec) + "\n" +
								   padBegin("num files: ", 36) + _intNumStatFiles + "\n\n" +
								   "    ==== mode ====\n\n" +
								   (_boolModeHeaderDBCheck? padBegin("mode_header search time total: ", 36) + formatTimeSpan(_intModeHeaderSearchTime) + "\n" : "") +
								   padBegin("mode_header inserts: ", 36) + _intModeHeaderRecords + "\n" +
								   padBegin("mode_cts inserts: ", 36) + _intModeCtsRecords + "\n" +
								   padBegin("mode_obj_single inserts: ", 36) + _intModeObjSingleRecords + "\n" +
								   padBegin("mode_obj_pair inserts: ", 36) + _intModeObjPairRecords + "\n" +
								   padBegin("total lines: ", 36) + _intModeLinesTotal + "\n" +
								   padBegin("num files: ", 36) + _intNumModeFiles + "\n");
			}
			
			//  apply the indexes, if requested
			if( _boolApplyIndexes ){
				applyIndexes(con);
			}

			System.out.println("End time: " + format.format(new java.util.Date()) + "\n" +
					   		   "Load total: " + formatTimeSpan(intLoadTime) + "\n");
		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null ){ con.close(); } }catch(SQLException e){}
		}

		System.out.println("\n----  MVLoad Done  ----");
	}
	
	public static String getUsage(){
		return	"Usage:  mv_load\n" +
				"          [-index]\n" +
				"          load_spec_file\n" +
				"\n" +
				"          where   \"-index\" indicates that no data should be loaded, and only the indexing commands applied\n" +
				"                  \"load_spec_file\" specifies the XML load specification document\n";
	}
	
	/**
	 * Attempt to load the input file into the database data_file table, and then, if successful, into the appropriate
	 * set of tables: stat or mode.
	 * @param file File to process
	 * @param con Connection to the database to load
	 * @throws Exception
	 */
	public static void processFile(File file, Connection con) throws Exception{
		long intProcessDataFileBegin = (new java.util.Date()).getTime();
		DataFileInfo info = processDataFile(file, con);
		if( null == info ){ return; }
		long intProcessDataFileTime = (new java.util.Date()).getTime() - intProcessDataFileBegin;
		String strFileMsg = "  " + info._dataFilePath + "/" + info._dataFileFilename + 
							(_boolVerbose? "\n" + padBegin("data file time: ", 36) + formatTimeSpan(intProcessDataFileTime) : "");
		
		if( (info._dataFileLuTypeName.equals("point_stat")   || info._dataFileLuTypeName.equals("grid_stat") ||
			 info._dataFileLuTypeName.equals("wavelet_stat") || info._dataFileLuTypeName.equals("ensemble_stat")) &&
			_boolLoadStat ){
			System.out.println(strFileMsg);
			loadStatFile(info, con);
			_intNumStatFiles++;
		} else if( (info._dataFileLuTypeName.equals("mode_obj") || info._dataFileLuTypeName.equals("mode_cts")) &&
				   _boolLoadMode ){
			System.out.println(strFileMsg);
			loadModeFile(info, con);
			_intNumModeFiles++;
		}
	}
	
	/**
	 * Load the MET output data from the data file underlying the input DataFileInfo object into the database underlying the
	 * input Connection.  The header information can be checked in two different ways: using a table for the current file
	 * (specified by _boolStatHeaderTableCheck) or by searching the stat_header table for a duplicate (specified by
	 * _boolStatHeaderDBCheck).  Records in line_data tables, stat_group tables and line_data_thresh tables are created 
	 * from the data in the input file.  If necessary, records in the stat_header table are created as well.
	 * @param info Contains MET output data file information
	 * @param con Connection to the target database
	 * @throws Exception
	 */
	public static void loadStatFile(DataFileInfo info, Connection con) throws Exception{

		//  initialize the insert data structure
		MVLoadStatInsertData d = new MVLoadStatInsertData();
		d._con = con;

		//  performance counters
		long intStatHeaderLoadStart = (new java.util.Date()).getTime();
		long intStatHeaderSearchTime = 0;
		int intStatHeaderRecords = 0;
		int intStatHeaderInserts = 0;
		int intLineDataRecords = 0;
		int intLineDataInserts = 0;
		int intLineDataSkipped = 0;
		int intVarLengthRecords = 0;
		int intVarLengthInserts = 0;

		//  get the next stat_header_id
		int intStatHeaderIdNext = getNextId(con, "stat_header", "stat_header_id");
		
		//  set up the input file for reading
		String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
		BufferedReader reader = new BufferedReader( new FileReader(strFilename) );
		int intLine = 0;
		
		//  read in each line of the input file
		while( reader.ready() ){
			String[] listToken = reader.readLine().split("\\s+");
			intLine++;
			
			//  the first line is the header line
			if( 1 > listToken.length || listToken[0].equals("VERSION") ){
				continue;
			}
			
			//  error if the version number does not match the configured value
			String strMetVersion = listToken[0];
			if( !strMetVersion.equals(_strMetVersion) ){
				throw new Exception("ERROR: file MET version " + strMetVersion + " does not match configured value " + _strMetVersion);
			}
			
			//  if the line type load selector is activated, check that the current line type is on the list
			d._strLineType = listToken[20];
			if( _boolLineTypeLoad ){
				if( !_tableLineTypeLoad.containsKey(d._strLineType) ){ continue; }
			}
			
			d._strFileLine = strFilename + ":" + intLine;
			
			//  parse the valid times
			java.util.Date dateFcstValidBeg = _formatStat.parse(listToken[3]);
			java.util.Date dateFcstValidEnd = _formatStat.parse(listToken[4]);
			java.util.Date dateObsValidBeg = _formatStat.parse(listToken[6]);
			java.util.Date dateObsValidEnd = _formatStat.parse(listToken[7]);

			//  format the valid times for the database insert  
			String strFcstValidBeg = _formatDB.format(dateFcstValidBeg);
			String strFcstValidEnd = _formatDB.format(dateFcstValidEnd);
			String strObsValidBeg = _formatDB.format(dateObsValidBeg);
			String strObsValidEnd = _formatDB.format(dateObsValidEnd);
			
			//  calculate the number of seconds corresponding to fcst_lead
			String strFcstLead = listToken[2];
			int intFcstLeadLen = strFcstLead.length();
			int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(intFcstLeadLen-2, intFcstLeadLen));
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen-4, intFcstLeadLen-2)) * 60;
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen-4)) * 3600;
			
			//  determine the init time by combining fcst_valid_beg and fcst_lead
			Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calFcstInitBeg.setTime(dateFcstValidBeg);
			calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
			java.util.Date dateFcstInitBeg = calFcstInitBeg.getTime();
			String strFcstInitBeg = _formatDB.format(dateFcstInitBeg);
			
			//  ensure that the interp_pnts field value is a reasonable integer 
			String strInterpPnts = listToken[15];
			if( strInterpPnts.equals("NA") ){ strInterpPnts = "0"; }
		
			String strLineType = d._strLineType;
			
			//  do not load matched pair lines or orank lines
			if( (!_boolLoadMpr && strLineType.equals("MPR")) || (!_boolLoadOrank && strLineType.equals("ORANK")) ){ continue; }
									
			
			/*
			 * * * *  stat_header insert  * * * *
			 */
			intStatHeaderRecords++;
			
			//  build the stat_header value list for this line			
			String[] listStatHeaderValue = {
					listToken[0],		//  version
					listToken[1],		//  model
					listToken[8],		//  fcst_var
					listToken[9],		//  fcst_lev
					listToken[10],		//  obs_var
					listToken[11],		//  obs_lev
					listToken[12],		//  obtype
					listToken[13],		//  vx_mask
					listToken[14],		//  interp_mthd
					strInterpPnts,		//  interp_pnts
					listToken[16],		//  fcst_thresh
					listToken[17]		//  obs_thresh
			};
			
			//  build a where clause for searching for duplicate stat_header records
			String strStatHeaderWhereClause =
					"  version = '" +				listToken[0] + "'\n" +
					"  AND model = '" +				listToken[1] + "'\n" +
					"  AND fcst_var = '" +			listToken[8] + "'\n" +
					"  AND fcst_lev = '" +			listToken[9] + "'\n" +
					"  AND obs_var = '" +			listToken[10] + "'\n" +
					"  AND obs_lev = '" +			listToken[11] + "'\n" +
					"  AND obtype = '" +			listToken[12] + "'\n" +
					"  AND vx_mask = '" +			listToken[13] + "'\n" +
					"  AND interp_mthd = '" +		listToken[14] + "'\n" +
					"  AND interp_pnts = " +		strInterpPnts + "\n" +
					"  AND fcst_thresh = '" +		listToken[16] + "'\n" +
					"  AND obs_thresh = '" +		listToken[17] + "'";
			
			//  build the value list for the stat_header insert
			String strStatHeaderValueList = "";
			for(int i=0; i < listStatHeaderValue.length; i++){
				strStatHeaderValueList += (0 < i? ", " : "") + "'" + listStatHeaderValue[i] + "'";
			}
			
			//  look for the header key in the table, and record the time taken
			boolean boolStatHeaderPresent = false; 
			long intStatHeaderTableBegin = (new java.util.Date()).getTime();
			boolStatHeaderPresent = _tableStatHeaders.containsKey(strStatHeaderValueList);
			
			//  check the table to see if a stat_header already exists
			int intStatHeaderId = -1;
			if( boolStatHeaderPresent ){
				intStatHeaderId = ((Integer)_tableStatHeaders.get(strStatHeaderValueList)).intValue();
				_intStatHeaderTableTime += (new java.util.Date()).getTime() - intStatHeaderTableBegin;
				
			//  if the stat_header does not yet exist, create one
			} else {
				_intStatHeaderTableTime += (new java.util.Date()).getTime() - intStatHeaderTableBegin;				
				
				//  look for an existing stat_header record with the same information
				long intStatHeaderSearchBegin = (new java.util.Date()).getTime();
				String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
				Statement stmt = con.createStatement();
				ResultSet res = stmt.executeQuery(strStatHeaderSelect);
				if( res.next() ){
					intStatHeaderId = res.getInt(1);
				}
				stmt.close();
				intStatHeaderSearchTime = (new java.util.Date()).getTime() - intStatHeaderSearchBegin;
				_intStatHeaderSearchTime += intStatHeaderSearchTime;
				
				//  if not present in the table or database, add a stat_header record with a new stat_header_id
				if( -1 == intStatHeaderId ){
					intStatHeaderId = intStatHeaderIdNext++;
					d._listInsertValues.add("(" + intStatHeaderId + ", " + strStatHeaderValueList + ")");
				}
				_tableStatHeaders.put(strStatHeaderValueList, new Integer(intStatHeaderId));
			}	

			
			/*
			 * * * *  line_data insert  * * * *
			 */			

			//  
			int intLineDataMax = listToken.length;
			String strLineDataId = "";
			intLineDataRecords++;
			
			//  if the line type is of variable length, get the line_data_id
			boolean boolHasVarLengthGroups = _tableVarLengthGroupIndices.containsKey(d._strLineType);
			
			//  determine the maximum token index for the data
			if( boolHasVarLengthGroups ){
				int intLineDataId = ((Integer)_tableVarLengthLineDataId.get(strLineType)).intValue();
				strLineDataId = "" + intLineDataId + ", ";
				_tableVarLengthLineDataId.put(strLineType, new Integer(intLineDataId + 1));
				int[] listVarLengthGroupIndices = (int[])_tableVarLengthGroupIndices.get(d._strLineType);
				intLineDataMax = listVarLengthGroupIndices[1];
			}
			
			//  build the value list for the insert statment
			String strLineDataValueList = "" +
				    strLineDataId +						//  line_data_id (if present) 
					intStatHeaderId + ", " +			//  stat_header_id
					info._dataFileId + ", " +			//  data_file_id
					intLine + ", " +					//  line_num
					strFcstLead + ", " +				//  fcst_lead
					"'" + strFcstValidBeg + "', " +		//  fcst_valid_beg
					"'" + strFcstValidEnd + "', " +		//  fcst_valid_end
					"'" + strFcstInitBeg + "', " +		//  fcst_init_beg
					listToken[5] + ", " +				//  obs_lead
					"'" + strObsValidBeg + "', " +		//  obs_valid_beg
					"'" + strObsValidEnd + "'";			//  obs_valid_end
	
			//  if the line data requires a cov_thresh value, add it
			String strCovThresh = listToken[18];
			if( _tableCovThreshLineTypes.containsKey(d._strLineType) ){
				if( strCovThresh.equals("NA") ){ System.out.println("  **  WARNING: cov_thresh value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine); }
				strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
			} else if( !strCovThresh.equals("NA") ){
				System.out.println("  **  WARNING: unexpected cov_thresh value '" + strCovThresh + "' with line type '" + d._strLineType + "'\n        " + d._strFileLine);
			}
		
			//  if the line data requires an alpha value, add it
			String strAlpha = listToken[19];
			if( _tableAlphaLineTypes.containsKey(d._strLineType) ){
				if( strAlpha.equals("NA") ){ System.out.println("  **  WARNING: alpha value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine); }
				strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
			} else if( !strAlpha.equals("NA") ){
				System.out.println("  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + d._strLineType + "'\n        " + d._strFileLine);
			}
			
			//  add total and all of the stats on the rest of the line to the value list
			for(int i=21; i < intLineDataMax; i++){
				
				//  for the METv2.0 PSTD line type, add the baser and CIs
				if( 23 == i && "PSTD".equals(d._strLineType) && "V2.0".equals(_strMetVersion) ){
					strLineDataValueList += ", '-9999', '-9999', '-9999'";
				}
				
				//  for the METv2.0 MPR line type, add the obs_sid
				if( 23 == i && "MPR".equals(d._strLineType) && "V2.0".equals(_strMetVersion) ){
					strLineDataValueList += ", 'NA'";
				}
				
				//  add the stats in order
				strLineDataValueList += ", '" + replaceInvalidValues(listToken[i]) + "'";
			}
			
			//  add the values list to the line type values map
			ArrayList listLineTypeValues = new ArrayList();
			if( d._tableLineDataValues.containsKey(d._strLineType) ){ listLineTypeValues = (ArrayList)d._tableLineDataValues.get(d._strLineType); }
			listLineTypeValues.add("(" + strLineDataValueList + ")");
			d._tableLineDataValues.put(d._strLineType, listLineTypeValues);
			intLineDataInserts++;
			
			
			/*
			 * * * *  var_length insert  * * * *
			 */
			
			if( boolHasVarLengthGroups ){
				
				//  get the index information about the current line type
				int[] listVarLengthGroupIndices = (int[])_tableVarLengthGroupIndices.get(d._strLineType);
				int intGroupCntIndex = listVarLengthGroupIndices[0];
				int intGroupIndex = listVarLengthGroupIndices[1];
				int intGroupSize = listVarLengthGroupIndices[2];
				int intNumGroups = Integer.parseInt(listToken[intGroupCntIndex]);
				if( d._strLineType.equals("PCT") || d._strLineType.equals("PJC") || d._strLineType.equals("PRC") ){
					intNumGroups -= 1;
				}
				ArrayList listThreshValues = (ArrayList)d._tableVarLengthValues.get(d._strLineType);
				if( null == listThreshValues ){ listThreshValues = new ArrayList(); }
			
				//  build a insert value statement for each threshold group
				if( d._strLineType.equals("MCTC") ){
					for(int i=0; i < intNumGroups; i++){
						for(int j=0; j < intNumGroups; j++){
							listThreshValues.add("(" + strLineDataId + (i+1) + ", " + (j+1) + ", " + 
												 replaceInvalidValues( listToken[intGroupIndex++] ) + ")");
							intVarLengthRecords++;
						}
					}
				} else {
					for(int i=0; i < intNumGroups; i++){
						String strThreshValues = "(" + strLineDataId + (i+1);
						for(int j=0; j < intGroupSize; j++){
							strThreshValues += ", " + replaceInvalidValues( listToken[intGroupIndex++] );
						}
						strThreshValues += ")";
						listThreshValues.add(strThreshValues);
						intVarLengthRecords++;
					}
				}
				d._tableVarLengthValues.put(d._strLineType, listThreshValues);				
			}
			
			//  if the insert threshhold has been reached, commit the stored data to the database 
			if( _intInsertSize <= d._listInsertValues.size() ){				
				int[] listInserts		= commitStatData(d);
				intStatHeaderInserts	+= listInserts[INDEX_STAT_HEADERS];
				intLineDataInserts		+= listInserts[INDEX_LINE_DATA];
				intVarLengthInserts		+= listInserts[INDEX_VAR_LENGTH];				
			}
			
		}  // end: while( reader.ready() )
		
		//  commit all the remaining stored data
		int[] listInserts		= commitStatData(d);
		intStatHeaderInserts	+= listInserts[INDEX_STAT_HEADERS];
		intLineDataInserts		+= listInserts[INDEX_LINE_DATA];
		intVarLengthInserts		+= listInserts[INDEX_VAR_LENGTH];

		reader.close();
		//_tableStatHeaders.clear();
		//_tableStatHeaders = new Hashtable();
		
		_intStatLinesTotal += (intLine - 1);
		_intStatHeaderRecords += intStatHeaderRecords;
		_intStatHeaderInserts += intStatHeaderInserts;
		_intLineDataInserts += intLineDataInserts;
		_intLineDataRecords += intLineDataRecords;
		_intVarLengthRecords += intVarLengthRecords;
		_intVarLengthInserts += intVarLengthInserts;
		
		//  print a performance report
		long intStatHeaderLoadTime = (new java.util.Date()).getTime() - intStatHeaderLoadStart;
		double dblLinesPerMSec = (double)(intLine - 1) / (double)(intStatHeaderLoadTime);
		
		if( _boolVerbose ){
			System.out.println(padBegin("fine lines: ", 36) + (intLine - 1) + "\n" +
							   padBegin("stat_header records: ", 36) + intStatHeaderRecords + "\n" +
							   padBegin("stat_header inserts: ", 36) + intStatHeaderInserts + "\n" +
							   padBegin("line_data records: ", 36) + intLineDataRecords + "\n" +
							   padBegin("line_data inserts: ", 36) + intLineDataInserts + "\n" +
							   padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
							   padBegin("var length records: ", 36) + intVarLengthRecords + "\n" +
							   padBegin("var length inserts: ", 36) + intVarLengthInserts + "\n" +
							   padBegin("total load time: ", 36) + formatTimeSpan(intStatHeaderLoadTime) + "\n" +
							   padBegin("stat_header search time: ", 36) + formatTimeSpan(intStatHeaderSearchTime) + "\n" +
							   padBegin("lines / msec: ", 36) + _formatPerf.format(dblLinesPerMSec) + "\n\n");
		}

	}
	
	/*
	 * MVLoadStatInsertData is used to store insert value lists for the various types of grid_stat and point_stat
	 * tables.  The structure is built in loadStatFile() and is unloaded and executed in commitStatData().
	 */
	static class MVLoadStatInsertData{
		public Connection	_con						= null;
		public ArrayList	_listInsertValues			= new ArrayList(); 
		public Hashtable	_tableLineDataValues		= new Hashtable();
		public ArrayList	_listStatGroupInsertValues	= new ArrayList();
		public Hashtable	_tableVarLengthValues		= new Hashtable();
		public String		_strLineType				= "";
		public String		_strFileLine				= "";
	}
	
	
	public static final int INDEX_STAT_HEADERS	= 0;
	public static final int INDEX_LINE_DATA		= 1;
	public static final int INDEX_STAT_GROUP	= 2;
	public static final int INDEX_VAR_LENGTH	= 3;
	
	/**
	 * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method was designed to be called from
	 * loadStatFile(), which is responsible for building insert value lists for the various types of grid_stat and point_stat 
	 * database tables.
	 * @param d Data structure loaded with insert value lists
	 * @return An array of four integers, indexed by the INDEX_* members, representing the number of database inserts of each type
	 * @throws Exception
	 */
	public static int[] commitStatData(MVLoadStatInsertData d)
	throws Exception {

		int[] listInserts = new int[]{0, 0, 0, 0};		
		String strValueList = "";
		
		/*
		 * * * *  stat_header commit  * * * * 
		 */			

		//  build and execute the stat header insert statement, if the list length has reached the insert size or the end of the file
		if( 0 < d._listInsertValues.size() ){			
			for(int i=0; i < d._listInsertValues.size(); i++){
				strValueList += (0 < i? ", " : "") + d._listInsertValues.get(i).toString(); 
			}
			String strStatHeaderInsert = "INSERT INTO stat_header VALUES " + strValueList + ";";
			try{
				int intResStatHeaderInsert = executeUpdate(d._con, strStatHeaderInsert);
				if( d._listInsertValues.size() != intResStatHeaderInsert ){
					System.out.println("  **  WARNING: unexpected result from stat_header INSERT: " + intResStatHeaderInsert + "\n        " + d._strFileLine);
				}
			}catch(Exception e){
				throw e;
			}
			d._listInsertValues.clear();
			listInserts[INDEX_STAT_HEADERS]++; //  intStatHeaderInserts++;
		}

		
		/*
		 * * * *  line_data commit  * * * * 
		 */
		
		//  for each line type, build an insert statement with the appropriate list of values
		for(Iterator iterEntries = d._tableLineDataValues.entrySet().iterator(); iterEntries.hasNext();){
			Map.Entry entry = (Map.Entry)iterEntries.next();
			d._strLineType = entry.getKey().toString();
			ArrayList listValues = (ArrayList)entry.getValue();
			
			//  build the list of value lists for this line type
			strValueList = "";
			for(int i=0; i < listValues.size(); i++){
				strValueList += (0 < i? ", " : "") + listValues.get(i).toString();
			}
		
			//  build and execute the line data insert statement
			String strLineDataTable = "line_data_" + d._strLineType.toLowerCase();
			String strLineDataInsert = "INSERT INTO " + strLineDataTable + " VALUES " + strValueList + ";";
			int intResLineDataInsert = executeUpdate(d._con, strLineDataInsert);
			if( listValues.size() != intResLineDataInsert ){ System.out.println("  **  WARNING: unexpected result from line_data INSERT: " + 
																				intResLineDataInsert + "\n        " + d._strFileLine); }
			listInserts[INDEX_LINE_DATA]++; //  intLineDataInserts++;
		}
		d._tableLineDataValues.clear();
	
		
		/*
		 * * * *  stat_group commit  * * * * 
		 */

		//  build a stat_group insert with all stored values
		if( 0 < d._listStatGroupInsertValues.size() ){
			String strStatGroupInsertValues = "";
			for(int i=0; i < d._listStatGroupInsertValues.size(); i++){
				strStatGroupInsertValues += (i == 0? "" : ", ") + d._listStatGroupInsertValues.get(i).toString();		
			}
			String strStatGroupInsert = "INSERT INTO stat_group VALUES " + strStatGroupInsertValues + ";";
			int intStatGroupInsert = executeUpdate(d._con, strStatGroupInsert);
			if( d._listStatGroupInsertValues.size() != intStatGroupInsert ){
				System.out.println("  **  WARNING: unexpected result from stat_group INSERT: " + intStatGroupInsert + " vs. " + 
								   d._listStatGroupInsertValues.size() + "\n        " + d._strFileLine);
			}
			listInserts[INDEX_STAT_GROUP]++; //  intStatGroupInserts++;
		}
		d._listStatGroupInsertValues.clear();
		
		/*
		 * * * *  variable length data commit  * * * * 
		 */
		
		//  insert probabilistic data into the thresh tables
		String[] listVarLengthTypes = (String[])d._tableVarLengthValues.keySet().toArray(new String[]{});
		for(int i=0; i < listVarLengthTypes.length; i++){
			String[] listVarLengthValues = toArray( (ArrayList)d._tableVarLengthValues.get(listVarLengthTypes[i]) );
			if( 1 > listVarLengthValues.length ){ continue; }
			String strVarLengthTable = _tableVarLengthTable.get(listVarLengthTypes[i]).toString();
			String strThreshInsert = "INSERT INTO " + strVarLengthTable + " VALUES ";
			for(int j=0; j < listVarLengthValues.length; j++){
				strThreshInsert += (0 < j? ", " : "") + listVarLengthValues[j];
				listInserts[INDEX_VAR_LENGTH]++; //  intVarLengthInserts++;
			}
			int intThreshInsert = executeUpdate(d._con, strThreshInsert);
			if( listVarLengthValues.length != intThreshInsert ){
				System.out.println("  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " + 
								   listVarLengthValues.length + "\n        " + d._strFileLine);
			}
			// listInserts[INDEX_VAR_LENGTH]++; //  intVarLengthInserts++;
			d._tableVarLengthValues.put(listVarLengthTypes[i], new ArrayList());
		}
		
		return listInserts;
	}
	
	//  line_type_lu_id values for the various mode line types
	public static final int MODE_CTS		= 19;
	public static final int MODE_SINGLE		= 17;
	public static final int MODE_PAIR		= 18;
	
	/**
	 * Load the MET output data from the data file underlying the input DataFileInfo object into the database underlying the
	 * input Connection.  The header information can be checked in two different ways: using a table for the current file
	 * (specified by _boolModeHeaderTableCheck).  Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables
	 * are created from the data in the input file.  If necessary, records in the mode_header table are created.
	 * @param info Contains MET output data file information
	 * @param con Connection to the target database
	 * @throws Exception
	 */
	public static void loadModeFile(DataFileInfo info, Connection con) throws Exception{

		//  data structure for storing mode object ids
		Hashtable tableModeObjectId = new Hashtable();
		
		//  performance counters
		long intModeHeaderLoadStart = (new java.util.Date()).getTime();
		long intModeHeaderSearchTime = 0;
		int intModeHeaderInserts = 0;
		int intModeCtsInserts = 0;
		int intModeObjSingleInserts = 0;
		int intModeObjPairInserts = 0;

		//  get the next mode record ids from the database
		int intModeHeaderIdNext = getNextId(con, "mode_header", "mode_header_id");
		int intModeObjIdNext = getNextId(con, "mode_obj_single", "mode_obj_id");

		//  set up the input file for reading
		String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
		BufferedReader reader = new BufferedReader( new FileReader(strFilename) );
		int intLine = 1;
		
		//  read each line of the input file
		while( reader.ready() ){
			String[] listToken = reader.readLine().split("\\s+");
			
			//  the first line is the header line
			if( 1 > listToken.length || listToken[0].equals("VERSION") ){
				intLine++;
				continue;
			}
			
			String strFileLine = strFilename + ":" + intLine;
			
			//  determine the line type
			int intLineTypeLuId = -1;
			int intDataFileLuId = Integer.parseInt(info._dataFileLuId);
			String strObjectId = listToken[16];
			Matcher matModeSingle = _patModeSingle.matcher(strObjectId);
			Matcher matModePair = _patModePair.matcher(strObjectId);
			if( 2 == intDataFileLuId )         { intLineTypeLuId = MODE_CTS;    }
			else if( matModeSingle.matches() ) { intLineTypeLuId = MODE_SINGLE; }
			else if( matModePair.matches() )   { intLineTypeLuId = MODE_PAIR;   }
			else { throw new Exception("METViewer load error: loadModeFile() unable to determine line type " + listToken[16] + "\n        " + strFileLine); }
			
			
			/*
			 * * * *  mode_header insert  * * * *
			 */
			
			//  parse the valid times
			java.util.Date dateFcstValidBeg = _formatStat.parse(listToken[3]);
			java.util.Date dateObsValidBeg = _formatStat.parse(listToken[6]);

			//  format the valid times for the database insert  
			String strFcstValidBeg = _formatDB.format(dateFcstValidBeg);
			String strObsValidBeg = _formatDB.format(dateObsValidBeg);
			
			//  calculate the number of seconds corresponding to fcst_lead
			String strFcstLead = listToken[2];
			int intFcstLeadLen = strFcstLead.length();
			int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(intFcstLeadLen-2, intFcstLeadLen));
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen-4, intFcstLeadLen-2)) * 60;
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen-4)) * 3600;
			
			//  determine the init time by combining fcst_valid_beg and fcst_lead
			Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calFcstInitBeg.setTime(dateFcstValidBeg);
			calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
			java.util.Date dateFcstInitBeg = calFcstInitBeg.getTime();
			String strFcstInit = _formatDB.format(dateFcstInitBeg);
			
			//  build a value list from the header information
			String strModeHeaderValueList = "" +
					"'" + listToken[0] + "', " +			//  version
					"'" + listToken[1] + "', " +			//  model
					"'" + listToken[2] + "', " +			//  fcst_lead
					"'" + strFcstValidBeg + "', " +			//  fcst_valid
					"'" + listToken[4] + "', " +			//  fcst_accum
					"'" + strFcstInit + "', " +				//  fcst_init
					"'" + listToken[5] + "', " +			//  obs_lead
					"'" + strObsValidBeg + "', " +			//  obs_valid
					"'" + listToken[7] + "', " +			//  obs_accum
					"'" + listToken[8] + "', " +			//  fcst_rad
					"'" + listToken[9] + "', " +			//  fcst_thr
					"'" + listToken[10] + "', " +			//  obs_rad
					"'" + listToken[11] + "', " +			//  obs_thr
					"'" + listToken[12] + "', " +			//  fcst_var
					"'" + listToken[13] + "', " +			//  fcst_lev
					"'" + listToken[14] + "', " +			//  obs_var
					"'" + listToken[15] + "'";				//  obs_lev
			
			String strModeHeaderWhereClause = "" +
					"  version = '" + listToken[0] + "'\n" +
					"  AND model = '" + listToken[1] + "'\n" +
					"  AND fcst_lead = '" + listToken[2] + "'\n" +
					"  AND fcst_valid = '" + strFcstValidBeg + "'\n" +
					"  AND fcst_accum = '" + listToken[4] + "'\n" +
					"  AND fcst_init = '" + strFcstInit + "'\n" +
					"  AND obs_lead = '" + listToken[5] + "'\n" +
					"  AND obs_valid = '" + strObsValidBeg + "'\n" +
					"  AND obs_accum = '" + listToken[7] + "'\n" +
					"  AND fcst_rad = '" + listToken[8] + "'\n" +
					"  AND fcst_thr = '" + listToken[9] + "'\n" +
					"  AND obs_rad = '" + listToken[10] + "'\n" +
					"  AND obs_thr = '" + listToken[11] + "'\n" +
					"  AND fcst_var = '" + listToken[12] + "'\n" +
					"  AND fcst_lev = '" + listToken[13] + "'\n" +
					"  AND obs_var = '" + listToken[14] + "'\n" +
					"  AND obs_lev = '" + listToken[15] + "';";

			//  look for the header key in the table
			int intModeHeaderId = -1;
			if( _tableModeHeaders.containsKey(strModeHeaderValueList) ){
				intModeHeaderId = ((Integer)_tableModeHeaders.get(strModeHeaderValueList)).intValue(); 
			}
			
			//  if the mode_header does not yet exist, create one
			else {
				
				//  look for an existing mode_header record with the same information
				boolean boolFoundModeHeader = false;
				long intModeHeaderSearchBegin = (new java.util.Date()).getTime();
				if( _boolModeHeaderDBCheck ){
					String strModeHeaderSelect = "SELECT\n  mode_header_id\nFROM\n  mode_header\nWHERE\n" + strModeHeaderWhereClause;
					Statement stmt = con.createStatement();
					ResultSet res = stmt.executeQuery(strModeHeaderSelect);
					if( res.next() ){
						String strModeHeaderIdDup = res.getString(1);
						intModeHeaderId = Integer.parseInt(strModeHeaderIdDup);
						boolFoundModeHeader = true;
						System.out.println("  **  WARNING: found duplicate mode_header record with id " + strModeHeaderIdDup + "\n        " + strFileLine);
					}
					stmt.close();
				}
				intModeHeaderSearchTime = (new java.util.Date()).getTime() - intModeHeaderSearchBegin;
				_intModeHeaderSearchTime += intModeHeaderSearchTime;

				//  if the mode_header was not found, add it to the table
				if( !boolFoundModeHeader ){
					
					intModeHeaderId = intModeHeaderIdNext++;
					_tableModeHeaders.put(strModeHeaderValueList, new Integer(intModeHeaderId));
					
					//  build an insert statement for the mode header
					strModeHeaderValueList = "" +
							intModeHeaderId + ", " +				//  mode_header_id
							intLineTypeLuId + ", " +				//  line_type_lu_id
							info._dataFileId + ", " + 				//  data_file_id
							intLine + ", " + 						//  linenumber
							strModeHeaderValueList;
					
					//  insert the record into the mode_header database table
					String strModeHeaderInsert = "INSERT INTO mode_header VALUES (" + strModeHeaderValueList + ");";
					int intModeHeaderInsert = executeUpdate(con, strModeHeaderInsert);
					if( 1 != intModeHeaderInsert ){
						System.out.println("  **  WARNING: unexpected result from mode_header INSERT: " + intModeHeaderInsert + "\n        " + strFileLine);
					}
					intModeHeaderInserts++;
				}
			}
			
			
			/*
			 * * * *  mode_cts insert  * * * *
			 */
			
			if( MODE_CTS == intLineTypeLuId ){
				
				//  build the value list for the mode_cts insert
				String strCTSValueList = "" + intModeHeaderId + ", '" + listToken[16] + "'";
				for(int i=0; i < 18; i++){ strCTSValueList += ", " + replaceInvalidValues(listToken[17 + i]); }
				
				//  insert the record into the mode_cts database table
				String strModeCtsInsert = "INSERT INTO mode_cts VALUES (" + strCTSValueList + ");";
				int intModeCtsInsert = executeUpdate(con, strModeCtsInsert);
				if( 1 != intModeCtsInsert ){
					System.out.println("  **  WARNING: unexpected result from mode_cts INSERT: " + intModeCtsInsert + "\n        " + strFileLine);
				}
				intModeCtsInserts++;
				
			}
			
			/*
			 * * * *  mode_obj_single insert  * * * *
			 */
			
			else if( MODE_SINGLE == intLineTypeLuId ){

				//  build the value list for the mode_cts insert
				int intModeObjId =  intModeObjIdNext++;
				String strSingleValueList = "" + intModeObjId + ", " + intModeHeaderId + ", '" + strObjectId + "', '" + listToken[17] + "'";
				for(int i=0; i < 21; i++){ strSingleValueList += ", " + replaceInvalidValues(listToken[18 + i]); }
				
				//  insert the record into the mode_obj_single database table
				String strModeObjSingleInsert = "INSERT INTO mode_obj_single VALUES (" + strSingleValueList + ");";
				int intModeObjSingleInsert = executeUpdate(con, strModeObjSingleInsert);
				if( 1 != intModeObjSingleInsert ){
					System.out.println("  **  WARNING: unexpected result from mode_obj_single INSERT: " + intModeObjSingleInsert + "\n        " + strFileLine);
				}
				intModeObjSingleInserts++;
				
				//  add the mode_obj_id to the table, using the object_id as the key
				tableModeObjectId.put(strObjectId, new Integer(intModeObjId));
				
			}

			/*
			 * * * *  mode_obj_pair insert  * * * *
			 */
			
			else if( MODE_PAIR == intLineTypeLuId ){
				
				//  determine the mode_obj_id values for the pair
				int intModeObjectIdFcst = ((Integer)tableModeObjectId.get(matModePair.group(1))).intValue();
				int intModeObjectIdObs = ((Integer)tableModeObjectId.get(matModePair.group(2))).intValue();

				//  build the value list for the mode_cts insert
				String strPairValueList = "" + intModeObjectIdObs + ", " + intModeObjectIdFcst + ", " + intModeHeaderId + ", " + 
										  "'" + listToken[16] + "', '" + listToken[17] + "'";
				for(int i=0; i < 12; i++){ strPairValueList += ", " + replaceInvalidValues(listToken[39 + i]); }
				
				//  insert the record into the mode_obj_pair database table
				String strModeObjPairInsert = "INSERT INTO mode_obj_pair VALUES (" + strPairValueList + ");";
				int intModeObjPairInsert = executeUpdate(con, strModeObjPairInsert);
				if( 1 != intModeObjPairInsert ){
					System.out.println("  **  WARNING: unexpected result from mode_obj_pair INSERT: " + intModeObjPairInsert + "\n        " + strFileLine);
				}
				intModeObjPairInserts++;

			}
			
			intLine++;
		}
		reader.close();
		
		//  increment the global mode counters
		_intModeLinesTotal += (intLine - 1);
		_intModeHeaderRecords += intModeHeaderInserts;
		_intModeCtsRecords += intModeCtsInserts;
		_intModeObjSingleRecords += intModeObjSingleInserts;
		_intModeObjPairRecords += intModeObjPairInserts;
		
		//  print a performance report
		if( _boolVerbose ){
			long intModeHeaderLoadTime = (new java.util.Date()).getTime() - intModeHeaderLoadStart;		
			System.out.println(padBegin("mode_header inserts: ", 36) + intModeHeaderInserts + "\n" +
							   padBegin("mode_cts inserts: ", 36) + intModeCtsInserts + "\n" +
							   padBegin("mode_obj_single inserts: ", 36) + intModeObjSingleInserts + "\n" +
							   padBegin("mode_obj_pair inserts: ", 36) + intModeObjPairInserts + "\n" +
							   (_boolModeHeaderDBCheck? padBegin("mode_header search time: ", 36) + formatTimeSpan(intModeHeaderSearchTime) + "\n": "") +
							   padBegin("total load time: ", 36) + formatTimeSpan(intModeHeaderLoadTime) + "\n\n");
		}
	}
	
	/**
	 * Executes the input update statement against the database underlying the input Connection and cleans
	 * up any resources upon completion.
	 * @param con
	 * @param update SQL UPDATE statement to execute
	 * @return Number of records affected (output of Statement.executeUpdate() call)
	 * @throws SQLException
	 */
	public static int executeUpdate(Connection con, String update) throws Exception{
		int intRes = -1;
		Statement stmt = con.createStatement();
		try{
			intRes = stmt.executeUpdate(update);
		} catch(SQLException se){
			//throw new Exception("caught SQLException calling executeUpdate: " + se.getMessage() + "\n  sql: " + update);
			throw new Exception("caught SQLException calling executeUpdate: " + se.getMessage());
		}
		stmt.close();
		return intRes;
	}
	
    public static String replaceInvalidValues(String strData){
    	return strData.replace("NA", "-9999").replace("nan", "-9999");
    }
	
    /**
     * Build and execute a query that retrieves the next table record id, whose name is specified by the input field,
     * from the specified input table.  The statement is run against the input Connection and the next available id
     * is returned.
     * @param con
     * @param table Database table whose next available id is returned
     * @param field Field name of the table id record
     * @return Next available id
     * @throws Exception
     */
	public static int getNextId(Connection con, String table, String field) throws Exception {
		int intId = -1;
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery("SELECT MAX(" + field + ") FROM " + table + ";");
		if( !res.next() ){ throw new Exception("METViewer load error: getNextId(" + table + ", " + field + ") unable to find max id"); }
		String strId = res.getString(1);
		if( null == strId ) { intId = 0; } 
		else                { intId = (Integer.parseInt(strId) + 1); }
		stmt.close();
		return intId;
	}

	/**
	 * Search all line_data tables to determine the maximum line_data_id used among them and return it
	 * @param con database connection to search against
	 * @return the maximum line_data_id value found among the line_data tables
	 * @throws Exception
	public static int getNextLineDataId(Connection con) throws Exception{
		int intMaxId = 0;
		for(int i=0; i < _listLineDataTables.length; i++){
			int intMaxIdCur = getNextId(con, _listLineDataTables[i], "line_data_id");
			if( intMaxIdCur > intMaxId ){ intMaxId = intMaxIdCur; }			
		}
		return intMaxId;
	}
	 */

	/**
	 * Analyze the input file object to determine what type of MET output file it is.  Create an entry in
	 * the data_file table for the file and build a DataFileInfo data structure with information about
	 * the file and return it.
	 * @param file points to a MET output file to process
	 * @param con database connection to use
	 * @return data structure containing information about the input file
	 */
	public static DataFileInfo processDataFile(File file, Connection con) throws Exception {
		String strPath = file.getParent().replace("\\", "/");
		String strFile = file.getName();
		String strDataFileLuId = "-1";
		String strDataFileLuTypeName = "";
		String strDataFileId = "-1";

		// set default values for the loaded time (now) and the modified time (that of input file)
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		String strLoadDate = _formatDB.format(cal.getTime());
		cal.setTimeInMillis(file.lastModified());
		String strModDate = _formatDB.format(cal.getTime());

		// determine the type of the input data file by parsing the filename
		if     ( strFile.matches("^point_stat\\S+\\.stat$") )		{ strDataFileLuTypeName = "point_stat";    }
		else if( strFile.matches("^grid_stat\\S+\\.stat$") )		{ strDataFileLuTypeName = "grid_stat";     }
		else if( strFile.matches("^wavelet_stat\\S+\\.stat$") )		{ strDataFileLuTypeName = "wavelet_stat";  }
		else if( strFile.matches("^mode_\\S+_obj\\.txt$") )			{ strDataFileLuTypeName = "mode_obj";      }
		else if( strFile.matches("^mode_\\S+_cts\\.txt$") )			{ strDataFileLuTypeName = "mode_cts";      }
		else if( strFile.matches("^ensemble_stat.*\\S+\\.stat$") )	{ strDataFileLuTypeName = "ensemble_stat"; }
		//else{ throw new Exception("processDataFile() - could not determine file type of " + strFile); }
		else{
			//System.out.println("  **  WARNING: could not determine file type of "	+ strFile);
			return null;
		}

		if( !"".equals(strDataFileLuTypeName) ){ strDataFileLuId = (String) _tableDataFileLU.get(strDataFileLuTypeName); }
		
		// build a query to look for the file and path in the data_file table
		String strDataFileQuery = 
			"SELECT " + 
			"  dfl.type_name, " +
			"  df.data_file_id, " + 
			"  df.load_date, " + 
			"  df.mod_date " + 
			"FROM " + 
			"  data_file_lu dfl, " + 
			"  data_file df " + 
			"WHERE " + 
			"  dfl.data_file_lu_id = df.data_file_lu_id " + 
			"  AND df.filename = \'" + strFile + "\' " + 
			"  AND df.path = \'" + strPath + "\';";
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(strDataFileQuery);

		// if the data file is already present in the database, print a warning and return the id
		if( res.next() ){
			strDataFileLuTypeName = res.getString(1);
			strDataFileId = res.getString(2);
			strLoadDate = res.getString(3);
			strModDate = res.getString(4);

			DataFileInfo info = new DataFileInfo(strDataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
			System.out.println("  **  WARNING: file already present in table data_file"); // :\n" + printDataFileInfo(info) + "\n");
			return info;
		}

		// if the file is not present in the data_file table, query for the largest data_file_id
		res = stmt.executeQuery("SELECT MAX(data_file_id) FROM data_file;");
		if( !res.next() ){ throw new Exception("METViewer load error: processDataFile() unable to find max data_file_id"); }		
		strDataFileId = res.getString(1);
		if( null == strDataFileId ){ strDataFileId = "0"; }
		else					   { strDataFileId = "" + (Integer.parseInt(strDataFileId) + 1); }
		stmt.close();
		
		// add the input file to the data_file table
		String strDataFileInsert = 
			"INSERT INTO data_file VALUES (" + 
				strDataFileId + ", " +			// data_file_id
				strDataFileLuId + ", " +		// data_file_lu_id
				"'" + strFile + "', " +			// filename
				"'" + strPath + "', " +			// path
				"'" + strLoadDate + "', " +		// load_date
				"'" + strModDate + "');";		// mod_date
		int intRes = executeUpdate(con, strDataFileInsert);
		if( 1 != intRes ){ System.out.println("  **  WARNING: unexpected result from data_file INSERT: " + intRes); }
		
		return new DataFileInfo(strDataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
	}

	public static String printDataFileInfo(DataFileInfo info) {
		int intPad = 22;
		return 
			padBegin("dataFileId: ", intPad) +			info._dataFileId + "\n" + 
			padBegin("dataFileFilename: ", intPad) +	info._dataFileFilename + "\n" +
			padBegin("dataFilePath: ", intPad) +		info._dataFilePath	+ "\n" +
			padBegin("dataFileLuId: ", intPad) +		info._dataFileLuId + "\n" + 
			padBegin("dataFileLuTypeName: ", intPad) +	info._dataFileLuTypeName + "\n";
	}

	public static Hashtable _tableAlphaLineTypes = new Hashtable();
	static {
		_tableAlphaLineTypes.put("CTS", new Boolean(true));
		_tableAlphaLineTypes.put("CNT", new Boolean(true));
		_tableAlphaLineTypes.put("PSTD", new Boolean(true));
		_tableAlphaLineTypes.put("NBRCTS", new Boolean(true));
		_tableAlphaLineTypes.put("NBRCNT", new Boolean(true));
		_tableAlphaLineTypes.put("MCTS", new Boolean(true));
	}

	public static Hashtable _tableCovThreshLineTypes = new Hashtable();
	static{
		_tableCovThreshLineTypes.put("NBRCTC", new Boolean(true));
		_tableCovThreshLineTypes.put("NBRCTS", new Boolean(true));
	}
	
	public static final String[] _listLineDataTables = {
		"line_data_fho",		"line_data_ctc",		"line_data_cts",		"line_data_cnt",		"line_data_pct",
		"line_data_pstd",		"line_data_pjc",		"line_data_prc",		"line_data_sl1l2",		"line_data_sal1l2",
		"line_data_vl1l2",		"line_data_val1l2",		"line_data_mpr",		"line_data_nbrctc",		"line_data_nbrcts",
		"line_data_nbrcnt",		"line_data_isc",		"line_data_mctc",		"line_data_rhist",		"line_data_orank"
	};
	
	public static final MVOrderedMap _mapIndexes = new MVOrderedMap();
	static{
		_mapIndexes.put("#stat_header#_model_idx",				"model");
		_mapIndexes.put("#stat_header#_fcst_var_idx",			"fcst_var");
		_mapIndexes.put("#stat_header#_fcst_lev_idx",			"fcst_lev");
		_mapIndexes.put("#stat_header#_obtype_idx",				"obtype");
		_mapIndexes.put("#stat_header#_vx_mask_idx",			"vx_mask");
		_mapIndexes.put("#stat_header#_interp_mthd_idx",		"interp_mthd");
		_mapIndexes.put("#stat_header#_interp_pnts_idx",		"interp_pnts");
		_mapIndexes.put("#stat_header#_fcst_thresh_idx",		"fcst_thresh");

		_mapIndexes.put("#mode_header#_model_idx",				"model");
		_mapIndexes.put("#mode_header#_fcst_lead_idx",			"fcst_lead");
		_mapIndexes.put("#mode_header#_fcst_valid_idx",			"fcst_valid");
		_mapIndexes.put("#mode_header#_fcst_init_idx",			"fcst_init");
		_mapIndexes.put("#mode_header#_fcst_rad_idx",			"fcst_rad");
		_mapIndexes.put("#mode_header#_fcst_thr_idx",			"fcst_thr");
		_mapIndexes.put("#mode_header#_fcst_var_idx",			"fcst_var");
		_mapIndexes.put("#mode_header#_fcst_lev_idx",			"fcst_lev");
		
		for(int i=0; i < _listLineDataTables.length; i++){
			_mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_lead_idx",		"fcst_lead");
			_mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_valid_beg_idx",	"fcst_valid_beg");
			_mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_init_beg_idx",	"fcst_init_beg");
		}
	}
	
	public static final Pattern _patIndexName = Pattern.compile("#([\\w\\d]+)#([\\w\\d]+)");
	
	public static void applyIndexes(Connection con, boolean drop) throws Exception{
		
		System.out.println("    ==== indexes ====\n" + (drop? "  dropping..." : ""));
		Map.Entry[] listIndexes = _mapIndexes.getOrderedEntries();
		for(int i=0; i < listIndexes.length; i++){
			String strIndexKey = listIndexes[i].getKey().toString();
			String strField = listIndexes[i].getValue().toString();
			long intIndexStart = (new java.util.Date()).getTime();
			
			//  build a create index statment and run it
			Matcher matIndex = _patIndexName.matcher(strIndexKey);
			if( !matIndex.matches() ){ throw new Exception("  **  ERROR: failed to parse index key " + strIndexKey); }
			String strTable = matIndex.group(1);
			String strIndexName = strTable + matIndex.group(2);
			String strIndex = "";
			if( drop ){ strIndex = "DROP INDEX " + strIndexName + " ON " + strTable + " ;";                     }
			else      { strIndex = "CREATE INDEX " + strIndexName + " ON " + strTable + " (" + strField + ");"; }
			try{
				executeUpdate(con, strIndex);
			}catch(Exception e){
				System.out.println("  **  ERROR: caught " + e.getClass() + " applying index " + strIndexName + ": " + e.getMessage());
			}
			
			//  print out a performance message
			long intIndexTime = (new java.util.Date()).getTime() - intIndexStart;
			System.out.println(padBegin( strIndexName + ": ", 36) + formatTimeSpan(intIndexTime));
		}
		System.out.println();
	}
	public static void applyIndexes(Connection con) throws Exception{ applyIndexes(con, false); }
	public static void dropIndexes(Connection con) throws Exception{ applyIndexes(con, true); }
	
	
	/**
	 * Initialize the table containing the max line_data_id for all line_data tables corresponding
	 * to variable length rows.
	 * @param con database connection used to search against
	 * @throws Exception
	 */
	public static void initVarLengthLineDataIds(Connection con) throws Exception{
		_tableVarLengthLineDataId.clear();
		String[] listVarLengthLines = (String[])_tableVarLengthTable.keySet().toArray(new String[]{});
		for(int i=0; i < listVarLengthLines.length; i++){
			String strVarLengthTable = "line_data_" + listVarLengthLines[i].toLowerCase();
			int intLineDataId = getNextId(con, strVarLengthTable, "line_data_id");
			_tableVarLengthLineDataId.put(listVarLengthLines[i], new Integer(intLineDataId));
		}
	}
	
}

class DataFileInfo {
	public DataFileInfo(String dataFileId, String dataFileFilename,
			String dataFilePath, String dataFileLoadDate,
			String dataFileModDate, String dataFileLuId,
			String dataFileLuTypeName) {
		_dataFileId = dataFileId;
		_dataFileFilename = dataFileFilename;
		_dataFilePath = dataFilePath;
		_dataFileLoadDate = dataFileLoadDate;
		_dataFileModDate = dataFileModDate;
		_dataFileLuId = dataFileLuId;
		_dataFileLuTypeName = dataFileLuTypeName;
	}

	public String _dataFileId = "";
	public String _dataFileFilename = "";
	public String _dataFilePath = "";
	public String _dataFileLoadDate = "";
	public String _dataFileModDate = "";
	public String _dataFileLuId = "";
	public String _dataFileLuTypeName = "";
}

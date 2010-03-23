package edu.ucar.metviewer;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class MVLoad extends MVUtil {

	// private static final Logger _logger = Logger.getLogger(MVBatch.class);
	// private static final PrintStream _logStream = System.out;

	public static String _strHost				= "kemosabe";
	public static String _strPort				= "3306";
	// public static String _strHostPort		= "pigpen:3306";
	public static String _strDatabase			= "metvdb2_hmt";
	public static String _strUser				= "pgoldenb";
	public static String _strPwd				= "pgoldenb";
	
	public static DecimalFormat _formatPerf		= new DecimalFormat("0.000");


	public static Hashtable _tableStatHeaders		= new Hashtable(1024);
	public static boolean _boolStatHeaderTableCheck	= true;
	public static boolean _boolStatHeaderDBCheck	= false;
	public static long _intStatHeaderSearchTime		= 0;
	public static long _intStatHeaderTableTime		= 0;
	
	public static int _intLinesTotal				= 0;
	public static int _intStatHeaderRecords			= 0;
	public static int _intStatHeaderInserts			= 0;
	public static int _intLineDataRecords			= 0;
	public static int _intLineDataInserts			= 0;
	
	public static boolean _boolInsert				= false;
	public static int _intInsertSize				= 20;
	public static ArrayList _listInsertValues		= new ArrayList();
	public static Hashtable _tableLineDataValues	= new Hashtable();
	
	public static final Hashtable _tableDataFileLU	= new Hashtable();
	static {
		_tableDataFileLU.put("point_stat", "0");
		_tableDataFileLU.put("grid_stat", "1");
		_tableDataFileLU.put("mode_cts", "2");
		_tableDataFileLU.put("mode_obj", "3");
		_tableDataFileLU.put("wavelet_stat", "4");
	}

	public static void main(String[] argv) {
		System.out.println("----  MVLoad  ----\n");
		Connection con = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			if( 0 < argv.length ){ _strHost = argv[0]; }
			System.out.println(padBegin("database host: ", 36) + _strHost + "\n" +
							   padBegin("begin time: ", 36) + format.format(new java.util.Date()) + "\n");

			// connect to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + _strHost + ":" + _strPort + "/" + _strDatabase, _strUser, _strPwd);
			if( con.isClosed() ){ throw new Exception("database connection failed"); }

			// if the insert size is greater than 1, ensure that the db header check is off
			if( 1 < _intInsertSize && _boolStatHeaderDBCheck ){
				throw new Exception("insert size (" + _intInsertSize + ") > 1 and database header check turned on");
			}
			
			// * * * *   QNSE data  * * * *
			/*
			MVOrderedMap mapLoadVar = new MVOrderedMap();
			mapLoadVar.put("model", new String[] { "AFWAop", "QNSE" });
			//mapLoadVar.put("date", buildDateList("2008060212", "2008060512", 36 * 3600, "yyyyMMddHH"));
			mapLoadVar.put("date", buildDateList("2008060212", "2008080112", 36 * 3600, "yyyyMMddHH"));
			//mapLoadVar.put("date", new String[]{"2008060212"});

			String strBaseFolderTmpl = "/var/autofs/mnt/pd6/score/DTC/AFWA_RC/{model}/{date}/metprd";
			//String strBaseFolderTmpl = "c:/src/QNSE_met/{model}/{date}/metprd";
			 */

			//  * * * *  HMT data  * * * *
			/*
			*/
			MVOrderedMap mapLoadVar = new MVOrderedMap();
			mapLoadVar.put("model", new String[] {"arw-tom-gep0", "arw-fer-gep1", "arw-sch-gep2", "arw-tom-gep3", "nmm-fer-gep4", 
					  							  "arw-fer-gep5", "arw-sch-gep6", "arw-tom-gep7", "nmm-fer-gep8", "gfs", "ens-mean"});
			String[] listDates = buildDateList("2009122118V_06h", "2010022112V_06h", 6 * 3600, "yyyyMMddHH'V_06h'");
			listDates = append(listDates, buildDateList("2009122212V_24h", "2010022212V_24h", 24 * 3600, "yyyyMMddHH'V_24h'"));
			Arrays.sort(listDates, new Comparator(){
				public int compare(Object o1, Object o2){ return ((String)o1).compareTo( (String)o2 ); }
			});
			mapLoadVar.put("date", listDates);

			String strBaseFolderTmpl = "/var/autofs/mnt/pd6/score/DTC/HMT/West/dwr_domains/{model}/{date}/grid_stat";
			
			
			long intLoadTimeStart = (new java.util.Date()).getTime();
			int intNumFiles = 0;
			int intLinesPrev = 0;
			
			MVOrderedMap[] listPerm = permute(mapLoadVar).getRows();
			for (int intPerm = 0; intPerm < listPerm.length; intPerm++) {
				String strBaseFolder = buildTemplateString(strBaseFolderTmpl, listPerm[intPerm]);
				
				System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " - " + strBaseFolder + "\n" + 
								   listPerm[intPerm].getRDecl());
				long intPermStart = (new java.util.Date()).getTime();

				File fileBaseFolder = new File(strBaseFolder);
				if (!fileBaseFolder.exists()) {
					System.out.println("  **  WARNING: base folder not found: " + fileBaseFolder);
					continue;
				}
				File[] listDataFiles = fileBaseFolder.listFiles();
				
				for (int j = 0; j < listDataFiles.length; j++) {
					long intProcessDataFileBegin = (new java.util.Date()).getTime();
					DataFileInfo info = processDataFile(listDataFiles[j], con);
					if( null == info ){ continue; }
					long intProcessDataFileTime = (new java.util.Date()).getTime() - intProcessDataFileBegin;
					System.out.println("  " + info._dataFilePath + "/" + info._dataFileFilename + "\n" + 
									   padBegin("data file time: ", 32) + formatTimeSpan(intProcessDataFileTime));
					
					if( info._dataFileLuTypeName.equals("point_stat") || info._dataFileLuTypeName.equals("grid_stat") ){
						loadStatFile(info, con);
					} else if( info._dataFileLuTypeName.equals("mode_obj") ){
						
					}
					intNumFiles++;
				}
				
				int intLinesPerm = _intLinesTotal - intLinesPrev;
				intLinesPrev = _intLinesTotal;
				System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " complete - insert time: " + 
								   formatTimeSpan( (new java.util.Date()).getTime() - intPermStart ) + "  lines: " + intLinesPerm + "\n");
			}

			//  print a performance report
			long intLoadTime = (new java.util.Date()).getTime() - intLoadTimeStart;
			double dblLinesPerMSec =  (double)_intLinesTotal / (double)(intLoadTime);
			
			System.out.println(padBegin("load total: ", 36) + formatTimeSpan(intLoadTime) + "\n" +
							   (_boolStatHeaderDBCheck? padBegin("stat_header search time total: ", 36) + formatTimeSpan(_intStatHeaderSearchTime) + "\n" : "") +
							   (_boolStatHeaderTableCheck? padBegin("stat_header table time total: ", 36) + formatTimeSpan(_intStatHeaderTableTime) + "\n" : "") +
							   padBegin("stat header records: ", 36) + _intStatHeaderRecords + "\n" +
							   padBegin("stat header inserts: ", 36) + _intStatHeaderInserts + "\n" +
							   padBegin("line data records: ", 36) + _intLineDataRecords + "\n" +
							   padBegin("line data inserts: ", 36) + _intLineDataInserts + "\n" +
							   padBegin("total lines: ", 36) + _intLinesTotal + "\n" +
							   padBegin("insert size: ", 36) + _intInsertSize + "\n" +
							   padBegin("lines / msec: ", 36) + _formatPerf.format(dblLinesPerMSec) + "\n" +
							   padBegin("num files: ", 36) + intNumFiles + "\n" +
							   padBegin("end time: ", 36) + format.format(new java.util.Date()) + "\n");

		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null ){ con.close(); } }catch(SQLException e){}
		}

		System.out.println("\n----  MVLoad Done  ----");
	}

	public static void loadStatFile(DataFileInfo info, Connection con) throws Exception{

		int intStatHeaderIdNext = getNextStatHeaderId(con);
		
		long intStatHeaderLoadStart = (new java.util.Date()).getTime();
		long intStatHeaderSearchTime = 0;
		int intStatHeaderRecords = 0;
		int intStatHeaderInserts = 0;
		int intLineDataRecords = 0;
		int intLineDataInserts = 0;
		String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
		BufferedReader reader = new BufferedReader( new FileReader(strFilename) );
		int intLine = 1;
		while( reader.ready() ){
			String[] listToken = reader.readLine().split("\\s+");
			
			//  the first line is the header line
			if( 1 > listToken.length || listToken[0].equals("VERSION") ){
				intLine++;
				continue;
			}
			
			String strFileLine = strFilename + ":" + intLine;
			
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
			int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(4,6));
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(2,4)) * 60;
			intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0,2)) * 3600;
			
			//  determine the init time by combining fcst_valid_beg and fcst_lead
			Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calFcstInitBeg.setTime(dateFcstValidBeg);
			calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
			java.util.Date dateFcstInitBeg = calFcstInitBeg.getTime();
			String strFcstInitBeg = _formatDB.format(dateFcstInitBeg);
			
			//  build the stat_header value list for this line
			String strStatHeaderValueList =
					"'" + listToken[0] + "', " +		//  version
					"'" + listToken[1] + "', " +		//  model
					"'" + strFcstLead + "', " +			//  fcst_lead
					"'" + strFcstValidBeg + "', " +		//  fcst_valid_beg
					"'" + strFcstValidEnd + "', " +		//  fcst_valid_end
					"'" + strFcstInitBeg + "', " +		//  fcst_init_beg
					listToken[5] + ", " +				//  obs_lead
					"'" + strObsValidBeg + "', " +		//  obs_valid_beg
					"'" + strObsValidEnd + "', " +		//  obs_valid_end
					"'" + listToken[8] + "', " +		//  fcst_var
					"'" + listToken[9] + "', " +		//  fcst_lev
					"'" + listToken[10] + "', " +		//  obs_var
					"'" + listToken[11] + "', " +		//  obs_lev
					"'" + listToken[12] + "', " +		//  obtype
					"'" + listToken[13] + "', " +		//  vx_mask
					"'" + listToken[14] + "', " +		//  interp_mthd
					listToken[15] + ", " +				//  interp_pnts
					"'" + listToken[16] + "', " +		//  fcst_thresh
					"'" + listToken[17] + "'";			//  obs_thresh

			//  build a where clause for searching for duplicate stat_header records
			String strStatHeaderWhereClause =
					"  version = '" +				listToken[0] + "'\n" +
					"  AND model = '" +				listToken[1] + "'\n" +
					"  AND fcst_lead = '" +			strFcstLead + "'\n" +
					"  AND fcst_valid_beg = '" +	strFcstValidBeg + "'\n" +
					"  AND fcst_valid_end = '" +	strFcstValidEnd + "'\n" +
					"  AND fcst_init_beg = '" +		strFcstInitBeg + "'\n" +
					"  AND obs_lead = " +			listToken[5] + "\n" +
					"  AND obs_valid_beg = '" +		strObsValidBeg + "'\n" +
					"  AND obs_valid_end = '" +		strObsValidEnd + "'\n" +
					"  AND fcst_var = '" +			listToken[8] + "'\n" +
					"  AND fcst_lev = '" +			listToken[9] + "'\n" +
					"  AND obs_var = '" +			listToken[10] + "'\n" +
					"  AND obs_lev = '" +			listToken[11] + "'\n" +
					"  AND obtype = '" +			listToken[12] + "'\n" +
					"  AND vx_mask = '" +			listToken[13] + "'\n" +
					"  AND interp_mthd = '" +		listToken[14] + "'\n" +
					"  AND interp_pnts = " +		listToken[15] + "\n" +
					"  AND fcst_thresh = '" +		listToken[16] + "'\n" +
					"  AND obs_thresh = '" +		listToken[17] + "'";
			
			//  look for the header key in the table, and record the time taken
			boolean boolStatHeaderPresent = false; 
			long intStatHeaderTableBegin = (new java.util.Date()).getTime();
			if( _boolStatHeaderTableCheck ){
				boolStatHeaderPresent = _tableStatHeaders.containsKey(strStatHeaderValueList);
			}
			
			//  check the table to see if a stat_header already exists
			int intStatHeaderId = -1;
			if( boolStatHeaderPresent ){
				intStatHeaderId = ((Integer)_tableStatHeaders.get(strStatHeaderValueList)).intValue();
				_intStatHeaderTableTime += (new java.util.Date()).getTime() - intStatHeaderTableBegin;
				
			//  if the stat_header does not yet exist, create one
			} else {
				_intStatHeaderTableTime += (new java.util.Date()).getTime() - intStatHeaderTableBegin;				
				intStatHeaderId = intStatHeaderIdNext++;
				_tableStatHeaders.put(strStatHeaderValueList, new Integer(intStatHeaderId));
				
				//  look for an existing stat_header record with the same information
				boolean boolFoundStatHeader = false;
				long intStatHeaderSearchBegin = (new java.util.Date()).getTime();
				if( _boolStatHeaderDBCheck ){
					String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
					ResultSet res = con.createStatement().executeQuery(strStatHeaderSelect);
					if( res.next() ){
						String strStatHeaderIdDup = res.getString(1);
						boolFoundStatHeader = true;
						System.out.println("  **  WARNING: found duplicate stat_header record with id " + strStatHeaderIdDup + "\n        " + strFileLine);
					}
				}
				intStatHeaderSearchTime = (new java.util.Date()).getTime() - intStatHeaderSearchBegin;
				_intStatHeaderSearchTime += intStatHeaderSearchTime;
				
				if( !boolFoundStatHeader ){
					//  add the value list to the values list
					_listInsertValues.add("(" + intStatHeaderId + ", " + strStatHeaderValueList + ")");
					intStatHeaderRecords++;
				}
			}	
			
			//  build and execute the stat header insert statement, if the list length has reached the insert size or the end of the file
			_boolInsert = _intInsertSize <= _listInsertValues.size() || (0 < _listInsertValues.size() && !reader.ready());						
			if( _boolInsert ){
				String strValueList = "";
				for(int i=0; i < _listInsertValues.size(); i++){
					strValueList += (0 < i? ", " : "") + _listInsertValues.get(i).toString(); 
				}
				String strStatHeaderInsert = "INSERT INTO stat_header VALUES " + strValueList + ";";
				try{
					int intResStatHeaderInsert = con.createStatement().executeUpdate(strStatHeaderInsert);
					if( _listInsertValues.size() != intResStatHeaderInsert ){
						System.out.println("  **  WARNING: unexpected result from stat_header INSERT: " + intResStatHeaderInsert + "\n" +
									  	   "        " + strFileLine);
					}
				}catch(Exception e){
					throw e;
				}
				_listInsertValues.clear();
				intStatHeaderInserts++;
			}
						
			//  build a value list for the line data insert
			String strLineType = listToken[20];
			String strLineDataValueList = "" +
					intStatHeaderId + ", " +			//  stat_header_id
					info._dataFileId + ", " +			//  data_file_id
					intLine + ", " +					//  line_num
					listToken[21] + ", ";				//  total
			
			//  if the line data requires a cov_thresh value, add it
			String strCovThresh = listToken[18];
			if( _tableCovThreshLineTypes.containsKey(strLineType) ){
				if( strCovThresh.equals("NA") ){ System.out.println("  **  WARNING: cov_thresh value NA on line " + intLine + " with line type '" + strLineType + "'\n" +
																	"        " + strFileLine); }
				strLineDataValueList += "'" + replaceInvalidValues(strCovThresh) + "', ";
			} else if( !strCovThresh.equals("NA") ){
				System.out.println("  **  WARNING: unexpected cov_thresh value '" + strCovThresh + "' on line " + intLine + " with line type '" + strLineType + "'\n" + 
								   "        " + strFileLine);
			}

			//  if the line data requires an alpha value, add it
			String strAlpha = listToken[19];
			if( _tableAlphaLineTypes.containsKey(strLineType) ){
				if( strAlpha.equals("NA") ){ System.out.println("  **  WARNING: alpha value NA on line " + intLine + " with line type '" + strLineType + "'\n" + 
																"        " + strFileLine); }
				strLineDataValueList += replaceInvalidValues(strAlpha) + ", ";
			} else if( !strAlpha.equals("NA") ){
				System.out.println("  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + strLineType + "'\n        " + strFileLine);
			}
			
			//  add the rest of the fields for the line data insert
			for(int i=22; i < listToken.length; i++){
				strLineDataValueList += (22 == i? "" : ", ") + replaceInvalidValues(listToken[i]);
			}
			
			//  add the values list to the line type values map
			ArrayList listLineTypeValues = new ArrayList();
			if( _tableLineDataValues.containsKey(strLineType) ){ listLineTypeValues = (ArrayList)_tableLineDataValues.get(strLineType); }
			listLineTypeValues.add("(" + strLineDataValueList + ")");
			_tableLineDataValues.put(strLineType, listLineTypeValues);
			
			if( _boolInsert ){
				
				//  for each line type, build an insert statement with the appropriate list of values
				for(Iterator iterEntries = _tableLineDataValues.entrySet().iterator(); iterEntries.hasNext();){
					Map.Entry entry = (Map.Entry)iterEntries.next();
					strLineType = entry.getKey().toString();
					ArrayList listValues = (ArrayList)entry.getValue();
					
					//  build the list of value lists for this line type
					String strValueList = "";
					for(int i=0; i < listValues.size(); i++){
						strValueList += (0 < i? ", " : "") + listValues.get(i).toString();
					}
				
					//  build and execute the line data insert statement
					String strLineDataTable = "line_data_" + strLineType.toLowerCase();
					String strStatHeaderInsert = "INSERT INTO " + strLineDataTable + " VALUES " + strValueList + ";";	
					int intResLineDataInsert = con.createStatement().executeUpdate(strStatHeaderInsert);
					if( listValues.size() != intResLineDataInsert ){ System.out.println("  **  WARNING: unexpected result from line_data INSERT on line " + intLine + ": " + 
																						intResLineDataInsert + "\n        " + strFileLine); }
					intLineDataInserts++;
				}
				_tableLineDataValues.clear();
			}
			_boolInsert = false;
			intLineDataRecords++;
			intLine++;
		}
		reader.close();
		_tableStatHeaders.clear();
		
		_intLinesTotal += (intLine - 1);
		_intStatHeaderRecords += intStatHeaderRecords;
		_intStatHeaderInserts += intStatHeaderInserts;
		_intLineDataInserts += intLineDataInserts;
		_intLineDataRecords += intLineDataRecords;
		
		//  print a performance report
		long intStatHeaderLoadTime = (new java.util.Date()).getTime() - intStatHeaderLoadStart;
		double dblLinesPerMSec = (double)(intLine - 1) / (double)(intStatHeaderLoadTime);
		
		System.out.println(padBegin("stat_header records: ", 32) + intStatHeaderRecords + "\n" +
						   padBegin("stat_header inserts: ", 32) + intStatHeaderInserts + "\n" +
						   padBegin("line_data records: ", 32) + intLineDataRecords + "\n" +
						   padBegin("line_data inserts: ", 32) + intLineDataInserts + "\n" +
						   padBegin("total load time: ", 32) + formatTimeSpan(intStatHeaderLoadTime) + "\n" +
						   (_boolStatHeaderDBCheck? padBegin("stat_header search time: ", 32) + formatTimeSpan(intStatHeaderSearchTime) + "\n": "") +
						   padBegin("lines / msec: ", 32) + _formatPerf.format(dblLinesPerMSec) + "\n\n");
	}
	
	public static String replaceInvalidValues(String strData){ return strData.replace("NA", "-9999").replace("nan", "-9999"); }

	public static int getNextStatHeaderId(Connection con) throws Exception {
		int intStatHeaderId = -1;
		ResultSet res = con.createStatement().executeQuery("SELECT MAX(stat_header_id) FROM stat_header;");
		if( !res.next() ){ throw new Exception("processDataFile() unable to find max data_file_id"); }
		String strStatHeaderId = res.getString(1);
		if( null == strStatHeaderId )	{ intStatHeaderId = 0; } 
		else							{ intStatHeaderId = (Integer.parseInt(strStatHeaderId) + 1); }
		return intStatHeaderId;
	}

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
		if( strFile.matches("^point_stat.*") )				{ strDataFileLuTypeName = "point_stat";   }
		else if( strFile.matches("^grid_stat.*") )			{ strDataFileLuTypeName = "grid_stat";    }
		else if( strFile.matches("^wavelet_stat.*") )		{ strDataFileLuTypeName = "wavelet_stat"; }
		//else if( strFile.matches("^mode_\\S+_obj\\.txt$") )	{ strDataFileLuTypeName = "mode_obj";     }
		//else if( strFile.matches("^mode_\\S+_cts\\.txt$") )	{ strDataFileLuTypeName = "mode_cts";     }
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
		ResultSet res = con.createStatement().executeQuery(strDataFileQuery);

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
		res = con.createStatement().executeQuery("SELECT MAX(data_file_id) FROM data_file;");
		if( !res.next() ){ throw new Exception("processDataFile() unable to find max data_file_id"); }		
		strDataFileId = res.getString(1);
		if( null == strDataFileId ){ strDataFileId = "0"; }
		else					   { strDataFileId = "" + (Integer.parseInt(strDataFileId) + 1); }

		// add the input file to the data_file table
		String strDataFileInsert = 
			"INSERT INTO data_file VALUES (" + 
				strDataFileId + ", " +			// data_file_id
				strDataFileLuId + ", " +		// data_file_lu_id
				"'" + strFile + "', " +			// filename
				"'" + strPath + "', " +			// path
				"'" + strLoadDate + "', " +		// load_date
				"'" + strModDate + "');";		// mod_date
		int intRes = con.createStatement().executeUpdate(strDataFileInsert);
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
	}

	public static Hashtable _tableCovThreshLineTypes = new Hashtable();
	static{
		_tableCovThreshLineTypes.put("NBRCTC", new Boolean(true));
		_tableCovThreshLineTypes.put("NBRCTS", new Boolean(true));
	}
	
	public static Hashtable _tableIndexes = new Hashtable();
	static{
		_tableIndexes.put("stat_header_model_idx",			"model");
		_tableIndexes.put("stat_header_fcst_var_idx",		"fcst_var");
		_tableIndexes.put("stat_header_fcst_lev_idx",		"fcst_lev");
		_tableIndexes.put("stat_header_fcst_lead_idx",		"fcst_lead");
		_tableIndexes.put("stat_header_vx_mask_idx",		"vx_mask");
		_tableIndexes.put("stat_header_interp_mthd_idx",	"interp_mthd");
		_tableIndexes.put("stat_header_initdate_idx",		"initdate");
		_tableIndexes.put("stat_header_fcst_valid_beg_idx",	"fcst_valid_beg");
		_tableIndexes.put("mode_header_model_idx",			"model");
		_tableIndexes.put("mode_header_fcst_var_idx",		"fcst_var");
		_tableIndexes.put("mode_header_fcst_lev_idx",		"fcst_lev");
		_tableIndexes.put("mode_header_fcst_rad_idx",		"fcst_rad");
	}

	public static void applyIndexes(Connection con){
		
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

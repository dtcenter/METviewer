package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class MVLoad extends MVUtil {

	// private static final Logger _logger = Logger.getLogger(MVBatch.class);
	// private static final PrintStream _logStream = System.out;

	//public static String _strHost				= "kemosabe";
	//public static String _strPort				= "3306";
	//public static String _strHostPort		= "pigpen:3306";
	//public static String _strDatabase			= "metvdb5_hwt";
	//public static String _strUser				= "pgoldenb";
	//public static String _strPwd				= "pgoldenb";

	public static boolean _boolVerbose				= false;
	public static int _intInsertSize				= 1;
	public static boolean _boolStatHeaderTableCheck	= true;
	public static boolean _boolStatHeaderDBCheck	= false;
	public static boolean _boolModeHeaderDBCheck	= false;
	public static boolean _boolDropIndexes			= false;
	public static boolean _boolApplyIndexes			= false;
	
	public static DecimalFormat _formatPerf		= new DecimalFormat("0.000");

	public static final Pattern _patModeSingle	= Pattern.compile("^(C?[FO]\\d{3})$");
	public static final Pattern _patModePair	= Pattern.compile("^(C?F\\d{3})_(C?O\\d{3})$");
	
	public static Hashtable _tableStatHeaders		= new Hashtable(1024);
	public static Hashtable _tableModeHeaders		= new Hashtable(1024);

	public static long _intStatHeaderSearchTime		= 0;
	public static long _intStatHeaderTableTime		= 0;
	public static long _intModeHeaderSearchTime		= 0;
	
	public static int _intStatLinesTotal			= 0;
	public static int _intStatHeaderRecords			= 0;
	public static int _intStatHeaderInserts			= 0;
	public static int _intLineDataRecords			= 0;
	public static int _intLineDataInserts			= 0;
	public static int _intStatGroupRecords			= 0;
	public static int _intStatGroupInserts			= 0;
	public static int _intThreshRecords				= 0;
	public static int _intThreshInserts				= 0;
	public static int _intModeLinesTotal			= 0;
	public static int _intModeHeaderRecords			= 0;
	public static int _intModeCtsRecords			= 0;
	public static int _intModeObjSingleRecords		= 0;
	public static int _intModeObjPairRecords		= 0;
	
	public static final Hashtable _tableDataFileLU	= new Hashtable();
	static {
		_tableDataFileLU.put("point_stat", "0");
		_tableDataFileLU.put("grid_stat", "1");
		_tableDataFileLU.put("mode_cts", "2");
		_tableDataFileLU.put("mode_obj", "3");
		_tableDataFileLU.put("wavelet_stat", "4");
	}
	
	/*
	 * line type format: 
	 *   - line_type_lu_id
	 *   - # line data fields (redundant for lines with stat groups)
	 *   - (optional) list of stat group field lookup ids
	 *     - with both normal & bootstrap confidence interval
	 *     - only bootstrap confidence interval
	 *     - only normal confidence interval
	 *     - no confidence intervals
	 */
	public static final Hashtable _tableLineType	= new Hashtable();
	static {
		_tableLineType.put("FHO",		new int[][]{ new int[]{0},	new int[]{3},	new int[]{} });
		_tableLineType.put("CTC",		new int[][]{ new int[]{1},	new int[]{4},	new int[]{} });
		_tableLineType.put("CTS",		new int[][]{ new int[]{2},	new int[]{0},	new int[]{0, 1, 2, 4, 5, 6, 7, 8, 10, 12}, new int[]{3, 9, 11}, new int[]{},	new int[]{} });
		_tableLineType.put("CNT",		new int[][]{ new int[]{3},	new int[]{5},	new int[]{13, 14, 15, 16, 17, 18, 19}, new int[]{20, 21, 22, 23, 24, 25, 26, 27, 28, 29}, new int[]{},	new int[]{} });
		_tableLineType.put("PCT",		new int[][]{ new int[]{4},	new int[]{1},	new int[]{} });
		_tableLineType.put("PSTD",		new int[][]{ new int[]{5},	new int[]{1},	new int[]{}, new int[]{}, new int[]{30},	new int[]{31, 32, 33, 34} });
		_tableLineType.put("PJC",		new int[][]{ new int[]{6},	new int[]{1},	new int[]{} });
		_tableLineType.put("PRC",		new int[][]{ new int[]{7},	new int[]{1},	new int[]{} });
		_tableLineType.put("SL1L2",		new int[][]{ new int[]{8},	new int[]{5},	new int[]{} });
		_tableLineType.put("SAL1L2",	new int[][]{ new int[]{9},	new int[]{5},	new int[]{} });
		_tableLineType.put("VL1L2",		new int[][]{ new int[]{10},	new int[]{7},	new int[]{} });
		_tableLineType.put("VAL1L2",	new int[][]{ new int[]{11},	new int[]{7},	new int[]{} });
		_tableLineType.put("MPR",		new int[][]{ new int[]{12},	new int[]{8},	new int[]{} });
		_tableLineType.put("NBRCTC",	new int[][]{ new int[]{13},	new int[]{4},	new int[]{} });
		_tableLineType.put("NBRCTS",	new int[][]{ new int[]{14},	new int[]{0},	new int[]{35, 36, 37, 39, 40, 41, 42, 43, 45, 47}, new int[]{38, 44, 46}, new int[]{},	new int[]{} });
		_tableLineType.put("NBRCNT",	new int[][]{ new int[]{15},	new int[]{0},	new int[]{}, new int[]{48, 49}, new int[]{},	new int[]{} });
		_tableLineType.put("ISC",		new int[][]{ new int[]{16},	new int[]{10},	new int[]{} });
	}

	/*
	 * stat group data indices for line data fields and stat group fields
	 *   - line data indices (single member)
	 *   - stat group indices with normal & bootstrap CIs (5 members)
	 *   - stat group indices with bootstrap CI only (3 members)
	 *   - stat group indices with normal CI only (3 members)
	 *   - stat group indices with no CIs (1 member)
	 */
	public static final Hashtable _tableStatGroupIndices = new Hashtable(); 
	static {			
		_tableStatGroupIndices.put("CTS",		new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices.put("CNT",		new int[][]{ new int[]{47, 48, 49, 50, 51}, new int[]{22, 27, 32, 37, 42, 52, 57}, new int[]{62, 65, 68, 71, 74, 77, 80, 83, 86, 89}, new int[]{}, new int[]{} });
		_tableStatGroupIndices.put("PSTD",		new int[][]{ new int[]{22}, new int[]{}, new int[]{}, new int[]{27}, new int[]{23, 24, 25, 26} });
		_tableStatGroupIndices.put("NBRCTS",	new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices.put("NBRCNT",	new int[][]{ new int[]{}, new int[]{}, new int[]{22, 25}, new int[]{}, new int[]{} });
	}

	/*
	 * thresh group data indices for probabilistic line data
	 *   - index of first repeating probabilistic fields
	 *   - number of fields in each repeating set
	 */
	public static final Hashtable _tableThreshGroupIndices = new Hashtable(); 
	static {			
		_tableThreshGroupIndices.put("PCT",  new int[]{23, 3});
		_tableThreshGroupIndices.put("PSTD", new int[]{30, 1});
		_tableThreshGroupIndices.put("PJC",	 new int[]{23, 7});
		_tableThreshGroupIndices.put("PRC",  new int[]{23, 3});
	}
	
	public static void main(String[] argv) {
		System.out.println("----  MVLoad  ----\n");
		Connection con = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			System.out.println(padBegin("begin time: ", 36) + format.format(new java.util.Date()) + "\n");

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
			MVOrderedMap mapLoadVar = new MVOrderedMap();
			mapLoadVar.put("model", new String[] {"arw-tom-gep0", "arw-fer-gep1", "arw-sch-gep2", "arw-tom-gep3", "nmm-fer-gep4", 
					  							  "arw-fer-gep5", "arw-sch-gep6", "arw-tom-gep7", "nmm-fer-gep8", "gfs", "ens-mean"});
			String[] listDates = buildDateList("2009122118V_06h", "2010040518V_06h", 6 * 3600, "yyyyMMddHH'V_06h'");
			listDates = append(listDates, buildDateList("2009122212V_24h", "2010040512V_24h", 24 * 3600, "yyyyMMddHH'V_24h'"));
			Arrays.sort(listDates, new Comparator(){
				public int compare(Object o1, Object o2){ return ((String)o1).compareTo( (String)o2 ); }
			});
			mapLoadVar.put("date", listDates);
			mapLoadVar.put("data_type", new String[]{"mode", "grid_stat", "point_stat"});

			String strBaseFolderTmpl = "/var/autofs/mnt/pd6/score/DTC/HMT/West/rerun/dwr_domains/{model}/{date}/{data_type}";

			mapLoadVar.put("model", new String[] {"ens-mean"});
			mapLoadVar.put("date", new String[]{"2010012418V_06h"});
			mapLoadVar.put("data_type", new String[]{"grid_stat"});

			//  * * * *  HWT prob data  * * * *
			MVOrderedMap mapLoadVar = new MVOrderedMap();
			mapLoadVar.put("model", new String[] {"srf"});
			String[] listDates = buildDateList("2010042412V_06h", "2010050212V_06h", 3 * 3600, "yyyyMMddHH'V_06h'");
			listDates = append(listDates, buildDateList("2010042412V_03h", "2010050212V_03h", 3 * 3600, "yyyyMMddHH'V_03h'"));
			//String[] listDates = buildDateList("2010042412V_06h", "2010042415V_06h", 3 * 3600, "yyyyMMddHH'V_06h'");
			//listDates = append(listDates, buildDateList("2010042412V_06h", "20100424212V_06h", 3 * 3600, "yyyyMMddHH'V_03h'"));
			Arrays.sort(listDates, new Comparator(){
				public int compare(Object o1, Object o2){ return ((String)o1).compareTo( (String)o2 ); }
			});
			mapLoadVar.put("date", listDates);
			mapLoadVar.put("data_type", new String[]{"grid_stat"});

			String strBaseFolderTmpl = "/d1/pgoldenb/var/hwt/data/{date}/{data_type}";			
			*/

			if( 1 > argv.length ){
				System.out.println("usage: ./load.sh [load_file]\n\n----  MVLoad Done  ----");
				return;
			}
			
			//  parse the plot job
			MVLoadJobParser parser = new MVLoadJobParser(argv[0]);
			MVLoadJob job = parser.getLoadJob();
			
			/*
			// connect to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + job.getDBHost() + "/" + job.getDBName(), job.getDBName(), job.getDBPassword());
			if( con.isClosed() ){ throw new Exception("database connection failed"); }
			*/
			
			con = job.getConnection();
			
			_boolVerbose				= job.getVerbose();
			_intInsertSize				= job.getInsertSize();
			_boolStatHeaderTableCheck	= job.getStatHeaderTableCheck();
			_boolStatHeaderDBCheck		= job.getStatHeaderDBCheck();
			_boolModeHeaderDBCheck		= job.getModeHeaderDBCheck();
			_boolDropIndexes			= job.getDropIndexes();
			_boolApplyIndexes			= job.getApplyIndexes();
			
			// if the insert size is greater than 1, ensure that the db header check is off
			if( 1 < _intInsertSize && _boolStatHeaderDBCheck ){
				throw new Exception("insert size (" + _intInsertSize + ") > 1 and database header check turned on");
			}
			
			long intLoadTimeStart = (new java.util.Date()).getTime();
			int intNumStatFiles = 0;
			int intNumModeFiles = 0;
			int intStatLinesPrev = 0;
			int intModeLinesPrev = 0;
			
			if( _boolDropIndexes ){
				dropIndexes(con);
			}
			
			MVOrderedMap[] listPerm = permute(job.getLoadVal()).getRows();
			for (int intPerm = 0; intPerm < listPerm.length; intPerm++) {
				String strBaseFolder = buildTemplateString(job.getFolderTmpl(), listPerm[intPerm]);
				
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
					System.out.println("  " + info._dataFilePath + "/" + info._dataFileFilename + 
										(_boolVerbose? "\n" + padBegin("data file time: ", 36) + formatTimeSpan(intProcessDataFileTime) : ""));
					
					if( info._dataFileLuTypeName.equals("point_stat") || info._dataFileLuTypeName.equals("grid_stat") ){
						loadStatFile(info, con);
						intNumStatFiles++;
					} else if( info._dataFileLuTypeName.equals("mode_obj") || info._dataFileLuTypeName.equals("mode_cts") ){
						loadModeFile(info, con);
						intNumModeFiles++;
					}
				}
				
				_tableModeHeaders.clear();
				
				int intStatLinesPerm = _intStatLinesTotal - intStatLinesPrev;
				int intModeLinesPerm = _intModeLinesTotal - intModeLinesPrev;
				intStatLinesPrev = _intStatLinesTotal;
				intModeLinesPrev = _intModeLinesTotal;
				System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " complete - insert time: " + 
								   formatTimeSpan( (new java.util.Date()).getTime() - intPermStart ) + "  stat lines: " + intStatLinesPerm + 
								   "  mode lines: " + intModeLinesPerm + "\n");
			}

			//  print a performance report
			long intLoadTime = (new java.util.Date()).getTime() - intLoadTimeStart;
			double dblLinesPerMSec =  (double)_intStatLinesTotal / (double)(intLoadTime);
			
			System.out.println("\n    ==== grid_stat ====\n\n" +
							   (_boolStatHeaderDBCheck? padBegin("stat_header search time total: ", 36) + formatTimeSpan(_intStatHeaderSearchTime) + "\n" : "") +
							   (_boolStatHeaderTableCheck? padBegin("stat_header table time total: ", 36) + formatTimeSpan(_intStatHeaderTableTime) + "\n" : "") +
							   padBegin("stat header records: ", 36) + _intStatHeaderRecords + "\n" +
							   padBegin("stat header inserts: ", 36) + _intStatHeaderInserts + "\n" +
							   padBegin("line data records: ", 36) + _intLineDataRecords + "\n" +
							   padBegin("line data inserts: ", 36) + _intLineDataInserts + "\n" +
							   padBegin("stat group records: ", 36) + _intStatGroupRecords + "\n" +
							   padBegin("stat group inserts: ", 36) + _intStatGroupInserts + "\n" +
							   padBegin("thresh records: ", 36) + _intThreshRecords + "\n" +
							   padBegin("thresh inserts: ", 36) + _intThreshInserts + "\n" +
							   padBegin("total lines: ", 36) + _intStatLinesTotal + "\n" +
							   padBegin("insert size: ", 36) + _intInsertSize + "\n" +
							   padBegin("lines / msec: ", 36) + _formatPerf.format(dblLinesPerMSec) + "\n" +
							   padBegin("num files: ", 36) + intNumStatFiles + "\n\n" +
							   "    ==== mode ====\n\n" +
							   (_boolModeHeaderDBCheck? padBegin("mode_header search time total: ", 36) + formatTimeSpan(_intModeHeaderSearchTime) + "\n" : "") +
							   padBegin("mode_header inserts: ", 36) + _intModeHeaderRecords + "\n" +
							   padBegin("mode_cts inserts: ", 36) + _intModeCtsRecords + "\n" +
							   padBegin("mode_obj_single inserts: ", 36) + _intModeObjSingleRecords + "\n" +
							   padBegin("mode_obj_pair inserts: ", 36) + _intModeObjPairRecords + "\n" +
							   padBegin("total lines: ", 36) + _intModeLinesTotal + "\n" +
							   padBegin("num files: ", 36) + intNumModeFiles + "\n\n");
			
			if( _boolApplyIndexes ){
				applyIndexes(con);
			}

			System.out.println(padBegin("end time: ", 36) + format.format(new java.util.Date()) + "\n" +
					   		   padBegin("load total: ", 36) + formatTimeSpan(intLoadTime) + "\n");
		} catch (Exception e) {
			System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try{ if( con != null ){ con.close(); } }catch(SQLException e){}
		}

		System.out.println("\n----  MVLoad Done  ----");
	}

	public static void loadStatFile(DataFileInfo info, Connection con) throws Exception{

		//  data structures for storing value strings
		ArrayList listInsertValues		= new ArrayList();
		Hashtable tableLineDataValues	= new Hashtable();
		ArrayList listStatGroupInsertValues = new ArrayList();
		Hashtable tableThreshValues		= new Hashtable();
		
		//  initialize the threshold value lists
		tableThreshValues.put("PCT", new ArrayList());
		tableThreshValues.put("PSTD", new ArrayList());
		tableThreshValues.put("PJC", new ArrayList());
		tableThreshValues.put("PRC", new ArrayList());
		
		//  performance counters
		long intStatHeaderLoadStart = (new java.util.Date()).getTime();
		long intStatHeaderSearchTime = 0;
		int intStatHeaderRecords = 0;
		int intStatHeaderInserts = 0;
		int intLineDataRecords = 0;
		int intLineDataInserts = 0;
		int intStatGroupRecords = 0;
		int intStatGroupInserts = 0;
		int intThreshRecords = 0;
		int intThreshInserts = 0;

		//  get the next stat record ids from the database
		int intStatHeaderIdNext = getNextId(con, "stat_header", "stat_header_id");
		int intLineDataId = getNextLineDataId(con);
		
		//  set up the input file for reading
		String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
		BufferedReader reader = new BufferedReader( new FileReader(strFilename) );
		int intLine = 1;
		
		//  read in each line of the input file
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
			
			/*
			 * * * *  stat_header insert  * * * *
			 */
			
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
					Statement stmt = con.createStatement();
					ResultSet res = stmt.executeQuery(strStatHeaderSelect);
					if( res.next() ){
						String strStatHeaderIdDup = res.getString(1);
						boolFoundStatHeader = true;
						System.out.println("  **  WARNING: found duplicate stat_header record with id " + strStatHeaderIdDup + "\n        " + strFileLine);
					}
					stmt.close();
				}
				intStatHeaderSearchTime = (new java.util.Date()).getTime() - intStatHeaderSearchBegin;
				_intStatHeaderSearchTime += intStatHeaderSearchTime;
				
				if( !boolFoundStatHeader ){
					//  add the value list to the values list
					listInsertValues.add("(" + intStatHeaderId + ", " + strStatHeaderValueList + ")");
					intStatHeaderRecords++;
				}
			}	

			
			/*
			 * * * *  line_data insert  * * * *
			 */			
			
			//  build a value list for the line data insert
			String strLineType = listToken[20];
			int[][] listLineTypeInfo = (int[][])_tableLineType.get(strLineType);
			int intNumLineDataFields = listLineTypeInfo[1][0];
			boolean boolHasStatGroups = _tableStatGroupIndices.containsKey(strLineType);
			boolean boolHasThreshGroups = _tableThreshGroupIndices.containsKey(strLineType);
			int[][] listStatGroupIndices = null;
			int[] listThreshGroupIndices = null;
			int[] listLineDataIndices = null;
			
			String strLineDataValueList = "" +
					intLineDataId + ", " +				//  line_data_id
					intStatHeaderId + ", " +			//  stat_header_id
					info._dataFileId + ", " +			//  data_file_id
					intLine + ", " +					//  line_num
					listToken[21];						//  total
			
			//  if the line data requires a cov_thresh value, add it
			String strCovThresh = listToken[18];
			if( _tableCovThreshLineTypes.containsKey(strLineType) ){
				if( strCovThresh.equals("NA") ){ System.out.println("  **  WARNING: cov_thresh value NA with line type '" + strLineType + "'\n        " + strFileLine); }
				strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
			} else if( !strCovThresh.equals("NA") ){
				System.out.println("  **  WARNING: unexpected cov_thresh value '" + strCovThresh + "' with line type '" + strLineType + "'\n        " + strFileLine);
			}

			//  if the line data requires an alpha value, add it
			String strAlpha = listToken[19];
			if( _tableAlphaLineTypes.containsKey(strLineType) ){
				if( strAlpha.equals("NA") ){ System.out.println("  **  WARNING: alpha value NA with line type '" + strLineType + "'\n        " + strFileLine); }
				strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
			} else if( !strAlpha.equals("NA") ){
				System.out.println("  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + strLineType + "'\n        " + strFileLine);
			}
			strLineDataValueList += (0 < intNumLineDataFields? ", " : "");
			
			//  build the list of token indices that will be added to the line_data insert
			if( boolHasStatGroups ){
				listStatGroupIndices = (int[][])_tableStatGroupIndices.get(strLineType);
				listLineDataIndices = listStatGroupIndices[0];
			} else {
				listLineDataIndices = new int[intNumLineDataFields];
				for(int i=0; i < intNumLineDataFields; i++){ listLineDataIndices[i] = 22 + i; } 
			}			
			
			//  add the appropriate fields for the line data insert
			for(int i=0; i < listLineDataIndices.length; i++){
				strLineDataValueList += (0 == i? "" : ", ") + replaceInvalidValues(listToken[ listLineDataIndices[i] ]);
			}
			
			//  add the values list to the line type values map
			ArrayList listLineTypeValues = new ArrayList();
			if( tableLineDataValues.containsKey(strLineType) ){ listLineTypeValues = (ArrayList)tableLineDataValues.get(strLineType); }
			listLineTypeValues.add("(" + strLineDataValueList + ")");
			tableLineDataValues.put(strLineType, listLineTypeValues);
			
			
			/*
			 * * * *  stat_group insert  * * * *
			 */
			
			if( boolHasStatGroups ){
				int[] listStatGroupLuIdBoth = listLineTypeInfo[2];
				int[] listStatGroupLuIdBoot = listLineTypeInfo[3];
				int[] listStatGroupLuIdNorm = listLineTypeInfo[4];		
				int[] listStatGroupLuIdNone = listLineTypeInfo[5];		
				int[] listStatGroupLuIdAll = new int[ listStatGroupLuIdBoth.length + 
				                                      listStatGroupLuIdBoot.length +
				                                      listStatGroupLuIdNorm.length +
				                                      listStatGroupLuIdNone.length ];
				
				//  build the list of all stat_group_lu_ids
				int intStatGroupLuIdIndex = 0;
				for(int i=0; i < listStatGroupLuIdBoth.length; i++){ listStatGroupLuIdAll[intStatGroupLuIdIndex++] = listStatGroupLuIdBoth[i]; }
				for(int i=0; i < listStatGroupLuIdBoot.length; i++){ listStatGroupLuIdAll[intStatGroupLuIdIndex++] = listStatGroupLuIdBoot[i]; }
				for(int i=0; i < listStatGroupLuIdNorm.length; i++){ listStatGroupLuIdAll[intStatGroupLuIdIndex++] = listStatGroupLuIdNorm[i]; }
				for(int i=0; i < listStatGroupLuIdNone.length; i++){ listStatGroupLuIdAll[intStatGroupLuIdIndex++] = listStatGroupLuIdNone[i]; }
				
				//  initialize the token indices for the stat group
				int intStatGroupBothIndex = 0;
				int intStatGroupBootIndex = 0;
				int intStatGroupNormIndex = 0;
				int intStatGroupNoneIndex = 0;

				//  for each stat group, build an insert statment
				for(int i=0; i < listStatGroupLuIdAll.length; i++){
					int intStatGroupLuId = listStatGroupLuIdAll[i];
					String strStatGroupInsertValues = "" + intStatGroupLuId + ", " +
													  intStatHeaderId + ", " +
													  intLineDataId + ", ";
					
					if( contains(listStatGroupLuIdBoth, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[1][intStatGroupBothIndex++];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 3]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 4]);
					}
					else if( contains(listStatGroupLuIdBoot, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[2][intStatGroupBootIndex++];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + 
													", 0, 0, " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]);
					}
					else if( contains(listStatGroupLuIdNorm, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[3][intStatGroupNormIndex++];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]) + 
													", 0, 0";
					}
					else {
						int intTokenIndex = listStatGroupIndices[4][intStatGroupNoneIndex++];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", 0, 0, 0, 0"; 
					}
					listStatGroupInsertValues.add("(" + strStatGroupInsertValues + ")");
				}
				intStatGroupRecords++;
			}
			
			
			/*
			 * * * *  thresh_group insert  * * * *
			 */
			
			if( boolHasThreshGroups ){
				
				//  get the index information about the current line type
				listThreshGroupIndices = (int[])_tableThreshGroupIndices.get(strLineType);
				int intGroupIndex = listThreshGroupIndices[0];
				int intGroupSize = listThreshGroupIndices[1];
				int intNumGroups = Integer.parseInt(listToken[22]) - 1;
				ArrayList listThreshValues = (ArrayList)tableThreshValues.get(strLineType);
			
				//  build a insert value statement for each threshold group
				for(int i=0; i < intNumGroups; i++){
					String strThreshValues = "(" + intLineDataId + ", " + (i+1);
					for(int j=0; j < intGroupSize; j++){
						strThreshValues += ", " + replaceInvalidValues( listToken[intGroupIndex++] );
					}
					strThreshValues += ")";
					listThreshValues.add(strThreshValues);
					intThreshRecords++;
				}
				tableThreshValues.put(strLineType, listThreshValues);				
			}
			
			
			/*
			 * * * *  stat_header commit  * * * * 
			 */			

			//  if the insert flag it true, insert all of the line_data values and stat_group values 
			if( _intInsertSize <= listInsertValues.size() || (0 < listInsertValues.size() && !reader.ready()) ){
				
				//  build and execute the stat header insert statement, if the list length has reached the insert size or the end of the file
				String strValueList = "";
				for(int i=0; i < listInsertValues.size(); i++){
					strValueList += (0 < i? ", " : "") + listInsertValues.get(i).toString(); 
				}
				String strStatHeaderInsert = "INSERT INTO stat_header VALUES " + strValueList + ";";
				try{
					int intResStatHeaderInsert = executeUpdate(con, strStatHeaderInsert);
					if( listInsertValues.size() != intResStatHeaderInsert ){
						System.out.println("  **  WARNING: unexpected result from stat_header INSERT: " + intResStatHeaderInsert + "\n        " + strFileLine);
					}
				}catch(Exception e){
					throw e;
				}
				listInsertValues.clear();
				intStatHeaderInserts++;

				/*
				 * * * *  line_data commit  * * * * 
				 */
				
				//  for each line type, build an insert statement with the appropriate list of values
				for(Iterator iterEntries = tableLineDataValues.entrySet().iterator(); iterEntries.hasNext();){
					Map.Entry entry = (Map.Entry)iterEntries.next();
					strLineType = entry.getKey().toString();
					ArrayList listValues = (ArrayList)entry.getValue();
					
					//  build the list of value lists for this line type
					strValueList = "";
					for(int i=0; i < listValues.size(); i++){
						strValueList += (0 < i? ", " : "") + listValues.get(i).toString();
					}
				
					//  build and execute the line data insert statement
					String strLineDataTable = "line_data_" + strLineType.toLowerCase();
					String strLineDataInsert = "INSERT INTO " + strLineDataTable + " VALUES " + strValueList + ";";	
					int intResLineDataInsert = executeUpdate(con, strLineDataInsert);
					if( listValues.size() != intResLineDataInsert ){ System.out.println("  **  WARNING: unexpected result from line_data INSERT: " + 
																						intResLineDataInsert + "\n        " + strFileLine); }
					intLineDataInserts++;
				}
				tableLineDataValues.clear();
			
				
				/*
				 * * * *  stat_group commit  * * * * 
				 */

				//  build a stat_group insert with all stored values
				if( 0 < listStatGroupInsertValues.size() ){
					String strStatGroupInsertValues = "";
					for(int i=0; i < listStatGroupInsertValues.size(); i++){
						strStatGroupInsertValues += (i == 0? "" : ", ") + listStatGroupInsertValues.get(i).toString();		
					}
					String strStatGroupInsert = "INSERT INTO stat_group VALUES " + strStatGroupInsertValues + ";";
					int intStatGroupInsert = executeUpdate(con, strStatGroupInsert);
					if( listStatGroupInsertValues.size() != intStatGroupInsert ){
						System.out.println("  **  WARNING: unexpected result from stat_group INSERT: " + intStatGroupInsert + " vs. " + 
											listStatGroupInsertValues.size() + "\n        " + strFileLine);
					}
					intStatGroupInserts++;
				}
				listStatGroupInsertValues.clear();
				
				/*
				 * * * *  thresh_group commit  * * * * 
				 */
				
				//  insert probabilistic data into the thresh tables
				String[] listThreshTypes = (String[])tableThreshValues.keySet().toArray(new String[]{});
				for(int i=0; i < listThreshTypes.length; i++){
					String[] listThreshValues = (String[])((ArrayList)tableThreshValues.get(listThreshTypes[i])).toArray(new String[]{});
					if( 1 > listThreshValues.length ){ continue; }
					String strThreshInsert = "INSERT INTO line_data_" + listThreshTypes[i].toLowerCase() + "_thresh VALUES ";
					for(int j=0; j < listThreshValues.length; j++){
						strThreshInsert += (0 < j? ", " : "") + listThreshValues[j];
					}
					int intThreshInsert = executeUpdate(con, strThreshInsert);
					if( listThreshValues.length != intThreshInsert ){
						System.out.println("  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " + 
										   listThreshValues.length + "\n        " + strFileLine);
					}
					intThreshInserts++;
					tableThreshValues.put(listThreshTypes[i], new ArrayList());
				}
			}
			
			intLineDataId++;
			intLineDataRecords++;
			intLine++;
		}
		reader.close();
		_tableStatHeaders.clear();
		_tableStatHeaders = new Hashtable();
		
		_intStatLinesTotal += (intLine - 1);
		_intStatHeaderRecords += intStatHeaderRecords;
		_intStatHeaderInserts += intStatHeaderInserts;
		_intLineDataInserts += intLineDataInserts;
		_intLineDataRecords += intLineDataRecords;
		_intStatGroupRecords += intStatGroupRecords;
		_intStatGroupInserts += intStatGroupInserts;
		_intThreshRecords += intThreshRecords;
		_intThreshInserts += intThreshInserts;
		
		//  print a performance report
		long intStatHeaderLoadTime = (new java.util.Date()).getTime() - intStatHeaderLoadStart;
		double dblLinesPerMSec = (double)(intLine - 1) / (double)(intStatHeaderLoadTime);
		
		if( _boolVerbose ){
			System.out.println(padBegin("stat_header records: ", 36) + intStatHeaderRecords + "\n" +
							   padBegin("stat_header inserts: ", 36) + intStatHeaderInserts + "\n" +
							   padBegin("line_data records: ", 36) + intLineDataRecords + "\n" +
							   padBegin("line_data inserts: ", 36) + intLineDataInserts + "\n" +
							   padBegin("stat_group records: ", 36) + intStatGroupRecords + "\n" +
							   padBegin("stat_group inserts: ", 36) + intStatGroupInserts + "\n" +
							   padBegin("thresh records: ", 36) + intThreshRecords + "\n" +
							   padBegin("thresh inserts: ", 36) + intThreshInserts + "\n" +
							   padBegin("total load time: ", 36) + formatTimeSpan(intStatHeaderLoadTime) + "\n" +
							   (_boolStatHeaderDBCheck? padBegin("stat_header search time: ", 36) + formatTimeSpan(intStatHeaderSearchTime) + "\n": "") +
							   padBegin("lines / msec: ", 36) + _formatPerf.format(dblLinesPerMSec) + "\n\n");
		}
	}
	
	//  line_type_lu_id values for the various mode line types
	public static final int MODE_CTS		= 19;
	public static final int MODE_SINGLE		= 17;
	public static final int MODE_PAIR		= 18;
	
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
			else { throw new Exception("loadModeFile() unable to determine line type " + listToken[16] + "\n        " + strFileLine); }
			
			
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
	
	public static int executeUpdate(Connection con, String update) throws SQLException{
		Statement stmt = con.createStatement();
		int intRes = stmt.executeUpdate(update);
		stmt.close();
		return intRes;
	}
	
    public static String replaceInvalidValues(String strData){ return strData.replace("NA", "-9999").replace("nan", "-9999"); }
	
	public static int getNextId(Connection con, String table, String field) throws Exception {
		int intId = -1;
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery("SELECT MAX(" + field + ") FROM " + table + ";");
		if( !res.next() ){ throw new Exception("getNextId(" + table + ", " + field + ") unable to find max id"); }
		String strId = res.getString(1);
		if( null == strId ) { intId = 0; } 
		else                { intId = (Integer.parseInt(strId) + 1); }
		stmt.close();
		return intId;
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
		"line_data_isc"
	};

	public static int getNextLineDataId(Connection con) throws Exception{
		int intMaxId = 0;
		for(int i=0; i < _listLineDataTables.length; i++){
			int intMaxIdCur = getNextId(con, _listLineDataTables[i], "line_data_id");
			if( intMaxIdCur > intMaxId ){ intMaxId = intMaxIdCur; }			
		}
		return intMaxId;
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
		else if( strFile.matches("^mode_\\S+_obj\\.txt$") )	{ strDataFileLuTypeName = "mode_obj";     }
		else if( strFile.matches("^mode_\\S+_cts\\.txt$") )	{ strDataFileLuTypeName = "mode_cts";     }
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
		if( !res.next() ){ throw new Exception("processDataFile() unable to find max data_file_id"); }		
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
	}

	public static Hashtable _tableCovThreshLineTypes = new Hashtable();
	static{
		_tableCovThreshLineTypes.put("NBRCTC", new Boolean(true));
		_tableCovThreshLineTypes.put("NBRCTS", new Boolean(true));
	}
	
	public static final Hashtable _tableIndexes = new Hashtable();
	static{
		/*
		_tableIndexes.put("stat_header_model_idx",			"model");
		_tableIndexes.put("stat_header_fcst_var_idx",		"fcst_var");
		_tableIndexes.put("stat_header_fcst_lev_idx",		"fcst_lev");
		_tableIndexes.put("stat_header_fcst_lead_idx",		"fcst_lead");
		_tableIndexes.put("stat_header_vx_mask_idx",		"vx_mask");
		_tableIndexes.put("stat_header_interp_mthd_idx",	"interp_mthd");
		_tableIndexes.put("stat_header_fcst_init_beg_idx",	"fcst_init_beg");
		_tableIndexes.put("stat_header_fcst_valid_beg_idx",	"fcst_valid_beg");
		*/
		_tableIndexes.put("mode_header_model_idx",			"model");
		_tableIndexes.put("mode_header_fcst_var_idx",		"fcst_var");
		_tableIndexes.put("mode_header_fcst_lev_idx",		"fcst_lev");
		_tableIndexes.put("mode_header_fcst_rad_idx",		"fcst_rad");
	}
	
	public static void applyIndexes(Connection con, boolean drop) throws Exception{
		
		System.out.println("    ==== indexes ====\n\n" + (drop? "  dropping..." : ""));
		for(Iterator iterEntries = _tableIndexes.entrySet().iterator(); iterEntries.hasNext();){
			Map.Entry entry = (Map.Entry)iterEntries.next();
			String strIndexName = entry.getKey().toString();
			String strField = entry.getValue().toString();
			long intIndexStart = (new java.util.Date()).getTime();
			
			//  build a create index statment and run it
			String strTable = "";
			if( strIndexName.startsWith("stat") ) { strTable = "stat_header"; }
			else                                  { strTable = "mode_header"; }
			String strIndex = "";
			if( drop ){ strIndex = "DROP INDEX " + strIndexName + " ON " + strTable + " ;";                     }
			else      { strIndex = "CREATE INDEX " + strIndexName + " ON " + strTable + " (" + strField + ");"; }
			executeUpdate(con, strIndex);
			
			//  print out a performance message
			long intIndexTime = (new java.util.Date()).getTime() - intIndexStart;
			System.out.println(padBegin( strIndexName + ": ", 36) + formatTimeSpan(intIndexTime));
		}
		System.out.println();
	}
	public static void applyIndexes(Connection con) throws Exception{ applyIndexes(con, false); }
	public static void dropIndexes(Connection con) throws Exception{ applyIndexes(con, true); }
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

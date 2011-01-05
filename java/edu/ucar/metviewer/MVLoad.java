package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class MVLoad extends MVUtil {

	public static String _strMetVersion				= "V2.0";

	public static boolean _boolVerbose				= false;
	public static int _intInsertSize				= 1;
	public static boolean _boolStatHeaderTableCheck	= true;
	public static boolean _boolStatHeaderDBCheck	= false;
	public static boolean _boolModeHeaderDBCheck	= false;
	public static boolean _boolDropIndexes			= false;
	public static boolean _boolApplyIndexes			= false;
	public static boolean _boolIndexOnly			= false;

	public static boolean _boolLineTypeLoad			= false;
	public static Hashtable _tableLineTypeLoad		= new Hashtable();

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
	public static int _intThreshRecords				= 0;
	public static int _intThreshInserts				= 0;
	public static int _intNumModeFiles				= 0;
	public static int _intModeLinesTotal			= 0;
	public static int _intModeHeaderRecords			= 0;
	public static int _intModeCtsRecords			= 0;
	public static int _intModeObjSingleRecords		= 0;
	public static int _intModeObjPairRecords		= 0;
	
	public static final Hashtable _tableDataFileLU_v2 = new Hashtable();
	static {
		_tableDataFileLU_v2.put("point_stat",	"0");
		_tableDataFileLU_v2.put("grid_stat",	"1");
		_tableDataFileLU_v2.put("mode_cts",		"2");
		_tableDataFileLU_v2.put("mode_obj",		"3");
		_tableDataFileLU_v2.put("wavelet_stat",	"4");
	}
	public static final Hashtable _tableDataFileLU_v3 = new Hashtable();
	static {
		_tableDataFileLU_v3.put("point_stat",	"0");
		_tableDataFileLU_v3.put("grid_stat",	"1");
		_tableDataFileLU_v3.put("mode_cts",		"2");
		_tableDataFileLU_v3.put("mode_obj",		"3");
		_tableDataFileLU_v3.put("wavelet_stat",	"4");
		_tableDataFileLU_v3.put("ensemble_stat","5");
	}
	public static Hashtable _tableDataFileLU = _tableDataFileLU_v2;
	
	/*
	 * line type format: 
	 *   - line_type_lu_id
	 *   - # line data fields (redundant for lines with stat groups)
	 *   - (optional) list of stat group field lookup ids (WARNING: indexes must be listed in increasing order)
	 *     - with both normal & bootstrap confidence interval
	 *     - only bootstrap confidence interval
	 *     - only normal confidence interval
	 *     - no confidence intervals
	 */
	public static final Hashtable _tableLineType_v2 = new Hashtable();
	static {
		_tableLineType_v2.put("FHO",	new int[][]{ new int[]{0},	new int[]{3},	new int[]{} });
		_tableLineType_v2.put("CTC",	new int[][]{ new int[]{1},	new int[]{4},	new int[]{} });
		_tableLineType_v2.put("CTS",	new int[][]{ new int[]{2},	new int[]{0},	new int[]{0, 1, 2, 4, 5, 6, 7, 8, 10, 12}, new int[]{3, 9, 11}, new int[]{}, new int[]{} });
		_tableLineType_v2.put("CNT",	new int[][]{ new int[]{3},	new int[]{5},	new int[]{13, 14, 15, 16, 17, 18, 19}, new int[]{20, 21, 22, 23, 24, 25, 26, 27, 28, 29}, new int[]{}, new int[]{} });
		_tableLineType_v2.put("PCT",	new int[][]{ new int[]{4},	new int[]{1},	new int[]{} });
		_tableLineType_v2.put("PSTD",	new int[][]{ new int[]{5},	new int[]{1},	new int[]{}, new int[]{}, new int[]{30}, new int[]{31, 32, 33, 34} });
		_tableLineType_v2.put("PJC",	new int[][]{ new int[]{6},	new int[]{1},	new int[]{} });
		_tableLineType_v2.put("PRC",	new int[][]{ new int[]{7},	new int[]{1},	new int[]{} });
		_tableLineType_v2.put("SL1L2",	new int[][]{ new int[]{8},	new int[]{5},	new int[]{} });
		_tableLineType_v2.put("SAL1L2",	new int[][]{ new int[]{9},	new int[]{5},	new int[]{} });
		_tableLineType_v2.put("VL1L2",	new int[][]{ new int[]{10},	new int[]{7},	new int[]{} });
		_tableLineType_v2.put("VAL1L2",	new int[][]{ new int[]{11},	new int[]{7},	new int[]{} });
		_tableLineType_v2.put("MPR",	new int[][]{ new int[]{12},	new int[]{9},	new int[]{} });
		_tableLineType_v2.put("NBRCTC",	new int[][]{ new int[]{13},	new int[]{4},	new int[]{} });
		_tableLineType_v2.put("NBRCTS",	new int[][]{ new int[]{14},	new int[]{0},	new int[]{35, 36, 37, 39, 40, 41, 42, 43, 45, 47}, new int[]{38, 44, 46}, new int[]{},	new int[]{} });
		_tableLineType_v2.put("NBRCNT",	new int[][]{ new int[]{15},	new int[]{0},	new int[]{}, new int[]{48, 49}, new int[]{}, new int[]{} });
		_tableLineType_v2.put("ISC",	new int[][]{ new int[]{16},	new int[]{10},	new int[]{} });
	}
	public static final Hashtable _tableLineType_v3 = new Hashtable();
	static {
		_tableLineType_v3.put("FHO",	new int[][]{ new int[]{0},	new int[]{3},	new int[]{} });
		_tableLineType_v3.put("CTC",	new int[][]{ new int[]{1},	new int[]{4},	new int[]{} });
		_tableLineType_v3.put("CTS",	new int[][]{ new int[]{2},	new int[]{0},	new int[]{0, 1, 2, 4, 5, 6, 7, 8, 10, 12}, new int[]{3, 9, 11}, new int[]{}, new int[]{} });
		_tableLineType_v3.put("CNT",	new int[][]{ new int[]{3},	new int[]{5},	new int[]{13, 14, 15, 16, 17, 18, 19}, new int[]{20, 21, 22, 23, 24, 25, 26, 27, 28, 29}, new int[]{}, new int[]{} });
		_tableLineType_v3.put("PCT",	new int[][]{ new int[]{4},	new int[]{1},	new int[]{} });
		_tableLineType_v3.put("PSTD",	new int[][]{ new int[]{5},	new int[]{1},	new int[]{}, new int[]{}, new int[]{30, 50}, new int[]{31, 32, 33, 34} });
		_tableLineType_v3.put("PJC",	new int[][]{ new int[]{6},	new int[]{1},	new int[]{} });
		_tableLineType_v3.put("PRC",	new int[][]{ new int[]{7},	new int[]{1},	new int[]{} });
		_tableLineType_v3.put("SL1L2",	new int[][]{ new int[]{8},	new int[]{5},	new int[]{} });
		_tableLineType_v3.put("SAL1L2",	new int[][]{ new int[]{9},	new int[]{5},	new int[]{} });
		_tableLineType_v3.put("VL1L2",	new int[][]{ new int[]{10},	new int[]{7},	new int[]{} });
		_tableLineType_v3.put("VAL1L2",	new int[][]{ new int[]{11},	new int[]{7},	new int[]{} });
		_tableLineType_v3.put("MPR",	new int[][]{ new int[]{12},	new int[]{9},	new int[]{} });
		_tableLineType_v3.put("NBRCTC",	new int[][]{ new int[]{13},	new int[]{4},	new int[]{} });
		_tableLineType_v3.put("NBRCTS",	new int[][]{ new int[]{14},	new int[]{0},	new int[]{35, 36, 37, 39, 40, 41, 42, 43, 45, 47}, new int[]{38, 44, 46}, new int[]{}, new int[]{} });
		_tableLineType_v3.put("NBRCNT",	new int[][]{ new int[]{15},	new int[]{0},	new int[]{}, new int[]{48, 49}, new int[]{}, new int[]{} });
		_tableLineType_v3.put("ISC",	new int[][]{ new int[]{16},	new int[]{10},	new int[]{} });
		_tableLineType_v3.put("MCTC",	new int[][]{ new int[]{20},	new int[]{1},	new int[]{} });
		_tableLineType_v3.put("MCTS",	new int[][]{ new int[]{21},	new int[]{1},	new int[]{51}, new int[]{52, 53, 54}, new int[]{}, new int[]{} });
		_tableLineType_v3.put("RHIST",	new int[][]{ new int[]{22},	new int[]{1},	new int[]{}, new int[]{}, new int[]{}, new int[]{55, 56} });
		_tableLineType_v3.put("ORANK",	new int[][]{ new int[]{23},	new int[]{11},	new int[]{} });
	}
	public static Hashtable _tableLineType = _tableLineType_v2;
	

	/*
	 * stat group data indices for line data fields and stat group fields
	 *   - line data indices (single member)
	 *   - stat group indices with normal & bootstrap CIs (5 members)
	 *   - stat group indices with bootstrap CI only (3 members)
	 *   - stat group indices with normal CI only (3 members)
	 *   - stat group indices with no CIs (1 member)
	 */
	public static final Hashtable _tableStatGroupIndices_v2 = new Hashtable(); 
	static {			
		_tableStatGroupIndices_v2.put("CTS",	new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v2.put("CNT",	new int[][]{ new int[]{47, 48, 49, 50, 51}, new int[]{22, 27, 32, 37, 42, 52, 57}, new int[]{62, 65, 68, 71, 74, 77, 80, 83, 86, 89}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v2.put("PSTD",	new int[][]{ new int[]{22}, new int[]{}, new int[]{}, new int[]{27}, new int[]{23, 24, 25, 26} });
		_tableStatGroupIndices_v2.put("NBRCTS",	new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v2.put("NBRCNT",	new int[][]{ new int[]{}, new int[]{}, new int[]{22, 25}, new int[]{}, new int[]{} });
	}
	public static final Hashtable _tableStatGroupIndices_v3 = new Hashtable(); 
	static {			
		_tableStatGroupIndices_v3.put("CTS",	new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v3.put("CNT",	new int[][]{ new int[]{47, 48, 49, 50, 51}, new int[]{22, 27, 32, 37, 42, 52, 57}, new int[]{62, 65, 68, 71, 74, 77, 80, 83, 86, 89}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v3.put("PSTD",	new int[][]{ new int[]{22}, new int[]{}, new int[]{}, new int[]{23, 30}, new int[]{26, 27, 28, 29} });
		_tableStatGroupIndices_v3.put("NBRCTS",	new int[][]{ new int[]{}, new int[]{22, 27, 32, 40, 45, 50, 55, 60, 68, 76}, new int[]{37, 65, 73}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v3.put("NBRCNT",	new int[][]{ new int[]{}, new int[]{}, new int[]{22, 25}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v3.put("MCTS",	new int[][]{ new int[]{22}, new int[]{23}, new int[]{28, 31, 34}, new int[]{}, new int[]{} });
		_tableStatGroupIndices_v3.put("RHIST",	new int[][]{ new int[]{24}, new int[]{}, new int[]{}, new int[]{}, new int[]{22, 23} });
	}
	public static Hashtable _tableStatGroupIndices = _tableStatGroupIndices_v2; 

	/*
	 * variable length group data indices for lines with an arbitrary number of fields
	 *   - index of field containing number of sets
	 *   - index of first repeating field(s)
	 *   - number of fields in each repeating set
	 */
	public static final Hashtable _tableVarLengthTable_v2 = new Hashtable();
	static {			
		_tableVarLengthTable_v2.put("PCT",   "line_data_pct_thresh");
		_tableVarLengthTable_v2.put("PSTD",  "line_data_pstd_thresh");
		_tableVarLengthTable_v2.put("PJC",   "line_data_pjc_thresh");
		_tableVarLengthTable_v2.put("PRC",   "line_data_prc_thresh");
	}
	public static final Hashtable _tableVarLengthTable_v3 = new Hashtable();
	static {			
		_tableVarLengthTable_v3.put("PCT",   "line_data_pct_thresh");
		_tableVarLengthTable_v3.put("PSTD",  "line_data_pstd_thresh");
		_tableVarLengthTable_v3.put("PJC",   "line_data_pjc_thresh");
		_tableVarLengthTable_v3.put("PRC",   "line_data_prc_thresh");
		_tableVarLengthTable_v3.put("MCTC",  "line_data_mctc_cnt");
		_tableVarLengthTable_v3.put("RHIST", "line_data_rhist_rank");
		_tableVarLengthTable_v3.put("ORANK", "line_data_orank_ens");
	}
	public static Hashtable _tableVarLengthTable = _tableVarLengthTable_v3;
	
	public static final Hashtable _tableVarLengthGroupIndices_v2 = new Hashtable();
	static {			
		_tableVarLengthGroupIndices_v2.put("PCT",  new int[]{22, 23, 3});
		_tableVarLengthGroupIndices_v2.put("PSTD", new int[]{22, 30, 1});
		_tableVarLengthGroupIndices_v2.put("PJC",	new int[]{22, 23, 7});
		_tableVarLengthGroupIndices_v2.put("PRC",  new int[]{22, 23, 3});
	}
	public static final Hashtable _tableVarLengthGroupIndices_v3 = new Hashtable();
	static {			
		_tableVarLengthGroupIndices_v3.put("PCT",  new int[]{22, 23, 3});
		_tableVarLengthGroupIndices_v3.put("PSTD", new int[]{22, 33, 1});
		_tableVarLengthGroupIndices_v3.put("PJC",	new int[]{22, 23, 7});
		_tableVarLengthGroupIndices_v3.put("PRC",  new int[]{22, 23, 3});
		_tableVarLengthGroupIndices_v3.put("MCTC", new int[]{22, 23, 1});
		_tableVarLengthGroupIndices_v3.put("RHIST", new int[]{24, 25, 1});
		_tableVarLengthGroupIndices_v3.put("ORANK", new int[]{32, 33, 1});
	}
	public static Hashtable _tableVarLengthGroupIndices = _tableVarLengthGroupIndices_v2;
	


	public static final String[] _listLineDataTables_v2 = {
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
	public static final String[] _listLineDataTables_v3 = {
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
		"line_data_rhist",
		"line_data_orank"
	};
	public static String[] _listLineDataTables = _listLineDataTables_v2;
		
	public static void main(String[] argv) {
		System.out.println("----  MVLoad  ----\n");
		Connection con = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
		
			//  parse the input arguments
			if( 1 > argv.length || 2 < argv.length ){
				System.out.println(getUsage() + "\n\n----  MVLoad Done  ----");
				return;
			}
			if( 2 == argv.length ){
				if( argv[1].equalsIgnoreCase("-index") ){ _boolIndexOnly = true; }
				else{
					System.out.println("ERROR: unrecognized argument \"" + argv[1] + "\"\n" + getUsage() + "\n\n----  MVLoad Done  ----");
					return;
				}
			}
			
			//  parse the plot job
			System.out.println("Begin time: " + format.format(new java.util.Date()) + "\n" +
							   "Parsing: " + argv[0] + "\n" +
							   (_boolIndexOnly? "Applying Index Settings Only\n" : ""));
			MVLoadJobParser parser = new MVLoadJobParser(argv[0]);
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
			_boolStatHeaderTableCheck	= job.getStatHeaderTableCheck();
			_boolStatHeaderDBCheck		= job.getStatHeaderDBCheck();
			_boolModeHeaderDBCheck		= job.getModeHeaderDBCheck();
			_boolDropIndexes			= job.getDropIndexes();
			_boolApplyIndexes			= job.getApplyIndexes();
			
			_boolLineTypeLoad			= job.getLineTypeLoad();
			_tableLineTypeLoad			= job.getLineTypeLoadMap();
			
			//  set the database schema structure information according to the MET version
			if( _strMetVersion.equals("V2.0") ){
				_tableDataFileLU            = _tableDataFileLU_v2;
				_tableLineType              = _tableLineType_v2;
				_tableStatGroupIndices      = _tableStatGroupIndices_v2; 
				_tableVarLengthTable        = _tableVarLengthTable_v2;
				_tableVarLengthGroupIndices = _tableVarLengthGroupIndices_v2;
				_listLineDataTables			= _listLineDataTables_v2;
			} else if( _strMetVersion.equals("V3.0") ){
				_tableDataFileLU            = _tableDataFileLU_v3;
				_tableLineType              = _tableLineType_v3;
				_tableStatGroupIndices      = _tableStatGroupIndices_v3; 
				_tableVarLengthTable        = _tableVarLengthTable_v3;
				_tableVarLengthGroupIndices = _tableVarLengthGroupIndices_v3;
				_listLineDataTables			= _listLineDataTables_v3;
			} else {
				throw new Exception("ERROR: MET output version " + _strMetVersion + " not supported");
			}

						
			//  if the insert size is greater than 1, ensure that the db header check is off
			if( 1 < _intInsertSize && _boolStatHeaderDBCheck ){
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
			
			//  if there are <load_file> files specified, load them
			String[] listLoadFiles = job.getLoadFiles();
			if( !_boolIndexOnly && 0 < listLoadFiles.length ){				
				for(int i=0; i < listLoadFiles.length; i++){
					try{
						processFile(new File(listLoadFiles[i]), con);
					}catch(Exception e){
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
							System.out.println("  **  ERROR: caught " + e.getClass() + " in processFile(): " + e.getMessage() + "\n" + 
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
				"          load_spec_file\n" +
				"          [-index]\n" +
				"\n" +
				"          where   \"load_spec_file\" specifies the XML load specification document\n" +
				"                  \"-index\" indicates that no data should be loaded, and only the indexing commands applied\n";
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
		System.out.println("  " + info._dataFilePath + "/" + info._dataFileFilename + 
							(_boolVerbose? "\n" + padBegin("data file time: ", 36) + formatTimeSpan(intProcessDataFileTime) : ""));
		
		if( info._dataFileLuTypeName.equals("point_stat")   || info._dataFileLuTypeName.equals("grid_stat") ||
			info._dataFileLuTypeName.equals("wavelet_stat") || info._dataFileLuTypeName.equals("ensemble_stat") ){
			loadStatFile(info, con);
			_intNumStatFiles++;
		} else if( info._dataFileLuTypeName.equals("mode_obj") || info._dataFileLuTypeName.equals("mode_cts") ){
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

		//  data structures for storing value strings
		MVLoadStatInsertData d = new MVLoadStatInsertData();
		d._con = con;		
		String[] listVarLengthKeys = (String[])_tableVarLengthGroupIndices.keySet().toArray(new String[]{});
		for(int i=0; i < listVarLengthKeys.length; i++){
			d._tableVarLengthValues.put(listVarLengthKeys[i], new ArrayList());
		}

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
			
			//  error if the version number does not match the configured value
			String strMetVersion = listToken[0];
			if( !strMetVersion.equals(_strMetVersion) ){
				throw new Exception("file MET version " + strMetVersion + " does not match configured value " + _strMetVersion);
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
					strInterpPnts + ", " +				//  interp_pnts
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
					"  AND interp_pnts = " +		strInterpPnts + "\n" +
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
				
				//  look for an existing stat_header record with the same information
				long intStatHeaderSearchBegin = (new java.util.Date()).getTime();
				if( _boolStatHeaderDBCheck ){
					String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
					Statement stmt = con.createStatement();
					ResultSet res = stmt.executeQuery(strStatHeaderSelect);
					if( res.next() ){
						intStatHeaderId = res.getInt(1);
						System.out.println("  **  WARNING: found duplicate stat_header record with id " + intStatHeaderId + "\n        " + d._strFileLine);
					}
					stmt.close();
				}
				intStatHeaderSearchTime = (new java.util.Date()).getTime() - intStatHeaderSearchBegin;
				_intStatHeaderSearchTime += intStatHeaderSearchTime;
				
				//  if not present in the table or database, add a stat_header record with a new stat_header_id
				if( -1 == intStatHeaderId ){
					intStatHeaderId = intStatHeaderIdNext++;
					d._listInsertValues.add("(" + intStatHeaderId + ", " + strStatHeaderValueList + ")");
					intStatHeaderRecords++;
				}
				_tableStatHeaders.put(strStatHeaderValueList, new Integer(intStatHeaderId));
			}	

			
			/*
			 * * * *  line_data insert  * * * *
			 */			
			
			//  build a value list for the line data insert
			int[][] listLineTypeInfo = (int[][])_tableLineType.get(d._strLineType);
			int intNumLineDataFields = listLineTypeInfo[1][0];
			boolean boolHasStatGroups = _tableStatGroupIndices.containsKey(d._strLineType);
			boolean boolHasVarLengthGroups = _tableVarLengthGroupIndices.containsKey(d._strLineType);
			int[][] listStatGroupIndices = null;
			int[] listVarLengthGroupIndices = null;
			int[] listLineDataIndices = null;
			
			String strLineDataValueList = "" +
					intLineDataId + ", " +				//  line_data_id
					intStatHeaderId + ", " +			//  stat_header_id
					info._dataFileId + ", " +			//  data_file_id
					intLine + ", " +					//  line_num
					listToken[21];						//  total
			
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
			strLineDataValueList += (0 < intNumLineDataFields? ", " : "");
			
			//  build the list of token indices that will be added to the line_data insert
			if( boolHasStatGroups ){
				listStatGroupIndices = (int[][])_tableStatGroupIndices.get(d._strLineType);
				listLineDataIndices = listStatGroupIndices[0];
			} else {
				listLineDataIndices = new int[intNumLineDataFields];
				for(int i=0; i < intNumLineDataFields; i++){ listLineDataIndices[i] = 22 + i; } 
			}			
			
			//  add the appropriate fields for the line data insert
			for(int i=0; i < listLineDataIndices.length; i++){
				strLineDataValueList += (0 == i? "" : ", ") + "'" + replaceInvalidValues(listToken[ listLineDataIndices[i] ]) + "'";
			}
			
			//  add the values list to the line type values map
			ArrayList listLineTypeValues = new ArrayList();
			if( d._tableLineDataValues.containsKey(d._strLineType) ){ listLineTypeValues = (ArrayList)d._tableLineDataValues.get(d._strLineType); }
			listLineTypeValues.add("(" + strLineDataValueList + ")");
			d._tableLineDataValues.put(d._strLineType, listLineTypeValues);
			
			
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
					String strStatValue = "";
					
					if( -1 < Arrays.binarySearch(listStatGroupLuIdBoth, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[1][intStatGroupBothIndex++];
						strStatValue = listToken[intTokenIndex];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 3]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 4]);
					}
					else if( -1 < Arrays.binarySearch(listStatGroupLuIdBoot, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[2][intStatGroupBootIndex++];
						strStatValue = listToken[intTokenIndex];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + 
													", 0, 0, " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]);
					}
					else if( -1 < Arrays.binarySearch(listStatGroupLuIdNorm, intStatGroupLuId) ){
						int intTokenIndex = listStatGroupIndices[3][intStatGroupNormIndex++];
						strStatValue = listToken[intTokenIndex];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 1]) + ", " + 
													replaceInvalidValues(listToken[intTokenIndex + 2]) + 
													", 0, 0";
					}
					else {
						int intTokenIndex = listStatGroupIndices[4][intStatGroupNoneIndex++];
						strStatValue = listToken[intTokenIndex];
						strStatGroupInsertValues += replaceInvalidValues(listToken[intTokenIndex]) + ", 0, 0, 0, 0"; 
					}
					
					//  if the stat value is legit, add it to the insert
					if( !strStatValue.equalsIgnoreCase("NA") && !strStatValue.equalsIgnoreCase("nan") ){
						d._listStatGroupInsertValues.add("(" + strStatGroupInsertValues + ")");
						intStatGroupRecords++;
					}
				}
			}
			
			
			/*
			 * * * *  thresh_group insert  * * * *
			 */
			
			if( boolHasVarLengthGroups ){
				
				//  get the index information about the current line type
				listVarLengthGroupIndices = (int[])_tableVarLengthGroupIndices.get(d._strLineType);
				int intGroupCntIndex = listVarLengthGroupIndices[0];
				int intGroupIndex = listVarLengthGroupIndices[1];
				int intGroupSize = listVarLengthGroupIndices[2];
				int intNumGroups = Integer.parseInt(listToken[intGroupCntIndex]);
				if( d._strLineType.equals("PCT") || d._strLineType.equals("PJC") || d._strLineType.equals("PRC") ){
					intNumGroups -= 1;
				}
				ArrayList listThreshValues = (ArrayList)d._tableVarLengthValues.get(d._strLineType);
			
				//  build a insert value statement for each threshold group
				if( d._strLineType.equals("MCTC") ){
					for(int i=0; i < intNumGroups; i++){
						for(int j=0; j < intNumGroups; j++){
							listThreshValues.add("(" + intLineDataId + ", " + (i+1) + ", " + (j+1) + ", " + 
												 replaceInvalidValues( listToken[intGroupIndex++] ) + ")");
							intThreshRecords++;
						}
					}
				} else {
					for(int i=0; i < intNumGroups; i++){
						String strThreshValues = "(" + intLineDataId + ", " + (i+1);
						for(int j=0; j < intGroupSize; j++){
							strThreshValues += ", " + replaceInvalidValues( listToken[intGroupIndex++] );
						}
						strThreshValues += ")";
						listThreshValues.add(strThreshValues);
						intThreshRecords++;
					}
				}
				d._tableVarLengthValues.put(d._strLineType, listThreshValues);				
			}
						
			//  if the insert threshhold has been reached, commit the stored data to the database 
			if( _intInsertSize <= d._listInsertValues.size() ){				
				int[] listInserts		= commitStatData(d);
				intStatHeaderInserts	+= listInserts[INDEX_STAT_HEADERS];
				intLineDataInserts		+= listInserts[INDEX_LINE_DATA];
				intStatGroupInserts		+= listInserts[INDEX_STAT_GROUP];
				intThreshInserts		+= listInserts[INDEX_THRESH];				
			}
			
			intLineDataId++;
			intLineDataRecords++;
			intLine++;

		}  // end: while( reader.ready() )
		
		//  commit all the remaining stored data
		int[] listInserts		= commitStatData(d);
		intStatHeaderInserts	+= listInserts[INDEX_STAT_HEADERS];
		intLineDataInserts		+= listInserts[INDEX_LINE_DATA];
		intStatGroupInserts		+= listInserts[INDEX_STAT_GROUP];
		intThreshInserts		+= listInserts[INDEX_THRESH];
		
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
	public static final int INDEX_THRESH		= 3;
	
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
				listInserts[INDEX_THRESH]++; //  intThreshInserts++;
			}
			int intThreshInsert = executeUpdate(d._con, strThreshInsert);
			if( listVarLengthValues.length != intThreshInsert ){
				System.out.println("  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " + 
								   listVarLengthValues.length + "\n        " + d._strFileLine);
			}
			// listInserts[INDEX_THRESH]++; //  intThreshInserts++;
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
	public static int executeUpdate(Connection con, String update) throws SQLException{
		Statement stmt = con.createStatement();
		int intRes = stmt.executeUpdate(update);
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
	
	public static final MVOrderedMap _mapIndexes = new MVOrderedMap();
	static{
		_mapIndexes.put("#stat_header#_model_idx",				"model");
		_mapIndexes.put("#stat_header#_fcst_lead_idx",			"fcst_lead");
		_mapIndexes.put("#stat_header#_fcst_valid_beg_idx",		"fcst_valid_beg");
		_mapIndexes.put("#stat_header#_fcst_init_beg_idx",		"fcst_init_beg");
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
		
		//_mapIndexes.put("#line_data_rhist_rank#_i_value_idx",	"i_value");
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

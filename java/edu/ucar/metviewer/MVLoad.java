package edu.ucar.metviewer;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MVLoad extends MVUtil {

  public static final DecimalFormat _formatPerf = new DecimalFormat("0.000");
  public static final Pattern _patModeSingle = Pattern.compile("^(C?[FO]\\d{3})$");
  public static final Pattern _patModePair = Pattern.compile("^(C?F\\d{3})_(C?O\\d{3})$");
  protected static final Map _tableStatHeaders = new HashMap<>(1024);
  protected static final Map _tableModeHeaders = new HashMap<>(1024);
  public static final long _intStatHeaderTableTime = 0;
  public static final int _intStatGroupRecords = 0;
  public static final int _intStatGroupInserts = 0;
  /*
   * data_file_lu_id values for each MET output type
   */
  protected static final Map<String, Integer> _tableDataFileLU = new HashMap<>();
  /*
   * variable length table names for each variable length output line type
   */
  protected static final Map<String, String> _tableVarLengthTable = new HashMap<>();
  /*
   * variable length group data indices for lines with an arbitrary number of fields
   *   - index of field containing number of sets
   *   - index of first repeating field(s)
   *   - number of fields in each repeating set
   */
  protected static final Map _tableVarLengthGroupIndices = new HashMap<>();
  protected static final Map<String, Integer> _tableVarLengthLineDataId = new HashMap<>();
  public static final int INDEX_LINE_DATA = 1;
  public static final int INDEX_STAT_GROUP = 2;
  public static final int INDEX_VAR_LENGTH = 3;
  //  line_type_lu_id values for the various mode line types
  public static final int MODE_CTS = 19;
  public static final int MODE_SINGLE = 17;
  public static final int MODE_PAIR = 18;
  protected static final Map<String, Boolean> _tableAlphaLineTypes = new HashMap<>();
  protected static final Map<String, Boolean> _tableCovThreshLineTypes = new HashMap<>();
  protected static final String[] _listLineDataTables = {
    "line_data_fho", "line_data_ctc", "line_data_cts", "line_data_cnt", "line_data_pct",
    "line_data_pstd", "line_data_pjc", "line_data_prc", "line_data_sl1l2", "line_data_sal1l2",
    "line_data_vl1l2", "line_data_val1l2", "line_data_mpr", "line_data_nbrctc", "line_data_nbrcts",
    "line_data_nbrcnt", "line_data_isc", "line_data_mctc", "line_data_rhist", "line_data_orank",
    "line_data_ssvar", "line_data_enscnt"
  };
  protected static final MVOrderedMap _mapIndexes = new MVOrderedMap();
  public static final Pattern _patIndexName = Pattern.compile("#([\\w\\d]+)#([\\w\\d]+)");
  public static boolean _boolVerbose = false;
  public static int _intInsertSize = 1;
  public static boolean _boolModeHeaderDBCheck = false;
  public static boolean _boolStatHeaderDBCheck = false;
  public static boolean _boolDropIndexes = false;
  public static boolean _boolApplyIndexes = false;
  public static boolean _boolIndexOnly = false;
  public static boolean _boolLineTypeLoad = false;
  protected static Map _tableLineTypeLoad = new HashMap<>();
  public static boolean _boolLoadStat = true;
  public static boolean _boolLoadMode = true;
  public static boolean _boolLoadMpr = false;
  public static boolean _boolLoadOrank = false;
  public static boolean _boolForceDupFile = false;
  public static long _intStatHeaderSearchTime = 0;
  public static long _intModeHeaderSearchTime = 0;
  public static int _intNumStatFiles = 0;
  public static int _intStatLinesTotal = 0;
  public static int _intStatHeaderRecords = 0;
  public static int _intStatHeaderInserts = 0;
  public static int _intLineDataRecords = 0;
  public static int _intLineDataInserts = 0;
  public static int _intVarLengthRecords = 0;
  public static int _intVarLengthInserts = 0;
  public static int _intNumModeFiles = 0;
  public static int _intModeLinesTotal = 0;
  public static int _intModeHeaderRecords = 0;
  public static int _intModeCtsRecords = 0;
  public static int _intModeObjSingleRecords = 0;
  public static int _intModeObjPairRecords = 0;
  private static BoneCP connectionPool;

  static {
    _tableDataFileLU.put("point_stat", 0);
    _tableDataFileLU.put("grid_stat", 1);
    _tableDataFileLU.put("mode_cts", 2);
    _tableDataFileLU.put("mode_obj", 3);
    _tableDataFileLU.put("wavelet_stat", 4);
    _tableDataFileLU.put("ensemble_stat", 5);
    _tableDataFileLU.put("vsdb_point_stat", 6);
    _tableDataFileLU.put("stat", 7);
  }

  static {
    _tableVarLengthTable.put("PCT", "line_data_pct_thresh");
    _tableVarLengthTable.put("PSTD", "line_data_pstd_thresh");
    _tableVarLengthTable.put("PJC", "line_data_pjc_thresh");
    _tableVarLengthTable.put("PRC", "line_data_prc_thresh");
    _tableVarLengthTable.put("MCTC", "line_data_mctc_cnt");
    _tableVarLengthTable.put("RHIST", "line_data_rhist_rank");
    _tableVarLengthTable.put("PHIST", "line_data_phist_bin");
    _tableVarLengthTable.put("ORANK", "line_data_orank_ens");
  }

  static {
    _tableVarLengthGroupIndices.put("PCT", new int[]{22, 23, 3});
    _tableVarLengthGroupIndices.put("PSTD", new int[]{22, 33, 1});
    _tableVarLengthGroupIndices.put("PJC", new int[]{22, 23, 7});
    _tableVarLengthGroupIndices.put("PRC", new int[]{22, 23, 3});
    _tableVarLengthGroupIndices.put("MCTC", new int[]{22, 23, 1});
    _tableVarLengthGroupIndices.put("RHIST", new int[]{24, 25, 1});
    _tableVarLengthGroupIndices.put("PHIST", new int[]{23, 24, 1});
    _tableVarLengthGroupIndices.put("ORANK", new int[]{32, 33, 1});
  }

  static {
    _tableAlphaLineTypes.put("CTS", Boolean.TRUE);
    _tableAlphaLineTypes.put("CNT", Boolean.TRUE);
    _tableAlphaLineTypes.put("PSTD", Boolean.TRUE);
    _tableAlphaLineTypes.put("NBRCTS", Boolean.TRUE);
    _tableAlphaLineTypes.put("NBRCNT", Boolean.TRUE);
    _tableAlphaLineTypes.put("MCTS", Boolean.TRUE);
    _tableAlphaLineTypes.put("SSVAR", Boolean.TRUE);
  }

  static {
    _tableCovThreshLineTypes.put("NBRCTC", Boolean.TRUE);
    _tableCovThreshLineTypes.put("NBRCTS", Boolean.TRUE);
  }

  static {
    _mapIndexes.put("#stat_header#_model_idx", "model");
    _mapIndexes.put("#stat_header#_fcst_var_idx", "fcst_var");
    _mapIndexes.put("#stat_header#_fcst_lev_idx", "fcst_lev");
    _mapIndexes.put("#stat_header#_obtype_idx", "obtype");
    _mapIndexes.put("#stat_header#_vx_mask_idx", "vx_mask");
    _mapIndexes.put("#stat_header#_interp_mthd_idx", "interp_mthd");
    _mapIndexes.put("#stat_header#_interp_pnts_idx", "interp_pnts");
    _mapIndexes.put("#stat_header#_fcst_thresh_idx", "fcst_thresh");

    _mapIndexes.put("#mode_header#_model_idx", "model");
    _mapIndexes.put("#mode_header#_fcst_lead_idx", "fcst_lead");
    _mapIndexes.put("#mode_header#_fcst_valid_idx", "fcst_valid");
    _mapIndexes.put("#mode_header#_fcst_init_idx", "fcst_init");
    _mapIndexes.put("#mode_header#_fcst_rad_idx", "fcst_rad");
    _mapIndexes.put("#mode_header#_fcst_thr_idx", "fcst_thr");
    _mapIndexes.put("#mode_header#_fcst_var_idx", "fcst_var");
    _mapIndexes.put("#mode_header#_fcst_lev_idx", "fcst_lev");

    for (int i = 0; i < _listLineDataTables.length; i++) {
      _mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_lead_idx", "fcst_lead");
      _mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_valid_beg_idx", "fcst_valid_beg");
      _mapIndexes.put("#" + _listLineDataTables[i] + "#_fcst_init_beg_idx", "fcst_init_beg");
    }
  }

  public static void main(String[] argv) {
    System.out.println("----  MVLoad  ----\n");
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    try {

      String strXML;

      //  parse the input arguments
      if (1 > argv.length || 2 < argv.length) {
        System.out.println(getUsage() + "\n\n----  MVLoad Done  ----");
        return;
      }
      if (2 == argv.length) {
        if (argv[0].equalsIgnoreCase("-index")) {
          _boolIndexOnly = true;
        } else {
          System.out.println("ERROR: unrecognized argument \"" + argv[1] + "\"\n" + getUsage() + "\n\n----  MVLoad Done  ----");
          return;
        }
        strXML = argv[1];
      } else {
        strXML = argv[0];
      }

      //  parse the plot job
      System.out.println("Begin time: " + format.format(new Date()) + "\n" +
        "Parsing: " + strXML + "\n" +
        (_boolIndexOnly ? "Applying Index Settings Only\n" : ""));
      MVLoadJobParser parser = new MVLoadJobParser(strXML);
      MVLoadJob job = parser.getLoadJob();


      Class.forName(job.getDBDriver());

      // setup the connection pool
      BoneCPConfig config = new BoneCPConfig();
      config.setJdbcUrl("jdbc:" + job.getDBManagementSystem() + "://" + job.getDBHost() + "/" + job.getDBName() + "?rewriteBatchedStatements=true"); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
      config.setUsername(job.getDBUser());
      config.setPassword(job.getDBPassword());
      config.setMinConnectionsPerPartition(2);
      config.setMaxConnectionsPerPartition(3);
      config.setPartitionCount(1);
      config.setIdleConnectionTestPeriodInSeconds(1);
      config.setIdleMaxAgeInSeconds(240);
      config.setStatementsCacheSize(100);
      config.setReleaseHelperThreads(3);

      connectionPool = new BoneCP(config); // setup the connection pool

      //  process the elements of the job
      System.out.println("Database Connection:\n" +
        "      db: " + job.getDBManagementSystem() + "\n" +
        "      host: " + job.getDBHost() + "\n" +
        "  database: " + job.getDBName() + "\n" +
        "      user: " + job.getDBUser() + "\n" +
        "  password: " + job.getDBPassword() + "\n");


      _boolVerbose = job.getVerbose();
      _intInsertSize = job.getInsertSize();
      _boolModeHeaderDBCheck = job.getModeHeaderDBCheck();
      _boolStatHeaderDBCheck = job.getStatHeaderDBCheck();
      _boolDropIndexes = job.getDropIndexes();
      _boolApplyIndexes = job.getApplyIndexes();

      _boolLineTypeLoad = job.getLineTypeLoad();
      _tableLineTypeLoad = job.getLineTypeLoadMap();

      _boolLoadStat = job.getLoadStat();
      _boolLoadMode = job.getLoadMode();

      _boolLoadMpr = job.getLoadMpr();
      _boolLoadOrank = job.getLoadOrank();

      _boolForceDupFile = job.getForceDupFile();

      //  process the instance_info load information
      boolean boolLoadNote = !job.getLoadNote().equals("");


      //  if the insert size is greater than 1, ensure that the db header check is off
      if (1 < _intInsertSize) {
        throw new Exception("METViewer load error: insert size (" + _intInsertSize + ") > 1 and database header check turned on");
      }

      long intLoadTimeStart = new Date().getTime();

      //  drop the database indexes, if requested
      if (_boolDropIndexes) {
        dropIndexes();
      }

      //  if the job involves only applying indexes, do so and return
      if (_boolIndexOnly && _boolApplyIndexes) {
        applyIndexes();
        System.out.println("\n----  MVLoad Done  ----");
        return;
      }

      //  initialize the line_data_id table
      initVarLengthLineDataIds();

      //  if there are <load_file> files specified, load them
      String[] listLoadFiles = job.getLoadFiles();
      File file;
      if (!_boolIndexOnly && listLoadFiles != null) {
        for (int i = 0; i < listLoadFiles.length; i++) {
          try {
            file = new File(listLoadFiles[i]);
            processFile(file);
          } catch (Exception e) {
            System.out.println("  **  ERROR: caught " + e.getClass() + " loading file " + listLoadFiles[i] + ": " + e.getMessage());
            e.printStackTrace();
            System.out.println("  **  WARNING: error(s) encountered loading file " + listLoadFiles[i] + " - skipping file");
          }
        }
      }

      //  if there is a file template specified, load it
      if (!_boolIndexOnly && null != job.getFolderTmpl() && !job.getFolderTmpl().equals("")) {
        int intStatLinesPrev = 0;
        int intModeLinesPrev = 0;

        //  build a folder with each permutation of load values and load the data therein
        MVOrderedMap[] listPerm = permute(job.getLoadVal()).getRows();
        String strBaseFolder;
        long intPermStart;
        File fileBaseFolder;
        File[] listDataFiles;
        for (int intPerm = 0; intPerm < listPerm.length; intPerm++) {

          //  determine the name of the current folder
          strBaseFolder = buildTemplateString(job.getFolderTmpl(), listPerm[intPerm]);
          System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " - " + strBaseFolder /* + "\n" + listPerm[intPerm].getRDecl() */);
          intPermStart = new Date().getTime();

          //  try to access the folder and its contents, and continue if it does not exist
          fileBaseFolder = new File(strBaseFolder);
          if (!fileBaseFolder.exists()) {
            continue;
          }

          //  process each fine in the folder
          listDataFiles = fileBaseFolder.listFiles();
          if (listDataFiles != null) {
            for (int j = 0; j < listDataFiles.length; j++) {
              try {
                processFile(listDataFiles[j]);
              } catch (Exception e) {
                System.out.println("  **  ERROR: caught " + e.getClass() + " in processFile()\n" +
                  e.getMessage() + "\n" +
                  "  **  WARNING: error(s) encountered loading file " + listDataFiles[j] + " - skipping file");
                e.printStackTrace();
              }
            }
          }
          _tableModeHeaders.clear();

          //  bookkeeping
          int intStatLinesPerm = _intStatLinesTotal - intStatLinesPrev;
          int intModeLinesPerm = _intModeLinesTotal - intModeLinesPrev;
          intStatLinesPrev = _intStatLinesTotal;
          intModeLinesPrev = _intModeLinesTotal;
          System.out.println("Permutation " + (intPerm + 1) + " of " + listPerm.length + " complete - insert time: " +
            formatTimeSpan(new Date().getTime() - intPermStart) + "  stat lines: " + intStatLinesPerm +
            "  mode lines: " + intModeLinesPerm + "\n");
        }
      }

      //  print a performance report
      long intLoadTime = new Date().getTime() - intLoadTimeStart;
      double dblLinesPerMSec = (double) _intStatLinesTotal / (double) (intLoadTime);
      if (!_boolIndexOnly) {
        System.out.println("\n    ==== grid_stat ====\n\n" +

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
          (_boolModeHeaderDBCheck ? padBegin("mode_header search time total: ", 36) + formatTimeSpan(_intModeHeaderSearchTime) + "\n" : "") +
          (_boolStatHeaderDBCheck ? padBegin("stat_header search time total: ", 36) + formatTimeSpan(_intStatHeaderSearchTime) + "\n" : "") +
          padBegin("mode_header inserts: ", 36) + _intModeHeaderRecords + "\n" +
          padBegin("mode_cts inserts: ", 36) + _intModeCtsRecords + "\n" +
          padBegin("mode_obj_single inserts: ", 36) + _intModeObjSingleRecords + "\n" +
          padBegin("mode_obj_pair inserts: ", 36) + _intModeObjPairRecords + "\n" +
          padBegin("total lines: ", 36) + _intModeLinesTotal + "\n" +
          padBegin("num files: ", 36) + _intNumModeFiles + "\n");
      }

      //  apply the indexes, if requested
      if (_boolApplyIndexes) {
        applyIndexes();
      }

      //  update the instance_info table, if requested
      if (boolLoadNote) {

        //  get the instance_info information to insert
        int intInstInfoIdNext = getNextId("instance_info", "instance_info_id");
        String strUpdater = "";
        try {
          strUpdater = sysCmd();
        } catch (Exception e) {
          try {
            strUpdater = sysCmd();
          } catch (Exception e2) {
          }
        }
        strUpdater = strUpdater.trim();
        SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strUpdateDate = formatDB.format(new Date());
        String strUpdateDetail = job.getLoadNote();

        //  read the load xml into a string, if requested
        String strLoadXML = "";
        if (job.getLoadXML()) {
          try (BufferedReader reader = new BufferedReader(new FileReader(strXML))) {
            while (reader.ready()) {
              strLoadXML += reader.readLine().trim();
            }
            reader.close();
          }
        }

        //  construct an update statement for instance_info
        String strInstInfoSQL =
          "INSERT INTO instance_info VALUES (" +
            "'" + intInstInfoIdNext + "', " +
            "'" + strUpdater + "', " +
            "'" + strUpdateDate + "', " +
            "'" + strUpdateDetail + "', " +
            "'" + strLoadXML + "'" +
            ");";

        //  execute the insert SQL
        System.out.print("Inserting instance_info record...  ");
        int intInsert = executeUpdate(strInstInfoSQL);
        if (1 != intInsert) {
          throw new Exception("unexpected number of instance_info rows inserted: " + intInsert);
        }
        System.out.println("Done\n");
      }

      System.out.println("End time: " + format.format(new Date()) + "\n" +
        "Load total: " + formatTimeSpan(intLoadTime) + "\n");
    } catch (Exception e) {
      System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("\n----  MVLoad Done  ----");
  }

  public static String getUsage() {
    return "Usage:  mv_load\n" +
      "          [-index]\n" +
      "          load_spec_file\n" +
      "\n" +
      "          where   \"-index\" indicates that no data should be loaded, and only the indexing commands applied\n" +
      "                  \"load_spec_file\" specifies the XML load specification document\n";
  }

  /**
   * Attempt to load the input file into the database data_file table, and then, if successful, into the appropriate set of tables: stat or mode.
   *
   * @param file File to process //* @param con Connection to the database to load
   * @throws Exception
   */
  public static void processFile(File file) throws Exception {
    long intProcessDataFileBegin = new Date().getTime();
    DataFileInfo info = processDataFile(file);
    if (null == info) {
      return;
    }
    long intProcessDataFileTime = new Date().getTime() - intProcessDataFileBegin;
    String strFileMsg = "  " + info._dataFilePath + "/" + info._dataFileFilename +
      (_boolVerbose ? "\n" + padBegin("data file time: ", 36) + formatTimeSpan(intProcessDataFileTime) : "");

    if (info._dataFileLuTypeName.equals("stat") && _boolLoadStat) {
      System.out.println(strFileMsg);
      loadStatFile(info);
      _intNumStatFiles++;
    } else if ((info._dataFileLuTypeName.equals("mode_obj") || info._dataFileLuTypeName.equals("mode_cts")) &&
      _boolLoadMode) {
      System.out.println(strFileMsg);
      loadModeFile(info);
      _intNumModeFiles++;
    } else if (info._dataFileLuTypeName.equals("vsdb_point_stat") && _boolLoadStat) {
      System.out.println(strFileMsg);
      loadStatFileVSDB(info);
      _intNumStatFiles++;
    }
  }

  public static void loadStatFileVSDB(DataFileInfo info) throws Exception {

    //  initialize the insert data structure
    MVLoadStatInsertData d = new MVLoadStatInsertData();

    //  performance counters
    long intStatHeaderLoadStart = new Date().getTime();
    long intStatHeaderSearchTime = 0;
    int intStatHeaderRecords = 0;
    int intStatHeaderInserts = 0;
    int intLineDataRecords = 0;
    int intLineDataInserts = 0;
    int intLineDataSkipped = 0;
    int intVarLengthRecords = 0;
    int intVarLengthInserts = 0;

    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
    String ensValue = "";
    String[] ensValueArr = info._dataFilePath.split("\\/");
    if (ensValueArr[ensValueArr.length - 1].contains("_")) {
      String[] ensValue1 = ensValueArr[ensValueArr.length - 1].split("_");
      ensValue = "_" + ensValue1[ensValue1.length - 1];
    }

    int intLine = 0;
    try (FileReader fileReader = new FileReader(strFilename); BufferedReader reader = new BufferedReader(fileReader)) {
      List<String> allMatches;
      SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
      formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat formatStatVsdb = new SimpleDateFormat("yyyyMMddHH", Locale.US);
      formatStatVsdb.setTimeZone(TimeZone.getTimeZone("UTC"));

      //  read in each line of the input file, remove "="
      while (reader.ready()) {

        String line = reader.readLine();
        try {
          line = line.replaceAll("\\s=\\s", " "); // remove " = "
          Matcher m = Pattern.compile("\\d-0\\.").matcher(line); // some records do not have a space between columns if the value in column starts with "-"

          allMatches = new ArrayList<>();
          while (m.find()) {
            allMatches.add(m.group());
          }
          for (String match : allMatches) {
            String newStr = match.replace("-", " -");
            line = line.replace(match, newStr);
          }

          String[] listToken = line.split("\\s+");
          intLine++;
          String thresh = "NA";
          String modelName = listToken[1];

          if (listToken[6].equals("BSS") || listToken[6].equals("ECON") || listToken[6].equals("HIST")
            || listToken[6].equals("RELI") || listToken[6].equals("RELP") || listToken[6].equals("RMSE") || listToken[6].equals("RPS")) {
            modelName = modelName.split("\\/")[0] + ensValue;
          }

          //  if the line type load selector is activated, check that the current line type is on the list

          if (listToken[6].equals("RMSE")) {
            d._strLineType = "CNT";
          } else if (listToken[6].equals("BSS")) {
            d._strLineType = "PSTD";
          } else if (listToken[6].equals("HIST")) {
            d._strLineType = "RHIST";
          } else if (listToken[6].equals("SL1L2")) {
            d._strLineType = "SL1L2";
          } else if (listToken[6].equals("SAL1L2")) {
            d._strLineType = "SAL1L2";
          } else if (listToken[6].equals("VL1L2")) {
            d._strLineType = "VL1L2";
          } else if (listToken[6].equals("VAL1L2")) {
            d._strLineType = "VAL1L2";
          } else if (listToken[6].equals("RPS")) {
            d._strLineType = "ENSCNT";
          } else if (listToken[6].equals("RELI")) {
            d._strLineType = "PCT";
          } else if (listToken[6].startsWith("FHO")) {
            d._strLineType = "CTC";
            String[] threshArr = listToken[6].split("FHO");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else if (listToken[6].startsWith("FSS")) {
            d._strLineType = "NBRCNT";
            String[] threshArr = listToken[6].split("FSS");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else {
            continue;
          }
          if (_boolLineTypeLoad && !_tableLineTypeLoad.containsKey(d._strLineType)) {
            continue;
          }

          d._strFileLine = strFilename + ":" + intLine;

          //  parse the valid times


          Date dateFcstValidBeg = formatStatVsdb.parse(listToken[3]);

          //  format the valid times for the database insert
          String strFcstValidBeg = formatDB.format(dateFcstValidBeg);


          //  calculate the number of seconds corresponding to fcst_lead
          String strFcstLead = listToken[2];
          int intFcstLeadSec = Integer.parseInt(strFcstLead) * 3600;

          //  determine the init time by combining fcst_valid_beg and fcst_lead
          Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
          calFcstInitBeg.setTime(dateFcstValidBeg);
          calFcstInitBeg.add(Calendar.SECOND, (-1) * intFcstLeadSec);
          Date dateFcstInitBeg = calFcstInitBeg.getTime();
          String strFcstInitBeg = formatDB.format(dateFcstInitBeg);
          String strObsValidBeg = formatDB.format(dateFcstValidBeg);
          String strFcstValidEnd = formatDB.format(dateFcstValidBeg);
          String strObsValidEnd = formatDB.format(dateFcstValidBeg);

          //  ensure that the interp_pnts field value is a reasonable integer
          String strInterpPnts = "0";

          String strLineType = d._strLineType;



			/*
       * * * *  stat_header insert  * * * *
			 */
          intStatHeaderRecords++;

          //  build the stat_header value list for this line
          String[] listStatHeaderValue = {
            listToken[0],    //  version
            modelName,      //  model
            "NA",           //  descr
            listToken[7],    //  fcst_var
            listToken[8],    //  fcst_lev
            listToken[7],    //  obs_var
            listToken[8],    //  obs_lev
            listToken[4],    //  obtype
            listToken[5],    //  vx_mask
            "NA",    //  interp_mthd
            strInterpPnts,    //  interp_pnts
            thresh,    //  fcst_thresh
            thresh    //  obs_thresh
          };

          //  build a where clause for searching for duplicate stat_header records
          String strStatHeaderWhereClause =
            "  model = '" + modelName + "'\n" +
            "  AND descr = '" + "NA" + "'\n" +
              "  AND fcst_var = '" + listToken[7] + "'\n" +
              "  AND fcst_lev = '" + listToken[8] + "'\n" +
              "  AND obtype = '" + listToken[4] + "'\n" +
              "  AND vx_mask = '" + listToken[5] + "'\n" +
              "  AND interp_mthd = '" + "NA" + "'\n" +
              "  AND interp_pnts = " + strInterpPnts + "\n" +
              "  AND fcst_thresh = '" + thresh + "'\n" +
              "  AND obs_thresh = '" + thresh + "'";

          //  build the value list for the stat_header insert
          String strStatHeaderValueList = "";
          for (int i = 0; i < listStatHeaderValue.length; i++) {
            strStatHeaderValueList += (0 < i ? ", " : "") + "'" + listStatHeaderValue[i] + "'";
          }


          String strFileLine = strFilename + ":" + intLine;

          //  look for the header key in the table
          int intStatHeaderId = -1;
          if (_tableStatHeaders.containsKey(strStatHeaderValueList)) {
            intStatHeaderId = (Integer) _tableStatHeaders.get(strStatHeaderValueList);
          }

          //  if the stat_header does not yet exist, create one
          else {

            //  look for an existing stat_header record with the same information
            boolean boolFoundStatHeader = false;
            long intStatHeaderSearchBegin = new Date().getTime();
            if (_boolStatHeaderDBCheck) {
              String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
              Connection con = null;
              Statement stmt = null;
              ResultSet res = null;
              try {
                con = connectionPool.getConnection();
                stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
                res = stmt.executeQuery(strStatHeaderSelect);
                if (res.next()) {
                  String strStatHeaderIdDup = res.getString(1);
                  intStatHeaderId = Integer.parseInt(strStatHeaderIdDup);
                  boolFoundStatHeader = true;
                }
              } catch (Exception e) {
                System.out.println(e.getMessage());
              } finally {
                try {
                  res.close();
                } catch (Exception e) { /* ignored */ }
                try {
                  stmt.close();
                } catch (Exception e) { /* ignored */ }
                try {
                  con.close();
                } catch (Exception e) { /* ignored */ }
              }
            }
            intStatHeaderSearchTime = new Date().getTime() - intStatHeaderSearchBegin;
            _intStatHeaderSearchTime += intStatHeaderSearchTime;

            //  if the stat_header was not found, add it to the table
            if (!boolFoundStatHeader) {

              intStatHeaderId = intStatHeaderIdNext++;
              _tableStatHeaders.put(strStatHeaderValueList, intStatHeaderId);

              //  build an insert statement for the mode header
              strStatHeaderValueList = Integer.toString(intStatHeaderId) + ", " +        //  stat_header_id
                strStatHeaderValueList;

              //  insert the record into the stat_header database table
              String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + strStatHeaderValueList + ");";

              int intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
              if (1 != intStatHeaderInsert) {
                System.out.println("  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
              }
              intStatHeaderInserts++;
            } else {
              _tableStatHeaders.put(strStatHeaderValueList, intStatHeaderId);
            }
          }


			/*
       * * * *  line_data insert  * * * *
			 */

          //
          String strLineDataId = "";
          intLineDataRecords++;

          //  if the line type is of variable length, get the line_data_id
          boolean boolHasVarLengthGroups = _tableVarLengthGroupIndices.containsKey(d._strLineType);

          //  determine the maximum token index for the data
          if (boolHasVarLengthGroups) {
            int intLineDataId = _tableVarLengthLineDataId.get(strLineType);
            strLineDataId = Integer.toString(intLineDataId) + ", ";
            _tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
          }

          //  build the value list for the insert statment
          String strLineDataValueList =
            strLineDataId +            //  line_data_id (if present)
              intStatHeaderId + ", " +      //  stat_header_id
              info._dataFileId + ", " +      //  data_file_id
              intLine + ", " +          //  line_num
              strFcstLead + ", " +        //  fcst_lead
              "'" + strFcstValidBeg + "', " +    //  fcst_valid_beg
              "'" + strFcstValidEnd + "', " +    //  fcst_valid_end
              "'" + strFcstInitBeg + "', " +    //  fcst_init_beg
              "000000" + ", " +        //  obs_lead
              "'" + strObsValidBeg + "', " +    //  obs_valid_beg
              "'" + strObsValidEnd + "'";      //  obs_valid_end

          //  if the line data requires a cov_thresh value, add it
          String strCovThresh = "NA";
          if (_tableCovThreshLineTypes.containsKey(d._strLineType)) {
            if (strCovThresh.equals("NA")) {
              System.out.println("  **  WARNING: cov_thresh value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine);
            }
            strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
          }

          //  if the line data requires an alpha value, add it
          String strAlpha = "-9999";
          if (_tableAlphaLineTypes.containsKey(d._strLineType)) {
            if (strAlpha.equals("NA")) {
              System.out.println("  **  WARNING: alpha value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine);
            }
            strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
          }

          if (listToken[6].equals("RMSE")) {//CNT line type
            for (int i = 0; i < 89; i++) {
              if (i == 53) {
                strLineDataValueList += ", '" + listToken[10] + "'";
              } else if (i == 31) {
                strLineDataValueList += ", '" + listToken[11] + "'";
              } else if (i == 36) {
                strLineDataValueList += ", '" + listToken[9] + "'";
              } else if (i == 44) {
                strLineDataValueList += ", '" + listToken[12] + "'";
              } else if (i == 0 || i == 28 || i == 29 || i == 30) {//total,ranks, frank_ties, orank_ties
                strLineDataValueList += ", '0'";
              } else if (i == 77) {
//do nothing
              } else {
                strLineDataValueList += ", '-9999'";
              }
            }
          }


          if (listToken[6].equals("BSS")) {//PSTD line type
            for (int i = 0; i < 16; i++) {
              switch (i) {
                case 0:
                case 1:
                  strLineDataValueList += ", '0'";
                  break;
                case 2:
                case 3:
                case 8:
                case 10:
                case 11:
                case 4:
                case 13:
                case 14:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 5:
                  strLineDataValueList += ", '" + listToken[12] + "'";
                  break;
                case 6:
                  strLineDataValueList += ", '" + listToken[13] + "'";
                  break;
                case 7:
                  strLineDataValueList += ", '" + listToken[14] + "'";
                  break;
                case 9:
                  strLineDataValueList += ", '" + listToken[9] + "'";
                  break;
                case 12:
                  strLineDataValueList += ", '" + listToken[10] + "'";
                  break;
                case 15:
                  strLineDataValueList += ", '" + listToken[11] + "'";
                  break;
                default:
              }

            }
          }

          if (listToken[6].equals("RPS")) {//ENSCNT line type
            for (int i = 0; i < 30; i++) {
              switch (i) {
                case 0:
                  strLineDataValueList += ", '" + listToken[9] + "'";
                  break;
                case 1:
                case 2:
                case 3:
                case 4:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 5:
                  strLineDataValueList += ", '" + listToken[10] + "'";
                  break;
                case 6:
                case 7:
                case 8:
                case 9:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 10:
                  strLineDataValueList += ", '" + listToken[11] + "'";
                  break;
                case 11:
                case 12:
                case 13:
                case 14:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 15:
                  strLineDataValueList += ", '" + listToken[12] + "'";
                  break;
                case 16:
                case 17:
                case 18:
                case 19:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 20:
                  strLineDataValueList += ", '" + listToken[13] + "'";
                  break;
                case 21:
                case 22:
                case 23:
                case 24:
                  strLineDataValueList += ", '-9999'";
                  break;
                case 25:
                  strLineDataValueList += ", '" + listToken[14] + "'";
                  break;
                case 26:
                case 27:
                case 28:
                case 29:
                  strLineDataValueList += ", '-9999'";
                  break;
                default:

              }
            }

          }

          if (listToken[6].equals("HIST")) {//RHIST line type
            for (int i = 0; i < 5; i++) {
              if (i == 3) {
                int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                strLineDataValueList += ", '" + intGroupSize + "'";
              } else if (i == 0) {//total
                strLineDataValueList += ", '0'";
              } else {
                strLineDataValueList += ", '-9999'";
              }
            }
          }
          if (listToken[6].equals("RELI")) {//PCT line type
            int total = 0;
            int intGroupSize;
            int intGroupIndex = 9;
            try {
              intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
            } catch (Exception e) {
              intGroupSize = 0;
            }
            for (int i = 0; i < intGroupSize; i++) {
              Integer on;
              try {
                on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue();
                total = total + on;
              } catch (Exception e) {
              }
              intGroupIndex++;
            }


            strLineDataValueList += ", " + total + ", " + intGroupSize;
          }

          if (listToken[6].equals("SL1L2") || listToken[6].equals("SAL1L2")) {//SL1L2,SAL1L2 line types
            for (int i = 0; i < 7; i++) {
              if (i + 9 < listToken.length) {
                if (i == 0) {
                  strLineDataValueList += ", '" + (Double.valueOf(listToken[i + 9])).intValue() + "'";
                } else {
                  strLineDataValueList += ", '" + Double.valueOf(listToken[i + 9]) + "'";
                }

              } else {
                strLineDataValueList += ", '-9999'";
              }
            }
          }
          if (listToken[6].equals("VL1L2") || listToken[6].equals("VAL1L2")) {//VL1L2,VAL1L2 line type
            for (int i = 0; i < 8; i++) {
              if (i + 9 < listToken.length) {
                if (i == 0) {
                  strLineDataValueList += ", '" + (Double.valueOf(listToken[i + 9])).intValue() + "'";
                } else {
                  strLineDataValueList += ", '" + Double.valueOf(listToken[i + 9]) + "'";
                }
              } else {
                strLineDataValueList += ", '-9999'";
              }

            }
          }
          if (listToken[6].startsWith("FHO")) {//CTC line type

            double total = Double.parseDouble(listToken[9]);
            double f_rate = Double.parseDouble(listToken[10]);
            double h_rate = Double.parseDouble(listToken[11]);
            double o_rate;
            if (listToken.length > 12) {
              o_rate = Double.valueOf(listToken[12]);
            } else {
              o_rate = 0;
              System.out.println("Error");
            }

            double fy = total * f_rate;
            double fy_oy = total * h_rate;
            double oy = total * o_rate;
            double fy_on = fy - fy_oy;
            double fn_oy = oy - fy_oy;
            double fn_on = total - fy - oy + fy_oy;


            for (int i = 0; i < 5; i++) {
              if (i == 4) {
                strLineDataValueList += ", '" + Math.max(0, fn_on) + "'";
              } else if (i == 3) {
                strLineDataValueList += ", '" + Math.max(0, fn_oy) + "'";
              } else if (i == 2) {
                strLineDataValueList += ", '" + Math.max(0, fy_on) + "'";
              } else if (i == 1) {
                strLineDataValueList += ", '" + Math.max(0, fy_oy) + "'";
              } else if (i == 0) {//total,
                strLineDataValueList += ", '" + listToken[9] + "'";
              }

            }
          }
          if (listToken[6].startsWith("FSS")) {//NBRCNT line type
            for (int i = 0; i < 19; i++) {
              if (i == 0) {//total,
                strLineDataValueList += ", " + listToken[9];
              } else if (i == 1) {//fbs
                strLineDataValueList += ", " + listToken[10];
              } else {
                strLineDataValueList += ", '-9999'";
              }
            }
          }


          //  add the values list to the line type values map
          List<String> listLineTypeValues = new ArrayList<>();
          if (d._tableLineDataValues.containsKey(d._strLineType)) {
            listLineTypeValues = d._tableLineDataValues.get(d._strLineType);
          }
          listLineTypeValues.add("(" + strLineDataValueList + ")");
          d._tableLineDataValues.put(d._strLineType, listLineTypeValues);
          intLineDataInserts++;


			/*
       * * * *  var_length insert  * * * *
			 */

          if (boolHasVarLengthGroups) {
            //  get the index information about the current line type
            int intGroupIndex = 0;
            int intGroupSize = 0;
            int intNumGroups = 0;

            if (listToken[6].equals("HIST")) {//RHIST line type)
              intGroupIndex = 9;
              try {
                intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              } catch (Exception e) {
                intNumGroups = 0;
              }
              intGroupSize = 1;
            } else if (listToken[6].equals("RELI")) {//PCT line type)
              intGroupIndex = 9;
              try {
                intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              } catch (Exception e) {
                intGroupSize = 0;
              }
              intNumGroups = 2;
            }

            List<String> listThreshValues = d._tableVarLengthValues.get(d._strLineType);
            if (null == listThreshValues) {
              listThreshValues = new ArrayList<>();
            }

            //  build a insert value statement for each threshold group
            if (listToken[6].equals("HIST")) {
              for (int i = 0; i < intNumGroups; i++) {
                StringBuilder strThreshValues = new StringBuilder("(");
                strThreshValues.append(strLineDataId).append(i + 1);
                for (int j = 0; j < intGroupSize; j++) {
                  double res = Double.parseDouble(listToken[intGroupIndex++]);
                  if (res != -9999) {
                    strThreshValues.append(", ").append(res * 100);
                  }

                }
                strThreshValues.append(')');
                listThreshValues.add(strThreshValues.toString());
                intVarLengthRecords++;
              }
            } else if (listToken[6].equals("RELI")) {
              int total =0;
              for (int i = 0; i < intGroupSize; i++) {
                double thresh_i;
                if (intGroupSize > 1) {
                  thresh_i = (double) i / (double)(intGroupSize - 1);
                } else {
                  thresh_i = 0;
                }
                String strThreshValues = "(" + strLineDataId + (i + 1) + "," + thresh_i;
                Integer oy = null;
                Integer on = null;
                try{
                  oy = Double.valueOf(listToken[intGroupIndex]).intValue();
                  on = Double.valueOf(listToken[intGroupIndex +intGroupSize]).intValue() - oy;
                  strThreshValues += ", " + oy + ", " + on;
                  total = total + oy + on;
                }catch (Exception e){
                  strThreshValues += ", -9999,  -9999";
                }

                intGroupIndex++;
                strThreshValues += ")";
                listThreshValues.add(strThreshValues);
                intVarLengthRecords++;
              }
            }

            d._tableVarLengthValues.put(d._strLineType, listThreshValues);
          }

          //  if the insert threshhold has been reached, commit the stored data to the database
          if (_intInsertSize <= d._listInsertValues.size()) {
            int[] listInserts = commitStatData(d);
            intLineDataInserts += listInserts[INDEX_LINE_DATA];
            intVarLengthInserts += listInserts[INDEX_VAR_LENGTH];
          }
        } catch (Exception e) {
          System.out.println("ERROR: line:" + line + " has errors and would be ignored.");
          System.out.println(e.getMessage());
        }
      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(d);
    intLineDataInserts += listInserts[INDEX_LINE_DATA];
    intVarLengthInserts += listInserts[INDEX_VAR_LENGTH];


    _intStatLinesTotal += (intLine - 1);
    _intStatHeaderRecords += intStatHeaderRecords;
    _intStatHeaderInserts += intStatHeaderInserts;
    _intLineDataInserts += intLineDataInserts;
    _intLineDataRecords += intLineDataRecords;
    _intVarLengthRecords += intVarLengthRecords;
    _intVarLengthInserts += intVarLengthInserts;

    //  print a performance report
    long intStatHeaderLoadTime = new Date().getTime() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (_boolVerbose) {
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
    System.out.println("intLine " + intLine);

  }

  private static String findValueInArray(String[] listToken, List<String> headerNames, String header) {
    int pos = headerNames.indexOf(header);
    if (pos >= 0 && pos < listToken.length) {
      return listToken[pos];
    } else {
      return "NA";
    }
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the database underlying the input Connection. The header
   * information can be checked in two different ways: using a table for the current file (specified by _boolStatHeaderTableCheck) or by searching the
   * stat_header table for a duplicate (specified by _boolStatHeaderDBCheck).  Records in line_data tables, stat_group tables and line_data_thresh tables are
   * created from the data in the input file. If necessary, records in the stat_header table are created as well.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target database
   * @throws Exception
   */
  public static void loadStatFile(DataFileInfo info) throws Exception {

    //  initialize the insert data structure
    MVLoadStatInsertData d = new MVLoadStatInsertData();
    //  performance counters
    long intStatHeaderLoadStart = new Date().getTime();
    long intStatHeaderSearchTime = 0;
    int intStatHeaderRecords = 0;
    int intStatHeaderInserts = 0;
    int intLineDataRecords = 0;
    int intLineDataInserts = 0;
    int intLineDataSkipped = 0;
    int intVarLengthRecords = 0;
    int intVarLengthInserts = 0;

    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
    int intLine = 0;
    List<String> headerNames = new ArrayList<>();
    try (
      FileReader fileReader = new FileReader(strFilename);
      BufferedReader reader = new BufferedReader(fileReader)) {
      //  read in each line of the input file
      while (reader.ready()) {
        String[] listToken = reader.readLine().split("\\s+");
        intLine++;

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          continue;
        }

        //  error if the version number does not match the configured value
        String strMetVersion = findValueInArray(listToken, headerNames, "VERSION");

        //  update the var length tree with information for METv2.0, if necessary
        if ("V2.0".equals(strMetVersion)) {
          _tableVarLengthGroupIndices.put("PSTD", new int[]{22, 30, 1});
        }


        //  if the line type load selector is activated, check that the current line type is on the list
        d._strLineType = findValueInArray(listToken, headerNames, "LINE_TYPE");
        if (_boolLineTypeLoad && !_tableLineTypeLoad.containsKey(d._strLineType)) {
          continue;
        }

        d._strFileLine = strFilename + ":" + intLine;

        SimpleDateFormat formatStat = new SimpleDateFormat(MVUtil.DB_DATE_STAT, Locale.US);
        formatStat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));

        //  parse the valid times
        Date dateFcstValidBeg = formatStat.parse(findValueInArray(listToken, headerNames, "FCST_VALID_BEG"));
        Date dateFcstValidEnd = formatStat.parse(findValueInArray(listToken, headerNames, "FCST_VALID_END"));
        Date dateObsValidBeg = formatStat.parse(findValueInArray(listToken, headerNames, "OBS_VALID_BEG"));
        Date dateObsValidEnd = formatStat.parse(findValueInArray(listToken, headerNames, "OBS_VALID_END"));

        //  format the valid times for the database insert
        String strFcstValidBeg = formatDB.format(dateFcstValidBeg);
        String strFcstValidEnd = formatDB.format(dateFcstValidEnd);
        String strObsValidBeg = formatDB.format(dateObsValidBeg);
        String strObsValidEnd = formatDB.format(dateObsValidEnd);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = findValueInArray(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calFcstInitBeg.setTime(dateFcstValidBeg);
        calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
        Date dateFcstInitBeg = calFcstInitBeg.getTime();
        String strFcstInitBeg = formatDB.format(dateFcstInitBeg);

        //  ensure that the interp_pnts field value is a reasonable integer
        String strInterpPnts = findValueInArray(listToken, headerNames, "INTERP_PNTS");
        if (strInterpPnts.equals("NA")) {
          strInterpPnts = "0";
        }

        String strLineType = d._strLineType;

        //  do not load matched pair lines or orank lines
        if ((!_boolLoadMpr && strLineType.equals("MPR")) || (!_boolLoadOrank && strLineType.equals("ORANK"))) {
          continue;
        }


			/*
       * * * *  stat_header insert  * * * *
			 */
        intStatHeaderRecords++;

        //  build the stat_header value list for this line
        String[] listStatHeaderValue = {
          findValueInArray(listToken, headerNames, "VERSION"),    //  version
          findValueInArray(listToken, headerNames, "MODEL"),    //  model
          findValueInArray(listToken, headerNames, "DESC"),    //  descr
          findValueInArray(listToken, headerNames, "FCST_VAR"),    //  fcst_var
          findValueInArray(listToken, headerNames, "FCST_LEV"),    //  fcst_lev
          findValueInArray(listToken, headerNames, "OBS_VAR"),    //  obs_var
          findValueInArray(listToken, headerNames, "OBS_LEV"),    //  obs_lev
          findValueInArray(listToken, headerNames, "OBTYPE"),    //  obtype
          findValueInArray(listToken, headerNames, "VX_MASK"),    //  vx_mask
          findValueInArray(listToken, headerNames, "INTERP_MTHD"),    //  interp_mthd
          strInterpPnts,    //  interp_pnts
          findValueInArray(listToken, headerNames, "FCST_THRESH"),    //  fcst_thresh
          findValueInArray(listToken, headerNames, "OBS_THRESH")    //  obs_thresh
        };

        //  build a where clause for searching for duplicate stat_header records
        String strStatHeaderWhereClause =
          "  model = '" + findValueInArray(listToken, headerNames, "MODEL") + "'\n" +
            " AND descr = '" + findValueInArray(listToken, headerNames, "DESC") + "'\n" +
            //"  AND version = '" +				listToken[0] + "'\n" +
            "  AND fcst_var = '" + findValueInArray(listToken, headerNames, "FCST_VAR") + "'\n" +
            "  AND fcst_lev = '" + findValueInArray(listToken, headerNames, "FCST_LEV") + "'\n" +
            //"  AND obs_var = '" +			listToken[10] + "'\n" +
            //"  AND obs_lev = '" +			listToken[11] + "'\n" +
            "  AND obtype = '" + findValueInArray(listToken, headerNames, "OBTYPE") + "'\n" +
            "  AND vx_mask = '" + findValueInArray(listToken, headerNames, "VX_MASK") + "'\n" +
            "  AND interp_mthd = '" + findValueInArray(listToken, headerNames, "INTERP_MTHD") + "'\n" +
            "  AND interp_pnts = " + strInterpPnts + "\n" +
            "  AND fcst_thresh = '" + findValueInArray(listToken, headerNames, "FCST_THRESH") + "'\n" +
            "  AND obs_thresh = '" + findValueInArray(listToken, headerNames, "OBS_THRESH") + "'";

        //  build the value list for the stat_header insert
        String strStatHeaderValueList = "";
        for (int i = 0; i < listStatHeaderValue.length; i++) {
          strStatHeaderValueList += (0 < i ? ", " : "") + "'" + listStatHeaderValue[i] + "'";
        }


        String strFileLine = strFilename + ":" + intLine;

        //  look for the header key in the table
        int intStatHeaderId = -1;
        if (_tableStatHeaders.containsKey(strStatHeaderValueList)) {
          intStatHeaderId = (Integer) _tableStatHeaders.get(strStatHeaderValueList);
        }

        //  if the stat_header does not yet exist, create one
        else {

          //  look for an existing stat_header record with the same information
          boolean boolFoundStatHeader = false;
          long intStatHeaderSearchBegin = new Date().getTime();
          if (_boolStatHeaderDBCheck) {
            String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
            Connection con = null;
            Statement stmt = null;
            ResultSet res = null;
            try {
              con = connectionPool.getConnection();
              stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
              //stmt.setFetchSize(Integer.MIN_VALUE);
              res = stmt.executeQuery(strStatHeaderSelect);
              if (res.next()) {
                String strStatHeaderIdDup = res.getString(1);
                intStatHeaderId = Integer.parseInt(strStatHeaderIdDup);
                boolFoundStatHeader = true;
              }
            } catch (Exception e) {
              System.out.println(e.getMessage());
            } finally {
              try {
                res.close();
              } catch (Exception e) { /* ignored */ }
              try {
                stmt.close();
              } catch (Exception e) { /* ignored */ }
              try {
                con.close();
              } catch (Exception e) { /* ignored */ }
            }
          }
          intStatHeaderSearchTime = new Date().getTime() - intStatHeaderSearchBegin;
          _intStatHeaderSearchTime += intStatHeaderSearchTime;

          //  if the stat_header was not found, add it to the table
          if (!boolFoundStatHeader) {

            intStatHeaderId = intStatHeaderIdNext++;
            _tableStatHeaders.put(strStatHeaderValueList, intStatHeaderId);

            //  build an insert statement for the mode header
            strStatHeaderValueList =
              Integer.toString(intStatHeaderId) + ", " +        //  stat_header_id
                strStatHeaderValueList;

            //  insert the record into the stat_header database table
            String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + strStatHeaderValueList + ");";
            int intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
            if (1 != intStatHeaderInsert) {
              System.out.println("  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
            }
            intStatHeaderInserts++;
          } else {
            _tableStatHeaders.put(strStatHeaderValueList, intStatHeaderId);
          }
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
        if (boolHasVarLengthGroups) {
          int intLineDataId = _tableVarLengthLineDataId.get(strLineType);
          strLineDataId = Integer.toString(intLineDataId) + ", ";
          _tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
          int[] listVarLengthGroupIndices = (int[]) _tableVarLengthGroupIndices.get(d._strLineType);

          if (d._strLineType.equals("RHIST") || d._strLineType.equals("PSTD")) {
            intLineDataMax = intLineDataMax - Integer.valueOf(listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
          } else {
            intLineDataMax = listVarLengthGroupIndices[1];
          }
        }

        //  build the value list for the insert statment
        String strLineDataValueList =
          strLineDataId +            //  line_data_id (if present)
            intStatHeaderId + ", " +      //  stat_header_id
            info._dataFileId + ", " +      //  data_file_id
            intLine + ", " +          //  line_num
            strFcstLead + ", " +        //  fcst_lead
            "'" + strFcstValidBeg + "', " +    //  fcst_valid_beg
            "'" + strFcstValidEnd + "', " +    //  fcst_valid_end
            "'" + strFcstInitBeg + "', " +    //  fcst_init_beg
            findValueInArray(listToken, headerNames, "OBS_LEAD") + ", " +        //  obs_lead
            "'" + strObsValidBeg + "', " +    //  obs_valid_beg
            "'" + strObsValidEnd + "'";      //  obs_valid_end

        //  if the line data requires a cov_thresh value, add it
        String strCovThresh = findValueInArray(listToken, headerNames, "COV_THRESH");
        if (_tableCovThreshLineTypes.containsKey(d._strLineType)) {
          if (strCovThresh.equals("NA")) {
            System.out.println("  **  WARNING: cov_thresh value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine);
          }
          strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
        } else if (!strCovThresh.equals("NA")) {
          System.out.println("  **  WARNING: unexpected cov_thresh value '" + strCovThresh + "' with line type '" + d._strLineType + "'\n        " + d._strFileLine);
        }

        //  if the line data requires an alpha value, add it
        String strAlpha = findValueInArray(listToken, headerNames, "ALPHA");
        if (_tableAlphaLineTypes.containsKey(d._strLineType)) {
          if (strAlpha.equals("NA")) {
            System.out.println("  **  WARNING: alpha value NA with line type '" + d._strLineType + "'\n        " + d._strFileLine);
          }
          strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
        } else if (!strAlpha.equals("NA")) {
          System.out.println("  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + d._strLineType + "'\n        " + d._strFileLine);
        }

        //  add total and all of the stats on the rest of the line to the value list
        for (int i = headerNames.size(); i < intLineDataMax; i++) {
          //  for the METv2.0 MPR line type, add the obs_sid
          if (headerNames.size() +2 == i && "MPR".equals(d._strLineType) && "V2.0".equals(strMetVersion)) {
            strLineDataValueList += ", 'NA'";
          }
          //  add the stats in order
          strLineDataValueList += ", '" + replaceInvalidValues(listToken[i]) + "'";
        }


        if (strLineType.equals("ORANK")) {
          //skip ensemble fields and get data for the rest
          int[] listVarLengthGroupIndices = (int[]) _tableVarLengthGroupIndices.get(d._strLineType);
          int extraFieldsInd = intLineDataMax + Integer.valueOf(listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
          for (int i = extraFieldsInd; i < listToken.length; i++) {
            strLineDataValueList += ", '" + replaceInvalidValues(listToken[i]) + "'";
          }
        }


        String[] insertValuesArr = strLineDataValueList.split(",");
        List<String> insertValuesList = new LinkedList<>(Arrays.asList(insertValuesArr));
        int size = insertValuesList.size();
        int maxSize = size;
        switch (strLineType) {
          case "CNT":
            maxSize = 99;
            break;
          case "MPR":
            maxSize = 21;
            break;
          case "ORANK":
            maxSize = 26;
            break;
          case "CTS":
            maxSize = 104;
            break;
          case "NBRCTS":
            maxSize = 105;
            break;
          case "NBRCNT":
            maxSize = 30;
            break;
          case "SAL1L2":
            maxSize = 17;
            break;
          case "SL1L2":
            maxSize = 17;
            break;
          case "PSTD":
            maxSize = 28;
            break;
          case "SSVAR":
            maxSize = 46;
            break;
          case "RHIST":
            maxSize = 16;
            break;
          default:
        }
        while (size < maxSize) {
          insertValuesList.add("-9999");
          size++;
        }
        strLineDataValueList = "";
        for (String s : insertValuesList) {
          strLineDataValueList = strLineDataValueList + s + ",";
        }
        strLineDataValueList = strLineDataValueList.substring(0, strLineDataValueList.length() - 1);

        //  add the values list to the line type values map
        List<String> listLineTypeValues = new ArrayList<>();
        if (d._tableLineDataValues.containsKey(d._strLineType)) {
          listLineTypeValues = d._tableLineDataValues.get(d._strLineType);
        }
        listLineTypeValues.add("(" + strLineDataValueList + ")");
        d._tableLineDataValues.put(d._strLineType, listLineTypeValues);
        intLineDataInserts++;


			/*
       * * * *  var_length insert  * * * *
			 */

        if (boolHasVarLengthGroups) {

          //  get the index information about the current line type
          int[] listVarLengthGroupIndices = (int[]) _tableVarLengthGroupIndices.get(d._strLineType);
          int intGroupCntIndex = listVarLengthGroupIndices[0];
          int intGroupIndex = listVarLengthGroupIndices[1];
          int intGroupSize = listVarLengthGroupIndices[2];
          int intNumGroups = Integer.parseInt(listToken[intGroupCntIndex]);

          if (d._strLineType.equals("PCT") || d._strLineType.equals("PJC") || d._strLineType.equals("PRC")) {
            intNumGroups -= 1;
          }
          List<String> listThreshValues = d._tableVarLengthValues.get(d._strLineType);
          if (null == listThreshValues) {
            listThreshValues = new ArrayList<>();
          }

          //  build a insert value statement for each threshold group
          if (d._strLineType.equals("MCTC")) {
            for (int i = 0; i < intNumGroups; i++) {
              for (int j = 0; j < intNumGroups; j++) {
                listThreshValues.add("(" + strLineDataId + (i + 1) + ", " + (j + 1) + ", " +
                  replaceInvalidValues(listToken[intGroupIndex++]) + ")");
                intVarLengthRecords++;
              }
            }
          } else {
            if (d._strLineType.equals("RHIST") || d._strLineType.equals("PSTD")) {
              intGroupIndex = intLineDataMax;
            }
            for (int i = 0; i < intNumGroups; i++) {
              String strThreshValues = "(" + strLineDataId + (i + 1);
              for (int j = 0; j < intGroupSize; j++) {
                strThreshValues += ", " + replaceInvalidValues(listToken[intGroupIndex++]);
              }
              strThreshValues += ")";
              listThreshValues.add(strThreshValues);
              intVarLengthRecords++;
            }
          }
          d._tableVarLengthValues.put(d._strLineType, listThreshValues);
        }

        //  if the insert threshhold has been reached, commit the stored data to the database
        if (_intInsertSize <= d._listInsertValues.size()) {
          int[] listInserts = commitStatData(d);
          intLineDataInserts += listInserts[INDEX_LINE_DATA];
          intVarLengthInserts += listInserts[INDEX_VAR_LENGTH];
        }

      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(d);
    intLineDataInserts += listInserts[INDEX_LINE_DATA];
    intVarLengthInserts += listInserts[INDEX_VAR_LENGTH];


    _intStatLinesTotal += (intLine - 1);
    _intStatHeaderRecords += intStatHeaderRecords;
    _intStatHeaderInserts += intStatHeaderInserts;
    _intLineDataInserts += intLineDataInserts;
    _intLineDataRecords += intLineDataRecords;
    _intVarLengthRecords += intVarLengthRecords;
    _intVarLengthInserts += intVarLengthInserts;

    //  print a performance report
    long intStatHeaderLoadTime = new Date().getTime() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (_boolVerbose) {
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

  /**
   * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method was designed to be called from loadStatFile(), which is
   * responsible for building insert value lists for the various types of grid_stat and point_stat database tables.
   *
   * @param d Data structure loaded with insert value lists
   * @return An array of four integers, indexed by the INDEX_* members, representing the number of database inserts of each type
   * @throws Exception
   */
  public static int[] commitStatData(MVLoadStatInsertData d)
    throws Exception {

    int[] listInserts = new int[]{0, 0, 0, 0};

		/*
     * * * *  stat_header was committed commit  * * * *
		 */

    d._listInsertValues.clear();


		/*
     * * * *  line_data commit  * * * *
		 */

    //  for each line type, build an insert statement with the appropriate list of values
    for (Map.Entry<String, List<String>> entry : d._tableLineDataValues.entrySet()) {
      d._strLineType = entry.getKey();
      ArrayList listValues = (ArrayList) entry.getValue();
      String strLineDataTable = "line_data_" + d._strLineType.toLowerCase(Locale.US);

      int intResLineDataInsert = executeBatch(listValues, strLineDataTable);
      if (listValues.size() != intResLineDataInsert) {
        System.out.println("  **  WARNING: unexpected result from line_data INSERT: " +
          intResLineDataInsert + "\n        " + d._strFileLine);
      }
      listInserts[INDEX_LINE_DATA]++; //  intLineDataInserts++;
    }
    d._tableLineDataValues.clear();


		/*
     * * * *  stat_group commit  * * * *
		 */

    //  build a stat_group insert with all stored values
    if (!d._listStatGroupInsertValues.isEmpty()) {
      String strStatGroupInsertValues = "";
      for (int i = 0; i < d._listStatGroupInsertValues.size(); i++) {
        strStatGroupInsertValues += (i == 0 ? "" : ", ") + d._listStatGroupInsertValues.get(i);
      }
      String strStatGroupInsert = "INSERT INTO stat_group VALUES " + strStatGroupInsertValues + ";";
      int intStatGroupInsert = executeUpdate(strStatGroupInsert);
      if (d._listStatGroupInsertValues.size() != intStatGroupInsert) {
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
    String[] listVarLengthTypes = d._tableVarLengthValues.keySet().toArray(new String[]{});
    for (int i = 0; i < listVarLengthTypes.length; i++) {
      String[] listVarLengthValues = toArray(d._tableVarLengthValues.get(listVarLengthTypes[i]));
      if (1 > listVarLengthValues.length) {
        continue;
      }
      String strVarLengthTable = _tableVarLengthTable.get(listVarLengthTypes[i]);
      String strThreshInsert = "INSERT INTO " + strVarLengthTable + " VALUES ";
      for (int j = 0; j < listVarLengthValues.length; j++) {
        strThreshInsert += (0 < j ? ", " : "") + listVarLengthValues[j];
        listInserts[INDEX_VAR_LENGTH]++; //  intVarLengthInserts++;
      }
      int intThreshInsert = executeUpdate(strThreshInsert);
      if (listVarLengthValues.length != intThreshInsert) {
        System.out.println("  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " +
          listVarLengthValues.length + "\n        " + d._strFileLine);
      }
      d._tableVarLengthValues.put(listVarLengthTypes[i], new ArrayList<>());
    }

    return listInserts;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the database underlying the input Connection. The header
   * information can be checked in two different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).  Records in mode_obj_pair
   * tables, mode_obj_single tables and mode_cts tables are created from the data in the input file.  If necessary, records in the mode_header table are
   * created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target database
   * @throws Exception
   */
  public static void loadModeFile(DataFileInfo info) throws Exception {

    //  data structure for storing mode object ids
    Map<String, Integer> tableModeObjectId = new HashMap<>();

    //  performance counters
    long intModeHeaderLoadStart = new Date().getTime();
    long intModeHeaderSearchTime = 0;
    int intModeHeaderInserts = 0;
    int intModeCtsInserts = 0;
    int intModeObjSingleInserts = 0;
    int intModeObjPairInserts = 0;

    //  get the next mode record ids from the database
    int intModeHeaderIdNext = getNextId("mode_header", "mode_header_id");
    int intModeObjIdNext = getNextId("mode_obj_single", "mode_obj_id");

    //  set up the input file for reading
    String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
    int intLine = 1;
    List<String> headerNames = new ArrayList<>();
    try (
      FileReader fileReader = new FileReader(strFilename);
      BufferedReader reader = new BufferedReader(fileReader);) {
      //  read each line of the input file
      while (reader.ready()) {
        String[] listToken = reader.readLine().split("\\s+");

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          intLine++;
          continue;
        }

        String strFileLine = strFilename + ":" + intLine;

        //  determine the line type
        int intLineTypeLuId;
        int intDataFileLuId = info._dataFileLuId;
        String strObjectId = findValueInArray(listToken, headerNames, "OBJECT_ID");
        Matcher matModeSingle = _patModeSingle.matcher(strObjectId);
        Matcher matModePair = _patModePair.matcher(strObjectId);
        if (2 == intDataFileLuId) {
          intLineTypeLuId = MODE_CTS;
        } else if (matModeSingle.matches()) {
          intLineTypeLuId = MODE_SINGLE;
        } else if (matModePair.matches()) {
          intLineTypeLuId = MODE_PAIR;
        } else {
          throw new Exception("METViewer load error: loadModeFile() unable to determine line type " + findValueInArray(listToken, headerNames, "OBJECT_ID") + "\n        " + strFileLine);
        }


			/*
       * * * *  mode_header insert  * * * *
			 */

        SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat formatStat = new SimpleDateFormat(MVUtil.DB_DATE_STAT, Locale.US);
        formatStat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //  parse the valid times
        Date dateFcstValidBeg = formatStat.parse(findValueInArray(listToken, headerNames, "FCST_VALID"));
        Date dateObsValidBeg = formatStat.parse(findValueInArray(listToken, headerNames, "OBS_VALID"));

        //  format the valid times for the database insert
        String strFcstValidBeg = formatDB.format(dateFcstValidBeg);
        String strObsValidBeg = formatDB.format(dateObsValidBeg);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = findValueInArray(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calFcstInitBeg.setTime(dateFcstValidBeg);
        calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
        Date dateFcstInitBeg = calFcstInitBeg.getTime();
        String strFcstInit = formatDB.format(dateFcstInitBeg);

        //  build a value list from the header information
        //replace "NA" for fcst_accum (listToken[4]) and obs_accum (listToken[7]) to NULL

        String strModeHeaderValueList =
          "'" + findValueInArray(listToken, headerNames, "VERSION") + "', " +      //  version
            "'" + findValueInArray(listToken, headerNames, "MODEL") + "', " +      //  model
            "'" + findValueInArray(listToken, headerNames, "DESC") + "', " +      //  descr
            "'" + findValueInArray(listToken, headerNames, "FCST_LEAD") + "', " +      //  fcst_lead
            "'" + strFcstValidBeg + "', ";      //  fcst_valid
        if ("NA".equals(findValueInArray(listToken, headerNames, "FCST_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  fcst_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList + "'" + findValueInArray(listToken, headerNames, "FCST_ACCUM") + "', ";      //  fcst_accum
        }
        strModeHeaderValueList = strModeHeaderValueList + "'" + strFcstInit + "', " +        //  fcst_init
          "'" + findValueInArray(listToken, headerNames, "OBS_LEAD") + "', " +      //  obs_lead
          "'" + strObsValidBeg + "', ";      //  obs_valid
        if ("NA".equals(findValueInArray(listToken, headerNames, "OBS_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  obs_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList + "'" + findValueInArray(listToken, headerNames, "OBS_ACCUM") + "', ";      //  obs_accum
        }
        strModeHeaderValueList = strModeHeaderValueList + "'" + findValueInArray(listToken, headerNames, "FCST_RAD") + "', " +      //  fcst_rad
          "'" + findValueInArray(listToken, headerNames, "FCST_THR") + "', " +      //  fcst_thr
          "'" + findValueInArray(listToken, headerNames, "OBS_RAD") + "', " +      //  obs_rad
          "'" + findValueInArray(listToken, headerNames, "OBS_THR") + "', " +      //  obs_thr
          "'" + findValueInArray(listToken, headerNames, "FCST_VAR") + "', " +      //  fcst_var
          "'" + findValueInArray(listToken, headerNames, "FCST_LEV") + "', " +      //  fcst_lev
          "'" + findValueInArray(listToken, headerNames, "OBS_VAR") + "', " +      //  obs_var
          "'" + findValueInArray(listToken, headerNames, "OBS_LEV") + "'";        //  obs_lev

        String strModeHeaderWhereClause =
          "  version = '" + findValueInArray(listToken, headerNames, "VERSION") + "'\n" +
            "  AND model = '" + findValueInArray(listToken, headerNames, "MODEL") + "'\n" +
            "  AND descr = '" + findValueInArray(listToken, headerNames, "DESC") + "'\n" +
            "  AND fcst_lead = '" + findValueInArray(listToken, headerNames, "FCST_LEAD") + "'\n" +
            "  AND fcst_valid = '" + strFcstValidBeg + "'\n" +
            "  AND fcst_accum = '" + findValueInArray(listToken, headerNames, "FCST_ACCUM") + "'\n" +
            "  AND fcst_init = '" + strFcstInit + "'\n" +
            "  AND obs_lead = '" + findValueInArray(listToken, headerNames, "OBS_LEAD") + "'\n" +
            "  AND obs_valid = '" + strObsValidBeg + "'\n" +
            "  AND obs_accum = '" + findValueInArray(listToken, headerNames, "OBS_ACCUM") + "'\n" +
            "  AND fcst_rad = '" + findValueInArray(listToken, headerNames, "FCST_RAD") + "'\n" +
            "  AND fcst_thr = '" + findValueInArray(listToken, headerNames, "FCST_THR") + "'\n" +
            "  AND obs_rad = '" + findValueInArray(listToken, headerNames, "OBS_RAD") + "'\n" +
            "  AND obs_thr = '" + findValueInArray(listToken, headerNames, "OBS_THR") + "'\n" +
            "  AND fcst_var = '" + findValueInArray(listToken, headerNames, "FCST_VAR") + "'\n" +
            "  AND fcst_lev = '" + findValueInArray(listToken, headerNames, "FCST_LEV") + "'\n" +
            "  AND obs_var = '" + findValueInArray(listToken, headerNames, "OBS_VAR") + "'\n" +
            "  AND obs_lev = '" + findValueInArray(listToken, headerNames, "OBS_LEV") + "';";

        //  look for the header key in the table
        int intModeHeaderId = -1;
        if (_tableModeHeaders.containsKey(strModeHeaderValueList)) {
          intModeHeaderId = (Integer) _tableModeHeaders.get(strModeHeaderValueList);
        }

        //  if the mode_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean boolFoundModeHeader = false;
          long intModeHeaderSearchBegin = new Date().getTime();
          if (_boolModeHeaderDBCheck) {
            String strModeHeaderSelect = "SELECT\n  mode_header_id\nFROM\n  mode_header\nWHERE\n" + strModeHeaderWhereClause;
            try (Connection con = connectionPool.getConnection();
                 Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(strModeHeaderSelect)) {
              if (res.next()) {
                String strModeHeaderIdDup = res.getString(1);
                intModeHeaderId = Integer.parseInt(strModeHeaderIdDup);
                boolFoundModeHeader = true;
                System.out.println("  **  WARNING: found duplicate mode_header record with id " + strModeHeaderIdDup + "\n        " + strFileLine);
              }
              res.close();
              stmt.close();
              con.close();
            } catch (Exception e) {
              System.out.println(e.getMessage());
            }

          }
          intModeHeaderSearchTime = new Date().getTime() - intModeHeaderSearchBegin;
          _intModeHeaderSearchTime += intModeHeaderSearchTime;

          //  if the mode_header was not found, add it to the table
          if (!boolFoundModeHeader) {

            intModeHeaderId = intModeHeaderIdNext++;
            _tableModeHeaders.put(strModeHeaderValueList, intModeHeaderId);

            //  build an insert statement for the mode header
            strModeHeaderValueList =
              intModeHeaderId + ", " +        //  mode_header_id
                intLineTypeLuId + ", " +        //  line_type_lu_id
                info._dataFileId + ", " +        //  data_file_id
                intLine + ", " +            //  linenumber
                strModeHeaderValueList;

            //  insert the record into the mode_header database table
            String strModeHeaderInsert = "INSERT INTO mode_header VALUES (" + strModeHeaderValueList + ");";
            int intModeHeaderInsert = executeUpdate(strModeHeaderInsert);
            if (1 != intModeHeaderInsert) {
              System.out.println("  **  WARNING: unexpected result from mode_header INSERT: " + intModeHeaderInsert + "\n        " + strFileLine);
            }
            intModeHeaderInserts++;
          }
        }


			/*
       * * * *  mode_cts insert  * * * *
			 */

        if (MODE_CTS == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          String strCTSValueList = intModeHeaderId + ", '" + findValueInArray(listToken, headerNames, "FIELD") + "'";
          int totalIndex = headerNames.indexOf("TOTAL");
          for (int i = 0; i < 18; i++) {
            strCTSValueList += ", " + replaceInvalidValues(listToken[totalIndex + i]);
          }

          //  insert the record into the mode_cts database table
          String strModeCtsInsert = "INSERT INTO mode_cts VALUES (" + strCTSValueList + ");";
          int intModeCtsInsert = executeUpdate(strModeCtsInsert);
          if (1 != intModeCtsInsert) {
            System.out.println("  **  WARNING: unexpected result from mode_cts INSERT: " + intModeCtsInsert + "\n        " + strFileLine);
          }
          intModeCtsInserts++;

        }

			/*
			 * * * *  mode_obj_single insert  * * * *
			 */

        else if (MODE_SINGLE == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          int intModeObjId = intModeObjIdNext++;
          String strSingleValueList = intModeObjId + ", " + intModeHeaderId + ", '" + strObjectId + "', '" + findValueInArray(listToken, headerNames, "OBJECT_CAT") + "'";
          int centroidxIndex = headerNames.indexOf("CENTROID_X");
          for (int i = 0; i < 21; i++) {
            strSingleValueList += ", " + replaceInvalidValues(listToken[centroidxIndex + i]);
          }

          //  insert the record into the mode_obj_single database table
          String strModeObjSingleInsert = "INSERT INTO mode_obj_single VALUES (" + strSingleValueList + ");";
          int intModeObjSingleInsert = executeUpdate(strModeObjSingleInsert);
          if (1 != intModeObjSingleInsert) {
            System.out.println("  **  WARNING: unexpected result from mode_obj_single INSERT: " + intModeObjSingleInsert + "\n        " + strFileLine);
          }
          intModeObjSingleInserts++;

          //  add the mode_obj_id to the table, using the object_id as the key
          tableModeObjectId.put(strObjectId, intModeObjId);

        }

			/*
			 * * * *  mode_obj_pair insert  * * * *
			 */

        else if (MODE_PAIR == intLineTypeLuId) {

          //  determine the mode_obj_id values for the pair
          int intModeObjectIdFcst = tableModeObjectId.get(matModePair.group(1));
          int intModeObjectIdObs = tableModeObjectId.get(matModePair.group(2));

          //  build the value list for the mode_cts insert
          String strPairValueList = intModeObjectIdObs + ", " + intModeObjectIdFcst + ", " + intModeHeaderId + ", " +
            "'" + strObjectId + "', '" + findValueInArray(listToken, headerNames, "OBJECT_CAT") + "'";
          int centroiddistIndex = headerNames.indexOf("CENTROID_DIST");
          for (int i = 0; i < 12; i++) {
            strPairValueList += ", " + replaceInvalidValues(listToken[centroiddistIndex + i]);
          }

          //  insert the record into the mode_obj_pair database table
          String strModeObjPairInsert = "INSERT INTO mode_obj_pair VALUES (" + strPairValueList + ");";
          int intModeObjPairInsert = executeUpdate(strModeObjPairInsert);
          if (1 != intModeObjPairInsert) {
            System.out.println("  **  WARNING: unexpected result from mode_obj_pair INSERT: " + intModeObjPairInsert + "\n        " + strFileLine);
          }
          intModeObjPairInserts++;

        }

        intLine++;
      }
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //  increment the global mode counters
    _intModeLinesTotal += (intLine - 1);
    _intModeHeaderRecords += intModeHeaderInserts;
    _intModeCtsRecords += intModeCtsInserts;
    _intModeObjSingleRecords += intModeObjSingleInserts;
    _intModeObjPairRecords += intModeObjPairInserts;

    //  print a performance report
    if (_boolVerbose) {
      long intModeHeaderLoadTime = new Date().getTime() - intModeHeaderLoadStart;
      System.out.println(padBegin("mode_header inserts: ", 36) + intModeHeaderInserts + "\n" +
        padBegin("mode_cts inserts: ", 36) + intModeCtsInserts + "\n" +
        padBegin("mode_obj_single inserts: ", 36) + intModeObjSingleInserts + "\n" +
        padBegin("mode_obj_pair inserts: ", 36) + intModeObjPairInserts + "\n" +
        (_boolModeHeaderDBCheck ? padBegin("mode_header search time: ", 36) + formatTimeSpan(intModeHeaderSearchTime) + "\n" : "") +
        padBegin("total load time: ", 36) + formatTimeSpan(intModeHeaderLoadTime) + "\n\n");
    }
  }

  public static int executeBatch(List<String> listValues, String strLineDataTable) throws Exception {

    String strLineDataInsert = "INSERT INTO " + strLineDataTable + " VALUES " + "(";
    int numberOfValues = listValues.get(0).split(",").length;
    for (int i = 0; i < numberOfValues; i++) {
      strLineDataInsert = strLineDataInsert + "?,";
    }
    strLineDataInsert = strLineDataInsert.substring(0, strLineDataInsert.length() - 1);
    strLineDataInsert = strLineDataInsert + ")";
    int intResLineDataInsert = 0;
    Connection con = null;
    Statement stmt = null;
    PreparedStatement ps = null;
    try {
      con = connectionPool.getConnection();
      stmt = con.createStatement();
      ps = con.prepareStatement(strLineDataInsert);
      for (int i = 0; i < listValues.size(); i++) {

        String[] listValuesArr = listValues.get(i).split(",");
        listValuesArr[0] = listValuesArr[0].replace("(", "");
        listValuesArr[listValuesArr.length - 1] = listValuesArr[listValuesArr.length - 1].replace(")", "");
        for (int j = 0; j < listValuesArr.length; j++) {
          ps.setObject(j + 1, listValuesArr[j].trim().replaceAll("'", ""));
        }
        ps.addBatch();

        //execute and commit batch of 20000 queries
        if (i % 20000 == 0) {
          int[] updateCounts = ps.executeBatch();
          intResLineDataInsert = intResLineDataInsert + IntStream.of(updateCounts).sum();
        }
      }

      int[] updateCounts = ps.executeBatch();
      intResLineDataInsert = intResLineDataInsert + IntStream.of(updateCounts).sum();

    } catch (SQLException se) {
      throw new Exception("caught SQLException calling executeBatch: " + se.getMessage());
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return intResLineDataInsert;
  }

  /**
   * Executes the input update statement against the database underlying the input Connection and cleans up any resources upon completion.
   *
   * @param update SQL UPDATE statement to execute
   * @return Number of records affected (output of Statement.executeUpdate() call)
   * @throws SQLException
   */
  public static int executeUpdate(String update) throws Exception {

    int intRes = -1;
    try (
      Connection con = connectionPool.getConnection();
      Statement stmt = con.createStatement();) {
      intRes = stmt.executeUpdate(update);
      stmt.close();
      con.close();
    } catch (SQLException se) {
      System.out.println("Error " + update);
      throw new Exception("caught SQLException calling executeUpdate: " + se.getMessage());
    }

    return intRes;
  }

  public static String replaceInvalidValues(String strData) {
    return strData.replace("NA", "-9999").replace("-nan", "-9999").replace("nan", "-9999");
  }

  /**
   * Build and execute a query that retrieves the next table record id, whose name is specified by the input field, from the specified input table. The
   * statement is run against the input Connection and the next available id is returned. // * @param con
   *
   * @param table Database table whose next available id is returned
   * @param field Field name of the table id record
   * @return Next available id
   * @throws Exception
   */
  public static int getNextId(String table, String field) throws Exception {
    int intId = -1;
    PreparedStatement pstmt = null;
    ResultSet res = null;
    try (Connection con = connectionPool.getConnection();) {
      pstmt = con.prepareStatement("SELECT MAX(" + field + ") FROM " + table);
      res = pstmt.executeQuery();
      if (!res.next()) {
        throw new Exception("METViewer load error: getNextId(" + table + ", " + field + ") unable to find max id");
      }
      String strId = res.getString(1);
      if (null == strId) {
        intId = 0;
      } else {
        intId = Integer.parseInt(strId) + 1;
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
      if (res != null) {
        res.close();
      }
    }

    return intId;
  }

  /**
   * Analyze the input file object to determine what type of MET output file it is.  Create an entry in the data_file table for the file and build a
   * DataFileInfo data structure with information about the file and return it.
   *
   * @param file points to a MET output file to process // * @param con database connection to use
   * @return data structure containing information about the input file
   */
  public static DataFileInfo processDataFile(File file) throws Exception {
    String strPath = file.getParent().replace("\\", "/");
    String strFile = file.getName();
    int strDataFileLuId = -1;
    String strDataFileLuTypeName;
    Integer dataFileId = -1;

    //check file size and return if it  is 0
    if (file.length() == 0) {
      return null;
    }
    SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
    formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
    // set default values for the loaded time (now) and the modified time (that of input file)
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String strLoadDate = formatDB.format(cal.getTime());
    cal.setTimeInMillis(file.lastModified());
    String strModDate = formatDB.format(cal.getTime());

    // determine the type of the input data file by parsing the filename
    if (strFile.matches("\\S+\\.stat$")) {
      strDataFileLuTypeName = "stat";
    } else if (strFile.matches("\\S+_obj\\.txt$")) {
      strDataFileLuTypeName = "mode_obj";
    } else if (strFile.matches("\\S+_cts\\.txt$")) {
      strDataFileLuTypeName = "mode_cts";
    } else if (strFile.matches("\\S+\\.vsdb$")) {
      strDataFileLuTypeName = "vsdb_point_stat";
    } else {
      return null;
    }

    if (!"".equals(strDataFileLuTypeName)) {
      strDataFileLuId = _tableDataFileLU.get(strDataFileLuTypeName);
    }

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


    try (
      Connection con = connectionPool.getConnection();
      Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
      ResultSet res = stmt.executeQuery(strDataFileQuery);) {

      // if the data file is already present in the database, print a warning and return the id
      if (res.next()) {
        strDataFileLuTypeName = res.getString(1);
        dataFileId = res.getInt(2);
        strLoadDate = res.getString(3);
        strModDate = res.getString(4);

        if (_boolForceDupFile) {
          DataFileInfo info = new DataFileInfo(dataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
          System.out.println("  **  WARNING: file already present in table data_file"); // :\n" + printDataFileInfo(info) + "\n");
          return info;
        } else {
          throw new Exception("file already present in table data_file, use force_dup_file setting to override");
        }
      }
      res.close();
      stmt.close();
      con.close();
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
    // if the file is not present in the data_file table, query for the largest data_file_id
    try (
      Connection con = connectionPool.getConnection();
      Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
      ResultSet res = stmt.executeQuery("SELECT MAX(data_file_id) FROM data_file;")) {

      if (!res.next()) {
        throw new Exception("METViewer load error: processDataFile() unable to find max data_file_id");
      }
      dataFileId = res.getInt(1);
      if (res.wasNull()) {
        dataFileId = 0;
      }
      dataFileId = dataFileId + 1;
      res.close();
      stmt.close();
      con.close();

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }


    // add the input file to the data_file table
    String strDataFileInsert =
      "INSERT INTO data_file VALUES (" +
        dataFileId + ", " +      // data_file_id
        strDataFileLuId + ", " +    // data_file_lu_id
        "'" + strFile + "', " +      // filename
        "'" + strPath + "', " +      // path
        "'" + strLoadDate + "', " +    // load_date
        "'" + strModDate + "');";    // mod_date
    int intRes = executeUpdate(strDataFileInsert);
    if (1 != intRes) {
      System.out.println("  **  WARNING: unexpected result from data_file INSERT: " + intRes);
    }

    return new DataFileInfo(dataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
  }

  public static void applyIndexes(boolean drop) throws Exception {

    System.out.println("    ==== indexes ====\n" + (drop ? "  dropping..." : ""));
    Map.Entry[] listIndexes = _mapIndexes.getOrderedEntries();
    for (int i = 0; i < listIndexes.length; i++) {
      String strIndexKey = listIndexes[i].getKey().toString();
      String strField = listIndexes[i].getValue().toString();
      long intIndexStart = new Date().getTime();

      //  build a create index statment and run it
      Matcher matIndex = _patIndexName.matcher(strIndexKey);
      if (!matIndex.matches()) {
        throw new Exception("  **  ERROR: failed to parse index key " + strIndexKey);
      }
      String strTable = matIndex.group(1);
      String strIndexName = strTable + matIndex.group(2);
      String strIndex;
      if (drop) {
        strIndex = "DROP INDEX " + strIndexName + " ON " + strTable + " ;";
      } else {
        strIndex = "CREATE INDEX " + strIndexName + " ON " + strTable + " (" + strField + ");";
      }
      try {
        executeUpdate(strIndex);
      } catch (Exception e) {
        System.out.println("  **  ERROR: caught " + e.getClass() + " applying index " + strIndexName + ": " + e.getMessage());
      }

      //  print out a performance message
      long intIndexTime = new Date().getTime() - intIndexStart;
      System.out.println(padBegin(strIndexName + ": ", 36) + formatTimeSpan(intIndexTime));
    }
    System.out.println();
  }

  public static void applyIndexes() throws Exception {
    applyIndexes(false);
  }

  public static void dropIndexes() throws Exception {
    applyIndexes(true);
  }

  /**
   * Initialize the table containing the max line_data_id for all line_data tables corresponding to variable length rows. //* @param con database connection
   * used to search against
   *
   * @throws Exception
   */
  public static void initVarLengthLineDataIds() throws Exception {
    _tableVarLengthLineDataId.clear();
    String[] listVarLengthLines = _tableVarLengthTable.keySet().toArray(new String[]{});
    for (int i = 0; i < listVarLengthLines.length; i++) {
      String strVarLengthTable = "line_data_" + listVarLengthLines[i].toLowerCase();
      int intLineDataId = getNextId(strVarLengthTable, "line_data_id");
      _tableVarLengthLineDataId.put(listVarLengthLines[i], intLineDataId);
    }
  }

  /*
   * MVLoadStatInsertData is used to store insert value lists for the various types of grid_stat and point_stat
   * tables.  The structure is built in loadStatFile() and is unloaded and executed in commitStatData().
   */
  static class MVLoadStatInsertData {

    public final List<String> _listInsertValues = new ArrayList<>();
    public final Map<String, List<String>> _tableLineDataValues = new HashMap<>();
    public final List<String> _listStatGroupInsertValues = new ArrayList<>();
    public final Map<String, List<String>> _tableVarLengthValues = new HashMap<>();
    public String _strLineType = "";
    public String _strFileLine = "";
  }

}

class DataFileInfo {

  public Integer _dataFileId = 0;
  public String _dataFileFilename = "";
  public String _dataFilePath = "";
  public String _dataFileLoadDate = "";
  public String _dataFileModDate = "";
  public int _dataFileLuId = -1;
  public String _dataFileLuTypeName = "";

  public DataFileInfo(Integer dataFileId, String dataFileFilename,
                      String dataFilePath, String dataFileLoadDate,
                      String dataFileModDate, int dataFileLuId,
                      String dataFileLuTypeName) {
    _dataFileId = dataFileId;
    _dataFileFilename = dataFileFilename;
    _dataFilePath = dataFilePath;
    _dataFileLoadDate = dataFileLoadDate;
    _dataFileModDate = dataFileModDate;
    _dataFileLuId = dataFileLuId;
    _dataFileLuTypeName = dataFileLuTypeName;
  }
}

/**
 * MysqlLoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import edu.ucar.metviewer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:19 $
 */
public class MysqlLoadDatabaseManager extends MysqlDatabaseManager implements LoadDatabaseManager {

  private static final Logger logger = LogManager.getLogger("MysqlLoadDatabaseManager");


  private final Pattern patIndexName = Pattern.compile("#([\\w\\d]+)#([\\w\\d]+)");
  private final Map<String, Integer> tableVarLengthLineDataId = new HashMap<>();
  private final Map<String, Integer> statHeaders = new HashMap<>();

  private static final  int INDEX_LINE_DATA = 1;
  private static final  int INDEX_VAR_LENGTH = 3;

  private final Map<String, Integer> modeHeaders = new HashMap<>();


  private final String[] listLineDataTables = {
    "line_data_fho", "line_data_ctc", "line_data_cts", "line_data_cnt", "line_data_pct",
    "line_data_pstd", "line_data_pjc", "line_data_prc", "line_data_sl1l2", "line_data_sal1l2",
    "line_data_vl1l2", "line_data_val1l2", "line_data_mpr", "line_data_nbrctc", "line_data_nbrcts",
    "line_data_nbrcnt", "line_data_isc", "line_data_mctc", "line_data_rhist", "line_data_orank",
    "line_data_ssvar", "line_data_enscnt"
  };
  private MVOrderedMap mapIndexes;
  /*
    * variable length table names for each variable length output line type
    */
  private final Map<String, String> tableVarLengthTable;
  /*
    * data_file_lu_id values for each MET output type
    */
  private final Map<String, Integer> tableDataFileLU;

  public MysqlLoadDatabaseManager(DatabaseInfo databaseInfo) throws Exception {
    super(databaseInfo);
    mapIndexes = new MVOrderedMap();
    mapIndexes.put("#stat_header#_model_idx", "model");
    mapIndexes.put("#stat_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#stat_header#_fcst_lev_idx", "fcst_lev");
    mapIndexes.put("#stat_header#_obtype_idx", "obtype");
    mapIndexes.put("#stat_header#_vx_mask_idx", "vx_mask");
    mapIndexes.put("#stat_header#_interp_mthd_idx", "interp_mthd");
    mapIndexes.put("#stat_header#_interp_pnts_idx", "interp_pnts");
    mapIndexes.put("#stat_header#_fcst_thresh_idx", "fcst_thresh");

    mapIndexes.put("#mode_header#_model_idx", "model");
    mapIndexes.put("#mode_header#_fcst_lead_idx", "fcst_lead");
    mapIndexes.put("#mode_header#_fcst_valid_idx", "fcst_valid");
    mapIndexes.put("#mode_header#_fcst_init_idx", "fcst_init");
    mapIndexes.put("#mode_header#_fcst_rad_idx", "fcst_rad");
    mapIndexes.put("#mode_header#_fcst_thr_idx", "fcst_thr");
    mapIndexes.put("#mode_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#mode_header#_fcst_lev_idx", "fcst_lev");

    for (String listLineDataTable : listLineDataTables) {
      mapIndexes.put("#" + listLineDataTable + "#_fcst_lead_idx", "fcst_lead");
      mapIndexes.put("#" + listLineDataTable + "#_fcst_valid_beg_idx", "fcst_valid_beg");
      mapIndexes.put("#" + listLineDataTable + "#_fcst_init_beg_idx", "fcst_init_beg");
    }
    tableVarLengthTable = new HashMap<>();
    tableVarLengthTable.put("PCT", "line_data_pct_thresh");
    tableVarLengthTable.put("PSTD", "line_data_pstd_thresh");
    tableVarLengthTable.put("PJC", "line_data_pjc_thresh");
    tableVarLengthTable.put("PRC", "line_data_prc_thresh");
    tableVarLengthTable.put("MCTC", "line_data_mctc_cnt");
    tableVarLengthTable.put("RHIST", "line_data_rhist_rank");
    tableVarLengthTable.put("PHIST", "line_data_phist_bin");
    tableVarLengthTable.put("ORANK", "line_data_orank_ens");

    tableDataFileLU = new HashMap<>();
    tableDataFileLU.put("point_stat", 0);
    tableDataFileLU.put("grid_stat", 1);
    tableDataFileLU.put("mode_cts", 2);
    tableDataFileLU.put("mode_obj", 3);
    tableDataFileLU.put("wavelet_stat", 4);
    tableDataFileLU.put("ensemble_stat", 5);
    tableDataFileLU.put("vsdb_point_stat", 6);
    tableDataFileLU.put("stat", 7);


    initVarLengthLineDataIds();
  }

  @Override
  public void applyIndexes() throws Exception {
    applyIndexes(false);
  }

  @Override
  public void dropIndexes() throws Exception {
    applyIndexes(true);
  }

  private void applyIndexes(boolean drop) throws Exception {

    logger.info("    ==== indexes ====\n" + (drop ? "  dropping..." : ""));
    Map.Entry[] listIndexes = mapIndexes.getOrderedEntries();
    for (Map.Entry listIndex : listIndexes) {
      String strIndexKey = listIndex.getKey().toString();
      String strField = listIndex.getValue().toString();
      long intIndexStart = new Date().getTime();

      //  build a create index statment and run it
      Matcher matIndex = patIndexName.matcher(strIndexKey);
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
        logger.error("  **  ERROR: caught " + e.getClass() + " applying index " + strIndexName + ": " + e.getMessage());
      }

      //  print out a performance message
      long intIndexTime = new Date().getTime() - intIndexStart;
      logger.info(MVUtil.padBegin(strIndexName + ": ", 36) + MVUtil.formatTimeSpan(intIndexTime));
    }
    logger.info("");
  }

  /**
   * Executes the input update statement against the database underlying the input Connection and cleans up any resources upon completion.
   *
   * @param update SQL UPDATE statement to execute
   * @return Number of records affected (output of Statement.executeUpdate() call)
   * @throws SQLException
   */
  private int executeUpdate(String update) throws Exception {

    int intRes;
    try (
      Connection con = getConnection();
      Statement stmt = con.createStatement()) {
      intRes = stmt.executeUpdate(update);
      stmt.close();
      con.close();
    } catch (SQLException se) {
      logger.error(update);
      throw new Exception("caught SQLException calling executeUpdate: " + se.getMessage());
    }

    return intRes;
  }

  /**
   * Initialize the table containing the max line_data_id for all line_data tables corresponding to variable length rows. //* @param con database connection
   * used to search against
   *
   * @throws Exception
   */
  private void initVarLengthLineDataIds() throws Exception {
    tableVarLengthLineDataId.clear();
    Set<String> lineTypes = tableVarLengthTable.keySet();
    for (String lineType : lineTypes) {
      String strVarLengthTable = "line_data_" + lineType.toLowerCase();
      int lineDataId = getNextId(strVarLengthTable, "line_data_id");
      tableVarLengthLineDataId.put(lineType, lineDataId);
    }
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
  private int getNextId(String table, String field) throws Exception {
    int intId = -1;
    PreparedStatement pstmt = null;
    ResultSet res = null;
    try (Connection con = getConnection()) {
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
   * Load the MET output data from the data file underlying the input DataFileInfo object into the database underlying the input Connection. The header
   * information can be checked in two different ways: using a table for the current file (specified by _boolStatHeaderTableCheck) or by searching the
   * stat_header table for a duplicate (specified by _boolStatHeaderDBCheck).  Records in line_data tables, stat_group tables and line_data_thresh tables are
   * created from the data in the input file. If necessary, records in the stat_header table are created as well.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target database
   * @throws Exception
   */
  @Override
  public Map<String, Long> loadStatFile(DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();
    //  initialize the insert data structure
    MVLoadStatInsertData mvLoadStatInsertData = new MVLoadStatInsertData();
    //  performance counters
    long intStatHeaderLoadStart = new Date().getTime();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);

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
        String strMetVersion = MVUtil.findValueInArray(listToken, headerNames, "VERSION");


        //  if the line type load selector is activated, check that the current line type is on the list
        mvLoadStatInsertData.setLineType(MVUtil.findValueInArray(listToken, headerNames, "LINE_TYPE"));
        if (info._boolLineTypeLoad && !info._tableLineTypeLoad.containsKey(mvLoadStatInsertData.getLineType())) {
          continue;
        }

        mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);

        SimpleDateFormat formatStat = new SimpleDateFormat(MVUtil.DB_DATE_STAT, Locale.US);
        formatStat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));

        //  parse the valid times
        Date dateFcstValidBeg = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID_BEG"));
        Date dateFcstValidEnd = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID_END"));
        Date dateObsValidBeg = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID_BEG"));
        Date dateObsValidEnd = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID_END"));

        //  format the valid times for the database insert
        String strFcstValidBeg = formatDB.format(dateFcstValidBeg);
        String strFcstValidEnd = formatDB.format(dateFcstValidEnd);
        String strObsValidBeg = formatDB.format(dateObsValidBeg);
        String strObsValidEnd = formatDB.format(dateObsValidEnd);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
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
        String strInterpPnts = MVUtil.findValueInArray(listToken, headerNames, "INTERP_PNTS");
        if (strInterpPnts.equals("NA")) {
          strInterpPnts = "0";
        }

        String strLineType = mvLoadStatInsertData.getLineType();

        //  do not load matched pair lines or orank lines
        if ((!info._boolLoadMpr && strLineType.equals("MPR")) || (!info._boolLoadOrank && strLineType.equals("ORANK"))) {
          continue;
        }


			/*
       * * * *  stat_header insert  * * * *
			 */
        headerRecords++;

        //  build the stat_header value list for this line
        String[] listStatHeaderValue = {
          MVUtil.findValueInArray(listToken, headerNames, "VERSION"),    //  version
          MVUtil.findValueInArray(listToken, headerNames, "MODEL"),    //  model
          MVUtil.findValueInArray(listToken, headerNames, "DESC"),    //  descr
          MVUtil.findValueInArray(listToken, headerNames, "FCST_VAR"),    //  fcst_var
          MVUtil.findValueInArray(listToken, headerNames, "FCST_LEV"),    //  fcst_lev
          MVUtil.findValueInArray(listToken, headerNames, "OBS_VAR"),    //  obs_var
          MVUtil.findValueInArray(listToken, headerNames, "OBS_LEV"),    //  obs_lev
          MVUtil.findValueInArray(listToken, headerNames, "OBTYPE"),    //  obtype
          MVUtil.findValueInArray(listToken, headerNames, "VX_MASK"),    //  vx_mask
          MVUtil.findValueInArray(listToken, headerNames, "INTERP_MTHD"),    //  interp_mthd
          strInterpPnts,    //  interp_pnts
          MVUtil.findValueInArray(listToken, headerNames, "FCST_THRESH"),    //  fcst_thresh
          MVUtil.findValueInArray(listToken, headerNames, "OBS_THRESH")    //  obs_thresh
        };

        //  build a where clause for searching for duplicate stat_header records
        String strStatHeaderWhereClause =
          "  model = '" + MVUtil.findValueInArray(listToken, headerNames, "MODEL") + "'\n" +
            " AND descr = '" + MVUtil.findValueInArray(listToken, headerNames, "DESC") + "'\n" +
            //"  AND version = '" +				listToken[0] + "'\n" +
            "  AND fcst_var = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_VAR") + "'\n" +
            "  AND fcst_lev = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEV") + "'\n" +
            //"  AND obs_var = '" +			listToken[10] + "'\n" +
            //"  AND obs_lev = '" +			listToken[11] + "'\n" +
            "  AND obtype = '" + MVUtil.findValueInArray(listToken, headerNames, "OBTYPE") + "'\n" +
            "  AND vx_mask = '" + MVUtil.findValueInArray(listToken, headerNames, "VX_MASK") + "'\n" +
            "  AND interp_mthd = '" + MVUtil.findValueInArray(listToken, headerNames, "INTERP_MTHD") + "'\n" +
            "  AND interp_pnts = " + strInterpPnts + "\n" +
            "  AND fcst_thresh = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_THRESH") + "'\n" +
            "  AND obs_thresh = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_THRESH") + "'";

        //  build the value list for the stat_header insert
        String strStatHeaderValueList = "";
        for (int i = 0; i < listStatHeaderValue.length; i++) {
          strStatHeaderValueList += (0 < i ? ", " : "") + "'" + listStatHeaderValue[i] + "'";
        }


        String strFileLine = strFilename + ":" + intLine;

        //  look for the header key in the table
        int intStatHeaderId = -1;
        if (statHeaders.containsKey(strStatHeaderValueList)) {
          intStatHeaderId =  statHeaders.get(strStatHeaderValueList);
        }

        //  if the stat_header does not yet exist, create one
        else {

          //  look for an existing stat_header record with the same information
          boolean boolFoundStatHeader = false;
          long intStatHeaderSearchBegin = new Date().getTime();
          if (info._boolStatHeaderDBCheck) {
            String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
            Connection con = null;
            Statement stmt = null;
            ResultSet res = null;
            try {
              con = getConnection();
              stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
              res = stmt.executeQuery(strStatHeaderSelect);
              if (res.next()) {
                String strStatHeaderIdDup = res.getString(1);
                intStatHeaderId = Integer.parseInt(strStatHeaderIdDup);
                boolFoundStatHeader = true;
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
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

          timeStats.put("headerSearchTime", timeStats.get("headerSearchTime") + new Date().getTime() - intStatHeaderSearchBegin);


          //  if the stat_header was not found, add it to the table
          if (!boolFoundStatHeader) {

            intStatHeaderId = intStatHeaderIdNext++;
            statHeaders.put(strStatHeaderValueList, intStatHeaderId);

            //  build an insert statement for the mode header
            strStatHeaderValueList =
              Integer.toString(intStatHeaderId) + ", " +        //  stat_header_id
                strStatHeaderValueList;

            //  insert the record into the stat_header database table
            String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + strStatHeaderValueList + ");";
            int intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
            if (1 != intStatHeaderInsert) {
              logger.warn("  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
            }
            headerInserts++;
          } else {
            statHeaders.put(strStatHeaderValueList, intStatHeaderId);
          }
        }


			/*
       * * * *  line_data insert  * * * *
			 */

        //
        int intLineDataMax = listToken.length;
        String strLineDataId = "";
        dataRecords++;

        //  if the line type is of variable length, get the line_data_id
        boolean boolHasVarLengthGroups = MVUtil.lengthGroupIndices.containsKey(mvLoadStatInsertData.getLineType());

        //  determine the maximum token index for the data
        if (boolHasVarLengthGroups) {
          int intLineDataId = tableVarLengthLineDataId.get(strLineType);
          strLineDataId = Integer.toString(intLineDataId) + ", ";
          tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
          int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices.get(mvLoadStatInsertData.getLineType());
          int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1, listVarLengthGroupIndices1.length);
          if (headerNames.indexOf("DESC") < 0) {
            //for old versions
            listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
            listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;

          }

          if (mvLoadStatInsertData.getLineType().equals("RHIST") || mvLoadStatInsertData.getLineType().equals("PSTD")) {
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
            MVUtil.findValueInArray(listToken, headerNames, "OBS_LEAD") + ", " +        //  obs_lead
            "'" + strObsValidBeg + "', " +    //  obs_valid_beg
            "'" + strObsValidEnd + "'";      //  obs_valid_end

        //  if the line data requires a cov_thresh value, add it
        String strCovThresh = MVUtil.findValueInArray(listToken, headerNames, "COV_THRESH");
        if (MVUtil.covThreshLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
          strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
        }

        //  if the line data requires an alpha value, add it
        String strAlpha = MVUtil.findValueInArray(listToken, headerNames, "ALPHA");
        if (MVUtil.alphaLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
          if (strAlpha.equals("NA")) {
            logger.warn("  **  WARNING: alpha value NA with line type '" + mvLoadStatInsertData.getLineType() + "'\n        " + mvLoadStatInsertData.getFileLine());
          }
          strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
        } else if (!strAlpha.equals("NA")) {
          logger.warn("  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + mvLoadStatInsertData.getLineType() + "'\n        " + mvLoadStatInsertData.getFileLine());
        }

        //  add total and all of the stats on the rest of the line to the value list
        for (int i = headerNames.indexOf("LINE_TYPE")+1; i < intLineDataMax; i++) {
          //  for the METv2.0 MPR line type, add the obs_sid
          if (headerNames.indexOf("LINE_TYPE")+1 + 2 == i && "MPR".equals(mvLoadStatInsertData.getLineType()) && "V2.0".equals(strMetVersion)) {
            strLineDataValueList += ", 'NA'";
          }
          //  add the stats in order
          strLineDataValueList += ", '" + replaceInvalidValues(listToken[i]) + "'";
        }


        if (strLineType.equals("ORANK")) {
          //skip ensemble fields and get data for the rest
          int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices.get(mvLoadStatInsertData.getLineType());
          int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1, listVarLengthGroupIndices1.length);
          if (headerNames.indexOf("DESC") < 0) {
            //for old versions
            listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
            listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;
          }
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
            maxSize = 27;
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
            maxSize = 29;
            break;
          case "SSVAR":
            maxSize = 46;
            break;
          case "RHIST":
            maxSize = 17;
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
        if (mvLoadStatInsertData.getTableLineDataValues().containsKey(mvLoadStatInsertData.getLineType())) {
          listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues().get(mvLoadStatInsertData.getLineType());
        }
        listLineTypeValues.add("(" + strLineDataValueList + ")");
        mvLoadStatInsertData.getTableLineDataValues().put(mvLoadStatInsertData.getLineType(), listLineTypeValues);
        dataInserts++;


			/*
       * * * *  var_length insert  * * * *
			 */

        if (boolHasVarLengthGroups) {

          //  get the index information about the current line type
          int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices.get(mvLoadStatInsertData.getLineType());
          int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1, listVarLengthGroupIndices1.length);
          if (headerNames.indexOf("DESC") < 0) {
            //for old versions
            listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
            listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;
          }
          int intGroupCntIndex = listVarLengthGroupIndices[0];
          int intGroupIndex = listVarLengthGroupIndices[1];
          int intGroupSize = listVarLengthGroupIndices[2];
          int intNumGroups = Integer.parseInt(listToken[intGroupCntIndex]);

          if (mvLoadStatInsertData.getLineType().equals("PCT") || mvLoadStatInsertData.getLineType().equals("PJC") || mvLoadStatInsertData.getLineType().equals("PRC")) {
            intNumGroups -= 1;
          }
          List<String> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues().get(mvLoadStatInsertData.getLineType());
          if (null == listThreshValues) {
            listThreshValues = new ArrayList<>();
          }

          //  build a insert value statement for each threshold group
          if (mvLoadStatInsertData.getLineType().equals("MCTC")) {
            for (int i = 0; i < intNumGroups; i++) {
              for (int j = 0; j < intNumGroups; j++) {
                listThreshValues.add("(" + strLineDataId + (i + 1) + ", " + (j + 1) + ", " +
                  replaceInvalidValues(listToken[intGroupIndex++]) + ")");
                lengthRecords++;
              }
            }
          } else {
            if (mvLoadStatInsertData.getLineType().equals("RHIST") || mvLoadStatInsertData.getLineType().equals("PSTD")) {
              intGroupIndex = intLineDataMax;
            }
            for (int i = 0; i < intNumGroups; i++) {
              String strThreshValues = "(" + strLineDataId + (i + 1);
              for (int j = 0; j < intGroupSize; j++) {
                strThreshValues += ", " + replaceInvalidValues(listToken[intGroupIndex++]);
              }
              strThreshValues += ")";
              listThreshValues.add(strThreshValues);
              lengthRecords++;
            }
          }
          mvLoadStatInsertData.getTableVarLengthValues().put(mvLoadStatInsertData.getLineType(), listThreshValues);
        }

        //  if the insert threshhold has been reached, commit the stored data to the database
        if (info._intInsertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
          int[] listInserts = commitStatData(mvLoadStatInsertData);
          dataInserts += listInserts[INDEX_LINE_DATA];
          lengthInserts += listInserts[INDEX_VAR_LENGTH];
        }

      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(mvLoadStatInsertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];


    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);


    //  print a performance report
    long intStatHeaderLoadTime = new Date().getTime() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (info._boolVerbose) {
      logger.info(MVUtil.padBegin("file lines: ", 36) + (intLine - 1) + "\n" +
        MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
        MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
        MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
        MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
        MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
        MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
        MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
        MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(intStatHeaderLoadTime) + "\n" +
        MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(headerSearchTime) + "\n" +
        MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(dblLinesPerMSec) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method was designed to be called from loadStatFile(), which is
   * responsible for building insert value lists for the various types of grid_stat and point_stat database tables.
   *
   * @param mvLoadStatInsertData Data structure loaded with insert value lists
   * @return An array of four integers, indexed by the INDEX_* members, representing the number of database inserts of each type
   * @throws Exception
   */
  private int[] commitStatData(MVLoadStatInsertData mvLoadStatInsertData)
    throws Exception {

    int[] listInserts = new int[]{0, 0, 0, 0};

  		/*
       * * * *  stat_header was committed commit  * * * *
  		 */

    mvLoadStatInsertData.getListInsertValues().clear();


  		/*
       * * * *  line_data commit  * * * *
  		 */

    //  for each line type, build an insert statement with the appropriate list of values
    for (Map.Entry<String, List<String>> entry : mvLoadStatInsertData.getTableLineDataValues().entrySet()) {
      mvLoadStatInsertData.setLineType(entry.getKey());
      ArrayList listValues = (ArrayList) entry.getValue();
      String strLineDataTable = "line_data_" + mvLoadStatInsertData.getLineType().toLowerCase(Locale.US);

      int intResLineDataInsert = executeBatch(listValues, strLineDataTable);
      if (listValues.size() != intResLineDataInsert) {
        logger.warn("  **  WARNING: unexpected result from line_data INSERT: " +
          intResLineDataInsert + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      listInserts[INDEX_LINE_DATA]++;
    }
    mvLoadStatInsertData.getTableLineDataValues().clear();


  		/*
       * * * *  stat_group commit  * * * *
  		 */

    //  build a stat_group insert with all stored values
    if (!mvLoadStatInsertData.getListStatGroupInsertValues().isEmpty()) {
      String strStatGroupInsertValues = "";
      for (int i = 0; i < mvLoadStatInsertData.getListStatGroupInsertValues().size(); i++) {
        strStatGroupInsertValues += (i == 0 ? "" : ", ") + mvLoadStatInsertData.getListStatGroupInsertValues().get(i);
      }
      String strStatGroupInsert = "INSERT INTO stat_group VALUES " + strStatGroupInsertValues + ";";
      int intStatGroupInsert = executeUpdate(strStatGroupInsert);
      if (mvLoadStatInsertData.getListStatGroupInsertValues().size() != intStatGroupInsert) {
        logger.warn("  **  WARNING: unexpected result from stat_group INSERT: " + intStatGroupInsert + " vs. " +
          mvLoadStatInsertData.getListStatGroupInsertValues().size() + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      int indexStatGroup = 2;
      listInserts[indexStatGroup]++;
    }
    mvLoadStatInsertData.getListStatGroupInsertValues().clear();

  		/*
       * * * *  variable length data commit  * * * *
  		 */

    //  insert probabilistic data into the thresh tables
    Set<String> strings = mvLoadStatInsertData.getTableVarLengthValues().keySet();
    String[] listVarLengthTypes = strings.toArray(new String[strings.size()]);


    for (String listVarLengthType : listVarLengthTypes) {
      String[] listVarLengthValues = MVUtil.toArray(mvLoadStatInsertData.getTableVarLengthValues().get(listVarLengthType));
      if (1 > listVarLengthValues.length) {
        continue;
      }
      String strVarLengthTable = tableVarLengthTable.get(listVarLengthType);
      String strThreshInsert = "INSERT INTO " + strVarLengthTable + " VALUES ";
      for (int j = 0; j < listVarLengthValues.length; j++) {
        strThreshInsert += (0 < j ? ", " : "") + listVarLengthValues[j];
        listInserts[INDEX_VAR_LENGTH]++; //  lengthInserts++;
      }
      int intThreshInsert = executeUpdate(strThreshInsert);
      if (listVarLengthValues.length != intThreshInsert) {
        logger.warn("  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " +
          listVarLengthValues.length + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      mvLoadStatInsertData.getTableVarLengthValues().put(listVarLengthType, new ArrayList<>());
    }

    return listInserts;
  }

  @Override
  public Map<String, Long> loadStatFileVSDB(DataFileInfo info) throws Exception {

    Map<String, Long> timeStats = new HashMap<>();

    //  initialize the insert data structure
    MVLoadStatInsertData mvLoadStatInsertData = new MVLoadStatInsertData();

    //  performance counters
    long intStatHeaderLoadStart = new Date().getTime();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);


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
      SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
            mvLoadStatInsertData.setLineType("CNT");
          } else if (listToken[6].equals("BSS")) {
            mvLoadStatInsertData.setLineType("PSTD");
          } else if (listToken[6].equals("HIST")) {
            mvLoadStatInsertData.setLineType("RHIST");
          } else if (listToken[6].equals("SL1L2")) {
            mvLoadStatInsertData.setLineType("SL1L2");
          } else if (listToken[6].equals("SAL1L2")) {
            mvLoadStatInsertData.setLineType("SAL1L2");
          } else if (listToken[6].equals("VL1L2")) {
            mvLoadStatInsertData.setLineType("VL1L2");
          } else if (listToken[6].equals("VAL1L2")) {
            mvLoadStatInsertData.setLineType("VAL1L2");
          } else if (listToken[6].equals("RPS")) {
            mvLoadStatInsertData.setLineType("ENSCNT");
          } else if (listToken[6].equals("RELI")) {
            mvLoadStatInsertData.setLineType("PCT");
            int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
            thresh = "==1/" +String.valueOf(intGroupSize);
          } else if (listToken[6].startsWith("FHO")) {
            mvLoadStatInsertData.setLineType("CTC");
            String[] threshArr = listToken[6].split("FHO");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else if (listToken[6].startsWith("FSS")) {
            mvLoadStatInsertData.setLineType("NBRCNT");
            String[] threshArr = listToken[6].split("FSS");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else {
            continue;
          }
          if (info._boolLineTypeLoad && !info._tableLineTypeLoad.containsKey(mvLoadStatInsertData.getLineType())) {
            continue;
          }

          mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);

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

          String strLineType = mvLoadStatInsertData.getLineType();



			/*
       * * * *  stat_header insert  * * * *
			 */
          headerRecords++;

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
          if (statHeaders.containsKey(strStatHeaderValueList)) {
            intStatHeaderId = statHeaders.get(strStatHeaderValueList);
          }

          //  if the stat_header does not yet exist, create one
          else {

            //  look for an existing stat_header record with the same information
            boolean boolFoundStatHeader = false;
            long intStatHeaderSearchBegin = new Date().getTime();
            if (info._boolStatHeaderDBCheck) {
              String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
              Connection con = null;
              Statement stmt = null;
              ResultSet res = null;
              try {
                con = getConnection();
                stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
                res = stmt.executeQuery(strStatHeaderSelect);
                if (res.next()) {
                  String strStatHeaderIdDup = res.getString(1);
                  intStatHeaderId = Integer.parseInt(strStatHeaderIdDup);
                  boolFoundStatHeader = true;
                }
              } catch (Exception e) {
                logger.error(e.getMessage());
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
            timeStats.put("headerSearchTime", timeStats.get("headerSearchTime") + new Date().getTime() - intStatHeaderSearchBegin);


            //  if the stat_header was not found, add it to the table
            if (!boolFoundStatHeader) {

              intStatHeaderId = intStatHeaderIdNext++;
              statHeaders.put(strStatHeaderValueList, intStatHeaderId);

              //  build an insert statement for the mode header
              strStatHeaderValueList = Integer.toString(intStatHeaderId) + ", " +        //  stat_header_id
                strStatHeaderValueList;

              //  insert the record into the stat_header database table
              String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + strStatHeaderValueList + ");";

              int intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
              if (1 != intStatHeaderInsert) {
                logger.warn("  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
              }
              headerInserts++;
            } else {
              statHeaders.put(strStatHeaderValueList, intStatHeaderId);
            }
          }


			/*
       * * * *  line_data insert  * * * *
			 */

          //
          String strLineDataId = "";
          dataRecords++;

          //  if the line type is of variable length, get the line_data_id
          boolean boolHasVarLengthGroups = MVUtil.lengthGroupIndices.containsKey(mvLoadStatInsertData.getLineType());

          //  determine the maximum token index for the data
          if (boolHasVarLengthGroups) {
            int intLineDataId = tableVarLengthLineDataId.get(strLineType);
            strLineDataId = Integer.toString(intLineDataId) + ", ";
            tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
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
          if (MVUtil.covThreshLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
            strLineDataValueList += ", '" + replaceInvalidValues(strCovThresh) + "'";
          }

          //  if the line data requires an alpha value, add it
          String strAlpha = "-9999";
          if (MVUtil.alphaLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
            if (strAlpha.equals("NA")) {
              logger.warn("  **  WARNING: alpha value NA with line type '" + mvLoadStatInsertData.getLineType() + "'\n        " + mvLoadStatInsertData.getFileLine());
            }
            strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
          }

          if (listToken[6].equals("RMSE")) {//CNT line type
            for (int i = 0; i < 88; i++) {
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
                strLineDataValueList += ", '" + listToken[13] + "'";
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
            for (int i = 0; i < 6; i++) {
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
                logger.error(e.getMessage());
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
              logger.error("o_rate os 0");
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
            double fss = -9999;
            if (listToken.length > 11) {
              fss = 1 - Double.valueOf(listToken[10]) / (Double.valueOf(listToken[11]) + Double.valueOf(listToken[12]));
            }
            for (int i = 0; i < 19; i++) {
              if (i == 0) {//total,
                strLineDataValueList += ", " + listToken[9];
              } else if (i == 1) {//fbs
                strLineDataValueList += ", " + listToken[10];
              } else if (i == 4) {//fss
                strLineDataValueList += ", " + fss;
              } else {
                strLineDataValueList += ", '-9999'";
              }
            }
          }


          //  add the values list to the line type values map
          List<String> listLineTypeValues = new ArrayList<>();
          if (mvLoadStatInsertData.getTableLineDataValues().containsKey(mvLoadStatInsertData.getLineType())) {
            listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues().get(mvLoadStatInsertData.getLineType());
          }
          listLineTypeValues.add("(" + strLineDataValueList + ")");
          mvLoadStatInsertData.getTableLineDataValues().put(mvLoadStatInsertData.getLineType(), listLineTypeValues);
          dataInserts++;


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

            List<String> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues().get(mvLoadStatInsertData.getLineType());
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
                lengthRecords++;
              }
            } else if (listToken[6].equals("RELI")) {
              int total = 0;
              for (int i = 0; i < intGroupSize; i++) {
                double thresh_i;
                if (intGroupSize > 1) {
                  thresh_i = (double) i / (double) (intGroupSize - 1);
                } else {
                  thresh_i = 0;
                }
                String strThreshValues = "(" + strLineDataId + (i + 1) + "," + thresh_i;
                Integer oy;
                Integer on;
                try {
                  oy = Double.valueOf(listToken[intGroupIndex]).intValue();
                  on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue() - oy;
                  strThreshValues += ", " + oy + ", " + on;
                  total = total + oy + on;
                } catch (Exception e) {
                  strThreshValues += ", -9999,  -9999";
                }

                intGroupIndex++;
                strThreshValues += ")";
                listThreshValues.add(strThreshValues);
                lengthRecords++;
              }
            }

            mvLoadStatInsertData.getTableVarLengthValues().put(mvLoadStatInsertData.getLineType(), listThreshValues);
          }

          //  if the insert threshhold has been reached, commit the stored data to the database
          if (info._intInsertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
            int[] listInserts = commitStatData(mvLoadStatInsertData);
            dataInserts += listInserts[INDEX_LINE_DATA];
            lengthInserts += listInserts[INDEX_VAR_LENGTH];
          }
        } catch (Exception e) {
          logger.error("ERROR: line:" + line + " has errors and would be ignored.");
          logger.error(e.getMessage());
        }
      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(mvLoadStatInsertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];

    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);

    //  print a performance report
    long intStatHeaderLoadTime = new Date().getTime() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (info._boolVerbose) {
      logger.info(MVUtil.padBegin("file lines: ", 6) + (intLine - 1) + "\n" +
        MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
        MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
        MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
        MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
        MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
        MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
        MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
        MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(intStatHeaderLoadTime) + "\n" +
        MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(headerSearchTime) + "\n" +
        MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(dblLinesPerMSec) + "\n\n");
    }
    logger.info("intLine " + intLine);
    return timeStats;
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
  @Override
  public Map<String, Long> loadModeFile(DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();

    //  data structure for storing mode object ids
    Map<String, Integer> tableModeObjectId = new HashMap<>();

    //  performance counters
    long intModeHeaderLoadStart = new Date().getTime();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long ctsInserts = 0;
    long objSingleInserts = 0;
    long objPairInserts = 0;

    //  get the next mode record ids from the database
    int intModeHeaderIdNext = getNextId("mode_header", "mode_header_id");
    int intModeObjIdNext = getNextId("mode_obj_single", "mode_obj_id");

    //  set up the input file for reading
    String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
    int intLine = 1;
    List<String> headerNames = new ArrayList<>();
    try (
      FileReader fileReader = new FileReader(strFilename);
      BufferedReader reader = new BufferedReader(fileReader)) {
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
        String strObjectId = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_ID");
        Matcher matModeSingle = MVUtil.patModeSingleObjectId.matcher(strObjectId);
        Matcher matModePair = MVUtil.patModePairObjectId.matcher(strObjectId);
        int modeCts = 19;
        int modeSingle = 17;
        int modePair = 18;
        if (2 == intDataFileLuId) {
          intLineTypeLuId = modeCts;
        } else if (matModeSingle.matches()) {
          intLineTypeLuId = modeSingle;
        } else if (matModePair.matches()) {
          intLineTypeLuId = modePair;
        } else {
          throw new Exception("METViewer load error: loadModeFile() unable to determine line type " + MVUtil.findValueInArray(listToken, headerNames, "OBJECT_ID") + "\n        " + strFileLine);
        }


			/*
       * * * *  mode_header insert  * * * *
			 */

        SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat formatStat = new SimpleDateFormat(MVUtil.DB_DATE_STAT, Locale.US);
        formatStat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //  parse the valid times
        Date dateFcstValidBeg = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID"));
        Date dateObsValidBeg = formatStat.parse(MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID"));

        //  format the valid times for the database insert
        String strFcstValidBeg = formatDB.format(dateFcstValidBeg);
        String strObsValidBeg = formatDB.format(dateObsValidBeg);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
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
          "'" + MVUtil.findValueInArray(listToken, headerNames, "VERSION") + "', " +      //  version
            "'" + MVUtil.findValueInArray(listToken, headerNames, "MODEL") + "', " +      //  model
            "'" + MVUtil.findValueInArray(listToken, headerNames, "DESC") + "', " +      //  descr
            "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD") + "', " +      //  fcst_lead
            "'" + strFcstValidBeg + "', ";      //  fcst_valid
        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "FCST_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  fcst_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList + "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_ACCUM") + "', ";      //  fcst_accum
        }
        strModeHeaderValueList = strModeHeaderValueList + "'" + strFcstInit + "', " +        //  fcst_init
          "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_LEAD") + "', " +      //  obs_lead
          "'" + strObsValidBeg + "', ";      //  obs_valid
        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "OBS_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  obs_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList + "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_ACCUM") + "', ";      //  obs_accum
        }
        strModeHeaderValueList = strModeHeaderValueList + "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_RAD") + "', " +      //  fcst_rad
          "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_THR") + "', " +      //  fcst_thr
          "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_RAD") + "', " +      //  obs_rad
          "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_THR") + "', " +      //  obs_thr
          "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_VAR") + "', " +      //  fcst_var
          "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEV") + "', " +      //  fcst_lev
          "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_VAR") + "', " +      //  obs_var
          "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_LEV") + "'";        //  obs_lev

        String strModeHeaderWhereClause =
          "  version = '" + MVUtil.findValueInArray(listToken, headerNames, "VERSION") + "'\n" +
            "  AND model = '" + MVUtil.findValueInArray(listToken, headerNames, "MODEL") + "'\n" +
            "  AND descr = '" + MVUtil.findValueInArray(listToken, headerNames, "DESC") + "'\n" +
            "  AND fcst_lead = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD") + "'\n" +
            "  AND fcst_valid = '" + strFcstValidBeg + "'\n" +
            "  AND fcst_accum = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_ACCUM") + "'\n" +
            "  AND fcst_init = '" + strFcstInit + "'\n" +
            "  AND obs_lead = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_LEAD") + "'\n" +
            "  AND obs_valid = '" + strObsValidBeg + "'\n" +
            "  AND obs_accum = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_ACCUM") + "'\n" +
            "  AND fcst_rad = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_RAD") + "'\n" +
            "  AND fcst_thr = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_THR") + "'\n" +
            "  AND obs_rad = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_RAD") + "'\n" +
            "  AND obs_thr = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_THR") + "'\n" +
            "  AND fcst_var = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_VAR") + "'\n" +
            "  AND fcst_lev = '" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEV") + "'\n" +
            "  AND obs_var = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_VAR") + "'\n" +
            "  AND obs_lev = '" + MVUtil.findValueInArray(listToken, headerNames, "OBS_LEV") + "';";

        //  look for the header key in the table
        int intModeHeaderId = -1;
        if (modeHeaders.containsKey(strModeHeaderValueList)) {
          intModeHeaderId = modeHeaders.get(strModeHeaderValueList);
        }

        //  if the mode_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean boolFoundModeHeader = false;
          long intModeHeaderSearchBegin = new Date().getTime();
          if (info._boolModeHeaderDBCheck) {
            String strModeHeaderSelect = "SELECT\n  mode_header_id\nFROM\n  mode_header\nWHERE\n" + strModeHeaderWhereClause;
            try (Connection con = getConnection();
                 Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(strModeHeaderSelect)) {
              if (res.next()) {
                String strModeHeaderIdDup = res.getString(1);
                intModeHeaderId = Integer.parseInt(strModeHeaderIdDup);
                boolFoundModeHeader = true;
                logger.warn("  **  WARNING: found duplicate mode_header record with id " + strModeHeaderIdDup + "\n        " + strFileLine);
              }
              res.close();
              stmt.close();
              con.close();
            } catch (Exception e) {
              logger.error(e.getMessage());
            }

          }
          timeStats.put("headerSearchTime", timeStats.get("headerSearchTime") + new Date().getTime() - intModeHeaderSearchBegin);


          //  if the mode_header was not found, add it to the table
          if (!boolFoundModeHeader) {

            intModeHeaderId = intModeHeaderIdNext++;
            modeHeaders.put(strModeHeaderValueList, intModeHeaderId);

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
              logger.warn("  **  WARNING: unexpected result from mode_header INSERT: " + intModeHeaderInsert + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


			/*
       * * * *  mode_cts insert  * * * *
			 */

        if (modeCts == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          String strCTSValueList = intModeHeaderId + ", '" + MVUtil.findValueInArray(listToken, headerNames, "FIELD") + "'";
          int totalIndex = headerNames.indexOf("TOTAL");
          for (int i = 0; i < 18; i++) {
            strCTSValueList += ", " + replaceInvalidValues(listToken[totalIndex + i]);
          }

          //  insert the record into the mode_cts database table
          String strModeCtsInsert = "INSERT INTO mode_cts VALUES (" + strCTSValueList + ");";
          int intModeCtsInsert = executeUpdate(strModeCtsInsert);
          if (1 != intModeCtsInsert) {
            logger.warn("  **  WARNING: unexpected result from mode_cts INSERT: " + intModeCtsInsert + "\n        " + strFileLine);
          }
          ctsInserts++;

        }

			/*
       * * * *  mode_obj_single insert  * * * *
			 */

        else if (modeSingle == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          int intModeObjId = intModeObjIdNext++;
          String strSingleValueList = intModeObjId + ", " + intModeHeaderId + ", '" + strObjectId + "', '" + MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT") + "'";
          int centroidxIndex = headerNames.indexOf("CENTROID_X");
          for (int i = 0; i < 21; i++) {
            strSingleValueList += ", " + replaceInvalidValues(listToken[centroidxIndex + i]);
          }

          //  insert the record into the mode_obj_single database table
          String strModeObjSingleInsert = "INSERT INTO mode_obj_single VALUES (" + strSingleValueList + ");";
          int intModeObjSingleInsert = executeUpdate(strModeObjSingleInsert);
          if (1 != intModeObjSingleInsert) {
            logger.warn("  **  WARNING: unexpected result from mode_obj_single INSERT: " + intModeObjSingleInsert + "\n        " + strFileLine);
          }
          objSingleInserts++;

          //  add the mode_obj_id to the table, using the object_id as the key
          tableModeObjectId.put(strObjectId, intModeObjId);

        }

			/*
       * * * *  mode_obj_pair insert  * * * *
			 */

        else if (modePair == intLineTypeLuId) {

          //  determine the mode_obj_id values for the pair
          int intModeObjectIdFcst = tableModeObjectId.get(matModePair.group(1));
          int intModeObjectIdObs = tableModeObjectId.get(matModePair.group(2));

          //  build the value list for the mode_cts insert
          String strPairValueList = intModeObjectIdObs + ", " + intModeObjectIdFcst + ", " + intModeHeaderId + ", " +
            "'" + strObjectId + "', '" + MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT") + "'";
          int centroiddistIndex = headerNames.indexOf("CENTROID_DIST");
          for (int i = 0; i < 12; i++) {
            strPairValueList += ", " + replaceInvalidValues(listToken[centroiddistIndex + i]);
          }

          //  insert the record into the mode_obj_pair database table
          String strModeObjPairInsert = "INSERT INTO mode_obj_pair VALUES (" + strPairValueList + ");";
          int intModeObjPairInsert = executeUpdate(strModeObjPairInsert);
          if (1 != intModeObjPairInsert) {
            logger.warn("  **  WARNING: unexpected result from mode_obj_pair INSERT: " + intModeObjPairInsert + "\n        " + strFileLine);
          }
          objPairInserts++;

        }

        intLine++;
      }
      fileReader.close();
      reader.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    //  increment the global mode counters
    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("ctsInserts", ctsInserts);
    timeStats.put("objSingleInserts", objSingleInserts);
    timeStats.put("objPairInserts", objPairInserts);


    //  print a performance report
    if (info._boolVerbose) {
      long intModeHeaderLoadTime = new Date().getTime() - intModeHeaderLoadStart;
      logger.info(MVUtil.padBegin("mode_header inserts: ", 36) + headerInserts + "\n" +
        MVUtil.padBegin("mode_cts inserts: ", 36) + ctsInserts + "\n" +
        MVUtil.padBegin("mode_obj_single inserts: ", 36) + objSingleInserts + "\n" +
        MVUtil.padBegin("mode_obj_pair inserts: ", 36) + objPairInserts + "\n" +
        (info._boolModeHeaderDBCheck ? MVUtil.padBegin("mode_header search time: ", 36) + MVUtil.formatTimeSpan(timeStats.get("headerSearchTime")) + "\n" : "") +
        MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(intModeHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  private int executeBatch(List<String> listValues, String strLineDataTable) throws Exception {

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
    IntStream intStream = null;
    try {
      con = getConnection();
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
          intStream = IntStream.of(updateCounts);
          intResLineDataInsert = intResLineDataInsert + intStream.sum();
          intStream.close();
          ps.clearBatch();
        }
      }

      int[] updateCounts = ps.executeBatch();
      intStream = IntStream.of(updateCounts);
      intResLineDataInsert = intResLineDataInsert + IntStream.of(updateCounts).sum();
      intStream.close();

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
      if(intStream != null){
        intStream.close();
      }
    }
    return intResLineDataInsert;
  }

  /**
   * Analyze the input file object to determine what type of MET output file it is.  Create an entry in the data_file table for the file and build a
   * DataFileInfo data structure with information about the file and return it.
   *
   * @param file points to a MET output file to process // * @param con database connection to use
   * @return data structure containing information about the input file
   */
  @Override
  public DataFileInfo processDataFile(File file, boolean forceDupFile) throws Exception {
    String strPath = file.getParent().replace("\\", "/");
    String strFile = file.getName();
    int strDataFileLuId = -1;
    String strDataFileLuTypeName;
    Integer dataFileId;

    //check file size and return if it  is 0
    if (file.length() == 0) {
      return null;
    }
    SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
      strDataFileLuId = tableDataFileLU.get(strDataFileLuTypeName);
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
      Connection con = getConnection();
      Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
      ResultSet res = stmt.executeQuery(strDataFileQuery)) {

      // if the data file is already present in the database, print a warning and return the id
      if (res.next()) {
        strDataFileLuTypeName = res.getString(1);
        dataFileId = res.getInt(2);
        strLoadDate = res.getString(3);
        strModDate = res.getString(4);

        if (forceDupFile) {
          DataFileInfo info = new DataFileInfo(dataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
          logger.warn("  **  WARNING: file already present in table data_file");
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
      Connection con = getConnection();
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
      logger.warn("  **  WARNING: unexpected result from data_file INSERT: " + intRes);
    }

    return new DataFileInfo(dataFileId, strFile, strPath, strLoadDate, strModDate, strDataFileLuId, strDataFileLuTypeName);
  }

  @Override
  public void updateInfoTable(String strXML, MVLoadJob job) throws Exception {
    //  get the instance_info information to insert
    int intInstInfoIdNext = getNextId("instance_info", "instance_info_id");
    String strUpdater = "";
    try {
      strUpdater = MVUtil.sysCmd();
    } catch (Exception e) {
      try {
        strUpdater = MVUtil.sysCmd();
      } catch (Exception e2) {
      }
    }
    strUpdater = strUpdater.trim();
    SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
    logger.info("Inserting instance_info record...  ");
    int intInsert = executeUpdate(strInstInfoSQL);
    if (1 != intInsert) {
      throw new Exception("unexpected number of instance_info rows inserted: " + intInsert);
    }
    logger.info("Done\n");
  }


  private String replaceInvalidValues(String strData) {
    return strData.replace("NA", "-9999").replace("-nan", "-9999").replace("nan", "-9999");
  }

}
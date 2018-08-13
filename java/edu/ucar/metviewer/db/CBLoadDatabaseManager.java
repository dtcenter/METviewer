/**
 * CBLoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import edu.ucar.metviewer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.couchbase.client.core.CouchbaseException;
            import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.*;
import com.couchbase.client.java.env.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:19 $
 */
public class CBLoadDatabaseManager extends CBDatabaseManager implements LoadDatabaseManager {

  private static final Logger logger = LogManager.getLogger("CBLoadDatabaseManager");

  private final Pattern patIndexName = Pattern.compile("#([\\w\\d]+)#([\\w\\d]+)");
  private final Map<String, Integer> tableVarLengthLineDataId = new HashMap<>();
  private final Map<String, String> statHeaders = new HashMap<>();

  private static final int INDEX_LINE_DATA = 1;
  private static final int INDEX_VAR_LENGTH = 3;

  private static final double[] X_POINTS_FOR_ECON = new double[]{
      0.952380952, 0.909090909, 0.800000000, 0.666666667, 0.500000000, 0.333333333,
      0.200000000, 0.125000000, 0.100000000, 0.055555556, 0.037037037, 0.025000000,
      0.016666667, 0.011111111, 0.007142857, 0.004761905, 0.002857143, 0.002000000
  };

  private final Map<String, Integer> modeHeaders = new HashMap<>();
  private final Map<String, Integer> mtdHeaders = new HashMap<>();
  private final String[] modeObjSingleColumns = new String[]{
      "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT", "CENTROID_LON", "AXIS_ANG", "LENGTH", "WIDTH", "AREA", "AREA_THRESH", "CURVATURE", "CURVATURE_X", "CURVATURE_Y", "COMPLEXITY", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75", "INTENSITY_90", "INTENSITY_50", "INTENSITY_SUM"};

  private final String[] mtdObj2dColumns = new String[]{
      "OBJECT_CAT", "TIME_INDEX", "AREA", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT",
      "CENTROID_LON", "AXIS_ANG"
  };
  private final String[] mtdObj3dSingleColumns = new String[]{
      "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_T", "CENTROID_LAT",
      "CENTROID_LON", "X_DOT", "Y_DOT ", "AXIS_ANG", "VOLUME", "START_TIME", "END_TIME", "CDIST_TRAVELLED", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75", "INTENSITY_90"
  };
  private final String[] listLineDataTables = {
      "line_data_fho", "line_data_ctc", "line_data_cts", "line_data_cnt", "line_data_pct",
      "line_data_pstd", "line_data_pjc", "line_data_prc", "line_data_sl1l2", "line_data_sal1l2",
      "line_data_vl1l2", "line_data_val1l2", "line_data_mpr", "line_data_nbrctc", "line_data_nbrcts",
      "line_data_nbrcnt", "line_data_isc", "line_data_mctc", "line_data_rhist", "line_data_orank", "line_data_relp", "line_data_eclv",
      "line_data_ssvar", "line_data_enscnt", "line_data_grad"
  };
  private MVOrderedMap mapIndexes;
  /*
    * variable length table names for each variable length output line type
    */
  private final Map<String, String> tableVarLengthTable;
  /*
   * names of the fields for each line type
   */
  private final Map<String, String> tableLineDataFieldsTable;
  /*
    * data_file_lu_id values for each MET output type
    */
  private final Map<String, Integer> tableDataFileLU;

  public CBLoadDatabaseManager(DatabaseInfo databaseInfo) throws Exception {
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

    mapIndexes.put("#mtd_header#_model_idx", "model");
    mapIndexes.put("#mtd_header#_fcst_lead_idx", "fcst_lead");
    mapIndexes.put("#mtd_header#_fcst_valid_idx", "fcst_valid");
    mapIndexes.put("#mtd_header#_fcst_init_idx", "fcst_init");
    mapIndexes.put("#mtd_header#_fcst_rad_idx", "fcst_rad");
    mapIndexes.put("#mtd_header#_fcst_thr_idx", "fcst_thr");
    mapIndexes.put("#mtd_header#_fcst_var_idx", "fcst_var");
    mapIndexes.put("#mtd_header#_fcst_lev_idx", "fcst_lev");


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
    tableVarLengthTable.put("RELP", "line_data_relp_ens");
    tableVarLengthTable.put("PHIST", "line_data_phist_bin");
    tableVarLengthTable.put("ORANK", "line_data_orank_ens");
    tableVarLengthTable.put("ECLV", "line_data_eclv_pnt");

    tableLineDataFieldsTable = new HashMap<>();
    tableLineDataFieldsTable.put("CTC", "total, fy_oy, fy_on, fn_oy, fn_on, ");
    tableLineDataFieldsTable.put("GRAD", "total, fgbar, ogbar, mgbar, egbar, ");
    tableLineDataFieldsTable.put("sl1l2", "total, fbar, obar, fobar, ffbar, oobar, ");
    tableLineDataFieldsTable.put("sal1l2", "total, fabar, oabar, foabar, ffabar, ooabar, ");
    tableLineDataFieldsTable.put("vl1l2",
            "total, ufbar, vfbar, uobar, vobar, uvfobar, uvffbar, uvoobar, f_speed_bar, o_speed_bar, ");
    tableLineDataFieldsTable.put("val1l2", "total, ufabar, vfabar, uoabar, voabar, uvfoabar, uvffabar, uvooabar, ");
    tableLineDataFieldsTable.put("NBR_CNT", "total, fbs, fss, ");

    tableDataFileLU = new HashMap<>();
    tableDataFileLU.put("point_stat", 0);
    tableDataFileLU.put("grid_stat", 1);
    tableDataFileLU.put("mode_cts", 2);
    tableDataFileLU.put("mode_obj", 3);
    tableDataFileLU.put("wavelet_stat", 4);
    tableDataFileLU.put("ensemble_stat", 5);
    tableDataFileLU.put("vsdb_point_stat", 6);
    tableDataFileLU.put("stat", 7);
    tableDataFileLU.put("mtd_2d", 8);
    tableDataFileLU.put("mtd_3d_pc", 9);
    tableDataFileLU.put("mtd_3d_ps", 10);
    tableDataFileLU.put("mtd_3d_sc", 11);
    tableDataFileLU.put("mtd_3d_ss", 12);


    initVarLengthLineDataIds();
  }

  @Override
  public void applyIndexes() throws Exception {
    // what indexes do we need to apply in couchbase?
    // do nothing for now
  }

  @Override
  public void dropIndexes() throws Exception {
    // what indexes do we need to drop in couchbase?
    // do nothing for now
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
        logger.error(
            "  **  ERROR: caught " + e.getClass() + " applying index " + strIndexName + ": " + e.getMessage());
      }

      //  print out a performance message
      long intIndexTime = new Date().getTime() - intIndexStart;
      logger.info(MVUtil.padBegin(strIndexName + ": ", 36) + MVUtil.formatTimeSpan(intIndexTime));
    }
    logger.info("");
  }

  /**
   * Executes the input update statement against the database underlying the input Connection and
   * cleans up any resources upon completion.
   *
   * @param update SQL UPDATE statement to execute
   * @return Number of records affected (output of Statement.executeUpdate() call)
   * @throws Exception
   */
  private int executeUpdate(String update) throws Exception {

    int intRes;
   /* try (
            Connection con = getConnection();
            Statement stmt = con.createStatement()  ) {
       intRes = stmt.executeUpdate(update);

    } catch (Exception e) {
      logger.error(update);
      throw new Exception("caught Exception calling executeUpdate: " + e.getMessage());
    }
  */
    intRes = -1;
    return intRes;
  }

  /**
   * Initialize the table containing the max line_data_id for all line_data tables corresponding to
   * variable length rows. //* @param con database connection used to search against
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
   * Build and execute a query that retrieves the next table record id, whose name is specified by
   * the input field, from the specified input table. The statement is run against the input
   * Connection and the next available id is returned. // * @param con
   *
   * @param table Database table whose next available id is returned
   * @param field Field name of the table id record
   * @return Next available id
   * @throws Exception
   */
  private int getNextId(String table, String field) throws Exception {
    int intId = -1;
 /*   PreparedStatement pstmt = null;
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
*/
    return intId;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolStatHeaderTableCheck) or
   * by searching the stat_header table for a duplicate (specified by _boolStatHeaderDBCheck).
   * Records in line_data tables, stat_group tables and line_data_thresh tables are created from the
   * data in the input file. If necessary, records in the stat_header table are created as well.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
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
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    String headerIdString = null;
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
        mvLoadStatInsertData
            .setLineType(MVUtil.findValueInArray(listToken, headerNames, "LINE_TYPE"));
        if (!MVUtil.isValidLineType(mvLoadStatInsertData.getLineType())) {
          logger.warn(
              "  **  WARNING: unexpected line type: " + mvLoadStatInsertData.getLineType()
                  + "  the line will be ignored     ");
          continue;
        }

        if (info._boolLineTypeLoad && !info._tableLineTypeLoad
                                           .containsKey(mvLoadStatInsertData.getLineType())) {
          continue;
        }

        mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);


        //  parse the valid times
        Date dateFcstValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID_BEG"));
        Date dateFcstValidEnd = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID_END"));
        Date dateObsValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID_BEG"));
        Date dateObsValidEnd = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID_END"));

        //  format the valid times for the database insert
        String strFcstValidBeg = DATE_FORMAT.format(dateFcstValidBeg);
        String strFcstValidEnd = DATE_FORMAT.format(dateFcstValidEnd);
        String strObsValidBeg = DATE_FORMAT.format(dateObsValidBeg);
        String strObsValidEnd = DATE_FORMAT.format(dateObsValidEnd);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calFcstInitBeg.setTime(dateFcstValidBeg);
        calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
        Date dateFcstInitBeg = calFcstInitBeg.getTime();
        String strFcstInitBeg = DATE_FORMAT.format(dateFcstInitBeg);

        //  ensure that the interp_pnts field value is a reasonable integer
        String strInterpPnts = MVUtil.findValueInArray(listToken, headerNames, "INTERP_PNTS");
        if (strInterpPnts.equals("NA")) {
          strInterpPnts = "0";
        }

        String strLineType = mvLoadStatInsertData.getLineType();

        //  do not load matched pair lines or orank lines
        if ((!info._boolLoadMpr && strLineType.equals("MPR"))
                || (!info._boolLoadOrank && strLineType.equals("ORANK"))) {
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
                "  AND fcst_var = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_VAR") + "'\n" +
                "  AND fcst_lev = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_LEV") + "'\n" +
                //"  AND obs_var = '" +			listToken[10] + "'\n" +
                //"  AND obs_lev = '" +			listToken[11] + "'\n" +
                "  AND obtype = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                             "OBTYPE") + "'\n" +
                "  AND vx_mask = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "VX_MASK") + "'\n" +
                "  AND interp_mthd = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                  "INTERP_MTHD") + "'\n" +
                "  AND interp_pnts = " + strInterpPnts + "\n" +
                "  AND fcst_thresh = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                  "FCST_THRESH") + "'\n" +
                "  AND obs_thresh = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                 "OBS_THRESH") + "'";

        //  build the value list for the stat_header insert
        String strStatHeaderValueList = "";
        for (int i = 0; i < listStatHeaderValue.length; i++) {
          strStatHeaderValueList += (0 < i ? ", " : "") + "'" + listStatHeaderValue[i] + "'";
        }


        String strFileLine = strFilename + ":" + intLine;

        //  look for the header key in the table
        Integer intStatHeaderId = -1;
        if (statHeaders.containsKey(strStatHeaderValueList)) {
          headerIdString = statHeaders.get(strStatHeaderValueList);
        }
        //  if the stat_header does not yet exist, create one
        else {
          //  look for an existing stat_header record with the same information
          boolean boolFoundStatHeader = false;
          long intStatHeaderSearchBegin = new Date().getTime();
          if (info._boolStatHeaderDBCheck) {
            String strStatHeaderSelect = "SELECT\n  stat_header_id\nFROM\n  stat_header\nWHERE\n" + strStatHeaderWhereClause;
    /*        Connection con = null;
            Statement stmt = null;
            ResultSet res = null;
            try {
              con = getConnection();
              stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                         ResultSet.CONCUR_READ_ONLY);
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
              } catch (Exception e) {  }
              try {
                stmt.close();
              } catch (Exception e) {  }
              try {
                con.close();
              } catch (Exception e) {  }
            }  */
          }

          timeStats.put("headerSearchTime", timeStats.get("headerSearchTime") + new Date()
                                                                                    .getTime() - intStatHeaderSearchBegin);


          //  if the stat_header was not found, add it to the table
          if (!boolFoundStatHeader) {

            intStatHeaderId = intStatHeaderIdNext++;
            statHeaders.put(strStatHeaderValueList, headerIdString);

            //  build an insert statement for the mode header
            strStatHeaderValueList =
                Integer.toString(intStatHeaderId) + ", " +        //  stat_header_id
                    strStatHeaderValueList;

            //  insert the record into the stat_header database table
            String strStatHeaderInsert = "INSERT INTO stat_header VALUES (" + strStatHeaderValueList + ");";
            int intStatHeaderInsert;
            try {
              intStatHeaderInsert = executeUpdate(strStatHeaderInsert);
              if (1 != intStatHeaderInsert) {
                logger.warn(
                    "  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
                intStatHeaderId = null;
              } else {
                headerInserts++;
              }
            } catch (Exception e) {
              logger.error(e.getMessage());
              intStatHeaderId = null;
            }

          } else {
            statHeaders.put(strStatHeaderValueList, headerIdString);
          }
        }
        if (intStatHeaderId != null) {

          int intLineDataMax = listToken.length;
          String strLineDataId = "";
          dataRecords++;

          //  if the line type is of variable length, get the line_data_id
          boolean boolHasVarLengthGroups = MVUtil.lengthGroupIndices
                                               .containsKey(mvLoadStatInsertData.getLineType());

          //  determine the maximum token index for the data
          if (boolHasVarLengthGroups) {
            int intLineDataId = tableVarLengthLineDataId.get(strLineType);
            strLineDataId = Integer.toString(intLineDataId) + ", ";
            tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
            int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices
                                                   .get(mvLoadStatInsertData.getLineType());
            int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1,
                                                            listVarLengthGroupIndices1.length);
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
              listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;

            }

            if (mvLoadStatInsertData.getLineType().equals("RHIST") || mvLoadStatInsertData
                                                                          .getLineType()
                                                                          .equals("PSTD")) {
              intLineDataMax = intLineDataMax - Integer.valueOf(
                  listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
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
                  MVUtil.findValueInArray(listToken, headerNames,
                                          "OBS_LEAD") + ", " +        //  obs_lead
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
              logger.warn("  **  WARNING: alpha value NA with line type '" + mvLoadStatInsertData
                                                                                 .getLineType() + "'\n        " + mvLoadStatInsertData
                                                                                                                      .getFileLine());
            }
            strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
          } else if (!strAlpha.equals("NA")) {
            logger.warn(
                "  **  WARNING: unexpected alpha value '" + strAlpha + "' in line type '" + mvLoadStatInsertData
                                                                                                .getLineType() + "'\n        " + mvLoadStatInsertData
                                                                                                                                     .getFileLine());
          }

          //  add total and all of the stats on the rest of the line to the value list
          for (int i = headerNames.indexOf("LINE_TYPE") + 1; i < intLineDataMax; i++) {
            //  for the METv2.0 MPR line type, add the obs_sid
            if (headerNames.indexOf("LINE_TYPE") + 1 + 2 == i && "MPR".equals(
                mvLoadStatInsertData.getLineType()) && "V2.0".equals(strMetVersion)) {
              strLineDataValueList += ", 'NA'";
            }
            //  add the stats in order
            strLineDataValueList += ", '" + replaceInvalidValues(listToken[i]) + "'";
          }


          if (strLineType.equals("ORANK")) {
            //skip ensemble fields and get data for the rest
            int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices
                                                   .get(mvLoadStatInsertData.getLineType());
            int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1,
                                                            listVarLengthGroupIndices1.length);
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
              listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;
            }
            int extraFieldsInd = intLineDataMax + Integer.valueOf(
                listToken[listVarLengthGroupIndices[0]]) * listVarLengthGroupIndices[2];
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
              maxSize = 105;
              break;
            case "MPR":
              maxSize = 24;
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
            case "GRAD":
              maxSize = 18;
              break;
            case "PSTD":
              maxSize = 30;
              break;
            case "SSVAR":
              maxSize = 46;
              break;
            case "RHIST":
              maxSize = 17;
              break;
            case "VL1L2":
              maxSize = 20;
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
          strLineDataValueList = strLineDataValueList
                                     .substring(0, strLineDataValueList.length() - 1);

          //  add the values list to the line type values map
          List<String> listLineTypeValues = new ArrayList<>();
          if (mvLoadStatInsertData.getTableLineDataValues()
                  .containsKey(mvLoadStatInsertData.getLineType())) {
            listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues()
                                     .get(mvLoadStatInsertData.getLineType());
          }
          listLineTypeValues.add("(" + strLineDataValueList + ")");
          mvLoadStatInsertData.getTableLineDataValues()
              .put(mvLoadStatInsertData.getLineType(), listLineTypeValues);
          dataInserts++;


			/*
       * * * *  var_length insert  * * * *
			 */

          if (boolHasVarLengthGroups) {

            //  get the index information about the current line type
            int[] listVarLengthGroupIndices1 = MVUtil.lengthGroupIndices
                                                   .get(mvLoadStatInsertData.getLineType());
            int[] listVarLengthGroupIndices = Arrays.copyOf(listVarLengthGroupIndices1,
                                                            listVarLengthGroupIndices1.length);
            if (headerNames.indexOf("DESC") < 0) {
              //for old versions
              listVarLengthGroupIndices[0] = listVarLengthGroupIndices[0] - 1;
              listVarLengthGroupIndices[1] = listVarLengthGroupIndices[1] - 1;
            }
            int intGroupCntIndex = listVarLengthGroupIndices[0];
            int intGroupIndex = listVarLengthGroupIndices[1];
            int intGroupSize = listVarLengthGroupIndices[2];
            int intNumGroups = Integer.parseInt(listToken[intGroupCntIndex]);

            if (mvLoadStatInsertData.getLineType().equals("PCT")
                    || mvLoadStatInsertData.getLineType().equals("PJC")
                    || mvLoadStatInsertData.getLineType().equals("PRC")) {
              intNumGroups -= 1;
            }
            List<String> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues()
                                                .get(mvLoadStatInsertData.getLineType());
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
              if (mvLoadStatInsertData.getLineType().equals("RHIST")
                      || mvLoadStatInsertData.getLineType().equals("PSTD")) {
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
            mvLoadStatInsertData.getTableVarLengthValues()
                .put(mvLoadStatInsertData.getLineType(), listThreshValues);
          }

          //  if the insert threshhold has been reached, commit the stored data to the database
          if (info._intInsertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
            int[] listInserts = commitStatData(mvLoadStatInsertData);
            dataInserts += listInserts[INDEX_LINE_DATA];
            lengthInserts += listInserts[INDEX_VAR_LENGTH];
          }
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
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intStatHeaderLoadTime) + "\n" +
                      MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
          headerSearchTime) + "\n" +
                      MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(
          dblLinesPerMSec) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method
   * was designed to be called from loadStatFile(), which is responsible for building insert value
   * lists for the various types of grid_stat and point_stat database tables.
   *
   * @param mvLoadStatInsertData Data structure loaded with insert value lists
   * @return An array of four integers, indexed by the INDEX_* members, representing the number of
   * database inserts of each type
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
    for (Map.Entry<String, List<String>> entry : mvLoadStatInsertData.getTableLineDataValues()
                                                     .entrySet()) {
      mvLoadStatInsertData.setLineType(entry.getKey());
      ArrayList listValues = (ArrayList) entry.getValue();
      String strLineDataTable = "line_data_" + mvLoadStatInsertData.getLineType()
                                                   .toLowerCase(Locale.US);

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
        strStatGroupInsertValues += (i == 0 ? "" : ", ") + mvLoadStatInsertData
                                                               .getListStatGroupInsertValues()
                                                               .get(i);
      }
      String strStatGroupInsert = "INSERT INTO stat_group VALUES " + strStatGroupInsertValues + ";";
      int intStatGroupInsert = executeUpdate(strStatGroupInsert);
      if (mvLoadStatInsertData.getListStatGroupInsertValues().size() != intStatGroupInsert) {
        logger.warn(
            "  **  WARNING: unexpected result from stat_group INSERT: " + intStatGroupInsert + " vs. " +
                mvLoadStatInsertData.getListStatGroupInsertValues()
                    .size() + "\n        " + mvLoadStatInsertData.getFileLine());
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
      String[] listVarLengthValues = MVUtil.toArray(
          mvLoadStatInsertData.getTableVarLengthValues().get(listVarLengthType));
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
        logger.warn(
            "  **  WARNING: unexpected result from thresh INSERT: " + intThreshInsert + " vs. " +
                listVarLengthValues.length + "\n        " + mvLoadStatInsertData.getFileLine());
      }
      mvLoadStatInsertData.getTableVarLengthValues().put(listVarLengthType, new ArrayList<>());
    }

    return listInserts;
  }

  @Override
  public Map<String, Long> loadStatFileVSDB(DataFileInfo info, DatabaseInfo databaseInfo) throws Exception {

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
    long nextIdNumber = 0;
    String nextIdString = "";
    String headerIdString = "";
    JsonObject headerFile;
    JsonDocument response;
    JsonDocument doc;
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    N1qlQueryRow firstRow = null;
    JsonObject firstRowObject = null;

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
      SimpleDateFormat formatStatVsdb = new SimpleDateFormat("yyyyMMddHH", Locale.US);
      formatStatVsdb.setTimeZone(TimeZone.getTimeZone("UTC"));

      //  read in each line of the input file, remove "="
      while (reader.ready()) {

        String line = reader.readLine();
        try {
          line = line.replaceAll("=\\s", " "); // remove "= "
          Matcher m = Pattern.compile("\\d-0\\.").matcher(
              line); // some records do not have a space between columns if the value in column starts with "-"

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

          if (listToken[6].equals("BSS") || listToken[6].equals("ECON")
                  || listToken[6].equals("HIST") || listToken[6].equals("RELI")
                  || listToken[6].equals("RELP") || listToken[6].equals("RMSE")
                  || listToken[6].equals("RPS")) {
            modelName = modelName.split("\\/")[0] + ensValue;
          }

          //  if the line type load selector is activated, check that the current line type is on the list

          if (listToken[6].equals("RMSE")) {
            mvLoadStatInsertData.setLineType("CNT");
          } else if (listToken[6].equals("BSS")) {
            mvLoadStatInsertData.setLineType("PSTD");
          } else if (listToken[6].equals("HIST")) {
            mvLoadStatInsertData.setLineType("RHIST");
          } else if (listToken[6].equals("RELP")) {
            mvLoadStatInsertData.setLineType("RELP");
          } else if (listToken[6].equals("SL1L2")) {
            mvLoadStatInsertData.setLineType("SL1L2");
          } else if (listToken[6].equals("GRAD")) {
            mvLoadStatInsertData.setLineType("GRAD");
          } else if (listToken[6].equals("SAL1L2")) {
            mvLoadStatInsertData.setLineType("SAL1L2");
          } else if (listToken[6].equals("VL1L2")) {
            mvLoadStatInsertData.setLineType("VL1L2");
          } else if (listToken[6].equals("VAL1L2")) {
            mvLoadStatInsertData.setLineType("VAL1L2");
          } else if (listToken[6].equals("RPS")) {
            mvLoadStatInsertData.setLineType("ENSCNT");
          } else if (listToken[6].equals("ECON")) {
            mvLoadStatInsertData.setLineType("ECLV");
          } else if (listToken[6].equals("RELI")) {
            mvLoadStatInsertData.setLineType("PCT");
            int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
            thresh = "==1/" + String.valueOf(intGroupSize);
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
          if (info._boolLineTypeLoad && !info._tableLineTypeLoad
                                             .containsKey(mvLoadStatInsertData.getLineType())) {
            continue;
          }

          mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);

          //  parse the valid times


          Date dateFcstValidBeg = formatStatVsdb.parse(listToken[3]);

          //  format the valid times for the database insert
          String strFcstValidBeg = DATE_FORMAT.format(dateFcstValidBeg);


          //  calculate the number of seconds corresponding to fcst_lead
          String strFcstLead = listToken[2];
          int intFcstLeadSec = Integer.parseInt(strFcstLead) * 3600;

          //  determine the init time by combining fcst_valid_beg and fcst_lead
          Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
          calFcstInitBeg.setTime(dateFcstValidBeg);
          calFcstInitBeg.add(Calendar.SECOND, (-1) * intFcstLeadSec);
          Date dateFcstInitBeg = calFcstInitBeg.getTime();
          String strFcstInitBeg = DATE_FORMAT.format(dateFcstInitBeg);
          String strObsValidBeg = DATE_FORMAT.format(dateFcstValidBeg);
          String strFcstValidEnd = DATE_FORMAT.format(dateFcstValidBeg);
          String strObsValidEnd = DATE_FORMAT.format(dateFcstValidBeg);

          //  ensure that the interp_pnts field value is a reasonable integer
          String strInterpPnts = "0";

          String strLineType = mvLoadStatInsertData.getLineType();


			/*
       * * * *  stat_header insert  * * * *
			 */
          headerRecords++;

          //  build the value list for the stat_header search
          String strStatHeaderValueList = modelName + listToken[7] + listToken[8] +
                                          listToken[4] +listToken[5] + thresh;
          headerIdString = "";

          //  look for the header key in the table
          if (statHeaders.containsKey(strStatHeaderValueList)) {
            headerIdString = statHeaders.get(strStatHeaderValueList);
          }

          //  if the stat_header is not in the table
          else {

            //  look for an existing stat_header record with the same information
            boolean boolFoundStatHeader = false;
            long intStatHeaderSearchBegin = new Date().getTime();
            if (info._boolStatHeaderDBCheck) {
              // build a Couchbase query to look for duplicate stat_header records
              String strDataFileQuery =  "SELECT " +
                      "meta().id as headerFileId, " +
                      "type, " +
                      "header_type, " +
                      "data_type, " +
                      "data_id " +
                      "FROM `" +
                      getBucket().name() +
                      "` WHERE " +
                      "type = \'header\' AND " +
                      "`header_type` = \'stat\' AND " +
                      "`data_type` = \'vsdb_point_stat\' AND " +
                      "model = \'" + modelName + "\' AND " +
                      "`fcst_var` = \'" + listToken[7] + "\' AND " +
                      "`fcst_lev` = \'" + listToken[8] + "\' AND " +
                      "obtype = \'" + listToken[4] + "\' AND " +
                      "`vx_mask` = \'" + listToken[5] + "\' AND " +
                      "`fcst_thresh` = \'" + thresh + "\';";

              try {
                queryResult = getBucket().query(N1qlQuery.simple(strDataFileQuery));
                queryList = queryResult.allRows();
                // if the header document is already present in the database, print a warning and return the id
                if (queryList.size() > 0) {
                  firstRow = queryList.get(0);
                  firstRowObject = firstRow.value();
                  // set headerIdString to id of existing header document
                  headerIdString = firstRowObject.get("headerFileId").toString();
                  boolFoundStatHeader = true;
                  // add header to table
                  statHeaders.put(strStatHeaderValueList, headerIdString);
                  logger.warn("  **  WARNING: header document already present in database");
                }
                timeStats.put("headerSearchTime", timeStats.get("headerSearchTime") + new Date().getTime() -
                                                  intStatHeaderSearchBegin);
                } catch (CouchbaseException e) {
                throw new Exception(e.getMessage());
              }
            }  // end if (info._boolStatHeaderDBCheck)

            //  if the stat_header was not found, add it to the database and table
            if (!boolFoundStatHeader) {
              //  create a unique data_file id from a Couchbase counter, starting at 1 the first time
              try {
                nextIdNumber = getBucket().counter("HDCounter", 1, 1).content();
                // unique id must be a string
                headerIdString = databaseInfo.getDbName() + "::header::stat::" + modelName + "::" + String.valueOf(nextIdNumber);
              } catch (CouchbaseException e) {
                throw new Exception(e.getMessage());
              }

              try {
                headerFile = JsonObject.empty()
                        .put("type", "header")
                        .put("header_type", "stat")
                        .put("data_type", info._dataFileLuTypeName)
                        .put("data_id", info._dataFileId)
                        .put("version", listToken[0])
                        .put("model", modelName)
                        .put("descr", "NA")
                        .put("fcst_var", listToken[7])
                        .put("fcst_lev", listToken[8])
                        .put("obs_var", listToken[7])
                        .put("obs_lev", listToken[8])
                        .put("obtype", listToken[4])
                        .put("vx_mask", listToken[5])
                        .put("interp_mthd", "NA")
                        .put("interp_pnts", strInterpPnts)
                        .put("fcst_thresh", thresh)
                        .put("obs_thresh", thresh);

                doc = JsonDocument.create(headerIdString, headerFile);
                response = getBucket().upsert(doc);
                if (response.content().isEmpty()) {
                  logger.warn("  **  WARNING: unexpected result from header INSERT");
                } else {
                  headerInserts++;
                }
              } catch (Exception e) {
                throw new Exception(e.getMessage());
              }
              // add header to table
              statHeaders.put(strStatHeaderValueList, headerIdString);
            } // end if (!boolFoundStatHeader)
          } // end else stat_header is not in table

          if (headerIdString != null) {
            String strLineDataId = "";
            dataRecords++;
            //  if the line type is of variable length, get the line_data_id
            boolean boolHasVarLengthGroups = MVUtil.lengthGroupIndices.containsKey(strLineType);
            //  determine the maximum token index for the data
            if (boolHasVarLengthGroups) {
              int intLineDataId = tableVarLengthLineDataId.get(strLineType);
              strLineDataId = Integer.toString(intLineDataId) + ", ";
              tableVarLengthLineDataId.put(strLineType, intLineDataId + 1);
            }

            //  build the value list for the insert statment
            String strLineDataValueList =
                    databaseInfo.getDbName() + ", " +     // database name for ID
                            strLineType.toLowerCase() + "," +   // line type
                            modelName + ", " +            // model name for ID
                            headerIdString + "," +       //  CB header_id
                            info._dataFileId + "," +     //  CB data_id for data_file
                    intLine + ", " +          //  line_num
                    strFcstLead + ", " +        //  fcst_lead
                            strFcstValidBeg + "," +    //  fcst_valid_beg
                            strFcstValidEnd + "," +    //  fcst_valid_end
                            strFcstInitBeg + "," +     //  fcst_init_beg
                            "00" + "," +               //  obs_lead
                            strObsValidBeg + "," +     //  obs_valid_beg
                            strObsValidEnd;            //  obs_valid_end

            //  if the line data requires a cov_thresh value, add it
            String strCovThresh = "NA";
            if (MVUtil.covThreshLineTypes.containsKey(strLineType)) {
              strLineDataValueList += "," + replaceInvalidValues(strCovThresh);
            }

            //  if the line data requires an alpha value, add it
            String strAlpha = "-9999";
            if (MVUtil.alphaLineTypes.containsKey(strLineType)) {
              if (strAlpha.equals("NA")) {
                logger.warn("  **  WARNING: alpha value NA with line type '" + strLineType + "'\n        " + mvLoadStatInsertData
                                                                                                                        .getFileLine());
              }
              strLineDataValueList += ", " + replaceInvalidValues(strAlpha);
            }

            if (listToken[6].equals("RMSE")) {//CNT line type
              for (int i = 0; i < 94; i++) {
                if (i == 53) {
                  strLineDataValueList += "," + listToken[10];
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
              for (int i = 0; i < 17; i++) {
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
                  case 16:
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
                    strLineDataValueList += ", -9999";
                    break;
                  case 15:
                    strLineDataValueList += ", '" + listToken[12] + "'";
                    break;
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                    strLineDataValueList += ", -9999";
                    break;
                  case 20:
                    strLineDataValueList += ", '" + listToken[13] + "'";
                    break;
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                    strLineDataValueList += ", -9999";
                    break;
                  case 25:
                    strLineDataValueList += ", '" + listToken[14] + "'";
                    break;
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                    strLineDataValueList += ", -9999";
                    break;
                  default:

                }
              }

            }

            if (listToken[6].equals("HIST")) { //RHIST line type
              for (int i = 0; i < 6; i++) {
                if (i == 3) {
                  int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                  strLineDataValueList += ", '" + intGroupSize + "'";
                } else if (i == 0) {//total
                  strLineDataValueList += ", 0";
                } else {
                  strLineDataValueList += ", -9999";
                }
              }
            }

            if (listToken[6].equals("RELP")) {  // RELP line type
              strLineDataValueList += ", 0";
              int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]);
              strLineDataValueList += ", '" + intGroupSize + "'";
            }
            if (listToken[6].equals("ECON")) {  // ECLV line type
              strLineDataValueList += ", 0, -9999, -9999";
              int intGroupSize = 18;
              strLineDataValueList += ", '" + intGroupSize + "'";
            }


            if (listToken[6].equals("RELI")) { //PCT line type
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

            if (listToken[6].equals("SL1L2")
                    || listToken[6].equals("SAL1L2")) {//SL1L2,SAL1L2 line types
              for (int i = 0; i < 7; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    strLineDataValueList += ","
                                                + (Double.valueOf(listToken[i + 9])).intValue();
                  } else {
                    strLineDataValueList += "," + Double.valueOf(listToken[i + 9]);
                  }

                } else {
                  strLineDataValueList += ",-9999";
                }
              }
            }
            if (listToken[6].equals("VAL1L2")
                    || listToken[6].equals("GRAD")) {//VAL1L2,GRAD line type
              for (int i = 0; i < 8; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    strLineDataValueList += ","
                                                + (Double.valueOf(listToken[i + 9])).intValue();
                  } else {
                    strLineDataValueList += "," + Double.valueOf(listToken[i + 9]);
                  }
                } else {
                  strLineDataValueList += ",-9999";
                }

              }
            }
            if (listToken[6].equals("VL1L2")) {//VL1L2
              for (int i = 0; i < 10; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    strLineDataValueList += ","
                                                + (Double.valueOf(listToken[i + 9])).intValue();
                  } else {
                    strLineDataValueList += "," + Double.valueOf(listToken[i + 9]);
                  }
                } else {
                  strLineDataValueList += ",-9999";
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
                  strLineDataValueList += "," + Math.max(0, fn_on);
                } else if (i == 3) {
                  strLineDataValueList += "," + Math.max(0, fn_oy);
                } else if (i == 2) {
                  strLineDataValueList += "," + Math.max(0, fy_on);
                } else if (i == 1) {
                  strLineDataValueList += "," + Math.max(0, fy_oy);
                } else if (i == 0) {//total,
                  strLineDataValueList += "," + listToken[9];
                }

              }
            }
            if (listToken[6].startsWith("FSS")) {//NBRCNT line type
              double fss = -9999;
              if (listToken.length > 11) {
                fss = 1 - Double.valueOf(listToken[10])
                              / (Double.valueOf(listToken[11]) + Double.valueOf(listToken[12]));
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
            if (mvLoadStatInsertData.getTableLineDataValues()
                    .containsKey(strLineType)) {
              listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues()
                                       .get(strLineType);
            }
            listLineTypeValues.add(strLineDataValueList);
            mvLoadStatInsertData.getTableLineDataValues()
                .put(strLineType, listLineTypeValues);
            dataInserts++;


			/*
       * * * *  var_length insert  * * * *
			 */

            if (boolHasVarLengthGroups) {
              //  get the index information about the current line type
              int intGroupIndex = 0;
              int intGroupSize = 0;
              int intNumGroups = 0;

              if (listToken[6].equals("HIST")) {//RHIST line type
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                } catch (Exception e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("RELP")) {//RELP line type)
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]);
                } catch (Exception e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("ECON")) {//ECLV line type)
                intGroupIndex = 9;
                intNumGroups = 18;
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

              List<String> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues()
                                                  .get(strLineType);
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
              } else if (listToken[6].equals("RELP")) {
                for (int i = 0; i < intNumGroups; i++) {
                  StringBuilder strThreshValues = new StringBuilder("(");
                  strThreshValues.append(strLineDataId).append(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      strThreshValues.append(", ").append(res);
                    }

                  }
                  strThreshValues.append(')');
                  listThreshValues.add(strThreshValues.toString());
                  lengthRecords++;
                }
              } else if (listToken[6].equals("ECON")) {

                for (int i = 0; i < intNumGroups; i++) {
                  StringBuilder strThreshValues = new StringBuilder("(");
                  strThreshValues.append(strLineDataId).append(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      strThreshValues.append(", ").append(X_POINTS_FOR_ECON[i]).append(",")
                          .append(res);
                    }

                  }
                  strThreshValues.append(')');
                  listThreshValues.add(strThreshValues.toString());
                  lengthRecords++;
                }
              }

              mvLoadStatInsertData.getTableVarLengthValues()
                  .put(strLineType, listThreshValues);
            }

            //  if the insert threshhold has been reached, commit the stored data to the database
            if (info._intInsertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
              int[] listInserts = commitStatData(mvLoadStatInsertData);
              dataInserts += listInserts[INDEX_LINE_DATA];
              lengthInserts += listInserts[INDEX_VAR_LENGTH];
            }
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

    timeStats.put("linesTotal", (long) (intLine));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);

    //  print a performance report
    long intStatHeaderLoadTime = new Date().getTime() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine) / (double) (intStatHeaderLoadTime);

    if (info._boolVerbose) {
      logger.info(MVUtil.padBegin("file lines: ", 6) + (intLine) + "\n" +
                      MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
                      MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
                      MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
                      MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
                      MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
                      MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
                      MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intStatHeaderLoadTime) + "\n" +
                      MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
          headerSearchTime) + "\n" +
                      MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf.format(
          dblLinesPerMSec) + "\n\n");
    }
    logger.info("intLine " + intLine);
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
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
          throw new Exception("METViewer load error: loadModeFile() unable to determine line type "
                                  + MVUtil.findValueInArray(listToken,headerNames,"OBJECT_ID")
                                  + "\n        " + strFileLine);
        }


			/*
       * * * *  mode_header insert  * * * *
			 */

        //  parse the valid times
        Date dateFcstValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID"));
        Date dateObsValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID"));

        //  format the valid times for the database insert
        String strFcstValidBeg = DATE_FORMAT.format(dateFcstValidBeg);
        String strObsValidBeg = DATE_FORMAT.format(dateObsValidBeg);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(
            strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calFcstInitBeg.setTime(dateFcstValidBeg);
        calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
        Date dateFcstInitBeg = calFcstInitBeg.getTime();
        String strFcstInit = DATE_FORMAT.format(dateFcstInitBeg);

        //  build a value list from the header information
        //replace "NA" for fcst_accum (listToken[4]) and obs_accum (listToken[7]) to NULL

        String strModeHeaderValueList =
            "'" + MVUtil.findValueInArray(listToken, headerNames,
                                          "VERSION") + "', " +      //  version
                "'" + MVUtil.findValueInArray(listToken, headerNames,
                                              "MODEL") + "', ";       //  model

        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "N_VALID"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  N_VALID
        } else {
          strModeHeaderValueList = strModeHeaderValueList
                                       + MVUtil.findValueInArray(listToken,headerNames,"N_VALID")
                                       + ", ";      //  N_VALID
        }
        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "GRID_RES"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  GRID_RES
        } else {
          strModeHeaderValueList = strModeHeaderValueList
                                       + MVUtil.findValueInArray(listToken,headerNames,"GRID_RES")
                                       + ", ";      //  GRID_RES
        }

        strModeHeaderValueList = strModeHeaderValueList
                                     + "'" + MVUtil.findValueInArray(listToken,headerNames,"DESC")
                                     + "', " +      //  GRID_RES
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,"FCST_LEAD")
                                     + "', " +      //  fcst_lead
                                     "'" + strFcstValidBeg + "', ";      //  fcst_valid
        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "FCST_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  fcst_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList
                                       + "'"
                                       + MVUtil.findValueInArray(listToken,headerNames,"FCST_ACCUM")
                                       + "', ";      //  fcst_accum
        }
        strModeHeaderValueList = strModeHeaderValueList + "'" + strFcstInit + "', " +        //  fcst_init
                                     "'"
                                     + MVUtil.findValueInArray(listToken, headerNames,"OBS_LEAD")
                                     + "', " +      //  obs_lead
                                     "'" + strObsValidBeg + "', ";      //  obs_valid
        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "OBS_ACCUM"))) {
          strModeHeaderValueList = strModeHeaderValueList + "NULL" + ", ";      //  obs_accum
        } else {
          strModeHeaderValueList = strModeHeaderValueList
                                       + "'"
                                       + MVUtil.findValueInArray(listToken,headerNames,"OBS_ACCUM")
                                       + "', ";      //  obs_accum
        }
        strModeHeaderValueList = strModeHeaderValueList
                                     + "'"
                                     + MVUtil.findValueInArray(listToken,headerNames,"FCST_RAD")
                                     + "', " +      //  fcst_rad
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "FCST_THR") + "', " +      //  fcst_thr
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "OBS_RAD") + "', " +      //  obs_rad
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "OBS_THR") + "', " +      //  obs_thr
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "FCST_VAR") + "', " +      //  fcst_var
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "FCST_LEV") + "', " +      //  fcst_lev
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "OBS_VAR") + "', " +      //  obs_var
                                     "'" + MVUtil.findValueInArray(listToken, headerNames,
                                                                   "OBS_LEV") + "'";        //  obs_lev

        String strModeHeaderWhereClause =
            "  version = '" + MVUtil.findValueInArray(listToken, headerNames, "VERSION") + "'\n" +
                "  AND model = '" + MVUtil
                                        .findValueInArray(listToken, headerNames, "MODEL") + "'\n" +
                "  AND n_valid = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "N_VALID") + "'\n" +
                "  AND grid_res = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "GRID_RES") + "'\n" +
                "  AND descr = '" + MVUtil
                                        .findValueInArray(listToken, headerNames, "DESC") + "'\n" +
                "  AND fcst_lead = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                "FCST_LEAD") + "'\n" +
                "  AND fcst_valid = '" + strFcstValidBeg + "'\n" +
                "  AND fcst_accum = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                 "FCST_ACCUM") + "'\n" +
                "  AND fcst_init = '" + strFcstInit + "'\n" +
                "  AND obs_lead = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "OBS_LEAD") + "'\n" +
                "  AND obs_valid = '" + strObsValidBeg + "'\n" +
                "  AND obs_accum = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                                "OBS_ACCUM") + "'\n" +
                "  AND fcst_rad = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_RAD") + "'\n" +
                "  AND fcst_thr = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_THR") + "'\n" +
                "  AND obs_rad = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_RAD") + "'\n" +
                "  AND obs_thr = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_THR") + "'\n" +
                "  AND fcst_var = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_VAR") + "'\n" +
                "  AND fcst_lev = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_LEV") + "'\n" +
                "  AND obs_var = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_VAR") + "'\n" +
                "  AND obs_lev = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_LEV") + "';";

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
            String strModeHeaderSelect = "SELECT\n  mode_header_id\nFROM\n  mode_header\nWHERE\n"
                                             + strModeHeaderWhereClause;
      /*      try (Connection con = getBucket();
                 Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                      ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(strModeHeaderSelect)) {
              if (res.next()) {
                String strModeHeaderIdDup = res.getString(1);
                intModeHeaderId = Integer.parseInt(strModeHeaderIdDup);
                boolFoundModeHeader = true;
                logger.warn(
                    "  **  WARNING: found duplicate mode_header record with id " + strModeHeaderIdDup + "\n        " + strFileLine);
              }
              res.close();
              stmt.close();
              con.close();
            } catch (Exception e) {
              logger.error(e.getMessage());
            }  */

          }
          timeStats.put("headerSearchTime", timeStats.get("headerSearchTime")
                                                + new Date().getTime() - intModeHeaderSearchBegin);


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
            String strModeHeaderInsert = "INSERT INTO mode_header VALUES ("
                                             + strModeHeaderValueList + ");";
            int intModeHeaderInsert = executeUpdate(strModeHeaderInsert);
            if (1 != intModeHeaderInsert) {
              logger.warn(
                  "  **  WARNING: unexpected result from mode_header INSERT: "
                      + intModeHeaderInsert + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


			/*
       * * * *  mode_cts insert  * * * *
			 */

        if (modeCts == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          String strCTSValueList = intModeHeaderId + ", '"
                                       + MVUtil.findValueInArray(listToken,headerNames,"FIELD")
                                       + "'";
          int totalIndex = headerNames.indexOf("TOTAL");
          for (int i = 0; i < 18; i++) {
            strCTSValueList += ", " + replaceInvalidValues(listToken[totalIndex + i]);
          }

          //  insert the record into the mode_cts database table
          String strModeCtsInsert = "INSERT INTO mode_cts VALUES (" + strCTSValueList + ");";
          int intModeCtsInsert = executeUpdate(strModeCtsInsert);
          if (1 != intModeCtsInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_cts INSERT: "
                    + intModeCtsInsert + "\n        " + strFileLine);
          }
          ctsInserts++;

        }

			/*
       * * * *  mode_obj_single insert  * * * *
			 */

        else if (modeSingle == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          int intModeObjId = intModeObjIdNext++;
          String strSingleValueList = intModeObjId + ", " + intModeHeaderId + ", '"
                                          + strObjectId + "'";
          for (String header : modeObjSingleColumns) {
            strSingleValueList += ", '" + replaceInvalidValues(
                MVUtil.findValueInArray(listToken, headerNames, header)) + "'";
          }

          //set flags
          Integer simpleFlag = 1;
          Integer fcstFlag = 0;
          if (strObjectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (strObjectId.startsWith("CF") || strObjectId.startsWith("F")) {
            fcstFlag = 1;
          }
          Integer matchedFlag = 0;
          String[] objCatArr = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          if (objCatArr.length == 1 && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }
          strSingleValueList = strSingleValueList + "," + fcstFlag + "," + simpleFlag + ","
                                   + matchedFlag;

          //  insert the record into the mode_obj_single database table
          String strModeObjSingleInsert = "INSERT INTO mode_obj_single VALUES ("
                                              + strSingleValueList + ");";
          int intModeObjSingleInsert = executeUpdate(strModeObjSingleInsert);
          if (1 != intModeObjSingleInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_obj_single INSERT: "
                    + intModeObjSingleInsert + "\n        " + strFileLine);
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
          String strPairValueList = intModeObjectIdObs + ", " + intModeObjectIdFcst
                                        + ", " + intModeHeaderId + ", " +
                                        "'" + strObjectId + "', '"
                                        + MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT")
                                        + "'";
          int centroiddistIndex = headerNames.indexOf("CENTROID_DIST");
          for (int i = 0; i < 12; i++) {
            strPairValueList += ", " + replaceInvalidValues(listToken[centroiddistIndex + i]);
          }

          //set flags
          Integer simpleFlag = 1;
          String[] objIdArr = strObjectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          Integer matchedFlag = 0;
          String[] objCatArr = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          if (objCatArr.length == 2 && objCatArr[0].substring(2).equals(objCatArr[1].substring(2))
                  && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }
          strPairValueList = strPairValueList + "," + simpleFlag + "," + matchedFlag;

          //  insert the record into the mode_obj_pair database table
          String strModeObjPairInsert = "INSERT INTO mode_obj_pair VALUES ("
                                            + strPairValueList + ");";
          int intModeObjPairInsert = executeUpdate(strModeObjPairInsert);
          if (1 != intModeObjPairInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mode_obj_pair INSERT: "
                    + intModeObjPairInsert + "\n        " + strFileLine);
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
                      (info._boolModeHeaderDBCheck ? MVUtil.padBegin("mode_header search time: ",
                                                                     36) + MVUtil.formatTimeSpan(
                          timeStats.get("headerSearchTime")) + "\n" : "") +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intModeHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws Exception
   */
  @Override
  public Map<String, Long> loadMtdFile(DataFileInfo info) throws Exception {
    Map<String, Long> timeStats = new HashMap<>();

    //  performance counters
    long intMtdHeaderLoadStart = new Date().getTime();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long obj3dSingleInserts = 0;
    long obj3dPairInserts = 0;
    long obj2dInserts = 0;

    //  get the next mode record ids from the database
    int intMtdHeaderIdNext = getNextId("mtd_header", "mtd_header_id");

    //  set up the input file for reading
    String strFilename = info._dataFilePath + "/" + info._dataFileFilename;
    int intLine = 1;
    List<String> headerNames = new ArrayList<>();
    try (
            FileReader fileReader = new FileReader(strFilename);
            BufferedReader reader = new BufferedReader(fileReader)) {
      //  read each line of the input file
      while (reader.ready()) {
        String lineStr = reader.readLine().trim();
        String[] listToken = lineStr.split("\\s+");

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
        int mtd3dSingle = 17;
        int mtd3dPair = 18;
        int mtd2d = 19;
        if (11 == intDataFileLuId || 12 == intDataFileLuId) {
          intLineTypeLuId = mtd3dSingle;
        } else if (9 == intDataFileLuId || 10 == intDataFileLuId) {
          intLineTypeLuId = mtd3dPair;
        } else if (8 == intDataFileLuId) {
          intLineTypeLuId = mtd2d;
        } else {
          throw new Exception("METViewer load error: loadModeFile() unable to determine line type"
                                  + " " + strFileLine);
        }
        //  parse the valid times
        Date dateFcstValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "FCST_VALID"));
        Date dateObsValidBeg = DB_DATE_STAT_FORMAT.parse(
            MVUtil.findValueInArray(listToken, headerNames, "OBS_VALID"));

        //  format the valid times for the database insert
        String strFcstValidBeg = DATE_FORMAT.format(dateFcstValidBeg);
        String strObsValidBeg = DATE_FORMAT.format(dateObsValidBeg);

        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = 0;
        try {
          intFcstLeadSec = Integer.parseInt(
              strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
          intFcstLeadSec += Integer.parseInt(
              strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
          intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 6,
                                                                   intFcstLeadLen - 4)) * 3600;
        } catch (Exception e) {
        }
        String fcstLeadInsert = MVUtil.findValueInArray(listToken, headerNames, "FCST_LEAD");
        if (fcstLeadInsert.equals("NA")) {
          fcstLeadInsert = "0";
        } else {
          if (fcstLeadInsert.contains("_")) {
            fcstLeadInsert = fcstLeadInsert.split("_")[1];
          }
        }

        String obsLeadInsert = MVUtil.findValueInArray(listToken, headerNames, "OBS_LEAD");
        if (obsLeadInsert.equals("NA")) {
          obsLeadInsert = "0";
        } else {
          if (obsLeadInsert.contains("_")) {
            obsLeadInsert = obsLeadInsert.split("_")[1];
          }
        }

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        Calendar calFcstInitBeg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calFcstInitBeg.setTime(dateFcstValidBeg);
        calFcstInitBeg.add(Calendar.SECOND, -1 * intFcstLeadSec);
        Date dateFcstInitBeg = calFcstInitBeg.getTime();
        String strFcstInit = DATE_FORMAT.format(dateFcstInitBeg);


        String mtdHeaderValueList = "'" + MVUtil.findValueInArray(listToken, headerNames, "VERSION")
                                        + "', " + "'"
                                        + MVUtil.findValueInArray(listToken, headerNames, "MODEL")
                                        + "', " + "'"
                                        + MVUtil.findValueInArray(listToken, headerNames, "DESC")
                                        + "', ";


        mtdHeaderValueList = mtdHeaderValueList
                                 + "'"
                                 + fcstLeadInsert
                                 + "', " + "'" + strFcstValidBeg + "', "
                                 + "'" + strFcstInit + "', "
                                 + "'" + obsLeadInsert
                                 + "', " + "'" + strObsValidBeg + "', ";

        if ("NA".equals(MVUtil.findValueInArray(listToken, headerNames, "T_DELTA"))) {
          mtdHeaderValueList = mtdHeaderValueList + "NULL" + ", ";
        } else {
          mtdHeaderValueList = mtdHeaderValueList + "'"
                                   + MVUtil.findValueInArray(listToken, headerNames, "T_DELTA")
                                   + "', ";
        }
        mtdHeaderValueList = mtdHeaderValueList
                                 + "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_RAD")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_THR")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_RAD")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_THR")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_VAR")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "FCST_LEV")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_VAR")
                                 + "', " +
                                 "'" + MVUtil.findValueInArray(listToken, headerNames, "OBS_LEV")
                                 + "'";

        String mtdHeaderWhereClause =
            "  version = '" + MVUtil.findValueInArray(listToken, headerNames, "VERSION")
                + "'\n"
                + "  AND model = '" + MVUtil.findValueInArray(listToken, headerNames, "MODEL")
                + "'\n"
                + "  AND descr = '" + MVUtil.findValueInArray(listToken, headerNames, "DESC")
                + "'\n"
                + "  AND fcst_lead = '" + fcstLeadInsert + "'\n" +
                "  AND fcst_valid = '" + strFcstValidBeg + "'\n" +
                "  AND t_delta = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "T_DELTA") + "'\n" +
                "  AND fcst_init = '" + strFcstInit + "'\n" +
                "  AND obs_lead = '" + obsLeadInsert + "'\n" +
                "  AND obs_valid = '" + strObsValidBeg + "'\n" +

                "  AND fcst_rad = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_RAD") + "'\n" +
                "  AND fcst_thr = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_THR") + "'\n" +
                "  AND obs_rad = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_RAD") + "'\n" +
                "  AND obs_thr = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_THR") + "'\n" +
                "  AND fcst_var = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_VAR") + "'\n" +
                "  AND fcst_lev = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                               "FCST_LEV") + "'\n" +
                "  AND obs_var = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_VAR") + "'\n" +
                "  AND obs_lev = '" + MVUtil.findValueInArray(listToken, headerNames,
                                                              "OBS_LEV") + "';";

        //  look for the header key in the table
        int mtdHeaderId = -1;
        if (mtdHeaders.containsKey(mtdHeaderValueList)) {
          mtdHeaderId = mtdHeaders.get(mtdHeaderValueList);
        }

        //  if the mtd_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean foundMtdHeader = false;
          long mtdHeaderSearchBegin = new Date().getTime();
          if (info._boolMtdHeaderDBCheck) {
            String strMtdHeaderSelect = "SELECT\n  mtd_header_id\nFROM\n  mtd_header\nWHERE\n" +
                                            mtdHeaderWhereClause;
    /*        try (Connection con = getBucket();
                 Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                      ResultSet.CONCUR_READ_ONLY);
                 ResultSet res = stmt.executeQuery(strMtdHeaderSelect)) {
              if (res.next()) {
                String strMtdHeaderIdDup = res.getString(1);
                mtdHeaderId = Integer.parseInt(strMtdHeaderIdDup);
                foundMtdHeader = true;
                logger.warn(
                    "  **  WARNING: found duplicate mtd_header record with id " +
                        strMtdHeaderIdDup + "\n        " + strFileLine);
              }
              res.close();
              stmt.close();
              con.close();
            } catch (Exception e) {
              logger.error(e.getMessage());
            }  */

          }
          timeStats.put("headerSearchTime", timeStats.get("headerSearchTime")
                                                + new Date().getTime() - mtdHeaderSearchBegin);


          //  if the mtd_header was not found, add it to the table
          if (!foundMtdHeader) {

            mtdHeaderId = intMtdHeaderIdNext++;
            mtdHeaders.put(mtdHeaderValueList, mtdHeaderId);

            //  build an insert statement for the mtd header
            mtdHeaderValueList =
                mtdHeaderId + ", " +
                    intLineTypeLuId + ", " +
                    info._dataFileId + ", " +
                    intLine + ", " +
                    mtdHeaderValueList;

            //  insert the record into the mtd_header database table
            String strMtdHeaderInsert = "INSERT INTO mtd_header VALUES (" + mtdHeaderValueList +
                                            ");";
            int intMtdHeaderInsert = executeUpdate(strMtdHeaderInsert);
            if (1 != intMtdHeaderInsert) {
              logger.warn(
                  "  **  WARNING: unexpected result from mtd_header INSERT: " + intMtdHeaderInsert
                      + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


        if (mtd3dSingle == intLineTypeLuId) {
          String str3dSingleValueList = mtdHeaderId + ", '" + strObjectId + "'";
          for (String header : mtdObj3dSingleColumns) {
            str3dSingleValueList += ", '" + replaceInvalidValues(
                MVUtil.findValueInArray(listToken, headerNames, header)) + "'";
          }

          //set flags
          Integer simpleFlag = 1;
          Integer fcstFlag = 0;
          if (strObjectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (strObjectId.startsWith("CF") || strObjectId.startsWith("F")) {
            fcstFlag = 1;
          }
          Integer matchedFlag = 0;
          String objCat = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT");
          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (Exception e) {
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }
          str3dSingleValueList = str3dSingleValueList + "," + fcstFlag + "," + simpleFlag + ","
                                     + matchedFlag;

          //  insert the record into the mtd_obj_single database table
          int mtd3dObjSingleInsert = executeUpdate("INSERT INTO mtd_3d_obj_single VALUES ("
                                                       + str3dSingleValueList + ");");
          if (1 != mtd3dObjSingleInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_3d_obj_single INSERT: "
                    + mtd3dObjSingleInsert + "\n        " + strFileLine);
          }
          obj3dSingleInserts++;
        } else if (mtd2d == intLineTypeLuId) {
          String str2dValueList = mtdHeaderId + ", '" + strObjectId + "'";
          for (String header : mtdObj2dColumns) {
            str2dValueList += ", '" + replaceInvalidValues(
                MVUtil.findValueInArray(listToken, headerNames, header)) + "'";
          }

          //set flags
          Integer simpleFlag = 1;
          Integer fcstFlag = 0;
          if (strObjectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (strObjectId.startsWith("CF") || strObjectId.startsWith("F")) {
            fcstFlag = 1;
          }
          Integer matchedFlag = 0;
          String objCat = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT");

          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (Exception e) {
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }
          str2dValueList = str2dValueList + "," + fcstFlag + "," + simpleFlag + "," + matchedFlag;

          //  insert the record into the mtd_obj_single database table
          int mtd2dObjInsert = executeUpdate("INSERT INTO mtd_2d_obj VALUES ("
                                                 + str2dValueList + ");");
          if (1 != mtd2dObjInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_2d_obj INSERT: "
                    + mtd2dObjInsert + "\n        " + strFileLine);
          }
          obj2dInserts++;
        } else if (mtd3dPair == intLineTypeLuId) {

          //  build the value list for the mode_cts insert
          String str3dPairValueList = mtdHeaderId + ", "
                                          + "'"
                                          + strObjectId
                                          + "', '"
                                          + MVUtil.findValueInArray(listToken, headerNames,
                                                                    "OBJECT_CAT")
                                          + "'";
          int spaceCentroidDistIndex = headerNames.indexOf("SPACE_CENTROID_DIST");
          for (int i = 0; i < 11; i++) {
            str3dPairValueList += ", " + replaceInvalidValues(
                listToken[spaceCentroidDistIndex + i]);
          }

          //set flags
          Integer simpleFlag = 1;
          String[] objIdArr = strObjectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          Integer matchedFlag = 0;
          String[] objCatArr = MVUtil.findValueInArray(listToken, headerNames, "OBJECT_CAT")
                                   .split("_");
          Integer num1 = null;
          Integer num2 = null;
          try {
            num1 = Integer.valueOf(objCatArr[0].substring(objCatArr[0].length() - 3));
            num2 = Integer.valueOf(objCatArr[1].substring(objCatArr[1].length() - 3));
          } catch (Exception e) {
          }
          if (num1.equals(num2) && num1 != 0) {
            matchedFlag = 1;
          }
          str3dPairValueList = str3dPairValueList + "," + simpleFlag + "," + matchedFlag;

          int mtd3dObjPairInsert = executeUpdate("INSERT INTO mtd_3d_obj_pair VALUES ("
                                                     + str3dPairValueList + ");");
          if (1 != mtd3dObjPairInsert) {
            logger.warn(
                "  **  WARNING: unexpected result from mtd_3d_obj_pair INSERT: " +
                    mtd3dObjPairInsert + "\n        " + strFileLine);
          }
          obj3dPairInserts++;

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
    timeStats.put("obj3dSingleInserts", obj3dSingleInserts);
    timeStats.put("obj3dPairInserts", obj3dPairInserts);
    timeStats.put("obj2dInserts", obj2dInserts);


    //  print a performance report
    if (info._boolVerbose) {
      long intMtdHeaderLoadTime = new Date().getTime() - intMtdHeaderLoadStart;
      logger.info(MVUtil.padBegin("mtd_header inserts: ", 36) + headerInserts + "\n" +
                      MVUtil
                          .padBegin("mtd_3d_obj_single inserts: ", 36) + obj3dSingleInserts + "\n" +
                      MVUtil.padBegin("mtd_3d_obj_pair inserts: ", 36) + obj3dPairInserts + "\n" +
                      MVUtil.padBegin("mtd_2d_obj inserts: ", 36) + obj2dInserts + "\n" +
                      (info._boolMtdHeaderDBCheck ? MVUtil.padBegin("mtd_header search time: ",
                                                                    36) + MVUtil.formatTimeSpan(
                          timeStats.get("headerSearchTime")) + "\n" : "") +
                      MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
          intMtdHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  @Override
  public void updateGroup(String group) throws Exception {

  }

  @Override
  public void updateDescription(String description) throws Exception {

  }

  private int executeBatch(List<String> listValues, String strLineDataTable) throws Exception {
    long nextIdNumber;
    String lineDataIdString;
    JsonObject lineDataFile;
    lineDataFile = null;
    JsonDocument response;
    JsonDocument doc;
    String[] listFieldsArr;
    String[] listValuesArr;

    int intResLineDataInsert = 0;

    for (int i = 0; i < listValues.size(); i++) {

      listValuesArr = listValues.get(i).split(",");
      //  create a unique data_file id from a Couchbase counter, starting at 1 the first time
      try {
        nextIdNumber = getBucket().counter("LDCounter", 1, 1).content();
        // unique id must be a string
        lineDataIdString = listValuesArr[0] + "::line::" + listValuesArr[1] + "::" + listValuesArr[2] + "::" + String.valueOf(nextIdNumber);

      } catch (CouchbaseException e) {
        throw new Exception(e.getMessage());
      }

      try {
        lineDataFile = JsonObject.empty()
                .put("type", "line")
                .put("line_type", listValuesArr[1])
                .put("header_id", listValuesArr[3])
                .put("data_id", listValuesArr[4])
                .put("line_num", listValuesArr[5])
                .put("fcst_lead", listValuesArr[6])
                .put("fcst_valid_beg", listValuesArr[7])
                .put("fcst_valid_end", listValuesArr[8])
                .put("fcst_init_beg", listValuesArr[9])
                .put("obs_lead", listValuesArr[10])
                .put("obs_valid_beg", listValuesArr[11])
                .put("obs_valid_end", listValuesArr[12]);

        listFieldsArr = tableLineDataFieldsTable.get(listValuesArr[1]).split(",");
        for (int j = 0; j < listFieldsArr.length; j++) {
          lineDataFile.put(listFieldsArr[j], listValuesArr[j+13]);
        }

        doc = JsonDocument.create(lineDataIdString, lineDataFile);
        response = getBucket().upsert(doc);
        if (response.content().isEmpty()) {
          logger.warn("  **  WARNING: unexpected result from line data INSERT");
        } else {
            intResLineDataInsert++;
        }
      } catch (Exception e) {
        throw new Exception(e.getMessage());
      }

    }

    return intResLineDataInsert;
  }

  /**
   * Analyze the input file object to determine what type of MET output file it is.  Create an entry
   * in the data_file table for the file and build a DataFileInfo data structure with information
   * about the file and return it.
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
    JsonDocument doc;
    N1qlQueryResult queryResult;
    queryResult = null;
    List<N1qlQueryRow> queryList;
    queryList = null;
    N1qlQueryRow firstRow;
    firstRow = null;
    JsonObject firstRowObject;
    firstRowObject = null;
    long nextIdNumber;
    nextIdNumber = 0;
    String nextIdString;
    String dupIdString;
    nextIdString = "";
    dupIdString = "";
    JsonObject dataFile;
    JsonDocument response;

    //check file size and return if it  is 0
    if (file.length() == 0) {
      return null;
    }
    // set default values for the loaded time (now) and the modified time (that of input file)
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String strLoadDate = DATE_FORMAT.format(cal.getTime());
    cal.setTimeInMillis(file.lastModified());
    String strModDate = DATE_FORMAT.format(cal.getTime());

    // determine the type of the input data file by parsing the filename
    if (strFile.matches("\\S+\\.stat$")) {
      strDataFileLuTypeName = "stat";
    } else if (strFile.matches("\\S+_obj\\.txt$")) {
      strDataFileLuTypeName = "mode_obj";
    } else if (strFile.matches("\\S+_cts\\.txt$")) {
      strDataFileLuTypeName = "mode_cts";
    } else if (strFile.matches("\\S+\\.vsdb$")) {
      strDataFileLuTypeName = "vsdb_point_stat";
    } else if (strFile.matches("\\S+2d.txt$")) {
      strDataFileLuTypeName = "mtd_2d";
    } else if (strFile.matches("\\S+3d_pair_cluster.txt$")) {
      strDataFileLuTypeName = "mtd_3d_pc";
    } else if (strFile.matches("\\S+3d_pair_simple.txt$")) {
      strDataFileLuTypeName = "mtd_3d_ps";
    } else if (strFile.matches("\\S+3d_single_cluster.txt$")) {
      strDataFileLuTypeName = "mtd_3d_sc";
    } else if (strFile.matches("\\S+3d_single_simple.txt$")) {
      strDataFileLuTypeName = "mtd_3d_ss";
    } else {
      return null;
    }

    if (!"".equals(strDataFileLuTypeName)) {
      strDataFileLuId = tableDataFileLU.get(strDataFileLuTypeName);
    }
    // for compile. remove when CB fully in
    dataFileId = 0;

    // build a Couchbase query to look for the file and path in the data_file table
    String strDataFileQuery =  "SELECT " +
            "meta().id as dataFileId, " +
            "type, " +
            "data_type, " +
            "load_date, " +
            "mod_date " +
            "FROM `" +
            getBucket().name() +
            "` WHERE " +
            "type = \'file\' AND " +
            "`data_type` = \'" + strDataFileLuTypeName + "\' AND " +
            "filename = \'" + strFile + "\' AND " +
            "`path` = \'" + strPath + "\';";

    try {
      queryResult = getBucket().query(N1qlQuery.simple(strDataFileQuery));
      queryList = queryResult.allRows();

      // if the data file is already present in the database, print a warning and return the id
      if (queryList.size() > 0) {
        if (forceDupFile) {
          firstRow = queryList.get(0);
          firstRowObject = firstRow.value();
          // set dupIdString to id of existing data file document
          dupIdString = firstRowObject.get("dataFileId").toString();
          strLoadDate = firstRowObject.get("load_date").toString();
          strModDate = firstRowObject.get("mod_date").toString();
          DataFileInfo info = new DataFileInfo(dataFileId, strFile, strPath, strLoadDate,
                                               strModDate, strDataFileLuId, strDataFileLuTypeName);
          logger.warn("  **  WARNING: file already present in table data_file");
          return info;
        } else {
          throw new Exception("file already present in table data_file, use force_dup_file setting to override");
        }
      }

    } catch (CouchbaseException e) {
      throw new Exception(e.getMessage());
    }
    //  create a unique string data_file id from a Couchbase counter, starting at 1 the first time
    try {
      nextIdNumber = getBucket().counter("DFCounter", 1, 1).content();
      if (0 > nextIdNumber) {
        throw new Exception("METViewer load error: processDataFile() unable to get counter");
      }

    } catch (CouchbaseException e) {
      throw new Exception(e.getMessage());
    }
    nextIdString = getDatabaseInfo().getDbName() + "::file::" + strDataFileLuTypeName + "::" + String.valueOf(nextIdNumber);
    try {
      dataFile = JsonObject.empty()
              .put("type", "file")
              .put("data_type", strDataFileLuTypeName)
              .put("filename", strFile)
              .put("path", strPath)
              .put("load_date", strLoadDate)
              .put("mod_date", strModDate);

      doc = JsonDocument.create(nextIdString, dataFile);
      response = getBucket().upsert(doc);
      if (response.content().isEmpty()) {
        logger.warn("  **  WARNING: unexpected result from data_file INSERT");
      }
    } catch (Exception e) {
      throw new Exception(e.getMessage());
      }
    return new DataFileInfo(nextIdString, strFile, strPath, strLoadDate, strModDate, strDataFileLuId,
                            strDataFileLuTypeName);
  }

  @Override
  public void updateInfoTable(String strXML, MVLoadJob job) throws Exception {
    long nextIdNumber;
    nextIdNumber = 0;
    String nextIdString;
    nextIdString = "";
    String headerIdString;
    headerIdString = "";
    JsonObject instanceFile;
    JsonDocument response;
    JsonDocument doc;
    JsonObject firstRowObject;
    firstRowObject = null;

    //  get the instance_info information to insert
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
    String strUpdateDate = DATE_FORMAT.format(new Date());
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

    //  create a unique string data_file id from a Couchbase counter, starting at 1 the first time
    try {
      nextIdNumber = bucket.counter("DFCounter", 1, 1).content();
      if (0 > nextIdNumber) {
        throw new Exception("METViewer load error: updateInfoTable() unable to get counter");
      }
    } catch (CouchbaseException e) {
      throw new Exception(e.getMessage());
    }
    // Create new id for data file job document
    nextIdString = getDbName() + "::job::" + String.valueOf(nextIdNumber);

    //  execute the CB insert
    logger.info("Inserting instance_info record...  ");

    try {
      instanceFile = JsonObject.empty()
              .put("type", "job")
              .put("updater", strUpdater)
              .put("update_date", strUpdateDate)
              .put("update_note", strUpdateDetail)
              .put("xml_test", strLoadXML);

      doc = JsonDocument.create(nextIdString, instanceFile);
      response = bucket.upsert(doc);
      if (response.content().isEmpty()) {
        logger.warn("  **  WARNING: unexpected result from instance_info INSERT");
    }
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    logger.info("Done\n");
  }


  private String replaceInvalidValues(String strData) {
    return strData.replace("NA", "-9999").replace("-nan", "-9999").replace("nan", "-9999");
  }

}

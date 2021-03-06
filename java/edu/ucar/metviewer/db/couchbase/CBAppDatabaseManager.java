/**
 * CBAppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.couchbase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import edu.ucar.metviewer.BuildMysqlQueryStrings;
import edu.ucar.metviewer.MVNode;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.ValidationException;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public class CBAppDatabaseManager extends CBDatabaseManager implements AppDatabaseManager {


  private static final Logger logger = LogManager.getLogger("CBAppDatabaseManager");
  private final List<String> statHeaderSqlType = new ArrayList<>();
  private final List<String> modeHeaderSqlType = new ArrayList<>();
  private final Map<String, String> mtd3dSingleStatField = new HashMap<>();


  private final List<String> mtdHeaderSqlType = new ArrayList<>();

  private StopWatch cbStopWatch = new StopWatch();

  public CBAppDatabaseManager(DatabaseInfo databaseInfo, String password) throws Exception {
    super(databaseInfo, password);
    statHeaderSqlType.add("model");
    statHeaderSqlType.add("descr");
    statHeaderSqlType.add("fcst_lead");
    statHeaderSqlType.add("fcst_valid_beg");
    statHeaderSqlType.add("fcst_valid_end");
    statHeaderSqlType.add("fcst_init_beg");
    statHeaderSqlType.add("obs_lead");
    statHeaderSqlType.add("obs_valid_beg");
    statHeaderSqlType.add("obs_valid_end");
    statHeaderSqlType.add("init_hour");
    statHeaderSqlType.add("valid_hour");
    statHeaderSqlType.add("fcst_var");
    statHeaderSqlType.add("fcst_lev");
    statHeaderSqlType.add("obs_var");
    statHeaderSqlType.add("obs_lev");
    statHeaderSqlType.add("obtype");
    statHeaderSqlType.add("vx_mask");
    statHeaderSqlType.add("interp_mthd");
    statHeaderSqlType.add("interp_pnts");
    statHeaderSqlType.add("fcst_thresh");
    statHeaderSqlType.add("obs_thresh");
    statHeaderSqlType.add("fcst_perc");
    statHeaderSqlType.add("obs_perc");

    modeHeaderSqlType.add("model");
    modeHeaderSqlType.add("descr");
    modeHeaderSqlType.add("fcst_lead");
    modeHeaderSqlType.add("fcst_valid");
    modeHeaderSqlType.add("fcst_accum");
    modeHeaderSqlType.add("fcst_init");
    modeHeaderSqlType.add("obs_lead");
    modeHeaderSqlType.add("obs_valid");
    modeHeaderSqlType.add("obs_accum");
    modeHeaderSqlType.add("init_hour");
    modeHeaderSqlType.add("valid_hour");
    modeHeaderSqlType.add("fcst_rad");
    modeHeaderSqlType.add("fcst_thr");
    modeHeaderSqlType.add("obs_rad");
    modeHeaderSqlType.add("obs_thr");
    modeHeaderSqlType.add("fcst_var");
    modeHeaderSqlType.add("fcst_lev");
    modeHeaderSqlType.add("obs_var");
    modeHeaderSqlType.add("obs_lev");


    mtd3dSingleStatField.put("3D_CENTROID_X", "centroid_x");
    mtd3dSingleStatField.put("3D_CENTROID_Y", "centroid_y");
    mtd3dSingleStatField.put("3D_CENTROID_T", "centroid_t");
    mtd3dSingleStatField.put("3D_CENTROID_LAT", "centroid_lat");
    mtd3dSingleStatField.put("3D_CENTROID_LON", "centroid_lon");
    mtd3dSingleStatField.put("3D_X_DOT", "x_dot");
    mtd3dSingleStatField.put("3D_Y_DOT", "y_dot");
    mtd3dSingleStatField.put("3D_AXIS_ANG", "axis_ang");
    mtd3dSingleStatField.put("3D_VOLUME", "volume");
    mtd3dSingleStatField.put("3D_START_TIME", "start_time");
    mtd3dSingleStatField.put("3D_END_TIME", "end_time");
    mtd3dSingleStatField.put("3D_DURATION", "end_time-start_time");
    mtd3dSingleStatField.put("3D_CDIST_TRAVELLED", "cdist_travelled");
    mtd3dSingleStatField.put("3D_INTENSITY_10", "intensity_10");
    mtd3dSingleStatField.put("3D_INTENSITY_25", "intensity_25");
    mtd3dSingleStatField.put("3D_INTENSITY_50", "intensity_50");
    mtd3dSingleStatField.put("3D_INTENSITY_75", "intensity_75");
    mtd3dSingleStatField.put("3D_INTENSITY_90", "intensity_90");


    mtdHeaderSqlType.add("model");
    mtdHeaderSqlType.add("descr");
    mtdHeaderSqlType.add("fcst_lead");
    mtdHeaderSqlType.add("fcst_valid");
    mtdHeaderSqlType.add("fcst_accum");
    mtdHeaderSqlType.add("fcst_init");
    mtdHeaderSqlType.add("obs_lead");
    mtdHeaderSqlType.add("t_delta");
    mtdHeaderSqlType.add("fcst_rad");
    mtdHeaderSqlType.add("fcst_thr");
    mtdHeaderSqlType.add("fcst_var");
    mtdHeaderSqlType.add("fcst_lev");
    mtdHeaderSqlType.add("obs_var");
    mtdHeaderSqlType.add("obs_lev");

  }


  @Override
  public List<String> getListStat(String strFcstVar, String[] currentDBName) {
    List<String> listStatName = new ArrayList<>();

    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    String queryString = "";
    String dbList = "[";

    for (String cdbName : currentDBName) {
      if (dbList.length() > 1) {
        dbList += ", ";
      }
      dbList += "\'" + cdbName + "\'";
    }
    dbList += "]";

    // find the line_data documents where the header has the selected forecast variable
    queryString = "SELECT DISTINCT ld.line_type"
            + " FROM `" + getBucket().name() + "` AS h "
            + " INNER JOIN `" + getBucket()
            .name() + "` AS ld on ld.header_id = meta(h).id"
            + " WHERE ld.type = \'line\' AND h.type = \'header\' AND h.header_type = \'stat\' "
            + " AND h.fcst_var = \'" + strFcstVar + "\' "
            + " AND h.dbname IN " + dbList;

    try {
      cbStopWatch.start();
      queryResult = getBucket().query(N1qlQuery.simple(queryString));
      cbStopWatch.stop();
      logger.error("getListStat QUERYSTRING: " + queryString + "\nDuration: " + cbStopWatch
              .getFormattedTotalDuration());
      boolean boolCnt = false;
      boolean boolCts = false;
      boolean boolVcnt = false;
      for (N1qlQueryRow row : queryResult) {
        // System.out.println(row);
        switch (row.value().get("line_type").toString()) {
          case "cnt":
          case "sl1l2":
          case "sal1l2":
          case "grad":
            if (!boolCnt) {
              listStatName.addAll(MVUtil.statsCnt.keySet());
            }
            boolCnt = true;
            break;
          case "cts":
          case "ctc":
            if (!boolCts) {
              listStatName.addAll(MVUtil.statsCts.keySet());
            }
            boolCts = true;
            break;
          case "nbrcnt":
            listStatName.addAll(MVUtil.statsNbrcnt.keySet());
            break;
          case "nbrcts":
            listStatName.addAll(MVUtil.statsNbrcts.keySet());
            break;
          case "pstd":
            listStatName.addAll(MVUtil.statsPstd.keySet());
            break;
          case "mcts":
            listStatName.addAll(MVUtil.statsMcts.keySet());
            break;
          case "rhist":
            listStatName.addAll(MVUtil.statsRhist.keySet());
            break;
          case "vl1l2":
            //case val1l2:
            listStatName.addAll(MVUtil.statsVl1l2.keySet());
            listStatName.addAll(MVUtil.statsVcnt.keySet());
            boolVcnt = true;
            break;
          case "phist":
            listStatName.addAll(MVUtil.statsPhist.keySet());
            break;
          case "enscnt":
            listStatName.addAll(MVUtil.statsEnscnt.keySet());
            break;
          case "mpr":
            listStatName.addAll(MVUtil.statsMpr.keySet());
            break;
          case "orank":
            listStatName.addAll(MVUtil.statsOrank.keySet());
            break;
          case "ssvar":
            listStatName.addAll(MVUtil.statsSsvar.keySet());
            break;
          case "val1l2":
            listStatName.addAll(MVUtil.statsVal1l2.keySet());
            break;
          case "vcnt":
            if (!boolVcnt) {
              listStatName.addAll(MVUtil.statsVcnt.keySet());
            }
            break;
          case "ecnt":
            listStatName.addAll(MVUtil.statsEcnt.keySet());
            break;
          default:

        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    Collections.sort(listStatName);
    return listStatName;
  }

  @Override
  public List<String> getListValues(MVNode nodeCall, String strField, String[] currentDBName) {
    List<String> listRes = new ArrayList<>();
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    String queryString = "";

    boolean boolMode = nodeCall.children[0].tag.equals("mode_field");
    boolean boolMtd = nodeCall.children[0].tag.equals("mtd_field");
    boolean boolRhist = nodeCall.children[0].tag.equals("rhist_field");
    boolean boolPhist = nodeCall.children[0].tag.equals("phist_field");
    boolean boolROC = nodeCall.children[0].tag.equals("roc_field");
    boolean boolRely = nodeCall.children[0].tag.equals("rely_field");
    boolean boolEnsSS = nodeCall.children[0].tag.equals("ensss_field");
    boolean boolPerf = nodeCall.children[0].tag.equals("perf_field");
    boolean boolTaylor = nodeCall.children[0].tag.equals("taylor_field");
    boolean boolEclv = nodeCall.children[0].tag.equals("eclv_field");
    String strHeaderTable;
    if (boolMode) {
      strHeaderTable = "mode";
    } else if (boolMtd) {
      strHeaderTable = "mtd";
    } else {
      strHeaderTable = "stat";
    }
    boolean boolNRank = strField.equalsIgnoreCase("N_RANK");
    boolean boolNBin = strField.equalsIgnoreCase("N_BIN");

    //  parse the fcst_var/stat constraint to build a list of line_data tables and fcst_var values
    Map<String, String> tableFcstVarStat = new HashMap<>();
    Map<String, String> tableLineDataTables = new HashMap<>();
    boolean boolFcstVar = false;
    if (boolRhist) {
      tableLineDataTables.put("rhist", "true");
    } else if (boolPhist) {
      tableLineDataTables.put("phist", "true");
    } else if (boolROC) {
      tableLineDataTables.put("pct", "true");
      tableLineDataTables.put("ctc", "true");
    } else if (boolRely) {
      tableLineDataTables.put("pct", "true");
    } else if (boolEnsSS) {
      tableLineDataTables.put("ssvar", "true");
    } else if (boolPerf) {
      tableLineDataTables.put("cts", "true");
    } else if (boolTaylor) {
      tableLineDataTables.put("sl1l2", "true");
    } else if (boolEclv) {
      tableLineDataTables.put("eclv", "true");
    } else if (1 < nodeCall.children.length) {
      boolFcstVar = true;
      MVNode nodeFcstVarStat = nodeCall.children[1];
      for (int i = 0; i < nodeFcstVarStat.children.length; i++) {
        MVNode nodeFcstVar = nodeFcstVarStat.children[i];
        tableFcstVarStat.put(nodeFcstVar.name, "true");
        for (int j = 0; j < nodeFcstVar.children.length; j++) {
          String strStat = nodeFcstVar.children[j].value;
          String strLineDataTable = MVUtil.getStatTable(strStat).substring(10);
          tableLineDataTables.put(strLineDataTable, "true");
          if (strLineDataTable.equals("cnt")) {
            tableLineDataTables.put("sl1l2", "true");
            tableLineDataTables.put("sal1l2", "true");
          } else if (strLineDataTable.equals("cts")) {
            tableLineDataTables.put("ctc", "true");
          }
        }
      }
    }
    String[] listTables = tableLineDataTables.keySet().toArray(new String[]{});
    String tableList = "";
    if (listTables.length == 0) {
      boolFcstVar = false;
    } else {
      tableList = "[";
      for (String tableName : listTables) {
        if (tableList.length() > 1) {
          tableList += ", ";
        }
        tableList += "\'" + tableName + "\'";
      }
      tableList += "]";
    }

    String strWhere = "";
    //we need to get all values of obs_var for all variables
    if (strField.equalsIgnoreCase("obs_var")) {
      boolFcstVar = false;
    }
    if (boolFcstVar) {
      String strFcstVarList = "";
      String[] listFcstVar = tableFcstVarStat.keySet().toArray(new String[]{});
      boolean boolRegEx = false;
      for (int i = 0; i < listFcstVar.length; i++) {
        if (listFcstVar[i].contains("*")) {
          boolRegEx = true;
        }
        if (listFcstVar[i].length() > 0 && !listFcstVar[i].equals("NA")) {
          strFcstVarList += (0 < i ? "," : "") + "'" + listFcstVar[i].replace("*", "%") + "'";
        }
      }
      if (strFcstVarList.length() > 0) {
        strWhere += " WHERE h.fcst_var " + (boolRegEx ? "LIKE" : "IN") + " [" + strFcstVarList + "]";
        strWhere += " AND h.type = \'header\' AND h.header_type = \'stat\'";
      }
    }

    //  parse the list of constraints into a SQL where clause
    String strWhereTime = "";
    for (int i = 1; i < nodeCall.children.length; i++) {
      if (nodeCall.children[i].tag.equals("stat")) {
        continue;
      }

      //  determine if the field should be used as criteria
      MVNode nodeField = nodeCall.children[i];
      String strFieldCrit = nodeField.name.toLowerCase(Locale.ENGLISH);
      boolean boolTimeCritField = false;
      boolean boolTimeCritCur = false;
      if (strFieldCrit.contains("valid")
              || strFieldCrit.contains("init")
              || strFieldCrit.contains("lead")) {
        boolTimeCritField = strField.equals(strFieldCrit)
                || (strField.contains("fcst_init")
                && strFieldCrit.equals("init_hour"))
                || (strField.contains("fcst_valid")
                && strFieldCrit.equals("valid_hour"));
        boolTimeCritCur = true;
      }
      //  if so, build a where clause for the criteria
      String strFieldDBCrit = formatField(strFieldCrit, boolMode || boolMtd, false);
      if (strFieldDBCrit.contains("n_rank") || strFieldDBCrit.contains("n_bin")) {
        continue;
      }
      String strSqlOp = "IN";
      String strValList = "";
      for (int j = 0; j < nodeField.children.length; j++) {
        String strVal = nodeField.children[j].value;
        if (strVal.contains("*")) {
          strSqlOp = "LIKE";
        }
        strValList += (0 < j ? "," : "") + "\'" + strVal.replace("*", "%") + "\'";
      }

      //  add the where clause to the criteria, if appropriate
      if (boolTimeCritField) {
        if (boolMode || boolMtd) {
          strWhere += (strWhere.equals("") ? " WHERE " : " AND ")
                  + strFieldDBCrit + " " + strSqlOp + " [" + strValList + "]";
        } else {
          strWhereTime += (strWhereTime.equals("") ? " WHERE " : " AND ")
                  + strFieldDBCrit + " " + strSqlOp + " [" + strValList + "]";
        }
      } else if (!boolTimeCritCur) {
        strWhere += (strWhere.equals("") ? " WHERE " : " AND ")
                + strFieldDBCrit + " " + strSqlOp + " [" + strValList + "]";
      }
    }

    //  build a query for the values
    String strFieldDB = formatField(strField, boolMode || boolMtd).trim();
    if (boolFcstVar) {
      if (strField.contains("valid") || strField.contains("init") || strField.contains("lead")) {
        strFieldDB = "ld." + strField;
      } else {
        strFieldDB = "h." + strField;
      }
    } else {
      strFieldDB = strFieldDB.replaceAll("ld\\.", "");
      strFieldDB = strFieldDB.replaceAll("h\\.", "");
    }

    String dbList = "[";

    for (String cdbName : currentDBName) {
      if (dbList.length() > 1) {
        dbList += ", ";
      }
      dbList += "\'" + cdbName + "\'";
    }
    dbList += "]";

    if (boolNRank) {
      queryString = "SELECT DISTINCT ld.n_rank "
              + " FROM `" + getBucket().name() + "` as h "
              + " INNER JOIN `" + getBucket()
              .name() + "` as ld on ld.header_id = meta(h).id "
              + strWhere + (strWhere.equals("") ? " WHERE" : " AND")
              + " ld.type = \'line\' AND ld.line_type = \'rhist\'"
              + " AND h.type = \'header\' AND h.header_type = \'" + strHeaderTable + "\'"
              + " AND h.dbname IN " + dbList
              + " ORDER BY ld.n_rank";
    } else if (boolNBin) {
      queryString = "SELECT DISTINCT ld.n_bin "
              + " FROM `" + getBucket().name() + "` as h "
              + " INNER JOIN `" + getBucket()
              .name() + "` as ld on ld.header_id = meta(h).id "
              + strWhere + (strWhere.equals("") ? " WHERE" : " AND")
              + " ld.type = \'line\' AND ld.line_type = \'phist\'"
              + " AND h.type = \'header\' AND h.header_type = \'" + strHeaderTable + "\'"
              + " AND h.dbname IN " + dbList
              + " ORDER BY ld.n_bin";
    } else if (!boolMode && !boolMtd && boolFcstVar) {
      queryString = "SELECT DISTINCT " + strFieldDB
              + " FROM `" + getBucket().name() + "` as h "
              + " INNER JOIN `" + getBucket()
              .name() + "` as ld on ld.header_id = meta(h).id "
              + strWhere + (strWhere.equals("") ? " WHERE" : " AND")
              + " ld.type = \'line\' AND ld.line_type IN " + tableList
              + " AND h.type = \'header\' AND h.header_type = \'" + strHeaderTable + "\'"
              + " AND h.dbname IN " + dbList
              + " ORDER BY " + strFieldDB;
    } else if (!boolMode && !boolMtd
            && (strField.equals("fcst_lead")
            || strField.contains("valid")
            || strField.contains("init"))) {
      //  build a query to list all distinct,
      //  ordered values of the list field from the selected line documents
      strWhere = strWhere.replaceAll("ld\\.", "");
      queryString = "SELECT DISTINCT " + strFieldDB
              + " FROM `" + getBucket().name() + "` "
              + strWhere + (strWhere.equals("") ? " WHERE" : " AND")
              + " line_type IN " + tableList
              + " AND type = \'line\'" + strWhereTime
              + " AND dbname IN " + dbList
              + " ORDER BY " + strFieldDB;
    } else {
      strWhere = strWhere.replaceAll("h\\.", "");
      queryString = "SELECT DISTINCT " + strFieldDB
              + " FROM `" + getBucket().name() + "` "
              + strWhere + (strWhere.equals("") ? " WHERE" : " AND")
              + " header_type = \'" + strHeaderTable
              + "\' AND type = \'header\'"
              + " AND dbname IN " + dbList
              + " ORDER BY " + strFieldDB;
    }
    //  execute the query
    try {
      cbStopWatch.start();
      queryResult = getBucket().query(N1qlQuery.simple(queryString));
      cbStopWatch.stop();
      logger.error("getListStat QUERYSTRING: " + queryString + "\nDuration: " + cbStopWatch
              .getFormattedTotalDuration());

      for (N1qlQueryRow row : queryResult) {
        listRes.add(row.value().get(strField).toString());
      }

    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    Collections.sort(listRes);
    return listRes;
  }

  @Override
  public MvResponse executeQueriesAndSaveToFile(
          List<String> queries, String fileName,
          boolean isCalc, String currentDBName,
          boolean isNewFile) throws ValidationException {
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    String queryString = "";
    MvResponse mvResponse = new MvResponse();


    List<String> listSqlBeforeSelect = new ArrayList<>();
    List<String> listSqlLastSelectTemp = new ArrayList<>();
    List<String> listSqlLastSelect = new ArrayList<>();
    for (int i = queries.size() - 1; i >= 0; i--) {
      if (queries.get(i).startsWith("SELECT")) {
        listSqlLastSelectTemp.add(queries.get(i));
      } else {
        break;
      }
    }
    for (String sql : queries) {
      if (listSqlLastSelectTemp.contains(sql)) {
        listSqlLastSelect.add(sql);
      } else {
        listSqlBeforeSelect.add(sql);
      }
    }
    StopWatch dbStopWatch = new StopWatch();
    StopWatch saveToFileStopWatch = new StopWatch();

    try {
      dbStopWatch.start();
      for (String aListSqlBeforeSelect : listSqlBeforeSelect) {
        try {
          queryResult = getBucket().query(N1qlQuery.simple(aListSqlBeforeSelect));
          queryList = queryResult.allRows();
        } catch (CouchbaseException e) {
          logger.error(e.getMessage());
        }
      }
      try {
        dbStopWatch.stop();
      } catch (StopWatchException e) {
        logger.error(e.getMessage());
      }

      for (int i = 0; i < listSqlLastSelect.size(); i++) {
        boolean append = !isNewFile || i != 0;
        boolean printHeader = !append;
        try (FileWriter fstream = new FileWriter(new File(fileName), append);
             BufferedWriter out = new BufferedWriter(fstream);) {

          queryString = listSqlLastSelect.get(i);
          dbStopWatch.start();
          queryResult = getBucket().query(N1qlQuery.simple(queryString));
          dbStopWatch.stop();
          saveToFileStopWatch.start();
          queryList = queryResult.allRows();
          if (queryList.size() > 0) {
            printFormattedTable(queryList, queryString, out, printHeader);
            saveToFileStopWatch.stop();
            out.flush();
            mvResponse.setSuccess(true);
          } else {
            throw new Exception("Error: Query returned no data");
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
          String stat = "This";
          if (e.getMessage().contains("Unknown column")) {
            String[] queryArr = listSqlLastSelect.get(i).split(",");
            for (String str : queryArr) {
              if (str.contains("stat_name")) {
                stat = str.replaceAll("stat_name", "").trim();
                break;
              }
            }
            if (stat.equals("This")) {
              Pattern pattern = Pattern.compile("'(.*?)'");
              Matcher matcher = pattern.matcher(e.getMessage());

              if (matcher.find()) {
                stat = matcher.group(1);
                if (stat.contains(".")) {
                  stat = stat.split("\\.")[1];
                }
              }
            }
            logger.error(stat + " statistic can only be plotted as an aggregation of lines");

            //rethrow the exception to be printed as a error popup on UI
            throw new ValidationException(
                    stat + " statistic can only be plotted as an aggregation of lines");
          }

        }
      }

    } catch (CouchbaseException e) {
      logger.error(e.getMessage());
    }
    String message = null;
    try {
      message = "Database query time for " + currentDBName + " "
              + dbStopWatch.getFormattedTotalDuration();
      if (saveToFileStopWatch.getTotalDuration() != null) {
        message = message + "\nSave to file time for   " + currentDBName + " "
                + saveToFileStopWatch.getFormattedTotalDuration();
      }
    } catch (StopWatchException e) {
      logger.error(e.getMessage());
    }

    mvResponse.setInfoMessage(message);

    return mvResponse;
  }

  /**
   * Prints a textual representation of the input with the field names in the first row to the
   * specified {@link BufferedWriter} destination.
   *
   * @param res            The ResultSet to print
   * @param bufferedWriter The stream to write the formatted results to (defaults to printStream)
   */

  private void printFormattedTable(
          List<N1qlQueryRow> res, String queryString, BufferedWriter bufferedWriter, boolean isHeader) {
    N1qlQueryRow firstRow = null;
    JsonObject firstRowObject = null;
    ArrayList<String> docFields = new ArrayList<String>();
    String fieldString = "";
    String strVal = "";
    Boolean firstLine;

    // determine the field names in the correct order from the select portion of the query statement
    fieldString = queryString
            .substring(queryString.indexOf(" ") + 1, queryString.indexOf("FROM") - 1)
            .trim();
    String[] fieldArr = fieldString.split(",");
    for (String str : fieldArr) {
      str = str.trim();
      while (str.contains(" ")) {
        str = str.substring(str.indexOf(" ") + 1);
      }
      if (str.contains(".")) {
        str = str.substring(str.indexOf(".") + 1);
      }

      docFields.add(str);
    }
    char delim = '\t';
    try {
      //  print out the column headers
      firstLine = true;
      for (String docHeader : docFields) {
        if (firstLine) {
          bufferedWriter.write(docHeader);
          firstLine = false;
        } else {
          bufferedWriter.write(delim + docHeader);
        }
      }
      bufferedWriter.write(System.getProperty("line.separator"));

      //  print out the table of values
      for (N1qlQueryRow row : res) {
        firstRowObject = row.value();
        firstLine = true;
        for (String docLine : docFields) {
          strVal = firstRowObject.get(docLine).toString();
          strVal = strVal.equalsIgnoreCase("null") ? "NA" : strVal;
          strVal = strVal.equalsIgnoreCase("-9999") ? "NA" : strVal;
          if (firstLine) {
            bufferedWriter.write(strVal);
            firstLine = false;
          } else {
            bufferedWriter.write(delim + strVal);
          }
        }
        bufferedWriter.write(System.getProperty("line.separator"));
      }

//          if (objectType.equals("DATETIME")) {
//            Calendar cal = Calendar.getInstance();
//            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Timestamp ts = res.getTimestamp(i, cal);
//            strVal = DATE_FORMAT.format(ts);

    } catch (Exception e) {
      logger.error(
              "  **  ERROR: Caught " + e.getClass()
                      + " in printFormattedTable(ResultSet res): " + e.getMessage());
    }
  }

  private Map<String, Integer> getPctThreshInfo(String query, String currentDBName) {
    int numPctThresh = 0;
    int pctThresh = -1;
    Map<String, Integer> result = new HashMap<>();
//    try (Connection con = getConnection(currentDBName);
//         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
//                                              ResultSet.CONCUR_READ_ONLY);
//         ResultSet resultSet = stmt.executeQuery(query)
//    ) {
//
//      //  validate and save the number of thresholds
//      while (resultSet.next()) {
//        pctThresh = resultSet.getInt(1);
//        numPctThresh++;
//      }
//
//    } catch (SQLException e) {
//      logger.error(e.getMessage());
//    }
    result.put("numPctThresh", numPctThresh);
    result.put("pctThresh", pctThresh);

    return result;
  }

  private List<String> getNumbers(String queryString, String currentDBName) {
    List<String> result = new ArrayList<>();
    String fieldName;

    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;

    fieldName = queryString.substring(queryString.indexOf(".") + 1);
    fieldName = fieldName.substring(0, fieldName.indexOf(" "));

    try {
      queryResult = getBucket().query(N1qlQuery.simple(queryString));
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    for (N1qlQueryRow row : queryResult) {

      result.add(row.value().get(fieldName).toString());
    }
    return result;
  }

  /**
   * Returns a list of all available database names
   *
   * @return list of database names
   */
  @Override
  public List<String> getAllDatabases() {
    return new ArrayList<>(listDB.keySet());
  }

  @Override
  public Map<String, String> getAllDatabasesWithDescription() {
    return Collections.unmodifiableMap(listDB);
  }

  @Override
  public Map<String, List<String>> getAllGroups() {
    return Collections.unmodifiableMap(groupToDatabases);
  }


  @Override
  public SimpleDateFormat getDateFormat() {
    return DATE_FORMAT;
  }

  @Override
  public Pattern getDateRangePattern() {

    return Pattern.compile("(?i)\\s*between\\s+'([^']+)'\\s+and\\s+'([^']+)'\\s*");
  }

  @Override
  public void closeDataSource() {

  }


  /**
   * The input job and plot_fix information is used to build a list of SQL queries that result in
   * the temp table plot_data being filled with formatted plot data for a single plot.  Several job
   * validation checks are performed, and an Exception is thrown in case of error.
   *
   * @param job            contains plot job information
   * @param mapPlotFixPerm permutation of plot_fix values for current plot
   * @return list of SQL statements that result in plot data
   * @throws Exception
   */
  @Override
  public List<String> buildPlotSql(
          MVPlotJob job, MVOrderedMap mapPlotFixPerm,
          PrintStream printStreamSql) throws ValidationException {
    MVOrderedMap _mapFcstVarPat = new MVOrderedMap();
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    //  determine if the plot job is for stat data or MODE data
    List<String> tableHeaderSqlType;
    if (job.isModeJob()) {
      tableHeaderSqlType = modeHeaderSqlType;
    } else if (job.isMtdJob()) {
      tableHeaderSqlType = mtdHeaderSqlType;
    } else {
      tableHeaderSqlType = statHeaderSqlType;
    }

    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job,
            job.isModeJob() || job.isMtdJob());

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && !job.getPlotCond().isEmpty()) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + " ";
    }

    //  determine if the plot requires data aggregation or calculations
    boolean boolAggStat = job.isAggStat();

    boolean boolCalcStat = job.isModeRatioJob()
            || job.isMtdRatioJob()
            || job.isCalcStat();
    boolean boolEnsSs = job.getPlotTmpl().equals("ens_ss.R_tmpl");

    //  remove multiple dep group capability
    MVOrderedMap[] listDep = job.getDepGroups();
    MVOrderedMap mapDepGroup;
    if (1 != listDep.length && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
      throw new ValidationException("unexpected number of <dep> groups: " + listDep.length);
    }
    if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
      mapDepGroup = new MVOrderedMap();
    } else {
      mapDepGroup = listDep[0];
    }

    List<String> listSql = new ArrayList<>();
    String strSelectSql = "";
    for (int intY = 1; intY <= 2; intY++) {

      //  get the dep values for the current dep group
      MVOrderedMap mapDep = (MVOrderedMap) mapDepGroup.get("dep" + intY);
      if (mapDep == null) {
        mapDep = new MVOrderedMap();
      }


      //  establish lists of entries for each group of variables and values
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val())
              .getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new ValidationException("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length
              && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
        throw new ValidationException("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2

      if (!job.getPlotTmpl().equals(
              "eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        throw new ValidationException("no Y1 series stat found");
      }
      if (2 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        continue;
      }


      //  build the select list and where clauses for the series variables and values

      String strWhere = strPlotFixWhere;
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(job.isModeJob() || job.isMtdJob(),
              tableHeaderSqlType,
              listSeries, strWhere, true);
      String strSelectList = buildMysqlQueryStrings.getSelectList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields are not present
      // in the select list and temp table list, add them
      if (!strSelectList.contains("fcst_init")) {
        if (job.isModeJob() || job.isMtdJob()) {
          strSelectList += ",h.fcst_init";
        } else {
          if (strSelectList.length() > 0) {
            strSelectList += ",";
          }
          strSelectList += "ld.fcst_init_beg";


        }
      }
      if (!strSelectList.contains("fcst_valid")) {
        if (job.isModeJob() || job.isMtdJob()) {
          strSelectList += ",h.fcst_valid";
        } else {
          if (strSelectList.length() > 0) {
            strSelectList += ",";
          }
          strSelectList += "ld.fcst_valid_beg";

        }
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
              tableHeaderSqlType,
              listSeries, strWhere, false);
      String selectPlotList = buildQueryPlotStrings.getSelectList().trim();
      //  if the fcst_valid or fcst_init fields are not present
      // in the select list and temp table list, add them
      if (!selectPlotList.contains("fcst_init") && !selectPlotList.contains("init_hour")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectPlotList += ",h.fcst_init";
        } else {
          selectPlotList += ",ld.fcst_init_beg";
        }
      }
      if (!selectPlotList.contains("fcst_valid")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectPlotList += ",h.fcst_valid";
        } else {
          selectPlotList += ",ld.fcst_valid_beg";
        }
      }

      if (!boolEnsSs && !strSelectList.contains("fcst_lead")) {
        if (job.isModeJob() || job.isMtdJob()) {

          if (job.getEventEqual()) {
            strSelectList += "," + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                    + " WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead "
                    + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                    + " WHERE model = h.model) ) fcst_lead";
          } else {
            strSelectList += ",h.fcst_lead";
          }
          selectPlotList += ",h.fcst_lead";
        } else {
          if (job.getEventEqual()) {
            strSelectList += "," + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                    + " WHERE model = h.model) is NULL , ld.fcst_lead , ld.fcst_lead "
                    + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                    + " WHERE model = h.model) ) fcst_lead";
          } else {
            strSelectList += ",ld.fcst_lead";
          }
          selectPlotList += ",h.fcst_lead";
        }
      }


      //  for MODE, build the group by list
      String[] listGroupBy = new String[]{};
      if ((job.isModeJob() && !job.isModeRatioJob())
              || (job.isMtdJob() && !job.isMtdRatioJob())) {
        ArrayList<String> listGroupFields = new ArrayList<>();
        listGroupFields.add(job.getIndyVar());
        for (Map.Entry listSery : listSeries) {
          listGroupFields.add(listSery.getKey().toString());
        }

        Collections.addAll(listGroupFields, job.getPlotFixVal().getKeyList());
        listGroupBy = listGroupFields.toArray(new String[]{});
      }

      //  for ensemble spread/skill, add the ssvar line data and bail
      if (boolEnsSs) {

        listSql.add("SELECT "
                + "h." + selectPlotList + ",h.fcst_var,"
                + "ld.total,ld.bin_n,ld.var_min,ld.var_max,ld.var_mean,"
                + "ld.fbar,ld.obar,ld.fobar,ld.ffbar,ld.oobar"
                + " FROM `" + getBucket().name() + "` AS h "
                + " INNER JOIN `" + getBucket()
                .name() + "` AS ld ON ld.header_id = meta(h).id "
                + " WHERE " + strWhere + " AND ld.type = \'line\' AND ld.line_type = \'ssvar\' "
                + " AND h.type = \'header\' AND h.header_type = \'stat\' "
                + " AND h.dbname IN [\'" + job.getCurrentDBName().get(0) + "\']");

        return listSql;
      }


      //  validate and get the type and values for the independent variable
      String strIndyVarType = "";
      String strIndyVar = job.getIndyVar();
      String strIndyVarFormatted = "";
      if (!strIndyVar.isEmpty()) {
        String[] listIndyVal = job.getIndyVal();
        if (!tableHeaderSqlType.contains(strIndyVar)) {
          throw new ValidationException("unrecognized indep "
                  + (job.isModeJob() ? "mode" : "stat")
                  + "_header field: " + strIndyVar);
        }
        if (1 > listIndyVal.length) {
          throw new ValidationException("no independent variable values specified");
        }

        //  construct the select list item, where clause
        // and temp table entry for the independent variable
        if (!strSelectList.contains(strIndyVar)) {
          strSelectList += "," + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                  true);
          selectPlotList += "," + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                  true);
        }
        strIndyVarFormatted = formatField(strIndyVar, job.isModeJob() || job.isMtdJob(), false);
        if (strIndyVar.equals("fcst_lead") && job.getEventEqual()) {
          strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + " WHERE model = h.model) is NULL , "
                  + strIndyVarFormatted + " , " + strIndyVarFormatted
                  + " + (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + " WHERE model = h.model) ) ";
        }
        strWhere += (!strWhere.isEmpty() ? "  AND " : "") + strIndyVarFormatted
                + " IN [" + MVUtil.buildValueList(job.getIndyVal()) + "] ";
      }
      //  add fcst_var to the select list and temp table entries
      // strSelectList += ",h.fcst_var";
      selectPlotList += ",h.fcst_var";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();

        }
      }

      /*
       *  For agg_stat PCT plots, retrieve the sizes of PCT threshold lists for each series
       */
      Map<String, Integer> pctThreshInfo = new HashMap<>();
      if (job.getAggPct()) {
        MVOrderedMap[] series = MVUtil.permute(job.getSeries1Val().convertFromSeriesMap())
                .getRows();
        MVOrderedMap[] forecastVars;
        if (job.getPlotTmpl().equals("eclv.R_tmpl") && job.getDepGroups().length == 0) {
          MVOrderedMap m = new MVOrderedMap();
          m.put("NA", "ECLV");
          forecastVars = new MVOrderedMap[]{m};
        } else {
          forecastVars = MVUtil.permute((MVOrderedMap) job.getDepGroups()[0].get("dep" + intY))
                  .getRows();
        }
        for (int forecastVarsInd = 0; forecastVarsInd < forecastVars.length; forecastVarsInd++) {
          MVOrderedMap stats = forecastVars[forecastVarsInd];
          String[] vars = stats.getKeyList();
          for (int varsInd = 0; varsInd < vars.length; varsInd++) {
            int[] seriesNthresh = new int[series.length];
            for (int seriesInd = 0; seriesInd < series.length; seriesInd++) {
              MVOrderedMap ser = series[seriesInd];
              String[] serName = ser.getKeyList();
              for (int serNameInd = 0; serNameInd < serName.length; serNameInd++) {
                String strSelPctThresh = "SELECT DISTINCT ld.n_thresh FROM   "
                        + "stat_header h,   line_data_pct ld ";
                strSelPctThresh = strSelPctThresh + " WHERE ";
                if (strIndyVarFormatted.length() > 0 && job.getIndyVal().length > 0) {
                  strSelPctThresh = strSelPctThresh + strIndyVarFormatted;
                  strSelPctThresh = strSelPctThresh + " IN [" + MVUtil.buildValueList(
                          job.getIndyVal()) + "] " + " AND ";
                }
                strSelPctThresh = strSelPctThresh + serName[serNameInd]
                        + " = '" + ser.getStr(serName[serNameInd]) + "'";
                if (!vars[varsInd].equals("NA")) {
                  strSelPctThresh = strSelPctThresh + " AND fcst_var='" + vars[varsInd] + "' ";
                }
                if (strPlotFixWhere.length() > 0) {
                  strSelPctThresh = strSelPctThresh + "  AND  " + strPlotFixWhere;
                }
                strSelPctThresh = strSelPctThresh + "  AND ld.stat_header_id = h.stat_header_id;";
                printStreamSql.println(strSelPctThresh + " ");
                printStreamSql.flush();

                //  run the PCT thresh query
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < job.getCurrentDBName().size(); i++) {
                  pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(i));
                  if (1 != pctThreshInfo.get("numPctThresh")) {
                    String error = "number of PCT thresholds (" + pctThreshInfo.get(
                            "numPctThresh") + ") not distinct for " + serName[serNameInd]
                            + " = '" + ser.getStr(serName[serNameInd])
                            + "' AND database  " + job.getCurrentDBName().get(i) + "'";
                    if (!vars[varsInd].equals("NA")) {
                      error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                    }
                    errors.add(error);
                  } else if (1 > pctThreshInfo.get("numPctThresh")) {
                    String error = "invalid number of PCT thresholds ("
                            + pctThreshInfo.get("numPctThresh") + ") found for "
                            + serName[serNameInd] + " = '"
                            + ser.getStr(serName[serNameInd])
                            + "' AND database " + job.getCurrentDBName().get(i) + "'";
                    if (!vars[varsInd].equals("NA")) {
                      error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                    }
                    errors.add(error);
                  } else {
                    errors.add(null);
                    seriesNthresh[seriesInd] = pctThreshInfo.get("pctThresh");
                  }
                }
                boolean noErrors = false;
                for (String error : errors) {
                  if (error == null) {
                    noErrors = true;
                    break;
                  }
                }
                if (!noErrors) {
                  for (String error : errors) {
                    if (error != null) {
                      throw new ValidationException(error);
                    }
                  }
                }
                //seriesNthresh[seriesInd] = pctThreshInfo.get("pctThresh");
              }

            }
            boolean allEqual = true;
            for (Integer s : seriesNthresh) {
              if (!s.equals(seriesNthresh[0])) {
                allEqual = false;
                break;
              }
            }
            if (!allEqual) {
              String error = "Different value for PCT thresholds   for individual series!";
              throw new ValidationException(error);
            } else {
              pctThreshInfo.put("pctThresh", seriesNthresh[0]);
            }
          }
        }

      } else {
        pctThreshInfo.put("pctThresh", -1);
      }


      //  determine how many queries are needed to gather that stat information
      int intNumQueries;
      String[][] listFcstVarStat;
      if (!job.getPlotTmpl().equals("eclv.R_tmpl")) {
        listFcstVarStat = MVUtil.buildFcstVarStatList(mapDep);
        intNumQueries = listFcstVarStat.length;
      } else {
        intNumQueries = 1;
        listFcstVarStat = new String[0][0];
      }

      //  build a query for each fcst_var/stat pair
      // or a just single query for contingency tables or partial sums
      for (int intFcstVarStat = 0; intFcstVarStat < intNumQueries; intFcstVarStat++) {
        String strFcstVarClause = "";
        String strStat = "";
        if (listFcstVarStat.length > 0) {
          //  get the current fcst_var/stat pair
          String strFcstVar = listFcstVarStat[intFcstVarStat][0];
          strStat = listFcstVarStat[intFcstVarStat][1];

          //  build the fcst_var where clause criteria
          strFcstVarClause = "= '" + strFcstVar + "'";
          Matcher matProb = MVUtil.prob.matcher(strFcstVar);
          if (matProb.matches() && strFcstVar.contains("*")) {
            Pattern patFcstVar = Pattern.compile(
                    strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
            if (!_mapFcstVarPat.containsKey(patFcstVar)) {
              _mapFcstVarPat.put(patFcstVar, MVUtil.replaceSpecialChars(strFcstVar));
            }
            strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
          }
        } else {
          if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
            strStat = "ECLV";
          }
        }
        if (!strSelectList.contains("fcst_var")) {
          strSelectList += ",\n'" + listFcstVarStat[intFcstVarStat][0] + "' fcst_var";
        } else {
          strSelectList = strSelectList
                  .replace(listFcstVarStat[intFcstVarStat - 1][0] + "' fcst_var"
                          , listFcstVarStat[intFcstVarStat][0] + "' fcst_var");
        }

        //  determine the table containing the current stat
        Map tableStats;
        String strStatTable = "";
        String strStatField = strStat.toLowerCase(Locale.US);
        if (job.isModeJob()) {
          String strStatMode = MVUtil.parseModeStat(strStat)[0];
          if (MVUtil.modeSingleStatField.containsKey(strStatMode)) {
            tableStats = MVUtil.modeSingleStatField;
          } else if (MVUtil.modePairStatField.containsKey(strStatMode)) {
            tableStats = MVUtil.modeSingleStatField;
          } else if (MVUtil.modeRatioField.contains(strStat)) {
            tableStats = MVUtil.modeSingleStatField;
          } else {
            throw new ValidationException("unrecognized mode stat: " + strStatMode);
          }
        } else if (job.isMtdJob()) {

          String[] listStatParse = strStat.split("_");
          String stat = strStat.replace("_" + listStatParse[listStatParse.length - 1], "");

          if (mtd3dSingleStatField.containsKey(stat)) {
            tableStats = mtd3dSingleStatField;
          } else if (MVUtil.mtd3dPairStatField.containsKey(stat)) {
            tableStats = MVUtil.mtd3dPairStatField;
          } else if (MVUtil.mtdRatioField.contains(strStat)) {
            tableStats = mtd3dSingleStatField;
          } else if (MVUtil.mtd2dStatField.containsKey(stat)) {
            tableStats = MVUtil.mtd2dStatField;
          } else {
            throw new ValidationException("unrecognized mode stat: " + stat);
          }
        } else {

          String aggType = null;
          if (job.getCalcCtc() || job.getAggCtc()) {
            aggType = MVUtil.CTC;
          } else if (job.getCalcSl1l2() || job.getAggSl1l2()) {
            aggType = MVUtil.SL1L2;
          } else if (job.getCalcGrad() || job.getAggGrad()) {
            aggType = MVUtil.GRAD;
          } else if (job.getCalcSal1l2() || job.getAggSal1l2()) {
            aggType = MVUtil.SAL1L2;
          } else if (job.getAggNbrCnt()) {
            aggType = MVUtil.NBRCNT;
          } else if (job.getAggPct()) {
            aggType = MVUtil.PCT;
          } else if (job.getAggSsvar()) {
            aggType = MVUtil.SSVAR;
          } else if (job.getCalcVl1l2() || job.getAggVl1l2()) {
            aggType = MVUtil.VL1L2;
          } else if (job.getCalcVal1l2() || job.getAggVal1l2()) {
            aggType = MVUtil.VAL1L2;
          } else if (job.getAggEcnt()) {
            aggType = MVUtil.ECNT;
          }


          if (MVUtil.statsCnt.containsKey(strStat)) {
            tableStats = MVUtil.statsCnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsCnt, strStat, aggType);
              strStatTable = " AND ld.line_type = \'" + aggType + "\' ";
            } else {
              strStatTable = " AND ld.line_type = \'cnt\' ";
            }
          } else if (MVUtil.statsSsvar.containsKey(strStat)) {
            tableStats = MVUtil.statsSsvar;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsSsvar, strStat, aggType);
              strStatTable = " AND ld.line_type = \'" + aggType + "\' ";
            } else {
              strStatTable = " AND ld.line_type = \'ssvar\' ";
            }
          } else if (MVUtil.statsCts.containsKey(strStat)) {
            tableStats = MVUtil.statsCts;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsCts, strStat, aggType);
              strStatTable = " AND ld.line_type = \'ctc\' ";
            } else {
              strStatTable = " AND ld.line_type = \'cts\' ";
            }
          } else if (MVUtil.statsNbrcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsNbrcnt, strStat, aggType);
            }
            strStatTable = " AND ld.line_type = \'nbrcnt\' ";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsEnscnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEnscnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsEnscnt, strStat, aggType);
            }
            strStatTable = " AND ld.line_type = \'enscnt\' ";
            strStatField = strStat.replace("ENS_", "").toLowerCase();
          } else if (MVUtil.statsNbrcts.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcts;
            MVUtil.isAggTypeValid(MVUtil.statsNbrcts, strStat, aggType);
            strStatTable = " AND ld.line_type = \'nbrcts\' ";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsPstd.containsKey(strStat)) {
            tableStats = MVUtil.statsPstd;
            strStatTable = " AND ld.line_type = \'pstd\' ";
            if (aggType != null) {
              strStatTable = " AND ld.line_type = \'pct\' ";
              MVUtil.isAggTypeValid(MVUtil.statsPstd, strStat, aggType);
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strStatTable += ",   line_data_pct_thresh ldt" + i;
              }
              strStatTable += " ";
            }
            strStatField = strStat.replace("PSTD_", "").toLowerCase();
          } else if (MVUtil.statsMcts.containsKey(strStat)) {
            tableStats = MVUtil.statsMcts;
            MVUtil.isAggTypeValid(MVUtil.statsMcts, strStat, aggType);
            strStatTable = " AND ld.line_type = \'mcts\' ";
            strStatField = strStat.replace("MCTS_", "").toLowerCase();
          } else if (MVUtil.statsRhist.containsKey(strStat)) {
            tableStats = MVUtil.statsRhist;
            strStatTable = " AND ld.line_type = \'rhist\' ";
            strStatField = strStat.replace("RHIST_", "").toLowerCase();
          } else if (MVUtil.statsPhist.containsKey(strStat)) {
            tableStats = MVUtil.statsPhist;
            MVUtil.isAggTypeValid(MVUtil.statsPhist, strStat, aggType);
            strStatTable = " AND ld.line_type = \'phist\' ";
            strStatField = strStat.replace("PHIST_", "").toLowerCase();
          } else if (MVUtil.statsVl1l2.containsKey(strStat)) {
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVl1l2, strStat, aggType);
            }
            tableStats = MVUtil.statsVl1l2;
            strStatTable = " AND ld.line_type = \'vl1l2\' ";
            strStatField = strStat.replace("VL1L2_", "").toLowerCase();
          } else if (MVUtil.statsVal1l2.containsKey(strStat)) {
            MVUtil.isAggTypeValid(MVUtil.statsVal1l2, strStat, aggType);
            tableStats = MVUtil.statsVal1l2;
            strStatTable = " AND ld.line_type = \'val1l2\' ";
            strStatField = strStat.replace("VAL1L2_", "").toLowerCase();
          } else if (MVUtil.statsMpr.containsKey(strStat)) {
            tableStats = MVUtil.statsMpr;
            strStatTable = " AND ld.line_type = \'mpr\' ";
          } else if (MVUtil.statsOrank.containsKey(strStat)) {
            tableStats = MVUtil.statsOrank;
            strStatTable = " AND ld.line_type = \'orank\' ";
            strStatField = strStat.replace("ORANK_", "").toLowerCase();
          } else if (MVUtil.statsVcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsVcnt;
            strStatField = strStat.replace("VCNT_", "").toLowerCase();
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVcnt, strStat, aggType);
              strStatTable = " AND ld.line_type = \'" + aggType + "\' AND";
            } else {
              strStatTable = " AND ld.line_type = \'vcnt\' ";
            }
          } else if (strStat.equals("ECLV") && job.getPlotTmpl().equals("eclv.R_tmpl")) {
            if (aggType != null && aggType.equals(MVUtil.CTC)) {
              tableStats = MVUtil.statsCts;
              strStatTable = " AND ld.line_type = \'ctc\' ";
            } else {
              tableStats = MVUtil.statsPstd;
              strStatTable = "line_data_pct ld,   line_data_pct_thresh ldt ";
            }
          } else if (MVUtil.statsEcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEcnt;
            strStatTable = " AND ld.line_type = \'ecnt\' ";
            strStatField = strStat.replace("ECNT_", "").toLowerCase();
          } else {
            throw new ValidationException("unrecognized stat: " + strStat);
          }
        }

        //  build the SQL for the current fcst_var and stat
        if (job.isModeJob()) {

          //  build the mode SQL
          String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;

          listSql.addAll(buildModeStatSql(strSelectList, strWhereFcstVar, strStat, listGroupBy,
                  job.getEventEqual(), listSeries));
        } else if (job.isMtdJob()) {

          //  build the mtd SQL
          String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;
          listSql.addAll(buildMtdStatSql(strSelectList, strWhereFcstVar, strStat));
        } else {
          boolean boolBCRMSE = false;
          String strSelectStat = strSelectList;

          //  build the select list and temp table elements for the stat and CIs
          if (strStat.equals("BCRMSE")) {
            boolBCRMSE = true;
            strStatField = "bcmse";
          }
          strSelectStat += ", \'" + strStat + "\' stat_name";

          //  add the appropriate stat table members, depending
          // on the use of aggregation and stat calculation
          if (job.getAggCtc()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fy_oy,   ld.fy_on,   "
                    + "ld.fn_oy,   ld.fn_on";
          } else if (job.getAggSl1l2()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fbar,   ld.obar,   "
                    + "ld.fobar,   ld.ffbar,   ld.oobar,  ld.mae";
          } else if (job.getAggGrad()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fgbar,   ld.ogbar,   "
                    + "ld.mgbar,   ld.egbar,   ld.s1,  ld.s1_og,   ld.fgog_ratio";
          } else if (job.getAggSsvar()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fbar,   ld.obar,   "
                    + "ld.fobar,   ld.ffbar,   ld.oobar,   "
                    + "ld.var_mean,    ld.bin_n";
          } else if (job.getAggSal1l2()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fabar,   ld.oabar,   "
                    + "ld.foabar,   ld.ffabar,   ld.ooabar,  ld.mae";

          } else if (job.getAggEcnt()) {

            strSelectStat += ",  'NA' stat_value,"
                    + " ld.total,  ld.me,   ld.rmse,  ld.crps,  ld.crpss,"
                    + "  ld.ign,  "
                    + " ld.spread,  ld.me_oerr,  ld.rmse_oerr,"
                    + "   ld.spread_oerr,"
                    + " ld.spread_plus_oerr";
          } else if (job.getAggPct()) {
            if (!job.getPlotTmpl().equals("eclv.R_tmpl")) {
              strSelectStat += ",   'NA' stat_value,   ld.total,   (ld.n_thresh - 1)";
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strSelectStat += ", ";
                if (i < pctThreshInfo.get("pctThresh") - 1) {
                  strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + ldt"
                          + (i + 1) + ".thresh_i)/2, 3), ";
                } else {
                  strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + 1)/2, 3), ";
                }
                strSelectStat += "  ldt" + i + ".oy_i, "
                        + "  ldt" + i + ".on_i";
              }
            } else {
              strSelectStat += ",   'NA' stat_value,   ld.n_thresh,  ldt.thresh_i,  ldt.oy_i ,"
                      + " ldt.on_i";
            }
          } else if (job.getAggNbrCnt()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,   ld.fbs,   ld.fss";
          } else if (job.getAggVl1l2()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,  ld.ufbar,  ld.vfbar,  ld.uobar,"
                    + "  ld.vobar,  ld.uvfobar,  ld.uvffbar,  ld.uvoobar,"
                    + "   ld.f_speed_bar,   ld.o_speed_bar";
          } else if (job.getAggVal1l2()) {
            strSelectStat += ",   'NA' stat_value,   ld.total,  ld.ufabar,  ld.vfabar,  "
                    + "ld.uoabar,  ld.voabar,  ld.uvfoabar,  ld.uvffabar, "
                    + " ld.uvooabar";
          } else if (job.getCalcCtc()) {
            strSelectStat += ",   ld.total, ld.fy_oy, ld.fy_on, ld.fn_oy, ld.fn_on, "
                    + "'NA' stat_value, "
                    + "  'NA' stat_ncl,   'NA' stat_ncu,   "
                    + "'NA' stat_bcl,   'NA' stat_bcu";
          } else if (job.getCalcSl1l2()) {
            if (strStat.equalsIgnoreCase("mae")) {
              strSelectStat += ",    ld.mae,  'NA' stat_value, "
                      + "  'NA' stat_ncl,   'NA' stat_ncu,   'NA' stat_bcl,  "
                      + "'NA' stat_bcu";
            } else {
              strSelectStat += ",   ld.total, ld.fbar, ld.obar, ld.fobar, ld.ffbar, ld.oobar,"
                      + " 'NA' stat_value, "
                      + "  'NA' stat_ncl,   'NA' stat_ncu,   'NA' stat_bcl,   "
                      + "'NA' stat_bcu";
            }
          } else if (job.getCalcGrad()) {
            strSelectStat += ",   ld.total, ld.fgbar, ld.ogbar, ld.mgbar, ld.egbar,  "
                    + "'NA' stat_value, "
                    + "  'NA' stat_ncl,   'NA' stat_ncu,   'NA' stat_bcl,   "
                    + "'NA' stat_bcu";
          } else if (job.getCalcSal1l2()) {
            strSelectStat += ",   ld.total, ld.fabar, ld.oabar, ld.foabar, ld.ffabar, "
                    + "ld.ooabar,  'NA' stat_value, "
                    + "  'NA' stat_ncl,   'NA' stat_ncu,   'NA' stat_bcl,   "
                    + "'NA' stat_bcu";
          } else if (job.getCalcVl1l2()) {
            strSelectStat += ",   ld.total, ld.ufbar, ld.vfbar, ld.uobar, ld.vobar, "
                    + "ld.uvfobar, ld.uvffbar, ld.uvoobar,"
                    + " ld.f_speed_bar, ld.o_speed_bar, 'NA' stat_value, "
                    + "  'NA' stat_ncl,   'NA' stat_ncu,   'NA' stat_bcl,   "
                    + "'NA' stat_bcu";

          } else if (job.getCalcVal1l2()) {
            strSelectStat += ",  ld.total, ld.ufabar, ld.vfabar, ld.uoabar, ld.voabar, "
                    + "ld.uvfoabar, ld.uvffabar, ld.uvooabar,"
                    + " 'NA' stat_value,"
                    + "  'NA' stat_ncl,  'NA' stat_ncu,  'NA' stat_bcl,  "
                    + "'NA' stat_bcu";
          } else {
            if (boolBCRMSE) {
              strSelectStat += ",   IF(ld." + strStatField + "=-9999,'NA',CAST(sqrt(ld."
                      + strStatField + ") as DECIMAL(30, 5))) stat_value";

            } else {
              strSelectStat += ",   IF(ld." + strStatField + "=-9999,'NA',ld."
                      + strStatField + " ) stat_value";

            }

            //  determine if the current stat has normal or bootstrap CIs
            String[] listStatCI = (String[]) tableStats.get(strStat);
            boolean boolHasNorm = false;
            boolean boolHasBoot = false;
            for (String aListStatCI : listStatCI) {
              if (aListStatCI.equals("nc")) {
                boolHasNorm = true;
              } else if (aListStatCI.equals("bc")) {
                boolHasBoot = true;
              }
            }

            //  add the CIs to the select list, if present, otherwise, invalid data
            if (boolHasNorm) {
              strSelectStat += ",   IF(ld." + strStatField + "_ncl=-9999,'NA',ld."
                      + strStatField + "_ncl  ) stat_ncl"
                      + ",   IF(ld." + strStatField + "_ncu=-9999,'NA',ld."
                      + strStatField + "_ncu  ) stat_ncu";
            } else {
              strSelectStat += ",   'NA' stat_ncl,   'NA' stat_ncu";
            }

            if (boolHasBoot && !boolAggStat) {
              if (boolBCRMSE) {
                strSelectStat += ",   IF(ld." + strStatField + "_bcl=-9999,'NA',CAST(sqrt(ld."
                        + strStatField + "_bcl) as DECIMAL(30, 5))) stat_bcl"
                        + ",   IF(ld." + strStatField + "_bcu=-9999,'NA',CAST(sqrt(ld."
                        + strStatField + "_bcu) as DECIMAL(30, 5))) stat_bcu";
              } else {
                strSelectStat += ",   IF(ld." + strStatField + "_bcl=-9999,'NA',ld."
                        + strStatField + "_bcl) stat_bcl"
                        + ",   IF(ld." + strStatField + "_bcu=-9999,'NA',ld."
                        + strStatField + "_bcu ) stat_bcu";
              }
            } else {
              strSelectStat += ",   'NA' stat_bcl,   'NA' stat_bcu";
            }
          }

          String strStatNaClause = "";
          if (!boolAggStat && !boolCalcStat) {
            strStatNaClause = " AND ld." + strStatField + " != -9999";
          }
          if (job.getAggPct()) {
            if (!job.getPlotTmpl().equals("eclv.R_tmpl")) {
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strStatNaClause += " AND ld.line_data_id = ldt" + i + ".line_data_id "
                        + " AND ldt" + i + ".i_value = " + i;
              }
            } else {
              strStatNaClause = " AND ld.line_data_id = ldt.line_data_id ";
            }
          }

          //  build the query
          strSelectSql += (strSelectSql.isEmpty() ? "" : " UNION ALL ")
                  + "SELECT " + strSelectStat
                  + " FROM `" + getBucket().name() + "` AS h "
                  + " INNER JOIN `" + getBucket()
                  .name() + "` AS ld ON ld.header_id = meta(h).id ";
          strSelectSql += " WHERE " + strWhere;
          if (strFcstVarClause.length() > 0) {
            strSelectSql += " AND h.fcst_var " + strFcstVarClause + " ";
          }
          strSelectSql += " AND h.type = \'header\' AND ld.type = \'line\'" + strStatTable
                  + " AND h.dbname = \'" + job.getCurrentDBName().get(0) + "\'"
                  + strStatNaClause;
        }

      }

    }

    //  add the stat plot query to the list
    if (!job.isModeJob() && !job.isMtdJob()) {
      listSql.add(strSelectSql + ";");
    }

    //remove duplicated queries
    listSql = new ArrayList<>(new LinkedHashSet<>(listSql));

    return listSql;
  }

  /**
   * Build where clauses for each of the input aggregation field/value entries and return the
   * clauses as a String
   *
   * @param listPlotFixFields list of &lt;plot_fix&gt; field/value pairs
   * @param boolModePlot      specifies MODE plot
   * @return generated SQL where clauses
   */

  private String buildPlotFixWhere(
          Map.Entry[] listPlotFixFields, MVPlotJob job,
          boolean boolModePlot) {
    String strWhere = "";

    //  build the aggregate fields where clause
    for (int i = 0; i < listPlotFixFields.length; i++) {
      String strField = (String) listPlotFixFields[i].getKey();
      String strCondition = "";
      Object objValue = listPlotFixFields[i].getValue();
      if (objValue instanceof String[]) {
        strCondition = "IN [" + MVUtil.buildValueList((String[]) objValue) + "]";
      } else if (objValue instanceof MVOrderedMap) {
        MVOrderedMap mapTmpl = job.getTmplVal();
        String strSetName = mapTmpl.get(strField + "_set").toString();
        String[] listValues = (String[]) ((MVOrderedMap) objValue).get(strSetName);
        strCondition = "IN [" + MVUtil.buildValueList(listValues) + "]";

      } else if (objValue instanceof String) {
        if (objValue.toString().startsWith("BETWEEN")) {
          strCondition = objValue.toString();
        } else {
          strCondition = "IN ['" + objValue.toString() + "']";
        }
      }
      String strIndyVarFormatted = formatField(strField, boolModePlot, false);
      if (strField.equals("fcst_lead") && job.getEventEqual()) {
        strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                + " WHERE model = h.model) is NULL , "
                + strIndyVarFormatted + " , " + strIndyVarFormatted
                + " + (select fcst_lead_offset FROM model_fcst_lead_offset "
                + " WHERE model = h.model) ) ";
      }
      strWhere += (0 < i ? "  AND " : "  ") + strIndyVarFormatted + " " + strCondition + " ";
    }

    return strWhere;
  }

  /**
   * Use the input query components to build a list of select statements to gather plot data. This
   * function is a switchboard for the different types of MODE statistics: single, pair, derived,
   * difference and ratios.
   *
   * @param strSelectList list of select fields
   * @param strWhere      list of where clauses
   * @param strStat       MODE stat
   * @param listGroupBy   list of fields to group by
   * @param listSeries
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildModeStatSql(
          String strSelectList, String strWhere, String strStat,
          String[] listGroupBy, boolean isEventEqualization,
          Map.Entry[] listSeries) {

    List<String> listQuery = new ArrayList<>();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatComp = MVUtil.parseModeStat(strStat);
    if (listStatComp[0].equals("ACOV")) {
      strWhere = strWhere.replace("h.", "");
      listQuery.add(buildModeSingleAcovTable(strSelectList, strWhere, strStat, listGroupBy,
              isEventEqualization));
    } else if (MVUtil.modeSingleStatField.containsKey(listStatComp[0])) {
      if (!listStatComp[1].startsWith("D")) {
        strWhere = strWhere.replace("h.", "");
        listQuery.add(buildModeSingleStatTable(strSelectList, strWhere, strStat, listGroupBy,
                isEventEqualization));
      } else {
        String strWhereForQuery = strWhere.replace("h.", "");
        String newStat = strStat.replace("_D", "_F");
        String query1 = buildModeSingleStatTable(strSelectList, strWhereForQuery, newStat,
                listGroupBy,
                isEventEqualization);
        newStat = strStat.replace("_D", "_O");
        String query2 = buildModeSingleStatTable(strSelectList, strWhereForQuery, newStat,
                listGroupBy,
                isEventEqualization);
        strWhere = strWhere.replace("h.", "s.");
        listQuery
                .add(buildModeSingleStatDiffTable(strSelectList, strWhere, strStat, query1, query2,
                        listSeries));
      }
    } else if (MVUtil.modePairStatField.containsKey(listStatComp[0])) {


      if (listStatComp[0].equals("MAXINT")) {
        String[] listMaxintQueries = {
                buildModePairStatTable(strSelectList, strWhere, "MAXINTF_" + listStatComp[1]),
                buildModePairStatTable(strSelectList, strWhere, "MAXINTO_" + listStatComp[1])
        };
        listMaxintQueries[0] = listMaxintQueries[0].replace("MAXINTF", "MAXINT");
        listMaxintQueries[1] = listMaxintQueries[1].replace("MAXINTO", "MAXINT");
        listQuery.addAll(Arrays.asList(listMaxintQueries));
      } else {
        listQuery.add(buildModePairStatTable(strSelectList, strWhere, strStat));
      }
    } else if ((listStatComp[0].equals("RATIO")
            || listStatComp[0].equals("AREARAT")
            || strStat.startsWith("OBJ"))) {
      listQuery.add(buildModeSingleStatRatioTable(strSelectList, strWhere));
    }
    return listQuery;
  }

  /**
   * Use the input query components to build a list of select statements to gather plot data. This
   * function is a switchboard for the different types of MODE statistics: single, pair, derived,
   * difference and ratios.
   *
   * @param strSelectList list of select fields
   * @param strWhere      list of where clauses
   * @param strStat       MTD stat
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildMtdStatSql(
          String strSelectList, String strWhere, String strStat) {

    List<String> listQuery = new ArrayList<>();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatParse = strStat.split("_");
    String stat = strStat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];
    if (mtd3dSingleStatField.containsKey(stat)) {
      if (!strStatFlag.startsWith("D")) {
        strWhere = strWhere.replace("h.", "");
        listQuery.add(buildMtd3dSingleStatTable(strSelectList, strWhere, strStat));
      } else {
        String strWhereForQuery = strWhere.replace("h.", "");
        String newStat = strStat.replace("_D", "_F");
        String query1 = buildMtd3dSingleStatTable(strSelectList, strWhereForQuery, newStat);
        newStat = strStat.replace("_D", "_O");
        String query2 = buildMtd3dSingleStatTable(strSelectList, strWhereForQuery, newStat);
        strWhere = strWhere.replace("h.", "s.");
        listQuery
                .add(buildMtd3dSingleStatDiffTable(strSelectList, strWhere, strStat, query1, query2));
      }
    } else if (MVUtil.mtd2dStatField.containsKey(stat)) {
      if (!strStatFlag.startsWith("D")) {
        strWhere = strWhere.replace("h.", "");
        listQuery.add(buildMtd2dStatTable(strSelectList, strWhere, strStat));
      } else {
        String strWhereForQuery = strWhere.replace("h.", "");
        String newStat = strStat.replace("_D", "_F");
        String query1 = buildMtd2dStatTable(strSelectList, strWhereForQuery, newStat);
        newStat = strStat.replace("_D", "_O");
        String query2 = buildMtd2dStatTable(strSelectList, strWhereForQuery, newStat);
        strWhere = strWhere.replace("h.", "s.");
        listQuery.add(buildMtd2dStatDiffTable(strSelectList, strWhere, strStat, query1, query2));
      }
    } else if (MVUtil.mtd3dPairStatField.containsKey(stat)) {
      strWhere = strWhere.replace("h.", "");
      listQuery.add(buildMtd3dPairStatTable(strSelectList, strWhere, strStat));
    } else {
      strWhere = strWhere.replace("h.", "");
      if (stat.startsWith("2d")) {
        listQuery.add(buildMtdSingleStatRatio2dTable(strSelectList, strWhere));
      } else if (stat.startsWith("3d")) {
        listQuery.add(buildMtdSingleStatRatio3dTable(strSelectList, strWhere));
      }
    }
    return listQuery;
  }

  private String buildModeSingleAcovTable(
          String selectList, String strWhere, String stat,
          String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];
    String strGroupBy = " GROUP BY ";
    for (int i = 0; i < groups.length; i++) {
      strGroupBy += (0 < i ? ", " : "") + "  " + groups[i];
    }

    //  build the query components
    String strStat = "SUM(area) / (2*total)";
    if (strStatFlag.charAt(0) != 'A') {
      strStat = "SUM(area) / total";
      strGroupBy += " , fcst_flag";
      strWhere += "   AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }


    if (!strGroupBy.contains("fcst_valid")) {
      if (groups.length > 0) {
        strGroupBy += "  ,";
      }
      strGroupBy += " fcst_valid";

    }

    //mandatory group by fcst_valid and fcst_lead for EE
    if (isEventEqualization && !strGroupBy.contains("fcst_lead")) {
      if (groups.length > 0) {
        strGroupBy += "  ,";
      }
      strGroupBy += " fcst_lead";
    }

    String selectListStat = selectList.replaceAll("h\\.", "").replaceAll(",\\s+$", "");

    //  build the query
    return
            "SELECT " + selectListStat + ", "
                    + "  '' object_id, "
                    + "  '' object_cat, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strStat + " stat_value "
                    + " FROM "
                    + "  mode_header , "
                    + "  mode_obj_single , "
                    + "  mode_cts "
                    + " WHERE " + strWhere + " "
                    + "  AND simple_flag = 1 "
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id "
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id "
                    + "  AND mode_cts.field = 'OBJECT' "
                    + strGroupBy + ";";
  }

  /**
   * Build SQL to gather mode pair data
   *
   * @param strSelectList
   * @param stat
   * @return
   */
  private String buildModePairStatTable(String strSelectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "   AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "   AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String selectListStat = strSelectList.replaceAll("h\\.", "");
    String strGroupListMMI = selectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");
    strGroupListMMI = strGroupListMMI.replaceAll("if\\D+fcst_lead", "fcst_lead");
    //  set the object_id field, depending on the stat
    String objectId = "object_id";
    String objectIdName = "object_id";
    String strGroupBy = "";
    String statName = listStatParse[0];

    if (statName.startsWith("MAXINT")) {
      if (statName.equals("MAXINTF")) {
        objectId = "SUBSTR(object_id, 1, LOCATE('_', object_id)-1) fcst_id";
        objectIdName = "fcst_id";
      } else if (statName.equals("MAXINTO")) {
        objectId = "SUBSTR(object_id, LOCATE('_', object_id)+1) obs_id";
        objectIdName = "obs_id";
      }
      strGroupBy = " GROUP BY " + strGroupListMMI + ",   " + objectIdName;
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.modePairStatField.get(statName);
    strWhere = strWhere.replace("h.", "");
    //  build the query
    return
            "SELECT " + selectListStat + ", "
                    + "  " + objectId + ", "
                    + "  object_cat, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strTableStat + " stat_value "
                    + " FROM "
                    + "  mode_header, "
                    + "  mode_obj_pair "
                    + " WHERE " + strWhere
                    + "  AND mode_header.mode_header_id = mode_obj_pair.mode_header_id "
                    + strGroupBy + ";";
  }


  /**
   * Build SQL to gather mode pair data
   *
   * @param strSelectList
   * @param stat
   * @return
   */
  private String buildMtd3dPairStatTable(String strSelectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatFlag = listStatParse[listStatParse.length - 1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "   AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "   AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "");
    String strGroupListMMI = strSelectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");
    //  set the object_id field, depending on the stat
    String objectId = "object_id";
    String statName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.mtd3dPairStatField.get(statName);

    //  build the query
    return
            "SELECT " + strSelectListStat + ", "
                    + "  " + objectId + ", "
                    + "  cluster_id, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strTableStat + " stat_value "
                    + " FROM mtd_header, mtd_3d_obj_pair  "
                    + " WHERE " + strWhere
                    + " AND mtd_header.mtd_header_id = mtd_3d_obj_pair.mtd_header_id";
  }

  private String buildModeSingleStatTable(
          String selectList, String strWhere, String stat,
          String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }

    String strStatFlag = listStatParse[1];


    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "   AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "   AND simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "   AND matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    //  build the group by clause
    String strGroupBy = "";
    String statName = listStatParse[0];
    if (statName.startsWith("CNT")) {
      strGroupBy = " GROUP BY ";
      for (int i = 0; i < groups.length; i++) {
        strGroupBy += (0 < i ? ", " : "") + "  " + groups[i];
      }
      if (!statName.equals("CNTSUM") && !strGroupBy.contains("fcst_valid")) {
        if (groups.length > 0) {
          strGroupBy += "  ,";
        }
        strGroupBy += " fcst_valid";
      }

      //mandatory group by fcst_valid and fcst_lead for EE
      if (isEventEqualization) {
        if (!strGroupBy.contains("fcst_valid")) {
          if (groups.length > 0) {
            strGroupBy += "  ,";
          }
          strGroupBy += " fcst_valid";
        }
        if (!strGroupBy.contains("fcst_lead")) {
          if (groups.length > 0) {
            strGroupBy += "  ,";
          }
          strGroupBy += " fcst_lead";
        }
      }
    }
    String selectListStat = selectList.replaceAll("h\\.", "");
    //  build the query
    return
            "SELECT " + selectListStat + ", "
                    + "  object_id, "
                    + "  object_cat, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + MVUtil.modeSingleStatField.get(statName) + " stat_value "
                    + " FROM "
                    + "  mode_header , "
                    + "  mode_obj_single , "
                    + "  mode_cts  "
                    + " WHERE " + strWhere
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id "
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id "
                    + "  AND mode_cts.field = 'OBJECT'"
                    + strGroupBy;
  }


  private String buildMtd3dSingleStatTable(
          String selectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String statFlag = listStatParse[listStatParse.length - 1];
    //  build the list of fields involved in the computations


    //  build the object flag where clause
    if (statFlag.charAt(0) != 'A') {
      strWhere += "   AND fcst_flag = " + ('F' == statFlag.charAt(0) ? "1" : "0");
    }
    if (statFlag.charAt(1) != 'A') {
      strWhere += "   AND simple_flag = " + ('S' == statFlag.charAt(1) ? "1" : "0");
    }
    if (statFlag.charAt(2) != 'A') {
      strWhere += "   AND matched_flag = " + ('M' == statFlag.charAt(2) ? "1" : "0");
    }
    String statName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String selectListStat = selectList.replaceAll("h\\.", "");


    //  build the query
    return
            "SELECT " + selectListStat + ", "
                    + "  object_id, "
                    + "  cluster_id, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + mtd3dSingleStatField.get(statName) + " stat_value "
                    + " FROM mtd_header, mtd_3d_obj_single  "
                    + " WHERE " + strWhere
                    + " AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";
  }


  private String buildMtd2dStatTable(
          String selectList, String strWhere, String stat) {


    String[] listStatParse = stat.split("_");
    String strStatFlag = listStatParse[listStatParse.length - 1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "   AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "   AND simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "   AND matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strSelectListStat = selectList.replaceAll("h\\.", "");
    //  build the query
    return
            "SELECT " + strSelectListStat + ", "
                    + "  object_id, "
                    + "  cluster_id, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + MVUtil.mtd2dStatField.get(strStatName) + " stat_value "
                    + " FROM mtd_header, mtd_2d_obj  "
                    + " WHERE " + strWhere
                    + " AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";
  }

  private String buildModeSingleStatRatioTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");
    strWhere = strWhere.replaceAll("h\\.", "");

    return
            "SELECT " + strSelectListStat + ", "
                    + "  object_id, "
                    + "  object_cat, "
                    + "  area, "
                    + "  total, "
                    + "  fcst_flag, "
                    + "  simple_flag, "
                    + "  matched_flag "
                    + " FROM "
                    + "  mode_header , "
                    + "  mode_obj_single , "
                    + "  mode_cts  "
                    + " WHERE " + strWhere
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id "
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id"
                    + "  AND mode_cts.field = 'OBJECT'";
  }

  private String buildMtdSingleStatRatio2dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
            "SELECT " + strSelectListStat + ", "
                    + "  object_id, "
                    + "  cluster_id, "
                    + "  area, "
                    + "  fcst_flag, "
                    + "  simple_flag, "
                    + "  matched_flag "
                    + " FROM mtd_header, mtd_2d_obj "
                    + " WHERE " + strWhere
                    + "  AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";

  }

  private String buildMtdSingleStatRatio3dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
            "SELECT " + strSelectListStat + ", "
                    + "  object_id, "
                    + "  cluster_id, "
                    + "  volume, "
                    + "  fcst_flag, "
                    + "  simple_flag, "
                    + "  matched_flag "
                    + " FROM mtd_header, mtd_3d_obj_single "
                    + " WHERE " + strWhere
                    + "  AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";

  }

  private String buildModeSingleStatDiffTable(
          String strSelectList, String strWhere, String stat,
          String table1, String table2,
          Map.Entry[] listSeries) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "   AND s.fcst_var = s2.fcst_var";


    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.modeSingleStatField.get(strStatName);
    String statName = strTableStat.split("\\(")[0];
    String[] strTableStats = new String[2];
    if (strTableStat.contains("object_id")) {
      strTableStats[0] = statName + "( s.object_id)";
      strTableStats[1] = statName + "( s2.object_id)";
    } else {
      strTableStats[0] = "s." + "stat_value";
      strTableStats[1] = "s2." + "stat_value";

    }

    //  build the query COUNT(object_id)
    String result =
            "SELECT " + strSelectListStat + ", "
                    + "  s.object_id, "
                    + "  s.object_cat, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value "
                    + " FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2 "
                    + " WHERE "
                    + strWhere + " "
                    + " AND SUBSTRING(s.object_id, -3) = SUBSTRING(s2.object_id,  -3) ";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + "s.stat_value" + " != -9999"
              + " AND " + "s2.stat_value" + " != -9999"
              + " AND s.fcst_valid = s2.fcst_valid "
              + " AND s.fcst_lead = s2.fcst_lead";
      for (int i = 0; i < listSeries.length; i++) {
        result = result + " AND s." + listSeries[i].getKey()
                + " = s2." + listSeries[i].getKey();
      }
      result = result + ";";
    }
    return result;
  }

  private String buildMtd3dSingleStatDiffTable(
          String strSelectList, String strWhere,
          String stat,
          String table1, String table2) {

    //  parse the stat into the stat name and the object flags
    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "   AND s.fcst_var = s2.fcst_var";


    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = mtd3dSingleStatField.get(strStatName);
    String statName = strTableStat.split("\\(")[0];
    String[] strTableStats = new String[2];
    if (strTableStat.contains("object_id")) {
      strTableStats[0] = statName + "( s.object_id)";
      strTableStats[1] = statName + "( s2.object_id)";
    } else {
      strTableStats[0] = "s." + "stat_value";
      strTableStats[1] = "s2." + "stat_value";

    }

    String result =
            "SELECT " + strSelectListStat + ", "
                    + "  s.object_id, "
                    + "  s.cluster_id, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value "
                    + " FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2 "
                    + " WHERE "
                    + strWhere + " "
                    + "  AND SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) = SUBSTRING(s2.object_id,  "
                    + "LOCATE('_', s "
                    + ".object_id)+1) ";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0]
              + " != -9999 AND " + strTableStats[1] + " != -9999;";
    }
    return result;
  }

  private String buildMtd2dStatDiffTable(
          String strSelectList, String strWhere, String stat,
          String table1, String table2) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "   AND s.fcst_var = s2.fcst_var";


    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.mtd2dStatField.get(strStatName);
    String statName = strTableStat.split("\\(")[0];
    String[] strTableStats = new String[2];
    if (strTableStat.contains("object_id")) {
      strTableStats[0] = statName + "( s.object_id)";
      strTableStats[1] = statName + "( s2.object_id)";
    } else {
      strTableStats[0] = "s." + "stat_value";
      strTableStats[1] = "s2." + "stat_value";

    }

    String result =
            "SELECT " + strSelectListStat + ", "
                    + "  s.object_id, "
                    + "  s.cluster_id, "
                    + "  '" + stat + "' stat_name, "
                    + "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value "
                    + " FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2 "
                    + " WHERE "
                    + strWhere + " "
                    + "  AND SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) "
                    + "= SUBSTRING(s2.object_id,  LOCATE('_', s.object_id)+1) ";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0]
              + " != -9999 AND " + strTableStats[1] + " != -9999;";
    }
    return result;
  }


  @Override
  public List<String> buildPlotModeEventEqualizeSql(
          MVPlotJob job, MVOrderedMap mapPlotFixPerm,
          MVOrderedMap mapPlotFixVal)
          throws ValidationException {
    MVOrderedMap fcstVarPat = new MVOrderedMap();

    //  determine if the plot job is for stat data or MODE data
    List<String> tableHeaderSqlType;
    if (job.isModeJob()) {
      tableHeaderSqlType = modeHeaderSqlType;
    } else {
      tableHeaderSqlType = statHeaderSqlType;
    }


    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job,
            job.isModeJob() || job.isMtdJob());

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && job.getPlotCond().length() > 0) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + " ";
    }


    //  remove multiple dep group capability
    MVOrderedMap[] listDep = job.getDepGroups();
    if (1 != listDep.length) {
      throw new ValidationException("unexpected number of <dep> groups: " + listDep.length);
    }
    MVOrderedMap mapDepGroup = listDep[0];


    /*
     *  Build queries for statistics on both the y1 and y2 axes
     */

    List<String> listSql = new ArrayList<>();
    for (int intY = 1; intY <= 2; intY++) {

      //  get the dep values for the current dep group
      MVOrderedMap mapDep = (MVOrderedMap) mapDepGroup.get("dep" + intY);

      //  establish lists of entires for each group of variables and values
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val())
              .getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new ValidationException("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length) {
        throw new ValidationException("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2
      if (!job.getPlotTmpl().equals(
              "eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        throw new ValidationException("no Y1 series stat found");
      }
      if (2 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        continue;
      }


      /*
       *  Construct query components from the series variable/value pairs
       */

      //  build the select list and where clauses for the series variables and values

      String strWhere = strPlotFixWhere;
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(job.isModeJob() || job.isMtdJob(),
              tableHeaderSqlType,
              listSeries, strWhere, true);
      String selectList = buildMysqlQueryStrings.getSelectList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields
      // are not present in the select list and temp table list, add them
      if (!selectList.contains("fcst_init")) {
        selectList += ",   h.fcst_init";
      }
      if (!selectList.contains("fcst_valid")) {
        selectList += ",   h.fcst_valid";
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
              tableHeaderSqlType,
              listSeries, strWhere, false);
      String selectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields
      // are not present in the select list and temp table list, add them
      if (!selectPlotList.contains("fcst_init") && !selectPlotList.contains("init_hour")) {
        if (job.isModeJob()) {
          selectPlotList += ",   h.fcst_init";
        } else {
          selectPlotList += ",  " + " ld.fcst_init_beg";
        }
      }
      if (!selectPlotList.contains("fcst_valid")) {
        if (job.isModeJob()) {
          selectPlotList += ",   h.fcst_valid";
        } else {
          selectPlotList += ",  " + " ld.fcst_valid_beg";
        }
      }

      if (!selectList.contains("fcst_lead")) {
        if (job.getEventEqual()) {
          selectList += ",  " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + " WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead "
                  + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + " WHERE model = h.model) ) fcst_lead";
        } else {
          selectList += ",  " + " h.fcst_lead";
        }
        selectPlotList += ",  " + " h.fcst_lead";
      }

      /*
       *  Construct the query components for the independent variable and values
       */

      //  validate and get the type and values for the independent variable
      String strIndyVarType;
      String strIndyVar = job.getIndyVar();
      String[] listIndyVal = job.getIndyVal();
      if (!tableHeaderSqlType.contains(strIndyVar)) {
        throw new ValidationException("unrecognized indep " + (job.isModeJob() ? "mode" : "stat")
                + "_header field: " + strIndyVar);
      }
      if (1 > listIndyVal.length) {
        throw new ValidationException("no independent variable values specified");
      }

      //  construct the select list item, where clause
      // and temp table entry for the independent variable
      if (!selectList.contains(strIndyVar)) {
        selectList += ",   " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(), true);
        selectPlotList += ",   " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                true);
      }
      String strIndyVarFormatted = formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
              false);
      if (strIndyVar.equals("fcst_lead") && job.getEventEqual()) {
        strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                + " WHERE model = h.model) is NULL , "
                + strIndyVarFormatted + " , " + strIndyVarFormatted
                + " +  (select fcst_lead_offset FROM model_fcst_lead_offset "
                + " WHERE model = h.model) ) ";
      }
      strWhere += (!strWhere.isEmpty() ? "  AND " : "") + strIndyVarFormatted +
              " IN [" + MVUtil.buildValueList(job.getIndyVal()) + "] ";

      //  add fcst_var to the select list and temp table entries
      selectList += ",   h.fcst_var";
      selectPlotList += ",   h.fcst_var";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();

        }
      }

      /*
       *  Construct a query for each fcst_var/stat pair
       */

      //  determine how many queries are needed to gather that stat information
      int intNumQueries;
      String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(mapDep);

      intNumQueries = listFcstVarStat.length;

      //  build a query for each fcst_var/stat pair
      // or a just single query for contingency tables or partial sums
      for (int intFcstVarStat = 0; intFcstVarStat < intNumQueries; intFcstVarStat++) {

        //  get the current fcst_var/stat pair
        String strFcstVar = listFcstVarStat[intFcstVarStat][0];

        //  build the fcst_var where clause criteria
        String strFcstVarClause = "= '" + strFcstVar + "'";
        Matcher matProb = MVUtil.prob.matcher(strFcstVar);
        if (matProb.matches() && strFcstVar.contains("*")) {
          Pattern patFcstVar = Pattern.compile(
                  strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
          if (!fcstVarPat.containsKey(patFcstVar)) {
            fcstVarPat.put(patFcstVar, MVUtil.replaceSpecialChars(strFcstVar));
          }
          strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
        }

        //  build the mode SQL
        String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;
        strWhereFcstVar = strWhereFcstVar.replace("h.", "");
        selectList = selectList.replace("h.", "");
        listSql.addAll(buildModeStatEventEqualizeSql(selectList, strWhereFcstVar));

      }

    }


    //remove duplicated queries
    listSql = new ArrayList<>(new LinkedHashSet<>(listSql));

    return listSql;
  }


  private List buildModeStatEventEqualizeSql(String strSelectList, String strWhere) {

    List listQuery = new ArrayList();

    listQuery.add(buildModeSingleStatRatioEventEqualizeTable(strSelectList, strWhere));

    return listQuery;
  }


  private String buildModeSingleStatRatioEventEqualizeTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
            "SELECT " + strSelectListStat + ", "
                    + "  object_id, "
                    + "  object_cat, "
                    + "  area, "
                    + "  total, "
                    + "  fcst_flag, "
                    + "  simple_flag, "
                    + "  matched_flag "
                    + " FROM "
                    + " mode_header , "
                    + " mode_obj_single, "
                    + " mode_cts  "
                    + " WHERE " + strWhere
                    + " AND mode_obj_single.mode_header_id = mode_header.mode_header_id"
                    + " AND mode_cts.mode_header_id = mode_obj_single.mode_header_id"
                    + " AND mode_cts.field = 'OBJECT'";
  }

  @Override
  public String buildAndExecuteQueriesForHistJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm,
          PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException {
    String strTempList = "";
    String strSelectList = "";
    String strWhereSeries = "";
    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();


    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSqlType.contains(strSeriesField)) {
        throw new ValidationException("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
              + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false)
              + " IN [" + MVUtil.buildValueList(listSeriesVal) + "] ";

    }

    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());


    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);
    String strNumSelect = "";
    String type = "";
    String table = "";
    String tableBins = "";
    String binColumnName = null;

    if (job.getPlotTmpl().startsWith("relp")) {
      type = "ens";
      table = "relp";
      tableBins = "relp_ens";
    } else if (job.getPlotTmpl().startsWith("rhist")) {
      type = "rank";
      table = "rhist";
      tableBins = "rhist_rank";
    } else if (job.getPlotTmpl().startsWith("phist")) {
      type = "bin";
      table = "phist";
      tableBins = "phist_bin";
      binColumnName = "bin_size";
    }
    strWhere = strWhere.replaceAll("h\\.n_" + type, "ld.n_" + type);

    String dbList = "[";

    for (String cdbName : job.getCurrentDBName()) {
      if (dbList.length() > 1) {
        dbList += ", ";
      }
      dbList += "\'" + cdbName + "\'";
    }
    dbList += "]";

    // find the line_data documents where the header has the selected forecast variable
    strNumSelect = "SELECT DISTINCT ld.n_" + type
            + " FROM `" + getBucket().name() + "` AS h "
            + " INNER JOIN `" + getBucket()
            .name() + "` AS ld on ld.header_id = meta(h).id"
            + " WHERE " + strWhere
            + " AND ld.type = \'line\' AND ld.line_type = \'" + table + "\'"
            + " AND h.type = \'header\' AND h.header_type = \'stat\' "
            + " AND h.dbname IN " + dbList;

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + " ");
      printStreamSql.flush();
    }


    //  run the rank number query and warn, if necessary
    String strMsg = "";
    List<String> listNum = getNumbers(strNumSelect, dbList);


    if (listNum.isEmpty()) {
      throw new ValidationException("no " + type + "  data found");
    } else if (1 < listNum.size()) {
      strMsg = "  **  WARNING: multiple n_" + type + " values found for search criteria: ";
      for (int i = 0; i < listNum.size(); i++) {
        strMsg += (0 < i ? ", " : "") + listNum.get(i);
      }
      printStream.println(strMsg);
    }


    //  build a query for the rank data
    strWhere = strWhere + strWhereSeries;
    String strPlotDataSelect =
            "SELECT hbin.i_value, ";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ", ";
    }


    strPlotDataSelect = strPlotDataSelect + " SUM(TONUMBER(hbin." + type + "_i)) AS stat_value";

    if (binColumnName != null) {
      strPlotDataSelect = strPlotDataSelect + ", ld." + binColumnName;
    }
    strPlotDataSelect = strPlotDataSelect
            + " FROM `" + getBucket().name() + "` AS h "
            + " INNER JOIN `" + getBucket()
            .name() + "` AS ld on ld.header_id = meta(h).id"
            + " UNNEST ld." + tableBins + "[0:" + listNum.get(0) + "] AS hbin"
            + " WHERE " + strWhere
            + " AND ld.type = \'line\' AND ld.line_type = \'" + table + "\'"
            + " AND h.type = \'header\' AND h.header_type = \'stat\'"
            + " AND h.dbname IN " + dbList
            + " GROUP BY hbin.i_value";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
    }
    // for phist plots with bin_size, add to group by as well as select
    if (binColumnName != null) {
      strPlotDataSelect = strPlotDataSelect + ", ld." + binColumnName;
    }
    strPlotDataSelect = strPlotDataSelect + " ORDER BY TONUMBER(hbin.i_value);";
    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + " ");
      printStreamSql.flush();
    }

    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      MvResponse mvResponse = executeQueriesAndSaveToFile(queries, strDataFile,
              job.getCalcCtc() || job.getCalcSl1l2()
                      || job.getCalcSal1l2() || job.getCalcGrad(),
              job.getCurrentDBName().get(i), i == 0);
      if (mvResponse.getInfoMessage() != null) {
        printStream.println(mvResponse.getInfoMessage());
      }
      printStream.println();
    }
    return strMsg;
  }


  @Override
  public int buildAndExecuteQueriesForRocRelyJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm,
          PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException {
    String strSelectList = "";
    String strTempList = "";
    String strWhereSeries = "";

    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();

    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSqlType.contains(strSeriesField)) {
        throw new ValidationException("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
              + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += " AND " + formatField(strSeriesField, false, false)
              + " IN [" + MVUtil.buildValueList(listSeriesVal) + "] ";

    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());

    boolean boolRelyPlot = job.getPlotTmpl().startsWith("rely");

    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);
    strWhere = strWhere + strWhereSeries;

    String dbList = "[";

    for (String cdbName : job.getCurrentDBName()) {
      if (dbList.length() > 1) {
        dbList += ", ";
      }
      dbList += "\'" + cdbName + "\'";
    }
    dbList += "]";

    //  check to ensure only a single obs_thresh is used
    String strObsThreshSelect =
            "SELECT DISTINCT h.obs_thresh "
                    + " FROM `" + getBucket().name() + "` AS h "
                    + " INNER JOIN `" + getBucket().name() + "` AS ld ON ld.header_id = meta(h).id"
                    + " WHERE " + strWhere + (strWhere.equals("") ? "" : " AND ")
                    + " ld.type = \'line\' AND ld.line_type = "
                    + (boolRelyPlot || job.getRocPct() ? "\'pct\'" : "\'ctc\'")
                    + " AND h.type = \'header\' AND h.header_type = \'stat\'"
                    + " AND h.dbname IN " + dbList
                    + " ORDER BY h.obs_thresh;";

    if (printStreamSql != null) {
      printStreamSql.println(strObsThreshSelect + " ");
    }


    //  build the query depending on the type of data requested
    String strPlotDataSelect = "";
    List<String> listFcstThresh = new ArrayList<>();
    if (boolRelyPlot || job.getRocPct()) {

      //  check to ensure only a single fcst_thresh is used
      String strFcstThreshSelect = "SELECT DISTINCT h.fcst_thresh ";

      strFcstThreshSelect = strFcstThreshSelect
              + " FROM `" + getBucket().name() + "` AS h "
              + " INNER JOIN `" + getBucket()
              .name() + "` AS ld ON ld.header_id = meta(h).id"
              + " WHERE " + strWhere + (strWhere.equals("") ? "" : " AND ")
              + " ld.type = \'line\' AND ld.line_type = "
              + (boolRelyPlot || job.getRocPct() ? "\'pct\'" : "\'ctc\'")
              + " AND h.type = \'header\' AND h.header_type = \'stat\'"
              + " AND h.dbname IN " + dbList
              + " ORDER BY h.fcst_thresh;";


      if (printStreamSql != null) {
        printStreamSql.println(strFcstThreshSelect + " ");
      }

      listFcstThresh = getNumbers(strFcstThreshSelect, job.getCurrentDBName().get(0));


      //  build the plot data sql
      strPlotDataSelect =
              "SELECT ";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ", ";
      }
      if (boolRelyPlot) {
        strPlotDataSelect = strPlotDataSelect
                + " thresh.i_value,"
                + " TONUMBER(thresh.thresh_i) AS thresh_i,"
                + " thresh.oy_i,"
                + " thresh.on_i";

        strPlotDataSelect = strPlotDataSelect
                + " FROM `" + getBucket().name() + "` AS h "
                + " INNER JOIN `" + getBucket()
                .name() + "` AS ld on ld.header_id = meta(h).id"
                + " UNNEST ld.pct_thresh AS thresh"
                + " WHERE " + strWhere + (strWhere.equals("") ? "" : " AND ")
                + " ld.type = \'line\' AND ld.line_type = \'pct\'"
                + " AND h.type = \'header\' AND h.header_type = \'stat\'"
                + " AND h.dbname IN " + dbList
                + " ORDER BY thresh_i";
      } else {
        strPlotDataSelect = strPlotDataSelect
                + " thresh.i_value, "
                + " thresh.thresh_i, "
                + " SUM(TONUMBER(thresh.oy_i)) oy_i, "
                + " SUM(TONUMBER(thresh.on_i)) on_i ";

        strPlotDataSelect = strPlotDataSelect
                + " FROM `" + getBucket().name() + "` AS h "
                + " INNER JOIN `" + getBucket()
                .name() + "` AS ld on ld.header_id = meta(h).id"
                + " UNNEST ld.pct_thresh AS thresh"
                + " WHERE " + strWhere + (strWhere.equals("") ? "" : " AND ")
                + " ld.type = \'line\' AND ld.line_type = \'pct\'"
                + " AND h.type = \'header\' AND h.header_type = \'stat\'"
                + " AND h.dbname IN " + dbList
                + " GROUP BY thresh.i_value, thresh.thresh_i"
                + " ORDER BY thresh.i_value";
        if (listSeries.length > 0) {
          strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
        }
        strPlotDataSelect = strPlotDataSelect + ";";
      }

    } else if (job.getRocCtc()) {

      strPlotDataSelect =
              "SELECT h.fcst_thresh thresh, ";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ", ";
      }
      strPlotDataSelect = strPlotDataSelect
              + " SUM(tonumber(ld.fy_oy)) fy_oy, "
              + " SUM(tonumber(ld.fy_on)) fy_on, "
              + " SUM(tonumber(ld.fn_oy)) fn_oy, "
              + " SUM(tonumber(ld.fn_on)) fn_on "
              + " FROM `" + getBucket().name() + "` AS h "
              + " INNER JOIN `" + getBucket()
              .name() + "` AS ld ON ld.header_id = meta(h).id "
              + " WHERE " + strWhere + " AND ld.type = \'line\' AND ld.line_type = \'ctc\' "
              + " AND h.type = \'header\' AND h.header_type = \'stat\' "
              + " AND h.dbname IN " + dbList
              + " GROUP BY "
              + " h.fcst_thresh";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
      }
      strPlotDataSelect = strPlotDataSelect + ";";

    }

    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + " ");
    }


    //  if the query does not return data from a expected obs_thresh, throw an error
    int intNumDepSeries = 1;
    Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
    for (Map.Entry aListSeries1Val : listSeries1Val) {
      String[] listVal = (String[]) aListSeries1Val.getValue();
      intNumDepSeries *= listVal.length;
    }
    List<String> listObsThresh = getNumbers(strObsThreshSelect, job.getCurrentDBName().get(0));
    if (intNumDepSeries < listObsThresh.size()) {
      String obsThreshMsg = "ROC/Reliability plots must contain data from only a single obs_thresh,"
              + " instead found " + listObsThresh.size();
      for (int i = 0; i < listObsThresh.size(); i++) {
        obsThreshMsg += (0 == i ? ": " : ", ") + listObsThresh.toString();
      }
      throw new ValidationException(obsThreshMsg);
    }

    if (listObsThresh.isEmpty()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data "
              + "from at least one obs_thresh ";
      throw new ValidationException(strObsThreshMsg);
    }

    //  if the query for a PCT plot does not return data from a single fcst_thresh, throw an error
    if (job.getRocPct() && intNumDepSeries < listFcstThresh.size()) {
      String fcstThreshMsg = "ROC/Reliability plots using PCTs must contain data "
              + "from only a single fcst_thresh, "
              + "instead found " + listFcstThresh.size();
      for (int i = 0; i < listFcstThresh.size(); i++) {
        fcstThreshMsg += (0 == i ? ":" : "") + "   " + listFcstThresh.toString();
      }
      throw new ValidationException(fcstThreshMsg.toString());
    }
    if (job.getRocPct() && listObsThresh.isEmpty()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data "
              + "from at least one obs_thresh ";
      throw new ValidationException(strObsThreshMsg);
    }

    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      MvResponse mvResponse = executeQueriesAndSaveToFile(queries, strDataFile,
              job.isCalcStat(),
              job.getCurrentDBName().get(i),
              i == 0);
      if (mvResponse.getInfoMessage() != null) {
        printStream.println(mvResponse.getInfoMessage());
      }
    }

    return intNumDepSeries;
  }

  @Override
  public int buildAndExecuteQueriesForEclvJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm,
          PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException {
    String strSelectList = "";
    String strTempList = "";
    String strWhereSeries = "";

    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();

    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSqlType.contains(strSeriesField)) {
        throw new ValidationException("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
              + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false)
              + " IN [" + MVUtil.buildValueList(listSeriesVal) + "] ";


    }
    if (!strSelectList.toString().contains("fcst_valid")) {
      strSelectList += ",  " + " ld.fcst_valid_beg";
    }
    if (!strSelectList.toString().contains("fcst_lead")) {
      strSelectList += ",  " + " ld.fcst_lead";
    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());


    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);

    strWhere = strWhere.replaceAll("h\\.n_pnt", "ld.n_pnt");
    String strNumSelect =
            "SELECT DISTINCT "
                    + "  ld.n_pnt "
                    + " FROM "
                    + "  stat_header h, "
                    + "  line_data_eclv ld "
                    + " WHERE "
                    + strWhere
                    + "  AND h.stat_header_id = ld.stat_header_id;";

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + " ");
    }
    //  run the rank number query and warn, if necessary
    String strMsg = "";
    List<String> listNum = getNumbers(strNumSelect, job.getCurrentDBName().get(0));


    if (listNum.isEmpty()) {
      throw new ValidationException("no pnt  data found");
    } else if (1 < listNum.size()) {
      strMsg = "  **  WARNING: multiple n_pnt values found for search criteria: ";
      for (int i = 0; i < listNum.size(); i++) {
        strMsg += (0 < i ? ", " : "") + listNum.get(i);
      }
      printStream.println(strMsg);
    }


    Map<String, Integer> pctThreshInfo;
    MVOrderedMap[] series = MVUtil.permute(job.getSeries1Val().convertFromSeriesMap()).getRows();
    for (int seriesInd = 0; seriesInd < series.length; seriesInd++) {
      MVOrderedMap ser = series[seriesInd];
      String[] serName = ser.getKeyList();
      for (int serNameInd = 0; serNameInd < serName.length; serNameInd++) {
        String strSelPctThresh = "SELECT DISTINCT ld.n_pnt FROM   stat_header h,   "
                + "line_data_eclv ld,  line_data_eclv_pnt ldt  WHERE "
                + serName[serNameInd] + " = '"
                + ser.getStr(serName[serNameInd]) + "' AND "
                + strWhere
                + "  AND ld.stat_header_id = h.stat_header_id "
                + "AND ld.line_data_id = ldt.line_data_id;";
        printStreamSql.println(strSelPctThresh + " ");

        //  run the PCT thresh query
        pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(0));
        if (1 != pctThreshInfo.get("numPctThresh")) {
          throw new ValidationException("number of ECLV pnts (" + pctThreshInfo.get(
                  "numPctThresh") + ") not distinct for " + serName[serNameInd] + " = '" + ser.getStr(
                  serName[serNameInd]));
        } else if (1 > pctThreshInfo.get("numPctThresh")) {
          throw new ValidationException("invalid number of ECLV pnts (" + pctThreshInfo.get(
                  "numPctThresh") + ") found for" + serName[serNameInd]
                  + " = '" + ser.getStr(serName[serNameInd]) + "'");
        }
      }
    }
    strWhere = strWhere + strWhereSeries;

    //  build the query depending on the type of data requested
    String strPlotDataSelect = "";

    //  build the plot data sql
    strPlotDataSelect =
            "SELECT ";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ", ";
    }
    if (listPlotFixVal.length > 0) {
      for (Map.Entry aListPlotFixVal : listPlotFixVal) {
        String strField = (String) aListPlotFixVal.getKey();
        strPlotDataSelect = strPlotDataSelect + strField + ", ";
      }
    }

    strPlotDataSelect = strPlotDataSelect
            + "  ldt.x_pnt_i, "
            + "  ldt.y_pnt_i  ";

    strPlotDataSelect = strPlotDataSelect + " FROM "
            + "  stat_header h, "
            + "  line_data_eclv ld, "
            + "  line_data_eclv_pnt ldt "
            + " WHERE "
            + strWhere
            + "  AND h.stat_header_id = ld.stat_header_id "
            + "  AND ld.line_data_id = ldt.line_data_id";

    strPlotDataSelect = strPlotDataSelect + ";";


    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + " ");
    }


    //  if the query does not return data from a expected obs_thresh, throw an error
    int intNumDepSeries = 1;
    Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
    for (Map.Entry aListSeries1Val : listSeries1Val) {
      String[] listVal = (String[]) aListSeries1Val.getValue();
      intNumDepSeries *= listVal.length;
    }


    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      MvResponse mvResponse = executeQueriesAndSaveToFile(queries, strDataFile,
              job.isCalcStat(),
              job.getCurrentDBName().get(i),
              i == 0);
      if (mvResponse.getInfoMessage() != null) {
        printStream.println(mvResponse.getInfoMessage());
      }
    }

    return intNumDepSeries;
  }

  /**
   * Format the input field according to the table that it is stored in, and whether or not it needs
   * to be derived or formatted as a date.  Stat fields must be differentiated from MODE fields.
   * Also, fields intended for the select list, as opposed to a where clause, must be specified.
   *
   * @param field  MET output header field to format
   * @param mode   specifies whether or not the field is a stat_header or mode_header field
   * @param fmtSel specifies whether the formatted field will be used for the select list (true) or
   *               a where clause (false)
   * @return the formatted field
   */
  private String formatField(String field, boolean mode, boolean fmtSel) {
    if (field.equals("init_hour")) {
      return (mode ? "HOUR(h.fcst_init)" : "HOUR(ld.fcst_init_beg)") + (fmtSel ? " init_hour" : "");
    } else if (field.equals("valid_hour")) {
      return (mode ? "HOUR(h.fcst_valid)" : "HOUR(ld.fcst_valid_beg)")
              + (fmtSel ? " valid_hour" : "");
    } else if (field.equals("fcst_init") && fmtSel) {
      return " fcst_init";
    } else if (field.equals("fcst_init_beg") && fmtSel) {
      return " fcst_init_beg";
    } else if (field.equals("fcst_init_beg") && !fmtSel) {
      return "ld.fcst_init_beg";
    } else if (field.equals("fcst_valid") && fmtSel) {
      return " fcst_valid";
    } else if (field.equals("fcst_valid_beg") && fmtSel) {
      return " fcst_valid_beg";
    } else if (field.equals("fcst_valid_beg") && !fmtSel) {
      return "ld.fcst_valid_beg";
    } else if (field.equals("fcst_lead")) {
      return mode ? "h.fcst_lead" : "ld.fcst_lead";
    } else {
      return "h." + field;
    }
  }


  private String formatField(String field, boolean mode) {
    return formatField(field, mode, true);
  }

  private BuildMysqlQueryStrings build(
          boolean boolModePlot,
          List<String> tableHeaderSqlType,
          Map.Entry[] listSeries, String strWhere,
          boolean isFormatSelect) throws ValidationException {

    BuildMysqlQueryStrings buildMysqlQueryStrings = new BuildMysqlQueryStrings(boolModePlot,
            tableHeaderSqlType,
            listSeries, strWhere,
            isFormatSelect);
    for (Map.Entry entry : listSeries) {

      //  get the current series field and values
      String field = entry.getKey().toString();

      //  validate the series field and get its type
      String strTempType;
      if (!tableHeaderSqlType.contains(field)) {
        throw new ValidationException("unrecognized " + (boolModePlot ? "mode" : "stat")
                + "_header field: " + field);
      }
      //strTempType = tableHeaderSqlType.get(field);

      //  build the select list element, where clause and temp table list element
      if (buildMysqlQueryStrings.getSelectList().length() == 0) {
        if (isFormatSelect) {
          buildMysqlQueryStrings.setSelectList(
                  buildMysqlQueryStrings.getSelectList() + "  "
                          + formatField(field, boolModePlot, true));
        } else {
          buildMysqlQueryStrings
                  .setSelectList(buildMysqlQueryStrings.getSelectList() + "  " + field);

        }
      } else {
        if (isFormatSelect) {
          buildMysqlQueryStrings.setSelectList(
                  buildMysqlQueryStrings.getSelectList() + ", "
                          + "  " + formatField(field, boolModePlot, true));

        } else {
          buildMysqlQueryStrings
                  .setSelectList(buildMysqlQueryStrings.getSelectList() + ", " + "  " + field);
        }
      }
      buildMysqlQueryStrings.setWhere(
              buildMysqlQueryStrings.getWhere()
                      + (buildMysqlQueryStrings.getWhere().isEmpty() ? "  " : "  AND ")
                      + formatField(field, boolModePlot, false)
                      + " IN [" + MVUtil.buildValueList((String[]) entry.getValue()) + "] ");


    }
    return buildMysqlQueryStrings;
  }

}


/**
 * MysqlAppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import edu.ucar.metviewer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public class MysqlAppDatabaseManager extends MysqlDatabaseManager implements AppDatabaseManager {


  private static final Logger logger = LogManager.getLogger("MysqlAppDatabaseManager");
  public static final String INSERT_INTO_MODE_SINGLE = "INSERT INTO mode_single";
  private final Map<String, String> statHeaderSQLType = new HashMap<>();
  private final Map<String, String> modeHeaderSQLType = new HashMap<>();
  private final Map<String, String> modeSingleStatField = new HashMap<>();


  public MysqlAppDatabaseManager(DatabaseInfo databaseInfo) throws SQLException {
    super(databaseInfo);
    statHeaderSQLType.put("model", "VARCHAR(64)");
    statHeaderSQLType.put("descr", "VARCHAR(64)");
    statHeaderSQLType.put("fcst_lead", "INT");
    statHeaderSQLType.put("fcst_valid_beg", "DATETIME");
    statHeaderSQLType.put("fcst_valid_end", "DATETIME");
    statHeaderSQLType.put("fcst_init_beg", "DATETIME");
    statHeaderSQLType.put("obs_lead", "INT UNSIGNED");
    statHeaderSQLType.put("obs_valid_beg", "DATETIME");
    statHeaderSQLType.put("obs_valid_end", "DATETIME");
    statHeaderSQLType.put("init_hour", "INT UNSIGNED");
    statHeaderSQLType.put("valid_hour", "INT UNSIGNED");
    statHeaderSQLType.put("fcst_var", "VARCHAR(64)");
    statHeaderSQLType.put("fcst_lev", "VARCHAR(16)");
    statHeaderSQLType.put("obs_var", "VARCHAR(64)");
    statHeaderSQLType.put("obs_lev", "VARCHAR(16)");
    statHeaderSQLType.put("obtype", "VARCHAR(32)");
    statHeaderSQLType.put("vx_mask", "VARCHAR(32)");
    statHeaderSQLType.put("interp_mthd", "VARCHAR(16)");
    statHeaderSQLType.put("interp_pnts", "INT UNSIGNED");
    statHeaderSQLType.put("fcst_thresh", "VARCHAR(16)");
    statHeaderSQLType.put("obs_thresh", "VARCHAR(16)");

    modeHeaderSQLType.put("model", "VARCHAR(64)");
    modeHeaderSQLType.put("descr", "VARCHAR(64)");
    modeHeaderSQLType.put("fcst_lead", "INT UNSIGNED");
    modeHeaderSQLType.put("fcst_valid", "DATETIME");
    modeHeaderSQLType.put("fcst_accum", "INT UNSIGNED");
    modeHeaderSQLType.put("fcst_init", "DATETIME");
    modeHeaderSQLType.put("obs_lead", "INT UNSIGNED");
    modeHeaderSQLType.put("obs_valid", "DATETIME");
    modeHeaderSQLType.put("obs_accum", "INT UNSIGNED");
    modeHeaderSQLType.put("init_hour", "INT UNSIGNED");
    modeHeaderSQLType.put("valid_hour", "INT UNSIGNED");
    modeHeaderSQLType.put("fcst_rad", "INT UNSIGNED");
    modeHeaderSQLType.put("fcst_thr", "VARCHAR(16)");
    modeHeaderSQLType.put("obs_rad", "INT UNSIGNED");
    modeHeaderSQLType.put("obs_thr", "VARCHAR(16)");
    modeHeaderSQLType.put("fcst_var", "VARCHAR(64)");
    modeHeaderSQLType.put("fcst_lev", "VARCHAR(16)");
    modeHeaderSQLType.put("obs_var", "VARCHAR(64)");
    modeHeaderSQLType.put("obs_lev", "VARCHAR(16)");

    modeSingleStatField.put("ACOV", "SUM(area)");
    modeSingleStatField.put("CNT", "COUNT(object_id)");
    modeSingleStatField.put("CNTSUM", "COUNT(object_id)");
    modeSingleStatField.put("CENTX", "centroid_x");
    modeSingleStatField.put("CENTY", "centroid_y");
    modeSingleStatField.put("CENTLAT", "centroid_lat");
    modeSingleStatField.put("CENTLON", "centroid_lon");
    modeSingleStatField.put("AXAVG", "axis_avg");
    modeSingleStatField.put("LEN", "length");
    modeSingleStatField.put("WID", "width");
    modeSingleStatField.put("ASPECT", "IF((length/width) < (width/length), length/width, width/length)");
    modeSingleStatField.put("AREA", "area");
    modeSingleStatField.put("AREAFIL", "area_filter");
    modeSingleStatField.put("AREATHR", "area_thresh");
    modeSingleStatField.put("CURV", "curvature");
    modeSingleStatField.put("CURVX", "curvature_x");
    modeSingleStatField.put("CURVY", "curvature_y");
    modeSingleStatField.put("CPLX", "complexity");
    modeSingleStatField.put("INT10", "intensity_10");
    modeSingleStatField.put("INT25", "intensity_25");
    modeSingleStatField.put("INT50", "intensity_50");
    modeSingleStatField.put("INT75", "intensity_75");
    modeSingleStatField.put("INT90", "intensity_90");
    modeSingleStatField.put("INTN", "intensity_nn");
    modeSingleStatField.put("INTSUM", "intensity_sum");

  }

  @Override
  public List<String> getListStat(String strFcstVar, String[] currentDBName) {
    List<String> listStatName = new ArrayList<>();

    String strSQL = "(SELECT IFNULL( (SELECT ld.stat_header_id  'cnt'    FROM line_data_cnt    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) cnt)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'sl1l2'  FROM line_data_sl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) sl1l2)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'cts'    FROM line_data_cts    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) cts)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ctc'    FROM line_data_ctc    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) ctc)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'nbrcnt' FROM line_data_nbrcnt ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) nbrcnt)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'nbrcts' FROM line_data_nbrcts ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) nbrcts)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'pstd'   FROM line_data_pstd   ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) pstd)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'mcts'   FROM line_data_mcts   ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) mcts)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'rhist'  FROM line_data_rhist  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) rhist)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'vl1l2'  FROM line_data_vl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) vl1l2)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'phist'  FROM line_data_phist  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) phist)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'enscnt'  FROM line_data_enscnt  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) enscnt)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'mpr'  FROM line_data_mpr  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) mpr)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'orank'  FROM line_data_orank  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) orank)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ssvar'  FROM line_data_ssvar  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) ssvar)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'sal1l2'  FROM line_data_sal1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) sal1l2)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'val1l2'  FROM line_data_val1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) val1l2)\n";
    for (String database : currentDBName) {
      try (Connection con = getConnection(database);
           Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           ResultSet res = stmt.executeQuery(strSQL)) {
        int intStatIndex = 0;
        boolean boolCnt = false;
        boolean boolCts = false;
        while (res.next()) {
          int intStatCount = res.getInt(1);
          if (-9999 != intStatCount) {
            switch (intStatIndex) {
              case 0:
              case 1:
              case 15:
                if (!boolCnt) {
                  listStatName.addAll(MVUtil.statsCnt.keySet());
                }
                boolCnt = true;
                break;
              case 2:
              case 3:
                if (!boolCts) {
                  listStatName.addAll(MVUtil.statsCts.keySet());
                }
                boolCts = true;
                break;
              case 4:
                listStatName.addAll(MVUtil.statsNbrcnt.keySet());
                break;
              case 5:
                listStatName.addAll(MVUtil.statsNbrcts.keySet());
                break;
              case 6:
                listStatName.addAll(MVUtil.statsPstd.keySet());
                break;
              case 7:
                listStatName.addAll(MVUtil.statsMcts.keySet());
                break;
              case 8:
                listStatName.addAll(MVUtil.statsRhist.keySet());
                break;
              case 9:
                //case 16:
                listStatName.addAll(MVUtil.statsVl1l2.keySet());
                break;
              case 10:
                listStatName.addAll(MVUtil.statsPhist.keySet());
                break;
              case 11:
                listStatName.addAll(MVUtil.statsEnscnt.keySet());
                break;
              case 12:
                listStatName.addAll(MVUtil.statsMpr.keySet());
                break;
              case 13:
                listStatName.addAll(MVUtil.statsOrank.keySet());
                break;
              case 14:
                listStatName.addAll(MVUtil.statsSsvar.keySet());
                break;
              case 16:
                listStatName.addAll(MVUtil.statsVal1l2.keySet());
                break;
              default:

            }
          }
          intStatIndex++;
        }
        stmt.close();
        res.close();
        con.close();
      } catch (SQLException e) {
        logger.error(e.getMessage());
      }
    }
    Collections.sort(listStatName);
    //Set<String> set = new LinkedHashSet<>(listStatName);
    //return new ArrayList<>(set);
    return listStatName;
  }

  @Override
  public List<String> getListValues(MVNode nodeCall, String strField, String[] currentDBName) {
    List<String> listRes = new ArrayList<>();
    boolean boolMode = nodeCall._children[1]._tag.equals("mode_field");
    boolean boolRhist = nodeCall._children[1]._tag.equals("rhist_field");
    boolean boolPhist = nodeCall._children[1]._tag.equals("phist_field");
    boolean boolROC = nodeCall._children[1]._tag.equals("roc_field");
    boolean boolRely = nodeCall._children[1]._tag.equals("rely_field");
    boolean boolEnsSS = nodeCall._children[1]._tag.equals("ensss_field");
    boolean boolPerf = nodeCall._children[1]._tag.equals("perf_field");
    boolean boolTaylor = nodeCall._children[1]._tag.equals("taylor_field");
    String strHeaderTable = boolMode ? "mode_header" : "stat_header";
    boolean boolNRank = strField.equalsIgnoreCase("N_RANK");
    boolean boolNBin = strField.equalsIgnoreCase("N_BIN");

    //  parse the fcst_var/stat constraint to build a list of line_data tables and fcst_var values
    Map<String, String> tableFcstVarStat = new HashMap<>();
    Map<String, String> tableLineDataTables = new HashMap<>();
    boolean boolFcstVar = false;
    if (boolRhist) {
      tableLineDataTables.put("line_data_rhist", "true");
    } else if (boolPhist) {
      tableLineDataTables.put("line_data_phist", "true");
    } else if (boolROC) {
      tableLineDataTables.put("line_data_pct", "true");
      tableLineDataTables.put("line_data_ctc", "true");
    } else if (boolRely) {
      tableLineDataTables.put("line_data_pct", "true");
    } else if (boolEnsSS) {
      tableLineDataTables.put("line_data_ssvar", "true");
    } else if (boolPerf) {
      tableLineDataTables.put("line_data_cts", "true");
    } else if (boolTaylor) {
      tableLineDataTables.put("line_data_sl1l2", "true");
    } else if (2 < nodeCall._children.length) {
      boolFcstVar = true;
      MVNode nodeFcstVarStat = nodeCall._children[2];
      for (int i = 0; i < nodeFcstVarStat._children.length; i++) {
        MVNode nodeFcstVar = nodeFcstVarStat._children[i];
        tableFcstVarStat.put(nodeFcstVar._name, "true");
        for (int j = 0; j < nodeFcstVar._children.length; j++) {
          String strStat = nodeFcstVar._children[j]._value;
          String strLineDataTable = MVUtil.getStatTable(strStat);
          tableLineDataTables.put(strLineDataTable, "true");
          if (strLineDataTable.equals("line_data_cnt")) {
            tableLineDataTables.put("line_data_sl1l2", "true");
            tableLineDataTables.put("line_data_sal1l2", "true");
          } else if (strLineDataTable.equals("line_data_cts")) {
            tableLineDataTables.put("line_data_ctc", "true");
          }
        }
      }
    }
    String[] listTables = tableLineDataTables.keySet().toArray(new String[]{});
    String strWhere = "";
    if (boolFcstVar) {
      String strFcstVarList = "";
      String[] listFcstVar = tableFcstVarStat.keySet().toArray(new String[]{});
      boolean boolRegEx = false;
      for (int i = 0; i < listFcstVar.length; i++) {
        if (listFcstVar[i].contains("*")) {
          boolRegEx = true;
        }
        if (listFcstVar[i].length() > 0 && !listFcstVar[i].equals("NA")) {
          strFcstVarList += (0 < i ? ", " : "") + "'" + listFcstVar[i].replace("*", "%") + "'";
        }
      }
      if (strFcstVarList.length() > 0) {
        strWhere += "WHERE h.fcst_var " + (boolRegEx ? "LIKE" : "IN") + " (" + strFcstVarList + ")";
      }
    }

    //  parse the list of constraints into a SQL where clause
    String strWhereTime = "";
    for (int i = 2; i < nodeCall._children.length; i++) {
      if (nodeCall._children[i]._tag.equals("stat")) {
        continue;
      }

      //  determine if the field should be used as criteria
      MVNode nodeField = nodeCall._children[i];
      String strFieldCrit = nodeField._name.toLowerCase(Locale.ENGLISH);
      boolean boolTimeCritField = false;
      boolean boolTimeCritCur = false;
      if (strFieldCrit.contains("valid") || strFieldCrit.contains("init") || strFieldCrit.contains("lead")) {
        boolTimeCritField = strField.equals(strFieldCrit) ||
          (strField.contains("fcst_init") && strFieldCrit.equals("init_hour")) ||
          (strField.contains("fcst_valid") && strFieldCrit.equals("valid_hour"));
        boolTimeCritCur = true;
      }
      //  if so, build a where clause for the criteria
      String strFieldDBCrit = formatField(strFieldCrit, boolMode, false);
      if (strFieldDBCrit.contains("n_rank") || strFieldDBCrit.contains("n_bin")) {
        continue;
      }
      String strSQLOp = "IN";
      String strValList = "";
      for (int j = 0; j < nodeField._children.length; j++) {
        String strVal = nodeField._children[j]._value;
        if (strVal.contains("*")) {
          strSQLOp = "LIKE";
        }
        strValList += (0 < j ? ", " : "") + "'" + strVal.replace("*", "%") + "'";
      }

      //  add the where clause to the criteria, if appropriate
      if (boolTimeCritField) {
        if (boolMode) {
          strWhere += (strWhere.equals("") ? " WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
        } else {
          strWhereTime += (strWhereTime.equals("") ? " WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
        }
      } else if (!boolTimeCritCur) {
        strWhere += (strWhere.equals("") ? "WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
      }
    }

    //  build a query for the values
    String strSQL;
    String strTmpTable = null;
    for (String database : currentDBName) {
      try (Connection con = getConnection(database)) {
        if (boolNRank) {
          strSQL = "SELECT DISTINCT ld.n_rank " +
            "FROM stat_header h, line_data_rhist ld " +
            strWhere + (strWhere.equals("") ? "WHERE" : " AND") + " ld.stat_header_id = h.stat_header_id " +
            "ORDER BY n_rank;";
        } else if (boolNBin) {
          strSQL = "SELECT DISTINCT ld.n_bin " +
            "FROM stat_header h, line_data_phist ld " +
            strWhere + (strWhere.equals("") ? "WHERE" : " AND") + " ld.stat_header_id = h.stat_header_id " +
            "ORDER BY ld.n_bin;";
        } else if (!boolMode && (strField.equals("fcst_lead") || strField.contains("valid") || strField.contains("init"))) {
          String strSelectField = formatField(strField, boolMode);
          //  create a temp table for the list values from the different line_data tables
          strTmpTable = "tmp_" + new Date().getTime();
          try (Statement stmtTmp = con.createStatement()) {
            String strTmpSQL = "CREATE TEMPORARY TABLE " + strTmpTable + " (" + strField + " TEXT);";
            stmtTmp.executeUpdate(strTmpSQL);
            //  add all distinct list field values to the temp table from each line_data table
            for (String listTable : listTables) {
              strTmpSQL = "INSERT INTO " + strTmpTable + " SELECT DISTINCT " + strSelectField + " FROM " + listTable + " ld" + strWhereTime;
              stmtTmp.executeUpdate(strTmpSQL);
            }
            stmtTmp.close();
          } catch (SQLException e) {
            logger.error(e.getMessage());
          }

          //  build a query to list all distinct, ordered values of the list field from the temp table
          strSQL = "SELECT DISTINCT " + strField + " FROM " + strTmpTable + " ORDER BY " + strField + ";";
        } else {
          String strFieldDB = formatField(strField, boolMode).replaceAll("h\\.", "");
          strWhere = strWhere.replaceAll("h\\.", "");
          strSQL = "SELECT DISTINCT " + strFieldDB + " FROM " + strHeaderTable + " " + strWhere + " ORDER BY " + strField;
        }
        //  execute the query
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet res = stmt.executeQuery(strSQL)) {

          while (res.next()) {
            listRes.add(res.getString(1));
          }
          //  drop the temp table, if present
          if (strTmpTable != null) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + strTmpTable + ";");
          }
          res.close();
          stmt.close();

        } catch (SQLException e) {
          logger.error(e.getMessage());
        }
        con.close();
      } catch (SQLException e) {
        logger.error(e.getMessage());
      }
    }
    Collections.sort(listRes);
    //Set<String> set = new LinkedHashSet<>(listRes);
    //return new ArrayList<>(set);
    return listRes;
  }


  @Override
  public boolean executeQueriesAndSaveToFile(List<String> queries, String fileName, boolean isCalc, String currentDBName, boolean isNewFile) throws Exception {
    boolean success = false;

    List<String> listSQLBeforeSelect = new ArrayList<>();
    List<String> listSQLLastSelectTemp = new ArrayList<>();
    List<String> listSQLLastSelect = new ArrayList<>();
    for (int i = queries.size() - 1; i >= 0; i--) {
      if (queries.get(i).startsWith("SELECT")) {
        listSQLLastSelectTemp.add(queries.get(i));
      } else {
        break;
      }
    }
    for (String sql : queries) {
      if (listSQLLastSelectTemp.contains(sql)) {
        listSQLLastSelect.add(sql);
      } else {
        listSQLBeforeSelect.add(sql);
      }
    }

    try (Connection con = getConnection(currentDBName)) {
      for (String aListSQLBeforeSelect : listSQLBeforeSelect) {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
          stmt.execute(aListSQLBeforeSelect);
          stmt.close();
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }

      for (int i = 0; i < listSQLLastSelect.size(); i++) {
        boolean append = !isNewFile || i != 0;
        boolean printHeader = !append;
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSetLast = stmt.executeQuery(listSQLLastSelect.get(i));
             FileWriter fstream = new FileWriter(new File(fileName), append);
             BufferedWriter out = new BufferedWriter(fstream)) {

          printFormattedTable(resultSetLast, out, "\t", isCalc, printHeader);
          out.flush();
          resultSetLast.close();
          stmt.close();
          success = true;

        } catch (Exception e) {
          logger.error(e.getMessage());
          if (e.getMessage().contains("Unknown column")) {
            Pattern pattern = Pattern.compile("'(.*?)'");
            Matcher matcher = pattern.matcher(e.getMessage());
            String stat = "This";
            if (matcher.find()) {
              stat = matcher.group(1);
              if (stat.contains(".")) {
                stat = stat.split("\\.")[1];
              }
            }
            logger.error(stat + " statistic can only be plotted as an aggregation of lines");

            //rethrow the exception to be printed as a error popup on UI
            throw new Exception(stat + " statistic can only be plotted as an aggregation of lines");
          }

        }
      }
      con.close();

    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
    return success;
  }

  /**
   * Prints a textual representation of the input {@link ResultSet} with the field names in the first row to the specified {@link BufferedWriter} destination.
   *
   * @param res            The ResultSet to print
   * @param bufferedWriter The stream to write the formatted results to (defaults to printStream)
   * @param delim          The delimiter to insert between field headers and values (defaults to ' ')
   */

  private void printFormattedTable(ResultSet res, BufferedWriter bufferedWriter, String delim, boolean isCalc, boolean isHeader) {

    try {
      ResultSetMetaData met = res.getMetaData();
      //  get the column display widths
      int[] intFieldWidths = new int[met.getColumnCount()];
      for (int i = 1; i <= met.getColumnCount(); i++) {
        intFieldWidths[i - 1] = met.getColumnDisplaySize(i) + 2;
      }

      //  print out the column headers
      if (isHeader) {
        for (int i = 1; i <= met.getColumnCount(); i++) {
          if (delim.equals(" ")) {
            bufferedWriter.write(MVUtil.padEnd(met.getColumnLabel(i), intFieldWidths[i - 1]));
          } else {
            if (1 == i) {
              bufferedWriter.write(met.getColumnLabel(i));
            } else {
              bufferedWriter.write(delim + met.getColumnLabel(i));
            }
          }
        }
        bufferedWriter.write(System.getProperty("line.separator"));
      }

      //  print out the table of values
      int intLine = 0;
      boolean isValValid;
      SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

      while (res.next()) {
        String line = "";
        isValValid = true;
        for (int i = 1; i <= met.getColumnCount(); i++) {
          String strVal;
          String objectType = met.getColumnTypeName(i);


          if (objectType.equals("DATETIME")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Timestamp ts = res.getTimestamp(i, cal);
            strVal = formatDB.format(ts);
          } else {

            strVal = res.getString(i);
            strVal = strVal.equalsIgnoreCase("null") ? "NA" : strVal;
            strVal = strVal.equalsIgnoreCase("-9999") ? "NA" : strVal;
          }
          String columnName = met.getColumnName(i);
          if (columnName.equals("stat_value") && isCalc && strVal.equals("NA")/*job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() */) {
            isValValid = false;
          }

          if (delim.equals(" ")) {
            line = line + (MVUtil.padEnd(strVal, intFieldWidths[i - 1]));
          } else {
            if (1 == i) {
              line = line + (strVal);
            } else {
              line = line + (delim + strVal);
            }
          }
        }
        if (isValValid) {
          bufferedWriter.write(line);
          bufferedWriter.write(System.getProperty("line.separator"));
          intLine++;
        }
      }

      if (0 == intLine) {
        logger.info("No data was returned from database");
        throw new EmptyResultSetException("result set contained no data");
      }

    } catch (Exception e) {
      logger.error("  **  ERROR: Caught " + e.getClass() + " in printFormattedTable(ResultSet res): " + e.getMessage());
    }
  }

  private Map<String, Integer> getPctThreshInfo(String query, String currentDBName) {
    int numPctThresh = 0;
    int pctThresh = -1;
    Map<String, Integer> result = new HashMap<>();
    try (Connection con = getConnection(currentDBName);
         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ResultSet resultSet = stmt.executeQuery(query)
    ) {

      //  validate and save the number of thresholds
      while (resultSet.next()) {
        pctThresh = resultSet.getInt(1);
        numPctThresh++;
      }
      resultSet.close();
      stmt.close();
      con.close();

    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
    result.put("numPctThresh", numPctThresh);
    result.put("pctThresh", pctThresh);

    return result;
  }

  private List<String> getNumbers(String query, String currentDBName) {
    List<String> result = new ArrayList<>();
    try (Connection con = getConnection(currentDBName);
         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ResultSet resultSet = stmt.executeQuery(query)
    ) {

      //   save the number of thresholds
      while (resultSet.next()) {
        result.add(resultSet.getString(1));
      }
      resultSet.close();
      stmt.close();
      con.close();

    } catch (SQLException e) {
      logger.error(e.getMessage());
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
    return Collections.unmodifiableList(listDB);
  }


  /**
   * Use the input query components to build a series of MODE temp table SQL statements and return them in a list.
   *
   * @param strTempList   list of fields for temp tables
   * @param strSelectList list of select fields for temp table population queries
   * @param strWhere      list of where clauses for temp table population
   * @return list of DDL and queries that build and populate MODE temp tables
   */
  private List buildModeTempSQL(String strTempList, String strSelectList, String strWhere, String strStat) {

    //  build the appropriate type of query, depending on the statistic
    String[] listStatComp = MVUtil.parseModeStat(strStat);
    ArrayList listQuery = new ArrayList();

    //  add the object information to the temp table field list
    String strTempListMode = strTempList + ",\n" +
      "    object_id           VARCHAR(128),\n" +
      "    object_cat          VARCHAR(128),\n";


    if (MVUtil.modePairStatField.containsKey(listStatComp[0])) {

      listQuery.add("DROP  TABLE IF EXISTS mode_pair;");
      listQuery.add(
        "CREATE TEMPORARY TABLE mode_pair\n" +
          "(\n" +
          strTempListMode +
          "    centroid_dist       DOUBLE,\n" +
          "    boundary_dist       DOUBLE,\n" +
          "    convex_hull_dist    DOUBLE,\n" +
          "    angle_diff          DOUBLE,\n" +
          "    area_ratio          DOUBLE,\n" +
          "    intersection_area   INT ,\n" +
          "    union_area          INT ,\n" +
          "    symmetric_diff      INTEGER,\n" +
          "    intersection_over_area DOUBLE,\n" +
          "    complexity_ratio    DOUBLE,\n" +
          "    percentile_intensity_ratio DOUBLE,\n" +
          "    interest            DOUBLE,\n" +
          "    simple_flag         BOOLEAN,\n" +
          "    matched_flag        BOOLEAN,\n" +
          "    INDEX (fcst_valid),\n" +
          "    INDEX (object_id),\n" +
          "    INDEX (object_cat)\n" +
          ");");

      listQuery.add(
        "INSERT INTO mode_pair\n" +
          "SELECT\n" + strSelectList + ",\n" +
          "  mop.object_id,\n" +
          "  mop.object_cat,\n" +
          "  mop.centroid_dist,\n" +
          "  mop.boundary_dist,\n" +
          "  mop.convex_hull_dist,\n" +
          "  mop.angle_diff,\n" +
          "  mop.area_ratio,\n" +
          "  mop.intersection_area,\n" +
          "  mop.union_area,\n" +
          "  mop.symmetric_diff,\n" +
          "  mop.intersection_over_area,\n" +
          "  mop.complexity_ratio,\n" +
          "  mop.percentile_intensity_ratio,\n" +
          "  mop.interest,\n" +
          "  IF(mop.object_id REGEXP '^F[[:digit:]]{3}_O[[:digit:]]{3}$', 1, 0) simple_flag,\n" +
          //"  IF(mop.interest >= 0.7, 1, 0) matched_flag\n" +
          "  IF(mop.interest >= 0, 1, 0) matched_flag\n" +
          "FROM\n" +
          "  mode_header h,\n" +
          "  mode_obj_pair mop\n" +
          "WHERE\n" + strWhere +
          "  AND mop.mode_header_id = h.mode_header_id;");
    } else {
      //  build the MODE single object stat tables
      listQuery.add("\nDROP  TABLE IF EXISTS mode_single;");
      listQuery.add(
        "CREATE TEMPORARY TABLE mode_single\n" +
          "(\n" +
          strTempListMode +
          "    centroid_x          DOUBLE,\n" +
          "    centroid_y          DOUBLE,\n" +
          "    centroid_lat        DOUBLE,\n" +
          "    centroid_lon        DOUBLE,\n" +
          "    axis_avg            DOUBLE,\n" +
          "    length              DOUBLE,\n" +
          "    width               DOUBLE,\n" +
          "    area                INT ,\n" +
          "    area_filter         INT ,\n" +
          "    area_thresh         INT ,\n" +
          "    curvature           DOUBLE,\n" +
          "    curvature_x         DOUBLE,\n" +
          "    curvature_y         DOUBLE,\n" +
          "    complexity          DOUBLE,\n" +
          "    intensity_10        DOUBLE,\n" +
          "    intensity_25        DOUBLE,\n" +
          "    intensity_50        DOUBLE,\n" +
          "    intensity_75        DOUBLE,\n" +
          "    intensity_90        DOUBLE,\n" +
          "    intensity_nn        DOUBLE,\n" +
          "    intensity_sum       DOUBLE,\n" +
          "    total               INT ,\n" +
          "    fcst_flag           BOOLEAN,\n" +
          "    simple_flag         BOOLEAN,\n" +
          "    matched_flag        BOOLEAN,\n" +
          "    INDEX (fcst_valid),\n" +
          "    INDEX (object_id),\n" +
          "    INDEX (object_cat)\n" +
          ");");

      //  insert information from mode_obj_single into the temp tables with header data
      listQuery.add(
        INSERT_INTO_MODE_SINGLE + "\n" +
          "SELECT\n" + strSelectList + ",\n" +
          "  mos.object_id,\n" +
          "  mos.object_cat,\n" +
          "  mos.centroid_x,\n" +
          "  mos.centroid_y,\n" +
          "  mos.centroid_lat,\n" +
          "  mos.centroid_lon,\n" +
          "  mos.axis_avg,\n" +
          "  mos.length,\n" +
          "  mos.width,\n" +
          "  mos.area,\n" +
          "  mos.area_filter,\n" +
          "  mos.area_thresh,\n" +
          "  mos.curvature,\n" +
          "  mos.curvature_x,\n" +
          "  mos.curvature_y,\n" +
          "  mos.complexity,\n" +
          "  mos.intensity_10,\n" +
          "  mos.intensity_25,\n" +
          "  mos.intensity_50,\n" +
          "  mos.intensity_75,\n" +
          "  mos.intensity_90,\n" +
          "  mos.intensity_nn,\n" +
          "  mos.intensity_sum,\n" +
          "  mc.total,\n" +
          "  IF(mos.object_id REGEXP '^C?F[[:digit:]]{3}$', 1, 0) fcst_flag,\n" +
          "  IF(mos.object_id REGEXP '^[FO][[:digit:]]{3}$', 1, 0) simple_flag,\n" +
          "  IF(mos.object_cat REGEXP '^C[FO]000$', 0, 1) matched_flag\n" +
          "FROM\n" +
          "  mode_header h,\n" +
          "  mode_obj_single mos,\n" +
          "  mode_cts mc\n" +
          "WHERE\n" + strWhere +
          "  AND mos.mode_header_id = h.mode_header_id\n" +
          "  AND mc.mode_header_id = mos.mode_header_id\n" +
          "  AND mc.field = 'OBJECT';");
    }

    return listQuery;
  }

  private static List<String> buildModeEventEqualizeTempSQL(String strTempList, String strSelectList, String strWhere) {

    List<String> listQuery = new ArrayList<>();


    //  build the MODE single object stat tables
    listQuery.add("\nDROP  TABLE IF EXISTS mode_single;");
    listQuery.add(
      "CREATE TEMPORARY TABLE mode_single\n" +
        "(\n" +
        strTempList + ",\n" +
        "    total               INT \n" +
        ");");

    //  insert information from mode_obj_single into the temp tables with header data
    listQuery.add(
      INSERT_INTO_MODE_SINGLE + "\n" +
        "SELECT  \n" + strSelectList + ",\n" +
        "  mc.total \n" +
        "FROM\n" +
        "  mode_header h,\n" +
        "  mode_cts mc\n" +
        "WHERE\n" + strWhere +
        "  AND mc.mode_header_id = h.mode_header_id\n" +
        "  AND mc.field = 'OBJECT' ;");


    return listQuery;
  }

  /**
   * The input job and plot_fix information is used to build a list of SQL queries that result in the temp table plot_data being filled with formatted plot data
   * for a single plot.  Several job validation checks are performed, and an Exception is thrown in case of error.
   *
   * @param job            contains plot job information
   * @param mapPlotFixPerm permutation of plot_fix values for current plot
   * @param mapPlotFixVal  plot_fix values and sets information
   * @return list of SQL statements that result in plot data
   * @throws Exception
   */
  @Override
  public List<String> buildPlotSQL(MVPlotJob job, MVOrderedMap mapPlotFixPerm, MVOrderedMap mapPlotFixVal, PrintStream printStreamSQL) throws Exception {
    MVOrderedMap _mapFcstVarPat = new MVOrderedMap();

    //  determine if the plot job is for stat data or MODE data
    boolean boolModePlot = MVUtil.isModeJob(job);
    boolean boolModeRatioPlot = MVUtil.isModeRatioJob(job);
    Map<String, String> tableHeaderSQLType;
    if (boolModePlot) {
      tableHeaderSQLType = modeHeaderSQLType;
    } else {
      tableHeaderSQLType = statHeaderSQLType;
    }

    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job, boolModePlot);

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && job.getPlotCond().length() > 0) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + "\n";
    }

    //  determine if the plot requires data aggregation or calculations
    boolean boolAggCtc = job.getAggCtc();
    boolean boolAggSl1l2 = job.getAggSl1l2();
    boolean boolAggSal1l2 = job.getAggSal1l2();
    boolean boolAggPct = job.getAggPct();
    boolean boolAggNbrCnt = job.getAggNbrCnt();
    boolean boolAggSsvar = job.getAggSsvar();
    boolean boolAggVl1l2 = job.getAggVl1l2();
    boolean boolAggVal1l2 = job.getAggVal1l2();
    boolean boolAggStat = boolAggCtc || boolAggSl1l2 || boolAggSal1l2 || boolAggPct || boolAggNbrCnt || boolAggSsvar || boolAggVl1l2 || boolAggVal1l2;
    boolean boolCalcCtc = job.getCalcCtc();
    boolean boolCalcSl1l2 = job.getCalcSl1l2();
    boolean boolCalcSal1l2 = job.getCalcSal1l2();
    boolean boolCalcVl1l2 = job.getCalcVl1l2();
    boolean boolCalcStat;
    boolCalcStat = boolModeRatioPlot || boolCalcCtc || boolCalcSl1l2 || boolCalcSal1l2 || boolCalcVl1l2;
    boolean boolEnsSs = job.getPlotTmpl().equals("ens_ss.R_tmpl");

    //  remove multiple dep group capability
    MVOrderedMap[] listDep = job.getDepGroups();
    MVOrderedMap mapDepGroup;
    if (1 != listDep.length && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
      throw new Exception("unexpected number of <dep> groups: " + listDep.length);
    }
    if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
      mapDepGroup = new MVOrderedMap();
    } else {
      mapDepGroup = listDep[0];
    }


  		/*
       *  Build queries for statistics on both the y1 and y2 axes
  		 */

    List<String> listSQL = new ArrayList<>();
    String strSelectSQL = "";
    for (int intY = 1; intY <= 2; intY++) {

      //  get the dep values for the current dep group
      MVOrderedMap mapDep = (MVOrderedMap) mapDepGroup.get("dep" + intY);
      if (mapDep == null) {
        mapDep = new MVOrderedMap();
      }


      //  establish lists of entires for each group of variables and values
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val()).getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
        throw new Exception("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2

      if (!job.getPlotTmpl().equals("eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("no Y1 series stat found");
      }
      if (2 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        continue;
      }


  			/*
         *  Construct query components from the series variable/value pairs
  			 */

      //  build the select list and where clauses for the series variables and values
      String strSelectPlotList = "";
      String strWhere = strPlotFixWhere;
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(boolModePlot, tableHeaderSQLType, listSeries, strWhere, true);
      String strSelectList = buildMysqlQueryStrings.getSelectList();
      String strTempList = buildMysqlQueryStrings.getTempList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectList.contains("fcst_init")) {
        if (boolModePlot) {
          strSelectList += ",\n  h.fcst_init";
          strTempList += ",\n    fcst_init           " + "DATETIME";
        } else {
          if (strSelectList.length() > 0) {
            strSelectList += ",\n ";
          }
          strSelectList += " ld.fcst_init_beg";

          if (strTempList.length() > 0) {
            strTempList += ",\n";
          }
          strTempList += "   fcst_init_beg       " + "DATETIME";
        }
      }
      if (!strSelectList.contains("fcst_valid")) {
        if (boolModePlot) {
          strSelectList += ",\n  h.fcst_valid";
          strTempList += ",\n    fcst_valid          " + "DATETIME";
        } else {
          if (strSelectList.length() > 0) {
            strSelectList += ",\n ";
          }
          strSelectList += " ld.fcst_valid_beg";
          if (strTempList.length() > 0) {
            strTempList += ",\n  ";
          }
          strTempList += "  fcst_valid_beg      " + "DATETIME";
        }
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(boolModePlot, tableHeaderSQLType, listSeries, strWhere, false);
      strSelectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectPlotList.contains("fcst_init") && !strSelectPlotList.contains("init_hour")) {
        if (boolModePlot) {
          strSelectPlotList += ",\n  h.fcst_init";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!strSelectPlotList.contains("fcst_valid")) {
        if (boolModePlot) {
          strSelectPlotList += ",\n  h.fcst_valid";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_valid_beg";
        }
      }

      if (!boolEnsSs && !strSelectList.contains("fcst_lead")) {
        if (boolModePlot) {

          if (job.getEventEqual()) {
            strSelectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) fcst_lead";
          } else {
            strSelectList += ",\n  h.fcst_lead";
          }
          strSelectPlotList += ",\n  h.fcst_lead";
          strTempList += ",\n    fcst_lead          " + "INT ";
        } else {
          if (job.getEventEqual()) {
            strSelectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , ld.fcst_lead , ld.fcst_lead + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) fcst_lead";
          } else {
            strSelectList += ",\n " + " ld.fcst_lead";
          }
          strSelectPlotList += ",\n  h.fcst_lead";
          strTempList += ",\n    fcst_lead      " + "INT ";
        }
      }


      //  for MODE, build the group by list
      String[] listGroupBy = new String[]{};
      if (boolModePlot && !boolModeRatioPlot) {
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

        listSQL.add("SELECT\n" +
          strSelectPlotList + ",\n  h.fcst_var,\n" +
          "  ld.total,\n  ld.bin_n,\n  ld.var_min,\n  ld.var_max,\n  ld.var_mean,\n" +
          "  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar " +
          "FROM\n" +
          "  stat_header h,\n" +
          "  line_data_ssvar ld\n" +
          "WHERE\n" + strWhere +
          "  AND h.stat_header_id = ld.stat_header_id;\n");

        return listSQL;
      }


  			/*
         *  Construct the query components for the independent variable and values
  			 */

      //  validate and get the type and values for the independent variable
      String strIndyVarType = "";
      String strIndyVar = job.getIndyVar();
      String strIndyVarFormatted = "";
      if (!strIndyVar.isEmpty()) {
        String[] listIndyVal = job.getIndyVal();
        if (!tableHeaderSQLType.containsKey(strIndyVar)) {
          throw new Exception("unrecognized indep " + (boolModePlot ? "mode" : "stat") + "_header field: " + strIndyVar);
        }
        strIndyVarType = tableHeaderSQLType.get(strIndyVar);
        if (1 > listIndyVal.length) {
          throw new Exception("no independent variable values specified");
        }

        //  construct the select list item, where clause and temp table entry for the independent variable
        if (!strSelectList.contains(strIndyVar)) {
          strSelectList += ",\n  " + formatField(strIndyVar, boolModePlot, true);
          strSelectPlotList += ",\n  " + formatField(strIndyVar, boolModePlot, true);
          strTempList += ",\n    " + MVUtil.padEnd(strIndyVar, 20) + strIndyVarType;
        }
        strIndyVarFormatted = formatField(strIndyVar, boolModePlot, false);
        if (strIndyVar.equals("fcst_lead") && job.getEventEqual()) {
          strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , " + strIndyVarFormatted + " , " + strIndyVarFormatted + " + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) ";
        }
        strWhere += (!strWhere.isEmpty() ? "  AND " : "") + strIndyVarFormatted +
          " IN (" + MVUtil.buildValueList(job.getIndyVal()) + ")\n";
      }
      //  add fcst_var to the select list and temp table entries
      strSelectList += ",\n  h.fcst_var";
      strSelectPlotList += ",\n  h.fcst_var";
      strTempList += ",\n    fcst_var            VARCHAR(64)";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          if (!strTempList.contains(strField) && listPlotFixVal[i].getValue() != null) {
            strSelectList += ",\n  " + formatField(strField, boolModePlot, true);
            strSelectPlotList += ",\n  " + formatField(strField, boolModePlot, true);
            strTempList += ",\n    " + strField + "            VARCHAR(64)";
          }
        }
      }

        /*
         *  For agg_stat PCT plots, retrieve the sizes of PCT threshold lists for each series
  			 */
      Map<String, Integer> pctThreshInfo = new HashMap<>();
      if (boolAggPct) {
        MVOrderedMap[] series = MVUtil.permute(job.getSeries1Val().convertFromSeriesMap()).getRows();
        MVOrderedMap[] forecastVars;
        if (job.getPlotTmpl().equals("eclv.R_tmpl") && job.getDepGroups().length == 0) {
          MVOrderedMap m = new MVOrderedMap();
          m.put("NA", "ECLV");
          forecastVars = new MVOrderedMap[]{m};
        } else {
          forecastVars = MVUtil.permute((MVOrderedMap) job.getDepGroups()[0].get("dep" + intY)).getRows();
        }
        for (int forecastVarsInd = 0; forecastVarsInd < forecastVars.length; forecastVarsInd++) {
          MVOrderedMap stats = forecastVars[forecastVarsInd];
          String[] vars = stats.getKeyList();
          for (int varsInd = 0; varsInd < vars.length; varsInd++) {
            for (int seriesInd = 0; seriesInd < series.length; seriesInd++) {
              MVOrderedMap ser = series[seriesInd];
              String[] serName = ser.getKeyList();
              for (int serNameInd = 0; serNameInd < serName.length; serNameInd++) {
                String strSelPctThresh = "SELECT DISTINCT ld.n_thresh\nFROM\n  stat_header h,\n  line_data_pct ld\n";
                strSelPctThresh = strSelPctThresh + "WHERE\n";
                if (strIndyVarFormatted.length() > 0 && job.getIndyVal().length > 0) {
                  strSelPctThresh = strSelPctThresh + strIndyVarFormatted;
                  strSelPctThresh = strSelPctThresh + " IN (" + MVUtil.buildValueList(job.getIndyVal()) + ")\n " + " AND ";
                }
                strSelPctThresh = strSelPctThresh + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]);
                if (!vars[varsInd].equals("NA")) {
                  strSelPctThresh = strSelPctThresh + "' AND fcst_var='" + vars[varsInd] + "' ";
                }
                if (strPlotFixWhere.length() > 0) {
                  strSelPctThresh = strSelPctThresh + "  AND  " + strPlotFixWhere;
                }
                strSelPctThresh = strSelPctThresh + "  AND ld.stat_header_id = h.stat_header_id;";
                printStreamSQL.println(strSelPctThresh + "\n");

                //  run the PCT thresh query
                pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(0));
                if (1 != pctThreshInfo.get("numPctThresh")) {
                  String error = "number of PCT thresholds (" + pctThreshInfo.get("numPctThresh") + ") not distinct for " + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]);
                  if (!vars[varsInd].equals("NA")) {
                    error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                  }
                  throw new Exception(error);
                } else if (1 > pctThreshInfo.get("numPctThresh")) {
                  String error = "invalid number of PCT thresholds (" + pctThreshInfo.get("numPctThresh") + ") found for " + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]);
                  if (!vars[varsInd].equals("NA")) {
                    error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                  }
                  throw new Exception(error);
                }
              }
            }
          }
        }

      } else {
        pctThreshInfo.put("pctThresh", -1);
      }




  			/*
         *  Construct a query for each fcst_var/stat pair
  			 */

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

      //  build a query for each fcst_var/stat pair or a just single query for contingency tables or partial sums
      for (int intFcstVarStat = 0; intFcstVarStat < intNumQueries; intFcstVarStat++) {
        String strFcstVarClause = "";
        String strStat = "";
        if (listFcstVarStat.length > 0) {
          //  get the current fcst_var/stat pair
          String strFcstVar = listFcstVarStat[intFcstVarStat][0];
          strStat = listFcstVarStat[intFcstVarStat][1];

          //  build the fcst_var where clause criteria
          strFcstVarClause = "= '" + strFcstVar + "'";
          Matcher matProb = MVUtil._patProb.matcher(strFcstVar);
          if (matProb.matches() && strFcstVar.contains("*")) {
            Pattern patFcstVar = Pattern.compile(strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
            if (!_mapFcstVarPat.containsKey(patFcstVar)) {
              _mapFcstVarPat.put(patFcstVar, MVUtil.formatR(strFcstVar));
            }
            strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
          }
        } else {
          if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
            strStat = "ECLV";
          }
        }

        //  determine the table containing the current stat
        Map tableStats = null;
        String strStatTable = "";
        String strStatField = strStat.toLowerCase(Locale.US);
        if (boolModePlot) {
          String strStatMode = MVUtil.parseModeStat(strStat)[0];
          if (modeSingleStatField.containsKey(strStatMode)) {
            tableStats = modeSingleStatField;
          } else if (MVUtil.modePairStatField.containsKey(strStatMode)) {
            tableStats = modeSingleStatField;
          } else if (MVUtil.modeRatioField.contains(strStat)) {
            tableStats = modeSingleStatField;
          } else {
            throw new Exception("unrecognized mode stat: " + strStatMode);
          }
        } else {

          String aggType = null;
          if (boolAggStat || boolCalcStat) {
            if (boolCalcCtc || boolAggCtc) {
              aggType = MVUtil.CTC;
            } else if (boolCalcSl1l2 || boolAggSl1l2) {
              aggType = MVUtil.SL1L2;
            } else if (boolCalcSal1l2 || boolAggSal1l2) {
              aggType = MVUtil.SAL1L2;
            } else if (boolAggNbrCnt) {
              aggType = MVUtil.NBR_CNT;
            } else if (boolAggPct) {
              aggType = MVUtil.PCT;
            } else if (boolAggSsvar) {
              aggType = MVUtil.SSVAR;
            } else if (boolCalcVl1l2 || boolAggVl1l2) {
              aggType = MVUtil.VL1L2;
            } else if (boolAggVal1l2) {
              aggType = MVUtil.VAL1L2;
            }
          }


          if (MVUtil.statsCnt.containsKey(strStat)) {
            tableStats = MVUtil.statsCnt;
            if (boolAggStat || boolCalcStat) {
              MVUtil.isAggTypeValid(MVUtil.statsCnt, strStat, aggType);
              strStatTable = "line_data_" + aggType + " ld\n";
            } else {
              strStatTable = "line_data_cnt" + " ld\n";
            }
          } else if (MVUtil.statsSsvar.containsKey(strStat)) {
            tableStats = MVUtil.statsSsvar;
            if (boolAggStat) {
              MVUtil.isAggTypeValid(MVUtil.statsSsvar, strStat, aggType);
              strStatTable = "line_data_" + aggType + " ld\n";
            } else {
              strStatTable = "line_data_ssvar" + " ld\n";
            }
          } else if (MVUtil.statsCts.containsKey(strStat)) {
            tableStats = MVUtil.statsCts;
            if (boolAggStat || boolCalcStat) {
              MVUtil.isAggTypeValid(MVUtil.statsCts, strStat, aggType);
              strStatTable = "line_data_ctc" + " ld\n";
            } else {
              strStatTable = "line_data_cts" + " ld\n";
            }
          } else if (MVUtil.statsNbrcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcnt;
            if (boolAggStat) {
              MVUtil.isAggTypeValid(MVUtil.statsNbrcnt, strStat, aggType);
            }
            strStatTable = "line_data_nbrcnt ld\n";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsEnscnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEnscnt;
            if (boolAggStat) {
              MVUtil.isAggTypeValid(MVUtil.statsEnscnt, strStat, aggType);
            }
            strStatTable = "line_data_enscnt ld\n";
            strStatField = strStat.replace("ENS_", "").toLowerCase();
          } else if (MVUtil.statsNbrcts.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcts;
            MVUtil.isAggTypeValid(MVUtil.statsNbrcts, strStat, aggType);
            strStatTable = "line_data_nbrcts ld\n";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsPstd.containsKey(strStat)) {
            tableStats = MVUtil.statsPstd;
            strStatTable = "line_data_pstd ld\n";
            if (boolAggStat) {
              strStatTable = "line_data_pct ld";
              MVUtil.isAggTypeValid(MVUtil.statsPstd, strStat, aggType);
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strStatTable += ",\n  line_data_pct_thresh ldt" + i;
              }
              strStatTable += "\n";
            }
            strStatField = strStat.replace("PSTD_", "").toLowerCase();
          } else if (MVUtil.statsMcts.containsKey(strStat)) {
            tableStats = MVUtil.statsMcts;
            MVUtil.isAggTypeValid(MVUtil.statsMcts, strStat, aggType);
            strStatTable = "line_data_mcts ld\n";
            strStatField = strStat.replace("MCTS_", "").toLowerCase();
          } else if (MVUtil.statsRhist.containsKey(strStat)) {
            tableStats = MVUtil.statsRhist;
            strStatTable = "line_data_rhist ld\n";
            strStatField = strStat.replace("RHIST_", "").toLowerCase();
          } else if (MVUtil.statsPhist.containsKey(strStat)) {
            tableStats = MVUtil.statsPhist;
            MVUtil.isAggTypeValid(MVUtil.statsPhist, strStat, aggType);
            strStatTable = "line_data_phist ld\n";
            strStatField = strStat.replace("PHIST_", "").toLowerCase();
          } else if (MVUtil.statsVl1l2.containsKey(strStat)) {
            if (boolAggStat || boolCalcStat) {
              MVUtil.isAggTypeValid(MVUtil.statsVl1l2, strStat, aggType);
            }
            tableStats = MVUtil.statsVl1l2;
            strStatTable = "line_data_vl1l2 ld\n";
            strStatField = strStat.replace("VL1L2_", "").toLowerCase();
          } else if (MVUtil.statsVal1l2.containsKey(strStat)) {
            MVUtil.isAggTypeValid(MVUtil.statsVal1l2, strStat, aggType);
            tableStats = MVUtil.statsVal1l2;
            strStatTable = "line_data_val1l2 ld\n";
            strStatField = strStat.replace("VAL1L2_", "").toLowerCase();
          } else if (MVUtil.statsMpr.containsKey(strStat)) {
            tableStats = MVUtil.statsMpr;
            strStatTable = "line_data_mpr ld\n";
          } else if (MVUtil.statsOrank.containsKey(strStat)) {
            tableStats = MVUtil.statsOrank;
            strStatTable = "line_data_orank ld\n";
          } else if (strStat.equals("ECLV") && job.getPlotTmpl().equals("eclv.R_tmpl")) {
            if (boolAggCtc) {
              tableStats = MVUtil.statsCts;
              strStatTable = "line_data_ctc" + " ld\n";
            } else {
              tableStats = MVUtil.statsPstd;
              strStatTable = "line_data_pct ld, line_data_pct_thresh ldt\n";
            }
          } else {
            throw new Exception("unrecognized stat: " + strStat);
          }
        }

        //  build the SQL for the current fcst_var and stat
        if (boolModePlot) {
          //  the single and pair temp tables only need to be built once
          if (1 == intY) {
            listSQL.addAll(buildModeTempSQL(strTempList, strSelectList, strWhere, strStat));
          }
          //  build the mode SQL
          String strWhereFcstVar = "  fcst_var " + strFcstVarClause;
          listSQL.addAll(buildModeStatSQL(strSelectList, strWhereFcstVar, strStat, listGroupBy, job.getEventEqual()));

        } else {
          boolean boolBCRMSE = false;
          String strSelectStat = strSelectList;

          //  build the select list and temp table elements for the stat and CIs
          if (strStat.equals("BCRMSE")) {
            boolBCRMSE = true;
            strStatField = "bcmse";
          }
          strSelectStat += ",\n  '" + strStat + "' stat_name";

          //  add the appropriate stat table members, depending on the use of aggregation and stat calculation
          if (boolAggCtc) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fy_oy,\n  ld.fy_on,\n  ld.fn_oy,\n  ld.fn_on";
          } else if (boolAggSl1l2) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n ld.mae";
          } else if (boolAggSsvar) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n  ld.var_mean, \n  ld.bin_n";
          } else if (boolAggSal1l2) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fabar,\n  ld.oabar,\n  ld.foabar,\n  ld.ffabar,\n  ld.ooabar,\n ld.mae";
          } else if (boolAggPct) {
            if (!job.getPlotTmpl().equals("eclv.R_tmpl")) {
              strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  (ld.n_thresh - 1)";
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strSelectStat += ",\n";
                if (i < pctThreshInfo.get("pctThresh") - 1) {
                  strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + ldt" + (i + 1) + ".thresh_i)/2, 3),\n";
                } else {
                  strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + 1)/2, 3),\n";
                }
                strSelectStat += "  ldt" + i + ".oy_i,\n" +
                  "  ldt" + i + ".on_i";
              }
            } else {
              strSelectStat += ",\n  0 stat_value,\n  ld.n_thresh,\n ldt.thresh_i,\n ldt.oy_i\n, ldt.on_i";
            }
          } else if (boolAggNbrCnt) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbs,\n  ld.fss";
          } else if (boolAggVl1l2) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufbar,\n ld.vfbar,\n ld.uobar,\n ld.vobar,\n ld.uvfobar,\n ld.uvffbar,\n ld.uvoobar";
          } else if (boolAggVal1l2) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufabar,\n ld.vfabar,\n ld.uoabar,\n ld.voabar,\n ld.uvfoabar,\n ld.uvffabar,\n ld.uvooabar";
          } else if (boolCalcCtc) {
            strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.fy_oy, ld.fy_on, ld.fn_oy, ld.fn_on) stat_value,\n" +
              "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (boolCalcSl1l2) {
            if (strStat.equalsIgnoreCase("mae")) {
              strSelectStat += ",\n  calc" + strStat + "( ld.mae) stat_value,\n" +
                "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            } else {
              strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.fbar, ld.obar, ld.fobar, ld.ffbar, ld.oobar) stat_value,\n" +
                "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            }
          } else if (boolCalcSal1l2) {
            strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.fabar, ld.oabar, ld.foabar, ld.ffabar, ld.ooabar) stat_value,\n" +
              "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (boolCalcVl1l2) {
            strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.ufbar, ld.vfbar, ld.uobar, ld.vobar, ld.uvfobar, ld.uvffbar, ld.uvoobar) stat_value,\n" +
              "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else {
            if (boolBCRMSE) {
              strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',CAST(sqrt(ld." + strStatField + ") as DECIMAL(30, 5))) stat_value";

            } else {
              strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',ld." + strStatField + " ) stat_value";

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
              strSelectStat += ",\n  IF(ld." + strStatField + "_ncl=-9999,'NA',ld." + strStatField + "_ncl  ) stat_ncl" +
                ",\n  IF(ld." + strStatField + "_ncu=-9999,'NA',ld." + strStatField + "_ncu  ) stat_ncu";
            } else {
              strSelectStat += ",\n  'NA' stat_ncl,\n  'NA' stat_ncu";
            }

            if (boolHasBoot && !boolAggStat) {
              if (boolBCRMSE) {
                strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',CAST(sqrt(ld." + strStatField + "_bcl) as DECIMAL(30, 5))) stat_bcl" +
                  ",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',CAST(sqrt(ld." + strStatField + "_bcu) as DECIMAL(30, 5))) stat_bcu";
              } else {
                strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',ld." + strStatField + "_bcl) stat_bcl" +
                  ",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',ld." + strStatField + "_bcu ) stat_bcu";
              }
            } else {
              strSelectStat += ",\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            }
          }

          String strStatNaClause = "";
          if (!boolAggStat && !boolCalcStat) {
            strStatNaClause = "\n  AND ld." + strStatField + " != -9999";
          }
          if (boolAggPct) {
            if (!job.getPlotTmpl().equals("eclv.R_tmpl")) {
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strStatNaClause += "\n  AND ld.line_data_id = ldt" + i + ".line_data_id\n" +
                  "  AND ldt" + i + ".i_value = " + i;
              }
            } else {
              strStatNaClause = "\n  AND ld.line_data_id = ldt.line_data_id\n";
            }
          }

          //  build the query
          strSelectSQL += (strSelectSQL.isEmpty() ? "" : "\nUNION\n") +
            "SELECT\n" + strSelectStat + "\n" +
            "FROM\n  stat_header h,\n  " + strStatTable;
          strSelectSQL += "WHERE\n" + strWhere;
          if (strFcstVarClause.length() > 0) {
            strSelectSQL += "  AND h.fcst_var " + strFcstVarClause + "\n";
          }
          strSelectSQL += "  AND ld.stat_header_id = h.stat_header_id" + strStatNaClause;
        }

      }

    }

    //  add the stat plot query to the list
    if (!boolModePlot) {
      listSQL.add(strSelectSQL + ";");
    }

    //remove duplicated queries
    listSQL = new ArrayList<>(new LinkedHashSet<>(listSQL));

    //add statements for creating mode_single2 table if needed
    boolean hasSecondTable = false;
    Integer insertInd = null;
    for (int i = 0; i < listSQL.size(); i++) {
      if (listSQL.get(i).contains("mode_single2")) {
        hasSecondTable = true;
      }
      if (listSQL.get(i).contains(INSERT_INTO_MODE_SINGLE)) {
        insertInd = i;
      }
    }
    if (hasSecondTable && insertInd != null) {
      listSQL.add(insertInd + 1, "DROP  TABLE IF EXISTS mode_single2;");
      listSQL.add(insertInd + 2, "CREATE TEMPORARY TABLE mode_single2 SELECT * FROM mode_single;");
    }


    return listSQL;
  }

  /**
   * Build where clauses for each of the input aggregation field/value entries and return the clauses as a String
   *
   * @param listPlotFixFields list of &lt;plot_fix&gt; field/value pairs
   * @param boolModePlot      specifies MODE plot
   * @return generated SQL where clauses
   */

  private String buildPlotFixWhere(Map.Entry[] listPlotFixFields, MVPlotJob job, boolean boolModePlot) {
    String strWhere = "";

    //  build the aggregate fields where clause
    for (int i = 0; i < listPlotFixFields.length; i++) {
      String strField = (String) listPlotFixFields[i].getKey();
      String strCondition = "";
      Object objValue = listPlotFixFields[i].getValue();
      if (objValue instanceof String[]) {
        strCondition = "IN (" + MVUtil.buildValueList((String[]) objValue) + ")";
      } else if (objValue instanceof MVOrderedMap) {
        MVOrderedMap mapTmpl = job.getTmplVal();
        String strSetName = mapTmpl.get(strField + "_set").toString();
        String[] listValues = (String[]) ((MVOrderedMap) objValue).get(strSetName);
        strCondition = "IN (" + MVUtil.buildValueList(listValues) + ")";

      } else if (objValue instanceof String) {
        if (objValue.toString().startsWith("BETWEEN")) {
          strCondition = objValue.toString();
        } else {
          strCondition = "IN ('" + objValue.toString() + "')";
        }
      }
      String strIndyVarFormatted = formatField(strField, boolModePlot, false);
      if (strField.equals("fcst_lead") && job.getEventEqual()) {
        strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , " + strIndyVarFormatted + " , " + strIndyVarFormatted + " + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) ";
      }
      strWhere += (0 < i ? "  AND " : "  ") + strIndyVarFormatted + " " + strCondition + "\n";
    }

    return strWhere;
  }

  /**
   * Use the input query components to build a list of select statements to gather plot data. This function is a switchboard for the different types of MODE
   * statistics: single, pair, derived, difference and ratios.
   *
   * @param strSelectList list of select fields
   * @param strWhere      list of where clauses
   * @param strStat       MODE stat
   * @param listGroupBy   list of fields to group by
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildModeStatSQL(String strSelectList, String strWhere, String strStat, String[] listGroupBy, boolean isEventEqualization) {

    List<String> listQuery = new ArrayList<>();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatComp = MVUtil.parseModeStat(strStat);
    if (listStatComp[0].equals("ACOV")) {
      listQuery.add(buildModeSingleAcovTable(strSelectList, strWhere, strStat, listGroupBy, isEventEqualization));
    } else if (modeSingleStatField.containsKey(listStatComp[0])) {
      if (!listStatComp[1].startsWith("D")) {
        listQuery.add(buildModeSingleStatTable(strSelectList, strWhere, strStat, listGroupBy, isEventEqualization));
      } else {
        //we will add statements for creating mode_single2 table later
        listQuery.add(buildModeSingleStatDiffTable(strSelectList, strWhere, strStat));
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
    } else if (listStatComp[0].equals("RATIO") || listStatComp[0].equals("AREARAT") || strStat.startsWith("OBJ")) {
      listQuery.add(buildModeSingleStatRatioTable(strSelectList, strWhere));
    }
    return listQuery;
  }

  private String buildModeSingleAcovTable(String selectList, String strWhere, String stat, String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];
    String strGroupBy = "\nGROUP BY\n";
    for (int i = 0; i < groups.length; i++) {
      strGroupBy += (0 < i ? ",\n" : "") + "  " + groups[i];
    }

    //  build the query components
    String strSelectListStat = selectList.replaceAll("h\\.", "").replaceAll(",\\s+$", "");
    String strStat = "SUM(area) / (2*total)";
    if (strStatFlag.charAt(0) != 'A') {
      strStat = "SUM(area) / total";
      strGroupBy += " , fcst_flag";
      strWhere += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
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


    //  build the query
    return
      "SELECT\n" + strSelectListStat + ",\n" +
        "  '' object_id,\n" +
        "  '' object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strStat + " stat_value\n" +
        "FROM mode_single\n" +
        "WHERE\n" +
        strWhere + "\n" +
        "  AND simple_flag = 1\n" +
        strGroupBy + ";";
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
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "\n  AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "");
    String strGroupListMMI = strSelectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");
    strGroupListMMI = strGroupListMMI.replaceAll("if\\D+fcst_lead", "fcst_lead");
    //  set the object_id field, depending on the stat
    String strObjectId = "object_id";
    String strObjectIdName = "object_id";
    String strGroupBy = "";
    if (strStatName.startsWith("MAXINT")) {
      if (strStatName.equals("MAXINTF")) {
        strObjectId = "SUBSTR(object_id, 1, LOCATE('_', object_id)-1) fcst_id";
        strObjectIdName = "fcst_id";
      } else if (strStatName.equals("MAXINTO")) {
        strObjectId = "SUBSTR(object_id, LOCATE('_', object_id)+1) obs_id";
        strObjectIdName = "obs_id";
      }
      strGroupBy = "\nGROUP BY\n" + strGroupListMMI + ",\n  " + strObjectIdName;
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.modePairStatField.get(strStatName);

    //  build the query
    return
      // "INSERT INTO plot_data\n" +
      "SELECT\n" + strSelectListStat + ",\n" +
        "  " + strObjectId + ",\n" +
        "  object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strTableStat + " stat_value\n" +
        "FROM mode_pair\n" +
        "WHERE\n" +
        strWhere + strGroupBy + ";";
  }

  private String buildModeSingleStatTable(String selectList, String strWhere, String stat, String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "\n  AND matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    //  build the group by clause
    String strGroupBy = "";
    if (strStatName.startsWith("CNT")) {
      strGroupBy = "\nGROUP BY\n";
      for (int i = 0; i < groups.length; i++) {
        strGroupBy += (0 < i ? ",\n" : "") + "  " + groups[i];
      }
      if (!strStatName.equals("CNTSUM") && !strGroupBy.contains("fcst_valid")) {
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

    //  build the query
    return
      "SELECT\n" + strSelectListStat + ",\n" +
        "  object_id,\n" +
        "  object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + modeSingleStatField.get(strStatName) + " stat_value\n" +
        "FROM mode_single\n" +
        "WHERE\n" + strWhere + strGroupBy + ";";
  }

  private String buildModeSingleStatRatioTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
      "SELECT\n" + strSelectListStat + ",\n" +
        "  object_id,\n" +
        "  object_cat,\n" +
        "  area,\n" +
        "  total,\n" +
        "  fcst_flag,\n" +
        "  simple_flag,\n" +
        "  matched_flag\n" +
        "FROM mode_single\n" +
        "WHERE\n" + strWhere + ";";
  }

  private String buildModeSingleStatDiffTable(String strSelectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "\n  AND s.fcst_var = s2.fcst_var";

    //  build the where clause using the input select fields
    Matcher mat = MVUtil._patModeSingle.matcher(strSelectList);
    while (mat.find()) {
      if (!mat.group(1).contains("NULL") && !mat.group(1).contains("FROM") && !mat.group(1).contains("WHERE")) {
        strWhere += "\n  AND s." + mat.group(1) + " = s2." + mat.group(1);
      }
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND s.simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0") +
        "\n  AND s2.simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "\n  AND s.matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0") +
        "\n  AND s2.matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = modeSingleStatField.get(strStatName);
    String statName = strTableStat.split("\\(")[0];
    String[] strTableStats = new String[2];
    if (strTableStat.contains("object_id")) {
      strTableStats[0] = statName + "( s.object_id)";
      strTableStats[1] = statName + "( s2.object_id)";
    } else {
      strTableStats[0] = "s." + strTableStat;
      strTableStats[1] = "s2." + strTableStat;

    }

    //  build the query COUNT(object_id)
    String result =
      "SELECT\n" + strSelectListStat + ",\n" +
        "  s.object_id,\n" +
        "  s.object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n" +
        "FROM mode_single s, mode_single2 s2\n" +
        "WHERE\n" +
        strWhere + "\n" +
        "  AND s.fcst_flag = 1\n" +
        "  AND s2.fcst_flag = 0\n" +
        "  AND RIGHT(s.object_id, 3) = RIGHT(s2.object_id, 3)\n";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0] + " != -9999 AND " + strTableStats[1] + " != -9999;";
    }
    return result;
  }

  @Override
  public List<String> buildPlotModeEventEqualizeSQL(MVPlotJob job, MVOrderedMap mapPlotFixPerm, MVOrderedMap mapPlotFixVal) throws Exception {
    MVOrderedMap _mapFcstVarPat = new MVOrderedMap();

    //  determine if the plot job is for stat data or MODE data
    boolean boolModePlot = MVUtil.isModeJob(job);
    Map<String, String> tableHeaderSQLType;
    if (boolModePlot) {
      tableHeaderSQLType = modeHeaderSQLType;
    } else {
      tableHeaderSQLType = statHeaderSQLType;
    }


    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job, boolModePlot);

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && job.getPlotCond().length() > 0) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + "\n";
    }


    //  remove multiple dep group capability
    MVOrderedMap[] listDep = job.getDepGroups();
    if (1 != listDep.length) {
      throw new Exception("unexpected number of <dep> groups: " + listDep.length);
    }
    MVOrderedMap mapDepGroup = listDep[0];


		/*
     *  Build queries for statistics on both the y1 and y2 axes
		 */

    List<String> listSQL = new ArrayList<>();
    for (int intY = 1; intY <= 2; intY++) {

      //  get the dep values for the current dep group
      MVOrderedMap mapDep = (MVOrderedMap) mapDepGroup.get("dep" + intY);

      //  establish lists of entires for each group of variables and values
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val()).getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length) {
        throw new Exception("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2
      if (!job.getPlotTmpl().equals("eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("no Y1 series stat found");
      }
      if (2 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        continue;
      }


			/*
       *  Construct query components from the series variable/value pairs
			 */

      //  build the select list and where clauses for the series variables and values
      String strSelectPlotList;
      String strWhere = strPlotFixWhere;
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(boolModePlot, tableHeaderSQLType, listSeries, strWhere, true);
      String strSelectList = buildMysqlQueryStrings.getSelectList();
      String strTempList = buildMysqlQueryStrings.getTempList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectList.contains("fcst_init")) {
        strSelectList += ",\n  h.fcst_init";
        strTempList += ",\n    fcst_init           " + "DATETIME";
      }
      if (!strSelectList.contains("fcst_valid")) {
        strSelectList += ",\n  h.fcst_valid";
        strTempList += ",\n    fcst_valid          " + "DATETIME";
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(boolModePlot, tableHeaderSQLType, listSeries, strWhere, false);
      strSelectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectPlotList.contains("fcst_init") && !strSelectPlotList.contains("init_hour")) {
        if (boolModePlot) {
          strSelectPlotList += ",\n  h.fcst_init";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!strSelectPlotList.contains("fcst_valid")) {
        if (boolModePlot) {
          strSelectPlotList += ",\n  h.fcst_valid";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_valid_beg";
        }
      }

      if (!strSelectList.contains("fcst_lead")) {
        if (job.getEventEqual()) {
          strSelectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) fcst_lead";
        } else {
          strSelectList += ",\n " + " h.fcst_lead";
        }
        strSelectPlotList += ",\n " + " h.fcst_lead";
        strTempList += ",\n    fcst_lead          " + "INT ";
      }

			/*
       *  Construct the query components for the independent variable and values
			 */

      //  validate and get the type and values for the independent variable
      String strIndyVarType;
      String strIndyVar = job.getIndyVar();
      String[] listIndyVal = job.getIndyVal();
      if (!tableHeaderSQLType.containsKey(strIndyVar)) {
        throw new Exception("unrecognized indep " + (boolModePlot ? "mode" : "stat") + "_header field: " + strIndyVar);
      }
      strIndyVarType = tableHeaderSQLType.get(strIndyVar);
      if (1 > listIndyVal.length) {
        throw new Exception("no independent variable values specified");
      }

      //  construct the select list item, where clause and temp table entry for the independent variable
      if (!strSelectList.contains(strIndyVar)) {
        strSelectList += ",\n  " + formatField(strIndyVar, boolModePlot, true);
        strSelectPlotList += ",\n  " + formatField(strIndyVar, boolModePlot, true);
        strTempList += ",\n    " + MVUtil.padEnd(strIndyVar, 20) + strIndyVarType;
      }
      String strIndyVarFormatted = formatField(strIndyVar, boolModePlot, false);
      if (strIndyVar.equals("fcst_lead") && job.getEventEqual()) {
        strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) is NULL , " + strIndyVarFormatted + " , " + strIndyVarFormatted + " + (select fcst_lead_offset FROM model_fcst_lead_offset WHERE model = h.model) ) ";
      }
      strWhere += (!strWhere.isEmpty() ? "  AND " : "") + strIndyVarFormatted +
        " IN (" + MVUtil.buildValueList(job.getIndyVal()) + ")\n";

      //  add fcst_var to the select list and temp table entries
      strSelectList += ",\n  h.fcst_var";
      strSelectPlotList += ",\n  h.fcst_var";
      strTempList += ",\n    fcst_var            VARCHAR(64)";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          if (!strTempList.contains(strField) && listPlotFixVal[i].getValue() != null) {
            strSelectList += ",\n  " + formatField(strField, boolModePlot, true);
            strSelectPlotList += ",\n  " + formatField(strField, boolModePlot, true);
            strTempList += ",\n    " + strField + "            VARCHAR(64)";
          }
        }
      }


			/*
       *  Build the temp tables to hold plot data
			 */


      //  the single and pair temp tables only need to be built once
      if (1 == intY) {
        listSQL.addAll(buildModeEventEqualizeTempSQL(strTempList, strSelectList, strWhere));
      }


			/*
       *  Construct a query for each fcst_var/stat pair
			 */

      //  determine how many queries are needed to gather that stat information
      int intNumQueries;
      String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(mapDep);

      intNumQueries = listFcstVarStat.length;

      //  build a query for each fcst_var/stat pair or a just single query for contingency tables or partial sums
      for (int intFcstVarStat = 0; intFcstVarStat < intNumQueries; intFcstVarStat++) {

        //  get the current fcst_var/stat pair
        String strFcstVar = listFcstVarStat[intFcstVarStat][0];

        //  build the fcst_var where clause criteria
        String strFcstVarClause = "= '" + strFcstVar + "'";
        Matcher matProb = MVUtil._patProb.matcher(strFcstVar);
        if (matProb.matches() && strFcstVar.contains("*")) {
          Pattern patFcstVar = Pattern.compile(strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
          if (!_mapFcstVarPat.containsKey(patFcstVar)) {
            _mapFcstVarPat.put(patFcstVar, MVUtil.formatR(strFcstVar));
          }
          strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
        }

        //  build the mode SQL
        String strWhereFcstVar = "  fcst_var " + strFcstVarClause;
        listSQL.addAll(buildModeStatEventEqualizeSQL(strSelectPlotList, strWhereFcstVar));

      }

    }


    //remove duplicated queries
    listSQL = new ArrayList<>(new LinkedHashSet<>(listSQL));

    return listSQL;
  }


  private List buildModeStatEventEqualizeSQL(String strSelectList, String strWhere) {

    List listQuery = new ArrayList();

    listQuery.add(buildModeSingleStatRatioEventEqualizeTable(strSelectList, strWhere));

    return listQuery;
  }


  private String buildModeSingleStatRatioEventEqualizeTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
      "SELECT\n" + strSelectListStat + ",\n" +
        "  total\n" +
        "FROM mode_single\n" +
        "WHERE\n" + strWhere + ";";
  }

  @Override
  public String buildAndExecuteQueriesForHistJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm, PrintStream printStream, PrintStream printStreamSql) throws Exception {
    String strTempList = "";
    String strSelectList = "";
    String strWhereSeries = "";
    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();


    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSQLType.containsKey(strSeriesField)) {
        throw new Exception("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",") + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false) +
        " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n") + "    " + MVUtil.padEnd(strSeriesField, 20) + statHeaderSQLType.get(strSeriesField);

    }

    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());


    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);
    String strNumSelect = "";
    String type = "";
    String table = "";
    String tableBins = "";
    if (job.getPlotTmpl().startsWith("relp")) {
      type = "ens";
      table = "line_data_relp";
      tableBins = "line_data_relp_ens";
    } else if (job.getPlotTmpl().startsWith("rhist")) {
      type = "rank";
      table = "line_data_rhist";
      tableBins = "line_data_rhist_rank";
    } else if (job.getPlotTmpl().startsWith("phist")) {
      type = "bin";
      table = "line_data_phist";
      strWhere = strWhere.replaceAll("h\\.n_bin", "ld.n_bin");
      tableBins = "line_data_phist_bin";
    }
    strWhere = strWhere.replaceAll("h\\.n_" + type, "ld.n_" + type);
    strNumSelect =
      "SELECT DISTINCT\n" +
        "  ld.n_" + type + "\n" +
        "FROM\n" +
        "  stat_header h,\n" +
        "  " + table + " ld\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id;";

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + "\n");
    }


    //  run the rank number query and warn, if necessary
    String strMsg = "";
    List<String> listNum = getNumbers(strNumSelect, job.getCurrentDBName().get(0));


    if (listNum.isEmpty()) {
      throw new Exception("no " + type + "  data found");
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
      "SELECT\n" +
        "  ldr.i_value,\n";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
    }

    strPlotDataSelect = strPlotDataSelect + "  SUM(ldr." + type + "_i) stat_value\n" +
      "FROM\n" +
      "  stat_header h,\n" +
      "  " + table + " ld,\n" +
      "  " + tableBins + " ldr\n" +
      "WHERE\n" +
      strWhere +
      "  AND h.stat_header_id = ld.stat_header_id\n" +
      "  AND ld.line_data_id = ldr.line_data_id\n" +
      "GROUP BY i_value";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
    }
    strPlotDataSelect = strPlotDataSelect + ";";
    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + "\n");
    }

    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      executeQueriesAndSaveToFile(queries, strDataFile, job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(), job.getCurrentDBName().get(i), i == 0);
    }
    return strMsg;
  }


  @Override
  public int buildAndExecuteQueriesForRocRelyJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm, PrintStream printStream, PrintStream printStreamSql) throws Exception {
    String strSelectList = "";
    String strTempList = "";
    String strWhereSeries = "";

    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();

    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSQLType.containsKey(strSeriesField)) {
        throw new Exception("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",") + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false) +
        " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n") + "    " + MVUtil.padEnd(strSeriesField, 20) + statHeaderSQLType.get(strSeriesField);

    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());

    boolean boolRelyPlot = job.getPlotTmpl().startsWith("rely");

    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);
    strWhere = strWhere + strWhereSeries;


    //  check to ensure only a single obs_thresh is used
    String strObsThreshSelect =
      "SELECT\n" +
        "  DISTINCT(h.obs_thresh)\n" +
        "FROM\n" +
        "  stat_header h,\n" +
        "  " + (boolRelyPlot || job.getRocPct() ? "line_data_pct" : "line_data_ctc") + " ld\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id\n" +
        "ORDER BY h.obs_thresh;";

    if (printStreamSql != null) {
      printStreamSql.println(strObsThreshSelect + "\n");
    }

    List<String> listObsThresh = getNumbers(strObsThreshSelect, job.getCurrentDBName().get(0));

    //  build the query depending on the type of data requested
    String strPlotDataSelect = "";
    List<String> listFcstThresh = new ArrayList<>();
    if (boolRelyPlot || job.getRocPct()) {

      //  check to ensure only a single fcst_thresh is used
      String strFcstThreshSelect =
        "SELECT\n";

      strFcstThreshSelect = strFcstThreshSelect + "  DISTINCT(h.fcst_thresh) thresh\n";

      strFcstThreshSelect = strFcstThreshSelect + "FROM\n" +
        "  stat_header h,\n" +
        "  line_data_pct ld\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id\n" +
        "ORDER BY h.fcst_thresh;";


      if (printStreamSql != null) {
        printStreamSql.println(strFcstThreshSelect + "\n");
      }

      listFcstThresh = getNumbers(strFcstThreshSelect, job.getCurrentDBName().get(0));


      //  build the plot data sql
      strPlotDataSelect =
        "SELECT\n" +
          "  ld.total,\n";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
      }
      strPlotDataSelect = strPlotDataSelect +

        "  ldt.i_value,\n" +
        "  ldt.thresh_i,\n" +
        "  SUM(ldt.oy_i) oy_i,\n" +
        "  SUM(ldt.on_i) on_i\n";

      strPlotDataSelect = strPlotDataSelect + "FROM\n" +
        "  stat_header h,\n" +
        "  line_data_pct ld,\n" +
        "  line_data_pct_thresh ldt\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id\n" +
        "  AND ld.line_data_id = ldt.line_data_id\n" +
        "GROUP BY\n" +
        "  ldt.thresh_i";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
      }
      strPlotDataSelect = strPlotDataSelect + ";";

    } else if (job.getRocCtc()) {

      strPlotDataSelect =
        "SELECT\n" +
          "  h.fcst_thresh thresh,\n";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
      }
      strPlotDataSelect = strPlotDataSelect + "  ld.total,\n" +
        "  SUM(ld.fy_oy) fy_oy,\n" +
        "  SUM(ld.fy_on) fy_on,\n" +
        "  SUM(ld.fn_oy) fn_oy,\n" +
        "  SUM(ld.fn_on) fn_on\n" +
        "FROM\n" +
        "  stat_header h,\n" +
        "  line_data_ctc ld\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id\n" +
        "GROUP BY\n" +
        "  h.fcst_thresh";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + ", " + strSelectList;
      }
      strPlotDataSelect = strPlotDataSelect + ";";

    }

    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + "\n");
    }


    //  if the query does not return data from a expected obs_thresh, throw an error
    int intNumDepSeries = 1;
    Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
    for (Map.Entry aListSeries1Val : listSeries1Val) {
      String[] listVal = (String[]) aListSeries1Val.getValue();
      intNumDepSeries *= listVal.length;
    }
    if (intNumDepSeries < listObsThresh.size()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data from only a single obs_thresh, " +
        "instead found " + listObsThresh.size();
      for (int i = 0; i < listObsThresh.size(); i++) {
        strObsThreshMsg += (0 == i ? ": " : ", ") + listObsThresh.toString();
      }
      throw new Exception(strObsThreshMsg);
    }

    if (listObsThresh.isEmpty()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data from at least one obs_thresh ";
      throw new Exception(strObsThreshMsg);
    }

    //  if the query for a PCT plot does not return data from a single fcst_thresh, throw an error
    if (job.getRocPct() && intNumDepSeries < listFcstThresh.size()) {
      String strFcstThreshMsg = "ROC/Reliability plots using PCTs must contain data from only a single fcst_thresh, " +
        "instead found " + listFcstThresh.size();
      for (int i = 0; i < listFcstThresh.size(); i++) {
        strFcstThreshMsg += (0 == i ? ":" : "") + "\n  " + listFcstThresh.toString();
      }
      throw new Exception(strFcstThreshMsg);
    }
    if (job.getRocPct() && listObsThresh.isEmpty()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data from at least one obs_thresh ";
      throw new Exception(strObsThreshMsg);
    }

    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      executeQueriesAndSaveToFile(queries, strDataFile, job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(), job.getCurrentDBName().get(i), i == 0);
    }

    return intNumDepSeries;
  }

  @Override
  public int buildAndExecuteQueriesForEclvJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm, PrintStream printStream, PrintStream printStreamSql) throws Exception {
    String strSelectList = "";
    String strTempList = "";
    String strWhereSeries = "";

    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();

    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSQLType.containsKey(strSeriesField)) {
        throw new Exception("unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",") + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false) +
        " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n") + "    " + MVUtil.padEnd(strSeriesField, 20) + statHeaderSQLType.get(strSeriesField);

    }
    if (!strSelectList.contains("fcst_valid")) {
      strSelectList += ",\n " + " ld.fcst_valid_beg";
    }
    if (!strSelectList.contains("fcst_lead")) {
      strSelectList += ",\n " + " ld.fcst_lead";
    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());


    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);

    strWhere = strWhere.replaceAll("h\\.n_pnt", "ld.n_pnt");
    String strNumSelect =
      "SELECT DISTINCT\n" +
        "  ld.n_pnt\n" +
        "FROM\n" +
        "  stat_header h,\n" +
        "  line_data_eclv ld\n" +
        "WHERE\n" +
        strWhere +
        "  AND h.stat_header_id = ld.stat_header_id;";

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + "\n");
    }
    //  run the rank number query and warn, if necessary
    String strMsg = "";
    List<String> listNum = getNumbers(strNumSelect, job.getCurrentDBName().get(0));


    if (listNum.isEmpty()) {
      throw new Exception("no pnt  data found");
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
        String strSelPctThresh = "SELECT DISTINCT ld.n_pnt\nFROM\n  stat_header h,\n  line_data_eclv ld\n" +
          "WHERE\n"
          + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]) + "' AND " + strWhere
          + "  AND ld.stat_header_id = h.stat_header_id;";
        printStreamSql.println(strSelPctThresh + "\n");

        //  run the PCT thresh query
        pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(0));
        if (1 != pctThreshInfo.get("numPctThresh")) {
          throw new Exception("number of ECLV pnts (" + pctThreshInfo.get("numPctThresh") + ") not distinct for " + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]));
        } else if (1 > pctThreshInfo.get("numPctThresh")) {
          throw new Exception("invalid number of ECLV pnts (" + pctThreshInfo.get("numPctThresh") + ") found for" + serName[serNameInd] + " = '" + ser.getStr(serName[serNameInd]) +
            "'");
        }
      }
    }
    strWhere = strWhere + strWhereSeries;

    //  build the query depending on the type of data requested
    String strPlotDataSelect = "";

    //  build the plot data sql
    strPlotDataSelect =
      "SELECT\n";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
    }
    if (listPlotFixVal.length > 0) {
      for (Map.Entry aListPlotFixVal : listPlotFixVal) {
        String strField = (String) aListPlotFixVal.getKey();
        strPlotDataSelect = strPlotDataSelect + strField + ",\n";
      }
    }

    strPlotDataSelect = strPlotDataSelect +

      "  ldt.x_pnt_i,\n" +
      "  ldt.y_pnt_i \n";

    strPlotDataSelect = strPlotDataSelect + "FROM\n" +
      "  stat_header h,\n" +
      "  line_data_eclv ld,\n" +
      "  line_data_eclv_pnt ldt\n" +
      "WHERE\n" +
      strWhere +
      "  AND h.stat_header_id = ld.stat_header_id\n" +
      "  AND ld.line_data_id = ldt.line_data_id";

    strPlotDataSelect = strPlotDataSelect + ";";


    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + "\n");
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
      executeQueriesAndSaveToFile(queries, strDataFile, job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(), job.getCurrentDBName().get(i), i == 0);
    }

    return intNumDepSeries;
  }

  @Override
  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  /**
   * Format the input field according to the table that it is stored in, and whether or not it needs to be derived or formatted as a date.  Stat fields must be
   * differentiated from MODE fields.  Also, fields intended for the select list, as opposed to a where clause, must be specified.
   *
   * @param field  MET output header field to format
   * @param mode   specifies whether or not the field is a stat_header or mode_header field
   * @param fmtSel specifies whether the formatted field will be used for the select list (true) or a where clause (false)
   * @return the formatted field
   */
  private String formatField(String field, boolean mode, boolean fmtSel) {
    if (field.equals("init_hour")) {
      return (mode ? "HOUR(h.fcst_init)" : "HOUR(ld.fcst_init_beg)") + (fmtSel ? " init_hour" : "");
    } else if (field.equals("valid_hour")) {
      return (mode ? "HOUR(h.fcst_valid)" : "HOUR(ld.fcst_valid_beg)") + (fmtSel ? " valid_hour" : "");
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

  private BuildMysqlQueryStrings build(boolean boolModePlot, Map<String, String> tableHeaderSQLType, Map.Entry[] listSeries, String strWhere, boolean isFormatSelect) throws Exception {
    BuildMysqlQueryStrings buildMysqlQueryStrings = new BuildMysqlQueryStrings(boolModePlot, tableHeaderSQLType, listSeries, strWhere, isFormatSelect);
    for (Map.Entry entry : listSeries) {

      //  get the current series field and values
      String field = entry.getKey().toString();
      String[] value = (String[]) entry.getValue();

      //  validate the series field and get its type
      String strTempType;
      if (!tableHeaderSQLType.containsKey(field)) {
        throw new Exception("unrecognized " + (boolModePlot ? "mode" : "stat") + "_header field: " + field);
      }
      strTempType = tableHeaderSQLType.get(field);

      //  build the select list element, where clause and temp table list element
      if (buildMysqlQueryStrings.getSelectList().length() == 0) {
        if (isFormatSelect) {
          buildMysqlQueryStrings.setSelectList(buildMysqlQueryStrings.getSelectList() + "  " + formatField(field, boolModePlot, true));
        } else {
          buildMysqlQueryStrings.setSelectList(buildMysqlQueryStrings.getSelectList() + "  " + field);

        }
      } else {
        if (isFormatSelect) {
          buildMysqlQueryStrings.setSelectList(buildMysqlQueryStrings.getSelectList() + ",\n" + "  " + formatField(field, boolModePlot, true));

        } else {
          buildMysqlQueryStrings.setSelectList(buildMysqlQueryStrings.getSelectList() + ",\n" + "  " + field);
        }
      }
      buildMysqlQueryStrings.setWhere(buildMysqlQueryStrings.getWhere() + (buildMysqlQueryStrings.getWhere().isEmpty() ? "  " : "  AND ") + formatField(field, boolModePlot, false) +
        " IN (" + MVUtil.buildValueList(value) + ")\n");

      buildMysqlQueryStrings.setTempList((buildMysqlQueryStrings.getTempList().isEmpty() ? "" : ",\n") + "    " + MVUtil.padEnd(field, 20) + strTempType);
    }
    return buildMysqlQueryStrings;
  }

}


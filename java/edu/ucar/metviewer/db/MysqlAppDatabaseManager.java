/**
 * MysqlAppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import edu.ucar.metviewer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
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
  private final Map<String, String> statHeaderSQLType = new HashMap<>();
  private final Map<String, String> modeHeaderSQLType = new HashMap<>();
  private final Map<String, String> mtd3dSingleStatField = new HashMap<>();


  private final Map<String, String> mtdHeaderSQLType = new HashMap<>();
  public MysqlAppDatabaseManager(
                                    DatabaseInfo databaseInfo,
                                    PrintWriter printStreamSql) throws SQLException {
    super(databaseInfo, printStreamSql);
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


    mtdHeaderSQLType.put("model", "VARCHAR(64)");
    mtdHeaderSQLType.put("descr", "VARCHAR(64)");
    mtdHeaderSQLType.put("fcst_lead", "INT UNSIGNED");
    mtdHeaderSQLType.put("fcst_valid", "DATETIME");
    mtdHeaderSQLType.put("fcst_accum", "INT UNSIGNED");
    mtdHeaderSQLType.put("fcst_init", "DATETIME");
    mtdHeaderSQLType.put("obs_lead", "INT UNSIGNED");
    mtdHeaderSQLType.put("t_delta", "INT");
    mtdHeaderSQLType.put("fcst_rad", "INT UNSIGNED");
    mtdHeaderSQLType.put("fcst_thr", "VARCHAR(16)");
    mtdHeaderSQLType.put("fcst_var", "VARCHAR(64)");
    mtdHeaderSQLType.put("fcst_lev", "VARCHAR(16)");
    mtdHeaderSQLType.put("obs_var", "VARCHAR(64)");
    mtdHeaderSQLType.put("obs_lev", "VARCHAR(16)");

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
                        "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'val1l2'  FROM line_data_val1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) val1l2)\n" +
                        "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'grad'  FROM line_data_grad  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) grad)\n" +
                        "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'vcnt'  FROM line_data_vcnt  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar +"' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) vcnt)\n";

    for (String database : currentDBName) {
      try (Connection con = getConnection(database);
           Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                ResultSet.CONCUR_READ_ONLY);
           ResultSet res = stmt.executeQuery(strSQL)) {
        int intStatIndex = 0;
        boolean boolCnt = false;
        boolean boolCts = false;
        boolean boolVcnt = false;
        while (res.next()) {
          int intStatCount = res.getInt(1);
          if (-9999 != intStatCount) {
            switch (intStatIndex) {
              case 0:
              case 1:
              case 15:
              case 17:
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
                listStatName.addAll(MVUtil.statsVcnt.keySet());
                boolVcnt=true;
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
              case 18:
                if(!boolVcnt) {
                  listStatName.addAll(MVUtil.statsVcnt.keySet());
                }
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
    boolean boolMtd = nodeCall._children[1]._tag.equals("mtd_field");
    boolean boolRhist = nodeCall._children[1]._tag.equals("rhist_field");
    boolean boolPhist = nodeCall._children[1]._tag.equals("phist_field");
    boolean boolROC = nodeCall._children[1]._tag.equals("roc_field");
    boolean boolRely = nodeCall._children[1]._tag.equals("rely_field");
    boolean boolEnsSS = nodeCall._children[1]._tag.equals("ensss_field");
    boolean boolPerf = nodeCall._children[1]._tag.equals("perf_field");
    boolean boolTaylor = nodeCall._children[1]._tag.equals("taylor_field");
    boolean boolEclv = nodeCall._children[1]._tag.equals("eclv_field");
    String strHeaderTable;
    if (boolMode) {
      strHeaderTable = "mode_header";
    } else if (boolMtd) {
      strHeaderTable = "mtd_header";
    } else {
      strHeaderTable = "stat_header";
    }
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
    } else if (boolEclv) {
      tableLineDataTables.put("line_data_eclv", "true");
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
      if (strFieldCrit.contains("valid") || strFieldCrit.contains("init") || strFieldCrit.contains(
          "lead")) {
        boolTimeCritField = strField.equals(strFieldCrit) ||
                                (strField.contains("fcst_init") && strFieldCrit
                                                                       .equals("init_hour")) ||
                                (strField.contains("fcst_valid") && strFieldCrit
                                                                        .equals("valid_hour"));
        boolTimeCritCur = true;
      }
      //  if so, build a where clause for the criteria
      String strFieldDBCrit = formatField(strFieldCrit, boolMode || boolMtd, false);
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
        if (boolMode || boolMtd) {
          strWhere += (strWhere.equals(
              "") ? " WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
        } else {
          strWhereTime += (strWhereTime.equals(
              "") ? " WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
        }
      } else if (!boolTimeCritCur) {
        strWhere += (strWhere.equals(
            "") ? "WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
      }
    }

    //  build a query for the values
    String strSQL;
    String strTmpTable = null;
    for (String database : currentDBName) {
      try (Connection con = getConnection(database)) {
        if (boolNRank) {
          strSQL = "SELECT DISTINCT ld.n_rank "
                       + "FROM stat_header h, line_data_rhist ld "
                       + strWhere + (strWhere.equals("") ? "WHERE" : " AND")
                       + " ld.stat_header_id = h.stat_header_id "
                       + "ORDER BY n_rank;";
        } else if (boolNBin) {
          strSQL = "SELECT DISTINCT ld.n_bin "
                       + "FROM stat_header h, line_data_phist ld "
                       + strWhere + (strWhere.equals("") ? "WHERE" : " AND")
                       + " ld.stat_header_id = h.stat_header_id "
                       + "ORDER BY ld.n_bin;";
        } else if (!boolMode && !boolMtd
                       && (strField.equals("fcst_lead")
                               || strField.contains("valid")
                               || strField.contains("init"))) {
          String strSelectField = formatField(strField, boolMode || boolMtd);
          //  create a temp table for the list values from the different line_data tables
          strTmpTable = "tmp_" + new Date().getTime();
          try (Statement stmtTmp = con.createStatement()) {
            String strTmpSQL = "CREATE TEMPORARY TABLE " + strTmpTable + " (" + strField + " TEXT);";
            stmtTmp.executeUpdate(strTmpSQL);
            //  add all distinct list field values to the temp table from each line_data table
            for (String listTable : listTables) {
              strTmpSQL = "INSERT INTO " + strTmpTable
                              + " SELECT DISTINCT " + strSelectField
                              + " FROM " + listTable + " ld" + strWhereTime;
              stmtTmp.executeUpdate(strTmpSQL);
            }
            stmtTmp.close();
          } catch (SQLException e) {
            logger.error(e.getMessage());
          }

          //  build a query to list all distinct, ordered values of the list field from the temp table
          strSQL = "SELECT DISTINCT " + strField + " FROM "
                       + strTmpTable + " ORDER BY " + strField + ";";
        } else {
          String strFieldDB = formatField(strField, boolMode || boolMtd).replaceAll("h\\.", "");
          strWhere = strWhere.replaceAll("h\\.", "");
          strSQL = "SELECT DISTINCT " + strFieldDB + " FROM "
                       + strHeaderTable + " " + strWhere + " ORDER BY " + strField;
        }
        //  execute the query
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                  ResultSet.CONCUR_READ_ONLY);
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
  public boolean executeQueriesAndSaveToFile(
                                                List<String> queries, String fileName,
                                                boolean isCalc, String currentDBName,
                                                boolean isNewFile) throws Exception {
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
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                  ResultSet.CONCUR_READ_ONLY)) {
          stmt.execute(aListSQLBeforeSelect);
          stmt.close();
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }

      for (int i = 0; i < listSQLLastSelect.size(); i++) {
        boolean append = !isNewFile || i != 0;
        boolean printHeader = !append;
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                  ResultSet.CONCUR_READ_ONLY);
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
          String stat = "This";
          if (e.getMessage().contains("Unknown column")) {
            String[] queryArr = listSQLLastSelect.get(i).split(",");
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
   * Prints a textual representation of the input {@link ResultSet} with the field names in the
   * first row to the specified {@link BufferedWriter} destination.
   *
   * @param res            The ResultSet to print
   * @param bufferedWriter The stream to write the formatted results to (defaults to printStream)
   * @param delim          The delimiter to insert between field headers and values (defaults to '
   *                       ')
   */

  private void printFormattedTable(
                                      ResultSet res, BufferedWriter bufferedWriter, String delim,
                                      boolean isCalc, boolean isHeader) {

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

      while (res.next()) {
        String line = "";
        for (int i = 1; i <= met.getColumnCount(); i++) {
          String strVal;
          String objectType = met.getColumnTypeName(i);


          if (objectType.equals("DATETIME")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Timestamp ts = res.getTimestamp(i, cal);
            strVal = MysqlDatabaseManager.DATE_FORMAT.format(ts);
          } else {

            strVal = res.getString(i);
            strVal = strVal.equalsIgnoreCase("null") ? "NA" : strVal;
            strVal = strVal.equalsIgnoreCase("-9999") ? "NA" : strVal;
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
        bufferedWriter.write(line);
        bufferedWriter.write(System.getProperty("line.separator"));
        intLine++;

      }

      if (0 == intLine) {
        logger.info("No data was returned from database");
        throw new EmptyResultSetException("result set contained no data");
      }

    } catch (Exception e) {
      logger.error(
          "  **  ERROR: Caught " + e.getClass() + " in printFormattedTable(ResultSet res): " + e.getMessage());
    }
  }

  private Map<String, Integer> getPctThreshInfo(String query, String currentDBName) {
    int numPctThresh = 0;
    int pctThresh = -1;
    Map<String, Integer> result = new HashMap<>();
    try (Connection con = getConnection(currentDBName);
         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                              ResultSet.CONCUR_READ_ONLY);
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
         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                              ResultSet.CONCUR_READ_ONLY);
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

  @Override
  public SimpleDateFormat getDateFormat() {
    return MysqlDatabaseManager.DATE_FORMAT;
  }

  @Override
  public Pattern getDateRangePattern() {

    return Pattern.compile("(?i)\\s*between\\s+'([^']+)'\\s+and\\s+'([^']+)'\\s*");
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
  public List<String> buildPlotSQL(
                                      MVPlotJob job, MVOrderedMap mapPlotFixPerm,
                                      PrintWriter printStreamSQL) throws Exception {
    MVOrderedMap _mapFcstVarPat = new MVOrderedMap();
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    //  determine if the plot job is for stat data or MODE data
    Map<String, String> tableHeaderSQLType;
    if (job.isModeJob()) {
      tableHeaderSQLType = modeHeaderSQLType;
    } else if (job.isMtdJob()) {
      tableHeaderSQLType = mtdHeaderSQLType;
    } else {
      tableHeaderSQLType = statHeaderSQLType;
    }

    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job,
                                               job.isModeJob() || job.isMtdJob());

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && !job.getPlotCond().isEmpty()) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + "\n";
    }

    //  determine if the plot requires data aggregation or calculations
    boolean boolAggStat = job.getAggCtc()
                              || job.getAggSl1l2()
                              || job.getAggSal1l2()
                              || job.getAggPct()
                              || job.getAggNbrCnt()
                              || job.getAggSsvar()
                              || job.getAggVl1l2()
                              || job.getAggVal1l2()
                              || job.getAggGrad();

    boolean boolCalcStat = job.isModeRatioJob()
                               || job.isMtdRatioJob()
                               || job.getCalcCtc()
                               || job.getCalcSl1l2()
                               || job.getCalcSal1l2()
                               || job.getCalcVl1l2()
                               || job.getCalcGrad();
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
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val())
                                   .getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length && !job.getPlotTmpl()
                                                                  .equals("eclv.R_tmpl")) {
        throw new Exception("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2

      if (!job.getPlotTmpl().equals(
          "eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
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
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(job.isModeJob() || job.isMtdJob(),
                                                            tableHeaderSQLType,
                                                            listSeries, strWhere, true);
      String strSelectList = buildMysqlQueryStrings.getSelectList();
      String strTempList = buildMysqlQueryStrings.getTempList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectList.contains("fcst_init")) {
        if (job.isModeJob() || job.isMtdJob()) {
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
        if (job.isModeJob() || job.isMtdJob()) {
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
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
                                                           tableHeaderSQLType,
                                                           listSeries, strWhere, false);
      strSelectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectPlotList.contains("fcst_init") && !strSelectPlotList.contains("init_hour")) {
        if (job.isModeJob() || job.isMtdJob()) {
          strSelectPlotList += ",\n  h.fcst_init";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!strSelectPlotList.contains("fcst_valid")) {
        if (job.isModeJob() || job.isMtdJob()) {
          strSelectPlotList += ",\n  h.fcst_valid";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_valid_beg";
        }
      }

      if (!boolEnsSs && !strSelectList.contains("fcst_lead")) {
        if (job.isModeJob() || job.isMtdJob()) {

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
          throw new Exception("unrecognized indep " + (job.isModeJob() ? "mode" : "stat") + "_header field: " + strIndyVar);
        }
        strIndyVarType = tableHeaderSQLType.get(strIndyVar);
        if (1 > listIndyVal.length) {
          throw new Exception("no independent variable values specified");
        }

        //  construct the select list item, where clause and temp table entry for the independent variable
        if (!strSelectList.contains(strIndyVar)) {
          strSelectList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                                                 true);
          strSelectPlotList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                                                     true);
          strTempList += ",\n    " + MVUtil.padEnd(strIndyVar, 20) + strIndyVarType;
        }
        strIndyVarFormatted = formatField(strIndyVar, job.isModeJob() || job.isMtdJob(), false);
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
            strSelectList += ",\n  " + formatField(strField, job.isModeJob() || job.isMtdJob(),
                                                   true);
            strSelectPlotList += ",\n  " + formatField(strField, job.isModeJob() || job.isMtdJob(),
                                                       true);
            strTempList += ",\n    " + strField + "            VARCHAR(64)";
          }
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
                String strSelPctThresh = "SELECT DISTINCT ld.n_thresh\nFROM\n  stat_header h,\n  line_data_pct ld\n";
                strSelPctThresh = strSelPctThresh + "WHERE\n";
                if (strIndyVarFormatted.length() > 0 && job.getIndyVal().length > 0) {
                  strSelPctThresh = strSelPctThresh + strIndyVarFormatted;
                  strSelPctThresh = strSelPctThresh + " IN (" + MVUtil.buildValueList(
                      job.getIndyVal()) + ")\n " + " AND ";
                }
                strSelPctThresh = strSelPctThresh + serName[serNameInd] + " = '" + ser.getStr
                                                                                           (serName[serNameInd]) + "'";
                if (!vars[varsInd].equals("NA")) {
                  strSelPctThresh = strSelPctThresh + " AND fcst_var='" + vars[varsInd] + "' ";
                }
                if (strPlotFixWhere.length() > 0) {
                  strSelPctThresh = strSelPctThresh + "  AND  " + strPlotFixWhere;
                }
                strSelPctThresh = strSelPctThresh + "  AND ld.stat_header_id = h.stat_header_id;";
                printStreamSQL.println(strSelPctThresh + "\n");

                //  run the PCT thresh query
                pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(0));
                if (1 != pctThreshInfo.get("numPctThresh")) {
                  String error = "number of PCT thresholds (" + pctThreshInfo.get(
                      "numPctThresh") + ") not distinct for " + serName[serNameInd] + " = '" + ser.getStr(
                      serName[serNameInd]);
                  if (!vars[varsInd].equals("NA")) {
                    error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                  }
                  throw new Exception(error);
                } else if (1 > pctThreshInfo.get("numPctThresh")) {
                  String error = "invalid number of PCT thresholds (" + pctThreshInfo.get(
                      "numPctThresh") + ") found for " + serName[serNameInd] + " = '" + ser.getStr(
                      serName[serNameInd]);
                  if (!vars[varsInd].equals("NA")) {
                    error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                  }
                  throw new Exception(error);
                }
                seriesNthresh[seriesInd] = pctThreshInfo.get("pctThresh");
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
              throw new Exception(error);
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
            throw new Exception("unrecognized mode stat: " + strStatMode);
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
            throw new Exception("unrecognized mode stat: " + stat);
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
            aggType = MVUtil.NBR_CNT;
          } else if (job.getAggPct()) {
            aggType = MVUtil.PCT;
          } else if (job.getAggSsvar()) {
            aggType = MVUtil.SSVAR;
          } else if (job.getCalcVl1l2() || job.getAggVl1l2()) {
            aggType = MVUtil.VL1L2;
          } else if (job.getAggVal1l2()) {
            aggType = MVUtil.VAL1L2;
          }


          if (MVUtil.statsCnt.containsKey(strStat)) {
            tableStats = MVUtil.statsCnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsCnt, strStat, aggType);
              strStatTable = "line_data_" + aggType + " ld\n";
            } else {
              strStatTable = "line_data_cnt" + " ld\n";
            }
          } else if (MVUtil.statsSsvar.containsKey(strStat)) {
            tableStats = MVUtil.statsSsvar;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsSsvar, strStat, aggType);
              strStatTable = "line_data_" + aggType + " ld\n";
            } else {
              strStatTable = "line_data_ssvar" + " ld\n";
            }
          } else if (MVUtil.statsCts.containsKey(strStat)) {
            tableStats = MVUtil.statsCts;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsCts, strStat, aggType);
              strStatTable = "line_data_ctc" + " ld\n";
            } else {
              strStatTable = "line_data_cts" + " ld\n";
            }
          } else if (MVUtil.statsNbrcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsNbrcnt, strStat, aggType);
            }
            strStatTable = "line_data_nbrcnt ld\n";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsEnscnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEnscnt;
            if (aggType != null) {
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
            if (aggType != null) {
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
            if (aggType != null) {
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
          } else if (MVUtil.statsVcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsVcnt;
            strStatField = strStat.replace("VCNT_", "").toLowerCase();
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVcnt, strStat, aggType);
              strStatTable = "line_data_" + aggType + " ld\n";
            } else {
              strStatTable = "line_data_vcnt" + " ld\n";
            }
          } else if (strStat.equals("ECLV") && job.getPlotTmpl().equals("eclv.R_tmpl")) {
            if (aggType != null && aggType.equals(MVUtil.CTC)) {
              tableStats = MVUtil.statsCts;
              strStatTable = "line_data_ctc" + " ld\n";
            } else {
              tableStats = MVUtil.statsPstd;
              strStatTable = "line_data_pct ld,\n  line_data_pct_thresh ldt\n";
            }

          } else {
            throw new Exception("unrecognized stat: " + strStat);
          }
        }

        //  build the SQL for the current fcst_var and stat
        if (job.isModeJob()) {

          //  build the mode SQL
          String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;

          listSQL.addAll(buildModeStatSQL(strSelectList, strWhereFcstVar, strStat, listGroupBy,
                                          job.getEventEqual()));
        } else if (job.isMtdJob()) {

          //  build the mtd SQL
          String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;
          listSQL.addAll(buildMtdStatSQL(strSelectList, strWhereFcstVar, strStat,
                                         listGroupBy,
                                         job.getEventEqual()));
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
          if (job.getAggCtc()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fy_oy,\n  ld.fy_on,\n  ld.fn_oy,\n  ld.fn_on";
          } else if (job.getAggSl1l2()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n ld.mae";
          } else if (job.getAggGrad()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fgbar,\n  ld.ogbar,\n  ld.mgbar,\n  ld.egbar,\n  ld.s1,\n ld.s1_og, \n ld.fgog_ratio";
          } else if (job.getAggSsvar()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n  ld.var_mean, \n  ld.bin_n";
          } else if (job.getAggSal1l2()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fabar,\n  ld.oabar,\n  ld.foabar,\n  ld.ffabar,\n  ld.ooabar,\n ld.mae";
          } else if (job.getAggPct()) {
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
              strSelectStat += ",\n  0 stat_value,\n  ld.n_thresh,\n ldt.thresh_i,\n ldt.oy_i\n,"
                                   + " ldt.on_i";
            }
          } else if (job.getAggNbrCnt()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbs,\n  ld.fss";
          } else if (job.getAggVl1l2()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufbar,\n ld.vfbar,\n ld.uobar,"
                                 + "\n ld.vobar,\n ld.uvfobar,\n ld.uvffbar,\n ld.uvoobar,"
                                 + " \n ld.f_speed_bar, \n ld.o_speed_bar";
          } else if (job.getAggVal1l2()) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufabar,\n ld.vfabar,\n ld.uoabar,\n ld.voabar,\n ld.uvfoabar,\n ld.uvffabar,\n ld.uvooabar";
          } else if (job.getCalcCtc()) {
            strSelectStat += ",\n  ld.total, ld.fy_oy, ld.fy_on, ld.fn_oy, ld.fn_on, 'NA' stat_value,\n" +
                                 "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (job.getCalcSl1l2()) {
            if (strStat.equalsIgnoreCase("mae")) {
              strSelectStat += ",\n   ld.mae,  'NA' stat_value,\n" +
                                   "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            } else {
              strSelectStat += ",\n  ld.total, ld.fbar, ld.obar, ld.fobar, ld.ffbar, ld.oobar, 'NA' stat_value,\n" +
                                   "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            }
          } else if (job.getCalcGrad()) {
            strSelectStat += ",\n  ld.total, ld.fgbar, ld.ogbar, ld.mgbar, ld.egbar,  'NA' stat_value,\n" +
                                 "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (job.getCalcSal1l2()) {
            strSelectStat += ",\n  ld.total, ld.fabar, ld.oabar, ld.foabar, ld.ffabar, ld.ooabar,  'NA' stat_value,\n" +
                                 "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (job.getCalcVl1l2()) {
            strSelectStat += ",\n  ld.total, ld.ufbar, ld.vfbar, ld.uobar, ld.vobar, ld.uvfobar, ld.uvffbar, ld.uvoobar,"
                                 + " ld.f_speed_bar, ld.o_speed_bar, 'NA' stat_value,\n" +
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
          if (job.getAggPct()) {
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
          strSelectSQL += (strSelectSQL.isEmpty() ? "" : "\nUNION ALL\n") +
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
    if (!job.isModeJob() && !job.isMtdJob()) {
      listSQL.add(strSelectSQL + ";");
    }

    //remove duplicated queries
    listSQL = new ArrayList<>(new LinkedHashSet<>(listSQL));

    return listSQL;
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
   * Use the input query components to build a list of select statements to gather plot data. This
   * function is a switchboard for the different types of MODE statistics: single, pair, derived,
   * difference and ratios.
   *
   * @param strSelectList list of select fields
   * @param strWhere      list of where clauses
   * @param strStat       MODE stat
   * @param listGroupBy   list of fields to group by
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildModeStatSQL(
                                           String strSelectList, String strWhere, String strStat,
                                           String[] listGroupBy, boolean isEventEqualization) {

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
            .add(buildModeSingleStatDiffTable(strSelectList, strWhere, strStat, query1, query2));
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
   * @param listGroupBy   list of fields to group by
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildMtdStatSQL(
                                          String strSelectList, String strWhere, String strStat,
                                          String[] listGroupBy, boolean isEventEqualization) {

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
            "FROM\n" +
            "  mode_header ,\n" +
            "  mode_obj_single ,\n" +
            "  mode_cts\n" +
            "WHERE\n" +
            strWhere + "\n" +
            "  AND simple_flag = 1\n" +
            "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n" +
            "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id\n" +
            "  AND mode_cts.field = 'OBJECT' "
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
    strWhere = strWhere.replace("h.", "");
    //  build the query
    return
        // "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
            "  " + strObjectId + ",\n" +
            "  object_cat,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + strTableStat + " stat_value\n" +
            "FROM\n" +
            "  mode_header,\n" +
            "  mode_obj_pair\n" +
            "WHERE\n" +
            strWhere
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
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];

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

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.mtd3dPairStatField.get(strStatName);

    //  build the query
    return
        // "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
            "  " + strObjectId + ",\n" +
            "  cluster_id,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + strTableStat + " stat_value\n" +
            "FROM mtd_header, mtd_3d_obj_pair \n" +
            "WHERE\n" + strWhere +
            " AND mtd_header.mtd_header_id = mtd_3d_obj_pair.mtd_header_id";
  }

  private String buildModeSingleStatTable(
                                             String selectList, String strWhere, String stat,
                                             String[] groups, boolean isEventEqualization) {

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
            "  " + MVUtil.modeSingleStatField.get(strStatName) + " stat_value\n" +
            "FROM\n" +
            "  mode_header ,\n" +
            "  mode_obj_single ,\n" +
            "  mode_cts \n" +
            "WHERE\n" + strWhere
            + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n"
            + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id\n"
            + "  AND mode_cts.field = 'OBJECT'"
            + strGroupBy;
  }


  private String buildMtd3dSingleStatTable(
                                              String selectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];
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


    //  build the query
    return
        "SELECT\n" + strSelectListStat + ",\n" +
            "  object_id,\n" +
            "  cluster_id,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + mtd3dSingleStatField.get(strStatName) + " stat_value\n" +
            "FROM mtd_header, mtd_3d_obj_single \n" +
            "WHERE\n" + strWhere +
            " AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";
  }


  private String buildMtd2dStatTable(
                                        String selectList, String strWhere, String stat) {


    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];


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


    //  build the query
    return
        "SELECT\n" + strSelectListStat + ",\n" +
            "  object_id,\n" +
            "  cluster_id,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + MVUtil.mtd2dStatField.get(strStatName) + " stat_value\n" +
            "FROM mtd_header, mtd_2d_obj \n" +
            "WHERE\n" + strWhere +
            " AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";
  }

  private String buildModeSingleStatRatioTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");
    strWhere = strWhere.replaceAll("h\\.", "");

    return
        "SELECT\n" + strSelectListStat + ",\n" +
            "  object_id,\n" +
            "  object_cat,\n" +
            "  area,\n" +
            "  total,\n" +
            "  fcst_flag,\n" +
            "  simple_flag,\n" +
            "  matched_flag\n" +
            "FROM\n" +
            "  mode_header ,\n" +
            "  mode_obj_single ,\n" +
            "  mode_cts \n" +
            "WHERE\n" + strWhere +
            "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n" +
            "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id" +
            "  AND mode_cts.field = 'OBJECT'";
  }

  private String buildMtdSingleStatRatio2dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
        "SELECT\n" + strSelectListStat + ",\n" +
            "  object_id,\n" +
            "  cluster_id,\n" +
            "  area,\n" +
            "  fcst_flag,\n" +
            "  simple_flag,\n" +
            "  matched_flag\n" +
            "FROM mtd_header, mtd_2d_obj\n" +
            "WHERE\n" + strWhere
            + "  AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";

  }

  private String buildMtdSingleStatRatio3dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
        "SELECT\n" + strSelectListStat + ",\n" +
            "  object_id,\n" +
            "  cluster_id,\n" +
            "  volume,\n" +
            "  fcst_flag,\n" +
            "  simple_flag,\n" +
            "  matched_flag\n" +
            "FROM mtd_header, mtd_3d_obj_single\n" +
            "WHERE\n" + strWhere
            + "  AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";

  }

  private String buildModeSingleStatDiffTable(
                                                 String strSelectList, String strWhere, String stat,
                                                 String table1, String table2) {

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
        "SELECT\n" + strSelectListStat + ",\n" +
            "  s.object_id,\n" +
            "  s.object_cat,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n" +
            "FROM ("
            + table1
            + " ) s, ( " + table2 + " ) s2\n" +
            "WHERE\n" +
            strWhere + "\n" +
            " AND SUBSTRING(s.object_id, -3) = SUBSTRING(s2.object_id,  -3)\n";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + "s.stat_value" + " != -9999 AND " + "s2.stat_value" + " != "
                   + "-9999;";
    }
    return result;
  }

  private String buildMtd3dSingleStatDiffTable(
                                                  String strSelectList, String strWhere, String
                                                                                             stat,
                                                  String table1, String table2) {

    //  parse the stat into the stat name and the object flags
    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "\n  AND s.fcst_var = s2.fcst_var";

   /* //  build the where clause using the input select fields
    Matcher mat = MVUtil.modeSingle.matcher(strSelectList);
    while (mat.find()) {
      if (!mat.group(1).contains("NULL") && !mat.group(1).contains("FROM") && !mat.group(1)
                                                                                   .contains(
                                                                                       "WHERE")) {
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
    }*/

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
        "SELECT\n" + strSelectListStat + ",\n" +
            "  s.object_id,\n" +
            "  s.cluster_id,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n" +
            "FROM ("
            + table1
            + " ) s, ( " + table2 + " ) s2\n" +
            "WHERE\n" +
            strWhere + "\n" +
            "  AND SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) = SUBSTRING(s2.object_id,  "
            + "LOCATE('_', s\n"
            + ".object_id)+1)\n";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0] + " != -9999 AND " + strTableStats[1] + " != -9999;";
    }
    return result;
  }

  private String buildMtd2dStatDiffTable(
                                            String strSelectList, String strWhere, String stat,
                                            String table1, String table2) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "\n  AND s.fcst_var = s2.fcst_var";


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
        "SELECT\n" + strSelectListStat + ",\n" +
            "  s.object_id,\n" +
            "  s.cluster_id,\n" +
            "  '" + stat + "' stat_name,\n" +
            "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n" +
            "FROM ("
            + table1
            + " ) s, ( " + table2 + " ) s2\n" +
            "WHERE\n" +
            strWhere + "\n" +
            "  AND SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) "
            + "= SUBSTRING(s2.object_id,  LOCATE('_', s.object_id)+1)\n";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0] + " != -9999 AND " + strTableStats[1] + " != -9999;";
    }
    return result;
  }


  @Override
  public List<String> buildPlotModeEventEqualizeSQL(
                                                       MVPlotJob job, MVOrderedMap mapPlotFixPerm,
                                                       MVOrderedMap mapPlotFixVal) throws Exception {
    MVOrderedMap _mapFcstVarPat = new MVOrderedMap();

    //  determine if the plot job is for stat data or MODE data
    Map<String, String> tableHeaderSQLType;
    if (job.isModeJob()) {
      tableHeaderSQLType = modeHeaderSQLType;
    } else {
      tableHeaderSQLType = statHeaderSQLType;
    }


    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job,
                                               job.isModeJob() || job.isMtdJob());

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
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val())
                                   .getOrderedEntriesForSqlSeries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length) {
        throw new Exception("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2
      if (!job.getPlotTmpl().equals(
          "eclv.R_tmpl") && 1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
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
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(job.isModeJob() || job.isMtdJob(),
                                                            tableHeaderSQLType,
                                                            listSeries, strWhere, true);
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
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
                                                           tableHeaderSQLType,
                                                           listSeries, strWhere, false);
      strSelectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectPlotList.contains("fcst_init") && !strSelectPlotList.contains("init_hour")) {
        if (job.isModeJob()) {
          strSelectPlotList += ",\n  h.fcst_init";
        } else {
          strSelectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!strSelectPlotList.contains("fcst_valid")) {
        if (job.isModeJob()) {
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
        throw new Exception("unrecognized indep " + (job.isModeJob() ? "mode" : "stat") + "_header field: " + strIndyVar);
      }
      strIndyVarType = tableHeaderSQLType.get(strIndyVar);
      if (1 > listIndyVal.length) {
        throw new Exception("no independent variable values specified");
      }

      //  construct the select list item, where clause and temp table entry for the independent variable
      if (!strSelectList.contains(strIndyVar)) {
        strSelectList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(), true);
        strSelectPlotList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                                                   true);
        strTempList += ",\n    " + MVUtil.padEnd(strIndyVar, 20) + strIndyVarType;
      }
      String strIndyVarFormatted = formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                                               false);
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
            strSelectList += ",\n  " + formatField(strField, job.isModeJob() || job.isMtdJob(),
                                                   true);
            strSelectPlotList += ",\n  " + formatField(strField, job.isModeJob() || job.isMtdJob(),
                                                       true);
            strTempList += ",\n    " + strField + "            VARCHAR(64)";
          }
        }
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
        Matcher matProb = MVUtil.prob.matcher(strFcstVar);
        if (matProb.matches() && strFcstVar.contains("*")) {
          Pattern patFcstVar = Pattern.compile(
              strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
          if (!_mapFcstVarPat.containsKey(patFcstVar)) {
            _mapFcstVarPat.put(patFcstVar, MVUtil.replaceSpecialChars(strFcstVar));
          }
          strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
        }

        //  build the mode SQL
        String strWhereFcstVar = strWhere + " AND  fcst_var " + strFcstVarClause;
        strWhereFcstVar = strWhereFcstVar.replace("h.", "");
        strSelectList = strSelectList.replace("h.", "");
        listSQL.addAll(buildModeStatEventEqualizeSQL(strSelectList, strWhereFcstVar));

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
            "  object_id,\n" +
            "  object_cat,\n" +
            "  area,\n" +
            "  total,\n" +
            "  fcst_flag,\n" +
            "  simple_flag,\n" +
            "  matched_flag\n" +
            "FROM\n"
            + " mode_header ,\n"
            + " mode_obj_single,\n"
            + " mode_cts \n" +
            "WHERE\n" + strWhere +
            " AND mode_obj_single.mode_header_id = mode_header.mode_header_id" +
            " AND mode_cts.mode_header_id = mode_obj_single.mode_header_id" +
            " AND mode_cts.field = 'OBJECT'";
  }

  @Override
  public String buildAndExecuteQueriesForHistJob(
                                                    MVPlotJob job, String strDataFile,
                                                    MVOrderedMap listPlotFixPerm,
                                                    PrintStream printStream,
                                                    PrintWriter printStreamSql) throws Exception {
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
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
                           + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false) +
                            " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n")
                         + "    " + MVUtil.padEnd(strSeriesField, 20)
                         + statHeaderSQLType.get(strSeriesField);

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
      executeQueriesAndSaveToFile(queries, strDataFile,
                                  job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() || job.getCalcGrad(),
                                  job.getCurrentDBName().get(i), i == 0);
    }
    return strMsg;
  }


  @Override
  public int buildAndExecuteQueriesForRocRelyJob(
                                                    MVPlotJob job, String strDataFile,
                                                    MVOrderedMap listPlotFixPerm,
                                                    PrintStream printStream,
                                                    PrintWriter printStreamSql) throws Exception {
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
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
                           + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false)
                            + " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n") + "    "
                         + MVUtil.padEnd(strSeriesField, 20)
                         + statHeaderSQLType.get(strSeriesField);

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
      executeQueriesAndSaveToFile(queries, strDataFile,
                                  job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() || job.getCalcGrad(),
                                  job.getCurrentDBName().get(i), i == 0);
    }

    return intNumDepSeries;
  }

  @Override
  public int buildAndExecuteQueriesForEclvJob(
                                                 MVPlotJob job, String strDataFile,
                                                 MVOrderedMap listPlotFixPerm,
                                                 PrintStream printStream,
                                                 PrintWriter printStreamSql) throws Exception {
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
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
                           + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + formatField(strSeriesField, false, false) +
                            " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
      strTempList += (strTempList.isEmpty() ? "" : ",\n")
                         + "    " + MVUtil.padEnd(strSeriesField, 20)
                         + statHeaderSQLType.get(strSeriesField);

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
        String strSelPctThresh = "SELECT DISTINCT ld.n_pnt\nFROM\n  stat_header h,\n  "
                                     + "line_data_eclv ld,\n line_data_eclv_pnt ldt\n" +
                                     "WHERE\n"
                                     + serName[serNameInd] + " = '" + ser.getStr(
            serName[serNameInd]) + "' AND " + strWhere
                                     + "  AND ld.stat_header_id = h.stat_header_id AND ld.line_data_id = ldt.line_data_id;";
        printStreamSql.println(strSelPctThresh + "\n");

        //  run the PCT thresh query
        pctThreshInfo = getPctThreshInfo(strSelPctThresh, job.getCurrentDBName().get(0));
        if (1 != pctThreshInfo.get("numPctThresh")) {
          throw new Exception("number of ECLV pnts (" + pctThreshInfo.get(
              "numPctThresh") + ") not distinct for " + serName[serNameInd] + " = '" + ser.getStr(
              serName[serNameInd]));
        } else if (1 > pctThreshInfo.get("numPctThresh")) {
          throw new Exception("invalid number of ECLV pnts (" + pctThreshInfo.get(
              "numPctThresh") + ") found for" + serName[serNameInd] + " = '" + ser.getStr(
              serName[serNameInd]) +
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
      executeQueriesAndSaveToFile(queries, strDataFile,
                                  job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() || job.getCalcGrad(),
                                  job.getCurrentDBName().get(i), i == 0);
    }

    return intNumDepSeries;
  }

  @Override
  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
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

  private BuildMysqlQueryStrings build(
                                          boolean boolModePlot,
                                          Map<String, String> tableHeaderSQLType,
                                          Map.Entry[] listSeries, String strWhere,
                                          boolean isFormatSelect) throws Exception {

    BuildMysqlQueryStrings buildMysqlQueryStrings = new BuildMysqlQueryStrings(boolModePlot,
                                                                               tableHeaderSQLType,
                                                                               listSeries, strWhere,
                                                                               isFormatSelect);
    for (Map.Entry entry : listSeries) {

      //  get the current series field and values
      String field = entry.getKey().toString();
      String[] value = (String[]) entry.getValue();

      //  validate the series field and get its type
      String strTempType;
      if (!tableHeaderSQLType.containsKey(field)) {
        throw new Exception("unrecognized " + (boolModePlot ? "mode" : "stat")
                                + "_header field: " + field);
      }
      strTempType = tableHeaderSQLType.get(field);

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
              buildMysqlQueryStrings.getSelectList() + ",\n"
                  + "  " + formatField(field, boolModePlot, true));

        } else {
          buildMysqlQueryStrings
              .setSelectList(buildMysqlQueryStrings.getSelectList() + ",\n" + "  " + field);
        }
      }
      buildMysqlQueryStrings.setWhere(
          buildMysqlQueryStrings.getWhere()
              + (buildMysqlQueryStrings.getWhere().isEmpty() ? "  " : "  AND ")
              + formatField(field, boolModePlot, false)
              + " IN (" + MVUtil.buildValueList(value) + ")\n");

      buildMysqlQueryStrings.setTempList(
          (buildMysqlQueryStrings.getTempList().isEmpty() ? "" : ",\n")
              + "    " + MVUtil.padEnd(field, 20) + strTempType);
    }
    return buildMysqlQueryStrings;
  }

}


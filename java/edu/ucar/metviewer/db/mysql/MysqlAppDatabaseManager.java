/**
 * MysqlAppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.mysql;

import edu.ucar.metviewer.*;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public class MysqlAppDatabaseManager extends MysqlDatabaseManager implements AppDatabaseManager {


  private static final Logger logger = LogManager.getLogger("MysqlAppDatabaseManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private final List<String> statHeaderSqlType = new ArrayList<>();
  private final List<String> modeHeaderSqlType = new ArrayList<>();
  private final Map<String, String> mtd3dSingleStatField = new HashMap<>();


  private final List<String> mtdHeaderSqlType = new ArrayList<>();

  public MysqlAppDatabaseManager(DatabaseInfo databaseInfo, String password) {
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
    mtdHeaderSqlType.add("fcst_t_beg");
    mtdHeaderSqlType.add("fcst_t_end");
    mtdHeaderSqlType.add("obs_t_beg");
    mtdHeaderSqlType.add("obs_t_end");

  }


  @Override
  public List<String> getListStat(String strFcstVar, String[] currentDBName) {
    List<String> listStatName = new ArrayList<>();
    //strFcstVar

    String strSql = "(SELECT IFNULL( (SELECT ld.stat_header_id  'cnt'    FROM line_data_cnt    "
            + "ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) cnt)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'sl1l2'  FROM "
            + "line_data_sl1l2  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) sl1l2)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'cts'    FROM "
            + "line_data_cts    ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) cts)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ctc'    FROM "
            + "line_data_ctc    ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) ctc)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'nbrcnt' FROM "
            + "line_data_nbrcnt ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) nbrcnt)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'nbrcts' FROM "
            + "line_data_nbrcts ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) nbrcts)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'pstd'   FROM "
            + "line_data_pstd   ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) pstd)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'mcts'   FROM "
            + "line_data_mcts   ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) mcts)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'rhist'  FROM "
            + "line_data_rhist  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) rhist)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'vl1l2'  FROM "
            + "line_data_vl1l2  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) vl1l2)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'phist'  FROM "
            + "line_data_phist  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1)  ,-9999) phist)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'enscnt'  FROM "
            + "line_data_enscnt  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) enscnt)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'mpr'  FROM "
            + "line_data_mpr  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) mpr)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'orank'  FROM "
            + "line_data_orank  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) orank)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ssvar'  FROM "
            + "line_data_ssvar  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) ssvar)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'sal1l2'  FROM "
            + "line_data_sal1l2  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) sal1l2)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'val1l2'  FROM "
            + "line_data_val1l2  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) val1l2)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'grad'  FROM "
            + "line_data_grad  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) grad)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'vcnt'  FROM "
            + "line_data_vcnt  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) vcnt)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ecnt'  FROM "
            + "line_data_ecnt  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) ecnt)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'perc'  FROM "
            + "line_data_perc  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) perc)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'dmap'  FROM "
            + "line_data_dmap  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) dmap)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'rps'  FROM "
            + "line_data_rps  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) rps)\n"
            + "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'pct'  FROM "
            + "line_data_pct  ld, stat_header h WHERE h.fcst_var = ? AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) pct)\n";


    for (String database : currentDBName) {
      ResultSet res = null;
      try (Connection con = getConnection(database);
           PreparedStatement stmt = con.prepareStatement(strSql, ResultSet.TYPE_FORWARD_ONLY,
                   ResultSet.CONCUR_READ_ONLY)) {
        for (int i = 1; i <= 24; i++) {
          stmt.setString(i, strFcstVar);
        }
        res = stmt.executeQuery();
        int intStatIndex = 0;
        boolean boolCnt = false;
        boolean boolCts = false;
        boolean boolVcnt = false;
        boolean boolSl1l2 = false;
        boolean boolSal1l2 = false;
        boolean boolGrad = false;
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
                  boolCnt = true;
                }
                if (!boolSl1l2) {
                  listStatName.addAll(MVUtil.statsSl1l2.keySet());
                  boolSl1l2 = true;
                }
                if (!boolSal1l2) {
                  listStatName.addAll(MVUtil.statsSal1l2.keySet());
                  boolSal1l2 = true;
                }
                if (!boolGrad) {
                  listStatName.addAll(MVUtil.statsGrad.keySet());
                  boolGrad = true;
                }
                break;
              case 2:
              case 3:
                if (!boolCts) {
                  listStatName.addAll(MVUtil.statsCts.keySet());
                  listStatName.addAll(MVUtil.statsCtc.keySet());
                }
                boolCts = true;
                break;
              case 4:
                listStatName.addAll(MVUtil.statsNbrcnt.keySet());
                break;
              case 5:
                listStatName.addAll(MVUtil.statsNbrcts.keySet());
                listStatName.addAll(MVUtil.statsNbrctc.keySet());
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
                boolVcnt = true;
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
                if (!boolVcnt) {
                  listStatName.addAll(MVUtil.statsVcnt.keySet());
                }
                break;
              case 19:
                listStatName.addAll(MVUtil.statsEcnt.keySet());
                break;
              case 20:
                listStatName.addAll(MVUtil.statsPerc.keySet());
                break;
              case 21:
                listStatName.addAll(MVUtil.statsDmap.keySet());
                break;
              case 22:
                listStatName.addAll(MVUtil.statsRps.keySet());
                break;
              case 23:
                listStatName.addAll(MVUtil.statsPct.keySet());
                break;
              default:

            }
          }
          intStatIndex++;
        }
      } catch (SQLException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      } finally {
        if (res != null) {
          try {
            res.close();
          } catch (SQLException e) {
            logger.error(ERROR_MARKER, e.getMessage());
          }
        }
      }
    }
    Collections.sort(listStatName);
    return listStatName;
  }

  @Override
  public List<String> getListValues(MVNode nodeCall, String field, String[] currentDBName) {
    List<String> listRes = new ArrayList<>();
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
      strHeaderTable = "mode_header";
    } else if (boolMtd) {
      strHeaderTable = "mtd_header";
    } else {
      strHeaderTable = "stat_header";
    }
    boolean boolNRank = field.equalsIgnoreCase("N_RANK");
    boolean boolNBin = field.equalsIgnoreCase("N_BIN");

    //  parse the fcst_var/stat constraint to build a list of line_data tables and fcst_var values
    Map<String, String> tableFcstVarStat = new HashMap<>();
    Map<String, String> tableLineDataTables = new HashMap<>();
    boolean fcstVar = false;
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
    } else if (1 < nodeCall.children.length) {
      if (boolEclv) {
        tableLineDataTables.put("line_data_eclv", "true");
      }
      fcstVar = true;
      MVNode nodeFcstVarStat = nodeCall.children[1];
      for (int i = 0; i < nodeFcstVarStat.children.length; i++) {
        MVNode nodeFcstVar = nodeFcstVarStat.children[i];
        if (!nodeFcstVar.name.isEmpty()) {
          tableFcstVarStat.put(nodeFcstVar.name, "true");
        }
        for (int j = 0; j < nodeFcstVar.children.length; j++) {
          String strStat = nodeFcstVar.children[j].value;
          String strLineDataTable = MVUtil.getStatTable(strStat);
          if (!strLineDataTable.isEmpty()) {
            tableLineDataTables.put(strLineDataTable, "true");
            if (strLineDataTable.equals("line_data_cnt")) {
              tableLineDataTables.put("line_data_sl1l2", "true");
              tableLineDataTables.put("line_data_sal1l2", "true");
            } else if (strLineDataTable.equals("line_data_cts")) {
              tableLineDataTables.put("line_data_ctc", "true");
            } else if (strLineDataTable.equals("line_data_vcnt")) {
              tableLineDataTables.put("line_data_vl1l2", "true");
            }
          }

        }
      }
    }
    String[] tables = tableLineDataTables.keySet().toArray(new String[]{});
    StringBuilder where = new StringBuilder();
    //we need to get all values of obs_var for all variables
    if (field.equalsIgnoreCase("obs_var")) {
      fcstVar = false;
    }
    if (fcstVar) {
      String fcstVarList = "";
      String[] fcstVars = tableFcstVarStat.keySet().toArray(new String[]{});
      boolean regEx = false;
      for (int i = 0; i < fcstVars.length; i++) {
        if (fcstVars[i].contains("*")) {
          regEx = true;
        }
        if (fcstVars[i].length() > 0 && !fcstVars[i].equals("NA")) {
          fcstVarList += (0 < i ? ", " : "") + "'" + fcstVars[i].replace("*", "%") + "'";
        }
      }
      if (fcstVarList.length() > 0) {
        where.append("WHERE").append(BINARY).append("h.fcst_var ").append(regEx ? "LIKE" : "IN")
                .append(" (")
                .append(fcstVarList).append(")");
      }
    }

    //  parse the list of constraints into a SQL where clause
    String whereTime = "";
    for (int i = 1; i < nodeCall.children.length; i++) {
      if (nodeCall.children[i].tag.equals("stat")) {
        continue;
      }

      //  determine if the field should be used as criteria
      MVNode nodeField = nodeCall.children[i];
      String fieldCrit = nodeField.name.toLowerCase(Locale.ENGLISH);
      boolean timeCritField = false;
      boolean timeCritCur = false;
      if (fieldCrit.contains("valid")
              || fieldCrit.contains("init")
              || fieldCrit.contains("lead")) {
        timeCritField = field.equals(fieldCrit)
                || (field.contains("fcst_init") && fieldCrit.equals("init_hour"))
                || (field.contains("fcst_valid") && fieldCrit.equals("valid_hour"));
        timeCritCur = true;
      }
      //  if so, build a where clause for the criteria
      String fieldDBCrit = formatField(fieldCrit, boolMode || boolMtd, false);
      if (fieldDBCrit.contains("n_rank") || fieldDBCrit.contains("n_bin")) {
        continue;
      }
      String sqlOp = "IN";
      String valList = "";
      for (int j = 0; j < nodeField.children.length; j++) {
        String strVal = nodeField.children[j].value;
        if (strVal.contains("*")) {
          sqlOp = "LIKE";
        }
        valList += (0 < j ? ", " : "") + "'" + strVal.replace("*", "%") + "'";
      }

      //  add the where clause to the criteria, if appropriate
      if (timeCritField) {
        if (boolMode || boolMtd) {
          where.append(where.length() == 0 ? " WHERE " : " AND ").append(BINARY).append(fieldDBCrit)
                  .append(" ")
                  .append(sqlOp).append(" ").append("(").append(valList).append(")");
        } else {
          whereTime += (whereTime.equals("") ? " WHERE " : " AND ")
                  + fieldDBCrit + " " + sqlOp + " (" + valList + ")";
        }
      } else if (!timeCritCur) {
        where.append(where.length() == 0 ? "WHERE " : " AND ").append(BINARY).append(fieldDBCrit)
                .append(" ")
                .append(sqlOp).append(" (").append(valList).append(")");
      }
    }

    //  build a query for the values
    String strSql;
    for (String database : currentDBName) {
      try (Connection con = getConnection(database)) {
        if (boolNRank) {
          strSql = "SELECT DISTINCT ld.n_rank "
                  + "FROM stat_header h, line_data_rhist ld "
                  + where + (where.length() == 0 ? "WHERE" : " AND")
                  + " ld.stat_header_id = h.stat_header_id "
                  + "ORDER BY n_rank;";
        } else if (boolNBin) {
          strSql = "SELECT DISTINCT ld.n_bin "
                  + "FROM stat_header h, line_data_phist ld "
                  + where + (where.length() == 0 ? "WHERE" : " AND")
                  + " ld.stat_header_id = h.stat_header_id "
                  + "ORDER BY ld.n_bin;";
        } else if (!boolMode && !boolMtd
                && (field.equals("fcst_lead")
                || field.contains("valid")
                || field.contains("init"))) {
          String formattedField = formatField(field, boolMode || boolMtd, true);

          strSql = "SELECT DISTINCT " + field + " FROM (";

          for (int i = 0; i < tables.length; i++) {
            strSql = strSql + " SELECT DISTINCT " + formattedField + " FROM " + tables[i] + " " + whereTime;
            if (i != tables.length - 1) {
              strSql = strSql + " UNION ";
            }
          }
          strSql = strSql + ") ll  ORDER BY " + field;

        } else {

          String strFieldDB = formatField(field, boolMode || boolMtd, true);
          String whereReplaced = where.toString().replaceAll("h\\.", "");
          strSql = "SELECT DISTINCT " + strFieldDB + " FROM "
                  + strHeaderTable + " " + whereReplaced + " ORDER BY " + field;
        }
        //  execute the query
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
             ResultSet res = stmt.executeQuery(strSql)) {

          while (res.next()) {
            if (field.startsWith("fcst_valid") || field.startsWith("obs_valid")
                    || field.startsWith("fcst_init") || field.startsWith("obs_init")) {
              LocalDateTime ts = res.getTimestamp(1).toLocalDateTime();
              listRes.add(DATE_FORMATTER.format(ts));
            } else {
              listRes.add(res.getString(1));
            }
          }

        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      } catch (SQLException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
    }
    //Collections.sort(listRes);
    return listRes;
  }

  @Override
  public MvResponse executeQueriesAndSaveToFile(
          List<String> queries, String fileName,
          boolean isCalc, String currentDBName,
          boolean isNewFile) throws ValidationException {
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

    try (Connection con = getConnection(currentDBName)) {
      dbStopWatch.start();
      for (String aListSqlBeforeSelect : listSqlBeforeSelect) {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)) {
          stmt.execute(aListSqlBeforeSelect);
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      dbStopWatch.stop();

      for (int i = 0; i < listSqlLastSelect.size(); i++) {
        boolean append = !isNewFile || i != 0;
        boolean printHeader = !append;
        try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
             FileWriter fstream = new FileWriter(new File(fileName), append);
             BufferedWriter out = new BufferedWriter(fstream);) {

          //TODO investigate implications of adding the Fetch Size
          //stmt.setFetchSize(Integer.MIN_VALUE);
          dbStopWatch.start();
          ResultSet resultSetLast = stmt.executeQuery(listSqlLastSelect.get(i));
          dbStopWatch.stop();
          saveToFileStopWatch.start();
          printFormattedTable(resultSetLast, out, printHeader);
          saveToFileStopWatch.stop();
          out.flush();
          resultSetLast.close();
          mvResponse.setSuccess(true);

        } catch (SQLException | IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
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
            logger.info(stat + " statistic can only be plotted as an aggregation of lines");

            //rethrow the exception to be printed as a error popup on UI
            throw new ValidationException(
                    stat + " statistic can only be plotted as an aggregation of lines");
          }

        }
      }
      con.close();

    } catch (SQLException | StopWatchException e) {
      logger.error(ERROR_MARKER, e.getMessage());
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
      logger.error(ERROR_MARKER, e.getMessage());
    }

    mvResponse.setInfoMessage(message);

    return mvResponse;
  }

  /**
   * Prints a textual representation of the input {@link ResultSet} with the field names in the
   * first row to the specified {@link BufferedWriter} destination.
   *
   * @param res            The ResultSet to print
   * @param bufferedWriter The stream to write the formatted results to (defaults to printStream)
   */

  private void printFormattedTable(ResultSet res, BufferedWriter bufferedWriter, boolean isHeader) {

    char delim = '\t';
    try {
      ResultSetMetaData met = res.getMetaData();
      int fcstLeadIndex = -1;
      int fcstObsIndex = -1;

      //  print out the column headers

      for (int i = 1; i <= met.getColumnCount(); i++) {
        String label = met.getColumnLabel(i);
        if (label.equals("fcst_lead")) {
          fcstLeadIndex = i;
        } else if (label.equals("obs_lead")) {
          fcstObsIndex = i;
        }
        if (isHeader) {
          if (1 == i) {
            bufferedWriter.write(label);
          } else {
            bufferedWriter.write(delim + label);
          }
        }
      }
      if (isHeader) {
        bufferedWriter.write(MVUtil.LINE_SEPARATOR);
      }

      //  print out the table of values
      int intLine = 0;

      while (res.next()) {
        StringBuilder line = new StringBuilder();
        for (int i = 1; i <= met.getColumnCount(); i++) {
          String strVal;
          String objectType = met.getColumnTypeName(i);


          if (objectType.equals("DATETIME")) {
            LocalDateTime ts = res.getTimestamp(i).toLocalDateTime();
            strVal = DATE_FORMATTER.format(ts);
          } else {
            strVal = res.getString(i);

            //do not replace values for lead times
            if ((i != fcstLeadIndex && i != fcstObsIndex)
                    &&
                    (strVal == null || strVal.equalsIgnoreCase("null")
                            || strVal.equalsIgnoreCase("-9999"))) {
              strVal = "NA";
            }

          }


          if (1 == i) {
            line.append(strVal);
          } else {
            line.append(delim).append(strVal);
          }

        }
        bufferedWriter.write(line.toString());
        bufferedWriter.write(MVUtil.LINE_SEPARATOR);
        intLine++;

      }

      if (0 == intLine) {
        logger.info("No data was returned from database");
        throw new EmptyResultSetException("result set contained no data");
      }

    } catch (EmptyResultSetException | SQLException | IOException e) {
      logger.error(ERROR_MARKER,
              "  **  ERROR: Caught " + e.getClass()
                      + " in printFormattedTable(ResultSet res): " + e.getMessage());
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

    } catch (SQLException e) {
      logger.error(ERROR_MARKER, e.getMessage());
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

    } catch (SQLException e) {
      logger.error(ERROR_MARKER, e.getMessage());
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
    List<String> headerSql;
    if (job.isModeJob()) {
      headerSql = modeHeaderSqlType;
    } else if (job.isMtdJob()) {
      headerSql = mtdHeaderSqlType;
    } else {
      headerSql = statHeaderSqlType;
    }

    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String plotFixWhere = buildPlotFixWhere(listPlotFixVal, job,
            job.isModeJob() || job.isMtdJob());

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && !job.getPlotCond().isEmpty()) {
      if (!plotFixWhere.isEmpty()) {
        plotFixWhere += "  AND ";
      }
      plotFixWhere += job.getPlotCond() + "\n";
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
    if (job.getPlotTmpl().equals("performance.R_tmpl")) {
      //check if fcst_var value in mapDep was changed by the template
      if (job.getPlotFixVal().containsKey("fcst_var")) {
        MVOrderedMap mapFarPody = new MVOrderedMap();
        Object objFcstVar = job.getPlotFixVal().get("fcst_var");
        String[] listFcstVar;
        if (objFcstVar instanceof String[]) {
          listFcstVar = (String[]) job.getPlotFixVal().get("fcst_var");
        } else if (objFcstVar instanceof String) {
          listFcstVar = new String[]{(String) objFcstVar};
        } else {
          MVOrderedMap mapFcstVar = (MVOrderedMap) job.getPlotFixVal().get("fcst_var");
          listFcstVar = (String[]) mapFcstVar.get(mapFcstVar.getKeyList()[0]);
        }
        if (job.getAggNbrCtc()) {
          mapFarPody.put(listFcstVar[0], new String[]{"NBR_FAR", "NBR_PODY"});
        } else {
          mapFarPody.put(listFcstVar[0], new String[]{"FAR", "PODY"});
        }
        for (MVOrderedMap map : listDep) {
          map.put("dep1", mapFarPody);
        }
      }
    }

    List<String> listSql = new ArrayList<>();
    String strSelectSql = "";
    for (
            int intY = 1;
            intY <= 2; intY++) {

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

      String whereClause = plotFixWhere;
      BuildMysqlQueryStrings buildMysqlQueryStrings = build(job.isModeJob() || job.isMtdJob(),
              headerSql,
              listSeries, whereClause, true);
      String selectList = buildMysqlQueryStrings.getSelectList();
      whereClause = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields are not present
      // in the select list and temp table list, add them
      if (!selectList.contains("fcst_init")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectList += ",\n  h.fcst_init";
        } else {
          if (selectList.length() > 0) {
            selectList += ",\n ";
          }
          selectList += " ld.fcst_init_beg";

        }
      }
      if (!selectList.contains("fcst_valid")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectList += ",\n  h.fcst_valid";
        } else {
          if (selectList.length() > 0) {
            selectList += ",\n ";
          }
          selectList += " ld.fcst_valid_beg";

        }
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
              headerSql,
              listSeries, whereClause, false);
      String selectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields are not present
      // in the select list and temp table list, add them
      if (!selectPlotList.contains("fcst_init") && !selectPlotList.contains("init_hour")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectPlotList += ",\n  h.fcst_init";
        } else {
          selectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!selectPlotList.contains("fcst_valid")) {
        if (job.isModeJob() || job.isMtdJob()) {
          selectPlotList += ",\n  h.fcst_valid";
        } else {
          selectPlotList += ",\n " + " ld.fcst_valid_beg";
        }
      }

      if (!selectList.contains("fcst_lead")) {
        if (!boolEnsSs)
          if (job.isModeJob() || job.isMtdJob()) {

            if (job.getEventEqual()) {
              selectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                      + "WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead "
                      + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                      + "WHERE model = h.model) ) fcst_lead";
            } else {
              selectList += ",\n  h.fcst_lead";
            }
            selectPlotList += ",\n  h.fcst_lead";

          } else {
            if (job.getEventEqual()) {
              selectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                      + "WHERE model = h.model) is NULL , ld.fcst_lead , ld.fcst_lead "
                      + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                      + "WHERE model = h.model) ) fcst_lead";
            } else {
              selectList += ",\n " + " ld.fcst_lead";
            }
            selectPlotList += ",\n  h.fcst_lead";

          }
        else {
          selectPlotList += ",\n  ld.fcst_lead";
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
        if (listPlotFixVal.length > 0) {
          for (int i = 0; i < listPlotFixVal.length; i++) {
            String strField = (String) listPlotFixVal[i].getKey();
            if (!strField.equals("fcst_var") && !selectPlotList.contains(strField) && listPlotFixVal[i].getValue() != null) {
              selectPlotList += ",\n" + strField;

            }
          }
        }
        listSql.add("SELECT\n"
                + selectPlotList + ",\n  h.fcst_var,\n"
                + "  ld.total,\n  ld.bin_n,\n  ld.var_min,\n  ld.var_max,\n  ld.var_mean,\n"
                + "  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar "
                + "FROM\n"
                + "  stat_header h,\n"
                + "  line_data_ssvar ld\n"
                + "WHERE\n" + whereClause
                + "  AND h.stat_header_id = ld.stat_header_id;\n");

        return listSql;
      }


      //  validate and get the type and values for the independent variable
      String indyVar = job.getIndyVar();
      String indyVarFormatted = "";
      if (!indyVar.isEmpty()) {
        String[] listIndyVal = job.getIndyVal();
        if (!headerSql.contains(indyVar)) {
          throw new ValidationException("unrecognized indep "
                  + (job.isModeJob() ? "mode" : "stat")
                  + "_header field: " + indyVar);
        }
        if (1 > listIndyVal.length) {
          throw new ValidationException("no independent variable values specified");
        }

        //  construct the select list item, where clause
        // and temp table entry for the independent variable
        if (!selectList.contains(indyVar)) {
          selectList += ",\n  " + formatField(indyVar, job.isModeJob() || job.isMtdJob(),
                  true);
          selectPlotList += ",\n  " + formatField(indyVar, job.isModeJob() || job.isMtdJob(),
                  true);
        }
        indyVarFormatted = formatField(indyVar, job.isModeJob() || job.isMtdJob(), false);
        if (indyVar.equals("fcst_lead") && job.getEventEqual()) {
          indyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + "WHERE model = h.model) is NULL , "
                  + indyVarFormatted + " , " + indyVarFormatted
                  + " + (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + "WHERE model = h.model) ) ";
        }
        String field;
        if (indyVarFormatted.startsWith("HOUR(")) {
          field = indyVarFormatted;
        } else {
          field = BINARY + indyVarFormatted;
        }
        whereClause += (!whereClause.isEmpty() ? "  AND " : "") + field
                + " IN (" + MVUtil.buildValueList(job.getIndyVal()) + ")\n";
      }
      //  add fcst_var to the select list and temp table entries
      //selectList += ",\n  h.fcst_var";
      selectPlotList += ",\n  h.fcst_var";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          String fieldFormatted = formatField(strField, job.isModeJob() || job.isMtdJob(), true);
          if (listPlotFixVal[i].getValue() != null) {
            if (!selectList.contains(fieldFormatted.trim())) {
              selectList += ",\n  "
                      + formatField(strField, job.isModeJob() || job.isMtdJob(), true);
            }
            if (!selectPlotList.contains(fieldFormatted.trim())) {
              selectPlotList += ",\n  "
                      + formatField(strField, job.isModeJob() || job.isMtdJob(), true);
            }

          }
        }
      }

      if (job.getAggRps()) {
        /*
         *  For agg_stat RPS plots, retrieve N_PROB is constant for each series
         */
        MVOrderedMap[] series = MVUtil.permute(job.getSeries1Val().convertFromSeriesMap())
                .getRows();
        MVOrderedMap[] forecastVars = MVUtil.permute((MVOrderedMap) job.getDepGroups()[0].get("dep" + intY))
                .getRows();
        for (int forecastVarsInd = 0; forecastVarsInd < forecastVars.length; forecastVarsInd++) {
          MVOrderedMap stats = forecastVars[forecastVarsInd];
          String[] vars = stats.getKeyList();
          for (int varsInd = 0; varsInd < vars.length; varsInd++) {
            for (int seriesInd = 0; seriesInd < series.length; seriesInd++) {
              MVOrderedMap ser = series[seriesInd];
              String[] serName = ser.getKeyList();
              for (int serNameInd = 0; serNameInd < serName.length; serNameInd++) {
                String selRpsProb = "SELECT DISTINCT ld.n_prob\nFROM\n  "
                        + "stat_header h,\n  line_data_rps ld\n";
                selRpsProb = selRpsProb + "WHERE\n";
                if (indyVarFormatted.length() > 0 && job.getIndyVal().length > 0) {
                  selRpsProb = selRpsProb + BINARY + indyVarFormatted
                          + " IN (" + MVUtil.buildValueList(
                          job.getIndyVal()) + ")\n  AND ";
                  selRpsProb = selRpsProb + BINARY + serName[serNameInd]
                          + " = '" + ser.getStr(serName[serNameInd]) + "'";
                  if (!vars[varsInd].equals("NA")) {
                    selRpsProb = selRpsProb + " AND" + BINARY + " fcst_var='" + vars[varsInd] + "' ";
                  }
                  if (plotFixWhere.length() > 0) {
                    selRpsProb = selRpsProb + "  AND  " + plotFixWhere;
                  }
                  selRpsProb = selRpsProb + "  AND ld.stat_header_id = h.stat_header_id;";
                  printStreamSql.println(selRpsProb + "\n");
                  printStreamSql.flush();
                  //  run the PCT thresh query
                  List<String> errors = new ArrayList<>();
                  for (int i = 0; i < job.getCurrentDBName().size(); i++) {
                    Map<String, Integer> rpsProbInfo = getPctThreshInfo(selRpsProb, job.getCurrentDBName().get(i));
                    if (1 != rpsProbInfo.get("numPctThresh")) {
                      String error = "number of RPS PROBs (" + rpsProbInfo.get(
                              "numPctThresh") + ") not distinct for " + serName[serNameInd]
                              + " = '" + ser.getStr(serName[serNameInd])
                              + "' AND database  " + job.getCurrentDBName().get(i) + "'";
                      if (!vars[varsInd].equals("NA")) {
                        error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                      }
                      errors.add(error);
                    } else if (1 > rpsProbInfo.get("numPctThresh")) {
                      String error = "invalid number of RPS PROBs ("
                              + rpsProbInfo.get("numPctThresh") + ") found for "
                              + serName[serNameInd] + " = '"
                              + ser.getStr(serName[serNameInd])
                              + "' AND database " + job.getCurrentDBName().get(i) + "'";
                      if (!vars[varsInd].equals("NA")) {
                        error = error + "' AND fcst_var='" + vars[varsInd] + "'";
                      }
                      errors.add(error);
                    } else {
                      errors.add(null);
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
                }
              }
            }
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
                String selPctThresh = "SELECT DISTINCT ld.n_thresh\nFROM\n  "
                        + "stat_header h,\n  line_data_pct ld\n";
                selPctThresh = selPctThresh + "WHERE\n";
                if (indyVarFormatted.length() > 0 && job.getIndyVal().length > 0) {
                  selPctThresh = selPctThresh + BINARY + indyVarFormatted
                          + " IN (" + MVUtil.buildValueList(
                          job.getIndyVal()) + ")\n  AND ";
                }
                selPctThresh = selPctThresh + BINARY + serName[serNameInd]
                        + " = '" + ser.getStr(serName[serNameInd]) + "'";
                if (!vars[varsInd].equals("NA")) {
                  selPctThresh = selPctThresh + " AND" + BINARY + " fcst_var='" + vars[varsInd] + "' ";
                }
                if (plotFixWhere.length() > 0) {
                  selPctThresh = selPctThresh + "  AND  " + plotFixWhere;
                }
                selPctThresh = selPctThresh + "  AND ld.stat_header_id = h.stat_header_id;";

                printStreamSql.println(selPctThresh + "\n");
                printStreamSql.flush();


                //  run the PCT thresh query
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < job.getCurrentDBName().size(); i++) {
                  pctThreshInfo = getPctThreshInfo(selPctThresh, job.getCurrentDBName().get(i));
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
        String fcstVarClause = "";
        String strStat = "";
        String strFcstVar = "";
        if (listFcstVarStat.length > 0) {
          //  get the current fcst_var/stat pair
          strFcstVar = listFcstVarStat[intFcstVarStat][0];
          strStat = listFcstVarStat[intFcstVarStat][1];

          //  build the fcst_var where clause criteria
          fcstVarClause = "= '" + strFcstVar + "'";
          Matcher matProb = MVUtil.prob.matcher(strFcstVar);
          if (matProb.matches() && strFcstVar.contains("*")) {
            Pattern patFcstVar = Pattern.compile(
                    strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
            if (!_mapFcstVarPat.containsKey(patFcstVar)) {
              _mapFcstVarPat.put(patFcstVar, MVUtil.replaceSpecialChars(strFcstVar));
            }
            fcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
          }
        } else {
          if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
            strStat = "ECLV";
            if (mapPlotFixVal.get("fcst_var") instanceof MVOrderedMap) {
              MVOrderedMap m = (MVOrderedMap) mapPlotFixVal.get("fcst_var");
              strFcstVar = ((String[]) m.get(m.getKeyList()[0]))[0];
            }
          }
        }
        if (!selectList.contains("fcst_var")) {
          selectList += ",\n'" + strFcstVar + "' fcst_var";
        } else if (intFcstVarStat > 0 && selectList.contains(listFcstVarStat[intFcstVarStat - 1][0] + "' fcst_var")) {
          selectList = selectList.replace(listFcstVarStat[intFcstVarStat - 1][0] + "' fcst_var"
                  , strFcstVar + "' fcst_var");
        } else {
          selectList = selectList.replace("fcst_var"
                  , "'" + strFcstVar + "' fcst_var");
        }

        //  determine the table containing the current stat
        Map tableStats;
        String statTable = "";
        String statField = strStat.toLowerCase(Locale.US);
        if (job.isModeJob()) {
          String statMode = MVUtil.parseModeStat(strStat)[0];
          if (MVUtil.modeSingleStatField.containsKey(statMode)) {
            tableStats = MVUtil.modeSingleStatField;
          } else if (MVUtil.modePairStatField.containsKey(statMode)) {
            tableStats = MVUtil.modeSingleStatField;
          } else if (MVUtil.modeRatioField.contains(strStat)) {
            tableStats = MVUtil.modeSingleStatField;
          } else {
            throw new ValidationException("unrecognized mode stat: " + statMode);
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
          } else if (job.getAggNbrCtc()) {
            aggType = MVUtil.NBRCTC;
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
          } else if (job.getAggRps()) {
            aggType = MVUtil.RPS;
          }


          if (MVUtil.statsCnt.containsKey(strStat)) {
            tableStats = MVUtil.statsCnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsCnt, strStat, aggType);
              statTable = "line_data_" + aggType + " ld\n";
            } else {
              statTable = "line_data_cnt" + " ld\n";
            }
          } else if (MVUtil.statsSsvar.containsKey(strStat)) {
            tableStats = MVUtil.statsSsvar;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsSsvar, strStat, aggType);
              statTable = "line_data_" + aggType + " ld\n";
            } else {
              statTable = "line_data_ssvar" + " ld\n";
              statField = strStat.replace("SSVAR_", "").toLowerCase();
            }
          } else if (MVUtil.statsCts.containsKey(strStat)) {
            tableStats = MVUtil.statsCts;
            if (aggType != null && aggType.equals(MVUtil.CTC)) {
              MVUtil.isAggTypeValid(MVUtil.statsCts, strStat, aggType);
              statTable = "line_data_ctc" + " ld\n";
            } else {
              statTable = "line_data_cts" + " ld\n";
            }
          } else if (MVUtil.statsNbrcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsNbrcnt, strStat, aggType);
            }
            statTable = "line_data_nbrcnt ld\n";
            statField = strStat.replace("NBR_", "").replace("CNT_", "").toLowerCase();
          } else if (MVUtil.statsEnscnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEnscnt;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsEnscnt, strStat, aggType);
            }
            statTable = "line_data_enscnt ld\n";
            statField = strStat.replace("ENS_", "").toLowerCase();
          } else if (MVUtil.statsNbrcts.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrcts;
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsNbrcts, strStat, aggType);
              statTable = "line_data_nbrctc" + " ld\n";
            } else {
              statTable = "line_data_nbrcts" + " ld\n";
            }
            statField = strStat.replace("NBR_", "").toLowerCase();
          } else if (MVUtil.statsPstd.containsKey(strStat)) {
            tableStats = MVUtil.statsPstd;
            statTable = "line_data_pstd ld\n";
            if (aggType != null && !strStat.endsWith("_TOTAL")) {
              statTable = "line_data_pct ld";
              MVUtil.isAggTypeValid(MVUtil.statsPstd, strStat, aggType);
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                statTable += ",\n  line_data_pct_thresh ldt" + i;
              }
              statTable += "\n";
            }
            statField = strStat.replace("PSTD_", "").toLowerCase();
          } else if (MVUtil.statsMcts.containsKey(strStat)) {
            tableStats = MVUtil.statsMcts;
            MVUtil.isAggTypeValid(MVUtil.statsMcts, strStat, aggType);
            statTable = "line_data_mcts ld\n";
            statField = strStat.replace("MCTS_", "").toLowerCase();
          } else if (MVUtil.statsRhist.containsKey(strStat)) {
            tableStats = MVUtil.statsRhist;
            statTable = "line_data_rhist ld\n";
            statField = strStat.replace("RHIST_", "").toLowerCase();
          } else if (MVUtil.statsPhist.containsKey(strStat)) {
            tableStats = MVUtil.statsPhist;
            MVUtil.isAggTypeValid(MVUtil.statsPhist, strStat, aggType);
            statTable = "line_data_phist ld\n";
            statField = strStat.replace("PHIST_", "").toLowerCase();
          } else if (MVUtil.statsVl1l2.containsKey(strStat)) {
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVl1l2, strStat, aggType);
            }
            tableStats = MVUtil.statsVl1l2;
            statTable = "line_data_vl1l2 ld\n";
            statField = strStat.replace("VL1L2_", "").toLowerCase();
          } else if (MVUtil.statsVal1l2.containsKey(strStat)) {
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVal1l2, strStat, aggType);
            }
            tableStats = MVUtil.statsVal1l2;
            statTable = "line_data_val1l2 ld\n";
            statField = strStat.replace("VAL1L2_", "").toLowerCase();
          } else if (MVUtil.statsMpr.containsKey(strStat)) {
            tableStats = MVUtil.statsMpr;
            statTable = "line_data_mpr ld\n";
          } else if (MVUtil.statsDmap.containsKey(strStat)) {
            tableStats = MVUtil.statsDmap;
            statTable = "line_data_dmap ld\n";
            statField = strStat.replace("DMAP_", "").toLowerCase();
          } else if (MVUtil.statsRps.containsKey(strStat)) {
            tableStats = MVUtil.statsRps;
            statTable = "line_data_rps ld\n";
            statField = strStat.replace("RPS_", "").toLowerCase();
          } else if (MVUtil.statsCtc.containsKey(strStat)) {
            tableStats = MVUtil.statsCtc;
            statTable = "line_data_ctc ld\n";
            statField = strStat.replace("CTC_", "").toLowerCase();
          } else if (MVUtil.statsNbrctc.containsKey(strStat)) {
            tableStats = MVUtil.statsNbrctc;
            statTable = "line_data_nbrctc ld\n";
            statField = strStat.replace("NBR_CTC_", "").toLowerCase();
          } else if (MVUtil.statsPct.containsKey(strStat)) {
            tableStats = MVUtil.statsPct;
            statTable = "line_data_pct ld\n";
            statField = strStat.replace("PCT_", "").toLowerCase();
          } else if (MVUtil.statsOrank.containsKey(strStat)) {
            tableStats = MVUtil.statsOrank;
            statTable = "line_data_orank ld\n";
            statField = strStat.replace("ORANK_", "").toLowerCase();
          } else if (MVUtil.statsSl1l2.containsKey(strStat)) {
            tableStats = MVUtil.statsSl1l2;
            statTable = "line_data_sl1l2 ld\n";
            statField = strStat.replace("SL1L2_", "").toLowerCase();
          } else if (MVUtil.statsSal1l2.containsKey(strStat)) {
            tableStats = MVUtil.statsSal1l2;
            statTable = "line_data_sal1l2 ld\n";
            statField = strStat.replace("SAL1L2_", "").toLowerCase();
          } else if (MVUtil.statsGrad.containsKey(strStat)) {
            tableStats = MVUtil.statsGrad;
            statTable = "line_data_grad ld\n";
            statField = strStat.replace("GRAD_", "").toLowerCase();
          } else if (MVUtil.statsVcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsVcnt;
            statField = strStat.replace("VCNT_", "").toLowerCase();
            if (aggType != null) {
              MVUtil.isAggTypeValid(MVUtil.statsVcnt, strStat, aggType);
              statTable = "line_data_" + aggType + " ld\n";
            } else {
              statTable = "line_data_vcnt" + " ld\n";
            }
          } else if (strStat.equals("ECLV") && job.getPlotTmpl().equals("eclv.R_tmpl")) {
            if (aggType != null && aggType.equals(MVUtil.CTC)) {
              tableStats = MVUtil.statsCts;
              statTable = "line_data_ctc" + " ld\n";
            } else {
              tableStats = MVUtil.statsPstd;
              statTable = "line_data_pct ld,\n  line_data_pct_thresh ldt\n";
            }
          } else if (MVUtil.statsEcnt.containsKey(strStat)) {
            tableStats = MVUtil.statsEcnt;
            statTable = "line_data_ecnt ld\n";
            statField = strStat.replace("ECNT_", "").toLowerCase();
          } else if (MVUtil.statsPerc.containsKey(strStat)) {
            tableStats = MVUtil.statsPerc;
            statTable = "line_data_perc ld\n";
          } else {
            throw new ValidationException("unrecognized stat: " + strStat);
          }
        }

        //  build the SQL for the current fcst_var and stat
        if (job.isModeJob()) {

          //  build the mode SQL
          String whereFcstVar = whereClause + " AND" + BINARY + " fcst_var " + fcstVarClause;

          listSql.addAll(buildModeStatSql(selectList, whereFcstVar, strStat, listGroupBy,
                  job.getEventEqual(), listSeries));
        } else if (job.isMtdJob()) {

          //  build the mtd SQL
          String strWhereFcstVar = whereClause + " AND" + BINARY + " fcst_var " + fcstVarClause;
          listSql.addAll(buildMtdStatSql(selectList, strWhereFcstVar, strStat));
        } else {
          boolean boolBCRMSE = false;
          String selectStat = selectList;

          //  build the select list and temp table elements for the stat and CIs
          if (strStat.equals("BCRMSE")) {
            boolBCRMSE = true;
            statField = "bcmse";
          }
          selectStat += ",\n  '" + strStat + "' stat_name";

          //  add the appropriate stat table members, depending
          // on the use of aggregation and stat calculation
          if (job.getAggCtc() || job.getAggNbrCtc()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fy_oy,\n  ld.fy_on,\n  "
                    + "ld.fn_oy,\n  ld.fn_on";
          } else if (job.getAggSl1l2()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  "
                    + "ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n ld.mae";
          } else if (job.getAggGrad()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fgbar,\n  ld.ogbar,\n  "
                    + "ld.mgbar,\n  ld.egbar,\n  ld.s1,\n ld.s1_og, \n ld.fgog_ratio";
          } else if (job.getAggSsvar()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  "
                    + "ld.fobar,\n  ld.ffbar,\n  ld.oobar,\n  "
                    + "ld.var_mean, \n  ld.bin_n";
          } else if (job.getAggSal1l2()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fabar,\n  ld.oabar,\n  "
                    + "ld.foabar,\n  ld.ffabar,\n  ld.ooabar,\n ld.mae";

          } else if (job.getAggEcnt()) {

            selectStat += ",\n  0 stat_value,"
                    + "\n ld.total,\n  ld.me, \n  ld.rmse,\n  ld.crps,\n  ld.crpss,"
                    + "\n  ld.ign,  "
                    + "\n ld.spread,\n  ld.me_oerr,\n  ld.rmse_oerr,"
                    + " \n  ld.spread_oerr,"
                    + "\n ld.spread_plus_oerr";
          } else if (job.getAggPct()) {

            if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
              selectStat += ",\n  0 stat_value,\n  ld.n_thresh,\n ldt.thresh_i,\n ldt.oy_i\n,"
                      + " ldt.on_i";
            } else if (strStat.endsWith("_TOTAL")) {
              selectStat += ",\n  0 stat_value,\n  ld.total ";
            } else {
              selectStat += ",\n  0 stat_value,\n  ld.total,\n  (ld.n_thresh - 1)";
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                selectStat += ",\n";
                if (i < pctThreshInfo.get("pctThresh") - 1) {
                  selectStat += "  FORMAT((ldt" + i + ".thresh_i + ldt"
                          + (i + 1) + ".thresh_i)/2, 3),\n";
                } else {
                  selectStat += "  FORMAT((ldt" + i + ".thresh_i + 1)/2, 3),\n";
                }
                selectStat += "  ldt" + i + ".oy_i,\n"
                        + "  ldt" + i + ".on_i";
              }
            }
          } else if (job.getAggNbrCnt()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbs,\n  ld.fss, ld.afss, ld.ufss, ld.f_rate, ld.o_rate ";
          } else if (job.getAggRps()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.rps,\n  ld.rpss ";
          } else if (job.getAggVl1l2()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufbar,\n ld.vfbar,\n ld.uobar,"
                    + "\n ld.vobar,\n ld.uvfobar,\n ld.uvffbar,\n ld.uvoobar,"
                    + " \n ld.f_speed_bar, \n ld.o_speed_bar";
          } else if (job.getAggVal1l2()) {
            selectStat += ",\n  0 stat_value,\n  ld.total,\n ld.ufabar,\n ld.vfabar,\n "
                    + "ld.uoabar,\n ld.voabar,\n ld.uvfoabar,\n ld.uvffabar,\n"
                    + " ld.uvooabar";
          } else if (job.getCalcCtc()) {
            selectStat += ",\n  ld.total, ld.fy_oy, ld.fy_on, ld.fn_oy, ld.fn_on, "
                    + "'NA' stat_value,\n"
                    + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  "
                    + "'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (job.getCalcSl1l2()) {
            if (strStat.equalsIgnoreCase("mae")) {
              selectStat += ",\n   ld.mae,  'NA' stat_value,\n"
                      + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n "
                      + "'NA' stat_bcu";
            } else {
              selectStat += ",\n  ld.total, ld.fbar, ld.obar, ld.fobar, ld.ffbar, ld.oobar,"
                      + " 'NA' stat_value,\n"
                      + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  "
                      + "'NA' stat_bcu";
            }
          } else if (job.getCalcGrad()) {
            selectStat += ",\n  ld.total, ld.fgbar, ld.ogbar, ld.mgbar, ld.egbar,  "
                    + "'NA' stat_value,\n"
                    + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  "
                    + "'NA' stat_bcu";
          } else if (job.getCalcSal1l2()) {
            selectStat += ",\n  ld.total, ld.fabar, ld.oabar, ld.foabar, ld.ffabar, "
                    + "ld.ooabar,  'NA' stat_value,\n"
                    + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  "
                    + "'NA' stat_bcu";
          } else if (job.getCalcVl1l2()) {
            selectStat += ",\n  ld.total, ld.ufbar, ld.vfbar, ld.uobar, ld.vobar, "
                    + "ld.uvfobar, ld.uvffbar, ld.uvoobar,"
                    + " ld.f_speed_bar, ld.o_speed_bar, 'NA' stat_value,\n"
                    + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  "
                    + "'NA' stat_bcu";

          } else if (job.getCalcVal1l2()) {
            selectStat += ",\n  ld.total, ld.ufabar, ld.vfabar, ld.uoabar, ld.voabar, "
                    + "ld.uvfoabar, ld.uvffabar, ld.uvooabar,"
                    + " 'NA' stat_value,\n"
                    + "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  "
                    + "'NA' stat_bcu";
          } else {
            if (boolBCRMSE) {
              selectStat += ",\n  IF(ld." + statField + "=-9999,'NA',CAST(sqrt(ld."
                      + statField + ") as DECIMAL(30, 5))) stat_value";

            } else {
              selectStat += ",\n  IF(ld." + statField + "=-9999,'NA',ld."
                      + statField + " ) stat_value";

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
              selectStat += ",\n  IF(ld." + statField + "_ncl=-9999,'NA',ld."
                      + statField + "_ncl  ) stat_ncl"
                      + ",\n  IF(ld." + statField + "_ncu=-9999,'NA',ld."
                      + statField + "_ncu  ) stat_ncu";
            } else {
              selectStat += ",\n  'NA' stat_ncl,\n  'NA' stat_ncu";
            }

            if (boolHasBoot && !boolAggStat) {
              if (boolBCRMSE) {
                selectStat += ",\n  IF(ld." + statField + "_bcl=-9999,'NA',CAST(sqrt(ld."
                        + statField + "_bcl) as DECIMAL(30, 5))) stat_bcl"
                        + ",\n  IF(ld." + statField + "_bcu=-9999,'NA',CAST(sqrt(ld."
                        + statField + "_bcu) as DECIMAL(30, 5))) stat_bcu";
              } else {
                selectStat += ",\n  IF(ld." + statField + "_bcl=-9999,'NA',ld."
                        + statField + "_bcl) stat_bcl"
                        + ",\n  IF(ld." + statField + "_bcu=-9999,'NA',ld."
                        + statField + "_bcu ) stat_bcu";
              }
            } else {
              selectStat += ",\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            }
          }

          String strStatNaClause = "";
          if (!boolAggStat && !boolCalcStat) {
            strStatNaClause = "\n  AND ld." + statField + " != -9999";
          }
          if (job.getAggPct()) {
            if (job.getPlotTmpl().equals("eclv.R_tmpl")) {
              strStatNaClause = "\n  AND ld.line_data_id = ldt.line_data_id\n";

            } else if (!strStat.endsWith("_TOTAL")) {
              for (int i = 1; i < pctThreshInfo.get("pctThresh"); i++) {
                strStatNaClause += "\n  AND ld.line_data_id = ldt" + i + ".line_data_id\n"
                        + "  AND ldt" + i + ".i_value = " + i;
              }
            }
          }

          //  build the query
          strSelectSql += (strSelectSql.isEmpty() ? "" : "\nUNION ALL\n")
                  + "SELECT\n" + selectStat + "\n"
                  + "FROM\n  stat_header h,\n  " + statTable;
          strSelectSql += "WHERE\n" + whereClause;
          if (fcstVarClause.length() > 0) {
            strSelectSql += "  AND " + BINARY + "h.fcst_var " + fcstVarClause + "\n";
          }
          strSelectSql += "  AND ld.stat_header_id = h.stat_header_id" + strStatNaClause;
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
   * @param plotFixFields list of &lt;plot_fix&gt; field/value pairs
   * @param isModePlot    specifies MODE plot
   * @return generated SQL where clauses
   */

  private String buildPlotFixWhere(
          Map.Entry[] plotFixFields, MVPlotJob job,
          boolean isModePlot) {
    String whereClause = "";

    //  build the aggregate fields where clause
    for (int i = 0; i < plotFixFields.length; i++) {
      String strField = (String) plotFixFields[i].getKey();
      String condition = "";
      Object objValue = plotFixFields[i].getValue();
      if (objValue instanceof String[]) {
        condition = "IN (" + MVUtil.buildValueList((String[]) objValue) + ")";
      } else if (objValue instanceof MVOrderedMap) {
        MVOrderedMap mapTmpl = job.getTmplVal();
        String setName = mapTmpl.get(strField + "_set").toString();
        String[] listValues = (String[]) ((MVOrderedMap) objValue).get(setName);
        condition = "IN (" + MVUtil.buildValueList(listValues) + ")";

      } else if (objValue instanceof String) {
        if (objValue.toString().startsWith("BETWEEN")) {
          condition = objValue.toString();
        } else {
          condition = "IN ('" + objValue.toString() + "')";
        }
      }
      String indyVarFormatted = formatField(strField, isModePlot, false);
      if (strField.equals("fcst_lead") && job.getEventEqual()) {
        indyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                + "WHERE model = h.model) is NULL , "
                + indyVarFormatted + " , " + indyVarFormatted
                + " + (select fcst_lead_offset FROM model_fcst_lead_offset "
                + "WHERE model = h.model) ) ";
      }
      //add BINARY for all fields except HOUR(...)
      String field;
      if (indyVarFormatted.startsWith("HOUR(")) {
        field = indyVarFormatted;
      } else {
        field = BINARY + indyVarFormatted;
      }
      whereClause += (0 < i ? "  AND " : "  ") + field + " " + condition + "\n";
    }

    return whereClause;
  }

  /**
   * Use the input query components to build a list of select statements to gather plot data. This
   * function is a switchboard for the different types of MODE statistics: single, pair, derived,
   * difference and ratios.
   *
   * @param selectList  list of select fields
   * @param whereClause list of where clauses
   * @param stat        MODE stat
   * @param listGroupBy list of fields to group by
   * @param listSeries
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildModeStatSql(
          String selectList, String whereClause, String stat, String[] listGroupBy,
          boolean isEventEqualization, Map.Entry[] listSeries) {

    List<String> listQuery = new ArrayList<>();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatComp = MVUtil.parseModeStat(stat);
    if (listStatComp[0].equals("ACOV")) {
      whereClause = whereClause.replace("h.", "");
      listQuery.add(buildModeSingleAcovTable(selectList, whereClause, stat, listGroupBy,
              isEventEqualization));
    } else if (MVUtil.modeSingleStatField.containsKey(listStatComp[0])) {
      if (!listStatComp[1].startsWith("D")) {
        whereClause = whereClause.replace("h.", "");
        listQuery.add(buildModeSingleStatTable(selectList, whereClause, stat, listGroupBy,
                isEventEqualization));
      } else {
        String strWhereForQuery = whereClause.replace("h.", "");
        String newStat = stat.replace("_D", "_F");
        // selectList = selectList  + "  h.object_id,\n"+ "  h.object_cat";
        String query1 = buildModeSingleStatTable(selectList + ",  h.object_id",
                strWhereForQuery, newStat,
                listGroupBy,
                isEventEqualization);
        newStat = stat.replace("_D", "_O");
        String query2 = buildModeSingleStatTable(selectList + ",  h.object_id",
                strWhereForQuery, newStat,
                listGroupBy,
                isEventEqualization);

        listQuery
                .add(buildModeSingleStatDiffTable(selectList, stat, query1, query2,
                        listSeries));
      }
    } else if (MVUtil.modePairStatField.containsKey(listStatComp[0])) {


      if (listStatComp[0].equals("MAXINT")) {
        String[] listMaxintQueries = {
                buildModePairStatTable(selectList, whereClause, "MAXINTF_" + listStatComp[1]),
                buildModePairStatTable(selectList, whereClause, "MAXINTO_" + listStatComp[1])
        };
        listMaxintQueries[0] = listMaxintQueries[0].replace("MAXINTF", "MAXINT");
        listMaxintQueries[1] = listMaxintQueries[1].replace("MAXINTO", "MAXINT");
        listQuery.addAll(Arrays.asList(listMaxintQueries));
      } else {
        listQuery.add(buildModePairStatTable(selectList, whereClause, stat));
      }
    } else if ((listStatComp[0].equals("RATIO")
            || listStatComp[0].equals("AREARAT")
            || stat.startsWith("OBJ"))) {
      listQuery.add(buildModeSingleStatRatioTable(selectList, whereClause));
    }
    return listQuery;
  }

  /**
   * Use the input query components to build a list of select statements to gather plot data. This
   * function is a switchboard for the different types of MODE statistics: single, pair, derived,
   * difference and ratios.
   *
   * @param strSelectList list of select fields
   * @param whereClause   list of where clauses
   * @param strStat       MTD stat
   * @return list of SQL queries for gathering plot data
   */
  private List<String> buildMtdStatSql(String strSelectList, String whereClause, String strStat) {

    List<String> listQuery = new ArrayList<>();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatParse = strStat.split("_");
    String stat = strStat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strStatFlag = listStatParse[listStatParse.length - 1];
    if (mtd3dSingleStatField.containsKey(stat)) {
      if (!strStatFlag.startsWith("D")) {
        whereClause = whereClause.replace("h.", "");
        listQuery.add(buildMtd3dSingleStatTable(strSelectList, whereClause, strStat));
      } else {
        String strWhereForQuery = whereClause.replace("h.", "");
        String newStat = strStat.replace("_D", "_F");
        String query1 = buildMtd3dSingleStatTable(strSelectList, strWhereForQuery, newStat);
        newStat = strStat.replace("_D", "_O");
        String query2 = buildMtd3dSingleStatTable(strSelectList, strWhereForQuery, newStat);
        whereClause = whereClause.replace("h.", "s.");
        listQuery
                .add(
                        buildMtd3dSingleStatDiffTable(strSelectList, whereClause, strStat, query1, query2));
      }
    } else if (MVUtil.mtd2dStatField.containsKey(stat)) {
      if (!strStatFlag.startsWith("D")) {
        whereClause = whereClause.replace("h.", "");
        listQuery.add(buildMtd2dStatTable(strSelectList, whereClause, strStat));
      } else {
        String strWhereForQuery = whereClause.replace("h.", "");
        String newStat = strStat.replace("_D", "_F");
        String query1 = buildMtd2dStatTable(strSelectList, strWhereForQuery, newStat);
        newStat = strStat.replace("_D", "_O");
        String query2 = buildMtd2dStatTable(strSelectList, strWhereForQuery, newStat);
        whereClause = whereClause.replace("h.", "s.");
        listQuery.add(buildMtd2dStatDiffTable(strSelectList, whereClause, strStat, query1, query2));
      }
    } else if (MVUtil.mtd3dPairStatField.containsKey(stat)) {
      whereClause = whereClause.replace("h.", "");
      listQuery.add(buildMtd3dPairStatTable(strSelectList, whereClause, strStat));
    } else {
      whereClause = whereClause.replace("h.", "");
      if (stat.startsWith("2d")) {
        listQuery.add(buildMtdSingleStatRatio2dTable(strSelectList, whereClause));
      } else if (stat.startsWith("3d")) {
        listQuery.add(buildMtdSingleStatRatio3dTable(strSelectList, whereClause));
      }
    }
    return listQuery;
  }

  private String buildModeSingleAcovTable(
          String selectList, String whereClause, String stat,
          String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];
    String groupByClause = "\nGROUP BY\n";
    for (int i = 0; i < groups.length; i++) {
      groupByClause += (0 < i ? ",\n" : "") + "  " + groups[i];
    }

    //  build the query components
    String strStat = "SUM(area) / (2*total)";
    if (strStatFlag.charAt(0) != 'A') {
      strStat = "SUM(area) / total";
      groupByClause += " , fcst_flag";
      whereClause += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }


    if (!groupByClause.contains("fcst_valid")) {
      if (groups.length > 0) {
        groupByClause += "  ,";
      }
      groupByClause += " fcst_valid";

    }

    //mandatory group by fcst_valid and fcst_lead for EE
    if (isEventEqualization && !groupByClause.contains("fcst_lead")) {
      if (groups.length > 0) {
        groupByClause += "  ,";
      }
      groupByClause += " fcst_lead";
    }

    String selectListStat =
            selectList.replaceAll("h\\.", "")
                    .replaceAll(",\\s+$", "")
                    .replaceAll("fcst_init", "NOW() fcst_init");


    //  build the query
    return
            "SELECT\n" + selectListStat + ",\n"
                    + "'N/A'   object_id,\n"
                    + "'N/A'   object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + strStat + " stat_value\n"
                    + "FROM\n"
                    + "  mode_header ,\n"
                    + "  mode_obj_single ,\n"
                    + "  mode_cts\n"
                    + "WHERE\n" + whereClause + "\n"
                    + "  AND simple_flag = 1\n"
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n"
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id\n"
                    + "  AND " + BINARY + "mode_cts.field = 'OBJECT' "
                    + groupByClause + ", total;";
  }

  /**
   * Build SQL to gather mode pair data
   *
   * @param strSelectList
   * @param stat
   * @return
   */
  private String buildModePairStatTable(String strSelectList, String whereClause, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      whereClause += "\n  AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      whereClause += "\n  AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String selectListStat = strSelectList.replaceAll("h\\.", "");
    String groupListMMI = selectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");
    groupListMMI = groupListMMI.replaceAll("if\\D+fcst_lead", "fcst_lead");
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
      strGroupBy = "\nGROUP BY\n" + groupListMMI + ",\n  " + objectIdName;
      strGroupBy = strGroupBy.replaceAll(".+fcst_var", " fcst_var");
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String tableStat = MVUtil.modePairStatField.get(statName);
    whereClause = whereClause.replace("h.", "");
    //  build the query
    return
            "SELECT\n" + selectListStat + ",\n"
                    + "  " + objectId + ",\n"
                    + " 'NA' object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + tableStat + " stat_value\n"
                    + "FROM\n"
                    + "  mode_header,\n"
                    + "  mode_obj_pair\n"
                    + "WHERE\n" + whereClause
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
  private String buildMtd3dPairStatTable(String strSelectList, String whereClause, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String strStatFlag = listStatParse[listStatParse.length - 1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      whereClause += "\n  AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      whereClause += "\n  AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "");
    //  set the object_id field, depending on the stat
    String objectId = "object_id";
    String statName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = MVUtil.mtd3dPairStatField.get(statName);

    //  build the query
    return
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  " + objectId + ",\n"
                    + "  object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + strTableStat + " stat_value,\n"
                    + " '3d' object_type\n"
                    + "FROM mtd_header, mtd_3d_obj_pair \n"
                    + "WHERE\n" + whereClause
                    + " AND mtd_header.mtd_header_id = mtd_3d_obj_pair.mtd_header_id";
  }

  private String buildModeSingleStatTable(
          String selectList, String whereClause, String stat,
          String[] groups, boolean isEventEqualization) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }

    String strStatFlag = listStatParse[1];


    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      whereClause += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      whereClause += "\n  AND simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      whereClause += "\n  AND matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    String selectListStat = selectList.replaceAll("h\\.", "");


    //  build the group by clause
    String groupBy = "";
    String statName = listStatParse[0];
    if (statName.startsWith("CNT")) {
      selectListStat = selectListStat.replaceAll(",.+object_id", "");
      groupBy = "\nGROUP BY\n";
      for (int i = 0; i < groups.length; i++) {
        groupBy += (0 < i ? ",\n" : "") + "  " + groups[i];
      }
      if (!statName.equals("CNTSUM") && !groupBy.contains("fcst_valid")) {
        if (groups.length > 0) {
          groupBy += "  ,";
        }
        groupBy += " fcst_valid";

      } else {
        if (selectListStat.contains("fcst_valid")) {
          selectListStat = selectListStat.replace("fcst_valid,",
                  "NOW()  fcst_valid,");
        }
      }


      //mandatory group by fcst_valid and fcst_lead for EE
      if (isEventEqualization) {

        if (!groupBy.contains("fcst_lead")) {
          if (groups.length > 0) {
            groupBy += "  ,";
          }
          groupBy += " fcst_lead";
        }
        if (!selectListStat.contains("fcst_lead")) {
          selectListStat = selectListStat + ", fcst_lead\n";
        }
      } else {
        if (Arrays.binarySearch(groups, "fcst_lead") >= 0) {
          if (!groupBy.contains("fcst_lead")) {
            if (groups.length > 0) {
              groupBy += "  ,";
            }
            groupBy += " fcst_lead";
          }
        } else {
          selectListStat = selectListStat.replace("fcst_lead,",
                  "");
        }
      }
    }
    //remove fcst_init from the select list to create valid "GROUP BY"
    if (!groupBy.contains("fcst_init")) {
      selectListStat = selectListStat.replace("fcst_init,",
              "NOW()  fcst_init,");
    }

    //  build the query
    return
            "SELECT\n" + selectListStat + ",\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + MVUtil.modeSingleStatField.get(statName) + " stat_value\n"
                    + "FROM\n"
                    + "  mode_header ,\n"
                    + "  mode_obj_single ,\n"
                    + "  mode_cts \n"
                    + "WHERE\n" + whereClause
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n"
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id\n"
                    + "  AND mode_cts.field = 'OBJECT'"
                    + groupBy;
  }


  private String buildMtd3dSingleStatTable(
          String selectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = stat.split("_");
    String statFlag = listStatParse[listStatParse.length - 1];
    //  build the list of fields involved in the computations


    //  build the object flag where clause
    if (statFlag.charAt(0) != 'A') {
      strWhere += "\n  AND fcst_flag = " + ('F' == statFlag.charAt(0) ? "1" : "0");
    }
    if (statFlag.charAt(1) != 'A') {
      strWhere += "\n  AND simple_flag = " + ('S' == statFlag.charAt(1) ? "1" : "0");
    }
    if (statFlag.charAt(2) != 'A') {
      strWhere += "\n  AND matched_flag = " + ('M' == statFlag.charAt(2) ? "1" : "0");
    }
    String statName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String selectListStat = selectList.replaceAll("h\\.", "");


    //  build the query
    return
            "SELECT\n" + selectListStat + ",\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + mtd3dSingleStatField.get(statName) + " stat_value,\n"
                    + " '3d' object_type\n"
                    + "FROM mtd_header, mtd_3d_obj_single \n"
                    + "WHERE\n" + strWhere
                    + " AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";
  }


  private String buildMtd2dStatTable(
          String selectList, String strWhere, String stat) {


    String[] listStatParse = stat.split("_");
    String strStatFlag = listStatParse[listStatParse.length - 1];

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

    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");
    String strSelectListStat = selectList.replaceAll("h\\.", "");
    //  build the query
    return
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  revision_id,\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + MVUtil.mtd2dStatField.get(strStatName) + " stat_value,\n"
                    + " '2d' object_type\n"
                    + "FROM mtd_header, mtd_2d_obj \n"
                    + "WHERE\n" + strWhere
                    + " AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";
  }

  private String buildModeSingleStatRatioTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");
    strWhere = strWhere.replaceAll("h\\.", "");

    return
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  area,\n"
                    + "  total,\n"
                    + "  fcst_flag,\n"
                    + "  simple_flag,\n"
                    + "  matched_flag\n"
                    + "FROM\n"
                    + "  mode_header ,\n"
                    + "  mode_obj_single ,\n"
                    + "  mode_cts \n"
                    + "WHERE\n" + strWhere
                    + "  AND mode_obj_single.mode_header_id = mode_header.mode_header_id\n"
                    + "  AND mode_cts.mode_header_id = mode_obj_single.mode_header_id"
                    + "  AND mode_cts.field = 'OBJECT'";
  }

  private String buildMtdSingleStatRatio2dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  area,\n"
                    + "  fcst_flag,\n"
                    + "  simple_flag,\n"
                    + "  matched_flag,\n"
                    + " '2d' object_type\n"
                    + "FROM mtd_header, mtd_2d_obj\n"
                    + "WHERE\n" + strWhere
                    + "  AND mtd_header.mtd_header_id = mtd_2d_obj.mtd_header_id";

  }

  private String buildMtdSingleStatRatio3dTable(String selectList, String strWhere) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");


    return
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  volume,\n"
                    + "  fcst_flag,\n"
                    + "  simple_flag,\n"
                    + "  matched_flag,\n"
                    + " '3d' object_type\n"
                    + "FROM mtd_header, mtd_3d_obj_single\n"
                    + "WHERE\n" + strWhere
                    + "  AND mtd_header.mtd_header_id = mtd_3d_obj_single.mtd_header_id";

  }

  private String buildModeSingleStatDiffTable(
          String selectList, String stat,
          String table1, String table2,
          Map.Entry[] listSeries) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = MVUtil.parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String originalStatName = listStatParse[0];

    //  build the list of fields involved in the computations
    String selectListStat = selectList.replaceAll("h\\.", "s.")
            .replaceAll(".+fcst_var", " s.fcst_var");
    //make sure that all fields have table prefix
    String[] selectStatsArr = selectListStat.split(",");
    StringBuilder newSelectListStat = new StringBuilder();
    for (String selectStats : selectStatsArr) {
      selectStats = selectStats.trim();
      //add table prefix if it is missing
      if (!selectStats.startsWith("s.")) {
        newSelectListStat.append("s.");
      }
      newSelectListStat.append(selectStats).append(",");
    }
    //remove last comma
    newSelectListStat.setLength(newSelectListStat.length() - 1);
    selectListStat = newSelectListStat.toString();

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String tableStat = MVUtil.modeSingleStatField.get(originalStatName);
    String statName = tableStat.split("\\(")[0];
    String[] tableStats = new String[2];
    if (tableStat.contains("object_id")) {
      tableStats[0] = statName + "( s.object_id)";
      tableStats[1] = statName + "( s2.object_id)";
    } else {
      tableStats[0] = "s." + "stat_value";
      tableStats[1] = "s2." + "stat_value";

    }

    //  build the query COUNT(object_id)
    String result =
            "SELECT\n" + selectListStat + ",\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + tableStats[0] + " - " + tableStats[1] + " stat_value\n"
                    + "FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2\n"
                    + "WHERE\n"
                    + " s.fcst_var = s2.fcst_var\n";
    if (!table1.contains(MVUtil.COUNT)) {
      result += " AND" + BINARY + " SUBSTRING(s.object_id, -3) = SUBSTRING(s2.object_id,  -3) \n";
    }
    if (!tableStat.contains("object_id")) {
      result = result + "  AND " + "s.stat_value" + " != -9999"
              + " AND " + "s2.stat_value" + " != -9999"
              + " AND s.fcst_valid = s2.fcst_valid "
              + " AND s.fcst_lead = s2.fcst_lead";
      for (int i = 0; i < listSeries.length; i++) {
        result = result + " AND s." + listSeries[i].getKey() + " = s2." + listSeries[i].getKey();
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
    String[] listStatParse = stat.split("_");
    String strStatName = stat.replace("_" + listStatParse[listStatParse.length - 1], "");

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "\n  AND s.fcst_var = s2.fcst_var";


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
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  s.object_id,\n"
                    + "  s.object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n"
                    + "FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2,\n"
                    + " '3d' object_type\n"
                    + "WHERE\n"
                    + strWhere + "\n"
                    + "  AND " + BINARY + "SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) = SUBSTRING(s2"
                    + ".object_id,  "
                    + "LOCATE('_', s.object_id)+1)\n";
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
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  revision_id,\n"
                    + "  s.object_id,\n"
                    + "  s.object_cat,\n"
                    + "  '" + stat + "' stat_name,\n"
                    + "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value,\n"
                    + " '2d' object_type\n"
                    + "FROM ("
                    + table1
                    + " ) s, ( " + table2 + " ) s2\n"
                    + "WHERE\n"
                    + strWhere + "\n"
                    + "  AND " + BINARY + "SUBSTRING(s.object_id, LOCATE('_', s.object_id)+1) "
                    + "= SUBSTRING(s2.object_id,  LOCATE('_', s.object_id)+1) \n";
    if (!strTableStat.contains("object_id")) {
      result = result + "  AND " + strTableStats[0] + " != -9999 AND "
              + strTableStats[1] + " != -9999;";
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
    List<String> headerSql;
    if (job.isModeJob()) {
      headerSql = modeHeaderSqlType;
    } else {
      headerSql = statHeaderSqlType;
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
              headerSql,
              listSeries, strWhere, true);
      String selectList = buildMysqlQueryStrings.getSelectList();
      strWhere = buildMysqlQueryStrings.getWhere();

      //  if the fcst_valid or fcst_init fields
      // are not present in the select list and temp table list, add them
      if (!selectList.contains("fcst_init")) {
        selectList += ",\n  h.fcst_init";
      }
      if (!selectList.contains("fcst_valid")) {
        selectList += ",\n  h.fcst_valid";
      }
      BuildMysqlQueryStrings buildQueryPlotStrings = build(job.isModeJob() || job.isMtdJob(),
              headerSql,
              listSeries, strWhere, false);
      String selectPlotList = buildQueryPlotStrings.getSelectList();
      //  if the fcst_valid or fcst_init fields
      // are not present in the select list and temp table list, add them
      if (!selectPlotList.contains("fcst_init") && !selectPlotList.contains("init_hour")) {
        if (job.isModeJob()) {
          selectPlotList += ",\n  h.fcst_init";
        } else {
          selectPlotList += ",\n " + " ld.fcst_init_beg";
        }
      }
      if (!selectPlotList.contains("fcst_valid")) {
        if (job.isModeJob()) {
          selectPlotList += ",\n  h.fcst_valid";
        } else {
          selectPlotList += ",\n " + " ld.fcst_valid_beg";
        }
      }

      if (!selectList.contains("fcst_lead")) {
        if (job.getEventEqual()) {
          selectList += ",\n " + " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + "WHERE model = h.model) is NULL , h.fcst_lead , h.fcst_lead "
                  + "+ (select fcst_lead_offset FROM model_fcst_lead_offset "
                  + "WHERE model = h.model) ) fcst_lead";
        } else {
          selectList += ",\n " + " h.fcst_lead";
        }
        selectPlotList += ",\n " + " h.fcst_lead";
      }

      /*
       *  Construct the query components for the independent variable and values
       */

      //  validate and get the type and values for the independent variable
      String strIndyVar = job.getIndyVar();
      String[] listIndyVal = job.getIndyVal();
      if (!headerSql.contains(strIndyVar)) {
        throw new ValidationException("unrecognized indep " + (job.isModeJob() ? "mode" : "stat")
                + "_header field: " + strIndyVar);
      }
      if (1 > listIndyVal.length) {
        throw new ValidationException("no independent variable values specified");
      }

      //  construct the select list item, where clause
      // and temp table entry for the independent variable
      if (!selectList.contains(strIndyVar)) {
        selectList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(), true);
        selectPlotList += ",\n  " + formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
                true);
      }
      String strIndyVarFormatted = formatField(strIndyVar, job.isModeJob() || job.isMtdJob(),
              false);
      if (strIndyVar.equals("fcst_lead") && job.getEventEqual()) {
        strIndyVarFormatted = " if( (select fcst_lead_offset FROM model_fcst_lead_offset "
                + "WHERE model = h.model) is NULL , "
                + strIndyVarFormatted + " , " + strIndyVarFormatted
                + " +  (select fcst_lead_offset FROM model_fcst_lead_offset "
                + "WHERE model = h.model) ) ";
      }
      strWhere += (!strWhere.isEmpty() ? "  AND " : "") + strIndyVarFormatted
              + " IN (" + MVUtil.buildValueList(job.getIndyVal()) + ")\n";

      //  add fcst_var to the select list and temp table entries
      selectList += ",\n  h.fcst_var";
      selectPlotList += ",\n  h.fcst_var";

      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          String fieldFormatted = formatField(strField, job.isModeJob() || job.isMtdJob(), true);
          if (listPlotFixVal[i].getValue() != null) {
            if (!selectList.contains(fieldFormatted.trim())) {
              selectList += ",\n  " + fieldFormatted;
            }
            if (!selectPlotList.contains(fieldFormatted.trim())) {
              selectPlotList += ",\n  " + fieldFormatted;
            }
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
            "SELECT\n" + strSelectListStat + ",\n"
                    + "  object_id,\n"
                    + "  object_cat,\n"
                    + "  area,\n"
                    + "  total,\n"
                    + "  fcst_flag,\n"
                    + "  simple_flag,\n"
                    + "  matched_flag\n"
                    + "FROM\n"
                    + " mode_header ,\n"
                    + " mode_obj_single,\n"
                    + " mode_cts \n"
                    + "WHERE\n" + strWhere
                    + " AND mode_obj_single.mode_header_id = mode_header.mode_header_id"
                    + " AND mode_cts.mode_header_id = mode_obj_single.mode_header_id"
                    + " AND " + BINARY + "mode_cts.field = 'OBJECT'";
  }

  @Override
  public String buildAndExecuteQueriesForHistJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm,
          PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException {
    String strSelectList = "";
    String strWhereSeries = "";
    Map.Entry[] listSeries = job.getSeries1Val().getOrderedEntriesForSqlSeries();


    for (Map.Entry listSery : listSeries) {
      //  get the current series field and values
      String strSeriesField = listSery.getKey().toString();
      String[] listSeriesVal = (String[]) listSery.getValue();
      //  validate the series field and get its type
      if (!statHeaderSqlType.contains(strSeriesField)) {
        throw new ValidationException(
                "unrecognized " + "stat" + "_header field: " + strSeriesField);
      }
      //  build the select list element, where clause and temp table list element
      strSelectList += (strSelectList.isEmpty() ? "" : ",")
              + "  " + formatField(strSeriesField, false, true);
      strWhereSeries += "  AND " + BINARY + formatField(strSeriesField, false, false)
              + " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";
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
      binColumnName = "bin_size";
    }
    strWhere = strWhere.replaceAll("h\\.n_" + type, "ld.n_" + type);
    strNumSelect =
            "SELECT DISTINCT\n"
                    + "  ld.n_" + type + " \n"
                    + "FROM\n"
                    + "  stat_header h,\n"
                    + "  " + table + " ld\n"
                    + "WHERE\n"
                    + strWhere
                    + "  AND h.stat_header_id = ld.stat_header_id;";

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + "\n");
      printStreamSql.flush();
    }


    //  run the rank number query and warn, if necessary
    String strMsg = "";
    List<String> listNum = getNumbers(strNumSelect, job.getCurrentDBName().get(0));


    if (listNum.isEmpty()) {
      throw new ValidationException("no " + type + "  data found");
    } else if (1 < listNum.size()) {
      strMsg = "  **  WARNING: multiple n_" + type + " values found for search criteria: ";
      for (int i = 0; i < listNum.size(); i++) {
        strMsg += (0 < i ? ", " : "") + listNum.get(i);
      }
      printStream.println(strMsg + "\n");
    }


    //  build a query for the rank data
    strWhere = strWhere + strWhereSeries;
    String strPlotDataSelect =
            "SELECT\n  ldr.i_value,\n";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
    }
    if (listPlotFixVal.length > 0) {
      for (int i = 0; i < listPlotFixVal.length; i++) {
        String strField = (String) listPlotFixVal[i].getKey();
        if (!strField.equals("fcst_var") && listPlotFixVal[i].getValue() != null) {
          strPlotDataSelect += strField + ",\n";

        }
      }
    }


    strPlotDataSelect = strPlotDataSelect + "  ldr." + type + "_i,\n"
            + "   ld.fcst_valid_beg, \n"
            + "   ld.fcst_lead \n";

    if (binColumnName != null) {
      strPlotDataSelect = strPlotDataSelect + ", ld." + binColumnName + "\n";
    }
    strPlotDataSelect = strPlotDataSelect + "FROM\n"
            + "  stat_header h,\n"
            + "  " + table + " ld,\n"
            + "  " + tableBins + " ldr\n"
            + "WHERE\n"
            + strWhere
            + "  AND h.stat_header_id = ld.stat_header_id\n"
            + "  AND ld.line_data_id = ldr.line_data_id\n";

    strPlotDataSelect = strPlotDataSelect + ";";
    if (printStreamSql != null) {
      printStreamSql.println(strPlotDataSelect + "\n");
      printStreamSql.flush();
    }

    //  get the data for the current plot from the plot_data temp table and write it to a data file
    List<String> queries = new ArrayList<>(1);
    queries.add(strPlotDataSelect);
    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      MvResponse mvResponse = executeQueriesAndSaveToFile(queries, strDataFile,
              job.getCalcCtc() || job.getCalcSl1l2()
                      || job.getCalcSal1l2() || job.getCalcGrad(),
              job.getCurrentDBName().get(i),
              i == 0);
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
      strWhereSeries += "  AND " + BINARY + formatField(strSeriesField, false, false)
              + " IN (" + MVUtil.buildValueList(listSeriesVal) + ")\n";

    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());

    boolean boolRelyPlot = job.getPlotTmpl().startsWith("rely");

    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);
    strWhere = strWhere + strWhereSeries;


    //  check to ensure only a single obs_thresh is used
    String strObsThreshSelect =
            "SELECT\n  DISTINCT(h.obs_thresh)\n"
                    + "FROM\n"
                    + "  stat_header h,\n"
                    + "  " + (boolRelyPlot || job.getRocPct() ? "line_data_pct" : "line_data_ctc")
                    + " ld\n"
                    + "WHERE\n"
                    + strWhere
                    + "  AND h.stat_header_id = ld.stat_header_id\n"
                    + "ORDER BY h.obs_thresh;";


    if (printStreamSql != null) {
      printStreamSql.println(strObsThreshSelect + "\n");
    }


    //  build the query depending on the type of data requested
    String strPlotDataSelect = "";
    List<String> listFcstThresh = new ArrayList<>();
    if (boolRelyPlot || job.getRocPct()) {

      //  check to ensure only a single fcst_thresh is used
      String strFcstThreshSelect =
              "SELECT\n";

      strFcstThreshSelect = strFcstThreshSelect + "  DISTINCT(h.fcst_thresh) thresh\n";

      strFcstThreshSelect = strFcstThreshSelect
              + "FROM\n"
              + "  stat_header h,\n"
              + "  line_data_pct ld\n"
              + "WHERE\n"
              + strWhere
              + "  AND h.stat_header_id = ld.stat_header_id\n"
              + "ORDER BY h.fcst_thresh;";


      if (printStreamSql != null) {
        printStreamSql.println(strFcstThreshSelect + "\n");
      }

      listFcstThresh = getNumbers(strFcstThreshSelect, job.getCurrentDBName().get(0));


      //  build the plot data sql
      strPlotDataSelect =
              "SELECT\n  ";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
      }
      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          String fieldFormatted = formatField(strField, job.isModeJob() || job.isMtdJob(), true);
          if (!strField.equals("fcst_var") && listPlotFixVal[i].getValue() != null) {
            if (!strPlotDataSelect.contains(fieldFormatted.trim())) {
              strPlotDataSelect += fieldFormatted + ",\n";
            }

          }
        }
      }
      if (boolRelyPlot) {
        strPlotDataSelect = strPlotDataSelect
                + "  ldt.i_value,\n"
                + "  ldt.thresh_i,\n"
                + "  ldt.oy_i oy_i,\n"
                + "  ldt.on_i on_i,\n"
                + "   ld.fcst_valid_beg, \n"
                + "   ld.fcst_lead \n";

        strPlotDataSelect = strPlotDataSelect + "FROM\n"
                + "  stat_header h,\n"
                + "  line_data_pct ld,\n"
                + "  line_data_pct_thresh ldt\n"
                + "WHERE\n"
                + strWhere
                + "  AND h.stat_header_id = ld.stat_header_id\n"
                + "  AND ld.line_data_id = ldt.line_data_id;";

      } else {
        strPlotDataSelect = strPlotDataSelect
                + "  ldt.i_value,\n"
                + "  ldt.thresh_i,\n"
                + "  ldt.oy_i oy_i,\n"
                + "  ldt.on_i on_i,\n"
                + "   ld.fcst_valid_beg, \n"
                + "   ld.fcst_lead \n";

        strPlotDataSelect = strPlotDataSelect + "FROM\n"
                + "  stat_header h,\n"
                + "  line_data_pct ld,\n"
                + "  line_data_pct_thresh ldt\n"
                + "WHERE\n"
                + strWhere
                + "  AND h.stat_header_id = ld.stat_header_id\n"
                + "  AND ld.line_data_id = ldt.line_data_id\n";
        strPlotDataSelect = strPlotDataSelect + ";";
      }

    } else if (job.getRocCtc()) {

      strPlotDataSelect =
              "SELECT\n  h.fcst_thresh fcst_thresh,\n";
      if (listSeries.length > 0) {
        strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
      }
      if (listPlotFixVal.length > 0) {
        for (int i = 0; i < listPlotFixVal.length; i++) {
          String strField = (String) listPlotFixVal[i].getKey();
          if (!strField.equals("fcst_var") && !strField.equals("fcst_thresh") && listPlotFixVal[i].getValue() != null) {
            strPlotDataSelect += formatField(strField, job.isModeJob() || job.isMtdJob(), true) + ",\n";

          }
        }
      }
      strPlotDataSelect = strPlotDataSelect
              + "  ld.fy_oy fy_oy,\n"
              + "  ld.fy_on fy_on,\n"
              + "  ld.fn_oy fn_oy,\n"
              + "  ld.fn_on fn_on,\n"
              + "   ld.fcst_valid_beg, \n"
              + "   ld.fcst_lead \n"
              + "FROM\n"
              + "  stat_header h,\n"
              + "  line_data_ctc ld\n"
              + "WHERE\n"
              + strWhere
              + "  AND h.stat_header_id = ld.stat_header_id\n";
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
    List<String> listObsThresh = getNumbers(strObsThreshSelect, job.getCurrentDBName().get(0));
    if (listObsThresh.size() > 1) {
      StringBuilder obsThreshMsg = new StringBuilder(
              "ROC/Reliability plots must contain data from only a single obs_thresh,"
                      + " instead found " + listObsThresh.size());
      for (int i = 0; i < listObsThresh.size(); i++) {
        obsThreshMsg.append(0 == i ? ": " : ", ").append(listObsThresh.get(i));
      }
      throw new ValidationException(obsThreshMsg.toString());
    }

    if (listObsThresh.isEmpty()) {
      String strObsThreshMsg = "ROC/Reliability plots must contain data "
              + "from at least one obs_thresh ";
      throw new ValidationException(strObsThreshMsg);
    }

    //  if the query for a PCT plot does not return data from a single fcst_thresh, throw an error
    if (job.getRocPct() && listFcstThresh.size() > 1) {
      StringBuilder fcstThreshMsg = new StringBuilder(
              "ROC/Reliability plots using PCTs must contain data "
                      + "from only a single fcst_thresh, "
                      + "instead found " + listFcstThresh.size());
      for (int i = 0; i < listFcstThresh.size(); i++) {
        fcstThreshMsg.append(0 == i ? ":" : "").append("\n  ").append(listFcstThresh.get(i));
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
        printStream.println(mvResponse.getInfoMessage() + "\n");
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
    StringBuilder strSelectList = new StringBuilder();
    StringBuilder strWhereSeries = new StringBuilder();

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
      strSelectList.append((strSelectList.length() == 0) ? "" : ",").append("  ")
              .append(formatField(strSeriesField, false, true));
      strWhereSeries.append("  AND ").append(BINARY)
              .append(formatField(strSeriesField, false, false))
              .append(" IN (").append(MVUtil.buildValueList(listSeriesVal)).append(")\n");


    }
    if (!strSelectList.toString().contains("fcst_valid")) {
      strSelectList.append(",\n " + " ld.fcst_valid_beg");
    }
    if (!strSelectList.toString().contains("fcst_lead")) {
      strSelectList.append(",\n " + " ld.fcst_lead");
    }


    //  populate the template map with fixed values
    Map.Entry[] listPlotFixVal = MVUtil.buildPlotFixTmplMap(listPlotFixPerm, job.getPlotFixVal());


    //  build the stat_header where clauses of the sql
    String strWhere = buildPlotFixWhere(listPlotFixVal, job, false);

    strWhere = strWhere.replaceAll("h\\.n_pnt", "ld.n_pnt");
    String strNumSelect =
            "SELECT DISTINCT\n"
                    + "  ld.n_pnt\n"
                    + "FROM\n"
                    + "  stat_header h,\n"
                    + "  line_data_eclv ld\n"
                    + "WHERE\n"
                    + strWhere
                    + "  AND h.stat_header_id = ld.stat_header_id;";

    if (printStreamSql != null) {
      printStreamSql.println(strNumSelect + "\n");
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
      printStream.println(strMsg + "\n");
    }


    Map<String, Integer> pctThreshInfo;
    MVOrderedMap[] series = MVUtil.permute(job.getSeries1Val().convertFromSeriesMap()).getRows();
    for (int seriesInd = 0; seriesInd < series.length; seriesInd++) {
      MVOrderedMap ser = series[seriesInd];
      String[] serName = ser.getKeyList();
      for (int serNameInd = 0; serNameInd < serName.length; serNameInd++) {
        String strSelPctThresh = "SELECT DISTINCT ld.n_pnt\nFROM\n  stat_header h,\n  "
                + "line_data_eclv ld,\n line_data_eclv_pnt ldt\n WHERE\n"
                + serName[serNameInd] + " = '"
                + ser.getStr(serName[serNameInd]) + "' AND "
                + strWhere
                + "  AND ld.stat_header_id = h.stat_header_id "
                + "AND ld.line_data_id = ldt.line_data_id;";
        if (printStreamSql != null) {
          printStreamSql.println(strSelPctThresh + "\n");
        }

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
            "SELECT\n";
    if (listSeries.length > 0) {
      strPlotDataSelect = strPlotDataSelect + strSelectList + ",\n";
    }
    if (listPlotFixVal.length > 0) {
      for (Map.Entry aListPlotFixVal : listPlotFixVal) {
        String strField = (String) aListPlotFixVal.getKey();
        if (!strPlotDataSelect.contains(strField)) {
          strPlotDataSelect = strPlotDataSelect + strField + ",\n";
        }
      }
    }

    strPlotDataSelect = strPlotDataSelect
            + "  ldt.x_pnt_i,\n"
            + "  ldt.y_pnt_i \n";

    strPlotDataSelect = strPlotDataSelect + "FROM\n"
            + "  stat_header h,\n"
            + "  line_data_eclv ld,\n"
            + "  line_data_eclv_pnt ldt\n"
            + "WHERE\n"
            + strWhere
            + "  AND h.stat_header_id = ld.stat_header_id\n"
            + "  AND ld.line_data_id = ldt.line_data_id";

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
    if (field.equals("init_hour") && fmtSel) {
      return (mode ? "HOUR(fcst_init)" : "HOUR(fcst_init_beg)") + " init_hour";
    } else if (field.equals("init_hour") && !fmtSel) {
      return (mode ? "HOUR(h.fcst_init)" : "HOUR(ld.fcst_init_beg)");
    } else if (field.equals("valid_hour") && fmtSel) {
      return (mode ? "HOUR(fcst_valid)" : "HOUR(fcst_valid_beg)") + " valid_hour";
    } else if (field.equals("valid_hour") && !fmtSel) {
      return (mode ? "HOUR(h.fcst_valid)" : "HOUR(ld.fcst_valid_beg)");
    } else if (field.equals("fcst_init") && fmtSel) {
      return " fcst_init";
    } else if (field.equals("fcst_init_beg") && fmtSel) {
      return mode ? " fcst_init" : " fcst_init_beg";
    } else if (field.equals("fcst_init_beg") && !fmtSel) {
      return mode ? "ld.fcst_init" : "ld.fcst_init_beg";
    } else if (field.equals("fcst_valid") && fmtSel) {
      return " fcst_valid";
    } else if (field.equals("fcst_valid_beg") && fmtSel) {
      return mode ? " fcst_valid" : " fcst_valid_beg";
    } else if (field.equals("fcst_valid_beg") && !fmtSel) {
      return mode ? "ld.fcst_valid" : "ld.fcst_valid_beg";
    } else if (field.equals("fcst_lead") && !fmtSel) {
      return mode ? "h.fcst_lead" : "ld.fcst_lead";
    } else if (field.equals("fcst_lead") && fmtSel) {
      return "fcst_lead";
    } else if (field.equals("obs_valid_beg") && !fmtSel) {
      return mode ? "ld.obs_valid" : "ld.obs_valid_beg";
    } else if (field.equals("obs_valid_beg") && fmtSel) {
      return mode ? " obs_valid" : " obs_valid_beg";
    } else if (field.equals("obs_init_beg") && !fmtSel) {
      return mode ? "ld.obs_init" : "ld.obs_init_beg";
    } else if (field.equals("obs_init_beg") && fmtSel) {
      return mode ? " obs_init" : " obs_init_beg";
    } else {
      if (!fmtSel) {
        return "h." + field;
      } else {
        return field;
      }
    }
  }



  private BuildMysqlQueryStrings build(
          boolean boolModePlot,
          List<String> headerSql,
          Map.Entry[] listSeries, String strWhere,
          boolean isFormatSelect) throws ValidationException {

    BuildMysqlQueryStrings buildMysqlQueryStrings = new BuildMysqlQueryStrings(boolModePlot,
            headerSql,
            listSeries, strWhere,
            isFormatSelect);
    for (Map.Entry entry : listSeries) {

      //  get the current series field and values
      String field = entry.getKey().toString();

      //  validate the series field and get its type
      if (!headerSql.contains(field)) {
        throw new ValidationException("unrecognized " + (boolModePlot ? "mode" : "stat")
                + "_header field: " + field);
      }

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
                      + BINARY + formatField(field, boolModePlot, false)
                      + " IN (" + MVUtil.buildValueList((String[]) entry.getValue()) + ")\n");

    }
    return buildMysqlQueryStrings;
  }

}


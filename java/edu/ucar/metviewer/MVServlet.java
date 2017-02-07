package edu.ucar.metviewer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MVServlet extends HttpServlet {

  public static final Pattern _patDBLoad = Pattern.compile(".*/db/([\\w\\d]+)$");
  public static final Pattern _patDownload = Pattern.compile(".*/download");
  public static final Pattern _patProbFcstVar = Pattern.compile("PROB\\(([\\w\\d]+)([<>=]{1,2})([^\\)]+)\\)");
  private static final String DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";
  protected static final Map<String, String> _tableListValCache = new HashMap<>();
  protected static final Map<String, String> _tableListStatCache = new HashMap<>();
  private static final long serialVersionUID = 1L;
  private static final Logger _logger = Logger.getLogger("edu.ucar.metviewer.MVServlet");
  private static final FilenameFilter PNG_FILTER = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.toLowerCase(Locale.ENGLISH).endsWith(".png");
    }
  };
  private static final FilenameFilter XML_FILTER = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.toLowerCase(Locale.ENGLISH).endsWith(".xml");
    }
  };
  public static String _strDBHost = "";
  public static String _strDBUser = "";
  public static String _strDBPassword = "";
  public static String _strDBManagementSystem = "";
  public static String _strDBDriver = "";
  public static String _strPlotXML = "";
  public static String _strRTmpl = "";
  public static String _strRWork = "";
  public static String _strPlots = "";
  public static String _strRscript = "";
  public static String _strRedirect = "";
  public static String _strURLOutput = "";
  public static String _strData = "";
  public static String _strScripts = "";
  public static boolean _boolListValCache = false;
  public static boolean _boolListStatCache = false;

  /**
   * Clear cached <list_val> values for the input database
   *
   * @param con
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListValCache(Connection con) throws Exception {
    if (!_boolListValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + con.getMetaData().getURL() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListValCache);
    return "<list_val_clear_cache>success: removed " + intNumRemoved + " entries</list_val_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_val cache to the log file.
   *
   * @param con
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListValCacheKeys(Connection con) throws Exception {
    if (!_boolListValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + con.getMetaData().getURL() + "</db>";
    String[] listKeys = listTableEntriesByKeyPrefix(strKeyPrefix, _tableListValCache);
    String strXML = "";
    String strMsg = "db url: " + con.getMetaData().getURL() + "  # keys: " + listKeys.length + "\n";
    for (String listKey : listKeys) {
      strMsg += "  " + listKey + "\n";
      strXML += "<key>" + listKey + "</key>";
    }
    _logger.debug("handleListValCacheKeys() - " + strMsg);
    return "<list_val_cache_keys>" + strXML + "</list_val_cache_keys>";
  }

  /**
   * Clear cached <list_stat> values for the input database
   *
   * @param con
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListStatCache(Connection con) throws Exception {
    if (!_boolListStatCache) {
      return "<error>caching list_stat caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + con.getMetaData().getURL() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListStatCache);
    return "<list_stat_clear_cache>success: removed " + intNumRemoved + " entries</list_stat_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_stat cache to the log file.
   *
   * @param con
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListStatCacheKeys(Connection con) throws Exception {
    if (!_boolListStatCache) {
      return "<error>list_stat caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + con.getMetaData().getURL() + "</db>";
    String[] listKeys = listTableEntriesByKeyPrefix(strKeyPrefix, _tableListStatCache);
    String strXML = "";
    String strMsg = "db url: " + con.getMetaData().getURL() + "  # keys: " + listKeys.length + "\n";
    for (String listKey : listKeys) {
      strMsg += "  " + listKey + "\n";
      strXML += "<key>" + listKey + "</key>";
    }
    _logger.debug("handleListStatCacheKeys() - " + strMsg);
    return "<list_stat_cache_keys>" + strXML + "</list_stat_cache_keys>";
  }

  /**
   * Searches all key values of the input table, which are assumed to be Strings, and removes any entry whose key matches the specified prefix
   *
   * @param prefix String key prefix to match
   * @param table  Table from which matching entries will be removed
   * @return number of removed entries
   */
  public static int removeTableEntriesByKeyPrefix(String prefix, Map<String, String> table) {
    int intNumRemoved = 0;
    Map.Entry[] listEntries = table.entrySet().toArray(new Map.Entry[]{});
    for (Map.Entry listEntry : listEntries) {
      String strKey = listEntry.getKey().toString();
      if (!strKey.startsWith(prefix)) {
        continue;
      }
      table.remove(strKey);
      intNumRemoved++;
    }
    return intNumRemoved;
  }

  /**
   * Searches all key values of the input table, which are assumed to be Strings, and removes any entry whose key matches the specified prefix
   *
   * @param prefix String key prefix to match
   * @param table  Table from which matching entries will be removed
   * @return number of removed entries
   */
  public static String[] listTableEntriesByKeyPrefix(String prefix, Map<String, String> table) {
    ArrayList listKeys = new ArrayList();
    Map.Entry[] listEntries = table.entrySet().toArray(new Map.Entry[]{});
    for (Map.Entry listEntry : listEntries) {
      String strKey = listEntry.getKey().toString();
      if (strKey.startsWith(prefix)) {
        listKeys.add(strKey);
      }
    }
    return MVUtil.toArray(listKeys);
  }

  /**
   * List database values for the specified field with the specified constraints, using cached data if appropriate, and return it in serialized XML
   *
   * @param nodeCall    MVNode containing request information
   * @param requestBody the XML sent by the client which is used to cache the response
   * @param con         Database connection to search against
   * @return XML response information
   * @throws Exception
   */
  public static String handleListVal(MVNode nodeCall, String requestBody, Connection con) throws Exception {
    //  parse the input request, and initialize the response
    StringBuilder strResp = new StringBuilder("<list_val>");
    String strId = nodeCall._children[0]._value;
    String strHeaderField = nodeCall._children[1]._value;
    boolean boolMode = nodeCall._children[1]._tag.equals("mode_field");
    boolean boolRhist = nodeCall._children[1]._tag.equals("rhist_field");
    boolean boolPhist = nodeCall._children[1]._tag.equals("phist_field");
    boolean boolROC = nodeCall._children[1]._tag.equals("roc_field");
    boolean boolRely = nodeCall._children[1]._tag.equals("rely_field");
    boolean boolEnsSS = nodeCall._children[1]._tag.equals("ensss_field");
    boolean boolPerf = nodeCall._children[1]._tag.equals("perf_field");
    boolean boolTaylor = nodeCall._children[1]._tag.equals("taylor_field");
    String strHeaderTable = boolMode ? "mode_header" : "stat_header";
    _logger.debug("handleListVal() - listing values for field " + strHeaderField + " and id " + strId);
    strResp.append("<id>").append(strId).append("</id>");

    //  check the list val cache for the request data
    String strCacheKey = "<db>" + con.getMetaData().getURL() + "</db>" +
      requestBody.replaceAll("<id>\\d+</id>", "").replaceAll("<date>\\d+</date>", "");
    if (_boolListValCache && _tableListValCache.containsKey(strCacheKey)) {
      String strListVal = _tableListValCache.get(strCacheKey).toString().replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
      _logger.debug("handleListVal() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListVal);
      return strListVal;
    }

    //  determine if the requested field is n_rank and format accordingly
    String strField = strHeaderField.toLowerCase(Locale.ENGLISH);
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

    //  build a list of the line_data tables for all the stats
    String[] listTables =  tableLineDataTables.keySet().toArray(new String[]{});

    //  build the where clause for the fcst_var values, if present
    String strWhere = "";
    if (boolFcstVar) {
      String strFcstVarList = "";
      String[] listFcstVar =  tableFcstVarStat.keySet().toArray(new String[]{});
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
    long intStart = 0;
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
      String strFieldDBCrit = MVUtil.formatField(strFieldCrit, boolMode, false);
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
    String strTmpTable = "";
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
      String strSelectField = MVUtil.formatField(strField, boolMode);
      //  create a temp table for the list values from the different line_data tables
      strTmpTable = "tmp_" + new Date().getTime();
      try (Statement stmtTmp = con.createStatement();) {
        String strTmpSQL = "CREATE TEMPORARY TABLE " + strTmpTable + " (" + strField + " TEXT);";
        _logger.debug("handleListVal() - sql: " + strTmpSQL);
        long intStartTmp = new Date().getTime();
        stmtTmp.executeUpdate(strTmpSQL);
        _logger.debug("handleListVal() - temp table " + strTmpTable + " query returned in " + MVUtil.formatTimeSpan(new Date().getTime() - intStartTmp));
        //  add all distinct list field values to the temp table from each line_data table
        for (int i = 0; i < listTables.length; i++) {
          strTmpSQL = "INSERT INTO " + strTmpTable + " SELECT DISTINCT " + strSelectField + " FROM " + listTables[i] + " ld" + strWhereTime;
          _logger.debug("handleListVal() - sql: " + strTmpSQL);
          if (0 == i) {
            intStart = new Date().getTime();
          }
          stmtTmp.executeUpdate(strTmpSQL);
        }
      }

      //  build a query to list all distinct, ordered values of the list field from the temp table
      strSQL = "SELECT DISTINCT " + strField + " FROM " + strTmpTable + " ORDER BY " + strField + ";";
    } else {
      String strFieldDB = MVUtil.formatField(strField, boolMode).replaceAll("h\\.", "");
      strWhere = strWhere.replaceAll("h\\.", "");
      strSQL = "SELECT DISTINCT " + strFieldDB + " FROM " + strHeaderTable + " " + strWhere + " ORDER BY " + strField;
    }
    //  execute the query
    try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery(strSQL)) {

      _logger.debug("handleListVal() - sql: " + strSQL);
      if (0 == intStart) {
        intStart = new Date().getTime();
      }

      //  build a list of values from the query
      int intNumVal = 0;
      List<String> listRes = new ArrayList<>();

      while (res.next()) {
        listRes.add(res.getString(1));
        intNumVal++;
      }

      _logger.debug("handleListVal() - returned " + intNumVal + " values in " + MVUtil.formatTimeSpan(new Date().getTime() - intStart));
      String[] listVal = listRes.toArray(new String[]{});

      //  drop the temp table, if present
      if (!"".equals(strTmpTable)) {
        stmt.executeUpdate("DROP TABLE IF EXISTS " + strTmpTable + ";");
      }

      //  sort and format the results, depending on the field
      if (strField.equals("fcst_thresh") || strField.equals("fcst_thr") ||
        strField.equals("obs_thresh") || strField.equals("obs_thr")) {
        listVal = MVUtil.sortThresh(listVal);
      } else if (strField.equals("fcst_lev") || strField.equals("obs_lev")) {
        listVal = MVUtil.sortLev(listVal);
      } else if (strField.equals("fcst_lead") || strField.equals("obs_lead")) {
        listVal = MVUtil.sortFormatLead(listVal, true, false);
      } else if (strField.equals("init_hour") || strField.equals("valid_hour")) {
        listVal = MVUtil.sortHour(listVal, true);
      } else if (strField.equals("fcst_valid") || strField.equals("fcst_init") || strField.equals("obs_valid")) {
        listVal = MVUtil.formatDates(listVal);
      }

      //  add the list of field values to the response
      HashMap<String, String> tabProb = new HashMap<>();
      for (String aListVal : listVal) {

        //  add the database field value to the list
        strResp.append("<val>").append(aListVal.replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;")).append("</val>");

        //  if the database field value is probabilistic, add a wild card version
        if (!strField.equals("fcst_var")) {
          continue;
        }
        Matcher matProb = _patProbFcstVar.matcher(aListVal);
        if (matProb.matches()) {
          String strProbKey = matProb.group(1) + matProb.group(2);
          String strProbFcstVar = "PROB(" + strProbKey + "*)";
          if (!tabProb.containsKey(strProbKey)) {
            strResp.append("<val>").append(strProbFcstVar).append("</val>");
            tabProb.put(strProbKey, strProbFcstVar);
          }
        }
      }

    }
    strResp.append("</list_val>");
    _tableListValCache.put(strCacheKey, strResp.toString());
    return strResp.toString();
  }

  /**
   * List statistics for the specified fcst_var, using cached data if appropriate, and return it in serialized XML
   *
   * @param nodeCall    MVNode containing request information
   * @param requestBody the XML sent by the client which is used to cache the response
   * @param con         Database connection to search against
   * @return XML response information
   * @throws Exception
   */
  public static String handleListStat(MVNode nodeCall, String requestBody, Connection con) throws Exception {
    //  if the request is for the mode stats, return the static list
    String strId = nodeCall._children[0]._value;
    String strFcstVar = nodeCall._children[1]._value;
    if (nodeCall._children[0]._tag.equals("mode_fcst_var")) {
      String strResp = "<list_stat><id>" + strId + "</id></list_stat>";
      _logger.debug("handleListStat() - returning mode stats: " + strResp);
      return strResp;
    }

    //  check the list val cache for the request data
    String strCacheKey = "<db>" + con.getMetaData().getURL() + "</db>" +
      requestBody.replaceAll("<id>\\d+</id>", "").replaceAll("<date>\\d+</date>", "");
    if (_boolListStatCache) {
      _logger.debug("handleListStat() - checking cache for key " + strCacheKey + ": " + _tableListStatCache.containsKey(strCacheKey));
      if (_tableListStatCache.containsKey(strCacheKey)) {
        String strListStat = _tableListStatCache.get(strCacheKey).replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
        _logger.debug("handleListStat() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListStat);
        return strListStat;
      }
    }

    //  build a query for the fcst_var stat counts
    //this is a query for the local db


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
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'mpr'  FROM line_data_orank  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) orank)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'ssvar'  FROM line_data_ssvar  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) ssvar)\n" +
      "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'sal1l2'  FROM line_data_sal1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) sal1l2)\n";
    //  "UNION ALL ( SELECT IFNULL( (SELECT ld.stat_header_id 'val1l2'  FROM line_data_val1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id limit 1) ,-9999) val1l2)\n";

    //this is a query for the VSDB
    /*String strSQL =
         "(SELECT COUNT(*), 'sl1l2'  FROM line_data_sl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
         "(SELECT COUNT(*), 'ctc'    FROM line_data_ctc    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
         "(SELECT COUNT(*), 'vl1l2'  FROM line_data_vl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id);";*/
    _logger.debug("handleListStat() - gathering stat counts for fcst_var " + strFcstVar + "\n  sql: " + strSQL);
    long intStart = new Date().getTime();
    //  build a list of stat names using the stat ids returned by the query
    StringBuilder strResp = new StringBuilder("<list_stat><id>" + strId + "</id>");
    List<String> listStatName = new ArrayList<>();
    try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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
                listStatName.addAll(Arrays.asList(MVUtil._tableStatsCnt.getKeyList()));
              }
              boolCnt = true;
              break;
            case 2:
            case 3:
              if (!boolCts) {
                listStatName.addAll(Arrays.asList(MVUtil._tableStatsCts.getKeyList()));
              }
              boolCts = true;
              break;
            case 4:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsNbrcnt.getKeyList()));
              break;
            case 5:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsNbrcts.getKeyList()));
              break;
            case 6:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsPstd.getKeyList()));
              break;
            case 7:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsMcts.getKeyList()));
              break;
            case 8:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsRhist.getKeyList()));
              break;
            case 9:
              //case 16:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsVl1l2.getKeyList()));
              break;
            case 10:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsPhist.getKeyList()));
              break;
            case 11:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsEnscnt.getKeyList()));
              break;
            case 12:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsMpr.getKeyList()));
              break;
            case 13:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsOrank.getKeyList()));
              break;
            case 14:
              listStatName.addAll(Arrays.asList(MVUtil._tableStatsSsvar.getKeyList()));
              break;
            default:

          }
        }
        intStatIndex++;
      }
      stmt.close();
      res.close();
    }
    //  sort and build the response string using the list of stat names
    String[] listStat = MVUtil.toArray(listStatName);
    Arrays.sort(listStat, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((String) o1).compareTo((String) o2);
      }
    });
    int intNumStat = 0;
    for (String aListStat : listStat) {
      strResp.append("<val>").append(aListStat).append("</val>");
      intNumStat++;
    }
    _logger.debug("handleListStat() - returned " + intNumStat + " stats in " + MVUtil.formatTimeSpan(new Date().getTime() - intStart));

    //  clean up
    strResp.append("</list_stat>");
    _tableListStatCache.put(strCacheKey, strResp.toString());
    return strResp.toString();
  }

  /**
   * Save and parse the plot request XML, then generate the requested plot and return information about it. Update the web_plot table in the database from which
   * the plot data was drawn.
   *
   * @param strRequest XML plot specification
   * @param con        database connection
   * @return status message
   */
  public static String handlePlot(String strRequest, Connection con, String currentDBName) throws Exception {

    //  extract the plot xml from the request
    String strPlotXML = strRequest;
    strPlotXML = strPlotXML.substring(strPlotXML.indexOf("</db_con>") + 9);
    strPlotXML = strPlotXML.substring(0, strPlotXML.indexOf("</request>"));
    String strPlotPrefix;
    SimpleDateFormat formatPlot = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);

    strPlotPrefix = "plot_" + formatPlot.format(new Date());

    //  add plot file information to the plot spec
    strPlotXML =
      "<plot_spec>" +
        "<connection>" +
        "<host>" + _strDBHost + "</host>" +
        "<database>" + currentDBName + "</database>" +
        "<user>" + "******" + "</user>" +
        "<password>" + "******" + "</password>" +
        "</connection>" +
        (_strRscript.equals("") ? "" : "<rscript>" + _strRscript + "</rscript>") +
        "<folders>" +
        "<r_tmpl>" + _strRTmpl + "</r_tmpl>" +
        "<r_work>" + _strRWork + "</r_work>" +
        "<plots>" + _strPlots + "</plots>" +
        "<data>" + _strData + "</data>" +
        "<scripts>" + _strScripts + "</scripts>" +
        "</folders>" +
        strPlotXML +
        "</plot_spec>";

    strPlotXML = strPlotXML.replace("<tmpl>",
      "<tmpl>" +
        "<data_file>" + strPlotPrefix + ".data" + "</data_file>" +
        "<plot_file>" + strPlotPrefix + ".png" + "</plot_file>" +
        "<r_file>" + strPlotPrefix + ".R" + "</r_file>");

    //  parse the input document and build the MVNode data structure
    Document doc = MVPlotJobParser.getDocumentBuilder().parse(new ByteArrayInputStream(strPlotXML.getBytes()));
    StreamResult streamResult;
    File f = new File(_strPlotXML + "/" + strPlotPrefix + ".xml");
    try (FileOutputStream stream = new FileOutputStream(f);
         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream, "UTF-8");) {
      //Begin write DOM to file

      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      streamResult = new StreamResult(outputStreamWriter);
      transformer.transform(new DOMSource(doc), streamResult);
      stream.flush();
    } catch (Exception e) {
      _logger.error("handlePlot() - ERROR: caught " + e.getClass() + " serializing plot xml: " + e.getMessage());
      return "<error>failed to serialize plot xml - reason: " + e.getMessage() + "</error>";
    }

    //  parse the input plot job
    MVPlotJobParser parser;
    MVPlotJob job;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      byteArrayInputStream = new ByteArrayInputStream(strPlotXML.getBytes());
      parser = new MVPlotJobParser(byteArrayInputStream, con);
      MVPlotJob[] jobs = parser.getJobsList();
      if (1 != jobs.length) {
        throw new Exception("unexpected number of plot jobs generated: " + jobs.length);
      }
      job = jobs[0];
    } catch (Exception e) {
      _logger.error("handlePlot() - ERROR: caught " + e.getClass() + " parsing plot job: " + e.getMessage());
      return "<error>failed to parse plot job - reason: " + e.getMessage() + "</error>";
    } finally {
      if (byteArrayInputStream != null) {
        byteArrayInputStream.close();
      }
    }

    //  run the plot job and write the batch output to the log file
    ByteArrayOutputStream log = null;
    PrintStream printStream = null;


    String strJobTmpl = job.getPlotTmpl();
    String strRErrorMsg = "";
    FileWriter writer = null;
    try {
      log = new ByteArrayOutputStream();
      printStream = new PrintStream(log);
      MVBatch bat = new MVBatch(printStream);
      bat._intNumPlots = 1;
      //  configure the batch engine and run the job
      bat._intNumPlots = 1;
      bat._strRtmplFolder = parser.getRtmplFolder();
      bat._strRworkFolder = parser.getRworkFolder();
      bat._strPlotsFolder = parser.getPlotsFolder();
      bat._strDataFolder = parser.getDataFolder();
      bat._strScriptsFolder = parser.getScriptsFolder();

      //  build the job SQL using the batch engine
      bat._boolSQLOnly = true;
      bat._boolVerbose = true;
      bat.setDbManagementSystem(_strDBManagementSystem);
      runTargetedJob(job, strJobTmpl, bat);
      bat._boolSQLOnly = false;
      bat._boolVerbose = false;
      String strPlotSQL = log.toString();

      //  write the plot SQL to a file
      writer = new FileWriter(_strPlotXML + "/" + strPlotPrefix + ".sql");
      writer.write(strPlotSQL);
      writer.close();
      log.reset();

      //  run the job to generate the plot
      //bat._boolVerbose = true;
      runTargetedJob(job, strJobTmpl, bat);
      String strPlotterOutput = log.toString();
      writer = new FileWriter(_strPlotXML + "/" + strPlotPrefix + ".log");
      writer.write(strPlotterOutput);
      writer.close();
      //  parse out R error messages, if present, throwing an exception if the error was fatal
      Matcher matOutput = Pattern.compile("(?sm)(==== Start Rscript error  ====.*====   End Rscript error  ====)").matcher(strPlotterOutput);
      if (matOutput.find()) {
        strRErrorMsg = matOutput.group(1);
      }
      if (strPlotterOutput.contains("Execution halted")) {
        throw new Exception("R error");
      }
      if (strPlotterOutput.contains("query returned no data")) {
        throw new Exception("query returned no data");
      }

    } catch (Exception e) {
      _logger.debug("handlePlot() - ERROR: caught " + e.getClass() + " running plot: " + e.getMessage() + "\nbatch output:\n" + log.toString());
      return "<response><error>" +
        "failed to run plot " + strPlotPrefix + " - reason: " + e.getMessage() +
        (!strRErrorMsg.equals("") ? ":\n" + strRErrorMsg : "") +
        "</error><plot>" + strPlotPrefix + "</plot></response>";
    } finally {
      if (log != null) {
        log.close();
      }
      if (printStream != null) {
        printStream.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    _logger.debug("handlePlot() - batch output:\n" + log.toString());

    return "<response><plot>" + strPlotPrefix + "</plot>" + (!strRErrorMsg.equals("") ? "<r_error>" + strRErrorMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</r_error></response>" : "</response>");
  }

  private static void runTargetedJob(MVPlotJob job, String strJobTmpl, MVBatch bat) throws Exception {
    if (strJobTmpl.equals("rhist.R_tmpl")) {
      bat.runRhistJob(job);
    } else if (strJobTmpl.equals("phist.R_tmpl")) {
      bat.runPhistJob(job);
    } else if (strJobTmpl.equals("roc.R_tmpl") ||
      strJobTmpl.equals("rely.R_tmpl")) {
      bat.runRocRelyJob(job);
    } else if (strJobTmpl.equals("performance.R_tmpl")) {
      bat.runJob(job);
    } else {
      bat.runJob(job);
    }
  }

  /**
   * Print an error message into writer of the input response
   *
   * @param response
   */
  public static void printErrorPage(HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");
    out.print(
      "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n\n" +
        "<html>\n" +
        "<head>\n" +
        "<title>METViewer Error</title>\n" +
        "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + _strRedirect + "/include/metviewer.css\"/>\n" +
        "<link rel=\"shortcut icon\" href=\"" + _strRedirect + "/include/ral_icon.ico\" type=\"image/x-icon\"/>\n" +
        "</head>\n" +
        "<body style=\"padding-left:20px; padding-top:20px\">\n" +
        "<span class=\"bold\">An error has occurred in METViewer.  Please contact your system administrator</span>\n" +
        "</body></html>\n\n");
  }

  /**
   * Search the mv_rev table of the database under the input connection and serialize its contents into an XML string which is returned.
   *
   * @param con database connection to use
   * @return serialized XML version of the mv_rev table
   * @throws Exception
   */
  public static String handleListMVRev(Connection con) throws Exception {
    StringBuilder strResp = new StringBuilder("<list_mv_rev>");

    //  query the database for the contents of the mv_rev table
    try (
      Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      ResultSet res = stmt.executeQuery("SELECT * FROM mv_rev;");) {
      while (res.next()) {
        strResp.append("<val>");
        strResp.append("<rev_id>").append(res.getString(1)).append("</rev_id>");
        strResp.append("<rev_date>").append(res.getString(2)).append("</rev_date>");
        strResp.append("<rev_name>").append(res.getString(3)).append("</rev_name>");
        strResp.append("<rev_detail>").append(res.getString(4)).append("</rev_detail>");
        strResp.append("</val>");
      }
    }
    strResp.append("</list_mv_rev>");
    return strResp.toString();
  }

  /**
   * Search the instance_info table of the database under the input connection and serialize its contents into an XML string which is returned.
   *
   * @param con database connection to use
   * @return serialized XML version of the instance_info table
   * @throws Exception
   */
  public static String handleListInstInfo(Connection con) throws Exception {
    StringBuilder strResp = new StringBuilder("<list_inst_info>");

    //  query the database for the contents of the instance_info table
    try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery("SELECT * FROM instance_info;")) {
      while (res.next()) {
        strResp.append("<val>");
        strResp.append("<info_id>").append(res.getString(1)).append("</info_id>");
        strResp.append("<info_updater>").append(res.getString(2)).append("</info_updater>");
        strResp.append("<info_date>").append(res.getString(3)).append("</info_date>");
        strResp.append("<info_detail>").append(res.getString(4)).append("</info_detail>");
        strResp.append("<info_xml>").append(!"".equals(res.getString(5))).append("</info_xml>");
        strResp.append("</val>");
      }
    }
    strResp.append("</list_inst_info>");
    return strResp.toString();
  }

  /**
   * Parse the input request XML and construct an insert statement for the instance_info table record with the input info_id.  Issue the insert and return a
   * status, depending on success.
   *
   * @param con database connection to use
   * @return status message indicating success or failure
   * @throws Exception
   */
  public static String handleAddInstInfo(MVNode nodeCall, Connection con) throws Exception {

    //  parse the components of the insert statment from the input XML
    String strInfoId = nodeCall._children[0]._value;
    String strInfoUpdater = nodeCall._children[1]._value;
    String strInfoDate = nodeCall._children[2]._value;
    String strInfoDetail = nodeCall._children[3]._value;
    //  validate the input
    if ("".equals(strInfoUpdater)) {
      return "<error>updater must not be blank</error>";
    }
    if ("".equals(strInfoDetail)) {
      return "<error>update_detail must not be blank</error>";
    }
    try {
      SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
      formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
      formatDB.parse(strInfoDate);
    } catch (Exception e) {
      return "<error>could not parse update_date: '" + strInfoDate + "'</error>";
    }
    int intRes = 0;
    //  construct the insert statement for the instance_info table
    try (Statement stmt = con.createStatement();) {
      intRes = stmt.executeUpdate("INSERT INTO instance_info VALUES (" +
        strInfoId + ", " +
        "'" + strInfoUpdater + "', " +
        "'" + strInfoDate + "', " +
        "'" + strInfoDetail + "', '');");
    }
    //  validate the returned number of updated records
    if (1 != intRes) {
      return "<error>unexpected number of records updated (" + intRes + ") for instance_info_id = " + strInfoId + "</error>";
    }
    return "<add_inst_info>success: updated 1 record</add_inst_info>";
  }

  /**
   * Parse the input request XML and construct an update statement for the instance_info table record with the input info_id.  Issue the update and return a
   * status, depending on success.
   *
   * @param con database connection to use
   * @return status message indicating success or failure
   * @throws Exception
   */
  public static String handleUpdateInstInfo(MVNode nodeCall, Connection con) throws Exception {

    //  parse the components of the update statment from the input XML
    String strInfoId = nodeCall._children[0]._value;
    String strInfoUpdater = nodeCall._children[1]._value;
    String strInfoDate = nodeCall._children[2]._value;
    String strInfoDetail = nodeCall._children[3]._value;

    //  validate the input
    if ("".equals(strInfoUpdater)) {
      return "<error>updater must not be blank</error>";
    }
    if ("".equals(strInfoDetail)) {
      return "<error>update_detail must not be blank</error>";
    }
    try {
      SimpleDateFormat formatDB = new SimpleDateFormat(MVUtil.DB_DATE, Locale.US);
      formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
      formatDB.parse(strInfoDate);
    } catch (Exception e) {
      return "<error>could not parse update_date: '" + strInfoDate + "'</error>";
    }
    int intRes = 0;
    //  construct the update statement for the instance_info table
    try (Statement stmt = con.createStatement();) {
      intRes = stmt.executeUpdate("UPDATE instance_info SET " +
        "updater = '" + strInfoUpdater + "', " +
        "update_date = '" + strInfoDate + "', " +
        "update_detail = '" + strInfoDetail + "' " +
        "WHERE instance_info_id = " + strInfoId + ";");
    }

    //  validate the returned number of updated records
    if (1 != intRes) {
      return "<error>unexpected number of records updated (" + intRes + ") for instance_info_id = " + strInfoId + "</error>";
    }
    return "<update_inst_info>success: updated 1 record</update_inst_info>";
  }

  /**
   * Parse the instance_info_id from the input request XML and determine the name of the load XML file on the web server.  If the file already exists, return
   * it's name.  If not, build it and return its name.
   *
   * @param nodeCall parsed request XML
   * @param con      database connection that load xml will be queried against
   * @return XML message containing the name of the load XML file
   * @throws Exception
   */
  public static String handleViewLoadXML(MVNode nodeCall, Connection con) throws Exception {

    //  parse the instance_info_id value from the input XML
    String strInfoId = nodeCall._children[0]._value;

    //  build the load XML file name
    String strLoadPrefix = "load_" + MVUtil.getDBName(con) + "_id" + strInfoId;
    String strLoadXMLFile = _strPlotXML + "/" + strLoadPrefix + ".xml";

    //  if the file exists, return it's name
    File fileLoadXML = new File(strLoadXMLFile);
    if (fileLoadXML.exists()) {
      return "<view_load_xml>" + strLoadPrefix + "</view_load_xml>";
    }

    //  get the load XML for the specified instance_info_id
    String strLoadXML = "";
    try (Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery("SELECT load_xml FROM instance_info WHERE instance_info_id = " + strInfoId + ";");) {

      while (res.next()) {
        strLoadXML = res.getString(1);
      }
    }

    //  put the load XML from the database into a file
    Document doc = MVPlotJobParser.getDocumentBuilder().parse(new ByteArrayInputStream(strLoadXML.getBytes()));
    //Begin write DOM to file
    File f = new File(strLoadXMLFile);
    try (FileOutputStream stream = new FileOutputStream(f);) {
      DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
      LSSerializer serializer = impl.createLSSerializer();
      LSOutput lso = impl.createLSOutput();
      lso.setByteStream(stream);
      serializer.write(doc, lso);
      stream.flush();
    } catch (Exception e) {
      _logger.error("handleViewLoadXML() - ERROR: caught " + e.getClass() + " serializing load xml: " + e.getMessage());
      return "<error>failed to serialize load xml - reason: " + e.getMessage() + "</error>";
    }

    return "<view_load_xml>" + strLoadPrefix + "</view_load_xml>";
  }

  /**
   * Parse the input node as a plot spec node and select the first of the returned jobs.  Strip the plot_fix field values to create a single plot specification
   * and return the serialized version.
   *
   * @param nodeCall plot spec node to parse
   * @return serialized plot spec of a single plot from the input spec
   * @throws Exception
   */
  public static StringBuilder handleXMLUpload(MVNode nodeCall, Connection con) throws Exception {

    //  run the parser to generate plot jobs
    MVPlotJobParser par = new MVPlotJobParser(nodeCall._children[0], con);
    MVPlotJob[] listJobs = par.getJobsList();
    if (1 > listJobs.length) {
      throw new Exception("parsed XML contained no plot jobs");
    }

    //  process the plot job to serialize
    MVPlotJob job = listJobs[0];

    //  pare down each plot_fix field to a single value or set
    MVOrderedMap mapPlotFix = job.getPlotFixVal();
    job.clearPlotFixVal();
    String[] listFixField = mapPlotFix.getKeyList();
    for (String aListFixField : listFixField) {
      Object objFixVal = mapPlotFix.get(aListFixField);
      if (objFixVal instanceof String[]) {
        String[] listFixVal = (String[]) objFixVal;
        job.addPlotFixVal(aListFixField, new String[]{listFixVal[0]});
      } else if (objFixVal instanceof MVOrderedMap) {
        MVOrderedMap mapFixSet = (MVOrderedMap) objFixVal;
        String[] listFixSetKey = mapFixSet.getKeyList();
        MVOrderedMap mapFixSetSingle = new MVOrderedMap();
        mapFixSetSingle.put(listFixSetKey[0], mapFixSet.get(listFixSetKey[0]));
        job.addPlotFixVal(aListFixField, mapFixSetSingle);
      }
    }
    //  note the upload
    _logger.debug("handleXMLUpload() - plot XML upload for database plot " + job.getDBName());

    //  return the serialized plot XML
    return MVPlotJobParser.serializeJob(job);
  }

  public static String getAvailableResults(String showAll) {
    StringBuilder result = new StringBuilder();
    String dir;
    String extension;
    FilenameFilter filter;
    if (showAll.equals("false")) {
      dir = _strPlots;
      extension = ".png";
      filter = PNG_FILTER;
    } else {
      dir = _strPlotXML;
      extension = ".xml";
      filter = XML_FILTER;
    }

    File plotDir = new File(dir);
    String fileXML;
    String[] plotNames = plotDir.list(filter);
    Arrays.sort(plotNames, Collections.reverseOrder());
    for (String name : plotNames) {

      String success = "true";
      if (dir.equals(_strPlotXML)) {
        //check if the image exists
        File imageFile = new File(_strPlots + "/" + name.replace(extension, "") + ".png");
        if (!imageFile.exists()) {
          success = "false";
        }
      }
      fileXML = "<file name=\"" + name.replace(extension, "").replace("plot_", "") + "\" success=\"" + success + "\" />";
      result.append(fileXML);
    }


    return result.toString();
  }

  /**
   * Read the resource bundle containing database configuration information and initialize the global variables
   */
  public void init() throws ServletException {
    _logger.debug("init() - loading properties...");
    try {
      ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
      _strDBHost = bundle.getString("db.host");
      _strDBUser = bundle.getString("db.user");
      _strDBPassword = bundle.getString("db.password");
      _strDBManagementSystem = bundle.getString("db.managementSystem");
      _strDBDriver = bundle.getString("db.driver");

      _boolListValCache = bundle.getString("cache.val").equals("true");
      _boolListStatCache = bundle.getString("cache.stat").equals("true");

      _strRscript = bundle.getString("rscript.bin");

      _strPlotXML = bundle.getString("folders.plot_xml");
      _strRTmpl = bundle.getString("folders.r_tmpl");
      _strRWork = bundle.getString("folders.r_work");
      _strPlots = bundle.getString("folders.plots");
      _strData = bundle.getString("folders.data");
      _strScripts = bundle.getString("folders.scripts");

      try {
        _strRedirect = bundle.getString("redirect");
      } catch (MissingResourceException e) {
        _strRedirect = "metviewer";
      }
      try {
        _strURLOutput = bundle.getString("url.output");
      } catch (MissingResourceException e) {
        _strURLOutput = "";
      }

    } catch (Exception e) {
      _logger.error("init() - ERROR: caught " + e.getClass() + " loading properties: " + e.getMessage());
    }
    _logger.debug("init() - done loading properties");
  }

  /**
   * Override the parent's doGet() method with a debugging implementation that echoes inputs
   *
   * @param request  Contains request information, including parameters
   * @param response Used to send information back to the requester
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    PrintWriter out = null;
    try {
      //  if the request specifies a database to load, redirect with the appropriate parameter
      String strPath = request.getRequestURL().toString();
      Matcher matDBLoad = _patDBLoad.matcher(strPath);
      if (matDBLoad.matches()) {
        String strDB = matDBLoad.group(1);
        if (!Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).validate(strDB)) {
          printErrorPage(response);
          return;
        }
        //  redirect the user to the web app
        request.getRequestDispatcher(_strRedirect + "/metviewer1.jsp?db=" + matDBLoad.group(1)).forward(request, response);
        return;
      } else {
        Matcher matDownload = _patDownload.matcher(strPath);
        if (matDownload.matches()) {


          String plot;
          String type;
          String filePath = "";

          plot = request.getParameter("plot");

          type = request.getParameter("type");


          switch (type) {
            case "plot_xml_url":
              filePath = _strPlotXML + "/" + plot + ".xml";
              break;
            case "plot_sql_url":
              filePath = _strPlotXML + "/" + plot + ".sql";
              break;
            case "r_script_url":
              filePath = _strScripts + "/" + plot + ".R";
              break;
            case "r_data_url":
              filePath = _strData + "/" + plot + ".data";
              break;
            case "plot_log_url":
              filePath = _strPlotXML + "/" + plot + ".log";
              break;
            case "plot_image_url":
              filePath = _strPlots + "/" + plot + ".png";
              break;
            case "y1_points_url":
              filePath = _strData + "/" + plot + ".points1";
              break;
            case "y2_points_url":
              filePath = _strData + "/" + plot + ".points2";
              break;
            default:
              filePath = _strPlotXML + "/" + plot + ".xml";
              break;
          }
          int length;
          File file = new File(filePath);
          ServletContext context = getServletConfig().getServletContext();
          String mimetype = context.getMimeType(filePath);

          // sets response content type
          if (mimetype == null) {
            mimetype = "application/octet-stream";
          }
          response.setContentType(mimetype);
          response.setContentLength((int) file.length());
          String fileName = new File(filePath).getName();

          // sets HTTP header
          response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

          byte[] byteBuffer = new byte[4096];
          try (ServletOutputStream outStream = response.getOutputStream();
               FileInputStream fileInputStream = new FileInputStream(file);
               DataInputStream in = new DataInputStream(fileInputStream)) {

            // reads the file's bytes and writes them to the response stream
            while ((length = in.read(byteBuffer)) != -1) {
              outStream.write(byteBuffer, 0, length);
            }
          } catch (Exception e) {
            System.out.println(e.getMessage());
          }
          return;
        }

      }

      //  if there is no specified database, print out the list of parameters for debugging
      out = response.getWriter();
      response.setContentType("text/plain");
      out.println("howdy from MVServlet");
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Override the parent's doPost() method with an implementation that reads XML from the body of the request and parses it into one of several commands.  The
   * XML request command is carried out and an XML response is constructed and sent back to the requester.
   *
   * @param request  Contains request information, including parameters
   * @param response Used to send information back to the requester
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    //  initialize the response writer and session
    PrintWriter out = null;
    response.setContentType("text/plain");
    ByteArrayOutputStream s = null;
    BufferedReader reader = null;
    Connection con = null;
    String referer;
    try {

      //  initialize the request information
      String strRequestBody = "";
      request.getSession().setAttribute("init_xml", "");


      //  if the request is a file upload, build the request from the file XML
      if (ServletFileUpload.isMultipartContent(request)) {
        //  set up the upload handler and parse the request
        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> items = uploadHandler.parseRequest(request);
        //  find the upload file in the request and read its contents
        StringBuilder strUploadXML = new StringBuilder();
        for (FileItem item : items) {
          if (!item.isFormField()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(item.getInputStream())) {
              reader = new BufferedReader(inputStreamReader);
              while (reader.ready()) {
                strUploadXML.append(reader.readLine().replaceAll("<\\?.*\\?>", "")).append('\n');
              }
            }
          }
        }

        //  scrub non-xml from the file contents
        strRequestBody = "<request><xml_upload>" + strUploadXML + "</xml_upload></request>";
        String[] refererArr = request.getHeader("referer").split("/");
        referer = refererArr[refererArr.length - 1];
        System.out.println("referer " + referer);

      }
      //  if the request is not a file upload, read it directly
      else {
        String line;
        try {
          reader = request.getReader();
          while ((line = reader.readLine()) != null) {
            strRequestBody = strRequestBody + line;
          }
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }

      }
      _logger.debug("doPost() - request (" + request.getRemoteHost() + "): " + strRequestBody);

      StringBuilder strResp = new StringBuilder();
      if (!strRequestBody.startsWith("<")) {
        String[] simpleRequest = strRequestBody.split("=");
        if (simpleRequest[0].equals("fileUploadLocal") && simpleRequest.length > 1) {
          String runId = simpleRequest[1];
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          File plotXML = new File(_strPlotXML + File.separator + "plot_" + runId + ".xml");
          if (plotXML.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(plotXML);) {
              Document doc = db.parse(fileInputStream);
              Node plot_spec = doc.getDocumentElement();
              Node xml_upload = doc.createElement("xml_upload");
              xml_upload.appendChild(plot_spec);
              MVNode mv_xml_upload = new MVNode(xml_upload);

              if (con == null) {
                con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
              }
              try {
                strResp = handleXMLUpload(mv_xml_upload, con);
              } catch (Exception e) {

              }
            }
            request.getSession().setAttribute("init_xml", strResp.toString().replace("'", "\""));

            request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);
          }
        }
      } else {


        //  instantiate and configure the xml parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(strRequestBody.getBytes());) {
          doc = dbf.newDocumentBuilder().parse(byteArrayInputStream);
        }

        MVNode nodeReq = new MVNode(doc.getFirstChild());

        String currentDBName = "";
        List<String> databases;


        //  examine the children of the request node
        for (int i = 0; i < nodeReq._children.length; i++) {
          MVNode nodeCall = nodeReq._children[i];
          //  <list_db> request
          if (nodeCall._tag.equalsIgnoreCase("list_db")) {
            strResp.append("<list_db>");
            databases = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getAllDatabases();
            for (String database : databases) {
              strResp.append("<val>").append(database).append("</val>");
            }
            strResp.append("<url_output><![CDATA[").append(_strURLOutput).append("]]></url_output>");
            strResp.append("</list_db>");

          } else if (nodeCall._tag.equalsIgnoreCase("list_db_update")) {
            strResp.append("<list_db>");
            Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).initDBList();
            databases = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getAllDatabases();
            for (String database : databases) {
              strResp.append("<val>").append(database).append("</val>");
            }
            strResp.append("</list_db>");
            strResp.append("<url_output><![CDATA[").append(_strURLOutput).append("]]></url_output>");
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            handleClearListValCache(con);
            handleClearListStatCache(con);
          }

          //  <date> tag, which is used to prevent caching
          else if (nodeCall._tag.equalsIgnoreCase("date")) {
            //do nothing
          }
          //  <db_con> node containing the database connection name
          else if (nodeCall._tag.equalsIgnoreCase("db_con")) {

            //  check the connection pool
            currentDBName = nodeCall._value;
            con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection(currentDBName);

          }

          //  <list_val>
          else if (nodeCall._tag.equalsIgnoreCase("list_val")) {
            strResp.append(handleListVal(nodeCall, strRequestBody, con));
          }

          //  <list_stat>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat")) {
            strResp.append(handleListStat(nodeCall, strRequestBody, con));
          }
          //  <list_val_clear_cache>
          else if (nodeCall._tag.equalsIgnoreCase("list_val_clear_cache")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            strResp.append(handleClearListValCache(con));
          }

          //  <list_val_cache_keys>
          else if (nodeCall._tag.equalsIgnoreCase("list_val_cache_keys")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            strResp.append(handleListValCacheKeys(con));
          }

          //  <list_stat_clear_cache>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat_clear_cache")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            strResp.append(handleClearListStatCache(con));
          }

          //  <list_stat_cache_keys>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat_cache_keys")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            strResp.append(handleListStatCacheKeys(con));
          }

          //  <plot>
          else if (nodeCall._tag.equalsIgnoreCase("plot")) {
            strResp.append(handlePlot(strRequestBody, con, currentDBName));
          }
          //  <list_mv_rev>
          else if (nodeCall._tag.equalsIgnoreCase("list_mv_rev")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            strResp.append(handleListMVRev(con));
          }

          //  <list_inst_info>
          else if (nodeCall._tag.equalsIgnoreCase("list_inst_info")) {
            strResp.append(handleListInstInfo(con));
          }

          //  <add_inst_info>
          else if (nodeCall._tag.equalsIgnoreCase("add_inst_info")) {
            strResp.append(handleAddInstInfo(nodeCall, con));
          }

          //  <update_inst_info>
          else if (nodeCall._tag.equalsIgnoreCase("update_inst_info")) {
            strResp.append(handleUpdateInstInfo(nodeCall, con));
          }

          //  <view_load_xml>
          else if (nodeCall._tag.equalsIgnoreCase("view_load_xml")) {
            strResp.append(handleViewLoadXML(nodeCall, con));
          }

          //  <xml_upload>
          else if (nodeCall._tag.equalsIgnoreCase("xml_upload")) {
            if (con == null) {
              con = Datasource.getInstance(_strDBManagementSystem, _strDBDriver, _strDBHost, _strDBUser, _strDBPassword).getConnection();
            }
            try {
              strResp.append(handleXMLUpload(nodeCall, con));
              request.getSession().setAttribute("init_xml", strResp.toString().replace("'", "\""));

              request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);

            } catch (Exception e) {
              strResp.append("<error>could not parse request</error>");
              request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);
            }

          } else if (nodeCall._tag.equalsIgnoreCase("history")) {
            String isShowAll = nodeCall._children[0]._value;
            strResp.append("<results>").append(getAvailableResults(isShowAll)).append("</results>");

          }

          //  not handled
          else {
            strResp.append("<error>unexpected request type: ").append(nodeCall._tag).append("</error>");
          }
        }
      }
      if (strResp.length() == 0) {
        strResp.append("<error>could not parse request</error>");
      }

      _logger.debug("doPost() - response: " + strResp);
      out = response.getWriter();
      out.println(strResp);

    } catch (Exception e) {
      _logger.error("doPost() - caught " + e.getClass() + ": " + e.getMessage() + "\n" + s.toString());
      out.println("<error>caught " + e.getClass() + ": " + e.getMessage() + "</error>");
    } finally {
      if (out != null) {
        out.close();
      }
      if (s != null) {
        s.close();
      }

      if (reader != null) {
        reader.close();
      }

      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          _logger.error(e.getMessage());
        }
      }
    }
  }

}

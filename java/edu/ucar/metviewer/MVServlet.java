package edu.ucar.metviewer;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
  private static final Logger _logger = LogManager.getLogger("MVServlet");
  private static final FilenameFilter PNG_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.toLowerCase(Locale.ENGLISH).endsWith(".png");
    }
  };
  private static final FilenameFilter XML_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.toLowerCase(Locale.ENGLISH).endsWith(".xml");
    }
  };
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
  public static AppDatabaseManager databaseManager;

  /**
   * Clear cached <list_val> values for the input database
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListValCache() throws Exception {
    if (!_boolListValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListValCache);
    return "<list_val_clear_cache>success: removed " + intNumRemoved + " entries</list_val_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_val cache to the log file.
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListValCacheKeys() throws Exception {
    if (!_boolListValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    String[] listKeys = listTableEntriesByKeyPrefix(strKeyPrefix, _tableListValCache);
    String strXML = "";
    String strMsg = "db url: " + databaseManager.getDatabaseInfo().getHost() + "  # keys: " + listKeys.length + "\n";
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
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListStatCache() throws Exception {
    if (!_boolListStatCache) {
      return "<error>caching list_stat caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListStatCache);
    return "<list_stat_clear_cache>success: removed " + intNumRemoved + " entries</list_stat_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_stat cache to the log file.
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListStatCacheKeys() throws Exception {
    if (!_boolListStatCache) {
      return "<error>list_stat caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    String[] listKeys = listTableEntriesByKeyPrefix(strKeyPrefix, _tableListStatCache);
    String strXML = "";
    String strMsg = "db url: " + databaseManager.getDatabaseInfo().getHost() + "  # keys: " + listKeys.length + "\n";
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
   * @return XML response information
   * @throws Exception
   */
  public static String handleListVal(MVNode nodeCall, String requestBody, String currentDBName) throws Exception {
    //  parse the input request, and initialize the response
    StringBuilder strResp = new StringBuilder("<list_val>");
    String strId = nodeCall._children[0]._value;

    strResp.append("<id>").append(strId).append("</id>");

    //  check the list val cache for the request data
    String strCacheKey = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>" +
      requestBody.replaceAll("<id>\\d+</id>", "").replaceAll("<date>\\d+</date>", "");
    if (_boolListValCache && _tableListValCache.containsKey(strCacheKey)) {
      return _tableListValCache.get(strCacheKey).replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
    }
    String strField = nodeCall._children[1]._value.toLowerCase(Locale.ENGLISH);

    List<String> listRes = databaseManager.getListValues(nodeCall, strField, currentDBName);
    String[] listVal = listRes.toArray(new String[]{});
    PrintStream printStream = IoBuilder.forLogger(MVServlet.class)
                                                         .setLevel(org.apache.logging.log4j.Level.INFO)
                                                         .buildPrintStream();
    //  sort and format the results, depending on the field
    if (strField.equals("fcst_thresh") || strField.equals("fcst_thr") ||
      strField.equals("obs_thresh") || strField.equals("obs_thr")) {
      listVal = MVUtil.sortThresh(listVal, printStream);
    } else if (strField.equals("fcst_lev") || strField.equals("obs_lev")) {
      listVal = MVUtil.sortLev(listVal,printStream);
    } else if (strField.equals("fcst_lead") || strField.equals("obs_lead")) {
      listVal = MVUtil.sortFormatLead(listVal, true, false);
    } else if (strField.equals("init_hour") || strField.equals("valid_hour")) {
      listVal = MVUtil.sortHour(listVal, true);
    } else if (strField.equals("fcst_valid") || strField.equals("fcst_init") || strField.equals("obs_valid")) {
      listVal = MVUtil.formatDates(listVal);
    }
    printStream.close();

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


    strResp.append("</list_val>");
    _tableListValCache.put(strCacheKey, strResp.toString());
    return strResp.toString();
  }

  /**
   * List statistics for the specified fcst_var, using cached data if appropriate, and return it in serialized XML
   *
   * @param nodeCall    MVNode containing request information
   * @param requestBody the XML sent by the client which is used to cache the response
   * @return XML response information
   * @throws Exception
   */
  public static String handleListStat(MVNode nodeCall, String requestBody, String currentDBName) throws Exception {
    //  if the request is for the mode stats, return the static list
    String strId = nodeCall._children[0]._value;
    String strFcstVar = nodeCall._children[1]._value;
    if (nodeCall._children[0]._tag.equals("mode_fcst_var")) {
      String strResp = "<list_stat><id>" + strId + "</id></list_stat>";
      _logger.debug("handleListStat() - returning mode stats: " + strResp);
      return strResp;
    }

    //  check the list val cache for the request data
    String strCacheKey = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>" +
      requestBody.replaceAll("<id>\\d+</id>", "").replaceAll("<date>\\d+</date>", "");
    if (_boolListStatCache) {
      _logger.debug("handleListStat() - checking cache for key " + strCacheKey + ": " + _tableListStatCache.containsKey(strCacheKey));
      if (_tableListStatCache.containsKey(strCacheKey)) {
        String strListStat = _tableListStatCache.get(strCacheKey).replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
        _logger.debug("handleListStat() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListStat);
        return strListStat;
      }
    }

    List<String> listStatName = databaseManager.getListStat(strFcstVar, currentDBName);


    StringBuilder strResp = new StringBuilder("<list_stat><id>" + strId + "</id>");

    //  sort and build the response string using the list of stat names
    String[] listStat = MVUtil.toArray(listStatName);
    Arrays.sort(listStat, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }
    });
    for (String aListStat : listStat) {
      strResp.append("<val>").append(aListStat).append("</val>");
    }

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
   * @return status message
   */
  public static String handlePlot(String strRequest, String currentDBName) throws Exception {

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
        "<host>" + databaseManager.getDatabaseInfo().getHost() + "</host>" +
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
      parser = new MVPlotJobParser(byteArrayInputStream, currentDBName);
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

    ByteArrayOutputStream logSql = null;
    PrintStream printStreamSql = null;


    String strJobTmpl = job.getPlotTmpl();
    String strRErrorMsg = "";
    FileWriter writer = null;
    try {
      log = new ByteArrayOutputStream();
      logSql = new ByteArrayOutputStream();
      printStream = new PrintStream(log);
      printStreamSql = new PrintStream(logSql);
      MVBatch bat = new MVBatch(printStream, printStreamSql, databaseManager);
      bat._intNumPlots = 1;
      //  configure the batch engine and run the job
      bat._intNumPlots = 1;
      bat._strRtmplFolder = parser.getRtmplFolder();
      bat._strRworkFolder = parser.getRworkFolder();
      bat._strPlotsFolder = parser.getPlotsFolder();
      bat._strDataFolder = parser.getDataFolder();
      bat._strScriptsFolder = parser.getScriptsFolder();


      //  run the job to generate the plot
      runTargetedJob(job, strJobTmpl, bat);
      //  build the job SQL using the batch engine
      String strPlotSQL = logSql.toString();

      //  write the plot SQL to a file
      writer = new FileWriter(_strPlotXML + "/" + strPlotPrefix + ".sql");
      writer.write(strPlotSQL);
      writer.close();
      logSql.reset();

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
      if (logSql != null) {
        logSql.close();
      }
      if (printStreamSql != null) {
        printStreamSql.close();
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
      bat.runHistJob(job);
    } else if (strJobTmpl.equals("phist.R_tmpl")) {
      bat.runHistJob(job);
    } else if (strJobTmpl.equals("relp.R_tmpl")) {
      bat.runHistJob(job);
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
   * Parse the input node as a plot spec node and select the first of the returned jobs.  Strip the plot_fix field values to create a single plot specification
   * and return the serialized version.
   *
   * @param nodeCall plot spec node to parse
   * @return serialized plot spec of a single plot from the input spec
   * @throws Exception
   */
  public static StringBuilder handleXMLUpload(MVNode nodeCall) throws Exception {

    //  run the parser to generate plot jobs
    MVPlotJobParser par = new MVPlotJobParser(nodeCall._children[0]);
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


    //  return the serialized plot XML
    return MVPlotJobParser.serializeJob(job, databaseManager.getDatabaseInfo());
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
  @Override
  public void init() throws ServletException {
    _logger.debug("init() - loading properties...");
    try {
      ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
      DatabaseInfo databaseInfo = new DatabaseInfo();
      databaseInfo.setHost(bundle.getString("db.host"));
      databaseInfo.setUser(bundle.getString("db.user"));
      databaseInfo.setPassword(bundle.getString("db.password"));
      if (bundle.getString("db.managementSystem").equals("mysql")) {
        databaseManager = new MysqlAppDatabaseManager(databaseInfo);
      }

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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    PrintWriter out = null;
    try {
      //  if the request specifies a database to load, redirect with the appropriate parameter
      String strPath = request.getRequestURL().toString();
      Matcher matDBLoad = _patDBLoad.matcher(strPath);
      if (matDBLoad.matches()) {
        String strDB = matDBLoad.group(1);
        if (!databaseManager.validate(strDB)) {
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
            _logger.error(e.getMessage());
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
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    //  initialize the response writer and session
    PrintWriter out = null;
    response.setContentType("text/plain");
    ByteArrayOutputStream s = null;
    BufferedReader reader = null;
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
        _logger.debug("referer " + referer);

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
          _logger.error(e.getMessage());
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

              try {
                strResp = handleXMLUpload(mv_xml_upload);
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
            databases = databaseManager.getAllDatabases();
            for (String database : databases) {
              strResp.append("<val>").append(database).append("</val>");
            }
            strResp.append("<url_output><![CDATA[").append(_strURLOutput).append("]]></url_output>");
            strResp.append("</list_db>");

          } else if (nodeCall._tag.equalsIgnoreCase("list_db_update")) {
            strResp.append("<list_db>");
            databaseManager.initDBList();
            databases = databaseManager.getAllDatabases();
            for (String database : databases) {
              strResp.append("<val>").append(database).append("</val>");
            }
            strResp.append("</list_db>");
            strResp.append("<url_output><![CDATA[").append(_strURLOutput).append("]]></url_output>");

            handleClearListValCache();
            handleClearListStatCache();
          }

          //  <date> tag, which is used to prevent caching
          else if (nodeCall._tag.equalsIgnoreCase("date")) {
            //do nothing
          }
          //  <db_con> node containing the database connection name
          else if (nodeCall._tag.equalsIgnoreCase("db_con")) {
            currentDBName = nodeCall._value;
          }

          //  <list_val>
          else if (nodeCall._tag.equalsIgnoreCase("list_val")) {
            strResp.append(handleListVal(nodeCall, strRequestBody, currentDBName));
          }

          //  <list_stat>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat")) {
            strResp.append(handleListStat(nodeCall, strRequestBody, currentDBName));
          }
          //  <list_val_clear_cache>
          else if (nodeCall._tag.equalsIgnoreCase("list_val_clear_cache")) {

            strResp.append(handleClearListValCache());
          }

          //  <list_val_cache_keys>
          else if (nodeCall._tag.equalsIgnoreCase("list_val_cache_keys")) {

            strResp.append(handleListValCacheKeys());
          }

          //  <list_stat_clear_cache>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat_clear_cache")) {

            strResp.append(handleClearListStatCache());
          }

          //  <list_stat_cache_keys>
          else if (nodeCall._tag.equalsIgnoreCase("list_stat_cache_keys")) {
            strResp.append(handleListStatCacheKeys());
          }

          //  <plot>
          else if (nodeCall._tag.equalsIgnoreCase("plot")) {
            strResp.append(handlePlot(strRequestBody, currentDBName));
          }


          //  <xml_upload>
          else if (nodeCall._tag.equalsIgnoreCase("xml_upload")) {

            try {
              strResp.append(handleXMLUpload(nodeCall));
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

    }
  }

}

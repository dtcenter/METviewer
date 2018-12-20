package edu.ucar.metviewer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseManager;
import edu.ucar.metviewer.jobManager.ContourJobManager;
import edu.ucar.metviewer.jobManager.EclvJobManager;
import edu.ucar.metviewer.jobManager.EnsSsJobManager;
import edu.ucar.metviewer.jobManager.JobManager;
import edu.ucar.metviewer.jobManager.PerformanceJobManager;
import edu.ucar.metviewer.jobManager.RelpJobManager;
import edu.ucar.metviewer.jobManager.RelyJobManager;
import edu.ucar.metviewer.jobManager.RhistJobManager;
import edu.ucar.metviewer.jobManager.RocJobManager;
import edu.ucar.metviewer.jobManager.SeriesJobManager;
import edu.ucar.metviewer.jobManager.TaylorJobManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MVServlet extends HttpServlet {

  public static final PrintStream errorStream
      = IoBuilder.forLogger(MVUtil.class).setLevel(org.apache.logging.log4j.Level.INFO)
            .setMarker(new MarkerManager.Log4jMarker("ERROR"))
            .buildPrintStream();

  public static final Pattern patDBLoad = Pattern.compile(".*/db/([\\w\\d]+)$");
  public static final Pattern patDownload = Pattern.compile(".*/download");
  public static final String DELIMITER = File.separator;
  protected static final Map<String, String> valCache = new HashMap<>();
  protected static final Map<String, String> statCache = new HashMap<>();
  private static final String DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";
  private static final long serialVersionUID = 1L;
  private static final Logger logger = LogManager.getLogger("MVServlet");
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
  public static String plotXml = "";
  public static String rTmpl = "";
  public static String rWork = "";
  public static String plots = "";
  public static String rscript = "";
  public static String redirect = "";
  public static String urlOutput = "";
  public static String data = "";
  public static String scripts = "";
  public static String managementSystem = "";
  public static boolean isValCache = false;
  public static boolean isStatCache = false;
  public static AppDatabaseManager databaseManager;

  /**
   * Clear cached <list_val> values for the input database
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListValCache() {
    if (!isValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, valCache);
    return "<list_val_clear_cache>success: removed "
               + intNumRemoved + " entries</list_val_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_val cache to the log file.
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListValCacheKeys() {
    if (!isValCache) {
      return "<error>list_val caching not activated</error>";
    }
    String strKeyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    String[] listKeys = listTableEntriesByKeyPrefix(strKeyPrefix, valCache);
    String strXml = "";
    String strMsg = "db url: " + databaseManager.getDatabaseInfo()
                                     .getHost() + "  # keys: " + listKeys.length + "\n";
    for (String listKey : listKeys) {
      strMsg += "  " + listKey + "\n";
      strXml += "<key>" + listKey + "</key>";
    }
    logger.debug("handleListValCacheKeys() - " + strMsg);
    return "<list_val_cache_keys>" + strXml + "</list_val_cache_keys>";
  }

  /**
   * Clear cached <list_stat> values for the input database
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleClearListStatCache() {
    if (!isStatCache) {
      return "<error>caching list_stat caching not activated</error>";
    }
    String keyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    int intNumRemoved = removeTableEntriesByKeyPrefix(keyPrefix, statCache);
    return "<list_stat_clear_cache>success: removed "
               + intNumRemoved + " entries</list_stat_clear_cache>";
  }

  /**
   * Dump out all the keys of the list_stat cache to the log file.
   *
   * @return reponse XML indicating progress
   * @throws Exception
   */
  public static String handleListStatCacheKeys() {
    if (!isStatCache) {
      return "<error>list_stat caching not activated</error>";
    }
    String keyPrefix = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>";
    String[] keys = listTableEntriesByKeyPrefix(keyPrefix, statCache);
    String strXml = "";
    String strMsg = "db url: " + databaseManager.getDatabaseInfo()
                                     .getHost() + "  # keys: " + keys.length + "\n";
    for (String listKey : keys) {
      strMsg += "  " + listKey + "\n";
      strXml += "<key>" + listKey + "</key>";
    }
    logger.debug("handleListStatCacheKeys() - " + strMsg);
    return "<list_stat_cache_keys>" + strXml + "</list_stat_cache_keys>";
  }

  /**
   * Searches all key values of the input table, which are assumed to be Strings, and removes any
   * entry whose key matches the specified prefix
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
   * Searches all key values of the input table, which are assumed to be Strings, and removes any
   * entry whose key matches the specified prefix
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
   * List database values for the specified field with the specified constraints, using cached data
   * if appropriate, and return it in serialized XML
   *
   * @param nodeCall    MVNode containing request information
   * @param requestBody the XML sent by the client which is used to cache the response
   * @return XML response information
   * @throws Exception
   */
  public static String handleListVal(
      MVNode nodeCall, String requestBody,
      String[] currentDbName) throws Exception {
    //  parse the input request, and initialize the response
    StringBuilder strResp = new StringBuilder("<list_val>");
    String strId = nodeCall.children[0].value;

    strResp.append("<id>").append(strId).append("</id>");

    //  check the list val cache for the request data
    String strCacheKey = "<db>" + databaseManager.getDatabaseInfo().getHost() + "</db>"
                             + requestBody.replaceAll("<id>\\d+</id>", "")
                                   .replaceAll("<date>\\d+</date>", "");
    if (isValCache && valCache.containsKey(strCacheKey)) {
      return valCache.get(strCacheKey)
                 .replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
    }
    String strField = nodeCall.children[1].value.toLowerCase(Locale.ENGLISH);

    List<String> listRes = databaseManager.getListValues(nodeCall, strField, currentDbName);


    //  sort and format the results, depending on the field
    if (strField.equals("fcst_thresh") || strField.equals("fcst_thr")
            || strField.equals("obs_thresh") || strField.equals("obs_thr")) {
      listRes = MVUtil.sortThresh(listRes);
    } else if (strField.equals("fcst_lev") || strField.equals("obs_lev")) {
      listRes = MVUtil.sortLev(listRes);
    } else if (strField.equals("interp_pnts")) {
      listRes = MVUtil.sortInterpPnts(listRes);
    } else if (strField.equals("fcst_lead") || strField.equals("obs_lead")) {
      listRes = MVUtil.sortFormatLead(listRes, true, false);
    } else if (strField.equals("init_hour") || strField.equals("valid_hour")) {
      listRes = MVUtil.sortHour(listRes, true);
    } else if (strField.equals("fcst_valid") || strField.equals("fcst_init") || strField.equals(
        "obs_valid")) {
      listRes = MVUtil.formatDates(listRes);
    }


    //  add the list of field values to the response
    for (String aListVal : listRes) {

      //  add the database field value to the list
      strResp.append("<val>")
          .append(aListVal.replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;"))
          .append("</val>");

    }


    strResp.append("</list_val>");
    valCache.put(strCacheKey, strResp.toString());
    return strResp.toString();
  }

  /**
   * List statistics for the specified fcst_var, using cached data if appropriate, and return it in
   * serialized XML
   *
   * @param nodeCall    MVNode containing request information
   * @param requestBody the XML sent by the client which is used to cache the response
   * @return XML response information
   * @throws Exception
   */
  public static String handleListStat(
      MVNode nodeCall, String requestBody,
      String[] currentDBName) throws Exception {
    //  if the request is for the mode stats, return the static list
    String strId = nodeCall.children[0].value;
    String strFcstVar = nodeCall.children[1].value;
    if (strFcstVar.equals("undefined")) {
      return "<list_stat><id>" + strId + "</id></list_stat>";
    }
    if (nodeCall.children[0].tag.equals("mode_fcst_var")) {
      String strResp = "<list_stat><id>" + strId + "</id></list_stat>";
      logger.debug("handleListStat() - returning mode stats: " + strResp);
      return strResp;
    }

    //  check the list val cache for the request data
    String strCacheKey = "<db>"
                             + databaseManager.getDatabaseInfo().getHost()
                             + "</db>"
                             + requestBody.replaceAll("<id>\\d+</id>", "")
                                   .replaceAll("<date>\\d+</date>", "");
    if (isStatCache) {
      logger.debug(
          "handleListStat() - checking cache for key " + strCacheKey
              + ": " + statCache.containsKey(strCacheKey));
      if (statCache.containsKey(strCacheKey)) {
        String strListStat = statCache.get(strCacheKey)
                                 .replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>");
        logger.debug(
            "handleListStat() - returning cached value\n  key: "
                + strCacheKey + "\n  val: " + strListStat);
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
    statCache.put(strCacheKey, strResp.toString());
    return strResp.toString();
  }

  /**
   * Save and parse the plot request XML, then generate the requested plot and return information
   * about it. Update the web_plot table in the database from which the plot data was drawn.
   *
   * @param strRequest XML plot specification
   * @return status message
   */
  public static String handlePlot(String strRequest, String[] currentDBName) throws Exception {

    //  extract the plot xml from the request
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String strPlotXML = strRequest;
    strPlotXML = strPlotXML.substring(strPlotXML.indexOf("</db_con>") + 9);
    strPlotXML = strPlotXML.substring(0, strPlotXML.indexOf("</request>"));
    String plotPrefix;
    SimpleDateFormat formatPlot = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);
    plotPrefix = "plot_" + formatPlot.format(new Date());
    String databases = "";
    for (String database : currentDBName) {
      databases = databases + database + ",";
    }
    databases = databases.substring(0, databases.length() - 1);
    //  add plot file information to the plot spec
    strPlotXML =
        "<plot_spec>"
            + "<connection>"
            + "<host>" + databaseManager.getDatabaseInfo().getHost() + "</host>"
            + "<database>" + databases + "</database>"
            + "<user>" + "******" + "</user>"
            + "<password>" + "******" + "</password>"
            + "<management_system>" + managementSystem + "</management_system>"
            + "</connection>"
            + (rscript.equals("") ? "" : "<rscript>" + rscript + "</rscript>")
            + "<folders>"
            + "<r_tmpl>" + rTmpl + "</r_tmpl>"
            + "<r_work>" + rWork + "</r_work>"
            + "<plots>" + plots + "</plots>"
            + "<data>" + data + "</data>"
            + "<scripts>" + scripts + "</scripts>"
            + "</folders>"
            + strPlotXML
            + "</plot_spec>";

    strPlotXML = strPlotXML.replace("<tmpl>",
                                    "<tmpl>"
                                        + "<data_file>" + plotPrefix + ".data" + "</data_file>"
                                        + "<plot_file>" + plotPrefix + ".png" + "</plot_file>"
                                        + "<r_file>" + plotPrefix + ".R" + "</r_file>");


    //  parse the input plot job
    MVPlotJobParser parser;
    MVPlotJob job;
    try (ByteArrayInputStream byteArrayInputStream
             = new ByteArrayInputStream(strPlotXML.getBytes())) {
      parser = new MVPlotJobParser(byteArrayInputStream);
      MVPlotJob[] jobs = parser.getJobsList();
      if (1 != jobs.length) {
        throw new Exception("unexpected number of plot jobs generated: " + jobs.length);
      }
      job = jobs[0];
    } catch (Exception e) {

      errorStream.print(
          "handlePlot() - ERROR: caught " + e.getClass() + " parsing plot job: " + e.getMessage());
      return "<error>failed to parse plot job - reason: " + e.getMessage() + "</error>";
    }

    //  run the plot job and write the batch output to the log file
    ByteArrayOutputStream log = null;
    PrintStream printStream = null;

    ByteArrayOutputStream logSql = null;
    ByteArrayOutputStream logError = null;
    PrintStream printStreamSql = null;
    PrintStream printStreamError = null;


    String strRErrorMsg = "";
    try {
      log = new ByteArrayOutputStream();
      logSql = new ByteArrayOutputStream();
      logError = new ByteArrayOutputStream();
      printStream = new PrintStream(log);
      printStreamSql = new PrintStream(logSql);
      printStreamError = new PrintStream(logError);
      MVBatch mvBatch = new MVBatch(printStream, printStreamSql, printStreamError, databaseManager);
      //  configure the batch engine and run the job
      mvBatch.setRtmplFolder(parser.getRtmplFolder());
      mvBatch.setRworkFolder(parser.getRworkFolder());
      mvBatch.setPlotsFolder(parser.getPlotsFolder());
      mvBatch.setDataFolder(parser.getDataFolder());
      mvBatch.setScriptsFolder(parser.getScriptsFolder());

      //change timestamp job name to the custom if exists
      if (!job.getJobTitleTmpl().isEmpty()) {
        String title;
        if (!job.getKeepRevisions()) {
          title = job.getJobTitleTmpl();
        } else {
          //find file in plot xml
          String[] similarNames =
              new File(plotXml).list(new MVUtil.NameFilter(job.getJobTitleTmpl()));
          if (similarNames == null || similarNames.length == 0) {
            //no jobs with the same name
            title = job.getJobTitleTmpl();
          } else {
            title = job.getJobTitleTmpl() + "(" + similarNames.length + ")";
          }
        }
        String plotPrefixCustom = "plot_" + title;
        job.setDataFileTmpl(plotPrefixCustom + ".data");
        job.setPlotFileTmpl(plotPrefixCustom + ".png");
        job.setRFileTmpl(plotPrefixCustom + ".R");
        strPlotXML = strPlotXML.replace(plotPrefix, plotPrefixCustom);
        plotPrefix = plotPrefixCustom;
      }
      //  parse the input document and build the MVNode data structure
      Document doc = MVPlotJobParser.getDocumentBuilder()
                         .parse(new ByteArrayInputStream(strPlotXML.getBytes()));
      StreamResult streamResult;
      File f = new File(plotXml + DELIMITER + plotPrefix + ".xml");
      try (FileOutputStream stream = new FileOutputStream(f);
           OutputStreamWriter outputStreamWriter
               = new OutputStreamWriter(stream, StandardCharsets.UTF_8);) {
        //Begin write DOM to file

        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");

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
        logger.error(
            "handlePlot() - ERROR: caught " + e.getClass() + " serializing plot xml: "
                + e.getMessage());
        return "<error>failed to serialize plot xml - reason: " + e.getMessage() + "</error>";
      }

      //  run the job to generate the plot
      runTargetedJob(job, mvBatch);



      String plotterOutput = log.toString();
      //  parse out R error messages, if present, throwing an exception if the error was fatal
      Matcher matOutput = Pattern.compile(
          "(?sm)(==== Start Rscript error  ====.*====   End Rscript error  ====)")
                              .matcher(plotterOutput);
      if (matOutput.find()) {
        strRErrorMsg = matOutput.group(1);
      }
      if (plotterOutput.contains("Execution halted")) {
        throw new Exception("R error");
      }
      if (plotterOutput.contains("query returned no data")) {
        throw new Exception("query returned no data");
      }

    } catch (Exception e) {
      stopWatch.stop();
      errorStream.print(
          "handlePlot() - ERROR: caught " + e.getClass()
              + " running plot: " + e.getMessage() + "\nbatch output:\n" + log.toString());
      String strPlotterOutput = log.toString();
      try (FileWriter fileWriter = new FileWriter(plotXml + DELIMITER + plotPrefix + ".log")) {
        fileWriter.write(strPlotterOutput);
      }
      strRErrorMsg = strRErrorMsg.replace("&", "&amp;").replace("<", "&lt;")
                         .replace(">", "&gt;");
      String message = e.getMessage().replace("&", "&amp;").replace("<", "&lt;")
                           .replace(">", "&gt;");
      return "<response><error>"
                 + "failed to run plot " + plotPrefix + " - reason: " + message
                 + (!strRErrorMsg.equals("") ? ":\n" + strRErrorMsg : "")
                 + "</error><plot>" + plotPrefix + "</plot></response>";
    } finally {

      stopWatch.stop();
      if(logSql != null) {
        //  build the job SQL using the batch engine
        String plotSql = logSql.toString();

        //  write the plot SQL to a file
        try (FileWriter fileWriter = new FileWriter(plotXml + DELIMITER + plotPrefix + ".sql")) {
          fileWriter.write(plotSql);
        }
        logSql.reset();
      }


      if (log != null && printStream != null) {
        printStream.println("\nTotal execution time " + stopWatch.getFormattedTotalDuration());
        String plotterOutput = log.toString();

        try (FileWriter fileWriter = new FileWriter(plotXml + DELIMITER + plotPrefix + ".log")) {
          fileWriter.write(plotterOutput);
        }

      }
      if (log != null) {
        log.close();
      }
      if(printStream!= null){
        printStream.close();
      }

      if (logSql != null) {
        logSql.flush();
        logSql.close();
      }
      if (logError != null) {
        logError.flush();
        logError.close();
      }
      if (printStreamSql != null) {
        printStreamSql.flush();
        printStreamSql.close();
      }
      if (printStreamError != null) {
        printStreamError.close();
      }

    }
    logger.debug("handlePlot() - batch output:\n" + log.toString());

    return "<response><plot>" + plotPrefix
               + "</plot>"
               + (!strRErrorMsg.equals("")
                      ? "<r_error>"
                            + strRErrorMsg.replace("&", "&amp;").replace("<", "&lt;")
                                  .replace(">", "&gt;")
                            + "</r_error></response>" : "</response>");
  }

  private static void runTargetedJob(MVPlotJob job, MVBatch bat) throws Exception {
    JobManager jobManager;
    switch (job.getPlotTmpl()) {
      case "rhist.R_tmpl":
        jobManager = new RhistJobManager(bat);
        break;
      case "phist.R_tmpl":
        jobManager = new RhistJobManager(bat);
        break;
      case "relp.R_tmpl":
        jobManager = new RelpJobManager(bat);
        break;
      case "roc.R_tmpl":
        jobManager = new RocJobManager(bat);
        break;
      case "rely.R_tmpl":
        jobManager = new RelyJobManager(bat);
        break;
      case "eclv.R_tmpl":
        jobManager = new EclvJobManager(bat);
        break;
      case "taylor_plot.R_tmpl":
        jobManager = new TaylorJobManager(bat);
        break;
      case "performance.R_tmpl":
        jobManager = new PerformanceJobManager(bat);
        break;
      case "ens_ss.R_tmpl":
        jobManager = new EnsSsJobManager(bat);
        break;
      case "contour_plot.R_tmpl":
        jobManager = new ContourJobManager(bat);
        break;
      default:
        jobManager = new SeriesJobManager(bat);
        break;
    }
    jobManager.runJob(job);

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
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n\n"
            + "<html>\n"
            + "<head>\n"
            + "<title>METviewer Error</title>\n"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\""
            + redirect + "/include/metviewer.css\"/>\n"
            + "<link rel=\"shortcut icon\" href=\""
            + redirect + "/include/ral_icon.ico\" type=\"image/x-icon\"/>\n"
            + "</head>\n"
            + "<body style=\"padding-left:20px; padding-top:20px\">\n"
            + "<span class=\"bold\">An error has occurred in METviewer."
            + "  Please contact your system administrator</span>\n"
            + "</body></html>\n\n");
  }


  /**
   * Parse the input node as a plot spec node and select the first of the returned jobs.  Strip the
   * plot_fix field values to create a single plot specification and return the serialized version.
   *
   * @param nodeCall plot spec node to parse
   * @return serialized plot spec of a single plot from the input spec
   * @throws Exception
   */
  public static StringBuilder handleXmlUpload(MVNode nodeCall) throws Exception {

    //  run the parser to generate plot jobs
    MVPlotJobParser par = new MVPlotJobParser(nodeCall.children[0]);
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

  private static String getAvailableResults(String showAll) {
    StringBuilder result = new StringBuilder();
    String dir;
    String extension;
    FilenameFilter filter;
    if (showAll.equals("false")) {
      dir = plots;
      extension = ".png";
      filter = PNG_FILTER;
    } else {
      dir = plotXml;
      extension = ".xml";
      filter = XML_FILTER;
    }

    File plotDir = new File(dir);
    String fileXml;
    File[] files = plotDir.listFiles(filter);
    if (files != null) {
      Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
      for (File file : files) {

        String success = "true";
        if (dir.equals(plotXml)) {
          //check if the image exists
          File imageFile = new File(
              plots + DELIMITER + file.getName().replace(extension, "") + ".png");
          if (!imageFile.exists()) {
            success = "false";
          }
        }
        fileXml = "<file name=\"" + file.getName().replace(extension, "")
                                        .replace("plot_", "") + "\" success=\"" + success + "\" />";
        result.append(fileXml);
      }
    }

    return result.toString();
  }

  /**
   * Read the resource bundle containing database configuration information and initialize the
   * global variables
   */
  @Override
  public void init() throws ServletException {
    logger.debug("init() - loading properties...");
    try {
      ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");

      managementSystem = bundle.getString("db.managementSystem");
      databaseManager = (AppDatabaseManager) DatabaseManager.getAppManager(
          managementSystem,
          bundle.getString("db.host"),
          bundle.getString("db.user"),
          bundle.getString("db.password"));

      isValCache = bundle.getString("cache.val").equals("true");
      isStatCache = bundle.getString("cache.stat").equals("true");

      rscript = bundle.getString("rscript.bin");

      plotXml = bundle.getString("folders.plot_xml");
      rTmpl = bundle.getString("folders.r_tmpl");
      rWork = bundle.getString("folders.r_work");
      plots = bundle.getString("folders.plots");
      data = bundle.getString("folders.data");
      scripts = bundle.getString("folders.scripts");

      try {
        redirect = bundle.getString("redirect");
      } catch (MissingResourceException e) {
        redirect = "metviewer";
      }
      try {
        urlOutput = bundle.getString("url.output");
      } catch (MissingResourceException e) {
        urlOutput = "";
      }

    } catch (Exception e) {
      errorStream.print(
          "init() - ERROR: caught " + e.getClass() + " loading properties: " + e.getMessage());
    }
    logger.debug("init() - done loading properties");
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
    try {
      //  if the request specifies a database to load, redirect with the appropriate parameter
      String strPath = request.getRequestURL().toString();
      Matcher matDbLoad = patDBLoad.matcher(strPath);
      if (matDbLoad.matches()) {
        String strDb = matDbLoad.group(1);
        if (!databaseManager.validate(strDb)) {
          printErrorPage(response);
          return;
        }
        //  redirect the user to the web app
        request.getRequestDispatcher(redirect + "/metviewer1.jsp?db=" + matDbLoad.group(1))
            .forward(request, response);
        return;
      } else {
        Matcher matDownload = patDownload.matcher(strPath);
        if (matDownload.matches()) {


          String plot;
          String type;
          String filePath = "";

          plot = request.getParameter("plot");

          type = request.getParameter("type");


          switch (type) {
            case "plot_xml_url":
              filePath = plotXml + DELIMITER + plot + ".xml";
              break;
            case "plot_sql_url":
              filePath = plotXml + DELIMITER + plot + ".sql";
              break;
            case "r_script_url":
              filePath = scripts + DELIMITER + plot + ".R";
              break;
            case "r_data_url":
              filePath = data + DELIMITER + plot + ".data";
              break;
            case "plot_log_url":
              filePath = plotXml + DELIMITER + plot + ".log";
              break;
            case "plot_image_url":
              filePath = plots + DELIMITER + plot + ".png";
              break;
            case "y1_points_url":
              filePath = data + DELIMITER + plot + ".points1";
              break;
            case "y2_points_url":
              filePath = data + DELIMITER + plot + ".points2";
              break;
            default:
              filePath = plotXml + DELIMITER + plot + ".xml";
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
            errorStream.print(e.getMessage());
          }
          return;
        }

      }

      //  if there is no specified database, print out the list of parameters for debugging
      try (PrintWriter printWriter = response.getWriter()) {
        response.setContentType("text/plain");
        printWriter.println("howdy from MVServlet");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Override the parent's doPost() method with an implementation that reads XML from the body of
   * the request and parses it into one of several commands.  The XML request command is carried out
   * and an XML response is constructed and sent back to the requester.
   *
   * @param request  Contains request information, including parameters
   * @param response Used to send information back to the requester
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    //  initialize the response writer and session
    PrintWriter out = null;

    ByteArrayOutputStream byteArrayOutputStream = null;
    BufferedReader reader = null;
    String referer;
    try {

      //  initialize the request information
      StringBuilder requestBody = new StringBuilder();
      request.getSession().setAttribute("init_xml", "");


      //  if the request is a file upload, build the request from the file XML
      if (ServletFileUpload.isMultipartContent(request)) {
        //  set up the upload handler and parse the request
        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> items = uploadHandler.parseRequest(request);
        //  find the upload file in the request and read its contents
        StringBuilder uploadXml = new StringBuilder();
        for (FileItem item : items) {
          if (!item.isFormField()) {
            try (InputStreamReader inputStreamReader
                     = new InputStreamReader(item.getInputStream())) {
              reader = new BufferedReader(inputStreamReader);
              while (reader.ready()) {
                uploadXml.append(reader.readLine().replaceAll("<\\?.*\\?>", "")).append('\n');
              }
            }
          }
        }

        //  scrub non-xml from the file contents
        requestBody = new StringBuilder(
            "<request><xml_upload>" + uploadXml + "</xml_upload></request>");
        response.setContentType("application/xml");
        String[] refererArr = request.getHeader("referer").split(DELIMITER);
        referer = refererArr[refererArr.length - 1];
        logger.debug("referer " + referer);

      } else {
        //  if the request is not a file upload, read it directly
        String line;
        try {
          reader = request.getReader();
          while ((line = reader.readLine()) != null) {
            requestBody.append(line);
          }
          response.setContentType("application/xml");
        } catch (Exception e) {
          errorStream.print(e.getMessage());
        }

      }
      logger.debug("doPost() - request (" + request.getRemoteHost() + "): " + requestBody);

      StringBuilder strResp = new StringBuilder();
      if (!requestBody.toString().startsWith("<")) {
        String[] simpleRequest = requestBody.toString().split("=");
        if (simpleRequest[0].equals("fileUploadLocal") && simpleRequest.length > 1) {
          String runId = simpleRequest[1].replace("%28", "(").replace("%29", ")");
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          File fileXml = new File(MVServlet.plotXml + File.separator + "plot_" + runId + ".xml");
          if (fileXml.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(fileXml);) {
              Document doc = db.parse(fileInputStream);
              Node plotSpec = doc.getDocumentElement();
              Node xmlUpload = doc.createElement("xml_upload");
              xmlUpload.appendChild(plotSpec);
              MVNode mvXmlUpload = new MVNode(xmlUpload);

              try {
                strResp = handleXmlUpload(mvXmlUpload);
              } catch (Exception e) {
                errorStream.print(e.getMessage());
              }
            }
            request.getSession().setAttribute("init_xml", strResp.toString().replace("'", "\""));

            request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);
          }
        }
      } else {


        //  instantiate and configure the xml parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        dbf.setNamespaceAware(true);
        Document doc;
        try (ByteArrayInputStream byteArrayInputStream
                 = new ByteArrayInputStream(requestBody.toString().getBytes())) {
          doc = dbf.newDocumentBuilder().parse(byteArrayInputStream);
        }

        MVNode nodeReq = new MVNode(doc.getFirstChild());

        String[] currentDbName = null;
        Map<String, String> databases;
        Map<String, List<String>> groups;


        //  examine the children of the request node
        for (int i = 0; i < nodeReq.children.length; i++) {
          MVNode nodeCall = nodeReq.children[i];
          //  <list_db> request
          if (nodeCall.tag.equalsIgnoreCase("list_db")) {
            strResp.append("<list_db>");
            databases = databaseManager.getAllDatabasesWithDescription();
            groups = databaseManager.getAllGroups();
            StringBuilder groupsXmlStr = new StringBuilder("<groups>");
            SortedSet<String> keys = new TreeSet<>(groups.keySet());

            for (String key : keys) {
              if (!key.equals(MVUtil.DEFAULT_DATABASE_GROUP)) {
                groupsXmlStr.append("<group name='").append(key).append("'>");
                for (String database : groups.get(key)) {
                  groupsXmlStr.append("<db>");
                  groupsXmlStr.append("<val>").append(database).append("</val>");
                  groupsXmlStr.append("<desc>").append(databases.get(database)).append("</desc>");
                  groupsXmlStr.append("</db>");
                }
                groupsXmlStr.append("</group>");
              }
            }
            if (groups.containsKey(MVUtil.DEFAULT_DATABASE_GROUP)) {
              groupsXmlStr.append("<group name='").append(MVUtil.DEFAULT_DATABASE_GROUP)
                  .append("' >");
              for (String database : groups.get(MVUtil.DEFAULT_DATABASE_GROUP)) {
                groupsXmlStr.append("<db>");
                groupsXmlStr.append("<val>").append(database).append("</val>");
                groupsXmlStr.append("<desc>").append(databases.get(database)).append("</desc>");
                groupsXmlStr.append("</db>");
              }
              groupsXmlStr.append("</group>");
            }

            groupsXmlStr.append("</groups>");
            strResp.append(groupsXmlStr);
            for (Map.Entry<String, String> database : databases.entrySet()) {
              strResp.append("<val>").append(database.getKey()).append("</val>");
              strResp.append("<desc>").append(database.getValue()).append("</desc>");
            }

            strResp.append("<url_output><![CDATA[").append(urlOutput)
                .append("]]></url_output>");
            strResp.append("</list_db>");

          } else if (nodeCall.tag.equalsIgnoreCase("list_db_update")) {
            strResp.append("<list_db>");
            databaseManager.initDBList(true);
            databases = databaseManager.getAllDatabasesWithDescription();
            groups = databaseManager.getAllGroups();
            StringBuilder groupsXmlStr = new StringBuilder("<groups>");
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
              groupsXmlStr.append("<group name='").append(entry.getKey()).append("'>");
              for (String database : entry.getValue()) {
                groupsXmlStr.append("<db>");
                groupsXmlStr.append("<val>").append(database).append("</val>");
                groupsXmlStr.append("<desc>").append(databases.get(database)).append("</desc>");
                groupsXmlStr.append("</db>");
              }
              groupsXmlStr.append("</group>");
            }
            groupsXmlStr.append("</groups>");
            strResp.append(groupsXmlStr);

            strResp.append("</list_db>");
            strResp.append("<url_output><![CDATA[").append(urlOutput)
                .append("]]></url_output>");

            handleClearListValCache();
            handleClearListStatCache();
          }

          //  <date> tag, which is used to prevent caching
          else if (nodeCall.tag.equalsIgnoreCase("date")) {
            //do nothing
          }
          //  <db_con> node containing the database connection name
          else if (nodeCall.tag.equalsIgnoreCase("db_con")) {
            if (nodeCall.value.isEmpty()) {
              currentDbName = new String[0];
            } else {
              currentDbName = nodeCall.value.split(",");
            }
          }

          //  <list_val>
          else if (nodeCall.tag.equalsIgnoreCase("list_val")) {
            strResp.append(handleListVal(nodeCall, requestBody.toString(), currentDbName));
          }

          //  <list_stat>
          else if (nodeCall.tag.equalsIgnoreCase("list_stat")) {
            strResp.append(handleListStat(nodeCall, requestBody.toString(), currentDbName));
          }
          //  <list_val_clear_cache>
          else if (nodeCall.tag.equalsIgnoreCase("list_val_clear_cache")) {

            strResp.append(handleClearListValCache());
          }

          //  <list_val_cache_keys>
          else if (nodeCall.tag.equalsIgnoreCase("list_val_cache_keys")) {

            strResp.append(handleListValCacheKeys());
          }

          //  <list_stat_clear_cache>
          else if (nodeCall.tag.equalsIgnoreCase("list_stat_clear_cache")) {

            strResp.append(handleClearListStatCache());
          }

          //  <list_stat_cache_keys>
          else if (nodeCall.tag.equalsIgnoreCase("list_stat_cache_keys")) {
            strResp.append(handleListStatCacheKeys());
          }

          //  <plot>
          else if (nodeCall.tag.equalsIgnoreCase("plot")) {
            strResp.append(handlePlot(requestBody.toString(), currentDbName));
          }


          //  <xml_upload>
          else if (nodeCall.tag.equalsIgnoreCase("xml_upload")) {

            try {
              strResp.append(handleXmlUpload(nodeCall));
              request.getSession().setAttribute("init_xml", strResp.toString().replace("'", "\""));

              request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);

            } catch (Exception e) {
              strResp.append("<error>could not parse request</error>");
              request.getRequestDispatcher("/metviewer1.jsp").forward(request, response);
            }

          } else if (nodeCall.tag.equalsIgnoreCase("history")) {
            String isShowAll = nodeCall.children[0].value;
            strResp.append("<results>").append(getAvailableResults(isShowAll)).append("</results>");

          }

          //  not handled
          else {
            strResp.append("<error>unexpected request type: ").append(nodeCall.tag)
                .append("</error>");
          }
        }
      }
      if (strResp.length() == 0) {
        strResp.append("<error>could not parse request</error>");
      }

      logger.debug("doPost() - response: " + strResp);
      response.setContentType("application/xml;charset=UTF-8");
      try (PrintWriter printWriter = response.getWriter()) {
        printWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        printWriter.append(strResp);
      }
    } catch (Exception e) {

      errorStream
          .print("doPost() - caught " + e.getClass()
                     + ": " + e.getMessage() + "\n" + byteArrayOutputStream.toString());
      out.append("<error>caught " + e.getClass() + ": " + e.getMessage() + "</error>");
    } finally {
      if (out != null) {
        out.close();
      }
      if (byteArrayOutputStream != null) {
        byteArrayOutputStream.close();
      }

      if (reader != null) {
        reader.close();
      }
    }
  }

}

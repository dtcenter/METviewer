package edu.ucar.metviewer;

import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class MVPlotJobParser {

  protected static final Map<String, Method> formatToBoolValues = new HashMap<>();
  private static final Logger logger = LogManager.getLogger("MVPlotJobParser");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final Map<String, Method> formatToStrValues = new HashMap<>();

  static {
    try {
      formatToBoolValues
              .put("vert_plot", MVPlotJob.class.getDeclaredMethod("setVertPlot", boolean.class));
      formatToBoolValues
              .put("x_reverse", MVPlotJob.class.getDeclaredMethod("setXReverse", boolean.class));
      formatToBoolValues
              .put("num_stats", MVPlotJob.class.getDeclaredMethod("setShowNStats", boolean.class));
      formatToBoolValues
              .put("indy1_stag", MVPlotJob.class.getDeclaredMethod("setIndy1Stagger", boolean.class));
      formatToBoolValues
              .put("indy2_stag", MVPlotJob.class.getDeclaredMethod("setIndy2Stagger", boolean.class));
      formatToBoolValues
              .put("grid_on", MVPlotJob.class.getDeclaredMethod("setGridOn", boolean.class));
      formatToBoolValues
              .put("sync_axes", MVPlotJob.class.getDeclaredMethod("setSyncAxes", boolean.class));
      formatToBoolValues
              .put("dump_points1", MVPlotJob.class.getDeclaredMethod("setDumpPoints1", boolean.class));
      formatToBoolValues
              .put("dump_points2", MVPlotJob.class.getDeclaredMethod("setDumpPoints2", boolean.class));
      formatToBoolValues
              .put("log_y1", MVPlotJob.class.getDeclaredMethod("setLogY1", boolean.class));
      formatToBoolValues
              .put("log_y2", MVPlotJob.class.getDeclaredMethod("setLogY2", boolean.class));
      formatToBoolValues.put("varianceInflationFactor", MVPlotJob.class.getDeclaredMethod(
              "setVarianceInflationFactor", boolean.class));
      formatToBoolValues.put("varianceinflationfactor", MVPlotJob.class.getDeclaredMethod(
              "setVarianceInflationFactor", boolean.class));
      formatToBoolValues.put("normalizedHistogram", MVPlotJob.class.getDeclaredMethod(
              "setNormalizedHistogram", boolean.class));
      formatToBoolValues.put("cache_agg_stat",
              MVPlotJob.class.getDeclaredMethod("setCacheAggStat", boolean.class));
      formatToBoolValues
              .put("event_equal", MVPlotJob.class.getDeclaredMethod("setEventEqual", Boolean.class));
      formatToBoolValues
              .put("taylor_voc", MVPlotJob.class.getDeclaredMethod("setTaylorVoc", boolean.class));
      formatToBoolValues.put("taylor_show_gamma",
              MVPlotJob.class
                      .getDeclaredMethod("setTaylorShowGamma", boolean.class));
      formatToBoolValues.put("add_color_bar",
              MVPlotJob.class.getDeclaredMethod("setAddColorBar", Boolean.class));
      formatToBoolValues.put("reverse_y",
              MVPlotJob.class.getDeclaredMethod("setReverseY", Boolean.class));
      formatToBoolValues.put("reverse_x",
              MVPlotJob.class.getDeclaredMethod("setReverseX", Boolean.class));
      formatToBoolValues.put("add_contour_overlay",
              MVPlotJob.class
                      .getDeclaredMethod("setAddContourOverlay", Boolean.class));

    } catch (NoSuchMethodException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
  }

  static {
    try {
      formatToStrValues
              .put("plot_type", MVPlotJob.class.getDeclaredMethod("setPlotType", String.class));
      formatToStrValues
              .put("plot_height", MVPlotJob.class.getDeclaredMethod("setPlotHeight", String.class));
      formatToStrValues
              .put("plot_width", MVPlotJob.class.getDeclaredMethod("setPlotWidth", String.class));
      formatToStrValues
              .put("plot_res", MVPlotJob.class.getDeclaredMethod("setPlotRes", String.class));
      formatToStrValues
              .put("plot_units", MVPlotJob.class.getDeclaredMethod("setPlotUnits", String.class));
      formatToStrValues.put("mar", MVPlotJob.class.getDeclaredMethod("setMar", String.class));
      formatToStrValues.put("mgp", MVPlotJob.class.getDeclaredMethod("setMgp", String.class));
      formatToStrValues.put("cex", MVPlotJob.class.getDeclaredMethod("setCex", String.class));
      formatToStrValues
              .put("title_weight", MVPlotJob.class.getDeclaredMethod("setTitleWeight", String.class));
      formatToStrValues
              .put("title_size", MVPlotJob.class.getDeclaredMethod("setTitleSize", String.class));
      formatToStrValues
              .put("title_offset", MVPlotJob.class.getDeclaredMethod("setTitleOffset", String.class));
      formatToStrValues
              .put("title_align", MVPlotJob.class.getDeclaredMethod("setTitleAlign", String.class));
      formatToStrValues
              .put("xtlab_orient", MVPlotJob.class.getDeclaredMethod("setXtlabOrient", String.class));
      formatToStrValues
              .put("xtlab_perp", MVPlotJob.class.getDeclaredMethod("setXtlabPerp", String.class));
      formatToStrValues
              .put("xtlab_horiz", MVPlotJob.class.getDeclaredMethod("setXtlabHoriz", String.class));
      formatToStrValues
              .put("xtlab_freq", MVPlotJob.class.getDeclaredMethod("setXtlabFreq", String.class));
      formatToStrValues
              .put("xtlab_size", MVPlotJob.class.getDeclaredMethod("setXtlabSize", String.class));
      formatToStrValues
              .put("xlab_weight", MVPlotJob.class.getDeclaredMethod("setXlabWeight", String.class));
      formatToStrValues
              .put("xlab_size", MVPlotJob.class.getDeclaredMethod("setXlabSize", String.class));
      formatToStrValues
              .put("xlab_offset", MVPlotJob.class.getDeclaredMethod("setXlabOffset", String.class));
      formatToStrValues
              .put("xlab_align", MVPlotJob.class.getDeclaredMethod("setXlabAlign", String.class));
      formatToStrValues
              .put("ytlab_orient", MVPlotJob.class.getDeclaredMethod("setYtlabOrient", String.class));
      formatToStrValues
              .put("ytlab_perp", MVPlotJob.class.getDeclaredMethod("setYtlabPerp", String.class));
      formatToStrValues
              .put("ytlab_horiz", MVPlotJob.class.getDeclaredMethod("setYtlabHoriz", String.class));
      formatToStrValues
              .put("ytlab_size", MVPlotJob.class.getDeclaredMethod("setYtlabSize", String.class));
      formatToStrValues
              .put("ylab_weight", MVPlotJob.class.getDeclaredMethod("setYlabWeight", String.class));
      formatToStrValues
              .put("ylab_size", MVPlotJob.class.getDeclaredMethod("setYlabSize", String.class));
      formatToStrValues
              .put("ylab_offset", MVPlotJob.class.getDeclaredMethod("setYlabOffset", String.class));
      formatToStrValues
              .put("ylab_align", MVPlotJob.class.getDeclaredMethod("setYlabAlign", String.class));
      formatToStrValues
              .put("grid_lty", MVPlotJob.class.getDeclaredMethod("setGridLty", String.class));
      formatToStrValues
              .put("grid_col", MVPlotJob.class.getDeclaredMethod("setGridCol", String.class));
      formatToStrValues
              .put("grid_lwd", MVPlotJob.class.getDeclaredMethod("setGridLwd", String.class));
      formatToStrValues.put("grid_x", MVPlotJob.class.getDeclaredMethod("setGridX", String.class));
      formatToStrValues
              .put("x2tlab_orient", MVPlotJob.class.getDeclaredMethod("setX2tlabOrient", String.class));
      formatToStrValues
              .put("x2tlab_perp", MVPlotJob.class.getDeclaredMethod("setX2tlabPerp", String.class));
      formatToStrValues
              .put("x2tlab_horiz", MVPlotJob.class.getDeclaredMethod("setX2tlabHoriz", String.class));
      formatToStrValues
              .put("x2tlab_size", MVPlotJob.class.getDeclaredMethod("setX2tlabSize", String.class));
      formatToStrValues
              .put("x2lab_weight", MVPlotJob.class.getDeclaredMethod("setX2labWeight", String.class));
      formatToStrValues
              .put("x2lab_size", MVPlotJob.class.getDeclaredMethod("setX2labSize", String.class));
      formatToStrValues
              .put("x2lab_offset", MVPlotJob.class.getDeclaredMethod("setX2labOffset", String.class));
      formatToStrValues
              .put("x2lab_align", MVPlotJob.class.getDeclaredMethod("setX2labAlign", String.class));
      formatToStrValues
              .put("y2tlab_orient", MVPlotJob.class.getDeclaredMethod("setY2tlabOrient", String.class));
      formatToStrValues
              .put("y2tlab_perp", MVPlotJob.class.getDeclaredMethod("setY2tlabPerp", String.class));
      formatToStrValues
              .put("y2tlab_horiz", MVPlotJob.class.getDeclaredMethod("setY2tlabHoriz", String.class));
      formatToStrValues
              .put("y2tlab_size", MVPlotJob.class.getDeclaredMethod("setY2tlabSize", String.class));
      formatToStrValues
              .put("y2lab_weight", MVPlotJob.class.getDeclaredMethod("setY2labWeight", String.class));
      formatToStrValues
              .put("y2lab_size", MVPlotJob.class.getDeclaredMethod("setY2labSize", String.class));
      formatToStrValues
              .put("y2lab_offset", MVPlotJob.class.getDeclaredMethod("setY2labOffset", String.class));
      formatToStrValues
              .put("y2lab_align", MVPlotJob.class.getDeclaredMethod("setY2labAlign", String.class));
      formatToStrValues
              .put("legend_size", MVPlotJob.class.getDeclaredMethod("setLegendSize", String.class));
      formatToStrValues
              .put("legend_box", MVPlotJob.class.getDeclaredMethod("setLegendBox", String.class));
      formatToStrValues
              .put("legend_inset", MVPlotJob.class.getDeclaredMethod("setLegendInset", String.class));
      formatToStrValues
              .put("legend_ncol", MVPlotJob.class.getDeclaredMethod("setLegendNcol", String.class));
      formatToStrValues.put("caption_weight",
              MVPlotJob.class.getDeclaredMethod("setCaptionWeight", String.class));
      formatToStrValues
              .put("caption_col", MVPlotJob.class.getDeclaredMethod("setCaptionCol", String.class));
      formatToStrValues
              .put("caption_size", MVPlotJob.class.getDeclaredMethod("setCaptionSize", String.class));
      formatToStrValues.put("caption_offset",
              MVPlotJob.class.getDeclaredMethod("setCaptionOffset", String.class));
      formatToStrValues
              .put("caption_align", MVPlotJob.class.getDeclaredMethod("setCaptionAlign", String.class));
      formatToStrValues
              .put("box_pts", MVPlotJob.class.getDeclaredMethod("setBoxPts", String.class));
      formatToStrValues
              .put("box_outline", MVPlotJob.class.getDeclaredMethod("setBoxOutline", String.class));
      formatToStrValues
              .put("box_boxwex", MVPlotJob.class.getDeclaredMethod("setBoxBoxwex", String.class));
      formatToStrValues
              .put("box_notch", MVPlotJob.class.getDeclaredMethod("setBoxNotch", String.class));
      formatToStrValues
              .put("box_avg", MVPlotJob.class.getDeclaredMethod("setBoxAvg", String.class));
      formatToStrValues.put("rely_event_hist",
              MVPlotJob.class.getDeclaredMethod("setRelyEventHist", String.class));
      formatToStrValues
              .put("ci_alpha", MVPlotJob.class.getDeclaredMethod("setCIAlpha", String.class));

      formatToStrValues
              .put("eqbound_low", MVPlotJob.class.getDeclaredMethod("setEqboundLow", String.class));
      formatToStrValues
              .put("eqbound_high", MVPlotJob.class.getDeclaredMethod("setEqboundHigh", String.class));

      formatToStrValues
              .put("ensss_pts", MVPlotJob.class.getDeclaredMethod("setEnsSsPts", String.class));
      formatToStrValues.put("ensss_pts_disp",
              MVPlotJob.class.getDeclaredMethod("setEnsSsPtsDisp", String.class));

      formatToStrValues
              .put("plot_ci", MVPlotJob.class.getDeclaredMethod("setPlotCI", String.class));
      formatToStrValues
              .put("plot_disp", MVPlotJob.class.getDeclaredMethod("setPlotDisp", String.class));
      formatToStrValues
              .put("show_signif", MVPlotJob.class.getDeclaredMethod("setShowSignif", String.class));

      formatToStrValues
              .put("order_series", MVPlotJob.class.getDeclaredMethod("setOrderSeries", String.class));
      formatToStrValues
              .put("colors", MVPlotJob.class.getDeclaredMethod("setColors", String.class));
      formatToStrValues.put("pch", MVPlotJob.class.getDeclaredMethod("setPch", String.class));
      formatToStrValues.put("type", MVPlotJob.class.getDeclaredMethod("setType", String.class));
      formatToStrValues.put("lty", MVPlotJob.class.getDeclaredMethod("setLty", String.class));
      formatToStrValues.put("lwd", MVPlotJob.class.getDeclaredMethod("setLwd", String.class));
      formatToStrValues
              .put("con_series", MVPlotJob.class.getDeclaredMethod("setConSeries", String.class));
      formatToStrValues
              .put("legend", MVPlotJob.class.getDeclaredMethod("setLegend", String.class));
      formatToStrValues.put("y1_lim", MVPlotJob.class.getDeclaredMethod("setY1Lim", String.class));
      formatToStrValues.put("x1_lim", MVPlotJob.class.getDeclaredMethod("setX1Lim", String.class));
      formatToStrValues
              .put("y1_bufr", MVPlotJob.class.getDeclaredMethod("setY1Bufr", String.class));
      formatToStrValues.put("y2_lim", MVPlotJob.class.getDeclaredMethod("setY2Lim", String.class));
      formatToStrValues
              .put("y2_bufr", MVPlotJob.class.getDeclaredMethod("setY2Bufr", String.class));
      formatToStrValues
              .put("plot_cmd", MVPlotJob.class.getDeclaredMethod("setPlotCmd", String.class));
      formatToStrValues
              .put("plot_cond", MVPlotJob.class.getDeclaredMethod("setPlotCond", String.class));
      formatToStrValues
              .put("execution_type", MVPlotJob.class.getDeclaredMethod("setExecutionType", String.class));
      formatToStrValues
              .put("annotation_template", MVPlotJob.class.getDeclaredMethod("setAnnotationTemplate", String.class));
      formatToStrValues
              .put("plot_stat", MVPlotJob.class.getDeclaredMethod("setPlotStat", String.class));
      formatToStrValues.put("contour_intervals",
              MVPlotJob.class.getDeclaredMethod("setContourIntervals", String
                      .class));
      formatToStrValues.put("color_palette",
              MVPlotJob.class.getDeclaredMethod("setColorPalette", String.class));

    } catch (NoSuchMethodException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
  }

  protected final MVOrderedMap jobs = new MVOrderedMap();
  protected final Map dateListDecl = new HashMap<>();
  protected final Map dateRangeDecl = new HashMap<>();
  protected final Map dateRangeListDecl = new HashMap<>();
  protected final Map plotDecl = new HashMap<>();
  protected final Map plotNode = new HashMap<>();
  protected Document doc = null;
  protected MVPlotJob[] listJobs = {};
  protected MVNode plotSpec = null;
  protected String rscript = "Rscript";
  protected String rtmplFolder = "";
  protected String rWorkFolder = "";
  protected String plotsFolder = "";
  protected String dataFolder = "";
  protected String scriptsFolder = "";
  protected String dbHost = "";
  protected String dbUser = "";
  protected String dbPass = "";
  protected String dbManagementSystem = "";
  protected String dbDriver = null;


  /**
   * Build a parser whose input source is the specified URI
   *
   * @param spec URI of the XML plot specification source
   */
  public MVPlotJobParser(String spec) throws ParserConfigurationException, IOException, SAXException, DatabaseException, ValidationException {
    super();
    DocumentBuilder builder = getDocumentBuilder();
    //  parse the input document and build the MVNode data structure
    doc = builder.parse(spec);
    plotSpec = new MVNode(doc.getFirstChild());

    parsePlotJobSpec();
  }


  /**
   * Build a parser whose input source is the specified InputStream
   *
   * @param in Stream from which the plot specification will be drawn
   */
  public MVPlotJobParser(InputStream in) throws ParserConfigurationException, DatabaseException, ValidationException, IOException, SAXException {
    DocumentBuilder builder = getDocumentBuilder();

    //  parse the input document and build the MVNode data structure
    doc = builder.parse(in);
    plotSpec = new MVNode(doc.getFirstChild());
    parsePlotJobSpec();
  }

  /**
   * Build a parser whose input source is the plot_spec MVNode
   *
   * @param node plot_spec MVNode to parse
   */
  public MVPlotJobParser(MVNode node) throws DatabaseException, ValidationException {
    super();
    plotSpec = node;
    parsePlotJobSpec();
  }

  /**
   * Create a parser-specific instance of the DocumentBuilder and return it
   */
  public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {

    //  instantiate and configure the xml parser

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    //Disable DTDs
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    dbf.setNamespaceAware(true);
    dbf.setValidating(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    documentBuilder.setErrorHandler(null);
    return documentBuilder;
  }



  /**
   * Determine if the input plot job has many necessary components to build a plot.  If not, return
   * the structure name that has been found to be missing.
   *
   * @param job plot job to inspect
   * @return name of missing structure, or an empty string if the job is ok
   */
  public static String checkJobCompleteness(MVPlotJob job) {
    if (job.getPlotTmpl().isEmpty()) {
      return "lacks template";
    } else if (job.getIndyVar().isEmpty()
            && !job.getPlotTmpl().contains("taylor")
            && !job.getPlotTmpl().contains("eclv")
            && !MVUtil.isEtbJob(job)) {
      return "lacks indep";
    } else if (1 > job.getIndyVal().length
            && null == job.getIndyDep()
            && !job.getPlotTmpl().contains("taylor")
            && !job.getPlotTmpl().contains("eclv")
            && !MVUtil.isEtbJob(job)) {
      return "lacks indep";
    } else if (1 > job.getDepGroups().length && !job.getPlotTmpl().contains("eclv")) {
      return "lacks dep";
    } else if (1 > job.getSeries1Val().size()) {
      return "lacks series1";
    } else if (job.getRFileTmpl().isEmpty()) {
      return "lacks r_file";
    } else if (job.getPlotFileTmpl().isEmpty()) {
      return "lacks plot_file";
    } else if (job.getDataFileTmpl().isEmpty()) {
      return "lacks data_file";
    } else if ((job.getAggCtc()
            || job.getAggSl1l2()
            || job.getAggSal1l2()
            || job.getAggGrad()
            || job.getAggEcnt()
            || job.getAggNbrCnt()
            || job.getAggPct()
            || job.getAggSsvar()
            || job.getAggVal1l2()
            || job.getAggVl1l2())
            && (job.getCalcCtc()
            || job.getCalcSl1l2()
            || job.getCalcSal1l2()
            || job.getCalcGrad()
    )) {
      return "has both agg_stat and calc_stat";
    }
    return "";
  }

  /**
   * Determine if the input plot job fields have been introduced in the alphabetical order . If not,
   * return the structure name that has been found to be incorrect.
   *
   * @param job plot job to inspect
   * @return name of missing structure, or an empty string if the job is ok
   */
  public static String checkJobFieldsOrder(MVPlotJob job) throws ValidationException {
    String result = "";
    Map.Entry[] mapValues = job.getSeries1Val().getOrderedEntriesForSqlSeries();
    result = result + checkOrder(mapValues);

    mapValues = job.getSeries2Val().getOrderedEntriesForSqlSeries();
    result = result + checkOrder(mapValues);

    mapValues = job.getPlotFixVal().getOrderedEntries();
    result = result + checkOrder(mapValues);

    for (int i = 0; i < job.getDepGroups().length; i++) {
      mapValues = job.getDepGroups()[i].getOrderedEntries();
      result = result + checkOrder(mapValues);
    }

    String[] indyVals = job.getIndyVal();
    if (!job.getIndyVar().startsWith("fcst_thresh") && !job.getIndyVar().startsWith(
            "obs_thresh") && !job.getIndyVar().startsWith("fcst_lev")) {
      if (job.getIndyVar().startsWith("fcst_lead") || job.getIndyVar().startsWith(
              "valid_hour") || job.getIndyVar().startsWith("init_hour")) {
        Integer[] valuesSortedInt = new Integer[indyVals.length];
        for (int i = 0; i < indyVals.length; i++) {
          try {
            valuesSortedInt[i] = Integer.valueOf(indyVals[i]);
          } catch (NumberFormatException e) {
            logger.error(ERROR_MARKER, e.getMessage());
          }
        }
        Arrays.sort(valuesSortedInt);
        for (int i = 0; i < indyVals.length; i++) {
          Integer indyInt = null;
          try {
            indyInt = Integer.valueOf(indyVals[i]);
            if (!indyInt.equals(valuesSortedInt[i])) {
              result = result + "Values for variable " + job.getIndyVar() + " are not sorted";
            }
          } catch (NumberFormatException e) {
            logger.error(ERROR_MARKER, e.getMessage());
          }

        }
      } else {
        String[] indyValsSorted;
        if (job.getIndyVar().equals("interp_pnts")) {
          List<String> sorted = MVUtil.sortInterpPnts(Arrays.asList(indyVals));
          indyValsSorted = sorted.toArray(new String[sorted.size()]);
        } else {
          indyValsSorted = Arrays.copyOf(indyVals, indyVals.length);
          Arrays.sort(indyValsSorted);
        }
        if (!Arrays.equals(indyVals, indyValsSorted)) {
          result = result + "Values for variable " + job.getIndyVar() + " are not sorted";
        }
      }
    }
    return result;
  }

  private static String checkOrder(Map.Entry[] mapValues) throws ValidationException {
    for (Map.Entry entry : mapValues) {

      Object valuesObj = entry.getValue();


      if (valuesObj instanceof String[]) {

        String[] values = (String[]) entry.getValue();

        /*
                    check if values have ',' - groups.
                    ignore if true
                    */
        boolean isGroup = false;
        for (String value : values) {
          if (value.contains(",")) {
            isGroup = true;
            break;
          }
        }
        if (!isGroup) {
          if (!String.valueOf(entry.getKey()).startsWith("fcst_thresh")
                  && !String.valueOf(entry.getKey()).startsWith("obs_thresh")
                  && !String.valueOf(entry.getKey()).startsWith("fcst_lev")) {
            if (String.valueOf(entry.getKey()).startsWith("fcst_lead")
                    || String.valueOf(entry.getKey()).startsWith("valid_hour")
                    || String.valueOf(entry.getKey()).startsWith("init_hour")) {
              Integer[] valuesSortedInt = new Integer[values.length];
              for (int i = 0; i < values.length; i++) {
                try {
                  valuesSortedInt[i] = Integer.valueOf(values[i]);
                } catch (NumberFormatException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
              Arrays.sort(valuesSortedInt);
              for (int i = 0; i < values.length; i++) {
                Integer indyInt = null;
                try {
                  indyInt = Integer.valueOf(values[i]);
                  if (!indyInt.equals(valuesSortedInt[i])) {
                    return "Values for variable " + entry.getKey().toString() + " are not sorted";
                  }
                } catch (NumberFormatException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
            } else {
              String[] valuesSorted;
              if (entry.getKey().equals("interp_pnts")) {
                List<String> sorted = MVUtil.sortInterpPnts(Arrays.asList(values));
                valuesSorted = sorted.toArray(new String[sorted.size()]);
              } else {
                valuesSorted = Arrays.copyOf(values, values.length);
                Arrays.sort(valuesSorted);
              }
              if (!Arrays.equals(values, valuesSorted)) {
                return "Values for variable " + entry.getKey().toString() + " are not sorted";
              }
            }
          }
        }
      } else if (valuesObj instanceof MVOrderedMap) {
        MVOrderedMap values = (MVOrderedMap) entry.getValue();
        return checkOrder(values.getOrderedEntries());
      }
    }
    return "";
  }


  /**
   * Form a SQL between clause using the information in the input &lt;date_range&gt; node.
   *
   * @param nodeDateRange Contains date range information
   * @return String containing SQL between clause
   */
  public static String parseDateRange(MVNode nodeDateRange) throws ValidationException {
    String strStart = "";
    String strEnd = "";
    SimpleDateFormat formatDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    formatDb.setTimeZone(TimeZone.getTimeZone("UTC"));
    String strFormat = formatDb.toPattern();
    for (int j = 0; j < nodeDateRange.children.length; j++) {
      MVNode nodeChild = nodeDateRange.children[j];
      if (nodeChild.tag.equals("start")) {
        strStart = (0 < nodeChild.children.length ? MVUtil.parseDateOffset(nodeChild.children[0],
                strFormat) : nodeChild.value);
      } else if (nodeChild.tag.equals("end")) {
        strEnd = (0 < nodeChild.children.length ? MVUtil.parseDateOffset(nodeChild.children[0],
                strFormat) : nodeChild.value);
      }
    }
    return "BETWEEN '" + strStart + "' AND '" + strEnd + "'";
  }

  /**
   * Parse the &lt;indep&gt; node of a xml plot specification, returning the parsed information in
   * the form of two lists.  The first list contains the independent variable values, the second
   * list contains the labels and the third contains the plot values.
   *
   * @param node XML plot specification &lt;indep&gt; node
   * @param dep  (optional) String representation of a dependency value date
   * @return Two lists of independent variable values and labels, respectively
   */
  public static String[][] parseIndyNode(MVNode node, String dep) throws ValidationException {
    int intIndyNum = node.children.length;
    List<String> listIndyVal = new ArrayList<>();
    List<String> listIndyLabel = new ArrayList<>();
    List<String> listIndyPlotVal = new ArrayList<>();
    for (int j = 0; j < intIndyNum; j++) {
      MVNode nodeIndyVal = node.children[j];

      //  <val>
      if (nodeIndyVal.tag.equals("val")) {
        listIndyVal.add(nodeIndyVal.value);
        if (!nodeIndyVal.label.equals("")) {
          listIndyLabel.add(nodeIndyVal.label);
        } else {
          listIndyLabel.add(nodeIndyVal.value);
        }
        if (!nodeIndyVal.plotVal.equals("")) {
          listIndyPlotVal.add(nodeIndyVal.plotVal);
        }
      }

      //  <date_list>
      else if (nodeIndyVal.tag.equalsIgnoreCase("date_list")) {
        String strStart = "";
        String strEnd = "";
        int intInc = 0;
        SimpleDateFormat formatDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDb.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strFormat = formatDb.toPattern();

        for (int k = 0; k < nodeIndyVal.children.length; k++) {
          MVNode nodeChild = nodeIndyVal.children[k];
          if (nodeChild.tag.equals("start")) {
            strStart = (0 < nodeChild.children.length
                    ? MVUtil.parseDateOffset(nodeChild.children[0], strFormat, dep)
                    : nodeChild.value);
          } else if (nodeChild.tag.equals("end")) {
            strEnd = (0 < nodeChild.children.length
                    ? MVUtil.parseDateOffset(nodeChild.children[0], strFormat, dep)
                    : nodeChild.value);
          } else if (nodeChild.tag.equals("inc")) {
            intInc = Integer.parseInt(nodeChild.value);
          } else if (nodeChild.tag.equals("label_format")) {
            strFormat = nodeChild.value;
          }
        }

        SimpleDateFormat formatLabel = new SimpleDateFormat(strFormat, Locale.US);
        formatLabel.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<String> listDates = MVUtil.buildDateList(strStart, strEnd, intInc,
                formatDb.toPattern());
        List<String> listLabels = new ArrayList<>();
        for (String listDate : listDates) {

          try {
            listLabels.add(formatLabel.format(formatDb.parse(listDate)));
          } catch (ParseException e) {
            logger.error(ERROR_MARKER, e.getMessage());
          }

        }

        listIndyVal.addAll(listDates);
        listIndyLabel.addAll(listLabels);
      }
    }

    return new String[][]{MVUtil.toArray(listIndyVal),
            MVUtil.toArray(listIndyLabel), MVUtil.toArray(listIndyPlotVal)};
  }

  public static StringBuilder serializeJob(MVPlotJob job, DatabaseInfo databaseInfo) {

    //  database information
    String databases = "";
    for (String db : job.getCurrentDBName()) {
      databases = databases + db + ",";
    }
    databases = databases.substring(0, databases.length() - 1);
    StringBuilder xmlStr = new StringBuilder(
            "<plot_spec>"
                    + "<connection>"
                    + "<host>" + databaseInfo.getHost() + "</host>"
                    + "<database>" + databases + "</database>"
                    + "<user>" + "******" + "</user>"
                    + "<password>" + "******" + "</password>"
                    + "</connection>"
                    + "<plot>");

    //  plot template
    xmlStr.append("<template>").append(job.getPlotTmpl()).append("</template>");

    for (int intY = 1; intY <= 2; intY++) {

      //  get the series for the current y-axis
      MVOrderedMap mapSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val());
      xmlStr.append("<series").append(intY).append('>');

      //  serialize each fcst_var and it's vals
      String[] listSeriesField = mapSeries.getKeyList();

      for (int i = 0; i < listSeriesField.length; i++) {

        String fieldName = listSeriesField[i];
        String xmlSimpleValues = "";
        String[] listSeriesVal = (String[]) mapSeries.get(i);
        for (int j = 0; j < listSeriesVal.length; j++) {
          if (listSeriesVal[j].contains(",")) {
            xmlStr.append("<field name=\"").append(fieldName).append("\">");
            if (fieldName.equals("init_hour")) {
              String strHour = listSeriesVal[j];
              while (strHour.length() < 2) {
                strHour = "0" + strHour;
              }
              xmlStr.append("<val>").append(strHour).append("</val>");
            } else {
              xmlStr.append("<val>").append(listSeriesVal[j]).append("</val>");
            }
            xmlStr.append("</field>");
          } else {
            if (xmlSimpleValues.length() == 0) {
              xmlSimpleValues = "<field name=\"" + fieldName + "\">";
            }
            if (fieldName.equals("init_hour")) {
              String strHour = listSeriesVal[j];
              while (strHour.length() < 2) {
                strHour = "0" + strHour;
              }
              xmlSimpleValues += "<val>" + strHour + "</val>";
            } else {
              xmlSimpleValues += "<val>" + sanitise(listSeriesVal[j]) + "</val>";
            }
          }

        }
        if (xmlSimpleValues.length() > 0) {
          xmlSimpleValues += "</field>";
        }
        xmlStr.append(xmlSimpleValues);
      }
      xmlStr.append("</series" + intY + ">");
    }


    //  if there are dep,  and indep elements present, handle them
    if (!job.getPlotTmpl().startsWith("rhist")
            && !job.getPlotTmpl().startsWith("phist")
            && !job.getPlotTmpl().startsWith("roc")
            && !job.getPlotTmpl().startsWith("rely")
            && !job.getPlotTmpl().startsWith("relp")
            && !job.getPlotTmpl().startsWith("eclv")) {
      int axis = 2;
      if (job.getPlotTmpl().startsWith("taylor")) {
        axis = 1;
      }
      // dep
      xmlStr.append("<dep>");
      MVOrderedMap[] listDepGroup = job.getDepGroups();
      for (int intY = 1; intY <= axis; intY++) {

        //  get the list of fcst_var for the current dep
        MVOrderedMap mapDep = (MVOrderedMap) listDepGroup[0].get("dep" + intY);
        Map.Entry[] listDep = mapDep.getOrderedEntries();

        //  serialize the dep and it's fcst_var stats
        String strDep = "dep" + intY;
        xmlStr.append("<" + strDep + ">");
        for (int i = 0; i < listDep.length; i++) {
          String[] listStat = (String[]) listDep[i].getValue();
          xmlStr.append("<fcst_var name=\"" + listDep[i].getKey().toString() + "\">");
          for (int j = 0; j < listStat.length; j++) {
            xmlStr.append("<stat>" + listStat[j] + "</stat>");
          }
          xmlStr.append("</fcst_var>");
        }
        xmlStr.append("</" + strDep + ">");
      }
      xmlStr.append("</dep>");


      //  indep
      xmlStr.append("<indep name=\"" + job.getIndyVar() + "\">");
      String[] listIndyVal = job.getIndyVal();
      String[] listIndyPlotVal = job.getIndyPlotVal();
      String[] listIndyLabel = job.getIndyLabel();
      for (int i = 0; i < listIndyVal.length; i++) {
        String strIndyPlotVal = (0 < listIndyPlotVal.length ? listIndyPlotVal[i] : "");
        xmlStr.append("<val label=\""
                + listIndyLabel[i].replace("&", "&#38;").replace(">", "&gt;")
                .replace("<", "&lt;")
                + "\" plot_val=\""
                + strIndyPlotVal.replace("&", "&#38;").replace(">", "&gt;")
                .replace("<", "&lt;")
                + "\">"
                + listIndyVal[i].replace("&", "&#38;").replace(">", "&gt;")
                .replace("<", "&lt;")
                + "</val>");
      }
      xmlStr.append("</indep>");

    }

    //  plot_fix
    MVOrderedMap mapPlotFix = job.getPlotFixVal();
    xmlStr.append("<plot_fix>");
    String[] listFixField = mapPlotFix.getKeyList();
    String[] listFixFieldEx = job.getPlotFixValEq().getKeyList();
    for (int i = 0; i < listFixField.length; i++) {
      boolean isEqualize = false;
      for (String name : listFixFieldEx) {
        if (name.equals(listFixField[i])) {
          isEqualize = true;
          break;
        }
      }
      xmlStr.append("<field name=\"" + listFixField[i] + "\" equalize=\""
              + isEqualize + "\" >");
      Object objFixVal = mapPlotFix.get(listFixField[i]);
      if (objFixVal instanceof String[]) {
        String[] listFixVal = (String[]) objFixVal;
        for (int j = 0; j < listFixVal.length; j++) {
          xmlStr.append("<val>" + listFixVal[j].replace("&", "&#38;").replace(">", "&gt;")
                  .replace("<", "&lt;") + "</val>");
        }
      } else if (objFixVal instanceof MVOrderedMap) {
        MVOrderedMap mapFixSet = (MVOrderedMap) objFixVal;
        String[] listFixSetKey = mapFixSet.getKeyList();
        for (int j = 0; j < listFixSetKey.length; j++) {
          String[] listFixSetVal = (String[]) mapFixSet.get(listFixSetKey[j]);
          xmlStr.append("<set name=\"" + listFixSetKey[j] + "\">");
          for (int k = 0; k < listFixSetVal.length; k++) {
            xmlStr.append("<val>" + listFixSetVal[k].replace("&", "&#38;").replace(">", "&gt;")
                    .replace("<", "&lt;") + "</val>");
          }
          xmlStr.append("</set>");
        }
      }
      xmlStr.append("</field>");
    }
    xmlStr.append("</plot_fix>");

    //  agg_stat
    if ((job.getAggCtc() || job.getAggSl1l2() || job.getAggSal1l2() || job.getAggPct()
            || job.getAggNbrCnt() || job.getAggSsvar()
            || job.getAggVl1l2() || job.getAggGrad() || job.getAggEcnt())
            || job.isModeRatioJob()) {
      xmlStr.append(
              "<agg_stat>"
                      + "<agg_ctc>" + (job.getAggCtc() ? "TRUE" : "FALSE") + "</agg_ctc>"
                      + "<agg_nbrctc>" + (job.getAggNbrCtc() ? "TRUE" : "FALSE") + "</agg_nbrctc>"
                      + "<agg_sl1l2>" + (job.getAggSl1l2() ? "TRUE" : "FALSE") + "</agg_sl1l2>"
                      + "<agg_grad>" + (job.getAggGrad() ? "TRUE" : "FALSE") + "</agg_grad>"
                      + "<agg_sal1l2>" + (job.getAggSal1l2() ? "TRUE" : "FALSE") + "</agg_sal1l2>"
                      + "<agg_pct>" + (job.getAggPct() ? "TRUE" : "FALSE") + "</agg_pct>"
                      + "<agg_nbrcnt>" + (job.getAggNbrCnt() ? "TRUE" : "FALSE") + "</agg_nbrcnt>"
                      + "<agg_ssvar>" + (job.getAggSsvar() ? "TRUE" : "FALSE") + "</agg_ssvar>"
                      + "<agg_vl1l2>" + (job.getAggVl1l2() ? "TRUE" : "FALSE") + "</agg_vl1l2>"
                      + "<agg_val1l2>" + (job.getAggVal1l2() ? "TRUE" : "FALSE") + "</agg_val1l2>"
                      + "<agg_ecnt>" + (job.getAggEcnt() ? "TRUE" : "FALSE") + "</agg_ecnt>"
                      + "<agg_rps>" + (job.getAggRps() ? "TRUE" : "FALSE") + "</agg_rps>"
                      + "<boot_repl>" + job.getAggBootRepl() + "</boot_repl>"
                      + "<boot_random_seed>" + job.getAggBootRandomSeed() + "</boot_random_seed>"
                      + "<cl_step>" + job.getCl_step() + "</cl_step>"
                      + "<boot_ci>" + job.getAggBootCI() + "</boot_ci>"
                      + "<eveq_dis>" + (job.getEveqDis() ? "TRUE" : "FALSE") + "</eveq_dis>"
                      + "<cache_agg_stat>" + (job.getCacheAggStat() ? "TRUE" : "FALSE")
                      + "</cache_agg_stat>"
                      + "</agg_stat>");
    }

    //  calc_stat
    if (!job.getRevisionStat() && (job.getCalcCtc() || job.getCalcSl1l2()
            || job.getCalcSal1l2() || job.getCalcVl1l2() || job.getCalcGrad())) {
      xmlStr.append(
              "<calc_stat>"
                      + "<calc_ctc>" + (job.getCalcCtc() ? "TRUE" : "FALSE") + "</calc_ctc>"
                      + "<calc_sl1l2>" + (job.getCalcSl1l2() ? "TRUE" : "FALSE") + "</calc_sl1l2>"
                      + "<calc_sal1l2>" + (job.getCalcSal1l2() ? "TRUE" : "FALSE") + "</calc_sal1l2>"
                      + "<calc_vl1l2>" + (job.getCalcVl1l2() ? "TRUE" : "FALSE") + "</calc_vl1l2>"
                      + "<calc_val1l2>" + (job.getCalcVal1l2() ? "TRUE" : "FALSE") + "</calc_val1l2>"
                      + "<calc_grad>" + (job.getCalcGrad() ? "TRUE" : "FALSE") + "</calc_grad>"
                      + "</calc_stat>");
    }


    //revis_stat

    if (job.getRevisionStat()) {
      xmlStr.append(
              "<revis_stat>"
                      + "<calc_ctc>" + (job.getCalcCtc() ? "TRUE" : "FALSE") + "</calc_ctc>"
                      + "<calc_sl1l2>" + (job.getCalcSl1l2() ? "TRUE" : "FALSE") + "</calc_sl1l2>"
                      + "<calc_sal1l2>" + (job.getCalcSal1l2() ? "TRUE" : "FALSE") + "</calc_sal1l2>"
                      + "<calc_vl1l2>" + (job.getCalcVl1l2() ? "TRUE" : "FALSE") + "</calc_vl1l2>"
                      + "<calc_grad>" + (job.getCalcGrad() ? "TRUE" : "FALSE") + "</calc_grad>"
                      + "<revision_ac>" + (job.getRevisionAc() ? "TRUE" : "FALSE") + "</revision_ac>"
                      + "<revision_run>" + (job.getRevisionRun() ? "TRUE" : "FALSE") + "</revision_run>"
                      + "</revis_stat>");
    }


    //  roc_calc
    if (job.getPlotTmpl().equals("roc.R_tmpl")) {
      xmlStr.append(
              "<roc_calc>"
                      + "<roc_pct>" + (job.getRocPct() ? "TRUE" : "FALSE") + "</roc_pct>"
                      + "<roc_ctc>" + (job.getRocCtc() ? "TRUE" : "FALSE") + "</roc_ctc>"
                      + "</roc_calc>");

    }

    if (job.getPlotTmpl().equals("roc.R_tmpl") || job.getPlotTmpl().equals("rely.R_tmpl")) {
      xmlStr.append("<summary_curve>");
      for (String stat : job.getSummaryCurve()) {
        xmlStr.append("<val>" + stat + "</val>");
      }
      xmlStr.append("</summary_curve>");
      xmlStr.append("<inset_hist>").append(job.getInsetHist()).append("</inset_hist>");
      xmlStr.append("<rely_event_hist>").append(job.getRelyEventHist()).append("</rely_event_hist>");
      xmlStr.append("<add_skill_line>").append(job.getAddSkillLine()).append("</add_skill_line>");
      xmlStr.append("<add_noskill_line>").append(job.getAddNoSkillLine()).append("</add_noskill_line>");
      xmlStr.append("<add_reference_line>").append(job.getAddReferenceLine())
              .append("</add_reference_line>");
      xmlStr.append("<add_point_thresholds>").append(job.getAddPointThresholds())
              .append("</add_point_thresholds>");
      xmlStr.append("<create_html>").append(job.getCreateHtml())
              .append("</create_html>");
    }

    //  roc_calc
    if (job.getPlotTmpl().equals("ens_ss.R_tmpl")) {
      xmlStr.append(
              "<ensss_pts>" + job.getEnsSsPts() + "</ensss_pts>" +
                      "<ensss_pts_disp>" + job.getEnsSsPtsDisp() + "</ensss_pts_disp>");
    }

    //taylor
    if (job.getPlotTmpl().equals("taylor_plot.R_tmpl")) {
      xmlStr.append("<taylor_voc>" + job.getTaylorVoc() + "</taylor_voc>"
              + "<taylor_show_gamma>" + job.getTaylorShowGamma()
              + "</taylor_show_gamma>");
    }

    //  tmpl

    xmlStr.append(
            "<tmpl>"
                    + "<title>" + sanitise(preserveBackslash(job.getTitleTmpl())) + "</title>"
                    + "<x_label>" + sanitise(preserveBackslash(job.getXLabelTmpl())) + "</x_label>"
                    + "<y1_label>" + sanitise(preserveBackslash(job.getY1LabelTmpl())) + "</y1_label>"
                    + "<y2_label>" + sanitise(preserveBackslash(job.getY2LabelTmpl())) + "</y2_label>"
                    + "<caption>" + sanitise(preserveBackslash(job.getCaptionTmpl())) + "</caption>"
                    + "<job_title>" + job.getJobTitleTmpl() + "</job_title>"
                    + "<keep_revisions>" + job.getKeepRevisions() + "</keep_revisions>"
                    + "<listDiffSeries1>" + job.getDiffSeries1() + "</listDiffSeries1>"
                    + "<listDiffSeries2>" + job.getDiffSeries2() + "</listDiffSeries2>"
                    + "</tmpl>");

    //  plot_cmd / plot_cond
    xmlStr.append(
            "<plot_cmd>" + job.getPlotCmd() + "</plot_cmd>" +
                    "<plot_cond>" + job.getPlotCond() + "</plot_cond>");

    //  execution_type
    xmlStr.append(
            "<execution_type>" + job.getExecutionType() + "</execution_type>");

    //  annotation_template
    if(!job.getAnnotationTemplate().isEmpty()) {
      xmlStr.append(
              "<annotation_template>" + job.getAnnotationTemplate() + "</annotation_template>");
    }

    //  plot fmt
    xmlStr.append(
            "<event_equal>" + job.getEventEqual() + "</event_equal>" +
                    "<vert_plot>" + job.getVertPlot() + "</vert_plot>" +
                    "<x_reverse>" + job.getXReverse() + "</x_reverse>" +
                    "<num_stats>" + job.getShowNStats() + "</num_stats>" +
                    "<indy1_stag>" + job.getIndy1Stagger() + "</indy1_stag>" +
                    "<indy2_stag>" + job.getIndy2Stagger() + "</indy2_stag>" +
                    "<grid_on>" + job.getGridOn() + "</grid_on>" +
                    "<sync_axes>" + job.getSyncAxes() + "</sync_axes>" +
                    "<dump_points1>" + job.getDumpPoints1() + "</dump_points1>" +
                    "<dump_points2>" + job.getDumpPoints2() + "</dump_points2>" +
                    "<log_y1>" + job.getLogY1() + "</log_y1>" +
                    "<log_y2>" + job.getLogY2() + "</log_y2>" +
                    "<plot_type>" + job.getPlotType() + "</plot_type>" +
                    "<plot_height>" + job.getPlotHeight() + "</plot_height>" +
                    "<plot_width>" + job.getPlotWidth() + "</plot_width>" +
                    "<plot_res>" + job.getPlotRes() + "</plot_res>" +
                    "<plot_units>" + job.getPlotUnits() + "</plot_units>" +
                    "<mar>" + job.getMar() + "</mar>" +
                    "<mgp>" + job.getMgp() + "</mgp>" +
                    "<cex>" + job.getCex() + "</cex>" +
                    "<title_weight>" + job.getTitleWeight() + "</title_weight>" +
                    "<title_size>" + job.getTitleSize() + "</title_size>" +
                    "<title_offset>" + job.getTitleOffset() + "</title_offset>" +
                    "<title_align>" + job.getTitleAlign() + "</title_align>" +
                    "<xtlab_orient>" + job.getXtlabOrient() + "</xtlab_orient>" +
                    "<xtlab_perp>" + job.getXtlabPerp() + "</xtlab_perp>" +
                    "<xtlab_horiz>" + job.getXtlabHoriz() + "</xtlab_horiz>" +
                    "<xtlab_freq>" + job.getXtlabFreq() + "</xtlab_freq>" +
                    "<xlab_weight>" + job.getXlabWeight() + "</xlab_weight>" +
                    "<xlab_size>" + job.getXlabSize() + "</xlab_size>" +
                    "<xtlab_size>" + job.getXtlabSize() + "</xtlab_size>" +
                    "<xlab_offset>" + job.getXlabOffset() + "</xlab_offset>" +
                    "<xlab_align>" + job.getYlabAlign() + "</xlab_align>" +
                    "<ytlab_orient>" + job.getYtlabOrient() + "</ytlab_orient>" +
                    "<ytlab_perp>" + job.getYtlabPerp() + "</ytlab_perp>" +
                    "<ytlab_horiz>" + job.getYtlabHoriz() + "</ytlab_horiz>" +
                    "<ylab_weight>" + job.getYlabWeight() + "</ylab_weight>" +
                    "<ylab_size>" + job.getYlabSize() + "</ylab_size>" +
                    "<ytlab_size>" + job.getYtlabSize() + "</ytlab_size>" +
                    "<ylab_offset>" + job.getYlabOffset() + "</ylab_offset>" +
                    "<ylab_align>" + job.getYlabAlign() + "</ylab_align>" +
                    "<grid_lty>" + job.getGridLty() + "</grid_lty>" +
                    "<grid_col>" + job.getGridCol() + "</grid_col>" +
                    "<grid_lwd>" + job.getGridLwd() + "</grid_lwd>" +
                    "<grid_x>" + job.getGridX() + "</grid_x>" +
                    "<x2tlab_orient>" + job.getX2tlabOrient() + "</x2tlab_orient>" +
                    "<x2tlab_perp>" + job.getX2tlabPerp() + "</x2tlab_perp>" +
                    "<x2tlab_horiz>" + job.getX2tlabHoriz() + "</x2tlab_horiz>" +
                    "<x2lab_weight>" + job.getX2labWeight() + "</x2lab_weight>" +
                    "<x2lab_size>" + job.getX2labSize() + "</x2lab_size>" +
                    "<x2tlab_size>" + job.getX2tlabSize() + "</x2tlab_size>" +
                    "<x2lab_offset>" + job.getX2labOffset() + "</x2lab_offset>" +
                    "<x2lab_align>" + job.getX2labAlign() + "</x2lab_align>" +
                    "<y2tlab_orient>" + job.getY2tlabOrient() + "</y2tlab_orient>" +
                    "<y2tlab_perp>" + job.getY2tlabPerp() + "</y2tlab_perp>" +
                    "<y2tlab_horiz>" + job.getY2tlabHoriz() + "</y2tlab_horiz>" +
                    "<y2lab_weight>" + job.getY2labWeight() + "</y2lab_weight>" +
                    "<y2lab_size>" + job.getY2labSize() + "</y2lab_size>" +
                    "<y2tlab_size>" + job.getY2tlabSize() + "</y2tlab_size>" +
                    "<y2lab_offset>" + job.getY2labOffset() + "</y2lab_offset>" +
                    "<y2lab_align>" + job.getY2labAlign() + "</y2lab_align>" +
                    "<legend_size>" + job.getLegendSize() + "</legend_size>" +
                    "<legend_box>" + job.getLegendBox() + "</legend_box>" +
                    "<legend_inset>" + job.getLegendInset() + "</legend_inset>" +
                    "<legend_ncol>" + job.getLegendNcol() + "</legend_ncol>" +
                    "<caption_weight>" + job.getCaptionWeight() + "</caption_weight>" +
                    "<caption_col>" + job.getCaptionCol() + "</caption_col>" +
                    "<caption_size>" + job.getCaptionSize() + "</caption_size>" +
                    "<caption_offset>" + job.getCaptionOffset() + "</caption_offset>" +
                    "<caption_align>" + job.getCaptionAlign() + "</caption_align>" +
                    "<box_pts>" + job.getBoxPts() + "</box_pts>" +
                    "<box_outline>" + job.getBoxOutline() + "</box_outline>" +
                    "<box_boxwex>" + job.getBoxBoxwex() + "</box_boxwex>" +
                    "<box_notch>" + job.getBoxNotch() + "</box_notch>" +
                    "<box_avg>" + job.getBoxAvg() + "</box_avg>" +
                    "<ci_alpha>" + job.getCIAlpha() + "</ci_alpha>" +

                    "<plot_ci>" + job.getPlotCI() + "</plot_ci>" +
                    "<plot_disp>" + job.getPlotDisp() + "</plot_disp>" +
                    "<show_signif>" + job.getShowSignif() + "</show_signif>" +
                    "<order_series>" + job.getOrderSeries() + "</order_series>" +
                    "<colors>" + job.getColors() + "</colors>" +
                    "<pch>" + job.getPch() + "</pch>" +
                    "<type>" + job.getType() + "</type>" +
                    "<lty>" + job.getLty() + "</lty>" +
                    "<lwd>" + job.getLwd() + "</lwd>" +
                    "<con_series>" + job.getConSeries() + "</con_series>" +
                    "<legend>" + sanitise(job.getLegend()) + "</legend>" +

                    "<y1_lim>" + job.getY1Lim() + "</y1_lim>" +
                    "<x1_lim>" + job.getX1Lim() + "</x1_lim>" +
                    "<y1_bufr>" + job.getY1Bufr() + "</y1_bufr>" +
                    "<y2_lim>" + job.getY2Lim() + "</y2_lim>" +
                    "<y2_bufr>" + job.getY2Bufr() + "</y2_bufr>" +
                    "<varianceInflationFactor>" + job.getVarianceInflationFactor()
                    + "</varianceInflationFactor>" +
                    "<normalized_histogram>" + job.getNormalizedHistogram() + "</normalized_histogram>" +
                    "<color_palette>" + job.getColorPalette() + "</color_palette>" +
                    "<add_color_bar>" + job.getAddColorBar() + "</add_color_bar>" +
                    "<reverse_y>" + job.getReverseY() + "</reverse_y>" +
                    "<reverse_x>" + job.getReverseX() + "</reverse_x>" +
                    "<add_contour_overlay>" + job.getAddContourOverlay() + "</add_contour_overlay>" +
                    "<plot_stat>" + job.getPlotStat() + "</plot_stat>" +
                    "<eqbound_low>" + job.getEqboundLow() + "</eqbound_low>" +
                    "<eqbound_high>" + job.getEqboundHigh() + "</eqbound_high>");
    if (job.getContourIntervals() != null) {
      xmlStr.append("<contour_intervals>" + job.getContourIntervals() + "</contour_intervals>");
    }

    //  close the plot job
    xmlStr.append("</plot></plot_spec>");
    return xmlStr;
  }
  private static String sanitise(String str){
    return str.replace("&", "&#38;")
            .replace(">", "&gt;")
            .replace("<", "&lt;");
  }

  private static String preserveBackslash(String str) {
    String result = str;
    if (str.contains("\\")) {
      result = str.replaceAll("\\\\", "\\\\\\\\");
    }
    return result;
  }

  public Document getDocument() {
    return doc;
  }

  public MVPlotJob[] getJobsList() {
    return Arrays.copyOf(listJobs, listJobs.length);
  }

  public MVOrderedMap getJobsMap() {
    return jobs;
  }

  public String getRtmplFolder() {
    return rtmplFolder;
  }

  public String getRworkFolder() {
    return rWorkFolder;
  }

  public String getPlotsFolder() {
    return plotsFolder;
  }

  public String getDataFolder() {
    return dataFolder;
  }

  public String getScriptsFolder() {
    return scriptsFolder;
  }

  protected void parsePlotJobSpec() throws ValidationException, DatabaseException {
    ArrayList listJobs = new ArrayList();
    String dbName = "";

    for (int i = 0; null != plotSpec && i < plotSpec.children.length; i++) {


      MVNode node = plotSpec.children[i];

      //  <connection>
      if (node.tag.equals("connection")) {
        for (int j = 0; j < node.children.length; j++) {
          if (node.children[j].tag.equals("host")) {
            dbHost = node.children[j].value;
          } else if (node.children[j].tag.equals("database")) {
            dbName = node.children[j].value;
          } else if (node.children[j].tag.equals("user")) {
            dbUser = node.children[j].value;
          } else if (node.children[j].tag.equals("password")) {
            dbPass = node.children[j].value;
          } else if (node.children[j].tag.equals("management_system")) {
            dbManagementSystem = node.children[j].value;
          } else if (node.children[j].tag.equals("driver")) {
            dbDriver = node.children[j].value;
          }
        }

      }

      //  <rscript>
      else if (node.tag.equals("rscript")) {
        rscript = node.value;
      }

      //  <folders>
      else if (node.tag.equals("folders")) {
        for (int j = 0; j < node.children.length; j++) {
          if (node.children[j].tag.equals("r_tmpl")) {
            rtmplFolder = node.children[j].value;
          } else if (node.children[j].tag.equals("r_work")) {
            rWorkFolder = node.children[j].value;
          } else if (node.children[j].tag.equals("plots")) {
            plotsFolder = node.children[j].value;
          } else if (node.children[j].tag.equals("data")) {
            dataFolder = node.children[j].value;
          } else if (node.children[j].tag.equals("scripts")) {
            scriptsFolder = node.children[j].value;
          }
        }
      }

      //  <date_list>
      else if (node.tag.equals("date_list")) {

        dateListDecl.put(node.name, MVUtil.buildDateList(node, System.out));
      }

      //  <date_range>
      else if (node.tag.equals("date_range")) {
        dateRangeDecl.put(node.name, parseDateRange(node));
      }

      //  <date_range_list>
      else if (node.tag.equals("date_range_list")) {

        //  gather the elements of the range list
        String rangeStart = "";
        String rangeEnd = "";
        int rangeLength = -1;
        int inc = -1;
        SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String format = formatDB.toPattern();
        for (int l = 0; l < node.children.length; l++) {
          MVNode nodeChild = node.children[l];
          if (nodeChild.tag.equals("range_start")) {
            rangeStart = (0 < nodeChild.children.length ? MVUtil.parseDateOffset(
                    nodeChild.children[0], format) : nodeChild.value);
          } else if (nodeChild.tag.equals("range_end")) {
            rangeEnd = (0 < nodeChild.children.length
                    ? MVUtil.parseDateOffset(nodeChild.children[0], format)
                    : nodeChild.value);
          } else if (nodeChild.tag.equalsIgnoreCase("range_length")) {
            rangeLength = Integer.parseInt(nodeChild.value);
          } else if (nodeChild.tag.equalsIgnoreCase("inc")) {
            inc = Integer.parseInt(nodeChild.value);
          }
        }

        //  parse the begin and end times
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long endTime = -1;
        try {
          cal.setTime(formatDB.parse(rangeStart));
          endTime = formatDB.parse(rangeEnd).getTime();
        } catch (ParseException e) {
          throw new ValidationException("range_start is invalid");
        }

        //  build the list
        ArrayList dateRange = new ArrayList();
        while (cal.getTime().getTime() <= endTime) {
          String strStartCur = formatDB.format(cal.getTime());
          cal.add(Calendar.MINUTE, rangeLength);
          String strEndCur = formatDB.format(cal.getTime());
          dateRange.add("BETWEEN '" + strStartCur + "' AND '" + strEndCur + "'");
          cal.add(Calendar.MINUTE, inc - rangeLength);
        }
        dateRangeListDecl.put(node.name, MVUtil.toArray(dateRange));
      }

      //  <plot>
      else if (node.tag.equals("plot")) {


        // parse the plot and add it to the job table and, if appropriate, the list of runnable jobs
        plotNode.put(node.name, node);
        String strInherits = node.inherits.trim();
        MVPlotJob job;

        if ("".equals(strInherits)) {
          job = parsePlotJob(node, null);
        } else {
          String[] listInherits = strInherits.split("\\s*,\\s*");
          if (!plotDecl.containsKey(listInherits[0])) {
            throw new ValidationException("inherited plot job " + listInherits[0] + " not found");
          }
          MVPlotJob jobBase = (MVPlotJob) plotDecl.get(listInherits[0]);
          for (int j = 1; j < listInherits.length; j++) {
            if (!plotNode.containsKey(listInherits[j])) {
              throw new ValidationException("multiple inherited plot job " + listInherits[j] + " not found");
            }
            MVNode nodeInherit = (MVNode) plotNode.get(listInherits[j]);
            jobBase = parsePlotJob(nodeInherit, jobBase);
          }
          job = parsePlotJob(node, jobBase);
        }

        //  set the job database information
        job.setRscript(rscript);
        List<String> databases = new ArrayList<>();
        String[] databasesArray = dbName.split(",");
        for (String db : databasesArray) {
          databases.add(db.trim());
        }

        job.setCurrentDBName(databases);

        //  check the job and add it to the jobs table and to the runnable jobs, if appropriate
        plotDecl.put(node.name, job);
        String strCompleteness = "";
        boolean boolPlotRun = !node.run.equalsIgnoreCase("false");

        if (job.getPlotTmpl().equals("roc.R_tmpl")) {

          //  ROC jobs must have an aggregation method selected
          if (!job.getRocPct() && !job.getRocCtc()) {
            strCompleteness = "if ROC template is selected, one of roc_pct or roc_ctc must be true";
          }

        } else if (job.getPlotTmpl().equals("ens_ss.R_tmpl")) {

          //  ensemble spread/skill must have a fcst_var selected
          if (job.getPlotFixVal().containsKey("fcst_var")) {
            MVOrderedMap mapDep = new MVOrderedMap();
            MVOrderedMap mapMse = new MVOrderedMap();
            Object objFcstVar = job.getPlotFixVal().get("fcst_var");
            String[] listFcstVar;
            if (objFcstVar instanceof String[]) {
              listFcstVar = (String[]) job.getPlotFixVal().get("fcst_var");
            } else {
              MVOrderedMap mapFcstVar = (MVOrderedMap) job.getPlotFixVal().get("fcst_var");
              listFcstVar = (String[]) mapFcstVar.get(mapFcstVar.getKeyList()[0]);
            }
            mapMse.put(listFcstVar[0], new String[]{"MSE"});
            mapDep.put("dep1", mapMse);
            mapDep.put("dep2", new MVOrderedMap());
            job.addDepGroup(mapDep);
          } else if (boolPlotRun) {
            strCompleteness = "if ens_ss template is selected, a FCST_VAR must be specified in plot_fix";
          }
        } else if (job.getPlotTmpl().equals("performance.R_tmpl")) {
          //  performance plot must have a fcst_var selected
          if (job.getPlotFixVal().containsKey("fcst_var")) {
            MVOrderedMap mapDep = new MVOrderedMap();
            MVOrderedMap mapFarPody = new MVOrderedMap();
            Object objFcstVar = job.getPlotFixVal().get("fcst_var");
            String[] listFcstVar;
            if (objFcstVar instanceof String[]) {
              listFcstVar = (String[]) job.getPlotFixVal().get("fcst_var");
            } else {
              MVOrderedMap mapFcstVar = (MVOrderedMap) job.getPlotFixVal().get("fcst_var");
              listFcstVar = (String[]) mapFcstVar.get(mapFcstVar.getKeyList()[0]);
            }
            if(job.getAggNbrCtc()){
              mapFarPody.put(listFcstVar[0], new String[]{"NBR_FAR", "NBR_PODY"});
            }else {
              mapFarPody.put(listFcstVar[0], new String[]{"FAR", "PODY"});
            }
            mapDep.put("dep1", mapFarPody);
            mapDep.put("dep2", new MVOrderedMap());
            job.addDepGroup(mapDep);
          } else if (boolPlotRun) {
            strCompleteness = " FCST_VAR must be specified in plot_fix";
          }

        } else if (!job.getPlotTmpl().equals("rhist.R_tmpl")
                && !job.getPlotTmpl().equals("rely.R_tmpl")
                && !job.getPlotTmpl().equals("phist.R_tmpl")
                && !job.getPlotTmpl().equals("relp.R_tmpl")
                && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
          strCompleteness = checkJobCompleteness(job);
        }
        if (strCompleteness.isEmpty()) {
          strCompleteness = checkJobFieldsOrder(job);
        }


        //  add runnable jobs to the run table if complete, complain otherwise
        if (strCompleteness.isEmpty()) {
          if (boolPlotRun) {
            jobs.put(node.name, job);
          }
          listJobs.add(job);
        } else if (boolPlotRun) {
          throw new ValidationException("plot " + node.name + ": " + strCompleteness);
        }
      }

    }

    this.listJobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[]{});
  }

  /**
   * Parse a single xml plot specification &lt;plot&gt; node from an xml plot specification and
   * return the resulting MVPlotJob.  The inherited job is specified by the input jobBase.
   *
   * @param nodePlot XML plot specification object to parse
   * @param jobBase  MVPlotJob whose characteristics to inherit
   * @return Populated MVPlot structure
   */
  public MVPlotJob parsePlotJob(MVNode nodePlot, MVPlotJob jobBase) throws DatabaseException, ValidationException {
    MVPlotJob job = (null != jobBase ? jobBase.copy() : new MVPlotJob());

    for (int i = 0; i < nodePlot.children.length; i++) {
      MVNode node = nodePlot.children[i];

      //  <template>
      if (node.tag.equals("template")) {
        job.setPlotTmpl(node.value);
      }

      //  <indep>
      else if (node.tag.equals("indep")) {
        String equalize = node.getAttribute("equalize");
        if ("true".equalsIgnoreCase(equalize) || "false".equalsIgnoreCase(equalize)) {
          job.setEqualizeByIndep(Boolean.valueOf(equalize));
        }

        job.setIndyVar(node.name);
        if (!"".equals(node.depends)) {
          job.setIndyDep(new MVPlotDep(node.depends, node));
        } else {
          String[][] listIndy = parseIndyNode(node, "");
          job.setIndyVal(listIndy[0]);
          job.setIndyLabel(listIndy[1]);
          job.setIndyPlotVal(listIndy[2]);
        }
      }

      //  <plot_fix>
      else if (node.tag.equals("plot_fix")) {

        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeFix = node.children[j];
          String equalize = nodeFix.getAttribute("equalize");
          //  <remove> and <clear>
          if (nodeFix.tag.equals("remove")) {
            job.removePlotFixVal(nodeFix.name);
            continue;
          } else if (nodeFix.tag.equals("clear")) {
            job.clearPlotFixVal();
            continue;
          }

          //  <field>
          ArrayList listFixVal;
          if (job.getPlotFixVal().containsKey(nodeFix.name)
                  && job.getPlotFixVal().get(nodeFix.name) instanceof String[]) {
            listFixVal = (ArrayList) MVUtil.toArrayList(
                    (String[]) job.getPlotFixVal().get(nodeFix.name));
          } else {
            listFixVal = new ArrayList();
          }
          MVOrderedMap mapFixVal = new MVOrderedMap();
          MVOrderedMap mapTmplVal = new MVOrderedMap();
          for (int k = 0; k < nodeFix.children.length; k++) {
            MVNode nodeFixVal = nodeFix.children[k];

            //  <val>
            if (nodeFixVal.tag.equals("val")) {
              listFixVal.add(nodeFixVal.value);
            }

            //  <set>
            else if (nodeFixVal.tag.equals("set")) {
              ArrayList listFixSet = new ArrayList();
              for (int l = 0; l < nodeFixVal.children.length; l++) {
                MVNode nodeFixSet = nodeFixVal.children[l];

                //  <val>
                if (nodeFixSet.tag.equals("val")) {
                  listFixSet.add(nodeFixSet.value);
                }

                //  <date_list>
                else if (nodeFixSet.tag.equals("date_list")) {
                  if (dateListDecl.get(nodeFixSet.name) instanceof List) {
                    listFixSet.addAll((List) dateListDecl.get(nodeFixSet.name));
                  } else {
                    listFixSet
                            .addAll(Arrays.asList((String[]) dateListDecl.get(nodeFixSet.name)));
                  }
                  mapTmplVal.put(nodeFix.name, nodeFixSet.name);
                }

                //  <date_range>
                else if (nodeFixSet.tag.equals("date_range")) {
                  throw new ValidationException("sets of date_range structures not supported");
                }

                //  <date_range_list>
                else if (nodeFixSet.tag.equals("date_range_list")) {
                  throw new ValidationException("sets of date_range_list structures not supported");
                }
              }
              mapFixVal.put(nodeFixVal.name, MVUtil.toArray(listFixSet));
            }

            //  <date_list>
            else if (nodeFixVal.tag.equals("date_list")) {
              if (dateListDecl.get(nodeFixVal.name) instanceof List) {
                listFixVal.addAll((List) dateListDecl.get(nodeFixVal.name));
              } else {
                listFixVal
                        .addAll(Arrays.asList((String[]) dateListDecl.get(nodeFixVal.name)));
              }
            }

            //  <date_range>
            else if (nodeFixVal.tag.equals("date_range")) {
              if (!dateRangeDecl.containsKey(nodeFixVal.name)) {
                throw new ValidationException("date_range " + nodeFixVal.name + " not found in plot_fix");
              }
              String strDateRangeVal = dateRangeDecl.get(nodeFixVal.name).toString();
              listFixVal.add(strDateRangeVal);
              mapTmplVal.put(strDateRangeVal, nodeFixVal.name);
              if (!nodeFixVal.id.equals("")) {
                job.addTmplVal(nodeFixVal.id, nodeFixVal.name);
              }
            }

            //  <date_range_list>
            else if (nodeFixVal.tag.equals("date_range_list")) {
              String[] listDateRange = (String[]) dateRangeListDecl.get(nodeFixVal.name);
              listFixVal.addAll(Arrays.asList(listDateRange));
            }

          }
          if (!listFixVal.isEmpty()) {
            job.addPlotFixVal(nodeFix.name, MVUtil.toArray(listFixVal));
            if ("true".equals(equalize)) {
              job.addPlotFixValEq(nodeFix.name, MVUtil.toArray(listFixVal));
            }
          } else if (0 < mapFixVal.size()) {
            job.addPlotFixVal(nodeFix.name, mapFixVal);
            if ("true".equals(equalize)) {
              job.addPlotFixValEq(nodeFix.name, mapFixVal);
            }
          }

          if (0 < mapTmplVal.size()) {
            job.addTmplMap(nodeFix.name, mapTmplVal);
          }
        }
      }

      //  <series1> or <series2>
      else if (node.tag.equals("series1") || node.tag.equals("series2")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeSeries = node.children[j];

          //  <remove>
          if (nodeSeries.tag.equals("remove")) {
            if (node.tag.equals("series1")) {
              job.removeSeries1Val(nodeSeries.name);
            } else if (node.tag.equals("series2")) {
              job.removeSeries1Val(nodeSeries.name);
            }
            continue;
          }

          //  <clear>
          else if (nodeSeries.tag.equals("clear")) {
            if (node.tag.equals("series1")) {
              job.clearSeries1Val();
            } else if (node.tag.equals("series2")) {
              job.clearSeries2Val();
            }
            continue;
          }

          //  <field>
          String[] listAggVal = new String[nodeSeries.children.length];
          for (int k = 0; k < nodeSeries.children.length; k++) {
            listAggVal[k] = nodeSeries.children[k].value;
          }
          if (node.tag.equals("series1")) {
            if (jobBase != null) {
              job.clearSeries1Val();
            }
            job.addSeries1Val(nodeSeries.name, listAggVal);
          } else if (node.tag.equals("series2")) {
            if (jobBase != null) {
              job.clearSeries2Val();
            }
            job.addSeries2Val(nodeSeries.name, listAggVal);
          }
        }
      }

      //  <series_nobs>
      else if (node.tag.equals("series_nobs")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeSeriesNobs = node.children[j];

          //  <remove> and <clear>
          if (nodeSeriesNobs.tag.equals("remove")) {
            job.removeSeriesNobs(nodeSeriesNobs.name);
            continue;
          } else if (nodeSeriesNobs.tag.equals("clear")) {
            job.clearSeriesNobs();
            continue;
          }

          //  <field>
          String strField = nodeSeriesNobs.name;
          String strValue = nodeSeriesNobs.children[0].value;
          job.addSeriesNobs(strField, strValue);
        }
      }

      //  <dep>
      else if (node.tag.equals("dep")) {

        //job.addDepGroup( buildDepMap(node) );

        //  <dep>
        MVOrderedMap mapDep = new MVOrderedMap();
        boolean boolDep1Present = false;
        boolean boolDep2Present = false;
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeDepN = node.children[j];

          //  <clear>
          if (nodeDepN.tag.equals("clear")) {
            job.clearDepGroups();
          }

          //  <mode_group>
          else if (nodeDepN.tag.equals("mode_group")) {
            throw new ValidationException(
                    "<mode_group> tag no longer supported, use multiple inheritance instead");
          }

          //  <dep1> or <dep2>
          else if (nodeDepN.tag.startsWith("dep")) {
            MVOrderedMap mapDepN = new MVOrderedMap();

            //  presence accounting
            if (nodeDepN.tag.equals("dep1")) {
              boolDep1Present = true;
            } else if (nodeDepN.tag.equals("dep2")) {
              boolDep2Present = true;
            }

            //  <fcst_var>
            for (int k = 0; k < nodeDepN.children.length; k++) {
              MVNode nodeFcstVar = nodeDepN.children[k];
              ArrayList listStats = new ArrayList();

              //  <stat>
              for (int l = 0; l < nodeFcstVar.children.length; l++) {
                String strStat = nodeFcstVar.children[l].value;
                if (!job.getPlotTmpl().startsWith("contour") && !isStatValid(strStat)) {
                  throw new ValidationException("unknown stat name " + strStat);
                }
                listStats.add(strStat);
              }
              mapDepN.put(nodeFcstVar.name, listStats.toArray(new String[]{}));
            }
            mapDep.put(nodeDepN.tag, mapDepN);
          }

          //  <fix>
          else if (nodeDepN.tag.startsWith("fix")) {
            throw new ValidationException("<dep> child <fix> no longer supported, use <plot_fix> instead");
          }
        }

        //  complain if a dep component is missing
        if (!boolDep1Present) {
          throw new ValidationException("plot job dep lacks dep1");
        }
        if (!boolDep2Present && !job.getPlotTmpl().contains("taylor")) {
          throw new ValidationException("plot job dep lacks dep2");
        }

        //  add the dep group to the job
        job.addDepGroup(mapDep);
      }

      //  <agg>
      else if (node.tag.equals("agg")) {
        throw new ValidationException("<agg> no longer supported, please change to <plot_fix>");
      }

      //  <taylor_voc>
      else if (node.tag.equals("taylor_voc")) {
        if ("true".equalsIgnoreCase(node.value) || "false".equalsIgnoreCase(node.value)) {
          job.setTaylorVoc(Boolean.parseBoolean(node.value));
        }
      }
      //  <taylor_show_gamma>
      else if (node.tag.equals("taylor_show_gamma")) {
        if ("true".equalsIgnoreCase(node.value) || "false".equalsIgnoreCase(node.value)) {
          job.setTaylorShowGamma(Boolean.parseBoolean(node.value));
        }
      }

      //  <tmpl>
      else if (node.tag.equals("tmpl")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeTmpl = node.children[j];

          //  <val_map>
          if (nodeTmpl.tag.equals("val_map")) {
            MVOrderedMap mapValMap = new MVOrderedMap();
            for (int k = 0; k < nodeTmpl.children.length; k++) {
              MVNode nodeKey = nodeTmpl.children[k].children[0];
              String strKey;
              if (0 < nodeKey.children.length && nodeKey.children[0].tag.equals("date_range")) {
                strKey = dateRangeDecl.get(nodeKey.children[0].name).toString();
              } else {
                strKey = nodeKey.value;
              }

              MVNode nodeVal = nodeTmpl.children[k].children[1];
              mapValMap.put(strKey, nodeVal.value);
            }
            job.addTmplMap(nodeTmpl.name, mapValMap);
          }

          //  templates
          else if (nodeTmpl.tag.equals("data_file")) {
            job.setDataFileTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("plot_file")) {
            job.setPlotFileTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("r_file")) {
            job.setRFileTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("title")) {
            job.setTitleTmpl(nodeTmpl.value.replaceAll("\"", "'"));
          } else if (nodeTmpl.tag.equals("x_label")) {
            job.setXLabelTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("y1_label")) {
            job.setY1LabelTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("y2_label")) {
            job.setY2LabelTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("caption")) {
            job.setCaptionTmpl(nodeTmpl.value);
          } else if (nodeTmpl.tag.equals("job_title")) {
            job.setJobTitleTmpl(nodeTmpl.value.replaceAll("\\s", "_"));
          } else if (nodeTmpl.tag.equals("keep_revisions")) {
            job.setKeepRevisions(nodeTmpl.value.equalsIgnoreCase("true"));
          } else if (nodeTmpl.tag.equalsIgnoreCase("listDiffSeries1")) {
            if (!job.getPlotTmpl().startsWith("contour")) {
              //validate listDiffSeries - make sure that differences
              // will be calculated for the same Forecast Variable and Statistic
              validateListDiffSeries(node, nodeTmpl);
            }
            job.setDiffSeries1(nodeTmpl.value);
          } else if (nodeTmpl.tag.equalsIgnoreCase("listDiffSeries2")) {
            validateListDiffSeries(node, nodeTmpl);
            job.setDiffSeries2(nodeTmpl.value);
          }

        }
      }

      //  <dep1_scale> <dep2_scale>
      else if (node.tag.equals("dep1_scale") || node.tag.equals("dep2_scale")) {

        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeDepScale = node.children[j];

          //  <remove>
          if (nodeDepScale.tag.equals("remove")) {
            if (node.tag.equals("dep1_scale")) {
              job.removeDep1Scale(nodeDepScale.name);
            } else if (node.tag.equals("dep2_scale")) {
              job.removeDep2Scale(nodeDepScale.name);
            }
          }

          //  <clear>
          else if (nodeDepScale.tag.equals("clear")) {
            if (node.tag.equals("dep1_scale")) {
              job.clearDep1Scale();
            } else if (node.tag.equals("dep2_scale")) {
              job.clearDep2Scale();
            }
          }

          //  <field>
          else if (node.tag.equals("dep1_scale")) {
            job.addDep1Scale(nodeDepScale.name, nodeDepScale.value);
          } else if (node.tag.equals("dep2_scale")) {
            job.addDep2Scale(nodeDepScale.name, nodeDepScale.value);
          }
        }

      }

      //  <bootstrapping>
      else if (node.tag.equals("bootstrapping")) {
        throw new ValidationException("<bootstrapping> tag no longer supported, use <agg_stat> instead");
      }

      //  <agg_stat>
      else if (node.tag.equals("agg_stat")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeAggStat = node.children[j];
          boolean val = nodeAggStat.value.equalsIgnoreCase("true");
          if (nodeAggStat.tag.equals("agg_ctc")) {
            job.setAggCtc(val);
          } else if (nodeAggStat.tag.equals("agg_nbrctc")) {
            job.setAggNbrCtc(val);
          } else if (nodeAggStat.tag.equals("agg_sl1l2")) {
            job.setAggSl1l2(val);
          } else if (nodeAggStat.tag.equals("agg_grad")) {
            job.setAggGrad(val);
          } else if (nodeAggStat.tag.equals("agg_sal1l2")) {
            job.setAggSal1l2(val);
          } else if (nodeAggStat.tag.equals("agg_pct")) {
            job.setAggPct(val);
          } else if (nodeAggStat.tag.equals("agg_nbrcnt")) {
            job.setAggNbrCnt(val);
          } else if (nodeAggStat.tag.equals("agg_ssvar")) {
            job.setAggSsvar(val);
          } else if (nodeAggStat.tag.equals("agg_vl1l2")) {
            job.setAggVl1l2(val);
          } else if (nodeAggStat.tag.equals("agg_val1l2")) {
            job.setAggVal1l2(val);
          } else if (nodeAggStat.tag.equals("agg_ecnt")) {
            job.setAggEcnt(val);
          } else if (nodeAggStat.tag.equals("agg_rps")) {
            job.setAggRps(val);
          } else if (nodeAggStat.tag.equals("boot_repl")) {
            job.setAggBootRepl(nodeAggStat.value);
          } else if (nodeAggStat.tag.equals("boot_random_seed")) {
            job.setAggBootRandomSeed(nodeAggStat.value);
          } else if (nodeAggStat.tag.equals("cl_step")) {
            job.setCl_step(nodeAggStat.value);
          } else if (nodeAggStat.tag.equals("boot_ci")) {
            job.setAggBootCI(nodeAggStat.value);
          } else if (nodeAggStat.tag.equals("eveq_dis")) {
            job.setEveqDis(val);
          } else if (nodeAggStat.tag.equals("cache_agg_stat")) {
            job.setCacheAggStat(val);
          }
        }

      }

      //  <calc_stat>
      else if (node.tag.equals("calc_stat")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeCalcStat = node.children[j];
          boolean val = nodeCalcStat.value.equalsIgnoreCase("true");
          if (nodeCalcStat.tag.equals("calc_ctc")) {
            job.setCalcCtc(val);
          } else if (nodeCalcStat.tag.equals("calc_sl1l2")) {
            job.setCalcSl1l2(val);
          } else if (nodeCalcStat.tag.equals("calc_grad")) {
            job.setCalcGrad(val);
          } else if (nodeCalcStat.tag.equals("calc_sal1l2")) {
            job.setCalcSal1l2(val);
          } else if (nodeCalcStat.tag.equals("calc_vl1l2")) {
            job.setCalcVl1l2(val);
          } else if (nodeCalcStat.tag.equals("calc_val1l2")) {
            job.setCalcVal1l2(val);
          }
        }

        if (job.getCalcCtc() && job.getCalcSl1l2() && job.getCalcSal1l2()
                && job.getCalcVl1l2() && job.getCalcVal1l2() && job.getCalcGrad()) {
          throw new ValidationException("invalid calc_stat setting - both calc_ctc and calc_sl1l2 "
                  + "and calc_sal1l2 and calc_vl1l2 and calc_grad are true");
        }
      }
      // <revis_stat />
      else if (node.tag.equals("revis_stat")) {
        job.setRevisionStat(Boolean.TRUE);
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeRevisStat = node.children[j];
          boolean val = nodeRevisStat.value.equalsIgnoreCase("true");
          if (nodeRevisStat.tag.equals("calc_ctc")) {
            job.setCalcCtc(val);
          } else if (nodeRevisStat.tag.equals("calc_sl1l2")) {
            job.setCalcSl1l2(val);
          } else if (nodeRevisStat.tag.equals("calc_grad")) {
            job.setCalcGrad(val);
          } else if (nodeRevisStat.tag.equals("calc_sal1l2")) {
            job.setCalcSal1l2(val);
          } else if (nodeRevisStat.tag.equals("calc_vl1l2")) {
            job.setCalcVl1l2(val);
          } else if (nodeRevisStat.tag.equals("revision_ac")) {
            job.setRevisionAc(val);
          } else if (nodeRevisStat.tag.equals("revision_run")) {
            job.setRevisionRun(val);
          }
        }
        if (job.getCalcCtc() && job.getCalcSl1l2() && job.getCalcSal1l2()
                && job.getCalcVl1l2() && job.getCalcVal1l2() && job.getCalcGrad()) {
          throw new ValidationException("invalid revis_stat setting - both calc_ctc and calc_sl1l2 "
                  + "and calc_sal1l2 and calc_vl1l2 and calc_grad are true");
        }

      }


      //  <roc_calc>
      else if (node.tag.equals("roc_calc")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeRocStat = node.children[j];
          if (nodeRocStat.tag.equals("roc_pct")) {
            job.setRocPct(nodeRocStat.value.equalsIgnoreCase("true"));
          } else if (nodeRocStat.tag.equals("roc_ctc")) {
            job.setRocCtc(nodeRocStat.value.equalsIgnoreCase("true"));
          }
        }

        if (!job.getRocPct() && !job.getRocCtc()) {
          throw new ValidationException("invalid roc_calc setting - neither roc_pct nor roc_ctc are true");
        }
        if (job.getRocPct() && job.getRocCtc()) {
          throw new ValidationException("invalid roc_calc setting - both roc_pct and roc_ctc are true");
        }
      } else if (node.tag.equals("summary_curve")) {
        for (int j = 0; j < node.children.length; j++) {
          MVNode nodeVal = node.children[j];
          if (nodeVal.tag.equals("val")) {
            job.addSummaryCurve(nodeVal.value);
          }
        }
      } else if (node.tag.equals("add_point_thresholds")) {
        job.setAddPointThresholds(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("add_skill_line")) {
        job.setAddSkillLine(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("inset_hist")) {
        job.setInsetHist(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("add_noskill_line")) {
        job.setAddNoSkillLine(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("add_reference_line")) {
        job.setAddReferenceLine(node.value.equalsIgnoreCase("true"));
      } else if (node.tag.equals("create_html")) {
        job.setCreateHtml(node.value.equalsIgnoreCase("true"));
      }

      //  <normalized_histogram>
      else if (node.tag.equals("normalized_histogram")) {
        job.setNormalizedHistogram(node.value.equalsIgnoreCase("true"));

      }


      //  boolean format settings
      else if (formatToBoolValues.containsKey(node.tag)) {
        Method m = formatToBoolValues.get(node.tag);
        try {
          m.invoke(job, node.value.equals("true"));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ExceptionInInitializerError e) {
          logger.info(
                  "  **  ERROR: caught " + e.getClass() + " parsing format boolean '"
                          + node.tag + "': " + e.getMessage());
        }
      }

      //  R string format settings
      else if (formatToStrValues.containsKey(node.tag)) {
        if (node.tag.equals("order_series")) {
          //validate order_series
          String strStat = node.value;
          if (!isOrderValid(strStat)) {
            throw new ValidationException("Series order is invalid " + strStat);
          }
        }
        Method m = formatToStrValues.get(node.tag);
        try {
          m.invoke(job, node.value);
        } catch (Exception e) {
          if (e instanceof InvocationTargetException) {
            throw new ValidationException(e.getMessage());
          } else {
            logger.info(
                    "  **  ERROR: caught " + e.getClass() + " parsing format string '"
                            + node.tag + "': " + e.getMessage());
            throw new ValidationException(e.getMessage());
          }
        }

      }

      //  report unused tags
      else {
        logger.info("  **  WARNING: unused plot tag '" + node.tag + "'");
      }
    }

    return job;
  }

  private void validateListDiffSeries(MVNode node, MVNode nodeTmpl) throws ValidationException {
    String[] diffSeries = nodeTmpl.value.split("c\\(");
    for (int k = 1; k < diffSeries.length; k++) {
      String[] diffSeriesArray = diffSeries[k].replace("\"", "").replace(")", "").split(",");
      String[] diffSeriesParametersArray = diffSeriesArray[0].split(" ");

      if (diffSeriesParametersArray.length > 2) {
        String variableStat = diffSeriesParametersArray[diffSeriesParametersArray.length - 2]
                + " "
                + diffSeriesParametersArray[diffSeriesParametersArray.length - 1];
        if (diffSeriesArray.length > 2 && diffSeriesArray[2].equals("DIFF")
                && !diffSeriesArray[1].endsWith(variableStat)) {
          throw new ValidationException("Difference curve "
                  + diffSeries[k]
                  + " configured to be calculated using different "
                  + "variable and/or statistic."
                  + " It isn't supported by Image Viewer.");
        }
      }
    }
    //validate listDiffSeries - make sure that MODE Attribute stats are not in the list
    for (String stat : MVUtil.modeSingleStatField.keySet()) {
      if (node.value.indexOf(stat) > 0) {
        throw new ValidationException("MODE Attribute stats " + stat
                + " can't be a part of difference curve.");
      }
    }
    for (String stat : MVUtil.modePairStatField.keySet()) {
      if (node.value.indexOf(stat) > 0) {
        throw new ValidationException("MODE Attribute stats " + stat
                + " can't be a part of difference curve.");
      }
    }
  }

  /**
   * Determine if the input statistic name is valid by searching the tables of supported
   * statistics.
   *
   * @param strStat name of statistic to test for validity
   * @return true if valid, false otherwise
   */
  public boolean isStatValid(String strStat) {
    return !MVUtil.getStatTable(strStat).equals("");
  }

  /**
   * Determine if the input order name is valid by validating the order of integers
   *
   * @param strStat order of series
   * @return true if valid, false otherwise
   */
  public boolean isOrderValid(String strStat) throws ValidationException {
    //c(1, 3, 2)
    boolean result = true;
    if (!strStat.equals("c()")) {
      List<Integer> inInts = new ArrayList<>();
      String[] inChars = strStat.split("\\(")[1].split("\\)")[0].split(",");
      for (String ch : inChars) {
        Integer order;
        try {
          order = Integer.valueOf(ch.trim());
          inInts.add(order);
        } catch (NumberFormatException e) {
          throw new ValidationException("the order value is invalid");
        }
      }
      Collections.sort(inInts);
      for (int i = 0; i < inInts.size(); i++) {
        if (!inInts.get(i).equals(i + 1)) {
          result = false;
          break;
        }
      }
    }
    return result;
  }

  public DatabaseInfo getDatabaseInfo() {
    DatabaseInfo databaseInfo = null;
    if (dbHost != null && dbUser != null) {
      databaseInfo = new DatabaseInfo(dbHost, dbUser);
    }
    return databaseInfo;
  }

}

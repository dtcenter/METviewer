package edu.ucar.metviewer;

import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class MVPlotJobParser extends MVUtil {

  protected static final Map<String, Method> _tableFormatBoolean = new HashMap<>();
  private static final Logger _logger = LogManager.getLogger("MVPlotJobParser");
  private static final Map<String, Method> _tableFormatString = new HashMap<>();

  static {
    try {
      _tableFormatBoolean.put("vert_plot", MVPlotJob.class.getDeclaredMethod("setVertPlot", boolean.class));
      _tableFormatBoolean.put("x_reverse", MVPlotJob.class.getDeclaredMethod("setXReverse", boolean.class));
      _tableFormatBoolean.put("num_stats", MVPlotJob.class.getDeclaredMethod("setShowNStats", boolean.class));
      _tableFormatBoolean.put("indy1_stag", MVPlotJob.class.getDeclaredMethod("setIndy1Stagger", boolean.class));
      _tableFormatBoolean.put("indy2_stag", MVPlotJob.class.getDeclaredMethod("setIndy2Stagger", boolean.class));
      _tableFormatBoolean.put("grid_on", MVPlotJob.class.getDeclaredMethod("setGridOn", boolean.class));
      _tableFormatBoolean.put("sync_axes", MVPlotJob.class.getDeclaredMethod("setSyncAxes", boolean.class));
      _tableFormatBoolean.put("dump_points1", MVPlotJob.class.getDeclaredMethod("setDumpPoints1", boolean.class));
      _tableFormatBoolean.put("dump_points2", MVPlotJob.class.getDeclaredMethod("setDumpPoints2", boolean.class));
      _tableFormatBoolean.put("log_y1", MVPlotJob.class.getDeclaredMethod("setLogY1", boolean.class));
      _tableFormatBoolean.put("log_y2", MVPlotJob.class.getDeclaredMethod("setLogY2", boolean.class));
      _tableFormatBoolean.put("varianceInflationFactor", MVPlotJob.class.getDeclaredMethod("setVarianceInflationFactor", boolean.class));
      _tableFormatBoolean.put("varianceinflationfactor", MVPlotJob.class.getDeclaredMethod("setVarianceInflationFactor", boolean.class));
      _tableFormatBoolean.put("normalizedHistogram", MVPlotJob.class.getDeclaredMethod("setNormalizedHistogram", boolean.class));
      _tableFormatBoolean.put("cache_agg_stat", MVPlotJob.class.getDeclaredMethod("setCacheAggStat", boolean.class));
      _tableFormatBoolean.put("event_equal", MVPlotJob.class.getDeclaredMethod("setEventEqual", Boolean.class));
      _tableFormatBoolean.put("taylor_voc", MVPlotJob.class.getDeclaredMethod("setTaylorVoc", boolean.class));
      _tableFormatBoolean.put("taylor_show_gamma", MVPlotJob.class.getDeclaredMethod("setTaylorShowGamma", boolean.class));

    } catch (NoSuchMethodException e) {
      _logger.error(e.getMessage());
    }
  }

  static {
    try {
      _tableFormatString.put("plot_type", MVPlotJob.class.getDeclaredMethod("setPlotType", String.class));
      _tableFormatString.put("plot_height", MVPlotJob.class.getDeclaredMethod("setPlotHeight", String.class));
      _tableFormatString.put("plot_width", MVPlotJob.class.getDeclaredMethod("setPlotWidth", String.class));
      _tableFormatString.put("plot_res", MVPlotJob.class.getDeclaredMethod("setPlotRes", String.class));
      _tableFormatString.put("plot_units", MVPlotJob.class.getDeclaredMethod("setPlotUnits", String.class));
      _tableFormatString.put("mar", MVPlotJob.class.getDeclaredMethod("setMar", String.class));
      _tableFormatString.put("mgp", MVPlotJob.class.getDeclaredMethod("setMgp", String.class));
      _tableFormatString.put("cex", MVPlotJob.class.getDeclaredMethod("setCex", String.class));
      _tableFormatString.put("title_weight", MVPlotJob.class.getDeclaredMethod("setTitleWeight", String.class));
      _tableFormatString.put("title_size", MVPlotJob.class.getDeclaredMethod("setTitleSize", String.class));
      _tableFormatString.put("title_offset", MVPlotJob.class.getDeclaredMethod("setTitleOffset", String.class));
      _tableFormatString.put("title_align", MVPlotJob.class.getDeclaredMethod("setTitleAlign", String.class));
      _tableFormatString.put("xtlab_orient", MVPlotJob.class.getDeclaredMethod("setXtlabOrient", String.class));
      _tableFormatString.put("xtlab_perp", MVPlotJob.class.getDeclaredMethod("setXtlabPerp", String.class));
      _tableFormatString.put("xtlab_horiz", MVPlotJob.class.getDeclaredMethod("setXtlabHoriz", String.class));
      _tableFormatString.put("xtlab_freq", MVPlotJob.class.getDeclaredMethod("setXtlabFreq", String.class));
      _tableFormatString.put("xtlab_size", MVPlotJob.class.getDeclaredMethod("setXtlabSize", String.class));
      _tableFormatString.put("xlab_weight", MVPlotJob.class.getDeclaredMethod("setXlabWeight", String.class));
      _tableFormatString.put("xlab_size", MVPlotJob.class.getDeclaredMethod("setXlabSize", String.class));
      _tableFormatString.put("xlab_offset", MVPlotJob.class.getDeclaredMethod("setXlabOffset", String.class));
      _tableFormatString.put("xlab_align", MVPlotJob.class.getDeclaredMethod("setXlabAlign", String.class));
      _tableFormatString.put("ytlab_orient", MVPlotJob.class.getDeclaredMethod("setYtlabOrient", String.class));
      _tableFormatString.put("ytlab_perp", MVPlotJob.class.getDeclaredMethod("setYtlabPerp", String.class));
      _tableFormatString.put("ytlab_horiz", MVPlotJob.class.getDeclaredMethod("setYtlabHoriz", String.class));
      _tableFormatString.put("ytlab_size", MVPlotJob.class.getDeclaredMethod("setYtlabSize", String.class));
      _tableFormatString.put("ylab_weight", MVPlotJob.class.getDeclaredMethod("setYlabWeight", String.class));
      _tableFormatString.put("ylab_size", MVPlotJob.class.getDeclaredMethod("setYlabSize", String.class));
      _tableFormatString.put("ylab_offset", MVPlotJob.class.getDeclaredMethod("setYlabOffset", String.class));
      _tableFormatString.put("ylab_align", MVPlotJob.class.getDeclaredMethod("setYlabAlign", String.class));
      _tableFormatString.put("grid_lty", MVPlotJob.class.getDeclaredMethod("setGridLty", String.class));
      _tableFormatString.put("grid_col", MVPlotJob.class.getDeclaredMethod("setGridCol", String.class));
      _tableFormatString.put("grid_lwd", MVPlotJob.class.getDeclaredMethod("setGridLwd", String.class));
      _tableFormatString.put("grid_x", MVPlotJob.class.getDeclaredMethod("setGridX", String.class));
      _tableFormatString.put("x2tlab_orient", MVPlotJob.class.getDeclaredMethod("setX2tlabOrient", String.class));
      _tableFormatString.put("x2tlab_perp", MVPlotJob.class.getDeclaredMethod("setX2tlabPerp", String.class));
      _tableFormatString.put("x2tlab_horiz", MVPlotJob.class.getDeclaredMethod("setX2tlabHoriz", String.class));
      _tableFormatString.put("x2tlab_size", MVPlotJob.class.getDeclaredMethod("setX2tlabSize", String.class));
      _tableFormatString.put("x2lab_weight", MVPlotJob.class.getDeclaredMethod("setX2labWeight", String.class));
      _tableFormatString.put("x2lab_size", MVPlotJob.class.getDeclaredMethod("setX2labSize", String.class));
      _tableFormatString.put("x2lab_offset", MVPlotJob.class.getDeclaredMethod("setX2labOffset", String.class));
      _tableFormatString.put("x2lab_align", MVPlotJob.class.getDeclaredMethod("setX2labAlign", String.class));
      _tableFormatString.put("y2tlab_orient", MVPlotJob.class.getDeclaredMethod("setY2tlabOrient", String.class));
      _tableFormatString.put("y2tlab_perp", MVPlotJob.class.getDeclaredMethod("setY2tlabPerp", String.class));
      _tableFormatString.put("y2tlab_horiz", MVPlotJob.class.getDeclaredMethod("setY2tlabHoriz", String.class));
      _tableFormatString.put("y2tlab_size", MVPlotJob.class.getDeclaredMethod("setY2tlabSize", String.class));
      _tableFormatString.put("y2lab_weight", MVPlotJob.class.getDeclaredMethod("setY2labWeight", String.class));
      _tableFormatString.put("y2lab_size", MVPlotJob.class.getDeclaredMethod("setY2labSize", String.class));
      _tableFormatString.put("y2lab_offset", MVPlotJob.class.getDeclaredMethod("setY2labOffset", String.class));
      _tableFormatString.put("y2lab_align", MVPlotJob.class.getDeclaredMethod("setY2labAlign", String.class));
      _tableFormatString.put("legend_size", MVPlotJob.class.getDeclaredMethod("setLegendSize", String.class));
      _tableFormatString.put("legend_box", MVPlotJob.class.getDeclaredMethod("setLegendBox", String.class));
      _tableFormatString.put("legend_inset", MVPlotJob.class.getDeclaredMethod("setLegendInset", String.class));
      _tableFormatString.put("legend_ncol", MVPlotJob.class.getDeclaredMethod("setLegendNcol", String.class));
      _tableFormatString.put("caption_weight", MVPlotJob.class.getDeclaredMethod("setCaptionWeight", String.class));
      _tableFormatString.put("caption_col", MVPlotJob.class.getDeclaredMethod("setCaptionCol", String.class));
      _tableFormatString.put("caption_size", MVPlotJob.class.getDeclaredMethod("setCaptionSize", String.class));
      _tableFormatString.put("caption_offset", MVPlotJob.class.getDeclaredMethod("setCaptionOffset", String.class));
      _tableFormatString.put("caption_align", MVPlotJob.class.getDeclaredMethod("setCaptionAlign", String.class));
      _tableFormatString.put("box_pts", MVPlotJob.class.getDeclaredMethod("setBoxPts", String.class));
      _tableFormatString.put("box_outline", MVPlotJob.class.getDeclaredMethod("setBoxOutline", String.class));
      _tableFormatString.put("box_boxwex", MVPlotJob.class.getDeclaredMethod("setBoxBoxwex", String.class));
      _tableFormatString.put("box_notch", MVPlotJob.class.getDeclaredMethod("setBoxNotch", String.class));
      _tableFormatString.put("box_avg", MVPlotJob.class.getDeclaredMethod("setBoxAvg", String.class));
      _tableFormatString.put("rely_event_hist", MVPlotJob.class.getDeclaredMethod("setRelyEventHist", String.class));
      _tableFormatString.put("ci_alpha", MVPlotJob.class.getDeclaredMethod("setCIAlpha", String.class));
      _tableFormatString.put("ensss_pts", MVPlotJob.class.getDeclaredMethod("setEnsSsPts", String.class));
      _tableFormatString.put("ensss_pts_disp", MVPlotJob.class.getDeclaredMethod("setEnsSsPtsDisp", String.class));

      _tableFormatString.put("plot_ci", MVPlotJob.class.getDeclaredMethod("setPlotCI", String.class));
      _tableFormatString.put("plot_disp", MVPlotJob.class.getDeclaredMethod("setPlotDisp", String.class));
      _tableFormatString.put("show_signif", MVPlotJob.class.getDeclaredMethod("setShowSignif", String.class));
      //_tableFormatString.put("listDiffSeries1", MVPlotJob.class.getDeclaredMethod("setDiffSeries1", new Class[]{String.class}));
      //_tableFormatString.put("listDiffSeries2", MVPlotJob.class.getDeclaredMethod("setDiffSeries2", new Class[]{String.class}));
      _tableFormatString.put("order_series", MVPlotJob.class.getDeclaredMethod("setOrderSeries", String.class));
      _tableFormatString.put("colors", MVPlotJob.class.getDeclaredMethod("setColors", String.class));
      _tableFormatString.put("pch", MVPlotJob.class.getDeclaredMethod("setPch", String.class));
      _tableFormatString.put("type", MVPlotJob.class.getDeclaredMethod("setType", String.class));
      _tableFormatString.put("lty", MVPlotJob.class.getDeclaredMethod("setLty", String.class));
      _tableFormatString.put("lwd", MVPlotJob.class.getDeclaredMethod("setLwd", String.class));
      _tableFormatString.put("con_series", MVPlotJob.class.getDeclaredMethod("setConSeries", String.class));
      _tableFormatString.put("legend", MVPlotJob.class.getDeclaredMethod("setLegend", String.class));
      _tableFormatString.put("y1_lim", MVPlotJob.class.getDeclaredMethod("setY1Lim", String.class));
      _tableFormatString.put("y1_bufr", MVPlotJob.class.getDeclaredMethod("setY1Bufr", String.class));
      _tableFormatString.put("y2_lim", MVPlotJob.class.getDeclaredMethod("setY2Lim", String.class));
      _tableFormatString.put("y2_bufr", MVPlotJob.class.getDeclaredMethod("setY2Bufr", String.class));
      _tableFormatString.put("plot_cmd", MVPlotJob.class.getDeclaredMethod("setPlotCmd", String.class));
      _tableFormatString.put("plot_cond", MVPlotJob.class.getDeclaredMethod("setPlotCond", String.class));
      _tableFormatString.put("plot_stat", MVPlotJob.class.getDeclaredMethod("setPlotStat", String.class));
    } catch (NoSuchMethodException e) {
      _logger.error(e.getMessage());
    }
  }

  protected final MVOrderedMap _mapJobs = new MVOrderedMap();
  protected final Map _tableDateListDecl = new HashMap<>();
  protected final Map _tableDateRangeDecl = new HashMap<>();
  protected final Map _tableDateRangeListDecl = new HashMap<>();
  protected final Map _tablePlotDecl = new HashMap<>();
  protected final Map _tablePlotNode = new HashMap<>();
  protected Document _doc = null;
  protected MVPlotJob[] _listJobs = {};
  protected MVNode _nodePlotSpec = null;
  protected String _strRscript = "Rscript";
  protected String _strRtmplFolder = "";
  protected String _strRworkFolder = "";
  protected String _strPlotsFolder = "";
  protected String _strDataFolder = "";
  protected String _strScriptsFolder = "";
  protected String strDBHost = "";
  protected String strDBUser = "";
  protected String strDBPassword = "";
  protected String strDBDriver = null;


  /**
   * Build a parser whose input source is the specified URI
   *
   * @param spec URI of the XML plot specification source
   */
  public MVPlotJobParser(String spec) throws Exception {
    super();
    DocumentBuilder builder = getDocumentBuilder();
    //  parse the input document and build the MVNode data structure
    _doc = builder.parse(spec);
    _nodePlotSpec = new MVNode(_doc.getFirstChild());

    parsePlotJobSpec();
  }


  /**
   * Build a parser whose input source is the specified InputStream
   *
   * @param in Stream from which the plot specification will be drawn
   */
  public MVPlotJobParser(InputStream in) throws Exception {
    super();
    DocumentBuilder builder = getDocumentBuilder();

    //  parse the input document and build the MVNode data structure
    _doc = builder.parse(in);
    _nodePlotSpec = new MVNode(_doc.getFirstChild());
    parsePlotJobSpec();
  }

  /**
   * Build a parser whose input source is the plot_spec MVNode
   *
   * @param node plot_spec MVNode to parse
   */
  public MVPlotJobParser(MVNode node) throws Exception {
    super();
    _nodePlotSpec = node;
    parsePlotJobSpec();
  }

  /**
   * Create a parser-specific instance of the DocumentBuilder and return it
   */
  public static DocumentBuilder getDocumentBuilder() throws Exception {

    //  instantiate and configure the xml parser

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder();
  }

  /**
   * Determine if the input plot job has many necessary components to build a plot.  If not, return the structure name that has been found to be missing.
   *
   * @param job plot job to inspect
   * @return name of missing structure, or an empty string if the job is ok
   */
  public static String checkJobCompleteness(MVPlotJob job) {
    if (job.getPlotTmpl().isEmpty()) {
      return "lacks template";
    } else if (job.getIndyVar().isEmpty() && !job.getPlotTmpl().contains("taylor") && !job.getPlotTmpl().contains("eclv")) {
      return "lacks indep";
    } else if (1 > job.getIndyVal().length && null == job.getIndyDep() && !job.getPlotTmpl().contains("taylor") && !job.getPlotTmpl().contains("eclv")) {
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
    } else if ((job.getAggCtc() || job.getAggSl1l2() || job.getAggSal1l2()) && (job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() || job.getCalcGrad() || job.getAggGrad())) {
      return "has both agg_stat and calc_stat";
    }
    return "";
  }

  /**
   * Determine if the input plot job fields have been introduced in the alphabetical order . If not, return the structure name that has been found to be
   * incorrect.
   *
   * @param job plot job to inspect
   * @return name of missing structure, or an empty string if the job is ok
   */
  public static String checkJobFieldsOrder(MVPlotJob job) {
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
    if (!job.getIndyVar().startsWith("fcst_thresh") && !job.getIndyVar().startsWith("obs_thresh") && !job.getIndyVar().startsWith("fcst_lev")) {
      if (job.getIndyVar().startsWith("fcst_lead") || job.getIndyVar().startsWith("valid_hour") || job.getIndyVar().startsWith("init_hour")) {
        Integer[] valuesSortedInt = new Integer[indyVals.length];
        for (int i = 0; i < indyVals.length; i++) {
          try {
            valuesSortedInt[i] = Integer.valueOf(indyVals[i]);
          } catch (Exception e) {
            _logger.error(e.getMessage());
          }
        }
        Arrays.sort(valuesSortedInt);
        for (int i = 0; i < indyVals.length; i++) {
          try {
            if (!Integer.valueOf(indyVals[i]).equals(valuesSortedInt[i])) {
              result = result + "Values for variable " + job.getIndyVar() + " are not sorted";
            }
          } catch (Exception e) {
            _logger.error(e.getMessage());
          }
        }
      } else {
        String[] indyValsSorted = Arrays.copyOf(indyVals, indyVals.length);
        Arrays.sort(indyValsSorted);
        if (!Arrays.equals(indyVals, indyValsSorted)) {
          result = result + "Values for variable " + job.getIndyVar() + " are not sorted";
        }
      }
    }
    return result;
  }

  private static String checkOrder(Map.Entry[] mapValues) {
    for (Map.Entry entry : mapValues) {

      Object valuesObj = entry.getValue();

      if (valuesObj instanceof String[]) {
        String[] values = (String[]) entry.getValue();
        if (!String.valueOf(entry.getKey()).startsWith("fcst_thresh") && !String.valueOf(entry.getKey()).startsWith("obs_thresh") && !String.valueOf(entry.getKey()).startsWith("fcst_lev")) {
          if (String.valueOf(entry.getKey()).startsWith("fcst_lead") || String.valueOf(entry.getKey()).startsWith("valid_hour") || String.valueOf(entry.getKey()).startsWith("init_hour")) {
            Integer[] valuesSortedInt = new Integer[values.length];
            for (int i = 0; i < values.length; i++) {
              try {
                valuesSortedInt[i] = Integer.valueOf(values[i]);
              } catch (Exception e) {
                _logger.error(e.getMessage());
              }
            }
            Arrays.sort(valuesSortedInt);
            for (int i = 0; i < values.length; i++) {
              try {
                if (!Integer.valueOf(values[i]).equals(valuesSortedInt[i])) {
                  return "Values for variable " + entry.getKey().toString() + " are not sorted";
                }
              } catch (Exception e) {
                _logger.error(e.getMessage());
              }
            }
          } else {
            String[] valuesSorted = Arrays.copyOf(values, values.length);
            Arrays.sort(valuesSorted);
            if (!Arrays.equals(values, valuesSorted)) {
              return "Values for variable " + entry.getKey().toString() + " are not sorted";
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
  public static String parseDateRange(MVNode nodeDateRange) {
    String strStart = "";
    String strEnd = "";
    SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
    String strFormat = formatDB.toPattern();
    for (int j = 0; j < nodeDateRange._children.length; j++) {
      MVNode nodeChild = nodeDateRange._children[j];
      if (nodeChild._tag.equals("start")) {
        strStart = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value);
      } else if (nodeChild._tag.equals("end")) {
        strEnd = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value);
      }
    }
    return "BETWEEN '" + strStart + "' AND '" + strEnd + "'";
  }

  /**
   * Parse the &lt;indep&gt; node of a xml plot specification, returning the parsed information in the form of two lists.  The first list contains the
   * independent variable values, the second list contains the labels and the third contains the plot values.
   *
   * @param node XML plot specification &lt;indep&gt; node
   * @param dep  (optional) String representation of a dependency value date
   * @return Two lists of independent variable values and labels, respectively
   */
  public static String[][] parseIndyNode(MVNode node, String dep) {
    int intIndyNum = node._children.length;
    List<String> listIndyVal = new ArrayList<>();
    List<String> listIndyLabel = new ArrayList<>();
    List<String> listIndyPlotVal = new ArrayList<>();
    for (int j = 0; j < intIndyNum; j++) {
      MVNode nodeIndyVal = node._children[j];

      //  <val>
      if (nodeIndyVal._tag.equals("val")) {
        listIndyVal.add(nodeIndyVal._value);
        if (!nodeIndyVal._label.equals("")) {
          listIndyLabel.add(nodeIndyVal._label);
        } else {
          listIndyLabel.add(nodeIndyVal._value);
        }
        if (!nodeIndyVal._plotVal.equals("")) {
          listIndyPlotVal.add(nodeIndyVal._plotVal);
        }
      }

      //  <date_list>
      else if (nodeIndyVal._tag.equalsIgnoreCase("date_list")) {
        String strStart = "";
        String strEnd = "";
        int intInc = 0;
        SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strFormat = formatDB.toPattern();

        for (int k = 0; k < nodeIndyVal._children.length; k++) {
          MVNode nodeChild = nodeIndyVal._children[k];
          if (nodeChild._tag.equals("start")) {
            strStart = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat, dep) : nodeChild._value);
          } else if (nodeChild._tag.equals("end")) {
            strEnd = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat, dep) : nodeChild._value);
          } else if (nodeChild._tag.equals("inc")) {
            intInc = Integer.parseInt(nodeChild._value);
          } else if (nodeChild._tag.equals("label_format")) {
            strFormat = nodeChild._value;
          }
        }

        SimpleDateFormat formatLabel = new SimpleDateFormat(strFormat, Locale.US);
        formatLabel.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<String> listDates = buildDateList(strStart, strEnd, intInc, formatDB.toPattern(), System.out);
        List<String> listLabels = new ArrayList<>();
        for (String listDate : listDates) {
          try {
            listLabels.add(formatLabel.format(formatDB.parse(listDate)));
          } catch (Exception e) {
          }
        }

        listIndyVal.addAll(listDates);
        listIndyLabel.addAll(listLabels);
      }
    }

    return new String[][]{toArray(listIndyVal), toArray(listIndyLabel), toArray(listIndyPlotVal)};
  }

  public static StringBuilder serializeJob(MVPlotJob job, DatabaseInfo databaseInfo) {

    //  database information
    String databases = "";
    for (String db : job.getCurrentDBName()) {
      databases = databases + db + ",";
    }
    databases = databases.substring(0, databases.length() - 1);
    StringBuilder strXML = new StringBuilder(
      "<plot_spec>" +
        "<connection>" +
        "<host>" + databaseInfo.getHost() + "</host>" +
        "<database>" + databases + "</database>" +
        "<user>" + "******" + "</user>" +
        "<password>" + "******" + "</password>" +
        "</connection>" +
        "<plot>");

    //  plot template
    strXML.append("<template>").append(job.getPlotTmpl()).append("</template>");

    for (int intY = 1; intY <= 2; intY++) {

      //  get the series for the current y-axis
      MVOrderedMap mapSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val());
      strXML.append("<series").append(intY).append('>');

      //  serialize each fcst_var and it's vals
      String[] listSeriesField = mapSeries.getKeyList();

      for (int i = 0; i < listSeriesField.length; i++) {

        String fieldName = listSeriesField[i];
        String strXMLSimpleValues = "";
        String[] listSeriesVal = (String[]) mapSeries.get(i);
        for (int j = 0; j < listSeriesVal.length; j++) {
          if (listSeriesVal[j].contains(",")) {
            strXML.append("<field name=\"").append(fieldName).append("\">");
            if (fieldName.equals("init_hour")) {
              String strHour = listSeriesVal[j];
              while (strHour.length() < 2) {
                strHour = "0" + strHour;
              }
              strXML.append("<val>").append(strHour).append("</val>");
            } else {
              strXML.append("<val>").append(listSeriesVal[j]).append("</val>");
            }
            strXML.append("</field>");
          } else {
            if (strXMLSimpleValues.length() == 0) {
              strXMLSimpleValues = "<field name=\"" + fieldName + "\">";
            }
            if (fieldName.equals("init_hour")) {
              String strHour = listSeriesVal[j];
              while (strHour.length() < 2) {
                strHour = "0" + strHour;
              }
              strXMLSimpleValues += "<val>" + strHour + "</val>";
            } else {
              strXMLSimpleValues += "<val>" + listSeriesVal[j].replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "</val>";
            }
          }

        }
        if (strXMLSimpleValues.length() > 0) {
          strXMLSimpleValues += "</field>";
        }
        strXML.append(strXMLSimpleValues);
      }
      strXML.append("</series" + intY + ">");
    }


    //  if there are dep,  and indep elements present, handle them
    if (!job.getPlotTmpl().startsWith("rhist") && !job.getPlotTmpl().startsWith("phist") && !job.getPlotTmpl().startsWith("roc")
      && !job.getPlotTmpl().startsWith("rely") && !job.getPlotTmpl().startsWith("relp") && !job.getPlotTmpl().startsWith("eclv")) {
      int axis = 2;
      if (job.getPlotTmpl().startsWith("taylor")) {
        axis = 1;
      }
      // dep
      strXML.append("<dep>");
      MVOrderedMap[] listDepGroup = job.getDepGroups();
      for (int intY = 1; intY <= axis; intY++) {

        //  get the list of fcst_var for the current dep
        MVOrderedMap mapDep = (MVOrderedMap) listDepGroup[0].get("dep" + intY);
        Map.Entry[] listDep = mapDep.getOrderedEntries();

        //  serialize the dep and it's fcst_var stats
        String strDep = "dep" + intY;
        strXML.append("<" + strDep + ">");
        for (int i = 0; i < listDep.length; i++) {
          String[] listStat = (String[]) listDep[i].getValue();
          strXML.append("<fcst_var name=\"" + listDep[i].getKey().toString() + "\">");
          for (int j = 0; j < listStat.length; j++) {
            strXML.append("<stat>" + listStat[j] + "</stat>");
          }
          strXML.append("</fcst_var>");
        }
        strXML.append("</" + strDep + ">");
      }
      strXML.append("</dep>");


      //  indep
      strXML.append("<indep name=\"" + job.getIndyVar() + "\">");
      String[] listIndyVal = job.getIndyVal();
      String[] listIndyPlotVal = job.getIndyPlotVal();
      String[] listIndyLabel = job.getIndyLabel();
      for (int i = 0; i < listIndyVal.length; i++) {
        String strIndyPlotVal = (0 < listIndyPlotVal.length ? listIndyPlotVal[i] : "");
        strXML.append(
          "<val label=\"" + listIndyLabel[i].replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "\" plot_val=\"" + strIndyPlotVal.replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "\">" +
            listIndyVal[i].replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") +
            "</val>");
      }
      strXML.append("</indep>");
    }

    //  plot_fix
    MVOrderedMap mapPlotFix = job.getPlotFixVal();
    strXML.append("<plot_fix>");
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
      strXML.append("<field name=\"" + listFixField[i] + "\" equalize=\"" + String.valueOf(isEqualize) + "\" >");
      Object objFixVal = mapPlotFix.get(listFixField[i]);
      if (objFixVal instanceof String[]) {
        String[] listFixVal = (String[]) objFixVal;
        for (int j = 0; j < listFixVal.length; j++) {
          strXML.append("<val>" + listFixVal[j].replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "</val>");
        }
      } else if (objFixVal instanceof MVOrderedMap) {
        MVOrderedMap mapFixSet = (MVOrderedMap) objFixVal;
        String[] listFixSetKey = mapFixSet.getKeyList();
        for (int j = 0; j < listFixSetKey.length; j++) {
          String[] listFixSetVal = (String[]) mapFixSet.get(listFixSetKey[j]);
          strXML.append("<set name=\"" + listFixSetKey[j] + "\">");
          for (int k = 0; k < listFixSetVal.length; k++) {
            strXML.append("<val>" + listFixSetVal[k].replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "</val>");
          }
          strXML.append("</set>");
        }
      }
      strXML.append("</field>");
    }
    strXML.append("</plot_fix>");

    //  agg_stat
    if ((job.getAggCtc() || job.getAggSl1l2() || job.getAggSal1l2() || job.getAggPct() || job.getAggNbrCnt() || job.getAggSsvar() || job.getAggVl1l2() || job.getAggGrad()) || MVBatch.isModeRatioJob(job)) {
      strXML.append(
        "<agg_stat>" +
          "<agg_ctc>" + (job.getAggCtc() ? "TRUE" : "FALSE") + "</agg_ctc>" +
          "<agg_sl1l2>" + (job.getAggSl1l2() ? "TRUE" : "FALSE") + "</agg_sl1l2>" +
          "<agg_grad>" + (job.getAggGrad() ? "TRUE" : "FALSE") + "</agg_grad>" +
          "<agg_sal1l2>" + (job.getAggSal1l2() ? "TRUE" : "FALSE") + "</agg_sal1l2>" +
          "<agg_pct>" + (job.getAggPct() ? "TRUE" : "FALSE") + "</agg_pct>" +
          "<agg_nbrcnt>" + (job.getAggNbrCnt() ? "TRUE" : "FALSE") + "</agg_nbrcnt>" +
          "<agg_ssvar>" + (job.getAggSsvar() ? "TRUE" : "FALSE") + "</agg_ssvar>" +
          "<agg_vl1l2>" + (job.getAggVl1l2() ? "TRUE" : "FALSE") + "</agg_vl1l2>" +
          "<agg_val1l2>" + (job.getAggVal1l2() ? "TRUE" : "FALSE") + "</agg_val1l2>" +
          "<boot_repl>" + job.getAggBootRepl() + "</boot_repl>" +
          "<boot_random_seed>" + job.getAggBootRandomSeed() + "</boot_random_seed>" +
          "<cl_step>" + job.getCl_step() + "</cl_step>" +
          "<boot_ci>" + job.getAggBootCI() + "</boot_ci>" +
          "<eveq_dis>" + (job.getEveqDis() ? "TRUE" : "FALSE") + "</eveq_dis>" +
          "<cache_agg_stat>" + (job.getCacheAggStat() ? "TRUE" : "FALSE") + "</cache_agg_stat>" +
          "</agg_stat>");
    }

    //  calc_stat
    if (job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() || job.getCalcVl1l2() || job.getCalcGrad()) {
      strXML.append(
        "<calc_stat>" +
          "<calc_ctc>" + (job.getCalcCtc() ? "TRUE" : "FALSE") + "</calc_ctc>" +
          "<calc_sl1l2>" + (job.getCalcSl1l2() ? "TRUE" : "FALSE") + "</calc_sl1l2>" +
          "<calc_sal1l2>" + (job.getCalcSal1l2() ? "TRUE" : "FALSE") + "</calc_sal1l2>" +
          "<calc_vl1l2>" + (job.getCalcVl1l2() ? "TRUE" : "FALSE") + "</calc_vl1l2>" +
          "<calc_grad>" + (job.getCalcGrad() ? "TRUE" : "FALSE") + "</calc_grad>" +
          "</calc_stat>");
    }

    //  roc_calc
    if (job.getPlotTmpl().equals("roc.R_tmpl")) {
      strXML.append(
        "<roc_calc>" +
          "<roc_pct>" + (job.getRocPct() ? "TRUE" : "FALSE") + "</roc_pct>" +
          "<roc_ctc>" + (job.getRocCtc() ? "TRUE" : "FALSE") + "</roc_ctc>" +
          "</roc_calc>");

    }

    if (job.getPlotTmpl().equals("roc.R_tmpl") || job.getPlotTmpl().equals("rely.R_tmpl")) {
      strXML.append("<summary_curve>");
      for (String stat : job.getSummaryCurve()) {
        strXML.append("<val>" + stat + "</val>");
      }
      strXML.append("</summary_curve>");
    }

    //  roc_calc
    if (job.getPlotTmpl().equals("ens_ss.R_tmpl")) {
      strXML.append(
        "<ensss_pts>" + job.getEnsSsPts() + "</ensss_pts>" +
          "<ensss_pts_disp>" + job.getEnsSsPtsDisp() + "</ensss_pts_disp>");
    }

    //taylor
    if (job.getPlotTmpl().equals("taylor_plot.R_tmpl")) {
      strXML.append(
        "<taylor_voc>" + job.getTaylorVoc() + "</taylor_voc>" +
          "<taylor_show_gamma>" + job.getTaylorShowGamma() + "</taylor_show_gamma>");
    }

    //  tmpl

    strXML.append(
      "<tmpl>" +
        "<title>" + preserveBackslash(job.getTitleTmpl()) + "</title>" +
        "<x_label>" + preserveBackslash(job.getXLabelTmpl()) + "</x_label>" +
        "<y1_label>" + preserveBackslash(job.getY1LabelTmpl()) + "</y1_label>" +
        "<y2_label>" + preserveBackslash(job.getY2LabelTmpl()) + "</y2_label>" +
        "<caption>" + preserveBackslash(job.getCaptionTmpl()).replace("&", "&#38;").replace(">", "&gt;").replace("<", "&lt;") + "</caption>" +
        "<listDiffSeries1>" + job.getDiffSeries1() + "</listDiffSeries1>" +
        "<listDiffSeries2>" + job.getDiffSeries2() + "</listDiffSeries2>" +
        "</tmpl>");

    //  plot_cmd / plot_cond
    strXML.append(
      "<plot_cmd>" + job.getPlotCmd() + "</plot_cmd>" +
        "<plot_cond>" + job.getPlotCond() + "</plot_cond>");

    //  plot fmt
    strXML.append(
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
        "<rely_event_hist>" + job.getRelyEventHist() + "</rely_event_hist>" +
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
        "<legend>" + job.getLegend() + "</legend>" +

        "<y1_lim>" + job.getY1Lim() + "</y1_lim>" +
        "<y1_bufr>" + job.getY1Bufr() + "</y1_bufr>" +
        "<y2_lim>" + job.getY2Lim() + "</y2_lim>" +
        "<y2_bufr>" + job.getY2Bufr() + "</y2_bufr>" +
        "<varianceInflationFactor>" + job.getVarianceInflationFactor() + "</varianceInflationFactor>" +
        "<normalized_histogram>" + job.getNormalizedHistogram() + "</normalized_histogram>" +
        "<plot_stat>" + job.getPlotStat() + "</plot_stat>");

    //  close the plot job
    strXML.append("</plot></plot_spec>");
    return strXML;
  }

  public Document getDocument() {
    return _doc;
  }

  public MVPlotJob[] getJobsList() {
    return Arrays.copyOf(_listJobs, _listJobs.length);
  }

  public MVOrderedMap getJobsMap() {
    return _mapJobs;
  }

  public String getRtmplFolder() {
    return _strRtmplFolder;
  }

  public String getRworkFolder() {
    return _strRworkFolder;
  }

  public String getPlotsFolder() {
    return _strPlotsFolder;
  }

  public String getDataFolder() {
    return _strDataFolder;
  }

  public String getScriptsFolder() {
    return _strScriptsFolder;
  }

  protected void parsePlotJobSpec() throws Exception {
    ArrayList listJobs = new ArrayList();
    String strDBName = "";

    for (int i = 0; null != _nodePlotSpec && i < _nodePlotSpec._children.length; i++) {


      MVNode node = _nodePlotSpec._children[i];

      //  <connection>
      if (node._tag.equals("connection")) {
        for (int j = 0; j < node._children.length; j++) {
          if (node._children[j]._tag.equals("host")) {
            strDBHost = node._children[j]._value;
          } else if (node._children[j]._tag.equals("database")) {
            strDBName = node._children[j]._value;
          } else if (node._children[j]._tag.equals("user")) {
            strDBUser = node._children[j]._value;
          } else if (node._children[j]._tag.equals("password")) {
            strDBPassword = node._children[j]._value;

          } else if (node._children[j]._tag.equals("driver")) {
            strDBDriver = node._children[j]._value;
          }
        }

      }

      //  <rscript>
      else if (node._tag.equals("rscript")) {
        _strRscript = node._value;
      }

      //  <folders>
      else if (node._tag.equals("folders")) {
        for (int j = 0; j < node._children.length; j++) {
          if (node._children[j]._tag.equals("r_tmpl")) {
            _strRtmplFolder = node._children[j]._value;
          } else if (node._children[j]._tag.equals("r_work")) {
            _strRworkFolder = node._children[j]._value;
          } else if (node._children[j]._tag.equals("plots")) {
            _strPlotsFolder = node._children[j]._value;
          } else if (node._children[j]._tag.equals("data")) {
            _strDataFolder = node._children[j]._value;
          } else if (node._children[j]._tag.equals("scripts")) {
            _strScriptsFolder = node._children[j]._value;
          }
        }
      }

      //  <date_list>
      else if (node._tag.equals("date_list")) {

        _tableDateListDecl.put(node._name, buildDateList(node, System.out));
      }

      //  <date_range>
      else if (node._tag.equals("date_range")) {
        _tableDateRangeDecl.put(node._name, parseDateRange(node));
      }

      //  <date_range_list>
      else if (node._tag.equals("date_range_list")) {

        //  gather the elements of the range list
        String strRangeStart = "";
        String strRangeEnd = "";
        int intRangeLength = -1;
        int intInc = -1;
        SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strFormat = formatDB.toPattern();
        for (int l = 0; l < node._children.length; l++) {
          MVNode nodeChild = node._children[l];
          if (nodeChild._tag.equals("range_start")) {
            strRangeStart = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value);
          } else if (nodeChild._tag.equals("range_end")) {
            strRangeEnd = (0 < nodeChild._children.length ? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value);
          } else if (nodeChild._tag.equalsIgnoreCase("range_length")) {
            intRangeLength = Integer.parseInt(nodeChild._value);
          } else if (nodeChild._tag.equalsIgnoreCase("inc")) {
            intInc = Integer.parseInt(nodeChild._value);
          }
        }

        //  parse the begin and end times
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long intEndTime = -1;
        try {
          cal.setTime(formatDB.parse(strRangeStart));
          intEndTime = formatDB.parse(strRangeEnd).getTime();
        } catch (Exception e) {
        }

        //  build the list
        ArrayList listDateRange = new ArrayList();
        while (cal.getTime().getTime() <= intEndTime) {
          String strStartCur = formatDB.format(cal.getTime());
          cal.add(Calendar.MINUTE, intRangeLength);
          String strEndCur = formatDB.format(cal.getTime());
          listDateRange.add("BETWEEN '" + strStartCur + "' AND '" + strEndCur + "'");
          cal.add(Calendar.MINUTE, intInc - intRangeLength);
        }
        _tableDateRangeListDecl.put(node._name, toArray(listDateRange));
      }

      //  <plot>
      else if (node._tag.equals("plot")) {


        //  parse the plot and add it to the job table and, if appropriate, the list of runnable jobs
        _tablePlotNode.put(node._name, node);
        String strInherits = node._inherits.trim();
        MVPlotJob job;

        if ("".equals(strInherits)) {
          job = parsePlotJob(node, null);
        } else {
          String[] listInherits = strInherits.split("\\s*,\\s*");
          if (!_tablePlotDecl.containsKey(listInherits[0])) {
            throw new Exception("inherited plot job " + listInherits[0] + " not found");
          }
          MVPlotJob jobBase = (MVPlotJob) _tablePlotDecl.get(listInherits[0]);
          for (int j = 1; j < listInherits.length; j++) {
            if (!_tablePlotNode.containsKey(listInherits[j])) {
              throw new Exception("multiple inherited plot job " + listInherits[j] + " not found");
            }
            MVNode nodeInherit = (MVNode) _tablePlotNode.get(listInherits[j]);
            jobBase = parsePlotJob(nodeInherit, jobBase);
          }
          job = parsePlotJob(node, jobBase);
        }

        //  set the job database information
        job.setRscript(_strRscript);
        List<String> databases = new ArrayList<>();
        String[] databasesArray = strDBName.split(",");
        for (String db : databasesArray) {
          databases.add(db.trim());
        }

        job.setCurrentDBName(databases);

        //  check the job and add it to the jobs table and to the runnable jobs, if appropriate
        _tablePlotDecl.put(node._name, job);
        String strCompleteness = "";
        boolean boolPlotRun = !node._run.equalsIgnoreCase("false");

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
            mapFarPody.put(listFcstVar[0], new String[]{"FAR", "PODY"});
            mapDep.put("dep1", mapFarPody);
            mapDep.put("dep2", new MVOrderedMap());
            job.addDepGroup(mapDep);
          } else if (boolPlotRun) {
            strCompleteness = "if ens_ss template is selected, a FCST_VAR must be specified in plot_fix";
          }

        } else if (!job.getPlotTmpl().equals("rhist.R_tmpl") && !job.getPlotTmpl().equals("rely.R_tmpl") && !job.getPlotTmpl().equals("phist.R_tmpl") && !job.getPlotTmpl().equals("relp.R_tmpl") && !job.getPlotTmpl().equals("eclv.R_tmpl")) {
          strCompleteness = checkJobCompleteness(job);
        }
        if (strCompleteness.isEmpty()) {
          strCompleteness = checkJobFieldsOrder(job);
        }


        //  add runnable jobs to the run table if complete, complain otherwise
        if (strCompleteness.isEmpty()) {
          if (boolPlotRun) {
            _mapJobs.put(node._name, job);
          }
          listJobs.add(job);
        } else if (boolPlotRun) {
          throw new Exception("plot " + node._name + ": " + strCompleteness);
        }
      }

    }

    _listJobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[]{});
  }

  /**
   * Parse a single xml plot specification &lt;plot&gt; node from an xml plot specification and return the resulting MVPlotJob.  The inherited job is specified
   * by the input jobBase.
   *
   * @param nodePlot XML plot specification object to parse
   * @param jobBase  MVPlotJob whose characteristics to inherit
   * @return Populated MVPlot structure
   */
  public MVPlotJob parsePlotJob(MVNode nodePlot, MVPlotJob jobBase) throws Exception {
    MVPlotJob job = (null != jobBase ? jobBase.copy() : new MVPlotJob());

    for (int i = 0; i < nodePlot._children.length; i++) {
      MVNode node = nodePlot._children[i];

      //  <template>
      if (node._tag.equals("template")) {
        job.setPlotTmpl(node._value);
      }

      //  <indep>
      else if (node._tag.equals("indep")) {
        String equalize = node.getAttribute("equalize");
        if ("true".equalsIgnoreCase(equalize) || "false".equalsIgnoreCase(equalize)) {
          job.setEqualizeByIndep(Boolean.valueOf(equalize));
        }

        job.setIndyVar(node._name);
        if (!"".equals(node._depends)) {
          job.setIndyDep(new MVPlotDep(node._depends, node));
        } else {
          String[][] listIndy = parseIndyNode(node, "");
          job.setIndyVal(listIndy[0]);
          job.setIndyLabel(listIndy[1]);
          job.setIndyPlotVal(listIndy[2]);
        }
      }

      //  <plot_fix>
      else if (node._tag.equals("plot_fix")) {

        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeFix = node._children[j];
          String equalize = nodeFix.getAttribute("equalize");
          //  <remove> and <clear>
          if (nodeFix._tag.equals("remove")) {
            job.removePlotFixVal(nodeFix._name);
            continue;
          } else if (nodeFix._tag.equals("clear")) {
            job.clearPlotFixVal();
            continue;
          }

          //  <field>
          ArrayList listFixVal;
          if (job.getPlotFixVal().containsKey(nodeFix._name) &&
            job.getPlotFixVal().get(nodeFix._name) instanceof String[]) {
            listFixVal = (ArrayList) toArrayList((String[]) job.getPlotFixVal().get(nodeFix._name));
          } else {
            listFixVal = new ArrayList();
          }
          MVOrderedMap mapFixVal = new MVOrderedMap();
          MVOrderedMap mapTmplVal = new MVOrderedMap();
          for (int k = 0; k < nodeFix._children.length; k++) {
            MVNode nodeFixVal = nodeFix._children[k];

            //  <val>
            if (nodeFixVal._tag.equals("val")) {
              listFixVal.add(nodeFixVal._value);
            }

            //  <set>
            else if (nodeFixVal._tag.equals("set")) {
              ArrayList listFixSet = new ArrayList();
              for (int l = 0; l < nodeFixVal._children.length; l++) {
                MVNode nodeFixSet = nodeFixVal._children[l];

                //  <val>
                if (nodeFixSet._tag.equals("val")) {
                  listFixSet.add(nodeFixSet._value);
                }

                //  <date_list>
                else if (nodeFixSet._tag.equals("date_list")) {
                  if (_tableDateListDecl.get(nodeFixSet._name) instanceof List) {
                    listFixSet.addAll((List) _tableDateListDecl.get(nodeFixSet._name));
                  } else {
                    listFixSet.addAll(Arrays.asList((String[]) _tableDateListDecl.get(nodeFixSet._name)));
                  }
                  mapTmplVal.put(nodeFix._name, nodeFixSet._name);
                }

                //  <date_range>
                else if (nodeFixSet._tag.equals("date_range")) {
                  throw new Exception("sets of date_range structures not supported");
                }

                //  <date_range_list>
                else if (nodeFixSet._tag.equals("date_range_list")) {
                  throw new Exception("sets of date_range_list structures not supported");
                }
              }
              mapFixVal.put(nodeFixVal._name, toArray(listFixSet));
            }

            //  <date_list>
            else if (nodeFixVal._tag.equals("date_list")) {
              if (_tableDateListDecl.get(nodeFixVal._name) instanceof List) {
                listFixVal.addAll((List) _tableDateListDecl.get(nodeFixVal._name));
              } else {
                listFixVal.addAll(Arrays.asList((String[]) _tableDateListDecl.get(nodeFixVal._name)));
              }
            }

            //  <date_range>
            else if (nodeFixVal._tag.equals("date_range")) {
              if (!_tableDateRangeDecl.containsKey(nodeFixVal._name)) {
                throw new Exception("date_range " + nodeFixVal._name + " not found in plot_fix");
              }
              String strDateRangeVal = _tableDateRangeDecl.get(nodeFixVal._name).toString();
              listFixVal.add(strDateRangeVal);
              mapTmplVal.put(strDateRangeVal, nodeFixVal._name);
              if (!nodeFixVal._id.equals("")) {
                job.addTmplVal(nodeFixVal._id, nodeFixVal._name);
              }
            }

            //  <date_range_list>
            else if (nodeFixVal._tag.equals("date_range_list")) {
              String[] listDateRange = (String[]) _tableDateRangeListDecl.get(nodeFixVal._name);
              listFixVal.addAll(Arrays.asList(listDateRange));
            }

          }
          if (!listFixVal.isEmpty()) {
            job.addPlotFixVal(nodeFix._name, toArray(listFixVal));
            if ("true".equals(equalize)) {
              job.addPlotFixValEq(nodeFix._name, toArray(listFixVal));
            }
          } else if (0 < mapFixVal.size()) {
            job.addPlotFixVal(nodeFix._name, mapFixVal);
            if ("true".equals(equalize)) {
              job.addPlotFixValEq(nodeFix._name, mapFixVal);
            }
          }

          if (0 < mapTmplVal.size()) {
            job.addTmplMap(nodeFix._name, mapTmplVal);
          }
        }
      }

      //  <series1> or <series2>
      else if (node._tag.equals("series1") || node._tag.equals("series2")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeSeries = node._children[j];

          //  <remove>
          if (nodeSeries._tag.equals("remove")) {
            if (node._tag.equals("series1")) {
              job.removeSeries1Val(nodeSeries._name);
            } else if (node._tag.equals("series2")) {
              job.removeSeries1Val(nodeSeries._name);
            }
            continue;
          }

          //  <clear>
          else if (nodeSeries._tag.equals("clear")) {
            if (node._tag.equals("series1")) {
              job.clearSeries1Val();
            } else if (node._tag.equals("series2")) {
              job.clearSeries2Val();
            }
            continue;
          }

          //  <field>
          String[] listAggVal = new String[nodeSeries._children.length];
          for (int k = 0; k < nodeSeries._children.length; k++) {
            listAggVal[k] = nodeSeries._children[k]._value;
          }
          if (node._tag.equals("series1")) {
            if (jobBase != null) {
              job.clearSeries1Val();
            }
            job.addSeries1Val(nodeSeries._name, listAggVal);
          } else if (node._tag.equals("series2")) {
            if (jobBase != null) {
              job.clearSeries2Val();
            }
            job.addSeries2Val(nodeSeries._name, listAggVal);
          }
        }
      }

      //  <series_nobs>
      else if (node._tag.equals("series_nobs")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeSeriesNobs = node._children[j];

          //  <remove> and <clear>
          if (nodeSeriesNobs._tag.equals("remove")) {
            job.removeSeriesNobs(nodeSeriesNobs._name);
            continue;
          } else if (nodeSeriesNobs._tag.equals("clear")) {
            job.clearSeriesNobs();
            continue;
          }

          //  <field>
          String strField = nodeSeriesNobs._name;
          String strValue = nodeSeriesNobs._children[0]._value;
          job.addSeriesNobs(strField, strValue);
        }
      }

      //  <dep>
      else if (node._tag.equals("dep")) {

        //job.addDepGroup( buildDepMap(node) );

        //  <dep>
        MVOrderedMap mapDep = new MVOrderedMap();
        boolean boolDep1Present = false;
        boolean boolDep2Present = false;
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeDepN = node._children[j];

          //  <clear>
          if (nodeDepN._tag.equals("clear")) {
            job.clearDepGroups();
          }

          //  <mode_group>
          else if (nodeDepN._tag.equals("mode_group")) {
            throw new Exception("<mode_group> tag no longer supported, use multiple inheritance instead");
          }

          //  <dep1> or <dep2>
          else if (nodeDepN._tag.startsWith("dep")) {
            MVOrderedMap mapDepN = new MVOrderedMap();

            //  presence accounting
            if (nodeDepN._tag.equals("dep1")) {
              boolDep1Present = true;
            } else if (nodeDepN._tag.equals("dep2")) {
              boolDep2Present = true;
            }

            //  <fcst_var>
            for (int k = 0; k < nodeDepN._children.length; k++) {
              MVNode nodeFcstVar = nodeDepN._children[k];
              ArrayList listStats = new ArrayList();

              //  <stat>
              for (int l = 0; l < nodeFcstVar._children.length; l++) {
                String strStat = nodeFcstVar._children[l]._value;
                if (!isStatValid(strStat)) {
                  throw new Exception("unknown stat name " + strStat);
                }
                listStats.add(strStat);
              }
              mapDepN.put(nodeFcstVar._name, listStats.toArray(new String[]{}));
            }
            mapDep.put(nodeDepN._tag, mapDepN);
          }

          //  <fix>
          else if (nodeDepN._tag.startsWith("fix")) {
            throw new Exception("<dep> child <fix> no longer supported, use <plot_fix> instead");
          }
        }

        //  complain if a dep component is missing
        if (!boolDep1Present) {
          throw new Exception("plot job dep lacks dep1");
        }
        if (!boolDep2Present && !job.getPlotTmpl().contains("taylor")) {
          throw new Exception("plot job dep lacks dep2");
        }

        //  add the dep group to the job
        job.addDepGroup(mapDep);
      }

      //  <agg>
      else if (node._tag.equals("agg")) {
        throw new Exception("<agg> no longer supported, please change to <plot_fix>");
      }

      //  <taylor_voc>
      else if (node._tag.equals("taylor_voc")) {
        if ("true".equalsIgnoreCase(node._value) || "false".equalsIgnoreCase(node._value)) {
          job.setTaylorVoc(Boolean.valueOf(node._value));
        }
      }
      //  <taylor_show_gamma>
      else if (node._tag.equals("taylor_show_gamma")) {
        if ("true".equalsIgnoreCase(node._value) || "false".equalsIgnoreCase(node._value)) {
          job.setTaylorShowGamma(Boolean.valueOf(node._value));
        }
      }

      //  <tmpl>
      else if (node._tag.equals("tmpl")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeTmpl = node._children[j];

          //  <val_map>
          if (nodeTmpl._tag.equals("val_map")) {
            MVOrderedMap mapValMap = new MVOrderedMap();
            for (int k = 0; k < nodeTmpl._children.length; k++) {
              MVNode nodeKey = nodeTmpl._children[k]._children[0];
              String strKey;
              if (0 < nodeKey._children.length && nodeKey._children[0]._tag.equals("date_range")) {
                strKey = _tableDateRangeDecl.get(nodeKey._children[0]._name).toString();
              } else {
                strKey = nodeKey._value;
              }

              MVNode nodeVal = nodeTmpl._children[k]._children[1];
              mapValMap.put(strKey, nodeVal._value);
            }
            job.addTmplMap(nodeTmpl._name, mapValMap);
          }

          //  templates
          else if (nodeTmpl._tag.equals("data_file")) {
            job.setDataFileTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("plot_file")) {
            job.setPlotFileTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("r_file")) {
            job.setRFileTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("title")) {
            job.setTitleTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("x_label")) {
            job.setXLabelTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("y1_label")) {
            job.setY1LabelTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("y2_label")) {
            job.setY2LabelTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equals("caption")) {
            job.setCaptionTmpl(nodeTmpl._value);
          } else if (nodeTmpl._tag.equalsIgnoreCase("listDiffSeries1")) {
            //validate listDiffSeries - make sure that differences will be calculated for the same Forecast Variable and Statistic
            validateListDiffSeries(node, nodeTmpl);
            job.setDiffSeries1(nodeTmpl._value);
          } else if (nodeTmpl._tag.equalsIgnoreCase("listDiffSeries2")) {
            validateListDiffSeries(node, nodeTmpl);
            job.setDiffSeries2(nodeTmpl._value);
          }

        }
      }

      //  <dep1_scale> <dep2_scale>
      else if (node._tag.equals("dep1_scale") || node._tag.equals("dep2_scale")) {

        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeDepScale = node._children[j];

          //  <remove>
          if (nodeDepScale._tag.equals("remove")) {
            if (node._tag.equals("dep1_scale")) {
              job.removeDep1Scale(nodeDepScale._name);
            } else if (node._tag.equals("dep2_scale")) {
              job.removeDep2Scale(nodeDepScale._name);
            }
          }

          //  <clear>
          else if (nodeDepScale._tag.equals("clear")) {
            if (node._tag.equals("dep1_scale")) {
              job.clearDep1Scale();
            } else if (node._tag.equals("dep2_scale")) {
              job.clearDep2Scale();
            }
          }

          //  <field>
          else if (node._tag.equals("dep1_scale")) {
            job.addDep1Scale(nodeDepScale._name, nodeDepScale._value);
          } else if (node._tag.equals("dep2_scale")) {
            job.addDep2Scale(nodeDepScale._name, nodeDepScale._value);
          }
        }

      }

      //  <bootstrapping>
      else if (node._tag.equals("bootstrapping")) {
        throw new Exception("<bootstrapping> tag no longer supported, use <agg_stat> instead");
      }

      //  <agg_stat>
      else if (node._tag.equals("agg_stat")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeAggStat = node._children[j];
          boolean val = nodeAggStat._value.equalsIgnoreCase("true");
          if (nodeAggStat._tag.equals("agg_ctc")) {
            job.setAggCtc(val);
          } else if (nodeAggStat._tag.equals("agg_sl1l2")) {
            job.setAggSl1l2(val);
          } else if (nodeAggStat._tag.equals("agg_grad")) {
            job.setAggGrad(val);
          } else if (nodeAggStat._tag.equals("agg_sal1l2")) {
            job.setAggSal1l2(val);
          } else if (nodeAggStat._tag.equals("agg_pct")) {
            job.setAggPct(val);
          } else if (nodeAggStat._tag.equals("agg_nbrcnt")) {
            job.setAggNbrCnt(val);
          } else if (nodeAggStat._tag.equals("agg_ssvar")) {
            job.setAggSsvar(val);
          } else if (nodeAggStat._tag.equals("agg_vl1l2")) {
            job.setAggVl1l2(val);
          } else if (nodeAggStat._tag.equals("agg_val1l2")) {
            job.setAggVal1l2(val);
          } else if (nodeAggStat._tag.equals("boot_repl")) {
            job.setAggBootRepl(nodeAggStat._value);
          } else if (nodeAggStat._tag.equals("boot_random_seed")) {
            job.setAggBootRandomSeed(nodeAggStat._value);
          } else if (nodeAggStat._tag.equals("cl_step")) {
            job.setCl_step(nodeAggStat._value);
          } else if (nodeAggStat._tag.equals("boot_ci")) {
            job.setAggBootCI(nodeAggStat._value);
          } else if (nodeAggStat._tag.equals("eveq_dis")) {
            job.setEveqDis(val);
          } else if (nodeAggStat._tag.equals("cache_agg_stat")) {
            job.setCacheAggStat(val);
          }
        }

      }

      //  <calc_stat>
      else if (node._tag.equals("calc_stat")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeCalcStat = node._children[j];
          boolean val = nodeCalcStat._value.equalsIgnoreCase("true");
          if (nodeCalcStat._tag.equals("calc_ctc")) {
            job.setCalcCtc(val);
          } else if (nodeCalcStat._tag.equals("calc_sl1l2")) {
            job.setCalcSl1l2(val);
          } else if (nodeCalcStat._tag.equals("calc_grad")) {
            job.setCalcGrad(val);
          } else if (nodeCalcStat._tag.equals("calc_sal1l2")) {
            job.setCalcSal1l2(val);
          } else if (nodeCalcStat._tag.equals("calc_vl1l2")) {
            job.setCalcVl1l2(val);
          }
        }

        if (job.getCalcCtc() && job.getCalcSl1l2() && job.getCalcSal1l2() && job.getCalcVl1l2() && job.getCalcGrad()) {
          throw new Exception("invalid calc_stat setting - both calc_ctc and calc_sl1l2 and calc_sal1l2 and calc_vl1l2 and calc_grad are true");
        }
      }

      //  <roc_calc>
      else if (node._tag.equals("roc_calc")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeRocStat = node._children[j];
          if (nodeRocStat._tag.equals("roc_pct")) {
            job.setRocPct(nodeRocStat._value.equalsIgnoreCase("true"));
          } else if (nodeRocStat._tag.equals("roc_ctc")) {
            job.setRocCtc(nodeRocStat._value.equalsIgnoreCase("true"));
          }
        }

        if (!job.getRocPct() && !job.getRocCtc()) {
          throw new Exception("invalid roc_calc setting - neither roc_pct nor roc_ctc are true");
        }
        if (job.getRocPct() && job.getRocCtc()) {
          throw new Exception("invalid roc_calc setting - both roc_pct and roc_ctc are true");
        }
      } else if (node._tag.equals("summary_curve")) {
        for (int j = 0; j < node._children.length; j++) {
          MVNode nodeVal = node._children[j];
          if (nodeVal._tag.equals("val")) {
            job.addSummaryCurve(nodeVal._value);
          }
        }
      } else if (node._tag.equals("add_point_thresholds")) {
        job.setAddPointThresholds(node._value.equalsIgnoreCase("true"));
      }
      //  <normalized_histogram>
      else if (node._tag.equals("normalized_histogram")) {
        job.setNormalizedHistogram(node._value.equalsIgnoreCase("true"));

      }


      //  boolean format settings
      else if (_tableFormatBoolean.containsKey(node._tag)) {
        Method m = (Method) _tableFormatBoolean.get(node._tag);
        try {
          m.invoke(job, node._value.equals("true"));
        } catch (Exception e) {
          System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format boolean '" + node._tag + "': " + e.getMessage());
        }
      }

      //  R string format settings
      else if (_tableFormatString.containsKey(node._tag)) {
        if (node._tag.equals("order_series")) {
          //validate order_series
          String strStat = node._value;
          if (!isOrderValid(strStat)) {
            throw new Exception("Series order is invalid " + strStat);
          }
        }
        Method m = (Method) _tableFormatString.get(node._tag);
        try {
          m.invoke(job, node._value);
        } catch (Exception e) {
          System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format string '" + node._tag + "': " + e.getMessage());
        }

      }

      //  report unused tags
      else {
        System.out.println("  **  WARNING: unused plot tag '" + node._tag + "'");
      }
    }

    return job;
  }

  private void validateListDiffSeries(MVNode node, MVNode nodeTmpl) throws Exception {
    String[] diffSeries = nodeTmpl._value.split("c\\(");
    for (int k = 1; k < diffSeries.length; k++) {
      String diffSeriesArray[] = diffSeries[k].replace("\"", "").replace(")", "").split(",");
      String diffSeriesParametersArray[] = diffSeriesArray[0].split(" ");

      if (diffSeriesParametersArray.length > 2) {
        String variableStat = diffSeriesParametersArray[diffSeriesParametersArray.length - 2] + " " + diffSeriesParametersArray[diffSeriesParametersArray.length - 1];
        if (diffSeriesArray.length > 2 && diffSeriesArray[2].equals("DIFF") && !diffSeriesArray[1].endsWith(variableStat)) {
          throw new Exception("Difference curve " + diffSeries[k] + " configured to be calculated using different variable and/or statistic. It isn't supported by Image Viewer.");
        }
      }
    }
    //validate listDiffSeries - make sure that MODE Attribute stats are not in the list
    for (String stat : modeSingleStatField) {
      if (node._value.indexOf(stat) > 0) {
        throw new Exception("MODE Attribute stats " + stat + " can't be a part of difference curve.");
      }
    }
    for (String stat : modePairStatField.keySet()) {
      if (node._value.indexOf(stat) > 0) {
        throw new Exception("MODE Attribute stats " + stat + " can't be a part of difference curve.");
      }
    }
  }

  /**
   * Determine if the input statistic name is valid by searching the tables of supported statistics.
   *
   * @param strStat name of statistic to test for validity
   * @return true if valid, false otherwise
   */
  public boolean isStatValid(String strStat) {
    return !getStatTable(strStat).equals("");
  }

  /**
   * Determine if the input order name is valid by validating the order of integers
   *
   * @param strStat order of series
   * @return true if valid, false otherwise
   */
  public boolean isOrderValid(String strStat) {
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

  private static String preserveBackslash(String str) {
    String result = str;
    if (str.contains("\\")) {
      result = str.replaceAll("\\\\", "\\\\\\\\");
    }
    return result;
  }

  public DatabaseInfo getDatabaseInfo() {
    DatabaseInfo databaseInfo = null;
    if (strDBHost != null && strDBUser != null && strDBPassword != null) {
      databaseInfo = new DatabaseInfo(strDBHost, strDBUser, strDBPassword);
    }
    return databaseInfo;
  }

}

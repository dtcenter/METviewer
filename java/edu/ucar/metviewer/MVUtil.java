package edu.ucar.metviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.w3c.dom.Document;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MVUtil {

  private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger("MVUtil");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");


  public static final Pattern thresh = Pattern.compile("([<>=!]{1,2})(\\d*(?:\\.\\d+)?)");
  public static final Pattern lev = Pattern.compile("(\\w)(\\d+)(?:-(\\d+))?");
  public static final Pattern interpPnts = Pattern.compile("\\d+?");
  public static final Pattern modeSingle = Pattern.compile("\\s+h\\.([^,]+),");
  public static final Pattern prob = Pattern.compile("PROB\\(([\\w\\d]+)([<>=]+)([^\\)]+)\\)");
  private static final Pattern plotTmpl = Pattern.compile("\\{((\\w+)(?:\\?[^}]*)?)\\}");
  private static final Pattern tag = Pattern.compile("([\\w\\d]+)(?:\\s*\\?(.*))?");


  public static final Pattern patModeSingleObjectId = Pattern.compile("^(C?[FO]\\d{3})$");
  public static final Pattern patModePairObjectId = Pattern.compile("^(C?F\\d{3})_(C?O\\d{3})$");

  public static final DateTimeFormatter APP_DATE_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public final static String LINE_SEPARATOR = System.getProperty("line.separator");
  /**
   * Parse the input mode statistic, which is assume to have the form SSSS_FFF, where SSSS is the
   * name of a mode statistic with arbitrary lenght and FFF is a three character flag indicator
   * string.
   */
  public static final Pattern _patModeStat = Pattern.compile("([^_]+)(?:_\\w{3})?_(\\w{2,3})");
  public static final String CTC = "ctc"; //Contingency Table Statistics
  public static final String NBRCTC = "nbrctc"; //Contingency Table Statistics
  public static final String SL1L2 = "sl1l2"; //Scalar partial sums
  public static final String GRAD = "grad"; //Gradient partial sums
  public static final String SAL1L2 = "sal1l2"; //  Scalar anomaly  partial sums
  public static final String SSVAR = "ssvar"; //  Spread/Skill Variance
  public static final String PCT = "pct"; //Contingency Table Counts for Probabilistic forecasts
  public static final String NBRCNT = "nbrcnt"; //  Neighborhood Continuous Statistics
  public static final String VL1L2 = "vl1l2"; // Vector Partial Sum
  public static final String VAL1L2 = "val1l2"; // Vector Anomaly Partial Sum
  public static final String ECNT = "ecnt"; // Ensemble Continuous Statistics
  public static final String RPS = "rps"; // Ranked Probability Score Statistics
  public static final Map<String, String[]> statsEnscnt = new HashMap<>();
  public static final Map<String, String[]> statsMpr = new HashMap<>();
  public static final Map<String, String[]> statsDmap = new HashMap<>();
  public static final Map<String, String[]> statsOrank = new HashMap<>();
  public static final Map<String, String[]> statsCnt = new HashMap<>();
  public static final Map<String, String[]> statsVcnt = new HashMap<>();
  public static final Map<String, String[]> statsEcnt = new HashMap<>();
  public static final Map<String, String[]> statsSsvar = new HashMap<>();
  public static final Map<String, String[]> statsCts = new HashMap<>();
  public static final Map<String, String[]> statsNbrcts = new HashMap<>();
  public static final Map<String, String[]> statsNbrcnt = new HashMap<>();
  public static final Map<String, String[]> statsPstd = new HashMap<>();
  public static final Map<String, String[]> statsMcts = new HashMap<>();
  public static final Map<String, String[]> statsPhist = new HashMap<>();
  public static final Map<String, String[]> statsRhist = new HashMap<>();
  public static final Map<String, String[]> statsVl1l2 = new HashMap<>();
  public static final Map<String, String[]> statsVal1l2 = new HashMap<>();
  public static final Map<String, String[]> statsPerc = new HashMap<>();
  public static final Map<String, String> modeSingleStatField = new HashMap<>();
  public static final Map<String, String> mtd3dSingleStatField = new HashMap<>();
  public static final Map<String, String> mtd2dStatField = new HashMap<>();
  public static final Map<String, String[]> statsRps = new HashMap<>();
  public static final Map<String, String[]> statsCtc = new HashMap<>();
  public static final Map<String, String[]> statsNbrctc = new HashMap<>();
  public static final Map<String, String[]> statsPct = new HashMap<>();
  public static final Map<String, String[]> statsSl1l2 = new HashMap<>();
  public static final Map<String, String[]> statsSal1l2 = new HashMap<>();
  public static final Map<String, String[]> statsGrad = new HashMap<>();


  public static final int MAX_STR_LEN = 500;


  public static final List<String> modeRatioField = new ArrayList<>();
  public static final List<String> mtdRatioField = new ArrayList<>();


  public static final Map<String, String> modePairStatField = new HashMap<>();
  public static final Map<String, String> mtd3dPairStatField = new HashMap<>();
  public static final List<String> calcStatCTC = new ArrayList<>();
  public static final String DB_DATE_MS = "yyyy-MM-dd HH:mm:ss.S";
  public static final SimpleDateFormat PLOT_FORMAT = new SimpleDateFormat("yyyyMMddHH");


  public static final Pattern patRTmpl = Pattern.compile("#<(\\w+)>#");

  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");
  public static final String PYTHON = "Python";
  public static final String RSCRIPT = "Rscript";

  /*
   * variable length group data indices for lines with an arbitrary number of fields
   *   - index of field containing number of sets
   *   - index of first repeating field(s)
   *   - number of fields in each repeating set
   */
  public static final Map<String, int[]> lengthGroupIndices = new HashMap<>();
  public static final Map<String, Boolean> alphaLineTypes = new HashMap<>();

  public static final Map<String, Boolean> covThreshLineTypes = new HashMap<>();

  public static final PrintStream errorStream = IoBuilder.forLogger(MVUtil.class)
          .setLevel(org.apache.logging.log4j.Level.INFO)
          .setMarker(
                  new MarkerManager.Log4jMarker("ERROR"))
          .buildPrintStream();
  public static final String DEFAULT_DATABASE_GROUP = "NO GROUP";

  public static final String SEPARATOR = ",";


  public static final String[] lineTypes = new String[]{
          "fho",
          "ctc",
          "cts",
          "mctc",
          "mcts",
          "cnt",
          "sl1l2",
          "sal1l2",
          "vl1l2",
          "val1l2",
          "pct",
          "pstd",
          "pjc",
          "prc",
          "eclv",
          "mpr",
          "nbrctc",
          "nbrcts",
          "nbrcnt",
          "isc",
          "rhist",
          "phist",
          "orank",
          "ssvar",
          "grad",
          "vcnt",
          "relp",
          "ecnt",
          "dmap",
          "rps"
  };

  static {

    modeRatioField.add("RATIO_FSA_ASA");
    modeRatioField.add("RATIO_OSA_ASA");
    modeRatioField.add("RATIO_ASM_ASA");
    modeRatioField.add("RATIO_ASU_ASA");
    modeRatioField.add("RATIO_FSM_FSA");
    modeRatioField.add("RATIO_FSU_FSA");
    modeRatioField.add("RATIO_OSM_OSA");
    modeRatioField.add("RATIO_OSU_OSA");
    modeRatioField.add("RATIO_FSM_ASM");
    modeRatioField.add("RATIO_OSM_ASM");
    modeRatioField.add("RATIO_FSU_ASU");
    modeRatioField.add("RATIO_OSU_ASU");
    modeRatioField.add("RATIO_FSA_AAA");
    modeRatioField.add("RATIO_OSA_AAA");
    modeRatioField.add("RATIO_FSA_FAA");
    modeRatioField.add("RATIO_FCA_FAA");
    modeRatioField.add("RATIO_OSA_OAA");
    modeRatioField.add("RATIO_OCA_OAA");
    modeRatioField.add("RATIO_FCA_ACA");
    modeRatioField.add("RATIO_OCA_ACA");
    modeRatioField.add("RATIO_FSA_OSA");
    modeRatioField.add("RATIO_OSA_FSA");
    modeRatioField.add("RATIO_ACA_ASA");
    modeRatioField.add("RATIO_ASA_ACA");
    modeRatioField.add("RATIO_FCA_FSA");
    modeRatioField.add("RATIO_FSA_FCA");
    modeRatioField.add("RATIO_OCA_OSA");
    modeRatioField.add("RATIO_OSA_OCA");

    modeRatioField.add("OBJHITS");
    modeRatioField.add("OBJMISSES");
    modeRatioField.add("OBJFAS");
    modeRatioField.add("OBJCSI");
    modeRatioField.add("OBJPODY");
    modeRatioField.add("OBJFAR");

    modeRatioField.add("AREARAT_FSA_ASA");
    modeRatioField.add("AREARAT_OSA_ASA");
    modeRatioField.add("AREARAT_ASM_ASA");
    modeRatioField.add("AREARAT_ASU_ASA");
    modeRatioField.add("AREARAT_FSM_FSA");
    modeRatioField.add("AREARAT_FSU_FSA");
    modeRatioField.add("AREARAT_OSM_OSA");
    modeRatioField.add("AREARAT_OSU_OSA");
    modeRatioField.add("AREARAT_FSM_ASM");
    modeRatioField.add("AREARAT_OSM_ASM");
    modeRatioField.add("AREARAT_FSU_ASU");
    modeRatioField.add("AREARAT_OSU_ASU");
    modeRatioField.add("AREARAT_FSA_AAA");
    modeRatioField.add("AREARAT_OSA_AAA");
    modeRatioField.add("AREARAT_FSA_FAA");
    modeRatioField.add("AREARAT_FCA_FAA");
    modeRatioField.add("AREARAT_OSA_OAA");
    modeRatioField.add("AREARAT_OCA_OAA");
    modeRatioField.add("AREARAT_FCA_ACA");
    modeRatioField.add("AREARAT_OCA_ACA");
    modeRatioField.add("AREARAT_FSA_OSA");
    modeRatioField.add("AREARAT_OSA_FSA");
    modeRatioField.add("AREARAT_ACA_ASA");
    modeRatioField.add("AREARAT_ASA_ACA");
    modeRatioField.add("AREARAT_FCA_FSA");
    modeRatioField.add("AREARAT_FSA_FCA");
    modeRatioField.add("AREARAT_OCA_OSA");
    modeRatioField.add("AREARAT_OSA_OCA");

    modeRatioField.add("OBJAHITS");
    modeRatioField.add("OBJAMISSES");
    modeRatioField.add("OBJAFAS");
    modeRatioField.add("OBJACSI");
    modeRatioField.add("OBJAPODY");
    modeRatioField.add("OBJAFAR");
  }


  static {

    mtdRatioField.add("2d_RATIO_FSA_ASA");
    mtdRatioField.add("2d_RATIO_OSA_ASA");
    mtdRatioField.add("2d_RATIO_ASM_ASA");
    mtdRatioField.add("2d_RATIO_ASU_ASA");
    mtdRatioField.add("2d_RATIO_FSM_FSA");
    mtdRatioField.add("2d_RATIO_FSU_FSA");
    mtdRatioField.add("2d_RATIO_OSM_OSA");
    mtdRatioField.add("2d_RATIO_OSU_OSA");
    mtdRatioField.add("2d_RATIO_FSM_ASM");
    mtdRatioField.add("2d_RATIO_OSM_ASM");
    mtdRatioField.add("2d_RATIO_FSU_ASU");
    mtdRatioField.add("2d_RATIO_OSU_ASU");
    mtdRatioField.add("2d_RATIO_FSA_AAA");
    mtdRatioField.add("2d_RATIO_OSA_AAA");
    mtdRatioField.add("2d_RATIO_FSA_FAA");
    mtdRatioField.add("2d_RATIO_FCA_FAA");
    mtdRatioField.add("2d_RATIO_OSA_OAA");
    mtdRatioField.add("2d_RATIO_OCA_OAA");
    mtdRatioField.add("2d_RATIO_FCA_ACA");
    mtdRatioField.add("2d_RATIO_OCA_ACA");
    mtdRatioField.add("2d_RATIO_FSA_OSA");
    mtdRatioField.add("2d_RATIO_OSA_FSA");
    mtdRatioField.add("2d_RATIO_ACA_ASA");
    mtdRatioField.add("2d_RATIO_ASA_ACA");
    mtdRatioField.add("2d_RATIO_FCA_FSA");
    mtdRatioField.add("2d_RATIO_FSA_FCA");
    mtdRatioField.add("2d_RATIO_OCA_OSA");
    mtdRatioField.add("2d_RATIO_OSA_OCA");


    mtdRatioField.add("3d_RATIO_FSA_ASA");
    mtdRatioField.add("3d_RATIO_OSA_ASA");
    mtdRatioField.add("3d_RATIO_ASM_ASA");
    mtdRatioField.add("3d_RATIO_ASU_ASA");
    mtdRatioField.add("3d_RATIO_FSM_FSA");
    mtdRatioField.add("3d_RATIO_FSU_FSA");
    mtdRatioField.add("3d_RATIO_OSM_OSA");
    mtdRatioField.add("3d_RATIO_OSU_OSA");
    mtdRatioField.add("3d_RATIO_FSM_ASM");
    mtdRatioField.add("3d_RATIO_OSM_ASM");
    mtdRatioField.add("3d_RATIO_FSU_ASU");
    mtdRatioField.add("3d_RATIO_OSU_ASU");
    mtdRatioField.add("3d_RATIO_FSA_AAA");
    mtdRatioField.add("3d_RATIO_OSA_AAA");
    mtdRatioField.add("3d_RATIO_FSA_FAA");
    mtdRatioField.add("3d_RATIO_FCA_FAA");
    mtdRatioField.add("3d_RATIO_OSA_OAA");
    mtdRatioField.add("3d_RATIO_OCA_OAA");
    mtdRatioField.add("3d_RATIO_FCA_ACA");
    mtdRatioField.add("3d_RATIO_OCA_ACA");
    mtdRatioField.add("3d_RATIO_FSA_OSA");
    mtdRatioField.add("3d_RATIO_OSA_FSA");
    mtdRatioField.add("3d_RATIO_ACA_ASA");
    mtdRatioField.add("3d_RATIO_ASA_ACA");
    mtdRatioField.add("3d_RATIO_FCA_FSA");
    mtdRatioField.add("3d_RATIO_FSA_FCA");
    mtdRatioField.add("3d_RATIO_OCA_OSA");
    mtdRatioField.add("3d_RATIO_OSA_OCA");

    mtdRatioField.add("2d_OBJHITS");
    mtdRatioField.add("2d_OBJMISSES");
    mtdRatioField.add("2d_OBJFAS");
    mtdRatioField.add("2d_OBJCSI");
    mtdRatioField.add("2d_OBJPODY");
    mtdRatioField.add("2d_OBJFAR");
    mtdRatioField.add("3d_OBJHITS");
    mtdRatioField.add("3d_OBJMISSES");
    mtdRatioField.add("3d_OBJFAS");
    mtdRatioField.add("3d_OBJCSI");
    mtdRatioField.add("3d_OBJPODY");
    mtdRatioField.add("3d_OBJFAR");

    mtdRatioField.add("3d_VOLRAT_FSA_ASA");
    mtdRatioField.add("3d_VOLRAT_OSA_ASA");
    mtdRatioField.add("3d_VOLRAT_ASM_ASA");
    mtdRatioField.add("3d_VOLRAT_ASU_ASA");
    mtdRatioField.add("3d_VOLRAT_FSM_FSA");
    mtdRatioField.add("3d_VOLRAT_FSU_FSA");
    mtdRatioField.add("3d_VOLRAT_OSM_OSA");
    mtdRatioField.add("3d_VOLRAT_OSU_OSA");
    mtdRatioField.add("3d_VOLRAT_FSM_ASM");
    mtdRatioField.add("3d_VOLRAT_OSM_ASM");
    mtdRatioField.add("3d_VOLRAT_FSU_ASU");
    mtdRatioField.add("3d_VOLRAT_OSU_ASU");
    mtdRatioField.add("3d_VOLRAT_FSA_AAA");
    mtdRatioField.add("3d_VOLRAT_OSA_AAA");
    mtdRatioField.add("3d_VOLRAT_FSA_FAA");
    mtdRatioField.add("3d_VOLRAT_FCA_FAA");
    mtdRatioField.add("3d_VOLRAT_OSA_OAA");
    mtdRatioField.add("3d_VOLRAT_OCA_OAA");
    mtdRatioField.add("3d_VOLRAT_FCA_ACA");
    mtdRatioField.add("3d_VOLRAT_OCA_ACA");
    mtdRatioField.add("3d_VOLRAT_FSA_OSA");
    mtdRatioField.add("3d_VOLRAT_OSA_FSA");
    mtdRatioField.add("3d_VOLRAT_ACA_ASA");
    mtdRatioField.add("3d_VOLRAT_ASA_ACA");
    mtdRatioField.add("3d_VOLRAT_FCA_FSA");
    mtdRatioField.add("3d_VOLRAT_FSA_FCA");
    mtdRatioField.add("3d_VOLRAT_OCA_OSA");
    mtdRatioField.add("3d_VOLRAT_OSA_OCA");

    mtdRatioField.add("2d_AREARAT_FSA_ASA");
    mtdRatioField.add("2d_AREARAT_OSA_ASA");
    mtdRatioField.add("2d_AREARAT_ASM_ASA");
    mtdRatioField.add("2d_AREARAT_ASU_ASA");
    mtdRatioField.add("2d_AREARAT_FSM_FSA");
    mtdRatioField.add("2d_AREARAT_FSU_FSA");
    mtdRatioField.add("2d_AREARAT_OSM_OSA");
    mtdRatioField.add("2d_AREARAT_OSU_OSA");
    mtdRatioField.add("2d_AREARAT_FSM_ASM");
    mtdRatioField.add("2d_AREARAT_OSM_ASM");
    mtdRatioField.add("2d_AREARAT_FSU_ASU");
    mtdRatioField.add("2d_AREARAT_OSU_ASU");
    mtdRatioField.add("2d_AREARAT_FSA_AAA");
    mtdRatioField.add("2d_AREARAT_OSA_AAA");
    mtdRatioField.add("2d_AREARAT_FSA_FAA");
    mtdRatioField.add("2d_AREARAT_FCA_FAA");
    mtdRatioField.add("2d_AREARAT_OSA_OAA");
    mtdRatioField.add("2d_AREARAT_OCA_OAA");
    mtdRatioField.add("2d_AREARAT_FCA_ACA");
    mtdRatioField.add("2d_AREARAT_OCA_ACA");
    mtdRatioField.add("2d_AREARAT_FSA_OSA");
    mtdRatioField.add("2d_AREARAT_OSA_FSA");
    mtdRatioField.add("2d_AREARAT_ACA_ASA");
    mtdRatioField.add("2d_AREARAT_ASA_ACA");
    mtdRatioField.add("2d_AREARAT_FCA_FSA");
    mtdRatioField.add("2d_AREARAT_FSA_FCA");
    mtdRatioField.add("2d_AREARAT_OCA_OSA");
    mtdRatioField.add("2d_AREARAT_OSA_OCA");

    mtdRatioField.add("3d_OBJVHITS");
    mtdRatioField.add("3d_OBJVMISSES");
    mtdRatioField.add("3d_OBJVFAS");
    mtdRatioField.add("3d_OBJVCSI");
    mtdRatioField.add("3d_OBJVPODY");
    mtdRatioField.add("3d_OBJVFAR");
    mtdRatioField.add("3d_OBJVHITS");
    mtdRatioField.add("2d_OBJAMISSES");
    mtdRatioField.add("2d_OBJAFAS");
    mtdRatioField.add("2d_OBJACSI");
    mtdRatioField.add("2d_OBJAPODY");
    mtdRatioField.add("2d_OBJAFAR");
  }


  static {
    covThreshLineTypes.put("NBRCTC", Boolean.TRUE);
    covThreshLineTypes.put("NBRCTS", Boolean.TRUE);
    covThreshLineTypes.put("PCT", Boolean.TRUE);
    covThreshLineTypes.put("PSTD", Boolean.TRUE);
    covThreshLineTypes.put("PJC", Boolean.TRUE);
    covThreshLineTypes.put("PRC", Boolean.TRUE);
  }

  static {
    alphaLineTypes.put("CTS", Boolean.TRUE);
    alphaLineTypes.put("NBRCTS", Boolean.TRUE);
    alphaLineTypes.put("NBRCNT", Boolean.TRUE);
    alphaLineTypes.put("MCTS", Boolean.TRUE);
    alphaLineTypes.put("SSVAR", Boolean.TRUE);
    alphaLineTypes.put("VCNT", Boolean.TRUE);
    alphaLineTypes.put("DMAP", Boolean.TRUE);
    alphaLineTypes.put("RPS", Boolean.TRUE);

    alphaLineTypes.put("CNT", Boolean.FALSE);
    alphaLineTypes.put("PSTD", Boolean.FALSE);
  }


  static {
    lengthGroupIndices.put("PCT", new int[]{23, 24, 3});
    lengthGroupIndices.put("PSTD", new int[]{23, 33, 1});
    lengthGroupIndices.put("PJC", new int[]{23, 24, 7});
    lengthGroupIndices.put("PRC", new int[]{23, 24, 3});
    lengthGroupIndices.put("MCTC", new int[]{23, 24, 1});
    lengthGroupIndices.put("RHIST", new int[]{23, 23, 1});
    lengthGroupIndices.put("PHIST", new int[]{24, 25, 1});
    lengthGroupIndices.put("RELP", new int[]{23, 24, 1});
    lengthGroupIndices.put("ORANK", new int[]{33, 34, 1});
    lengthGroupIndices.put("ECLV", new int[]{25, 26, 2});
  }

  static {
    statsEnscnt.put("ENS_RPSF", new String[]{});
    statsEnscnt.put("ENS_RPSCL", new String[]{});
    statsEnscnt.put("ENS_RPSS", new String[]{});
    statsEnscnt.put("ENS_CRPSF", new String[]{});
    statsEnscnt.put("ENS_CRPSCL", new String[]{});
    statsEnscnt.put("ENS_CRPSS", new String[]{});

  }

  static {
    statsMpr.put("MPR_FCST", new String[]{""});
    statsMpr.put("MPR_OBS", new String[]{""});
    statsMpr.put("MPR_CLIMO", new String[]{""});
  }

  static {
    statsDmap.put("DMAP_FBIAS", new String[]{""});
    statsDmap.put("DMAP_BADDELEY", new String[]{""});
    statsDmap.put("DMAP_HAUSDORFF", new String[]{""});
    statsDmap.put("DMAP_MED_FO", new String[]{""});
    statsDmap.put("DMAP_MED_OF", new String[]{""});
    statsDmap.put("DMAP_MED_MIN", new String[]{""});
    statsDmap.put("DMAP_MED_MAX", new String[]{""});
    statsDmap.put("DMAP_MED_MEAN", new String[]{""});
    statsDmap.put("DMAP_FOM_FO", new String[]{""});
    statsDmap.put("DMAP_FOM_OF", new String[]{""});
    statsDmap.put("DMAP_FOM_MIN", new String[]{""});
    statsDmap.put("DMAP_FOM_MAX", new String[]{""});
    statsDmap.put("DMAP_FOM_MEAN", new String[]{""});
    statsDmap.put("DMAP_ZHU_FO", new String[]{""});
    statsDmap.put("DMAP_ZHU_OF", new String[]{""});
    statsDmap.put("DMAP_ZHU_MIN", new String[]{""});
    statsDmap.put("DMAP_ZHU_MAX", new String[]{""});
    statsDmap.put("DMAP_ZHU_MEAN", new String[]{""});
  }

  static {
    statsRps.put("RPS_REL", new String[]{""});
    statsRps.put("RPS_RES", new String[]{""});
    statsRps.put("RPS_UNC", new String[]{""});
    statsRps.put("RPS", new String[]{RPS});
    statsRps.put("RPSS", new String[]{RPS});
    statsRps.put("RPSS_SMPL", new String[]{""});
    statsRps.put("RPS_COMP", new String[]{RPS});
    statsRps.put("RPS_TOTAL", new String[]{RPS});
  }

  static {
    statsCtc.put("CTC_FY_OY", new String[]{CTC});
    statsCtc.put("CTC_FY_ON", new String[]{CTC});
    statsCtc.put("CTC_FN_OY", new String[]{CTC});
    statsCtc.put("CTC_FN_ON", new String[]{CTC});
    statsCtc.put("CTC_OY", new String[]{CTC});
    statsCtc.put("CTC_ON", new String[]{CTC});
    statsCtc.put("CTC_FY", new String[]{CTC});
    statsCtc.put("CTC_FN", new String[]{CTC});
    statsCtc.put("CTC_TOTAL", new String[]{CTC});
  }

  static {
    statsOrank.put("ORANK_PIT", new String[]{""});
    statsOrank.put("ORANK_RANK", new String[]{""});
    statsOrank.put("ORANK_ENS_MEAN", new String[]{""});
    statsOrank.put("ORANK_SPREAD", new String[]{""});
    statsOrank.put("ORANK_CLIMO", new String[]{""});
    statsOrank.put("ORANK_ENS_MEAN_OERR", new String[]{""});
    statsOrank.put("ORANK_SPREAD_OERR", new String[]{""});
    statsOrank.put("ORANK_SPREAD_PLUS_OERR", new String[]{""});
  }

  static {
    statsCnt.put("FBAR", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("FSTDEV", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("OBAR", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("OSTDEV", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("PR_CORR", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("SP_CORR", new String[]{});
    statsCnt.put("KT_CORR", new String[]{});
    statsCnt.put("ME", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("ESTDEV", new String[]{"nc", "bc", SL1L2});
    statsCnt.put("MBIAS", new String[]{"bc", SL1L2});
    statsCnt.put("MAE", new String[]{"bc", SL1L2});
    statsCnt.put("MSE", new String[]{"bc", SL1L2});
    statsCnt.put("BCMSE", new String[]{"bc", SL1L2});
    statsCnt.put("BCRMSE", new String[]{"bc", SL1L2});
    statsCnt.put("RMSE", new String[]{"bc", SL1L2});
    statsCnt.put("FGBAR", new String[]{"bc", GRAD});
    statsCnt.put("OGBAR", new String[]{"bc", GRAD});
    statsCnt.put("MGBAR", new String[]{"bc", GRAD});
    statsCnt.put("EGBAR", new String[]{"bc", GRAD});
    statsCnt.put("S1", new String[]{"bc", GRAD});
    statsCnt.put("S1_OG", new String[]{"bc", GRAD});
    statsCnt.put("FGOG_RATIO", new String[]{"bc", GRAD});
    statsCnt.put("E10", new String[]{"bc"});
    statsCnt.put("E25", new String[]{"bc"});
    statsCnt.put("E50", new String[]{"bc"});
    statsCnt.put("E75", new String[]{"bc"});
    statsCnt.put("E90", new String[]{"bc"});
    statsCnt.put("IQR", new String[]{"bc"});
    statsCnt.put("MAD", new String[]{"bc"});
    //statsCnt.put("PAC", new String[]{"bc"});
    statsCnt.put("ANOM_CORR", new String[]{"bc", SAL1L2});
    statsCnt.put("ANOM_CORR_UNCNTR", new String[]{"bc", SAL1L2});
    statsCnt.put("ME2", new String[]{"bc", SL1L2});
    statsCnt.put("MSESS", new String[]{"bc", SL1L2});
    statsCnt.put("RMSFA", new String[]{"bc", SAL1L2});
    statsCnt.put("RMSOA", new String[]{"bc", SAL1L2});
  }

  static {
    statsSl1l2.put("SL1L2_TOTAL", new String[]{SL1L2});
  }

  static {
    statsSal1l2.put("SAL1L2_TOTAL", new String[]{SAL1L2});
  }

  static {
    statsGrad.put("GRAD_TOTAL", new String[]{GRAD});
  }

  static {
    statsVcnt.put("VCNT_FBAR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_OBAR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_FS_RMS", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_OS_RMS", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_MSVE", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_RMSVE", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_FSTDEV", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_OSTDEV", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_FDIR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_ODIR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_FBAR_SPEED", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_OBAR_SPEED", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_VDIFF_SPEED", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_VDIFF_DIR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_SPEED_ERR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_SPEED_ABSERR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_DIR_ERR", new String[]{"bc", VL1L2});
    statsVcnt.put("VCNT_DIR_ABSERR", new String[]{"bc", VL1L2});
  }

  //TOTAL N_ENS CRPS CRPSS IGN ME RMSE SPREAD ME_OERR RMSE_OERR SPREAD_OERR SPREAD_PLUS_OERR
  static {
    statsEcnt.put("ECNT_CRPS", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_CRPSS", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_IGN", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_ME", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_RMSE", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_SPREAD", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_ME_OERR", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_RMSE_OERR", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_SPREAD_OERR", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_SPREAD_PLUS_OERR", new String[]{"bc", ECNT});
    statsEcnt.put("ECNT_TOTAL", new String[]{ECNT});
  }

  static {
    statsSsvar.put("SSVAR_FBAR", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_FSTDEV", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_OBAR", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_OSTDEV", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_PR_CORR", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_ME", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_ESTDEV", new String[]{"nc", "bc", SSVAR});
    statsSsvar.put("SSVAR_MBIAS", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_MSE", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_BCMSE", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_BCRMSE", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_RMSE", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_ANOM_CORR", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_ME2", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_MSESS", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_Spread", new String[]{"bc", SSVAR});
    statsSsvar.put("SSVAR_TOTAL", new String[]{SSVAR});
  }


  static {
    statsCts.put("BASER", new String[]{"nc", "bc", CTC});
    statsCts.put("FMEAN", new String[]{"nc", "bc", CTC});
    statsCts.put("ACC", new String[]{"nc", "bc", CTC});
    statsCts.put("FBIAS", new String[]{"bc", CTC});
    statsCts.put("PODY", new String[]{"nc", "bc", CTC});
    statsCts.put("PODN", new String[]{"nc", "bc", CTC});
    statsCts.put("POFD", new String[]{"nc", "bc", CTC});
    statsCts.put("FAR", new String[]{"nc", "bc", CTC});
    statsCts.put("CSI", new String[]{"nc", "bc", CTC});
    statsCts.put("GSS", new String[]{"bc", CTC});
    statsCts.put("HK", new String[]{"nc", "bc", CTC});
    statsCts.put("HSS", new String[]{"bc", CTC});
    statsCts.put("ODDS", new String[]{"nc", "bc", CTC});
    statsCts.put("LODDS", new String[]{"nc", "bc", CTC});
    statsCts.put("ORSS", new String[]{"nc", "bc", CTC});
    statsCts.put("EDS", new String[]{"nc", "bc", CTC});
    statsCts.put("SEDS", new String[]{"nc", "bc", CTC});
    statsCts.put("EDI", new String[]{"nc", "bc", CTC});
    statsCts.put("SEDI", new String[]{"nc", "bc", CTC});
    statsCts.put("BAGSS", new String[]{"bc", CTC});
  }

  static {
    statsNbrcts.put("NBR_BASER", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_FMEAN", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_ACC", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_FBIAS", new String[]{"bc", NBRCTC});
    statsNbrcts.put("NBR_PODY", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_PODN", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_POFD", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_FAR", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_CSI", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_GSS", new String[]{"bc", NBRCTC});
    statsNbrcts.put("NBR_HK", new String[]{"nc", "bc", NBRCTC});
    statsNbrcts.put("NBR_HSS", new String[]{"bc", NBRCTC});
    statsNbrcts.put("NBR_ODDS", new String[]{"nc", "bc", NBRCTC});
  }

  static {
    statsNbrctc.put("NBR_CTC_TOTAL", new String[]{NBRCTC});
  }

  static {
    statsNbrcnt.put("NBR_FBS", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_FSS", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_AFSS", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_UFSS", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_F_RATE", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_O_RATE", new String[]{"bc", NBRCNT});
    statsNbrcnt.put("NBR_CNT_TOTAL", new String[]{NBRCNT});
  }

  static {
    statsPstd.put("PSTD_BASER", new String[]{"nc", PCT});
    statsPstd.put("PSTD_RELIABILITY", new String[]{PCT});
    statsPstd.put("PSTD_RESOLUTION", new String[]{PCT});
    statsPstd.put("PSTD_UNCERTAINTY", new String[]{PCT});
    statsPstd.put("PSTD_ROC_AUC", new String[]{PCT});
    statsPstd.put("PSTD_BRIER", new String[]{"nc", PCT});
    statsPstd.put("PSTD_BRIERCL", new String[]{});
    statsPstd.put("PSTD_BSS", new String[]{});
    statsPstd.put("PSTD_BSS_SMPL", new String[]{PCT});
  }

  static {
    statsPct.put("PCT_TOTAL", new String[]{PCT});
  }

  static {
    statsMcts.put("MCTS_ACC", new String[]{"nc", "bc"});
    statsMcts.put("MCTS_HK", new String[]{"bc"});
    statsMcts.put("MCTS_HSS", new String[]{"bc"});
    statsMcts.put("MCTS_GER", new String[]{"bc"});
    //statsMcts.put("MCTS_TOTAL", new String[]{MCTC});
  }

  static {
    statsRhist.put("RHIST_CRPS", new String[]{});
    statsRhist.put("RHIST_CRPSS", new String[]{});
    statsRhist.put("RHIST_IGN", new String[]{});
    statsRhist.put("RHIST_SPREAD", new String[]{});
  }

  static {
    statsVal1l2.put("VAL1L2_ANOM_CORR", new String[]{VAL1L2});
    statsVal1l2.put("VAL1L2_TOTAL", new String[]{VAL1L2});
  }

  static {
    statsVl1l2.put("VL1L2_UFBAR", new String[]{});
    statsVl1l2.put("VL1L2_VFBAR", new String[]{});
    statsVl1l2.put("VL1L2_UOBAR", new String[]{});
    statsVl1l2.put("VL1L2_VOBAR", new String[]{});
    statsVl1l2.put("VL1L2_BIAS", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_FVAR", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_OVAR", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_SPEED_ERR", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_RMSVE", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_SPEED_DIFF", new String[]{VL1L2});
    statsVl1l2.put("VL1L2_TOTAL", new String[]{VL1L2});
  }

  public static final String COUNT = "COUNT(*)";

  static {
    modeSingleStatField.put("ACOV", "SUM(area)");
    modeSingleStatField.put("CNT", COUNT);
    modeSingleStatField.put("CNTSUM", COUNT);
    modeSingleStatField.put("CENTX", "centroid_x");
    modeSingleStatField.put("CENTY", "centroid_y");
    modeSingleStatField.put("CENTLAT", "centroid_lat");
    modeSingleStatField.put("CENTLON", "centroid_lon");
    modeSingleStatField.put("AXAVG", "axis_avg");
    modeSingleStatField.put("LEN", "length");
    modeSingleStatField.put("WID", "width");
    modeSingleStatField
            .put("ASPECT", "IF((length/width) < (width/length), length/width, width/length)");
    modeSingleStatField.put("AREA", "area");
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


  static {
    modePairStatField.put("CENTDIST", "centroid_dist");
    modePairStatField.put("BOUNDDIST", "boundary_dist");
    modePairStatField.put("HULLDIST", "convex_hull_dist");
    modePairStatField.put("ANGLEDIFF", "angle_diff");
    modePairStatField.put("AREARATIO", "area_ratio");
    modePairStatField.put("INTAREA", "intersection_area");
    modePairStatField.put("UNIONAREA", "union_area");
    modePairStatField.put("SYMDIFF", "symmetric_diff");
    modePairStatField.put("INTOVERAREA", "intersection_over_area");
    modePairStatField.put("CMPLXRATIO", "complexity_ratio");
    modePairStatField.put("PERCINTRATIO", "percentile_intensity_ratio");
    modePairStatField.put("INT", "interest");
    modePairStatField.put("MAXINT", "MAX(interest)");
    modePairStatField.put("MAXINTF", "MAX(interest)");
    modePairStatField.put("MAXINTO", "MAX(interest)");
    modePairStatField.put("ASPECTDIFF", "aspect_diff");
    modePairStatField.put("CURVATURERATIO", "curvature_ratio");
  }

  static {
    mtd2dStatField.put("2D_AREA", "area");
    mtd2dStatField.put("2D_CENTROID_X", "centroid_x");
    mtd2dStatField.put("2D_CENTROID_Y", "centroid_y");
    mtd2dStatField.put("2D_CENTROID_LAT", "centroid_lat");
    mtd2dStatField.put("2D_CENTROID_LON", "centroid_lon");
    mtd2dStatField.put("2D_AXIS_ANG", "axis_ang");
    mtd2dStatField.put("2D_INTENSITY_10", "intensity_10");
    mtd2dStatField.put("2D_INTENSITY_25", "intensity_25");
    mtd2dStatField.put("2D_INTENSITY_50", "intensity_50");
    mtd2dStatField.put("2D_INTENSITY_75", "intensity_75");
    mtd2dStatField.put("2D_INTENSITY_90", "intensity_90");
    mtd2dStatField.put("2D_INTENSITY_N", "intensity_nn");
  }

  static {
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
    mtd3dSingleStatField.put("3D_INTENSITY_N", "intensity_nn");
  }

  static {
    mtd3dPairStatField.put("3D_SPACE_CENTROID_DIST", "space_centroid_dist");
    mtd3dPairStatField.put("3D_TIME_CENTROID_DELTA", "time_centroid_delta");
    mtd3dPairStatField.put("3D_AXIS_DIFF", "axis_diff");
    mtd3dPairStatField.put("3D_SPEED_DELTA", "speed_delta");
    mtd3dPairStatField.put("3D_DIRECTION_DIFF", "direction_diff");
    mtd3dPairStatField.put("3D_VOLUME_RATIO", "volume_ratio");
    mtd3dPairStatField.put("3D_START_TIME_DELTA", "start_time_delta");
    mtd3dPairStatField.put("3D_END_TIME_DELTA", "end_time_delta");
    mtd3dPairStatField.put("3D_INTERSECTION_VOLUME", "intersection_volume");
    mtd3dPairStatField.put("3D_DURATION_DIFF", "duration_diff");
    mtd3dPairStatField.put("3D_INTEREST", "interest");
  }

  static {
    statsPerc.put("FCST_PERC", new String[]{});
    statsPerc.put("OBS_PERC", new String[]{});
  }


  static {
    calcStatCTC.add("BASER");
    calcStatCTC.add("BASER");
    calcStatCTC.add("ACC");
    calcStatCTC.add("FBIAS");
    calcStatCTC.add("PODY");
    calcStatCTC.add("POFD");
    calcStatCTC.add("PODN");
    calcStatCTC.add("FAR");
    calcStatCTC.add("CSI");
    calcStatCTC.add("GSS");
  }

  private MVUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Build a list of strings representing consecutive dates between the input dates start and end,
   * incrementing by incr number of seconds.  It is assumed that the format of start and end is
   * given by the java date format string format.  The output dates will have the same format.
   *
   * @param start  Beginning date, given in the format specified by the format input
   * @param end    End date, given in the format specified by the format input
   * @param incr   Number of seconds to increment between successive dates in the list
   * @param format Java date format string, describing input and output dates
   * @return List of date strings
   */
  public static List<String> buildDateList(
          final String start, final String end, final int incr,
          final String format) {
    SimpleDateFormat formatDate = new SimpleDateFormat(format, Locale.US);
    formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    List<String> listDates = new ArrayList<>();

    try {
      Date dateStart = formatDate.parse(start);
      Date dateEnd = formatDate.parse(end);
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.setTime(dateStart);

      while ((incr > 0 && cal.getTime().getTime() <= dateEnd.getTime())
              || (incr < 0 && cal.getTime().getTime() >= dateEnd.getTime())) {
        listDates.add(formatDate.format(cal.getTime()));
        cal.add(Calendar.SECOND, incr);
      }

    } catch (ParseException e) {
      errorStream
              .print("  **  ERROR: caught " + e.getClass() + " in buildDateList(): " + e.getMessage());

    }
    return listDates;
  }

  /**
   * Wrap the buildDateList implementation above, by parsing the specified node for start, end, incr
   * and format and then returning the list of dates.
   *
   * @param node MVNode to parse for the date list parameters
   * @return List of date strings
   */
  public static List<String> buildDateList(final MVNode node, final PrintStream printStream)
          throws ValidationException {
    String strStart = "";
    String strEnd = "";
    int intInc = 0;
    String strFormat = "";

    for (int j = 0; j < node.children.length; j++) {
      MVNode nodeChild = node.children[j];
      if (nodeChild.tag.equals("inc")) {
        intInc = Integer.parseInt(nodeChild.value);
      } else if (nodeChild.tag.equals("format")) {
        strFormat = nodeChild.value;
      }
    }
    for (int j = 0; j < node.children.length; j++) {
      MVNode nodeChild = node.children[j];
      if (nodeChild.tag.equals("start")) {
        if (0 < nodeChild.children.length) {
          strStart = parseDateOffset(nodeChild.children[0], strFormat);
        } else {
          strStart = nodeChild.value;
        }
      } else if (nodeChild.tag.equals("end")) {
        strEnd = (0 < nodeChild.children.length
                ? parseDateOffset(nodeChild.children[0], strFormat) : nodeChild.value);
      }
    }

    return buildDateList(strStart, strEnd, intInc, strFormat);
  }

  /**
   * Build a String representation of the date specified by the input <date_offset> {@link MVNode}.
   * The offset is taken either from the current date (default) or from the date specified by the
   * input date.
   *
   * @param node   MVNode structure specifying the offset
   * @param format (optional) String representation of the input/output date formats
   * @param date   (optional) String representation of the date from which to offset
   * @return String representation of the offset date
   */
  public static String parseDateOffset(final MVNode node, final String format, final String date)
          throws ValidationException {
    int intOffset = 0;
    int intHour = 0;

    for (int i = 0; i < node.children.length; i++) {
      MVNode nodeChild = node.children[i];
      try {
        if (nodeChild.tag.equals("day_offset")) {
          intOffset = Integer.parseInt(nodeChild.value);
        } else if (nodeChild.tag.equals("hour")) {
          intHour = Integer.parseInt(nodeChild.value);
        }
      } catch (NumberFormatException e) {
        throw new ValidationException("day_offset or hour is invalid");
      }
    }

    SimpleDateFormat formatOffset = new SimpleDateFormat(format, Locale.US);
    formatOffset.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    try {
      cal.setTime(formatOffset.parse(date));
    } catch (ParseException e) {
      throw new ValidationException("date" + date + " is invalid");
    }
    cal.set(Calendar.HOUR_OF_DAY, intHour);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.add(Calendar.DATE, intOffset);

    return formatOffset.format(cal.getTime());
  }

  public static String parseDateOffset(final MVNode node, final String format) throws ValidationException {
    return parseDateOffset(node, format, null);
  }


  /**
   * Concatenate the elements of the input list with surrounding ticks and separated by commas for
   * use in the where clause of a SQL query.  For example, the function call
   * <code>buildValueList(new String[]{"a", "bb", "c"})</code> will return the string "'a', 'bb',
   * 'c'".
   *
   * @param values The list of values to be concatenated
   * @return The string of concatenated values for use in a SQL where clause
   */
  public static String buildValueList(final String[] values) {
    String[] localValues;
    if (values != null && values.length > 0) {
      List<String> newValues = new ArrayList<>();
      for (String value : values) {
        if (value.contains(",")) {
          String[] valuesArr = value.split(",");
          for (String v : valuesArr) {
            newValues.add(v);
          }
        } else {
          newValues.add(value);
        }
      }
      localValues = newValues.toArray(new String[newValues.size()]);
    } else {
      localValues = values;
    }
    String strValueList = "";
    for (int i = 0; null != localValues && i < localValues.length; i++) {
      if (0 < i) {
        strValueList += ", " + "'" + localValues[i] + "'";
      } else {
        strValueList += "" + "'" + localValues[i] + "'";
      }
    }
    return strValueList;
  }

  /**
   * Create a {@link MVDataTable} whose fields are the keys of the input table and whose rows
   * represent every permutation of the values stored in the input table.  It is assumed that the
   * table contains a mapping from String to String[].  If the input table is an {@link
   * MVOrderedMap}, the fields of the output MVDataTable are ordered in the same order as the keys
   * of the input.
   *
   * @param table Contains key/value pairs of String/String[] which will be permuted
   * @return MVDataTable whose rows are the permutations
   */
  public static MVDataTable permute(final MVOrderedMap table) {

    if (null == table || 1 > table.size()) {
      return new MVDataTable();
    }

    //  use the ordered list of table entries, if appropriate
    Map.Entry[] listVals;
    listVals = table.getOrderedEntries();

    //  if the input table contains a single value, build and return the simplest table
    if (1 == listVals.length) {
      MVDataTable dtRet = new MVDataTable();
      String strField = (String) listVals[0].getKey();
      Object objVal = listVals[0].getValue();

      //  handle simple field value lists
      if (objVal instanceof String[]) {
        dtRet.addField(strField);
        String[] listVal = (String[]) listVals[0].getValue();
        for (String val : listVal) {
          MVOrderedMap tableRow = new MVOrderedMap();
          tableRow.put(strField, val);
          dtRet.addRow(tableRow);
        }

        //  handle field value sets
      } else if (objVal instanceof MVOrderedMap) {
        dtRet.addField(strField + "_set");
        Map.Entry[] listValSet = ((MVOrderedMap) objVal).getOrderedEntries();
        for (int i = 0; i < listValSet.length; i++) {
          MVOrderedMap tableRow = new MVOrderedMap();
          tableRow.put(strField + "_set", listValSet[i].getKey());
          dtRet.addRow(tableRow);
        }
      }
      return dtRet;
    }

    //  if the input table contains more than one value, build the sub-table first
    String strField = (String) listVals[0].getKey();
    MVOrderedMap tableSub = new MVOrderedMap(table);
    tableSub.remove(strField);
    MVDataTable dtSub = permute(tableSub);

    //  build a new table with one copy of the sub-table for each value of the current field
    MVOrderedMap[] listRows = dtSub.getRows();
    MVDataTable dtRet = new MVDataTable(dtSub.getFields());
    Object objVal = listVals[0].getValue();

    //  handle simple field value lists
    if (objVal instanceof String[]) {
      dtRet.addField(strField, "", 0);
      String[] listVal = (String[]) listVals[0].getValue();
      for (int i = 0; i < listVal.length; i++) {
        for (int j = 0; j < listRows.length; j++) {
          MVOrderedMap tableRow = new MVOrderedMap(listRows[j]);
          tableRow.put(strField, listVal[i]);
          dtRet.addRow(tableRow);
        }
      }

      //  handle field value sets
    } else if (objVal instanceof MVOrderedMap) {
      dtRet.addField(strField + "_set", "", 0);
      Map.Entry[] listValSet = ((MVOrderedMap) objVal).getOrderedEntries();
      for (int i = 0; i < listValSet.length; i++) {
        for (int j = 0; j < listRows.length; j++) {
          MVOrderedMap tableRow = new MVOrderedMap(listRows[j]);
          tableRow.put(strField + "_set", listValSet[i].getKey());
          dtRet.addRow(tableRow);
        }
      }
    }

    return dtRet;
  }


  /**
   * Sort the list of input thresholds, according to the numeric threshold value.
   *
   * @param thresh List of thresholds
   * @return Sorted threshold list, by value
   */
  public static List<String> sortThresh(List<String> thresh) {
    thresh.sort(
            new Comparator<String>() {
              private final Pattern PATTERN_WITH_FLOAT = Pattern.compile("(\\D*)([-+]?\\d*\\.?\\d+)");

              public int compare(String s1, String s2) {

                // if the threshold contains  a float like this '>.1' -
                // add a missing 0 -> '>0.1'
                if (s1.contains(">.") || s1.contains("<.") || s1.contains("=.")
                        || s1.contains("> .") || s1.contains("< .") || s1.contains("= .")) {
                  int pos = s1.indexOf('.');
                  s1 = s1.substring(0, pos) + "0" + s1.substring(pos);
                }
                if (s2.contains(">.") || s2.contains("<.") || s2.contains("=.")
                        || s2.contains("> .") || s2.contains("< .") || s2.contains("= .")) {
                  int pos = s2.indexOf('.');
                  s2 = s2.substring(0, pos) + "0" + s2.substring(pos);
                }
                Matcher m1 = PATTERN_WITH_FLOAT.matcher(s1);
                Matcher m2 = PATTERN_WITH_FLOAT.matcher(s2);

                // The only way find() could fail is at the end of a string
                while (m1.find() && m2.find()) {


                  // matcher.group(2) fetches any digits captured by the
                  // second parentheses in PATTERN.
                  if (m1.group(2).isEmpty()) {
                    return m2.group(2).isEmpty() ? 0 : -1;
                  } else if (m2.group(2).isEmpty()) {
                    return +1;
                  }

                  Float n1 = Float.valueOf(m1.group(2));
                  Float n2 = Float.valueOf(m2.group(2));
                  int numberCompare = n1.compareTo(n2);
                  if (0 != numberCompare) {
                    return numberCompare;
                  }
                  // matcher.group(1) fetches any non-digits captured by the
                  // first parentheses in PATTERN.
                  int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
                  if (0 != nonDigitCompare) {
                    return nonDigitCompare;
                  }
                }

                // Handle if one string is a prefix of the other.
                // Nothing comes before something.
                if (m1.hitEnd() && m2.hitEnd()) {
                  return 1;
                }
                if (m1.hitEnd()) {
                  return -1;
                }
                if (m2.hitEnd()) {
                  return 1;
                }
                return +1;
              }
            }
    );

    return thresh;
  }


  /**
   * Sort the list of input levels, according to the first numeric level value.
   *
   * @param lev List of thresholds
   * @return Sorted threshold list, by value
   */
  public static List<String> sortLev(final List<String> lev) {
    return sortVals(lev, MVUtil.lev);
  }

  /**
   * Sort the list of Interp Pnts, according to the first numeric level value.
   *
   * @param lev List of Interp Pnts
   * @return Sorted Inter Pnts list, by value
   */
  public static List<String> sortInterpPnts(final List<String> lev) throws ValidationException {
    List<Integer> resultInt = new ArrayList<>(lev.size());
    for (String interpPnt : lev) {
      try {
        resultInt.add(Integer.valueOf(interpPnt));
      } catch (NumberFormatException e) {
        throw new ValidationException("interp_pnt is invalid");
      }
    }
    Collections.sort(resultInt);
    List<String> resultStr = new ArrayList<>(resultInt.size());
    for (Integer interpPnt : resultInt) {
      resultStr.add(String.valueOf(interpPnt));
    }
    return resultStr;
  }

  /**
   * Sort the input list of values by parsing them with the input pattern and sort them according to
   * the numerical portion (assumed to be group 2 of the matched pattern).
   *
   * @param vals List of String representations of the values
   * @param pat  Pattern used to parse the input values
   * @return Sorted list, by numerical value
   */
  public static List<String> sortVals(
          final List<String> vals,
          final Pattern pat) {

    //  parse the input values and store the numerical values in a sortable array
    double[] listVal = new double[vals.size()];
    Map tableVal = new HashMap<>();
    double dblInvalid = -.00001;
    for (int i = 0; i < vals.size(); i++) {
      String val = vals.get(i);

      //  apply the pattern to the value
      double dblVal;
      //if value is double and ends with '.' - remove '.' to match the pattern
      if (val.endsWith(".")) {
        val = val.substring(0, val.length() - 1);
      }
      Matcher mat = pat.matcher(val);

      //  if the value matches, parse out the numerical value
      if (mat.matches()) {
        if (mat.groupCount() == 0) {
          dblVal = Double.parseDouble(val);
        } else {
          dblVal = Double.parseDouble(mat.group(2));
          if (3 == mat.groupCount() && null != mat.group(3)) {
            dblVal = (dblVal + Double.parseDouble(mat.group(3))) / 2;
          }
        }
      }

      //  otherwise, use the literal value with a default numerical value
      else {
        dblVal = dblInvalid;
        dblInvalid -= .00001;
      }

      //  verify and store the numerical value and the value pair
      listVal[i] = dblVal;
      Double dblKey = listVal[i];
      Object objVal = vals.get(i);
      if (tableVal.containsKey(dblKey)) {
        Object objValCur = tableVal.get(dblKey);
        ArrayList listValCur = new ArrayList();
        if (objValCur instanceof String) {
          listValCur.add(objValCur);
          listValCur.add(val);
        } else {
          ((ArrayList) objValCur).add(val);
        }
        objVal = listValCur;
      }
      tableVal.put(listVal[i], objVal);
    }

    //  sort the numerical values and build a sorted list of values
    Arrays.sort(listVal);
    ArrayList listRet = new ArrayList();
    Map<Double, String> tableAdded = new HashMap<>();
    for (int i = 0; i < listVal.length; i++) {

      //  verify that the values have not already been added
      Double dblKey = listVal[i];
      if (tableAdded.containsKey(dblKey)) {
        continue;
      }

      //  if not, add the value(s) to the return list
      Object objValCur = tableVal.get(dblKey);
      if (objValCur instanceof String) {
        listRet.add(listRet.size(), objValCur);
      } else {
        ArrayList listValCur = (ArrayList) objValCur;
        for (Object valCur : listValCur) {
          listRet.add(listRet.size(), valCur);
        }
      }

      tableAdded.put(dblKey, "true");
    }

    return listRet;
  }

  /**
   * Parse, format and sort the input list of lead times, removing the trailing 0000, if requested.
   *
   * @param lead List of lead time values
   * @return Sorted list of formatted lead times, by numerical value
   */
  public static List<String> sortFormatLead(
          final List<String> lead) {

    //  parse and format the leads and store the numerical values in a sortable array
    double[] listVal = new double[lead.size()];
    Map<Double, String> tableVal = new HashMap<>();
    for (int i = 0; i < lead.size(); i++) {
      listVal[i] = Double.parseDouble(lead.get(i));
      tableVal.put(listVal[i], lead.get(i));
    }

    //  sort the lead numerical values and build a sorted list of leads
    Arrays.sort(listVal);
    List<String> listRet = new ArrayList<>(lead.size());
    for (int i = 0; i < listVal.length; i++) {
      listRet.add(i, tableVal.get(listVal[i]));
    }

    return listRet;
  }

  /**
   * removes the trailing .0 from dates .
   *
   * @return list of formatted  dates
   */
  public static List<String> formatDates(final List<String> dates) {
    List<String> listRet = new ArrayList<>(dates.size());
    for (int i = 0; i < dates.size(); i++) {
      listRet.add(i, dates.get(i).replace(".0", ""));
    }
    return listRet;
  }

  public static List<String> sortHour(final List<String> hour) throws ValidationException {

    List<Integer> hoursInt = new ArrayList<>();
    for (String hourStr : hour) {
      try {
        hoursInt.add(Integer.valueOf(hourStr));
      } catch (NumberFormatException e) {
        throw new ValidationException("the hour " + hourStr + " is invalid");
      }
    }
    Collections.sort(hoursInt);

    List<String> result = new ArrayList<>();
    for (Integer hourInt : hoursInt) {
      result.add(String.format("%02d", hourInt));
    }
    return result;

  }


  /**
   * Pads input str with spaces appended to the end so that the length of the returned String is at
   * least width characters
   *
   * @param str   The string to pad
   * @param width The minimum number of characters in the returned String
   * @return the padded version of the input str
   */
  public static String padEnd(String str, final String pad, final int width) {
    while (width > str.length()) {
      str += pad;
    }
    return str;
  }

  public static String padEnd(final String str, final int width) {
    return padEnd(str, " ", width);
  }


  /**
   * Pads input str with spaces appended to the beginning so that the length of the returned String
   * is at least width characters
   *
   * @param str   The string to pad
   * @param width The minimum number of characters in the returned String
   * @return the padded version of the input str
   */
  public static String padBegin(String str, final String pad, final int width) {
    while (width > str.length()) {
      str = pad + str;
    }
    return str;
  }

  public static String padBegin(final String str, final int width) {
    return padBegin(str, " ", width);
  }

  public static String padBegin(final String str) {
    return padBegin(str, 16);
  }

  /**
   * Create a string representation for the input time span, which should represent milliseconds
   * between events.  For example, a time span message can be generated as follows:
   * <code>formatTimeStamp(dateEnd.getTime() - dateStart.getTime())</code>
   *
   * @param span Time span, in milliseconds
   * @return Time span in format [days]d H:mm:ss.mmmm
   */
  public static String formatTimeSpan(long span) {
    long intDay = span / (24L * 60L * 60L * 1000L);
    span -= intDay * 24L * 60L * 60L * 1000L;
    long intHr = span / (60L * 60L * 1000L);
    span -= intHr * 60L * 60L * 1000L;
    long intMin = span / (60L * 1000L);
    span -= intMin * 60L * 1000L;
    long intSec = span / 1000L;
    span -= intSec * 1000L;
    long intMs = span;

    return (0 < intDay ? Long.toString(intDay) + "d " : "") + Long.toString(intHr)
            + (10 > intMin ? ":0" : ":") + Long.toString(intMin)
            + (10 > intSec ? ":0" : ":") + Long.toString(intSec) + "."
            + (100 > intMs ? "0" + (10 > intMs ? "0" : "") : "") + Long.toString(intMs);
  }


  /**
   * Append the first array with the values of the second
   *
   * @param l1 Array to be appended
   * @param l2 Array to append
   * @return The combined array
   */
  public static Object[] append(final Object[] l1, final Object[] l2, final Object[] cast) {
    List listRet = Arrays.asList(l1);
    listRet.addAll(Arrays.asList(l2));
    return listRet.toArray(cast);

  }

  public static String[] append(final String[] s1, final String[] s2) {
    return (String[]) append(s1, s2, new String[]{});
  }

  public static String[] append(final String s1, final String[] s2) {
    return append(new String[]{s1}, s2);
  }

  public static String[] append(final String[] s1, final String s2) {
    return append(s1, new String[]{s2});
  }


  public static Map.Entry[] append(final Map.Entry[] s1, final Map.Entry[] s2) {
    return (Map.Entry[]) append(s1, s2, new Map.Entry[]{});
  }

  public static MVPlotJob[] append(final MVPlotJob[] s1, final MVPlotJob[] s2) {
    return (MVPlotJob[]) append(s1, s2, new MVPlotJob[]{});
  }

  public static int[] append(int[] s1, int[] s2) {
    if (null == s1) {
      s1 = new int[]{};
    }
    if (null == s2) {
      s2 = new int[]{};
    }
    int r = 0;
    int[] ret = new int[s1.length + s2.length];
    for (int i = 0; i < s1.length; i++) {
      ret[r++] = s1[i];
    }
    for (int i = 0; i < s2.length; i++) {
      ret[r++] = s2[i];
    }
    return ret;
  }

  public static String[] unique(final String[] dataArr) {
    Map<String, String> table = new HashMap<>();
    for (String data : dataArr) {
      if (!table.containsKey(data)) {
        table.put(data, "true");
      }
    }
    Set<String> strings = table.keySet();
    return strings.toArray(new String[strings.size()]);
  }

  public static int sum(int[] dataArr) {
    if (1 > dataArr.length) {
      return 0;
    }
    int intSum = 0;
    for (int data : dataArr) {
      intSum += data;
    }
    return intSum;
  }

  /**
   * Attempt to convert the input ArrayList, which is assumed to contain all Strings, to a
   * String[].
   *
   * @param list ArrayList to convert
   * @return Converted list
   */
  public static String[] toArray(final List<String> list) {
    return list.toArray(new String[list.size()]);
  }

  public static List<String> toArrayList(final String[] list) {
    List<String> ret = new ArrayList<>();
    ret.addAll(Arrays.asList(list));
    return ret;
  }

  /**
   * Create a deep copy of the input list
   *
   * @param list List to copy
   * @return Copied list
   */
  public static MVOrderedMap[] copyList(final MVOrderedMap[] list) {
    MVOrderedMap[] listRet = new MVOrderedMap[list.length];
    for (int i = 0; i < list.length; i++) {
      listRet[i] = new MVOrderedMap(list[i]);
    }
    return listRet;
  }

  /**
   * Create a deep copy of the input list
   *
   * @param list List to copy
   * @return Copied list
   */
  public static String[] copyList(final String[] list) {
    return list.clone();
  }


  /**
   * Returns a string representation of the MVOrderedMap in R declaration syntax
   *
   * @param map Data structure to convert to R list representation
   * @return The R-syntax representation of the input map
   */
  public static String getRDecl(final MVOrderedMap map) {
    String strRDecl = "list(\n";
    List keys = map.getListKeys();
    String[] listKeys = (String[]) keys.toArray(new String[keys.size()]);
    for (int i = 0; i < listKeys.length; i++) {
      if (0 < i) {
        strRDecl += ",\n" + MVUtil.padBegin("`" + listKeys[i].replace("&#38;", "&").replace("&gt;", ">")
                .replace("&lt;", "<") + "`") + " = ";
      } else {
        strRDecl += "" + MVUtil.padBegin("`" + listKeys[i].replace("&#38;", "&").replace("&gt;", ">")
                .replace("&lt;", "<") + "`") + " = ";
      }
      Object objVal = map.get(listKeys[i]);
      if (objVal instanceof String) {
        strRDecl += "\"" + objVal.toString() + "\"";
      } else if (objVal instanceof String[]) {
        strRDecl += "c(";
        String[] listVal = (String[]) objVal;
        for (int j = 0; j < listVal.length; j++) {
          strRDecl += (0 < j ? ", " : "") + "\"" + listVal[j] + "\"";
        }
        strRDecl += ")";
      } else if (objVal instanceof MVOrderedMap) {
        strRDecl += ((MVOrderedMap) objVal).getRDecl();
      } else {
        strRDecl += "\"???\",\n";
      }
    }
    strRDecl += "\n)";
    return strRDecl;
  }

  public static Map<String, Object> getYamlDecl(final MVOrderedMap map) {
    Map<String, Object> result = new LinkedHashMap<>();
    List<String> keys = map.getListKeys();
    for (String key : keys) {
      Object objVal = map.get(key);
      if (objVal instanceof String) {
        result.put(key, new String[]{((String) objVal).replace("&#38;", "&").replace("&gt;", ">")
                .replace("&lt;", "<")});
      } else if (objVal instanceof String[]) {
        result.put(key, objVal);
      } else if (objVal instanceof MVOrderedMap) {
        result.put(key, ((MVOrderedMap) objVal).getYamlDecl());
      }
    }
    return result;
  }


  public static String[] parseModeStat(final String stat) {
    Matcher mat = _patModeStat.matcher(stat);
    if (!mat.matches()) {
      return new String[]{stat};
    }
    return new String[]{mat.group(1), mat.group(2)};
  }


  /**
   * Determine the database line_data table in which the input statistic is stored.  For mode stats,
   * consider only the first portion of the stat name.  If the stat is not found in any table,
   * return an empty string.
   *
   * @param strStat stat name to look up
   * @return the name of the database line_data table which contains the stat
   */
  public static String getStatTable(final String strStat) {
    String strStatMode = parseModeStat(strStat)[0];
    String strStatMrd = "";
    if (strStatMode.equals(strStat)) {
      String[] listStatComp = strStat.split("_");
      strStatMrd = strStat.replace("_" + listStatComp[listStatComp.length - 1], "");
    }

    //TODO mtdRatioField and modeRatioField contain the same fields

    if (statsCnt.containsKey(strStat)) {
      return "line_data_cnt";
    } else if (statsCts.containsKey(strStat)) {
      return "line_data_cts";
    } else if (statsNbrcnt.containsKey(strStat)) {
      return "line_data_nbrcnt";
    } else if (statsNbrcts.containsKey(strStat)) {
      return "line_data_nbrcts";
    } else if (statsPstd.containsKey(strStat)) {
      return "line_data_pstd";
    } else if (statsMcts.containsKey(strStat)) {
      return "line_data_mcts";
    } else if (statsRhist.containsKey(strStat)) {
      return "line_data_rhist";
    } else if (statsVl1l2.containsKey(strStat)) {
      return "line_data_vl1l2";
    } else if (statsVal1l2.containsKey(strStat)) {
      return "line_data_val1l2";
    } else if (modeSingleStatField.containsKey(strStatMode)) {
      return "mode_obj_single";
    } else if (modePairStatField.containsKey(strStatMode)) {
      return "mode_obj_pair";
    } else if (modeRatioField.contains(strStat)) {
      return "mode_obj_single";
    } else if (statsEnscnt.containsKey(strStat)) {
      return "line_data_enscnt";
    } else if (statsMpr.containsKey(strStat)) {
      return "line_data_mpr";
    } else if (statsOrank.containsKey(strStat)) {
      return "line_data_orank";
    } else if (statsSsvar.containsKey(strStat)) {
      return "line_data_ssvar";
    } else if (mtd3dSingleStatField.containsKey(strStatMrd)) {
      return "mtd_3d_obj_single";
    } else if (mtd3dPairStatField.containsKey(strStatMrd)) {
      return "mtd_3d_obj_pair";
    } else if (mtd2dStatField.containsKey(strStatMrd)) {
      return "mtd_2d_obj";
    } else if (mtdRatioField.contains(strStat)) {
      return "mtd_3d_obj_single";
    } else if (statsVcnt.containsKey(strStat)) {
      return "line_data_vcnt";
    } else if (statsEcnt.containsKey(strStat)) {
      return "line_data_ecnt";
    } else if (statsPerc.containsKey(strStat)) {
      return "line_data_perc";
    } else if (statsDmap.containsKey(strStat)) {
      return "line_data_dmap";
    } else if (statsRps.containsKey(strStat)) {
      return "line_data_rps";
    } else if (statsCtc.containsKey(strStat)) {
      return "line_data_ctc";
    } else if (statsNbrctc.containsKey(strStat)) {
      return "line_data_ctc";
    } else if (statsPct.containsKey(strStat)) {
      return "line_data_pct";
    } else if (statsSl1l2.containsKey(strStat)) {
      return "line_data_sl1l2";
    } else if (statsSal1l2.containsKey(strStat)) {
      return "line_data_sal1l2";
    } else if (statsGrad.containsKey(strStat)) {
      return "line_data_grad";
    } else {
      return "";
    }
  }


  public static MvResponse runRscript(
          final String rscript,
          final String script) {
    return runRscript(rscript, script, new String[]{}, null);
  }

  public static MvResponse runRscript(
          final String rscriptCommand, final String scriptName,
          final String[] args) {
    return runRscript(rscriptCommand, scriptName, args, null);
  }

  /**
   * Run the input R script named r using the Rscript command.  The output and error output will be
   * written to standard output.
   *
   * @param rscriptCommand Rscript command
   * @param scriptName     R script to run
   * @param args           (optional) Arguments to pass to the R script
   * @throws Exception
   */
  public static MvResponse runRscript(
          final String rscriptCommand, final String scriptName,
          final String[] args, final String[] env) {

    MvResponse mvResponse = new MvResponse();

    //  build a list of arguments
    StringBuilder argList = new StringBuilder();
    for (int i = 0; null != args && i < args.length; i++) {
      argList.append(' ').append(args[i]);
    }

    Process proc = null;
    InputStreamReader inputStreamReader = null;
    InputStreamReader errorInputStreamReader = null;

    BoundedBufferedReader readerProcStd = null;
    BoundedBufferedReader readerProcErr = null;

    boolean boolExit = false;
    int intExitStatus = 0;
    StringBuilder strProcStd = new StringBuilder();
    StringBuilder strProcErr = new StringBuilder();


    try {
      String rscriptCommandClean = cleanString(rscriptCommand);
      String scriptNameClean = cleanString(scriptName);
      String strArgListClean = cleanString(argList.toString());


      proc = Runtime.getRuntime()
              .exec(rscriptCommandClean + " " + scriptNameClean + strArgListClean,
                      env,
                      new File(System.getProperty("user.home")));
      inputStreamReader = new InputStreamReader(proc.getInputStream());
      errorInputStreamReader = new InputStreamReader(proc.getErrorStream());

      readerProcStd = new BoundedBufferedReader(inputStreamReader, 50000, 2048);
      readerProcErr = new BoundedBufferedReader(errorInputStreamReader, 50000, 2048);
      while (!boolExit) {
        try {
          intExitStatus = proc.exitValue();
          boolExit = true;
        } catch (IllegalThreadStateException e) {
          logger.debug(e.getMessage());
        }

        while (readerProcStd.ready()) {
          String line = readerProcStd.readLineBounded();
          strProcStd.append(line).append('\n');
        }
        while (readerProcErr.ready()) {
          String line = readerProcErr.readLineBounded();
          strProcErr.append(line).append('\n');
        }
      }
    } catch (SecurityException | IOException | IllegalArgumentException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    } finally {

      if (inputStreamReader != null) {
        try {
          inputStreamReader.close();
        } catch (IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (errorInputStreamReader != null) {
        try {
          errorInputStreamReader.close();
        } catch (IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (readerProcStd != null) {
        try {
          readerProcStd.close();
        } catch (IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (readerProcErr != null) {
        try {
          readerProcErr.close();
        } catch (IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (proc != null) {
        proc.destroy();
      }

    }

    String type = "Rscript";
    if (rscriptCommand.contains("python")) {
      type = PYTHON;
    }
    if (strProcStd.length() > 0) {
      mvResponse.setInfoMessage(
              "==== Start " + type + " output  ====\n" + strProcStd + "====   End " + type + " output  ====");
    }

    if (strProcErr.length() > 0) {
      mvResponse.setErrorMessage(
              "==== Start " + type + " error  ====\n" + strProcErr + "====   End " + type + " error  ====");
    }
    mvResponse.setSuccess(0 == intExitStatus);
    return mvResponse;
  }

  /**
   * Populate the template tags in the input template file named tmpl with values from the input
   * table vals and write the result to the output file named output.
   *
   * @param tmpl   Template file to populate
   * @param output Output file to write
   * @param vals   Table containing values corresponding to tags in the input template
   * @throws Exception
   */
  public static void populateTemplateFile(
          final String tmpl, final String output,
          final Map<String, Object> vals) throws IOException {
    try (FileReader fileReader = new FileReader(tmpl);
         BoundedBufferedReader reader = new BoundedBufferedReader(fileReader);
         PrintStream writer = new PrintStream(output)) {
      while (reader.ready()) {
        String strTmplLine = reader.readLineBounded();
        String strOutputLine = strTmplLine;

        Matcher matRtmplLine = patRTmpl.matcher(strTmplLine);
        while (matRtmplLine.find()) {
          String strRtmplTag = matRtmplLine.group(1);
          if (!vals.containsKey(strRtmplTag)) {
            continue;
          }
          String strRTagVal = (String) vals.get(strRtmplTag);
          if (strRTagVal != null) {
            strOutputLine = strOutputLine.replace("#<" + strRtmplTag + ">#", strRTagVal);
          }

        }

        writer.println(strOutputLine);
      }
    } catch (IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
      throw e;
    }

  }


  /**
   * Build a list of the fcst_var/stat combinations stored in the input <dep> structure.  The output
   * list is structured as a list of pairs of Strings, with the first element storing the fcst_var
   * and the second element storing the associated statistic.
   *
   * @param mapDep <dep1> or <dep2> structure from a MVPlotJob
   * @return a list of fcst_var/stat pairs
   */
  public static String[][] buildFcstVarStatList(final MVOrderedMap mapDep) {
    List listRet = new ArrayList();
    String[] listFcstVar = mapDep.getKeyList();
    String[] listStat;
    for (int intFcstVar = 0; intFcstVar < listFcstVar.length; intFcstVar++) {
      listStat = (String[]) mapDep.get(listFcstVar[intFcstVar]);
      for (int intStat = 0; intStat < listStat.length; intStat++) {
        listRet.add(new String[]{listFcstVar[intFcstVar], listStat[intStat]});
      }
    }
    return (String[][]) listRet.toArray(new String[][]{});
  }


  /**
   * Construct the template map for the specified permutation of plot_fix values, using the
   * specified set values.
   *
   * @param mapPlotFix    plot_fix field/value pairs to use in populating the template values
   * @param mapPlotFixVal values used for sets
   * @throws Exception
   */
  public static Map.Entry[] buildPlotFixTmplMap(
          final MVOrderedMap mapPlotFix,
          final MVOrderedMap mapPlotFixVal) {
    Map.Entry[] listPlotFixVal = mapPlotFix.getOrderedEntries();
    //  replace fixed value set names with their value maps
    ArrayList listPlotFixValAdj = new ArrayList();
    for (Map.Entry aListPlotFixVal : listPlotFixVal) {
      String strFixVar = aListPlotFixVal.getKey().toString();
      if (!strFixVar.endsWith("_set")) {
        listPlotFixValAdj.add(aListPlotFixVal);
        continue;
      }

      String strFixVarAdj = strFixVar.replaceAll("_set$", "");
      MVOrderedMap mapFixSet = (MVOrderedMap) mapPlotFixVal.get(strFixVarAdj);
      listPlotFixValAdj.add(new MVMapEntry(strFixVarAdj, mapFixSet));
    }
    listPlotFixVal = (Map.Entry[]) listPlotFixValAdj
            .toArray(new Map.Entry[listPlotFixValAdj.size()]);

    return listPlotFixVal;
  }

  /**
   * check if the aggregation type is compatible with the statistic We're currently only aggregating
   * SL1L2 -> CNT and CTC -> CTS
   *
   * @param tableStats
   * @param strStat
   * @param aggType
   * @throws Exception
   */
  public static void isAggTypeValid(
          final Map<String, String[]> tableStats, final String strStat,
          final String aggType) throws ValidationException {
    //check if aggType is allowed for this stat
    String[] types = tableStats.get(strStat);
    boolean isFound = false;
    for (String type : types) {
      if (type.equals(aggType)) {
        isFound = true;
        break;
      }
    }
    if (!isFound) {
      throw new ValidationException(
              "aggregation type " + aggType + " isn't compatible with the statistic " + strStat);
    }
  }

  public static String findValue(
          final String[] listToken, final List<String> headerNames,
          final String header) {
    int pos = headerNames.indexOf(header);
    if (pos >= 0 && pos < listToken.length) {
      return listToken[pos];
    } else {
      return "NA";
    }
  }


  public static void updateLog4jConfiguration() {

    try {
      String jarPath = MVUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()
              .getPath();
      URI imgurl = new URI("jar:file:" + jarPath + "!/edu/ucar/metviewer/resources/log4j2.xml");
      Logger l = (Logger) LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
      l.getContext().setConfigLocation(imgurl);
    } catch (URISyntaxException | SecurityException | UnsupportedOperationException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }

  }

  /**
   * Format the input String so that it conforms to R variable name standards
   *
   * @param in String to format
   * @return Formatted String
   */
  public static String replaceSpecialChars(final String in) {

    String strFormatR = in;
    Matcher matProb = MVUtil.prob.matcher(in);
    if (matProb.matches()) {
      if (!in.contains("*")) {
        strFormatR = "PROB_" + matProb.group(1) + matProb.group(2) + matProb.group(3);
      } else {
        strFormatR = "PROB_" + matProb.group(1);
      }
    }

    return strFormatR.replace("(", "")
            .replace(")", "")
            .replace("<=", "le")
            .replace(">=", "ge")
            .replace("=", "eq")
            .replace("<", "lt")
            .replace(">", "gt");
  }

  public static String buildTemplateInfoString(final String tmpl, final MVOrderedMap vals,
                                               final MVOrderedMap tmplMaps,
                                               final PrintStream printStream) throws ValidationException {
    return buildTemplate(tmpl, vals, tmplMaps, printStream, "infoString");
  }

  public static String buildTemplateString(
          final String tmpl, final MVOrderedMap vals,
          final MVOrderedMap tmplMaps,
          final PrintStream printStream) throws ValidationException {

    return buildTemplate(tmpl, vals, tmplMaps, printStream, "fileName");
  }

  /**
   * Populate a template string, specified by tmpl, with values specified in the input map vals.  If
   * a template tag is not found in the input vals table, a warning is printed and the tag is passed
   * through to the output.
   *
   * @param tmpl     Template String containing tags with format <tag_name>
   * @param vals     Contains a mapping from tag names to values
   * @param tmplMaps Map of value maps for each template field, used with map template parm
   *                 (optional)
   * @return String built using the template and values
   */
  private static String buildTemplate(
          final String tmpl, final MVOrderedMap vals,
          final MVOrderedMap tmplMaps,
          final PrintStream printStream, final String stringType) throws ValidationException {


    String strRet = tmpl;
    Matcher matTmpl = plotTmpl.matcher(tmpl);
    SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd", Locale.US);
    formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    while (matTmpl.find()) {
      String strTmplTag = matTmpl.group(1);
      String strTmplTagName = matTmpl.group(2);

      MVOrderedMap mapParms = parseTagParams(strTmplTag);
      if (strTmplTagName.equals("date")) {
        vals.put("date", formatDate.format(new Date()));
      }

      if (!vals.containsKey(strTmplTagName)) {
        printStream.println("  **  WARNING: template tag " + strTmplTagName + " not found in agg"
                + " perm");
        continue;
      }

      String strVal = (String) vals.get(strTmplTagName);


      //  if there is a corresponding tag value map, use the map value
      if (mapParms.containsKey("map")) {
        String strMapName = mapParms.get("map").toString();
        if (strMapName.equalsIgnoreCase("true")) {
          strMapName = strTmplTagName;
        }
        MVOrderedMap mapTmplVal = (MVOrderedMap) tmplMaps.get(strMapName);
        if (null == mapTmplVal) {
          throw new ValidationException(
                  "template tag " + strTmplTagName + " does not have a val_map defined");
        }
        if (mapTmplVal.containsKey(strVal)) {
          strVal = mapTmplVal.getStr(strVal);
        }

      }
      if (stringType.equals("fileName")) {
        strVal = strVal.replace(">", "gt").replace("<", "lt").replaceAll("=", "e");
      }

      //  if there is a format parameter, apply it to the value
      if (mapParms.containsKey("format")) {
        String strFormat = mapParms.getStr("format");

        if (strTmplTagName.equals("fcst_lead")) {
          if (strVal.equals("0")) {
            strVal = "00000";
          }
          if (strFormat.equals("HH")) {
            strVal = strVal.substring(0, strVal.length() - 4);
          }
          if (strFormat.equals("HHmm")) {
            strVal = strVal.substring(0, strVal.length() - 2);
          }
          while (strFormat.length() > strVal.length()) {
            strVal = "0" + strVal;
          }

        } else if (strTmplTagName.equals("init_hour")
                || strTmplTagName.equals("valid_hour") && strFormat.equals("HH")) {
          while (2 > strVal.length()) {
            strVal = "0" + strVal;
          }
        }


        if (mapParms.getStr("format").equalsIgnoreCase("R")) {
          strVal = MVUtil.replaceSpecialChars(strVal);
        }
      }

      //  if the tag value is a date, format it accordingly

      try {
        SimpleDateFormat formatDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatDb.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat formatDBms = new SimpleDateFormat(MVUtil.DB_DATE_MS, Locale.US);
        formatDBms.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateParse = formatDb.parse(strVal);
        if (null != dateParse) {
          strVal = formatPlotFormat(dateParse);
        } else {
          dateParse = formatDBms.parse(strVal);
          if (null != dateParse) {
            strVal = formatPlotFormat(dateParse);
          }
        }
      } catch (ParseException e) {
        logger.debug(e.getMessage());
      }

      //  if the tag is a threshold, format it accordingly
      if (strTmplTagName.equals("fcst_thresh") || strTmplTagName.equals("fcst_thr")
              || strTmplTagName.equals("obs_thresh") || strTmplTagName.equals("obs_thr")) {
        strVal = formatThresh(strTmplTag, strVal);
      }
      //replace "/" with "_"  - file names can't have "/"!!!!!
      strVal = strVal.replace("/", "_");

      strRet = strRet.replace("{" + strTmplTag + "}", strVal);
    }
    return strRet;
  }

  /**
   * Parse template tag parameter pairs and return them in an ordered map.  For example,
   * <i>parseTagParams("tag_name?param1=val1;param2=val2")</i> returns a map with two members,
   * param1 and param2 with their values set accordingly.
   *
   * @param tag Formatted tag with param/value pairs to parse
   * @return Ordered map containing parsed param/value pairs
   */
  public static MVOrderedMap parseTagParams(final String tag) {
    MVOrderedMap mapRet = new MVOrderedMap();
    Matcher mat = MVUtil.tag.matcher(tag);
    if (mat.matches() && null != mat.group(2)) {
      String[] listPairs = mat.group(2).split("\\s*&\\s*");
      for (int i = 0; i < listPairs.length; i++) {
        String[] listPair = listPairs[i].split("\\s*=\\s*");
        mapRet.put(listPair[0], listPair[1]);
      }
    }

    return mapRet;
  }

  /**
   * Reformat the fcst_thresh value using the directions provided in the body of the template tag.
   * It is assumed that the input template tag has the parameterized tag format:
   * <i>fcst_thresh?param1=val1;param2=val2[;...]  where the params can be the following:</i> <ul>
   * <li><b>units</b> set to either mm or in (input assumed to be in mm) <li><b>format</b> set to
   * the java formatting string to apply, for example 0.00# <li><b>symbol</b> set to either letters
   * or math, for example ge or >=, respectively </ul>
   *
   * @param fcstTag    Template tag name (including params) for fcst_thresh
   * @param fcstThresh Template map value to be formatted
   * @return
   */
  public static String formatThresh(final String fcstTag, final String fcstThresh) {
    String strThreshRet = fcstThresh;
    MVOrderedMap mapParams = parseTagParams(fcstTag);
    DecimalFormat format = new DecimalFormat("0.000");

    //  attempt to parse the input threshold
    String strSymbol;
    String strThresh;
    double dblThresh;
    Matcher matFcstThresh = MVUtil.thresh.matcher(fcstThresh);
    if (matFcstThresh.matches()) {
      strSymbol = matFcstThresh.group(1);
      strThresh = matFcstThresh.group(2);
      dblThresh = Double.parseDouble(strThresh);
    } else {
      return strThreshRet;
    }

    //  change the units, if requested
    if (mapParams.containsKey("units")) {
      String strUnits = mapParams.get("units").toString();
      if (strUnits.equals("in")) {
        strThresh = format.format(dblThresh /= 25.4);
        strThreshRet = strSymbol + strThresh;
      }
    }

    //  change the format, if requested
    if (mapParams.containsKey("format")) {
      String strFormat = mapParams.get("format").toString();
      strThresh = new DecimalFormat(strFormat).format(dblThresh);
      strThreshRet = strSymbol + strThresh;
    }

    //  change the logic symbol, if requested
    if (mapParams.containsKey("symbol")) {
      String strSymbolType = mapParams.get("symbol").toString();
      if (strSymbolType.equals("letters")) {
        strSymbol = strSymbol.replace("==", "eq")
                .replace("!=", "ne")
                .replace("<=", "le")
                .replace(">=", "ge")
                .replace("<", "lt")
                .replace(">", "gt");
        strThreshRet = strSymbol + strThresh;
      }
    }

    return strThreshRet;
  }

  public static String buildTemplateString(
          final String tmpl, final MVOrderedMap vals,
          final PrintStream printStream) throws ValidationException {
    return buildTemplateString(tmpl, vals, null, printStream);
  }

  /**
   * Add the fcst_var and stat names from the dep structure of the input job to the input list of
   * tmpl map values.
   *
   * @param job MVPlotJob whose dep structure will be processed
   */
  public static MVOrderedMap addTmplValDep(MVPlotJob job) {
    MVOrderedMap mapTmplValsPlot = new MVOrderedMap(job.getTmplVal());
    if (job.getIndyVar() != null) {
      mapTmplValsPlot.put("indy_var", job.getIndyVar());
    }
    for (int intY = 1; intY <= 2; intY++) {

      //  get a list of dep groups
      MVOrderedMap[] listDepGroup = job.getDepGroups();
      MVOrderedMap mapDep = null;
      if (listDepGroup.length > 0) {
        mapDep = (MVOrderedMap) listDepGroup[0].get("dep" + intY);
      }
      if (mapDep != null) {
        Map.Entry[] listDep = mapDep.getOrderedEntries();

        //  build tmpl map values for each fcst_var
        String strDep = "dep" + intY;
        for (int i = 0; i < listDep.length; i++) {

          //  resolve the fcst_var and stats for the current dep
          String strFcstVar = listDep[i].getKey().toString();
          String[] listStat = (String[]) listDep[i].getValue();

          //  build and add the fcst_var to the tmpl value map
          String strDepFcstVar = strDep + "_" + (i + 1);
          mapTmplValsPlot.put(strDepFcstVar, strFcstVar);
          for (int j = 0; j < listStat.length; j++) {
            mapTmplValsPlot.put(strDepFcstVar + "_stat" + (j + 1), listStat[j]);
          }
        }
      }
    }
    return mapTmplValsPlot;
  }

  /**
   * Creates a string representation of an R collection containing the list of values in the input
   * list, val.
   *
   * @param val   List of values to print in the R collection
   * @param ticks (optional) Print tickmarks around values, for when constituents are factors as
   *              opposed to numeric, defaults to true
   * @return String representation of the R collection
   */
  public static String printRCol(final Object[] val, final boolean ticks) {
    StringBuilder rStr = new StringBuilder("c(");
    for (int i = 0; i < val.length; i++) {
      if (0 < i) {
        rStr.append(", ");
      }
      String value = val[i].toString().replace("&#38;", "&").replace("&gt;", ">")
              .replace("&lt;", "<");
      if (ticks) {
        rStr.append("\"").append(value).append("\"");
      } else {
        rStr.append(value);
      }
    }
    rStr.append(")");
    return rStr.toString();
  }

  /**
   * Creates a string representation of an R collection containing the list of values in the input
   * list, val.
   *
   * @param val List of values to print in the R collection
   * @return String representation of the R collection
   */
  public static String[] printYamlCol(final Object[] val) {
    List<String> result = new ArrayList<>();
    for (Object o : val) {
      String value = o.toString().replace("&#38;", "&").replace("&gt;", ">")
              .replace("&lt;", "<");
      result.add(value);
    }
    return result.toArray(new String[0]);
  }

  public static String printRCol(final Object[] val) {
    return printRCol(val, true);
  }

  /**
   * Parses an R collection string representation and returns a list of the values in the
   * collection. An R collection has syntax c(1, 2, 3) or c("a", "bb", "ccc")
   *
   * @param strRCol
   * @return list of String representations of each collection member
   */
  public static String[] parseRCol(final String strRCol) {
    if (strRCol.contains("\"")) {
      Matcher matRColStr = Pattern.compile("c\\(\\s*\"(.*)\"\\s*\\)").matcher(strRCol);
      if (!matRColStr.matches()) {
        return new String[]{};
      }
      String strList = matRColStr.group(1);
      List<String> list = new ArrayList<>();
      while (strList.matches(".*\"\\s*,\\s*\".*")) {
        list.add(strList.replaceFirst("\"\\s*,\\s*\".*", ""));
        strList = strList.replaceFirst(".*?\"\\s*,\\s*\"", "");
      }
      list.add(strList.replaceFirst("\"\\s*,\\s*\".*", ""));
      return list.toArray(new String[list.size()]);
    } else {
      Matcher matRColNum = Pattern.compile("c\\(\\s*(.*)\\s*\\)").matcher(strRCol);
      if (!matRColNum.matches()) {
        return new String[]{};
      }
      String strList = matRColNum.group(1);
      return strList.split("\\s*,\\s*");
    }
  }

  private static void populateInt(Map<String, Object> tableRTags, String tagName, String strValue, int def){
    int valInt = def;
    try {
      valInt = Integer.parseInt(strValue);
    } catch (Exception e) {
    }
    tableRTags.put(tagName, valInt);
  }

  private static void populateDouble(Map<String, Object> tableRTags, String tagName, String strValue, double def){
    double valDouble = def;
    try {
      valDouble = Double.parseDouble(strValue);
    } catch (Exception e) {
    }
    tableRTags.put(tagName, valDouble);
  }


  /**
   * Populate the input table with the plot formatting tag values stored in the input job.
   *
   * @param tableRTags template value table to receive plot formatting values
   * @param job        source for plot formatting values
   */
  public static void populatePlotFmtTmplYaml(Map<String, Object> tableRTags, MVPlotJob job) {
    populateDouble(tableRTags, "alpha", job.getCIAlpha(), 0.05);

    tableRTags.put("plot_type", job.getPlotType());
    populateDouble(tableRTags, "plot_width", job.getPlotWidth(), 11);
    populateDouble(tableRTags, "plot_height", job.getPlotHeight(), 8.5);
    populateInt(tableRTags, "plot_res", job.getPlotRes(), 72);

    tableRTags.put("plot_units", job.getPlotUnits());

    String[] valArr = job.getMar().replace("c(", "").replace(")","").split(",");
    List<Integer> vals = new ArrayList<>();
    try {
      for (String s : valArr){
        vals.add(Integer.parseInt(s));
      }
    } catch (Exception e) {
    }
    tableRTags.put("mar", vals);

    valArr = job.getMgp().replace("c(", "").replace(")","").split(",");
    vals = new ArrayList<>();
    try {
      for (String s : valArr){
        vals.add(Integer.parseInt(s));
      }
    } catch (Exception e) {
    }
    tableRTags.put("mgp", vals);

    populateInt(tableRTags, "cex", job.getCex(), 1);
    populateDouble(tableRTags, "title_weight", job.getTitleWeight(), 1.4);
    populateDouble(tableRTags, "title_size", job.getTitleSize(), 1.4);
    populateInt(tableRTags, "title_offset", job.getTitleOffset(), -2);
    populateDouble(tableRTags, "title_align", job.getTitleAlign(), 0.5);

    populateInt(tableRTags, "xtlab_orient", job.getXtlabOrient(), 1);
    populateDouble(tableRTags, "xtlab_perp", job.getXtlabPerp(), 0.75);
    populateDouble(tableRTags, "xtlab_horiz", job.getXtlabHoriz(), 0.5);
    populateInt(tableRTags, "xtlab_decim", job.getXtlabFreq(), 1);
    populateInt(tableRTags, "xtlab_size", job.getXtlabSize(), 1);

    populateInt(tableRTags, "xlab_weight", job.getXlabWeight(), 1);
    populateInt(tableRTags, "xlab_size", job.getXlabSize(), 1);
    populateInt(tableRTags, "xlab_offset", job.getXlabOffset(), 2);
    populateDouble(tableRTags, "xtlab_horiz", job.getXtlabHoriz(), 0.5);
    populateDouble(tableRTags, "xlab_align", job.getXlabAlign(), 0.5);

    populateInt(tableRTags, "ytlab_orient", job.getYtlabOrient(), 1);
    populateDouble(tableRTags, "ytlab_perp", job.getYtlabPerp(), 0.5);
    populateDouble(tableRTags, "ytlab_horiz", job.getYtlabHoriz(), 0.5);
    populateInt(tableRTags, "ytlab_size", job.getYtlabSize(), 1);
    populateInt(tableRTags, "ylab_weight", job.getYlabWeight(), 1);
    populateInt(tableRTags, "ylab_size", job.getYlabSize(), 1);
    populateInt(tableRTags, "ylab_offset", job.getYlabOffset(), -2);
    populateDouble(tableRTags, "ylab_align", job.getYlabAlign(), 0.5);

    populateInt(tableRTags, "grid_lty", job.getGridLty(), 3);
    tableRTags.put("grid_col", job.getGridCol());
    populateInt(tableRTags, "grid_lwd", job.getGridLwd(), 1);
    tableRTags.put("grid_x", job.getGridX());

    populateInt(tableRTags, "x2tlab_orient", job.getX2tlabOrient(), 1);
    populateInt(tableRTags, "x2tlab_perp", job.getX2tlabPerp(), 1);
    populateDouble(tableRTags, "x2tlab_horiz", job.getX2tlabHoriz(), 0.5);
    populateDouble(tableRTags, "x2tlab_size", job.getX2tlabSize(), 0.8);
    populateInt(tableRTags, "x2lab_weight", job.getX2labWeight(), 1);
    populateDouble(tableRTags, "x2lab_size", job.getX2labSize(), 0.8);
    populateDouble(tableRTags, "x2lab_offset", job.getX2labOffset(), -0.5);
    populateDouble(tableRTags, "x2lab_align", job.getX2labAlign(), 0.5);
    populateInt(tableRTags, "y2tlab_orient", job.getY2tlabOrient(), 1);
    populateInt(tableRTags, "y2tlab_perp", job.getY2tlabPerp(), 1);
    populateDouble(tableRTags, "y2tlab_horiz", job.getY2tlabHoriz(), 0.5);
    populateDouble(tableRTags, "y2tlab_size", job.getY2tlabSize(), 0.5);
    populateInt(tableRTags, "y2lab_weight", job.getY2labWeight(), 1);
    populateInt(tableRTags, "y2lab_size", job.getY2labSize(), 1);
    populateInt(tableRTags, "y2lab_offset", job.getY2labOffset(), 1);
    populateDouble(tableRTags, "y2lab_align", job.getY2labAlign(), 0.5);

    populateDouble(tableRTags, "legend_size", job.getLegendSize(), 0.8);
    tableRTags.put("legend_box", job.getLegendBox());
    String[] insetStr = job.getLegendInset().replace("c(", "")
            .replace(")", "").split(",");
    Map<String, Double> insetMap = new HashMap<>();
    try {
      insetMap.put("x", Double.valueOf(insetStr[0]));
      insetMap.put("y", Double.valueOf(insetStr[1]));
    } catch (Exception e) {
      insetMap = new HashMap<>();
    }
    if (!insetMap.isEmpty()) {
      tableRTags.put("legend_inset", insetMap);
    }
    populateInt(tableRTags, "legend_ncol", job.getLegendNcol(), 3);
    populateInt(tableRTags, "caption_weight", job.getCaptionWeight(), 1);

    tableRTags.put("caption_col", job.getCaptionCol());
    populateDouble(tableRTags, "caption_size", job.getCaptionSize(), 0.8);
    populateDouble(tableRTags, "caption_offset", job.getCaptionOffset(), 3);
    populateDouble(tableRTags, "caption_align", job.getCaptionAlign(), 0);

    tableRTags.put("box_pts", job.getBoxPts().equalsIgnoreCase("true") ? "True" : "False");
    tableRTags.put("box_outline", job.getBoxOutline().equalsIgnoreCase("true") ? "True" : "False");
    populateDouble(tableRTags, "box_boxwex", job.getBoxBoxwex(), 0.2);
    tableRTags.put("box_notch", job.getBoxNotch().equalsIgnoreCase("true") ? "True" : "False");
    tableRTags.put("box_avg", job.getBoxAvg().equalsIgnoreCase("true") ? "True" : "False");

  }





  /**
   * Populate the input table with the plot formatting tag values stored in the input job.
   *
   * @param tableRTags template value table to receive plot formatting values
   * @param job        source for plot formatting values
   */
  public static void populatePlotFmtTmpl(Map<String, Object> tableRTags, MVPlotJob job) {
    tableRTags.put("plot_type", job.getPlotType());
    tableRTags.put("plot_width", job.getPlotWidth());
    tableRTags.put("plot_height", job.getPlotHeight());
    tableRTags.put("plot_res", job.getPlotRes());
    tableRTags.put("plot_units", job.getPlotUnits());
    tableRTags.put("mar", job.getMar());
    tableRTags.put("mgp", job.getMgp());
    tableRTags.put("cex", job.getCex());
    tableRTags.put("title_weight", job.getTitleWeight());
    tableRTags.put("title_size", job.getTitleSize());
    tableRTags.put("title_offset", job.getTitleOffset());
    tableRTags.put("title_align", job.getTitleAlign());
    tableRTags.put("xtlab_orient", job.getXtlabOrient());
    tableRTags.put("xtlab_perp", job.getXtlabPerp());
    tableRTags.put("xtlab_horiz", job.getXtlabHoriz());
    tableRTags.put("xtlab_decim", job.getXtlabFreq());
    tableRTags.put("xtlab_size", job.getXtlabSize());
    tableRTags.put("xlab_weight", job.getXlabWeight());
    tableRTags.put("xlab_size", job.getXlabSize());
    tableRTags.put("xlab_offset", job.getXlabOffset());
    tableRTags.put("xlab_align", job.getXlabAlign());
    tableRTags.put("ytlab_orient", job.getYtlabOrient());
    tableRTags.put("ytlab_perp", job.getYtlabPerp());
    tableRTags.put("ytlab_horiz", job.getYtlabHoriz());
    tableRTags.put("ytlab_size", job.getYtlabSize());
    tableRTags.put("ylab_weight", job.getYlabWeight());
    tableRTags.put("ylab_size", job.getYlabSize());
    tableRTags.put("ylab_offset", job.getYlabOffset());
    tableRTags.put("ylab_align", job.getYlabAlign());
    tableRTags.put("grid_lty", job.getGridLty());
    tableRTags.put("grid_col", job.getGridCol());
    tableRTags.put("grid_lwd", job.getGridLwd());
    tableRTags.put("grid_x", job.getGridX());
    tableRTags.put("x2tlab_orient", job.getX2tlabOrient());
    tableRTags.put("x2tlab_perp", job.getX2tlabPerp());
    tableRTags.put("x2tlab_horiz", job.getX2tlabHoriz());
    tableRTags.put("x2tlab_size", job.getX2tlabSize());
    tableRTags.put("x2lab_weight", job.getX2labWeight());
    tableRTags.put("x2lab_size", job.getX2labSize());
    tableRTags.put("x2lab_offset", job.getX2labOffset());
    tableRTags.put("x2lab_align", job.getX2labAlign());
    tableRTags.put("y2tlab_orient", job.getY2tlabOrient());
    tableRTags.put("y2tlab_perp", job.getY2tlabPerp());
    tableRTags.put("y2tlab_horiz", job.getY2tlabHoriz());
    tableRTags.put("y2tlab_size", job.getY2tlabSize());
    tableRTags.put("y2lab_weight", job.getY2labWeight());
    tableRTags.put("y2lab_size", job.getY2labSize());
    tableRTags.put("y2lab_offset", job.getY2labOffset());
    tableRTags.put("y2lab_align", job.getY2labAlign());
    tableRTags.put("legend_size", job.getLegendSize());
    tableRTags.put("legend_box", job.getLegendBox());
    tableRTags.put("legend_inset", job.getLegendInset());
    tableRTags.put("legend_ncol", job.getLegendNcol());
    tableRTags.put("caption_weight", job.getCaptionWeight());
    tableRTags.put("caption_col", job.getCaptionCol());
    tableRTags.put("caption_size", job.getCaptionSize());
    tableRTags.put("caption_offset", job.getCaptionOffset());
    tableRTags.put("caption_align", job.getCaptionAlign());
    tableRTags.put("box_pts", job.getBoxPts());
    tableRTags.put("box_outline", job.getBoxOutline());
    tableRTags.put("box_boxwex", job.getBoxBoxwex());
    tableRTags.put("box_notch", job.getBoxNotch());
    tableRTags.put("box_avg", job.getBoxAvg());
    tableRTags.put("rely_event_hist", job.getRelyEventHist());
    tableRTags.put("ci_alpha", job.getCIAlpha());
    tableRTags.put("ensss_pts", job.getEnsSsPts());
    tableRTags.put("ensss_pts_disp", job.getEnsSsPtsDisp());
  }

  /**
   * Creates a list of length rep of copies of the input val.  Mimics the R function of the same
   * name
   *
   * @param val Value to repeat
   * @param rep Number of time to repeat
   * @return List of repeated values, with length specified by input rep
   */
  public static String[] rep(final String val, final int rep) {
    if (1 > rep) {
      return new String[]{};
    }
    String[] listRet = new String[rep];
    for (int i = 0; i < rep; i++) {
      listRet[i] = val;
    }
    return listRet;
  }

  /**
   * Creates a list of length rep of copies of the input val.  Mimics the R function of the same
   * name
   *
   * @param val Value to repeat
   * @param rep Number of time to repeat
   * @return List of repeated values, with length specified by input rep
   */
  public static Integer[] rep(final int val, final int rep) {
    if (1 > rep) {
      return new Integer[]{};
    }
    Integer[] listRet = new Integer[rep];
    for (int i = 0; i < rep; i++) {
      listRet[i] = val;
    }
    return listRet;
  }

  /**
   * Creates a list of integers where the first element is min, the second is min+1 .... the last is
   * max name
   *
   * @param min The first value
   * @param max The last number
   * @return List of  values
   */
  public static Integer[] repPlusOne(final int min, final int max) {

    Integer[] listRet = new Integer[max - min + 1];
    int start = min;
    for (int i = 0; i < (max - min + 1); i++) {
      listRet[i] = start;
      start++;
    }
    return listRet;
  }

  public static boolean isValidLineType(final String lineType) {
    boolean result = false;
    for (String type : lineTypes) {
      if (type.equalsIgnoreCase(lineType)) {
        result = true;
        break;
      }
    }
    return result;
  }

  public static boolean isNumeric(final Object obj) {
    try {
      Double.parseDouble(String.valueOf(obj));
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }


  public static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }

  public static class NameFilter implements FilenameFilter {

    private String currentName;

    NameFilter(String name) {
      this.currentName = name;
    }

    @Override
    public boolean accept(File dir, String name) {
      return name.contains(currentName) && name.endsWith("xml");
    }

  }

  public static String domSourceToString(final Document document) {
    String result = "";
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");

    DOMSource source = new DOMSource(document);
    try (StringWriter stringWriter = new StringWriter()) {
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer transformer = transformerFactory.newTransformer();
      transformer.transform(source, new StreamResult(stringWriter));
      result = stringWriter.toString();
    } catch (TransformerException | IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    return result;
  }

  public static Document createDocument() throws ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    DocumentBuilder dBuilder = dbf.newDocumentBuilder();
    return dBuilder.newDocument();
  }

  public static void safeClose(FileWriter fileWriter) {
    if (fileWriter != null) {
      try {
        fileWriter.close();
      } catch (IOException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
    }
  }

  public static String cleanString(String aString) {
    if (aString == null) {
      return null;
    }
    StringBuilder cleanString = new StringBuilder();
    for (int i = 0; i < aString.length(); ++i) {
      cleanString.append(cleanChar(aString.charAt(i)));
    }
    return cleanString.toString();
  }

  private static char cleanChar(char aChar) {

    // 0 - 9
    for (int i = 48; i < 58; ++i) {
      if (aChar == i) {
        return (char) i;
      }
    }

    // 'A' - 'Z'
    for (int i = 65; i < 91; ++i) {
      if (aChar == i) {
        return (char) i;
      }
    }

    // 'a' - 'z'
    for (int i = 97; i < 123; ++i) {
      if (aChar == i) {
        return (char) i;
      }
    }

    // other valid characters
    switch (aChar) {
      case '/':
        return '/';
      case '.':
        return '.';
      case '-':
        return '-';
      case '_':
        return '_';
      case ' ':
        return ' ';
    }
    return '%';
  }

  public static synchronized String formatPlotFormat(Date date) {
    return PLOT_FORMAT.format(date);
  }

  public static synchronized Date parsePlotFormat(String dateStr) throws ParseException {
    return PLOT_FORMAT.parse(dateStr);
  }

  public static synchronized String formatPerf(double val) {
    return DECIMAL_FORMAT.format(val);
  }

  public static String[][] getDiffSeriesArr(String diffSeriesTemplate) {
    if (diffSeriesTemplate.equals("list()")) {
      return new String[][]{};
    }
    String diffSeries = diffSeriesTemplate.replace("list(", "");
    String[] diffSeriesArr = diffSeries.split("c\\(");
    List<String[]> result = new ArrayList<>();
    for (String serie : diffSeriesArr) {
      if (!serie.isEmpty()) {
        serie = serie.replaceAll("\"", "").replaceAll("\\(", "").replaceAll("\\)", "");
        String[] args = serie.split(",");
        result.add(args);
      }
    }
    return result.toArray(new String[0][]);
  }

  public static synchronized void createYamlFile(final String fileName, final Map<String, Object> info) throws IOException {
    DumperOptions options = new DumperOptions();
    //options.setDefaultScalarStyle(DumperOptions.ScalarStyle.SINGLE_QUOTED);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    //sort by key
    Map<String, Object> sortedMap = new TreeMap<>(info);
    Yaml yaml = new Yaml(options);
    try (FileWriter writer = new FileWriter(fileName)) {
      yaml.dump(sortedMap, writer);
    }
  }


}

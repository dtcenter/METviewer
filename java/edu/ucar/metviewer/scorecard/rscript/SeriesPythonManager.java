package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.createYamlFile;


public class SeriesPythonManager {
  private static final Logger logger = LogManager.getLogger(SeriesPythonManager.class);

  private static String python;
  private static final String metPlotpyHome;
  private static final String metCalcpyHome;

  private static final String PYTHON_SCRIPT = "/metplotpy/plots/line/line.py";

  private SeriesPythonManager() {

  }


  static {
    python = System.getProperty("python.env");
    if (!python.contains("/bin/")) {
      python = python + "/bin/python";
    }
    metPlotpyHome = System.getProperty("metplotpy.home");
    metCalcpyHome = System.getProperty("metcalcpy.home");
  }

  public static void makePlots(Scorecard scorecard, Map<String, Object> yamlInfoOriginal, int rowCounter) throws IOException {

    Map<String, Object> yamlInfo = prepareConfig(yamlInfoOriginal, scorecard.columnsStructure());
    String fileName = scorecard.getPlotFile().split("\\.")[0] + "_" + rowCounter + "." + scorecard.getPlotFile().split("\\.")[1];
    String yamlName = scorecard.getPlotFile().split("\\.")[0] + "_" + rowCounter + "." + "yaml";
    yamlInfo.put("plot_filename", scorecard.getWorkingFolders().getPlotsDir() + "/" + fileName);

    List<Map<String, Entry>> listRows = scorecard.getListOfEachRowWithDesc();
    StringBuilder rowTitle = new StringBuilder("Row #" + rowCounter + " Fields:");
    for (Map.Entry<String, Entry> entry : listRows.get(rowCounter - 1).entrySet()) {
      rowTitle.append(" ").append(entry.getValue().getLabel());
    }
    yamlInfo.put("title", rowTitle.toString());

    createYamlFile(scorecard.getWorkingFolders().getDataDir() + "/" + yamlName, yamlInfo);

    logger.info(python + " " + metPlotpyHome + PYTHON_SCRIPT + " " + scorecard.getWorkingFolders().getDataDir() + "/" + yamlName);
    MvResponse mvResponse = MVUtil.runRscript(python,
            metPlotpyHome + PYTHON_SCRIPT,
            new String[]{scorecard.getWorkingFolders().getDataDir() + "/" + yamlName},
            new String[]{"PYTHONPATH=" + metPlotpyHome + ":" + metCalcpyHome,
                    "METPLOTPY_BASE=" + metPlotpyHome});
    if (mvResponse.getInfoMessage() != null) {
      logger.info(mvResponse.getInfoMessage());
    }
    if (mvResponse.getErrorMessage() != null) {
      logger.error(mvResponse.getErrorMessage());
    }
  }

  private static Map<String, Object> prepareConfig(Map<String, Object> yamlInfoOriginal, Map<String, List<Entry>> columnsStructure) {
    Map<String, Object> yamlInfo = new HashMap<>(yamlInfoOriginal);
    yamlInfo.put("derived_series_1", new ArrayList<>());
    Map.Entry<String, List<Entry>> last = null;
    for (Map.Entry<String, List<Entry>> entry : columnsStructure.entrySet()) {
      last = entry;
    }
    if (last == null) {
      yamlInfo.put("indy_var", "");
    } else {
      yamlInfo.put("indy_var", last.getKey());
    }
    List<String> values = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    if (last != null) {
      for (Entry en : last.getValue()) {
        if (!values.contains(en.getName())) {
          values.add(en.getName());
          labels.add(en.getLabel());
        }
      }
    }
    yamlInfo.put("indy_vals", values);
    yamlInfo.put("indy_label", labels);

    Map<String, List<String>> seriesList = (LinkedHashMap) yamlInfo.get("series_val_1");
    if (last != null) {
      seriesList.remove(last.getKey());
    }
    for (Map.Entry<String, List<Entry>> entry : columnsStructure.entrySet()) {
      if (last != null && !entry.getKey().equals(last.getKey())) {
        values = new ArrayList<>();
        for (Entry en : entry.getValue()) {
          if (!values.contains(en.getName())) {
            values.add(en.getName());
          }
        }
        seriesList.put(entry.getKey(), values);
      }
    }
    yamlInfo.put("series_val_1", seriesList);
    yamlInfo.put("plot_ci", new String[]{"none", "none"});
    yamlInfo.put("plot_disp", new String[]{"True", "True"});
    yamlInfo.put("series_line_style", new String[]{"-", "-"});
    yamlInfo.put("series_line_width", new Integer[]{2, 2});
    yamlInfo.put("series_order", new Integer[]{1, 2});
    yamlInfo.put("con_series", new Integer[]{1, 1});
    yamlInfo.put("series_symbols", new String[]{".", "."});
    yamlInfo.put("series_type", new String[]{"b", "b"});
    yamlInfo.put("show_signif", new String[]{"False", "False"});
    yamlInfo.put("colors", new String[]{"#ff0000", "#0000ff"});
    yamlInfo.put("plot_caption", "");
    if (last != null) {
      yamlInfo.put("xaxis", last.getKey());
    } else {
      yamlInfo.put("xaxis", "");
    }
    yamlInfo.put("yaxis_1", ((ArrayList<String>) yamlInfo.get("list_stat_1")).get(0));
    yamlInfo.put("title", "");
    yamlInfo.put("grid_col", "#cccccc");
    yamlInfo.put("event_equal", "False");
    yamlInfo.put("stat_input", yamlInfo.get("agg_stat_output"));
    yamlInfo.put("plot_ci", new String[]{"boot", "boot"});
    return yamlInfo;
  }
}

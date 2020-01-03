/**
 * AggRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.createYmlFile;

/**
 * Constructs and runs Rscript for agg stats
 *
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 15:19 $
 */
public class AggPythonManager extends PythonManager {

  private static final Logger logger = LogManager.getLogger("AggRscriptManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final String STAT_SCRIPT_FILE_NAME = "/include/agg_stat.R";
  private final String strAggInfo;
  private final String aggStatTemplScriptDir;
  private final String aggStatDataFilePath;
  private final Map<String, Object> tableAggStatInfoCommon;
  private static final String PYTHON_SCRIPT = "/metcalcpy/agg_stat.py";


  public AggPythonManager(final Scorecard scorecard) {
    super(scorecard);
    strAggInfo = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile()
            .replaceFirst("\\.data.agg_stat$",
                    ".agg_stat.info");
    aggStatTemplScriptDir = scorecard.getWorkingFolders().getrWorkDir();
    aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard
            .getAggStatDataFile();


    tableAggStatInfoCommon = new HashMap<>();
    tableAggStatInfoCommon.put("event_equal", "True");
    tableAggStatInfoCommon.put("num_iterations", scorecard.getNumBootReplicates());
    tableAggStatInfoCommon.put("alpha", 0.05);
    tableAggStatInfoCommon.put("fixed_vars_vals_input", new HashMap<>());
    tableAggStatInfoCommon.put("fcst_var_val_2", new HashMap<>());

    tableAggStatInfoCommon.put("agg_stat_output",
            scorecard.getWorkingFolders().getDataDir() + scorecard
                    .getDataFile());
    Integer seed = null;
    if (scorecard.getBootRandomSeed() != null) {
      seed = scorecard.getBootRandomSeed();
    }
    tableAggStatInfoCommon.put("random_seed", seed);
    tableAggStatInfoCommon.put("list_stat_2", new ArrayList<>());
    tableAggStatInfoCommon.put("method", "perc");
    tableAggStatInfoCommon.put("num_threads", -1);
    tableAggStatInfoCommon.put("series_val_2", new HashMap<>());
    tableAggStatInfoCommon.put("derived_series_2", new ArrayList<>());
  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    clean();
    initModels();
    if (models != null) {

      Map<String, Object> yamlInfo = new TreeMap<>(tableAggStatInfoCommon);
      init(mapRow);
      initAggBool(yamlInfo, Util.getAggTypeForStat(stat));

      //create a template
      yamlInfo.put("indy_var", indyVar);
      yamlInfo.put("indy_vals", indyList.get(indyVar));

      List<String> statList = new ArrayList<>();
      statList.add(stat);
      yamlInfo.put("list_stat_1", statList);


      Map<String, String> list_static_val = new HashMap<>();
      list_static_val.put("fcst_var", fcstVar);
      yamlInfo.put("list_static_val", list_static_val);

      yamlInfo.put("series_val_1", seriesList);




      yamlInfo.put("derived_series_1", seriesDiffList);
      Map<String, List<String>> fcst_var_val_1 = new HashMap<>();
      List<String> fcst_var_val_1_list = new ArrayList<>();
      fcst_var_val_1_list.add(stat);
      fcst_var_val_1.put(fcstVar, fcst_var_val_1_list);
      yamlInfo.put("fcst_var_val_1", fcst_var_val_1);

      //check id output file exists and its length not 0
      File output = new File((String) yamlInfo.get("agg_stat_output"));
      boolean isAppend = false;
      if (output.exists() && output.length() > 0) {
        isAppend = true;
      }
      yamlInfo.put("append_to_file", isAppend ? "True" : "False");
      int lastDot = aggStatDataFilePath.lastIndexOf('.');
      String thredFileName = aggStatDataFilePath
              .substring(0, lastDot) + threadName + aggStatDataFilePath
              .substring(lastDot);
      yamlInfo.put("agg_stat_input", thredFileName);

      lastDot = strAggInfo.lastIndexOf('.');
      String thredInfoFileName = strAggInfo.substring(0, lastDot)
              + threadName + strAggInfo.substring(lastDot);


      String aggStatTemplScript;

      aggStatTemplScript = aggStatTemplScriptDir + STAT_SCRIPT_FILE_NAME;
      try (PrintStream printStream = IoBuilder.forLogger(AggRscriptManager.class)
              .setLevel(org.apache.logging.log4j.Level.INFO)
              .buildPrintStream()) {
        createYmlFile(thredInfoFileName, yamlInfo);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + pythonEnv + " " + metCalcpyHome + PYTHON_SCRIPT + " " + thredInfoFileName);


        MvResponse mvResponse = MVUtil.runRscript(pythonEnv,
                metCalcpyHome + PYTHON_SCRIPT,
                new String[]{thredInfoFileName},
                new String[]{"PYTHONPATH=" + metCalcpyHome});
        stopWatch.stop();
        if (mvResponse.getInfoMessage() != null) {
          printStream.println(mvResponse.getInfoMessage());
        }
        if (mvResponse.getErrorMessage() != null) {
          printStream.println(mvResponse.getErrorMessage());
        }
        printStream.println("Python time " + stopWatch.getFormattedTotalDuration());
      } catch (IOException | StopWatchException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
    }
  }

  private void initAggBool(Map<String, Object> tableAggStatInfo, String stat) {

    String lineType = "N/A";
    if (stat.equals(MVUtil.CTC)) {
      lineType = "ctc";
    }
    if (stat.equals(MVUtil.NBRCTC)) {
      lineType = "nbr_ctc";
    }
    if (stat.equals(MVUtil.SL1L2)) {
      lineType = "sl1l2";
    }
    if (stat.equals(MVUtil.GRAD)) {
      lineType = "grad";
    }
    if (stat.equals(MVUtil.SAL1L2)) {
      lineType = "sal1l2";
    }
    if (stat.equals(MVUtil.VL1L2)) {
      lineType = "vl1l2";
    }
    if (stat.equals(MVUtil.VAL1L2)) {
      lineType = "val1l2";
    }
    if (stat.equals(MVUtil.PCT)) {
      lineType = "pct";
    }
    if (stat.equals(MVUtil.NBRCNT)) {
      lineType = "nbr_cnt";
    }
    if (stat.equals(MVUtil.ECNT)) {
      lineType = "ecnt";
    }
    tableAggStatInfo.put("line_type", lineType);
  }

}

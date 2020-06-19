/**
 * AggRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Constructs and runs Rscript for agg stats
 *
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 15:19 $
 */
public class AggRscriptManager extends RscriptManager {

  private static final Logger logger = LogManager.getLogger("AggRscriptManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final String STAT_SCRIPT_FILE_NAME = "/include/agg_stat.R";
  private final String strAggInfo;
  private final String aggStatTemplFilePath;
  private final String aggStatTemplScriptDir;
  private final String aggStatDataFilePath;
  private final Map<String, String> tableAggStatInfoCommon;


  public AggRscriptManager(final Scorecard scorecard) {
    super(scorecard);
    strAggInfo = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile()
            .replaceFirst("\\.data.agg_stat$",
                    ".agg_stat.info");
    aggStatTemplFilePath = scorecard.getWorkingFolders().getrTemplateDir();
    aggStatTemplScriptDir = scorecard.getWorkingFolders().getrWorkDir();
    aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard
            .getAggStatDataFile();


    tableAggStatInfoCommon = new HashMap<>();
    tableAggStatInfoCommon.put("agg_nbrcnt", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("agg_ssvar", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("event_equal", String.valueOf(Boolean.TRUE).toUpperCase());
    tableAggStatInfoCommon.put("eveq_dis", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("boot_repl", String.valueOf(scorecard.getNumBootReplicates()));
    tableAggStatInfoCommon.put("boot_ci", "perc");
    tableAggStatInfoCommon.put("ci_alpha", "0.05");
    tableAggStatInfoCommon.put("equalize_by_indep", String.valueOf(Boolean.TRUE).toUpperCase());
    tableAggStatInfoCommon.put("agg_stat2", "c()");
    tableAggStatInfoCommon.put("series2_list", "list()");
    tableAggStatInfoCommon.put("fix_val_list_eq", "list()");
    tableAggStatInfoCommon.put("fix_val_list", "list()");
    tableAggStatInfoCommon
            .put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableAggStatInfoCommon.put("series2_diff_list", "list()");
    tableAggStatInfoCommon.put("dep2_plot", "c()");
    tableAggStatInfoCommon.put("cl_step", "0.05");

    tableAggStatInfoCommon.put("agg_stat_output",
            scorecard.getWorkingFolders().getDataDir() + scorecard
                    .getDataFile());
    String seed = "NA";
    if (scorecard.getBootRandomSeed() != null) {
      seed = String.valueOf(scorecard.getBootRandomSeed());
    }
    tableAggStatInfoCommon.put("boot_random_seed", seed);

  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    clean();
    initModels();
    if (models != null) {

      Map<String, Object> tableAggStatInfo = new HashMap<>(tableAggStatInfoCommon);
      init(mapRow);
      initAggBool(tableAggStatInfo, Util.getAggTypeForStat(stat));

      //create a template
      tableAggStatInfo.put("indy_var", indyVar);
      tableAggStatInfo.put("indy_list", "c(" + indyList + ")");

      tableAggStatInfo.put("agg_stat1", "c(\"" + stat + "\")");
      tableAggStatInfo.put("contour_diff", "FALSE");


      tableAggStatInfo.put("series1_list", seriesList.toString());


      tableAggStatInfo.put("agg_stat_static", "list(`fcst_var` = \"" + fcstVar + "\")");

      tableAggStatInfo.put("series1_diff_list", seriesDiffList.toString());
      tableAggStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");

      //check id output file exists and its length not 0
      File output = new File((String) tableAggStatInfo.get("agg_stat_output"));
      boolean isAppend = false;
      if (output.exists() && output.length() > 0) {
        isAppend = true;
      }
      tableAggStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      int lastDot = aggStatDataFilePath.lastIndexOf('.');
      String thredFileName = aggStatDataFilePath
              .substring(0, lastDot) + threadName + aggStatDataFilePath
              .substring(lastDot);
      tableAggStatInfo.put("agg_stat_input", thredFileName);

      lastDot = strAggInfo.lastIndexOf('.');
      String thredInfoFileName = strAggInfo.substring(0, lastDot) + threadName + strAggInfo
              .substring(
                      lastDot);

      try (PrintStream printStream = IoBuilder.forLogger(AggRscriptManager.class)
              .setLevel(org.apache.logging.log4j.Level.INFO)
              .buildPrintStream()) {

        String aggStatTemplScript;
        String aggStatTemplFile;

        aggStatTemplScript = aggStatTemplScriptDir + STAT_SCRIPT_FILE_NAME;
        aggStatTemplFile = aggStatTemplFilePath + "/agg_stat.info_tmpl";

        MVUtil.populateTemplateFile(aggStatTemplFile, thredInfoFileName, tableAggStatInfo);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + rScriptCommand + " " + aggStatTemplScript);


        MvResponse mvResponse = MVUtil.runRscript(rScriptCommand, aggStatTemplScript,
                new String[]{thredInfoFileName});
        stopWatch.stop();
        if (mvResponse.getInfoMessage() != null) {
          printStream.println(mvResponse.getInfoMessage());
        }
        if (mvResponse.getErrorMessage() != null) {
          printStream.println(mvResponse.getErrorMessage());
        }
        printStream.println("Rscript time " + stopWatch.getFormattedTotalDuration());
      } catch (IOException | StopWatchException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
    }
  }

  private void initAggBool(Map<String, Object> tableAggStatInfo, String stat) {
    if (stat.equals(MVUtil.CTC)) {
      tableAggStatInfo.put("agg_ctc", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_ctc", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.NBRCTC)) {
      tableAggStatInfo.put("agg_nbrctc", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_nbrctc", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.SL1L2)) {
      tableAggStatInfo.put("agg_sl1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_sl1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.GRAD)) {
      tableAggStatInfo.put("agg_grad", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_grad", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.SAL1L2)) {
      tableAggStatInfo.put("agg_sal1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_sal1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.VL1L2)) {
      tableAggStatInfo.put("agg_vl1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_vl1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.VAL1L2)) {
      tableAggStatInfo.put("agg_val1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_val1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.PCT)) {
      tableAggStatInfo.put("agg_pct", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_pct", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.NBRCNT)) {
      tableAggStatInfo.put("agg_nbrcnt", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_nbrcnt", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.ECNT)) {
      tableAggStatInfo.put("agg_ecnt", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_ecnt", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.RPS)) {
      tableAggStatInfo.put("agg_rps", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_rps", String.valueOf(Boolean.FALSE).toUpperCase());
    }

  }

}

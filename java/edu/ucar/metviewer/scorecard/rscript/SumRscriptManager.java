/**
 * SumRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.RscriptResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:40 $
 */
public class SumRscriptManager extends RscriptManager {

  private static final Logger logger = LogManager.getLogger("SumRscriptManager");
  private static final String SCRIPT_FILE_NAME = "/scorecard.R_tmpl";
  private static final String SUM_FILE_NAME = "/sum_stat.info_tmpl";
  private static final String STAT_SCRIPT_FILE_NAME = "/include/sum_stat.R";
  private final Map<String, String> tableCalcStatInfoCommon;
  private final String calcStatTemplScript;
  private final String sumStatTemplScript;
  private final String strRFile;
  private final String strSumRFile;
  private final String strSumInfo;
  private final String sumStatTemplFilePath;
  private final String sumStatTemplScriptDir;
  private final String sumStatDataFilePath;


  public SumRscriptManager(Scorecard scorecard) {
    super(scorecard);
    calcStatTemplScript = scorecard.getWorkingFolders().getrTemplateDir() + SCRIPT_FILE_NAME;
    sumStatTemplScript = scorecard.getWorkingFolders().getrTemplateDir() + SUM_FILE_NAME;
    strRFile = scorecard.getWorkingFolders().getScriptsDir()
                   + scorecard.getDataFile().replaceFirst("\\.data$", ".R");
    strSumRFile = scorecard.getWorkingFolders().getrWorkDir() + "sum_stat.R";
    strSumInfo = scorecard.getWorkingFolders().getDataDir()
                     + scorecard.getSumStatDataFile().replaceFirst("\\.data.sum_stat$",
                                                                   ".sum_stat.info");
    sumStatTemplFilePath = scorecard.getWorkingFolders().getrTemplateDir();
    sumStatTemplScriptDir = scorecard.getWorkingFolders().getrWorkDir();
    sumStatDataFilePath = scorecard.getWorkingFolders().getDataDir()
                              + scorecard.getSumStatDataFile();


    tableCalcStatInfoCommon = new HashMap<>();
    if (scorecard.getStatFlag().equals("EMC")) {
      tableCalcStatInfoCommon.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());
    } else {
      tableCalcStatInfoCommon.put("event_equal", String.valueOf(Boolean.TRUE).toUpperCase());
    }
    tableCalcStatInfoCommon.put("ci_alpha", "0.05");
    tableCalcStatInfoCommon.put("equalize_by_indep", String.valueOf(Boolean.FALSE).toUpperCase());
    tableCalcStatInfoCommon.put("series2_list", "list()");
    tableCalcStatInfoCommon.put("fix_val_list_eq", "list()");
    tableCalcStatInfoCommon.put("dep1_scale", "list()");
    tableCalcStatInfoCommon.put("indy_plot_val", "list()");
    tableCalcStatInfoCommon.put("plot_stat", scorecard.getPlotStat());
    tableCalcStatInfoCommon
        .put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableCalcStatInfoCommon.put("data_file", scorecard.getWorkingFolders().getDataDir()
                                                 + scorecard.getDataFile()
                                                       .replaceAll(".data", ".dataFromDb"));
    tableCalcStatInfoCommon
        .put("plot_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    tableCalcStatInfoCommon.put("r_work", scorecard.getWorkingFolders().getrWorkDir());
    tableCalcStatInfoCommon.put("stat_flag", scorecard.getStatFlag());


    tableCalcStatInfoCommon.put("eveq_dis", String.valueOf(Boolean.FALSE).toUpperCase());
    tableCalcStatInfoCommon.put("sum_stat_output", scorecard.getWorkingFolders().getDataDir()
                                                       + scorecard.getDataFile() + "1");
    String dates = "c()";
    for (Field fixedField : fixedVars) {
      if ("fcst_valid_beg".equals(fixedField.getName())
              || "fcst_init_beg".equals(fixedField.getName())) {

        dates = "c('"
                    + fixedField.getValues().get(0).getName().split("\\s")[0]
                    + "', '" + fixedField.getValues().get(1).getName().split("\\s")[0]
                    + "')";
        break;
      }
    }
    tableCalcStatInfoCommon.put("dates_list", dates);

  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    clean();
    initModels();
    if (models != null) {
      Map<String, String> tableCalcStatInfo = new HashMap<>(tableCalcStatInfoCommon);
      init(mapRow);
      tableCalcStatInfo.put("indy_var", indyVar);
      tableCalcStatInfo.put("indy_list", "c(" + indyList + ")");
      tableCalcStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");
      tableCalcStatInfo.put("dep2_plot", "list()");
      tableCalcStatInfo.put("series_list", seriesList.toString());
      tableCalcStatInfo.put("series1_list", seriesList.toString());
      tableCalcStatInfo.put("series2_list", "list()");
      tableCalcStatInfo.put("series_diff_list", seriesDiffList.toString());
      tableCalcStatInfo.put("sum_stat_static", "list(`fcst_var` = \"" + fcstVar + "\")");

      String aggType = Util.getAggTypeForStat(Util.getStatForRow(mapRow));
      tableCalcStatInfo
          .put("sum_ctc", String.valueOf(Boolean.valueOf(aggType.equals("ctc"))).toUpperCase());
      tableCalcStatInfo.put("sum_sl1l2", String.valueOf(Boolean.valueOf(aggType.equals("sl1l2")))
                                             .toUpperCase());
      tableCalcStatInfo.put("sum_grad", String.valueOf(Boolean.valueOf(aggType.equals("grad")))
                                            .toUpperCase());
      tableCalcStatInfo.put("sum_sal1l2", String.valueOf(Boolean.valueOf(aggType.equals("sal1l2")
      )).toUpperCase());
      tableCalcStatInfo.put("sum_vl1l2", String.valueOf(Boolean.valueOf(aggType.equals("vl1l2")))
                                             .toUpperCase());
      tableCalcStatInfo.put("sum_val1l2", String.valueOf(Boolean.valueOf(aggType.equals("val1l2")))
                                              .toUpperCase());

      //check id output file exists and its length not 0
      File output = new File(tableCalcStatInfo.get("sum_stat_output"));
      boolean isAppend = false;
      // if (output.exists() && output.length() > 0) {
      //   isAppend = true;
      // }
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      int lastDot = sumStatDataFilePath.lastIndexOf('.');
      String thredFileName = sumStatDataFilePath.substring(0, lastDot) + threadName +
                                 sumStatDataFilePath.substring(lastDot);
      tableCalcStatInfo.put("sum_stat_input", tableCalcStatInfoCommon.get("data_file"));
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      String thredInfoFileName = strSumInfo.substring(0, lastDot)
                                     + threadName + strSumInfo.substring(lastDot);

      try (PrintStream printStream = IoBuilder.forLogger(SumRscriptManager.class)
                                         .setLevel(org.apache.logging.log4j.Level.INFO)
                                         .buildPrintStream()) {
        String sumStatTemplScript;
        String sumStatTemplFile;

        sumStatTemplScript = sumStatTemplScriptDir + STAT_SCRIPT_FILE_NAME;
        sumStatTemplFile = sumStatTemplFilePath + "/sum_stat.info_tmpl";
        MVUtil.populateTemplateFile(sumStatTemplFile, thredInfoFileName, tableCalcStatInfo);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + rScriptCommand + " " + sumStatTemplScript );


        RscriptResponse rscriptResponse = MVUtil.runRscript(rScriptCommand, sumStatTemplScript,
                                                            new String[]{thredInfoFileName});

        stopWatch.stop();
        if (rscriptResponse.getInfoMessage() != null) {
          printStream.println(rscriptResponse.getInfoMessage());
        }
        if (rscriptResponse.getErrorMessage() != null) {
          printStream.println(rscriptResponse.getErrorMessage());
        }
        printStream.println("Rscript time " + stopWatch.getFormattedTotalDuration());
      } catch (Exception e) {
        logger.error(e);
        logger.error(e);
      }
      tableCalcStatInfo.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());


      //check if output file exists and its length is not 0
      output = new File(tableCalcStatInfo.get("plot_file"));
      isAppend = false;
      if (output.exists() && output.length() > 0) {
        isAppend = true;
      }
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      tableCalcStatInfo.put("data_file",
                            tableCalcStatInfoCommon.get("sum_stat_output"));


      try (PrintStream printStream = IoBuilder.forLogger(SumRscriptManager.class)
                                         .setLevel(org.apache.logging.log4j.Level.INFO)
                                         .buildPrintStream()) {
        MVUtil.populateTemplateFile(calcStatTemplScript, strRFile, tableCalcStatInfo);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + rScriptCommand + " " + strRFile);


        RscriptResponse rscriptResponse = MVUtil.runRscript(rScriptCommand, strRFile);
        stopWatch.stop();
        if (rscriptResponse.getInfoMessage() != null) {
          printStream.println(rscriptResponse.getInfoMessage());
        }
        if (rscriptResponse.getErrorMessage() != null) {
          printStream.println(rscriptResponse.getErrorMessage());
        }
        printStream.println("Rscript time " + stopWatch.getFormattedTotalDuration());
      } catch (Exception e) {
        logger.error(e);
        logger.error(e);
      }

    }
  }
}

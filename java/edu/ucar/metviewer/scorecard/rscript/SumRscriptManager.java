/**
 * SumRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:40 $
 */
public class SumRscriptManager extends RscriptManager {

  private static final Logger logger = LogManager.getLogger("SumRscriptManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

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
    tableCalcStatInfoCommon.put("equalize_by_indep", String.valueOf(Boolean.TRUE).toUpperCase());
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
    long ndays = 0;
    for (Field fixedField : fixedVars) {
      if ("fcst_valid_beg".equals(fixedField.getName())
              || "fcst_init_beg".equals(fixedField.getName())) {
        int fixedFieldValsSize = fixedField.getValues().size();
        boolean isSizeEven = fixedFieldValsSize % 2 == 0;
        if (!isSizeEven) {
          fixedFieldValsSize = fixedFieldValsSize - 1;
          ndays = 1;
        }
        for (int i = 0; i < fixedFieldValsSize; i = i + 2) {
          LocalDate localDate1 = LocalDate.parse(fixedField.getValues().get(i).getName().split("\\s")[0]);
          LocalDate localDate2 = LocalDate.parse(fixedField.getValues().get(i + 1).getName().split("\\s")[0]);
          ndays = ndays + DAYS.between(localDate1, localDate2) + 1;
        }


        break;
      }
    }
    tableCalcStatInfoCommon.put("ndays", String.valueOf(ndays));

  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    List<Entry> allModels = null;
    for (Field fixedField : fixedVars) {
      if ("model".equals(fixedField.getName())) {
        allModels = fixedField.getValues();
        break;
      }
    }
    if (allModels != null) {
      for (int i = 0; i < allModels.size(); i = i + 2) {
        clean();
        models = new ArrayList<>(2);
        models.add(allModels.get(i));
        if (i + 1 < allModels.size()) {
          models.add(allModels.get(i + 1));
        }
        Map<String, Object> tableCalcStatInfo = new HashMap<>(tableCalcStatInfoCommon);
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

        tableCalcStatInfo.put("fix_val_list_eq", listFixedValEx.toString());

        boolean isAppend = false;

        tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
        int lastDot = sumStatDataFilePath.lastIndexOf('.');

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
          printStream.println("Running " + rScriptCommand + " " + sumStatTemplScript);


          MvResponse mvResponse = MVUtil.runRscript(rScriptCommand, sumStatTemplScript,
                  new String[]{thredInfoFileName});

          stopWatch.stop();
          if (mvResponse.getInfoMessage() != null) {
            printStream.println(mvResponse.getInfoMessage());
          }
          if (mvResponse.getErrorMessage() != null) {
            printStream.println(mvResponse.getErrorMessage());
          }
          printStream.println("Rscript time " + stopWatch.getFormattedTotalDuration());
        } catch (StopWatchException | IOException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
        tableCalcStatInfo.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());


        //check if output file exists and its length is not 0
        File output = new File((String) tableCalcStatInfo.get("plot_file"));
        isAppend = output.exists() && output.length() > 0;
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


          MvResponse mvResponse = MVUtil.runRscript(rScriptCommand, strRFile);
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
  }
}

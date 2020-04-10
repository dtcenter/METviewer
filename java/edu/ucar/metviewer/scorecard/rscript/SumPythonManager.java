package edu.ucar.metviewer.scorecard.rscript;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.createYmlFile;
import static java.time.temporal.ChronoUnit.DAYS;

public class SumPythonManager extends PythonManager{
  private static final Logger logger = LogManager.getLogger("SumPythonManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final String SCRIPT_FILE_NAME = "/scorecard.R_tmpl";
  private static final String SUM_FILE_NAME = "/sum_stat.info_tmpl";
  private static final String PYTHON_SCRIPT = "/metcalcpy/sum_stat.py";
  private final Map<String, Object> tableCalcStatInfoCommon;
  private final String calcStatTemplScript;
  private final String sumStatTemplScript;
  private final String strRFile;
  private final String strSumRFile;
  private final String strSumInfo;
  private final String sumStatTemplFilePath;
  private final String sumStatTemplScriptDir;
  private final String sumStatDataFilePath;


  public SumPythonManager(Scorecard scorecard) {
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
      tableCalcStatInfoCommon.put("event_equal", "False");
    } else {
      tableCalcStatInfoCommon.put("event_equal", "True");
    }
    tableCalcStatInfoCommon.put("alpha", 0.05);
    tableCalcStatInfoCommon.put("equalize_by_indep", "True");
    tableCalcStatInfoCommon.put("series2_list", "list()");
    tableCalcStatInfoCommon.put("fix_val_list_eq", new String[0]);
    tableCalcStatInfoCommon.put("dep1_scale", new String[0]);
    tableCalcStatInfoCommon.put("indy_plot_val", new String[0]);
    tableCalcStatInfoCommon.put("plot_stat", scorecard.getPlotStat());
    tableCalcStatInfoCommon
            .put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableCalcStatInfoCommon.put("data_file", scorecard.getWorkingFolders().getDataDir()
            + scorecard.getDataFile()
            .replaceAll(".data", ".dataFromDb"));
    tableCalcStatInfoCommon
            .put("sum_stat_output", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    tableCalcStatInfoCommon.put("r_work", scorecard.getWorkingFolders().getrWorkDir());
    tableCalcStatInfoCommon.put("stat_flag", scorecard.getStatFlag());
    tableCalcStatInfoCommon
            .put("plot_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());

    tableCalcStatInfoCommon.put("eveq_dis", "False");
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
    tableCalcStatInfoCommon.put("ndays", ndays);

  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    clean();
    initModels();
    if (models != null) {
      Map<String, Object> yamlInfo = new TreeMap<>(tableCalcStatInfoCommon);
      Map<String, Object> tableCalcStatInfo = new HashMap<>(tableCalcStatInfoCommon);
      init(mapRow);
      initSumBool(yamlInfo, Util.getAggTypeForStat(stat));
      tableCalcStatInfo.put("indy_var", indyVar);
      yamlInfo.put("indy_var", indyVar);
      tableCalcStatInfo.put("indy_list", "c(" + indyList + ")");
      yamlInfo.put("indy_var", indyVar);
      yamlInfo.put("indy_vals", indyList.get(indyVar));
      tableCalcStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");

      Map<String, List<String>> fcst_var_val_1 = new HashMap<>();
      List<String> fcst_var_val_1_list = new ArrayList<>();
      fcst_var_val_1_list.add(stat);
      fcst_var_val_1.put(fcstVar, fcst_var_val_1_list);
      yamlInfo.put("fcst_var_val_1", fcst_var_val_1);

      tableCalcStatInfo.put("dep2_plot", "list()");
      yamlInfo.put("fcst_var_val_2", new HashMap<>());

      tableCalcStatInfo.put("series_list", seriesList.toString());
      tableCalcStatInfo.put("series1_list", seriesList.toString());


      yamlInfo.put("series_val_1", seriesList);

      tableCalcStatInfo.put("series2_list", "list()");
      yamlInfo.put("series_val_2", new HashMap<>());
      tableCalcStatInfo.put("series_diff_list", seriesDiffList.toString());
      tableCalcStatInfo.put("sum_stat_static", "list(`fcst_var` = \"" + fcstVar + "\")");
      yamlInfo.put("sum_stat_static", fcstVar );

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


      boolean isAppend = false;

      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      yamlInfo.put("append_to_file", "False");
      int lastDot = sumStatDataFilePath.lastIndexOf('.');

      tableCalcStatInfo.put("sum_stat_input", tableCalcStatInfoCommon.get("data_file"));
      yamlInfo.put("sum_stat_input", tableCalcStatInfoCommon.get("data_file"));
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      yamlInfo.put("append_to_file", "False");
      String thredInfoFileName = strSumInfo.substring(0, lastDot)
              + threadName + strSumInfo.substring(lastDot);

      try (PrintStream printStream = IoBuilder.forLogger(SumRscriptManager.class)
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
        printStream.println("Rscript time " + stopWatch.getFormattedTotalDuration());
      } catch (StopWatchException | IOException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
      tableCalcStatInfo.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());
      yamlInfo.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());


      //check if output file exists and its length is not 0
      File output = new File((String) tableCalcStatInfo.get("plot_file"));
      isAppend = output.exists() && output.length() > 0;
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      yamlInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      tableCalcStatInfo.put("data_file",
              tableCalcStatInfoCommon.get("sum_stat_output"));
      yamlInfo.put("data_file",
              tableCalcStatInfoCommon.get("sum_stat_output"));

      tableCalcStatInfo.put("indy_plot_val", "list()");
      tableCalcStatInfo.put("fix_val_list_eq", "list()");
      tableCalcStatInfo.put("dep1_scale", "list()");
      tableCalcStatInfo.put("ndays", String.valueOf(tableCalcStatInfo.get("ndays")));
      tableCalcStatInfo.put("equalize_by_indep", "TRUE");
      StringBuilder strForList = new StringBuilder();
      for( Map.Entry<String, List<String>> entry: indyList.entrySet()){
        strForList.append("`").append(entry.getKey()).append("` = c(");
        for (String val : entry.getValue()) {
          if (strForList.indexOf(val) == -1) {
            strForList.append("\"").append(val).append("\",");
          }
        }
        if (strForList.length() > 0) {
          strForList.deleteCharAt(strForList.length() - 1);
        }
        strForList.append(")");
      }
      tableCalcStatInfo.put("indy_list", "c(" + strForList + ")");
      strForList = new StringBuilder();
      for( Map.Entry<String, List<String>> entry: seriesList.entrySet()){
        strForList.append("`").append(entry.getKey()).append("` = c(");
        for (String val : entry.getValue()) {
          if (strForList.indexOf(val) == -1) {
            strForList.append("\"").append(val).append("\",");
          }
        }
        if (strForList.length() > 0) {
          strForList.deleteCharAt(strForList.length() - 1);
        }
        strForList.append("),");
      }
      if (strForList.length() > 0) {
        strForList.deleteCharAt(strForList.length() - 1);
      }
      tableCalcStatInfo.put("series_list", "list(" + strForList + ")");

      strForList = new StringBuilder();
      for (List<String> diff : seriesDiffList) {
        StringBuilder diffSeries = new StringBuilder("c(");
        for(String var :diff){
          diffSeries.append("\"").append(var).append("\",");
        }
        if (diffSeries.length() > 0) {
          diffSeries.deleteCharAt(diffSeries.length() - 1);
        }
        strForList.append(diffSeries).append("),");
      }
      if (strForList.length() > 0) {
        strForList.deleteCharAt(strForList.length() - 1);
      }
      tableCalcStatInfo.put("series_diff_list", "list(" + strForList + ")");

      try (PrintStream printStream = IoBuilder.forLogger(SumPythonManager.class)
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

  private void initSumBool(Map<String, Object> tableCalcStatInfo, String stat) {
    String lineType = "N/A";
    if (stat.equals(MVUtil.CTC)) {
      lineType = "ctc";
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
    tableCalcStatInfo.put("line_type", lineType);

  }
}

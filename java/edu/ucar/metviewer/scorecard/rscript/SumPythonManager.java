package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.exceptions.NotSupportedException;
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

public class SumPythonManager extends PythonManager {
  private static final Logger logger = LogManager.getLogger("SumPythonManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final String SCRIPT_FILE_NAME = "/scorecard.R_tmpl";
  private static final String PYTHON_SCRIPT = "/metcalcpy/sum_stat.py";
  private final Map<String, Object> yamlInfo;
  private final Map<String, Object> tableCalcStatInfo;
  private final String calcStatTemplScript;
  private final String rScriptFileName;
  private final String sumInfoFileName;


  public SumPythonManager(Scorecard scorecard) {
    super(scorecard);
    calcStatTemplScript = scorecard.getWorkingFolders().getrTemplateDir() + SCRIPT_FILE_NAME;
    rScriptFileName = scorecard.getWorkingFolders().getScriptsDir()
            + scorecard.getDataFile().replaceFirst("\\.data$", ".R");
    sumInfoFileName = scorecard.getWorkingFolders().getDataDir()
            + scorecard.getSumStatDataFile().replaceFirst("\\.data.sum_stat$",
            ".sum_stat.info");


    String sumStatOutput = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile() + "1";

    yamlInfo = new HashMap<>();
    String isEe = "True";
    if (scorecard.getStatFlag().equals("EMC")) {
      isEe = "False";
    }
    yamlInfo.put("event_equal", isEe);
    yamlInfo.put("sum_stat_input", scorecard.getWorkingFolders().getDataDir()
            + scorecard.getDataFile()
            .replaceAll(".data", ".dataFromDb"));

    yamlInfo.put("sum_stat_output", sumStatOutput);
    tableCalcStatInfo = new HashMap<>();
    tableCalcStatInfo.put("plot_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    tableCalcStatInfo.put("plot_stat", scorecard.getPlotStat());
    tableCalcStatInfo.put("r_work", scorecard.getWorkingFolders().getrWorkDir());
    tableCalcStatInfo.put("stat_flag", scorecard.getStatFlag());
    tableCalcStatInfo.put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableCalcStatInfo.put("data_file", sumStatOutput);
  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) throws NotSupportedException {
    clean();
    initModels();
    if (models != null) {
      init(mapRow);

      String lineType = getLineType(Util.getAggTypeForStat(stat));
      if (lineType.equals("N/A")){
        throw new NotSupportedException(stat, "sum_stat");
      }
      yamlInfo.put("line_type", lineType);
      yamlInfo.put("indy_var", indyVar);
      yamlInfo.put("indy_vals", indyList.get(indyVar));


      Map<String, List<String>> fcstVarVal1 = new HashMap<>();
      List<String> fcstVarVal1List = new ArrayList<>();
      fcstVarVal1List.add(stat);
      fcstVarVal1.put(fcstVar, fcstVarVal1List);
      yamlInfo.put("fcst_var_val_1", fcstVarVal1);
      yamlInfo.put("fcst_var_val_2", new HashMap<>());
      yamlInfo.put("series_val_1", seriesList);
      yamlInfo.put("series_val_2", new HashMap<>());
      yamlInfo.put("append_to_file", "False");
      yamlInfo.put("derived_series_1", seriesDiffList);


      try (PrintStream printStream = IoBuilder.forLogger(SumRscriptManager.class)
              .setLevel(org.apache.logging.log4j.Level.INFO)
              .buildPrintStream()) {

        createYmlFile(sumInfoFileName, yamlInfo);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + python + " " + metCalcpyHome + PYTHON_SCRIPT + " " + sumInfoFileName);


        MvResponse mvResponse = MVUtil.runRscript(python,
                metCalcpyHome + PYTHON_SCRIPT,
                new String[]{sumInfoFileName},
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

      //done with summary aggregation - start with scorecard

      tableCalcStatInfo.put("indy_var", indyVar);
      tableCalcStatInfo.put("indy_list", "c(" + indyList + ")");
      tableCalcStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");
      tableCalcStatInfo.put("dep2_plot", "list()");

      tableCalcStatInfo.put("series_list", seriesList.toString());
      tableCalcStatInfo.put("series1_list", seriesList.toString());

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
      boolean isAppend = false;
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());
      tableCalcStatInfo.put("event_equal", String.valueOf(Boolean.FALSE).toUpperCase());


      //check if output file exists and its length is not 0
      File output = new File((String) tableCalcStatInfo.get("plot_file"));
      isAppend = output.exists() && output.length() > 0;
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());


      tableCalcStatInfo.put("indy_plot_val", "list()");
      tableCalcStatInfo.put("fix_val_list_eq", "list()");

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
      tableCalcStatInfo.put("ndays", String.valueOf(ndays));
      tableCalcStatInfo.put("equalize_by_indep", "TRUE");
      StringBuilder strForList = new StringBuilder();
      for (Map.Entry<String, List<String>> entry : indyList.entrySet()) {
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
      for (Map.Entry<String, List<String>> entry : seriesList.entrySet()) {
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
        for (String var : diff) {
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
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());

      try (PrintStream printStream = IoBuilder.forLogger(SumPythonManager.class)
              .setLevel(org.apache.logging.log4j.Level.INFO)
              .buildPrintStream()) {
        MVUtil.populateTemplateFile(calcStatTemplScript, rScriptFileName, tableCalcStatInfo);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        printStream.println("Running " + rScriptCommand + " " + rScriptFileName);


        MvResponse mvResponse = MVUtil.runRscript(rScriptCommand, rScriptFileName);
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

  private String getLineType(String stat) {
    String lineType = "N/A";
    if (stat.equals(MVUtil.CTC)) {
      lineType = MVUtil.CTC;
    }
    if (stat.equals(MVUtil.SL1L2)) {
      lineType = MVUtil.SL1L2;
    }
    if (stat.equals(MVUtil.GRAD)) {
      lineType = MVUtil.GRAD;
    }
    if (stat.equals(MVUtil.SAL1L2)) {
      lineType = MVUtil.SAL1L2;
    }
    if (stat.equals(MVUtil.VL1L2)) {
      lineType = MVUtil.VL1L2;
    }
    if (stat.equals(MVUtil.VAL1L2)) {
      lineType = MVUtil.VAL1L2;
    }
    return lineType;
  }
}

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


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.createYamlFile;
import static java.time.temporal.ChronoUnit.DAYS;

public class SumPythonManager extends PythonManager {
  private static final Logger logger = LogManager.getLogger(SumPythonManager.class);

  private static final String PYTHON_SCRIPT = "/metcalcpy/sum_stat.py";
  private final Map<String, Object> yamlInfo;
  private final String pythonFileInfo;
  private final String sumInfoFileName;
  String sumStatInput;
  String sumStatOutput;
  String scorecardInput;
  String scorecardOutput;


  public SumPythonManager(Scorecard scorecard) {
    super(scorecard);
    pythonFileInfo = scorecard.getWorkingFolders().getScriptsDir()
            + scorecard.getDataFile().replaceFirst("\\.data$", ".yaml");
    sumInfoFileName = scorecard.getWorkingFolders().getDataDir()
            + scorecard.getSumStatDataFile().replaceFirst("\\.data.sum_stat$",
            ".sum_stat.info");




    yamlInfo = new HashMap<>();
    String isEe = "True";
    if (scorecard.getStatFlag().equals("EMC")) {
      isEe = "False";
    }
    yamlInfo.put("event_equal", isEe);
    sumStatInput = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile()
            .replaceAll(".data", ".dataFromDb");

    sumStatOutput = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile() + "1";

    yamlInfo.put("stat_flag", scorecard.getStatFlag());
    scorecardInput = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile() + "1";
    scorecardOutput = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile();
    File old_output = new File(scorecardOutput);
    if (old_output.exists()){
      old_output.delete();
    }
  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) throws NotSupportedException {
    //delete old file
    File old_output = new File(sumStatOutput);
    if (old_output.exists()){
      old_output.delete();
    }


    yamlInfo.put("sum_stat_output", sumStatOutput);
    yamlInfo.put("sum_stat_input", sumStatInput);
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
        init(mapRow);

        String lineType = getLineType(Util.getAggTypeForStat(stat));
        if (lineType.equals("N/A")) {
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
        yamlInfo.put("fixed_vars_vals_input", listFixedValEx);


        try  {

          createYamlFile(sumInfoFileName, yamlInfo);

          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          logger.info("Running " + python + " " + metCalcpyHome + PYTHON_SCRIPT + " " + sumInfoFileName);


          MvResponse mvResponse = MVUtil.runRscript(python,
                  metCalcpyHome + PYTHON_SCRIPT,
                  new String[]{sumInfoFileName},
                  new String[]{"PYTHONPATH=" + metCalcpyHome});

          stopWatch.stop();
          if (mvResponse.getInfoMessage() != null) {
            logger.info(mvResponse.getInfoMessage());
          }
          if (mvResponse.getErrorMessage() != null) {
            logger.error(mvResponse.getErrorMessage());
          }
          logger.info("Python time " + stopWatch.getFormattedTotalDuration());
        } catch (StopWatchException | IOException e) {
          logger.error( e.getMessage());
        }

        //done with summary aggregation - start with scorecard

        File output = new File(scorecardOutput);
        boolean isAppend = output.exists() && output.length() > 0;
        yamlInfo.put("append_to_file", isAppend ? "True" : "False");
        yamlInfo.put("event_equal", "False");

        List<String> statList = new ArrayList<>();
        statList.add(stat);
        yamlInfo.put("list_stat_1", statList);




        yamlInfo.put("indy_plot_val", new ArrayList<>());
        yamlInfo.put("fix_val_list_eq", new ArrayList<>());

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
            for (int k = 0; k < fixedFieldValsSize; k = k + 2) {
              LocalDate localDate1 = LocalDate.parse(fixedField.getValues().get(k).getName().split("\\s")[0]);
              LocalDate localDate2 = LocalDate.parse(fixedField.getValues().get(k + 1).getName().split("\\s")[0]);
              ndays = ndays + DAYS.between(localDate1, localDate2) + 1;
            }


            break;
          }
        }
        yamlInfo.put("ndays", ndays);
        yamlInfo.put("equalize_by_indep", "True");
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

        strForList = new StringBuilder();
        for (List<String> diff : seriesDiffList) {
          StringBuilder diffSeries = new StringBuilder("c(");
          for (String diff_component : diff) {
            diffSeries.append("\"").append(diff_component).append("\",");
          }
          if (diffSeries.length() > 0) {
            diffSeries.deleteCharAt(diffSeries.length() - 1);
          }
          strForList.append(diffSeries).append("),");
        }
        if (strForList.length() > 0) {
          strForList.deleteCharAt(strForList.length() - 1);
        }

        yamlInfo.put("sum_stat_input", scorecardInput);
        yamlInfo.put("sum_stat_output", scorecardOutput);

        try {

          createYamlFile(pythonFileInfo, yamlInfo);

          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          logger.info("Running " + python + " " + metCalcpyHome + "/metcalcpy/scorecard.py" + " " + pythonFileInfo);


          MvResponse mvResponse = MVUtil.runRscript(python,
                  metCalcpyHome + "/metcalcpy/scorecard.py",
                  new String[]{pythonFileInfo},
                  new String[]{"PYTHONPATH=" + metCalcpyHome});

          stopWatch.stop();
          if (mvResponse.getInfoMessage() != null) {
            logger.info(mvResponse.getInfoMessage());
          }
          if (mvResponse.getErrorMessage() != null) {
            logger.error(mvResponse.getErrorMessage());
          }
          logger.info("Python time " + stopWatch.getFormattedTotalDuration());
        } catch (IOException | StopWatchException e) {
          logger.error( e.getMessage());
        }

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

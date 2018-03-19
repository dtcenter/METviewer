/**
 * CalcRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVUtil;
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
public class CalcRscriptManager extends RscriptManager {

  private static final Logger logger = LogManager.getLogger("CalcRscriptManager");

  private final Map<String, String> tableCalcStatInfoCommon;
  private final String calcStatTemplScript;
  private static final String SCRIPT_FILE_NAME = "/scorecard.R_tmpl";
  private final String strRFile;


  private CalcRscriptManager(Scorecard scorecard) {
    super(scorecard);
    calcStatTemplScript = scorecard.getWorkingFolders().getrTemplateDir() + SCRIPT_FILE_NAME;
    strRFile = scorecard.getWorkingFolders().getScriptsDir() + scorecard.getDataFile().replaceFirst("\\.data$", ".R");

    tableCalcStatInfoCommon = new HashMap<>();
    tableCalcStatInfoCommon.put("event_equal", String.valueOf(Boolean.TRUE).toUpperCase());
    tableCalcStatInfoCommon.put("ci_alpha", "0.05");
    tableCalcStatInfoCommon.put("equalize_by_indep", String.valueOf(Boolean.FALSE).toUpperCase());
    tableCalcStatInfoCommon.put("series2_list", "list()");
    tableCalcStatInfoCommon.put("fix_val_list_eq", "list()");
    tableCalcStatInfoCommon.put("dep1_scale", "list()");
    tableCalcStatInfoCommon.put("indy_plot_val", "list()");
    tableCalcStatInfoCommon.put("plot_stat", scorecard.getPlotStat());
    tableCalcStatInfoCommon.put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableCalcStatInfoCommon.put("data_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    tableCalcStatInfoCommon.put("r_work", scorecard.getWorkingFolders().getrWorkDir());
  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow, String threadName) {
    Map<String, String> tableCalcStatInfo = new HashMap<>(tableCalcStatInfoCommon);
    String stat = Util.getStatForRow(mapRow);
    StringBuilder fixVars = new StringBuilder();
    String fcstVar = null;
    StringBuilder diffVals = new StringBuilder();
    List<String> diffSeries = new ArrayList<>();
    for (Map.Entry<String, Entry> entry : mapRow.entrySet()) {
      if ("fcst_var".equals(entry.getKey())) {
        fcstVar = entry.getValue().getName();
      } else if (!"stat".equals(entry.getKey())) { // do not include stat variable to the fix vars list
        fixVars.append("`").append(entry.getKey()).append("` = c(\"").append(entry.getValue().getName()).append("\"),");
        diffVals.append(entry.getValue().getName()).append(" ");
      }
    }

    if (fixVars.length() > 0) {
      fixVars.deleteCharAt(fixVars.length() - 1);
    }
    if (diffVals.length() > 0) {
      diffVals.deleteCharAt(diffVals.length() - 1);
    }

    List<Entry> models = null;
    for (Field fixedField : fixedVars) {
      if ("model".equals(fixedField.getName())) {
        models = fixedField.getValues();
        break;
      }
    }
    if (models != null) {
      StringBuilder seriesList = new StringBuilder("list(");
      String indyVar = "";

      StringBuilder indyList = new StringBuilder();
      for (Map.Entry<String, List<Entry>> entry : listColumns.entrySet()) {
        if ("fcst_lead".equals(entry.getKey())) {
          seriesList.append("`").append(entry.getKey()).append("` = c(");
          for (Entry val : entry.getValue()) {
            if (seriesList.indexOf(val.getName()) == -1) {
              seriesList.append("\"").append(val.getName()).append("\",");
            }
            StringBuilder difStr = new StringBuilder("c(");
            for (Entry model : models) {
              difStr.append("\"");
              if (diffVals.length() > 0) {
                difStr.append(diffVals).append(" ");
              }
              difStr.append(model.getName()).append(" ").append(val.getName()).append(" ").append(fcstVar).append(" ").append(stat).append("\",");
            }

            //difStr.append("\"DIFF_SIG\"").append("),");
            difStr.append("\"").append(diffStat).append("\"),");

            diffSeries.add(difStr.toString().trim());
          }
          if (seriesList.length() > 0) {
            seriesList.deleteCharAt(seriesList.length() - 1);
          }
          seriesList.append("),");
        } else {
          indyVar = entry.getKey();
          for (Entry val : entry.getValue()) {
            if (indyList.indexOf(val.getName()) == -1) {
              indyList.append("\"").append(val.getName()).append("\",");
            }
          }
          if (indyList.length() > 0) {
            indyList.deleteCharAt(indyList.length() - 1);
          }
        }
      }
      if (seriesList.charAt(seriesList.length() - 1) == ',' && fixVars.length() == 0) {
        seriesList.deleteCharAt(seriesList.length() - 1);
      }
      seriesList.append(fixVars).append(", `model` = c(");
      for (Entry val : models) {
        seriesList.append("\"").append(val.getName()).append("\",");
      }
      if (seriesList.length() > 0) {
        seriesList.deleteCharAt(seriesList.length() - 1);
      }

      seriesList.append("))");

      StringBuilder seriesDiffList = new StringBuilder("list(");
      for (String diff : diffSeries) {
        if (seriesDiffList.indexOf(diff) == -1) {
          seriesDiffList.append(diff);
        }
      }
      if (seriesDiffList.length() > 0) {
        seriesDiffList.deleteCharAt(seriesDiffList.length() - 1);
      }
      seriesDiffList.append(")");


      tableCalcStatInfo.put("indy_var", indyVar);
      tableCalcStatInfo.put("indy_list", "c(" + indyList + ")");
      tableCalcStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");
      tableCalcStatInfo.put("series1_list", seriesList.toString());
      tableCalcStatInfo.put("series1_diff_list", seriesDiffList.toString());


      try(PrintStream printStream = IoBuilder.forLogger(CalcRscriptManager.class)
                                                     .setLevel(org.apache.logging.log4j.Level.INFO)
                                                     .buildPrintStream()) {
        MVUtil.populateTemplateFile(calcStatTemplScript, strRFile, tableCalcStatInfo);
        //  run agg_stat/
        MVUtil mvUtil = new MVUtil();


        mvUtil.runRscript(rScriptCommand, strRFile, printStream);
      } catch (Exception e) {
        logger.error(e);
      }

    }
  }
}

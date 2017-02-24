/**
 * SumRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:40 $
 */
public class SumRscriptManager extends RscriptManager {

  private static final Logger logger = Logger.getLogger(SumRscriptManager.class);

  Map<String, String> tableCalcStatInfoCommon;
  private final String calcStatTemplScript;
  private static final String SCRIPT_FILE_NAME = "/scorecard.R_tmpl";
  private String strRFile;


  public SumRscriptManager(Scorecard scorecard) {
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
    tableCalcStatInfoCommon.put("plot_stat", "median");
    tableCalcStatInfoCommon.put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableCalcStatInfoCommon.put("data_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile().replaceAll(".data", ".dataFromDb"));
    tableCalcStatInfoCommon.put("plot_file", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    tableCalcStatInfoCommon.put("r_work", scorecard.getWorkingFolders().getrWorkDir());
    tableCalcStatInfoCommon.put("stat_flag", scorecard.getStatFlag());
  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow) {
    clean();
    initModels();
    if (models != null) {
      Map<String, String> tableCalcStatInfo = new HashMap<>(tableCalcStatInfoCommon);
      init(mapRow);
      tableCalcStatInfo.put("indy_var", indyVar);
      tableCalcStatInfo.put("indy_list", "c(" + indyList + ")");
      tableCalcStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");
      tableCalcStatInfo.put("series_list", seriesList.toString());
      tableCalcStatInfo.put("series_diff_list", seriesDiffList.toString());

      //check id output file exists and its length not 0
      File output = new File(tableCalcStatInfo.get("plot_file"));
      boolean isAppend = false;
      if (output.exists() && output.length() > 0) {
        isAppend = true;
      }
      tableCalcStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());


      try {
        MVUtil.populateTemplateFile(calcStatTemplScript, strRFile, tableCalcStatInfo);
        //  run agg_stat/
        MVUtil mvUtil = new MVUtil();
        mvUtil.runRscript(rScriptCommand, strRFile, false);
      } catch (Exception e) {
        logger.error(e);
      }

    }
  }
}

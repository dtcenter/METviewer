/**
 * AggRscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 15:19 $
 */
public class AggRscriptManager extends RscriptManager {

  private static final Logger logger = Logger.getLogger(AggRscriptManager.class);
  private final String strAggInfo;
  private final String aggStatTemplFilePath;
  private final String aggStatTemplScript;

  Map<String, String> tableAggStatInfoCommon;

  private static final String SCRIPT_FILE_NAME = "/include/agg_stat.R";


  public AggRscriptManager(final Scorecard scorecard) {
    super(scorecard);
    strAggInfo = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile().replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
    aggStatTemplFilePath = scorecard.getWorkingFolders().getrTemplateDir() + "/agg_stat.info_tmpl";
    aggStatTemplScript = scorecard.getWorkingFolders().getrWorkDir() + SCRIPT_FILE_NAME;


    tableAggStatInfoCommon = new HashMap<>();
    tableAggStatInfoCommon.put("agg_nbrcnt", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("agg_ssvar", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("agg_vl1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("event_equal", String.valueOf(Boolean.TRUE).toUpperCase());
    tableAggStatInfoCommon.put("eveq_dis", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("boot_repl", String.valueOf(scorecard.getNumBootReplicates()));
    tableAggStatInfoCommon.put("boot_ci", "perc");
    tableAggStatInfoCommon.put("ci_alpha", "0.05");
    tableAggStatInfoCommon.put("equalize_by_indep", String.valueOf(Boolean.FALSE).toUpperCase());
    tableAggStatInfoCommon.put("agg_stat2", "c()");
    tableAggStatInfoCommon.put("series2_list", "list()");
    tableAggStatInfoCommon.put("fix_val_list_eq", "list()");
    tableAggStatInfoCommon.put("working_dir", scorecard.getWorkingFolders().getrWorkDir() + "/include");
    tableAggStatInfoCommon.put("series2_diff_list", "list()");
    tableAggStatInfoCommon.put("dep2_plot", "c()");
    tableAggStatInfoCommon.put("agg_stat_input", scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile());
    tableAggStatInfoCommon.put("agg_stat_output", scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
    String seed = "NA";
    if (scorecard.getBootRandomSeed() != null) {
      seed = String.valueOf(scorecard.getBootRandomSeed());
    }
    tableAggStatInfoCommon.put("boot_random_seed", seed);

  }

  @Override
  public void calculateStatsForRow(Map<String, Entry> mapRow) {
    clean();
    initModels();
    if (models != null) {

      Map<String, String> tableAggStatInfo = new HashMap<>(tableAggStatInfoCommon);
      init(mapRow);
      initAggBool(tableAggStatInfo, Util.getAggTypeForStat(stat));


      tableAggStatInfo.put("indy_var", indyVar);
      tableAggStatInfo.put("indy_list", "c(" + indyList + ")");

      tableAggStatInfo.put("agg_stat1", "c(\"" + stat + "\")");


      tableAggStatInfo.put("series1_list", seriesList.toString());


      tableAggStatInfo.put("agg_stat_static", "list(`fcst_var` = \"" + fcstVar + "\")");

      tableAggStatInfo.put("series1_diff_list", seriesDiffList.toString());
      tableAggStatInfo.put("dep1_plot", "list(`" + fcstVar + "` = c(\"" + stat + "\"))");

      //check id output file exists and its length not 0
      File output = new File(tableAggStatInfo.get("agg_stat_output"));
      boolean isAppend = false;
      if (output.exists() && output.length() > 0) {
        isAppend = true;
      }
      tableAggStatInfo.put("append_to_file", String.valueOf(isAppend).toUpperCase());


      try {
        MVUtil.populateTemplateFile(aggStatTemplFilePath, strAggInfo, tableAggStatInfo);
        //  run agg_stat/
        MVUtil mvUtil = new MVUtil();
        mvUtil.runRscript(rScriptCommand, aggStatTemplScript, new String[]{strAggInfo}, false);
      } catch (Exception e) {
        logger.error(e);
      }
    }
  }

  private void initAggBool(Map<String, String> tableAggStatInfo, String stat) {
    if (stat.equals(MVUtil.CTC)) {
      tableAggStatInfo.put("agg_ctc", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_ctc", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.SL1L2)) {
      tableAggStatInfo.put("agg_sl1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_sl1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
    if (stat.equals(MVUtil.SAL1L2)) {
      tableAggStatInfo.put("agg_sal1l2", String.valueOf(Boolean.TRUE).toUpperCase());
    } else {
      tableAggStatInfo.put("agg_sal1l2", String.valueOf(Boolean.FALSE).toUpperCase());
    }
  }

}

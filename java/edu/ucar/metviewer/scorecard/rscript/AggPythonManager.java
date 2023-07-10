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
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.File;
import java.io.IOException;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.createYamlFile;

/**
 * Constructs and runs Rscript for agg stats
 *
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 15:19 $
 */
public class AggPythonManager extends PythonManager {

  private static final Logger logger = LogManager.getLogger(AggRscriptManager.class);

  private final String aggInfoFileName;
  private final Map<String, Object> yamlInfo;
  private static final String PYTHON_SCRIPT = "/metcalcpy/agg_stat.py";


  public AggPythonManager(final Scorecard scorecard) {
    super(scorecard);
    aggInfoFileName = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile()
            .replaceFirst("\\.data.agg_stat$",
                    ".agg_stat.info");


    yamlInfo = new HashMap<>();
    yamlInfo.put("event_equal", "True");
    yamlInfo.put("num_iterations", scorecard.getNumBootReplicates());
    yamlInfo.put("alpha", 0.05);
    yamlInfo.put("fcst_var_val_2", new HashMap<>());

    yamlInfo.put("agg_stat_output",
            scorecard.getWorkingFolders().getDataDir() + scorecard
                    .getDataFile());
    Integer seed = null;
    if (scorecard.getBootRandomSeed() != null) {
      seed = scorecard.getBootRandomSeed();
    }
    yamlInfo.put("random_seed", seed);
    yamlInfo.put("list_stat_2", new ArrayList<>());
    yamlInfo.put("method", "perc");
    yamlInfo.put("num_threads", -1);
    yamlInfo.put("series_val_2", new HashMap<>());
    yamlInfo.put("derived_series_2", new ArrayList<>());
    yamlInfo.put("agg_stat_input", scorecard.getWorkingFolders().getDataDir() + scorecard
            .getAggStatDataFile());

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
        init(mapRow);
        initAggBool(yamlInfo, Util.getAggTypeForStat(stat));

        //create a template
        yamlInfo.put("indy_var", indyVar);
        yamlInfo.put("indy_vals", indyList.get(indyVar));

        List<String> statList = new ArrayList<>();
        statList.add(stat);
        yamlInfo.put("list_stat_1", statList);
        yamlInfo.put("series_val_1", seriesList);

        yamlInfo.put("derived_series_1", seriesDiffList);
        Map<String, List<String>> fcst_var_val_1 = new HashMap<>();
        List<String> fcst_var_val_1_list = new ArrayList<>();
        fcst_var_val_1_list.add(stat);
        fcst_var_val_1.put(fcstVar, fcst_var_val_1_list);
        yamlInfo.put("fcst_var_val_1", fcst_var_val_1);
        yamlInfo.put("fixed_vars_vals_input", listFixedValEx);


        //check id output file exists and its length not 0
        File output = new File((String) yamlInfo.get("agg_stat_output"));
        boolean isAppend = false;
        if (output.exists() && output.length() > 0) {
          isAppend = true;
        }
        yamlInfo.put("append_to_file", isAppend ? "True" : "False");

        try  {
          createYamlFile(aggInfoFileName, yamlInfo);
          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          logger.info(python + " " + metCalcpyHome + PYTHON_SCRIPT + " " + aggInfoFileName);


          MvResponse mvResponse = MVUtil.runRscript(python,
                  metCalcpyHome + PYTHON_SCRIPT,
                  new String[]{aggInfoFileName},
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

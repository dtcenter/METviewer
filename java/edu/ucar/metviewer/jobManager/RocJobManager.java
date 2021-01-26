/**
 * RocJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.ValidationException;
import edu.ucar.metviewer.rscriptManager.RscriptNoneStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptStatManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:09 $
 */
public class RocJobManager extends JobManager {

  public RocJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }


  @Override
  protected void run(MVPlotJob job) throws ParseException, ValidationException, IOException, StopWatchException {
    boolean isEE = job.getEventEqual();

    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap plotFixPerm : listPlotFixPerm) {
      job.setEventEqual(isEE);

      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(),
              plotFixPerm,
              mvBatch.getDatabaseManager().getDateFormat());
      job.setTmplVal(fixTmplVal);
      MVOrderedMap fixVals = job.getPlotFixVal();
      for (String fixFar : fixVals.getKeyList()) {
        if (fixTmplVal.containsKey(fixFar)) {
          fixVals.put(fixFar, fixTmplVal.get(fixFar));
        }
      }
      MVOrderedMap fixValsEE = job.getPlotFixValEq();
      for (String fixFar : fixValsEE.getKeyList()) {
        if (fixTmplVal.containsKey(fixFar)) {
          fixValsEE.put(fixFar, fixTmplVal.get(fixFar));
        }
      }

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());

      String dataFile = mvBatch.getDataFolder()
              + MVUtil.buildTemplateString(job.getDataFileTmpl(),
              mapPlotTmplVals,
              job.getTmplMaps(),
              mvBatch.getPrintStream());
      (new File(dataFile)).getParentFile().mkdirs();
      int intNumDepSeries = mvBatch.getDatabaseManager()
              .buildAndExecuteQueriesForRocRelyJob(job, dataFile, plotFixPerm,
                      mvBatch.getPrintStream(),
                      mvBatch.getPrintStreamSql());

      Map<String, Object> info = createInfoMap(job, intNumDepSeries);
      RscriptStatManager rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      rscriptStatManager
              .prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>());

      if (job.getExecutionType().equals("Rscript") || this.getPythonScript().isEmpty()) {
        info.put("data_file", dataFile);
        rscriptStatManager.runRscript(job, info);
      } else {
        Map<String, Object> yamlInfo = createYamlInfoMap(job);
        yamlInfo.put("stat_input", dataFile);
        yamlInfo.put("roc_pct", job.getRocPct());
        yamlInfo.put("roc_ctc", job.getRocCtc());
        yamlInfo.put("add_point_thresholds", job.getAddPointThresholds() ? "True" : "False");
        yamlInfo.put("reverse_connection_order", job.getReverseConnectionOrder() ? "True" : "False");
        job.setPlotTmpl(this.getPythonScript());
        yamlInfo = this.addPlotConfigs(yamlInfo, job, intNumDepSeries);
        rscriptStatManager.runPythonScript(job, yamlInfo);

      }


    }

  }

  @Override
  protected String getPythonScript() {
    return "/plots/roc_diagram/roc_diagram.py";
  }

}

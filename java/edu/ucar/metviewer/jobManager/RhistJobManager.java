/**
 * RhistJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
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
 * @version : 1.0 : 21/12/17 09:56 $
 */
public class RhistJobManager extends JobManager {

  public RhistJobManager(MVBatch mvBatch) {
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
      /*
       *  Print the data file in the R_work subfolder and file specified by the data file template
       */


      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());

      String dataFile = mvBatch.getDataFolder()
              + MVUtil.buildTemplateString(job.getDataFileTmpl(),
              mapPlotTmplVals,
              job.getTmplMaps(),
              mvBatch.getPrintStream());
      (new File(dataFile)).getParentFile().mkdirs();

      String strMsg = mvBatch.getDatabaseManager()
              .buildAndExecuteQueriesForHistJob(job, dataFile,
                      plotFixPerm,
                      mvBatch.getPrintStream(),
                      mvBatch.getPrintStreamSql());
      if (strMsg.length() > 0) {
        mvBatch.print("\n==== Start database error  ====\n" + strMsg + "\n====   End database error"
                + "  ====");
      }


      int intNumDepSeries = 1;
      Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
      for (Map.Entry aListSeries1Val : listSeries1Val) {
        String[] listVal = (String[]) aListSeries1Val.getValue();
        if(listVal.length > 0) {
          intNumDepSeries *= listVal.length;
        }
      }

      //  validate the number of formatting elements
      if (intNumDepSeries != MVUtil.parseRCol(job.getPlotDisp()).length) {
        throw new ValidationException("length of plot_disp differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0 && intNumDepSeries != MVUtil.parseRCol(
              job.getOrderSeries()).length) {
        throw new ValidationException("length of order_series differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries != MVUtil.parseRCol(job.getColors()).length) {
        throw new ValidationException("length of colors differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty() &&
              intNumDepSeries != MVUtil.parseRCol(job.getLegend()).length) {
        throw new ValidationException("length of legend differs from number of series ("
                + intNumDepSeries + ")");
      }
      Map<String, Object> info = createInfoMap(job, intNumDepSeries);
      RscriptStatManager rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      if (job.getExecutionType().equals("Rscript")) {
        rscriptStatManager
                .prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>());
        info.put("data_file", dataFile);

        rscriptStatManager.runRscript(job, info);
      } else {
        Map<String, Object> yamlInfo = createYamlInfoMap(job);
        yamlInfo.put("normalized_histogram", job.getNormalizedHistogram()  ? "True" : "False");
        yamlInfo.put("stat_input", dataFile);
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, new ArrayList<>());
        job.setPlotTmpl(this.getPythonScript());
        yamlInfo = this.addPlotConfigs(yamlInfo, job, intNumDepSeries);
        rscriptStatManager.runPythonScript(job, yamlInfo);
      }

    }

  }
  @Override
  protected String getPythonScript() {
    return "/plots/line/rhist.py";
  }

}

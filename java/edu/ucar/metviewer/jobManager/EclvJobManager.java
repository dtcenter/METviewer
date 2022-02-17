/**
 * EclvJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.ValidationException;
import edu.ucar.metviewer.rscriptManager.RscriptAggStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptNoneStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptStatManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 13:05 $
 */
public class EclvJobManager extends JobManager {

  public EclvJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected void run(MVPlotJob job) throws ValidationException, ParseException, IOException, StopWatchException {
    boolean isEE = job.getEventEqual();
    MVOrderedMap mapTmplValsPlot = new MVOrderedMap(job.getTmplVal());
    if (job.getIndyVar() != null) {
      mapTmplValsPlot.put("indy_var", job.getIndyVar());
      job.setTmplVal(mapTmplValsPlot);
    }


    //  construct the file system paths for the files used to build the plot
    MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());


    String dataFile = mvBatch.getDataFolder()
            + MVUtil.buildTemplateString(job.getDataFileTmpl(),
            mapPlotTmplVals, job.getTmplMaps(),
            mvBatch.getPrintStream());


    List<String> listQuery = new ArrayList<>();
    int intNumDepSeries;

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
      Map<String, Object> info;
      Map<String, Object> yamlInfo = null;
      RscriptStatManager rscriptStatManager;
      if (job.getAggCtc() || job.getAggPct()) {
        intNumDepSeries = 1;
        Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
        for (Map.Entry aListSeries1Val : listSeries1Val) {
          String[] listVal = (String[]) aListSeries1Val.getValue();
          intNumDepSeries *= listVal.length;
        }
        info = createInfoMap(job, intNumDepSeries);
        rscriptStatManager = new RscriptAggStatManager(mvBatch);
        //  build the SQL statements for the current plot
        listQuery = mvBatch.getDatabaseManager().buildPlotSql(job, plotFixPerm,
                mvBatch.getPrintStreamSql());

        List<String> listAggStats1 = new ArrayList<>();
        listAggStats1.add("ECLV");

        if (job.getExecutionType().equals("Rscript")) {
          rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);

          info.put("agg_stat1", MVUtil.printRCol(
                  listAggStats1.toArray(new String[listAggStats1.size()]), true));
          rscriptStatManager.runRscript(job, info);
          info.put("event_equal", "FALSE");
        }else {
          yamlInfo = createYamlInfoMap(job);
          yamlInfo.put("agg_stat1", listAggStats1);
          String lineType = "ctc";
          if (job.getAggPct()){
            lineType = "pct";
          }
          yamlInfo.put("line_type", lineType);
          rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, listQuery);
          rscriptStatManager.runPythonScript(job, yamlInfo);
          yamlInfo.put("event_equal", "False");
          yamlInfo.remove("agg_stat_output");
          yamlInfo.remove("agg_stat_input");
          yamlInfo.remove("agg_ctc");
          yamlInfo.remove("agg_pct");
        }

        //  turn off the event equalizer
        job.setEventEqual(Boolean.FALSE);

        listQuery.clear();

      } else {

        (new File(dataFile)).getParentFile().mkdirs();
        intNumDepSeries = mvBatch.getDatabaseManager()
                .buildAndExecuteQueriesForEclvJob(job, dataFile, plotFixPerm,
                        mvBatch.getPrintStream(),
                        mvBatch.getPrintStreamSql());
        info = createInfoMap(job, intNumDepSeries);
      }

      rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      if (job.getExecutionType().equals("Rscript")) {
        rscriptStatManager
                .prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
        info.put("data_file", dataFile);
        rscriptStatManager.runRscript(job, info);
      }else {
        if (yamlInfo == null) {
          yamlInfo = createYamlInfoMap(job);
        }
        yamlInfo.put("stat_input", dataFile);
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, listQuery);
        job.setPlotTmpl(this.getPythonScript());
        yamlInfo = this.addPlotConfigs(yamlInfo, job, intNumDepSeries);
        rscriptStatManager.runPythonScript(job, yamlInfo);
      }


    }

  }
  @Override
  protected String getPythonScript() {
    return "/metplotpy/plots/eclv/eclv.py";
  }
}

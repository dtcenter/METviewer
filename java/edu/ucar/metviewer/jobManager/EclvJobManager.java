/**
 * EclvJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
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
  protected void run(MVPlotJob job) throws Exception {

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
      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(),
                                                    plotFixPerm,
                                                    mvBatch.getDatabaseManager().getDateFormat());
      job.setTmplVal(fixTmplVal);
      Map<String, String> info ;
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
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
        List<String> listAggStats1 = new ArrayList<>();
        listAggStats1.add("ECLV");
        info.put("agg_stat1", MVUtil.printRCol(
            listAggStats1.toArray(new String[listAggStats1.size()]), true));
        boolean success = rscriptStatManager.runRscript(job, info);
        //  turn off the event equalizer
        job.setEventEqual(Boolean.FALSE);
        info.put("event_equal",  "FALSE");
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
      rscriptStatManager
          .prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
      info.put("data_file", dataFile);

      boolean success = rscriptStatManager.runRscript(job, info);


    }

  }
}

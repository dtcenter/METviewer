/**
 * RelyJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.util.ArrayList;
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
 * @version : 1.0 : 21/12/17 12:51 $
 */
public class RelyJobManager extends JobManager {

  public RelyJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected void run(MVPlotJob job) throws Exception {


    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap plotFixPerm : listPlotFixPerm) {
      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(),
                                                    plotFixPerm,
                                                    mvBatch.getDatabaseManager().getDateFormat());
      job.setTmplVal(fixTmplVal);
      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());

      String dataFile = mvBatch.getDataFolder()
                            + MVUtil.buildTemplateString(job.getDataFileTmpl(),
                                                         mapPlotTmplVals,
                                                         job.getTmplMaps(),
                                                         mvBatch.getPrintStream());
      (new File(dataFile)).getParentFile().mkdirs();
      int intNumDepSeries = mvBatch.getDatabaseManager()
                                .buildAndExecuteQueriesForRocRelyJob(job, dataFile + ".agg_stat",
                                                                     plotFixPerm,
                                                                     mvBatch.getPrintStream(),
                                                                     mvBatch.getPrintStreamSql());

      Map<String, String> info = createInfoMap(job, intNumDepSeries);
      info.put("agg_pct", "TRUE");
      info.put("indy_var", "thresh_i");
      info.put("indy_list", "c(0, 0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9)");
      info.put("agg_stat1", "c(\"PSTD_CALIBRATION\", \"PSTD_BASER\", \"PSTD_NI\")");
      MVOrderedMap mapAggStatStatic = new MVOrderedMap();

      Object objFcstVar = job.getPlotFixVal().get("fcst_var");
      String[] listFcstVar;
      if (objFcstVar instanceof String[]) {
        listFcstVar = (String[]) job.getPlotFixVal().get("fcst_var");
      } else {
        MVOrderedMap mapFcstVar = (MVOrderedMap) job.getPlotFixVal().get("fcst_var");
        listFcstVar = (String[]) mapFcstVar.get(mapFcstVar.getKeyList()[0]);
      }
      mapAggStatStatic.put("fcst_var", listFcstVar[0]);
      info.put("agg_stat_static", mapAggStatStatic.getRDecl());

      MVOrderedMap mapDep1Plot = new MVOrderedMap();
      mapDep1Plot.put(listFcstVar[0], new String[]{"PSTD_CALIBRATION","PSTD_BASER", "PSTD_NI"});
      info.put("dep1_plot",  mapDep1Plot.getRDecl());


      RscriptStatManager rscriptStatManager = new RscriptAggStatManager(mvBatch);
      rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>(0));
      rscriptStatManager.runRscript(job, info);


      rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      rscriptStatManager
          .prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>());
      info.put("data_file", dataFile);

      boolean success = rscriptStatManager.runRscript(job, info);

    }

  }
}

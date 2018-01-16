/**
 * RhistJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
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
  protected void prepareRscript(MVPlotJob job) throws Exception {


    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap plotFixPerm : listPlotFixPerm) {

      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(), plotFixPerm, mvBatch
                                                                                        .getDatabaseManager()
                                                                                        .getDateFormat());
      job.setTmplVal(fixTmplVal);

  			/*
         *  Print the data file in the R_work subfolder and file specified by the data file template
  			 */


      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());

      String dataFile = mvBatch.getDataFolder() + MVUtil
                                                         .buildTemplateString(job.getDataFileTmpl(),
                                                                              mapPlotTmplVals,
                                                                              job.getTmplMaps(),
                                                                              mvBatch
                                                                                  .getPrintStream());
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
        intNumDepSeries *= listVal.length;
      }

      //  validate the number of formatting elements
      if (intNumDepSeries != MVUtil.parseRCol(job.getPlotDisp()).length) {
        throw new Exception("length of plot_disp differs from number of series (" + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0 && intNumDepSeries != MVUtil.parseRCol(
          job.getOrderSeries()).length) {
        throw new Exception("length of order_series differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != MVUtil.parseRCol(job.getColors()).length) {
        throw new Exception("length of colors differs from number of series (" + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty() &&
              intNumDepSeries != MVUtil.parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series (" + intNumDepSeries + ")");
      }
      Map<String, String> info = createInfoMap(job, intNumDepSeries);
      RscriptStatManager rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      rscriptStatManager
          .prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>());
      info.put("data_file", dataFile);

      boolean success = rscriptStatManager.runRscript(job, info);
      if (success) {
        mvBatch.print("Created plot " + rscriptStatManager.getPlotFile());
      } else {
        mvBatch.print("Failed to create plot " + rscriptStatManager.getPlotFile());
      }

    }

  }

}

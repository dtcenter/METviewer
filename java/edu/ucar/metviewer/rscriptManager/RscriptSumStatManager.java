/**
 * RscriptSumStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.rscriptManager;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

/**
 * @author : tatiana $
 * @version : 1.0 : 22/12/17 10:49 $
 */
public class RscriptSumStatManager extends RscriptStatManager {

  private static final PrintStream errorStream = IoBuilder.forLogger(MVUtil.class)
                                                     .setLevel(org.apache
                                                                   .logging.log4j.Level.INFO)
                                                     .setMarker(
                                                         new MarkerManager.Log4jMarker("ERROR"))
                                                     .buildPrintStream();


  public RscriptSumStatManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  public void prepareDataFileAndRscript(
                                           MVPlotJob job, MVOrderedMap mvMap,
                                           Map<String, String> info,

                                           List<String> listQuery) throws
      Exception {

    //  run the plot SQL against the database connection
    long intStartTime = new Date().getTime();

    dataFile = mvBatch.getDataFolder()
                   + MVUtil.buildTemplateString(job.getDataFileTmpl(),
                                                MVUtil.addTmplValDep(job),
                                                job.getTmplMaps(),
                                                mvBatch.getPrintStream());


    dataFile = dataFile + ".sum_stat";

    rScriptFile = mvBatch.getRworkFolder() + "include/sum_stat.R";

    (new File(dataFile)).getParentFile().mkdirs();

    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      mvBatch.getDatabaseManager().executeQueriesAndSaveToFile(listQuery, dataFile,
                                                               job.getCalcCtc()
                                                                   || job.getCalcSl1l2()
                                                                   || job.getCalcSal1l2(),
                                                               job.getCurrentDBName().get(i),
                                                               i == 0);
    }
    mvBatch.print("Query returned  plot_data rows in " + MVUtil.formatTimeSpan(
        new Date().getTime() - intStartTime));

  }

  @Override
  public boolean runRscript(MVPlotJob job, Map<String, String> info) {

    String sumInfo = dataFile.replaceFirst("\\.data.sum_stat$", ".sum_stat.info");
    String sumOutput = dataFile.replaceFirst("\\.sum_stat$", "");
    File fileAggOutput = new File(sumOutput);
    String tmplFileName = "sum_stat.info_tmpl";
    info.put("sum_stat_input", dataFile);
    info.put("sum_stat_output", sumOutput);
    boolean success = false;
    try {
      MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + tmplFileName, sumInfo,
                                  info);
      //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting
      if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
        fileAggOutput.getParentFile().mkdirs();
        success = MVUtil.runRscript(job.getRscript(),
                                    rScriptFile,
                                    new String[]{sumInfo},
                                    mvBatch.getPrintStream());
      }
    } catch (Exception e) {
      errorStream.print(e.getMessage());
    }

    return success;
  }
}

/**
 * RscriptSumStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.rscriptManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
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
      List<String> listQuery) throws Exception {

    //  run the plot SQL against the database connection
    dataFile = mvBatch.getDataFolder()
                   + MVUtil.buildTemplateString(job.getDataFileTmpl(),
                                                MVUtil.addTmplValDep(job),
                                                job.getTmplMaps(),
                                                mvBatch.getPrintStream());
    dataFile = dataFile + ".sum_stat";

    rScriptFile = mvBatch.getRworkFolder() + "include/sum_stat.R";

    (new File(dataFile)).getParentFile().mkdirs();

    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      MvResponse mvResponse =
          mvBatch.getDatabaseManager().executeQueriesAndSaveToFile(listQuery, dataFile,
                                                                   job.isCalcStat(),
                                                                   job.getCurrentDBName().get(i),
                                                                   i == 0);
      if (mvResponse.getInfoMessage() != null) {
        mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
      }
    }
  }

  @Override
  public boolean runRscript(MVPlotJob job, Map<String, String> info) {

    String sumInfo = dataFile.replaceFirst("\\.data.sum_stat$", ".sum_stat.info");
    String sumOutput = dataFile.replaceFirst("\\.sum_stat$", "");
    File fileAggOutput = new File(sumOutput);
    String tmplFileName = "sum_stat.info_tmpl";
    info.put("sum_stat_input", dataFile);
    info.put("sum_stat_output", sumOutput);
    MvResponse mvResponse = new MvResponse();
    try {
      MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + tmplFileName, sumInfo, info);
      //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting
      if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
        fileAggOutput.getParentFile().mkdirs();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        mvBatch.getPrintStream().println("\nRunning " + job.getRscript() + " " + rScriptFile);
        mvResponse = MVUtil.runRscript(job.getRscript(), rScriptFile, new String[]{sumInfo});
        stopWatch.stop();
        if (mvResponse.getInfoMessage() != null) {
          mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
        }
        if (mvResponse.getErrorMessage() != null) {
          mvBatch.getPrintStream().println(mvResponse.getErrorMessage());
        }
        mvBatch.getPrintStream().println("Rscript time " + stopWatch.getFormattedTotalDuration());
      }
    } catch (IOException | StopWatchException e) {
      errorStream.print(e.getMessage());
    }

    return mvResponse.isSuccess();
  }
}

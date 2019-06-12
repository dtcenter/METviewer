/**
 * RscriptNoneStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
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
import edu.ucar.metviewer.ValidationException;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

/**
 * @author : tatiana $
 * @version : 1.0 : 28/12/17 13:44 $
 */
public class RscriptNoneStatManager extends RscriptStatManager {

  private static final PrintStream errorStream
          = IoBuilder.forLogger(MVUtil.class)
          .setLevel(org.apache.logging.log4j.Level.INFO)
          .setMarker(new MarkerManager.Log4jMarker("ERROR"))
          .buildPrintStream();

  public RscriptNoneStatManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  public void prepareDataFileAndRscript(
          MVPlotJob job, MVOrderedMap mvMap,
          Map<String, String> info,
          List<String> listQuery) throws ValidationException {

    //  use the map of all plot values to populate the template strings
    String fileName = MVUtil.buildTemplateString(job.getPlotFileTmpl(),
            MVUtil.addTmplValDep(job),
            job.getTmplMaps(),
            mvBatch.getPrintStream());
    plotFile = mvBatch.getPlotsFolder() + fileName;

    //validate file name - should be less than 256 chars
    if (plotFile.length() >= 256) {
      throw new ValidationException("The plot filename is longer than the limit 255 characters");
    }

    fileName = MVUtil.buildTemplateString(job.getRFileTmpl(),
            MVUtil.addTmplValDep(job),
            job.getTmplMaps(),
            mvBatch.getPrintStream());
    rScriptFile = mvBatch.getScriptsFolder() + fileName;


    //  create the plot and R script output folders, if necessary
    (new File(plotFile)).getParentFile().mkdirs();
    (new File(rScriptFile)).getParentFile().mkdirs();


    if (job.getLogY1() && !job.getY1Lim().equals("c()")) {
      //check if y1_lim has 0
      String[] lims = job.getY1Lim().replace("c(", "").replace(")", "").split(",");
      if (lims[0].equals("0") || lims[1].equals("0")) {
        throw new ValidationException("Y1 axis limits can't start or end with 0 if Log Scale is on");
      }
    }

    if (job.getLogY2() && !job.getY2Lim().equals("c()")) {
      //check if y2_lim has 0
      String[] lims = job.getY2Lim().replace("c(", "").replace(")", "").split(",");
      if (lims[0].equals("0") || lims[1].equals("0")) {
        throw new ValidationException("Y2 axis limits can't start or end with 0 if Log Scale is on");
      }
    }

  }

  @Override
  public boolean runRscript(MVPlotJob job, Map<String, String> info) {
    MvResponse mvResponse;
    try {
      info.put("plot_file", plotFile);

      MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + job.getPlotTmpl(), rScriptFile, info);
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      mvBatch.getPrintStream().println("\nRunning " + job.getRscript() + " " + rScriptFile);
      mvResponse = MVUtil.runRscript(job.getRscript(), rScriptFile);
      stopWatch.stop();
      if (mvResponse.getInfoMessage() != null) {
        mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
      }
      if (mvResponse.getErrorMessage() != null) {
        mvBatch.getPrintStream().println(mvResponse.getErrorMessage());
      }
      if (mvResponse.isSuccess()) {
        mvBatch.getPrintStream().println("Created plot " + getPlotFile());
      } else {
        mvBatch.getPrintStream().println("Failed to create plot " + getPlotFile());
      }

      mvBatch.getPrintStream().println("Rscript time " + stopWatch.getFormattedTotalDuration());
    } catch (IOException | StopWatchException e) {
      errorStream.print(e.getMessage());
      mvResponse = new MvResponse();
    }

    return mvResponse.isSuccess();
  }

}

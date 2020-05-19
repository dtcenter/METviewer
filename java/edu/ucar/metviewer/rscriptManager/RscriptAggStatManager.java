/**
 * RscriptAggStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.rscriptManager;

import edu.ucar.metviewer.*;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.ucar.metviewer.MVUtil.createYmlFile;

/**
 * @author : tatiana $
 * @version : 1.0 : 22/12/17 10:15 $
 */
public class RscriptAggStatManager extends RscriptStatManager {
  private static final String AGG_PYTHON_SCRIPT = "/metcalcpy/agg_stat.py";

  private static final PrintStream errorStream = IoBuilder.forLogger(MVUtil.class)
          .setLevel(org.apache
                  .logging.log4j.Level.INFO)
          .setMarker(
                  new MarkerManager.Log4jMarker("ERROR"))
          .buildPrintStream();

  public RscriptAggStatManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  public void prepareDataFileAndRscript(
          MVPlotJob job, MVOrderedMap mvMap,
          Map<String, Object> info,
          List<String> listQuery) throws ValidationException, IOException, StopWatchException {


    String fileName = MVUtil.buildTemplateString(job.getDataFileTmpl(),
            MVUtil.addTmplValDep(job),
            job.getTmplMaps(),
            mvBatch.getPrintStream());

    dataFile = mvBatch.getDataFolder() + fileName;
    if ((job.isModeJob() || job.isMtdJob()) && job.getEventEqual()) {
      //run ee first
      //create sql query
      MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
      List<String> eventEqualizeSql = mvBatch.getDatabaseManager()
              .buildPlotModeEventEqualizeSql(job,
                      mvMap,
                      mapPlotFixVal);

      for (String sql : eventEqualizeSql) {
        mvBatch.printSql(sql + "\n");
      }


      MvResponse mvResponse = new MvResponse();
      for (int i = 0; i < job.getCurrentDBName().size(); i++) {
        mvResponse = mvBatch.getDatabaseManager()
                .executeQueriesAndSaveToFile(eventEqualizeSql, dataFile + "_ee_input",
                        job.isCalcStat(),
                        job.getCurrentDBName().get(i), i == 0);
        if (mvResponse.getInfoMessage() != null) {
          mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
        }
      }

      if (mvResponse.isSuccess()) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String eeInfo = dataFile.replaceFirst("\\.data$", ".agg_stat_event_equalize.info");

        if(info.getClass() == TreeMap.class){
          //use python
          info.put("agg_stat_input", dataFile + "_ee_input");
          info.put("agg_stat_output", dataFile + ".ee");
          createYmlFile(eeInfo, info);
          mvBatch.getPrintStream().println("\nRunning "
                  + mvBatch.getPython()
                  + " "
                  + mvBatch.getMetCalcpyHome() + "/metcalcpy/agg_stat_event_equalize.py"
                  + " "
                  + eeInfo);

          mvResponse = MVUtil.runRscript(mvBatch.getPython(),
                  mvBatch.getMetCalcpyHome() + "/metcalcpy/agg_stat_event_equalize.py",
                  new String[]{eeInfo},
                  new String[]{"PYTHONPATH=" + mvBatch.getMetCalcpyHome()});


        }else {
          String tmplFileName = "agg_stat_event_equalize.info_tmpl";
          info.put("agg_stat_input", dataFile + "_ee_input");
          info.put("agg_stat_output", dataFile + ".ee");
          MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + "/" + tmplFileName, eeInfo, info);
          String scriptName = mvBatch.getRworkFolder() + "/include/agg_stat_event_equalize.R";


          mvBatch.getPrintStream().print("Running '" + job.getRscript() + " " + scriptName + "'");
          mvResponse = MVUtil.runRscript(job.getRscript(), scriptName, new String[]{eeInfo});
        }
        stopWatch.stop();

        if (mvResponse.getInfoMessage() != null) {
          mvBatch.getPrintStream().print(mvResponse.getInfoMessage());
        }
        if (mvResponse.getErrorMessage() != null) {
          mvBatch.getPrintStream().print(mvResponse.getErrorMessage());
        }
        mvBatch.getPrintStream().print("Rscript time " + stopWatch.getFormattedTotalDuration());
      }

    }

    //  run the plot SQL against the database connection
    String scriptFileName;

    if (job.isModeJob()) {
      if (job.isModeRatioJob()) {
        dataFile = dataFile + ".agg_stat_bootstrap";
        //perform event equalisation against previously calculated cases, ratio statistic calculation and bootstrapping
        if(info.getClass() == TreeMap.class){
          scriptFileName =  "/metcalcpy/agg_stat_bootstrap.py";
        }else {
          scriptFileName = "include/agg_stat_bootstrap.R";
        }
      } else {
        dataFile = dataFile + ".agg_stat_eqz";
        //perform event equalisation against previously calculated cases
        if(info.getClass() == TreeMap.class){
          scriptFileName =  "/metcalcpy/agg_stat_eqz.py";
        }else {
          scriptFileName = "include/agg_stat_eqz.R";
        }

      }
    } else if (job.isMtdJob()) {
      if (job.isMtdRatioJob()) {
        dataFile = dataFile + ".agg_stat_bootstrap";
        //perform event equalisation against previously calculated cases, ratio statistic calculation and bootstrapping
        if(info.getClass() == TreeMap.class){
          scriptFileName =  "/metcalcpy/agg_stat_bootstrap.py";
        }else {
          scriptFileName = "include/agg_stat_bootstrap.R";
        }

      } else {
        dataFile = dataFile + ".agg_stat_eqz";
        //perform event equalisation against previously calculated cases
        if(info.getClass() == TreeMap.class){
          scriptFileName =  "/metcalcpy/agg_stat_eqz.py";
        }else {
          scriptFileName = "include/agg_stat_eqz.R";
        }
      }
    } else {
      dataFile = dataFile + ".agg_stat";
      //perform event equalisation , statistic calculation and bootstrapping
      if (job.getPlotTmpl().contains("eclv")) {
        scriptFileName = "include/agg_eclv.R";
      } else {
        scriptFileName = "include/agg_stat.R";
      }
    }

    if(info.getClass() == TreeMap.class){
      rScriptFile = scriptFileName;
    }else {
      rScriptFile = mvBatch.getRworkFolder() + scriptFileName;
    }

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
  public boolean runRscript(MVPlotJob job, Map<String, Object> info) {
    String aggInfo;
    String aggOutput;
    String tmplFileName;
    if (job.isModeJob() || job.isMtdJob()) {
      if (job.isModeRatioJob() || job.isMtdRatioJob()) {
        aggInfo = dataFile.replaceFirst("\\.data.agg_stat_bootstrap$",
                ".agg_stat_bootstrap.info");
        aggOutput = dataFile.replaceFirst("\\.agg_stat_bootstrap$", "");
        info.put("agg_stat_input_ee",
                dataFile.replaceFirst("\\.agg_stat_bootstrap$", ".ee"));
        tmplFileName = "agg_stat_bootstrap.info_tmpl";
      } else {
        aggInfo = dataFile.replaceFirst("\\.data.agg_stat_eqz$", ".agg_stat_eqz.info");
        aggOutput = dataFile.replaceFirst("\\.agg_stat_eqz$", "");
        info
                .put("agg_stat_input_ee", dataFile.replaceFirst("\\.agg_stat_eqz$", ".ee"));
        tmplFileName = "agg_stat_eqz.info_tmpl";
      }

    } else {
      aggInfo = dataFile.replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
      aggOutput = dataFile.replaceFirst("\\.agg_stat$", "");
      tmplFileName = "agg_stat.info_tmpl";
    }
    File fileAggOutput = new File(aggOutput);

    info.put("agg_stat_input", dataFile);
    info.put("agg_stat_output", aggOutput);
    MvResponse mvResponse = new MvResponse();

    try {
      MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + tmplFileName, aggInfo,
              info);
      //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting

      if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
        fileAggOutput.getParentFile().mkdirs();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        mvBatch.getPrintStream().println("\nRunning " + job.getRscript() + " " + rScriptFile);

        mvResponse = MVUtil.runRscript(job.getRscript(),
                rScriptFile,
                new String[]{aggInfo});
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

  @Override
  public boolean runPythonScript(MVPlotJob job, Map<String, Object> info) {
    String aggInfo;
    String aggOutput;
    if (job.isModeJob() || job.isMtdJob()) {
      if (job.isModeRatioJob() || job.isMtdRatioJob()) {
        aggInfo = dataFile.replaceFirst("\\.data.agg_stat_bootstrap$",
                ".agg_stat_bootstrap.info");
        aggOutput = dataFile.replaceFirst("\\.agg_stat_bootstrap$", "");
        info.put("agg_stat_input_ee",
                dataFile.replaceFirst("\\.agg_stat_bootstrap$", ".ee"));
      } else {
        aggInfo = dataFile.replaceFirst("\\.data.agg_stat_eqz$", ".agg_stat_eqz.info");
        aggOutput = dataFile.replaceFirst("\\.agg_stat_eqz$", "");
        info
                .put("agg_stat_input_ee", dataFile.replaceFirst("\\.agg_stat_eqz$", ".ee"));
      }

    } else {
      aggInfo = dataFile.replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
      aggOutput = dataFile.replaceFirst("\\.agg_stat$", "");
    }
    File fileAggOutput = new File(aggOutput);

    info.put("agg_stat_input", dataFile);
    info.put("agg_stat_output", aggOutput);
    MvResponse mvResponse = new MvResponse();

    try {
      createYmlFile(aggInfo, info);

      if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
        fileAggOutput.getParentFile().mkdirs();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        if(job.isModeJob() || job.isMtdJob()){
          mvBatch.getPrintStream().println("\nRunning "
                  + mvBatch.getPython()
                  + " "
                  + mvBatch.getMetCalcpyHome() + rScriptFile
                  + " "
                  + aggInfo);

          mvResponse = MVUtil.runRscript(mvBatch.getPython(),
                  mvBatch.getMetCalcpyHome() + rScriptFile,
                  new String[]{aggInfo},
                  new String[]{"PYTHONPATH=" + mvBatch.getMetCalcpyHome()});
        }else {

          mvBatch.getPrintStream().println("\nRunning "
                  + mvBatch.getPython()
                  + " "
                  + mvBatch.getMetCalcpyHome() + AGG_PYTHON_SCRIPT
                  + " "
                  + aggInfo);

          mvResponse = MVUtil.runRscript(mvBatch.getPython(),
                  mvBatch.getMetCalcpyHome() + AGG_PYTHON_SCRIPT,
                  new String[]{aggInfo},
                  new String[]{"PYTHONPATH=" + mvBatch.getMetCalcpyHome()});
        }
        stopWatch.stop();
        if (mvResponse.getInfoMessage() != null) {
          mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
        }
        if (mvResponse.getErrorMessage() != null) {
          mvBatch.getPrintStream().println(mvResponse.getErrorMessage());
        }
        mvBatch.getPrintStream().println("Python script execution time " + stopWatch.getFormattedTotalDuration());
      }
    } catch (IOException | StopWatchException e) {
      errorStream.print(e.getMessage());
    }


    return mvResponse.isSuccess();
  }


}

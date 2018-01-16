/**
 * RscriptAggStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
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
 * @version : 1.0 : 22/12/17 10:15 $
 */
public class RscriptAggStatManager extends RscriptStatManager {
  private static final PrintStream errorStream = IoBuilder.forLogger(MVUtil.class).setLevel(org.apache
                                                                                               .logging.log4j.Level.INFO)
                        .setMarker(new MarkerManager.Log4jMarker("ERROR"))
                        .buildPrintStream();

  public RscriptAggStatManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  public void prepareDataFileAndRscript(
                                           MVPlotJob job,MVOrderedMap mvMap,
                                           Map<String, String> info,
                                           List<String> listQuery) throws Exception {



    String fileName = MVUtil.buildTemplateString(job.getDataFileTmpl(),
                                                 MVUtil.addTmplValDep(job),
                                                                  job.getTmplMaps(),
                                                                  mvBatch.getPrintStream());

    dataFile = mvBatch.getDataFolder() + fileName;
    if (job.isModeJob() && job.getEventEqual()) {
      //run ee first
      //create sql query
      MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
      List<String> eventEqualizeSql = mvBatch.getDatabaseManager()
                                          .buildPlotModeEventEqualizeSQL(job,
                                                                         mvMap,
                                                                         mapPlotFixVal);

      for (String sql : eventEqualizeSql) {
        mvBatch.printSql(sql + "\n");
      }


      boolean success = false;
      for (int i = 0; i < job.getCurrentDBName().size(); i++) {
        success = mvBatch.getDatabaseManager()
                      .executeQueriesAndSaveToFile(eventEqualizeSql, dataFile + "_ee_input",
                                                   job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(),
                                                   job.getCurrentDBName().get(i), i == 0);
      }

      if (success) {
        String tmplFileName = "agg_stat_event_equalize.info_tmpl";
        info.put("agg_stat_input", dataFile + "_ee_input");
        info.put("agg_stat_output", dataFile + ".ee");
        String eeInfo = dataFile.replaceFirst("\\.data$", ".agg_stat_event_equalize.info");


        MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + "/" + tmplFileName, eeInfo,
                                    info);
        MVUtil.runRscript(job.getRscript(), mvBatch.getRworkFolder() +
                                                "/include/agg_stat_event_equalize.R",
                          new String[]{eeInfo}, mvBatch.getPrintStream());
      }

    }

    //  run the plot SQL against the database connection
    long intStartTime = new Date().getTime();



    String scriptFileName = null;

    if (job.isModeJob()) {
      if (job.isModeRatioJob()) {
        dataFile = dataFile + ".agg_stat_bootstrap";
        //perform event equalisation against previously calculated cases, ratio statistic calculation and bootstrapping
        scriptFileName = "include/agg_stat_bootstrap.R";
      } else {
        dataFile = dataFile + ".agg_stat_eqz";
        //perform event equalisation against previously calculated cases
        scriptFileName = "include/agg_stat_eqz.R";
      }
    } else {
      dataFile = dataFile + ".agg_stat";
      //perform event equalisation , statistic calculation and bootstrapping
      if(job.getPlotTmpl().contains("eclv")){
        scriptFileName = "include/agg_eclv.R";
      }else {
        scriptFileName = "include/agg_stat.R";
      }
    }


    rScriptFile = mvBatch.getRworkFolder() + scriptFileName;


    (new File(dataFile)).getParentFile().mkdirs();

    for (int i = 0; i < job.getCurrentDBName().size(); i++) {
      mvBatch.getDatabaseManager().executeQueriesAndSaveToFile(listQuery, dataFile,
                                                               job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(),
                                                               job.getCurrentDBName().get(i),
                                                               i == 0);
    }
    mvBatch.print("Query returned  plot_data rows in " + MVUtil.formatTimeSpan(
        new Date().getTime() - intStartTime));

  }

  @Override
  public boolean runRscript(MVPlotJob job, Map<String, String> info) {
    String aggInfo;
    String aggOutput;
    boolean success = false;
    String tmplFileName = null;
    if (job.isModeJob()) {
      if (job.isModeRatioJob()) {
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
    try {
      MVUtil.populateTemplateFile(mvBatch.getRtmplFolder() + tmplFileName, aggInfo,
                                  info);
      //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting
      if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
        fileAggOutput.getParentFile().mkdirs();

        success = MVUtil.runRscript(job.getRscript(),
                                    rScriptFile,
                                    new String[]{aggInfo},
                                    mvBatch.getPrintStream());

      }
    } catch (Exception e) {
      errorStream.print(e.getMessage());
    }


    return success;
  }

}

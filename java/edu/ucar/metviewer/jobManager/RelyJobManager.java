/**
 * RelyJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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
 * @version : 1.0 : 21/12/17 12:51 $
 */
public class RelyJobManager extends JobManager {

  public RelyJobManager(MVBatch mvBatch) {
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

      Map<String, Object> info = createInfoMap(job, intNumDepSeries);
      info.put("agg_pct", "TRUE");
      info.put("indy_var", "thresh_i");
      info.put("indy_list", "c(0, 0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9)");
      info.put("agg_stat1", "c(\"PSTD_CALIBRATION\", \"PSTD_BASER\", \"PSTD_NI\")");
      MVOrderedMap mapAggStatStatic = new MVOrderedMap();

      Object objFcstVar = job.getPlotFixVal().get("fcst_var");
      String[] listFcstVar;
      if (objFcstVar instanceof String[]) {
        listFcstVar = (String[]) job.getPlotFixVal().get("fcst_var");
      } else if (objFcstVar instanceof String) {
        listFcstVar = new String[]{(String) job.getPlotFixVal().get("fcst_var")};
      } else {
        MVOrderedMap mapFcstVar = (MVOrderedMap) job.getPlotFixVal().get("fcst_var");
        listFcstVar = (String[]) mapFcstVar.get(mapFcstVar.getKeyList()[0]);
      }
      mapAggStatStatic.put("fcst_var", listFcstVar[0]);

      MVOrderedMap mapDep1Plot = new MVOrderedMap();
      mapDep1Plot.put(listFcstVar[0], new String[]{"PSTD_CALIBRATION", "PSTD_BASER", "PSTD_NI"});
      info.put("dep1_plot", mapDep1Plot.getRDecl());


      RscriptStatManager rscriptStatManager = new RscriptAggStatManager(mvBatch);
      Map<String, Object> yamlInfo = null;
      if (job.getExecutionType().equals("Rscript")) {
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>(0));
        rscriptStatManager.runRscript(job, info);
      } else {
        yamlInfo = createYamlInfoMap(job);
        yamlInfo.put("indy_var", "thresh_i");
        List<String> indy_vals = new ArrayList<>();
        indy_vals.add("0");
        indy_vals.add("0.1");
        indy_vals.add("0.2");
        indy_vals.add("0.3");
        indy_vals.add("0.4");
        indy_vals.add("0.5");
        indy_vals.add("0.6");
        indy_vals.add("0.7");
        indy_vals.add("0.8");
        indy_vals.add("0.9");

        yamlInfo.put("indy_vals", indy_vals);
        List<String> list_stat = new ArrayList<>();
        list_stat.add("PSTD_CALIBRATION");
        list_stat.add("PSTD_BASER");
        list_stat.add("PSTD_NI");
        yamlInfo.put("list_stat_1", list_stat);
        Map<String, List<String>> fcst_var_val = new HashMap<>();
        for (String fcst_var : listFcstVar) {
          fcst_var_val.put(fcst_var, new ArrayList<>(list_stat));
        }
        yamlInfo.put("fcst_var_val_1",fcst_var_val);

        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, new ArrayList<>(0));
        rscriptStatManager.runPythonScript(job, yamlInfo);
        yamlInfo.remove("agg_stat_output");
        yamlInfo.remove("agg_stat_input");
      }


      rscriptStatManager = new RscriptNoneStatManager(mvBatch);
      if (job.getExecutionType().equals("Rscript")) {
        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, new ArrayList<>());
        info.put("data_file", dataFile);
        rscriptStatManager.runRscript(job, info);
      } else {
        if (yamlInfo == null) {
          yamlInfo = createYamlInfoMap(job);
        }
        yamlInfo.put("stat_input", dataFile);
        yamlInfo.put("summary_curves", job.getSummaryCurve());
        yamlInfo.put("add_skill_line", job.getAddSkillLine() ? "True" : "False");
        yamlInfo.put("add_noskill_line", job.getAddNoSkillLine() ? "True" : "False");
        yamlInfo.put("add_reference_line", job.getAddReferenceLine() ? "True" : "False");
        yamlInfo.put("rely_event_hist", job.getRelyEventHist().equals("TRUE") ? "True" : "False");
        yamlInfo.put("inset_hist", job.getInsetHist() ? "True" : "False");

        rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, new ArrayList<>());
        job.setPlotTmpl(this.getPythonScript());
        yamlInfo = this.addPlotConfigs(yamlInfo, job, intNumDepSeries);
        rscriptStatManager.runPythonScript(job, yamlInfo);
      }


    }

  }

  @Override
  protected String getPythonScript() {
    return "/plots/reliability_diagram/reliability.py";
  }

}

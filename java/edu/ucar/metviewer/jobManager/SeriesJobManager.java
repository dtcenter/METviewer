/**
 * SeriesJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.*;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.rscriptManager.RscriptAggStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptNoneStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptStatManager;
import edu.ucar.metviewer.rscriptManager.RscriptSumStatManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 13:13 $
 */
public class SeriesJobManager extends JobManager {


  public SeriesJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected void run(MVPlotJob job) throws ParseException, ValidationException, IOException, StopWatchException, DatabaseException {


    //  determine if the plots require data aggregation
    boolean isAggStat = job.isAggStat();
    boolean isCalcStat = job.isCalcStat();
    boolean isEE = job.getEventEqual();


    /*
     *  Build a plot for each permutation of <plot_fix> values
     */

    List<String> listQuery;
    AppDatabaseManager appDatabaseManager = mvBatch.getDatabaseManager();

    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap plotFixPerm : listPlotFixPerm) {
      job.setEventEqual(isEE);

      mvBatch
              .print("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");

      //    insert set values for this permutation
      MVOrderedMap fixTmplVal = buildPlotFixTmplVal(job.getTmplMaps(),
              plotFixPerm,
              appDatabaseManager.getDateFormat());
      job.setTmplVal(fixTmplVal);
      //  if the independent variable uses a dependency, populate the values
      MVPlotDep depIndy = job.getIndyDep();
      if (null != depIndy) {
        String strDep = "";
        if (job.getTmplVal().containsKey(depIndy.getDepVar())) {
          strDep = appDatabaseManager.getDateFormat().format(
                  MVUtil.parsePlotFormat(job.getTmplVal().getStr(depIndy.getDepVar())));
        }
        String[][] listIndy = MVPlotJobParser.parseIndyNode(depIndy.getSpec(), strDep);
        job.setIndyVal(listIndy[0]);
        job.setIndyLabel(listIndy[1]);
      }
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

      //if it is a model job with attribute stat  - validate
      if ((job.isModeJob() && !job.isModeRatioJob())
              || (job.isMtdJob() && !job.isMtdRatioJob())) {
        validateModeSeriesDefinition(job);
      }

      //  build the SQL statements for the current plot
      listQuery = appDatabaseManager.buildPlotSql(job, plotFixPerm, mvBatch.getPrintStreamSql());
      for (String sql : listQuery) {
        mvBatch.printSql(sql + "\n\n");
      }


      Map.Entry[] listSeries2Val;
      if (null != job.getSeries2Val()) {
        listSeries2Val = job.getSeries2Val().getOrderedEntriesForSqlSeries();
      } else {
        listSeries2Val = new Map.Entry[]{};
      }

      MVOrderedMap mapDep2Plot = (MVOrderedMap) job.getDepGroups()[0].get("dep2");
      Map.Entry[] listDep2Plot;
      if (null != mapDep2Plot) {
        listDep2Plot = mapDep2Plot.getOrderedEntries();
      } else {
        listDep2Plot = new Map.Entry[]{};
      }

      // calculate the number of plot curves

      int intNumDep2 = 0;
      for (Map.Entry aListDep2Plot : listDep2Plot) {
        intNumDep2 += ((String[]) aListDep2Plot.getValue()).length;
      }
      int intNumSeries1Perm = 1;
      Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSqlSeries();
      for (Map.Entry aListSeries1Val : listSeries1Val) {
        String[] listVal = (String[]) aListSeries1Val.getValue();
        intNumSeries1Perm *= listVal.length;
      }
      int intNumSeries2Perm = 1;
      for (Map.Entry aListSeries2Val : listSeries2Val) {
        intNumSeries2Perm *= ((String[]) aListSeries2Val.getValue()).length;
      }
      Map.Entry[] listDep1Plot = ((MVOrderedMap) job.getDepGroups()[0].get("dep1"))
              .getOrderedEntries();
      int intNumDep1 = getNumberPlotCurves(listDep1Plot);
      int intNumDep1Series = intNumDep1 * intNumSeries1Perm;
      int intNumDep2Series = intNumDep2 * intNumSeries2Perm;
      int intNumDepSeries = getNumDepSeries(intNumDep1Series, intNumDep2Series, job);
      intNumDepSeries = intNumDepSeries + job.getDiffSeries1Count();
      intNumDepSeries = intNumDepSeries + job.getDiffSeries2Count();


      //  validate the number of formatting elements
      if (intNumDepSeries > MVUtil.parseRCol(job.getPlotDisp()).length) {
        throw new ValidationException("length of plot_disp differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0
              && intNumDepSeries > MVUtil.parseRCol(job.getOrderSeries()).length) {
        throw new ValidationException("length of order_series differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getColors()).length) {
        throw new ValidationException("length of colors differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getPch()).length) {
        throw new ValidationException("length of pch differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getType()).length) {
        throw new ValidationException("length of type differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getLty()).length) {
        throw new ValidationException("length of lty differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (intNumDepSeries > MVUtil.parseRCol(job.getLwd()).length) {
        throw new ValidationException("length of lwd differs from number of series ("
                + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty()
              && intNumDepSeries > MVUtil.parseRCol(job.getLegend()).length) {
        throw new ValidationException("length of legend differs from number of series ("
                + intNumDepSeries + ")");
      }

      if (intNumDepSeries > MVUtil.parseRCol(job.getShowSignif()).length) {
        throw new ValidationException("length of show_signif differs from number of series ("
                + intNumDepSeries + ")");
      }
      validateNumDepSeries(job, intNumDepSeries);

      //validate revision_series option
      if (job.getPlotTmpl().startsWith("revision") && isAggStat) {
        throw new ValidationException("revision series option only available for Summary statistics");
      }
      if (job.getPlotTmpl().startsWith("revision") && !job.getIndyVar().equals("fcst_valid_beg")) {
        throw new ValidationException("revision series option only available for independent variable "
                + "'fcst_valid_beg'");
      }
      if (job.getPlotTmpl().startsWith("revision") && job.getDiffSeries1Count() > 0) {
        throw new ValidationException("revision series don't produce derived curves");
      }
      if (job.getPlotTmpl().startsWith("revision") && job.getSeries2Val().size() > 0) {
        throw new ValidationException("revision series don't produce plots on Y2 axis");
      }

      Map<String, Object> info = createInfoMap(job, intNumDepSeries);

      Map<String, Object> yamlInfo = new TreeMap<>();
      yamlInfo.put("method", job.getAggBootCI());
      yamlInfo.put("num_iterations", MVUtil.isNumeric(job.getAggBootRepl()) ? Integer.parseInt(job.getAggBootRepl()) : 1);
      yamlInfo.put("num_threads", -1);
      yamlInfo.put("alpha", MVUtil.isNumeric(job.getCIAlpha()) ? Double.parseDouble(job.getCIAlpha()) : 0.05);
      yamlInfo.put("random_seed", job.getAggBootRandomSeed().equals("NA") ? null : Integer.parseInt(job.getAggBootRandomSeed()));
      yamlInfo.put("line_type", job.getLineType());
      yamlInfo.put("indy_var", job.getIndyVar());
      yamlInfo.put("event_equal", job.getEventEqual() ? "True" : "False");
      String[] listIndyValFmt = job.getIndyVal();
      if (job.getIndyVar().matches(".*_hour")) {
        for (int i = 0; i < listIndyValFmt.length; i++) {
          listIndyValFmt[i] = String.valueOf(Integer.parseInt(listIndyValFmt[i]));
        }
      }
      yamlInfo.put("indy_vals", listIndyValFmt);
      yamlInfo.put("series_val_1", job.getSeries1Val().getYamlDeclSeries());
      yamlInfo.put("series_val_2", job.getSeries2Val().getYamlDeclSeries());
      List<String> listAggStats1 = new ArrayList<>();
      List<String> listAggStats2 = new ArrayList<>();
      MVOrderedMap mapDep;
      if (job.getDepGroups().length > 0) {
        mapDep = job.getDepGroups()[0];
      } else {
        mapDep = new MVOrderedMap();
      }
      String strFcstVar = "";
      for (int intY = 1; intY <= 2; intY++) {
        MVOrderedMap mapDepY = (MVOrderedMap) mapDep.get("dep" + intY);
        if (mapDepY != null) {
          MVOrderedMap mapStat = new MVOrderedMap();

          String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(mapDepY);
          for (String[] aListFcstVarStat : listFcstVarStat) {
            String strFcstVarCur = aListFcstVarStat[0];
            if (strFcstVar.isEmpty()) {
              strFcstVar = strFcstVarCur;
            } else if (!strFcstVar.equals(strFcstVarCur)) {
              //check if this is a mode/mtd/agg/sum stat job
              if (job.isModeJob() || job.isMtdJob() || isAggStat || job.getEventEqual()) {
                throw new ValidationException("fcst_var must remain constant for MODE, MTD, Aggregation "
                        + "statistics, Event Equalizer");
              }
            }
            mapStat.put(aListFcstVarStat[1], aListFcstVarStat[0]);
          }
          if (1 == intY) {
            listAggStats1.addAll(Arrays.asList(mapStat.getKeyList()));
          }else {
            listAggStats2.addAll(Arrays.asList(mapStat.getKeyList()));
          }
        }
      }

      yamlInfo.put("list_stat_1", MVUtil.printYamlCol(listAggStats1.toArray(new String[0])));
      yamlInfo.put("list_stat_2", MVUtil.printYamlCol(listAggStats2.toArray(new String[0])));
      yamlInfo.put("fcst_var_val_1", mapDep.get("dep1"));
      yamlInfo.put("fcst_var_val_2", mapDep.get("dep2"));
      MVOrderedMap mapAggStatStatic = new MVOrderedMap();
      mapAggStatStatic.put("fcst_var", strFcstVar);
      yamlInfo.put("list_static_val", mapAggStatStatic);
      yamlInfo.put("fixed_vars_vals_input", job.getPlotFixValEq());
      String diffSeriesTemplate = MVUtil.buildTemplateInfoString(job.getDiffSeries1(), MVUtil.addTmplValDep(job),
              job.getTmplMaps(), mvBatch.getPrintStream());

      yamlInfo.put("derived_series_1", MVUtil.getDiffSeriesArr(diffSeriesTemplate));

      diffSeriesTemplate = MVUtil.buildTemplateInfoString(job.getDiffSeries2(), MVUtil.addTmplValDep(job),
              job.getTmplMaps(), mvBatch.getPrintStream());

      yamlInfo.put("derived_series_2", MVUtil.getDiffSeriesArr(diffSeriesTemplate));


      RscriptStatManager rscriptStatManager = null;
      if (job.isModeJob() || job.isMtdJob() || isAggStat) {
        rscriptStatManager = new RscriptAggStatManager(mvBatch);
      } else if (isCalcStat) {
        rscriptStatManager = new RscriptSumStatManager(mvBatch);
      }

      //run summary or agg stats Rscripts - if needed
      if (rscriptStatManager != null) {
        if (job.isMtdJob()) {
          rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
          rscriptStatManager.runRscript(job, info);
        }else {
          rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, yamlInfo, listQuery);
          rscriptStatManager.runPythonScript(job, yamlInfo);
        }

        //rscriptStatManager.runPythonScript(job, yamlInfo);
        //  turn off the event equalizer
        job.setEventEqual(Boolean.FALSE);
        info.put("event_equal", "FALSE");
        yamlInfo.put("event_equal", "False");
        listQuery.clear();
      }

      //run script for revision series if needed
      rscriptStatManager = new RscriptNoneStatManager(mvBatch);


      //run main Rscript
      String dataFileName = mvBatch.getDataFolder()
              + MVUtil.buildTemplateString(job.getDataFileTmpl(),
              MVUtil.addTmplValDep(job),
              job.getTmplMaps(),
              mvBatch.getPrintStream());
      File dataFile = new File(dataFileName);
      if (!listQuery.isEmpty()) {
        dataFile.getParentFile().mkdirs();
        for (int i = 0; i < job.getCurrentDBName().size(); i++) {
          MvResponse mvResponse =
                  appDatabaseManager.executeQueriesAndSaveToFile(listQuery,
                          dataFileName,
                          job.isCalcStat(),
                          job.getCurrentDBName().get(i),
                          i == 0);
          if (mvResponse.getInfoMessage() != null) {
            mvBatch.getPrintStream().println(mvResponse.getInfoMessage());
          }
        }

      }

      rscriptStatManager.prepareDataFileAndRscript(job, plotFixPerm, info, listQuery);
      info.put("data_file", dataFileName);
      yamlInfo.put("data_file", dataFileName);

      rscriptStatManager.runRscript(job, info);

    }

  }

  private void validateModeSeriesDefinition(MVPlotJob job) throws ValidationException {
    MVOrderedMap[] listDep = job.getDepGroups();
    for (int dep = 1; dep <= 2; dep++) {
      String[][] listFcstVarStat
              = MVUtil.buildFcstVarStatList((MVOrderedMap) listDep[0].get("dep" + dep));

      for (String[] aListFcstVarStat : listFcstVarStat) {
        String[] arr = aListFcstVarStat[1].split("_");
        String stat = arr[arr.length - 2];
        String type = arr[arr.length - 1];
        //validate for all attr stats except for those
        if (!stat.equals("CNT")
                && !stat.equals("CNTSUM")
                && !stat.equals("MAXINT")
                && !stat.equals("MAXINTF")
                && !stat.equals("MAXINTO")
                && type.startsWith("D")) {

          if (!type.equals("DCM")) {
            throw new ValidationException("Incorrect series definition. Stat "
                    + stat + " can only have Cluster and Matched for Diff type");
          }
        }
      }
    }
  }

}

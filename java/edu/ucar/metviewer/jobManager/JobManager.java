/**
 * JobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

import edu.ucar.metviewer.DatabaseException;
import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVDataTable;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.ValidationException;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 09:58 $
 */
public abstract class JobManager {

  public static final String FALSE = "FALSE";
  protected MVBatch mvBatch;
  protected MVOrderedMap[] listPlotFixPerm;

  protected JobManager(MVBatch mvBatch) {
    this.mvBatch = mvBatch;
  }

  private static final Map<String, String> symbolsRtoPython = new HashMap<>();
  private static final Map<String, String> linesRtoPython = new HashMap<>();

  static {
    symbolsRtoPython.put("20", ".");
    symbolsRtoPython.put("19", "o");
    symbolsRtoPython.put("15", "s");
    symbolsRtoPython.put("17", "^");
    symbolsRtoPython.put("18", "d");
    symbolsRtoPython.put("1", "H");

    linesRtoPython.put("1", "-");
    linesRtoPython.put("2", "--");
    linesRtoPython.put("3", ":");
    //NEED TO CHANE LATER: use '-' for others
    linesRtoPython.put("4", "-:");
    linesRtoPython.put("5", "-");
    linesRtoPython.put("6", "-");
  }

  public void runJob(MVPlotJob job) {
    try {
      //  build a list of fixed value permutations for all plots
      listPlotFixPerm = buildPlotFixValList(job.getPlotFixVal());
      run(job);
    } catch (ParseException | ValidationException | IOException | StopWatchException |
            DatabaseException e) {
      mvBatch.print("Failed to create a plot. " + e.getMessage());
    }
  }

  protected abstract void run(MVPlotJob job) throws ParseException, ValidationException, IOException, StopWatchException, DatabaseException;


  /**
   * Build the list of plot_fix field/value permutations for all jobs
   *
   * @param mapPlotFixVal map of field/value pairs to permute
   * @return list of permutations
   */
  protected MVOrderedMap[] buildPlotFixValList(final MVOrderedMap mapPlotFixVal) {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap[] list = {new MVOrderedMap()};
    if (0 < mapPlotFixVal.size()) {
      MVDataTable tabPlotFixPerm = MVUtil.permute(mapPlotFixVal);
      list = tabPlotFixPerm.getRows();
    }

    return list;
  }


  protected MVOrderedMap buildPlotFixTmplVal(
          final MVOrderedMap tmplMaps,
          final MVOrderedMap plotFixPerm,
          final SimpleDateFormat dbFormat) throws ParseException {
    MVOrderedMap result = new MVOrderedMap();
    for (Map.Entry fixValEntry : plotFixPerm.getOrderedEntries()) {
      String strFixVar = fixValEntry.getKey().toString();
      String strFixVal = fixValEntry.getValue().toString();
      MVOrderedMap templates = (MVOrderedMap) tmplMaps.get(strFixVar);
      if (templates != null && templates.containsKey(strFixVal)) {
        strFixVal = templates.getStr(strFixVal);
      } else {
        Matcher matDateRange = mvBatch.getDatabaseManager().getDateRangePattern()
                .matcher(strFixVal);
        if (matDateRange.matches()) {
          strFixVal = MVUtil.formatPlotFormat(dbFormat.parse(matDateRange.group(2)));
        }
      }
      result.putStr(strFixVar, strFixVal);

    }
    return result;
  }

  protected int getNumberPlotCurves(Map.Entry[] listDep1Plot) {
    int intNumDep1 = 0;
    for (Map.Entry aListDep1Plot : listDep1Plot) {
      intNumDep1 += ((String[]) aListDep1Plot.getValue()).length;
    }
    return intNumDep1;
  }

  protected void validateNumDepSeries(MVPlotJob job, int intNumDepSeries) throws ValidationException {
    if (intNumDepSeries != MVUtil.parseRCol(job.getPlotCI()).length) {
      throw new ValidationException("length of plot_ci differs from number of series ("
              + intNumDepSeries + ")");
    }
    if (intNumDepSeries != MVUtil.parseRCol(job.getConSeries()).length) {
      throw new ValidationException("length of con_series differs from number of series ("
              + intNumDepSeries + ")");
    }
  }

  protected int getNumDepSeries(int intNumDep1Series, int intNumDep2Series, MVPlotJob job) {
    return intNumDep1Series + intNumDep2Series;
  }

  protected Map<String, Object> createInfoMap(MVPlotJob job, int intNumDepSeries) throws ValidationException {

    MVOrderedMap mapDep;
    if (job.getDepGroups().length > 0) {
      mapDep = job.getDepGroups()[0];
    } else {
      mapDep = new MVOrderedMap();
    }

    // format the indy values, if fcst_hour or valid_hour is being used
    String[] listIndyValFmt = job.getIndyVal();
    if (job.getIndyVar().matches(".*_hour")) {
      for (int i = 0; i < listIndyValFmt.length; i++) {
        listIndyValFmt[i] = String.valueOf(Integer.parseInt(listIndyValFmt[i]));
      }
    }

    String[] listIndyLabel = job.getIndyLabel();
    if (!"0".equals(job.getXtlabFreq())) {
      int intDecim = 0;
      try {
        intDecim = Integer.parseInt(job.getXtlabFreq());
        if (1 > intDecim) {
          throw new Exception();
        }
      } catch (Exception e) {
        throw new ValidationException("unable to parse xtlab_decim value " + job.getXtlabFreq());
      }
      listIndyLabel = decimate(listIndyLabel, intDecim);
    }

    MVOrderedMap mapAggStatStatic = new MVOrderedMap();
    String strFcstVar = "";
    List<String> listAggStats1 = new ArrayList<>();
    List<String> listAggStats2 = new ArrayList<>();
    for (int intY = 1; intY <= 2; intY++) {
      MVOrderedMap mapDepY = (MVOrderedMap) mapDep.get("dep" + intY);
      if (mapDepY != null && !mapDepY.isEmpty()) {
        strFcstVar = "";
        MVOrderedMap mapStat = new MVOrderedMap();

        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(mapDepY);
        for (String[] aListFcstVarStat : listFcstVarStat) {
          String strFcstVarCur = aListFcstVarStat[0];
         if (strFcstVar.isEmpty() ) {
            strFcstVar = strFcstVarCur;
          } else if (!strFcstVar.equals(strFcstVarCur)) {
            //check if this is a mode/mtd/agg/sum stat job
            boolean isAggStat = job.isAggStat();
            if (job.isModeJob() || job.isMtdJob() || isAggStat || job.getEventEqual()) {
              throw new ValidationException("fcst_var must remain constant for MODE, MTD, Aggregation "
                      + "statistics, Event Equalizer");
            }
          }
          mapStat.put(aListFcstVarStat[1], aListFcstVarStat[0]);
        }
        if (1 == intY) {
          listAggStats1.addAll(Arrays.asList(mapStat.getKeyList()));
        } else if (2 == intY) {
          listAggStats2.addAll(Arrays.asList(mapStat.getKeyList()));
        }
      }
    }

    mapAggStatStatic.put("fcst_var", strFcstVar);
    MVOrderedMap mapTmplValsPlot = MVUtil.addTmplValDep(job);

    String strTitle = MVUtil.buildTemplateInfoString(job.getTitleTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String strXLabel = MVUtil.buildTemplateInfoString(job.getXLabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String strY1Label = MVUtil.buildTemplateInfoString(job.getY1LabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String strY2Label = MVUtil.buildTemplateInfoString(job.getY2LabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String strCaption = MVUtil.buildTemplateInfoString(job.getCaptionTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String diffSeries1 = MVUtil.buildTemplateInfoString(job.getDiffSeries1(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String diffSeries2 = MVUtil.buildTemplateInfoString(job.getDiffSeries2(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());

    MVOrderedMap mapDep1Plot = (MVOrderedMap) mapDep.get("dep1");
    MVOrderedMap mapDep2Plot = (MVOrderedMap) mapDep.get("dep2");

    Map<String, Object> info = new HashMap<>();

    //  populate the plot settings in the R script template
    info.put("r_work", mvBatch.getRworkFolder());
    info.put("indy_var", job.getIndyVar());
    info.put("indy_list",
            0 < job.getIndyVal().length ? MVUtil.printRCol(listIndyValFmt, true) : "c()"
    );
    info.put("indy_label",
            0 < listIndyLabel.length ? MVUtil.printRCol(listIndyLabel, true) : "c()"
    );

    // check if all IndyPlotVals are empty strings
    Set<String> indyPlotValsSet = new HashSet<>(Arrays.asList(job.getIndyPlotVal()));
    boolean allEmpty = indyPlotValsSet.size() == 0 || (indyPlotValsSet.size() == 1 && job.getIndyPlotVal()[0].isEmpty());
    info.put("indy_plot_val", !allEmpty ? MVUtil.printRCol(job.getIndyPlotVal(), false) : "c()");
    info.put("dep1_plot", null != mapDep1Plot ? mapDep1Plot.getRDecl() : "c()");
    info.put("dep2_plot", null != mapDep2Plot ? mapDep2Plot.getRDecl() : "c()");
    info.put("agg_list", new MVOrderedMap().getRDecl());
    info.put("series1_list", job.getSeries1Val().getRDeclSeries());
    info.put("series2_list", job.getSeries2Val().getRDeclSeries());
    info.put("series_nobs", job.getSeriesNobs().getRDecl());
    info.put("dep1_scale", job.getDep1Scale().getRDecl());
    info.put("dep2_scale", job.getDep2Scale().getRDecl());

    info.put("plot_title", strTitle);
    info.put("x_label", strXLabel);
    info.put("y1_label", strY1Label);
    info.put("y2_label", strY2Label);
    info.put("plot_caption", strCaption);
    info.put("plot_cmd", job.getPlotCmd());
    info.put("event_equal", job.getEventEqual() ? "TRUE" : FALSE);
    info.put("vert_plot", job.getVertPlot() ? "TRUE" : FALSE);
    info.put("equalize_by_indep", job.getEqualizeByIndep() ? "TRUE" : FALSE);
    info.put("x_reverse", job.getXReverse() ? "TRUE" : FALSE);
    info.put("show_nstats", job.getShowNStats() ? "TRUE" : FALSE);
    info.put("indy1_stagger", job.getIndy1Stagger() ? "TRUE" : FALSE);
    info.put("indy2_stagger", job.getIndy2Stagger() ? "TRUE" : FALSE);
    info.put("grid_on", job.getGridOn() ? "TRUE" : FALSE);
    info.put("sync_axes", job.getSyncAxes() ? "TRUE" : FALSE);
    info.put("dump_points1", job.getDumpPoints1() ? "TRUE" : FALSE);
    info.put("dump_points2", job.getDumpPoints2() ? "TRUE" : FALSE);
    info.put("log_y1", job.getLogY1() ? "TRUE" : FALSE);
    info.put("log_y2", job.getLogY2() ? "TRUE" : FALSE);
    info
            .put("variance_inflation_factor", job.getVarianceInflationFactor() ? "TRUE" : FALSE);
    info.put("plot_stat", job.getPlotStat());
    info.put("series1_diff_list", diffSeries1);
    info.put("series2_diff_list", diffSeries2);

    //  populate the formatting information in the R script template
    MVUtil.populatePlotFmtTmpl(info, job);


    //  replace the template tags with the template values for the current plot
    info.put("plot_ci",
            job.getPlotCI().isEmpty()
                    ? MVUtil.printRCol(MVUtil.rep("none", intNumDepSeries), false) : job.getPlotCI()
    );
    info.put("plot_disp", job.getPlotDisp().isEmpty() ? MVUtil.printRCol(
            MVUtil.rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
    info.put("show_signif", job.getShowSignif().isEmpty() ? MVUtil.printRCol(
            MVUtil.rep("TRUE", intNumDepSeries)) : job.getShowSignif());
    info.put("order_series",
            job.getOrderSeries().isEmpty()
                    ? MVUtil.printRCol(MVUtil.repPlusOne(1, intNumDepSeries)) : job.getOrderSeries()
    );
    info.put("colors",
            job.getColors().isEmpty() ? "rainbow(" + intNumDepSeries + ")" : job.getColors()
    );
    info.put("pch",
                    job.getPch().isEmpty() ? MVUtil.printRCol(
                            MVUtil.rep(20, intNumDepSeries)) : job.getPch());
    info.put("type", job.getType().isEmpty() ? MVUtil.printRCol(
            MVUtil.rep("b", intNumDepSeries)) : job.getType());
    info.put("lty",
                    job.getLty().isEmpty() ? MVUtil.printRCol(
                            MVUtil.rep(1, intNumDepSeries)) : job.getLty());
    info.put("lwd",
                    job.getLwd().isEmpty() ? MVUtil.printRCol(
                            MVUtil.rep(1, intNumDepSeries)) : job.getLwd());
    info.put("con_series", job.getConSeries().isEmpty() ? MVUtil.printRCol(
            MVUtil.rep(0, intNumDepSeries)) : job.getConSeries());
    info.put("legend", job.getLegend().isEmpty() ? "c()" : job.getLegend());
    info.put("y1_lim", job.getY1Lim().isEmpty() ? "c()" : job.getY1Lim());
    info.put("x1_lim", job.getX1Lim().isEmpty() ? "c()" : job.getX1LimR());
    info.put("y1_bufr", job.getY1Bufr().isEmpty() ? "0" : job.getY1Bufr());
    info.put("y2_lim", job.getY2Lim().isEmpty() ? "c()" : job.getY2Lim());
    info.put("y2_bufr", job.getY2Bufr().isEmpty() ? "0" : job.getY2Bufr());
    info.put("pos", job.getTaylorVoc() ? "TRUE" : FALSE);
    info.put("show_gamma", job.getTaylorShowGamma() ? "TRUE" : FALSE);
    info.put("add_skill_line", job.getAddSkillLine() ? "TRUE" : FALSE);
    info.put("add_noskill_line", job.getAddNoSkillLine() ? "TRUE" : FALSE);
    info.put("add_reference_line", job.getAddReferenceLine() ? "TRUE" : FALSE);
    info.put("add_point_thresholds", job.getAddPointThresholds() ? "TRUE" : FALSE);
    info.put("reverse_connection_order", job.getReverseConnectionOrder() ? "TRUE" : FALSE);
    info.put("plot_type", job.getPlotType());
    info.put("legend_ncol", job.getLegendNcol());
    info.put("legend_size", job.getLegendSize());
    info.put("legend_box", job.getLegendBox());
    info.put("legend_inset", job.getLegendInset());
    info.put("summary_curves", job.getSummaryCurveRformat());
    info.put("roc_pct", job.getRocPct() ? "TRUE" : FALSE);
    info.put("roc_ctc", job.getRocCtc() ? "TRUE" : FALSE);
    info.put("sum_ctc", job.getCalcCtc() ? "TRUE" : FALSE);
    info.put("sum_sl1l2", job.getCalcSl1l2() ? "TRUE" : FALSE);
    info.put("sum_grad", job.getCalcGrad() ? "TRUE" : FALSE);
    info.put("sum_vl1l2", job.getCalcVl1l2() ? "TRUE" : FALSE);
    info.put("sum_val1l2", job.getCalcVal1l2() ? "TRUE" : FALSE);
    info.put("sum_sal1l2", job.getCalcSal1l2() ? "TRUE" : FALSE);
    info.put("eveq_dis", job.getEveqDis() ? "TRUE" : FALSE);
    info.put("indy_var", job.getIndyVar());
    info.put("indy_list",
            0 < listIndyValFmt.length ? MVUtil.printRCol(listIndyValFmt, true) : "c()");
    info.put("series1_list", job.getSeries1Val().getRDeclSeries());
    info.put("series2_list", job.getSeries2Val().getRDeclSeries());
    info.put("sum_stat_static", mapAggStatStatic.getRDecl());
    info.put("append_to_file", FALSE);

    info.put("working_dir", mvBatch.getRworkFolder() + "/include");
    info.put("equalize_by_indep", job.getEqualizeByIndep() ? "TRUE" : FALSE);
    info.put("fix_val_list_eq", job.getPlotFixValEq().getRDecl());
    info.put("fix_val_list", job.getPlotFixVal().getRDecl());

    info.put("dep1_plot", null != mapDep1Plot ? mapDep1Plot.getRDecl() : "c()");
    info.put("dep2_plot", null != mapDep2Plot ? mapDep2Plot.getRDecl() : "c()");
    info.put("agg_ctc", job.getAggCtc() ? "TRUE" : FALSE);
    info.put("agg_mctc", job.getAggMctc() ? "TRUE" : FALSE);
    info.put("agg_nbrctc", job.getAggNbrCtc() ? "TRUE" : FALSE);
    info.put("agg_sl1l2", job.getAggSl1l2() ? "TRUE" : FALSE);
    info.put("agg_sal1l2", job.getAggSal1l2() ? "TRUE" : FALSE);
    info.put("agg_grad", job.getAggGrad() ? "TRUE" : FALSE);
    info.put("agg_nbrcnt", job.getAggNbrCnt() ? "TRUE" : FALSE);
    info.put("agg_ssvar", job.getAggSsvar() ? "TRUE" : FALSE);
    info.put("agg_vl1l2", job.getAggVl1l2() ? "TRUE" : FALSE);
    info.put("agg_val1l2", job.getAggVal1l2() ? "TRUE" : FALSE);
    info.put("agg_pct", job.getAggPct() ? "TRUE" : FALSE);
    info.put("agg_ecnt", job.getAggEcnt() ? "TRUE" : FALSE);
    info.put("agg_rps", job.getAggRps() ? "TRUE" : FALSE);
    info.put("cache_agg_stat", job.getCacheAggStat() ? "TRUE" : FALSE);

    info.put("boot_repl", job.getAggBootRepl());
    info.put("boot_random_seed", job.getAggBootRandomSeed());
    info.put("boot_ci", job.getAggBootCI());
    info.put("ci_alpha", job.getCIAlpha());
    info.put("agg_stat1",
            MVUtil.printRCol(listAggStats1.toArray(new String[listAggStats1.size()]), true));
    info.put("agg_stat2", MVUtil.printRCol(
            listAggStats2.toArray(new String[listAggStats2.size()]),
            true));
    info.put("agg_stat_static", mapAggStatStatic.getRDecl());
    info.put("cl_step", "0.05");
    info.put("normalized_histogram", job.getNormalizedHistogram() ? "TRUE" : FALSE);
    info.put("color_palette", job.getColorPalette());
    info.put("contour_intervals", String.valueOf(job.getContourIntervals()));
    info.put("reverse_x", job.getReverseX() ? "TRUE" : FALSE);
    info.put("reverse_y", job.getReverseY() ? "TRUE" : FALSE);
    info.put("add_color_bar", job.getAddColorBar() ? "TRUE" : FALSE);
    info.put("add_contour_overlay", job.getAddContourOverlay() ? "TRUE" : FALSE);
    info.put("contour_diff", FALSE);
    info.put("revision_run", job.getRevisionRun() ? "TRUE" : FALSE);
    info.put("revision_ac", job.getRevisionAc() ? "TRUE" : FALSE);
    info.put("ensss_pts", job.getEnsSsPts());
    info.put("ensss_pts_disp", job.getEnsSsPtsDisp() ? "TRUE" : FALSE);


    return info;
  }

  protected Map<String, Object> createYamlInfoMap(MVPlotJob job) throws ValidationException {
    Map<String, Object> yamlInfo = new TreeMap<>();
    yamlInfo.put("method", job.getAggBootCI());
    yamlInfo.put("num_iterations", MVUtil.isNumeric(job.getAggBootRepl()) ? Integer.parseInt(job.getAggBootRepl()) : 1);
    yamlInfo.put("num_threads", -1);
    yamlInfo.put("random_seed", job.getAggBootRandomSeed().equals("NA") ? null : Integer.parseInt(job.getAggBootRandomSeed()));
    yamlInfo.put("line_type", job.getLineType().equals("N/A") ? "None" : job.getLineType());
    yamlInfo.put("indy_var", job.getIndyVar());
    yamlInfo.put("event_equal", job.getEventEqual() ? "True" : "False");
    yamlInfo.put("circular_block_bootstrap", job.getCircularBlockBootstrap() ? "True" : "False");
    String[] listIndyValFmt = job.getIndyVal();
    if (job.getIndyVar().matches(".*_hour")) {
      for (int i = 0; i < listIndyValFmt.length; i++) {
        listIndyValFmt[i] = String.valueOf(Integer.parseInt(listIndyValFmt[i]));
      }
    }
    yamlInfo.put("indy_vals", listIndyValFmt);
    String[] listIndyLabel = job.getIndyLabel();
    if (!"0".equals(job.getXtlabFreq())) {
      int intDecim = 0;
      try {
        intDecim = Integer.parseInt(job.getXtlabFreq());
        if (1 > intDecim) {
          throw new Exception();
        }
      } catch (Exception e) {
        throw new ValidationException("unable to parse xtlab_decim value " + job.getXtlabFreq());
      }
      listIndyLabel = decimate(listIndyLabel, intDecim);
    }
    yamlInfo.put("indy_label", 0 < listIndyLabel.length ? listIndyLabel : new String[]{});

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
      if (mapDepY != null && !mapDepY.isEmpty()) {
          strFcstVar = "";

        MVOrderedMap mapStat = new MVOrderedMap();
        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(mapDepY);
        for (String[] aListFcstVarStat : listFcstVarStat) {
          String strFcstVarCur = aListFcstVarStat[0];
          if (strFcstVar.isEmpty()) {
            strFcstVar = strFcstVarCur;
          } else if (!strFcstVar.equals(strFcstVarCur)) {
            //check if this is a mode/mtd/agg/sum stat job
            if (job.isModeJob() || job.isMtdJob() || job.isAggStat() || job.getEventEqual()) {
              throw new ValidationException("fcst_var must remain constant for MODE, MTD, Aggregation "
                      + "statistics, Event Equalizer");
            }
          }
          mapStat.put(aListFcstVarStat[1], aListFcstVarStat[0]);
        }
        if (1 == intY) {
          listAggStats1.addAll(Arrays.asList(mapStat.getKeyList()));
        } else {
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
    yamlInfo.put("plot_stat", job.getPlotStat());
    MVOrderedMap mapTmplValsPlot = MVUtil.addTmplValDep(job);
    String strCaption = MVUtil.buildTemplateInfoString(job.getCaptionTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());

    yamlInfo.put("plot_caption", strCaption);
    yamlInfo.put("create_html", job.getCreateHtml() ? "True" : "False");


    //  populate the formatting information in the R script template
    MVUtil.populatePlotFmtTmplYaml(yamlInfo, job);
    yamlInfo.put("show_nstats", job.getShowNStats() ? "True" : "False");
    yamlInfo.put("indy_stagger_1", job.getIndy1Stagger() ? "True" : "False");
    yamlInfo.put("indy_stagger_2", job.getIndy2Stagger() ? "True" : "False");
    yamlInfo.put("variance_inflation_factor", job.getVarianceInflationFactor() ? "True" : "False");
    yamlInfo.put("dump_points_1", job.getDumpPoints1() ? "True" : "False");
    yamlInfo.put("dump_points_2", job.getDumpPoints2() ? "True" : "False");
    yamlInfo.put("vert_plot", job.getVertPlot() ? "True" : "False");
    yamlInfo.put("xaxis_reverse", job.getXReverse() ? "True" : "False");
    yamlInfo.put("sync_yaxes", job.getSyncAxes() ? "True" : "False");
    yamlInfo.put("grid_on", job.getGridOn() ? "True" : "False");
    yamlInfo.put("show_signif", rListToList(job.getShowSignif()));


    yamlInfo.put("ylim", rListToListNumeric(job.getY1Lim()));
    yamlInfo.put("y2lim", rListToListNumeric(job.getY2Lim()));
    yamlInfo.put("xlim", rListToListNumeric(job.getX1Lim()));
    yamlInfo.put("ensss_pts", Integer.parseInt(job.getEnsSsPts()));
    yamlInfo.put("ensss_pts_disp", job.getEnsSsPtsDisp() ? "True" : "False");

    return yamlInfo;
  }


  /**
   * Build a list by removing elements of the input list at the specified frequency.
   *
   * @param list List to decimate
   * @param freq (optional) Frequency at which to remove members from the input list, defaults to
   *             30
   * @return Decimated list
   */
  private String[] decimate(final String[] list, final int freq) {
    String[] ret = new String[list.length];
    for (int i = 0; i < list.length; i++) {
      ret[i] = (i % freq == 0 ? list[i] : "");
    }
    return ret;
  }

  protected abstract String getPythonScript();

  protected Map<String, Object> addPlotConfigs(Map<String, Object> yamlInfo, MVPlotJob job, int intNumDepSeries) throws ValidationException {
    Map<String, Object> info = new HashMap<>(yamlInfo);
    MVOrderedMap mapTmplValsPlot = MVUtil.addTmplValDep(job);
    String title = MVUtil.buildTemplateInfoString(job.getTitleTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String xLabel = MVUtil.buildTemplateInfoString(job.getXLabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String y1Label = MVUtil.buildTemplateInfoString(job.getY1LabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    String y2Label = MVUtil.buildTemplateInfoString(job.getY2LabelTmpl(), mapTmplValsPlot,
            job.getTmplMaps(), mvBatch.getPrintStream());
    info.put("title", title);
    info.put("xaxis", xLabel);
    info.put("yaxis_1", y1Label);
    info.put("yaxis_2", y2Label);
    info.put("plot_disp", rListToList(job.getPlotDisp()));
    info.put("series_order",  rListToListNumeric(job.getOrderSeries()));
    info.put("user_legend", rListToList(job.getLegend()));

    List<String> colorsListStr = rListToList(job.getColors());
    List<String> colorsList = new ArrayList<>();
    for (String color : colorsListStr) {
      colorsList.add(color.replaceAll("FF$", ""));
    }
    info.put("colors", colorsList);

    info.put("series_line_width",  rListToListNumeric(job.getLwd()));

    List<String> symbolsListStr = rListToList(job.getPch());
    List<String> symbolsList = new ArrayList<>();
    for (String symbol : symbolsListStr) {
      symbolsList.add(symbolsRtoPython.get(symbol));
    }
    info.put("series_symbols", symbolsList);

    List<String> lineStyleListStr = rListToList(job.getLty());
    List<String> lineStyleList = new ArrayList<>();
    for (String lineStyle : lineStyleListStr) {
      lineStyleList.add(linesRtoPython.get(lineStyle));
    }
    info.put("series_line_style", lineStyleList);
    info.put("series_type", rListToList(job.getType()));

    info.put("show_signif",  rListToList(job.getShowSignif()));

    info.put("con_series", rListToListNumeric(job.getConSeries()));
    info.put("plot_ci", rListToList(job.getPlotCI()));


    return info;
  }

  protected List<String> rListToList(String rList) {
    String[] rListArray = rList.replace("c(", "").replace(")", "")
            .replace("\"", "")
            .split(",");
    if (rListArray.length == 0 || (rListArray.length == 1 && rListArray[0].isEmpty())) {
      return new ArrayList<>();
    }
    // Replace boolean strings
    for (int i = 0; i < rListArray.length; i++) {
      if (rListArray[i].equalsIgnoreCase("true")) {
        rListArray[i] = "True";
      } else if (rListArray[i].equalsIgnoreCase("false")) {
        rListArray[i] = "False";
      }
    }
    return Arrays.asList(rListArray);
  }

  protected List<Object> rListToListNumeric(String rList) {
    Object[] rListArray = rList.replace("c(", "").replace(")", "")
            .replace("\"", "")
            .split(",");
    if (rListArray.length == 0 || (rListArray.length == 1 && String.valueOf(rListArray[0]).isEmpty())) {
      return new ArrayList<>();
    }
    List<Object> result = new ArrayList<>();
    // Replace boolean strings
    for (int i = 0; i < rListArray.length; i++) {
      if (MVUtil.isInteger(String.valueOf(rListArray[i]), 10)) {
        result.add(Integer.valueOf(String.valueOf(rListArray[i])));
      } else if (MVUtil.isNumeric(String.valueOf(rListArray[i]))) {
        result.add(Double.valueOf(String.valueOf(rListArray[i])));
      }else{
        result.add(String.valueOf(rListArray[i]));
      }
    }
    return result;
  }
}

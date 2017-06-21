package edu.ucar.metviewer;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MVBatch extends MVUtil {
  private static final Logger logger = LogManager.getLogger("MVBatch");

  private PrintStream printStream ;
  private PrintStream printStreamSQL;


  protected boolean verbose = false;
  public String _strRtmplFolder = "";
  public String _strRworkFolder = "";
  public String _strPlotsFolder = "";
  public String _strDataFolder = "";
  public String _strScriptsFolder = "";
  public int _intNumPlots = 0;
  private int _intNumPlotsRun = 0;
  private static AppDatabaseManager databaseManager;


  public MVBatch(PrintStream log, PrintStream printStreamSql, AppDatabaseManager manager) {
    super();
    MVUtil.updateLog4jConfiguration();
    printStream = log;
    printStreamSQL = printStreamSql;
    databaseManager = manager;
  }

  public MVBatch() {
    this(IoBuilder.forLogger(MVBatch.class).setLevel(org.apache.logging.log4j.Level.INFO).buildPrintStream(), IoBuilder.forLogger(MVBatch.class).setLevel(org.apache.logging.log4j.Level.INFO).buildPrintStream(), null);
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public static String getUsage() {
    return "Usage:  mv_batch\n" +
      "          [-list]\n" +
      "          [-printSql]\n" +
      "          plot_spec_file\n" +
      "          [job_name]\n" +
      "\n" +
      "        where     \"-list\" indicates that the available plot jobs should be listed and no plots run\n" +
      "                  \"-printSql\" print SQL statements\n" +
      "                  \"plot_spec_file\" specifies the XML plot specification document\n" +
      "                  \"job_name\" specifies the name of the job from the plot specification to run\n";
  }

  public static void main(String[] argv) {
    MVBatch bat = new MVBatch();

    bat.printStream.println("----  MVBatch  ----\n");

    try {

      MVPlotJob[] jobs;

      //  if no input file is present, bail
      if (1 > argv.length) {
        logger.info(getUsage() + "\n----  MVBatch Done  ----");
        return;
      }

      //  parse the command line options
      boolean boolList = false;
      int intArg = 0;
      for (; intArg < argv.length && !argv[intArg].matches(".*\\.xml$"); intArg++) {
        if (argv[intArg].equals("-list")) {
          boolList = true;
        } else if (argv[intArg].equals("-printSql")) {
          bat.setVerbose(true);
        } else {
          logger.error("  **  ERROR: unrecognized option '" + argv[intArg] + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
          return;
        }
      }

      //  parse the input file
      String strXMLInput = argv[intArg++];
      bat.printStream.println("input file: " + strXMLInput + "\n");

      MVPlotJobParser parser = new MVPlotJobParser(strXMLInput);
      if (parser.getStrDBType() == null || parser.getStrDBType().equals("mysql")) {
        databaseManager = new MysqlAppDatabaseManager(parser.getDatabaseInfo());
      }
      MVOrderedMap mapJobs = parser.getJobsMap();

      //  build a list of jobs to run
      ArrayList listJobNamesInput = new ArrayList();
      for (; intArg < argv.length; intArg++) {
        listJobNamesInput.add(argv[intArg]);
      }
      String[] listJobNames = mapJobs.getKeyList();
      if (!listJobNamesInput.isEmpty()) {
        listJobNames = toArray(listJobNamesInput);
      }
      bat.printStream.println((boolList ? "" : "processing ") + listJobNames.length + " jobs:");
      for (int i = 0; i < listJobNames.length; i++) {
        bat.printStream.println("  " + listJobNames[i]);
      }


      //  if only a list of plot jobs is requested, return
      if (boolList) {
        bat.printStream.println("\n----  MVBatch Done  ----");
        return;
      }

      //  if a job name is present, run only that job, otherwise run all jobs
      if (1 > listJobNames.length) {
        jobs = parser.getJobsList();
      } else {
        ArrayList listJobs = new ArrayList();
        for (int i = 0; i < listJobNames.length; i++) {
          if (!mapJobs.containsKey(listJobNames[i])) {
            bat.printStream.println("  **  WARNING: unrecognized job \"" + listJobNames[i] + "\"");
            continue;
          }
          listJobs.add(mapJobs.get(listJobNames[i]));
        }
        jobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[]{});
      }

      //  get the path information for the job
      if (!parser.getRtmplFolder().isEmpty()) {
        bat._strRtmplFolder = parser.getRtmplFolder();
      }
      if (!parser.getRworkFolder().isEmpty()) {
        bat._strRworkFolder = parser.getRworkFolder();
      }
      if (!parser.getPlotsFolder().isEmpty()) {
        bat._strPlotsFolder = parser.getPlotsFolder();
      }
      if (!parser.getDataFolder().isEmpty()) {
        bat._strDataFolder = parser.getDataFolder();
      }
      if (!parser.getScriptsFolder().isEmpty()) {
        bat._strScriptsFolder = parser.getScriptsFolder();
      }

      //  calculate the number of plots
      bat._intNumPlots = 0;
      for (int intJob = 0; intJob < jobs.length; intJob++) {

        //  add a job for each permutation of plot fixed values
        Map.Entry[] listPlotFix = jobs[intJob].getPlotFixVal().getOrderedEntries();
        int intNumJobPlots = 1;
        for (int j = 0; j < listPlotFix.length; j++) {
          Object objFixVal = listPlotFix[j].getValue();
          if (objFixVal instanceof String[]) {
            intNumJobPlots *= ((String[]) objFixVal).length;
          } else if (objFixVal instanceof MVOrderedMap) {
            intNumJobPlots *= ((MVOrderedMap) objFixVal).size();
          }
        }

        bat._intNumPlots += intNumJobPlots;
      }
      Date dateStart = new Date();
      SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
      bat.printStream.println("Running " + bat._intNumPlots + " plots\n" + "Begin time: " + formatDB.format(dateStart) + "\n");


      for (int intJob = 0; intJob < jobs.length; intJob++) {
        if (0 < intJob) {
          bat.printStream.println("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
        }
        if (jobs[intJob].getPlotTmpl().equals("rhist.R_tmpl")) {
          bat.runRhistJob(jobs[intJob]);
        } else if (jobs[intJob].getPlotTmpl().equals("phist.R_tmpl")) {
          bat.runPhistJob(jobs[intJob]);
        } else if (jobs[intJob].getPlotTmpl().equals("roc.R_tmpl") ||
          jobs[intJob].getPlotTmpl().equals("rely.R_tmpl")) {
          bat.runRocRelyJob(jobs[intJob]);
        } else {
          bat.runJob(jobs[intJob]);
        }
      }

      Date dateEnd = new Date();
      long intPlotTime = dateEnd.getTime() - dateStart.getTime();
      long intPlotAvg = 0 < bat._intNumPlots ? intPlotTime / (long) bat._intNumPlots : 0;
      formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
      bat.printStream.println("\n" +
        padBegin("End time: ") + formatDB.format(dateEnd) + "\n" +
        padBegin("Plots run: ") + bat._intNumPlotsRun + " of " + bat._intNumPlots + "\n" +
        padBegin("Total time: ") + formatTimeSpan(intPlotTime) + "\n" +
        padBegin("Avg plot time: ") + formatTimeSpan(intPlotAvg) + "\n");

    } catch (Exception e) {
      logger.error("  **  ERROR:  "  + e.getMessage());
    }

    logger.info("----  MVBatch Done  ----");
  }


  public static void validateModeSeriesDefinition(MVPlotJob job) throws Exception {
    MVOrderedMap[] listDep = job.getDepGroups();
    for (int dep = 1; dep <= 2; dep++) {
      String[][] listFcstVarStat = buildFcstVarStatList((MVOrderedMap) listDep[0].get("dep" + dep));

      for (int i = 0; i < listFcstVarStat.length; i++) {
        String stat = listFcstVarStat[i][1].split("_")[0];
        String type = listFcstVarStat[i][1].split("_")[1];
        //validate for all attr stats except for those
        if (!stat.equals("CNT") && !stat.equals("CNTSUM") && !stat.equals("MAXINT") && !stat.equals("MAXINTF") && !stat.equals("MAXINTO") && type.startsWith("D")) {

          if (!type.equals("DCM")) {
            throw new Exception("Incorrect series definition. Stat " + stat + " can only have Cluster and Matched for Diff type");
          }
        }
      }
    }
  }


  /**
   * Build the list of plot_fix field/value permutations for all jobs
   *
   * @param mapPlotFixVal map of field/value pairs to permute
   * @return list of permutations
   */
  public static MVOrderedMap[] buildPlotFixValList(MVOrderedMap mapPlotFixVal) {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap[] listPlotFixPerm = {new MVOrderedMap()};
    if (0 < mapPlotFixVal.size()) {
      MVDataTable tabPlotFixPerm = permute(mapPlotFixVal);
      listPlotFixPerm = tabPlotFixPerm.getRows();
    }

    return listPlotFixPerm;
  }

  /**
   * Populate the input table with the plot formatting tag values stored in the input job.
   *
   * @param tableRTags template value table to receive plot formatting values
   * @param job        source for plot formatting values
   */
  public static void populatePlotFmtTmpl(Map<String, String> tableRTags, MVPlotJob job) {
    tableRTags.put("plot_type", job.getPlotType());
    tableRTags.put("plot_width", job.getPlotWidth());
    tableRTags.put("plot_height", job.getPlotHeight());
    tableRTags.put("plot_res", job.getPlotRes());
    tableRTags.put("plot_units", job.getPlotUnits());
    tableRTags.put("mar", job.getMar());
    tableRTags.put("mgp", job.getMgp());
    tableRTags.put("cex", job.getCex());
    tableRTags.put("title_weight", job.getTitleWeight());
    tableRTags.put("title_size", job.getTitleSize());
    tableRTags.put("title_offset", job.getTitleOffset());
    tableRTags.put("title_align", job.getTitleAlign());
    tableRTags.put("xtlab_orient", job.getXtlabOrient());
    tableRTags.put("xtlab_perp", job.getXtlabPerp());
    tableRTags.put("xtlab_horiz", job.getXtlabHoriz());
    tableRTags.put("xtlab_decim", job.getXtlabFreq());
    tableRTags.put("xtlab_size", job.getXtlabSize());
    tableRTags.put("xlab_weight", job.getXlabWeight());
    tableRTags.put("xlab_size", job.getXlabSize());
    tableRTags.put("xlab_offset", job.getXlabOffset());
    tableRTags.put("xlab_align", job.getXlabAlign());
    tableRTags.put("ytlab_orient", job.getYtlabOrient());
    tableRTags.put("ytlab_perp", job.getYtlabPerp());
    tableRTags.put("ytlab_horiz", job.getYtlabHoriz());
    tableRTags.put("ytlab_size", job.getYtlabSize());
    tableRTags.put("ylab_weight", job.getYlabWeight());
    tableRTags.put("ylab_size", job.getYlabSize());
    tableRTags.put("ylab_offset", job.getYlabOffset());
    tableRTags.put("ylab_align", job.getYlabAlign());
    tableRTags.put("grid_lty", job.getGridLty());
    tableRTags.put("grid_col", job.getGridCol());
    tableRTags.put("grid_lwd", job.getGridLwd());
    tableRTags.put("grid_x", job.getGridX());
    tableRTags.put("x2tlab_orient", job.getX2tlabOrient());
    tableRTags.put("x2tlab_perp", job.getX2tlabPerp());
    tableRTags.put("x2tlab_horiz", job.getX2tlabHoriz());
    tableRTags.put("x2tlab_size", job.getX2tlabSize());
    tableRTags.put("x2lab_weight", job.getX2labWeight());
    tableRTags.put("x2lab_size", job.getX2labSize());
    tableRTags.put("x2lab_offset", job.getX2labOffset());
    tableRTags.put("x2lab_align", job.getX2labAlign());
    tableRTags.put("y2tlab_orient", job.getY2tlabOrient());
    tableRTags.put("y2tlab_perp", job.getY2tlabPerp());
    tableRTags.put("y2tlab_horiz", job.getY2tlabHoriz());
    tableRTags.put("y2tlab_size", job.getY2tlabSize());
    tableRTags.put("y2lab_weight", job.getY2labWeight());
    tableRTags.put("y2lab_size", job.getY2labSize());
    tableRTags.put("y2lab_offset", job.getY2labOffset());
    tableRTags.put("y2lab_align", job.getY2labAlign());
    tableRTags.put("legend_size", job.getLegendSize());
    tableRTags.put("legend_box", job.getLegendBox());
    tableRTags.put("legend_inset", job.getLegendInset());
    tableRTags.put("legend_ncol", job.getLegendNcol());
    tableRTags.put("caption_weight", job.getCaptionWeight());
    tableRTags.put("caption_col", job.getCaptionCol());
    tableRTags.put("caption_size", job.getCaptionSize());
    tableRTags.put("caption_offset", job.getCaptionOffset());
    tableRTags.put("caption_align", job.getCaptionAlign());
    tableRTags.put("box_pts", job.getBoxPts());
    tableRTags.put("box_outline", job.getBoxOutline());
    tableRTags.put("box_boxwex", job.getBoxBoxwex());
    tableRTags.put("box_notch", job.getBoxNotch());
    tableRTags.put("box_avg", job.getBoxAvg());
    tableRTags.put("rely_event_hist", job.getRelyEventHist());
    tableRTags.put("ci_alpha", job.getCIAlpha());
    tableRTags.put("ensss_pts", job.getEnsSsPts());
    tableRTags.put("ensss_pts_disp", job.getEnsSsPtsDisp());
  }


  /**
   * Build SQL for and gather data from the traditional statistics line data tables or the mode data tables and use it to create a plot.
   *
   * @param job traditional or mode plot job
   * @throws Exception
   */
  public void runJob(MVPlotJob job) throws Exception {

    if (job.getPlotTmpl().equals("taylor_plot.R_tmpl")) {
      job.setEventEqual(Boolean.TRUE);
      job.setEqualizeByIndep(Boolean.TRUE);
      job.setAggSl1l2(Boolean.TRUE);
      job.setPlotFixValEq(new MVOrderedMap(job.getPlotFixVal()));
      String[] stats = new String[]{"FBAR", "FSTDEV", "OBAR", "OSTDEV", "PR_CORR", "RMSE"};
      for (String var : ((MVOrderedMap) job.getDepGroups()[0].get("dep1")).getKeyList()) {
        ((MVOrderedMap) job.getDepGroups()[0].get("dep1")).put(var, stats);
      }
    }

    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap mapPlotFixValEq = job.getPlotFixValEq();
    MVOrderedMap mapTmplVals = job.getTmplVal();

    //  build a list of fixed value permutations for all plots
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  determine if the plots require data aggregation
    boolean boolAggCtc = job.getAggCtc();
    boolean boolAggSl1l2 = job.getAggSl1l2();
    boolean boolAggSal1l2 = job.getAggSal1l2();
    boolean boolAggPct = job.getAggPct();
    boolean boolAggNbrCnt = job.getAggNbrCnt();
    boolean boolAggSsvar = job.getAggSsvar();
    boolean boolAggVl1l2 = job.getAggVl1l2();
    boolean boolAggStat = boolAggCtc || boolAggSl1l2 || boolAggSal1l2 || boolAggNbrCnt || boolAggSsvar || boolAggVl1l2;

    boolean boolEnsSs = job.getPlotTmpl().equals("ens_ss.R_tmpl");


		/*
     *  Build a plot for each permutation of <plot_fix> values
		 */
    SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));
    SimpleDateFormat formatPlot = new SimpleDateFormat(MVUtil.DB_DATE_PLOT);
    formatPlot.setTimeZone(TimeZone.getTimeZone("UTC"));
    MVOrderedMap mapTmplValsPlot;
    List<String> listQuery;
    MVOrderedMap mapDep;
    HashMap<String, String> tableAggStatInfo;
    String[] listIndyValFmt;
    //  run the plot jobs once for each permutation of plot fixed values
    for (int intPlotFix = 0; intPlotFix < listPlotFixPerm.length; intPlotFix++) {


      if (0 < intPlotFix) {
        printStream.println("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
      }

      //  if the independent variable uses a dependency, populate the values
      MVPlotDep depIndy = job.getIndyDep();
      if (null != depIndy) {
        String strDep = "";
        if (mapTmplVals.containsKey(depIndy.getDepVar())) {
          strDep = formatDB.format(formatPlot.parse(mapTmplVals.getStr(depIndy.getDepVar())));
        }
        String[][] listIndy = MVPlotJobParser.parseIndyNode(depIndy.getSpec(), strDep);
        job.setIndyVal(listIndy[0]);
        job.setIndyLabel(listIndy[1]);
      }
      boolean boolModeRatioPlot = isModeRatioJob(job);
      //if it is a model job with attribute stat  - validate
      if (isModeJob(job) && !boolModeRatioPlot) {
        validateModeSeriesDefinition(job);
      }

      //  build the SQL statements for the current plot
      listQuery = databaseManager.buildPlotSQL(job, listPlotFixPerm[intPlotFix], mapPlotFixVal, verbose);
      if(verbose){
        for(String sql : listQuery){
          printStreamSQL.println(sql);
          printStreamSQL.println("");
        }
      }

      mapTmplValsPlot = new MVOrderedMap(mapTmplVals);
      if (job.getIndyVar() != null) {
        mapTmplValsPlot.put("indy_var", job.getIndyVar());
      }
      addTmplValDep(job, mapTmplValsPlot);

      tableAggStatInfo = new HashMap<>();
      //  resolve the dep maps and series values for each y-axis
      mapDep = job.getDepGroups()[0];

      // format the indy values, if fcst_hour or valid_hour is being used
      listIndyValFmt = job.getIndyVal();
      if (job.getIndyVar().matches(".*_hour")) {
        for (int i = 0; i < listIndyValFmt.length; i++) {
          try {
            listIndyValFmt[i] = String.valueOf(Integer.parseInt(listIndyValFmt[i]));
          } catch (Exception e) {
          }
        }
      }

      List<String> listAggStats1 = new ArrayList<>();
      List<String> listAggStats2 = new ArrayList<>();
      MVOrderedMap mapAggStatStatic = new MVOrderedMap();
      //  build a list of the plot dep stats for the two y-axes, verifying that the fcst_var remains constant

      String strFcstVar = "";


      if (isModeJob(job) || boolAggStat || boolAggPct) {

        for (int intY = 1; intY <= 2; intY++) {
          MVOrderedMap mapDepY = (MVOrderedMap) mapDep.get("dep" + intY);
          if (mapDepY != null) {
            MVOrderedMap mapStat = new MVOrderedMap();
            String[][] listFcstVarStat = buildFcstVarStatList(mapDepY);
            for (int i = 0; i < listFcstVarStat.length; i++) {
              String strFcstVarCur = listFcstVarStat[i][0];
              if (strFcstVar.isEmpty()) {
                strFcstVar = strFcstVarCur;
              } else if (!strFcstVar.equals(strFcstVarCur)) {
                throw new Exception("fcst_var must remain constant for MODE or when agg_stat/agg_pct/agg_stat_bootstrap is activated");
              }
              mapStat.put(listFcstVarStat[i][1], listFcstVarStat[i][0]);
            }
            if (1 == intY) {
              listAggStats1.addAll(Arrays.asList(mapStat.getKeyList()));
            } else if (2 == intY) {
              listAggStats2.addAll(Arrays.asList(mapStat.getKeyList()));
            }
          }
        }

        mapAggStatStatic.put("fcst_var", strFcstVar);
        MVOrderedMap mapDep1Plot = (MVOrderedMap) mapDep.get("dep1");
        MVOrderedMap mapDep2Plot = (MVOrderedMap) mapDep.get("dep2");
        //  build the map containing tag values for the agg_stat info template

        tableAggStatInfo.put("agg_ctc", job.getAggCtc() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_sl1l2", job.getAggSl1l2() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_sal1l2", job.getAggSal1l2() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_nbrcnt", job.getAggNbrCnt() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_ssvar", job.getAggSsvar() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_vl1l2", job.getAggVl1l2() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("event_equal", String.valueOf(job.getEventEqual()));
        tableAggStatInfo.put("eveq_dis", job.getEveqDis() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("cache_agg_stat", job.getCacheAggStat() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("boot_repl", job.getAggBootRepl());
        tableAggStatInfo.put("boot_random_seed", job.getAggBootRandomSeed());
        tableAggStatInfo.put("boot_ci", job.getAggBootCI());
        tableAggStatInfo.put("ci_alpha", job.getCIAlpha());
        tableAggStatInfo.put("indy_var", job.getIndyVar());
        tableAggStatInfo.put("indy_list", 0 < listIndyValFmt.length ? printRCol(listIndyValFmt, true) : "c()");
        tableAggStatInfo.put("series1_list", job.getSeries1Val().getRDeclSeries());
        tableAggStatInfo.put("series2_list", job.getSeries2Val().getRDeclSeries());
        tableAggStatInfo.put("agg_stat1", printRCol(toArray(listAggStats1), true));
        tableAggStatInfo.put("agg_stat2", printRCol(toArray(listAggStats2), true));
        tableAggStatInfo.put("agg_stat_static", mapAggStatStatic.getRDecl());
        tableAggStatInfo.put("append_to_file", "FALSE");

        tableAggStatInfo.put("working_dir", _strRworkFolder + "/include");
        tableAggStatInfo.put("event_equal", job.getEventEqual() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("equalize_by_indep", job.getEqualizeByIndep() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("fix_val_list_eq", mapPlotFixValEq.getRDecl());
        tableAggStatInfo.put("fix_val_list", mapPlotFixVal.getRDecl());

        String diffSeries1 = buildTemplateString(job.getDiffSeries1(), mapTmplValsPlot, job.getTmplMaps(),printStream);
        String diffSeries2 = buildTemplateString(job.getDiffSeries2(), mapTmplValsPlot, job.getTmplMaps(),printStream);
        tableAggStatInfo.put("series1_diff_list", diffSeries1);
        tableAggStatInfo.put("series2_diff_list", diffSeries2);
        tableAggStatInfo.put("dep1_plot", mapDep1Plot.getRDecl());
        tableAggStatInfo.put("dep2_plot", null != mapDep2Plot ? mapDep2Plot.getRDecl() : "c()");

      }

      if (isModeJob(job) && job.getEventEqual()) {
        //run ee first
        //create sql query
        List<String> eventEqualizeSql = databaseManager.buildPlotModeEventEqualizeSQL(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);

        if (printStreamSQL != null) {
          for (String sql : eventEqualizeSql) {
            printStreamSQL.println(sql + "\n");
          }
        }


        String strDataFileEe = _strDataFolder + "/" + buildTemplateString(job.getDataFileTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
        List<String> queries = new ArrayList<>(1);
        queries.add(eventEqualizeSql.get(eventEqualizeSql.size() - 1));
        boolean success = databaseManager.executeQueriesAndSaveToFile(eventEqualizeSql, strDataFileEe + "_ee_input", job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(), job.getCurrentDBName());

        if (success) {
          String tmplFileName = "agg_stat_event_equalize.info_tmpl";
          tableAggStatInfo.put("agg_stat_input", strDataFileEe + "_ee_input");
          tableAggStatInfo.put("agg_stat_output", strDataFileEe + ".ee");
          String eeInfo = strDataFileEe.replaceFirst("\\.data$", ".agg_stat_event_equalize.info");


          populateTemplateFile(_strRtmplFolder + "/" + tmplFileName, eeInfo, tableAggStatInfo);
          runRscript(job.getRscript(), _strRworkFolder + "/include/agg_stat_event_equalize.R", new String[]{eeInfo},printStream);
        }

      }

			/*
       *  Build and run the query
			 */


      //  run the plot SQL against the database connection
      long intStartTime = new Date().getTime();

      if(printStreamSQL != null) {
        for (String sql : listQuery) {
          printStreamSQL.println(sql + "\n");
        }
      }






			/*
       *  Print the data file in the R_work subfolder and file specified by the data file template
			 */

      //  construct the file system paths for the files used to build the plot
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      if (_strDataFolder.isEmpty()) {
        _strDataFolder = _strRworkFolder + "data/";
      } else {
        _strDataFolder = _strDataFolder + (_strDataFolder.endsWith("/") ? "" : "/");
      }
      if (_strScriptsFolder.isEmpty()) {
        _strScriptsFolder = _strRworkFolder + "scripts/";
      } else {
        _strScriptsFolder = _strScriptsFolder + (_strScriptsFolder.endsWith("/") ? "" : "/");
      }
      String strDataFile = _strDataFolder + buildTemplateString(job.getDataFileTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);

      if (isModeJob(job)) {
        if (boolModeRatioPlot) {
          strDataFile = strDataFile + ".agg_stat_bootstrap";
        } else {
          strDataFile = strDataFile + ".agg_stat_eqz";
        }
      } else {
        if (boolAggStat) {
          strDataFile = strDataFile + ".agg_stat";
        }
        if (boolAggPct) {
          strDataFile = strDataFile + ".agg_pct";
        }
      }
      (new File(strDataFile)).getParentFile().mkdirs();

      databaseManager.executeQueriesAndSaveToFile(listQuery, strDataFile, job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2(), job.getCurrentDBName());

      printStream.println("Query returned  plot_data rows in " + formatTimeSpan(new Date().getTime() - intStartTime));



			/*
       *  Make a copy of the series variables to use for the plot
			 */
      MVOrderedMap mapSeries1ValPlot = job.getSeries1Val();
      MVOrderedMap mapSeries2ValPlot = job.getSeries2Val();

			/*
       *  If agg_stat is requested, generate the agg_stat or agg_stat_bootstrap data files and run agg_stat.R  or agg_stat_bootstrap.R
			 */

      if (isModeJob(job) || boolAggStat || boolAggPct) {

        //  construct and create the path for the agg_stat or agg_stat_bootstrap or simple mode equalize data output file
        String strAggInfo;
        String strAggOutput;

        if (isModeJob(job)) {
          if (boolModeRatioPlot) {
            strAggInfo = strDataFile.replaceFirst("\\.data.agg_stat_bootstrap$", ".agg_stat_bootstrap.info");
            strAggOutput = strDataFile.replaceFirst("\\.agg_stat_bootstrap$", "");
            tableAggStatInfo.put("agg_stat_input_ee", strDataFile.replaceFirst("\\.agg_stat_bootstrap$", ".ee"));
          } else {
            strAggInfo = strDataFile.replaceFirst("\\.data.agg_stat_eqz$", ".agg_stat_eqz.info");
            strAggOutput = strDataFile.replaceFirst("\\.agg_stat_eqz$", "");
            tableAggStatInfo.put("agg_stat_input_ee", strDataFile.replaceFirst("\\.agg_stat_eqz$", ".ee"));
          }

        } else if (boolAggStat) {
          strAggInfo = strDataFile.replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
          strAggOutput = strDataFile.replaceFirst("\\.agg_stat$", "");
        } else {
          //boolAggPct
          strAggInfo = strDataFile.replaceFirst("\\.data.agg_pct$", ".agg_pct.info");
          strAggOutput = strDataFile.replaceFirst("\\.agg_pct$", "");
        }


        File fileAggOutput = new File(strAggOutput);


        //  populate the  info file
        String tmplFileName;

        if (isModeJob(job)) {
          if (boolModeRatioPlot) {
            tmplFileName = "agg_stat_bootstrap.info_tmpl";
          } else {
            tmplFileName = "agg_stat_eqz.info_tmpl";
          }
        } else if (boolAggStat) {
          tmplFileName = "agg_stat.info_tmpl";
        } else {
          //boolAggPct
          tmplFileName = "agg_pct.info_tmpl";
        }
        tableAggStatInfo.put("agg_stat_input", strDataFile);
        tableAggStatInfo.put("agg_stat_output", strAggOutput);
        populateTemplateFile(_strRtmplFolder + tmplFileName, strAggInfo, tableAggStatInfo);

        //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting
        if (!fileAggOutput.exists() || !job.getCacheAggStat()) {
          fileAggOutput.getParentFile().mkdirs();
          String scriptFileName;

          if (isModeJob(job)) {
            if (boolModeRatioPlot) {
              //perform event equalisation against previously calculated cases, ratio statistic calculation and bootstrapping
              scriptFileName = "include/agg_stat_bootstrap.R";
            } else {
              //perform event equalisation against previously calculated cases
              scriptFileName = "include/agg_stat_eqz.R";
            }
          } else if (boolAggStat) {
            //perform event equalisation , statistic calculation and bootstrapping
            scriptFileName = "include/agg_stat.R";
          } else {
            //boolAggPct
            //perform event equalisation , statistic calculation
            scriptFileName = "include/agg_pct.R";
          }

          runRscript(job.getRscript(), _strRworkFolder + scriptFileName, new String[]{strAggInfo}, printStream);

          if (!fileAggOutput.exists()) {
            return;
          }
        }


        //  remove the .agg_stat suffix from the data file
        strDataFile = strAggOutput;

        //  turn off the event equalizer
        job.setEventEqual(Boolean.FALSE);

      }


			/*
       *  Generate filenames and plot labels from the templates
			 */

      //  use the map of all plot values to populate the template strings
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strRFile = _strScriptsFolder + buildTemplateString(job.getRFileTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strY2Label = buildTemplateString(job.getY2LabelTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String diffSeries1 = buildTemplateString(job.getDiffSeries1(), mapTmplValsPlot, job.getTmplMaps(), printStream);
      String diffSeries2 = buildTemplateString(job.getDiffSeries2(), mapTmplValsPlot, job.getTmplMaps(), printStream);


      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();

      //  trim the number of indy_lables, if necessary
      String[] listIndyLabel = job.getIndyLabel();
      if (!"0".equals(job.getXtlabFreq())) {
        int intDecim = 0;
        try {
          intDecim = Integer.parseInt(job.getXtlabFreq());
          if (1 > intDecim) {
            throw new Exception();
          }
        } catch (Exception e) {
          throw new Exception("unable to parse xtlab_decim value " + job.getXtlabFreq());
        }
        listIndyLabel = decimate(listIndyLabel, intDecim);
      }


			/*
       *  Generate the map of R template tags for the plot
			 */


      MVOrderedMap mapDep1Plot = (MVOrderedMap) mapDep.get("dep1");
      MVOrderedMap mapDep2Plot = (MVOrderedMap) mapDep.get("dep2");

      Map.Entry[] listSeries1Val = mapSeries1ValPlot.getOrderedEntriesForSQLSeries();
      Map.Entry[] listSeries2Val = null != job.getSeries2Val() ? mapSeries2ValPlot.getOrderedEntriesForSQLSeries() : new Map.Entry[]{};
      Map.Entry[] listDep1Plot = mapDep1Plot.getOrderedEntries();
      Map.Entry[] listDep2Plot = null != mapDep2Plot ? mapDep2Plot.getOrderedEntries() : new Map.Entry[]{};


      HashMap<String, String> tableRTags = new HashMap<>();

      //  populate the plot settings in the R script template
      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("indy_var", job.getIndyVar());
      tableRTags.put("indy_list", 0 < job.getIndyVal().length ? printRCol(listIndyValFmt, true) : "c()");
      tableRTags.put("indy_label", 0 < listIndyLabel.length ? printRCol(listIndyLabel, true) : "c()");
      tableRTags.put("indy_plot_val", 0 < job.getIndyPlotVal().length ? printRCol(job.getIndyPlotVal(), false) : "c()");
      tableRTags.put("dep1_plot", mapDep1Plot.getRDecl());
      tableRTags.put("dep2_plot", null != mapDep2Plot ? mapDep2Plot.getRDecl() : "c()");
      tableRTags.put("agg_list", new MVOrderedMap().getRDecl());
      tableRTags.put("series1_list", mapSeries1ValPlot.getRDeclSeries());
      tableRTags.put("series2_list", mapSeries2ValPlot.getRDeclSeries());
      tableRTags.put("series_nobs", job.getSeriesNobs().getRDecl());
      tableRTags.put("dep1_scale", job.getDep1Scale().getRDecl());
      tableRTags.put("dep2_scale", job.getDep2Scale().getRDecl());
      tableRTags.put("plot_file", strPlotFile);
      tableRTags.put("data_file", strDataFile);
      tableRTags.put("plot_title", strTitle);
      tableRTags.put("x_label", strXLabel);
      tableRTags.put("y1_label", strY1Label);
      tableRTags.put("y2_label", strY2Label);
      tableRTags.put("plot_caption", strCaption);
      tableRTags.put("plot_cmd", job.getPlotCmd());
      tableRTags.put("event_equal", job.getEventEqual() ? "TRUE" : "FALSE");
      tableRTags.put("vert_plot", job.getVertPlot() ? "TRUE" : "FALSE");
      tableRTags.put("equalize_by_indep", job.getEqualizeByIndep() ? "TRUE" : "FALSE");
      tableRTags.put("x_reverse", job.getXReverse() ? "TRUE" : "FALSE");
      tableRTags.put("show_nstats", job.getShowNStats() ? "TRUE" : "FALSE");
      tableRTags.put("indy1_stagger", job.getIndy1Stagger() ? "TRUE" : "FALSE");
      tableRTags.put("indy2_stagger", job.getIndy2Stagger() ? "TRUE" : "FALSE");
      tableRTags.put("grid_on", job.getGridOn() ? "TRUE" : "FALSE");
      tableRTags.put("sync_axes", job.getSyncAxes() ? "TRUE" : "FALSE");
      tableRTags.put("dump_points1", job.getDumpPoints1() ? "TRUE" : "FALSE");
      tableRTags.put("dump_points2", job.getDumpPoints2() ? "TRUE" : "FALSE");
      tableRTags.put("log_y1", job.getLogY1() ? "TRUE" : "FALSE");
      tableRTags.put("log_y2", job.getLogY2() ? "TRUE" : "FALSE");
      tableRTags.put("variance_inflation_factor", job.getVarianceInflationFactor() ? "TRUE" : "FALSE");
      tableRTags.put("plot_stat", job.getPlotStat());
      tableRTags.put("series1_diff_list", diffSeries1);
      tableRTags.put("series2_diff_list", diffSeries2);
      tableRTags.put("fix_val_list_eq", mapPlotFixValEq.getRDecl());
      tableRTags.put("fix_val_list", mapPlotFixVal.getRDecl());


      // calculate the number of plot curves
      int intNumDep1 = 0;
      if (job.getPlotTmpl().equals("performance.R_tmpl")) {
        intNumDep1 = 1;
      } else if (job.getPlotTmpl().equals("taylor_plot.R_tmpl")) {
        intNumDep1 = listDep1Plot.length;
      } else {
        for (Map.Entry aListDep1Plot : listDep1Plot) {
          intNumDep1 += ((String[]) aListDep1Plot.getValue()).length;
        }
      }
      int intNumDep2 = 0;
      for (Map.Entry aListDep2Plot : listDep2Plot) {
        intNumDep2 += ((String[]) aListDep2Plot.getValue()).length;
      }
      int intNumSeries1Perm = 1;
      for (Map.Entry aListSeries1Val : listSeries1Val) {
        String[] listVal = (String[]) aListSeries1Val.getValue();
        intNumSeries1Perm *= listVal.length;
      }
      int intNumSeries2Perm = 1;
      for (Map.Entry aListSeries2Val : listSeries2Val) {
        intNumSeries2Perm *= ((String[]) aListSeries2Val.getValue()).length;
      }

      int intNumDep1Series = intNumDep1 * intNumSeries1Perm;
      int intNumDep2Series = intNumDep2 * intNumSeries2Perm;
      int intNumDepSeries = intNumDep1Series + intNumDep2Series;
      if (boolEnsSs && job.getEnsSsPtsDisp().equalsIgnoreCase("TRUE")) {
        intNumDepSeries *= 2;
      }
      intNumDepSeries = intNumDepSeries + job.getDiffSeries1Count();
      intNumDepSeries = intNumDepSeries + job.getDiffSeries2Count();

      //  populate the formatting information in the R script template
      populatePlotFmtTmpl(tableRTags, job);

      //  validate the number of formatting elements
      if (intNumDepSeries != parseRCol(job.getPlotDisp()).length) {
        throw new Exception("length of plot_disp differs from number of series (" + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0 && intNumDepSeries != parseRCol(job.getOrderSeries()).length) {
        throw new Exception("length of order_series differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getColors()).length) {
        throw new Exception("length of colors differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getPch()).length) {
        throw new Exception("length of pch differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getType()).length) {
        throw new Exception("length of type differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getLty()).length) {
        throw new Exception("length of lty differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getLwd()).length) {
        throw new Exception("length of lwd differs from number of series (" + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty() &&
        intNumDepSeries != parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series (" + intNumDepSeries + ")");
      }

      if (intNumDepSeries != parseRCol(job.getShowSignif()).length) {
        throw new Exception("length of show_signif differs from number of series (" + intNumDepSeries + ")");
      }
      if (!boolEnsSs) {
        if (intNumDepSeries != parseRCol(job.getPlotCI()).length) {
          throw new Exception("length of plot_ci differs from number of series (" + intNumDepSeries + ")");
        }
        if (intNumDepSeries != parseRCol(job.getConSeries()).length) {
          throw new Exception("length of con_series differs from number of series (" + intNumDepSeries + ")");
        }
      }

      //  replace the template tags with the template values for the current plot
      tableRTags.put("plot_ci", job.getPlotCI().isEmpty() ? printRCol(rep("none", intNumDepSeries), false) : job.getPlotCI());
      tableRTags.put("plot_disp", job.getPlotDisp().isEmpty() ? printRCol(rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
      tableRTags.put("show_signif", job.getShowSignif().isEmpty() ? printRCol(rep("TRUE", intNumDepSeries)) : job.getShowSignif());
      tableRTags.put("order_series", job.getOrderSeries().isEmpty() ? printRCol(repPlusOne(1, intNumDepSeries)) : job.getOrderSeries());
      tableRTags.put("colors", job.getColors().isEmpty() ? "rainbow(" + intNumDepSeries + ")" : job.getColors());
      tableRTags.put("pch", job.getPch().isEmpty() ? printRCol(rep(20, intNumDepSeries)) : job.getPch());
      tableRTags.put("type", job.getType().isEmpty() ? printRCol(rep("b", intNumDepSeries)) : job.getType());
      tableRTags.put("lty", job.getLty().isEmpty() ? printRCol(rep(1, intNumDepSeries)) : job.getLty());
      tableRTags.put("lwd", job.getLwd().isEmpty() ? printRCol(rep(1, intNumDepSeries)) : job.getLwd());
      tableRTags.put("con_series", job.getConSeries().isEmpty() ? printRCol(rep(0, intNumDepSeries)) : job.getConSeries());
      tableRTags.put("legend", job.getLegend().isEmpty() ? "c()" : job.getLegend());
      tableRTags.put("y1_lim", job.getY1Lim().isEmpty() ? "c()" : job.getY1Lim());
      tableRTags.put("y1_bufr", job.getY1Bufr().isEmpty() ? "0" : job.getY1Bufr());
      tableRTags.put("y2_lim", job.getY2Lim().isEmpty() ? "c()" : job.getY2Lim());
      tableRTags.put("y2_bufr", job.getY2Bufr().isEmpty() ? "0" : job.getY2Bufr());
      tableRTags.put("pos", job.getTaylorVoc() ? "TRUE" : "FALSE");
      tableRTags.put("show_gamma", job.getTaylorShowGamma() ? "TRUE" : "FALSE");

      if (job.getLogY1() && !job.getY1Lim().equals("c()")) {
        //check if y1_lim has 0
        String[] lims = job.getY1Lim().replace("c(", "").replace(")", "").split(",");
        if (lims[0].equals("0") || lims[1].equals("0")) {
          throw new Exception("Y1 axis limits can't start or end with 0 if Log Scale is on");
        }
      }
      if (job.getLogY2() && !job.getY2Lim().equals("c()")) {
        //check if y2_lim has 0
        String[] lims = job.getY2Lim().replace("c(", "").replace(")", "").split(",");
        if (lims[0].equals("0") || lims[1].equals("0")) {
          throw new Exception("Y2 axis limits can't start or end with 0 if Log Scale is on");
        }
      }


			/*
       *  Read the template in, replacing the appropriate tags with generated R code
			 */

      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);


			/*
       *  Attempt to run the generated R script
			 */


      boolean boolSuccess = runRscript(job.getRscript(), strRFile,printStream);
      _intNumPlotsRun++;
      printStream.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile);

    }

  }


  /**
   * Add the fcst_var and stat names from the dep structure of the input job to the input list of tmpl map values.
   *
   * @param job         MVPlotJob whose dep structure will be processed
   * @param mapTmplVals map containing tmpl values
   */
  public void addTmplValDep(MVPlotJob job, MVOrderedMap mapTmplVals) {
    for (int intY = 1; intY <= 2; intY++) {

      //  get a list of dep groups
      MVOrderedMap[] listDepGroup = job.getDepGroups();
      MVOrderedMap mapDep = (MVOrderedMap) listDepGroup[0].get("dep" + intY);
      if (mapDep != null) {
        Map.Entry[] listDep = mapDep.getOrderedEntries();

        //  build tmpl map values for each fcst_var
        String strDep = "dep" + intY;
        for (int i = 0; i < listDep.length; i++) {

          //  resolve the fcst_var and stats for the current dep
          String strFcstVar = listDep[i].getKey().toString();
          String[] listStat = (String[]) listDep[i].getValue();

          //  build and add the fcst_var to the tmpl value map
          String strDepFcstVar = strDep + "_" + (i + 1);
          mapTmplVals.put(strDepFcstVar, strFcstVar);
          for (int j = 0; j < listStat.length; j++) {
            mapTmplVals.put(strDepFcstVar + "_stat" + (j + 1), listStat[j]);
          }
        }
      }
    }
  }

  /**
   * Build SQL for and gather data from the line_data_rhist and line_data_rhist_rank tables and use it to build a rank histogram plot.
   *
   * @param job rank histogram plot job
   * @throws Exception
   */
  public void runRhistJob(MVPlotJob job) throws Exception {
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();

    //  build a list of fixed value permutations for all plots
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);


    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap aListPlotFixPerm : listPlotFixPerm) {



			/*
       *  Print the data file in the R_work subfolder and file specified by the data file template
			 */

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      if (_strDataFolder.length() ==0) {
        _strDataFolder = _strRworkFolder + "data/";
      } else {
        _strDataFolder = _strDataFolder + (_strDataFolder.endsWith("/") ? "" : "/");
      }
      if (_strScriptsFolder.length() == 0) {
        _strScriptsFolder = _strRworkFolder + "scripts/";
      } else {
        _strScriptsFolder = _strScriptsFolder + (_strScriptsFolder.endsWith("/") ? "" : "/");
      }
      String strDataFile = _strDataFolder + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      (new File(strDataFile)).getParentFile().mkdirs();

      String strMsg = databaseManager.buildAndExecuteQueriesForRhistJob(job, strDataFile, aListPlotFixPerm, printStream, printStreamSQL);

      //  build the template strings using the current template values
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strRFile = _strScriptsFolder + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);

      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();
      int intNumDepSeries = 1;
      Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSQLSeries();
      for (Map.Entry aListSeries1Val : listSeries1Val) {
        String[] listVal = (String[]) aListSeries1Val.getValue();
        intNumDepSeries *= listVal.length;
      }

      //  validate the number of formatting elements
      if (intNumDepSeries != parseRCol(job.getPlotDisp()).length) {
        throw new Exception("length of plot_disp differs from number of series (" + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0 && intNumDepSeries != parseRCol(job.getOrderSeries()).length) {
        throw new Exception("length of order_series differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getColors()).length) {
        throw new Exception("length of colors differs from number of series (" + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty() &&
        intNumDepSeries != parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series (" + intNumDepSeries + ")");
      }

      //  create a table containing all template values for populating the R_tmpl
      HashMap<String, String> tableRTags = new HashMap<>();

      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("plot_file", strPlotFile);
      tableRTags.put("data_file", strDataFile);
      tableRTags.put("plot_title", strTitle);
      tableRTags.put("x_label", strXLabel);
      tableRTags.put("y1_label", strY1Label);
      tableRTags.put("plot_caption", strCaption);
      tableRTags.put("plot_cmd", job.getPlotCmd());
      tableRTags.put("grid_on", job.getGridOn() ? "TRUE" : "FALSE");
      tableRTags.put("colors", job.getColors().isEmpty() ? "\"gray\"" : job.getColors());
      tableRTags.put("y1_lim", job.getY1Lim().isEmpty() ? "c()" : job.getY1Lim());
      tableRTags.put("normalized_histogram", job.getNormalizedHistogram() ? "TRUE" : "FALSE");
      tableRTags.put("series1_list", job.getSeries1Val().getRDeclSeries());
      tableRTags.put("legend_ncol", job.getLegendNcol());
      tableRTags.put("legend_inset", job.getLegendInset());
      tableRTags.put("legend", job.getLegend().isEmpty() ? "c()" : job.getLegend());
      tableRTags.put("plot_disp", job.getPlotDisp().isEmpty() ? printRCol(rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
      tableRTags.put("order_series", job.getOrderSeries().isEmpty() ? printRCol(repPlusOne(1, intNumDepSeries)) : job.getOrderSeries());

      populatePlotFmtTmpl(tableRTags, job);

      //  populate the R_tmpl with the template values
      (new File(strRFile)).getParentFile().mkdirs();
      (new File(strPlotFile)).getParentFile().mkdirs();
      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);


			/*
       *  Attempt to run the generated R script
			 */


      boolean boolSuccess = runRscript(job.getRscript(), strRFile, printStream);
      printStream.println("\n==== Start Rscript error  ====\n" + strMsg + "\n====   End Rscript error  ====");

      _intNumPlotsRun++;
      printStream.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile + "\n\n");
    }

  }

  /**
   * Build SQL for and gather data from the line_data_phist and line_data_phist_bink tables and use it to build a rank histogram plot.
   *
   * @param job histogram plot job
   * @throws Exception
   */
  public void runPhistJob(MVPlotJob job) throws Exception {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap aListPlotFixPerm : listPlotFixPerm) {
        /*
         *  Print the data file in the R_work subfolder and file specified by the data file template
  			 */

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      if (_strDataFolder.length() == 0) {
        _strDataFolder = _strRworkFolder + "data/";
      } else {
        _strDataFolder = _strDataFolder + (_strDataFolder.endsWith("/") ? "" : "/");
      }
      if (_strScriptsFolder.length() == 0) {
        _strScriptsFolder = _strRworkFolder + "scripts/";
      } else {
        _strScriptsFolder = _strScriptsFolder + (_strScriptsFolder.endsWith("/") ? "" : "/");
      }
      String strDataFile = _strDataFolder + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      (new File(strDataFile)).getParentFile().mkdirs();
      String strMsg = databaseManager.buildAndExecuteQueriesForPhistJob(job, strDataFile, aListPlotFixPerm, printStream, printStreamSQL);

      //  build the template strings using the current template values
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strRFile = _strScriptsFolder + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);

      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();
      int intNumDepSeries = 1;
      Map.Entry[] listSeries1Val = job.getSeries1Val().getOrderedEntriesForSQLSeries();
      for (int i = 0; i < listSeries1Val.length; i++) {
        String[] listVal = (String[]) listSeries1Val[i].getValue();
        intNumDepSeries *= listVal.length;
      }

      //  validate the number of formatting elements
      if (intNumDepSeries != parseRCol(job.getPlotDisp()).length) {
        throw new Exception("length of plot_disp differs from number of series (" + intNumDepSeries + ")");
      }
      if (job.getOrderSeries().length() > 0 && intNumDepSeries != parseRCol(job.getOrderSeries()).length) {
        throw new Exception("length of order_series differs from number of series (" + intNumDepSeries + ")");
      }
      if (intNumDepSeries != parseRCol(job.getColors()).length) {
        throw new Exception("length of colors differs from number of series (" + intNumDepSeries + ")");
      }
      if (!job.getLegend().isEmpty() &&
        intNumDepSeries != parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series (" + intNumDepSeries + ")");
      }

      //  create a table containing all template values for populating the R_tmpl
      HashMap<String, String> tableRTags = new HashMap<>();

      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("plot_file", strPlotFile);
      tableRTags.put("data_file", strDataFile);
      tableRTags.put("plot_title", strTitle);
      tableRTags.put("x_label", strXLabel);
      tableRTags.put("y1_label", strY1Label);
      tableRTags.put("plot_caption", strCaption);
      tableRTags.put("plot_cmd", job.getPlotCmd());
      tableRTags.put("grid_on", job.getGridOn() ? "TRUE" : "FALSE");
      tableRTags.put("colors", job.getColors().length() == 0 ? "\"gray\"" : job.getColors());
      tableRTags.put("y1_lim", job.getY1Lim().length() == 0 ? "c()" : job.getY1Lim());
      tableRTags.put("normalized_histogram", job.getNormalizedHistogram() ? "TRUE" : "FALSE");
      tableRTags.put("series1_list", job.getSeries1Val().getRDeclSeries());
      tableRTags.put("legend_ncol", job.getLegendNcol());
      tableRTags.put("legend_inset", job.getLegendInset());
      tableRTags.put("legend", job.getLegend().length() == 0 ? "c()" : job.getLegend());
      tableRTags.put("plot_disp", job.getPlotDisp().length() == 0 ? printRCol(rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
      tableRTags.put("order_series", job.getOrderSeries().length() == 0 ? printRCol(repPlusOne(1, intNumDepSeries)) : job.getOrderSeries());


      populatePlotFmtTmpl(tableRTags, job);

      //  populate the R_tmpl with the template values
      (new File(strRFile)).getParentFile().mkdirs();
      (new File(strPlotFile)).getParentFile().mkdirs();
      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);


  			/*
         *  Attempt to run the generated R script
  			 */


      boolean boolSuccess = runRscript(job.getRscript(), strRFile, printStream);
      if (strMsg.length() > 0) {
        printStream.println("\n==== Start Rscript error  ====\n" + strMsg + "\n====   End Rscript error  ====");
      }
      _intNumPlotsRun++;
      printStream.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile + "\n\n");
    }

  }


  /**
   * Build SQL for and gather data from the line_data_prc and line_data_prc_thresh tables and use it to build a ROC plot or a reliability plot.
   *
   * @param job ROC/reliability plot job
   * @throws Exception
   */

  public void runRocRelyJob(MVPlotJob job) throws Exception {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  run the plot jobs once for each permutation of plot fixed values
    for (MVOrderedMap aListPlotFixPerm : listPlotFixPerm) {

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      if (_strDataFolder.length() == 0) {
        _strDataFolder = _strRworkFolder + "data/";
      } else {
        _strDataFolder = _strDataFolder + (_strDataFolder.endsWith("/") ? "" : "/");
      }
      if (_strScriptsFolder.length() == 0) {
        _strScriptsFolder = _strRworkFolder + "scripts/";
      } else {
        _strScriptsFolder = _strScriptsFolder + (_strScriptsFolder.endsWith("/") ? "" : "/");
      }
      String strDataFile = _strDataFolder + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      (new File(strDataFile)).getParentFile().mkdirs();
      int intNumDepSeries = databaseManager.buildAndExecuteQueriesForRocRelyJob(job, strDataFile, aListPlotFixPerm, printStream, printStreamSQL);

      //  build the template strings using the current template values
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strRFile = _strScriptsFolder + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strY2Label = buildTemplateString(job.getY2LabelTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps(), printStream);

      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();

      //  create a table containing all template values for populating the R_tmpl
      HashMap<String, String> tableRTags = new HashMap<>();

      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("plot_file", strPlotFile);
      tableRTags.put("data_file", strDataFile);
      tableRTags.put("roc_pct", job.getRocPct() ? "TRUE" : "FALSE");
      tableRTags.put("roc_ctc", job.getRocCtc() ? "TRUE" : "FALSE");
      tableRTags.put("plot_title", strTitle);
      tableRTags.put("x_label", strXLabel);
      tableRTags.put("y1_label", strY1Label);
      tableRTags.put("y2_label", strY2Label);
      tableRTags.put("y2tlab_orient", job.getY2tlabOrient());
      tableRTags.put("y2tlab_perp", job.getY2tlabPerp());
      tableRTags.put("y2tlab_horiz", job.getY2tlabHoriz());
      tableRTags.put("y2tlab_size", job.getY2tlabSize());
      tableRTags.put("y2lab_weight", job.getY2labWeight());
      tableRTags.put("y2lab_size", job.getY2labSize());
      tableRTags.put("y2lab_offset", job.getY2labOffset());
      tableRTags.put("y2lab_align", job.getY2labAlign());
      tableRTags.put("plot_caption", strCaption);
      tableRTags.put("plot_cmd", job.getPlotCmd());
      tableRTags.put("colors", job.getColors().isEmpty() ? "c(\"gray\")" : job.getColors());
      tableRTags.put("pch", job.getPch().isEmpty() ? "c(20)" : job.getPch());
      tableRTags.put("type", job.getType().isEmpty() ? "c(b)" : job.getType());
      tableRTags.put("lty", job.getLty().isEmpty() ? "c(1)" : job.getLty());
      tableRTags.put("lwd", job.getLwd().isEmpty() ? "c(1)" : job.getLwd());
      tableRTags.put("series1_list", job.getSeries1Val().getRDeclSeries());
      tableRTags.put("legend", job.getLegend().isEmpty() ? "c()" : job.getLegend());
      tableRTags.put("plot_disp", job.getPlotDisp().isEmpty() ? printRCol(rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
      tableRTags.put("order_series", job.getOrderSeries().isEmpty() ? printRCol(repPlusOne(1, intNumDepSeries)) : job.getOrderSeries());
      tableRTags.put("legend_size", job.getLegendSize());
      tableRTags.put("legend_box", job.getLegendBox());
      tableRTags.put("legend_inset", job.getLegendInset());
      tableRTags.put("legend_ncol", job.getLegendNcol());
      tableRTags.put("plot_type", job.getPlotType());
      tableRTags.put("summary_curves", job.getSummaryCurveRformat());

      populatePlotFmtTmpl(tableRTags, job);

      //  populate the R_tmpl with the template values
      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);

			/*
       *  Attempt to run the generated R script
			 */

      boolean boolSuccess = runRscript(job.getRscript(), strRFile, printStream);
      _intNumPlotsRun++;
      printStream.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile + "\n\n");
    }

  }

}

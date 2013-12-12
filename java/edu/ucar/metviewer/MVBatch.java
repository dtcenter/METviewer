package edu.ucar.metviewer;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MVBatch extends MVUtil {

  public PrintStream _out = System.out;

  public boolean _boolSQLOnly = false;
  public boolean _boolVerbose = false;

  public String _strRtmplFolder = "/home/pgoldenb/apps/verif/metviewer/R_tmpl/";
  public String _strRworkFolder = "/d1/pgoldenb/var/hmt/R_work/";
  public String _strPlotsFolder = "/d1/pgoldenb/var/hmt/plots/";

  public static final Pattern _patRTmpl = Pattern.compile("#<(\\w+)>#");
  public static final Pattern _patDateRange = Pattern.compile("(?i)\\s*between\\s+'([^']+)'\\s+and\\s+'([^']+)'\\s*");

  public final boolean _boolPlot = true;
  public boolean _boolSQLSort = true;
  public boolean _boolCacheAggStat = true;

  public int _intNumPlots = 0;
  public int _intPlotIndex = 0;
  public int _intNumPlotsRun = 0;

  public MVOrderedMap _mapFcstVarPat = new MVOrderedMap();

  public MVBatch(PrintStream log, boolean boolWindows) {
    _out = log;
  }

  public MVBatch(PrintStream log) {
    this(log, false);
  }

  public MVBatch() {
    this(System.out, false);
  }

  public static String getUsage() {
    return "Usage:  mv_batch\n" +
      "          [-list]\n" +
      "          [-v]\n" +
      "          [-sql]\n" +
      "          plot_spec_file\n" +
      "          [job_name]\n" +
      "\n" +
      "        where     \"-list\" indicates that the available plot jobs should be listed and no plots run\n" +
      "                  \"-v\" indicates verbose output\n" +
      "                  \"-sql\" indicates that the queries for each plot jobs should be listed and no plots run\n" +
      "                  \"plot_spec_file\" specifies the XML plot specification document\n" +
      "                  \"job_name\" specifies the name of the job from the plot specification to run\n";
  }

  public static void main(String[] argv) {
    MVBatch bat = new MVBatch();

    bat._out.println("----  MVBatch  ----\n");
    Connection con = null;

    try {

      MVPlotJob[] jobs = {};

      //  if no input file is present, bail
      if (1 > argv.length) {
        bat._out.println(getUsage() + "\n----  MVBatch Done  ----");
        return;
      }

      //  parse the command line options
      boolean boolList = false;
      int intArg = 0;
      for (; intArg < argv.length && !argv[intArg].matches(".*\\.xml$"); intArg++) {
        if (argv[intArg].equals("-list")) {
          boolList = true;
        } else if (argv[intArg].equals("-sql")) {
          bat._boolSQLOnly = true;
        } else if (argv[intArg].equals("-v")) {
          bat._boolVerbose = true;
        } else {
          System.out.println("  **  ERROR: unrecognized option '" + argv[intArg] + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
          return;
        }
      }
      bat._boolVerbose = (bat._boolSQLOnly ? true : bat._boolVerbose);

      //  parse the input file
      String strXMLInput = argv[intArg++];
      if (!bat._boolSQLOnly) {
        bat._out.println("input file: " + strXMLInput + "\n");
      }
      //con = Datasource.getInstance().getConnection();
      MVPlotJobParser parser = new MVPlotJobParser(strXMLInput, con);
      MVOrderedMap mapJobs = parser.getJobsMap();

      //  build a list of jobs to run
      ArrayList listJobNamesInput = new ArrayList();
      for (; intArg < argv.length; intArg++) {
        listJobNamesInput.add(argv[intArg]);
      }
      String[] listJobNames = mapJobs.getKeyList();
      if (0 < listJobNamesInput.size()) {
        listJobNames = toArray(listJobNamesInput);
      }
      if (!bat._boolSQLOnly) {
        bat._out.println((boolList ? "" : "processing ") + listJobNames.length + " jobs:");
        for (int i = 0; i < listJobNames.length; i++) {
          bat._out.println("  " + listJobNames[i]);
        }
      }

      //  if only a list of plot jobs is requested, return
      if (boolList) {
        bat._out.println("\n----  MVBatch Done  ----");
        try {
          if (con != null) con.close();
        } catch (SQLException e) {
        }
        return;
      }

      //  if a job name is present, run only that job, otherwise run all jobs
      if (1 > listJobNames.length) {
        jobs = parser.getJobsList();
      } else {
        ArrayList listJobs = new ArrayList();
        for (int i = 0; i < listJobNames.length; i++) {
          if (!mapJobs.containsKey(listJobNames[i])) {
            bat._out.println("  **  WARNING: unrecognized job \"" + listJobNames[i] + "\"");
            continue;
          }
          listJobs.add(mapJobs.get(listJobNames[i]));
        }
        jobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[]{});
      }

      //  get the path information for the job
      if (!parser.getRtmplFolder().equals("")) {
        bat._strRtmplFolder = parser.getRtmplFolder();
      }
      if (!parser.getRworkFolder().equals("")) {
        bat._strRworkFolder = parser.getRworkFolder();
      }
      if (!parser.getPlotsFolder().equals("")) {
        bat._strPlotsFolder = parser.getPlotsFolder();
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
      java.util.Date dateStart = new java.util.Date();
      if (!bat._boolSQLOnly) {
        bat._out.println("Running " + bat._intNumPlots + " plots\n" + "Begin time: " + _formatDB.format(dateStart) + "\n");
      }

      for (int intJob = 0; intJob < jobs.length; intJob++) {
        if (0 < intJob)
          bat._out.println("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
        if (jobs[intJob].getPlotTmpl().equals("rhist.R_tmpl")) {
          bat.runRhistJob(jobs[intJob]);
        } else if (jobs[intJob].getPlotTmpl().equals("roc.R_tmpl") ||
          jobs[intJob].getPlotTmpl().equals("rely.R_tmpl")) {
          bat.runRocRelyJob(jobs[intJob]);
        } else {
          bat.runJob(jobs[intJob]);
        }
      }

      java.util.Date dateEnd = new java.util.Date();
      long intPlotTime = dateEnd.getTime() - dateStart.getTime();
      long intPlotAvg = (0 < bat._intNumPlots ? intPlotTime / (long) bat._intNumPlots : 0);
      if (!bat._boolSQLOnly) {
        bat._out.println("\n" +
          padBegin("End time: ") + _formatDB.format(dateEnd) + "\n" +
          padBegin("Plots run: ") + bat._intNumPlotsRun + " of " + bat._intNumPlots + "\n" +
          padBegin("Total time: ") + formatTimeSpan(intPlotTime) + "\n" +
          padBegin("Avg plot time: ") + formatTimeSpan(intPlotAvg) + "\n");
      }

    } catch (Exception e) {
      System.err.println("  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        if (con != null) con.close();
      } catch (SQLException e) {
      }
    }

    bat._out.println("\n----  MVBatch Done  ----");
  }

  /**
   * Build SQL for and gather data from the traditional statistics line data
   * tables or the mode data tables and use it to create a plot.
   *
   * @param job traditional or mode plot job
   * @throws Exception
   */
  public void runJob(MVPlotJob job) throws Exception {

    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap mapTmplVals = job.getTmplVal();

    //  build a list of fixed value permutations for all plots
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  determine if the plots require data aggregation
    boolean boolAggCtc = job.getAggCtc();
    boolean boolAggSl1l2 = job.getAggSl1l2();
    boolean boolAggPct = job.getAggPct();
    boolean boolAggNbrCnt = job.getAggNbrCnt();
    boolean boolAggStat = boolAggCtc || boolAggSl1l2 || boolAggNbrCnt;

    boolean boolEnsSs = job.getPlotTmpl().equals("ens_ss.R_tmpl");


		/*
     *  Build a plot for each permutation of <plot_fix> values
		 */

    //  run the plot jobs once for each permutation of plot fixed values
    for (int intPlotFix = 0; intPlotFix < listPlotFixPerm.length; intPlotFix++) {

      if (0 < intPlotFix) {
        _out.println("\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
      }

      //  if the independent variable uses a dependency, populate the values
      MVPlotDep depIndy = job.getIndyDep();
      if (null != depIndy) {
        String strDep = "";
        if (mapTmplVals.containsKey(depIndy.getDepVar())) {
          strDep = _formatDB.format(_formatPlot.parse(mapTmplVals.getStr(depIndy.getDepVar())));
        }
        String[][] listIndy = MVPlotJobParser.parseIndyNode(depIndy.getSpec(), strDep);
        job.setIndyVal(listIndy[0]);
        job.setIndyLabel(listIndy[1]);
      }
			
			
			/*
			 *  Build and run the query
			 */

      //  build the SQL statements for the current plot
      ArrayList listQuery = buildPlotSQL(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);


      //  run the plot SQL against the database connection
      long intStartTime = (new java.util.Date()).getTime();
      Statement stmt = null;
      ResultSet res = null;
      String[] listSQL = toArray(listQuery);
      for (int i = 0; i < listSQL.length; i++) {
        if (_boolVerbose) {
          _out.println(listSQL[i] + "\n");
        }
        if (_boolSQLOnly) {
          continue;
        }

        stmt = job.getConnection().createStatement();
        stmt.execute(listSQL[i]);
        stmt.close();
      }
      if (_boolSQLOnly) {
        return;
      }

      //  get the number of rows in the job data set
      stmt = job.getConnection().createStatement();
      stmt.execute("SELECT COUNT(*) FROM plot_data;");
      res = stmt.getResultSet();
      int intNumJobDataRows = -1;
      if (res.next()) {
        intNumJobDataRows = res.getInt(1);
      }
      stmt.close();
      _out.println("Query returned " + intNumJobDataRows + " plot_data rows in " +
        formatTimeSpan((new java.util.Date()).getTime() - intStartTime));

      //  if there is no data, do not try to plot it
      if (1 > intNumJobDataRows) {
        _out.println("  **  WARNING: query returned no data");
        _intPlotIndex += 1;
        continue;
      }

      //  update the fcst_var values in the plot_data table
      updatePlotDataFcstVar(job);

			
			/*
			 *  Build the tmpl values map for the plot
			 */

      MVOrderedMap mapTmplValsPlot = new MVOrderedMap(mapTmplVals);
      mapTmplValsPlot.put("indy_var", job.getIndyVar());
      addTmplValDep(job, mapTmplValsPlot);
      _out.println(mapTmplValsPlot.getRDecl() + "\n");


			/*
			 *  Print the data file in the R_work subfolder and file specified by the data file template
			 */

      //  construct the file system paths for the files used to build the plot 
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      String strDataFile = _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapTmplValsPlot, job.getTmplMaps());
      boolean boolModeRatioPlot = isModeRatioJob(job);
      if (boolModeRatioPlot) {
        strDataFile = strDataFile + ".agg_stat_bootstrap";
      } else {
        if (boolAggStat) {
          strDataFile = strDataFile + ".agg_stat";
        }
        if (boolAggPct) {
          strDataFile = strDataFile + ".agg_pct";
        }
      }
      (new File(strDataFile)).getParentFile().mkdirs();

      //  get the plot data from the plot_data temp table and write it to a data file
      stmt = job.getConnection().createStatement();
      String strPlotDataSelect = "SELECT * FROM plot_data;";
      if (job.getCalcCtc() || job.getCalcSl1l2()) {
        strPlotDataSelect = "SELECT * FROM plot_data WHERE stat_value != 'NA';";
      }
      if (_boolVerbose) {
        _out.println(strPlotDataSelect);
      }
      stmt.execute(strPlotDataSelect);
      printFormattedTable(stmt.getResultSet(), new PrintStream(strDataFile), "\t");
      stmt.close();

      // format the indy values, if fcst_hour or valid_hour is being used
      String[] listIndyValFmt = job.getIndyVal();
      if (job.getIndyVar().matches(".*_hour")) {
        for (int i = 0; i < listIndyValFmt.length; i++) {
          try {
            listIndyValFmt[i] = "" + Integer.parseInt(listIndyValFmt[i]);
          } catch (Exception e) {
          }
        }
      }

			
			/*
			 *  Make a copy of the series variables to use for the plot
			 */
      MVOrderedMap mapSeries1ValPlot = new MVOrderedMap(job.getSeries1Val());
      MVOrderedMap mapSeries2ValPlot = new MVOrderedMap(job.getSeries2Val());
			
			
			/*
			 *  If agg_stat is requested, generate the agg_stat or agg_stat_bootstrap data files and run agg_stat.R  or agg_stat_bootstrap.R
			 */

      if (boolModeRatioPlot || boolAggStat || boolAggPct) {

        //  construct and create the path for the agg_stat or agg_stat_bootstrap data output file
        String strAggInfo;
        String strAggOutput;


        if (boolModeRatioPlot) {
          strAggInfo = strDataFile.replaceFirst("\\.data.agg_stat_bootstrap$", ".agg_stat_bootstrap.info");
          strAggOutput = strDataFile.replaceFirst("\\.agg_stat_bootstrap$", "");
        } else if (boolAggStat) {
          strAggInfo = strDataFile.replaceFirst("\\.data.agg_stat$", ".agg_stat.info");
          strAggOutput = strDataFile.replaceFirst("\\.agg_stat$", "");
        } else {
          //boolAggPct
          strAggInfo = strDataFile.replaceFirst("\\.data.agg_pct$", ".agg_pct.info");
          strAggOutput = strDataFile.replaceFirst("\\.agg_pct$", "");
        }


        File fileAggOutput = new File(strAggOutput);

        //  build a list of the plot dep stats for the two y-axes, verifying that the fcst_var remains constant
        ArrayList listAggStats1 = new ArrayList();
        ArrayList listAggStats2 = new ArrayList();
        MVOrderedMap mapDep = job.getDepGroups()[0];
        String strFcstVar = "";
        for (int intY = 1; intY <= 2; intY++) {
          MVOrderedMap mapDepY = (MVOrderedMap) mapDep.get("dep" + intY);
          MVOrderedMap mapStat = new MVOrderedMap();
          String[][] listFcstVarStat = buildFcstVarStatList(mapDepY);
          for (int i = 0; i < listFcstVarStat.length; i++) {
            String strFcstVarCur = listFcstVarStat[i][0];
            if (strFcstVar.equals("")) {
              strFcstVar = strFcstVarCur;
            } else if (!strFcstVar.equals(strFcstVarCur)) {
              throw new Exception("fcst_var must remain constant when agg_stat/agg_pct/agg_stat_bootstrap is activated");
            }
            mapStat.put(listFcstVarStat[i][1], listFcstVarStat[i][0]);
          }
          if (1 == intY) {
            listAggStats1.addAll(Arrays.asList(mapStat.getKeyList()));
          } else if (2 == intY) {
            listAggStats2.addAll(Arrays.asList(mapStat.getKeyList()));
          }
        }
        MVOrderedMap mapAggStatStatic = new MVOrderedMap();
        mapAggStatStatic.put("fcst_var", strFcstVar);

        //  build the map containing tag values for the agg_stat info template
        Hashtable tableAggStatInfo = new Hashtable();
        tableAggStatInfo.put("agg_ctc", job.getAggCtc() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_sl1l2", job.getAggSl1l2() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_nbrcnt", job.getAggNbrCnt() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("event_equal", job.getEventEqual() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("event_equal_m", job.getEventEqualM() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_diff1", job.getAggDiff1() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("agg_diff2", job.getAggDiff2() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("eveq_dis", job.getEveqDis() ? "TRUE" : "FALSE");
        tableAggStatInfo.put("boot_repl", job.getAggBootRepl());
        tableAggStatInfo.put("boot_ci", job.getAggBootCI());
        tableAggStatInfo.put("ci_alpha", job.getCIAlpha());
        tableAggStatInfo.put("indy_var", job.getIndyVar());
        tableAggStatInfo.put("indy_list", (0 < listIndyValFmt.length ? printRCol(listIndyValFmt, true) : "c()"));
        tableAggStatInfo.put("series1_list", job.getSeries1Val().getRDecl());
        tableAggStatInfo.put("series2_list", job.getSeries2Val().getRDecl());
        tableAggStatInfo.put("agg_stat1", printRCol(toArray(listAggStats1), true));
        tableAggStatInfo.put("agg_stat2", printRCol(toArray(listAggStats2), true));
        tableAggStatInfo.put("agg_stat_static", mapAggStatStatic.getRDecl());
        tableAggStatInfo.put("agg_stat_input", strDataFile);
        tableAggStatInfo.put("agg_stat_output", strAggOutput);
        tableAggStatInfo.put("working_dir", _strRworkFolder + "include");

        //  populate the  info file
        String tmplFileName;

        if (boolModeRatioPlot) {
          tmplFileName = "agg_stat_bootstrap.info_tmpl";
        } else if (boolAggStat) {
          tmplFileName = "agg_stat.info_tmpl";
        } else {
          //boolAggPct
          tmplFileName = "agg_pct.info_tmpl";
        }

        populateTemplateFile(_strRtmplFolder + tmplFileName, strAggInfo, tableAggStatInfo);

        //  run agg_stat/agg_pct/agg_stat_bootstrap to generate the data file for plotting
        if (!fileAggOutput.exists() || !_boolCacheAggStat) {
          fileAggOutput.getParentFile().mkdirs();
          String scriptFileName;


          if (boolModeRatioPlot) {
            scriptFileName = "include/agg_stat_bootstrap.R";
          } else if (boolAggStat) {
            scriptFileName = "include/agg_stat.R";
          } else {
            //boolAggPct
            scriptFileName = "include/agg_pct.R";
          }

          runRscript(job.getRscript(), _strRworkFolder + scriptFileName, new String[]{strAggInfo});

          //if( !fileAggStatOutput.exists() ){ throw new Exception("agg_stat.R failed"); }
          if (!fileAggOutput.exists()) {
            return;
          }
        }

        //  if agg_diffN is turned on, add __AGG_DIFFN__ to the plot series
        for (int i = 0; i < 2; i++) {
          MVOrderedMap mapSeriesVal = null;
          String strDiffSeries = "";
          if (i == 0 && job.getAggDiff1()) {
            mapSeriesVal = mapSeries1ValPlot;
            strDiffSeries = "__AGG_DIFF1__";
          } else if (i == 1 && job.getAggDiff2()) {
            mapSeriesVal = mapSeries2ValPlot;
            strDiffSeries = "__AGG_DIFF2__";
          } else {
            continue;
          }
          String[] listSeriesVar = mapSeriesVal.getKeyList();
          ArrayList listDiffVal = new ArrayList(Arrays.asList(((String[]) mapSeriesVal.get(listSeriesVar[listSeriesVar.length - 1]))));
          listDiffVal.add(listDiffVal.size() - 1, strDiffSeries);
          mapSeriesVal.put(listSeriesVar[listSeriesVar.length - 1], toArray(listDiffVal));
        }

        //  remove the .agg_stat suffix from the data file
        strDataFile = strAggOutput;

        //  turn off the event equalizer
        job.setEventEqual(false);

      } //  end: if( boolAggStat )

			
			/*
			 *  Generate filenames and plot labels from the templates 
			 */

      //  use the map of all plot values to populate the template strings
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strRFile = _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strY2Label = buildTemplateString(job.getY2LabelTmpl(), mapTmplValsPlot, job.getTmplMaps());
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapTmplValsPlot, job.getTmplMaps());

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

      //  resolve the dep maps and series values for each y-axis
      MVOrderedMap mapDep = job.getDepGroups()[0];
      MVOrderedMap mapDep1Plot = (MVOrderedMap) mapDep.get("dep1");
      MVOrderedMap mapDep2Plot = (MVOrderedMap) mapDep.get("dep2");

      Map.Entry[] listSeries1Val = mapSeries1ValPlot.getOrderedEntries();
      Map.Entry[] listSeries2Val = (null != job.getSeries2Val() ? mapSeries2ValPlot.getOrderedEntries() : new Map.Entry[]{});
      Map.Entry[] listDep1Plot = mapDep1Plot.getOrderedEntries();
      Map.Entry[] listDep2Plot = (null != mapDep2Plot ? mapDep2Plot.getOrderedEntries() : new Map.Entry[]{});


      Hashtable tableRTags = new Hashtable();

      //  populate the plot settings in the R script template
      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("indy_var", job.getIndyVar());
      tableRTags.put("indy_list", (0 < job.getIndyVal().length ? printRCol(listIndyValFmt, true) : "c()"));
      tableRTags.put("indy_label", (0 < listIndyLabel.length ? printRCol(listIndyLabel, true) : "c()"));
      tableRTags.put("indy_plot_val", (0 < job.getIndyPlotVal().length ? printRCol(job.getIndyPlotVal(), false) : "c()"));
      tableRTags.put("dep1_plot", mapDep1Plot.getRDecl());
      tableRTags.put("dep2_plot", (null != mapDep2Plot ? mapDep2Plot.getRDecl() : "c()"));
      tableRTags.put("agg_list", (new MVOrderedMap()).getRDecl());
      tableRTags.put("series1_list", mapSeries1ValPlot.getRDecl());
      tableRTags.put("series2_list", mapSeries2ValPlot.getRDecl());
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
      tableRTags.put("event_equal", (job.getEventEqual() ? "TRUE" : "FALSE"));
      tableRTags.put("event_equal_m", (job.getEventEqualM() ? "TRUE" : "FALSE"));
      tableRTags.put("vert_plot", (job.getVertPlot() ? "TRUE" : "FALSE"));
      tableRTags.put("x_reverse", (job.getXReverse() ? "TRUE" : "FALSE"));
      tableRTags.put("show_nstats", (job.getShowNStats() ? "TRUE" : "FALSE"));
      tableRTags.put("indy1_stagger", (job.getIndy1Stagger() ? "TRUE" : "FALSE"));
      tableRTags.put("indy2_stagger", (job.getIndy2Stagger() ? "TRUE" : "FALSE"));
      tableRTags.put("grid_on", (job.getGridOn() ? "TRUE" : "FALSE"));
      tableRTags.put("sync_axes", (job.getSyncAxes() ? "TRUE" : "FALSE"));
      tableRTags.put("dump_points1", (job.getDumpPoints1() ? "TRUE" : "FALSE"));
      tableRTags.put("dump_points2", (job.getDumpPoints2() ? "TRUE" : "FALSE"));
      tableRTags.put("log_y1", (job.getLogY1() ? "TRUE" : "FALSE"));
      tableRTags.put("log_y2", (job.getLogY2() ? "TRUE" : "FALSE"));
      tableRTags.put("variance_inflation_factor", (job.getVarianceInflationFactor() ? "TRUE" : "FALSE"));
      tableRTags.put("plot_stat", job.getPlotStat());

      // calculate the number of plot curves
      int intNumDep1 = 0;
      for (int i = 0; i < listDep1Plot.length; i++) {
        intNumDep1 += ((String[]) listDep1Plot[i].getValue()).length;
      }
      int intNumDep2 = 0;
      for (int i = 0; i < listDep2Plot.length; i++) {
        intNumDep2 += ((String[]) listDep2Plot[i].getValue()).length;
      }
      int intNumSeries1Perm = 1;
      for (int i = 0; i < listSeries1Val.length; i++) {
        String[] listVal = (String[]) listSeries1Val[i].getValue();
        intNumSeries1Perm *= listVal.length;
      }
      int intNumSeries2Perm = 1;
      for (int i = 0; i < listSeries2Val.length; i++) {
        intNumSeries2Perm *= ((String[]) listSeries2Val[i].getValue()).length;
      }

      int intNumDep1Series = intNumDep1 * intNumSeries1Perm;
      int intNumDep2Series = intNumDep2 * intNumSeries2Perm;
      int intNumDepSeries = intNumDep1Series + intNumDep2Series;
      if (boolEnsSs && job.getEnsSsPtsDisp().equalsIgnoreCase("TRUE"))
        intNumDepSeries *= 2;
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
      if (!job.getLegend().equals("") &&
        intNumDepSeries != parseRCol(job.getLegend()).length) {
        throw new Exception("length of legend differs from number of series (" + intNumDepSeries + ")");
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
      tableRTags.put("plot_ci", job.getPlotCI().equals("") ? printRCol(rep("none", intNumDepSeries), false) : job.getPlotCI());
      tableRTags.put("plot_disp", job.getPlotDisp().equals("") ? printRCol(rep("TRUE", intNumDepSeries)) : job.getPlotDisp());
      tableRTags.put("series1_diff_list", job.getDiffSeries1());
      tableRTags.put("series2_diff_list", job.getDiffSeries2());
      tableRTags.put("order_series", job.getOrderSeries().equals("") ? printRCol(repPlusOne(1, intNumDepSeries)) : job.getOrderSeries());
      tableRTags.put("colors", job.getColors().equals("") ? "rainbow(" + intNumDepSeries + ")" : job.getColors());
      tableRTags.put("pch", job.getPch().equals("") ? printRCol(rep(20, intNumDepSeries)) : job.getPch());
      tableRTags.put("type", job.getType().equals("") ? printRCol(rep("b", intNumDepSeries)) : job.getType());
      tableRTags.put("lty", job.getLty().equals("") ? printRCol(rep(1, intNumDepSeries)) : job.getLty());
      tableRTags.put("lwd", job.getLwd().equals("") ? printRCol(rep(1, intNumDepSeries)) : job.getLwd());
      tableRTags.put("con_series", job.getConSeries().equals("") ? printRCol(rep(0, intNumDepSeries)) : job.getConSeries());
      tableRTags.put("legend", job.getLegend().equals("") ? "c()" : job.getLegend());
      tableRTags.put("y1_lim", job.getY1Lim().equals("") ? "c()" : job.getY1Lim());
      tableRTags.put("y1_bufr", job.getY1Bufr().equals("") ? "0" : job.getY1Bufr());
      tableRTags.put("y2_lim", job.getY2Lim().equals("") ? "c()" : job.getY2Lim());
      tableRTags.put("y2_bufr", job.getY2Bufr().equals("") ? "0" : job.getY2Bufr());
			
			
			/*
			 *  Read the template in, replacing the appropriate tags with generated R code
			 */

      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);

			
			/*
			 *  Attempt to run the generated R script
			 */

      if (_boolPlot) {
        boolean boolSuccess = runRscript(job.getRscript(), strRFile);
        _intNumPlotsRun++;
        _out.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile);
      }

    }  //  end: for(int intPlotFix=0; intPlotFix < listPlotFixPerm.length; intPlotFix++)

  }

  /**
   * The input job and plot_fix information is used to build a list of SQL
   * queries that result in the temp table plot_data being filled with formatted
   * plot data for a single plot.  Several job validation checks are performed,
   * and an Exception is thrown in case of error.
   *
   * @param job            contains plot job information
   * @param mapPlotFixPerm permutation of plot_fix values for current plot
   * @param mapPlotFixVal  plot_fix values and sets information
   * @return list of SQL statements that result in plot data
   * @throws Exception
   */
  public ArrayList buildPlotSQL(MVPlotJob job, MVOrderedMap mapPlotFixPerm, MVOrderedMap mapPlotFixVal) throws Exception {

    //  determine if the plot job is for stat data or MODE data
    boolean boolModePlot = isModeJob(job);
    boolean boolModeRatioPlot = isModeRatioJob(job);
    Hashtable tableHeaderSQLType = (boolModePlot ? _tableModeHeaderSQLType : _tableStatHeaderSQLType);

    //  populate the plot template values with plot_fix values
    Map.Entry[] listPlotFixVal = buildPlotFixTmplMap(job, mapPlotFixPerm, mapPlotFixVal);

    //  build the sql where clauses for the current permutation of fixed variables and values
    String strPlotFixWhere = buildPlotFixWhere(listPlotFixVal, job, boolModePlot);

    //  add the user-specified condition clause, if present
    if (null != job.getPlotCond() && !job.getPlotCond().equals("")) {
      strPlotFixWhere += "  AND " + job.getPlotCond() + "\n";
    }

    //  determine if the plot requires data aggregation or calculations
    boolean boolAggCtc = job.getAggCtc();
    boolean boolAggSl1l2 = job.getAggSl1l2();
    boolean boolAggPct = job.getAggPct();
    boolean boolAggNbrCnt = job.getAggNbrCnt();
    boolean boolAggStat = boolAggCtc || boolAggSl1l2 || boolAggPct || boolAggNbrCnt;
    boolean boolCalcCtc = job.getCalcCtc();
    boolean boolCalcSl1l2 = job.getCalcSl1l2();
    boolean boolCalcStat;
    if (boolModeRatioPlot) {
      boolCalcStat = true;
    } else {
      boolCalcStat = boolCalcCtc || boolCalcSl1l2;
    }
    boolean boolEnsSs = job.getPlotTmpl().equals("ens_ss.R_tmpl");

    //  remove multiple dep group capability
    MVOrderedMap[] listDep = job.getDepGroups();
    if (1 != listDep.length) {
      throw new Exception("unexpected number of <dep> groups: " + listDep.length);
    }
    MVOrderedMap mapDepGroup = listDep[0];
		
		
		/*
		 *  Build queries for statistics on both the y1 and y2 axes
		 */

    ArrayList listSQL = new ArrayList();
    String strTempSQL = "";
    String strSelectSQL = "";
    for (int intY = 1; intY <= 2; intY++) {

      //  get the dep values for the current dep group
      MVOrderedMap mapDep = (MVOrderedMap) mapDepGroup.get("dep" + intY);

      //  establish lists of entires for each group of variables and values
      Map.Entry[] listSeries = (1 == intY ? job.getSeries1Val() : job.getSeries2Val()).getOrderedEntries();
      Map.Entry[] listDepPlot = mapDep.getOrderedEntries();

      //  if there is a mis-match between the presence of series and dep values, bail
      if (0 < listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("dep values present, but no series values for Y" + intY);
      }
      if (1 > listDepPlot.length && 0 < listSeries.length) {
        throw new Exception("series values present, but no dep values for Y" + intY);
      }

      //  there must be at least one y1 series and stat, but not for y2
      if (1 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        throw new Exception("no Y1 series stat found");
      }
      if (2 == intY && 1 > listDepPlot.length && 1 > listSeries.length) {
        continue;
      }
			
			
			/*
			 *  Construct query components from the series variable/value pairs 
			 */

      //  build the select list and where clauses for the series variables and values
      String strSelectList = "";
      String strTempList = "";
      String strWhere = strPlotFixWhere;
      for (int i = 0; i < listSeries.length; i++) {

        //  get the current series field and values
        String strSeriesField = listSeries[i].getKey().toString();
        String[] listSeriesVal = (String[]) listSeries[i].getValue();

        //  validate the series field and get its type
        String strTempType = "";
        if (!tableHeaderSQLType.containsKey(strSeriesField)) {
          throw new Exception("unrecognized " + (boolModePlot ? "mode" : "stat") + "_header field: " + strSeriesField);
        }
        strTempType = tableHeaderSQLType.get(strSeriesField).toString();

        //  build the select list element, where clause and temp table list element  
        strSelectList += (strSelectList.equals("") ? "" : ",\n") + "  " + formatField(strSeriesField, boolModePlot, true);
        strWhere += (strWhere.equals("") ? "  " : "  AND ") + formatField(strSeriesField, boolModePlot, false) +
          " IN (" + buildValueList(listSeriesVal) + ")\n";
        strTempList += (strTempList.equals("") ? "" : ",\n") + "    " + padEnd(strSeriesField, 20) + strTempType + "";
      }

      //  if the fcst_valid or fcst_init fields are not present in the select list and temp table list, add them
      if (!strSelectList.matches("(?s).*(fcst_init\\w*),.*")) {
        if (boolModePlot) {
          //strSelectList	+= ",\n  " + getSQLDateFormat("h.fcst_init") + " fcst_init";
          strSelectList += ",\n  h.fcst_init";
          strTempList += ",\n    fcst_init           DATETIME";
        } else {
          strSelectList += ",\n " + " ld.fcst_init_beg";
          strTempList += ",\n    fcst_init_beg       DATETIME";
        }
      }
      if (!strSelectList.matches("(?s).*fcst_valid\\w*,.*")) {
        if (boolModePlot) {
          //strSelectList	+= ",\n  " + getSQLDateFormat("h.fcst_valid") + " fcst_valid";
          strSelectList += ",\n  h.fcst_valid";
          strTempList += ",\n    fcst_valid          DATETIME";
        } else {
          strSelectList += ",\n " + " ld.fcst_valid_beg";
          strTempList += ",\n    fcst_valid_beg      DATETIME";
        }
      }

      //  for MODE, build the group by list
      String[] listGroupBy = new String[]{};
      if (boolModePlot && !boolModeRatioPlot) {
        ArrayList listGroupFields = new ArrayList();
        listGroupFields.add(job.getIndyVar());
        for (int i = 0; i < listSeries.length; i++) {
          listGroupFields.add(listSeries[i].getKey().toString());
        }
        listGroupBy = (String[]) listGroupFields.toArray(new String[]{});
      }

      //  for ensemble spread/skill, add the ssvar line data and bail
      if (boolEnsSs) {

        listSQL.add("DROP TEMPORARY TABLE IF EXISTS plot_data;");
        listSQL.add("CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    fcst_var            VARCHAR(32),\n" +
          "    total               INT UNSIGNED,\n" +
          "    bin_n               INT UNSIGNED,\n" +
          "    var_min             DOUBLE,\n" +
          "    var_max             DOUBLE,\n" +
          "    var_mean            DOUBLE,\n" +
          "    fbar                DOUBLE,\n" +
          "    obar                DOUBLE,\n" +
          "    fobar               DOUBLE,\n" +
          "    ffbar               DOUBLE,\n" +
          "    oobar               DOUBLE\n" +
          ");\n");

        listSQL.add("INSERT INTO plot_data\nSELECT\n" +
          strSelectList + ",\n  h.fcst_var,\n" +
          "  ld.total,\n  ld.bin_n,\n  ld.var_min,\n  ld.var_max,\n  ld.var_mean,\n" +
          "  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar\n" +
          "FROM\n" +
          "  stat_header h,\n" +
          "  line_data_ssvar ld\n" +
          "WHERE\n" + strWhere +
          "  AND h.stat_header_id = ld.stat_header_id;\n");

        return listSQL;
      }

			
			/*
			 *  Construct the query components for the independent variable and values
			 */

      //  validate and get the type and values for the independent variable
      String strIndyVarType = "";
      String strIndyVar = job.getIndyVar();
      String[] listIndyVal = job.getIndyVal();
      if (!tableHeaderSQLType.containsKey(strIndyVar)) {
        throw new Exception("unrecognized indep " + (boolModePlot ? "mode" : "stat") + "_header field: " + strIndyVar);
      }
      strIndyVarType = tableHeaderSQLType.get(strIndyVar).toString();
      if (1 > listIndyVal.length) {
        throw new Exception("no independent variable values specified");
      }

      //  construct the select list item, where clause and temp table entry for the independent variable
      if (!strSelectList.contains(strIndyVar)) {
        strSelectList += ",\n  " + formatField(strIndyVar, boolModePlot, true);
        strTempList += ",\n    " + padEnd(strIndyVar, 20) + strIndyVarType;
      }
      strWhere += (!strWhere.equals("") ? "  AND " : "") + formatField(strIndyVar, boolModePlot, false) +
        " IN (" + buildValueList(job.getIndyVal()) + ")\n";

      //  add fcst_var to the select list and temp table entries
      strSelectList += ",\n  h.fcst_var";
      strTempList += ",\n    fcst_var            VARCHAR(64)";
			
			/*
			 *  For agg_stat PCT plots, retrieve the sizes of PCT threshold lists
			 */
      int intPctThresh = -1;
      if (boolAggPct) {
        String strSelPctThresh = "SELECT DISTINCT ld.n_thresh\nFROM\n  stat_header h,\n  line_data_pct ld\n" +
          "WHERE\n" + strWhere + "  AND ld.stat_header_id = h.stat_header_id;";
        if (_boolVerbose || _boolSQLOnly) {
          System.out.println(strSelPctThresh + "\n");
        }

        //  run the PCT thresh query
        Statement stmt = job.getConnection().createStatement();
        try {
          stmt.executeQuery(strSelPctThresh);

          //  validate and save the number of thresholds
          int intNumPctThresh = 0;
          ResultSet r = stmt.getResultSet();
          while (r.next()) {
            intPctThresh = r.getInt(1);
            intNumPctThresh++;
          }
          if (1 != intNumPctThresh) {
            throw new Exception("number of PCT thresholds (" + intNumPctThresh + ") not distinct for plot data");
          } else if (1 > intNumPctThresh) {
            throw new Exception("invalid number of PCT thresholds (" + intNumPctThresh + ") found");
          }
        } catch (Exception e) {

        } finally {
          stmt.close();
        }

      }
			
			
			/*
			 *  Build the temp tables to hold plot data
			 */

      String strTempSQLCur = "";
      if (boolModePlot) {

        //  the single and pair temp tables only need to be built once
        if (1 == intY) {
          listSQL.addAll(buildModeTempSQL(strTempList, strSelectList, strWhere, boolModeRatioPlot));
        }
        if (boolModeRatioPlot) {
          //  build the temp table sql
          strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
            strTempList + ",\n" +
            "    object_id           VARCHAR(16),\n" +
            "    object_cat          VARCHAR(16),\n" +
            "    area           VARCHAR(32),\n" +
            "    total               INT UNSIGNED,\n" +
            "    fcst_flag          BOOLEAN,\n" +
            "    simple_flag          BOOLEAN,\n" +
            "    matched_flag          BOOLEAN\n" +

            ");\n";
        } else {

          //  build the temp table sql
          strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
            strTempList + ",\n" +
            "    object_id           VARCHAR(16),\n" +
            "    object_cat          VARCHAR(16),\n" +
            "    stat_name           VARCHAR(32),\n" +
            "    stat_value          VARCHAR(32)\n" +
            ");\n";
        }

      } else if (boolAggCtc) {

        strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    stat_name           VARCHAR(32),\n" +
          "    stat_value          VARCHAR(16),\n" +
          "    total               INT UNSIGNED,\n" +
          "    fy_oy               INT UNSIGNED,\n" +
          "    fy_on               INT UNSIGNED,\n" +
          "    fn_oy               INT UNSIGNED,\n" +
          "    fn_on               INT UNSIGNED\n" +
          ");\n";

      } else if (boolAggSl1l2) {

        strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    stat_name           VARCHAR(32),\n" +
          "    stat_value          VARCHAR(16),\n" +
          "    total               INT UNSIGNED,\n" +
          "    fbar                DOUBLE,\n" +
          "    obar                DOUBLE,\n" +
          "    fobar               DOUBLE,\n" +
          "    ffbar               DOUBLE,\n" +
          "    oobar               DOUBLE\n" +
          ");\n";

      } else if (boolAggPct) {

        strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    stat_name           VARCHAR(32),\n" +
          "    stat_value          VARCHAR(16),\n" +
          "    total               INT UNSIGNED,\n" +
          "    n_thresh            INT UNSIGNED";

        for (int i = 1; i < intPctThresh; i++) {
          strTempSQLCur += ",\n" +
            "    thresh_" + i + "            DOUBLE,\n" +
            "    oy_" + i + "                INT UNSIGNED,\n" +
            "    on_" + i + "                INT UNSIGNED";
        }
        strTempSQLCur +=
          "\n);\n";

      } else if (boolAggNbrCnt) {

        strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    stat_name           VARCHAR(32),\n" +
          "    stat_value          VARCHAR(16),\n" +
          "    total               INT UNSIGNED,\n" +
          "    fbs                 DOUBLE,\n" +
          "    fss                 DOUBLE\n" +
          ");\n";

      } else {

        strTempSQLCur = "CREATE TEMPORARY TABLE plot_data\n(\n" +
          strTempList + ",\n" +
          "    stat_name           VARCHAR(32),\n" +
          "    stat_value          VARCHAR(16),\n" +
          "    stat_ncl            VARCHAR(16),\n" +
          "    stat_ncu            VARCHAR(16),\n" +
          "    stat_bcl            VARCHAR(16),\n" +
          "    stat_bcu            VARCHAR(16)\n" +
          ");\n";
      }

      //  store or validate the temp table structure
      if (1 == intY) {
        strTempSQL = strTempSQLCur;
        listSQL.add("DROP TEMPORARY TABLE IF EXISTS plot_data;");
        listSQL.add(strTempSQL);
      } else if (!strTempSQLCur.equals(strTempSQL)) {
        throw new Exception("plot_data schemas inconsistent between y1 and y2");
      }
			
			
			/*
			 *  Construct a query for each fcst_var/stat pair
			 */

      //  determine how many queries are needed to gather that stat information
      int intNumQueries = -1;
      String[][] listFcstVarStat = buildFcstVarStatList(mapDep);
      if (boolAggStat && 1 == intY) {
        intNumQueries = 1;
      } else if (boolAggStat && 2 == intY) {
        intNumQueries = 0;
      } else {
        intNumQueries = listFcstVarStat.length;
      }

      //  build a query for each fcst_var/stat pair or a just single query for contingency tables or partial sums
      for (int intFcstVarStat = 0; intFcstVarStat < intNumQueries; intFcstVarStat++) {

        //  get the current fcst_var/stat pair
        String strFcstVar = listFcstVarStat[intFcstVarStat][0];
        String strStat = listFcstVarStat[intFcstVarStat][1];

        //  build the fcst_var where clause criteria
        String strFcstVarClause = "= '" + strFcstVar + "'";
        Matcher matProb = _patProb.matcher(strFcstVar);
        if (matProb.matches() && strFcstVar.contains("*")) {
          Pattern patFcstVar = Pattern.compile(strFcstVar.replace("*", ".*").replace("(", "\\(").replace(")", "\\)"));
          if (!_mapFcstVarPat.containsKey(patFcstVar)) {
            _mapFcstVarPat.put(patFcstVar, formatR(strFcstVar));
          }
          strFcstVarClause = "LIKE '" + strFcstVar.replace("*", "%") + "'";
        }

        //  determine the table containing the current stat
        Hashtable tableStats = null;
        String strStatTable = "";
        String strStatField = strStat.toLowerCase();
        if (boolModePlot) {
          String strStatMode = parseModeStat(strStat)[0];
          if (_tableModeSingleStatField.containsKey(strStatMode)) {
            tableStats = _tableModeSingleStatField;
          } else if (_tableModePairStatField.containsKey(strStatMode)) {
            tableStats = _tableModeSingleStatField;
          } else if (_tableModeRatioField.containsKey(strStat)) {
            tableStats = _tableModeSingleStatField;
          } else {
            throw new Exception("unrecognized mode stat: " + strStatMode);
          }
        } else {

          String aggType = null;
          if (boolAggStat || boolCalcStat) {
            if (boolCalcCtc || boolAggCtc) {
              aggType = MVUtil.CTC;
            } else if (boolCalcSl1l2 || boolAggSl1l2) {
              aggType = MVUtil.SL1L2;
            } else if (boolAggNbrCnt) {
              aggType = MVUtil.NBR_CNT;
            } else if (boolAggPct) {
              aggType = MVUtil.PCT;
            }
          }


          if (_tableStatsCnt.containsKey(strStat)) {
            tableStats = _tableStatsCnt;
            if (boolAggStat || boolCalcStat) {
              isAggTypeValid(_tableStatsCnt, strStat, aggType);
              strStatTable = "line_data_sl1l2" + " ld\n";
            } else {
              strStatTable = "line_data_cnt" + " ld\n";
            }
          } else if (_tableStatsCts.containsKey(strStat)) {
            tableStats = _tableStatsCts;
            if (boolAggStat || boolCalcStat) {
              isAggTypeValid(_tableStatsCts, strStat, aggType);
              strStatTable = "line_data_ctc" + " ld\n";
            } else {
              strStatTable = "line_data_cts" + " ld\n";
            }
          } else if (_tableStatsNbrcnt.containsKey(strStat)) {
            tableStats = _tableStatsNbrcnt;
            isAggTypeValid(_tableStatsNbrcnt, strStat, aggType);
            strStatTable = "line_data_nbrcnt ld\n";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (_tableStatsNbrcts.containsKey(strStat)) {
            tableStats = _tableStatsNbrcts;
            isAggTypeValid(_tableStatsNbrcts, strStat, aggType);
            strStatTable = "line_data_nbrcts ld\n";
            strStatField = strStat.replace("NBR_", "").toLowerCase();
          } else if (_tableStatsPstd.containsKey(strStat)) {
            tableStats = _tableStatsPstd;
            strStatTable = "line_data_pstd ld\n";
            if (boolAggStat) {
              strStatTable = "line_data_pct ld";
              isAggTypeValid(_tableStatsPstd, strStat, aggType);
              for (int i = 1; i < intPctThresh; i++) {
                strStatTable += ",\n  line_data_pct_thresh ldt" + i;
              }
              strStatTable += "\n";
            }
            strStatField = strStat.replace("PSTD_", "").toLowerCase();
          } else if (_tableStatsMcts.containsKey(strStat)) {
            tableStats = _tableStatsMcts;
            isAggTypeValid(_tableStatsMcts, strStat, aggType);
            strStatTable = "line_data_mcts ld\n";
            strStatField = strStat.replace("MCTS_", "").toLowerCase();
          } else if (_tableStatsRhist.containsKey(strStat)) {
            tableStats = _tableStatsRhist;
            isAggTypeValid(_tableStatsRhist, strStat, aggType);
            strStatTable = "line_data_rhist ld\n";
            strStatField = strStat.replace("RHIST_", "").toLowerCase();
          } else if (_tableStatsVl1l2.containsKey(strStat)) {
            isAggTypeValid(_tableStatsRhist, strStat, aggType);
            tableStats = _tableStatsVl1l2;
            strStatTable = "line_data_vl1l2 ld\n";
            strStatField = strStat.replace("VL1L2_", "").toLowerCase();
          } else {
            throw new Exception("unrecognized stat: " + strStat);
          }
        }

        //  build the SQL for the current fcst_var and stat
        if (boolModePlot) {

          //  build the mode SQL
          String strWhereFcstVar = "  fcst_var " + strFcstVarClause;
          listSQL.addAll(buildModeStatSQL(strTempList, strSelectList, strWhereFcstVar, strStat, listGroupBy));

        } else {
          boolean boolBCRMSE = false;
          String strSelectStat = strSelectList;

          //  build the select list and temp table elements for the stat and CIs
          if (strStat.equals("BCRMSE")) {
            boolBCRMSE = true;
            strStatField = "bcmse";
          }
          strSelectStat += ",\n  '" + strStat + "' stat_name";

          //  add the appropriate stat table members, depending on the use of aggregation and stat calculation
          if (boolAggCtc) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fy_oy,\n  ld.fy_on,\n  ld.fn_oy,\n  ld.fn_on";
          } else if (boolAggSl1l2) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbar,\n  ld.obar,\n  ld.fobar,\n  ld.ffbar,\n  ld.oobar";
          } else if (boolAggPct) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  (ld.n_thresh - 1)";
            for (int i = 1; i < intPctThresh; i++) {
              strSelectStat += ",\n";
              if (i < intPctThresh - 1) {
                strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + ldt" + (i + 1) + ".thresh_i)/2, 3),\n";
              } else {
                strSelectStat += "  FORMAT((ldt" + i + ".thresh_i + 1)/2, 3),\n";
              }
              strSelectStat += "  ldt" + i + ".oy_i,\n" +
                "  ldt" + i + ".on_i";
            }
          } else if (boolAggNbrCnt) {
            strSelectStat += ",\n  0 stat_value,\n  ld.total,\n  ld.fbs,\n  ld.fss";
          } else if (boolCalcCtc) {
            strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.fy_oy, ld.fy_on, ld.fn_oy, ld.fn_on) stat_value,\n" +
              "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else if (boolCalcSl1l2) {
            strSelectStat += ",\n  calc" + strStat + "(ld.total, ld.fbar, ld.obar, ld.fobar, ld.ffbar, ld.oobar) stat_value,\n" +
              "  'NA' stat_ncl,\n  'NA' stat_ncu,\n  'NA' stat_bcl,\n  'NA' stat_bcu";
          } else {
            //if( boolBCRMSE ){ strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',REPLACE(FORMAT(sqrt(ld." + strStatField + "),5),',','')) stat_value"; }
            if (boolBCRMSE) {
              strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',CAST(sqrt(ld." + strStatField + ") as DECIMAL(30, 5))) stat_value";
            }
            //else            { strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',REPLACE(FORMAT(ld." + strStatField + ",5),',','')) stat_value"; }
            else {
              strSelectStat += ",\n  IF(ld." + strStatField + "=-9999,'NA',CAST(ld." + strStatField + " as DECIMAL(30, 5))) stat_value";
            }

            //  determine if the current stat has normal or bootstrap CIs
            String[] listStatCI = (String[]) tableStats.get(strStat);
            boolean boolHasNorm = false;
            boolean boolHasBoot = false;
            for (int i = 0; i < listStatCI.length; i++) {
              if (listStatCI[i].equals("nc")) {
                boolHasNorm = true;
              } else if (listStatCI[i].equals("bc")) {
                boolHasBoot = true;
              }
            }

            //  add the CIs to the select list, if present, otherwise, invalid data
            if (boolHasNorm) {
              //strSelectStat += ",\n  IF(ld." + strStatField + "_ncl=-9999,'NA',REPLACE(FORMAT(ld." + strStatField + "_ncl,5),',','')) stat_ncl" +
              strSelectStat += ",\n  IF(ld." + strStatField + "_ncl=-9999,'NA',CAST(ld." + strStatField + "_ncl  as DECIMAL(30, 5))) stat_ncl" +
                // 				 ",\n  IF(ld." + strStatField + "_ncu=-9999,'NA',REPLACE(FORMAT(ld." + strStatField + "_ncu,5),',','')) stat_ncu";
                ",\n  IF(ld." + strStatField + "_ncu=-9999,'NA',CAST(ld." + strStatField + "_ncu  as DECIMAL(30, 5))) stat_ncu";
            } else {
              strSelectStat += ",\n  'NA' stat_ncl,\n  'NA' stat_ncu";
            }

            if (boolHasBoot && !boolAggStat) {
              if (boolBCRMSE) {
                //strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',REPLACE(FORMAT(sqrt(ld." + strStatField + "_bcl),5),',','')) stat_bcl" +
                strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',CAST(sqrt(ld." + strStatField + "_bcl) as DECIMAL(30, 5))) stat_bcl" +
                  //				",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',REPLACE(FORMAT(sqrt(ld." + strStatField + "_bcu),5),',','')) stat_bcu";
                  ",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',CAST(sqrt(ld." + strStatField + "_bcu) as DECIMAL(30, 5))) stat_bcu";
              } else {
                //strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',REPLACE(FORMAT(ld." + strStatField + "_bcl,5),',','')) stat_bcl" +
                strSelectStat += ",\n  IF(ld." + strStatField + "_bcl=-9999,'NA',CAST(ld." + strStatField + "_bcl as DECIMAL(30, 5))) stat_bcl" +
                  //  					",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',REPLACE(FORMAT(ld." + strStatField + "_bcu,5),',','')) stat_bcu";
                  ",\n  IF(ld." + strStatField + "_bcu=-9999,'NA',CAST(ld." + strStatField + "_bcu as DECIMAL(30, 5))) stat_bcu";
              }
            } else {
              strSelectStat += ",\n  'NA' stat_bcl,\n  'NA' stat_bcu";
            }
          }

          String strStatNaClause = "";
          if (!boolAggStat && !boolCalcStat) {
            strStatNaClause = "\n  AND ld." + strStatField + " != -9999";
          }
          if (boolAggPct) {
            for (int i = 1; i < intPctThresh; i++) {
              strStatNaClause += "\n  AND ld.line_data_id = ldt" + i + ".line_data_id\n" +
                "  AND ldt" + i + ".i_value = " + i;
            }
          }

          //  build the query
          strSelectSQL += (strSelectSQL.equals("") ? "INSERT INTO plot_data\n" : "\nUNION\n") +
            "SELECT\n" + strSelectStat + "\n" +
            "FROM\n  stat_header h,\n  " + strStatTable +
            "WHERE\n" + strWhere +
            "  AND h.fcst_var " + strFcstVarClause + "\n" +
            "  AND ld.stat_header_id = h.stat_header_id" + strStatNaClause;
        }

      }  //  end: for(int intFcstVarStat=0; intFcstVarStat < listFcstVarStat.length; intFcstVarStat++)

    }  //  end: for(int intY=1; intY <= 2; intY++)

    //  add the stat plot query to the list
    if (!boolModePlot) {
      listSQL.add(strSelectSQL + ";");
    }

    return listSQL;
  }

  /**
   * check if the aggregation type is compatible with the statistic We're
   * currently only aggregating SL1L2 -> CNT and CTC -> CTS
   *
   * @param tableStats
   * @param strStat
   * @param aggType
   * @throws Exception
   */
  private void isAggTypeValid(MVOrderedMap tableStats, String strStat, String aggType) throws Exception {
    //check if aggType is allowed for this stat
    String[] types = (String[]) tableStats.get(strStat);
    boolean isFound = false;
    for (String type : types) {
      if (type.equals(aggType)) {
        isFound = true;
        break;
      }
    }
    if (!isFound) {
      throw new Exception("aggregation type " + aggType + " isn't compatible with the statistic " + strStat);
    }
  }

  /**
   * Examine the first statistic in the first <dep1> structure to determine if
   * the job is plotting stat statistics or MODE statisticss
   *
   * @param job job whose <dep1> is examined
   * @return true if the checked stat is a MODE stat, false otherwise
   */
  public static boolean isModeJob(MVPlotJob job) {
    MVOrderedMap[] listDep = job.getDepGroups();
    String[][] listFcstVarStat = buildFcstVarStatList((MVOrderedMap) listDep[0].get("dep1"));
    String strStat = parseModeStat(listFcstVarStat[0][1])[0];

    return (_tableModeSingleStatField.containsKey(strStat) ||
      _tableModePairStatField.containsKey(strStat) ||
      _tableModeRatioField.containsKey(listFcstVarStat[0][1]));
  }

  /**
   * Examine the first statistic in the first <dep1> structure to determine if
   * the job is MODE ratio statistics
   *
   * @param job job whose <dep1> is examined
   * @return true if the checked stat is a MODE stat, false otherwise
   */
  public static boolean isModeRatioJob(MVPlotJob job) {
    MVOrderedMap[] listDep = job.getDepGroups();
    if (listDep.length > 0) {
      String[][] listFcstVarStat = buildFcstVarStatList((MVOrderedMap) listDep[0].get("dep1"));
      //String strStat = parseModeStat(listFcstVarStat[0][1])[0];

      return (_tableModeRatioField.containsKey(listFcstVarStat[0][1]));
    } else {
      return false;
    }
  }

  /**
   * Build a list of the fcst_var/stat combinations stored in the input <dep>
   * structure.  The output list is structured as a list of pairs of Strings,
   * with the first element storing the fcst_var and the second element storing
   * the associated statistic.
   *
   * @param mapDep <dep1> or <dep2> structure from a MVPlotJob
   * @return a list of fcst_var/stat pairs
   */
  public static String[][] buildFcstVarStatList(MVOrderedMap mapDep) {
    ArrayList listRet = new ArrayList();
    String[] listFcstVar = mapDep.getKeyList();
    for (int intFcstVar = 0; intFcstVar < listFcstVar.length; intFcstVar++) {
      String[] listStat = (String[]) mapDep.get(listFcstVar[intFcstVar]);
      for (int intStat = 0; intStat < listStat.length; intStat++) {
        listRet.add(new String[]{listFcstVar[intFcstVar], listStat[intStat]});
      }
    }
    return (String[][]) listRet.toArray(new String[][]{});
  }


  /**
   * Use the input query components to build a series of MODE temp table SQL
   * statements and return them in a list.
   *
   * @param strTempList   list of fields for temp tables
   * @param strSelectList list of select fields for temp table population
   *                      queries
   * @param strWhere      list of where clauses for temp table population
   * @return list of DDL and queries that build and populate MODE temp tables
   */
  public static ArrayList buildModeTempSQL(String strTempList, String strSelectList, String strWhere, boolean boolModeRatioPlot) {

    ArrayList listQuery = new ArrayList();

    //  add the object information to the temp table field list
    String strTempListMode = strTempList + ",\n" +
      "    object_id           VARCHAR(128),\n" +
      "    object_cat          VARCHAR(128),\n";


    //  build the MODE single object stat tables
    listQuery.add("DROP TEMPORARY TABLE IF EXISTS mode_single;");
    listQuery.add(
      "CREATE TEMPORARY TABLE mode_single\n" +
        "(\n" +
        strTempListMode +
        "    centroid_x          DOUBLE,\n" +
        "    centroid_y          DOUBLE,\n" +
        "    centroid_lat        DOUBLE,\n" +
        "    centroid_lon        DOUBLE,\n" +
        "    axis_avg            DOUBLE,\n" +
        "    length              DOUBLE,\n" +
        "    width               DOUBLE,\n" +
        "    area                INT UNSIGNED,\n" +
        "    area_filter         INT UNSIGNED,\n" +
        "    area_thresh         INT UNSIGNED,\n" +
        "    curvature           DOUBLE,\n" +
        "    curvature_x         DOUBLE,\n" +
        "    curvature_y         DOUBLE,\n" +
        "    complexity          DOUBLE,\n" +
        "    intensity_10        DOUBLE,\n" +
        "    intensity_25        DOUBLE,\n" +
        "    intensity_50        DOUBLE,\n" +
        "    intensity_75        DOUBLE,\n" +
        "    intensity_90        DOUBLE,\n" +
        "    intensity_nn        DOUBLE,\n" +
        "    intensity_sum       DOUBLE,\n" +
        "    total               INT UNSIGNED,\n" +
        "    fcst_flag           BOOLEAN,\n" +
        "    simple_flag         BOOLEAN,\n" +
        "    matched_flag        BOOLEAN,\n" +
        "    INDEX (fcst_valid),\n" +
        "    INDEX (object_id),\n" +
        "    INDEX (object_cat)\n" +
        ");");

    //  insert information from mode_obj_single into the temp tables with header data
    listQuery.add(
      "INSERT INTO mode_single\n" +
        "SELECT\n" + strSelectList + ",\n" +
        "  mos.object_id,\n" +
        "  mos.object_cat,\n" +
        "  mos.centroid_x,\n" +
        "  mos.centroid_y,\n" +
        "  mos.centroid_lat,\n" +
        "  mos.centroid_lon,\n" +
        "  mos.axis_avg,\n" +
        "  mos.length,\n" +
        "  mos.width,\n" +
        "  mos.area,\n" +
        "  mos.area_filter,\n" +
        "  mos.area_thresh,\n" +
        "  mos.curvature,\n" +
        "  mos.curvature_x,\n" +
        "  mos.curvature_y,\n" +
        "  mos.complexity,\n" +
        "  mos.intensity_10,\n" +
        "  mos.intensity_25,\n" +
        "  mos.intensity_50,\n" +
        "  mos.intensity_75,\n" +
        "  mos.intensity_90,\n" +
        "  mos.intensity_nn,\n" +
        "  mos.intensity_sum,\n" +
        "  mc.total,\n" +
        "  IF(mos.object_id REGEXP '^C?F[[:digit:]]{3}$', 1, 0) fcst_flag,\n" +
        "  IF(mos.object_id REGEXP '^[FO][[:digit:]]{3}$', 1, 0) simple_flag,\n" +
        "  IF(mos.object_cat REGEXP '^C[FO]000$', 0, 1) matched_flag\n" +
        "FROM\n" +
        "  mode_header h,\n" +
        "  mode_obj_single mos,\n" +
        "  mode_cts mc\n" +
        "WHERE\n" + strWhere +
        "  AND mos.mode_header_id = h.mode_header_id\n" +
        "  AND mc.mode_header_id = mos.mode_header_id\n" +
        "  AND mc.field = 'OBJECT';");

    if (!boolModeRatioPlot) {
      listQuery.add("DROP TEMPORARY TABLE IF EXISTS mode_single2;");
      listQuery.add("CREATE TEMPORARY TABLE mode_single2 SELECT * FROM mode_single;");

      listQuery.add("DROP TEMPORARY TABLE IF EXISTS mode_pair;");
      listQuery.add(
        "CREATE TEMPORARY TABLE mode_pair\n" +
          "(\n" +
          strTempListMode +
          "    centroid_dist       DOUBLE,\n" +
          "    boundary_dist       DOUBLE,\n" +
          "    convex_hull_dist    DOUBLE,\n" +
          "    angle_diff          DOUBLE,\n" +
          "    area_ratio          DOUBLE,\n" +
          "    intersection_area   INT UNSIGNED,\n" +
          "    union_area          INT UNSIGNED,\n" +
          "    symmetric_diff      INTEGER,\n" +
          "    intersection_over_area DOUBLE,\n" +
          "    complexity_ratio    DOUBLE,\n" +
          "    percentile_intensity_ratio DOUBLE,\n" +
          "    interest            DOUBLE,\n" +
          "    simple_flag         BOOLEAN,\n" +
          "    matched_flag        BOOLEAN,\n" +
          "    INDEX (fcst_valid),\n" +
          "    INDEX (object_id),\n" +
          "    INDEX (object_cat)\n" +
          ");");

      listQuery.add(
        "INSERT INTO mode_pair\n" +
          "SELECT\n" + strSelectList + ",\n" +
          "  mop.object_id,\n" +
          "  mop.object_cat,\n" +
          "  mop.centroid_dist,\n" +
          "  mop.boundary_dist,\n" +
          "  mop.convex_hull_dist,\n" +
          "  mop.angle_diff,\n" +
          "  mop.area_ratio,\n" +
          "  mop.intersection_area,\n" +
          "  mop.union_area,\n" +
          "  mop.symmetric_diff,\n" +
          "  mop.intersection_over_area,\n" +
          "  mop.complexity_ratio,\n" +
          "  mop.percentile_intensity_ratio,\n" +
          "  mop.interest,\n" +
          "  IF(mop.object_id REGEXP '^F[[:digit:]]{3}_O[[:digit:]]{3}$', 1, 0) simple_flag,\n" +
          //"  IF(mop.interest >= 0.7, 1, 0) matched_flag\n" +
          "  IF(mop.interest >= 0, 1, 0) matched_flag\n" +
          "FROM\n" +
          "  mode_header h,\n" +
          "  mode_obj_pair mop\n" +
          "WHERE\n" + strWhere +
          "  AND mop.mode_header_id = h.mode_header_id;");
    }

    return listQuery;
  }

  /**
   * Use the input query components to build a list of select statements to
   * gather plot data. This function is a switchboard for the different types of
   * MODE statistics: single, pair, derived, difference and ratios.
   *
   * @param strTempList   list of temp table fields
   * @param strSelectList list of select fields
   * @param strWhere      list of where clauses
   * @param strStat       MODE stat
   * @param listGroupBy   list of fields to group by
   * @return list of SQL queries for gathering plot data
   */
  public static ArrayList buildModeStatSQL(String strTempList, String strSelectList, String strWhere, String strStat, String[] listGroupBy) {

    ArrayList listQuery = new ArrayList();

    //  build the appropriate type of query, depending on the statistic
    String[] listStatComp = parseModeStat(strStat);
    if (listStatComp[0].equals("ACOV")) {
      listQuery.add(buildModeSingleAcovTable(strSelectList, strWhere, strStat, listGroupBy));
    } else if (_tableModeSingleStatField.containsKey(listStatComp[0])) {
      if (!listStatComp[1].startsWith("D")) {
        listQuery.add(buildModeSingleStatTable(strSelectList, strWhere, strStat, listGroupBy));
      } else {
        listQuery.add(buildModeSingleStatDiffTable(strSelectList, strWhere, strStat));
      }
    } else if (_tableModePairStatField.containsKey(listStatComp[0])) {
      if (listStatComp[0].equals("MAXINT")) {
        String[] listMaxintQueries = {
          buildModePairStatTable(strSelectList, strWhere, "MAXINTF_" + listStatComp[1]),
          buildModePairStatTable(strSelectList, strWhere, "MAXINTO_" + listStatComp[1])
        };
        listMaxintQueries[0] = listMaxintQueries[0].replace("MAXINTF", "MAXINT");
        listMaxintQueries[1] = listMaxintQueries[1].replace("MAXINTO", "MAXINT");
        listQuery.addAll(Arrays.asList(listMaxintQueries));
      } else {
        listQuery.add(buildModePairStatTable(strSelectList, strWhere, strStat));
      }
    } else if (listStatComp[0].equals("RATIO") || listStatComp[0].equals("AREARAT") || strStat.startsWith("OBJ")) {
      listQuery.add(buildModeSingleStatRatioTable(strSelectList, strWhere, strStat, listGroupBy));
    }
    strSelectList = strSelectList.replaceAll("h\\.", "");
    return listQuery;
  }

  /**
   * Update all fcst_var values in the plot_data table with values that can be
   * used as a varialbe name in an R script, and update probabilistic fcst_var
   * thresholds with wild cards, if necessary.
   *
   * @param job MVPlotJob whose database connection will be used
   * @throws Exception
   */
  public void updatePlotDataFcstVar(MVPlotJob job) throws Exception {

    //  get a list of the fcst_vars
    Statement stmt = job.getConnection().createStatement();
    stmt.execute("SELECT DISTINCT fcst_var FROM plot_data;");
    ResultSet res = stmt.getResultSet();
    MVOrderedMap mapFcstVar = new MVOrderedMap();
    while (res.next()) {
      String strFcstVar = res.getString(1);
      String strFcstVarProc = strFcstVar;
      Map.Entry[] listFcstVarPat = _mapFcstVarPat.getOrderedEntries();
      for (int i = 0; i < listFcstVarPat.length; i++) {
        Matcher matFcstVar = ((Pattern) listFcstVarPat[i].getKey()).matcher(strFcstVar);
        if (matFcstVar.matches()) {
          strFcstVarProc = listFcstVarPat[i].getValue().toString();
        }
      }

      mapFcstVar.put(strFcstVar, formatR(strFcstVarProc));
    }
    stmt.close();

    //  update the fcst_var values with the new values
    if (_boolVerbose && !_boolSQLOnly) {
      _out.println("Updating fcst_var values...");
    }
    Map.Entry[] listFcstVarProc = mapFcstVar.getOrderedEntries();
    for (int i = 0; i < listFcstVarProc.length; i++) {
      String strFcstVarOld = listFcstVarProc[i].getKey().toString();
      String strFcstVarNew = listFcstVarProc[i].getValue().toString();
      if (strFcstVarOld.equals(strFcstVarNew)) {
        if (_boolVerbose && !_boolSQLOnly) {
          _out.println("fcst_var " + strFcstVarOld + " does not require an update");
        }
        continue;
      }
      stmt = job.getConnection().createStatement();
      String strFcstVarUpdate = "UPDATE plot_data SET fcst_var='" + strFcstVarNew + "' WHERE fcst_var='" + strFcstVarOld + "';";
      if (_boolVerbose) {
        _out.println(strFcstVarUpdate);
      }
      stmt.execute(strFcstVarUpdate);
      stmt.close();
    }
    if (_boolVerbose && !_boolSQLOnly) {
      _out.println("Done\n");
    }
  }

  /**
   * Add the fcst_var and stat names from the dep structure of the input job to
   * the input list of tmpl map values.
   *
   * @param job         MVPlotJob whose dep structure will be processed
   * @param mapTmplVals map containing tmpl values
   */
  public void addTmplValDep(MVPlotJob job, MVOrderedMap mapTmplVals) {
    for (int intY = 1; intY <= 2; intY++) {

      //  get a list of dep groups
      MVOrderedMap[] listDepGroup = job.getDepGroups();
      MVOrderedMap mapDep = (MVOrderedMap) listDepGroup[0].get("dep" + intY);
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

  /**
   * Build SQL for and gather data from the line_data_rhist and
   * line_data_rhist_rank tables and use it to build a rank histogram plot.
   *
   * @param job rank histogram plot job
   * @throws Exception
   */
  public void runRhistJob(MVPlotJob job) throws Exception {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  run the plot jobs once for each permutation of plot fixed values
    for (int intPlotFix = 0; intPlotFix < listPlotFixPerm.length; intPlotFix++) {

      //  populate the template map with fixed values 
      Map.Entry[] listPlotFixVal = buildPlotFixTmplMap(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);

      boolean boolModePlot = false;
      Statement stmt = null;

      //  build the stat_header where clauses of the sql  
      String strWhere = buildPlotFixWhere(listPlotFixVal, job, boolModePlot);

      //  if the n_rank is present, replace the table
      strWhere = strWhere.replaceAll("h\\.n_rank", "ld.n_rank");

      //  build a query for the number of ranks among selected rhist records
      String strRankNumSelect =
        "SELECT DISTINCT\n" +
          "  ld.n_rank\n" +
          "FROM\n" +
          "  stat_header h,\n" +
          "  line_data_rhist ld\n" +
          "WHERE\n" +
          strWhere +
          "  AND h.stat_header_id = ld.stat_header_id;";
      if (_boolVerbose) {
        _out.println(strRankNumSelect + "\n");
      }

      //  run the rank number query and warn, if necessary
      stmt = job.getConnection().createStatement();
      stmt.execute(strRankNumSelect);
      ResultSet res = stmt.getResultSet();
      ArrayList listRankNum = new ArrayList();
      while (res.next()) {
        listRankNum.add(res.getString(1));
      }
      String strMsg = "";
      if (0 == listRankNum.size()) {
        throw new Exception("no rank data found");
      } else if (1 < listRankNum.size()) {
        strMsg = "  **  WARNING: multiple n_rank values found for search criteria: ";
        for (int i = 0; i < listRankNum.size(); i++) {
          strMsg += (0 < i ? ", " : "") + listRankNum.get(i).toString();
        }
        _out.println(strMsg);
      }
      stmt.close();

      //  build a query for the rank data
      String strPlotDataSelect =
        "SELECT\n" +
          "  ldr.i_value,\n" +
          "  SUM(ldr.rank_i)\n" +
          "FROM\n" +
          "  stat_header h,\n" +
          "  line_data_rhist ld,\n" +
          "  line_data_rhist_rank ldr\n" +
          "WHERE\n" +
          strWhere +
          "  AND h.stat_header_id = ld.stat_header_id\n" +
          "  AND ld.line_data_id = ldr.line_data_id\n" +
          "GROUP BY i_value;";

      if (_boolVerbose) {
        _out.println(strPlotDataSelect + "\n");
      }
      if (_boolSQLOnly) {
        return;
      }
						

			/*
			 *  Print the data file in the R_work subfolder and file specified by the data file template
			 */

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      String strDataFile = _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      (new File(strDataFile)).getParentFile().mkdirs();

      //  get the data for the current plot from the plot_data temp table and write it to a data file
      stmt = job.getConnection().createStatement();
      stmt.execute(strPlotDataSelect);
      printFormattedTable(stmt.getResultSet(), new PrintStream(strDataFile), "\t");
      stmt.close();

      //  build the template strings using the current template values
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strRFile = _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps());

      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();

      //  create a table containing all template values for populating the R_tmpl
      Hashtable tableRTags = new Hashtable();

      tableRTags.put("r_work", _strRworkFolder);
      tableRTags.put("plot_file", strPlotFile);
      tableRTags.put("data_file", strDataFile);
      tableRTags.put("plot_title", strTitle);
      tableRTags.put("x_label", strXLabel);
      tableRTags.put("y1_label", strY1Label);
      tableRTags.put("plot_caption", strCaption);
      tableRTags.put("plot_cmd", job.getPlotCmd());
      tableRTags.put("grid_on", (job.getGridOn() ? "TRUE" : "FALSE"));
      tableRTags.put("colors", job.getColors().equals("") ? "\"gray\"" : job.getColors());
      tableRTags.put("y1_lim", job.getY1Lim().equals("") ? "c()" : job.getY1Lim());
      tableRTags.put("normalized_histogram", (job.getNormalizedHistogram() ? "TRUE" : "FALSE"));

      populatePlotFmtTmpl(tableRTags, job);

      //  populate the R_tmpl with the template values
      (new File(strRFile)).getParentFile().mkdirs();
      (new File(strPlotFile)).getParentFile().mkdirs();
      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);

			
			/*
			 *  Attempt to run the generated R script
			 */

      if (_boolPlot) {
        boolean boolSuccess = runRscript(job.getRscript(), strRFile);
        if (!strMsg.equals("")) {
          _out.println("\n==== Start Rscript error  ====\n" + strMsg + "\n====   End Rscript error  ====");
        }
        _intNumPlotsRun++;
        _out.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile + "\n\n");
      }
    }
  }


  /**
   * Build SQL for and gather data from the line_data_prc and
   * line_data_prc_thresh tables and use it to build a ROC plot or a reliability
   * plot.
   *
   * @param job ROC/reliability plot job
   * @throws Exception
   */
  public void runRocRelyJob(MVPlotJob job) throws Exception {

    //  build a list of fixed value permutations for all plots
    MVOrderedMap mapPlotFixVal = job.getPlotFixVal();
    MVOrderedMap[] listPlotFixPerm = buildPlotFixValList(mapPlotFixVal);

    //  run the plot jobs once for each permutation of plot fixed values
    for (int intPlotFix = 0; intPlotFix < listPlotFixPerm.length; intPlotFix++) {

      //  populate the template map with fixed values 
      Map.Entry[] listPlotFixVal = buildPlotFixTmplMap(job, listPlotFixPerm[intPlotFix], mapPlotFixVal);

      boolean boolRelyPlot = job.getPlotTmpl().startsWith("rely");
      boolean boolModePlot = false;
      Statement stmt = null;

      //  build the stat_header where clauses of the sql  
      String strWhere = buildPlotFixWhere(listPlotFixVal, job, boolModePlot);

      //  store distinct fcst_thresh and obs_thresh values to verify the plot spec
      ArrayList listFcstThresh = new ArrayList();
      ArrayList listObsThresh = new ArrayList();

      //  check to ensure only a single obs_thresh is used
      String strObsThreshSelect =
        "SELECT\n" +
          "  DISTINCT(h.obs_thresh)\n" +
          "FROM\n" +
          "  stat_header h,\n" +
          "  " + (boolRelyPlot || job.getRocPct() ? "line_data_pct" : "line_data_ctc") + " ld\n" +
          "WHERE\n" +
          strWhere +
          "  AND h.stat_header_id = ld.stat_header_id\n" +
          "ORDER BY h.obs_thresh;";

      //  run the obs_thresh query and throw an error, if necessary
      if (_boolVerbose || _boolSQLOnly) {
        _out.println(strObsThreshSelect + "\n");
      }
      stmt = job.getConnection().createStatement();
      stmt.execute(strObsThreshSelect);
      ResultSet res = stmt.getResultSet();
      while (res.next()) {
        listObsThresh.add(res.getString(1));
      }
      stmt.close();

      //  build the query depending on the type of data requested
      String strPlotDataSelect = "";
      if (boolRelyPlot || job.getRocPct()) {

        //  check to ensure only a single fcst_thresh is used
        String strFcstThreshSelect =
          "SELECT\n" +
            "  DISTINCT(h.fcst_thresh)\n" +
            "FROM\n" +
            "  stat_header h,\n" +
            "  line_data_pct ld\n" +
            "WHERE\n" +
            strWhere +
            "  AND h.stat_header_id = ld.stat_header_id\n" +
            "ORDER BY h.fcst_thresh;";

        //  run the fcst_thresh query and throw an error, if necessary
        if (_boolVerbose || _boolSQLOnly) {
          _out.println(strFcstThreshSelect + "\n");
        }
        stmt = job.getConnection().createStatement();
        stmt.execute(strFcstThreshSelect);
        res = stmt.getResultSet();
        while (res.next()) {
          listFcstThresh.add(res.getString(1));
        }
        stmt.close();

        //  build the plot data sql
        strPlotDataSelect =
          "SELECT\n" +
            "  ld.total,\n" +
            "  ldt.i_value,\n" +
            "  ldt.thresh_i,\n" +
            "  SUM(ldt.oy_i) oy_i,\n" +
            "  SUM(ldt.on_i) on_i\n" +
            "FROM\n" +
            "  stat_header h,\n" +
            "  line_data_pct ld,\n" +
            "  line_data_pct_thresh ldt\n" +
            "WHERE\n" +
            strWhere +
            "  AND h.stat_header_id = ld.stat_header_id\n" +
            "  AND ld.line_data_id = ldt.line_data_id\n" +
            "GROUP BY\n" +
            "  ldt.thresh_i;";

      } else if (job.getRocCtc()) {

        strPlotDataSelect =
          "SELECT\n" +
            "  h.fcst_thresh thresh,\n" +
            "  ld.total,\n" +
            "  SUM(ld.fy_oy) fy_oy,\n" +
            "  SUM(ld.fy_on) fy_on,\n" +
            "  SUM(ld.fn_oy) fn_oy,\n" +
            "  SUM(ld.fn_on) fn_on\n" +
            "FROM\n" +
            "  stat_header h,\n" +
            "  line_data_ctc ld\n" +
            "WHERE\n" +
            strWhere +
            "  AND h.stat_header_id = ld.stat_header_id\n" +
            "GROUP BY\n" +
            "  h.fcst_thresh;";

      }

      //  print the SQL and continue if no plot is requested
      if (_boolVerbose) {
        _out.println(strPlotDataSelect + "\n");
      }
      if (_boolSQLOnly) {
        continue;
      }

      //  if the query does not return data from a single obs_thresh, throw an error
      if (1 != listObsThresh.size()) {
        String strObsThreshMsg = "ROC plots must contain data from only a single obs_thresh, " +
          "instead found " + listObsThresh.size();
        for (int i = 0; i < listObsThresh.size(); i++) {
          strObsThreshMsg += (0 == i ? ": " : ", ") + listObsThresh.toString();
        }
        throw new Exception(strObsThreshMsg);
      }

      //  if the query for a PCT plot does not return data from a single fcst_thresh, throw an error
      if (job.getRocPct() && 1 != listFcstThresh.size()) {
        String strFcstThreshMsg = "ROC/Reliability plots using PCTs must contain data from only a single fcst_thresh, " +
          "instead found " + listFcstThresh.size();
        for (int i = 0; i < listFcstThresh.size(); i++) {
          strFcstThreshMsg += (0 == i ? ":" : "") + "\n  " + listFcstThresh.toString();
        }
        throw new Exception(strFcstThreshMsg);
      }
			

			/*
			 *  Print the data file in the R_work subfolder and file specified by the data file template
			 */

      //  construct the file system paths for the files used to build the plot
      MVOrderedMap mapPlotTmplVals = new MVOrderedMap(job.getTmplVal());
      _strRtmplFolder = _strRtmplFolder + (_strRtmplFolder.endsWith("/") ? "" : "/");
      _strRworkFolder = _strRworkFolder + (_strRworkFolder.endsWith("/") ? "" : "/");
      _strPlotsFolder = _strPlotsFolder + (_strPlotsFolder.endsWith("/") ? "" : "/");
      String strDataFile = _strRworkFolder + "data/" + buildTemplateString(job.getDataFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      (new File(strDataFile)).getParentFile().mkdirs();

      //  get the data for the current plot from the plot_data temp table and write it to a data file
      stmt = job.getConnection().createStatement();
      stmt.execute(strPlotDataSelect);
      printFormattedTable(stmt.getResultSet(), new PrintStream(strDataFile), "\t");
      stmt.close();

      //  build the template strings using the current template values
      String strPlotFile = _strPlotsFolder + buildTemplateString(job.getPlotFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strRFile = _strRworkFolder + "scripts/" + buildTemplateString(job.getRFileTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strTitle = buildTemplateString(job.getTitleTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strXLabel = buildTemplateString(job.getXLabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strY1Label = buildTemplateString(job.getY1LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strY2Label = buildTemplateString(job.getY2LabelTmpl(), mapPlotTmplVals, job.getTmplMaps());
      String strCaption = buildTemplateString(job.getCaptionTmpl(), mapPlotTmplVals, job.getTmplMaps());

      //  create the plot and R script output folders, if necessary
      (new File(strPlotFile)).getParentFile().mkdirs();
      (new File(strRFile)).getParentFile().mkdirs();

      //  create a table containing all template values for populating the R_tmpl
      Hashtable tableRTags = new Hashtable();

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
      tableRTags.put("colors", job.getColors().equals("") ? "c(\"gray\")" : job.getColors());
      tableRTags.put("pch", job.getPch().equals("") ? "c(20)" : job.getPch());
      tableRTags.put("type", job.getType().equals("") ? "c(b)" : job.getType());
      tableRTags.put("lty", job.getLty().equals("") ? "c(1)" : job.getLty());
      tableRTags.put("lwd", job.getLwd().equals("") ? "c(1)" : job.getLwd());
      populatePlotFmtTmpl(tableRTags, job);

      //  populate the R_tmpl with the template values
      populateTemplateFile(_strRtmplFolder + job.getPlotTmpl(), strRFile, tableRTags);
			
			/*
			 *  Attempt to run the generated R script
			 */
      if (_boolPlot) {
        boolean boolSuccess = runRscript(job.getRscript(), strRFile);
        _intNumPlotsRun++;
        _out.println((boolSuccess ? "Created" : "Failed to create") + " plot " + strPlotFile + "\n\n");
      }
    }
  }


  /**
   * Build where clauses for each of the input aggregation field/value entries
   * and return the clauses as a String
   *
   * @param listPlotFixFields list of &lt;plot_fix&gt; field/value pairs
   * @param boolModePlot      specifies MODE plot
   * @return generated SQL where clauses
   */
  public static String buildPlotFixWhere(Map.Entry[] listPlotFixFields, MVPlotJob job, boolean boolModePlot) {
    String strWhere = "";

    //  build the aggregate fields where clause
    for (int i = 0; i < listPlotFixFields.length; i++) {
      String strField = (String) listPlotFixFields[i].getKey();
      String strCondition = "";
      Object objValue = listPlotFixFields[i].getValue();
      if (objValue instanceof String[]) {
        strCondition = "IN (" + buildValueList((String[]) objValue) + ")";
      } else if (objValue instanceof MVOrderedMap) {
        MVOrderedMap mapTmpl = job.getTmplVal();
        String strSetName = mapTmpl.get(strField + "_set").toString();
        String[] listValues = (String[]) ((MVOrderedMap) objValue).get(strSetName);
        strCondition = "IN (" + buildValueList(listValues) + ")";
				/*
				Map.Entry[] listSets = ((MVOrderedMap)objValue).getOrderedEntries();
				for(int j=0; j < listSets.length; j++){
					strCondition += (0 == j? "" : ", ") + buildValueList( (String[])listSets[j].getValue() );
				}
				strCondition = "IN (" + strCondition + ")";
				*/
      } else if (objValue instanceof String) {
        if (objValue.toString().startsWith("BETWEEN")) {
          strCondition = objValue.toString();
        } else {
          strCondition = "IN ('" + objValue.toString() + "')";
        }
      }
      strWhere += (0 < i ? "  AND " : "  ") + formatField(strField, boolModePlot, false) + " " + strCondition + "\n";
    }

    return strWhere;
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
   * Construct the template map for the specified permutation of plot_fix
   * values, using the specified set values.
   *
   * @param job           job whose template values are used
   * @param mapPlotFix    plot_fix field/value pairs to use in populating the
   *                      template values
   * @param mapPlotFixVal values used for sets
   * @throws Exception
   */
  public Map.Entry[] buildPlotFixTmplMap(MVPlotJob job, MVOrderedMap mapPlotFix, MVOrderedMap mapPlotFixVal)
    throws Exception {
    MVOrderedMap mapTmplVals = job.getTmplVal();
    Map.Entry[] listPlotFixVal = mapPlotFix.getOrderedEntries();

    //  add the fixed values to the template value map, and insert set values for this permutation
    for (int i = 0; i < listPlotFixVal.length; i++) {
      String strFixVar = listPlotFixVal[i].getKey().toString();
      String strFixVal = listPlotFixVal[i].getValue().toString();
      MVOrderedMap mapTmpl = job.getTmplMap(strFixVar);
      if (null != mapTmpl && mapTmpl.containsKey(strFixVal)) {
        strFixVal = mapTmpl.getStr(strFixVal);
      } else {
        Matcher matDateRange = _patDateRange.matcher(strFixVal);
        if (matDateRange.matches()) {
          strFixVal = _formatPlot.format(_formatDB.parse(matDateRange.group(2)));
        }
      }
      mapTmplVals.putStr(listPlotFixVal[i].getKey().toString(), strFixVal);

    }

    //  replace fixed value set names with their value maps
    ArrayList listPlotFixValAdj = new ArrayList();
    for (int i = 0; i < listPlotFixVal.length; i++) {
      String strFixVar = listPlotFixVal[i].getKey().toString();
      if (!strFixVar.endsWith("_set")) {
        listPlotFixValAdj.add(listPlotFixVal[i]);
        continue;
      }

      String strFixVarAdj = strFixVar.replaceAll("_set$", "");
      MVOrderedMap mapFixSet = (MVOrderedMap) mapPlotFixVal.get(strFixVarAdj);
      listPlotFixValAdj.add(new MVMapEntry(strFixVarAdj, mapFixSet));
    }
    listPlotFixVal = (Map.Entry[]) listPlotFixValAdj.toArray(new Map.Entry[]{});

    return listPlotFixVal;
  }

  /**
   * Populate the input table with the plot formatting tag values stored in the
   * input job.
   *
   * @param tableRTags template value table to receive plot formatting values
   * @param job        source for plot formatting values
   */
  public static void populatePlotFmtTmpl(Hashtable tableRTags, MVPlotJob job) {
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
   * Populate the template tags in the input template file named tmpl with
   * values from the input table vals and write the result to the output file
   * named output.
   *
   * @param tmpl   Template file to populate
   * @param output Output file to write
   * @param vals   Table containing values corresponding to tags in the input
   *               template
   * @throws Exception
   */
  public static void populateTemplateFile(String tmpl, String output, Hashtable vals) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(tmpl));
    PrintStream writer = new PrintStream(output);
    while (reader.ready()) {
      String strTmplLine = reader.readLine();
      String strOutputLine = strTmplLine;

      Matcher matRtmplLine = _patRTmpl.matcher(strTmplLine);
      while (matRtmplLine.find()) {
        String strRtmplTag = matRtmplLine.group(1);
        if (!vals.containsKey(strRtmplTag)) {
          continue;
        }
        String strRTagVal = (String) vals.get(strRtmplTag);
        strOutputLine = strOutputLine.replace("#<" + strRtmplTag + ">#", strRTagVal);
      }

      writer.println(strOutputLine);
    }
    reader.close();
    writer.close();
  }

  /**
   * Run the input R script named r using the Rscript command.  The output and
   * error output will be written to standard output.
   *
   * @param Rscript Rscript command
   * @param script  R script to run
   * @param args    (optional) Arguments to pass to the R script
   * @throws Exception
   */
  public boolean runRscript(String Rscript, String script, String[] args) throws Exception {

    //  build a list of arguments
    String strArgList = "";
    for (int i = 0; null != args && i < args.length; i++) {
      strArgList += " " + args[i];
    }

    //  run the R script and wait for it to complete
    if (!_boolSQLOnly) {
      _out.println("\nRunning '" + Rscript + " " + script + "'");
    }
    Process proc = Runtime.getRuntime().exec(Rscript + " " + script + strArgList);

    boolean boolExit = false;
    int intExitStatus = 0;
    String strProcStd = "", strProcErr = "";
    BufferedReader readerProcStd = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    BufferedReader readerProcErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    while (!boolExit) {
      try {
        intExitStatus = proc.exitValue();
        boolExit = true;
      } catch (Exception e) {
      }

      while (readerProcStd.ready()) {
        strProcStd += readerProcStd.readLine() + "\n";
      }
      while (readerProcErr.ready()) {
        strProcErr += readerProcErr.readLine() + "\n";
      }
    }
    readerProcStd.close();
    readerProcErr.close();
    proc.destroy();

    if (!"".equals(strProcStd) && !_boolSQLOnly) {
      _out.println("\n==== Start Rscript output  ====\n" + strProcStd + "====   End Rscript output  ====\n");
    }

    if (!"".equals(strProcErr) && !_boolSQLOnly) {
      _out.println("\n==== Start Rscript error  ====\n" + strProcErr + "====   End Rscript error  ====\n");
    }

    //  return the success flag  
    return 0 == intExitStatus;
  }

  public boolean runRscript(String Rscript, String script) throws Exception {
    return runRscript(Rscript, script, new String[]{});
  }

  /**
   * Build SQL to gather mode pair data
   *
   * @param strSelectList
   * @param stat
   * @return
   */
  public static String buildModePairStatTable(String strSelectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the object flag where clause
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "\n  AND  simple_flag = " + ('S' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND  matched_flag = " + ('M' == strStatFlag.charAt(1) ? "1" : "0");
    }

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "");
    String strGroupListMMI = strSelectListStat.replaceAll("HOUR\\([^\\)]+\\) ", "");

    //  set the object_id field, depending on the stat
    String strObjectId = "object_id";
    String strObjectIdName = "object_id";
    String strGroupBy = "";
    if (strStatName.startsWith("MAXINT")) {
      if (strStatName.equals("MAXINTF")) {
        strObjectId = "SUBSTR(object_id, 1, LOCATE('_', object_id)-1) fcst_id";
        strObjectIdName = "fcst_id";
      } else if (strStatName.equals("MAXINTO")) {
        strObjectId = "SUBSTR(object_id, LOCATE('_', object_id)+1) obs_id";
        strObjectIdName = "obs_id";
      }
      strGroupBy = "\nGROUP BY\n" + strGroupListMMI + ",\n  " + strObjectIdName;
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = _tableModePairStatField.get(strStatName).toString();

    //  build the query
    return
      "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
        "  " + strObjectId + ",\n" +
        "  object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strTableStat + " stat_value\n" +
        "FROM mode_pair\n" +
        "WHERE\n" +
        strWhere + strGroupBy + ";";
  }

  public static final Pattern _patModeSingle = Pattern.compile("\\s+h\\.([^,]+),");

  public static String buildModeSingleStatDiffTable(String strSelectList, String strWhere, String stat) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the list of fields involved in the computations
    String strSelectListStat = strSelectList.replaceAll("h\\.", "s.");

    //  modify the where clause to suit two tables
    strWhere = strWhere.replaceAll("fcst_var", "s.fcst_var") + "\n  AND s.fcst_var = s2.fcst_var";

    //  build the where clause using the input select fields
    Matcher mat = _patModeSingle.matcher(strSelectList);
    while (mat.find()) {
      strWhere += "\n  AND s." + mat.group(1) + " = s2." + mat.group(1);
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND s.simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0") +
        "\n  AND s2.simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "\n  AND s.matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0") +
        "\n  AND s2.matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    //  set the table stat field, object_id pattern and group by clause, depending on the stat
    String strTableStat = _tableModeSingleStatField.get(strStatName).toString();
    String statName = strTableStat.split("\\(")[0];
    String[] strTableStats = new String[2];
    if (strTableStat.contains("object_id")) {
      strTableStats[0] = statName + "( s.object_id)";
      strTableStats[1] = statName + "( s2.object_id)";
    } else {
      strTableStats[0] = "s." + strTableStat;
      strTableStats[1] = "s2." + strTableStat;

    }

    //  build the query COUNT(object_id)
    return
      "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
        "  s.object_id,\n" +
        "  s.object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strTableStats[0] + " - " + strTableStats[1] + " stat_value\n" +
        "FROM mode_single s, mode_single2 s2\n" +
        "WHERE\n" +
        strWhere + "\n" +
        "  AND s.fcst_flag = 1\n" +
        "  AND s2.fcst_flag = 0\n" +
        //"  AND RIGHT(s.object_id, 3) = RIGHT(s2.object_id, 3) group by model,fcst_init,fcst_valid,fcst_lead ;";
        "  AND RIGHT(s.object_id, 3) = RIGHT(s2.object_id, 3) ;";
  }

  public static String buildModeSingleStatTable(String selectList, String strWhere, String stat, String[] groups) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatName = listStatParse[0];
    String strStatFlag = listStatParse[1];

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");

    //  set the table stat field
    String strTableStat = _tableModeSingleStatField.get(strStatName).toString();

    //  build the object flag where clause		
    if (strStatFlag.charAt(0) != 'A') {
      strWhere += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }
    if (strStatFlag.charAt(1) != 'A') {
      strWhere += "\n  AND simple_flag = " + ('S' == strStatFlag.charAt(1) ? "1" : "0");
    }
    if (strStatFlag.charAt(2) != 'A') {
      strWhere += "\n  AND matched_flag = " + ('M' == strStatFlag.charAt(2) ? "1" : "0");
    }

    //  build the group by clause
    String strGroupBy = "";
    if (strStatName.startsWith("CNT")) {
      strGroupBy = "\nGROUP BY\n";
      for (int i = 0; i < groups.length; i++) {
        strGroupBy += (0 < i ? ",\n" : "") + "  " + groups[i];
      }
      if (!strStatName.equals("CNTSUM")) {
        strGroupBy += ",\n  fcst_valid";
      }
    }

    //  build the query
    return
      "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
        "  object_id,\n" +
        "  object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strTableStat + " stat_value\n" +
        "FROM mode_single\n" +
        "WHERE\n" + strWhere + strGroupBy + ";";
  }

  public static String buildModeSingleStatRatioTable(String selectList, String strWhere, String stat, String[] groups) {

    //  build the list of fields involved in the computations
    String strSelectListStat = selectList.replaceAll("h\\.", "");

    //  set the table stat field
    String strTableStat = _tableModeRatioField.get(stat).toString();

    //  build the group by clause
    String strGroupBy = "";
    for (int i = 0; i < groups.length; i++) {
      strGroupBy += (0 < i ? ",\n" : "") + "  " + groups[i];
    }

    //  build the query
		/*return
			"INSERT INTO plot_data\n" +
			"SELECT\n" + strSelectListStat + ",\n" +
			"  object_id,\n" +
			"  object_cat,\n" +
			"  '" + stat + "' stat_name,\n" +
			"  " + strTableStat + " stat_value\n" +  
			"FROM mode_single\n" + 
			"WHERE\n" + strWhere + "\n" +
			"GROUP BY\n" + strGroupBy + ";";
    */
    return
      "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
        "  object_id,\n" +
        "  object_cat,\n" +
        "  area,\n" +
        "  total,\n" +
        "  fcst_flag,\n" +
        "  simple_flag,\n" +
        "  matched_flag\n" +
        "FROM mode_single\n" +
        "WHERE\n" + strWhere + ";";
  }

  public static String buildModeSingleAcovTable(String selectList, String strWhere, String stat, String[] groups) {

    //  parse the stat into the stat name and the object flags
    String[] listStatParse = parseModeStat(stat);
    if (2 != listStatParse.length) {
      return "";
    }
    String strStatFlag = listStatParse[1];

    //  build the query components
    String strSelectListStat = selectList.replaceAll("h\\.", "").replaceAll(",\\s+$", "");
    String strStat = "SUM(area) / (2*total)";
    String strGroupBy = strSelectListStat;
    strGroupBy = strGroupBy.replaceAll("DATE_FORMAT\\(.*\\) ", "");
    if (strStatFlag.charAt(0) != 'A') {
      strStat = "SUM(area) / total";
      strGroupBy += ",\n  fcst_flag";
      strWhere += "\n  AND fcst_flag = " + ('F' == strStatFlag.charAt(0) ? "1" : "0");
    }

    //  build the query
    return
      "INSERT INTO plot_data\n" +
        "SELECT\n" + strSelectListStat + ",\n" +
        "  '' object_id,\n" +
        "  '' object_cat,\n" +
        "  '" + stat + "' stat_name,\n" +
        "  " + strStat + " stat_value\n" +
        "FROM mode_single\n" +
        "WHERE\n" +
        strWhere + "\n" +
        "  AND simple_flag = 1\n" +
        "GROUP BY\n" + strGroupBy + ";";
  }
}

package edu.ucar.metviewer;


import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseManager;
import edu.ucar.metviewer.jobManager.*;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;


public class MVBatch {


  private PrintStream printStream;
  private PrintStream printStreamSql;
  private PrintStream printStreamEr;


  private boolean verbose = false;
  private String rtmplFolder = "";
  private String rworkFolder = "";
  private String plotsFolder = "";
  private String dataFolder = "";
  private String scriptsFolder = "";
  private int numPlots = 0;
  private int numPlotsRun = 0;
  private AppDatabaseManager databaseManager;
  private String dbType;
  private String metCalcpyHome;
  private String metPlotpyHome;
  private String pythonEnv;
  private String python;


  public String getMetCalcpyHome() {
    return metCalcpyHome;
  }

  public void setMetCalcpyHome(String metCalcpyHome) {
    this.metCalcpyHome = metCalcpyHome;
  }

  public String getMetPlotpyHome() {
    return metPlotpyHome;
  }

  public void setMetPlotpyHome(String metPlotpyHome) {
    this.metPlotpyHome = metPlotpyHome;
  }

  public String getPythonEnv() {
    return pythonEnv;
  }

  public String getPython() {
    return python;
  }

  public void setPythonEnv(String pythonEnv) {
    this.pythonEnv = pythonEnv;
    this.python = pythonEnv + "/bin/python";
  }

  public MVBatch(
          PrintStream log, PrintStream printStreamSql, PrintStream printStreamEr,
          AppDatabaseManager manager) {
    super();

    this.printStream = log;
    this.printStreamSql = printStreamSql;
    this.printStreamEr = printStreamEr;
    databaseManager = manager;
  }

  public MVBatch() {

    this(IoBuilder.forLogger(MVBatch.class).setLevel(org.apache.logging.log4j.Level.INFO)
                    .buildPrintStream(),
            IoBuilder.forLogger(MVBatch.class).setLevel(org.apache.logging.log4j.Level.INFO)
                    .buildPrintStream(),
            IoBuilder.forLogger().setLevel(org.apache.logging.log4j.Level.INFO)
                    .setMarker(new MarkerManager.Log4jMarker("ERROR"))
                    .buildPrintStream(), null);
  }

  public void print(String message) {
    if (this.printStream != null) {
      this.printStream.println(message);
      this.printStream.flush();
    }
  }

  public void printSql(String message) {
    if (this.printStreamSql != null) {
      this.printStreamSql.print(message);
      this.printStreamSql.flush();
    }
  }

  public void printError(String message) {
    if (this.printStreamEr != null) {
      this.printStreamEr.print(message);
      this.printStreamEr.flush();
    }
  }

  public String getDataFolder() {
    if (!dataFolder.endsWith(File.separator)) {
      dataFolder = dataFolder + File.separator;
    }
    return dataFolder;
  }

  public void setDataFolder(String dataFolder) {
    this.dataFolder = dataFolder;
  }

  public AppDatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public void setDatabaseManager(AppDatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void closeDataSource() {
    if (this.databaseManager != null) {
      this.databaseManager.closeDataSource();
    }
  }


  public int getNumPlots() {
    return numPlots;
  }

  public void setNumPlots(int numPlots) {
    this.numPlots = numPlots;
  }

  public int getNumPlotsRun() {
    return numPlotsRun;
  }

  public void setNumPlotsRun(int numPlotsRun) {
    this.numPlotsRun = numPlotsRun;
  }

  public String getPlotsFolder() {
    if (!plotsFolder.endsWith(File.separator)) {
      plotsFolder = plotsFolder + File.separator;
    }
    return plotsFolder;
  }

  public void setPlotsFolder(String plotsFolder) {
    this.plotsFolder = plotsFolder;
  }

  public PrintStream getPrintStream() {
    return printStream;
  }


  public String getRworkFolder() {
    if (!rworkFolder.endsWith(File.separator)) {
      rworkFolder = rworkFolder + File.separator;
    }
    return rworkFolder;
  }

  public void setRworkFolder(String rworkFolder) {
    this.rworkFolder = rworkFolder;
  }

  public PrintStream getPrintStreamSql() {
    return printStreamSql;
  }


  public String getRtmplFolder() {
    if (!rtmplFolder.endsWith(File.separator)) {
      rtmplFolder = rtmplFolder + File.separator;
    }
    return rtmplFolder;
  }

  public void setRtmplFolder(String rtmplFolder) {
    this.rtmplFolder = rtmplFolder;
  }

  public String getScriptsFolder() {
    if (!scriptsFolder.endsWith(File.separator)) {
      scriptsFolder = scriptsFolder + File.separator;
    }
    return scriptsFolder;
  }

  public void setScriptsFolder(String scriptsFolder) {
    this.scriptsFolder = scriptsFolder;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void setDbType(String dbType) {
    this.dbType = dbType;
  }

  public String getDbType() {
    return dbType;
  }

  public static String getUsage() {
    String version = MVUtil.getVersionNumber();
    String message;
    if (!version.isEmpty()) {
      message = "Version: " + version + "\n";
    } else {
      message = "";
    }
    return message +
            "Usage:  mv_batch\n"
            + "          [-list]\n"
            + "          [-printSql]\n"
            + "          plot_spec_file\n"
            + "          [job_name]\n"
            + "\n"
            + "        where     \"-list\" indicates that the available plot jobs should be "
            + "listed and no plots run\n"
            + "                  \"-printSql\" print SQL statements\n"
            + "                  \"plot_spec_file\" specifies the XML plot specification "
            + "document\n"
            + "                  \"job_name\" specifies the name of the job from the plot "
            + "specification to run\n";
  }

  public static void main(String[] argv) throws Exception {

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    MVBatch mvBatch = new MVBatch();

    mvBatch.print("----  MVBatch  ----\n");
    try {
      MVPlotJob[] jobs;
      // if no input file is present, bail
      if (1 > argv.length) {
        mvBatch.print(getUsage() + "\n----  MVBatch Done  ----");
        return;
      }
      //  parse the command line options
      boolean boolList = false;
      int intArg = 0;
      for (; intArg < argv.length && !argv[intArg].matches(".*\\.xml$"); intArg++) {
        if (argv[intArg].equals("-list")) {
          boolList = true;
        } else if (argv[intArg].equals("-printSql")) {
          mvBatch.setVerbose(true);
        }else if ("-h".equalsIgnoreCase(argv[0]) || "--h".equalsIgnoreCase(argv[0]) || "-help".equalsIgnoreCase(argv[0])) {
          mvBatch.print(getUsage() + "\n\n----  MVBatch Done  ----");
          return;
        } else {
          mvBatch.print(
                  "  **  ERROR: unrecognized option '"
                          + argv[intArg] + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
          return;
        }
      }

      String version  = MVUtil.getVersionNumber();
      if (!version.isEmpty()){
        mvBatch.print("Version: " + version + "\n");
      }
      mvBatch.setPythonEnv(System.getProperty("python.env"));
      mvBatch.setMetCalcpyHome(System.getProperty("metcalcpy.home"));
      mvBatch.setMetPlotpyHome(System.getProperty("metplotpy.home"));

      //  parse the input file
      String xmlInput = argv[intArg++];
      mvBatch.print("input file: " + xmlInput + "\n");

      MVPlotJobParser parser = new MVPlotJobParser(xmlInput);
      MVOrderedMap mapJobs = parser.getJobsMap();

      //  build a list of jobs to run
      List<String> listJobNamesInput = new ArrayList<>();
      for (; intArg < argv.length; intArg++) {
        listJobNamesInput.add(argv[intArg]);
      }
      String[] listJobNames = mapJobs.getKeyList();
      if (!listJobNamesInput.isEmpty()) {
        listJobNames = MVUtil.toArray(listJobNamesInput);
      }
      mvBatch.print((boolList ? "" : "processing ") + listJobNames.length + " jobs:");
      for (String listJobName : listJobNames) {
        mvBatch.print("  " + listJobName);
      }


      //  if only a list of plot jobs is requested, return
      if (boolList) {
        mvBatch.print("\n----  MVBatch Done  ----");
        return;
      }

      //  if a job name is present, run only that job, otherwise run all jobs
      if (1 > listJobNames.length) {
        jobs = parser.getJobsList();
      } else {
        ArrayList listJobs = new ArrayList();
        for (String listJobName : listJobNames) {
          if (!mapJobs.containsKey(listJobName)) {
            mvBatch.printStream.println("  **  WARNING: unrecognized job \"" + listJobName + "\"");
            continue;
          }
          listJobs.add(mapJobs.get(listJobName));
        }
        jobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[listJobs.size()]);
      }
      mvBatch.setDatabaseManager((AppDatabaseManager) DatabaseManager.getAppManager(parser.dbManagementSystem, parser.dbHost, parser.dbUser, parser.dbPass,
              jobs[0].getCurrentDBName().get(0)));
      mvBatch.setRtmplFolder(parser.getRtmplFolder()
              + (parser.getRtmplFolder()
              .endsWith(File.separator) ? "" : File.separator));
      mvBatch.setRworkFolder(parser.getRworkFolder()
              + (parser.getRworkFolder()
              .endsWith(File.separator) ? "" : File.separator));
      mvBatch.setPlotsFolder(parser.getPlotsFolder()
              + (parser.getPlotsFolder()
              .endsWith(File.separator) ? "" : File.separator));
      mvBatch.setDataFolder(parser.getDataFolder());


      mvBatch.setScriptsFolder(parser.getScriptsFolder()
              + (parser.getScriptsFolder().endsWith(File.separator) ? "" :
              File.separator));


      //  calculate the number of plots
      for (MVPlotJob job : jobs) {

        //  add a job for each permutation of plot fixed values
        Map.Entry[] plotFix = job.getPlotFixVal().getOrderedEntries();
        int intNumJobPlots = 1;
        for (Map.Entry plotFixEntry : plotFix) {
          Object objFixVal = plotFixEntry.getValue();
          if (objFixVal instanceof String[]) {
            intNumJobPlots *= ((String[]) objFixVal).length;
          } else if (objFixVal instanceof MVOrderedMap) {
            intNumJobPlots *= ((MVOrderedMap) objFixVal).size();
          }
        }

        mvBatch.setNumPlots(mvBatch.getNumPlots() + intNumJobPlots);
      }
      StopWatch jobsStopWatch = new StopWatch();
      mvBatch.print("Running " + mvBatch.numPlots + " plots");


      for (int intJob = 0; intJob < jobs.length; intJob++) {
        jobsStopWatch.start();
        if (0 < intJob) {
          mvBatch.print(
                  "\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
        }
        JobManager jobManager;
        switch (jobs[intJob].getPlotTmpl()) {
          case "rhist.R_tmpl":
            jobManager = new RhistJobManager(mvBatch);
            break;
          case "phist.R_tmpl":
            jobManager = new RhistJobManager(mvBatch);
            break;
          case "relp.R_tmpl":
            jobManager = new RelpJobManager(mvBatch);
            break;
          case "roc.R_tmpl":
            jobManager = new RocJobManager(mvBatch);
            break;
          case "rely.R_tmpl":
            jobManager = new RelyJobManager(mvBatch);
            break;
          case "eclv.R_tmpl":
            jobManager = new EclvJobManager(mvBatch);
            break;
          case "taylor_plot.R_tmpl":
            jobManager = new TaylorJobManager(mvBatch);
            break;
          case "performance.R_tmpl":
            jobManager = new PerformanceJobManager(mvBatch);
            break;
          case "ens_ss.R_tmpl":
            jobManager = new EnsSsJobManager(mvBatch);
            break;
          case "contour_plot.R_tmpl":
            jobManager = new ContourJobManager(mvBatch);
            break;
          default:
            if (MVUtil.isEtbJob(jobs[intJob])){
              jobManager = new EtbJobManager(mvBatch);
            }else {
              jobManager = new SeriesJobManager(mvBatch);
            }
            break;
        }
        jobManager.runJob(jobs[intJob]);

        mvBatch.numPlotsRun++;
        jobsStopWatch.stop();
        mvBatch.print("\n" + "Job " + (intJob + 1) + " execution time " + jobsStopWatch.getFormattedDuration());

      }
      stopWatch.stop();
      long plotAvg = (jobsStopWatch.getTotalDuration() / 1000000) / (long) mvBatch.numPlots;

      mvBatch.print("\n"
              + MVUtil.padBegin("Plots run: ") + mvBatch.getNumPlotsRun() + " of " + mvBatch.getNumPlots()
              + "\n"
              + MVUtil.padBegin("Total time: ") + jobsStopWatch.getFormattedTotalDuration() + "\n"
              + MVUtil.padBegin("Avg plot time: ") + MVUtil.formatTimeSpan(plotAvg) + "\n");


    } catch (Exception e) {
      stopWatch.stop();
      mvBatch.print("  **  ERROR:  " + e.getMessage());
    }
    mvBatch.closeDataSource();

    mvBatch.print("----  MVBatch Done  ----");

    mvBatch.print("\nTotal execution time " + stopWatch.getFormattedTotalDuration());

  }

}

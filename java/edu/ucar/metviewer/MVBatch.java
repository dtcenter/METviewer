package edu.ucar.metviewer;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;
import edu.ucar.metviewer.db.CBAppDatabaseManager;
import edu.ucar.metviewer.jobManager.ContourJobManager;
import edu.ucar.metviewer.jobManager.EclvJobManager;
import edu.ucar.metviewer.jobManager.EnsSsJobManager;
import edu.ucar.metviewer.jobManager.JobManager;
import edu.ucar.metviewer.jobManager.PerformanceJobManager;
import edu.ucar.metviewer.jobManager.RelpJobManager;
import edu.ucar.metviewer.jobManager.RelyJobManager;
import edu.ucar.metviewer.jobManager.RhistJobManager;
import edu.ucar.metviewer.jobManager.RocJobManager;
import edu.ucar.metviewer.jobManager.SeriesJobManager;
import edu.ucar.metviewer.jobManager.TaylorJobManager;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;

public class MVBatch  {


  private PrintStream printStream;
  private PrintWriter printStreamSql;
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
  private String dbType = "mysql";


  public MVBatch(
                    PrintStream log, PrintWriter printStreamSql, PrintStream printStreamEr,
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
             .buildPrintWriter(),
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

  public PrintWriter getPrintStreamSql() {
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
    return "Usage:  mv_batch\n"
               + "          [-list]\n"
               + "          [-printSql]\n"
               + "          db_type\n"
               + "          plot_spec_file\n"
               + "          [job_name]\n"
               + "\n"
               + "        where     \"-list\" indicates that the available plot jobs should be "
               + "listed and no plots run\n"
               + "                  \"-printSql\" print SQL statements\n"
               + "                  \"db_type\" specifies database type (available values : mysql)"
               + "\n"
               + "                  \"plot_spec_file\" specifies the XML plot specification "
               + "document\n"
               + "                  \"job_name\" specifies the name of the job from the plot "
               + "specification to run\n";
  }

  public static void main(String[] argv) {
    MVBatch bat = new MVBatch();

    bat.print("----  MVBatch  ----\n");

    try {

      MVPlotJob[] jobs;

      //  if no input file is present, bail
      if (1 > argv.length) {
        bat.print(getUsage() + "\n----  MVBatch Done  ----");
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
        } else if (argv[intArg].equalsIgnoreCase("mysql")) {
          bat.setDbType("mysql");
        } else if (argv[intArg].equalsIgnoreCase("CB")) {
          bat.setDbType("CB");
        } else {
          bat.print(
              "  **  ERROR: unrecognized option '"
                  + argv[intArg] + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
          return;
        }
      }

      //  parse the input file
      String xmlInput = argv[intArg];
      bat.print("input file: " + xmlInput + "\n");
      intArg++;

      MVPlotJobParser parser = new MVPlotJobParser(xmlInput);
      if (bat.getDbType() == null || bat.getDbType().equals("mysql")) {
        bat.setDatabaseManager(
            new MysqlAppDatabaseManager(parser.getDatabaseInfo(), bat.getPrintStreamSql()));
      } else if (bat.getDbType().equals("CB")) {
        bat.setDatabaseManager(
                new CBAppDatabaseManager(parser.getDatabaseInfo(), bat.getPrintStreamSql()));
      }
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
      bat.print((boolList ? "" : "processing ") + listJobNames.length + " jobs:");
      for (String listJobName : listJobNames) {
        bat.print("  " + listJobName);
      }


      //  if only a list of plot jobs is requested, return
      if (boolList) {
        bat.print("\n----  MVBatch Done  ----");
        return;
      }

      //  if a job name is present, run only that job, otherwise run all jobs
      if (1 > listJobNames.length) {
        jobs = parser.getJobsList();
      } else {
        ArrayList listJobs = new ArrayList();
        for (String listJobName : listJobNames) {
          if (!mapJobs.containsKey(listJobName)) {
            bat.printStream.println("  **  WARNING: unrecognized job \"" + listJobName + "\"");
            continue;
          }
          listJobs.add(mapJobs.get(listJobName));
        }
        jobs = (MVPlotJob[]) listJobs.toArray(new MVPlotJob[listJobs.size()]);
      }

      bat.setRtmplFolder(parser.getRtmplFolder()
                             + (parser.getRtmplFolder()
                                    .endsWith(File.separator) ? "" : File.separator));
      bat.setRworkFolder(parser.getRworkFolder()
                             + (parser.getRworkFolder()
                                    .endsWith(File.separator) ? "" : File.separator));
      bat.setPlotsFolder(parser.getPlotsFolder()
                             + (parser.getPlotsFolder()
                                    .endsWith(File.separator) ? "" : File.separator));
      bat.setDataFolder(parser.getDataFolder());


      bat.setScriptsFolder(parser.getScriptsFolder()
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

        bat.setNumPlots(bat.getNumPlots() + intNumJobPlots);
      }
      LocalDateTime dateStart = LocalDateTime.now();
      bat.print("Running " + bat.numPlots + " plots\n"
                    + "Begin time: " + MVUtil.APP_DATE_FORMATTER.format(dateStart) + "\n");


      for (int intJob = 0; intJob < jobs.length; intJob++) {
        if (0 < intJob) {
          bat.print(
              "\n# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
        }
        JobManager jobManager;
        switch (jobs[intJob].getPlotTmpl()) {
          case "rhist.R_tmpl":
            jobManager = new RhistJobManager(bat);
            break;
          case "phist.R_tmpl":
            jobManager = new RhistJobManager(bat);
            break;
          case "relp.R_tmpl":
            jobManager = new RelpJobManager(bat);
            break;
          case "roc.R_tmpl":
            jobManager = new RocJobManager(bat);
            break;
          case "rely.R_tmpl":
            jobManager = new RelyJobManager(bat);
            break;
          case "eclv.R_tmpl":
            jobManager = new EclvJobManager(bat);
            break;
          case "taylor_plot.R_tmpl":
            jobManager = new TaylorJobManager(bat);
            break;
          case "performance.R_tmpl":
            jobManager = new PerformanceJobManager(bat);
            break;
          case "ens_ss.R_tmpl":
            jobManager = new EnsSsJobManager(bat);
            break;
          case "contour_plot.R_tmpl":
            jobManager = new ContourJobManager(bat);
            break;
          default:
            jobManager = new SeriesJobManager(bat);
            break;
        }
        jobManager.runJob(jobs[intJob]);

        bat.numPlotsRun++;

      }

      LocalDateTime dateEnd = LocalDateTime.now();
      long plotTime = dateStart.until(dateEnd, ChronoUnit.MILLIS);
      long plotAvg = 0 < bat.numPlots ? plotTime / (long) bat.numPlots : 0;
      bat.print("\n"
                    + MVUtil.padBegin("End time: ") + MVUtil.APP_DATE_FORMATTER.format(dateEnd) + "\n"
                    + MVUtil.padBegin("Plots run: ") + bat.getNumPlotsRun() + " of " + bat.getNumPlots()
                    + "\n"
                    + MVUtil.padBegin("Total time: ") + MVUtil.formatTimeSpan(plotTime) + "\n"
                    + MVUtil.padBegin("Avg plot time: ") + MVUtil.formatTimeSpan(plotAvg) + "\n");

    } catch (Exception e) {
      bat.print("  **  ERROR:  " + e.getMessage());
    }

    bat.print("----  MVBatch Done  ----");
  }

}

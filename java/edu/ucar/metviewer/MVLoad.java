package edu.ucar.metviewer;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import edu.ucar.metviewer.db.DatabaseManager;
import edu.ucar.metviewer.db.LoadDatabaseManager;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.io.IoBuilder;

public class MVLoad {

  private static final Logger logger = LogManager.getLogger("MVLoad");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");
  private static LoadDatabaseManager loadDatabaseManager;


  private static final long statHeaderTableTime = 0;
  private static final int statGroupRecords = 0;
  private static final int statGroupInserts = 0;

  //  line_type_lu_id values for the various mode line types


  private static boolean verbose = false;
  private static int insertSize = 1;
  private static boolean modeHeaderDBCheck = false;
  private static boolean mtdHeaderDBCheck = false;
  private static boolean statHeaderDBCheck = false;
  private static boolean indexOnly = false;
  private static boolean lineTypeLoad = false;
  private static Map tableLineTypeLoad = new HashMap<>();
  private static boolean loadStat = true;
  private static boolean loadMode = true;
  private static boolean loadMtd = true;
  private static boolean loadMpr = false;
  private static boolean loadOrank = false;
  private static boolean forceDupFile = false;
  private static long statHeaderSearchTime = 0;
  private static long modeHeaderSearchTime = 0;
  private static long mtdHeaderSearchTime = 0;
  private static int numStatFiles = 0;
  private static int statLinesTotal = 0;
  private static int statHeaderRecords = 0;
  private static int statHeaderInserts = 0;
  private static int lineDataRecords = 0;
  private static int lineDataInserts = 0;
  private static int lengthRecords = 0;
  private static int lengthInserts = 0;
  private static int numModeFiles = 0;
  private static int numMtdFiles = 0;
  private static int modeLinesTotal = 0;
  private static int mtdLinesTotal = 0;
  private static int modeHeaderRecords = 0;
  private static int mtdHeaderRecords = 0;
  private static int modeCtsRecords = 0;
  private static int modeObjSingleRecords = 0;
  private static int mtdObj3dSingleRecords = 0;
  private static int modeObjPairRecords = 0;
  private static int mtdObj3dPairRecords = 0;
  private static int mtdObj2dRecords = 0;


  private MVLoad() {
  }

  public static void main(String[] argv) {

    logger.info("----  MVLoad  ----\n");

    try {

      //  parse the input arguments
      if (1 > argv.length) {
        logger.info(getUsage() + "\n\n----  MVLoad Done  ----");
        return;
      }

      int intArg = 0;
      for (; intArg < argv.length && !argv[intArg].matches(".*\\.xml$"); intArg++) {
        if ("-index".equalsIgnoreCase(argv[0])) {
          indexOnly = true;
        }else if ("-h".equalsIgnoreCase(argv[0]) || "--h".equalsIgnoreCase(argv[0]) || "-help".equalsIgnoreCase(argv[0])) {
          logger.info(getUsage() + "\n\n----  MVLoad Done  ----");
          return;
        } else {
          logger.error(
                  "  **  ERROR: unrecognized option '" + argv[intArg]
                          + "'\n\n" + getUsage() + "\n----  MVBatch Done  ----");
          return;
        }
      }
      String strXML = argv[intArg];

      //  parse the plot job
      String version  = MVUtil.getVersionNumber();
      if (!version.isEmpty()){
        logger.info("Version: " + version + "\n");
      }

      logger.info("Begin time: " + MVUtil.APP_DATE_FORMATTER.format(LocalDateTime.now()));
      logger.info("Parsing: " + strXML + "\n"
              + (indexOnly ? "Applying Index Settings Only\n" : ""));
      MVLoadJobParser parser = new MVLoadJobParser(strXML);
      MVLoadJob job = parser.getLoadJob();

      String management_system = parser.getLoadJob().getDBManagementSystem();
      loadDatabaseManager = (LoadDatabaseManager) DatabaseManager.getLoadManager(management_system, job.getDBHost(), job.getDBUser(), job.getDBPassword(), job.getDBName());
      verbose = job.getVerbose();
      insertSize = job.getInsertSize();
      modeHeaderDBCheck = job.getModeHeaderDBCheck();
      mtdHeaderDBCheck = job.getMtdHeaderDBCheck();
      statHeaderDBCheck = job.getStatHeaderDBCheck();

      lineTypeLoad = job.getLineTypeLoad();
      tableLineTypeLoad = job.getLineTypeLoadMap();

      loadStat = job.getLoadStat();
      loadMode = job.getLoadMode();
      loadMtd = job.getLoadMtd();

      loadMpr = job.getLoadMpr();
      loadOrank = job.getLoadOrank();

      forceDupFile = job.getForceDupFile();

      //  process the instance_info load information
      boolean boolLoadNote = !(job.getLoadNote().length() == 0);


      //  if the insert size is greater than 1, ensure that the db header check is off
      if (1 < insertSize) {
        throw new Exception("METviewer load error: insert size ("
                + insertSize + ") > 1 and database header check turned on");
      }

      long intLoadTimeStart = new Date().getTime();
      if (job.getGroup() != null) {
        loadDatabaseManager.updateGroup(job.getGroup());
      }
      if (job.getDescription() != null) {
        loadDatabaseManager.updateDescription(job.getDescription());
      }

      //  drop the database indexes, if requested
      boolean dropIndexes = job.getDropIndexes();
      if (dropIndexes) {
        loadDatabaseManager.dropIndexes();
      }

      //  if the job involves only applying indexes, do so and return
      boolean applyIndexes = job.getApplyIndexes();
      if (indexOnly && applyIndexes) {
        loadDatabaseManager.applyIndexes();
        logger.info("\n----  MVLoad Done  ----");
        return;
      }


      //  if there are <load_file> files specified, load them
      String[] listLoadFiles = job.getLoadFiles();
      File file;
      if (!indexOnly && listLoadFiles != null) {
        for (int i = 0; i < listLoadFiles.length; i++) {
          try {
            file = new File(listLoadFiles[i]);
            processFile(file);
          } catch (Exception e) {
            logger.error(ERROR_MARKER,
                    "  **  ERROR: caught " + e.getClass() + " loading file "
                            + listLoadFiles[i] + ": " + e.getMessage());
            logger.error(e);
            logger.info(
                    "  **  WARNING: error(s) encountered loading file "
                            + listLoadFiles[i] + " - skipping file");
          }
        }
      }

      //  if there is a file template specified, load it
      if (!indexOnly && null != job.getFolderTmpl() && !(job.getFolderTmpl().length() == 0)) {
        int intStatLinesPrev = 0;
        int intModeLinesPrev = 0;
        int intMtdLinesPrev = 0;

        //  build a folder with each permutation of load values and load the data therein
        MVOrderedMap[] listPerm = MVUtil.permute(job.getLoadVal()).getRows();
        String baseFolder;
        long intPermStart;
        File fileBaseFolder;
        File[] listDataFiles;
        for (int intPerm = 0; intPerm < listPerm.length; intPerm++) {
          PrintStream printStream = null;
          try {
             printStream = IoBuilder.forLogger(MVLoad.class)
                    .setLevel(org.apache.logging.log4j.Level.INFO)
                    .buildPrintStream();
          }catch (Exception e){
            System.out.println(e.getMessage());
          }
          //  determine the name of the current folder
          baseFolder = MVUtil.buildTemplateString(job.getFolderTmpl(), listPerm[intPerm],
                  printStream);
          baseFolder = MVUtil.cleanString(baseFolder);
          if (printStream != null) {
            printStream.close();
          }
          logger.info(
                  "Permutation " + (intPerm + 1) + " of " + listPerm.length + " - " + baseFolder);
          intPermStart = new Date().getTime();

          //  try to access the folder and its contents, and continue if it does not exist
          fileBaseFolder = new File(baseFolder);
          if (fileBaseFolder.exists()) {

            //  process each file in the folder
            listDataFiles = fileBaseFolder.listFiles();
            if (listDataFiles != null) {
              for (File listDataFile : listDataFiles) {
                try {
                  processFile(listDataFile);
                } catch (Exception e) {
                  logger.error(ERROR_MARKER, "  **  ERROR: caught " + e.getClass() + " in processFile()\n"
                          + e.getMessage() + "\n"
                          + "  **  WARNING: error(s) encountered loading file "
                          + listDataFile + " - skipping file");
                  logger.error(e);
                }
              }
            }

            //  bookkeeping
            int intStatLinesPerm = statLinesTotal - intStatLinesPrev;
            int intModeLinesPerm = modeLinesTotal - intModeLinesPrev;
            int intMtdLinesPerm = mtdLinesTotal - intMtdLinesPrev;
            intStatLinesPrev = statLinesTotal;
            intModeLinesPrev = modeLinesTotal;
            intMtdLinesPrev = mtdLinesTotal;
            logger.info(
                    "Permutation " + (intPerm + 1) + " of " + listPerm.length
                            + " complete - insert time: "
                            + MVUtil.formatTimeSpan(new Date().getTime() - intPermStart)
                            + "  stat lines: " + intStatLinesPerm
                            + "  mode lines: " + intModeLinesPerm + "\n" + "  mtd lines: "
                            + intMtdLinesPerm + "\n");
          }
        }
      }

      //  print a performance report
      long intLoadTime = new Date().getTime() - intLoadTimeStart;
      double dblLinesPerMSec = (double) statLinesTotal / (double) (intLoadTime);
      if (!indexOnly) {
        logger.info("==== grid_stat ====\n\n" +

                MVUtil.padBegin("stat_header table time total: ", 36)
                + MVUtil.formatTimeSpan(statHeaderTableTime) + "\n" +
                MVUtil.padBegin("stat header records: ", 36) + statHeaderRecords + "\n" +
                MVUtil.padBegin("stat header inserts: ", 36) + statHeaderInserts + "\n" +
                MVUtil.padBegin("line data records: ", 36) + lineDataRecords + "\n" +
                MVUtil.padBegin("line data inserts: ", 36) + lineDataInserts + "\n" +
                MVUtil.padBegin("stat group records: ", 36) + statGroupRecords + "\n" +
                MVUtil.padBegin("stat group inserts: ", 36) + statGroupInserts + "\n" +
                MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
                MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
                MVUtil.padBegin("total lines: ", 36) + statLinesTotal + "\n" +
                MVUtil.padBegin("insert size: ", 36) + insertSize + "\n" +
                MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf(
                dblLinesPerMSec) + "\n" +
                MVUtil.padBegin("num files: ", 36) + numStatFiles + "\n\n" +
                "    ==== mode ====\n\n" +
                (modeHeaderDBCheck ? MVUtil.padBegin("mode_header search time total: ",
                        36) + MVUtil.formatTimeSpan(
                        modeHeaderSearchTime) + "\n" : "") +
                (mtdHeaderDBCheck ? MVUtil.padBegin("mtd_header search time total: ", 36)
                        + MVUtil.formatTimeSpan(mtdHeaderSearchTime)
                        + "\n" : "") +
                (statHeaderDBCheck ? MVUtil.padBegin("stat_header search time total: ", 36)
                        + MVUtil.formatTimeSpan(statHeaderSearchTime)
                        + "\n" : "") +
                MVUtil.padBegin("mode_header inserts: ", 36) + modeHeaderRecords + "\n" +
                MVUtil.padBegin("mode_cts inserts: ", 36) + modeCtsRecords + "\n" +
                MVUtil.padBegin("mode_obj_single inserts: ",
                        36) + modeObjSingleRecords + "\n" +
                MVUtil.padBegin("mode_obj_pair inserts: ", 36) + modeObjPairRecords + "\n" +
                "    ==== mtd ====\n\n" +
                MVUtil.padBegin("mtd_header inserts: ", 36) + mtdHeaderRecords + "\n" +
                MVUtil.padBegin("mtd_3d_obj_single inserts: ", 36) +
                mtdObj3dSingleRecords + "\n" +
                MVUtil.padBegin("mtd_3d_obj_pair inserts: ", 36) +
                mtdObj3dPairRecords + "\n" +
                MVUtil.padBegin("mtd_2d_obj inserts: ", 36) +
                mtdObj2dRecords + "\n" +
                MVUtil.padBegin("total lines: ", 36) + modeLinesTotal + "\n" +
                MVUtil.padBegin("num files: ", 36) + numModeFiles + "\n" +
                MVUtil.padBegin("num files: ", 36) + numMtdFiles + "\n"
        );
      }

      //  apply the indexes, if requested
      if (applyIndexes) {
        loadDatabaseManager.applyIndexes();
      }

      //  update the instance_info table, if requested
      if (boolLoadNote) {
        loadDatabaseManager.updateInfoTable(strXML, job);

      }

      logger.info("End time: " + MVUtil.APP_DATE_FORMATTER.format(LocalDateTime.now()));
      logger.info("Load total: " + MVUtil.formatTimeSpan(intLoadTime) + "\n");
    } catch (Exception e) {
      logger.error(ERROR_MARKER, "  **  ERROR: Caught " + e.getClass() + ": " + e.getMessage());
      logger.error(e);
    }

    logger.info("----  MVLoad Done  ----");
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
            "Usage:  mv_load\n" +
            "          [-index]\n" +
            "          load_spec_file\n" +
            "\n" +
            "          where   \"-index\" indicates that no data should be loaded, and only the indexing commands applied\n" +
            "                  \"load_spec_file\" specifies the XML load specification document\n";
  }

  /**
   * Attempt to load the input file into the database data_file table, and then, if successful, into
   * the appropriate set of tables: stat or mode.
   *
   * @param file File to process //* @param con Connection to the database to load
   * @throws Exception
   */
  public static void processFile(File file) throws DatabaseException {
    long intProcessDataFileBegin = new Date().getTime();
    DataFileInfo info = loadDatabaseManager.processDataFile(file, forceDupFile);
    if (null == info) {
      return;
    }
    info.lineTypeLoad = lineTypeLoad;
    info.tableLineTypeLoad = tableLineTypeLoad;
    info.loadMpr = loadMpr;
    info.loadOrank = loadOrank;
    info.statHeaderDBCheck = statHeaderDBCheck;
    info.modeHeaderDBCheck = modeHeaderDBCheck;
    info.mtdHeaderDBCheck = mtdHeaderDBCheck;
    info.verbose = verbose;
    info.insertSize = insertSize;
    long intProcessDataFileTime = new Date().getTime() - intProcessDataFileBegin;
    String strFileMsg = "  " + info.path + "/"
            + info.filename + (verbose ? "\n" + MVUtil.padBegin("data file time: ", 36)
            + MVUtil.formatTimeSpan(intProcessDataFileTime) : "");

    if ("stat".equals(info.luTypeName) && loadStat) {
      logger.info(strFileMsg);
      Map<String, Long> timeStats = loadDatabaseManager.loadStatFile(info);
      statHeaderSearchTime += timeStats.get("headerSearchTime");
      statLinesTotal += timeStats.get("linesTotal");
      statHeaderRecords += timeStats.get("headerRecords");
      statHeaderInserts += timeStats.get("headerInserts");
      lineDataInserts += timeStats.get("dataInserts");
      lineDataRecords += timeStats.get("dataRecords");
      lengthRecords += timeStats.get("lengthRecords");
      lengthInserts += timeStats.get("lengthInserts");

      numStatFiles++;
    } else if (("mode_obj".equals(info.luTypeName)
            || "mode_cts".equals(info.luTypeName))
            && loadMode) {
      logger.info(strFileMsg);
      Map<String, Long> timeStats = loadDatabaseManager.loadModeFile(info);
      modeHeaderSearchTime += timeStats.get("headerSearchTime");
      modeHeaderRecords += timeStats.get("headerInserts");
      modeCtsRecords += timeStats.get("ctsInserts");
      modeObjSingleRecords += timeStats.get("objSingleInserts");
      modeObjPairRecords += timeStats.get("objPairInserts");


      modeLinesTotal += timeStats.get("linesTotal");
      numModeFiles++;
    } else if (("mtd_2d".equals(info.luTypeName)
            || "mtd_3d_pc".equals(info.luTypeName)
            || "mtd_3d_ps".equals(info.luTypeName)
            || "mtd_3d_sc".equals(info.luTypeName)
            || "mtd_3d_ss".equals(info.luTypeName))
            && loadMtd) {
      logger.info(strFileMsg);
      Map<String, Long> timeStats = loadDatabaseManager.loadMtdFile(info);
      mtdHeaderSearchTime += timeStats.get("headerSearchTime");
      mtdHeaderRecords += timeStats.get("headerInserts");
      mtdObj3dSingleRecords += timeStats.get("obj3dSingleInserts");
      mtdObj3dPairRecords += timeStats.get("obj3dPairInserts");
      mtdObj2dRecords += timeStats.get("obj2dInserts");


      mtdLinesTotal += timeStats.get("linesTotal");
      numMtdFiles++;
    } else if ("vsdb_point_stat".equals(info.luTypeName) && loadStat) {
      logger.info(strFileMsg);
      Map<String, Long> timeStats = loadDatabaseManager.loadStatFileVSDB(info);
      statHeaderSearchTime += timeStats.get("headerSearchTime");
      statLinesTotal += timeStats.get("linesTotal");
      statHeaderRecords += timeStats.get("headerRecords");
      statHeaderInserts += timeStats.get("headerInserts");
      lineDataInserts += timeStats.get("dataInserts");
      lineDataRecords += timeStats.get("dataRecords");
      lengthRecords += timeStats.get("lengthRecords");
      lengthInserts += timeStats.get("lengthInserts");
      numStatFiles++;
    }
  }

}


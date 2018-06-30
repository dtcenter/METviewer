package edu.ucar.metviewer.test;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;
import edu.ucar.metviewer.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static edu.ucar.metviewer.test.util.TestUtil.*;
import static org.junit.Assert.*;

public class MVBatchTest {
    static MVBatch mvBatch;
    static String testDataDir = ROOT_DIR + FILE_SEPARATOR + "plots_batch";
    static ByteArrayOutputStream log;
    static PrintStream printStream;
    static ByteArrayOutputStream logSql;
    static ByteArrayOutputStream logError;
    static PrintWriter printStreamSql;
    static PrintStream printStreamError;
    static DatabaseInfo databaseInfo;
    static AppDatabaseManager databaseManager;

    @BeforeClass
    public static void setUp() throws Exception {

        log = new ByteArrayOutputStream();
        printStream = new PrintStream(log);
        logSql = new ByteArrayOutputStream();
        logError = new ByteArrayOutputStream();
        printStreamSql = new PrintWriter(logSql);
        printStreamError = new PrintStream(logError);
        databaseInfo = new DatabaseInfo();
        //databaseInfo.setDbName(TestUtil.DBNAME);
        //databaseInfo.setDbTYPE(TestUtil.DBTYPE);
        databaseInfo.setHost(TestUtil.host);
        databaseInfo.setUser(TestUtil.USERNAME);
        databaseInfo.setPassword(TestUtil.PWD);
        databaseInfo.setDbName(TestUtil.database);
        AppDatabaseManager databaseManager = new MysqlAppDatabaseManager(databaseInfo, printStreamSql);
        mvBatch = new MVBatch(printStream, printStreamSql, printStreamError, databaseManager);
        MVUtil.updateLog4jConfiguration();
    }

    @Before
    public void beforeTest() {
        try {
            logSql.reset();
            logError.reset();
            log.reset();
        } catch(Exception e) {
            fail("failed to reset logs");
        }
    }

    @After
    public void tearDown() throws Exception {
        logSql.reset();
        logError.reset();
        log.reset();
    }

    @Test
    public void printSql() {
        String msg = "This is a test";
        mvBatch.printSql(msg);
        assertTrue("mvBatch printSql failed", msg.equals(logSql.toString()));
    }

    @Test
    public void getSetDataFolder() {
        String msg = "fred";
        mvBatch.setDataFolder(msg);
        String expectedMsg = msg + File.separator;
        assertTrue("The mvBatch data folder did not get properly set",expectedMsg.equals(mvBatch.getDataFolder()));
        msg = "";
        mvBatch.setDataFolder(msg);
        expectedMsg = File.separator;
        assertTrue("The mvBatch data folder did not get properly reset",mvBatch.getDataFolder().equals(expectedMsg));
    }

    @Test
    public void getSetDatabaseManager() {
        DatabaseInfo myDatabaseInfo = new DatabaseInfo();
        myDatabaseInfo.setHost("fredsmachine");
        myDatabaseInfo.setUser("fred");
        myDatabaseInfo.setPassword("fredspassword");
        //myDatabaseInfo.setDatabase("fredsdatabase");
        //myDatabaseInfo.setDBType("fredsCB");
        try {
            AppDatabaseManager myDatabaseManager = new MysqlAppDatabaseManager(myDatabaseInfo, printStreamSql);
            mvBatch.setDatabaseManager(myDatabaseManager);
            assertEquals("getSetDatabaseManager - the databaseManager was not properly set or retrieved", myDatabaseManager,mvBatch.getDatabaseManager());
            mvBatch.setDatabaseManager(null);
        } catch (Exception sdbm) {
            fail ("failed getSetDatabaseManager with exception: " + sdbm.getMessage());
        }
    }


    @Test
    public void getSetRworkFolder() {
        String msg = "fredsworkfolder";
        mvBatch.setRworkFolder(msg);
        String expectedMsg = msg + File.separator;
        assertTrue("The mvBatch work folder did not get properly set",expectedMsg.equals(mvBatch.getRworkFolder()));
        msg = "";
        mvBatch.setRworkFolder(msg);
        expectedMsg = File.separator;
        assertTrue("The mvBatch work folder did not get properly reset",mvBatch.getRworkFolder().equals(expectedMsg));
    }

    @Test
    public void getSetRtmplFolder() {
        String msg = "fredsRtmplfolder";
        mvBatch.setRtmplFolder(msg);
        String expectedMsg = msg + File.separator;
        assertTrue("The mvBatch templ folder did not get properly set",expectedMsg.equals(mvBatch.getRtmplFolder()));
        msg = "";
        mvBatch.setRtmplFolder(msg);
        expectedMsg = File.separator;
        assertTrue("The mvBatch work folder did not get properly reset",mvBatch.getRtmplFolder().equals(expectedMsg));
    }

    @Test
    public void getSetScriptsFolder() {
        String msg = "fredsRscriptsfolder";
        mvBatch.setScriptsFolder(msg);
        String expectedMsg = msg + File.separator;
        assertTrue("The mvBatch scripts folder did not get properly set",expectedMsg.equals(mvBatch.getScriptsFolder()));
        msg = "";
        mvBatch.setScriptsFolder(msg);
        expectedMsg = File.separator;
        assertTrue("The mvBatch scripts folder did not get properly reset",mvBatch.getScriptsFolder().equals(expectedMsg));
    }

    @Test
    public void getSetDbType() {
        mvBatch.setDbType("fredsDbType");
        assertTrue("The mvBatch DbType did not get properly set","fredsDbType".equals(mvBatch.getDbType()));
        mvBatch.setDbType("");
        assertTrue("The mvBatch DbType folder did not get properly reset",mvBatch.getDbType().equals(""));
    }

    @Test
    public void print() {
        String msg = "This is a test";
        mvBatch.print(msg);
        String expectedMsg = msg + System.getProperty("line.separator");
        assertTrue("mvBatch print failed", expectedMsg.equals(log.toString()));
    }

    @Test
    public void printError() {
        String msg = "This is a test";
        mvBatch.printError(msg);
        assertTrue("mvBatch printError failed", msg.equals(logError.toString()));
    }

    @Test
    public void getSetNumPlots() {
        int numPlots = 5;
        mvBatch.setNumPlots(5);
        assertTrue("mvBatch getSetNumPlots failed", numPlots == mvBatch.getNumPlots());
        mvBatch.setNumPlots(0);
        assertTrue("mvBatch getSetNumPlots failed", 0 == mvBatch.getNumPlots());
    }


    @Test
    public void getSetNumPlotsRun() {
        int numPlots = 5;
        mvBatch.setNumPlotsRun(5);
        assertTrue("mvBatch getSetNumPlotsRun failed", numPlots == mvBatch.getNumPlotsRun());
        mvBatch.setNumPlotsRun(0);
        assertTrue("mvBatch getSetNumPlotsRun failed", 0 == mvBatch.getNumPlotsRun());
    }

    @Test
    public void getSetPlotsFolder() {
        String folder = "myNonSeparatorTerminatedFolderName";
        mvBatch.setPlotsFolder(folder);
        String expectedFolder = folder + File.separator;
        assertTrue("getSetPlotsFolder failed with non separator terminated string", expectedFolder.equals(mvBatch.getPlotsFolder()));
        mvBatch.setPlotsFolder(folder + File.separator);
        assertTrue("getSetPlotsFolder failed with separator terminated string", expectedFolder.equals(mvBatch.getPlotsFolder()));
    }

    @Test
    public void getPrintStream() {
        assertEquals("mvBatch getPrintStream failed", printStream, mvBatch.getPrintStream());
    }

    @Test
    public void getPrintStreamSql() {
        assertEquals("mvBatch getPrintStreamSql failed", printStreamSql, mvBatch.getPrintStreamSql());
    }

    @Test
    public void isSetVerbose() {
        mvBatch.setVerbose(true);
        assertTrue("mvBatch setVerbose to true failed", mvBatch.isVerbose());
        mvBatch.setVerbose(false);
        assertFalse("mvBatch setVerbose to false failed", mvBatch.isVerbose());
    }

    /** plot type tests are really more of an integration level test.
    * These are here to allow us to do debugging in the IDE under the JUNIT test runner.
    * The following tests essentially duplicate tests that are run in batch mode except
    * that these only compare result plot file sizes.
    **/

    @Test
    public void seriesDiffGrouping() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "diff_grouping";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void handSelectedDates() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "hand_selected_dates";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void tylor() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "tylor";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void rhist() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "rhist";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void phist() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "phist";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void servlet() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "servlet";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void rely() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "rely";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void series_sum_stat() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "series_sum_stat";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void plot_afwa_thresh() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-printSql");
        argsList.add("mysql");
        String plotType = "plot_afwa_thresh";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }
}

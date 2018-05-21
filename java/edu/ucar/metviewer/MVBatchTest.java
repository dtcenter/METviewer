package edu.ucar.metviewer;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;
import edu.ucar.metviewer.db.MysqlDatabaseManager;
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
import static org.junit.Assert.*;

import static edu.ucar.metviewer.test.util.TestUtil.*;

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
        AppDatabaseManager databaseManager = new MysqlAppDatabaseManager(databaseInfo, printStreamSql);
        mvBatch = new MVBatch(printStream, printStreamSql, printStreamError, databaseManager);
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
        assertTrue("mvBatch printSql failed", msg.equals(printStreamSql.toString()));
    }

    @Test
    public void getSetDataFolder() {
        mvBatch.setDataFolder("fred");
        assertTrue("The mvBatch data folder did not get properly set","fred".equals(mvBatch.getDataFolder()));
        mvBatch.setDataFolder("");
        assertTrue("The mvBatch data folder did not get properly reset",mvBatch.getDataFolder().equals(""));
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
            assertEquals("getSetDatabaseManager - the the databaseManager was not propery set or retrieved", myDatabaseManager,mvBatch.getDatabaseManager());
            mvBatch.setDatabaseManager(null);
        } catch (Exception sdbm) {
            fail ("failed getSetDatabaseManager with exception: " + sdbm.getMessage());
        }
    }


    @Test
    public void getSetRworkFolder() {
        mvBatch.setRworkFolder("fredsworkfolder");
        assertTrue("The mvBatch work folder did not get properly set","fredsworkfolder".equals(mvBatch.getRworkFolder()));
        mvBatch.setRworkFolder("");
        assertTrue("The mvBatch work folder did not get properly reset",mvBatch.getRworkFolder().equals(""));
    }

    @Test
    public void getSetRtmplFolder() {
        mvBatch.setRtmplFolder("fredsRtmplfolder");
        assertTrue("The mvBatch templ folder did not get properly set","fredsRtmplfolder".equals(mvBatch.getRtmplFolder()));
        mvBatch.setRtmplFolder("");
        assertTrue("The mvBatch work folder did not get properly reset",mvBatch.getRtmplFolder().equals(""));
    }

    @Test
    public void getSetScriptsFolder() {
        mvBatch.setScriptsFolder("fredsRscriptsfolder");
        assertTrue("The mvBatch scripts folder did not get properly set","fredsRscriptsfolder".equals(mvBatch.getScriptsFolder()));
        mvBatch.setScriptsFolder("");
        assertTrue("The mvBatch scripts folder did not get properly reset",mvBatch.getScriptsFolder().equals(""));
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
        assertTrue("mvBatch print failed", msg.equals(printStream.toString()));
    }

    @Test
    public void printError() {
        String msg = "This is a test";
        mvBatch.printError(msg);
        assertTrue("mvBatch printError failed", msg.equals(printStreamError.toString()));
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
        mvBatch.setNumPlots(0);
        assertTrue("mvBatch getSetNumPlotsRun failed", 0 == mvBatch.getNumPlotsRun());
    }

    @Test
    public void getSetPlotsFolder() {
        String folder = "myNonSeparatorTerminatedFolderName";
        mvBatch.setPlotsFolder(folder + File.separator);
        String expectedFolder = mvBatch.getPlotsFolder();
        assertTrue("getSetPlotsFolder failed with separator terminated string", expectedFolder.equals(mvBatch.getPlotsFolder()));
        expectedFolder = mvBatch.getPlotsFolder() + File.separator;
        assertTrue("getSetPlotsFolder failed with non separator terminated string", expectedFolder.equals(mvBatch.getPlotsFolder()));
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
    public void seriesStat() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-list");
        argsList.add("-printSql");
        String plotType = "series_stat";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void seriesModeAttr() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-list");
        argsList.add("-printSql");
        String plotType = "series_mode_attr";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void seriesDiffGrouping() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
        String plotType = "hand_selected_dates";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void seriesModeRatio() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-list");
        argsList.add("-printSql");
        String plotType = "series_mode_ratio";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }

    @Test
    public void tyler() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
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
        argsList.add("-list");
        argsList.add("-printSql");
        String plotType = "plot_afwa_thresh";
        String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
        argsList.add(fpath);
        xlateTestSpec(fpath);
        MVBatch.main(argsList.toArray(new String[argsList.size()]));
        compareBinaryFilesBySize(testDataDir, plotType);
    }
}
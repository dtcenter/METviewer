package edu.ucar.metviewer.test;

import java.io.File;
import java.util.Map;

import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.MVLoadJob;
import edu.ucar.metviewer.MVLoadJobParser;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlLoadDatabaseManager;
import org.apache.logging.log4j.io.IoBuilder;
import org.junit.Test;

import static org.junit.Assert.fail;

public class MysqlLoadDatabaseManagerTest {

    @Test
    public void applyIndexes() {
    }

    @Test
    public void dropIndexes() {
    }

    @Test
    public void loadStatFile() {
    }

    @Test
    public void loadStatFileVSDB() {
        String strXML = "Iamanxmlpath"; //FIX THIS
        MVLoadJobParser parser = null;
        try {
            parser = new MVLoadJobParser(strXML);
            MVLoadJob job = parser.getLoadJob();
            DatabaseInfo databaseInfo = new DatabaseInfo(job.getDBHost(), job.getDBUser(), job.getDBPassword());
            MysqlLoadDatabaseManager mysqlLoadDatabaseManager = new MysqlLoadDatabaseManager(databaseInfo,
                    IoBuilder.forLogger(MysqlLoadDatabaseManager.class).setLevel(org.apache.logging.log4j.Level.INFO)
                            .buildPrintStream());
            File file = new File("a data file path");
            boolean forceDupFile = job.getForceDupFile();
            DataFileInfo info = mysqlLoadDatabaseManager.processDataFile(file, forceDupFile);
            Map<String, Long> timeStats = mysqlLoadDatabaseManager.loadStatFileVSDB(info);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught: " + e.getMessage());
        }

    }

    @Test
    public void loadModeFile() {
    }

    @Test
    public void loadMtdFile() {
    }

    @Test
    public void processDataFile() {
    }

    @Test
    public void updateInfoTable() {
    }

}
package edu.ucar.metviewer.test;

import java.io.File;
import java.util.Map;

import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.MVLoadJob;
import edu.ucar.metviewer.MVLoadJobParser;
import edu.ucar.metviewer.db.LoadDatabaseManager;
import edu.ucar.metviewer.db.DatabaseManager;
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
        LoadDatabaseManager loadDatabaseManager;
        try {
            parser = new MVLoadJobParser(strXML);
            MVLoadJob job = parser.getLoadJob();
            String management_system = parser.getLoadJob().getDBManagementSystem();
            loadDatabaseManager = (LoadDatabaseManager)DatabaseManager.getLoadManager(management_system, job.getDBHost(), job.getDBUser(), job.getDBPassword(), job.getDBName());
            File file = new File("a data file path");
            boolean forceDupFile = job.getForceDupFile();
            DataFileInfo info = loadDatabaseManager.processDataFile(file, forceDupFile, loadDatabaseManager.getDatabaseInfo());
            Map<String, Long> timeStats = loadDatabaseManager.loadStatFileVSDB(info, loadDatabaseManager.getDatabaseInfo());
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
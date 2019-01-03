package edu.ucar.metviewer.test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;

import edu.ucar.metviewer.db.CBDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestCBDatabaseManager extends CBDatabaseManager implements TestDBManager {
    public TestCBDatabaseManager(DatabaseInfo databaseInfo, String password) throws Exception {
        super(databaseInfo, password);
    }

    private static final Logger logger = LogManager.getLogger("TestCBDatabaseManager");

    public int getNumberOfRows(String lineDataType) throws Exception {
        String tableName = lineDataType;
        int rows= -1;
        try {
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return rows;
    }

    public void loadData(String fileName, String database) throws Exception {
        Reader reader = null;
        Connection con = null;
        Statement statement = null;
        try {
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}

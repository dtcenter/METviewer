package edu.ucar.metviewer.test;

import edu.ucar.metviewer.db.CBLoadDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;

public class TestCBLoadDatabaseManager extends CBLoadDatabaseManager implements TestLoadDatabaseManager{
    public TestCBLoadDatabaseManager(DatabaseInfo databaseInfo) throws Exception {
        super(databaseInfo);
    }

    private static final Logger logger = LogManager.getLogger("MysqlLoadDatabaseManager");

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

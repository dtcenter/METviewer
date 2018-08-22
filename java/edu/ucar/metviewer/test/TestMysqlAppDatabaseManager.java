package edu.ucar.metviewer.test;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlAppDatabaseManager;

import java.sql.SQLException;

public class TestMysqlAppDatabaseManager extends MysqlAppDatabaseManager implements TestAppDatabaseManager{
    public TestMysqlAppDatabaseManager(DatabaseInfo databaseInfo) throws SQLException {
        super(databaseInfo);
    }
}

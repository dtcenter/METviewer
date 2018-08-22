package edu.ucar.metviewer.test;

import edu.ucar.metviewer.test.TestAppDatabaseManager;
import edu.ucar.metviewer.db.CBAppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;

import java.sql.SQLException;

public class TestCBAppDatabaseManager extends CBAppDatabaseManager implements TestAppDatabaseManager {
    public TestCBAppDatabaseManager(DatabaseInfo databaseInfo) throws SQLException {
        super(databaseInfo);
    }
}

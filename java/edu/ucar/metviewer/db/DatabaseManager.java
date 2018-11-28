/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.logging.log4j.io.IoBuilder;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public abstract class DatabaseManager {
    private PrintStream pw;
    private DatabaseInfo databaseInfo;
    protected static final String DB_PREFIX_MV = "mv_";
    public static final String[] SQL_INJECTION_WORDS = new String[]{
            "OR ", "--", "SELECT", "UNION", "DROP", "CREATE"
    };

    public DatabaseManager(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
        pw = IoBuilder.forLogger(getClass().getSimpleName())
                .setLevel(org.apache.logging.log4j.Level.INFO).buildPrintStream();
    }

    public DatabaseManager() {
    }

    public PrintStream getPrintStream() {
        return pw;
    }

    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }

    public String getDbName() {
        return databaseInfo.getDbName();
    }

    public static DatabaseManager getLoadManager(String management_system, String host, String user, String password, String dbName) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        databaseInfo.setDbName(dbName);
        DatabaseManager databaseManager = null;
        switch (dbType) {
            case "mysql":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".MysqlLoadDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            case "cb":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".CBLoadDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    public static DatabaseManager getManager(String management_system,
                                             String host,
                                             String user,
                                             String password,
                                             String dbName) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        databaseInfo.setDbName(dbName);
        DatabaseManager databaseManager = null;
        switch (dbType) {
            case "mysql":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".MysqlDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            case "cb":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".CBDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    // ...AppDatabaseManagers don't need a database name. They get a list of database names from the database engine.
    public static DatabaseManager getAppManager(String management_system, String host, String user, String password) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        DatabaseManager databaseManager = null;
        PrintWriter pw = null;
        // NOTE: the funky contstuctor using reflection is to enable us to build
        // a mysql version without a couchbase dependency
        // and a CB version without a mysql dependency.
        // The build.xml will conditionally leave out the unwanted
        // dependencies (jar files) based on db.management.system
        switch (dbType) {
            case "mysql":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".MysqlAppDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            case "cb":
                databaseManager = (DatabaseManager)Class.forName("edu.ucar.metviewer.db" +
                        ".CBAppDatabaseManager").getDeclaredConstructor(edu.ucar.metviewer.db.DatabaseInfo.class).newInstance(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    public abstract void initDBList(boolean updateGroups);

}

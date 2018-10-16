/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import org.apache.logging.log4j.io.IoBuilder;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public abstract class DatabaseManager {
    private static List<String> listDB;
    private PrintWriter pw;
    private DatabaseInfo databaseInfo;
    protected static final String DB_PREFIX_MV = "mv_";
    public static final String[] SQL_INJECTION_WORDS = new String[]{
            "OR ", "--", "SELECT", "UNION", "DROP", "CREATE"
    };

    public DatabaseManager(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
        listDB = new ArrayList<>();
        pw = IoBuilder.forLogger(getClass().getSimpleName()).setLevel(org.apache.logging.log4j.Level.INFO).buildPrintWriter();
    }

    public DatabaseManager() {
    }

    public PrintWriter getPrintWriter(){
        return pw;
    }
    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }
    public String getDbName() {
        return databaseInfo.getDbName();
    }

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    // protected static final SimpleDateFormat DB_DATE_STAT_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    protected static final DateTimeFormatter DB_DATE_STAT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");


    public static DatabaseManager getLoadManager(String management_system, String host, String user, String password, String dbName) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = (ms == null || ms == "") ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        databaseInfo.setDbName(dbName);
        DatabaseManager databaseManager = null;
        switch (dbType) {
            case "mysql":
                databaseManager = new MysqlLoadDatabaseManager(databaseInfo);
                break;
            case "cb":
                databaseManager = new CBLoadDatabaseManager(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    public static DatabaseManager getManager(String management_system, String host, String user, String password, String dbName) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = (ms == null || ms == "") ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        databaseInfo.setDbName(dbName);
        DatabaseManager databaseManager = null;
        switch (dbType) {
            case "mysql":
                databaseManager = new MysqlDatabaseManager(databaseInfo);
                break;
            case "cb":
                databaseManager = new CBDatabaseManager(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    // ...AppDatabaseManagers don't need a database name. They get a list of database names from the database engine.
    public static DatabaseManager getAppManager(String management_system, String host, String user, String password) throws Exception {
        String ms = management_system.toLowerCase();
        String dbType = (ms == null || ms == "")  ? "mysql" : ms; // default dbType to mysql if management_system is missing
        DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
        DatabaseManager databaseManager = null;
        PrintWriter pw = null;
        switch (dbType) {
            case "mysql":
                databaseManager = new MysqlAppDatabaseManager(databaseInfo);
                break;
            case "cb":
                databaseManager = new CBAppDatabaseManager(databaseInfo);
                break;
            default:
                throw new IllegalArgumentException("Invalid database type: " + dbType);
        }
        return databaseManager;
    }

    public abstract void initDBList(boolean updateGroups);

}

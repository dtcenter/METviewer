/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.PrintStream;

import org.apache.logging.log4j.io.IoBuilder;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public abstract class DatabaseManager {

  public static final String[] SQL_INJECTION_WORDS = new String[]{
      "OR ", "--", "SELECT", "UNION", "DROP", "CREATE"
  };
  protected static final String DB_PREFIX_MV = "mv_";
  private PrintStream pw;
  private DatabaseInfo databaseInfo;

  public DatabaseManager(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
    pw = IoBuilder.forLogger(getClass().getSimpleName())
             .setLevel(org.apache.logging.log4j.Level.INFO).buildPrintStream();
  }

  public DatabaseManager() {
  }

  public static DatabaseManager getLoadManager(
      String management_system, String host, String user, String password,
      String dbName) throws Exception {
    String ms = management_system.toLowerCase();
    String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
    databaseInfo.setDbName(dbName);
    DatabaseManager databaseManager = null;
    switch (dbType) {
      case "mysql":
        databaseManager = new MysqlLoadDatabaseManager(databaseInfo);
        break;
      case "mariadb":
        databaseManager = new MariaDbLoadDatabaseManager(databaseInfo);
        break;
      case "cb":
        databaseManager = new CBLoadDatabaseManager(databaseInfo);
        break;
      case "aurora":
        databaseManager = new AuroraLoadDatabaseManager(databaseInfo);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }

  public static DatabaseManager getManager(
      String management_system,
      String host,
      String user,
      String password,
      String dbName) throws Exception {
    String ms = management_system.toLowerCase();
    String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
    databaseInfo.setDbName(dbName);
    DatabaseManager databaseManager;
    switch (dbType) {
      case "mysql":
        databaseManager = new MysqlDatabaseManager(databaseInfo);
        break;
      case "mariadb":
        databaseManager = new MariaDbAppDatabaseManager(databaseInfo);
        break;
      case "cb":
        databaseManager = new CBDatabaseManager(databaseInfo);
        break;
      case "aurora":
        databaseManager = new AuroraAppDatabaseManager(databaseInfo);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }

  // ...AppDatabaseManagers don't need a database name. They get a list of database names from the database engine.
  public static DatabaseManager getAppManager(
      String management_system, String host, String user, String password) throws Exception {
    String ms = management_system.toLowerCase();
    String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user, password);
    DatabaseManager databaseManager = null;
    switch (dbType) {
      case "mysql":
        databaseManager = new MysqlAppDatabaseManager(databaseInfo);
        break;
      case "mariadb":
        databaseManager = new MariaDbAppDatabaseManager(databaseInfo);
        break;
      case "cb":
        databaseManager = new CBAppDatabaseManager(databaseInfo);
        break;
      case "aurora":
        databaseManager = new AuroraAppDatabaseManager(databaseInfo);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
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

  public abstract void initDBList(boolean updateGroups);

}

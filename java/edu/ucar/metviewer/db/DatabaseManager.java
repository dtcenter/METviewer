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

  protected static final String[] SQL_INJECTION_WORDS = new String[]{
      "OR ", "--", "SELECT", "UNION", "DROP", "CREATE"
  };
  private static final String MYSQL = "mysql";
  private static final String MARIADB = "mariadb";
  public static final String CB = "cb";
  private static final String AURORA = "aurora";
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
      String managementSystem, String host, String user, String password,
      String dbName) throws Exception {
    String ms = managementSystem.toLowerCase();

    // default dbType to mysql if management_system is missing
    String dbType = ms.isEmpty() ? MYSQL : ms;
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(dbName);
    DatabaseManager databaseManager = null;
    switch (dbType) {
      case MYSQL:
        databaseManager = new MysqlLoadDatabaseManager(databaseInfo, password);
        break;
      case MARIADB:
        databaseManager = new MariaDbLoadDatabaseManager(databaseInfo, password);
        break;
      case CB:
        databaseManager = new CBLoadDatabaseManager(databaseInfo, password);
        break;
      case AURORA:
        databaseManager = new AuroraLoadDatabaseManager(databaseInfo, password);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }

  public static DatabaseManager getManager(
      String managementSystem,
      String host,
      String user,
      String password,
      String dbName) throws Exception {
    String ms = managementSystem.toLowerCase();

    // default dbType to mysql if management_system is missing
    String dbType = ms.isEmpty() ? MYSQL : ms;
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(dbName);
    DatabaseManager databaseManager;
    switch (dbType) {
      case MYSQL:
        databaseManager = new MysqlDatabaseManager(databaseInfo, password);
        break;
      case MARIADB:
        databaseManager = new MariaDbAppDatabaseManager(databaseInfo, password);
        break;
      case CB:
        databaseManager = new CBDatabaseManager(databaseInfo, password);
        break;
      case AURORA:
        databaseManager = new AuroraAppDatabaseManager(databaseInfo, password);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }


  /**
   * AppDatabaseManagers don't need a database name. They get a list of database names from the
   * database engine.
   *
   * @param managementSystem - database type
   * @param host - host name
   * @param user - user name
   * @param password - db user password
   * @return - database manager for the requared database type
   * @throws Exception
   */
  public static DatabaseManager getAppManager(
      String managementSystem, String host, String user, String password) throws Exception {
    String ms = managementSystem.toLowerCase();

    // default dbType to mysql if management_system is missing
    String dbType = ms.isEmpty() ? MYSQL : ms;
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user/*, password*/);
    DatabaseManager databaseManager = null;
    switch (dbType) {
      case MYSQL:
        databaseManager = new MysqlAppDatabaseManager(databaseInfo, password);
        break;
      case MARIADB:
        databaseManager = new MariaDbAppDatabaseManager(databaseInfo, password);
        break;
      case CB:
        databaseManager = new CBAppDatabaseManager(databaseInfo, password);
        break;
      case AURORA:
        databaseManager = new AuroraAppDatabaseManager(databaseInfo, password);
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

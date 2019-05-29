/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.apache.logging.log4j.io.IoBuilder;

import static org.apache.logging.log4j.Level.INFO;

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

  public static final String MYSQL = "mysql";
  public static final String MARIADB = "mariadb";
  public static final String CB = "cb";
  public static final String AURORA = "aurora";

  public DatabaseManager(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
    pw = IoBuilder.forLogger(getClass().getSimpleName()).setLevel(INFO).buildPrintStream();
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

  public static DatabaseManager getLoadManager(
          String managementSystem, String host, String user, String password,
          String dbName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    String ms = managementSystem.toLowerCase(Locale.ENGLISH);
    String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(dbName);
    DatabaseManager databaseManager;
    switch (dbType) {
      // It is necessary to use Class.forName for the specialized
      // DatabaseManager construction so as to not create a
      // dependency on the specialized database jar files.
      case MYSQL:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.mysql.MysqlLoadDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case CB:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.couchbase.CBLoadDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case MARIADB:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.mariadb.MariaDbLoadDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case AURORA:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.aurora.AuroraLoadDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }


  // ...AppDatabaseManagers don't need a database name. They get a list of database names from the database engine.
  public static DatabaseManager getAppManager(
          String managementSystem, String host, String user, String password, String database) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    String ms = managementSystem.toLowerCase(Locale.ENGLISH);
    String dbType = ms.isEmpty() ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(database);
    DatabaseManager databaseManager;
    // NOTE: the contstuctor using reflection is to enable us to build
    // a mysql version without a couchbase dependency
    // and a CB version without a mysql dependency.
    // The build.xml will conditionally leave out the unwanted
    // dependencies (jar files) based on db.management.system
    switch (dbType) {
      case MYSQL:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.mysql.MysqlAppDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case CB:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.couchbase.CBAppDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case MARIADB:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.mariadb.MariaDbAppDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      case AURORA:
        databaseManager = (DatabaseManager) Class.forName(
                "edu.ucar.metviewer.db.aurora.AuroraAppDatabaseManager")
                .getDeclaredConstructor(DatabaseInfo.class,
                        String.class)
                .newInstance(databaseInfo, password);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }

  public abstract void initDBList(boolean updateGroups);

}

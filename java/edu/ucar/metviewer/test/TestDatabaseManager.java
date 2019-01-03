/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.db.AuroraAppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MariaDbAppDatabaseManager;
import edu.ucar.metviewer.db.MysqlDatabaseManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public class TestDatabaseManager {

  public static TestDBManager getManager(
      String management_system, String host, String user, String password,
      String dbName) throws Exception {
    String ms = management_system.toLowerCase();
    String dbType = (ms == null || ms == "") ? "mysql" : ms; // default dbType to mysql if management_system is missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(dbName);
    TestDBManager databaseManager = null;
    MysqlDatabaseManager mysqlDatabaseManager;


    switch (dbType) {
      case "mysql":
        mysqlDatabaseManager = new MysqlDatabaseManager(databaseInfo, password);
        databaseManager = new TestMysqlDatabaseManager(mysqlDatabaseManager);
        break;
      case "mariadb":
        mysqlDatabaseManager = new MariaDbAppDatabaseManager(databaseInfo, password);
        databaseManager = new TestMysqlDatabaseManager(mysqlDatabaseManager);
        break;
      case "aurora":
        mysqlDatabaseManager = new AuroraAppDatabaseManager(databaseInfo, password);
        databaseManager = new TestMysqlDatabaseManager(mysqlDatabaseManager);
        break;
      case "cb":
        databaseManager = new TestCBDatabaseManager(databaseInfo, password);
        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }
}

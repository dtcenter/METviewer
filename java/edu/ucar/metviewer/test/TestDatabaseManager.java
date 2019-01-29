/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.DatabaseManager;


/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public class TestDatabaseManager {

  public static TestDBManager getManager(
      String management_system, String host, String user, String password,
      String dbName) throws Exception {
    String ms = management_system.toLowerCase();
    String dbType = (ms.isEmpty()) ? "mysql" : ms; // default dbType to mysql if management_system is
    // missing
    DatabaseInfo databaseInfo = new DatabaseInfo(host, user);
    databaseInfo.setDbName(dbName);
    TestDBManager databaseManager;


    switch (dbType) {
      case DatabaseManager.MYSQL:
        databaseManager =
            (TestDBManager) Class.forName(
                "edu.ucar.metviewer.db.TestMysqlDatabaseManager")
                                .getDeclaredConstructor(DatabaseInfo.class, String.class)
                                .newInstance(databaseInfo, password);

        break;
      case DatabaseManager.MARIADB:
        databaseManager =
            (TestDBManager) Class.forName(
                "edu.ucar.metviewer.db.TestMariaDbDatabaseManager")
                                .getDeclaredConstructor(DatabaseInfo.class, String.class)
                                .newInstance(databaseInfo, password);

        break;
      case DatabaseManager.AURORA:
        databaseManager =
            (TestDBManager) Class.forName(
                "edu.ucar.metviewer.db.TestAuroraDatabaseManager")
                                .getDeclaredConstructor(DatabaseInfo.class,
                                                        String.class)
                                .newInstance(databaseInfo, password);

        break;
      case DatabaseManager.CB:
        databaseManager =
            (TestDBManager) Class.forName(
                "edu.ucar.metviewer.db.TestCBDatabaseManager")
                                .getDeclaredConstructor(DatabaseInfo.class,
                                                        String.class)
                                .newInstance(databaseInfo, password);

        break;
      default:
        throw new IllegalArgumentException("Invalid database type: " + dbType);
    }
    return databaseManager;
  }
}

/**
 * MariaDbAppDatabaseManager.java Copyright UCAR (c) 2018. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer.db.mariadb;

import java.sql.SQLException;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.mysql.MysqlAppDatabaseManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 2018-12-18 08:59 $
 */
public class MariaDbAppDatabaseManager extends MysqlAppDatabaseManager {

  public MariaDbAppDatabaseManager(DatabaseInfo databaseInfo, String password) throws SQLException {
    super(databaseInfo, password);
  }


  @Override
  protected String getJdbcUrl(final String hostName, final String dbName) {
    String jdbcUrl = "jdbc:mariadb://" + hostName;
    if (dbName != null) {
      jdbcUrl = jdbcUrl + "/" + dbName;
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";
    return jdbcUrl;
  }
}

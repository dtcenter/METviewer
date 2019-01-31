/**
 * AuroraAppDatabaseManager.java Copyright UCAR (c) 2018. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer.db.aurora;

import java.sql.SQLException;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.mysql.MysqlAppDatabaseManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 2018-12-19 15:06 $
 */
public class AuroraAppDatabaseManager extends MysqlAppDatabaseManager {

  public AuroraAppDatabaseManager(DatabaseInfo databaseInfo, String password) throws SQLException {
    super(databaseInfo, password);
  }

  @Override
  protected String getJdbcUrl(final String hostName, final String dbName) {
    String jdbcUrl = "jdbc:mysql:aurora://" + hostName;
    if (dbName != null) {
      jdbcUrl = jdbcUrl + "/" + dbName;
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";
    return jdbcUrl;
  }
}

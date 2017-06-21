/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;


import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class MysqlDatabaseManager {

  private static final Logger logger = LogManager.getLogger("MysqlDatabaseManager");
  private static BoneCP connectionPool;
  protected static final String DB_PREFIX_MV = "mv_";
  protected DatabaseInfo databaseInfo;
  protected static List<String> listDB=new ArrayList<>();


  public MysqlDatabaseManager(DatabaseInfo databaseInfo) throws SQLException {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
    }
    // setup the connection pool
    BoneCPConfig config = new BoneCPConfig();
    String jdbcUrl = "jdbc:" + "mysql" + "://" + databaseInfo.getHost();
    if (databaseInfo.getDbName() != null) {
      jdbcUrl = jdbcUrl + "/" + databaseInfo.getDbName();
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(databaseInfo.getUser());
    config.setPassword(databaseInfo.getPassword());
    config.setMinConnectionsPerPartition(10);
    config.setMaxConnectionsPerPartition(50);
    config.setPartitionCount(1);
    config.setIdleConnectionTestPeriodInSeconds(1);
    config.setIdleMaxAgeInSeconds(240);
    config.setStatementsCacheSize(100);
    config.setReleaseHelperThreads(3);
    connectionPool = new BoneCP(config); // setup the connection pool
    this.databaseInfo = databaseInfo;
    initDBList();
  }


  public void initDBList() {
    listDB.clear();
    try (Connection testConnection = connectionPool.getConnection();
         Statement testStatement = testConnection.createStatement();
         ResultSet resultSet = testStatement.executeQuery("show databases")

    ) {

      String database;


      while (resultSet.next()) {
        database = resultSet.getString("Database");
        if (database.startsWith(DB_PREFIX_MV)) {
          listDB.add(database);
        }
      }

      Collections.sort(listDB);
      resultSet.close();
      testStatement.close();
      testConnection.close();

    } catch (SQLException e) {
      logger.error(e.getMessage());

    }
  }


  /**
   * checks if a database with specified name exists
   *
   * @param db - name of the database to check
   * @return - is database valid
   */
  public boolean validate(String db) {
    boolean result = false;
    for (String availableDB : listDB) {
      if (availableDB.equals(db)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Returns a connection to the database with the specified name
   *
   * @param db - a name of database to get a connection for
   * @return - db connection
   * @throws SQLException
   */
  public Connection getConnection(String db) throws SQLException {
    boolean validDB = validate(db);
    Connection con = null;
    Statement statement = null;
    ResultSet rs = null;
    if (validDB) {
      try {
        con = connectionPool.getConnection();
        statement = con.createStatement();
        rs = statement.executeQuery("use " + db);

      } catch (SQLException e) {
        logger.error(e.getMessage());
      } finally {
        if (statement != null) {
          statement.close();
        }
        if (rs != null) {
          rs.close();
        }

      }
    }
    return con;
  }

  /**
   * Returns a connection to MySQL
   *
   * @return - connection
   */
  public Connection getConnection() {
    Connection con = null;
    try {
      con = connectionPool.getConnection();
    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
    return con;
  }

}

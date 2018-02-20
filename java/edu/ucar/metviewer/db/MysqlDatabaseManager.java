/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class MysqlDatabaseManager {

  private static final Logger logger = LogManager.getLogger("MysqlDatabaseManager");
  //private static BoneCP connectionPool;
  protected static final String DB_PREFIX_MV = "mv_";
  protected DatabaseInfo databaseInfo;
  protected static List<String> listDB = new ArrayList<>();
  private DataSource dataSource;


  protected static final SimpleDateFormat DATE_FORMAT
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


  public MysqlDatabaseManager(DatabaseInfo databaseInfo, PrintWriter printStreamSql) throws SQLException {


    String jdbcUrl = "jdbc:" + "mysql" + "://" + databaseInfo.getHost();
    if (databaseInfo.getDbName() != null) {
      jdbcUrl = jdbcUrl + "/" + databaseInfo.getDbName();
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";

    this.databaseInfo = databaseInfo;
    PoolConfiguration configurationToUse = new PoolProperties();
    configurationToUse.setUrl(jdbcUrl);
    configurationToUse.setUsername(databaseInfo.getUser());
    configurationToUse.setPassword(databaseInfo.getPassword());
    configurationToUse.setDriverClassName("com.mysql.jdbc.Driver");
    configurationToUse.setInitialSize(10);
    configurationToUse.setMaxActive(50);
    configurationToUse.setMaxIdle(15);
    configurationToUse.setMaxWait(10000);
    configurationToUse.setValidationQuery("select 1");
    configurationToUse.setTestOnBorrow(Boolean.TRUE);
    configurationToUse.setTestOnReturn(Boolean.FALSE);
    configurationToUse.setTestWhileIdle(Boolean.FALSE);
    configurationToUse.setMinEvictableIdleTimeMillis(1800000);
    configurationToUse.setTimeBetweenEvictionRunsMillis(1200000);
    configurationToUse.setRemoveAbandoned(Boolean.TRUE);
    configurationToUse.setRemoveAbandonedTimeout(60);
    configurationToUse.setValidationInterval(30000);
    configurationToUse.setRemoveAbandonedTimeout(60);
    configurationToUse.setMinIdle(10);
    configurationToUse.setRemoveAbandoned(true);
    configurationToUse.setJdbcInterceptors(
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    try {
      dataSource = new DataSource();
      dataSource.setPoolProperties(configurationToUse);
      dataSource.setLogWriter(printStreamSql);
    } catch (Exception e) {
      logger.debug(e);
      logger.error("Database connection  for a primary database was not initialised.");
      logger.error(e.getMessage());
      dataSource = null;
    }

    initDBList();
  }


  public void initDBList() {
    listDB.clear();
    String sql = "SELECT DISTINCT ( TABLE_SCHEMA ) FROM information_schema.TABLES where "
                     + "table_name in ('mode_header', 'stat_header', 'mtd_header') and TABLE_ROWS "
                     + "> 0 and "
                     + "TABLE_SCHEMA like 'mv_%'";
    try (Connection testConnection = dataSource.getConnection();
         Statement testStatement = testConnection.createStatement();
         ResultSet resultSet = testStatement.executeQuery(sql)

    ) {

      String database;


      while (resultSet.next()) {
        database = resultSet.getString("TABLE_SCHEMA");
        listDB.add(database);
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
        con = dataSource.getConnection();
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
      con = dataSource.getConnection();
    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
    return con;
  }

}

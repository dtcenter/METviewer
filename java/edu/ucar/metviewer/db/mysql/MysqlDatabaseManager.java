/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.mysql;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class MysqlDatabaseManager extends DatabaseManager {

  private static final Logger logger = LogManager.getLogger("MysqlDatabaseManager");
  protected static Map<String, String> listDB = new TreeMap<>();
  protected static Map<String, List<String>> groupToDatabases = new HashMap<>();
  private static String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

  public static final SimpleDateFormat DATE_FORMAT =
          new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_STRING);


  private DataSource dataSource;
  public static final String BINARY ="  BINARY ";



  public MysqlDatabaseManager(DatabaseInfo databaseInfo, String password) {
    super(databaseInfo);
    String jdbcUrl = getJdbcUrl(databaseInfo.getHost(), databaseInfo.getDbName());
    PoolConfiguration configurationToUse = new PoolProperties();
    configurationToUse.setUrl(jdbcUrl);
    configurationToUse.setUsername(databaseInfo.getUser());
    configurationToUse.setPassword(password);
    configurationToUse.setDriverClassName("org.mariadb.jdbc.Driver");
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
        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    try {
      dataSource = new DataSource();
      dataSource.setPoolProperties(configurationToUse);
      dataSource.setLogWriter(new PrintWriter(getPrintStream()));
    } catch (Exception e) {
      logger.debug(e);
      logger.error("Database connection  for a primary database was not initialised.");
      logger.error(e.getMessage());
      dataSource = null;
    }

    boolean updateGroups = false;
    if (databaseInfo.getDbName() == null) {
      updateGroups = true;
    }
    initDBList(updateGroups);
  }
  public synchronized String formatDate(Date date){
   return DATE_FORMAT.format(date.getTime());
  }

  protected String getJdbcUrl(final String hostName, final String dbName) {
    String jdbcUrl = "jdbc:mysql://" + hostName;
    if (dbName != null) {
      jdbcUrl = jdbcUrl + "/" + dbName;
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";
    return jdbcUrl;
  }

  public void initDBList(boolean updateGroups) {
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
        listDB.put(database, "");
      }
    } catch (SQLException e) {
      logger.error(e.getMessage());

    }

    if (updateGroups) {

      //init groups
      groupToDatabases.clear();

      //for each database find a group
      for (Map.Entry<String, String> database : listDB.entrySet()) {
        String[] metadata = getDatabaseMetadata(database.getKey());
        database.setValue(metadata[1]);

        if (!groupToDatabases.containsKey(metadata[0])) {
          groupToDatabases.put(metadata[0], new ArrayList<>());
        }

        groupToDatabases.get(metadata[0]).add(database.getKey());
      }

    }
  }

  private String[] getDatabaseMetadata(String database) {
    String group = "";
    String description = "";
    String sql = "SELECT * from metadata";
    try (Connection con = getConnection(database);
         Statement statement = con.createStatement();
         ResultSet rs = statement.executeQuery(sql)
    ) {
      while (rs.next()) {
        group = rs.getString("category");
        description = rs.getString("description");
      }

    } catch (SQLException e) {
      logger.error("Can't get groups for database " + database + " SQL exception: " + e);
    }
    if (group.isEmpty()) {
      group = MVUtil.DEFAULT_DATABASE_GROUP;
    }

    return new String[]{group, description};
  }


  /**
   * checks if a database with specified name exists
   *
   * @param db - name of the database to check
   * @return - is database valid
   */
  public boolean validate(String db) {
    boolean result = false;
    for (String availableDB : listDB.keySet()) {
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
    if (validDB) {
      try {
        con = dataSource.getConnection();
        con.setCatalog(db);

      } catch (Exception e) {
        logger.error("can't get connection for database " + db + " " +e.getMessage());
        if(con != null) {
          con.close();
        }
      }
    }else{
      logger.error("Database " + db + " is invalid");
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
      logger.error("can't get connection " +e.getMessage());
    }
    return con;
  }
}

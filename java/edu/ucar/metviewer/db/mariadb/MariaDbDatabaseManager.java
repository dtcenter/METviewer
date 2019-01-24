/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.mariadb;


/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class MariaDbDatabaseManager extends edu.ucar.metviewer.db.DatabaseManager {

  private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("MysqlDatabaseManager");
  protected static java.util.Map<String, String> listDB = new java.util.TreeMap<>();
  protected static java.util.Map<String, java.util.List<String>> groupToDatabases = new java.util.HashMap<>();
  protected static final java.text.SimpleDateFormat DATE_FORMAT =
          new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
  protected static final java.time.format.DateTimeFormatter DATE_FORMAT_1
          = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private org.apache.tomcat.jdbc.pool.DataSource dataSource;
  protected static final String BINARY ="  BINARY ";




  public MariaDbDatabaseManager(edu.ucar.metviewer.db.DatabaseInfo databaseInfo) throws java.sql.SQLException {
      super(databaseInfo);
    String jdbcUrl = "jdbc:" + "mysql" + "://" + databaseInfo.getHost();
    if (databaseInfo.getDbName() != null) {
      jdbcUrl = jdbcUrl + "/" + databaseInfo.getDbName();
    }
    jdbcUrl = jdbcUrl + "?rewriteBatchedStatements=true";
    org.apache.tomcat.jdbc.pool.PoolConfiguration configurationToUse = new org.apache.tomcat.jdbc.pool.PoolProperties();
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
        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    try {
      dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
      dataSource.setPoolProperties(configurationToUse);
      dataSource.setLogWriter(new java.io.PrintWriter(getPrintStream()));
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

  public void initDBList(boolean updateGroups) {
    listDB.clear();
    String sql = "SELECT DISTINCT ( TABLE_SCHEMA ) FROM information_schema.TABLES where "
                     + "table_name in ('mode_header', 'stat_header', 'mtd_header') and TABLE_ROWS "
                     + "> 0 and "
                     + "TABLE_SCHEMA like 'mv_%'";
    try (java.sql.Connection testConnection = dataSource.getConnection();
         java.sql.Statement testStatement = testConnection.createStatement();
         java.sql.ResultSet resultSet = testStatement.executeQuery(sql)

    ) {
      String database;
      while (resultSet.next()) {
        database = resultSet.getString("TABLE_SCHEMA");
        listDB.put(database, "");
      }
    } catch (java.sql.SQLException e) {
      logger.error(e.getMessage());

    }

    if (updateGroups) {

      //init groups
      groupToDatabases.clear();

      //for each database find a group
      for (java.util.Map.Entry<String, String> database : listDB.entrySet()) {
        String[] metadata = getDatabaseMetadata(database.getKey());
        database.setValue(metadata[1]);

        if (!groupToDatabases.containsKey(metadata[0])) {
          groupToDatabases.put(metadata[0], new java.util.ArrayList<>());
        }

        groupToDatabases.get(metadata[0]).add(database.getKey());
      }

    }
  }

  private String[] getDatabaseMetadata(String database) {
    String group = "";
    String description = "";
    String sql = "SELECT * from metadata";
    try (java.sql.Connection con = getConnection(database);
         java.sql.Statement statement = con.createStatement();
         java.sql.ResultSet rs = statement.executeQuery(sql)
    ) {
      while (rs.next()) {
        group = rs.getString("category");
        description = rs.getString("description");
      }

    } catch (java.sql.SQLException e) {
      logger.error("Can't get groups for database " + database + " SQL exception: " + e);
    }
    if (group.isEmpty()) {
      group = edu.ucar.metviewer.MVUtil.DEFAULT_DATABASE_GROUP;
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
   * @throws java.sql.SQLException
   */
  public java.sql.Connection getConnection(String db) throws java.sql.SQLException {
    boolean validDB = validate(db);
    java.sql.Connection con = null;
    java.sql.Statement statement = null;
    java.sql.ResultSet rs = null;
    if (validDB) {
      try {
        con = dataSource.getConnection();
        statement = con.createStatement();
        rs = statement.executeQuery("use " + db);

      } catch (java.sql.SQLException e) {
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
  public java.sql.Connection getConnection() {
    java.sql.Connection con = null;
    try {
      con = dataSource.getConnection();
    } catch (java.sql.SQLException e) {
      logger.error(e.getMessage());
    }
    return con;
  }
}

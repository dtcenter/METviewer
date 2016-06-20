/**
 * Datasource.java Copyright UCAR (c) 2013. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2013.
 */

package edu.ucar.metviewer;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * This singleton creates and manages the connection pool
 *
 * @author tatiana: tatiana $
 * @version : 1.0 : 20/May/13 09:17 $
 */
public class Datasource {

  private static final String DB_PREFIX_MV = "mv_";
  private static final Logger logger = Logger.getLogger("edu.ucar.metviewer.Datasource");
  private static Datasource datasource = null;
  private static BoneCP connectionPool;
  private static List<String> listDB;
  private static String dbManagementSystem = "mysql";
  private static String dbHost = "localhost";
  private static String dbUser = "mvuser";
  private static String dbPassword = "mvuser";

  /**
   * Creates the connection pool and the list of all available databases using database credentials from the properties file
   *
   * @throws SQLException
   */
  private Datasource() throws SQLException {
    // load datasource properties
    ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
    dbHost = bundle.getString("db.host");
    dbUser = bundle.getString("db.user");
    dbPassword = bundle.getString("db.password");
    dbManagementSystem = bundle.getString("db.managementSystem");
    String strDBDriver = bundle.getString("db.driver");
    if (strDBDriver.length() == 0) {
      strDBDriver = "com.mysql.jdbc.Driver";
    }
    if (!strDBDriver.contains(dbManagementSystem)) {
      throw new SQLException("Database type doesn't match to database driver. Can't initialise the pool.");
    }

    try {
      Class.forName(strDBDriver);
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
    }
    // setup the connection pool
    BoneCPConfig config = new BoneCPConfig();
    if (dbManagementSystem.equals("mysql")) {
      config.setJdbcUrl("jdbc:" + dbManagementSystem + "://" + dbHost + "?rewriteBatchedStatements=true"); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/
      config.setUsername(dbUser);
      config.setPassword(dbPassword);
      config.setMinConnectionsPerPartition(2);
      config.setMaxConnectionsPerPartition(3);
      config.setPartitionCount(1);
      config.setIdleConnectionTestPeriodInSeconds(1);
      config.setIdleMaxAgeInSeconds(240);
      config.setStatementsCacheSize(100);
      config.setReleaseHelperThreads(3);
      connectionPool = new BoneCP(config); // setup the connection pool
    } else if (dbManagementSystem.equals("postgresql")) {

    }
    listDB = new ArrayList<>();
    initDBList();

  }

  /**
   * Creates the connection pool and the list of all available databases using database credentials from the parameters list
   *
   * @param strDBHost     - DB host
   * @param strDBUser     - DB user name
   * @param strDBPassword - DB password
   * @throws SQLException
   */
  private Datasource(String strDBDriver, String strDBHost, String strDBUser, String strDBPassword) throws SQLException {
    // load datasource properties
    dbHost = strDBHost;
    dbUser = strDBUser;
    dbPassword = strDBPassword;

    if (strDBDriver == null) {
      strDBDriver = "com.mysql.jdbc.Driver";
    }
    if (!strDBDriver.contains(dbManagementSystem)) {
      throw new SQLException("Database type doesn't match to database driver. Can't initialise the pool.");
    }

    try {
      Class.forName(strDBDriver);
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
    }
    // setup the connection pool
    BoneCPConfig config = new BoneCPConfig();
    if (dbManagementSystem.equals("mysql")) {
      config.setJdbcUrl("jdbc:" + dbManagementSystem + "://" + strDBHost + "?rewriteBatchedStatements=true"); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
      config.setUsername(strDBUser);
      config.setPassword(strDBPassword);
      config.setMinConnectionsPerPartition(10);
      config.setMaxConnectionsPerPartition(50);
      config.setPartitionCount(1);
      config.setIdleConnectionTestPeriodInSeconds(1);
      config.setIdleMaxAgeInSeconds(240);
      config.setStatementsCacheSize(100);
      config.setReleaseHelperThreads(3);
      connectionPool = new BoneCP(config); // setup the connection pool

    } else if (dbManagementSystem.equals("postgresql")) {
      //config.setJdbcUrl("jdbc:" + dbManagementSystem + "://" + strDBHost+ "/template1" ); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
    }


    listDB = new ArrayList<>();
    initDBList();

  }

  public static Datasource getInstance() {
    if (datasource == null) {
      try {
        datasource = new Datasource();
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
    return datasource;

  }

  public static Datasource getInstance(String strDBManagementSystem, String strDBDriver, String strDBHost, String strDBUser, String strDBPassword) {
    if (datasource == null) {
      try {
        if (strDBManagementSystem != null) {
          dbManagementSystem = strDBManagementSystem;
        }
        datasource = new Datasource(strDBDriver, strDBHost, strDBUser, strDBPassword);
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
    return datasource;

  }

  /**
   * creates a list of all available database names that starts form the valid prefix
   *
   * @throws SQLException
   */

  public void initDBList() throws SQLException {
    Connection testConnection = null;
    Statement testStatement = null;
    listDB.clear();
    ResultSet resultSet = null;
    try {

      String database;
      if (dbManagementSystem.equals("mysql")) {
        testConnection = connectionPool.getConnection();
        testStatement = testConnection.createStatement();
        resultSet = testStatement.executeQuery("show databases");

        while (resultSet.next()) {
          database = resultSet.getString("Database");
          if (database.startsWith(DB_PREFIX_MV)) {
            listDB.add(database);
          }
        }
      } else if (dbManagementSystem.equals("postgresql")) {
        String url = "jdbc:" + dbManagementSystem + "://" + dbHost + "/" + "template1";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        testConnection = DriverManager.getConnection(url, props);
        testStatement = testConnection.createStatement();
        resultSet = testStatement.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");
        while (resultSet.next()) {
          database = resultSet.getString("datname");
          if (database.startsWith(DB_PREFIX_MV)) {
            listDB.add(database);
          }
        }
      }
      Collections.sort(listDB);

    } catch (SQLException e) {
      logger.error(e.getMessage());

    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (testStatement != null) {
        testStatement.close();
      }
      if (testConnection != null) {
        testConnection.close();
      }

    }
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
    Statement statement = null/**/;
    if (validDB) {
      try {
        if (dbManagementSystem.equals("mysql")) {
          con = connectionPool.getConnection();
          statement = con.createStatement();
          //con.setReadOnly(true);
          statement.executeQuery("use " + db);
        } else if (dbManagementSystem.equals("postgresql")) {
          String url = "jdbc:" + dbManagementSystem + "://" + dbHost + "/" + db;
          Properties props = new Properties();
          props.setProperty("user", dbUser);
          props.setProperty("password", dbPassword);
          con = DriverManager.getConnection(url, props);
        }

      } catch (SQLException e) {
        logger.error(e.getMessage());
      } finally {
        if (statement != null) {
          statement.close();
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

  /**
   * Returns a connection to the database with the specified name and credentials
   *
   * @param strDBHost     - DB host
   * @param strDBName     - DB name
   * @param strDBUser     - DB user name
   * @param strDBPassword - DB password
   * @return - db connection
   */
  public Connection getConnection(String strDbManagementSystem, String strDBDriver, String strDBHost, String strDBName, String strDBUser, String strDBPassword) {
    Connection con = null;

    if (datasource == null) {
      try {
        if (strDbManagementSystem != null) {
          dbManagementSystem = strDbManagementSystem;
        }
        datasource = new Datasource(strDBDriver, strDBHost, strDBUser, strDBPassword);
      } catch (SQLException e) {
        logger.error(e.getMessage());
      }
    }
    try {
      con = getConnection(strDBName);
    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
    return con;
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
   * Returns a list of all available database names
   *
   * @return list of database names
   */
  public List<String> getAllDatabases() {
    return Collections.unmodifiableList(listDB);
  }
}

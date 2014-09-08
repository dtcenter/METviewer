/**
 * Datasource.java
 * Copyright UCAR (c) 2013.
 * University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2013.
 */

package edu.ucar.metviewer;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This singleton creates and manages the connection pool
 * @author tatiana: tatiana $
 * @version : 1.0 : 20/May/13 09:17 $
 */
public class Datasource {

  private static final String DB_PREFIX_MV = "mv_";
  private static Datasource datasource = null;
  private static final Logger logger = Logger.getLogger("edu.ucar.metviewer.Datasource");
  private static BoneCP connectionPool;
  private static List<String> listDB;

  /**
   * Creates the connection pool and the list of all available databases using database credentials from the properties file
   * @throws IOException
   * @throws SQLException
   */
  private Datasource() throws IOException, SQLException {
    // load datasource properties
    ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
    String strDBHost = bundle.getString("db.host");
    String strDBUser = bundle.getString("db.user");
    String strDBPassword = bundle.getString("db.password");

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
    }
    // setup the connection pool
    BoneCPConfig config = new BoneCPConfig();
    config.setJdbcUrl("jdbc:mysql://" + strDBHost); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
    config.setUsername(strDBUser);
    config.setPassword(strDBPassword);
    config.setMinConnectionsPerPartition(2);
    config.setMaxConnectionsPerPartition(3);
    config.setPartitionCount(1);
    config.setIdleConnectionTestPeriodInSeconds(1);
    config.setIdleMaxAgeInSeconds(240);
    config.setStatementsCacheSize(100);
    config.setReleaseHelperThreads(3);
    connectionPool = new BoneCP(config); // setup the connection pool


    listDB = new ArrayList<String>();
    initDBList();

  }

  /**
   * Creates the connection pool and the list of all available databases using database credentials from the parameters list
   * @param strDBHost - DB host
   * @param strDBUser - DB user name
   * @param strDBPassword - DB password
   * @throws IOException
   * @throws SQLException
   */
  private Datasource(String strDBHost, String strDBUser, String strDBPassword) throws IOException, SQLException {
    // load datasource properties

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
    }
    // setup the connection pool
    BoneCPConfig config = new BoneCPConfig();
    config.setJdbcUrl("jdbc:mysql://" + strDBHost); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
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


    listDB = new ArrayList<String>();
    initDBList();

  }

  /**
   * creates a list of all available database names that starts form the valid prefix
   * @throws SQLException
   */

  public void initDBList() throws SQLException {
    Connection testConnection = null;
    Statement testStatement = null;
    listDB.clear();
    try {
      testConnection = connectionPool.getConnection();
      testStatement = testConnection.createStatement();
      //testStatement.executeQuery("select 1");
      ResultSet resultSet = testStatement.executeQuery("show databases");
      String database;
      while (resultSet.next()) {
        database = resultSet.getString("Database");
        if (database.startsWith(DB_PREFIX_MV)) {
          listDB.add(database);
        }
      }
      Collections.sort(listDB);

    } catch (SQLException e) {
      logger.error(e.getMessage());

    } finally {
      if (testStatement != null) {
        testStatement.close();
      }
      if (testConnection != null) {
        testConnection.close();
      }

    }
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

  public static Datasource getInstance(String strDBHost,  String strDBUser, String strDBPassword) {
      if (datasource == null) {
        try {
          datasource = new Datasource(strDBHost, strDBUser, strDBPassword);
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }
      return datasource;

    }

  /**
   * Returns a connection to the database with the specified name
   * @param db - a name of database to get a connection for
   * @return - db connection
   * @throws SQLException
   */
  public  Connection getConnection(String db) throws SQLException {
    boolean validDB = validate(db);
    Connection con = null;
    Statement statement = null/**/;
    if (validDB) {
      try {
        con = connectionPool.getConnection();
        //con.setReadOnly(true);
        statement = con.createStatement();
        statement.executeQuery("use " + db);

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
   * @return - connection
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
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
   * @param strDBHost - DB host
   * @param strDBName - DB name
    * @param strDBUser - DB user name
    * @param strDBPassword - DB password
   * @return - db connection
   */
  public  Connection getConnection(String strDBHost, String strDBName, String strDBUser, String strDBPassword) {
    Connection con = null;

    if (datasource == null) {
      try {
        datasource = new Datasource(strDBHost, strDBUser, strDBPassword);
      } catch (IOException e) {
        logger.error(e.getMessage());
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
   * @param db - name of the database to check
   * @return
   */
  public  boolean validate(String db) {
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
   * @return list of database names
   */
  public List<String> getAllDatabases() {
    return Collections.unmodifiableList(listDB);
  }
}

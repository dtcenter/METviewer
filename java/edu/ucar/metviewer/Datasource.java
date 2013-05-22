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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

/**
 * @author tatiana: tatiana $
 * @version : 1.0 : 20/May/13 09:17 $
 */
public class Datasource {

  private static Datasource datasource = null;
  private static final Logger _logger = Logger.getLogger("edu.ucar.metviewer.Datasource");
  private static String[] listDB;
  private BoneCP connectionPool;

  private Datasource() throws IOException, SQLException {
    // load datasource properties
    ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
    String strDBHost = bundle.getString("db.host");
    String strDBUser = bundle.getString("db.user");
    String strDBPassword = bundle.getString("db.password");
    listDB = bundle.getString("db.list").split("\\s*,\\s*");

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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


    Connection testConnection = null;
    Statement testStatement = null;

    // test connectivity and initialize pool
    try {
      testConnection = connectionPool.getConnection();
      testStatement = testConnection.createStatement();
      testStatement.executeQuery("select 1");
    } catch (SQLException e) {

      _logger.error(e.getMessage());

    } finally {
      testStatement.close();
      testConnection.close();

    }

  }

  public static Datasource getInstance() {
    if (datasource == null) {
      try {
        datasource = new Datasource();
      } catch (IOException e) {
        _logger.error(e.getMessage());
      } catch (SQLException e) {
        _logger.error(e.getMessage());
      }
    }
    return datasource;

  }

  public Connection getConnection(String db) throws SQLException {
    boolean validDB = validate(db);
    Connection con = null;
    Statement statement=null/**/;
    if (validDB) {
      try {
        con = connectionPool.getConnection();
        //con.setReadOnly(true);
        statement = con.createStatement();
        statement.executeQuery("use " + db);

      } catch (SQLException e) {
        _logger.error(e.getMessage());
      } finally {
        if (statement != null) {
          statement.close();
        }

      }
    }
      return con;
    }

  private boolean validate(String db) {
    boolean result = false;
    for (String availableDB : listDB) {
      if (availableDB.equals(db)) {
        result = true;
        break;
      }
    }
    return result;
  }

}

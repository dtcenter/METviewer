package edu.ucar.metviewer.test;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import org.apache.logging.log4j.Logger;
import org.mariadb.jdbc.MariaDbDataSource;

import static java.lang.System.out;

public class TestMariaDbDatabaseManager extends MysqlDatabaseManager implements TestDBManager {

  public TestMariaDbDatabaseManager(DatabaseInfo databaseInfo, String password) {
    super(databaseInfo, password);
  }

  private static final Logger logger
          = org.apache.logging.log4j.LogManager.getLogger("TestMysqlDatabaseManager");

  public int getNumberOfRows(String lineDataType) throws Exception {
    String tableName = lineDataType;
    int rows = -1;
    try (
            java.sql.Connection con = getConnection();
            java.sql.Statement statement = getConnection().createStatement();
            java.sql.ResultSet resultSet = statement.executeQuery("select count(*) from " + tableName);
    ) {
      if (resultSet.next()) {
        rows = resultSet.getInt("count(*)");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return rows;
  }

  public void loadData(String fileName, String database) throws Exception {
    java.io.Reader reader = null;
    java.sql.Connection con = null;
    java.sql.Statement statement = null;
    try {
      con = getConnection();
      statement = con.createStatement();
      statement.executeUpdate("drop database " + database);
      statement.executeUpdate("create database " + database);
      statement.executeUpdate("use " + database);
      edu.ucar.metviewer.test.util.ScriptRunner scriptRunner = new edu.ucar.metviewer.test.util.ScriptRunner(
              con, false, true);
      reader = new java.io.FileReader(fileName);
      scriptRunner.runScript(reader);
    } catch (Exception e) {
      logger.error(e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (java.sql.SQLException e) {
          System.out.println(e.getMessage());
        }
      }
      if (statement != null) {
        try {
          statement.close();
        } catch (java.sql.SQLException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  public void checkCreateDatabase(
          String host, String userName,
          String password, String database) {
    java.sql.Connection aConn = null;
    java.sql.Statement aStmt = null;
    try {
      MariaDbDataSource aDataSource = new MariaDbDataSource();
      aDataSource.setUser(userName);
      aDataSource.setPassword(password);
      aDataSource.setServerName(host.split(":")[0]); // don't need the port here
      aDataSource.setPort(Integer.parseInt(host.split(":")[1])); // don't need the port here
      aConn = aDataSource.getConnection();
      aStmt = aConn.createStatement();
      aStmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database + ";");
    } catch (Exception e) {
      out.println(e.getMessage());
    } finally {
      if (aConn != null) {
        try {
          aConn.close();
        } catch (java.sql.SQLException e) {
          out.println(e.getMessage());
        }
      }
      if (aStmt != null) {
        try {
          aStmt.close();
        } catch (java.sql.SQLException e) {
          out.println(e.getMessage());
        }
      }
    }
  }
}


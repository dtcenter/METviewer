package edu.ucar.metviewer.test;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.ucar.metviewer.db.MysqlDatabaseManager;
import edu.ucar.metviewer.test.util.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestMysqlDatabaseManager implements TestDBManager {

  private static final Logger logger = LogManager.getLogger("TestMysqlDatabaseManager");
  private MysqlDatabaseManager databaseManager;

  public TestMysqlDatabaseManager(MysqlDatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public int getNumberOfRows(final String lineDataType) {
    int rows = -1;
    try {
      Connection con = this.databaseManager.getConnection();
      Statement statement = this.databaseManager.getConnection().createStatement();
      ResultSet resultSet = statement.executeQuery("select count(*) from " + lineDataType);
      if (resultSet.next()) {
        rows = resultSet.getInt("count(*)");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return rows;
  }

  public void loadData(String fileName, String database) throws Exception {
    Reader reader = null;
    Connection con = null;
    Statement statement = null;
    try {
      con = this.databaseManager.getConnection();
      statement = con.createStatement();
      statement.executeUpdate("drop database " + database);
      statement.executeUpdate("create database " + database);
      statement.executeUpdate("use " + database);
      ScriptRunner scriptRunner = new ScriptRunner(con, false, true);
      reader = new FileReader(fileName);
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
        } catch (SQLException e) {
          System.out.println(e.getMessage());
        }
      }
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }
}

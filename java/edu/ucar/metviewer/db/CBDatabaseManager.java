/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.*;
import com.couchbase.client.java.env.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

import edu.ucar.metviewer.MVUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class CBDatabaseManager {

  private static final Logger logger = LogManager.getLogger("CBDatabaseManager");
  //private static BoneCP connectionPool;
  protected static final String DB_PREFIX_MV = "mv_";
  protected DatabaseInfo databaseInfo;
  protected static Map<String, String> listDB = new TreeMap<>();
  protected static Map<String, List<String>> groupToDatabases = new HashMap<>();

  private DataSource dataSource;
  static CouchbaseEnvironment env;
  static Cluster cluster;
  static Bucket bucket;

  protected static final SimpleDateFormat DATE_FORMAT
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


  public CBDatabaseManager(DatabaseInfo databaseInfo, PrintWriter printStreamSql) throws CouchbaseException {

    env = DefaultCouchbaseEnvironment.builder()
            .connectTimeout(40000) //20000ms = 20s, default is 5s
            .build();

    // hardcoded bucket for now - change to command line option? XML tag?
    String bucketName = "testvsdb";
    cluster = null;
    bucket = null;

    try {
      cluster = CouchbaseCluster.create(env, databaseInfo.getHost());
      cluster.authenticate(databaseInfo.getUser(), databaseInfo.getPassword());
      bucket = cluster.openBucket(bucketName);

    }
    catch (CouchbaseException e) {
      logger.debug(e);
      logger.error("Open bucket connection for a Couchbase database did not succeed.");
      logger.error(e.getMessage());
    }
   /*
    boolean updateGroups = false;
    if (databaseInfo.getDbName() == null) {
      updateGroups = true;
    }
    initDBList(updateGroups); */
  }

  public void initDBList(boolean updateGroups) {
    listDB.clear();

    /* when this is updated for Couchbase, this query will get the list of database names */
    String nquery =  "select distinct substr(meta(`" +
                      bucket.name() +
                      "`).id, 0, position(meta(`" +
                      bucket.name() +
                      "`).id, \'::\')) as database_name from `" +
                      bucket.name() +
                      "` where type = \'file\';";

    try (Connection testConnection = dataSource.getConnection();
         Statement testStatement = testConnection.createStatement();
         ResultSet resultSet = testStatement.executeQuery(nquery)
    ) {
      String database;
      while (resultSet.next()) {
        database = resultSet.getString("TABLE_SCHEMA");
        listDB.put(database, "");
      }
      resultSet.close();
      testStatement.close();
      testConnection.close();

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
      logger.error("Can't get groups for database " + database);
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

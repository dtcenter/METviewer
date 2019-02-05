/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.couchbase;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class CBDatabaseManager extends DatabaseManager {

  private static final Logger logger = LogManager.getLogger("CBDatabaseManager");
  protected static final String DB_PREFIX_MV = "mv_";
  protected static Map<String, String> listDB = new TreeMap<>();
  protected static Map<String, List<String>> groupToDatabases = new HashMap<>();
  private static Bucket bucket = null;
  protected static final java.text.SimpleDateFormat DATE_FORMAT =
          new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
  protected static final java.time.format.DateTimeFormatter DATE_FORMAT_1
          = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public CBDatabaseManager(DatabaseInfo databaseInfo, String password) throws CouchbaseException {
    super(databaseInfo);

    // hardcoded bucket for now - change to command line option? XML tag?
    String bucketName = "testvsdb";
    CouchbaseEnvironment env;
    try {
      env = DefaultCouchbaseEnvironment.builder()
              .connectTimeout(40000) //20000ms = 20s, default is 5s
              .queryTimeout(300000) //20000ms = 20s, default is 75000ms
              .build();
      Cluster cluster = CouchbaseCluster.create(env, getDatabaseInfo().getHost());
      cluster.authenticate(getDatabaseInfo().getUser(), password);
      bucket = cluster.openBucket(bucketName);
    } catch (CouchbaseException e) {
      logger.debug(e);
      logger.error("Open bucket connection for a Couchbase database did not succeed.");
      logger.error(e.getMessage());
    }
    boolean updateGroups = false;
    if (databaseInfo.getDbName() == null) {
      updateGroups = true;
    }
    initDBList(updateGroups);
  }

  public void initDBList(boolean updateGroups) {
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> results = null;
    JsonObject firstRowObject = null;

    listDB.clear();

    // get list of database names from data file documents
    String nquery =  "select distinct dbname as database_name " +
                      "from `" +
                      getBucket().name() +
                      "` where type = \'file\';";
    try {
      queryResult = getBucket().query(N1qlQuery.simple(nquery));
      results = queryResult.allRows();

      for (N1qlQueryRow row : results) {
        firstRowObject = row.value();
        listDB.put(firstRowObject.get("database_name").toString(), "");
      }
    } catch (Exception e) {
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
    N1qlQueryResult queryResult = null;
    List<N1qlQueryRow> queryList = null;
    N1qlQueryRow firstRow = null;
    JsonObject firstRowObject = null;
    String group = MVUtil.DEFAULT_DATABASE_GROUP;
    String description = "";

    String strDataFileQuery =  "SELECT " +
            "meta().id as groupId, " +
            "type, " +
            "`group`, " +
            "description " +
            "FROM `" +
            getBucket().name() +
            "` WHERE " +
            "type = \'category\' AND " +
            "dbname = \'" + database + "\';";

    try {
      queryResult = getBucket().query(N1qlQuery.simple(strDataFileQuery));
      queryList = queryResult.allRows();

      // if a category document is present for the database, get the database group and description and the ID
      if (queryList.size() > 0) {
        firstRow = queryList.get(0);
        firstRowObject = firstRow.value();
        group = firstRowObject.get("group").toString();
        description = firstRowObject.get("description").toString();
      }

    } catch (CouchbaseException e) {
      System.out.println(e.getMessage());
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
   * Returns a connection to couchbase
   *
   * @return - connection (for couchbase a bucket)
   */
  protected Bucket getBucket() {
 /*   CouchbaseEnvironment env;
    if (bucket == null) {
//      CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
//              .connectTimeout(40000) //20000ms = 20s, default is 5s
//              .build();
      // hardcoded bucket for now - change to command line option? XML tag?
      String bucketName = "testvsdb";
      try {
        env = DefaultCouchbaseEnvironment.builder()
                .connectTimeout(40000) //20000ms = 20s, default is 5s
                .queryTimeout(300000) //20000ms = 20s, default is 75000ms
                .build();
        Cluster cluster = CouchbaseCluster.create(env, getDatabaseInfo().getHost());
        cluster.authenticate(getDatabaseInfo().getUser(), password);
        bucket = cluster.openBucket(bucketName);
      } catch (CouchbaseException e) {
        logger.debug(e);
        logger.error("Open bucket connection for a Couchbase database did not succeed.");
        logger.error(e.getMessage());
      }
    }*/
    return bucket;
  }
}

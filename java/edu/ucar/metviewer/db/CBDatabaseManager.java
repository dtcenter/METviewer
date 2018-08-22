/**
 * MysqlDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;


import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 23/05/17 09:51 $
 */
public class CBDatabaseManager extends DatabaseManager{

  private static final Logger logger = LogManager.getLogger("CBDatabaseManager");
  protected static final String DB_PREFIX_MV = "mv_";
  protected static Map<String, String> listDB = new TreeMap<>();
  protected static Map<String, List<String>> groupToDatabases = new HashMap<>();
  private static Bucket bucket = null;

  public CBDatabaseManager(DatabaseInfo databaseInfo) throws CouchbaseException {
    super(databaseInfo);
    boolean updateGroups = false;
    if (databaseInfo.getDbName() == null) {
      updateGroups = true;
    }
    initDBList(updateGroups);
  }

  public void initDBList(boolean updateGroups) {
    listDB.clear();

    /* when this is updated for Couchbase, this query will get the list of database names */
    String nquery =  "select distinct substr(meta(`" +
                      getBucket().name() +
                      "`).id, 0, position(meta(`" +
                      getBucket().name() +
                      "`).id, \'::\')) as database_name from `" +
                      getBucket().name() +
                      "` where type = \'file\';";
    try {
      N1qlQueryResult queryResult = getBucket().query(N1qlQuery.simple(nquery));
      List<N1qlQueryRow> results = queryResult.allRows();
      String database;
      Iterator<N1qlQueryRow> resultsIterator = results.iterator();
      while (resultsIterator.hasNext()) {
        database = resultsIterator.next().toString();
        listDB.put(database, "");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    if (updateGroups) {

      //init groups
      groupToDatabases.clear();

      //for each database find a group
      for (Map.Entry<String, String> database : listDB.entrySet()) {
        //String[] metadata = getDatabaseMetadata(database.getKey());
        //database.setValue(metadata[1]);

        //if (!groupToDatabases.containsKey(metadata[0])) {
          //groupToDatabases.put(metadata[0], new ArrayList<>());
        //}

        //groupToDatabases.get(metadata[0]).add(database.getKey());
      }

    }
  }


  /**
   * Returns a connection to couchbase
   *
   * @return - connection (for couchbase a bucket)
   */
  protected Bucket getBucket() {
    if (bucket == null) {
      CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
              .connectTimeout(40000) //20000ms = 20s, default is 5s
              .build();
      // hardcoded bucket for now - change to command line option? XML tag?
      String bucketName = "testvsdb";
      try {
        env = DefaultCouchbaseEnvironment.builder()
                .connectTimeout(40000) //20000ms = 20s, default is 5s
                .build();
        Cluster cluster = CouchbaseCluster.create(env, getDatabaseInfo().getHost());
        cluster.authenticate(getDatabaseInfo().getUser(), getDatabaseInfo().getPassword());
        bucket = cluster.openBucket(bucketName);
      } catch (CouchbaseException e) {
        logger.debug(e);
        logger.error("Open bucket connection for a Couchbase database did not succeed.");
        logger.error(e.getMessage());
      }
    }
    return bucket;
  }

  @Override
  public void initDBList() {

  }
}

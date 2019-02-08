/**
 * MVPruneDB.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.aurora.AuroraAppDatabaseManager;
import edu.ucar.metviewer.db.mariadb.MariaDbAppDatabaseManager;
import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Counts or deletes data that  match the criteria specified in  XML file
 *
 * @author MVPruneDB: tatiana $
 * @version : 1.0 : 11/11/16 14:02 $
 */
public class MVPruneDB {

  private static final Logger logger = LogManager.getLogger("MVPruneDB");

  private static final String USAGE = "USAGE:  mv_prune.sh <prune_db_spec_file>\n" +
                                          "                    where <prune_db_spec_file> specifies the XML pruning specification document\n";
  private final Map<String, List<String>> fieldToRangeValues = new HashMap<>();
  private final Map<String, List<String>> fieldToListValues = new HashMap<>();
  private final List<String> files = new ArrayList<>();
  private final Set<String> directories = new HashSet<>();
  private String databaseName;
  private String user;
  private String pwd;
  private String host;
  private Boolean isInfoOnly = null;

  public static void main(String[] args) throws Exception {
    String filename;
    String dbType = "mysql";
    if (0 == args.length) {
      logger.error("  Error: no arguments!!!");
      logger.info(USAGE);

    } else {
      filename = args[0];
      if (args.length > 1) {
        switch (args[1]) {
          case "mysql":
            dbType = "mysql";
            break;
          case "mariadb":
            dbType = "mariadb";
            break;
          case "aurora":
            dbType = "aurora";
            break;
        }
      }
      PruneXmlParser pruneXmlParser = new PruneXmlParser();
      MVPruneDB mvPruneDB = pruneXmlParser.parseParameters(filename);
      boolean isValid = mvPruneDB.validate();
      if (isValid) {
        MysqlDatabaseManager databaseManager = null;
        if (dbType.equals("mysql")) {
          databaseManager = new MysqlDatabaseManager(new DatabaseInfo(mvPruneDB.getHost(),
                                                                      mvPruneDB.getUser()),
                                                     mvPruneDB.getPwd());
        } else if (dbType.equals("mariadb")) {
          databaseManager = new MariaDbAppDatabaseManager(new DatabaseInfo(mvPruneDB.getHost(),
                                                                           mvPruneDB.getUser()),
                                                          mvPruneDB.getPwd());
        } else if (dbType.equals("aurora")) {
          databaseManager =
              new AuroraAppDatabaseManager(new DatabaseInfo(mvPruneDB.getHost(),
                                                            mvPruneDB.getUser()),
                                           mvPruneDB.getPwd());
        }
        PruneDbManager pruneDbManager = new PruneDbManagerMysql(databaseManager);
        pruneDbManager.pruneData(mvPruneDB);
      }

    }
    logger.info("----  MVPruneDB Done  ----");
  }

  public Set<String> getDirectories() {
    return directories;
  }

  public Boolean getInfoOnly() {
    return isInfoOnly;
  }

  public void setInfoOnly(Boolean infoOnly) {
    isInfoOnly = infoOnly;
  }

  private String getPwd() {
    return pwd;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
  }

  private String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  private String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Map<String, List<String>> getFieldToRangeValues() {
    return fieldToRangeValues;
  }

  public Map<String, List<String>> getFieldToListValues() {
    return fieldToListValues;
  }

  public List<String> getFiles() {
    return files;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  /**
   * checks if all parameters for pruning are present and valid.
   *
   * @return
   */
  private boolean validate() {
    boolean isValid = true;
    if (this.host == null) {
      logger.error("Please, specify DB host");
      isValid = false;
    } else if (this.user == null) {
      logger.error("Please, specify DB user");
      isValid = false;
    } else if (this.pwd == null) {
      logger.error("Please, specify DB password");
      isValid = false;
    } else if (this.databaseName == null) {
      logger.error("Please, specify databaseName");
      isValid = false;
    } else if ((!this.files.isEmpty() || !this.directories.isEmpty()) && (!this.fieldToListValues
                                                                               .isEmpty() || !this.fieldToRangeValues
                                                                                                  .isEmpty())) {
      logger.error("Files and fields are both presented. Please, specify one or another");
      isValid = false;
    } else if (this.fieldToListValues.isEmpty() && this.fieldToRangeValues.isEmpty() && this.files
                                                                                            .isEmpty() && this.directories
                                                                                                              .isEmpty()) {
      logger.error("Please, specify at least one field or file");
      isValid = false;
    } else if (this.isInfoOnly == null) {
      logger.error("info_only value should be 'true' or 'false'");
      isValid = false;
    } else {
      for (Map.Entry<String, List<String>> entry : this.fieldToListValues.entrySet()) {
        if (entry.getValue().isEmpty()) {
          logger.error("Field " + entry.getKey() + " doesn't have any values");
          isValid = false;
        }
      }
      for (Map.Entry<String, List<String>> entry : this.fieldToRangeValues.entrySet()) {
        if (entry.getValue().size() != 2) {
          logger.error("Field " + entry.getKey() + " doesn't have valid range");
          isValid = false;
        }
      }
    }

    return isValid;
  }

}

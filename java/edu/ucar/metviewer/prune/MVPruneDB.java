/**
 * MVPruneDB.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.*;

/**
 * Counts or deletes data that  match the criteria specified in  XML file
 *
 * @author MVPruneDB: tatiana $
 * @version : 1.0 : 11/11/16 14:02 $
 */
public class MVPruneDB {

  private static final Logger logger = Logger.getLogger(MVPruneDB.class);

  private static final String USAGE = "USAGE:  mv_prune.sh <prune_db_spec_file>\n" +
    "                    where <prune_db_spec_file> specifies the XML pruning specification document\n";


  private String databaseName;
  private String user;
  private String pwd;
  private String host;
  private final Map<String, List<String>> fieldToRangeValues = new HashMap<>();
  private final Map<String, List<String>> fieldToListValues = new HashMap<>();
  private final List<String> files = new ArrayList<>();
  private final Set<String> directories = new HashSet<>();
  private Boolean isInfoOnly = null;

  public Set<String> getDirectories() {
    return directories;
  }

  public void setInfoOnly(Boolean infoOnly) {
    isInfoOnly = infoOnly;
  }

  public Boolean getInfoOnly() {
    return isInfoOnly;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
  }

  public String getPwd() {
    return pwd;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getHost() {
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

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getDatabaseName() {
    return databaseName;
  }


  public static void main(String[] args) throws Exception {
    String filename;
    updateLog4jConfiguration();
    if (0 == args.length) {
      logger.error("  Error: no arguments!!!");
      logger.info(USAGE);

    } else {
      filename = args[0];
      PruneXmlParser pruneXmlParser = new PruneXmlParser();
      MVPruneDB mvPruneDB = pruneXmlParser.parseParameters(filename);
      boolean isValid = mvPruneDB.validate();
      if (isValid) {
        PruneDbManager pruneDbManager = new PruneDbManager();
        pruneDbManager.pruneData(mvPruneDB);
      }

    }
    logger.info("----  MVPruneDB Done  ----");
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
    } else if ((!this.files.isEmpty() || !this.directories.isEmpty()) && (!this.fieldToListValues.isEmpty() || !this.fieldToRangeValues.isEmpty())) {
      logger.error("Files and fields are both presented. Please, specify one or another");
      isValid = false;
    } else if (this.fieldToListValues.isEmpty() && this.fieldToRangeValues.isEmpty() && this.files.isEmpty() && this.directories.isEmpty()) {
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


  public static void updateLog4jConfiguration() {

    PatternLayout layout = new PatternLayout("%m%n");
    ConsoleAppender appender = new ConsoleAppender(layout);
    appender.setName("stdout");
    appender.activateOptions();
    Logger.getRootLogger().addAppender(appender);

  }

}
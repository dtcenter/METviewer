/**
 * Scorecard.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.aurora.AuroraAppDatabaseManager;
import edu.ucar.metviewer.db.mariadb.MariaDbAppDatabaseManager;
import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import edu.ucar.metviewer.scorecard.db.AggDatabaseManagerMySQL;
import edu.ucar.metviewer.scorecard.db.DatabaseManager;
import edu.ucar.metviewer.scorecard.db.SumDatabaseManagerMySQL;
import edu.ucar.metviewer.scorecard.exceptions.MissingFileException;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import edu.ucar.metviewer.scorecard.model.WorkingFolders;
import edu.ucar.metviewer.scorecard.rscript.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.util.*;

import static edu.ucar.metviewer.MVUtil.RSCRIPT;

/**
 * Creates scorecard image using configuration XML
 *
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:07 $
 */
public class Scorecard {

  private static final Logger logger = LogManager.getLogger("Scorecard");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private List<String> databaseNames;
  private String user;
  private String pwd;
  private String host;
  private WorkingFolders workingFolders; // rscript, data and output folders from XML file
  private String rTemplate; // R template "scorecard.R_tmpl"
  private String dataFile; // data file name from XML file
  private String plotFile; // image file name from XML file
  private String rScriptCommand; // Rscript command location
  private String title = "Verification Scorecard"; //title of the scorecard
  private List<Field> columns = new ArrayList<>(); // list of columns
  private List<Field> rows = new ArrayList<>(); // list of rows
  private List<Field> fixedVars = new ArrayList<>();
  private Boolean aggStat = Boolean.TRUE;
  private Boolean viewValue = Boolean.FALSE;
  private Boolean viewSymbol = Boolean.TRUE;
  private Boolean viewLegend = Boolean.TRUE;
  private Boolean printSQL = Boolean.FALSE;
  private int numBootReplicates = 1000;
  private Integer bootRandomSeed;
  private String plotStat = "median";
  private String statFlag = "NCAR";
  private String stat;
  private String statValue;
  private String statSymbol;
  private String thresholdFile = null;
  private String weightFile = null;
  private List<String> leftColumnsNames = new ArrayList<>();
  private String symbolSize = "100%";
  private String executionType = RSCRIPT;


  public static String getUsage() {
    String version = MVUtil.getVersionNumber();
    String message;
    if (!version.isEmpty()) {
      message = "Version: " + version + "\n";
    } else {
      message = "";
    }

    return message +
            "USAGE:  mv_scorecard.sh  db_type  <scorecard_spec_file>\n" +
            "                    where db_type - mysql \n <scorecard_spec_file> specifies the XML scorecard specification document\n";

  }

  public static void main(String[] args) throws Exception {


    String filename;
    String dbType = "mysql";
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    if (0 == args.length) {
      logger.info("  Error: no arguments!!!");
      logger.info(getUsage());

    } else {

      int intArg = 0;
      for (; intArg < args.length && !args[intArg].matches(".*\\.xml$"); intArg++) {
        if (args[intArg].equals("mysql")) {
          dbType = "mysql";
        } else if (args[intArg].equals("mariadb")) {
          dbType = "mariadb";
        } else if (args[intArg].equals("aurora")) {
          dbType = "aurora";
        } else if ("-h".equalsIgnoreCase(args[intArg]) || "--h".equalsIgnoreCase(args[intArg]) || "-help".equalsIgnoreCase(args[intArg])) {
          logger.info(getUsage());
          return;
        }
      }

      filename = args[intArg];

      String version  = MVUtil.getVersionNumber();
      if (!version.isEmpty()){
        logger.info("Version: " + version + "\n");
      }
      XmlParser xmlParser = new XmlParser();
      // parce XML and init parameters
      Scorecard scorecard = xmlParser.parseParameters(filename);

      //add a second model ( the same as the first one) if only one is selected
      scorecard.fillValues();

      //remove previous output with similar names
      scorecard.cleanOldResults();

      //TODO implement validation
      boolean isValid = scorecard.validate();
      if (isValid) {

        DatabaseManager scorecardDbManager = null;
        Object rscriptManager = null;
        //create a list of each row with statistic as a key and columns
        List<Map<String, Entry>> listRows = scorecard.getListOfEachRowWithDesc();

        //depending on stat type init mangers
        MysqlDatabaseManager databaseManager = null;
        DatabaseInfo databaseInfo = new DatabaseInfo(scorecard.getHost(), scorecard.getUser());
        databaseInfo.setDbName(scorecard.getDatabaseNames().get(0));
        if (dbType.equals("mysql")) {
          databaseManager = new MysqlDatabaseManager(databaseInfo, scorecard.getPwd());
        } else if (dbType.equals("mariadb")) {
          databaseManager = new MariaDbAppDatabaseManager(databaseInfo, scorecard.getPwd());
        } else if (dbType.equals("aurora")) {
          databaseManager = new AuroraAppDatabaseManager(databaseInfo, scorecard.getPwd());
        }


        if (scorecard.getAggStat()) {
          scorecardDbManager = new AggDatabaseManagerMySQL(scorecard, databaseManager);
          if(scorecard.getExecutionType().equals("Python")) {
            rscriptManager = new AggPythonManager(scorecard);
          }else {
            rscriptManager = new AggRscriptManager(scorecard);
          }
        } else {
          scorecardDbManager = new SumDatabaseManagerMySQL(scorecard, databaseManager);
          if(scorecard.getExecutionType().equals("Python")) {
            rscriptManager = new SumPythonManager(scorecard);
          }else {
            rscriptManager = new SumRscriptManager(scorecard);
          }
        }
        int rowCounter = 1;
        stopWatch.stop();
        logger.info("Scorecard init time " + stopWatch.getFormattedDuration());

        //for each row calculate statistics in the individual cell
        for (Map<String, Entry> mapRow : listRows) {

          stopWatch.start();
          StringBuilder logMessage = new StringBuilder();
          for (Map.Entry<String, Entry> column : mapRow.entrySet()) {
            logMessage.append(column.getKey()).append(": ").append(column.getValue().getName())
                    .append(", ");
          }
          logger.info(
                  "---------------------------------------------------------------------------------------");
          logger.info("Row #" + rowCounter + ": " + logMessage);
          logger.info(
                  "---------------------------------------------------------------------------------------");

          try {
            //get data from db and save it into file
            scorecardDbManager.createDataFile(mapRow, "");

            //use rscript and data from the db file to calculate stats and append them into the resulting file
            if (scorecard.getAggStat()) {
              if(scorecard.getExecutionType().equals("Python")) {
                ((AggPythonManager) rscriptManager).calculateStatsForRow(mapRow, "");
              }else {
                ((AggRscriptManager) rscriptManager).calculateStatsForRow(mapRow, "");
              }
            } else {
              if(scorecard.getExecutionType().equals("Python")) {
                ((SumPythonManager) rscriptManager).calculateStatsForRow(mapRow, "");
              }else {
                ((SumRscriptManager) rscriptManager).calculateStatsForRow(mapRow, "");
              }
            }

          } catch (Exception e) {
            logger.error(ERROR_MARKER, e.getMessage());
          }
          stopWatch.stop();
          logger.info("\nRow execution time " + stopWatch.getFormattedDuration());
          logger.info(
                  "---------------------------------------------------------------------------------------");
          rowCounter++;

        }
        stopWatch.start();
        File dataFile = new File(scorecard.getWorkingFolders().getDataDir()
                + scorecard.getDataFile());
        //if the resulting file exists - create an image and html file
        if (dataFile.exists()) {
          stopWatch.stop();
          stopWatch.start();
          GraphicalOutputManager graphicalOutputManager = new GraphicalOutputManager(scorecard);
          graphicalOutputManager.createGraphics();
          stopWatch.stop();
          logger.info("\nHTML and image  creation time " + stopWatch.getFormattedDuration());
          stopWatch.start();

        } else {
          throw new MissingFileException(dataFile.getAbsolutePath());
        }
        databaseManager.closeDataSource();
      } else {
        logger.error("Validation ERROR: Only one column can be aggregated or grouped.");
      }
    }
    stopWatch.stop();
    logger.info("\nTotal execution time " + stopWatch.getFormattedTotalDuration());

  }

  public Boolean getPrintSQL() {
    return printSQL;
  }

  public void setPrintSQL(Boolean printSQL) {
    this.printSQL = printSQL;
  }

  public String getStatFlag() {
    return statFlag;
  }

  public void setStatFlag(String statFlag) {
    this.statFlag = statFlag;
  }

  public String getStatValue() {
    return statValue;
  }

  public void setStatValue(String statValue) {
    this.statValue = statValue;
  }

  public String getStatSymbol() {
    return statSymbol;
  }

  public void setStatSymbol(String statSymbol) {
    this.statSymbol = statSymbol;
  }

  public Boolean getViewLegend() {
    return viewLegend;
  }

  public void setViewLegend(Boolean viewLegend) {
    this.viewLegend = viewLegend;
  }

  public Integer getBootRandomSeed() {
    return bootRandomSeed;
  }

  public void setBootRandomSeed(Integer bootRandomSeed) {
    this.bootRandomSeed = bootRandomSeed;
  }

  public String getPlotStat() {
    return plotStat;
  }

  public void setPlotStat(String plotStat) {
    this.plotStat = plotStat;
  }

  public int getNumBootReplicates() {
    return numBootReplicates;
  }

  public void setNumBootReplicates(int numBootReplicates) {
    this.numBootReplicates = numBootReplicates;
  }

  public Boolean getViewValue() {
    return viewValue;
  }

  public void setViewValue(Boolean viewValue) {
    this.viewValue = viewValue;
  }

  public Boolean getViewSymbol() {
    return viewSymbol;
  }

  public void setViewSymbol(Boolean viewSymbol) {
    this.viewSymbol = viewSymbol;
  }

  public String getrScriptCommand() {
    return rScriptCommand;
  }

  public void setrScriptCommand(String rScriptCommand) {
    this.rScriptCommand = rScriptCommand;
  }

  public String getPwd() {
    return pwd;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
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

  public List<String> getDatabaseNames() {
    return databaseNames;
  }

  public void setDatabaseNames(List<String> databaseNames) {
    this.databaseNames = databaseNames;
  }

  public WorkingFolders getWorkingFolders() {
    return workingFolders;
  }

  public void setWorkingFolders(WorkingFolders workingFolders) {
    this.workingFolders = workingFolders;
  }

  public void setrTemplate(String rTemplate) {
    this.rTemplate = rTemplate;
  }

  public List<Field> getFixedVars() {
    return fixedVars;
  }

  public void setFixedVars(List<Field> fixedVars) {
    this.fixedVars = fixedVars;
  }

  public void setRows(List<Field> rows) {
    this.rows = rows;
  }

  public void setColumns(List<Field> columns) {
    this.columns = columns;
  }

  private Boolean getAggStat() {
    return aggStat;
  }

  public void setAggStat(Boolean aggStat) {
    this.aggStat = aggStat;
  }

  public String getPlotFile() {
    return plotFile;
  }

  public void setPlotFile(String plotFile) {
    this.plotFile = plotFile;
  }

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }

  public String getAggStatDataFile() {
    return dataFile + ".agg_stat";
  }

  public String getSumStatDataFile() {
    return dataFile + ".sum_stat";
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getStat() {
    return stat;
  }

  public void setStat(String stat) {
    this.stat = stat;
  }

  public String getThresholdFile() {
    return thresholdFile;
  }

  public void setThresholdFile(String thresholdFile) {
    this.thresholdFile = thresholdFile;
  }
  public String getWeightFile() {
    return weightFile;
  }

  public void setWeightFile(String weightFile) {
    this.weightFile = weightFile;
  }

  public List<String> getLeftColumnsNames() {
    return leftColumnsNames;
  }

  public void setLeftColumnsNames(String columnsName) {
    this.leftColumnsNames.add(columnsName);
  }

  public String getSymbolSize() {
    return symbolSize;
  }

  public void setSymbolSize(String symbolSize) {
    this.symbolSize = symbolSize;
  }

  public String getExecutionType() {
    return executionType;
  }

  public void setExecutionType(String executionType) {
    if (executionType.equals("Rscript") || executionType.equals("Python")) {
      this.executionType = executionType;
    }
  }

  /**
   * Checks if XML included only one model. If it does - add the second model that is the same as
   * the first one
   */
  private void fillValues() {
    for (Field fixedVar : fixedVars) {
      if (fixedVar.getName().equals("model") && fixedVar.getValues().size() == 1) {
        logger.info("XML has only one model. Adding the second model that is the same as the first "
                + "one");
        fixedVar.getValues().add(new Entry(fixedVar.getValues().get(0)));
      }
    }
  }


  /**
   * removes previous output files
   */
  private void cleanOldResults() {
    //TODO remove html and other data files
    File file = new File(getWorkingFolders().getDataDir() + this.getDataFile());
    deleteFile(file);
    file = new File(getWorkingFolders().getDataDir() + this.getPlotFile());
    deleteFile(file);
    file = new File(getWorkingFolders().getDataDir() + getAggStatDataFile());
    deleteFile(file);
    file = new File(getWorkingFolders().getPlotsDir() + getPlotFile());
    deleteFile(file);
  }

  private void deleteFile(File file) {
    boolean isDeleted;
    if (file.exists()) {
      isDeleted = file.delete();
      if (isDeleted) {
        logger.info("File " + file.getPath() + " was deleted");
      } else {
        logger.info("Can't delete file " + file.getPath());
      }
    }
  }

  /**
   * Converts columns into map with key=field and values=all possible unique values
   *
   * @return
   */
  public Map<String, List<Entry>> columnsStructure() {
    Map<String, List<Entry>> result = new LinkedHashMap<>();
    for (Field field : columns) {
      List<Map<String, Entry>> maps = getMaps(field, new LinkedHashMap<>());
      for (Map<String, Entry> columnMap : maps) {
        for (Map.Entry<String, Entry> entry : columnMap.entrySet()) {
          if (!result.containsKey(entry.getKey())) {
            result.put(entry.getKey(), new LinkedList<>());
          }
          result.get(entry.getKey()).add(entry.getValue());

        }
      }
    }
    return result;
  }

  public List<Map<String, Entry>> getListOfEachRowWithDesc() {
    List<Map<String, Entry>> result = new ArrayList<>(numOfRows());
    for (Field field : rows) {
      result.addAll(getMaps(field, new LinkedHashMap<>()));
    }
    return result;
  }

  public List<Map<String, Entry>> getListOfEachColumnWithDesc() {
    List<Map<String, Entry>> result = new ArrayList<>(numOfRows());
    for (Field field : columns) {
      result.addAll(getMaps(field, new LinkedHashMap<>()));
    }
    return result;
  }

  private List<Map<String, Entry>> getMaps(Field field, Map<String, Entry> map) {
    List<Map<String, Entry>> res = new ArrayList<>();

    for (Entry value : field.getValues()) {
      Map<String, Entry> newMap = new LinkedHashMap<>(map);
      newMap.put(field.getName(), value);
      if (field.getFields().isEmpty()) {
        res.add(newMap);
      }
      for (Field field1 : field.getFields()) {
        res.addAll(getMaps(field1, newMap));
      }
    }
    return res;
  }


  private int numOfRows() {
    int total = 0;
    for (Field field : rows) {
      total = total + getRowsFromField(field);
    }
    return total;
  }


  private int getRowsFromField(Field field) {
    int total = 0;
    if (field.getFields().isEmpty()) {
      total = field.getValues().size();
    } else {
      for (Field f : field.getFields()) {
        total = total + getRowsFromField(f);
      }
    }
    return total;
  }

  private boolean validate() {
    Map<String, List<Entry>> columns = this.columnsStructure();
    int numberOfAggColumns = 0;
    for (Map.Entry<String, List<Entry>> columnEntry : columns.entrySet()) {
      for (Entry entry : columnEntry.getValue()) {
        if (entry.getName().contains(":") || entry.getName().contains(",")) {
          numberOfAggColumns++;
          break;
        }
      }
    }

    return numberOfAggColumns <= 1;
  }

}

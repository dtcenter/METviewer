/**
 * Scorecard.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard;

import edu.ucar.metviewer.scorecard.db.AggDatabaseManagerMySQL;
import edu.ucar.metviewer.scorecard.db.DatabaseManager;
import edu.ucar.metviewer.scorecard.db.SumDatabaseManagerMySQL;
import edu.ucar.metviewer.scorecard.exceptions.MissingFileException;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import edu.ucar.metviewer.scorecard.model.WorkingFolders;
import edu.ucar.metviewer.scorecard.rscript.AggRscriptManager;
import edu.ucar.metviewer.scorecard.rscript.RscriptManager;
import edu.ucar.metviewer.scorecard.rscript.SumRscriptManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Creates scorecard image using configuration XML
 *
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:07 $
 */
public class Scorecard {

  private static final Logger logger = Logger.getLogger(Scorecard.class);
  private String databaseName;
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
  private int numBootReplicates = 1000;
  private Integer bootRandomSeed;
  private String statFlag = "NCAR";

  private static final String USAGE = "USAGE:  mv_scorecatd.sh <scorecard_spec_file>\n" +
    "                    where <scorecard_spec_file> specifies the XML scorecard specification document\n";


  public String getStatFlag() {
    return statFlag;
  }

  public void setStatFlag(String statFlag) {
    this.statFlag = statFlag;
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

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public WorkingFolders getWorkingFolders() {
    return workingFolders;
  }

  public void setWorkingFolders(WorkingFolders workingFolders) {
    this.workingFolders = workingFolders;
  }

  public String getrTemplate() {
    return rTemplate;
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

  public List<Field> getRows() {
    return rows;
  }

  public void setRows(List<Field> rows) {
    this.rows = rows;
  }

  public List<Field> getColumns() {
    return columns;
  }

  public void setColumns(List<Field> columns) {
    this.columns = columns;
  }

  public Boolean getAggStat() {
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

  public String getAggStatDataFile() {
    return dataFile + ".agg_stat";
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public static void main(String[] args) throws Exception {
    long begin = System.currentTimeMillis();
    long nanos = System.nanoTime();
    String filename;
    updateLog4jConfiguration();
    if (0 == args.length) {
      logger.error("  Error: no arguments!!!");
      logger.info(USAGE);

    } else {
      filename = args[0];
      XmlParser xmlParser = new XmlParser();
      // parce XML and init parameters
      Scorecard scorecard = xmlParser.parseParameters(filename);
      //remove previous output with similar names
      scorecard.cleanOldResults();

      //TODO implement validation
      boolean isValid = scorecard.validate();
      if (isValid) {

        DatabaseManager scorecardDbManager;
        RscriptManager rscriptManager;
        //create a list of each row with statistic as a key and columns
        List<Map<String, Entry>> listRows = scorecard.getListOfEachRowWithDesc();

        //depending on stat type init mangers
        if (scorecard.getAggStat()) {
          scorecardDbManager = new AggDatabaseManagerMySQL(scorecard);
          rscriptManager = new AggRscriptManager(scorecard);
        } else {
          scorecardDbManager = new SumDatabaseManagerMySQL(scorecard);
          rscriptManager = new SumRscriptManager(scorecard);
        }
        int rowCounter = 1;
        //for each row calculate statistics in the individual cell
        for (Map<String, Entry> mapRow : listRows) {
          StringBuilder logMessage = new StringBuilder();
          for (Map.Entry<String, Entry> column : mapRow.entrySet()) {
            logMessage.append(column.getKey()).append(": ").append(column.getValue().getName()).append(", ");
          }
          logger.info("---------------------------------------------------------------------------------------");
          logger.info("Row #" + rowCounter + ": " + logMessage);
          logger.info("---------------------------------------------------------------------------------------");

          //get data from db and save it into file
          scorecardDbManager.createDataFile(mapRow, "");

          //use rscript and data from the db file to calculate stats and append them into the resulting file
          rscriptManager.calculateStatsForRow(mapRow, "");
          logger.info("---------------------------------------------------------------------------------------");
          rowCounter++;
        }

        File dataFile = new File(scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile());
        //if the resulting file exists - create an image and html file
        if (dataFile.exists()) {
          GraphicalOutputManager graphicalOutputManager = new GraphicalOutputManager(scorecard);
          graphicalOutputManager.createGraphics();

        } else {
          throw new MissingFileException(dataFile.getAbsolutePath());
        }

      }

    }
    long duration = System.nanoTime() - nanos;
    int seconds = (int) (duration / 1000000000);
    logger.info("----  Scorecard Done  ---- " + seconds + " seconds");

  }

  private static Callable<String> callable(DatabaseManager scorecardDbManager, RscriptManager rscriptManager, Map<String, Entry> mapRow, String rowCounter) {
    return () -> {

      StringBuilder logMessage = new StringBuilder();
      for (Map.Entry<String, Entry> column : mapRow.entrySet()) {
        logMessage.append(column.getKey()).append(": ").append(column.getValue().getName()).append(", ");
      }
      logger.info("---------------------------------------------------------------------------------------");
      logger.info("Row #" + rowCounter + ": " + logMessage);
      logger.info("---------------------------------------------------------------------------------------");

      scorecardDbManager.createDataFile(mapRow, rowCounter);
      logger.info("Row #" + rowCounter + ": " + "done database");
      rscriptManager.calculateStatsForRow(mapRow, rowCounter);
      logger.info("Row #" + rowCounter + ": " + "done Rscript");
      logger.info("---------------------------------------------------------------------------------------");

      return rowCounter + " done";
    };
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
          //add only unique values
          if( !result.get(entry.getKey()).contains(entry.getValue())) {
            result.get(entry.getKey()).add(entry.getValue());
          }

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


  public int numOfRows() {
    int total = 0;
    for (Field field : rows) {
      total = total + getRowsFromField(field);
    }
    return total;
  }

  public int numOfColumns() {
    int total = 0;
    for (Field field : columns) {
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
    return true;
  }

  public static void updateLog4jConfiguration() {

    PatternLayout layout = new PatternLayout("%m%n");
    ConsoleAppender appender = new ConsoleAppender(layout);
    appender.setName("stdout");
    appender.activateOptions();
    Logger.getRootLogger().addAppender(appender);

  }

}

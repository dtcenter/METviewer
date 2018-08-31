/**
 * Scorecard.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates scorecard image using configuration XML
 *
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:07 $
 */
public class Scorecard {

  private static final Logger logger = LogManager.getLogger("Scorecard");
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
  private Boolean printSQL = Boolean.FALSE;
  private int numBootReplicates = 1000;
  private Integer bootRandomSeed;
  private String plotStat = "median";
  private String statFlag = "NCAR";
  private String stat = "DIFF_SIG";
  private String thresholdFile = null;

  private static final String USAGE = "USAGE:  mv_scorecard.sh  db_type  <scorecard_spec_file>\n" +
                                          "                    where db_type - mysql \n <scorecard_spec_file> specifies the XML scorecard specification document\n";

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

  public String getAggStatDataFile() {
    return dataFile + ".agg_stat";
  }

  public String getSumStatDataFile() {
    return dataFile + ".sum_stat";
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

  public String getStat() {
    return stat;
  }

  public String getThresholdFile() {
    return thresholdFile;
  }

  public void setThresholdFile(String thresholdFile) {
    this.thresholdFile = thresholdFile;
  }

  public void setStat(String stat) {
    this.stat = stat;
  }


  public static void main(String[] args) throws Exception {
    long nanos = System.nanoTime();
    String filename;
    String dbType = "mysql";
    if (0 == args.length) {
      logger.error("  Error: no arguments!!!");
      logger.info(USAGE);

    } else {

      int intArg = 0;
      for (; intArg < args.length && !args[intArg].matches(".*\\.xml$"); intArg++) {
        if (args[intArg].equals("mysql")) {
          dbType = "mysql";
        }
      }

      filename = args[intArg];
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
        RscriptManager rscriptManager = null;
        //create a list of each row with statistic as a key and columns
        List<Map<String, Entry>> listRows = scorecard.getListOfEachRowWithDesc();

        //depending on stat type init mangers
        if (scorecard.getAggStat()) {
          if (dbType.equals("mysql")) {
            scorecardDbManager = new AggDatabaseManagerMySQL(scorecard);
          }
          rscriptManager = new AggRscriptManager(scorecard);
        } else {
          if (dbType.equals("mysql")) {
            scorecardDbManager = new SumDatabaseManagerMySQL(scorecard);
          }
          rscriptManager = new SumRscriptManager(scorecard);
        }
        int rowCounter = 1;
        //for each row calculate statistics in the individual cell
        for (Map<String, Entry> mapRow : listRows) {
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
            rscriptManager.calculateStatsForRow(mapRow, "");
            logger.info(
                "---------------------------------------------------------------------------------------");
          } catch (Exception e) {
            logger.error(e.getMessage());
          }
          rowCounter++;
        }

        File dataFile = new File(scorecard.getWorkingFolders().getDataDir()
                                     + scorecard.getDataFile());
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
    return true;
  }

}

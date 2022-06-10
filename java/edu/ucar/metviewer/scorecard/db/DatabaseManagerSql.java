/**
 * DatabaseManagerMySQL.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.DatabaseException;
import edu.ucar.metviewer.EmptyResultSetException;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import static edu.ucar.metviewer.MVUtil.GROUP_SEPARATOR;
import static edu.ucar.metviewer.db.mysql.MysqlDatabaseManager.BINARY;
import static edu.ucar.metviewer.db.mysql.MysqlDatabaseManager.DATE_FORMATTER;

/**
 * @author : tatiana $
 * @version : 1.0 : 2018-12-18 10:58 $
 */
public abstract class DatabaseManagerSql implements DatabaseManager {

  private static final Logger logger = LogManager.getLogger("DatabaseManagerSql");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private final Map<String, List<Entry>> columnsDescription;
  private final List<String> databaseNames;
  private final List<Field> fixedVars;
  private final Boolean printSQL;
  String aggStatDataFilePath;
  private MysqlDatabaseManager databaseManager;

  DatabaseManagerSql(
          final Scorecard scorecard, MysqlDatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
    fixedVars = scorecard.getFixedVars();
    columnsDescription = scorecard.columnsStructure();
    databaseNames = scorecard.getDatabaseNames();
    printSQL = scorecard.getPrintSQL();
  }

  protected abstract String getSelectFields(String table, Integer thresh);

  protected abstract String getStatValue(String table, String stat);


  @Override
  public void createDataFile(
          Map<String, Entry> map,
          String threadName) throws DatabaseException, SQLException, IOException, StopWatchException {
    String mysql = getQueryForRow(map);
    if (mysql != null) {
      if (printSQL) {
        logger.info("MySQL query: ");
        logger.info(mysql);
      }
      int lastDot = aggStatDataFilePath.lastIndexOf('.');
      String thredFileName = aggStatDataFilePath
              .substring(0, lastDot) + threadName
              + aggStatDataFilePath.substring(lastDot);
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      for(int i=0; i< databaseNames.size(); i++) {
        boolean newFile = ( i == 0);
        try (Connection con = databaseManager.getConnection(databaseNames.get(i));
             PreparedStatement pstmt = con.prepareStatement(mysql);
             ResultSet res = pstmt.executeQuery();
             FileWriter fstream = new FileWriter(new File(thredFileName), !newFile);
             BufferedWriter out = new BufferedWriter(fstream)) {
          stopWatch.stop();
          logger.info("Database query time " + stopWatch.getFormattedDuration());
          stopWatch.start();
          printFormattedTable(res, out, newFile);// isCalc=false,  isHeader=true
          stopWatch.stop();
          logger.info("Save to file time " + stopWatch.getFormattedDuration());
          out.flush();
          out.close();
          res.close();
          pstmt.close();
          con.close();
        } catch (SQLException | IOException | StopWatchException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
    }
  }

  private String getQueryForRow(Map<String, Entry> map) throws DatabaseException {
    //get fcst_var
    StringBuilder selectFields = new StringBuilder();
    StringBuilder whereFields = new StringBuilder();

    String aggType = Util.getAggTypeForStat(Util.getStatForRow(map));
    if (aggType.isEmpty()) {
      throw new DatabaseException("Can't find a line type for stat " + Util.getStatForRow(map));
    }
    if (aggType.contains("nbr")) {
      aggType = aggType.replace("_", "");
    }

    String table = "line_data_" + aggType;


    for (Map.Entry<String, Entry> entry : map.entrySet()) {
      if ("stat".equals(entry.getKey())) {
        selectFields.append("'").append(entry.getValue().getName())
                .append("' stat_name,")
                .append(getStatValue(table, entry.getValue().getName())).append(" 'NA' stat_value,");
      } else {
        if (selectFields.indexOf(entry.getKey()) == -1) {
          selectFields.append(entry.getKey()).append(",");
        }
        whereFields.append(BINARY).append(entry.getKey()).append(" IN ('")
                .append(entry.getValue().getName().replaceAll(GROUP_SEPARATOR, "','"))
                .append("') AND ");
      }
    }
    for (Field fixedField : fixedVars) {
      StringBuilder values = new StringBuilder();
      if ("fcst_valid_beg".equals(fixedField.getName())
              || "fcst_init_beg".equals(fixedField.getName())) {
        int fixedFieldValsSize = fixedField.getValues().size();
        boolean isSizeEven = fixedFieldValsSize % 2 == 0;
        whereFields.append("(");
        if(isSizeEven){
          for(int i=0; i< fixedFieldValsSize; i=i+2){
            whereFields.append(fixedField.getName()).append(" BETWEEN ").append("'")
                    .append(fixedField.getValues().get(i).getName()).append("' AND '")
                    .append(fixedField.getValues().get(i+1).getName());
            if(i<fixedFieldValsSize-2){
              whereFields.append("' OR ");
            }else {
              whereFields.append("') AND ");
            }
          }
        }else {
          for(int i=0; i< fixedFieldValsSize-1; i=i+2){
            whereFields.append(fixedField.getName()).append(" BETWEEN ").append("'")
                    .append(fixedField.getValues().get(i).getName()).append("' AND '")
                    .append(fixedField.getValues().get(i+1).getName()).append("' OR ");
          }
          whereFields.append(fixedField.getName()).append(" = '")
                  .append(fixedField.getValues().get(fixedFieldValsSize-1).getName())
                  .append("') AND ");
        }



      } else if ("init_hour".equals(fixedField.getName())) {
        for (Entry val : fixedField.getValues()) {
          values.append(Integer.valueOf(val.getName())).append(",");
        }
        if (values.length() > 0) {
          values.deleteCharAt(values.length() - 1);
        }
        whereFields.append("HOUR(fcst_init_beg) IN (").append(values.toString()).append(") AND ");
      } else if ("valid_hour".equals(fixedField.getName())) {
        for (Entry val : fixedField.getValues()) {
          values.append(Integer.valueOf(val.getName())).append(",");
        }
        if (values.length() > 0) {
          values.deleteCharAt(values.length() - 1);
        }
        whereFields.append("HOUR(fcst_valid_beg) IN (").append(values.toString()).append(") AND ");
      } else {
        for (Entry val : fixedField.getValues()) {
          values.append(val.getName()).append(GROUP_SEPARATOR);
        }
        if (values.length() > 0) {
          values.deleteCharAt(values.length() - 1);
        }
        whereFields.append(BINARY).append(fixedField.getName()).append(" IN ('")
                .append(values.toString().replaceAll(GROUP_SEPARATOR, "','")).append("') AND ");
      }
      if (selectFields.indexOf(fixedField.getName()) == -1 && !fixedField.getName().equals(
              "init_hour") && !fixedField.getName().equals("valid_hour")) {
        selectFields.append(fixedField.getName()).append(",");
      }
    }

    for (Map.Entry<String, List<Entry>> columnEntry : columnsDescription.entrySet()) {
      if (selectFields.indexOf(columnEntry.getKey()) == -1) {
        selectFields.append(columnEntry.getKey()).append(",");
      }
      List<String> uniqueValues = new ArrayList<>();

      for (Entry val : columnEntry.getValue()) {
        String[] ungrouped = val.getName().split(GROUP_SEPARATOR);
        for(String ungroupedVal : ungrouped) {
          if (!uniqueValues.contains(ungroupedVal)) {
            uniqueValues.add(ungroupedVal);
          }
        }
      }
      whereFields.append(BINARY).append(columnEntry.getKey()).append(" IN ('")
              .append(String.join("','", uniqueValues)).append("') AND ");
    }


    //add necessary fields
    if (selectFields.indexOf("fcst_valid_beg") == -1) {
      selectFields.append("fcst_valid_beg,");
    }
    if (selectFields.indexOf("fcst_lead") == -1) {
      selectFields.append("fcst_lead,");
    }


    Map<String, Integer> pctThreshInfo;
    List<String> errors = new ArrayList<>();
    List<Integer> pctThreshList = new ArrayList<>();
    if (Util.getAggTypeForStat(Util.getStatForRow(map)).equals(MVUtil.PCT)) {
      String mysql = "SELECT DISTINCT ld.n_thresh FROM stat_header h,line_data_pct ld WHERE " + whereFields + "ld.stat_header_id = h.stat_header_id";


      for (String databaseName : databaseNames) {
        pctThreshInfo = getPctThreshInfo(mysql, databaseName);
        if (1 != pctThreshInfo.get("numPctThresh")) {
          String error = "number of PCT thresholds (" + pctThreshInfo.get(
                  "numPctThresh") + ") not distinct  for database  " + databaseName + "'";
          errors.add(error);
        } else if (1 > pctThreshInfo.get("numPctThresh")) {
          String error = "invalid number of PCT thresholds ("
                  + pctThreshInfo.get("numPctThresh") + ") found for "
                  + " database " + databaseName + "'";
          errors.add(error);
        } else {
          pctThreshList.add(pctThreshInfo.get("pctThresh"));
        }
      }
    }else {
      pctThreshList.add(0);
    }

    boolean allEqual = true;
    for (Integer s : pctThreshList) {
      if (!s.equals(pctThreshList.get(0))) {
        allEqual = false;
        break;
      }
    }

    if (errors.isEmpty() && allEqual) {
      selectFields.append(getSelectFields(table, pctThreshList.get(0)));



      //make sure that selectFields doesn't have "," as the last element
      if (selectFields.lastIndexOf(",") == selectFields.length() - 1) {
        selectFields.deleteCharAt(selectFields.length() - 1);
      }


      if (Util.getAggTypeForStat(Util.getStatForRow(map)).equals(MVUtil.PCT)) {
        for (int i = 1; i < pctThreshList.get(0); i++) {
          table += ",  line_data_pct_thresh ldt" + i;
        }
        for (int i = 1; i < pctThreshList.get(0); i++) {
          if (i != 1) {
            whereFields.append("  AND");
          }
          whereFields.append(" line_data_pct.line_data_id = ldt").append(i)
                  .append(".line_data_id  AND ldt").append(i).append(".i_value = ").append(i);
        }
        whereFields.append(" AND stat_header.stat_header_id = line_data_pct.stat_header_id");
      } else {
        whereFields.append("stat_header.stat_header_id = ").append(table)
                .append(".stat_header_id;");
      }
      return "SELECT " + selectFields + " FROM stat_header," + table + " WHERE " + whereFields;
    } else {
      if(!errors.isEmpty()){
        logger.info(errors.get(0));
      }else {
        logger.info("number of  pnts  not distinct for " + whereFields);
      }
      return null;
    }
  }

  private void printFormattedTable(
          ResultSet res, BufferedWriter bufferedWriter, boolean isHeader) {

    try {
      ResultSetMetaData met = res.getMetaData();

      //  print out the column headers
      for (int i = 1; i <= met.getColumnCount(); i++) {
        if (isHeader) {
          if (1 == i) {
            bufferedWriter.write(met.getColumnLabel(i));
          } else {
            bufferedWriter.write("\t" + met.getColumnLabel(i));
          }
        }
      }
      if (isHeader) {
        bufferedWriter.write(MVUtil.LINE_SEPARATOR);
      }


      //  print out the table of values
      int intLine = 0;

      while (res.next()) {
        String line = "";
        for (int i = 1; i <= met.getColumnCount(); i++) {
          String strVal;
          String objectType = met.getColumnTypeName(i);


          if (objectType.equals("DATETIME")) {
            LocalDateTime ts = res.getTimestamp(i).toLocalDateTime();
            strVal = DATE_FORMATTER.format(ts);
          } else {

            strVal = res.getString(i);
            if (strVal == null || strVal.equalsIgnoreCase("null")) {
              strVal = "NA";
            }
            strVal = strVal.equalsIgnoreCase("-9999") ? "NA" : strVal;
          }


          if (1 == i) {
            line = line + (strVal);
          } else {
            line = line + ("\t" + strVal);
          }

        }
        bufferedWriter.write(line);
        bufferedWriter.write(MVUtil.LINE_SEPARATOR);
        intLine++;

      }

      if (0 == intLine) {
        logger.info("No data was returned from database");
        throw new EmptyResultSetException("result set contained no data");
      }

    } catch (IOException | SQLException | EmptyResultSetException e) {
      logger.error(ERROR_MARKER,
              "  **  ERROR: Caught " + e.getClass() + " in printFormattedTable(ResultSet res): " + e.getMessage());
    }
  }
  private Map<String, Integer> getPctThreshInfo(String query, String currentDBName) {
    int numPctThresh = 0;
    int pctThresh = -1;
    Map<String, Integer> result = new HashMap<>();
    try (Connection con = databaseManager.getConnection(currentDBName);
         Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                 ResultSet.CONCUR_READ_ONLY);
         ResultSet resultSet = stmt.executeQuery(query)
    ) {

      //  validate and save the number of thresholds
      while (resultSet.next()) {
        pctThresh = resultSet.getInt(1);
        numPctThresh++;
      }

    } catch (SQLException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    result.put("numPctThresh", numPctThresh);
    result.put("pctThresh", pctThresh);

    return result;
  }

}

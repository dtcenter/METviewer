/**
 * DatabaseManagerMySQL.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import edu.ucar.metviewer.EmptyResultSetException;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlDatabaseManager;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:32 $
 */
public abstract class DatabaseManagerMySQL extends MysqlDatabaseManager implements DatabaseManager {

  private static final Logger logger = LogManager.getLogger("DatabaseManagerMySQL");

  private final Map<String, List<Entry>> columnsDescription;
  private final String databaseName;
  String aggStatDataFilePath;
  private final List<Field> fixedVars;
  private final Boolean printSQL;


  DatabaseManagerMySQL(final Scorecard scorecard) throws SQLException {
    super(new DatabaseInfo(scorecard.getHost(), scorecard.getUser(), scorecard.getPwd()));
    fixedVars = scorecard.getFixedVars();
    columnsDescription = scorecard.columnsStructure();
    databaseName = scorecard.getDatabaseName();
    printSQL = scorecard.getPrintSQL();
  }

  protected abstract String getSelectFields(String table, Integer thresh);

  protected abstract String getStatValue(String table, String stat);

  @Override
  public void createDataFile(Map<String, Entry> map, String threadName) {
    String mysql = getQueryForRow(map);
    if (mysql != null) {
      if (printSQL) {
        logger.info("MySQL query: ");
        logger.info(mysql);
      }
      int lastDot = aggStatDataFilePath.lastIndexOf('.');
      String thredFileName = aggStatDataFilePath.substring(0, lastDot) + threadName + aggStatDataFilePath.substring(lastDot);
      try (Connection con = getConnection(databaseName);
           PreparedStatement pstmt = con.prepareStatement(mysql); ResultSet res = pstmt.executeQuery();
           FileWriter fstream = new FileWriter(new File(thredFileName), false);
           BufferedWriter out = new BufferedWriter(fstream)) {
        printFormattedTable(res, out, "\t", false, true);// isCalc=false,  isHeader=true
        out.flush();
        out.close();
        con.close();
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
  }

  private String getQueryForRow(Map<String, Entry> map) {
    //get fcst_var
    StringBuilder selectFields = new StringBuilder();
    StringBuilder whereFields = new StringBuilder();

    String aggType = Util.getAggTypeForStat(Util.getStatForRow(map));
    if (aggType.contains("nbr")) {
      aggType = aggType.replace("_", "");
    }
    String table = "line_data_" + aggType;


    for (Map.Entry<String, Entry> entry : map.entrySet()) {
      if ("stat".equals(entry.getKey())) {
        selectFields.append("'").append(entry.getValue().getName()).append("' stat_name,").append(getStatValue(table, entry.getValue().getName())).append(" stat_value,");
      } else {
        if (selectFields.indexOf(entry.getKey()) == -1) {
          selectFields.append(entry.getKey()).append(",");
        }
        whereFields.append(entry.getKey()).append(" IN ('").append(entry.getValue().getName().replaceAll(",", "','")).append("') AND ");
      }
    }
    for (Field fixedField : fixedVars) {
      StringBuilder values = new StringBuilder();
      if ("fcst_valid_beg".equals(fixedField.getName()) || "fcst_init_beg".equals(fixedField.getName())) {
        whereFields.append(fixedField.getName()).append(" BETWEEN ").append("'").append(fixedField.getValues().get(0).getName()).append("' AND '").append(fixedField.getValues().get(1).getName()).append("' AND ");
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
          values.append(val.getName()).append(",");
        }
        if (values.length() > 0) {
          values.deleteCharAt(values.length() - 1);
        }
        whereFields.append(fixedField.getName()).append(" IN ('").append(values.toString().replaceAll(",", "','")).append("') AND ");
      }
      if (selectFields.indexOf(fixedField.getName()) == -1 && !fixedField.getName().equals("init_hour") && !fixedField.getName().equals("valid_hour")) {
        selectFields.append(fixedField.getName()).append(",");
      }
    }

    for (Map.Entry<String, List<Entry>> columnEntry : columnsDescription.entrySet()) {
      if (selectFields.indexOf(columnEntry.getKey()) == -1) {
        selectFields.append(columnEntry.getKey()).append(",");
      }
      StringBuilder values = new StringBuilder();
      for (Entry val : columnEntry.getValue()) {
        if (values.indexOf(val.getName()) == -1) {
          values.append(val.getName()).append(",");
        }
      }
      if (values.length() > 0) {
        values.deleteCharAt(values.length() - 1);
      }
      whereFields.append(columnEntry.getKey()).append(" IN ('").append(values.toString().replaceAll(",", "','")).append("') AND ");
    }


    //add necessary fields
    if (selectFields.indexOf("fcst_valid_beg") == -1) {
      selectFields.append("fcst_valid_beg,");
    }
    if (selectFields.indexOf("fcst_lead") == -1) {
      selectFields.append("fcst_lead,");
    }

    Integer thresh = 0;
    Integer numThresh = 1;
    if (Util.getAggTypeForStat(Util.getStatForRow(map)).equals(MVUtil.PCT)) {
      Map<String, Integer> pctThreshInfo = new HashMap<>();

      String mysql = "SELECT DISTINCT ld.n_thresh FROM stat_header h,line_data_pct ld WHERE " + whereFields + "ld.stat_header_id = h.stat_header_id";
      try (Connection con = getConnection(databaseName);
           Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           ResultSet resultSet = stmt.executeQuery(mysql);
      ) {
        int numPctThresh = 0;
        int pctThresh = -1;
        //  validate and save the number of thresholds
        while (resultSet.next()) {
          pctThresh = resultSet.getInt(1);
          numPctThresh++;
        }
        pctThreshInfo.put("numPctThresh", numPctThresh);
        pctThreshInfo.put("pctThresh", pctThresh);
        resultSet.close();
        stmt.close();
        con.close();
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
      thresh = pctThreshInfo.get("pctThresh");
      numThresh = pctThreshInfo.get("numPctThresh");
    }
    if (1 == numThresh) {
      selectFields.append(getSelectFields(table, thresh));
      //make sure that selectFields doesn't have "," as the last element
      if (selectFields.lastIndexOf(",") == selectFields.length() - 1) {
        selectFields.deleteCharAt(selectFields.length() - 1);
      }


      if (Util.getAggTypeForStat(Util.getStatForRow(map)).equals(MVUtil.PCT)) {
        for (int i = 1; i < thresh; i++) {
          table += ",  line_data_pct_thresh ldt" + i;
        }
        for (int i = 1; i < thresh; i++) {
          if (i != 1) {
            whereFields.append("  AND");
          }
          whereFields.append(" line_data_pct.line_data_id = ldt").append(i).append(".line_data_id  AND ldt").append(i).append(".i_value = ").append(i);
        }
        whereFields.append(" AND stat_header.stat_header_id = line_data_pct.stat_header_id");
      } else {
        whereFields.append("stat_header.stat_header_id = ").append(table).append(".stat_header_id;");
      }
      return "SELECT " + selectFields + " FROM stat_header," + table + " WHERE " + whereFields;
    } else {
      logger.error("number of  pnts (" + numThresh + ") not distinct for " + whereFields);
      return null;
    }
  }

  private void printFormattedTable(ResultSet res, BufferedWriter bufferedWriter, String delim, boolean isCalc, boolean isHeader) {

    try {
      ResultSetMetaData met = res.getMetaData();
      //  get the column display widths
      int[] intFieldWidths = new int[met.getColumnCount()];
      for (int i = 1; i <= met.getColumnCount(); i++) {
        intFieldWidths[i - 1] = met.getColumnDisplaySize(i) + 2;
      }

      //  print out the column headers
      if (isHeader) {
        for (int i = 1; i <= met.getColumnCount(); i++) {
          if (delim.equals(" ")) {
            bufferedWriter.write(MVUtil.padEnd(met.getColumnLabel(i), intFieldWidths[i - 1]));
          } else {
            if (1 == i) {
              bufferedWriter.write(met.getColumnLabel(i));
            } else {
              bufferedWriter.write(delim + met.getColumnLabel(i));
            }
          }
        }
        bufferedWriter.write(System.getProperty("line.separator"));
      }

      //  print out the table of values
      int intLine = 0;
      boolean isValValid;
      SimpleDateFormat formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

      while (res.next()) {
        String line = "";
        isValValid = true;
        for (int i = 1; i <= met.getColumnCount(); i++) {
          String strVal;
          String objectType = met.getColumnTypeName(i);


          if (objectType.equals("DATETIME")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Timestamp ts = res.getTimestamp(i, cal);
            strVal = formatDB.format(ts);
          } else {

            strVal = res.getString(i);
            strVal = strVal.equalsIgnoreCase("null") ? "NA" : strVal;
            strVal = strVal.equalsIgnoreCase("-9999") ? "NA" : strVal;
          }
          String columnName = met.getColumnName(i);
          if (columnName.equals("stat_value") && isCalc && strVal.equals("NA")/*job.getCalcCtc() || job.getCalcSl1l2() || job.getCalcSal1l2() */) {
            isValValid = false;
          }

          if (delim.equals(" ")) {
            line = line + (MVUtil.padEnd(strVal, intFieldWidths[i - 1]));
          } else {
            if (1 == i) {
              line = line + (strVal);
            } else {
              line = line + (delim + strVal);
            }
          }
        }
        if (isValValid) {
          bufferedWriter.write(line);
          bufferedWriter.write(System.getProperty("line.separator"));
          intLine++;
        }
      }

      if (0 == intLine) {
        logger.info("No data was returned from database");
        throw new EmptyResultSetException("result set contained no data");
      }

    } catch (Exception e) {
      logger.error("  **  ERROR: Caught " + e.getClass() + " in printFormattedTable(ResultSet res): " + e.getMessage());
    }
  }

}

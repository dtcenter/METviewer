/**
 * DatabaseManagerMySQL.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import edu.ucar.metviewer.Datasource;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:32 $
 */
public abstract class DatabaseManagerMySQL implements DatabaseManager {

  private static final Logger logger = Logger.getLogger(AggDatabaseManagerMySQL.class);

  private static final String DATABASE_TYPE = "mysql";
  private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
  protected Datasource datasource;
  protected final Map<String, List<Entry>> columnsDescription;
  protected final String databaseName;
  protected String aggStatDataFilePath;
  protected final List<Field> fixedVars;


  public DatabaseManagerMySQL(final Scorecard scorecard) {
    datasource = Datasource.getInstance(DATABASE_TYPE, DATABASE_DRIVER, scorecard.getHost(), scorecard.getUser(), scorecard.getPwd());
    fixedVars = scorecard.getFixedVars();
    columnsDescription = scorecard.columnsStructure();
    databaseName = scorecard.getDatabaseName();
  }

  protected abstract String getSelectFields(String table);

  protected abstract String getStatValue(String table, String stat);

  @Override
  public void createDataFile(Map<String, Entry> map, String threadName) {
    String mysql = getQueryForRow(map);
    int lastDot = aggStatDataFilePath.lastIndexOf('.');
    String thredFileName = aggStatDataFilePath.substring(0,lastDot) + threadName + aggStatDataFilePath.substring(lastDot);
    try (Connection con = datasource.getConnection(databaseName);
         PreparedStatement pstmt = con.prepareStatement(mysql); ResultSet res = pstmt.executeQuery();
         FileWriter fstream = new FileWriter(new File(thredFileName), false);
         BufferedWriter out = new BufferedWriter(fstream)) {
      MVUtil.printFormattedTable(res, out, "\t", false, true);// isCalc=false,  isHeader=true
      out.flush();
      out.close();
      con.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  private String getQueryForRow(Map<String, Entry> map) {
    //get fcst_var
    StringBuilder selectFields = new StringBuilder();
    StringBuilder whereFields = new StringBuilder();

    String table = "line_data_" + Util.getAggTypeForStat(Util.getStatForRow(map));

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


    selectFields.append(getSelectFields(table));
    //make sure that selectFields doesn't have "," as the last element
    if (selectFields.lastIndexOf(",") == selectFields.length() - 1) {
        selectFields.deleteCharAt(selectFields.length() - 1);
    }

    whereFields.append("stat_header.stat_header_id = ").append(table).append(".stat_header_id;");


    return "SELECT " + selectFields + " FROM stat_header," + table + " WHERE " + whereFields;
  }

}

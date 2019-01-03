/**
 * PruneDbManager.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.MysqlDatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/12/16 11:43 $
 */
class PruneDbManager extends MysqlDatabaseManager {

  private static final Logger logger = LogManager.getLogger("PruneDbManager");
  private final List<Table> tables;


  public PruneDbManager(DatabaseInfo databaseInfo, String password) throws SQLException {
    super(databaseInfo, password);
    //init tables data
    tables = new ArrayList<>();
    tables.add(new Table("line_data_cnt", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_ctc", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_cts", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_enscnt", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_fho", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_isc", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_mcts", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_mpr", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_nbrcnt", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_nbrctc", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_nbrcts", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_sal1l2", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_sl1l2", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_ssvar", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_val1l2", "stat_header", "stat_header_id"));
    tables.add(new Table("line_data_vl1l2", "stat_header", "stat_header_id"));

    tables.add(new Table("line_data_mctc", "stat_header", "stat_header_id", "line_data_mctc_cnt", "line_data_id"));
    tables.add(new Table("line_data_orank", "stat_header", "stat_header_id", "line_data_orank_ens", "line_data_id"));
    tables.add(new Table("line_data_pct", "stat_header", "stat_header_id", "line_data_pct_thresh", "line_data_id"));
    tables.add(new Table("line_data_phist", "stat_header", "stat_header_id", "line_data_phist_bin", "line_data_id"));
    tables.add(new Table("line_data_pjc", "stat_header", "stat_header_id", "line_data_pjc_thresh", "line_data_id"));
    tables.add(new Table("line_data_prc", "stat_header", "stat_header_id", "line_data_prc_thresh", "line_data_id"));
    tables.add(new Table("line_data_pstd", "stat_header", "stat_header_id", "line_data_pstd_thresh", "line_data_id"));
    tables.add(new Table("line_data_rhist", "stat_header", "stat_header_id", "line_data_rhist_rank", "line_data_id"));
    tables.add(new Table("line_data_relp", "stat_header", "stat_header_id", "line_data_relp_ens", "line_data_id"));
    tables.add(new Table("line_data_eclv", "stat_header", "stat_header_id", "line_data_eclv_pnt", "line_data_id"));

    tables.add(new Table("mode_cts", "mode_header", "mode_header_id"));
    tables.add(new Table("mode_obj_pair", "mode_header", "mode_header_id"));
    tables.add(new Table("mode_obj_single", "mode_header", "mode_header_id"));

    tables.add(new Table("data_file", null, "data_file_id"));

  }


  public void pruneData(MVPruneDB mvPruneDB) {
    int totalDeleted = 0;

    try (Connection con = getConnection(mvPruneDB.getDatabaseName())) {
      // get the list of tables in database
      List<String> allTables = getAllTables(con, mvPruneDB.getDatabaseName());
      StringBuilder whereStr = createWhere(con, mvPruneDB);
      if (whereStr.length() == 0) {
        throw new Exception("Nothing to delete. Could be that listed files are not in database.");
      }
      for (String table : allTables) {
        Table tableObj = getTableByName(table);
        if (tableObj != null) {
          totalDeleted = totalDeleted + processTable(tableObj, con, whereStr, mvPruneDB.getInfoOnly());
        }
      }
      logger.info("Total deleted " + (totalDeleted) + " records");

      if (!mvPruneDB.getInfoOnly()) {
        //remove empty header records
        int totalStatHeader = cleanHeaderTable(con, "stat");
        logger.info("From stat_header table was deleted  " + (totalStatHeader) + " records");
        int totalModeHeader = cleanHeaderTable(con, "mode");
        logger.info("From mode_header table was deleted  " + (totalModeHeader) + " records");

      }

      con.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

  }

  /**
   * Finds all ID in the header table that don't have foreign keys equivalents in the dependant tables
   *
   * @param con
   * @param type
   * @return
   */
  private List<Integer> findHeaderIdsForDeletion(Connection con, String type) {
    List<Integer> headerIdForDeletion = new ArrayList<>();
    StringBuilder emptyHeaderIdSql = new StringBuilder();

    int index = 0;
    //construct SQL query using  each table with type
    for (Table table : tables) {
      if (table.getHeaderTable() != null && table.getHeaderTable().startsWith(type)) {
        if (index == 0) {
          emptyHeaderIdSql.append("SELECT ").append(table.getHeaderKey()).append(" FROM ").append("(SELECT ").append(table.getHeaderTable()).append('.').append(table.getHeaderKey()).append("   FROM ").append(table.getHeaderTable()).append("     LEFT OUTER JOIN ").append(table.getName()).append(" ON ").append(table.getName()).append('.').append(table.getHeaderKey()).append("= ").append(table.getHeaderTable()).append('.').append(table.getHeaderKey()).append("   WHERE ").append(table.getName()).append('.').append(table.getHeaderKey()).append(" IS NULL) t").append(index).append("  INNER JOIN");
        } else {
          emptyHeaderIdSql.append("(SELECT ").append(table.getHeaderTable()).append('.').append(table.getHeaderKey()).append("   FROM ").append(table.getHeaderTable()).append("     LEFT OUTER JOIN ").append(table.getName()).append(" ON ").append(table.getName()).append('.').append(table.getHeaderKey()).append("= ").append(table.getHeaderTable()).append('.').append(table.getHeaderKey()).append("   WHERE ").append(table.getName()).append('.').append(table.getHeaderKey()).append(" IS NULL) t").append(index).append(" USING (").append(table.getHeaderKey()).append(')').append("  INNER JOIN");
        }
        index++;
      }
    }
    //remove last "INNER JOIN"
    for (int i = 0; i <= 9; i++) {
      emptyHeaderIdSql.deleteCharAt(emptyHeaderIdSql.length() - 1);
    }
    //execute SQL and process result
    try (PreparedStatement pstmt = con.prepareStatement(emptyHeaderIdSql.toString()); ResultSet r = pstmt.executeQuery()) {
      while (r.next()) {
        headerIdForDeletion.add(r.getInt(1));
      }
      pstmt.close();
      r.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    return headerIdForDeletion;
  }

  /**
   * removes all records fro the header tables that  don't have foreign keys equivalents in the dependant tables
   *
   * @param con
   * @param type
   * @return - number of deleted records
   */
  private int cleanHeaderTable(Connection con, String type) {
    int totalDeleted = 0;
    //get IDs
    List<Integer> headerIdForDeletion = findHeaderIdsForDeletion(con, type);

    if (!headerIdForDeletion.isEmpty()) {
      //create CSV from the list
      StringBuilder ids = new StringBuilder();
      for (Integer id : headerIdForDeletion) {
        ids.append(id).append(',');
      }
      if (ids.length() > 0) {
        ids.deleteCharAt(ids.length() - 1);
      }

      //construct delete SQL
      String deleteRowsSql = "DELETE FROM " + type + "_header WHERE " + type + "_header_id IN (" + ids + ")";

      //execute
      try (PreparedStatement pstmt = con.prepareStatement(deleteRowsSql)) {
        totalDeleted = pstmt.executeUpdate();
      } catch (SQLException e) {
        logger.error(e);
      }

      //optimize table
      String optimizeSql = "OPTIMIZE TABLE " + type + "_header";
      try (PreparedStatement pstmt = con.prepareStatement(optimizeSql); ResultSet r = pstmt.executeQuery()) {
        r.close();
        pstmt.close();
      } catch (SQLException e) {
        logger.error(e);
      }
    }
    return totalDeleted;
  }

  /**
   * counts or deletes records from the table that match the criteria
   *
   * @param tableObj
   * @param con
   * @return
   */
  private int processTable(Table tableObj, Connection con, StringBuilder whereStr, boolean isInfoOnly) {

    int deleted;
    String message;
    if (tableObj.getDependentTable() == null) {
      message = "From table " + tableObj.getName();
    } else {
      message = "From tables " + tableObj.getName() + " and " + tableObj.getDependentTable();
    }
    if (isInfoOnly) {
      //count number of records for deletion and print it
      deleted = countRowsForDeletion(con, tableObj, whereStr);

      logger.info(message + " would be deleted " + (deleted) + " records");

    } else {
      //delete
      deleted = deleteRows(con, tableObj, whereStr);
      if (deleted > 0) {
        optimizeTable(con, tableObj);
      }
      logger.info(message + " deleted " + (deleted) + " records.");
    }
    return deleted;
  }

  /**
   * optimizes table and it's dependant if exists
   *
   * @param con
   * @param table
   */
  private void optimizeTable(Connection con, Table table) {

    try (PreparedStatement pstmt = con.prepareStatement("OPTIMIZE TABLE " + table.getName()); ResultSet r = pstmt.executeQuery()) {
      r.close();
      pstmt.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    if (table.getDependentTable() != null) {
      try (PreparedStatement pstmt = con.prepareStatement("OPTIMIZE TABLE " + table.getDependentTable()); ResultSet r = pstmt.executeQuery()) {
        r.close();
        pstmt.close();
      } catch (SQLException e) {
        logger.error(e);
      }
    }
  }

  private Table getTableByName(String name) {
    Table tableObj = null;
    for (Table table1 : tables) {
      if (table1.getName().equals(name)) {
        tableObj = table1;
        break;
      }
    }
    return tableObj;
  }

  /**
   * returns a list of all table names from the selected schema
   *
   * @param con
   * @return
   */
  private List<String> getAllTables(Connection con, String databaseName) {
    ResultSet res = null;
    List<String> allTables = new ArrayList<>();
    try (PreparedStatement pstmt = con.prepareStatement("SELECT TABLE_NAME FROM (SELECT DISTINCT TABLE_NAME   FROM INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = ? ) as all_tables "
    )) {
      pstmt.setString(1, databaseName);
      res = pstmt.executeQuery();
      while (res.next()) {
        allTables.add(res.getString(1));
      }
      res.close();
      pstmt.close();
    } catch (SQLException e) {
      logger.error(e);
    } finally {
      if (res != null) {
        try {
          res.close();
        } catch (SQLException e) {
          logger.error(e);
        }
      }
    }
    return allTables;
  }

  /**
   * deletes roes from the table and it's dependant if exists
   *
   * @param con
   * @param table
   * @param whereStr
   * @return
   */
  private int deleteRows(Connection con, Table table, StringBuilder whereStr) {
    String sql;
    int rows = 0;
    //ONLY delete rows from the table itself and dependent table if exists
    if (table.getDependentTable() == null && table.getHeaderTable() == null) {
      sql = "DELETE  FROM " + table.getName() + " WHERE " + whereStr;
    } else if (table.getDependentTable() == null) {
      sql = "DELETE " + table.getName() + " FROM " + table.getHeaderTable() + ", " + table.getName() +
        " WHERE " + table.getHeaderTable() + "." + table.getHeaderKey() + " = " + table.getName() + "." + table.getHeaderKey() + "  AND " + whereStr;
    } else {
      sql = "DELETE " + table.getName() + ", " + table.getDependentTable() + " FROM " + table.getHeaderTable() + ", " + table.getName() + ", " + table.getDependentTable() +
        " WHERE " + table.getHeaderTable() + "." + table.getHeaderKey() + " = " + table.getName() + "." + table.getHeaderKey() +
        " AND " + table.getName() + "." + table.getDependentKey() + " = " + table.getDependentTable() + "." + table.getDependentKey() +
        "  AND " + whereStr;
    }
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      rows = pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      logger.error(e);
      printUnknownColumnMessage(e, table);
    }
    return rows;
  }

  /**
   * counts rows that would be deleted
   *
   * @param con
   * @param table
   * @param whereStr
   * @return
   */
  private int countRowsForDeletion(Connection con, Table table, StringBuilder whereStr) {
    int total = 0;
    String sql;
    if (table.getHeaderTable() == null && table.getDependentTable() == null) {
      sql = "SELECT COUNT(*)  FROM " + table.getName() + " WHERE " + whereStr;
    } else if (table.getDependentTable() == null) {
      sql = "SELECT COUNT(*)  FROM " + table.getName() + "," + table.getHeaderTable() +
        " WHERE " + table.getHeaderTable() + "." + table.getHeaderKey() + " = " + table.getName() + "." + table.getHeaderKey() + "  AND " + whereStr;
    } else {
      sql = "SELECT COUNT(*)  FROM " + table.getName() + ", " + table.getDependentTable() + "," + table.getHeaderTable() +
        " WHERE " + table.getHeaderTable() + "." + table.getHeaderKey() + " = " + table.getName() + "." + table.getHeaderKey() +
        " AND " + table.getName() + "." + table.getDependentKey() + " = " + table.getDependentTable() + "." + table.getDependentKey() +
        "  AND " + whereStr;

    }
    try (PreparedStatement pstmt = con.prepareStatement(sql); ResultSet res = pstmt.executeQuery()) {

      while (res.next()) {
        total = res.getInt(1);
      }
      res.close();
      pstmt.close();
    } catch (SQLException e) {
      logger.error(e);
      printUnknownColumnMessage(e, table);
    }
    if (table.getHeaderTable() != null && table.getDependentTable() != null) {
      sql = "SELECT COUNT(*)  FROM " + table.getName() + "," + table.getHeaderTable() +
        " WHERE " + table.getHeaderTable() + "." + table.getHeaderKey() + " = " + table.getName() + "." + table.getHeaderKey() + "  AND " + whereStr;
      try (PreparedStatement pstmt = con.prepareStatement(sql); ResultSet res = pstmt.executeQuery()) {

        while (res.next()) {
          total = total + res.getInt(1);
        }
        res.close();
        pstmt.close();
      } catch (SQLException e) {
        logger.error(e);
        printUnknownColumnMessage(e, table);
      }
    }
    return total;
  }

  /**
   * prints verbalised message if the field is not present in this table
   *
   * @param e
   * @param table
   */
  private void printUnknownColumnMessage(Exception e, Table table) {
    if (e.getMessage().contains("Unknown column")) {
      Pattern pattern = Pattern.compile("'(.*?)'");
      Matcher matcher = pattern.matcher(e.getMessage());
      String field = "";
      if (matcher.find()) {
        field = matcher.group(1);
      }
      logger.info("Field " + field + " is not in table '" + table.getName() + "'. Skipping this table");
    }
  }

  private StringBuilder createWhere(Connection con, MVPruneDB mvPruneDB) {
    StringBuilder result = new StringBuilder();
    if (!mvPruneDB.getFieldToListValues().isEmpty() || !mvPruneDB.getFieldToRangeValues().isEmpty()) {
      //proceed with fields pruning

      for (Map.Entry<String, List<String>> entry : mvPruneDB.getFieldToListValues().entrySet()) {
        result.append(' ').append(entry.getKey()).append(" IN (");
        for (String value : entry.getValue()) {
          result.append("'").append(value).append("'").append(',');
        }
        if (result.length() > 5) {
          result.deleteCharAt(result.length() - 1);
        }
        result.append(") ").append(" AND ");
      }
      for (Map.Entry<String, List<String>> entry : mvPruneDB.getFieldToRangeValues().entrySet()) {
        result.append(' ').append(entry.getKey()).append(" >= '").append(entry.getValue().get(0)).append("' AND ").append(entry.getKey()).append(" <= '").append(entry.getValue().get(1)).append("'");
        result.append(" AND ");
      }

      for (int i = 0; i < 4; i++) {
        result.deleteCharAt(result.length() - 1);
      }
    } else {
      //proceed with files and direcrories pruning

      //get ids for files
      String sql = "SELECT data_file_id from data_file WHERE filename = ? and path = ?";
      Set<Integer> fileIds = new HashSet<>();
      for (String fileStr : mvPruneDB.getFiles()) {
        File f = new File(fileStr);
        ResultSet res = null;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

          pstmt.setString(1, f.getName());
          pstmt.setString(2, f.getParent());
          res = pstmt.executeQuery();
          while (res.next()) {
            fileIds.add(res.getInt(1));
          }
          pstmt.close();

        } catch (SQLException e) {
          logger.error(e);

        } finally {
          if (res != null) {
            try {
              res.close();
            } catch (SQLException e) {
              logger.error(e);
            }
          }
        }
      }

      //get ids for directories
      sql = "SELECT data_file_id from data_file WHERE path = ?";
      for (String fileStr : mvPruneDB.getDirectories()) {
        ResultSet res = null;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

          pstmt.setString(1, fileStr);
          res = pstmt.executeQuery();
          while (res.next()) {
            fileIds.add(res.getInt(1));
          }
          pstmt.close();

        } catch (SQLException e) {
          logger.error(e);

        } finally {
          if (res != null) {
            try {
              res.close();
            } catch (SQLException e) {
              logger.error(e);
            }
          }
        }
      }

      if (!fileIds.isEmpty()) {
        result.append(' ').append("data_file_id").append(" IN (");
        for (Integer id : fileIds) {
          result.append(id).append(',');
        }
        result.deleteCharAt(result.length() - 1);
        result.append(')');
      }
    }
    return result;
  }

}

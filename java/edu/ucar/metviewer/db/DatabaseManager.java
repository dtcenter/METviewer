/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:42 $
 */
public abstract class DatabaseManager {

  protected static final String DB_PREFIX_MV = "mv_";
  protected DatabaseInfo databaseInfo;
  protected static List<String> listDB;
  public static final String[] SQL_INJECTION_WORDS = new String[]{
      "OR ", "--", "SELECT", "UNION", "DROP", "CREATE"
  };


  public DatabaseManager(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
    listDB = new ArrayList<>();

  }

  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  public abstract void initDBList();

}

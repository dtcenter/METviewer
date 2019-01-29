/**
 * AuroraLoadDatabaseManager.java Copyright UCAR (c) 2018. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer.db.aurora;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.mysql.MysqlLoadDatabaseManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 2018-12-19 15:08 $
 */
public class AuroraLoadDatabaseManager extends MysqlLoadDatabaseManager {

  public AuroraLoadDatabaseManager(DatabaseInfo databaseInfo, String password) throws Exception {
    super(databaseInfo, password);
  }

}

/**
 * PruneDbManager.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import java.util.Map;

import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import edu.ucar.metviewer.scorecard.db.DatabaseManager;
import edu.ucar.metviewer.scorecard.model.Entry;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/12/16 11:43 $
 */
abstract class PruneDbManager implements DatabaseManager {

  protected MysqlDatabaseManager databaseManager;

  public PruneDbManager(MysqlDatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public abstract void pruneData(MVPruneDB mvPruneDB);


  @Override
  public void createDataFile(
      Map<String, Entry> map, String threadName) {
        // do nothing
  }
}

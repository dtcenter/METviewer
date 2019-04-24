/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import edu.ucar.metviewer.DatabaseException;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.scorecard.model.Entry;

/**
 * @author : tatiana $
 * @version : 1.0 : 24/01/17 08:40 $
 */
@FunctionalInterface
public interface DatabaseManager {
  void createDataFile(Map<String, Entry> map, String threadName) throws DatabaseException, SQLException, IOException, StopWatchException;
}

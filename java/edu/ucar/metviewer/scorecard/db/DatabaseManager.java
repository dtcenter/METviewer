/**
 * DatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import edu.ucar.metviewer.scorecard.model.Entry;

import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 24/01/17 08:40 $
 */
@FunctionalInterface
public interface DatabaseManager {
  public void createDataFile(Map<String, Entry> map, String threadName) ;
}

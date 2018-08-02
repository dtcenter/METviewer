/**
 * LoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.File;
import java.util.Map;

import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.MVLoadJob;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/06/17 12:33 $
 */
public interface LoadDatabaseManager {

  void dropIndexes() throws Exception;

  void applyIndexes() throws Exception;

  void updateInfoTable(String strXML, MVLoadJob job) throws Exception;

  DataFileInfo processDataFile(File file, boolean forceDupFile) throws Exception;

  Map<String, Long> loadStatFile(DataFileInfo info) throws Exception;

  Map<String, Long> loadModeFile(DataFileInfo info) throws Exception;

  Map<String, Long> loadStatFileVSDB(DataFileInfo info) throws Exception;

  Map<String, Long> loadMtdFile(DataFileInfo info) throws Exception;

  void updateGroup(final String group) throws Exception;

  void updateDescription(String description) throws Exception;
}

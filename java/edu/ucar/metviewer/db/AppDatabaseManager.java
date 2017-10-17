/**
 * AppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import edu.ucar.metviewer.MVNode;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;

import java.io.PrintStream;
import java.util.List;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/06/17 12:14 $
 */
public interface AppDatabaseManager {

  List<String> buildPlotSQL(MVPlotJob job, MVOrderedMap mapPlotFixPerm, MVOrderedMap mapPlotFixVal,PrintStream printStreamSQL) throws Exception;

  List<String> buildPlotModeEventEqualizeSQL(MVPlotJob job, MVOrderedMap mapPlotFixPerm, MVOrderedMap mapPlotFixVal) throws Exception;

  boolean executeQueriesAndSaveToFile(List<String> queries, String fileName, boolean isCalc, String currentDBName) throws Exception;

  String buildAndExecuteQueriesForHistJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm,  PrintStream printStream, PrintStream printStreamSql) throws Exception;

  int buildAndExecuteQueriesForRocRelyJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm,  PrintStream printStream, PrintStream printStreamSql) throws Exception;
  int buildAndExecuteQueriesForEclvJob(MVPlotJob job, String strDataFile, MVOrderedMap listPlotFixPerm,  PrintStream printStream, PrintStream printStreamSql) throws Exception;

  DatabaseInfo getDatabaseInfo();

  List<String> getListValues(MVNode nodeCall, String strField, String currentDBName);

  List<String> getListStat(String strFcstVar, String currentDBName);

  boolean validate(String db);

  List<String> getAllDatabases();

  void initDBList();

}

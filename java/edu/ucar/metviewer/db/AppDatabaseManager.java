/**
 * AppDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.ucar.metviewer.MVNode;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.MvResponse;
import edu.ucar.metviewer.ValidationException;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/06/17 12:14 $
 */
public interface AppDatabaseManager {

  List<String> buildPlotSql(
          MVPlotJob job, MVOrderedMap mapPlotFixPerm,
          PrintStream printStreamSql) throws ValidationException;

  List<String> buildPlotModeEventEqualizeSql(
          MVPlotJob job, MVOrderedMap mapPlotFixPerm,
          MVOrderedMap mapPlotFixVal) throws ValidationException;

  MvResponse executeQueriesAndSaveToFile(
          List<String> queries, String fileName, boolean isCalc,
          String currentDBName, boolean isNewFile) throws ValidationException;

  String buildAndExecuteQueriesForHistJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm, PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException;

  int buildAndExecuteQueriesForRocRelyJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm, PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException;

  int buildAndExecuteQueriesForEclvJob(
          MVPlotJob job, String strDataFile,
          MVOrderedMap listPlotFixPerm, PrintStream printStream,
          PrintStream printStreamSql) throws ValidationException;

  DatabaseInfo getDatabaseInfo();

  List<String> getListValues(MVNode nodeCall, String strField, String[] currentDBName);

  List<String> getListStat(String strFcstVar, String[] currentDBName);

  boolean validate(String db);

  List<String> getAllDatabases();

  Map<String, String> getAllDatabasesWithDescription();

  Map<String, List<String>> getAllGroups();

  void initDBList(boolean updateGroups);

  public SimpleDateFormat getDateFormat();

  public abstract Pattern getDateRangePattern();

  public void closeDataSource();

}

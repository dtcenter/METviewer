/**
 * RscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import java.util.*;

import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;


/**
 * Constructs and runs Rscript
 *
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 09:22 $
 */
public abstract class PythonManager {

  protected final Map<String, List<Entry>> listColumns;
  protected final List<Field> fixedVars;
  protected final String rScriptCommand;
  protected String indyVar;
  protected Map<String, List<String>> indyList;
  protected Map<String, List<String>> seriesList;
  protected List<List<String>> seriesDiffList;
  protected List<Entry> models;
  private StringBuilder diffVals;
  protected String fcstVar;

  private Map<String, List<String>> fixVars;
  protected String stat;
  protected String diffStatValue;
  protected String diffStatSymbol;
  protected String python;
  protected String metCalcpyHome;


  PythonManager(final Scorecard scorecard) {
    this.listColumns = scorecard.columnsStructure();
    fixedVars = scorecard.getFixedVars();
    rScriptCommand = scorecard.getrScriptCommand();
    diffStatValue = scorecard.getStatValue();
    diffStatSymbol = scorecard.getStatSymbol();
    python = System.getProperty("python.env") + "/bin/python";
    metCalcpyHome = System.getProperty("metcalcpy.home");
  }

  public abstract void calculateStatsForRow(Map<String, Entry> mapRow, String threadName);

  /**
   * Creates a list of comparing models
   */
  void initModels() {
    for (Field fixedField : fixedVars) {
      if ("model".equals(fixedField.getName())) {
        models = fixedField.getValues();
        break;
      }
    }
  }

  void init(Map<String, Entry> mapRow) {
    stat = Util.getStatForRow(mapRow);

    for (Map.Entry<String, Entry> entry : mapRow.entrySet()) {
      if ("fcst_var".equals(entry.getKey())) {
        fcstVar = entry.getValue().getName();
      } else if (!"stat".equals(entry.getKey())) { // do not include stat variable to the fix vars list
        List<String> fixVarList = new ArrayList<>();
        fixVarList.add(entry.getValue().getName());
        fixVars.put(entry.getKey(), fixVarList);
        diffVals.append(entry.getValue().getName()).append(" ");
      }
    }

    if (diffVals.length() > 0) {
      diffVals.deleteCharAt(diffVals.length() - 1);
    }

    int size = listColumns.entrySet().iterator().next().getValue().size();
    List<String> columnsPermutationList = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      columnsPermutationList.add("");
    }


    List<String> diffStats = new ArrayList<>();
    if (diffStatSymbol != null) {
      diffStats.add(diffStatSymbol);
    }
    if (diffStatValue != null && !diffStats.contains(diffStatValue)) {
      diffStats.add(diffStatValue);
    }


    for (Map.Entry<String, List<Entry>> entry : listColumns.entrySet()) {
      Map<String, List<String>> indyListTemp = new HashMap<>();
      List<String> indyVals = new ArrayList<>();
      indyListTemp.put(entry.getKey(), indyVals);
      for (Entry val : entry.getValue()) {
        if (!indyVals.contains(val.getName())) {
          indyVals.add(val.getName());
        }
      }


      if (indyVar.isEmpty()) {
        indyVar = entry.getKey();
        indyList = new HashMap<>(indyListTemp);
      } else {
        seriesList = new HashMap<>(indyListTemp);
      }
    }
    for (int i = 0; i < size; i++) {
      for (Map.Entry<String, List<Entry>> entry : listColumns.entrySet()) {
        // do not include indy var
        if (!entry.getKey().equals(indyVar)) {
          columnsPermutationList.set(i, columnsPermutationList.get(i) + entry.getValue().get(i).getName() + " ");
        }
      }
    }
    for (String pe : columnsPermutationList) {
      for (String st : diffStats) {
        StringBuilder difStr;
        List<String> diffSeries = new ArrayList<>();
        for (Entry model : models) {
          difStr = new StringBuilder();
          if (diffVals.length() > 0) {
            difStr.append(diffVals).append(" ");
          }
          difStr.append(model.getName()).append(" ").append(pe.trim()).append(" ")
                  .append(fcstVar).append(" ").append(stat);
          diffSeries.add(difStr.toString().trim());
        }

        diffSeries.add(st);
        if (!seriesDiffList.contains(diffSeries)) {
          seriesDiffList.add(diffSeries);
        }
      }

    }


    seriesList.putAll(fixVars);
    List<String> modelList = new ArrayList<>();
    seriesList.put("model", modelList);
    for (Entry val : models) {
      modelList.add(val.getName());
    }

  }

  void clean() {
    indyVar = "";
    indyList = new HashMap<>();
    seriesList = new HashMap<>();
    seriesDiffList = new ArrayList<>();
    models = null;
    diffVals = new StringBuilder();
    fcstVar = null;
    fixVars = new HashMap<>();
    stat = null;
  }

}


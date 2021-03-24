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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Constructs and runs Rscript
 *
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 09:22 $
 */
public abstract class RscriptManager {

  private static final Logger logger = LogManager.getLogger("RscriptManager");
  final Map<String, List<Entry>> listColumns;
  final List<Field> fixedVars;
  final String rScriptCommand;
  String indyVar;
  StringBuilder indyList;
  StringBuilder seriesList;
  StringBuilder seriesDiffList;
  List<Entry> models;
  private StringBuilder diffVals;
  String fcstVar;
  private List<String> diffSeries;
  private StringBuilder fixVars;
  String stat;
  //String diffStat;
  String diffStatValue;
  String diffStatSymbol;
  StringBuilder listFixedValEx;


  RscriptManager(final Scorecard scorecard) {
    this.listColumns = scorecard.columnsStructure();
    fixedVars = scorecard.getFixedVars();
    rScriptCommand = scorecard.getrScriptCommand();
    //diffStat = scorecard.getStat();
    diffStatValue = scorecard.getStatValue();
    diffStatSymbol = scorecard.getStatSymbol();
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
        fixVars.append("`").append(entry.getKey()).append("` = c(\"").append(entry.getValue().getName()).append("\"),");
        diffVals.append(entry.getValue().getName()).append(" ");
      }
    }

    if (fixVars.length() > 0) {
      fixVars.deleteCharAt(fixVars.length() - 1);
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
      StringBuilder strForList = new StringBuilder();
      strForList.append("`").append(entry.getKey()).append("` = c(");
      boolean isAggregatedField = false;
      for (Entry val : entry.getValue()) {
        if(val.getName().contains(":")){
          isAggregatedField = true;
        }
        if (strForList.indexOf(val.getName()) == -1) {
          strForList.append("\"").append(val.getName()).append("\",");
        }
      }
      if (strForList.length() > 0) {
        strForList.deleteCharAt(strForList.length() - 1);
      }

      if(/*!isAggregatedField && */indyVar.isEmpty()){
        indyVar = entry.getKey();
        indyList.append(strForList);
        indyList.append(")");
      }else {
        seriesList.append(strForList);
        seriesList.append("),");
      }
    }
    for (int i = 0; i < size; i++) {
      for (Map.Entry<String, List<Entry>> entry : listColumns.entrySet()) {
        // do not include indy var
        if(!entry.getKey().equals(indyVar)) {
          columnsPermutationList.set(i, columnsPermutationList.get(i) + entry.getValue().get(i).getName() + " ");
        }
      }
    }
    for (String pe : columnsPermutationList) {
      for (String st : diffStats) {
        StringBuilder difStr = new StringBuilder("c(");
        for (Entry model : models) {
          difStr.append("\"");
          if (diffVals.length() > 0) {
            difStr.append(diffVals).append(" ");
          }
          difStr.append(model.getName()).append(" ").append(pe.trim()).append(" ")
                  .append(fcstVar).append(" ").append(stat).append("\",");
        }

        difStr.append("\"").append(st).append("\"),");
        diffSeries.add(difStr.toString().trim());
      }
    }

    //indyVar = "scorecard";
    //indyList.append("\"").append("column").append("\"");
    if (seriesList.charAt(seriesList.length() - 1) == ',' && fixVars.length() == 0) {
      seriesList.deleteCharAt(seriesList.length() - 1);
    }
    seriesList.append(fixVars).append(", `model` = c(");
    for (Entry val : models) {
      seriesList.append("\"").append(val.getName()).append("\",");
    }
    if (seriesList.length() > 0) {
      seriesList.deleteCharAt(seriesList.length() - 1);
    }

    seriesList.append("))");

    for (String diff : diffSeries) {
      if (seriesDiffList.indexOf(diff) == -1) {
        seriesDiffList.append(diff);
      }
    }
    if (seriesDiffList.length() > 0) {
      seriesDiffList.deleteCharAt(seriesDiffList.length() - 1);
    }
    seriesDiffList.append(")");
    for (Field fixedField : fixedVars) {
      if (!"model".equals(fixedField.getName())
              && !"fcst_lead".equals(fixedField.getName())
              && !"fcst_valid_beg".equals(fixedField.getName())
              && fixedField.isEqualize()) {
        listFixedValEx.append("`").append(fixedField.getName()).append("`=c(");
        for(Entry val : fixedField.getValues()){
          listFixedValEx.append("\"").append(val.getName()).append("\"").append(",");
        }
        if (listFixedValEx.length() > 0) {
          listFixedValEx.deleteCharAt(listFixedValEx.length() - 1);
        }
        listFixedValEx.append("),");
      }
    }
    if (listFixedValEx.length() > 0 && !listFixedValEx.toString().equals("list(")) {
      listFixedValEx.deleteCharAt(listFixedValEx.length() - 1);
    }
    listFixedValEx.append(")");
  }

  void clean() {
    indyVar = "";
    indyList = new StringBuilder();
    seriesList = new StringBuilder("list(");
    seriesDiffList = new StringBuilder("list(");
    listFixedValEx = new StringBuilder("list(");
    models = null;
    diffVals = new StringBuilder();
    fcstVar = null;
    diffSeries = new ArrayList<>();
    fixVars = new StringBuilder();
    stat = null;
  }

}

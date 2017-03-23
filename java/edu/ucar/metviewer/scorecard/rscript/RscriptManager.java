/**
 * RscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.Util;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 09:22 $
 */
public abstract class RscriptManager {

  private static final Logger logger = Logger.getLogger(RscriptManager.class);
  protected final Map<String, List<Entry>> listColumns;
  protected final List<Field> fixedVars;
  protected final String rScriptCommand;
  protected String indyVar;
  protected StringBuilder indyList;
  protected StringBuilder seriesList;
  protected StringBuilder seriesDiffList;
  protected List<Entry> models;
  protected StringBuilder diffVals;
  protected String fcstVar;
  protected List<String> diffSeries;
  protected StringBuilder fixVars;
  protected String stat;


  public RscriptManager(final Scorecard scorecard) {
    this.listColumns = scorecard.columnsStructure();
    fixedVars = scorecard.getFixedVars();
    rScriptCommand = scorecard.getrScriptCommand();
  }

  public abstract void calculateStatsForRow(Map<String, Entry> mapRow, String threadName);

  protected void initModels() {
    for (Field fixedField : fixedVars) {
      if ("model".equals(fixedField.getName())) {
        models = fixedField.getValues();
        break;
      }
    }
  }

  protected void init(Map<String, Entry> mapRow) {
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
    for (Map.Entry<String, List<Entry>> entry : listColumns.entrySet()) {
      if ("fcst_lead".equals(entry.getKey())) {
        seriesList.append("`").append(entry.getKey()).append("` = c(");
        for (Entry val : entry.getValue()) {
          if (seriesList.indexOf(val.getName()) == -1) {
            seriesList.append("\"").append(val.getName()).append("\",");
          }
          StringBuilder difStr = new StringBuilder("c(");
          for (Entry model : models) {
            difStr.append("\"");
            if (diffVals.length() > 0) {
              difStr.append(diffVals).append(" ");
            }
            difStr.append(model.getName()).append(" ").append(val.getName()).append(" ").append(fcstVar).append(" ").append(stat).append("\",");
          }

          difStr.append("\"DIFF_SIG\"").append("),");
          diffSeries.add(difStr.toString().trim());
        }
        if (seriesList.length() > 0) {
          seriesList.deleteCharAt(seriesList.length() - 1);
        }
        seriesList.append("),");
      } else {
        indyVar = entry.getKey();
        for (Entry val : entry.getValue()) {
          if (indyList.indexOf(val.getName()) == -1) {
            indyList.append("\"").append(val.getName()).append("\",");
          }
        }
        if (indyList.length() > 0) {
          indyList.deleteCharAt(indyList.length() - 1);
        }
      }
    }
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
  }

  protected void clean() {
    indyVar = "";
    indyList = new StringBuilder();
    seriesList = new StringBuilder("list(");
    seriesDiffList = new StringBuilder("list(");
    models = null;
    diffVals = new StringBuilder();
    fcstVar = null;
    diffSeries = new ArrayList<>();
    fixVars = new StringBuilder();
    stat = null;
  }

}

/**
 * Util.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author : tatiana $
 * @version : 1.0 : 20/01/17 14:56 $
 */
public class Util {

  private static final Logger logger = LogManager.getLogger("Util");
  private static final String[] aggTypes = new String[]{MVUtil.CTC, MVUtil.SL1L2, MVUtil.SAL1L2,
          MVUtil.VL1L2, MVUtil.VAL1L2, MVUtil.PCT, MVUtil.GRAD, MVUtil.NBRCNT, MVUtil.ECNT,
          MVUtil.NBRCTC, MVUtil.RPS};

  private Util() {

  }

  public static String getStatForRow(Map<String, Entry> mapRow) {
    //get stat for the current row
    String stat = null;
    for (Map.Entry<String, Entry> entry : mapRow.entrySet()) {
      if ("stat".equals(entry.getKey())) {
        stat = entry.getValue().getName();
        break;
      }
    }
    return stat;
  }

  public static String getAggTypeForStat(final String stat) {
    String aggType = "";

    Map<String, String[]> orderedMap;

    if (MVUtil.statsCnt.containsKey(stat)) {
      orderedMap = MVUtil.statsCnt;
    } else if (MVUtil.statsNbrcts.containsKey(stat)) {
      orderedMap = MVUtil.statsNbrcts;
    } else if (MVUtil.statsSsvar.containsKey(stat)) {
      orderedMap = MVUtil.statsSsvar;
    } else if (MVUtil.statsCts.containsKey(stat)) {
      orderedMap = MVUtil.statsCts;
    } else if (MVUtil.statsVl1l2.containsKey(stat)) {
      orderedMap = MVUtil.statsVl1l2;
    } else if (MVUtil.statsVal1l2.containsKey(stat)) {
      orderedMap = MVUtil.statsVal1l2;
    } else if (MVUtil.statsPstd.containsKey(stat)) {
      orderedMap = MVUtil.statsPstd;
    } else if (MVUtil.statsNbrcnt.containsKey(stat)) {
      orderedMap = MVUtil.statsNbrcnt;
    } else if (MVUtil.statsEcnt.containsKey(stat)) {
      orderedMap = MVUtil.statsEcnt;
    } else if (MVUtil.statsMctc.containsKey(stat)) {
      orderedMap = MVUtil.statsMctc;
    } else {
      orderedMap = new HashMap<>();
    }

    String[] entry = orderedMap.get(stat);

    for (String st : entry) {
      if (Arrays.asList(aggTypes).contains(st.toLowerCase())) {
        aggType = st;
        break;
      }
    }

    return aggType;
  }

  public static List<Map<String, Integer>> getRowspansForRowHeader(List<Map<String, Entry>> listRows) {
    List<Map<String, Integer>> fieldToCountMap = new ArrayList<>(listRows.size());
    List<List<Entry>> allCombinationsOfValues = new ArrayList<>();
    //for each row
    for (Map<String, Entry> row : listRows) {

      Map<String, Integer> rowFieldToCount = new LinkedHashMap<>(row.size());
      List<String> fieldsFromRow = new ArrayList<>(row.keySet().size());

      //for each field from this row
      for (String fieldName : row.keySet()) {

        fieldsFromRow.add(fieldName);
        List<Map<String, Entry>> copyOfAllOriginalRows = new ArrayList<>(listRows);
        List<Entry> valueCombination = new ArrayList<>();

        //find all rows that have current field and value
        for (String fieldFromRow : fieldsFromRow) {
          //get fields value
          Entry fieldValue = row.get(fieldFromRow);
          valueCombination.add(fieldValue);
          //remove rows that don't have this field value
          for (Map<String, Entry> aRow : listRows) {
            if (aRow.containsKey(fieldFromRow) && !aRow.get(fieldFromRow).equals(fieldValue)) {
              copyOfAllOriginalRows.remove(aRow);
            }
          }
        }

        //copyOfAllOriginalRows contains only rows with the unique combination
        if (!allCombinationsOfValues.contains(valueCombination)) {
          allCombinationsOfValues.add(valueCombination);
          rowFieldToCount.put(fieldName, copyOfAllOriginalRows.size());
        } else {
          rowFieldToCount.put(fieldName, 0);
        }

      }
      fieldToCountMap.add(rowFieldToCount);
    }
    return fieldToCountMap;
  }
}

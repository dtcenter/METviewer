/**
 * Util.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard;

import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.model.Entry;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 20/01/17 14:56 $
 */
public class Util {

  private static final Logger logger = Logger.getLogger(Util.class);
  private static final String[] aggTypes = new String[]{MVUtil.CTC, MVUtil.SL1L2, MVUtil.SAL1L2, MVUtil.VL1L2};

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
    String aggType = null;

    MVOrderedMap orderedMap;

    if (MVUtil._tableStatsCnt.containsKey(stat)) {
      orderedMap = MVUtil._tableStatsCnt;
    } else if (MVUtil._tableStatsSsvar.containsKey(stat)) {
      orderedMap = MVUtil._tableStatsSsvar;
    } else if (MVUtil._tableStatsCts.containsKey(stat)) {
      orderedMap = MVUtil._tableStatsCts;
    } else if (MVUtil._tableStatsVl1l2.containsKey(stat)) {
      orderedMap = MVUtil._tableStatsVl1l2;
    } else {
      orderedMap = new MVOrderedMap();
    }
    try {
      String[] entry = (String[]) orderedMap.get(stat);
      for (String st : entry) {
        if (Arrays.asList(aggTypes).contains(st.toLowerCase())) {
          aggType = st;
          break;
        }
      }
    } catch (NullPointerException e) {
      logger.error(e);
      aggType = "";
    }
    return aggType;
  }
}

/**
 * AggDatabaseManagerMySQL.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard.db;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.Scorecard;
import org.apache.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:20 $
 */
public class AggDatabaseManagerMySQL extends DatabaseManagerMySQL {

  private static final Logger logger = Logger.getLogger(AggDatabaseManagerMySQL.class);


  public AggDatabaseManagerMySQL(final Scorecard scorecard) {
    super(scorecard);
    aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile();

  }

  @Override
  protected String getSelectFields(String table) {
    String result = "";
    if (table.endsWith(MVUtil.CTC)) {
      result = "total, fy_oy,fy_on,fn_oy,fn_on";
    } else if (table.endsWith(MVUtil.SL1L2)) {
      result = "total,fbar,obar,fobar,ffbar,oobar,mae";
    } else if (table.endsWith(MVUtil.SAL1L2)) {
      result = "total,fabar,oabar,foabar,ffabar,ooabar,mae";
    }else if (table.endsWith(MVUtil.VL1L2)){
      result = "total,ufbar,vfbar,uobar,vobar,uvfobar,uvffbar,uvoobar";
    }
    return result;
  }

  @Override
  protected String getStatValue(String table, String stat) {
    return "0";
  }

}

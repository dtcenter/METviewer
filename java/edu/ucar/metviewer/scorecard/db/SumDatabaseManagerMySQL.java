/**
 * SumDatabaseManagerMySQL.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.db;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.scorecard.Scorecard;

import java.sql.SQLException;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 11:38 $
 */
public class SumDatabaseManagerMySQL extends DatabaseManagerMySQL {

  public SumDatabaseManagerMySQL(Scorecard scorecard) throws SQLException {
    super(scorecard);
    aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard.getDataFile().replaceAll(".data", ".dataFromDb");

  }

  @Override
  protected String getSelectFields(String table) {
    return "'NA' stat_ncl,'NA' stat_ncu,'NA' stat_bcl,'NA' stat_bcu";
  }

  @Override
  protected String getStatValue(String table, String stat) {
    String result = "";
    if (table.endsWith(MVUtil.CTC)) {
      result = "calc" + stat + "(total, fy_oy, fy_on, fn_oy, fn_on)";
    } else if (table.endsWith(MVUtil.SL1L2)) {
      if (stat.equalsIgnoreCase("mae")) {
        result = "calc" + stat + "( mae) ";
      } else {
        result = "calc" + stat + "(total, fbar, obar, fobar, ffbar, oobar)";
      }
    } else if (table.endsWith(MVUtil.SAL1L2)) {
      result = "calc" + stat + "(total, fabar, oabar, foabar, ffabar, ooabar)";
    } else if (table.endsWith(MVUtil.VL1L2)) {
      result = "calc" + stat + "(total, ufbar, vfbar, uobar, vobar, uvfobar, uvffbar, uvoobar)";
    }


    return result;
  }

}

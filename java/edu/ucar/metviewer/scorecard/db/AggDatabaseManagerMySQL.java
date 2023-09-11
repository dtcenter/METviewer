/**
 * AggDatabaseManagerMySQL.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard.db;

import java.sql.SQLException;

import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.db.mysql.MysqlDatabaseManager;
import edu.ucar.metviewer.scorecard.Scorecard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 15:20 $
 */
public class AggDatabaseManagerMySQL extends DatabaseManagerSql {

  private static final Logger logger = LogManager.getLogger("AggDatabaseManagerMySQL");


  public AggDatabaseManagerMySQL(final Scorecard scorecard, final MysqlDatabaseManager databaseManager){
    super(scorecard, databaseManager);
    aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile();

  }

  @Override
  protected String getSelectFields(String table, Integer thresh) {
    StringBuilder result = new StringBuilder();
    if (table.endsWith(MVUtil.CTC)) {
      result = new StringBuilder("total, fy_oy,fy_on,fn_oy,fn_on");
    } else if (table.endsWith(MVUtil.SL1L2)) {
      result = new StringBuilder("total,fbar,obar,fobar,ffbar,oobar,mae");
    } else if (table.endsWith(MVUtil.GRAD)) {
      result = new StringBuilder("total,fgbar,ogbar,mgbar,egbar");
    } else if (table.endsWith(MVUtil.SAL1L2)) {
      result = new StringBuilder("total,fabar,oabar,foabar,ffabar,ooabar,mae");
    } else if (table.endsWith(MVUtil.VL1L2)) {
      result = new StringBuilder("total,ufbar,vfbar,uobar,vobar,uvfobar,uvffbar,uvoobar");
    } else if (table.endsWith(MVUtil.VAL1L2)) {
      result = new StringBuilder("total,ufabar,vfabar,uoabar,voabar,uvfoabar,uvffabar,uvooabar");
    } else if (table.endsWith(MVUtil.ECNT)) {
      result = new StringBuilder("total,me,rmse,crps,crpss,ign,spread,me_oerr,rmse_oerr,spread_oerr,spread_plus_oerr,crpscl,crps_emp,crpscl_emp ,crpss_emp");
    } else if (table.endsWith(MVUtil.RPS)) {
      result = new StringBuilder("total,rps,rpss, rps_comp");
    } else if (table.endsWith("nbrcnt")) {
      result = new StringBuilder("total,fbs,fss");
    } else if (table.endsWith(MVUtil.PCT)) {
      result = new StringBuilder("total,(line_data_pct.n_thresh - 1)");
      for (int i = 1; i < thresh; i++) {
        result.append(",");
        if (i < thresh - 1) {
          result.append("  FORMAT((ldt").append(i).append(".thresh_i + ldt").append(i + 1).append(".thresh_i)/2, 3),");
        } else {
          result.append("  FORMAT((ldt").append(i).append(".thresh_i + 1)/2, 3),");
        }
        result.append("  ldt").append(i).append(".oy_i,").append("  ldt").append(i).append(".on_i");
      }
    }
    return result.toString();
  }

  @Override
  protected String getStatValue(String table, String stat) {
    return "";
  }

}

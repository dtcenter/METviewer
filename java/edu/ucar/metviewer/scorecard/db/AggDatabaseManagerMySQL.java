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


    public AggDatabaseManagerMySQL(final Scorecard scorecard,
                                   final MysqlDatabaseManager databaseManager) throws SQLException {
        super(scorecard, databaseManager);
        aggStatDataFilePath = scorecard.getWorkingFolders().getDataDir() + scorecard.getAggStatDataFile();

    }

    @Override
    protected String getSelectFields(String table, Integer thresh) {
        String result = "";
        if (table.endsWith(MVUtil.CTC)) {
            result = "total, fy_oy,fy_on,fn_oy,fn_on";
        } else if (table.endsWith(MVUtil.SL1L2)) {
            result = "total,fbar,obar,fobar,ffbar,oobar,mae";
        } else if (table.endsWith(MVUtil.GRAD)) {
            result = "total,fgbar,ogbar,mgbar,egbar";
        } else if (table.endsWith(MVUtil.SAL1L2)) {
            result = "total,fabar,oabar,foabar,ffabar,ooabar,mae";
        } else if (table.endsWith(MVUtil.VL1L2)) {
            result = "total,ufbar,vfbar,uobar,vobar,uvfobar,uvffbar,uvoobar, f_speed_bar, o_speed_bar, total_dir, dir_me, dir_mae, dir_mse";
        } else if (table.endsWith(MVUtil.VAL1L2)) {
            result = "total,ufabar,vfabar,uoabar,voabar,uvfoabar,uvffabar,uvooabar, fa_speed_bar, oa_speed_bar, total_dir, dira_me, dira_mae, dira_mse";
        } else if (table.endsWith(MVUtil.ECNT)) {
          result = "total,me,rmse,crps,crpss,ign,spread,me_oerr,rmse_oerr,spread_oerr,spread_plus_oerr,crpscl," +
                  "crps_emp,crpscl_emp,crpss_emp,crpss_emp_fair,spread_md,mae,mae_oerr,bias_ratio,n_ge_obs, " +
                  "me_ge_obs,n_lt_obs,me_lt_obs,ign_conv_oerr,ign_corr_oerr";
        } else if (table.endsWith(MVUtil.RPS)) {
            result = "total,rps,rpss, rps_comp";
        } else if (table.endsWith("nbrcnt")) {
            result = "total,fbs,fss";
        } else if (table.endsWith(MVUtil.PCT)) {
            result = "total,(line_data_pct.n_thresh - 1)";
            for (int i = 1; i < thresh; i++) {
                result += ",";
                if (i < thresh - 1) {
                    result += "  FORMAT((ldt" + i + ".thresh_i + ldt" + (i + 1) + ".thresh_i)/2, 3),";
                } else {
                    result += "  FORMAT((ldt" + i + ".thresh_i + 1)/2, 3),";
                }
                result += "  ldt" + i + ".oy_i," +
                        "  ldt" + i + ".on_i";
            }
        }
        return result;
    }

    @Override
    protected String getStatValue(String table, String stat) {
        return "";
    }

}

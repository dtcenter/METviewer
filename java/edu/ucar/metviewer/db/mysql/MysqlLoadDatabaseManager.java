/**
 * MysqlLoadDatabaseManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db.mysql;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucar.metviewer.BoundedBufferedReader;
import edu.ucar.metviewer.DataFileInfo;
import edu.ucar.metviewer.DatabaseException;
import edu.ucar.metviewer.MVLoadJob;
import edu.ucar.metviewer.MVLoadStatInsertData;
import edu.ucar.metviewer.MVUtil;
import edu.ucar.metviewer.StopWatch;
import edu.ucar.metviewer.StopWatchException;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.LoadDatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:19 $
 */
public class MysqlLoadDatabaseManager extends MysqlDatabaseManager implements LoadDatabaseManager {

  protected static final DateTimeFormatter DB_DATE_STAT_FORMAT
          = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
  private static final Logger logger = LogManager.getLogger("MysqlLoadDatabaseManager");
  private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

  private static final int INDEX_LINE_DATA = 1;
  private static final int INDEX_VAR_LENGTH = 3;
  private static final double[] X_POINTS_FOR_ECON = new double[]{
          0.952380952, 0.909090909, 0.800000000, 0.666666667, 0.500000000, 0.333333333,
          0.200000000, 0.125000000, 0.100000000, 0.055555556, 0.037037037, 0.025000000,
          0.016666667, 0.011111111, 0.007142857, 0.004761905, 0.002857143, 0.002000000
  };
  private static final int MAX_LINES = 60000;
  private static final int MAX_LINE_LEN = 2000;
  private final Map<String, Integer> tableVarLengthLineDataId = new HashMap<>();
  private final Map<String, Integer> statHeaders = new HashMap<>();
  private final Map<String, Integer> modeHeaders = new HashMap<>();
  private final Map<String, Integer> mtdHeaders = new HashMap<>();
  private final String[] modeObjSingleColumns = new String[]{
          "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT", "CENTROID_LON", "AXIS_ANG",
          "LENGTH", "WIDTH", "AREA", "AREA_THRESH", "CURVATURE", "CURVATURE_X", "CURVATURE_Y",
          "COMPLEXITY", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75", "INTENSITY_90",
          "INTENSITY_50", "INTENSITY_SUM"};

  private final String[] mtdObj2dColumns = new String[]{
          "OBJECT_CAT", "TIME_INDEX", "AREA", "CENTROID_X", "CENTROID_Y", "CENTROID_LAT",
          "CENTROID_LON", "AXIS_ANG", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75", "INTENSITY_90"
  };
  private final String[] mtdObj3dSingleColumns = new String[]{
          "OBJECT_CAT", "CENTROID_X", "CENTROID_Y", "CENTROID_T", "CENTROID_LAT",
          "CENTROID_LON", "X_DOT", "Y_DOT ", "AXIS_ANG", "VOLUME", "START_TIME", "END_TIME",
          "CDIST_TRAVELLED", "INTENSITY_10", "INTENSITY_25", "INTENSITY_50", "INTENSITY_75",
          "INTENSITY_90"
  };
  private final String[] listLineDataTables = {
          "line_data_fho", "line_data_ctc", "line_data_cts", "line_data_cnt", "line_data_pct",
          "line_data_pstd", "line_data_pjc", "line_data_prc", "line_data_sl1l2", "line_data_sal1l2",
          "line_data_vl1l2", "line_data_val1l2", "line_data_mpr", "line_data_nbrctc", "line_data_nbrcts",
          "line_data_nbrcnt", "line_data_isc", "line_data_mctc", "line_data_rhist", "line_data_orank",
          "line_data_relp", "line_data_eclv",
          "line_data_ssvar", "line_data_enscnt", "line_data_grad"
  };
  /*
   * variable length table names for each variable length output line type
   */
  private final Map<String, String> tableVarLengthTable;
  /*
   * data_file_lu_id values for each MET output type
   */
  private final Map<String, Integer> tableDataFileLU;
  private final String[] dropIndexesQueries;
  private final String[] createIndexesQueries;
  private final Map<String, String> tableToInsert;

  public MysqlLoadDatabaseManager(
          DatabaseInfo databaseInfo, String password) throws DatabaseException {
    super(databaseInfo, password);

    tableToInsert = new HashMap<>();
    tableToInsert.put("line_data_fho", "INSERT INTO line_data_fho VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?)");//14
    tableToInsert.put("line_data_ctc", "INSERT INTO line_data_ctc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?)");//15
    tableToInsert.put("line_data_cts",
            "INSERT INTO line_data_cts VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//104
    tableToInsert.put("line_data_cnt", "INSERT INTO line_data_cnt VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//105
    tableToInsert
            .put("line_data_ecnt", "INSERT INTO line_data_ecnt VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?)");//22
    tableToInsert
            .put("line_data_mctc", "INSERT INTO line_data_mctc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?)");//13
    tableToInsert.put("line_data_mctc_cnt", "INSERT INTO line_data_mctc_cnt VALUES (?,?,?,?)");//4
    tableToInsert
            .put("line_data_mcts", "INSERT INTO line_data_mcts VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//27
    tableToInsert.put("line_data_pct", "INSERT INTO line_data_pct VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?)");//14
    tableToInsert
            .put("line_data_pct_thresh", "INSERT INTO line_data_pct_thresh VALUES (?,?,?,?,?)");//5
    tableToInsert
            .put("line_data_pstd", "INSERT INTO line_data_pstd VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//30
    tableToInsert
            .put("line_data_pstd_thresh", "INSERT INTO line_data_pstd_thresh VALUES (?,?,?)");//3
    tableToInsert.put("line_data_pjc", "INSERT INTO line_data_pjc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?)");//14
    tableToInsert.put("line_data_pjc_thresh", "INSERT INTO line_data_pjc_thresh VALUES (?,?,?,?,?,"
            + "?,?,?,?)");//9
    tableToInsert.put("line_data_prc", "INSERT INTO line_data_prc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?)");//14
    tableToInsert
            .put("line_data_prc_thresh", "INSERT INTO line_data_prc_thresh VALUES (?,?,?,?,?)");//5
    tableToInsert
            .put("line_data_sl1l2", "INSERT INTO line_data_sl1l2 VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?)");//17
    tableToInsert
            .put("line_data_grad", "INSERT INTO line_data_grad VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//20
    tableToInsert
            .put("line_data_sal1l2", "INSERT INTO line_data_sal1l2 VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?)");//17
    tableToInsert
            .put("line_data_vl1l2", "INSERT INTO line_data_vl1l2 VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?)");//20
    tableToInsert
            .put("line_data_val1l2", "INSERT INTO line_data_val1l2 VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?)");//18
    tableToInsert.put("line_data_mpr", "INSERT INTO line_data_mpr VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,?,?)");//24

    tableToInsert
            .put("line_data_nbrctc", "INSERT INTO line_data_nbrctc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?)");//16
    tableToInsert
            .put("line_data_nbrcts", "INSERT INTO line_data_nbrcts VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//105
    tableToInsert
            .put("line_data_nbrcnt", "INSERT INTO line_data_nbrcnt VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//30
    tableToInsert
            .put("line_data_enscnt", "INSERT INTO line_data_enscnt VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?)");//40
    tableToInsert.put("line_data_isc", "INSERT INTO line_data_isc VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?)");//22
    tableToInsert
            .put("line_data_rhist", "INSERT INTO line_data_rhist VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?)");//13
    tableToInsert.put("line_data_rhist_rank", "INSERT INTO line_data_rhist_rank VALUES (?,?,?)");//3
    tableToInsert
            .put("line_data_relp", "INSERT INTO line_data_relp VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?)");//13
    tableToInsert.put("line_data_relp_ens", "INSERT INTO line_data_relp_ens VALUES (?,?,?)");//3
    tableToInsert
            .put("line_data_eclv", "INSERT INTO line_data_eclv VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?)");//15
    tableToInsert.put("line_data_eclv_pnt", "INSERT INTO line_data_eclv_pnt VALUES (?,?,?,?)");//4
    tableToInsert
            .put("line_data_phist", "INSERT INTO line_data_phist VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?)");//14
    tableToInsert.put("line_data_phist_bin", "INSERT INTO line_data_phist_bin VALUES (?,?,?)");//3
    tableToInsert
            .put("line_data_orank", "INSERT INTO line_data_orank VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//30
    tableToInsert.put("line_data_orank_ens", "INSERT INTO line_data_orank_ens VALUES (?,?,?)");//3
    tableToInsert
            .put("line_data_ssvar", "INSERT INTO line_data_ssvar VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//46
    tableToInsert
            .put("line_data_vcnt", "INSERT INTO line_data_vcnt VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//66

    tableToInsert
            .put("line_data_perc", "INSERT INTO line_data_perc VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");//12

    tableToInsert.put("mode_cts", "INSERT INTO mode_cts VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?)");//20

    tableToInsert
            .put("mode_obj_single", "INSERT INTO mode_obj_single VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");//27

    tableToInsert.put("mode_obj_pair", "INSERT INTO mode_obj_pair VALUES (?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?)");//21

    dropIndexesQueries = new String[]{
            "DROP INDEX stat_header_model_idx ON stat_header",
            "DROP INDEX stat_header_fcst_var_idx ON stat_header",
            "DROP INDEX stat_header_fcst_lev_idx ON stat_header",
            "DROP INDEX stat_header_obtype_idx ON stat_header",
            "DROP INDEX stat_header_vx_mask_idx ON stat_header",
            "DROP INDEX stat_header_interp_mthd_idx ON stat_header",
            "DROP INDEX stat_header_interp_pnts_idx ON stat_header",
            "DROP INDEX stat_header_fcst_thresh_idx ON stat_header",
            "DROP INDEX mode_header_model_idx ON mode_header",
            "DROP INDEX mode_header_fcst_lead_idx ON mode_header",
            "DROP INDEX mode_header_fcst_valid_idx ON mode_header",
            "DROP INDEX mode_header_fcst_init_idx ON mode_header",
            "DROP INDEX mode_header_fcst_rad_idx ON mode_header",
            "DROP INDEX mode_header_fcst_thr_idx ON mode_header",
            "DROP INDEX mode_header_fcst_var_idx ON mode_header",
            "DROP INDEX mode_header_fcst_lev_idx ON mode_header",
            "DROP INDEX mtd_header_model_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_lead_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_valid_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_init_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_rad_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_thr_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_var_idx ON mtd_header",
            "DROP INDEX mtd_header_fcst_lev_idx ON mtd_header",
            "DROP INDEX line_data_fho_fcst_lead_idx ON line_data_fho",
            "DROP INDEX line_data_fho_fcst_valid_beg_idx ON line_data_fho",
            "DROP INDEX line_data_fho_fcst_init_beg_idx ON line_data_fho",
            "DROP INDEX line_data_ctc_fcst_lead_idx ON line_data_ctc",
            "DROP INDEX line_data_ctc_fcst_valid_beg_idx ON line_data_ctc",
            "DROP INDEX line_data_ctc_fcst_init_beg_idx ON line_data_ctc",
            "DROP INDEX line_data_cts_fcst_lead_idx ON line_data_cts",
            "DROP INDEX line_data_cts_fcst_valid_beg_idx ON line_data_cts",
            "DROP INDEX line_data_cts_fcst_init_beg_idx ON line_data_cts",
            "DROP INDEX line_data_cnt_fcst_lead_idx ON line_data_cnt",
            "DROP INDEX line_data_cnt_fcst_valid_beg_idx ON line_data_cnt",
            "DROP INDEX line_data_cnt_fcst_init_beg_idx ON line_data_cnt",
            "DROP INDEX line_data_pct_fcst_lead_idx ON line_data_pct",
            "DROP INDEX line_data_pct_fcst_valid_beg_idx ON line_data_pct",
            "DROP INDEX line_data_pct_fcst_init_beg_idx ON line_data_pct",
            "DROP INDEX line_data_pstd_fcst_lead_idx ON line_data_pstd",
            "DROP INDEX line_data_pstd_fcst_valid_beg_idx ON line_data_pstd",
            "DROP INDEX line_data_pstd_fcst_init_beg_idx ON line_data_pstd",
            "DROP INDEX line_data_pjc_fcst_lead_idx ON line_data_pjc",
            "DROP INDEX line_data_pjc_fcst_valid_beg_idx ON line_data_pjc",
            "DROP INDEX line_data_pjc_fcst_init_beg_idx ON line_data_pjc",
            "DROP INDEX line_data_prc_fcst_lead_idx ON line_data_prc",
            "DROP INDEX line_data_prc_fcst_valid_beg_idx ON line_data_prc",
            "DROP INDEX line_data_prc_fcst_init_beg_idx ON line_data_prc",
            "DROP INDEX line_data_sl1l2_fcst_lead_idx ON line_data_sl1l2",
            "DROP INDEX line_data_sl1l2_fcst_valid_beg_idx ON line_data_sl1l2",
            "DROP INDEX line_data_sl1l2_fcst_init_beg_idx ON line_data_sl1l2",
            "DROP INDEX line_data_sal1l2_fcst_lead_idx ON line_data_sal1l2",
            "DROP INDEX line_data_sal1l2_fcst_valid_beg_idx ON line_data_sal1l2",
            "DROP INDEX line_data_sal1l2_fcst_init_beg_idx ON line_data_sal1l2",
            "DROP INDEX line_data_vl1l2_fcst_lead_idx ON line_data_vl1l2",
            "DROP INDEX line_data_vl1l2_fcst_valid_beg_idx ON line_data_vl1l2",
            "DROP INDEX line_data_vl1l2_fcst_init_beg_idx ON line_data_vl1l2",
            "DROP INDEX line_data_val1l2_fcst_lead_idx ON line_data_val1l2",
            "DROP INDEX line_data_val1l2_fcst_valid_beg_idx ON line_data_val1l2",
            "DROP INDEX line_data_val1l2_fcst_init_beg_idx ON line_data_val1l2",
            "DROP INDEX line_data_mpr_fcst_lead_idx ON line_data_mpr",
            "DROP INDEX line_data_mpr_fcst_valid_beg_idx ON line_data_mpr",
            "DROP INDEX line_data_mpr_fcst_init_beg_idx ON line_data_mpr",
            "DROP INDEX line_data_nbrctc_fcst_lead_idx ON line_data_nbrctc",
            "DROP INDEX line_data_nbrctc_fcst_valid_beg_idx ON line_data_nbrctc",
            "DROP INDEX line_data_nbrctc_fcst_init_beg_idx ON line_data_nbrctc",
            "DROP INDEX line_data_nbrcts_fcst_lead_idx ON line_data_nbrcts",
            "DROP INDEX line_data_nbrcts_fcst_valid_beg_idx ON line_data_nbrcts",
            "DROP INDEX line_data_nbrcts_fcst_init_beg_idx ON line_data_nbrcts",
            "DROP INDEX line_data_nbrcnt_fcst_lead_idx ON line_data_nbrcnt",
            "DROP INDEX line_data_nbrcnt_fcst_valid_beg_idx ON line_data_nbrcnt",
            "DROP INDEX line_data_nbrcnt_fcst_init_beg_idx ON line_data_nbrcnt",
            "DROP INDEX line_data_isc_fcst_lead_idx ON line_data_isc",
            "DROP INDEX line_data_isc_fcst_valid_beg_idx ON line_data_isc",
            "DROP INDEX line_data_isc_fcst_init_beg_idx ON line_data_isc",
            "DROP INDEX line_data_mctc_fcst_lead_idx ON line_data_mctc",
            "DROP INDEX line_data_mctc_fcst_valid_beg_idx ON line_data_mctc",
            "DROP INDEX line_data_mctc_fcst_init_beg_idx ON line_data_mctc",
            "DROP INDEX line_data_rhist_fcst_lead_idx ON line_data_rhist",
            "DROP INDEX line_data_rhist_fcst_valid_beg_idx ON line_data_rhist",
            "DROP INDEX line_data_rhist_fcst_init_beg_idx ON line_data_rhist",
            "DROP INDEX line_data_orank_fcst_lead_idx ON line_data_orank",
            "DROP INDEX line_data_orank_fcst_valid_beg_idx ON line_data_orank",
            "DROP INDEX line_data_orank_fcst_init_beg_idx ON line_data_orank",
            "DROP INDEX line_data_relp_fcst_lead_idx ON line_data_relp",
            "DROP INDEX line_data_relp_fcst_valid_beg_idx ON line_data_relp",
            "DROP INDEX line_data_relp_fcst_init_beg_idx ON line_data_relp",
            "DROP INDEX line_data_eclv_fcst_lead_idx ON line_data_eclv",
            "DROP INDEX line_data_eclv_fcst_valid_beg_idx ON line_data_eclv",
            "DROP INDEX line_data_eclv_fcst_init_beg_idx ON line_data_eclv",
            "DROP INDEX line_data_ssvar_fcst_lead_idx ON line_data_ssvar",
            "DROP INDEX line_data_ssvar_fcst_valid_beg_idx ON line_data_ssvar",
            "DROP INDEX line_data_ssvar_fcst_init_beg_idx ON line_data_ssvar",
            "DROP INDEX line_data_enscnt_fcst_lead_idx ON line_data_enscnt",
            "DROP INDEX line_data_enscnt_fcst_valid_beg_idx ON line_data_enscnt",
            "DROP INDEX line_data_enscnt_fcst_init_beg_idx ON line_data_enscnt",
            "DROP INDEX line_data_grad_fcst_lead_idx ON line_data_grad",
            "DROP INDEX line_data_grad_fcst_valid_beg_idx ON line_data_grad",
            "DROP INDEX line_data_grad_fcst_init_beg_idx ON line_data_grad"
    };

    createIndexesQueries = new String[]{
            "CREATE INDEX stat_header_model_idx ON stat_header (model)",
            "CREATE INDEX stat_header_fcst_var_idx ON stat_header (fcst_var)",
            "CREATE INDEX stat_header_fcst_lev_idx ON stat_header (fcst_lev)",
            "CREATE INDEX stat_header_obtype_idx ON stat_header (obtype)",
            "CREATE INDEX stat_header_vx_mask_idx ON stat_header (vx_mask)",
            "CREATE INDEX stat_header_interp_mthd_idx ON stat_header (interp_mthd)",
            "CREATE INDEX stat_header_interp_pnts_idx ON stat_header (interp_pnts)",
            "CREATE INDEX stat_header_fcst_thresh_idx ON stat_header (fcst_thresh)",
            "CREATE INDEX mode_header_model_idx ON mode_header (model)",
            "CREATE INDEX mode_header_fcst_lead_idx ON mode_header (fcst_lead)",
            "CREATE INDEX mode_header_fcst_valid_idx ON mode_header (fcst_valid)",
            "CREATE INDEX mode_header_fcst_init_idx ON mode_header (fcst_init)",
            "CREATE INDEX mode_header_fcst_rad_idx ON mode_header (fcst_rad)",
            "CREATE INDEX mode_header_fcst_thr_idx ON mode_header (fcst_thr)",
            "CREATE INDEX mode_header_fcst_var_idx ON mode_header (fcst_var)",
            "CREATE INDEX mode_header_fcst_lev_idx ON mode_header (fcst_lev)",
            "CREATE INDEX mtd_header_model_idx ON mtd_header (model)",
            "CREATE INDEX mtd_header_fcst_lead_idx ON mtd_header (fcst_lead)",
            "CREATE INDEX mtd_header_fcst_valid_idx ON mtd_header (fcst_valid)",
            "CREATE INDEX mtd_header_fcst_init_idx ON mtd_header (fcst_init)",
            "CREATE INDEX mtd_header_fcst_rad_idx ON mtd_header (fcst_rad)",
            "CREATE INDEX mtd_header_fcst_thr_idx ON mtd_header (fcst_thr)",
            "CREATE INDEX mtd_header_fcst_var_idx ON mtd_header (fcst_var)",
            "CREATE INDEX mtd_header_fcst_lev_idx ON mtd_header (fcst_lev)",
            "CREATE INDEX line_data_fho_fcst_lead_idx ON line_data_fho (fcst_lead)",
            "CREATE INDEX line_data_fho_fcst_valid_beg_idx ON line_data_fho (fcst_valid_beg)",
            "CREATE INDEX line_data_fho_fcst_init_beg_idx ON line_data_fho (fcst_init_beg)",
            "CREATE INDEX line_data_ctc_fcst_lead_idx ON line_data_ctc (fcst_lead)",
            "CREATE INDEX line_data_ctc_fcst_valid_beg_idx ON line_data_ctc (fcst_valid_beg)",
            "CREATE INDEX line_data_ctc_fcst_init_beg_idx ON line_data_ctc (fcst_init_beg)",
            "CREATE INDEX line_data_cts_fcst_lead_idx ON line_data_cts (fcst_lead)",
            "CREATE INDEX line_data_cts_fcst_valid_beg_idx ON line_data_cts (fcst_valid_beg)",
            "CREATE INDEX line_data_cts_fcst_init_beg_idx ON line_data_cts (fcst_init_beg)",
            "CREATE INDEX line_data_cnt_fcst_lead_idx ON line_data_cnt (fcst_lead)",
            "CREATE INDEX line_data_cnt_fcst_valid_beg_idx ON line_data_cnt (fcst_valid_beg)",
            "CREATE INDEX line_data_cnt_fcst_init_beg_idx ON line_data_cnt (fcst_init_beg)",
            "CREATE INDEX line_data_pct_fcst_lead_idx ON line_data_pct (fcst_lead)",
            "CREATE INDEX line_data_pct_fcst_valid_beg_idx ON line_data_pct (fcst_valid_beg)",
            "CREATE INDEX line_data_pct_fcst_init_beg_idx ON line_data_pct (fcst_init_beg)",
            "CREATE INDEX line_data_pstd_fcst_lead_idx ON line_data_pstd (fcst_lead)",
            "CREATE INDEX line_data_pstd_fcst_valid_beg_idx ON line_data_pstd (fcst_valid_beg)",
            "CREATE INDEX line_data_pstd_fcst_init_beg_idx ON line_data_pstd (fcst_init_beg)",
            "CREATE INDEX line_data_pjc_fcst_lead_idx ON line_data_pjc (fcst_lead)",
            "CREATE INDEX line_data_pjc_fcst_valid_beg_idx ON line_data_pjc (fcst_valid_beg)",
            "CREATE INDEX line_data_pjc_fcst_init_beg_idx ON line_data_pjc (fcst_init_beg)",
            "CREATE INDEX line_data_prc_fcst_lead_idx ON line_data_prc (fcst_lead)",
            "CREATE INDEX line_data_prc_fcst_valid_beg_idx ON line_data_prc (fcst_valid_beg)",
            "CREATE INDEX line_data_prc_fcst_init_beg_idx ON line_data_prc (fcst_init_beg)",
            "CREATE INDEX line_data_sl1l2_fcst_lead_idx ON line_data_sl1l2 (fcst_lead)",
            "CREATE INDEX line_data_sl1l2_fcst_valid_beg_idx ON line_data_sl1l2 (fcst_valid_beg)",
            "CREATE INDEX line_data_sl1l2_fcst_init_beg_idx ON line_data_sl1l2 (fcst_init_beg)",
            "CREATE INDEX line_data_sal1l2_fcst_lead_idx ON line_data_sal1l2 (fcst_lead)",
            "CREATE INDEX line_data_sal1l2_fcst_valid_beg_idx ON line_data_sal1l2 (fcst_valid_beg)",
            "CREATE INDEX line_data_sal1l2_fcst_init_beg_idx ON line_data_sal1l2 (fcst_init_beg)",
            "CREATE INDEX line_data_vl1l2_fcst_lead_idx ON line_data_vl1l2 (fcst_lead)",
            "CREATE INDEX line_data_vl1l2_fcst_valid_beg_idx ON line_data_vl1l2 (fcst_valid_beg)",
            "CREATE INDEX line_data_vl1l2_fcst_init_beg_idx ON line_data_vl1l2 (fcst_init_beg)",
            "CREATE INDEX line_data_val1l2_fcst_lead_idx ON line_data_val1l2 (fcst_lead)",
            "CREATE INDEX line_data_val1l2_fcst_valid_beg_idx ON line_data_val1l2 (fcst_valid_beg)",
            "CREATE INDEX line_data_val1l2_fcst_init_beg_idx ON line_data_val1l2 (fcst_init_beg)",
            "CREATE INDEX line_data_mpr_fcst_lead_idx ON line_data_mpr (fcst_lead)",
            "CREATE INDEX line_data_mpr_fcst_valid_beg_idx ON line_data_mpr (fcst_valid_beg)",
            "CREATE INDEX line_data_mpr_fcst_init_beg_idx ON line_data_mpr (fcst_init_beg)",
            "CREATE INDEX line_data_nbrctc_fcst_lead_idx ON line_data_nbrctc (fcst_lead)",
            "CREATE INDEX line_data_nbrctc_fcst_valid_beg_idx ON line_data_nbrctc (fcst_valid_beg)",
            "CREATE INDEX line_data_nbrctc_fcst_init_beg_idx ON line_data_nbrctc (fcst_init_beg)",
            "CREATE INDEX line_data_nbrcts_fcst_lead_idx ON line_data_nbrcts (fcst_lead)",
            "CREATE INDEX line_data_nbrcts_fcst_valid_beg_idx ON line_data_nbrcts (fcst_valid_beg)",
            "CREATE INDEX line_data_nbrcts_fcst_init_beg_idx ON line_data_nbrcts (fcst_init_beg)",
            "CREATE INDEX line_data_nbrcnt_fcst_lead_idx ON line_data_nbrcnt (fcst_lead)",
            "CREATE INDEX line_data_nbrcnt_fcst_valid_beg_idx ON line_data_nbrcnt (fcst_valid_beg)",
            "CREATE INDEX line_data_nbrcnt_fcst_init_beg_idx ON line_data_nbrcnt (fcst_init_beg)",
            "CREATE INDEX line_data_isc_fcst_lead_idx ON line_data_isc (fcst_lead)",
            "CREATE INDEX line_data_isc_fcst_valid_beg_idx ON line_data_isc (fcst_valid_beg)",
            "CREATE INDEX line_data_isc_fcst_init_beg_idx ON line_data_isc (fcst_init_beg)",
            "CREATE INDEX line_data_mctc_fcst_lead_idx ON line_data_mctc (fcst_lead)",
            "CREATE INDEX line_data_mctc_fcst_valid_beg_idx ON line_data_mctc (fcst_valid_beg)",
            "CREATE INDEX line_data_mctc_fcst_init_beg_idx ON line_data_mctc (fcst_init_beg)",
            "CREATE INDEX line_data_rhist_fcst_lead_idx ON line_data_rhist (fcst_lead)",
            "CREATE INDEX line_data_rhist_fcst_valid_beg_idx ON line_data_rhist (fcst_valid_beg)",
            "CREATE INDEX line_data_rhist_fcst_init_beg_idx ON line_data_rhist (fcst_init_beg)",
            "CREATE INDEX line_data_orank_fcst_lead_idx ON line_data_orank (fcst_lead)",
            "CREATE INDEX line_data_orank_fcst_valid_beg_idx ON line_data_orank (fcst_valid_beg)",
            "CREATE INDEX line_data_orank_fcst_init_beg_idx ON line_data_orank (fcst_init_beg)",
            "CREATE INDEX line_data_relp_fcst_lead_idx ON line_data_relp (fcst_lead)",
            "CREATE INDEX line_data_relp_fcst_valid_beg_idx ON line_data_relp (fcst_valid_beg)",
            "CREATE INDEX line_data_relp_fcst_init_beg_idx ON line_data_relp (fcst_init_beg)",
            "CREATE INDEX line_data_eclv_fcst_lead_idx ON line_data_eclv (fcst_lead)",
            "CREATE INDEX line_data_eclv_fcst_valid_beg_idx ON line_data_eclv (fcst_valid_beg)",
            "CREATE INDEX line_data_eclv_fcst_init_beg_idx ON line_data_eclv (fcst_init_beg)",
            "CREATE INDEX line_data_ssvar_fcst_lead_idx ON line_data_ssvar (fcst_lead)",
            "CREATE INDEX line_data_ssvar_fcst_valid_beg_idx ON line_data_ssvar (fcst_valid_beg)",
            "CREATE INDEX line_data_ssvar_fcst_init_beg_idx ON line_data_ssvar (fcst_init_beg)",
            "CREATE INDEX line_data_enscnt_fcst_lead_idx ON line_data_enscnt (fcst_lead)",
            "CREATE INDEX line_data_enscnt_fcst_valid_beg_idx ON line_data_enscnt (fcst_valid_beg)",
            "CREATE INDEX line_data_enscnt_fcst_init_beg_idx ON line_data_enscnt (fcst_init_beg)",
            "CREATE INDEX line_data_grad_fcst_lead_idx ON line_data_grad (fcst_lead)",
            "CREATE INDEX line_data_grad_fcst_valid_beg_idx ON line_data_grad (fcst_valid_beg)",
            "CREATE INDEX line_data_grad_fcst_init_beg_idx ON line_data_grad (fcst_init_beg)"
    };


    tableVarLengthTable = new HashMap<>();
    tableVarLengthTable.put("PCT", "line_data_pct_thresh");
    tableVarLengthTable.put("PSTD", "line_data_pstd_thresh");
    tableVarLengthTable.put("PJC", "line_data_pjc_thresh");
    tableVarLengthTable.put("PRC", "line_data_prc_thresh");
    tableVarLengthTable.put("MCTC", "line_data_mctc_cnt");
    tableVarLengthTable.put("RHIST", "line_data_rhist_rank");
    tableVarLengthTable.put("RELP", "line_data_relp_ens");
    tableVarLengthTable.put("PHIST", "line_data_phist_bin");
    tableVarLengthTable.put("ORANK", "line_data_orank_ens");
    tableVarLengthTable.put("ECLV", "line_data_eclv_pnt");

    tableDataFileLU = new HashMap<>();
    tableDataFileLU.put("point_stat", 0);
    tableDataFileLU.put("grid_stat", 1);
    tableDataFileLU.put("mode_cts", 2);
    tableDataFileLU.put("mode_obj", 3);
    tableDataFileLU.put("wavelet_stat", 4);
    tableDataFileLU.put("ensemble_stat", 5);
    tableDataFileLU.put("vsdb_point_stat", 6);
    tableDataFileLU.put("stat", 7);
    tableDataFileLU.put("mtd_2d", 8);
    tableDataFileLU.put("mtd_3d_pc", 9);
    tableDataFileLU.put("mtd_3d_ps", 10);
    tableDataFileLU.put("mtd_3d_sc", 11);
    tableDataFileLU.put("mtd_3d_ss", 12);


    initVarLengthLineDataIds();
  }

  @Override
  public void applyIndexes() {
    applyIndexes(false);
  }

  @Override
  public void dropIndexes() {
    applyIndexes(true);
  }

  private void applyIndexes(boolean drop) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String[] sqlArray;
    String operation;
    if (drop) {
      sqlArray = dropIndexesQueries;
      operation = "dropping";
    } else {
      sqlArray = createIndexesQueries;
      operation = "creating";
    }
    logger.info("    ==== indexes ====" + operation);

    for (String sql : sqlArray) {
      try (Connection con = getConnection();
           PreparedStatement stmt = con.prepareStatement(sql)) {
        stmt.executeUpdate();
      } catch (SQLException se) {
        //print message only if the error is NOT about duplicated or missing index
        //1061; Symbol: ER_DUP_KEYNAME; SQLSTATE: 42000 Message: Duplicate key name '%s'
        //1091; Symbol: ER_CANT_DROP_FIELD_OR_KEY; SQLSTATE: 42000 Message: Can't
        // DROP '%s'; check that column/key exists
        if (se.getErrorCode() != 1091 && se.getErrorCode() != 1061) {
          logger.error(ERROR_MARKER, se.getMessage());
        }
      }
    }
    try {
      stopWatch.stop();
      logger.info("Indexes " + operation + " " + stopWatch.getFormattedTotalDuration());

    } catch (StopWatchException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    logger.info("");
  }


  /**
   * Initialize the table containing the max line_data_id for all line_data tables corresponding to
   * variable length rows. //* @param con database connection used to search against
   */
  private void initVarLengthLineDataIds() throws DatabaseException {
    tableVarLengthLineDataId.clear();
    Set<String> lineTypes = tableVarLengthTable.keySet();
    for (String lineType : lineTypes) {
      String strVarLengthTable = "line_data_" + lineType.toLowerCase();
      int lineDataId = getNextId(strVarLengthTable, "line_data_id");
      tableVarLengthLineDataId.put(lineType, lineDataId);
    }
  }

  /**
   * Build and execute a query that retrieves the next table record id, whose name is specified by
   * the input field, from the specified input table. The statement is run against the input
   * Connection and the next available id is returned. // * @param con
   *
   * @param table Database table whose next available id is returned
   * @param field Field name of the table id record
   * @return Next available id
   * @throws DatabaseException
   */
  private int getNextId(String table, String field) throws DatabaseException {
    int intId;
    PreparedStatement pstmt = null;
    ResultSet res = null;
    try (Connection con = getConnection()) {
      pstmt = con.prepareStatement("SELECT MAX(" + field + ") FROM " + table);
      res = pstmt.executeQuery();
      if (!res.next()) {
        throw new DatabaseException(
                "METviewer load error: getNextId(" + table + ", " + field + ") unable"
                        + " to find max id");
      }
      String strId = res.getString(1);
      if (null == strId) {
        intId = 0;
      } else {
        intId = Integer.parseInt(strId) + 1;
      }

    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage());
    } finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (res != null) {
        try {
          res.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
    }

    return intId;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolStatHeaderTableCheck) or
   * by searching the stat_header table for a duplicate (specified by statHeaderDBCheck). Records in
   * line_data tables, stat_group tables and line_data_thresh tables are created from the data in
   * the input file. If necessary, records in the stat_header table are created as well.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws DatabaseException
   */
  @Override
  public Map<String, Long> loadStatFile(final DataFileInfo info) throws DatabaseException {
    Map<String, Long> timeStats = new HashMap<>();
    //  initialize the insert data structure
    MVLoadStatInsertData insertData = new MVLoadStatInsertData();
    //  performance counters
    long statHeaderLoadStart = System.currentTimeMillis();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);

    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String filename = info.path + "/" + info.filename;
    int intLine = 0;
    List<String> headerNames = new ArrayList<>();
    boolean headerHasUnits = true;

    try (
            FileReader fileReader = new FileReader(filename);
            BoundedBufferedReader reader = new BoundedBufferedReader(fileReader, MAX_LINES,
                    MAX_LINE_LEN)) {
      //  read in each line of the input file
      while (reader.ready()) {
        String line = reader.readLineBounded();
        String[] listToken = line.split("\\s+");
        intLine++;

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          headerHasUnits = headerNames.contains("FCST_UNITS");
          continue;
        }


        //  if the line type load selector is activated, check that the current line type is on the list
        insertData.setLineType(MVUtil.findValue(listToken, headerNames, "LINE_TYPE"));
        if (!MVUtil.isValidLineType(insertData.getLineType())) {
          logger.warn(
                  "  **  WARNING: unexpected line type: " + insertData.getLineType()
                          + "  the line will be ignored     ");
          continue;
        }

        if (info.lineTypeLoad && !info.tableLineTypeLoad.containsKey(insertData.getLineType())) {
          continue;
        }

        String lineType = insertData.getLineType();
        boolean isMet8 = true;
        if (insertData.getLineType().equals("RHIST")) {
          int indexOfNrank = headerNames.indexOf("LINE_TYPE") + 2;
          boolean isInt = MVUtil.isInteger(listToken[indexOfNrank], 10);
          isMet8 = isInt && (Integer.valueOf(listToken[indexOfNrank]) + indexOfNrank == listToken.length - 1);

        }

        int[] lengthGroupIndices = null;
        if (MVUtil.lengthGroupIndices.containsKey(lineType)) {
          lengthGroupIndices = Arrays.copyOf(MVUtil.lengthGroupIndices.get(lineType), MVUtil.lengthGroupIndices.get(lineType).length);
          int diff = lengthGroupIndices[1] - lengthGroupIndices[0];
          if (headerHasUnits) {

            if (lineType.equals("PCT") || lineType.equals("PCTD")
                    || lineType.equals("PJC") || lineType.equals("PRC") || lineType.equals("MCTC")
                    || lineType.equals("RELP")) {
              lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 2;
            } else if (lineType.equals("PHIST")) {
              lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 3;
            } else if (lineType.equals("ORANK")) {
              lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 12;
            } else if (lineType.equals("ECLV")) {
              lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 4;
            } else if (lineType.equals("RHIST")) {
              lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 2;
            }
            lengthGroupIndices[1] = lengthGroupIndices[0] + diff;
          } else {
            if (lineType.equals("RHIST")) {
              if (!isMet8) {
                lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 4;
              } else {
                lengthGroupIndices[0] = headerNames.indexOf("LINE_TYPE") + 2;
              }
              lengthGroupIndices[1] = lengthGroupIndices[0] + diff;
            } else {
              if (headerNames.indexOf("DESC") < 0) {
                //for old versions
                lengthGroupIndices[0] = lengthGroupIndices[0] - 1;
                lengthGroupIndices[1] = lengthGroupIndices[1] - 1;
              }
            }
          }

        }
        insertData.setFileLine(filename + ":" + intLine);


        //  parse the valid time

        LocalDateTime fcstValidBeg = LocalDateTime.parse(MVUtil.findValue(listToken, headerNames, "FCST_VALID_BEG"),
                DB_DATE_STAT_FORMAT);


        LocalDateTime fcstValidEnd = LocalDateTime.parse(MVUtil.findValue(listToken, headerNames, "FCST_VALID_END"),
                DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidBeg = LocalDateTime.parse(MVUtil.findValue(listToken, headerNames, "OBS_VALID_BEG"),
                DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidEnd = LocalDateTime.parse(MVUtil.findValue(listToken, headerNames, "OBS_VALID_END"),
                DB_DATE_STAT_FORMAT);

        //  format the valid times for the database insert
        String fcstValidBegStr = DATE_FORMATTER.format(fcstValidBeg);
        String fcstValidEndStr = DATE_FORMATTER.format(fcstValidEnd);
        String obsValidBegStr = DATE_FORMATTER.format(obsValidBeg);
        String obsValidEndStr = DATE_FORMATTER.format(obsValidEnd);

        //  calculate the number of seconds corresponding to fcst_lead
        String fcstLeadStr = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int fcstLeadLen = fcstLeadStr.length();
        int fcstLeadSec = Integer.parseInt(fcstLeadStr.substring(fcstLeadLen - 2, fcstLeadLen));
        fcstLeadSec += Integer
                .parseInt(fcstLeadStr.substring(fcstLeadLen - 4, fcstLeadLen - 2)) * 60;
        fcstLeadSec += Integer.parseInt(fcstLeadStr.substring(0, fcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead

        LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
        fcstInitBeg = fcstInitBeg.minusSeconds(fcstLeadSec);

        String fcstInitBegStr = DATE_FORMATTER.format(fcstInitBeg);

        //  ensure that the interp_pnts field value is a reasonable integer
        String strInterpPnts = MVUtil.findValue(listToken, headerNames, "INTERP_PNTS");
        if (strInterpPnts.equals("NA")) {
          strInterpPnts = "0";
        }


        //  do not load matched pair lines or orank lines
        if ((!info.loadMpr && lineType.equals("MPR")) || (!info.loadOrank && lineType.equals("ORANK"))) {
          continue;
        }


        /*
         * * * *  stat_header insert  * * * *
         */
        headerRecords++;

        //  build the stat_header value list for this line

        List<Object> statHeaderValueList = new ArrayList<>();
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "VERSION"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "MODEL"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "DESC"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBS_LEV"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "OBTYPE"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "VX_MASK"));
        statHeaderValueList.add(MVUtil.findValue(listToken, headerNames, "INTERP_MTHD"));
        statHeaderValueList.add(strInterpPnts);

        String fcstThresh = MVUtil.findValue(listToken, headerNames, "FCST_THRESH");
        double fcstPerc = (double) -9999;
        String fcstThreshStr;
        if (fcstThresh.contains("(") && fcstThresh.contains(")")) {
          String percStr = fcstThresh.substring(fcstThresh.indexOf("(") + 1, fcstThresh.indexOf(")"));
          fcstThreshStr = fcstThresh.substring(0, fcstThresh.indexOf("("));
          try {
            fcstPerc = Double.valueOf(percStr);
          } catch (NumberFormatException e) {
            logger.info("String for the forecast threshold percentile " + percStr
                    + " can't be converted to double and ignored");
          }
        } else {
          fcstThreshStr = fcstThresh;
        }

        String obsThresh = MVUtil.findValue(listToken, headerNames, "OBS_THRESH");
        double obsPerc = (double) -9999;
        String obsThreshStr;
        if (obsThresh.contains("(") && obsThresh.contains(")")) {
          String percStr = obsThresh.substring(obsThresh.indexOf("(") + 1, obsThresh.indexOf(")"));
          obsThreshStr = obsThresh.substring(0, obsThresh.indexOf("("));
          try {
            obsPerc = Double.valueOf(percStr);
          } catch (NumberFormatException e) {
            logger.info("String for the obs threshold percentile " + percStr
                    + " can't be converted to double and ignored");
          }
        } else {
          obsThreshStr = obsThresh;
        }
        statHeaderValueList.add(fcstThreshStr);
        statHeaderValueList.add(obsThreshStr);
        //statHeaderValueList.add(fcstPerc);
        //statHeaderValueList.add(obsPerc);


        //  build a where clause for searching for duplicate stat_header records
        String statHeaderWhere = BINARY + "  model =?"
                + "  AND " + BINARY + "descr =?"
                + "  AND " + BINARY + "fcst_var =?"
                + "  AND " + BINARY + "fcst_units =?"
                + "  AND " + BINARY + "fcst_lev =?"
                + "  AND " + BINARY + "obs_var =?"
                + "  AND " + BINARY + "obs_units =?"
                + "  AND " + BINARY + "obs_lev =?"
                + "  AND " + BINARY + "obtype =?"
                + "  AND " + BINARY + "vx_mask =?"
                + "  AND " + BINARY + "interp_mthd =?"
                + "  AND interp_pnts =?"
                + "  AND " + BINARY + "fcst_thresh =?"
                + "  AND " + BINARY + "obs_thresh =?";


        //  build the value list for the stat_header insert
        StringBuilder csvBuilder = new StringBuilder();
        for (int i = 0; i < statHeaderValueList.size(); i++) {
          Object value = statHeaderValueList.get(i);
          if (MVUtil.isNumeric(value)) {
            csvBuilder.append(value);
          } else {
            csvBuilder.append("'").append(value).append("'");
          }
          if (i != statHeaderValueList.size() - 1) {
            csvBuilder.append(MVUtil.SEPARATOR);
          }
        }
        String statHeaderValue = csvBuilder.toString();

        String fileLine = filename + ":" + intLine;

        //  look for the header key in the table
        Integer statHeaderId = -1;
        if (statHeaders.containsKey(statHeaderValue)) {
          statHeaderId = statHeaders.get(statHeaderValue);
        }

        //  if the stat_header does not yet exist, create one
        else {

          //  look for an existing stat_header record with the same information
          boolean foundStatHeader = false;
          long statHeaderSearchBegin = System.currentTimeMillis();
          if (info.statHeaderDBCheck) {
            String statHeaderSelect = "SELECT stat_header_id FROM  stat_header WHERE" +
                    statHeaderWhere;
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet res = null;
            try {
              con = getConnection();
              stmt = con.prepareStatement(statHeaderSelect);
              stmt.setString(1, MVUtil.findValue(listToken, headerNames, "MODEL"));
              stmt.setString(2, MVUtil.findValue(listToken, headerNames, "DESC"));
              stmt.setString(3, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(4, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(5, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(6, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(7, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(8, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));
              stmt.setString(9, MVUtil.findValue(listToken, headerNames, "OBTYPE"));
              stmt.setString(10, MVUtil.findValue(listToken, headerNames, "VX_MASK"));
              stmt.setString(11, MVUtil.findValue(listToken, headerNames, "INTERP_MTHD"));
              stmt.setInt(12, Integer.valueOf(strInterpPnts));
              stmt.setString(13, fcstThreshStr);
              stmt.setString(14, obsThreshStr);

              res = stmt.executeQuery();
              if (res.next()) {
                statHeaderId = res.getInt(1);
                foundStatHeader = true;
              }
            } catch (SQLException | NumberFormatException e) {
              logger.error(ERROR_MARKER, e.getMessage());
            } finally {

              if (res != null) {
                try {
                  res.close();
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
              if (stmt != null) {
                try {
                  stmt.close();
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
              try {
                con.close();
              } catch (SQLException e) {
                logger.error(ERROR_MARKER, e.getMessage());
              }
            }
          }

          timeStats.put("headerSearchTime",
                  timeStats.get("headerSearchTime") + System.currentTimeMillis()
                          - statHeaderSearchBegin);


          //  if the stat_header was not found, add it to the table
          if (!foundStatHeader) {

            statHeaderId = intStatHeaderIdNext++;
            statHeaders.put(statHeaderValue, statHeaderId);

            //  insert the record into the stat_header database table
            String sql = "INSERT INTO stat_header VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int statHeaderInsert;
            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

              stmt.setInt(1, statHeaderId);
              stmt.setString(2, MVUtil.findValue(listToken, headerNames, "VERSION"));
              stmt.setString(3, MVUtil.findValue(listToken, headerNames, "MODEL"));
              stmt.setString(4, MVUtil.findValue(listToken, headerNames, "DESC"));
              stmt.setString(5, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(6, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(7, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(8, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(9, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(10, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));
              stmt.setString(11, MVUtil.findValue(listToken, headerNames, "OBTYPE"));
              stmt.setString(12, MVUtil.findValue(listToken, headerNames, "VX_MASK"));
              stmt.setString(13, MVUtil.findValue(listToken, headerNames, "INTERP_MTHD"));
              stmt.setObject(14, strInterpPnts, Types.INTEGER);
              stmt.setString(15, fcstThreshStr);
              stmt.setString(16, obsThreshStr);

              statHeaderInsert = stmt.executeUpdate();

            } catch (SQLException se) {
              logger.error(ERROR_MARKER, se.getMessage());
              throw new DatabaseException("caught SQLException calling executeUpdate: " + se.getMessage());
            }


            if (1 != statHeaderInsert) {
              logger.warn(
                      "  **  WARNING: unexpected result from stat_header INSERT: "
                              + statHeaderInsert + "\n        " + fileLine);
              statHeaderId = null;
            } else {
              headerInserts++;
            }

          } else {
            statHeaders.put(statHeaderValue, statHeaderId);
          }
        }

        if (statHeaderId != null) {

          int lineDataMax = listToken.length;
          String lineDataId = "";
          dataRecords++;

          //  if the line type is of variable length, get the line_data_id
          boolean hasVarLengthGroups = MVUtil.lengthGroupIndices.containsKey(lineType);

          //  determine the maximum token index for the data
          if (hasVarLengthGroups && lengthGroupIndices != null && lengthGroupIndices.length == 3) {
            int intLineDataId = tableVarLengthLineDataId.get(lineType);
            lineDataId = Integer.toString(intLineDataId);
            tableVarLengthLineDataId.put(lineType, intLineDataId + 1);
            switch (lineType) {
              case "RHIST":
                lineDataMax = lineDataMax - Integer.valueOf(listToken[lengthGroupIndices[0]]) * lengthGroupIndices[2];
                break;
              case "PSTD":
                lineDataMax = lineDataMax - Integer.valueOf(listToken[lengthGroupIndices[0]]) * lengthGroupIndices[2];
                break;
              default:
                lineDataMax = lengthGroupIndices[1];
                break;
            }

          }

          //  build the value list for the insert statement
          List<Object> lineDataValues = new ArrayList<>();

          if (!lineDataId.isEmpty()) {
            lineDataValues.add(lineDataId);
          }
          lineDataValues.add(statHeaderId);
          lineDataValues.add(info.fileId);
          lineDataValues.add(intLine);
          lineDataValues.add(fcstLeadStr);
          lineDataValues.add(fcstValidBegStr);
          lineDataValues.add(fcstValidEndStr);
          lineDataValues.add(fcstInitBegStr);
          lineDataValues.add(MVUtil.findValue(listToken, headerNames, "OBS_LEAD"));
          lineDataValues.add(obsValidBegStr);
          lineDataValues.add(obsValidEndStr);

          //add percentile data if beeded
          if (fcstPerc != -9999 || obsPerc != -9999) {
            List<Object> lineDataValuesPercentile = new ArrayList<>(lineDataValues);
            lineDataValuesPercentile.add(fcstPerc);
            lineDataValuesPercentile.add(obsPerc);
            csvBuilder = new StringBuilder();
            for (int i = 0; i < lineDataValuesPercentile.size(); i++) {
              Object value = lineDataValuesPercentile.get(i);
              if (MVUtil.isNumeric(value)) {
                csvBuilder.append(value);
              } else {
                csvBuilder.append("'").append(value).append("'");
              }
              if (i != lineDataValuesPercentile.size() - 1) {
                csvBuilder.append(MVUtil.SEPARATOR);
              }
            }
            String csv = csvBuilder.toString();

            //  add the values list to the line type values map
            if (!insertData.getTableLineDataValues().containsKey("PERC")) {
              insertData.getTableLineDataValues().put("PERC", new ArrayList<>());
            }
            insertData.getTableLineDataValues().get("PERC").add("(" + csv + ")");

            dataInserts++;
          }

          //  if the line data requires a cov_thresh value, add it
          String covThresh = MVUtil.findValue(listToken, headerNames, "COV_THRESH");
          if (MVUtil.covThreshLineTypes.containsKey(insertData.getLineType())) {
            lineDataValues.add(replaceInvalidValues(covThresh));
          }

          //  if the line data requires an alpha value, add it
          String alpha = MVUtil.findValue(listToken, headerNames, "ALPHA");
          if (MVUtil.alphaLineTypes.containsKey(insertData.getLineType())) {
            if (alpha.equals("NA")) {
              logger.warn("  **  WARNING: alpha value NA with line type '"
                      + insertData.getLineType() + "'\n        "
                      + insertData.getFileLine());
            }
            lineDataValues.add(replaceInvalidValues(alpha));
          } else if (!alpha.equals("NA")) {
            logger.warn(
                    "  **  WARNING: unexpected alpha value '" + alpha + "' in line type '"
                            + insertData.getLineType() + "'\n        "
                            + insertData.getFileLine());
          }

          //  add total and all of the stats on the rest of the line to the value list
          if (lineType.equals("RHIST")) {
            int lineTypeIndex = headerNames.indexOf("LINE_TYPE");
            for (int i = lineTypeIndex + 1; i < lineDataMax; i++) {
              if (!isMet8) {
                //skip crps ,ign,crpss, spread
                if (i == lineTypeIndex + 2 || i == lineTypeIndex + 3
                        || i == lineTypeIndex + 5 || i == lineTypeIndex + 6) {
                  continue;
                }
              }
              lineDataValues.add(replaceInvalidValues(listToken[i]));
            }
            if (!isMet8) {
              //insert crps ,ign,crpss, spread to ECNT table
              List<Object> ecntLineDataValues = new ArrayList<>(lineDataValues);
              int indexOfNrankOld = headerNames.indexOf("LINE_TYPE") + 4;
              boolean isMetOld = (Double.valueOf(listToken[indexOfNrankOld])
                      .intValue() + indexOfNrankOld) ==
                      listToken.length - 1;

              ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 2]));
              if (isMetOld) {
                ecntLineDataValues.add(-9999);
              } else {
                ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 5]));
              }
              ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 3]));
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              if (isMetOld) {
                ecntLineDataValues.add(-9999);
              } else {
                ecntLineDataValues.add(replaceInvalidValues(listToken[lineTypeIndex + 6]));
              }
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              ecntLineDataValues.add(-9999);
              csvBuilder = new StringBuilder();

              for (int i = 1; i < ecntLineDataValues.size(); i++) {
                Object value = ecntLineDataValues.get(i);
                if (MVUtil.isNumeric(value)) {
                  csvBuilder.append(value);
                } else {
                  csvBuilder.append("'").append(value).append("'");
                }
                if (i != ecntLineDataValues.size() - 1) {
                  csvBuilder.append(MVUtil.SEPARATOR);
                }
              }
              String csv = csvBuilder.toString();
              if (!insertData.getTableLineDataValues().containsKey("ECNT")) {
                insertData.getTableLineDataValues().put("ECNT", new ArrayList<>());
              }
              insertData.getTableLineDataValues().get("ECNT").add("(" + csv + ")");
              dataInserts++;
            }
          } else {
            for (int i = headerNames.indexOf("LINE_TYPE") + 1; i < lineDataMax; i++) {
              //  add the stats in order
              lineDataValues.add(replaceInvalidValues(listToken[i]));

            }
          }


          if (lineType.equals("ORANK") && lengthGroupIndices != null && lengthGroupIndices.length == 3) {
            //skip ensemble fields and get data for the rest
            int extraFieldsInd = lineDataMax + Integer.valueOf(listToken[lengthGroupIndices[0]]) * lengthGroupIndices[2];
            for (int i = extraFieldsInd; i < listToken.length; i++) {
              lineDataValues.add(replaceInvalidValues(listToken[i]));
            }
          }

          int size = lineDataValues.size();
          int maxSize = size;
          switch (lineType) {
            case "CNT":
              maxSize = 105;
              break;
            case "MPR":
              maxSize = 24;
              break;
            case "ORANK":
              maxSize = 30;
              break;
            case "CTS":
              maxSize = 104;
              break;
            case "NBRCTS":
              maxSize = 105;
              break;
            case "NBRCNT":
              maxSize = 30;
              break;
            case "SAL1L2":
              maxSize = 17;
              break;
            case "SL1L2":
              maxSize = 17;
              break;
            case "GRAD":
              maxSize = 20;
              break;
            case "PSTD":
              maxSize = 30;
              break;
            case "SSVAR":
              maxSize = 46;
              break;

            case "VL1L2":
              maxSize = 20;
              break;
            case "ECNT":
              maxSize = 22;
              break;

            default:
          }
          while (size < maxSize) {
            lineDataValues.add(-9999);
            size++;
          }


          csvBuilder = new StringBuilder();
          for (int i = 0; i < lineDataValues.size(); i++) {
            Object value = lineDataValues.get(i);
            if (MVUtil.isNumeric(value)) {
              csvBuilder.append(value);
            } else {
              csvBuilder.append("'").append(value).append("'");
            }
            if (i != lineDataValues.size() - 1) {
              csvBuilder.append(MVUtil.SEPARATOR);
            }
          }
          String csv = csvBuilder.toString();

          //  add the values list to the line type values map
          if (!insertData.getTableLineDataValues().containsKey(insertData.getLineType())) {
            insertData.getTableLineDataValues().put(insertData.getLineType(), new ArrayList<>());
          }
          insertData.getTableLineDataValues().get(insertData.getLineType()).add("(" + csv + ")");

          dataInserts++;


          /*
           * * * *  var_length insert  * * * *
           */

          if (hasVarLengthGroups && lengthGroupIndices != null && lengthGroupIndices.length == 3) {

            //  get the index information about the current line type

            int groupCntIndex = lengthGroupIndices[0];
            int groupIndex = lengthGroupIndices[1];
            int groupSize = lengthGroupIndices[2];
            int numGroups = Integer.parseInt(listToken[groupCntIndex]);

            if (insertData.getLineType().equals("PCT")
                    || insertData.getLineType().equals("PJC")
                    || insertData.getLineType().equals("PRC")) {
              numGroups -= 1;
            }
            List<List<Object>> threshValues = insertData.getTableVarLengthValues()
                    .get(insertData.getLineType());
            if (null == threshValues) {
              threshValues = new ArrayList<>();
            }

            //  build a insert value statement for each threshold group
            if (insertData.getLineType().equals("MCTC")) {
              for (int i = 0; i < numGroups; i++) {
                for (int j = 0; j < numGroups; j++) {
                  List<Object> vals = new ArrayList<>();
                  vals.add(lineDataId);
                  vals.add(i + 1);
                  vals.add(j + 1);
                  vals.add(replaceInvalidValues(listToken[groupIndex++]));
                  threshValues.add(vals);
                  lengthRecords++;
                }
              }
            } else {
              if (insertData.getLineType().equals("RHIST") || insertData.getLineType().equals("PSTD")) {
                groupIndex = lineDataMax;
              }
              for (int i = 0; i < numGroups; i++) {
                List<Object> vals = new ArrayList<>();
                vals.add(lineDataId);
                vals.add(i + 1);

                for (int j = 0; j < groupSize; j++) {
                  vals.add(replaceInvalidValues(listToken[groupIndex++]));

                }
                threshValues.add(vals);
                lengthRecords++;
              }
            }
            insertData.getTableVarLengthValues().put(insertData.getLineType(), threshValues);
          }

          //  if the insert threshold has been reached, commit the stored data to the database
          if (info.insertSize <= insertData.getListInsertValues().size()) {
            int[] listInserts = commitStatData(insertData);
            dataInserts += listInserts[INDEX_LINE_DATA];
            lengthInserts += listInserts[INDEX_VAR_LENGTH];
          }
        }
      }  // end: while( reader.ready() )

    } catch
    (NegativeArraySizeException | IOException | ArrayIndexOutOfBoundsException | NumberFormatException | DatabaseException e) {
      logger.error(ERROR_MARKER, "ERROR for file " + filename + " : " + e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(insertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];


    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);


    //  print a performance report
    long statHeaderLoadTime = System.currentTimeMillis() - statHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (statHeaderLoadTime);

    if (info.verbose) {
      logger.info(MVUtil.padBegin("file lines: ", 36) + (intLine - 1) + "\n" +
              MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
              MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
              MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
              MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
              MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
              MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
              MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
              MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
              statHeaderLoadTime) + "\n" +
              MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
              headerSearchTime) + "\n" +
              MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf(
              dblLinesPerMSec) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Loads the insert value lists stored in the data structure MVLoadStatInsertData.  This method
   * was designed to be called from loadStatFile(), which is responsible for building insert value
   * lists for the various types of grid_stat and point_stat database tables.
   *
   * @param statInsertData Data structure loaded with insert value lists
   * @return An array of four integers, indexed by the INDEX_* members, representing the number of
   * database inserts of each type
   * @throws DatabaseException
   */
  private int[] commitStatData(MVLoadStatInsertData statInsertData)
          throws DatabaseException {

    int[] listInserts = new int[]{0, 0, 0, 0};

    /*
     * * * *  stat_header was committed commit  * * * *
     */

    statInsertData.getListInsertValues().clear();


    /*
     * * * *  line_data commit  * * * *
     */

    //  for each line type, build an insert statement with the appropriate list of values
    for (Map.Entry<String, List<String>> entry : statInsertData.getTableLineDataValues()
            .entrySet()) {
      statInsertData.setLineType(entry.getKey());
      ArrayList listValues = (ArrayList) entry.getValue();
      String tableName = "line_data_" + statInsertData.getLineType().toLowerCase(Locale.US);

      boolean resLineDataInsertCount = executeBatch(listValues, tableName);
      if (!resLineDataInsertCount) {
        logger.warn(
                "  **  WARNING: unexpected result from line_data INSERT: "
                        + statInsertData.getFileLine());
      }
      listInserts[INDEX_LINE_DATA]++;
    }
    statInsertData.getTableLineDataValues().clear();

    statInsertData.getListStatGroupInsertValues().clear();

    /*
     * * * *  variable length data commit  * * * *
     */

    //  insert probabilistic data into the thresh tables
    Set<String> strings = statInsertData.getTableVarLengthValues().keySet();
    String[] varLengthTypes = strings.toArray(new String[strings.size()]);


    for (String listVarLengthType : varLengthTypes) {
      List<List<Object>> listVarLengthValues =
              statInsertData.getTableVarLengthValues().get(listVarLengthType);
      if (listVarLengthValues.isEmpty()) {
        continue;
      }
      String sql = tableToInsert.get(tableVarLengthTable.get(listVarLengthType));

      try (Connection con = getConnection();
           PreparedStatement stmt = con.prepareStatement(sql)) {
        int[] threshInsert;
        for (List<Object> listVarLengthValue : listVarLengthValues) {
          for (int k = 0; k < listVarLengthValue.size(); k++) {
            stmt.setObject(k + 1, listVarLengthValue.get(k));
            listInserts[INDEX_VAR_LENGTH]++; //  lengthInserts++;
          }
          stmt.addBatch();
        }
        threshInsert = stmt.executeBatch();
        if (listVarLengthValues.size() != threshInsert.length) {
          logger.warn(
                  "  **  WARNING: unexpected result from thresh INSERT: " + threshInsert.length + " vs. " +
                          listVarLengthValues.size() + "\n        " + statInsertData.getFileLine());
        }
      } catch (SQLException se) {
        logger.error(ERROR_MARKER, se.getMessage());
        throw new DatabaseException(
                "caught SQLException calling executeUpdate: " + se.getMessage());
      }

      statInsertData.getTableVarLengthValues().put(listVarLengthType, new ArrayList<>());
    }

    return listInserts;
  }

  @Override
  public Map<String, Long> loadStatFileVSDB(DataFileInfo info) throws DatabaseException {

    Map<String, Long> timeStats = new HashMap<>();

    //  initialize the insert data structure
    MVLoadStatInsertData mvLoadStatInsertData = new MVLoadStatInsertData();

    //  performance counters
    long intStatHeaderLoadStart = System.currentTimeMillis();
    long headerSearchTime = 0;
    long headerRecords = 0;
    long headerInserts = 0;
    long dataRecords = 0;
    long dataInserts = 0;
    long intLineDataSkipped = 0;
    long lengthRecords = 0;
    long lengthInserts = 0;
    timeStats.put("headerSearchTime", 0L);


    //  get the next stat_header_id
    int intStatHeaderIdNext = getNextId("stat_header", "stat_header_id");

    //  set up the input file for reading
    String strFilename = info.path + "/" + info.filename;
    String ensValue = "";
    String[] ensValueArr = info.path.split("\\/");
    if (ensValueArr[ensValueArr.length - 1].contains("_")) {
      String[] ensValue1 = ensValueArr[ensValueArr.length - 1].split("_");
      ensValue = "_" + ensValue1[ensValue1.length - 1];
    }

    int intLine = 0;
    try (FileReader fileReader = new FileReader(strFilename);
         BoundedBufferedReader reader = new BoundedBufferedReader(fileReader, MAX_LINES,
                 MAX_LINE_LEN)) {
      List<String> allMatches;
      DateTimeFormatter formatStatVsdb = DateTimeFormatter.ofPattern("yyyyMMddHH");

      //  read in each line of the input file, remove "="
      while (reader.ready()) {
        String line = reader.readLineBounded();
        try {
          line = line.replaceAll("\\s=\\s", " "); // remove " = "

          Matcher m = Pattern.compile("\\d-0\\.").matcher(
                  line); // some records do not have a space between columns if the value in column starts with "-"

          allMatches = new ArrayList<>();
          while (m.find()) {
            allMatches.add(m.group());
          }
          for (String match : allMatches) {
            String newStr = match.replace("-", " -");
            line = line.replace(match, newStr);
          }

          String[] listToken = line.split("\\s+");
          intLine++;
          String thresh = "NA";
          String modelName = listToken[1];

          if (listToken[6].equals("BSS") || listToken[6].equals("ECON")
                  || listToken[6].equals("HIST") || listToken[6].equals("RELI")
                  || listToken[6].equals("RELP") || listToken[6].equals("RMSE")
                  || listToken[6].equals("RPS")) {
            modelName = modelName.split("\\/")[0] + ensValue;
          }

          //  if the line type load selector is activated, check that the current line type is on the list

          if (listToken[6].equals("RMSE")) {
            mvLoadStatInsertData.setLineType("CNT");
          } else if (listToken[6].equals("BSS")) {
            mvLoadStatInsertData.setLineType("PSTD");
          } else if (listToken[6].equals("HIST")) {
            mvLoadStatInsertData.setLineType("RHIST");
          } else if (listToken[6].equals("RELP")) {
            mvLoadStatInsertData.setLineType("RELP");
          } else if (listToken[6].equals("SL1L2")) {
            mvLoadStatInsertData.setLineType("SL1L2");
          } else if (listToken[6].equals("GRAD")) {
            mvLoadStatInsertData.setLineType("GRAD");
          } else if (listToken[6].equals("SAL1L2")) {
            mvLoadStatInsertData.setLineType("SAL1L2");
          } else if (listToken[6].equals("VL1L2")) {
            mvLoadStatInsertData.setLineType("VL1L2");
          } else if (listToken[6].equals("VAL1L2")) {
            mvLoadStatInsertData.setLineType("VAL1L2");
          } else if (listToken[6].equals("RPS")) {
            mvLoadStatInsertData.setLineType("ENSCNT");
          } else if (listToken[6].equals("ECON")) {
            mvLoadStatInsertData.setLineType("ECLV");
          } else if (listToken[6].equals("RELI")) {
            mvLoadStatInsertData.setLineType("PCT");
            int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
            thresh = "==1/" + intGroupSize;
          } else if (listToken[6].startsWith("FHO")) {
            mvLoadStatInsertData.setLineType("CTC");
            String[] threshArr = listToken[6].split("FHO");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else if (listToken[6].startsWith("FSS")) {
            mvLoadStatInsertData.setLineType("NBRCNT");
            String[] threshArr = listToken[6].split("FSS");
            if (threshArr.length > 1) {
              thresh = threshArr[1];
            }
          } else {
            continue;
          }
          if (info.lineTypeLoad
                  && !info.tableLineTypeLoad.containsKey(mvLoadStatInsertData.getLineType())) {
            continue;
          }

          mvLoadStatInsertData.setFileLine(strFilename + ":" + intLine);

          //  parse the valid times


          LocalDateTime fcstValidBeg = LocalDateTime.parse(listToken[3], formatStatVsdb);

          //  format the valid times for the database insert
          String fcstValidBegStr = DATE_FORMATTER.format(fcstValidBeg);

          //  calculate the number of seconds corresponding to fcst_lead
          String strFcstLead = listToken[2];
          int intFcstLeadSec = Integer.parseInt(strFcstLead) * 3600;

          //  determine the init time by combining fcst_valid_beg and fcst_lead

          LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
          fcstInitBeg = fcstInitBeg.minusSeconds(intFcstLeadSec);
          String fcstInitBegStr = DATE_FORMATTER.format(fcstInitBeg);
          String obsValidBegStr = DATE_FORMATTER.format(fcstValidBeg);
          String fcstValidEndStr = DATE_FORMATTER.format(fcstValidBeg);
          String obsValidEndStr = DATE_FORMATTER.format(fcstValidBeg);


          //  ensure that the interp_pnts field value is a reasonable integer
          String interpPnts = "0";

          String strLineType = mvLoadStatInsertData.getLineType();



          /*
           * * * *  stat_header insert  * * * *
           */
          headerRecords++;

          //  build the stat_header value list for this line
          String[] statHeaderValue = {
                  listToken[0],    //  version
                  modelName,      //  model
                  "NA",           //  descr
                  listToken[7],    //  fcst_var
                  "NA",            //  fcst_units
                  listToken[8],    //  fcst_lev
                  listToken[7],    //  obs_var
                  "NA",            //  obs_units
                  listToken[8],    //  obs_lev
                  listToken[4],    //  obtype
                  listToken[5],    //  vx_mask
                  "NA",    //  interp_mthd
                  interpPnts,    //  interp_pnts
                  thresh,    //  fcst_thresh
                  thresh,    //  obs_thresh
          };

          //  build a where clause for searching for duplicate stat_header records


          String statHeaderWhere = BINARY + "  model =?"
                  + "  AND " + BINARY + "descr =?"
                  + "  AND " + BINARY + "fcst_var =?"
                  + "  AND " + BINARY + "fcst_units =?"
                  + "  AND " + BINARY + "fcst_lev =?"
                  + "  AND " + BINARY + "obs_var =?"
                  + "  AND " + BINARY + "obs_units =?"
                  + "  AND " + BINARY + "obs_lev =?"
                  + "  AND " + BINARY + "obtype =?"
                  + "  AND " + BINARY + "vx_mask =?"
                  + "  AND " + BINARY + "interp_mthd =?"
                  + "  AND interp_pnts =?"
                  + "  AND " + BINARY + "fcst_thresh =?"
                  + "  AND " + BINARY + "obs_thresh =?";

          //  build the value list for the stat_header insert
          String statHeaderValueList = "";
          for (int i = 0; i < statHeaderValue.length; i++) {
            statHeaderValueList += (0 < i ? ", " : "") + "'" + statHeaderValue[i] + "'";
          }


          String strFileLine = strFilename + ":" + intLine;

          //  look for the header key in the table
          Integer statHeaderId = -1;
          if (statHeaders.containsKey(statHeaderValueList)) {
            statHeaderId = statHeaders.get(statHeaderValueList);
          }

          //  if the stat_header does not yet exist, create one
          else {

            //  look for an existing stat_header record with the same information
            boolean foundStatHeader = false;
            long intStatHeaderSearchBegin = System.currentTimeMillis();
            if (info.statHeaderDBCheck) {
              String statHeaderSelect = "SELECT stat_header_id FROM  stat_header WHERE" + statHeaderWhere;
              Connection con = null;
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                con = getConnection();
                stmt = con.prepareStatement(statHeaderSelect);
                stmt.setString(1, modelName);
                stmt.setString(2, "NA");
                stmt.setString(3, listToken[7]);
                stmt.setString(4, "NA");
                stmt.setString(5, listToken[8]);
                stmt.setString(6, listToken[7]);
                stmt.setString(7, "NA");
                stmt.setString(8, listToken[8]);
                stmt.setString(9, listToken[4]);
                stmt.setString(10, listToken[5]);
                stmt.setString(11, "NA");
                stmt.setInt(12, Integer.valueOf(interpPnts));
                stmt.setString(13, thresh);
                stmt.setString(14, thresh);

                res = stmt.executeQuery();
                if (res.next()) {
                  String statHeaderIdDup = res.getString(1);
                  statHeaderId = Integer.parseInt(statHeaderIdDup);
                  foundStatHeader = true;
                }
              } catch (SQLException | NumberFormatException e) {
                logger.error(ERROR_MARKER, e.getMessage());
              } finally {
                try {
                  if (res != null) {
                    res.close();
                  }
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
                try {
                  if (stmt != null) {
                    stmt.close();
                  }
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
                try {
                  con.close();
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
            }
            timeStats.put("headerSearchTime",
                    timeStats.get("headerSearchTime")
                            + System.currentTimeMillis() - intStatHeaderSearchBegin);


            //  if the stat_header was not found, add it to the table
            if (!foundStatHeader) {

              statHeaderId = intStatHeaderIdNext++;
              statHeaders.put(statHeaderValueList, statHeaderId);

              //  insert the record into the stat_header database table
              String sql = "INSERT INTO stat_header VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
              int intStatHeaderInsert;
              try (Connection con = getConnection();
                   PreparedStatement stmt = con.prepareStatement(sql)) {


                stmt.setInt(1, statHeaderId);
                stmt.setString(2, statHeaderValue[0]);
                stmt.setString(3, modelName);
                stmt.setString(4, "NA");
                stmt.setString(5, listToken[7]);
                stmt.setString(6, "NA");
                stmt.setString(7, listToken[8]);
                stmt.setString(8, listToken[7]);
                stmt.setString(9, "NA");
                stmt.setString(10, listToken[8]);
                stmt.setString(11, listToken[4]);
                stmt.setString(12, listToken[5]);
                stmt.setString(13, "NA");
                stmt.setObject(14, interpPnts, Types.INTEGER);
                stmt.setString(15, thresh);
                stmt.setString(16, thresh);


                intStatHeaderInsert = stmt.executeUpdate();
              } catch (SQLException se) {
                logger.error(ERROR_MARKER, se.getMessage());
                throw new DatabaseException(
                        "caught SQLException calling executeUpdate: " + se.getMessage());
              }

              if (1 != intStatHeaderInsert) {
                logger.warn(
                        "  **  WARNING: unexpected result from stat_header INSERT: " + intStatHeaderInsert + "\n        " + strFileLine);
                statHeaderId = null;
              } else {
                headerInserts++;
              }

            } else {
              statHeaders.put(statHeaderValueList, statHeaderId);
            }
          }
          if (statHeaderId != null) {

            String lineDataIdStr = "";
            dataRecords++;

            //  if the line type is of variable length, get the line_data_id
            boolean hasVarLengthGroups = MVUtil.lengthGroupIndices
                    .containsKey(mvLoadStatInsertData.getLineType());

            //  determine the maximum token index for the data
            if (hasVarLengthGroups) {
              int lineDataId = tableVarLengthLineDataId.get(strLineType);
              lineDataIdStr = Integer.toString(lineDataId);
              tableVarLengthLineDataId.put(strLineType, lineDataId + 1);
            }

            //  build the value list for the insert statment
            StringBuilder lineDataValueList = new StringBuilder();
            if (!lineDataIdStr.isEmpty()) {
              lineDataValueList.append(lineDataIdStr).append(", ");//  line_data_id (if present)
            }

            lineDataValueList.append(
                    statHeaderId + ", " +      //  stat_header_id
                            info.fileId + ", " +      //  data_file_id
                            intLine + ", " +          //  line_num
                            strFcstLead + ", " +        //  fcst_lead
                            "'" + fcstValidBegStr + "', " +    //  fcst_valid_beg
                            "'" + fcstValidEndStr + "', " +    //  fcst_valid_end
                            "'" + fcstInitBegStr + "', " +    //  fcst_init_beg
                            "000000" + ", " +        //  obs_lead
                            "'" + obsValidBegStr + "', " +    //  obs_valid_beg
                            "'" + obsValidEndStr + "'");      //  obs_valid_end

            //  if the line data requires a cov_thresh value, add it
            String strCovThresh = "NA";
            if (MVUtil.covThreshLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
              lineDataValueList.append(", '").append(replaceInvalidValues(strCovThresh))
                      .append("'");
            }

            //  if the line data requires an alpha value, add it
            String alpha = "-9999";
            if (MVUtil.alphaLineTypes.containsKey(mvLoadStatInsertData.getLineType())) {
              if (alpha.equals("NA")) {
                logger.warn("  **  WARNING: alpha value NA with line type '"
                        + mvLoadStatInsertData.getLineType()
                        + "'\n        " + mvLoadStatInsertData.getFileLine());
              }
              lineDataValueList.append(", ").append(replaceInvalidValues(alpha));
            }

            if (listToken[6].equals("RMSE")) {//CNT line type
              for (int i = 0; i < 94; i++) {
                if (i == 53) {
                  lineDataValueList.append(", '").append(listToken[10]).append("'");
                } else if (i == 31) {
                  lineDataValueList.append(", '").append(listToken[11]).append("'");
                } else if (i == 36) {
                  lineDataValueList.append(", '").append(listToken[9]).append("'");
                } else if (i == 44) {
                  lineDataValueList.append(", '").append(listToken[12]).append("'");
                } else if (i == 0 || i == 28 || i == 29 || i == 30) {//total,ranks, frank_ties, orank_ties
                  lineDataValueList.append(", '0'");
                } else if (i == 77) {
                  lineDataValueList.append(", '").append(listToken[13]).append("'");
                } else {
                  lineDataValueList.append(", '-9999'");
                }
              }
            }


            if (listToken[6].equals("BSS")) {//PSTD line type
              for (int i = 0; i < 17; i++) {
                switch (i) {
                  case 0:
                  case 1:
                    lineDataValueList.append(", '0'");
                    break;
                  case 2:
                  case 3:
                  case 8:
                  case 10:
                  case 11:
                  case 4:
                  case 13:
                  case 14:
                  case 16:
                    lineDataValueList.append(", '-9999'");
                    break;
                  case 5:
                    lineDataValueList.append(", '").append(listToken[12]).append("'");
                    break;
                  case 6:
                    lineDataValueList.append(", '").append(listToken[13]).append("'");
                    break;
                  case 7:
                    lineDataValueList.append(", '").append(listToken[14]).append("'");
                    break;
                  case 9:
                    lineDataValueList.append(", '").append(listToken[9]).append("'");
                    break;
                  case 12:
                    lineDataValueList.append(", '").append(listToken[10]).append("'");
                    break;
                  case 15:
                    lineDataValueList.append(", '").append(listToken[11]).append("'");
                    break;
                  default:
                }

              }
            }

            if (listToken[6].equals("RPS")) {//ENSCNT line type
              for (int i = 0; i < 30; i++) {
                switch (i) {
                  case 0:
                    lineDataValueList.append(", '").append(listToken[9]).append("'");
                    break;
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                    lineDataValueList.append(", '-9999'");
                    break;
                  case 5:
                    lineDataValueList.append(", '").append(listToken[10]).append("'");
                    break;
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                    lineDataValueList.append(", '-9999'");
                    break;
                  case 10:
                    lineDataValueList.append(", '").append(listToken[11]).append("'");
                    break;
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                    lineDataValueList.append(", -9999");
                    break;
                  case 15:
                    lineDataValueList.append(", '").append(listToken[12]).append("'");
                    break;
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                    lineDataValueList.append(", -9999");
                    break;
                  case 20:
                    lineDataValueList.append(", '").append(listToken[13]).append("'");
                    break;
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                    lineDataValueList.append(", -9999");
                    break;
                  case 25:
                    lineDataValueList.append(", '").append(listToken[14]).append("'");
                    break;
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                    lineDataValueList.append(", -9999");
                    break;
                  default:

                }
              }

            }

            if (listToken[6].equals("HIST")) { //RHIST line type
              int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              lineDataValueList.append(", 0,").append(intGroupSize);

            }

            if (listToken[6].equals("RELP")) {  // RELP line type
              lineDataValueList.append(", 0");
              int intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]);
              lineDataValueList.append(", '").append(intGroupSize).append("'");
            }
            if (listToken[6].equals("ECON")) {  // ECLV line type
              lineDataValueList.append(", 0, -9999, -9999");
              int intGroupSize = 18;
              lineDataValueList.append(", '").append(intGroupSize).append("'");
            }


            if (listToken[6].equals("RELI")) { //PCT line type
              int total = 0;
              int intGroupSize;
              int intGroupIndex = 9;
              try {
                intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
              } catch (NumberFormatException e) {
                intGroupSize = 0;
              }
              for (int i = 0; i < intGroupSize; i++) {
                int on;
                try {
                  on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue();
                  total = total + on;
                } catch (NumberFormatException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
                intGroupIndex++;
              }


              lineDataValueList.append(", ").append(total).append(", ").append(intGroupSize);
            }

            if (listToken[6].equals("SL1L2")
                    || listToken[6].equals("SAL1L2")) {//SL1L2,SAL1L2 line types
              for (int i = 0; i < 7; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList.append(", '")
                            .append((Double.valueOf(listToken[i + 9])).intValue()).append("'");
                  } else {
                    lineDataValueList.append(", '").append(Double.valueOf(listToken[i + 9]))
                            .append("'");
                  }

                } else {
                  lineDataValueList.append(", '-9999'");
                }
              }
            }
            if (listToken[6].equals("VAL1L2")) {//VAL1L2 line type
              for (int i = 0; i < 8; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList.append(", '")
                            .append((Double.valueOf(listToken[i + 9])).intValue()).append("'");
                  } else {
                    lineDataValueList.append(", '").append(Double.valueOf(listToken[i + 9]))
                            .append("'");
                  }
                } else {
                  lineDataValueList.append(", '-9999'");
                }

              }
            }
            if (listToken[6].equals("GRAD")) {//GRAD line type
              for (int i = 0; i < 10; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList.append(", '")
                            .append((Double.valueOf(listToken[i + 9])).intValue()).append("'");
                  } else {
                    lineDataValueList.append(", '").append(Double.valueOf(listToken[i + 9]))
                            .append("'");
                  }
                } else {
                  lineDataValueList.append(", '-9999'");
                }

              }
            }
            if (listToken[6].equals("VL1L2")) {//VL1L2
              for (int i = 0; i < 10; i++) {
                if (i + 9 < listToken.length) {
                  if (i == 0) {
                    lineDataValueList.append(", '")
                            .append((Double.valueOf(listToken[i + 9])).intValue()).append("'");
                  } else {
                    lineDataValueList.append(", '").append(Double.valueOf(listToken[i + 9]))
                            .append("'");
                  }
                } else {
                  lineDataValueList.append(", '-9999'");
                }

              }
            }
            if (listToken[6].startsWith("FHO")) {//CTC line type

              double total = Double.parseDouble(listToken[9]);
              double f_rate = Double.parseDouble(listToken[10]);
              double h_rate = Double.parseDouble(listToken[11]);
              double o_rate;
              if (listToken.length > 12) {
                o_rate = Double.valueOf(listToken[12]);
              } else {
                o_rate = 0;
                logger.info("o_rate os 0");
              }

              double fy = total * f_rate;
              double fy_oy = total * h_rate;
              double oy = total * o_rate;
              double fy_on = fy - fy_oy;
              double fn_oy = oy - fy_oy;
              double fn_on = total - fy - oy + fy_oy;


              for (int i = 0; i < 5; i++) {
                if (i == 4) {
                  lineDataValueList.append(", '").append(Math.max(0, fn_on)).append("'");
                } else if (i == 3) {
                  lineDataValueList.append(", '").append(Math.max(0, fn_oy)).append("'");
                } else if (i == 2) {
                  lineDataValueList.append(", '").append(Math.max(0, fy_on)).append("'");
                } else if (i == 1) {
                  lineDataValueList.append(", '").append(Math.max(0, fy_oy)).append("'");
                } else if (i == 0) {//total,
                  lineDataValueList.append(", '").append(listToken[9]).append("'");
                }

              }
            }
            if (listToken[6].startsWith("FSS")) {//NBRCNT line type
              double fss = -9999;
              if (listToken.length > 11) {
                fss = 1 - Double.valueOf(listToken[10])
                        / (Double.valueOf(listToken[11]) + Double.valueOf(listToken[12]));
              }
              for (int i = 0; i < 19; i++) {
                if (i == 0) {//total,
                  lineDataValueList.append(", ").append(listToken[9]);
                } else if (i == 1) {//fbs
                  lineDataValueList.append(", ").append(listToken[10]);
                } else if (i == 4) {//fss
                  lineDataValueList.append(", ").append(fss);
                } else {
                  lineDataValueList.append(", '-9999'");
                }
              }
            }


            //  add the values list to the line type values map
            List<String> listLineTypeValues = new ArrayList<>();
            if (mvLoadStatInsertData.getTableLineDataValues()
                    .containsKey(mvLoadStatInsertData.getLineType())) {
              listLineTypeValues = mvLoadStatInsertData.getTableLineDataValues()
                      .get(mvLoadStatInsertData.getLineType());
            }
            listLineTypeValues.add("(" + lineDataValueList + ")");
            mvLoadStatInsertData.getTableLineDataValues()
                    .put(mvLoadStatInsertData.getLineType(), listLineTypeValues);
            dataInserts++;


            /*
             * * * *  var_length insert  * * * *
             */

            if (hasVarLengthGroups) {
              //  get the index information about the current line type
              int intGroupIndex = 0;
              int intGroupSize = 0;
              int intNumGroups = 0;

              if (listToken[6].equals("HIST")) {//RHIST line type
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                } catch (NumberFormatException e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("RELP")) {//RELP line type)
                intGroupIndex = 9;
                try {
                  intNumGroups = Integer.valueOf(listToken[1].split("\\/")[1]);
                } catch (NumberFormatException e) {
                  intNumGroups = 0;
                }
                intGroupSize = 1;
              } else if (listToken[6].equals("ECON")) {//ECLV line type)
                intGroupIndex = 9;
                intNumGroups = 18;
                intGroupSize = 1;
              } else if (listToken[6].equals("RELI")) {//PCT line type)
                intGroupIndex = 9;
                try {
                  intGroupSize = Integer.valueOf(listToken[1].split("\\/")[1]) + 1;
                } catch (NumberFormatException e) {
                  intGroupSize = 0;
                }
                intNumGroups = 2;
              }

              List<List<Object>> listThreshValues = mvLoadStatInsertData.getTableVarLengthValues()
                      .get(mvLoadStatInsertData.getLineType());
              if (null == listThreshValues) {
                listThreshValues = new ArrayList<>();
              }

              //  build a insert value statement for each threshold group
              if (listToken[6].equals("HIST")) {
                for (int i = 0; i < intNumGroups; i++) {
                  List<Object> threshValues = new ArrayList<>();
                  threshValues.add(lineDataIdStr);
                  threshValues.add(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      threshValues.add(res * 100);
                    }

                  }
                  listThreshValues.add(threshValues);
                  lengthRecords++;
                }
              } else if (listToken[6].equals("RELI")) {
                int total = 0;
                for (int i = 0; i < intGroupSize; i++) {
                  double thresh_i;
                  if (intGroupSize > 1) {
                    thresh_i = (double) i / (double) (intGroupSize - 1);
                  } else {
                    thresh_i = 0;
                  }
                  List<Object> threshValues = new ArrayList<>();

                  threshValues.add(lineDataIdStr);
                  threshValues.add(i + 1);
                  threshValues.add(thresh_i);
                  int oy;
                  int on;
                  try {
                    oy = Double.valueOf(listToken[intGroupIndex]).intValue();
                    on = Double.valueOf(listToken[intGroupIndex + intGroupSize]).intValue() - oy;
                    threshValues.add(oy);
                    threshValues.add(on);
                    total = total + oy + on;
                  } catch (NumberFormatException e) {
                    threshValues.add("-9999");
                    threshValues.add("-9999");

                  }

                  intGroupIndex++;
                  listThreshValues.add(threshValues);
                  lengthRecords++;
                }
              } else if (listToken[6].equals("RELP")) {
                for (int i = 0; i < intNumGroups; i++) {
                  List<Object> threshValues = new ArrayList<>();
                  threshValues.add(lineDataIdStr);
                  threshValues.add(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      threshValues.add(res);
                    }

                  }

                  listThreshValues.add(threshValues);
                  lengthRecords++;
                }
              } else if (listToken[6].equals("ECON")) {

                for (int i = 0; i < intNumGroups; i++) {
                  List<Object> threshValues = new ArrayList<>();
                  threshValues.add(lineDataIdStr);
                  threshValues.add(i + 1);
                  for (int j = 0; j < intGroupSize; j++) {
                    double res = Double.parseDouble(listToken[intGroupIndex++]);
                    if (res != -9999) {
                      threshValues.add(X_POINTS_FOR_ECON[i]);
                      threshValues.add(res);
                    }

                  }
                  listThreshValues.add(threshValues);
                  lengthRecords++;
                }
              }

              mvLoadStatInsertData.getTableVarLengthValues()
                      .put(mvLoadStatInsertData.getLineType(), listThreshValues);
            }

            //  if the insert threshhold has been reached, commit the stored data to the database
            if (info.insertSize <= mvLoadStatInsertData.getListInsertValues().size()) {
              int[] listInserts = commitStatData(mvLoadStatInsertData);
              dataInserts += listInserts[INDEX_LINE_DATA];
              lengthInserts += listInserts[INDEX_VAR_LENGTH];
            }
          }
        } catch (NumberFormatException e) {
          logger.info("ERROR: line:" + line + " has errors and would be ignored.");
          logger.error(ERROR_MARKER, e.getMessage());
        }

      }  // end: while( reader.ready() )
      fileReader.close();
      reader.close();
    } catch (IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }

    //  commit all the remaining stored data
    int[] listInserts = commitStatData(mvLoadStatInsertData);
    dataInserts += listInserts[INDEX_LINE_DATA];
    lengthInserts += listInserts[INDEX_VAR_LENGTH];

    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerRecords", headerRecords);
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("dataInserts", dataInserts);
    timeStats.put("dataRecords", dataRecords);
    timeStats.put("lengthRecords", lengthRecords);
    timeStats.put("lengthInserts", lengthInserts);

    //  print a performance report
    long intStatHeaderLoadTime = System.currentTimeMillis() - intStatHeaderLoadStart;
    double dblLinesPerMSec = (double) (intLine - 1) / (double) (intStatHeaderLoadTime);

    if (info.verbose) {
      logger.info(MVUtil.padBegin("file lines: ", 6) + (intLine - 1) + "\n" +
              MVUtil.padBegin("stat_header records: ", 36) + headerRecords + "\n" +
              MVUtil.padBegin("stat_header inserts: ", 36) + headerInserts + "\n" +
              MVUtil.padBegin("line_data records: ", 36) + dataRecords + "\n" +
              MVUtil.padBegin("line_data inserts: ", 36) + dataInserts + "\n" +
              MVUtil.padBegin("line_data skipped: ", 36) + intLineDataSkipped + "\n" +
              MVUtil.padBegin("var length records: ", 36) + lengthRecords + "\n" +
              MVUtil.padBegin("var length inserts: ", 36) + lengthInserts + "\n" +
              MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
              intStatHeaderLoadTime) + "\n" +
              MVUtil.padBegin("stat_header search time: ", 36) + MVUtil.formatTimeSpan(
              headerSearchTime) + "\n" +
              MVUtil.padBegin("lines / msec: ", 36) + MVUtil.formatPerf(
              dblLinesPerMSec) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws DatabaseException
   */
  @Override
  public Map<String, Long> loadModeFile(DataFileInfo info) throws DatabaseException {
    Map<String, Long> timeStats = new HashMap<>();

    //  data structure for storing mode object ids
    Map<String, Integer> tableModeObjectId = new HashMap<>();

    //  performance counters
    long intModeHeaderLoadStart = System.currentTimeMillis();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long ctsInserts = 0;
    long objSingleInserts = 0;
    long objPairInserts = 0;

    //  get the next mode record ids from the database
    int intModeHeaderIdNext = getNextId("mode_header", "mode_header_id");
    int intModeObjIdNext = getNextId("mode_obj_single", "mode_obj_id");

    //  set up the input file for reading
    String strFilename = info.path + "/" + info.filename;
    int intLine = 1;
    List<String> headerNames = new ArrayList<>();
    try (
            FileReader fileReader = new FileReader(strFilename);
            BoundedBufferedReader reader = new BoundedBufferedReader(fileReader, MAX_LINES,
                    MAX_LINE_LEN)) {
      //  read each line of the input file
      while (reader.ready()) {
        String[] listToken = reader.readLineBounded().split("\\s+");
        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          intLine++;
          continue;
        }

        String strFileLine = strFilename + ":" + intLine;

        //  determine the line type
        int lineTypeLuId;
        int dataFileLuId = info.luId;
        String objectId = MVUtil.findValue(listToken, headerNames, "OBJECT_ID");
        Matcher matModeSingle = MVUtil.patModeSingleObjectId.matcher(objectId);
        Matcher matModePair = MVUtil.patModePairObjectId.matcher(objectId);
        int modeCts = 19;
        int modeSingle = 17;
        int modePair = 18;
        if (2 == dataFileLuId) {
          lineTypeLuId = modeCts;
        } else if (matModeSingle.matches()) {
          lineTypeLuId = modeSingle;
        } else if (matModePair.matches()) {
          lineTypeLuId = modePair;
        } else {
          throw new DatabaseException(
                  "METviewer load error: loadModeFile() unable to determine line type "
                          + MVUtil.findValue(listToken, headerNames, "OBJECT_ID")
                          + "\n        " + strFileLine);
        }


        /*
         * * * *  mode_header insert  * * * *
         */

        //  parse the valid times

        LocalDateTime fcstValidBeg = LocalDateTime.parse(
                MVUtil.findValue(listToken, headerNames, "FCST_VALID"),
                DB_DATE_STAT_FORMAT);


        LocalDateTime obsValidBeg = LocalDateTime.parse(
                MVUtil.findValue(listToken, headerNames, "OBS_VALID"),
                DB_DATE_STAT_FORMAT);

        //  format the valid times for the database insert
        String fcstValidBegStr = DATE_FORMATTER.format(fcstValidBeg);
        String obsValidBegStr = DATE_FORMATTER.format(obsValidBeg);


        //  calculate the number of seconds corresponding to fcst_lead
        String strFcstLead = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int intFcstLeadLen = strFcstLead.length();
        int intFcstLeadSec = Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 2, intFcstLeadLen));
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(intFcstLeadLen - 4, intFcstLeadLen - 2)) * 60;
        intFcstLeadSec += Integer.parseInt(strFcstLead.substring(0, intFcstLeadLen - 4)) * 3600;

        //  determine the init time by combining fcst_valid_beg and fcst_lead

        LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
        fcstInitBeg = fcstInitBeg.minusSeconds(intFcstLeadSec);

        String fcstInitStr = DATE_FORMATTER.format(fcstInitBeg);


        //  build a value list from the header information
        //replace "NA" for fcst_accum (listToken[4]) and obs_accum (listToken[7]) to NULL

        String modeHeaderValueList = "'" + MVUtil.findValue(listToken, headerNames, "VERSION")
                + "', '" + MVUtil.findValue(listToken, headerNames, "MODEL") + "', ";

        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  N_VALID
        } else {
          modeHeaderValueList = modeHeaderValueList + MVUtil.findValue(listToken, headerNames, "N_VALID")
                  + ", ";      //  N_VALID
        }
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  GRID_RES
        } else {
          modeHeaderValueList = modeHeaderValueList + MVUtil.findValue(listToken, headerNames, "GRID_RES")
                  + ", ";      //  GRID_RES
        }

        modeHeaderValueList = modeHeaderValueList
                + "'" + MVUtil.findValue(listToken, headerNames, "DESC") + "', "
                + MVUtil.findValue(listToken, headerNames, "FCST_LEAD") + ", "
                + "'" + fcstValidBegStr + "', ";      //  fcst_valid
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "FCST_ACCUM"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  fcst_accum
        } else {
          modeHeaderValueList = modeHeaderValueList + MVUtil.findValue(listToken, headerNames, "FCST_ACCUM")
                  + ", ";      //  fcst_accum
        }
        modeHeaderValueList = modeHeaderValueList + "'" + fcstInitStr + "', "
                + MVUtil.findValue(listToken, headerNames, "OBS_LEAD") + ", "
                + "'" + obsValidBegStr + "', ";
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "OBS_ACCUM"))) {
          modeHeaderValueList = modeHeaderValueList + "NULL" + ", ";      //  obs_accum
        } else {
          modeHeaderValueList = modeHeaderValueList + MVUtil.findValue(listToken, headerNames, "OBS_ACCUM")
                  + ", ";
        }
        modeHeaderValueList = modeHeaderValueList
                + MVUtil.findValue(listToken, headerNames, "FCST_RAD") + "', "     //  fcst_rad
                + MVUtil.findValue(listToken, headerNames, "FCST_THR") + "', "       //  fcst_thr
                + MVUtil.findValue(listToken, headerNames, "OBS_RAD") + ", " +     //  obs_rad
                "'" + MVUtil.findValue(listToken, headerNames, "OBS_THR") + "', " +      //  obs_thr
                "'" + MVUtil.findValue(listToken, headerNames, "FCST_VAR") + "', " +      //  fcst_units
                "'" + MVUtil.findValue(listToken, headerNames, "FCST_UNITS") + "', " +      //  fcst_var
                "'" + MVUtil.findValue(listToken, headerNames, "FCST_LEV") + "', " +      //  fcst_lev
                "'" + MVUtil.findValue(listToken, headerNames, "OBS_VAR") + "', " +      //  obs_var
                "'" + MVUtil.findValue(listToken, headerNames, "OBS_UNITS") + "', " +      //  obs_units
                "'" + MVUtil.findValue(listToken, headerNames, "OBS_LEV") + "'";        //  obs_lev
        String isnvalid = "=";
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
          isnvalid = "is";
        }
        String isgridres = "=";
        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
          isgridres = "is";
        }

        Integer fcstaccum = null;
        try {
          fcstaccum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "FCST_ACCUM"));
        } catch (NumberFormatException e) {
          logger.info("FCST_ACCUM " + MVUtil.findValue(listToken, headerNames, "FCST_ACCUM") + " is invalid");
        }
        String isfcstaccum = "=";
        if (fcstaccum == null) {
          isfcstaccum = "is";
        }

        Integer obsaccum = null;
        try {
          obsaccum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "OBS_ACCUM"));
        } catch (NumberFormatException e) {
          logger.info("OBS_ACCUM " + MVUtil.findValue(listToken, headerNames, "OBS_ACCUM") + " is invalid");
        }
        String isobsaccum = "=";
        if (obsaccum == null) {
          isobsaccum = "is";
        }

        String headerWhere = BINARY
                + " version = ?"
                + "  AND " + BINARY + " model = ?"
                + "  AND n_valid " + isnvalid + " ?"
                + "  AND grid_res " + isgridres + " ?"
                + "  AND " + BINARY + "descr = ?"
                + "  AND fcst_lead = ?"
                + "  AND fcst_valid = ?"
                + "  AND fcst_accum " + isfcstaccum + " ?"
                + "  AND fcst_init = ?"
                + "  AND obs_lead = ?"
                + "  AND obs_valid = ?"
                + "  AND obs_accum " + isobsaccum + " ?"
                + "  AND fcst_rad = ?"
                + "  AND " + BINARY + "fcst_thr = ?"
                + "  AND obs_rad = ?"
                + "  AND " + BINARY + "obs_thr = ?"
                + "  AND " + BINARY + "fcst_var = ?"
                + "  AND " + BINARY + "fcst_units = ?"
                + "  AND " + BINARY + "fcst_lev = ?"
                + "  AND " + BINARY + "obs_var = ?"
                + "  AND " + BINARY + "obs_units = ?"
                + "  AND " + BINARY + "obs_lev = ?";


        //  look for the header key in the table
        int modeHeaderId = -1;
        if (modeHeaders.containsKey(modeHeaderValueList)) {
          modeHeaderId = modeHeaders.get(modeHeaderValueList);
        }

        //  if the mode_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean foundModeHeader = false;
          long modeHeaderSearchBegin = System.currentTimeMillis();
          if (info.modeHeaderDBCheck) {
            String modeHeaderSelect = "SELECT mode_header_id FROM mode_header WHERE"
                    + headerWhere;
            ResultSet res = null;
            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(modeHeaderSelect)) {


              stmt.setString(1, MVUtil.findValue(listToken, headerNames, "VERSION"));
              stmt.setString(2, MVUtil.findValue(listToken, headerNames, "MODEL"));
              if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
                stmt.setNull(3, Types.INTEGER);
              } else {
                stmt.setObject(3, MVUtil.findValue(listToken, headerNames, "N_VALID"), Types.INTEGER);
              }
              if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
                stmt.setNull(4, Types.INTEGER);
              } else {
                stmt.setObject(4, MVUtil.findValue(listToken, headerNames, "GRID_RES"), Types.INTEGER);
              }
              stmt.setString(5, MVUtil.findValue(listToken, headerNames, "DESC"));
              stmt.setObject(6, MVUtil.findValue(listToken, headerNames, "FCST_LEAD"), Types.INTEGER);
              stmt.setObject(7, fcstValidBegStr, Types.TIMESTAMP);

              if (fcstaccum == null) {
                stmt.setNull(8, Types.INTEGER);
              } else {
                stmt.setInt(8, fcstaccum);
              }
              stmt.setObject(9, fcstInitStr, Types.TIMESTAMP);
              stmt.setObject(10, MVUtil.findValue(listToken, headerNames, "OBS_LEAD"), Types.INTEGER);
              stmt.setObject(11, obsValidBegStr, Types.TIMESTAMP);

              if (obsaccum == null) {
                stmt.setNull(12, Types.INTEGER);
              } else {
                stmt.setInt(12, obsaccum);
              }
              stmt.setObject(13, MVUtil.findValue(listToken, headerNames, "FCST_RAD"), Types.INTEGER);
              stmt.setString(14, MVUtil.findValue(listToken, headerNames, "FCST_THR"));
              stmt.setObject(15, MVUtil.findValue(listToken, headerNames, "OBS_RAD"), Types.INTEGER);
              stmt.setString(16, MVUtil.findValue(listToken, headerNames, "OBS_THR"));
              stmt.setString(17, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(18, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(19, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(20, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(21, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(22, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));

              res = stmt.executeQuery();
              if (res.next()) {
                String modeHeaderIdDup = res.getString(1);
                modeHeaderId = Integer.parseInt(modeHeaderIdDup);
                foundModeHeader = true;
                logger.warn(
                        "  **  WARNING: found duplicate mode_header record with id " + modeHeaderIdDup
                                + "\n        " + strFileLine);
              }
            } catch (SQLException e) {
              logger.error(ERROR_MARKER, e.getMessage());
            } finally {
              if (res != null) {
                try {
                  res.close();
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
            }

          }
          timeStats.put("headerSearchTime",
                  timeStats.get("headerSearchTime") + System.currentTimeMillis() - modeHeaderSearchBegin);


          //  if the mode_header was not found, add it to the table
          if (!foundModeHeader) {

            modeHeaderId = intModeHeaderIdNext++;
            modeHeaders.put(modeHeaderValueList, modeHeaderId);


            //  insert the record into the mode_header database table
            String sql = "INSERT INTO mode_header VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int modeHeaderInsertCount;
            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {
              stmt.setInt(1, modeHeaderId);
              stmt.setInt(2, lineTypeLuId);
              stmt.setInt(3, info.fileId);
              stmt.setInt(4, intLine);
              stmt.setString(5, MVUtil.findValue(listToken, headerNames, "VERSION"));
              stmt.setString(6, MVUtil.findValue(listToken, headerNames, "MODEL"));
              if ("NA".equals(MVUtil.findValue(listToken, headerNames, "N_VALID"))) {
                stmt.setNull(7, Types.INTEGER);
              } else {
                stmt.setObject(7, MVUtil.findValue(listToken, headerNames, "N_VALID"), Types.INTEGER);
              }
              if ("NA".equals(MVUtil.findValue(listToken, headerNames, "GRID_RES"))) {
                stmt.setNull(8, Types.INTEGER);
              } else {
                stmt.setObject(8, MVUtil.findValue(listToken, headerNames, "GRID_RES"), Types.INTEGER);
              }
              stmt.setString(9, MVUtil.findValue(listToken, headerNames, "DESC"));
              stmt.setObject(10, MVUtil.findValue(listToken, headerNames, "FCST_LEAD"), Types.INTEGER);
              stmt.setObject(11, fcstValidBegStr, Types.TIMESTAMP);
              Integer accum = null;
              try {
                accum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "FCST_ACCUM"));
              } catch (NumberFormatException e) {
                logger.info("FCST_ACCUM " + MVUtil.findValue(listToken, headerNames, "FCST_ACCUM") + " is invalid");
              }
              if (accum == null) {
                stmt.setNull(12, Types.INTEGER);
              } else {
                stmt.setInt(12, accum);
              }
              stmt.setObject(13, fcstInitStr, Types.TIMESTAMP);
              stmt.setObject(14, MVUtil.findValue(listToken, headerNames, "OBS_LEAD"), Types.INTEGER);
              stmt.setObject(15, obsValidBegStr, Types.TIMESTAMP);
              try {
                accum = Integer.valueOf(MVUtil.findValue(listToken, headerNames, "OBS_ACCUM"));
              } catch (NumberFormatException e) {
                logger.info("OBS_ACCUM " + MVUtil.findValue(listToken, headerNames, "OBS_ACCUM") + " "
                        + "is invalid");
              }
              if (accum == null) {
                stmt.setNull(16, Types.INTEGER);
              } else {
                stmt.setInt(16, accum);
              }
              stmt.setObject(17, MVUtil.findValue(listToken, headerNames, "FCST_RAD"), Types.INTEGER);
              stmt.setString(18, MVUtil.findValue(listToken, headerNames, "FCST_THR"));
              stmt.setObject(19, MVUtil.findValue(listToken, headerNames, "OBS_RAD"), Types.INTEGER);
              stmt.setString(20, MVUtil.findValue(listToken, headerNames, "OBS_THR"));
              stmt.setString(21, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(22, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(23, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(24, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(25, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(26, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));

              modeHeaderInsertCount = stmt.executeUpdate();
            } catch (SQLException se) {
              logger.error(ERROR_MARKER, se.getMessage());
              throw new DatabaseException(
                      "caught SQLException calling executeUpdate: " + se.getMessage());
            }

            if (1 != modeHeaderInsertCount) {
              logger.warn("  **  WARNING: unexpected result from mode_header INSERT: "
                      + modeHeaderInsertCount + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


        /*
         * * * *  mode_cts insert  * * * *
         */

        if (modeCts == lineTypeLuId) {

          int totalIndex = headerNames.indexOf("TOTAL");

          String modeCtsInsert = tableToInsert.get("mode_cts");
          int modeCtsInsertCount;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(modeCtsInsert)) {
            stmt.setInt(1, modeHeaderId);
            stmt.setString(2, MVUtil.findValue(listToken, headerNames, "FIELD"));
            stmt.setObject(3, replaceInvalidValues(listToken[totalIndex]), Types.INTEGER);
            stmt.setObject(4, replaceInvalidValues(listToken[totalIndex + 1]), Types.INTEGER);
            stmt.setObject(5, replaceInvalidValues(listToken[totalIndex + 2]), Types.INTEGER);
            stmt.setObject(6, replaceInvalidValues(listToken[totalIndex + 3]), Types.INTEGER);
            stmt.setObject(7, replaceInvalidValues(listToken[totalIndex + 4]), Types.INTEGER);
            stmt.setObject(8, replaceInvalidValues(listToken[totalIndex + 5]), Types.DOUBLE);
            stmt.setObject(9, replaceInvalidValues(listToken[totalIndex + 6]), Types.DOUBLE);
            stmt.setObject(10, replaceInvalidValues(listToken[totalIndex + 7]), Types.DOUBLE);
            stmt.setObject(11, replaceInvalidValues(listToken[totalIndex + 8]), Types.DOUBLE);
            stmt.setObject(12, replaceInvalidValues(listToken[totalIndex + 9]), Types.DOUBLE);
            stmt.setObject(13, replaceInvalidValues(listToken[totalIndex + 10]), Types.DOUBLE);
            stmt.setObject(14, replaceInvalidValues(listToken[totalIndex + 11]), Types.DOUBLE);
            stmt.setObject(15, replaceInvalidValues(listToken[totalIndex + 12]), Types.DOUBLE);
            stmt.setObject(16, replaceInvalidValues(listToken[totalIndex + 13]), Types.DOUBLE);
            stmt.setObject(17, replaceInvalidValues(listToken[totalIndex + 14]), Types.DOUBLE);
            stmt.setObject(18, replaceInvalidValues(listToken[totalIndex + 15]), Types.DOUBLE);
            stmt.setObject(19, replaceInvalidValues(listToken[totalIndex + 16]), Types.DOUBLE);
            stmt.setObject(20, replaceInvalidValues(listToken[totalIndex + 17]), Types.DOUBLE);
            modeCtsInsertCount = stmt.executeUpdate();

          } catch (SQLException se) {
            logger.error(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }

          if (1 != modeCtsInsertCount) {
            logger.warn("  **  WARNING: unexpected result from mode_cts INSERT: "
                    + modeCtsInsertCount + "\n        " + strFileLine);
          }
          ctsInserts++;

        }

        /*
         * * * *  mode_obj_single insert  * * * *
         */

        else if (modeSingle == lineTypeLuId) {

          //  build the value list for the mode_cts insert
          int modeObjId = intModeObjIdNext++;


          //set flags
          int simpleFlag = 1;
          int fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          int matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT").split("_");
          if (objCatArr.length == 1 && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }

          String modeObjSingleInsert = tableToInsert.get("mode_obj_single");
          int intModeObjSingleInsert;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(modeObjSingleInsert)) {
            stmt.setInt(1, modeObjId);
            stmt.setInt(2, modeHeaderId);
            stmt.setString(3, objectId);
            stmt.setString(4, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[0]));
            stmt.setObject(5, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[1]),
                    Types.DOUBLE);
            stmt.setObject(6, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[2]),
                    Types.DOUBLE);
            stmt.setObject(7, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[3]),
                    Types.DOUBLE);
            stmt.setObject(8, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[4]),
                    Types.DOUBLE);
            stmt.setObject(9, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[5]),
                    Types.DOUBLE);
            stmt.setObject(10, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[6]),
                    Types.DOUBLE);
            stmt.setObject(11, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[7]),
                    Types.DOUBLE);

            stmt.setObject(12, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[8]),
                    Types.INTEGER);
            stmt.setObject(13, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[9]),
                    Types.INTEGER);

            stmt.setObject(14, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[10]),
                    Types.DOUBLE);
            stmt.setObject(15, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[11]),
                    Types.DOUBLE);
            stmt.setObject(16, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[12]),
                    Types.DOUBLE);
            stmt.setObject(17, MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[13]),
                    Types.DOUBLE);

            String value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[14]);
            if ("NA".equals(value)) {
              stmt.setNull(18, Types.DOUBLE);
            } else {
              stmt.setObject(18, value, Types.DOUBLE);
            }

            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[15]);
            if ("NA".equals(value)) {
              stmt.setNull(19, Types.DOUBLE);
            } else {
              stmt.setObject(19, value, Types.DOUBLE);
            }

            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[16]);
            if ("NA".equals(value)) {
              stmt.setNull(20, Types.DOUBLE);
            } else {
              stmt.setObject(20, value, Types.DOUBLE);
            }

            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[17]);
            if ("NA".equals(value)) {
              stmt.setNull(21, Types.DOUBLE);
            } else {
              stmt.setObject(21, value, Types.DOUBLE);
            }

            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[18]);
            if ("NA".equals(value)) {
              stmt.setNull(22, Types.DOUBLE);
            } else {
              stmt.setObject(22, value, Types.DOUBLE);
            }
            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[19]);
            if ("NA".equals(value)) {
              stmt.setNull(23, Types.DOUBLE);
            } else {
              stmt.setObject(23, value, Types.DOUBLE);
            }

            value = MVUtil.findValue(listToken, headerNames, modeObjSingleColumns[20]);
            if ("NA".equals(value)) {
              stmt.setNull(24, Types.DOUBLE);
            } else {
              stmt.setObject(24, value, Types.DOUBLE);
            }

            stmt.setInt(25, fcstFlag);
            stmt.setInt(26, simpleFlag);
            stmt.setInt(27, matchedFlag);
            intModeObjSingleInsert = stmt.executeUpdate();

          } catch (SQLException se) {
            logger.error(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }


          if (1 != intModeObjSingleInsert) {
            logger.warn(
                    "  **  WARNING: unexpected result from mode_obj_single INSERT: "
                            + intModeObjSingleInsert + "\n        " + strFileLine);
          }
          objSingleInserts++;

          //  add the mode_obj_id to the table, using the object_id as the key
          tableModeObjectId.put(objectId, modeObjId);

        }

        /*
         * * * *  mode_obj_pair insert  * * * *
         */

        else if (modePair == lineTypeLuId) {

          //  determine the mode_obj_id values for the pair
          int modeObjectIdFcst = tableModeObjectId.get(matModePair.group(1));
          int modeObjectIdObs = tableModeObjectId.get(matModePair.group(2));

          //set flags
          int simpleFlag = 1;
          String[] objIdArr = objectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          int matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT")
                  .split("_");
          if (objCatArr.length == 2 && objCatArr[0].substring(2).equals(objCatArr[1].substring(2))
                  && !objCatArr[0].substring(2).equals("000")) {
            matchedFlag = 1;
          }

          //  insert the record into the mode_obj_pair database table
          String modeObjPairInsert = tableToInsert.get("mode_obj_pair");
          int intModeObjPairInsert;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(modeObjPairInsert)) {
            stmt.setInt(1, modeObjectIdObs);
            stmt.setInt(2, modeObjectIdFcst);
            stmt.setInt(3, modeHeaderId);
            stmt.setString(4, objectId);
            stmt.setString(5, MVUtil.findValue(listToken, headerNames, "OBJECT_CAT"));
            stmt.setObject(6, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "CENTROID_DIST")), Types.DOUBLE);
            stmt.setObject(7, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "BOUNDARY_DIST")), Types.DOUBLE);
            stmt.setObject(8, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "CONVEX_HULL_DIST")), Types.DOUBLE);
            stmt.setObject(9, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "ANGLE_DIFF")), Types.DOUBLE);
            stmt.setObject(10, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "ASPECT_DIFF")), Types.DOUBLE);
            stmt.setObject(11, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "AREA_RATIO")), Types.DOUBLE);
            stmt.setObject(12, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "INTERSECTION_AREA")), Types.INTEGER);
            stmt.setObject(13, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "UNION_AREA")), Types.INTEGER);
            stmt.setObject(14, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "SYMMETRIC_DIFF")), Types.INTEGER);
            stmt.setObject(15, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "INTERSECTION_OVER_AREA")), Types.DOUBLE);
            stmt.setObject(16, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "CURVATURE_RATIO")), Types.DOUBLE);
            stmt.setObject(17, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "COMPLEXITY_RATIO")), Types.DOUBLE);
            stmt.setObject(18, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "PERCENTILE_INTENSITY_RATIO")), Types.DOUBLE);
            stmt.setObject(19, replaceInvalidValues(
                    MVUtil.findValue(listToken, headerNames, "INTEREST")), Types.DOUBLE);
            stmt.setInt(20, simpleFlag);
            stmt.setInt(21, matchedFlag);

            intModeObjPairInsert = stmt.executeUpdate();

          } catch (SQLException se) {
            logger.error(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }
          if (1 != intModeObjPairInsert) {
            logger.warn("  **  WARNING: unexpected result from mode_obj_pair INSERT: "
                    + intModeObjPairInsert + "\n        " + strFileLine);
          }
          objPairInserts++;

        }

        intLine++;
      }
      fileReader.close();
      reader.close();

    } catch (IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }

    //  increment the global mode counters
    timeStats.put("linesTotal", (long) (intLine - 1));
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("ctsInserts", ctsInserts);
    timeStats.put("objSingleInserts", objSingleInserts);
    timeStats.put("objPairInserts", objPairInserts);


    //  print a performance report
    if (info.verbose) {
      long intModeHeaderLoadTime = System.currentTimeMillis() - intModeHeaderLoadStart;
      logger.info(MVUtil.padBegin("mode_header inserts: ", 36) + headerInserts + "\n" +
              MVUtil.padBegin("mode_cts inserts: ", 36) + ctsInserts + "\n" +
              MVUtil.padBegin("mode_obj_single inserts: ", 36) + objSingleInserts + "\n" +
              MVUtil.padBegin("mode_obj_pair inserts: ", 36) + objPairInserts + "\n" +
              (info.modeHeaderDBCheck ? MVUtil.padBegin("mode_header search time: ",
                      36) + MVUtil.formatTimeSpan(
                      timeStats.get("headerSearchTime")) + "\n" : "") +
              MVUtil.padBegin("total load time: ", 36) + MVUtil.formatTimeSpan(
              intModeHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  /**
   * Load the MET output data from the data file underlying the input DataFileInfo object into the
   * database underlying the input Connection. The header information can be checked in two
   * different ways: using a table for the current file (specified by _boolModeHeaderTableCheck).
   * Records in mode_obj_pair tables, mode_obj_single tables and mode_cts tables are created from
   * the data in the input file.  If necessary, records in the mode_header table are created.
   *
   * @param info Contains MET output data file information //* @param con Connection to the target
   *             database
   * @throws DatabaseException
   */
  @Override
  public Map<String, Long> loadMtdFile(DataFileInfo info) throws DatabaseException {
    Map<String, Long> timeStats = new HashMap<>();

    //  performance counters
    long intMtdHeaderLoadStart = System.currentTimeMillis();
    timeStats.put("headerSearchTime", 0L);
    long headerInserts = 0;
    long obj3dSingleInserts = 0;
    long obj3dPairInserts = 0;
    long obj2dInserts = 0;

    //  get the next mode record ids from the database
    int intMtdHeaderIdNext = getNextId("mtd_header", "mtd_header_id");

    //  set up the input file for reading
    String filename = info.path + "/" + info.filename;
    int line = 1;
    List<String> headerNames = new ArrayList<>();
    try (
            FileReader fileReader = new FileReader(filename);
            BoundedBufferedReader reader = new BoundedBufferedReader(fileReader, MAX_LINES,
                    MAX_LINE_LEN)) {
      //  read each line of the input file
      while (reader.ready()) {
        String lineStr = reader.readLineBounded().trim();
        String[] listToken = lineStr.split("\\s+");

        //  the first line is the header line
        if (1 > listToken.length || listToken[0].equals("VERSION")) {
          headerNames = Arrays.asList(listToken);
          line++;
          continue;
        }

        String strFileLine = filename + ":" + line;

        //  determine the line type
        int lineTypeLuId;
        int dataFileLuId = info.luId;
        String objectId = MVUtil.findValue(listToken, headerNames, "OBJECT_ID");
        int mtd3dSingle = 17;
        int mtd3dPair = 18;
        int mtd2d = 19;
        if (11 == dataFileLuId || 12 == dataFileLuId) {
          lineTypeLuId = mtd3dSingle;
        } else if (9 == dataFileLuId || 10 == dataFileLuId) {
          lineTypeLuId = mtd3dPair;
        } else if (8 == dataFileLuId) {
          lineTypeLuId = mtd2d;
        } else {
          throw new DatabaseException("METviewer load error: loadModeFile() unable to determine "
                  + "line "
                  + "type"
                  + " " + strFileLine);
        }
        //  parse the valid times

        LocalDateTime fcstValidBeg;
        try {
          fcstValidBeg = LocalDateTime.parse(
                  MVUtil.findValue(listToken, headerNames, "FCST_VALID"),
                  DB_DATE_STAT_FORMAT);
        } catch (DateTimeParseException e) {
          fcstValidBeg = null;
        }

        LocalDateTime obsValidBeg;
        try {
          obsValidBeg = LocalDateTime.parse(
                  MVUtil.findValue(listToken, headerNames, "OBS_VALID"),
                  DB_DATE_STAT_FORMAT);
        } catch (DateTimeParseException e) {
          obsValidBeg = null;
        }

        //  format the valid times for the database insert
        String fcstValidBegStr;
        if (fcstValidBeg != null) {
          fcstValidBegStr = DATE_FORMATTER.format(fcstValidBeg);
        } else {
          fcstValidBegStr = null;
        }


        String obsValidBegStr;
        if (obsValidBeg == null) {
          obsValidBegStr = null;
        } else {
          obsValidBegStr = DATE_FORMATTER.format(obsValidBeg);
        }


        //  calculate the number of seconds corresponding to fcst_lead
        String fcstLead = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        int fcstLeadLen = fcstLead.length();
        int fcstLeadSec = 0;
        try {
          fcstLeadSec = Integer.parseInt(
                  fcstLead.substring(fcstLeadLen - 2, fcstLeadLen));
          fcstLeadSec += Integer.parseInt(
                  fcstLead.substring(fcstLeadLen - 4, fcstLeadLen - 2)) * 60;
          fcstLeadSec += Integer.parseInt(fcstLead.substring(fcstLeadLen - 6,
                  fcstLeadLen - 4)) * 3600;
        } catch (NumberFormatException e) {
          logger.debug("fcstLead " + fcstLead + " is invalid");
        }
        String fcstLeadInsert = MVUtil.findValue(listToken, headerNames, "FCST_LEAD");
        if (fcstLeadInsert.equals("NA")) {
          fcstLeadInsert = "-9999";
        } else {
          if (fcstLeadInsert.contains("_")) {
            fcstLeadInsert = fcstLeadInsert.split("_")[1];
          }
        }

        String obsLeadInsert = MVUtil.findValue(listToken, headerNames, "OBS_LEAD");
        if (obsLeadInsert.equals("NA")) {
          obsLeadInsert = "-9999";
        } else {
          if (obsLeadInsert.contains("_")) {
            obsLeadInsert = obsLeadInsert.split("_")[1];
          }
        }

        //  determine the init time by combining fcst_valid_beg and fcst_lead
        String fcstInitStr;
        if (fcstValidBeg != null) {
          LocalDateTime fcstInitBeg = LocalDateTime.from(fcstValidBeg);
          fcstInitBeg = fcstInitBeg.minusSeconds(fcstLeadSec);
          fcstInitStr = DATE_FORMATTER.format(fcstInitBeg);
        } else {
          fcstInitStr = null;
        }


        String mtdHeaderValueList = "'" + MVUtil.findValue(listToken, headerNames, "VERSION")
                + "', " + "'"
                + MVUtil.findValue(listToken, headerNames, "MODEL")
                + "', " + "'"
                + MVUtil.findValue(listToken, headerNames, "DESC")
                + "', ";


        mtdHeaderValueList = mtdHeaderValueList
                + fcstLeadInsert
                + ", " + "'" + fcstValidBegStr + "', "
                + "'" + fcstInitStr + "', "
                + obsLeadInsert
                + ", " + "'" + obsValidBegStr + "', ";

        if ("NA".equals(MVUtil.findValue(listToken, headerNames, "T_DELTA"))) {
          mtdHeaderValueList = mtdHeaderValueList + "NULL" + ", ";
        } else {
          mtdHeaderValueList = mtdHeaderValueList
                  + MVUtil.findValue(listToken, headerNames, "T_DELTA")
                  + ", ";
        }
        mtdHeaderValueList = mtdHeaderValueList
                + MVUtil.findValue(listToken, headerNames, "FCST_RAD") + ", "
                + "'" + MVUtil.findValue(listToken, headerNames, "FCST_THR") + "', "
                + MVUtil.findValue(listToken, headerNames, "OBS_RAD") + ", "
                + "'" + MVUtil.findValue(listToken, headerNames, "OBS_THR") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "FCST_VAR") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "FCST_UNITS") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "FCST_LEV") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "OBS_VAR") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "OBS_UNITS") + "', "
                + "'" + MVUtil.findValue(listToken, headerNames, "OBS_LEV") + "'";


        String mtdHeaderWhereClause = BINARY +
                "  version = ?"
                + "  AND " + BINARY + "model = ?"
                + "  AND " + BINARY + "descr = ?"
                + "  AND fcst_lead = ?"
                + "  AND fcst_valid = ?"
                + "  AND t_delta = ?"
                + "  AND fcst_init = ?"
                + "  AND obs_lead = ?"
                + "  AND obs_valid = ?"
                + "  AND fcst_rad = ?"
                + "  AND " + BINARY + "fcst_thr = ?"
                + "  AND obs_rad = ?"
                + "  AND " + BINARY + "obs_thr = ?"
                + "  AND " + BINARY + "fcst_var = ?"
                + "  AND " + BINARY + "fcst_units = ?"
                + "  AND " + BINARY + "fcst_lev = ?"
                + "  AND " + BINARY + "obs_var = ?"
                + "  AND " + BINARY + "obs_units = ?"
                + "  AND " + BINARY + "obs_lev = ?";

        //  look for the header key in the table
        int mtdHeaderId = -1;
        if (mtdHeaders.containsKey(mtdHeaderValueList)) {
          mtdHeaderId = mtdHeaders.get(mtdHeaderValueList);
        }

        //  if the mtd_header does not yet exist, create one
        else {

          //  look for an existing mode_header record with the same information
          boolean foundMtdHeader = false;
          long mtdHeaderSearchBegin = System.currentTimeMillis();
          if (info.mtdHeaderDBCheck) {
            String strMtdHeaderSelect = "SELECT mtd_header_id FROM mtd_header WHERE" +
                    mtdHeaderWhereClause;
            ResultSet res = null;
            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(strMtdHeaderSelect)
            ) {
              stmt.setString(1, MVUtil.findValue(listToken, headerNames, "VERSION"));
              stmt.setString(2, MVUtil.findValue(listToken, headerNames,
                      "MODEL"));
              stmt.setString(3, MVUtil.findValue(listToken, headerNames,
                      "DESC"));
              stmt.setObject(4, fcstLeadInsert, Types.INTEGER);
              stmt.setObject(5, fcstValidBegStr, Types.TIMESTAMP);
              stmt.setObject(6, MVUtil.findValue(listToken, headerNames, "T_DELTA"), Types.INTEGER);
              stmt.setObject(7, fcstInitStr, Types.TIMESTAMP);
              stmt.setObject(8, obsLeadInsert, Types.INTEGER);
              stmt.setObject(9, obsValidBegStr, Types.TIMESTAMP);
              stmt.setObject(10, MVUtil.findValue(listToken, headerNames, "FCST_RAD"), Types.INTEGER);
              stmt.setString(11, MVUtil.findValue(listToken, headerNames, "FCST_THR"));
              stmt.setObject(12, MVUtil.findValue(listToken, headerNames, "OBS_RAD"), Types.INTEGER);
              stmt.setString(13, MVUtil.findValue(listToken, headerNames, "OBS_THR"));
              stmt.setString(14, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(15, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(16, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(17, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(18, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(19, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));


              res = stmt.executeQuery();
              if (res.next()) {
                String strMtdHeaderIdDup = res.getString(1);
                mtdHeaderId = Integer.parseInt(strMtdHeaderIdDup);
                foundMtdHeader = true;
                logger.warn("  **  WARNING: found duplicate mtd_header record with id " +
                        strMtdHeaderIdDup + "\n        " + strFileLine);
              }
            } catch (NumberFormatException | SQLException e) {
              logger.error(ERROR_MARKER, e.getMessage());
            } finally {
              if (res != null) {
                try {
                  res.close();
                } catch (SQLException e) {
                  logger.error(ERROR_MARKER, e.getMessage());
                }
              }
            }

          }
          timeStats.put("headerSearchTime",
                  timeStats.get("headerSearchTime")
                          + System.currentTimeMillis() - mtdHeaderSearchBegin);


          //  if the mtd_header was not found, add it to the table
          if (!foundMtdHeader) {

            mtdHeaderId = intMtdHeaderIdNext++;
            mtdHeaders.put(mtdHeaderValueList, mtdHeaderId);


            //  insert the record into the mtd_header database table
            String sql = "INSERT INTO mtd_header VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int mtdHeaderInsert;
            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {
              stmt.setInt(1, mtdHeaderId);
              stmt.setInt(2, lineTypeLuId);
              stmt.setInt(3, info.fileId);
              stmt.setInt(4, line);
              stmt.setString(5, MVUtil.findValue(listToken, headerNames, "VERSION"));
              stmt.setString(6, MVUtil.findValue(listToken, headerNames, "MODEL"));
              stmt.setString(7, MVUtil.findValue(listToken, headerNames, "DESC"));
              stmt.setObject(8, fcstLeadInsert, Types.INTEGER);
              stmt.setObject(9, fcstValidBegStr, Types.TIMESTAMP);
              stmt.setObject(10, fcstInitStr, Types.TIMESTAMP);
              stmt.setObject(11, obsLeadInsert, Types.INTEGER);
              stmt.setObject(12, obsValidBegStr, Types.TIMESTAMP);
              if ("NA".equals(MVUtil.findValue(listToken, headerNames, "T_DELTA"))) {
                stmt.setNull(13, Types.INTEGER);
              } else {
                stmt.setObject(13, MVUtil.findValue(listToken, headerNames, "T_DELTA"), Types.INTEGER);
              }
              stmt.setObject(14, MVUtil.findValue(listToken, headerNames, "FCST_RAD"), Types.INTEGER);
              stmt.setString(15, MVUtil.findValue(listToken, headerNames, "FCST_THR"));
              stmt.setObject(16, MVUtil.findValue(listToken, headerNames, "OBS_RAD"), Types.INTEGER);
              stmt.setString(17, MVUtil.findValue(listToken, headerNames, "OBS_THR"));
              stmt.setString(18, MVUtil.findValue(listToken, headerNames, "FCST_VAR"));
              stmt.setString(19, MVUtil.findValue(listToken, headerNames, "FCST_UNITS"));
              stmt.setString(20, MVUtil.findValue(listToken, headerNames, "FCST_LEV"));
              stmt.setString(21, MVUtil.findValue(listToken, headerNames, "OBS_VAR"));
              stmt.setString(22, MVUtil.findValue(listToken, headerNames, "OBS_UNITS"));
              stmt.setString(23, MVUtil.findValue(listToken, headerNames, "OBS_LEV"));
              mtdHeaderInsert = stmt.executeUpdate();

            } catch (SQLException se) {
              logger.error(ERROR_MARKER, se.getMessage());
              throw new DatabaseException("caught SQLException calling executeUpdate: " + se.getMessage());
            }

            if (1 != mtdHeaderInsert) {
              logger.warn("  **  WARNING: unexpected result from mtd_header INSERT: " + mtdHeaderInsert
                      + "\n        " + strFileLine);
            }
            headerInserts++;
          }
        }


        if (mtd3dSingle == lineTypeLuId) {
          String sql = "INSERT INTO mtd_3d_obj_single VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


          //set flags
          int simpleFlag = 1;
          int fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          int matchedFlag = 0;
          String objCat = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT");
          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (NumberFormatException e) {
            logger.info(" objCat " + objCat + " is invalid");
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }


          //  insert the record into the mtd_obj_single database table
          int mtd3dObjSingleInsert;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, mtdHeaderId);
            stmt.setString(2, objectId);
            stmt.setString(3, replaceInvalidValues(MVUtil.findValue(listToken, headerNames,
                    mtdObj3dSingleColumns[0])));

            for (int i = 0; i < 17; i++) {
              stmt.setObject(4 + i,
                      replaceInvalidValues(MVUtil.findValue(listToken, headerNames, mtdObj3dSingleColumns[i + 1])),
                      Types.DOUBLE);
            }
            stmt.setInt(21, fcstFlag);
            stmt.setInt(22, simpleFlag);
            stmt.setInt(23, matchedFlag);

            mtd3dObjSingleInsert = stmt.executeUpdate();

          } catch (SQLException se) {
            logger.error(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }


          if (1 != mtd3dObjSingleInsert) {
            logger.warn(
                    "  **  WARNING: unexpected result from mtd_3d_obj_single INSERT: "
                            + mtd3dObjSingleInsert + "\n        " + strFileLine);
          }
          obj3dSingleInserts++;
        } else if (mtd2d == lineTypeLuId) {

          //set flags
          int simpleFlag = 1;
          int fcstFlag = 0;
          if (objectId.startsWith("C")) {
            simpleFlag = 0;
          }
          if (objectId.startsWith("CF") || objectId.startsWith("F")) {
            fcstFlag = 1;
          }
          int matchedFlag = 0;
          String objCat = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT");

          Integer num = null;
          try {
            num = Integer.valueOf(objCat.substring(objCat.length() - 3));
          } catch (NumberFormatException e) {
            logger.info("objCat " + objCat + " is invalid");
          }
          if (num != null && num != 0) {
            matchedFlag = 1;
          }

          //  insert the record into the mtd_obj_single database table
          String sql = "INSERT INTO mtd_2d_obj VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
          int mtd2dObjInsert;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, mtdHeaderId);
            stmt.setString(2, objectId);
            stmt.setString(3, replaceInvalidValues(MVUtil.findValue(listToken, headerNames,
                    mtdObj2dColumns[0])));
            for (int i = 0; i < 12; i++) {
              stmt.setObject(4 + i,
                      replaceInvalidValues(MVUtil.findValue(listToken, headerNames, mtdObj2dColumns[i + 1])),
                      Types.DOUBLE);
            }
            stmt.setInt(16, fcstFlag);
            stmt.setInt(17, simpleFlag);
            stmt.setInt(18, matchedFlag);

            mtd2dObjInsert = stmt.executeUpdate();
          } catch (SQLException se) {
            logger.error(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }

          if (1 != mtd2dObjInsert) {
            logger.warn(
                    "  **  WARNING: unexpected result from mtd_2d_obj INSERT: "
                            + mtd2dObjInsert + "\n        " + strFileLine);
          }
          obj2dInserts++;
        } else if (mtd3dPair == lineTypeLuId) {

          //  build the value list for the mode_cts insert

          String sql = "INSERT INTO mtd_3d_obj_pair VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

          int spaceCentroidDistIndex = headerNames.indexOf("SPACE_CENTROID_DIST");


          //set flags
          int simpleFlag = 1;
          String[] objIdArr = objectId.split("_");
          if (objIdArr.length == 2 && objIdArr[0].startsWith("C") && objIdArr[1].startsWith("C")) {
            simpleFlag = 0;
          }

          int matchedFlag = 0;
          String[] objCatArr = MVUtil.findValue(listToken, headerNames, "OBJECT_CAT")
                  .split("_");
          Integer num1;
          Integer num2;
          try {
            num1 = Integer.valueOf(objCatArr[0].substring(objCatArr[0].length() - 3));
            num2 = Integer.valueOf(objCatArr[1].substring(objCatArr[1].length() - 3));
            if (num1.equals(num2) && num1 != 0) {
              matchedFlag = 1;
            }
          } catch (NumberFormatException e) {
            logger.info("objCatArr is invalid");
          }


          int mtd3dObjPairInsert;
          try (Connection con = getConnection();
               PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, mtdHeaderId);
            stmt.setString(2, objectId);
            stmt.setString(3, MVUtil.findValue(listToken, headerNames, "OBJECT_CAT"));
            stmt.setObject(4, replaceInvalidValues(listToken[spaceCentroidDistIndex]),
                    Types.DOUBLE);
            stmt.setObject(5, replaceInvalidValues(listToken[spaceCentroidDistIndex + 1]),
                    Types.DOUBLE);
            stmt.setObject(6, replaceInvalidValues(listToken[spaceCentroidDistIndex + 2]),
                    Types.DOUBLE);
            stmt.setObject(7, replaceInvalidValues(listToken[spaceCentroidDistIndex + 3]),
                    Types.DOUBLE);
            stmt.setObject(8, replaceInvalidValues(listToken[spaceCentroidDistIndex + 4]),
                    Types.DOUBLE);
            stmt.setObject(9, replaceInvalidValues(listToken[spaceCentroidDistIndex + 5]),
                    Types.DOUBLE);
            stmt.setObject(10, replaceInvalidValues(listToken[spaceCentroidDistIndex + 6]),
                    Types.DOUBLE);
            stmt.setObject(11, replaceInvalidValues(listToken[spaceCentroidDistIndex + 7]),
                    Types.DOUBLE);
            stmt.setObject(12, replaceInvalidValues(listToken[spaceCentroidDistIndex + 8]),
                    Types.DOUBLE);
            stmt.setObject(13, replaceInvalidValues(listToken[spaceCentroidDistIndex + 9]),
                    Types.DOUBLE);
            stmt.setObject(14, replaceInvalidValues(listToken[spaceCentroidDistIndex + 10]),
                    Types.DOUBLE);
            stmt.setInt(15, simpleFlag);
            stmt.setInt(16, matchedFlag);
            mtd3dObjPairInsert = stmt.executeUpdate();
          } catch (SQLException se) {
            logger.info(ERROR_MARKER, se.getMessage());
            throw new DatabaseException(
                    "caught SQLException calling executeUpdate: " + se.getMessage());
          }

          if (1 != mtd3dObjPairInsert) {
            logger.warn(
                    "  **  WARNING: unexpected result from mtd_3d_obj_pair INSERT: " +
                            mtd3dObjPairInsert + "\n        " + strFileLine);
          }
          obj3dPairInserts++;

        }

        line++;
      }
      fileReader.close();
      reader.close();
    } catch (IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }

    //  increment the global mode counters
    timeStats.put("linesTotal", (long) (line - 1));
    timeStats.put("headerInserts", headerInserts);
    timeStats.put("obj3dSingleInserts", obj3dSingleInserts);
    timeStats.put("obj3dPairInserts", obj3dPairInserts);
    timeStats.put("obj2dInserts", obj2dInserts);


    //  print a performance report
    if (info.verbose) {
      long intMtdHeaderLoadTime = System.currentTimeMillis() - intMtdHeaderLoadStart;
      logger.info(
              MVUtil.padBegin("mtd_header inserts: ", 36)
                      + headerInserts + "\n"
                      + MVUtil.padBegin("mtd_3d_obj_single inserts: ", 36)
                      + obj3dSingleInserts + "\n"
                      + MVUtil.padBegin("mtd_3d_obj_pair inserts: ", 36)
                      + obj3dPairInserts + "\n"
                      + MVUtil.padBegin("mtd_2d_obj inserts: ", 36) + obj2dInserts + "\n"
                      + (info.mtdHeaderDBCheck ? MVUtil.padBegin("mtd_header search time: ", 36)
                      + MVUtil.formatTimeSpan(
                      timeStats.get("headerSearchTime"))
                      + "\n" : "")
                      + MVUtil.padBegin("total load time: ", 36)
                      + MVUtil.formatTimeSpan(intMtdHeaderLoadTime) + "\n\n");
    }
    return timeStats;
  }

  @Override
  public void updateGroup(String group) throws DatabaseException {
    String sql = "SELECT  category FROM metadata";
    String currentCategory = "";
    int nrows = 0;

    try (Connection con = getConnection();
         Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                 java.sql.ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery(sql)) {

      while (res.next()) {
        currentCategory = res.getString(1);
        nrows = nrows + 1;
      }

    } catch (SQLException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    if (!currentCategory.equals(group)) {
      if (nrows == 0) {
        sql = "INSERT INTO metadata VALUES (?,?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
          stmt.setString(1, group);
          stmt.setString(2, "");
          stmt.executeUpdate();
        } catch (SQLException se) {
          logger.error(ERROR_MARKER, se.getMessage());
          throw new DatabaseException(
                  "caught SQLException calling executeUpdate: " + se.getMessage());
        }
      } else {
        sql =
                "UPDATE metadata SET category = ? WHERE " + BINARY + "category = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
          stmt.setString(1, group);
          stmt.setString(2, currentCategory);
          stmt.executeUpdate();
        } catch (SQLException se) {
          logger.error(ERROR_MARKER, se.getMessage());
          throw new DatabaseException(
                  "caught SQLException calling executeUpdate: " + se.getMessage());
        }
      }
    }
  }

  @Override
  public void updateDescription(String description) throws DatabaseException {
    String sql = "SELECT  description FROM metadata";
    String currentDescription = "";
    int nrows = 0;

    try (Connection con = getConnection();
         Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                 java.sql.ResultSet.CONCUR_READ_ONLY);
         ResultSet res = stmt.executeQuery(sql)) {

      while (res.next()) {
        currentDescription = res.getString(1);
        nrows = nrows + 1;
      }

    } catch (SQLException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    if (!currentDescription.equals(description)) {
      if (nrows == 0) {
        sql = "INSERT INTO metadata VALUES (?,?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
          stmt.setString(1, "");
          stmt.setString(2, description);
          stmt.executeUpdate();
        } catch (SQLException se) {
          logger.error(ERROR_MARKER, se.getMessage());
          throw new DatabaseException(
                  "caught SQLException calling executeUpdate: " + se.getMessage());
        }

      } else {

        sql = "UPDATE metadata SET description = ? WHERE " + BINARY + "description = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
          stmt.setString(1, description);
          stmt.setString(2, currentDescription);
          stmt.executeUpdate();
        } catch (SQLException se) {
          logger.error(ERROR_MARKER, se.getMessage());
          throw new DatabaseException(
                  "caught SQLException calling executeUpdate: " + se.getMessage());
        }
      }
    }
  }


  private boolean executeBatch(
          final List<String> listValues, final String table) throws DatabaseException {


    String insertSql = tableToInsert.get(table);
    boolean result = true;
    Connection con = null;
    Statement stmt = null;
    PreparedStatement ps = null;
    try {
      con = getConnection();
      stmt = con.createStatement();
      ps = con.prepareStatement(insertSql);
      for (int i = 0; i < listValues.size(); i++) {

        String[] valuesArr = listValues.get(i).split(",");
        valuesArr[0] = valuesArr[0].replace("(", "");
        valuesArr[valuesArr.length - 1] = valuesArr[valuesArr.length - 1].replace(")", "");
        for (int j = 0; j < valuesArr.length; j++) {
          ps.setObject(j + 1, valuesArr[j].trim().replaceAll("'", ""));
        }
        ps.addBatch();

        //execute and commit batch of 20000 queries
        if (i != 0 && i % MAX_LINES == 0 && result) {
          int[] updateCounts = ps.executeBatch();
          if (updateCounts[0] < 0 && updateCounts[0] != Statement.SUCCESS_NO_INFO) {
            result = false;
          }
          ps.clearBatch();
        }
      }
      if (result) {
        int[] updateCounts = ps.executeBatch();
        if (updateCounts[0] < 0 && updateCounts[0] != Statement.SUCCESS_NO_INFO) {
          result = false;
        }
      }

    } catch (SQLException se) {
      logger.error(ERROR_MARKER, se.getMessage());
      throw new DatabaseException("caught SQLException calling executeBatch: " + se.getMessage());
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }

    }
    return result;
  }

  /**
   * Analyze the input file object to determine what type of MET output file it is.  Create an entry
   * in the data_file table for the file and build a DataFileInfo data structure with information
   * about the file and return it.
   *
   * @param file points to a MET output file to process // * @param con database connection to use
   * @return data structure containing information about the input file
   */
  @Override
  public DataFileInfo processDataFile(File file, boolean forceDupFile) throws DatabaseException {
    String filePath = file.getParent().replace("\\", "/");
    String fileName = file.getName();
    int dataFileLuId;
    String dataFileLuTypeName;
    int dataFileId;

    //check file size and return if it  is 0
    if (file.length() == 0) {
      return null;
    }
    // set default values for the loaded time (now) and the modified time (that of input file)
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String loadDate = formatDate(cal.getTime());
    cal.setTimeInMillis(file.lastModified());

    String modDate = formatDate(cal.getTime());


    // determine the type of the input data file by parsing the filename
    if (fileName.matches("\\S+\\.stat$")) {
      dataFileLuTypeName = "stat";
    } else if (fileName.matches("\\S+_obj\\.txt$")) {
      dataFileLuTypeName = "mode_obj";
    } else if (fileName.matches("\\S+_cts\\.txt$")) {
      dataFileLuTypeName = "mode_cts";
    } else if (fileName.matches("\\S+\\.vsdb$")) {
      dataFileLuTypeName = "vsdb_point_stat";
    } else if (fileName.matches("\\S+2d.txt$")) {
      dataFileLuTypeName = "mtd_2d";
    } else if (fileName.matches("\\S+3d_pair_cluster.txt$")) {
      dataFileLuTypeName = "mtd_3d_pc";
    } else if (fileName.matches("\\S+3d_pair_simple.txt$")) {
      dataFileLuTypeName = "mtd_3d_ps";
    } else if (fileName.matches("\\S+3d_single_cluster.txt$")) {
      dataFileLuTypeName = "mtd_3d_sc";
    } else if (fileName.matches("\\S+3d_single_simple.txt$")) {
      dataFileLuTypeName = "mtd_3d_ss";
    } else {
      return null;
    }

    // do not insert empty files into the database
    int lines = 0;
    try (
            FileReader fileReader = new FileReader(file);
            BoundedBufferedReader reader = new BoundedBufferedReader(fileReader, MAX_LINES, MAX_LINE_LEN)) {
      //  read in each line of the input file
      while (reader.ready()) {
        String line = reader.readLineBounded();
        if (line.length() > 0 && !line.startsWith("VERSION")) {
          lines++;
        }
        if (lines == 1) {
          break;
        }
      }
    } catch (IOException e) {
      logger.error(ERROR_MARKER, e.getMessage());
    }
    if (lines == 0) {
      logger.warn("  **  WARNING: file " + file.getAbsolutePath() + " is empty and will be ignored");
      return null;
    }

    dataFileLuId = tableDataFileLU.get(dataFileLuTypeName);


    // build a query to look for the file and path in the data_file table
    String dataFileQuery =
            "SELECT " +
                    "  dfl.type_name, " +
                    "  df.data_file_id, " +
                    "  df.load_date, " +
                    "  df.mod_date " +
                    "FROM " +
                    "  data_file_lu dfl, " +
                    "  data_file df " +
                    "WHERE " +
                    "  dfl.data_file_lu_id = df.data_file_lu_id " +
                    "  AND  BINARY df.filename = ? " +
                    "  AND  BINARY df.path = ? ";

    ResultSet resultSet = null;
    try (
            Connection con = getConnection();
            PreparedStatement stmt = con.prepareStatement(dataFileQuery, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {

      stmt.setString(1, fileName);
      stmt.setString(2, filePath);
      resultSet = stmt.executeQuery();

      // if the data file is already present in the database, print a warning and return the id
      if (resultSet.next()) {
        dataFileLuTypeName = resultSet.getString(1);
        dataFileId = resultSet.getInt(2);
        loadDate = resultSet.getString(3);
        modDate = resultSet.getString(4);

        if (forceDupFile) {
          DataFileInfo info = new DataFileInfo(dataFileId, fileName, filePath, loadDate,
                  modDate, dataFileLuId, dataFileLuTypeName);
          logger.warn("  **  WARNING: file already present in table data_file");
          return info;
        } else {
          throw new DatabaseException(
                  "file already present in table data_file, use force_dup_file setting to override");
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage());
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          logger.error(ERROR_MARKER, e.getMessage());
        }
      }
    }
    // if the file is not present in the data_file table, query for the largest data_file_id
    try (
            Connection con = getConnection();
            Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            ResultSet res = stmt.executeQuery("SELECT MAX(data_file_id) FROM data_file;")) {

      if (!res.next()) {
        throw new DatabaseException("METviewer load error: processDataFile() unable to find max "
                + "data_file_id");
      }
      dataFileId = res.getInt(1);
      if (res.wasNull()) {
        dataFileId = 0;
      }
      dataFileId = dataFileId + 1;

    } catch (DatabaseException | SQLException e) {
      throw new DatabaseException(e.getMessage());
    }


    // add the input file to the data_file table


    String strDataFileInsert =
            "INSERT INTO data_file VALUES (?,?,?,?,?,?)";
    int resCounter;
    try (Connection con = getConnection();
         PreparedStatement stmt = con.prepareStatement(strDataFileInsert)) {
      stmt.setInt(1, dataFileId);
      stmt.setInt(2, dataFileLuId);
      stmt.setString(3, fileName);
      if (filePath.length() > 120) {
        stmt.setString(4, filePath.substring(0, 115) + "...");
      } else {
        stmt.setString(4, filePath);
      }
      stmt.setObject(5, loadDate, Types.TIMESTAMP);
      stmt.setObject(6, modDate, Types.TIMESTAMP);
      resCounter = stmt.executeUpdate();
    } catch (SQLException se) {
      logger.error(ERROR_MARKER, se.getMessage());
      throw new DatabaseException(
              "caught SQLException calling executeUpdate: " + se.getMessage());
    }
    if (1 != resCounter) {
      logger.warn("  **  WARNING: unexpected result from data_file INSERT: " + resCounter);
    }

    return new DataFileInfo(dataFileId, fileName, filePath,
            loadDate,
            modDate, dataFileLuId,
            dataFileLuTypeName);
  }

  @Override
  public void updateInfoTable(String strXML, MVLoadJob job) throws DatabaseException {
    //  get the instance_info information to insert
    int instInfoIdNext = getNextId("instance_info", "instance_info_id");
    String updater = "mvuser";
    String updateDate = DATE_FORMATTER.format(LocalDateTime.now());
    String updateDetail = job.getLoadNote();

    //  read the load xml into a string, if requested
    StringBuilder loadXmlStr = new StringBuilder();
    if (job.getLoadXML()) {
      strXML = MVUtil.cleanString(strXML);
      try (FileReader fileReader = new FileReader(strXML);
           BoundedBufferedReader reader = new BoundedBufferedReader(fileReader)) {
        while (reader.ready()) {
          loadXmlStr.append(reader.readLineBounded().trim());
        }
      } catch (IOException e) {
        logger.error(ERROR_MARKER, e.getMessage());
      }
    }

    //  construct an update statement for instance_info


    String instInfoSQL =
            "INSERT INTO instance_info VALUES (?,?,?,?,?)";

    //  execute the insert SQL
    logger.info("Inserting instance_info record...  ");
    int insert;
    try (Connection con = getConnection();
         PreparedStatement stmt = con.prepareStatement(instInfoSQL)) {
      stmt.setInt(1, instInfoIdNext);
      stmt.setString(2, updater);
      stmt.setObject(3, updateDate, Types.TIMESTAMP);
      stmt.setString(4, updateDetail);
      stmt.setString(5, loadXmlStr.toString());
      insert = stmt.executeUpdate();
    } catch (SQLException se) {
      logger.error(ERROR_MARKER, se.getMessage());
      throw new DatabaseException(
              "caught SQLException calling executeUpdate: " + se.getMessage());
    }

    if (1 != insert) {
      throw new DatabaseException("unexpected number of instance_info rows inserted: " + insert);
    }
    logger.info("Done\n");
  }


  private String replaceInvalidValues(final String strData) {
    return strData.replace("NA", "-9999")
            .replace("-nan", "-9999")
            .replace("nan", "-9999");
  }

}

/**
 * LoadDataTest.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.MVLoad;
import edu.ucar.metviewer.test.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static edu.ucar.metviewer.test.util.TestUtil.*;
import static org.junit.Assert.assertEquals;

/**
 * @author : tatiana $
 * @version : 1.0 : 18/Jul/14 11:44 $
 */
public class LoadDataTest {


  private static final Map<String, Integer> TABLES_TO_ROWS = new HashMap<>();
  private static TestDBManager myDatabaseManager;  // knows how to load data and getNumberOfRows

  @Before
  public void init() {
    TABLES_TO_ROWS.put("data_file", 1982);
    TABLES_TO_ROWS.put("data_file_lu", 14);
    TABLES_TO_ROWS.put("line_data_cnt", 149457);//148057
    TABLES_TO_ROWS.put("line_data_ctc", 164512);
    TABLES_TO_ROWS.put("line_data_cts", 38697);//38527
    TABLES_TO_ROWS.put("line_data_fho", 38550);//38527
    TABLES_TO_ROWS.put("line_data_isc", 0);
    TABLES_TO_ROWS.put("line_data_mctc", 1);
    TABLES_TO_ROWS.put("line_data_mctc_cnt", 16);
    TABLES_TO_ROWS.put("line_data_mcts", 2);
    TABLES_TO_ROWS.put("line_data_mpr", 2988780);
    TABLES_TO_ROWS.put("line_data_nbrcnt", 248);
    TABLES_TO_ROWS.put("line_data_nbrctc", 124);
    TABLES_TO_ROWS.put("line_data_nbrcts", 248);
    TABLES_TO_ROWS.put("line_data_orank", 7327);
    TABLES_TO_ROWS.put("line_data_orank_ens", 38061);
    TABLES_TO_ROWS.put("line_data_pct", 1277);
    TABLES_TO_ROWS.put("line_data_pct_thresh", 23656);
    TABLES_TO_ROWS.put("line_data_perc", 200);
    TABLES_TO_ROWS.put("line_data_phist", 52);
    TABLES_TO_ROWS.put("line_data_phist_bin", 1040);
    TABLES_TO_ROWS.put("line_data_pjc", 1);
    TABLES_TO_ROWS.put("line_data_pjc_thresh", 4);
    TABLES_TO_ROWS.put("line_data_prc", 1);
    TABLES_TO_ROWS.put("line_data_probrirw", 0);
    TABLES_TO_ROWS.put("line_data_probrirw_thresh", 0);
    TABLES_TO_ROWS.put("line_data_prc_thresh", 4);
    TABLES_TO_ROWS.put("line_data_pstd", 1277);
    TABLES_TO_ROWS.put("line_data_pstd_thresh", 5);
    TABLES_TO_ROWS.put("line_data_relp", 1312);
    TABLES_TO_ROWS.put("line_data_relp_ens", 22568);
    TABLES_TO_ROWS.put("line_data_rhist", 1331);
    TABLES_TO_ROWS.put("line_data_rhist_rank", 23994);
    TABLES_TO_ROWS.put("line_data_rps", 18);
    TABLES_TO_ROWS.put("line_data_sal1l2", 40);
    TABLES_TO_ROWS.put("line_data_sl1l2", 153500);//140530
    TABLES_TO_ROWS.put("line_data_ssvar", 5515);
    TABLES_TO_ROWS.put("line_data_val1l2", 24);
    TABLES_TO_ROWS.put("line_data_tcmpr", 0);
    TABLES_TO_ROWS.put("line_data_vl1l2", 4156);
    TABLES_TO_ROWS.put("line_data_vcnt", 12);
    TABLES_TO_ROWS.put("mode_cts", 1163);
    TABLES_TO_ROWS.put("mode_header", 399);
    TABLES_TO_ROWS.put("mode_obj_pair", 174295);
    TABLES_TO_ROWS.put("mode_obj_single", 22909);
    TABLES_TO_ROWS.put("stat_header", 6636);
    TABLES_TO_ROWS.put("line_data_enscnt", 1276);
    TABLES_TO_ROWS.put("mtd_2d_obj", 9576);
    TABLES_TO_ROWS.put("mtd_3d_obj_pair", 57);
    TABLES_TO_ROWS.put("mtd_3d_obj_single", 773);
    // recreate database
    Reader reader = null;
    try {
      myDatabaseManager = TestDatabaseManager.getManager(type, host, USERNAME, PWD, database);
      myDatabaseManager.checkCreateDatabase(host, USERNAME, PWD, database);
      String fname = LOAD_DIR + FILE_SEPARATOR + "load/mv_mysql.sql";
      myDatabaseManager.loadData(fname, database);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    String[] args = new String[]{LOAD_DIR + FILE_SEPARATOR + "load/load_test.xml"};

    // replace credentials from system properties
    xlateTestSpec(args[0]);
    try {
    MVLoad.main(args);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void checkDatabaseContent() {
    try {
      for (Map.Entry<String, Integer> entry : TABLES_TO_ROWS.entrySet()) {
        Integer rows = myDatabaseManager.getNumberOfRows(entry.getKey());
        assertEquals("Number of rows in table " + entry.getKey() + " should be " + entry.getValue() + " but it is not", entry.getValue(), rows);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
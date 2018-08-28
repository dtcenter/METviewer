/**
 * LoadDataTest.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.MVLoad;
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
      TABLES_TO_ROWS.put("data_file", 1701);
      TABLES_TO_ROWS.put("data_file_lu", 7);
      TABLES_TO_ROWS.put("line_data_cnt", 149385);//148057
      TABLES_TO_ROWS.put("line_data_ctc", 164435);//38527
      TABLES_TO_ROWS.put("line_data_cts", 38575);//38527
      TABLES_TO_ROWS.put("line_data_fho", 38495);//38527
      TABLES_TO_ROWS.put("line_data_isc", 0);
      TABLES_TO_ROWS.put("line_data_mctc", 0);
      TABLES_TO_ROWS.put("line_data_mctc_cnt", 0);
      TABLES_TO_ROWS.put("line_data_mcts", 0);
      TABLES_TO_ROWS.put("line_data_mpr", 2939590);
      TABLES_TO_ROWS.put("line_data_nbrcnt", 80);
      TABLES_TO_ROWS.put("line_data_nbrctc", 40);
      TABLES_TO_ROWS.put("line_data_nbrcts", 80);
      TABLES_TO_ROWS.put("line_data_orank", 3980);
      TABLES_TO_ROWS.put("line_data_orank_ens", 19900);
      TABLES_TO_ROWS.put("line_data_pct", 1277);
      TABLES_TO_ROWS.put("line_data_pct_thresh", 23656);
      TABLES_TO_ROWS.put("line_data_phist", 19);
      TABLES_TO_ROWS.put("line_data_phist_bin", 380);
      TABLES_TO_ROWS.put("line_data_pjc", 1);
      TABLES_TO_ROWS.put("line_data_pjc_thresh", 4);
      TABLES_TO_ROWS.put("line_data_prc", 1);
      TABLES_TO_ROWS.put("line_data_prc_thresh", 4);
      TABLES_TO_ROWS.put("line_data_pstd", 1277);
      TABLES_TO_ROWS.put("line_data_pstd_thresh", 5);
      TABLES_TO_ROWS.put("line_data_rhist", 1295);
      TABLES_TO_ROWS.put("line_data_rhist_rank", 23766);
      TABLES_TO_ROWS.put("line_data_sal1l2", 18);
      TABLES_TO_ROWS.put("line_data_sl1l2", 153444);//140530
      TABLES_TO_ROWS.put("line_data_ssvar", 2001);
      TABLES_TO_ROWS.put("line_data_val1l2", 18);
      TABLES_TO_ROWS.put("line_data_vl1l2", 4146);
      TABLES_TO_ROWS.put("mode_cts", 1161);
      TABLES_TO_ROWS.put("mode_header", 398);
      TABLES_TO_ROWS.put("mode_obj_pair", 174290);
      TABLES_TO_ROWS.put("mode_obj_single", 22903);
      TABLES_TO_ROWS.put("mv_rev", 6);
      TABLES_TO_ROWS.put("stat_header", 6097);
      TABLES_TO_ROWS.put("line_data_enscnt", 1276);
      // recreate database

      Reader reader = null;
      try {
        myDatabaseManager = TestDatabaseManager.getManager(type, host, USERNAME, PWD, database);
        String fname = LOAD_DIR + FILE_SEPARATOR + "load/mv_mysql.sql";
        myDatabaseManager.loadData(fname, database);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
        String[] args = new String[]{LOAD_DIR + FILE_SEPARATOR + "load/load_test.xml"};

        // replace credentials from system properties
        xlateTestSpec(args[0]);

        MVLoad.main(args);
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
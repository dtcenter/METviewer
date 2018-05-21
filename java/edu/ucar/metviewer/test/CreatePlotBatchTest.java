/**
 * CreatePlotBatchTest.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static edu.ucar.metviewer.test.util.TestUtil.DIRECTORY_FILTER;
import static edu.ucar.metviewer.test.util.TestUtil.FILE_SEPARATOR;
import static edu.ucar.metviewer.test.util.TestUtil.ROOT_DIR;
import static edu.ucar.metviewer.test.util.TestUtil.cleanWorkingDirs;
import static edu.ucar.metviewer.test.util.TestUtil.comparePlotFilesWithNames;
import static edu.ucar.metviewer.test.util.TestUtil.comparePointsFilesWithNames;
import static edu.ucar.metviewer.test.util.TestUtil.runBatch;

/**
 * Run batch mode tests
 * The config files and expected result for the regression testing should be in <root_dir>/plots_batch
 * Class passes each config file to MVBatch class and compare output files with the expected.
 * @author : tatiana $
 * @version : 1.0 : 22/Jul/14 14:52 $
 */
@RunWith(Parameterized.class)
public class CreatePlotBatchTest {

  private static String testDataDir;
  private String plotType = null;

  public CreatePlotBatchTest(String plotType) {
    super();
    this.plotType = plotType;
  }

  /**
   * register all possible test cases in the test directory
   * @return - test cases names
   * @throws Exception - if test directory doesn't exists or it is empty
   */
  @Parameterized.Parameters
  public static Collection<String[]> data() throws Exception {
    testDataDir = ROOT_DIR + FILE_SEPARATOR + "plots_batch";

    File file = new File(testDataDir);
    if (!file.exists()) {
      throw new Exception(testDataDir + " doesn't exist.");
    }
    String[] directories = file.list(DIRECTORY_FILTER);
    Collection<String[]> plots = new ArrayList<>();
    for (String directory : directories) {
      plots.add(new String[]{directory});
    }
    if (plots.isEmpty()) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    return plots;
  }

  @Before
  public void runScript() {
    runBatch(testDataDir, plotType);
  }

  @Test
  public void compareOutputFiles() {
    if(!"ens_ss".equals(plotType)) {
      comparePointsFilesWithNames(testDataDir, "1", plotType);
      comparePointsFilesWithNames(testDataDir, "2", plotType);
    }
    comparePlotFilesWithNames(testDataDir, plotType);
  }


  @After
  public void cleanup() {
    if (System.getProperty("noClean") == null) {
      cleanWorkingDirs();
    }
  }

}

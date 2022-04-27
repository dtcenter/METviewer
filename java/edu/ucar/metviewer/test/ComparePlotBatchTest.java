/**
 * CreatePlotBatchTest.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static edu.ucar.metviewer.test.util.TestUtil.*;

/**
 * Compare batch tests
 * The config files and expected result for the regression testing should be in <root_dir>/plots_batch
 * Class passes each config file to MVBatch class and compare output files with the expected.
 */
@RunWith(Parameterized.class)
public class ComparePlotBatchTest {
  private static String testDataDir;
  private static String testCompareDataDir;
  private static String testOutputDir;
  private static String testCompareOutputDir;
  private String plotType = null;

  public ComparePlotBatchTest(String plotType) {
    super();
    this.plotType = plotType;
  }

  /**
   * register all possible test cases in the test directory
   *
   * @return - test cases names
   * @throws Exception - if test directory doesn't exists or it is empty
   */
  @Parameterized.Parameters
  public static Collection<String[]> data() throws Exception {
    testDataDir = ROOT_DIR + FILE_SEPARATOR + "test_data" + FILE_SEPARATOR + "test_cases";
    testCompareDataDir = ROOT_COMPARE_DIR + FILE_SEPARATOR + "test_data" + FILE_SEPARATOR + "test_cases";

    testOutputDir = ROOT_DIR + FILE_SEPARATOR +"output" + FILE_SEPARATOR + "plots" + FILE_SEPARATOR;
    testCompareOutputDir = ROOT_COMPARE_DIR + FILE_SEPARATOR +"output" + FILE_SEPARATOR + "plots" + FILE_SEPARATOR;
    File file = new File(testDataDir);
    if (!file.exists()) {
      throw new Exception(testDataDir + " doesn't exist.");
    }

    File file1 = new File(testOutputDir);
    if (!file1.exists()) {
      throw new Exception(testOutputDir + " doesn't exist.");
    }
    // does the testDataDir directory exist and does it have any files and are any of them .xml files?
    String[] directories = file.list(DIRECTORY_FILTER);
    Collection<String[]> testPlots = new ArrayList<>();
    for (String directory : directories) {
      File dir = new File(directory);
      testPlots.add(new String[]{directory});
    }
    if (testPlots.isEmpty()) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    // does the testCompareDataDir directory exist and does it have any files and are any of them .xml files?
    String[] compareDirectories = file.list(COMPARE_DIRECTORY_FILTER);
    Collection<String[]> comparePlots = new ArrayList<>();
    for (String directory : compareDirectories) {
      comparePlots.add(new String[]{directory});
    }
    if (comparePlots.isEmpty()) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    return testPlots;
  }

  @Test
  public void compareOutputFiles() {
    System.out.println("Comparing " + plotType);

    if(plotType.startsWith("scorecard")){
      System.out.println("_________ Scorecard");
      compareYamlTestFiles(testOutputDir, testCompareOutputDir, plotType);
      compareBinaryTestFiles(testOutputDir, testCompareOutputDir, plotType);
    }else {
      compareYamlTestFiles(testOutputDir, testCompareOutputDir, plotType);
    }
  }
}

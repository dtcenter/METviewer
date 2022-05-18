/**
 * AllTestRunner.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static edu.ucar.metviewer.test.util.TestUtil.cleanWorkingDirs;

/**
 * @author : tatiana $
 * @version : 1.1 : 10/Sep/21 - RTP $
 * <p>
 * example test invocation...
 * java -Xmx2048M -ea -Dmv_root_dir=/Users/pierce/METViewerTest/mv_2_6_dev-rtp/HEAD
 * -Dmv_database=mv_test_rp_fred -Dmv_user=met_admin -Dmv_pwd=MaPass4mvmay2018##
 * -Dmv_host=model-vxtest.gsd.esrl.noaa.gov -Dmv_port=3306
 * -Dlog4j.configurationFile=file:/Users/pierce/IdeaProjects/METViewer/java/edu/ucar/metviewer/resources/log4j2.xml
 * -DloadData=y
 * -DservletTest=y
 * edu.ucar.metviewer.test.AllTestRunner
 * <p>
 * System Properties that affect testRunner
 * compareOnly - unset to create test plots and compare - set to anything to only compare
 * mv_root_dir - METViewer test data root directory - This is the test dir/branch/tag
 * mv_host - database host
 * mv_port - database port
 * mv_database - test database
 * mv_user - test database user
 * mv_pwd - test database passwoprd
 * mv_type - test database type
 * captureCreatedImages - unset to test previously captured images - set to anything to capture the images that the test creates
 * captured images are stored in .....
 * noClean - set to anything to prevent cleaning test directories - unset to auto clean test directories
 */
public class AllTestRunner {

  public static void main(String[] args) {

    Result compareResult;
    Result batchResult;
    List<Failure> failureListPlotBatchTest = null;

    cleanWorkingDirs();


    if (System.getProperty("compareOnly") == null) {
      // really test
      batchResult = JUnitCore.runClasses(CreatePlotBatchTest.class);
    } else {
      batchResult = JUnitCore.runClasses(ComparePlotBatchTest.class);
    }
    failureListPlotBatchTest = batchResult.getFailures();


    System.out.println("*************************************************");
    System.out.println("PlotBatchTest results");
    System.out.println("*************************************************");
    if (failureListPlotBatchTest != null) {
      for (Failure failure : failureListPlotBatchTest) {
        System.out.println(failure.toString());
        System.out.println(failure.getMessage());
        System.out.println(failure.getTrace());
        System.out.println(failure.getTestHeader());
        System.out.println(failure.getDescription().toString());
      }
      if (failureListPlotBatchTest.isEmpty()) {
        System.out.println("***** Plot making tests finished successfully... *****");
      }
    } else {
      System.out.println("***** Plot batch testing was ignored... *****");
    }

    System.out.println("*************************************************");
    System.out.println();

    System.out.println("*************************************************");
    int failureListPlotBatchTestCount = failureListPlotBatchTest == null ? 0 : failureListPlotBatchTest.size();
    System.out.println("*************************************************");
    System.out.println("There were " + failureListPlotBatchTestCount + " failures");
    System.exit(failureListPlotBatchTestCount);
  }
}

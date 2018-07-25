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
 * @version : 1.0 : 11/Jul/14 13:54 $
 */
public class AllTestRunner {

  public static void main(String[] args) {

      Result result;
      List<Failure> failureListLoadDataTest = null;
      List<Failure> failureListPlotBatchTest = null;

      cleanWorkingDirs();
      if (System.getProperty("loadData") != null) {
        result = JUnitCore.runClasses(LoadDataTest.class);
        failureListLoadDataTest = result.getFailures();
      }

      if (System.getProperty("compareOnly") == null) {
        // really test
        result = JUnitCore.runClasses(CreatePlotBatchTest.class);
      } else {
        result = JUnitCore.runClasses(ComparePlotBatchTest.class);
      }
      failureListPlotBatchTest = result.getFailures();

      System.out.println("*************************************************");
      if (failureListLoadDataTest != null) {
        for (Failure failure : failureListLoadDataTest) {
          System.out.println(failure.toString());
        }
        if (failureListLoadDataTest.isEmpty()) {
          System.out.println("***** Database loading tests finished successfully... *****");
        }
      } else {
        System.out.println("***** Database loading tests was ignored... *****");
      }
      System.out.println("*************************************************");
      System.out.println();
      System.out.println("*************************************************");
      for (Failure failure : failureListPlotBatchTest) {
        System.out.println(failure.toString());
      }
      if (failureListPlotBatchTest.isEmpty()) {
        System.out.println("***** Plot making tests finished successfully... *****");
      }
      System.out.println("*************************************************");
      System.out.println();
      System.out.println("*************************************************");
      int failureListLoadDataTestCount = failureListLoadDataTest == null ? 0: failureListLoadDataTest.size();
      int failureListPlotBatchTestCount = failureListPlotBatchTest == null ? 0: failureListPlotBatchTest.size();
      int exitCode = failureListLoadDataTestCount + failureListPlotBatchTestCount;
      System.out.println("*************************************************");
      System.out.println("There were " + exitCode + " failures");
      System.exit( exitCode);
    }
}

/**
 * AllTestRunner.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * @author : R Pierce $
 * @version : 1.0 : 13/Jul/18 13:54 $
 */
public class AllTestCompare {

  public static void main(String[] args) {

    Result result;
    result = JUnitCore.runClasses(ComparePlotBatchTest.class);
    List<Failure> failureListComparePlotBatchTest = result.getFailures();
    System.out.println("*************************************************");
    for (Failure failure : failureListComparePlotBatchTest) {
      System.out.println(failure.toString());
    }
    if (failureListComparePlotBatchTest.isEmpty()) {
      System.out.println("***** Plots compared successfully... *****");
    }
    System.out.println("*************************************************");
    int exitCode = failureListComparePlotBatchTest == null ? 0 : failureListComparePlotBatchTest.size();
    System.out.println("*************************************************");
    System.out.println("There were " + exitCode + " failures");
    System.exit(exitCode);
  }
}

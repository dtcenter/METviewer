/**
 * AllTestRunner.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

/**
 * @author : tatiana $
 * @version : 1.0 : 11/Jul/14 13:54 $
 */
public class AllTestRunner {

  public static void main(String[] args) {
    Result result;
    List<Failure> failureListLoadDataTest = null;
    if (args.length > 0 && args[0].equals("all")) {
      result = JUnitCore.runClasses(LoadDataTest.class);
      failureListLoadDataTest = result.getFailures();

    }

    result = JUnitCore.runClasses(CreatePlotBatchTest.class);
    List<Failure> failureListCreatePlotBatchTest = result.getFailures();


    result = JUnitCore.runClasses(TestMVServlet.class);
    List<Failure> failureListTestMVServlet = result.getFailures();


    result = JUnitCore.runClasses(CreatePlotServletTest.class);
    List<Failure> failureCreatePlotServletTest = result.getFailures();

    System.out.println("*************************************************");
    if (failureListLoadDataTest != null) {


      for (Failure failure : failureListLoadDataTest) {
        System.out.println(failure.toString());
      }
      if (failureListLoadDataTest.size() == 0) {
        System.out.println("***** Database loading tests finished successfully... *****");
      }

    } else {
      System.out.println("***** Database loading tests was ignored... *****");
    }
    System.out.println("*************************************************");
    System.out.println();
    System.out.println("*************************************************");
    for (Failure failure : failureListCreatePlotBatchTest) {
      System.out.println(failure.toString());
    }
    if (failureListCreatePlotBatchTest.size() == 0) {
      System.out.println("***** Plot making tests finished successfully... *****");
    }
    System.out.println("*************************************************");
    System.out.println();
    System.out.println("*************************************************");
    for (Failure failure : failureListTestMVServlet) {
      System.out.println(failure.toString());
    }
    if (failureListTestMVServlet.size() == 0) {
      System.out.println("***** Servlet tests finished successfully... *****");
    }
    System.out.println("*************************************************");
    System.out.println();
    System.out.println("*************************************************");
    for (Failure failure : failureCreatePlotServletTest) {
      System.out.println(failure.toString());
    }
    if (failureCreatePlotServletTest.size() == 0) {
      System.out.println("***** Create Plot Servlet tests finished successfully... *****");
    }
    System.out.println("*************************************************");
  }

}

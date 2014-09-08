/**
 * CreatePlotServletTest.java
 * Copyright UCAR (c) 2014.
 * University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.MVServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import static edu.ucar.metviewer.test.util.TestUtil.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Run web mode tests The config files and expected result for the regression
 * testing should be in <root_dir>/plots_web Class passes each config file to
 * MVBatch class and compare output files with the expected.
 *
 * @author : tatiana $
 * @version : 1.0 : 23/Jul/14 13:29 $
 */
@RunWith(Parameterized.class)
public class CreatePlotServletTest {

  private String xmlStr;
  private static final String RESPONSE_RED = "<response><plot>plot_\\d{8}_\\d{6}</plot></response>";
  private String plotType = null;
  private static String testDataDir;


  public CreatePlotServletTest(String plotType) {
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
    //get all dir names - excludes load
    testDataDir = rootDir + FILE_SEPARATOR + "plots_web";
    File file = new File(testDataDir);
    String[] directories = file.list(DIRECTORY_FILTER);
    Collection<String[]> plots = new ArrayList<>();
    for (String directory : directories) {
      plots.add(new String[]{directory});
    }
    if (plots.size() == 0) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    return plots;
  }


  @Before
  public void prepareRequest() {
    cleanWorkingDirs();
    File requestFile = new File(testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + "request.xml");
    xmlStr = readFileToString(requestFile);
  }

  @Test
  public void testServlet() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession httpSession = mock(HttpSession.class);

    StringReader stringReader = null;
    BufferedReader bufferedReader = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    PrintWriter printWriter = null;

    try {
      stringReader = new StringReader(xmlStr);
      bufferedReader = new BufferedReader(stringReader);
      byteArrayOutputStream = new ByteArrayOutputStream();
      printWriter = new PrintWriter(byteArrayOutputStream);

      when(request.getReader()).thenReturn(bufferedReader);
      when(request.getSession()).thenReturn(httpSession);
      when(response.getWriter()).thenReturn(printWriter);
      MVServlet._strPlotXML = plotsDir;
      MVServlet._strRTmpl = templateDir;
      MVServlet._strRWork = rworkDir;
      MVServlet._strPlots = plotsDir;
      MVServlet._strRscript = rscript;
      MVServlet._strDBHost = host;
      MVServlet._strDBUser = username;
      MVServlet._strDBPassword = pwd;
      MVServlet._boolListValCache = true;
      MVServlet._boolListStatCache = true;

      new MVServlet().doPost(request, response);

      verify(request, atLeast(1)).getReader();
      verify(request, atLeast(1)).getSession();
      verify(response, atLeast(1)).getWriter();
      printWriter.flush();
      String responseStr = byteArrayOutputStream.toString().trim();
      boolean isMatch = responseStr.matches(RESPONSE_RED);
      assertTrue(isMatch);

    } finally {
      if (printWriter != null) {
        printWriter.close();
      }
      if (byteArrayOutputStream != null) {
        byteArrayOutputStream.close();
      }
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (stringReader != null) {
        stringReader.close();
      }
    }

    compareDataFilesWithoutNames(testDataDir, plotType);
    comparePlotFilesWithoutNames(testDataDir, plotType);
    compareSqlFilesWithoutNames(testDataDir, plotType);
    comparePointsFilesWithoutNames(testDataDir, "1", plotType);
    comparePointsFilesWithoutNames(testDataDir, "2", plotType);
    compareAggStatInfoFilesWithoutNames(testDataDir, plotType);
    compareDataAggStatWithoutNames(testDataDir, plotType);
  }

  @After
  public void cleanup() {
    cleanWorkingDirs();
  }

}
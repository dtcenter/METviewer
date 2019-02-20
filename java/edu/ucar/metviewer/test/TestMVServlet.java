/**
 * TestMVServlet.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import edu.ucar.metviewer.MVServlet;
import edu.ucar.metviewer.db.AppDatabaseManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static edu.ucar.metviewer.test.util.TestUtil.FILE_SEPARATOR;
import static edu.ucar.metviewer.test.util.TestUtil.PLOTS_DIR;
import static edu.ucar.metviewer.test.util.TestUtil.PWD;
import static edu.ucar.metviewer.test.util.TestUtil.ROOT_DIR;
import static edu.ucar.metviewer.test.util.TestUtil.RWORK_DIR;
import static edu.ucar.metviewer.test.util.TestUtil.TEMPLATE_DIR;
import static edu.ucar.metviewer.test.util.TestUtil.USERNAME;
import static edu.ucar.metviewer.test.util.TestUtil.host;
import static edu.ucar.metviewer.test.util.TestUtil.readFileToString;
import static edu.ucar.metviewer.test.util.TestUtil.rscript;
import static edu.ucar.metviewer.test.util.TestUtil.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ru
 *
 * @author : tatiana $
 * @version : 1.0 : 23/Jul/14 10:57 $
 */
@RunWith(Parameterized.class)
public class TestMVServlet {

  private final String requestValue;
  private final String responseValue;
  private static int count = 0;

  public TestMVServlet(String requestValue, String responseValue) {
    this.requestValue = requestValue;
    this.responseValue = responseValue;
  }


  @Parameterized.Parameters
  public static Collection<String[]> data() throws Exception {
    String testDataDir = ROOT_DIR + FILE_SEPARATOR + "servlet";
    Collection<String[]> requestResponse = new ArrayList<>();
    File file = new File(testDataDir);
    //get all requests
    File[] requestFiles = file.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return name.matches("request_\\d+.xml");
      }
    });
    if (requestFiles.length == 0) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    for (File requestFile : requestFiles) {
      String number = requestFile.getName().split("_")[1].split("\\.")[0];
      File responseFile = new File(testDataDir + FILE_SEPARATOR + "response_" + number + ".xml");
      if (!responseFile.exists()) {
        System.out.println("Response file for request # " + number + " doesn't exist. Skip this request");
        continue;
      }
      String requestStr = readFileToString(requestFile);
      String responseStr = readFileToString(responseFile);
      requestResponse.add(new String[]{requestStr, responseStr});
    }
    if (requestResponse.isEmpty()) {
      throw new Exception(testDataDir + " doesn't have any test cases.");
    }
    return requestResponse;
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
      stringReader = new StringReader(requestValue);
      bufferedReader = new BufferedReader(stringReader);
      byteArrayOutputStream = new ByteArrayOutputStream();
      printWriter = new PrintWriter(byteArrayOutputStream);


      when(request.getReader()).thenReturn(bufferedReader);
      when(request.getSession()).thenReturn(httpSession);
      when(response.getWriter()).thenReturn(printWriter);


      MVServlet mvServlet = new MVServlet();
           mvServlet.setPlotXml(PLOTS_DIR);
           mvServlet.setrTmpl(TEMPLATE_DIR);
           mvServlet.setrWork(RWORK_DIR);
           mvServlet.setPlots(PLOTS_DIR);
           mvServlet.setRscript(rscript);
           mvServlet.setValCache(true);
           //type, host, USERNAME, PWD all come from TestUtil (System.getProperty)
           mvServlet.setDatabaseManager((AppDatabaseManager) edu.ucar.metviewer.db.DatabaseManager.getAppManager(type,host, USERNAME,PWD));


      mvServlet.doPost(request, response);

      verify(request, atLeast(1)).getReader();
      verify(request, atLeast(1)).getSession();
      verify(response, atLeast(1)).getWriter();
      printWriter.flush();
      System.out.println("********");
      System.out.println("request " + count + " is: " + requestValue);


      String expected =
              trimXML(responseValue).replaceAll("<url_output>.*</url_output>"
                      ,"");
      // Why is <url_ouptut> there? RTP
      String resp = trimXML(byteArrayOutputStream.toString().replaceAll(
              "<url_output>.*</url_output>","").
              replaceAll("<[?]xml.*[?]>","").trim());
      System.out.println("Expected response  " + count + " is: " + expected);
      System.out.println("Actual " + count + " response is: " + resp);
      count ++;
      System.out.println("********");
      if (expected.contains("plot_")) {
        String expectedPattern = expected.replaceAll("<plot>plot_.*</plot>",
                "<plot>plot_.*</plot>");
        System.out.println("converted plot number to pattern for matching: " + expectedPattern);
        assertTrue(resp.matches(expectedPattern));
      } else {
        assertEquals(expected, resp);
      }
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
  }

  private String trimXML(String input) {
    return input.replaceAll("(>\\s+<)|(>\\n+<)|(>\\r+<)", "><");
  }

}

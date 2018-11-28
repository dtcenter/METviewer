/**
 * TestMVServlet.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import edu.ucar.metviewer.MVServlet;
import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseManager;
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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

      MVServlet.plotXml = PLOTS_DIR;
      MVServlet.rTmpl = TEMPLATE_DIR;
      MVServlet.rWork = RWORK_DIR;
      MVServlet.plots = PLOTS_DIR;
      MVServlet.rscript = rscript;
      MVServlet.isValCache = true;
      MVServlet.databaseManager =
              (AppDatabaseManager) edu.ucar.metviewer.db.DatabaseManager.getAppManager(type,host, USERNAME,PWD);
      MVServlet.isStatCache = true;
      new MVServlet().doPost(request, response);

      verify(request, atLeast(1)).getReader();
      verify(request, atLeast(1)).getSession();
      verify(response, atLeast(1)).getWriter();
      printWriter.flush();
      System.out.println("********");
      System.out.println("request is: " + requestValue);

      String resp =  trimXML(byteArrayOutputStream.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","")).trim();
      System.out.println("Actual response is: " + resp);
      System.out.println("********");
      assertEquals(trimXML(responseValue), resp);
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

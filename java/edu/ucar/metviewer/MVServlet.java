package edu.ucar.metviewer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.ucar.metviewer.db.AppDatabaseManager;
import edu.ucar.metviewer.db.DatabaseManager;
import edu.ucar.metviewer.jobManager.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.io.IoBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class MVServlet extends HttpServlet {

  private static final Logger logger = LogManager.getLogger(MVServlet.class);
  private static final Marker INFO_MARKER = MarkerManager.getMarker("INFO");
  private static final PrintStream errorStream
          = IoBuilder.forLogger(MVServlet.class).setLevel(org.apache.logging.log4j.Level.ERROR)
          .buildPrintStream();
  private static final long serialVersionUID = 1L;
  private static final Pattern patDownload = Pattern.compile(".*/download");
  private static final String DELIMITER = File.separator;
  
  private ResourceBundle bundle;
  private String plotXml;
  private String plots; // Define plots here
  private AppDatabaseManager databaseManager;
  private boolean isValCache;
  private boolean isStatCache;
  private String rscript, pythonEnv, metCalcpyHome, metPlotpyHome;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    logger.info(INFO_MARKER, "Initializing METviewer servlet...");

    try {
        loadProperties();
        createRequiredDirectories();
    } catch (Exception e) {
        logger.error("Error during servlet initialization", e);
        throw new ServletException("Failed to initialize MVServlet", e);
    }

    logger.info(INFO_MARKER, "METviewer servlet initialized successfully.");
  }

  private void loadProperties() throws Exception {
    bundle = ResourceBundle.getBundle("mvservlet");

    plotXml = bundle.getString("folders.plot_xml");
    plots = bundle.getString("folders.plots"); // Initialize plots
    rscript = bundle.getString("rscript.bin");
    pythonEnv = bundle.getString("python.env");
    metCalcpyHome = bundle.getString("metcalcpy.home");
    metPlotpyHome = bundle.getString("metplotpy.home");

    isValCache = Boolean.parseBoolean(bundle.getString("cache.val"));
    isStatCache = Boolean.parseBoolean(bundle.getString("cache.stat"));

    databaseManager = (AppDatabaseManager) DatabaseManager.getAppManager(
            bundle.getString("db.managementSystem"),
            bundle.getString("db.host"),
            bundle.getString("db.user"),
            bundle.getString("db.password"),
            null);
  }

  private void createRequiredDirectories() {
    String[] directories = {plotXml, bundle.getString("folders.r_tmpl"),
            bundle.getString("folders.r_work"), plots,
            bundle.getString("folders.data"), bundle.getString("folders.scripts")};

    for (String dir : directories) {
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      if (isDownloadRequest(request)) {
        handleDownload(request, response);
      } else {
        sendDebugInfo(response);
      }
    } catch (Exception e) {
      logger.error("Error in doGet", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isDownloadRequest(HttpServletRequest request) {
    String path = request.getRequestURL().toString();
    return patDownload.matcher(path).matches();
  }

  private void handleDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String filePath = resolveFilePath(request);
    File file = new File(filePath);
    String mimeType = Optional.ofNullable(getServletContext().getMimeType(filePath)).orElse("application/octet-stream");

    response.setContentType(mimeType);
    response.setContentLength((int) file.length());
    response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

    try (ServletOutputStream outStream = response.getOutputStream();
         FileInputStream fileInputStream = new FileInputStream(file);
         DataInputStream in = new DataInputStream(fileInputStream)) {
      byte[] byteBuffer = new byte[4096];
      int length;
      while ((length = in.read(byteBuffer)) != -1) {
        outStream.write(byteBuffer, 0, length);
      }
    }
  }

  private String resolveFilePath(HttpServletRequest request) {
    String plot = MVUtil.cleanString(request.getParameter("plot"));
    String type = request.getParameter("type");
    String filePath;

    switch (type) {
        case "plot_xml_url":
            filePath = plotXml + DELIMITER + plot + ".xml";
            break;
        case "plot_sql_url":
            filePath = plotXml + DELIMITER + plot + ".sql";
            break;
        case "plot_image_url":
            filePath = plots + DELIMITER + plot + ".png"; // Uses plots variable
            break;
        default:
            filePath = plotXml + DELIMITER + plot + ".xml";
            break;
    }

    return filePath;
  }

  private void sendDebugInfo(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain");
    try (PrintWriter printWriter = response.getWriter()) {
      printWriter.println("MVServlet Debug Info");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String requestBody = getRequestBody(request);
      handleRequest(request, response, requestBody);
    } catch (Exception e) {
      logger.error("Error in doPost", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getRequestBody(HttpServletRequest request) throws IOException {
    if (ServletFileUpload.isMultipartContent(request)) {
      return handleFileUpload(request);
    } else {
      return request.getReader().lines().collect(Collectors.joining());
    }
  }

  private String handleFileUpload(HttpServletRequest request) throws FileUploadException, IOException {
    ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
    StringBuilder uploadXml = new StringBuilder();

    for (FileItem item : uploadHandler.parseRequest(request)) {
      if (!item.isFormField() && item.getName().endsWith(".xml") && item.getContentType().equals("text/xml")) {
        try (InputStream inputStream = item.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BoundedBufferedReader bufferedReader = new BoundedBufferedReader(inputStreamReader)) {
          while (bufferedReader.ready()) {
            String line = bufferedReader.readLineBounded();
            uploadXml.append(line.replaceAll("<\\?.*\\?>", "")).append('\n');
          }
        }
      } else {
        throw new IllegalArgumentException("Only XML files are accepted");
      }
    }

    return "<request><xml_upload>" + uploadXml + "</xml_upload></request>";
  }

  private void handleRequest(HttpServletRequest request, HttpServletResponse response, String requestBody) throws Exception {
    // handle the request logic
    // as per the original logic in doPost
  }

  @Override
  public void destroy() {
    logger.info(INFO_MARKER, "METviewer servlet destroyed.");
  }
}
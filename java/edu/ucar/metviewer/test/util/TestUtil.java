/**
 * TestUtil.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test.util;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.scorecard.Scorecard;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author : tatiana $
 * @version : 1.0 : 29/Jul/14 13:48 $
 */
public class TestUtil {

  public static final String FILE_SEPARATOR;
  public static final String DATA_DIR;
  public static final String SCRIPTS_DIR;
  public static final String LOAD_DIR;
  public static final String MET_DATA_DIR;
  public static final String database;
  public static final String USERNAME;
  public static final String PWD;
  public static final String HOST_NAME;
  public static final String PORT;
  public static final String host;
  public static final String rscript;
  public static final String TEMPLATE_DIR;
  public static final String RWORK_DIR;
  public static final String PLOTS_DIR;
  private static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
    @Override
    public int compare(File o1, File o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };
  private static final CustomFilenameFilter PLOT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".png";
    }

    @Override
    public String getActualDir() {
      return PLOTS_DIR;
    }
  };
  private static final CustomFilenameFilter SQL_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".sql";
    }

    @Override
    public String getActualDir() {
      return PLOTS_DIR;
    }
  };
  private static final CustomFilenameFilter DATA_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".data";
    }

    @Override
    public String getActualDir() {
      return DATA_DIR;
    }
  };
  private static final CustomFilenameFilter POINTS1_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".points1";
    }

    @Override
    public String getActualDir() {
      return DATA_DIR;
    }
  };
  private static final CustomFilenameFilter POINTS2_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".points2";
    }

    @Override
    public String getActualDir() {
      return DATA_DIR;
    }
  };
  private static final CustomFilenameFilter DATA_AGG_STAT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".data.agg_stat";
    }

    @Override
    public String getActualDir() {
      return DATA_DIR;
    }
  };
  private static final CustomFilenameFilter AGG_STAT_INFO_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".agg_stat.info";
    }

    @Override
    public String getActualDir() {
      return DATA_DIR;
    }

  };
  private static final CustomFilenameFilter RSCRIPT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".R";
    }

    @Override
    public String getActualDir() {
      return SCRIPTS_DIR;
    }
  };
  public static boolean list = false;
  public static boolean verbose = false;
  public static boolean sql = false;
  public static String job_name = null;
  public static String type = null;
  public static String driver = null;
  public static String ROOT_DIR;
  private static final CustomFilenameFilter XML_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".xml";
    }

    @Override
    public String getActualDir() {
      return ROOT_DIR + FILE_SEPARATOR + "test_data" + FILE_SEPARATOR + "test_cases";
    }
  };
  public static final FilenameFilter DIRECTORY_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File current, String name) {
      File f = new File(current, name);
      return f.isDirectory() && f.list(XML_FILES_FILTER).length > 0;
    }
  };
  public static String ROOT_COMPARE_DIR;
  private static final CustomFilenameFilter COMPARE_XML_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".xml";
    }

    @Override
    public String getActualDir() {
      return ROOT_COMPARE_DIR + FILE_SEPARATOR + "test_data" + FILE_SEPARATOR + "test_cases";
    }
  };
  public static final FilenameFilter COMPARE_DIRECTORY_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File current, String name) {
      File f = new File(current, name);
      return f.isDirectory() && f.list(COMPARE_XML_FILES_FILTER).length > 0;
    }
  };
  public static String MV_BRANCH_TAG;

  static {
    FILE_SEPARATOR = System.getProperty("file.separator");
    if (System.getProperty("mv_root_dir") == null) {
      ROOT_DIR = "/d3/projects/METViewer/test_data";
    } else {
      ROOT_DIR = System.getProperty("mv_root_dir");  // This is the test dir/branch/tag
    }
    ROOT_COMPARE_DIR = System.getProperty(
        "mv_root_compare_dir"); // used for comparing test results to previous captured data

    if (System.getProperty("mv_host") == null) {
      HOST_NAME = "dakota.rap.ucar.edu";
    } else {
      HOST_NAME = System.getProperty("mv_host");
    }
    if (System.getProperty("mv_port") == null) {
      PORT = "3306";
    } else {
      PORT = System.getProperty("mv_port");
    }
    host = HOST_NAME + ":" + PORT;
    RWORK_DIR = ROOT_DIR + FILE_SEPARATOR + "R_work";
    //PLOTS_DIR = RWORK_DIR + FILE_SEPARATOR + "plots";
    PLOTS_DIR = ROOT_DIR + FILE_SEPARATOR + "output" + FILE_SEPARATOR + "plots" + FILE_SEPARATOR;
    //DATA_DIR = RWORK_DIR + FILE_SEPARATOR + "data";
    DATA_DIR = ROOT_DIR + FILE_SEPARATOR + "output" + FILE_SEPARATOR + "data" + FILE_SEPARATOR;
    //SCRIPTS_DIR = RWORK_DIR + FILE_SEPARATOR + "scripts";
    SCRIPTS_DIR = ROOT_DIR + FILE_SEPARATOR + "output" + FILE_SEPARATOR + "scripts" + FILE_SEPARATOR;
    LOAD_DIR = ROOT_DIR + FILE_SEPARATOR + "load_data";
    MET_DATA_DIR = ROOT_DIR + FILE_SEPARATOR + "met_data";
    if (System.getProperty("mv_database") == null) {
      database = "mv_test";
    } else {
      database = System.getProperty("mv_database");
    }
    if (System.getProperty("mv_user") == null) {
      USERNAME = "mvuser";
    } else {
      USERNAME = System.getProperty("mv_user");
    }
    if (System.getProperty("mv_pwd") == null) {
      PWD = "mvuser";
    } else {
      PWD = System.getProperty("mv_pwd");
    }
    rscript = "Rscript";
    TEMPLATE_DIR = ROOT_DIR + FILE_SEPARATOR + "R_tmpl/";
    list = false;
    verbose = true;
    sql = false;
    job_name = null;
    type = "mysql";
    driver = "com.mysql.jdbc.Driver";
  }

  private TestUtil() {
    throw new IllegalAccessError("Utility class");
  }

  public static void runBatch(String testDataDir, String plotType) {
    List<String> argsList = new ArrayList<>();
    if (list) {
      argsList.add("-list");
    }
    if (verbose) {
      argsList.add("-printSql");
    }

    String fpath = testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml";
    argsList.add(fpath);
    xlateTestSpec(fpath);
    if (job_name != null) {
      argsList.add(job_name);
    }
    String[] args = new String[argsList.size()];
    args = argsList.toArray(args);
    if (plotType.contains("scorecard")) {
      try {
        Scorecard.main(args);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    } else {
      MVBatch.main(args);
    }
  }

  public static void xlateTestSpec(String fpath) {
    // replace credentials from system properties

    String tag = "";
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document doc = dbf.newDocumentBuilder().parse(fpath);
      // these should only have one element
      tag = "host";
      if (doc.getElementsByTagName("host").item(0) != null) {
        doc.getElementsByTagName("host").item(0).setTextContent(TestUtil.host);
      }
      tag = "database";
      if (doc.getElementsByTagName("database").item(0) != null) {
        doc.getElementsByTagName("database").item(0).setTextContent(TestUtil.database);
      }
      tag = "user";
      if (doc.getElementsByTagName("user").item(0) != null) {
        doc.getElementsByTagName("user").item(0).setTextContent(TestUtil.USERNAME);
      }
      tag = "password";
      if (doc.getElementsByTagName("password").item(0) != null) {
        doc.getElementsByTagName("password").item(0).setTextContent(TestUtil.PWD);
      }
      tag = "r_tmpl";
      if (doc.getElementsByTagName("r_tmpl").item(0) != null) {
        doc.getElementsByTagName("r_tmpl").item(0).setTextContent(TestUtil.TEMPLATE_DIR);
      }
      tag = "r_work";
      if (doc.getElementsByTagName("r_work").item(0) != null) {
        doc.getElementsByTagName("r_work").item(0).setTextContent(TestUtil.RWORK_DIR);
      }

      // these could potentially have more than one element
      tag = "data";
      NodeList nodeList = doc.getElementsByTagName("data");
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodeList.item(i).setTextContent(TestUtil.DATA_DIR);
      }
      tag = "scripts";
      nodeList = doc.getElementsByTagName("scripts");
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodeList.item(i).setTextContent(TestUtil.SCRIPTS_DIR);
      }
      tag = "plots";
      nodeList = doc.getElementsByTagName("plots");
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodeList.item(i).setTextContent(TestUtil.PLOTS_DIR);
      }
      tag = "rscript";
      nodeList = doc.getElementsByTagName("rscript");
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodeList.item(i).setTextContent("Rscript");
      }
      tag = "folder_tmpl";
      nodeList = doc.getElementsByTagName("folder_tmpl");
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodeList.item(i)
            .setTextContent(MET_DATA_DIR + FILE_SEPARATOR + "{config}/{fcst_init}/{config1}");
      }

      //assign a group name
      tag = "group";
      Element group = doc.createElement(tag);
      group.setTextContent("Testing");
      nodeList = doc.getElementsByTagName("load_spec");
      nodeList.item(0).appendChild(group);

      TransformerFactory.newInstance().newTransformer()
          .transform(new DOMSource(doc), new StreamResult(new File(fpath)));
    } catch (Exception e) {
      System.out.println(
          "Exception translating tag " + tag + " for file " + fpath + ":" + e.getMessage());
    }
  }


  public static void cleanWorkingDirs() {
    boolean doClean = true;
    if (doClean) {
      try {
        File testDir = new File(PLOTS_DIR);
        FileUtils.cleanDirectory(testDir);
        testDir = new File(DATA_DIR);
        FileUtils.cleanDirectory(testDir);
        testDir = new File(SCRIPTS_DIR);
        FileUtils.cleanDirectory(testDir);
      } catch (IOException e) {
        out.println(e.getMessage());
      }
    }
  }

  public static void comparePointsFilesWithNames(String testDataDir, String axis, String plotType) {
    CustomFilenameFilter filenameFilter = null;
    if (axis.equals("1")) {
      filenameFilter = POINTS1_FILES_FILTER;
    } else if (axis.equals("2")) {
      filenameFilter = POINTS2_FILES_FILTER;
    }
    if (filenameFilter != null) {
      compareTextFiles(testDataDir, plotType, true, true, filenameFilter);
    }
  }

  public static void comparePointsFilesWithoutNames(
                                                       String testDataDir, String axis,
                                                       String plotType) {

    CustomFilenameFilter filenameFilter = null;
    if (axis.equals("1")) {
      filenameFilter = POINTS1_FILES_FILTER;
    } else if (axis.equals("2")) {
      filenameFilter = POINTS2_FILES_FILTER;
    }
    if (filenameFilter != null) {
      compareTextFiles(testDataDir, plotType, false, false, filenameFilter);
    }

  }

  public static void captureCreatedImages(String testDataDir, String plotType) {
    //get all test results datafiles
    File dir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File actualFileDir = new File(PLOT_FILES_FILTER.getActualDir());
    File[] newGeneratedFiles = actualFileDir.listFiles(
        PLOT_FILES_FILTER);   // this assumes that the PLOTS directory was cleaned each time
    for (int i = 0; i < newGeneratedFiles.length; i++) {
      out.println("copying image " + newGeneratedFiles[i]
                                         .getAbsolutePath() + " to " + dir.getAbsolutePath());
      try {
        FileUtils.copyFileToDirectory(newGeneratedFiles[i], dir);
      } catch (Exception e) {
        fail("Failed to capture image : " + newGeneratedFiles[i]
                                                .getAbsolutePath() + " with error: " + e.getMessage());
      }
    }
  }


  public static void comparePlotFilesWithNames(String testDataDir, String plotType) {
    compareBinaryFiles(testDataDir, plotType, true, true, PLOT_FILES_FILTER);

  }

  public static void comparePlotFilesWithoutNames(String testDataDir, String plotType) {
    compareBinaryFiles(testDataDir, plotType, false, false, PLOT_FILES_FILTER);

  }

  public static void compareBinaryFilesBySize(String testDataDir, String plotType) {
    compareBinaryFilesBySize(testDataDir, plotType, PLOT_FILES_FILTER);
  }

  public static void compareBinaryTestFiles(
                                               String testDataDir, String compareDataDir,
                                               String plotType) {
    compareBinaryTestFiles(testDataDir, compareDataDir, plotType, true, true, PLOT_FILES_FILTER);
  }

  /**
   * Compare the number of files, names (if requested) and contents
   *
   * @param testDataDir    - expected files root dir
   * @param plotType       - use case
   * @param isCompareNames - should the name comparison be executed
   * @param filter         - custom file filter to use
   */
  private static void compareTextFiles(
                                          String testDataDir, String plotType,
                                          boolean isCompareNames, boolean isCompareContent,
                                          CustomFilenameFilter filter) {
    //get all test results datafiles
    File dir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File[] expectedFiles = dir.listFiles(filter);

    for (int i = 0; i < expectedFiles.length; i++) {
      File actualFile = new File(filter.getActualDir() + "/" + expectedFiles[i].getName());
      if (isCompareNames) {

        assertTrue(actualFile.getName() + " does not exist.", actualFile.exists());
      }
      if (isCompareContent) {
        try {
          assertTrue(
              "Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile
                                                                                              .getName() + " must be identical but they are not",
              Files.lines(Paths.get(expectedFiles[i].getAbsolutePath())).count() == Files.lines(
                  Paths.get(actualFile.getAbsolutePath())).count()
          );
        } catch (Exception e) {
          try {
            Files.lines(Paths.get(expectedFiles[i].getAbsolutePath())).count();
          } catch (Exception ex) {
            out.println("Error reading file " + expectedFiles[i].getAbsolutePath());
            out.println(ex);
          }
          try {
            Files.lines(Paths.get(actualFile.getAbsolutePath())).count();
          } catch (Exception ex) {
            out.println("Error reading file " + actualFile.getAbsolutePath());
            out.println(ex);
          }

        }

        ArrayList<String> actualLines = new ArrayList<>();
        ArrayList<String> expectedLines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(actualFile.getAbsolutePath()))) {
          actualLines = stream.collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
          out.println(e);
        }
        try (Stream<String> stream = Files.lines(Paths.get(expectedFiles[i].getAbsolutePath()))) {
          expectedLines = stream.collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
          out.println(e);
        }
        for (int j = 0; j < actualLines.size(); j++) {
          assertTrue("Line " + expectedLines.get(j) + " from file " + actualFile
                                                                          .getAbsolutePath() + " is incorrect",
                     actualLines.get(j).equals(expectedLines.get(j)));
        }
      }
    }

  }

  private static void compareBinaryFilesBySize(
                                                  String testDataDir, String plotType,
                                                  CustomFilenameFilter filter) {
    File dir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File[] expectedFiles = dir.listFiles(filter);
    for (int i = 0; i < expectedFiles.length; i++) {
      File actualFile = new File(filter.getActualDir() + "/" + expectedFiles[i].getName());
      boolean areTheSameSize = actualFile.length() == expectedFiles[i].length();
      if ((!actualFile.exists() || !areTheSameSize) && (System.getProperty(
          "captureCreatedImages") != null)) {
        out.println("copying image " + actualFile.getAbsolutePath() + " to " + expectedFiles[i]
                                                                                   .getAbsolutePath());
        areTheSameSize = actualFile.length() == expectedFiles[i].length();
        try {
          FileUtils.copyFile(actualFile, expectedFiles[i]);
        } catch (Exception e) {
          fail("Failed to capture image : " + actualFile
                                                  .getAbsolutePath() + " with error: " + e.getMessage());
        }
      }
      assertTrue(actualFile.getName() + " does not exist.", actualFile.exists());
      assertTrue(
          "Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile
                                                                                          .getName() + " must be the same size but they are not",
          areTheSameSize);
    }
  }

  private static void compareBinaryFiles(
                                            String testDataDir, String plotType,
                                            boolean isCompareNames, boolean isCompareContent,
                                            CustomFilenameFilter filter) {
    //get all test results datafiles
    File dir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File[] expectedFiles = dir.listFiles(filter);
    for (int i = 0; i < expectedFiles.length; i++) {
      File actualFile = new File(filter.getActualDir() + "/" + expectedFiles[i].getName());
      if (isCompareNames) {
        assertTrue(actualFile.getName() + " does not exist.", actualFile.exists());
      }
      if (isCompareContent) {
        BufferedImage expectedImg = null;
        BufferedImage actualImg = null;
        boolean areTheSame = false;
        try {
          expectedImg = ImageIO.read(expectedFiles[i]);
          actualImg = ImageIO.read(actualFile);
          areTheSame = bufferedImagesEqual(expectedImg, actualImg);
        } catch (IOException e) {
        }
        assertTrue(
            "Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile
                                                                                            .getName() + " must be identical but they are not",
            areTheSame);
      }
    }
  }

  private static void compareBinaryTestFiles(
                                                String testDataDir, String compareDataDir,
                                                String plotType, boolean isCompareNames,
                                                boolean isCompareContent,
                                                CustomFilenameFilter filter) {
    //get all test results datafiles
    File testDir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File compDir = new File(compareDataDir + FILE_SEPARATOR + plotType);
    File[] expectedFiles = compDir.listFiles(filter);
    for (int i = 0; i < expectedFiles.length; i++) {
      File actualFile = new File(testDir + "/" + expectedFiles[i].getName());
      if (isCompareNames) {
        assertTrue(actualFile.getName() + " does not exist.", actualFile.exists());
      }
      if (isCompareContent) {
        BufferedImage expectedImg = null;
        BufferedImage actualImg = null;
        boolean areTheSame = false;
        try {
          out.println("comparing files " + expectedFiles[i].getAbsolutePath() + " to " + actualFile
                                                                                             .getAbsolutePath());
          expectedImg = ImageIO.read(expectedFiles[i]);
          actualImg = ImageIO.read(actualFile);
          areTheSame = bufferedImagesEqual(expectedImg, actualImg);
        } catch (IOException e) {
        }
        assertTrue(
            "Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile
                                                                                            .getName() + " in dir " + testDir
                                                                                                                          .getAbsolutePath() + " must be identical to a file in " + compDir
                                                                                                                                                                                        .getAbsolutePath() + " but is not",
            areTheSame);
      }
    }
  }


  /**
   * Reads the content of the file to a string
   *
   * @param requestFile - file to read
   * @return - content of the file as string
   */
  public static String readFileToString(File requestFile) {
    StringBuilder stringBuilder = new StringBuilder();
    try (FileReader fileReader = new FileReader(requestFile); BufferedReader reader = new BufferedReader(fileReader)) {
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
    } catch (Exception e) {
      out.println("Error reading file " + requestFile.getAbsolutePath());
      out.println(e);
    }

    return stringBuilder.toString();
  }

  static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
    if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
      for (int x = 0; x < img1.getWidth(); x++) {
        for (int y = 0; y < img1.getHeight(); y++) {
          if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
            return false;
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public abstract static class CustomFilenameFilter implements FilenameFilter {

    public abstract String getFileExtension();

    public abstract String getActualDir();

    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(getFileExtension());
    }
  }

}
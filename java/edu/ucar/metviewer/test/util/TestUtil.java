/**
 * TestUtil.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test.util;

import edu.ucar.metviewer.MVBatch;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

/**
 * @author : tatiana $
 * @version : 1.0 : 29/Jul/14 13:48 $
 */
public class TestUtil {

  public static final String FILE_SEPARATOR;
  private static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
    @Override
    public int compare(File o1, File o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };
  public static final String ROOT_DIR;
  public static final String RWORK_DIR;
  public static final String PLOTS_DIR;
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
  private static final CustomFilenameFilter XML_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".xml";
    }

    @Override
    public String getActualDir() {
      return PLOTS_DIR;
    }
  };
  public static final FilenameFilter DIRECTORY_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File current, String name) {
      File f = new File(current, name);
      return f.isDirectory() && f.list(XML_FILES_FILTER).length > 0;
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
  public static final String DATA_DIR;
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
  public static final String SCRIPTS_DIR;
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
  public static final String LOAD_DIR;
  public static  final String database;
  public static  final String USERNAME;
  public static  final String PWD;
  public static  final String host;
  public static final  String rscript;
  public static  final String TEMPLATE_DIR;
  public static boolean list = false;
  public static boolean verbose = false;
  public static boolean sql = false;
  public static String job_name = null;
  public static String type = null;
  public static String driver = null;

  static {
    ROOT_DIR = "/d3/projects/METViewer/test_data";
    FILE_SEPARATOR = System.getProperty("file.separator");

    RWORK_DIR = ROOT_DIR + FILE_SEPARATOR + "R_work";
    PLOTS_DIR = RWORK_DIR + FILE_SEPARATOR + "plots";
    DATA_DIR = RWORK_DIR + FILE_SEPARATOR + "data";
    SCRIPTS_DIR = RWORK_DIR + FILE_SEPARATOR + "scripts";
    LOAD_DIR = ROOT_DIR + FILE_SEPARATOR + "load_data";
    database = "mv_test";
    USERNAME = "mvuser";
    PWD = "mvuser";
    host = "dakota.rap.ucar.edu:3306";
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


    argsList.add(testDataDir + FILE_SEPARATOR + plotType + FILE_SEPARATOR + plotType + ".xml");
    if (job_name != null) {
      argsList.add(job_name);
    }
    String[] args = new String[argsList.size()];
    args = argsList.toArray(args);

    MVBatch.main(args);

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

  public static void comparePointsFilesWithoutNames(String testDataDir, String axis, String plotType) {

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


  public static void comparePlotFilesWithNames(String testDataDir, String plotType) {
    compareBinaryFiles(testDataDir, plotType, true, true, PLOT_FILES_FILTER);

  }

  public static void comparePlotFilesWithoutNames(String testDataDir, String plotType) {
    compareBinaryFiles(testDataDir, plotType, false, false, PLOT_FILES_FILTER);

  }


  /**
   * Compare the number of files, names (if requested) and contents
   *
   * @param testDataDir    - expected files root dir
   * @param plotType       - use case
   * @param isCompareNames - should the name comparison be executed
   * @param filter         - custom file filter to use
   */
  private static void compareTextFiles(String testDataDir, String plotType, boolean isCompareNames, boolean isCompareContent, CustomFilenameFilter filter) {
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
          assertTrue("Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile.getName() + " must be identical but they are not",
            Files.lines(Paths.get(expectedFiles[i].getAbsolutePath())).count() == Files.lines(Paths.get(actualFile.getAbsolutePath())).count()
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
          assertTrue("Line " + expectedLines.get(j) + " from file " + actualFile.getAbsolutePath() + " is incorrect", actualLines.get(j).equals(expectedLines.get(j)));
        }
      }
    }

  }

  private static void compareBinaryFiles(String testDataDir, String plotType, boolean isCompareNames, boolean isCompareContent, CustomFilenameFilter filter) {
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
        assertTrue("Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFile.getName() + " must be identical but they are not", areTheSame);

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

  public abstract static class CustomFilenameFilter implements FilenameFilter {

    public abstract String getFileExtension();

    public abstract String getActualDir();

    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(getFileExtension());
    }
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

}

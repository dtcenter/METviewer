/**
 * TestUtil.java
 * Copyright UCAR (c) 2014.
 * University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test.util;

import edu.ucar.metviewer.MVBatch;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author : tatiana $
 * @version : 1.0 : 29/Jul/14 13:48 $
 */
public class TestUtil {

  public static String rootDir;

  public final static String FILE_SEPARATOR;
  public static String rworkDir;
  public static String plotsDir;
  public static String dataDir;
  public static String scriptsDir;
  public static String loadDir;
  public static String database;
  public static String username;
  public static String pwd;
  public static String host;
  public static String rscript;
  public static String templateDir;
  public static boolean list = false;
  public static boolean verbose = false;
  public static boolean sql = false;
  public static String job_name = null;

  static {
    rootDir = "/d3/projects/METViewer/test_data";
    FILE_SEPARATOR = System.getProperty("file.separator");
    rworkDir = rootDir + FILE_SEPARATOR + "R_work";
    plotsDir = rworkDir + FILE_SEPARATOR + "plots";
    dataDir = rworkDir + FILE_SEPARATOR + "data";
    scriptsDir = rworkDir + FILE_SEPARATOR + "scripts";
    loadDir = rootDir + FILE_SEPARATOR + "load_data";
    database = "mv_test";
    username = "mvuser";
    pwd = "mvuser";
    host = "dakota.rap.ucar.edu:3306";
    rscript = "Rscript";
    templateDir = rootDir + FILE_SEPARATOR + "R_tmpl/";
    list = false;
    verbose = false;
    sql = false;
    job_name = null;
  }

  public static final FilenameFilter DIRECTORY_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File current, String name) {
      File f = new File(current, name);
      return f.isDirectory() && f.list(XML_FILES_FILTER).length > 0;
    }
  };

  private static final CustomFilenameFilter DATA_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".data";
    }

    @Override
    public String getActualDir() {
      return dataDir;
    }
  };
  private static final CustomFilenameFilter PLOT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".png";
    }

    @Override
    public String getActualDir() {
      return plotsDir;
    }
  };

  private static final CustomFilenameFilter POINTS1_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".points1";
    }

    @Override
    public String getActualDir() {
      return dataDir;
    }
  };

  private static final CustomFilenameFilter POINTS2_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".points2";
    }

    @Override
    public String getActualDir() {
      return dataDir;
    }
  };

  private static final CustomFilenameFilter XML_FILES_FILTER = new CustomFilenameFilter() {
      @Override
      public String getFileExtension() {
        return ".xml";
      }

      @Override
      public String getActualDir() {
        return plotsDir;
      }
    };


  private static final CustomFilenameFilter RSCRIPT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".R";
    }

    @Override
    public String getActualDir() {
      return scriptsDir;
    }
  };

  private static final CustomFilenameFilter SQL_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".sql";
    }

    @Override
    public String getActualDir() {
      return plotsDir;
    }
  };

  private static final CustomFilenameFilter DATA_AGG_STAT_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".data.agg_stat";
    }

    @Override
    public String getActualDir() {
      return dataDir;
    }
  };

  private static final CustomFilenameFilter AGG_STAT_INFO_FILES_FILTER = new CustomFilenameFilter() {
    @Override
    public String getFileExtension() {
      return ".agg_stat.info";
    }

    @Override
    public String getActualDir() {
      return dataDir;
    }

  };


  private static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
    @Override
    public int compare(File o1, File o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  public static void runBatch(String testDataDir, String plotType) {
    List<String> argsList = new ArrayList<>();
    if (list) {
      argsList.add("-list");
    }
    if (verbose) {
      argsList.add("-v");
    }
    if (sql) {
      argsList.add("-sql");
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
    try {
      File testDir = new File(plotsDir);
      FileUtils.cleanDirectory(testDir);
      testDir = new File(dataDir);
      FileUtils.cleanDirectory(testDir);
      testDir = new File(scriptsDir);
      FileUtils.cleanDirectory(testDir);
    } catch (IOException e) {
      out.println(e.getMessage());
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
      compareFiles(testDataDir, plotType, true, filenameFilter);
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
      compareFiles(testDataDir, plotType, false, filenameFilter);
    }

  }


  public static void compareDataFilesWithNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, true, DATA_FILES_FILTER);

  }

  public static void compareDataFilesWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, DATA_FILES_FILTER);

  }


  public static void comparePlotFilesWithNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, true, PLOT_FILES_FILTER);

  }

  public static void comparePlotFilesWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, PLOT_FILES_FILTER);

  }


  public static void compareRscriptFilesWithNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, true, RSCRIPT_FILES_FILTER);

  }

  public static void compareRscriptFilesWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, RSCRIPT_FILES_FILTER);

  }


  public static void compareSqlFilesWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, SQL_FILES_FILTER);

  }


  public static void compareAggStatInfoFilesWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, AGG_STAT_INFO_FILES_FILTER);
  }

  public static void compareAggStatInfoFilesWithNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, true, AGG_STAT_INFO_FILES_FILTER);
  }

  public static void compareDataAggStatWithNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, true, DATA_AGG_STAT_FILES_FILTER);
  }

  public static void compareDataAggStatWithoutNames(String testDataDir, String plotType) {
    compareFiles(testDataDir, plotType, false, DATA_AGG_STAT_FILES_FILTER);
  }

  /**
   * Compare the number of files, names (if requested) and contents
   * @param testDataDir - expected files root dir
   * @param plotType - use case
   * @param isCompareNames - should the name comparison be executed
   * @param filter - custom file filter to use
   */
  private static void compareFiles(String testDataDir, String plotType, boolean isCompareNames, CustomFilenameFilter filter) {
    //get all test results datafiles
    File[] actualFiles = new File(filter.getActualDir()).listFiles(filter);
    File dir = new File(testDataDir + FILE_SEPARATOR + plotType);
    File[] expectedFiles = dir.listFiles(filter);
    assertEquals("Number of files for " + plotType + " " + filter.getFileExtension() + " must be identical", expectedFiles.length, actualFiles.length);
    if (actualFiles.length > 0) {
      Arrays.sort(actualFiles, FILE_NAME_COMPARATOR);
      Arrays.sort(expectedFiles, FILE_NAME_COMPARATOR);

      for (int i = 0; i < actualFiles.length; i++) {
        if (isCompareNames) {
          assertTrue("File names for " + plotType + " " + filter.getFileExtension() + " should be identical but they are not", expectedFiles[i].getName().equals(actualFiles[i].getName()));
        }
        try {
          assertTrue("Files for " + plotType + " " + filter.getFileExtension() + " with name " + actualFiles[i].getName() + " must be identical but they are not", FileUtils.contentEquals(expectedFiles[i], actualFiles[i]));
        } catch (IOException e) {
          out.println(e.getMessage());
        }
      }
    }
  }

  /**
   * Reads the content of the file to a string
   * @param requestFile - file to read
   * @return - content of the file as string
   */
  public static String readFileToString(File requestFile) {
    BufferedReader reader = null;
    FileReader fileReader = null;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      fileReader = new FileReader(requestFile);
      reader = new BufferedReader(fileReader);
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
    } catch (IOException e) {
      out.println(e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          out.println(e.getMessage());
        }
      }
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
          out.println(e.getMessage());
        }
      }
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

}

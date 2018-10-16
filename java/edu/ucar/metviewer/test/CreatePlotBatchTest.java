/**
 * CreatePlotBatchTest.java Copyright UCAR (c) 2014. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2014.
 */

package edu.ucar.metviewer.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static edu.ucar.metviewer.test.util.TestUtil.*;



/**
 * Run batch mode tests
 * The config files and expected result for the regression testing should be in <root_dir>/plots_batch
 * Class passes each config file to MVBatch class and compare output files with the expected.
 * @author : tatiana $
 * @version : 1.0 : 22/Jul/14 14:52 $
 */
@RunWith(Parameterized.class)
public class CreatePlotBatchTest {

  private static String testDataDir;
   private String plotType = null;

   public CreatePlotBatchTest(String plotType) {
     super();
     this.plotType = plotType;
   }

   /**
    * register all possible test cases in the test directory
    * @return - test cases names
    * @throws Exception - if test directory doesn't exists or it is empty
    */
   @Parameterized.Parameters
   public static Collection<String[]> data() throws Exception {
     testDataDir = ROOT_DIR + FILE_SEPARATOR + "test_data" + FILE_SEPARATOR + "test_cases";

     File file = new File(testDataDir);
     if (!file.exists()) {
       throw new Exception(testDataDir + " doesn't exist.");
     }
     // does the testDataDir directory exist and does it have any files and are any of them .xml files?
     String[] directories = file.list(DIRECTORY_FILTER);
     Collection<String[]> plots = new ArrayList<>();
     for (String directory : directories) {
       plots.add(new String[]{directory});
     }
     if (plots.isEmpty()) {
       throw new Exception(testDataDir + " doesn't have any test cases.");
     }
     return plots;
   }

   @Before
   public void runScript() {
     if (System.getProperty("captureCreatedImages") != null) {
       // if we are capturing images we have to clean the working dir each run
       // because there are potentially multiple images created for each plotType
       // i.e. a test case can cause some number of images to be created
       // and we don't know how many.
       // METviewer doesn't create multiple output plot directories so
       // all of the generated plots are in the same output directory.
       // we have to take them all if we are capturing images.
       cleanWorkingDirs();
     }
     runBatch(testDataDir, plotType);
   }

   @Test
   public void compareOutputFiles() {
     if (System.getProperty("captureCreatedImages") == null) {
       if (!"ens_ss".equals(plotType)) {
         comparePointsFilesWithNames(testDataDir, "1", plotType);
         comparePointsFilesWithNames(testDataDir, "2", plotType);
       }
       comparePlotFilesWithNames(testDataDir, plotType);
     } else {
         captureCreatedImages(testDataDir, plotType);
         comparePlotFilesWithNames(testDataDir, plotType);
     }
   }


   @After
   public void cleanup() {
     if (System.getProperty("noClean") == null) {
       cleanWorkingDirs();
     }
   }

 }
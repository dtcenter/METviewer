/**
 * PhistJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:14 $
 */
public class PhistJobManager extends RhistJobManager {
  /**
   * Class for Probability Histogram or Histograms of probability integral transform
   * @param mvBatch
   */

  public PhistJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/metplotpy/plots/histogram/prob_hist.py";
  }
}

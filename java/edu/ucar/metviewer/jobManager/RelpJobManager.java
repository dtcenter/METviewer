/**
 * RelpJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:15 $
 */
public class RelpJobManager extends RhistJobManager {
  /**
   * Class for Relative Histogram or Histograms of relative position
   * @param mvBatch
   */

  public RelpJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/plots/histogram/rel_hist.py";
  }
}

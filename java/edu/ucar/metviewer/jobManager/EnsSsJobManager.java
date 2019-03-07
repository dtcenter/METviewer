/**
 * EnsSsJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVPlotJob;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:48 $
 */
public class EnsSsJobManager extends SeriesJobManager {

  public EnsSsJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected int getNumDepSeries(int intNumDep1Series, int intNumDep2Series, MVPlotJob job) {
    int intNumDepSeries = intNumDep1Series + intNumDep2Series;
    if (job.getEnsSsPtsDisp().equalsIgnoreCase("TRUE")) {
      intNumDepSeries *= 2;
    }
    return intNumDepSeries;
  }
  @Override
  protected void validateNumDepSeries(MVPlotJob job, int intNumDepSeries) {
    //do nothing
  }
}

/**
 * ContourJobManager.java Copyright UCAR (c) 2018. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer.jobManager;

import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.ValidationException;

/**
 * @author : tatiana $
 * @version : 1.0 : 27/03/18 15:23 $
 */
public class ContourJobManager extends SeriesJobManager {

  public ContourJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected int getNumDepSeries(int intNumDep1Series, int intNumDep2Series, MVPlotJob job) {
    return 1 - job.getDiffSeries1Count();
  }


  @Override
  protected int getNumberPlotCurves(Map.Entry[] listDep1Plot) {
    return 1;
  }

  @Override
  protected Map<String, String> createInfoMap(MVPlotJob job, int intNumDepSeries) throws ValidationException {
    Map<String, String> info = super.createInfoMap(job, intNumDepSeries);
    if (job.getDiffSeries1Count() == 0) {
      info.put("contour_diff", "FALSE");
    } else {
      info.put("contour_diff", "TRUE");
    }
    return info;
  }
}

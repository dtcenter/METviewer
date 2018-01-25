/**
 * TaylorJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:36 $
 */
public class TaylorJobManager extends SeriesJobManager {

  public TaylorJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected void run(MVPlotJob job) throws Exception {
    job.setEventEqual(Boolean.TRUE);
    job.setEqualizeByIndep(Boolean.TRUE);
    job.setAggSl1l2(Boolean.TRUE);
    job.setPlotFixValEq(new MVOrderedMap(job.getPlotFixVal()));
    String[] stats = new String[]{"FBAR", "FSTDEV", "OBAR", "OSTDEV", "PR_CORR", "RMSE"};
    for (String var : ((MVOrderedMap) job.getDepGroups()[0].get("dep1")).getKeyList()) {
      ((MVOrderedMap) job.getDepGroups()[0].get("dep1")).put(var, stats);
    }
    super.run(job);
  }

  @Override
  protected int getNumberPlotCurves(Map.Entry[] listDep1Plot) {
    return listDep1Plot.length;
  }

}

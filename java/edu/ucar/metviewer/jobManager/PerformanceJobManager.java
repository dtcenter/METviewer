/**
 * PerformanceJobManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric
 * Research (UCAR), National Center for Atmospheric Research (NCAR), Research Applications
 * Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.jobManager;

import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.ValidationException;

/**
 * @author : tatiana $
 * @version : 1.0 : 21/12/17 15:43 $
 */
public class PerformanceJobManager extends SeriesJobManager {

  public PerformanceJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }

  @Override
  protected int getNumberPlotCurves(Map.Entry[] listDep1Plot) {
    return 1;
  }

  @Override
  protected String getPythonScript() {
    return "/plots/performance_diagram/performance_diagram.py";
  }
  @Override
  protected  Map<String, Object> addPlotConfigs(Map<String, Object> yamlInfo, MVPlotJob job, int intNumDepSeries) throws ValidationException {
    Map<String, Object> result = super.addPlotConfigs(yamlInfo, job, intNumDepSeries);
    result.put("annotation_template", job.getAnnotationTemplate());
    return result;
  }


}

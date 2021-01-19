package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVPlotJob;
import edu.ucar.metviewer.ValidationException;

import java.util.Map;

public class EtbJobManager extends SeriesJobManager{
  public EtbJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/plots/equivalence_testing_bounds/equivalence_testing_bounds.py";
  }

  @Override
  protected Map<String, Object> addPlotConfigs(Map<String, Object> yamlInfo, MVPlotJob job, int intNumDepSeries) throws ValidationException {
    // Override num_iterations - no ned for bootstrapping
    Map<String, Object> info = super.addPlotConfigs(yamlInfo, job,  intNumDepSeries);
    info.put("num_iterations", 1);
    return info;
  }
}

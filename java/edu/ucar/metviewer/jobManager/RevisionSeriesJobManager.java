package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

public class RevisionSeriesJobManager extends SeriesJobManager{
  public RevisionSeriesJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/metplotpy/plots/revision_series/revision_series.py";
  }
}

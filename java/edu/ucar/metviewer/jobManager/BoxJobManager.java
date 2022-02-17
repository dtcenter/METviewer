package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

public class BoxJobManager extends SeriesJobManager{
  public BoxJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/metplotpy/plots/box/box.py";
  }
}

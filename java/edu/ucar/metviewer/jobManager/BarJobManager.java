package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

public class BarJobManager extends SeriesJobManager{
  public BarJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/plots/bar/bar.py";
  }
}

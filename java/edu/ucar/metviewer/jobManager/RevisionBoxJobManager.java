package edu.ucar.metviewer.jobManager;

import edu.ucar.metviewer.MVBatch;

public class RevisionBoxJobManager extends SeriesJobManager{
  public RevisionBoxJobManager(MVBatch mvBatch) {
    super(mvBatch);
  }
  @Override
  protected String getPythonScript() {
    return "/metplotpy/plots/revision_box/revision_box.py";
  }
}

/**
 * RscriptStatManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.rscriptManager;

import java.util.List;
import java.util.Map;

import edu.ucar.metviewer.MVBatch;
import edu.ucar.metviewer.MVOrderedMap;
import edu.ucar.metviewer.MVPlotJob;

/**
 * @author : tatiana $
 * @version : 1.0 : 22/12/17 10:12 $
 */
public abstract class RscriptStatManager {
  protected final MVBatch mvBatch;
  protected String dataFile;
  protected String rScriptFile;
  protected String plotFile="";

  public String getPlotFile() {
    return plotFile;
  }

  protected RscriptStatManager(final MVBatch mvBatch){
    this.mvBatch = mvBatch;
  }

  public abstract void prepareDataFileAndRscript(MVPlotJob job, MVOrderedMap mvMap,
                                                 Map<String, String> info,
                                                 List<String> listQuery) throws Exception;
  public abstract boolean runRscript(MVPlotJob job, Map<String, String> info);


}

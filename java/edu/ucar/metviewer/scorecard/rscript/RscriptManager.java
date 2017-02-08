/**
 * RscriptManager.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.rscript;

import edu.ucar.metviewer.scorecard.Scorecard;
import edu.ucar.metviewer.scorecard.model.Entry;
import edu.ucar.metviewer.scorecard.model.Field;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 07/02/17 09:22 $
 */
public abstract class RscriptManager {
  private static final Logger logger = Logger.getLogger(RscriptManager.class);
  protected final Map<String, List<Entry>> listColumns;
  protected final List<Field> fixedVars;
  protected final String rScriptCommand;


  public RscriptManager(final Scorecard scorecard) {
    this.listColumns = scorecard.columnsStructure();
    fixedVars = scorecard.getFixedVars();
    rScriptCommand = scorecard.getrScriptCommand();
  }

  public abstract void calculateStats(Map<String, Entry> mapRow) ;

}

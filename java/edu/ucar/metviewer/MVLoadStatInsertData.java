/**
 * MVLoadStatInsertData.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MVLoadStatInsertData is used to store insert value lists for the various types of grid_stat and point_stat tables.  The structure is built in loadStatFile()
 * and is unloaded and executed in commitStatData().
 *
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 12:03 $
 */


public class MVLoadStatInsertData {

  private final List<String> listInsertValues = new ArrayList<>();
  private final Map<String, List<String>> tableLineDataValues = new HashMap<>();
  private final List<String> listStatGroupInsertValues = new ArrayList<>();
  private final Map<String, List<List<Object>>> tableVarLengthValues = new HashMap<>();
  private String lineType = "";
  private String fileLine = "";

  public List<String> getListInsertValues() {
    return listInsertValues;
  }

  public Map<String, List<String>> getTableLineDataValues() {
    return tableLineDataValues;
  }

  public List<String> getListStatGroupInsertValues() {
    return listStatGroupInsertValues;
  }

  public Map<String, List<List<Object>>> getTableVarLengthValues() {
    return tableVarLengthValues;
  }

  public String getLineType() {
    return lineType;
  }

  public void setLineType(String lineType) {
    this.lineType = lineType;
  }

  public String getFileLine() {
    return fileLine;
  }

  public void setFileLine(String fileLine) {
    this.fileLine = fileLine;
  }
}

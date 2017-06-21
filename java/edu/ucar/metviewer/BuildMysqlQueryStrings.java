/**
 * BuildMysqlQueryStrings.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer;

import java.util.Arrays;
import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 31/05/17 12:49 $
 */
public class BuildMysqlQueryStrings {

  private final boolean modePlot;
  private final Map<String, String> headerSQLType;
  private final Map.Entry[] series;
  private String selectList = "";
  private String tempList = "";
  private String where = "";
  boolean isFormatSelect = true;


  public BuildMysqlQueryStrings(boolean modePlot, Map<String, String> headerSQLType, Map.Entry[] series, String where, boolean isFormatSelect) {
    this.modePlot = modePlot;
    this.headerSQLType = headerSQLType;
    this.series = Arrays.copyOf(series, series.length);

    this.where = where;
    this.isFormatSelect = isFormatSelect;
  }

  public String getSelectList() {
    return selectList;
  }

  public String getTempList() {
    return tempList;
  }

  public String getWhere() {
    return where;
  }

  public boolean isModePlot() {
    return modePlot;
  }

  public Map<String, String> getHeaderSQLType() {
    return headerSQLType;
  }

  public Map.Entry[] getSeries() {
    return series;
  }

  public void setSelectList(String selectList) {
    this.selectList = selectList;
  }

  public void setTempList(String tempList) {
    this.tempList = tempList;
  }

  public void setWhere(String where) {
    this.where = where;
  }

  public boolean isFormatSelect() {
    return isFormatSelect;
  }

  public void setFormatSelect(boolean formatSelect) {
    isFormatSelect = formatSelect;
  }
}


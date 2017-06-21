/**
 * Table.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

/**
 * @author : tatiana $
 * @version : 1.0 : 02/12/16 08:41 $
 */
class Table {

  private String name;
  private String dependentTable;
  private final String headerTable;
  private final String headerKey;
  private String dependentKey;

  public Table(String name, String headerTable, String headerKey, String dependentTable, String dependentKey) {
    this.name = name;
    this.dependentKey = dependentKey;
    this.dependentTable = dependentTable;
    this.headerKey = headerKey;
    this.headerTable = headerTable;
  }

  public Table(String name, String headerTable, String headerKey) {
    this.name = name;
    this.headerTable = headerTable;
    this.headerKey = headerKey;
  }

  public String getDependentKey() {
    return dependentKey;
  }

  public String getDependentTable() {
    return dependentTable;
  }

  public String getHeaderKey() {
    return headerKey;
  }

  public String getHeaderTable() {
    return headerTable;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Table{" +
      " name='" + name + '\'' +
      ", headerTable='" + headerTable + '\'' +
      ", headerKey='" + headerKey + '\'' +
      ", dependentTable='" + dependentTable + '\'' +
      ", dependentKey='" + dependentKey + '\'' +


      '}';
  }

}



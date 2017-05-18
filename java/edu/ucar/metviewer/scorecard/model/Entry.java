/**
 * Entry.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.model;

/**
 * @author : tatiana $
 * @version : 1.0 : 12/01/17 11:55 $
 */
public class Entry {

  private String name;
  private String label;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    if (label == null) {
      return name;
    }
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof Entry)) {
      return false;
    }

    Entry entry = (Entry) obj;
    return entry.name.equals(name);
  }

  @Override
  public String toString() {
    return "Entry{" +
      "name='" + name + '\'' +
      ", label='" + label + '\'' +
      '}';
  }
}

/**
 * Field.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/12/16 14:46 $
 */
public class Field {
  private String name;
  private List<Entry> values = new ArrayList<>();
  private List<Field> fields = new ArrayList<>();

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    this.fields = fields;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Entry> getValues() {
    return values;
  }

  public void setValues(List<Entry> values) {
    this.values = values;
  }


}

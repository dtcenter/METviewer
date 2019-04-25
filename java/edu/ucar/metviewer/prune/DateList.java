/**
 * DateList.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.prune;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.ucar.metviewer.ParsingException;

/**
 * @author : tatiana $
 * @version : 1.0 : 02/12/16 08:41 $
 */
class DateList {

  private String name;
  private String startStr;
  private String endStr;
  private Integer increment;
  private String formatStr;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setEndStr(String endStr) {
    this.endStr = endStr;
  }

  public void setFormatStr(String formatStr) {
    this.formatStr = formatStr;
  }

  public void setIncrement(Integer increment) {
    this.increment = increment;
  }

  public void setStartStr(String startStr) {
    this.startStr = startStr;
  }

  public List<String> getValues() throws ParsingException {
    validate();
    List<String> values = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatStr);
    LocalDateTime start;
    LocalDateTime end;
    try {
      start = LocalDateTime.parse(startStr, formatter);
    } catch (Exception e) {
      throw new ParsingException("Date format can't be applied to the start date in 'date_list' "
              + "element");
    }
    try {
      end = LocalDateTime.parse(endStr, formatter);
    } catch (Exception e) {
      throw new ParsingException(
              "Date format can't be applied to the end date in 'date_list' element");
    }
    while (start.isBefore(end) || start.isEqual(end)) {
      values.add(start.format(formatter));
      start = start.plusSeconds(increment);
    }
    return values;
  }

  private void validate() throws ParsingException {
    if (name == null) {
      throw new ParsingException("Attribute 'name' in 'date_list' element is missing");
    }
    if (startStr == null) {
      throw new ParsingException("Element 'start' in 'date_list' element is missing");
    }
    if (endStr == null) {
      throw new ParsingException("Element 'end' in 'date_list' element is missing");
    }
    if (increment == null) {
      throw new ParsingException("Element 'inc' in 'date_list' element is missing");
    }
    if (formatStr == null) {
      throw new ParsingException("Element 'format' in 'date_list' element is missing");
    }

  }
}

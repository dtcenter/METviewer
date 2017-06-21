/**
 * LegendRange.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.math.BigDecimal;

/**
 * Description how the threshold would be represented in the table and legend
 *
 * @author : tatiana $
 * @version : 1.0 : 31/01/17 13:44 $
 */
public class LegendRange {

  private static final Logger logger = LogManager.getLogger("LegendRange");

  private String symbol = ""; //HTML symbol
  private String color = "#000000"; // color of the symbol
  private String background = "#FFFFFF"; // cell's background
  private BigDecimal upperLimit;
  private BigDecimal lowerLimit;
  private boolean includeUpperLimit;
  private boolean includeLowerLimit;
  private String formatString = "";//format for the legend text


  public void setFormatString(String formatString) {
    if (formatString != null) {
      this.formatString = formatString;
    } else {
      this.formatString = "";
    }
  }

  public void setSymbol(String symbol) {
    if (symbol != null) {
      this.symbol = symbol;
    } else {
      this.symbol = "";
    }
  }

  public void setColor(String color) {
    if (validateColorStr(color)) {
      this.color = color;
    } else {
      this.color = "#000000";//black
      logger.error("Font color for the legend range is invalid: " + color + ". Use black color instead");
    }
  }

  public void setBackground(String background) {
    if (validateColorStr(background)) {
      this.background = background;
    } else {
      this.background = "#FFFFFF";//white
      logger.error("Background color for the legend range is invalid: " + background + ". Use white color instead");
    }
  }

  public void setUpperLimit(BigDecimal upperLimit) {
    this.upperLimit = upperLimit;
  }

  public void setLowerLimit(BigDecimal lowerLimit) {
    this.lowerLimit = lowerLimit;
  }

  public void setIncludeUpperLimit(boolean includeUpperLimit) {
    this.includeUpperLimit = includeUpperLimit;
  }

  public void setIncludeLowerLimit(boolean includeLowerLimit) {
    this.includeLowerLimit = includeLowerLimit;
  }

  public String getBackground() {
    return background;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getColor() {
    return color;
  }

  public BigDecimal getUpperLimit() {
    return upperLimit;
  }

  public BigDecimal getLowerLimit() {
    return lowerLimit;
  }

  public boolean isIncludeUpperLimit() {
    return includeUpperLimit;
  }

  public boolean isIncludeLowerLimit() {
    return includeLowerLimit;
  }

  public String getFormatString() {
    return formatString;
  }


  private boolean validateColorStr(String colorStr) {
    Color tmpColor = null;
    try {
      tmpColor = Color.decode(colorStr);
      // color is a valid color
    } catch (IllegalArgumentException iae) {
      // This color string is not valid
    }
    return tmpColor != null;
  }
}

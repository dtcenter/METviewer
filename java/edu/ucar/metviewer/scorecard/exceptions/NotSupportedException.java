package edu.ucar.metviewer.scorecard.exceptions;

public class NotSupportedException extends Exception {

  public NotSupportedException(String statistic, String type) {
    super("Statistic " + statistic + " not supported for " + type);
  }

}

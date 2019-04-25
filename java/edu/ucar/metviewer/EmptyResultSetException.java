/**
 * EmptyResultSetException.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer;

/**
 * @author : tatiana $
 * @version : 1.0 : 10/02/17 08:37 $
 */
public class EmptyResultSetException extends Exception {
  public EmptyResultSetException(String message) {
    super(message);
  }
}

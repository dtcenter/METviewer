/**
 * MissingFileException.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.exceptions;

/**
 * @author : tatiana $
 * @version : 1.0 : 25/01/17 09:37 $
 */
public class MissingFileException extends Exception {

  public MissingFileException(String fileName) {
    super("File " + fileName + " doesn't exist");
  }

}

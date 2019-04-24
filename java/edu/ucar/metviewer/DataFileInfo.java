/**
 * DataFileInfo.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer;

import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:56 $
 */
public class DataFileInfo {

  public Integer fileId = 0;
  public String fileIdStr = "";
  public String filename = "";
  public String path = "";
  public String loadDate = "";
  public String modDate = "";
  public int luId = -1;
  public String luTypeName = "";
  public boolean lineTypeLoad;
  public Map tableLineTypeLoad;
  public boolean loadMpr;
  public boolean loadOrank;
  public boolean statHeaderDBCheck;
  public boolean modeHeaderDBCheck;
  public boolean mtdHeaderDBCheck;
  public boolean verbose;
  public int insertSize;

  public DataFileInfo(
          Integer fileId, String filename,
          String dataFilePath, String dataFileLoadDate,
          String dataFileModDate, int dataFileLuId,
          String dataFileLuTypeName) {
    this.fileId = fileId;
    this.filename = filename;
    path = dataFilePath;
    loadDate = dataFileLoadDate;
    modDate = dataFileModDate;
    luId = dataFileLuId;
    luTypeName = dataFileLuTypeName;
  }

  public DataFileInfo(
          String fileIdStr, String filename,
          String dataFilePath, String dataFileLoadDate,
          String dataFileModDate, int dataFileLuId,
          String dataFileLuTypeName) {
    this.fileIdStr = fileIdStr;
    this.filename = filename;
    path = dataFilePath;
    loadDate = dataFileLoadDate;
    modDate = dataFileModDate;
    luId = dataFileLuId;
    luTypeName = dataFileLuTypeName;
  }
}

/**
 * DataFileInfo.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR), Research
 * Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer;

import java.util.Map;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/06/17 11:56 $
 */
public class DataFileInfo {

  public Integer _dataFileId = 0;
  public String _dataFileFilename = "";
  public String _dataFilePath = "";
  public String _dataFileLoadDate = "";
  public String _dataFileModDate = "";
  public int _dataFileLuId = -1;
  public String _dataFileLuTypeName = "";
  public boolean _boolLineTypeLoad;
  public Map _tableLineTypeLoad;
  public boolean _boolLoadMpr;
  public boolean _boolLoadOrank;
  public boolean _boolStatHeaderDBCheck;
  public boolean _boolModeHeaderDBCheck;
  public boolean _boolMtdHeaderDBCheck;
  public boolean _boolVerbose;
  public int _intInsertSize;

  public DataFileInfo(Integer dataFileId, String dataFileFilename,
                      String dataFilePath, String dataFileLoadDate,
                      String dataFileModDate, int dataFileLuId,
                      String dataFileLuTypeName) {
    _dataFileId = dataFileId;
    _dataFileFilename = dataFileFilename;
    _dataFilePath = dataFilePath;
    _dataFileLoadDate = dataFileLoadDate;
    _dataFileModDate = dataFileModDate;
    _dataFileLuId = dataFileLuId;
    _dataFileLuTypeName = dataFileLuTypeName;
  }
}

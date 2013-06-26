package edu.ucar.metviewer;

import java.sql.Connection;

public class MVLoadJob {

  protected Connection _con = null;


  protected String _strDBHost = "";
  protected String _strDBName = "";
  protected String _strDBUser = "";
  protected String _strDBPassword = "";

  protected boolean _boolModSchema = false;

  protected boolean _boolLineTypeLoad = false;
  protected MVOrderedMap _mapLineTypeLoad = new MVOrderedMap();

  protected boolean _boolLoadStat = true;
  protected boolean _boolLoadMode = true;

  protected boolean _boolLoadMpr = false;
  protected boolean _boolLoadOrank = false;

  protected boolean _boolForceDupFile = false;

  protected boolean _boolVerbose = false;
  protected int _intInsertSize = 1;
  protected boolean _boolModeHeaderDBCheck = true;
  protected boolean _boolStatHeaderDBCheck = true;
  protected boolean _boolDropIndexes = false;
  protected boolean _boolApplyIndexes = true;

  protected String _strFolderTmpl = "";
  protected MVOrderedMap _mapLoadVal = new MVOrderedMap();

  protected String[] _listLoadFiles = {};

  protected String _strLoadNote = "";
  protected boolean _boolLoadXML = true;

  public Connection getConnection() {
    return _con;
  }

  public void setConnection(Connection con) {
    _con = con;
  }



  public String getDBHost() {
    return _strDBHost;
  }

  public void setDBHost(String dbHost) {
    _strDBHost = dbHost;
  }

  public String getDBName() {
    return _strDBName;
  }

  public void setDBName(String dbName) {
    _strDBName = dbName;
  }

  public String getDBUser() {
    return _strDBUser;
  }

  public void setDBUser(String dbUser) {
    _strDBUser = dbUser;
  }

  public String getDBPassword() {
    return _strDBPassword;
  }

  public void setDBPassword(String dbPassword) {
    _strDBPassword = dbPassword;
  }

  public boolean getModSchema() {
    return _boolModSchema;
  }

  public void setModSchema(boolean modSchema) {
    _boolModSchema = modSchema;
  }

  public boolean getLineTypeLoad() {
    return _boolLineTypeLoad;
  }

  public void setLineTypeLoad(boolean lineTypeLoad) {
    _boolLineTypeLoad = lineTypeLoad;
  }

  public MVOrderedMap getLineTypeLoadMap() {
    return _mapLineTypeLoad;
  }

  public void addLineTypeLoad(String type) {
    _mapLineTypeLoad.put(type, true);
  }

  public void removeLineTypeLoad(String type) {
    _mapLineTypeLoad.remove(type);
  }

  public void clearLineTypeLoad() {
    _mapLineTypeLoad = new MVOrderedMap();
  }

  public boolean getLoadStat() {
    return _boolLoadStat;
  }

  public void setLoadStat(boolean loadStat) {
    _boolLoadStat = loadStat;
  }

  public boolean getLoadMode() {
    return _boolLoadMode;
  }

  public void setLoadMode(boolean loadMode) {
    _boolLoadMode = loadMode;
  }

  public boolean getLoadMpr() {
    return _boolLoadMpr;
  }

  public void setLoadMpr(boolean loadMpr) {
    _boolLoadMpr = loadMpr;
  }

  public boolean getLoadOrank() {
    return _boolLoadOrank;
  }

  public void setLoadOrank(boolean loadOrank) {
    _boolLoadOrank = loadOrank;
  }

  public boolean getForceDupFile() {
    return _boolForceDupFile;
  }

  public void setForceDupFile(boolean forceDupFile) {
    _boolForceDupFile = forceDupFile;
  }

  public boolean getVerbose() {
    return _boolVerbose;
  }

  public void setVerbose(boolean verbose) {
    _boolVerbose = verbose;
  }

  public int getInsertSize() {
    return _intInsertSize;
  }

  public void setInsertSize(int insertSize) {
    _intInsertSize = insertSize;
  }

  public boolean getModeHeaderDBCheck() {
    return _boolModeHeaderDBCheck;
  }

  public void setModeHeaderDBCheck(boolean modeHeaderDBCheck) {
    _boolModeHeaderDBCheck = modeHeaderDBCheck;
  }

  public boolean getStatHeaderDBCheck() {
    return _boolStatHeaderDBCheck;
  }

  public void setStatHeaderDBCheck(boolean statHeaderDBCheck) {
    _boolStatHeaderDBCheck = statHeaderDBCheck;
  }

  public boolean getDropIndexes() {
    return _boolDropIndexes;
  }

  public void setDropIndexes(boolean dropIndexes) {
    _boolDropIndexes = dropIndexes;
  }

  public boolean getApplyIndexes() {
    return _boolApplyIndexes;
  }

  public void setApplyIndexes(boolean applyIndexes) {
    _boolApplyIndexes = applyIndexes;
  }

  public String getFolderTmpl() {
    return _strFolderTmpl;
  }

  public void setFolderTmpl(String folderTmpl) {
    _strFolderTmpl = folderTmpl;
  }

  public MVOrderedMap getLoadVal() {
    return _mapLoadVal;
  }

  public void addLoadVal(String field, String[] vals, int index) {
    _mapLoadVal.put(field, vals, index);
  }

  public void addLoadVal(String field, String[] vals) {
    addLoadVal(field, vals, _mapLoadVal.size());
  }

  public void removeLoadVal(String field) {
    _mapLoadVal.remove(field);
  }

  public void clearLoadVal() {
    _mapLoadVal = new MVOrderedMap();
  }

  public String[] getLoadFiles() {
    return _listLoadFiles;
  }

  public void setLoadFiles(String[] loadFiles) {
    _listLoadFiles = loadFiles;
  }

  public String getLoadNote() {
    return _strLoadNote;
  }

  public void setLoadNote(String loadNote) {
    _strLoadNote = loadNote;
  }

  public boolean getLoadXML() {
    return _boolLoadXML;
  }

  public void setLoadXML(boolean loadXML) {
    _boolLoadXML = loadXML;
  }
}

/**
 * DatabaseInfo.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.db;

/**
 * @author : tatiana $
 * @version : 1.0 : 19/05/17 12:47 $
 */
public class DatabaseInfo {

  private String host;
  private String user;
  private String password;
  private String dbName;

  public DatabaseInfo(String host, String user, String password) {
    this.host = host;
    this.user = user;
    this.password = password;
  }

  public DatabaseInfo() {
  }


  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }
}

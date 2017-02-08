/**
 * WorkingFolders.java Copyright UCAR (c) 2016. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research (NCAR),
 * Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2016.
 */

package edu.ucar.metviewer.scorecard.model;

/**
 * @author : tatiana $
 * @version : 1.0 : 22/12/16 12:20 $
 */
public class WorkingFolders {
  private String plotsDir;
  private String dataDir;
  private String scriptsDir;
  private String rWorkDir;
  private String rTemplateDir;

  public String getDataDir() {
    return dataDir;
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir;
  }

  public String getPlotsDir() {
    return plotsDir;
  }

  public void setPlotsDir(String plotsDir) {
    this.plotsDir = plotsDir;
  }

  public String getrTemplateDir() {
    return rTemplateDir;
  }

  public void setrTemplateDir(String rTemplateDir) {
    this.rTemplateDir = rTemplateDir;
  }

  public String getrWorkDir() {
    return rWorkDir;
  }

  public void setrWorkDir(String rWorkDir) {
    this.rWorkDir = rWorkDir;
  }

  public String getScriptsDir() {
    return scriptsDir;
  }

  public void setScriptsDir(String scriptsDir) {
    this.scriptsDir = scriptsDir;
  }
}

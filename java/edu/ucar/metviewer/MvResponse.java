/**
 * MvResponse.java Copyright UCAR (c) 2018. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer;

/**
 * @author : tatiana $
 * @version : 1.0 : 06/11/18 09:43 $
 */
public class MvResponse {

  private boolean success = false;
  private String infoMessage = "";
  private String errorMessage = "";

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getInfoMessage() {
    return infoMessage;
  }

  public void setInfoMessage(String infoMessage) {
    this.infoMessage = infoMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}

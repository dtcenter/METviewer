package edu.ucar.metviewer;

public class LineAttributes {
  private String type=""; //vert or horiz
  private String lty="1"; //line type (solid, dotted,..)
  private String lwd = "1"; // line width
  private String color = "#ff0000";
  private String position=""; // x for vert and y for horiz

  public String getType() {
    return type;
  }

  public void setType(String type) {
    if (type.equals("vert_line") || type.equals("horiz_line")){
      this.type = type;
    }
  }

  public String getLty() {
    return lty;
  }

  public void setLty(String lty) {
      this.lty = lty;
  }

  public String getLwd() {
    return lwd;
  }

  public void setLwd(String lwd) {
      this.lwd = lwd;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
      this.position = position;
  }
}

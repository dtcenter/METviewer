package edu.ucar.metviewer;

/**
 * Storage class for plot feature dependency information.  If a xml plot
 * specification contains features that are dependent on other plot
 * information, the database variable should be stored along with the xml plot
 * specification node.  When the plot is generated the dependent information
 * will be parsed and generated.
 */
public class MVPlotDep {

  protected String _strDepVar = "";
  protected MVNode _nodeSpec = null;

  public String getDepVar() {
    return _strDepVar;
  }

  public void setDepVar(String depVar) {
    _strDepVar = depVar;
  }

  public MVNode getSpec() {
    return _nodeSpec;
  }

  public void setSpec(MVNode spec) {
    _nodeSpec = spec;
  }

  public MVPlotDep(String depVar, MVNode node) {
    _strDepVar = depVar;
    _nodeSpec = node;
  }
}
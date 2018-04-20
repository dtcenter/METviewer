package edu.ucar.metviewer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MVNode {

  protected Node node = null;
  public String tag = "";
  public String name = "";
  protected String label = "";
  protected String inherits = "";
  protected String id = "";
  protected String run = "";
  protected String depends = "";
  protected String plotVal = "";
  public String value = "";
  protected String equalize = "";
  public MVNode[] children = {};

  public MVNode(Node node) {
    this.node = node;
    tag = node.getNodeName();

    //  get the node name attribute value, if present
    NamedNodeMap mapAttr = node.getAttributes();
    for (int i = 0; i < mapAttr.getLength(); i++) {
      Node nodeAttr = mapAttr.item(i);
      String strAttrName = nodeAttr.getNodeName();
      if (strAttrName.equals("name")) {
        name = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("label")) {
        label = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("inherits")) {
        inherits = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("id")) {
        id = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("run")) {
        run = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("depends")) {
        depends = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("plot_val")) {
        plotVal = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("equalize")) {
        equalize = nodeAttr.getNodeValue();
      } else {
        System.out.println(
            "  **  WARNING: unrecognized attribute name '" + strAttrName
                + "' in node '" + tag + "'");
      }
    }

    List<MVNode> listChildren = new ArrayList<>();
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node nodeChild = list.item(i);

      if (Node.TEXT_NODE == nodeChild.getNodeType()) {
        if (nodeChild.getNodeValue().matches("\\s*")) {
          continue;
        }
        value = nodeChild.getNodeValue();
      } else if (Node.ELEMENT_NODE == nodeChild.getNodeType()) {
        listChildren.add(new MVNode(nodeChild));
      }
    }
    children = (!listChildren.isEmpty()
                    ?  listChildren.toArray(new MVNode[]{}) : new MVNode[]{});
  }


  public String getAttribute(String attributeName) {
    String attributeValue = null;
    NamedNodeMap namedNodeMap = node.getAttributes();
    if (namedNodeMap.getLength() > 0) {
      try {
        attributeValue = namedNodeMap.getNamedItem(attributeName).getNodeValue();
      } catch (NullPointerException e) {
      }
    }
    return attributeValue;
  }

}



package edu.ucar.metviewer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class MVNode {

  protected Node _node = null;
  protected String _tag = "";
  protected String _name = "";
  protected String _label = "";
  protected String _inherits = "";
  protected String _id = "";
  protected String _run = "";
  protected String _depends = "";
  protected String _plotVal = "";
  protected String _value = "";
  protected String _equalize = "";
  protected MVNode[] _children = {};

  public MVNode(Node node) {
    _node = node;
    _tag = node.getNodeName();

    //  get the node name attribute value, if present
    NamedNodeMap mapAttr = node.getAttributes();
    for (int i = 0; i < mapAttr.getLength(); i++) {
      Node nodeAttr = mapAttr.item(i);
      String strAttrName = nodeAttr.getNodeName();
      if (strAttrName.equals("name")) {
        _name = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("label")) {
        _label = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("inherits")) {
        _inherits = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("id")) {
        _id = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("run")) {
        _run = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("depends")) {
        _depends = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("plot_val")) {
        _plotVal = nodeAttr.getNodeValue();
      } else if (strAttrName.equals("equalize")) {
        _equalize = nodeAttr.getNodeValue();
      } else {
        System.out.println("  **  WARNING: unrecognized attribute name '" + strAttrName + "' in node '" + _tag + "'");
      }
    }

    ArrayList listChildren = new ArrayList();
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node nodeChild = list.item(i);

      if (Node.TEXT_NODE == nodeChild.getNodeType()) {
        if (nodeChild.getNodeValue().matches("\\s*")) {
          continue;
        }
        _value = nodeChild.getNodeValue();
      } else if (Node.ELEMENT_NODE == nodeChild.getNodeType()) {
        listChildren.add(new MVNode(nodeChild));
      }
    }
    _children = (!listChildren.isEmpty() ? (MVNode[]) listChildren.toArray(new MVNode[]{}) : new MVNode[]{});
  }

  public static String printNode(MVNode mvnode, int lev) {
    String strRet = tabPad(lev) + "<" + mvnode._tag;
    if (null != mvnode._name) {
      strRet += " name=\"" + mvnode._name + "\"";
    }

    boolean boolCloseTag = true;
    if (null == mvnode._value && null == mvnode._children) {
      strRet += " /";
      boolCloseTag = false;
    }
    strRet += ">";

    if (null != mvnode._value) {
      strRet += mvnode._value;
    } else {
      strRet += "\n";
    }

    if (null != mvnode._children) {
      for (int i = 0; i < mvnode._children.length; i++) {
        strRet += printNode(mvnode._children[i], lev + 1);
      }
      strRet += tabPad(lev) + "</" + mvnode._tag + ">\n";
    } else if (boolCloseTag) {
      strRet += "</" + mvnode._tag + ">\n";
    }
    return strRet;
  }


  public static String tabPad(int lev) {
    String pad = "";
    for (int i = 0; i < lev; i++) {
      pad += "\t";
    }
    return pad;
  }

  public String getAttribute(String attributeName) {
    String attributeValue = null;
    NamedNodeMap namedNodeMap = _node.getAttributes();
    if (namedNodeMap.getLength() > 0) {
      try {
        attributeValue = namedNodeMap.getNamedItem(attributeName).getNodeValue();
      } catch (NullPointerException e) {
      }
    }
    return attributeValue;
  }

}



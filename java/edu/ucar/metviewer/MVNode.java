package edu.ucar.metviewer;

import java.util.*;
import org.w3c.dom.*;

public class MVNode{
	protected Node _node			= null;
	protected String _tag			= "";
	protected String _name			= "";
	protected String _label			= "";
	protected String _inherits		= "";
	protected String _id			= "";
	protected String _run			= "";
	protected String _value			= "";
	protected MVNode[] _children	= {};
		
	public MVNode(Node node){
		_node = node;
		_tag = node.getNodeName();
		
		//  get the node name attribute value, if present 
		NamedNodeMap mapAttr = node.getAttributes();
		for(int i=0; i < mapAttr.getLength(); i++){
			Node nodeAttr = mapAttr.item(i); 
			String strAttrName = nodeAttr.getNodeName(); 
			if     ( strAttrName.equals("name") )		{ _name		= nodeAttr.getNodeValue(); }
			else if( strAttrName.equals("label") )		{ _label	= nodeAttr.getNodeValue(); }
			else if( strAttrName.equals("inherits") )	{ _inherits	= nodeAttr.getNodeValue(); }
			else if( strAttrName.equals("id") )			{ _id		= nodeAttr.getNodeValue(); }
			else if( strAttrName.equals("run") )		{ _run		= nodeAttr.getNodeValue(); }
			else{
				System.out.println("  **  WARNING: unrecognized attribute name '" + strAttrName + "' in node '" + _tag + "'");
			}
		}

		ArrayList listChildren = new ArrayList();
		NodeList list = node.getChildNodes();
		for(int i=0; i < list.getLength(); i++){
			Node nodeChild = list.item(i);

			if( Node.TEXT_NODE == nodeChild.getNodeType() ){
				if( nodeChild.getNodeValue().matches("\\s*") ){ continue; }
				_value = nodeChild.getNodeValue();
			} else if( Node.ELEMENT_NODE == nodeChild.getNodeType() ){
				listChildren.add( new MVNode(nodeChild) );				
			}			
		}
		_children = (0 < listChildren.size()? (MVNode[])listChildren.toArray(new MVNode[]{}) : new MVNode[]{});
	}
	
	public static String printNode(MVNode mvnode, int lev){
		String strRet = tabPad(lev) + "<" + mvnode._tag;
		if( null != mvnode._name ){ strRet += " name=\"" + mvnode._name + "\""; }
		
		boolean boolCloseTag = true;
		if( null == mvnode._value && null == mvnode._children ){
			strRet += " /";
			boolCloseTag = false;
		}
		strRet += ">";
		
		if( null != mvnode._value )	{ strRet += mvnode._value; }
		else						{ strRet += "\n";		   }
		
		if( null != mvnode._children ){
			for(int i=0; i < mvnode._children.length; i++){ 
				strRet += printNode( mvnode._children[i], lev+1 );
			}
			strRet += tabPad(lev) + "</" + mvnode._tag + ">\n";
		} else if( boolCloseTag ){
			strRet += "</" + mvnode._tag + ">\n";
		}
		return strRet;
	}
	public static String printNode(MVNode mvnode)	{ return printNode(mvnode, 0);	}
	public String printNode()					 	{ return printNode(this, 0);	}
	public String printNode(int lev)				{ return printNode(this, lev);	}
		
	public static String tabPad(int lev){
		String pad = "";
		for(int i=0; i < lev; i++){ pad += "\t"; }
		return pad;
	}

	public static String getNodeTypeString(Node node){
		switch(node.getNodeType()){
		case Node.ATTRIBUTE_NODE:				return "ATTRIBUTE_NODE";
		case Node.CDATA_SECTION_NODE:			return "CDATA_SECTION_NODE";
		case Node.COMMENT_NODE:					return "COMMENT_NODE";
		case Node.DOCUMENT_FRAGMENT_NODE:		return "DOCUMENT_FRAGMENT_NODE";
		case Node.DOCUMENT_NODE:				return "DOCUMENT_NODE";
		case Node.DOCUMENT_TYPE_NODE:			return "DOCUMENT_TYPE_NODE";
		case Node.ELEMENT_NODE:					return "ELEMENT_NODE";
		case Node.ENTITY_NODE:					return "ENTITY_NODE";
		case Node.ENTITY_REFERENCE_NODE:		return "ENTITY_REFERENCE_NODE";
		case Node.NOTATION_NODE:				return "NOTATION_NODE";
		case Node.PROCESSING_INSTRUCTION_NODE:	return "PROCESSING_INSTRUCTION_NODE";
		case Node.TEXT_NODE:					return "TEXT_NODE";
		default:								return "(unknown)";
		}
	}
	
}



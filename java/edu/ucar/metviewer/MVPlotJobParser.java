package edu.ucar.metviewer;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.DOMParser;

public class MVPlotJobParser {
	public static void main(String[] args) {

		try {
			String strXML = 
				"<dep>" +
				"	<!-- " +
				"		this is a multi-line comment " +
				"	-->" +
				"	<dep1>" +
				"		<fcst_var name=\"APCP_24\"><stat>FBIAS</stat></fcst_var>" +
				"	</dep1>" +
				"	<dep2>" +
				"		<fcst_var name=\"APCP_24\"><stat>BASER</stat></fcst_var>" +
				"	</dep2>" +
				"	<fix>" +
				"		<fcst_var name=\"APCP_24\">" +
				"			<var name=\"fcst_lev\">A24</var>" +
				"		</fcst_var>" +
				"	</fix>" +
				"</dep>";
			
			DOMParser parser = new DOMParser();
			parser.parse( new InputSource(new ByteArrayInputStream(strXML.getBytes())) );
			Document doc = parser.getDocument();
			System.out.println( buildDepMap(strXML).getRDecl() );
			
			
			
			/*
			MVOrderedMap map = buildDepMap(strXML);
			System.out.println(map.getRDecl());
			*/
			
			/*
			 * 
			DOMParser parser = new DOMParser();
			parser.parse( new InputSource(new ByteArrayInputStream(strXML.getBytes())) );
			
			//parser.parse("plot.xml");
			Document doc = parser.getDocument();
			
			NodeList nodes = doc.getElementsByTagName("dep");
			for(int i=0; i < nodes.getLength(); i++){
				MVOrderedMap map = buildDepMap(nodes.item(i));
				System.out.println(map.getRDecl());
			}
			 */

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
	public static MVOrderedMap buildDepMap(String strDepXML) throws Exception{
		DOMParser parser = new DOMParser();
		parser.parse( new InputSource(new ByteArrayInputStream(strDepXML.getBytes())) );
		Document doc = parser.getDocument();
		
		//return buildDepMap( doc.getElementsByTagName("dep").item(0) );
		
		MVOrderedMap mapDep = new MVOrderedMap();
		
		//  <dep>
		MVNode nodeDep = new MVNode( doc.getElementsByTagName("dep").item(0) );
		for(int i=0; i < nodeDep._children.length; i++){
			
			//  <dep1> or <dep2>
			MVNode nodeDepN = nodeDep._children[i];
			if( nodeDepN._tag.startsWith("dep") ){
				MVOrderedMap mapDepN = new MVOrderedMap();
				
				//  <fcst_var>
				for(int j=0; j < nodeDepN._children.length; j++){
					MVNode nodeFcstVar = nodeDepN._children[j];					
					ArrayList listStats = new ArrayList();
					
					//  <stat>s
					for(int k=0; k < nodeFcstVar._children.length; k++){
						listStats.add(nodeFcstVar._children[k]._value);
					}
					mapDepN.put(nodeFcstVar._name, listStats.toArray(new String[]{}));
				}
				mapDep.put(nodeDepN._tag, mapDepN);
			}
			
			//  <fix>
			else if( nodeDepN._tag.startsWith("fix") ){
				MVOrderedMap mapFix = new MVOrderedMap();
				
				//  <fcst_var>
				for(int j=0; j < nodeDepN._children.length; j++){
					MVNode nodeFcstVar = nodeDepN._children[j];					
					MVOrderedMap mapFcstVar = new MVOrderedMap();
					
					//  <var>s
					for(int k=0; k < nodeFcstVar._children.length; k++){
						mapFcstVar.put(nodeFcstVar._children[k]._name, nodeFcstVar._children[k]._value);
					}
					mapFix.put(nodeFcstVar._name, mapFcstVar);
				}
				mapDep.put(nodeDepN._tag, mapFix);
			}
		}
		return mapDep;
	}
	
	public static MVOrderedMap buildDepMap(Node nodeDep){
		MVOrderedMap mapDep = new MVOrderedMap();
		
		//  <dep> level
		NodeList nodesDep = nodeDep.getChildNodes();
		for(int i=0; i < nodesDep.getLength(); i++){
			Node nodeDepMember = nodesDep.item(i);
			
			//  <dep1> or <dep2>
			NodeList nodesFcstVar = nodeDepMember.getChildNodes();
			String strDepName = nodeDepMember.getNodeName();
			if( strDepName.startsWith("dep") ){
				MVOrderedMap mapDepN = new MVOrderedMap();
				
				//  <fcst_var>
				for(int j=0; j < nodesFcstVar.getLength(); j++){
					Node nodeFcstVar = nodesFcstVar.item(j);
					if( !nodeFcstVar.getNodeName().equals("fcst_var") ){ continue; }
					Node nodeFcstVarName = nodeFcstVar.getAttributes().getNamedItem("name");
					String strFcstVarName = nodeFcstVarName.getNodeValue();
					
					//  <stat>
					ArrayList listStats = new ArrayList();
					NodeList nodesStats = nodeFcstVar.getChildNodes();
					for(int k=0; k < nodesStats.getLength(); k++){
						Node nodeStat = nodesStats.item(k);
						if( !nodeStat.getNodeName().equals("stat") ){ continue; }
						listStats.add( getSimpleNodeText(nodeStat) );
					}
					String[] stats = (String[])listStats.toArray(new String[]{});
					mapDepN.put(strFcstVarName, stats);
				}
				mapDep.put(strDepName, mapDepN);
			}
			
			//  <fix>
			else if( strDepName.equals("fix") ){
				mapDep.put("fix", "tricky");
			}
		}

		return mapDep;
	}
	
	public static String getSimpleNodeText(Node node){		
		NodeList nodes = node.getChildNodes();
		for(int i=0; i < nodes.getLength(); i++){
			Node nodeVal = nodes.item(i);
			if( nodeVal.getNodeName().equals("#text") ){ return nodeVal.getNodeValue(); }
		}
		return null;
	}
}

class MVNode{
	protected Node _node;
	protected String _tag;
	protected String _name;
	protected String _value;
	protected MVNode[] _children;
		
	public MVNode(Node node){
		_node = node;
		_tag = node.getNodeName();
		
		//  get the node name attribute value, if present 
		NamedNodeMap mapAttr = node.getAttributes();
		if( 0 < mapAttr.getLength() ){ _name = mapAttr.getNamedItem("name").getNodeValue(); }

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
		_children = (0 < listChildren.size()? (MVNode[])listChildren.toArray(new MVNode[]{}) : null);
	}
	
	public static String printNode(MVNode mvnode, int lev){
		String strRet = tabPad(lev) + "<" + mvnode._tag;
		if( null != mvnode._name ){ strRet += " name=\"" + mvnode._name + "\""; }
		strRet += ">";
		
		if( null != mvnode._value )	{ strRet += mvnode._value; }
		else						{ strRet += "\n";		   }
		
		if( null != mvnode._children ){
			for(int i=0; i < mvnode._children.length; i++){ 
				strRet += printNode( mvnode._children[i], lev+1 );
			}
			strRet += tabPad(lev) + "</" + mvnode._tag + ">\n";
		} else {
			strRet += "</" + mvnode._tag + ">\n";
		}
		return strRet;
	}
	public static String printNode(MVNode mvnode){ return printNode(mvnode, 0); }
		
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



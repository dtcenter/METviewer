package edu.ucar.metviewer;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;

public class MVLoadJobParser extends MVUtil{
	
	protected Hashtable _tableDateListDecl = new Hashtable();
	protected Hashtable _tableDateRangeDecl = new Hashtable();
	protected MVNode _nodeLoadSpec = null;
	protected MVLoadJob _job = null;
	
	protected Connection _con = null;

	public MVLoadJobParser(String spec) throws Exception{		

		//  instantiate and configure the xml parser
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setSchema(schema);
	    dbf.setValidating(false);
	    dbf.setNamespaceAware(false);
	    
		DocumentBuilder builder = dbf.newDocumentBuilder();
		builder.setErrorHandler(new ErrorHandler(){
			public void error(SAXParseException exception)		{ printException("error", exception);		}
			public void fatalError(SAXParseException exception)	{ printException("fatalError", exception);	}	
			public void warning(SAXParseException exception)	{ printException("warning", exception);		}
			
			public void printException(String type, SAXParseException e){
				System.out.println("  **  ERROR: " + e.getMessage() + "\n" +
								   "      line: " + e.getLineNumber() + "  column: " + e.getColumnNumber());					
			}
		});
		
		//  parse the input document and build the MVNode data structure
		Document doc = builder.parse(spec);
		_nodeLoadSpec = new MVNode(doc.getFirstChild());
		
		parseLoadJobSpec();
	}
	
	public MVLoadJob getLoadJob(){ return _job; }
	
	public void parseLoadJobSpec(){
		MVLoadJob job = new MVLoadJob();
		for(int i=0; null != _nodeLoadSpec && i < _nodeLoadSpec._children.length; i++){
			MVNode node = _nodeLoadSpec._children[i];

			//  <connection>
			if( node._tag.equals("connection") ){
				for(int j=0; j < node._children.length; j++){
					if     ( node._children[j]._tag.equals("host") )	{ job.setDBHost( node._children[j]._value ); }
					else if( node._children[j]._tag.equals("database") ){ job.setDBName( node._children[j]._value ); }
					else if( node._children[j]._tag.equals("user") )	{ job.setDBUser( node._children[j]._value ); }
					else if( node._children[j]._tag.equals("password") ){ job.setDBPassword( node._children[j]._value ); }
				}
				
				try {
					//  connect to the database
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection con = DriverManager.getConnection("jdbc:mysql://" + job.getDBHost() + "/" + job.getDBName(), job.getDBUser(), job.getDBPassword());
					if( con.isClosed() )	throw new Exception("METViewer load error: database connection failed");
					_con = con;
					job.setConnection(con);
				} catch(Exception ex){
					System.out.println("  **  ERROR: parseLoadJobSpec() caught " + ex.getClass() + " connecting to database: " + ex.getMessage());
				}
			}
			
			//  <date_list>
			else if( node._tag.equals("date_list") ){
				String strName = node._name;
				String strStart = "";
				String strEnd = "";
				int intInc = 0;
				String strFormat = "";
				
				for(int j=0; j < node._children.length; j++){
					MVNode nodeChild = node._children[j];
					if     ( nodeChild._tag.equals("inc") )    { intInc = Integer.parseInt(nodeChild._value); }
					else if( nodeChild._tag.equals("format") ) { strFormat = nodeChild._value;                }
				}
				for(int j=0; j < node._children.length; j++){
					MVNode nodeChild = node._children[j];
					if     ( nodeChild._tag.equals("start") ) { strStart = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }
					else if( nodeChild._tag.equals("end") )   { strEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }
				}
				
				String[] listDates = buildDateList(strStart, strEnd, intInc, strFormat);
				_tableDateListDecl.put(strName, listDates);
			}
			
			//  simple string fields
			else if( node._tag.equals("verbose") )                 { job.setVerbose( node._value.equalsIgnoreCase("true") );              }
			else if( node._tag.equals("insert_size") )             { job.setInsertSize( Integer.parseInt(node._value) );                  }
			else if( node._tag.equals("stat_header_table_check") ) { job.setStatHeaderTableCheck( node._value.equalsIgnoreCase("true") ); }
			else if( node._tag.equals("stat_header_db_check") )    { job.setStatHeaderDBCheck( node._value.equalsIgnoreCase("true") );    }
			else if( node._tag.equals("mode_header_db_check") )    { job.setModeHeaderDBCheck( node._value.equalsIgnoreCase("true") );    }
			else if( node._tag.equals("drop_indexes") )            { job.setDropIndexes( node._value.equalsIgnoreCase("true") );          }
			else if( node._tag.equals("apply_indexes") )           { job.setApplyIndexes( node._value.equalsIgnoreCase("true") );         }
			else if( node._tag.equals("folder_tmpl") )             { job.setFolderTmpl( node._value );                                    }

			//  <load_val>
			else if( node._tag.equals("load_val") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeField = node._children[j];
					String strFieldName = nodeField._name;
					ArrayList listVal = new ArrayList();
					for(int k=0; k < nodeField._children.length; k++){
						MVNode nodeChild = nodeField._children[k];
						
						//  <val>
						if( nodeChild._tag.equals("val") ){ listVal.add( nodeChild._value ); }
						
						//  <date_list>
						else if( nodeChild._tag.equals("date_list") ){
							listVal.addAll( Arrays.asList( (String[])_tableDateListDecl.get(nodeChild._name) ) );							
						}
					}
					job.addLoadVal(strFieldName, toArray(listVal));
				}
			}
			
		}
		
		_job = job;
	}
	
	public static boolean checkJobCompleteness(MVLoadJob job){
		if     ( null == job._con )                 { return false; }
		else if( job.getFolderTmpl().equals("")  )	{ return false; }
		else if( 1 > job.getLoadVal().size() )      { return false; }
		
		return true;
	}
	
}

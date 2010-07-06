package edu.ucar.metviewer;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.log4j.*;

public class MVServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger _logger = Logger.getLogger("edu.ucar.metviewer.MVServlet");
	
	public static String _strDBHost = "";
	public static String _strDBUser = "";
	public static String _strDBPassword = "";
	public static String[] _listDB = {};
	
	public void init() throws ServletException {
		_logger.debug("init() - loading properties...");
		try{
			ResourceBundle bundle = ResourceBundle.getBundle("mvservlet");
			_strDBHost     = bundle.getString("db.host");
			_strDBUser     = bundle.getString("db.user");
			_strDBPassword = bundle.getString("db.password");
			_listDB        = bundle.getString("db.list").split("\\s*,\\s*");
		}catch(Exception e){
			_logger.error("init() - ERROR: caught " + e.getClass() + " loading properties: " + e.getMessage());
		}				
		_logger.debug("init() - done loading properties");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();

        //response.setContentType("text/html");
        response.setContentType("text/plain");
        
        //  print out the HTTP GET params
        Map params = request.getParameterMap();
        ArrayList listParamsArr = new ArrayList();
        listParamsArr.addAll( params.entrySet() );
        Map.Entry[] listParams = (Map.Entry[])listParamsArr.toArray(new Map.Entry[]{});
        out.println("params:");
        for(int i=0; i < listParams.length; i++){
        	Object objKey = listParams[i].getKey();
        	Object objVal = listParams[i].getValue();
        	String strVal = "(unknown)";
        	if( objVal instanceof String[] ){ strVal = ((String[])objVal)[0]; }
        	out.println("  " + objKey.toString() + ": " + strVal);
        }
        
        out.println("howdy from MVServlet");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        //  read the body of the request
    	BufferedReader reader = new BufferedReader( new InputStreamReader(request.getInputStream()) );
    	String strLine = "";
    	String strRequestBody = "";
    	while( (strLine = reader.readLine()) != null ){ strRequestBody = strRequestBody + strLine; }
    	_logger.debug("doPost() - request: " + strRequestBody);
       
    	//  initialize the response writer 
    	PrintWriter out = response.getWriter();
        response.setContentType("text/plain");
        
        HttpSession session = request.getSession();

        //  attempt to parse the body of the request
        try{
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
					_logger.error("doPost() - xml parser error: " + e.getMessage() + "\n      line: " + e.getLineNumber() + "  column: " + e.getColumnNumber());					
				}
			});
			
			//  parse the input document and build the MVNode data structure
			//Document doc = builder.parse(request.getInputStream());
			Document doc = builder.parse( new ByteArrayInputStream(strRequestBody.getBytes()) );
			MVNode nodeReq = new MVNode(doc.getFirstChild());
			String strResp = "";
			
			for(int i=0; i < nodeReq._children.length; i++){
				MVNode nodeCall = nodeReq._children[i];

				//  <list_db> request
				if( nodeCall._tag.equalsIgnoreCase("list_db") ){
					strResp = "<list_db>";
					for(int j=0; j < _listDB.length; j++){ strResp += "<db>" + _listDB[j] + "</db>"; }
					strResp += "</list_db>";
			    	_logger.debug("doPost() - list_db response: " + strRequestBody);
				}
				
				//  <connect_db> request
				else if( nodeCall._tag.equalsIgnoreCase("connect_db") ){
					strResp = "<connect_db>";
					String strDB = nodeCall._value;
			    	_logger.debug("doPost() - connect_db: connecting to " + strDB);
					
					//  if an open connection exists in the session, close it
					Connection con = null;
					if( null != session.getAttribute("con") ){ con = (Connection)session.getAttribute("con"); }
					if( null != con && !con.isClosed() ){
				    	_logger.debug("doPost() - connect_db: disconnecting existing connection");
				    	strResp += "<disconnect>" + con.getMetaData().getURL() + "</disconnect>";
				    	con.close();
					}
					
					//  attempt to open a new connection and store it in the session
					try {
						Class.forName("com.mysql.jdbc.Driver").newInstance();
						con = DriverManager.getConnection("jdbc:mysql://" + _strDBHost + "/" + strDB, _strDBUser, _strDBPassword);
						if( con.isClosed() )	throw new Exception("METViewer error: database connection failed");
						session.setAttribute("con", con);
						strResp += "<connected>" + con.getMetaData().getURL() + "</connected>";
				    	_logger.debug("doPost() - connect_db: connected to " + strDB);
					} catch(Exception ex){
						strResp += "<error>caught " + ex.getClass() + ": " + ex.getMessage() + "</error>";
				    	_logger.error("doPost() - connect_db: caught " + ex.getClass() + " connecting to database: " + ex.getMessage());
					}
					
					strResp += "</connect_db>";
				}				
				
				//  not handled
				else {
					strResp = "<error>unexpected request type: " + nodeCall._tag + "</error>";
				}
			}
			
			if( strResp.equals("") ){
				strResp = "<error>could not parse request</error>";
			}
			
			out.println(strResp);
        }catch(Exception e){
        	_logger.error("doPost() - caught " + e.getClass() + ": " + e.getMessage());
        }
    }
}

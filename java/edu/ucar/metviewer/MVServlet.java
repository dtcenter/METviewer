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
	
	public static Hashtable _tableDBConnection = new Hashtable();
	
	public static boolean _boolListValCache = true;
	public static Hashtable _tableListValCache = new Hashtable();
	public static boolean _boolListStatCache = true;
	public static Hashtable _tableListStatCache = new Hashtable();
	
	public static final String _strListStatModeResp = "<stat>MMI</stat><stat>MMIF</stat><stat>MMIO</stat><stat>MIA</stat><stat>MAR</stat>" + 
													  "<stat>MCD</stat><stat>MAD</stat><stat>P50</stat><stat>P90</stat>";
	
	/**
	 * Read the resource bundle containing database configuration information and initialize the global
	 * variables
	 */
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

	/**
	 * Override the parent's doGet() method with a debugging implementation that echoes inputs
	 * @param request Contains request information, including parameters
	 * @param response Used to send information back to the requester 
	 */
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

	/**
	 * Override the parent's doPost() method with an implementation that reads XML from the body of the request
	 * and parses it into one of several commands.  The XML request command is carried out and an XML response
	 * is constructed and sent back to the requester.
	 * @param request Contains request information, including parameters
	 * @param response Used to send information back to the requester 
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        //  read the body of the request
    	BufferedReader reader = new BufferedReader( new InputStreamReader(request.getInputStream()) );
    	String strLine = "";
    	String strRequestBody = "";
    	while( (strLine = reader.readLine()) != null ){ strRequestBody = strRequestBody + strLine; }
    	_logger.debug("doPost() - request: " + strRequestBody);
       
    	//  initialize the response writer and session
    	PrintWriter out = response.getWriter();
        response.setContentType("text/plain");        
        
        //  attempt to parse the body of the request
        try{
			//  instantiate and configure the xml parser
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
			Document doc = builder.parse( new ByteArrayInputStream(strRequestBody.getBytes()) );
			MVNode nodeReq = new MVNode(doc.getFirstChild());
			String strResp = "";
			Connection con = null;
			
			//  examine the children of the request node
			for(int i=0; i < nodeReq._children.length; i++){
				MVNode nodeCall = nodeReq._children[i];

				//  <list_db> request
				if( nodeCall._tag.equalsIgnoreCase("list_db") ){
					strResp = "<list_db>";
					for(int j=0; j < _listDB.length; j++){ strResp += "<val>" + _listDB[j] + "</val>"; }
					strResp += "</list_db>";
				}

				//  <db_con> node containing the database connection name
				else if( nodeCall._tag.equalsIgnoreCase("db_con") )    {
					
					//  check the connection pool
					String strDBCon = nodeCall._value; 
					if( _tableDBConnection.containsKey(strDBCon) ){						
						con = (Connection)_tableDBConnection.get(strDBCon);
						if( !con.isClosed() ){
							_logger.debug("doPost() - db_con: using cached connection " + strDBCon);
							continue;
						} else {
							_logger.debug("doPost() - db_con: cached connection " + strDBCon + " is closed");
						}
					}
					
					//  attempt to open a new connection and store it in the session
					try {
						Class.forName("com.mysql.jdbc.Driver").newInstance();
						con = DriverManager.getConnection("jdbc:mysql://" + _strDBHost + "/" + strDBCon, _strDBUser, _strDBPassword);
						if( con.isClosed() )	throw new Exception("METViewer error: database connection failed");
						_tableDBConnection.put(strDBCon, con);
				    	_logger.debug("doPost() - db_con: created new connection " + strDBCon);
					} catch(Exception ex){
				    	_logger.error("doPost() - db_con: " + ex.getClass() + " connecting to database: " + ex.getMessage());
				    	throw ex;
					}

				}
				
				//  <list_val>
				else if( nodeCall._tag.equalsIgnoreCase("list_val") )  { strResp += handleListVal  (nodeCall, strRequestBody, con); }
				
				//  <list_stat>
				else if( nodeCall._tag.equalsIgnoreCase("list_stat") ) { strResp += handleListStat (nodeCall, strRequestBody, con); }

				//  <list_val_clear_cache>
				else if( nodeCall._tag.equalsIgnoreCase("list_val_clear_cache") ){
					_tableListValCache.clear();
					strResp = "<list_val_clear_cache>success</list_val_clear_cache>";
				}
				
				//  <list_stat_clear_cache>
				else if( nodeCall._tag.equalsIgnoreCase("list_stat_clear_cache") ){
					_tableListStatCache.clear();
					strResp = "<list_stat_clear_cache>success</list_stat_clear_cache>";
				}
				
				//  not handled
				else {
					strResp = "<error>unexpected request type: " + nodeCall._tag + "</error>";
				}
			}
			
			if( strResp.equals("") ){
				strResp = "<error>could not parse request</error>";
			}
			
			_logger.debug("doPost() - response: " + strResp);
			out.println(strResp);
        }catch(Exception e){
        	ByteArrayOutputStream s = new ByteArrayOutputStream();
        	e.printStackTrace( new PrintStream(s) );
        	_logger.error("doPost() - caught " + e.getClass() + ": " + e.getMessage() + "\n" + s.toString());
        	out.println("<error>caught " + e.getClass() + ": " + e.getMessage() + "</error>");
        }
    }
    
    /**
     * List database values for the specified field with the specified constraints, using cached data if appropriate, and
     * return it in serialized XML
     * @param nodeCall MVNode containing request information
     * @param requestBody the XML sent by the client which is used to cache the response
     * @param con Database connection to search against
     * @return XML response information
     * @throws Exception
     */
    public static String handleListVal(MVNode nodeCall, String requestBody, Connection con) throws Exception{

		//  parse the input request, and initialize the response
		String strResp = "<list_val>";
		String strId = nodeCall._children[0]._value;
		String strHeaderField = nodeCall._children[1]._value;
		boolean boolMode = nodeCall._children[1]._tag.equals("mode_field");
		String strHeaderTable = boolMode? "mode_header" : "stat_header";
    	_logger.debug("handleListVal() - listing values for field " + strHeaderField + " and id " + strId);
    	strResp += "<id>" + strId + "</id>";
    	
    	//  check the list val cache for the request data
    	//String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody;
    	String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody.replaceAll("<id>\\d+</id>", "");
    	if( _boolListValCache && _tableListValCache.containsKey(strCacheKey) ){
    		String strListVal = _tableListValCache.get(strCacheKey).toString().replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>"); 
        	_logger.debug("handleListVal() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListVal);
    		return strListVal;
    	}
    	
    	//  parse the list of constraints into a SQL where clause
    	String strWhere = "";
    	for(int i=2; i < nodeCall._children.length; i++){
    		MVNode nodeField = nodeCall._children[i];
    		String strFieldDBCrit = MVUtil.formatField(nodeField._name.toLowerCase(), boolMode).replaceAll("h\\.", "");
    		strWhere += (2 < i? "AND " : "WHERE ") + strFieldDBCrit + " IN (";
    		for(int j=0; j < nodeField._children.length; j++){
    			strWhere += (0 < j? ", " : "") + "'" + nodeField._children[j]._value + "'";
    		}
    		strWhere += ") ";
    	}
		
		//  build a query for the values and execute it
		String strFieldDB = MVUtil.formatField(strHeaderField.toLowerCase(), boolMode).replaceAll("h\\.", "");
		Statement stmt = con.createStatement();
		String strSQL = "SELECT DISTINCT " + strFieldDB + " FROM " + strHeaderTable + " " + strWhere + "ORDER BY " + strFieldDB;
		_logger.debug("handleListVal() - sql: " + strSQL);
		long intStart = (new java.util.Date()).getTime();
		stmt.executeQuery(strSQL);
		
		//  add the list of field values from the query to the response
		int intNumVal = 0;
		ResultSet res = stmt.getResultSet();
		while( res.next() ){
			strResp += "<val>" + res.getString(1) + "</val>";
			intNumVal++;
		}
		_logger.debug("handleListVal() - returned " + intNumVal + " values in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStart));
		
		//  clean up  
		stmt.close();
		strResp += "</list_val>";
		_tableListValCache.put(strCacheKey, strResp);
		return strResp;
    }
    
    /**
     * List statistics for the specified fcst_var, using cached data if appropriate, and return it in serialized XML
     * @param nodeCall MVNode containing request information
     * @param requestBody the XML sent by the client which is used to cache the response
     * @param con Database connection to search against
     * @return XML response information
     * @throws Exception
     */
    public static String handleListStat(MVNode nodeCall, String requestBody, Connection con) throws Exception{

		//  if the request is for the mode stats, return the static list
		String strId = nodeCall._children[0]._value;
		String strFcstVar = nodeCall._children[1]._value;
		if( nodeCall._children[0]._tag.equals("mode_fcst_var") ){
			String strResp = "<list_stat><id>" + strId + "</id>" + _strListStatModeResp + "</list_stat>";
			_logger.debug("handleListStat() - returning mode stats: " + strResp);
			return strResp;
		}
		
    	//  check the list val cache for the request data
    	//String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody;
    	String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody.replaceAll("<id>\\d+</id>", "");
    	if( _boolListStatCache && _tableListStatCache.containsKey(strCacheKey) ){
    		String strListStat = _tableListStatCache.get(strCacheKey).toString().replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>"); 
        	_logger.debug("handleListStat() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListStat);
    		return strListStat;
    	}
    	
		//  build a query for the statistics and execute it
		String strSQL = "SELECT DISTINCT sgl.stat_group_name FROM stat_header sh, stat_group sg, stat_group_lu sgl " +
						"WHERE sh.fcst_var = '" + strFcstVar + "' AND sg.stat_header_id = sh.stat_header_id AND " +
							"sg.stat_group_lu_id = sgl.stat_group_lu_id ORDER BY sgl.stat_group_name";
    	_logger.debug("handleListStat() - listing stats for fcst_var " + strFcstVar + "\n  sql: " + strSQL);
		Statement stmt = con.createStatement();
		long intStart = (new java.util.Date()).getTime();
		stmt.executeQuery(strSQL);

		//  add the list of field values from the query to the response
		String strResp = "<list_stat><id>" + strId + "</id>";
		int intNumStat = 0;
		ResultSet res = stmt.getResultSet();
		while( res.next() ){
			strResp += "<val>" + res.getString(1) + "</val>";
			intNumStat++;
		}
		_logger.debug("handleListStat() - returned " + intNumStat + " stats in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStart));
		
		//  clean up  
		stmt.close();
		strResp += "</list_stat>";
		_tableListStatCache.put(strCacheKey, strResp);
		return strResp;
    }
}

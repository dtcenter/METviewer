package edu.ucar.metviewer;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.log4j.*;
import org.apache.xml.serialize.*;

public class MVServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger _logger = Logger.getLogger("edu.ucar.metviewer.MVServlet");
	
	public static String _strDBHost = "";
	public static String _strDBUser = "";
	public static String _strDBPassword = "";
	public static String[] _listDB = {};
	
	public static String _strPlotXML = "";
	public static String _strRTmpl = "";
	public static String _strRWork = "";
	public static String _strPlots = "";
	
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
			_strDBHost		= bundle.getString("db.host");
			_strDBUser		= bundle.getString("db.user");
			_strDBPassword	= bundle.getString("db.password");
			_listDB			= bundle.getString("db.list").split("\\s*,\\s*");
			
			_strPlotXML		= bundle.getString("folders.plot_xml");
			_strRTmpl		= bundle.getString("folders.r_tmpl");
			_strRWork		= bundle.getString("folders.r_work");
			_strPlots		= bundle.getString("folders.plots");
			
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
    	_logger.debug("doPost() - request (" + request.getRemoteHost() + "): " + strRequestBody);
       
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
				
				//  <plot>
				else if( nodeCall._tag.equalsIgnoreCase("plot") ) { strResp += handlePlot(strRequestBody, con); }

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
    
    public static final SimpleDateFormat _formatPlot = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static String handlePlot(String strRequest, Connection con) throws Exception{
    	Statement stmt;    	
    	String strWebPlotId = "0";
    	String strPlotPrefix = "plot_";
    	
    	//  extract the plot xml from the request
    	String strPlotXML = strRequest;
    	strPlotXML = strPlotXML.substring(strPlotXML.indexOf("</db_con>") + 9);
    	strPlotXML = strPlotXML.substring(0, strPlotXML.indexOf("</request>"));
    	
    	//  query the database to get the next web_plot_id
    	try{
	    	stmt = con.createStatement();
	    	if( !stmt.execute("SELECT MAX(web_plot_id) FROM web_plot;") ){ throw new Exception("Statment.execute() returned false"); }
	    	ResultSet res = stmt.getResultSet();
	    	while( res.next() ){
	    		int intWebPlotId = res.getInt(1);
	    		if( !res.wasNull() ){ strWebPlotId = "" + (intWebPlotId + 1); } 
	    	}
	    	stmt.close();
    	} catch(Exception e){
    		_logger.error("handlePlot() - ERROR: caught " + e.getClass() + " acquiring web_plot_id: " + e.getMessage());
    		return "<error>failed to acquire web_plot_id</error>";
    	}
    	
    	//  construct the names of the plot files
    	java.util.Date datePlot = new java.util.Date();
    	String strPlotPrefixId = strWebPlotId; 
    	while( 5 > strPlotPrefixId.length() ){ strPlotPrefixId = "0" + strPlotPrefixId; }
    	strPlotPrefix += strPlotPrefixId + "_" + _formatPlot.format(datePlot);

    	//  add plot file information to the plot spec
    	strPlotXML =
    		"<plot_spec>" +
    		 	"<folders>" +
    		 		"<r_tmpl>" + _strRTmpl + "</r_tmpl>" +
    		 		"<r_work>" + _strRWork + "</r_work>" +
    		 		"<plots>" + _strPlots + "</plots>" +
    		 	"</folders>" +
    		 	strPlotXML +
    		"</plot_spec>";
    	
    	strPlotXML = strPlotXML.replace("<tmpl>",
    		"<tmpl>" +
    			"<data_file>" + strPlotPrefix + ".data" + "</data_file>" +
    			"<plot_file>" + strPlotPrefix + ".png" + "</plot_file>" +
    			"<r_file>" + strPlotPrefix + ".R" + "</r_file>");    			

    	//  parse the input plot job
    	MVPlotJobParser parser;
    	MVPlotJob job;
    	try{
	    	parser = new MVPlotJobParser( new ByteArrayInputStream( strPlotXML.getBytes() ), con );
	    	MVPlotJob[] jobs = parser.getJobsList();
	    	if( 1 != jobs.length ){ throw new Exception("unexpected number of plot jobs generated: " + jobs.length); }
	    	job = jobs[0];
    	} catch(Exception e){
    		_logger.error("handlePlot() - ERROR: caught " + e.getClass() + " parsing plot job: " + e.getMessage());
    		return "<error>failed to parse plot job</error>";
    	}
   	
        //  write the formatted plot XML to a file
		Document doc = parser.getDocument();
		try{	
			FileOutputStream stream = new FileOutputStream(_strPlotXML + "/" + strPlotPrefix + ".xml");
			XMLSerializer ser = new XMLSerializer(new OutputFormat(Method.XML, "UTF-8", true));
			ser.setOutputByteStream(stream);
			ser.serialize(doc);
			stream.flush();
			stream.close();
		} catch(Exception e) {
			_logger.error("handlePlot() - ERROR: caught " + e.getClass() + " serializing plot xml: " + e.getMessage());
			return "<error>failed to serialize plot xml</error>";
		}    	
    	
    	//  run the plot job and write the batch output to the log file
    	ByteArrayOutputStream log = new ByteArrayOutputStream();
    	MVBatch bat = new MVBatch( new PrintStream(log) );
    	try{
			bat._strRtmplFolder = parser.getRtmplFolder();
			bat._strRworkFolder = parser.getRworkFolder();
			bat._strPlotsFolder = parser.getPlotsFolder();    		
    		//bat._boolVerbose = true;
    		bat.runJob( job );
    	} catch(Exception e){
        	_logger.debug("handlePlot() - ERROR: caught " + e.getClass() + " running plot: " + e.getMessage() + "\nbatch output:\n" + log.toString());
        	return "<error>failed to run plot</error>";        	
    	}
    	_logger.debug("handlePlot() - batch output:\n" + log.toString());
    	
    	//  store the web_plot information in the database
    	try{
	    	stmt = con.createStatement();
	    	int intRes = stmt.executeUpdate("INSERT INTO web_plot VALUES (" + strWebPlotId + ", '" + MVUtil._formatDB.format(datePlot) + "', '" + strPlotXML + "');");
	    	if( 1 != intRes ){ throw new Exception("unexpected result from web_plot INSERT statement: " + intRes); }
    	} catch(Exception e){
        	_logger.debug("handlePlot() - ERROR: caught " + e.getClass() + " updating web_plot: " + e.getMessage());
        	return "<error>failed to update web_plot</error>";
    	}
    	
    	return "<plot>" + strPlotPrefix + "</plot>";
    }

}

package edu.ucar.metviewer;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
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
	
	public static Hashtable _tableStatGroupName = new Hashtable();
	
	public static boolean _boolListValCache = false;
	public static Hashtable _tableListValCache = new Hashtable();
	public static boolean _boolListStatCache = false;
	public static Hashtable _tableListStatCache = new Hashtable();
	
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

	public static final Pattern _patDBLoad = Pattern.compile(".*/db/([\\w\\d]+)$");
	
	/**
	 * Override the parent's doGet() method with a debugging implementation that echoes inputs
	 * @param request Contains request information, including parameters
	 * @param response Used to send information back to the requester 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {

		//  if the request specifies a database to load, redirect with the appropriate parameter
		String strPath = request.getRequestURL().toString();
		Matcher matDBLoad = _patDBLoad.matcher(strPath);
		if( matDBLoad.matches() ){
			String strDB = matDBLoad.group(1);
			
			//  ensure that the requested database exists
			String[] listDBSort = (String[])_listDB.clone();
			Arrays.sort(listDBSort);
			if( 0 > Arrays.binarySearch(listDBSort, "mv_" + strDB) ){
				printErrorPage(response);
				return;
			}

			//  redirect the user to the web app
			response.sendRedirect("/metviewer/metviewer.jsp?db=" + matDBLoad.group(1));			
			return;
		}
				
		//  if there is no specified database, print out the list of parameters for debugging  
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
				
				//  <date> tag, which is used to prevent caching
				else if( nodeCall._tag.equalsIgnoreCase("date") )    {
					continue;
				}

				//  <db_con> node containing the database connection name
				else if( nodeCall._tag.equalsIgnoreCase("db_con") )    {
					
					//  check the connection pool
					String strDBCon = nodeCall._value; 
					if( _tableDBConnection.containsKey(strDBCon) ){						
						con = (Connection)_tableDBConnection.get(strDBCon);

						//  if the connection is present, test it
						if( !con.isClosed() ){
							try{
								Statement stmt = con.createStatement();
								stmt.executeQuery("SELECT COUNT(*) FROM stat_group_lu;");
								stmt.close();
								_logger.debug("doPost() - db_con: using cached connection " + strDBCon);
								continue;
							}catch(Exception e){
								_logger.debug("doPost() - db_con: cached connection " + strDBCon + " is inoperable");
							}
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
				else if( nodeCall._tag.equalsIgnoreCase("list_val_clear_cache") ){ strResp += handleClearListValCache(con); }
				
				//  <list_stat_clear_cache>
				else if( nodeCall._tag.equalsIgnoreCase("list_stat_clear_cache") ){ strResp += handleClearListStatCache(con); }
				
				//  <db_clear_cache>
				else if( nodeCall._tag.equalsIgnoreCase("db_clear_cache") ){ strResp += handleClearDBCache(); }
				
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
     * Clear cached <list_val> values for the input database 
     * @param con
     * @return reponse XML indicating progress
     * @throws Exception
     */
    public static String handleClearListValCache(Connection con) throws Exception{
    	if( !_boolListValCache ){ return "<error>caching list_val caching not activated</error>"; }    	
    	String strKeyPrefix = "<db>" + con.getMetaData().getURL() +"</db>";
    	int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListValCache);
    	return "<list_val_clear_cache>success: removed " + intNumRemoved + " entries</list_val_clear_cache>";
    }
    
    /**
     * Clear cached <list_stat> values for the input database 
     * @param con
     * @return reponse XML indicating progress
     * @throws Exception
     */
    public static String handleClearListStatCache(Connection con) throws Exception{
    	if( !_boolListStatCache ){ return "<error>caching list_stat caching not activated</error>"; }
    	String strKeyPrefix = "<db>" + con.getMetaData().getURL() +"</db>";
    	int intNumRemoved = removeTableEntriesByKeyPrefix(strKeyPrefix, _tableListStatCache);
    	return "<list_stat_clear_cache>success: removed " + intNumRemoved + " entries</list_stat_clear_cache>";
    }
    
    /**
     * Clear the database connection table of all connections, closing them in the process
     * @return response XML indicating progress
     * @throws Exception
     */
    public static synchronized String handleClearDBCache() throws Exception{
    	
    	//  remove each database connection from the connection table, commit its transactions and close it
    	Map.Entry[] listDB = (Map.Entry[])_tableDBConnection.entrySet().toArray(new Map.Entry[]{});    	
    	for(int i=0; i < listDB.length; i++){
    		String strDB = listDB[i].getKey().toString();
    		Connection con = (Connection)_tableDBConnection.remove(strDB);
    		try{
    			con.commit();
    			con.close();
    		} catch(Exception e){}
    	}
    	
    	return "<db_clear_cache>success: removed " + listDB.length + " database connections</db_clear_cache>";
    }
    
    /**
     * Searches all key values of the input table, which are assumed to be Strings, and removes any entry
     * whose key matches the specified prefix
     * @param prefix String key prefix to match
     * @param table Table from which matching entries will be removed
     * @return number of removed entries
     */
    public static int removeTableEntriesByKeyPrefix(String prefix, Hashtable table){
    	int intNumRemoved = 0;
    	Map.Entry[] listEntries = (Map.Entry[])table.entrySet().toArray(new Map.Entry[]{});
    	for(int i=0; i < listEntries.length; i++){
    		String strKey = listEntries[i].getKey().toString(); 
    		if( !strKey.startsWith( prefix ) ){ continue; } 
    		table.remove(strKey);
			intNumRemoved++;
    	}
    	return intNumRemoved;
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
		boolean boolRhist = nodeCall._children[1]._tag.equals("rhist_field");
		String strHeaderTable = boolMode? "mode_header" : "stat_header";
    	_logger.debug("handleListVal() - listing values for field " + strHeaderField + " and id " + strId);
    	strResp += "<id>" + strId + "</id>";
    	
    	//  check the list val cache for the request data
    	String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody.replaceAll("<id>\\d+</id>", "");
    	if( _boolListValCache && _tableListValCache.containsKey(strCacheKey) ){
    		String strListVal = _tableListValCache.get(strCacheKey).toString().replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>"); 
        	_logger.debug("handleListVal() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListVal);
    		return strListVal;
    	}
    	
    	//  determine if the requested field is n_rank and format accordingly
    	String strField = strHeaderField.toLowerCase();
    	boolean boolNRank = strField.equalsIgnoreCase("N_RANK");
    	
    	//  parse the fcst_var/stat constraint to build a list of line_data tables and fcst_var values
    	Hashtable tableFcstVarStat = new Hashtable();    	
    	Hashtable tableLineDataTables = new Hashtable();
    	boolean boolFcstVar = false;
    	if( !boolRhist && 2 < nodeCall._children.length ){
    		boolFcstVar = true;
			MVNode nodeFcstVarStat = nodeCall._children[2];
			for(int i=0; i < nodeFcstVarStat._children.length; i++){
				MVNode nodeFcstVar = nodeFcstVarStat._children[i];
				tableFcstVarStat.put(nodeFcstVar._name, "true");
				for(int j=0; j < nodeFcstVar._children.length; j++){
					String strStat = nodeFcstVar._children[j]._value;
					String strLineDataTable = MVUtil.getStatTable(strStat);
					tableLineDataTables.put(strLineDataTable, "true");
					if     ( strLineDataTable.equals("line_data_cnt") ){ tableLineDataTables.put("line_data_sl1l2", "true"); }
					else if( strLineDataTable.equals("line_data_cts") ){ tableLineDataTables.put("line_data_ctc", "true");   }
				}
			}
    	} else if( boolRhist ){
    		tableLineDataTables.put("line_data_rhist", "true");
    	}
		
		//  build a list of the line_data tables for all the stats
		String[] listTables = (String[])tableLineDataTables.keySet().toArray(new String[]{});
		
		//  build the where clause for the fcst_var values, if present
		String strWhere = "";
		if( boolFcstVar ){
	    	String strFcstVarList = "";
			String[] listFcstVar = (String[])tableFcstVarStat.keySet().toArray(new String[]{});
			boolean boolRegEx = false;
			for(int i=0; i < listFcstVar.length; i++){
				if( listFcstVar[i].contains("*") ){ boolRegEx = true; }
				strFcstVarList += (0 < i? ", " : "") + "'" + listFcstVar[i].replace("*", "%") + "'";
			}
			//strWhere += "WHERE h.fcst_var " + (boolRegEx? "LIKE" : "IN") + " (" + strFcstVarList + ")";
			strWhere += "WHERE h.fcst_var LIKE (" + strFcstVarList + ")";
		}
		String strWhereFcstVar = strWhere;
		
    	//  parse the list of constraints into a SQL where clause
		boolean boolTimeCrit = (strField.contains("init") || strField.contains("valid") || strField.contains("lead"));
		String strWhereTime = "";
		long intStart = 0;
    	for(int i=2; i < nodeCall._children.length; i++){
    		
    		if( nodeCall._children[i]._tag.equals("stat") ){ continue; }
    		
    		//  determine if the field should be used as criteria
    		MVNode nodeField = nodeCall._children[i];
    		String strFieldCrit = nodeField._name.toLowerCase();
    		boolean boolTimeCritField = false;
    		boolean boolTimeCritCur = false;
    		if( strFieldCrit.contains("valid") || strFieldCrit.contains("init") || strFieldCrit.contains("lead") ){
    			boolTimeCritField = strField.equals(strFieldCrit) || 
    								(strField.contains("fcst_init") && strFieldCrit.equals("init_hour")) ||
    								(strField.contains("fcst_valid") && strFieldCrit.equals("valid_hour"));
    			boolTimeCritCur = true;
    			boolTimeCrit = true;
    		}

    		//  if so, build a where clause for the criteria
    		String strFieldDBCrit = MVUtil.formatField(strFieldCrit, boolMode, false);
    		if( -1 != strFieldDBCrit.indexOf("n_rank") ){ continue; }
    		String strSQLOp = "IN";
    		String strValList = "";
    		for(int j=0; j < nodeField._children.length; j++){
    			String strVal = nodeField._children[j]._value;
    			if( strVal.contains("*") ){ strSQLOp = "LIKE"; }
    			strValList += (0 < j? ", " : "") + "'" + strVal.replace("*", "%") + "'";
    		}
    		
    		//  add the where clause to the criteria, if appropriate
    		if( boolTimeCritField ){
    			strWhereTime += (strWhereTime.equals("")? " WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
    		} else if( !boolTimeCritCur ){
        		strWhere += (strWhere.equals("")? "WHERE " : " AND ") + strFieldDBCrit + " " + strSQLOp + " (" + strValList + ")";
    		}
    	}
		
		//  build a query for the values
    	String strSQL = "";
    	String strTmpTable = "";
    	if( boolNRank ){
    		strSQL = "SELECT DISTINCT ld.n_rank " +
    				 "FROM stat_header h, line_data_rhist ld " + 
    				 strWhere + (strWhere.equals("")? "WHERE" : " AND") + " ld.stat_header_id = h.stat_header_id " + 
    				 "ORDER BY n_rank;";

    	} else if( !boolMode && (strField.equals("fcst_lead") || strField.contains("valid") || strField.contains("init")) ){

    		String strSelectField = MVUtil.formatField(strField, boolMode);

    		//  create a temp table for the list values from the different line_data tables
    		strTmpTable = "tmp_" + (new java.util.Date()).getTime();
    		Statement stmtTmp = con.createStatement();
    		String strTmpSQL = "CREATE TEMPORARY TABLE " + strTmpTable + " (" + strField + " TEXT);";
    		_logger.debug("handleListVal() - sql: " + strTmpSQL);
    		long intStartTmp = (new java.util.Date()).getTime();
    		stmtTmp.executeUpdate(strTmpSQL);
    		_logger.debug("handleListVal() - temp table " + strTmpTable + " query returned in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStartTmp));		

    		//  add all distinct list field values to the temp table from each line_data table
    		for(int i=0; i < listTables.length; i++){
    			strTmpSQL = "INSERT INTO " + strTmpTable + " SELECT DISTINCT " + strSelectField + " FROM " + listTables[i] + " ld" + strWhereTime;    				
        		_logger.debug("handleListVal() - sql: " + strTmpSQL);
        		if( 0 == i ){ intStart = (new java.util.Date()).getTime(); }
    			stmtTmp.executeUpdate(strTmpSQL);
    		}
    		stmtTmp.close();
    		
    		//  build a query to list all distinct, ordered values of the list field from the temp table
    		strSQL = "SELECT DISTINCT " + strField + " FROM " + strTmpTable + " ORDER BY " + strField + ";";
    		   		
    	} else {
    		String strFieldDB = MVUtil.formatField(strField, boolMode).replaceAll("h\\.", "");
    		strWhere = strWhere.replaceAll("h\\.", "");
    		strSQL = "SELECT DISTINCT " + strFieldDB + " FROM " + strHeaderTable + " " + strWhere + " ORDER BY " + strField;
    	}

    	//  execute the query
		Statement stmt = con.createStatement();
		_logger.debug("handleListVal() - sql: " + strSQL);
		if( 0 == intStart ){ intStart = (new java.util.Date()).getTime(); }
		stmt.executeQuery(strSQL);
		
		//  build a list of values from the query
		int intNumVal = 0;
		ResultSet res = stmt.getResultSet();
		ArrayList listRes = new ArrayList();
		while( res.next() ){
			listRes.add(res.getString(1));
			intNumVal++;
		}
		_logger.debug("handleListVal() - returned " + intNumVal + " values in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStart));		
		String[] listVal = (String[])listRes.toArray(new String[]{});
		
		//  drop the temp table, if present
		if( !"".equals(strTmpTable) ){
			stmt.executeUpdate("DROP TABLE IF EXISTS " + strTmpTable + ";");
		}
		
		//  sort and format the results, depending on the field
		if( strField.equals("fcst_thresh") || strField.equals("fcst_thr") || 
			strField.equals("obs_thresh")  || strField.equals("obs_thr") ){
			listVal = MVUtil.sortThresh(listVal);
		} else if( strField.equals("fcst_lev") || strField.equals("obs_lev") ){
			listVal = MVUtil.sortLev(listVal);
		} else if( strField.equals("fcst_lead") || strField.equals("obs_lead") ){
			listVal = MVUtil.sortFormatLead(listVal, true, false);
		}
		
		//  add the list of field values to the response
		Hashtable tabProb = new Hashtable();
		for(int i=0; i < listVal.length; i++){
			
			//  add the database field value to the list
			strResp += "<val>" + listVal[i] + "</val>";			
			
			//  if the database field value is probabilistic, add a wild card version 
			if( !strField.equals("fcst_var") ){ continue; }
			Matcher matProb = _patProbFcstVar.matcher(listVal[i]);
			if( matProb.matches() ){
				String strProbKey = matProb.group(1) + matProb.group(2);
				String strProbFcstVar = "PROB(" + strProbKey + "*)";
				if( !tabProb.containsKey(strProbKey) ){
					strResp += "<val>" + strProbFcstVar + "</val>";
					tabProb.put(strProbKey, strProbFcstVar);
				}
			}	
		}
		
		//  clean up  
		stmt.close();
		strResp += "</list_val>";
		_tableListValCache.put(strCacheKey, strResp);
		return strResp;
    }
    
    public static final Pattern _patProbFcstVar = Pattern.compile("PROB\\(([\\w\\d]+)([<>=]{1,2})([^\\)]+)\\)");
    
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
			String strResp = "<list_stat><id>" + strId + "</id></list_stat>";
			_logger.debug("handleListStat() - returning mode stats: " + strResp);
			return strResp;
		}
		
    	//  check the list val cache for the request data
    	String strCacheKey = "<db>" + con.getMetaData().getURL() +"</db>" + requestBody.replaceAll("<id>\\d+</id>", "");
    	if( _boolListStatCache && _tableListStatCache.containsKey(strCacheKey) ){
    		String strListStat = _tableListStatCache.get(strCacheKey).toString().replaceAll("<id>\\d+</id>", "<id>" + strId + "</id>"); 
        	_logger.debug("handleListStat() - returning cached value\n  key: " + strCacheKey + "\n  val: " + strListStat);
    		return strListStat;
    	}
    	
    	//  build a query for the fcst_var stat counts
    	String strSQL = "(SELECT COUNT(*), 'cnt'    FROM line_data_cnt    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
						"(SELECT COUNT(*), 'sl1l2'  FROM line_data_sl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'cts'    FROM line_data_cts    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'ctc'    FROM line_data_ctc    ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'nbrcnt' FROM line_data_nbrcnt ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'nbrcts' FROM line_data_nbrcts ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'pstd'   FROM line_data_pstd   ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'mcts'   FROM line_data_mcts   ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
    					"(SELECT COUNT(*), 'rhist'  FROM line_data_rhist  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id) UNION " +
						"(SELECT COUNT(*), 'vl1l2'  FROM line_data_vl1l2  ld, stat_header h WHERE h.fcst_var = '" + strFcstVar + "' AND h.stat_header_id = ld.stat_header_id);";
    	_logger.debug("handleListStat() - gathering stat counts for fcst_var " + strFcstVar + "\n  sql: " + strSQL);
		Statement stmt = con.createStatement();
		long intStart = (new java.util.Date()).getTime();
		stmt.executeQuery(strSQL);

		//  build a list of stat names using the stat ids returned by the query
		String strResp = "<list_stat><id>" + strId + "</id>";
		ResultSet res = stmt.getResultSet();
		ArrayList listStatName = new ArrayList();
		int intStatIndex = 0;
		boolean boolCnt = false;
		boolean boolCts = false;
		while( res.next() ){
			int intStatCount = res.getInt(1);
			if( 0 != intStatCount ){
				switch(intStatIndex){
				case 0: case 1:
					if( !boolCnt ){ listStatName.addAll( Arrays.asList(MVUtil._tableStatsCnt.getKeyList()) ); }
					boolCnt = true;
					break;
				case 2: case 3:
					if( !boolCts ){ listStatName.addAll( Arrays.asList(MVUtil._tableStatsCts.getKeyList()) ); }
					boolCts = true;
					break;
				case 4:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsNbrcnt.getKeyList()) );	break;
				case 5:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsNbrcts.getKeyList()) );	break;
				case 6:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsPstd.getKeyList()) );		break;
				case 7:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsMcts.getKeyList()) );		break;
				case 8:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsRhist.getKeyList()) );		break;
				case 9:		listStatName.addAll( Arrays.asList(MVUtil._tableStatsVl1l2.getKeyList()) );		break;
				}
			}
			intStatIndex++;
		}

		//  sort and build the response string using the list of stat names
		String[] listStat = MVUtil.toArray(listStatName);
		Arrays.sort(listStat, new Comparator(){
			public int compare(Object o1, Object o2){ return ((String)o1).compareTo( (String)o2 ); }
		});
		int intNumStat = 0;
		for(int i=0; i < listStat.length; i++){
			strResp += "<val>" + listStat[i] + "</val>";
			intNumStat++;
		}
		_logger.debug("handleListStat() - returned " + intNumStat + " stats in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStart));
		
		//  clean up  
		stmt.close();
		strResp += "</list_stat>";
		_tableListStatCache.put(strCacheKey, strResp);
		return strResp;
    }
    
	/**
	 * Query the database using the specified connection for a list of the stat_group_lu_ids and associated
	 * stat names.  Store the pairs in the _tableStatGroupName table for use by handleStatList() method.
	 * @param con database connection to search against
	 * @throws Exception
	 */
	public static synchronized void loadStatGroupNames(Connection con) throws Exception{
		_tableStatGroupName.clear();

		//  build the stat_group_lu select query and execute it
		String strSQL = "SELECT stat_group_lu_id, stat_group_name FROM stat_group_lu ORDER BY stat_group_lu_id;";
		Statement stmt = con.createStatement();
		_logger.debug("loadStatGroupNames() - sql: " + strSQL);
		long intStart = (new java.util.Date()).getTime();
		ResultSet rs = stmt.executeQuery(strSQL);
		
		//  build the table with the query results
		while( rs.next() ){
			_tableStatGroupName.put(rs.getString(1), rs.getString(2));
		}
		_logger.debug("loadStatGroupNames() - returned " + _tableStatGroupName.size() + " stats in " + MVUtil.formatTimeSpan((new java.util.Date()).getTime() - intStart));
		
		//  clean up
		stmt.close();
	}
    
    public static final SimpleDateFormat _formatPlot = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    /**
     * Save and parse the plot request XML, then generate the requested plot and return information about it.
     * Update the web_plot table in the database from which the plot data was drawn. 
     * @param strRequest XML plot specification
     * @param con database connection
     * @return status message
     */
    public static String handlePlot(String strRequest, Connection con) throws Exception{
    	
    	//  extract the plot xml from the request
    	String strPlotXML = strRequest;
    	strPlotXML = strPlotXML.substring(strPlotXML.indexOf("</db_con>") + 9);
    	strPlotXML = strPlotXML.substring(0, strPlotXML.indexOf("</request>"));
    	
    	//  query the database to get the next web_plot_id
    	java.util.Date datePlot = new java.util.Date();
    	String strWebPlotId = "#";
    	try{
        	strWebPlotId = getWebPlotIdUpdate(strPlotXML, datePlot, con);
    	} catch(Exception e){
    		_logger.error("handlePlot() - ERROR: caught " + e.getClass() + " establishing web_plot_id: " + e.getMessage());
    		return "<error>failed to establish web_plot_id - reason: " + e.getMessage() + "</error>";
    	}
    	
    	//  construct the names of the plot files
    	String strPlotPrefixId = strWebPlotId; 
    	while( 5 > strPlotPrefixId.length() ){ strPlotPrefixId = "0" + strPlotPrefixId; }
    	String strPlotPrefix = "plot_" + strPlotPrefixId + "_" + _formatPlot.format(datePlot);

    	//  add plot file information to the plot spec
    	String strDBName = con.getMetaData().getURL();
    	strDBName = strDBName.substring(strDBName.lastIndexOf("/") + 1);
    	strPlotXML =
    		"<plot_spec>" +
		 		"<!--" +
		 		"<connection>" +
					"<host>" + _strDBHost + "</host>" +
					"<database>" + strDBName + "</database>" +
					"<user>" + _strDBUser + "</user>" +
					"<password>" + _strDBPassword + "</password>" +
		 		"</connection>" +
		 		"-->" +
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
		
		//  parse the input document and build the MVNode data structure
		Document doc = MVPlotJobParser.getDocumentBuilder().parse(new ByteArrayInputStream( strPlotXML.getBytes() ));
		try{	
			FileOutputStream stream = new FileOutputStream(_strPlotXML + "/" + strPlotPrefix + ".xml");
			XMLSerializer ser = new XMLSerializer(new OutputFormat(Method.XML, "UTF-8", true));
			ser.setOutputByteStream(stream);
			ser.serialize(doc);
			stream.flush();
			stream.close();
		} catch(Exception e) {
			_logger.error("handlePlot() - ERROR: caught " + e.getClass() + " serializing plot xml: " + e.getMessage());
			return "<error>failed to serialize plot xml - reason: " + e.getMessage() + "</error>";
		}    	
    	
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
    		return "<error>failed to parse plot job - reason: " + e.getMessage() + "</error>";
    	}
   	
    	//  run the plot job and write the batch output to the log file
    	ByteArrayOutputStream log = new ByteArrayOutputStream();
    	MVBatch bat = new MVBatch( new PrintStream(log) );
    	bat._intNumPlots = 1;
    	String strJobTmpl = job.getPlotTmpl();
		String strRErrorMsg = "";
    	try{
    		
    		//  configure the batch engine and run the job
    		bat._intNumPlots = 1;
			bat._strRtmplFolder = parser.getRtmplFolder();
			bat._strRworkFolder = parser.getRworkFolder();
			bat._strPlotsFolder = parser.getPlotsFolder();    		
    		
    		//  build the job SQL using the batch engine
    		bat._boolSQLOnly = true;
    		bat._boolVerbose = true;
			if( strJobTmpl.equals("rhist.R_tmpl") ){ bat.runRhistJob(job); }
			else                                   { bat.runJob(job);      }
    		bat._boolSQLOnly = false;
    		bat._boolVerbose = false;
    		String strPlotSQL = log.toString();
    		
    		//  write the plot SQL to a file
    		FileWriter writer = new FileWriter(_strPlotXML + "/" + strPlotPrefix + ".sql");
    		writer.write(strPlotSQL);
    		writer.close();
    		log.reset();
    		
			//  run the job to generate the plot
    		//bat._boolVerbose = true;
			if( strJobTmpl.equals("rhist.R_tmpl") ){ bat.runRhistJob(job); }
			else                                   { bat.runJob(job);      }
    		String strPlotterOutput = log.toString();
    		writer = new FileWriter(_strPlotXML + "/" + strPlotPrefix + ".log");
    		writer.write(strPlotterOutput);
    		writer.close();

    		//  parse out R error messages, if present, throwing an exception if the error was fatal
    		Matcher matOutput = Pattern.compile("(?sm)(==== Start Rscript error  ====.*====   End Rscript error  ====)").matcher(strPlotterOutput);
    		if( matOutput.find() ){ strRErrorMsg = matOutput.group(1); }
    		if( strPlotterOutput.contains("Execution halted") ){ throw new Exception("R error"); }
    		if( strPlotterOutput.contains("query returned no data") ){ throw new Exception("query returned no data"); }
    		
    	} catch(Exception e){
        	_logger.debug("handlePlot() - ERROR: caught " + e.getClass() + " running plot: " + e.getMessage() + "\nbatch output:\n" + log.toString());
        	return "<error>" +
        				"failed to run plot " + strPlotPrefix + " - reason: " + e.getMessage() + 
        				(!strRErrorMsg.equals("")? ":\n" + strRErrorMsg : "") + 
        			"</error>";        	
    	}
    	_logger.debug("handlePlot() - batch output:\n" + log.toString());
    	
    	//  build an archive with the R scripts and data
    	String strTarCmd = "tar cvfC/home/pgoldenb/apps/verif/metviewer/dist/metviewer " + 
    						    "plots/" + strPlotPrefix + ".tar.gz " +
    							"R_work/scripts/" + strPlotPrefix + ".R " +
    							"R_work/data/" + strPlotPrefix + ".data " +
    							"R_work/include/Compute_STDerr.R " +
    							"R_work/include/util_plot.R";
    	if( job.getAggCtc() || job.getAggSl1l2() ){
    		strTarCmd += " " + 
    							"R_work/include/boot.R " +
    							"R_work/data/" + strPlotPrefix + ".boot.info " +
    							"R_work/data/" + strPlotPrefix + ".data.boot ";
    	}
    	try{
        	//runCmd(strTarCmd);    		
    	} catch(Exception e){
    		_logger.error("handlePlot() - caught " + e.getClass() + " creating plot code archive: " + e.getMessage());
    	}

    	return "<plot>" + strPlotPrefix + "</plot>" + (!strRErrorMsg.equals("")? "<r_error>" + strRErrorMsg + "</r_error>": "");
    }
    
    /**
     * Determine what the next web_plot_id should be by querying the database and then create a new entry for the
     * plot.  This code must be syncronized to ensure that there is no conflict among plotting processes. 
     * @param strPlotXML XML plot specification to store in the new dabase entry
     * @param date Plot date used in plot name
     * @param con Database connection that will be queried and updated against
     * @return Web plot id value that was used
     */
    public static synchronized String getWebPlotIdUpdate(String strPlotXML, java.util.Date date, Connection con) throws Exception{

    	//  retrieve the current latest web_plot_id from the database
    	Statement stmt = con.createStatement();
    	String strWebPlotId = "0";
    	if( !stmt.execute("SELECT MAX(web_plot_id) FROM web_plot;") ){ throw new Exception("Statment.execute() returned false"); }
    	ResultSet res = stmt.getResultSet();
    	while( res.next() ){
    		int intWebPlotId = res.getInt(1);
    		if( !res.wasNull() ){ strWebPlotId = "" + (intWebPlotId + 1); } 
    	}
    	stmt.close();
    	
    	//  store the web_plot information in the database
    	stmt = con.createStatement();
    	int intRes = stmt.executeUpdate("INSERT INTO web_plot VALUES (" + strWebPlotId + ", '" + MVUtil._formatDB.format(date) + "', '" + strPlotXML + "');");
    	if( 1 != intRes ){ throw new Exception("unexpected result from web_plot INSERT statement: " + intRes); }
    	stmt.close();

    	return strWebPlotId;
    }
    
    /**
     * Print an error message into writer of the input response 
     * @param response
     */
    public static void printErrorPage(HttpServletResponse response) throws IOException{    	
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.print(
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n\n" +
			"<html>\n" +
			"<head>\n" +
			"<title>METViewer Error</title>\n" +
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"/metviewer/include/metviewer.css\"/>\n" +
			"<link rel=\"shortcut icon\" href=\"/metviewer/include/ral_icon.ico\" type=\"image/x-icon\"/>\n" +
			"</head>\n" +
			"<body style=\"padding-left:20px; padding-top:20px\">\n" +
			"<span class=\"bold\">An error has occurred in METViewer.  Please contact your system administrator</span>\n" +
			"</body></html>\n\n");
    }

    
	public static void runCmd(String cmd) throws Exception{

		//  run the command and wait for completion
		String strMsg = "runCmd()\n$ " + cmd + "\n";
		Process proc = Runtime.getRuntime().exec(cmd);
		proc.waitFor();

		//  read and print the standard error
		String strStdErr = "";
		BufferedReader readerProcError = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );		
		while( readerProcError.ready() ){ strStdErr += readerProcError.readLine() + "\n"; }
		readerProcError.close();
		if( !"".equals(strStdErr) ){ strMsg += strStdErr; }

		//  read and print the standard out
		String strStdOut = "";
		BufferedReader readerProcIn = new BufferedReader( new InputStreamReader(proc.getInputStream()) );		
		while( readerProcIn.ready() ){ strStdOut += readerProcIn.readLine() + "\n"; }
		readerProcIn.close();
		if( !"".equals(strStdOut) ){ strMsg += strStdOut; }
		
		_logger.debug(strMsg);
	}

    
}

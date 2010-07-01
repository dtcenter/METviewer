package edu.ucar.metviewer;

import java.io.*;
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
	
	public void init() throws ServletException {
		_logger.error("init() - loading properties...");
		Properties prop = new Properties();
		try{
			prop.load(new FileInputStream("mvservlet.properties"));
		}catch(Exception e){
			_logger.error("init() - ERROR: caught " + e.getClass() + " loading properties: " + e.getMessage());
		}		
		_logger.error("init() - done loading properties: " + prop.getProperty("db.username"));
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
    	
    	PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

    	BufferedReader reader = new BufferedReader( new InputStreamReader(request.getInputStream()) );
    	String strLine = "";
    	String strRequestBody = "";
    	while( (strLine = reader.readLine()) != null ){ strRequestBody = strRequestBody + strLine; }
    	_logger.info("doPost():  " + strRequestBody);
       
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
					System.out.println("  **  ERROR: " + e.getMessage() + "\n" +
									   "      line: " + e.getLineNumber() + "  column: " + e.getColumnNumber());					
				}
			});
			
			//  parse the input document and build the MVNode data structure
			//Document doc = builder.parse(request.getInputStream());
			Document doc = builder.parse( new ByteArrayInputStream(strRequestBody.getBytes()) );
			MVNode nodeReq = new MVNode(doc.getFirstChild());
			
			for(int i=0; i < nodeReq._children.length; i++){
				MVNode nodeCall = nodeReq._children[i];
				if( nodeCall._tag.equalsIgnoreCase("list_db") ){
					String[] listDB = listDB();
				}
			}
			String strVal = nodeReq._value;
        }catch(Exception e){
        	out.println("ERROR: caught " + e.getClass() + ": " + e.getMessage());
        }
    	
        doGet(request, response);
    }
    
    public static String[] listDB() throws Exception{
    	
    	String strCmd = "mysql -upgoldenb -ppgoldenb -s -e\"show databases;\"";
    	String[] listCmd = {"mysql", "-upgoldenb", "-ppgoldenb", "-s", "-e\\\"show databases\\\""};
    	_logger.debug("listDB() - running command: " + strCmd);
    	
    	//  issue the list databases command
		Process proc = Runtime.getRuntime().exec(listCmd);
		if( MVBatch._boolProcWait ){
			proc.waitFor();
		} else {
			try{
				Thread.sleep(10000);
			}catch(InterruptedException ie){}
		}

		//  build a list of the METViewer databases
		ArrayList listDB = new ArrayList();
		BufferedReader readerProcIn = new BufferedReader( new InputStreamReader(proc.getInputStream()) );		
		while( readerProcIn.ready() ){
			String strDB = readerProcIn.readLine();
			if( !strDB.startsWith("metvdb_") ){ listDB.add(strDB); } 
		}
		readerProcIn.close();			
		
		//  print the error information to the log file
		String strErr = "";
		BufferedReader readerProcError = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );		
		while( readerProcError.ready() ){ strErr += readerProcError.readLine() + "\n"; }
		readerProcError.close();
		if( !"".equals(strErr) ){ _logger.debug("listDB() - error information: " + strErr); }

		return new String[]{};
    }
}

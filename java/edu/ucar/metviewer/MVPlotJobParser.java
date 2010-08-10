package edu.ucar.metviewer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;

public class MVPlotJobParser extends MVUtil{

	protected Document _doc = null;
	
	protected MVPlotJob[] _listJobs = {};
	protected MVOrderedMap _mapJobs = new MVOrderedMap();
	
	protected Hashtable _tableDateListDecl = new Hashtable();
	protected Hashtable _tableDateRangeDecl = new Hashtable();
	protected Hashtable _tableDateRangeListDecl = new Hashtable();
	protected Hashtable _tablePlotDecl = new Hashtable();
	protected Hashtable _tableTmplVal = new Hashtable();
	protected MVNode _nodePlotSpec = null;
	
	protected Connection _con = null;
	protected String _strRscript = "Rscript";
	protected String _strRtmplFolder = "";
	protected String _strRworkFolder = "";
	protected String _strPlotsFolder = "";

	/*
	public static void main(String[] args) {
		System.out.println("----  MVPlotJobParser  ----\n");

		Connection con = null;
		try {

			//  connect to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://kemosabe:3306/metvdb_hmt", "pgoldenb", "pgoldenb");
			if( con.isClosed() )	throw new Exception("database connection failed");			
			System.out.println("connected to kemosabe");
			
			//  parse the data structure
			MVPlotJobParser parser = new MVPlotJobParser("plot.xml", con);
			MVPlotJob[] jobs = parser.parsePlotJobSpec();
			int intNumJobs = jobs.length;
			
		} catch(SAXParseException se){
			System.out.println("  **  ERROR: caught " + se.getClass() + ": " + se.getMessage());
		} catch(Exception ex){
			System.out.println("  **  ERROR: caught " + ex.getClass() + ": " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try{ if( con != null )	con.close(); }catch(SQLException e){}
		}
		System.out.println("----  MVPlotJobParser Done  ----");
	}
	*/
	
	/**
	 * Build a parser whose input source is the specified URI
	 * @param spec URI of the XML plot specification source
	 * @param con Optional database connection to use in absense of plot_spec &lt;connection&gt;
	 */
	public MVPlotJobParser(String spec, Connection con) throws Exception{		
		_con = con;
		DocumentBuilder builder = getDocumentBuilder();
		
		//  parse the input document and build the MVNode data structure
		_doc = builder.parse(spec);
		_nodePlotSpec = new MVNode(_doc.getFirstChild());
		parsePlotJobSpec();
	}
	public MVPlotJobParser(String spec) throws Exception{ this(spec, null); }

	/**
	 * Build a parser whose input source is the specified InputStream
	 * @param in Stream from which the plot specification will be drawn
	 * @param con Database connection for the plot data
	 */
	public MVPlotJobParser(InputStream in, Connection con) throws Exception{		
		_con = con;
		DocumentBuilder builder = getDocumentBuilder();
		
		//  parse the input document and build the MVNode data structure
		_doc = builder.parse(in);
		_nodePlotSpec = new MVNode(_doc.getFirstChild());
		parsePlotJobSpec();
	}
	
	/**
	 * Create a parser-specific instance of the DocumentBuilder and return it 
	 */
	public static DocumentBuilder getDocumentBuilder() throws Exception{

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
		
		return builder;
	}
	
	public Document getDocument(){ return _doc; }
	
	public MVPlotJob[] getJobsList(){ return _listJobs; }
	public MVOrderedMap getJobsMap(){ return _mapJobs; }
	
	public String getRtmplFolder(){ return _strRtmplFolder; }
	public String getRworkFolder(){ return _strRworkFolder; }
	public String getPlotsFolder(){ return _strPlotsFolder; }
	
	protected void parsePlotJobSpec() throws Exception{
		ArrayList listJobs = new ArrayList();
		String strDBHost = "";
		String strDBName = "";
		String strDBUser = "";
		String strDBPassword ="";		
		
		for(int i=0; null != _nodePlotSpec && i < _nodePlotSpec._children.length; i++){
			MVNode node = _nodePlotSpec._children[i];
			//  <connection>
			if( node._tag.equals("connection") ){
				for(int j=0; j < node._children.length; j++){
					if     ( node._children[j]._tag.equals("host") )	{ strDBHost		= node._children[j]._value; }
					else if( node._children[j]._tag.equals("database") ){ strDBName		= node._children[j]._value; }
					else if( node._children[j]._tag.equals("user") )	{ strDBUser		= node._children[j]._value; }
					else if( node._children[j]._tag.equals("password") ){ strDBPassword	= node._children[j]._value; }
				}

				try {
					//  connect to the database
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection con = DriverManager.getConnection("jdbc:mysql://" + strDBHost + "/" + strDBName, strDBUser, strDBPassword);
					if( con.isClosed() )	throw new Exception("METViewer error: database connection failed");
					_con = con;
				} catch(Exception ex){
					System.out.println("  **  ERROR: parsePlotJob() caught " + ex.getClass() + " connecting to database: " + ex.getMessage());
				}
			}
			
			//  <rscript>
			else if( node._tag.equals("rscript") ){
				_strRscript = node._value;
			}
				
			//  <folders>
			else if( node._tag.equals("folders") ){
				for(int j=0; j < node._children.length; j++){
					if     ( node._children[j]._tag.equals("r_tmpl") )	{ _strRtmplFolder	= node._children[j]._value; }
					else if( node._children[j]._tag.equals("r_work") )	{ _strRworkFolder	= node._children[j]._value; }
					else if( node._children[j]._tag.equals("plots") )	{ _strPlotsFolder	= node._children[j]._value; }
				}
			}

			//  <date_list>
			else if( node._tag.equals("date_list") ){
				String strName = node._name;				
				String[] listDates = parseDateList(node, _con);
				_tableDateListDecl.put(strName, listDates);
			}

			//  <date_range>
			else if( node._tag.equals("date_range") ){
				_tableDateRangeDecl.put(node._name, parseDateRange(node));
			}
			
			//  <date_range_list>
			else if( node._tag.equals("date_range_list") ){
				
				//  gather the elements of the range list
				String strRangeStart = "";
				String strRangeEnd = "";
				int intRangeLength = -1;
				int intInc = -1;
				String strFormat = _formatDB.toPattern();
				for(int l=0; l < node._children.length; l++){
					MVNode nodeChild = node._children[l];
					if     ( nodeChild._tag.equals("range_start") ) { strRangeStart = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }
					else if( nodeChild._tag.equals("range_end") )   { strRangeEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }					
					else if( nodeChild._tag.equalsIgnoreCase("range_length") ){	intRangeLength = Integer.parseInt(nodeChild._value); }					
					else if( nodeChild._tag.equalsIgnoreCase("inc") )         { intInc         = Integer.parseInt(nodeChild._value); }
				}
			
				//  parse the begin and end times
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				long intEndTime = -1;
				try{
					cal.setTime( _formatDB.parse(strRangeStart) );
					intEndTime = _formatDB.parse(strRangeEnd).getTime();
				}catch(Exception e){}
				
				//  build the list
				ArrayList listDateRange = new ArrayList();
				while( cal.getTime().getTime() <= intEndTime ){
					String strStartCur = _formatDB.format( cal.getTime() );
					cal.add(Calendar.MINUTE, intRangeLength);
					String strEndCur   = _formatDB.format( cal.getTime() );
					listDateRange.add("BETWEEN '" + strStartCur + "' AND '" + strEndCur + "'");
					cal.add(Calendar.MINUTE, intInc - intRangeLength);					
				}
				_tableDateRangeListDecl.put(node._name, toArray(listDateRange));
			}

			//  <plot>
			else if( node._tag.equals("plot") ){

				//  make sure the database connection has been established
				if( _con == null ){
					System.out.println("  **  ERROR: database connection missing for plot " + node._name);
					return;
				}
				
				//  parse the plot and add it to the job table and, if appropriate, the list of runnable jobs 
				String strInherits = node._inherits;
				MVPlotJob jobBase = ( !strInherits.equals("") ? (MVPlotJob)_tablePlotDecl.get(strInherits) : null);
				MVPlotJob job = parsePlotJob(node, jobBase);
				
				//  set the job database information  
				job.setConnection(_con);
				job.setRscript(_strRscript);
				job.setDBHost(strDBHost);
				job.setDBName(strDBName);
				job.setDBUser(strDBUser);
				job.setDBPassword(strDBPassword);
				
				//  add the job to the jobs table and to the runnable jobs, if appropriate
				_tablePlotDecl.put(node._name, job);
				String strCompleteness = checkJobCompleteness(job);
				boolean boolComplete = strCompleteness.equals("");
				boolean boolPlotRun = !node._run.equalsIgnoreCase("false");
				if( boolComplete )	{
					if( boolPlotRun ){ _mapJobs.put(node._name, job); }
					listJobs.add( job );
				} else if( boolPlotRun ){
					throw new Exception("plot set to run lacks " + strCompleteness);
				}
			}
		}
		
		_listJobs = (MVPlotJob[])listJobs.toArray(new MVPlotJob[]{});
	}
	
	/**
	 * Parse a single xml plot specification &lt;plot&gt; node from an xml plot specification
	 * and return the resulting MVPlotJob.  The inherited job is specified by the input jobBase. 
	 * @param nodePlot XML plot specification object to parse
	 * @param jobBase MVPlotJob whose characteristics to inherit
	 * @return Populated MVPlot structure
	 */
	public MVPlotJob parsePlotJob(MVNode nodePlot, MVPlotJob jobBase) throws Exception{
		MVPlotJob job = (null != jobBase? jobBase.copy() : new MVPlotJob());

		for(int i=0; i < nodePlot._children.length; i++){
			MVNode node = nodePlot._children[i];
			
			//  <template>
			if( node._tag.equals("template") ){
				job.setPlotTmpl(node._value);
			}
			
			//  <indep>
			else if( node._tag.equals("indep") ){
				job.setIndyVar(node._name);
				if( !"".equals(node._depends) ){
					job.setIndyDep(new MVPlotDep(node._depends, node));
				} else {
					String[][] listIndy = parseIndyNode(node, "");
					job.setIndyVal( listIndy[0] );
					job.setIndyLabel( listIndy[1] );
					job.setIndyPlotVal( listIndy[2] );
				}
			}
			
			//  <plot_fix>
			else if( node._tag.equals("plot_fix") ){
				
				for(int j=0; j < node._children.length; j++){
					MVNode nodeFix = node._children[j];	

					//  <remove> and <clear>
					if     ( nodeFix._tag.equals("remove") ){		job.removePlotFixVal(nodeFix._name);	continue;	}
					else if( nodeFix._tag.equals("clear") ) {		job.clearPlotFixVal();					continue;	}
					
					//  <field>
					ArrayList listFixVal = new ArrayList();
					MVOrderedMap mapFixVal = new MVOrderedMap();
					MVOrderedMap mapTmplVal = new MVOrderedMap();
					for(int k=0; k < nodeFix._children.length; k++){
						MVNode nodeFixVal = nodeFix._children[k];
						
						//  <val>
						if( nodeFixVal._tag.equals("val") ){ listFixVal.add(nodeFixVal._value); }
						
						//  <set>
						else if( nodeFixVal._tag.equals("set") ){
							String[] listAggSet = new String[nodeFixVal._children.length];
							for(int l=0; l < nodeFixVal._children.length; l++){ listAggSet[l] = nodeFixVal._children[l]._value; }
							mapFixVal.put(nodeFixVal._name, listAggSet);
						}
						
						//  <date_list>
						else if( nodeFixVal._tag.equals("date_list") ){
							listFixVal.addAll( Arrays.asList((String[])_tableDateListDecl.get(nodeFixVal._name)) );
						}
						
						//  <date_range>
						else if( nodeFixVal._tag.equals("date_range") ){
							String strDateRangeVal = _tableDateRangeDecl.get(nodeFixVal._name).toString(); 
							listFixVal.add(strDateRangeVal);
							mapTmplVal.put(strDateRangeVal, nodeFixVal._name);
							if( !nodeFixVal._id.equals("") ){ job.addTmplVal(nodeFixVal._id, nodeFixVal._name); }
						}

						//  <date_range_list>
						else if( nodeFixVal._tag.equals("date_range_list") ){
							String[] listDateRange = (String[])_tableDateRangeListDecl.get(nodeFixVal._name); 
							listFixVal.addAll(Arrays.asList(listDateRange));
						}

					}
					if     ( 0 < listFixVal.size() ){ job.addPlotFixVal(nodeFix._name, toArray(listFixVal)); }
					else if( 0 < mapFixVal.size() ) { job.addPlotFixVal(nodeFix._name, mapFixVal); }
					
					if( 0 < mapTmplVal.size() ){ job.addTmplMap(nodeFix._name, mapTmplVal); }
				}
			}
			
			//  <series1> or <series2>
			else if( node._tag.equals("series1") || node._tag.equals("series2") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeSeries = node._children[j];
					
					//  <remove>
					if( nodeSeries._tag.equals("remove") ){
						if     ( node._tag.equals("series1") ){ job.removeSeries1Val(nodeSeries._name); }
						else if( node._tag.equals("series2") ){ job.removeSeries1Val(nodeSeries._name); }
						continue;
					}
					
					//  <clear>
					else if( nodeSeries._tag.equals("clear") ){
						if     ( node._tag.equals("series1") ){ job.clearSeries1Val(); }
						else if( node._tag.equals("series2") ){ job.clearSeries2Val(); }
						continue;
					}
					
					//  <field>
					String[] listAggVal = new String[nodeSeries._children.length];
					for(int k=0; k < nodeSeries._children.length; k++){ listAggVal[k] = nodeSeries._children[k]._value; }
					if     ( node._tag.equals("series1") ){ job.addSeries1Val(nodeSeries._name, listAggVal); }
					else if( node._tag.equals("series2") ){ job.addSeries2Val(nodeSeries._name, listAggVal); }
				}
			}
			
			//  <series_nobs>
			else if( node._tag.equals("series_nobs") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeSeriesNobs = node._children[j];
					
					//  <remove> and <clear>
					if     ( nodeSeriesNobs._tag.equals("remove") ){	job.removeSeriesNobs(nodeSeriesNobs._name);		continue;	}
					else if( nodeSeriesNobs._tag.equals("clear") ) {	job.clearSeriesNobs();							continue;	}
					
					//  <field>
					String strField = nodeSeriesNobs._name;
					String strValue = nodeSeriesNobs._children[0]._value;
					job.addSeriesNobs(strField, strValue);
				}
			}
			
			//  <dep>
			else if( node._tag.equals("dep") ){
				
				//job.addDepGroup( buildDepMap(node) );

				//  <dep>
				MVOrderedMap mapDep = new MVOrderedMap();
				for(int j=0; j < node._children.length; j++){
					MVNode nodeDepN = node._children[j];
					
					//  <clear>
					if( nodeDepN._tag.equals("clear") ){ job.clearDepGroups(); }

					//  <mode_group>
					else if( nodeDepN._tag.equals("mode_group") ){
						ArrayList listModeGroup = new ArrayList();

						String strDepN = "";
						for(int k=0; k < nodeDepN._children.length; k++){
							MVNode nodeDepNMode = nodeDepN._children[k];

							//  <dep1> or <dep2>
							if( nodeDepNMode._tag.startsWith("dep") ){
								MVOrderedMap mapDepNMode = new MVOrderedMap();
								strDepN = nodeDepNMode._tag;
								
								//  <fcst_var>
								for(int l=0; l < nodeDepNMode._children.length; l++){
									MVNode nodeFcstVar = nodeDepNMode._children[l];					
									ArrayList listStats = new ArrayList();
									
									//  <stat>s
									for(int m=0; m < nodeFcstVar._children.length; m++){
										listStats.add(nodeFcstVar._children[m]._value);
									}
									mapDepNMode.put(nodeFcstVar._name, listStats.toArray(new String[]{}));
								}
								listModeGroup.add(mapDepNMode);
							}
						}
						mapDep.put(strDepN, (MVOrderedMap[])listModeGroup.toArray(new MVOrderedMap[]{}));						
					}
					
					//  <dep1> or <dep2>
					else if( nodeDepN._tag.startsWith("dep") ){
						MVOrderedMap mapDepN = new MVOrderedMap();
						
						//  <fcst_var>
						for(int k=0; k < nodeDepN._children.length; k++){
							MVNode nodeFcstVar = nodeDepN._children[k];					
							ArrayList listStats = new ArrayList();
							
							//  <stat>s
							for(int l=0; l < nodeFcstVar._children.length; l++){
								String strStat = nodeFcstVar._children[l]._value;
								if( !MVBatch._tableStatIndex.containsKey(strStat) && !MVBatch._tableModeStatIndex.containsKey(strStat) ){
									throw new Exception("unknown stat name " + strStat);
								}
								listStats.add(strStat);
							}
							mapDepN.put(nodeFcstVar._name, listStats.toArray(new String[]{}));
						}
						mapDep.put(nodeDepN._tag, mapDepN);
					}
					
					//  <fix>
					else if( nodeDepN._tag.startsWith("fix") ){
						MVOrderedMap mapFix = new MVOrderedMap();
						
						//  <fcst_var>
						for(int k=0; k < nodeDepN._children.length; k++){
							MVNode nodeFcstVar = nodeDepN._children[k];					
							MVOrderedMap mapFcstVar = new MVOrderedMap();
							
							//  <var>s
							for(int l=0; l < nodeFcstVar._children.length; l++){
								MVNode nodeChild = nodeFcstVar._children[l];
								String strValue = "";
								//mapFcstVar.put(nodeChild._name, nodeChild._value);
								
								//  atomic values
								if( 1 > nodeChild._children.length ){ strValue = nodeChild._value; }
								
								//  <set>
								else if( nodeChild._children[0]._tag.equals("set") ){

								}
								
								//  <date_range>
								else if( nodeChild._children[0]._tag.equals("date_range") ){
									MVNode nodeDateRange = nodeChild._children[0];
									strValue = _tableDateRangeDecl.get(nodeDateRange._name).toString();
									if( !nodeDateRange._id.equals("") ){ job.addTmplVal(nodeDateRange._id, nodeDateRange._name); }
								}

								//  <date_list>
								else if( nodeChild._children[0]._tag.equals("date_list") ){

								}
								
								mapFcstVar.put(nodeChild._name, strValue); 
							}
							mapFix.put(nodeFcstVar._name, mapFcstVar);
						}
						mapDep.put(nodeDepN._tag, mapFix);
					}
				}
				job.addDepGroup( mapDep );
			}
			
			//  <agg>
			else if( node._tag.equals("agg") ){
				
				for(int j=0; j < node._children.length; j++){
					MVNode nodeAgg = node._children[j];	

					//  <remove> and <clear>
					if     ( nodeAgg._tag.equals("remove") ){		job.removeAggVal(nodeAgg._name);		continue;	}
					else if( nodeAgg._tag.equals("clear") ) {		job.clearAggVal();						continue;	}
					
					//  <field>
					ArrayList listAggVal = new ArrayList();
					MVOrderedMap mapAggVal = new MVOrderedMap();
					for(int k=0; k < nodeAgg._children.length; k++){
						MVNode nodeChild = nodeAgg._children[k];
						
						//  <val>
						if( nodeChild._tag.equals("val") ){ listAggVal.add(nodeChild._value); }
						
						//  <set>
						else if( nodeChild._tag.equals("set") ){
							String[] listAggSet = new String[nodeChild._children.length];
							for(int l=0; l < nodeChild._children.length; l++){ listAggSet[l] = nodeChild._children[l]._value; }
							mapAggVal.put(nodeChild._name, listAggSet);
						}
						
						//  <date_list>
						else if( nodeChild._tag.equals("date_list") ){
							listAggVal.addAll(Arrays.asList( (String[])_tableDateListDecl.get(nodeChild._name) ));							
						}
					}
					if     ( 0 < listAggVal.size() ){ job.addAggVal(nodeAgg._name, (String[])listAggVal.toArray(new String[]{})); }
					else if( 0 < mapAggVal.size() ) { job.addAggVal(nodeAgg._name, mapAggVal); }
				}
			}
			
			//  <tmpl>
			else if( node._tag.equals("tmpl") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeTmpl = node._children[j];
					
					//  <val_map>
					if( nodeTmpl._tag.equals("val_map") ){
						MVOrderedMap mapValMap = new MVOrderedMap();
						for(int k=0; k < nodeTmpl._children.length; k++){
							MVNode nodeKey = nodeTmpl._children[k]._children[0];
							String strKey = "";							
							if( 0 < nodeKey._children.length && nodeKey._children[0]._tag.equals("date_range") ){
								strKey = _tableDateRangeDecl.get(nodeKey._children[0]._name).toString(); 
							}
							else { strKey = nodeKey._value; }
							
							MVNode nodeVal = nodeTmpl._children[k]._children[1];
							mapValMap.put(strKey, nodeVal._value);
						}
						job.addTmplMap(nodeTmpl._name, mapValMap);
					}
					
					//  templates
					else if( nodeTmpl._tag.equals("data_file") )	{ job.setDataFileTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("plot_file") )	{ job.setPlotFileTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("r_file") )		{ job.setRFileTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("title") )		{ job.setTitleTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("x_label") )		{ job.setXLabelTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("y1_label") )		{ job.setY1LabelTmpl(nodeTmpl._value);	} 
					else if( nodeTmpl._tag.equals("y2_label") )		{ job.setY2LabelTmpl(nodeTmpl._value);	} 
					
				}				
			}
			
			//  <dep1_scale> <dep2_scale>
			else if( node._tag.equals("dep1_scale") || node._tag.equals("dep2_scale") ){

				for(int j=0; j < node._children.length; j++){
					MVNode nodeDepScale = node._children[j];
					
					//  <remove>
					if( nodeDepScale._tag.equals("remove") ){
						if     ( node._tag.equals("dep1_scale") ){ job.removeDep1Scale(nodeDepScale._name); }
						else if( node._tag.equals("dep2_scale") ){ job.removeDep2Scale(nodeDepScale._name); }
					}
					
					//  <clear>
					else if( nodeDepScale._tag.equals("clear") ){
						if     ( node._tag.equals("dep1_scale") ){ job.clearDep1Scale(); }
						else if( node._tag.equals("dep2_scale") ){ job.clearDep2Scale(); }
					}

					//  <field>
					else if( node._tag.equals("dep1_scale") ){	job.addDep1Scale(nodeDepScale._name, nodeDepScale._value);	}
					else if( node._tag.equals("dep2_scale") ){	job.addDep2Scale(nodeDepScale._name, nodeDepScale._value);	}					
				}
				
			}
			
			//  <bootstrapping>
			else if( node._tag.equals("bootstrapping") ){
				job.setBootstrapping(true);
				
				for(int j=0; j < node._children.length; j++){
					MVNode nodeBoot = node._children[j];					
					if( nodeBoot._tag.equals("boot_repl") )			{ job.setBootRepl(nodeBoot._value);                           }
					else if( nodeBoot._tag.equals("boot_ci") )		{ job.setBootCI(nodeBoot._value);                             }
					else if( nodeBoot._tag.equals("boot_diff1") )	{ job.setBootDiff1(nodeBoot._value.equalsIgnoreCase("true")); }
					else if( nodeBoot._tag.equals("boot_diff2") )	{ job.setBootDiff2(nodeBoot._value.equalsIgnoreCase("true")); }
				}				
			}
			
			else if( _tableFormatBoolean.containsKey(node._tag) ){
				Method m = (Method)_tableFormatBoolean.get(node._tag);
				try{
					m.invoke(job, new Object[]{new Boolean(node._value.equals("true"))});
				}catch(Exception e){
					System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format boolean '" + node._tag + "': " + e.getMessage());
				}
			}
			
			else if( _tableFormatString.containsKey(node._tag) ){
				Method m = (Method)_tableFormatString.get(node._tag);
				try{
					m.invoke(job, new Object[]{node._value});
				}catch(Exception e){
					System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format string '" + node._tag + "': " + e.getMessage());
				}
			}
			
			else{
				System.out.println("  **  WARNING: unused plot tag '" + node._tag + "'");
			}
		}
		
		return job;
	}
	
	public static String checkJobCompleteness(MVPlotJob job){
		if     ( job.getPlotTmpl().equals("")     )	{ return "template";	}
		else if( job.getIndyVar().equals("")      )	{ return "indep";		}
		else if( 1 > job.getIndyVal().length && 
				 null == job.getIndyDep()         )	{ return "indep";		}
		else if( 1 > job.getDepGroups().length    )	{ return "dep";			}
		else if( 1 > job.getSeries1Val().size()   )	{ return "series1";		}
		else if( 1 > job.getAggVal().size() &&
				 1 > job.getPlotFixVal().size() )	{ return "agg/plot_fix";}
		else if( job.getRFileTmpl().equals("")    )	{ return "r_file";		}
		else if( job.getPlotFileTmpl().equals("") )	{ return "plot_file";	}
		else if( job.getDataFileTmpl().equals("") )	{ return "data_file";	}
		else if( job.getXLabelTmpl().equals("")   )	{ return "x_label";		}
		else if( job.getY1LabelTmpl().equals("")  )	{ return "y1_label";	}
		
		return "";
	}
	
	public static final Hashtable _tableFormatBoolean = new Hashtable();
	static{
		try{
			_tableFormatBoolean.put("event_equal",	MVPlotJob.class.getDeclaredMethod("setEventEqual",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("vert_plot",	MVPlotJob.class.getDeclaredMethod("setVertPlot",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("x_reverse",	MVPlotJob.class.getDeclaredMethod("setXReverse",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("plot1_diff",	MVPlotJob.class.getDeclaredMethod("setPlot1Diff",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("plot2_diff",	MVPlotJob.class.getDeclaredMethod("setPlot2Diff",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("num_stats",	MVPlotJob.class.getDeclaredMethod("setShowNStats",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("indy1_stag",	MVPlotJob.class.getDeclaredMethod("setIndy1Stagger",new Class[]{boolean.class}));
			_tableFormatBoolean.put("indy2_stag",	MVPlotJob.class.getDeclaredMethod("setIndy2Stagger",new Class[]{boolean.class}));
			_tableFormatBoolean.put("grid_on",		MVPlotJob.class.getDeclaredMethod("setGridOn",		new Class[]{boolean.class}));
			_tableFormatBoolean.put("sync_axes",	MVPlotJob.class.getDeclaredMethod("setSyncAxes",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("dump_points1",	MVPlotJob.class.getDeclaredMethod("setDumpPoints1",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("dump_points2",	MVPlotJob.class.getDeclaredMethod("setDumpPoints2",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("log_y1",		MVPlotJob.class.getDeclaredMethod("setLogY1",		new Class[]{boolean.class}));
			_tableFormatBoolean.put("log_y2",		MVPlotJob.class.getDeclaredMethod("setLogY2",		new Class[]{boolean.class}));
		}catch(NoSuchMethodException e){}
	}
	
	public static final Hashtable _tableFormatString = new Hashtable();
	static{
		try{
			_tableFormatString.put("plot_type",		MVPlotJob.class.getDeclaredMethod("setPlotType",	new Class[]{String.class}));
			_tableFormatString.put("plot_height",	MVPlotJob.class.getDeclaredMethod("setPlotHeight",	new Class[]{String.class}));
			_tableFormatString.put("plot_width",	MVPlotJob.class.getDeclaredMethod("setPlotWidth",	new Class[]{String.class}));
			_tableFormatString.put("plot_res",		MVPlotJob.class.getDeclaredMethod("setPlotRes",		new Class[]{String.class}));
			_tableFormatString.put("plot_units",	MVPlotJob.class.getDeclaredMethod("setPlotUnits",	new Class[]{String.class}));
			_tableFormatString.put("mar",			MVPlotJob.class.getDeclaredMethod("setMar",			new Class[]{String.class}));
			_tableFormatString.put("mgp",			MVPlotJob.class.getDeclaredMethod("setMgp",			new Class[]{String.class}));
			_tableFormatString.put("cex",			MVPlotJob.class.getDeclaredMethod("setCex",			new Class[]{String.class}));
			_tableFormatString.put("title_weight",	MVPlotJob.class.getDeclaredMethod("setTitleWeight",	new Class[]{String.class}));
			_tableFormatString.put("title_size",	MVPlotJob.class.getDeclaredMethod("setTitleSize",	new Class[]{String.class}));
			_tableFormatString.put("title_offset",	MVPlotJob.class.getDeclaredMethod("setTitleOffset",	new Class[]{String.class}));
			_tableFormatString.put("title_align",	MVPlotJob.class.getDeclaredMethod("setTitleAlign",	new Class[]{String.class}));
			_tableFormatString.put("xtlab_orient",	MVPlotJob.class.getDeclaredMethod("setXtlabOrient",	new Class[]{String.class}));
			_tableFormatString.put("xtlab_perp",	MVPlotJob.class.getDeclaredMethod("setXtlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("xtlab_horiz",	MVPlotJob.class.getDeclaredMethod("setXtlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("xlab_weight",	MVPlotJob.class.getDeclaredMethod("setXlabWeight",	new Class[]{String.class}));
			_tableFormatString.put("xlab_size",		MVPlotJob.class.getDeclaredMethod("setXlabSize",	new Class[]{String.class}));
			_tableFormatString.put("xlab_offset",	MVPlotJob.class.getDeclaredMethod("setXlabOffset",	new Class[]{String.class}));
			_tableFormatString.put("xlab_align",	MVPlotJob.class.getDeclaredMethod("setXlabAlign",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_orient",	MVPlotJob.class.getDeclaredMethod("setYtlabOrient",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_perp",	MVPlotJob.class.getDeclaredMethod("setYtlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_horiz",	MVPlotJob.class.getDeclaredMethod("setYtlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("ylab_weight",	MVPlotJob.class.getDeclaredMethod("setYlabWeight",	new Class[]{String.class}));
			_tableFormatString.put("ylab_size",		MVPlotJob.class.getDeclaredMethod("setYlabSize",	new Class[]{String.class}));
			_tableFormatString.put("ylab_offset",	MVPlotJob.class.getDeclaredMethod("setYlabOffset",	new Class[]{String.class}));
			_tableFormatString.put("ylab_align",	MVPlotJob.class.getDeclaredMethod("setYlabAlign",	new Class[]{String.class}));
			_tableFormatString.put("grid_lty",		MVPlotJob.class.getDeclaredMethod("setGridLty",		new Class[]{String.class}));
			_tableFormatString.put("grid_col",		MVPlotJob.class.getDeclaredMethod("setGridCol",		new Class[]{String.class}));
			_tableFormatString.put("grid_lwd",		MVPlotJob.class.getDeclaredMethod("setGridLwd",		new Class[]{String.class}));
			_tableFormatString.put("grid_x",		MVPlotJob.class.getDeclaredMethod("setGridX",		new Class[]{String.class}));
			_tableFormatString.put("x2tlab_orient",	MVPlotJob.class.getDeclaredMethod("setX2tlabOrient",new Class[]{String.class}));
			_tableFormatString.put("x2tlab_perp",	MVPlotJob.class.getDeclaredMethod("setX2tlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("x2tlab_horiz",	MVPlotJob.class.getDeclaredMethod("setX2tlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_weight",	MVPlotJob.class.getDeclaredMethod("setX2labWeight",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_size",	MVPlotJob.class.getDeclaredMethod("setX2labSize",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_offset",	MVPlotJob.class.getDeclaredMethod("setX2labOffset",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_align",	MVPlotJob.class.getDeclaredMethod("setX2labAlign",	new Class[]{String.class}));
			_tableFormatString.put("y2tlab_orient",	MVPlotJob.class.getDeclaredMethod("setY2tlabOrient",new Class[]{String.class}));
			_tableFormatString.put("y2tlab_perp",	MVPlotJob.class.getDeclaredMethod("setY2tlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("y2tlab_horiz",	MVPlotJob.class.getDeclaredMethod("setY2tlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_weight",	MVPlotJob.class.getDeclaredMethod("setY2labWeight",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_size",	MVPlotJob.class.getDeclaredMethod("setY2labSize",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_offset",	MVPlotJob.class.getDeclaredMethod("setY2labOffset",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_align",	MVPlotJob.class.getDeclaredMethod("setY2labAlign",	new Class[]{String.class}));
			_tableFormatString.put("legend_size",	MVPlotJob.class.getDeclaredMethod("setLegendSize",	new Class[]{String.class}));
			_tableFormatString.put("legend_box",	MVPlotJob.class.getDeclaredMethod("setLegendBox",	new Class[]{String.class}));
			_tableFormatString.put("legend_inset",	MVPlotJob.class.getDeclaredMethod("setLegendInset",	new Class[]{String.class}));
			_tableFormatString.put("legend_ncol",	MVPlotJob.class.getDeclaredMethod("setLegendNcol",	new Class[]{String.class}));
			_tableFormatString.put("box_boxwex",	MVPlotJob.class.getDeclaredMethod("setBoxBoxwex",	new Class[]{String.class}));
			_tableFormatString.put("box_notch",		MVPlotJob.class.getDeclaredMethod("setBoxNotch",	new Class[]{String.class}));
			_tableFormatString.put("ci_alpha",		MVPlotJob.class.getDeclaredMethod("setCIAlpha",		new Class[]{String.class}));
			
			_tableFormatString.put("plot_ci",		MVPlotJob.class.getDeclaredMethod("setPlotCI",		new Class[]{String.class}));
			_tableFormatString.put("colors",		MVPlotJob.class.getDeclaredMethod("setColors",		new Class[]{String.class}));
			_tableFormatString.put("pch",			MVPlotJob.class.getDeclaredMethod("setPch",			new Class[]{String.class}));
			_tableFormatString.put("type",			MVPlotJob.class.getDeclaredMethod("setType",		new Class[]{String.class}));
			_tableFormatString.put("lty",			MVPlotJob.class.getDeclaredMethod("setLty",			new Class[]{String.class}));
			_tableFormatString.put("lwd",			MVPlotJob.class.getDeclaredMethod("setLwd",			new Class[]{String.class}));
			_tableFormatString.put("con_series",	MVPlotJob.class.getDeclaredMethod("setConSeries",	new Class[]{String.class}));
			_tableFormatString.put("legend",		MVPlotJob.class.getDeclaredMethod("setLegend",		new Class[]{String.class}));
			_tableFormatString.put("y1_lim",		MVPlotJob.class.getDeclaredMethod("setY1Lim",		new Class[]{String.class}));
			_tableFormatString.put("y1_bufr",		MVPlotJob.class.getDeclaredMethod("setY1Bufr",		new Class[]{String.class}));
			_tableFormatString.put("y2_lim",		MVPlotJob.class.getDeclaredMethod("setY2Lim",		new Class[]{String.class}));
			_tableFormatString.put("y2_bufr",		MVPlotJob.class.getDeclaredMethod("setY2Bufr",		new Class[]{String.class}));
			_tableFormatString.put("plot_cmd",		MVPlotJob.class.getDeclaredMethod("setPlotCmd",		new Class[]{String.class}));
		}catch(NoSuchMethodException e){}
	}
	
	public static MVOrderedMap buildDepMap(String strDepXML) throws Exception{
		DOMParser parser = new DOMParser();
		parser.parse( new InputSource(new ByteArrayInputStream(strDepXML.getBytes())) );
		Document doc = parser.getDocument();
					
		MVNode nodeDep = new MVNode( doc.getElementsByTagName("dep").item(0) );
		return buildDepMap(nodeDep);
	}
	
	/**
	 * Populate an MVOrderedMap data structure with information specified in the input XML plot
	 * specification &lt;dep&gt; node.  The returned data structure should be added to an MVPlotJob. 
	 * @param nodeDep
	 * @return
	 */
	public static MVOrderedMap buildDepMap(MVNode nodeDep){

		//  <dep>
		MVOrderedMap mapDep = new MVOrderedMap();
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
	
	/**
	 * Build a list of String date representations, specified by the information in the input 
	 * &lt;date_list&gt; node.  The input database connection is used to query for all dates of the
	 * specified field between the start and end date.  
	 * @param nodeDateList XML plot specification node specifying the date list
	 * @param con Database connection to query against
	 * @return List of String representations of specified dates
	 */
	public static String[] parseDateList(MVNode nodeDateList, Connection con){
		String strField = "";
		String strStart = "";
		String strEnd = "";
		String strHour = "";
		
		for(int i=0; i < nodeDateList._children.length; i++){
			MVNode nodeChild = nodeDateList._children[i];
			if     ( nodeChild._tag.equals("field") )	{ strField = nodeChild._name;  }
			else if( nodeChild._tag.equals("hour")  )	{ strHour  = nodeChild._value; }			
			else if( nodeChild._tag.equals("start") )	{ strStart = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0]) : nodeChild._value); }
			else if( nodeChild._tag.equals("end")   )	{ strEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0]) : nodeChild._value); }
		}
		
		return buildDateAggList(con, strField, strStart, strEnd, strHour);
	}

	/**
	 * Form a SQL between clause using the information in the input &lt;date_range&gt; node. 
	 * @param nodeDateRange Contains date range information
	 * @return String containing SQL between clause
	 */
	public static String parseDateRange(MVNode nodeDateRange){
		String strStart = "";
		String strEnd = "";
		String strFormat = _formatDB.toPattern();
		for(int j=0; j < nodeDateRange._children.length; j++){
			MVNode nodeChild = nodeDateRange._children[j]; 
			if     ( nodeChild._tag.equals("start") ) { strStart = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }
			else if( nodeChild._tag.equals("end") )   { strEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat) : nodeChild._value); }
		}
		return "BETWEEN '" + strStart + "' AND '" + strEnd + "'";
	}
	
	/**
	 * Parse the &lt;indep&gt; node of a xml plot specification, returning the parsed information in the
	 * form of two lists.  The first list contains the independent variable values, the second list
	 * contains the labels and the third contains the plot values.
	 * @param node XML plot specification &lt;indep&gt; node
	 * @param dep (optional) String representation of a dependency value date
	 * @return Two lists of independent variable values and labels, respectively
	 */
	public static String[][] parseIndyNode(MVNode node, String dep){
		int intIndyNum = node._children.length;
		ArrayList listIndyVal = new ArrayList();
		ArrayList listIndyLabel = new ArrayList();
		ArrayList listIndyPlotVal = new ArrayList();
		for(int j=0; j < intIndyNum; j++){
			MVNode nodeIndyVal = node._children[j];
			
			//  <val>
			if( nodeIndyVal._tag.equals("val") ){
				listIndyVal.add( nodeIndyVal._value );
				if( !nodeIndyVal._label.equals("") )	{ listIndyLabel.add( nodeIndyVal._label ); }
				else									{ listIndyLabel.add( nodeIndyVal._value ); }
				if( !nodeIndyVal._plotVal.equals("") )  { listIndyPlotVal.add( nodeIndyVal._plotVal ); }
			}
			
			//  <date_list>
			else if( nodeIndyVal._tag.equalsIgnoreCase("date_list") ){
				String strStart = "";
				String strEnd = "";
				int intInc = 0;
				String strFormat = _formatDB.toPattern();
				
				for(int k=0; k < nodeIndyVal._children.length; k++){
					MVNode nodeChild = nodeIndyVal._children[k];
					if     ( nodeChild._tag.equals("start") ) { strStart = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat, dep) : nodeChild._value); }
					else if( nodeChild._tag.equals("end") )   { strEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0], strFormat, dep) : nodeChild._value); }
					else if( nodeChild._tag.equals("inc") )          { intInc = Integer.parseInt(nodeChild._value); }
					else if( nodeChild._tag.equals("label_format") ) { strFormat = nodeChild._value;                }
				}
				
				SimpleDateFormat formatLabel = new SimpleDateFormat(strFormat);
				formatLabel.setTimeZone(TimeZone.getTimeZone("UTC"));
				String[] listDates = buildDateList(strStart, strEnd, intInc, _formatDB.toPattern());
				String[] listLabels = new String[listDates.length];
				for(int k=0; k < listDates.length; k++){
					try{ listLabels[k] = formatLabel.format( _formatDB.parse(listDates[k]) ); }catch(Exception e){}
				}
				
				listIndyVal.addAll( Arrays.asList(listDates) );
				listIndyLabel.addAll( Arrays.asList(listLabels) );
			}
		}		

		return new String[][]{ toArray(listIndyVal), toArray(listIndyLabel), toArray(listIndyPlotVal) };
	}
}

package edu.ucar.metviewer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class MVPlotJobParser extends MVUtil{

	protected Document _doc = null;
	
	protected MVPlotJob[] _listJobs = {};
	protected MVOrderedMap _mapJobs = new MVOrderedMap();
	
	protected Hashtable _tableDateListDecl = new Hashtable();
	protected Hashtable _tableDateRangeDecl = new Hashtable();
	protected Hashtable _tableDateRangeListDecl = new Hashtable();
	protected Hashtable _tablePlotDecl = new Hashtable();
	protected Hashtable _tablePlotNode = new Hashtable();
	protected Hashtable _tableTmplVal = new Hashtable();
	protected MVNode _nodePlotSpec = null;
	
	protected Connection _con = null;
	protected String _strRscript = "Rscript";
	protected String _strRtmplFolder = "";
	protected String _strRworkFolder = "";
	protected String _strPlotsFolder = "";

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
	 * Build a parser whose input source is the plot_spec MVNode
	 * @param node plot_spec MVNode to parse 
	 * @param con Database connection for the plot data
	 */
	public MVPlotJobParser(MVNode node) throws Exception{		
		_nodePlotSpec = node;
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
				/*
				String strName = node._name;				
				String[] listDates = parseDateList(node, _con);
				_tableDateListDecl.put(strName, listDates);
				*/
				_tableDateListDecl.put(node._name, buildDateList(node));
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
				if( _con == null ){ throw new Exception("database connection missing for plot " + node._name); }
				
				//  parse the plot and add it to the job table and, if appropriate, the list of runnable jobs 
				_tablePlotNode.put(node._name, node);
				String strInherits = node._inherits.trim();
				MVPlotJob job = null;
				if( "".equals(strInherits) ){
					job = parsePlotJob(node, null);
				} else {
					String[] listInherits = strInherits.split("\\s*,\\s*");
					if( !_tablePlotDecl.containsKey(listInherits[0]) ){ throw new Exception("inherited plot job " + listInherits[0] + " not found"); }
					MVPlotJob jobBase = (MVPlotJob)_tablePlotDecl.get(listInherits[0]); 
					for(int j=1; j < listInherits.length; j++){
						if( !_tablePlotNode.containsKey(listInherits[j]) ){ throw new Exception("multiple inherited plot job " + listInherits[j] + " not found"); }
						MVNode nodeInherit = (MVNode)_tablePlotNode.get(listInherits[j]);
						jobBase = parsePlotJob(nodeInherit, jobBase);
					}
					job = parsePlotJob(node, jobBase);
				}
				
				//  set the job database information  
				job.setConnection(_con);
				job.setRscript(_strRscript);
				job.setDBHost(strDBHost);
				job.setDBName(strDBName);
				job.setDBUser(strDBUser);
				job.setDBPassword(strDBPassword);
				
				//  add the job to the jobs table and to the runnable jobs, if appropriate
				_tablePlotDecl.put(node._name, job);
				String strCompleteness = "";
				if( job.getPlotTmpl().equals("roc.R_tmpl") ){
					if( !job.getRocPct() && ! job.getRocCtc() ){
						strCompleteness = "if ROC template is selected, one of roc_pct or roc_ctc must be true";
					}
				} else if( !job.getPlotTmpl().equals("rhist.R_tmpl") && !job.getPlotTmpl().equals("rely.R_tmpl") ){
					strCompleteness = checkJobCompleteness(job);
				}
				boolean boolComplete = strCompleteness.equals("");
				boolean boolPlotRun = !node._run.equalsIgnoreCase("false");
				if( boolComplete )	{
					if( boolPlotRun ){ _mapJobs.put(node._name, job); }
					listJobs.add( job );
				} else if( boolPlotRun ){
					throw new Exception("plot " + node._name + ": " + strCompleteness);
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
					ArrayList listFixVal = job.getPlotFixVal().containsKey(nodeFix._name) &&
										   job.getPlotFixVal().get(nodeFix._name) instanceof String[] ? 
											toArrayList( (String[])job.getPlotFixVal().get(nodeFix._name) ) : new ArrayList();
					MVOrderedMap mapFixVal = new MVOrderedMap();
					MVOrderedMap mapTmplVal = new MVOrderedMap();
					for(int k=0; k < nodeFix._children.length; k++){
						MVNode nodeFixVal = nodeFix._children[k];
						
						//  <val>
						if( nodeFixVal._tag.equals("val") ){ listFixVal.add(nodeFixVal._value); }
						
						//  <set>
						else if( nodeFixVal._tag.equals("set") ){
							ArrayList listFixSet = new ArrayList();
							for(int l=0; l < nodeFixVal._children.length; l++){
								MVNode nodeFixSet = nodeFixVal._children[l];
								
								//  <val>
								if( nodeFixSet._tag.equals("val") ){ listFixSet.add(nodeFixSet._value); }
								
								//  <date_list>
								else if( nodeFixSet._tag.equals("date_list") ){
									listFixSet.addAll( Arrays.asList((String[])_tableDateListDecl.get(nodeFixSet._name)) );
									mapTmplVal.put(nodeFix._name, nodeFixSet._name);
								}
								
								//  <date_range>
								else if( nodeFixSet._tag.equals("date_range") ){
									throw new Exception("sets of date_range structures not supported");
								}
	
								//  <date_range_list>
								else if( nodeFixSet._tag.equals("date_range_list") ){
									throw new Exception("sets of date_range_list structures not supported");
								}
							}
							mapFixVal.put(nodeFixVal._name, toArray(listFixSet));							
						}
						
						//  <date_list>
						else if( nodeFixVal._tag.equals("date_list") ){
							listFixVal.addAll( Arrays.asList((String[])_tableDateListDecl.get(nodeFixVal._name)) );
						}
						
						//  <date_range>
						else if( nodeFixVal._tag.equals("date_range") ){
							if( !_tableDateRangeDecl.containsKey(nodeFixVal._name) ){ throw new Exception("date_range " + nodeFixVal._name + " not found in plot_fix"); }
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
				boolean boolDep1Present = false;
				boolean boolDep2Present = false;
				for(int j=0; j < node._children.length; j++){
					MVNode nodeDepN = node._children[j];
					
					//  <clear>
					if( nodeDepN._tag.equals("clear") ){ job.clearDepGroups(); }

					//  <mode_group>
					else if( nodeDepN._tag.equals("mode_group") ){
						throw new Exception("<mode_group> tag no longer supported, use multiple inheritance instead");
					}
					
					//  <dep1> or <dep2>
					else if( nodeDepN._tag.startsWith("dep") ){
						MVOrderedMap mapDepN = new MVOrderedMap();
						
						//  presence accounting
						if     ( nodeDepN._tag.equals("dep1") ){ boolDep1Present = true; }
						else if( nodeDepN._tag.equals("dep2") ){ boolDep2Present = true; }
						
						//  <fcst_var>
						for(int k=0; k < nodeDepN._children.length; k++){
							MVNode nodeFcstVar = nodeDepN._children[k];					
							ArrayList listStats = new ArrayList();
							
							//  <stat>s
							for(int l=0; l < nodeFcstVar._children.length; l++){
								String strStat = nodeFcstVar._children[l]._value;
								if( !isStatValid( strStat ) ){ throw new Exception("unknown stat name " + strStat); }								
								listStats.add(strStat);
							}
							mapDepN.put(nodeFcstVar._name, listStats.toArray(new String[]{}));
						}
						mapDep.put(nodeDepN._tag, mapDepN);
					}
					
					//  <fix>
					else if( nodeDepN._tag.startsWith("fix") ){
						throw new Exception("<dep> child <fix> no longer supported, use <plot_fix> instead");
					}
				}
				
				//  complain if a dep component is missing
				if( !boolDep1Present ){ throw new Exception("plot job dep lacks dep1"); }
				if( !boolDep2Present ){ throw new Exception("plot job dep lacks dep2"); }
				
				//  add the dep group to the job
				job.addDepGroup( mapDep );
			}
			
			//  <agg>
			else if( node._tag.equals("agg") ){				
				throw new Exception("<agg> no longer supported, please change to <plot_fix>");
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
					else if( nodeTmpl._tag.equals("caption") )		{ job.setCaptionTmpl(nodeTmpl._value);	} 
					
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
				throw new Exception("<bootstrapping> tag no longer supported, use <agg_stat> instead");
			}
			
			//  <agg_stat>
			else if( node._tag.equals("agg_stat") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeAggStat = node._children[j];					
					if     ( nodeAggStat._tag.equals("agg_ctc")   )	{ job.setAggCtc(nodeAggStat._value.equalsIgnoreCase("true"));   }
					else if( nodeAggStat._tag.equals("agg_sl1l2") )	{ job.setAggSl1l2(nodeAggStat._value.equalsIgnoreCase("true")); }
					else if( nodeAggStat._tag.equals("agg_pct") )	{ job.setAggPct(nodeAggStat._value.equalsIgnoreCase("true"));   }
					else if( nodeAggStat._tag.equals("agg_nbrcnt") ){ job.setAggNbrCnt(nodeAggStat._value.equalsIgnoreCase("true"));}
					else if( nodeAggStat._tag.equals("boot_repl") )	{ job.setAggBootRepl(nodeAggStat._value);                       }
					else if( nodeAggStat._tag.equals("boot_ci") )	{ job.setAggBootCI(nodeAggStat._value);                         }
					else if( nodeAggStat._tag.equals("agg_diff1") )	{ job.setAggDiff1(nodeAggStat._value.equalsIgnoreCase("true")); }
					else if( nodeAggStat._tag.equals("agg_diff2") )	{ job.setAggDiff2(nodeAggStat._value.equalsIgnoreCase("true")); }
					else if( nodeAggStat._tag.equals("eveq_dis") )	{ job.setEveqDis(nodeAggStat._value.equalsIgnoreCase("true"));  }
				}
				
				if( !job.getAggCtc() && !job.getAggSl1l2() && !job.getAggPct() && !job.getAggNbrCnt() ){ 
					throw new Exception("invalid agg_stat setting - one of the aggregation line types must be selected"); 
				}
			}
			
			//  <calc_stat>
			else if( node._tag.equals("calc_stat") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeCalcStat = node._children[j];					
					if     ( nodeCalcStat._tag.equals("calc_ctc")   )	{ job.setCalcCtc(nodeCalcStat._value.equalsIgnoreCase("true"));   }
					else if( nodeCalcStat._tag.equals("calc_sl1l2") )	{ job.setCalcSl1l2(nodeCalcStat._value.equalsIgnoreCase("true")); }
				}
				
				if( !job.getCalcCtc() && !job.getCalcSl1l2() ){ throw new Exception("invalid calc_stat setting - neither calc_ctc nor calc_sl1l2 are true"); }
				if( job.getCalcCtc() && job.getCalcSl1l2() )  { throw new Exception("invalid calc_stat setting - both calc_ctc and calc_sl1l2 are true"); }
			}
			
			//  <roc_calc>
			else if( node._tag.equals("roc_calc") ){
				for(int j=0; j < node._children.length; j++){
					MVNode nodeRocStat = node._children[j];					
					if     ( nodeRocStat._tag.equals("roc_pct")   )	{ job.setRocPct(nodeRocStat._value.equalsIgnoreCase("true"));   }
					else if( nodeRocStat._tag.equals("roc_ctc") )	{ job.setRocCtc(nodeRocStat._value.equalsIgnoreCase("true")); }
				}
				
				if( !job.getRocPct() && !job.getRocCtc() ){ throw new Exception("invalid roc_calc setting - neither roc_pct nor roc_ctc are true"); }
				if( job.getRocPct()  && job.getRocCtc() ) { throw new Exception("invalid roc_calc setting - both roc_pct and roc_ctc are true"); }
			}
			
			//  boolean format settings
			else if( _tableFormatBoolean.containsKey(node._tag) ){
				Method m = (Method)_tableFormatBoolean.get(node._tag);
				try{
					m.invoke(job, new Object[]{new Boolean(node._value.equals("true"))});
				}catch(Exception e){
					System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format boolean '" + node._tag + "': " + e.getMessage());
				}
			}
			
			//  R string format settings
			else if( _tableFormatString.containsKey(node._tag) ){
				Method m = (Method)_tableFormatString.get(node._tag);
				try{
					m.invoke(job, new Object[]{node._value});
				}catch(Exception e){
					System.out.println("  **  ERROR: caught " + e.getClass() + " parsing format string '" + node._tag + "': " + e.getMessage());
				}
			}
			
			//  report unused tags
			else{
				System.out.println("  **  WARNING: unused plot tag '" + node._tag + "'");
			}
		}
		
		return job;
	}
	
	/**
	 * Determine if the input plot job has many necessary components to build a plot.  If not,
	 * return the structure name that has been found to be missing. 
	 * @param job plot job to inspect
	 * @return name of missing structure, or an empty string if the job is ok
	 */
	public static String checkJobCompleteness(MVPlotJob job){
		if     ( job.getPlotTmpl().equals("")     )	{ return "lacks template";						}
		else if( job.getIndyVar().equals("")      )	{ return "lacks indep";							}
		else if( 1 > job.getIndyVal().length && 
				 null == job.getIndyDep()         )	{ return "lacks indep";							}
		else if( 1 > job.getDepGroups().length    )	{ return "lacks dep";							}
		else if( 1 > job.getSeries1Val().size()   )	{ return "lacks series1";						}
		else if( job.getRFileTmpl().equals("")    )	{ return "lacks r_file";						}
		else if( job.getPlotFileTmpl().equals("") )	{ return "lacks plot_file";						}
		else if( job.getDataFileTmpl().equals("") )	{ return "lacks data_file";						}
		else if( job.getXLabelTmpl().equals("")   )	{ return "lacks x_label";						}
		else if( job.getY1LabelTmpl().equals("")  )	{ return "lacks y1_label";						}
		else if( (job.getAggCtc() || 
				  job.getAggSl1l2()) &&
				 (job.getCalcCtc() ||
				  job.getCalcSl1l2())             )	{ return "has both agg_stat and calc_stat";		}
		
		return "";
	}
	
	public static final Hashtable _tableFormatBoolean = new Hashtable();
	static{
		try{
			_tableFormatBoolean.put("event_equal",	MVPlotJob.class.getDeclaredMethod("setEventEqual",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("event_equal_m",MVPlotJob.class.getDeclaredMethod("setEventEqualM",	new Class[]{boolean.class}));
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
			_tableFormatString.put("xtlab_freq",	MVPlotJob.class.getDeclaredMethod("setXtlabFreq",	new Class[]{String.class}));
			_tableFormatString.put("xtlab_size",	MVPlotJob.class.getDeclaredMethod("setXtlabSize",	new Class[]{String.class}));
			_tableFormatString.put("xlab_weight",	MVPlotJob.class.getDeclaredMethod("setXlabWeight",	new Class[]{String.class}));
			_tableFormatString.put("xlab_size",		MVPlotJob.class.getDeclaredMethod("setXlabSize",	new Class[]{String.class}));
			_tableFormatString.put("xlab_offset",	MVPlotJob.class.getDeclaredMethod("setXlabOffset",	new Class[]{String.class}));
			_tableFormatString.put("xlab_align",	MVPlotJob.class.getDeclaredMethod("setXlabAlign",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_orient",	MVPlotJob.class.getDeclaredMethod("setYtlabOrient",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_perp",	MVPlotJob.class.getDeclaredMethod("setYtlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_horiz",	MVPlotJob.class.getDeclaredMethod("setYtlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("ytlab_size",	MVPlotJob.class.getDeclaredMethod("setYtlabSize",	new Class[]{String.class}));
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
			_tableFormatString.put("x2tlab_size",	MVPlotJob.class.getDeclaredMethod("setX2tlabSize",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_weight",	MVPlotJob.class.getDeclaredMethod("setX2labWeight",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_size",	MVPlotJob.class.getDeclaredMethod("setX2labSize",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_offset",	MVPlotJob.class.getDeclaredMethod("setX2labOffset",	new Class[]{String.class}));
			_tableFormatString.put("x2lab_align",	MVPlotJob.class.getDeclaredMethod("setX2labAlign",	new Class[]{String.class}));
			_tableFormatString.put("y2tlab_orient",	MVPlotJob.class.getDeclaredMethod("setY2tlabOrient",new Class[]{String.class}));
			_tableFormatString.put("y2tlab_perp",	MVPlotJob.class.getDeclaredMethod("setY2tlabPerp",	new Class[]{String.class}));
			_tableFormatString.put("y2tlab_horiz",	MVPlotJob.class.getDeclaredMethod("setY2tlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("y2tlab_size",	MVPlotJob.class.getDeclaredMethod("setY2tlabSize",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_weight",	MVPlotJob.class.getDeclaredMethod("setY2labWeight",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_size",	MVPlotJob.class.getDeclaredMethod("setY2labSize",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_offset",	MVPlotJob.class.getDeclaredMethod("setY2labOffset",	new Class[]{String.class}));
			_tableFormatString.put("y2lab_align",	MVPlotJob.class.getDeclaredMethod("setY2labAlign",	new Class[]{String.class}));
			_tableFormatString.put("legend_size",	MVPlotJob.class.getDeclaredMethod("setLegendSize",	new Class[]{String.class}));
			_tableFormatString.put("legend_box",	MVPlotJob.class.getDeclaredMethod("setLegendBox",	new Class[]{String.class}));
			_tableFormatString.put("legend_inset",	MVPlotJob.class.getDeclaredMethod("setLegendInset",	new Class[]{String.class}));
			_tableFormatString.put("legend_ncol",	MVPlotJob.class.getDeclaredMethod("setLegendNcol",	new Class[]{String.class}));
			_tableFormatString.put("caption_weight",MVPlotJob.class.getDeclaredMethod("setCaptionWeight",new Class[]{String.class}));
			_tableFormatString.put("caption_col",	MVPlotJob.class.getDeclaredMethod("setCaptionCol",	new Class[]{String.class}));
			_tableFormatString.put("caption_size",	MVPlotJob.class.getDeclaredMethod("setCaptionSize",	new Class[]{String.class}));
			_tableFormatString.put("caption_offset",MVPlotJob.class.getDeclaredMethod("setCaptionOffset",new Class[]{String.class}));
			_tableFormatString.put("caption_align",	MVPlotJob.class.getDeclaredMethod("setCaptionAlign",new Class[]{String.class}));
			_tableFormatString.put("box_pts",		MVPlotJob.class.getDeclaredMethod("setBoxPts",		new Class[]{String.class}));
			_tableFormatString.put("box_outline",	MVPlotJob.class.getDeclaredMethod("setBoxOutline",	new Class[]{String.class}));
			_tableFormatString.put("box_boxwex",	MVPlotJob.class.getDeclaredMethod("setBoxBoxwex",	new Class[]{String.class}));
			_tableFormatString.put("box_notch",		MVPlotJob.class.getDeclaredMethod("setBoxNotch",	new Class[]{String.class}));
			_tableFormatString.put("box_avg",		MVPlotJob.class.getDeclaredMethod("setBoxAvg",		new Class[]{String.class}));
			_tableFormatString.put("rely_event_hist",MVPlotJob.class.getDeclaredMethod("setRelyEventHist",new Class[]{String.class}));
			_tableFormatString.put("ci_alpha",		MVPlotJob.class.getDeclaredMethod("setCIAlpha",		new Class[]{String.class}));
			
			_tableFormatString.put("plot_ci",		MVPlotJob.class.getDeclaredMethod("setPlotCI",		new Class[]{String.class}));
			_tableFormatString.put("plot_disp",		MVPlotJob.class.getDeclaredMethod("setPlotDisp",	new Class[]{String.class}));
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
			_tableFormatString.put("plot_cond",		MVPlotJob.class.getDeclaredMethod("setPlotCond",	new Class[]{String.class}));
		}catch(NoSuchMethodException e){}
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
			else if( nodeChild._tag.equals("val")   )	{ strEnd   = (0 < nodeChild._children.length? parseDateOffset(nodeChild._children[0]) : nodeChild._value); }
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

	/**
	 * Determine if the input statistic name is valid by searching the tables of supported statistics.  
	 * @param strStat name of statistic to test for validity
	 * @return true if valid, false otherwise
	 */
	public boolean isStatValid(String strStat){ return !getStatTable(strStat).equals(""); }
	
	public static String serializeJob(MVPlotJob job) throws Exception{
		
    	//  database information
    	String strXML = 
    		"<plot_spec>" +
		 		"<connection>" +
					"<host>" +		job.getDBHost()		+ "</host>" +
					"<database>" +	job.getDBName()		+ "</database>" +
					"<user>" +		job.getDBUser()		+ "</user>" +
					"<password>" +	job.getDBPassword()	+ "</password>" +
		 		"</connection>" +
		 		"<plot>";
    	
    	//  plot template
    	strXML += "<template>" + job.getPlotTmpl() + "</template>";

    	//  if there are dep, series and indep elements present, handle them
    	if( !job.getPlotTmpl().startsWith("rhist") && !job.getPlotTmpl().startsWith("roc")  && 
    		!job.getPlotTmpl().startsWith("rely") ){
    	
	    	// dep
	    	strXML += "<dep>";
	    	MVOrderedMap[] listDepGroup = job.getDepGroups();    	
			for(int intY=1; intY <= 2; intY++){
				
				//  get the list of fcst_var for the current dep
				MVOrderedMap mapDep = (MVOrderedMap)listDepGroup[0].get("dep" + intY);
				Map.Entry[] listDep = mapDep.getOrderedEntries();
				
				//  serialize the dep and it's fcst_var stats
				String strDep = "dep" + intY;
				strXML += "<" + strDep + ">";
				for(int i=0; i < listDep.length; i++){
					String[] listStat = (String[])listDep[i].getValue();
					strXML += "<fcst_var name=\"" + listDep[i].getKey().toString() + "\">";
					for(int j=0; j < listStat.length; j++){ strXML += "<stat>" + listStat[j] + "</stat>"; }
					strXML += "</fcst_var>";
				}
				strXML += "</" + strDep + ">";
			}
	    	strXML += "</dep>";
	    	
	    	//  series
	    	for(int intY=1; intY <= 2; intY++){
	    		
	    		//  get the series for the current y-axis
	    		MVOrderedMap mapSeries = (1 == intY? job.getSeries1Val() : job.getSeries2Val());
	    		strXML += "<series" + intY + ">";
	    		
	    		//  serialize each fcst_var and it's vals
	    		String[] listSeriesField = mapSeries.getKeyList();
	    		for(int i=0; i < listSeriesField.length; i++){
	    			strXML += "<field name=\"" + listSeriesField[i] + "\">";
	    			String[] listSeriesVal = (String[])mapSeries.get(listSeriesField[i]);
	    			for(int j=0; j < listSeriesVal.length; j++){ strXML += "<val>" + listSeriesVal[j] + "</val>"; }    			
	    			strXML += "</field>";
	    		}
	    		strXML += "</series" + intY + ">";    		
	    	}
	    	
	    	//  indep
	    	strXML += "<indep name=\"" + job.getIndyVar() + "\">";
	    	String[] listIndyVal = job.getIndyVal();
	    	String[] listIndyPlotVal = job.getIndyPlotVal();
	    	String[] listIndyLabel = job.getIndyLabel();
	    	for(int i=0; i < listIndyVal.length; i++){
	    		String strIndyPlotVal = (0 < listIndyPlotVal.length? listIndyPlotVal[i] : "");
	    		strXML +=
	    			"<val label=\"" + listIndyLabel[i] + "\" plot_val=\"" + strIndyPlotVal + "\">" +
	    				listIndyVal[i] + 
	    			"</val>";
	    	}
	    	strXML += "</indep>";
    	}
    	
    	//  plot_fix
		MVOrderedMap mapPlotFix = job.getPlotFixVal();
		strXML += "<plot_fix>";
		String[] listFixField = mapPlotFix.getKeyList();
		for(int i=0; i < listFixField.length; i++){
			strXML += "<field name=\"" + listFixField[i] + "\">";
			Object objFixVal = mapPlotFix.get(listFixField[i]);
			if( objFixVal instanceof String[] ){
				String[] listFixVal = (String[])objFixVal;
				for(int j=0; j < listFixVal.length; j++){ strXML += "<val>" + listFixVal[j] + "</val>"; }    			
			} else if( objFixVal instanceof MVOrderedMap ){
				MVOrderedMap mapFixSet = (MVOrderedMap)objFixVal;
				String[] listFixSetKey = mapFixSet.getKeyList();
				for(int j=0; j < listFixSetKey.length; j++){
					String[] listFixSetVal = (String[])mapFixSet.get( listFixSetKey[j] );
					strXML += "<set name=\"" + listFixSetKey[j] + "\">";
					for(int k=0; k < listFixSetVal.length; k++){ strXML += "<val>" + listFixSetVal[k] + "</val>"; }
					strXML += "</set>";
				}
			}
			strXML += "</field>";
		}
		strXML += "</plot_fix>";
		
		//  agg_stat
		if( job.getAggCtc() || job.getAggSl1l2() || job.getAggPct() || job.getAggNbrCnt() ){
			strXML +=
				"<agg_stat>" +
					"<agg_ctc>" +	(job.getAggCtc()?   "TRUE" : "FALSE") + "</agg_ctc>" +
					"<agg_sl1l2>" +	(job.getAggSl1l2()? "TRUE" : "FALSE") + "</agg_sl1l2>" +
					"<agg_pct>" +	(job.getAggPct()? 	"TRUE" : "FALSE") + "</agg_pct>" +
					"<agg_nbrcnt>" +(job.getAggNbrCnt()?"TRUE" : "FALSE") + "</agg_nbrcnt>" +
					"<boot_repl>" +	job.getAggBootRepl() +	"</boot_repl>" +
					"<boot_ci>" +	job.getAggBootCI() +	"</boot_ci>" +
					"<agg_diff1>" +	(job.getAggDiff1()? "TRUE" : "FALSE") + "</agg_diff1>" +
					"<agg_diff2>" +	(job.getAggDiff2()? "TRUE" : "FALSE") + "</agg_diff2>" +
					"<eveq_dis>" +	(job.getEveqDis()?  "TRUE" : "FALSE") + "</eveq_dis>" +
				"</agg_stat>";			
		}
    	
		//  calc_stat
		if( job.getCalcCtc() || job.getCalcSl1l2() ){
			strXML +=
				"<calc_stat>" +
					"<calc_ctc>" +	(job.getCalcCtc()?    "TRUE" : "FALSE") + "</calc_ctc>" +
					"<calc_sl1l2>"+ (job.getCalcSl1l2() ? "TRUE" : "FALSE") + "</calc_sl1l2>" +
				"</calc_stat>";			
		}
		
		//  roc_calc
		if( job.getPlotTmpl().equals("roc.R_tmpl") ){
			strXML +=
				"<roc_calc>" +
					"<roc_pct>" + (job.getRocPct()?    "TRUE" : "FALSE") + "</roc_pct>" +
					"<roc_ctc>" + (job.getRocCtc() ?   "TRUE" : "FALSE") + "</roc_ctc>" +
				"</roc_calc>";			
		}
		
		//  tmpl
		strXML +=
			"<tmpl>" +
	            "<title>" +		job.getTitleTmpl() +	"</title>" +
	            "<x_label>" +	job.getXLabelTmpl() +	"</x_label>" +
	            "<y1_label>" +	job.getY1LabelTmpl() +	"</y1_label>" +
	            "<y2_label>" +	job.getY2LabelTmpl() +	"</y2_label>" +
	            "<caption>" +	job.getCaptionTmpl() +	"</caption>" +
	        "</tmpl>";
		
		//  plot_cmd / plot_cond
		strXML += 
			"<plot_cmd>" + 		job.getPlotCmd() 		+ "</plot_cmd>" +
			"<plot_cond>" + 	job.getPlotCond() 		+ "</plot_cond>";
		
		//  plot fmt
		strXML += 
	        "<event_equal>" +	job.getEventEqual()		+ "</event_equal>" +
	        "<event_equal_m>" +	job.getEventEqualM()	+ "</event_equal_m>" +
	        "<vert_plot>" +		job.getVertPlot()		+ "</vert_plot>" +
	        "<x_reverse>" +		job.getXReverse()		+ "</x_reverse>" +
	        "<plot1_diff>" +	job.getPlot1Diff()		+ "</plot1_diff>" +
	        "<plot2_diff>" +	job.getPlot2Diff()		+ "</plot2_diff>" +
	        "<num_stats>" +		job.getShowNStats()		+ "</num_stats>" +
	        "<indy1_stag>" +	job.getIndy1Stagger()	+ "</indy1_stag>" +
	        "<indy2_stag>" +	job.getIndy2Stagger()	+ "</indy2_stag>" +
	        "<grid_on>" +		job.getGridOn()			+ "</grid_on>" +
	        "<sync_axes>" +		job.getSyncAxes()		+ "</sync_axes>" +
	        "<dump_points1>" +	job.getDumpPoints1()	+ "</dump_points1>" +
	        "<dump_points2>" +	job.getDumpPoints2()	+ "</dump_points2>" +
	        "<log_y1>" +		job.getLogY1()			+ "</log_y1>" +
	        "<log_y2>" +		job.getLogY2()			+ "</log_y2>" +
	        "<plot_type>" +		job.getPlotType()		+ "</plot_type>" +
	        "<plot_height>" +	job.getPlotHeight()		+ "</plot_height>" +
	        "<plot_width>" +	job.getPlotWidth()		+ "</plot_width>" +
	        "<plot_res>" +		job.getPlotRes()		+ "</plot_res>" +
	        "<plot_units>" +	job.getPlotUnits()		+ "</plot_units>" +
	        "<mar>" +			job.getMar()			+ "</mar>" +
	        "<mgp>" +			job.getMgp()			+ "</mgp>" +
	        "<cex>" +			job.getCex()			+ "</cex>" +
	        "<title_weight>" +	job.getTitleWeight()	+ "</title_weight>" +
	        "<title_size>" +	job.getTitleSize()		+ "</title_size>" +
	        "<title_offset>" +	job.getTitleOffset()	+ "</title_offset>" +
	        "<title_align>" +	job.getTitleAlign()		+ "</title_align>" +
	        "<xtlab_orient>" +	job.getXtlabOrient()	+ "</xtlab_orient>" +
	        "<xtlab_perp>" +	job.getXtlabPerp()		+ "</xtlab_perp>" +
	        "<xtlab_horiz>" +	job.getXtlabHoriz()		+ "</xtlab_horiz>" +
	        "<xtlab_freq>" +	job.getXtlabFreq()		+ "</xtlab_freq>" +
	        "<xlab_weight>" +	job.getXlabWeight()		+ "</xlab_weight>" +
	        "<xlab_size>" +		job.getXlabSize()		+ "</xlab_size>" +
	        "<xlab_offset>" +	job.getXlabOffset()		+ "</xlab_offset>" +
	        "<xlab_align>" +	job.getYlabAlign()		+ "</xlab_align>" +
	        "<ytlab_orient>" +	job.getYtlabOrient()	+ "</ytlab_orient>" +
	        "<ytlab_perp>" +	job.getYtlabPerp()		+ "</ytlab_perp>" +
	        "<ytlab_horiz>" +	job.getYtlabHoriz()		+ "</ytlab_horiz>" +
	        "<ylab_weight>" +	job.getYlabWeight()		+ "</ylab_weight>" +
	        "<ylab_size>" +		job.getYlabSize()		+ "</ylab_size>" +
	        "<ylab_offset>" +	job.getYlabOffset()		+ "</ylab_offset>" +
	        "<ylab_align>" +	job.getYlabAlign()		+ "</ylab_align>" +
	        "<grid_lty>" +		job.getGridLty()		+ "</grid_lty>" +
	        "<grid_col>" +		job.getGridCol()		+ "</grid_col>" +
	        "<grid_lwd>" +		job.getGridLwd()		+ "</grid_lwd>" +
	        "<grid_x>" +		job.getGridX()			+ "</grid_x>" +
	        "<x2tlab_orient>" +	job.getX2tlabOrient()	+ "</x2tlab_orient>" +
	        "<x2tlab_perp>" +	job.getX2tlabPerp()		+ "</x2tlab_perp>" +
	        "<x2tlab_horiz>" +	job.getX2tlabHoriz()	+ "</x2tlab_horiz>" +
	        "<x2lab_weight>" +	job.getX2labWeight()	+ "</x2lab_weight>" +
	        "<x2lab_size>" +	job.getX2labSize()		+ "</x2lab_size>" +
	        "<x2lab_offset>" +	job.getX2labOffset()	+ "</x2lab_offset>" +
	        "<x2lab_align>" +	job.getX2labAlign()		+ "</x2lab_align>" +
	        "<y2tlab_orient>" +	job.getY2tlabOrient()	+ "</y2tlab_orient>" +
	        "<y2tlab_perp>" +	job.getY2tlabPerp()		+ "</y2tlab_perp>" +
	        "<y2tlab_horiz>" +	job.getY2tlabHoriz()	+ "</y2tlab_horiz>" +
	        "<y2lab_weight>" +	job.getY2labWeight()	+ "</y2lab_weight>" +
	        "<y2lab_size>" +	job.getY2labSize()		+ "</y2lab_size>" +
	        "<y2lab_offset>" +	job.getY2labOffset()	+ "</y2lab_offset>" +
	        "<y2lab_align>" +	job.getY2labAlign()		+ "</y2lab_align>" +
	        "<legend_size>" +	job.getLegendSize()		+ "</legend_size>" +
	        "<legend_box>" +	job.getLegendBox()		+ "</legend_box>" +
	        "<legend_inset>" +	job.getLegendInset()	+ "</legend_inset>" +
	        "<legend_ncol>" +	job.getLegendNcol()		+ "</legend_ncol>" +
	        "<caption_weight>"+	job.getCaptionWeight()	+ "</caption_weight>" +
	        "<caption_col>" +	job.getCaptionCol()		+ "</caption_col>" +
	        "<caption_size>" +	job.getCaptionSize()	+ "</caption_size>" +
	        "<caption_offset>"+	job.getCaptionOffset()	+ "</caption_offset>" +
	        "<caption_align>" +	job.getCaptionAlign()	+ "</caption_align>" +
	        "<box_pts>" +		job.getBoxPts()			+ "</box_pts>" +
	        "<box_outline>" +	job.getBoxOutline()		+ "</box_outline>" +
	        "<box_boxwex>" +	job.getBoxBoxwex()		+ "</box_boxwex>" +
	        "<box_notch>" +		job.getBoxNotch()		+ "</box_notch>" +
	        "<box_avg>" +		job.getBoxAvg()			+ "</box_avg>" +
	        "<rely_event_hist>"+job.getRelyEventHist()	+ "</rely_event_hist>" +
	        "<ci_alpha>" +		job.getCIAlpha()		+ "</ci_alpha>" +
	        
	        "<plot_ci>" +		job.getPlotCI() +		"</plot_ci>" +
	        "<plot_disp>" +		job.getPlotDisp() +		"</plot_disp>" +
	        "<colors>" +		job.getColors() +		"</colors>" +
	        "<pch>" +			job.getPch() +			"</pch>" +
	        "<type>" +			job.getType() +			"</type>" +
	        "<lty>" +			job.getLty() +			"</lty>" +
	        "<lwd>" +			job.getLwd() +			"</lwd>" +
	        "<con_series>" +	job.getConSeries() +	"</con_series>" +
			"<legend>" +		job.getLegend() +		"</legend>" +
			
			"<y1_lim>" +		job.getY1Lim() +		"</y1_lim>" +
			"<y1_bufr>" +		job.getY1Bufr() +		"</y1_bufr>" +
			"<y2_lim>" +		job.getY2Lim() +		"</y2_lim>" +
			"<y2_bufr>" +		job.getY2Bufr() +		"</y2_bufr>";
		
    	//  close the plot job
    	strXML += "</plot></plot_spec>";
		return strXML;
	}
}

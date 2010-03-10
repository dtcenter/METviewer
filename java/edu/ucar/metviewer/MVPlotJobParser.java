package edu.ucar.metviewer;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;

public class MVPlotJobParser extends MVUtil{
	
	protected Hashtable _tableSpecDecl = new Hashtable();
	protected Hashtable _tablePlotDecl = new Hashtable();
	protected MVNode _nodePlotSpec = null;
	
	protected Connection _con = null;
	protected String _strRtmplFolder = "";
	protected String _strRworkFolder = "";
	protected String _strPlotsFolder = "";
	
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
	
	public MVPlotJobParser(String spec, Connection con) throws Exception{		
		_con = con;
		
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
		_nodePlotSpec = new MVNode(doc.getFirstChild());
	}
	
	public String getRtmplFolder(){ return _strRtmplFolder; }
	public String getRworkFolder(){ return _strRworkFolder; }
	public String getPlotsFolder(){ return _strPlotsFolder; }
	
	public MVPlotJob[] parsePlotJobSpec(){
		ArrayList listJobs = new ArrayList();
		
		for(int i=0; null != _nodePlotSpec && i < _nodePlotSpec._children.length; i++){
			MVNode node = _nodePlotSpec._children[i];

			//  <connection>
			if( node._tag.equals("connection") ){
				String strHost = "";
				String strDatabase = "";
				String strUser = "";
				String strPassword ="";
				
				for(int j=0; j < node._children.length; j++){
					if     ( node._children[j]._tag.equals("host") )	{ strHost		= node._children[j]._value; }
					else if( node._children[j]._tag.equals("database") ){ strDatabase	= node._children[j]._value; }
					else if( node._children[j]._tag.equals("user") )	{ strUser		= node._children[j]._value; }
					else if( node._children[j]._tag.equals("password") ){ strPassword	= node._children[j]._value; }
				}
				
				try {
					//  connect to the database
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection con = DriverManager.getConnection("jdbc:mysql://" + strHost + "/" + strDatabase, strUser, strPassword);
					if( con.isClosed() )	throw new Exception("database connection failed");
					_con = con;
				} catch(Exception ex){
					System.out.println("  **  ERROR: parsePlotJob() caught " + ex.getClass() + " connecting to database: " + ex.getMessage());
				}
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
				_tableSpecDecl.put(strName, listDates);
			}

			//  <plot>
			else if( node._tag.equals("plot") ){

				//  make sure the database connection has been established
				if( _con == null ){
					System.out.println("  **  ERROR: database connection missing for plot " + node._name);
					return new MVPlotJob[]{};
				}
				
				//  parse the plot and add it to the job table and, if appropriate, the list of runnable jobs 
				String strInherits = node._inherits;
				MVPlotJob jobBase = ( !strInherits.equals("") ? (MVPlotJob)_tablePlotDecl.get(strInherits) : null);
				MVPlotJob job = parsePlotJob(node, jobBase);
				job.setConnection(_con);
				_tablePlotDecl.put(node._name, job);
				if( checkJobCompleteness(job) )	{ listJobs.add( job ); }
				//else							{ System.out.println("  **  WARNING: incomplete job " + node._name); }
			}
		}
		
		return (MVPlotJob[])listJobs.toArray(new MVPlotJob[]{});
	}
	
	public MVPlotJob parsePlotJob(MVNode nodePlot, MVPlotJob jobBase){
		MVPlotJob job = (null != jobBase? jobBase.copy() : new MVPlotJob());

		for(int i=0; i < nodePlot._children.length; i++){
			MVNode node = nodePlot._children[i];
			
			//  <template>
			if( node._tag.equals("template") ){
				job.setPlotTmpl(node._value);
			}
			
			//  <indep>
			else if( node._tag.equals("indep") ){
				int intIndyNum = node._children.length;
				String[] listIndyVal = new String[intIndyNum];
				String[] listIndyLabel = new String[intIndyNum];
				for(int j=0; j < intIndyNum; j++){
					MVNode nodeIndyVal = node._children[j];
					listIndyVal[j] = nodeIndyVal._value;
					if( !nodeIndyVal._label.equals("") )	{ listIndyLabel[j] = nodeIndyVal._label; }
					else									{ listIndyLabel[j] = nodeIndyVal._value; }
				}
				job.setIndyVar(node._name);
				job.setIndyVal(listIndyVal);
				job.setIndyLabel(listIndyLabel);
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
					
					//  <dep1> or <dep2>
					else if( nodeDepN._tag.startsWith("dep") ){
						MVOrderedMap mapDepN = new MVOrderedMap();
						
						//  <fcst_var>
						for(int k=0; k < nodeDepN._children.length; k++){
							MVNode nodeFcstVar = nodeDepN._children[k];					
							ArrayList listStats = new ArrayList();
							
							//  <stat>s
							for(int l=0; l < nodeFcstVar._children.length; l++){
								listStats.add(nodeFcstVar._children[l]._value);
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
								mapFcstVar.put(nodeFcstVar._children[l]._name, nodeFcstVar._children[l]._value);
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
					String[] listAggVal = new String[nodeAgg._children.length];
					MVOrderedMap mapAggVal = new MVOrderedMap();
					for(int k=0; k < nodeAgg._children.length; k++){
						MVNode nodeChild = nodeAgg._children[k];
						
						//  <val>
						if( nodeChild._tag.equals("val") ){ listAggVal[k] = nodeChild._value; }
						
						//  <set>
						else if( nodeChild._tag.equals("set") ){
							String[] listAggSet = new String[nodeChild._children.length];
							for(int l=0; l < nodeChild._children.length; l++){ listAggSet[l] = nodeChild._children[l]._value; }
							mapAggVal.put(nodeChild._name, listAggSet);
						}
						
						//  <date_list>
						else if( nodeChild._tag.equals("date_list") ){
							listAggVal = (String[])_tableSpecDecl.get(nodeChild._name);							
						}
					}
					job.addAggVal(nodeAgg._name, listAggVal);
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
							MVNode nodeVal = nodeTmpl._children[k]._children[1];
							mapValMap.put(nodeKey._value, nodeVal._value);
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
	
	public static boolean checkJobCompleteness(MVPlotJob job){
		if     ( job.getPlotTmpl().equals("")     )	{ return false; }
		else if( job.getIndyVar().equals("")      )	{ return false; }
		else if( 1 > job.getIndyVal().length      )	{ return false; }
		else if( 1 > job.getDepGroups().length    )	{ return false; }
		else if( 1 > job.getSeries1Val().size()   )	{ return false; }
		else if( 1 > job.getSeries2Val().size()   )	{ return false; }
		else if( 1 > job.getAggVal().size()       )	{ return false; }
		else if( job.getRFileTmpl().equals("")    )	{ return false; }
		else if( job.getPlotFileTmpl().equals("") )	{ return false; }
		else if( job.getDataFileTmpl().equals("") )	{ return false; }
		else if( job.getXLabelTmpl().equals("")   )	{ return false; }
		else if( job.getY1LabelTmpl().equals("")  )	{ return false; }
		else if( job.getY2LabelTmpl().equals("")  )	{ return false; }
		
		return true;
	}
	
	public static final Hashtable _tableFormatBoolean = new Hashtable();
	static{
		try{
			_tableFormatBoolean.put("event_equal",	MVPlotJob.class.getDeclaredMethod("setEventEqual",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("plot1_diff",	MVPlotJob.class.getDeclaredMethod("setPlot1Diff",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("plot2_diff",	MVPlotJob.class.getDeclaredMethod("setPlot2Diff",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("num_stats",	MVPlotJob.class.getDeclaredMethod("setShowNStats",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("indy_stag",	MVPlotJob.class.getDeclaredMethod("setIndyStagger",	new Class[]{boolean.class}));
			_tableFormatBoolean.put("grid_on",		MVPlotJob.class.getDeclaredMethod("setGridOn",		new Class[]{boolean.class}));
			_tableFormatBoolean.put("sync_axes",	MVPlotJob.class.getDeclaredMethod("setSyncAxes",	new Class[]{boolean.class}));
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
			_tableFormatString.put("ytalb_horiz",	MVPlotJob.class.getDeclaredMethod("setYtlabHoriz",	new Class[]{String.class}));
			_tableFormatString.put("tlab_weight",	MVPlotJob.class.getDeclaredMethod("setYlabWeight",	new Class[]{String.class}));
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
			_tableFormatString.put("yt2lab_perp",	MVPlotJob.class.getDeclaredMethod("setY2tlabPerp",	new Class[]{String.class}));
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
			_tableFormatString.put("y1_lim",		MVPlotJob.class.getDeclaredMethod("setY1Lim",		new Class[]{String.class}));
			_tableFormatString.put("y2_lim",		MVPlotJob.class.getDeclaredMethod("setY2Lim",		new Class[]{String.class}));
			_tableFormatString.put("plot_cmd",		MVPlotJob.class.getDeclaredMethod("setPlotCmd",		new Class[]{String.class}));
		}catch(NoSuchMethodException e){}
	}
	
	public static String[] parseDateList(MVNode nodeDateList, Connection con){
		String strField = "";
		String strStart = "";
		String strEnd = "";
		String strHour = "";
		
		for(int i=0; i < nodeDateList._children.length; i++){
			MVNode nodeChild = nodeDateList._children[i];
			if     ( nodeChild._tag.equals("field") )	{ strField = nodeChild._name;  }
			else if( nodeChild._tag.equals("start") )	{ strStart = nodeChild._value; }
			else if( nodeChild._tag.equals("end")   )	{ strEnd = nodeChild._value;   }
			else if( nodeChild._tag.equals("hour")  )	{ strHour = nodeChild._value;  }			
		}
		
		return buildDateAggList(con, strField, strStart, strEnd, strHour);
	}
	public String[] parseDateList(MVNode nodeDateList){ return parseDateList(nodeDateList, _con); }
	
	public static MVOrderedMap buildDepMap(String strDepXML) throws Exception{
		DOMParser parser = new DOMParser();
		parser.parse( new InputSource(new ByteArrayInputStream(strDepXML.getBytes())) );
		Document doc = parser.getDocument();
					
		MVNode nodeDep = new MVNode( doc.getElementsByTagName("dep").item(0) );
		return buildDepMap(nodeDep);
	}
	
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
		_children = (0 < listChildren.size()? (MVNode[])listChildren.toArray(new MVNode[]{}) : null);
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



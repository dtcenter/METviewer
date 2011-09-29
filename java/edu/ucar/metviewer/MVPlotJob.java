package edu.ucar.metviewer;

import java.util.*;
import java.sql.Connection;

/**
 * Storage class for a xml plot specification, implementing the java bean interface.  Instances are populated
 * by the MVPlotJobParser and handled by MVBatch.     
 */
public class MVPlotJob extends MVUtil{

	protected String _strJobName			= "";
	
	protected Connection _con				= null;
	protected String _strDBHost				= "";
	protected String _strDBName				= "";
	protected String _strDBUser				= "";
	protected String _strDBPassword			= "";
	
	protected String _strRscript			= "";

	protected String _strPlotTmpl			= "";
	
	protected String _strIndyVar			= "";
	protected String[] _listIndyVal			= {};
	protected String[] _listIndyPlotVal		= {};
	protected String[] _listIndyLabel		= {};
	protected MVPlotDep _depIndy			= null;
	
	protected MVOrderedMap _mapPlotFixVal	= new MVOrderedMap();
	
	protected MVOrderedMap[] _listDepGroup	= {};
	
	protected MVOrderedMap _mapSeries1Val	= new MVOrderedMap();
	protected MVOrderedMap _mapSeries2Val	= new MVOrderedMap();
	
	protected MVOrderedMap _mapSeriesNobs	= new MVOrderedMap();
	
	protected MVOrderedMap _mapDep1Scale	= new MVOrderedMap();
	protected MVOrderedMap _mapDep2Scale	= new MVOrderedMap();

	protected MVOrderedMap _mapTmplMaps		= new MVOrderedMap();
	protected MVOrderedMap _mapTmplVal		= new MVOrderedMap();
	
	protected String _strDataFileTmpl		= "";
	protected String _strPlotFileTmpl		= "";
	protected String _strRFileTmpl			= "";
	protected String _strTitleTmpl			= "";
	protected String _strXLabelTmpl			= "";
	protected String _strY1LabelTmpl		= "";
	protected String _strY2LabelTmpl		= "";
	protected String _strCaptionTmpl		= "";
	protected String _strPlotCmd			= "";
	
	protected boolean _boolEventEqual		= false;
	protected boolean _boolEventEqualM		= false;
	protected boolean _boolVertPlot			= false;
	protected boolean _boolXReverse			= false;
	protected boolean _boolPlot1Diff		= false;
	protected boolean _boolPlot2Diff		= false;
	protected boolean _boolShowNStats		= false;
	protected boolean _boolIndy1Stagger		= false;
	protected boolean _boolIndy2Stagger		= false;
	protected boolean _boolGridOn			= false;
	protected boolean _boolSyncAxes			= false;
	protected boolean _boolDumpPoints1		= false;
	protected boolean _boolDumpPoints2		= false;
	protected boolean _boolLogY1			= false;
	protected boolean _boolLogY2			= false;
	
	protected String _strPlotType			= "png256";
	protected String _strPlotHeight			= "8.5";
	protected String _strPlotWidth			= "11";
	protected String _strPlotRes			= "72";
	protected String _strPlotUnits			= "in";
	protected String _strMar				= "c(8, 4, 5, 4)";
	protected String _strMgp				= "c(1, 1, 0)";
	protected String _strCex				= "1";
	protected String _strTitleWeight		= "2";
	protected String _strTitleSize			= "1";
	protected String _strTitleOffset		= "-2.5";
	protected String _strTitleAlign			= ".5";
	protected String _strXtlabOrient		= "1";
	protected String _strXtlabPerp			= "-.75";
	protected String _strXtlabHoriz			= ".5";
	protected String _strXtlabFreq			= "0";
	protected String _strXtlabSize			= "1";
	protected String _strXlabWeight			= "1";
	protected String _strXlabSize			= "1";
	protected String _strXlabOffset			= "2";
	protected String _strXlabAlign			= ".5";
	protected String _strYtlabOrient		= "1";
	protected String _strYtlabPerp			= ".5";
	protected String _strYtlabHoriz			= ".5";
	protected String _strYtlabSize			= "1";
	protected String _strYlabWeight			= "1";
	protected String _strYlabSize			= "1";
	protected String _strYlabOffset			= "-2";
	protected String _strYlabAlign			= ".5";
	protected String _strGridLty			= "3";
	protected String _strGridCol			= "#CCCCCC";
	protected String _strGridLwd			= "1";
	protected String _strGridX				= "listX";
	protected String _strX2tlabOrient		= "1";
	protected String _strX2tlabPerp			= "1";
	protected String _strX2tlabHoriz		= ".5";
	protected String _strX2tlabSize			= ".8";
	protected String _strX2labWeight		= "1";
	protected String _strX2labSize			= ".8";
	protected String _strX2labOffset		= "-.5";
	protected String _strX2labAlign			= ".5";
	protected String _strY2tlabOrient		= "1";
	protected String _strY2tlabPerp			= ".5";
	protected String _strY2tlabHoriz		= ".5";
	protected String _strY2tlabSize			= "1";
	protected String _strY2labWeight		= "1";
	protected String _strY2labSize			= "1";
	protected String _strY2labOffset		= "1";
	protected String _strY2labAlign			= ".5";
	protected String _strLegendSize			= ".8";
	protected String _strLegendBox			= "o";
	protected String _strLegendInset		= "c(0, -.25)";
	protected String _strLegendNcol			= "3";
	protected String _strCaptionWeight		= "1";
	protected String _strCaptionCol			= "#999999";
	protected String _strCaptionSize		= ".8";
	protected String _strCaptionOffset		= "3";
	protected String _strCaptionAlign		= ".5";	
	protected String _strBoxPts				= "FALSE";
	protected String _strBoxBoxwex			= ".2";
	protected String _strBoxOutline			= "TRUE";
	protected String _strBoxNotch			= "FALSE";
	protected String _strBoxAvg				= "FALSE";
	protected String _strRelyEventHist		= "TRUE";
	protected String _strCIAlpha			= ".05";
	
	protected boolean _boolAggCtc			= false;
	protected boolean _boolAggSl1l2			= false;
	protected boolean _boolAggPct			= false;
	protected String _strAggBootRepl		= "1";
	protected String _strAggBootCI			= "bca";
	protected boolean _strAggDiff1			= false;
	protected boolean _strAggDiff2			= false;
	
	protected boolean _boolCalcCtc			= false;
	protected boolean _boolCalcSl1l2		= false;

	protected boolean _boolRocPct			= false;
	protected boolean _boolRocCtc			= false;
	
	protected String _strPlotCI				= "";
	protected String _strPlotDisp			= "";
	protected String _strColors				= "";
	protected String _strPch				= "";
	protected String _strType				= "";
	protected String _strLty				= "";
	protected String _strLwd				= "";
	protected String _strConSeries			= "";
	protected String _strLegend				= "";
	protected String _strY1Lim				= "";
	protected String _strY1Bufr				= ".04";
	protected String _strY2Lim				= "";
	protected String _strY2Bufr				= ".04";
	
	public static MVPlotJob		getBaseJob(Connection con)				throws Exception{ return new MVPlotJob();   }
	public static MVPlotJob[]	getJobs(Connection con, MVPlotJob base)	throws Exception{ return new MVPlotJob[]{}; }
	public static MVPlotJob[]	getJobs(Connection con)					throws Exception{ return new MVPlotJob[]{}; }
	
	/**
	 * Deep copy of the MVPlotJob, useful for inheritance.
	 * @return Copy of this MVPlotJob
	 */
	public MVPlotJob copy(){
		MVPlotJob job = new MVPlotJob();
		
		job._strJobName			= _strJobName + "_copy";
		job._con				= _con;
		job._strDBHost			= _strDBHost;
		job._strDBName			= _strDBName;
		job._strDBUser			= _strDBUser;
		job._strDBPassword		= _strDBPassword;
		job._strRscript			= _strRscript;
		job._strPlotTmpl		= _strPlotTmpl;
		job._strIndyVar			= _strIndyVar;
		job._listIndyVal		= copyList(_listIndyVal);
		job._listIndyPlotVal	= copyList(_listIndyPlotVal);
		job._listIndyLabel		= copyList(_listIndyLabel);
		job._depIndy			= _depIndy;
		job._mapPlotFixVal		= new MVOrderedMap(_mapPlotFixVal);
		job._listDepGroup		= copyList(_listDepGroup);
		job._mapSeries1Val		= new MVOrderedMap(_mapSeries1Val);
		job._mapSeries2Val		= new MVOrderedMap(_mapSeries2Val);
		job._mapSeriesNobs		= new MVOrderedMap(_mapSeriesNobs);
		job._mapDep1Scale		= new MVOrderedMap(_mapDep1Scale);
		job._mapDep2Scale		= new MVOrderedMap(_mapDep2Scale);
		job._mapTmplMaps		= new MVOrderedMap(_mapTmplMaps);
		job._mapTmplVal			= new MVOrderedMap(_mapTmplVal);
		
		job._strDataFileTmpl	= _strDataFileTmpl;
		job._strPlotFileTmpl	= _strPlotFileTmpl;
		job._strRFileTmpl		= _strRFileTmpl;
		job._strTitleTmpl		= _strTitleTmpl;
		job._strXLabelTmpl		= _strXLabelTmpl;
		job._strY1LabelTmpl		= _strY1LabelTmpl;
		job._strY2LabelTmpl		= _strY2LabelTmpl;
		job._strCaptionTmpl		= _strCaptionTmpl;
		job._strPlotCmd			= _strPlotCmd;
		
		job._boolEventEqual		= _boolEventEqual;
		job._boolEventEqualM	= _boolEventEqualM;
		job._boolVertPlot		= _boolVertPlot;
		job._boolXReverse		= _boolXReverse;
		job._boolPlot1Diff		= _boolPlot1Diff;
		job._boolPlot2Diff		= _boolPlot2Diff;
		job._boolShowNStats		= _boolShowNStats;
		job._boolIndy1Stagger	= _boolIndy1Stagger;
		job._boolIndy2Stagger	= _boolIndy2Stagger;
		job._boolGridOn			= _boolGridOn;
		job._boolSyncAxes		= _boolSyncAxes;
		job._boolDumpPoints1	= _boolDumpPoints1;
		job._boolDumpPoints2	= _boolDumpPoints2;
		job._boolLogY1			= _boolLogY1;
		job._boolLogY2			= _boolLogY2;
		
		job._strPlotType		= _strPlotType;
		job._strPlotHeight		= _strPlotHeight;
		job._strPlotWidth		= _strPlotWidth;
		job._strPlotRes			= _strPlotRes;
		job._strPlotUnits		= _strPlotUnits;
		job._strMar				= _strMar;
		job._strMgp				= _strMgp;
		job._strCex				= _strCex;
		job._strTitleWeight		= _strTitleWeight;
		job._strTitleSize		= _strTitleSize;
		job._strTitleOffset		= _strTitleOffset;
		job._strTitleAlign		= _strTitleAlign;
		job._strXtlabOrient		= _strXtlabOrient;
		job._strXtlabPerp		= _strXtlabPerp;
		job._strXtlabHoriz		= _strXtlabHoriz;
		job._strXtlabFreq		= _strXtlabFreq;
		job._strXtlabSize		= _strXtlabSize;
		job._strXlabWeight		= _strXlabWeight;
		job._strXlabSize		= _strXlabSize;
		job._strXlabOffset		= _strXlabOffset;
		job._strXlabAlign		= _strXlabAlign;
		job._strYtlabOrient		= _strYtlabOrient;
		job._strYtlabPerp		= _strYtlabPerp;
		job._strYtlabHoriz		= _strYtlabHoriz;
		job._strYtlabSize		= _strYtlabSize;
		job._strYlabWeight		= _strYlabWeight;
		job._strYlabSize		= _strYlabSize;
		job._strYlabOffset		= _strYlabOffset;
		job._strYlabAlign		= _strYlabAlign;
		job._strGridLty			= _strGridLty;
		job._strGridCol			= _strGridCol;
		job._strGridLwd			= _strGridLwd;
		job._strGridX			= _strGridX;
		job._strX2tlabOrient	= _strX2tlabOrient;
		job._strX2tlabPerp		= _strX2tlabPerp;
		job._strX2tlabHoriz		= _strX2tlabHoriz;
		job._strX2tlabSize		= _strX2tlabSize;
		job._strX2labWeight		= _strX2labWeight;
		job._strX2labSize		= _strX2labSize;
		job._strX2labOffset		= _strX2labOffset;
		job._strX2labAlign		= _strX2labAlign;
		job._strY2tlabOrient	= _strY2tlabOrient;
		job._strY2tlabPerp		= _strY2tlabPerp;
		job._strY2tlabHoriz		= _strY2tlabHoriz;
		job._strY2tlabSize		= _strY2tlabSize;
		job._strY2labWeight		= _strY2labWeight;
		job._strY2labSize		= _strY2labSize;
		job._strY2labOffset		= _strY2labOffset;
		job._strY2labAlign		= _strY2labAlign;
		job._strLegendSize		= _strLegendSize;
		job._strLegendBox		= _strLegendBox;
		job._strLegendInset		= _strLegendInset;
		job._strLegendNcol		= _strLegendNcol;
		job._strCaptionWeight	= _strCaptionWeight;
		job._strCaptionCol		= _strCaptionCol;
		job._strCaptionSize		= _strCaptionSize;
		job._strCaptionOffset	= _strCaptionOffset;
		job._strCaptionAlign	= _strCaptionAlign;
		job._strBoxOutline		= _strBoxOutline;
		job._strBoxPts			= _strBoxPts;
		job._strBoxBoxwex		= _strBoxBoxwex;
		job._strBoxNotch		= _strBoxNotch;
		job._strBoxAvg			= _strBoxAvg;
		job._strRelyEventHist	= _strRelyEventHist;
		job._strCIAlpha			= _strCIAlpha;

		job._boolAggCtc			= _boolAggCtc;
		job._boolAggSl1l2		= _boolAggSl1l2;
		job._boolAggPct			= _boolAggPct;
		job._strAggBootRepl		= _strAggBootRepl;
		job._strAggBootCI		= _strAggBootCI;
		job._strAggDiff1		= _strAggDiff1;
		job._strAggDiff2		= _strAggDiff2;
		
		job._boolCalcCtc		= _boolCalcCtc;
		job._boolCalcSl1l2		= _boolCalcSl1l2;

		job._boolRocPct			= _boolRocPct;
		job._boolRocCtc			= _boolRocCtc;

		job._strPlotCI			= _strPlotCI;
		job._strPlotDisp		= _strPlotDisp;
		job._strColors			= _strColors;
		job._strPch				= _strPch;
		job._strType			= _strType;
		job._strLty				= _strLty;
		job._strLwd				= _strLwd;
		job._strConSeries		= _strConSeries;
		job._strLegend			= _strLegend;
		job._strY1Lim			= _strY1Lim;
		job._strY1Bufr			= _strY1Bufr;
		job._strY2Lim			= _strY2Lim;
		job._strY2Bufr			= _strY2Bufr;
		
		return job;
	}
	
	public String	getJobName()										{ return _strJobName;									}
	public void		setJobName(String jobName)							{ _strJobName = jobName;								}
	public Connection getConnection()									{ return _con;											}
	public void		setConnection(Connection con)						{ _con = con;											}
	public String	getDBHost()											{ return _strDBHost;									}
	public void		setDBHost(String dbHost)							{ _strDBHost = dbHost;									}
	public String	getDBName()											{ return _strDBName;									}
	public void		setDBName(String dbName)							{ _strDBName = dbName;									}
	public String	getDBUser()											{ return _strDBUser;									}
	public void		setDBUser(String dbUser)							{ _strDBUser = dbUser;									}
	public String	getDBPassword()										{ return _strDBPassword;								}
	public void		setDBPassword(String dbPassword)					{ _strDBPassword = dbPassword;							}
	public String	getRscript()										{ return _strRscript;									}
	public void		setRscript(String Rscript)							{ _strRscript = Rscript;								}
	public String	getPlotTmpl()										{ return _strPlotTmpl;									}
	public void		setPlotTmpl(String plotTmpl)						{ _strPlotTmpl = plotTmpl;								}
	public String	getIndyVar()										{ return _strIndyVar;									}
	public void		setIndyVar(String indyVar)							{ _strIndyVar = indyVar;								}
	public String[]	getIndyVal()										{ return _listIndyVal;									}
	public void		setIndyVal(String[] indyVal)						{ _listIndyVal = indyVal;								}
	public String[]	getIndyPlotVal()									{ return _listIndyPlotVal;								}
	public void		setIndyPlotVal(String[] indyPlotVal)				{ _listIndyPlotVal = indyPlotVal;						}
	public String[]	getIndyLabel()										{ return _listIndyLabel;								}
	public void		setIndyLabel(String[] indyLabel)					{ _listIndyLabel = indyLabel;							}
	public MVPlotDep getIndyDep()										{ return _depIndy;										}
	public void		setIndyDep(MVPlotDep dep)							{ _depIndy = dep;										}
	
	public MVOrderedMap getPlotFixVal()									{ return _mapPlotFixVal;								}
	public void addPlotFixVal(String field, String[] vals, int index)	{ _mapPlotFixVal.put(field, vals, index);				}
	public void addPlotFixVal(String field, String[] vals)				{ addPlotFixVal(field, vals, _mapPlotFixVal.size());	}
	public void addPlotFixVal(String field, MVOrderedMap sets, int index){ _mapPlotFixVal.put(field, sets, index);				}
	public void addPlotFixVal(String field, MVOrderedMap sets)			{ addPlotFixVal(field, sets, _mapPlotFixVal.size());	}
	public void removePlotFixVal(String field)							{ _mapPlotFixVal.remove(field);							}
	public void clearPlotFixVal()										{ _mapPlotFixVal = new MVOrderedMap();					}
	
	public MVOrderedMap[] getDepGroups()								{ return _listDepGroup;									}
	public void addDepGroup(MVOrderedMap depGroup){
		ArrayList listDepGroup = new ArrayList(Arrays.asList(_listDepGroup));
		listDepGroup.add(depGroup);
		_listDepGroup = (MVOrderedMap[])listDepGroup.toArray(new MVOrderedMap[]{});
	}
	public void clearDepGroups()										{ _listDepGroup = new MVOrderedMap[]{};					}
	
	public MVOrderedMap getSeries1Val()									{ return _mapSeries1Val;								}
	public void addSeries1Val(String field, String[] vals, int index)	{ _mapSeries1Val.put(field, vals, index);				}
	public void addSeries1Val(String field, String[] vals)				{ addSeries1Val(field, vals, _mapSeries1Val.size());	}
	public void removeSeries1Val(String field)							{ _mapSeries1Val.remove(field);							}
	public void clearSeries1Val()										{ _mapSeries1Val = new MVOrderedMap();					}
	
	public MVOrderedMap getSeries2Val()									{ return _mapSeries2Val;								}
	public void addSeries2Val(String field, String[] vals, int index)	{ _mapSeries2Val.put(field, vals, index);				}
	public void addSeries2Val(String field, String[] vals)				{ addSeries2Val(field, vals, _mapSeries2Val.size());	}
	public void removeSeries2Val(String field)							{ _mapSeries2Val.remove(field);							}
	public void clearSeries2Val()										{ _mapSeries2Val = new MVOrderedMap();					}
	
	public MVOrderedMap getSeriesNobs()									{ return _mapSeriesNobs;								}
	public void addSeriesNobs(String field, String val, int index)		{ _mapSeriesNobs.put(field, val, index);				}
	public void addSeriesNobs(String field, String val)					{ addSeriesNobs(field, val, _mapSeriesNobs.size());		}
	public void removeSeriesNobs(String field)							{ _mapSeriesNobs.remove(field);							}
	public void clearSeriesNobs()										{ _mapSeriesNobs = new MVOrderedMap();					}
	
	public MVOrderedMap getDep1Scale()									{ return _mapDep1Scale;									}
	public void addDep1Scale(String field, String val)					{ _mapDep1Scale.put(field, val);						}
	public void removeDep1Scale(String field)							{ _mapDep1Scale.remove(field);							}
	public void clearDep1Scale()										{ _mapDep1Scale = new MVOrderedMap();					}
	
	public MVOrderedMap getDep2Scale()									{ return _mapDep2Scale;									}
	public void addDep2Scale(String field, String val)					{ _mapDep2Scale.put(field, val);						}
	public void removeDep2Scale(String field)							{ _mapDep2Scale.remove(field);							}
	public void clearDep2Scale()										{ _mapDep2Scale = new MVOrderedMap();					}
	
	public MVOrderedMap getTmplMaps()									{ return _mapTmplMaps;									}
	public MVOrderedMap getTmplMap(String field){
		return ( _mapTmplMaps.containsKey(field)? (MVOrderedMap)_mapTmplMaps.get(field) : null);
	}
	public void addTmplMap(String field, MVOrderedMap map, int index)	{ _mapTmplMaps.put(field, map, index);					}
	public void addTmplMap(String field, MVOrderedMap map)				{ addTmplMap(field, map, _mapTmplMaps.size());			}
	public void removeTmplMap(String field)								{ _mapTmplMaps.remove(field);							}
	public void clearTmplMap()											{ _mapTmplMaps = new MVOrderedMap();					}

	public MVOrderedMap getTmplVal()									{ return _mapTmplVal;									}
	public void		addTmplVal(String id, String name)					{ _mapTmplVal.put(id, name);							}
	
	public String	getDataFileTmpl()									{ return _strDataFileTmpl;								}
	public void		setDataFileTmpl(String dataFileTmpl)				{ _strDataFileTmpl = dataFileTmpl;						}
	public String	getPlotFileTmpl()									{ return _strPlotFileTmpl;								}
	public void		setPlotFileTmpl(String plotFileTmpl)				{ _strPlotFileTmpl = plotFileTmpl;						}
	public String	getRFileTmpl()										{ return _strRFileTmpl;									}
	public void		setRFileTmpl(String rFileTmpl)						{ _strRFileTmpl = rFileTmpl;							}
	public String	getTitleTmpl()										{ return _strTitleTmpl;									}
	public void		setTitleTmpl(String titleTmpl)						{ _strTitleTmpl = titleTmpl;							}
	public String	getXLabelTmpl()										{ return _strXLabelTmpl;								}
	public void		setXLabelTmpl(String xLabelTmpl)					{ _strXLabelTmpl = xLabelTmpl;							}
	public String	getY1LabelTmpl()									{ return _strY1LabelTmpl;								}
	public void		setY1LabelTmpl(String Y1LabelTmpl)					{ _strY1LabelTmpl = Y1LabelTmpl;						}
	public String	getY2LabelTmpl()									{ return _strY2LabelTmpl;								}
	public void		setY2LabelTmpl(String Y2LabelTmpl)					{ _strY2LabelTmpl = Y2LabelTmpl;						}
	public String	getCaptionTmpl()									{ return _strCaptionTmpl;								}
	public void		setCaptionTmpl(String captionTmpl)					{ _strCaptionTmpl = captionTmpl;						}
	public String	getPlotCmd()										{ return _strPlotCmd;									}
	public void		setPlotCmd(String plotCmd)							{ _strPlotCmd = plotCmd;								}
	
	public boolean	getEventEqual()										{ return _boolEventEqual;								}
	public void		setEventEqual(boolean eventEqual)					{ _boolEventEqual = eventEqual; 						}
	public boolean	getEventEqualM()									{ return _boolEventEqualM;								}
	public void		setEventEqualM(boolean eventEqualM)					{ _boolEventEqualM = eventEqualM; 						}
	public boolean	getVertPlot()										{ return _boolVertPlot;									}
	public void		setVertPlot(boolean vertPlot)						{ _boolVertPlot = vertPlot; 							}
	public boolean	getXReverse()										{ return _boolXReverse;									}
	public void		setXReverse(boolean xReverse)						{ _boolXReverse = xReverse; 							}
	public boolean	getPlot1Diff()										{ return _boolPlot1Diff;								}
	public void		setPlot1Diff(boolean plot1Diff)						{ _boolPlot1Diff = plot1Diff; 							}
	public boolean	getPlot2Diff()										{ return _boolPlot2Diff;								}
	public void		setPlot2Diff(boolean plot2Diff)						{ _boolPlot2Diff = plot2Diff; 							}
	public boolean	getShowNStats()										{ return _boolShowNStats;								}
	public void		setShowNStats(boolean showNStats)					{ _boolShowNStats = showNStats; 						}
	public boolean	getIndy1Stagger()									{ return _boolIndy1Stagger;								}
	public void		setIndy1Stagger(boolean indy1Stagger)				{ _boolIndy1Stagger = indy1Stagger;						}
	public boolean	getIndy2Stagger()									{ return _boolIndy2Stagger;								}
	public void		setIndy2Stagger(boolean indy2Stagger)				{ _boolIndy2Stagger = indy2Stagger;						}
	public boolean	getGridOn()											{ return _boolGridOn;									}
	public void		setGridOn(boolean gridOn)							{ _boolGridOn = gridOn; 								}
	public boolean	getSyncAxes()										{ return _boolSyncAxes;									}
	public void		setSyncAxes(boolean syncAxes)						{ _boolSyncAxes = syncAxes; 							}
	public boolean	getDumpPoints1()									{ return _boolDumpPoints1;								}
	public void		setDumpPoints1(boolean dumpPoints1)					{ _boolDumpPoints1 = dumpPoints1; 						}
	public boolean	getDumpPoints2()									{ return _boolDumpPoints2;								}
	public void		setDumpPoints2(boolean dumpPoints2)					{ _boolDumpPoints2 = dumpPoints2; 						}
	public boolean	getLogY1()											{ return _boolLogY1;									}
	public void		setLogY1(boolean logY1)								{ _boolLogY1 = logY1; 									}
	public boolean	getLogY2()											{ return _boolLogY2;									}
	public void		setLogY2(boolean logY2)								{ _boolLogY2 = logY2; 									}
	
	public String	getPlotType()										{ return _strPlotType;									}
	public void		setPlotType(String plotType)						{ _strPlotType = plotType;								}
	public String	getPlotHeight()										{ return _strPlotHeight;								}
	public void		setPlotHeight(String plotHeight)					{ _strPlotHeight = plotHeight;							}
	public String	getPlotWidth()										{ return _strPlotWidth;									}
	public void		setPlotWidth(String plotWidth)						{ _strPlotWidth = plotWidth;							}
	public String	getPlotRes()										{ return _strPlotRes;									}
	public void		setPlotRes(String plotRes)							{ _strPlotRes = plotRes;								}
	public String	getPlotUnits()										{ return _strPlotUnits;									}
	public void		setPlotUnits(String plotUnits)						{ _strPlotUnits = plotUnits;							}
	public String	getMar()											{ return _strMar;										}
	public void		setMar(String mar)									{ _strMar = mar;										}
	public String	getMgp()											{ return _strMgp;										}
	public void		setMgp(String mgp)									{ _strMgp = mgp;										}
	public String	getCex()											{ return _strCex;										}
	public void		setCex(String cex)									{ _strCex = cex;										}
	public String	getTitleWeight()									{ return _strTitleWeight;								}
	public void		setTitleWeight(String titleWeight)					{ _strTitleWeight = titleWeight;						}
	public String	getTitleSize()										{ return _strTitleSize;									}
	public void		setTitleSize(String titleSize)						{ _strTitleSize = titleSize;							}
	public String	getTitleOffset()									{ return _strTitleOffset;								}
	public void		setTitleOffset(String titleOffset)					{ _strTitleOffset = titleOffset;						}
	public String	getTitleAlign()										{ return _strTitleAlign;								}
	public void		setTitleAlign(String titleAlign)					{ _strTitleAlign = titleAlign;							}
	public String	getXtlabOrient()									{ return _strXtlabOrient;								}
	public void		setXtlabOrient(String xtlabOrient)					{ _strXtlabOrient = xtlabOrient;						}
	public String	getXtlabPerp()										{ return _strXtlabPerp;									}
	public void		setXtlabPerp(String xtlabPerp)						{ _strXtlabPerp = xtlabPerp;							}
	public String	getXtlabHoriz()										{ return _strXtlabHoriz;								}
	public void		setXtlabHoriz(String xtlabHoriz)					{ _strXtlabHoriz = xtlabHoriz;							}
	public String	getXtlabFreq()										{ return _strXtlabFreq;									}
	public void		setXtlabFreq(String xtlabFreq)						{ _strXtlabFreq = xtlabFreq;							}
	public String	getXtlabSize()										{ return _strXtlabSize;									}
	public void		setXtlabSize(String xtlabSize)						{ _strXtlabSize = xtlabSize;							}
	public String	getXlabWeight()										{ return _strXlabWeight;								}
	public void		setXlabWeight(String xlabWeight)					{ _strXlabWeight = xlabWeight;							}
	public String	getXlabSize()										{ return _strXlabSize;									}
	public void		setXlabSize(String xlabSize)						{ _strXlabSize = xlabSize;								}
	public String	getXlabOffset()										{ return _strXlabOffset;								}
	public void		setXlabOffset(String xlabOffset)					{ _strXlabOffset = xlabOffset;							}
	public String	getXlabAlign()										{ return _strXlabAlign;									}
	public void		setXlabAlign(String xlabAlign)						{ _strXlabAlign = xlabAlign;							}
	public String	getYtlabOrient()									{ return _strYtlabOrient;								}
	public void		setYtlabOrient(String ytlabOrient)					{ _strYtlabOrient = ytlabOrient;						}
	public String	getYtlabPerp()										{ return _strYtlabPerp;									}
	public void		setYtlabPerp(String ytlabPerp)						{ _strYtlabPerp = ytlabPerp;							}
	public String	getYtlabHoriz()										{ return _strYtlabHoriz;								}
	public void		setYtlabHoriz(String ytlabHoriz)					{ _strYtlabHoriz = ytlabHoriz;							}
	public String	getYtlabSize()										{ return _strYtlabSize;									}
	public void		setYtlabSize(String ytlabSize)						{ _strYtlabSize = ytlabSize;							}
	public String	getYlabWeight()										{ return _strYlabWeight;								}
	public void		setYlabWeight(String ylabWeight)					{ _strYlabWeight = ylabWeight;							}
	public String	getYlabSize()										{ return _strYlabSize;									}
	public void		setYlabSize(String ylabSize)						{ _strYlabSize = ylabSize;								}
	public String	getYlabOffset()										{ return _strYlabOffset;								}
	public void		setYlabOffset(String ylabOffset)					{ _strYlabOffset = ylabOffset;							}
	public String	getYlabAlign()										{ return _strYlabAlign;									}
	public void		setYlabAlign(String ylabAlign)						{ _strYlabAlign = ylabAlign;							}
	public String	getGridLty()										{ return _strGridLty;									}
	public void		setGridLty(String gridLty)							{ _strGridLty = gridLty;								}
	public String	getGridCol()										{ return _strGridCol;									}
	public void		setGridCol(String gridCol)							{ _strGridCol = gridCol;								}
	public String	getGridLwd()										{ return _strGridLwd;									}
	public void		setGridLwd(String gridLwd)							{ _strGridLwd = gridLwd;								}
	public String	getGridX()											{ return _strGridX;										}
	public void		setGridX(String gridX)								{ _strGridX = gridX;									}
	public String	getX2tlabOrient()									{ return _strX2tlabOrient;								}
	public void		setX2tlabOrient(String x2tlabOrient)				{ _strX2tlabOrient = x2tlabOrient;						}
	public String	getX2tlabPerp()										{ return _strX2tlabPerp;								}
	public void		setX2tlabPerp(String x2tlabPerp)					{ _strX2tlabPerp = x2tlabPerp;							}
	public String	getX2tlabHoriz()									{ return _strX2tlabHoriz;								}
	public void		setX2tlabHoriz(String x2tlabHoriz)					{ _strX2tlabHoriz = x2tlabHoriz;						}
	public String	getX2tlabSize()										{ return _strX2tlabSize;								}
	public void		setX2tlabSize(String x2tlabSize)					{ _strX2tlabSize = x2tlabSize;							}
	public String	getX2labWeight()									{ return _strX2labWeight;								}
	public void		setX2labWeight(String x2labWeight)					{ _strX2labWeight = x2labWeight;						}
	public String	getX2labSize()										{ return _strX2labSize;									}
	public void		setX2labSize(String x2labSize)						{ _strX2labSize = x2labSize;							}
	public String	getX2labOffset()									{ return _strX2labOffset;								}
	public void		setX2labOffset(String x2labOffset)					{ _strX2labOffset = x2labOffset;						}
	public String	getX2labAlign()										{ return _strX2labAlign;								}
	public void		setX2labAlign(String x2labAlign)					{ _strX2labAlign = x2labAlign;							}
	public String	getY2tlabOrient()									{ return _strY2tlabOrient;								}
	public void		setY2tlabOrient(String y2tlabOrient)				{ _strY2tlabOrient = y2tlabOrient;						}
	public String	getY2tlabPerp()										{ return _strY2tlabPerp;								}
	public void		setY2tlabPerp(String y2tlabPerp)					{ _strY2tlabPerp = y2tlabPerp;							}
	public String	getY2tlabHoriz()									{ return _strY2tlabHoriz;								}
	public void		setY2tlabHoriz(String y2tlabHoriz)					{ _strY2tlabHoriz = y2tlabHoriz;						}
	public String	getY2tlabSize()										{ return _strY2tlabSize;								}
	public void		setY2tlabSize(String y2tlabSize)					{ _strY2tlabSize = y2tlabSize;							}
	public String	getY2labWeight()									{ return _strY2labWeight;								}
	public void		setY2labWeight(String y2labWeight)					{ _strY2labWeight = y2labWeight;						}
	public String	getY2labSize()										{ return _strY2labSize;									}
	public void		setY2labSize(String y2labSize)						{ _strY2labSize = y2labSize;							}
	public String	getY2labOffset()									{ return _strY2labOffset;								}
	public void		setY2labOffset(String y2labOffset)					{ _strY2labOffset = y2labOffset;						}
	public String	getY2labAlign()										{ return _strY2labAlign;								}
	public void		setY2labAlign(String y2labAlign)					{ _strY2labAlign = y2labAlign;							}
	public String	getLegendSize()										{ return _strLegendSize;								}
	public void		setLegendSize(String legendSize)					{ _strLegendSize = legendSize;							}
	public String	getLegendBox()										{ return _strLegendBox;									}
	public void		setLegendBox(String legendBox)						{ _strLegendBox = legendBox;							}
	public String	getLegendInset()									{ return _strLegendInset;								}
	public void		setLegendInset(String legendInset)					{ _strLegendInset = legendInset;						}
	public String	getLegendNcol()										{ return _strLegendNcol;								}
	public void		setLegendNcol(String legendNcol)					{ _strLegendNcol = legendNcol;							}
	public String	getCaptionWeight()									{ return _strCaptionWeight;								}
	public void		setCaptionWeight(String captionWeight)				{ _strCaptionWeight = captionWeight;					}
	public String	getCaptionCol()										{ return _strCaptionCol;								}
	public void		setCaptionCol(String captionCol)					{ _strCaptionCol = captionCol;							}
	public String	getCaptionSize()									{ return _strCaptionSize;								}
	public void		setCaptionSize(String captionSize)					{ _strCaptionSize = captionSize;						}
	public String	getCaptionOffset()									{ return _strCaptionOffset;								}
	public void		setCaptionOffset(String captionOffset)				{ _strCaptionOffset = captionOffset;					}
	public String	getCaptionAlign()									{ return _strCaptionAlign;								}
	public void		setCaptionAlign(String captionAlign)				{ _strCaptionAlign = captionAlign;						}
	public String	getBoxOutline()										{ return _strBoxOutline;								}
	public void		setBoxOutline(String boxOutline)					{ _strBoxOutline = boxOutline;							}
	public String	getBoxPts()											{ return _strBoxPts;									}
	public void		setBoxPts(String boxPts)							{ _strBoxPts = boxPts;									}
	public String	getBoxBoxwex()										{ return _strBoxBoxwex;									}
	public void		setBoxBoxwex(String boxBoxwex)						{ _strBoxBoxwex = boxBoxwex;							}
	public String	getBoxNotch()										{ return _strBoxNotch;									}
	public void		setBoxNotch(String boxNotch)						{ _strBoxNotch = boxNotch;								}
	public String	getBoxAvg()											{ return _strBoxAvg;									}
	public void		setBoxAvg(String boxAvg)							{ _strBoxAvg = boxAvg;									}
	public String	getRelyEventHist()									{ return _strRelyEventHist;								}
	public void		setRelyEventHist(String relyEventHist)				{ _strRelyEventHist = relyEventHist;					}
	public String	getCIAlpha()										{ return _strCIAlpha;									}
	public void		setCIAlpha(String ciAlpha)							{ _strCIAlpha = ciAlpha;								}

	public boolean	getAggCtc()											{ return _boolAggCtc;									}
	public void		setAggCtc(boolean aggCtc)							{ _boolAggCtc = aggCtc; 								}
	public boolean	getAggSl1l2()										{ return _boolAggSl1l2;									}
	public void		setAggSl1l2(boolean aggSl1l2)						{ _boolAggSl1l2 = aggSl1l2; 							}
	public boolean	getAggPct()											{ return _boolAggPct;									}
	public void		setAggPct(boolean aggPct)							{ _boolAggPct = aggPct; 								}
	public String	getAggBootRepl()									{ return _strAggBootRepl;								}
	public void		setAggBootRepl(String aggBootRepl)					{ _strAggBootRepl = aggBootRepl;						}
	public String	getAggBootCI()										{ return _strAggBootCI;									}
	public void		setAggBootCI(String aggBootCI)						{ _strAggBootCI = aggBootCI;							}
	public boolean	getAggDiff1()										{ return _strAggDiff1;									}
	public void		setAggDiff1(boolean aggDiff1)						{ _strAggDiff1 = aggDiff1;								}
	public boolean	getAggDiff2()										{ return _strAggDiff2;									}
	public void		setAggDiff2(boolean aggDiff2)						{ _strAggDiff2 = aggDiff2;								}
	
	public boolean	getCalcCtc()										{ return _boolCalcCtc;									}
	public void		setCalcCtc(boolean calcCtc)							{ _boolCalcCtc = calcCtc; 								}
	public boolean	getCalcSl1l2()										{ return _boolCalcSl1l2;								}
	public void		setCalcSl1l2(boolean calcSl1l2)						{ _boolCalcSl1l2 = calcSl1l2; 							}	
	
	public boolean	getRocPct()											{ return _boolRocPct;									}
	public void		setRocPct(boolean rocPct)							{ _boolRocPct = rocPct; 								}
	public boolean	getRocCtc()											{ return _boolRocCtc;									}
	public void		setRocCtc(boolean rocCtc)							{ _boolRocCtc = rocCtc; 								}	
	
	public String	getPlotCI()											{ return _strPlotCI;									}
	public void		setPlotCI(String plotCI)							{ _strPlotCI = plotCI;									}
	public String	getPlotDisp()										{ return _strPlotDisp;									}
	public void		setPlotDisp(String plotDisp)						{ _strPlotDisp = plotDisp;								}
	public String	getColors()											{ return _strColors;									}
	public void		setColors(String colors)							{ _strColors = colors;									}
	public String	getPch()											{ return _strPch;										}
	public void		setPch(String pch)									{ _strPch = pch;										}
	public String	getType()											{ return _strType;										}
	public void		setType(String type)								{ _strType = type;										}
	public String	getLty()											{ return _strLty;										}
	public void		setLty(String lty)									{ _strLty = lty;										}
	public String	getLwd()											{ return _strLwd;										}
	public void		setLwd(String lwd)									{ _strLwd = lwd;										}
	public String	getConSeries()										{ return _strConSeries;									}
	public void		setConSeries(String conSeries)						{ _strConSeries = conSeries;							}
	public String	getLegend()											{ return _strLegend;									}
	public void		setLegend(String legend)							{ _strLegend = legend;									}
	public String	getY1Lim()											{ return _strY1Lim;										}
	public void		setY1Lim(String y1Lim)								{ _strY1Lim = y1Lim;									}
	public String	getY1Bufr()											{ return _strY1Bufr;									}
	public void		setY1Bufr(String y1Bufr)							{ _strY1Bufr = y1Bufr;									}
	public String	getY2Lim()											{ return _strY2Lim;										}
	public void		setY2Lim(String y2Lim)								{ _strY2Lim = y2Lim;									}
	public String	getY2Bufr()											{ return _strY2Bufr;									}
	public void		setY2Bufr(String y2Bufr)							{ _strY2Bufr = y2Bufr;									}
	
}

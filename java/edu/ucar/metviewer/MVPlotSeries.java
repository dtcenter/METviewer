package edu.ucar.metviewer;

public class MVPlotSeries{
	
	protected int _intAxis 					= 1;
	protected String _strDepVar 			= "";
	protected MVOrderedMap _mapSeriesVal	= new MVOrderedMap();
	protected MVOrderedMap _mapFixedVal		= new MVOrderedMap();
	
	protected String _strColor				= "";
	protected String _strPch				= "";
	protected String _strType				= "";
	protected String _strLty				= "";
	protected boolean _boolCI				= false;

}
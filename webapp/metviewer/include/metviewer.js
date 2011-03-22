
var _strDBCon = "";

var TMPL_SERIES_PLOT	= 1;
var TMPL_BAR_PLOT		= 2;
var TMPL_BOX_PLOT		= 3;
var TMPL_RHIST			= 4;
var _intTmpl = TMPL_SERIES_PLOT;

var _listLnkSer = ["Dep1", "Series1", "Dep2", "Series2", "Fix", "Indy", "FmtPlot", "FmtSeries", "AggStat"];

var _intDepIdNext = 1;
var _listDep1Div = new Array();
var _listDep2Div = new Array();

var _divFieldVal;
var _intFieldValIdNext = 0;

var _listStatModeSingle = [
						"ACOV", "CNT", "CNTSUM", "CENTX", "CENTY", "CENTLAT", "CENTLON", "AXAVG", "LEN", "WID", "ASPECT",
						"AREA", "AREAFIL", "AREATHR", "CURV", "CURVX", "CURVY", "CPLX", "INT10", "INT25", "INT50", 
						"INT75", "INT90", "INTN", "INTSUM", 
					 
						"RATIO_FSA_ASA", "RATIO_OSA_ASA", "RATIO_ASM_ASA", "RATIO_ASU_ASA", "RATIO_FSM_FSA", 
						"RATIO_FSU_FSA", "RATIO_OSM_OSA", "RATIO_OSU_OSA", "RATIO_FSM_ASM", "RATIO_OSM_ASM", 
						"RATIO_FSU_ASU", "RATIO_OSU_ASU", "RATIO_FSA_AAA", "RATIO_OSA_AAA", "RATIO_FSA_FAA", 
						"RATIO_FCA_FAA", "RATIO_OSA_OAA", "RATIO_OCA_OAA", "RATIO_FCA_ACA", "RATIO_OCA_ACA", 
						"RATIO_FSA_OSA", "RATIO_OSA_FSA", "RATIO_ACA_ASA", "RATIO_ASA_ACA", "RATIO_FCA_FSA", 
						"RATIO_FSA_FCA", "RATIO_OCA_OSA", "RATIO_OSA_OCA", "OBJHITS", "OBJMISSES", "OBJFAS", 
						"OBJCSI", "OBJPODY", "OBJFAR", 

						"AREARAT_FSA_ASA", "AREARAT_OSA_ASA", "AREARAT_ASM_ASA", "AREARAT_ASU_ASA", "AREARAT_FSM_FSA", 
						"AREARAT_FSU_FSA", "AREARAT_OSM_OSA", "AREARAT_OSU_OSA", "AREARAT_FSM_ASM", "AREARAT_OSM_ASM", 
						"AREARAT_FSU_ASU", "AREARAT_OSU_ASU", "AREARAT_FSA_AAA", "AREARAT_OSA_AAA", "AREARAT_FSA_FAA", 
						"AREARAT_FCA_FAA", "AREARAT_OSA_OAA", "AREARAT_OCA_OAA", "AREARAT_FCA_ACA", "AREARAT_OCA_ACA", 
						"AREARAT_FSA_OSA", "AREARAT_OSA_FSA", "AREARAT_ACA_ASA", "AREARAT_ASA_ACA", "AREARAT_FCA_FSA", 
						"AREARAT_FSA_FCA", "AREARAT_OCA_OSA", "AREARAT_OSA_OCA", "OBJAHITS", "OBJAMISSES", "OBJAFAS", 
						"OBJACSI", "OBJAPODY", "OBJAFAR",
					];

var _listStatModePair = [						
						"CENTDIST", "BOUNDDIST", "HULLDIST", "ANGLEDIFF", "AREARATIO", "INTAREA", "UNIONAREA", 
						"SYMDIFF", "INTOVERAREA", "CMPLXRATIO", "PERCINTRATIO",  "INT", "MAXINT", "MAXINTF", "MAXINTO"
					];

var _listStatMode = _listStatModeSingle.concat(_listStatModePair);

var _listVarStat = ["MODEL", "FCST_LEAD", "FCST_VALID_BEG", "VALID_HOUR", "FCST_INIT_BEG", "INIT_HOUR", "FCST_LEV", 
                    "OBTYPE", "VX_MASK", "INTERP_MTHD", "INTERP_PNTS", "FCST_THRESH"];
var _listVarRhist = ["FCST_VAR", "MODEL", "FCST_LEAD", "FCST_VALID_BEG", "VALID_HOUR", "FCST_INIT_BEG", "INIT_HOUR", 
                     "FCST_LEV", "OBTYPE", "VX_MASK", "INTERP_MTHD", "INTERP_PNTS", "FCST_THRESH", "N_RANK"];
var _listVarMode = ["MODEL", "FCST_LEAD", "FCST_VALID", "VALID_HOUR", "FCST_INIT", "INIT_HOUR", "FCST_ACCUM", 
                    "FCST_RAD", "FCST_THR", "FCST_LEV"];
var _listVar = _listVarStat;

var _listSeries1Div = new Array();
var _listSeries2Div = new Array();
var _listFixDiv = new Array();
var _listRhistDiv = new Array();

var _listIndyVarStat = ["FCST_LEAD", "FCST_LEV", "FCST_THRESH", "OBS_THRESH", "FCST_VALID_BEG", "VALID_HOUR", 
                        "FCST_INIT_BEG", "INIT_HOUR", "INTERP_PNTS"];
var _listIndyVarMode = ["FCST_LEAD", "FCST_LEV", "FCST_THR", "FCST_VALID", "VALID_HOUR", "FCST_INIT", "INIT_HOUR",
                        "FCST_RAD"];
var _listIndyVar = _listIndyVarStat;
var _intIndyValIdNext = 0;

var _strPlotData = "stat";

var _strFmtPlotWidth = "275px";
var _intNumFmtPlotCol = 4;
var _intFmtPlotTxtIndex = 0;
var _intFmtPlotBoolIndex = 0;
var _intNumSeries = 0;
var _listFmtSeriesDefaults = ["false", "false", "none", "", "20", "b", "1", "1", "1", ""];


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Administration/Utility Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * When the page loads, perform the initialization duties including setting
 * pointers to DHTML elements, loading the list of databases and populating the
 * various database field lists.
 */

function onLoad(){
	_url = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1) + "servlet";
	console("_url: " + _url + "\n\n");

	_boolIE = (-1 != navigator.appName.indexOf("Internet Explorer"));

	/*
	 console(
	 	"browser codeName: " + navigator.appCodeName + "\n" + 
	 	"browser name: " + navigator.appName + "\n" + 
	 	"browser version: " + navigator.appVersion + "\n" + 
	 	"cookies enabled: " + navigator.cookieEnabled + "\n" + 
	 	"platform: " + navigator.platform + "\n" + 
	 	"user-agent header: " + navigator.userAgent + "\n" + 
	 	"IE: " + (_boolIE? "true" : "false") + "\n\n"
	 );
	*/
	console("load() - IE: " + (_boolIE? "true" : "false") + "\n\n");

	// add a handler to ensure that the Loading... screen tracks with scrolling
	window.onscroll = function(e){ if( _boolDim ){ dimScreen(true); } }
	
	// initialize the debug controls
	setDebugDisp(_boolDebugDisp);

	// initialize the database list and controls
	var strDBLoad = document.getElementById("spanDBLoad").innerHTML;
	if( "" == strDBLoad ){
		listDBReq();
	} else {		
		_strDBCon = strDBLoad;
		document.getElementById("selDB").style.display = "none";
		console("onLoad() - loading single database: " + _strDBCon + "\n\n");
		listFcstVar1Req(0);
	}

	// initialize the dep list
	var divDep0 = document.getElementById("divDep1").getElementsByTagName("div")[0];
	_listDep1Div.push( divDep0 );

	// initialize the series lists
	_divFieldVal = document.getElementById("divFieldVal");
	fillSelect(document.getElementById("selField"), _listVar);
	addSeries1Div();
	document.getElementById("lnkRemoveFieldVal0").style.display = "none";

	// initialize the independent variable controls
	fillSelect(document.getElementById("selIndyVar"), _listIndyVar);

	// initialize the plot format controls
	for(var i=1; i < _intNumFmtPlotCol; i++){
		var tdFmtPlotBool = document.getElementById("trFmtPlotBool").insertCell(i);
		tdFmtPlotBool.align = "right";
		tdFmtPlotBool.style.width = _strFmtPlotWidth;
	}
	for(var i=1; i < _intNumFmtPlotCol; i++){
		var tdFmtPlotTxt = document.getElementById("trFmtPlotTxt").insertCell(i);
		tdFmtPlotTxt.align="right";
		tdFmtPlotTxt.style.width = _strFmtPlotWidth;
	}

	// add the boolean formatting option controls
	addFmtPlot("Event Equalizer",				"event_equal",		"false",		"bool");
	addFmtPlot("Event Equalizer Multi",			"event_equal_m",	"false",		"bool");
	addFmtPlot("Vertical Levels Plot",			"vert_plot",		"false",		"bool");
	addFmtPlot("Reverse X Values",				"x_reverse",		"false",		"bool");
	addFmtPlot("Y1 Series Difference Curve",	"plot1_diff",		"false",		"bool");
	addFmtPlot("Y2 Series Difference Curve",	"plot2_diff",		"false",		"bool");
	addFmtPlot("Display Number of Stats",		"num_stats",		"false",		"bool");
	addFmtPlot("Y1 Stagger Points",				"indy1_stag",		"false",		"bool");
	addFmtPlot("Y2 Stagger Points",				"indy2_stag",		"false",		"bool");
	addFmtPlot("Plot Grid",						"grid_on",			"true",			"bool");
	addFmtPlot("Synch Y1 and Y2 Ranges",		"sync_axes",		"false",		"bool");
	addFmtPlot("Print Y1 Series Values",		"dump_points1",		"false",		"bool");
	addFmtPlot("Print Y2 Series Values",		"dump_points2",		"false",		"bool");
	addFmtPlot("Y1 Axis Log Scale",				"log_y1",			"false",		"bool");
	addFmtPlot("Y2 Axis Log Scale",				"log_y2",			"false",		"bool");

	// add onchange listeners to some controls
	var tabFmtPlot = document.getElementById("tabFmtPlotBool");
	var selVertPlot = tabFmtPlot.rows[0].cells[2].getElementsByTagName("select")[0];
	selVertPlot.setAttribute("onchange", "javascript:updateFmtPlot()");
 	if( _boolIE ){ selVertPlot.attachEvent("onchange", new Function("updateFmtPlot()")); }
	var selPlot1Diff = tabFmtPlot.rows[1].cells[0].getElementsByTagName("select")[0];
	selPlot1Diff.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selPlot1Diff.attachEvent("onchange", new Function("buildSeriesDiv()")); }
	var selPlot2Diff = tabFmtPlot.rows[1].cells[1].getElementsByTagName("select")[0];
	selPlot2Diff.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selPlot2Diff.attachEvent("onchange", new Function("buildSeriesDiv()")); }

	// add the text formatting options
	addFmtPlot("Plot Image Type",				"plot_type",		"png16m",		"txt");
	addFmtPlot("Plot Height",					"plot_height",		"8.5",			"txt");
	addFmtPlot("Plot Width",					"plot_width",		"11",			"txt");
	addFmtPlot("Plot Resolution",				"plot_res",			"72",			"txt");
	addFmtPlot("Plot Units",					"plot_units",		["in", "mm"],	"txt");
	addFmtPlot("Plot Margins",					"mar",				"c(8, 4, 5, 4)","txt");
	addFmtPlot("Axis Margin Line",				"mgp",				"c(1, 1, 0)", 	"txt");
	addFmtPlot("Text Magnification",			"cex",				"1",			"txt");
	addFmtPlot("Title Text Weight",				"title_weight",		["2", "1", "3", "4", "5"], "txt");
	addFmtPlot("Title Text Size",				"title_size",		"1.4",			"txt");
	addFmtPlot("Title Vert Offset",				"title_offset",		"-2",			"txt");
	addFmtPlot("Title Horiz Align",				"title_align",		".5",			"txt");
	addFmtPlot("X1 Values Orientation",			"xtlab_orient",		["1", "3"],		"txt");
	addFmtPlot("X1 Values Perp Offset ",		"xtlab_perp",		"-.75",			"txt");
	addFmtPlot("X1 Values Horiz Align",			"xtlab_horiz",		".5",			"txt");
	addFmtPlot("X1 Values Frequency",			"xtlab_freq",		"0",			"txt");
	addFmtPlot("X1 Label Text Weight",			"xlab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("X1 Label Text Size",			"xlab_size",		"1",			"txt");
	addFmtPlot("X1 Label Perp Offset",			"xlab_offset",		"2",			"txt");
	addFmtPlot("X1 Label Horiz Align",			"xlab_align",		".5",			"txt");
	addFmtPlot("Y1 Values Orientation",			"ytlab_orient",		["1", "3"],		"txt");
	addFmtPlot("Y1 Values Perp Offset",			"ytlab_perp",		".5",			"txt");
	addFmtPlot("Y1 Values Horiz Align",			"ytlab_horiz",		".5",			"txt");
	addFmtPlot("Y1 Label Text Weight",			"ylab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("Y1 Label Text Size",			"ylab_size",		"1",			"txt");
	addFmtPlot("Y1 Label Perp Offset",			"ylab_offset",		"-2",			"txt");
	addFmtPlot("Y1 Label Horiz Align",			"ylab_align",		".5",			"txt");
	addFmtPlot("Grid Line Type",				"grid_lty",			["3", "1", "2", "4", "5", "6"], "txt");
	addFmtPlot("Grid Line Color",				"grid_col",			"#CCCCCC",		"txt");
	addFmtPlot("Grid Line Width",				"grid_lwd",			"1",			"txt");
	addFmtPlot("Grid X positions",				"grid_x",			"listX",		"txt");
	addFmtPlot("X2 Stats Orientation",			"x2tlab_orient",	["1", "3"],		"txt");
	addFmtPlot("X2 Stats Perp Offset",			"x2tlab_perp",		"1",			"txt");
	addFmtPlot("X2 Stast Horiz Align",			"x2tlab_horiz",		".5",			"txt");
	addFmtPlot("X2 Label Text Weight",			"x2lab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("X2 Label Text Size",			"x2lab_size",		".8",			"txt");
	addFmtPlot("X2 Label Perp Offset",			"x2lab_offset",		"-.5",			"txt");
	addFmtPlot("X2 Label Horiz Align",			"x2lab_align",		".5",			"txt");
	addFmtPlot("Y2 Values Orientation",			"y2tlab_orient",	["1", "3"],		"txt");
	addFmtPlot("Y2 Values Perp Offset",			"y2tlab_perp",		".5",			"txt");
	addFmtPlot("Y2 Values Horiz Align",			"y2tlab_horiz",		".5",			"txt");
	addFmtPlot("Y2 Label Text Weight",			"y2lab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("Y2 Label Text Size",			"y2lab_size",		"1",			"txt");
	addFmtPlot("Y2 Label Perp Offset",			"y2lab_offset",		"1",			"txt");
	addFmtPlot("Y2 Label Horiz Align",			"y2lab_align",		".5",			"txt");
	addFmtPlot("Legend Text Size",				"legend_size",		".8",			"txt");
	addFmtPlot("Legend Box Type",				"legend_box",		["o", "n"],		"txt");
	addFmtPlot("Legend Box Position",			"legend_inset",		"c(0, -.25)",	"txt");
	addFmtPlot("Legend # of Columns",			"legend_ncol",		"3",			"txt");
	addFmtPlot("Caption Text Weight",			"caption_weight",	["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("Caption Text Color",			"caption_col",		"#333333FF",	"txt");
	addFmtPlot("Caption Text Size",				"caption_size",		".8",			"txt");
	addFmtPlot("Caption Perp Offset",			"caption_offset",	"3",			"txt");
	addFmtPlot("Caption Horiz Align",			"caption_align",	"0",			"txt");
	addFmtPlot("Box Plot Points",				"box_pts",			["FALSE", "TRUE"], "txt");
	addFmtPlot("Box Plot Show Outliers",		"box_outline",		["TRUE", "FALSE"], "txt");
	addFmtPlot("Box Plot Box Width",			"box_boxwex",		".2",			"txt");
	addFmtPlot("Box Plot Show Notches",			"box_notch",		["FALSE", "TRUE"], "txt");
	addFmtPlot("Box Plot Show Avg",				"box_avg",			["FALSE", "TRUE"], "txt");
	addFmtPlot("Conf Interval Alpha",			"ci_alpha",			".05", 			"txt");
	
	// build the series formatting controls
	buildSeriesDiv();
	setFmtSeriesMod(0, "false");

	// update the agg_stat controls
	updateAggStat();
}


/**
 * Button handlers that call the server to clear the <list_val> and <list_stat>
 * caches
 */
function dbClearCacheReq()      { sendRequest("POST", "<db_clear_cache/>"       , nullResp); clearControls(); }
function listValClearCacheReq() { sendRequest("POST", "<list_val_clear_cache/>" , nullResp); clearControls(); }
function listStatClearCacheReq(){ sendRequest("POST", "<list_stat_clear_cache/>", nullResp); clearControls(); }

/**
 * Search the specified div list for the member with the specified id and return
 * its index. The div id is determined from the value of the div input with the
 * specified index.
 */
function findDivId(listDiv, intDivId, intInputIndex){
 	var intIndex = -1;
 	for(i in listDiv){
 		var intIdCur = listDiv[i].getElementsByTagName("input")[intInputIndex].value;
 		if( intDivId == intIdCur ){ intIndex = i; }
 	}
 	return intIndex;
}

/**
 * When the user changes selected plot data type, update the lists of variables
 * appropriately and reset the controls.
 */
function updatePlotData(){

	// update the data members and lists accordingly
	var strPlotData = getSelected( document.getElementById("selPlotData") )[0];
	var strTmpl = getSelected( document.getElementById("selTemplate") )[0];
	if( strPlotData == "Stat" ){
		_strPlotData = (strTmpl == "rhist"? "rhist" : "stat");
		_listVar = _listVarStat;
		_listIndyVar = _listIndyVarStat;
	} else if( strPlotData == "MODE" ){
		_strPlotData = "mode";
		_listVar = _listVarMode;
		_listIndyVar = _listIndyVarMode;
	}
	clearControls();
}

/**
 * Clear all variable/value controls and reset the select lists to the currently
 * selected lists of fcst_var and variables.
 */
function clearControls(){

	// determine the selected template
	var strTemplate = getSelected( document.getElementById("selTemplate") )[0];
	
	if( TMPL_RHIST == _intTmpl ){

		// reset the fixed values
		while( 0 < _listRhistDiv.length ){ removeRhistDiv( _listRhistDiv[0].getElementsByTagName("input")[1].value); }
		
	} else {
		
		// reset the dep stat controls
		var intDepId = _listDep1Div[0].getElementsByTagName("input")[1].value;
		clearDepStat(intDepId);
		while( 1 < _listDep1Div.length ){ removeDep1Var(_listDep1Div[1].getElementsByTagName("input")[1].value); }
		while( 0 < _listDep2Div.length ){ removeDep2Var(_listDep2Div[0].getElementsByTagName("input")[1].value); }
		listFcstVar1Req(intDepId);
		_listDep1Div[0].getElementsByTagName("td")[3].style.display = "none";
		_listDep1Div[0].getElementsByTagName("td")[4].style.display = "none";
		_listDep1Div[0].getElementsByTagName("td")[5].style.display = "none";
	
		// reset the series controls
		while( 0 < _listSeries1Div.length ){ removeSeries1Div( _listSeries1Div[0].getElementsByTagName("input")[1].value); }
		while( 0 < _listSeries2Div.length ){ removeSeries2Div( _listSeries2Div[0].getElementsByTagName("input")[1].value); }
	
		// reset the select field variable list
		var selField = document.getElementById("selField");
		clearSelect(selField);
		fillSelect(selField, _listVar);
		addSeries1Div();
		document.getElementById("lnkRemoveFieldVal0").style.display = "none";
		
		// reset the fixed values
		while( 0 < _listFixDiv.length ){ removeFixDiv( _listFixDiv[0].getElementsByTagName("input")[1].value); }
		
		// reset the agg_stat controls
		var divAggStat = document.getElementById("divAggStat");
		divAggStat.getElementsByTagName("input")[0].checked = false;
		updateAggStat();	
	
		// reset the indep controls
		var selIndyVar = document.getElementById("selIndyVar");
		clearSelect(selIndyVar);
		fillSelect(selIndyVar, _listIndyVar);
		clearIndyVal();
	}
	
	listFcstVar1Req(0);
}

/**
 * The specified td element is assumed to contain the standard formatting
 * control structure, and the setting XML tag name is extracted and returned.
 */
function getFmtTag(tdFmt){
	var strRet = "";
	var listTd = tdFmt.getElementsByTagName("td");
	if( 1 < listTd.length ){ strRet = listTd[2].innerHTML; }
	return strRet;
}

/**
 * The specified td element is assumed to contain the standard formatting
 * control structure, and the setting control value is extracted and returned.
 */
function getFmtVal(tdFmt){
	var strVal = "";
	var txtVal = tdFmt.getElementsByTagName("input")[0];
	var boolTxt = ( undefined != txtVal && "none" != txtVal.style.display );
	var selVal = tdFmt.getElementsByTagName("select")[0];
	if( boolTxt ){ strVal = txtVal.value; }
	else         { strVal = getSelected(selVal)[0]; }
	return strVal;	
}

/**
 * Set the label and tag of the formatting controls in the specified td element
 * to the specified values
 */
function setFmtLabelTag(tdFmt, label, tag){
	var listTd = tdFmt.getElementsByTagName("td");
	if( 1 < listTd.length ){
		listTd[0].innerHTML = label;
		listTd[2].innerHTML = tag;
	}
}

/**
 * Set the value of the formatting control in the specified td element to the
 * specified value
 */
function setFmtVal(tdFmt, val){
	var txtVal = tdFmt.getElementsByTagName("input")[0];
	var boolTxt = ( undefined != txtVal && "none" != txtVal.style.display );
	if( boolTxt ){ txtVal.value = val; }
	else         { setSelected(tdFmt.getElementsByTagName("select")[0], val); }
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Template Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * When the plot template is switched, configure the visibility of the controls
 */
function updateTmpl(){

	// update the template type data member
	var strTmpl = getSelected( document.getElementById("selTemplate") )[0];
	if     ( null != strTmpl.match( /^series_plot$/ ) ){ _intTmpl = TMPL_SERIES_PLOT; } 
	else if( null != strTmpl.match( /^bar_plot$/    ) ){ _intTmpl = TMPL_BAR_PLOT;    } 
	else if( null != strTmpl.match( /^box_plot$/    ) ){ _intTmpl = TMPL_BOX_PLOT;    } 
	else if( null != strTmpl.match( /^rhist$/       ) ){ _intTmpl = TMPL_RHIST;       } 
	
	// default visibility settings for the series_plot template
	var boolY1 = true;
	var boolY2 = true;
	var boolY2NA = false;
	var boolFix = true;
	var boolRhist = false;
	var boolIndy = true;
	var boolAggStat = true;
	var boolAggStatNA = false;
	
	// for box_plot and bar_plot templates, hide the Y2 controls and agg_stat
	if( TMPL_BOX_PLOT == _intTmpl || TMPL_BAR_PLOT == _intTmpl ){
		boolY2NA = true;
		boolAggStatNA = true;		
	} else if( TMPL_RHIST == _intTmpl ){
		boolY1 = false;
		boolY2 = false;
		boolFix = false;
		boolRhist = true;
		boolIndy = false;
		boolAggStat = false;
	}
	
	// configure the visibility of the Y1, Y2, fix and indy controls
	document.getElementById("tdY1").style.display    		= boolY1?		"table-cell" : "none";
	document.getElementById("tdY2").style.display    		= boolY2?		"table-cell" : "none";
	document.getElementById("spanY2NA").style.display    	= boolY2NA?		"inline" : "none";
	document.getElementById("divDep2").style.display     	= boolY2NA?		"none" : "inline";
	document.getElementById("divSeries2").style.display  	= boolY2NA?		"none" : "inline";
	document.getElementById("divFix").style.display    		= boolFix?		"inline" : "none";
	document.getElementById("divRhist").style.display   	= boolRhist?	"inline" : "none";
	document.getElementById("tdIndy").style.display    		= boolIndy?		"table-cell" : "none";
	
	// configure the visibility of the agg_stat controls
	document.getElementById("tdAggStat").style.display		= boolAggStat?	"table-cell" : "none";
	document.getElementById("divAggStat").getElementsByTagName("input")[0].checked = false;
	updateAggStat();	
	document.getElementById("spanAggStatNA").style.display  = boolAggStatNA?"inline" : "none";
	document.getElementById("chkAggStat").style.display     = boolAggStatNA?"none" : "inline";
	document.getElementById("spanAggStat").style.display    = boolAggStatNA?"none" : "inline";
	
	// configure the visibility of the plot formatting controls
	var listFmtAxis = document.getElementById("divFmtAxis").getElementsByTagName("td");
	listFmtAxis[4].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[5].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[6].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[7].style.display							= boolY2NA?		"none" : "inline";
	
	if( TMPL_RHIST == _intTmpl ){
		buildSeriesDivRhist();
		updateFmtPlot();
	} else { buildSeriesDiv(); }

	updatePlotData();
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Dependent Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function addDep1(){ addDep(1); }
function removeDep1Var(intDepId){ removeDepVar(1, intDepId); }
function listFcstVar1Req(intDepId){ listFcstVarReq(intDepId, listFcstVar1Resp); }
function listFcstVar1Resp(strResp){ selectFieldResp(strResp, _listDep1Div, 1, 0); }

function addDep2(){ addDep(2); }
function removeDep2Var(intDepId){ removeDepVar(2, intDepId); }
function listFcstVar2Req(intDepId){ listFcstVarReq(intDepId, listFcstVar2Resp); }
function listFcstVar2Resp(strResp){ selectFieldResp(strResp, _listDep2Div, 1, 0); }

/**
 * Create a GUI control cluster for specifying a dependent variable fcst_var and
 * stat(s)
 */
function addDep(intY){

	var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
	
 	// clone the dependent variable controls
 	var intDepId = _intDepIdNext++;
 	var divDep = _listDep1Div[0].cloneNode(true);

 	// update the components of the cloned fixed value controls
 	divDep.id = "divDep" + intDepId;
 	var selFcstVar = divDep.getElementsByTagName("select")[0];
 	selFcstVar.id = "selFcstVar" + intDepId;
 	selFcstVar.setAttribute("onchange", "javascript:clearDepStat(" + intDepId + ")");
 	if( _boolIE ){ selFcstVar.attachEvent("onchange", new Function("clearDepStat(" + intDepId + ")")); }
 	var btnFcstVar = divDep.getElementsByTagName("input")[0];
 	btnFcstVar.setAttribute("onclick", "javascript:selectFcstVarReq(" + intDepId + ")");
 	if( _boolIE ){ btnFcstVar.attachEvent("onclick", new Function("selectFcstVarReq(" + intDepId + ")")); }
 	var selStat = divDep.getElementsByTagName("select")[1];
 	clearSelect(selStat);
 	selStat.style.display = "none";
 	selStat.id = "selStat" + intDepId;
 	selStat.setAttribute("onchange", "javascript:updateDepStat(" + intDepId + ")");
 	if( _boolIE ){ selStat.attachEvent("onchange", new Function("updateDepStat(" + intDepId + ")")); }
 	divDep.getElementsByTagName("td")[3].style.display = "none";
 	divDep.getElementsByTagName("td")[4].style.display = "none";
 	divDep.getElementsByTagName("td")[5].style.display = "none";
 	var chkDiff = divDep.getElementsByTagName("input")[2];
 	chkDiff.setAttribute("onclick", "javascript:modeStatDiffChk(" + intDepId + ")");
 	if( _boolIE ){ chkDiff.attachEvent("onclick", new Function("modeStatDiffChk(" + intDepId + ")")); }
 	var lnkDep = divDep.getElementsByTagName("a")[0];
 	lnkDep.setAttribute("onclick", "javascript:removeDep" + intY + "Var(" + intDepId + ")");
 	if( _boolIE ){ lnkDep.attachEvent("onclick", new Function("removeDep" + intY + "Var(" + intDepId + ")")); }
 	divDep.getElementsByTagName("span")[1].style.display = "inline";
 	divDep.getElementsByTagName("input")[1].value = "" + intDepId;

 	// add the new fixed variable value section to the page
 	listDepDiv.push(divDep);
	var divDepParent = document.getElementById("divDep" + intY);
	var divImgParent = document.getElementById("imgDep" + intY);
	// console("addDep(" + intY + ")\n divDepParent: " + divDepParent + "\n
	// divImgParent: " + divImgParent + "\n\n");
 	divDepParent.insertBefore(divDep, divImgParent);

 	// ensure the first remove link is visible
 	listDepDiv[0].getElementsByTagName("span")[1].style.display = "inline";
}

/**
 * Remove the specified dep div from the list of dependent variable controls
 */
function removeDepVar(intY, intDepId){
	var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
	removeFieldValDiv(intDepId, listDepDiv, 1);
 	if( 1 == intY && 1 == listDepDiv.length ){ listDepDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
 	buildSeriesDiv();
}

/**
 * List the fcst_var database field values, and populate the dependent variable
 * fcst_var select with the results
 */
function listFcstVarReq(intDepId, fnListFcstVarResp){
	sendRequest("POST", 
				"<list_val>" +
					"<id>" + intDepId + "</id>" +
					"<" + _strPlotData + "_field>FCST_VAR</" + _strPlotData + "_field>" + 
				"</list_val>", 
				fnListFcstVarResp);
}

/**
 * List the statistics available for the specified forecast variable and
 * populate the statistics select with the results
 */
function selectFcstVarReq(intId){
	
	// query the database for stat_header stats, if appropriate
	if( _strPlotData == "stat" ){
		var selFcstVar = document.getElementById("selFcstVar" + intId);
		sendRequest("POST",
					"<list_stat>" +
						"<id>" + intId + "</id>" +
						"<" + _strPlotData + "_fcst_var>" + 
							selFcstVar.options[selFcstVar.selectedIndex].text + 
						"</" + _strPlotData + "_fcst_var>" +
					"</list_stat>",
					selectFcstVarResp);
	}
	
	// otherwise, use the static list of mode stats
	else {
		var selFcstVar = document.getElementById("selStat" + intId);
		fillSelect(selFcstVar, _listStatMode);
		selFcstVar.style.display = "inline";
	}
}
function selectFcstVarResp(strResp){

	// parse the response
	var resp = parseListValResp(strResp, "stat");
	if( null == resp ){ return; }

	// populate and display the stats select control
	var selFcstVar = document.getElementById("selStat" + resp.id);
	fillSelect(selFcstVar, resp.vals);
	selFcstVar.style.display = "inline";
}

/**
 * Build an XML criteria string for a <list_val> command which contains the list
 * of currently selected dependent variable fcst_var values
 */
function buildFcstVarCrit(intY){
	
	// determine the list of dep divs to consider
	var listDepDiv;
	if     ( 1 == intY ){ listDepDiv = _listDep1Div; }
	else if( 2 == intY ){ listDepDiv = _listDep2Div; }
	else                { listDepDiv = _listDep1Div.concat(_listDep2Div); }
	
	// add the fcst_var from each dep div to the list
	var strFixCrit = "<field name=\"FCST_VAR\">";
	for(i in listDepDiv){
		var selFcstVar = listDepDiv[i].getElementsByTagName("select")[0];
		strFixCrit += "<val>" + selFcstVar.options[selFcstVar.selectedIndex].text + "</val>";
	}
	strFixCrit += "</field>";
	return strFixCrit;
}

function buildFcstVarStatCrit(intY){
	
	// determine the list of dep divs to consider
	var listDepDiv;
	if     ( 1 == intY ){ listDepDiv = _listDep1Div; }
	else if( 2 == intY ){ listDepDiv = _listDep2Div; }
	else                { listDepDiv = _listDep1Div.concat(_listDep2Div); }
	
	// for each fcst_var in the dep div list, build a list of stats
	var strFixCrit = "<stat>";
	for(i in listDepDiv){
		var selFcstVar = listDepDiv[i].getElementsByTagName("select")[0];
		var listStat = getSelected( listDepDiv[i].getElementsByTagName("select")[1] );
		strFixCrit += "<fcst_var name=\"" + selFcstVar.options[selFcstVar.selectedIndex].text + "\">";
		for(intStat in listStat){ strFixCrit += "<val>" + listStat[intStat] + "</val>"; }		
		strFixCrit += "</fcst_var>";
	}
	strFixCrit += "</stat>";
	return strFixCrit;
}

/**
 * Clears the dep stat select control of the specified index when a change is
 * made to the fcst_var select
 */
function clearDepStat(intIndex){
	var selStat = document.getElementById("selStat" + intIndex);
	clearSelect(selStat);
	selStat.style.display = "none";
}

/**
 * When a dep stat selection is changed, this method is called to handle the
 * toggling of the mode checkbox visibility and the updating of the series
 * format controls.
 */
function updateDepStat(id){
	var divDep = getDepDiv(id);

	// determine the visibility of the mode checkboxes
	var listStatSel = getSelected( divDep.getElementsByTagName("select")[1] );
	var boolVisMode = "mode" == _strPlotData && 0 < listStatSel.length &&
					  null == listStatSel[0].match( /^RATIO_.+/ ) && 
					  null == listStatSel[0].match( /^AREARAT_.+/ ) && 
					  null == listStatSel[0].match( /^OBJ.+/ );
	var boolSingle = (-1 < listSearch(listStatSel[0], _listStatModeSingle));
	var boolAcov = null != listStatSel[0].match( /^ACOV$/ )
	var boolVisFO = boolVisMode && boolSingle;
	var boolVisFODif = boolVisFO && !boolAcov;
	var boolVisSC = boolVisMode && !boolAcov;
	var boolVisMU = boolVisMode && !boolAcov;

	// toggle the visibility of the checkbox table cells
	divDep.getElementsByTagName("span")[0].style.display = boolVisMode?		"inline" : "none";	
	divDep.getElementsByTagName("td")[3].style.display = boolVisFO?			"table-cell" : "none";
	divDep.getElementsByTagName("td")[4].style.display = boolVisSC?			"table-cell" : "none";
	divDep.getElementsByTagName("td")[5].style.display = boolVisMU?			"table-cell" : "none";
	
	// change the state of the Difference checkbox, if appropriate
	divDep.getElementsByTagName("input")[2].disabled = !boolVisFODif;
	if( !boolVisFODif ){
		divDep.getElementsByTagName("input")[2].checked = false;
		divDep.getElementsByTagName("input")[3].disabled = false;
		divDep.getElementsByTagName("input")[4].disabled = false;
	}

	buildSeriesDiv();
}

function modeStatDiffChk(id){
	var divDep = getDepDiv(id);
	
	var boolDiff = divDep.getElementsByTagName("input")[2].checked;
	divDep.getElementsByTagName("input")[3].checked = true;
	divDep.getElementsByTagName("input")[3].disabled = boolDiff;
	divDep.getElementsByTagName("input")[4].checked = true;
	divDep.getElementsByTagName("input")[4].disabled = boolDiff;	
}

/**
 * Search the lists of dep1 and dep2 divs for the one whose value matches the
 * input value and return it
 */
function getDepDiv(id){
	var listDepDiv = _listDep1Div.concat(_listDep2Div);
	var intIndex = findDivId(listDepDiv, id, 1);
	if( 0 > intIndex ){
		console("getDepDiv() - ERROR: index for div id " + resp.id + " not found\n\n");
		return;
	}
	return listDepDiv[intIndex];
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Field Value Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a field val div of the specified category and add it to the from and
 * to the input controls list
 */
function addFieldValDiv(category, listDiv){

	// use the next field val id
	var intId = _intFieldValIdNext++;

	// clone the field value div and update the controls
	var divFieldVal = _divFieldVal.cloneNode(true);
	divFieldVal.id = "divFieldVal" + intId;
	var selVar = divFieldVal.getElementsByTagName("select")[0];
	selVar.id = "selFieldVal" + intId;
	selVar.setAttribute("onchange", "javascript:clearFieldVal(" + intId + ")");
 	if( _boolIE ){ selVar.attachEvent("onchange", new Function("clearFieldVal(" + intId + ")")); }
	var btnFieldVal = divFieldVal.getElementsByTagName("input")[0];
	btnFieldVal.setAttribute("onclick", "javascript:select" + category + "VarReq(" + intId + ")");
	if( _boolIE ){ btnFieldVal.attachEvent("onclick", new Function("select" + category + "VarReq(" + intId + ")")); }
	var selVal = divFieldVal.getElementsByTagName("select")[1];
	clearSelect(selVal);
	selVal.style.display = "none";
	selVal.id = "selVal" + intId;
	var tdFieldMove = divFieldVal.getElementsByTagName("td")[1];
	tdFieldMove.id = "tdFieldMove" + intId;
	tdFieldMove.style.display = "none";
	var imgFieldUp = divFieldVal.getElementsByTagName("img")[0];
	imgFieldUp.setAttribute("onclick", "javascript:move" + category + "FieldUp(" + intId + ")");
	if( _boolIE ){ imgFieldUp.attachEvent("onclick", new Function("move" + category + "FieldUp(" + intId + ")")); }
	var imgFieldDown = divFieldVal.getElementsByTagName("img")[1];
	imgFieldDown.setAttribute("onclick", "javascript:move" + category + "FieldDown(" + intId + ")");
	if( _boolIE ){ imgFieldDown.attachEvent("onclick", new Function("move" + category + "FieldDown(" + intId + ")")); }
	divFieldVal.getElementsByTagName("span")[1].id = "lnkRemoveFieldVal" + intId;
	var lnkRemove = divFieldVal.getElementsByTagName("a")[0];
	lnkRemove.setAttribute("onclick", "javascript:remove" + category + "Div(" + intId + ")");
	if( _boolIE ){ lnkRemove.attachEvent("onclick", new Function("remove" + category + "Div(" + intId + ")")); }
	divFieldVal.getElementsByTagName("input")[1].value = "" + intId;
	divFieldVal.style.display = "inline";

	// add the new div to the input controls list and add it to the form
	listDiv.push( divFieldVal );
	document.getElementById("div" + category).insertBefore(divFieldVal, document.getElementById("img" + category));
}

/**
 * Remove the field val div with the specified id from the from and the controls
 * list. The index of the hidden id field is specified by intInputId.
 */
function removeFieldValDiv(intId, listDiv, intInputId){

	// attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, intInputId);
	if( 0 > intIndex ){
		console("removeFieldValDiv() - WARNING: div " + intId + " not found\n\n");
		return;
	}

 	// remove the specified div from the list and hide it
 	var divFieldVal =  listDiv[intIndex];
 	listDiv.splice(intIndex, 1);
 	divFieldVal.style.display = "none";

}

/**
 * Build a list_val server request for the field val with the specified id in
 * the specified controls list. The request includes the fixed value criteria up
 * to the specified index. The response xml is passed to the specified response
 * function. If the input control flag is set to -1, the criteria for rhist is
 * used. Otherwise, if the control flag is 1 or 2, the criteria for only the
 * corresponding axis only is used.
 */
function selectFieldReq(intId, listDiv, intFixEnd, fnResp, intY){

	// attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, 1);
	if( 0 > intIndex ){
		console("selectFieldReq() - ERROR: div " + intId + " not found\n\n");
		return;
	}

	// gather the criteria
	var strFcstVarCrit = "";
	var strFixCrit = "";
	if( -1 == intY ){
		strFixCrit = buildRhistCrit(intFixEnd);
	} else {
		if(  1 == intY || 2 == intY ){ strFcstVarCrit = buildFcstVarStatCrit(intY); }
		else                         { strFcstVarCrit = buildFcstVarStatCrit(); }		
		strFixCrit = buildFixCrit(intFixEnd);
	}

	// build a list_val request for the selected field
	var selField = listDiv[intIndex].getElementsByTagName("select")[0];
	var strField = selField.options[selField.selectedIndex].text;
	sendRequest("POST",
				"<list_val>" +
					"<id>" + intId + "</id>" +
					"<" + _strPlotData + "_field>" + strField + "</" + _strPlotData + "_field>" +
					strFcstVarCrit + 
					strFixCrit + 
				"</list_val>",
				fnResp);
}

/**
 * Handle the specified <list_val> response XML (strResp), populating the div
 * from the input list with the id contained in the response <id>. The div id is
 * determined by examining the hidden field with the specified index
 * (intIdIndex). The select control of the specified index (intSelIndex) will be
 * populated. The field ordering arrows will be displayed as specified.
 */
function selectFieldResp(strResp, listDiv, intIdIndex, intSelIndex, showArrows){

	// parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	// retrieve and validate the div from the input div list
	var intIndex = findDivId(listDiv, resp.id, intIdIndex);
	if( 0 > intIndex ){
		console("selectFieldResp() - ERROR: index for div id " + resp.id + " not found\n\n");
		return;
	}

	// add the field values to the value select list
	var selVal = listDiv[intIndex].getElementsByTagName("select")[intSelIndex];
	selVal.style.display = "inline";
	if( showArrows ){
		listDiv[intIndex].getElementsByTagName("td")[1].style.display = "table-cell";
	}
	fillSelect(selVal, resp.vals);

}

/**
 * Clears the specified field value select control of the specified index when a
 * change is made to the field var
 */
function clearFieldVal(intId){
	var selVal = document.getElementById("selVal" + intId);
	clearSelect(selVal);
	selVal.style.display = "none";
	document.getElementById("tdFieldMove" + intId).style.display = "none";
}

/**
 * Move the currently selected item in the select value control in the specified
 * list with the specified id up one place. This function will not attempt to do
 * anything if there are fewer than two items in the select list, or if the
 * first item is selected.
 */
function moveFieldUp(listDiv, intId){
	
	// find the selected element, and bail if there is not exactly one
	var selVal = document.getElementById("selVal" + intId);
	var listSel = getSelected(selVal);
	var intSel = selVal.selectedIndex;
	if( 2 > selVal.options.length || 1 != listSel.length || intSel == 0 ){ return; }
	
	// move the selected option
	// var optMove = selVal.options[intSel].cloneNode(true);
	var optMove = document.createElement("option");
	optMove.text = selVal.options[intSel].text;
	selVal.remove(intSel);
	if( _boolIE ){ selVal.add(optMove, intSel - 1); }
	else         { selVal.add(optMove, selVal.options[intSel - 1]); }
	selVal.selectedIndex = intSel - 1;
}

/**
 * Move the currently selected item in the select value control in the specified
 * list with the specified id down one place. This function will not attempt to
 * do anything if there are fewer than two items in the select list, or if the
 * list item is selected.
 */
function moveFieldDown(listDiv, intId){

	// find the selected element, and bail if there is not exactly one
	var selVal = document.getElementById("selVal" + intId);
	var listSel = getSelected(selVal);
	var intSel = selVal.selectedIndex;
	if( 2 > selVal.options.length || 1 != listSel.length || intSel == selVal.options.length - 1 ){ return; }
	
	// move the selected option
	var optMove = document.createElement("option");
	optMove.text = selVal.options[intSel].text;
	selVal.remove(intSel);
	if( _boolIE ){ selVal.add(optMove, intSel >= selVal.options.length - 1? null : intSel + 1); }
	else         { selVal.add(optMove, intSel >= selVal.options.length - 1? null : selVal.options[intSel + 1]); }
	selVal.selectedIndex = intSel + 1;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Series Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add and remove a series1 div and process select requests and
 * responses
 */
function addSeries1Div()               { addSeriesDiv(1); }
function removeSeries1Div(intId)       { removeSeriesDiv(1, intId); }
function selectSeries1VarReq(intId)    { selectFieldReq(intId, _listSeries1Div, _listFixDiv.length - 1, selectSeries1VarResp, 1); }
function selectSeries1VarResp(strResp) { selectFieldResp(strResp, _listSeries1Div, 1, 1, true); }
function moveSeries1FieldUp(intId)     { moveFieldUp(_listSeries1Div, intId); }
function moveSeries1FieldDown(intId)   { moveFieldDown(_listSeries1Div, intId); }

/**
 * Handlers to add and remove a series2 div and process select requests and
 * responses
 */
function addSeries2Div()               { addSeriesDiv(2); }
function removeSeries2Div(intId)       { removeSeriesDiv(2, intId); }
function selectSeries2VarReq(intId)    { selectFieldReq(intId, _listSeries2Div, _listFixDiv.length - 1, selectSeries2VarResp, 2); }
function selectSeries2VarResp(strResp) { selectFieldResp(strResp, _listSeries2Div, 1, 1, true); }
function moveSeries2FieldUp(intId)     { moveFieldUp(_listSeries2Div, intId); }
function moveSeries2FieldDown(intId)   { moveFieldDown(_listSeries2Div, intId); }

/**
 * Build and add a series div for the specified series with configured controls
 */
function addSeriesDiv(intSeries){
	// determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	// add a field val div and modify its components for duty as a series div
	addFieldValDiv("Series" + intSeries, listSeriesDiv);
	var intSeriesIndex = listSeriesDiv.length - 1;
	var selVal = listSeriesDiv[ intSeriesIndex ].getElementsByTagName("select")[1];
	selVal.setAttribute("onchange", "javascript:buildSeriesDiv()");
 	if( _boolIE ){ selVal.attachEvent("onchange", new Function("buildSeriesDiv()")); }
	for(i in listSeriesDiv){ listSeriesDiv[i].getElementsByTagName("span")[1].style.display = "inline"; }
}

/**
 * Dispose of and hide the series div of the specified series with the specified
 * id
 */
function removeSeriesDiv(intSeries, intId){
	// determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	// dispose of and hide the series div
	removeFieldValDiv(intId, listSeriesDiv, 1);
 	if( 1 == listSeriesDiv.length ){ listSeriesDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
 	buildSeriesDiv();
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Fixed Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add, remove and populate the fixed variable controls
 */
function addFixVar()         { addFieldValDiv("Fix", _listFixDiv);       }
function removeFixDiv(intId) { removeFieldValDiv(intId, _listFixDiv, 1); }
function selectFixVarReq(intId){
	var intIndexCrit = findDivId(_listFixDiv, intId, 1);
	if( 0 > intIndexCrit ){
		console("selectFixVarReq() - ERROR: index for id " + intId + " not found\n");
		return;
	}
	selectFieldReq(intId, _listFixDiv, intIndexCrit - 1, selectFixVarResp);
}
function selectFixVarResp(strResp){ selectFieldResp(strResp, _listFixDiv, 1, 1, false); }

/**
 * Construct a string of database field and value criteria that reflects the
 * selected fields and values in the fixed values controls 
 *
function buildFixCrit(endIndex){
	var strFixCrit = "";
	for(i=0; i <= endIndex; i++){
		var divFixCrit = _listFixDiv[i]; 
		var selFixCrit = divFixCrit.getElementsByTagName("select")[0]; 
		var strFixCritCur = "<field name=\"" + selFixCrit.options[ selFixCrit.selectedIndex ].text + "\">";
		var listFixCritVal = getSelected( divFixCrit.getElementsByTagName("select")[1] );
		for(var j=0; j < listFixCritVal.length; j++){ strFixCritCur += "<val>" + listFixCritVal[j] + "</val>"; }
		strFixCritCur += "</field>";
		if( 0 < listFixCritVal.length ){ strFixCrit += strFixCritCur; }
	}
	return strFixCrit; 
}
 */

/**
 * Construct a string of database field and value criteria that reflects the
 * selected fields and values in the values controls of the input div list
 */
function buildFieldValCrit(listDiv, endIndex){
	var strCrit = "";
	for(i=0; i <= endIndex; i++){
		var divCrit = listDiv[i];
		var selCrit = divCrit.getElementsByTagName("select")[0];
		var strCritCur = "<field name=\"" + selCrit.options[ selCrit.selectedIndex ].text + "\">";
		var listCritVal = getSelected( divCrit.getElementsByTagName("select")[1] );
		for(var j=0; j < listCritVal.length; j++){ strCritCur += "<val>" + listCritVal[j] + "</val>"; }
		strCritCur += "</field>";
		if( 0 < listCritVal.length ){ strCrit += strCritCur; }
	}
	return strCrit;
}
function buildFixCrit(endIndex){ return buildFieldValCrit(_listFixDiv, endIndex); }
function buildRhistCrit(endIndex){ return buildFieldValCrit(_listRhistDiv, endIndex); }



/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Rhist Fixed Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add, remove and populate the rhist fixed variable controls
 */
function addRhistVar(){
	// add the control group for the field value selection
	addFieldValDiv("Rhist", _listRhistDiv);
	
	// add rank size to the list of fields
	var selField = _listRhistDiv[_listRhistDiv.length-1].getElementsByTagName("select")[0];
	clearSelect(selField);
	fillSelect(selField, _listVarRhist);
}
function removeRhistDiv(intId) { removeFieldValDiv(intId, _listRhistDiv, 1); }
function selectRhistVarReq(intId){
	var intIndexCrit = findDivId(_listRhistDiv, intId, 1);
	if( 0 > intIndexCrit ){
		console("selectRhistVarReq() - ERROR: index for id " + intId + " not found\n");
		return;
	}
	selectFieldReq(intId, _listRhistDiv, intIndexCrit - 1, selectRhistVarResp, -1);
}
function selectRhistVarResp(strResp){ selectFieldResp(strResp, _listRhistDiv, 1, 1, false); }


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Independent Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Build and run a request for values for the currently selected independent
 * variable and populate the independent variable value list with the results
 */
function selectIndyVarReq(){

	// build a list_val request for the selected independent field
	var strFcstVarCrit = buildFcstVarStatCrit();
	var strFixCrit = buildFixCrit(_listFixDiv.length - 1);
	var strField = getSelected( document.getElementById("selIndyVar") )[0];
	sendRequest("POST",
				"<list_val>" +
					"<id>0</id>" + 
					"<" + _strPlotData + "_field>" + strField + "</" + _strPlotData + "_field>" +
					strFcstVarCrit + 
					strFixCrit + 
				"</list_val>",
				selectIndyVarResp);
}
function selectIndyVarResp(strResp){

	// parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	// hide all currently display indy val controls
	clearIndyVal();
	
	// add a indy val control group for each indy value
	var divIndy = document.getElementById("divIndy");
	var strField = getSelected( document.getElementById("selIndyVar") )[0];
	for( i in resp.vals ){
		var trIndyVal = tabIndyVal.insertRow(tabIndyVal.rows.length);

		// build a control set for the independent variable value
		var tdIndyChk = trIndyVal.insertCell(0);
		tdIndyChk.appendChild( document.getElementById("spanIndyValChk").cloneNode(true) );
		var tdIndyLab = trIndyVal.insertCell(1);
		tdIndyLab.appendChild( document.getElementById("spanIndyValLab").cloneNode(true) );
		trIndyVal.getElementsByTagName("span")[1].innerHTML = resp.vals[i];
		trIndyVal.style.display = "table-row";
		
		// set the default label
		var strLabel = resp.vals[i];
		if( "FCST_LEAD" == strField ){
			var listParse = strLabel.match( /(\d+)0000$/ );
			if( null != listParse ){ strLabel = listParse[1]; }
		}
		trIndyVal.getElementsByTagName("input")[1].value = strLabel;
	}
	document.getElementById("spanIndyCheck").style.display = "inline";
	
	// update the plot formatting to accommodate date series
	updateFmtPlot();
}

/**
 * Remove all rows of the indy table that contain values
 */
function clearIndyVal(){
	// hide all currently display indy val controls
	var tabIndyVal = document.getElementById("tabIndyVal");
	while( 1 < tabIndyVal.rows.length ){ tabIndyVal.deleteRow(tabIndyVal.rows.length - 1); }
	document.getElementById("spanIndyCheck").style.display = "none";
}

/**
 * Checks or unchecks all indy values, as specified
 */
function indyCheck(boolCheck){
	var tabIndyVal = document.getElementById("tabIndyVal");
	for(var i=1; i < tabIndyVal.rows.length; i++){
		var chkIndy = tabIndyVal.rows[i].getElementsByTagName("input")[0];
		chkIndy.checked = boolCheck;
	}
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Plot Formatting Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Construct plot formatting controls with the specified label, value and type.
 * The currently supported values of type are "txt" and "bool". The controls are
 * placed at the next available place for their respective type.
 */
function addFmtPlot(label, tag, value, type){

	var boolTypeTxt = (type == "txt");
	var intFmtIndex = (boolTypeTxt? _intFmtPlotTxtIndex : _intFmtPlotBoolIndex);
	var intCol = intFmtIndex % _intNumFmtPlotCol
	var tabFmt = document.getElementById( boolTypeTxt? "tabFmtPlotTxt" : "tabFmtPlotBool" );
	var tdFmt;

	// if the index is zero, populate the existing cell
	if( 0 == intFmtIndex ){
		tdFmt = tabFmt.rows[0].cells[0];
	}

	// if the table requires a new row, create one
	else if( 0 == intCol ){
		var trFmt = tabFmt.insertRow( tabFmt.rows.length );
		for(var i=0; i < _intNumFmtPlotCol; i++){
			tdFmt = trFmt.insertCell(i);
			tdFmt.align = "right";
			tdFmt.style.width = _strFmtPlotWidth;
		}
		tdFmt = trFmt.cells[0];
	}

	// if a new row is not required, get the next available cell on the current row
	else {
		tdFmt = tabFmt.rows[ tabFmt.rows.length - 1 ].cells[intCol];
	}

	// populate the controls with the specified input information
	if( 0 != intFmtIndex ){
		tdFmt.appendChild( document.getElementById( boolTypeTxt? "spanFmtPlotTxt" : "spanFmtPlotBool" ).cloneNode(true) );
	}
	
	// update the label and tag
	setFmtLabelTag(tdFmt, label, tag);

	// configure and populate the selection control for text settings
	var listFmtTd = tdFmt.getElementsByTagName("td");
	if( boolTypeTxt ){
		var boolSel = (value instanceof Array);
		var selVal = listFmtTd[1].getElementsByTagName("select")[0];
		var txtVal = listFmtTd[1].getElementsByTagName("input")[0];
		selVal.style.display = (boolSel? "inline" : "none");
		txtVal.style.display = (boolSel? "none" : "inline");
		if( boolSel ){ fillSelect(selVal, value); }
		else         { txtVal.value = value; }
		_intFmtPlotTxtIndex++;
	} 
	
	// configure and populate the select control for bool settings
	else {
		tdFmt.getElementsByTagName("select")[0].selectedIndex = ( "true" == value? 0 : 1 );
		_intFmtPlotBoolIndex++;
	}
}

/**
 * Toggle the visibility of the plot formatting controls
 */
function handleFmtPlotTxtDisp(){ handleFmtDisp("PlotTxt"); }
function handleFmtSeriesDisp() { handleFmtDisp("Series");  }
function handleFmtDisp(type){
	var spanDisp = document.getElementById("spanFmt" + type + "Disp");
	var tab = document.getElementById("tabFmt" + type);
	var imgArrow = spanDisp.getElementsByTagName("img")[0];
	var spanMsg = spanDisp.getElementsByTagName("span")[0];
	if( null != spanMsg.innerHTML.match("Show.*") ){
		spanMsg.innerHTML = "Hide Formatting Controls";
		imgArrow.src = imgArrow.src.substring(0, imgArrow.src.lastIndexOf("/") + 1) + "arrow_down.gif";
		tab.style.display = "table";
		if( "Series" == type ){ document.getElementById("btnFmtSeriesDefaults").style.display = "inline"; }
		else                  { document.getElementById("spanFmtPlotCmd").style.display = "inline"; }
	} else {
		spanMsg.innerHTML = "Show Formatting Controls";
		imgArrow.src = imgArrow.src.substring(0, imgArrow.src.lastIndexOf("/") + 1) + "arrow_right.gif";
		tab.style.display = "none";
		if( "Series" == type ){ document.getElementById("btnFmtSeriesDefaults").style.display = "none"; }
		else                  { document.getElementById("spanFmtPlotCmd").style.display = "none"; }
	}
}

/**
 * Update the plot formatting default values, depending on the type of plot
 */
function updateFmtPlot(){

	// determine if vert_plot is true or false
	var tabFmtPlot = document.getElementById("tabFmtPlotBool");
	var selVertPlot = tabFmtPlot.rows[0].cells[2].getElementsByTagName("select")[0];
	var boolVert = (getSelected(selVertPlot)[0] == "true");
	
	// determine if the independent variable is a date type
	var selIndyVar = document.getElementById("selIndyVar");
	var strIndyVar = getSelected(selIndyVar)[0];
	var boolIndyDate = TMPL_RHIST != _intTmpl && 
					   ( strIndyVar == "FCST_VALID_BEG" || strIndyVar == "FCST_INIT_BEG" ||
						 strIndyVar == "FCST_VALID"     || strIndyVar == "FCST_INIT"     );

	// set the default values for each format setting
	var tabFmtPlotTxt = document.getElementById("tabFmtPlotTxt");
	tabFmtPlotTxt.rows[1].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? "c(6, 4, 4, 4)" : (boolIndyDate? "c(13, 4, 3, 4)" : "c(8, 4, 5, 4)"));	// mar
	tabFmtPlotTxt.rows[2].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? "-.4"           : (boolIndyDate? "-.5"            : "-2"           ));	// title_offset
	setSelected(tabFmtPlotTxt.rows[3].cells[0].getElementsByTagName("select")[0], 
		(boolVert? "1"             : (boolIndyDate? "3"              : "1"            )));	// xtlab_orient
	tabFmtPlotTxt.rows[3].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? ".5"            : (boolIndyDate? ".5"             : "-.75"         ));	// xtlab_perp
	tabFmtPlotTxt.rows[3].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? ".6"            : (boolIndyDate? ".9"             : ".5"           ));	// xtlab_horiz
	tabFmtPlotTxt.rows[4].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? "-2"            : (boolIndyDate? "14"             : "2"            ));	// xlab_offset
	tabFmtPlotTxt.rows[5].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? "-1"            : (boolIndyDate? ".5"             : ".5"           ));	// ytlab_perp
	tabFmtPlotTxt.rows[6].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? "2"             : (boolIndyDate? "-2"             : "-2"           ));	// ylab_offset
	tabFmtPlotTxt.rows[9].cells[0].getElementsByTagName("input")[0].value = 
		(boolVert? "1"             : (boolIndyDate? "-.5"            : "-.5"          ));	// x2lab_offset
	tabFmtPlotTxt.rows[11].cells[3].getElementsByTagName("input")[0].value = 
		(boolVert? "c(0, -.17)"    : (boolIndyDate? "c(0, -.48)"     : "c(0, -.25)"   ));	// legend_inset
}

/**
 * Build the list of plot series reflected by the current state of the controls
 */
function buildSeriesDiv(){
	
	if( TMPL_RHIST == _intTmpl ){ return; }
	
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var spanFmtSeriesDisp = document.getElementById("spanFmtSeriesDisp");
	_intNumSeries = 0;
	
	// update the visibility of the series formatting controls
	spanFmtSeriesDisp.style.display = "inline";
	var spanMsg = spanFmtSeriesDisp.getElementsByTagName("span")[0];
	if( null != spanMsg.innerHTML.match("Hide.*") ){
		document.getElementById("tabFmtSeries").style.display = "inline";
	}
	document.getElementById("tabFmtSeriesRhist").style.display = "none";
	
	// build a table containing all current series settings
	var table = new Hashtable();
	for(var intRow=0; intRow < tabFmtSeries.rows.length; intRow += 2){
		var listSpan = tabFmtSeries.rows[intRow].getElementsByTagName("span");
		var listInput = tabFmtSeries.rows[intRow].getElementsByTagName("input");
		if( listInput[0].value == "false" ){ continue; }
		var listFmtTd = getFmtSeriesVal(intRow);
		
		// get the series name and values and put them in the table
		var strSeriesName = listSpan[2].innerHTML + " - " + listSpan[3].innerHTML;
		var strFmt = listInput[0].value;
		strFmt += "|" + listInput[1].checked;
		for(var j=0; j < listFmtTd.length; j++){ strFmt += "|" + getFmtVal(listFmtTd[j]); }
		table.put(strSeriesName, strFmt);
	}

	// clear all existing series, except the first two
	while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }

	// build permutation of the series values
	var listSeries1Perm = permuteSeries(_listSeries1Div, 0, getPlotDiff(1));
	var listSeries2Perm = permuteSeries(_listSeries2Div, 0, getPlotDiff(2));
	
	// build all y1 and y2 series
	for(var intY=1; intY <= 2; intY++){

		var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
		var listSeriesPerm = (1 == intY? listSeries1Perm : listSeries2Perm);
			
		// for each dep div, consider the fcst_var and selected stats
		for(var intDep=0; intDep < listDepDiv.length; intDep++){
	
			// get the dep var information
			var strFcstVar = getSelected( listDepDiv[intDep].getElementsByTagName("select")[0] )[0];
			var listStat = getSelected( listDepDiv[intDep].getElementsByTagName("select")[1] );
	
			// build a series for each combination of fcst_var, stat and series
			// permutation
			for(var intStat=0; intStat < listStat.length; intStat++){
				for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++){
	
					// build the series name
					var strSeriesName =  listSeriesPerm[intSeries] + " " + strFcstVar + " " + listStat[intStat];
	
					var trFmtSeries;
					var tdName;
	
					// if the series is the first to be built, use the existing
					// controls
					if( 0 == _intNumSeries ){
						trFmtSeries = tabFmtSeries.rows[0];
						tdName = trFmtSeries.cells[0];
					}
	
					// otherwise, build a new set of series formatting controls
					else {
						
						// insert the <hr/> between series format controls
						if( 1 == _intNumSeries ){ tabFmtSeries.rows[1].style.display = "table-row"; }
						else {
							var trHR = tabFmtSeries.insertRow( tabFmtSeries.rows.length );
							var tdHR = trHR.insertCell(0);
							tdHR.colSpan = "3";
							tdHR.appendChild( document.getElementById("spanFmtSeriesHR").cloneNode(true) );
						}
	
						// insert a copy of the series format controls
						trFmtSeries = tabFmtSeries.insertRow( tabFmtSeries.rows.length );
	
						var tdName = trFmtSeries.insertCell(0);
						tdName.align = "right";
						tdName.style.width = "350px";
						tdName.style.paddingTop = "20px";
						tdName.appendChild( document.getElementById("spanFmtSeriesName").cloneNode(true) );
	
						var tdFmt1 = trFmtSeries.insertCell(1);
						tdFmt1.align = "right";
						tdFmt1.style.width = "200px";
						tdFmt1.style.paddingTop = "20px";
						tdFmt1.appendChild( document.getElementById("tabFmtSeriesVal1").cloneNode(true) );
						tdFmt1.getElementsByTagName("input")[1].value = "";
	
						var tdFmt2 = trFmtSeries.insertCell(2);
						tdFmt2.align = "right";
						tdFmt2.style.width = "275px";
						tdFmt2.style.paddingTop = "20px";
						tdFmt2.appendChild( document.getElementById("tabFmtSeriesVal2").cloneNode(true) );
					}
	
					// populate the controls with the series name
					var strYSeries = (1 == intY? "Y1" : "Y2") + " Series";
					tdName.getElementsByTagName("span")[2].innerHTML = strSeriesName;
					tdName.getElementsByTagName("span")[3].innerHTML = strYSeries;
					
					// add change handlers to the formatting inputs
					var listInput = trFmtSeries.getElementsByTagName("input");
				 	listInput[1].setAttribute("onclick", "javascript:setFmtSeriesMod(" + _intNumSeries + ", 'true')");
				 	if( _boolIE ){ listInput[1].attachEvent("onclick", new Function("setFmtSeriesMod(" + _intNumSeries + ", 'true')")); }
					for(var i=2; i < listInput.length; i++){
					 	listInput[i].setAttribute("onkeydown", "javascript:setFmtSeriesMod(" + _intNumSeries + ", 'true')");
					 	if( _boolIE ){ listInput[i].attachEvent("onkeydown", new Function("setFmtSeriesMod(" + _intNumSeries + ", 'true')")); }
					}		
					var listSel = trFmtSeries.getElementsByTagName("select");
					for(var i=0; i < listSel.length; i++){
						listSel[i].setAttribute("onchange", "javascript:setFmtSeriesMod(" + _intNumSeries + ", 'true')");
					 	if( _boolIE ){ listSel[i].attachEvent("onchange", new Function("setFmtSeriesMod(" + _intNumSeries + ", 'true')")); }
					}					

					// get format settings for the current field, if available,
					// otherwise use defaults
					var strVal = table.get(strSeriesName + " - " + strYSeries);
					var listVal = _listFmtSeriesDefaults;
					if( undefined != strVal ){ var listVal = strVal.split("|"); }
					
					// apply the settings to the formatting controls
					listInput[0].value = listVal[0];
					tdName.getElementsByTagName("input")[1].checked = (listVal[1] == "true"? true : false);
					var listFmtTd = getFmtSeriesVal( _intNumSeries * 2 );
					for(var i=0; i < listFmtTd.length; i++){
						setFmtVal(listFmtTd[i], listVal[i+2]);
					}

					_intNumSeries++;
					
				}  // end: for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++)
				
			}  // end: for(var intStat=0; intStat < listStat.length; intStat++)
			
		}  // end: for(var intDep=0; intDep < listDepDiv.length; intDep++)
		
	}  // end: for(var intY=1; intY <= 2; intY++)
	
	// set the default color for each series to the appropriate shade of rainbow
	var listColors = rainbow(_intNumSeries);
	for(var i=0; i < _intNumSeries; i++){
		var listFmtTd = getFmtSeriesVal( i * 2 );
		var strColor = getFmtVal( listFmtTd[1] );
		if( "" == strColor ){ setFmtVal(listFmtTd[1], listColors[i]); }
	}
	
	// show or hide the controls, depending on the number of series
	tabFmtSeries.style.display		= (1 > _intNumSeries ? "none" : tabFmtSeries.style.display);
	spanFmtSeriesDisp.style.display	= (1 > _intNumSeries ? "none" : "inline");
	document.getElementById("spanFmtSeriesNum").innerHTML = "# Series: " + _intNumSeries;
	
	// update the agg_stat diff fields
	document.getElementById("txtAggDiff1").value = (getPlotDiff(1)? "TRUE" : "FALSE");
	document.getElementById("txtAggDiff2").value = (getPlotDiff(2)? "TRUE" : "FALSE");
}

/**
 * Build and return a list of the format control td structures in the specified
 * row
 */
function getFmtSeriesVal(row){
	
	// get and validate the requested row
	var listTdFmt = new Array();
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var trFmtSeries = tabFmtSeries.rows[row];
	if( 3 != trFmtSeries.cells.length ){ return listTdFmt; }
	
	// build a list of formatting td elements
	var tabVal1 = trFmtSeries.cells[1].getElementsByTagName("table")[0];
	for(var i=0; i < tabVal1.rows.length; i++){ 
		for(var j=0; j < tabVal1.rows[i].cells.length; j++){ listTdFmt.push(tabVal1.rows[i].cells[j]); }
	}
	var tabVal2 = trFmtSeries.cells[2].getElementsByTagName("table")[0];
	for(var i=0; i < tabVal2.rows.length; i++){ 
		for(var j=0; j < tabVal2.rows[i].cells.length; j++){ listTdFmt.push(tabVal2.rows[i].cells[j]); }
	}
	return listTdFmt;
}

/**
 * Determine the state of the Hide checkbox for the input row and return the
 * state of the control
 */
function getFmtSeriesHide(row){
	
	// get and validate the requested row
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var trFmtSeries = tabFmtSeries.rows[row];
	if( 3 != trFmtSeries.cells.length ){ return listTdFmt; }
	
	var chkHide = trFmtSeries.cells[0].getElementsByTagName("input")[1];
	return chkHide.checked;
}

/**
 * Sets the value of the specified series modification indicator control to the
 * specified value, defaulting to true (modified).
 */
function setFmtSeriesMod(series, val){
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var txtMod = tabFmtSeries.rows[series * 2].getElementsByTagName("input")[0];
	txtMod.value = (undefined != val? val : "true");
}

/**
 * Reset all series controls to their default values and set all series
 * modification indicator controls to false (unmodified).
 */
function setFmtSeriesDefaults(){
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	for(var i=0; i < _intNumSeries; i++){ setFmtSeriesMod(i, "false"); }
	buildSeriesDiv();
}

/**
 * Build a list of all series variable combinations for the specified list of
 * series field divs, starting with the div at the specified index (0 for all
 * permutations). If a difference curve is specified, add it to the series.
 */
function permuteSeries(listSeriesDiv, intIndex, boolDiff){

	if( 1 > listSeriesDiv.length ){ return new Array(); }
	var listVal = getSelected( listSeriesDiv[intIndex].getElementsByTagName("select")[1] );

	// if the index has reached the end of the list, return the selected values
	// from the last control
	if( listSeriesDiv.length == intIndex + 1 ){
		if( boolDiff ){ listVal.splice(listVal.length - 1, 0, "__DIFF__"); }
		return listVal;
	}

	// otherwise, get the list for the next fcst_var and build upon it
	var listValNext = permuteSeries(listSeriesDiv, intIndex + 1, boolDiff);
	if( 1 > listVal.length ){ return listValNext; }
	var listRet = new Array();
	for(var i=0; i < listVal.length; i++){
		for(var j=0; j < listValNext.length; j++){
			listRet.push(listVal[i] + " " + listValNext[j]);
		}
	}
	return listRet;
}

/**
 * Return the boolean value of the format setting for plotN_diff, where N is
 * specified as either 1 or 2
 */
function getPlotDiff(y){
	var tab = document.getElementById("tabFmtPlotBool");
	for(var i=0; i < tab.rows.length; i++){
		for(var j=0; j < tab.rows[i].cells.length; j++){
			var listTd = tab.rows[i].cells[j].getElementsByTagName("td");
			if( 1 > listTd.length ){ continue; }
			if( "plot" + y + "_diff" == getFmtTag(tab.rows[i].cells[j]) ){
				return ( "true" == getFmtVal(tab.rows[i].cells[j]) );
			}
		}
	}
	return false;
}

/**
 * Set up the format series controls for the rhist template
 */
function buildSeriesDivRhist(){
	document.getElementById("tabFmtSeries").style.display = "none";
	document.getElementById("spanFmtSeriesDisp").style.display = "none";
	var tabFmtSeries = document.getElementById("tabFmtSeriesRhist");
	tabFmtSeries.style.display = "inline";

	// clear all existing series, except the first two
	while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }
		
	// show or hide the controls, depending on the number of series
	_intNumSeries = 1;
	tabFmtSeries.style.display = tabFmtSeries.style.display;
	document.getElementById("spanFmtSeriesNum").innerHTML = "# Series: " + _intNumSeries;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Agg Stat Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Update the agg_stat controls according to the enabled checkbox setting
 */
function updateAggStat(){
	var divAggStat = document.getElementById("divAggStat");
	var chkAggStat = divAggStat.getElementsByTagName("input")[0];
	document.getElementById("tabAggStatParm").style.display = (chkAggStat.checked? "table" : "none");
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Calc Stat Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Update the calc_stat controls according to the enabled checkbox setting
 */
function updateCalcStat(){
	var divCalcStat = document.getElementById("divCalcStat");
	var chkCalcStat = divCalcStat.getElementsByTagName("input")[0];
	document.getElementById("tabCalcStatParm").style.display = (chkCalcStat.checked? "table" : "none");
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Plot Spec Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Contruct the plot spec xml from information selected in the plot controls
 */
function buildPlotXML(){
	
	var listInput;
	
	var strDepXML = "";
	var strTemplate = getSelected( document.getElementById("selTemplate") )[0];
	
	// <template>
	strDepXML += "<template>" + strTemplate + ".R_tmpl</template>";
	
	if( TMPL_RHIST == _intTmpl ){

		// <plot_fix>
		strDepXML += "<plot_fix>" + buildFieldValXML("field", "val", _listRhistDiv, false, true) + "</plot_fix>";
		
	} else {
		
		// <dep>
		strDepXML += "<dep>";
		strDepXML += "<dep1>" + buildFieldValXML("fcst_var", "stat", _listDep1Div, true, false) + "</dep1>";
		strDepXML += "<dep2>" + buildFieldValXML("fcst_var", "stat", _listDep2Div, true, false) + "</dep2>";
		// strDepXML += "<fix></fix></dep>";
		strDepXML += "</dep>";
		
		// <series1> and <series2>
		var strSeriesXML = "";
		strDepXML += "<series1>" + buildFieldValXML("field", "val", _listSeries1Div, false, false) + "</series1>";
		strDepXML += "<series2>" + buildFieldValXML("field", "val", _listSeries2Div, false, false) + "</series2>";
		
		// <plot_fix>
		strDepXML += "<plot_fix>" + buildFieldValXML("field", "val", _listFixDiv, false, true) + "</plot_fix>";
		
		// <indep>
		var divIndy = document.getElementById("divIndy");
		var tabIndyVal = document.getElementById("tabIndyVal");
		var strIndepField = getSelected( divIndy.getElementsByTagName("select")[0] )[0].toLowerCase();
		strDepXML += "<indep name=\"" + strIndepField + "\">";
		for(var i=1; i < tabIndyVal.rows.length; i++){
			listInput = tabIndyVal.rows[i].getElementsByTagName("input");
			var boolIndyValChk = listInput[0].checked;
			var strVal = tabIndyVal.rows[i].getElementsByTagName("span")[1].innerHTML;
			var strLab = listInput[1].value;
			var strPlotVal = listInput[2].value;
			if( boolIndyValChk ){
				strDepXML += "<val label=\"" + strLab + "\" plot_val=\"" + strPlotVal + "\">" + strVal + "</val>";
			}
		}
		strDepXML += "</indep>";
		
		// agg_stat
		var chkAggStat = document.getElementById("chkAggStat");
		if( chkAggStat.checked ){
			var listAggStatParm = document.getElementById("tabAggStatParm").getElementsByTagName("input");
			strDepXML += "<agg_stat>";
			strDepXML += 	   "<agg_ctc>" + listAggStatParm[0].checked + "</agg_ctc>";
			strDepXML += 	 "<agg_sl1l2>" + listAggStatParm[1].checked + "</agg_sl1l2>";
			strDepXML += 	 "<boot_repl>" + listAggStatParm[2].value + "</boot_repl>";
			strDepXML += 	   "<boot_ci>" + listAggStatParm[4].value + "</boot_ci>";
			strDepXML += 	 "<agg_diff1>" + listAggStatParm[3].value + "</agg_diff1>";
			strDepXML +=	 "<agg_diff2>" + listAggStatParm[5].value + "</agg_diff2>";
			strDepXML += "</agg_stat>";		
		}
		
		// calc_stat
		var chkCalcStat = document.getElementById("chkCalcStat");
		if( chkCalcStat.checked ){
			var listCalcStatParm = document.getElementById("tabCalcStatParm").getElementsByTagName("input");
			strDepXML += "<calc_stat>";
			strDepXML += 	   "<calc_ctc>" + listCalcStatParm[0].checked + "</calc_ctc>";
			strDepXML += 	 "<calc_sl1l2>" + listCalcStatParm[1].checked + "</calc_sl1l2>";
			strDepXML += "</calc_stat>";		
		}
		
	}
	
	// <tmpl>
	var divTitleLab = document.getElementById("divTitleLab");
	listInput = divTitleLab.getElementsByTagName("input");
	strDepXML += "<tmpl>";
	strDepXML +=       "<title>" + listInput[0].value + "</title>";
	strDepXML +=     "<x_label>" + listInput[1].value + "</x_label>";
	strDepXML +=    "<y1_label>" + listInput[2].value + "</y1_label>";
	strDepXML +=    "<y2_label>" + listInput[3].value + "</y2_label>";
	strDepXML +=     "<caption>" + listInput[4].value + "</caption>";
	strDepXML += "</tmpl>";
	
	// bool formatting
	var tabFmtPlotBool = document.getElementById("tabFmtPlotBool");
	for(var i=0; i < tabFmtPlotBool.rows.length; i++){
		for(var j=0; j < tabFmtPlotBool.rows[i].cells.length; j++){
			var listTdBool = tabFmtPlotBool.rows[i].cells[j].getElementsByTagName("td");
			if( 1 > listTdBool.length ){ continue; }
			var strTag = getFmtTag(tabFmtPlotBool.rows[i].cells[j]);
			var strVal = getFmtVal(tabFmtPlotBool.rows[i].cells[j]);
			if( chkAggStat != undefined && chkAggStat.checked && strTag.match( /_diff$/ ) ){ strVal = "false"; }
			strDepXML += "<" + strTag + ">" + strVal + "</" + strTag + ">";
		}
	}
	
	// txt formatting
	var tabFmtPlotTxt = document.getElementById("tabFmtPlotTxt");
	for(var i=0; i < tabFmtPlotTxt.rows.length; i++){
		for(var j=0; j < tabFmtPlotTxt.rows[i].cells.length; j++){			
			var listTdTxt = tabFmtPlotTxt.rows[i].cells[j].getElementsByTagName("td");
			if( 1 > listTdTxt.length ){ continue; }
			var strTag = getFmtTag(tabFmtPlotTxt.rows[i].cells[j]);
			var strVal = getFmtVal(tabFmtPlotTxt.rows[i].cells[j]);
			strDepXML += "<" + strTag + ">" + strVal + "</" + strTag + ">";
		}
	}
	
	// series formatting
	var listFmtSeries = new Array("", "", "", "", "", "", "", "", "");
	var boolLegend = false;	
	if( TMPL_RHIST == _intTmpl ){
		var tabFmtSeries = document.getElementById("tabFmtSeriesRhistVal");
		listFmtSeries[0] = "TRUE";
		listFmtSeries[1] = "\"none\"";
		listFmtSeries[2] = "\"" + getFmtVal(tabFmtSeries.rows[0].cells[0]) + "\"";
		listFmtSeries[3] = "20";
		listFmtSeries[4] = "\"b\"";
		listFmtSeries[5] = "1";
		listFmtSeries[6] = getFmtVal(tabFmtSeries.rows[1].cells[0]);
		listFmtSeries[7] = "1";
		listFmtSeries[8] = "\"" + getFmtVal(tabFmtSeries.rows[2].cells[0]) + "\"";
		boolLegend = ("" != listFmtSeries[8]);
	} else {
		var tabFmtSeries = document.getElementById("tabFmtSeries");		
		for(var intRow=0; intRow < tabFmtSeries.rows.length; intRow += 2){
			var listFmtTd = getFmtSeriesVal(intRow);
			for(var i=0; i < listFmtSeries.length; i++){
				var strVal = "";
				if( i == 0 ){ strVal = (getFmtSeriesHide(intRow)? "FALSE" : "TRUE"); }
				else        { strVal = getFmtVal(listFmtTd[i-1]); }
				if( 8 == i && strVal != "" ){ boolLegend = true; }
				if( 1 == i || 2 == i || 4 == i || 8 == i){ strVal = "\"" + strVal + "\""; }
				listFmtSeries[i] += (0 < intRow? ", " : "") + strVal;			
			}
		}
	}
	strDepXML +=    "<plot_ci>c(" + listFmtSeries[1] + ")</plot_ci>";
	strDepXML +=  "<plot_disp>c(" + listFmtSeries[0] + ")</plot_disp>";
	strDepXML +=     "<colors>c(" + listFmtSeries[2] + ")</colors>";
	strDepXML +=        "<pch>c(" + listFmtSeries[3] + ")</pch>";
	strDepXML +=       "<type>c(" + listFmtSeries[4] + ")</type>";
	strDepXML +=        "<lty>c(" + listFmtSeries[5] + ")</lty>";
	strDepXML +=        "<lwd>c(" + listFmtSeries[6] + ")</lwd>";
	strDepXML += "<con_series>c(" + listFmtSeries[7] + ")</con_series>";
	if( boolLegend ){
		strDepXML += "<legend>c(" + listFmtSeries[8] + ")</legend>";
	}
	
	var strPlotCmd = document.getElementById("txtPlotCmd").value;
	if( "" != strPlotCmd ){ strDepXML += "<plot_cmd>" + strPlotCmd + "</plot_cmd>"; }
	
	// axis formatting
	var divFmtAxis = document.getElementById("divFmtAxis");
	listInput = divFmtAxis.getElementsByTagName("input");
	strDepXML +=  "<y1_lim>" + listInput[0].value + "</y1_lim>";
	strDepXML += "<y1_bufr>" + listInput[1].value + "</y1_bufr>";
	strDepXML +=  "<y2_lim>" + listInput[2].value + "</y2_lim>";
	strDepXML += "<y2_bufr>" + listInput[3].value + "</y2_bufr>";
	
	return strDepXML;
}

/**
 * Build an XML structure with specified field tag and value tag from the
 * information selected in the specified list of div controls
 */
function buildFieldValXML(strFieldTag, strValTag, listDiv, boolDep, boolSet){
	var strXML = "";
	var tabField = new Hashtable();
	var listField = new Array();
	for(i in listDiv){
		
		// get the field value and format it
		var listSel = listDiv[i].getElementsByTagName("select");
		var strVar = getSelected( listSel[0] )[0];
		if( boolDep ){ strVar = strVar.toUpperCase(); }
		else         { strVar = strVar.toLowerCase(); }
		
		// get the selected stats/values and format them
		var listVal = getSelected( listSel[1] );
		if( 1 > listVal.length ){ continue; }
		if( boolDep ){
			for(j in listVal){ listVal[j] = buildModeStatCode(listVal[j], listDiv[i]); }
		}
		
		// build the XML for the list of values
		var strValXML = "";
		for(j in listVal){ strValXML += "<" + strValTag + ">" + listVal[j] + "</" + strValTag + ">"; }
		var strValXMLCur = tabField.get(strVar);
		listField.push(strVar);
		tabField.put( strVar, (undefined == strValXMLCur? strValXML : strValXMLCur + strValXML) );		
	}
	
	// build the XML for each field stored in the table
	for(i in listField){
		var strVar = listField[i];
		strXML += "<" + strFieldTag + " name=\"" + strVar + "\">";
		if( boolSet ){ strXML += "<set name=\"" + strVar + "_" + i + "\">"; }
		strXML += tabField.get(strVar);
		if( boolSet ){ strXML += "</set>"; }
		strXML += "</" + strFieldTag + ">";
	}
	
	return strXML;
}

/**
 * If the specified statistic is a mode statistic that requires a code suffix,
 * determine the suffix from the controls of the specified divDep, add them to
 * the specified stat and return it
 */
function buildModeStatCode(stat, divDep){
	
	// if the input stat does not need a code suffix, return it
	if( "mode" != _strPlotData || 
		null != stat.match( /^RATIO_.+/ ) || 
		null != stat.match( /^AREARAT_.+/ ) || 
		null != stat.match( /^OBJ.+/ ) ){
		return stat;
	}
			
	// determine the first letter of the code [A|F|O|D]
	var boolDiff = divDep.getElementsByTagName("input")[2].checked;
	var boolFcst = divDep.getElementsByTagName("input")[3].checked;
	var boolObs  = divDep.getElementsByTagName("input")[4].checked;	
	var strCode = "_";
	if( -1 < listSearch(stat, _listStatModeSingle) ){
		if( boolDiff )                { strCode += "D"; }
		else if( boolFcst && boolObs ){ strCode += "A"; }
		else if( boolFcst )           { strCode += "F"; }
		else if( boolObs )            { strCode += "O"; }
		else                          { strCode += "A"; }
	}
	
	// if the stat is ACOV, return the code
	if( null != stat.match( /^ACOV$/ ) ){ return stat + strCode + "SA"; } 

	// determine the second letter of the code [A|S|C]
	var boolSimp = divDep.getElementsByTagName("input")[5].checked;
	var boolClus = divDep.getElementsByTagName("input")[6].checked;
	if( boolSimp && boolClus )    { strCode += "A"; }
	else if( boolSimp )           { strCode += "S"; }
	else if( boolClus )           { strCode += "C"; }
	else                          { strCode += "A"; }
	
	// determine the third letter of the code [A|M|U]
	var boolMat  = divDep.getElementsByTagName("input")[7].checked;
	var boolUnm  = divDep.getElementsByTagName("input")[8].checked;
	if( boolMat && boolUnm )      { strCode += "A"; }
	else if( boolMat )            { strCode += "M"; }
	else if( boolUnm )            { strCode += "U"; }
	else                          { strCode += "A"; }
	
	return stat + strCode;
}

function runPlotReq(){ sendRequest("POST", "<plot>" + buildPlotXML() + "</plot>", runPlotResp); }
function runPlotResp(strResp){
	if( null != (listProc = strResp.match( /<r_error>([\s\S]*)<\/r_error>/ )) ){
		alert("R error message:\n" + listProc[1]);
	}
	if( null != (listProc = strResp.match( /<plot>(.*)<\/plot>/ )) ){
		var strPlot = listProc[1];
		var win = window.open("plot.html", strPlot);
	}
}

function testPlotResp(){ runPlotResp("<plot>plot_00021_20100810_084037</plot>"); }

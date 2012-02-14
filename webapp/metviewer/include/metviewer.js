
var _strDBCon = "";
var _strInitXML = "";

var TMPL_SERIES_PLOT	= 1;
var TMPL_BAR_PLOT		= 2;
var TMPL_BOX_PLOT		= 3;
var TMPL_RHIST			= 4;
var TMPL_ROC			= 5;
var TMPL_RELY			= 6;
var _intTmpl = TMPL_SERIES_PLOT;

var _listLnkSer = ["Dep1", "Series1", "Dep2", "Series2", "Fix", "FixSpc", "Indy", "AggStat", "CalcStat", "FmtPlot", "FmtSeries"];

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
var _listVarSpc = ["FCST_VAR", "MODEL", "FCST_LEAD", "FCST_VALID_BEG", "VALID_HOUR", "FCST_INIT_BEG", "INIT_HOUR", 
                     "FCST_LEV", "OBTYPE", "VX_MASK", "INTERP_MTHD", "INTERP_PNTS", "FCST_THRESH", "OBS_THRESH"];
var _listVarRhist = ["FCST_VAR", "MODEL", "FCST_LEAD", "FCST_VALID_BEG", "VALID_HOUR", "FCST_INIT_BEG", "INIT_HOUR", 
                     "FCST_LEV", "OBTYPE", "VX_MASK", "INTERP_MTHD", "INTERP_PNTS", "FCST_THRESH", "N_RANK"];
var _listVarMode = ["MODEL", "FCST_LEAD", "FCST_VALID", "VALID_HOUR", "FCST_INIT", "INIT_HOUR", "FCST_ACCUM", 
                    "FCST_RAD", "FCST_THR", "FCST_LEV"];
var _listVar = _listVarStat;

var _listSeries1Div = new Array();
var _listSeries2Div = new Array();
var _listFixDiv = new Array();
var _listFixSpcDiv = new Array();

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


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Administration/Utility Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
		"browser codeName: " + 	navigator.appCodeName + "\n" + 
		"browser name: " + 		navigator.appName + "\n" + 
		"browser version: " +	navigator.appVersion + "\n" + 
		"cookies enabled: " +	navigator.cookieEnabled + "\n" + 
		"platform: " + navigator.platform + "\n" +
		"user-agent header: " + navigator.userAgent + "\n" +
		"IE: " + (_boolIE? "true" : "false") + "\n\n"
	);
	*/
	console("load() - IE: " + (_boolIE? "true" : "false") + "\n\n");

	//  add a handler to ensure that the Loading... screen tracks with scrolling
	window.onscroll = function(e){ if( _boolDim ){ dimScreen(true); } }
	
	//  initialize the debug controls
	setDebugDisp(_boolDebugDisp);
	
	//  initialize the plot format controls
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

	//  add the boolean formatting option controls
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

	//  add onchange listeners to some controls
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

	//  add the text formatting options
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
	addFmtPlot("X1 Values Size",				"xtlab_size",		"1",			"txt");
	addFmtPlot("X1 Label Text Weight",			"xlab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("X1 Label Text Size",			"xlab_size",		"1",			"txt");
	addFmtPlot("X1 Label Perp Offset",			"xlab_offset",		"2",			"txt");
	addFmtPlot("X1 Label Horiz Align",			"xlab_align",		".5",			"txt");
	addFmtPlot("Y1 Values Orientation",			"ytlab_orient",		["1", "3"],		"txt");
	addFmtPlot("Y1 Values Perp Offset",			"ytlab_perp",		".5",			"txt");
	addFmtPlot("Y1 Values Horiz Align",			"ytlab_horiz",		".5",			"txt");
	addFmtPlot("Y1 Values Size",				"ytlab_size",		"1",			"txt");
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
	addFmtPlot("X2 Stats Horiz Align",			"x2tlab_horiz",		".5",			"txt");
	addFmtPlot("X2 Stats Size",					"x2tlab_size",		".8",			"txt");
	addFmtPlot("X2 Label Text Weight",			"x2lab_weight",		["1", "2", "3", "4", "5"], "txt");
	addFmtPlot("X2 Label Text Size",			"x2lab_size",		".8",			"txt");
	addFmtPlot("X2 Label Perp Offset",			"x2lab_offset",		"-.5",			"txt");
	addFmtPlot("X2 Label Horiz Align",			"x2lab_align",		".5",			"txt");
	addFmtPlot("Y2 Values Orientation",			"y2tlab_orient",	["1", "3"],		"txt");
	addFmtPlot("Y2 Values Perp Offset",			"y2tlab_perp",		".5",			"txt");
	addFmtPlot("Y2 Values Horiz Align",			"y2tlab_horiz",		".5",			"txt");
	addFmtPlot("Y2 Values Size",				"y2tlab_size",		"1",			"txt");
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
	addFmtPlot("Reliability Event Histogram",	"rely_event_hist",	["TRUE", "FALSE"], "txt");
	addFmtPlot("Conf Interval Alpha",			"ci_alpha",			".05", 			"txt");	

	//  initialize the dep list
	var divDep0 = document.getElementById("divDep1").getElementsByTagName("div")[0];
	_listDep1Div.push( divDep0 );

	//  initialize the series lists
	_divFieldVal = document.getElementById("divFieldVal");
	fillSelect(document.getElementById("selField"), _listVar);
	addSeries1Div();
	document.getElementById("lnkRemoveFieldVal0").style.display = "none";

	//  initialize the independent variable controls
	fillSelect(document.getElementById("selIndyVar"), _listIndyVar);
	
	//  build the series formatting controls
	buildSeriesDiv();
	setFmtSeriesMod(0, "false");

	//  update the agg_stat controls
	updateAggStat();

	//  initialize the database list and controls
	var strDBLoad = document.getElementById("spanDBLoad").innerHTML;
	if( "" != strDBLoad ){
		_strDBCon = strDBLoad;
		document.getElementById("selDB").style.display = "none";
		console("onLoad() - loading single database: " + _strDBCon + "\n\n");
		listFcstVar1Req(0);
	} else {		
		listDBReq();
		var txtInitXML = document.getElementById("txtInitXML");
		if( "" != txtInitXML.value ){ loadInitXML(txtInitXML.value); }
	}
	
}


/**
 * Button handlers that call the server to clear/list the <list_val> and <list_stat>
 * caches
 */
function dbClearCacheReq()      { sendRequest("POST", "<db_clear_cache/>"       , nullResp); clearControls(); }
function listValClearCacheReq() { sendRequest("POST", "<list_val_clear_cache/>" , nullResp); clearControls(); }
function listValCacheKeysReq()  { sendRequest("POST", "<list_val_cache_keys/>" ,  nullResp); clearControls(); }
function listStatClearCacheReq(){ sendRequest("POST", "<list_stat_clear_cache/>", nullResp); clearControls(); }
function listStatCacheKeysReq() { sendRequest("POST", "<list_stat_cache_keys/>" , nullResp); clearControls(); }

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

	//  update the data members and lists accordingly
	var strPlotData = getSelected( document.getElementById("selPlotData") )[0];
	var strTmpl = getSelected( document.getElementById("selTemplate") )[0];
	if( strPlotData == "Stat" ){
		if     ( strTmpl == "rhist" ){ _strPlotData = "rhist"; }
		else if( strTmpl == "roc"   ){ _strPlotData = "roc";   }
		else if( strTmpl == "rely"  ){ _strPlotData = "rely";  }
		else                         { _strPlotData = "stat";  }
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
 * Determine if the input statistic is a MODE statistic and return true if so,
 * false otherwise.
 */
function isModeStat(stat){
	return (
			(null != stat.match( /^\w+_[FODA]?[SCA][MUA]$/ )) ||
			(null != stat.match( /^RATIO_.+/ ))               || 
			(null != stat.match( /^AREARAT_.+/ ))             || 
			(null != stat.match( /^OBJ.+/ ))
		   );
}

/**
 * Clear all variable/value controls and reset the select lists to the currently
 * selected lists of fcst_var and variables.
 */
function clearControls(){

	//  determine the selected template
	var strTemplate = getSelected( document.getElementById("selTemplate") )[0];
	
	//  for specialized plot templates, clear the <plot_fix> controls and return
	if( isTmplSpc() ){

		//  reset the fixed values
		while( 0 < _listFixSpcDiv.length ){ removeFixSpcDiv( _listFixSpcDiv[0].getElementsByTagName("input")[1].value); }
		//listFcstVar1Req(0);
		return;		
	}
		
	//  reset the dep stat controls
	var intDepId = _listDep1Div[0].getElementsByTagName("input")[1].value;
	clearDepStat(intDepId);
	while( 1 < _listDep1Div.length ){ removeDep1Var(_listDep1Div[1].getElementsByTagName("input")[1].value); }
	while( 0 < _listDep2Div.length ){ removeDep2Var(_listDep2Div[0].getElementsByTagName("input")[1].value); }
	listFcstVar1Req(intDepId);
	_listDep1Div[0].getElementsByTagName("td")[3].style.display = "none";
	_listDep1Div[0].getElementsByTagName("td")[4].style.display = "none";
	_listDep1Div[0].getElementsByTagName("td")[5].style.display = "none";

	//  reset the series controls
	while( 0 < _listSeries1Div.length ){ removeSeries1Div( _listSeries1Div[0].getElementsByTagName("input")[1].value); }
	while( 0 < _listSeries2Div.length ){ removeSeries2Div( _listSeries2Div[0].getElementsByTagName("input")[1].value); }

	//  reset the select field variable list
	var selField = document.getElementById("selField");
	clearSelect(selField);
	fillSelect(selField, _listVar);
	addSeries1Div();
	_listSeries1Div[0].getElementsByTagName("span")[1].style.display = "none";
	
	//  reset the fixed values
	while( 0 < _listFixDiv.length ){ removeFixDiv( _listFixDiv[0].getElementsByTagName("input")[1].value); }
	
	//  reset the agg_stat controls
	var divAggStat = document.getElementById("divAggStat");
	divAggStat.getElementsByTagName("input")[0].checked = false;
	updateAggStat();	

	//  reset the indep controls
	var selIndyVar = document.getElementById("selIndyVar");
	clearSelect(selIndyVar);
	fillSelect(selIndyVar, _listIndyVar);
	clearIndyVal();

	//  populate the fcst_var list
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


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Template Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Determine if the specified template type is one of the specialized plot
 * templates and return true if so, false otherwise.  Default input is the
 * current _intTmpl setting.
 */
function isTmplSpc(){ 
	return (TMPL_RHIST	== _intTmpl || 
			TMPL_ROC	== _intTmpl || 
			TMPL_RELY	== _intTmpl); 
}

/**
 * When the plot template is switched, configure the visibility of the controls
 */
function updateTmpl(){

	//  update the template type data member
	var strTmpl = getSelected( document.getElementById("selTemplate") )[0];
	if     ( null != strTmpl.match( /^series_plot$/ ) ){ _intTmpl = TMPL_SERIES_PLOT; } 
	else if( null != strTmpl.match( /^bar_plot$/    ) ){ _intTmpl = TMPL_BAR_PLOT;    } 
	else if( null != strTmpl.match( /^box_plot$/    ) ){ _intTmpl = TMPL_BOX_PLOT;    } 
	else if( null != strTmpl.match( /^rhist$/       ) ){ _intTmpl = TMPL_RHIST;       } 
	else if( null != strTmpl.match( /^roc$/         ) ){ _intTmpl = TMPL_ROC;         } 
	else if( null != strTmpl.match( /^rely$/        ) ){ _intTmpl = TMPL_RELY;         } 
	
	//  default visibility settings for the series_plot template
	var boolY1 = true;
	var boolY2 = true;
	var boolY2NA = false;
	var boolFix = true;
	var boolFixSpc = false;
	var boolIndy = true;
	var boolAggStat = true;
	var boolAggStatNA = false;
	var boolCalcStat = true;
	var boolCalcStatNA = false;
	var boolRocCalc = false;
	
	//  for box_plot and bar_plot templates, hide the Y2 controls and agg_stat
	if( TMPL_BOX_PLOT == _intTmpl || TMPL_BAR_PLOT == _intTmpl ){
		boolY2NA = true;
		boolAggStatNA = true;	
	} else if( isTmplSpc() ){
		boolY1 = false;
		boolY2 = false;
		boolFix = false;
		boolFixSpc = true;
		boolIndy = false;
		boolAggStat = false;
		boolCalcStat = false;
		boolRocCalc = (TMPL_ROC == _intTmpl);
	}
	
	//  configure the visibility of the Y1, Y2, fix and indy controls
	document.getElementById("tdY1").style.display    		= boolY1?		"table-cell" : "none";
	document.getElementById("tdY2").style.display    		= boolY2?		"table-cell" : "none";
	document.getElementById("spanY2NA").style.display    	= boolY2NA?		"inline" : "none";
	document.getElementById("divDep2").style.display     	= boolY2NA?		"none" : "inline";
	document.getElementById("divSeries2").style.display  	= boolY2NA?		"none" : "inline";
	document.getElementById("divFix").style.display    		= boolFix?		"inline" : "none";
	document.getElementById("divFixSpc").style.display   	= boolFixSpc?	"inline" : "none";
	document.getElementById("tdIndy").style.display    		= boolIndy?		"table-cell" : "none";
	
	//  configure the visibility of the agg_stat controls
	document.getElementById("tdAggStat").style.display		= boolAggStat?	"table-cell" : "none";
	document.getElementById("divAggStat").getElementsByTagName("input")[0].checked = false;
	updateAggStat();	
	document.getElementById("spanAggStatNA").style.display  = boolAggStatNA?"inline" : "none";
	document.getElementById("chkAggStat").style.display     = boolAggStatNA?"none" : "inline";
	document.getElementById("spanAggStat").style.display    = boolAggStatNA?"none" : "inline";
	
	//  configure the visibility of the calc_stat controls
	document.getElementById("tdCalcStat").style.display		= boolCalcStat?	"table-cell" : "none";
	document.getElementById("divCalcStat").getElementsByTagName("input")[0].checked = false;
	updateCalcStat();	
	document.getElementById("spanCalcStatNA").style.display = boolCalcStatNA?"inline" : "none";
	document.getElementById("chkCalcStat").style.display    = boolCalcStatNA?"none" : "inline";
	document.getElementById("spanCalcStat").style.display   = boolCalcStatNA?"none" : "inline";

	//  configure the visibility of the calc_stat controls
	document.getElementById("tdRocCalc").style.display		= boolRocCalc?   "table-cell" : "none";

	//  configure the visibility of the plot formatting controls
	var listFmtAxis = document.getElementById("divFmtAxis").getElementsByTagName("td");
	listFmtAxis[4].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[5].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[6].style.display							= boolY2NA?		"none" : "inline";
	listFmtAxis[7].style.display							= boolY2NA?		"none" : "inline";
	
	if( isTmplSpc() ){
		buildSeriesDivSpc();
		updateFmtPlot();
	} else { buildSeriesDiv(); }

	updatePlotData();
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Dependent Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
	
 	//  clone the dependent variable controls
 	var intDepId = _intDepIdNext++;
 	var divDep = _listDep1Div[0].cloneNode(true);

 	//  update the components of the cloned fixed value controls
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

 	//  add the new fixed variable value section to the page
 	listDepDiv.push(divDep);
	var divDepParent = document.getElementById("divDep" + intY);
	var divImgParent = document.getElementById("imgDep" + intY);
	//  console("addDep(" + intY + ")\n divDepParent: " + divDepParent + "\n
	//  divImgParent: " + divImgParent + "\n\n");
 	divDepParent.insertBefore(divDep, divImgParent);

 	//  ensure the first remove link is visible
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
	
	//  query the database for stat_header stats, if appropriate
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
	
	//  otherwise, use the static list of mode stats
	else {
		var selFcstVar = document.getElementById("selStat" + intId);
		fillSelect(selFcstVar, _listStatMode);
		selFcstVar.style.display = "inline";
	}
}
function selectFcstVarResp(strResp){

	//  parse the response
	var resp = parseListValResp(strResp, "stat");
	if( null == resp ){ return; }

	//  populate and display the stats select control
	var selFcstVar = document.getElementById("selStat" + resp.id);
	fillSelect(selFcstVar, resp.vals);
	selFcstVar.style.display = "inline";
}

/**
 * Build an XML criteria string for a <list_val> command which contains the list
 * of currently selected dependent variable fcst_var values
 */
function buildFcstVarCrit(intY){
	
	//  determine the list of dep divs to consider
	var listDepDiv;
	if     ( 1 == intY ){ listDepDiv = _listDep1Div; }
	else if( 2 == intY ){ listDepDiv = _listDep2Div; }
	else                { listDepDiv = _listDep1Div.concat(_listDep2Div); }
	
	//  add the fcst_var from each dep div to the list
	var strFixCrit = "<field name=\"FCST_VAR\">";
	for(i in listDepDiv){
		var selFcstVar = listDepDiv[i].getElementsByTagName("select")[0];
		strFixCrit += "<val>" + escapeXml(selFcstVar.options[selFcstVar.selectedIndex].text) + "</val>";
	}
	strFixCrit += "</field>";
	return strFixCrit;
}

function buildFcstVarStatCrit(intY){
	
	//  determine the list of dep divs to consider
	var listDepDiv;
	if     ( 1 == intY ){ listDepDiv = _listDep1Div; }
	else if( 2 == intY ){ listDepDiv = _listDep2Div; }
	else                { listDepDiv = _listDep1Div.concat(_listDep2Div); }
	
	//  for each fcst_var in the dep div list, build a list of stats
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

	//  determine the visibility of the mode checkboxes
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

	//  toggle the visibility of the checkbox table cells
	divDep.getElementsByTagName("span")[0].style.display = boolVisMode?		"inline" : "none";	
	divDep.getElementsByTagName("td")[3].style.display = boolVisFO?			"table-cell" : "none";
	divDep.getElementsByTagName("td")[4].style.display = boolVisSC?			"table-cell" : "none";
	divDep.getElementsByTagName("td")[5].style.display = boolVisMU?			"table-cell" : "none";
	
	//  change the state of the Difference checkbox, if appropriate
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

/**
 * Parse the id of the specified div and return its id number
 */
function getDivDepId(div){
	return ( null != (listId = div.id.match( /divDep(\d+)/ )) ? listId[1] : 0 );
}
function getDivFieldValId(div){
	return ( null != (listId = div.id.match( /divFieldVal(\d+)/ )) ? listId[1] : 0 );
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Field Value Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a field val div of the specified category and add it to the from and
 * to the input controls list
 */
function addFieldValDiv(category, listDiv){

	//  use the next field val id
	var intId = _intFieldValIdNext++;

	//  clone the field value div and update the controls
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

	//  add the new div to the input controls list and add it to the form
	listDiv.push( divFieldVal );
	document.getElementById("div" + category).insertBefore(divFieldVal, document.getElementById("img" + category));
}

/**
 * Remove the field val div with the specified id from the from and the controls
 * list. The index of the hidden id field is specified by intInputId.
 */
function removeFieldValDiv(intId, listDiv, intInputId){

	//  attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, intInputId);
	if( 0 > intIndex ){
		console("removeFieldValDiv() - WARNING: div " + intId + " not found\n\n");
		return;
	}

 	//  remove the specified div from the list and hide it
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

	//  attempt to find the specified div, and if not found, bail
	var intIndex = findDivId(listDiv, intId, 1);
	if( 0 > intIndex ){
		console("selectFieldReq() - ERROR: div " + intId + " not found\n\n");
		return;
	}

	//  gather the criteria
	var strFcstVarCrit = "";
	var strFixCrit = "";
	if( -1 == intY ){
		strFixCrit = buildFixSpcCrit(intFixEnd);
	} else {
		if(  1 == intY || 2 == intY ){ strFcstVarCrit = buildFcstVarStatCrit(intY); }
		else                         { strFcstVarCrit = buildFcstVarStatCrit(); }		
		strFixCrit = buildFixCrit(intFixEnd);
	}

	//  build a list_val request for the selected field
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

	//  parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	//  retrieve and validate the div from the input div list
	var intIndex = findDivId(listDiv, resp.id, intIdIndex);
	if( 0 > intIndex ){
		console("selectFieldResp() - ERROR: index for div id " + resp.id + " not found\n\n");
		return;
	}

	//  add the field values to the value select list
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
	
	//  find the selected element, and bail if there is not exactly one
	var selVal = document.getElementById("selVal" + intId);
	var listSel = getSelected(selVal);
	var intSel = selVal.selectedIndex;
	if( 2 > selVal.options.length || 1 != listSel.length || intSel == 0 ){ return; }
	
	//  move the selected option
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

	//  find the selected element, and bail if there is not exactly one
	var selVal = document.getElementById("selVal" + intId);
	var listSel = getSelected(selVal);
	var intSel = selVal.selectedIndex;
	if( 2 > selVal.options.length || 1 != listSel.length || intSel == selVal.options.length - 1 ){ return; }
	
	//  move the selected option
	var optMove = document.createElement("option");
	optMove.text = selVal.options[intSel].text;
	selVal.remove(intSel);
	if( _boolIE ){ selVal.add(optMove, intSel >= selVal.options.length - 1? null : intSel + 1); }
	else         { selVal.add(optMove, intSel >= selVal.options.length - 1? null : selVal.options[intSel + 1]); }
	selVal.selectedIndex = intSel + 1;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Series Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
	//  determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	//  add a field val div and modify its components for duty as a series div
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
	//  determine the appropriate div list
	var listSeriesDiv = (1 == intSeries? _listSeries1Div : _listSeries2Div);
	
	//  dispose of and hide the series div
	removeFieldValDiv(intId, listSeriesDiv, 1);
 	if( 1 == listSeriesDiv.length ){ listSeriesDiv[0].getElementsByTagName("span")[1].style.display = "none"; }
 	buildSeriesDiv();
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Fixed Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
 * selected fields and values in the values controls of the input div list
 */
function buildFieldValCrit(listDiv, endIndex){
	var strCrit = "";
	for(i=0; i <= endIndex; i++){
		var divCrit = listDiv[i];
		var selCrit = divCrit.getElementsByTagName("select")[0];
		var strCritCur = "<field name=\"" + selCrit.options[ selCrit.selectedIndex ].text + "\">";
		var listCritVal = getSelected( divCrit.getElementsByTagName("select")[1] );		
		for(var j=0; j < listCritVal.length; j++){
			strCritCur += "<val>" + escapeXml(listCritVal[j]) + "</val>"; 
		}
		strCritCur += "</field>";
		if( 0 < listCritVal.length ){ strCrit += strCritCur; }
	}
	return strCrit;
}
function buildFixCrit(endIndex){ return buildFieldValCrit(_listFixDiv, endIndex); }
function buildFixSpcCrit(endIndex){ return buildFieldValCrit(_listFixSpcDiv, endIndex); }



/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Specialized Plot Fixed Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Handlers to add, remove and populate the specialized plot fixed variable controls
 */
function addFixSpcVar(){
	//  add the control group for the field value selection
	addFieldValDiv("FixSpc", _listFixSpcDiv);
	
	//  add rank size to the list of fields
	var selField = _listFixSpcDiv[_listFixSpcDiv.length-1].getElementsByTagName("select")[0];
	clearSelect(selField);
	if( TMPL_RHIST == _intTmpl ){ fillSelect(selField, _listVarRhist); }
	else                        { fillSelect(selField, _listVarSpc);   }
}
function removeFixSpcDiv(intId) { removeFieldValDiv(intId, _listFixSpcDiv, 1); }
function selectFixSpcVarReq(intId){
	var intIndexCrit = findDivId(_listFixSpcDiv, intId, 1);
	if( 0 > intIndexCrit ){
		console("selectFixSpcVarReq() - ERROR: index for id " + intId + " not found\n");
		return;
	}
	selectFieldReq(intId, _listFixSpcDiv, intIndexCrit - 1, selectFixSpcVarResp, -1);
}
function selectFixSpcVarResp(strResp){ selectFieldResp(strResp, _listFixSpcDiv, 1, 1, false); }


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Independent Variable Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Build and run a request for values for the currently selected independent
 * variable and populate the independent variable value list with the results
 */
function selectIndyVarReq(){

	//  build a list_val request for the selected independent field
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

	//  parse the response
	var resp = parseListValResp(strResp, "val");
	if( null == resp ){ return; }

	//  hide all currently display indy val controls
	clearIndyVal();
	
	//  add a indy val control group for each indy value
	var divIndy = document.getElementById("divIndy");
	var strField = getSelected( document.getElementById("selIndyVar") )[0];
	for( i in resp.vals ){
		var trIndyVal = tabIndyVal.insertRow(tabIndyVal.rows.length);

		//  build a control set for the independent variable value
		var tdIndyChk = trIndyVal.insertCell(0);
		tdIndyChk.appendChild( document.getElementById("spanIndyValChk").cloneNode(true) );
		var tdIndyLab = trIndyVal.insertCell(1);
		tdIndyLab.appendChild( document.getElementById("spanIndyValLab").cloneNode(true) );
		trIndyVal.getElementsByTagName("span")[1].innerHTML = resp.vals[i];
		trIndyVal.style.display = "table-row";
		
		//  set the default label
		var strLabel = resp.vals[i];
		if( "FCST_LEAD" == strField ){
			var listParse = strLabel.match( /(\d+)0000$/ );
			if( null != listParse ){ strLabel = listParse[1]; }
		}
		trIndyVal.getElementsByTagName("input")[1].value = strLabel;
	}
	document.getElementById("spanIndyCheck").style.display = "inline";
	
	//  update the plot formatting to accommodate date series
	updateFmtPlot();
}

/**
 * Remove all rows of the indy table that contain values
 */
function clearIndyVal(){
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

/**
 * Find the table row with the corresponding indy val
 */
function findIndyTr(val){
	var strVal = val.replace("<", "&lt;").replace(">", "&gt;");
	var tabIndyVal = document.getElementById("tabIndyVal");
	for(var i=0; i < tabIndyVal.rows.length; i++){
		var strValCur = tabIndyVal.rows[i].getElementsByTagName("span")[1].innerHTML;		
		if( strVal == strValCur ){ return tabIndyVal.rows[i]; }
	}
	return null;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Plot Formatting Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Construct plot formatting controls with the specified label, value and type.
 * The currently supported values of type are "txt" and "bool". The controls are
 * placed at the next available place for their respective type.
 */
function addFmtPlot(label, tag, value, type){

	var boolTypeTxt = (type == "txt");
	var intFmtIndex = (boolTypeTxt? _intFmtPlotTxtIndex : _intFmtPlotBoolIndex);
	var intCol = intFmtIndex % _intNumFmtPlotCol;
	var tabFmt = document.getElementById( boolTypeTxt? "tabFmtPlotTxt" : "tabFmtPlotBool" );
	var tdFmt;

	//  if the index is zero, populate the existing cell
	if( 0 == intFmtIndex ){
		tdFmt = tabFmt.rows[0].cells[0];
	}

	//  if the table requires a new row, create one
	else if( 0 == intCol ){
		var trFmt = tabFmt.insertRow( tabFmt.rows.length );
		for(var i=0; i < _intNumFmtPlotCol; i++){
			tdFmt = trFmt.insertCell(i);
			tdFmt.align = "right";
			tdFmt.style.width = _strFmtPlotWidth;
		}
		tdFmt = trFmt.cells[0];
	}

	//  if a new row is not required, get the next available cell on the current row
	else {
		tdFmt = tabFmt.rows[ tabFmt.rows.length - 1 ].cells[intCol];
	}

	//  populate the controls with the specified input information
	if( 0 != intFmtIndex ){
		tdFmt.appendChild( document.getElementById( boolTypeTxt? "spanFmtPlotTxt" : "spanFmtPlotBool" ).cloneNode(true) );
	}
	
	//  update the label and tag
	setFmtLabelTag(tdFmt, label, tag);

	//  configure and populate the selection control for text settings
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
	
	//  configure and populate the select control for bool settings
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

	//  determine if vert_plot is true or false
	var tabFmtPlot = document.getElementById("tabFmtPlotBool");
	var selVertPlot = tabFmtPlot.rows[0].cells[2].getElementsByTagName("select")[0];
	var boolVert = (getSelected(selVertPlot)[0] == "true");
	
	//  determine if the independent variable is a date type
	var selIndyVar = document.getElementById("selIndyVar");
	var strIndyVar = getSelected(selIndyVar)[0];
	var boolIndyDate = !isTmplSpc() && 
					   ( strIndyVar == "FCST_VALID_BEG" || strIndyVar == "FCST_INIT_BEG" ||
						 strIndyVar == "FCST_VALID"     || strIndyVar == "FCST_INIT"     );

	//  set the default values for each format setting
	var tabFmtPlotTxt = document.getElementById("tabFmtPlotTxt");
	tabFmtPlotTxt.rows[1].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? "c(6, 4, 4, 4)" : (boolIndyDate? "c(13, 4, 3, 4)" : "c(8, 4, 5, 4)"));	//  mar
	tabFmtPlotTxt.rows[2].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? "-.4"           : (boolIndyDate? "-.5"            : "-2"           ));	//  title_offset
	setSelected(tabFmtPlotTxt.rows[3].cells[0].getElementsByTagName("select")[0], 
		(boolVert? "1"             : (boolIndyDate? "3"              : "1"            )));	//  xtlab_orient
	tabFmtPlotTxt.rows[3].cells[1].getElementsByTagName("input")[0].value = 
		(boolVert? ".5"            : (boolIndyDate? ".5"             : "-.75"         ));	//  xtlab_perp
	tabFmtPlotTxt.rows[3].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? ".6"            : (boolIndyDate? ".9"             : ".5"           ));	//  xtlab_horiz
	tabFmtPlotTxt.rows[4].cells[3].getElementsByTagName("input")[0].value = 
		(boolVert? "-2"            : (boolIndyDate? "14"             : "2"            ));	//  xlab_offset
	tabFmtPlotTxt.rows[5].cells[2].getElementsByTagName("input")[0].value = 
		(boolVert? "-1"            : (boolIndyDate? ".5"             : ".5"           ));	//  ytlab_perp
	tabFmtPlotTxt.rows[6].cells[3].getElementsByTagName("input")[0].value = 
		(boolVert? "2"             : (boolIndyDate? "-2"             : "-2"           ));	//  ylab_offset
	tabFmtPlotTxt.rows[9].cells[3].getElementsByTagName("input")[0].value = 
		(boolVert? "1"             : (boolIndyDate? "-.5"            : "-.5"          ));	//  x2lab_offset
	tabFmtPlotTxt.rows[12].cells[3].getElementsByTagName("input")[0].value = 
		(boolVert? "c(0, -.17)"    : (boolIndyDate? "c(0, -.48)"     : "c(0, -.25)"   ));	//  legend_inset
}

/**
 * Build the list of plot series reflected by the current state of the controls
 */
function buildSeriesDiv(){
	
	if( isTmplSpc() ){ return; }

	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var spanFmtSeriesDisp = document.getElementById("spanFmtSeriesDisp");
	_intNumSeries = 0;
	
	//  update the visibility of the series formatting controls
	spanFmtSeriesDisp.style.display = "inline";
	var spanMsg = spanFmtSeriesDisp.getElementsByTagName("span")[0];
	if( null != spanMsg.innerHTML.match("Hide.*") ){
		document.getElementById("tabFmtSeries").style.display = "inline";
	}
	document.getElementById("tabFmtSeriesRhist").style.display = "none";
	
	//  build a table containing all current series settings
	var listFmt = new Array();
	var tableFmt = new Hashtable();
	for(var intRow=0; intRow < tabFmtSeries.rows.length; intRow += 2){
		var listSpan = tabFmtSeries.rows[intRow].getElementsByTagName("span");
		var listInput = tabFmtSeries.rows[intRow].getElementsByTagName("input");
		var listFmtTd = getFmtSeriesVal(intRow);
		
		//  get the series name and values and put them in the table
		var strSeriesName = listSpan[2].innerHTML + " - " + listSpan[3].innerHTML;
		var strFmt = listInput[0].value;
		strFmt += "|" + listInput[1].checked;
		for(var j=0; j < listFmtTd.length; j++){ strFmt += "|" + getFmtVal(listFmtTd[j]); }
		listFmt.push(strFmt);
		if( listInput[0].value != "false" ){ tableFmt.put(strSeriesName, strFmt); }
	}
	
	//  determine if the lockbox is checked
	var boolLockFmt = document.getElementById("chkFmtSeriesLock").checked;

	//  clear all existing series, except the first two
	while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }

	//  build permutation of the series values
	var listSeries1Perm = permuteSeries(_listSeries1Div, 0, getPlotDiff(1));
	var listSeries2Perm = permuteSeries(_listSeries2Div, 0, getPlotDiff(2));
	
	//  build all y1 and y2 series
	for(var intY=1; intY <= 2; intY++){

		var listDepDiv = (1 == intY? _listDep1Div : _listDep2Div);
		var listSeriesPerm = (1 == intY? listSeries1Perm : listSeries2Perm);
			
		//  for each dep div, consider the fcst_var and selected stats
		for(var intDep=0; intDep < listDepDiv.length; intDep++){
	
			//  get the dep var information
			var strFcstVar = getSelected( listDepDiv[intDep].getElementsByTagName("select")[0] )[0];
			var listStat = getSelected( listDepDiv[intDep].getElementsByTagName("select")[1] );
	
			//  build a series for each combination of fcst_var, stat and series permutation
			for(var intStat=0; intStat < listStat.length; intStat++){
				for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++){
	
					//  build the series name
					var strSeriesName =  listSeriesPerm[intSeries] + " " + strFcstVar + " " + listStat[intStat];
	
					var trFmtSeries;
					var tdName;
	
					//  if the series is the first to be built, use the existing controls
					if( 0 == _intNumSeries ){
						trFmtSeries = tabFmtSeries.rows[0];
						tdName = trFmtSeries.cells[0];
					}
	
					//  otherwise, build a new set of series formatting controls
					else {
						
						//  insert the <hr/> between series format controls
						if( 1 == _intNumSeries ){ tabFmtSeries.rows[1].style.display = "table-row"; }
						else {
							var trHR = tabFmtSeries.insertRow( tabFmtSeries.rows.length );
							var tdHR = trHR.insertCell(0);
							tdHR.colSpan = "3";
							tdHR.appendChild( document.getElementById("spanFmtSeriesHR").cloneNode(true) );
						}
	
						//  insert a copy of the series format controls
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
	
					//  populate the controls with the series name
					var strYSeries = (1 == intY? "Y1" : "Y2") + " Series";
					tdName.getElementsByTagName("span")[2].innerHTML = strSeriesName;
					tdName.getElementsByTagName("span")[3].innerHTML = strYSeries;
					
					//  add change handlers to the formatting inputs
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

					//  get format settings for the current field, if available, otherwise use defaults
					var listVal = _listFmtSeriesDefaults;
					var strVal = tableFmt.get(escapeXml(strSeriesName) + " - " + strYSeries);
					if     ( boolLockFmt         ){ listVal = listFmt[ _intNumSeries % listFmt.length ].split("|"); }
					else if( undefined != strVal ){ listVal = strVal.split("|");                                    }
					
					//  apply the settings to the formatting controls
					listInput[0].value = listVal[0];
					tdName.getElementsByTagName("input")[1].checked = (listVal[1] == "true"? true : false);
					var listFmtTd = getFmtSeriesVal( _intNumSeries * 2 );
					for(var i=0; i < listFmtTd.length; i++){
						setFmtVal(listFmtTd[i], listVal[i+2]);
					}

					_intNumSeries++;
					
				}  //  end: for(var intSeries=0; intSeries < listSeriesPerm.length; intSeries++)
				
			}  //  end: for(var intStat=0; intStat < listStat.length; intStat++)
			
		}  //  end: for(var intDep=0; intDep < listDepDiv.length; intDep++)
		
	}  //  end: for(var intY=1; intY <= 2; intY++)
	
	//  set the default color for each series to the appropriate shade of rainbow
	var listColors = rainbow(_intNumSeries);
	for(var i=0; i < _intNumSeries; i++){
		var listFmtTd = getFmtSeriesVal( i * 2 );
		var strColor = getFmtVal( listFmtTd[1] );
		if( "" == strColor ){ setFmtVal(listFmtTd[1], listColors[i]); }
	}
	
	//  show or hide the controls, depending on the number of series
	tabFmtSeries.style.display		= (1 > _intNumSeries ? "none" : tabFmtSeries.style.display);
	spanFmtSeriesDisp.style.display	= (1 > _intNumSeries ? "none" : "inline");
	document.getElementById("spanFmtSeriesNum").innerHTML = "# Series: " + _intNumSeries;
	
	//  update the agg_stat diff fields
	document.getElementById("txtAggDiff1").value = (getPlotDiff(1)? "TRUE" : "FALSE");
	document.getElementById("txtAggDiff2").value = (getPlotDiff(2)? "TRUE" : "FALSE");
}

/**
 * Build and return a list of the format control td structures in the specified
 * row
 */
function getFmtSeriesVal(row){
	
	//  determine the id of the series formatting controls table
	var strTabFmtSeriesId = "tabFmtSeries";
	switch(_intTmpl){
		case TMPL_RHIST: strTabFmtSeriesId = "tabFmtSeriesRhist"; break;
		case TMPL_ROC  : strTabFmtSeriesId = "tabFmtSeriesRoc";   break;
		case TMPL_RELY : strTabFmtSeriesId = "tabFmtSeriesRely";  break;
	}
	
	//  get and validate the requested row
	var listTdFmt = new Array();
	var tabFmtSeries = document.getElementById(strTabFmtSeriesId);
	var trFmtSeries = tabFmtSeries.rows[row];
	if( 3 != trFmtSeries.cells.length ){ return listTdFmt; }
	
	//  build a list of formatting td elements
	var tabVal1 = trFmtSeries.cells[1].getElementsByTagName("table")[0];
	for(var i=0; i < tabVal1.rows.length; i++){ 
		for(var j=0; j < tabVal1.rows[i].cells.length; j++){ listTdFmt.push(tabVal1.rows[i].cells[j]); }
	}
	if( TMPL_RHIST != _intTmpl ){
		var tabVal2 = trFmtSeries.cells[2].getElementsByTagName("table")[0];
		for(var i=0; i < tabVal2.rows.length; i++){ 
			for(var j=0; j < tabVal2.rows[i].cells.length; j++){ listTdFmt.push(tabVal2.rows[i].cells[j]); }
		}
	}
	
	return listTdFmt;
}

/**
 * Validate that the specified row in the series formatting controls contains a
 * Hide checkbox. If so, return the checkbox control, otherwise, null.
 */
function getFmtSeriesHideChk(row){
	var tabFmtSeries = document.getElementById("tabFmtSeries");
	var trFmtSeries = tabFmtSeries.rows[row];
	if( 3 != trFmtSeries.cells.length ){ return null; }
	return trFmtSeries.cells[0].getElementsByTagName("input")[1];
}

/**
 * Determine the state of the Hide checkbox for the input row and return the
 * state of the control
 */
function getFmtSeriesHide(row){	return getFmtSeriesHideChk(row).checked; }

/**
 * Set the state of the Hide checkbox for the input row to the specified value
 */
function setFmtSeriesHide(row, val){ getFmtSeriesHideChk(row).checked = val; }

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

	//  if the index has reached the end of the list, return the selected values
	//  from the last control
	if( listSeriesDiv.length == intIndex + 1 ){
		if( boolDiff ){ listVal.splice(listVal.length - 1, 0, "__DIFF__"); }
		return listVal;
	}

	//  otherwise, get the list for the next fcst_var and build upon it
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
function buildSeriesDivSpc(){
	
	//  hide all the series formatting controls
	document.getElementById("tabFmtSeries").style.display = "none";
	document.getElementById("spanFmtSeriesDisp").style.display = "none";
	document.getElementById("tabFmtSeriesRhist").style.display = "none";
	document.getElementById("tabFmtSeriesRoc").style.display = "none";
	document.getElementById("tabFmtSeriesRely").style.display = "none";
	
	//  determine the formatting controls to configure
	var strFmtSeriesTab = "tabFmtSeriesRhist";
	if     ( TMPL_RHIST == _intTmpl ){ strFmtSeriesTab = "tabFmtSeriesRhist"; }
	else if( TMPL_ROC   == _intTmpl ){ strFmtSeriesTab = "tabFmtSeriesRoc";   }
	else if( TMPL_RELY  == _intTmpl ){ strFmtSeriesTab = "tabFmtSeriesRely";   }
	var tabFmtSeries = document.getElementById(strFmtSeriesTab);
	tabFmtSeries.style.display = "inline";

	//  clear all existing series, except the first two
	if( !isTmplSpc() ){
		while( 2 < tabFmtSeries.rows.length ){ tabFmtSeries.deleteRow( tabFmtSeries.rows.length - 1 ); }
	}
		
	//  show or hide the controls, depending on the number of series
	_intNumSeries = ( _intTmpl == TMPL_RELY? 2 : 1);
	tabFmtSeries.style.display = tabFmtSeries.style.display;
	document.getElementById("spanFmtSeriesNum").innerHTML = "# Series: " + _intNumSeries;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Agg Stat Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Update the agg_stat controls according to the enabled checkbox setting
 */
function updateAggStat(){
	var divAggStat = document.getElementById("divAggStat");
	var chkAggStat = divAggStat.getElementsByTagName("input")[0];
	document.getElementById("tabAggStatParm").style.display = (chkAggStat.checked? "table" : "none");
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Calc Stat Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Update the calc_stat controls according to the enabled checkbox setting
 */
function updateCalcStat(){
	var divCalcStat = document.getElementById("divCalcStat");
	var chkCalcStat = divCalcStat.getElementsByTagName("input")[0];
	document.getElementById("tabCalcStatParm").style.display = (chkCalcStat.checked? "table" : "none");
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Plot Spec Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Contruct the plot spec xml from information selected in the plot controls
 */
function buildPlotXML(){
	
	var listInput;
	
	var strDepXML = "";
	var strTemplate = getSelected( document.getElementById("selTemplate") )[0];
	
	//  <template>
	strDepXML += "<template>" + strTemplate + ".R_tmpl</template>";
	
	if( isTmplSpc() ){

		//  <plot_fix>
		strDepXML += "<plot_fix>" + buildFieldValXML("field", "val", _listFixSpcDiv, false, true) + "</plot_fix>";
		
		//  <roc_calc>
		if( TMPL_ROC == _intTmpl ){
			var listRocCalcParm = document.getElementById("tabRocCalcParm").getElementsByTagName("input");
			strDepXML += "<roc_calc>";
			strDepXML += 	   "<roc_pct>" + listRocCalcParm[0].checked + "</roc_pct>";
			strDepXML += 	   "<roc_ctc>" + listRocCalcParm[1].checked + "</roc_ctc>";
			strDepXML += "</roc_calc>";		
		}		
		
	} else {
		
		//  <dep>
		strDepXML += "<dep>";
		strDepXML += "<dep1>" + buildFieldValXML("fcst_var", "stat", _listDep1Div, true, false) + "</dep1>";
		strDepXML += "<dep2>" + buildFieldValXML("fcst_var", "stat", _listDep2Div, true, false) + "</dep2>";
		//  strDepXML += "<fix></fix></dep>";
		strDepXML += "</dep>";
		
		//  <series1> and <series2>
		var strSeriesXML = "";
		strDepXML += "<series1>" + buildFieldValXML("field", "val", _listSeries1Div, false, false) + "</series1>";
		strDepXML += "<series2>" + buildFieldValXML("field", "val", _listSeries2Div, false, false) + "</series2>";
		
		//  <plot_fix>
		strDepXML += "<plot_fix>" + buildFieldValXML("field", "val", _listFixDiv, false, true) + "</plot_fix>";
		
		//  <indep>
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
		
		//  agg_stat
		var chkAggStat = document.getElementById("chkAggStat");
		if( chkAggStat.checked ){
			var listAggStatParm = document.getElementById("tabAggStatParm").getElementsByTagName("input");
			strDepXML += "<agg_stat>";
			strDepXML += 	   "<agg_ctc>" + listAggStatParm[0].checked + "</agg_ctc>";
			strDepXML += 	 "<agg_sl1l2>" + listAggStatParm[1].checked + "</agg_sl1l2>";
			strDepXML += 	   "<agg_pct>" + listAggStatParm[2].checked + "</agg_pct>";
			strDepXML += 	"<agg_nbrcnt>" + listAggStatParm[3].checked + "</agg_nbrcnt>";
			strDepXML += 	 "<boot_repl>" + listAggStatParm[4].value + "</boot_repl>";
			strDepXML += 	   "<boot_ci>" + listAggStatParm[6].value + "</boot_ci>";
			strDepXML += 	 "<agg_diff1>" + listAggStatParm[5].value + "</agg_diff1>";
			strDepXML +=	 "<agg_diff2>" + listAggStatParm[7].value + "</agg_diff2>";
			strDepXML += "</agg_stat>";		
		}
		
		//  calc_stat
		var chkCalcStat = document.getElementById("chkCalcStat");
		if( chkCalcStat.checked ){
			var listCalcStatParm = document.getElementById("tabCalcStatParm").getElementsByTagName("input");
			strDepXML += "<calc_stat>";
			strDepXML += 	   "<calc_ctc>" + listCalcStatParm[0].checked + "</calc_ctc>";
			strDepXML += 	 "<calc_sl1l2>" + listCalcStatParm[1].checked + "</calc_sl1l2>";
			strDepXML += "</calc_stat>";		
		}
		
	}
	
	//  <tmpl>
	var divTitleLab = document.getElementById("divTitleLab");
	listInput = divTitleLab.getElementsByTagName("input");
	strDepXML += "<tmpl>";
	strDepXML +=       "<title>" + listInput[0].value + "</title>";
	strDepXML +=     "<x_label>" + listInput[1].value + "</x_label>";
	strDepXML +=    "<y1_label>" + listInput[2].value + "</y1_label>";
	strDepXML +=    "<y2_label>" + listInput[3].value + "</y2_label>";
	strDepXML +=     "<caption>" + listInput[4].value + "</caption>";
	strDepXML += "</tmpl>";
	
	//  bool formatting
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
	
	//  txt formatting
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
	
	//  series formatting
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
	} else if( TMPL_ROC == _intTmpl ){
		var tabFmtSeries1 = document.getElementById("tabFmtSeriesRoc1");
		var tabFmtSeries2 = document.getElementById("tabFmtSeriesRoc2");
		listFmtSeries[0] = "TRUE";
		listFmtSeries[1] = "\"none\"";
		listFmtSeries[2] = "\"" + getFmtVal(tabFmtSeries1.rows[0].cells[0]) + "\"";
		listFmtSeries[3] = getFmtVal(tabFmtSeries1.rows[1].cells[0]);
		listFmtSeries[4] = "\"" + getFmtVal(tabFmtSeries1.rows[2].cells[0]) + "\"";
		listFmtSeries[5] = getFmtVal(tabFmtSeries2.rows[0].cells[0]);
		listFmtSeries[6] = getFmtVal(tabFmtSeries2.rows[1].cells[0]);
		listFmtSeries[7] = "1";
		listFmtSeries[8] = "";
		boolLegend = false;
	} else if( TMPL_RELY == _intTmpl ){
		var tabFmtSeries1 = document.getElementById("tabFmtSeriesRely1");
		var tabFmtSeries2 = document.getElementById("tabFmtSeriesRely2");
		var tabFmtSeries3 = document.getElementById("tabFmtSeriesRely3");
		var tabFmtSeries4 = document.getElementById("tabFmtSeriesRely4");
		listFmtSeries[0] = "TRUE, TRUE";
		listFmtSeries[1] = "\"none\", \"none\"";
		listFmtSeries[2] = "\"" + getFmtVal(tabFmtSeries1.rows[0].cells[0]) + "\", " +
						   "\"" + getFmtVal(tabFmtSeries3.rows[0].cells[0]) + "\"";
		listFmtSeries[3] = getFmtVal(tabFmtSeries1.rows[1].cells[0]) + ", " +
						   getFmtVal(tabFmtSeries3.rows[1].cells[0]);
		listFmtSeries[4] = "\"" + getFmtVal(tabFmtSeries1.rows[2].cells[0]) + "\", " +
						   "\"" + getFmtVal(tabFmtSeries3.rows[2].cells[0]) + "\"";
		listFmtSeries[5] = getFmtVal(tabFmtSeries2.rows[0].cells[0]) + ", " +
						   getFmtVal(tabFmtSeries4.rows[0].cells[0]);
		listFmtSeries[6] = getFmtVal(tabFmtSeries2.rows[1].cells[0]) + ", " +
						   getFmtVal(tabFmtSeries4.rows[1].cells[0]);
		listFmtSeries[7] = "1, 1";
		listFmtSeries[8] = "";
		boolLegend = false;
	} else {
		var tabFmtSeries = document.getElementById("tabFmtSeries");		
		for(var intRow=0; intRow < tabFmtSeries.rows.length; intRow += 2){
			var listFmtTd = getFmtSeriesVal(intRow);
			for(var i=0; i < listFmtSeries.length; i++){
				var strVal = "";
				if( i == 0 ){ strVal = (getFmtSeriesHide(intRow)? "FALSE" : "TRUE"); }
				else        { strVal = getFmtVal(listFmtTd[i-1]); }
				if( 8 == i && strVal != "" ){ boolLegend = true; }
				if( 1 == i || 2 == i || 4 == i || 8 == i ){ strVal = "\"" + strVal + "\""; }
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
	
	//  axis formatting
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
		
		//  get the field value and format it
		var listSel = listDiv[i].getElementsByTagName("select");
		var strVar = getSelected( listSel[0] )[0];
		if( boolDep ){ /* strVar = strVar.toUpperCase(); */ }
		else         { strVar = strVar.toLowerCase(); }
		
		//  get the selected stats/values and format them
		var listVal = getSelected( listSel[1] );
		if( 1 > listVal.length ){ continue; }
		if( boolDep ){
			for(j in listVal){ listVal[j] = buildModeStatCode(listVal[j], listDiv[i]); }
		}
		
		//  build the XML for the list of values
		var strValXML = "";
		for(j in listVal){ strValXML += "<" + strValTag + ">" + escapeXml(listVal[j]) + "</" + strValTag + ">"; }
		var strValXMLCur = tabField.get(strVar);
		listField.push(strVar);
		tabField.put( strVar, (undefined == strValXMLCur? strValXML : strValXMLCur + strValXML) );		
	}
	
	//  build the XML for each field stored in the table
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
	
	//  if the input stat does not need a code suffix, return it
	if( "mode" != _strPlotData || 
		null != stat.match( /^RATIO_.+/ ) || 
		null != stat.match( /^AREARAT_.+/ ) || 
		null != stat.match( /^OBJ.+/ ) ){
		return stat;
	}
			
	//  determine the first letter of the code [A|F|O|D]
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
	
	//  if the stat is ACOV, return the code
	if( null != stat.match( /^ACOV$/ ) ){ return stat + strCode + "SA"; } 

	//  determine the second letter of the code [A|S|C]
	var boolSimp = divDep.getElementsByTagName("input")[5].checked;
	var boolClus = divDep.getElementsByTagName("input")[6].checked;
	if( boolSimp && boolClus )    { strCode += "A"; }
	else if( boolSimp )           { strCode += "S"; }
	else if( boolClus )           { strCode += "C"; }
	else                          { strCode += "A"; }
	
	//  determine the third letter of the code [A|M|U]
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


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Init XML Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Each loadInitXML function performs a step of the XML upload initialization
 * process, then calls the loadSleep() function, passing it the next function in
 * the chain. When the callback for the current initiazation step is complete,
 * the next function in the chain will be called. Each function parses the
 * appropriate section of the init XML stored in the _strInitXML data member.
 */
function loadInitXML(strInitXML){
	console("loadInitXML() - initializing with xml:\n" + strInitXML + "\n\n");
	_strInitXML = strInitXML;
	_boolDimOverride = true;
	dimScreen(true);
	_boolInitXML = true;
	_intInitXMLChecks = 0;
	setTimeout("loadInitXML_checkProg()", _intInitXMLCheckWait);
	loadSleep("loadInitXML_phaseDB()");
}

var _boolInitXML = false;
var _intInitXMLChecks = 0;
var _intInitXMLChecksMax = 0;
var _intInitXMLCheckMax = 5;
var _intInitXMLCheckWait = 2000;

/**
 * Periodically check the progress of the XML load, making sure that it is not
 * stuck.  If the load does get stuck, print an error message indicating that
 * there has been an error.  Otherwise, do nothing.
 */
function loadInitXML_checkProg(){
	
	//  if the load is complete, stop checking
	if( !_boolInitXML ){ return; }
	
	//  if there is an ongoing server request, set the next check
	if( _boolLoad ){
		_intInitXMLChecks = 0;
		setTimeout("loadInitXML_checkProg()", _intInitXMLCheckWait);
		return;
	}
	
	//  if the number of checks has not been exceeded, increment
	if( _intInitXMLChecks < _intInitXMLCheckMax ){
		_intInitXMLChecks++;
		if( _intInitXMLChecks > _intInitXMLChecksMax ){ _intInitXMLChecksMax = _intInitXMLChecks; }
		setTimeout("loadInitXML_checkProg()", _intInitXMLCheckWait);
	} 
	
	//  if the number of checks has been exceeded, report an error
	else {
		alert("ERROR: javascript error loading XML");
		_boolDimOverride = false;
		dimScreen(false);
		_boolInitXML = false;
	}
}

/**
 * Parse the name of the plot database from _strInitXML, set the database select
 * box and then call the plot_type handler.
 */
function loadInitXML_phaseDB(){
	console("loadInitXML_phaseDB()\n\n");
	_strDBCon = _strInitXML.match( /<database>(.*)<\/database>/ )[1];
	setSelected(document.getElementById("selDB"), _strDBCon);
	loadSleep("loadInitXML_phasePlotType()");
}

/**
 * Parse the plot_type from _strInitXML using assumptions about the first stat
 * name, set the plot_data select box and then call the plot_tmpl handler.
 */
function loadInitXML_phasePlotType(){
	console("loadInitXML_phasePlotType()\n\n");
	var listStat = _strInitXML.match( /<stat>(\w+)<\/stat>/ );
	var strStat = (null != listStat? listStat[1] : "");
	setSelected(document.getElementById("selPlotData"), isModeStat(strStat)? "MODE" :  "Stat");
	updatePlotData();
	loadSleep("loadInitXML_phaseTmpl()");
}

/**
 * Parse the plot_tmpl from _strInitXML, set the plot_tmpl select box and then
 * call the dep handler.
 */
function loadInitXML_phaseTmpl(){
	console("loadInitXML_phaseTmpl()\n\n");
	_strInitXMLPlotTmpl = _strInitXML.match( /<template>(.*)\.R_tmpl<\/template>/ )[1];
	setSelected(document.getElementById("selTemplate"), _strInitXMLPlotTmpl);
	updateTmpl();
	if( isTmplSpc() ){ loadSleep("loadInitXML_phasePlotFix()"); } 
	else             { loadSleep("loadInitXML_phaseDep()");     }
}

//  data structures for managing dep fcst_var/stat pairs
var _tableInitXMLDep1 = new Hashtable();
var _listInitXMLDep1 = new Array();
var _tableInitXMLDep2 = new Hashtable();
var _listInitXMLDep2 = new Array();
var _divInitXMLDep;

/**
 * Parse the _strInitXML dep information and build hashtables and lists
 * containing the fcst_vars and fcst_var/stat pairs. Then, start the chain of
 * calls to load dep controls with those pairs.
 */
function loadInitXML_phaseDep(){
	console("loadInitXML_phaseDep()\n\n");
	
	//  parse the dep 1 structures, creating a hashtable entry for each fcst_var
	var strDep1 = _strInitXML.match( /<dep1>(.*)<\/dep1>/ )[1];
	loadInitXML_buildMap(strDep1, "fcst_var", _tableInitXMLDep1, _listInitXMLDep1);
	
	//  parse the dep 2 structures, creating a hashtable entry for each fcst_var
	if( null == _strInitXML.match( /<dep2\/>/ ) ){
		var strDep2 = _strInitXML.match( /<dep2>(.*)<\/dep2>/ )[1];
		loadInitXML_buildMap(strDep2, "fcst_var", _tableInitXMLDep2, _listInitXMLDep2);
	}
	
	//  start the dep load cycle
	loadInitXML_phaseDepLoad();
}

/**
 * Parse the specified xml, which is assumed to contain fcst_var blocks with
 * value lists, into fcst_var/value (stat or val) pairs and enter each into the
 * specified Hashtable.
 */
function loadInitXML_buildMap(xml, field, map, list){
	var strFcstVar = xml;
	var reg = new RegExp("<" + field + " name=\"([^\"]+)\">(.*?)<\/" + field + ">(.*)");
	while( null != (listFcstVar = strFcstVar.match( reg )) ){
		
		//  remove the enclosing <set>, if necessary
		var strVal = listFcstVar[2];
		strVal = strVal.replace( /<set name=\"\w*\">/, "" );
		strVal = strVal.replace( /<\/set>/, "" );
		
		//  add the list of values to the map with the field name as key
		map.put(listFcstVar[1], strVal);
		list.push(listFcstVar[1]);
		strFcstVar = listFcstVar[3];
	}	
}

/**
 * Recursively loop through the constructed lists of dep fcst_vars, listing the
 * available stats and then selecting them according to the stat information for
 * each fcst_var in the tables. The current fcst_var/stat dep div is set and
 * passed among the functions to update the controls. When the lists are empty,
 * the series loading function is called.
 */
function loadInitXML_phaseDepLoad(){
	console("loadInitXML_phaseDepLoad()\n");
	
	//  if there is a currently loaded stat list, select the stats
	if( _divInitXMLDep != undefined ){
		loadInitXML_phaseDepStats();		
	}
	
	//  if there are no more dep fcst_vars to load, continue to series
	if( 1 > _listInitXMLDep1.length && 1 > _listInitXMLDep2.length ){
		console("loadInitXML_phaseDepLoad() complete\n\n");
		loadSleep("loadInitXML_phaseSeries()");
		return;
	}

	//  determine the current div dep to set controls for, adding one if necessary
	var intDep = 1;
	var listDep = _listDep1Div;
	if( _divInitXMLDep == undefined ){
		console("  dep1 first\n");
		_divInitXMLDep = _listDep1Div[0];
	} else {
		console("  dep" + intDep + " new\n");
		if( 1 > _listInitXMLDep1.length ){ intDep = 2; listDep = _listDep2Div; }
		addDep(intDep);
		_divInitXMLDep = listDep[ listDep.length - 1 ];
	}

	//  load the stats for the next fcst_var
	console("  divDep = " + _divInitXMLDep + "  id = " + getDivDepId(_divInitXMLDep) + "\n");
	var strFcstVar = ( 1 == intDep? _listInitXMLDep1[0] : _listInitXMLDep2[0] );
	console("  fsct_var = " + strFcstVar + "  select = " + _divInitXMLDep.getElementsByTagName("select")[0] + "\n");
	setSelected( _divInitXMLDep.getElementsByTagName("select")[0], strFcstVar );
	selectFcstVarReq( getDivDepId(_divInitXMLDep) );
	
	//  wait for the current fcst_var load to finish, then repeat
	console("\n");
	loadSleep("loadInitXML_phaseDepLoad()");
}

/**
 * Examine the fcst_var lists to determine the next one whose stats must be
 * parsed and selected, removing the fcst_var from the list. The list of stats
 * is parsed and selected in the stats list control. The MODE checkboxes are
 * updated, if appropriate.
 */
function loadInitXML_phaseDepStats(){
	
	//  determine the list of stats to parse and select
	var strStats = "";
	if     ( 0 < _listInitXMLDep1.length ){ strStats = _tableInitXMLDep1.get(_listInitXMLDep1.shift()); }
	else if( 0 < _listInitXMLDep2.length ){ strStats = _tableInitXMLDep2.get(_listInitXMLDep2.shift()); } 
	console("  stats: " + strStats + "\n");
	
	//  parse the stats and select them in the dep stat list
	strStats = strStats.match( /<stat>(.*)<\/stat>/ )[1];
	listStats = strStats.split( /<\/stat><stat>/ );
	listStatsSel = new Array();
	var listMode;
	for(i in listStats){
		
		//  parse the stat name, including the MODE suffix, if appropriate
		var strStat = listStats[i];
		if( null == listStats[i].match( /^RATIO_.+/ ) &&
			null == listStats[i].match( /^AREARAT_.+/ ) &&
			null == listStats[i].match( /^OBJ.+/ ) &&
			null != (listMode = listStats[i].match( /^(\w+)_([FODA])?([SCA])([MUA])$/ )) ){
			strStat = listMode[1];
		}
		
		for(j in listMode){ console("      listMode[" + j + "] = " + listMode[j] + "\n"); }
		
		//  select the stat in the stat list
		listStatsSel.push(strStat);
		console("    stat: " + listStats[i] + " - parsed stat: " + strStat + "\n");		
	}
	setSelected( _divInitXMLDep.getElementsByTagName("select")[1], listStatsSel );
	
	//  if there is a stat MODE suffix, check the appropriate boxes
	if( null != listMode ){
		console("    checking MODE boxes - listMode.length = " + listMode.length + "\n");	
		
		//  determine the MODE checkbox configuration
		var strChkFODA = (undefined == listMode[2]? "" : listMode[2]);
		console("      strChkFODA = " + strChkFODA + "\n");
		
		//  set up the checkboxes for first letter of the code [A|F|O|D]
		_divInitXMLDep.getElementsByTagName("input")[2].checked = (null != strChkFODA.match( /D/ ));
		_divInitXMLDep.getElementsByTagName("input")[3].checked = (null != strChkFODA.match( /[AF]/ ));
		_divInitXMLDep.getElementsByTagName("input")[4].checked = (null != strChkFODA.match( /[AO]/ ));
		
		//  set up the checkboxes for the second letter of the code [A|S|C]
		_divInitXMLDep.getElementsByTagName("input")[5].checked = (null != listMode[3].match( /[AS]/ ));
		_divInitXMLDep.getElementsByTagName("input")[6].checked = (null != listMode[3].match( /[AC]/ ));
		
		//  set up the checkboxes for the third letter of the code [A|M|U]
		_divInitXMLDep.getElementsByTagName("input")[7].checked = (null != listMode[4].match( /[AM]/ ));
		_divInitXMLDep.getElementsByTagName("input")[8].checked = (null != listMode[4].match( /[AU]/ ));		
	}
	updateDepStat( getDivDepId(_divInitXMLDep) );
	console("  stats done\n");
}

//  data structures for managing series field/val pairs
var _tableInitXMLSeries1 = new Hashtable();
var _listInitXMLSeries1 = new Array();
var _tableInitXMLSeries2 = new Hashtable();
var _listInitXMLSeries2 = new Array();
var _divInitXMLSeries;

/**
 * Parse the _strInitXML series information and build hashtables and lists
 * containing the field and field/val pairs. Then, start the chain of calls to
 * load series controls with the pairs.
 */
function loadInitXML_phaseSeries(){	
	console("loadInitXML_phaseSeries()\n\n");
	
	//  parse the series 1 structures, creating a hashtable entry for each field
	var strSeries1 = _strInitXML.match( /<series1>(.*)<\/series1>/ )[1];
	loadInitXML_buildMap(strSeries1, "field", _tableInitXMLSeries1, _listInitXMLSeries1);
	
	//  parse the series 2 structures, creating a hashtable entry for each field
	if( null == _strInitXML.match( /<series2\/>/ ) ){
		var strSeries2 = _strInitXML.match( /<series2>(.*)<\/series2>/ )[1];
		loadInitXML_buildMap(strSeries2, "field", _tableInitXMLSeries2, _listInitXMLSeries2);
	}	
		
	//  start the series load cycle
	loadInitXML_phaseSeriesLoad();
}

/**
 * Recursively loop through the constructed lists of series fields, listing the
 * available values and then selecting them according to the val information for
 * each field in the tables. The current field/val series div is set and passed
 * among the functions to update the controls. When the lists are empty, the
 * plot_fix loading function is called.
 */
function loadInitXML_phaseSeriesLoad(){
	console("loadInitXML_phaseSeriesLoad()\n");
	
	//  if there is a currently loaded val list, select the values
	if( _divInitXMLSeries != undefined ){
		loadInitXML_updateFieldVals( 
			(0 < _listInitXMLSeries1.length? _listInitXMLSeries1  : _listInitXMLSeries2  ),
			(0 < _listInitXMLSeries1.length? _tableInitXMLSeries1 : _tableInitXMLSeries2 ),
			_divInitXMLSeries
		);
	}
	
	//  if there are no more series fcst_vars to load, continue to plot_fix values
	if( 1 > _listInitXMLSeries1.length && 1 > _listInitXMLSeries2.length ){
		console("loadInitXML_phaseSeriesLoad() complete\n\n");
		loadSleep("loadInitXML_phasePlotFix()");
		return;
	}

	//  determine the current div series to set controls for, adding one if necessary
	var intSeries = 1;
	var listSeries = _listSeries1Div;
	if( _divInitXMLSeries == undefined ){
		console("  series1 first - _listSeries1Div.length = " + _listSeries1Div.length + "\n");
		_divInitXMLSeries = _listSeries1Div[0];
	} else {
		if( 1 > _listInitXMLSeries1.length ){ intSeries = 2; listSeries = _listSeries2Div; }
		console("  series" + intSeries + " new\n");
		addSeriesDiv(intSeries);
		_divInitXMLSeries = listSeries[ listSeries.length - 1 ];
	}

	//  load the stats for the next fcst_var
	console("  divSeries = " + _divInitXMLSeries + "  id = " + _divInitXMLSeries.id + "\n");
	var strFcstVar = ( 1 == intSeries? _listInitXMLSeries1[0] : _listInitXMLSeries2[0] );
	console("  field = " + strFcstVar + "  select = " + _divInitXMLSeries.getElementsByTagName("select")[0] + "\n" +
			"  div id = " + _divInitXMLSeries.id + "\n");
	setSelected( _divInitXMLSeries.getElementsByTagName("select")[0], strFcstVar.toUpperCase() );
	if( 1 == intSeries ){ selectSeries1VarReq( getDivFieldValId(_divInitXMLSeries) ); }
	else                { selectSeries2VarReq( getDivFieldValId(_divInitXMLSeries) ); }
	
	//  wait for the current fcst_var load to finish, then repeat
	console("\n");
	loadSleep("loadInitXML_phaseSeriesLoad()");
}

/**
 * After removing the first field on the input list, extract the corresponding
 * list of vals from the specified table. Parse the list of vals and then select
 * them in the val select control contained in the specified fieldVal div.
 */
function loadInitXML_updateFieldVals(list, table, div){
	
	//  determine the list of vals to parse and select
	var strVals = "";
	strVals = table.get(list.shift());
	console("  vals = " + strVals + "\n  select = " + div.getElementsByTagName("select")[1] + "\n");
	
	//  parse the vals and select them in the series val list
	strVals = strVals.match( /<val>(.*)<\/val>/ )[1];
	listVals = strVals.split( /<\/val><val>/ );
	listValsSel = new Array();
	for(i in listVals){
		console("    val = " + listVals[i] + "\n");
		listValsSel.push(listVals[i]);
	}
	setSelected(div.getElementsByTagName("select")[1], listValsSel);
}

//  data structures for managing plot_fix field/val pairs
var _tableInitXMLPlotFix = new Hashtable();
var _listInitXMLPlotFix = new Array();
var _divInitXMLPlotFix;

/**
 * Parse the _strInitXML plot_fix information and build hashtables and lists
 * containing the field and field/val pairs. Then, start the chain of calls to
 * load plot_fix controls with the pairs.
 */
function loadInitXML_phasePlotFix(){
	console("loadInitXML_phasePlotFix()\n");
	
	//  parse the plot_fix structures, creating a hashtable entry for each field
	var strPlotFix = _strInitXML.match( /<plot_fix>(.*)<\/plot_fix>/ )[1];
	loadInitXML_buildMap(strPlotFix, "field", _tableInitXMLPlotFix, _listInitXMLPlotFix);
	
	//  start the series load cycle
	loadInitXML_phasePlotFixLoad();
}

/**
 * Recursively loop through the constructed lists of plot_fix fields, listing
 * the available values and then selecting them according to the val information
 * for each field in the tables. The current field/val plot_fix div is set and
 * passed among the functions to update the controls. When the lists are empty,
 * the indy loading function is called.
 */
function loadInitXML_phasePlotFixLoad(){
	
	//  if there is a currently loaded val list, select the values
	if( _divInitXMLPlotFix != undefined ){
		loadInitXML_updateFieldVals( _listInitXMLPlotFix, _tableInitXMLPlotFix, _divInitXMLPlotFix );
	}
	
	//  if there are no more series fcst_vars to load, continue to plot_fix values
	if( 1 > _listInitXMLPlotFix.length ){
		console("loadInitXML_phasePlotFixLoad() complete\n\n");

		//  go to the next step, which depends on the plot template
		if( isTmplSpc() ){ loadSleep("loadInitXML_phaseFormat()"); }
		else             { loadSleep("loadInitXML_phaseIndy()");   }
		return;
	}

	//  add and set the plot_fix div to set controls for
	if( isTmplSpc() ){ 
		addFixSpcVar();
		_divInitXMLPlotFix = _listFixSpcDiv[_listFixSpcDiv.length - 1];
	} else {
		addFixVar();
		_divInitXMLPlotFix = _listFixDiv[_listFixDiv.length - 1];
	}
	
	
	//  load the stats for the next fcst_var
	console("  divPlotFix = " +	_divInitXMLPlotFix + "\n" +
			"  id = " +			_divInitXMLPlotFix.id + "\n" +
			"  field = " +		_listInitXMLPlotFix[0] + "\n" + 
			"  select = " +		_divInitXMLPlotFix.getElementsByTagName("select")[0] + "\n");
	setSelected( _divInitXMLPlotFix.getElementsByTagName("select")[0], _listInitXMLPlotFix[0].toUpperCase() );
	if( isTmplSpc() ){ selectFixSpcVarReq( getDivFieldValId(_divInitXMLPlotFix) ); }
	else             { selectFixVarReq(   getDivFieldValId(_divInitXMLPlotFix) ); }
	
	//  wait for the current fcst_var load to finish, then repeat
	console("\n");
	loadSleep("loadInitXML_phasePlotFixLoad()");
}

/**
 * Parse the indy var and set the indy var select control to the appropriate
 * value. Then, list the indy var values and call the indy val handler.
 */
function loadInitXML_phaseIndy(){
	console("loadInitXML_phaseIndy()\n");

	//  parse and set the indy var
	var strIndyVar = _strInitXML.match( /<indep name=\"(\w+)\">/ )[1];
	console("  var = " + strIndyVar + "\n");
	setSelected( document.getElementById("selIndyVar"), strIndyVar.toUpperCase() );
	
	//  get the indy vals and call the indy val handler
	console("\n");
	selectIndyVarReq();
	loadSleep("loadInitXML_phaseIndyLoad()");
}

/**
 * When the indy val list is loaded, parse the list of indy vals from the XML
 * and update the indy val list controls. Then, call the formatting handler.
 */
function loadInitXML_phaseIndyLoad(){
	console("loadInitXML_phaseIndyLoad()\n");
	
	//  parse the list of indy values
	var strIndyVals = _strInitXML.match( /<indep[^>]+>(.*)<\/indep>/ )[1];
	console("  strIndyVals = " + strIndyVals + "\n  setting controls\n");
	indyCheck(false);
	
	//  for each indy val on the list, configure the indy val control
	while( 0 < strIndyVals.length ){
		
		//  parse the components of the next indy val
		var listIndyVal = strIndyVals.match( /<val label="([\w\.\-: <>=]*)" plot_val="(\w*)">([\w\.\-: <>=]+)<\/val>(.*)/ );
		//PGO var listIndyVal = strIndyVals.match( /<val label="([\w\.\-:<>=]*)" plot_val="(\w*)">([\w\.\-:<>=]+)<\/val>(.*)/ );
		strIndyVals = listIndyVal[4];
		
		//  find the corresponding indy val table row and set its controls
		console("    val = " + listIndyVal[3] + "\n");
		var trVal = findIndyTr(listIndyVal[3]);
		trVal.getElementsByTagName("input")[0].checked = true;
		trVal.getElementsByTagName("input")[1].value = listIndyVal[1];
		trVal.getElementsByTagName("input")[2].value = listIndyVal[2];
	}
	
	console("  controls complete\nloadInitXML_phaseIndyLoad() complete\n\n");
	loadInitXML_phaseFormat();
}

/**
 * The remaining web app functionality does not require AJAX calls, so it can be
 * performed in a single function. Parse and set the controls in the following
 * areas: - agg_stat - calc_stat - title_lab - plot_fmt - series_fmt - fmt_axis
 */
function loadInitXML_phaseFormat(){
	console("loadInitXML_phaseFormat()\n");
	
	//  parse and set the agg_stat controls
	var listAggInput = document.getElementById("divAggStat").getElementsByTagName("input");
	var boolAggDiff1 = false;
	var boolAggDiff2 = false;
	if( null != _strInitXML.match( /<agg_stat>/ ) ){
		console("  agg_stat\n");
		listAggInput[0].checked = true;
		var strAggCtc = _strInitXML.match( /<agg_ctc>(\w+)<\/agg_ctc>/ )[1];
		listAggInput[1].checked = (strAggCtc == "TRUE");
		console("    agg_ctc = " + strAggCtc + " -> " + (strAggCtc == "TRUE") + " - checked = " + listAggInput[1].checked + "\n");
		var strAggSl1l2 = _strInitXML.match( /<agg_sl1l2>(\w+)<\/agg_sl1l2>/ )[1];
		listAggInput[2].checked = (strAggSl1l2 == "TRUE");
		console("    agg_sl1l2 = " + strAggSl1l2 + " -> " +  + (strAggSl1l2 == "TRUE") + " - checked = " + listAggInput[2].checked + "\n");
		var strAggPct = _strInitXML.match( /<agg_pct>(\w+)<\/agg_pct>/ )[1];
		listAggInput[3].checked = (strAggPct == "TRUE");
		console("    agg_pct = " + strAggPct + " -> " + (strAggPct == "TRUE") + " - checked = " + listAggInput[3].checked + "\n");
		var strAggNbrCnt = _strInitXML.match( /<agg_nbrcnt>(\w+)<\/agg_nbrcnt>/ )[1];
		listAggInput[4].checked = (strAggNbrCnt == "TRUE");
		console("    agg_nbrcnt = " + strAggNbrCnt + " -> " + (strAggNbrCnt == "TRUE") + " - checked = " + listAggInput[4].checked + "\n");
		listAggInput[5].value = _strInitXML.match( /<boot_repl>(\w+)<\/boot_repl>/ )[1];
		listAggInput[6].value = _strInitXML.match( /<agg_diff1>(\w+)<\/agg_diff1>/ )[1];
		boolAggDiff1 = ("TRUE" == listAggInput[5].value);
		//console(" parsed value = " + _strInitXML.match( /<agg_diff1>(\w+)<\/agg_diff1>/ )[1] + " boolAggDiff1 = " + boolAggDiff1 + "\n");
		listAggInput[7].value = _strInitXML.match( /<boot_ci>(\w+)<\/boot_ci>/ )[1];
		listAggInput[8].value = _strInitXML.match( /<agg_diff2>(\w+)<\/agg_diff2>/ )[1];
		boolAggDiff2 = ("TRUE" == listAggInput[7].value);
	} else {
		listAggInput[0].checked = false;
	}
	updateAggStat();
	
	//  parse and set the calc_stat controls
	var listCalcInput = document.getElementById("divCalcStat").getElementsByTagName("input");
	if( null != _strInitXML.match( /<calc_stat>/ ) ){
		console("  calc_stat\n");
		listCalcInput[0].checked = true;
		var strCalcCtc = _strInitXML.match( /<calc_ctc>(\w+)<\/calc_ctc>/ )[1];
		listCalcInput[1].checked = (strCalcCtc == "TRUE");
		var strCalcSl1l2 = _strInitXML.match( /<calc_sl1l2>(\w+)<\/calc_sl1l2>/ )[1];
		listCalcInput[2].checked = (strCalcSl1l2 == "TRUE");
	} else {
		listCalcInput[0].checked = false;
	}
	updateCalcStat();

	//  parse and set the roc_calc controls
	if( TMPL_ROC == _intTmpl ){
		console("  roc_calc\n");
		var listRocCalcInput = document.getElementById("divRocCalc").getElementsByTagName("input");
		var strRocPct = _strInitXML.match( /<roc_pct>(\w+)<\/roc_pct>/ )[1];
		listRocCalcInput[0].checked = (strRocPct == "TRUE");
		var strRocCtc = _strInitXML.match( /<roc_ctc>(\w+)<\/roc_ctc>/ )[1];
		listRocCalcInput[1].checked = (strRocCtc == "TRUE");
	}
		
	//  parse and set the tmpl information
	var listTmplInput = document.getElementById("divTitleLab").getElementsByTagName("input");
	var listLab;
	console("  title and labels\n");
	listTmplInput[0].value = ( null != (listLab = _strInitXML.match( /<title>(.+)<\/title>/       )) ? listLab[1] : "" );
	listTmplInput[1].value = ( null != (listLab = _strInitXML.match( /<x_label>(.+)<\/x_label>/   )) ? listLab[1] : "" );
	listTmplInput[2].value = ( null != (listLab = _strInitXML.match( /<y1_label>(.+)<\/y1_label>/ )) ? listLab[1] : "" );
	listTmplInput[3].value = ( null != (listLab = _strInitXML.match( /<y2_label>(.+)<\/y2_label>/ )) ? listLab[1] : "" );
	listTmplInput[4].value = ( null != (listLab = _strInitXML.match( /<caption>(.+)<\/caption>/   )) ? listLab[1] : "" );
	
	//  plot formatting bool flags
	var tabFmtPlotBool = document.getElementById("tabFmtPlotBool");
	console("  fmt_plot bool\n");
	for(var i=0; i < tabFmtPlotBool.rows.length; i++){
		for(var j=0; j < tabFmtPlotBool.rows[i].cells.length; j++){

			//  determine the name of the flag
			var listTdBool = tabFmtPlotBool.rows[i].cells[j].getElementsByTagName("td");
			if( 1 > listTdBool.length ){ continue; }
			var strBoolName = listTdBool[2].innerHTML;
			
			//  parse the flag value from the xml and set the control appropriately
			var regBool = new RegExp( "<" + strBoolName + ">(\\w+)<\/" + strBoolName + ">" );
			var selBool = tabFmtPlotBool.rows[i].cells[j].getElementsByTagName("select")[0];
			if     ( strBoolName == "plot1_diff" && boolAggDiff1          ){ setSelected( selBool, "TRUE" );         } 
			else if( strBoolName == "plot2_diff" && boolAggDiff2          ){ setSelected( selBool, "TRUE" );         }
			else if( null != (listBoolVal = _strInitXML.match( regBool )) ){ setSelected( selBool, listBoolVal[1] ); }
			else { console("    WARNING: fmt_plot bool property " + strBoolName + " not found\n"); }
		}
	}
	buildSeriesDiv();

	//  plot formatting text values
	var tabFmtPlotTxt = document.getElementById("tabFmtPlotTxt");
	console("  fmt_plot txt\n");
	for(var i=0; i < tabFmtPlotTxt.rows.length; i++){
		for(var j=0; j < tabFmtPlotTxt.rows[i].cells.length; j++){

			//  determine the name of the property
			var listTdTxt = tabFmtPlotTxt.rows[i].cells[j].getElementsByTagName("td");
			if( 1 > listTdTxt.length ){ continue; }
			var strTxtName = listTdTxt[2].innerHTML;
			console("    txt property = " + strTxtName + " - ");
			
			//  parse the flag value from the xml and set the control appropriately
			var regTxt = new RegExp( "<" + strTxtName + ">(.+)<\/" + strTxtName + ">" );
			if( null != (listTxtVal = _strInitXML.match( regTxt )) ){
				var inputTxtTxt = listTdTxt[1].getElementsByTagName("input")[0];
				var inputTxtSel = listTdTxt[1].getElementsByTagName("select")[0];
				if( inputTxtTxt.style.display != "none" ){
					console("text box = " + inputTxtTxt + " value = " + listTxtVal[1] + "\n");
					inputTxtTxt.value = listTxtVal[1];
				} else {
					console("select = " + inputTxtSel + " value = " + listTxtVal[1] + "\n");
					setSelected( inputTxtSel, listTxtVal[1] );
				}
			} else { console("WARNING: fmt_plot txt property " + strTxtName + " not found\n"); }
		}
	}

	//  initialize the list of formatting values, depending on template
	var listParm = ["plot_ci", "colors", "pch", "type", "lty", "lwd", "con_series", "legend", "plot_disp"];
	switch(_intTmpl){
	case TMPL_RHIST: listParm = ["colors", "lwd", "legend"];             break;
	case TMPL_ROC:   listParm = ["colors", "pch", "type", "lty", "lwd"]; break;
	case TMPL_RELY:  listParm = ["colors", "pch", "type", "lty", "lwd"]; break;
	}

	//  series formatting
	console("  fmt_series txt\n");
	for(var i in listParm){
		console("    " + listParm[i]);
		
		//  parse the values for the current format setting
		var regFmt = new RegExp("<" + listParm[i] + ">c\\((.*)\\)<\/" + listParm[i] + ">");
		var listFmtVal;
		if( null == (listFmtVal = _strInitXML.match( regFmt )) ){
			console(" not found\n");
			continue;
		}
		
		//  loop through the list of formatting values and controls
		console(" found - " + listFmtVal[1] + "\n");
		listFmtVal = listFmtVal[1].split(",");
		intFmtRow = 0;
		for(var j in listFmtVal){			
			var listFmtTd = getFmtSeriesVal(intFmtRow);
			
			//  strip leading spaces and quotes from the format value
			var strFmtVal = listFmtVal[j].replace(/^\s*\"?/, "").replace(/\"?\s*$/, "");
			console("      " + strFmtVal + " - ");
			
			//  if the parameter is plot_disp, set the hide checkbox
			if( listParm[i] == "plot_disp" ){
				console("hide chk\n");
				setFmtSeriesHide(intFmtRow, (strFmtVal == "FALSE"));
				intFmtRow += 2;
				continue;
			}
			
			//  locate the format control in the series format list and set it
			var listFmtTdParm = listFmtTd[i].getElementsByTagName("td");
			var listFmtTxt = listFmtTdParm[1].getElementsByTagName("input");			
			var listFmtSel = listFmtTdParm[1].getElementsByTagName("select");
			if( null != listFmtTxt && 0 < listFmtTxt.length ){				
				console("text\n");
				listFmtTxt[0].value = strFmtVal;
			} else if( null != listFmtSel && 0 < listFmtSel.length ){
				console("select\n");
				setSelected(listFmtSel[0], strFmtVal);
			} else {
				console("WARNING: series format control not found\n");
			}
			
			//  if setting the first parameter (plot_ci), set the series formatting section to modified
			if( 0 == i && !isTmplSpc() ){ setFmtSeriesMod(j, true); }

			intFmtRow += 2;
		}
		
	}
	
	//  axis formatting
	var listFmtAxisInput = document.getElementById("divFmtAxis").getElementsByTagName("input");
	var listFmtAxis;
	console("  axis formatting\n");
	listFmtAxisInput[0].value = ( null != (listFmtAxis = _strInitXML.match( /<y1_lim>(.+)<\/y1_lim>/   )) ? listFmtAxis[1] : "" );
	listFmtAxisInput[1].value = ( null != (listFmtAxis = _strInitXML.match( /<y1_bufr>(.+)<\/y1_bufr>/ )) ? listFmtAxis[1] : "" );
	listFmtAxisInput[2].value = ( null != (listFmtAxis = _strInitXML.match( /<y2_lim>(.+)<\/y2_lim>/   )) ? listFmtAxis[1] : "" );
	listFmtAxisInput[3].value = ( null != (listFmtAxis = _strInitXML.match( /<y2_bufr>(.+)<\/y2_bufr>/ )) ? listFmtAxis[1] : "" );
		
	//  turn off the dimmer
	console("  max checks: " + _intInitXMLChecksMax + "\nloadInitXML_phaseFormat() complete\n\n");
	_boolDimOverride = false;
	dimScreen(false);
	_boolInitXML = false;
}



var _boolIE = false;
var _boolDim = false;

var _url;

var _intDebugState = 0;
var _boolDebugDisp = false;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Administration/Utility Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function parse(){
	var strData = document.getElementById("txtData").value;
	var strPattern = document.getElementById("txtPattern").value;

	console("parse()\n  data: " + strData + "\n  pattern: " + strPattern + "\n");

	var pat = new RegExp(strPattern);
	var listParse = strData.match( pat );
	if( null != listParse ){
		console("  match:\n");
		for(i in listParse){ console("    listParse[" + i + "]: " + listParse[i] + "\n"); }
	} else {
		console("  no match\n");
	}

	console("parse() complete\n\n");
}

/**
 * Test functions related to the GET and POST test controls
 */
function testPostReq()    { sendRequest("POST", document.getElementById("txtPost").value, nullResp); }
function testGetReq()     { sendRequest("GET", document.getElementById("txtGet").value, nullResp);   }

/**
 * Wrapper for appending information to the console text box
 */
function console(str){ document.getElementById("txtConsole").value += str; }
function consoleClear(){ document.getElementById("txtConsole").value = ""; }

/**
 * Dims the screen while the web app is waiting for the servlet
 */
function dimScreen(boolDim){

	// adjust the size and location of the dim screen elements
	var divLoading = document.getElementById("divLoading");
	var divDimScreen = document.getElementById("divDimScreen");
	if( !_boolIE ){
	    divLoading.style.left = (window.innerWidth / 2) - 60 + window.pageXOffset;
	    divLoading.style.top = (window.innerHeight / 2) - 40 + window.pageYOffset;
	    divDimScreen.style.left = window.pageXOffset;
	    divDimScreen.style.top = window.pageYOffset;
	    divDimScreen.style.width = window.innerWidth;
	    divDimScreen.style.heigth = window.innerHeight;
	} else {
		divLoading.style.left = (document.body.clientWidth / 2) - 60 + document.body.scrollLeft;
		divLoading.style.top = (document.body.clientHeight / 2) - 40 + document.body.scrollTop;
	    divDimScreen.style.left = document.body.scrollLeft;
	    divDimScreen.style.top = document.body.scrollTop;
	    divDimScreen.style.width = document.body.clientWidth;
	    divDimScreen.style.heigth = document.body.clientHeight;
	}

	// display the dim screen controls
	_boolDim = boolDim;
	document.title = "METViewer" + (boolDim? " - Loading..." : "");
	document.getElementById("divDimScreen").style.display = (boolDim? "block" : "none");
	document.getElementById("divLoading").style.display = (boolDim? "block" : "none");
}

function debugClick(ctrl){
	if     ( "title" == ctrl && 0 == _intDebugState ){ _intDebugState++; }
	else if( "db"    == ctrl && 1 == _intDebugState ){ _intDebugState++; }
	else if( "title" == ctrl && 2 == _intDebugState ){ setDebugDisp(!_boolDebugDisp); _intDebugState = 0; }
	else{ _intDebugState = 0; }
}

function setDebugDisp(show){
	
	// update the visibility of the debug console and test buttons
	document.getElementById("trDebug").style.display = (show? "table-row" : "none");
	
	// update the handler for the loading screen
	var divLoading = document.getElementById("divLoading");
	divLoading.setAttribute("onclick", (show? "javascript:dimScreen(false)" : ""));
 	if( _boolIE ){ divLoading.attachEvent("onclick", new Function(show? "dimScreen(false)" : "")); }
	
	// update the visibility of the Serialize links
	for(var i=0; i < _listLnkSer.length; i++){
		document.getElementById("lnkSer" + _listLnkSer[i]).style.display = (show? "inline" : "none");
	}
	
	_boolDebugDisp = show;
}

/**
 * Debugging facility which dumps the DHTML for the input element into the
 * console
 */
function serialize(strId){ serializeNode(document.getElementById(strId)); }
function serializeNode(node){
	var strXML = "";
	try     { strXML = XML( (new XMLSerializer()).serializeToString(node) ).toXMLString(); }
	catch(e){ strXML = node.outerHTML; }
	console("\n" + strXML + "\n\n");
}

/**
 * Clear and populate a select list with the specified id with the items in the
 * specified list
 */
function fillSelect(sel, listOpt){
	clearSelect(sel);
	for(i in listOpt){
		var opt = document.createElement("option");
		opt.text = listOpt[i];
		try{ sel.add(opt, null); }catch(ex){ sel.add(opt); }
	}
}

/**
 * Remove all elements from a select control
 */
function clearSelect(sel){ while( 0 < sel.length ){ sel.remove(sel.length - 1); } }

/**
 * Build a list of the selected items in the specified select control
 */
function getSelected(sel){
	var listRet = new Array();
	for(var i=0; i < sel.options.length; i++){
		if( sel.options[i].selected ){ listRet.push( sel.options[i].text ); }
	}
	return listRet;
}

/**
 * Options in the specified select control will be selected if they are present
 * in the specified list of inputs and not selected if they are not.
 */
function setSelected(sel, val){
	if( !(val instanceof Array) ){ val = [val]; }
	var listOpt = new Array();
	for(var i=0; i < sel.options.length; i++){ listOpt.push( sel.options[i].text ); }
	for(var i=0; i < listOpt.length; i++){
		sel.options[i].selected = (-1 != listSearch( listOpt[i], val ));
	}
}

/**
 * Set the disabled status of the input button to the input state
 */
function enableButton(btn, enabled){
	btn.style.color	= (enabled? "#434A44" : "#939A94");
	btn.disabled	= (enabled? ""        : "disabled");
}

/**
 * A simple data structure for storing the components of a parsed <list_val>
 * reponse
 */
function ListValResp(id, vals){
	this.id = id;
	this.vals = vals;
}

/**
 * Parse the specified <list_val> server response XML and return the results in
 * a listVal object
 */
function parseListValResp(strResp, strType){
	//var listProc = strResp.match( new RegExp("<list_" + strType + ">(?:<id>(\\d+)<\/id>)?<val>(.*)<\/val><\/list_" + strType + ">") );
	var listProc = strResp.match( new RegExp("<list_" + strType + ">(?:<id>(\\d+)<\/id>)?<val>([\\s\\S]*)<\/val><\/list_" + strType + ">") );
	if( null == listProc ){
		console("parseListValResp() - ERROR: could not parse response: " + strResp + "\n\n");
		return null;
	}
	return new ListValResp(listProc[1], listProc[2].split( /<\/val><val>/ ));
}

/**
 * It is assumed that the input list contains strings. The elements of list are
 * searched for an exact match to the input string val and the first index
 * containing a match is returned. If no match is found, -1 is returned.
 */
function listSearch(val, list){
	for(i in list){ if( list[i] == val ){ return i; } }
	return -1;
}

/**
 * Construct a list of RGBA color hex represenations with the specified length
 * and format #RRGGBBAA, where the colors are spaced equally along the rainbow
 * spectrum.
 */
function rainbow(num){
	if( 1 > num )	{ return new Array();	}
	if( 1 == num )	{ return ["#FF0000FF"]; }

	var listRet = new Array();
	var dblInc = 1.0 / (num - 1);
	var dblVal = 0;
	for(var i=0; i < num; i++, dblVal += dblInc){ listRet.push("#" + interpolateColor(dblVal) +  "FF"); }
	return listRet;
}

/**
 * Create a hex representation of the specified "rainbow" color along the
 * spectrum from 0 (red, FF0000) to 1 (violet, FF00FF).
 */
function interpolateColor(rel){
	if     ( rel < 0.0 ) { return "FF0000"; }
	else if( rel > 1.0 ) { return "FF00FF"; }

	var min = 0;
	var max = 1;
	
	switch( Math.floor(rel/.2) ){
		case 0:					return hex(max) + hex(max*(min + (1-min)*(rel/.2))) + hex(min);
		case 1:	rel -= .2;		return hex(min + max*(1-min)*(1 - rel/.2)) + hex(max) + hex(min);
		case 2:	rel -= .4;		return hex(min) + hex(max) + hex(max*(min + (1-min)*(rel/.2)));
		case 3:	rel -= .6;		return hex(min) + hex(max*(1-min)*(1 - rel/.2)) + hex(max);
		case 4:	rel -= .8;		return hex(max*(min + (1-min)*(rel/.2))*.5) + hex(min) + hex(max);
		default:				return hex(max*.5) + hex(min) + hex(max);
	}
}

/**
 * Create the two character hexadecimal representation of specified value,
 * multiplied by 255. The intended use is to create an RGB representation with
 * 8-bit color depth.
 */
function hex(val){
	var strRet = Math.round(val * 255).toString(16).toUpperCase();
	while( 2 > strRet.length ){ strRet = "0" + strRet; }
	return strRet;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * AJAX Functions
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Create a request object in a browser-dependent manner
 */
function getRequest(){
	var req;
	if (window.XMLHttpRequest) { req = new XMLHttpRequest(); }
	else if (window.ActiveXObject) {
		try{ req = new ActiveXObject("Microsoft.XMLHTTP"); }
		catch (e) { try { req = new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {} }
	}
	return req;
}

/**
 * Send an XML request to the server asynchronously, and call processResponse()
 * when the response arrives
 */
function sendRequest(reqType, reqData, fnResp){

	dimScreen(true);

	// add the database connection to the request, if appropriate
	if( null == reqData.match( /<list_db\/>/ ) ){ reqData = "<db_con>" + _strDBCon + "</db_con>" + reqData; }
	console("sendRequest() - request: " + reqData + "\n");

	// set the request to wait until the data is ready
	var strResp = "";
	var req = getRequest();
	req.onreadystatechange =
    function() {
		if (req.readyState == 4) {

			// classify the response type
			if (req.status == 200) { strResp = req.responseText; }
			else                   { strResp = "<error>req.status " + req.status + "</error>"; }

			// dispatch the response
			console("sendRequest() - response: " + strResp + "\n\n");
			var listParse = strResp.match( /<error>([\s\S]*)<\/error>/ );
			if( null != listParse ){ alert("METViewer error: " + listParse[1]); }
			else                   { fnResp(strResp);                           }
			dimScreen(false);
        }
	};

	var reqURL = _url + "?date=" + (new Date()).getMilliseconds();

	// send the request in the manner specified
    if( reqType == "GET" ){
		req.open(reqType, reqURL + "&data=" + encodeURI(reqData), true);
		req.send(null);
	} else {
		var reqPostData = "<request>" + reqData + "<date>" + (new Date()).getMilliseconds() + "</date></request>";
		req.open(reqType, reqURL, true);
		req.setRequestHeader("Content-Type", "text/xml");
		req.setRequestHeader("Content-length", reqPostData.length);
		req.setRequestHeader("Connection", "close");
		req.send(reqPostData);
	}
}

/**
 * A response handler that does nothing
 */
function nullResp(strResp){}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Database Controls
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Request and process the list of databases, populating the database list
 * select control
 */
function listDBReq(){ sendRequest("POST", "<list_db/>", listDBResp); }
function listDBResp(strResp){
	var resp = parseListValResp(strResp, "db");
	fillSelect(document.getElementById("selDB"), resp.vals);
	updateDBCon();
}

/**
 * When the selected database is changed, update the data members and controls
 */
function updateDBCon(){
	var selDB = document.getElementById("selDB");
	_strDBCon = selDB.options[selDB.selectedIndex].text;
	console("updateDBCon() - _strDBCon: " + _strDBCon + "\n\n");

	// reset the controls
	clearControls();
}



var _strDBCon = "";

var _listLnkSer = ["InstInfo"];

var _tabMVRev = null;
var _tabInstInfo = null;
var _btnAdd = null;
var _btnUpdate = null;
var _txtInfoId = null;
var _txtInfoUpdater = null;
var _txtInfoDate = null;
var _txtInfoDetail = null;

/**
 * When the page loads, perform the initialization duties including setting
 * pointers to DHTML elements and loading the list of databases.
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
	
	//  initialize the global variables
	_tabMVRev		= document.getElementById("tabMVRev");
	_tabInstInfo	= document.getElementById("tabInstInfo");
	_btnAdd			= document.getElementById("btnAdd");
	_btnUpdate		= document.getElementById("btnUpdate");
	_txtInfoId		= document.getElementById("txtInfoId");
	_txtInfoUpdater	= document.getElementById("txtInfoUpdater");
	_txtInfoDate	= document.getElementById("txtInfoDate");
	_txtInfoDetail	= document.getElementById("txtInfoDetail");
}

function clearControls(){
	dimScreen(true);
	fillMVRevReq();
	fillInstInfoReq();
	clearInstInfo();
	dimScreen(false);
}

/**
 * fillMVRev functions request the list of mv_rev database table records from the database
 * and populate the database schema history table with the results.
 */
function fillMVRevReq(){ sendRequest("POST", "<list_mv_rev/>", fillMVRevResp); }
function fillMVRevResp(strResp){
	
	//  clear the mv_rev table
	while( 2 < _tabMVRev.rows.length ){ _tabMVRev.deleteRow(_tabMVRev.rows.length - 1); }

	//  parse the mv_rev records
	var resp = parseListValResp(strResp, "mv_rev");
	if( null == resp ){ return; }

	//  add each record information to a row in the table
	for(i in resp.vals){
		
		//  add a new row to the table
		var tr = _tabMVRev.rows[1].cloneNode(true);
		tr.style.display = "table-row";
		_tabMVRev.appendChild( tr );
		
		//  populate the cells of the new row
		var strMVRev = resp.vals[i];		
		tr.cells[0].innerHTML = strMVRev.match( /<rev_id>(.*)<\/rev_id>/ )[1];
		tr.cells[1].innerHTML = strMVRev.match( /<rev_date>(.*)<\/rev_date>/ )[1];
		tr.cells[2].innerHTML = strMVRev.match( /<rev_name>(.*)<\/rev_name>/ )[1];
		tr.cells[3].innerHTML = strMVRev.match( /<rev_detail>(.*)<\/rev_detail>/ )[1];
	}
	
}

/**
 * fillMVRev functions request the list of mv_rev database table records from the database
 * and populate the database schema history table with the results.
 */
function fillInstInfoReq(){ sendRequest("POST", "<list_inst_info/>", fillInstInfoResp); }
function fillInstInfoResp(strResp){
	
	//  clear the mv_rev table
	while( 3 < _tabInstInfo.rows.length ){ _tabInstInfo.deleteRow(_tabInstInfo.rows.length - 1); }
	
	//  if there are no records, display the no records message
	if( null == strResp.match( /<val>/ ) ){
		_tabInstInfo.rows[2].style.display = "table-row";		
		return;
	} else {
		_tabInstInfo.rows[2].style.display = "none";		
	}

	//  parse the mv_rev records
	var resp = parseListValResp(strResp, "inst_info");
	if( null == resp ){ return; }

	//  add each record information to a row in the table
	for(i in resp.vals){
		
		//  add a new row to the table
		var tr = _tabInstInfo.rows[1].cloneNode(true);
		tr.style.display = "table-row";
		_tabInstInfo.appendChild( tr );
		
		//  populate the cells of the new row
		var strInstInfo = resp.vals[i];
		var strInfoId = strInstInfo.match( /<info_id>(.*)<\/info_id>/ )[1];
		tr.cells[1].innerHTML = strInfoId;
		tr.cells[2].innerHTML = strInstInfo.match( /<info_updater>(.*)<\/info_updater>/ )[1];
		tr.cells[3].innerHTML = strInstInfo.match( /<info_date>(.*)<\/info_date>/ )[1];
		tr.cells[4].innerHTML = strInstInfo.match( /<info_detail>([\s\S]*)<\/info_detail>/ )[1];
		
		//  update the Edit button event handler call with the info_id
		var btnEdit = tr.getElementsByTagName("input")[0];
		btnEdit.setAttribute("onclick", "javascript:editInstInfoRec(" + strInfoId + ")");
	 	if( _boolIE ){ btnEdit.attachEvent("onclick", new Function("editInstInfoRec(" + strInfoId + ")")); }

		//  update the View XML button event handler call with the info_id and toggle visibility
		var strLoadXML = strInstInfo.match( /<info_xml>(.*)<\/info_xml>/ )[1];
	 	var lnkXML = tr.getElementsByTagName("a")[0];
	 	if( "true" == strLoadXML ){
	 		lnkXML.setAttribute("onclick", "javascript:viewInstInfoXMLReq(" + strInfoId + ")");
	 		if( _boolIE ){ lnkXML.attachEvent("onclick", new Function("viewInstInfoXMLReq(" + strInfoId + ")")); }
	 	} else {
	 		tr.cells[5].innerHTML = "&nbsp;";
	 	}
	}
	
	clearInstInfo();
}

/**
 * Assume that the input id corresponds to the instance_info_id of the record to be
 * updated, and populate the edit info textbox controls with the record information.  
 * @param id instance_info_id of the record to populate the controls with
 */
function editInstInfoRec(id){
	
	//  find the table row with the requested data
	var tr = null;
	for(var i=3; i < _tabInstInfo.rows.length; i++){
		var strInfoId = _tabInstInfo.rows[i].cells[1].innerHTML;
		if( strInfoId == id ){ tr = _tabInstInfo.rows[i]; }
	}
	
	//  verify the table row
	if( null == tr ){
		console("ERROR: data for instance_info_id = " + id + " not found\n\n");
		return;
	}
	
	//  populate the edit controls with the information in the selected row
	_txtInfoId.value		= tr.cells[1].innerHTML;
	_txtInfoUpdater.value	= tr.cells[2].innerHTML;
	_txtInfoDate.value		= tr.cells[3].innerHTML;
	_txtInfoDetail.value	= tr.cells[4].innerHTML;
	
	//  set the button states appropriately
	enableButton(_btnAdd, false);
	enableButton(_btnUpdate, true);
}

/**
 * Clear the edit instance info textbox controls and set the info_id control to the next
 * available instance_info_id.  
 */
function clearInstInfo(){
	
	//  determine the next instance_info_id
	var intIdNext = 0;
	if( 3 < _tabInstInfo.rows.length ){
		intIdNext = Number(_tabInstInfo.rows[ _tabInstInfo.rows.length - 1 ].cells[1].innerHTML);
		intIdNext += 1;
	}

	//  set the instance_info_id and clear the text box controls
	_txtInfoId.value		= intIdNext;
	_txtInfoUpdater.value	= "";
	_txtInfoDate.value		= "";
	_txtInfoDetail.value	= "";
	
	//  set the button states appropriately
	enableButton(_btnAdd, true);
	enableButton(_btnUpdate, false);
}

/**
 * Build an XML string containing the serialized information from the instance info text controls.
 * @return serialized instance info data
 */
function buildInstInfoXML(){
	var strRet = 
		"<info_id>" + _txtInfoId.value + "</info_id>" +
		"<info_updater>" + _txtInfoUpdater.value + "</info_updater>" +
		"<info_date>" + _txtInfoDate.value + "</info_date>" +
		"<info_detail>" + _txtInfoDetail.value + "</info_detail>";
	return strRet; 
}

/**
 * Send an update_inst_info request to the server containing the information in the text
 * controls.  When the response arrives, print the message contained within.
 */
function updateInstInfoReq(){
	sendRequest("POST", "<update_inst_info>" + buildInstInfoXML() + "</update_inst_info>", updateInstInfoResp);
}
function updateInstInfoResp(strResp){
	var listProc = null;
	if( null != (listProc = strResp.match( /<error>(.*)<\/error>/ )) ){ alert("ERROR: " + listProc[1]); }
	clearControls();
}

/**
 * Send an ADD_inst_info request to the server containing the information in the text
 * controls.  When the response arrives, print the message contained within.
 */
function addInstInfoReq(){
	sendRequest("POST", "<add_inst_info>" + buildInstInfoXML() + "</add_inst_info>", updateInstInfoResp);
}
function addInstInfoResp(strResp){
	var listProc = null;
	if( null != (listProc = strResp.match( /<error>(.*)<\/error>/ )) ){ alert("ERROR: " + listProc[1]); }
	clearControls();
}

/**
 * Send a server request to view the load XML file with the specified instance_info_id.  When
 * the response arrives, open a new browser window showing the XML. 
 * @param id instance_info_id whose load XML will be fetched
 */
function viewInstInfoXMLReq(id){
	sendRequest("POST", "<view_load_xml><info_id>" + id + "</info_id></view_load_xml>", viewInstInfoXMLResp);
}
function viewInstInfoXMLResp(strResp){
	if( null != (listProc = strResp.match( /<view_load_xml>(.*)<\/view_load_xml>/ )) ){
		var win = window.open("xml/" + listProc[1] + ".xml", listProc[1]);
	} else {
		alert("ERROR: failed to parse <view_load_xml> result: " + strResp);
	}
}

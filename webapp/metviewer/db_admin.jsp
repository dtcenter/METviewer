<%@ page import="java.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>METViewer - Database Administration</title>
<link rel="stylesheet" type="text/css" href="css/metviewer.css"/>
<link rel="shortcut icon" href="include/ral_icon.ico" type="image/x-icon"/>
</head>

<body onLoad="onLoad()" style="padding-left: 20px">

<script type="text/javascript" src="include/Hashtable.js"></script>
<script type="text/javascript" src="include/util.js"></script>
<script type="text/javascript" src="include/db_admin.js"></script>

<%

//  if there is a database input parameter, put its value into the db load control
String strDB = "";
String strDBVal = request.getParameter("db");
if( null != strDBVal ){ strDB = "mv_" + strDBVal; }

%>

<div id="divDimScreen" class="dimScreen">&nbsp;</div>
<div id="divLoading" class="loading" align="center" onclick="javascript:dimScreen(false)">Loading...<br/>
<img src="include/loading_white.gif" style="padding-top:10px"/></div>

<table width="100%" height="100%" cellspacing="0" cellpadding="5" border="0">
	<tr><td class="title"><span onclick="javascript:debugClick('title')">METViewer</span>&nbsp;<span class="stdTiny">v0.5</span></td></tr>
	<tr><td><hr/></td></tr>

	<tr><td>
		<!--
		<input type="text" id="txtPost"/>
		<input type="button" class="gButton" onclick="javascript:testPostReq()" value="Post"/><br/><br/>
		<input type="text" id="txtGet"/>
		<input type="button" class="gButton" onclick="javascript:testGetReq()" value="Get"/><br/><br/>
		 -->
		<span class="header" onclick="javascript:debugClick('db')">Database:</span>&nbsp;&nbsp;
		<select id="selDB" onChange="javascript:updateDBCon()"></select><span class="bold" id="spanDBLoad"><%= strDB %></span>
		<!-- <input type="button" class="gButton" onclick="javascript:listDBReq()" value="List"/> -->
	</td></tr>
	
	<tr><td height="100%" style="vertical-align: top"><span id="spanPlotControls" style="display:inline">
	
		<br/><br/>
		<div id="divInstInfo">
		<span class="header">Database Instance Information</span>
		<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divInstInfo')" id="lnkSerInstInfo">Serialize</a><br/><br/>
		<table id="tabInstInfo" border="1">
			<tr>
				<th class="gray" width="50">&nbsp;</th>
				<th class="gray" width="50">info_id</th>
				<th class="gray" width="200">updater</th>
				<th class="gray" width="200">update_date</th>
				<th class="gray">update_detail</th>
				<th class="gray" width="50">load_xml</th>
			</tr>
			<tr style="display:none">
				<td align="center"><input type="button" class="gButton" onclick="javascript:editInstInfoRec(-1)" value="Edit"></input></td>
				<td align="center">0</td>
				<td align="center">pgoldenb</td>
				<td align="center">2011-02-20 12:00:00</td>
				<td align="left"  >Added new table tread_mark and deleted column from table flat_tire</td>
				<td align="center">
					<a onclick="javascript:viewInstInfoXML(-1)" style="color:#3366cc; text-decoration:underline">View XML</a>
				</td>
			</tr>		
			<tr>
				<td align="center" colspan="6"><b>no records found</b></td>
			</tr>		
		</table>
		</div>
			
		<br/><br/>
		<span class="bold" style="padding-left:40px">Database Instance Record</span><br/><br/>
		<table id="tabInstInfo" border="0" style="padding-left:40px">
			<tr>
				<td align="right">info_id:</td>
				<td align="left"><input readonly="readonly" type="text" size="12" id="txtInfoId"></input></td>
			</tr>
			<tr>
				<td align="right">updater:</td>
				<td align="left"><input type="text" size="12" id="txtInfoUpdater"></input></td>
			</tr>
			<tr>
				<td align="right">update_date:</td>
				<td align="left"><input type="text" size="20" id="txtInfoDate"></input></td>
			</tr>
			<tr>
				<td align="right" style="vertical-align:top">update_detail:</td>
				<td align="left"><textarea rows="4" cols="60" id="txtInfoDetail"></textarea></td>
			</tr>
			<tr style="display:none">
				<td align="right" style="vertical-align:top">load_xml:</td>
				<td align="left"><textarea rows="4" cols="60" id="txtLoadXML"></textarea></td>
			</tr>
			<tr>
				<td align="right"><br/><br/></td>
				<td align="left">
					<input type="button" class="gButton" id="btnClear"  onclick="javascript:clearInstInfo()"     value="Clear"/>&nbsp;&nbsp;
					<input type="button" class="gButton" id="btnAdd"    onclick="javascript:addInstInfoReq()"    value="Add"/>&nbsp;&nbsp;
					<input type="button" class="gButton" id="btnUpdate" onclick="javascript:updateInstInfoReq()" value="Update"/>&nbsp;&nbsp;
				</td>
			</tr>
		</table><br/>
		<span style="padding-left:40px">
			
		</span>

		<br/><br/>
		<br/><br/>
		<span class="header">Database Schema Revision History</span><br/><br/>
		<table id="tabMVRev" border="1" width="80%">
			<tr>
				<th class="gray" width="50">rev_id</th>
				<th class="gray" width="200">rev_date</th>
				<th class="gray" width="50">rev_name</th>
				<th class="gray">rev_detail</th>
			</tr>
			<tr style="display:none">
				<td align="center">0</td>
				<td align="center">2011-02-20 12:00:00</td>
				<td align="center">0.3</td>
				<td align="left"  >Added new table tread_mark and deleted column from table flat_tire</td>
			</tr>		
		</table>
		<br/><br/>
	
	</span></td></tr>	

	<tr id="trDebug"><td>
		<hr/>
		<span class="header">Regex Test</span>
		<table>
			<tr>
				<td>Data:</td>
				<td><input type="text" id="txtData" size="50"/></td>
			</tr>
			<tr>
				<td>Pattern:</td>
				<td><input type="text" id="txtPattern" size="50"/></td>
			</tr>
			<tr><td colspan="2"><input type="button" class="gButton"onclick="javascript:parse()" value="Parse"/></td></tr>
		</table><br/><br/>

		<b>Console:</b>
		<input type="button" class="gButton" onclick="javascript:consoleClear()" value="Clear"/>&nbsp;&nbsp;
		<input type="button" class="gButton" onclick="javascript:dimScreen(true)" value="Dim"/><br/><br/>
		<textarea rows="20" cols="160" id="txtConsole"></textarea></td>
	</tr>

</table>

</body>
</html>

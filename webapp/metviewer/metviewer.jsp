<%@ page import="java.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>METViewer</title>
<link rel="stylesheet" type="text/css" href="include/metviewer.css"/>
<link rel="shortcut icon" href="include/ral_icon.ico" type="image/x-icon"/>
</head>

<body onLoad="onLoad()" style="padding-left: 20px">

<script type="text/javascript" src="include/Hashtable.js"></script>
<script type="text/javascript" src="include/metviewer.js"></script>

<%

//  if there is a database input parameter, put its value into the db load control
String strDB = "";
String strDBVal = request.getParameter("db");
if( null != strDBVal ){ strDB = "metvdb_" + strDBVal; }

%>

<div id="divDimScreen" class="dimScreen">&nbsp;</div>
<div id="divLoading" class="loading" align="center" onclick="javascript:dimScreen(false)">Loading...<br/>
<img src="include/loading_white.gif" style="padding-top:10px"/></div>

<table width="100%" height="100%" cellspacing="0" cellpadding="5" border="0">
	<tr><td class="title"><span onclick="javascript:debugClick('title')">METViewer</span></td></tr>
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
		<span style="padding-left: 100px" class="bold">Plot Data:</span>
		<select id="selPlotData" onchange="javascript:updatePlotData()"><option>Stat</option><option>MODE</option></select>
		<span style="padding-left: 100px" class="bold">Template:</span>
		<select id="selTemplate"><option>series_plot</option><option>box_plot</option><option>bar_plot</option></select><br/><br/><br/>
	</td></tr>

	<tr><td height="100%" style="vertical-align: top"><span id="spanPlotControls" style="display:inline">
		<table><tr><td class="grey">

		<span class="header" style="font-size:18px">Y1 Axis</span><br/><br/>

		<div id="divDep1" style="padding-left:30px">
			<span class="header" style="font-size:14px">Dependent Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#dep" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divDep1')" id="lnkSerDep1">Serialize</a><br/><br/>

			<div><table><tr>
				<td class="bold" style="padding-left: 20px; padding-right: 10px; height: 70px">Forecast Variable:</td>
				<td>
					<select id="selFcstVar0" onchange="javascript:clearDepStat(0)"></select>&nbsp;
					<input type="button" class="gButton" onclick="javascript:selectFcstVarReq(0)" value="Stats &#187;"/><br/>
					<input type="hidden" value="0"/>					
				</td>
				<td style="padding-left:10px">
					<select id="selStat0" multiple="multiple" style="vertical-align:middle; min-width:100px; display:none" size="5" 
						onchange="javascript:updateDepStat(0)"></select>&nbsp;&nbsp;
				</td>
				<td style="display:none; padding-right:20px">
					<span><input type="checkbox" onchange="javascript:modeStatDiffChk(0)"/> Difference<br/></span>				
					<input type="checkbox" checked="checked"/> Fcst<br/>				
					<input type="checkbox" checked="checked"/> Obs
				</td>
				<td style="display:none; padding-right:20px">
					<input type="checkbox" checked="checked"/> Simple<br/>				
					<input type="checkbox" checked="checked"/> Cluster
				</td>
				<td style="display:none; padding-right:20px">
					<input type="checkbox" checked="checked"/> Matched<br/>				
					<input type="checkbox" checked="checked"/> Unmatched
				</td>
				<td>
					<span style="display:none">
						<img src="include/sub_symbol.gif" style="padding-left: 20px"/>&nbsp;&nbsp;
						<a class="link" onclick="javascript:removeDep1Var(0)">Remove Dependent Variable</a>
					</span>
				</td>
			</tr></table></div>

			<img id="imgDep1" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addDep1()">Add a Dependent y1 Variable</a><br/><br/><br/>
		</div><br/><br/>				
		
		<div id="divSeries1" style="padding-left:30px">
			<span class="header" style="font-size:14px">Series Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#series" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divSeries1')" id="lnkSerSeries1">Serialize</a><br/><br/>

			<img id="imgSeries1" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addSeries1Div()">Add a Series y1 Value</a><br/><br/><br/>
		</div><br/><br/>

		</td></tr><tr><td>
		
		<span class="header" style="font-size:18px">Y2 Axis</span><br/><br/>

		<div id="divDep2" style="padding-left:30px">
			<span class="header" style="font-size:14px">Dependent Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#dep" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divDep2')" id="lnkSerDep2">Serialize</a><br/><br/>

			<img id="imgDep2" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addDep2()">Add a Dependent y2 Variable</a><br/><br/><br/>
		</div><br/><br/>
		
		<div id="divSeries2" style="padding-left:30px">
			<span class="header" style="font-size:14px">Series Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#series" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divSeries2')" id="lnkSerSeries2">Serialize</a><br/><br/>

			<img id="imgSeries2" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addSeries2Div()">Add a Series y2 Value</a><br/><br/><br/>
		</div><br/><br/>

		</td></tr><tr><td class="grey">

		<div id="divFieldVal" style="display:none">
			<table><tr>
			<td>
				<span class="bold" style="padding-left: 20px; padding-right:10px">Database Field:</span>
				<select id="selField"></select>&nbsp;
				<input type="button" class="gButton" onclick="javascript:selectFieldReq()" value="Values &#187;"/>&nbsp;&nbsp;
				<select id="selVal" multiple="multiple" style="vertical-align:middle; min-width:100px; display:none" size="5"></select>&nbsp;&nbsp;
			</td>
			<td>
				<img src="include/arrow_up.gif"/><br/><br/>
				<img src="include/arrow_down.gif"/>			
			</td>
			<td style="padding-left:30px">			
				<span id="lnkRemove">
					<img src="include/sub_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
					<a class="link" onclick="javascript:removeFieldVal()">Remove Field</a>
				</span>
				<input type="hidden" value=""/>
			</td>
			</tr></table><br/><br/>
		</div>
		
		<div id="divFix">
			<span class="header">Fixed Values</span>
			<span class="stdTiny">(<a href="doc/plot.html#plot_fix" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divFix')" id="lnkSerFix">Serialize</a><br/><br/>

			<img id="imgFix" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addFixVar()">Add a Fixed Value</a><br/><br/><br/>
		</div><br/><br/>


		</td></tr><tr><td>
				
		<div id="divIndy">
			<span class="header">Independent Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#indep" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divIndy')" id="lnkSerIndy">Serialize</a><br/><br/>

			<span class="bold" style="padding-left:20px; padding-right: 10px">Database Field:</span>
			<select id="selIndyVar" onchange="javascript:clearIndyVal()"></select>&nbsp;
			<input type="button" class="gButton" onclick="javascript:selectIndyVarReq()" value="Values &#187;"/><br/><br/>

			<table id="tabIndyVal" style="padding-left:60px"><tr id="trIndyVal" style="display:none">
				<td style="width: 100px">
					<span id="spanIndyValChk">
						<input type="checkbox" value="test value" style="vertical-align:middle" checked="checked"/>
						<span id="spanIndyVal" style="font-weight: bold">(indy val)</span>&nbsp;&nbsp;
					</span>
				</td>
				<td id="tdIndyValLab">
					<span id="spanIndyValLab">
						label: <input type="text" size="8"/>&nbsp;&nbsp;
						plot val: <input type="text" size="8"/><br/>
					</span>
				</td>
			</tr></table><br/>
			<span id="spanIndyCheck" style="display:none">
				<img src="include/add_symbol.gif" style="padding-left:80px"/>&nbsp;&nbsp;
				<a class="link" onclick="javascript:indyCheck(true)">Check all</a>
				<img src="include/sub_symbol.gif" style="padding-left:40px"/>&nbsp;&nbsp;
				<a class="link" onclick="javascript:indyCheck(false)">Uncheck all</a>
			</span>
		</div><br/><br/>

		</td></tr><tr><td class="grey">

		<div id="divBoot">
			<span class="header">Bootstrapping</span>
			<span class="stdTiny">(<a href="doc/plot.html#boot" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divBoot')" id="lnkSerBoot">Serialize</a><br/><br/>

			<input id="chkBoot" type="checkbox" onclick="javascript:updateBoot()"/><span class="bold">Bootstrapping Enabled</span><br/><br/>
			<table id="tabBootParm" cellpadding="0" cellspacing="0" style="display:none">
				<tr>
					<td align="right" style="padding-left:30px">boot_repl:</td><td>&nbsp;<input type="text" size="12" value="1000"/></td>
					<td align="right" style="padding-left:30px">
						boot_diff1:</td><td>&nbsp;<input id="txtBootDiff1" type="text" size="12" value="FALSE" disabled="disabled"/>
					</td>
				</tr>
				<tr>
					<td align="right" style="padding-left:30px">boot_ci:</td><td>&nbsp;<input type="text" size="12" value="bca"/></td>
					<td align="right" style="padding-left:30px">
						boot_diff2:</td><td>&nbsp;<input id="txtBootDiff2" type="text" size="12" value="FALSE" disabled="disabled"/>
					</td>
				</tr>
			</table>
		</div><br/><br/>
		
		</td></tr><tr><td>

		<div id="divTitleLab">
			<span class="header">Plot Titles &amp; Labels</span><br/><br/>

			<table cellpadding="0" cellspacing="0">
				<tr><td align="right" style="padding-left:30px">title:</td><td>&nbsp;<input type="text" size="80" value="test title"/></td></tr>
				<tr><td align="right" style="padding-left:30px">x_label:</td><td>&nbsp;<input type="text" size="80" value="test x_label"/></td></tr>
				<tr><td align="right" style="padding-left:30px">y1_label:</td><td>&nbsp;<input type="text" size="80" value="test y_label"/></td></tr>
				<tr><td align="right" style="padding-left:30px">y2_label:</td><td>&nbsp;<input type="text" size="80" value=""/></td></tr>
				<tr><td align="right" style="padding-left:30px">caption:</td><td>&nbsp;<input type="text" size="80" value=""/></td></tr>
			</table><br/><br/>
		</div>
		
		</td></tr><tr><td class="grey">

		<div id="divFmtPlot">
			<span class="header">Plot Formatting</span>
			<span class="stdTiny">(<a href="doc/plot.html#fmt_bool" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divFmtPlot')" id="lnkSerFmtPlot">Serialize</a><br/><br/>

			<table id="tabFmtPlotBool" cellspacing="0" cellpadding="0" border="0"><tr id="trFmtPlotBool">
				<td align="right">
					<span id="spanFmtPlotBool">
						<table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:10px">
							<tr>
								<td class="fmtLabel" align="right">Bool Field</td>
								<td rowspan="2"><select><option>true</option><option>false</option></select></td>
							</tr>
							<tr><td class="fmtTag" align="right">bool_field</td></tr>
						</table>
					</span>
				</td>
			</tr></table><br/><br/>

			<span id="spanFmtPlotTxtDisp" onclick="javascript:handleFmtPlotTxtDisp()" style="padding-left:40px">
				<img src="include/arrow_right.gif" style="vertical-align:-10%"/>&nbsp; <span>Show Plot Formatting</span>			
			</span><br/><br/>
			<table id="tabFmtPlotTxt" cellspacing="0" cellpadding="0" border="0" style="display:none"><tr id="trFmtPlotTxt">
				<td align="right">
					<span id="spanFmtPlotTxt">
						<table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:10px">
							<tr>
								<td class="fmtLabel" align="right">Format Field</td>
								<td rowspan="2"><input type="text" size="12"/><select style="min-width:100px"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">fmt_field</td></tr>
						</table>					
					</span>
				</td>
			</tr></table><br/>
			
			<span id="spanFmtPlotCmd" style="display:none">
				<table border="0" cellpadding="0" cellspacing="0" style="padding-left:28px; padding-bottom:10px">
					<tr>
						<td class="fmtLabel" align="right">Plot Script Commands</td>
						<td rowspan="2"><input id="txtPlotCmd" type="text" size="80"/></td>
					</tr>
					<tr><td class="fmtTag" align="right">plot_cmd</td></tr>
				</table>			
			</span>
		</div><br/><br/>

		</td></tr><tr><td>
		
		<div id="divFmtSeries">
			<span class="header">Series Formatting</span>
			<span class="stdTiny">(<a href="doc/plot.html#fmt_series" target="_blank">help</a>)</span>			
			<a class="stdTiny" style="padding-left:20px; padding-right:20px" onclick="javascript:serialize('divFmtSeries')" id="lnkSerFmtSeries">Serialize</a>
			<input id="btnFmtSeriesDefaults" type="button" class="gButton" onclick="javascript:setFmtSeriesDefaults()" value="Defaults" style="display:none"/>
			<!-- <span style="padding-left:20px"><input type="button" class="gButton" onclick="javascript:buildSeriesDiv()" value="Build"/></span>  -->
			<span id="spanFmtSeriesNum" class="bold" style="padding-left: 30px"># Series: 0</span>
			<br/><br/>
			
			<span id="spanFmtSeriesDisp" onclick="javascript:handleFmtSeriesDisp()" style="padding-left:40px">
				<img src="include/arrow_right.gif" style="vertical-align:-10%"/>&nbsp; <span>Show Series Formatting</span>			
			</span><br/><br/>
			
			<table id="tabFmtSeries" cellspacing="0" cellpadding="0" border="0" style="display:none; padding-bottom:20px">
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="tiny">&nbsp;</span><br/>
						<span class="bold" style="font-size:10pt; padding-right:20px">Series Name</span>
						<input type="hidden"/><br/>
						<span class="tiny" style="padding-right:20px">(series1 or 2)</span>
					</span>
				</td>
				<td align="right">
					<table id="tabFmtSeriesVal1" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Conf Interval Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>none</option>
										<option>norm</option>
										<option>boot</option>
										<option>std</option>
									</select>
								</td>
							</tr>
							<tr><td class="fmtTag" align="right">plot_ci</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Color</td>
								<td rowspan="2"><input type="text" size="12"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">color</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Point Symbol</td>
								<td rowspan="2"><input type="text" size="12"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">pch</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>p</option>
										<option>l</option>
										<option>o</option>
										<option>b</option>
										<option>s</option>
										<option>h</option>
										<option>n</option>
									</select>
								</td>
							</tr>
							<tr><td class="fmtTag" align="right">type</td></tr>
						</table>								
					</td></tr>
					</table>
				</td>
				<td align="right">
					<table id="tabFmtSeriesVal2" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>1</option>
										<option>2</option>
										<option>3</option>
										<option>4</option>
										<option>5</option>
										<option>6</option>
									</select>
								</td>
							</tr>
							<tr><td class="fmtTag" align="right">lty</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Width</td>
								<td rowspan="2"><input type="text" size="12"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">lwd</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Connect Across NA Values</td>
								<td rowspan="2"><input type="text" size="12"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">con_series</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Legend Text</td>
								<td rowspan="2"><input type="text" size="12"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">legend</td></tr>
						</table>
					</td></tr>
					</table>
				</td>
			</tr>
			<tr style="display:none"><td colspan="3">
				<span id="spanFmtSeriesHR" style="padding-left:30px; padding-top:20px"><hr align="right" style="width:90%"/></span>
			</td></tr>
			</table>
		</div>
		
		</td></tr><tr><td class="grey">
		
		<div id="divFmtAxis">
			<span class="header">Axis Bounds</span>
			<span class="stdTiny">(<a href="doc/plot.html#fmt_axis" target="_blank">help</a>)</span><br/><br/>			

			<table cellpadding="0" cellspacing="0">
				<tr>
					<td align="right" style="padding-left:30px">y1_lim:</td><td>&nbsp;<input type="text" size="12" value=""/></td>
					<td align="right" style="padding-left:30px">y1_bufr:</td><td>&nbsp;<input type="text" size="12" value=".04"/></td>
					<td align="right" style="padding-left:30px">y2_lim:</td><td>&nbsp;<input type="text" size="12" value="c(0,1)"/></td>
					<td align="right" style="padding-left:30px">y2_bufr:</td><td>&nbsp;<input type="text" size="12" value="0"/></td>
				</tr>
			</table><br/><br/>
		</div>
				
		</td></tr></table><br/><br/>
		
		<input type="button" class="header" onclick="javascript:runPlotReq()" value="&#187; Generate Plot &#171;"/><br/><br/>
		
	</span>&nbsp;</td></tr>

	<tr id="trDebug" style="display:none">
		<td>
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
		<input type="button" class="gButton" onclick="javascript:dimScreen(true)" value="Dim"/>&nbsp;&nbsp;
		<input type="button" class="gButton" onclick="javascript:listValClearCacheReq()" value="Clear Val List Cache"/>
		<input type="button" class="gButton" onclick="javascript:listStatClearCacheReq()" value="Clear Stat List Cache"/>
		<input type="button" class="gButton" onclick="javascript:testPlotResp()" value="plot.html"/><br/><br/>
		<textarea rows="20" cols="160" id="txtConsole"></textarea></td>
	</tr>

</table>

</body>
</html>
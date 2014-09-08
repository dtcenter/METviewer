<%@ page import="java.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>METViewer</title>
<link rel="stylesheet" type="text/css" href="css/metviewer.css"/>
<link rel="shortcut icon" href="include/ral_icon.ico" type="image/x-icon"/>
 <link rel="stylesheet" href="css/smoothness/jquery-ui-1.10.3.custom.min.css" />
   <script src="js/jquery-1.9.1.js" type="text/javascript"></script>
   <script src="js/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
    <script type="text/javascript" src="include/Hashtable.js"></script>
    <script type="text/javascript" src="include/util.js"></script>
    <script type="text/javascript" src="include/metviewer.js"></script>
</head>

<body onLoad="onLoad()" style="padding-left: 20px">



<%

//  if there is a database input parameter, put its value into the db load control
String strDB = "";
String strDBVal = request.getParameter("db");
if( null != strDBVal ){ strDB = strDBVal; }

//  read the init XML from the session
Object objInitXML = session.getAttribute("init_xml");
String strInitXML = (null == objInitXML? "" : objInitXML.toString());

%>

<div id="divDimScreen" class="dimScreen">&nbsp;</div>
<div id="divLoading" class="loading" align="center" onclick="javascript:dimScreen(false)">Loading...<br/>
<img src="include/loading_white.gif" style="padding-top:10px"/></div>

<table width="100%" height="100%" cellspacing="0" cellpadding="5" border="0">
	<tr><td>
		<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0"><tr>
			<td class="title"><span onclick="javascript:debugClick('title')">METViewer</span>&nbsp;<span class="stdTiny">v0.5.15</span></td>
			<td align="right">&nbsp;
				<form action="servlet" enctype="multipart/form-data" method="post" id="formUpload">
					<span class="header" style="font-size:14px">Plot XML Upload:</span>
					<input type="file" accept="text/xml" name="fileUpload"/>
					<input type="submit" value="Upload"/>
				</form>
			</td>
		</tr>
            <tr><td colspan="2" style="text-align: right;padding-right: 151px;"><span class="header" style="font-size:14px;">Reload list of databases: </span><button value="reloadDB" onclick="javascript:listDBUpdateReq()">Reload</button> </td></tr>
        </table>
		
		
	</td></tr>
	<tr><td><hr/></td></tr>

	<tr><td>
		<span class="header" onclick="javascript:debugClick('db')">Database:</span>&nbsp;&nbsp;
		<select id="selDB" onChange="javascript:updateDBCon()"></select><span class="bold" id="spanDBLoad"><%= strDB %></span>
		<span style="padding-left: 100px" class="bold">Plot Data:</span>
		<select id="selPlotData" onchange="javascript:updatePlotData()"><option value="stat" selected="selected">Stat</option><option value="mode">MODE</option></select>
		<span style="padding-left: 100px" class="bold">Template:</span>
		<select id="selTemplate" onchange="javascript:updateTmpl()">
			<option selected>series_plot</option>
			<option>box_plot</option>
			<option>bar_plot</option>
			<option>rhist</option>
            <option>phist</option>
			<option>roc</option>
         <option>rely</option>
         <option>ens_ss</option>
		</select><br/><br/><br/>
	</td></tr>

	<tr><td height="100%" style="vertical-align: top"><span id="spanPlotControls" style="display:inline">
		<table>
		
		<tr><td class="gray" id="tdY1">

		<span class="header" style="font-size:18px">Y1 Axis</span><br/><br/>

		<div id="divDep1" style="padding-left:30px">
			<span class="header" style="font-size:14px">Dependent Variables</span>
			<span class="stdTiny">(<a href="doc/plot.html#dep" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divDep1')" id="lnkSerDep1">Serialize</a><br/><br/>

			<div><table><tr>
				<td class="bold" style="padding-left: 20px; padding-right: 10px; height: 70px">Forecast Variable:</td>
				<td>
                    <table>
                        <tr><td rowspan="2"><select id="selFcstVar0" onchange="javascript:clearDepStat(0)"></select></td>
                            <td><input type="button" style="display: none;width:110px;" id="ratio_stats" class="gButton" onclick="javascript:selectFcstVarReq(0, this)" value="Ratio Stats &#187;" /><input type="hidden" value="0"/></td>
                        </tr>
                        <tr><td><input type="button"  id="other_stats" class="gButton" onclick="javascript:selectFcstVarReq(0, this)" value="Attribute Stats &#187;" style="width:110px;"/></td></tr>
                    </table>


					<!--select id="selFcstVar0" onchange="javascript:clearDepStat(0)"></select>&nbsp;
					<input type="button" class="gButton" onclick="javascript:selectFcstVarReq(0)" value="Stats &#187;"/><br/>
					<input type="hidden" value="0"/-->
				</td>
				<td style="padding-left:10px">
					<select id="selStat0" multiple="multiple" style="vertical-align:middle; min-width:100px; display:none" size="5" 
						onchange="javascript:updateDepStat(0)"></select>&nbsp;&nbsp;
				</td>
				<td style="display:none; padding-right:20px" >
					<span><input type="checkbox" onchange="javascript:modeStatDiffChk(0)" id="difference"/> Difference<br/></span>
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Fcst<br/>
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Obs
				</td>
				<td style="display:none; padding-right:20px" >
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Simple<br/>
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Cluster
				</td>
				<td style="display:none; padding-right:20px" >
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Matched<br/>
					<input type="checkbox" onchange="javascript:updateDepStat(0)" checked="checked" /> Unmatched
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

		</td></tr>
		<tr><td id="tdY2">
		
		<span class="header" style="font-size:18px">Y2 Axis</span><br/><br/>

		<span id="spanY2NA" class="header" style="padding-left:40px; display:none">N/A</span>
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
		
		</td></tr>
		<tr><td class="gray" id="tdFix">

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
                <td>
                    <input type="checkbox"  style="vertical-align:middle;" onclick="javascript:buildSeriesDiv()"/><label  style="vertical-align:middle;">Group</label>
                </td>

			<td style="padding-left:30px">			
				<span id="lnkRemove">
					<img src="include/sub_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
					<a class="link" onclick="javascript:removeFieldVal()">Remove Field</a>
				</span>
				<input type="hidden" value=""/>
			</td>
			</tr>
            </table>

            <br/><br/>
		</div>
		
		<div id="divFix">
			<span class="header">Fixed Values</span>
			<span class="stdTiny">(<a href="doc/plot.html#plot_fix" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divFix')" id="lnkSerFix">Serialize</a><br/><br/>

			<img id="imgFix" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addFixVar()">Add a Fixed Value</a><br/><br/><br/>
			
			<span style="padding-left:30px">plot_cond:&nbsp;<input type="text" id="txtPlotCond" size="80" value=""/></span><br/>
		</div>

		<div id="divFixSpc" style="display:none">
			<span class="header" id="spanFixSpcHdr">Specialized Plot Fixed Values</span>
			<span class="stdTiny">(<a href="doc/plot.html#plot_fix" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left: 20px" onclick="javascript:serialize('divFixSpc')" id="lnkSerFixSpc">Serialize</a><br/><br/>

			<img id="imgFixSpc" src="include/add_symbol.gif" style="padding-left:20px"/>&nbsp;&nbsp;
			<a class="link" onclick="javascript:addFixSpcVar()" id="lnkFixSpcHdr">Add a Specialized Plot Fixed Value</a><br/><br/><br/>
			         
         <span style="padding-left:30px">plot_cond:&nbsp;<input type="text" id="txtPlotCondSpc" size="80" value=""/></span><br/>			
		</div>
		
		<br/><br/>
		</td></tr>
		<tr><td id="tdIndy">
				
		<div id="divIndy">
			<span class="header">Independent Variable</span>
			<span class="stdTiny">(<a href="doc/plot.html#indep" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divIndy')" id="lnkSerIndy">Serialize</a><br/><br/>

         <span id="spanIndyNA" class="header" style="padding-left:40px; display:none">N/A</span>
         <span id="spanIndy">
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
			</span>
		</div><br/><br/>

		</td></tr>
		<tr><td class="gray" id="tdAggStat">

		<div id="divAggStat">
			<span class="header">Aggregation Statistics</span>
			<span class="stdTiny">(<a href="doc/plot.html#agg_stat" target="_blank">help</a>)</span>
			<a class="stdTiny" style="padding-left:20px" onclick="javascript:serialize('divAggStat')" id="lnkSerAggStat">Serialize</a><br/><br/>

			<span id="spanAggStatNA" class="header" style="padding-left:40px; display:none">N/A</span>
			<input id="chkAggStat" type="checkbox" onclick="javascript:updateAggStat()"/><span id="spanAggStat" class="bold">Aggregation Statistics Enabled</span><br/><br/>
			<table id="tabAggStatParm" border="0" cellpadding="0" cellspacing="0" style="display:none">
				<tr>
					<td align="left" style="padding-left:30px" colspan="6" id="aggStats">
						<input type="radio" name="agg_stat" value="ctc"/><span class="bold">CTC</span>&nbsp;&nbsp;&nbsp;
						<input type="radio" name="agg_stat" value="sl1l2"/><span class="bold">SL1L2</span>
						<input type="radio" name="agg_stat" value="pct"/><span class="bold">PCT</span>
						<input type="radio" name="agg_stat" value="nbrcnt"/><span class="bold">NBR_CNT</span>
						<br/><br/>
					</td>
				</tr>
				<tr>
					<td align="right" style="padding-left:30px">boot_repl:</td><td>&nbsp;<input type="text" size="12" value="1"/></td>

               <td align="right" style="padding-left:30px">eveq_dis:</td>
               <td>&nbsp;
                  <select id="selEveqDis" value="FALSE"><option>FALSE</option><option>TRUE</option></select>
               </td>
				</tr>
                <tr>
                    <td align="right" style="padding-left:30px">boot_ci:</td>
                    <td>&nbsp;<input type="text" size="12" value="perc"/></td>


                    </td>

                    <td align="right" style="padding-left:30px">
                        cache_agg_stat:
                    </td>
                    <td>&nbsp;
                        <select id="cacheAggStat" value="FALSE">
                            <option>FALSE</option>
                            <option>TRUE</option>
                        </select>
                    </td>
                </tr>
			</table>
		</div><br/><br/>

		</td></tr><tr><td id="tdCalcStat">

		<div id="divCalcStat">
			<span class="header">Statistics Calculations</span>
            <span class="stdTiny">(<a href="doc/plot.html#calc_stat"
                                      target="_blank">help</a>)</span><br/><br/>
            <span class="bold" style="padding-left:20px; padding-right: 10px">Plot statistic:</span>
            <select name="plot_stat" id="plot_stat">
                <option value="median">Median</option>
                <option value="mean">Mean</option>
            </select>

            <a class="stdTiny" style="padding-left:20px"
               onclick="javascript:serialize('divCalcStat')"
               id="lnkSerCalcStat">Serialize</a><br/><br/>

            <span id="spanCalcStatNA" class="header"
                  style="padding-left:40px; display:none">N/A</span>
            <input id="chkCalcStat" type="checkbox" onclick="javascript:updateCalcStat()"/><label for="chkCalcStat" id="spanCalcStat" class="bold">Statistics Calculation Enabled</label><br/><br/>
			<table id="tabCalcStatParm" border="0" cellpadding="0" cellspacing="0" style="display:none">
				<tr>
					<td align="left" style="padding-left:30px" colspan="4">
						<input type="radio" name="calc_stat" value="ctc"/><span class="bold">CTC</span>&nbsp;&nbsp;&nbsp;
						<input type="radio" name="calc_stat" value="sl1l2"/><span class="bold">SL1L2</span>
						<br/><br/>
					</td>
				</tr>
			</table>
		</div><br/><br/>
		
		</td></tr>
        <tr>
            <td id="tdRocCalc" style="display:none">

                <div id="divRocCalc">
                    <span class="header">ROC Calculations</span>
                    <span class="stdTiny">(<a href="doc/plot.html#roc_calc"
                                              target="_blank">help</a>)</span>
                    <a class="stdTiny" style="padding-left:20px"
                       onclick="javascript:serialize('divRocCalc')"
                       id="lnkSerRocCalc">Serialize</a><br/><br/>

                    <table id="tabRocCalcParm" border="0" cellpadding="0"
                           cellspacing="0">
                        <tr>
                            <td align="left" style="padding-left:30px"
                                colspan="4">
                                <input type="radio" name="roc_pct"
                                       value="pct"/><span
                                    class="bold">PCT</span>&nbsp;&nbsp;&nbsp;
                                <input type="radio" name="roc_ctc"
                                       value="ctc"/><span
                                    class="bold">CTC</span>
                                <br/><br/>
                            </td>
                        </tr>
                    </table>
                </div>
                <br/><br/>

            </td>
        </tr>

        <tr>
                    <td id="tdHistType" style="display:none">

                        <div id="divHistType">
                            <span class="header">Histogram type</span>
                            <span class="stdTiny">(<a href="doc/plot.html#hist_type"
                                                      target="_blank">help</a>)</span>


                            <table id="tabHistTypeParm" border="0" cellpadding="0"
                                   cellspacing="0">
                                <tr>
                                    <td align="left" style="padding-left:30px"
                                        colspan="4">
                                        <input type="radio" name="normalized_histogram"
                                               value="true" checked/><span
                                            class="bold">Normalized</span>&nbsp;&nbsp;&nbsp;
                                        <input type="radio" name="normalized_histogram"
                                               value="false"/><span
                                            class="bold">Raw counts</span>
                                        <br/><br/>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <br/><br/>

                    </td>
                </tr>


        <tr><td class="gray">
		
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
		
		</td></tr><tr><td>
		
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
				<img src="include/arrow_right.gif" style="vertical-align:-10%"/>&nbsp; <span>Show Formatting Controls</span>			
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

		</td></tr><tr><td class="gray">
		
		<div id="divFmtSeries">
			<span class="header">Series Formatting</span>
			<span class="stdTiny">(<a href="doc/plot.html#fmt_series" target="_blank">help</a>)</span>			
			<a class="stdTiny" style="padding-left:20px; padding-right:20px" onclick="javascript:serialize('divFmtSeries')" id="lnkSerFmtSeries">Serialize</a>
			<input id="btnFmtSeriesDefaults" type="button" class="gButton" onclick="javascript:setFmtSeriesDefaults()" value="Defaults" style="display:none"/>
			<!-- <span style="padding-left:20px"><input type="button" class="gButton" onclick="javascript:buildSeriesDiv()" value="Build"/></span>  -->
			<span id="spanFmtSeriesNum" class="bold" style="padding-left: 30px"># Series: 0</span>
            <input style=" margin-left: 50px;" id="btnFmtAddDifferenceCurve" type="button" class="gButton" onclick="javascript:openAddDiffCurveDialog()" value="Add Difference Curve" style="display:none"/>
			<span style="padding-left: 100px"><input id="chkFmtSeriesLock" type="checkbox" style="vertical-align:-10%"/>Lock Formatting</span>

			<br/><br/>
			
			<span id="spanFmtSeriesDisp" onclick="javascript:handleFmtSeriesDisp()" style="padding-left:40px">
				<img src="include/arrow_right.gif" style="vertical-align:-10%"/>&nbsp; <span>Show Formatting Controls</span>			
			</span><br/><br/>
			
			<table id="tabFmtSeries" cellspacing="0" cellpadding="0" border="0" style="display:none; padding-bottom:20px">
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="tiny">&nbsp;</span><br/>
						<span class="bold" style="font-size:10pt; padding-right:20px" name="seriesName">Series Name</span>
						<input type="hidden"/><br/>
						<span class="tiny" style="padding-right:20px">(series1 or 2)</span><br/><br/>
						<span style="padding-right:20px">Hide<input id="chkHide" type="checkbox"/></span><br/>
                        <span style="padding-right:20px">Order # <input id="seriesOrder" type="text" size="1" value=""/></span>

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
										<option>brier</option>
										<option>std</option>
										<!--<option>q98</option>-->
										<!--<option>q90</option>-->
										<!--<option>q80</option>-->
										<!--<option>q50</option>-->
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
								<td class="fmtLabel" align="right">Series Line Type</td>
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
                        <tr><td align="right">
                        						<table border="0" cellpadding="0" cellspacing="0">
                        							<tr>
                        								<td class="fmtLabel" align="right">Show Significant</td>
                        								<td rowspan="2"><select style="min-width:100px">
                        																		<option>TRUE</option>
                        																		<option selected>FALSE</option>

                        																	</select></td>
                        							</tr>
                        							<tr><td class="fmtTag" align="right">show_signif</td></tr>
                        						</table>
                        					</td></tr>
					</table>
				</td>
			</tr>
			<tr style="display:none"><td colspan="3">
				<span id="spanFmtSeriesHR" style="padding-left:30px; padding-top:20px"><hr align="right" style="width:90%"/></span>
			</td></tr>
			</table>
			
			<table id="tabFmtSeriesRhist" cellspacing="0" cellpadding="0" border="0" style="display:none; padding-bottom:20px">
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="bold" style="font-size:10pt; padding-right:20px">Rank Histogram</span>
					</span>
				</td>
				<td align="right">
					<table id="tabFmtSeriesRhistVal" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Color</td>
								<td rowspan="2"><input type="text" size="12" value="gray"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">color</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Width</td>
								<td rowspan="2"><input type="text" size="12" value="1"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">lwd</td></tr>
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
			<tr><td><br/><br/></td></tr>
			</table>
			
			<table id="tabFmtSeriesRoc" cellspacing="0" cellpadding="0" border="0" style="display:none; padding-bottom:20px">
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="bold" style="font-size:10pt; padding-right:20px">ROC Curve</span>
					</span>
				</td>
				<td align="right">
					<table id="tabFmtSeriesRoc1" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Color</td>
								<td rowspan="2"><input type="text" size="12" value="#AAAAAAFF"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">color</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Point Symbol</td>
								<td rowspan="2"><input type="text" size="12" value="20"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">pch</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Series Line Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>b</option>
										<option>p</option>
										<option>l</option>
										<option>o</option>
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
					<table id="tabFmtSeriesRoc2" border="0" cellpadding="0" cellspacing="0" style="width:300px">
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
								<td rowspan="2"><input type="text" size="12" value="1"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">lwd</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">&nbsp;</td></tr>
					</table>
				</td>
			</tr>
			<tr><td><br/><br/></td></tr>
			</table>
			
			<table id="tabFmtSeriesRely" cellspacing="0" cellpadding="0" border="0" style="display:none; padding-bottom:20px">
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="bold" style="font-size:10pt; padding-right:20px">Reliability Curve</span>
					</span>
				</td>
				<td align="right">
					<table id="tabFmtSeriesRely1" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Color</td>
								<td rowspan="2"><input type="text" size="12" value="#333333FF"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">color</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Point Symbol</td>
								<td rowspan="2"><input type="text" size="12" value="20"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">pch</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Series Line Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>b</option>
										<option>p</option>
										<option>l</option>
										<option>o</option>
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
					<table id="tabFmtSeriesRely2" border="0" cellpadding="0" cellspacing="0" style="width:300px">
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
								<td rowspan="2"><input type="text" size="12" value="1"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">lwd</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">&nbsp;</td></tr>
					</table>
				</td>
			</tr>
			<tr><td colspan="3"><br/><hr/><br/></td></tr>
			<tr>
				<td align="right" style="width:350px">
					<span id="spanFmtSeriesName">
						<span class="bold" style="font-size:10pt; padding-right:20px">Event Histogram</span>
					</span>
				</td>
				<td align="right">
					<table id="tabFmtSeriesRely3" border="0" cellpadding="0" cellspacing="0" style="width:300px">
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Line Color</td>
								<td rowspan="2"><input type="text" size="12" value="#AAAAAAFF"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">color</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Point Symbol</td>
								<td rowspan="2"><input type="text" size="12" value="20"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">pch</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td class="fmtLabel" align="right">Series Line Type</td>
								<td rowspan="2">
									<select style="min-width:100px">
										<option>b</option>
										<option>p</option>
										<option>l</option>
										<option>o</option>
										<option>s</option>
										<option selected="selected">h</option>
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
					<table id="tabFmtSeriesRely4" border="0" cellpadding="0" cellspacing="0" style="width:300px">
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
								<td rowspan="2"><input type="text" size="12" value="50"/></td>
							</tr>
							<tr><td class="fmtTag" align="right">lwd</td></tr>
						</table>
					</td></tr>
					<tr><td align="right">&nbsp;</td></tr>
					</table><br/><br/>
				</td>
			</tr>
			</table>

		</div>
		
		</td></tr><tr><td>
		
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
				
		</td></tr>


        </table><br/><br/><hr/><br/>
		
		<input type="button" class="header" onclick="javascript:runPlotReq()" value="&#187; Generate Plot &#171;"/><br/><br/>
				
	</span>&nbsp;</td></tr>

	<tr id="trDebug" style="display:none">
		<td>
		<hr/>
		<br/><br/>
		<span class="header" style="font-size:18px">Debug Panel</span><br/><br/><br/>
		
		<b>Servlet Controls:</b>
		<input type="button" class="gButton" onclick="javascript:listValClearCacheReq()" value="Clear Val List Cache"/>
		<input type="button" class="gButton" onclick="javascript:listValCacheKeysReq()" value="Val List Cache Keys"/>
		<input type="button" class="gButton" onclick="javascript:listStatClearCacheReq()" value="Clear Stat List Cache"/>
		<input type="button" class="gButton" onclick="javascript:listStatCacheKeysReq()" value="Stat List Cache Keys"/>
		<br/><br/>
		
		<b>Console:</b>
        <input type="button" class="gButton" onclick="javascript:startDebugOutput()" value="Start Debug Output"/>&nbsp;&nbsp;
        <input type="button" class="gButton" onclick="javascript:stopDebugOutput()" value="Stop Debug Output"/>&nbsp;&nbsp;
		<input type="button" class="gButton" onclick="javascript:consoleClear()" value="Clear"/>&nbsp;&nbsp;
		<input type="button" class="gButton" onclick="javascript:dimScreen(true)" value="Dim"/><br/>
		<textarea rows="20" cols="120" id="txtConsole"></textarea><br/><br/>

		<b>Init XML:</b><br/>
		<textarea rows="20" cols="120" id="txtInitXML"><%= strInitXML %></textarea><br/><br/>
		
		<b>Regex Test</b>
		<table>
			<tr><td>Data:</td>		<td><input type="text" id="txtData" size="50"/></td></tr>
			<tr><td>Pattern:</td>	<td><input type="text" id="txtPattern" size="50"/></td></tr>
			<tr><td colspan="2"><input type="button" class="gButton"onclick="javascript:parse()" value="Parse"/></td></tr>
		</table><br/><br/>
		
		<!-- 
		<b>HTTP Get/Post</b><br/>
		<input type="text" id="txtPost"/>
		<input type="button" class="gButton" onclick="javascript:testPostReq()" value="Post"/>&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="text" id="txtGet"/>
		<input type="button" class="gButton" onclick="javascript:testGetReq()" value="Get"/><br/><br/>
		-->
							
		</td>
	</tr>

</table>
<div id="addDiffCurveDialogForm" title="Add Series Difference Curve">
    <form>
        <div style="text-align:center; padding-right: 10px; padding-left: 10px;">
        <table align="center" >
            <tr>
                <td><input type="radio" id="y1AxisDiff" name="yAxisDiff"
                           value="1" onchange="changeYAxis(1)"
                           checked><label for="y1AxisDiff" class="header" style="font-size:14px">Y1 axis</label></td>
                <td><input type="radio" id="y2AxisDiff" name="yAxisDiff"
                           value="2" onchange="changeYAxis(2)"
                        ><label for="y2AxisDiff" class="header" style="font-size:14px">Y2 axis</label></td>
            </tr>
            <tr>
                <td>
                    <fieldset>
                        <div class="diffSelect">

                        <select name="series1Y1" id="series1Y1"
                                     onchange="populateSecondSelect(1); createNewDiffSeriesName(1);"></select>
                        </div>
                        <div class="diffSelect header" style="font-size:12px;text-align:center;">minus </div>
                        <div class="diffSelect">
                        <select name="series2Y1" id="series2Y1"
                                     onchange="createNewDiffSeriesName(1)"></select>
                        </div>

                    </fieldset>
                </td>
                <td>
                    <fieldset>
                        <div class="diffSelect">

                        <select name="series1Y2" id="series1Y2" disabled
                                     onchange="populateSecondSelect(2); createNewDiffSeriesName(2)">
                        </select></div>
                        <div class="diffSelect header" style="font-size:12px;text-align:center;">minus </div>
                        <div class="diffSelect">

                        <select name="series2Y2" id="series2Y2" disabled
                                     onchange="createNewDiffSeriesName(2)">
                        </select></div>

                    </fieldset>
                </td>
            </tr>
        </table>
        <div id="newDiffSeriesName" class="diffSelect" style="font-weight:bold;"></div>
        </div>
    </form>
    <div style="font-size:9px;"> * Event Equalizer selection will be changed to "TRUE" if at least one DIFF series is selected.</div>
</div>
</body>
</html>

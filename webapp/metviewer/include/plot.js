
function onLoad(){
console("onLoad()\n");

	//  parse the elements to build the plot, xml and script URLs
	var strPlotBase = window.name.substring(12);
	var strURLBase = window.location.href;
	strURLBase = strURLBase.substring(0, strURLBase.lastIndexOf("/"));
	
	//  set the plot image src
	var strPlotURL = strURLBase + "/plots/" + strPlotBase + ".png";
	document.getElementById("imgPlot").src = strPlotURL;
console("  strPlotURL: " + strPlotURL + "\n");
	
	//  set the links to the xml, R script and data
	document.getElementById("lnkPlotXML").href = strURLBase + "/xml/" + strPlotBase + ".xml";
	document.getElementById("lnkRScript").href = strURLBase + "/R_work/scripts/" + strPlotBase + ".R";
	document.getElementById("lnkRData").href = strURLBase + "/R_work/data/" + strPlotBase + ".data";
	
console("onLoad() complete\n\n");
}

/**
 * Wrapper for appending information to the console text box
 */
function console(str){ document.getElementById("txtConsole").value += str; }
function consoleClear(){ document.getElementById("txtConsole").value = ""; }


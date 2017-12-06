<!DOCTYPE html>

<HTML>
<HEAD>
<META http-equiv="content-type" content="text/html; charset=utf-8">

<TITLE>METViewer v2.3</TITLE>
<link rel="shortcut icon" href="./favicon.ico">

<link rel="stylesheet"
      href="css/smoothness/jquery-ui.min.css"/>
<link rel="stylesheet" href="css/layout-default-latest.css"/>
<link rel="stylesheet" href="css/ui.jqgrid.css"/>
<link rel="stylesheet" href="css/jquery.colorpicker.css"/>
<link rel="stylesheet" href="css/jquery.multiselect.css"/>
<style type="text/css">

    .ui-jqgrid .ui-jqgrid-btable, .ui-pg-table, .ui-widget, .ui-widget-content .ui-state-default,.ui-widget table {
        font-size: 11px !important;;

    }

    .no-padding {
        padding: 0 !important;
    }

    .add-padding {
        padding: 5px !important;
    }

    .no-scrollbar {
        overflow: hidden;
    }

    .allow-overflow {
        overflow: visible;
    }

    #plot_display {
        padding: 0;
        border: 0;
    }

    div.toolbar {
        padding: 5px 10px;
        border: 1px solid #BBB;
        background: #F6F6F6;
        text-align: center;
    }

    #tabs-west iframe {
        width: 100%;
        height: 100%
    }

    #tabs-west object {
        width: 100%;
        height: 100%
    }

    .hide {
        display: none !important;
    }

    th.ui-th-column div {
        white-space: normal !important;
        height: auto !important;

    }
    .ui-layout-button-close-west {
        background: url("css/smoothness/images/ui-icons_888888_256x240.png") -240px -48px;
        left: 1px;
    }

    .ui-layout-button-close {
        cursor: pointer;
        display: block;
        height: 15px;
        position: absolute;
        top: 4px;
        width: 15px;
        z-index: 2;
        margin: 2px;
    }

    .history-toggler-west-closed {
        background: url("css/smoothness/images/ui-icons_888888_256x240.png")  -208px -48px;
    }

    #fade {
        display: block;
        position:absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: #ababab;
        z-index: 1001;
        -moz-opacity: 0.8;
        opacity: .70;
        filter: alpha(opacity=80);
    }

    #modal {
        display: block;
        position: absolute;
        top: 45%;
        left: 45%;
        width: 64px;
        height: 64px;
        padding:30px 15px 0;
        border: 3px solid #ababab;
        box-shadow:1px 1px 10px #ababab;
        border-radius:20px;
        background-color: white;
        z-index: 1002;
        text-align:center;
        overflow: auto;
    }
    .ui-widget-content-plot{
        padding: 5px 10px;
        margin-bottom: 10px;
        overflow-x: auto;
    }
    .ui-widget-header-plot{
        padding-left: 10px;
        padding-top: 5px;
        padding-bottom: 5px;
        font-weight: bold;
    }
    .indy-var-option{
        margin-bottom:3px;
    }
    #generate_plot.ui-button {
       color:#DB490F;

    }

    .ui-jqgrid .ui-jqgrid-pager .ui-pg-div{
        padding:1px 3px;
    }
    .ui-pg-table .navtable{
        width: 550px;
    }
    .ui-jqgrid td input, .ui-jqgrid td select, .ui-jqgrid td textarea {
        padding-bottom: 1px;
        padding-top: 1px;
    }
    .ui-jqgrid tr.jqgrow td {
        height: 20px;
    }

</style>

<script src="js/jquery-min.js" type="text/javascript"></script>
<script src="js/jquery-ui.min.js"
        type="text/javascript"></script>
<script src="js/jquery.layout-latest.min.js"
        type="text/javascript"></script>
<script type="text/javascript"
        src="js/jquery.layout.resizeTabLayout-latest.min.js"></script>
<script type="text/javascript" src="js/grid.locale-en.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/jquery.colorpicker.min.js"></script>
<script type="text/javascript" src="js/jquery.multiselect.min.js"></script>
<script type="text/javascript" src="js/jquery.actual.min.js"></script>
<script type="text/javascript"
        src="js/swatches/jquery.ui.colorpicker-pantone.js"></script>
<script type="text/javascript" src="js/metviewer_common.min.js"></script>
<script type="text/javascript" src="js/moment.min.js"></script>

<script type="text/javascript">

    var outerLayout, innerLayout, colorOfSelectedGroup;
    var boxID = 0;
    var window_center = -381;
    var window_top = -306;
    var currentTab = 'Series';
    var lastSelRow, lastSelCol;
    var currentPlotTab = 'plot_image_url';
    var resultName;
    var strInitXML = '<%= session.getAttribute("init_xml") %>';
    var initXML;
    var urlOutput="";

    if (strInitXML != "null" && strInitXML.length > 0) {
      try {
        xmlDoc = $.parseXML(strInitXML);
        initXML = $(xmlDoc);
      } catch (e) {
        console.log("Can't parse XML")
      }
    }
    var series1Names=[];
    var series2Names=[];


    $(document).ready(function () {
        $('input[name=derive_oper]').change(function () {
            createNewDerivedSeriesName($('input[name=yAxisDiff]').val());
        });

        $.ajax({
            async: false,
            url: "servlet",
            type: "POST",
            dataType: 'xml',
            processData: false,
            data: '<request><list_db></list_db></request>',
            error: function (jqXHR, textStatus, errorThrown) {

            },
            success: function (data) {

                var url_output_xml = $(data).find("url_output");
                if (url_output_xml) {
                    urlOutput = $(url_output_xml).text();
                }
                var values = $(data).find("val");
                var databaseEl = $("#database");
                var selected;
                var selectedDatabase=[];
                if (initXML != null) {
                    var sd = initXML.find("database").text();
                    selectedDatabase = sd.split(",");

                }
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    if (selectedDatabase.length > 0) {
                        selected = (selectedDatabase.indexOf(t.text()) > -1);
                    } else {
                        selected = i == 0;
                    }

                    var opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        selected: selected
                    });
                    opt.appendTo(databaseEl);
                }
                $("#database").multiselect({
                    multiple: true,
                    header: true,
                    minWidth: 'auto',
                    height: 300,
                    selectedList: 1, // 0-based index
                    click: function (event, ui) {
                        seriesDiffY1 = [];
                        seriesDiffY2 = [];
                        var values, i;
                        if (currentTab == 'Series' || currentTab == 'Box' || currentTab == 'Bar') {
                            $('#listdt').jqGrid('clearGridData');
                            updateForecastVariables();
                            updateStats("y1", 1, []);
                            updateStats("y2", 1, []);
                            updateSeriesVarVal("y1", 1, []);
                            updateSeriesVarVal("y2", 1, []);
                        } else if (currentTab == 'Roc' || currentTab == 'Rely' || currentTab == 'Hist' || currentTab == 'Eclv') {
                            updateSeriesVarValHist(1, []);
                            for (i = 0; i < fixed_var_indexes.length; i++) {
                                values = $("#fixed_var_val_" + fixed_var_indexes[i]).val();
                                updateFixedVarValHist(fixed_var_indexes[i], values);
                            }
                            if (currentTab == 'Rely') {
                                updateSeriesRely();
                            } else {
                                updateSeriesHist();
                            }
                        } else if (currentTab == 'Ens_ss') {

                            for (i = 0; i < series_var_y1_indexes.length; i++) {
                                values = $("#series_var_val_y1_" + series_var_y1_indexes[i]).val();
                                updateSeriesVarValEns(series_var_y1_indexes[i], values);
                            }
                            for (i = 0; i < fixed_var_indexes.length; i++) {
                                values = $("#fixed_var_val_" + fixed_var_indexes[i]).val();
                                updateFixedVarValHist(fixed_var_indexes[i], values);
                            }
                        } else if (currentTab == 'Perf') {
                            $('#listdt').jqGrid('clearGridData');
                            updateSeriesVarVal("y1", 1, []);
                            var rowCount = $('#fixed_var_table').find('tr').length;
                            for( i=rowCount -1; i>= 0; i--){
                                removeFixedVar("fixed_var_" + (i+1));
                            }
                        }  else if (currentTab == 'Taylor') {
                            $('#listdt').jqGrid('clearGridData');
                            updateForecastVariables();
                            updateSeriesVarVal("y1", 1, []);
                        }
                    }
                });
                initPage();
            }
        });
    });
</script>
</HEAD>
<BODY>

<div id="header">
    <div class="toolbar ui-widget" id="toolbar ">
        <div style="float: left; cursor: alias;font-family: 'Arial Black',Gadget,sans-serif;" id="release">METViewer 2.3<span class="ui-icon ui-icon-info " style="float: right;  margin-left: .4em;" ></span>

        </div>
        <label for="database">Database:</label><select id="database" multiple="multiple">
    </select>
        <span style="margin-left:20px;"><button id="generate_plot">Generate Plot</button></span>
        <button id="load_xml" style="float: right">Load XML</button>
        <button id="reload_databases" style="float: right">Reload databases</button>
    </div>
</div>
<div id="series_formatting" class="ui-layout-south " style="overflow: visible; padding: 0 0 10px 10px;" >
    <div >
        <table id="listdt" ></table>
        <div id="pagerdt"></div>
    </div>
</div>
<div id="history" class="ui-layout-west  no-padding" style="overflow: hidden;">

    <div class="ui-widget-header ui-widget add-font-size "
         style="position: relative;padding-left: 20px; padding-top: 5px;padding-bottom: 5px;">
        History
    </div>

    <div class="ui-widget"
         style="visibility: visible;overflow-y: scroll;height: 100%; width: 100%;">
        <div id="show_history_choice" style="text-align: center; margin-top: 5px;">
       <input type="radio" id="show_history_all" name="show_history_choice" value="show_history_all"><label for="show_history_all">All</label>
       <input type="radio" id="show_history_success"  name="show_history_choice" checked="checked" value="show_history_success"><label for="show_history_success">Success</label>
            <button  id="refresh_history">Refresh
               </button>
       </div>

        <div style="overflow: hidden;">
            <table id="history_content" style="table-layout: fixed;">

            </table>
        </div>
    </div>
</div>


    <div id="plot_config" class="ui-layout-center no-padding">
        <ul class="allow-overflow ">
            <li><a href="plotJSP/series.jsp">Series</a></li>
            <li><a href="plotJSP/box.jsp">Box</a></li>
            <li><a href="plotJSP/bar.jsp">Bar</a></li>
            <li><a href="plotJSP/roc.jsp">Roc</a></li>
            <li><a href="plotJSP/rely.jsp">Rely</a></li>
            <li><a href="plotJSP/ens_ssl.jsp">Ens_ss</a></li>
            <li><a href="plotJSP/performance.jsp">Perf</a></li>
            <li><a href="plotJSP/taylor.jsp">Taylor</a></li>
            <li><a href="plotJSP/hist.jsp">Hist</a></li>
            <li><a href="plotJSP/eclv.jsp">Eclv</a></li>
        </ul>



</div>




<div id="plot_display" class="no-padding">


<div id="plot_display_inner" class="ui-layout-center no-padding" >
    <div class="ui-widget-header ui-widget add-font-size " style="position: relative;padding-left: 20px; padding-top: 5px;padding-bottom: 5px;" id="plot_display_inner_header">N/A </div>

    <button id="download_plot" style="top: 4px; float: right; right: 3px;">Download</button>


    <ul style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
        <li><a href="#plot_image" id="plot_image_url">Plot</a></li>
        <li><a href="#plot_xml" id="plot_xml_url">XML</a></li>
        <li><a href="#plot_log" id="plot_log_url">Log</a></li>
        <li><a href="#r_script" id="r_script_url">R script</a></li>
        <li><a id="r_data_url" href="R_work/data/.data">R data</a></li>
        <li><a href="#plot_sql" id="plot_sql_url">SQL</a></li>
        <li><a id="y1_points_url" href="R_work/data/.points1">Y1 Points</a></li>
        <li><a id="y2_points_url" href="R_work/data/.points2">Y2 Points</a></li>
    </ul>
    <div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-top: 0; padding-bottom: 1em;">


            <img src="images/plot_empty.png" id="plot_image" alt="plot" width="99%" height="99%">


        <div id="plot_xml">
        </div>

        <div id="plot_log">
        </div>
        <div id="r_script">
        </div>
        <div id="r_data">
        </div>
        <div id="plot_sql">

        </div>
        <div id="y1_points">

        </div>
        <div id="y2_points">

        </div>

    </div>
</div>




<div id="tab-south" class="ui-layout-south no-padding">
<ul class="allow-overflow ">
    <li><a href="#plot_title_labels">Titles & Labels</a></li>
    <li><a href="#common">Common</a></li>
    <li><a href="#plot_formatting">Formatting</a></li>
    <li><a href="#x1">X1</a></li>
    <li><a href="#x2">X2</a></li>
    <li><a href="#y1">Y1</a></li>
    <li><a href="#y2">Y2</a></li>
    <li><a href="#legend_caption_formatting">Legend & Caption</a></li>
</ul>
<button id="reset_formatting" style="bottom: 28px; float: right; right: 3px;">Reset</button>
<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-top: 0; padding: 0;">
<div id="plot_title_labels">
  <table style="width: 100%;">
    <col width="70">
    <%--<tr>--%>
      <%--<td><label for="file_name">File name</label></td>--%>
      <%--<td><input type="text" id="file_name" value=""--%>
                 <%--style="width: 100%"></td>--%>
    <%--</tr>--%>
    <tr>
      <td><label for="plot_title">Title</label></td>
      <td><input type="text" id="plot_title" value="test title"
                 style="width: 100%"></td>
    </tr>
    <tr>
      <td><label for="x_label_title">X label</label></td>
      <td><input type="text" id="x_label_title" value="test x_label"
                 style="width: 100%"></td>
    </tr>
    <tr>
      <td><label for="y1_label_title">Y1 label</label></td>
      <td><input type="text" id="y1_label_title" value="test y_label"
                 style="width: 100%"></td>
    </tr>
    <tr>
      <td><label for="y1_label_title">Y2 label</label></td>
      <td><input type="text" id="y2_label_title" value=""
                 style="width: 100%"></td>
    </tr>
    <tr>
      <td><label for="caption">Caption</label></td>
      <td><input type="text" id="caption" value="" style="width: 100%">
      </td>
    </tr>
  </table>
</div>
<div id="common">

    <table style="width: 95%;">

        <tr>
            <td><input type="checkbox" id="vert_plot"/>
                <label for="vert_plot">Vertical Levels Plot</label></td>
            <td><input type="checkbox" id="x_reverse"/>
                <label for="x_reverse">Reverse X Values</label></td>
        </tr>
        <tr>
            <td><input type="checkbox" id="num_stats"/>
                <label for="num_stats">Display Number of
                    Stats</label></td>
            <td><input type="checkbox" id="grid_on"/>
                <label for="grid_on">Plot Grid</label></td>
        </tr>

        <tr>
            <td><input type="checkbox" id="sync_axes"/>
                <label for="sync_axes">Synch Y1 and Y2
                    Ranges</label></td>
            <td><input type="checkbox" id="varianceInflationFactor"/>
                <label for="varianceInflationFactor">Variance Inflation Factor</label></td>
        </tr>

        <tr>
            <td><input type="checkbox" id="dump_points1"/>
                <label for="dump_points1">Print Y1 Series Values</label></td>
            <td><input type="checkbox" id="dump_points2"/>
                <label for="dump_points2">Print Y2 Series Values</label></td>
        </tr>

        <tr>
            <td><input type="checkbox" id="indy1_stag"/>
                <label for="indy1_stag">Y1 Stagger Points</label></td>
            <td><input type="checkbox" id="indy2_stag"/>
                <label for="indy2_stag">Y2 Stagger Points</label></td>
        </tr>

        <tr>
            <td colspan="2" style="text-align: center;"><label for="ci_alpha">Conf Interval
                Alpha</label><input type="text"
                                    size="12"
                                    id="ci_alpha"
                                    value="0.05"></td>
        </tr>

    </table>
</div>

<div id="plot_formatting" style="padding: 0 1.4em;">
    <table style="width:100%">
        <tr>
            <td colspan="2">
                <fieldset>
                    <legend>Plot Formatting</legend>
                    <table>
                        <tr>
                            <td><label for="plot_type">Image Type</label></td>
                            <td><select id="plot_type"><option>png16m</option><option>jpeg</option></select></td>
                            <td><label for="plot_height">Height</label></td>
                            <td><input type="text" size="11" id="plot_height" value="8.5"></td>
                            <td><label for="plot_width">Width</label></td>
                            <td><input type="text" size="11" id="plot_width" value="11"></td>

                        </tr>
                        <tr>

                            <td><label for="plot_units">Units</label></td>
                            <td><select id="plot_units" style="width:80px;"><option>in</option><option>mm</option></select></td>
                            <td><label for="cex">Text Magnification</label></td>
                            <td><input id="cex" type="text" value="1" size="11"></td>
                            <td><label for="plot_res">Resolution</label></td>
                            <td><input type="text" size="11" id="plot_res" value="72"></td>
                        </tr>
                        <tr>
                            <td><label >Margins</label></td>
                            <td colspan="2">
                              <input type="text" size="3" id="mar_bottom" style="width:30px; margin-right:5px;">
                              <input type="text" size="3" id="mar_left" style="width:30px; margin-right:5px;">
                              <input type="text" size="3" id="mar_top" style="width:30px; margin-right:5px;">
                              <input type="text" size="3" id="mar_right" style="width:30px; ">
                            </td>
                            <td><label>Axis Margin Line</label></td>
                          <td colspan="2">
                            <input type="text" size="3" id="mgp_title" style="width:30px; margin-right:5px;">
                            <input type="text" size="3" id="mgp_labels" style="width:30px; margin-right:5px;">
                            <input type="text" size="3" id="mgp_line" style="width:30px; margin-right:5px;">
                            </td>
                        </tr>

                    </table>
                </fieldset>
            </td>
        </tr>
        <tr>
            <td>
                <fieldset>
                    <legend>Title Formatting</legend>
                    <table>
                        <tr>
                            <td><label for="title_align">Horizontal align</label></td>
                            <td><input type="text" size="3" id="title_align" value="0.5"></td>

                            <td><label for="title_align">Perpendicular offset</label></td>
                            <td><input type="text" size="3" id="title_offset" value="-2"></td>

                        </tr>
                        <tr>
                            <td><label for="title_size">Text Size</label></td>
                            <td><input type="text" size="3" id="title_size" value="1.4"></td>
                            <td><label for="title_weight">Text Weight</label></td>
                            <td><select id="title_weight" autocomplete="off">
                                            <option value="1">plain text</option>
                                            <option value="2" selected="selected">bold</option>
                                            <option value="3">italic</option>
                                            <option value="4">bold italic</option>
                                            <option value="5">symbol</option>
                                        </select></td>
                        </tr>
                    </table>
                </fieldset>
            </td>
            <td>
                <fieldset>
                    <legend>Grid Line Formatting</legend>
                    <table>
                        <tr>
                            <td><label for="grid_lty">Line Type</label></td>
                            <td><select autocomplete="off"id="grid_lty">
                                            <option value=0>blank</option>
                                            <option value=1>solid</option>
                                            <option value=2>dashed</option>
                                            <option value=3 selected="selected">dotted</option>
                                            <option value=4>dotdash</option>
                                            <option value=5>longdash</option>
                                            <option value=6>twodash</option>
                                        </select></td>
                            <td><label for="grid_lwd">Line Width</label></td>
                            <td><input type="text" size="3" id="grid_lwd" value="1"></td>
                        </tr>
                        <tr>
                            <td><label for="grid_col">Line Color</label></td>
                            <td><input id="grid_col" type="text"  size="8"  class="cp-basic"></td>

                            <td><label for="grid_x">X positions</label></td>
                            <td><input type="text" size="3" id="grid_x" value="listX"></td>
                        </tr>
                    </table>
                </fieldset>
            </td>
        </tr>
    </table>



    <label for="plot_cmd">Plot Script Commands</label><input type="text" style="width: 99%;"

                                                                     id="plot_cmd"
                                                                     value="">

</div>

<div id="x1">
    <table>
        <tr>
            <td style="vertical-align: top;">
                <fieldset>
                    <legend>X1 axis label formatting</legend>
                    <table>
                        <tr>
                            <td><label for="xlab_align">Horizontal align</label></td>
                            <td><input type="text" size="12" id="xlab_align" value="0.5"></td>
                        </tr>
                        <tr>
                            <td><label for="xlab_offset">Perpendicular offset</label></td>
                            <td><input type="text" size="12" id="xlab_offset" value="2"></td>
                        </tr>
                        <tr>
                            <td><label for="xlab_size">Text size</label></td>
                            <td><input type="text" size="12" id="xlab_size" value="1"></td>
                        </tr>

                        <tr>
                            <td><label for="xlab_weight">Text Weight</label></td>
                            <td><select id="xlab_weight">
                                            <option value="1">Plain text</option>
                                            <option value="2">Bold</option>
                                            <option value="3">Italic</option>
                                            <option value="4">Bold Italic</option>
                                            <option value="5">Symbol</option>
                                        </select></td>
                        </tr>


                    </table>
                </fieldset>
            </td>
            <td style="vertical-align: top;">
                <fieldset>
                    <legend>X1 values formatting</legend>
                    <table>
                        <tr>
                            <td><label for="xtlab_horiz">Horizontal align</label></td>
                            <td><input type="text" size="23" id="xtlab_horiz" value="0.5"></td>
                        </tr>
                        <tr>
                            <td><label for="xtlab_perp">Perpendicular offset</label></td>
                            <td><input type="text" size="23" id="xtlab_perp" value="-0.75"></td>
                        </tr>
                        <tr>
                            <td><label for="xtlab_size">Text size</label></td>
                            <td><input type="text" size="23" id="xtlab_size" value="1"></td>
                        </tr>
                        <tr>
                            <td><label for="xtlab_freq">Frequency</label></td>
                            <td><input type="text" size="23" id="xtlab_freq" value="0"></td>
                        </tr>
                        <tr>
                            <td><label for="xtlab_orient">Orientation</label></td>
                            <td><select id="xtlab_orient">
                                                <option value="0">Parallel to axis</option>
                                                <option value="1" selected>Horizontal</option>
                                                <option value="2">Perpendicular to axis</option>
                                                <option value="3">Vertical</option>
                                            </select></td>
                        </tr>


                    </table>
                </fieldset>
            </td>
        </tr>
    </table>

</div>
<div id="x2">
    <table>
           <tr>
               <td style="vertical-align: top;">
                   <fieldset>
                       <legend>X2 axis label formatting</legend>
                       <table>
                           <tr>
                               <td><label for="x2lab_align">Horizontal align</label></td>
                               <td><input type="text" size="12" id="x2lab_align" value="0.5"></td>
                           </tr>
                           <tr>
                               <td><label for="x2lab_offset">Perpendicular offset</label></td>
                               <td><input type="text" size="12" id="x2lab_offset" value="-.5"></td>
                           </tr>
                           <tr>
                               <td><label for="x2lab_size">Text size</label></td>
                               <td><input type="text" size="12" id="x2lab_size" value=".8"></td>
                           </tr>

                           <tr>
                               <td><label for="x2lab_weight">Text Weight</label></td>
                               <td><select id="x2lab_weight">
                                               <option value="1">Plain text</option>
                                               <option value="2">Bold</option>
                                               <option value="3">Italic</option>
                                               <option value="4">Bold Italic</option>
                                               <option value="5">Symbol</option>
                                           </select></td>
                           </tr>


                       </table>
                   </fieldset>
               </td>
               <td style="vertical-align: top;">
                   <fieldset>
                       <legend>X2 values formatting</legend>
                       <table>
                           <tr>
                               <td><label for="x2tlab_horiz">Horizontal align</label></td>
                               <td><input type="text" size="23" id="x2tlab_horiz" value="0.5"></td>
                           </tr>
                           <tr>
                               <td><label for="x2tlab_perp">Perpendicular offset</label></td>
                               <td><input type="text" size="23" id="x2tlab_perp" value="1"></td>
                           </tr>
                           <tr>
                               <td><label for="x2tlab_size">Text size</label></td>
                               <td><input type="text" size="23" id="x2tlab_size" value=".8"></td>
                           </tr>

                           <tr>
                               <td><label for="x2tlab_orient">Orientation</label></td>
                               <td><select id="x2tlab_orient">
                                                   <option value="0">Parallel to axis</option>
                                                   <option value="1" selected>Horizontal</option>
                                                   <option value="2">Perpendicular to axis</option>
                                                   <option value="3">Vertical</option>
                                               </select></td>
                           </tr>


                       </table>
                   </fieldset>
               </td>
           </tr>
       </table>
</div>
<div id="y1">
    <table>
        <tr>
            <td colspan="2">
                <fieldset>
                    <legend>Y1 Bounds</legend>
                    <table style="width:100%"><tr>
                        <td><label  style="margin-right: 5px;">Limits</label><input type="text" size="6" id="y1_lim_min" style="width:40px; margin-right:5px;"><input type="text" size="6" id="y1_lim_max" style="width:40px; margin-left:5px;"></td>
                        <td><label for="y1_bufr" style="margin-right: 5px;">Top and bottom buffer</label><input type="text" size="12" id="y1_bufr">
                    </tr></table>
                </fieldset>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;">
                <fieldset>
                    <legend>Y1 axis label formatting</legend>
                    <table>
                        <tr>
                            <td><label for="ylab_align">Horizontal align</label></td>
                            <td><input type="text" size="12" id="ylab_align" ></td>
                        </tr>
                        <tr>
                            <td><label for="ylab_offset">Perpendicular offset</label></td>
                            <td><input type="text" size="12" id="ylab_offset"></td>
                        </tr>
                        <tr>
                            <td><label for="ylab_size">Text size</label></td>
                            <td><input type="text" size="12" id="ylab_size" ></td>
                        </tr>

                        <tr>
                            <td><label for="ylab_weight">Text Weight</label></td>
                            <td><select id="ylab_weight">
                                            <option value="1">Plain text</option>
                                            <option value="2">Bold</option>
                                            <option value="3">Italic</option>
                                            <option value="4">Bold Italic</option>
                                            <option value="5">Symbol</option>
                                        </select></td>
                        </tr>
                    </table>
                </fieldset>
            </td>
            <td style="vertical-align: top;">
                <fieldset>
                    <legend>Y1 values formatting</legend>
                    <table>
                        <tr>
                            <td><label for="ytlab_horiz">Horizontal
                                align</label></td>
                            <td><input type="text" size="23" id="ytlab_horiz"
                                       value="0.5"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_perp">Perpendicular
                                offset</label></td>
                            <td><input type="text" size="23" id="ytlab_perp" value=".5"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_size">Text size</label></td>
                            <td><input type="text" size="23" id="ytlab_size" value="1"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_orient">Orientation</label>
                            </td>
                            <td><select id="ytlab_orient">
                                <option value="0">Parallel to axis</option>
                                <option value="1" selected>Horizontal</option>
                                <option value="2">Perpendicular to axis
                                </option>
                                <option value="3">Vertical</option>
                            </select></td>
                        </tr>
                    </table>
                </fieldset>
            </td>
        </tr>
    </table>
</div>
<div id="y2">
    <table>
        <tr>
                   <td colspan="2">
                       <fieldset>
                           <legend>Y2 Bounds</legend>
                           <table style="width:100%"><tr>
                             <td><label  style="margin-right: 5px;">Limits</label><input type="text" size="6" id="y2_lim_min" style="width:40px; margin-right:5px;"><input type="text" size="6" id="y2_lim_max" style="width:40px; margin-left:5px;"></td>
                               <td><label for="y2_bufr" style="margin-right: 5px;">Top and bottom buffer</label><input type="text" size="12" id="y2_bufr">
                           </tr></table>
                       </fieldset></td>
                   </td>
               </tr>
           <tr>
               <td style="vertical-align: top;">
                   <fieldset>
                       <legend>Y2 axis label formatting</legend>
                       <table>
                           <tr>
                               <td><label for="y2lab_align">Horizontal align</label></td>
                               <td><input type="text" size="12" id="y2lab_align" value="0.5"></td>
                           </tr>
                           <tr>
                               <td><label for="y2lab_offset">Perpendicular offset</label></td>
                               <td><input type="text" size="12" id="y2lab_offset" value="1"></td>
                           </tr>
                           <tr>
                               <td><label for="y2lab_size">Text size</label></td>
                               <td><input type="text" size="12" id="y2lab_size" value="1"></td>
                           </tr>

                           <tr>
                               <td><label for="y2lab_weight">Text Weight</label></td>
                               <td><select id="y2lab_weight">
                                               <option value="1">Plain text</option>
                                               <option value="2">Bold</option>
                                               <option value="3">Italic</option>
                                               <option value="4">Bold Italic</option>
                                               <option value="5">Symbol</option>
                                           </select></td>
                           </tr>


                       </table>
                   </fieldset>
               </td>
               <td style="vertical-align: top;">
                   <fieldset>
                       <legend>Y2 values formatting</legend>
                       <table>
                           <tr>
                               <td><label for="y2tlab_horiz">Horizontal align</label></td>
                               <td><input type="text" size="23" id="y2tlab_horiz" value="0.5"></td>
                           </tr>
                           <tr>
                               <td><label for="y2tlab_perp">Perpendicular offset</label></td>
                               <td><input type="text" size="23" id="y2tlab_perp" value=".5"></td>
                           </tr>
                           <tr>
                               <td><label for="y2tlab_size">Text size</label></td>
                               <td><input type="text" size="23" id="y2tlab_size" value="1"></td>
                           </tr>
                           <tr>
                               <td><label for="y2tlab_orient">Orientation</label></td>
                               <td><select id="y2tlab_orient">
                                                   <option value="0">Parallel to axis</option>
                                                   <option value="1" selected>Horizontal</option>
                                                   <option value="2">Perpendicular to axis</option>
                                                   <option value="3">Vertical</option>
                                               </select></td>
                           </tr>


                       </table>
                   </fieldset>
               </td>
           </tr>
       </table>
</div>
<div id="legend_caption_formatting">
    <table>
              <tr>
                  <td style="vertical-align: top;">
                      <fieldset>
                          <legend>Legend formatting</legend>
                          <table>
                              <tr>
                                  <td><label for="legend_size">Text Size</label></td>
                                  <td><input type="text" size="10" id="legend_size" value="0.8"></td>
                              </tr>
                              <tr>
                                  <td><label>Box Position</label></td>
                                  <td><input type="text" size="5" id="legend_inset_min" style="width:40px; margin-right:5px;"><input type="text" size="5" id="legend_inset_max" style="width:40px; margin-left:5px;"></td>
                              </tr>
                              <tr>
                                  <td><label for="legend_box" style="width:75px">Box Type</label></td>
                                  <td><select id="legend_box"><option value="o">Box</option><option value="n">None</option></select></td>
                              </tr>

                              <tr>
                                  <td><label for="legend_ncol"># of Columns</label></td>
                                  <td><input type="text" size="10" id="legend_ncol" value="3"></td>
                              </tr>


                          </table>
                      </fieldset>
                  </td>
                  <td style="vertical-align: top;">
                      <fieldset>
                          <legend>Caption formatting</legend>
                          <table>
                              <tr>
                                <td><label for="caption_align">Horizontal align</label></td>
                                <td><input type="text" size="12" id="caption_align" value="0"></td>
                              </tr>
                              <tr>
                                  <td><label for="caption_offset">Perpendicular offset</label></td>
                                  <td><input type="text" size="12" id="caption_offset" value="3"></td>
                              </tr>
                              <tr>
                                  <td><label for="caption_size">Text size</label></td>
                                  <td><input type="text" size="12" id="caption_size" value="0.8"></td>
                              </tr>
                              <tr>
                                  <td><label for="caption_weight">Text Weight</label></td>
                                  <td><select id="caption_weight">
                                                  <option value="1">Plain text</option>
                                                  <option value="2">Bold</option>
                                                  <option value="3">Italic</option>
                                                  <option value="4">Bold Italic</option>
                                                  <option value="5">Symbol</option>
                                              </select></td>
                              </tr>
                            <tr>
                                  <td><label for="caption_col">Text Color</label></td>
                                  <td><input type="text" size="12" id="caption_col"  class="cp-basic"></td>
                              </tr>


                          </table>
                      </fieldset>
                  </td>
              </tr>
          </table>
</div>
</div>




</div>

</div>
<div id="unavailableDiffCurveDialogForm" title="Add Derived Curve">
    <div style="margin-top: 20px; padding: 0 .7em;" class="ui-state-highlight ui-corner-all">
    		<p><span style="float: left; margin-right: .3em;" class="ui-icon ui-icon-info"></span>
                Series Difference Curves are not supported for this plot type.</p>
    	</div>
</div>

<div id="incorrectDiffCurveDialogForm" title="Add Derived Curve">
    <div style="margin-top: 20px; padding: 0 .7em;" class="ui-state-highlight ui-corner-all">
    		<p><span style="float: left; margin-right: .3em;" class="ui-icon ui-icon-info"></span>
                It should be more than one series to create a derived curve.</p>
    	</div>
</div>
<div id="addDiffCurveDialogForm" title="Add Derived Curve">
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
                                     onchange=" createNewDerivedSeriesName(1);"></select>
                        </div>
                        <div class="diffSelect header" style="font-size:12px;text-align:center;">and </div>
                        <div class="diffSelect">
                        <select name="series2Y1" id="series2Y1"
                                     onchange="createNewDerivedSeriesName(1)"></select>
                        </div>

                    </fieldset>
                </td>
                <td>
                    <fieldset>
                        <div class="diffSelect">

                        <select name="series1Y2" id="series1Y2" disabled
                                     onchange=" createNewDerivedSeriesName(2)">
                        </select></div>
                        <div class="diffSelect header" style="font-size:12px;text-align:center;">minus </div>
                        <div class="diffSelect">

                        <select name="series2Y2" id="series2Y2" disabled
                                     onchange="createNewDerivedSeriesName(2)">
                        </select></div>

                    </fieldset>
                </td>
            </tr>
        </table>
            <div><input type="radio" name="derive_oper" value="DIFF" id="derive_oper_diff" checked /><label for="derive_oper_diff">DIFF</label>
            <input type="radio" name="derive_oper" value="RATIO" id="derive_oper_ratio" checked /><label for="derive_oper_ratio">RATIO</label>
            <input type="radio" name="derive_oper" value="SS" id="derive_oper_SS" checked /><label for="derive_oper_ss">SKILL SCORE</label>
            </div>
        <div id="newDiffSeriesName" class="diffSelect" style="font-weight:bold;"></div>
        </div>
    </form>
    <div style="font-size:9px;"> * Event Equalizer selection will be changed to "TRUE" if at least one DIFF series is selected.</div>
</div>

<div id="error_message" title="Error during creating the plot">
    <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
        <p>
        <span class="ui-icon ui-icon-alert"
              style="float:left; margin:0 7px 50px 0;"></span>

        <div id="error_message_text" style="color: #1A1A1A;"></div>
        </p>
    </div>
</div>

<div id="r_error_message" title="Warnings during creating the plot">
    <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;">
        <p>
        <span class="ui-icon ui-icon-info"
              style="float:left; margin:0 7px 50px 0;"></span>

        <div id="r_error_message_text" style="color: #1A1A1A;"></div>
        </p>
    </div>
</div>
<div id="fade"></div>
<div id="modal">
    <img id="loader" src="images/loading.gif" />
</div>
<div id='upload_file_dialog' title="Upload plot XML file">
    <form id="formUpload" method="post" enctype="multipart/form-data" action="servlet?jsp=new">
        <input type="file" name="fileUpload" accept="application/xml">
    </form>
</div>
<form id="formUploadLocal" method="post"  action="servlet">
        <input type="hidden" name="fileUploadLocal" id="uploadLocalId" value="">
    </form>

</BODY>
</HTML>
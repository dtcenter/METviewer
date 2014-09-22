<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<HTML>
<HEAD>
<META http-equiv="content-type" content="text/html; charset=utf-8">

<TITLE>METViewer</TITLE>
<link rel="shortcut icon" href="./favicon.ico">

<link rel="stylesheet"
      href="css/custom-smoothness/jquery-ui-1.10.4.custom.min.css"/>
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
</style>

<script src="js/jquery-1.9.1.js" type="text/javascript"></script>
<script src="js/jquery-ui-1.10.3.custom.min.js"
        type="text/javascript"></script>
<script src="js/jquery.layout-latest.min.js"
        type="text/javascript"></script>
<script type="text/javascript"
        src="js/jquery.layout.resizeTabLayout-latest.min.js"></script>
<script type="text/javascript" src="js/grid.locale-en.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/jquery.colorpicker.js"></script>
<script type="text/javascript" src="js/jquery.multiselect.js"></script>
<script type="text/javascript" src="js/jquery.actual.min.js"></script>
<script type="text/javascript"
        src="js/swatches/jquery.ui.colorpicker-pantone.js"></script>
<script type="text/javascript" src="js/metviewer_common.js"></script>

<script type="text/javascript">

    var outerLayout, innerLayout, colorOfSelectedGroup;
    var boxID = 0;
    var center = -381;
    var top = -306;
    var currentTab = 'Series';
    var lastSelRow, lastSelCol;
    var currentPlotTab = 'plot_image';
    var resultName;
    var strInitXML = '<%= session.getAttribute("init_xml") %>';
    var initXML;

    if(strInitXML != "null" && strInitXML.length > 0){
        initXML = $( strInitXML );
    }
    var series1Names=[];
    var series2Names=[];


    $(document).ready(function () {

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


                var values = $(data).find("val");
                var databaseEl = $("#database");
                var selected, selectedDatabase;
                if (initXML != null) {
                    selectedDatabase = initXML.find("database").text();
                }else{
                    selectedDatabase = querySt("db");
                }
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    if (selectedDatabase != null) {
                        selected = t.text() == selectedDatabase;
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
                    multiple: false,
                    header: false,
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
                            updateSeriesVarValSeries("y1", 1, []);
                            updateSeriesVarValSeries("y2", 1, []);
                        } else if (currentTab == 'Rhist' || currentTab == 'Phist' || currentTab == 'Roc' || currentTab == 'Rely') {
                            for (i = 0; i < fixed_var_indexes.length; i++) {
                                values = $("#fixed_var_val_" + fixed_var_indexes[i]).val();
                                updateFixedVarValHist(fixed_var_indexes[i], values);
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
                        }
                    }
                });
                initPage();
            }});
    });
    function addThemeSwitcher(container, position) {
        var pos = { top: '15px', zIndex: 10 };
        $('<div id="themeContainer" style="position: absolute; overflow-x: hidden;"></div>')
                .css($.extend(pos, position))
                .appendTo(container || 'body')
                .themeswitcher()
        ;
    }



    function initPage() {


        outerLayout = $("body").layout({
            name: "outer" // used for button binding
            , north__paneSelector: "#header"
            , south__paneSelector: "#series_formatting"
            , north__closable: false
            , north__resizable: false
            , north__spacing_open: 0
            , north__size: 55
            , east__paneSelector: "#plot_display"
            , center__paneSelector: "#plot_config"
            , south__minSize: 112
            , east__size: '50%'
            , east__minSize: 100
            , east__maxSize: 800
            , east__onresize: 'innerLayout.resizeAll'
            , resizeWhileDragging: true
            , east__closable: false
            , west__resizable: true
            , west__spacing_closed: 15
            , west__togglerLength_closed: 20
            , west__togglerAlign_closed: "top"		// align to top of resizer
            , west__paneSelector: "#history"
            , west__initClosed:	true
            , west__togglerLength_open: 0
            , west__initClose: true
            , west__togglerTip_open: "Close History"
            , west__togglerTip_closed: "Open History"
            , west__togglerClass: "history-toggler"
            , west__fxName: "slide"		// none, slide, drop, scale
            , autoBindCustomButtons: true
            , south__togglerLength_open: 0
            , west__onopen_start: function () {
                refreshHistory();
            }

            , useStateCookie: false
            //, cookie__keys: "west.size,west.isClosed"
        });
        $("#plot_display_inner").tabs({
            //disabled: [ 1, 2, 3, 4, 5 ],
            heightStyle: "content",
            beforeActivate: function (event, ui) {
                            currentPlotTab= ui.newPanel.attr('id');
                            //alert(ui.newTab.index());
                          }
        });
        $("#tab-south").tabs({collapsible: false});

        innerLayout = $("#plot_display").layout({
            name: "inner" // used for button binding
            , closable: false
            , center__paneSelector: "#plot_display_inner"
            , south__spacing_closed: 0
            , south__size: "30%", south__paneSelector: "#tab-south", south__resizable: true
        });


        $('body').on('resize', function () {
            $("#plot_display_inner").tabs('refresh');
            });

//addThemeSwitcher( '#toolbar', { top: '15px' });



        var westSelector = "body > .ui-layout-west"; // outer-west pane
        $("<span></span>").attr("id", "west-closer").prependTo(westSelector);
        outerLayout.addCloseBtn("#west-closer", "west");

        $("#listdt").jqGrid({
            datatype: "local",
            autowidth: true,
            shrinkToFit: true,
            colNames: ['ID', '#', 'Y axis', 'Hide', 'Title', 'Conf Interval', 'Line Color', 'Point Symbol', 'Series Line Type', 'Line Type', 'Line Width', 'Show Significant','Connect Across NA', 'Legend Text'],
            colModel: [
                {name: 'id', index: 'id', hidden: true},
                {name: 'order', width: 10, index: 'order', sortable: false},
                {name: 'y_axis', width: 30, index: 'y_axis', align: "center"},
                {name: 'hide', index: 'hide', width: 30, align: "center", editable: true, edittype: "checkbox", editoptions: {value: "Yes:No"}},
                {name: 'title', index: 'title', width: 300},
                {name: 'plot_ci', index: 'plot_ci', width: 70, editable: true, edittype: "select",formatter:'select', editoptions: {value: "none:none;boot:boot;norm:norm;brier:brier;std:std"}, align: "center"},
                {name: 'color', index: 'color', width: 40, editable: true, edittype: 'custom', editoptions: {custom_element: myelem, custom_value: myvalue}, formatter: colorDisplayFmatter, unformat: colorDisplayUnmatter, align: "center"},
                {name: 'pch', index: 'pch', editable: true, width: 70, align: "center", edittype: "select", formatter:'select',editoptions: {value: "20:Small circle;19:Circle;15:Square;17:Triangle;18:Rhombus"}},
                {name: 'type', index: 'type', sortable: false, editable: true, edittype: "select", formatter:'select',editoptions: {value: "p:points;l:lines;o:overplotted;b:joined lines;s:stair steps;h:histogram-like;n:nothing;"}, width: 50, align: "center"},
                {name: 'lty', index: 'lty', sortable: false, editable: true, edittype: "select", formatter:'select',editoptions: {value: "1:solid;2:dashed;3:dotted;4:dot-dash;5:long dash;6:long short"}, width: 70, align: "center"},
                {name: 'lwd', index: 'lwd', sortable: false, editable: true, sorttype: "int", width: 40, align: "center"},
                {name: 'show_signif', index: 'hide', width: 30, align: "center", editable: true, edittype: "checkbox", editoptions: {value: "Yes:No"}},
                {name: 'con_series', index: 'con_series', sortable: false, editable: true, edittype: "select", formatter:'select',editoptions: {value: "0:No;1:Yes"}, width: 30, align: "center"},
                {name: 'legend', index: 'legend', sortable: false, width: 150, align: "left",editable: true}
            ],
            rowNum: -1,
            height: 'auto',
            pager: '#pagerdt',
            pginput: false,
            pgtext: false,
            pgbuttons: false,
            viewrecords: true,
            caption: "Series Formatting",
            cmTemplate: { title: false },
            cellEdit: true,
            hoverrows: false,
            url: 'clientArray',
            cellsubmit: 'clientArray',
            afterInsertRow: function (rowid, rowdata) {
                $('#color_' + rowdata.id).colorpicker({
                    parts: ['header', 'map', 'bar', 'hex', 'rgb', 'alpha', 'preview', 'swatches', 'footer']
                })
            },
            beforeEditCell: function(rowid, cellname, value, iRow, iCol) {
                lastSelRow = iRow;
                lastSelCol = iCol;
            }
        });
        $("#listdt").jqGrid('navGrid', '#pagerdt', {edit: false, add: false, del: false, search: false, refresh: false});
        $("#listdt").jqGrid('navButtonAdd', '#pagerdt', {
            caption: "Add Difference Curve",
            title: "Add Difference Curve",
            buttonicon: "ui-icon-plus",
            onClickButton: function () {
                if(currentTab == 'Rhist' ||currentTab == 'Phist' || currentTab == 'Roc' || currentTab == 'Rely' || currentTab == 'Ens_ss'){
                    $("#unavailableDiffCurveDialogForm").dialog("open");
                }else{
                    var allSeries = $("#listdt").jqGrid('getRowData');
                    if(allSeries.length > 1){
                        $("#addDiffCurveDialogForm").dialog("open");
                    }else{
                        $("#incorrectDiffCurveDialogForm").dialog("open");
                    }
                }
            }
        }).jqGrid('navButtonAdd', '#pagerdt', {
                    caption: "Remove Difference Curve",
                    title: "Remove Difference Curve",
                    buttonicon: "ui-icon-trash",
                    onClickButton: function () {
                        var sr = $(this).jqGrid('getGridParam', 'selrow');
                        if (sr) {
                            var rowData = $(this).getRowData(sr);
                            if (rowData.title.startsWith("DIFF")) {
                                $(this).jqGrid('delRowData', sr);
                                var titleArr = rowData.title.replace("DIFF (", "").replace(")", "").split('"-"');
                                for (var i = 0; i < titleArr.length; i++) {
                                    titleArr[i] = titleArr[i].replace('"', "");
                                }
                                var title = titleArr.join();
                                var index;
                                if (rowData.y_axis == "Y1") {
                                    index = seriesDiffY1.indexOf(title);
                                    if (index > -1) {
                                        seriesDiffY1.splice(index, 1);
                                    }
                                } else {
                                    index = seriesDiffY2.indexOf(title);
                                    if (index > -1) {
                                        seriesDiffY2.splice(index, 1);
                                    }
                                }
                                //renumber
                                var allSeries = $("#listdt").jqGrid('getRowData');
                                for (var i = 0; i < allSeries.length; i++) {
                                    $("#listdt").jqGrid('setCell', allSeries[i].id, 'order', i + 1);
                                    $("#listdt").jqGrid('getLocalRow', allSeries[i].id).order = i + 1;
                                }
                            }else{
                                var idSelector = "#alertmod_" + this.p.id;
                                                           $.jgrid.viewModal(idSelector, {
                                                               gbox: "#gbox_" + $.jgrid.jqID(this.p.id),
                                                               jqm: true
                                                           });
                                                           $(idSelector).find("#alertcnt_listdt").empty().append('<div>This is not a difference curve</div>');
                                                           $(idSelector).find(".ui-jqdialog-titlebar-close").focus();
                            }
                        } else {
                            var idSelector = "#alertmod_" + this.p.id;
                            $.jgrid.viewModal(idSelector, {
                                gbox: "#gbox_" + $.jgrid.jqID(this.p.id),
                                jqm: true
                            });
                            $(idSelector).find(".ui-jqdialog-titlebar-close").focus();
                        }
                    }
                }).jqGrid('navButtonAdd', '#pagerdt', {
                    caption: "Apply defaults",
                    title: "Apply defaults",
                    buttonicon: "ui-icon-transferthick-e-w",
                    onClickButton: function () {
                        /*var sr = jQuery(this).jqGrid('getGridParam', 'selrow');
                         if (sr) {
                         jQuery(this).jqGrid('delRowData', sr);
                         } else {
                         jQuery.jgrid.viewModal("#alertmod", {gbox: "#gbox_" + "<portlet:namespace />criteriaTable", jqm: true});
                         jQuery("#jqg_alrt").focus();
                         }*/
                        alert("defaults..")
                    }
                });
        $("#listdt").setGridWidth($(window).width()-20);

        $("#unavailableDiffCurveDialogForm").dialog({
            autoOpen: false,
            height: "auto",
            width: "auto",
            modal: true,
            buttons: {
                Ok: function () {
                    $(this).dialog("close");
                }
            }
        });

        $("#incorrectDiffCurveDialogForm").dialog({
            autoOpen: false,
            height: "auto",
            width: "auto",
            modal: true,
            buttons: {
                Ok: function () {
                    $(this).dialog("close");
                }
            }
        });
        $("#addDiffCurveDialogForm").dialog({
            autoOpen: false,
            height: "auto",
            width: "auto",
            modal: true,
            buttons: {
                "Create a Difference Curve": function () {
                    var valid=false;
                    var yAxisValue = $('input:radio[name=yAxisDiff]:checked').val();
                    if (yAxisValue.indexOf("1") !== -1) {
                        if($('#series1Y1').val() &&  $('#series2Y1').val()){
                            seriesDiffY1.push($('#series1Y1').val() + "," + $('#series2Y1').val());
                            valid = true;
                        }
                    } else {
                        if($('#series1Y2').val() &&  $('#series2Y2').val()){
                            seriesDiffY2.push($('#series1Y2').val() + "," + $('#series2Y2').val());
                            valid = true;
                        }
                    }
                    $(this).dialog("close");
                    if(valid){
                        if (currentTab == 'Series') {
                            updateSeriesSeriesBox();
                        }
                        //force Event Equalizer
                        $("#event_equal").prop('checked', true);

                    }

                },
                Cancel: function () {
                    $(this).dialog("close");
                }
            },
            open: function () {
                var allSeries = $("#listdt").jqGrid('getRowData');
                for (var i = 0; i < allSeries.length; i++) {
                    $("#listdt").jqGrid('setCell', allSeries[i].id, 'order', i + 1);
                    $("#listdt").jqGrid('getLocalRow', allSeries[i].id).order = i + 1;
                }
                var selected_mode = $("#plot_data").multiselect("getChecked").val();
                $('#series1Y2').empty();
                $('#series1Y1').empty();
                $('#series2Y2').empty();
                $('#series2Y1').empty();

                $("#y1AxisDiff").prop("checked", true);
                $("#y2AxisDiff").removeAttr("checked");

                $('#series1Y2').attr("disabled", true);
                $('#series2Y2').attr("disabled", true);
                $('#series1Y1').removeAttr('disabled');
                $('#series2Y1').removeAttr('disabled');
                $('#y2AxisDiff').removeAttr("disabled");
                $('#y1AxisDiff').removeAttr("disabled");
                series1Names=[];
                series2Names=[];


                for (var i = 0; i < allSeries.length; i++) {
                    var isInclude = false;
                    if (allSeries[i].title.indexOf('DIFF') != 0) {

                        // curve can be included ONLY if it is MODE Ratio stat or any of Stat stats
                        if (selected_mode == "mode") {
                            var desc = allSeries[i].title.split(" ");
                            if (listStatModelRatio.indexOf(desc[desc.length - 1]) > -1) {
                                isInclude = true;
                            }
                        } else {
                            isInclude = true;
                        }
                    }


                    if (isInclude) {
                        var yAxisText = allSeries[i].y_axis;

                        if (yAxisText.indexOf("2") !== -1) {
                            $('#series1Y2')
                                    .append($("<option></option>")
                                            .attr("value", allSeries[i].title)
                                            .text(allSeries[i].title));
                            series2Names.push(allSeries[i].title);
                        } else {
                            $('#series1Y1')
                                    .append($("<option></option>")
                                            .attr("value", allSeries[i].title)
                                            .text(allSeries[i].title));

                            series1Names.push(allSeries[i].title);
                        }
                    }
                }

                populateSecondSelect(1,series1Names);
                populateSecondSelect(2,series2Names);

                if ($("#series1Y2 option").length > 0 && $("#series1Y1 option").length > 0) {
                    createNewDiffSeriesName(1);
                } else {
                    if ($("#series1Y2 option").length == 0) {
                        $('#y2AxisDiff').attr("disabled", true);
                        createNewDiffSeriesName(1);
                    }
                    if ($("#series1Y1 option").length == 0) {
                        $('#y1AxisDiff').attr("disabled", true);
                        $("#y1AxisDiff").removeAttr("checked");
                        $("#y2AxisDiff").prop("checked", true);
                        $('#series1Y2').removeAttr('disabled');
                        $('#series2Y2').removeAttr('disabled');
                        $('#series1Y1').attr("disabled", true);
                        $('#series2Y1').attr("disabled", true);
                        createNewDiffSeriesName(2);
                    }
                }


            },
            close: function () {
                //allFields.val("").removeClass("ui-state-error");
            }
        });

        $(window).bind('resize',function () {
            $("#listdt").setGridWidth($(window).width()-20);
        }).trigger('resize');
        $("#listdt").jqGrid('sortableRows', {
            update: function (e, ui) {
                var allSeries = $("#listdt").jqGrid('getRowData');
                for(var i=0; i< allSeries.length; i++){
                    $("#listdt").jqGrid('setCell', allSeries[i].id, 'order', i+1);
                    $("#listdt").jqGrid('getLocalRow', allSeries[i].id).order = i+1;
                }
            }
        });

        $("#pagerdt_left table.navtable tbody tr").append( // here 'pager' part or #pager_left is the id of the pager
                '<td><div ><input type="checkbox"  style="margin:0 4px;" id="seriesLock"/><label for="seriesLock">Lock Formatting</label></div></td>');
        $("#seriesLock").change(function () {
            if ($(this).is(':checked')) {
                $(this).attr("checked", "checked");
            }
            else {
                $(this).removeAttr("checked");
            }
        });

        $("#generate_plot").button({}).click(function () {
            sendXml();
        });

        $("#load_xml").button({}).click(function () {
            $("#upload_file_dialog").dialog("open");

        });

        $("#reload_databases").button({}).click(function () {
            requestDBUpdate();
        });
        $("input[name=show_history_choice][value=show_history_success]").prop('checked', true);
        $( "#show_history_choice" ).buttonset();
        $('#grid_col').colorpicker({
            parts: ['header', 'map', 'bar', 'hex', 'rgb', 'alpha', 'preview', 'swatches', 'footer'],
            showOn: 'focus alt click',
            buttonColorize: true,
            buttonImageOnly: true
        });
        $('#grid_col').colorpicker('setColor', '#CCCCCC');
        $('#caption_col').colorpicker({
            parts: ['header', 'map', 'bar', 'hex', 'rgb', 'alpha', 'preview', 'swatches', 'footer'],
            showOn: 'focus alt click',
            buttonColorize: true,
            buttonImageOnly: true
        });
        $('#caption_col').colorpicker('setColor', '#333333');

        $("#error_message").dialog({
            modal: true,
            autoOpen: false,
            buttons: {
                Ok: function () {
                    $(this).dialog("close");
                }
            }
        });
        $("#r_error_message").dialog({
            modal: false,
            autoOpen: false,
            buttons: {
                Ok: function () {
                    $(this).dialog("close");
                }
            }
        });

        $("#upload_file_dialog").dialog({
            modal: true,
            autoOpen: false,
            buttons: {
                'OK': function () {
                    $("#formUpload").submit();
                },
                'Cancel': function () {
                    $(this).dialog('close')
                }
            }
        });
        $("#refresh_history").button({
            icons: {
                primary: "ui-icon-refresh"
            },
            text: false
        }).click(function () {
                    refreshHistory();
                });
        $.ajaxSetup({
            beforeSend: function () {
                $('#modal').css("display",'block');
                $('#fade').css("display",'block');
            },
            complete: function () {
                $('#modal').css("display",'none');
                $('#fade').css("display",'none');
            }
        });
        $("#download_plot").button({
            icons: {
                primary: "ui-icon-disk"
            },
            text: false
        }).click(function () {
                    downloadPlot();
                });

        $("#reset_formatting").button({
            icons: {
                primary: "ui-icon-arrowrefresh-1-w"
            },
            text: false
        }).click(function () {
                    resetFormatting();
                });

        $( "#plot_image" ).click(function() {
            viewImage(resultName.replace("plot_", ""));
        });
        var tabIndex = 0;
        if (initXML != null) {
            var template = initXML.find("plot").find("template")[0].innerHTML;
            if (template == "series_plot.R_tmpl") {
                currentTab = "Series";
            } else if (template == "box_plot.R_tmpl") {
                currentTab = "Box";
            } else if (template == "bar_plot.R_tmpl") {
                currentTab = "Bar";
            } else if (template == "rhist.R_tmpl") {
                currentTab = "Rhist";}
            else if (template == "phist.R_tmpl") {
                currentTab = "Phist";
            } else if (template == "roc.R_tmpl") {
                currentTab = "Roc";
            } else if (template == "rely.R_tmpl") {
                currentTab = "Rely";
            } else if (template == "ens_ss.R_tmpl") {
                currentTab = "Ens_ss";
            }

            var tabs = $("#plot_config").find("a");

            for (var i = 0; i < tabs.length; i++) {
                if (tabs[i].text == currentTab) {
                    tabIndex = i;
                    break;
                }
            }
        }
        $("#plot_config").tabs({
            active: tabIndex ,
            beforeActivate: function (event, ui) {
                var tabId = ui.oldTab.attr("aria-controls");
                $("#" + tabId).empty();
                cleanUp();
                currentTab = ui.newTab.text();
            },
            beforeLoad: function( event, ui ) {
                ui.jqXHR.error(function() {
                ui.panel.html(
                "Couldn't load this tab. We'll try to fix this as soon as possible. " +
                "If this wouldn't be a demo." );
            });
           }
        });

        if (initXML != null) {
            $('#plot_title').val($(initXML.find("plot").find("tmpl").find("title")).text());
            $('#x_label_title').val($(initXML.find("plot").find("tmpl").find("x_label")).text());
            $('#y1_label_title').val($(initXML.find("plot").find("tmpl").find("y1_label")).text());
            $('#y2_label_title').val($(initXML.find("plot").find("tmpl").find("y2_label")).text());
            $('#caption').val($(initXML.find("plot").find("tmpl").find("caption")).text());

            $("#event_equal").prop('checked', $(initXML.find("plot").find("event_equal")).text() == "true");
            $("#event_equal_m").prop('checked', $(initXML.find("plot").find("event_equal_m")).text() == "true");
            $("#vert_plot").prop('checked', $(initXML.find("plot").find("vert_plot")).text() == "true");
            $("#x_reverse").prop('checked', $(initXML.find("plot").find("x_reverse")).text() == "true");
            $("#num_stats").prop('checked', $(initXML.find("plot").find("num_stats")).text() == "true");
            $("#grid_on").prop('checked', $(initXML.find("plot").find("grid_on")).text() == "true");
            $("#sync_axes").prop('checked', $(initXML.find("plot").find("sync_axes")).text() == "true");
            $("#dump_points1").prop('checked', $(initXML.find("plot").find("dump_points1")).text() == "true");
            $("#dump_points2").prop('checked', $(initXML.find("plot").find("dump_points2")).text() == "true");
            $("#indy1_stag").prop('checked', $(initXML.find("plot").find("indy1_stag")).text() == "true");
            $("#indy2_stag").prop('checked', $(initXML.find("plot").find("indy2_stag")).text() == "true");
            $("#varianceInflationFactor").prop('checked', $(initXML.find("plot").find("varianceinflationfactor")).text() == "true");
            $("#ci_alpha").val($(initXML.find("plot").find("ci_alpha")).text());

            $("#plot_type").val($(initXML.find("plot").find("plot_type")).text());
            $("#plot_height").val($(initXML.find("plot").find("plot_height")).text());
            $("#plot_width").val($(initXML.find("plot").find("plot_width")).text());
            $("#plot_units").val($(initXML.find("plot").find("plot_units")).text());
            $("#cex").val($(initXML.find("plot").find("cex")).text());
            $("#plot_res").val($(initXML.find("plot").find("plot_res")).text());
            $("#mar").val($(initXML.find("plot").find("mar")).text());
            $("#mgp").val($(initXML.find("plot").find("mgp")).text());
            $("#title_align").val($(initXML.find("plot").find("title_align")).text());
            $("#title_offset").val($(initXML.find("plot").find("title_offset")).text());
            $("#title_size").val($(initXML.find("plot").find("title_size")).text());
            $("#title_weight").val($(initXML.find("plot").find("title_weight")).text());
            $("#grid_lty").val($(initXML.find("plot").find("grid_lty")).text());
            $("#grid_lwd").val($(initXML.find("plot").find("grid_lwd")).text());
            $('#grid_col').colorpicker('setColor', $(initXML.find("plot").find("grid_col")).text());
            $("#grid_x").val($(initXML.find("plot").find("grid_x")).text());
            $("#plot_cmd").val($(initXML.find("plot").find("plot_cmd")).text());

            $("#xlab_align").val($(initXML.find("plot").find("xlab_align")).text());
            $("#xlab_offset").val($(initXML.find("plot").find("xlab_offset")).text());
            $("#xlab_size").val($(initXML.find("plot").find("xlab_size")).text());
            $("#xlab_weight").val($(initXML.find("plot").find("xlab_weight")).text());
            $("#xtlab_horiz").val($(initXML.find("plot").find("xtlab_horiz")).text());
            $("#xtlab_perp").val($(initXML.find("plot").find("xtlab_perp")).text());
            $("#xtlab_size").val($(initXML.find("plot").find("xtlab_size")).text());
            $("#xtlab_freq").val($(initXML.find("plot").find("xtlab_freq")).text());
            $("#xtlab_orient").val($(initXML.find("plot").find("xtlab_orient")).text());

            $("#x2lab_align").val($(initXML.find("plot").find("x2lab_align")).text());
            $("#x2lab_offset").val($(initXML.find("plot").find("x2lab_offset")).text());
            $("#x2lab_size").val($(initXML.find("plot").find("x2lab_size")).text());
            $("#x2lab_weight").val($(initXML.find("plot").find("x2lab_weight")).text());
            $("#x2tlab_horiz").val($(initXML.find("plot").find("x2tlab_horiz")).text());
            $("#x2tlab_perp").val($(initXML.find("plot").find("x2tlab_perp")).text());
            $("#x2tlab_size").val($(initXML.find("plot").find("x2tlab_size")).text());
            $("#x2tlab_orient").val($(initXML.find("plot").find("x2tlab_orient")).text());


            $("#ylab_align").val($(initXML.find("plot").find("ylab_align")).text());
            $("#ylab_offset").val($(initXML.find("plot").find("ylab_offset")).text());
            $("#ylab_size").val($(initXML.find("plot").find("ylab_size")).text());
            $("#ylab_weight").val($(initXML.find("plot").find("ylab_weight")).text());
            $("#ytlab_horiz").val($(initXML.find("plot").find("ytlab_horiz")).text());
            $("#ytlab_perp").val($(initXML.find("plot").find("ytlab_perp")).text());
            $("#ytlab_size").val($(initXML.find("plot").find("ytlab_size")).text());
            $("#ytlab_orient").val($(initXML.find("plot").find("ytlab_orient")).text());
            $("#y1_lim").val($(initXML.find("plot").find("y1_lim")).text());
            $("#y1_bufr").val($(initXML.find("plot").find("y1_bufr")).text());


            $("#y2lab_align").val($(initXML.find("plot").find("y2lab_align")).text());
            $("#y2lab_offset").val($(initXML.find("plot").find("y2lab_offset")).text());
            $("#y2lab_size").val($(initXML.find("plot").find("y2lab_size")).text());
            $("#y2lab_weight").val($(initXML.find("plot").find("y2lab_weight")).text());
            $("#y2tlab_horiz").val($(initXML.find("plot").find("y2tlab_horiz")).text());
            $("#y2tlab_perp").val($(initXML.find("plot").find("y2tlab_perp")).text());
            $("#y2tlab_size").val($(initXML.find("plot").find("y2tlab_size")).text());
            $("#y2tlab_orient").val($(initXML.find("plot").find("y2tlab_orient")).text());
            $("#y2_lim").val($(initXML.find("plot").find("y1_lim")).text());
            $("#y2_bufr").val($(initXML.find("plot").find("y1_bufr")).text());

            $("#legend_size").val($(initXML.find("plot").find("legend_size")).text());
            $("#legend_inset").val($(initXML.find("plot").find("legend_inset")).text());
            $("#legend_box").val($(initXML.find("plot").find("legend_box")).text());
            $("#legend_ncol").val($(initXML.find("plot").find("legend_ncol")).text());
            $("#caption_align").val($(initXML.find("plot").find("caption_align")).text());
            $("#caption_offset").val($(initXML.find("plot").find("caption_offset")).text());
            $("#caption_size").val($(initXML.find("plot").find("caption_size")).text());
            $("#caption_weight").val($(initXML.find("plot").find("caption_weight")).text());
            var caption_col = $(initXML.find("plot").find("caption_col")).text();
            if(caption_col.length > 7) {
                caption_col = caption_col.substring(0,7);
            }
            $('#caption_col').colorpicker('setColor', caption_col);


        }else{
            resetFormatting();
        }
    }


    function updateResult(result) {
        resultName = result;

        $('#plot_display_inner_header').text(resultName.replace("plot_", ""));
        $( "#plot_image" )
        .error(function() {
                    $( this).attr( "src", 'images/no_image.png' );
        })
        .attr( "src", 'plots/' + resultName + '.png' );
        $.ajax({
            type: "GET",
            url: "xml/" + resultName + ".xml",
            dataType: "xml",
            success: function (data) {
                var xmlDoc = $.parseXML(data);
                var xmlString;
                if (jQuery.browser == "msie") {
                    xmlString = data.xml;
                }
                else {
                    xmlString = (new XMLSerializer()).serializeToString(data);
                }
                var xml_formatted = formatXml(xmlString).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;').replace(/\n/g, '<br />');
                document.getElementById('plot_xml').innerHTML = xml_formatted;
            },
            error: function () {
                document.getElementById('plot_xml').innerHTML = "";
            }
        });
        $.ajax({
            type: "GET",
            url: "xml/" + resultName + ".sql",
            dataType: "text",
            success: function (data) {

                document.getElementById('plot_sql').innerHTML = data.replace(/\n/g, '<br />');
            },
            error: function () {
                document.getElementById('plot_sql').innerHTML = "";
            }
        });
        $.ajax({
            type: "GET",
            url: "R_work/scripts/" + resultName + ".R",
            dataType: "text",
            success: function (data) {

                document.getElementById('r_script').innerHTML = data.replace(/\n/g, '<br />');
            },
            error: function () {
                document.getElementById('r_script').innerHTML = "";
            }
        });
        $.ajax({
            type: "GET",
            url: "xml/" + resultName + ".log",
            dataType: "text",
            success: function (data) {

                document.getElementById('plot_log').innerHTML = data.replace(/\n/g, '<br />');
            },
            error: function () {
                document.getElementById('plot_log').innerHTML = "";
            }
        });

        /*$.ajax({
            type: "GET",
            url: "R_work/data/" + resultName + ".data",
            dataType: "text",
            success: function (data) {

                document.getElementById('r_data').innerHTML = data.replace(/\n/g, '<br />');
            },
            error: function () {
                document.getElementById('r_data').innerHTML = "";
            }
        });*/
        $('#ui-tabs-1').empty();
        $("#r_data_url").attr("href", "R_work/data/" + resultName + ".data");
        $('#plot_display_inner').tabs({ active: 0 });

       // $('#plot_display_inner').tabs('enable', 1).tabs('enable', 2).tabs('enable', 3).tabs('enable', 4).tabs('enable', 5).tabs('refresh');
    }

    function querySt(Key) {
        var url = window.location.href;
        var KeysValues = url.split(/[\?&]+/);
        for (var i = 0; i < KeysValues.length; i++) {
            var KeyValue = KeysValues[i].split("=");
            if (KeyValue[0] == Key) {
                return KeyValue[1];
            }
        }
    }
    function downloadPlot(e){

        var inputs='';
        inputs+='<input type="hidden" name="plot" value="'+ resultName +'" />';
        inputs+='<input type="hidden" name="type" value="'+ currentPlotTab +'" />';
        $('<form action="download" method="'+ 'get' +'">'+inputs+'</form>').appendTo('body').submit().remove();
    }



    function viewImage(id){
        boxID = boxID + 1;
        center = center + 20;
        top = top + 20;
        $('<div>').dialog({
            height: 'auto',
            width: 'auto',
            autoOpen:false,
            resizable: true,
            closeOnEscape: true,
            focus: true,
            dialogClass: "dialog-box-"+boxID,
            title: id,
            position: { my: ("center+"+ center + " top+" + top), at: "center top" , of:window},
            close: function (e) {
                $(this).empty();
                $(this).dialog('destroy');
                center = center - 20;
                top = top - 20;
            }

        }).html('<img src="plots/plot_' + id + '.png" onError="this.src=\'images/no_image.png\';"/>').dialog("open");

    }

    function colorDisplayFmatter(cellvalue, options, rowObject) {
      return '<input id="color_' + rowObject.id + '" type="text" value="' + rowObject.color + '" size="8" style="background-color:' + rowObject.color + ';">';
    }
    function colorDisplayUnmatter(cellvalue, options, cell) {
        var colorRGB = jQuery('input', cell).css('background-color');
        var parts = colorRGB.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
        delete(parts[0]);
        for (var i = 1; i <= 3; ++i) {
            parts[i] = parseInt(parts[i]).toString(16);
            if (parts[i].length == 1) parts[i] = '0' + parts[i];
        }

        return   parts.join('');
    }
    function myelem(value, options) {
        var el = document.createElement("input");
        el.type = "text";
        el.value = value;
        el.className = "cp-basic";
        el.style.width = "60px";
        $(el).colorpicker({
                                parts: ['header', 'map', 'bar', 'hex', 'rgb', 'alpha', 'preview', 'swatches', 'footer'],
                                showOn: 'both',
                                buttonColorize: true,
                                buttonImageOnly: true
                            });
        return el;
    }

    function myvalue(elem, operation, value) {
        if (operation === 'get') {
            return $(elem).val(); // change this to get value from color picker
        } else if (operation === 'set') {
            $('input', elem).val(value); // change this to set value of color picker
        }
    }

    if (typeof String.prototype.startsWith != 'function') {
      // see below for better implementation!
      String.prototype.startsWith = function (str){
        return this.indexOf(str) == 0;
      };
    }



</script>
</HEAD>
<BODY>

<div id="header">
    <div class="toolbar ui-widget" id="toolbar ">

      <label for="database">Database:</label><select id="database">
        </select>
            <span style="margin-left:20px;"><button id="generate_plot">Generate Plot </button></span>
        <button id="load_xml" style="float: right">Load XML</button>
               <button id="reload_databases" style="float: right">Reload databases</button>
    </div>


</div>
<div id="series_formatting" class="ui-layout-south " style="overflow: visible; padding-bottom: 10px; padding-left: 10px; padding-top: 0; padding-right: 0" >
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
            <li><a href="plotJSP/rhist.jsp">Rhist</a></li>
            <li><a href="plotJSP/phist.jsp">Phist</a></li>
            <li><a href="plotJSP/roc.jsp">Roc</a></li>
            <li><a href="plotJSP/rely.jsp">Rely</a></li>
            <li><a href="plotJSP/ens_ssl.jsp">Ens_ss</a></li>
        </ul>



</div>




<div id="plot_display" class="no-padding">


<div id="plot_display_inner" class="ui-layout-center no-padding" >
    <div class="ui-widget-header ui-widget add-font-size " style="position: relative;padding-left: 20px; padding-top: 5px;padding-bottom: 5px;" id="plot_display_inner_header">N/A </div>

    <button id="download_plot" style="top: 4px; float: right; right: 3px;">Download</button>


    <ul style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
        <li><a href="#plot_image">Plot</a></li>
        <li><a href="#plot_xml">XML</a></li>
        <li><a href="#plot_log">Log</a></li>
        <li><a href="#r_script">R script</a></li>
        <li><a id="r_data_url" href="R_work/data/.data">R data</a></li>
        <li><a href="#plot_sql">SQL</a></li>
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
            <td><input type="checkbox" id="event_equal"/>
                <label for="event_equal">Event Equalizer</label></td>
            <td><input type="checkbox" id="event_equal_m"/>
                <label for="event_equal_m">Event Equalizer
                    Multi</label></td>
        </tr>
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
                            <td><label for="mar">Margins</label></td>
                            <td ><input id="mar" type="text" value="c(8, 4, 5, 4)" size="11"></td>
                            <td><label for="mgp">Axis Margin Line</label></td>
                            <td ><input type="text" size="11" id="mgp" value="c(1, 1, 0)"></td>
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
                        <td><label for="y1_lim" style="margin-right: 5px;">Limits</label><input type="text"size="12" id="y1_lim"></td>
                        <td><label for="y1_bufr" style="margin-right: 5px;">Top and bottom buffer</label><input type="text" size="12" id="y1_bufr">
                    </tr></table>
                </fieldset></td>
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
                            <td><label for="ytlab_horiz">Horizontal align</label></td>
                            <td><input type="text" size="23" id="ytlab_horiz" value="0.5"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_perp">Perpendicular offset</label></td>
                            <td><input type="text" size="23" id="ytlab_perp" value=".5"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_size">Text size</label></td>
                            <td><input type="text" size="23" id="ytlab_size" value="1"></td>
                        </tr>
                        <tr>
                            <td><label for="ytlab_orient">Orientation</label></td>
                            <td><select id="ytlab_orient">
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
<div id="y2">
    <table>
        <tr>
                   <td colspan="2">
                       <fieldset>
                           <legend>Y2 Bounds</legend>
                           <table style="width:100%"><tr>
                               <td><label for="y2_lim" style="margin-right: 5px;">Limits</label><input type="text"size="12" id="y2_lim"></td>
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
                                  <td><label for="legend_inset">Box Position</label></td>
                                  <td><input type="text" size="10" id="legend_inset" value="c(0, -.25)"></td>
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
<div id="unavailableDiffCurveDialogForm" title="Add Series Difference Curve">
    <div style="margin-top: 20px; padding: 0 .7em;" class="ui-state-highlight ui-corner-all">
    		<p><span style="float: left; margin-right: .3em;" class="ui-icon ui-icon-info"></span>
                Series Difference Curves are not supported for this plot type.</p>
    	</div>
</div>

<div id="incorrectDiffCurveDialogForm" title="Add Series Difference Curve">
    <div style="margin-top: 20px; padding: 0 .7em;" class="ui-state-highlight ui-corner-all">
    		<p><span style="float: left; margin-right: .3em;" class="ui-icon ui-icon-info"></span>
                It should be more than one series to create a difference curve.</p>
    	</div>
</div>
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
                                     onchange="populateSecondSelect(1,series1Names); createNewDiffSeriesName(1);"></select>
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
                                     onchange="populateSecondSelect(2,series2Names); createNewDiffSeriesName(2)">
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
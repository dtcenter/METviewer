<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<HEAD>
    <META http-equiv="content-type" content="text/html; charset=utf-8">
    <style type="text/css">
        .add-font-size {
            font-size: 10px;
        }
        legend {
            font-weight: bold;;
        }
        fieldset{
            margin-top: 5px;
            margin-left: 5px;
            margin-right: 5px;
        }
    </style>
    <script type="text/javascript">


        $(document).ready(function () {
            $('.help-button').button({
                icons: {
                    primary: "ui-icon-help"
                },
                text: false

            }).click(function () {
                        $('#helpContent').empty();
                        $("#helpContent").append($("<iframe id='helpContentFrame'/>").css("width", "100%").css("height", "100%").attr("src", "doc/plot.html#" + $(this).attr("alt")));
                        $('#helpContent').dialog('open');
                    });

            $("#helpContent").append($("<iframe id='helpContentFrame'/>").css("width", "100%").css("height", "100%")).dialog({
                height: 400,
                width: 600,
                autoOpen: false
            });

            $("#tabs_axis_variables").tabs({
                heightStyle: "content"
            });

            $('#add_fcst_var_y1').button({
                        icons: {
                            primary: "ui-icon-circle-plus"
                        }
                    }
            ).click(function () {
                        addFcstVariableSeries("y1");
                    });
            $('#add_fcst_var_y2').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                        addFcstVariableSeries("y2");
                    });
            $('#add_fixed_var').button({
                icons: {
                           primary: "ui-icon-circle-plus"
                       }
            }).click(function () {
                addFixedVariableSeries();
            });



            $("#plot_data").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                click: function (event, ui) {
                    updateForecastVariables();
                    if (ui.value == 'stat') {
                        updateStats("y1", 1, []);
                        updateStats("y2", 1, []);
                        updateFixVarSeries("stat");

                    } else {
                        updateMode("y1", 1, []);
                        updateMode("y2", 1, []);
                        updateFixVarSeries("mode");
                    }
                    updateSeriesVarValSeries("y1", 1, []);
                    updateSeriesVarValSeries("y2", 1, []);
                }
            });

            $("#plot_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto'

            });


            $("#fcst_var_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header:false,
                minWidth:'auto',
                height:'auto',
                click: function(){
                    var id_array = this.id.split("_");
                    updateStats(id_array[id_array.length - 2], id_array[id_array.length - 1], []);
                    var selectedSeriesVarVal = $("#series_var_val_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("getChecked").val();
                    if (selectedSeriesVarVal == null) {
                        selectedSeriesVarVal = [];
                    }
                    updateSeriesVarValSeries(id_array[id_array.length - 2], id_array[id_array.length - 1],selectedSeriesVarVal);
                  }
            });


            $("#fcst_stat_y1_1").multiselect({
                selectedList: 100, // 0-based index
                noneSelectedText: "Select attribute stat",
                click: function (event, ui) {
                    var id_array = this.id.split("_");
                    $("#fcst_stat_mode_config_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).css("display", "none");
                    try{
                    $("#fcst_stat_mode_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("option", "allUnselected", true);
                    } catch (err){}
                    updateSeriesSeriesBox();
                },

                                                    position: {
                                                        my: 'right center' ,
                                                              at: 'right center'

                                                       }

            });
            $("#series_var_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                click: function () {
                    var id_array = this.id.split("_");
                    updateSeriesVarValSeries(id_array[id_array.length - 2], id_array[id_array.length - 1],[]);
                }
            });
            $("#indy_var").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 300,
                click: function () {
                    $("#indy_var_val").multiselect("uncheckAll");
                },
                position: {
                    my: 'left bottom',
                    at: 'left top'
                }

            });
            $("#series_var_val_y1_1").multiselect({
               selectedList: 100, // 0-based index
               noneSelectedText: "Select value",
                click: function(){
                    updateSeriesSeriesBox();
                }
            });
            $("#indy_var_val").multiselect({
                selectedList: 100, // 0-based index
                noneSelectedText: "Select value",
                addLabel: true,
                minWidth: 300,
                height: 300,
                beforeopen: function (event, ui) {
                    var values = $('#indy_var_val').val();
                    if (values == null) {
                        values = [];
                    }
                    populateIndyVarVal(values);
                },
                position: {
                    my: 'left bottom',
                    at: 'left top'
                }
            });


            $('#add_series_var_y1').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                        addSeriesVariableSeriesBox("y1");
                    });
            $('#add_series_var_y2').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                        addSeriesVariableSeriesBox("y2");
                    });


            $('#statistics p').hide();

            $(' input[name="statistics"]').click(function () {
                $('#statistics p').hide();
                $(this).prop("checked", true);
                $('#' + $(this).val()).show();
            });
            $('#radio').buttonset();

            $(".remove_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false,
                disabled: true
            }).click(function () {
                        if ($(this).attr('id').startsWith('remove_series_var')) {
                            removeSeriesVarSeriesBox($(this).attr('id'));
                        } else if ($(this).attr('id').startsWith('remove_fcst_var')) {
                            removeFcstVarSeriesBox($(this).attr('id'));
                        }
                    });
            $(".remove_fixed_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false
            }).click(function () {
                        removeFixedVarSeries($(this).attr('id'));
                    });


            if (initXML != null) {
                loadXMLSeries();

            }else{
                updateForecastVariables();
                updateStats("y1", 1, []);
                updateStats("y2", 1, []);
                updateSeriesVarValSeries("y1", 1, []);
                updateSeriesVarValSeries("y2", 1, []);
                $.each(fix_var_value_to_title_stat_map, function (key, val) {
                    $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
                });
            }


        });


</script>
</head>
<body>
<div style="overflow: auto;">
<div style="width: 100%; text-align: center;">
    <span style="margin-right:20px;"><label for="plot_data">Plot Data:</label><select id="plot_data">
    <option selected="selected" value="stat">Stat</option>
    <option value="mode">MODE</option>
</select></span>
    <span style="margin-left:20px; margin-top: 5px;"><label for="plot_stat">Plot statistic:</label><select id="plot_stat" name="plot_stat">
                       <option selected="selected" value="median">Median</option>
                       <option value="mean">Mean</option>
                   </select></span>
</div>
<div id="tabs_axis_variables" class="ui-layout-center no-padding" style="border:none;">
    <ul class="allow-overflow " style="background:none;border-left: medium none; border-right: medium none; border-top: medium none;">
                <li><a href="#y1_axis_variables">Y1 Axis variables</a></li>
                <li><a href="#y2_axis_variables">Y2 Axis variables</a></li>
            </ul>
    <div id="y1_axis_variables" class="no-padding">
        <div class="ui-widget-content ui-widget-content-plot ui-corner-all" style=" border-top:none;" >
            <div class="ui-widget-header-plot">Y1 Dependent (Forecast) Variables:<button class="help-button" style="float: right;" alt="dep">Help</button></div>
            <table id="dependent_var_table_y1">

                <tr>
                    <td>
                        <button id="remove_fcst_var_y1_1"
                                class="remove_var">Remove
                        </button>
                    </td>
                    <td>
                        <select id="fcst_var_y1_1">

                        </select>
                    </td>
                    <td>
                        <select name="fcst_stat_y1_1" multiple="multiple"
                                id="fcst_stat_y1_1"></select><br/>
                        <select name="fcst_stat_mode_y1_1"
                                id="fcst_stat_mode_y1_1" style="display: none">
                        </select>
                    </td>
                    <td id ="fcst_stat_mode_config_y1_1" style="display: none;">
                        <table style="white-space:nowrap;" ><tr>
                            <td rowspan="2"><input type="checkbox"  class="non-acov" name = "mode_stat_diff" value = "D" onclick="updateSeriesSeriesBox();"><label >Diff</label></td>
                            <td><input type="checkbox"  checked  name = "mode_stat_fcst" value = "F" onclick="updateSeriesSeriesBox();"><label >Fcst</label></td>
                            <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_simple" value = "S" onclick="updateSeriesSeriesBox();"><label >Simple</label></td>
                            <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_matched" value = "M" onclick="updateSeriesSeriesBox();"><label >Matched</label></td>
                        </tr>
                            <tr>
                                <td><input type="checkbox"  checked  name = "mode_stat_obs" value = "O" onclick="updateSeriesSeriesBox();"><label >Obs</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_cluster" value = "C" onclick="updateSeriesSeriesBox();"><label >Cluster</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_unmatched" value = "U" onclick="updateSeriesSeriesBox();"><label >Unmatched</label></td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>

            <button id="add_fcst_var_y1" style="margin-top:5px;">Variable</button>
            <div style="margin-top:5px;margin-bottom:5px;"></div>

            <div class="ui-widget-header-plot">Y1 Series Variables:<button class="help-button" style="float: right;" alt="series">Help</button></div>
            <table id='series_var_table_y1'>
                <tr>
                    <td>
                        <button id="remove_series_var_y1_1" class="remove_var">
                            Remove
                        </button>
                    </td>

                    <td>
                        <select id="series_var_y1_1">
                            <option value="model" >MODEL</option>
                            <option value="fcst_lead">FCST_LEAD</option>
                            <option value="fcst_valid_beg">FCST_VALID_BEG</option>
                            <option value="valid_hour">VALID_HOUR</option>
                            <option value="fcst_init_beg">FCST_INIT_BEG</option>
                            <option value="init_hour">INIT_HOUR</option>
                            <option value="fcst_lev">FCST_LEV</option>
                            <option value="obtype">OBTYPE</option>
                            <option value="vx_mask">VX_MASK</option>
                            <option value="interp_mthd">INTERP_MTHD</option>
                            <option value="interp_pnts">INTERP_PNTS</option>
                            <option value="fcst_thresh">FCST_THRESH</option>
                        </select>
                    </td>
                    <td>
                        <select  multiple="multiple" id="series_var_val_y1_1" >

                                    </select>

                    </td>
                    <td><input type="checkbox" id="group_series_var_y1_1" onclick="updateSeriesSeriesBox();"><label for="group_series_var_y1_1">Group_y1_1</label></td>
                </tr>
            </table>
            <button id="add_series_var_y1" style="margin-top:5px;">Series Variable</button>
        </div>
    </div>
    <div id="y2_axis_variables" class="no-padding">
        <div class="ui-widget-content ui-widget-content-plot ui-corner-all" >
            <div class="ui-widget-header-plot">Y2 Dependent (Forecast) Variables:<button class="help-button" style="float: right;" alt="dep">Help</button></div>

            <table id="dependent_var_table_y2" style="display: none;">

                <tr>
                    <td>
                        <button id="remove_fcst_var_y2_1"
                                >Remove
                        </button>
                    </td>
                    <td>
                        <select id="fcst_var_y2_1">

                        </select>
                    </td>
                    <td>
                        <select name="fcst_stat_y2_1" multiple="multiple"
                                id="fcst_stat_y2_1">
                        </select><br/>
                        <select name="fcst_stat_mode_y2_1" id="fcst_stat_mode_y2_1" style="display: none">
                                                </select>
                    </td>
                    <td id ="fcst_stat_mode_config_y2_1" style="display: none;">
                        <table style="white-space:nowrap;">
                            <tr>
                                <td rowspan="2"><input type="checkbox"  class="non-acov"  name = "mode_stat_diff" value = "D" onclick="updateSeriesSeriesBox();"><label >Diff</label></td>
                                <td><input type="checkbox"  checked  name = "mode_stat_fcst" value = "F" onclick="updateSeriesSeriesBox();"><label >Fcst</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_simple" value = "S" onclick="updateSeriesSeriesBox();"><label >Simple</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_matched" value = "M" onclick="updateSeriesSeriesBox();"><label >Matched</label></td>
                            </tr>
                            <tr>
                                <td><input type="checkbox"  checked  name = "mode_stat_obs" value = "O" onclick="updateSeriesSeriesBox();"><label >Obs</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_cluster" value = "C" onclick="updateSeriesSeriesBox();"><label >Cluster</label></td>
                                <td><input type="checkbox"  class="non-acov" checked  name = "mode_stat_unmatched" value = "U" onclick="updateSeriesSeriesBox();"><label >Unmatched</label></td>
                            </tr>
                         </table>
                    </td>
                </tr>
            </table>

            <button id="add_fcst_var_y2" style="margin-top:5px;">Variable</button>
            <div style="margin-top:5px;margin-bottom:5px;"></div>

            <div class="ui-widget-header-plot">Y2 Series Variables:<button class="help-button" style="float: right;" alt="series">Help</button></div>

            <table id='series_var_table_y2' style="display: none;">
                <tr>
                    <td>
                        <button id="remove_series_var_y2_1" >
                            Remove
                        </button>
                    </td>
                    <td>
                        <select id="series_var_y2_1">
                            <option value="model">MODEL</option>
                            <option value="fcst_lead">FCST_LEAD</option>
                            <option value="fcst_valid_beg">FCST_VALID_BEG</option>
                            <option value="valid_hour">VALID_HOUR</option>
                            <option value="fcst_init_beg">FCST_INIT_BEG</option>
                            <option value="init_hour">INIT_HOUR</option>
                            <option value="fcst_lev">FCST_LEV</option>
                            <option value="obtype">OBTYPE</option>
                            <option value="vx_mask">VX_MASK</option>
                            <option value="interp_mthd">INTERP_MTHD</option>
                            <option value="interp_pnts">INTERP_PNTS</option>
                            <option value="fcst_thresh">FCST_THRESH</option>
                        </select>
                    </td>
                    <td>
                        <select multiple="multiple" id="series_var_val_y2_1">

                        </select>
                    </td>
                    <td><input type="checkbox" id="group_series_var_y2_1" onclick="updateSeriesSeriesBox();"><label for="group_series_var_y2_1">Group_y2_1</label></td>
                </tr>
            </table>
            <button id="add_series_var_y2" style="margin-top:5px;">Series Variable</button>
        </div>
        </div>
</div>


<div class="ui-widget-content ui-widget-content-plot ui-corner-all"  >
    <div class="ui-widget-header-plot">Fixed Values:<button class="help-button" style="float: right;" alt="plot_fix">Help</button></div>

    <table id='fixed_var_table' style="display: none;">
        <tr>
                            <td>
                                <button id="remove_fixed_var_1" class="remove_fixed_var">
                                    Remove
                                </button>
                            </td>

                            <td>
                                <select id="fixed_var_1">

                                </select>
                            </td>
                            <td>
                                <select  multiple="multiple" id="fixed_var_val_1" ></select>

                            </td>

                        </tr>
        </table>
        <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
    <br/>
    <br/>
    <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""  id="txtPlotCond" style="width: 95%">
</div>


<div class="ui-widget-content ui-widget-content-plot ui-corner-all"  >
    <div class="ui-widget-header-plot">Independent Variables:<button class="help-button" style="float: right;" alt="indep">Help</button></div>

    <table id='indy_var_table'>
            <tr>
                <td>
                    <select id="indy_var">

                        <option value="fcst_lead">FCST_LEAD</option>
                        <option value="model">MODEL</option>
                                                                                        <option value="fcst_valid_beg">FCST_VALID_BEG</option>
                                                                                        <option value="valid_hour">VALID_HOUR</option>
                                                                                        <option value="fcst_init_beg">FCST_INIT_BEG</option>
                                                                                        <option value="init_hour">INIT_HOUR</option>
                                                                                        <option value="fcst_lev">FCST_LEV</option>
                                                                                        <option value="obtype">OBTYPE</option>
                                                                                        <option value="vx_mask">VX_MASK</option>
                                                                                        <option value="interp_mthd">INTERP_MTHD</option>
                                                                                        <option value="interp_pnts">INTERP_PNTS</option>
                                                                                        <option value="fcst_thresh">FCST_THRESH</option>
                                                                                        <option value="obs_thresh">OBS_THRESH</option>
                    </select>
                </td>
                <td>
                    <select id='indy_var_val' multiple="multiple">

                    </select>

                </td>

            </tr>
        </table>
    </div>

<div class="ui-widget-content ui-widget-content-plot ui-corner-all"  >
    <div class="ui-widget-header-plot">Statistics:</div>

<div id="statistics">
    <div id="radio">
    <input type="radio" name="statistics" value="none"
           id="none_statistics_label" checked/>
    <label for="none_statistics_label">None</label>
    <input type="radio" name="statistics" value="aggregation_statistics"
           id="aggregation_statistics_label"/>
    <label for="aggregation_statistics_label">Aggregation statistics</label>

    <input type="radio" name="statistics" value="calculations_statistics"
           id="calculations_statistics_label"/>
    <label for="calculations_statistics_label"> Statistics Calculations</label>
</div>
    <p id="aggregation_statistics"> <button class="help-button" style="float: right;bottom: 40px;" alt="agg_stat">Help</button>
    <table style="width:100%">
    <tr>
        <td><input type="radio" value="ctc" name="agg_stat" id="agg_ctc"></td>
        <td><label for="agg_ctc">Contingency table count (CTC)</label></td>

        <td style="text-align:right;"><input type="text" value="1" size="4" id="boot_repl"></td>
        <td><label for="boot_repl">Bootstrapping replications</label></td>
    </tr>
    <tr>
        <td><input type="radio" value="sl1l2" name="agg_stat" id="agg_sl1l2"></td>
        <td><label for="agg_sl1l2">Scalar partial sums (SL1L2)</label></td>

        <td style="text-align:right;"><select id="boot_ci">
                        <option selected value="perc">perc</option>
                        <option value="norm">norm</option>
                        <option value="basic">basic</option>
                        <option value="stud">stud</option>
                        <option value="bca">bca</option>
                        <option value="all">all</option>
                    </select></td>
        <td><label for="boot_ci">Confidence Interval</label></td>
    </tr>
    <tr>
        <td><input type="radio" value="pct" name="agg_stat" id="agg_pct"></td>
        <td><label for="agg_pct">Probability methods output (PCT)</label></td>

        <td style="text-align:right;"><input id="selEveqDis" type="checkbox"></td>
        <td><label title="Disables automatic event equalization that occurs when bootstrap confidence intervals are requested" for="selEveqDis">Event equalization</label></td>
    </tr>
    <tr>
        <td><input type="radio" value="nbrcnt" name="agg_stat" id="agg_nbrcnt"></td>
        <td><label for="agg_nbrcnt">Nbrhood method count (NBR_CNT)</label></td>

        <td style="text-align:right;"><input id="cacheAggStat" type="checkbox"></td>
        <td><label for="cacheAggStat">Cache aggregation statistics</label></td>
    </tr>


    </table>



    </p>

    <p id="calculations_statistics"><button class="help-button" style="float: right;bottom: 40px;" alt="calc_stat">Help</button>
    <table>
        <tr>
            <td><input type="radio" value="ctc" name="calc_stat" id="calc_ctc"></td>
            <td><label for="calc_ctc">Contingency table count (CTC)</label></td>
        </tr>
    <tr>
            <td><input type="radio" value="sl1l2" name="calc_stat" id="calc_sl1l2"></td>
            <td><label for="calc_sl1l2">Scalar partial sums (SL1L2)</label></td>
    </tr>
    </table>

    </p>

    <p id="none"></p>
</div>
</div>
<div id="helpContent" title="Help">
</div>
</div>
</body>
</html>
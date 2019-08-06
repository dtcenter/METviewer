<!DOCTYPE html>

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

        fieldset {
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
                $('#helpContent').dialog({
                    buttons: {
                        "Open in new window": function () {
                            var win = window.open('doc/plot.html');
                            if (win) {
                                //Browser has allowed it to be opened
                                win.focus();
                            } else {
                                //Broswer has blocked it
                                alert('Please allow popups for this site');
                            }
                        },
                        Cancel: function () {
                            $(this).dialog("close");
                        }
                    }
                });
                $('#helpContent').dialog('open');
            });

            $("#helpContent").append($("<iframe id='helpContentFrame'/>").css("width", "100%").css("height", "100%")).dialog({
                height: 400,
                width: 600,
                autoOpen: false
            });


            $('#add_fixed_var').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                addFixedVar();
            });


            $("#plot_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto'

            });


            $("#series_var_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                click: function (event, ui) {
                    $('#series_var_val_y1_date_period_start_1').empty();
                    $('#series_var_val_y1_date_period_end_1').empty();

                    if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                        $("#series_var_val_y1_date_period_button_1").css("display", "block");
                    } else {
                        $("#series_var_val_y1_date_period_button_1").css("display", "none");
                    }
                    var id_array = this.id.split("_");
                    updateSeriesVarVal(id_array[id_array.length - 2], id_array[id_array.length - 1], []);
                }

            });
            $("#series_var_val_y1_date_period_button_1").button({
                icons: {
                    primary: "ui-icon-check",
                    secondary: "ui-icon-circlesmall-plus"
                },
                text: false
            }).click(function () {
                $("#series_var_val_y1_date_period_1").dialog("open");
            });
            createValDatePeriodDialog('series_var_val_y1', 1);


            $("#indy_var").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 300,
                click: function (event, ui) {
                    $('#date_period_start').empty();
                    $('#date_period_end').empty();

                    if (ui.value === "fcst_init_beg" || ui.value === "fcst_valid_beg" || ui.value === "fcst_valid" || ui.value === "fcst_init") {
                        $("#date_period_button").css("display", "block");
                        $("#date_range_button").css("display", "block");
                    } else {
                        $("#date_period_button").css("display", "none");
                        $("#date_range_button").css("display", "none");
                    }
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
                click: function () {
                    updateSeriesPerf();
                },
                checkAll: function () {
                    updateSeriesPerf(true);
                },
                uncheckAll: function () {
                    updateSeries();
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
                    my: 'center center',
                    at: 'right center'
                }
            });

            $("#date_period_button").button({
                icons: {
                    primary: "ui-icon-check",
                    secondary: "ui-icon-circlesmall-plus"
                },
                text: false
            }).click(function () {
                $("#date_period_dialog").dialog("open");
            });


            createDatePeriodDialog();

            $('#add_series_var_y1').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                addSeriesVarPerf();
            });


            $('#aggregation_statistics ').hide();
            $('#calculations_statistics ').hide();

            $("#calc_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                click: function (event, ui) {
                    if (ui.value !== "none") {
                        $("#agg_stat").val("none");
                        $("#agg_stat").multiselect("refresh");
                    }
                }
            });
            $("#agg_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                click: function (event, ui) {
                    if (ui.value !== "none") {
                        $("#calc_stat").val("none");
                        $("#calc_stat").multiselect("refresh");
                    }
                }
            });


            $(' input[name="statistics"]').click(function () {
                $('#aggregation_statistics ').hide();
                $('#calculations_statistics ').hide();
                $(this).prop("checked", true);
                $('#' + $(this).val()).show();
            });
            //$('#radio').buttonset();
            $('#calculations_statistics').show();


            $(".remove_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false,
                disabled: true
            }).click(function () {
                if ($(this).attr('id').startsWith('remove_series_var')) {
                    removeSeriesVar($(this).attr('id'));
                } else if ($(this).attr('id').startsWith('remove_fcst_var')) {
                    removeFcstVar($(this).attr('id'));
                }
            });
            $(".remove_fixed_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false
            }).click(function () {
                removeFixedVar($(this).attr('id'));
            });

            if (initXML != null) {
                var sd = initXML.find("database").text();
                var selectedDatabase = sd.split(",");
                for (var i = 0; i < selectedDatabase.length; i++) {
                    $("input[name='multiselect_database'][value='" + selectedDatabase[i] + "']")
                        .prop("checked", true).change();
                }
                loadXMLSeries();
                initXML = null;
            } else {
                updateSeriesVarVal("y1", 1, []);
            }
        });
        $("#date_range_button").button({
            icons: {
                primary: "ui-icon-calendar"
            },
            text: false
        }).click(function (evt) {
            evt.stopPropagation();
            var values = $('#indy_var_val').val();
            if (values == null) {
                values = [];
            }
            populateIndyVarVal(values);
            var dates = $(previousIndVarValResponse).find("val");
            var start = $(dates[0]).text();
            var end = $(dates[dates.length - 1]).text();
            try {
                $("#date_range").unbind("datepicker-apply");
                $('#date_range').data('dateRangePicker').clear();
                $('#date_range').data('dateRangePicker').destroy();
            } catch (error) {
                console.log(error);
            }
            $('#date_range').dateRangePicker({
                startOfWeek: 'sunday',
                separator: ' - ',
                format: 'YYYY-MM-DD HH:mm',
                autoClose: false,
                time: {
                    enabled: true
                },
                startDate: moment(start, 'YYYY-MM-DD HH:mm:ss'),
                endDate: moment(end, 'YYYY-MM-DD HH:mm:ss'),
                showShortcuts: true,
                shortcuts: {
                    'prev-days': [3, 7, 30],
                    'prev': ['week', 'month', 'year'],
                    'next-days': null,
                    'next': null
                },
                monthSelect: true,
                yearSelect: true,
                container: document.getElementById('indy_var_table').parentElement,
                customTopBar: function () {
                    return createCalendarTopBarNoFormat();
                }
            }).bind('datepicker-apply', function (event, obj) {
                onIndyCalendarClose(obj);
            });
            $('#date_range').data('dateRangePicker').open();
        });


    </script>
</head>
<body>
<div style="overflow: auto;">


    <div id="y1_axis_variables" class="no-padding">
        <div class="ui-widget-content ui-widget-content-plot ui-corner-all">


            <div class="ui-widget-header-plot">Y1 Series Variables:
                <button class="help-button" style="float: right;"
                        alt="series">Help
                </button>
            </div>
            <table id='series_var_table_y1'>
                <tr>
                    <td>
                        <button id="remove_series_var_y1_1"
                                class="remove_var">
                            Remove
                        </button>
                    </td>

                    <td>
                        <select id="series_var_y1_1">
                            <option value="model">MODEL</option>
                            <option value="fcst_lead">FCST_LEAD</option>
                            <option value="fcst_valid_beg">FCST_VALID_BEG
                            </option>
                            <option value="valid_hour">VALID_HOUR</option>
                            <option value="fcst_init_beg">FCST_INIT_BEG
                            </option>
                            <option value="init_hour">INIT_HOUR</option>
                            <option value="fcst_lev">FCST_LEV</option>
                            <option value="obtype">OBTYPE</option>
                            <option value="vx_mask">VX_MASK</option>
                            <option value="interp_mthd">INTERP_MTHD
                            </option>
                            <option value="interp_pnts">INTERP_PNTS
                            </option>
                            <option value="fcst_thresh">FCST_THRESH
                            </option>
                        </select>
                    </td>
                    <td>
                        <select multiple="multiple"
                                id="series_var_val_y1_1"></select>

                    </td>
                    <td>
                        <button id="series_var_val_y1_date_period_button_1"
                                style="display: none;">Select period
                        </button>
                    </td>

                </tr>
            </table>
            <button id="add_series_var_y1" style="margin-top:5px;">Series
                Variable
            </button>
        </div>
    </div>


    <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">Fixed Values:
            <button class="help-button" style="float: right;" alt="plot_fix">
                Help
            </button>
        </div>

        <table id='fixed_var_table' style="display: none;">
            <tr>
                <td>
                    <button id="remove_fixed_var_1" class="remove_fixed_var">
                        Remove
                    </button>
                </td>

                <td>
                    <select id="fixed_var_1">
                        <option value="fcst_var">FCST_VAR</option>
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
                        <option value="obs_thresh">OBS_THRESH</option>
                    </select>
                </td>
                <td>
                    <select multiple="multiple" id="fixed_var_val_1"></select>
                </td>
                <td>
                    <button id="fixed_var_val_date_period_button_1"
                            style="display: none;">Select period
                    </button>
                </td>
                <td>
                    <input id="fixed_var_val_date_range_1" value="" style="display: none"/>
                    <button id="fixed_var_val_date_range_button_1"
                            style="display: none;">Calendar
                    </button>
                </td>

            </tr>
        </table>
        <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
        <br/>
        <br/>
        <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""
                                                          id="txtPlotCond"
                                                          style="width: 95%">
    </div>


    <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">Independent Variables:
            <button class="help-button" style="float: right;" alt="indep">
                Help
            </button>
        </div>

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
                <td>
                    <button id="date_period_button" style="display: none;">
                        Select multiple options
                    </button>
                </td>
                <td>
                    <input id="date_range" value="" style="display: none"/>
                    <button id="date_range_button" style="display: none;">Calendar</button>
                </td>
            </tr>

        </table>
    </div>

    <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">Statistics:</div>

        <div id="statistics">
            <div id="radio">

                <input type="radio" name="statistics" checked
                       value="calculations_statistics"
                       id="calculations_statistics_label"/>
                <label for="calculations_statistics_label"> Summary</label>
                <input type="radio" name="statistics"
                       value="aggregation_statistics"
                       id="aggregation_statistics_label"/>
                <label for="aggregation_statistics_label">Aggregation
                    statistics</label>


            </div>
            <div id="aggregation_statistics">
                <button class="help-button" style="float: right;bottom: 40px;"
                        alt="agg_stat">Help
                </button>
                <table style="width:100%">
                    <tr>
                        <td>
                            <select id="agg_stat">
                                <option value="none">None</option>
                                <option value="ctc">Contingency table count (CTC)</option>
                            </select>
                        </td>

                        <td style="text-align:right;"><input type="text" value="1"
                                                             size="4"
                                                             id="boot_repl"></td>
                        <td><label for="boot_repl">Bootstrapping
                            replications</label></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>


                        <td style="text-align:right;"><input type="text" value=""
                                                             size="4"
                                                             id="boot_random_seed"></td>
                        <td><label for="boot_random_seed">Bootstrapping
                            seed</label></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>

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


                        <td colspan="2" style="text-align:right;"><input id="selEveqDis"
                                                                         type="checkbox"></td>
                        <td><label
                                title="Disables automatic event equalization that occurs when bootstrap confidence intervals are requested"
                                for="selEveqDis">Event equalization</label></td>
                    </tr>
                    <tr>


                        <td colspan="2" style="text-align:right;"><input id="cacheAggStat"
                                                                         type="checkbox"></td>
                        <td><label for="cacheAggStat">Cache aggregation
                            statistics</label></td>
                    </tr>


                </table>


            </div>

            <div id="calculations_statistics">

                <button class="help-button" style="float: right;bottom: 40px;"
                        alt="calc_stat">Help
                </button>
                <table style="width:100%">

                    <tr>
                        <td><select id="calc_stat">
                            <option value="none">None</option>
                            <option value="ctc">Contingency table count (CTC)</option>
                        </select></td>


                        <td style="text-align:center;"><span><label for="plot_stat">Plot
                    statistic:</label><select id="plot_stat" name="plot_stat">
                    <option selected="selected" value="median">Median</option>
                    <option value="mean">Mean</option>
                    <option value="sum">Sum</option>
                  </select></span></td>
                    </tr>

                </table>

            </div>

            <div id="none"></div>
        </div>
    </div>
    <div id="helpContent" title="Help">
    </div>
    <div id="date_period_dialog">
        <table>
            <tr>
                <td><label for="date_period_start">Start:</label></td>
                <td><select id="date_period_start"></select></td>
            </tr>
            <tr>
                <td><label for="date_period_end">End:</label></td>
                <td><select id="date_period_end"></select></td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <label for="date_period_by">By:</label><input type="text"
                                                                  style="width: 30px"
                                                                  id="date_period_by"/>
                    <select id="date_period_by_unit">
                        <option value="sec">sec</option>
                        <option value="min">min</option>
                        <option value="hours" selected>hours</option>
                        <option value="days">days</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>

    <div id="fixed_var_val_date_period_1" style="display: none;">
        <table>
            <tr>
                <td><label>Start:</label>
                </td>
                <td><select id="fixed_var_val_date_period_start_1"></select>
                </td>
            </tr>
            <tr>
                <td><label>End:</label>
                </td>
                <td><select id="fixed_var_val_date_period_end_1"></select></td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <label>By:</label><input
                        type="text"
                        style="width: 50px"
                        id="fixed_var_val_date_period_by_1"/>
                    <select id="fixed_var_val_date_period_by_unit_1">
                        <option value="sec">sec</option>
                        <option value="min">min</option>
                        <option value="hours" selected>hours</option>
                        <option value="days">days</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>
    <div id="series_var_val_y1_date_period_1" style="display: none;">
        <table>
            <tr>
                <td><label>Start:</label>
                </td>
                <td><select
                        id="series_var_val_y1_date_period_start_1"></select>
                </td>
            </tr>
            <tr>
                <td><label>End:</label>
                </td>
                <td><select id="series_var_val_y1_date_period_end_1"></select>
                </td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <label>By:</label><input
                        type="text"
                        style="width: 50px"
                        id="series_var_val_y1_date_period_by_1"/>
                    <select id="series_var_val_y1_date_period_by_unit_1">
                        <option value="sec">sec</option>
                        <option value="min">min</option>
                        <option value="hours" selected>hours</option>
                        <option value="days">days</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>

    <div id="series_var_val_y2_date_period_1" style="display: none;">
        <table>
            <tr>
                <td><label>Start:</label>
                </td>
                <td><select
                        id="series_var_val_y2_date_period_start_1"></select>
                </td>
            </tr>
            <tr>
                <td><label>End:</label>
                </td>
                <td><select id="series_var_val_y2_date_period_end_1"></select>
                </td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <label>By:</label><input
                        type="text"
                        style="width: 30px"
                        id="series_var_val_y2_date_period_by_1"/>
                    <select id="series_var_val_y2_date_period_by_unit_1">
                        <option value="sec">sec</option>
                        <option value="min">min</option>
                        <option value="hours" selected>hours</option>
                        <option value="days">days</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>
</div>
</body>
</html>
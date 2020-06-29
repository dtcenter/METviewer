<!DOCTYPE html>

<html>
<HEAD>
    <META http-equiv="content-type" content="text/html; charset=utf-8">
    <style type="text/css">
        .add-font-size {
            font-size: 10px;
        }

        legend {
            font-weight: bold;
        }

        fieldset {
            margin-top: 5px;
            margin-left: 5px;
            margin-right: 5px;
        }
    </style>
    <script type="text/javascript">


        $(document).ready(function () {
            $.ajaxSetup({
                beforeSend: function () {
                    $('#modal').css("display", "block");
                    $('#fade').css("display", "block");
                },
                complete: function () {
                    $('#modal').css("display", "none");
                    $('#fade').css("display", "none");
                }
            });
            $('.help-button').button({
                icons: {
                    primary: "ui-icon-help"
                },
                text: false
            }).click(function () {
                $('#helpContent').empty().append($("<iframe id='helpContentFrame'/>").css("width", "100%").css("height", "100%").attr("src", "doc/plot.html#" + $(this).attr("alt")));
                $('#helpContent').dialog({
                    buttons: {
                        "Open in new window": function () {
                            var win = window.open('doc/plot.html');
                            if (win) {
                                //Browser has allowed it to be opened
                                win.focus();
                            } else {
                                //Browser has blocked it
                                alert('Please allow popups for this site');
                            }
                        },
                        Cancel: function () {
                            $(this).dialog("close");
                        }
                    }
                }).dialog('open');
            });

            $("#helpContent").append($("<iframe id='helpContentFrame'/>").css("width", "100%").css("height", "100%")).dialog({
                height: 400,
                width: 600,
                autoOpen: false
            });
            $("#indy_var_event_equal").prop('checked', false).prop('disabled', true);
            $("#fix_var_event_equal_1").prop('checked', false).prop('disabled', true);
            $('#event_equal').change(function () {
                if ($(this).prop("checked")) {
                    for (var i = 0; i < fixed_var_indexes.length; i++) {
                        $("#fix_var_event_equal_" + fixed_var_indexes[i]).prop('checked', true).prop('disabled', false);
                    }
                    $("#indy_var_event_equal").prop('checked', true).prop('disabled', false);
                } else {
                    for (var i = 0; i < fixed_var_indexes.length; i++) {
                        $("#fix_var_event_equal_" + fixed_var_indexes[i]).prop('checked', false).prop('disabled', true);
                    }
                    $("#indy_var_event_equal").prop('checked', false).prop('disabled', true);

                }
            }).prop("checked", false);


            $('#add_fixed_var').button({
                icons: {
                    primary: "ui-icon-circle-plus"
                }
            }).click(function () {
                addFixedVar();
            });


            $("#series_var_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 200,

                click: function () {

                    var values = $('#series_var_val_y1_1').val();
                    if (values == null) {
                        values = [];
                    }
                    updateYvalueCountour(values);
                },
                close: function () {
                    updateSeries();
                }
            });

            $("#fcst_var_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                click: function () {
                    updateStatVal();
                }


            });

            $("#fcst_stat_y1_1").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                noneSelectedText: "Select value",
                click: function () {
                    updateSeries();
                },
                close: function () {
                    updateSeries();
                }
            });


            $("#series_var_val_y1_1").multiselect({
                selectedList: 100, // 0-based index
                noneSelectedText: "Select value",
                close: function (event, ui) {
                    updateSeries();
                },
                position: {
                    my: 'right center',
                    at: 'right center'
                },
                checkAll: function () {
                    updateSeries();
                },
                uncheckAll: function () {
                    updateSeries();
                }

            });


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


            $('#aggregation_statistics ').hide();
            $('#calculations_statistics ').hide();


            $("#calc_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                position: {
                    my: 'left bottom',
                    at: 'left top'
                },
                click: function (event, ui) {
                    if (ui.value !== "none") {
                        $("#agg_stat").val("none").multiselect("refresh");
                    }
                }
            });
            $("#agg_stat").multiselect({
                multiple: false,
                selectedList: 1,
                header: false,
                minWidth: 'auto',
                height: 'auto',
                position: {
                    my: 'left bottom',
                    at: 'left top'
                },
                click: function (event, ui) {
                    if (ui.value !== "none") {
                        $("#calc_stat").val("none").multiselect("refresh");
                    }
                }
            });

            $(' input[name="statistics"]').click(function () {
                $('#aggregation_statistics ').hide();
                $('#calculations_statistics ').hide();
                $(this).prop("checked", true);
                $('#' + $(this).val()).show();
            });
            $('#calculations_statistics').show();


            $(".remove_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false,
                disabled: true
            }).click(function () {
                if ($(this).attr('id').startsWith('remove_fcst_var')) {
                    removeSeriesVar($(this).attr('id'));
                }
            });
            $(".remove_fixed_var").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false
            }).click(function () {
                removeFixedVar($(this).attr('id'));
                updateSeries();
            });


            $('#add_color_bar').prop('checked', true);
            $('#reverse_y').prop('checked', false);
            $('#reverse_x').prop('checked', false);
            $('#add_contour_overlay').prop('checked', true);
            $('#contour_intervals').val('');

            if (initXML != null) {
                var sd = initXML.find("database").text();
                var selectedDatabase = sd.split(",");
                for (var i = 0; i < selectedDatabase.length; i++) {
                    $("input[name='multiselect_database'][value='" + selectedDatabase[i] + "']")
                        .prop("checked", true).change();
                }
                var csv = selectedDatabase.join(",");
                var textnode = document.createTextNode(csv);
                var item = document.getElementById("categories1").childNodes[0];
                item.replaceChild(textnode, item.childNodes[0]);
                loadXMLContour();
                initXML = null;

            } else {
                updateStatVariable();
                updateYvalueCountour([]);
                $.each(fix_var_value_to_title_stat_map, function (key, val) {
                    $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
                });
            }
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
                    $("#date_range").unbind("datepicker-apply").data('dateRangePicker').clear().data('dateRangePicker').destroy();
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
                        return createCalendarTopBarWithFormat();
                    }
                }).bind('datepicker-apply', function (event, obj) {
                    onIndyCalendarClose(obj);
                }).data('dateRangePicker').open();
            });

        });


    </script>
</head>
<body>
<div style="overflow: auto;">

    <div class="ui-layout-center no-padding">
        <div id="stat_variables" class="no-padding">
            <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
                <div class="ui-widget-header-plot">Variable and Statistic:
                    <button class="help-button" style="float: right;"
                            alt="dep">Help
                    </button>
                </div>
                <table id="stat_var_table">
                    <tr>
                        <td>
                            <select id="fcst_var_y1_1">
                            </select>
                        </td>
                        <td>
                            <select id="fcst_stat_y1_1"></select>
                        </td>
                    </tr>
                </table>
            </div>
        </div>

        <div id="y1_axis_variables" class="no-padding">
            <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
                <div class="ui-widget-header-plot">Y axis Independent Variable:
                    <button class="help-button" style="float: right;"
                            alt="dep">Help
                    </button>
                </div>
                <table id="dependent_var_table_y1">

                    <tr>

                        <td>
                            <select id="series_var_y1_1">
                                <option value="fcst_lev">FCST_LEV</option>
                                <option value="model">MODEL</option>
                                <option value="valid_hour">VALID_HOUR</option>
                                <option value="init_hour">INIT_HOUR</option>
                                <option value="fcst_lead">FCST_LEAD</option>
                                <option value="obtype">OBTYPE</option>
                                <option value="vx_mask">VX_MASK</option>
                                <option value="interp_mthd">INTERP_MTHD</option>
                                <option value="interp_pnts">INTERP_PNTS</option>
                                <option value="fcst_thresh">FCST_THRESH</option>
                                <option value="obs_thresh">OBS_THRESH</option>
                            </select>
                        </td>
                        <td>
                            <select name="series_var_val_y1_1" multiple="multiple"
                                    id="series_var_val_y1_1"></select>

                        </td>

                    </tr>
                </table>


                <div style="margin-top:5px;margin-bottom:5px;"></div>


            </div>
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
                <td>
                    <input type="checkbox" id="fix_var_event_equal_1"
                           title='Add entry to event equalization logic'><label
                        for="fix_var_event_equal_1">Equalize</label>
                </td>


            </tr>
        </table>
        <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
        <div>

            <input type="checkbox" id="event_equal"
                   title="Equalize based on the valid time, lead time, and all series entries"/>
            <label for="event_equal" title="Equalize based on the valid time, lead time, and all series entries">Event
                Equalizer</label>
        </div>
        <br/>

        <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""
                                                          id="txtPlotCond"
                                                          style="width: 95%">
    </div>


    <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">X axis Independent Variable:
            <button class="help-button" style="float: right;" alt="indep">
                Help
            </button>
        </div>

        <table id='indy_var_table'>
            <tr>
                <td>
                    <select id="indy_var">
                        <option value="init_hour">INIT_HOUR</option>
                        <option value="fcst_lead">FCST_LEAD</option>
                        <option value="model">MODEL</option>
                        <option value="fcst_valid_beg">FCST_VALID_BEG</option>
                        <option value="valid_hour">VALID_HOUR</option>
                        <option value="fcst_init_beg">FCST_INIT_BEG</option>
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
                <td>
                    <input type="checkbox" id="indy_var_event_equal"
                           title='Add entry to event equalization logic'><label for="indy_var_event_equal"
                                                                                title='Add entry to event equalization logic'>Equalize</label>
                </td>
            </tr>

        </table>
    </div>
    <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">Configurations:
            <table width="95%">
                <tr>
                    <td style="text-align: right;"><select id="color_palette">
                        <option value="green.red">green.red</option>
                        <option value="blue.white.brown">blue.white.brown</option>
                        <option value="cm.colors">cm.colors</option>
                        <option value="topo.colors">topo.colors</option>
                        <option value="terrain.colors">terrain.colors</option>
                        <option value="heat.colors">heat.colors</option>
                        <option value="rainbow">rainbow</option>
                    </select></td>
                    <td><label for="color_palette">Color palette</label></td>
                    <td style="text-align: right;"><input id="reverse_y" type="checkbox"></td>
                    <td><label for="reverse_y">Reverse Y values</label></td>
                </tr>
                <tr>
                    <td style="text-align: right;"><input id="contour_intervals" style="width: 15px;"></td>
                    <td><label for="contour_intervals">Contour intervals</label></td>
                    <td style="text-align: right;"><input id="reverse_x" type="checkbox"></td>
                    <td><label for="reverse_x">Reverse X values</label></td>
                </tr>
                <tr>
                    <td style="text-align: right;"><input id="add_color_bar" type="checkbox"></td>
                    <td><label for="add_color_bar">Add color bar</label></td>
                    <td style="text-align: right;"><input id="add_contour_overlay" type="checkbox"></td>
                    <td><label for="add_contour_overlay">Add contour line overlay</label></td>
                </tr>
            </table>
        </div>
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
                        <td><select id="agg_stat">
                            <option value="none">None</option>
                            <option value="ctc">Contingency table count (CTC)</option>
                            <option value="sl1l2">Scalar partial sums (SL1L2)</option>
                            <option value="sal1l2">Scalar anomaly partial sums (SAL1L2)</option>
                            <option value="pct">Probability contingency table (PCT)</option>
                            <option value="nbrcnt">Neighborhood continuous statistics (NBRCNT)</option>
                            <option value="nbrctc">Neighborhood contingency table (NBRCTC)</option>
                            <option value="ssvar">Spread/Skill Variance (SSVAR)</option>
                            <option value="vl1l2">Vector partial sums (VL1L2)</option>
                            <option value="val1l2">Vector anomaly partial sums (VAL1L2)</option>
                            <option value="grad">Gradient partial sums (GRAD)</option>
                            <option value="ecnt">Ensemble Continuous Statistics (ECNT)</option>
                            <option value="rps">Ranked Probability Score Statistics (RPS)</option>

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
                        <td><label for="boot_ci">Confidence Interval method</label></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>

                        <td style="text-align:right;"><input id="cacheAggStat"
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
                            <option value="sl1l2">Scalar partial sums (SL1L2)</option>
                            <option value="sal1l2">Scalar anomaly partial sums (SAL1L2)</option>
                            <option value="vl1l2">Vector partial sums (VL1L2)</option>
                            <option value="val1l2">Vector anomaly partial sums (VAL1L2)</option>
                            <option value="grad">Gradient partial sums (GRAD)</option>
                        </select>
                        </td>
                        <td><span><label for="plot_stat">Plot
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
    <div id="fcst_var_val_y1_date_period_1" style="display: none;">
        <table>
            <tr>
                <td><label>Start:</label>
                </td>
                <td><select
                        id="fcst_var_val_y1_date_period_start_1"></select>
                </td>
            </tr>
            <tr>
                <td><label>End:</label>
                </td>
                <td><select id="fcst_var_val_y1_date_period_end_1"></select>
                </td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <label>By:</label><input
                        type="text"
                        style="width: 50px"
                        id="fcst_var_val_y1_date_period_by_1"/>
                    <select id="fcst_var_val_y1_date_period_by_unit_1">
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
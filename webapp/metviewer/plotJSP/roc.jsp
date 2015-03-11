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

        fieldset {
            margin-top: 5px;
            margin-left: 5px;
            margin-right: 5px;
        }
    </style>
    <script type="text/javascript">
        $(document).ready(function () {
            series_var_y1_indexes=[];
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
                autoOpen: false,
                open: function (event, ui) {
                }
            });
        });

        $(".remove_fixed_var").button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false,
            disabled: true
        }).click(function () {
                    removeFixedVarRhist($(this).attr('id'));
                });


        $('#add_series_var_y1').button({
            icons: {
                primary: "ui-icon-circle-plus"
            }
        }).click(function () {
                    addSeriesVariableRhist();
                });
        $(".remove_var").button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        }).click(function () {
                    removeSeriesVarCommon($(this).attr('id'));
                    updateSeriesRhist();
                });

        $("#fixed_var_1").multiselect({
            multiple: false,
            selectedList: 1,
            header: false,
            minWidth: 'auto',
            height: 'auto',
            click: function () {
                var id_array = this.id.split("_");
                updateFixedVarValHist(id_array[id_array.length - 1], []);
            }
        });
        $("#fixed_var_val_1").multiselect({
            selectedList: 100, // 0-based index
            noneSelectedText: "Select value"
        });
        updateFixedVarValHist(1, []);
        fixed_var_indexes.push(1);


        $('#add_fixed_var').button({
            icons: {
                primary: "ui-icon-circle-plus"
            }
        }).click(function () {
                    addFixedVariableRhist();
                });

        if (initXML != null) {
            loadXMLRoc();
            initXML = null;
        } else {
            updateSeriesVarValRhist(1, []);
            updateSeriesRhist();
            $("input[name=roc_type][value=pct]").prop('checked', true);
        }
        $('#roc_type').buttonset();
    </script>
</head>
<body>
<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Series Variables:
        <button class="help-button" style="float: right;" alt="series">Help
        </button>
    </div>
    <table id='series_var_table_y1'  style="display: none;">
        <tr>
            <td>
                <button id="remove_series_var_y1_1" class="remove_var">
                    Remove
                </button>
            </td>

            <td>
                <select id="series_var_y1_1">
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
                <select multiple="multiple" id="series_var_val_y1_1">

                </select>

            </td>
        </tr>
    </table>
    <button id="add_series_var_y1" style="margin-top:5px;">Series Variable
    </button>
</div>


<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Specialized Plot Fixed Values:
        <button class="help-button" style="float: right;" alt="plot_fix">Help
        </button>
    </div>
    <table id='fixed_var_table'>
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
                <select multiple="multiple" id="fixed_var_val_1">

                </select>

            </td>
        </tr>
    </table>
    <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
    <br/>
        <br/>
        <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""  id="txtPlotCond" style="width: 95%">
</div>
<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">ROC Calculations
            <button class="help-button" style="float: right;" alt="roc_calc">Help
            </button>
        </div>

    <div id="roc_type">
        <input type="radio" name="roc_type" id="pct_roc_pct" value="pct" checked/>
        <label for="pct_roc_pct">PCT</label>


        <input type="radio" name="roc_type"  id="ctc_roc_pct" value="ctc"/>
        <label for="ctc_roc_pct">CTC</label>
    </div>
    </div>
<div id="helpContent" title="Help">
</div>
</body>
</html>
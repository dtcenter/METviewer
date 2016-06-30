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
        series_var_y1_indexes = [];
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
            click: function (event, ui) {
                $('#fixed_var_val_date_period_start_1').empty();
                $('#fixed_var_val_date_period_end_1').empty();

                if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                    $("#fixed_var_val_date_period_button_1").css("display", "block");
                } else {
                    $("#fixed_var_val_date_period_button_1").css("display", "none");
                }
                var id_array = this.id.split("_");
                updateFixedVarValHist(id_array[id_array.length - 1], []);
            }
        });
        $("#fixed_var_val_date_period_button_1").button({
            icons: {
                primary: "ui-icon-check",
                secondary: "ui-icon-circlesmall-plus"
            },
            text: false
        }).click(function () {
            $("#fixed_var_val_date_period_1").dialog("open");
        });
        createValDatePeriodDialog('fixed_var_val',1);

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
      getForecastVariablesHist();

      $("input:radio[name='rely_event_hist']").change(function () {
        updateSeriesRhist();
      });
      updateSeriesRhist();
      $('#is_hist').buttonset();
      $('#summary_curve').val("none");
      $("#summary_curve").multiselect({
        multiple: false,
        header: false,
        height: 'auto',
        selectedList: 1,
        create: function () {
          $('#summary_curve').multiselect("uncheckAll");
        },
        click: function (event, ui) {
          updateSeriesRhist();
        }
      });

      if (initXML != null) {
        loadXMLRely();
        updateSeriesRhist();
        initXML = null;
        }else{
            updateSeriesVarValRhist(1, []);
            updateSeriesRhist();
            $("input[name=rely_event_hist][value=false]").prop('checked', true);
        }


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
            <td>
                <button id="series_var_val_y1_date_period_button_1"
                        style="display: none;">Select period
                </button>
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
            <td>
                <button id="fixed_var_val_date_period_button_1"
                        style="display: none;">Select period
                </button>
            </td>
        </tr>
    </table>
    <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
    <br/>
        <br/>
        <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""  id="txtPlotCond" style="width: 95%">
</div>
<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Reliability Event Histogram

        </div>

    <div id="is_hist">
        <input type="radio" name="rely_event_hist" value="true"
               id="true_is_hist" checked/>
        <label for="true_is_hist">Yes</label>


        <input type="radio" name="rely_event_hist" value="false"
               id="false_is_hist"/>
        <label for="false_is_hist">No</label>
    </div>
    </div>

<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
  <div class="ui-widget-header-plot">Summary Curve
    <button class="help-button" style="float: right;" alt="roc_calc">Help
    </button>
  </div>

  <select id="summary_curve" style="width: 135px;">
    <option value="none">None</option>
     <option value="median">Median</option>
     <option value="mean">Mean</option>
  </select>


</div>

<div id="helpContent" title="Help">
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
               <td><label >End:</label>
               </td>
               <td><select id="fixed_var_val_date_period_end_1"></select></td>
           </tr>
           <tr>
               <td colspan="2" style="text-align: center;">
                   <label >By:</label><input
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
</body>
</html>
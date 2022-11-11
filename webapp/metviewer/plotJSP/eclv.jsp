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

    $(' input[name="statistics"]').click(function () {
      $('#aggregation_statistics ').hide();
      $('#calculations_statistics ').hide();
      $(this).prop("checked", true);
      $('#' + $(this).val()).show();
    });
    $('#calculations_statistics').show();
    $('#aggregation_statistics ').hide();
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

    $("#plot_stat").multiselect({
      multiple: false,
      selectedList: 1,
      header: false,
      minWidth: 'auto',
      height: 'auto'

    });

    $(".remove_fixed_var").button({
      icons: {
        primary: "ui-icon-trash"
      },
      text: false,
      disabled: true
    }).click(function () {
      removeFixedVarHist($(this).attr('id'));
    });


    $('#add_series_var_y1').button({
      icons: {
        primary: "ui-icon-circle-plus"
      }
    }).click(function () {
      addSeriesVarHist();
    });
    $(".remove_var").button({
      icons: {
        primary: "ui-icon-trash"
      },
      text: false
    }).click(function () {
      removeSeriesVarCommon($(this).attr('id'));
      updateSeriesHist();
    });
    $('#cl_step').val(0.05);

    $('#event_equal').on("click", function () {
      if ($(this).prop("checked")) {
        for (var i = 0; i < fixed_var_indexes.length; i++) {
          $("#fix_var_event_equal_" + fixed_var_indexes[i]).prop('checked', true).prop('disabled', false);
        }
        $("#indy_var_event_equal").prop('checked', true).prop('disabled', false);
      } else {
        for (var i = 0; i < fixed_var_indexes.length; i++) {
          $("#fix_var_event_equal_" + fixed_var_indexes[i]).prop('checked', false).prop('disabled', true);
        }

      }
    }).prop("checked", false);

    $("#fixed_var_1").multiselect({
      multiple: false,
      selectedList: 1,
      header: false,
      minWidth: 'auto',
      height: 'auto',
      click: function (event, ui) {
        $('#fixed_var_val_date_period_start_1').empty();
        $('#fixed_var_val_date_period_end_1').empty();

        if (ui.value === "fcst_init_beg" || ui.value === "fcst_valid_beg" || ui.value === "fcst_valid" || ui.value === "fcst_init") {
          $("#fixed_var_val_date_period_button_1").css("display", "block");
          $("#fixed_var_val_date_range_button_1").css("display", "block");
        } else {
          $("#fixed_var_val_date_period_button_1").css("display", "none");
          $("#fixed_var_val_date_range_button_1").css("display", "none");
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
    createValDatePeriodDialog('fixed_var_val', 1);


    $("#fixed_var_val_date_range_button_1").button({
      icons: {
        primary: "ui-icon-calendar"
      },
      text: false
    }).click(function (evt) {
      evt.stopPropagation();
      var dates = $(fixVarValResponse[1]).find("val");
      var start = $(dates[0]).text();
      var end = $(dates[dates.length - 1]).text();
      try {
        $("#fixed_var_val_date_range_1").unbind("datepicker-apply").data('dateRangePicker').destroy();
      } catch (error) {
        console.log(error);
      }
      $("#fixed_var_val_date_range_1").dateRangePicker({
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
        container: document.getElementById('fixed_var_table').parentElement,
        customTopBar: function () {
          return createCalendarTopBarNoFormat(1);
        }
      }).bind('datepicker-apply', function (event, obj) {
        onIndyCalendarClose(obj,1);
      }).data('dateRangePicker').open();
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
      addFixedVar();
    });
    getForecastVariablesHist();

    if (initXML != null) {
      if(initXML.find("error").length > 0 ){
        alert(initXML.find("error").text())
      }else {
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
        loadXMLEclv();
        updateSeriesHist();
      }
      initXML = null;
    } else {
      series_var_y1_indexes = [];
      updateSeriesVarValHist(1, []);
      updateSeriesHist();
    }

  </script>
</head>
<body>
<div class="ui-widget-content ui-widget-content-plot ui-corner-all">
  <div class="ui-widget-header-plot">Series Variables:
    <button class="help-button" style="float: right;" alt="series">Help
    </button>
  </div>
  <table id='series_var_table_y1' style="display: none;">
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
          <option value="cov_thresh">COV_THRESH</option>
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

      <td>
        <input id="fixed_var_val_date_range_1" value="" style="display: none"/>
        <button id="fixed_var_val_date_range_button_1"
                style="display: none;">Calendar
        </button>
      </td>
      <td><input type="checkbox" id="fix_var_event_equal_1" title='Add entry to event equalization logic'><label for="fix_var_event_equal_1">Equalize</label> </td>

    </tr>
  </table>
  <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
  <div>

    <input type="checkbox" id="event_equal" title="Equalize based on the valid time, lead time, and all series entries"/>
    <label for="event_equal" title="Equalize based on the valid time, lead time, and all series entries">Event Equalizer</label>
  </div>
  <br/>
  <label for="txtPlotCond">Plot Cond</label> <input type="text" value="" id="txtPlotCond" style="width: 95%">
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
                <option value="pct">Probability contingency table (PCT)</option>
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
              </select></td>
              <td><label for="boot_ci">Confidence Interval method</label></td>
          </tr>
          <tr>
            <td>&nbsp;</td>

            <td style="text-align:right;"><input type="text" value="1"
                                                           size="4"
                                                           id="cl_step"></td>
                      <td><label for="cl_step">Cost loss step</label></td>
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
</body>
</html>
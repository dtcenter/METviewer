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
                //Browser has blocked it
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

      $("#tabs_axis_variables").tabs({
        heightStyle: "content"
      });

      $('#add_fcst_var_y1').button({
                icons: {
                  primary: "ui-icon-circle-plus"
                }
              }
      ).click(function () {
        addFcstVar("y1");
      });
      $('#add_fcst_var_y2').button({
        icons: {
          primary: "ui-icon-circle-plus"
        }
      }).click(function () {
        addFcstVar("y2");
      });
      $('#add_fixed_var').button({
        icons: {
          primary: "ui-icon-circle-plus"
        }
      }).click(function () {
        addFixedVar();
      });


      $("#plot_data").multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        height: 'auto',
        click: function (event, ui) {
          updateForecastVariables();
          if ($('#plot_data').multiselect('getChecked')[0].value !== ui.value) {
            ui.value = $('#plot_data').multiselect('getChecked')[0].value;
            updateForecastVariables();
            if ($('#plot_data').multiselect('getChecked')[0].value !== ui.value) {
              updateForecastVariables();
            }
          }
          if ($('#plot_data').multiselect('getChecked')[0].value == 'stat') {
            updateStats("y1", 1, []);
            updateStats("y2", 1, []);
            updateFixVar("stat");
            updateIndyVar("stat");
            $("#agg_none").prop('checked', 'checked');
          } else if ($('#plot_data').multiselect('getChecked')[0].value == 'mode') {
            updateMode("y1", 1, []);
            updateMode("y2", 1, []);
            updateFixVar("mode");
            updateIndyVar("mode");
          } else if ($('#plot_data').multiselect('getChecked')[0].value == 'mtd') {
            updateMtd("y1", 1, []);
            updateMtd("y2", 1, []);
            updateFixVar("mtd");
            updateIndyVar("mtd");
          }
          updateSeriesVarVal("y1", 1, []);
          updateSeriesVarVal("y2", 1, []);
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
        header: false,
        minWidth: 'auto',
        height: 200,

        click: function () {
          var id_array = this.id.split("_");
          updateStats(id_array[id_array.length - 2], id_array[id_array.length - 1], []);
          var selectedSeriesVarVal = $("#series_var_val_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("getChecked").val();
          if (selectedSeriesVarVal == null) {
            selectedSeriesVarVal = [];
          }
          updateSeriesVarVal(id_array[id_array.length - 2], id_array[id_array.length - 1], selectedSeriesVarVal);
        }
      });


      $("#fcst_stat_y1_1").multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select attribute stat",
        click: function (event, ui) {
          var id_array = this.id.split("_");
          $("#fcst_stat_mode_config_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).css("display", "none");
          try {
            $("#fcst_stat_mode_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("uncheckAll");
          } catch (err) {
            console.log("Error " + err);
          }

          if ($("#plot_data").val() === "mode") {
            $("input[name=statistics][value=aggregation_statistics]").prop('checked', true);
            $('#agg_stat').val('mode');
            try {
              $('#agg_stat').multiselect('refresh');
            } catch (e) {
            }
            $('#aggregation_statistics ').show();
            $('#calculations_statistics ').hide();
            $('#revision_statistics ').hide();
          }

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

          if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
            $("#date_period_button").css("display", "block");
          } else {
            $("#date_period_button").css("display", "none");
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
          updateSeries();
        },
        checkAll: function () {
          updateSeries(true);
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
        addSeriesVar("y1");
      });
      $('#add_series_var_y2').button({
        icons: {
          primary: "ui-icon-circle-plus"
        }
      }).click(function () {
        addSeriesVar("y2");
      });


      $('#aggregation_statistics ').hide();
      $('#calculations_statistics ').hide();
      $('#revision_statistics ').hide();

      $('#event_equal').prop("checked", false);

      $("#indy_var_event_equal").prop('checked', false).prop('disabled', true);
      $("#fix_var_event_equal_1").prop('checked', false).prop('disabled', true);
      $('#event_equal').change(function(){
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
      });

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
            $("#agg_stat").val("none");
            $("#agg_stat").multiselect("refresh");
            $("#revis_stat").val("none");
            $("#revis_stat").multiselect("refresh");
          }
        }
      });
      $("#revis_stat").multiselect({
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
            $("#agg_stat").val("none");
            $("#agg_stat").multiselect("refresh");
            $("#calc_stat").val("none");
            $("#calc_stat").multiselect("refresh");
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
            $("#calc_stat").val("none");
            $("#calc_stat").multiselect("refresh");
            $("#revis_stat").val("none");
            $("#revis_stat").multiselect("refresh");
          }
        }
      });

      $(' input[name="statistics"]').click(function () {
        $('#aggregation_statistics').hide();
        $('#calculations_statistics').hide();
        $('#revision_statistics').hide();
        $(this).prop("checked", true);
        $('#' + $(this).val()).show();
        if($(this).val() === "revision_statistics"){
          $('#indy_var').val("fcst_valid_beg");
          $("#indy_var").multiselect("refresh");
        }
      });
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
        var val = $($(categories[0]).find("val")[0]).text();
        $("input[name='multiselect_database'][value='" + val + "']")
                .prop("checked", true).change();
        updateForecastVariables();
        updateStats("y1", 1, []);
        updateStats("y2", 1, []);
        updateSeriesVarVal("y1", 1, []);
        updateSeriesVarVal("y2", 1, []);

      }


    });


  </script>
</head>
<body>
<div style="overflow: auto;">
  <div style="width: 100%; text-align: center;">
    <span style="margin-right:20px;"><label for="plot_data">Plot
      Data:</label><select id="plot_data">
      <option selected="selected" value="stat">Stat</option>
      <option value="mode">MODE</option>
      <option value="mtd">MODE-TD</option>
    </select></span>

  </div>
  <div id="tabs_axis_variables" class="ui-layout-center no-padding"
       style="border:none;">
    <ul class="allow-overflow "
        style="background:none;border-left: medium none; border-right: medium none; border-top: medium none;">
      <li><a href="#y1_axis_variables">Y1 Axis variables</a></li>
      <li><a href="#y2_axis_variables">Y2 Axis variables</a></li>
    </ul>
    <div id="y1_axis_variables" class="no-padding">
      <div class="ui-widget-content ui-widget-content-plot ui-corner-all"
           style=" border-top:none;">
        <div class="ui-widget-header-plot">Y1 Dependent (Forecast)
          Variables:
          <button class="help-button" style="float: right;"
                  alt="dep">Help
          </button>
        </div>
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
                      id="fcst_stat_mode_y1_1"
                      style="display: none">
              </select>
            </td>
            <td id="fcst_stat_mode_config_y1_1"
                style="display: none;">
              <table style="white-space:nowrap;">
                <tr>
                  <td rowspan="2"><input type="checkbox"
                                         class="non-acov non-pair"
                                         name="mode_stat_diff"
                                         value="D"
                                         onclick="updateSeries();"><label>Diff</label>
                  </td>
                  <td><input type="checkbox" class="non-pair"
                             checked name="mode_stat_fcst"
                             value="F"
                             onclick="updateSeries();"><label>Fcst</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_simple"
                             value="S"
                             onclick="updateSeries();"><label>Simple</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_matched"
                             value="M"
                             onclick="updateSeries();"><label>Matched</label>
                  </td>
                </tr>
                <tr>
                  <td><input type="checkbox" class="non-pair"
                             checked name="mode_stat_obs"
                             value="O"
                             onclick="updateSeries();"><label>Obs</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_cluster"
                             value="C"
                             onclick="updateSeries();"><label>Cluster</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked
                             name="mode_stat_unmatched"
                             value="U"
                             onclick="updateSeries();"><label>Unmatched</label>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>

        <button id="add_fcst_var_y1" style="margin-top:5px;">Variable
        </button>
        <div style="margin-top:5px;margin-bottom:5px;"></div>

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
                <option value="obs_var">OBS_VAR</option>
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
            <td><input type="checkbox" id="group_series_var_y1_1"
                       onclick="updateSeries();"><label
                    for="group_series_var_y1_1">Group_y1_1</label>
            </td>
          </tr>
        </table>
        <button id="add_series_var_y1" style="margin-top:5px;">Series
          Variable
        </button>
      </div>
    </div>
    <div id="y2_axis_variables" class="no-padding">
      <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
        <div class="ui-widget-header-plot">Y2 Dependent (Forecast)
          Variables:
          <button class="help-button" style="float: right;" alt="dep">Help</button>
        </div>

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
              <select name="fcst_stat_mode_y2_1"
                      id="fcst_stat_mode_y2_1"
                      style="display: none">
              </select>
            </td>
            <td id="fcst_stat_mode_config_y2_1"
                style="display: none;">
              <table style="white-space:nowrap;">
                <tr>
                  <td rowspan="2"><input type="checkbox"
                                         class="non-acov non-pair"
                                         name="mode_stat_diff"
                                         value="D"
                                         onclick="updateSeries();"><label>Diff</label>
                  </td>
                  <td><input type="checkbox" checked
                             name="mode_stat_fcst"
                             class="non-pair" value="F"
                             onclick="updateSeries();"><label>Fcst</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_simple"
                             value="S"
                             onclick="updateSeries();"><label>Simple</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_matched"
                             value="M"
                             onclick="updateSeries();"><label>Matched</label>
                  </td>
                </tr>
                <tr>
                  <td><input type="checkbox" checked
                             name="mode_stat_obs" value="O"
                             class="non-pair"
                             onclick="updateSeries();"><label>Obs</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked name="mode_stat_cluster"
                             value="C"
                             onclick="updateSeries();"><label>Cluster</label>
                  </td>
                  <td><input type="checkbox" class="non-acov"
                             checked
                             name="mode_stat_unmatched"
                             value="U"
                             onclick="updateSeries();"><label>Unmatched</label>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>

        <button id="add_fcst_var_y2" style="margin-top:5px;">Variable
        </button>
        <div style="margin-top:5px;margin-bottom:5px;"></div>

        <div class="ui-widget-header-plot">Y2 Series Variables:
          <button class="help-button" style="float: right;"
                  alt="series">Help
          </button>
        </div>

        <table id='series_var_table_y2' style="display: none;">
          <tr>
            <td>
              <button id="remove_series_var_y2_1">
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
                <option value="obs_thresh">OBS_THRESH</option>
                <option value="obs_var">OBS_VAR</option>
              </select>
            </td>
            <td>
              <select multiple="multiple"
                      id="series_var_val_y2_1">

              </select>
            </td>
            <td>
              <button id="series_var_val_y2_date_period_button_1"
                      style="display: none;">Select period
              </button>
            </td>
            <td><input type="checkbox" id="group_series_var_y2_1"
                       onclick="updateSeries();"><label
                    for="group_series_var_y2_1">Group_y2_1</label>
            </td>
          </tr>
        </table>
        <button id="add_series_var_y2" style="margin-top:5px;">Series
          Variable
        </button>
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
          <input type="checkbox" id="fix_var_event_equal_1" title='Add entry to event equalization logic'><label for="fix_var_event_equal_1">Equalize</label>
        </td>


      </tr>
    </table>
    <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>
    <div>

      <input type="checkbox" id="event_equal" title="Equalize based on the valid time, lead time, and all series entries"/>
      <label for="event_equal" title="Equalize based on the valid time, lead time, and all series entries">Event
        Equalizer</label>
    </div>
    <br/>
    <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""
                                                      id="txtPlotCond"
                                                      style="width: 95%">
  </div>


  <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Independent Variable:
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
            <option value="descr">DESC</option>
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
          <input type="checkbox" id="indy_var_event_equal" title='Add entry to event equalization logic'><label for="indy_var_event_equal" title='Add entry to event equalization logic'>Equalize</label>
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
        <input type="radio" name="statistics"
                      value="revision_statistics"
                      id="revision_statistics_label"/>
               <label for="revision_statistics_label">Revision
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
              <option value="pct">Probability methods output (PCT)</option>
              <option value="nbrcnt">Nbrhood method count (NBR_CNT)</option>
              <option value="ssvar">Spread/Skill Variance (SSVAR)</option>
              <option value="vl1l2">Vector partial sums (VL1L2)</option>
              <option value="val1l2">Vector anomaly partial sums (VAL1L2)</option>
              <option value="grad">Gradient partial sums (GRAD)</option>
              <option value="mode">MODE-ratio</option>
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

      <div id="revision_statistics">

        <button class="help-button" style="float: right;bottom: 40px;"
                alt="revis_stat">Help
        </button>
        <table style="width:100%">
          <tr>
            <td><select id="revis_stat">
              <option value="none">None</option>
              <option value="ctc">Contingency table count (CTC)</option>
              <option value="sl1l2">Scalar partial sums (SL1L2)</option>
              <option value="sal1l2">Scalar anomaly partial sums (SAL1L2)</option>
              <option value="vl1l2">Vector partial sums (VL1L2)</option>
              <option value="grad">Gradient partial sums (GRAD)</option>
            </select>
            </td>
            <td><input id="revision_ac" type="checkbox"><label
                    for="revision_ac">Add Auto-Correlation</label></td>
            <td><input id="revision_run" type="checkbox"><label
                    for="revision_run">Add Wald-Wolfowitz Runs Test</label></td>

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
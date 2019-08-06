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




      $('#add_fixed_var').button({
        icons: {
          primary: "ui-icon-circle-plus"
        }
      }).click(function () {
        addFixedVarHist();
      });

      $("#plot_data").multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        height: 'auto',
        click: function (event, ui) {
          updateForecastVariables();
          updateFixVar("stat");
          updateSeriesVarVal("y1", 1, []);
        }
      });


      $("#fcst_var_y1_1").multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 120,
        height: 200,

        click: function () {
          var id_array = this.id.split("_");
          var selectedSeriesVarVal = $("#series_var_val_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("getChecked").val();
          if (selectedSeriesVarVal == null) {
            selectedSeriesVarVal = [];
          }
          updateSeriesVarVal(id_array[id_array.length - 2], id_array[id_array.length - 1], selectedSeriesVarVal);
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


      $("#series_var_val_y1_1").multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        click: function () {
          updateSeriesHist();
        },
        checkAll: function () {
          updateSeriesHist();
        },
        uncheckAll: function () {
          updateSeriesHist();
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
        addSeriesVarHist();
      });


      $(".remove_var").button({
        icons: {
          primary: "ui-icon-trash"
        },
        text: false,
        disabled: true
      }).click(function () {
        removeSeriesVarCommon($(this).attr('id'));
        updateSeriesHist();
      });
      $(".remove_fixed_var").button({
        icons: {
          primary: "ui-icon-trash"
        },
        text: false
      }).click(function () {
        removeFixedVar($(this).attr('id'));
      });
      $('#is_taylor_voc').buttonset();
           $('#is_show_gamma').buttonset();
           $("input:radio[name='taylor_voc']").change(function(){
                   var _val = $(this).val();
                   if(_val === 'true'){
                     $('#is_show_gamma').buttonset( "enable" );
                   }else{
                     $('input[name="show_gamma"][value="false"]').prop('checked', true).button('refresh');
                     $('#is_show_gamma').buttonset( "disable" );
                   }
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
        updateForecastVariables();
        updateSeriesVarValHist(1, []);
        $.each(fix_var_value_to_title_stat_map, function (key, val) {
          $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
        });
        $("input[name=taylor_voc][value=true]").prop('checked', true);
      }


    });


  </script>
</head>
<body>
<div style="overflow: auto;">


  <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Y1 Dependent (Forecast)
      Variables:
      <button class="help-button" style="float: right;"
              alt="dep">Help
      </button>
    </div>
    <table id="dependent_var_table_y1">

      <tr>

        <td>
          <select id="fcst_var_y1_1">

          </select>
        </td>


      </tr>
    </table>


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

      </tr>
    </table>
    <button id="add_fixed_var" style="margin-top:5px;">Fixed Value</button>

    <br/>
    <label for="txtPlotCond">Plot Cond</label> <input type="text" value=""
                                                      id="txtPlotCond"
                                                      style="width: 95%">
  </div>

  <div class="ui-widget-content ui-widget-content-plot ui-corner-all">
    <div class="ui-widget-header-plot">Display
      <button class="help-button" style="float: right;" alt="taylor_voc">Help
      </button>
    </div>
    <table style="width: 100%;">
      <tr>
        <td>
          <div id="is_taylor_voc">
            Values of correlation
            <input type="radio" name="taylor_voc" value="true"
                   id="true_is_taylor_voc" checked/>
            <label for="true_is_taylor_voc">Positive</label>


            <input type="radio" name="taylor_voc" value="false"
                   id="false_is_taylor_voc"/>
            <label for="false_is_taylor_voc">All</label>
          </div>
        </td>
        <td>
            <div id="is_show_gamma">
              Standard deviation arcs
              <input type="radio" name="show_gamma" value="true"
                     id="true_is_show_gamma" checked/>
              <label for="true_is_show_gamma">Display</label>


              <input type="radio" name="show_gamma" value="false"
                     id="false_is_show_gamma"/>
              <label for="false_is_show_gamma">Hide</label>
            </div>
        </td>
      </tr>
    </table>

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

</div>
</body>
</html>
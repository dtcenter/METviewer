Feature: Plot Box Plot TMP 2m  RMSE

  This scenario plots a box plot 2 meter temperature RMSE over CONUS.

  Background:
    Given I load the app at "http://www.dtcenter.org/met/metviewer/metviewer1.jsp"
    And I select the "mv_scorecard" database
    And the selected database is "mv_scorecard"
    And I choose the plot type "Box"
    And I click the "Y1 Axis variables" tab

  @watch
  Scenario: plotTimeseries
    When I click the "Y1 Dependent" variable menu and wait up to 500 milliseconds
    Then I select the "TMP" variable menu option and wait up to 2000 milliseconds
    And the "Y1 Dependent" variable menu value is "TMP"

    When I click the "Y1 Dependent" attribute menu and wait up to 500 milliseconds
    Then I check the "RMSE" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Y1 Dependent" attribute menu value is "RMSE"

    When I click the "Y1 Series" 1 variable menu and wait up to 500 milliseconds
    Then I select the "MODEL" variable menu option and wait up to 2000 milliseconds
    And the "Y1 Series" 1 variable menu value is "MODEL"

    When I click the "Y1 Series" 1 attribute menu and wait up to 500 milliseconds
    Then I check the "sasctrl_0p25_G218" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Y1 Series" 1 attribute menu value is "sasctrl_0p25_G218"

    Then I click the "Fixed Value" button and wait up to 500 milliseconds
    Then I scroll the plot div to the bottom
    Then I click the "Fixed Value" 1 variable menu and wait up to 500 milliseconds
    Then I select the "VX_MASK" variable menu option and wait up to 2000 milliseconds
    And the "Fixed Value" 1 variable menu value is "VX_MASK"
    Then I scroll the plot div to the top
    When I click the "Fixed Value" 1 attribute menu and wait up to 500 milliseconds
    Then I check the "CONUS" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Fixed Value" 1 attribute menu value is "CONUS"

    Then I click the "Fixed Value" button and wait up to 500 milliseconds
    Then I scroll the plot div to the bottom
    Then I click the "Fixed Value" 2 variable menu and wait up to 500 milliseconds
    Then I select the "FCST_LEV" variable menu option and wait up to 2000 milliseconds
    And the "Fixed Value" 2 variable menu value is "FCST_LEV"
    Then I scroll the plot div to the top
    When I click the "Fixed Value" 2 attribute menu and wait up to 500 milliseconds
    Then I check the "Z2" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Fixed Value" 2 attribute menu value is "Z2"

    Then I click the "Fixed Value" button and wait up to 500 milliseconds
    Then I scroll the plot div to the bottom
    Then I click the "Fixed Value" 3 variable menu and wait up to 500 milliseconds
    Then I select the "INIT_HOUR" variable menu option and wait up to 2000 milliseconds
    And the "Fixed Value" 3 variable menu value is "INIT_HOUR"
    Then I scroll the plot div to the top
    When I click the "Fixed Value" 3 attribute menu and wait up to 500 milliseconds
    Then I check the "00" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Fixed Value" 3 attribute menu value is "00"

    When I click the "Independent Variable" variable menu and wait up to 500 milliseconds
    Then I click the "FCST_LEAD" button and wait up to 500 milliseconds
    And the "Independent Variable" variable menu value is "FCST_LEAD"

    When I click the "Independent Variable" attribute menu and wait up to 500 milliseconds
    Then I check the "60000" attribute menu option check box and wait up to 2000 milliseconds
    Then I check the "120000" attribute menu option check box and wait up to 2000 milliseconds
    Then I check the "180000" attribute menu option check box and wait up to 2000 milliseconds
    Then I check the "240000" attribute menu option check box and wait up to 2000 milliseconds
    Then I check the "300000" attribute menu option check box and wait up to 2000 milliseconds
    Then I check the "360000" attribute menu option check box and wait up to 2000 milliseconds
    And I click the x button and wait up to 500 milliseconds
    Then the "Independent Variable" attribute menu value is "60000, 120000, 180000, 240000, 300000, 360000"

    When I click the Generate Plot button and wait up to 5000 milliseconds
#    Then debug
    Then a plot should appear


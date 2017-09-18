Feature: Plot Timeseries

  This scenario plots a timeseries.

  Background:
    Given I load the app at "http://hwp-metvdev.gsd.esrl.noaa.gov:8080/metviewer/metviewer1.jsp"
    And I select the "mv_global_compare_klh2" database
    And the selected database is "mv_global_compare_klh2"
    And I choose the plot type "Series"
    And I click the "Y1 Axis variables" tab

  @watch
  Scenario: plotTimeseries
    When I click the "Y1 Dependent" variable menu and wait up to 500 milliseconds
    Then I select the "HGT" variable menu option and wait up to 2000 milliseconds
    And the "Y1 Dependent" variable menu value is "HGT"

    When I click the "Y1 Dependent" attribute menu and wait up to 500 milliseconds
    Then I check the "RMSE" attribute menu option check box and wait up to 2000 milliseconds
    Then debug
    And I click the "x" button and wait up to 500 milliseconds
    Then the "Y1 Dependent" attribute menu value is "RMSE"

    When I click the "Y1 Series" variable menu and wait up to 500 milliseconds
    Then I select the "MODEL" variable menu option and wait up to 2000 milliseconds
    And the "Y1 Dependent" variable menu value is "MODEL"

    When I click the "Y1 Series" attribute menu and wait up to 500 milliseconds
    Then I check the "GFS_global" attribute menu option check box and wait up to 2000 milliseconds
    And I click the "x" button and wait up to 500 milliseconds
    Then the "Y1 Series" attribute menu value is "GFS_global"

    When I click the "Independent Variable" variable menu and wait up to 500 milliseconds
    Then I select the "FCST_INIT_BEG" variable menu option and wait up to 2000 milliseconds
    And the "Independent Variable" variable menu value is "FCST_INIT_BEG"

    When I click the "Independent Variable" attribute menu and wait up to 500 milliseconds
    Then I click the "Check all" button and wait up to 500 milliseconds
    And all "Independent Variable" attribute menu values should be checked
    And I click the "x" button and wait up to 500 milliseconds

    When I click the "Aggregation statistics" radio button and wait up to 500 milliseconds
    Then the "Aggregation statistics" radio button is selected

    When I click the "Generate Plot" button and wait up to 5000 milliseconds
    Then a plot should appear


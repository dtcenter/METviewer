Feature: Plot Timeseries

  This scenario plots a timeseries.

  Background:
    Given I load the app

  @watch
  Scenario: plotTimeseries
    When I click the "Database" menu and wait up to 500 milliseconds
    Then the "Database" drop-down menu appears
    And I select the "mv_global_compare_klh2" menu option and wait up to 2000 milliseconds
    Then the "Database" menu value is "mv_global_compare_klh2"

    When I click the "Y1 Dependent" menu and wait up to 500 milliseconds
    Then the "Y1 Dependent" drop-down menu appears
    And I select the "HGT" menu option and wait up to 2000 milliseconds
    Then the "Y1 Dependent" menu value is "HGT"

    When I click the "Select attribute stat" menu and wait up to 500 milliseconds
    Then the "Select attribute stat" drop-down menu appears
    And I check the "RMSE" menu option check box and wait up to 2000 milliseconds
    Then the "Select attribute stat" menu value is "RMSE"

    When I click the "Y1 Series" menu and wait up to 500 milliseconds
    Then the "Y1 Series" drop-down menu appears
    And I select the "MODEL" menu option and wait up to 2000 milliseconds
    Then the "Y1 Dependent" menu value is "MODEL"

    When I click the "Select value" menu and wait up to 500 milliseconds
    Then the "Select value" drop-down menu appears
    And I check the "GFS_global" menu option check box and wait up to 2000 milliseconds
    Then the "Select value" menu value is "GFS_global"

    When I click the "Independent Variable" menu and wait up to 500 milliseconds
    Then the "Independent Variable" drop-down menu appears
    And I select the "FCST_INIT_BEG" menu option and wait up to 2000 milliseconds
    Then the "Independent Variable" menu value is "FCST_INIT_BEG"

    #This is the second menu with that name
    When I click the "Select value" menu and wait up to 500 milliseconds
    Then the "Select value" drop-down menu appears
    And I click the "Check all" button and wait up to 500 milliseconds
    Then all "Select value" menu values should be checked
    And I click the "x" button and wait up to 500 milliseconds
    Then the "Select value" drop-down menu is not visible

    When I click the "Aggregation statistics" radio button and wait up to 500 milliseconds
    Then the "Aggregation statistics" radio button is selected

    When I click the "Generate Plot" button and wait up to 5000 milliseconds
    Then a plot should appear


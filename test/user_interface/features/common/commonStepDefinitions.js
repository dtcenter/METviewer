module.exports = function () {
    "use strict";   //(see https://www.w3schools.com/js/js_strict.asp)

    this.Given(/^I visit "([^"]*)"$/, function (url) {
        browser.url(url);
    });

    this.Given(/^the element with the id "([^"]*)" is visible$/, function (elemId) {
        browser.isVisible("[id='" + elemId + "']");
    });

    this.When(/^I enter "([^"]*)" into the element with id "([^"]*)"$/, function (text, elemId) {
        browser.setValue("[id='" + elemId + "']",text);
    });

    this.When(/^I click the input with the text "([^"]*)" and wait for (\d+) milliseconds$/, function (buttonText, millis) {
        browser.execute(function (valueText) {
            var inputs = document.getElementsByTagName("input");
            for(var x = 0; x < inputs.length; x++) {
                if (inputs[x].value == valueText) {
                    inputs[x].click();
                    break;
                }
            }
        }, buttonText);
        browser.pause(millis);
    });

    this.Then(/^I should have a page title "([^"]*)"$/, function (title) {
        var titleText = browser.getTitle();
        assert.equal(titleText,title);
    });

    this.Then(/^I click the link with the text "([^"]*)"$/, function (text) {
        browser.click("a=" + text);
    });




    //--------------------------------------------------------------------------------------------------------


    const menuIdNames = {
        "Y1 Dependent" : "dependent_var_table_y1",
        "Y1 Series" : "series_var_table_y1",
        "Independent Variable" : "indy_var_table"
    };

    this.Given(/^I load the app at "([^"]*)"$/, function (url) {
        browser.url(url);
        browser.waitForVisible('button*=Generate Plot', 10000);

    });

    this.Given(/^I select the "([^"]*)" database$/, function (databaseName) {
        browser.click('.ui-layout-container .ui-layout-pane.ui-layout-pane-north .toolbar.ui-widget .ui-multiselect.ui-widget.ui-state-default.ui-corner-all');
        browser.scroll("span=" + databaseName);
        browser.click("span=" + databaseName);
    });

    this.Given(/^the selected database is "([^"]*)"$/, function (databaseName) {
        var datText = browser.getText('.ui-layout-container .ui-layout-pane.ui-layout-pane-north .toolbar.ui-widget .ui-multiselect.ui-widget.ui-state-default.ui-corner-all');
        assert(datText === databaseName, "database " + databaseName + " does not appear to be selected.");
    });

    this.Given(/^I choose the plot type "([^"]*)"$/, function (plotType) {
        browser.click('*=' + plotType);
    });

    this.Given(/^I click the "([^"]*)" tab$/, function (tabName) {
        browser.click('*=' + tabName);
    });

    this.When(/^I click the "([^"]*)" variable menu and wait up to (\d+) milliseconds$/, function (menuId, millis) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            browser.scroll('table#' + menuName + ' td:nth-child(1) > button[type="button"]');
            browser.click('table#' + menuName + ' td:nth-child(1) > button[type="button"]');
        } else {
            browser.scroll('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
            browser.click('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        }
    });

    this.When(/^I click the "([^"]*)" attribute menu and wait up to (\d+) milliseconds$/, function (menuId, millis) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            browser.scroll('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
            browser.click('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        } else {
            browser.scroll('table#' + menuName + ' td:nth-child(3) > button[type="button"]');
            browser.click('table#' + menuName + ' td:nth-child(3) > button[type="button"]');
        }
    });

    this.Then(/^I select the "([^"]*)" variable menu option and wait up to (\d+) milliseconds$/, function (varName, millis) {
        var visibles = browser.isVisible("span=" + varName);
        if (visibles.length > 1) {
            var visIndex = visibles.findIndex(function (e) {
                return e == true
            });
            var elems = browser.elements("span=" + varName);
            var visElem = elems.value[visIndex];
            visElem.scroll();
            visElem.click();
        } else {
            browser.scroll("span=" + varName);
            browser.click("span=" + varName);
        }
    });

    this.Then(/^the "([^"]*)" variable menu value is "([^"]*)"$/, function (menuId, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(1) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
    });

    this.Then(/^the "([^"]*)" attribute menu value is "([^"]*)"$/, function (menuId, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(3) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
    });

    this.Then(/^I check the "([^"]*)" attribute menu option check box and wait up to (\d+) milliseconds$/, function (varName, millis) {
        var visibles = browser.isVisible("span=" + varName);
        if (visibles.length > 1) {
            var visIndex = visibles.findIndex(function (e) {
                return e == true
            });
            var elems = browser.elements("span=" + varName);
            var visElem = elems.value[visIndex];
            visElem.scroll();
            visElem.click();
        } else {
            browser.scroll("span=" + varName);
            browser.click("span=" + varName);
        }
    });

    this.Then(/^I click the x button and wait up to (\d+) milliseconds$/, function (millis) {
        var visibles = browser.isVisible('.ui-icon.ui-icon-circle-close');
        var visIndex = visibles.findIndex(function(e){
            return e==true
        });
        var elems = browser.elements('.ui-icon.ui-icon-circle-close');
        var visElem = elems.value[visIndex];
        visElem.click();
    });

    this.Then(/^I click the "([^"]*)" button and wait up to (\d+) milliseconds$/, function (buttonName, millis) {
        var visibles = browser.isVisible("span=" + buttonName);
        var visIndex = visibles.findIndex(function(e){
            return e==true
        });
        var elems = browser.elements("span=" + buttonName);
        var visElem = elems.value[visIndex];
        visElem.click();
    });

    this.When(/^I click the Aggregation Statistics radio button and wait up to (\d+) milliseconds$/, function (millis) {
        browser.scroll("[id='aggregation_statistics_label']");
        browser.click("[id='aggregation_statistics_label']");
    });

    this.When(/^I click the Generate Plot button and wait up to (\d+) milliseconds$/, function (millis) {
        browser.scroll("[id='generate_plot']");
        browser.click("[id='generate_plot']");
    });

    this.Then(/^a plot should appear$/, function () {
        browser.waitForVisible("[id='modal']", 1000);
        browser.waitForVisible("[id='modal']", 20000, true);
    });

    this.Then(/^debug$/, {timeout: 480 * 1000}, function () {
        browser.debug();
    });

    this.Then(/^I pause for (\d+) milliseconds$/, function (millis) {
        browser.pause(millis);
    });
};

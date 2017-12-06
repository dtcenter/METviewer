module.exports = function () {
    "use strict";   //(see https://www.w3schools.com/js/js_strict.asp)
    var step = process.env['chimp.step'];
    var pauseAfterPlot = process.env['chimp.pauseAfterPlot'];
    if ( step && isNaN(step)) {
        step=1;
        console.log ("step value is not a number setting it to 1 second");
    }
    if (step) {
        step = step;
    }
    const stepIt = function() {
        if (step) {
            console.log("debugging for " + step  + " seconds: enter .exit to continue sooner");
            browser.debug();
        }
    }

    this.Given(/^I visit "([^"]*)"$/, function (url) {
        browser.url(url);
        stepIt();
    });

    this.Given(/^the element with the id "([^"]*)" is visible$/, function (elemId) {
        browser.isVisible("[id='" + elemId + "']");
        stepIt();
    });

    this.When(/^I enter "([^"]*)" into the element with id "([^"]*)"$/, function (text, elemId) {
        browser.setValue("[id='" + elemId + "']",text);
        stepIt();
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
        stepIt();
    });

    this.Then(/^I should have a page title "([^"]*)"$/, function (title) {
        var titleText = browser.getTitle();
        assert.equal(titleText,title);
        stepIt();
    });

    this.Then(/^I click the link with the text "([^"]*)"$/, function (text) {
        browser.click("a=" + text);
        stepIt();
    });

    this.Then(/^I scroll the plot div to the bottom$/, function () {
        browser.execute(function () {
            var plotDiv = document.getElementById('plot_config');
            plotDiv.scrollTop = plotDiv.scrollHeight;
        });
        stepIt();
    });

    this.Then(/^I scroll the plot div to the top$/, function () {
        browser.execute(function () {
            var plotDiv = document.getElementById('plot_config');
            plotDiv.scrollTop = 0;
        });
        stepIt();
    });

    this.Then(/^I scroll the plot div to the middle$/, function () {
        browser.execute(function () {
            var plotDiv = document.getElementById('plot_config');
            plotDiv.scrollTop = plotDiv.scrollHeight / 2;
        });
        stepIt();
    });

    //--------------------------------------------------------------------------------------------------------

    const menuIdNames = {
        "Y1 Dependent" : "dependent_var_table_y1",
        "Y1 Series" : "series_var_table_y1",
        "Independent Variable" : "indy_var_table",
        "Fixed Value" : "fixed_var_table",
        "Statistics" : "aggregation_statistics"
    };

    /* translate literal names to CSS selectors.
        CSS selectors can b determined with the debugger. Rigth click an element and copy the CSS path.
     */
    const menuCSS = {
        "Summary Statistics Button":"#calculations_statistics > table > tbody > tr > td:nth-child(1) > button",
        "Aggregation Statistics Button": "#aggregation_statistics > table > tbody > tr:nth-child(1) > td:nth-child(1) > button"
    };

    this.Given(/^I load the app at "([^"]*)"$/, function (url) {
        browser.url(url);
        browser.waitForVisible('button*=Generate Plot', 10000);
        stepIt();
    });

    this.Given(/^I select the "([^"]*)" database$/, function (databaseName) {
        browser.click('.ui-layout-container .ui-layout-pane.ui-layout-pane-north .toolbar.ui-widget .ui-multiselect.ui-widget.ui-state-default.ui-corner-all');
        browser.scroll("span=" + databaseName);
        browser.click("span=" + databaseName);
        stepIt();
    });

    this.Given(/^the selected database is "([^"]*)"$/, function (databaseName) {
        var datText = browser.getText('.ui-layout-container .ui-layout-pane.ui-layout-pane-north .toolbar.ui-widget .ui-multiselect.ui-widget.ui-state-default.ui-corner-all');
        assert(datText === databaseName, "database " + databaseName + " does not appear to be selected.");
        stepIt();
    });

    this.Given(/^I choose the plot type "([^"]*)"$/, function (plotType) {
        browser.click('*=' + plotType);
        browser.pause(2000);
        stepIt();
    });

    this.Given(/^I click the "([^"]*)" tab$/, function (tabName) {
        browser.click('*=' + tabName);
        stepIt();
    });

    this.When(/^I click the "([^"]*)" element by CSS selector and wait up to (\d+) milliseconds$/, function (menuCSSReference, millis) {
        browser.click(menuCSS[menuCSSReference]);
        stepIt();
    });

    this.Then(/^the "([^"]*)" element value by CSS selector is "([^"]*)"$/, function (menuCSSReference, value) {
        var text = browser.getText(menuCSS[menuCSSReference]);
        assert(text === value, "value " + value + " does not appear to be selected.");
        stepIt();
    });

    this.When(/^I click the "([^"]*)" variable menu and wait up to (\d+) milliseconds$/, function (menuId, millis) {
        var menuName = menuIdNames[menuId];
        var target = "";
        if (menuId === "Independent Variable") {
            target = 'table#' + menuName + ' td:nth-child(1) > button[type="button"]';
        } else {
            target = 'table#' + menuName + ' td:nth-child(2) > button[type="button"]';
        }
        browser.scroll(target);
        browser.click(target);
        stepIt();
    });

    this.When(/^I click the "([^"]*)" (\d+) variable menu and wait up to (\d+) milliseconds$/, function (menuId, itemNumber, millis) {
        var menuName = menuIdNames[menuId];
        var target = "";
        if (menuId === "Independent Variable") {
            target = 'table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(1) > button[type="button"]';
        } else {
            target = 'table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(2) > button[type="button"]';
        }
        browser.scroll(target);
        browser.click(target);
        stepIt();
    });

    this.When(/^I click the "([^"]*)" attribute menu and wait up to (\d+) milliseconds$/, function (menuId, millis) {
        var menuName = menuIdNames[menuId];
        var target = "";
        if (menuId === "Independent Variable") {
            target = 'table#' + menuName + ' td:nth-child(2) > button[type="button"]';
        } else {
            target = 'table#' + menuName + ' td:nth-child(3) > button[type="button"]';
        }
        browser.scroll(target);
        browser.click(target);
        stepIt();
    });

    this.When(/^I click the "([^"]*)" (\d+) attribute menu and wait up to (\d+) milliseconds$/, function (menuId, itemNumber, millis) {
        var menuName = menuIdNames[menuId];
        var target = "";
        if (menuId === "Independent Variable") {
            target = 'table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(2) > button[type="button"]';
        } else {
            target = 'table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(3) > button[type="button"]';
        }
        browser.scroll(target);
        browser.click(target);
        stepIt();
    });

    this.Then(/^I select the "([^"]*)" variable menu option and wait up to (\d+) milliseconds$/, function (varName, millis) {
        varName = varName.trim();
        var visibles = browser.isVisible("span=" + varName);
        if (! (visibles instanceof Array)) {visibles = [visibles];}
        var trueVisibles = visibles.filter(function(e){return e === true;});
        if (visibles.length > 1 && trueVisibles.length > 0) {
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
        stepIt();
    });

    this.Then(/^the "([^"]*)" variable menu value is "([^"]*)"$/, function (menuId, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(1) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
        stepIt();
    });

    this.Then(/^the "([^"]*)" (\d+) variable menu value is "([^"]*)"$/, function (menuId, itemNumber, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(1) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName +   ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(2) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
        stepIt();
    });

    this.Then(/^the "([^"]*)" attribute menu value is "([^"]*)"$/, function (menuId, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(2) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName + ' td:nth-child(3) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
        stepIt();
    });

    this.Then(/^the "([^"]*)" (\d+) attribute menu value is "([^"]*)"$/, function (menuId, itemNumber, menuValue) {
        var menuName = menuIdNames[menuId];
        if (menuId === "Independent Variable") {
            var menuText = browser.getText('table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(2) > button[type="button"]');
        } else {
            var menuText = browser.getText('table#' + menuName +  ' tr:nth-child(' + itemNumber + ')' + ' td:nth-child(3) > button[type="button"]');
        }
        assert(menuText === menuValue, "menu text " + menuValue + " does not appear to be selected.");
        stepIt();
    });

    this.Then(/^I check the "([^"]*)" attribute menu option check box and wait up to (\d+) milliseconds$/, function (varName, millis) {
        varName = varName.trim();
        var visibles = browser.isVisible("span=" + varName);
        if (! (visibles instanceof Array)) {visibles = [visibles];}
        var trueVisibles = visibles.filter(function(e){return e === true;});
        if (visibles.length > 1 && trueVisibles.length > 0) {
            var visIndex = visibles.findIndex(function (e) {
                return e == true
            });
            var elems = browser.elements("span*=" + varName);
            var visElem = elems.value[visIndex];
            visElem.scroll();
            visElem.click();
        } else {
            browser.scroll("span=" + varName);
            browser.click("span=" + varName);
        }
        stepIt();
    });

    this.Then(/^I click the x button and wait up to (\d+) milliseconds$/, function (millis) {
        var visibles = browser.isVisible('.ui-icon.ui-icon-circle-close');
        var visIndex = visibles.findIndex(function(e){
            return e==true
        });
        var elems = browser.elements('.ui-icon.ui-icon-circle-close');
        var visElem = elems.value[visIndex];
        visElem.click();
        stepIt();
    });

    this.Then(/^I click the "([^"]*)" button and wait up to (\d+) milliseconds$/, function (buttonName, millis) {
        var visibles = browser.isVisible("span*=" + buttonName);
        if (! (visibles instanceof Array)) {visibles = [visibles];}
        var trueVisibles = visibles.filter(function(e){return e === true;});
        if (visibles.length > 1 && trueVisibles.length > 0) {
            var visIndex = visibles.findIndex(function (e) {
                return e == true
            });
            var elems = browser.elements("span*=" + buttonName);
            var visElem = elems.value[visIndex];
            visElem.scroll();
            visElem.click();
        } else {
            browser.scroll("span=" + buttonName);
            browser.click("span=" + buttonName);
        }
        stepIt();
    });

    this.When(/^I click the Aggregation Statistics radio button and wait up to (\d+) milliseconds$/, function (millis) {
        browser.scroll("[id='aggregation_statistics_label']");
        browser.click("[id='aggregation_statistics_label']");
        stepIt();
    });

    this.When(/^I click the Summary radio button and wait up to (\d+) milliseconds$/, function (millis) {
        browser.scroll("[id='calculations_statistics_label']");
        browser.click("[id='calculations_statistics_label']");
        stepIt();
    });

    this.When(/^I click the Generate Plot button and wait up to (\d+) milliseconds$/, function (millis) {
        browser.scroll("[id='generate_plot']");
        browser.click("[id='generate_plot']");
        stepIt();
    });

    this.Then(/^a plot should appear$/, function () {
        browser.waitForVisible("[id='modal']", 1000);
        browser.waitForVisible("[id='modal']", 30000, true);
        if (pauseAfterPlot) {
            browser.pause(pauseAfterPlot * 1000);
        }
        stepIt();
    });

    this.Then(/^debug$/, {timeout: 480 * 1000}, function () {
            browser.debug();
    });

    this.Then(/^I pause for (\d+) milliseconds$/, function (millis) {
        browser.pause(millis);
    });
};

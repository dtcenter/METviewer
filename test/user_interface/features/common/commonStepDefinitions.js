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



    this.Given(/^I load the app$/, function () {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.When(/^I click the "([^"]*)" menu and wait up to (\d+) milliseconds$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^the "([^"]*)" drop\-down menu appears$/, function (arg1) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^I select the "([^"]*)" menu option and wait up to (\d+) milliseconds$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^the "([^"]*)" menu value is "([^"]*)"$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^I check the "([^"]*)" menu option check box and wait up to (\d+) milliseconds$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^I click the "([^"]*)" button and wait up to (\d+) milliseconds$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^all "([^"]*)" menu values should be checked$/, function (arg1) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^the "([^"]*)" drop\-down menu is not visible$/, function (arg1) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.When(/^I click the "([^"]*)" radio button and wait up to (\d+) milliseconds$/, function (arg1, arg2) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^the "([^"]*)" radio button is selected$/, function (arg1) {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });

    this.Then(/^a plot should appear$/, function () {
        // Write code here that turns the phrase above into concrete actions
        return 'pending';
    });
};

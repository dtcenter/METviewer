Economic Cost/Loss Value Plots
==============================

Description
-----------

The Economic Cost Loss Value statistic is sometime also called the Relative value score (Richardson, 2000; Wilks, 2001). This plot produces the relative value curve for deterministic forecasts based on counts in a 2x2 contingency table along with the expected cost-to-loss ratio. 

This information can help the user decide, for a cost/loss ratio C/L for taking action based on a forecast, what is the relative improvement in economic value between climatalogical and perfect information. The relative value is a skill score based on expected cost, with (sample) climatology as the reference forecast. Because the cost/loss ratio is different for different users of forecasts, the value is plotted as a function of cost to loss.

The ECLV score can range from \text{-\ensuremath{\infty}} to 1.

Like ROC diagrams, it gives information that can be used in decision making.

How-To
-------

Note: The ECLV plot can only be produced using output from the MET ECLV line type. If your database lacks this information, a different plot type should be selected. 

Selection of options to produce the ECLV plot proceeds approxately counter-clockwise around the METviewer window.

1. Select the desired database from the “Select databases” pulldown menu at the top margin of the METviewer window.

2. There are a number of tabs just under the database pulldown menu. Select the ‘Eclv’ tab.

3. Select the desired series variable to calculate statistics for in the “Series Variables” tab. Press the “+ Series Variable” button to reveal two pulldown menus. The first pulldown menu in the lists the categories available in the selected dataset. The second pulldown menu allows you to select the value of that category.

4. For a ECLV plot, the forecast variable (“FCST_VAR”) must be selected. This is found in the "Specialized Plot Fixed Values" section. In the example below, the forecast variable is “”. 
   
5. It usually does not make sense to mix statistic for different groups. The desired group to calculate statistics over can be specified in the “Specialized Plot Fixed Values” section. A single domain (category: “VX_MASK”, value: “CONUS”) is chosen. If multiple domains or threshold were chosen, the statistics would be a summary of all of those cases together, which may not always be desired.


Examples
--------

The figure below shows an ECLV plot. In this example, three different forecasting systems are used to predict precipitation at two different thresholds. The economic value peaks at about 0.6 for all forecasts, but at a cost to loss ratio of about 0.2 for half of the forecasts, and near 0.35 for the remaining half. Values of the ECLV are negative for both very low and very high values of the cost to loss ratio. This makes sense, since low values of C/L indiate either a very small cost to protect, very high losses, or both. In these cases, it probably makes sense to protect regardless of the forecast. At the other end, the cost to protect nears the amount of the potential loss. In that case, in probably makes sense to do nothing, regardless of the forecast, so the economic value of the forecast is negative. Between those extremes, each user can determine their own C/L ratio, risk tolerance, etc. to determine the best forecasting system for their needs. In the example below, many of the forecasts are quite similar, so a user may select from a grouping based on other criteria, such as forecast latency or computational requirements.

.. figure:: econ.png

	    Figure 12.1 Example ECLV plot for three models using two different thresholds.

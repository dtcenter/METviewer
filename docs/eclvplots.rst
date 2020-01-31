Chapter 12 Economic Cost/Loss Value Plots in METviewer
======================================================

12.1 Description
----------------

The Economic Cost Loss Value statistic is sometime also called the Relative value score (Richardson, 2000; Wilks, 2001). This plot produces the relative value curve for deterministic forecasts based on counts in a 2x2 contingency table along with the expected cost-to-loss ratio. 

This information can help the user decide, for a cost/loss ratio C/L for taking action based on a forecast, what is the relative improvement in economic value between climatalogical and perfect information. The relative value is a skill score based on expected cost, with (sample) climatology as the reference forecast. Because the cost/loss ratio is different for different users of forecasts, the value is plotted as a function of cost to loss.

The ECLV score can range from \text{-\ensuremath{\infty}} to 1.

Like ROC diagrams, it gives information that can be used in decision making.

12.2 Details
------------

The ECLV plot can only be produced using output from the MET ECLV line type. If your database lacks this information, a different plot type should be selected. 

12.3 Examples
-------------

The figure below shows an ECLV plot. In this example, three different forecasting systems are used to predict precipitation at two different thresholds. The economic value peaks at about 0.6 for all forecasts, but at a cost to loss ratio of about 0.2 for half of the forecasts, and near 0.35 for the remaining half. Values of the ECLV are negative for both very low and very high values of the cost to loss ratio. This makes sense, since low values of C/L indiate either a very small cost to protect, very high losses, or both. In these cases, it probably makes sense to protect regardless of the forecast. At the other end, the cost to protect nears the amount of the potential loss. In that case, in probably makes sense to do nothing, regardless of the forecast, so the economic value of the forecast is negative. Between those extremes, each user can determine their own C/L ratio, risk tolerance, etc. to determine the best forecasting system for their needs. In the example below, many of the forecasts are quite similar, so a user may select from a grouping based on other criteria, such as forecast latency or computational requirements.

.. figure:: econ.png

	    Figure 12.1 Example ECLV plot for three models using two different thresholds.

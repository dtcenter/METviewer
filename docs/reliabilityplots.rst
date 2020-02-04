Chapter 7 Reliability Plots in METviewer
========================================

7.1 Description
---------------

Reliability (or attributes) diagrams are used to show the conditional bias of probabilistic forecasts. If a probability forecast is reliable, then the relative frequency of events for a certain category of probability forecasts will be approximately the same as the forecast probability. In other words, when a 50% probability of precipitation is forecast, approximately half of the corresponding observations should indicate that precipitation fell. 

7.2 Details
-----------

Selection of options to produce your plot proceeds approxately counter-clockwise around the METviewer window. Select your database from the pulldown menus at the top margin of the METviewer window. Make sure the Reliability tab is selected near the top left, just under the database pulldown menu. The statistics for this type of plot are fixed, so you much only choose your set of cases. The reliability diagram only makes sense for probability forecasts, but these can be from ensemble forecasts or from a traditional forecast. There are several reference lines on the reliability diagram. 

This plot has three special options:

Event histograms: The histograms allow users to see how often the forecast falls within each probability bin. The sample sizes in each bin are typically different, and some may be too small for conclusions. They also allow users to judge the sharpness of the forecasts. 

Skill line: ??Needs a description??

Reference line: This line (typically dashed) denotes the sample climatology or base rate, the frequency of the event. 

Generate Plot button at top of METviewer window with the red text. 

7.3 Examples
------------

The reliability diagram groups the forecasts into bins according to the issued probability (x axis). The frequency with which the event was observed to occur for this sub-group of forecasts is then plotted against the y axis. For perfect reliability the forecast probability and the frequency of occurrence should be equal, and the plotted points should lie on the diagonal (by default a solid grey line in the figure). In other words, when a 50% probability of precipitation is forecast, approximately half of the corresponding observations should indicate that precipitation fell. 

In the figure the reliability curves have positive slope, indicating that as the forecast probability of the event occurring increases, so does the likelihood of observing the event. The forecasts therefore have some reliability. However, the slope is much less than the diagonal, indicating conditional bias or a lack of reliability. In this example, when forecast probability of precipitation is equal to 55% the actual chance of observing the event is closer to 35%.

Reliability diagrams can be used to make adjustments to the conditional bias of the forecast probabilities.

The histograms show the relative frequency with which the event has been predicted (typically over the reference period and at all gridpoints) with different levels of probability. In the example the majority of forecasts predict low probabilities of precipitation (near the climatological probability of ~9%). The forecast system is also capable of predicting relatively high probabilities of the event (e.g. greater than 40%), but such forecasts are less common.

.. figure:: reliability.png

	    Figure 7.1 Example reliability (attributes diagram) with histogram of observation counts and bootstrap confidence intervals.

??Several optional   Did you want more figures?? 

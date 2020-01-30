Chapter 9 Taylor Diagrams in METviewer
======================================

9.1 Description
---------------

Taylor diagrams are used to show a relationship between continuous statistics, (Taylor, 2001). It is used to quantify the degree of correspondence between the forecasts and observations with respect to three continuous statistics: the Pearson correlation coefficient, the root-mean-square error (RMSE), and the standard deviation. This output can be produced by many of the MET tools (Point Stat, Grid Stat, etc.) via the continuous statistics line type. In METviewer, continuous statistics can be accumulated over some time period but kept separate for different models, lead times, observation sources, domains, etc. 

9.2 Details
-----------

Selection of options to produce the plot proceeds approxately counter-clockwise around the METviewer window. Select the database from the pulldown menus at the top margin of the METviewer window. Make sure the “Taylor” tab is selected near the top left, just under the database pulldown menu. The statistics for this type of plot are fixed. Selection of the dependent variable, fixed values, and series values are as for other METviewer plots.

There are several reference curves and lines appear on the Taylor diagram. Near the bottom left, a slider bar allows the user to select whether or not to display the standard deviation arcs. Additionally, the user can select to show the plot as a quarter circle, as in the example below, which includes only positive correlations, or as a half-circle, which includes both positive and negative correlations. 

The distance between each forecast and the observation on the graph is a measure of how realistically each forecast reproduces that observation. For each forecast, three statistics are plotted: the Pearson correlation coefficient (Wilks, 2011; a measure of pattern similarity between the forecast and observed fields) is related to the azimuthal angle; the normalized (bias-corrected) RMSE in the forecast field is proportional to the distance from the point on the x-axis identified as “observed”. The standard deviation of the forecast is proportional to the radial distance from the origin. The means of the fields are subtracted out before computing their second-order statistics (e.g. bias-corrected RMSE), so the diagram does not provide information about biases. Rather, it characterizes the pattern error.

So, better forecasts lie closer on the Taylor diagram to observation point, while worse forecasts lie further away. 

9.3 Examples
------------

The example Taylor diagram below shows the temperature correlation and standard deviation for several models, two lead times, and two types of observations. Some of the forecasts with a 0 hour lead (i.e. the model analysis) are quite close to the ideal value of 1, 1. This corresponds to perfect correlation and matching standard deviations between model and observation. Some pink, green, and orange points lie closer to 0.95 correlation with slightly above or below 1 standard deviation values. 

.. figure:: plot_20181106_144318.png

	    Figure 9.1 Example Taylor diagram.

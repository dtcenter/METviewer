Taylor Diagrams
===============

Description
-----------

Taylor diagrams are used to show a relationship between continuous statistics, (Taylor, 2001). This type of plot is  used to quantify the degree of correspondence between the forecasts and observations with respect to three continuous statistics: the Pearson correlation coefficient, the root-mean-square error (RMSE), and the standard deviation. This output can be produced by many of the MET tools (Point Stat, Grid Stat, etc.) via the continuous statistics line type. In METviewer, continuous statistics can be accumulated over some time period but kept separate for different models, lead times, observation sources, domains, etc. 

How-To
-------

Selection of options to produce the Taylor diagram proceeds approxately counter-clockwise around the METviewer window.

1. Select the desired database from the “Select databases” pulldown menu at the top margin of the METviewer window.

2. There are a number of tabs just under the database pulldown menu. Select the ‘Taylor’ tab.

3. Select the dependent variable to calculate statistics for in the “Y1 Dependent (Forecast)  Variables” tab. The first pulldown menu in the “Y1 Dependent (Forecast) Variables” section lists the variables available in the selected database. 
  
   for in the “Series Variables” tab. Press the “+ Series Variable” button to reveal two pulldown menus. The first pulldown menu in the lists the categories available in the selected dataset. The second pulldown menu allows you to select the value of that category. The reliability diagram only makes sense for probability forecasts. These can be from ensemble forecasts or from a traditional forecast.

It usually does not make sense to mix statistic for different groups. The desired group to calculate statistics over can be specified in the “Specialized Plot Fixed Values” section. For a reliability diagram, the forecast variable (“FCST_VAR”) must be selected. In the example below, the forecast variable is “APCP_24_ENS_FREQ_gt12.700”. A single domain (category: “VX_MASK”, value: “EAST”) is chosen. If multiple domains or threshold were chosen, the statistics would be a summary of all of those cases together, which may not always be desired.

Choose whether or not to display the reliability event histogram in the “Reliability Event Histogram” section. Event histograms allow the user to see how often the forecast falls within each probability bin. The sample sizes in each bin are typically different, and some may be too small for conclusions. They also allow the user to judge the sharpness of the forecasts.

There are two checkboxes in the “Reliability Event Histogram” section.

Skill line: The skill, or reliability, of a forecast is indicated by the proximity of the plotted curve to the diagonal skill line. The deviation from the diagonal gives the conditional bias. If the curve lies below the line, this indicates over-forecasting (probabilities too high); points above the line indicate under-forecasting (probabilities too low).

Reference line: This line (typically dashed) denotes the sample climatology or base rate, the frequency of the event.

Select what summary curve to display (if any) in the “Summary Curve” section.

Adjust the aggregation options, if desired, in the “Aggregation options” section.

Now enough information has been entered to produce a graph. To do this, click the “Generate Plot” button at the top of the METviewer window (this is in red text). Typically, if a plot is not produced, it is because the database selected does not contain the correct type of data. Also, it is imperative to check the data used for the plot by selecting the “R data” tab on the righthand side, above the plot area. The data from the database is being used to calculate the statistics is listed in this tab. This tab should be checked to avoid the accidental accumulation of inappropriate database lines. For example, it does not make sense to accumulate statistics over different domains, thresholds, models, etc.

There are many other options for plots, but these are the basics.


There are several reference curves and lines appear on the Taylor diagram. Near the bottom left, a slider bar allows the user to select whether or not to display the standard deviation arcs. Additionally, the user can select to show the plot as a quarter circle, as in the example below, which includes only positive correlations, or as a half-circle, which includes both positive and negative correlations. 

The distance between each forecast and the observation on the graph is a measure of how realistically each forecast reproduces that observation. For each forecast, three statistics are plotted: the Pearson correlation coefficient (Wilks, 2011; a measure of pattern similarity between the forecast and observed fields) is related to the azimuthal angle; the normalized (bias-corrected) RMSE in the forecast field is proportional to the distance from the point on the x-axis identified as “observed”. The standard deviation of the forecast is proportional to the radial distance from the origin. The means of the fields are subtracted out before computing their second-order statistics (e.g. bias-corrected RMSE), so the diagram does not provide information about biases. Rather, it characterizes the pattern error.

So, better forecasts lie closer on the Taylor diagram to observation point, while worse forecasts lie further away. 

Examples
--------

The example Taylor diagram below shows the temperature correlation and standard deviation for several models, two lead times, and two types of observations. Some of the forecasts with a 0 hour lead (i.e. the model analysis) are quite close to the ideal value of 1, 1. This corresponds to perfect correlation and matching standard deviations between model and observation. Some pink, green, and orange points lie closer to 0.95 correlation with slightly above or below 1 standard deviation values. 

.. figure:: plot_20181106_144318.png

	    Figure 9.1 Example Taylor diagram.

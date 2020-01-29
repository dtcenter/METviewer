Series Plots in METviewer
=========================

Description
-----------

The series plot is the most general of all the METviewer plotting options. A series plot is really just a special case of a scatter plot, where the dependent variable (i.e. Y-axis value) is related from one value to the next of the independent variable (X-axis value). Thus, points are placed into the graphic as on a scatter plot, but the Y-axis values are shown with connecting lines across the X-axis. Examples of data appropriate for this type of plot include verification statistics (on the Y-axis) for different lead times or thresholds (X-axis). The tremendous flexibility of this plot means that the user must specify many options. 

Details
-------

Selection of options to produce your plot proceeds approxately counter-clockwise around the METviewer window. Select your database from the pulldown menus at the top margin of the METviewer window. Make sure the 'Series' tab is selected near the top left, just under the database pulldown menu. Next, you must select the type of MET statistics that will be used to create the plot. In the “Plot Data” menu, the list contains “Stat”, “MODE”, or “MODE-TD”. For details about these types of output statistics in MET, please see the most recent version of the MET Users' Guide (www.dtcenter.org/met/users/docs). The example in the following section shows a series plot of the statistic “Frequency Bias”. 

Users will generally wish to plot more than one line on the plot, and the different lines are selected in the “Y1 Series Variable” dropdown menu. Because it usually does not make sense to mix statistics for different groups, the “Fixed Values” section allows the user to specify exactly the groups of cases to be used in the plot. In the example below, a single domain (“East”) and precipitation threshold value (“>=0.254”) are chosen. If multiple domains or threshold were chosen, the statistics would be a summary of all of those cases together, which in this case would be non-sensical. 

Then the X-axis value is selected in the “Independent Variable” dropdown menu. For a series plot, this is often a date, lead time, or threshold. For a series plot, this variable must be continuous and there should be reason to believe that the statistic on the Y1 axis is connected across the values of the independent variable. If not, a scatter plot would be a more appropriate choice than the series plot.

In the example in the next section, the Y1 dependent variable “Frequency Bias” is plotted for each of the “Y1 Series Variable” ensemble members. 

Finally, the user must select the type of statistics summary from the radio buttons in the “Statistics” section. By default, the median value of all statistics will be plotted. Using the dropdown menu, the mean or sum may be selected instead. Further, aggregated statistics may be selected for certain varieties of statistics, listed in the dropdown menu. Choosing this option will cause a single statistic to be calculated from the individual database lines, then that statistic is plotted. 

There are many other options, but at this point enough information has been provided to produce a graphic. To do so, click the “Generate Plot” button at top of METviewer window with the red text. Typically, if not plot is produced, it is because the database selected does not contain the correct type of data. Also, it is imperative to check the data used for the plot by selected the “R data” tab. This way, accidental accumulation of inappropriate database lines can be avoided. For example, it does not make sense to accumulate statistics over different domains, thresholds, models, etc. 

Examples
--------

The image below shows an example of the plot and set-up options for a series plot in METviewer. This example uses the database “mv_hrrr_sppmp_test” to plot “Stat” output for seven ensemble members. The Frequency bias over the East domain is plotted for 3-hour precipitation accumulation exceeding 0.254 cm. Appropriate titles and labels have been entered in the titles and labels tab shown below the plot. Colors and line formatting are shown across the bottom menu of the plot. The values here are the defaults. 

.. figure:: SeriesPlot_MV_Capture.png

Screen capture of METviewer configured to produce a series plot of frequency bias by lead time for 3-hour accumulated precipitation for seven ensemble members.
	    

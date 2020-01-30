Chapter 8 Performance Diagrams in METviewer
===========================================

8.1 Description
---------------

Performance diagrams are used to show the relationship between categorical statistics, with axes representing detection and success (1 - false alarm) rates (Roebber, 2009). Frequency bias and the critical success index (CSI) are also shown on the diagram. The simplest input to the performance diagram is the MET contingency table statistics (cts) output. This output can be produced by many of the MET tools (Point Stat, Grid Stat, etc.). In METviewer, contingency tables can be accumulated over some time period but kept separate for different models, lead times, observation sources, domains, etc. 

8.2 Details
-----------

Selection of options to produce your plot proceeds approxately counter-clockwise around the METviewer window. Select your database from the pulldown menus at the top margin of the METviewer window. Make sure the “Perf” tab is selected near the top left, just under the database pulldown menu. The statistics for this type of plot are fixed, so you must only choose your set of cases. 

There are several reference lines on the performance diagram. The dashed lines that radiate from the origin are lines of equal frequency bias. Labels for the frequency bias amount are at the end of each line in the margins. The diagonal represents a perfect frequency bias score of 1. Curves of equal Critical Success Index (CSI) connect the top of the plot to the right side. CSI amounts are listed to the right side of the plot, with better values falling closer to the top. 

Generate Plot button at top of METviewer window with the red text. 

8.3 Examples
------------
 
.. figure:: PerfDiag_Example.png

	    Figure 8.1 Example performance diagram.


| ??Several optional ??

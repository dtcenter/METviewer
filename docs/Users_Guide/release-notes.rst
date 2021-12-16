METviewer Release Notes
-----------------------

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version `v4.0.2 <https://github.com/dtcenter/METviewer/releases/tag/v4.0.2>`_ release notes (20211215)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Enhancements:

  * Migrate to log4j 2.16.0 to address security vulnerability `#358 <https://github.com/dtcenter/METviewer/issues/358>`_


Version `v4.0.1 <https://github.com/dtcenter/METviewer/releases/tag/v4.0.1>`_ release notes (20210816)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Bugfixes:

  * Fixed the loading XML error when parsing <date_list> element with <date_offset> `#319 <https://github.com/dtcenter/METviewer/issues/319>`_


Version `v4.0.0 <https://github.com/dtcenter/METviewer/milestone/11>`_ release notes (20200512)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


* Bugfixes:

  * Fixed loading XML for the ROC diagram to the UI `#266 <https://github.com/dtcenter/METviewer/issues/266>`_
  * Fixed a bug when XML with variable indep plot_vals doesn't get loaded to UI correctly `#257 <https://github.com/dtcenter/METviewer/issues/257>`_
  * Fixed Event Equalisation for ROC diagram with multiple series `#256 <https://github.com/dtcenter/METviewer/issues/256>`_
  * Plot all series with different number of i_values on the Hist plot `#254 <https://github.com/dtcenter/METviewer/issues/254>`_
  * Fixed bugs in Docker file: build of 'pingouin' module,  permissions for the executables, mysql user name
  * Fixed the UI bug when the plot formatting menu is inaccessible when the Series formatting menu is pulled up and then down


* New Features:

  * Load the additional climatology column in the ORANK  line type updated for met-10.0.0 `#261 <https://github.com/dtcenter/METviewer/issues/261>`_
  * Load the additional climatology column in the ECNT line type updated for met-10.0.0 `#259 <https://github.com/dtcenter/METviewer/issues/259>`_
  * Add 'Equalize fixed var" option to scorecard `#235 <https://github.com/dtcenter/METviewer/issues/235>`_
  * Add 'cov_thresh' to the menu lists in GUI `#206 <https://github.com/dtcenter/METviewer/issues/206>`_
  * LODDS, ODDS, ORSS, SEDI, SEDS, EDI, EDS statistics are available for aggregation logic `#252 <https://github.com/dtcenter/METviewer/issues/252>`_
  * Support to reverse the connection order in ROC curve
  * Use the circular bootstrap as a default method instead of the regular
  * New Equivalence Testing Bounds plot
  * In the scorecard XML allow users to specify the label that will be used instead of CSV list for <plot_fix> fcst_init_beg
  * In the scorecard add support for the weights.
  * Python implementation of Reliability diagram
  * Add nbrctc data to the Performance diagram
  * Uses `METplotpy v1.0-beta1 <https://github.com/dtcenter/METplotpy>`_
      and  `METcalcpy v1.0-beta1 <https://github.com/dtcenter/METcalcpy>`_
  * Plotly/Python implementation of the Series plot(`#201 <https://github.com/dtcenter/METviewer/issues/201>`_)
  * Interactive plots in GUI (currently is implemented for Series plots only - will add other plots later)


* Enhancements:

  *  Using Github actions to build documentation `#274 <https://github.com/dtcenter/METviewer/issues/274>`_
  *  Restructure the "Conf interval" dropdown list `#255 <https://github.com/dtcenter/METviewer/issues/255>`_
  * METdatadb is available in METviewer
  * Thw warning message will be displayed if no plots were produced on AWS
  * HTML plot is scaled to the size of the window in GUI
  * The version number is added to the scripts INFO messages
  * <met_version> tag is discontinued and removed from METviewer
  * Updated 3rd-party libraries
  * Revise computation of No-Skill reference line on Reliability diagram
  * Check-box to add/remove No-Skill line
    (`#209 <https://github.com/dtcenter/METviewer/issues/209>`_)

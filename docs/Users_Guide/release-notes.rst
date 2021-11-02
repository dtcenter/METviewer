METviewer Release Notes
-----------------------

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues


Version 4.1.0 release notes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

beta3 release (20211007)
^^^^^^^^^^^^^^^^^^^^^^^^
* New Features:
   * **new SSIDX STAT line type and statistic SS_INDEX** (`#327 <https://github.com/dtcenter/METviewer/issues/327>`_)

* Enhancements:
   * **line plot with different forecast variables plotted on y1 and y2 axis** (`#328 <https://github.com/dtcenter/METviewer/issues/328>`_)
   * **Change ',' as a separator for the series group to ':'** (`#334 <https://github.com/dtcenter/METviewer/issues/334>`_)

* Bugfixes:
   * python plot is not created if plot margin values are doubles (`#331 <https://github.com/dtcenter/METviewer/issues/331>`_)

* Internal:

beta2 release (20210901)
^^^^^^^^^^^^^^^^^^^^^^^^
* New Features:
   * **Python implementation of Ensemble Spread-Skill plot (ens_ss)** (`#308 <https://github.com/dtcenter/METviewer/issues/308>`_)
   * **Python implementation of the Bar plot** (`#296 <https://github.com/dtcenter/METviewer/issues/296>`_)
   * **Aggregate and plot the HSS_EC statistic from the MCTS line type** (`#285 <https://github.com/dtcenter/METviewer/issues/285>`_)
   * **Support for plotting new G and GBETA statistics** (`#324 <https://github.com/dtcenter/METviewer/issues/324>`_)
   * **Plot new CNT columns for the Scatter Index** (`#317 <https://github.com/dtcenter/METviewer/issues/317>`_)

* Enhancements:
   * **Enhance METviewer database schema to comply with MET-10.1.0** (`#286 <https://github.com/dtcenter/METviewer/issues/286>`_)
   * Edit Python dependencies list in Dockerfile (`#311 <https://github.com/dtcenter/METviewer/issues/311>`_)
   * Add 'services' as a root element in docker-compose.yml (`#306 <https://github.com/dtcenter/METviewer/issues/306>`_)


* Bugfixes:
   * The loading module produces an error when parsing <date_list> element with <date_offset> (`#319 <https://github.com/dtcenter/METviewer/issues/319>`_)

* Internal:


beta1 release (20210706)
^^^^^^^^^^^^^^^^^^^^^^^^
* New Features:
   * **Python implementation of the Box plot** (`#292 <https://github.com/dtcenter/METviewer/issues/292>`_)

* Enhancements:
   * Deprecate 'doc' directory and use the link to readthedocs (`#287 <https://github.com/dtcenter/METviewer/issues/287>`_)

* Internal:

* Bugfixes:
   * Error when using Agg stats in performance diagram (`#284 <https://github.com/dtcenter/METviewer/issues/284>`_)
   * Scorecard is not created correctly if input models have 'label' attribute (`#300 <https://github.com/dtcenter/METviewer/issues/300>`_)




Version `v4.0.0 <https://github.com/dtcenter/METviewer/milestone/11>`_ release notes (20200512)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


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

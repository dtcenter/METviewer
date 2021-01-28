METviewer Release Notes
-----------------------

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues


**New in v4.0-beta1**


Bugfixes:

*

New Features:

* Uses `METplotpy v1.0-beta1 <https://github.com/dtcenter/METplotpy>`_
  and  `METcalcpy v1.0-beta1 <https://github.com/dtcenter/METcalcpy>`_
* Plotly/Python implementation of the Series plot
  (`#201 <https://github.com/dtcenter/METviewer/issues/201>`_)
* Interactive plots in GUI (currently is implemented for Series plots
  only - will add other plots later)


Enhancements:

* Check-box to add/remove No-Skill line
  (`#209 <https://github.com/dtcenter/METviewer/issues/209>`_)


**New in v4.0-beta3**


Bugfixes:

* Fixed bugs in Docker file: build of 'pingouin' module,  permissions for the executables, mysql user name

New Features:

* Support to reverse the connection order in ROC curve
* Use the circular bootstrap as a default method instead of the regular
* New Equivalence Testing Bounds plot

Enhancements:

* METdatadb is available in METviewer
* Thw warning message will be displayed if no plots were produced on AWS
* HTML plot is scaled to the size of the window in GUI
* The version number is added to the scripts INFO messages
* <met_version> tag is discontinued and removed from METviewer
* updated 3rd-party libraries


*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version `v5.0.0-beta3 <https://github.com/dtcenter/METviewer>`_ release notes (20220914)
----------------------------------------------------------------------------------------

* New Features:
   * Plot new statistics that are added in MET-11.0.0-beta1:
     VCNT_ANOM_CORR and VCNT_ANOM_CORR_UNCNTR
     (`#403 <https://github.com/dtcenter/METviewer/issues/403>`_)

* Enhancements:

* Bugfixes:
   * fix incorrect order of fcst_var_vals in yaml config file
     to plot multiple fields/statistics
     (`#413 <https://github.com/dtcenter/METviewer/issues/413>`_)

* Internal:
    * Create checksum for released code
      (`#417 <https://github.com/dtcenter/METviewer/issues/415>`_)


Version `v5.0.0-beta2 <https://github.com/dtcenter/METviewer>`_ release notes (202200803)
-----------------------------------------------------------------------------------------

* New Features:
   * Revision series for line and box plots
     (`#394 <https://github.com/dtcenter/METviewer/issues/394>`_)

* Enhancements:
    * **Move mv_mysql.sql from METviewer to METdatadb**
      (`#371 <https://github.com/dtcenter/METviewer/issues/371>`_)
    * add fcst_lead offset calculation to scorecard
      (`#404 <https://github.com/dtcenter/METviewer/issues/404>`_)

* Bugfixes:

* Internal:
    * Rename METdatadb to METdataio
      (`#406 <https://github.com/dtcenter/METviewer/issues/406>`_)


Version `v5.0.0-beta1 <https://github.com/dtcenter/METviewer>`_ release notes (20220622)
----------------------------------------------------------------------------------------

* New Features:
   * Allow line plots start from y=0 line
     (`#390 <https://github.com/dtcenter/METviewer/issues/390>`_)

* Enhancements:
    * Python implementation of the scorecard summary method
      (`#393 <https://github.com/dtcenter/METviewer/issues/393>`_)

* Bugfixes:
    * Include all field values into the resulting scorecard
      (`#395 <https://github.com/dtcenter/METviewer/issues/395>`_)

* Internal:
    * New testing procedure
      (`#391 <https://github.com/dtcenter/METviewer/issues/391>`_)

METviewer Upgrade Instructions
==============================

Upgrade instructions will be listed here if they are
applicable for this release.

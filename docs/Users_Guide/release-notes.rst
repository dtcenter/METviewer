*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version `v5.1.0-beta1 <https://github.com/dtcenter/METviewer>`_ release notes (20230301)
----------------------------------------------------------------------------------------

  .. dropdown:: New Features
     
     None
     
  .. dropdown::  Enhancements
  
     * Upgrade to using Python 3.10.4(`#449 <https://github.com/dtcenter/METviewer/issues/449>`_)

  .. dropdown:: Bugfixes
    
     None

  .. dropdown:: Internal
  
     None  

Version `v5.0.1 <https://github.com/dtcenter/METviewer>`_ release notes (20230130)
----------------------------------------------------------------------------------
* New Features:
* Enhancements:
* Bugfixes:
   * Add nco_requirements.txt file. Update Dockerfile to use NCO-approved Python packages(`#448 <https://github.com/dtcenter/METviewer/issues/448>`_)
* Internal:


Version `v5.0.0 <https://github.com/dtcenter/METviewer>`_ release notes (20221209)
----------------------------------------------------------------------------------

* New Features:
   * Allow line plots start from y=0 line (`#390 <https://github.com/dtcenter/METviewer/issues/390>`_)
   * Added Revision series for line and box plots (`#394 <https://github.com/dtcenter/METviewer/issues/394>`_)
   * Plot new VCNT statistics: VCNT_ANOM_CORR and VCNT_ANOM_CORR_UNCNTR (`#403 <https://github.com/dtcenter/METviewer/issues/403>`_)
   * Plot the new ECNT statistics: SPREAD_MD, MAE, MAE_OERR, BIAS_RATIO, ME_GE_OBS, and ME_LT_OBS (`#434 <https://github.com/dtcenter/METviewer/issues/434>`_)
   * Plot new SEEPS and SEEPS_MPR statistics (`#422 <https://github.com/dtcenter/METviewer/issues/422>`_)
   * Plot new CRPS_EMP_FAIR stat (`#421 <https://github.com/dtcenter/METviewer/issues/421>`_)

* Enhancements:
    * Python implementation of the scorecard summary method (`#393 <https://github.com/dtcenter/METviewer/issues/393>`_)
    * Move mv_mysql.sql from METviewer to METdataio (`#371 <https://github.com/dtcenter/METviewer/issues/371>`_)
    * Add fcst_lead offset calculation to scorecard (`#404 <https://github.com/dtcenter/METviewer/issues/404>`_)
    * Increase the maximum limit of the uploaded XML (`#425 <https://github.com/dtcenter/METviewer/issues/425>`_)
    * Return bad status from mv_load.sh if the loading step fails (`#414 <https://github.com/dtcenter/METviewer/issues/414>`_)

* Bugfixes:
    * Include all field values into the resulting scorecard (`#395 <https://github.com/dtcenter/METviewer/issues/395>`_)
    * Fix incorrect order of fcst_var_vals in yaml config file to plot multiple fields/statistics (`#413 <https://github.com/dtcenter/METviewer/issues/413>`_)
    * Fix MTD loader bug that did not include the last fcst_lead into the revision series data (`#423 <https://github.com/dtcenter/METviewer/issues/423>`_)
    * Fix loading ROC XML into the UI (`#428 <https://github.com/dtcenter/METviewer/issues/428>`_)

* Internal:
    * New testing procedure (`#391 <https://github.com/dtcenter/METviewer/issues/391>`_)
    * Rename METdatadb to METdataio (`#406 <https://github.com/dtcenter/METviewer/issues/406>`_)
    * Create checksum for released code (`#417 <https://github.com/dtcenter/METviewer/issues/415>`_)
    * Move release notes into its own chapter (`#427 <https://github.com/dtcenter/METviewer/issues/427>`_)
    * Fix warnings in Github Actions (`#426 <https://github.com/dtcenter/METviewer/issues/426>`_)
    * Create checksum for release code (`#412 <https://github.com/dtcenter/METviewer/issues/412>`_)



METviewer Upgrade Instructions
==============================

Upgrade instructions will be listed here if they are
applicable for this release.

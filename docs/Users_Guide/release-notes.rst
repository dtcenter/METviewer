*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version `v6.0.0-beta3 <https://github.com/dtcenter/METviewer>`_ release notes (20240417)
----------------------------------------------------------------------------------------

  .. dropdown:: New Features

  .. dropdown::  Enhancements

  .. dropdown:: Bugfixes
     * Fixed implementation of OBJCSI and OBJACSI (`#519 <https://github.com/dtcenter/METviewer/issues/519>`_)
     * MODE CSI statistics produces incorrect results (`#516 <https://github.com/dtcenter/METviewer/issues/516>`_)
     * Bugfix for loading xml (`#513 <https://github.com/dtcenter/METviewer/issues/513>`_)

  .. dropdown:: Internal

Version `v6.0.0-beta3 <https://github.com/dtcenter/METviewer>`_ release notes (20240208)
----------------------------------------------------------------------------------------

  .. dropdown:: New Features

  .. dropdown::  Enhancements
     * Add summary_curve parameter to ROC YAML (`#506 <https://github.com/dtcenter/METviewer/issues/506>`_)
     * Add accessibility statement to GUI (`#508 <https://github.com/dtcenter/METviewer/issues/508>`_)

  .. dropdown:: Bugfixes

  .. dropdown:: Internal
     * Update GitHub actions workflows to switch from node 16 to node 20 (`#510 <https://github.com/dtcenter/METviewer/issues/510>`_)



Version `v6.0.0-beta2 <https://github.com/dtcenter/METviewer>`_ release notes (20231116)
----------------------------------------------------------------------------------------

  .. dropdown:: New Features

     * New feature to specify the color of the no resolution and no skill lines for the reliability diagram (`#465 <https://github.com/dtcenter/METviewer/issues/465>`_)
     * Hide/show the legend entries line by line (`#501 <https://github.com/dtcenter/METviewer/issues/501>`_)

  .. dropdown::  Enhancements
     * Reorganise results tabs in METviewer UI (`#387 <https://github.com/dtcenter/METviewer/issues/387>`_)

  .. dropdown:: Bugfixes
     * Fixed loading Reliability plot XML with EE to UI  (`#494 <https://github.com/dtcenter/METviewer/issues/494>`_)
     * Fixed printing error messages to the Log tab  (`#495 <https://github.com/dtcenter/METviewer/issues/495>`_)

  .. dropdown:: Internal


Version `v6.0.0-beta1 <https://github.com/dtcenter/METviewer>`_ release notes (20230918)
----------------------------------------------------------------------------------------

  .. dropdown:: New Features

  .. dropdown::  Enhancements

  .. dropdown:: Bugfixes

  .. dropdown:: Internal


Version `v5.1.0 <https://github.com/dtcenter/METviewer>`_ release notes (20230731)
----------------------------------------------------------------------------------------


  .. dropdown:: New Features

      * New Dockerfile that creates a single image with database and METviewer in it. Can be used to create a Singularity image  (`#446 <https://github.com/dtcenter/METviewer/issues/446>`_)

  .. dropdown::  Enhancements

      * Upgrade to using Python 3.10.4 (`#449 <https://github.com/dtcenter/METviewer/issues/449>`_)

  .. dropdown:: Bugfixes

      * Fixed an error during uploading a reliability diagram XML to GUI (`#464 <https://github.com/dtcenter/METviewer/issues/464>`_)
      * error during printing log messages for batch jobs (`#484 <https://github.com/dtcenter/METviewer/issues/484>`_)

  .. dropdown:: Internal

     * Remove Couchbase code (`#479 <https://github.com/dtcenter/METviewer/issues/479>`_)
     * Record the username of the user associated with the batch/scorecard run (`#22 <https://github.com/dtcenter/METplus-Internal/issues/22>`_)
     * The user ID will be added to the log files (`#48 <https://github.com/dtcenter/METplus-Internal/issues/48>`_)
     * Record a time stamp indicating when the event occurred  (`#11 <https://github.com/dtcenter/METplus-Internal/issues/11>`_)
     * Testing the bug fix in MEtplotpy
     * Change the version number for scipy to 1.11.1


Version `v5.0.1 <https://github.com/dtcenter/METviewer>`_ release notes (20230130)
----------------------------------------------------------------------------------

  .. dropdown:: New Features
  
     None
     
  .. dropdown:: Enhancements
     
     None
     
  .. dropdown:: Bugfixes

   * Add nco_requirements.txt file. Update Dockerfile to use NCO-approved Python packages (`#448 <https://github.com/dtcenter/METviewer/issues/448>`_)

  .. dropdown:: Internal
  
     None


Version `v5.0.0 <https://github.com/dtcenter/METviewer>`_ release notes (20221209)
----------------------------------------------------------------------------------

  .. dropdown:: New Features
  
     * Allow line plots start from y=0 line (`#390 <https://github.com/dtcenter/METviewer/issues/390>`_)
     * Added Revision series for line and box plots (`#394 <https://github.com/dtcenter/METviewer/issues/394>`_)
     * Plot new VCNT statistics: VCNT_ANOM_CORR and VCNT_ANOM_CORR_UNCNTR (`#403 <https://github.com/dtcenter/METviewer/issues/403>`_)
     * Plot the new ECNT statistics: SPREAD_MD, MAE, MAE_OERR, BIAS_RATIO, ME_GE_OBS, and ME_LT_OBS (`#434 <https://github.com/dtcenter/METviewer/issues/434>`_)
     * Plot new SEEPS and SEEPS_MPR statistics (`#422 <https://github.com/dtcenter/METviewer/issues/422>`_)
     * Plot new CRPS_EMP_FAIR stat (`#421 <https://github.com/dtcenter/METviewer/issues/421>`_)

  .. dropdown:: Enhancements
  
     * Python implementation of the scorecard summary method (`#393 <https://github.com/dtcenter/METviewer/issues/393>`_)
     * Move mv_mysql.sql from METviewer to METdataio (`#371 <https://github.com/dtcenter/METviewer/issues/371>`_)
     * Add fcst_lead offset calculation to scorecard (`#404 <https://github.com/dtcenter/METviewer/issues/404>`_)
     * Increase the maximum limit of the uploaded XML (`#425 <https://github.com/dtcenter/METviewer/issues/425>`_)
     * Return bad status from mv_load.sh if the loading step fails (`#414 <https://github.com/dtcenter/METviewer/issues/414>`_)

  .. dropdown:: Bugfixes
 
     * Include all field values into the resulting scorecard (`#395 <https://github.com/dtcenter/METviewer/issues/395>`_)
     * Fix incorrect order of fcst_var_vals in yaml config file to plot multiple fields/statistics (`#413 <https://github.com/dtcenter/METviewer/issues/413>`_)
     * Fix MTD loader bug that did not include the last fcst_lead into the revision series data (`#423 <https://github.com/dtcenter/METviewer/issues/423>`_)
     * Fix loading ROC XML into the UI (`#428 <https://github.com/dtcenter/METviewer/issues/428>`_)

  .. dropdown:: Internal
  
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

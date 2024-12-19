*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version `v6.0.0 <https://github.com/dtcenter/METviewer>`_ Release Notes (20241219)
--------------------------------------------------------------------------------------

  .. dropdown:: New Features

     * Add GitHub action to run SonarQube for METviewer pull requests and feature branches (`#521 <https://github.com/dtcenter/METviewer/issues/521>`_)
     * New feature to specify the color of the no resolution and no skill lines for the reliability diagram (`#465 <https://github.com/dtcenter/METviewer/issues/465>`_)
     * Hide/show the legend entries line by line (`#501 <https://github.com/dtcenter/METviewer/issues/501>`_)
                
  .. dropdown::  Enhancements

     * **Enhance METviewer to plot statistics added to the ECNT, VL1L2, VAL1L2, and VCNT linetypes introduced in the MET 12.0.0 beta4 dev cycle** (`#517 <https://github.com/dtcenter/METviewer/issues/517>`_)
     * METviewer: Address SonarQube Vulnerabilities (`#58 <https://github.com/dtcenter/METplus-Internal/issues/58>`_)
     * Add summary_curve parameter to ROC YAML (`#506 <https://github.com/dtcenter/METviewer/issues/506>`_)
     * Add accessibility statement to GUI (`#508 <https://github.com/dtcenter/METviewer/issues/508>`_)
     * Reorganise results tabs in METviewer UI (`#387 <https://github.com/dtcenter/METviewer/issues/387>`_)
       
  .. dropdown:: Bugfixes

     * Fixed implementation of OBJCSI and OBJACSI (`#519 <https://github.com/dtcenter/METviewer/issues/519>`_)
     * MODE CSI statistics produces incorrect results (`#516 <https://github.com/dtcenter/METviewer/issues/516>`_)
     * Bugfix for loading xml (`#513 <https://github.com/dtcenter/METviewer/issues/513>`_)
     * Fixed loading Reliability plot XML with EE to UI (`#494 <https://github.com/dtcenter/METviewer/issues/494>`_)
     * Fixed printing error messages to the Log tab (`#495 <https://github.com/dtcenter/METviewer/issues/495>`_)

  .. dropdown:: Internal

     * **METviewer: Add a unique identifier in order to distinguish itself from other application logs** (`METplus-Internal#29 <https://github.com/dtcenter/METplus-internal/issues/29>`_)
     * METviewer: Adjust SonarQube for “false positive” METviewer vulnerabilities (`METplus-Internal#60 <https://github.com/dtcenter/METplus-internal/issues/60>`_)
     * Add testing.yaml testing workflow (`#539 <https://github.com/dtcenter/METviewer/issues/539>`_)
     * Update GitHub issue and pull request templates to reflect the current development workflow details (`#491 <https://github.com/dtcenter/METviewer/issues/491>`_)
     * Update GitHub actions workflows to switch from node 16 to node 20 (`#510 <https://github.com/dtcenter/METviewer/issues/510>`_)


METviewer Upgrade Instructions
==============================

 None

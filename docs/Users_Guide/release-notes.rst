*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues


Version `v6.1.0  <https://github.com/dtcenter/METviewer>`_ Release Notes (20250728)
--------------------------------------------------------------------------------------


  .. dropdown:: New Features

     None


  .. dropdown::  Enhancements

     * Update Dockerfile for Apptainer to use MET base 3.4/Python 3.12 (`#576 <https://github.com/dtcenter/METviewer/issues/576>`_)
     * Add release-docker-images.yml workflow to routinely rebuild Docker images and scan for CVEs (`#586 <https://github.com/dtcenter/METviewer/issues/586>`_)

  .. dropdown:: Bugfixes

     * Bugfix: Update Docker files to allow METviewer to run in METplus use case tests (`#574 <https://github.com/dtcenter/METviewer/issues/574>`_)


  .. dropdown:: Documentation

    * Document the scorecard <threshold_file> in the METviewer User's Guide (`#564 <https://github.com/dtcenter/METviewer/issues/564>`_)


METviewer Upgrade Instructions
==============================


 .. note::

    For Mac OS users who install Docker containers, Docker currently only supports the latest two versions of Mac OS.
    Check the latest Docker user's documentation to ensure that your Docker and MacOS environments are compatible.
   
 .. note::
  
    In the METviewer-6.1.0-beta2 release, METviewer switched from development with Python 3.10.4 to
    development with Python 3.12. View the met_plus_requirements.txt/nco_requirements.txt file at the top
    level of the repository for version numbers for the corresponding third-party packages.
     

    
 .. note::

    Kaleido and Plotly were updated (1.0.0 and 6.1.1 respectively) to address the **pending deprecation
    of kaleido 0.x versions** (after September 2025).

    Plotly/kaleido will **no longer include Chrome**. The  METviewer Dockerfile was updated to
    retrieve and install Chrome.

    **ATTENTION:  Mac M1/M2 processors (Apple Silicon)**

    METviewer docker images built or run on a Mac with the M1/M2 processor (Apple silicon) will result in an error
    emanating from Plotly/kaleido.  As a result, no plot is generated.

    Docker images built or run on AMD64 platforms (i.e. x86-64, Mac Intel) can still successfully generate
    a static image.

    The Plotly team has been contacted about this issue, and a response is pending.
    This issue has been raised with the Plotly team, and a response is pending.

*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues


Version 7.0.0 Beta 1 Release Notes (20260205)
--------------------------------


  .. dropdown:: New Features

     None


  .. dropdown::  Enhancements

     None

  .. dropdown:: Bugfixes

     * Bugfix: Requesting Y1/Y2 points results in an RScript error (`#640 <https://github.com/dtcenter/METviewer/issues/640>`_)


  .. dropdown:: Documentation

     None




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

 .. note::

    The METviewer batch module now generates a .configure directory in the user's home directory.
    Verify that write permissions are correctly set to allow the creation of this directory. This provides a
    workaround to a Chrome/choreographer permission issue that occurs when a Chrome download occurs at runtime.  The METviewer code
    now uses the existing Chrome (choreographer library) installed in the Python environment (to avoid downloading
    Chrome at runtime).  The choreographer library is included in the kaleido package.  The kaleido package
    is currently not available via conda, but can be installed via pip.

   

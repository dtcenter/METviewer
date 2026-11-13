*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues

Version 13.0.0 RC1 Release Notes (20261113)
-------------------------------------------


  .. dropdown:: New Features

     None


  .. dropdown::  Enhancements

     * METplus #3262 develop update ubuntu (`#669 <https://github.com/dtcenter/METviewer/pull/669>`_)

     * Update METviewer to work with Java 17 and Tomcat 10 (`#613 <https://github.com/dtcenter/METviewer/issues/613>`_)

  .. dropdown:: Bugfixes

     * Bugfix: Errors with running METviewer on M1/M2 Mac OS in Docker container (`#587 <https://github.com/dtcenter/METviewer/issues/587>`_)

     * Bugfix: Update jar versions in METviewer/bin scripts (`#603 <https://github.com/dtcenter/METviewer/issues/603>`_)


  .. dropdown:: Documentation

     * METplus #3247 link check (`#670 <https://github.com/dtcenter/METviewer/pull/670>`_)

     * Enhance the documentation by correcting broken links and typos (`#671 <https://github.com/dtcenter/METviewer/issues/671>`_)


Version 13.0.0 Beta 2 Release Notes (20260507)
----------------------------------------------


  .. dropdown:: New Features

     None


  .. dropdown::  Enhancements

     None

  .. dropdown:: Bugfixes

     None


  .. dropdown:: Documentation

     None


Version 13.0.0 Beta 1 Release Notes (20260205)
----------------------------------------------


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

    The version numbering for METviewer has been updated to 13.0.0 to provide consistency and clarity with all METplus components. View the met_plus_requirements.txt/nco_requirements.txt file at the top level of the repository for version numbers for the corresponding third-party packages.   
  
    For Mac OS users who install Docker containers, Docker currently only supports the latest two versions of Mac OS.
    Check the latest Docker user's documentation to ensure that your Docker and MacOS environments are compatible.

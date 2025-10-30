*****************************
METviewer Release Information
*****************************

METviewer Release Notes
=======================

When applicable, release notes are followed by the GitHub issue number which
describes the bugfix, enhancement, or new feature:
https://github.com/dtcenter/METviewer/issues


Version v6.0.3 Release Notes (20251030)
---------------------------------------


  .. dropdown:: New Features

     None


  .. dropdown::  Enhancements

     None


  .. dropdown:: Bugfixes


     * Update commons-lang3 library to >=3.18 to resolve CVE-2025-48924
       (`#630 <https://github.com/dtcenter/METviewer/issues/630>`_)


  .. dropdown:: Internal

     None

Version v6.0.2 Release Notes (20251016)
---------------------------------------

  .. dropdown:: New Features

     None

  .. dropdown::  Enhancements

     None

  .. dropdown:: Bugfixes

     * Add env vars to Python script execution for main_v6.0
       (`#623 <https://github.com/dtcenter/METviewer/issues/623>`_)

  .. dropdown:: Internal

     None

Version `v6.0.1 <https://github.com/dtcenter/METviewer>`_ Release Notes (20250723)
--------------------------------------------------------------------------------------

  .. dropdown:: New Features
     
     None

  .. dropdown::  Enhancements

     None
 
  .. dropdown:: Bugfixes

     * Apply updates to jar files with vulnerabilities to main_v6.0 (`#585 <https://github.com/dtcenter/METviewer/issues/585>`_)

  .. dropdown:: Internal

     * On 2025-09-29, re-tagged release to update base image used to build
       METviewer Docker images to get latest bugfix changes.
       There were no METviewer code changes.

METviewer Upgrade Instructions
==============================

.. note::

   Kaleido and Plotly were updated to address the upcoming deprecation of kaleido 0.x versions.  As
   a result, METviewer docker images CANNOT be built or run on Mac M1/M2 machines.  Mac Intel platform and other    amd64 platforms still support building and running with these newer versions of Plotly and kaleido.      

  This is an issue that has been raised with the Plotly team.  Resolution is pending.
   
   

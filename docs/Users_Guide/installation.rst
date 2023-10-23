.. _install:

****************************
METviewer Installation Guide
****************************

Introduction
============

This chapter describes how to install METviewer. METviewer has been
developed and tested on Linux operating systems. Support for additional
platforms and compilers may be added in future releases. 

Installing METviewer
====================

METviewer relies on the following tools. These must be installed and tested
prior to installing METviewer:

**Java JDK 1.8+**

**Ant** - `download ant <http://ant.apache.org/bindownload.cgi>`_ and
install the latest version.

**Database** - METviewer works with MySQL and MariaDB.
`Download MySQL <https://dev.mysql.com/downloads/mysql/>`_
or `download MariaDB <https://mariadb.org/download/>`_ and install the
latest version. Use "SET GLOBAL max_allowed_packet=110000000;" by typing
the command in MySQL and/or make the corresponding edit to */etc/my.cnf*,
so that the change persists after the next reboot.

**Apache Tomcat** -
`download Apache Tomcat 8 <https://tomcat.apache.org/download-80.cgi>`_
and install the latest version; test the sample JSP web apps.

Create an output directory under *<Tomcat>/webapps* for METviewer output
files. Under this directory create 4 subdirectories: *xml, plots, data,
scripts*



**R and R packages**

**IMPORTANT: Starting from METviewer v4.1.0 R and R packages will be deprecated. Python packages will be used instead**

`download R <https://www.r-project.org/>`_ and
install the latest version. Install required R packages:

 * boot
 * plotrix
 * data.table
 * verification
 * gsl


**Python**: install Python 3.10.4. Create an environment
(METviewer_py3.10.4) and install required packages:

* Python 3.10.4
* matplotlib==3.5.2
* scipy==1.8.1
* plotly==5.9.0
* xarray==2022.3.0
* pyyaml==6.0
* kaleido==0.2.1
* pint==0.19.2
* metpy==1.3.1
* pandas==1.5.1
* attrs==22.1.0
* exceptiongroup==1.0.4
* iniconfig==1.1.1
* lxml==4.9.1
* packaging==22.0
* pluggy==1.0.0
* PyMySQL==1.0.2
* pytz==2022.6
* setuptools==65.5.1
* six==1.16.0
* tomli==2.0.1
* wheel==0.38.1
* numpy==1.22.0
* netcdf4==1.6.2
* pytest==7.2.0
* python-dateutil==2.8.2
* imageio==2.19.3
* imutils==0.5.4
* scikit-image==0.19.3
* opencv-python




**METviewer v5.1** -
clone   `METviewer repository <https://github.com/dtcenter/METviewer>`_

   .. code-block:: none

     git clone https://github.com/dtcenter/METviewer.git

**METcalcpy v2.1** -
clone   `METcalcpy repository <https://github.com/dtcenter/METcalcpy>`_

   .. code-block:: none

     git clone https://github.com/dtcenter/METcalcpy.git

**METplotpy v2.1** -
clone   `METplotpy repository <https://github.com/dtcenter/METplotpy>`_

   .. code-block:: none

     git clone https://github.com/dtcenter/METplotpy.git

**METdataio v2.1** -
clone   `METdataio repository <https://github.com/dtcenter/METdataio>`_

   .. code-block:: none

     git clone https://github.com/dtcenter/METdataio.git

Configure and build METviewer
-----------------------------

#. Configure the batch and loading tools:

   * Edit *METviewer/bin/mv_batch.sh*:

        * Set the variable **PYTHON_ENV** to point at the Python environment
        * Set the variable **METCALCPY_HOME** to point to *METcalcpy* directory
        * Set the variable **METPLOTPY_HOME** to point to *METplotpy* directory

   * Edit *METviewer/bin/mv_load.sh*:

        * Set the variable **PYTHON_ENV** to point at the Python environment
        * Set the variable **METDATAIO_HOME** to point to *METdataio* directory


   * Edit *METviewer/bin/mv_scorecard.sh*:

        * Set the variable **PYTHON_ENV** to point at the Python environment
        * Set the variable **METCALCPY_HOME** to point to METcalc directory

   * Create a custom property file by copying
     *METviewer/webapp/metviewer/WEB-INF/classes/build.properties*
     to *METviewer* and providing custom values for the parameters:
     
        * Set **db.host** to the database server host and port,
	  e.g. db.ncep.gov:3306
        * Set **db.user** and **db.password** to the database username
	  and password
        * Set **db.management.system** to the database type - mysql or mariadb
        * Set redirect to the application name in url (ex. if the
	  application URL is "http://www.dtcenter.org/met/metviewer/"
	  redirect is "metviewer")
        * Set **output.dir** to the absolute path of the output directory
        * Set **webapps.dir** to the absolute path of the Tomcat's webapps
	  directory
        * Set **url.output** to the url to the output folder
        * Set **python.env** to the absolute path of the Python environment
	  directory
        * Set **metcalcpy.home** to the absolute path of the METcalcpy
	  directory
        * Set **metplotpy.home** to the absolute path of the METplotpy
	  directory


   * Edit *METviewer/webapp/metviewer/WEB-INF/classes/log4j.properties*:

        * Set **log4j.appender.logfile.File** setting to the absolute
	  path of a log file


#. Build and deploy the application:

   * Build METviewer and the web application. ``Replace the parameters
     values in the Ant command to what is appropriate for the user's
     setup``:

   .. code-block:: none

        cd MRTviewer
        ant -Dbuild.properties.file=METviewer/build.properties \
        -Ddb.management.system=mariadb -Dmetcalcpy.path=METcalcpy/ -Dmetplotpy.path=METplotpy/ \
        -Dmetdataio.path=METdataio/ \
       -Dpython.env.path=METviewer_py3.10.4/  clean all

  * Deploy the web app to tomcat

   .. code-block:: none

        cp METviewer/dist/metviewer.war Tomcat/webapps


3. Create a METviewer database:

   * Clone METdataio repositiry: https://github.com/dtcenter/METdataio.git

   * Create a database to store MET data, which has the prefix
     'mv\_', e.g. mv_met_data:

   .. code-block:: none

        cd METdataio/METdbLoad/sql
        mysql -u[db_username] -p[db_password] -e'create database [db_name];'
        mysql -u[db_username] -p[db_password] [db_name] < sql/mv_mysql.sql


#. Install test directory (for development, optional):
   
   * Check out test_data (*../apps/verif/metviewer_test_data/test_data/*)
     from CVS and move test_data directory to */d3/projects/METViewer/*:
   
   * Create links to R script and sql files

   .. code-block:: none
        
        cd /d3/projects/METViewer/test_data
        ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/R_tmpl R_tmpl
        mkdir R_work
        cd R_work
        mkdir data
        mkdir plots
        mkdir scripts
        ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/R_work/include/ include
        cd /d3/projects/METViewer/test_data/load_data/load
        ln -s /d3/projects/METViewer/src_dev/apps/verif/metviewer/sql/mv_mysql.sql mv_mysql.sql

Making a Database Accessible in the METviewer Web Application
=============================================================

To make a new database accessible in the METviewer Web Application click
on "Reload list of databases" button in the upper right corner of the
main JSP page. The list of available databases should be updated and a
new database should be in it.

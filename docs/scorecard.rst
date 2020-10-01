Scorecards
==========

Description
-----------

The scorecard can only be produced using xml code. The xml code specifications are listed in the detail below. There is no GUI interface. 

Details
-------

The scorecard Module is used to generate a scorecard image and HTML table using data in the METviewer database. The usage statement:

**Usage:**

.. code-block:: none

  mv_scorecard.sh your_scorecard_spec_file.xml

where your_scorecard_spec_file.xml specifies the XML plot specification document.

---- Scorecard Done ---- 

The your_scorecard_spec_file.xml passed to the scorecard module contains information about the scorecard that will be generated. It is an XML file whose top-level tag is **<plot_spec>** which contains the following elements, divided into functional sections:

**<connection>** see common xml

**<rscript>** specifies the Rscript instance to use, for example /usr/local/bin/Rscript.

**<folders>** a structure that specifies the input and output file system folders. 

**<r_tmpl>** the folder that contains the R templates used by METviewer, typically R_tmpl/ from the metviewer CVS source tree 

**<r_work>** the folder that will contain the data and generated R scripts, must contain the sub-folder include/ 

**<plots>** the output folder that will contain generated image and HTML files 

**<data>** the output folder that will contain generated data files 

**<scripts>** the output folder that will contain scripts files

**<plot>** a structure that specifies scorecard characteristics 

**<view_value>** TRUE or FALSE, indicating whether or not to display statistical values in the scorecard cells. The default value is FALSE (do not display) 

**<view_symbol>** TRUE or FALSE, indicating whether or not to display a symbol in the scorecard cells. The default value is TRUE ( display).

**<view_legend>** TRUE or FALSE, indicating whether or not to display a legend for the scorecard. The default value is TRUE ( display). 

**<stat_flag>** NCAR or EMC, which algorithm to use to calculate statistics.The default value is NCAR.

**<printSQL>** FALSE or TRUE, indicating whether or not to print SQL queries. The default value is FALSE ( do not print). 

**<stat>** DIFF (display a difference between models) or DIFF_SIG ( display p_value ) or SINGLE (display a value of statistic for the 1st model; then the value of the 2nd model is optional), indicating what value to show in the cell. The default value is DIFF_SIG ( display p_value ).

**<threshold_file>** full path to the XML file describing configurations for thresholds, colors and symbols

**<template>** the name of the R plot script template to use. The following templates are currently supported: scorecard.R_tmpl

**<plot_fix>** describes database fields - models, date range and init hour - that are constant. Model field contains two values - first and second model names. Date range can be specified using 'fcst_valid_beg' or 'fcst_init_beg' and contains two values - start and end dates Init hour can contain one or more values.

**<field name="model">** 

**<val name="first_model" />** 

**<val name="second_model" />** 

**</field>** 

**<field name="fcst_valid_beg">** 

**<val name="start_date" />** 

**<val name="end_date" />** 

**</field>** 

**<field name="init_hour">** 

**<val name="first_hour" />** 

**</field>**

**<rows>** describes an individual row in the scorecard table. Contains one or more nested **<field>** elements. 

**<field>** describes an individual row or row group in the scorecard table. Attributes: 

**name:** database field name 

**<val>** describes a value of the field. Field can have more than one value element. Attributes: 

**name:** database field name 

**label:** the label that shows up on the image.

**<field>** next level database field name. Field can have more than one field element.

**<columns>** describes an individual column in the scorecard table. Contains one or more nested **<field>** elements 

**<field>** describes an individual column or column group in the scorecard table. Attributes: 

**name:** database field name 

**<val>** describes a value of the field. Field can have more than one value element. Attributes: 

**name:** database field name 

**label:** the label that shows up on the image 

**<field>** next level database field name. Field can have more than one field element.

**<agg_stat>** TRUE(bootstrapping) or FALSE(student-T/normal), indicating whether or not to create a scorecard using aggregated statistics. The default value is TRUE (use bootstrapping ) 

**<boot_repl>** number of bootstrapping replications, use 1 for no bootstrapping. Used when **<agg_stat>** is TRUE.

**<boot_random_seed>** value of Random.seed for bootstrapping to get a reproducible random results. Used when **<agg_stat>** is TRUE.

**<plot_stat>** name of the aggregation statistic: median or mean. The default value is median. 

**<tmpl>** 

**<data_file>** name of the R data.frame data file, inside of the **<r_work>** folder specified above.

**<plot_file>** name of the output image file, inside of the **<plots>** folder specified above.

**<title>** title of the scorecard displayed on the image. Default value: Verification Scorecard 

Examples
--------

The image below shows an example an output scorecard. 

.. figure:: Scorecard.png

	    Figure 13.1 Scorecard output example.
	    

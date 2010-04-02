<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
	<title>METViewer</title>
	<link rel="stylesheet" type="text/css" href="include/metviewer.css"/>
	<link rel="shortcut icon" href="include/favicon.ico" type="image/x-icon"/>
</head>

<body style="margin:20px">

	<h2>Select Database</h2>

	<p>	
	<s:select cssClass="gInput" label="Database" name="database" list="databases"/>
	<s:submit cssClass="gButton" value="Select"/>
	</p>

</body>
</html>

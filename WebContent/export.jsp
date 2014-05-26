<%@ page session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>eBusiness Framework Demo - ExportXML</title>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" type="text/css" href="default.css">
</head>
<body>

	<%@ include file="header.jsp"%>
	<%@ include file="error.jsp"%>
	<%@ include file="authentication.jsp"%>
	<%@ include file="navigation.jspfragment"%>
	<%@ include file="exportsuccess.jsp"%>

	<h1>XML Export</h1>
	<div>
		<form method="POST" action="controllerservlet?action=exportXmlAction">
			Please select:<br /> <input type="hidden" name="type" value="export" />
			<input type="radio" name="select" value="all" checked /> All
			articles<br /> <input type="radio" name="select" value="selection" />
			Articles matching: <input type="text" name="match" /><br /> <br />
			<input type="submit" value="Press"> to export as XML!
		</form>
	</div>

	<h1>XHTML Export</h1>
	<div>
		<form method="POST"
			action="controllerservlet?action=exportXhtmlAction">
			Please select:<br /> <input type="hidden" name="type" value="export" />
			<input type="radio" name="select" value="all" checked /> All
			articles<br /> <input type="radio" name="select" value="selection" />
			Articles matching: <input type="text" name="match" /><br /> <br />
			<input type="submit" value="Press"> to export as XHTML!
		</form>
	</div>
</body>
</html>
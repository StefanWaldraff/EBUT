<%@ page session="true"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>eBusiness Framework Demo - Import</title>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" type="text/css" href="default.css">
<script type="text/javascript">
	function testEmpty(){
		if(document.uploadform.upfile.value == ""){
			alert("Please choose a document!");
			return false;
		}
		return true;
		
		
	}
</script>
</head>
<body>

	<%@ include file="header.jsp"%>
	<%@ include file="error.jsp"%>
	<%@ include file="authentication.jsp"%>
	<%@ include file="navigation.jspfragment"%>
	<%@ include file="uploadsuccess.jsp" %>

	<h1>Import</h1>
	<div>

		<form name="uploadform" method="POST" enctype="multipart/form-data"
			action="controllerservlet?action=importAction" >
			File to upload: <input type="file" name="upfile"><br /> <br />
			<input type="submit" value="upload" onclick="return testEmpty()" > to upload the file!
		</form>
		
		<!-- <h2>${message}</h2> -->

	</div>
</body>
</html>
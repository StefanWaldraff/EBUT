
<%@ page session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<br>
<c:forEach var="file" items="${sessionScope.filesUploaded}">
	<jsp:useBean id="file" class="java.lang.String" />

	<b>Successfully exported file.</b>
	<a href='download.jsp?file=<%=file%>' target='_blank'>Download</a>
	<br>
	<br>
</c:forEach>

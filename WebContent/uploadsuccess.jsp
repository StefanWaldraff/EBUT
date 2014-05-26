
<%@ page session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<br>
<c:if test="${fn:length${sessionScope.updateFeedback}} > 1 ">
	<b>Successfully imported file.</b>
	<br />
	<c:forEach var="entry" items="${sessionScope.updateFeedback}">
		${entry.key }: ${entry.value}<br />
	</c:forEach>
	<br />
	<br />
</c:if>

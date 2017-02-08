<%-- 
    Document   : showDygraphs
    Created on : 07-Feb-2017, 20:12:38
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- must use table to center chart --%>
<%-- http://dygraphs.com/tutorial.html --%>
<table align="center">
	<tr>
		<td>
			<div id="dygraphsOutput">

			</div>
		</td>
	</tr>
</table>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dygraphs-2.0.0/dygraph.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dygraphs-2.0.0/dygraph.min.js"></script>

<script type="text/javascript">
	//http://dygraphs.com/options.html
	var options = {};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<script type="text/javascript">
	new Dygraph(document.getElementById("dygraphsOutput"), '${csvData}', options);
</script>

<%-- 
    Document   : showReportParameters
    Created on : 09-Jan-2015, 07:31:29
    Author     : Timothy Anyona

Display report parameters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<p>
	<c:forEach var="entry" items="${parameterDisplayValues}">
		<encode:forHtmlContent value="${entry.value}"/> <br>
	</c:forEach>
</p>

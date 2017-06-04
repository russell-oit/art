<%-- 
    Document   : showSelectedParameters
    Created on : 17-Mar-2016, 07:49:39
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div class="well well-sm">
	<c:forEach var="reportParamEntry" items="${reportParamEntries}">
		<c:set var="reportParam" value="${reportParamEntry.value}"/>
		<%-- if page included using servletContext.getRequestDispatcher().include(), pageContext.response.locale returns the OS locale and request.locale the browser locale? --%>
		<b>${encode:forHtmlContent(reportParam.parameter.getLocalizedLabel(localeString))}</b>: ${encode:forHtmlContent(reportParam.displayValues)}
		<br>
	</c:forEach>
</div>
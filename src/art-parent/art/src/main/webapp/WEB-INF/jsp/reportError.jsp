<%-- 
    Document   : reportError
    Created on : 22-May-2014, 10:16:08
    Author     : Timothy Anyona

Page to display if error occurs in run report controller
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<t:mainPage title="${reportName}">
	<jsp:body>
		<c:if test="${not empty reportName}">
		<div class="reportHeader">
			<encode:forHtmlContent value="${reportName}"/>
		</div>
		</c:if>
		<jsp:include page="/WEB-INF/jsp/reportErrorInline.jsp"/>
	</jsp:body>
</t:mainPage>

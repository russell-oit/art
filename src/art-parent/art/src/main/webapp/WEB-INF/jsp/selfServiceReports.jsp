<%-- 
    Document   : selfServiceReports
    Created on : 24-Dec-2018, 18:13:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceReports" var="pageTitle"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/css/query-builder.default.min.css" /> 
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/js/query-builder.standalone.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multiselect-2.5.5/js/multiselect.min.js"></script>
		
	</jsp:attribute>

</t:mainPage>

<%-- 
    Document   : showLeaflet
    Created on : 01-Mar-2017, 17:15:55
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<div id="map" style="height: ${options.height}">

</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/leaflet-1.0.3/leaflet.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/leaflet-1.0.3/leaflet.js"></script>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${options.cssFile}">
</c:if>

<%-- https://stackoverflow.com/questions/10738044/jstl-el-equivalent-of-testing-for-null-and-list-size --%>
<c:forEach var="jsFileName" items="${options.jsFiles}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${jsFileName}"></script>
</c:forEach>


<script type="text/javascript">
	//http://leafletjs.com/
	//https://github.com/tmcw/mapmakers-cheatsheet
	var jsonData = ${data};

	var dataFileUrl = null;
	<c:if test="${not empty options.dataFile}">
	dataFileUrl = "${pageContext.request.contextPath}/js-templates/${options.dataFile}";
	</c:if>
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>

<%-- 
    Document   : showLeaflet
    Created on : 01-Mar-2017, 17:15:55
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div id="${mapId}" style="height: ${options.height}">

</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/Leaflet-1.3.4/leaflet.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/Leaflet-1.3.4/leaflet.js"></script>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(options.cssFile)}">
</c:if>

<%-- https://stackoverflow.com/questions/10738044/jstl-el-equivalent-of-testing-for-null-and-list-size --%>
<c:forEach var="jsFileName" items="${options.jsFiles}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(jsFileName)}"></script>
</c:forEach>

<c:forEach var="cssFileName" items="${options.cssFiles}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(cssFileName)}">
</c:forEach>

<script type="text/javascript">
	//http://leafletjs.com/
	//https://github.com/tmcw/mapmakers-cheatsheet
	//https://www.mapbox.com/help/how-web-maps-work/
	//https://gis.stackexchange.com/questions/68489/how-to-load-external-geojson-file-into-leaflet-map
	//https://gis.stackexchange.com/questions/87332/which-plugin-can-draw-topojson-right-within-leaflet-without-conversion
	//http://joshuafrazier.info/leaflet-basics/
	//https://gis.stackexchange.com/questions/184125/alternative-basemaps-for-leaflet
	//https://cimbura.com/2016/05/02/anatomy-web-map/
	//https://en.wikipedia.org/wiki/Web_mapping
	//https://en.wikipedia.org/wiki/Tiled_web_map
	var mapId = '${mapId}';
	var contextPath = "${pageContext.request.contextPath}";
	
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}

	var dataFileUrl = null;
	<c:if test="${not empty options.dataFile}">
	dataFileUrl = "${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(options.dataFile)}";
	</c:if>
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

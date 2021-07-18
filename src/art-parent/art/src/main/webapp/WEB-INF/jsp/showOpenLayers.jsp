<%-- 
    Document   : showOpenLayers
    Created on : 02-Mar-2017, 12:24:26
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div id="${mapId}" style="height: ${options.height}">

</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/OpenLayers-5.3.0/ol.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/OpenLayers-5.3.0/ol.js"></script>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(options.cssFile)}">
</c:if>

<c:forEach var="jsFileName" items="${options.jsFiles}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(jsFileName)}"></script>
</c:forEach>

<c:forEach var="cssFileName" items="${options.cssFiles}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(cssFileName)}">
</c:forEach>

<script type="text/javascript">
	//http://openlayers.org/
	//https://astuntechnology.github.io/osgis-ol3-leaflet/index.html
	//https://www.toptal.com/web/the-roadmap-to-roadmaps-a-survey-of-the-best-online-mapping-tools
	//https://openlayersbook.github.io/index.html
	//http://bkuliyev.com/creating-markers-in-openlayers-3/
	//http://openlayers.org/en/latest/examples/icon.html
	//https://gist.github.com/jgcasta/6a32902e5b239d35079d
	//https://codezone4.wordpress.com/2015/03/13/openlayers-3-map-with-marker/
	//https://stackoverflow.com/questions/24315801/how-to-add-markers-with-openlayers-3
	//https://gis.stackexchange.com/questions/191505/openlayer-3-popup-on-marker-mouseover
	//https://gis.stackexchange.com/questions/188865/error-with-popup-overlay-in-openlayers-3
	var mapId = '${mapId}';
	var contextPath = "${pageContext.request.contextPath}";
	
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}
	var markerUrl = "${pageContext.request.contextPath}/js/Leaflet-1.3.4/images/marker-icon.png";

	var dataFileUrl = null;
	<c:if test="${not empty options.dataFile}">
	dataFileUrl = "${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(options.dataFile)}";
	</c:if>
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

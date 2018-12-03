<%-- 
    Document   : showDatamaps
    Created on : 24-Feb-2017, 12:12:59
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div id="${containerId}" style="position: relative; width: ${options.width}; height: ${options.height}; margin: 0 auto;">

</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/topojson-3.0.2/topojson.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(options.datamapsJsFile)}"></script>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(options.cssFile)}">
</c:if>

<script>
	//https://blog.basilesimon.fr/2014/04/24/draw-simple-maps-with-no-effort-with-d3-js-and-datamaps-js/
	//https://stackoverflow.com/questions/41482906/states-not-highlighting
	//https://stackoverflow.com/questions/40640634/creating-heatmap-of-states-in-brazil
	//https://github.com/d3/d3-3.x-api-reference/blob/master/Geo-Projections.md
	//https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
	//https://github.com/markmarkoh/datamaps
	//http://mapshaper.org/
	//https://stackoverflow.com/questions/27215394/d3-datamaps-onclick-events-on-bubbles?rq=1
	//https://stackoverflow.com/questions/37940288/load-popup-on-hover-data-from-json-file-for-datamaps
	//https://stackoverflow.com/questions/37799379/add-legend-to-colored-worldmap
	//https://stackoverflow.com/questions/41641124/datamap-change-color-depending-on-value
	//http://bl.ocks.org/markmarkoh/11331459
	//http://latitudelongitude.org/
	//http://www.latlong.net/
	//http://andrew.hedges.name/experiments/convert_lat_long/
	//https://gist.github.com/markmarkoh/8856417
	//https://gist.github.com/markmarkoh/8717334
	//http://192.156.137.110/gis/search.asp
	//https://www.arcgis.com/home/item.html?id=5f83ca29e5b849b8b05bc0b281ae27bc
	//https://wiki.openstreetmap.org/wiki/WikiProject_Kenya
	//http://doc.arcgis.com/en/arcgis-online/reference/shapefiles.htm
	//https://wiki.openstreetmap.org/wiki/Shapefiles
	//http://docs.qgis.org/2.14/en/docs/gentle_gis_introduction/
	//http://www.spatialthoughts.com/blog/gis/mapshaper-command-line/
	//https://russiansphinx.blogspot.co.ke/2014/06/simplifying-shapefiles-in-mapshaper.html
	//https://gis.stackexchange.com/questions/97884/how-to-simplify-shapefiles-without-losing-attributes
	//https://github.com/markmarkoh/datamaps/blob/master/src/examples/highmaps_world.html

	<c:if test="${not empty data}">
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = JSON.parse(dataString);
	</c:if>

	var dataUrl = null;
	<c:if test="${not empty options.dataFile}">
	dataUrl = "${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(options.dataFile)}";
	</c:if>

		var dataType = '${encode:forJavaScript(options.dataType)}';

		var geographyConfig = {};
	<c:if test="${not empty options.mapFile}">
		var mapFileUrl = "${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(options.mapFile)}";
			$.extend(geographyConfig, {
				dataUrl: mapFileUrl
			});
	</c:if>

			var options = {
				element: document.getElementById('${containerId}'),
				dataUrl: dataUrl,
				dataType: dataType,
				geographyConfig: geographyConfig
			};
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

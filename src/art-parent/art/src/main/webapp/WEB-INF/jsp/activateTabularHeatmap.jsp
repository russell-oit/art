<%-- 
    Document   : activateTabularHeatmap
    Created on : 01-May-2017, 17:57:30
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-hottie-0.1.3/jquery.hottie.js"></script>

<script>
	var indexes = [];
	<c:if test="${not empty options.columns}">
	indexes =${options.columns};
	</c:if>

	var colors = [];
	<c:forEach var="color" items="${options.colors}">
	//https://stackoverflow.com/questions/16931350/how-to-pass-array-from-java-to-javascript
	var colorString = '${color}';
	colors[colors.length] = colorString;
	</c:forEach>

	var nullColor = '${options.nullColor}';

	var settings = {
		readValue: function (e) {
			return $(e).attr("data-value");
		}
	};

	if (colors.length > 0) {
		$.extend(settings, {
			colorArray: colors
		});
	}
	
	if(nullColor){
		$.extend(settings, {
			nullColor: nullColor
		});
	}

	<c:choose>
		<c:when test="${options.perColumn}">
	if (indexes.length === 0) {
		//https://stackoverflow.com/questions/6683882/jquery-how-to-count-table-columns
		var columnCount = $("table.heatmap > tbody > tr:first > td").length;
		for (var i = 1; i <= columnCount; i++) {
			var selector = 'table.heatmap tbody td:nth-child(' + i + ')';
			$(selector).hottie(settings);
		}
	} else {
		for (var i = 0; i < indexes.length; i++) {
			var selector = 'table.heatmap tbody td:nth-child(' + indexes[i] + ')';
			$(selector).hottie(settings);
		}
	}
		</c:when>
		<c:otherwise>
	if (indexes.length === 0) {
		$('table.heatmap tbody td').hottie(settings);
	} else {
		for (var i = 0; i < indexes.length; i++) {
			var selector = 'table.heatmap tbody td:nth-child(' + indexes[i] + ')';
			$(selector).addClass('heatmapData');
		}

		$("table.heatmap tbody td.heatmapData").hottie(settings);
	}
		</c:otherwise>
	</c:choose>


</script>

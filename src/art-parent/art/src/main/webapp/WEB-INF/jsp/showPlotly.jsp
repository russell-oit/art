<%-- 
    Document   : showPlotly
    Created on : 18-Apr-2018, 19:37:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div id="${chartId}">

</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/plotly-basic.min.js"></script>

<c:if test="${not empty localeFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/${localeFileName}"></script>
</c:if>


<script>
	//https://blog.sicara.com/compare-best-javascript-chart-libraries-2017-89fbe8cb112d
	//https://plot.ly/javascript/line-and-scatter/#line-and-scatter-plot
	var dataString = '${data}';
	var data = JSON.parse(dataString);

	var traces = [];

	var xColumn = '${xColumn}';
	var type = '${options.type}';
	var mode = '${options.mode}';
	var yColumnsString = '${yColumns}';
	var yColumns = JSON.parse(yColumnsString);

	if (xColumn) {
		var allX = [];
		var allY = [];
		
		yColumns.forEach(function (yCol, index) {
			allY[index] = [];
		});

		data.forEach(function (val) {
			allX.push(val[xColumn]);
			yColumns.forEach(function (yCol, index) {
				allY[index].push(val[yCol]);
			});
		});
		
		allY.forEach(function (rowYValue, index) {
			var trace = {
				x: allX,
				y: rowYValue,
				name: yColumns[index],
				type: type,
				mode: mode,
			};
			traces.push(trace);
		});
	}

	var layout = {};
	//https://community.plot.ly/t/remove-options-from-the-hover-toolbar/130/4
	//https://github.com/plotly/plotly.js/blob/master/src/components/modebar/buttons.js
	var config = {
		modeBarButtonsToRemove: ['sendDataToCloud'],
		displaylogo: false
	};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>
</c:if>

<script>
	Plotly.newPlot('${chartId}', traces, layout, config);
</script>

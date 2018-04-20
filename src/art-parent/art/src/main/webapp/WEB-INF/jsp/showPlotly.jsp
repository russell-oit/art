<%-- 
    Document   : showPlotly
    Created on : 18-Apr-2018, 19:37:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<c:if test="${not empty chartTypes}">
	<div class="row form-inline" style="margin-bottom: 10px; margin-right: 1px">
		<select class="form-control pull-right" id="select-${chartId}">
			<option value="--">--</option>
			<c:forEach var="chartType" items="${chartTypes}">
				<option value="${encode:forHtmlAttribute(chartType.value)}"><spring:message code="${chartType.localizedDescription}"/></option>
			</c:forEach>
		</select>
	</div>
</c:if>


<div id="${chartId}" style="margin-right: 2px">

</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/plotly-${bundle}.min.js"></script>

<c:if test="${not empty localeFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/${localeFileName}"></script>
</c:if>


<script>
	//https://blog.sicara.com/compare-best-javascript-chart-libraries-2017-89fbe8cb112d
	//https://plot.ly/javascript/line-and-scatter/#line-and-scatter-plot
	//https://github.com/plotly/plotly.js/tree/master/dist
	//https://github.com/plotly/plotly.js/issues/41
	//https://code.tutsplus.com/tutorials/create-interactive-charts-using-plotlyjs-getting-started--cms-29029
	//https://codeburst.io/notes-from-the-latest-plotly-js-release-b035a5b43e21
	//http://terokarvinen.com/2016/simple-line-graph-with-plotly-js
	//https://plot.ly/javascript/line-charts/
	var dataString = '${data}';
	var data = JSON.parse(dataString);

	var traces = [];

	var xColumn = '${xColumn}';
	var type = '${type}';
	var mode = '${mode}';
	var yColumnsString = '${yColumns}';
	var yColumns = JSON.parse(yColumnsString);
	var hole = ${options.hole};

	if (xColumn) {
		var allX = [];
		var allY = [];

		yColumns.forEach(function (yCol, index) {
			allY[index] = [];
		});

		//https://medium.com/@vworri/use-plotly-in-javascript-to-creat-a-bar-graph-from-json-82d7220b463d
		//https://github.com/plotly/plotly.js/issues/1104
		//https://stackoverflow.com/questions/45931909/apply-json-data-to-barchart-plotly
		data.forEach(function (val) {
			allX.push(val[xColumn]);
			yColumns.forEach(function (yCol, index) {
				allY[index].push(val[yCol]);
			});
		});

		allY.forEach(function (rowYValue, index) {
			var orientation = '${options.orientation}';
			var trace = {
				x: allX,
				y: rowYValue,
				name: yColumns[index],
				type: type,
				mode: mode,
				orientation: orientation,
				values: rowYValue,
				labels: allX,
				hole: hole
			};

			if (orientation === 'h' && type === 'bar') {
				trace.x = rowYValue;
				trace.y = allX;
			}

			var textPosition = '${options.textPosition}';
			var hoverInfo = '${options.hoverInfo}';
			if (hoverInfo) {
				trace.hoverinfo = hoverInfo;
			}

			if (${options.showText}) {
				trace.text = rowYValue;
				if (!textPosition) {
					textPosition = "auto";
				}
				trace.textposition = textPosition;
				if (!hoverInfo) {
					hoverInfo = "y";
				}
				trace.hoverinfo = hoverInfo;
			}
			traces.push(trace);
		});
	}

	var layout = {
		title: '${options.title}',
		xaxis: {
			title: '${options.xAxisTitle}'
		},
		yaxis: {
			title: '${options.yAxisTitle}'
		},
		showlegend: ${options.showLegend},
		barmode: '${options.barmode}'
	};

	var height = ${options.height};
	if (height > 0) {
		layout.height = height;
	}

	var width = ${options.width};
	if (width > 0) {
		layout.width = width;
	}

	//https://community.plot.ly/t/remove-options-from-the-hover-toolbar/130/4
	//https://github.com/plotly/plotly.js/blob/master/src/components/modebar/buttons.js
	//https://github.com/rwl/plotly/issues/6
	//https://github.com/plotly/plotly.js/issues/316
	//https://github.com/plotly/plotly.js/issues/185
	//https://plot.ly/javascript/configuration-options/#always-display-the-modebar
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

	function changeChartType() {
		//https://stackoverflow.com/questions/39104292/best-way-of-create-delete-restyle-graph-dynamically-with-plotly-js
		var newChartType = $('#select-${chartId} option:selected').val();
		if (newChartType !== '--') {
			traces.forEach(function (trace, index) {
				switch (newChartType) {
					case "line":
						trace.type = 'scatter';
						trace.mode = 'lines+markers';
						break;
					case "scatter":
						trace.type = 'scatter';
						trace.mode = 'markers';
						break;
					case "bar":
						trace.type = 'bar';
						break;
					case "pie":
						trace.type = 'pie';
						trace.hole = 0.0;
						break;
					case "donut":
						trace.type = 'pie';
						if (hole <= 0) {
							trace.hole = 0.4;
						} else {
							trace.hole = hole;
						}
						break;
					default:
						break;
				}
			});
			//https://plot.ly/javascript/plotlyjs-function-reference/#plotlyreact
			//https://github.com/plotly/plotly.js/issues/1218
			//https://github.com/plotly/plotly.js/issues/1850
			Plotly.react('${chartId}', traces, layout, config);
		}
	}

	//https://api.jquery.com/on/
	$("#select-${chartId}").on("change", changeChartType);
</script>

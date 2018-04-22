<%-- 
    Document   : showC3
    Created on : 19-Feb-2017, 13:47:07
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<c:if test="${not empty chartTypes}">
	<div class="row form-inline" style="margin-right: 1px">
		<select class="form-control pull-right" id="select-${chartId}">
			<option value="--">--</option>
			<c:forEach var="chartType" items="${chartTypes}">
				<option value="${encode:forHtmlAttribute(chartType.c3Type)}"><spring:message code="${chartType.localizedDescription}"/></option>
			</c:forEach>
		</select>
	</div>
</c:if>

<div id="${chartId}">

</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.js"></script>


<script type="text/javascript">
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = JSON.parse(dataString);

	//https://github.com/c3js/c3/issues/236
	//http://c3js.org/reference.html
	var data = {
		json: jsonData
	};

	var keys = {};
	var valueString = '${encode:forJavaScript(value)}';
	if (valueString) {
		var value = JSON.parse(valueString);
		keys.value = value;
		var x = '${encode:forJavaScript(x)}';
		if (x) {
			keys.x = x;
		}
		data.keys = keys;
	}

	//https://datahero.com/blog/2015/03/31/line-bar-graph-use-chart/
	var type = '${encode:forJavaScript(options.type)}';
	if (type) {
		data.type = type;
	}
	
	var groupsString = '${encode:forJavaScript(groups)}';
	if (groupsString) {
		var groups = JSON.parse(groupsString);
		data.groups = groups;
	}

	var options = {
		bindto: '#${chartId}',
		data: data
	};

	var axis = {};
	var xAxis = {};
	var yAxis = {};

	if (x) {
		//https://github.com/jonschlinkert/set-value
		xAxis.type = 'category';
	}

	var xAxisLabel = {};
	var yAxisLabel = {};

	xAxisLabel.text = '${encode:forJavaScript(options.xAxisLabel)}';
	xAxisLabel.position = '${encode:forJavaScript(options.xAxisLabelPosition)}';

	yAxisLabel.text = '${encode:forJavaScript(options.yAxisLabel)}';
	yAxisLabel.position = '${encode:forJavaScript(options.yAxisLabelPosition)}';

	xAxis.label = xAxisLabel;
	yAxis.label = yAxisLabel;

	axis.x = xAxis;
	axis.y = yAxis;

	axis.rotated = ${options.rotatedAxis};

	options.axis = axis;

	var width = ${options.width};
	var height = ${options.height};

	if (width > 0 || height > 0) {
		var size = {};
		if (width > 0) {
			size.width = width;
		}
		if (height > 0) {
			size.height = height;
		}
		options.size = size;
	}

	var legend = {};
	var tooltip = {};

	legend.show = ${options.showLegend};
	legend.position = '${encode:forJavaScript(options.legendPosition)}';

	tooltip.show = ${options.showTooltip};
	tooltip.grouped = ${options.groupedTooltip};

	options.legend = legend;
	options.tooltip = tooltip;
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>
</c:if>

<c:if test="${not empty cssFileName}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(cssFileName)}">
</c:if>

<script type="text/javascript">
	var chart = c3.generate(options);

	function changeChartType(event) {
		var chartObject = event.data.chartObject;
		var newChartType = $('#select-${chartId} option:selected').val();
		if (newChartType !== '--') {
			//https://github.com/c3js/c3/issues/844
			chartObject.transform(newChartType);
		}
	}

	//https://api.jquery.com/on/
	$("#select-${chartId}").on("change", {chartObject: chart}, changeChartType);
</script>

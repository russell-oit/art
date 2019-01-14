<%-- 
    Document   : showChart
    Created on : 30-Oct-2014, 15:58:59
    Author     : Timothy Anyona

Display a chart report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://sourceforge.net/projects/cewolf-art/tags" prefix="cewolf" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div align="center" style="width: 90%;">
	<cewolf:chart 
		id="${encode:forHtmlAttribute(htmlElementId)}" 
		title="${encode:forHtmlAttribute(chart.title)}" 
		type="${encode:forHtmlAttribute(chart.type)}" 
		xaxislabel="${encode:forHtmlAttribute(chart.xAxisLabel)}" 
		yaxislabel="${encode:forHtmlAttribute(chart.yAxisLabel)}"
		showlegend="${chart.chartOptions.showLegend}">

		<cewolf:colorpaint color="${encode:forHtmlAttribute(chart.chartOptions.backgroundColor)}"/>

		<cewolf:data>
			<cewolf:producer id="chart"/>
		</cewolf:data>

		<%-- run internal post processor. parameters are already set in the chart object, chartOptions property --%>
		<cewolf:chartpostprocessor id="chart"/>
	</cewolf:chart>

	<cewolf:img 
		chartid="${encode:forHtmlAttribute(htmlElementId)}" 
		renderer="/cewolf" 
		forceSessionId="false" 
		width="${chart.chartOptions.width}" 
		height="${chart.chartOptions.height}">

		<c:choose>
			<c:when test="${(not empty chart.hyperLinks) || (not empty chart.drilldownLinks)}">
				<cewolf:map tooltipgeneratorid="chart" linkgeneratorid="chart"
							target="${chart.openLinksInNewWindow ? '_blank' : '_self'}"/> 
			</c:when>
			<c:when test="${chart.hasTooltips}">
				<cewolf:map tooltipgeneratorid="chart"/> 
			</c:when>
		</c:choose>
	</cewolf:img>
</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster-4.2.6/css/tooltipster.bundle.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster-4.2.6/js/tooltipster.bundle.min.js"></script>

<script type="text/javascript">
	//https://github.com/iamceege/tooltipster/issues/527
	//https://github.com/iamceege/tooltipster/issues/364
	//https://stackoverflow.com/questions/7938259/all-but-not-jquery-selector
	//https://api.jquery.com/not-selector/
	//https://stackoverflow.com/questions/10687131/jquery-select-by-attribute-using-and-and-or-operators
	//https://stackoverflow.com/questions/6166871/jquery-multiple-selectors-in-a-not
	$('.tooltip-chart').not('.tooltipstered').tooltipster({
		delay: 0,
		animationDuration: 0
	});
</script>

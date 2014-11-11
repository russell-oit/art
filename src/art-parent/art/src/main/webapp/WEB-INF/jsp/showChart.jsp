<%-- 
    Document   : showChart
    Created on : 30-Oct-2014, 15:58:59
    Author     : Timothy Anyona

Display a chart report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" prefix="cewolf" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<cewolf:chart 
	id="${htmlElementId}" 
	plotbackgroundcolor="#FFFFFF"
	title="${chart.title}" 
	type="${chart.type}" 
	xaxislabel="${chart.xAxisLabel}" 
	yaxislabel="${chart.yAxisLabel}"
	showlegend="${chart.showLegend}"
	>

	<cewolf:colorpaint color="${chart.backgroundColor}"/>

	<cewolf:data>
		<cewolf:producer id="chart"/>
	</cewolf:data>

	<%-- run external post processors --%>
	<c:forEach var="pp" items="${externalPostProcessors}">
		<cewolf:chartpostprocessor id="${pp.id}">
			<c:forEach var="ppParam" items="${pp.params}">
				<cewolf:param name="${ppParam.key}" value="${ppParam.value}"/>
			</c:forEach>
		</cewolf:chartpostprocessor>
	</c:forEach>

	<%-- run internal post processor --%>
	<cewolf:chartpostprocessor id="chart">
		<c:forEach var="ppParam" items="${chart.internalPostProcessorParams}">
			<cewolf:param name="${ppParam.key}" value="${ppParam.value}"/>
		</c:forEach>
	</cewolf:chartpostprocessor>
</cewolf:chart>

<cewolf:img 
	chartid="${htmlElementId}" 
	renderer="/cewolf" 
	width="${chart.width}" 
	height="${chart.height}"
	removeAfterRender="true"
	>

	<c:if test="${chart.hasHyperLinks || chart.hasDrilldown}">
		<cewolf:map tooltipgeneratorid="chart" linkgeneratorid="chart"
					target="${chart.drilldown.openInNewWindow ? '_blank' : '_self'}"/> 
	</c:if>
</cewolf:img>

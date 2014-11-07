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
	yaxislabel="${chart.yAxisLabel}">

	<cewolf:colorpaint color="${chart.bgColor}"/>

	<cewolf:data>
		<cewolf:producer id="chart"/>
	</cewolf:data>
</cewolf:chart>

<cewolf:img 
	chartid="${htmlElementId}" 
	renderer="/cewolf" 
	width="${chart.width}" 
	height="${chart.height}"
	removeAfterRender="true"
	>
</cewolf:img>

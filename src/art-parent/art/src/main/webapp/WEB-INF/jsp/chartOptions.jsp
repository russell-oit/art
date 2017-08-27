<%-- 
    Document   : chartOptions
    Created on : 27-Aug-2017, 13:53:02
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div id="chartOptions">
	<div class="form-group">
		<label class="control-label ${labelColClass}">
			<spring:message code="reports.label.show"/>
		</label>
		<div class="${inputColClass}">
			<label class="checkbox-inline">
				<input type="checkbox" name="showLegend" value=""
					   ${chartOptions.showLegend ? "checked" : ""}>
				<spring:message code="reports.label.showLegend"/>
			</label>
			<label class="checkbox-inline">
				<input type="checkbox" name="showLabels" value=""
					   ${chartOptions.showLabels ? "checked" : ""}>
				<spring:message code="reports.label.showLabels"/>
			</label>
			<label class="checkbox-inline">
				<input type="checkbox" name="showPoints" value=""
					   ${chartOptions.showPoints ? "checked" : ""}>
				<spring:message code="reports.label.showPoints"/>
			</label>
			<label class="checkbox-inline">
				<input type="checkbox" name="showData" value=""
					   ${chartOptions.showData ? "checked" : ""}>
				<spring:message code="reports.label.showData"/>
			</label>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label ${labelColClass}" for="chartWidth">
			<spring:message code="reports.label.width"/>
		</label>
		<div class="${inputColClass}">
			<input type="text" name="chartWidth" 
				   maxlength="4" class="form-control"
				   value="${chartOptions.width}">
		</div>
	</div>
	<div class="form-group">
		<label class="control-label ${labelColClass}" for="chartHeight">
			<spring:message code="reports.label.height"/>
		</label>
		<div class="${inputColClass}">
			<input type="text" name="chartHeight" 
				   maxlength="4" class="form-control"
				   value="${chartOptions.height}">
		</div>
	</div>
	<c:if test="${enableSwapAxes}">
		<div class="form-group">
			<label class="control-label ${labelColClass}" for="swapAxes">
				<spring:message code="reports.label.swapAxes"/>
			</label>
			<div class="${inputColClass}">
				<label>
					<input type="checkbox" name="swapAxes" id="swapAxes" value=""
						   ${reportOptions.swapAxes ? "checked" : ""}>
				</label>
			</div>
		</div>
	</c:if>
</div>

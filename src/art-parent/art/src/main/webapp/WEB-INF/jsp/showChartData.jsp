<%-- 
    Document   : showChartData
    Created on : 29-Mar-2016, 06:37:45
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div align="center" style="width: 90%;">
	<table style="width: 90%;">
		<c:forEach var="row" items="${dataRows}" varStatus="loop">
			<c:if test="${loop.count == 1}">
				<tr>
					<c:forEach var="dynaProperties" items="${row.dynaClass.dynaProperties}">
						<c:set var="columnName" value="${dynaProperties.name}"/>
						<td class="chartDataHeader">
							${columnName}
						</td>
					</c:forEach>
				</tr>
			</c:if>
			<tr>
				<c:forEach var="dynaProperties" items="${row.dynaClass.dynaProperties}">
					<c:set var="columnName" value="${dynaProperties.name}"/>
					<c:set var="columnValue" value="${row.get(columnName)}"/>
					<td class="chartData">
						${columnValue}
					</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</table>
</div>

<%-- 
    Document   : showChartData
    Created on : 29-Mar-2016, 06:37:45
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<br>
<div align="center" style="width: 90%;">
	<table style="width: 90%;" class="table table-bordered table-striped table-condensed">
		<thead>
			<tr>
				<c:forEach var="column" items="${columns}">
					<c:set var="columnName" value="${column.name}"/>
					<th>
						${encode:forHtmlContent(columnName)}
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="row" items="${dataRows}">
				<tr>
					<c:forEach var="dynaProperties" items="${row.dynaClass.dynaProperties}">
						<c:set var="columnName" value="${dynaProperties.name}"/>
						<c:set var="columnValue" value="${row.get(columnName)}"/>
						<td>
							${encode:forHtmlContent(columnValue)}
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

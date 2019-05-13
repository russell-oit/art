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
				<c:forEach var="columnLabel" items="${columnLabels}">
					<th>
						${encode:forHtmlContent(columnLabel)}
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="row" items="${data}">
				<tr>
					<c:forEach var="columnName" items="${columnNames}">
						<td>
							${encode:forHtmlContent(row.get(columnName))}
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

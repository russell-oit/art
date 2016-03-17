<%-- 
    Document   : showSelectedParameters
    Created on : 17-Mar-2016, 07:49:39
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div align="center">
	<table border="0" width="90%">
		<tr>
			<td>
				<div align="center" width="90%" class="greyBack">
					<c:forEach var="parameterDisplayValue" items="${parameterDisplayValues}">
						${parameterDisplayValue.value}
						<br>
					</c:forEach>
				</div>
			</td>
		</tr>
	</table>
</div>
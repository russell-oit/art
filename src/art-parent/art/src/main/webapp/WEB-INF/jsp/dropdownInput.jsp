<%-- 
    Document   : dropdownInput
    Created on : 08-Mar-2016, 17:33:50
    Author     : Timothy Anyona

Display report parameter that uses dropdown input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<select class="form-control selectpicker"
		name="${reportParam.htmlElementName}"
		id="${reportParam.htmlElementName}">
	<c:forEach var="lovValue" items="${lovValues}">
		<option value="${lovValue.key}" ${reportParam.htmlValue == lovValue.key ? "selected" : ""}>${lovValue.value}</option>
	</c:forEach>
</select>

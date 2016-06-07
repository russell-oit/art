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
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<select class="form-control"
		name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		${reportParam.parameter.parameterType == 'MultiValue' ? "multiple size='5'" : ""}>
	
	<c:if test="${reportParam.parameter.parameterType == 'MultiValue'}">
		<option value="ALL_ITEMS"><spring:message code="reports.text.allItems"/></option>
	</c:if>
	<c:forEach var="lovValue" items="${lovValues}">
		<option value="${lovValue.key}" ${reportParam.htmlValue == lovValue.key ? "selected" : ""}>${lovValue.value}</option>
	</c:forEach>
</select>

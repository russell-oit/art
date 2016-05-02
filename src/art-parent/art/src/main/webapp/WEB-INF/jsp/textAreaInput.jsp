<%-- 
    Document   : textareaInput
    Created on : 08-Mar-2016, 17:19:51
    Author     : Timothy Anyona

Display report parameter that uses textarea input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<textarea rows="5" class="form-control"
		  name="${encode:forHtmlAttribute(reportParam.htmlElementName)}">
	${encode:forHtmlContent(reportParam.htmlValue)}
</textarea>

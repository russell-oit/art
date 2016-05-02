<%-- 
    Document   : showText
    Created on : 31-Oct-2014, 12:28:01
    Author     : Timothy Anyona

Displays text report output
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<p>${encode:forHtmlContent(reportSource)}</p>

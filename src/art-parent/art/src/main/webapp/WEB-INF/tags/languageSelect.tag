<%-- 
    Document   : languageSelect
    Created on : 26-Jan-2014, 13:13:18
    Author     : Timothy Anyona

Generate html select input for application languages
--%>

<%@tag description="Generate html select input for application languages" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@tag body-content="empty" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="message"%>

<%-- any content can be specified here e.g.: --%>

<%-- select must have name of "lang" as per spring configuration in dispatcherServlet.xml --%>
<c:set var="localeCode" value="${pageContext.response.locale}"/>
<select name="lang" id="lang" class="form-control">
	<option value="en">English</option>
	<c:forEach var="language" items="${languages}">
		<option value="${language.key}" ${localeCode == language.key ? "selected" : ""}>${language.value}</option>
	</c:forEach>
</select>
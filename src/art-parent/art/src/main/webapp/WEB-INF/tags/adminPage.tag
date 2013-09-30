<%-- 
    Document   : adminPage
    Created on : 13-Sep-2013, 18:35:48
    Author     : Timothy Anyona

Template for admin pages
--%>

<%@tag description="Admin Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">

	<jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:mainPage>
<%-- 
    Document   : adminPage
    Created on : 13-Sep-2013, 18:35:48
    Author     : Timothy Anyona

Template for admin pages
--%>

<%@tag description="Admin Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="${title}">
	<jsp:attribute name="headContent">
		<jsp:include page="/WEB-INF/jsp/adminHeadContent.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="pageHeader">
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	</jsp:attribute>
	
	 <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:genericPage>
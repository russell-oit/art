<%-- 
    Document   : adminPage
    Created on : 13-Sep-2013, 18:35:48
    Author     : Timothy Anyona
--%>

<%@tag description="Admin Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="${title}">
	<jsp:attribute name="head_area">
		<jsp:include page="/WEB-INF/jsp/adminConsole.jsp"/>
	</jsp:attribute>
		
		<jsp:attribute name="header">
			
		</jsp:attribute>
</t:genericPage>
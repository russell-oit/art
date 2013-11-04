<%-- 
    Document   : headerlessPage
    Created on : 04-Nov-2013, 08:44:05
    Author     : Timothy Anyona

Template for a headerless page (page without the header/menu/navbar)
Includes elements of a generic page plus a page footer
--%>

<%@tag description="Headerless Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="ART - ${title}">
	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:genericPage>
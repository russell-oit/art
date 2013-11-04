<%-- 
    Document   : mainPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Template for any main application page.
Includes bootstrap css, page header (navbar), page footer
bootstrap js, jquery js
--%>

<%@tag description="Main Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" %>
<%@attribute name="artTitle" %>
<%@attribute name="pageCss" fragment="true" %>
<%@attribute name="pageJavascript" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="${empty artTitle? 'ART - '.concat(title) : artTitle}">
	<jsp:attribute name="pageCss">
		<jsp:invoke fragment="pageCss"/>
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/twitter-bootstrap-hover-dropdown.min.js"></script>

		<jsp:invoke fragment="pageJavascript"/>
	</jsp:attribute>

	<jsp:attribute name="pageHeader">
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:genericPage>
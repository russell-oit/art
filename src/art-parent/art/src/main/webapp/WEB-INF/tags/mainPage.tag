<%-- 
    Document   : mainPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Template for any main application page.
Includes bootstrap css, page header (navbar), page footer
bootstrap js, jquery js, datatables css, datatables js
--%>

<%@tag description="Main Page Template" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="ART - ${title}">
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/datatables-jowin.css">

		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art-3.js"></script>
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0.min.js"></script>
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/twitter-bootstrap-hover-dropdown.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables-1.9.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/datatables-jowin.js"></script>
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.1.0.min.js"></script>
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

		<script type="text/javascript">
			$('#spinner').ajaxStart(function() {
				$(this).fadeIn('fast');
			}).ajaxStop(function() {
				$(this).stop().fadeOut('fast');
			});
		</script>

		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:attribute name="header">
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="footer">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
		<div id="spinner">
		<img src="${pageContext.request.contextPath}/images/spinner.gif" alt="Executing..." />
	</div>
        <jsp:doBody/>
    </jsp:body>
</t:genericPage>
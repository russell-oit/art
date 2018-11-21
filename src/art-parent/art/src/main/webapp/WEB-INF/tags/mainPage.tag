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
<%@attribute name="headContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:genericPage title="ART - ${title}">
	<jsp:attribute name="headContent">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.7/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-dropdown-hover-4.2.0/jquery.bootstrap-dropdown-hover.min.js"></script>

		<meta name="_csrf" content="${_csrf.token}"/>
		<meta name="_csrf_header" content="${_csrf.headerName}"/>

		<jsp:invoke fragment="headContent"/>
	</jsp:attribute>

	<jsp:attribute name="css">
		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<script type="text/javascript">
			$(document).ajaxStart(function () {
				$('#spinner').show();
			}).ajaxStop(function () {
				$('#spinner').hide();
			});

			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			$(document).ajaxSend(function (e, xhr, options) {
				if (header) {
					xhr.setRequestHeader(header, token);
				}
			});

			$(function () {
				$('[data-toggle="dropdown"]').bootstrapDropdownHover({
					hideTimeout: 100
				});
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
			<img src="${pageContext.request.contextPath}/images/spinner.gif" alt="Processing..." />
		</div>
		<jsp:doBody/>
	</jsp:body>
</t:genericPage>
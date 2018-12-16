<%-- 
    Document   : mainPageWithPanel
    Created on : 23-Dec-2013, 11:19:27
    Author     : Timothy Anyona

Template for main page with contents in one panel
Includes main page, plus a bootstrap panel where the contents go
--%>

<%@tag description="Template for main page with contents in one main panel" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="abovePanel" fragment="true" %>
<%@attribute name="belowPanel" fragment="true" %>
<%@attribute name="leftPanel" fragment="true" %>
<%@attribute name="rightPanel" fragment="true" %>
<%@attribute name="headContent" fragment="true" %>
<%@attribute name="title" required="true" %>
<%@attribute name="mainColumnClass" %>
<%@attribute name="panelTitle" %>
<%@attribute name="hasTable" type="java.lang.Boolean" %>
<%@attribute name="configPage" type="java.lang.Boolean" %>
<%@attribute name="hasNotify" type="java.lang.Boolean" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty mainColumnClass}">
	<c:set var="mainColumnClass" value="col-md-12"/>
</c:if>

<c:if test="${empty panelTitle}">
	<c:set var="panelTitle" value="${title}"/>
</c:if>

<t:mainPage title="${title}">
	<jsp:attribute name="css">
		<c:if test="${hasTable || configPage}">
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.18/css/dataTables.bootstrap.min.css"/>
		</c:if>

		<c:if test="${configPage}">
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.6/css/select.bootstrap.min.css"/>
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/css/buttons.dataTables.min.css"/>
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/css/buttons.bootstrap.min.css"/>
			<!--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/css/responsive.bootstrap.min.css"/>-->
		</c:if>

		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<c:if test="${hasTable || configPage}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.18/js/jquery.dataTables.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.18/js/dataTables.bootstrap.min.js"></script>
		</c:if>

		<c:if test="${configPage}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.6/js/dataTables.select.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/dataTables.buttons.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.bootstrap.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/JSZip-2.5.0/jszip.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.36/pdfmake.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.36/vfs_fonts.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.html5.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.print.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.colVis.min.js"></script>
	<!--		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/js/dataTables.responsive.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Responsive-2.0.2/js/responsive.bootstrap.min.js"></script>-->

		</c:if>
			
		<c:if test="${hasNotify || configPage}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/notifyjs-0.4.2/notify.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		</c:if>

		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:attribute name="headContent">
		<jsp:invoke fragment="headContent"/>
	</jsp:attribute>

	<jsp:body>
		<jsp:invoke fragment="abovePanel"/>

		<div class="row">
			<jsp:invoke fragment="leftPanel"/>

			<div class="${encode:forHtmlAttribute(mainColumnClass)}">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							${encode:forHtmlContent(panelTitle)}
						</h4>
					</div>
					<div class="panel-body">
						<jsp:doBody/>
					</div>
				</div>
			</div>

			<jsp:invoke fragment="rightPanel"/>
		</div>

		<jsp:invoke fragment="belowPanel"/>
    </jsp:body>
</t:mainPage>
<%-- 
    Document   : showDashboard
    Created on : 16-Mar-2016, 06:53:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.0.3/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art-3.css">

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables-1.10.0/bootstrap/3/dataTables.bootstrap.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.4.3/bootstrap-select-modified.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables-1.10.0/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables-1.10.0/bootstrap/3/dataTables.bootstrap.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.1.0.min.js"></script>

		<script type="text/javascript">
			$(document).ajaxStart(function () {
				$('#spinner').show();
			}).ajaxStop(function () {
				$('#spinner').hide();
			});
		</script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/prototype.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/scriptaculous/scriptaculous.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/overlib.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ajaxtags.js"></script>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ajaxtags-art.css" /> 

	</head>
	<body>
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>

		<div align="left">
			<table class="plain">
				<tr>
					<c:forEach var="column" items="${dashboard.columns}">
						<td>
							<c:forEach var="portlet" items="${column}">
								<div id="div_${portlet.source}">
									<ajax:portlet
										source="portlet_${portlet.source}"
										baseUrl="${portlet.baseUrl}"
										classNamePrefix="${portlet.classNamePrefix}"
										title="${portlet.title}"
										imageMaximize="${pageContext.request.contextPath}/images/maximize.png"
										imageMinimize="${pageContext.request.contextPath}/images/minimize.png"
										imageRefresh="${pageContext.request.contextPath}/images/refresh.png"             
										executeOnLoad= "${portlet.executeOnLoad}"
										refreshPeriod="${portlet.refreshPeriod}"
										/>
								</div>
							</c:forEach>
						</td>

					</c:forEach>

				</tr>
			</table>
		</div> 

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>

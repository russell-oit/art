<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.5.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
	</head>
	<body>

		<jsp:include page="/WEB-INF/jsp/header.jsp"/>

		<table class="centerTableAuto">
			<tr>
				<td colspan="2" class="data" align="center">
					<b><span style="color:red"> 
							<spring:message code="page.message.errorOccurred"/>
						</span>
					</b> 
				</td>
			</tr>
			<tr>
				<td class=attr>
			<spring:message code="analysis.text.message"/>:
		</td>
		<td class=data>
			${msg}
		</td>
	</tr>
</table>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

</body>
</html>

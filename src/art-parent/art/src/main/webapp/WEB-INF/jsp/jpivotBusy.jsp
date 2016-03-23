<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.0.3/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art-3.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art-3.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
	</head>
	<body>

		<jsp:include page="/WEB-INF/jsp/header.jsp"/>

		<%
			java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());
		%>

		<table class="centerTableAuto">
			<tr>
				<td colspan="2" class="data" align="center"> <b><span style="color:red"><%=messages.getString("jpivotQueryWait")%></span></b>
					<img src="<%= request.getContextPath()%>/images/spinner.gif">
				</td>
			</tr>
			<tr>
				<td class=attr> <%=messages.getString("message")%>
				</td>
				<td class=data><%=messages.getString("jpivotResultsWait")%>
				</td>
			</tr>
		</table>


		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

	</body>
</html>
<%-- 
    Document   : genericPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Base template for any application page.
Has elements common to all pages
--%>

<%@tag description="Generic Page Template" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>
<%@attribute name="headContent" fragment="true" %>
<%@attribute name="pageHeader" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<!DOCTYPE html>
<html>
	<head>
		<title>${title}</title>
		<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/font-awesome.min.css">
		<jsp:invoke fragment="headContent"/>
	</head>
	<body>
		<jsp:invoke fragment="pageHeader"/>
		<div id="container">
			<jsp:doBody/>
		</div>
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>
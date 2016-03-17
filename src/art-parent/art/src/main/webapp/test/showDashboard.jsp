<%-- 
    Document   : showDashboard
    Created on : 16-Mar-2016, 06:53:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/prototype.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/scriptaculous/scriptaculous.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/overlib.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ajaxtags.js"></script>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ajaxtags-art.css" /> 

	</head>
	<body>
		<div align="left">
			<table class="plain">
				<tr>
					<td>
						<div id="div0">
							<ajax:portlet
								source="portlet0"
								baseUrl="/art/test/test.jsp"
								classNamePrefix="portletAUTO"
								title="iko"
								imageMaximize="${pageContext.request.contextPath}/images/maximize.png"
								imageMinimize="${pageContext.request.contextPath}/images/minimize.png"
								imageRefresh="${pageContext.request.contextPath}/images/refresh.png"             
								executeOnLoad= "true"
								/>
						</div>
					</td>
				</tr>
			</table>
		</div> 
	</body>
</html>

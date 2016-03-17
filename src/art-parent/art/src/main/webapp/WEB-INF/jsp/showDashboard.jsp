<%-- 
    Document   : showDashboard
    Created on : 16-Mar-2016, 06:53:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<t:mainPage title="${reportName}">
	
	<jsp:attribute name="javascript">
		
	</jsp:attribute>
	<jsp:body>
		<b>${dashboard.title}</b> <br>
		&nbsp;&nbsp;&nbsp;${dashboard.description}

		<div id="reportOutput" class="col-md-10 col-md-offset-1">
			<jsp:include page="/WEB-INF/jsp/showDashboardInline.jsp"/>
		</div>
	</jsp:body>
</t:mainPage>

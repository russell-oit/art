<%-- 
    Document   : mainPageWithPanel
    Created on : 23-Dec-2013, 11:19:27
    Author     : Timothy Anyona

Template for main page with contents in one panel
Includes main page, plus a bootstrap panel where the contents go
--%>

<%@tag description="Template for main page with contents in one panel" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true" %>
<%@attribute name="columnClass" required="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
	<jsp:attribute name="css">
		<jsp:invoke fragment="css"/>
	</jsp:attribute>
	
	<jsp:attribute name="javascript">
		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="${columnClass}">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">${fn:escapeXml(title)}</h4>
					</div>
					<div class="panel-body">
						<jsp:doBody/>
					</div>
				</div>
			</div>
		</div>
    </jsp:body>
</t:mainPage>
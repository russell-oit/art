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
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="mainColumnClass" required="true" %>
<%@attribute name="mainPanelTitle" %>
<%@attribute name="aboveMainPanel" fragment="true" %>
<%@attribute name="belowMainPanel" fragment="true" %>
<%@attribute name="leftMainPanel" fragment="true" %>
<%@attribute name="rightMainPanel" fragment="true" %>
<%@attribute name="headContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
	<jsp:attribute name="css">
		<jsp:invoke fragment="css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>
	
	<jsp:attribute name="headContent">
		<jsp:invoke fragment="headContent"/>
	</jsp:attribute>

	<jsp:body>
		<jsp:invoke fragment="aboveMainPanel"/>

		<div class="row">
			<jsp:invoke fragment="leftMainPanel"/>

			<div class="${mainColumnClass}">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							<c:choose>
								<c:when test="${empty mainPanelTitle}">
									${fn:escapeXml(title)}
								</c:when>
								<c:otherwise>
									${fn:escapeXml(mainPanelTitle)}
								</c:otherwise>
							</c:choose>
						</h4>
					</div>
					<div class="panel-body">
						<jsp:doBody/>
					</div>
				</div>
			</div>

			<jsp:invoke fragment="rightMainPanel"/>
		</div>

		<jsp:invoke fragment="belowMainPanel"/>
    </jsp:body>
</t:mainPage>
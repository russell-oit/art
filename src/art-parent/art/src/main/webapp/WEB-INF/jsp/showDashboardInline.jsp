<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/prototype.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/scriptaculous/scriptaculous.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/overlib.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/ajaxtags.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ajaxtags-art.css" /> 

<div align="left">
	<table class="plain">
		<tr>

			<c:forEach var="column" items="${dashboard.columns}">
				<td>
					<c:forEach var="portlet" items="${column}">
						<ajax:portlet
							source="${portlet.source}"
							baseUrl="${portlet.baseUrl}"
							classNamePrefix="${portlet.classNamePrefix}"
							title="${portlet.title}"
							imageMaximize="${pageContext.request.contextPath}/images/maximize.png"
							imageMinimize="${pageContext.request.contextPath}/images/minimize.png"
							imageRefresh="${pageContext.request.contextPath}/images/refresh.png"             
							executeOnLoad= "${portlet.executeOnLoad}"
							refreshPeriod="${portlet.refreshPeriod}"
							/>
					</c:forEach>
				</td>

			</c:forEach>

		</tr>
	</table>
</div> 

<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="container-fluid">
	<div class="col-md-12">
		<div class="row">
			<h2>${dashboard.title}  
				<small>  ${dashboard.description}</small>
			</h2>
		</div>
		<div class="row">
			<table class="plain">
				<tr>
					<c:forEach var="column" items="${dashboard.columns}">
						<td style="vertical-align: top">
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
										preFunction="artAddWork"
										postFunction="artRemoveWork"
										/>
								</div>
							</c:forEach>
						</td>
					</c:forEach>
                </tr>
			</table>
		</div> 
    </div>
	<!-- Container-fluid -->
</div>

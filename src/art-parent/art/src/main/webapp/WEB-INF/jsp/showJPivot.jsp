<%-- 
    Document   : showJPivot
    Created on : 22-Mar-2016, 07:26:30
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://sourceforge.net/projects/jpivot-art/tags" prefix="jp" %>
<%@taglib uri="http://sourceforge.net/projects/wcf-art/tags" prefix="wcf" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

		<meta name="_csrf" content="${_csrf.token}"/>
		<meta name="_csrf_header" content="${_csrf.headerName}"/>

        <title>${reportName} - ART</title>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/jpivot/table/mdxtable.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/jpivot/navi/mdxnavi.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wcf/form/xform.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wcf/table/xtable.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wcf/tree/xtree.css" />
		<script type="text/javascript" src="${pageContext.request.contextPath}/wcf/scroller.js"></script>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.7.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jpivot.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.7/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>

	</head>
	<body>
		<div id="wrap">
			<jsp:include page="/WEB-INF/jsp/header.jsp"/>

			<div id="pageContent">
				<div class="container-fluid">

					<% if (request.getParameter("action") == null && request.getParameter("null") == null) { %>
					<c:choose>
						<c:when test="${reportType == 'JPivotMondrian'}">
							<jp:mondrianQuery id="${jpivotQueryId}" jdbcDriver="${databaseDriver}"
											  jdbcUrl="${databaseUrl}" jdbcUser="${databaseUser}"
											  jdbcPassword="${databasePassword}" catalogUri="${schemaFile}"
											  role="${roles}">
								${query}
							</jp:mondrianQuery>
						</c:when>
						<c:otherwise>
							<jp:xmlaQuery id="${jpivotQueryId}" uri="${xmlaUrl}"
										  dataSource="${xmlaDatasource}" catalog="${xmlaCatalog}">
								${query}
							</jp:xmlaQuery>
						</c:otherwise>
					</c:choose>
					<% }%>


					<table class="pivot" style="margin: 0 auto; width: 50%">
						<tr>
							<td class="title">
								<b> <br> ${encode:forHtmlContent(title)} </b> <br> <br>
							</td>
						</tr>

						<tr>
							<td>
								<br>

								<form action="showJPivot" method="post">
									<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
									<input type="hidden" name="action" value="edit">
									<input type="hidden" name="reportId" value="${reportId}">

									<%-- define table, navigator and forms --%>
									<wcf:scroller />

									<jp:table id="${tableId}" query="${modelQueryId}"/>
									<jp:navigator id="${navigatorId}" query="${modelQueryId}" visible="false" locale="${pageContext.response.locale}"/>
									<wcf:form id="${mdxEditId}" xmlUri="/WEB-INF/jpivot/table/mdxedit.xml" model="${modelQueryId}" visible="false" locale="${pageContext.response.locale}"/>
									<wcf:form id="${sortFormId}" xmlUri="/WEB-INF/jpivot/table/sortform.xml" model="${modelTableId}" visible="false" locale="${pageContext.response.locale}"/>

									<jp:print id="${printId}"/>
									<wcf:form id="${printFormId}" xmlUri="/WEB-INF/jpivot/print/printpropertiesform.xml" model="${modelPrintId}" visible="false" locale="${pageContext.response.locale}"/>

									<jp:chart id="${chartId}" query="${modelQueryId}" visible="false"/>
									<wcf:form id="${chartFormId}" xmlUri="/WEB-INF/jpivot/chart/chartpropertiesform.xml" model="${modelChartId}" visible="false" locale="${pageContext.response.locale}"/>
									<wcf:table id="${queryDrillThroughTable}" visible="false" selmode="none" editable="true"/>


									<%-- define a toolbar --%>
									<wcf:toolbar id="${toolbarId}" bundle="net.sf.jpivotart.jpivot.toolbar.resources" locale="${pageContext.response.locale}">
										<wcf:scriptbutton id="cubeNaviButton" tooltip="toolb.cube" img="cube" model="${navigatorVisible}"/>
										<wcf:scriptbutton id="mdxEditButton" tooltip="toolb.mdx.edit" img="mdx-edit" model="${mdxEditVisible}"/>
										<wcf:scriptbutton id="sortConfigButton" tooltip="toolb.table.config" img="sort-asc" model="${sortFormVisible}"/>
										<wcf:separator/>
										<wcf:scriptbutton id="levelStyle" tooltip="toolb.level.style" img="level-style" model="${tableLevelStyle}"/>
										<wcf:scriptbutton id="hideSpans" tooltip="toolb.hide.spans" img="hide-spans" model="${tableHideSpans}"/>
										<wcf:scriptbutton id="propertiesButton" tooltip="toolb.properties"  img="properties" model="${tableShowProperties}"/>
										<wcf:scriptbutton id="nonEmpty" tooltip="toolb.non.empty" img="non-empty" model="${tableNonEmptyButtonPressed}"/>
										<wcf:scriptbutton id="swapAxes" tooltip="toolb.swap.axes"  img="swap-axes" model="${tableSwapAxesButtonPressed}"/>
										<wcf:separator/>
										<wcf:scriptbutton model="${tableDrillMemberEnabled}" tooltip="toolb.navi.member" radioGroup="navi" id="drillMember" img="navi-member"/>
										<wcf:scriptbutton model="${tableDrillPositionEnabled}" tooltip="toolb.navi.position" radioGroup="navi" id="drillPosition" img="navi-position"/>
										<wcf:scriptbutton model="${tableDrillReplaceEnabled}" tooltip="toolb.navi.replace" radioGroup="navi" id="drillReplace" img="navi-replace"/>
										<wcf:scriptbutton model="${tableDrillThroughEnabled}" tooltip="toolb.navi.drillthru" id="drillThrough01" img="navi-through"/>
										<wcf:separator/>
										<wcf:scriptbutton id="chartButton01" tooltip="toolb.chart" img="chart" model="${chartVisible}"/>
										<wcf:scriptbutton id="chartPropertiesButton01" tooltip="toolb.chart.config" img="chart-config" model="${chartFormVisible}"/>
										<wcf:separator/>
										<wcf:scriptbutton id="printPropertiesButton01" tooltip="toolb.print.config" img="print-config" model="${printFormVisible}"/>
										<wcf:imgbutton id="printpdf" tooltip="toolb.print" img="print" href="${printPdf}" />
										<wcf:imgbutton id="printxls" tooltip="toolb.excel" img="excel" href="${printExcel}" />
									</wcf:toolbar>

									<%-- render toolbar --%>
									<wcf:render ref="${toolbarId}" xslUri="/WEB-INF/jpivot/toolbar/htoolbar.xsl" xslCache="true"/>

									<p>
										<%-- if there was an overflow, show error message --%>
										<c:if test="${not empty overflowResult}">
										<p><strong style="color: red">${overflowResult}</strong></p>
										</c:if>
									</p>


									<%-- render navigator --%>
									<wcf:render ref="${navigatorId}" xslUri="/WEB-INF/jpivot/navi/navigator.xsl" xslCache="true"/>

									<%-- edit mdx --%>
									<c:if test="${mdxEditIsVisible}">
										<wcf:render ref="${mdxEditId}" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>
									</c:if>

									<%-- sort properties --%>
									<wcf:render ref="${sortFormId}" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

									<%-- chart properties --%>
									<wcf:render ref="${chartFormId}" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

									<%-- print properties --%>
									<wcf:render ref="${printFormId}" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

									<!-- render the table -->
									<p>
										<br />
										<wcf:render ref="${tableId}" xslUri="/WEB-INF/jpivot/table/mdxtable.xsl" xslCache="true" locale="${pageContext.response.locale}"/>
									</p>

									<p>
										<spring:message code="jpivot.text.slicer"/>:
										<wcf:render ref="${tableId}" xslUri="/WEB-INF/jpivot/table/mdxslicer.xsl" xslCache="true"/>
									</p>

									<p>
										<!-- drill through table -->
										<wcf:render ref="${queryDrillThroughTable}" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true" locale="${pageContext.response.locale}"/>
									</p>

									<p>
										<!-- render chart -->
										<wcf:render ref="${chartId}" xslUri="/WEB-INF/jpivot/chart/chart.xsl" xslCache="true"/>
									</p>
								</form>
								<br>
							</td>
						</tr>

						<c:if test="${sessionUser.hasPermission('save_reports')}">
							<tr>
								<td class="info">
									<i><spring:message code="jpivot.text.saveCurrentView"/></i>
									<br>

									<form method="post" action="saveJPivot">
										<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
										<input type="hidden" name="pivotReportId" value="${reportId}" />
										<table>
											<tr>
												<td>
													<spring:message code="page.text.name"/>
												</td>
												<td style="padding-bottom: 5px">
													<input type="text" name="newPivotName" value="" size="20" maxlength="50" />
													<c:if test="${exclusiveAccess}">
														<input type="checkbox" name="overwrite" />&nbsp;<spring:message code="reports.text.overwrite"/>&nbsp;
														<input type="checkbox" name="delete" />&nbsp;<spring:message code="page.action.delete"/>&nbsp;
													</c:if>
													<button type="submit" id="save" class="btn btn-default action">
														<spring:message code="page.button.save"/>
													</button>
												</td>
											</tr>
											<tr>
												<td>
													<spring:message code="page.text.description"/>&nbsp;
												</td>
												<td>
													<input type="text" name="newPivotDescription" value="" size="45" maxlength="2000">
												</td>
											</tr>
										</table>
									</form>
								</td>
							</tr>
						</c:if>
					</table>
				</div>
			</div>
			<div id="push"></div>
		</div>
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>

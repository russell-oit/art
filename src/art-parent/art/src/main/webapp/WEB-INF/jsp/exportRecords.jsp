<%-- 
    Document   : exportRecords
    Created on : 21-Jan-2018, 14:11:36
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.exportRecords" var="exportRecordsText"/>
<c:set var="panelTitle">
	${exportRecordsText} - ${exportRecords.recordType.value}
</c:set>
<c:set var="pageTitle">
	${panelTitle}
</c:set>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				$("input[name='location']").change(function () {
					toggleVisibleFields();
				});

				toggleVisibleFields(); //show/hide on page load
			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var location = $("input[name='location']:checked").val();

				//https://stackoverflow.com/questions/14910760/switch-case-as-string
				switch (location) {
					case 'File':
						$("#datasourceDiv").hide();
						break;
					case 'Datasource':
						$("#datasourceDiv").show();
						break;
					default:
						break;
				}
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/exportRecords"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="exportRecords">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
					</div>
				</c:if>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p><encode:forHtmlContent value="${error}"/></p>
						</c:if>
					</div>
				</c:if>

				<form:hidden path="recordType"/>

				<c:choose>
					<c:when test="${exportRecords.recordType.value == 'Settings'}">
						<form:hidden path="ids"/>
					</c:when>
					<c:otherwise>
						<div class="form-group">
							<label class="control-label col-md-4">
								<spring:message code="page.label.ids"/>
							</label>
							<div class="col-md-8">
								<form:input path="ids" readonly="true" class="form-control"/>
							</div>
						</div>
					</c:otherwise>
				</c:choose>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.location"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="location" items="${locations}">
							<label class="radio-inline">
								<form:radiobutton path="location"
												  value="${location}"/>
								<spring:message code="${location.localizedDescription}"/>
							</label>
						</c:forEach>
						<form:errors path="location" cssClass="error"/>
					</div>
				</div>
				<div id="datasourceDiv" class="form-group">
					<label class="col-md-4 control-label " for="datasource.datasourceId">
						<spring:message code="page.text.datasource"/>
					</label>
					<div class="col-md-8">
						<form:select path="datasource.datasourceId" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<c:forEach var="datasource" items="${datasources}">
								<c:set var="datasourceStatus">
									<t:displayActiveStatus active="${datasource.active}" hideActive="true"/>
								</c:set>
								<form:option value="${datasource.datasourceId}"
											 data-content="${datasource.name} ${datasourceStatus}">
									${datasource.name} 
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="datasource.datasourceId" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.text.export"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>

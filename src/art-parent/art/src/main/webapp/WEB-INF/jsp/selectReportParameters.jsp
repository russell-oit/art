<%-- 
    Document   : selectReportParameters
    Created on : 20-May-2014, 15:01:32
    Author     : Timothy Anyona

Display report parameters and initiate running of report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPage title="${report.name}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/eonasdan-datepicker/css/bootstrap-datetimepicker.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.4.3/bootstrap-select-modified.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/moment.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/js/bootstrap-datetimepicker.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				$("#schedule").click(function (e) {
					e.preventDefault();
					var url = "${pageContext.request.contextPath}/app/scheduleReport.do";
					$('#parametersForm').attr('action', url).submit();
				});

				$("#runInNewPage").click(function (e) {
					$("#showInline").val("false");
					$("#parametersForm").submit();
				});

				$("#runInline").click(function (e) {
					e.preventDefault();

					$("#showInline").val("true");

					var $form = $(this).closest('form');

					//disable buttons
					$('.action').prop('disabled', true);

					var url = "${pageContext.request.contextPath}/app/runReport.do";
					$("#reportOutput").load(url, $form.serialize(),
							function (responseText, statusText, xhr) {
								//callback funtion for when jquery load has finished

								if (statusText === "success") {
									//TODO make htmlgrid output sortable

								} else if (statusText === "error") {
									bootbox.alert("<b>${errorOccurredText}</b><br>"
											+ xhr.status + "<br>" + responseText);
								}

								//enable buttons
								$('.action').prop('disabled', false);

							});

				});

				$('.datepicker').datetimepicker({
					format: 'YYYY-MM-DD'
				});

				$('.datetimepicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss'
				});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					iconBase: 'fa',
					tickIcon: 'fa-check-square'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				//immediately run query inline
//				$("#runInline").click();


			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<t:displayStackTrace error="${error}"/>
				</c:if>
			</div>
		</c:if>

		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<spring:url var="formUrl" value="/app/runReport.do"/>
				<form id="parametersForm" class="form-horizontal" method="POST" action="${formUrl}">
					<fieldset>
						<input type="hidden" name="reportId" value="${report.reportId}">
						<input type="hidden" name="showInline" id="showInline" value="true">

						<jsp:include page="reportParameters.jsp">
							<jsp:param name="paramsList" value="${paramsList}"/>
							<jsp:param name="reportParamsList" value="${reportParamsList}"/>
						</jsp:include>

						<c:if test="${isChart}">
							<div id="chartOptions">
								<div class="form-group">
									<label class="control-label col-md-6">
										<spring:message code="reports.label.show"/>
									</label>
									<div class="col-md-6">
										<label class="checkbox-inline">
											<input type="checkbox" name="showLegend" value="">
											<spring:message code="reports.label.showLegend"/>
										</label>
										<label class="checkbox-inline">
											<input type="checkbox" name="showLabels" value="">
											<spring:message code="reports.label.showLabels"/>
										</label>
										<label class="checkbox-inline">
											<input type="checkbox" name="showPoints" value="">
											<spring:message code="reports.label.showPoints"/>
										</label>
										<label class="checkbox-inline">
											<input type="checkbox" name="showData" value="">
											<spring:message code="reports.label.showData"/>
										</label>
									</div>
								</div>
							</div>
						</c:if>

						<div id="reportOptions">
							<c:if test="${enableReportFormats}">
								<div class="form-group">
									<label class="control-label col-md-6" for="reportFormat">
										<spring:message code="reports.label.format"/>
									</label>
									<div class="col-md-6">
										<select name="reportFormat" id="reportFormat" class="form-control selectpicker">
											<c:forEach var="reportFormat" items="${reportFormats}">
												<option value="${reportFormat}">
													<spring:message code="reports.format.${reportFormat}"/>
												</option>
											</c:forEach>
										</select>
									</div>
								</div>
							</c:if>

							<c:if test="${enableShowSelectedParameters}">
								<div class="form-group">
									<label class="control-label col-md-6" for="showSelectedParameters">
										<spring:message code="reports.label.showSelectedParameters"/>
									</label>
									<div class="col-md-6">
										<div class="checkbox">
											<label>
												<input type="checkbox" name="showSelectedParameters" value="">
											</label>
										</div>
									</div>
								</div>
							</c:if>

							<c:if test="${enableShowSql}">
								<div class="form-group">
									<label class="control-label col-md-6" for="showSql">
										<spring:message code="reports.label.showSql"/>
									</label>
									<div class="col-md-6">
										<div class="checkbox">
											<label>
												<input type="checkbox" name="showSql" value="">
											</label>
										</div>
									</div>
								</div>
							</c:if>
						</div>

						<div class="form-group">
							<div class="col-md-6">
								<div id="actionsDiv" class="pull-right">
									<c:if test="${enableSchedule}">
										<button type="button"id="schedule" class="btn btn-default action">
											<spring:message code="reports.action.schedule"/>
										</button>
									</c:if>
									<button type="button" id="runInNewPage" class="btn btn-default action">
										<spring:message code="reports.action.runInNewPage"/>
									</button>
									<button type="submit" id="runInline" class="btn btn-primary action">
										<spring:message code="page.action.run"/>
									</button>
								</div>
							</div>
						</div>
					</fieldset>
				</form>
			</div>
		</div>

		<div class="row">
			<div class="col-md-10 col-md-offset-1">
				<div id="reportOutput">
				</div>
			</div>
		</div>

	</jsp:body>
</t:mainPage>

<%-- 
    Document   : editReport
    Created on : 25-Feb-2014, 16:10:21
    Author     : Timothy Anyona

Edit report page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addReport" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyReport" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editReport" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${report.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="reports.text.selectFile" var="selectFileText"/>
<spring:message code="reports.text.change" var="changeText"/>
<spring:message code="reports.label.reportSource" var="reportSourceText"/>
<spring:message code="page.link.help" var="helpText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.3.8/tinymce.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			tinymce.init({
				selector: "textarea.editor",
				plugins: [
					"advlist autolink lists link image charmap print preview hr anchor pagebreak",
					"searchreplace visualblocks visualchars code",
					"nonbreaking table contextmenu directionality",
					"paste textcolor"
				],
				toolbar1: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent",
				toolbar2: "print preview | forecolor backcolor | link image | code",
				image_advtab: true
			});
		</script>

		<script type="text/javascript">
			$(document).ready(function () {
				$(function () {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="reportsConfig.do"]').parent().addClass('active');
				});

				$(function () {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

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
				
				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$("#reportTypeId").change(function () {
					toggleVisibleFields();
				});

				toggleVisibleFields(); //show/hide on page load

				$('#name').focus();

			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var reportTypeId = parseInt($('#reportTypeId option:selected').val(), 10);

				//show/hide report source textarea
				if (reportTypeId === 111) {
					//text
					$("#reportSourceHtmlDiv").show();
					$("#reportSourceDiv").hide();
				} else {
					$("#reportSourceHtmlDiv").hide();
					$("#reportSourceDiv").show();
				}

				//set report source label text
				var reportSourceType;
				switch (reportTypeId) {
					case 111:
						//text
						reportSourceType = "(HTML)";
						break;
					case 110:
						//dashboard
						reportSourceType = "(XML)";
						break;
					case 112:
					case 113:
					case 114:
						//pivot table
						reportSourceType = "(MDX)";
						break;
					default:
						reportSourceType = "(SQL)";
				}
				var reportSourceLabel = "${reportSourceText} " + reportSourceType;
				$("#reportSourceLabel").html(reportSourceLabel);

				//show/hide use rules
				switch (reportTypeId) {
					case 110: //dashboard
					case 111: //text
					case 115: //jasperreport template
					case 117: //jxls template
					case 120: //static lov
					case 121: //dynamic job recipients
					case 112: //mondrian
					case 113: //mondrian xmla
					case 114: //microsoft xmla
						$("#usesRulesDiv").hide();
						break;
					default:
						$("#usesRulesDiv").show();
				}

				//show/hide datasource
				switch (reportTypeId) {
					case 110: //dashboard
					case 111: //text
					case 120: //static lov
						$("#datasourceDiv").hide();
						break;
					default:
						$("#datasourceDiv").show();
				}

				//show/hide template
				switch (reportTypeId) {
					case 115: //jasper template
					case 116: //jasper art
					case 117: //jxls template
					case 118: //jxls art
					case 112: //mondrian
					case 122: //freemarker
					case 123: //xdocreport freemarker docx
					case 124: //xdocreport velocity docx
					case 125: //xdocreport freemarker odt
					case 126: //xdocreport velocity odt
					case 127: //xdocreport freemarker pptx
					case 128: //xdocreport velocity pptx
						$("#templateDiv").show();
						break;
					default:
						$("#templateDiv").hide();
				}

				//show/hide xmla fields
				switch (reportTypeId) {
					case 113: //mondrian xmla
						$("#xmlaFields").show();
						$("#xmlaDatasourceDiv").show();
						break;
					case 114: //sql server xmla
						$("#xmlaFields").show();
						//datasource name only configurable for mondrian xmla.
						//for sql server xmla, it's hardcoded as provider=msolap
						$("#xmlaDatasourceDiv").hide();
						break;
					default:
						$("#xmlaFields").hide();
				}

				//show/hide display resultset
				switch (reportTypeId) {
					case 110: //dashboard
					case 111: //text
					case 112: //mondrian
					case 113: //mondrian xmla
					case 114: //sql server xmla
					case 115: //jasper template
					case 117: //jxls template
					case 120: //static lov
						$("#displayResultsetDiv").hide();
						break;
					default:
						$("#displayResultsetDiv").show();
				}

				//show/hide display parameters in output
				if (reportTypeId <= 1 || reportTypeId === 101 || reportTypeId === 102
						|| reportTypeId === 103) {
					//show parameters only for chart, tabular, group, and crosstab reports
					$("#parametersInOutputDiv").show();
				} else {
					$("#parametersInOutputDiv").hide();
				}

				//show/hide chart fields
				if (reportTypeId < 0) {
					$("#chartFields").show();
					switch (reportTypeId) {
						case -2: //pie 3d
						case -13: //pie 2d
						case -10: //speedometer
							$("#chartAxisLabelFields").hide();
							break;
						default:
							$("#chartAxisLabelFields").show();
					}
				} else {
					$("#chartFields").hide();
				}

				//show/hide group column
				switch (reportTypeId) {
					case 1: //group
						$("#groupColumnDiv").show();
						break;
					default:
						$("#groupColumnDiv").hide();
				}
				
				//show/hide default report format
				switch (reportTypeId) {
					case 110: //dashboard
					case 112: //mondrian
					case 113: //mondrian xmla
					case 114: //sql server xmla
					case 100: //update
					case 111: //text
					case 103: //tabular html
					case 102: //crosstab html
					case 118: //jxls art
					case 117: //jxls template
					case 122: //freemarker
						$("#defaultReportFormatDiv").hide();
						break;
					default:
						$("#defaultReportFormatDiv").show();
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#reports">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/app/saveReport.do"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="report" enctype="multipart/form-data">
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
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>
				<c:if test="${not empty message}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${message}"/>
					</div>
				</c:if>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="reportId" readonly="true" class="form-control"/>
						</c:if>
						<c:if test="${action == 'copy'}">
							<form:hidden path="reportId"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<form:input path="name" maxlength="50" class="form-control"/>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="reportGroup">
						<spring:message code="page.text.reportGroup"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportGroup.reportGroupId" class="form-control selectpicker">
							<form:option value="0">--</form:option>
							<option data-divider="true"></option>
							<c:forEach var="group" items="${reportGroups}">
								<form:option value="${group.reportGroupId}">${group.name}</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="reportGroup" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="active">
						<spring:message code="page.label.active"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="active" id="active" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="hidden">
						<spring:message code="parameters.label.hidden"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="hidden" id="hidden" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="shortDescription">
						<spring:message code="reports.label.shortDescription"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="shortDescription" maxlength="250" class="form-control"/>
							<spring:message code="reports.help.shortDescription" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="shortDescription" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="description" rows="2" cols="40" class="form-control"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="contactPerson">
						<spring:message code="reports.label.contactPerson"/>
					</label>
					<div class="col-md-8">
						<form:input path="contactPerson" maxlength="20" class="form-control"/>
						<form:errors path="contactPerson" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="reportTypeId">
						<spring:message code="reports.label.reportType"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportTypeId" class="form-control selectpicker">
							<form:options items="${reportTypes}"
										  itemLabel="description" itemValue="value"/>
						</form:select>
						<div class="text-right">
							<a href="${pageContext.request.contextPath}/docs/Manual.html#report-types">
								<spring:message code="page.link.help"/>
							</a>
						</div>
						<form:errors path="reportTypeId" cssClass="error"/>
					</div>
				</div>
				<div id="groupColumnDiv" class="form-group">
					<label class="col-md-4 control-label " for="groupColumn">
						<spring:message code="reports.label.groupColumn"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="groupColumn" maxlength="2" class="form-control"/>
							<spring:message code="reports.help.groupColumn" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="groupColumn" cssClass="error"/>
					</div>
				</div>
				<div id="datasourceDiv" class="form-group">
					<label class="col-md-4 control-label " for="datasource">
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
				<div id="usesRulesDiv" class="form-group">
					<label class="control-label col-md-4" for="usesRules">
						<spring:message code="reports.label.usesRules"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="usesRules" id="usesRules" class="switch-yes-no"/>
						</div>
						<form:errors path="usesRules" cssClass="error"/>
					</div>
				</div>
				<div id="parametersInOutputDiv" class="form-group">
					<label class="control-label col-md-4" for="parametersInOutput">
						<spring:message code="reports.label.parametersInOutput"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="parametersInOutput" id="parametersInOutput" class="switch-yes-no"/>
						</div>
						<form:errors path="parametersInOutput" cssClass="error"/>
					</div>
				</div>
				<div id="displayResultsetDiv" class="form-group">
					<label class="col-md-4 control-label " for="displayResultset">
						<spring:message code="reports.label.displayResultset"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="displayResultset" maxlength="2" class="form-control"/>
							<spring:message code="reports.help.displayResultset" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="displayResultset" cssClass="error"/>
					</div>
				</div>
				<div id="defaultReportFormatDiv" class="form-group">
					<label class="col-md-4 control-label " for="defaultReportFormat">
						<spring:message code="reports.label.defaultReportFormat"/>
					</label>
					<div class="col-md-8">
						<form:select path="defaultReportFormat" class="form-control selectpicker">
							<form:option value="default"><spring:message code="drilldowns.option.default"/></form:option>
								<option data-divider="true"></option>
							<c:forEach var="reportFormat" items="${reportFormats}">
								<form:option value="${reportFormat.value}">
									<spring:message code="${reportFormat.localizedDescription}"/>
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="defaultReportFormat" cssClass="error"/>
					</div>
				</div>

				<fieldset id="chartFields">
					<fieldset id="chartAxisLabelFields">
						<div class="form-group">
							<label class="control-label col-md-4" for="xAxisLabel">
								<spring:message code="reports.label.xAxisLabel"/>
							</label>
							<div class="col-md-8">
								<form:input path="xAxisLabel" maxlength="50" class="form-control"/>
								<form:errors path="xAxisLabel" cssClass="error"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="yAxisLabel">
								<spring:message code="reports.label.yAxisLabel"/>
							</label>
							<div class="col-md-8">
								<form:input path="yAxisLabel" maxlength="50" class="form-control"/>
								<form:errors path="yAxisLabel" cssClass="error"/>
							</div>
						</div>
					</fieldset>

					<div class="form-group">
						<label class="control-label col-md-4">
							<spring:message code="reports.label.show"/>
						</label>
						<div class="col-md-8">
							<label class="checkbox-inline">
								<form:checkbox path="chartOptions.showLegend" id="showLegend"/>
								<spring:message code="reports.label.showLegend"/>
							</label>
							<label class="checkbox-inline">
								<form:checkbox path="chartOptions.showLabels" id="showLabels"/>
								<spring:message code="reports.label.showLabels"/>
							</label>
							<label class="checkbox-inline">
								<form:checkbox path="chartOptions.showPoints" id="showPoints"/>
								<spring:message code="reports.label.showPoints"/>
							</label>
							<label class="checkbox-inline">
								<form:checkbox path="chartOptions.showData" id="showData"/>
								<spring:message code="reports.label.showData"/>
							</label>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="chartOptions.width">
							<spring:message code="reports.label.width"/>
						</label>
						<div class="col-md-8">
							<form:input path="chartOptions.width" maxlength="4" class="form-control"/>
							<form:errors path="chartOptions.width" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="chartOptions.height">
							<spring:message code="reports.label.height"/>
						</label>
						<div class="col-md-8">
							<form:input path="chartOptions.height" maxlength="4" class="form-control"/>
							<form:errors path="chartOptions.height" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="chartOptions.backgroundColor">
							<spring:message code="reports.label.backgroundColor"/>
						</label>
						<div class="col-md-8">
							<form:input path="chartOptions.backgroundColor" maxlength="7" class="form-control"/>
							<form:errors path="chartOptions.backgroundColor" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="chartOptions.yAxisMin">
							<spring:message code="reports.label.yAxisMin"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="chartOptions.yAxisMin" maxlength="15" class="form-control"/>
								<spring:message code="reports.help.yAxisMinMax" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="chartOptions.yAxisMin" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="chartOptions.yAxisMax">
							<spring:message code="reports.label.yAxisMax"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="chartOptions.yAxisMax" maxlength="15" class="form-control"/>
								<spring:message code="reports.help.yAxisMinMax" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="chartOptions.yAxisMax" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="chartOptions.rotateAt">
							<spring:message code="reports.label.rotateAt"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="chartOptions.rotateAt" maxlength="4" class="form-control"/>
								<spring:message code="reports.help.rotateAt" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="chartOptions.rotateAt" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="chartOptions.removeAt">
							<spring:message code="reports.label.removeAt"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="chartOptions.removeAt" maxlength="4" class="form-control"/>
								<spring:message code="reports.help.removeAt" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="chartOptions.removeAt" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<div id="templateDiv" class="form-group">
					<label class="control-label col-md-4" for="template">
						<spring:message code="reports.label.template"/>
					</label>
					<div class="col-md-8">
						<div>
							<form:input path="template" maxlength="100" class="form-control"/>
							<form:errors path="template" cssClass="error"/>
						</div>
						<div class="fileinput fileinput-new" data-provides="fileinput">
							<span class="btn btn-default btn-file">
								<span class="fileinput-new">${selectFileText}</span>
								<span class="fileinput-exists">${changeText}</span>
								<input type="file" name="templateFile">
							</span>
							<span class="fileinput-filename"></span>
							<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
						</div>
						<div>
							<b><spring:message code="reports.text.resources"/></b>
						</div>
						<div class="fileinput fileinput-new" data-provides="fileinput">
							<span class="btn btn-default btn-file">
								<span class="fileinput-new">${selectFileText}</span>
								<span class="fileinput-exists">${changeText}</span>
								<input type="file" name="resourcesFile">
							</span>
							<span class="fileinput-filename"></span>
							<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
						</div>
					</div>
				</div>

				<fieldset id="xmlaFields">
					<div id="xmlaDatasourceDiv" class="form-group">
						<label class="control-label col-md-4" for="xmlaDatasource">
							<spring:message code="reports.label.xmlaDatasource"/>
						</label>
						<div class="col-md-8">
							<form:input path="xmlaDatasource" maxlength="50" class="form-control"/>
							<form:errors path="xmlaDatasource" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="xmlaCatalog">
							<spring:message code="reports.label.xmlaCatalog"/>
						</label>
						<div class="col-md-8">
							<form:input path="xmlaCatalog" maxlength="50" class="form-control"/>
							<form:errors path="xmlaCatalog" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<label id="reportSourceLabel" class="col-md-12 control-label" style="text-align: center">
				</label>

				<div id="reportSourceDiv" class="form-group">
					<div class="col-md-12">
						<form:textarea path="reportSource" rows="20" cols="70" wrap="off" class="form-control"/>
						<form:errors path="reportSource" cssClass="error"/>
					</div>
				</div>
				<div id="reportSourceHtmlDiv" class="form-group">
					<div class="col-md-12">
						<form:textarea path="reportSourceHtml" rows="20" cols="70" wrap="off" class="form-control editor"/>
						<form:errors path="reportSourceHtml" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>

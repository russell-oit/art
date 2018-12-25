<%-- 
    Document   : selfServiceReports
    Created on : 24-Dec-2018, 18:13:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceReports" var="pageTitle"/>

<spring:message code="page.text.search" var="searchText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/css/query-builder.default.min.css" /> 
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/js/query-builder.standalone.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multiselect-2.5.5/js/multiselect.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

		<script>
			$(function () {
				$('a[id="selfService"]').parent().addClass('active');
				$('a[href*="selfServiceReports"]').parent().addClass('active');

				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				loadViews();

				function loadViews() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getViews',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value=" + report.reportId + ">" + report.name2 + "</option>";
								});
								var select = $("#views");
								select.empty();
								select.append(options);
								select.selectpicker('refresh');
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					});
				}

				$("#views").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var reportId = $(this).find('option').eq(clickedIndex).val();

					//https://stackoverflow.com/questions/27347004/jquery-val-integer-datatype-comparison
					if (reportId === '0') {
						$('#multiselect').empty();
						$('#multiselect_to').empty();
						resetOptions();
					} else {
						resetOptions();

						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getViewDetails',
							data: {reportId: reportId},
							success: function (response) {
								if (response.success) {
									var columns = response.data;
									var options = "";
									$.each(columns, function (index, column) {
										options += "<option value='" + column.name + "' data-type='" + column.type + "'>" + column.label + "</option>";
									});
									var select = $("#multiselect");
									select.empty();
									select.append(options);
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: function (xhr) {
								showUserAjaxError(xhr, '${errorOccurredText}');
							}
						});
					}
				});

				function resetOptions() {

				}

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				$('#multiselect').multiselect({
					search: {
						left: '<input type="text" name="availableColumns" class="form-control" placeholder="${searchText}" />',
						right: '<input type="text" name="selectedColumns" class="form-control" placeholder="${searchText}" />'
					},
					fireSearch: function (value) {
						return value.length > 0;
					},
					sort: false
				});

				$('.parse-sql').on('click', function () {
					var result = $('#builder').queryBuilder('getSQL', $(this).data('stmt'));

					if (result !== null && result.sql.length) {
						//console.log(result);
						bootbox.alert({
							title: $(this).text(),
							message: '<pre class="code-popup">' + result.sql + (result.params ? '\n\n' + result.params : '') + '</pre>'
						});
					}
				});

				$('#selected').on('click', function () {
					var values = '';
					$('#multiselect_to option').each(function (index, element) {
						values += element.value + ' - ' + element.text + '\n';
					});

					bootbox.alert({
						message: '<pre class="code-popup">' + values + '</pre>'
					});
				});

				//createBuilder();

			});

			function createBuilder() {
				var filters = createFilters();
				$('#builder').queryBuilder({
					filters: filters
				});
			}

			function createFilters() {
				var filters = [];
				var ids = [];

				$('#multiselect option').each(function (index, element) {
					//console.log(index);
					//console.log(element.value);
					//console.log(element.text);
					var value = element.value;
					var text = element.text;
					var fieldType = $(element).data("type");
					if (fieldType === undefined) {
						fieldType = 'string';
					}

					if ($.inArray(value, ids) !== -1) {
						value += index;
					}

					ids.push(value);

					var filter = {
						id: value,
						label: text,
						type: fieldType
					};

					filters.push(filter);
				});

				return filters;
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12">
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>
			</div>
		</div>

		<div class="row" style="margin-bottom: 20px">
			<div class="col-md-4">
				<select id="views" class="form-control selectpicker">
					<option value="0">--</option>
				</select>
			</div>
		</div>

		<div class="row">
			<div class="col-md-5">
				<select name="from" id="multiselect" class="form-control" size="11" multiple="multiple">
				</select>
			</div>

			<div class="col-md-2">
				<button type="button" id="multiselect_undo" class="btn btn-primary btn-block">undo</button>
				<button type="button" id="multiselect_rightAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-forward"></i></button>
				<button type="button" id="multiselect_rightSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-right"></i></button>
				<button type="button" id="multiselect_leftSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-left"></i></button>
				<button type="button" id="multiselect_leftAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-backward"></i></button>
				<button type="button" id="multiselect_redo" class="btn btn-warning btn-block">redo</button>
			</div>

			<div class="col-md-5">
				<select name="to" id="multiselect_to" class="form-control" size="11" multiple="multiple"></select>

				<div class="row">
					<div class="col-md-6">
						<button type="button" id="multiselect_move_up" class="btn btn-block btn-default"><i class="glyphicon glyphicon-arrow-up"></i></button>
					</div>
					<div class="col-md-6">
						<button type="button" id="multiselect_move_down" class="btn btn-block btn-default col-sm-6"><i class="glyphicon glyphicon-arrow-down"></i></button>
					</div>
				</div>
			</div>
		</div>

		<div class="row" style="margin-top: 20px">
			<div class="col-md-12">
				<div class="row">
					<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="false">SQL</button>
					<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="question_mark">SQL statement
						(?)
					</button>
					<button class="btn btn-primary parse-sql" data-target="import_export" data-stmt="named">SQL statement (named)
					</button>
					<button class="btn btn-default" id="selected">Selected</button>
				</div>
				<div class="row">
					<div id="builder"></div>
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

<%-- 
    Document   : runningJobs
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.runningJobs" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" hasTable="true" hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-jdateformatparser-1.2.1/moment-jdateformatparser.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="jobsConfig"]').parent().addClass('active');

				var tbl = $('#jobs');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var dataUrl = "${pageContext.request.contextPath}/getRunningJobs";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var columnDefs = undefined; //pass undefined to use the default

				moment.locale(localeCode);

				var dateFormatJava = "${dateDisplayPattern}";
				var dateFormatMoment = moment().toMomentFormatString(dateFormatJava);

				//https://stackoverflow.com/questions/31880793/jquery-datatables-handle-null-sub-objects
				//https://stackoverflow.com/questions/30489307/make-column-data-as-hyperlink-datatable-jquery/30489991
				var columns = [
					{"data": "job.jobId", "defaultContent": "",
						render: function (data) {
							return data;
						}
					},
					{"data": "job.name", "defaultContent": "",
						render: function (data) {
							return escapeHtmlContent(data);
						}
					},
					{"data": "startTime",
						//https://stackoverflow.com/questions/28733613/format-json-date-to-mm-dd-yy-format-before-displaying-in-a-jquery-datatable
						render: function (data, type) {
							//https://datatables.net/manual/data/orthogonal-data#Computed-values
							// If display or filter data is requested, format the date
							if (type === 'display' || type === 'filter') {
								if (data === null) {
									return "";
								} else {
									var date = moment(data);
									var formattedDate = date.format(dateFormatMoment);
									//https://www.jianshu.com/p/c67f944a5726
									//https://momentjscom.readthedocs.io/en/latest/moment/04-displaying/02-fromnow/
									var fromNow = date.fromNow();
									return formattedDate + "<br>" + fromNow;
								}
							} else {
								// Otherwise the data type requested (`type`) is type detection or
								// sorting data, for which we want to use the integer, so just return
								// that, unaltered
								return data;
							}
						}
					}
				];

				//initialize datatable
				var oTable = initAjaxBasicTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, dataUrl, errorOccurredText,
						showErrors, columnDefs, columns);

				var table = oTable.api();

				yadcf.init(table,
						[
							{
								column_number: 0,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-name-filter"
							},
							{
								column_number: 1,
								filter_type: 'text',
								filter_default_label: ""
							},
							{
								column_number: 2,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-name-filter"
							}
						]
						);

				$("#refreshRecords").on("click", function () {
					table.ajax.reload();
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});
		</script>
	</jsp:attribute>

	<jsp:body>
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

		<div style="margin-bottom: 10px; text-align: right">
			<button type="button" id="refreshRecords" class="btn btn-default">
				<i class="fa fa-refresh"></i>
				<spring:message code="page.action.refresh"/>
			</button>
		</div>

		<table id="jobs" class="table table-striped table-bordered table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/><p></p></th>
					<th><spring:message code="page.text.name"/><p></p></th>
					<th><spring:message code="page.text.startTime"/><p></p></th>
				</tr>
			</thead>
		</table>
	</jsp:body>
</t:mainPageWithPanel>

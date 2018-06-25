<%-- 
    Document   : reportsConfig
    Created on : 25-Feb-2014, 10:46:51
    Author     : Timothy Anyona

Reports configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.reportsConfiguration" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="reports.message.linkedJobsExist" var="linkedJobsExistText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>
<spring:message code="reports.text.selectValue" var="selectValueText"/>
<spring:message code="page.message.recordUpdated" var="recordUpdatedText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="reports.label.reportSource" var="reportSourceText"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/js/dataTables.select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/JSZip-2.5.0/jszip.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/pdfmake.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/vfs_fonts.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.html5.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.print.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.colVis.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace-min-noconflict-1.2.6/ace.js" charset="utf-8"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.3.8/tinymce.min.js"></script>

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
				image_advtab: true,
				//https://codepen.io/nirajmchauhan/pen/EjQLpV
				paste_data_images: true,
				file_picker_callback: function (callback, value, meta) {
					if (meta.filetype == 'image') {
						$('#upload').trigger('click');
						$('#upload').on('change', function () {
							var file = this.files[0];
							var reader = new FileReader();
							reader.onload = function (e) {
								callback(e.target.result, {
									alt: ''
								});
							};
							reader.readAsDataURL(file);
						});
					}
				}
			});
		</script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}',
					selectAllText: '${selectAllText}',
					deselectAllText: '${deselectAllText}'
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

				var tbl = $('#reports');

				var oTable = tbl.dataTable({
					orderClasses: false,
					order: [[1, 'asc']],
					deferRender: true,
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, '${showAllRowsText}']],
					pageLength: 10,
					ajax: {
						type: "GET",
						dataType: "json",
						url: "${pageContext.request.contextPath}/getConfigReports",
						dataSrc: function (response) {
							//https://stackoverflow.com/questions/35475964/datatables-ajax-call-error-handle
							if (response.success) {
								return response.data;
							} else {
								notifyActionError('${errorOccurredText}', escapeHtmlContent(response.errorMessage));
								return "";
							}
						},
						error: ajaxErrorHandler
					},
					columns: [
						{"data": null, defaultContent: ""},
						{"data": "reportId"},
						{"data": "name"},
						{"data": "reportGroupNames"},
						{"data": "description"},
						{"data": "dtActiveStatus"},
						{data: "dtAction"}
					],
					//https://datatables.net/reference/option/rowId
					rowId: "dtRowId",
					//autoWidth: false,
					columnDefs: [{
							targets: 0,
							orderable: false,
							className: 'select-checkbox'
						},
						{
							targets: "dtHidden", //target name matches class name of th.
							visible: false
						}
					],
					dom: 'lBfrtip',
					buttons: [
						'selectAll',
						'selectNone',
						{
							extend: 'colvis',
							postfixButtons: ['colvisRestore']
						},
						{
							extend: 'excel',
							exportOptions: {
								columns: ':visible'
							}
						},
						{
							extend: 'pdf',
							exportOptions: {
								columns: ':visible'
							}
						},
						{
							extend: 'print',
							exportOptions: {
								columns: ':visible'
							}
						}
					],
					select: {
						style: 'multi',
						selector: 'td:first-child'
					},
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
//					createdRow: function (row, data, dataIndex) {
//						$(row).attr('data-id', data.reportId);
//						$(row).attr('data-name', data.name);
//					},
					initComplete: function () {
						$('div.dataTables_filter input').focus();

					}
				});

				var table = oTable.api();

				yadcf.init(table,
						[
							{
								column_number: 1,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-id-filter"
							},
							{
								column_number: 2,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-default-filter"
							},
							{
								column_number: 3,
								filter_default_label: '${selectValueText}',
								text_data_delimiter: ","
							},
							{
								column_number: 4,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-default-filter"
							},
							{
								column_number: 5,
								filter_default_label: '${selectValueText}',
								column_data_type: "html",
								html_data_type: "text"
							}
						]
						);

				tbl.find('tbody').on('click', '.editRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordId = table.row(row).data().reportId;
					$.ajax({
						type: "GET",
						dataType: "json",
						url: "${pageContext.request.contextPath}/getBasicReport",
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								var report = response.data;
								setReportFields(report);
								$("#editReport").show();
							} else {
								notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				//https://stackoverflow.com/questions/6440439/how-do-i-make-a-textarea-an-ace-editor
				//https://stackoverflow.com/questions/8963855/how-do-i-get-value-from-ace-editor
				//https://ace.c9.io/#nav=howto
				//https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts
				//https://templth.wordpress.com/2014/12/29/using-ace-editor-into-angular-applications/
				var sqlEditor = ace.edit("sqlEditor");
				//https://stackoverflow.com/questions/28936479/where-to-set-ace-editor-blockscrolling
				//https://github.com/angular-ui/ui-ace/issues/104
				sqlEditor.$blockScrolling = Infinity;
				sqlEditor.getSession().setMode("ace/mode/sql");
				sqlEditor.setHighlightActiveLine(false);
				//https://stackoverflow.com/questions/14907184/is-there-a-way-to-hide-the-vertical-ruler-in-ace-editor
				sqlEditor.setShowPrintMargin(false);
				//https://stackoverflow.com/questions/28283344/is-there-a-way-to-hide-the-line-numbers-in-ace-editor
				//sqlEditor.renderer.setShowGutter(false);
				sqlEditor.setOption("showLineNumbers", true);
				//https://stackoverflow.com/questions/11584061/automatically-adjust-height-to-contents-in-ace-cloud-9-editor
				//https://ace.c9.io/demo/autoresize.html
				//https://issues.jenkins-ci.org/browse/JENKINS-31585
				sqlEditor.setOption("maxLines", 30);
				sqlEditor.setOption("minLines", 10);

				document.getElementById('sqlEditor').style.fontSize = '14px'; //default seems to be 12px

				var reportSource = $('#reportSource');
				sqlEditor.getSession().on('change', function () {
					reportSource.val(sqlEditor.getSession().getValue());
				});

				var xmlEditor = ace.edit("xmlEditor");
				xmlEditor.$blockScrolling = Infinity;
				xmlEditor.getSession().setMode("ace/mode/xml");
				xmlEditor.setHighlightActiveLine(false);
				xmlEditor.setShowPrintMargin(false);

				//https://github.com/ajaxorg/ace/commit/abb1e4703b737757e20d1e7040943ba4e2483007
				//https://github.com/ajaxorg/ace/wiki/Configuring-Ace
				xmlEditor.setOption("showLineNumbers", false);
				xmlEditor.setOption("maxLines", 30);
				xmlEditor.setOption("minLines", 10);

				document.getElementById('xmlEditor').style.fontSize = '14px';

				xmlEditor.getSession().on('change', function () {
					reportSource.val(xmlEditor.getSession().getValue());
				});

				var optionsEditor = ace.edit("optionsEditor");
				optionsEditor.$blockScrolling = Infinity;
				optionsEditor.getSession().setMode("ace/mode/json");
				optionsEditor.setHighlightActiveLine(false);
				optionsEditor.setShowPrintMargin(false);
				optionsEditor.setOption("showLineNumbers", false);
				optionsEditor.setOption("maxLines", 20);
				optionsEditor.setOption("minLines", 7);
				document.getElementById('optionsEditor').style.fontSize = '14px';

				var options = $('#options');
				optionsEditor.getSession().on('change', function () {
					options.val(optionsEditor.getSession().getValue());
				});

				var pivotTableJsSavedOptionsEditor = ace.edit("pivotTableJsSavedOptionsEditor");
				pivotTableJsSavedOptionsEditor.$blockScrolling = Infinity;
				pivotTableJsSavedOptionsEditor.getSession().setMode("ace/mode/json");
				pivotTableJsSavedOptionsEditor.setHighlightActiveLine(false);
				pivotTableJsSavedOptionsEditor.setShowPrintMargin(false);
				pivotTableJsSavedOptionsEditor.setOption("showLineNumbers", false);
				pivotTableJsSavedOptionsEditor.setOption("maxLines", 20);
				pivotTableJsSavedOptionsEditor.setOption("minLines", 3);
				document.getElementById('pivotTableJsSavedOptionsEditor').style.fontSize = '14px';

				var pivotTableJsSavedOptions = $('#pivotTableJsSavedOptions');
				pivotTableJsSavedOptionsEditor.getSession().on('change', function () {
					pivotTableJsSavedOptions.val(pivotTableJsSavedOptionsEditor.getSession().getValue());
				});

				var gridstackSavedOptionsEditor = ace.edit("gridstackSavedOptionsEditor");
				gridstackSavedOptionsEditor.$blockScrolling = Infinity;
				gridstackSavedOptionsEditor.getSession().setMode("ace/mode/json");
				gridstackSavedOptionsEditor.setHighlightActiveLine(false);
				gridstackSavedOptionsEditor.setShowPrintMargin(false);
				gridstackSavedOptionsEditor.setOption("showLineNumbers", false);
				gridstackSavedOptionsEditor.setOption("maxLines", 20);
				gridstackSavedOptionsEditor.setOption("minLines", 3);
				document.getElementById('gridstackSavedOptionsEditor').style.fontSize = '14px';

				var gridstackSavedOptions = $('#gridstackSavedOptions');
				gridstackSavedOptionsEditor.getSession().on('change', function () {
					gridstackSavedOptions.val(gridstackSavedOptionsEditor.getSession().getValue());
				});

				var jsonEditor = ace.edit("jsonEditor");
				jsonEditor.$blockScrolling = Infinity;
				jsonEditor.getSession().setMode("ace/mode/json");
				jsonEditor.setHighlightActiveLine(false);
				jsonEditor.setShowPrintMargin(false);
				jsonEditor.setOption("showLineNumbers", false);
				jsonEditor.setOption("maxLines", 30);
				jsonEditor.setOption("minLines", 10);
				document.getElementById('jsonEditor').style.fontSize = '14px';

				jsonEditor.getSession().on('change', function () {
					reportSource.val(jsonEditor.getSession().getValue());
				});

				var groovyEditor = ace.edit("groovyEditor");
				groovyEditor.$blockScrolling = Infinity;
				groovyEditor.getSession().setMode("ace/mode/groovy");
				groovyEditor.setHighlightActiveLine(false);
				groovyEditor.setShowPrintMargin(false);
				groovyEditor.setOption("showLineNumbers", true);
				groovyEditor.setOption("maxLines", 30);
				groovyEditor.setOption("minLines", 10);
				document.getElementById('groovyEditor').style.fontSize = '14px';

				groovyEditor.getSession().on('change', function () {
					reportSource.val(groovyEditor.getSession().getValue());
				});

				function setReportFields(report) {
					$("#editLink").attr("href", "${pageContext.request.contextPath}/editReport?id=" + report.reportId);
					$("#copyLink").attr("href", "${pageContext.request.contextPath}/copyReport?id=" + report.reportId);
					$("#reportParameterConfigLink").attr("href", "${pageContext.request.contextPath}/reportParameterConfig?reportId=" + report.reportId);
					$("#reportRulesLink").attr("href", "${pageContext.request.contextPath}/reportRules?reportId=" + report.reportId);
					$("#drilldownsLink").attr("href", "${pageContext.request.contextPath}/drilldowns?reportId=" + report.reportId);
					$("#reportAccessRightsLink").attr("href", "${pageContext.request.contextPath}/reportAccessRights?reportId=" + report.reportId);
					$("#addJobLink").attr("href", "${pageContext.request.contextPath}/addJob?nextPage=jobsConfig&reportId=" + report.reportId);
					$("#selectReportParametersLink").attr("href", "${pageContext.request.contextPath}/selectReportParameters?reportId=" + report.reportId);

					$("#reportId").val(report.reportId);
					$("#name").val(report.name);
					//https://github.com/silviomoreto/bootstrap-select/issues/74
					//https://stackoverflow.com/questions/14804253/how-to-set-selected-value-on-select-using-selectpicker-plugin-from-bootstrap
					//https://silviomoreto.github.io/bootstrap-select/methods/
					$("#reportTypeId").selectpicker('val', report.reportTypeId);

					if (report.datasource) {
						//https://learn.jquery.com/using-jquery-core/faq/how-do-i-select-an-element-by-an-id-that-has-characters-used-in-css-notation/
						//https://api.jquery.com/jQuery.escapeSelector/
						//https://stackoverflow.com/questions/605630/how-to-select-html-nodes-by-id-with-jquery-when-the-id-contains-a-dot
						//https://stackoverflow.com/questions/9930577/jquery-dot-in-id-selector/9930611
						$("#datasource\\.datasourceId").selectpicker('val', report.datasource.datasourceId);
					}
					$("#useGroovy").bootstrapSwitch('state', report.useGroovy);
					$("#pivotTableJsSavedOptions").val(report.pivotTableJsSavedOptions);
					$("#gridstackSavedOptions").val(report.gridstackSavedOptions);
					$("#options").val(report.options);
					$("#reportSource").val(report.reportSource);
					$("#reportSourceHtml").val(report.reportSourceHtml);

					if (report.reportSourceHtml) {
						tinyMCE.get('reportSourceHtml').setContent(report.reportSourceHtml);
					}

					sqlEditor.getSession().setValue(reportSource.val());
					xmlEditor.getSession().setValue(reportSource.val());
					optionsEditor.getSession().setValue(options.val());
					pivotTableJsSavedOptionsEditor.getSession().setValue(pivotTableJsSavedOptions.val());
					gridstackSavedOptionsEditor.getSession().setValue(gridstackSavedOptions.val());
					jsonEditor.getSession().setValue(reportSource.val());
					groovyEditor.getSession().setValue(reportSource.val());

					toggleGroovyEditor(reportSource, groovyEditor, sqlEditor);

					toggleVisibleFields();
				}

				$("#testReport").click(function () {
					//https://stackoverflow.com/questions/2122085/jquery-and-tinymce-textarea-value-doesnt-submit
					tinymce.triggerSave();

					//disable buttons
					$('.action').prop('disabled', true);

					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/runReport",
						data: $('#basicReportForm').serialize(),
						success: function (data, status, xhr) {
							$("#reportOutput").html(data);
							$('.action').prop('disabled', false);
						},
						error: function (xhr, status, error) {
							//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
							bootbox.alert(xhr.responseText);
							$('.action').prop('disabled', false);
						}
					});
				});

				$("#testReportData").click(function () {
					//disable buttons
					$('.action').prop('disabled', true);

					//https://stackoverflow.com/questions/10398783/jquery-form-serialize-and-other-parameters
					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/runReport",
						data: $('#basicReportForm').serialize() + "&testData=true&reportFormat=htmlDataTable",
						success: function (data, status, xhr) {
							$("#reportOutput").html(data);
							$('.action').prop('disabled', false);
						},
						error: function (xhr, status, error) {
							//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
							bootbox.alert(xhr.responseText);
							$('.action').prop('disabled', false);
						}
					});
				});

				$('#useGroovy').on('switchChange.bootstrapSwitch', function (event, state) {
					toggleGroovyEditor(reportSource, groovyEditor, sqlEditor);
				});

				$("#applyOptions").on("click", function () {
					var reportTypeId = parseInt($('#reportTypeId option:selected').val(), 10);
					switch (reportTypeId) {
						case 129: //gridstack dashboard
							var items = [];

							$('.grid-stack-item.ui-draggable').each(function () {
								var $this = $(this);
								items.push({
									index: parseInt($this.attr('data-index'), 10),
									x: parseInt($this.attr('data-gs-x'), 10),
									y: parseInt($this.attr('data-gs-y'), 10),
									width: parseInt($this.attr('data-gs-width'), 10),
									height: parseInt($this.attr('data-gs-height'), 10)
								});
							});

							gridstackSavedOptionsEditor.getSession().setValue(JSON.stringify(items));
							break;
						case 132: //pivottable.js
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
							var config = $(".pivotTableJsOutputDiv").data("pivotUIOptions");
							var config_copy = JSON.parse(JSON.stringify(config));
							//delete some values which will not serialize to JSON
							delete config_copy["aggregators"];
							delete config_copy["renderers"];
							//delete some bulky default values
							delete config_copy["rendererOptions"];
							delete config_copy["localeStrings"];

							pivotTableJsSavedOptionsEditor.getSession().setValue(JSON.stringify(config_copy));
							break;
						default:
							break;
					}
				});

				function toggleGroovyEditor(reportSource, groovyEditor, sqlEditor) {
					var reportTypeId = parseInt($('#reportTypeId option:selected').val(), 10);

					switch (reportTypeId) {
						case 110: //dashboard
						case 111: //text
						case 129: //gridstack dashboard
						case 156: //org chart list
						case 149: //saiku report
						case 155: //org chart json
						case 151: //mongodb
						case 115: //jasper template
						case 117: //jxls template
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
						case 136: //dygraphs csv local
						case 137: //dygraphs csv server
						case 139: //datatables csv local
						case 140: //datatables csv server
						case 145: //datamaps file
						case 150: //saiku connection
						case 159: //reportengine file
							break;
						default:
							if ($('#useGroovy').is(':checked')) {
								$("#reportSourceLabel").html("${reportSourceText} (Groovy)");
								groovyEditor.getSession().setValue(reportSource.val());
								$("#sqlEditor").hide();
								$("#groovyEditor").show();
							} else {
								$("#reportSourceLabel").html("${reportSourceText} (SQL)");
								sqlEditor.getSession().setValue(reportSource.val());
								$("#sqlEditor").show();
								$("#groovyEditor").hide();
							}
					}
				}

				function toggleVisibleFields() {
					var reportTypeId = parseInt($('#reportTypeId option:selected').val(), 10);

					//show/hide report source
					if (reportTypeId === 111) {
						//text
						$("#reportSourceHtmlDiv").show();
						$("#reportSourceDiv").hide();
					} else {
						$("#reportSourceHtmlDiv").hide();

						switch (reportTypeId) {
							case 115: //jasper template
							case 117: //jxls template
							case 133: //pivottable.js csv local
							case 134: //pivottable.js csv server
							case 136: //dygraphs csv local
							case 137: //dygraphs csv server
							case 139: //datatables csv local
							case 140: //datatables csv server
							case 145: //datamaps file
							case 150: //saiku connection
							case 159: //reportengine file
								$("#reportSourceDiv").hide();
								break;
							default:
								$("#reportSourceDiv").show();
						}

						switch (reportTypeId) {
							case 110: //dashboard
							case 129: //gridstack dashboard
							case 156: //org chart list
								$("#sqlEditor").hide();
								$("#xmlEditor").show();
								$("#jsonEditor").hide();
								$("#groovyEditor").hide();
								break;
							case 149: //saiku report
							case 155: //org chart json
								$("#sqlEditor").hide();
								$("#xmlEditor").hide();
								$("#jsonEditor").show();
								$("#groovyEditor").hide();
								break;
							case 151: //mongodb
								$("#sqlEditor").hide();
								$("#xmlEditor").hide();
								$("#jsonEditor").hide();
								$("#groovyEditor").show();
								break;
							default:
								$("#sqlEditor").show();
								$("#xmlEditor").hide();
								$("#jsonEditor").hide();
								$("#groovyEditor").hide();
						}
					}

					//show/hide report source label
					switch (reportTypeId) {
						case 115: //jasper template
						case 117: //jxls template
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
						case 136: //dygraphs csv local
						case 137: //dygraphs csv server
						case 139: //datatables csv local
						case 140: //datatables csv server
						case 145: //datamaps file
						case 150: //saiku connection
						case 159: //reportengine file
							$("#reportSourceLabel").hide();
							break;
						default:
							$("#reportSourceLabel").show();
					}

					//set report source label text
					var reportSourceType;
					switch (reportTypeId) {
						case 111:
							//text
							reportSourceType = "(HTML)";
							break;
						case 110: //dashboard
						case 129: //gridstack dashboard
						case 156: //org chart list
							reportSourceType = "(XML)";
							break;
						case 112:
						case 113:
						case 114:
							//jpivot
							reportSourceType = "(MDX)";
							break;
						case 149: //saiku report
						case 155: //org chart json
							reportSourceType = "(JSON)";
							break;
						case 151: //mongodb
							reportSourceType = "(Groovy)";
							break;
						default:
							reportSourceType = "(SQL)";
					}
					var reportSourceLabel = "${reportSourceText} " + reportSourceType;
					$("#reportSourceLabel").html(reportSourceLabel);

					//show/hide use groovy
					switch (reportTypeId) {
						case 110: //dashboard
						case 129: //gridstack dashboard
						case 156: //org chart list
						case 149: //saiku report
						case 155: //org chart json
						case 151: //mongodb
						case 115: //jasper template
						case 117: //jxls template
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
						case 136: //dygraphs csv local
						case 137: //dygraphs csv server
						case 139: //datatables csv local
						case 140: //datatables csv server
						case 145: //datamaps file
						case 150: //saiku connection
						case 159: //reportengine file
							$("#useGroovyDiv").hide();
							break;
						default:
							$("#useGroovyDiv").show();
					}

					//show/hide datasource
					switch (reportTypeId) {
						case 110: //dashboard
						case 129: //gridstack dashboard
						case 111: //text
						case 120: //static lov
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
						case 136: //dygraphs csv local
						case 137: //dygraphs csv server
						case 139: //datatables csv local
						case 140: //datatables csv server
						case 145: //datamaps file
						case 149: //saiku report
						case 155: //org chart json
						case 156: //org chart list
						case 157: //org chart ajax
						case 159: //reportengine file
							$("#datasourceDiv").hide();
							break;
						default:
							$("#datasourceDiv").show();
					}

					//show/hide pivottable.js saved options
					switch (reportTypeId) {
						case 132: //pivottable.js
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
							$("#pivotTableJsSavedOptionsDiv").show();
							break;
						default:
							$("#pivotTableJsSavedOptionsDiv").hide();
					}

					//show/hide gridstack saved options field
					switch (reportTypeId) {
						case 129: //gridstack dashboard
							$("#gridstackSavedOptionsDiv").show();
							break;
						default:
							$("#gridstackSavedOptionsDiv").hide();
					}

					//show/hide apply button
					switch (reportTypeId) {
						case 129: //gridstack dashboard
						case 132: //pivottable.js
						case 133: //pivottable.js csv local
						case 134: //pivottable.js csv server
							$("#applyOptions").show();
							break;
						default:
							$("#applyOptions").hide();
					}

					//show/hide test report button
					switch (reportTypeId) {
						case 112: //jpivot mondrian
						case 113: //jpivot mondrian xmla
						case 114: //jpivot sql server xmla
						case 149: //saiku report
						case 150: //saiku connection
						case 120: //lov static
							$("#testReport").hide();
							break;
						default:
							$("#testReport").show();
					}

					//show/hide test report data button
					switch (reportTypeId) {
						case 100: //update
						case 110: //dashboard
						case 129: //gridstack dashboard
						case 111: //text
						case 112: //jpivot mondrian
						case 113: //jpivot mondrian xmla
						case 114: //jpivot sql server xmla
						case 149: //saiku report
						case 150: //saiku connection
						case 115: //jasper template
						case 117: //jxls template
						case 120: //lov static
						case 133: //pivottable.js csv local
						case 134: //pivottable.sj csv server
						case 151: //mongo
						case 136: //dygraphs csv local
						case 137: //dygraphs csv server
						case 139: //datatables csv local
						case 140: //datatables csv server
						case 155: //org chart json
						case 156: //org chart list
						case 157: //org chart ajax
						case 159: //report engine file
							$("#testReportData").hide();
							break;
						default:
							$("#testReportData").show();
					}
				}

				$("#deleteRecord").click(function () {
					var recordName = $("#name").val();
					var recordId = $("#reportId").val();

					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + recordName + "</b>",
						buttons: {
							cancel: {
								label: "${cancelText}"
							},
							confirm: {
								label: "${okText}"
							}
						},
						callback: function (result) {
							if (result) {
								//user confirmed delete. make delete request
								$.ajax({
									type: "POST",
									dataType: "json",
									url: "${pageContext.request.contextPath}/deleteReport",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											//https://datatables.net/reference/type/row-selector
											var rowSelector = "#row-" + recordId;
											table.row(rowSelector).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											$("#editReport").hide();
											notifyActionSuccess("${recordDeletedText}", recordName);
										} else {
											notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors});
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				$("#saveRecord").click(function (e) {
					e.preventDefault();

					var recordName = $("#name").val();
					var recordId = $("#reportId").val();

					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/saveBasicReport",
						data: $('#basicReportForm').serialize(),
						success: function (response) {
							if (response.success) {
								//https://stackoverflow.com/questions/41524345/bootstrap-alert-div-is-not-display-on-second-ajax-request-when-cancel-on-first-a
								var msg;
								msg = reusableAlertCloseButton + "${recordUpdatedText}";
								msg = msg + ": " + recordName + " (" + recordId + ")";
								$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
								$("#ajaxResponse").show();
								var rowSelector = "#row-" + recordId;
								table.cell(rowSelector, 2).data(recordName);
								$.notify("${recordUpdatedText}", "success");
							} else {
								notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				$('#reportsList').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
						});
						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
							buttons: {
								cancel: {
									label: "${cancelText}"
								},
								confirm: {
									label: "${okText}"
								}
							},
							callback: function (result) {
								if (result) {
									//user confirmed delete. make delete request
									$.ajax({
										type: "POST",
										dataType: "json",
										url: "${pageContext.request.contextPath}/deleteReports",
										data: {ids: ids},
										success: function (response) {
											var nonDeletedRecords = response.data;
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccess("${recordsDeletedText}", ids);
											} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
												notifySomeRecordsNotDeleted(nonDeletedRecords, "${someRecordsNotDeletedText}");
											} else {
												notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors});
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#editRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
						});
						window.location.href = '${pageContext.request.contextPath}/editReports?ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=Reports&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$("#refreshRecords").click(function () {
					table.ajax.reload();
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class='row'>
			<div class="col-md-12">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							${encode:forHtmlContent(pageTitle)}
						</h4>
					</div>
				</div>
			</div>
		</div>
		<div class='row' id="reportsList">
			<div class='col-md-8'>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>
				<c:if test="${not empty recordSavedMessage}">
					<div class="alert alert-success alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
					</div>
				</c:if>

				<div id="ajaxResponse">
				</div>

				<div style="margin-bottom: 10px;">
					<div class="btn-group">
						<a class="btn btn-default" href="${pageContext.request.contextPath}/addReport">
							<i class="fa fa-plus"></i>
							<spring:message code="page.action.add"/>
						</a>
						<button type="button" id="editRecords" class="btn btn-default">
							<i class="fa fa-pencil-square-o"></i>
							<spring:message code="page.action.edit"/>
						</button>
						<button type="button" id="deleteRecords" class="btn btn-default">
							<i class="fa fa-trash-o"></i>
							<spring:message code="page.action.delete"/>
						</button>
						<button type="button" id="refreshRecords" class="btn btn-default">
							<i class="fa fa-refresh"></i>
							<spring:message code="page.action.refresh"/>
						</button>
					</div>
					<c:if test="${sessionUser.accessLevel.value >= 80}">
						<div class="btn-group">
							<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Reports">
								<spring:message code="page.text.import"/>
							</a>
							<button type="button" id="exportRecords" class="btn btn-default">
								<spring:message code="page.text.export"/>
							</button>
						</div>
					</c:if>
				</div>

				<div class="table-responsive">
					<table id="reports" class="table table-bordered table-striped table-condensed" style='width: 100%'>
						<thead>
							<tr>
								<th class="noFilter"></th>
								<th><spring:message code="page.text.id"/><br><br></th>
								<th><spring:message code="page.text.name"/><br><br></th>
								<th><spring:message code="reports.text.groupName"/><br><br></th>
								<th><spring:message code="page.text.description"/><br><br></th>
								<th><spring:message code="page.text.active"/><br><br></th>
								<th class="noFilter"><spring:message code="page.text.action"/><br><br></th>
							</tr>
						</thead>
					</table>
				</div>
			</div>
			<div id='editReport' class='col-md-4' style='display: none'>
				<div class='row'>
					<div class="col-md-12">
						<div class="panel panel-default">
							<div class="panel-heading">
								<h4 class="panel-title text-center">
									<spring:message code="page.title.editReport"/>
								</h4>
							</div>
						</div>
					</div>
				</div>
				<div class='row'>
					<div class="col-md-12">
						<form id="basicReportForm" class="form-horizontal" method="POST">
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
							<input type="hidden" name="dummyBoolean" value="true"/>
							<input type="hidden" name="showInline" value="true">
							<input type="hidden" name="basicReport" value="true"/>

							<div class="form-group">
								<div class="col-md-12">
									<div class="btn-group">
										<a class="btn btn-default" id='editLink'
										   href="">
											<i class="fa fa-pencil-square-o"></i>
											<spring:message code="page.action.edit"/>
										</a>
										<button type="button" id="deleteRecord" class="btn btn-default">
											<i class="fa fa-trash-o"></i>
											<spring:message code="page.action.delete"/>
										</button>
										<a class="btn btn-default" id="copyLink"
										   href="">
											<i class="fa fa-copy"></i>
											<spring:message code="page.action.copy"/>
										</a>
									</div>
									<div class="btn-group">
										<button type="button" class="btn btn-default dropdown-toggle"
												data-toggle="dropdown" data-hover="dropdown"
												data-delay="100">
											<spring:message code="reports.action.more"/>
											<span class="caret"></span>
										</button>
										<ul class="dropdown-menu">
											<li>
												<a id="reportParameterConfigLink"
												   href="">
													<spring:message code="reports.action.parameters"/>
												</a>
											</li>
											<li>
												<a id="reportRulesLink"
												   href="">
													<spring:message code="reports.action.rules"/>
												</a>
											</li>
											<li>
												<a id="drilldownsLink"
												   href="">
													<spring:message code="reports.action.drilldowns"/>
												</a>
											</li>
											<li>
												<a id="reportAccessRightsLink"
												   href="">
													<spring:message code="page.action.accessRights"/>
												</a>
											</li>
											<li class="divider"></li>
											<li id='scheduleItem'>
												<a id="addJobLink"
												   href="">
													<spring:message code="reports.action.schedule"/>
												</a>
											</li>
											<li id='previewItem'>
												<a id="selectReportParametersLink"
												   href="">
													<spring:message code="reports.action.preview"/>
												</a>
											</li>
										</ul>
									</div>
								</div>
							</div>

							<div class="form-group">
								<label class="control-label col-md-4">
									<spring:message code="page.label.id"/>
								</label>
								<div class="col-md-8">
									<input type="text" name="reportId" id="reportId" readonly class="form-control"/>
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-4" for="name">
									<spring:message code="page.text.name"/>
								</label>
								<div class="col-md-8">
									<input type="text" name="name" id="name" maxlength="50" class="form-control"/>
								</div>
							</div>
							<div class="form-group">
								<label class="col-md-4 control-label " for="reportTypeId">
									<spring:message code="reports.label.reportType"/>
								</label>
								<div class="col-md-8">
									<select name="reportTypeId" id="reportTypeId" class="form-control selectpicker">
										<c:forEach var="reportType" items="${reportTypes}">
											<option value="${reportType.value}">${encode:forHtmlContent(reportType.description)}</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<div id="datasourceDiv" class="form-group">
								<label class="col-md-4 control-label " for="datasource.datasourceId">
									<spring:message code="page.text.datasource"/>
								</label>
								<div class="col-md-8">
									<select name="datasource.datasourceId" id="datasource.datasourceId" class="form-control selectpicker">
										<option value="0">--</option>
										<option data-divider="true"></option>
										<c:forEach var="datasource" items="${datasources}">
											<c:set var="datasourceStatus">
												<t:displayActiveStatus active="${datasource.active}" hideActive="true"/>
											</c:set>
											<option value="${datasource.datasourceId}"
													data-content="${encode:forHtmlAttribute(datasource.name)}&nbsp;${encode:forHtmlAttribute(datasourceStatus)}">
												${encode:forHtmlContent(datasource.name)} 
											</option>
										</c:forEach>
									</select>
								</div>
							</div>

							<div id="useGroovyDiv" class="form-group">
								<label class="control-label col-md-4" for="useGroovy">
									<spring:message code="reports.label.useGroovy"/>
								</label>
								<div class="col-md-8">
									<div class="checkbox">
										<input type="checkbox" name="useGroovy" id="useGroovy" class="switch-yes-no"/>
									</div>
								</div>
							</div>

							<div id="pivotTableJsSavedOptionsDiv" class="form-group">
								<label class="control-label col-md-12" style="text-align: center" for="pivotTableJsSavedOptions">
									<spring:message code="reports.label.savedOptions"/>
								</label>
								<div class="col-md-12">
									<input type="hidden" name="pivotTableJsSavedOptions" id="pivotTableJsSavedOptions"/>
									<div id="pivotTableJsSavedOptionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
								</div>
							</div>

							<div id="gridstackSavedOptionsDiv" class="form-group">
								<label class="control-label col-md-12" style="text-align: center" for="gridstackSavedOptions">
									<spring:message code="reports.label.savedOptions"/>
								</label>
								<div class="col-md-12">
									<input type="hidden" name="gridstackSavedOptions" id="gridstackSavedOptions"/>
									<div id="gridstackSavedOptionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
								</div>
							</div>

							<div id="optionsDiv" class="form-group">
								<label class="control-label col-md-12" style="text-align: center" for="options">
									<spring:message code="page.label.options"/>
								</label>
								<div class="col-md-12">
									<input type="hidden" name="options" id="options"/>
									<div id="optionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
								</div>
							</div>

							<label id="reportSourceLabel" class="col-md-12 control-label" style="text-align: center">
							</label>

							<div id="reportSourceDiv" class="form-group">
								<div class="col-md-12">
									<input type="hidden" name="reportSource" id="reportSource"/>
									<div id="sqlEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
									<div id="xmlEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
									<div id="jsonEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
									<div id="groovyEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
								</div>
							</div>
							<div id="reportSourceHtmlDiv" class="form-group">
								<div class="col-md-12">
									<textarea name="reportSourceHtml" id="reportSourceHtml" rows="20" cols="70" class="form-control editor"></textarea>
									<input name="image" type="file" id="upload" style="display:none;" onchange="">
								</div>
							</div>

							<div class="form-group">
								<div class="col-md-12">
									<span class="pull-right">
										<button type="button" class="btn btn-default" id="applyOptions">
											<spring:message code="page.button.apply"/>
										</button>
										<button type="button" id="testReportData" class="btn btn-default action">
											<spring:message code="reports.button.testData"/>
										</button>
										<button type="button" id="testReport" class="btn btn-default action">
											<spring:message code="reports.button.test"/>
										</button>
										<button type="submit" id="saveRecord" class="btn btn-primary">
											<spring:message code="page.button.save"/>
										</button>
									</span>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div id="reportOutput"></div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

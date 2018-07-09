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
			${panelTitle} - ${report.getLocalizedName(pageContext.response.locale)}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="reports.text.selectFile" var="selectFileText"/>
<spring:message code="reports.text.change" var="changeText"/>
<spring:message code="reports.label.reportSource" var="reportSourceText"/>
<spring:message code="page.link.help" var="helpText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="reports.message.fileTypeNotAllowed" var="fileTypeNotAllowedText"/>
<%-- https://stackoverflow.com/questions/8588365/spring-message-tag-with-multiple-arguments --%>
<spring:message code="reports.message.fileTooLargeMB" arguments="${maxFileSizeMB}" var="fileTooLargeMBText"/>
<spring:message code="fileupload.button.start" var="startText"/>
<spring:message code="fileupload.button.cancel" var="cancelText"/>
<spring:message code="page.action.delete" var="deleteText"/>
<spring:message code="caches.action.clear" var="clearText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="belowMainPanel">
		<div class="row">
			<div class="col-md-12">
				<div id="reportOutput"></div>
			</div>
		</div>
	</jsp:attribute>

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/css/jasny-bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/css/jquery.fileupload.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/css/jquery.fileupload-ui.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.3.8/tinymce.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/js/jasny-bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/vendor/jquery.ui.widget.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.iframe-transport.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-process.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-validate.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-ui.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace-min-noconflict-1.2.6/ace.js" charset="utf-8"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

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

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

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

				toggleVisibleFields(); //show/hide on page load

				$('#name').focus();

				var maxFileSizeBytes = ${maxFileSizeBytes};
				if (maxFileSizeBytes < 0) {
					//-1 or any negative value means no size limit
					//set to undefined
					//https://stackoverflow.com/questions/5795936/how-to-set-a-javascript-var-as-undefined
					maxFileSizeBytes = void 0;
				}

				//https://github.com/blueimp/jQuery-File-Upload/wiki/Options
				//https://stackoverflow.com/questions/34063348/jquery-file-upload-basic-plus-ui-and-i18n
				//https://stackoverflow.com/questions/11337897/how-to-customize-upload-download-template-of-blueimp-jquery-file-upload
				$('#fileupload').fileupload({
					url: '${pageContext.request.contextPath}/uploadResources',
					fileInput: $('#fileuploadInput'),
					acceptFileTypes: /(\.|\/)(jrxml|png|jpe?g|csv|txt|css|js|json|xml)$/i,
					maxFileSize: maxFileSizeBytes,
					messages: {
						acceptFileTypes: '${fileTypeNotAllowedText}',
						maxFileSize: '${fileTooLargeMBText}'
					},
					filesContainer: $('.files'),
					uploadTemplateId: null,
					downloadTemplateId: null,
					uploadTemplate: function (o) {
						var rows = $();
						$.each(o.files, function (index, file) {
							var row = $('<tr class="template-upload fade">' +
									'<td><p class="name"></p>' +
									'<strong class="error text-danger"></strong>' +
									'</td>' +
									'<td><p class="size"></p>' +
									'<div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">' +
									'<div class="progress-bar progress-bar-success" style="width:0%;"></div></div>' +
									'</td>' +
									'<td>' +
									(!index && !o.options.autoUpload ?
											'<button class="btn btn-primary start" disabled>' +
											'<i class="glyphicon glyphicon-upload"></i> ' +
											'<span>${startText}</span>' +
											'</button>' : '') +
									(!index ? '<button class="btn btn-warning cancel">' +
											'<i class="glyphicon glyphicon-ban-circle"></i> ' +
											'<span>${cancelText}</span>' +
											'</button>' : '') +
									'</td>' +
									'</tr>');
							row.find('.name').text(file.name);
							row.find('.size').text(o.formatFileSize(file.size));
							if (file.error) {
								row.find('.error').text(file.error);
							}
							rows = rows.add(row);
						});
						return rows;
					},
					downloadTemplate: function (o) {
						var rows = $();
						$.each(o.files, function (index, file) {
							var row = $('<tr class="template-download fade">' +
									'<td><p class="name"></p>' +
									(file.error ? '<strong class="error text-danger"></strong>' : '') +
									'</td>' +
									'<td><span class="size"></span></td>' +
									'<td>' +
									(file.deleteUrl ? '<button class="btn btn-danger delete">' +
											'<i class="glyphicon glyphicon-trash"></i> ' +
											'<span>${deleteText}</span>' +
											'</button>' : '') +
									'<button class="btn btn-warning cancel">' +
									'<i class="glyphicon glyphicon-ban-circle"></i> ' +
									'<span>${clearText}</span>' +
									'</button>' +
									'</td>' +
									'</tr>');
							row.find('.name').text(file.name);
							row.find('.size').text(o.formatFileSize(file.size));
							if (file.error) {
								row.find('.error').text(file.error);
							}
							if (file.deleteUrl) {
								row.find('button.delete')
										.attr('data-type', file.deleteType)
										.attr('data-url', file.deleteUrl);
							}
							rows = rows.add(row);
						});
						return rows;
					}
				});

				$("#testReport").click(function () {
					//https://stackoverflow.com/questions/2122085/jquery-and-tinymce-textarea-value-doesnt-submit
					tinymce.triggerSave();

					//disable buttons
					$('.action').prop('disabled', true);

					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/runReport",
						data: $('#fileupload').serialize(),
						success: function (data) {
							$("#reportOutput").html(data);
							$('.action').prop('disabled', false);
						},
						error: function (xhr) {
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
						data: $('#fileupload').serialize() + "&testData=true&reportFormat=htmlDataTable",
						success: function (data) {
							$("#reportOutput").html(data);
							$('.action').prop('disabled', false);
						},
						error: function (xhr) {
							//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
							bootbox.alert(xhr.responseText);
							$('.action').prop('disabled', false);
						}
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
				sqlEditor.getSession().setValue(reportSource.val());
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

				xmlEditor.getSession().setValue(reportSource.val());
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
				optionsEditor.getSession().setValue(options.val());
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
				pivotTableJsSavedOptionsEditor.getSession().setValue(pivotTableJsSavedOptions.val());
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
				gridstackSavedOptionsEditor.getSession().setValue(gridstackSavedOptions.val());
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

				jsonEditor.getSession().setValue(reportSource.val());
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

				groovyEditor.getSession().setValue(reportSource.val());
				groovyEditor.getSession().on('change', function () {
					reportSource.val(groovyEditor.getSession().getValue());
				});

				$('#useGroovy').on('switchChange.bootstrapSwitch', function (event, state) {
					toggleGroovyEditor(reportSource, groovyEditor, sqlEditor);
				});

				toggleGroovyEditor(reportSource, groovyEditor, sqlEditor);

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
			});
		</script>

		<script type="text/javascript">
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
						//do nothing
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

				//show/hide source report id
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
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 156: //org chart list
					case 149: //saiku report
					case 155: //org chart json
					case 151: //mongodb
					case 111: //text
						$("#sourceReportIdDiv").hide();
						break;
					default:
						$("#sourceReportIdDiv").show();
				}

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

				//show/hide use rules
				switch (reportTypeId) {
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 111: //text
					case 115: //jasperreport template
					case 117: //jxls template
					case 120: //static lov
					case 121: //dynamic job recipients
					case 113: //jpivot mondrian xmla
					case 114: //jpivot microsoft xmla
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
						$("#usesRulesDiv").hide();
						break;
					default:
						$("#usesRulesDiv").show();
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

				//show/hide template
				switch (reportTypeId) {
					case 115: //jasper template
					case 116: //jasper art
					case 117: //jxls template
					case 118: //jxls art
					case 112: //jpivot mondrian
					case 122: //freemarker
					case 131: //thymeleaf
					case 153: //velocity
					case 123: //xdocreport freemarker docx
					case 124: //xdocreport velocity docx
					case 125: //xdocreport freemarker odt
					case 126: //xdocreport velocity odt
					case 127: //xdocreport freemarker pptx
					case 128: //xdocreport velocity pptx
					case 130: //react pivot
					case 132: //pivottable.js
					case 133: //pivottable.js csv local
					case 134: //pivottable.js csv server
					case 135: //dygraphs
					case 136: //dygraphs csv local
					case 137: //dygraphs csv server
					case 138: //datatables
					case 139: //datatables csv local
					case 140: //datatables csv server
					case 142: //c3
					case 143: //chart.js
					case 144: //datamaps
					case 145: //datamaps file
					case 146: //leaflet
					case 147: //openlayers
					case 150: //saiku connection
					case 154: //org chart database
					case 155: //org chart json
					case 156: //org chart list
					case 157: //org chart ajax
					case 159: //reportengine file
					case 160: //plotly
						$("#templateDiv").show();
						break;
					default:
						$("#templateDiv").hide();
				}

				//show/hide resources
				switch (reportTypeId) {
					case 115: //jasper template
					case 116: //jasper art
					case 134: //pivottable.js csv server
					case 137: //dygraphs csv server
					case 140: //datatables csv server
					case 142: //c3
					case 144: //datamaps
					case 145: //datamaps file
					case 146: //leaflet
					case 147: //openlayers
					case 117: //jxls template
					case 118: //jxls art
					case 154: //org chart database
					case 155: //org chart json
					case 156: //org chart list
					case 157: //org chart ajax
						$("#resourcesDiv").show();
						break;
					default:
						$("#resourcesDiv").hide();
				}

				//show/hide xmla fields
				switch (reportTypeId) {
					case 113: //jpivot mondrian xmla
						$("#xmlaFields").show();
						$("#xmlaDatasourceDiv").show();
						break;
					case 114: //jpivot sql server xmla
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
					case 129: //gridstack dashboard
					case 111: //text
					case 112: //jpivot mondrian
					case 113: //jpivot mondrian xmla
					case 114: //jpivot sql server xmla
					case 115: //jasper template
					case 117: //jxls template
					case 120: //static lov
					case 133: //pivottable.js csv local
					case 134: //pivottable.js csv server
					case 136: //dygraphs csv local
					case 137: //dygraphs csv server
					case 139: //datatables csv local
					case 140: //datatables csv server
					case 145: //datamaps file
					case 149: //saiku report
					case 150: //saiku connection
					case 151: //mongodb
					case 155: //org chart json
					case 156: //org chart list
					case 157: //org chart ajax
					case 159: //reportengine file
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
						case - 2: //pie 3d
						case - 13: //pie 2d
						case - 10: //speedometer
							$("#chartAxisLabelFields").hide();
							break;
						default:
							$("#chartAxisLabelFields").show();
					}
				} else {
					$("#chartFields").hide();
				}

				//show/hide group column
				if (reportTypeId === 1) {
					//group
					$("#groupColumnDiv").show();
				} else {
					$("#groupColumnDiv").hide();
				}

				//show/hide default report format
				switch (reportTypeId) {
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 112: //jpivot mondrian
					case 113: //jpivot mondrian xmla
					case 114: //jpivot sql server xmla
					case 100: //update
					case 111: //text
					case 103: //tabular html
					case 102: //crosstab html
					case 118: //jxls art
					case 117: //jxls template
					case 122: //freemarker
					case 131: //thymeleaf
					case 153: //velocity
					case 130: //react pivot
					case 132: //pivottable.js
					case 133: //pivottable.js csv local
					case 134: //pivottable.js csv server
					case 135: //dygraphs
					case 136: //dygraphs csv local
					case 137: //dygraphs csv server
					case 138: //datatables
					case 139: //datatables csv local
					case 140: //datatables csv server
					case 141: //fixed width
					case 142: //c3
					case 143: //chart.js
					case 144: //datamaps
					case 145: //datamaps file
					case 146: //leaflet
					case 147: //openlayers
					case 149: //saiku report
					case 150: //saiku connection
					case 151: //mongodb
					case 154: //org chart database
					case 155: //org chart json
					case 156: //org chart list
					case 157: //org chart ajax
					case 160: //plotly
						$("#defaultReportFormatDiv").hide();
						break;
					default:
						$("#defaultReportFormatDiv").show();
				}

				//show/hide tabular fields
				switch (reportTypeId) {
					case 0: //tabular
					case 103: //tabular html
					case 148: //tabular heatmap
					case 101: //crosstab
					case 102: //crosstab html
						$("#tabularFields").show();
						break;
					default:
						$("#tabularFields").hide();
				}

				//show/hide omit title row
				switch (reportTypeId) {
					case 0: //tabular
					case 103: //tabular html
					case 148: //tabular heatmap
					case 101: //crosstab
					case 102: //crosstab html
					case 158: //reportengine
					case 159: //reportengine file
						$("#omitTitleRowDiv").show();
						break;
					default:
						$("#omitTitleRowDiv").hide();
				}

				//show/hide fetch size
				switch (reportTypeId) {
					case 100: //update
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 111: //text
					case 112: //jpivot mondrian
					case 113: //jpivot mondrian xmla
					case 114: //jpivot sql server xmla
					case 115: //jasper template
					case 117: //jxls template
					case 120: //static lov
					case 133: //pivottable.js csv local
					case 134: //pivottable.js csv server
					case 136: //dygraphs csv local
					case 137: //dygraphs csv server
					case 139: //datatables csv local
					case 140: //datatables csv server
					case 145: //datamaps file
					case 149: //saiku report
					case 150: //saiku connection
					case 151: //mongodb
					case 155: //org chart json
					case 156: //org chart list
					case 157: //org chart ajax
					case 159: //reportengine file
						$("#fetchSizeDiv").hide();
						break;
					default:
						$("#fetchSizeDiv").show();
				}

				//show/hide page orientation
				if (reportTypeId <= 1 || reportTypeId === 101) {
					//charts, tabular, group, crosstab
					$("#pageOrientationDiv").show();
				} else {
					$("#pageOrientationDiv").hide();
				}

				//show/hide lov use dynamic datasource
				if (reportTypeId === 119) {
					//lov dynamic
					$("#lovUseDynamicDatasourceDiv").show();
				} else {
					$("#lovUseDynamicDatasourceDiv").hide();
				}

				//show/hide locale
				switch (reportTypeId) {
					case 0: //tabular
					case 103: //tabular html
					case 148: //tabular heatmap
					case 101: //crosstab
					case 102: //crosstab html
					case 141: //fixed width
					case 152: //csv
						$("#localeDiv").show();
						break;
					default:
						$("#localeDiv").hide();
				}

				//show/hide open and modify password
				switch (reportTypeId) {
					case 0: //tabular
					case 101: //crosstab
					case 1: //group
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 115: //jasper template
					case 116: //jasper art
					case 123: //xdocreport docx freemarker
					case 124: //xdocreport docx velocity
					case 125: //xdocreport odt freemarker
					case 126: //xdocreport odt velocity
					case 117: //jxls template
					case 118: //jxls art
					case 158: //reportengine
					case 159: //reportengine file
						$("#openPasswordDiv").show();
						$("#modifyPasswordDiv").show();
						break;
					default:
						if (reportTypeId < 0) {
							//charts
							$("#openPasswordDiv").show();
							$("#modifyPasswordDiv").show();
						} else {
							$("#openPasswordDiv").hide();
							$("#modifyPasswordDiv").hide();
						}
				}

				//show/hide encryptor
				switch (reportTypeId) {
					case 0: //tabular
					case 101: //crosstab
					case 1: //group
					case 110: //dashboard
					case 129: //gridstack dashboard
					case 115: //jasper template
					case 116: //jasper art
					case 117: //jxls template
					case 118: //jxls art
					case 123: //xdocreport docx freemarker
					case 124: //xdocreport docx velocity
					case 125: //xdocreport odt freemarker
					case 126: //xdocreport odt velocity
					case 127: //xdocreport pptx freemarker
					case 128: //xdocreport pptx velocity
					case 152: //csv
					case 141: //fixed width
					case 158: //reportengine
					case 159: //reportengine file
						$("#encryptorDiv").show();
						break;
					default:
						if (reportTypeId < 0) {
							//charts
							$("#encryptorDiv").show();
						} else {
							$("#encryptorDiv").hide();
						}
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
		<spring:url var="formUrl" value="/saveReport"/>
		<form:form id="fileupload" class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="report" enctype="multipart/form-data">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
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

				<input type="hidden" name="showInline" id="showInline" value="true">
				<input type="hidden" name="action" value="${action}">
				<form:hidden path="dummyBoolean" value="true"/>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'edit'}">
								<form:input path="reportId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy' || action == 'add'}">
								<form:hidden path="reportId"/>
							</c:when>
						</c:choose>
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
					<label class="col-md-4 control-label " for="reportGroups">
						<spring:message code="page.text.reportGroups"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportGroups" items="${reportGroups}" multiple="true" 
									 itemLabel="name" itemValue="reportGroupId" 
									 class="form-control selectpicker"
									 data-actions-box="true"
									 />
						<form:errors path="reportGroups" cssClass="error"/>
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
						<form:textarea path="description" rows="2" cols="40" class="form-control" maxlength="2000"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="contactPerson">
						<spring:message code="reports.label.contactPerson"/>
					</label>
					<div class="col-md-8">
						<form:input path="contactPerson" maxlength="100" class="form-control"/>
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
						<form:select path="datasource" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<c:forEach var="datasource" items="${datasources}">
								<c:set var="datasourceStatus">
									<t:displayActiveStatus active="${datasource.active}" hideActive="true"/>
								</c:set>
								<c:if test="${report.datasource != null}">
									<c:set var="selected">
										${report.datasource.datasourceId == datasource.datasourceId ? "selected" : ""}
									</c:set>
								</c:if>
								<option value="${datasource.datasourceId}" ${selected}
										data-content="${encode:forHtmlAttribute(datasource.name)}&nbsp;${encode:forHtmlAttribute(datasourceStatus)}">
									${encode:forHtmlContent(datasource.name)}
								</option>
							</c:forEach>
						</form:select>
						<form:errors path="datasource" cssClass="error"/>
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

				<div id="omitTitleRowDiv" class="form-group">
					<label class="control-label col-md-4" for="omitTitleRow">
						<spring:message code="reports.label.omitTitleRow"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="omitTitleRow" id="omitTitleRow" class="switch-yes-no"/>
						</div>
						<form:errors path="omitTitleRow" cssClass="error"/>
					</div>
				</div>

				<fieldset id="tabularFields">
					<div class="form-group">
						<label class="col-md-4 control-label " for="hiddenColumns">
							<spring:message code="reports.label.hiddenColumns"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="hiddenColumns" maxlength="500" class="form-control"/>
								<spring:message code="reports.help.hiddenColumns" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="hiddenColumns" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="totalColumns">
							<spring:message code="reports.label.totalColumns"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="totalColumns" maxlength="500" class="form-control"/>
								<spring:message code="reports.help.totalColumns" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="totalColumns" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="dateFormat">
							<spring:message code="page.label.dateFormat"/>
						</label>
						<div class="col-md-8">
							<form:input path="dateFormat" maxlength="100" class="form-control"/>
							<form:errors path="dateFormat" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="numberFormat">
							<spring:message code="reports.label.numberFormat"/>
						</label>
						<div class="col-md-8">
							<form:input path="numberFormat" maxlength="50" class="form-control"/>
							<form:errors path="numberFormat" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="columnFormats">
							<spring:message code="reports.label.columnFormats"/>
						</label>
						<div class="col-md-8">
							<form:textarea path="columnFormats" rows="5" cols="20" wrap="off" class="form-control"/>
							<form:errors path="columnFormats" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="nullNumberDisplay">
							<spring:message code="reports.label.nullNumberDisplay"/>
						</label>
						<div class="col-md-8">
							<form:input path="nullNumberDisplay" maxlength="50" class="form-control"/>
							<form:errors path="nullNumberDisplay" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="nullStringDisplay">
							<spring:message code="reports.label.nullStringDisplay"/>
						</label>
						<div class="col-md-8">
							<form:input path="nullStringDisplay" maxlength="50" class="form-control"/>
							<form:errors path="nullStringDisplay" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<div id="localeDiv" class="form-group">
					<label class="control-label col-md-4" for="locale">
						<spring:message code="reports.label.locale"/>
					</label>
					<div class="col-md-8">
						<form:input path="locale" maxlength="50" class="form-control"/>
						<form:errors path="locale" cssClass="error"/>
					</div>
				</div>

				<div id="fetchSizeDiv" class="form-group">
					<label class="control-label col-md-4" for="fetchSize">
						<spring:message code="reports.label.fetchSize"/>
					</label>
					<div class="col-md-8">
						<form:input path="fetchSize" maxlength="5" class="form-control"/>
						<form:errors path="fetchSize" cssClass="error"/>
					</div>
				</div>

				<div id="pageOrientationDiv" class="form-group">
					<label class="control-label col-md-4" for="pageOrientation">
						<spring:message code="reports.label.pageOrientation"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="pageOrientation" items="${pageOrientations}">
							<label class="radio-inline">
								<form:radiobutton path="pageOrientation"
												  value="${pageOrientation}"/>
								<spring:message code="${pageOrientation.localizedDescription}"/>
							</label>
						</c:forEach>
						<form:errors path="pageOrientation" cssClass="error"/>
					</div>
				</div>

				<div id="lovUseDynamicDatasourceDiv" class="form-group">
					<label class="control-label col-md-4" for="lovUseDynamicDatasource">
						<spring:message code="reports.label.lovUseDynamicDatasource"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="lovUseDynamicDatasource" id="lovUseDynamicDatasource" class="switch-yes-no"/>
						</div>
						<form:errors path="lovUseDynamicDatasource" cssClass="error"/>
					</div>
				</div>

				<div id="openPasswordDiv" class="form-group">
					<label class="control-label col-md-4" for="openPassword">
						<spring:message code="reports.label.openPassword"/>
					</label>
					<div class="col-md-8">
						<div>
							<form:password path="openPassword" autocomplete="off" maxlength="100" class="form-control"/>
						</div>
						<div>
							<label class="checkbox-inline">
								<form:checkbox path="useNoneOpenPassword" id="useNoneOpenPassword"/>
								<spring:message code="reports.checkbox.none"/>
							</label>
						</div>
						<form:errors path="openPassword" cssClass="error"/>
					</div>
				</div>

				<div id="modifyPasswordDiv" class="form-group">
					<label class="control-label col-md-4" for="modifyPassword">
						<spring:message code="reports.label.modifyPassword"/>
					</label>
					<div class="col-md-8">
						<div>
							<form:password path="modifyPassword" autocomplete="off" maxlength="100" class="form-control"/>
						</div>
						<div>
							<label class="checkbox-inline">
								<form:checkbox path="useNoneModifyPassword" id="useNoneModifyPassword"/>
								<spring:message code="reports.checkbox.none"/>
							</label>
						</div>
						<form:errors path="modifyPassword" cssClass="error"/>
					</div>
				</div>

				<div id="encryptorDiv" class="form-group">
					<label class="col-md-4 control-label " for="encryptor">
						<spring:message code="reports.label.encryptor"/>
					</label>
					<div class="col-md-8">
						<form:select path="encryptor" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<c:forEach var="encryptor" items="${encryptors}">
								<c:set var="encryptorStatus">
									<t:displayActiveStatus active="${encryptor.active}" hideActive="true"/>
								</c:set>
								<c:if test="${report.encryptor != null}">
									<c:set var="selected">
										${report.encryptor.encryptorId == encryptor.encryptorId ? "selected" : ""}
									</c:set>
								</c:if>
								<option value="${encryptor.encryptorId}" ${selected}
										data-content="${encode:forHtmlAttribute(encryptor.name)}&nbsp;${encode:forHtmlAttribute(encryptorStatus)}">
									${encode:forHtmlContent(encryptor.name)}
								</option>
							</c:forEach>
						</form:select>
						<form:errors path="encryptor" cssClass="error"/>
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
					<div class="form-group">
						<label class="col-md-4 control-label " for="secondaryCharts">
							<spring:message code="reports.label.secondaryCharts"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="secondaryCharts" maxlength="100" class="form-control"/>
								<spring:message code="reports.help.secondaryCharts" var="help"/>
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<form:errors path="secondaryCharts" cssClass="error"/>
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
					</div>
				</div>
				<div id="resourcesDiv" class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="reports.text.resources"/>
					</label>
					<div class="col-md-8">
						<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
						<div class="fileupload-buttonbar">
							<div>
								<!-- The fileinput-button span is used to style the file input field as button -->
								<span class="btn btn-success fileinput-button">
									<i class="glyphicon glyphicon-plus"></i>
									<span><spring:message code="fileupload.button.addFiles"/></span>
									<input id="fileuploadInput" type="file" name="files[]" multiple>
								</span>
								<%-- https://stackoverflow.com/questions/925334/how-is-the-default-submit-button-on-an-html-form-determined --%>
								<button type="button" class="btn btn-primary start">
									<i class="glyphicon glyphicon-upload"></i>
									<span><spring:message code="fileupload.button.startUpload"/></span>
								</button>
								<button type="reset" class="btn btn-warning cancel">
									<i class="glyphicon glyphicon-ban-circle"></i>
									<span><spring:message code="fileupload.button.cancelUpload"/></span>
								</button>
								<!-- The global file processing state -->
								<span class="fileupload-process"></span>
							</div>
							<!-- The global progress state -->
							<div class="fileupload-progress fade">
								<!-- The global progress bar -->
								<div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
									<div class="progress-bar progress-bar-success" style="width:0%;"></div>
								</div>
								<!-- The extended global progress state -->
								<div class="progress-extended">&nbsp;</div>
							</div>
						</div>
						<!-- The table listing the files available for upload/download -->
						<table role="presentation" class="table table-striped"><tbody class="files"></tbody></table>
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

				<div id="sourceReportIdDiv" class="form-group">
					<label class="control-label col-md-4" for="sourceReportId">
						<spring:message code="reports.label.sourceReport"/>
					</label>
					<div class="col-md-8">
						<form:input path="sourceReportId" maxlength="10" class="form-control"/>
						<form:errors path="sourceReportId" cssClass="error"/>
					</div>
				</div>
				<div id="useGroovyDiv" class="form-group">
					<label class="control-label col-md-4" for="useGroovy">
						<spring:message code="reports.label.useGroovy"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="useGroovy" id="useGroovy" class="switch-yes-no"/>
						</div>
						<form:errors path="useGroovy" cssClass="error"/>
					</div>
				</div>

				<div id="pivotTableJsSavedOptionsDiv" class="form-group">
					<label class="control-label col-md-12" style="text-align: center" for="pivotTableJsSavedOptions">
						<spring:message code="reports.label.savedOptions"/>
					</label>
					<div class="col-md-12">
						<form:hidden path="pivotTableJsSavedOptions"/>
						<div id="pivotTableJsSavedOptionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
					</div>
				</div>

				<div id="gridstackSavedOptionsDiv" class="form-group">
					<label class="control-label col-md-12" style="text-align: center" for="gridstackSavedOptions">
						<spring:message code="reports.label.savedOptions"/>
					</label>
					<div class="col-md-12">
						<form:hidden path="gridstackSavedOptions"/>
						<div id="gridstackSavedOptionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
					</div>
				</div>

				<div id="optionsDiv" class="form-group">
					<label class="control-label col-md-12" style="text-align: center" for="options">
						<spring:message code="page.label.options"/>
					</label>
					<div class="col-md-12">
						<form:hidden path="options"/>
						<div id="optionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
					</div>
				</div>

				<label id="reportSourceLabel" class="col-md-12 control-label" style="text-align: center">
				</label>

				<div id="reportSourceDiv" class="form-group">
					<div class="col-md-12">
						<form:hidden path="reportSource"/>
						<div id="sqlEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
						<div id="xmlEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
						<div id="jsonEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
						<div id="groovyEditor" style="height: 400px; width: 100%; border: 1px solid black"></div>
					</div>
				</div>
				<div id="reportSourceHtmlDiv" class="form-group">
					<div class="col-md-12">
						<form:textarea path="reportSourceHtml" rows="20" cols="70" wrap="off" class="form-control editor"/>
						<input name="image" type="file" id="upload" style="display:none;" onchange="">
						<form:errors path="reportSourceHtml" cssClass="error"/>
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
							<button type="submit" class="btn btn-primary">
								<spring:message code="page.button.save"/>
							</button>
						</span>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>

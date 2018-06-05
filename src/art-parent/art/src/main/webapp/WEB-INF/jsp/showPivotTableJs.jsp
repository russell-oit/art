<%-- 
    Document   : showPivotTableJs
    Created on : 05-Feb-2017, 21:38:29
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="pivotTableJs.text.processing" var="processingText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="reports.message.reportSaved" var="reportSavedText"/>
<spring:message code="reports.message.reportDeleted" var="reportDeletedText"/>
<spring:message code="dialog.title.saveReport" var="saveReportText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="reports.message.cannotDeleteReport" var="cannotDeleteReportText"/>


<div class="row form-inline" style="margin-right: 1px; margin-bottom: 5px">
	<span class="pull-right">
		<a class="btn btn-default" id="link-${outputDivId}" style="display: none"
		   href="">
			<spring:message code="reports.link.newReport"/>
		</a>
		<c:if test="${exclusiveAccess}">
			<button class="btn btn-default" id="delete-${outputDivId}">
				<spring:message code="page.action.delete"/>
			</button>
		</c:if>
		<button class="btn btn-primary" id="save-${outputDivId}">
			<spring:message code="page.button.save"/>
		</button>
	</span>
</div>

<div id="${outputDivId}" class='pivotTableJsOutputDiv'>

</div>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>

<%-- c3 0.4 doesn't work with d3 4.x --%>
<%-- https://github.com/nicolaskruchten/pivottable/issues/579 --%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/pivottable-2.20.0/pivot.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.20.0/pivot.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.20.0/c3_renderers.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.20.0/export_renderers.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/PapaParse-4.1.4/papaparse.min.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/pivottable-subtotal-renderer-1.7.1/subtotal.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-subtotal-renderer-1.7.1/subtotal.min.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/plotly-basic.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.20.0/plotly_renderers.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

<c:if test="${not empty locale}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.20.0/pivot.${locale}.js"></script>
</c:if>

<script type="text/javascript">
	//set default values. can be overridden in template file
	//https://github.com/nicolaskruchten/pivottable/wiki/Parameters
	//https://stackoverflow.com/questions/4528744/how-does-extend-work-in-jquery
	//https://stackoverflow.com/questions/10130908/jquery-merge-two-objects
	var locale;
	var passedLocale = '${encode:forJavaScript(locale)}';
	if (passedLocale) {
		//passed locale is empty if translation file doesn't exist i.e. pivot.xx.js
		locale = passedLocale;
	} else {
		locale = "en";
	}

	//https://github.com/nicolaskruchten/pivottable/issues/875
	var renderers = {};
	if ($.pivotUtilities.locales[locale].renderers) {
		$.extend(renderers, $.pivotUtilities.locales[locale].renderers);
	} else {
		$.extend(renderers, $.pivotUtilities.renderers);
	}

	if ($.pivotUtilities.locales[locale].c3_renderers) {
		$.extend(renderers, $.pivotUtilities.locales[locale].c3_renderers);
	} else {
		$.extend(renderers, $.pivotUtilities.c3_renderers);
	}

	if ($.pivotUtilities.locales[locale].plotly_renderers) {
		$.extend(renderers, $.pivotUtilities.locales[locale].plotly_renderers);
	} else {
		$.extend(renderers, $.pivotUtilities.plotly_renderers);
	}

	if ($.pivotUtilities.locales[locale].export_renderers) {
		$.extend(renderers, $.pivotUtilities.locales[locale].export_renderers);
	} else {
		$.extend(renderers, $.pivotUtilities.export_renderers);
	}

	if ($.pivotUtilities.locales[locale].subtotal_renderers) {
		$.extend(renderers, $.pivotUtilities.locales[locale].subtotal_renderers);
	} else {
		$.extend(renderers, $.pivotUtilities.subtotal_renderers);
	}

	var options = {
		renderers: renderers,
		dataClass: $.pivotUtilities.SubtotalPivotData
	};

	var configString = '${encode:forJavaScript(configJson)}';
	if (configString) {
		var specifiedConfig = JSON.parse(configString);
		$.extend(options, specifiedConfig);
	}

	var overwrite = false;

	var download;
	var reportType = '${encode:forJavaScript(reportType)}';
	if (reportType === 'PivotTableJsCsvServer') {
		download = true;
	} else {
		download = false;
	}

	var csvConfig = {
		download: download,
		skipEmptyLines: true,
		error: function (e) {
			bootbox.alert(e);
		},
		complete: function (parsed) {
			$("#${outputDivId}").pivotUI(parsed.data, options, overwrite, locale);
		}
	};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>
</c:if>

<c:if test="${not empty cssFileName}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(cssFileName)}">
</c:if>

<c:if test="${not empty plotlyLocaleFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/plotly.js-1.36.0/${plotlyLocaleFileName}"></script>
</c:if>

<script>
	var savedConfigString = '${encode:forJavaScript(savedConfigJson)}';
	if (savedConfigString) {
		var savedConfig = JSON.parse(savedConfigString);
		$.extend(options, savedConfig);
	}
</script>

<c:choose>
	<c:when test="${reportType == 'PivotTableJs'}">
		<script type="text/javascript">
			var inputString = '${encode:forJavaScript(input)}';
			var input = JSON.parse(inputString);
			$("#${outputDivId}").pivotUI(input, options, overwrite, locale);
		</script>
	</c:when>
	<c:when test="${reportType == 'PivotTableJsCsvLocal'}">
		<%-- http://nicolas.kruchten.com/pivottable/examples/local.html --%>
		<style>
			#filechooser {
                /* color: #555; */
                text-decoration: underline;
                cursor: pointer; /* "hand" cursor */
            }
		</style>
		<p align="center" style="line-height: 1.5">
			<spring:message code="pivotTableJs.text.dropCsv"/>&nbsp;<spring:message code="pivotTableJs.text.or"/>&nbsp;
			<label id="filechooser">
				<spring:message code="pivotTableJs.text.clickToChoose"/>
				<input id="csv" type="file" style="display:none"/>
			</label>
		</p>
		<script type="text/javascript">
			var parseAndPivot = function (f) {
				$("#${outputDivId}").html("<p align='center' style='color:grey;'>(${processingText}...)</p>");
				Papa.parse(f, csvConfig);
			};

			$("#csv").bind("change", function (event) {
				parseAndPivot(event.target.files[0]);
			});

			var dragging = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				evt.originalEvent.dataTransfer.dropEffect = 'copy';
				$("body").removeClass("whiteborder").addClass("greyborder");
			};

			var endDrag = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				evt.originalEvent.dataTransfer.dropEffect = 'copy';
				$("body").removeClass("greyborder").addClass("whiteborder");
			};

			var dropped = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				$("body").removeClass("greyborder").addClass("whiteborder");
				parseAndPivot(evt.originalEvent.dataTransfer.files[0]);
			};

			$("html")
					.on("dragover", dragging)
					.on("dragend", endDrag)
					.on("dragexit", endDrag)
					.on("dragleave", endDrag)
					.on("drop", dropped);

		</script>
	</c:when>
	<c:when test="${reportType == 'PivotTableJsCsvServer'}">
		<script type="text/javascript">
			//http://nicolas.kruchten.com/pivottable/examples/mps_csv.html
			var dataFile = '${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(dataFileName)}';
				Papa.parse(dataFile, csvConfig);
		</script>
	</c:when>
</c:choose>

<div id="div-${outputDivId}" style="display:none;">
	<form id="form-${outputDivId}" class="form-horizontal" role="form">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
		<input type="hidden" name="reportId" value="${report.reportId}">
		<input type="hidden" id="config" name="config" value="">
        <div class="form-group">
			<label class="control-label col-md-4" for="name">
				<spring:message code="page.text.name"/>
			</label>
			<div class="col-md-8">
				<input type="text" id="name" name="name" maxlength="50" class="form-control"/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-md-4" for="description">
				<spring:message code="page.text.description"/>
			</label>
			<div class="col-md-8">
				<textarea id="description" name="description" class="form-control" rows="2" maxlength="200"></textarea>
			</div>
		</div>
		<c:if test="${exclusiveAccess}">
			<div class="form-group">
				<label class="control-label col-md-4" for="overwrite">
					<spring:message code="reports.text.overwrite"/>
				</label>
				<div class="col-md-8">
					<div class="checkbox">
						<label>
							<input type="checkbox" name="overwrite" id="overwrite" checked>
						</label>
					</div>
				</div>
			</div>
		</c:if>
		<div class="form-group">
			<label class="control-label col-md-4" for="saveSelectedParameters">
				<spring:message code="dialog.label.saveSelectedParameters"/>
			</label>
			<div class="col-md-8">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="saveSelectedParameters" id="saveSelectedParameters">
					</label>
				</div>
			</div>
		</div>
		<c:if test="${!report.reportType.pivotTableJs}">
			<div class="form-group">
				<label class="control-label col-md-4" for="savePivotTableOnly">
					<spring:message code="dialog.label.savePivotTableOnly"/>
				</label>
				<div class="col-md-8">
					<div class="checkbox">
						<label>
							<input type="checkbox" name="savePivotTableOnly" id="savePivotTableOnly">
						</label>
					</div>
				</div>
			</div>
		</c:if>
	</form>
</div>

<script>
	$("#save-${outputDivId}").on("click", function () {
		//https://stackoverflow.com/questions/42620131/save-the-pivottable-fields-and-filters-in-database
		//https://pivottable.js.org/examples/onrefresh.html
		//https://pivottable.js.org/examples/save_restore.html
		//https://github.com/nicolaskruchten/pivottable/wiki/Parameters

		var config = $("#${outputDivId}").data("pivotUIOptions");
		var config_copy = JSON.parse(JSON.stringify(config));
		//delete some values which will not serialize to JSON
		delete config_copy["aggregators"];
		delete config_copy["renderers"];
		//delete some bulky default values
		delete config_copy["rendererOptions"];
		delete config_copy["localeStrings"];

		$("#config").val(JSON.stringify(config_copy));

		var dialog = bootbox.confirm({
			title: "${saveReportText}",
			message: $("#div-${outputDivId}").html(),
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
					//https://github.com/makeusabrew/bootbox/issues/572
					var form = dialog.find('#form-${outputDivId}');
					var data = form.serialize();
					var reportParameters = $('#parametersForm').serialize();
					if (reportParameters) {
						data = data + '&' + reportParameters;
					}
//					console.log("params", $('#parametersForm').serialize());
//					$.extend(data, $('#parametersForm').serialize());

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/savePivotTableJs',
						dataType: 'json',
						data: data,
						success: function (response)
						{
							if (response.success) {
								if (!${exclusiveAccess} ||
										(${exclusiveAccess} && !dialog.find('#overwrite').is(':checked'))) {
									var newReportId = response.data;
									var newUrl = "${pageContext.request.contextPath}/selectReportParameters?reportId=" + newReportId;
									$("#link-${outputDivId}").attr("href", newUrl);
									$("#link-${outputDivId}").show();
								}
								$.notify("${reportSavedText}", "success");
							} else {
								$.notify(response.errorMessage, "error");
							}
						},
						error: function (xhr, status, error) {
							bootbox.alert({
								title: '${errorOccurredText}',
								message: xhr.responseText
							});
						}
					});
				} //end if result
			} //end callback
		}); //end bootbox confirm

		//https://github.com/makeusabrew/bootbox/issues/411
		//https://blog.shinychang.net/2014/06/05/Input%20autofocus%20in%20the%20bootbox%20dialog%20with%20buttons/
		dialog.on("shown.bs.modal", function () {
			dialog.attr("id", "dialog-${outputDivId}");
			dialog.find('#name').focus();
		});
	});

	//https://stackoverflow.com/questions/26328539/bootbox-make-the-default-button-work-with-the-enter-key
	$(document).on("submit", "#dialog-${outputDivId} form", function (e) {
		e.preventDefault();
		$("#dialog-${outputDivId} .btn-primary").click();
	});

	$("#delete-${outputDivId}").on("click", function () {
		var reportName = '${encode:forJavaScript(report.name)}';
		var reportId = ${report.reportId};

		bootbox.confirm({
			message: "${deleteRecordText}: <b>" + reportName + "</b>",
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
						url: "${pageContext.request.contextPath}/deletePivotTableJs",
						data: {id: reportId},
						success: function (response) {
							var nonDeletedRecords = response.data;
							if (response.success) {
								$.notify("${reportDeletedText}", "success");
							} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
								$.notify("${cannotDeleteReportText}", "error");
							} else {
								$.notify(response.errorMessage, "error");
							}
						},
						error: function (xhr, status, error) {
							bootbox.alert({
								title: '${errorOccurredText}',
								message: xhr.responseText
							});
						}
					});
				} //end if result
			} //end callback
		}); //end bootbox confirm
	});

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});

	$(document).ajaxStart(function () {
		$('#spinner').show();
	}).ajaxStop(function () {
		$('#spinner').hide();
	});
</script>

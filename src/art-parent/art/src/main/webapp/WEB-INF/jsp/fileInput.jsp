<%-- 
    Document   : fileInput
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div class="fileinput fileinput-new" data-provides="fileinput"
	 id="div-${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	 data-max-size="${maxFileSizeJasnyString}">
	<span class="btn btn-default btn-file">
		<span class="fileinput-new">
			<spring:message code="reports.text.selectFile"/>
		</span>
		<span class="fileinput-exists">
			<spring:message code="reports.text.change"/>
		</span>
		<input type="file"
			   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
			   accept="${encode:forHtmlAttribute(reportParam.parameter.fileAccept)}"
			   ${reportParam.parameter.multipleFiles ? "multiple" : ""}>
	</span>
	<span class="fileinput-filename"></span>
	<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
</div>

<script type="text/javascript">
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').on("max_size.bs.fileinput", function (e) {
		bootbox.alert("File too large. Maximum allowed is ${maxFileSizeJasnyString} MB.");
	});
</script>
<%-- 
    Document   : showTemplateReportResult
    Created on : 30-Oct-2014, 15:49:41
    Author     : Timothy Anyona

Display result (link to file) e.g. with jasper report, jxls report, chart pdf or png report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<p>
<div align="center">
	<table border="0" width="90%">"
		<tr>
			<td colspan="2" class="data" align="center" >"
				<a type="application/octet-stream" href="../export/${fileName}">${fileName}</a>
			</td>
		</tr>
	</table>
</div>
</p>

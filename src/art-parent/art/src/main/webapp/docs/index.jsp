<%-- 
    Document   : index
    Created on : 22-Oct-2013, 07:04:57
    Author     : Timothy Anyona

Page to provide access to application documentation
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericPage title="ART - Documentation">
	<jsp:body>
		<h2>ART Documentation</h2>
		<table class="table table-bordered table-striped table-hover">
			<tbody>
				<tr>
					<td>Features</td>
					<td><a type="application/octet-stream" href="Features.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Features.htm" target="_blank">html</a></td>
				</tr>
				<tr>
					<td>Installing</td>
					<td><a type="application/octet-stream" href="Installing.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Installing.htm" target="_blank">html</a></td>
				</tr>
				<tr>
					<td>Upgrading</td>
					<td><a type="application/octet-stream" href="Upgrading.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Upgrading.htm" target="_blank">html</a></td>
				</tr>
				<tr>
					<td>Tips</td>
					<td><a type="application/octet-stream" href="Tips.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Tips.htm" target="_blank">html</a></td>
				</tr>
				<tr>
					<td>Libraries</td>
					<td><a type="application/octet-stream" href="Libraries.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Libraries.htm" target="_blank">html</a></td>
				</tr>
				<tr>
					<td>Manual</td>
					<td><a type="application/octet-stream" href="Manual.pdf" target="_blank">pdf</a></td>
					<td><a type="application/octet-stream" href="Manual.htm" target="_blank">html</a></td>
				</tr>
			</tbody>
		</table>
	</jsp:body>
</t:genericPage>

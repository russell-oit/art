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
		<table class="table table-bordered table-striped">
			<tbody>
				<tr>
					<td>
						Features
						&nbsp; <a type="application/octet-stream" href="features.pdf" target="_blank">pdf</a>
						&nbsp; <a href="features.htm" target="_blank">html</a>
					</td>
				</tr>
				<tr>
					<td>
						Installing
						&nbsp; <a type="application/octet-stream" href="installing.pdf" target="_blank">pdf</a>
						&nbsp; <a href="installing.htm" target="_blank">html</a>
					</td>
				</tr>
				<tr>
					<td>
						Upgrading
						&nbsp; <a type="application/octet-stream" href="upgrading.pdf" target="_blank">pdf</a>
						&nbsp; <a href="upgrading.htm" target="_blank">html</a>
					</td>
				</tr>
				<tr>
					<td>
						Tips
						&nbsp; <a type="application/octet-stream" href="tips.pdf" target="_blank">pdf</a>
						&nbsp; <a href="tips.htm" target="_blank">html</a>
					</td>
				</tr>
				<tr>
					<td>
						Libraries
						&nbsp; <a type="application/octet-stream" href="libraries.pdf" target="_blank">pdf</a>
						&nbsp; <a href="libraries.htm" target="_blank">html</a>
					</td>
				</tr>
				<tr>
					<td>
						Manual
						&nbsp; <a type="application/octet-stream" href="manual.pdf" target="_blank">pdf</a>
						&nbsp; <a href="manual.htm" target="_blank">html</a>
					</td>
				</tr>
			</tbody>
		</table>
	</jsp:body>
</t:genericPage>

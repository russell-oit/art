<%-- 
    Document   : help
    Created on : 19-Sep-2013, 15:33:04
    Author     : Timothy Anyona

Page to provide access to application documentation
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericPage title="ART - Help">
	<jsp:body>
		<h2>ART Help</h2>
		<ul>
			<li>
				<a type="application/octet-stream" href="help/Features.pdf" target="_blank">
					Features
				</a>
			</li>
			<li>
				<a type="application/octet-stream" href="help/Installing.pdf" target="_blank">
					Installing
				</a>
			</li>
			<li>
				<a type="application/octet-stream" href="help/Upgrading.pdf" target="_blank">
					Upgrading
				</a>
			</li>
			<li>
				<a type="application/octet-stream" href="help/AdminManual.pdf" target="_blank">
					Admin Manual
				</a>
			</li>
			<li>
				<a type="application/octet-stream" href="help/Tips.pdf" target="_blank">
					Tips
				</a>
			</li>
		</ul>
	</jsp:body>
</t:genericPage>

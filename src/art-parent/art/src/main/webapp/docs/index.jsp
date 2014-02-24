<%-- 
    Document   : index
    Created on : 22-Oct-2013, 07:04:57
    Author     : Timothy Anyona

Page to provide access to application documentation
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericPage title="ART - Documentation">
	<jsp:attribute name="footer">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-6 col-md-offset-3 spacer60">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">Documentation</h4>
					</div>
					<div class="panel-body">
						<table class="table table-bordered table-striped">
							<tbody>
								<tr>
									<td>
										<a href="license.html">License</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="http://sourceforge.net/p/art/wiki/Home/">
											Online documentation
										</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="features.htm">Features</a>
										&nbsp; <a type="application/octet-stream" href="features.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="installing.htm">Installing</a>
										&nbsp; <a type="application/octet-stream" href="installing.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="upgrading.htm">Upgrading</a>
										&nbsp; <a type="application/octet-stream" href="upgrading.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="tips.htm">Tips</a>
										&nbsp; <a type="application/octet-stream" href="tips.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="libraries.htm">Libraries</a>
										&nbsp; <a type="application/octet-stream" href="libraries.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="manual.htm">Manual</a>
										&nbsp; <a type="application/octet-stream" href="manual.pdf">pdf</a>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</jsp:body>
</t:genericPage>

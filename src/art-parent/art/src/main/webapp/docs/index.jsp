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
										<a href="Features.html">Features</a>
										&nbsp; <a type="application/octet-stream" href="Features.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="Installing.html">Installing</a>
										&nbsp; <a type="application/octet-stream" href="Installing.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="Upgrading.html">Upgrading</a>
										&nbsp; <a type="application/octet-stream" href="Upgrading.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="Tips.html">Tips</a>
										&nbsp; <a type="application/octet-stream" href="Tips.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="Libraries.html">Libraries</a>
										&nbsp; <a type="application/octet-stream" href="Libraries.pdf">pdf</a>
									</td>
								</tr>
								<tr>
									<td>
										<a href="Manual.html">Manual</a>
										&nbsp; <a type="application/octet-stream" href="Manual.pdf">pdf</a>
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

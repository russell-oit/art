<%-- 
    Document   : login
    Created on : 18-Sep-2013, 16:56:48
    Author     : Timothy Anyona

Login page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericPage title="ART - Login">
	<jsp:attribute name="metaContent">
		<meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
	</jsp:attribute>

	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
	</jsp:attribute>

	<jsp:body>

		<div class="row">
			<div class="span4 offset4">
				<div class="well">
					<legend>Sign in to WebApp</legend>
					<form method="POST" action="" accept-charset="UTF-8">
						<div class="alert alert-error">
							<a class="close" data-dismiss="alert" href="#">x</a>Incorrect Username or Password!
						</div>
						<input class="span3" placeholder="Username" type="text" name="username">
						<input class="span3" placeholder="Password" type="password" name="password">
						<label class="checkbox">
							<input type="checkbox" name="remember" value="1"> Remember Me
						</label>
						<button class="btn-info btn" type="submit">Login</button>
					</form>
				</div>
			</div>
		</div>
	</jsp:body>
</t:genericPage>

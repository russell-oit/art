<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART</title>
    </head>
    <body>
		<%
			java.util.ResourceBundle eMessages = java.util.ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());

			String msg = (String) request.getAttribute("errorMessage");

			if (msg == null) {
				msg = request.getParameter("MSG");
			}

		%>

		<table align="center">
			<tr>
				<td colspan="2" class="data" align="center"> <span style="color:red"><b> <%=eMessages.getString("error")%> </b></span>
				</td>
			</tr>
			<tr>
				<td class="attr"> <%=eMessages.getString("message")%>
				</td>
				<td class="data"> <%=msg%>
				</td>
			</tr>
		</table>

	</body>
</html>



<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import=" art.servlets.ArtDBCP;" %>
<%
  /* Invalidate the current session to allow a new login */
  session.invalidate();
  java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>
<!DOCTYPE html>
<html>
    <head>
        <title>ART - Login</title>
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/art.css">
    </head>
    <body onload="javascript:document.getElementById('username').focus();">
        <table class="art login">
             <tr class="centerMiddle">
                <td class="loginTitle" colSpan="2" align="left">
					<img src="${pageContext.request.contextPath}/images/art-64px.jpg"
						 alt="ART" height="70">
				</td>
            </tr>
            <tr class="centerMiddle">
                <td colSpan="2">
					<img src="${pageContext.request.contextPath}/images/users-64px.jpg"
						 alt="" border="0" width="64" height="64"
						 style="vertical-align: middle;">
                    &nbsp;
					<span style="font-size:180%"><b>ART</b>
						<c:if test="${(pageScope._mobile == true)}">
							<i>mobile</i>
						</c:if>
					</span>
				</td>
			</tr>
            <form name="login" method="post" action="<%= request.getContextPath() %>/user/mshowGroups.jsp">
                <input type="hidden" name="_mobile" value="true">
                <c:if test="${(!empty requestScope.message)}">
                    <tr>
                        <td colspan="2" align="center">
                            <span style="color:red">
                                ${requestScope.message}
                            </span>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td class="loginLabel">
						<label for="username"><%=messages.getString("username")%></label>
					</td>
                    <td class="loginField">
                        <input name="username" id="username" maxLength="30" size="25">
                    </td>
                </tr>
                <tr>
                    <td class="loginLabel">
						<label for="password"><%=messages.getString("password")%></label>
					</td>
                    <td class="loginField">
                        <input name="password" id="password" type="password" maxLength="40" size="25">
                    </td>
                </tr>
                <tr>
                    <td class="centerMiddle" colspan="2">
						<input type="submit" class="buttonup"
							   onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" 
                               style="width:100px;" value="<%=messages.getString("login")%>">
					</td>
                </tr>
            </form>
        </table>
    </body>
</html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ include file ="renewSession.jsp" %>

<!DOCTYPE html>
<html>
    <head>
        <title>ART - Login</title>
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
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
						 alt="" border="0" width="64" height="64" style="vertical-align: middle">
                    &nbsp;
					<span style="font-size:180%"><b>ART</b>
						<c:if test="${(pageScope._mobile == true)}">
							<i>mobile</i>
						</c:if>
					</span>
				</td>
			</tr>

			<form name="login" method="post" action="<%= pageContext.getAttribute("nextPage")%>">

                <c:if test="${(!empty requestScope.message) && (pageScope._login != true)}">
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

		<%@ include file="user/footer.jsp" %>

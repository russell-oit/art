<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import=" art.servlets.ArtDBCP;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ include file ="renewSession.jsp" %>
<html>
    <head>
        <title>ART - LDAP Login</title>
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
    </head>
    <body onload="javascript:document.getElementById('username').focus();">
        <table cellSpacing="1" cellPadding="5" width="300" height="300" border="0" class="art" valign="middle" align="center">
            <tr vAlign="center" align="middle">
                <td class="title" colSpan="2" align="left"><img height="70" src="${pageContext.request.contextPath}/images/art-64px.jpg" alt="ART"></td>
            </tr>
            <tr vAlign="center" align="middle">
                <td colSpan=2> <img height="64" src="${pageContext.request.contextPath}/images/users-64px.jpg" width="64" align="absMiddle" border="0" alt="">
                    &nbsp;<span style="font-size:180%"><b>ART</b><c:if test="${(pageScope._mobile == true)}"><i>mobile</i></c:if></span> </td>
            </tr>
            <form name="login" method="post" action="${pageContext.request.contextPath}/execLDAPLogin.jsp">
                <input type="hidden" name="nextPage" value="${pageScope.nextPage}">
                <c:if test="${( !empty requestScope.message) && (pageScope._login != true) }">
                    <tr>
                        <td colspan="2" align="center">
                            <span style="color:red">
                                ${requestScope.message}
                            </span>
                        </td>
                    </tr>
                </c:if>

                <tr>
                    <td vAlign="center" align="right" width="50%"><%=messages.getString("username")%></td>
                    <td vAlign="center" align="left" width="50%">
                        <input id="username" maxLength="15" size="16" name="ldapusername">
                    </td>
                </tr>
                <tr>
                    <td vAlign="center" align="right" width="50%"><%=messages.getString("password")%></td>
                    <td vAlign="center" align="left" width="50%">
                        <input id="password" type="password" maxLength="40" size="16" name="ldappassword">
                    </td>
                </tr>
                <tr>
                    <td vAlign="center" align="middle" colspan="2">
                        <input type="submit" class="buttonup" onMouseOver="btndn(this);" onMouseOut="btnup(this);" style="width:100px;"
                               value="<%=messages.getString("login")%>"> </td>
                </tr>
            </form>
        </table>
        <p align="right">
            <span style="font-size:75%"><a href="${pageContext.request.contextPath}/login.jsp">Internal Login</a></span>
        </p>

        <%@ include file ="user/footer.jsp" %>

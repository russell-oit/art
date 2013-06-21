<%
// session.invalidate(); not needed anymore since all xxLogin.jsp pages call renewSession.jsp which invalidates the session
//_login=true prevents to show the "session expired" warning
response.sendRedirect(request.getContextPath()+"/user/mshowGroups.jsp?_mobile=true&_login=true"); %>
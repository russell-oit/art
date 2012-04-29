<% // session.invalidate(); not needed anymore since xxLoging.jsp call renewSession.jsp who invalidate the session
//_login=true prevents to show the "session expired" warning
response.sendRedirect(request.getContextPath()+"/user/mshowGroups.jsp?_mobile=true&_login=true"); %>
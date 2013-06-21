<%
  /* Invalidate the current session to allow a new login but cache the nextPage
     to go to after login if successful */
  pageContext.setAttribute("_mobile", false); 
  pageContext.setAttribute("_login", false); 
  if (session.getAttribute("nextPage") != null) {
	  //set next page to go to after login details entered
     String nextPage = response.encodeRedirectURL((String) session.getAttribute("nextPage")); 
	 if ( nextPage.contains("_mobile=true") ) {
	    pageContext.setAttribute("_mobile", true);
	 }
	 int idx = nextPage.indexOf("_login=true");
	 if ( idx > 0 ) {
	    pageContext.setAttribute("_login", true); // it is a request to log in, do not show "session expire message"
		//remove X_login=true where X could be a & or a ? char
		nextPage = nextPage.substring(0,idx-1);
	 }
     pageContext.setAttribute("nextPage",nextPage);
  } else {
	  //default to start page
     pageContext.setAttribute("nextPage",request.getContextPath() + "/user/showGroups.jsp");
  }
  session.invalidate();
  java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

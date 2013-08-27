<%@ page import="java.util.ResourceBundle, art.utils.ArtHelper" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<jsp:useBean id="dashboard" scope="request" class="art.utils.Dashboard" />

<%
//support display of results in the showparams page using jquery ajax
java.util.ResourceBundle sdMessages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

boolean isInline=false;
if(request.getParameter("_isInline")!=null){
	isInline=true;
}

if(!isInline){ 	%>	
	<%@ include file ="header.jsp" %>
<% }
%>


<%
int queryId=0;
String queryIdString=request.getParameter("queryId");
String objectId=request.getParameter("objectId");
if(queryIdString!=null){
	queryId=Integer.parseInt(queryIdString);
} else if(objectId!=null){
	queryId=Integer.parseInt(objectId);
}
  
   dashboard.load(queryId);
   ArtHelper.log(ue.getUsername(), "query", request.getRemoteAddr(), queryId, 0, 0, "dashboard");
   
   String contextPath=request.getContextPath();
   
   String imgMinimize=contextPath + "/images/minimize.png";
String imgMaximize=contextPath + "/images/maximize.png";
String imgRefresh=contextPath + "/images/refresh.png";
 %>

 <script type="text/javascript" src="../js/overlib.js"></script>

<b><%=dashboard.getTitle()%></b> <br>
    &nbsp;&nbsp;&nbsp;<%=dashboard.getDescription()%>
<div align="left">
<table class="plain">
 <tr>
<%
    for(int i=0; i<dashboard.getColumnsCount(); i++) {
     %> <td> <%
       for(int j=0; j<dashboard.getPortletsCount(i); j++) {

	  String source = "portlet_"+i+"_"+j+"_"+queryId;
	  String divid  = "div_"+i+"_"+j+"_"+queryId;
	  String cssclass  = "portlet"+dashboard.getColumnSize(i);
          String refresh = dashboard.getPortletRefresh(i,j);
	  String baseUrl = dashboard.getPortletLink(i,j, request); 
	  String title = dashboard.getPortletTitle(i,j);
	  boolean onload = dashboard.getPortletOnLoad(i,j);
	  // add icons to portlet title
	  if (!onload) {
	    title = title + "  <img src='" + contextPath + "/images/onLoadFalse.gif' title='"+sdMessages.getString("portletOnLoadFalse")+"'/>";
	  }
	  if (refresh!=null) {
	  title = title + " <img src='" + contextPath + "/images/clock_mini.gif' title='"+sdMessages.getString("portletAutoRefresh")+" "+refresh+" "+sdMessages.getString("seconds")+"'/> <small>"+refresh+"s</small>";	    
	  }
	  
	  
	  %>
           <div id="<%=divid%>">
	  
	  <%
          if (refresh==null) {
		  //postfunction doesn't run as expected for a portlet that has a refresh period. need to decrement work count in ajaxtags.js
		  //similar to another issue on ajaxtags forum. http://sourceforge.net/projects/ajaxtags/forums/forum/809911/topic/3357160
          %>
           <ajax:portlet
             source="<%=source%>"
             baseUrl="<%=baseUrl%>"
             classNamePrefix="<%=cssclass%>"
             title="<%=title%>"
             imageMaximize="<%=imgMaximize%>"
             imageMinimize="<%=imgMinimize%>"
             imageRefresh="<%=imgRefresh%>"             
             executeOnLoad= "<%=onload%>"
			 preFunction="artAddWork"	
              />
          <%  } else {
          %>
           <ajax:portlet
             source="<%=source%>"
             baseUrl="<%=baseUrl%>"
             classNamePrefix="<%=cssclass%>"
             title="<%=title%>"
             imageMaximize="<%=imgMaximize%>"
             imageMinimize="<%=imgMinimize%>"
             imageRefresh="<%=imgRefresh%>"             
             refreshPeriod="<%=refresh%>"
             executeOnLoad="<%=onload%>"
			 preFunction="artAddWork"				 
              />
          <%  }  %>
           </div>
           <br>
	  <%
        }
     %> </td> <%
    }
%>
 </tr>
</table>
</div> 

<%
if(!isInline){ 	%>	
	<%@ include file ="footer.jsp" %>
<% }
%>


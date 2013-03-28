<%@ page import="art.utils.*,art.servlets.ArtDBCP,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("QUERYACTION");

ArtQuery aq=new ArtQuery();

String[] queryIds = request.getParameterValues("QUERY_ID");

if (action.equals("DELETE")){
    for(String value : queryIds) {
        aq.setQueryId(Integer.parseInt(value));
        aq.delete();
    }	
} else if (action.equals("COPY")){
    aq.setQueryId(Integer.parseInt(queryIds[0]));
	aq.copy(request.getParameter("COPY_QUERY_NAME"));
} else if (action.equals("RENAME")){
    aq.setQueryId(Integer.parseInt(queryIds[0]));
	aq.rename(request.getParameter("NEW_QUERY_NAME"));
} else if (action.equals("MOVE")){
    int newGroupId=Integer.parseInt(request.getParameter("NEW_GROUP_ID"));
	aq.move(queryIds,newGroupId);
}

response.sendRedirect("manageQueries.jsp");
%>




<%@ page import="art.utils.*,art.servlets.ArtDBCP,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("QUERYACTION");

ArtQuery aq=new ArtQuery();

int id;
String[] queryIds = request.getParameterValues("QUERY_ID");
List<String> ids=Arrays.asList(queryIds);

if (action.equals("DELETE")){
    Iterator<String> it=ids.iterator();
    while(it.hasNext()){
        id=Integer.parseInt(it.next());
        aq.setQueryId(id);
        aq.delete();
    }	
} else if (action.equals("COPY")){
    id=Integer.parseInt(ids.get(0));
    aq.setQueryId(id);
	aq.copy(request.getParameter("NEW_QUERY_NAME"));
}

response.sendRedirect("manageQueries.jsp");
%>




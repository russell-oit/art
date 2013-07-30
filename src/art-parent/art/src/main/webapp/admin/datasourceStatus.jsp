<%@ page import="art.utils.*,java.util.*,art.servlets.ArtDBCP,art.dbcp.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("action");

if (action!=null && request.getSession().getAttribute("AdminSession")!=null) {
	out.println("<b>ART Datasource Status</b><br>");
	if (action.equals("REFRESH")){
		out.println("Action: <b>Refresh</b>");
		ArtDBCP.refreshConnections() ;
	} else if (action.equals("FORCEREFRESH") ){
		out.println("Action: <b>Force Refresh</b>");
		ArtDBCP.forceRefreshConnections();
	}
	
	//display status
	Map<Integer, DataSource> dataSources=ArtDBCP.getDataSources();
	for (Integer key : dataSources.keySet()) {			
		DataSource ds = dataSources.get(key);			
		if (ds != null) {
			out.println("<hr>");
			out.println("Name: <b>"+ ds.getName()+"</b> , ID: <b>"+key+"</b>");
			
			List<EnhancedConnection> connectionPool=ds.getConnectionPool();
			out.println("<pre>");			
			out.println(" Connection Pool Size  = " + connectionPool.size());
			
			StringBuilder sb = new StringBuilder(512);
			SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for(EnhancedConnection ec : connectionPool) {
				Date lastUsedTime = new Date(ec.getLastUsedTime());				
				sb.append(ec.getInUse() + " (" + dateFormatter.format(lastUsedTime) + ") , ");				
			}
			out.println("   In use          = " + sb.toString());
			out.println(" Total Connections Ever Requested = " + ds.getTotalConnections());
			out.println("</pre>");
		}
	}
}

%>


<%@ include file ="/user/footer.jsp" %>
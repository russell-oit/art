<%@ page import="art.utils.*,java.util.*,art.servlets.ArtDBCP,art.dbcp.*,java.text.SimpleDateFormat" %>
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
	LinkedHashMap<Integer, DataSource> dataSources=(LinkedHashMap)ArtDBCP.getDataSources();
	for (Integer key : dataSources.keySet()) {			
		DataSource ds = dataSources.get(key);			
		if (ds != null) {
			out.println("<hr>");
			out.println("Name: <b>"+ ds.getName()+"</b> , ID: <b>"+key+"</b>");
			
			List connectionPool=ds.getConnectionPool();
			out.println("<pre>");			
			out.println(" Connection Pool Size  = " + connectionPool.size());
			
			StringBuilder sb = new StringBuilder(512);
			SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for(int i=0; i<connectionPool.size() ; i++) {
				EnanchedConnection ec = (EnanchedConnection) connectionPool.get(i) ;								
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
<%@ page import="java.sql.*,art.utils.*,art.servlets.ArtDBCP,java.util.ResourceBundle,java.util.Calendar,java.text.SimpleDateFormat" %>
<%@ page import="org.quartz.*,org.apache.commons.lang.StringUtils" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>


<%

	int queryId;
	String queryName;
	String mdx;
	String queryDescription;

	queryId = Integer.parseInt(request.getParameter("pivotQueryId"));
	queryName = request.getParameter("newPivotName");
	queryDescription = request.getParameter("newPivotDescription");
	mdx = (String) session.getAttribute("mdx" + queryId);

	boolean overwriting = false;
	if (request.getParameter("overwrite") != null) {
		overwriting = true;
	}
	boolean deleting = false;
	if (request.getParameter("delete") != null && !overwriting) {
		deleting = true;
	}


	Connection conn = ArtDBCP.getConnection();
	if (conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
</jsp:forward>
<%		}

//check if any modification made
	if ((mdx == null || mdx.length() == 0) && !deleting) {
		conn.close();
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MSG" value="Nothing to save"/>
</jsp:forward>
<%
	}

	ArtQuery aq = new ArtQuery();

	boolean queryExists;
	queryExists = aq.create(conn, queryId);

	if (!queryExists) {
		conn.close();
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MSG" value="Query not found"/>
</jsp:forward>
<%
	}

	int queryType = aq.getQueryType();

	if (queryType != 112 && queryType != 113 && queryType != 114) {
		conn.close();
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MSG" value="Invalid query type"/>
</jsp:forward>
<%
	}

	String msg = messages.getString("pivotTableSaved");
	if (overwriting) {
		//overwrite query source with current mdx
		//query details loaded. update query
		aq.setText(mdx);
		if (StringUtils.length(queryDescription) > 0) {
			//update description
			aq.setDescription(queryDescription);
		}
		aq.update(conn);
	} else if (deleting) {
		//delete query
		aq.delete();
		msg = messages.getString("pivotTableDeleted");
	} else {
		//create new query based on current query
		ArtQuery newQuery = new ArtQuery();

		newQuery.setGroupId(aq.getGroupId());
		newQuery.setDatabaseId(aq.getDatabaseId());
		newQuery.setQueryType(aq.getQueryType());
		newQuery.setShortDescription("");
		newQuery.setContactPerson(ue.getUsername());
		newQuery.setUsesRules(aq.getUsesRules());
		newQuery.setStatus(aq.getStatus());
		newQuery.setTemplate(aq.getTemplate());

		if (queryDescription == null || queryDescription.length() == 0) {
			//no description provided. use original query description
			queryDescription = aq.getDescription();
		}
		newQuery.setDescription(queryDescription);

		if (queryName == null || queryName.trim().length() == 0) {
			//no name provided for the new query. create a default name
			queryName = aq.getName() + "-2";
		}
		newQuery.setName(queryName);

		//save current view's mdx
		newQuery.setText(mdx);

		//insert query
		newQuery.insert(conn);

		//give this user direct access to the view he has just created. so that he can update and overwrite it if desired
		newQuery.grantAccess(conn, ue.getUsername());
	}

	conn.close();

%>

<div style="text-align:center">
	<p> <%=msg%>
    </p>
</div>

<%@ include file ="footer.jsp" %>


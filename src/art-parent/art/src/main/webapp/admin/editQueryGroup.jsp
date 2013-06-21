<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String groupIdString=request.getParameter("GROUP_ID");
int groupId=-1;
if(groupIdString!=null){
	groupId=Integer.parseInt(groupIdString);
}

QueryGroup group=new QueryGroup();

if (action.equals("DELETE")){
	//check if queries exist in this group
	Map<Integer, ArtQuery> queries=group.getLinkedQueries(groupId);
	if(queries.size()>0){
		out.println("<pre>Error: There are queries in the group you want to delete.");
		out.println("       Delete the following queries or change their group");
		out.println("       in order to be able to delete this group: ");
		out.println();

		for (Map.Entry<Integer, ArtQuery> entry : queries.entrySet()) {
			ArtQuery aq=entry.getValue();
			out.println("Query ID: " + aq.getQueryId() + " , Name: " + aq.getName() + " , Group ID: " + aq.getGroupId());
		}
		out.println("</pre>");
		%>
		<%@ include file="/user/footer.jsp" %>
		<%
		return;
	} else {
		//no queries in this group. delete group
		group.delete(groupId);
		response.sendRedirect("manageQueryGroups.jsp");
		return;
	}
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	group.load(groupId);
}
%>


<form name="editQueryGroup" method="post" action="execEditQueryGroup.jsp">
	<input type="hidden" name="ACTION" value="<%=action%>">

	<table align="center">
		<tr>
			<td class="title" colspan="2">Define Query Group</td>
		</tr>
		
	    <tr>
			<td class="data"> ID </td>
			<%
			String inputType="hidden";
			if(modify){
				inputType="text";
			}
			%>
			<td class="data"> <input type="<%=inputType%>" name="GROUP_ID" value="<%=group.getGroupId()%>" size="25" readonly> </td>

		</tr>

		<tr><td class="data"> Name </td>
			<td class="data"> <input type="text" name="GROUP_NAME" value="<%=group.getName()%>" size="25" maxlength="25"> </td>
		</tr>

		<tr><td class="data"> Description </td>
			<td class="data"> <input type="text" name="GROUP_DESCRIPTION" value="<%=group.getDescription()%>" size="40" maxlength="50"> </td>
		</tr>

		<tr>
			<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
		</tr>
    </table>
</form>


<%@ include file="/user/footer.jsp" %>
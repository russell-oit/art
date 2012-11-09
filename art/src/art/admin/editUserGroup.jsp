<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String groupIdString=request.getParameter("GROUP_ID");
int groupId=-1;
if(groupIdString!=null){
	groupId=Integer.parseInt(groupIdString);
}

UserGroup ug=new UserGroup();

if (action.equals("DELETE")){
	ug.delete(groupId);
	response.sendRedirect("manageUserGroups.jsp");
	return;
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	ug.load(groupId);
}

Iterator it;
%>


<form name="editUserGroup" method="post" action="execEditUserGroup.jsp">
	<input type="hidden" name="ACTION" value="<%=action%>">

	<table align="center">
		<tr>
			<td class="title" colspan="2">Define User Group</td>
		</tr>
		
	    <tr>
			<td class="data"> ID </td>
			<%
			String inputType="hidden";
			if(modify){
				inputType="text";
			}
			%>
			<td class="data"> <input type="<%=inputType%>" name="GROUP_ID" value="<%=ug.getGroupId()%>" size="25" readonly> </td>
		</tr>

		<tr><td class="data"> Name </td>
			<td class="data"> <input type="text" name="GROUP_NAME" value="<%=ug.getName()%>" size="25" maxlength="25"> </td>
		</tr>

		<tr><td class="data"> Description </td>
			<td class="data"> <input type="text" name="GROUP_DESCRIPTION" value="<%=ug.getDescription()%>" size="40" maxlength="50"> </td>
		</tr>

		<tr>
            <td class="data"> Default Object Group </td>
            <td class="data">
                <select name="DEFAULT_OBJECT_GROUP" size="1">
					<option value="-1">No Default</option>
                    <%
					int defaultObjectGroup=ug.getDefaultObjectGroup();

					Integer objectGroupId;
					String selected;

					ObjectGroup og=new ObjectGroup();
					Map groups=og.getAllObjectGroupNames();
					it = groups.entrySet().iterator();

					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						objectGroupId=(Integer)entry.getValue();
						if(objectGroupId==defaultObjectGroup){
							selected="selected";
						} else {
							selected="";
						}

						if(objectGroupId!=0){
							%>
							<option value="<%=entry.getValue()%>" <%=selected%> ><%=entry.getKey()%></option>
							<%
						}
					}
					%>
                </select>
            </td>
        </tr>

		<tr><td colspan="2" >&nbsp;</td></tr>
		<tr><td class="data" colspan="2" >  Group Members </td></tr>
		<tr>
            <td class="data"> Select users to add<br> or remove from the group </td>
            <td class="data">
                <select name="USERS" size="10" multiple>
                    <%
					UserEntity ue=new UserEntity();
					List<String> usernames=ue.getAllUsernames();
					it = usernames.iterator();
					String username;

					while(it.hasNext()) {
						username=(String)it.next();
						%>
						<option value="<%=username%>"><%=username%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		<tr><td colspan="2" class="data"> Action:
			<select name="USERS_ACTION">
				<option value="ADD">ADD USERS AND SAVE</option>
				<option value="REMOVE">REMOVE USERS AND SAVE</option>
			</select>
		</td></tr>

		<tr><td colspan="2" class="data"> Current Members </td></tr>
		<tr>
            <td colspan="2" class="data2">
				<%
				Map map=ug.getUserGroupMembers();
				it = map.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					%>
					<%=entry.getValue()%> <br>
					<%
				}
				%>
            </td>
        </tr>

		<tr><td colspan="2">&nbsp;</td></tr>

		<tr>
			<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
		</tr>
    </table>
</form>


<%@ include file="footer.html" %>
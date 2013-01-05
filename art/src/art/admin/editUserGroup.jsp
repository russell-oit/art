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

String help;
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
            <td class="data"> Default Query Group </td>
            <td class="data">
                <select name="DEFAULT_QUERY_GROUP" size="1">
					<option value="-1">No Default</option>
                    <%
					int defaultQueryGroup=ug.getDefaultQueryGroup();

					Integer queryGroupId;
					String selected;

					QueryGroup qg=new QueryGroup();
					Map<String, Integer> groups=qg.getAllQueryGroupNames();
					for (Map.Entry<String, Integer> entry : groups.entrySet()) {
						queryGroupId=entry.getValue();
						if(queryGroupId==defaultQueryGroup){
							selected="selected";
						} else {
							selected="";
						}

						if(queryGroupId!=0){
							%>
							<option value="<%=entry.getValue()%>" <%=selected%> ><%=entry.getKey()%></option>
							<%
						}
					}
					%>
                </select>
            </td>
        </tr>
		
		<tr><td class="data"> Start Query </td>
			<td class="data">
				<input type="text" name="START_QUERY" value="<%=ug.getStartQuery()%>" size="40" maxlength="500">
				<% help="Query to be displayed on the Start Page. Enter either" +
               "\\nquery id e.g. 1" +
               "\\nquery id and parameters e.g 1&P_param1=value1&P_param2=value2";
                %>

                <input type="button" class="buttonup" onclick="javascript:alert('<%=help%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" />
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
					for(String username : usernames) {
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
				Map<Integer, String> map=ug.getUserGroupMembers();
				for (Map.Entry<Integer, String> entry : map.entrySet()) {
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


<%@ include file="/user/footer.jsp" %>
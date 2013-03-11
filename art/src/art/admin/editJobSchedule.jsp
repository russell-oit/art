<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String scheduleName = request.getParameter("SCHEDULE_NAME");

JobSchedule schedule=new JobSchedule();

if (action.equals("DELETE")){
	schedule.delete(scheduleName);
	response.sendRedirect("manageJobSchedules.jsp");
	return;
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	schedule.load(scheduleName);
}

String help;
%>


<form name="editJobSchedule" method="post" action="execEditJobSchedule.jsp">    	
	<input type="hidden" name="ACTION" value="<%=action%>">
	
	<table align="center">		
		<tr>
			<td class="title" colspan="2">Define Schedule</td>
		</tr>
				
	    <tr>
			<td class="data"> Schedule Name </td> 
			<%
			String edit="";
			if(modify){
				edit="readonly";
			}
			%>
			<td class="data"> <input type="text" name="SCHEDULE_NAME" value="<%=schedule.getScheduleName()%>" size="40" maxlength="50" <%=edit%>> </td>
			
		</tr>

		<tr><td class="data"> Minute </td>
			<td class="data"> <input type="text" name="MINUTE" value="<%=schedule.getMinute()%>" size="25" maxlength="100">
			<input type="button" class="buttonup" onclick="javascript:alert('Leave blank to use a random value between 0-59')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">				
			</td>
		</tr>

		<tr><td class="data"> Hour </td>
			<td class="data"> <input type="text" name="HOUR" value="<%=schedule.getHour()%>" size="25" maxlength="100">
				<%
				help="Leave blank to use a random value between 3-6 or specify a random start time range e.g. \\n 4|7 \\n 4:30|6 \\n 12:45|13:15";
				%>
			<input type="button" class="buttonup" onclick="javascript:alert('<%=help%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">				
			</td>
		</tr>

		<tr><td class="data"> Day (Day of the month) </td>
			<td class="data"> <input type="text" name="DAY" value="<%=schedule.getDay()%>" size="25" maxlength="100"> </td>
		</tr>

		<tr><td class="data"> Month </td>
			<td class="data"> <input type="text" name="MONTH" value="<%=schedule.getMonth()%>" size="25" maxlength="100"> </td>
		</tr>

		<tr><td class="data"> Weekday (Day of the week)</td>
			<td class="data"> <input type="text" name="WEEKDAY" value="<%=schedule.getWeekday()%>" size="25" maxlength="100"> </td>
		</tr>

		<tr>
			<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
		</tr>
    </table>    
</form>

<%@ include file="/user/scheduleHelp.html" %>

<%@ include file="/user/footer.jsp" %>
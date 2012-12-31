<%@ page import="art.utils.*,java.util.*" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<% request.setCharacterEncoding("UTF-8"); %>

<%
String action=request.getParameter("ACTION");

JobSchedule schedule=new JobSchedule();

schedule.setScheduleName(request.getParameter("SCHEDULE_NAME").trim());
schedule.setMinute(request.getParameter("MINUTE").replaceAll(" ", ""));
schedule.setHour(request.getParameter("HOUR").replaceAll(" ", ""));
schedule.setDay(request.getParameter("DAY").replaceAll(" ", ""));
schedule.setMonth(request.getParameter("MONTH").replaceAll(" ", ""));
schedule.setWeekday(request.getParameter("WEEKDAY").replaceAll(" ", ""));

if (action.equals("ADD")){	
	schedule.insert();
} else if (action.equals("MODIFY")){
	schedule.update();
}

response.sendRedirect("manageJobSchedules.jsp");
%>



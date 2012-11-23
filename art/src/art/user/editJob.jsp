<%@ page import="art.utils.*,java.util.*,java.text.*,art.servlets.ArtDBCP" %>
<%@ page import="org.quartz.*,org.apache.commons.lang.StringUtils" %>
<%@ page import="static org.quartz.JobBuilder.*" %>
<%@ page import="static org.quartz.CronScheduleBuilder.*" %>
<%@ page import="static org.quartz.TriggerBuilder.*" %>
<%@ page import="static org.quartz.JobKey.jobKey" %>
<%@ page import="static org.quartz.TriggerKey.triggerKey" %>

<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<jsp:useBean id="job" scope="session" class="art.utils.ArtJob" />
<%@ include file ="header.jsp" %>

<%
int adminLevel = ue.getAdminLevel();
String owner;
owner=request.getParameter("OWNER");

if(owner==null){ %>
	<jsp:setProperty name="job" property="username" value="<%=ue.getUsername()%>" />
	<jsp:setProperty name="job" property="*" />
<% } else if(owner!=null && adminLevel==100){ %>
		<jsp:setProperty name="job" property="username" value="<%=owner%>" />
	<jsp:setProperty name="job" property="*" />
<% } %>


<script type="text/javascript" src="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar_js.jsp"></script>

<script type="text/javascript">
window.onload=function(){
onClickSaveSchedule(document.getElementById("saveSchedule"));
onChangeAllowSharing(document.getElementById("allowSharing"));
}
</script>

<%
String help;

String msg="";

int queryId=0;
//support queryId as well as existing QUERY_ID
String queryIdString = request.getParameter("queryId");
String queryIdString2 = request.getParameter("QUERY_ID");
if (queryIdString != null) {
	queryId = Integer.parseInt(queryIdString);
} else if (queryIdString2 != null) {
	queryId = Integer.parseInt(queryIdString2);
}
if(queryId==0){
	//get query id from job
	queryId=job.getQueryId();
}
ArtQuery aq=new ArtQuery();
aq.create(queryId,false);

String queryName=aq.getName();
int queryType=aq.getQueryType();

//make sure correct from address is displayed, in case of an admin managing another person's job
String email;
email=ue.getEmail();
if(owner!=null && adminLevel==100){
	email=job.getFrom();
}

boolean saveJobSchedule=false;
String jobScheduleName;

if(request.getParameter("saveSchedule")!=null){
	saveJobSchedule=true;
}
jobScheduleName=request.getParameter("scheduleName");
if(jobScheduleName==null){
	jobScheduleName="";
}

//details of user and user group access
String action=request.getParameter("sharedUsersAction");
String[] selectedUsers=request.getParameterValues("sharedUsers");
String[] selectedGroups=request.getParameterValues("sharedUserGroups");

Iterator it;

%>

<c:if test="${param.action == 'save'}">

<%
//explicitly set properties if value on html form is blank. setproperty doesn't call the setter if the value is empty
if (request.getParameter("startDateString").equals("")){
	job.setStartDateString("");
}
if (request.getParameter("endDateString").equals("")){
	job.setEndDateString("");
}
if (request.getParameter("minute").equals("")){
	job.setMinute("");
}
if (request.getParameter("hour").equals("")){
	job.setHour("");
}
if (request.getParameter("day").equals("")){
	job.setDay("");
}
if (request.getParameter("weekday").equals("")){
	job.setWeekday("");
}
if (request.getParameter("month").equals("")){
	job.setMonth("");
}
if (request.getParameter("tos").equals("")){
	job.setTos("");
}
if (request.getParameter("message").equals("")){
	job.setMessage("");
}
if (request.getParameter("from").equals("")){
	job.setFrom("");
}
if (request.getParameter("subject").equals("")){
	job.setSubject("");
}
if (request.getParameter("jobName").equals("")){
	job.setJobName("");
}
if (request.getParameter("cc").equals("")){
	job.setCc("");
}
if (request.getParameter("bcc").equals("")){
	job.setBcc("");
}
%>

  <%

  //create quartz job to be running this job

  //build cron expression for the schedule

    String minute;
	String hour;
	String day;
	String weekday;
	String month;
	String second="0"; //seconds always 0
	String actualHour; //allow hour and minute to be left blank, in which case random values are used
	String actualMinute; //allow hour and minute to be left blank, in which case random values are used

  minute=job.getMinute().replaceAll(" ", ""); // cron fields shouldn't have any spaces in them
  actualMinute=minute;
  if (minute.length()==0){
	//no minute defined. use random value
	minute=String.valueOf(ArtDBCP.getRandomNumber(0, 59));
	}

	hour=job.getHour().replaceAll(" ", "");
	actualHour=hour;
  if (hour.length()==0){
	//no hour defined. use random value
	hour=String.valueOf(ArtDBCP.getRandomNumber(3, 7));
	}

	month=job.getMonth().replaceAll(" ", "");
	if (month.length()==0){
	//no month defined. default to every month
	month="*";
	}

	day=job.getDay().replaceAll(" ", "");
	weekday=job.getWeekday().replaceAll(" ", "");

	//set default day of the month if weekday is defined
	if (day.length()==0 && weekday.length()>=1 && !weekday.equals("?")){
		//weekday defined but day of the month is not. default day to ?
		day="?";
	}

  if (day.length()==0){
	//no day of month defined. default to *
	day="*";
	}

  if (weekday.length()==0){
	//no day of week defined. default to undefined
	weekday="?";
	}

	if (day.equals("?") && weekday.equals("?")){
		//unsupported. only one can be ?
		day="*";
		weekday="?";
	}
	if (day.equals("*") && weekday.equals("*")){
		//unsupported. only one can be defined
		day="*";
		weekday="?";
	}

	//build cron expression.
	//cron format is sec min hr dayofmonth month dayofweek (optionally year)
	String cronString;
	cronString=second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;

	//determine if start date and end date are valid dates
	java.util.Date startDate=null;
	java.util.Date endDate=null;
	boolean startDateValid=false;
	boolean endDateValid=false;
	SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd");
	dateFormatter.setLenient(false);
	java.util.Date nextRunDate=null;

	if (StringUtils.isBlank(job.getStartDateString())){
		//allow blank
		startDateValid=true;
	} else {
	try{
		startDate=dateFormatter.parse(job.getStartDateString());
		startDateValid=true;
	} catch(ParseException e){
		//do nothing
	}
	}

	if (StringUtils.isBlank(job.getEndDateString())){
		//allow blank
		endDateValid=true;
	} else {
	try{
		endDate=dateFormatter.parse(job.getEndDateString());
		endDateValid=true;
	} catch(ParseException e){
		//invalid date.do nothing
	}
	}

	//save job schedule details
	String saveSchedule=request.getParameter("saveSchedule");
	String scheduleName=request.getParameter("scheduleName");
	JobSchedule schedule=new JobSchedule();
	if(saveSchedule!=null){
		//we are saving the schedule
		if(StringUtils.isBlank(scheduleName)){
			msg="Schedule must have a name";
		} else if(schedule.exists(scheduleName)){
			msg="Schedule name exists. Change the name or un-check the save schedule option";
		}
	}

	if(startDateValid==false){
		msg="Invalid start date. Date should be in the format YYYY-MM-DD";
	} else if(endDateValid==false){
		msg="Invalid end date. Date should be in the format YYYY-MM-DD";
	} else if (!CronExpression.isValidExpression(cronString)){
		msg="Invalid job schedule";
	}
		
	if(msg.equals("")){
		//dates and schedule are valid
		//ensure end date is after start date
	
		//set start date and end date. must be today or in the future
		//define calendar with date of today and time of midnight - first millisecond of the day
		java.util.Calendar calToday = java.util.Calendar.getInstance();
		calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
		calToday.set(java.util.Calendar.MINUTE, 0);
		calToday.set(java.util.Calendar.SECOND, 0);
		calToday.set(java.util.Calendar.MILLISECOND, 0);

		java.util.Date now=new java.util.Date();

		if (startDate==null){
			//no start date specified. default to now
			startDate=now;
		} else if (calToday.getTime().after(startDate)){
			//start date in the past. default to now. to avoid job running for apparently missed schedules (in the past)
			startDate=now;
		} 

		//ensure end date is always last millisecond of the day. to enable same date for start and end date	
		if(endDate!=null){
			//set end date to the same day but just before midnight
			java.util.Calendar calEndDate=java.util.Calendar.getInstance();
			calEndDate.setTime(endDate);
			calEndDate.set(java.util.Calendar.HOUR_OF_DAY, 23);
			calEndDate.set(java.util.Calendar.MINUTE, 59);
			calEndDate.set(java.util.Calendar.SECOND, 59);
			calEndDate.set(java.util.Calendar.MILLISECOND, 999);
			endDate=calEndDate.getTime();
		}

		//check if start date after end date
		if(endDate!=null && startDate.after(endDate)){
			msg="Start Date is after End Date. Consider changing the End Date";
		}
	}
	
	if (msg.equals("")){
		//start date and end date are ok
		//confirm if the given schedule will ever run
		//evaluate the next run date for the the job
	CronTrigger tempTrigger = newTrigger()
			.withSchedule(cronSchedule(cronString))
			.startAt(startDate)
			.endAt(endDate)
			.build();	
		
	nextRunDate=tempTrigger.getFireTimeAfter(new java.util.Date());

		if (nextRunDate==null){
			msg="Job will never execute. Please change the schedule details";
		}
	}
	
	if (msg.equals("")){
		//everything is in order. save the job
		
		//set the next run date for the job
		job.setNextRunDate(nextRunDate);

		//save job details to the art database. generates job id for new jobs
		job.setMinute(minute);
		job.setHour(hour);
		job.setDay(day);
		job.setMonth(month);
		job.setWeekday(weekday);

		job.setStartDate(startDate);
		job.setEndDate(endDate);

		job.save(); //for a new job, will generate a new job id

		//grant/revoke access to the job. job needs to have been saved first so that a valid job id is available
		if(StringUtils.equals("Y", job.getAllowSharing())){		
			job.updateUserAccess(action,selectedUsers);
			job.updateUserGroupAccess(action,selectedGroups);
		}

		//create quartz job
		
		//get scheduler instance
		org.quartz.Scheduler scheduler=ArtDBCP.getScheduler();

		if (scheduler!=null){
			int jobId=job.getJobId();

			String jobName="job"+jobId;
			String jobGroup="jobGroup";
			String triggerName="trigger"+jobId;
			String triggerGroup="triggerGroup";

			JobDetail quartzJob = newJob(ArtJob.class)
					.withIdentity(jobKey(jobName,jobGroup))
					.usingJobData("jobid",jobId)
					.build();

			//create trigger that defines the schedule for the job
		CronTrigger trigger= newTrigger()
				.withIdentity(triggerKey(triggerName,triggerGroup))
				.withSchedule(cronSchedule(cronString))
				.startAt(startDate)
				.endAt(endDate)
				.build();
	   
			//delete any existing jobs or triggers with the same id before adding them to the scheduler
			scheduler.deleteJob(jobKey(jobName,jobGroup));
			scheduler.unscheduleJob(triggerKey(triggerName,triggerGroup));

			//add job and trigger to scheduler
			scheduler.scheduleJob(quartzJob, trigger);
		}

		//save schedule details
		if(saveSchedule!=null){
			//we are saving the schedule
			schedule.setMinute(actualMinute);
			schedule.setHour(actualHour);
			schedule.setDay(day);
			schedule.setMonth(month);
			schedule.setWeekday(weekday);

			schedule.setScheduleName(scheduleName);
			schedule.insert();
		}

		%>
<c:remove var="job" scope="session" />
 <%
		//use client side redirect instead of jsp:forward to avoid job being resubmitted if browser refresh is done immediately after saving the job
		response.sendRedirect("myJobs.jsp");
		return;
		}
	
%>


</c:if>

<c:if test="${!empty param.fromShowParams}">
 <% // called from showParams.jsp,
 // need to empty the job object that might have ben cached in the session
 job.reset();
 // and to set the params in the ArtJob
 Map<String,String[]> multiParams  = new HashMap<String,String[]>();
 Map<String,String> inlineParams = new HashMap<String,String>(); 
 ParameterProcessor.processParameters(request, inlineParams, multiParams, queryId);
 job.setParameters(inlineParams, multiParams); // objects need to be not null (empty ok)
 
 //enable show parameters in job output
 if (request.getParameter("_showParams")!=null){
     job.setShowParameters(true);
 }
 
 //enable show graph data in pdf output
 if (request.getParameter("_showGraphData")!=null){
     job.setShowGraphData(true);
 }
 
 //enable use of custom graph settings
 if (request.getParameter("_showDataPoints")!=null){
     job.setShowGraphDataPoints(true);
 }
  if (request.getParameter("_showLegend")!=null){
     job.setShowGraphLegend(true);
 }
  if (request.getParameter("_showLabels")!=null){
     job.setShowGraphLabels(true);
 }
 String jobGraphOptions=request.getParameter("_GRAPH_SIZE");
 if(StringUtils.isNotBlank(jobGraphOptions) && !StringUtils.equalsIgnoreCase(jobGraphOptions, "default")){
	 job.setJobGraphOptions(jobGraphOptions);
 }
 %>
</c:if>

<script language="javascript" type="text/javascript" src="<%= request.getContextPath() %>/js/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
tinyMCE.init({
	   mode : "exact",
	   elements : "mceedit",
	   theme : "advanced",
		   theme_advanced_buttons1 : "bold,italic,underline,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,undo,redo,fontselect,fontsizeselect,forecolor,link,hr,code",
	   theme_advanced_buttons2 : "",
	   theme_advanced_buttons3 : "",
	   theme_advanced_toolbar_location : "top" });
</script>

<script language="Javascript">
<!-- Begin
function onChangeJobType(t,queryType) {
   // initiate output types
   var s = document.getElementById("outputTypeId");
   s.options.length = 0; // reset the select
   var i=0;
   //display different options for normal queries, charts etc based on the job type
   if (queryType<0){
		//for charts, enable png&pdf output only
		s.options[i++] = new Option("<%=messages.getString("pdf")%>","pdf");
		<c:if test="${job.outputFormat == 'pdf'}">s.selectedIndex = i-1;</c:if>
		s.options[i++] = new Option("<%=messages.getString("png")%>","png");
		<c:if test="${job.outputFormat == 'png'}">s.selectedIndex = i-1;</c:if>

        document.getElementById("emailDiv").className="expand";
		document.getElementById("cacheDiv").className="collapse";
	} else if(queryType==115 || queryType==116){
		//for jasper reports, enable pdf, xls, xlsx output only
		s.options[i++] = new Option("<%=messages.getString("pdf")%>","pdf");
		<c:if test="${job.outputFormat == 'pdf'}">s.selectedIndex = i-1;</c:if>
		s.options[i++] = new Option("<%=messages.getString("xls")%>","xls");
		<c:if test="${job.outputFormat == 'xls'}">s.selectedIndex = i-1;</c:if>
		s.options[i++] = new Option("<%=messages.getString("xlsx")%>","xlsx");
		<c:if test="${job.outputFormat == 'xlsx'}">s.selectedIndex = i-1;</c:if>

        document.getElementById("emailDiv").className="expand";
		document.getElementById("cacheDiv").className="collapse";
	} else if(queryType==117 || queryType==118){
		//for jxls spreadsheets, only enable xls output
		s.options[i++] = new Option("<%=messages.getString("xls")%>","xls");
		<c:if test="${job.outputFormat == 'xls'}">s.selectedIndex = i-1;</c:if>
        document.getElementById("emailDiv").className="expand";
		document.getElementById("cacheDiv").className="collapse";
	} else {
		if (t.value == 5  ) { // only html is allowed inline emails
			s.options[i] = new Option("<%=messages.getString("htmlPlain")%>","htmlPlain");
			<c:if test="${job.outputFormat == 'htmlPlain'}">s.selectedIndex = i;</c:if>

			document.getElementById("emailDiv").className="expand";
			document.getElementById("cacheDiv").className="collapse";
		}
		if (t.value == 2  || t.value == 3 || t.value == 6 || t.value == 8 ) {
			s.options[i++] = new Option("<%=messages.getString("htmlPlain")%>","htmlPlain");
			<c:if test="${job.outputFormat == 'htmlPlain'}">s.selectedIndex = i-1;</c:if>
			s.options[i++] = new Option("<%=messages.getString("xlsZip")%>","xlsZip");
			<c:if test="${job.outputFormat == 'xlsZip'}">s.selectedIndex = i-1;</c:if>

			s.options[i++] = new Option("<%=messages.getString("pdf")%>","pdf");
			<c:if test="${job.outputFormat == 'pdf'}">s.selectedIndex = i-1;</c:if>
			s.options[i++] = new Option("<%=messages.getString("xls")%>","xls");
			<c:if test="${job.outputFormat == 'xls'}">s.selectedIndex = i-1;</c:if>

			s.options[i++] = new Option("<%=messages.getString("xlsx")%>","xlsx");
			<c:if test="${job.outputFormat == 'xlsx'}">s.selectedIndex = i-1;</c:if>

			s.options[i++] = new Option("<%=messages.getString("slk")%>","slk");
			<c:if test="${job.outputFormat == 'slk'}">s.selectedIndex = i-1;</c:if>
			s.options[i++] = new Option("<%=messages.getString("slkZip")%>","slkZip");
			<c:if test="${job.outputFormat == 'slkZip'}">s.selectedIndex = i-1;</c:if>
			s.options[i++] = new Option("<%=messages.getString("tsvZip")%>","tsvZip");
			<c:if test="${job.outputFormat == 'tsvZip'}">s.selectedIndex = i-1;</c:if>
			s.options[i++] = new Option("<%=messages.getString("tsvGz")%>","tsvGz");
			<c:if test="${job.outputFormat == 'tsvGz'}">s.selectedIndex = i-1;</c:if>

			document.getElementById("emailDiv").className="expand";
			document.getElementById("cacheDiv").className="collapse";
		}
		if (t.value == 1 ) { // type: alert
			s.options[0] = new Option("-","-");
			document.getElementById("emailDiv").className="expand";
			document.getElementById("cacheDiv").className="collapse";
		}
		if (t.value == 4) { // type: execute only
			s.options[0] = new Option("-","-");
			document.getElementById("emailDiv").className="collapse";
			document.getElementById("cacheDiv").className="collapse";
		}
		if (t.value == 9 || t.value == 10 ) {
			setCacheDatasources(s);
			document.getElementById("emailDiv").className="collapse";
			document.getElementById("cacheDiv").className="expand";
		}
	}

}

function setCacheDatasources(s) {
	<%
	// show this javascript function only for ADMINS to avoid sharing
	// database ids/names in the page html code to every user enabled to schedule jobs
	if(adminLevel>79){
	    String output = job.getOutputFormat();
		HashMap<Integer,art.dbcp.DataSource> dataSources = ArtDBCP.getDataSources();
		int i =0;
		for (Integer key : dataSources.keySet()) {
			art.dbcp.DataSource ds = (art.dbcp.DataSource) dataSources.get(key);
			String dsName = ds.getName();
			if (key.intValue() != 0) { // don't show ART repository
				%>
				s.options[<%=i%>] = new Option("<%=dsName%>","<%=key%>");
				<%
				if (StringUtils.equals(output,""+key) ) {
					%> s.selectedIndex = <%=i%> <%
				}
				i++;
			}
		}
	}
	%>

}

function showJobTypeHelp(t) {
  if (t.value == 1 ) {
      alert("<%=messages.getString("jobTypeAlertWarning")%>");
   } else if (t.value == 2 ) {
      alert("<%=messages.getString("jobTypeEmailAttWarning")%>");
   } else if (t.value == 5 ) {
      alert("<%=messages.getString("jobTypeEmailInWarning")%>");
   } else if (t.value == 3 ) {
      alert("<%=messages.getString("jobTypePublishWarning")%>");
   } else if (t.value == 4 ) {
      alert("<%=messages.getString("jobTypeExecuteWarning")%>");
   } else if (t.value == 6 ) {
      alert("<%=messages.getString("jobTypeCondEmailAttWarning")%>");
   } else if (t.value == 7 ) {
      alert("<%=messages.getString("jobTypeCondEmailInWarning")%>");
   } else if (t.value == 8 ) {
      alert("<%=messages.getString("jobTypeCondPublishWarning")%>");
   } else if (t.value == 9 ) {
      alert("<%=messages.getString("jobTypeCacheResultAppendWarning")%>");
   } else if (t.value == 10 ) {
      alert("<%=messages.getString("jobTypeCacheResultDeleteInsertWarning")%>");
   }
}

function onChangeAllowSharing(t) {
	if(t.value=="N"){
		//no sharing. splitting doesn't apply
		document.getElementById("allowSplitting").selectedIndex=0;
		document.getElementById("allowSplitting").disabled=true;
		document.getElementById("sharedUsersDiv").className="collapse";
	} else {
		document.getElementById("allowSplitting").disabled=false;
		document.getElementById("sharedUsersDiv").className="expand";
	}
}

function onClickSaveSchedule(t){
	if(t.checked==true){
		//enable schedule name text box
		document.getElementById("scheduleName").readOnly=false;
		document.getElementById('scheduleName').focus();
	} else {
		document.getElementById("scheduleName").readOnly=true;
	}
}

// End -->
</script>

 <!-- id: ${job.jobId} user: ${job.username}  -->
 <form onsubmit="return validateTinyMCE('mceedit', 4000);"  action="<%= request.getContextPath() %>/user/editJob.jsp?" name="JOB_FORM" method="post">
   <input type="hidden" name="action" value="save">

   <input type="hidden" name="queryId" value="<%=queryId%>">

   <%
   if(owner!=null && adminLevel==100){ %>
    <input type="hidden" name="OWNER" value="<%=owner%>">
   <%}%>

 <table align="center" width="60%">

  <tr><td class="title" colspan="4" >
		  <%=messages.getString("defineJob")%>
	 </td>
  </tr>

  <tr><td class="action" colspan="4" ><i><%=messages.getString("jobInfo")%></i></td></tr>
	<tr>
		<td class="data" colspan="4">
			<%=messages.getString("jobName")%>
			<input type="text" name="jobName" value="${job.jobName}" size="40" maxlength="50">
			&nbsp;&nbsp;
			<%=messages.getString("queryName")%>:&nbsp;<%=queryName%>
		</td>
	</tr>
    <tr>
       <td class="data">
	    <%=messages.getString("jobType")%> <br/>
		<c:choose>
			<c:when test="<%=queryType<0%>">
	           <select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
					<option value="6" <c:if test="${job.jobType == 6}">SELECTED</c:if>> <%=messages.getString("jobTypeCondEmailAttachment")%> </option>
			      <option value="8" <c:if test="${job.jobType == 8}">SELECTED</c:if>> <%=messages.getString("jobTypeCondPublish")%>  </option>
				</select>
			</c:when>
			<c:when test="<%=queryType==115%>">
	           <select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
				</select>
			</c:when>
			<c:when test="<%=queryType==116%>">
	           <select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
					<option value="6" <c:if test="${job.jobType == 6}">SELECTED</c:if>> <%=messages.getString("jobTypeCondEmailAttachment")%> </option>
			      <option value="8" <c:if test="${job.jobType == 8}">SELECTED</c:if>> <%=messages.getString("jobTypeCondPublish")%>  </option>
				</select>
			</c:when>
			<c:when test="<%=queryType==117%>">
	           <select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
				</select>
			</c:when>
			<c:when test="<%=queryType==118%>">
	           <select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
					<option value="6" <c:if test="${job.jobType == 6}">SELECTED</c:if>> <%=messages.getString("jobTypeCondEmailAttachment")%> </option>
			      <option value="8" <c:if test="${job.jobType == 8}">SELECTED</c:if>> <%=messages.getString("jobTypeCondPublish")%>  </option>
				</select>
			</c:when>
			<c:otherwise>
				<select name="jobType" id="jobTypeId" onChange="javascript:onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);">
			      <option value="2" <c:if test="${job.jobType == 2}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailAttachment")%> </option>
			      <option value="5" <c:if test="${job.jobType == 5}">SELECTED</c:if>> <%=messages.getString("jobTypeEmailInline")%> </option>
                  <option value="1" <c:if test="${job.jobType == 1}">SELECTED</c:if>> <%=messages.getString("jobTypeAlert")%> </option>			      
			      <option value="3" <c:if test="${job.jobType == 3}">SELECTED</c:if>> <%=messages.getString("jobTypePublish")%>  </option>
			      <option value="4" <c:if test="${job.jobType == 4}">SELECTED</c:if>> <%=messages.getString("jobTypeExecute")%> </option>
				  <option value="6" <c:if test="${job.jobType == 6}">SELECTED</c:if>> <%=messages.getString("jobTypeCondEmailAttachment")%> </option>
				  <option value="7" <c:if test="${job.jobType == 7}">SELECTED</c:if>> <%=messages.getString("jobTypeCondEmailInline")%> </option>
				  <option value="8" <c:if test="${job.jobType == 8}">SELECTED</c:if>> <%=messages.getString("jobTypeCondPublish")%>  </option>
				  <% if(adminLevel>79){ %>
					<option value="9" <c:if test="${job.jobType == 9}">SELECTED</c:if>> <%=messages.getString("jobTypeCacheResultAppend")%> </option>
					<option value="10" <c:if test="${job.jobType == 10}">SELECTED</c:if>> <%=messages.getString("jobTypeCacheResultDeleteInsert")%>  </option>
				  <% } %>
				</select>
			</c:otherwise>
		</c:choose>

	 <input type="button" class="buttonup" onClick="javascript:showJobTypeHelp(document.getElementById('jobTypeId'));" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
       </td>
       <td class="data" >
            &nbsp; <br/>
	    <select name="outputFormat" id="outputTypeId">
		</select>
		<div id="cacheDiv" class="collapse">
			Cached Table Name <input type="button" class="buttonup" onClick="javascript:alert('<%=messages.getString("jobCachedTableHelp")%>');" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			<br/>
			<input type="text" name="cachedTableName" value="${job.cachedTableName}" size="24" maxlength="30">
		</div>
       </td>

       <td class="data">
	    <%=messages.getString("EnableAuditing")%> <br/>
           <select name="enableAudit">
	      <option value="N" <c:if test="${job.enableAudit == 'N'}">SELECTED</c:if>> No</option>
	      <option value="Y" <c:if test="${job.enableAudit == 'Y'}">SELECTED</c:if>> Yes </option>
	   </select>
       </td>

	   <td class="data">
	    <%=messages.getString("jobStatus")%> <br/>
           <select name="activeStatus">
		   <option value="A" <c:if test="${job.activeStatus == 'A'}">selected</c:if>> Active </option>
	      <option value="D" <c:if test="${job.activeStatus == 'D'}">selected</c:if>> Disabled </option>
	   </select>
       </td>

   </tr>
 </table>
	<div id="emailDiv" class="collapse">
	 <table align="center" width="60%">
	   <tr><td class="action" colspan="4" ><i><%=messages.getString("jobEmailMessage")%></i></td></tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailFrom")%> </td>
		   <td class="data" colspan="2">
			   <input type="text" name="from" value="<%=email%>" size="30" readonly>
			   <%
			   help = "Your email address as configured in the user definition." +
					   "\\nIf it is blank, emails will not be sent successfully.";
			   %>
        <input type="button" class="buttonup" onClick="alert('<%=help%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		   </td>
	   </tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailTo")%> <br><small>(<%=messages.getString("mailToWarning")%>)</small> </td>
		   <td class="data" colspan="2">
			   <input type="text" name="tos" value="${job.tos}" size="60" maxlength="254">
		   </td>
	   </tr>
	    <tr>
		   <td class="data"> <%=messages.getString("mailRecipients")%> </td>
		   <td class="data" colspan="2">
			  <select name="recipientsQueryId">
		   <option value="0" <c:if test="${job.recipientsQueryId == 0}">selected</c:if>> None </option>
	      <%
//load dynamic recipient queries
   Map<Integer,String> rq=aq.getDynamicRecipientQueries();
    it = rq.entrySet().iterator();
	Integer recipientQueryId;
	String recipientQueryName;
	while(it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		recipientQueryId= (Integer) entry.getKey();
		recipientQueryName= (String) entry.getValue();
		%>
   <option value="<%=recipientQueryId%>" <%=job.getRecipientsQueryId()==recipientQueryId?"selected":""%>><%=recipientQueryName%></option>
   <%
   }
   %>
	   </select>
		   </td>
	   </tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailCc")%> </td>
		   <td class="data" colspan="2">
			   <input type="text" name="cc" value="${job.cc}" size="60" maxlength="254">
		   </td>
	   </tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailBcc")%> </td>
		   <td class="data" colspan="2">
			   <input type="text" name="bcc" value="${job.bcc}" size="60" maxlength="254">
		   </td>
	   </tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailSubject")%> </td>
		   <td class="data" colspan="2"> <input type="text" name="subject" value="${job.subject}" size="60" maxlength="254">
		   </td>
	   </tr>
	   <tr>
		   <td class="data"> <%=messages.getString("mailMessage")%> </td>
		 <td class="data" colspan="2">
		   <textarea name="message"  id="mceedit" rows="8" cols="60">${job.message}</textarea>
		 </td>
	   </tr>

	 </table>
 </div>

 <script language="javascript">
	onChangeJobType(document.getElementById('jobTypeId'),<%=queryType%>);
 </script>

 <table align="center" width="60%">

  <tr><td class="action" colspan="4" ><i><%=messages.getString("schedule")%></i><br/><code><%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.MEDIUM,request.getLocale()).format(new java.util.Date())%></code></td></tr>
  <tr>
  <td class="data" colspan="4"><%=messages.getString("savedSchedules")%>
  <select name="schedules" id="schedules">
  <option value="-">--</option>
  <%
  //get and display saved schedules
  JobSchedule js=new JobSchedule();
  List<String> schedules=js.getAllScheduleNames();
  it=schedules.iterator();
   String name;
	while(it.hasNext()) {
	 name=(String)it.next();
	%>
	<option value="<%=name%>" ><%=name%></option>
	<% }
	%>
  </select>
  <input id="getSchedule" type="button" class="buttonup" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" value="<%=messages.getString("getSavedSchedules")%>" />
  </td>
  </tr>

  <tr>
       <td class="data"> <%=messages.getString("month")%>
           <input type="text" name="month" id="month" value="${job.month}" />
       </td>
       <td class="data"> <%=messages.getString("day")%>
	  <input type="text" name="day" id="day" value="${job.day}" />
       </td>
       <td class="data" colspan="1"> <%=messages.getString("weekDay")%>
	  <input type="text" name="weekday" id="weekday" value="${job.weekday}" />
       </td>
   </tr>

  <tr>
       <td class="data"> <%=messages.getString("hour")%>
	  <input type="text" name="hour" id="hour" value="${job.hour}" />
       </td>

       <td class="data"> <%=messages.getString("minute")%>
           <input type="text" name="minute" id="minute" value="${job.minute}" />
       </td>

   </tr>

   <tr>
	<td class="data" colspan="2"> <%=messages.getString("startDate")%>
           <input type="text" id="startDateString" name="startDateString" value="${job.startDateString}" maxlength="10" />
 <img onclick="displayCalendar(document.getElementById('startDateString'),'yyyy-mm-dd',this)" src="<%= request.getContextPath() %>/images/calendar.png">
       </td>
	   <td class="data" colspan="2"> <%=messages.getString("endDate")%>
           <input type="text" id="endDateString" name="endDateString" value="${job.endDateString}" maxlength="10" />
<img onclick="displayCalendar(document.getElementById('endDateString'),'yyyy-mm-dd',this)" src="<%= request.getContextPath() %>/images/calendar.png">
       </td>
	  </tr>

	  <tr>
	  <td class="data" colspan="4">
	  <% if(saveJobSchedule){ %>
		  <input type="checkbox" name="saveSchedule" id="saveSchedule" checked onclick="javascript:onClickSaveSchedule(this);" />
		<%} else { %>
		<input type="checkbox" name="saveSchedule" id="saveSchedule" onclick="javascript:onClickSaveSchedule(this);" />
		<%}%>
	  <%=messages.getString("saveSchedule")%>&nbsp;&nbsp; <%=messages.getString("scheduleName")%>
	  <input type="text" name="scheduleName" id="scheduleName" value="<%=jobScheduleName%>" size="50" maxlength="50" />
	  </td>
	  </tr>

	  <tr><td class="action" colspan="4" ><i><%=messages.getString("jobSharing")%></i></td></tr>
	  <tr>
       <td class="data" colspan="4"> <%=messages.getString("allowJobSharing")%>
           <select name="allowSharing" id="allowSharing" onChange="javascript:onChangeAllowSharing(this);">
		   <% if(StringUtils.equals(job.getAllowSharing(),"Y")){ %>
	      <option value="N"> No</option>
	      <option value="Y" selected> Yes </option>
		  <% } else { %>
		  <option value="N" selected> No</option>
	      <option value="Y"> Yes </option>
		  <%}%>
	   </select>
       &nbsp;&nbsp;
	   <%=messages.getString("allowJobSplitting")%>
            <select name="allowSplitting" id="allowSplitting" >
		   <% if(StringUtils.equals(job.getAllowSharing(),"Y")){
		    if(StringUtils.equals(job.getAllowSplitting(),"Y")){ %>
	      <option value="N"> No</option>
	      <option value="Y" selected> Yes </option>
		  <% } else { %>
		  <option value="N" selected> No</option>
	      <option value="Y"> Yes </option>
		  <%}
		  } else {%>
		  <option value="N" selected> No</option>
	      <option value="Y"> Yes </option>
		  <%} %>
	   </select>
       </td>
   </tr>
   </table>

   	<div id="sharedUsersDiv" class="collapse">
	 <table align="center" width="60%">
   <tr>
   <td class="data">
   Select users to share the job with
   </td>
   <td class="data">
   <select name="sharedUsers" size="5" multiple>
   <%
   //sort array of previously selected username in order to use binary search to find and re-select them
   if(selectedUsers!=null){
	Arrays.sort(selectedUsers);
	}
   List<String> usernames=ue.getAllUsernames();
   it=usernames.iterator();
   String username;
	while(it.hasNext()) {
	 username=(String)it.next();
	 if(selectedUsers==null){
	%>
	<option value="<%=username%>" ><%=username%></option>
	<% } else {
	%>
   <option value="<%=username%>" <%=Arrays.binarySearch(selectedUsers,username)>=0?"selected":""%> ><%=username%></option>
   <% }
   }
   %>
	</select>
	</td>

	<td class="data">
	Select user groups to share the job with
	</td>
	<td class="data">
	<select name="sharedUserGroups" size="5" multiple>
   <%
//sort array of previously selected user groups in order to use binary search to find and re-select them
   if(selectedGroups!=null){
	Arrays.sort(selectedGroups);
	}

	UserGroup ug=new UserGroup();
   Map groups=ug.getAllUserGroupNames();
    it = groups.entrySet().iterator();
	String groupId;
	while(it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		groupId=String.valueOf(entry.getValue());
		if(selectedGroups==null){
   %>
   <option value="<%=groupId%>"><%=entry.getKey()%></option>
   <% } else {%>
   <option value="<%=groupId%>" <%=Arrays.binarySearch(selectedGroups,groupId)>=0?"selected":""%>><%=entry.getKey()%></option>
   <% }
   }
   %>
	</select>
	</td>
	</tr>

	<tr>
	<td colspan="4" style="color:#003366;">
	Action:
	<select name="sharedUsersAction">
	<option value="GRANT" <%="GRANT".equals(action)?"selected":""%> >GRANT </option>
	<option value="REVOKE" <%="REVOKE".equals(action)?"selected":""%> >REVOKE </option>
	</select>
   </td>
   </tr>
   		<tr><td colspan="4" class="data"> Current Assignment </td></tr>
		<tr>            
            <td colspan="2" class="data2">                
				<%				
				Map map;
				map=job.getSharedUsers();
				if(map.size()>0){
					%>
					<b>Users</b><br>
					<%
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<%=entry.getValue()%> <br>
						<%
					}
					%>
					<br>
					<%
				}
				%>    

				<%				
				map=job.getSharedUserGroups();
				if(map.size()>0){
					%>
					<b>User Groups</b><br>
					<%
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<%=entry.getValue()%> <br>
						<%
					} 
				}
				%>    
            </td>
        </tr>
   </table>
	  </div>

	  <table align="center" width="60%">
	  <tr>
	  <td colspan="4" class="data" style="color:red"> <%=msg%></td>
	  </tr>

 <tr>
  <td colspan="4" class="action" align="center" style="padding-bottom:6px"><br><input type="submit" value="<%=messages.getString("scheduleButton")%>">&nbsp;&nbsp;
  <br><br><small> <%=messages.getString("bottomNote")%> </small>
  </td>
  </tr>

 </table>
 </form>


<%
  String dataProviderUrl = request.getContextPath()+"/XmlDataProvider";
 %>
<ajax:updateField
  baseUrl="<%=dataProviderUrl%>"
  source="schedules"
  target="minute,hour,day,month,weekday"
  action="getSchedule"
  parameters="action=schedule,scheduleName={schedules}"
  valueUpdateByName="true"
  parser="new DefaultResponseParser('xml')"
  preFunction="artAddWork"
  postFunction="artRemoveWork"
 />


<%@ include file="scheduleHelp.html" %>

<%@ include file ="footer.jsp" %>


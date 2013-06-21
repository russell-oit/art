<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="java.util.*,art.utils.*,art.servlets.ArtDBCP,org.quartz.*,java.text.*" %>
<%@ page import="static org.quartz.JobBuilder.*" %>
<%@ page import="static org.quartz.TriggerBuilder.*" %>
<%@ page import="static org.quartz.JobKey.jobKey" %>
<%@ page import="static org.quartz.TriggerKey.triggerKey" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<%!
//format timestamp values for display
public String formatTimestamp(Date dt, Locale loc){
	String formattedTimestamp;	
	if (dt==null){
		formattedTimestamp="--";
	} else {
		String timestampFormat="dd-MMM-yyyy HH:mm:ss";
		SimpleDateFormat formatter=new SimpleDateFormat(timestampFormat,loc);
		formattedTimestamp=formatter.format(dt);
	}
	return formattedTimestamp;
}

//display job type
public String displayJobType(int jobType,ResourceBundle messages){
String description="";	   
	switch(jobType) {
		case 1: description=messages.getString("jobTypeAlert");
			   break;
		case 2: description=messages.getString("jobTypeEmailAttachment");
			   break;
		case 5: description=messages.getString("jobTypeEmailInline");
				   break;
		case 3: description=messages.getString("jobTypePublish");
			   break;
		case 4: description=messages.getString("jobTypeExecute");
			   break;
		case 6: description=messages.getString("jobTypeCondEmailAttachment");
			   break;
		case 7: description=messages.getString("jobTypeCondEmailInline");
				   break;
		case 8: description=messages.getString("jobTypeCondPublish");
			   break;
		case 9: description=messages.getString("jobTypeCacheResultAppend");
			   break;
		case 10: description=messages.getString("jobTypeCacheResultDeleteInsert");
			   break;
	}
	return description;
}           
    
%>

<c:remove var="job" scope="session" />

<% 
 // note this page is ugly. a mess of jsp and java...
 // call it hystorical entertainement... we should create some beans here...

Locale locale=request.getLocale();

 String msg = request.getParameter("MESSAGE");
 int accessLevel = ue.getAccessLevel();
 
String owner;
owner=request.getParameter("OWNER");


 /* Run or Delete a Job */
 String action = request.getParameter("action");
 int jobRunningId = -1;
 
 if (action != null && !action.equals("save")) {    
	int jobId = Integer.parseInt(request.getParameter("jobId"));			
	
	//get scheduler instance
	org.quartz.Scheduler scheduler=ArtDBCP.getScheduler();

	boolean schedulingEnabled=ArtDBCP.isSchedulingEnabled();
  
    if ( action.equals("run") ) { 
	
		//create temporary quartz job to run the job
		long ctime = System.currentTimeMillis();  
		//JobDetail tempJob = newJob("temp-job-"+jobId+"-"+ctime,"tempJobGroup", ArtJob.class);
		JobDetail tempJob = newJob(ArtJob.class)
				.withIdentity(jobKey("temp-job-"+jobId+"-"+ctime,"tempJobGroup"))
				.usingJobData("jobid",jobId)
				.usingJobData("tempjob","yes")
				.build();
        // create SimpleTrigger that will fire once, immediately		        
		SimpleTrigger tempTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(triggerKey("temp-trigger-"+jobId+"-"+ctime,"tempTriggerGroup"))
				.startNow()
				.build();
        				
		if (scheduler!=null){		
			if (schedulingEnabled){
				jobRunningId = jobId;
				scheduler.scheduleJob(tempJob, tempTrigger);
				msg = "Job (" + jobId + ") Running";
			}
			else {
				msg = "Couldn't run job. Scheduling not enabled";
			}															
		}
		else {
			msg = "Couldn't run job. Scheduler not available";
		}
       
    } else if (action.equals("delete")) {       		
		if (scheduler!=null){
		// Delete Job	   
        ArtJob aj = new ArtJob();
	   
	   //allow admin to manage all jobs
	   if(owner!=null && accessLevel>=80){
	       aj.load(jobId, owner);
		} else {
			aj.load(jobId, ue.getUsername());
		}
       aj.delete();
	      	   	   
	   msg = "Job (" + jobId + ") Deleted";
		}
		else {
		msg = "Couldn't delete job. Scheduler not available";
		}   	   		
       
    }
 }
 
Map<Integer,ArtJob> myJobs=ue.getJobs();
	
int jobId;
int jobType;
String queryName;
String lastFileName;
String resultMessage;
%>

 <table align="center" width="50%">

  <tr><td class="title"><%=messages.getString("myJobs")%></td></tr>  

  <tr><td class="action"><code><%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.MEDIUM,request.getLocale()).format(new java.util.Date())%></code>
      &nbsp;<a href="<%= request.getContextPath() %>/user/myJobs.jsp"><img src="<%= request.getContextPath() %>/images/listrefresh.png" title="<%=messages.getString("refresh")%>" border="0"></a></td></tr>  
 </table>
  

<%
    if (msg != null) {
       %>
         <table align="center">
          <tr>
	   <td class="attr"><img src="<%= request.getContextPath() %>/images/warning.png" > 
	   <%=msg%>
	   </td>
	  </tr>
	 </table>
	  
       <%
    }
	%>
	<p>
	<table align="center" width="95%">
  <tr>
  <td class="attr"><%=messages.getString("jobId")%></td>
  <td class="attr" width="15%"><%=messages.getString("jobName")%></td>
  <td class="attr"><%=messages.getString("jobType")%></td>
  <td class="attr"><%=messages.getString("jobTimeTaken")%></td>
  <td class="attr"><%=messages.getString("lastEndDate")%> </td>
  <td class="attr" width="45%"><%=messages.getString("result")%></td>
  <td class="attr"><%=messages.getString("nextRunDate")%> </td>
  <td class="attr" width="60px">&nbsp;</td>
  </tr>
  <%
    for (Map.Entry<Integer, ArtJob> entry : myJobs.entrySet()) {
		ArtJob job=entry.getValue();
		
		jobId=job.getJobId();
		jobType=job.getJobType();
		lastFileName=job.getFileName();
		queryName=job.getQueryName();
        
        java.util.Date lastEndDate=job.getLastEndDate();
        java.util.Date lastStartDate=job.getLastStartDate();
        long timeTakenMilli=-1; //time taken in milliseconds. default to negative value so that blank is displayed if either date is null
        if(lastEndDate!=null && lastStartDate!=null){
            timeTakenMilli=lastEndDate.getTime()-lastStartDate.getTime(); 
        }
        String timeTakenString;
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("#,##0.0##");
        double timeTaken=timeTakenMilli/(double)1000; //time taken in seconds  
        timeTakenString=df.format(timeTaken) + " " + messages.getString("seconds");
        if(timeTaken>60){
            //display time as mins:seconds
            int seconds = (int) ((timeTakenMilli / 1000) % 60);
            int minutes = (int) ((timeTakenMilli / 1000) / 60);
            df.applyPattern("00");
            timeTakenString=minutes + ":" + df.format(seconds) + " " + messages.getString("minutes");
        } else if(timeTaken<0){
            timeTakenString=""; //start time greater than end time. perhaps job didn't complete
        }
  
%>
  <tr>
   <td class="jobdetails">
    <b><%=jobId%></b>
    <br> <a href="javascript:showHide(document.getElementById('tr_<%=jobId%>'));">+/-</a>
   </td>
   <td class="jobdetails"><b><%=job.getJobName()%></b></td>  
   <td class="jobdetails"><%=displayJobType(jobType,messages)%></td>
   <td class="jobdetails"><%=timeTakenString%></td>
   <td class="jobdetails" >
	   <%
	 if(StringUtils.isBlank(timeTakenString)){
		 out.println("");
	 } else { %>
	   <code><%=formatTimestamp(job.getLastEndDate(),locale)%></code>
	   <% } %>
   </td>  
   <td class="jobdetails">
     <%
	 if(StringUtils.isBlank(timeTakenString)){
		 out.println("");
	 } else if ( lastFileName == null )  {
	out.println(messages.getString("noFile"));
      } else if (lastFileName.startsWith("-")) {
        out.println(lastFileName.substring(1));
     } else { 
		List<String> details=ArtDBCP.getFileDetailsFromResult(lastFileName);
		lastFileName=details.get(0);
		resultMessage=details.get(1);
	   %>
        <a type="application/octet-stream" href="<%= request.getContextPath() %>/export/jobs/<%=lastFileName%>" target="_blank"><%=lastFileName%> </a>		
     <%
out.println(resultMessage);
}
   %>      
   </td> 
   <td class="jobdetails" > <code><%=formatTimestamp(job.getNextRunDate(),locale)%></code>  </td>  
   <td class="jobdetails" width="60px"> 
     <a href="./myJobs.jsp?action=delete&jobId=<%=jobId%>" onClick="return confirm('<%=messages.getString("onDeleteJob")%> <%=jobId%>?')"><img src="<%= request.getContextPath() %>/images/delete.png" title="<%=messages.getString("delete")%>" border="0"></a>
     
     <a href="./editJob.jsp?jobId=<%=jobId%>"><img src="<%= request.getContextPath() %>/images/edit-10px.png" title="<%=messages.getString("edit")%>" border="0"></a>
     
     <% if (jobRunningId != jobId) { %>
     <a href="./myJobs.jsp?action=run&jobId=<%=jobId%>"><img src="<%= request.getContextPath() %>/images/play.png" title="<%=messages.getString("runNow")%>" border="0"></a>
     <% } %>
   </td>
  
  </tr>  
  <tr id="tr_<%=jobId%>" class="collapse">
	<td></td>
	<td colspan="6">
            <table border="0" width="100%">
			<tr>
			<td class="jobdetails"><i><%=messages.getString("queryName")%></i></td>
              <td class="jobdetails" colspan="3"><%=queryName%></td>
			  </tr>
             <tr>
	      <td class="jobdetails"><i><%=messages.getString("lastStartDate")%></i></td>
              <td class="jobdetails"><code><%=formatTimestamp(job.getLastStartDate(),locale)%></code></td>
			  
            <% if (jobType == 9 || jobType == 10) { %>
              <td class="jobdetails"><i>Cached Table Name</i></td>
              <td class="jobdetails"><code><%=job.getCachedTableName()%></code></td>
	     </tr>
             <tr>
              <td class="jobdetails" colspan="4">
	    <% } else { %>	    
             <td class="jobdetails"><i><%=messages.getString("viewMode")%></i></td>
              <td class="jobdetails"><code><%=job.getOutputFormat()%></code></td>
             </tr>
             <tr>
              <td class="jobdetails"><i><%=messages.getString("mailTo")%></i></td>
              <td class="jobdetails"><code><%=job.getTos()%></code></td>
              <td class="jobdetails"><i><%=messages.getString("mailSubject")%></i></td>
              <td class="jobdetails"><code><%=job.getSubject()%></code></td>
             </tr>
             <tr>
              <td class="jobdetails" colspan="4">
               <i><%=messages.getString("mailMessage")%>: </i> <br>
               <div style="border: solid 1px"><%=job.getMessage()%>&nbsp;</div>
	      <% } %>

			<%=job.getParametersDisplayString()%>
			
			<br>	      
		    <table width="60%" align="center"> 
		      <tr>
		       <td class="action"><small><%=messages.getString("month")%>  </small></td>  
		       <td class="action"><small><%=messages.getString("day")%>    </small></td>  
		       <td class="action"><small><%=messages.getString("weekDay")%></small></td>
			   <td class="action"><small><%=messages.getString("hour")%>   </small></td>
			   <td class="action"><small><%=messages.getString("minute")%> </small></td>
		      </tr>
		      <tr>
		       <td class="attr"><small><code><%=job.getMonth()%></code></small></td>  
		       <td class="attr"><small><code><%=job.getDay()%></code></small></td>  
		       <td class="attr"><small><code><%=job.getWeekday()%></code></small></td>
			   <td class="attr"><small><code><%=job.getHour()%></code></small></td>
		       <td class="attr"><small><code><%=job.getMinute()%></code></small></td>   
		      </tr>
		     </table>
              </td>
             </tr>
            </table>
	</td>
  </tr>

<%
    }
   %>
    </table>


</p>

<br /> 

 
<%   
//allow admin to manage all jobs
if(accessLevel>=80){
%>
    <table align="center" width="95%">
  <tr>
  <td align="center">   
  
<a href="javascript:showHide(document.getElementById('otherJobDivId'));"><%=messages.getString("manageOtherJobs")%></a>		
<!-- - ---------------------------------------- - -->

<div id="otherJobDivId" class="collapse">
 <table>
  <tr>
  <td class="attr">
   <%=messages.getString("jobOwner")%>
  </td>  
  <td class="attr"><%=messages.getString("jobId")%></td>
  <td class="attr" width="15%"><%=messages.getString("jobName")%></td> 
  <td class="attr"><%=messages.getString("jobType")%></td>
  <td class="attr"><%=messages.getString("jobTimeTaken")%></td>
  <td class="attr"><%=messages.getString("lastEndDate")%></td>
  <td class="attr" width="40%"><%=messages.getString("result")%></td>
  <td class="attr"><%=messages.getString("nextRunDate")%></td>
  <td class="attr" width="60px">&nbsp;</td>
  </tr>
  <%
	Map<String,ArtJob> otherJobs=ue.getOtherJobs();
	String jobOwner;
    for (Map.Entry<String, ArtJob> entry : otherJobs.entrySet()) {
		ArtJob job = entry.getValue();
		
		jobId=job.getJobId();
		jobType=job.getJobType();
		lastFileName=job.getFileName();
		queryName=job.getQueryName();
		jobOwner=job.getUsername();
        
        java.util.Date lastEndDate=job.getLastEndDate();
        java.util.Date lastStartDate=job.getLastStartDate();
        long timeTakenMilli=-1; //time taken in milliseconds. default to negative value so that blank is displayed if either date is null
        if(lastEndDate!=null && lastStartDate!=null){
            timeTakenMilli=lastEndDate.getTime()-lastStartDate.getTime(); 
        }
        String timeTakenString;
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("#,##0.0##");
        double timeTaken=timeTakenMilli/(double)1000; //time taken in seconds  
        timeTakenString=df.format(timeTaken) + " " + messages.getString("seconds");
        if(timeTaken>60){
            //display time as mins:seconds
            int seconds = (int) ((timeTakenMilli / 1000) % 60);
            int minutes = (int) ((timeTakenMilli / 1000) / 60);
            df.applyPattern("00");
            timeTakenString=minutes + ":" + df.format(seconds) + " " + messages.getString("minutes");
        } else if(timeTaken<0){
            timeTakenString=""; //start time greater than end time. perhaps job didn't complete
        }
%>
  <tr>
  <td class="jobdetails"><b><%=jobOwner%></b>
  <br> <a href="javascript:showHide(document.getElementById('tr2_<%=jobId%>'));">+/-</a>
  </td>  
  <td class="jobdetails"><%=jobId%></td>
   <td class="jobdetails"><b><%=job.getJobName()%></b></td>  
   <td class="jobdetails"><%=displayJobType(jobType,messages)%></td> 
   <td class="jobdetails"><%=timeTakenString%></td>
   	 <td class="jobdetails" > <code><%=formatTimestamp(job.getLastEndDate(),locale)%></code>  </td>  
   <td class="jobdetails">
     <%
	 if(StringUtils.isBlank(timeTakenString)){
		 out.println("");
	 } else if ( lastFileName == null )  {
	out.println(messages.getString("noFile"));
      } else if (lastFileName.startsWith("-")) {
        out.println(lastFileName.substring(1));
     } else { 
		List<String> details=ArtDBCP.getFileDetailsFromResult(lastFileName);
		lastFileName=details.get(0);
		resultMessage=details.get(1);
	   %>
        <a type="application/octet-stream" href="<%= request.getContextPath() %>/export/jobs/<%=lastFileName%>" target="_blank"><%=lastFileName%> </a>		
     <%
out.println(resultMessage);
}
   %>          
   </td> 
   <td class="jobdetails" > <code><%=formatTimestamp(job.getNextRunDate(),locale)%></code>  </td>  
   <td class="jobdetails" width="60px"> 
     <a href="./myJobs.jsp?action=delete&jobId=<%=jobId%>&OWNER=<%=jobOwner%>" onClick="return confirm('<%=messages.getString("onDeleteJob")%> <%=jobId%>?')"><img src="<%= request.getContextPath() %>/images/delete.png" title="<%=messages.getString("delete")%>" border="0"></a>
     
     <a href="./editJob.jsp?jobId=<%=jobId%>&OWNER=<%=jobOwner%>"><img src="<%= request.getContextPath() %>/images/edit-10px.png" title="<%=messages.getString("edit")%>" border="0"></a>
     
     <% if (jobRunningId != jobId) { %>
     <a href="./myJobs.jsp?action=run&jobId=<%=jobId%>"><img src="<%= request.getContextPath() %>/images/play.png" title="<%=messages.getString("runNow")%>" border="0"></a>
     <% } %>
   </td>  
  </tr>  
  <tr id="tr2_<%=jobId%>" class="collapse">
        <td></td>
	<td colspan="7">
            <table border="0" width="100%">
				<tr>
			<td class="jobdetails"><i><%=messages.getString("queryName")%></i></td>
              <td class="jobdetails" colspan="3"><%=queryName%></td>
			  </tr>
             <tr>
	      <td class="jobdetails"><i><%=messages.getString("lastStartDate")%></i></td>
              <td class="jobdetails"><code><%=formatTimestamp(job.getLastStartDate(),locale)%></code></td>
            <% if (jobType == 9 || jobType == 10) { %>
              <td class="jobdetails"><i>Cached Table Name</i></td>
              <td class="jobdetails"><code><%=job.getCachedTableName()%></code></td>
	     </tr>
             <tr>
              <td class="jobdetails" colspan="4">
	    <% } else { %>	    
             <td class="jobdetails"><i><%=messages.getString("viewMode")%></i></td>
              <td class="jobdetails"><code><%=job.getOutputFormat()%></code></td>
             </tr>
             <tr>
              <td class="jobdetails"><i><%=messages.getString("mailTo")%></i></td>
              <td class="jobdetails"><code><%=job.getTos()%></code></td>
              <td class="jobdetails"><i><%=messages.getString("mailSubject")%></i></td>
              <td class="jobdetails"><code><%=job.getSubject()%></code></td>
             </tr>
             <tr>
              <td class="jobdetails" colspan="4">
               <i><%=messages.getString("mailMessage")%>: </i> <br>
               <div style="border: solid 1px"><%=job.getMessage()%>&nbsp;</div>
	      <% } %>	    
		  
			<%=job.getParametersDisplayString()%>
			
			<br>	      
		    <table width="60%" align="center"> 
		      <tr>
		       <td class="action"><small><%=messages.getString("month")%>  </small></td>  
		       <td class="action"><small><%=messages.getString("day")%>    </small></td>  
		       <td class="action"><small><%=messages.getString("weekDay")%></small></td>
			   <td class="action"><small><%=messages.getString("hour")%>   </small></td>
			   <td class="action"><small><%=messages.getString("minute")%> </small></td> 
		      </tr>
		      <tr>
		       <td class="attr"><small><code><%=job.getMonth()%></code></small></td>  
		       <td class="attr"><small><code><%=job.getDay()%></code></small></td>  
		       <td class="attr"><small><code><%=job.getWeekday()%></code></small></td>
			   <td class="attr"><small><code><%=job.getHour()%></code></small></td>
		       <td class="attr"><small><code><%=job.getMinute()%></code></small></td>  
		      </tr>
		     </table>
              </td>
             </tr>
            </table>
	</td>
  </tr>

<%
    }
   %>
    </table>
	
	</td>
	</tr>
	</table>
<%
}	 
  
%> 

<%@ include file ="footer.jsp" %>


 

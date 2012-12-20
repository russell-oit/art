<%@ page import="java.util.*,java.text.*,art.utils.*,art.servlets.ArtDBCP" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<%!
//format timestamp values for display
public String formatTimestamp(java.util.Date date, Locale loc){
	String formattedTimestamp;	
	if (date==null){
		formattedTimestamp="--";
	} else {
		String timestampFormat="dd-MMM-yyyy HH:mm:ss";
		SimpleDateFormat formatter=new SimpleDateFormat(timestampFormat,loc);
		formattedTimestamp=formatter.format(date);
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


<% 
Locale locale=request.getLocale();
String resultMessage;

boolean splitJob=false; //for split jobs, get last start date and file name from art_user_jobs table
  
 try {
	
%>

 <table align="center" width="50%">

  <tr><td class="title"><%=messages.getString("sharedJobs")%></td></tr>  

  <tr><td class="action"><code><%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.MEDIUM,request.getLocale()).format(new java.util.Date())%></code>
      &nbsp;<a href="<%= request.getContextPath() %>/user/sharedJobs.jsp"><img src="<%= request.getContextPath() %>/images/listrefresh.png" title="<%=messages.getString("refresh")%>" border="0"></a></td></tr>   
 </table>
  
	<p>
 <!--   -->
 <table align="center" width="90%">
  <tr>
  <td class="attr"><%=messages.getString("jobId")%></td>
  <td class="attr" width="15%"><%=messages.getString("jobName")%></td>
  <td class="attr"><%=messages.getString("jobType")%></td>
  <td class="attr"><%=messages.getString("jobTimeTaken")%></td>
  <td class="attr"><%=messages.getString("lastEndDate")%> </td>
  <td class="attr" width="40%"><%=messages.getString("result")%></td>
  <td class="attr"><%=messages.getString("nextRunDate")%> </td>
  </tr>
  
  <%
  //get shared jobs user has access to
	Map<Integer,ArtJob> jobs=ue.getSharedJobs();
	
    for (Map.Entry<Integer, ArtJob> entry : jobs.entrySet()) {
		ArtJob job=entry.getValue();
		
		if(StringUtils.equals(job.getQueryRulesFlag(),"Y") && StringUtils.equals(job.getAllowSplitting(),"Y")){
			splitJob=true;
		} else {
			splitJob=false;
		}
        
        java.util.Date lastEndDate;   
   if(splitJob){
	//split job. get date from the art_user_jobs table
		lastEndDate=job.getSharedLastEndDate();
	} else {
		//get value from jobs table
		lastEndDate=job.getLastEndDate();
	}
        
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
    <b><%=job.getJobId()%></b>
    <br> <a href="javascript:showHide(document.getElementById('tr_<%=job.getJobId()%>'));">+/-</a>
   </td>
   <td class="jobdetails"><b><%=job.getJobName()%></b></td>  
   <td class="jobdetails"><%=displayJobType(job.getJobType(),messages)%></td>
   <td class="jobdetails"><%=timeTakenString%></td>
   <td class="jobdetails" >    	 
	 <%
	 if(StringUtils.isBlank(timeTakenString)){
		 out.println("");
	 } else { %>
	   <code><%=formatTimestamp(lastEndDate,locale)%></code>
	   <% } %>
   </td>  
   <td class="jobdetails">
<%
	String fileName;   
    if(splitJob){
	//split job. get file name from the art_user_jobs table
		fileName=job.getSharedFileName();
	} else {
		//get value from jobs table
		fileName=job.getFileName();
	}
	if(StringUtils.isBlank(timeTakenString)){
		out.println("");
	} else if (fileName==null){
		out.println(messages.getString("noFile"));
	} else if (fileName.startsWith("-")) { 
        out.println(fileName.substring(1));
	}  else { 
		List<String> details=ArtDBCP.getFileDetailsFromResult(fileName);
		fileName=details.get(0);
		resultMessage=details.get(1);
	   %>
        <a type="application/octet-stream" href="<%= request.getContextPath() %>/export/jobs/<%=fileName%>" target="_blank"><%=fileName%> </a>		
     <%
out.println(resultMessage);
}
   %>           

   </td> 
   <td class="jobdetails" > <code><%=formatTimestamp(job.getNextRunDate(),locale)%></code>  </td>  
  
  </tr>  
  <tr id="tr_<%=job.getJobId()%>" class="collapse">
	<td></td>
	<td colspan="5">
            <table border="0" width="100%">
			<tr>
			<td class="jobdetails"><i><%=messages.getString("queryName")%></i></td>
              <td class="jobdetails" colspan="3"><%=job.getQueryName()%></td>
			  </tr>
             <tr>
	      <td class="jobdetails"><i><%=messages.getString("lastStartDate")%></i></td>
              <td class="jobdetails"><code><%=formatTimestamp(job.getLastStartDate(),locale)%></code></td>
            <% if (job.getJobType() == 9 || job.getJobType() == 10) { %>
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

<%
  } catch(Exception e) {
     out.println("Exception: " + e);
  } 
%> 
<%@ include file ="footer.jsp" %>

 

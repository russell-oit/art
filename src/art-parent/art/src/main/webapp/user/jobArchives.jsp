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
           
%>


<% 
Locale locale=request.getLocale();
String resultMessage;

 try {
	
%>

 <table align="center" width="50%">

  <tr><td class="title"><%=messages.getString("jobArchives")%></td></tr>  

  <tr><td class="action"><code><%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.MEDIUM,request.getLocale()).format(new java.util.Date())%></code>
      &nbsp;<a href="<%= request.getContextPath() %>/user/jobArchives.jsp"><img src="<%= request.getContextPath() %>/images/listrefresh.png" title="<%=messages.getString("refresh")%>" border="0"></a></td></tr>   
 </table>
  
	<p>
 <!--   -->
 <table align="center" width="90%">
  <tr>
  <td class="attr" width="5%"><%=messages.getString("jobId")%></td>
  <td class="attr" width="30%"><%=messages.getString("jobName")%></td>
  <td class="attr"><%=messages.getString("runDate")%></td>
<td class="attr" width="50%"><%=messages.getString("result")%></td>
  </tr>
  
  <%
  //get job archives user has access to
	Map<String,ArtJob> jobs=ue.getJobArchives();
	
    for (Map.Entry<String, ArtJob> entry : jobs.entrySet()) {
		ArtJob job=entry.getValue();
  
%>
  <tr>
   <td class="jobdetails">
    <b><%=job.getJobId()%></b>
   </td>
   <td class="jobdetails"><b><%=job.getJobName()%></b></td>
   <td class="jobdetails">
	   <code><%=formatTimestamp(job.getLastEndDate(),locale)%></code>
   </td>
   			<td class="jobdetails">
<%
String fileName=job.getFileName();
if (fileName==null){
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

 

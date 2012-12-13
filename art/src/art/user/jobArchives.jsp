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
 <table align="center" width="60%">
  <tr>
  <td class="attr" width="20%"><%=messages.getString("jobId")%></td>
  <td class="attr"><%=messages.getString("jobName")%></td>
  </tr>
  
  <%
  //get job archives user has access to
	Map<Integer,ArtJob> jobs=ue.getJobArchives();
	
    for (Map.Entry<Integer, ArtJob> entry : jobs.entrySet()) {
		ArtJob job=entry.getValue();
  
%>
  <tr>
   <td class="jobdetails">
    <b><%=job.getJobId()%></b>
    <br> <a href="javascript:showHide(document.getElementById('tr_<%=job.getJobId()%>'));">+/-</a>
   </td>
   <td class="jobdetails"><b><%=job.getJobName()%></b></td>  
  </tr> 
  
  <tr id="tr_<%=job.getJobId()%>" class="collapse">
	<td></td>
	<td colspan="1">
			  <table border="0" width="100%">
				  <tr>
					  <td class="data" colspan="2"><%=messages.getString("archives")%></td>
				  </tr>
				  <tr>
					  <td class="attr"  width="20%"><%=messages.getString("runDate")%></td>
					  <td class="attr"><%=messages.getString("result")%></td>
				  </tr>
				  
				  <%
				  //get job archive details
	Map<String,java.util.Date> archives=job.getArchives();
	
    for (Map.Entry<String,java.util.Date> entry2 : archives.entrySet()) {
		String fileName=entry2.getKey();
		java.util.Date endDate=entry2.getValue();
		%>
		<tr>
			<td class="jobdetails">
				<code><%=formatTimestamp(endDate,locale)%></code>
			</td>
			<td class="jobdetails">
<%
if (fileName==null){
		out.println(messages.getString("noFile"));
	} else if (fileName.startsWith("-")) { 
        out.println(fileName.substring(1));
	}  else { 
		resultMessage="";
		if (fileName.indexOf("\n") > -1) {
			// publish jobs can have file link and message separated by newline(\n)
			String result=fileName;
			fileName = StringUtils.substringBefore(fileName, "\n"); //get file name
			fileName = StringUtils.replace(fileName, "\r", ""); //on windows pre-2.5, filenames had \\r\\n
			resultMessage = StringUtils.substringAfter(result, "\n"); //message
		}	
	   %>
        <a type="application/octet-stream" href="<%= request.getContextPath() %>/export/jobs/<%=fileName%>" target="_blank"><%=fileName%> </a>		
     <%
out.println(resultMessage);
}
   %>           

   </td> 
		</tr>
		<% } %>
		
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

 

<%@ page import="java.util.*,java.sql.*,art.utils.*,art.servlets.ArtDBCP,java.io.*,java.net.*" %>
<%@ page import="com.tonbeller.jpivot.olap.query.MdxOlapModel,
	com.tonbeller.wcf.form.FormComponent,
	com.tonbeller.jpivot.table.TableComponent,
	com.tonbeller.jpivot.olap.model.*,
	com.tonbeller.jpivot.tags.OlapModelProxy"
		 %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<% response.setHeader("Cache-control", "no-cache");%>
<%@ include file ="header.jsp" %>

<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib uri="http://www.tonbeller.com/wcf" prefix="wcf" %>


<%

	String title = "";
	String query = "";
	String databaseDriver = "";
	String databaseUrl = "";
	String databaseUser = "";
	String databasePassword = "";
	String schemaFile = "";
	int queryId = 0;
	String currentMdx = "";
	boolean exclusiveAccess = false;

	String jpivotQueryId; //query for use by jpivot


	if (request.getParameter("queryId") == null) {
		Integer i = (Integer) session.getAttribute("pivotQueryId");
		if (i != null) {
			queryId = i.intValue();
		}
	} else {
		queryId = Integer.parseInt(request.getParameter("queryId"));
		session.setAttribute("pivotQueryId", new Integer(queryId));
	}

	jpivotQueryId = "query" + queryId;

	if (request.getParameter("action") == null && request.getParameter("null") == null) {
		//first time we are displaying the pivot table.
		//parameter ?null=null&... used to display the page when settings are changed from the olap navigator toolbar button

		Connection conn = ArtDBCP.getConnection();
		if (conn == null || conn.isClosed()) {
			%>
			<jsp:forward page="error.jsp">
				<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
			</jsp:forward>
			<%
		}

	ArtQuery aq = new ArtQuery();
	boolean queryExists = aq.create(conn, queryId);

	if (!queryExists) {
		conn.close();
		%>
		<jsp:forward page="error.jsp">
			<jsp:param name="MSG" value="Query not found"/>
		</jsp:forward>
		<%
	}

	//check if user has access to this object
	if (StringUtils.equals(aq.getStatus(),"D")) {
		conn.close();
		%>
		<jsp:forward page="error.jsp">
			<jsp:param name="MSG" value="Query disabled. Please contact the ART administrator."/>
		</jsp:forward>
		<%
	}

	//check if user has access
	boolean adminSession = false;
	if (session.getAttribute("AdminSession") != null) {
		adminSession = true;
	}
	PreparedQuery pq = new PreparedQuery();
	String username=ue.getUsername();
	if (!pq.canExecuteQuery(username, queryId, adminSession)) {
		conn.close();
		%>
		<jsp:forward page="error.jsp">
			<jsp:param name="MSG" value="Access denied. Please contact the ART administrator."/>
		</jsp:forward>
		<%
	}

	//check if template file exists
	int queryType = aq.getQueryType();
	String template = aq.getTemplate();
	File templateFile = new File(ArtDBCP.getTemplatesPath() + template);
	if (queryType == 112 && !templateFile.exists()) {
		conn.close();
		%>
		<jsp:forward page="error.jsp">
			<jsp:param name="MSG" value="Template file not found. Please contact the ART administrator."/>
		</jsp:forward>
		<%
	}

	schemaFile = ArtDBCP.getRelativeTemplatesPath() + template;

	//put title in session. may be lost if olap navigator option on jpivot toolbar is used
	title = aq.getName();
	session.setAttribute("pivotTitle" + queryId, title);

	art.dbcp.DataSource ds = ArtDBCP.getDataSource(aq.getDatabaseId());

	databaseUrl = ds.getUrl().trim();
	databaseUser = ds.getUsername().trim();
	databasePassword = ds.getPassword();
	databaseDriver = ds.getDriver().trim();

	//get mdx to use, with parameter values substituted
	Map<String,String[]> multiParams   = new HashMap<String,String[]>();	
	Map<String,String> inlineParams  = new HashMap<String,String>();
	ParameterProcessor.processParameters(request, inlineParams, multiParams, queryId);

	pq.setUsername(username);
	pq.setQueryId(queryId);
	pq.setAdminSession(adminSession);	
	pq.setMultiParams(multiParams);	
	pq.setInlineParams(inlineParams);

	pq.execute();
	query=pq.getPreparedStatementSQL();

	//check if this is the only user who has access. if so, he can overwrite the pivot table view with a different view
	exclusiveAccess = aq.exclusiveAccess(conn, username);

	//save status in the session. will be lost as navigation is done on the pivot table
	session.setAttribute("pivotExclusiveAccess" + queryId, new Boolean(exclusiveAccess));


	String xmlaDatasource = aq.getXmlaDatasource();
	String xmlaCatalog = aq.getXmlaCatalog();
	String xmlaUrl = aq.getXmlaUrl();

	if(queryType==113 || queryType==114){
		//construct xmla url to incoporate username and password if present
		String xmlaUsername=aq.getXmlaUsername();
		String xmlaPassword=aq.getXmlaPassword();
		xmlaPassword=Encrypter.decrypt(xmlaPassword);
		URL url;

		try {
			url = new URL(xmlaUrl);
			if(StringUtils.length(xmlaUsername)>0){
				xmlaUrl=url.getProtocol() + "://" + xmlaUsername;
				if (StringUtils.length(xmlaPassword)>0) {
					xmlaUrl += ":" + xmlaPassword;
				}
				int port=url.getPort();
				if(port==-1){
					//no port specified
					xmlaUrl += "@" + url.getHost() + url.getPath();
				} else {
					xmlaUrl += "@" + url.getHost() + ":" + port + url.getPath();
				}
			}
		} catch (MalformedURLException e) {
			System.err.println("Invalid xmla url: " + e);
			e.printStackTrace();
		}
	}

	String roles;
	roles = aq.getMondrianRoles(conn, username);

	conn.close();

	if (queryType == 112) {
		//mondrian query
%>

<jp:mondrianQuery id="<%=jpivotQueryId%>" jdbcDriver="<%=databaseDriver%>"
jdbcUrl="<%=databaseUrl%>" jdbcUser="<%=databaseUser%>" jdbcPassword="<%=databasePassword%>" catalogUri="<%=schemaFile%>" role="<%=roles%>">
	<%=query%>
</jp:mondrianQuery>

<% } else if (queryType == 113) {
	//mondrian via xmla

	//prepend provider if only datasource name provided
	if (xmlaDatasource != null) {
		String tmp = xmlaDatasource.toLowerCase();
		if (!tmp.startsWith("provider=mondrian")) {
			xmlaDatasource = "Provider=Mondrian;DataSource=" + xmlaDatasource; //datasource name in datasources.xml file must be exactly the same
		}
	}
%>
<jp:xmlaQuery id="<%=jpivotQueryId%>" uri="<%=xmlaUrl%>"
dataSource="<%=xmlaDatasource%>" catalog="<%=xmlaCatalog%>">
	<%=query%>
</jp:xmlaQuery>

<% } else if (queryType == 114) {
	//sql server analysis services via xmla
	xmlaDatasource = "Provider=MSOLAP";
%>
<jp:xmlaQuery id="<%=jpivotQueryId%>" uri="<%=xmlaUrl%>"
dataSource="<%=xmlaDatasource%>" catalog="<%=xmlaCatalog%>" >
	<%=query%>
</jp:xmlaQuery>

<% }
	}
%>


<%
//get title from session
	title = (String) session.getAttribute("pivotTitle" + queryId);

//get exclusive access status from the session
	Boolean accessBoolean = (Boolean) session.getAttribute("pivotExclusiveAccess" + queryId);
	exclusiveAccess = accessBoolean.booleanValue();


//set identifiers for jpivot objects
	String tableId = "table" + queryId;
	String mdxEditId = "mdxedit" + queryId;
	String printId = "print" + queryId;
	String printFormId = "printform" + queryId;
	String navigatorId = "navi" + queryId;
	String sortFormId = "sortform" + queryId;
	String chartId = "chart" + queryId;
	String chartFormId = "chartform" + queryId;
	String toolbarId = "toolbar" + queryId;

	String queryDrillThroughTable = jpivotQueryId + ".drillthroughtable";

	String modelQueryId = "#{" + jpivotQueryId + "}";
	String modelTableId = "#{" + tableId + "}";
	String modelPrintId = "#{" + printId + "}";
	String modelChartId = "#{" + chartId + "}";

	String mdxEditVisible = "#{" + mdxEditId + ".visible}";
	String navigatorVisible = "#{" + navigatorId + ".visible}";
	String sortFormVisible = "#{" + sortFormId + ".visible}";
	String tableLevelStyle = "#{" + tableId + ".extensions.axisStyle.levelStyle}";
	String tableHideSpans = "#{" + tableId + ".extensions.axisStyle.hideSpans}";
	String tableShowProperties = "#{" + tableId + ".rowAxisBuilder.axisConfig.propertyConfig.showProperties}";
	String tableNonEmptyButtonPressed = "#{" + tableId + ".extensions.nonEmpty.buttonPressed}";
	String tableSwapAxesButtonPressed = "#{" + tableId + ".extensions.swapAxes.buttonPressed}";
	String tableDrillMemberEnabled = "#{" + tableId + ".extensions.drillMember.enabled}";
	String tableDrillPositionEnabled = "#{" + tableId + ".extensions.drillPosition.enabled}";
	String tableDrillReplaceEnabled = "#{" + tableId + ".extensions.drillReplace.enabled}";
	String tableDrillThroughEnabled = "#{" + tableId + ".extensions.drillThrough.enabled}";
	String chartVisible = "#{" + chartId + ".visible}";
	String chartFormVisible = "#{" + chartFormId + ".visible}";
	String printFormVisible = "#{" + printFormId + ".visible}";

	String printExcel = request.getContextPath() + "/Print?cube=" + queryId + "&type=0";
	String printPdf = request.getContextPath() + "/Print?cube=" + queryId + "&type=1";

//get the current mdx
	TableComponent table = (TableComponent) session.getAttribute(tableId);
	if (table != null) {
		OlapModel olapModel = table.getOlapModel();
		while (olapModel != null) {
			if (olapModel instanceof OlapModelProxy) {
				OlapModelProxy proxy = (OlapModelProxy) olapModel;
				olapModel = proxy.getDelegate();
			}
			if (olapModel instanceof OlapModelDecorator) {
				OlapModelDecorator decorator = (OlapModelDecorator) olapModel;
				olapModel = decorator.getDelegate();
			}
			if (olapModel instanceof MdxOlapModel) {
				MdxOlapModel model = (MdxOlapModel) olapModel;
				currentMdx = model.getCurrentMdx();
				olapModel = null;
			}
		}
	}

//save current mdx in the session
	session.setAttribute("mdx" + queryId, currentMdx);

//get object with olap query and result
	OlapModel _olapModel = (OlapModel) session.getAttribute(jpivotQueryId);

%>


<table class="pivot" align="center" width="50%">
	<tr> <td class="title">
			<b> <br /> <%=title%> </b> <br /> <br />
		</td> </tr>

	<tr><td>
			<br />

			<form action="showAnalysis.jsp" method="post">
				<input type="hidden" name="action" value="edit">
				<input type="hidden" name="queryId" value="<%=queryId%>">

				<%-- define table, navigator and forms --%>
				<wcf:scroller />

				<jp:table id="<%=tableId%>" query="<%=modelQueryId%>"/>
				<jp:navigator id="<%=navigatorId%>" query="<%=modelQueryId%>" visible="false"/>
				<wcf:form id="<%=mdxEditId%>" xmlUri="/WEB-INF/jpivot/table/mdxedit.xml" model="<%=modelQueryId%>" visible="false"/>
				<wcf:form id="<%=sortFormId%>" xmlUri="/WEB-INF/jpivot/table/sortform.xml" model="<%=modelTableId%>" visible="false"/>

				<jp:print id="<%=printId%>"/>
				<wcf:form id="<%=printFormId%>" xmlUri="/WEB-INF/jpivot/print/printpropertiesform.xml" model="<%=modelPrintId%>" visible="false"/>

				<jp:chart id="<%=chartId%>" query="<%=modelQueryId%>" visible="false"/>
				<wcf:form id="<%=chartFormId%>" xmlUri="/WEB-INF/jpivot/chart/chartpropertiesform.xml" model="<%=modelChartId%>" visible="false"/>
				<wcf:table id="<%=queryDrillThroughTable%>" visible="false" selmode="none" editable="true"/>


				<%-- define a toolbar --%>
				<wcf:toolbar id="<%=toolbarId%>" bundle="com.tonbeller.jpivot.toolbar.resources">
					<wcf:scriptbutton id="cubeNaviButton" tooltip="toolb.cube" img="cube" model="<%=navigatorVisible%>"/>
					<wcf:scriptbutton id="mdxEditButton" tooltip="toolb.mdx.edit" img="mdx-edit" model="<%=mdxEditVisible%>"/>
					<wcf:scriptbutton id="sortConfigButton" tooltip="toolb.table.config" img="sort-asc" model="<%=sortFormVisible%>"/>
					<wcf:separator/>
					<wcf:scriptbutton id="levelStyle" tooltip="toolb.level.style" img="level-style" model="<%=tableLevelStyle%>"/>
					<wcf:scriptbutton id="hideSpans" tooltip="toolb.hide.spans" img="hide-spans" model="<%=tableHideSpans%>"/>
					<wcf:scriptbutton id="propertiesButton" tooltip="toolb.properties"  img="properties" model="<%=tableShowProperties%>"/>
					<wcf:scriptbutton id="nonEmpty" tooltip="toolb.non.empty" img="non-empty" model="<%=tableNonEmptyButtonPressed%>"/>
					<wcf:scriptbutton id="swapAxes" tooltip="toolb.swap.axes"  img="swap-axes" model="<%=tableSwapAxesButtonPressed%>"/>
					<wcf:separator/>
					<wcf:scriptbutton model="<%=tableDrillMemberEnabled%>"	 tooltip="toolb.navi.member" radioGroup="navi" id="drillMember"   img="navi-member"/>
					<wcf:scriptbutton model="<%=tableDrillPositionEnabled%>" tooltip="toolb.navi.position" radioGroup="navi" id="drillPosition" img="navi-position"/>
					<wcf:scriptbutton model="<%=tableDrillReplaceEnabled%>"	 tooltip="toolb.navi.replace" radioGroup="navi" id="drillReplace"  img="navi-replace"/>
					<wcf:scriptbutton model="<%=tableDrillThroughEnabled%>"  tooltip="toolb.navi.drillthru" id="drillThrough01"  img="navi-through"/>
					<wcf:separator/>
					<wcf:scriptbutton id="chartButton01" tooltip="toolb.chart" img="chart" model="<%=chartVisible%>"/>
					<wcf:scriptbutton id="chartPropertiesButton01" tooltip="toolb.chart.config" img="chart-config" model="<%=chartFormVisible%>"/>
					<wcf:separator/>
					<wcf:scriptbutton id="printPropertiesButton01" tooltip="toolb.print.config" img="print-config" model="<%=printFormVisible%>"/>
					<wcf:imgbutton id="printpdf" tooltip="toolb.print" img="print" href="<%=printPdf%>" />
					<wcf:imgbutton id="printxls" tooltip="toolb.excel" img="excel" href="<%=printExcel%>" />
				</wcf:toolbar>

				<%-- render toolbar --%>
				<wcf:render ref="<%=toolbarId%>" xslUri="/WEB-INF/jpivot/toolbar/htoolbar.xsl" xslCache="true" />

				<p>

					<%-- if there was an overflow, show error message --%>
					<%
						if (_olapModel != null) {
							try {
								_olapModel.getResult();
								if (_olapModel.getResult().isOverflowOccured()) {
					%><p><strong style="color: red">Resultset overflow occurred</strong></p><%			}
							} catch (Throwable t) {
								t.printStackTrace();
				%><p><strong style="color: red">Error Occurred While getting Resultset</strong></p><%
								  }
							  }
				%>

				</p>


				<%-- render navigator --%>
				<wcf:render ref="<%=navigatorId%>" xslUri="/WEB-INF/jpivot/navi/navigator.xsl" xslCache="true" />

				<%-- edit mdx --%>
				<%
				FormComponent _mdxEdit = (FormComponent) session.getAttribute(mdxEditId);
				if (_mdxEdit.isVisible()) {%>
				<wcf:render ref="<%=mdxEditId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>
				<% }%>

				<%-- sort properties --%>
				<wcf:render ref="<%=sortFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

				<%-- chart properties --%>
				<wcf:render ref="<%=chartFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

				<%-- print properties --%>
				<wcf:render ref="<%=printFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>

				<!-- render the table -->
				<p> <br />
					<wcf:render ref="<%=tableId%>" xslUri="/WEB-INF/jpivot/table/mdxtable.xsl" xslCache="true"/>
				</p>

				<p>
					Filter:
					<wcf:render ref="<%=tableId%>" xslUri="/WEB-INF/jpivot/table/mdxslicer.xsl" xslCache="true"/>
				</p>

				<p>
					<!-- drill through table -->
					<wcf:render ref="<%=queryDrillThroughTable%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true"/>
				</p>

				<p>
					<!-- render chart -->
					<wcf:render ref="<%=chartId%>" xslUri="/WEB-INF/jpivot/chart/chart.xsl" xslCache="true"/>
				</p>

			</form>
			<br />
		</td></tr>

	<tr><td class="info">
			<i><%=messages.getString("saveCurrentView")%></i>
			<br>

			<form method="post" action="saveAnalysis.jsp">
				<input type="hidden" name="pivotQueryId" value="<%=queryId%>" />
				<table>
					<tr><td>
							<%=messages.getString("newViewName")%>
						</td>
						<td>
							<input type="text" name="newPivotName" value="" size="20" maxlength="25" />
							<% if (exclusiveAccess) {%>
							<input type="checkbox" name="overwrite" /><%=messages.getString("overwrite")%> &nbsp;
							<input type="checkbox" name="delete" /><%=messages.getString("delete")%> &nbsp;
							<%}%>
							<input id="save" type="submit" class="buttonup" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" value="<%=messages.getString("save")%>" />
						</td>
					</tr>
					<tr><td>
							<%=messages.getString("newViewDescription")%>
						</td>
						<td>
							<input type="text" name="newPivotDescription" value="" size="45" maxlength="2000">
						</td>
					</tr>
				</table>
			</form>

		</td>
	</tr>
</table>

<%@ include file ="footer.jsp" %>

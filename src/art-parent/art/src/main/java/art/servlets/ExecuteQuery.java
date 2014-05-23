/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.servlets;

import art.report.PreparedQuery;
import art.graph.*;
import art.output.*;
import art.utils.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to start off the query execution process and display the results.
 * 
 * Purpose: 1. Execute the query (on the right database...) 2. build the html
 * page to show the data or create the spreadsheet or exportable file and show
 * the link to retrieve it or forward to the page to create the graph 3. Write
 * log file with statistics
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ExecuteQuery extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ExecuteQuery.class);
	private int currentNumberOfRunningQueries = 0;
	@SuppressWarnings("rawtypes")
	HashMap<String, java.lang.Class> viewModes;

	/**
	 *
	 * @param config
	 * @throws ServletException
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		//load all view modes
		List<String> allViewModes = ArtConfig.getAllReportFormats();
		viewModes = new HashMap<String, java.lang.Class>(allViewModes.size());
		ClassLoader cl = this.getClass().getClassLoader();
		String vm = "";
		try {
			for (String viewMode : allViewModes) {
				vm = viewMode;
				viewModes.put(vm, cl.loadClass("art.output." + vm + "Output"));
			}
		} catch (Exception e) {
			logger.error("Error while loading view mode: {}", vm, e);
		}
	}

	
	/**
	 * Start the query execution process and display the output.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		request.setCharacterEncoding("UTF-8"); // THIS MUST BE THE FIRST LINE
		// make sure your server knows input is codified in UTF-8
		// note: on Tomcat you need to set the URIEncoding="UTF-8" on tomcat connector in server.xml


		// check if the html code should be rendered as an html fragmnet (without <html> and </html> tags)
		boolean isFragment;
		if (request.getParameter("_isFragment") != null) {
			isFragment = true;
		} else {
			isFragment = false;
		}

		// make sure the browser does not cache the result using Ajax (this happens in IE)
		if (isFragment) {
			response.setHeader("Cache-control", "no-cache");
		}

		// check if output is being displayed within the showparams page
		boolean isInline = false;
		if (request.getParameter("_isInline") != null) {
			isInline = true;
		}

		PrintWriter out = null; // this will be initialized according to the content type of the view mode
		ResourceBundle messages = ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());

		ServletContext ctx = getServletConfig().getServletContext();
		String baseExportPath = ArtConfig.getExportPath();
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");

		//generate output
		if (username == null) {
			//invalid session
			request.setAttribute("errorMessage", messages.getString("invalidSession"));
			ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
		} else {
			// the session is valid if the username is not null

			/*
			 * isFlushEnabled states if this servlet can produce html output.
			 * This is used to avoid to print on the page if the page control
			 * has to be redirected to another page (this is the case of graphs
			 * and output modes that do not generate html)
			 */
			boolean showHeaderAndFooter = true; // default generic HTML output
			ReportOutputInterface o = null;
			String str; //for error logging strings

			//get query details. don't declare query variables at class level. causes issues with dashboards            
			int queryId = 0;
			int queryType;
			String queryName;
			String xAxisLabel;
			String yAxisLabel;
			String graphOptions;
			String shortDescription;

			//support queryId as well as existing QUERY_ID
			String queryIdString = request.getParameter("queryId");
			String queryIdString2 = request.getParameter("QUERY_ID");
			if (queryIdString != null) {
				queryId = Integer.parseInt(queryIdString);
			} else if (queryIdString2 != null) {
				queryId = Integer.parseInt(queryIdString2);
			}

			try {
				Connection conn = ArtConfig.getConnection();
				ArtQuery aq = new ArtQuery();
				aq.create(conn, queryId);

				queryName = aq.getName();
				queryType = aq.getQueryType();
				xAxisLabel = aq.getXaxisLabel();
				yAxisLabel = aq.getYaxisLabel();
				graphOptions = aq.getGraphOptions();
				shortDescription = aq.getShortDescription();

				conn.close();
			} catch (Exception e) {
				logger.error("Error while getting query details", e);
				request.setAttribute("errorMessage", "Error while getting query details: " + e);
				ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
				return;
			}

			request.setAttribute("queryName", queryName);

			String viewMode = request.getParameter("viewMode");
			if (viewMode == null || StringUtils.equalsIgnoreCase(viewMode, "default")) {
				if (queryType < 0) {
					//graph
					viewMode = "graph";
				} else if (queryType > 0 && queryType < 100) {
					//group
					viewMode = "htmlreport";
				} else if (queryType == 115 || queryType == 116) {
					//jasper report
					viewMode = "pdf";
				} else {
					viewMode = "html";
				}
			}

			//ensure html only queries only output as html
			if (queryType == 102 || queryType == 103) {
				//crosstab html only or normal query html only
				viewMode = "html";
			}

			/*
			 * Find if output allows this servlet to print the header&footer
			 * (flush active or not)
			 */
			if (StringUtils.equalsIgnoreCase(viewMode, "SCHEDULE")) {
				// forward to the editJob page
				ctx.getRequestDispatcher("/user/editJob.jsp").forward(request, response);
				return; // a return is needed otherwise the flow would proceed!
			} else if (StringUtils.containsIgnoreCase(viewMode, "graph")) {
				showHeaderAndFooter = false; // graphs are created in memory and displayed by showGraph.jsp page
			} else if (StringUtils.equalsIgnoreCase(viewMode, "htmlreport")) {
				response.setContentType("text/html; charset=UTF-8");
				out = response.getWriter();
			} else if (queryType == 115 || queryType == 116 || queryType == 117 || queryType == 118) {
				//jasper report or jxls spreadsheet
				response.setContentType("text/html; charset=UTF-8");
				out = response.getWriter();
			} else if (queryType == 100) {
				//update query
				response.setContentType("text/html; charset=UTF-8");
				out = response.getWriter();
			} else if (queryType == 110) {
				// forward to the showDashboard page
				ctx.getRequestDispatcher("/user/showDashboard.jsp").forward(request, response);
				return; // a return is needed otherwise the flow would proceed!
			} else if (queryType == 111) {
				// forward to the showText page
				ctx.getRequestDispatcher("/user/showText.jsp").forward(request, response);
				return; // a return is needed otherwise the flow would proceed!
			} else if (queryType == 112 || queryType == 113 || queryType == 114) {
				// forward to the showAnalysis page
				ctx.getRequestDispatcher("/user/showAnalysis.jsp").forward(request, response);
				return; // a return is needed otherwise the flow would proceed!
			} else {
				// This is not a request to schedule, produce a graph or an "htmlReport" or an update query
				// => Load the appropriate ReportOutputInterface for the view mode

				try {
					@SuppressWarnings("rawtypes")
					java.lang.Class classx = viewModes.get(viewMode);
					o = (ReportOutputInterface) classx.newInstance();

					// Set the content type according to the object
					String contentType = o.getContentType();
					response.setContentType(contentType);
					// initialize the output stream - this is here
					// this need to appear after response.setContent!!!
					out = response.getWriter();

					// the view mode drives if this servlet uses the std header&footer,
					// if false the view mode needs to take care of all the output
					showHeaderAndFooter = o.isShowQueryHeaderAndFooter();

					if (isFragment) {
						showHeaderAndFooter = false; // disable in any case header&footer if isFragment is true
					}

				} catch (Exception e) {
					logger.error("Error while instantiating class: {}", viewMode, e);
					request.setAttribute("errorMessage", "Error while initializing " + viewMode + " view mode:" + e);
					ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
					return;
				}
			}

			if (showHeaderAndFooter) {
				ctx.getRequestDispatcher("/user/queryHeader.jsp").include(request, response);
				out.flush();
			}


			//run query            
			if (currentNumberOfRunningQueries <= ArtConfig.getSettings().getMaxRunningReports()) {
				int probe = 0; // used for debugging
				int numberOfRows = -1; //default to -1 in order to accomodate template reports for which you can't know the number of rows in the report

				ResultSet rs = null;
				PreparedQuery pq = null;

				try {
					ResultSetMetaData rsmd;
					long startQueryTime;
					long endQueryTime;
					long startTime = new java.util.Date().getTime(); //overall start time                    
					String startTimeString = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM, request.getLocale()).format(new java.util.Date(startTime)); //for display in query output header

					/*
					 * Increment the currentNumberOfRunningQueries Note if an
					 * exception kills the current thread the value may be not
					 * decremented correctly This value is shown in the the
					 * "Show Connection Status" link from the Datasource page
					 * (ART Admin part)
					 */
					currentNumberOfRunningQueries++;

					/*
					 * ***********************************
					 * BEGIN: Create the PreparedQuery object and feed it (this
					 * obj is a "wrapper" around the SQL string with some
					 * methods to act on it to apply rules, multi params etc)
					 */

					probe = 10;
					boolean adminSession = false;
					// check if the servlet has been called from the admin session
					if (session.getAttribute("AdminSession") != null) {
						adminSession = true;
					}

					pq = new PreparedQuery();
					pq.setUsername(username);
					pq.setQueryId(queryId);
					pq.setAdminSession(adminSession);


					/**
					 * ***************************************************************************
					 *
					 * ON QUERY PARAMETERS
					 *
					 * INLINE parameters has the format P_<param_name> and the
					 * #param_name# values are substituted in the SQL query
					 * before executing it.
					 *
					 * A MULTI parameter name begins with the sting 'M_'
					 * followed by the column name The following string is
					 * created AND MULTIPLE_FIELD_NAME in ('value1', 'value2',
					 * ..., 'valueN') and inserted on the prepared statement
					 * (handling the case where we have a GROUP BY expression)
					 *
					 * BIND parameters are deprecated, us INILINE instead BIND
					 * parameter name begins with the string 'Py' where y is the
					 * ? index on the prepared statement. For example P3=HELLO
					 * means the third ? on the prepared statement will be
					 * substituted with the HELLO string (the same for VARCHAR,
					 * INTEGER, NUMBER ...) For dates, there are 3 params to
					 * store each date parameter: day, month and year Py_days or
					 * Py_month or Py_year (for Dec 12, 2008 => P3_days = 12,
					 * P3_month = 3, P3_year=2008)
					 *
					 * The first step is to build the parameter lists
					 * ***************************************************************************
					 */
					probe = 40;

					/*
					 * *************************************
					 * BEGIN: Build parameters hash tables
					 */

					Map<String, String[]> multiParams = new HashMap<String, String[]>();
					Map<String, String> inlineParams = new HashMap<String, String>();

					ArtQuery aq = new ArtQuery();
					Map<String, ArtQueryParam> htmlParams = aq.getHtmlParams(queryId);

					//set default parameter values. so that they don't have to be specified on the url
					if (!htmlParams.isEmpty()) {
						for (Map.Entry<String, ArtQueryParam> entry : htmlParams.entrySet()) {
							ArtQueryParam param = entry.getValue();
							if (StringUtils.equals(param.getParamType(), "I")) {
								inlineParams.put(param.getParamLabel(), (String) param.getParamValue());
							}

						}
					}

					/*
					 * ParameterProcessor has a static function to parse the
					 * request and fill the hashmaps that store the parameters
					 */
					Map<Integer, ArtQueryParam> displayParams = ParameterProcessor.processParameters(request, inlineParams, multiParams, queryId, htmlParams);

					//set showparams flag. flag not only determined by presense of _showParams. may also be true if query set to always show params
					boolean showParams = false;
					if (!displayParams.isEmpty()) {
						showParams = true;
					}

					// Set the hash tables in the pq object
					pq.setMultiParams(multiParams);
					pq.setInlineParams(inlineParams);

					pq.setHtmlParams(htmlParams);

					/*
					 * END ***********************************
					 */

					probe = 50;

					int resultSetType;
					if (queryType == 116 || queryType == 118) {
						resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE; //need scrollable resultset in order to determine record count
					} else if (StringUtils.equalsIgnoreCase(viewMode, "htmlreport")) {
						resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
					} else if (queryType < 0) {
						resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE; //need scrollable resultset for graphs for show data option
					} else {
						resultSetType = ResultSet.TYPE_FORWARD_ONLY;
					}

					// JavaScript code to write status
					if (showHeaderAndFooter) {
						out.println("<script type=\"text/javascript\">writeStatus(\"" + messages.getString("queryExecuting") + "\");</script>");
						out.flush();
					}


					startQueryTime = new java.util.Date().getTime();

					/*
					 * *************
					 ***************
					 *****RUN IT**** ************** *************
					 */

					pq.execute(resultSetType);

					probe = 75;

					/*
					 * *************
					 ***************
					 ***************
					 **************
					 */

					endQueryTime = new java.util.Date().getTime();


					//get final sql with parameter placeholders replaced with parameter values
					String finalSQL = pq.getFinalSQL();

					//determine if final sql should be shown. only admins can see sql
					boolean showSQL = false;
					int accessLevel = 0;
					UserEntity ue = (UserEntity) session.getAttribute("ue");
					if (ue != null) {
						accessLevel = ue.getAccessLevel();
					}
					if (request.getParameter("_showSQL") != null && accessLevel > 5) {
						showSQL = true;
					}

					//set output object properties if required
					if (o != null) {
						try {
							probe = 77;

							o.setMaxRows(ArtConfig.getMaxRows(viewMode));
							o.setWriter(out);
							o.setQueryName(queryName);
							o.setFileUserName(username);
							o.setExportPath(baseExportPath);

							//don't set displayparams for html view modes. parameters will be displayed by this servlet
							if (!StringUtils.containsIgnoreCase(viewMode, "html")) {
								o.setDisplayParameters(displayParams);
							}

							//ensure htmlplain output doesn't display parameters if inline
							if (o instanceof htmlPlainOutput) {
								htmlPlainOutput hpo = (htmlPlainOutput) o;
								hpo.setDisplayInline(isInline);

								//ensure parameters are displayed if not in inline mode
								hpo.setDisplayParameters(displayParams);
							}

							//enable localization for datatable output
							if (o instanceof htmlDataTableOutput) {
								htmlDataTableOutput dt = (htmlDataTableOutput) o;
								dt.setLocale(request.getLocale());
							}

						} catch (Exception e) {
							logger.error("Error setting properties for class: {}", viewMode, e);
							request.setAttribute("errorMessage", "Error while initializing " + viewMode + " view mode:" + e);
							ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
							return;
						}
					}

					// display status information, parameters and final sql
					if (showHeaderAndFooter) {
						out.println("<script type=\"text/javascript\">writeStatus(\"" + messages.getString("queryFetching") + "\");</script>");
						String description = "";
						shortDescription = StringUtils.trim(shortDescription);
						if (StringUtils.length(shortDescription) > 0) {
							description = " :: " + shortDescription;
						}
						out.println("<script type=\"text/javascript\">writeInfo(\"<b>" + queryName + "</b>" + description + " :: " + startTimeString + "\");</script>");

						//display parameters
						if (showParams) {
							ReportOuputtHandler.displayParameters(out, displayParams, messages);
						}

						//display final sql
						if (showSQL) {
							ReportOuputtHandler.displayFinalSQL(out, finalSQL);
						}

						out.flush();
					}
					probe = 90;

					//handle jasper report output
					if (queryType == 115 || queryType == 116) {
						probe = 91;
						jasperOutput jasper = new jasperOutput();
						jasper.setQueryName(queryName);
						jasper.setFileUserName(username);
						jasper.setExportPath(baseExportPath);
						jasper.setOutputFormat(viewMode);
						jasper.setWriter(out);
						if (queryType == 115) {
							//report will use query in the report template
							jasper.createFile(null, queryId, inlineParams, multiParams, htmlParams);
						} else {
							//report will use data from art query
							rs = pq.getResultSet();
							jasper.createFile(rs, queryId, inlineParams, multiParams, htmlParams);
							numberOfRows = getNumberOfRows(rs);
						}
					} else if (queryType == 117 || queryType == 118) {
						//jxls spreadsheet
						probe = 92;
						jxlsOutput jxls = new jxlsOutput();
						jxls.setQueryName(queryName);
						jxls.setFileUserName(username);
						jxls.setExportPath(baseExportPath);
						jxls.setWriter(out);
						if (queryType == 117) {
							//report will use query in the jxls template
							jxls.createFile(null, queryId, inlineParams, multiParams, htmlParams);
						} else {
							//report will use data from art query
							rs = pq.getResultSet();
							jxls.createFile(rs, queryId, inlineParams, multiParams, htmlParams);
							numberOfRows = getNumberOfRows(rs);
						}
					} else {
						//get query results
						rs = pq.getResultSet();

						if (rs != null) {
							// it is a "select" query or a procedure ending with a select statement
							probe = 93;
							rsmd = rs.getMetaData();

							try {
								if (StringUtils.equalsIgnoreCase(viewMode, "htmlreport")) {
									/*
									 * HTML REPORT
									 */
									int splitCol;
									if (request.getParameter("SPLITCOL") == null) {
										splitCol = queryType;
									} else {
										splitCol = Integer.parseInt(request.getParameter("SPLITCOL"));
									}
									numberOfRows = htmlReportOut(out, rs, rsmd, splitCol);
									probe = 100;
								} else if (StringUtils.containsIgnoreCase(viewMode, "graph")) {
									/*
									 * GRAPH
									 */
									probe = 105;

									//do initial preparation of graph object
									ArtGraph ag = artGraphOut(rsmd, request, graphOptions, shortDescription, queryType);

									//set some graph properties
									ag.setXAxisLabel(xAxisLabel);
									if (yAxisLabel == null) {
										yAxisLabel = rsmd.getColumnLabel(1);
									}
									ag.setYAxisLabel(yAxisLabel);

									if (showParams) {
										request.setAttribute("showParams", "true");
										ag.setDisplayParameters(displayParams);
									}

									//add drill down queries
									Map<Integer, DrilldownQuery> drilldownQueries = aq.getDrilldownQueries(queryId);

									//build graph dataset
									ag.prepareDataset(rs, drilldownQueries, inlineParams, multiParams);

									//set other properties relevant for the graph display
									if (showSQL) {
										request.setAttribute("showSQL", "true");
										request.setAttribute("finalSQL", finalSQL);
									}

									//enable file names to contain query name
									//set base file name
									java.util.Date today = new java.util.Date();
									SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
									String filename = username + "-" + queryName + "-" + dateFormatter.format(today) + ArtUtils.getRandomFileNameString();
									filename = ArtUtils.cleanFileName(filename);
									request.setAttribute("baseFileName", filename);

									//allow direct url not to need _query_id
									request.setAttribute("queryType", Integer.valueOf(queryType));

									//allow use of both QUERY_ID and queryId in direct url
									request.setAttribute("queryId", Integer.valueOf(queryId));

									//pass graph object
									request.setAttribute("artGraph", ag);

									probe = 110;
								} else {
									if (queryType == 101 || queryType == 102) {
										/*
										 * CROSSTAB
										 */
										numberOfRows = ReportOuputtHandler.flushXOutput(messages, o, rs, rsmd);
									} else {
										/*
										 * NORMAL TABULAR OUTPUT
										 */

										//add support for drill down queries
										Map<Integer, DrilldownQuery> drilldownQueries = null;
										if (StringUtils.containsIgnoreCase(viewMode, "html")) {
											//only drill down for html output. drill down query launched from hyperlink                                            
											drilldownQueries = aq.getDrilldownQueries(queryId);
										}
										numberOfRows = ReportOuputtHandler.flushOutput(messages, o, rs, rsmd, drilldownQueries, request.getContextPath(), inlineParams, multiParams);
									}
									probe = 130;
								}

							} catch (ArtException e) { // "known" exceptions
								throw new ArtException(e.getMessage());
							} catch (Exception e) {    // other "unknown" exceptions                                
								str = "Error while running query ID " + queryId + ", execution for user " + username + ", for session id " + session.getId() + ", at position: " + probe;
								logger.error("Error: {}", str, e);
								throw new ArtException(messages.getString("queryFetchException") + " <br>" + messages.getString("step") + ": " + probe + "<br>" + messages.getString("details") + "<code> " + e + "</code><br>" + messages.getString("contactSupport") + "</p>");
							}

						} else {
							//this is an update query
							int rowsUpdated = pq.getUpdateCount(); // will be -1 if query has multiple statements
							request.setAttribute("rowsUpdated", "" + rowsUpdated);
							ctx.getRequestDispatcher("/user/updateExecuted.jsp").include(request, response);
						}
					}

					// Print the "working" time elapsed
					// The "working"  time elapsed is the time elapsed
					// from the when the query is created  (doPost()) to now (endTime).
					// The time elapsed from a user perspective can be bigger because the servlet output
					// is "cached and transmitted" over the network by the servlet engine.

					long endTime = new java.util.Date().getTime();

					long totalTime = (endTime - startTime) / (1000);
					long fetchTime = (endQueryTime - startQueryTime) / (1000);
					double preciseTotalTime = (endTime - startTime) / (double) 1000;
					NumberFormat nf = NumberFormat.getInstance(request.getLocale());
					DecimalFormat df = (DecimalFormat) nf;
					df.applyPattern("#,##0.0##");

					if (showHeaderAndFooter) {
						request.setAttribute("timeElapsed", df.format(preciseTotalTime) + " " + messages.getString("seconds"));
						if (numberOfRows == -1) {
							request.setAttribute("numberOfRows", "Unknown");
						} else {
							df.applyPattern("#,##0");
							request.setAttribute("numberOfRows", df.format(numberOfRows));
						}
						ctx.getRequestDispatcher("/user/queryFooter.jsp").include(request, response);
					}

					ArtHelper.log(username, "query", request.getRemoteAddr(), queryId, totalTime, fetchTime, "query, " + viewMode);
					probe = 200;

					if (StringUtils.containsIgnoreCase(viewMode, "graph")) {
						//graph. set output format and forward to the rendering page
						if (StringUtils.equalsIgnoreCase(viewMode, "graph")) {
							//only display graph on the browser
							request.setAttribute("outputToFile", "nofile");
						} else if (StringUtils.equalsIgnoreCase(viewMode, "pdfgraph")) {
							//additionally generate graph in a pdf file
							request.setAttribute("outputToFile", "pdf");
						} else if (StringUtils.equalsIgnoreCase(viewMode, "pnggraph")) {
							//additionally generate graph as a png file
							request.setAttribute("outputToFile", "png");
						}

						ctx.getRequestDispatcher("/user/showGraph.jsp").forward(request, response);
					}

				} catch (Exception e) { // we can't dispatch this error to a new page since we need to know if this servlet has already flushed something                    
					str = "Error while running query ID " + queryId + ", execution for user " + username + ", for session id " + session.getId() + ", at position: " + probe;
					logger.error("Error: {}", str, e);

					request.setAttribute("errorMessage", messages.getString("anException") + "<hr>" + messages.getString("step") + ":" + probe + "<br><code>" + e + "</code>");
					if (showHeaderAndFooter) { // we already flushed something: let's include the page
						request.setAttribute("headerOff", "true");
						ctx.getRequestDispatcher("/user/error.jsp").include(request, response);
					} else { // let's forward to the error page
						ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
					}
					// do not put a return here otherwise we risk the open connection to be maintained out of the pool

				} finally {
					// Decrement the currentNumberOfRunningQueries
					// close statements and return the connection to the pool
					try {
						if (rs != null) {
							rs.close();
						}
						if (pq != null) {
							pq.close();
						}
						currentNumberOfRunningQueries--;
					} catch (SQLException e) {
						currentNumberOfRunningQueries--;
						logger.error("Error while closing connections", e);
					}
				}

			} else { //  maxNumberOfRunningQueries reached
				logger.warn("Query not executed. Max running queries reached. User={}, queryId={}", username, queryId);

				request.setAttribute("errorMessage", messages.getString("maxRunnningQueriesReached"));
				ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
			} // END maxNumberOfRunningQueries IF

		}

	} // end doPost

	/**
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		// Session
		HttpSession session = request.getSession(true);

		/*
		 * A note about security: A malicious user cannot execute arbitrary
		 * queries because ExecuteQuery checks that the query is viewable by the
		 * authenticated or public user before the SQL code is executed (in
		 * PreparedQuery)
		 */

		if (request.getParameter("QUERY_ID") == null && request.getParameter("queryId") == null) {
			return; // nothing to do... this is a spurious GET request
		}

		// this is non authenticated session - default to public_user
		// this code is maintained for backward compatibility with Art pre 1.8
		// since Art 1.8 the "direct" art/user/QueryExecute servlet should be invoked
		// (adding _public_user=true) for public access - query must be granted to public_user)

		if (session.getAttribute("ue") == null) {
			// create a UserEntity object
			UserEntity ue = new UserEntity();
			ue.setUsername("public_user");

			ue.setAccessLevel(0);

			session.setAttribute("ue", ue);
			session.setAttribute("username", "public_user");
		}

		//  request object to the doPost
		// this causes export to file to refer to root insetad of art/export...
		// => you can use this only for view modes that do not generate a file...
		// use .......... instead
		doPost(request, response);
	}
	
	/**
	 * Generate a group report
	 *
	 * @param out
	 * @param rs
	 * @param rsmd
	 * @param splitCol
	 * @return number of rows output
	 * @throws SQLException
	 */
	private int htmlReportOut(PrintWriter out, ResultSet rs, ResultSetMetaData rsmd, int splitCol)
			throws SQLException {
		int col_count = rsmd.getColumnCount();
		int i;
		int counter = 0;
		htmlReportOutWriter o = new htmlReportOutWriter(out);
		String tmpstr;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values


		// Report, is intended to be something like that:
		/*
		 * ------------------------------------- | Attr1 | Attr2 | Attr3 | //
		 * Main header ------------------------------------- | Value1 | Value2 |
		 * Value3 | // Main Data -------------------------------------
		 *
		 * -----------------------------... | SubAttr1 | Subattr2 |... // Sub
		 * Header -----------------------------... | SubValue1.1 | SubValue1.2
		 * |... // Sub Data -----------------------------... | SubValue2.1 |
		 * SubValue2.2 |... -----------------------------...
		 * ................................ ................................
		 * ................................
		 *
		 * etc...
		 */

		// Build main header HTML
		for (i = 0; i < (splitCol); i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToMainHeader(tmpstr);
		}
		// Now the header is completed

		// Build the Sub Header
		for (; i < col_count; i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToSubHeader(tmpstr);
		}

		int maxRows = ArtConfig.getMaxRows("htmlreport");

		while (rs.next() && counter < maxRows) {
			// Separators
			out.println("<br><hr style=\"width:90%;height:1px\"><br>");

			// Output Main Header and Main Data
			o.header(90);
			o.printMainHeader();
			o.beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitCol; i++) {
				o.addCellToLine(rs.getString(i + 1));
				cmpStr.append(rs.getString(i + 1));
			}
			o.endLines();
			o.footer();

			// Output Sub Header and Sub Data
			o.header(80);
			o.printSubHeader();
			o.beginLines();

			// Output Sub Data (first line)
			for (; i < col_count; i++) {
				o.addCellToLine(rs.getString(i + 1));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				if (rs.next()) {
					counter++;
					tmpCmpStr = new StringBuffer();

					for (i = 0; i < splitCol; i++) {
						tmpCmpStr.append(rs.getString(i + 1));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) { // same Main
						o.newLine();
						// Add data lines
						for (; i < col_count; i++) {
							o.addCellToLine(rs.getString(i + 1));
						}
					} else {
						o.endLines();
						o.footer();
						currentMain = false;
						rs.previous();
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (!(counter < maxRows)) {
			o.newLine();
			o.addCellToLine("<blink>Too many rows (>" + maxRows
					+ ")! Data not completed. Please narrow your search or use the xlsx, slk or tsv view modes</blink>", "qeattr", "left", col_count);
		}

		o.endLines();
		o.footer();

		return counter + 1; // number of rows
	}

	//prepare graph object for it's display using the showGraph.jsp page
	private ArtGraph artGraphOut(ResultSetMetaData rsmd, HttpServletRequest request,
			String graphOptions, String shortDescr, int queryType) throws SQLException {

		ArtGraph o;

		switch (queryType) {
			case -1:
				o = new ArtXY();
				break;
			case -2:
			case -13:
				o = new ArtPie();
				break;
			case -6:
				o = new ArtTimeSeries();
				break;
			case -7:
				o = new ArtDateSeries();
				break; //  this line was missing... added thanks to anonymous post in sf
			case -10:
				o = new ArtSpeedometer();
				break;
			case -11:
			case -12:
				o = new ArtXYZChart();
				o.setQueryType(queryType);
				break;
			default: //-3,-4,-5,-8,-9,-14,-15,-16,-17
				o = new ArtCategorySeries();
		}

		String link = rsmd.getColumnLabel(2);
		if (link.equals("LINK")) {
			o.setUseHyperLinks(true);
		} else {
			o.setUseHyperLinks(false);
		}

		int width;
		int height;
		String bgColor;

		// @200x100 -10:100 #FFFFFF nolegend nolabels
		// = make a chart 200px width 100px height focused
		//  on Y range from -10 to 100  with a white backround, without legend and labels
		String params = request.getParameter("_GRAPH_SIZE");
		boolean customParam = false;
		if (StringUtils.isNotBlank(params) && !params.equalsIgnoreCase("DEFAULT")) {
			customParam = true;
		}

		String tmp;
		String options = "";
		boolean usingShortDescription = true; //to accomodate graph options in short description string for art pre-2.0

		int indexOf = shortDescr.lastIndexOf("@");
		if (indexOf > -1) {
			options = shortDescr;
			shortDescr = shortDescr.substring(0, indexOf);
		}

		if (graphOptions != null) {
			options = graphOptions;
			usingShortDescription = false;
		}

		if (customParam) {
			tmp = params;
			usingShortDescription = false;
		} else {
			tmp = options;
		}

		//process graph options string
		ArtQuery aq = new ArtQuery();
		aq.setQueryType(queryType);
		aq.setGraphDisplayOptions(tmp, usingShortDescription);

		double yMin = aq.getGraphYMin();
		double yMax = aq.getGraphYMax();
		width = aq.getGraphWidth();
		height = aq.getGraphHeight();
		bgColor = aq.getGraphBgColor();
		boolean showLegend = aq.isShowLegend();
		boolean showLabels = aq.isShowLabels();
		boolean showPoints = aq.isShowPoints();
		boolean showGraphData = aq.isShowGraphData();
		int rotateAt = aq.getGraphRotateAt();
		int removeAt = aq.getGraphRemoveAt();

		request.setAttribute("_rotate_at", String.valueOf(rotateAt));
		request.setAttribute("_remove_at", String.valueOf(removeAt));

		if (yMin < yMax) {
			request.setAttribute("_from", String.valueOf(yMin)); //cewolf expects strings
			request.setAttribute("_to", String.valueOf(yMax));
		}

		if (!showLegend) {
			request.setAttribute("_nolegend", "true");
		}

		if (showLabels) {
			request.setAttribute("_showlabels", "true");
		} else {
			request.setAttribute("_nolabels", "true");
		}

		if (showPoints) {
			request.setAttribute("_showpoints", "true");
		}

		//override show legend, labels, data points with values selected using checkboxes.
		//only override if from showparams.jsp. if displayed from direct url or dashboard, no chance to change settings using check boxes
		if (request.getParameter("_GRAPH_SIZE") != null) {
			if (request.getParameter("_showLegend") == null) {
				request.setAttribute("_nolegend", "true");
			} else {
				request.removeAttribute("_nolegend");
			}
			if (request.getParameter("_showLabels") == null) {
				request.removeAttribute("_showlabels");
			} else {
				request.setAttribute("_showlabels", "true");
			}
			if (request.getParameter("_showDataPoints") == null) {
				request.removeAttribute("_showpoints");
			} else {
				request.setAttribute("_showpoints", "true");
			}
			if (request.getParameter("_showGraphData") == null) {
				showGraphData = false;
			} else {
				showGraphData = true;
			}
		}

		o.setTitle(shortDescr);
		o.setWidth(width);
		o.setHeight(height);
		o.setSeriesName(rsmd.getColumnLabel(2));
		o.setBgColor(bgColor);
		o.setShowGraphData(showGraphData);

		return o;
	}


	//get number of rows in a resultset.
	private int getNumberOfRows(ResultSet rs) {
		int numberOfRows = -1;

		try {
			if (rs != null) {
				int type = rs.getType();
				if (type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE) {
					//resultset is scrollable
					rs.last();
					numberOfRows = rs.getRow();
					rs.beforeFirst();
				}
			}
		} catch (Exception e) {
			//not all drivers support this technique? If not supported, set number of rows to unknown (-1)
			numberOfRows = -1;
			logger.error("Error", e);
		}

		return numberOfRows;
	}
}

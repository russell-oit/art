/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobrunners;

import art.connectionpool.DbConnections;
import art.enums.ReportType;
import art.graph.ExportGraph;
import art.mail.Mailer;
import art.output.JasperReportsOutput;
import art.output.JxlsOutput;
import art.output.ReportOutputInterface;
import art.report.ChartOptions;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOptions;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.utils.CachedResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to run report jobs
 *
 * @author Timothy Anyona
 */
public class ReportJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(ReportJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = null;

//		int tempJobId = 0;
//		if (context != null) {
//			dataMap = context.getMergedJobDataMap();
//			tempJobId = dataMap.getInt("jobId");
//
//			logger.debug("Attempting to load Job Id {}", tempJobId);
//
//			load(tempJobId);
//		}
//
//		if (jobId == -1) {
//			//job not found. exit
//			logger.debug("Job Id {} not found", tempJobId);
//			return;
//		}
//
//		logger.debug("Job Id {} running...", jobId);
//
//		Connection conn = null;
//
//		try {
//			conn = Config.getConnection();
//
//			//get next run date	for the job for updating the jobs table. only update if it's a scheduled run and not an interactive, temporary job
//			String tempJob = dataMap.getString("tempjob");
//			if (tempJob == null) {
//				//not a temp job. set next run date
//				nextRunDate = context.getTrigger().getFireTimeAfter(new java.util.Date());
//			}
//
//			// set overall job start time in the jobs table
//			beforeExecution(conn);
//
//			//don't run job if query or job or job owner is disabled
//			if (StringUtils.equals("D", jobOwnerStatus)) {
//				//job owner disabled. don't run job. just update jobs table with current status
//				fileName = "-Job Owner Disabled";
//			} else if (StringUtils.equals("D", queryStatus)) {
//				//query disabled. don't run job. just update jobs table with current status
//				fileName = "-Query Disabled";
//			} else if (StringUtils.equals("N", activeStatus)) {
//				//job disabled. don't run job. just update jobs table with current status
//				fileName = "-Job Disabled";
//			} else {
//				//run job. handle dynamic recipients
//				if (recipientsQueryId > 0) {
//					//job has dynamic recipients
//					runDynamicRecipientsJob(conn);
//				} else {
//					//job doesn't have dynamic recipients
//					runNormalJob(conn);
//				}
//			}
//
//			//set job table's final end time and file name
//			afterCompletion(conn);
//
//		} catch (Exception e) {
//			logger.error("Error", e);
//		} finally {
//			try {
//				if (conn != null) {
//					conn.close();
//				}
//			} catch (Exception e) {
//				logger.error("Error", e);
//			}
//		}
	}

//	//run job
//	private void runJob(Connection conn, boolean splitJob, String user, String userEmail,
//			Map<String, Map<String, String>> recipientDetails, boolean recipientFilterPresent) {
//		//set job start date. relevant for split jobs
//		jobStartDate = new Timestamp(new java.util.Date().getTime());
//
//		ReportRunner reportRunner = null;
//
//		fileName = "-No File"; //reset file name
//
//		//create job audit record if auditing is enabled
//		createAuditRecord(conn, user);
//
//		try {
//			reportRunner = prepareQuery(user);
//
//			//for split jobs, don't check security. shared users have been allowed access to the output
//			if (splitJob) {
//				reportRunner.setAdminSession(true);
//			}
//
//			if (recipientFilterPresent) {
//				//enable report data to be filtered/different for each recipient
//				reportRunner.setRecipientFilterPresent(recipientFilterPresent);
//				for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
//					//map should only have one value if filter present
//					Map<String, String> recipientColumns = entry.getValue();
//					reportRunner.setRecipientColumn(recipientColumns.get(ArtUtils.RECIPIENT_COLUMN));
//					reportRunner.setRecipientId(recipientColumns.get(ArtUtils.RECIPIENT_ID));
//					reportRunner.setRecipientIdType(recipientColumns.get(ArtUtils.RECIPIENT_ID_TYPE));
//				}
//			}
//
//			//prepare report parameters
//			ParameterProcessor paramProcessor = new ParameterProcessor();
//			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, reportId);
//
//			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
//			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
//			ReportOptions reportOptions = paramProcessorResult.getReportOptions();
//			ChartOptions parameterChartOptions = paramProcessorResult.getChartOptions();
//
//			ReportType reportType;
//
//			//jobs don't show record count so generally no need for scrollable resultsets
//			int resultSetType;
//			if ((reportType.isChart() && parameterChartOptions.isShowData())) {
//				//need scrollable resultset for charts for show data option
//				resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
//			} else {
//				resultSetType = ResultSet.TYPE_FORWARD_ONLY;
//			}
//
//			/*
//			 * BEGIN EXECUTE QUERY
//			 */
//			reportRunner.execute(resultSetType);
//
//
//			/*
//			 * END EXECUTE QUERY
//			 */
//			/*
//			 * Job Types: 1 = Alert 2 = Mails as attachment 3 = Publish 4 = Just
//			 * Run 5 = Mail with output within the email as html 6 = Mails as
//			 * attachment only if query returns one or more rows 7 = Mail with
//			 * output within the email as html only if query returns one or more
//			 * rows 8 = Publish only if query returns one or more rows 9 = Cache
//			 * the result set in the cache database (append) 10 = Cache the
//			 * result set in the cache database (drop/insert)
//			 */
//			//trim address fields. to aid in checking if emails are configured
//			userEmail = StringUtils.trim(userEmail);
//			tos = StringUtils.trim(tos);
//			cc = StringUtils.trim(cc);
//			bcc = StringUtils.trim(bcc);
//
//			//determine if emailing is required and emails are configured
//			boolean generateEmail = false;
//			if (jobType == 3 || jobType == 8) {
//				//for split published jobs, tos should have a value to enable confirmation email for individual users
//				if (!StringUtils.equals(tos, userEmail) && (StringUtils.length(tos) > 4 || StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4) && StringUtils.length(userEmail) > 4) {
//					generateEmail = true;
//				} else if (StringUtils.equals(tos, userEmail) && (StringUtils.length(tos) > 4 || StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)) {
//					generateEmail = true;
//				}
//			} else {
//				//for non-publish jobs, if an email address is available, generate email
//				if (StringUtils.length(userEmail) > 4 || StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4) {
//					generateEmail = true;
//				}
//			}
//
//			//set email fields
//			String[] tosEmail = null;
//			String[] ccs = null;
//			String[] bccs = null;
//			if (generateEmail) {
//				tosEmail = StringUtils.split(userEmail, ";");
//				ccs = StringUtils.split(cc, ";");
//				bccs = StringUtils.split(bcc, ";");
//
//				logger.debug("Job Id {}. to: {}", jobId, userEmail);
//				logger.debug("Job Id {}. cc: {}", jobId, cc);
//				logger.debug("Job Id {}. bcc: {}", jobId, bcc);
//			}
//
//			if (jobType == 1) {
//				/*
//				 * ALERT if the resultset is not null and the first column is a
//				 * positive integer => send the alert email
//				 */
//
//				//only run alert query if we have some emails configured
//				if (generateEmail || recipientDetails != null) {
//					fileName = "-No Alert";
//
//					ResultSet rs = reportRunner.getResultSet();
//					if (rs.next()) {
//						int value = rs.getInt(1);
//						if (value > 0) {
//							logger.debug("Job Id {} - Raising Alert. Value is {}", jobId, value);
//
//							// compatibility with Art pre 1.8 where subject was not editable
//							if (subject == null) {
//								subject = "ART Alert: " + jobName + " (Job " + jobId + ")";
//							}
//
//							//send customized emails to dynamic recipients
//							if (recipientDetails != null) {
//								Mailer mailer = getMailer();
//
//								for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
//									String email = entry.getKey();
//									Map<String, String> recipientColumns = entry.getValue();
//
//									//customize message by replacing field labels with values for this recipient
//									String customMessage = message; //message for a particular recipient. may include personalization e.g. Dear Jane
//									if (customMessage == null) {
//										customMessage = "";
//									}
//
//									if (StringUtils.isNotBlank(customMessage)) {
//										for (Map.Entry<String, String> entry2 : recipientColumns.entrySet()) {
//											String columnName = entry2.getKey();
//											String columnValue = entry2.getValue();
//
//											String searchString = Pattern.quote("#" + columnName + "#"); //quote in case it contains special regex characters
//											String replaceString = Matcher.quoteReplacement(columnValue); //quote in case it contains special regex characters
//											customMessage = customMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
//										}
//									}
//
//									prepareAlertJob(mailer, customMessage);
//
//									mailer.setTo(email);
//
//									//send email for this recipient
//									try {
//										mailer.send();
//										fileName = "-Alert Sent";
//									} catch (MessagingException ex) {
//										logger.debug("Error", ex);
//										fileName = "-Error when sending alert <p>" + ex.toString() + "</p>";
//
//									}
//								}
//
//								if (recipientFilterPresent) {
//									//don't run normal email job after filtered email sent
//									generateEmail = false;
//								}
//							}
//
//							//send email to normal recipients
//							if (generateEmail) {
//								Mailer mailer = getMailer();
//
//								prepareAlertJob(mailer, message);
//
//								//set recipients						
//								mailer.setTo(tosEmail);
//								mailer.setCc(ccs);
//								mailer.setBcc(bccs);
//
//								try {
//									mailer.send();
//									fileName = "-Alert Sent";
//								} catch (MessagingException ex) {
//									logger.debug("Error", ex);
//									fileName = "-Error when sending alert <p>" + ex.toString() + "</p>";
//
//								}
//
//							} else {
//								logger.debug("Job Id {} - No Alert. Value is {}", jobId, value);
//							}
//						} else {
//							logger.debug("Job Id {} - Empty resultset for alert", jobId);
//						}
//					}
//				} else {
//					//no emails configured
//					fileName = "-No emails configured";
//				}
//			} else if (jobType == 2 || jobType == 3 || jobType == 5 || jobType == 6 || jobType == 7 || jobType == 8) {
//				/*
//				 * MAILwithAttachment or PUBLISH or MAILinLine
//				 */
//				ResultSet rs = null;
//				ResultSetMetaData rsmd = null;
//				if (queryType != 115 && queryType != 117) {
//					rs = reportRunner.getResultSet();
//					rsmd = rs.getMetaData();
//				}
//
//				logger.debug("Job Id {} - Mail or Publish. Type: {}", jobId, jobType);
//
//				//determine if the query returns records. to know if to generate output for conditional jobs
//				boolean generateOutput = true;
//
//				if (jobType == 6 || jobType == 7 || jobType == 8) {
//					//conditional job. check if resultset has records. no "recordcount" method so we have to execute query again
//					ReportRunner pqCount = prepareQuery(user);
//					pqCount.setAdminSession(true);
//					pqCount.execute();
//					ResultSet rsCount = pqCount.getResultSet();
//					if (!rsCount.next()) {
//						//no records
//						generateOutput = false;
//						fileName = "-No Records";
//					}
//					rsCount.close();
//					pqCount.close();
//				}
//
//				//for emailing jobs, only run query if some emails are configured
//				if (jobType == 2 || jobType == 5 || jobType == 6 || jobType == 7) {
//					//email attachment, email inline, conditional email attachment, conditional email inline
//					if (!generateEmail && recipientDetails == null) {
//						generateOutput = false;
//						fileName = "-No emails configured";
//					}
//				}
//
//				if (generateOutput) {
//					//generate output
//					//generate file name to use for report types and formats that generate files
//					String baseFileName = ArtUtils.getUniqueFileName(jobId);
//					String exportPath = Config.getReportsExportPath();
//
//					String fileName = baseFileName + "." + reportFormat.getFilenameExtension();
//					String fullFileName = exportPath + fileName;
//
//					if (queryType < 0) {
//						//save charts to file
//						ExportGraph eg = new ExportGraph();
//						eg.setFileUserName(jobFileUsername);
//						eg.setQueryName(queryName);
//						eg.setExportPath(jobsPath);
//						eg.setOutputFormat(outputFormat); // png or pdf
//						eg.setXAxisLabel(xAxisLabel);
//						eg.setYAxisLabel(yAxisLabel);
//						eg.setTitle(queryShortDescription);
//						eg.setShowData(showGraphData); //enable display of graph data below graph for pdf graph output
//						eg.setDisplayParameters(displayParams); //enable display of graph parameters above graph for pdf graph output
//						eg.setShowDataPoints(showGraphDataPoints);
//						eg.setShowLegend(showGraphLegend);
//						eg.setShowLabels(showGraphLabels);
//						eg.setQueryId(queryId);
//						eg.setGraphOptions(jobGraphOptions);
//
//						eg.createFile(rs, queryType);
//						fileName = eg.getFileName();
//					} else if (queryType == 115 || queryType == 116) {
//						//jasper report
//						JasperReportsOutput jasper = new JasperReportsOutput();
////						jasper.setQueryName(queryName);
////						jasper.setFileUserName(jobFileUsername);
////						jasper.setExportPath(jobsPath);
////						jasper.setReportFormatString(outputFormat);
//
//						if (queryType == 115) {
//							//report will use query in the report template
////							jasper.generateReport(null, queryId, pq.getInlineParams(), pq.getMultiParams(), htmlParams);
//						} else {
//							//report will use data from art query
////							jasper.generateReport(rs, queryId, pq.getInlineParams(), pq.getMultiParams(), htmlParams);
//						}
////						fileName = jasper.getFileName();
//					} else if (queryType == 117 || queryType == 118) {
//						//jxls spreadsheet
//						JxlsOutput jxls = new JxlsOutput();
////						jxls.setQueryName(queryName);
////						jxls.setFileUserName(jobFileUsername);
////						jxls.setExportPath(jobsPath);
////
////						if (queryType == 117) {
////							//report will use query in the jxls template
////							jxls.generateReport(null, queryId, pq.getInlineParams(), pq.getMultiParams(), htmlParams);
////						} else {
////							//report will use data from art query
////							jxls.generateReport(rs, queryId, pq.getInlineParams(), pq.getMultiParams(), htmlParams);
////						}
////						fileName = jxls.getFileName();
//					} else {
//						ReportOutputInterface o;
//
//						String classToLoad = "art.output." + outputFormat + "Output";
//						ClassLoader cl = this.getClass().getClassLoader();
//						Object obj = cl.loadClass(classToLoad).newInstance();
//
//						o = (ReportOutputInterface) obj;
//
//						o.setMaxRows(Config.getMaxRows(outputFormat));
//
//						//printwriter not needed for all output types. Avoid creating extra html file when output is not html, xml or rss
//						FileOutputStream fos = null;
//						PrintWriter out = null;
//						boolean printWriterUsed = false;
//
//						try {
//
//							if (outputFormat.indexOf("html") >= 0 || outputFormat.indexOf("xml") >= 0 || outputFormat.indexOf("rss") >= 0) {
//								if (outputFormat.indexOf("html") >= 0) {
//									SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
//									String datePart = dateFormatter.format(new java.util.Date());
//
//									fileName = jobFileUsername + "-" + queryName + "-" + datePart + ArtUtils.getRandomFileNameString() + ".html";
//									fileName = ArtUtils.cleanFileName(fileName);
//									fileName = jobsPath + fileName;
//
//								} else {
//									//xml or rss
//									fileName = jobFileUsername + ".html";
//									fileName = ArtUtils.cleanFileName(fileName);
//									fileName = jobsPath + fileName;
//								}
//								fos = new FileOutputStream(fileName);
//								out = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
//								o.setWriter(out);
//								printWriterUsed = true;
//							}
//
//							o.setQueryName(queryName);
//							o.setFileUserName(jobFileUsername);
//							o.setExportPath(jobsPath);
//							o.setDisplayParameters(displayParams);
//
//							ResourceBundle messages = ResourceBundle.getBundle("i18n.ArtMessages");
//							if (queryType == 101 || queryType == 102) {
////							ReportOutputHandler.flushXOutput(messages, o, rs, rsmd);
//							} else {
////							ReportOutputHandler.flushOutput(messages, o, rs, rsmd);
//							}
//
//							/*
//							 * Now the resultset has been flushed in the o ArtOutput
//							 * object and therefore it has been: 1. streamed in the
//							 * out PrintWriter (file) (or) 2. written in another
//							 * file if the o object did it (for example xls view
//							 * mode) Thus now we need to discover where the output
//							 * is (in the out object or in another file)
//							 */
//							if (o.getFileName() != null) {
//								fileName = o.getFileName();
//							}
//
//							// the file is on the PrintWriter (for html)
//							if (out != null) {
//								out.close();
//							}
//							if (fos != null) {
//								fos.close();
//							}
//
//						} finally {
//							//https://stackoverflow.com/questions/14436453/is-it-safe-to-use-apache-commons-io-ioutils-closequietly
//							IOUtils.closeQuietly(out);
//							IOUtils.closeQuietly(fos);
//						}
//					}
//
//					// fileName now stores the file to email or publish...                    
//					logger.debug("Job Id {}. File is: {}", jobId, fileName);
//
//					if (generateEmail || recipientDetails != null) {
//						//some kind of emailing required
//
//						// compatibility with Art pre 1.8 where subject was not editable
//						if (subject == null) {
//							subject = "ART Scheduler: " + jobName + " (Job " + jobId + ")";
//						}
//
//						//send customized emails to dynamic recipients
//						if (recipientDetails != null) {
//							Mailer mailer = getMailer();
//
//							String email = "";
//
//							for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
//								email = entry.getKey();
//								Map<String, String> recipientColumns = entry.getValue();
//
//								//customize message by replacing field labels with values for this recipient
//								String customMessage = message; //message for a particular recipient. may include personalization e.g. Dear Jane
//								if (customMessage == null) {
//									customMessage = "";
//								}
//
//								if (StringUtils.isNotBlank(customMessage)) {
//									for (Map.Entry<String, String> entry2 : recipientColumns.entrySet()) {
//										String columnName = entry2.getKey();
//										String columnValue = entry2.getValue();
//
//										String searchString = Pattern.quote("#" + columnName + "#"); //quote in case it contains special regex characters
//										String replaceString = Matcher.quoteReplacement(columnValue); //quote in case it contains special regex characters
//										customMessage = customMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
//									}
//								}
//
//								prepareEmailJob(mailer, customMessage);
//
//								mailer.setTo(email);
//
//								//send email for this recipient
//								try {
//									mailer.send();
//									fileName = "-File has been emailed";
//								} catch (MessagingException ex) {
//									logger.debug("Error", ex);
//									fileName = "-Error when sending some emails."
//											+ " <p>" + ex.toString() + "</p>";
//
//									String msg = "Error when sending some emails."
//											+ " \n" + ex.toString()
//											+ " \n To: " + email;
//									logger.warn(msg);
//
//								}
//							}
//
//							if (recipientFilterPresent) {
//								//don't run normal email job after filtered email sent
//								generateEmail = false;
//							}
//
//							//set filename to status of last recipient email sent
//							File f = new File(fileName);
//							boolean deleted = f.delete();
//							if (!deleted) {
//								logger.warn("Email attachment file not deleted: {}", fileName);
//							}
//						}
//
//						//send email to normal recipients
//						if (generateEmail) {
//							Mailer mailer = getMailer();
//
//							prepareEmailJob(mailer, message);
//
//							//set recipients						
//							mailer.setTo(tosEmail);
//							mailer.setCc(ccs);
//							mailer.setBcc(bccs);
//
//							//check if mail was successfully sent
//							try {
//								mailer.send();
//								fileName = "-File has been emailed";
//							} catch (MessagingException ex) {
//								logger.debug("Error", ex);
//								fileName = "-Error when sending some emails."
//										+ " <p>" + ex.toString() + "</p>";
//
//								String msg = "Error when sending some emails."
//										+ " \n" + ex.toString()
//										+ " \n Complete address list:\n To: " + userEmail + "\n Cc: " + cc + "\n Bcc: " + bcc;
//								logger.warn(msg);
//
//							}
//							if (jobType == 2 || jobType == 5 || jobType == 6 || jobType == 7) {
//								// delete the file since it has
//								// been sent via email (for publish jobs it is deleted by the scheduler)
//								File f = new File(fileName);
//								f.delete();
//							} else {
////								//publish job reminder email. separate file link and message with a newline character (\n)
////								if (mailSent) {
////									fileName = fileName + RESULT_SEPARATOR + "<p>Reminder email sent</p>";
////								} else {
////									fileName = fileName + RESULT_SEPARATOR + "<p>Error when sending reminder email <br><br>" + mailer.getSendError() + "</p>";
////								}
//							}
//						}
//					}
//				}
//
//			} else if (jobType == 9 || jobType == 10) {
//				// Cache the result in the cache database
//				int targetDatabaseId = Integer.parseInt(outputFormat);
//
//				Connection cacheDatabaseConnection = DbConnections.getConnection(targetDatabaseId);
//				ResultSet rs = reportRunner.getResultSet();
//				CachedResult cr = new CachedResult();
//				cr.setTargetConnection(cacheDatabaseConnection);
//				cr.setResultSet(rs);
//				if (cachedTableName == null || cachedTableName.length() == 0) {
//					cachedTableName = queryName + "_J" + jobId;
//				}
//				cr.setCachedTableName(cachedTableName);
//				cr.setCacheMode(jobType - 8); // 1 = append 2 = drop/insert (3 = update (not implemented))
//				cr.cacheIt();
//				cacheDatabaseConnection.close();
//				fileName = "- Table Name (rows inserted):  <code>" + cr.getCachedTableName() + "</code> (" + cr.getRowsCount() + ")"
//						+ "<br />Columns Names:<br /><code>" + cr.getCachedTableColumnsName() + "</code>";
//
//			} else { // jobType 4:just run it.
//				// This is used Used to start batch jobs at db level via calls to stored procs
//				// or just to run update statements.
//			}
//
//			logger.debug("Job Id {} ...finished", jobId);
//		} catch (Exception e) {
//			logger.error("Error. Job id={}, User={}", new Object[]{jobId, user, e});
//			fileName = "-<b>Error:</b> " + e;
//		} finally {
//			if (reportRunner != null) {
//				reportRunner.close();
//			}
//			// set audit timestamp and update archives
//			afterExecution(conn, splitJob, user);
//		}
//	}
//
//	/**
//	 * Prepare mailer object for sending alert job. Used for normal jobs, and
//	 * dynamic recipient jobs
//	 *
//	 * @param mailer
//	 * @param msg
//	 */
//	private void prepareAlertJob(Mailer mailer, String msg) {
//		mailer.setSubject(subject);
//		mailer.setFrom(from);
//		mailer.setMessage("<html>" + msg + "<hr><small>This is an automatically generated message (ART, Job ID " + jobId + ")</small></html>");
//	}
//
//	/**
//	 * Prepare mailer object for sending email job. Used for normal jobs, and
//	 * dynamic recipient jobs
//	 *
//	 * @param mailer
//	 */
//	private void prepareEmailJob(Mailer mailer, String msg) throws FileNotFoundException, IOException {
//
//		mailer.setSubject(subject);
//		mailer.setFrom(from);
//
//		if (StringUtils.isBlank(msg)) {
//			msg = "&nbsp;"; //if message is blank, ensure there's a space before the hr
//		}
//
//		if (jobType == 2 || jobType == 6) {
//			// e-mail output as attachment
//			List<File> l = new ArrayList<File>();
//			l.add(new File(fileName));
//			mailer.setAttachments(l);
//		} else if (jobType == 5 || jobType == 7) {
//			// inline html within email
//			// read the file and include it in the HTML message
//			FileInputStream fis = new FileInputStream(fileName);
//			try {
//				byte fileBytes[] = new byte[fis.available()];
//				int result = fis.read(fileBytes);
//				if (result == -1) {
//					logger.warn("EOF reached for inline email file: {}", fileName);
//				}
//				// convert the file to a string and get only the html table
//				String htmlTable = new String(fileBytes, "UTF-8");
//				//htmlTable = htmlTable.substring(htmlTable.indexOf("<html>") + 6, htmlTable.indexOf("</html>"));
//				htmlTable = htmlTable.substring(htmlTable.indexOf("<body>") + 6, htmlTable.indexOf("</body>")); //html plain output now has head and body sections
//				msg = msg + "<hr>" + htmlTable;
//			} finally {
//				fis.close();
//			}
//		}
//
//		String autoMessage = "<hr><small>This is an automatically generated message (ART, Job ID " + jobId + ")</small>";
//		mailer.setMessage("<html>" + msg + autoMessage + "</html>");
//	}

}

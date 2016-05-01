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
import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.enums.JobType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.job.JobService;
import art.jobparameter.JobParameter;
import art.jobparameter.JobParameterService;
import art.mail.Mailer;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOutputGenerator;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.utils.CachedResult;
import art.utils.FilenameHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Class to run report jobs
 *
 * @author Timothy Anyona
 */
public class ReportJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(ReportJob.class);
	private DbService dbService;
	private String fileName;
	private String jobAuditKey;
	private art.job.Job job;
	private Timestamp jobStartDate;
	private JobType jobType;
	private int jobId;
	private String runDetails;
	private String runMessage;

	@Override
	@CacheEvict(value = "jobs", allEntries = true)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (!Config.getSettings().isSchedulingEnabled()) {
			return;
		}

		JobDataMap dataMap = context.getMergedJobDataMap();
		int tempJobId = dataMap.getInt("jobId");

		JobService jobService = new JobService();

		try {
			job = jobService.getJob(tempJobId);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		if (job == null) {
			logger.info("Job not found: {}", tempJobId);
			return;
		}

		jobType = job.getJobType();
		jobId = job.getJobId();

		fileName = "";
		runDetails = "";
		runMessage = "";

		dbService = new DbService();

		//get next run date	for the job for updating the jobs table. only update if it's a scheduled run and not an interactive, temporary job
		boolean tempJob = dataMap.getBooleanValue("tempJob");
		Date nextRunDate;
		if (tempJob) {
			//temp job. use existing next run date
			nextRunDate = job.getNextRunDate();
		} else {
			//not a temp job. set new next run date
			nextRunDate = context.getTrigger().getFireTimeAfter(new Date());
		}

		try {
			//set overall job start time in the jobs table
			beforeExecution(nextRunDate);

			if (!job.isActive()) {
				runMessage = "jobs.message.jobDisabled";
			} else if (!job.getReport().isActive()) {
				runMessage = "jobs.message.reportDisabled";
			} else if (!job.getUser().isActive()) {
				runMessage = "jobs.message.ownerDisabled";
			} else {

				Connection conn = null;

				try {
					conn = DbConnections.getArtDbConnection();

					if (job.getRecipientsReportId() > 0) {
						//job has dynamic recipients
						runDynamicRecipientsJob(conn);
					} else {
						//job doesn't have dynamic recipients
						runNormalJob(conn);
					}
				} catch (SQLException ex) {
					logger.error("Error", ex);
				} finally {
					DatabaseUtils.close(conn);
				}
			}

			afterCompletion();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	private void sendEmail(Mailer mailer) throws MessagingException, IOException {
		if (Config.isEmailServerConfigured()) {
			mailer.send();
		} else {
			logger.info("Email server not configured. Job Id: {}", jobId);
		}
	}

	/**
	 * Prepare mailer object for sending alert job. Used for normal jobs, and
	 * dynamic recipient jobs
	 *
	 * @param mailer
	 * @param msg
	 */
	private void prepareAlertJob(Mailer mailer, String msg) {
		String from = job.getMailFrom();

		String subject = job.getMailSubject();
		// compatibility with Art pre 1.8 where subject was not editable
		if (subject == null) {
			subject = "ART Alert: (Job " + jobId + ")";
		}

		mailer.setSubject(subject);
		mailer.setFrom(from);

		String mainMessage;
		if (StringUtils.isBlank(msg)) {
			mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
		} else {
			mainMessage = msg;
		}

		Context ctx = new Context(Locale.getDefault());
		ctx.setVariable("mainMessage", mainMessage);
		ctx.setVariable("job", job);

		SpringTemplateEngine templateEngine = getTemplateEngine();
		String finalMessage = templateEngine.process("emailTemplate.html", ctx);
		mailer.setMessage(finalMessage);
	}

	/**
	 * Prepare mailer object for sending email job. Used for normal jobs, and
	 * dynamic recipient jobs
	 *
	 * @param mailer
	 */
	private void prepareEmailJob(Mailer mailer, String msg, String outputFileName)
			throws FileNotFoundException, IOException {

		String from = job.getMailFrom();

		String subject = job.getMailSubject();
		// compatibility with Art pre 1.8 where subject was not editable
		if (subject == null) {
			subject = "ART: (Job " + jobId + ")";
		}

		mailer.setSubject(subject);
		mailer.setFrom(from);

		String mainMessage;
		if (StringUtils.isBlank(msg)) {
			mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
		} else {
			mainMessage = msg;
		}

		Context ctx = new Context(Locale.getDefault());
		ctx.setVariable("mainMessage", mainMessage);
		ctx.setVariable("job", job);

		if (jobType.isEmailAttachment()) {
			// e-mail output as attachment
			List<File> attachments = new ArrayList<>();
			attachments.add(new File(outputFileName));
			mailer.setAttachments(attachments);
		} else if (jobType.isEmailInline()) {
			// inline html within email
			// read the file and include it in the HTML message
			try (FileInputStream fis = new FileInputStream(outputFileName)) {
				byte fileBytes[] = new byte[fis.available()];
				int result = fis.read(fileBytes);
				if (result == -1) {
					logger.warn("EOF reached for inline email file: {}", outputFileName);
				}
				// convert the file to a string and get only the html table
				String data = new String(fileBytes, "UTF-8");
				data = data.substring(data.indexOf("<body>") + 6, data.indexOf("</body>")); //html plain output now has head and body sections
				ctx.setVariable("data", data);
			}
		}

		SpringTemplateEngine templateEngine = getTemplateEngine();
		String finalMessage = templateEngine.process("emailTemplate.html", ctx);
		mailer.setMessage(finalMessage);
	}

	private SpringTemplateEngine getTemplateEngine() {
		//http://blog.zenika.com/2013/01/18/introducing-the-thymeleaf-template-engine/
		ClassLoaderTemplateResolver emailResolver = new ClassLoaderTemplateResolver();
		emailResolver.setPrefix("mail/");
		emailResolver.setTemplateMode("HTML");
		emailResolver.setCharacterEncoding("UTF-8");
		emailResolver.setOrder(1);

		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.addTemplateResolver(emailResolver);

		return engine;
	}

	private void beforeExecution(Date nextRunDate) throws SQLException {
		//update last start date and next run date on art jobs table
		String sql = "UPDATE ART_JOBS SET LAST_START_DATE = ?,"
				+ " LAST_FILE_NAME='', LAST_RUN_MESSAGE='', LAST_RUN_DETAILS='',"
				+ " NEXT_RUN_DATE = ? WHERE JOB_ID = ?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			DatabaseUtils.toSqlTimestamp(nextRunDate),
			jobId
		};

		dbService.update(sql, values);
	}

	private void afterCompletion() throws SQLException {
		//update job details
		String sql = "UPDATE ART_JOBS SET LAST_END_DATE = ?, LAST_FILE_NAME = ?,"
				+ " LAST_RUN_MESSAGE=?, LAST_RUN_DETAILS=? WHERE JOB_ID = ?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			fileName,
			runMessage,
			runDetails,
			jobId
		};

		dbService.update(sql, values);
	}

	private void runDynamicRecipientsJob(Connection conn) {
		String tos = job.getMailTo();
		String username = job.getUser().getUsername();
		int recipientsReportId = job.getRecipientsReportId();
		String cc = job.getMailCc();
		String bcc = job.getMailBcc();

		ReportRunner recipientsReportRunner = null;
		ResultSet rs = null;

		try {
			recipientsReportRunner = prepareReportRunner(username, recipientsReportId);

			recipientsReportRunner.execute();

			rs = recipientsReportRunner.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			if (columnCount == 1) {
				//only email column. add dynamic recipient emails to Tos and run like normal job
				ArrayList<String> emailsList = new ArrayList<>();
				while (rs.next()) {
					String email = rs.getString(1); //first column has email addresses
					if (StringUtils.length(email) > 4) {
						emailsList.add(email);
					}
				}

				if (emailsList.size() > 0) {
					String emails = StringUtils.join(emailsList, ";");
					runNormalJob(conn, emails);
				}
			} else if (columnCount > 1) {
				//personalization fields present
				//Get the column names. column indices start from 1
				ArrayList<String> columnList = new ArrayList<>();
				for (int i = 1; i < columnCount + 1; i++) {
					String columnName = rsmd.getColumnLabel(i); //use alias if available

					//store column names in lowercase to ensure special columns are found by list.contains()
					//some RDBMSs make all column names uppercase					
					columnList.add(columnName.toLowerCase());
				}

				if (columnList.contains(ArtUtils.RECIPIENT_COLUMN) && columnList.contains(ArtUtils.RECIPIENT_ID)) {
					//separate emails, different email message, different report data
					while (rs.next()) {
						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							String columnName;
							String columnValue;
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								columnName = rsmd.getColumnLabel(i); //use column alias if available

								if (rs.getString(columnName) == null) {
									columnValue = "";
								} else {
									columnValue = rs.getString(columnName);
								}
								recipientColumns.put(columnName.toLowerCase(), columnValue); //use lowercase so that special columns are found
							}

							Map<String, Map<String, String>> recipient = new HashMap<>();
							recipient.put(email, recipientColumns);

							//run job for this recipient
							boolean splitJob = true;
							boolean recipientFilterPresent = true;
							runJob(conn, splitJob, username, tos, recipient, recipientFilterPresent);
						}
					}

					//run normal job in case tos, cc etc configured
					if (StringUtils.length(tos) > 4 || StringUtils.length(cc) > 4
							|| StringUtils.length(bcc) > 4) {
						runNormalJob(conn);
					}
				} else {
					//separate emails, different email message, same report data
					Map<String, Map<String, String>> recipients = new HashMap<>();
					while (rs.next()) {
						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							String columnName;
							String columnValue;
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								columnName = rsmd.getColumnLabel(i); //use column alias if available

								if (rs.getString(columnName) == null) {
									columnValue = "";
								} else {
									columnValue = rs.getString(columnName);
								}
								recipientColumns.put(columnName, columnValue);
							}
							recipients.put(email, recipientColumns);
						}
					}

					//run job for all recipients
					boolean splitJob = true;
					runJob(conn, splitJob, username, tos, recipients);
				}
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(rs);

			if (recipientsReportRunner != null) {
				recipientsReportRunner.close();
			}
		}
	}

	private void runNormalJob(Connection conn) {
		runNormalJob(conn, null);
	}

	private void runNormalJob(Connection conn, String dynamicRecipientEmails) {
		//run job. if job isn't shared, generate single output
		//if job is shared and doesn't use rules, generate single output to be used by all users
		//if job is shared and uses rules, generate multiple, individualized output for each shared user

		try {
			int userCount = 0; //number of shared users
			String ownerFileName = null; //for shared jobs, ensure the jobs table has the job owner's file

			boolean splitJob = false; //flag to determine if job will generate one file or multiple individualized files. to know which tables to update

			String username = job.getUser().getUsername();

			if (job.isAllowSharing()) {
				if (job.isSplitJob()) {
					//generate individualized output for all shared users

					//update art_user_jobs table with users who have access through group membership. so that users newly added to a group can get their own output
					addSharedJobUsers(conn);

					//get users to generate output for
					String usersSQL = "SELECT AUJ.USERNAME, AU.EMAIL"
							+ " FROM ART_USER_JOBS AUJ"
							+ " INNER JOIN ART_USERS AU ON"
							+ " AUJ.USERNAME = AU.USERNAME"
							+ " WHERE AUJ.JOB_ID = ? AND AU.ACTIVE=1";

					PreparedStatement ps = null;
					ResultSet rs = null;
					try {
						ps = conn.prepareStatement(usersSQL);
						ps.setInt(1, jobId);

						rs = ps.executeQuery();
						while (rs.next()) {
							userCount += 1;
							runJob(conn, splitJob, rs.getString("USERNAME"), rs.getString("EMAIL"));
							//ensure that the job owner's output version is saved in the jobs table
							if (username.equals(rs.getString("USERNAME"))) {
								ownerFileName = fileName;
							}
						}
					} finally {
						DatabaseUtils.close(rs, ps);
					}

					if (userCount == 0) {
						//no shared users defined yet. generate one file for the job owner
						String emails = job.getMailTo();
						if (dynamicRecipientEmails != null) {
							emails = emails + ";" + dynamicRecipientEmails;
						}
						runJob(conn, splitJob, username, emails);
					}
				} else {
					//generate one single output to be used by all users
					String emails = job.getMailTo();
					if (dynamicRecipientEmails != null) {
						emails = emails + ";" + dynamicRecipientEmails;
					}
					runJob(conn, splitJob, username, emails);
				}
			} else {
				//job isn't shared. generate one file for the job owner
				String emails = job.getMailTo();
				if (dynamicRecipientEmails != null) {
					emails = emails + ";" + dynamicRecipientEmails;
				}
				runJob(conn, splitJob, username, emails);
			}

			//ensure jobs table always has job owner's file, or a note if no output was produced for the job owner
			if (ownerFileName != null) {
				fileName = ownerFileName;
			} else if (splitJob && userCount > 0) {
				//job is shared with other users but the owner doesn't have a copy. save note in the jobs table
				runMessage = "jobs.message.jobShared";
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	public void addSharedJobUsers(Connection conn) throws SQLException {
		String sql;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			//get users who should have access to the job through group membership but don't already have it
			sql = "SELECT AU.USERNAME, AUGA.USER_GROUP_ID "
					+ " FROM ART_USERS AU, ART_USER_GROUP_ASSIGNMENT AUGA, ART_USER_GROUP_JOBS AUGJ "
					+ " WHERE AU.USERNAME = AUGA.USERNAME AND AUGA.USER_GROUP_ID = AUGJ.USER_GROUP_ID "
					+ " AND AUGJ.JOB_ID = ? "
					+ " AND NOT EXISTS "
					+ " (SELECT * FROM ART_USER_JOBS AUJ "
					+ " WHERE AUJ.USERNAME = AU.USERNAME AND AUJ.JOB_ID = ?)";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, jobId);
			ps.setInt(2, jobId);
			rs = ps.executeQuery();

			sql = "INSERT INTO ART_USER_JOBS (JOB_ID, USERNAME, USER_GROUP_ID) VALUES (?,?,?)";

			while (rs.next()) {
				//insert records into the art_user_jobs table so that the users can have access to the job
				Object[] values = {
					jobId,
					rs.getString("USERNAME"),
					rs.getString("USER_GROUP_ID")
				};

				dbService.update(sql, values);
			}
		} finally {
			DatabaseUtils.close(rs, ps);
		}
	}

	private void runJob(Connection conn, boolean splitJob, String user, String userEmail) throws SQLException {
		runJob(conn, splitJob, user, userEmail, null, false);
	}

	private void runJob(Connection conn, boolean splitJob, String user, String userEmail,
			Map<String, Map<String, String>> recipientDetails) throws SQLException {
		runJob(conn, splitJob, user, userEmail, recipientDetails, false);
	}

	//run job
	private void runJob(Connection conn, boolean splitJob, String user, String userEmail,
			Map<String, Map<String, String>> recipientDetails, boolean recipientFilterPresent) throws SQLException {
		//set job start date. relevant for split jobs
		jobStartDate = new Timestamp(System.currentTimeMillis());

		ReportRunner reportRunner = null;

		fileName = ""; //reset file name

		//create job audit record if auditing is enabled
		createAuditRecord(user);

		try {
			reportRunner = prepareReportRunner(user);

			if (recipientFilterPresent) {
				//enable report data to be filtered/different for each recipient
				reportRunner.setRecipientFilterPresent(recipientFilterPresent);
				for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
					//map should only have one value if filter present
					Map<String, String> recipientColumns = entry.getValue();
					reportRunner.setRecipientColumn(recipientColumns.get(ArtUtils.RECIPIENT_COLUMN));
					reportRunner.setRecipientId(recipientColumns.get(ArtUtils.RECIPIENT_ID));
					reportRunner.setRecipientIdType(recipientColumns.get(ArtUtils.RECIPIENT_ID_TYPE));
				}
			}

			//prepare report parameters
			ParameterProcessorResult paramProcessorResult = buildParameters(job.getReport().getReportId(), jobId);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportRunner.setReportParamsMap(reportParamsMap);

			ReportType reportType = job.getReport().getReportType();

			//jobs don't show record count so generally no need for scrollable resultsets
			int resultSetType;
			if (reportType.isChart()) {
				//need scrollable resultset for charts for show data option
				resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
			} else {
				resultSetType = ResultSet.TYPE_FORWARD_ONLY;
			}
			
			reportRunner.execute(resultSetType);
			
			userEmail = StringUtils.trim(userEmail);

			String message = job.getMailMessage();
			String outputFormat = job.getOutputFormat();
			String queryName = job.getReport().getName();

			String tos = job.getMailTo();
			String cc = job.getMailCc();
			String bcc = job.getMailBcc();

			//trim address fields. to aid in checking if emails are configured
			tos = StringUtils.trim(tos);
			cc = StringUtils.trim(cc);
			bcc = StringUtils.trim(bcc);

			//determine if emailing is required and emails are configured
			boolean generateEmail = false;
			if (jobType.isPublish()) {
				//for split published jobs, tos should have a value to enable confirmation email for individual users
				if (!StringUtils.equals(tos, userEmail) && (StringUtils.length(tos) > 4
						|| StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)
						&& StringUtils.length(userEmail) > 4) {
					generateEmail = true;
				} else if (StringUtils.equals(tos, userEmail) && (StringUtils.length(tos) > 4
						|| StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)) {
					generateEmail = true;
				}
			} else {
				//for non-publish jobs, if an email address is available, generate email
				if (StringUtils.length(userEmail) > 4 || StringUtils.length(cc) > 4
						|| StringUtils.length(bcc) > 4) {
					generateEmail = true;
				}
			}

			//set email fields
			String[] tosEmail = null;
			String[] ccs = null;
			String[] bccs = null;
			if (generateEmail) {
				tosEmail = StringUtils.split(userEmail, ";");
				ccs = StringUtils.split(cc, ";");
				bccs = StringUtils.split(bcc, ";");

				logger.debug("Job Id {}. to: {}", jobId, userEmail);
				logger.debug("Job Id {}. cc: {}", jobId, cc);
				logger.debug("Job Id {}. bcc: {}", jobId, bcc);
			}

			if (jobType == JobType.Alert) {
				/*
				 * ALERT if the resultset is not null and the first column is a
				 * positive integer => send the alert email
				 */

				//only run alert query if we have some emails configured
				if (generateEmail || recipientDetails != null) {
					runMessage = "jobs.message.noAlert";

					ResultSet rs = null;
					try {
						rs = reportRunner.getResultSet();
						if (rs.next()) {
							int value = rs.getInt(1);
							if (value > 0) {
								runMessage = "jobs.message.alertExists";

								logger.debug("Job Id {} - Raising Alert. Value is {}", jobId, value);

								//send customized emails to dynamic recipients
								if (recipientDetails != null) {
									Mailer mailer = getMailer();

									for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
										String email = entry.getKey();
										Map<String, String> recipientColumns = entry.getValue();

										//customize message by replacing field labels with values for this recipient
										String customMessage = message; //message for a particular recipient. may include personalization e.g. Dear Jane
										if (customMessage == null) {
											customMessage = "";
										}

										if (StringUtils.isNotBlank(customMessage)) {
											for (Map.Entry<String, String> entry2 : recipientColumns.entrySet()) {
												String columnName = entry2.getKey();
												String columnValue = entry2.getValue();

												String searchString = Pattern.quote("#" + columnName + "#"); //quote in case it contains special regex characters
												String replaceString = Matcher.quoteReplacement(columnValue); //quote in case it contains special regex characters
												customMessage = customMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
											}
										}

										prepareAlertJob(mailer, customMessage);

										mailer.setTo(email);

										//send email for this recipient
										try {
											sendEmail(mailer);
											runMessage = "jobs.message.alertSent";
										} catch (MessagingException ex) {
											logger.debug("Error", ex);
											runMessage = "jobs.message.errorSendingAlert";
											runDetails = "<b>Error: </b> <p>" + ex.toString() + "</p>";
										}
									}

									if (recipientFilterPresent) {
										//don't run normal email job after filtered email sent
										generateEmail = false;
									}
								}

								//send email to normal recipients
								if (generateEmail) {
									Mailer mailer = getMailer();

									prepareAlertJob(mailer, message);

									//set recipients						
									mailer.setTo(tosEmail);
									mailer.setCc(ccs);
									mailer.setBcc(bccs);

									try {
										sendEmail(mailer);
										runMessage = "jobs.message.alertSent";
									} catch (MessagingException ex) {
										logger.debug("Error", ex);
										runMessage = "jobs.message.errorSendingAlert";
										runDetails = "<b>Error: </b> <p>" + ex.toString() + "</p>";
									}

								} else {
									logger.debug("Job Id {} - No Alert. Value is {}", jobId, value);
								}
							} else {
								logger.debug("Job Id {} - Empty resultset for alert", jobId);
							}
						}
					} finally {
						DatabaseUtils.close(rs);
					}
				} else {
					//no emails configured
					runMessage = "jobs.message.noEmailsConfigured";
				}
			} else if (jobType.isPublish() || jobType.isEmail()) {
				logger.debug("Job Id {} - Mail or Publish. Type: {}", jobId, jobType);

				//determine if the query returns records. to know if to generate output for conditional jobs
				boolean generateOutput = true;

				if (jobType.isConditional()) {
					//conditional job. check if resultset has records. no "recordcount" method so we have to execute query again
					ReportRunner countReportRunner = null;
					ResultSet rsCount = null;
					try {
						countReportRunner = prepareReportRunner(user);
						countReportRunner.setReportParamsMap(reportParamsMap);

						countReportRunner.execute();

						rsCount = countReportRunner.getResultSet();
						if (!rsCount.next()) {
							//no records
							generateOutput = false;
							runMessage = "jobs.message.noRecords";
						}
					} finally {
						DatabaseUtils.close(rsCount);

						if (countReportRunner != null) {
							countReportRunner.close();
						}
					}
				}

				//for emailing jobs, only run query if some emails are configured
				if (jobType.isEmail()) {
					//email attachment, email inline, conditional email attachment, conditional email inline
					if (!generateEmail && recipientDetails == null) {
						generateOutput = false;
						runMessage = "jobs.message.noEmailsConfigured";
					}
				}

				if (generateOutput) {
					Report report = job.getReport();
					ReportFormat reportFormat = ReportFormat.toEnum(job.getOutputFormat());

					//generate output
					//generate file name to use for report types and formats that generate files
					FilenameHelper filenameHelper = new FilenameHelper();
					String baseFileName = filenameHelper.getFileName(job);
					String exportPath = Config.getJobsExportPath();

					String extension;
					if (reportType.isJxls()) {
						String jxlsFilename = report.getTemplate();
						extension = FilenameUtils.getExtension(jxlsFilename);
					} else {
						extension = reportFormat.getFilenameExtension();
					}

					fileName = baseFileName + "." + extension;
					String outputFileName = exportPath + fileName;

					//printwriter not needed for all output types. Avoid creating extra html file when output is not html, xml or rss
					FileOutputStream fos = null;
					PrintWriter writer = null;

					if (reportFormat.isHtml()
							|| reportFormat == ReportFormat.xml
							|| reportFormat == ReportFormat.rss20) {
						fos = new FileOutputStream(outputFileName);
						writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
					}

					ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();
					reportOutputGenerator.setJobId(jobId);

					Locale locale = Locale.getDefault();
					reportOutputGenerator.generateOutput(report, reportRunner,
							reportFormat, locale, paramProcessorResult, writer, outputFileName);

					// the file is on the PrintWriter (for html)
					if (writer != null) {
						writer.close();
					}
					
					if (fos != null) {
						fos.close();
					}

					if (generateEmail || recipientDetails != null) {
						//some kind of emailing required

						//send customized emails to dynamic recipients
						if (recipientDetails != null) {
							Mailer mailer = getMailer();

							String email;

							for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
								email = entry.getKey();
								Map<String, String> recipientColumns = entry.getValue();

								//customize message by replacing field labels with values for this recipient
								String customMessage = message; //message for a particular recipient. may include personalization e.g. Dear Jane
								if (customMessage == null) {
									customMessage = "";
								}

								if (StringUtils.isNotBlank(customMessage)) {
									for (Map.Entry<String, String> entry2 : recipientColumns.entrySet()) {
										String columnName = entry2.getKey();
										String columnValue = entry2.getValue();

										String searchString = Pattern.quote("#" + columnName + "#"); //quote in case it contains special regex characters
										String replaceString = Matcher.quoteReplacement(columnValue); //quote in case it contains special regex characters
										customMessage = customMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
									}
								}

								prepareEmailJob(mailer, customMessage, outputFileName);

								mailer.setTo(email);

								//send email for this recipient
								try {
									sendEmail(mailer);
									runMessage = "jobs.message.fileEmailed";
								} catch (MessagingException ex) {
									logger.debug("Error", ex);
									runMessage = "jobs.message.errorSendingSomeEmails";
									runDetails = "<b>Error: </b>"
											+ " <p>" + ex.toString() + "</p>";

									String msg = "Error when sending some emails."
											+ " \n" + ex.toString()
											+ " \n To: " + email;
									logger.warn(msg);
								}
							}

							if (recipientFilterPresent) {
								//don't run normal email job after filtered email sent
								generateEmail = false;
							}

							//set filename to status of last recipient email sent
							File f = new File(outputFileName);
							boolean deleted = f.delete();
							if (!deleted) {
								logger.warn("Email attachment file not deleted: {}", outputFileName);
							}
						}

						//send email to normal recipients
						if (generateEmail) {
							Mailer mailer = getMailer();

							prepareEmailJob(mailer, message, outputFileName);

							//set recipients						
							mailer.setTo(tosEmail);
							mailer.setCc(ccs);
							mailer.setBcc(bccs);

							//check if mail was successfully sent
							try {
								sendEmail(mailer);
								runMessage = "jobs.message.fileEmailed";

								if (jobType.isEmail()) {
									// delete the file since it has
									// been sent via email (for publish jobs it is deleted by the scheduler)
									File file = new File(outputFileName);
									file.delete();
									fileName = "";
								} else {
									runMessage = "jobs.message.reminderSent";
								}
							} catch (MessagingException ex) {
								logger.debug("Error", ex);
								runMessage = "jobs.message.errorSendingSomeEmails";
								runDetails = "<b>Error: </b>"
										+ " <p>" + ex.toString() + "</p>";

								String msg = "Error when sending some emails."
										+ " \n" + ex.toString()
										+ " \n Complete address list:\n To: " + userEmail + "\n Cc: " + cc + "\n Bcc: " + bcc;
								logger.warn(msg);
							}

						}
					}
				}

			} else if (jobType.isCache()) {
				// Cache the result in the cache database
				int targetDatabaseId = Integer.parseInt(outputFormat);

				String cachedTableName = job.getCachedTableName();

				Connection cacheDatabaseConnection = null;
				ResultSet rs = null;
				try {
					cacheDatabaseConnection = DbConnections.getConnection(targetDatabaseId);
					rs = reportRunner.getResultSet();
					CachedResult cr = new CachedResult();
					cr.setTargetConnection(cacheDatabaseConnection);
					cr.setResultSet(rs);
					if (cachedTableName == null || cachedTableName.length() == 0) {
						cachedTableName = queryName + "_J" + jobId;
					}
					cr.setCachedTableName(cachedTableName);
					if (jobType == JobType.CacheAppend) {
						// 1 = append 2 = drop/insert (3 = update (not implemented))
						cr.setCacheMode(1);
					} else if (jobType == JobType.CacheInsert) {
						cr.setCacheMode(2);
					}
					cr.cacheIt();
					runDetails = "Table Name (rows inserted):  <code>"
							+ cr.getCachedTableName() + "</code> (" + cr.getRowsCount() + ")"
							+ "<br />Columns Names:<br /><code>"
							+ cr.getCachedTableColumnsName() + "</code>";
				} finally {
					DatabaseUtils.close(rs, cacheDatabaseConnection);
				}

			} else { // jobType 4:just run it.
				// This is used Used to start batch jobs at db level via calls to stored procs
				// or just to run update statements.
			}

			logger.debug("Job Id {} ...finished", jobId);
		} catch (Exception ex) {
			runDetails = "<b>Error:</b> " + ex.toString();
			logger.error("Error", ex);
		} finally {
			if (reportRunner != null) {
				reportRunner.close();
			}
			// set audit timestamp and update archives
			afterExecution(conn, splitJob, user);
		}
	}

	private ReportRunner prepareReportRunner(String user) throws SQLException {
		return prepareReportRunner(user, job.getReport());
	}

	/**
	 * Prepares a job for its execution Loads additional info needed to execute
	 * (immediately) the job (query id, datasource etc).
	 */
	private ReportRunner prepareReportRunner(String user, int reportId) throws SQLException {

		ReportService reportService = new ReportService();
		Report report = reportService.getReport(reportId);

		return prepareReportRunner(user, report);
	}

	/**
	 * Prepares a job for its execution Loads additional info needed to execute
	 * (immediately) the job (query id, datasource etc).
	 */
	private ReportRunner prepareReportRunner(String user, Report report) throws SQLException {

		ReportRunner reportRunner = new ReportRunner();
		reportRunner.setUsername(user);
		reportRunner.setReport(report);

		return reportRunner;

	}

	public ParameterProcessorResult buildParameters(int reportId, int jId) throws SQLException {
		ParameterProcessorResult paramProcessorResult = null;

		JobParameterService jobParameterService = new JobParameterService();
		List<JobParameter> jobParams = jobParameterService.getJobParameters(jId);
		Map<String, List<String>> paramValues = new HashMap<>();
		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String paramTypeString = jobParam.getParamTypeString();
			if (!StringUtils.equalsIgnoreCase(paramTypeString, "O")
					&& !StringUtils.startsWith(name, "p-")) {
				name = "p-" + name;
			}
			jobParam.setName(name);
			List<String> values = paramValues.get(name);
			if (values == null) {
				paramValues.put(name, new ArrayList<String>());
			}
		}

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String value = jobParam.getValue();
			List<String> values = paramValues.get(name);
			values.add(value);
		}

		Map<String, String[]> finalValues = new HashMap<>();

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			List<String> values = paramValues.get(name);
			String[] valuesArray = values.toArray(new String[0]);
			finalValues.put(name, valuesArray);
		}

		try {
			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessorResult = paramProcessor.process(finalValues, reportId);
		} catch (ParseException ex) {
			logger.error("Error", ex);
		}

		return paramProcessorResult;
	}

	//create job audit record
	private void createAuditRecord(String user) throws SQLException {
		if (job.isEnableAudit()) {
			//generate unique key for this job run
			jobAuditKey = generateKey();

			//insert job start audit values.
			String sql = "INSERT INTO ART_JOBS_AUDIT"
					+ " (JOB_ID, JOB_ACTION, JOB_AUDIT_KEY, START_DATE, USERNAME)"
					+ " VALUES (?, 'S', ?, ?, ?)";

			Object[] values = {
				jobId,
				jobAuditKey,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				user
			};

			dbService.update(sql, values);
		}
	}

	private String generateKey() {
		return ArtUtils.getUniqueId();
	}

	private Mailer getMailer() {
		String smtpServer = Config.getSettings().getSmtpServer();
		String smtpUsername = Config.getSettings().getSmtpUsername();
		String smtpPassword = Config.getSettings().getSmtpPassword();

		Mailer mailer = new Mailer();
		mailer.setHost(smtpServer);
		if (StringUtils.length(smtpUsername) > 3 && smtpPassword != null) {
			mailer.setUsername(smtpUsername);
			mailer.setPassword(smtpPassword);
		}

		mailer.setPort(Config.getSettings().getSmtpPort());
		mailer.setUseAuthentication(Config.getSettings().isUseSmtpAuthentication());
		mailer.setUseStartTls(Config.getSettings().isSmtpUseStartTls());

		return mailer;
	}

	/**
	 * Update ART_USER_JOBS table. If Audit Flag is set, a new row is added to
	 * ART_JOBS_AUDIT table
	 */
	private void afterExecution(Connection conn, boolean splitJob, String user) throws SQLException {
		String sql;

		if (jobType == JobType.Publish || jobType == JobType.CondPublish) {

			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				//for publish jobs, delete previous file and update job archives as necessary
				sql = "SELECT LAST_FILE_NAME, LAST_START_DATE, LAST_END_DATE"
						+ " FROM ART_JOBS"
						+ " WHERE JOB_ID = ?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, jobId);
				rs = ps.executeQuery();

				String archiveFileName;
				Timestamp archiveStartDate;
				Timestamp archiveEndDate;

				if (rs.next()) {
					archiveFileName = rs.getString("LAST_FILE_NAME");
					archiveStartDate = rs.getTimestamp("LAST_START_DATE");
					archiveEndDate = rs.getTimestamp("LAST_END_DATE");

					int runsToArchive = job.getRunsToArchive();

					if (runsToArchive > 0 && archiveFileName != null) {
						//update archives
						updateArchives(splitJob, user, archiveFileName, archiveStartDate, archiveEndDate);
					} else {
						//if not archiving, delete previous file
						if (archiveFileName != null && !archiveFileName.startsWith("-")) {
							List<String> details = ArtUtils.getFileDetailsFromResult(archiveFileName);
							archiveFileName = details.get(0);
							String filePath = Config.getJobsExportPath() + archiveFileName;
							File previousFile = new File(filePath);
							if (previousFile.exists()) {
								previousFile.delete();
							}
						}

						//delete old archives if they exist
						if (runsToArchive == 0) {
							deleteArchives();
						}
					}
				}
			} finally {
				DatabaseUtils.close(rs, ps);
			}
		}

		//update job details
		//no need to update jobs table if non-split job. aftercompletion will do the final update to the jobs table
		if (splitJob) {
			sql = "UPDATE ART_USER_JOBS SET LAST_FILE_NAME = ?,"
					+ " LAST_RUN_DETAILS=?, LAST_RUN_MESSAGE=?,"
					+ " LAST_START_DATE = ?, LAST_END_DATE = ? "
					+ " WHERE JOB_ID = ? AND USERNAME = ?";

			if (runDetails.length() > 4000) {
				runDetails = runDetails.substring(0, 4000);
			}

			Object[] values = {
				fileName,
				runDetails,
				runMessage,
				jobStartDate,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobId,
				user
			};

			dbService.update(sql, values);
		}

		//update audit table if required
		if (job.isEnableAudit()) {
			sql = "UPDATE ART_JOBS_AUDIT SET JOB_ACTION = 'E',"
					+ " END_DATE = ? WHERE JOB_AUDIT_KEY = ? AND JOB_ID = ?";

			Object[] values = {
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobAuditKey,
				jobId
			};

			dbService.update(sql, values);
		}
	}

	/**
	 * Update job archives table
	 */
	private void updateArchives(boolean splitJob, String user, String archiveFileName,
			Timestamp lastStartDate, Timestamp lastEndDate) {

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			conn = DbConnections.getArtDbConnection();

			String sql;

			boolean jobShared = false;
			if (job.isAllowSharing()) {
				jobShared = true;
			}

			String jobSharedFlag;
			if (splitJob) {
				jobSharedFlag = "S";
			} else if (jobShared) {
				jobSharedFlag = "Y";
			} else {
				jobSharedFlag = "N";
			}

			String archiveId = generateKey();
			int runsToArchive = job.getRunsToArchive();

			//add record to archive
			sql = "INSERT INTO ART_JOB_ARCHIVES"
					+ " (ARCHIVE_ID,JOB_ID,USERNAME,ARCHIVE_FILE_NAME,"
					+ " START_DATE,END_DATE,JOB_SHARED)"
					+ " VALUES(?,?,?,?,?,?,?)";

			Object[] values = {
				archiveId,
				jobId,
				user,
				archiveFileName,
				lastStartDate,
				lastEndDate,
				jobSharedFlag
			};

			dbService.update(sql, values);

			//delete previous run's records
			List<String> oldRecords = new ArrayList<>();
			if (jobSharedFlag.equals("S")) {
				sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
						+ " FROM ART_JOB_ARCHIVES "
						+ " WHERE JOB_ID=? AND USERNAME=?"
						+ " ORDER BY START_DATE DESC";

				ps = conn.prepareStatement(sql);
				ps.setInt(1, jobId);
				ps.setString(2, user);

				rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = rs.getString("ARCHIVE_FILE_NAME");
						String oldArchive = rs.getString("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
							List<String> details = ArtUtils.getFileDetailsFromResult(oldFileName);
							oldFileName = details.get(0);
							String filePath = Config.getJobsExportPath() + oldFileName;
							File previousFile = new File(filePath);
							if (previousFile.exists()) {
								previousFile.delete();
							}
						}
					}
				}
			} else {
				sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
						+ " FROM ART_JOB_ARCHIVES "
						+ " WHERE JOB_ID=?"
						+ " ORDER BY START_DATE DESC";

				ps = conn.prepareStatement(sql);
				ps.setInt(1, jobId);

				rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = rs.getString("ARCHIVE_FILE_NAME");
						String oldArchive = rs.getString("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
							List<String> details = ArtUtils.getFileDetailsFromResult(oldFileName);
							oldFileName = details.get(0);
							String filePath = Config.getJobsExportPath() + oldFileName;
							File previousFile = new File(filePath);
							if (previousFile.exists()) {
								previousFile.delete();
							}
						}
					}
				}
			}

			//delete old archive records
			if (oldRecords.size() > 0) {
				String oldRecordsString = StringUtils.join(oldRecords, ",");
				sql = "DELETE FROM ART_JOB_ARCHIVES WHERE ARCHIVE_ID IN(" + oldRecordsString + ")";
				dbService.update(sql);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Delete all records from job archives table for this job id
	 */
	private void deleteArchives() {
		Connection conn = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DbConnections.getArtDbConnection();

			String sql;

			List<String> oldRecords = new ArrayList<>();

			sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
					+ " FROM ART_JOB_ARCHIVES "
					+ " WHERE JOB_ID=?";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, jobId);

			rs = ps.executeQuery();
			while (rs.next()) {
				//delete archive file and database record
				String oldFileName = rs.getString("ARCHIVE_FILE_NAME");
				String oldArchive = rs.getString("ARCHIVE_ID");

				//remember database record for deletion
				oldRecords.add("'" + oldArchive + "'");

				//delete file
				if (oldFileName != null && !oldFileName.startsWith("-")) {
					List<String> details = ArtUtils.getFileDetailsFromResult(oldFileName);
					oldFileName = details.get(0);
					String filePath = Config.getJobsExportPath() + oldFileName;
					File previousFile = new File(filePath);
					if (previousFile.exists()) {
						previousFile.delete();
					}
				}
			}

			//delete old archive records
			if (oldRecords.size() > 0) {
				String oldRecordsString = StringUtils.join(oldRecords, ",");
				sql = "DELETE FROM ART_JOB_ARCHIVES WHERE ARCHIVE_ID IN(" + oldRecordsString + ")";
				dbService.update(sql);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(rs, ps, conn);
		}
	}
}

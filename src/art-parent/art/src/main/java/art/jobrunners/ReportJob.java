/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobrunners;

import art.cache.CacheHelper;
import art.connectionpool.DbConnections;
import art.dashboard.PdfDashboard;
import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.destination.Destination;
import art.destination.GoogleServiceAccountJsonKey;
import art.destinationoptions.S3AwsSdkOptions;
import art.destinationoptions.FtpOptions;
import art.destinationoptions.NetworkShareOptions;
import art.destinationoptions.SftpOptions;
import art.destinationoptions.WebsiteOptions;
import art.enums.DestinationType;
import art.enums.JobType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.job.JobOptions;
import art.job.JobService;
import art.job.JobUtils;
import art.jobparameter.JobParameterService;
import art.mail.Mailer;
import art.output.FreeMarkerOutput;
import art.output.StandardOutput;
import art.output.ThymeleafOutput;
import art.output.VelocityOutput;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOptions;
import art.runreport.ReportOutputGenerator;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.smtpserver.SmtpServer;
import art.user.User;
import art.utils.ArtHelper;
import art.utils.ArtUtils;
import art.utils.CachedResult;
import art.utils.ExpressionHelper;
import art.utils.FilenameHelper;
import art.utils.FinalFilenameValidator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.inject.Module;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.utils.SmbFiles;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import freemarker.template.TemplateException;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.tika.Tika;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import static org.jclouds.blobstore.options.PutOptions.Builder.multipart;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Runs report jobs
 *
 * @author Timothy Anyona
 */
@Component
public class ReportJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(ReportJob.class);

	private String fileName;
	private String jobAuditKey;
	private art.job.Job job;
	private Timestamp jobStartDate;
	private JobType jobType;
	private int jobId;
	private String runDetails;
	private String runMessage;
	private Locale locale;
	private ch.qos.logback.classic.Logger progressLogger;
	private long runStartTimeMillis;
	private FileAppender<ILoggingEvent> progressFileAppender;
	private JobOptions jobOptions;

	@Autowired
	private TemplateEngine jobTemplateEngine;

	@Autowired
	private TemplateEngine defaultTemplateEngine;

	@Autowired
	private CacheHelper cacheHelper;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private JobService jobService;

	@Autowired
	private DbService dbService;

	@Autowired
	ReportService reportService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//https://stackoverflow.com/questions/4258313/how-to-use-autowired-in-a-quartz-job
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		runStartTimeMillis = System.currentTimeMillis();

		JobDataMap dataMap = context.getMergedJobDataMap();
		jobId = dataMap.getInt("jobId");

		initializeProgressLogger();

		progressLogger.info("Started");

		try {
			if (!Config.getSettings().isSchedulingEnabled()) {
				jobLogAndClose("Scheduling not enabled");
				return;
			}

			try {
				job = jobService.getJob(jobId);
			} catch (SQLException ex) {
				logError(ex);
			}

			if (job == null) {
				logger.warn("Job not found: {}", jobId);
				jobLogAndClose("Job not found");
				return;
			}

			Report report = job.getReport();
			if (report == null) {
				logger.warn("Job report not found. Job Id {}", jobId);
				jobLogAndClose("Job report not found");
				return;
			}

			User user = job.getUser();
			if (user == null) {
				logger.warn("Job user not found. Job Id {}", jobId);
				jobLogAndClose("Job user not found");
				return;
			}

			jobType = job.getJobType();

			fileName = "";
			runDetails = "";
			runMessage = "";

			String systemLocale = Config.getSettings().getSystemLocale();
			logger.debug("systemLocale='{}'", systemLocale);

			locale = ArtUtils.getLocaleFromString(systemLocale);

			String options = job.getOptions();
			if (StringUtils.isBlank(options)) {
				jobOptions = new JobOptions();
			} else {
				jobOptions = ArtUtils.jsonToObject(options, JobOptions.class);
			}

			//get next run date	for the job for updating the jobs table. only update if it's a scheduled run and not an interactive, temporary job
			boolean tempJob = dataMap.getBooleanValue("tempJob");
			Date nextRunDate = null;
			if (tempJob) {
				//temp job. use existing next run date
				nextRunDate = job.getNextRunDate();
			} else {
				//not a temp job. set new next run date
				//get least next run date as job may have multiple triggers
				try {
					Scheduler scheduler = context.getScheduler();
					JobDetail quartJob = context.getJobDetail();
					@SuppressWarnings("unchecked")
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(quartJob.getKey());
					nextRunDate = JobUtils.getNextFireTime(triggers, scheduler);
				} catch (SchedulerException ex) {
					logError(ex);
				}
			}

			//set overall job start time in the jobs table
			beforeExecution(nextRunDate);

			if (!job.isActive()) {
				runMessage = "jobs.message.jobDisabled";
			} else if (!job.getReport().isActive()) {
				runMessage = "jobs.message.reportDisabled";
			} else if (!job.getUser().isActive()) {
				runMessage = "jobs.message.ownerDisabled";
			} else {
				runPreRunReports();
				if (job.getRecipientsReportId() > 0) {
					//job has dynamic recipients
					runDynamicRecipientsJob();
				} else {
					//job doesn't have dynamic recipients
					runNormalJob();
				}
			}

			sendFileToDestinations();
			runBatchFile();
			runPostRunReports();
		} catch (Exception ex) {
			logErrorAndSetDetails(ex);
		}

		afterCompletion();
		cacheHelper.clearJobs();

		sendErrorNotification();

		long runEndTimeMillis = System.currentTimeMillis();

		//https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/time/DurationFormatUtils.html
		String durationFormat = "m':'s':'S";
		String duration = DurationFormatUtils.formatPeriod(runStartTimeMillis, runEndTimeMillis, durationFormat);
		progressLogger.info("Completed. Time taken - {}", duration);
		progressLogger.detachAndStopAllAppenders();
		progressLogger.setLevel(Level.OFF);
	}

	/**
	 * Run pre run reports
	 *
	 * @throws SQLException
	 */
	private void runPreRunReports() throws SQLException {
		runReports(job.getPreRunReport());
	}

	/**
	 * Run post run reports
	 */
	private void runPostRunReports() throws SQLException {
		runReports(job.getPostRunReport());
	}

	/**
	 * Run pre/post run reports
	 *
	 * @param reportIds comma separated list of report ids to run
	 * @throws SQLException
	 */
	private void runReports(String reportIds) throws SQLException {
		logger.debug("Entering runReports: reportIds='{}'", reportIds);

		if (StringUtils.isBlank(reportIds)) {
			return;
		}

		String[] tempReportIdsArray = StringUtils.split(reportIds, ",");
		String[] reportIdsArray = StringUtils.stripAll(tempReportIdsArray);

		for (String reportIdString : reportIdsArray) {
			int reportId = Integer.parseInt(reportIdString);
			logger.debug("reportId={}", reportId);
			Report report = reportService.getReport(reportId);
			User jobUser = job.getUser();
			if (report == null) {
				throw new IllegalArgumentException("Pre/Post run report not found: " + reportId);
			} else if (!reportService.canUserRunReport(jobUser.getUserId(), reportId)) {
				throw new IllegalStateException("Job owner doesn't have access to pre/post run report: " + jobUser.getUsername() + " - " + reportId);
			}
			ReportRunner reportRunner = prepareReportRunner(jobUser, report);
			try {
				ParameterProcessorResult paramProcessorResult = buildParameters(reportId, jobId, jobUser);
				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				reportRunner.setReportParamsMap(reportParamsMap);
				reportRunner.execute();
			} finally {
				reportRunner.close();
			}
		}
	}

	/**
	 * Sends an email to configured email addresses if there was an error while
	 * running the job
	 *
	 */
	private void sendErrorNotification() {
		String errorNotificationTo = job.getErrorNotificationTo();
		if (StringUtils.isBlank(errorNotificationTo)) {
			return;
		}

		if (StringUtils.startsWith(runDetails, "<b>Error:</b>")) {
			if (!Config.getCustomSettings().isEnableEmailing()) {
				logger.info("Emailing disabled. Job Id {}", jobId);
			} else {
				ArtHelper artHelper = new ArtHelper();
				Mailer mailer = artHelper.getMailer();
				mailer.setDebug(logger.isDebugEnabled());

				String[] emailsArray = separateEmails(errorNotificationTo);
				String subject = "ART [Job Error]: " + job.getName() + " (" + jobId + ")";

				mailer.setTo(emailsArray);
				mailer.setFrom(Config.getSettings().getErrorNotificationFrom());
				mailer.setSubject(subject);

				try {
					Context ctx = new Context(locale);
					ctx.setVariable("error", runDetails);
					ctx.setVariable("job", job);

					String emailTemplateName = "jobErrorEmail";
					String message = defaultTemplateEngine.process(emailTemplateName, ctx);
					mailer.setMessage(message);

					mailer.send();
				} catch (IOException | MessagingException | RuntimeException ex) {
					logError(ex);
				}
			}
		}
	}

	/**
	 * Logs a message to the progress logger and closes the job log file
	 * appender
	 *
	 * @param message the message to log
	 */
	private void jobLogAndClose(String message) {
		runDetails = message;
		progressLogger.info(runDetails);
		progressLogger.detachAndStopAllAppenders();
		progressLogger.setLevel(Level.OFF);
		updateIncompleteRun();
	}

	/**
	 * Initializes the job progress logger
	 */
	private void initializeProgressLogger() {
		//https://stackoverflow.com/questions/7824620/logback-set-log-file-name-programmatically
		//http://oct.im/how-to-create-logback-loggers-dynamicallypragmatically.html
		//https://www.programcreek.com/java-api-examples/index.php?api=ch.qos.logback.core.FileAppender
		//https://stackoverflow.com/questions/7824620/logback-set-log-file-name-programmatically
		//http://mailman.qos.ch/pipermail/logback-user/2008-November/000800.html

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		String progressLogFilename = Config.getJobLogsPath() + jobId + ".log";

		progressFileAppender = new FileAppender<>();
		progressFileAppender.setContext(loggerContext);
		progressFileAppender.setName("jobLogAppender" + jobId);
		progressFileAppender.setFile(progressLogFilename);
		progressFileAppender.setAppend(false);

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern("[%level] %date{dd-MMM-yyyy HH:mm:ss.SSS} - %msg%n");
		encoder.start();

		progressFileAppender.setEncoder(encoder);
		progressFileAppender.start();

		progressLogger = loggerContext.getLogger("jobLog" + jobId);
		progressLogger.setLevel(Level.INFO);

		// Don't inherit root appender
		progressLogger.setAdditive(false);
		progressLogger.addAppender(progressFileAppender);
	}

	/**
	 * Log an error to stdout and to the progress logger
	 *
	 * @param ex the error
	 */
	private void logError(Throwable ex) {
		logger.error("Error. Job Id {}", jobId, ex);
		progressLogger.error("Error", ex);
	}

	/**
	 * Log an error as well as set the runDetails variable
	 *
	 * @param ex the error
	 */
	private void logErrorAndSetDetails(Throwable ex) {
		logError(ex);
		runDetails = "<b>Error:</b> " + ex.toString();
	}

	/**
	 * Sends the generated job file to destinations set for the job
	 */
	private void sendFileToDestinations() {
		List<Destination> destinations = job.getDestinations();

		if (CollectionUtils.isEmpty(destinations)) {
			return;
		}

		String jobsExportPath = Config.getJobsExportPath();
		String fullLocalFileName = jobsExportPath + fileName;

		for (Destination destination : destinations) {
			if (destination.isActive()) {
				DestinationType destinationType = destination.getDestinationType();
				switch (destinationType) {
					case FTP:
					case SFTP:
						ftpFile(destination, fullLocalFileName);
						break;
					case NetworkShare:
						sendFileToNetworkShare(destination, fullLocalFileName);
						break;
					case S3jclouds:
						sendFileToS3jclouds(destination, fullLocalFileName);
						break;
					case S3AwsSdk:
						sendFileToS3AwsSdk(destination, fullLocalFileName);
						break;
					case Azure:
						sendFileToAzure(destination, fullLocalFileName);
						break;
					case GoogleCloudStorage:
						sendFileToGoogleCloudStorage(destination, fullLocalFileName);
						break;
					case WebDav:
						sendFileToWebDav(destination, fullLocalFileName);
						break;
					case Website:
						sendFileToWebsite(destination, fullLocalFileName);
						break;
					case B2:
						sendFileToB2(destination, fullLocalFileName);
						break;
					default:
						throw new IllegalArgumentException("Unexpected destination type: " + destinationType);
				}
			}
		}
	}

	/**
	 * Copies the generated file to backblaze b2 storage
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToB2(Destination destination, String fullLocalFileName) {
		String provider = "b2";
		sendFileToBlobStorage(provider, destination, fullLocalFileName);
	}

	/**
	 * Copies the generated file to a web address that accepts file uploads
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToWebsite(Destination destination, String fullLocalFileName) {
		logger.debug("Entering sendFileToWebsite: destination={},"
				+ " fullLocalFileName='{}'", destination, fullLocalFileName);

		//https://stackoverflow.com/questions/7370771/how-to-post-files-using-jsoup
		//https://stackoverflow.com/questions/39814877/jsoup-login-to-website-and-visit
		//https://stackoverflow.com/questions/1445919/how-to-enable-wire-logging-for-a-java-httpurlconnection-traffic
		//https://stackoverflow.com/questions/30406264/cannot-login-to-website-by-using-jsoup-with-x-www-form-urlencoded-parameters
		//http://neembuuuploader.sourceforge.net/wiki/index.php/Identifying_the_HTTP_GET/POST_requests_for_upload
		try {
			WebsiteOptions websiteOptions;
			String options = destination.getOptions();
			if (StringUtils.isBlank(options)) {
				websiteOptions = new WebsiteOptions();
			} else {
				websiteOptions = ArtUtils.jsonToObject(options, WebsiteOptions.class);
			}

			Map<String, String> cookies = null;

			String username = destination.getUser();
			String password = destination.getPassword();
			String startUrl = websiteOptions.getStartUrl();
			String loginUrl = websiteOptions.getLoginUrl();
			String usernameField = websiteOptions.getUsernameField();
			String passwordField = websiteOptions.getPasswordField();
			String fileField = websiteOptions.getFileField();
			String csrfTokenInputField = websiteOptions.getCsrfTokenInputField();
			String csrfTokenCookie = websiteOptions.getCsrfTokenCookie();
			String csrfTokenOutputField = websiteOptions.getCsrfTokenOutputField();
			String csrfTokenValue = null;

			if (StringUtils.isNotBlank(startUrl)) {
				Response response = Jsoup.connect(startUrl)
						.method(Method.GET)
						.execute();
				cookies = response.cookies();
				if (StringUtils.isNotBlank(csrfTokenCookie)) {
					csrfTokenValue = cookies.get(csrfTokenCookie);
					logger.debug("csrfTokenValue='{}'", csrfTokenValue);
				} else if (StringUtils.isNotBlank(csrfTokenInputField)) {
					Document doc = response.parse();
					//doc.select().val() doesn't throw an error if field not there. returns empty string if field not there
					csrfTokenValue = doc.select("input[name=" + csrfTokenInputField + "]").val();
					logger.debug("csrfTokenValue='{}'", csrfTokenValue);
				}
			}
			if (StringUtils.isNotBlank(loginUrl) && StringUtils.isNotBlank(username)) {
				Response response = Jsoup.connect(loginUrl)
						.data(usernameField, username)
						.data(passwordField, password)
						.method(Method.POST)
						.execute();
				cookies = response.cookies();
				if (StringUtils.isNotBlank(csrfTokenCookie)) {
					csrfTokenValue = cookies.get(csrfTokenCookie);
					logger.debug("csrfTokenValue='{}'", csrfTokenValue);
				} else if (StringUtils.isNotBlank(csrfTokenInputField)) {
					Document doc = response.parse();
					csrfTokenValue = doc.select("input[name=" + csrfTokenInputField + "]").val();
					logger.debug("csrfTokenValue='{}'", csrfTokenValue);
				}
			}
			String path = destination.getPath();
			File file = new File(fullLocalFileName);
			try (FileInputStream fis = new FileInputStream(file)) {
				org.jsoup.Connection connection = Jsoup.connect(path)
						.ignoreContentType(true)
						.data(fileField, file.getName(), fis);
				if (cookies != null) {
					connection.cookies(cookies);
				}
				List<Map<String, String>> staticFields = websiteOptions.getStaticFields();
				if (CollectionUtils.isNotEmpty(staticFields)) {
					for (Map<String, String> staticField : staticFields) {
						connection.data(staticField);
					}
				}
				if (StringUtils.isNotBlank(csrfTokenOutputField)) {
					connection.data(csrfTokenOutputField, csrfTokenValue);
				}
				Document document = connection.post();
				String postReply = document.body().text();
				logger.debug("postReply='{}'", postReply);
			}
		} catch (IOException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		}
	}

	/**
	 * Copies the generated file to a webdav server location
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToWebDav(Destination destination, String fullLocalFileName) {
		logger.debug("Entering sendFileToWebDav: destination={},"
				+ " fullLocalFileName='{}'", destination, fullLocalFileName);

		String username = destination.getUser();
		logger.debug("username='{}'", username);
		String password = destination.getPassword();
		Sardine sardine;
		if (StringUtils.isBlank(username)) {
			sardine = SardineFactory.begin();
		} else {
			sardine = SardineFactory.begin(username, password);
		}

		String mainUrl = destination.getPath();
		logger.debug("mainUrl='{}'", mainUrl);
		if (!StringUtils.endsWith(mainUrl, "/")) {
			mainUrl = mainUrl + "/";
		}

		String destinationSubDirectory = destination.getSubDirectory();
		logger.debug("destinationSubDirectory='{}'", destinationSubDirectory);
		String jobSubDirectory = job.getSubDirectory();
		logger.debug("jobSubDirectory='{}'", jobSubDirectory);

		String directorySeparator = "/";
		String finalSubDirectory = combineSubDirectoryPaths(directorySeparator, destinationSubDirectory, jobSubDirectory);
		logger.debug("finalSubDirectory='{}'", finalSubDirectory);

		logger.debug("destination.isCreateDirectories()={}", destination.isCreateDirectories());

		// if file is in folder(s), create them first
		if (StringUtils.isNotBlank(finalSubDirectory)
				&& destination.isCreateDirectories()) {
			//can't create directory hierarchy in one go. create sub-directories one at a time
			String[] folders = StringUtils.split(finalSubDirectory, "/");
			//https://stackoverflow.com/questions/4078642/create-a-folder-hierarchy-through-ftp-in-java
			List<String> subFolders = new ArrayList<>();
			for (String folder : folders) {
				subFolders.add(folder);
				String partialPath = StringUtils.join(subFolders, "/");
				partialPath = mainUrl + partialPath;
				try {
					String url = ArtUtils.encodeMainUrl(partialPath);
					logger.debug("url='{}'", url);
					if (!sardine.exists(url)) {
						logger.debug("Creating directory - '{}'", url);
						sardine.createDirectory(url);
					}
				} catch (IOException | URISyntaxException ex) {
					logger.error("Error while creating sub-directory. Job Id {}", jobId, ex);
				}
			}
		}

		try {
			String finalPath = mainUrl + finalSubDirectory + fileName;
			logger.debug("finalPath='{}'", finalPath);

			String url = ArtUtils.encodeMainUrl(finalPath);
			logger.debug("url='{}'", url);

			try (InputStream fis = new FileInputStream(new File(fullLocalFileName))) {
				sardine.put(url, fis);
			}
		} catch (IOException | URISyntaxException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			try {
				sardine.shutdown();
			} catch (IOException ex) {
				logError(ex);
			}
		}
	}

	/**
	 * Copies the generated file to microsoft azure blob storage
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToAzure(Destination destination, String fullLocalFileName) {
		String provider = "azureblob";
		sendFileToBlobStorage(provider, destination, fullLocalFileName);
	}

	/**
	 * Copies the generated file to google cloud storage
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToGoogleCloudStorage(Destination destination, String fullLocalFileName) {
		String provider = "google-cloud-storage";
		sendFileToBlobStorage(provider, destination, fullLocalFileName);
	}

	/**
	 * Copies the generated file to amazon s3 using the aws-sdk library
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToS3AwsSdk(Destination destination, String fullLocalFileName) {
		//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3.html
		//https://javatutorial.net/java-s3-example
		//https://docs.aws.amazon.com/AmazonS3/latest/dev/HLuploadFileJava.html
		//https://stackoverflow.com/questions/46276121/java-aws-sdk-s3-upload-performance
		//http://improve.dk/pushing-the-limits-of-amazon-s3-upload-performance/
		//http://javasampleapproach.com/aws/amazon-s3/amazon-s3-uploaddownload-large-files-s3-springboot-amazon-s3-multipartfile-application
		//https://stackoverflow.com/questions/6590088/uploading-large-files-with-user-metadata-to-amazon-s3-using-java-sdk
		//https://stackoverflow.com/questions/4698869/problems-when-uploading-large-files-to-amazon-s3
		AmazonS3 s3Client = null;

		try {
			String accessKeyId = destination.getUser();
			String secretAccessKey = destination.getPassword();

			//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-explicit
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
			AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withForceGlobalBucketAccessEnabled(true);

			S3AwsSdkOptions s3AwsSdkOptions;
			String options = destination.getOptions();
			if (StringUtils.isBlank(options)) {
				s3AwsSdkOptions = new S3AwsSdkOptions();
			} else {
				s3AwsSdkOptions = ArtUtils.jsonToObject(options, S3AwsSdkOptions.class);
			}

			String region = s3AwsSdkOptions.getRegion();

			if (StringUtils.isBlank(region)) {
				//must set a region, otherwise an exception will be thrown
				//https://stackoverflow.com/questions/17117648/can-the-s3-sdk-figure-out-a-buckets-region-on-its-own?noredirect=1&lq=1
				//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-logging.html
				//https://github.com/aws/aws-sdk-java/issues/1284
				//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-region-selection.html
				//https://stackoverflow.com/questions/43857570/setting-the-aws-region-programmatically
				//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
				//https://stackoverflow.com/questions/31485331/how-to-set-connection-type-in-aws-java-sdk-https-vs-http
				final String DEFAULT_REGION = "us-east-1";
				region = DEFAULT_REGION;
			}

			s3ClientBuilder.withRegion(region);
			s3Client = s3ClientBuilder.build();

			String bucketName = destination.getPath();

			String destinationSubDirectory = destination.getSubDirectory();
			String jobSubDirectory = job.getSubDirectory();

			String directorySeparator = "/";
			String finalPath = combineDirectoryPaths(directorySeparator, destinationSubDirectory, jobSubDirectory);

			String remoteFileName = finalPath + fileName;

			File localFile = new File(fullLocalFileName);

			String mimeType = "application/unknown";
			Tika tika = new Tika();
			try {
				mimeType = tika.detect(localFile);
			} catch (IOException ex) {
				logError(ex);
			}

			ByteSource payload = Files.asByteSource(localFile);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentDisposition(fileName);
			metadata.setContentLength(payload.size());
			metadata.setContentType(mimeType);

			try (InputStream is = new FileInputStream(localFile)) {
				PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, remoteFileName, is, metadata);
				CannedAccessControlList cannedAcl = s3AwsSdkOptions.getCannedAcl();
				if (cannedAcl != null) {
					putObjectRequest.withCannedAcl(cannedAcl);
				}

				PutObjectResult putObjectResult = s3Client.putObject(putObjectRequest);
				String eTag = putObjectResult.getETag();
				logger.debug("Uploaded '{}'. eTag='{}'. Job Id {}", remoteFileName, eTag, jobId);
			}
		} catch (IOException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			//https://stackoverflow.com/questions/26866739/how-do-i-close-an-aws-s3-client-connection
			if (s3Client != null) {
				s3Client.shutdown();
			}
		}
	}

	/**
	 * Copies the generated file to amazon s3, using the jclouds library
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToS3jclouds(Destination destination, String fullLocalFileName) {
		String provider = "aws-s3";
		sendFileToBlobStorage(provider, destination, fullLocalFileName);
	}

	/**
	 * Copies the generated file to a cloud blob storage provider
	 *
	 * @param provider a string representing the cloud storage provider as per
	 * the jclouds library.
	 * https://jclouds.apache.org/reference/providers/#blobstore
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToBlobStorage(String provider, Destination destination,
			String fullLocalFileName) {

		logger.debug("Entering sendFileToBlobStorage: provider='{}' destination={},"
				+ " fullLocalFileName='{}'", provider, destination, fullLocalFileName);

		//https://www.ashishpaliwal.com/blog/2012/04/playing-with-jclouds-transient-blobstore/
		//https://jclouds.apache.org/start/blobstore/
		//https://jclouds.apache.org/reference/logging/
		//https://github.com/jclouds/jclouds-examples/blob/master/rackspace/src/main/java/org/jclouds/examples/rackspace/cloudfiles/UploadLargeObject.java
		//https://github.com/jclouds/jclouds-cloud-storage-workshop/blob/master/exercise4/src/main/java/org/jclouds/labs/blobstore/exercise4/MyDropboxClient.java
		//https://stackoverflow.com/questions/14582627/what-are-the-credentials-to-use-azure-blob-in-jclouds
		//https://jclouds.apache.org/guides/azure-storage/
		//https://jclouds.apache.org/guides/aws/
		//https://jclouds.apache.org/reference/providers/#blobstore-providers
		//https://github.com/apache/camel/blob/master/components/camel-jclouds/src/main/java/org/apache/camel/component/jclouds/JcloudsBlobStoreHelper.java
		//https://jclouds.apache.org/guides/google/
		//https://help.backblaze.com/hc/en-us/articles/224991568-Where-can-I-find-my-Account-ID-and-Application-Key-
		BlobStoreContext context = null;

		try {
			String identity;
			String credential;

			DestinationType destinationType = destination.getDestinationType();
			switch (destinationType) {
				case GoogleCloudStorage:
					String templatesPath = Config.getTemplatesPath();
					String jsonKeyFileName = destination.getGoogleJsonKeyFile();
					if (StringUtils.isBlank(jsonKeyFileName)) {
						throw new IllegalArgumentException("JSON Key file not specified");
					}
					String jsonKeyFilePath = templatesPath + jsonKeyFileName;
					File jsonKeyFile = new File(jsonKeyFilePath);
					if (jsonKeyFile.exists()) {
						ObjectMapper mapper = new ObjectMapper();
						GoogleServiceAccountJsonKey jsonKey = mapper.readValue(jsonKeyFile, GoogleServiceAccountJsonKey.class);
						identity = jsonKey.getClient_email();
						credential = jsonKey.getPrivate_key();
					} else {
						throw new IllegalStateException("JSON Key file not found: " + jsonKeyFilePath);
					}
					break;
				default:
					identity = destination.getUser();
					credential = destination.getPassword();
			}

			Iterable<Module> modules = ImmutableSet.<Module>of(
					new SLF4JLoggingModule());

			context = ContextBuilder.newBuilder(provider)
					.credentials(identity, credential)
					.modules(modules)
					.buildView(BlobStoreContext.class);

			String destinationSubDirectory = destination.getSubDirectory();
			String jobSubDirectory = job.getSubDirectory();

			String directorySeparator = "/";
			String finalPath = combineDirectoryPaths(directorySeparator, destinationSubDirectory, jobSubDirectory);

			String remoteFileName = finalPath + fileName;

			File localFile = new File(fullLocalFileName);

			String mimeType = "application/unknown"; //if contentType() is not specified, uploaded file has metadata content type of application/unknown
			Tika tika = new Tika();
			try {
				mimeType = tika.detect(localFile);
			} catch (IOException ex) {
				logError(ex);
			}

			BlobStore blobStore = context.getBlobStore();

			//https://www.javatips.net/api/jclouds-master/providers/b2/src/test/java/org/jclouds/b2/blobstore/integration/B2BlobIntegrationLiveTest.java
			//https://www.backblaze.com/b2/docs/b2_upload_file.html
			ByteSource payload = Files.asByteSource(localFile);
			PayloadBlobBuilder blobBuilder = blobStore.blobBuilder(remoteFileName)
					.payload(payload)
					.contentType(mimeType)
					.contentLength(payload.size());

			if (!StringUtils.equals(provider, "b2")) {
				//Content-Disposition header is not supported or allowed by b2 storage service
				blobBuilder.contentDisposition(fileName);
			}

			Blob blob = blobBuilder.build();

			String containerName = destination.getPath();

			// Upload the Blob
			//https://stackoverflow.com/questions/49078140/jclouds-multipart-upload-to-google-cloud-storage-failing-with-400-bad-request
			//https://issues.apache.org/jira/browse/JCLOUDS-1389
			String eTag;
			if (StringUtils.equals(provider, "b2")) {
				eTag = blobStore.putBlob(containerName, blob);
			} else {
				eTag = blobStore.putBlob(containerName, blob, multipart());
			}
			logger.debug("Uploaded '{}'. eTag='{}'. Job Id {}", fileName, eTag, jobId);
		} catch (IOException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

	/**
	 * Copies the generated file to a network share (one that uses the SMB2
	 * protocol)
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName the path of the file to copy
	 */
	private void sendFileToNetworkShare(Destination destination, String fullLocalFileName) {
		logger.debug("Entering sendFileToNetworkShare: destination={}, fullLocalFileName='{}'",
				destination, fullLocalFileName);

		try {
			NetworkShareOptions networkShareOptions;
			String options = destination.getOptions();
			if (StringUtils.isBlank(options)) {
				networkShareOptions = new NetworkShareOptions();
			} else {
				networkShareOptions = ArtUtils.jsonToObject(options, NetworkShareOptions.class);
			}

			SmbConfig.Builder configBuilder = SmbConfig.builder();
			if (networkShareOptions.getTimeoutSeconds() != null) {
				configBuilder = configBuilder.withTimeout(networkShareOptions.getTimeoutSeconds(), TimeUnit.SECONDS);
			}
			if (networkShareOptions.getSocketTimeoutSeconds() != null) {
				configBuilder = configBuilder.withSoTimeout(networkShareOptions.getSocketTimeoutSeconds(), TimeUnit.SECONDS);
			}
			if (networkShareOptions.getMultiProtocolNegotiate() != null) {
				configBuilder = configBuilder.withMultiProtocolNegotiate(networkShareOptions.getMultiProtocolNegotiate());
			}
			if (networkShareOptions.getDfsEnabled() != null) {
				configBuilder = configBuilder.withDfsEnabled(networkShareOptions.getDfsEnabled());
			}
			if (networkShareOptions.getSigningRequired() != null) {
				configBuilder = configBuilder.withSigningRequired(networkShareOptions.getSigningRequired());
			}
			if (networkShareOptions.getBufferSize() != null) {
				configBuilder = configBuilder.withBufferSize(networkShareOptions.getBufferSize());
			}

			SmbConfig config = configBuilder.build();

			try (SMBClient client = new SMBClient(config)) {
				String server = destination.getServer();
				int portSetting = destination.getPort();
				int finalPort;
				if (portSetting > 0) {
					finalPort = portSetting;
				} else {
					finalPort = SMBClient.DEFAULT_PORT;
				}

				try (com.hierynomus.smbj.connection.Connection connection = client.connect(server, finalPort)) {
					String username = destination.getUser();
					if (username == null) {
						username = "";
					}

					String password = destination.getPassword();
					if (password == null) {
						password = "";
					}

					String domain = destination.getDomain();

					AuthenticationContext ac;
					if (networkShareOptions.isAnonymousUser()) {
						ac = AuthenticationContext.anonymous();
					} else if (networkShareOptions.isGuestUser()) {
						ac = AuthenticationContext.guest();
					} else {
						ac = new AuthenticationContext(username, password.toCharArray(), domain);
					}

					com.hierynomus.smbj.session.Session session = connection.authenticate(ac);

					String destinationSubDirectory = destination.getSubDirectory();
					destinationSubDirectory = StringUtils.trimToEmpty(destinationSubDirectory);

					String jobSubDirectory = job.getSubDirectory();
					jobSubDirectory = StringUtils.trimToEmpty(jobSubDirectory);

					//linux shares can use either "\" or "/" as a directory separator
					//windows shares can only use "\" as a directory separator
					String directorySeparator;
					if (StringUtils.contains(destinationSubDirectory, "/")
							|| StringUtils.contains(jobSubDirectory, "/")) {
						directorySeparator = "/";
					} else {
						directorySeparator = "\\";
					}

					String finalSubDirectory = combineDirectoryPaths(directorySeparator, destinationSubDirectory, jobSubDirectory);

					// Connect to Share
					String path = destination.getPath();
					try (DiskShare share = (DiskShare) session.connectShare(path)) {
						//https://stackoverflow.com/questions/44634892/java-smb-file-share-without-smb-1-0-cifs-compatibility-enabled
						// if file is in folder(s), create them first
						if (StringUtils.isNotBlank(finalSubDirectory)
								&& destination.isCreateDirectories()) {
							//can't create directory hierarchy in one go. throws an error. create sub-directories one at a time
							String[] folders = StringUtils.split(finalSubDirectory, directorySeparator);
							//https://stackoverflow.com/questions/4078642/create-a-folder-hierarchy-through-ftp-in-java
							List<String> subFolders = new ArrayList<>();
							for (String folder : folders) {
								subFolders.add(folder);
								String partialPath = StringUtils.join(subFolders, directorySeparator);
								try {
									if (!share.folderExists(partialPath)) {
										share.mkdir(partialPath);
									}
								} catch (SMBApiException ex) {
									logError(ex);
								}
							}
						}

						File file = new File(fullLocalFileName);
						String destPath = finalSubDirectory + fileName;
						boolean overwrite = true;
						SmbFiles.copy(file, share, destPath, overwrite);
					}
				}
			}
		} catch (IOException | SMBApiException ex) {
			logErrorAndSetDetails(ex);
		}
	}

	/**
	 * Returns the result of combining several directory paths
	 *
	 * @param directorySeparator the directory separator in use
	 * @param firstDirectoryPath the first directory path
	 * @param otherDirectoryPaths other directory paths
	 * @return the final, combined directory path
	 */
	private String combineDirectoryPaths(String directorySeparator,
			String firstDirectoryPath, String... otherDirectoryPaths) {

		logger.debug("Entering combineDirectoryPaths: directorySeparator='{}',"
				+ " firstDirectoryPath='{}'", directorySeparator, firstDirectoryPath);

		String finalPath = StringUtils.trimToEmpty(firstDirectoryPath);

		if (StringUtils.isNotBlank(finalPath)
				&& !StringUtils.endsWith(finalPath, directorySeparator)) {
			finalPath = finalPath + directorySeparator;
		}

		String subDirectoryPath = combineSubDirectoryPaths(directorySeparator, otherDirectoryPaths);

		finalPath = finalPath + subDirectoryPath;

		return finalPath;
	}

	/**
	 * Returns the result of combining several sub-directory paths
	 *
	 * @param directorySeparator the directory separator in use
	 * @param subDirectoryPaths the sub-directory paths
	 * @return the final, combined sub-directory path
	 */
	private String combineSubDirectoryPaths(String directorySeparator,
			String... subDirectoryPaths) {

		logger.debug("Entering combineSubDirectoryPaths: directorySeparator='{}'", directorySeparator);

		String finalPath = "";

		for (String directoryPath : subDirectoryPaths) {
			logger.debug("directoryPath='{}'", directoryPath);
			directoryPath = StringUtils.trimToEmpty(directoryPath);
			if (StringUtils.startsWith(directoryPath, directorySeparator)) {
				directoryPath = StringUtils.substringAfter(directoryPath, directorySeparator);
			}

			if (StringUtils.isNotBlank(directoryPath)
					&& !StringUtils.endsWith(directoryPath, directorySeparator)) {
				directoryPath = directoryPath + directorySeparator;
			}

			finalPath = finalPath + directoryPath;
		}

		return finalPath;
	}

	/**
	 * Ftps the generated file
	 */
	private void ftpFile(Destination destination, String fullLocalFileName) {
		logger.debug("Entering ftpFile: destination={}, fullLocalFileName='{}'",
				destination, fullLocalFileName);

		String path = destination.getPath();
		logger.debug("path='{}'", path);
		String jobSubDirectory = job.getSubDirectory();
		logger.debug("jobSubDirectory='{}'", jobSubDirectory);

		String directorySeparator = "/";
		String finalPath = combineDirectoryPaths(directorySeparator, path, jobSubDirectory);
		String remoteFileName = finalPath + fileName;

		DestinationType destinationType = destination.getDestinationType();
		switch (destinationType) {
			case FTP:
				doFtp(destination, fullLocalFileName, remoteFileName, finalPath);
				break;
			case SFTP:
				doSftp(destination, fullLocalFileName, remoteFileName, finalPath);
				break;
			default:
				logger.warn("Unexpected ftp destination type: {}. Job Id {}", destinationType, jobId);
		}
	}

	/**
	 * Ftp the generated file using the ftp protocol
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName full path of the local job file
	 * @param remoteFileName the full file name of the ftp destination
	 * @param path the final path (minus the file name) of the ftp file
	 */
	private void doFtp(Destination destination, String fullLocalFileName,
			String remoteFileName, String path) {
		logger.debug("Entering doFtp: destination={}, fullLocalFileName='{}',"
				+ " remoteFileName='{}', path='{}'",
				destination, fullLocalFileName, remoteFileName, path);

		//http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
		//https://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
		//https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html
		//https://stackoverflow.com/questions/36302985/how-to-connect-to-ftp-over-tls-ssl-ftps-server-in-java
		//https://stackoverflow.com/questions/36349361/apache-java-ftp-client-does-not-switch-to-binary-transfer-mode-on-some-servers
		//https://stackoverflow.com/questions/6651158/apache-commons-ftp-problems
		//https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html#completePendingCommand()
		//https://stackoverflow.com/questions/19209826/android-ftpclient-cannot-upload-file-ftp-response-421-received-server-closed
		String server = destination.getServer();
		int port = destination.getPort();

		logger.debug("server='{}'", server);
		logger.debug("port={}", port);

		if (port <= 0) {
			final int DEFAULT_FTP_PORT = 21;
			port = DEFAULT_FTP_PORT;
		}

		String user = destination.getUser();
		String password = destination.getPassword();

		logger.debug("user='{}'", user);

		FTPClient ftpClient = new FTPClient();
		try {
			FtpOptions ftpOptions;
			String options = destination.getOptions();
			if (StringUtils.isBlank(options)) {
				ftpOptions = new FtpOptions();
			} else {
				ftpOptions = ArtUtils.jsonToObject(options, FtpOptions.class);
			}

			//https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/SocketClient.html#setConnectTimeout(int)
			Integer connectTimeoutSeconds = ftpOptions.getConnectTimeoutSeconds();
			if (connectTimeoutSeconds != null) {
				int connectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(connectTimeoutSeconds);
				ftpClient.setConnectTimeout(connectTimeoutMillis);
			}

			//https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/SocketClient.html#setDefaultTimeout(int)
			Integer defaultTimeoutSeconds = ftpOptions.getDefaultTimeoutSeconds();
			if (defaultTimeoutSeconds != null) {
				int defaultTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds);
				ftpClient.setDefaultTimeout(defaultTimeoutMillis);
			}

			//https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html#setControlKeepAliveTimeout(long)
			Long controlKeepAliveTimeoutSeconds = ftpOptions.getControlKeepAliveTimeoutSeconds();
			if (controlKeepAliveTimeoutSeconds != null) {
				ftpClient.setControlKeepAliveTimeout(controlKeepAliveTimeoutSeconds);
			}

			ftpClient.connect(server, port);

			// After connection attempt, you should check the reply code to verify
			// success.
			int reply = ftpClient.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				logger.warn("FTP server refused connection. Job Id {}", jobId);
				return;
			}

			if (!ftpClient.login(user, password)) {
				logger.warn("FTP login failed. Job Id {}", jobId);
				return;
			}

			ftpClient.enterLocalPassiveMode();

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			File localFile = new File(fullLocalFileName);

			//create path if it don't exist
			if (StringUtils.isNotBlank(path) && destination.isCreateDirectories()) {
				//can't create directory hierarchy in one go
				String[] folders = StringUtils.split(path, "/");
				//https://stackoverflow.com/questions/4078642/create-a-folder-hierarchy-through-ftp-in-java
				List<String> subFolders = new ArrayList<>();
				for (String folder : folders) {
					subFolders.add(folder);
					String partialPath = StringUtils.join(subFolders, "/");
					try {
						ftpClient.makeDirectory(partialPath);
					} catch (IOException ex) {
						logError(ex);
					}
				}
			}

			boolean done;
			try (InputStream inputStream = new FileInputStream(localFile)) {
				done = ftpClient.storeFile(remoteFileName, inputStream);
			}
			if (done) {
				logger.debug("Ftp file upload successful. Job Id {}", jobId);
			} else {
				logger.warn("Ftp file upload failed. Job Id {}", jobId);
			}

			ftpClient.logout();
		} catch (IOException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				logError(ex);
			}
		}
	}

	/**
	 * Ftp the generated file using the sftp protocol
	 *
	 * @param destination the destination object
	 * @param fullLocalFileName full path of the local job file
	 * @param remoteFileName the file name or full path of the ftp destination
	 * @param path the final path (minus the file name) of the ftp file
	 */
	private void doSftp(Destination destination, String fullLocalFileName,
			String remoteFileName, String path) {
		logger.debug("Entering doSftp: destination={}, fullLocalFileName='{}',"
				+ " remoteFileName='{}', path='{}'",
				destination, fullLocalFileName, remoteFileName, path);

		//https://stackoverflow.com/questions/14830146/how-to-transfer-a-file-through-sftp-in-java
		//https://github.com/jpbriend/sftp-example/blob/master/src/main/java/com/infinit/sftp/SftpClient.java
		//https://stackoverflow.com/questions/17473398/java-sftp-upload-using-jsch-but-how-to-overwrite-the-current-file
		//https://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/ChannelSftp.html
		String server = destination.getServer();
		int port = destination.getPort();

		logger.debug("server='{}'", server);
		logger.debug("port={}", port);

		if (port <= 0) {
			final int DEFAULT_SFTP_PORT = 22;
			port = DEFAULT_SFTP_PORT;
		}

		String user = destination.getUser();
		String password = destination.getPassword();

		logger.debug("user='{}'", user);

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {
			SftpOptions sftpOptions;
			String options = destination.getOptions();
			if (StringUtils.isBlank(options)) {
				sftpOptions = new SftpOptions();
			} else {
				sftpOptions = ArtUtils.jsonToObject(options, SftpOptions.class);
			}

			JSch jsch = new JSch();
			session = jsch.getSession(user, server, port);
			session.setPassword(password);

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			//https://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Session.html#setTimeout-int-
			Integer sessionConnectTimeoutSeconds = sftpOptions.getSessionConnectTimeoutSeconds();
			if (sessionConnectTimeoutSeconds != null) {
				int sessionConnectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(sessionConnectTimeoutSeconds);
				session.setTimeout(sessionConnectTimeoutMillis);
			}

			//https://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Session.html#setServerAliveInterval-int-
			Integer serverAliveIntervalSeconds = sftpOptions.getServerAliveIntervalSeconds();
			if (serverAliveIntervalSeconds != null) {
				int serverAliveIntervalMillis = (int) TimeUnit.SECONDS.toMillis(serverAliveIntervalSeconds);
				session.setServerAliveInterval(serverAliveIntervalMillis);
			}

			session.connect();
			logger.debug("Host connected");

			channel = session.openChannel("sftp");

			//https://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Channel.html#connect(int)
			Integer channelConnectTimeoutSeconds = sftpOptions.getChannelConnectTimeoutSeconds();
			if (channelConnectTimeoutSeconds != null) {
				int channelConnectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(channelConnectTimeoutSeconds);
				channel.connect(channelConnectTimeoutMillis);
			} else {
				channel.connect();
			}
			logger.debug("Channel connected");

			channelSftp = (ChannelSftp) channel;

			File localFile = new File(fullLocalFileName);

			//create path if it don't exist
			if (StringUtils.isNotBlank(path) && destination.isCreateDirectories()) {
				//can't create directory hierarchy in one go
				String[] folders = StringUtils.split(path, "/");
				//https://stackoverflow.com/questions/4078642/create-a-folder-hierarchy-through-ftp-in-java
				List<String> subFolders = new ArrayList<>();
				for (String folder : folders) {
					subFolders.add(folder);
					String partialPath = StringUtils.join(subFolders, "/");
					try {
						channelSftp.mkdir(partialPath);
					} catch (SftpException ex) {
						logError(ex);
					}
				}
			}

			try (InputStream inputStream = new FileInputStream(localFile)) {
				channelSftp.put(inputStream, remoteFileName, ChannelSftp.OVERWRITE);
			}
		} catch (JSchException | SftpException | IOException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			if (channelSftp != null) {
				channelSftp.disconnect();
			}

			if (channel != null) {
				channel.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
	}

	/**
	 * Runs a batch file configured to be run after the job completes
	 */
	private void runBatchFile() {
		logger.debug("Entering runBatchFile");

		String batchFileName = job.getBatchFile();

		if (StringUtils.isBlank(batchFileName)) {
			return;
		}

		//restrict file name that can be used for batch file
		if (!FinalFilenameValidator.isValid(batchFileName)) {
			logger.warn("Invalid batch file name '{}'. Job id {}", batchFileName, jobId);
			return;
		}

		logger.debug("batchFileName='{}'", batchFileName);

		String batchDirectory = Config.getBatchPath();
		String fullBatchFileName = batchDirectory + batchFileName;

		File batchFile = new File(fullBatchFileName);
		if (batchFile.exists()) {
			if (SystemUtils.IS_OS_WINDOWS) {
				//https://stackoverflow.com/questions/20919001/execute-batch-file-through-java-passing-file-path-as-arguments-which-contains-s
				//https://stackoverflow.com/questions/19103570/run-batch-file-from-java-code

				List<String> cmdAndArgs = Arrays.asList("cmd", "/c", batchFileName, fileName);

				ProcessBuilder processBuilder = new ProcessBuilder(cmdAndArgs);
				processBuilder.directory(new File(batchDirectory));
				try {
					processBuilder.start();
				} catch (IOException ex) {
					logErrorAndSetDetails(ex);
				}
			} else if (SystemUtils.IS_OS_UNIX) {
				//https://stackoverflow.com/questions/25403765/when-runtime-getruntime-exec-call-linux-batch-file-could-not-find-its-physical

				// Point to the shell that will run the script
				String shell = "/bin/bash";

				// Create a ProcessBuilder object
				ProcessBuilder processBuilder = new ProcessBuilder(shell, fullBatchFileName, fileName);

				// Set the script to run in its own directory
				processBuilder.directory(new File(batchDirectory));

				try {
					// Run the script
					processBuilder.start();
				} catch (IOException ex) {
					logErrorAndSetDetails(ex);
				}
			} else {
				String os = SystemUtils.OS_NAME;
				logger.warn("Unexpected OS: '{}'. Job Id {}", os, jobId);
			}
		} else {
			logger.warn("Batch file not found: '{}'. Job Id {}", fullBatchFileName, jobId);
		}
	}

	/**
	 * Sends an email
	 *
	 * @param mailer the mailer to use
	 * @return <code>true</code> if email sent. false if email not configured
	 * @throws MessagingException
	 * @throws IOException
	 */
	private boolean sendEmail(Mailer mailer) throws MessagingException, IOException {
		logger.debug("Entering sendEmail");

		SmtpServer jobSmtpServer = job.getSmtpServer();

		boolean sendEmail = true;
		if (!Config.getCustomSettings().isEnableEmailing()) {
			sendEmail = false;
			logger.info("Emailing disabled. Job Id {}", jobId);
			runMessage = "jobs.message.emailingDisabled";
		} else if (jobSmtpServer != null) {
			if (!jobSmtpServer.isActive()) {
				sendEmail = false;
				logger.info("Job smtp server disabled. Job Id {}", jobId);
				runMessage = "jobs.message.jobSmtpServerDisabled";
			} else if (StringUtils.isBlank(jobSmtpServer.getServer())) {
				sendEmail = false;
				logger.info("Job smtp server not configured. Job Id {}", jobId);
				runMessage = "jobs.message.jobSmtpServerNotConfigured";
			}
		} else if (!Config.isEmailServerConfigured()) {
			sendEmail = false;
			logger.info("Email server not configured. Job Id {}", jobId);
			runMessage = "jobs.message.emailServerNotConfigured";
		}

		if (sendEmail) {
			mailer.send();
		}

		return sendEmail;
	}

	/**
	 * Prepares a mailer object for sending an alert job
	 *
	 * @param mailer the mailer to use
	 * @param message the message of the email
	 * @param value the alert value
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareAlertMailer(Mailer mailer, String message, int value,
			Map<String, ReportParameter> reportParamsMap) throws ParseException {

		Map<String, String> recipientDetails = null;
		prepareAlertMailer(mailer, message, value, recipientDetails, reportParamsMap);
	}

	/**
	 * Prepares a mailer object for sending an alert job
	 *
	 * @param mailer the mailer to use
	 * @param message the message of the email
	 * @param value the alert value
	 * @param the dynamic recipient details
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareAlertMailer(Mailer mailer, String message, int value,
			Map<String, String> recipientDetails, Map<String, ReportParameter> reportParamsMap)
			throws ParseException {

		logger.debug("Entering prepareAlertMailer: value={}", value);

		setMailerFromAndSubject(mailer, recipientDetails, reportParamsMap);

		ExpressionHelper expressionHelper = new ExpressionHelper();
		String username = job.getUser().getUsername();
		String customMessage = expressionHelper.processString(message, reportParamsMap, username, recipientDetails);

		String mainMessage;
		if (StringUtils.isBlank(customMessage)) {
			mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
		} else {
			mainMessage = customMessage;

			//replace value placeholder in the message if it exists
			String searchString = "#value#";
			String replaceString = String.valueOf(value);
			mainMessage = StringUtils.replaceIgnoreCase(mainMessage, searchString, replaceString);
		}

		Context ctx = new Context(locale);
		ctx.setVariable("mainMessage", mainMessage);
		ctx.setVariable("job", job);
		ctx.setVariable("value", value);

		String finalMessage = getFinalEmailMessage(ctx);
		mailer.setMessage(finalMessage);
	}

	/**
	 * Prepares a mailer object for sending an alert job based on a freemarker
	 * or thymeleaf report
	 *
	 * @param reportType the report type
	 * @param mailer the mailer to use
	 * @param value the alert value
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareTemplateAlertMailer(ReportType reportType, Mailer mailer, int value,
			Map<String, ReportParameter> reportParamsMap)
			throws TemplateException, IOException, ParseException {

		Map<String, String> recipientColumns = null;
		prepareTemplateAlertMailer(reportType, mailer, value, recipientColumns, reportParamsMap);
	}

	/**
	 * Prepares a mailer object for sending an alert job based on a freemarker
	 * or thymeleaf report
	 *
	 * @param reportType the report type
	 * @param mailer the mailer to use
	 * @param value the alert value
	 * @param recipientColumns the recipient column details
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareTemplateAlertMailer(ReportType reportType, Mailer mailer,
			int value, Map<String, String> recipientColumns,
			Map<String, ReportParameter> reportParamsMap)
			throws TemplateException, IOException, ParseException {

		logger.debug("Entering prepareTemplateAlertMailer: reportType={}, "
				+ "value={}", reportType, value);

		setMailerFromAndSubject(mailer, recipientColumns, reportParamsMap);

		//set variables to be passed to template
		Map<String, Object> data = new HashMap<>();

		if (recipientColumns != null) {
			for (Entry<String, String> entry : recipientColumns.entrySet()) {
				String columnName = entry.getKey();
				String columnValue = entry.getValue();
				data.put(columnName, columnValue);
			}
		}

		data.put("value", value);

		Report report = job.getReport();

		//create output
		Writer writer = new StringWriter();

		switch (reportType) {
			case FreeMarker:
				FreeMarkerOutput freemarkerOutput = new FreeMarkerOutput();
				freemarkerOutput.setLocale(locale);
				freemarkerOutput.generateOutput(report, writer, data);
				break;
			case Thymeleaf:
				ThymeleafOutput thymeleafOutput = new ThymeleafOutput();
				thymeleafOutput.setLocale(locale);
				thymeleafOutput.generateOutput(report, writer, data);
				break;
			case Velocity:
				VelocityOutput velocityOutput = new VelocityOutput();
				velocityOutput.setLocale(locale);
				velocityOutput.generateOutput(report, writer, data);
				break;
			default:
				break;
		}

		String finalMessage = writer.toString();
		mailer.setMessage(finalMessage);
	}

	/**
	 * Prepares a mailer object for sending an email job
	 *
	 * @param mailer the mailer to use
	 * @param message the message of the email
	 * @param outputFileName the full path of a file to include with the email
	 * @param reportParamsList the report parameters used to run the job
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareMailer(Mailer mailer, String message, String outputFileName,
			List<ReportParameter> reportParamsList, Map<String, ReportParameter> reportParamsMap)
			throws FileNotFoundException, IOException, ParseException {

		Map<String, String> recipientDetails = null;
		prepareMailer(mailer, message, outputFileName, recipientDetails, reportParamsList, reportParamsMap);
	}

	/**
	 * Prepares a mailer object for sending an email job
	 *
	 * @param mailer the mailer to use
	 * @param message the message of the email
	 * @param outputFileName the full path of a file to include with the email
	 * @param recipientDetails the dynamic recipient details
	 * @param reportParamsList the report parameters used to run the job
	 * @param reportParamsMap map containing report parameters
	 */
	private void prepareMailer(Mailer mailer, String message, String outputFileName,
			Map<String, String> recipientDetails, List<ReportParameter> reportParamsList,
			Map<String, ReportParameter> reportParamsMap)
			throws FileNotFoundException, IOException, ParseException {

		logger.debug("Entering prepareEmailMailer: outputFileName='{}'", outputFileName);

		setMailerFromAndSubject(mailer, recipientDetails, reportParamsMap);

		Report report = job.getReport();
		ReportType reportType = report.getReportType();

		String messageData = null;

		if (jobType.isEmailAttachment()) {
			// e-mail output as attachment
			List<File> attachments = new ArrayList<>();
			attachments.add(new File(outputFileName));
			mailer.setAttachments(attachments);
		} else if (jobType.isEmailInline()) {
			// inline html within email
			// read the file and include it in the HTML message
			// convert the file to a string and get only the html table
			File outputFile = new File(outputFileName);
			messageData = FileUtils.readFileToString(outputFile, "UTF-8");
			if (reportType.isStandardOutput()) {
				messageData = StringUtils.substringBetween(messageData, "<body>", "</body>");
			}
		}

		ExpressionHelper expressionHelper = new ExpressionHelper();
		String username = job.getUser().getUsername();
		String customMessage = expressionHelper.processString(message, reportParamsMap, username, recipientDetails);

		if (reportType == ReportType.FreeMarker
				|| reportType == ReportType.Velocity
				|| reportType == ReportType.Thymeleaf) {
			String finalMessage;
			if (jobType.isEmailInline()) {
				finalMessage = messageData;
			} else {
				finalMessage = customMessage;
			}

			if (finalMessage == null) {
				finalMessage = "";
			}
			mailer.setMessage(finalMessage);
		} else {
			String mainMessage;
			if (StringUtils.isBlank(customMessage)) {
				mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
			} else {
				mainMessage = customMessage;
			}

			Context ctx = new Context(locale);
			ctx.setVariable("mainMessage", mainMessage);
			ctx.setVariable("job", job);
			ctx.setVariable("data", messageData);

			//pass report parameters
			for (ReportParameter reportParam : reportParamsList) {
				String paramName = reportParam.getParameter().getName();
				ctx.setVariable(paramName, reportParam);
			}

			ctx.setVariable("params", reportParamsList);
			ctx.setVariable("locale", locale);

			String finalMessage = getFinalEmailMessage(ctx);
			mailer.setMessage(finalMessage);
		}
	}

	/**
	 * Sets the from and subject properties of a mailer object
	 *
	 * @param mailer the mailer object
	 * @param recipientDetails the dynamic recipient details
	 * @param reportParamsMap map containing report parameters
	 */
	private void setMailerFromAndSubject(Mailer mailer, Map<String, String> recipientDetails,
			Map<String, ReportParameter> reportParamsMap) throws ParseException {

		String from = getMailFrom();

		String subject = job.getMailSubject();
		if (subject == null) {
			subject = "ART: (Job " + jobId + ")";
		}

		ExpressionHelper expressionHelper = new ExpressionHelper();
		String username = job.getUser().getUsername();
		subject = expressionHelper.processString(subject, reportParamsMap, username, recipientDetails);

		mailer.setSubject(subject);
		mailer.setFrom(from);
	}

	/**
	 * Returns the email address to use in the from field
	 *
	 * @return the email address to use in the from field
	 */
	private String getMailFrom() {
		logger.debug("Entering getMailFrom");

		String from;

		String settingsFrom = Config.getSettings().getSmtpFrom();
		logger.debug("settingsFrom='{}'", settingsFrom);

		String jobSmtpServerFrom = null;
		SmtpServer jobSmtpServer = job.getSmtpServer();
		if (jobSmtpServer != null && jobSmtpServer.isActive()) {
			jobSmtpServerFrom = jobSmtpServer.getFrom();
		}
		logger.debug("jobSmtpServerFrom='{}'", jobSmtpServerFrom);

		String jobMailFrom = job.getMailFrom();
		logger.debug("jobMailFrom='{}'", jobMailFrom);

		if (StringUtils.isNotBlank(jobSmtpServerFrom)) {
			from = jobSmtpServerFrom;
		} else if (StringUtils.isNotBlank(settingsFrom)) {
			from = settingsFrom;
		} else {
			from = jobMailFrom;
		}

		logger.debug("from='{}'", from);

		if (StringUtils.isBlank(from)) {
			logger.warn("From email address not available. Job Id {}", jobId);
		}

		return from;
	}

	/**
	 * Performs database updates required before the job is run. This includes
	 * updating the last start date and next run date columns.
	 *
	 * @param nextRunDate the next run date for the job
	 */
	private void beforeExecution(Date nextRunDate) {
		logger.debug("Entering beforeExecution: nextRunDate={}", nextRunDate);

		//update last start date and next run date on art jobs table
		String sql = "UPDATE ART_JOBS SET LAST_START_DATE = ?,"
				+ " LAST_FILE_NAME='', LAST_RUN_MESSAGE='', LAST_RUN_DETAILS='',"
				+ " NEXT_RUN_DATE = ? WHERE JOB_ID = ?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			DatabaseUtils.toSqlTimestamp(nextRunDate),
			jobId
		};

		try {
			dbService.update(sql, values);
		} catch (SQLException ex) {
			logError(ex);
		}
	}

	/**
	 * Performs database updates required after the job completes. This includes
	 * updating the last end date and last run details columns.
	 */
	private void afterCompletion() {
		logger.debug("Entering afterCompletion");

		//update job details
		String sql = "UPDATE ART_JOBS"
				+ " SET LAST_END_DATE=?, LAST_FILE_NAME=?,"
				+ " LAST_RUN_MESSAGE=?, LAST_RUN_DETAILS=?"
				+ " WHERE JOB_ID=?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			fileName,
			runMessage,
			runDetails,
			jobId
		};

		try {
			dbService.update(sql, values);
		} catch (SQLException ex) {
			logError(ex);
		}
	}

	/**
	 * Updates the last run date and run details fields
	 */
	private void updateIncompleteRun() {
		logger.debug("Entering updateIncompleteRun");

		//update job details
		String sql = "UPDATE ART_JOBS SET LAST_END_DATE=?,"
				+ " LAST_RUN_DETAILS=? WHERE JOB_ID=?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			runDetails,
			jobId
		};

		try {
			dbService.update(sql, values);
		} catch (SQLException ex) {
			logError(ex);
		}
	}

	/**
	 * Runs a dynamic recipients job
	 *
	 */
	private void runDynamicRecipientsJob() {
		logger.debug("Entering runDynamicRecipientsJob");

		String tos = job.getMailTo();
		User jobUser = job.getUser();
		int recipientsReportId = job.getRecipientsReportId();
		String cc = job.getMailCc();
		String bcc = job.getMailBcc();

		ReportRunner recipientsReportRunner = null;
		ResultSet rs = null;

		try {
			recipientsReportRunner = prepareReportRunner(jobUser, recipientsReportId);

			recipientsReportRunner.execute();

			rs = recipientsReportRunner.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			if (columnCount == 1) {
				//only email column. add dynamic recipient emails to Tos and run like normal job
				ArrayList<String> emailsList = new ArrayList<>();
				while (rs.next()) {
					String emailColumn = rs.getString(1); //first column has email addresses
					if (StringUtils.length(emailColumn) > 4) {
						String[] emailArray = separateEmails(emailColumn);
						emailsList.addAll(Arrays.asList(emailArray));
					}
				}

				if (emailsList.size() > 0) {
					runNormalJob(emailsList);
				}
			} else if (columnCount > 1) {
				//personalization fields present
				//Get the column names. column indices start from 1
				ArrayList<String> columnList = new ArrayList<>();
				for (int i = 1; i < columnCount + 1; i++) {
					String columnName = rsmd.getColumnLabel(i); //use alias if available
					columnList.add(columnName);
				}

				int recordCount = 0;

				if (ArtUtils.containsIgnoreCase(columnList, ArtUtils.RECIPIENT_COLUMN)
						&& ArtUtils.containsIgnoreCase(columnList, ArtUtils.RECIPIENT_ID)) {
					//separate emails, different email message, different report data
					while (rs.next()) {
						recordCount++;

						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								String columnName = rsmd.getColumnLabel(i); //use column alias if available
								String columnValue = rs.getString(i);
								if (columnValue == null) {
									columnValue = "";
								}
								recipientColumns.put(columnName.toLowerCase(Locale.ENGLISH), columnValue); //use lowercase so that special columns are found
							}

							Map<String, Map<String, String>> recipientDetails = new HashMap<>();
							recipientDetails.put(email, recipientColumns);

							//run job for this recipient
							boolean splitJob = true;
							boolean recipientFilterPresent = true;
							runJob(splitJob, jobUser, tos, recipientDetails, recipientFilterPresent);

							int logInterval = jobOptions.getLogInterval();
							if (logInterval > 0) {
								if (recordCount % logInterval == 0) {
									progressLogger.info("Record {} - '{}'", recordCount, email);
								}
							}
						}
					}

					//run normal job in case tos, cc etc configured
					if (StringUtils.length(tos) > 4 || StringUtils.length(cc) > 4
							|| StringUtils.length(bcc) > 4) {
						runNormalJob();
					}
				} else {
					//separate emails, different email message, same report data
					Map<String, Map<String, String>> recipients = new LinkedHashMap<>();
					while (rs.next()) {
						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								String columnName = rsmd.getColumnLabel(i); //use column alias if available
								String columnValue = rs.getString(i);
								if (columnValue == null) {
									columnValue = "";
								}
								recipientColumns.put(columnName, columnValue);
							}
							recipients.put(email, recipientColumns);
						}
					}

					//run job for all recipients
					boolean splitJob = true;
					runJob(splitJob, jobUser, tos, recipients);
				}
			}
		} catch (SQLException | RuntimeException ex) {
			logErrorAndSetDetails(ex);
		} finally {
			DatabaseUtils.close(rs);

			if (recipientsReportRunner != null) {
				recipientsReportRunner.close();
			}
		}
	}

	/**
	 * Runs a normal job (not a dynamic recipients job)
	 *
	 * @param conn a connection to the art database
	 */
	private void runNormalJob() {
		logger.debug("Entering runNormalJob");

		List<String> dynamicRecipientEmails = null;
		runNormalJob(dynamicRecipientEmails);
	}

	/**
	 * Runs a normal job (not a dynamic recipients job)
	 *
	 * @param conn a connection the the art database
	 * @param dynamicRecipientEmails a list of dynamic recipient emails
	 */
	private void runNormalJob(List<String> dynamicRecipientEmails) {
		logger.debug("Entering runNormalJob");

		//run job. if job isn't shared, generate single output
		//if job is shared and doesn't use rules, generate single output to be used by all users
		//if job is shared and uses rules, generate multiple, individualized output for each shared user
		try {
			int userCount = 0; //number of shared users
			String ownerFileName = null; //for shared jobs, ensure the jobs table has the job owner's file

			boolean splitJob = false; //flag to determine if job will generate one file or multiple individualized files. to know which tables to update

			User jobUser = job.getUser();

			String emails = combineEmails(job.getMailTo(), dynamicRecipientEmails);

			if (job.isAllowSharing()) {
				if (job.isSplitJob()) {
					//generate individualized output for all shared users

					//update art_user_job_map table with users who have access through group membership. so that users newly added to a group can get their own output
					addSharedJobUsers();

					//get users to generate output for
					String usersSql = "SELECT AUJM.USERNAME, AUJM.USER_ID, AU.EMAIL"
							+ " FROM ART_USER_JOB_MAP AUJM"
							+ " INNER JOIN ART_USERS AU ON"
							+ " AUJM.USER_ID = AU.USER_ID"
							+ " WHERE AUJM.JOB_ID=? AND AU.ACTIVE=1";

					ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
					List<Map<String, Object>> records = dbService.query(usersSql, h, jobId);

					for (Map<String, Object> record : records) {
						userCount += 1;
						//map list handler uses a case insensitive map, so case of column names doesn't matter
						String username = (String) record.get("USERNAME");
						Integer userId = (Integer) record.get("USER_ID");
						String email = (String) record.get("EMAIL");

						User user = new User();
						user.setUserId(userId);
						user.setUsername(username);

						runJob(splitJob, user, email);

						//ensure that the job owner's output version is saved in the jobs table
						String jobUsername = jobUser.getUsername();
						if (jobUsername.equals(username)) {
							ownerFileName = fileName;
						}
					}

					if (userCount == 0) {
						//no shared users defined yet. generate one file for the job owner
						runJob(splitJob, jobUser, emails);
					}
				} else {
					//generate one single output to be used by all users
					runJob(splitJob, jobUser, emails);
				}
			} else {
				//job isn't shared. generate one file for the job owner
				runJob(splitJob, jobUser, emails);
			}

			//ensure jobs table always has job owner's file, or a note if no output was produced for the job owner
			if (ownerFileName != null) {
				fileName = ownerFileName;
			} else if (splitJob && userCount > 0) {
				//job is shared with other users but the owner doesn't have a copy. save note in the jobs table
				runMessage = "jobs.message.jobShared";
			}
		} catch (SQLException | RuntimeException ex) {
			logError(ex);
		}
	}

	/**
	 * Adds records to the art_user_job_map table so that the users can have access
	 * to the job
	 *
	 * @throws SQLException
	 */
	public void addSharedJobUsers() throws SQLException {
		logger.debug("Entering addSharedJobUsers");

		String sql;

		//get users who should have access to the job through group membership but don't already have it
		sql = "SELECT AU.USER_ID, AUUGM.USER_GROUP_ID"
				+ " FROM ART_USERS AU, ART_USER_USERGROUP_MAP AUUGM, ART_USER_GROUP_JOBS AUGJ"
				+ " WHERE AU.USER_ID = AUUGM.USER_ID AND AUUGM.USER_GROUP_ID = AUGJ.USER_GROUP_ID"
				+ " AND AUGJ.JOB_ID = ?"
				+ " AND NOT EXISTS"
				+ " (SELECT * FROM ART_USER_JOB_MAP AUJM"
				+ " WHERE AUJM.USER_ID = AU.USER_ID AND AUJM.JOB_ID = ?)";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> records = dbService.query(sql, h, jobId, jobId);

		sql = "INSERT INTO ART_USER_JOB_MAP (USER_ID, JOB_ID, USER_GROUP_ID) VALUES (?,?,?)";

		for (Map<String, Object> record : records) {
			//map list handler uses a case insensitive map, so case of column names doesn't matter
			Integer userId = (Integer) record.get("USER_ID");
			Integer userGroupId = (Integer) record.get("USER_GROUP_ID");

			//insert records into the art_user_job_map table so that the users can have access to the job
			Object[] values = {
				userId,
				jobId,
				userGroupId
			};

			dbService.update(sql, values);
		}
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail)
			throws SQLException {

		Map<String, Map<String, String>> recipientDetails = null;
		boolean recipientFilterPresent = false;

		runJob(splitJob, user, userEmail, recipientDetails, recipientFilterPresent);
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @param recipientDetails dynamic recipient details
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail,
			Map<String, Map<String, String>> recipientDetails)
			throws SQLException {

		boolean recipientFilterPresent = false;
		runJob(splitJob, user, userEmail, recipientDetails, recipientFilterPresent);
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @param recipientDetails dynamic recipient details
	 * @param recipientFilterPresent whether the recipient filter is present
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail,
			Map<String, Map<String, String>> recipientDetails, boolean recipientFilterPresent)
			throws SQLException {

		logger.debug("Entering runJob: splitJob={}, user={}, userEmail='{}', recipientFilterPresent={}",
				splitJob, user, userEmail, recipientFilterPresent);

		//set job start date. relevant for split jobs
		jobStartDate = new Timestamp(System.currentTimeMillis());

		ReportRunner reportRunner = null;

		fileName = ""; //reset file name

		//create job audit record if auditing is enabled
		createAuditRecord(user);

		try {
			reportRunner = prepareReportRunner(user);

			Map<String, String> recipientColumns = null;
			if (recipientFilterPresent) {
				//enable report data to be filtered/different for each recipient
				reportRunner.setRecipientFilterPresent(recipientFilterPresent);
				for (Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
					//map should only have one value if filter present
					recipientColumns = entry.getValue();
					reportRunner.setRecipientColumn(recipientColumns.get(ArtUtils.RECIPIENT_COLUMN));
					reportRunner.setRecipientId(recipientColumns.get(ArtUtils.RECIPIENT_ID));
					reportRunner.setRecipientIdType(recipientColumns.get(ArtUtils.RECIPIENT_ID_TYPE));
				}
			}

			//prepare report parameters
			Report report = job.getReport();
			int reportId = report.getReportId();

			ParameterProcessorResult paramProcessorResult = buildParameters(reportId, jobId, user);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
			reportRunner.setReportParamsMap(reportParamsMap);

			ReportType reportType = report.getReportType();

			//jobs don't show record count so generally no need for scrollable resultsets
			int resultSetType;
			if (reportType.requiresScrollableResultSet()) {
				resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
			} else {
				resultSetType = ResultSet.TYPE_FORWARD_ONLY;
			}

			//run report
			if (!reportType.isDashboard()) {
				reportRunner.execute(resultSetType);
			}

			//get email message fields
			String message = job.getMailMessage();

			String to = job.getMailTo();
			String cc = job.getMailCc();
			String bcc = job.getMailBcc();

			//trim address fields. to aid in checking if emails are configured
			to = StringUtils.trim(to);
			cc = StringUtils.trim(cc);
			bcc = StringUtils.trim(bcc);

			userEmail = StringUtils.trim(userEmail);

			//determine if emailing is required and emails are configured
			boolean generateEmail = isGenerateEmail(to, userEmail, cc, bcc);

			//set email fields to be used by the mailer
			String[] tos = null;
			String[] ccs = null;
			String[] bccs = null;

			if (generateEmail) {
				tos = separateEmails(userEmail);
				ccs = separateEmails(cc);
				bccs = separateEmails(bcc);

				logger.debug("Job Id {}. to: {}", jobId, userEmail);
				logger.debug("Job Id {}. cc: {}", jobId, cc);
				logger.debug("Job Id {}. bcc: {}", jobId, bcc);
			}

			if (jobType == JobType.Alert) {
				runAlertJob(generateEmail, recipientDetails, reportRunner, message, recipientFilterPresent, tos, ccs, bccs, reportParamsMap);
			} else if (jobType.isPublish() || jobType.isEmail() || jobType == JobType.Print) {
				//determine if the query returns records. to know if to generate output for conditional jobs
				boolean generateOutput = isGenerateOutput(user, reportParamsMap);

				//for emailing jobs, only generate output if some emails are configured
				if (jobType.isEmail()) {
					//email attachment, email inline, conditional email attachment, conditional email inline
					if (!generateEmail && recipientDetails == null) {
						generateOutput = false;
						runMessage = "jobs.message.noEmailAddressesAvailable";
					}
				}

				if (generateOutput) {
					//generate output
					String outputFileName = generateOutputFile(reportRunner, paramProcessorResult, user, recipientColumns);

					if (jobType == JobType.Print) {
						printFile(outputFileName);
					} else if (generateEmail || recipientDetails != null) {
						//some kind of emailing required
						processAndSendEmail(recipientDetails, message, outputFileName, recipientFilterPresent, generateEmail, tos, ccs, bccs, userEmail, cc, bcc, reportParamsList, reportParamsMap);
					}
				}
			} else if (jobType.isCache()) {
				runCacheJob(reportRunner);
			} else if (jobType == JobType.JustRun) {
				// This is used Used to start batch jobs at db level via calls to stored procs
				// or just to run update statements.
				// do nothing
			} else if (jobType == JobType.Burst) {
				runBurstJob(reportRunner, paramProcessorResult);
			}

			logger.debug("Job Id {} ...finished", jobId);
		} catch (Exception ex) {
			logErrorAndSetDetails(ex);
			fileName = "";
		} finally {
			if (reportRunner != null) {
				reportRunner.close();
			}
			// set audit timestamp and update archives
			afterExecution(splitJob, user);
		}
	}

	/**
	 * Finalizes the email message to send and sends it
	 *
	 * @param recipientDetails
	 * @param message
	 * @param outputFileName
	 * @param recipientFilterPresent
	 * @param generateEmail
	 * @param tos
	 * @param ccs
	 * @param bccs
	 * @param userEmail
	 * @param cc
	 * @param bcc
	 * @param reportParamsList the report parameters used to run the job
	 * @throws IOException
	 */
	private void processAndSendEmail(Map<String, Map<String, String>> recipientDetails,
			String message, String outputFileName, boolean recipientFilterPresent,
			boolean generateEmail, String[] tos, String[] ccs, String[] bccs,
			String userEmail, String cc, String bcc,
			List<ReportParameter> reportParamsList,
			Map<String, ReportParameter> reportParamsMap) throws IOException, ParseException {

		logger.debug("Entering processAndSendEmail");

		String finalMessage;
		//append link to job output for publish reminders
		String artBaseUrl = Config.getSettings().getArtBaseUrl();
		if (jobType.isPublish() && StringUtils.isNotBlank(artBaseUrl)) {
			String jobLink = artBaseUrl + "/export/jobs/" + fileName;
			String jobLinkHtml = "<p><a href='" + jobLink + "'>" + jobLink + "</a></p>";
			finalMessage = message + jobLinkHtml;
		} else {
			finalMessage = message;
		}

		//send customized emails to dynamic recipients
		if (recipientDetails != null) {
			Mailer mailer = getMailer();
			int recordCount = 0;
			for (Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
				recordCount++;

				String emails = entry.getKey();
				String[] emailsArray = separateEmails(emails);
				Map<String, String> recipientColumns = entry.getValue();

				prepareMailer(mailer, finalMessage, outputFileName, recipientColumns, reportParamsList, reportParamsMap);

				mailer.setTo(emailsArray);

				String emailCcs = recipientColumns.get(ArtUtils.EMAIL_CC);
				if (emailCcs != null) {
					String[] emailCcsArray = separateEmails(emailCcs);
					mailer.setCc(emailCcsArray);
				}

				String emailBccs = recipientColumns.get(ArtUtils.EMAIL_BCC);
				if (emailBccs != null) {
					String[] emailBccsArray = separateEmails(emailBccs);
					mailer.setBcc(emailBccsArray);
				}

				//send email for this recipient
				try {
					boolean emailSent = sendEmail(mailer);
					if (emailSent) {
						runMessage = "jobs.message.fileEmailed";
					}

					if (!recipientFilterPresent) {
						int logInterval = jobOptions.getLogInterval();
						if (logInterval > 0) {
							if (recordCount % logInterval == 0) {
								progressLogger.info("Record {} - '{}'", recordCount, emails);
							}
						}
					}
				} catch (MessagingException ex) {
					logger.debug("Error", ex);
					fileName = "";
					runMessage = "jobs.message.errorSendingSomeEmails";
					runDetails = "<b>Error: </b>"
							+ " <p>" + ex.toString() + "</p>";

					String msg = "Error when sending some emails."
							+ " \n" + ex.toString()
							+ " \n To: " + emails;
					
					logger.warn("Job Id {}. " + msg, jobId);

					if (recipientFilterPresent) {
						progressLogger.warn("'{}'. {}", emails, msg);
					} else {
						progressLogger.warn("Record {} - '{}'. {}", recordCount, emails, msg);
					}
				}
			}

			if (recipientFilterPresent) {
				//don't run normal email job after filtered email sent
				generateEmail = false;

				//delete file since email has been sent
				File f = new File(outputFileName);
				boolean deleted = f.delete();
				if (!deleted) {
					logger.warn("Email attachment file not deleted: '{}'. Job Id {}", outputFileName, jobId);
				}
			}
		}

		//send email to normal recipients
		if (generateEmail) {
			Mailer mailer = getMailer();

			prepareMailer(mailer, finalMessage, outputFileName, reportParamsList, reportParamsMap);

			//set recipients
			mailer.setTo(tos);
			mailer.setCc(ccs);
			mailer.setBcc(bccs);

			try {
				boolean emailSent = sendEmail(mailer);
				if (emailSent) {
					if (jobType.isPublish()) {
						runMessage = "jobs.message.reminderSent";
					} else {
						runMessage = "jobs.message.fileEmailed";
					}
				}

			} catch (MessagingException ex) {
				logger.debug("Error", ex);
				fileName = "";
				runMessage = "jobs.message.errorSendingSomeEmails";
				runDetails = "<b>Error: </b>"
						+ " <p>" + ex.toString() + "</p>";

				String msg = "Error when sending some emails."
						+ " \n" + ex.toString()
						+ " \n Complete address list:\n To: " + userEmail + "\n Cc: " + cc + "\n Bcc: " + bcc;
				
				logger.warn("Job Id {}. " + msg, jobId);
				progressLogger.warn(msg);
			}
		}
	}

	/**
	 * Generates report output to file
	 *
	 * @param reportRunner the report runner to use
	 * @param paramProcessorResult the parameter processor result
	 * @param user the user under whose permission the report is run
	 * @param recipientColumns dynamic recipient columns for individual dynamic
	 * recipient reports
	 * @return the full path to the output file used
	 * @throws Exception
	 */
	private String generateOutputFile(ReportRunner reportRunner,
			ParameterProcessorResult paramProcessorResult, User user,
			Map<String, String> recipientColumns) throws Exception {

		logger.debug("Entering generateOutputFile: user={}", user);

		Report report = job.getReport();
		ReportType reportType = report.getReportType();
		String outputFormat = job.getOutputFormat();
		ReportFormat reportFormat;
		if (StringUtils.isBlank(outputFormat) || StringUtils.startsWith(outputFormat, "-")) {
			//set some default report format. note that it may determine the file name extension
			//fixed width reports didn't have a report format in 3.0. Was saved in database as "--"
			reportFormat = ReportFormat.html;
		} else {
			reportFormat = ReportFormat.toEnum(outputFormat);
		}

		//generate file name to use
		String exportPath = Config.getJobsExportPath();
		String fixedFileName = job.getFixedFileName();
		logger.debug("fixedFileName='{}'", fixedFileName);

		if (StringUtils.isNotBlank(fixedFileName)) {
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			String username = job.getUser().getUsername();

			ExpressionHelper expressionHelper = new ExpressionHelper();
			fixedFileName = expressionHelper.processString(fixedFileName, reportParamsMap, username);

			fixedFileName = ArtUtils.cleanFilename(fixedFileName);

			if (job.getRunsToArchive() > 0) {
				int randomNumber = ArtUtils.getRandomNumber(100, 999);
				String baseFilename = FilenameUtils.getBaseName(fixedFileName);
				String extension = FilenameUtils.getExtension(fixedFileName);
				if (StringUtils.containsAny(extension, "aes", "gpg")) {
					//allow second extension to be used for encryped files
					String base2 = FilenameUtils.getBaseName(baseFilename);
					String extension2 = FilenameUtils.getExtension(baseFilename);
					fileName = base2 + "-" + String.valueOf(randomNumber) + "." + extension2 + "." + extension;
				} else {
					fileName = baseFilename + "-" + String.valueOf(randomNumber) + "." + extension;
				}
			} else {
				fileName = fixedFileName;
				String fullFixedFileName = exportPath + fixedFileName;
				File fixedFile = new File(fullFixedFileName);
				if (fixedFile.exists()) {
					boolean fileDeleted = fixedFile.delete();
					if (!fileDeleted) {
						logger.warn("Could not delete fixed file: '{}'. Job Id {}", fullFixedFileName, jobId);
					}
				}
			}
		} else {
			FilenameHelper filenameHelper = new FilenameHelper();
			String baseFilename = filenameHelper.getBaseFilename(job, locale);
			String extension = filenameHelper.getFilenameExtension(report, reportType, reportFormat);

			fileName = baseFilename + "." + extension;
		}

		logger.debug("fileName = '{}'", fileName);

		String outputFileName = exportPath + fileName;

		String dynamicOpenPassword = null;
		String dynamicModifyPassword = null;
		final String DYNAMIC_OPEN_PASSWORD_COLUMN_NAME = "open_password";
		final String DYNAMIC_MODIFY_PASSWORD_COLUMN_NAME = "modify_password";
		if (MapUtils.isNotEmpty(recipientColumns)) {
			dynamicOpenPassword = recipientColumns.get(DYNAMIC_OPEN_PASSWORD_COLUMN_NAME);
			dynamicModifyPassword = recipientColumns.get(DYNAMIC_MODIFY_PASSWORD_COLUMN_NAME);
		}

		if (reportType.isDashboard()) {
			PdfDashboard.generatePdf(paramProcessorResult, report, user, locale, outputFileName, messageSource, dynamicOpenPassword, dynamicModifyPassword);
		} else {
			//create html file to output to as required
			FileOutputStream fos = null;
			PrintWriter writer = null;

			if (reportFormat.isHtml() || reportFormat == ReportFormat.xml
					|| reportFormat == ReportFormat.rss20) {
				fos = new FileOutputStream(outputFileName);
				writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
			}

			//generate output
			ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();
			reportOutputGenerator.setIsJob(true);
			reportOutputGenerator.setDynamicOpenPassword(dynamicOpenPassword);
			reportOutputGenerator.setDynamicModifyPassword(dynamicModifyPassword);

			try {
				reportOutputGenerator.generateOutput(report, reportRunner,
						reportFormat, locale, paramProcessorResult, writer,
						outputFileName, user, messageSource);
			} finally {
				if (writer != null) {
					writer.close();
				}

				if (fos != null) {
					fos.close();
				}
			}

			//encrypt file if applicable
			report.encryptFile(outputFileName);
		}

		return outputFileName;
	}

	/**
	 * Generates burst output
	 *
	 * @param reportRunner the report runner to use
	 * @param paramProcessorResult the parameter processor result
	 * @throws SQLException
	 * @throws IOException
	 */
	private void runBurstJob(ReportRunner reportRunner,
			ParameterProcessorResult paramProcessorResult) throws SQLException, IOException {

		logger.debug("Entering runBurstJob");

		Report report = job.getReport();
		ReportType reportType = report.getReportType();
		ReportFormat reportFormat = ReportFormat.toEnum(job.getOutputFormat());

		if (!reportType.isTabular()) {
			logger.warn("Invalid report type for burst job: {}. Job Id {}", reportType, jobId);
			fileName = "";
			runDetails = "Invalid report type for burst job: " + reportType;
			return;
		}

		List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
		ReportOptions reportOptions = paramProcessorResult.getReportOptions();

		//generate output
		ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

		reportOutputGenerator.setIsJob(true);

		ResultSet rs = null;
		try {
			boolean isJob = true;
			StandardOutput standardOutput = reportOutputGenerator.getStandardOutputInstance(reportFormat, isJob, report);

			standardOutput.setReportParamsList(reportParamsList); //used to show selected parameters and drilldowns
			standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());
			standardOutput.setLocale(locale);

			//generate output
			rs = reportRunner.getResultSet();

			standardOutput.generateBurstOutput(rs, reportFormat, job, report, reportType);
			runMessage = "jobs.message.filesGenerated";
		} finally {
			DatabaseUtils.close(rs);
		}
	}

	/**
	 * Returns <code>true</code> if emailing is required and emails are
	 * configured
	 *
	 * @param tos to mail to setting
	 * @param userEmail the user email
	 * @param cc the mail cc setting
	 * @param bcc the mail bcc setting
	 * @return
	 */
	private boolean isGenerateEmail(String to, String userEmail, String cc, String bcc) {
		logger.debug("Entering isGenerateEmail: to='{}', userEmail='{}', cc='{}', bcc='{}'",
				to, userEmail, cc, bcc);

		boolean generateEmail = false;

		if (jobType.isPublish()) {
			//for split published jobs, tos should have a value to enable confirmation email for individual users
			if (!StringUtils.equals(to, userEmail) && (StringUtils.length(to) > 4
					|| StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)
					&& StringUtils.length(userEmail) > 4) {
				generateEmail = true;
			} else if (StringUtils.equals(to, userEmail) && (StringUtils.length(to) > 4
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

		return generateEmail;
	}

	/**
	 * Returns <code>true</code> if the job should generate some output
	 *
	 * @param user the user under whose access the job is running
	 * @param reportParamsMap the report parameters
	 * @return <code>true</code> if the job should generate some output
	 * @throws SQLException
	 */
	private boolean isGenerateOutput(User user, Map<String, ReportParameter> reportParamsMap) throws SQLException {
		logger.debug("Entering isGenerateOutput: user={}", user);

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

		return generateOutput;
	}

	/**
	 * Runs cache insert and cache append jobs
	 *
	 * @param reportRunner the report runner to use
	 * @throws Exception
	 */
	private void runCacheJob(ReportRunner reportRunner) throws SQLException, IOException {
		logger.debug("Entering runCacheJob");

		Connection cacheDatabaseConnection = null;
		ResultSet rs = null;

		try {
			int targetDatabaseId = job.getCachedDatasourceId();
			cacheDatabaseConnection = DbConnections.getConnection(targetDatabaseId);
			rs = reportRunner.getResultSet();

			CachedResult cr = new CachedResult();
			cr.setTargetConnection(cacheDatabaseConnection);
			cr.setResultSet(rs);

			String cachedTableName = job.getCachedTableName();
			if (StringUtils.isBlank(cachedTableName)) {
				String reportName = job.getReport().getLocalizedName(locale);
				cachedTableName = reportName + "_J" + jobId;
			}
			cr.setCachedTableName(cachedTableName);
			cr.setJobType(jobType);
			cr.cacheIt();

			runDetails = "Table Name (rows inserted):  <code>"
					+ cr.getCachedTableName() + "</code> (" + cr.getRowsCount() + ")"
					+ "<br />Columns Names:<br /><code>"
					+ cr.getCachedTableColumnsName() + "</code>";
		} finally {
			DatabaseUtils.close(rs, cacheDatabaseConnection);
		}
	}

	/**
	 * Runs an alert job type
	 *
	 * @param generateEmail
	 * @param recipientDetails
	 * @param reportRunner
	 * @param message
	 * @param recipientFilterPresent
	 * @param tos
	 * @param ccs
	 * @param bccs
	 * @param reportParamsMap map containing report parameters
	 * @throws IOException
	 * @throws SQLException
	 */
	private void runAlertJob(boolean generateEmail, Map<String, Map<String, String>> recipientDetails,
			ReportRunner reportRunner, String message, boolean recipientFilterPresent,
			String[] tos, String[] ccs, String[] bccs, Map<String, ReportParameter> reportParamsMap)
			throws IOException, SQLException, TemplateException, ParseException {
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

						Report report = job.getReport();
						ReportType reportType = report.getReportType();

						//send customized emails to dynamic recipients
						if (recipientDetails != null) {
							Mailer mailer = getMailer();

							for (Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
								String emails = entry.getKey();
								String[] emailsArray = separateEmails(emails);
								Map<String, String> recipientColumns = entry.getValue();

								if (reportType == ReportType.FreeMarker
										|| reportType == ReportType.Velocity
										|| reportType == ReportType.Thymeleaf) {
									prepareTemplateAlertMailer(reportType, mailer, value, recipientColumns, reportParamsMap);
								} else {
									prepareAlertMailer(mailer, message, value, recipientColumns, reportParamsMap);
								}

								mailer.setTo(emailsArray);

								String emailCcs = recipientColumns.get(ArtUtils.EMAIL_CC);
								if (emailCcs != null) {
									String[] emailCcsArray = separateEmails(emailCcs);
									mailer.setCc(emailCcsArray);
								}

								String emailBccs = recipientColumns.get(ArtUtils.EMAIL_BCC);
								if (emailBccs != null) {
									String[] emailBccsArray = separateEmails(emailBccs);
									mailer.setBcc(emailBccsArray);
								}

								//send email for this recipient
								try {
									boolean emailSent = sendEmail(mailer);
									if (emailSent) {
										runMessage = "jobs.message.alertSent";
									}
								} catch (MessagingException ex) {
									logger.debug("Error", ex);
									fileName = "";
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

							if (reportType == ReportType.FreeMarker
									|| reportType == ReportType.Velocity
									|| reportType == ReportType.Thymeleaf) {
								prepareTemplateAlertMailer(reportType, mailer, value, reportParamsMap);
							} else {
								prepareAlertMailer(mailer, message, value, reportParamsMap);
							}

							//set recipients
							mailer.setTo(tos);
							mailer.setCc(ccs);
							mailer.setBcc(bccs);

							try {
								boolean emailSent = sendEmail(mailer);
								if (emailSent) {
									runMessage = "jobs.message.alertSent";
								}
							} catch (MessagingException ex) {
								logger.debug("Error", ex);
								fileName = "";
								runMessage = "jobs.message.errorSendingAlert";
								runDetails = "<b>Error: </b> <p>" + ex.toString() + "</p>";
							}
						}
					} else {
						logger.debug("Job Id {} - No Alert. Value is {}", jobId, value);
					}
				} else {
					logger.debug("Job Id {} - Empty resultset for alert", jobId);
				}
			} finally {
				DatabaseUtils.close(rs);
			}
		} else {
			//no emails addresses to send to
			runMessage = "jobs.message.noEmailAddressesAvailable";
		}
	}

	/**
	 * Prepares a report runner, including setting the user and report. The
	 * report used will be that of the executing job
	 *
	 * @param user the user to set
	 */
	private ReportRunner prepareReportRunner(User user) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}", user);

		return prepareReportRunner(user, job.getReport());
	}

	/**
	 * Prepares a report runner, including setting the user and report
	 *
	 * @param user the user to set
	 * @param reportId the report id of the report to set
	 */
	private ReportRunner prepareReportRunner(User user, int reportId) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}, reportId={}", user, reportId);

		Report report = reportService.getReport(reportId);

		return prepareReportRunner(user, report);
	}

	/**
	 * Prepares a report runner, including setting the user and report
	 *
	 * @param user the user to set
	 * @param report the report to set
	 */
	private ReportRunner prepareReportRunner(User user, Report report) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}, report={}", user, report);

		ReportRunner reportRunner = new ReportRunner();
		reportRunner.setUser(user);
		reportRunner.setReport(report);
		reportRunner.setJob(job);

		return reportRunner;
	}

	/**
	 * Builds parameters for a job
	 *
	 * @param reportId the report id
	 * @param jId the job id
	 * @param user the user under whose permission the job is being run
	 * @return parameter processor result
	 * @throws SQLException
	 */
	private ParameterProcessorResult buildParameters(int reportId, int jId,
			User user) throws SQLException {

		logger.debug("Entering buildParameters: reportId={}, jId={}", reportId, jId);

		ParameterProcessorResult paramProcessorResult = null;

		JobParameterService jobParameterService = new JobParameterService();
		Map<String, String[]> finalValues = jobParameterService.getJobParameterValues(jId);

		try {
			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessor.setIsJob(true);
			paramProcessorResult = paramProcessor.process(finalValues, reportId, user, locale);
		} catch (ParseException | IOException ex) {
			logError(ex);
		}

		return paramProcessorResult;
	}

	/**
	 * Creates a job audit record
	 *
	 * @param user the user running the job
	 * @throws SQLException
	 */
	private void createAuditRecord(User user) throws SQLException {
		logger.debug("Entering createAuditRecord: user={}", user);

		if (job.isEnableAudit()) {
			//generate unique key for this job run
			jobAuditKey = generateKey();

			//insert job start audit values.
			String sql = "INSERT INTO ART_JOBS_AUDIT"
					+ " (JOB_ID, JOB_ACTION, JOB_AUDIT_KEY, START_DATE, USER_ID, USERNAME)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			String jobAction = "S"; //job started
			Object[] values = {
				jobId,
				jobAction,
				jobAuditKey,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				user.getUserId(),
				user.getUsername()
			};

			dbService.update(sql, values);
		}
	}

	/**
	 * Generates a key that can be used as a primary key for audit or archive
	 * records
	 *
	 * @return
	 */
	private String generateKey() {
		logger.debug("Entering generateKey");

		return ArtUtils.getUniqueId();
	}

	/**
	 * Returns a mailer object that can be used to send emails
	 *
	 * @return a mailer object that can be used to send emails
	 */
	private Mailer getMailer() {
		logger.debug("Entering getMailer");

		Mailer mailer;

		ArtHelper artHelper = new ArtHelper();

		SmtpServer jobSmtpServer = job.getSmtpServer();
		if (jobSmtpServer != null && jobSmtpServer.isActive()) {
			mailer = artHelper.getMailer(jobSmtpServer);
		} else {
			mailer = artHelper.getMailer();
		}

		mailer.setDebug(logger.isDebugEnabled());

		return mailer;
	}

	/**
	 * Updates the ART_USER_JOB_MAP table. If Audit Flag is set, a new row is added
	 * to the ART_JOBS_AUDIT table
	 */
	private void afterExecution(boolean splitJob, User user) throws SQLException {
		logger.debug("Entering afterExecution: splitJob={}, user={}", splitJob, user);

		String sql;

		String lastFileName = job.getLastFileName();

		int runsToArchive = job.getRunsToArchive();

		if (runsToArchive > 0 && lastFileName != null) {
			//update archives
			updateArchives(splitJob, user);
		} else {
			//if not archiving, delete previous file
			if (StringUtils.isNotBlank(lastFileName)
					&& !StringUtils.equals(lastFileName, fileName)) {
				String filePath = Config.getJobsExportPath() + lastFileName;
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

		//update job details
		//no need to update jobs table if non-split job. aftercompletion will do the final update to the jobs table
		if (splitJob) {
			sql = "UPDATE ART_USER_JOB_MAP SET LAST_FILE_NAME = ?,"
					+ " LAST_RUN_DETAILS=?, LAST_RUN_MESSAGE=?,"
					+ " LAST_START_DATE = ?, LAST_END_DATE = ? "
					+ " WHERE JOB_ID = ? AND USER_ID = ?";

			if (runDetails.length() > 4000) {
				runDetails = runDetails.substring(0, 4000);
			}

			Object[] values2 = {
				fileName,
				runDetails,
				runMessage,
				jobStartDate,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobId,
				user.getUserId()
			};

			dbService.update(sql, values2);
		}

		//update audit table if required
		if (job.isEnableAudit()) {
			sql = "UPDATE ART_JOBS_AUDIT SET JOB_ACTION = 'E',"
					+ " END_DATE = ? WHERE JOB_AUDIT_KEY = ? AND JOB_ID = ?";

			Object[] values3 = {
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobAuditKey,
				jobId
			};

			dbService.update(sql, values3);
		}
	}

	/**
	 * Updates the job archives table
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user of the archive record
	 */
	private void updateArchives(boolean splitJob, User user) {
		logger.debug("Entering updateArchives: splitJob={}, user={}", splitJob, user);

		try {
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
					+ " (ARCHIVE_ID,JOB_ID,USER_ID,USERNAME,ARCHIVE_FILE_NAME,"
					+ " START_DATE,END_DATE,JOB_SHARED)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			Object[] values = {
				archiveId,
				jobId,
				user.getUserId(),
				user.getUsername(),
				job.getLastFileName(),
				DatabaseUtils.toSqlTimestamp(job.getLastStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getLastEndDate()),
				jobSharedFlag
			};

			dbService.update(sql, values);

			//delete previous run's records
			List<String> oldRecords = new ArrayList<>();
			if (jobSharedFlag.equals("S")) {
				sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
						+ " FROM ART_JOB_ARCHIVES "
						+ " WHERE JOB_ID=? AND USER_ID=?"
						+ " ORDER BY START_DATE DESC";

				ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
				List<Map<String, Object>> records = dbService.query(sql, h, jobId, user.getUserId());

				int count = 0;
				for (Map<String, Object> record : records) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
						String oldArchive = (String) record.get("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
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

				ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
				List<Map<String, Object>> records = dbService.query(sql, h, jobId);

				int count = 0;
				for (Map<String, Object> record : records) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
						String oldArchive = (String) record.get("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
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
			logError(ex);
		}
	}

	/**
	 * Deletes all records from job archives table for this job
	 */
	private void deleteArchives() {
		logger.debug("Entering deleteArchives");

		try {
			String sql;

			List<String> oldRecords = new ArrayList<>();

			sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME"
					+ " FROM ART_JOB_ARCHIVES"
					+ " WHERE JOB_ID=?";

			ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
			List<Map<String, Object>> records = dbService.query(sql, h, jobId);

			for (Map<String, Object> record : records) {
				//delete archive file and database record
				String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
				String oldArchive = (String) record.get("ARCHIVE_ID");

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
			logError(ex);
		}
	}

	/**
	 * Prints a file
	 *
	 * @param outputFileName full path to the file to print
	 * @throws IOException
	 */
	private void printFile(String outputFileName) throws IOException {
		//http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/javaxprintAPIandallowsyoutolistavailableprintersqueryanamedprinterprinttextandimagefilestoaprinterandprinttopostscriptfiles.htm
		//http://www.java2s.com/Code/Java/JDK-6/Usingsystemdefaultprintertoprintafileout.htm
		//https://docs.oracle.com/javase/7/docs/api/java/awt/Desktop.html
		//https://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform
		//https://stackoverflow.com/questions/102325/not-supported-platforms-for-java-awt-desktop-getdesktop
		//http://www.javaquery.com/2013/06/understanding-basics-javaawtdesktop.html

		//use desktop class to print using the default application registered for the output file type
		//using print service class sends raw data to the printer, and most printers won't be able to recognize/handle this with some file types, and will not print successfully
		//desktop class prints to the default printer. no way to change the default printer from java code?
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.PRINT)) {
				File file = new File(outputFileName);
				desktop.print(file);
			} else {
				throw new IllegalStateException("Desktop print not supported");
			}
		} else {
			throw new IllegalStateException("Desktop not supported");
		}
	}

	/**
	 * Returns the string to use in the email message based on the appropriate
	 * template
	 *
	 * @return the string to use in the email message based on the appropriate
	 * template
	 */
	private String getFinalEmailMessage(Context ctx) {
		String finalMessage;

		String jobEmailTemplateFileName = job.getEmailTemplate();
		logger.debug("jobEmailTemplateFileName='{}'", jobEmailTemplateFileName);

		if (StringUtils.isBlank(jobEmailTemplateFileName)) {
			String templateName = "basicEmail";
			finalMessage = defaultTemplateEngine.process(templateName, ctx);
		} else {
			String jobEmailTemplateFilePath = Config.getJobTemplatesPath() + jobEmailTemplateFileName;
			File jobEmailTemplateFile = new File(jobEmailTemplateFilePath);
			if (jobEmailTemplateFile.exists()) {
				finalMessage = jobTemplateEngine.process(jobEmailTemplateFileName, ctx);
			} else {
				throw new IllegalStateException("Email template file not found: " + jobEmailTemplateFilePath);
			}
		}

		return finalMessage;
	}

	/**
	 * Separates a list of email addresses separated by , or ; and returns an
	 * array of separated email addresses
	 *
	 * @param emailString the string containing possible multiple email
	 * addresses
	 * @return an array of separated, individual email addresses
	 */
	private String[] separateEmails(String emailString) {
		//https://blogs.msdn.microsoft.com/oldnewthing/20150119-00/?p=44883
		//https://stackoverflow.com/questions/12120190/what-is-the-best-separator-to-separate-multiple-emails
		//http://forums.mozillazine.org/viewtopic.php?t=212106
		//https://support.mozilla.org/en-US/questions/1038045
		//https://www.lifewire.com/separate-multiple-email-recipients-1173274
		//https://www.lifewire.com/commas-to-separate-email-recipients-1173680
		//https://www.extendoffice.com/documents/outlook/1649-outlook-allow-comma-as-address-separator.html

		String[] emailArray;

		//allow multiple emails separated by , or ;
		if (StringUtils.contains(emailString, ",")) {
			emailArray = StringUtils.split(emailString, ",");
		} else {
			emailArray = StringUtils.split(emailString, ";");
		}

		return emailArray;
	}

	/**
	 * Combines a string containing possible multiple email addresses with a
	 * list of email addresses returning a single string with multiple email
	 * addresses separated by ,
	 *
	 * @param emailString string containing possible multiple email addresses
	 * separated using either , or ;
	 * @param emailList a list of email addresses
	 * @return string containing all email addresses separated by ,
	 */
	private String combineEmails(String emailString, List<String> emailList) {
		List<String> finalEmailList = new ArrayList<>();
		String[] emailArray = separateEmails(emailString);

		if (emailArray != null) {
			CollectionUtils.addAll(finalEmailList, emailArray);
		}

		if (CollectionUtils.isNotEmpty(emailList)) {
			finalEmailList.addAll(emailList);
		}

		String emails = StringUtils.join(finalEmailList, ",");
		return emails;
	}

}

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
package art.servlets;

import art.artdatabase.ArtDatabase;
import art.connectionpool.DbConnections;
import art.encryption.AesEncryptor;
import art.enums.ArtAuthenticationMethod;
import art.enums.ConnectionPoolLibrary;
import art.jobrunners.CleanJob;
import art.logback.LevelAndLoggerFilter;
import art.logback.OnLevelEvaluator;
import art.saiku.SaikuConnectionManager;
import art.saiku.SaikuConnectionProvider;
import art.settings.CustomSettings;
import art.settings.Settings;
import art.settings.SettingsService;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import art.utils.UpgradeHelper;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.core.db.DataSourceConnectionSource;
import com.eclecticlogic.whisper.logback.WhisperAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.FontFactory;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.logging.slf4j.SLF4JLoggerImplFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import org.saiku.service.olap.OlapDiscoverService;
import org.saiku.service.olap.ThinQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * Initializes and shuts down the application, including creating and shutting
 * down database connections and the quartz scheduler. Also provides methods to
 * retrieve application settings.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class Config extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private static String exportPath;
	private static final ArrayList<String> reportFormats = new ArrayList<>(); //report formats available to users
	private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat();
	private static String webinfPath;
	private static String artDatabaseFilePath;
	private static ArtDatabase artDbConfig;
	private static Settings settings;
	private static CustomSettings customSettings;
	private static String workDirectoryPath;
	private static String artVersion;
	private static final Map<String, String> languages = new TreeMap<>();
	private static Configuration freemarkerConfig;
	private static TemplateEngine thymeleafReportTemplateEngine;
	private static Map<Integer, SaikuConnectionProvider> saikuConnections = new HashMap<>();
	private static VelocityEngine velocityEngine;
	private static String serverTimeZoneDescription;
	private static final Map<String, String> timeZones = new LinkedHashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.info("ART is starting up");
		ConfigInit();
	}

	/**
	 * Stops the quartz scheduler, closes database connections and de-registers
	 * jdbc drivers
	 */
	@Override
	public void destroy() {
		//shutdown quartz scheduler
		SchedulerUtils.shutdownScheduler();

		closeSaikuConnections();

		//close database connections
		DbConnections.closeAllConnections();

		//prevent tomcat warning concerning mysql cleanup thread
		//https://www.ralph-schuster.eu/2014/07/09/solution-to-tomcat-cant-stop-an-abandoned-connection-cleanup-thread/
		//https://stackoverflow.com/questions/25699985/the-web-application-appears-to-have-started-a-thread-named-abandoned-connect
		//https://dev.mysql.com/doc/relnotes/connector-j/5.1/en/news-5-1-41.html
		AbandonedConnectionCleanupThread.checkedShutdown();

		//deregister jdbc drivers
		deregisterJdbcDrivers();

		logger.info("ART stopped");

		//http://logback.10977.n7.nabble.com/Shutting-down-async-appenders-when-using-logback-through-slf4j-td12505.html
		//http://logback.qos.ch/manual/configuration.html#stopContext
		//explicitly stop logback to avoid tomcat reporting that some threads were not stopped
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();
		try {
			//wait for a while to avoid tomcat reporting that quartz threads were not stopped
			//https://jira.terracotta.org/jira/browse/QTZ-122
			//http://forums.terracotta.org/forums/posts/list/3479.page
			//https://stackoverflow.com/questions/34869562/quartz-scheduler-memory-leak-in-tomcat
			//https://stackoverflow.com/questions/7586255/quartz-memory-leak
			final long QUARTZ_SHUTDOWN_DELAY_SECONDS = 1;
			TimeUnit.SECONDS.sleep(QUARTZ_SHUTDOWN_DELAY_SECONDS);
		} catch (InterruptedException ex) {
			//logger context already stopped so can't use logger.error()
			System.out.println(ex);
		}
	}

	/**
	 * De-registers jdbc drivers
	 */
	private void deregisterJdbcDrivers() {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				logger.debug("JDBC driver deregistered: {}", driver);
			} catch (SQLException ex) {
				logger.error("Error while deregistering JDBC driver: {}", driver, ex);
			}
		}
	}

	/**
	 * Initializes database connections, the quartz scheduler and application
	 * settings
	 */
	private void ConfigInit() {
		logger.debug("Entering ConfigInit");
		
		//http://nesbot.com/2011/11/28/play-2-morphia-logging-error
		//https://stackoverflow.com/questions/5115635/morphia-logging-over-log4j-in-spring
		MorphiaLoggerFactory.registerLogger(SLF4JLoggerImplFactory.class);

		ServletContext ctx = getServletConfig().getServletContext();

		//save variables in application scope for access from jsp pages
		artVersion = ctx.getInitParameter("versionNumber");
		ctx.setAttribute("artVersion", artVersion);
		setJspEnumValues(ctx);

		//set application path
		appPath = ctx.getRealPath("/");
		logger.debug("appPath='{}'", appPath);

		if (!StringUtils.endsWith(appPath, File.separator)) {
			appPath = appPath + File.separator;
		}

		//set web-inf path
		webinfPath = appPath + "WEB-INF" + File.separator;

		//load custom settings
		loadCustomSettings(ctx);

		createFreemarkerConfiguration();

		createThymeleafReportTemplateEngine();

		createVelocityEngine();

		loadLanguages();

		setTimeZoneDetails();

		//initialize datasources
		initializeArtDatabase();

		String dateDisplayPattern = settings.getDateFormat() + " " + settings.getTimeFormat();
		ctx.setAttribute("dateDisplayPattern", dateDisplayPattern); //format of dates displayed in tables
	}

	/**
	 * Sets time zone variables
	 */
	private static void setTimeZoneDetails() {
		TimeZone serverTimeZone = TimeZone.getDefault();
		serverTimeZoneDescription = getTimeZoneDescription(serverTimeZone);

		String[] timeZoneIds = TimeZone.getAvailableIDs();
		for (String timeZoneId : timeZoneIds) {
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			String timeZoneDescription = getTimeZoneDescription(timeZone);
			timeZones.put(timeZoneId, timeZoneDescription);
		}
	}

	/**
	 * Returns a descriptive string for a time zone, including its GMT offset
	 *
	 * @param timeZone the time zone
	 * @return a descriptive string for a time zone, including its GMT offset
	 */
	private static String getTimeZoneDescription(TimeZone timeZone) {
		String offset = getTimeZoneOffset(timeZone.getRawOffset());
		String description = String.format("%s (GMT%s) ", timeZone.getID(), offset);
		return description;
	}

	/**
	 * Returns the time zone offset string to use
	 *
	 * @param rawOffset the raw offset
	 * @return the time zone offset string to use
	 */
	private static String getTimeZoneOffset(int rawOffset) {
		//http://www.baeldung.com/java-time-zones
		if (rawOffset == 0) {
			return "+00:00";
		}
		long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset);
		minutes = Math.abs(minutes - TimeUnit.HOURS.toMinutes(hours));

		return String.format("%+03d:%02d", hours, Math.abs(minutes));
	}

	/**
	 * Returns the ids and descriptions of time zones available in the jvm
	 *
	 * @return the ids and descriptions of time zones available in the jvm
	 */
	public static Map<String, String> getTimeZones() {
		return timeZones;
	}

	/**
	 * Returns the server time zone description
	 *
	 * @return the server time zone description
	 */
	public static String getServerTimeZoneDescription() {
		return serverTimeZoneDescription;
	}

	/**
	 * Creates the freemarker configuration object
	 */
	private static void createFreemarkerConfiguration() {
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_26);

		try {
			freemarkerConfig.setDirectoryForTemplateLoading(new File(getTemplatesPath()));
			freemarkerConfig.setDefaultEncoding("UTF-8");
			freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			freemarkerConfig.setLogTemplateExceptions(false);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Returns the freemarker configuration object
	 *
	 * @return the freemarker configuration object
	 */
	public static Configuration getFreemarkerConfig() {
		return freemarkerConfig;
	}

	/**
	 * Creates the template engine to use for the thymeleaf report type. Uses
	 * html template mode
	 */
	private static void createThymeleafReportTemplateEngine() {
		FileTemplateResolver templateResolver = new FileTemplateResolver();
		templateResolver.setPrefix(getTemplatesPath());
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(false);

		thymeleafReportTemplateEngine = new SpringTemplateEngine();
		((SpringTemplateEngine) thymeleafReportTemplateEngine).setEnableSpringELCompiler(true);
		thymeleafReportTemplateEngine.setTemplateResolver(templateResolver);
	}

	/**
	 * Returns the template engine to use for the thymeleaf report type
	 *
	 * @return
	 */
	public static TemplateEngine getThymeleafReportTemplateEngine() {
		return thymeleafReportTemplateEngine;
	}

	/**
	 * Creates the velocity engine used by velocity reports
	 */
	private static void createVelocityEngine() {
		velocityEngine = new VelocityEngine();

		//https://stackoverflow.com/questions/22056967/apache-velocity-resource-not-found-exception-even-though-template-file-is-in-the
		//https://stackoverflow.com/questions/34662161/velocitys-fileresourceloader-cant-find-resources
		//https://velocity.apache.org/engine/1.7/developer-guide.html#resource-management
		velocityEngine.setProperty("file.resource.loader.path", "");

		velocityEngine.init();
	}

	/**
	 * Returns the velocity engine to use for velocity reports
	 *
	 * @return the velocity engine to use for velocity reports
	 */
	public static VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	/**
	 * Returns the available application languages or translations
	 *
	 * @return available application languages or translations
	 */
	public static Map<String, String> getLanguages() {
		return languages;
	}

	/**
	 * Loads available language translations
	 */
	private void loadLanguages() {
		Properties properties = new Properties();

		String propertiesFilePath = webinfPath + "i18n" + File.separator + "languages.properties";
		File propertiesFile = new File(propertiesFilePath);
		if (propertiesFile.exists()) {
			try {
				try (FileInputStream inputStream = new FileInputStream(propertiesFilePath)) {
					properties.load(inputStream);

					Set<String> propertyNames = properties.stringPropertyNames();
					for (String key : propertyNames) {
						String value = properties.getProperty(key);
						languages.put(value.trim(), key.trim());
					}
				}
			} catch (IOException ex) {
				logger.error("Error", ex);
			}
		} else {
			logger.warn("File not found: {}", propertiesFilePath);
		}
	}

	/**
	 * Sets enum values in the application context for use in jsp pages
	 *
	 * @param ctx the servlet context
	 */
	private void setJspEnumValues(ServletContext ctx) {
		ctx.setAttribute("windowsDomainAuthentication", ArtAuthenticationMethod.WindowsDomain.getValue());
		ctx.setAttribute("internalAuthentication", ArtAuthenticationMethod.Internal.getValue());
	}

	/**
	 * Registers custom fonts to be used in pdf output
	 */
	private static void registerPdfFonts() {
		//register pdf fonts. 
		//fresh registering of 661 fonts in c:\windows\fonts can take as little as 10 secs
		//re-registering already registered directory of 661 fonts takes as little as 1 sec

		if (settings == null) {
			return;
		}

		String pdfFontName = settings.getPdfFontName();
		if (StringUtils.isNotBlank(pdfFontName)) {
			//register pdf font if not already registered
			if (!FontFactory.isRegistered(pdfFontName)) {
				//font not registered. register any defined font files or directories
				String pdfFontDirectory = settings.getPdfFontDirectory();
				if (StringUtils.isNotBlank(pdfFontDirectory)) {
					logger.info("Registering fonts from directory: '{}'", pdfFontDirectory);
					int i = FontFactory.registerDirectory(pdfFontDirectory);
					logger.info("{} fonts registered", i);
				}

				String pdfFontFile = settings.getPdfFontFile();
				if (StringUtils.isNotBlank(pdfFontFile)) {
					logger.info("Registering font file: '{}'", pdfFontFile);
					FontFactory.register(pdfFontFile);
					logger.info("Font file {} registered", pdfFontFile);
				}

				//output registerd fonts
				StringBuilder sb = new StringBuilder(100);
				String newline = System.getProperty("line.separator");
				@SuppressWarnings("rawtypes")
				Set fonts = FontFactory.getRegisteredFonts();
				for (Object f : fonts) {
					sb.append(newline);
					sb.append(f);
				}
				logger.info("Registered fonts: {}", sb.toString());
			}
		}
	}

	/**
	 * Initializes the art database and report datasource connection pools, runs
	 * upgrade steps on the art database and starts the quartz scheduler
	 */
	public static void initializeArtDatabase() {
		loadArtDatabaseConfiguration();

		if (artDbConfig == null) {
			SettingsService settingsService = new SettingsService();
			settings = new Settings();
			settingsService.setSettingsDefaults(settings);
			return;
		}

		try {
			//create connection pools
			DbConnections.createConnectionPools(artDbConfig);

			//create quartz scheduler. must be done before upgrade is run
			createQuartzScheduler();

			//upgrade art database
			String templatesPath = getTemplatesPath();
			UpgradeHelper upgradeHelper = new UpgradeHelper();
			upgradeHelper.upgrade(templatesPath);
		} catch (SQLException | RuntimeException ex) {
			//include runtime exception in case of PoolInitializationException when using hikaricp
			logger.error("Error", ex);
		}

		//load settings
		//put outside try block so that a settings object is always available
		//even if there's an error in connection pool creation
		loadSettings();
	}

	/**
	 * Loads art database configuration from the art-database file
	 */
	private static void loadArtDatabaseConfiguration() {
		ArtDatabase artDatabase = null;

		try {
			File artDatabaseFile = new File(artDatabaseFilePath);
			if (artDatabaseFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				artDatabase = mapper.readValue(artDatabaseFile, ArtDatabase.class);

				//decrypt password field
				String encryptedPassword = artDatabase.getPassword();
				String decryptedPassword = AesEncryptor.decrypt(encryptedPassword);
				artDatabase.setPassword(decryptedPassword);

				artDatabase.setDatasourceId(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
				artDatabase.setName(ArtDatabase.ART_DATABASE_DATASOURCE_NAME);
			} else {
				logger.info("ART Database configuration file not found");
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		if (artDatabase != null) {
			artDbConfig = null;
			artDbConfig = artDatabase;

			//set defaults for invalid values
			setArtDatabaseDefaults(artDbConfig);
		}
	}

	/**
	 * Saves art database configuration to file
	 *
	 * @param artDatabase the art database configuration
	 * @throws java.io.IOException
	 */
	public static void saveArtDatabaseConfiguration(ArtDatabase artDatabase)
			throws IOException {

		//encrypt password field for storing
		String encryptedPassword = AesEncryptor.encrypt(artDatabase.getPassword());
		artDatabase.setPassword(encryptedPassword);

		File artDatabaseFile = new File(artDatabaseFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(artDatabaseFile, artDatabase);

		//refresh configuration and set defaults for invalid values
		loadArtDatabaseConfiguration();
	}

	/**
	 * Loads application settings
	 */
	public static void loadSettings() {
		Settings newSettings = null;

		SettingsService settingsService = new SettingsService();

		try {
			if (isArtDatabaseConfigured()) {
				newSettings = settingsService.getSettings();
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		//use default settings if error or none specified
		if (newSettings == null) {
			newSettings = new Settings();
		}

		settings = null;
		settings = newSettings;

		//set defaults for settings with invalid values
		settingsService.setSettingsDefaults(settings);

		//set date formatters
		String dateFormat = settings.getDateFormat();
		timeFormatter.applyPattern("HH:mm:ss.SSS");
		dateFormatter.applyPattern(dateFormat);
		dateTimeFormatter.applyPattern(dateFormat + " " + settings.getTimeFormat());

		//set available report formats
		String reportFormatsString = settings.getReportFormats();
		String[] tmpReportFormatsArray = StringUtils.split(reportFormatsString, ",");
		String[] reportFormatsArray = StringUtils.stripAll(tmpReportFormatsArray);
		reportFormats.clear();
		if (reportFormatsArray != null) {
			reportFormats.addAll(Arrays.asList(reportFormatsArray));
		}

		//register pdf fonts 
		registerPdfFonts();

		//register database authentication jdbc driver
		String driver = settings.getDatabaseAuthenticationDriver();
		if (StringUtils.isNotBlank(driver)) {
			try {
				Class.forName(driver).newInstance();
				logger.info("Database Authentication JDBC Driver Registered: {}", driver);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("Error while registering Database Authentication JDBC Driver: {}", driver, e);
			}
		}

		//create the db appender if configured
		createDbAppender();

		//create error notification appender if required
		createErrorNotificationAppender();
	}

	/**
	 * Creates the quartz scheduler instance for use when scheduling jobs
	 */
	private static void createQuartzScheduler() {
		if (artDbConfig == null) {
			logger.warn("ART Database configuration not available");
			return;
		}

		String quartzFilePath = webinfPath + "classes" + File.separator + "quartz.properties";
		Scheduler scheduler = SchedulerUtils.createScheduler(artDbConfig, quartzFilePath);
		createCleanJob(scheduler);
	}

	/**
	 * Creates a job on the quartz scheduler to delete old report files
	 *
	 * @param scheduler the quartz scheduler instance
	 */
	private static void createCleanJob(Scheduler scheduler) {
		try {
			if (scheduler == null) {
				logger.warn("Cannot create clean job. Scheduler not available.");
				return;
			}

			String jobName = "clean";
			String jobGroup = "clean";
			String triggerName = "clean";
			String triggerGroup = "clean";

			JobDetail quartzJob = newJob(CleanJob.class)
					.withIdentity(jobKey(jobName, jobGroup))
					.build();

			//build cron expression for the schedule
			String minute = "0/10"; //run it every 10 mins
			String hour = "*";
			String day = "*";
			String weekday = "?";
			String month = "*";
			String second = "0"; //seconds always 0

			//build cron expression.
			//cron format is sec min hr dayofmonth month dayofweek (optionally year)
			String cronString;
			cronString = second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;

			Date startDate = new Date();
			Date endDate = null; //no end

			//create trigger that defines the schedule for the job
			CronTrigger trigger = newTrigger()
					.withIdentity(triggerKey(triggerName, triggerGroup))
					.withSchedule(cronSchedule(cronString))
					.startAt(startDate)
					.endAt(endDate)
					.build();

			//delete any existing jobs or triggers with the same id before adding them to the scheduler
			scheduler.deleteJob(jobKey(jobName, jobGroup));
			scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));

			//add job and trigger to scheduler
			scheduler.scheduleJob(quartzJob, trigger);
		} catch (SchedulerException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Sets defaults for art database configuration items that have invalid
	 * values
	 *
	 * @param artDatabase the art database configuration
	 */
	public static void setArtDatabaseDefaults(ArtDatabase artDatabase) {
		if (artDatabase == null) {
			return;
		}

		if (artDatabase.getConnectionPoolTimeoutMins() <= 0) {
			artDatabase.setConnectionPoolTimeoutMins(20);
		}
		if (artDatabase.getMaxPoolConnections() <= 0) {
			artDatabase.setMaxPoolConnections(20);
		}
		if (artDatabase.getConnectionPoolLibrary() == null) {
			artDatabase.setConnectionPoolLibrary(ConnectionPoolLibrary.HikariCP);
		}

	}

	/**
	 * Loads custom settings
	 * 
	 * @param ctx the servlet context
	 */
	public static void loadCustomSettings(ServletContext ctx) {
		CustomSettings newCustomSettings = null;

		try {
			String customSettingsFilePath = webinfPath + "art-custom-settings.json";
			File customSettingsFile = new File(customSettingsFilePath);
			if (customSettingsFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				newCustomSettings = mapper.readValue(customSettingsFile, CustomSettings.class);
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		//use default settings if error or none specified
		if (newCustomSettings == null) {
			newCustomSettings = new CustomSettings();
		}

		customSettings = null;
		customSettings = newCustomSettings;

		//set show errors custom setting
		ctx.setAttribute("showErrors", customSettings.isShowErrors());

		//set work directory base path
		workDirectoryPath = webinfPath + "work" + File.separator; //default work directory

		String customWorkDirectory = customSettings.getWorkDirectory();
		if (StringUtils.isNotBlank(customWorkDirectory)) {
			//custom work directory defined
			workDirectoryPath = customWorkDirectory;
			if (!StringUtils.endsWith(workDirectoryPath, File.separator)) {
				workDirectoryPath = workDirectoryPath + File.separator;
			}

			logger.info("Using custom work directory: '{}'", workDirectoryPath);
		}

		//set export path
		exportPath = workDirectoryPath + "export" + File.separator; //default

		//set custom export path
		String customExportDirectory = customSettings.getExportDirectory();
		if (StringUtils.isNotBlank(customExportDirectory)) {
			//custom export directory defined
			exportPath = customExportDirectory;
			if (!StringUtils.endsWith(exportPath, File.separator)) {
				exportPath = exportPath + File.separator;
			}

			logger.info("Using custom export directory: '{}'", exportPath);
		}

		//set art-database file path
		artDatabaseFilePath = workDirectoryPath + "art-database.json";

		//ensure work directories exist
		createWorkDirectories();
	}

	/**
	 * Returns the current custom settings
	 *
	 * @return the current custom settings
	 */
	public static CustomSettings getCustomSettings() {
		return customSettings;
	}

	/**
	 * Creates work directories
	 */
	private static void createWorkDirectories() {
		makeDirectory(getTemplatesPath());
		makeDirectory(getArtTempPath());
		makeDirectory(getJobsExportPath());
		makeDirectory(getReportsExportPath());
		makeDirectory(getBatchPath());
		makeDirectory(getJobLogsPath());
		makeDirectory(getRecordsExportPath());
	}

	/**
	 * Creates a directory given a full path
	 *
	 * @param directoryPath the directory path to create
	 */
	private static void makeDirectory(String directoryPath) {
		if (directoryPath == null) {
			return;
		}

		File directory = new File(directoryPath);

		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (!created) {
				logger.warn("Directory not created: {}", directory);
			}
		}

	}

	/**
	 * Returns <code>true</code> if art database has been configured
	 *
	 * @return <code>true</code> if art database has been configured
	 */
	public static boolean isArtDatabaseConfigured() {
		if (artDbConfig == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the full path to the art-database file
	 *
	 * @return full path to the art-database file
	 */
	public static String getArtDatabaseFilePath() {
		return artDatabaseFilePath;
	}

	/**
	 * Returns the full path to the web-inf directory.
	 *
	 * @return full path to the web-inf directory
	 */
	public static String getWebinfPath() {
		return webinfPath;
	}

	/**
	 * Returns the full path to the hsqldb directory.
	 *
	 * @return full path to the hsqldb directory
	 */
	public static String getHsqldbPath() {
		return webinfPath + "hsqldb" + File.separator;
	}

	/**
	 * Returns the full path to the classes directory.
	 *
	 * @return full path to the classes directory
	 */
	public static String getClassesPath() {
		return webinfPath + "classes" + File.separator;
	}

	/**
	 * Returns the full path to the export directory.
	 *
	 * @return full path to the export directory
	 */
	public static String getExportPath() {
		return exportPath;
	}

	/**
	 * Returns the full path to the jobs export directory
	 *
	 * @return full path to the jobs export directory
	 */
	public static String getJobsExportPath() {
		return exportPath + "jobs" + File.separator;
	}

	/**
	 * Returns the full path to the job logs directory
	 *
	 * @return full path to the job logs directory
	 */
	public static String getJobLogsPath() {
		return exportPath + "jobLogs" + File.separator;
	}

	/**
	 * Returns the full path to the reports export directory
	 *
	 * @return full path to the reports export directory
	 */
	public static String getReportsExportPath() {
		return exportPath + "reports" + File.separator;
	}

	/**
	 * Returns the full path to the records export directory
	 *
	 * @return full path to the records export directory
	 */
	public static String getRecordsExportPath() {
		return exportPath + "records" + File.separator;
	}

	/**
	 * Returns the full path to the batch directory
	 *
	 * @return full path to the batch directory
	 */
	public static String getBatchPath() {
		return workDirectoryPath + "batch" + File.separator;
	}

	/**
	 * Returns the full path to the WEB-INF\tmp directory
	 *
	 * @return full path to the templates directory
	 */
	public static String getArtTempPath() {
		return workDirectoryPath + "tmp" + File.separator;
	}

	/**
	 * Returns the full path to the thymeleaf templates directory
	 *
	 * @return full path to the thymeleaf templates directory
	 */
	public static String getThymeleafTemplatesPath() {
		return webinfPath + "thymeleaf" + File.separator;
	}

	/**
	 * Returns the full path to the templates directory
	 *
	 * @return full path to the templates directory
	 */
	public static String getTemplatesPath() {
		return workDirectoryPath + "templates" + File.separator;
	}

	/**
	 * Returns the full path to the default templates directory
	 *
	 * @return full path to the default templates directory
	 */
	public static String getDefaultTemplatesPath() {
		return webinfPath + "work" + File.separator + "templates" + File.separator;
	}

	/**
	 * Returns the relative path to the default templates directory
	 *
	 * @return relative path to the default templates directory
	 */
	public static String getRelativeDefaultTemplatesPath() {
		return "/WEB-INF/work/templates/";
	}

	/**
	 * Returns the full application path
	 *
	 * @return the full application path
	 */
	public static String getAppPath() {
		return appPath;
	}

	/**
	 * Returns the full path to the js-templates directory
	 *
	 * @return full path to the js-templates directory
	 */
	public static String getJsTemplatesPath() {
		return appPath + "js-templates" + File.separator;
	}

	/**
	 * Returns the art version
	 *
	 * @return the art version
	 */
	public static String getArtVersion() {
		return artVersion;
	}

	/**
	 * Returns <code>true</code> if a custom font should be used in pdf output
	 *
	 * @return <code>true</code> if a custom font should be used in pdf output
	 */
	public static boolean isUseCustomPdfFont() {
		if (StringUtils.isBlank(settings.getPdfFontName())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns <code>true</code> if the email server has been configured
	 *
	 * @return <code>true</code> if the email server has been configured
	 */
	public static boolean isEmailServerConfigured() {
		if (StringUtils.isBlank(settings.getSmtpServer())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the user selectable report formats
	 *
	 * @return user selectable report formats
	 */
	public static List<String> getReportFormats() {
		return reportFormats;
	}

	/**
	 * Returns the max rows for the given report format
	 *
	 * @param reportFormatString the report format
	 * @return the max rows for the given report format
	 */
	public static int getMaxRows(String reportFormatString) {
		int max = settings.getMaxRowsDefault();

		String setting = settings.getMaxRowsSpecific();
		String[] maxRows = StringUtils.split(setting, ",");
		if (maxRows != null) {
			for (String maxSetting : maxRows) {
				if (StringUtils.containsIgnoreCase(maxSetting, reportFormatString)) {
					String value = StringUtils.substringAfter(maxSetting, ":");
					max = Integer.parseInt(value);
					break;
				}
			}
		}

		return max;
	}

	/**
	 * Returns the string to be displayed in report output for a date field
	 *
	 * @param date the date value
	 * @return the string value to be displayed
	 */
	public static String getDateDisplayString(Date date) {
		String dateString;

		if (date == null) {
			dateString = "";
		} else if (timeFormatter.format(date).equals("00:00:00.000")) {
			//time portion is 0. display date only
			dateString = dateFormatter.format(date);
		} else {
			//display date and time
			dateString = dateTimeFormatter.format(date);
		}
		return dateString;
	}

	/**
	 * Returns the string to be displayed in report output for a date field
	 *
	 * @param date the date value
	 * @return the string value in iso format
	 */
	public static String getIsoDateDisplayString(Date date) {
		String dateString;

		if (date == null) {
			dateString = "";
		} else if (timeFormatter.format(date).equals("00:00:00.000")) {
			//time portion is 0. display date only
			dateString = ArtUtils.isoDateFormatter.format(date);
		} else {
			//display date and time
			dateString = ArtUtils.isoDateTimeFormatter.format(date);
		}
		return dateString;
	}

	/**
	 * Returns the art database configuration settings
	 *
	 * @return art database configuration or null if art database is not
	 * configured
	 */
	public static ArtDatabase getArtDbConfig() {
		return artDbConfig;
	}

	/**
	 * Returns application settings
	 *
	 * @return application settings
	 */
	public static Settings getSettings() {
		return settings;
	}

	/**
	 * Sets the settings variable
	 *
	 * @param newSettings the new settings
	 */
	public static void setSettings(Settings newSettings) {
		settings = null;
		settings = newSettings;
	}

	/**
	 * Returns the saiku connections
	 *
	 * @return the saiku connections
	 */
	public static Map<Integer, SaikuConnectionProvider> getSaikuConnections() {
		return saikuConnections;
	}

	/**
	 * Closes all saiku connections
	 */
	private static void closeSaikuConnections() {
		for (Entry<Integer, SaikuConnectionProvider> entry : saikuConnections.entrySet()) {
			SaikuConnectionProvider connectionProvider = entry.getValue();
			SaikuConnectionManager connectionManager = connectionProvider.getConnectionManager();
			connectionManager.destroy();
		}
	}

	/**
	 * Close saiku connections for a given user
	 *
	 * @param userId the id of the user
	 */
	public static void closeSaikuConnections(int userId) {
		SaikuConnectionProvider connectionProvider = saikuConnections.get(userId);
		if (connectionProvider != null) {
			SaikuConnectionManager connectionManager = connectionProvider.getConnectionManager();
			connectionManager.destroy();
			connectionProvider.setConnectionManager(null);
			connectionProvider.setMetaExplorer(null);
			connectionProvider.setDiscoverService(null);
			ThinQueryService thinQueryService = connectionProvider.getThinQueryService();
			thinQueryService.destroy();
			thinQueryService = null;
			connectionProvider = null;
			saikuConnections.remove(userId);
		}
	}

	/**
	 * Refreshes all saiku connections - clears the mondrian cache they use in
	 * the process
	 */
	public static void refreshSaikuConnections() {
		for (SaikuConnectionProvider connectionProvider : saikuConnections.values()) {
			SaikuConnectionManager connectionManager = connectionProvider.getConnectionManager();
			connectionManager.refreshAllConnections();
		}
	}

	/**
	 * Returns the olap discover service for a given user
	 *
	 * @param userId the id of the given user
	 * @return the olap discover service for a given user
	 */
	public static OlapDiscoverService getOlapDiscoverService(int userId) {
		SaikuConnectionProvider connectionProvider = saikuConnections.get(userId);
		return connectionProvider.getDiscoverService();
	}

	/**
	 * Returns the saiku connection manager for a given user
	 *
	 * @param userId the id of the given user
	 * @return the saiku connection manager for a given user
	 */
	public static SaikuConnectionManager getSaikuConnectionManager(int userId) {
		SaikuConnectionProvider connectionProvider = saikuConnections.get(userId);
		return connectionProvider.getConnectionManager();
	}

	/**
	 * Returns the thin query service for a given user
	 *
	 * @param userId the id of the given user
	 * @return the thin query service for a given user
	 */
	public static ThinQueryService getThinQueryService(int userId) {
		SaikuConnectionProvider connectionProvider = saikuConnections.get(userId);
		return connectionProvider.getThinQueryService();
	}

	/**
	 * Creates a db appender if configured in application settings
	 */
	private static void createDbAppender() {
		logger.debug("Entering createDbAppender");

		//https://stackoverflow.com/questions/43536302/set-sqldialect-to-logback-db-appender-programaticaly
		//https://stackoverflow.com/questions/22000995/configuring-logback-dbappender-programmatically
		//https://logback.qos.ch/apidocs/ch/qos/logback/classic/db/DBAppender.html
		//https://logback.qos.ch/manual/appenders.html
		//https://stackoverflow.com/questions/40460684/how-to-disable-logback-output-to-console-programmatically-but-append-to-file
		//https://learningviacode.blogspot.co.ke/2014/01/writing-logs-to-database.html
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		final String DB_APPENDER_NAME = "db";
		DBAppender dbAppender = (DBAppender) rootLogger.getAppender(DB_APPENDER_NAME);
		if (dbAppender != null) {
			dbAppender.stop();
			rootLogger.detachAppender(dbAppender);
		}

		int logsDatasourceId = settings.getLogsDatasourceId();
		logger.debug("logsDatasourceId={}", logsDatasourceId);

		if (logsDatasourceId == 0) {
			return;
		}

		DataSourceConnectionSource source = new DataSourceConnectionSource();
		source.setDataSource(DbConnections.getDataSource(logsDatasourceId));
		source.setContext(loggerContext);
		source.start();

		dbAppender = new DBAppender();
		dbAppender.setName(DB_APPENDER_NAME);
		dbAppender.setConnectionSource(source);
		dbAppender.setContext(loggerContext);
		dbAppender.start();

		rootLogger.addAppender(dbAppender);
	}

	private static void createErrorNotificationAppender() {
		//http://www.eclecticlogic.com/whisper/
		//http://www.eclecticlogic.com/2014/08/25/introducing-whisper/
		//http://mailman.qos.ch/pipermail/logback-user/2014-January/004285.html
		//https://stackoverflow.com/questions/47315788/logback-not-sending-email-for-level-less-than-error?rq=1
		//https://stackoverflow.com/questions/24739509/logback-fire-mail-for-warnings?rq=1
		//https://stackoverflow.com/questions/11527702/logback-programatically-added-smtpappender-leaves-message-body-blank
		//https://stackoverflow.com/questions/1993038/logback-smtpappender-limiting-rate/3985862
		//https://logback.qos.ch/xref/ch/qos/logback/classic/net/SMTPAppender.html
		//https://logback.qos.ch/xref/ch/qos/logback/classic/boolex/OnErrorEvaluator.html
		//https://logback.qos.ch/manual/filters.html#DuplicateMessageFilter
		//https://logback.qos.ch/manual/filters.html
		//https://logback.qos.ch/manual/filters.html#GEventEvaluator
		//https://stackoverflow.com/questions/13179773/logback-thresholdfilter-how-todo-the-opposite
		//https://dzone.com/articles/limiting-repetitive-log-messages-with-logback

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		final String WHISPER_APPENDER_NAME = "whisper";
		WhisperAppender tempWhisperAppender = (WhisperAppender) rootLogger.getAppender(WHISPER_APPENDER_NAME);
		if (tempWhisperAppender != null) {
			tempWhisperAppender.stop();
			rootLogger.detachAppender(tempWhisperAppender);
		}

		final String DIGEST_LOGGER_NAME = "digest.appender.logger";
		ch.qos.logback.classic.Logger digestLogger = loggerContext.getLogger(DIGEST_LOGGER_NAME);
		digestLogger.detachAndStopAllAppenders();
		digestLogger.setLevel(Level.OFF);
		digestLogger.setAdditive(false);

		String errorNotificationTo = settings.getErrorNotificationTo();
		if (StringUtils.isBlank(errorNotificationTo)) {
			return;
		}

		if (!customSettings.isEnableEmailing()) {
			logger.info("Emailing disabled");
			return;
		}

		if (!isEmailServerConfigured()) {
			logger.info("Email server not configured");
			return;
		}

		String levelString = settings.getErrorNotificatonLevel().getValue();
		Level level = Level.toLevel(levelString);

		String loggers = settings.getErrorNotificationLogger();

		LevelAndLoggerFilter filter = new LevelAndLoggerFilter(level, loggers);
		filter.setName("filter");
		filter.setContext(loggerContext);
		filter.start();

		//https://stackoverflow.com/questions/47315788/logback-not-sending-email-for-level-less-than-error
		//https://stackoverflow.com/questions/24739509/logback-fire-mail-for-warnings
		//https://stackoverflow.com/questions/16827033/why-is-logback-smtpappender-only-sending-1-email
		//https://amitstechblog.wordpress.com/2011/11/02/email-alerts-with-logback/
		OnLevelEvaluator evaluator = new OnLevelEvaluator(level);

		//https://logback.qos.ch/manual/layouts.html#ClassicHTMLLayout
		HTMLLayout layout = new HTMLLayout();
		layout.setContext(loggerContext);
		layout.start();

		String errorNotificationFrom = settings.getErrorNotificationFrom();
		String smtpHost = settings.getSmtpServer();
		int port = settings.getSmtpPort();
		boolean userStartTls = settings.isSmtpUseStartTls();
		boolean useSmtpAuthentication = settings.isUseSmtpAuthentication();
		String username = settings.getSmtpUsername();
		String password = settings.getSmtpPassword();
		String subject = settings.getErrorNotificationSubjectPattern();

		//https://logback.qos.ch/manual/appenders.html#SMTPAppender
		SMTPAppender errorEmailAppender = new SMTPAppender();
		errorEmailAppender.setName("errorEmail");
		errorEmailAppender.setContext(loggerContext);
		errorEmailAppender.addFilter(filter);
		errorEmailAppender.setSmtpHost(smtpHost);
		errorEmailAppender.setSmtpPort(port);
		errorEmailAppender.setSTARTTLS(userStartTls);
		if (useSmtpAuthentication) {
			errorEmailAppender.setUsername(username);
			errorEmailAppender.setPassword(password);
		}
		//context must be set before calling addTo() otherwise you get a null pointer exception
		errorEmailAppender.addTo(errorNotificationTo);
		errorEmailAppender.setFrom(errorNotificationFrom);
		errorEmailAppender.setSubject(subject);
		errorEmailAppender.setLayout(layout);
		errorEmailAppender.setEvaluator(evaluator);
		errorEmailAppender.start();

		subject = "%X{whisper.digest.subject}";
		SMTPAppender errorDigestAppender = new SMTPAppender();
		errorDigestAppender.setName("errorDigest");
		errorDigestAppender.setContext(loggerContext);
		errorDigestAppender.setSmtpHost(smtpHost);
		errorDigestAppender.setSmtpPort(port);
		errorDigestAppender.setSTARTTLS(userStartTls);
		if (useSmtpAuthentication) {
			errorDigestAppender.setUsername(username);
			errorDigestAppender.setPassword(password);
		}
		errorDigestAppender.addTo(errorNotificationTo);
		errorDigestAppender.setFrom(errorNotificationFrom);
		errorDigestAppender.setSubject(subject);
		errorDigestAppender.setLayout(layout);
		errorDigestAppender.setEvaluator(evaluator);
		errorDigestAppender.start();

		String suppressAfter = settings.getErrorNotificationSuppressAfter();
		String expireAfter = settings.getErrorNotificationExpireAfter();
		String digestFrequency = settings.getErrorNotificationDigestFrequency();

		WhisperAppender whisperAppender = new WhisperAppender();
		whisperAppender.setName(WHISPER_APPENDER_NAME);
		whisperAppender.setContext(loggerContext);
		whisperAppender.addFilter(filter);
		whisperAppender.setDigestLoggerName(DIGEST_LOGGER_NAME);
		whisperAppender.setSuppressAfter(suppressAfter);
		whisperAppender.setExpireAfter(expireAfter);
		whisperAppender.setDigestFrequency(digestFrequency);
		whisperAppender.addAppender(errorEmailAppender);
		whisperAppender.start();

		//note that digest logger messages will always appear as error
		//https://github.com/eclecticlogic/whisper/blob/master/src/main/java/com/eclecticlogic/whisper/logback/WhisperAppender.java
		digestLogger.setLevel(level);
		digestLogger.setAdditive(false);
		digestLogger.addAppender(errorDigestAppender);

		rootLogger.addAppender(whisperAppender);
	}
}

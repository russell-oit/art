/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.servlets;

import art.artdatabase.ArtDatabase;
import art.connectionpool.DbConnections;
import art.encryption.AesEncryptor;
import art.enums.ArtAuthenticationMethod;
import art.enums.ConnectionPoolLibrary;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.PdfPageSize;
import art.jobrunners.CleanJob;
import art.settings.CustomSettings;
import art.settings.Settings;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import art.utils.UpgradeHelper;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.FontFactory;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
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
	private static String settingsFilePath;
	private static Settings settings;
	private static CustomSettings customSettings;
	private static String workDirectoryPath;
	private static String artVersion;
	private static final Map<String, String> languages = new TreeMap<>();
	private static Configuration freemarkerConfig;
	private static TemplateEngine thymeleafReportTemplateEngine;

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
		logger.debug("Entering destroy");

		//shutdown quartz scheduler
		SchedulerUtils.shutdownScheduler();

		//close database connections
		DbConnections.closeAllConnections();

		//deregister jdbc drivers
		deregisterJdbcDrivers();

		logger.info("ART stopped");

		//http://logback.10977.n7.nabble.com/Shutting-down-async-appenders-when-using-logback-through-slf4j-td12505.html
		//http://logback.qos.ch/manual/configuration.html#stopContext
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();
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

		ServletContext ctx = getServletConfig().getServletContext();

		//save variables in application scope for access from jsp pages
		artVersion = ctx.getInitParameter("versionNumber");
		ctx.setAttribute("artVersion", artVersion);
		setJspEnumValues(ctx);

		//set application path
		appPath = ctx.getRealPath("/");

		if (!StringUtils.right(appPath, 1).equals(File.separator)) {
			appPath = appPath + File.separator;
		}

		//set web-inf path
		webinfPath = appPath + "WEB-INF" + File.separator;

		//load custom settings
		loadCustomSettings();

		//set show errors custom setting
		ctx.setAttribute("showErrors", customSettings.isShowErrors());

		//set work directory base path
		workDirectoryPath = webinfPath + "work" + File.separator; //default work directory

		String customWorkDirectory = customSettings.getWorkDirectory();
		if (StringUtils.isNotBlank(customWorkDirectory)) {
			//custom work directory defined
			workDirectoryPath = customWorkDirectory;
			if (!StringUtils.right(workDirectoryPath, 1).equals(File.separator)) {
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
			if (!StringUtils.right(exportPath, 1).equals(File.separator)) {
				exportPath = exportPath + File.separator;
			}

			logger.info("Using custom export directory: '{}'", exportPath);
		}

		//set art-database file path
		artDatabaseFilePath = workDirectoryPath + "art-database.json";

		//set settings file path
		settingsFilePath = workDirectoryPath + "art-settings.json";

		//ensure work directories exist
		createWorkDirectories();

		createFreemarkerConfiguration();

		createThymeleafReportTemplateEngine();

		loadLanguages();

		//load settings and initialize variables
		loadSettings();

		String dateDisplayPattern = settings.getDateFormat() + " " + settings.getTimeFormat();
		ctx.setAttribute("dateDisplayPattern", dateDisplayPattern); //format of dates displayed in tables

		//initialize datasources
		initializeArtDatabase();
	}

	/**
	 * Creates the freemarker configuration object
	 */
	private static void createFreemarkerConfiguration() {
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_25);

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
		thymeleafReportTemplateEngine = new TemplateEngine();
		FileTemplateResolver templateResolver = new FileTemplateResolver();
		templateResolver.setPrefix(getTemplatesPath());
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(false);
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
			return;
		}

		try {
			//create connection pools
			DbConnections.createConnectionPools(artDbConfig);

			//create quartz scheduler. must be done before upgrade is run
			createQuartzScheduler();

			//upgrade art database
			String upgradeFilePath = getArtTempPath() + "upgrade.txt";
			String templatesPath = getTemplatesPath();
			UpgradeHelper upgradeHelper = new UpgradeHelper();
			upgradeHelper.upgrade(artVersion, upgradeFilePath, templatesPath);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
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
	private static void loadSettings() {
		Settings newSettings = null;

		try {
			File settingsFile = new File(settingsFilePath);
			if (settingsFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				newSettings = mapper.readValue(settingsFile, Settings.class);
				//decrypt password fields
				newSettings.setSmtpPassword(AesEncryptor.decrypt(newSettings.getSmtpPassword()));
				newSettings.setLdapBindPassword(AesEncryptor.decrypt(newSettings.getLdapBindPassword()));
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		//use default settings if error or none specified
		if (newSettings == null) {
			newSettings = new Settings();
		}

		settings = null;
		settings = newSettings;

		//set defaults for settings with invalid values
		setSettingsDefaults(settings);

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

	}

	/**
	 * Saves application settings to file
	 *
	 * @param newSettings the settings to save
	 * @throws IOException
	 */
	public static void saveSettings(Settings newSettings) throws IOException {
		//encrypt password fields
		String clearTextSmtpPassword = newSettings.getSmtpPassword();
		String clearTextLdapBindPassword = newSettings.getLdapBindPassword();

		String encryptedSmtpPassword = AesEncryptor.encrypt(clearTextSmtpPassword);
		String encryptedLdapBindPassword = AesEncryptor.encrypt(clearTextLdapBindPassword);

		newSettings.setSmtpPassword(encryptedSmtpPassword);
		newSettings.setLdapBindPassword(encryptedLdapBindPassword);

		File settingsFile = new File(settingsFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, newSettings);

		//refresh settings and related configuration, and set defaults for invalid values
		loadSettings();
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
	 * Sets defaults for application settings that have invalid values
	 */
	private static void setSettingsDefaults(Settings pSettings) {
		if (pSettings == null) {
			return;
		}

		if (pSettings.getSmtpPort() <= 0) {
			pSettings.setSmtpPort(25);
		}
		if (pSettings.getMaxRowsDefault() <= 0) {
			pSettings.setMaxRowsDefault(10000);
		}
		if (pSettings.getLdapPort() <= 0) {
			pSettings.setLdapPort(389);
		}
		if (StringUtils.isBlank(pSettings.getLdapUserIdAttribute())) {
			pSettings.setLdapUserIdAttribute("uid");
		}
		if (StringUtils.isBlank(pSettings.getDateFormat())) {
			pSettings.setDateFormat("dd-MMM-yyyy");
		}
		if (StringUtils.isBlank(pSettings.getTimeFormat())) {
			pSettings.setTimeFormat("HH:mm:ss");
		}
		if (StringUtils.isBlank(pSettings.getReportFormats())) {
			pSettings.setReportFormats("htmlDataTable,htmlGrid,xlsx,pdf,docx,htmlPlain");
		}
		if (pSettings.getMaxRunningReports() <= 0) {
			pSettings.setMaxRunningReports(1000);
		}
		if (pSettings.getArtAuthenticationMethod() == null) {
			pSettings.setArtAuthenticationMethod(ArtAuthenticationMethod.Internal);
		}
		if (pSettings.getLdapConnectionEncryptionMethod() == null) {
			pSettings.setLdapConnectionEncryptionMethod(LdapConnectionEncryptionMethod.None);
		}
		if (pSettings.getLdapAuthenticationMethod() == null) {
			pSettings.setLdapAuthenticationMethod(LdapAuthenticationMethod.Simple);
		}
		if (pSettings.getPdfPageSize() == null) {
			pSettings.setPdfPageSize(PdfPageSize.A4Landscape);
		}
		if (pSettings.getDisplayNull() == null) {
			pSettings.setDisplayNull(DisplayNull.NoNumbersAsZero);
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
			artDatabase.setConnectionPoolLibrary(ConnectionPoolLibrary.ArtDBCP);
		}

	}

	/**
	 * Loads custom settings
	 */
	private static void loadCustomSettings() {
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
	private void createWorkDirectories() {
		makeDirectory(getTemplatesPath());
		makeDirectory(getArtTempPath());
		makeDirectory(getJobsExportPath());
		makeDirectory(getReportsExportPath());
		makeDirectory(getBatchPath());
	}

	/**
	 * Creates a directory given a full path
	 *
	 * @param directoryPath the directory path to create
	 */
	private void makeDirectory(String directoryPath) {
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
	 * Returns the full path to the reports export directory
	 *
	 * @return full path to the reports export directory
	 */
	public static String getReportsExportPath() {
		return exportPath + "reports" + File.separator;
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
	 * Returns the full path to the templates directory
	 *
	 * @return full path to the templates directory
	 */
	public static String getTemplatesPath() {
		return workDirectoryPath + "templates" + File.separator;
	}

	/**
	 * Returns the relative path to the templates directory
	 *
	 * @return relative path to the templates directory
	 */
	public static String getRelativeTemplatesPath() {
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
}

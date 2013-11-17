package art.artdatabase;

import art.servlets.ArtConfig;
import art.utils.Encrypter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utility methods related to art database configuration
 *
 * @author Timothy Anyona
 */
public class ArtDatabaseUtils {

	final static Logger logger = LoggerFactory.getLogger(ArtDatabaseUtils.class);
	final static String DRIVER_PROPERTY = "driver";
	final static String URL_PROPERTY = "url";
	final static String USERNAME_PROPERTY = "username";
	final static String PASSWORD_PROPERTY = "password";
	final static String CONNECTION_TEST_SQL_PROPERTY = "connectionTestSql";
	final static String CONNECTION_POOL_TIMEOUT_PROPERTY = "connectionPoolTimeout";
	final static String MAX_POOL_CONNECTIONS_PROPERTY = "maxPoolConnections";
	public final static int DEFAULT_CONNECTION_POOL_TIMEOUT = 20; //used also in ArtConfig

	/**
	 * Load art database configuration from properties file
	 *
	 * @return object with art database properties or null if file not found or
	 * error occurred
	 */
	public static ArtDatabaseForm loadConfiguration(String artDatabaseFilePath) {
		ArtDatabaseForm artDatabaseForm = null;

		try {
			File settingsFile = new File(artDatabaseFilePath);
			if (settingsFile.exists()) {
				FileInputStream o = new FileInputStream(artDatabaseFilePath);
				Properties p = new Properties();
				try {
					p.load(o);

					artDatabaseForm = new ArtDatabaseForm();

					artDatabaseForm.setDriver(p.getProperty(DRIVER_PROPERTY));
					artDatabaseForm.setUrl(p.getProperty(URL_PROPERTY));
					artDatabaseForm.setUsername(p.getProperty(USERNAME_PROPERTY));

					String artDbPassword = p.getProperty(PASSWORD_PROPERTY);
					artDbPassword = Encrypter.decrypt(artDbPassword);
					artDatabaseForm.setPassword(artDbPassword);

					artDatabaseForm.setConnectionTestSql(p.getProperty(CONNECTION_TEST_SQL_PROPERTY));

					int artDbPoolTimeout = NumberUtils.toInt(p.getProperty(CONNECTION_POOL_TIMEOUT_PROPERTY));
					if (artDbPoolTimeout <= 0) {
						artDbPoolTimeout = DEFAULT_CONNECTION_POOL_TIMEOUT;
					}
					artDatabaseForm.setConnectionPoolTimeout(artDbPoolTimeout);

					final int MAX_POOL_CONNECTIONS = 20;
					int maxPoolConnections = NumberUtils.toInt(p.getProperty(MAX_POOL_CONNECTIONS_PROPERTY));
					if (maxPoolConnections <= 0) {
						maxPoolConnections = MAX_POOL_CONNECTIONS;
					}
					artDatabaseForm.setMaxPoolConnections(maxPoolConnections);
				} finally {
					o.close();
				}
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
		}

		return artDatabaseForm;
	}

	/**
	 * Save art database configuration to properties file
	 *
	 * @param artDatabaseForm
	 * @param artDatabaseFilePath
	 */
	public static void SaveConfiguration(ArtDatabaseForm artDatabaseForm, String artDatabaseFilePath)
			throws FileNotFoundException, IOException {

		Properties p = new Properties();
		p.setProperty(DRIVER_PROPERTY, artDatabaseForm.getDriver());
		p.setProperty(URL_PROPERTY, artDatabaseForm.getUrl());
		p.setProperty(USERNAME_PROPERTY, artDatabaseForm.getUsername());
		p.setProperty(PASSWORD_PROPERTY, Encrypter.encrypt(artDatabaseForm.getPassword()));
		p.setProperty(CONNECTION_TEST_SQL_PROPERTY, artDatabaseForm.getConnectionTestSql());
		p.setProperty(CONNECTION_POOL_TIMEOUT_PROPERTY, String.valueOf(artDatabaseForm.getConnectionPoolTimeout()));
		p.setProperty(MAX_POOL_CONNECTIONS_PROPERTY, String.valueOf(artDatabaseForm.getMaxPoolConnections()));

		FileOutputStream o = new FileOutputStream(artDatabaseFilePath, false);
		try {
			p.store(o, "ART Database Properties");
		} finally {
			o.close();
		}
	}
}

package art.artdatabase;

import art.utils.Encrypter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utility methods related to art database configuration
 *
 * @author Timothy Anyona
 */
public class ArtDatabaseUtils {

	final static Logger logger = LoggerFactory.getLogger(ArtDatabaseUtils.class);
	public final static int DEFAULT_CONNECTION_POOL_TIMEOUT = 20; //used also in ArtConfig

	/**
	 * Load art database configuration from file
	 *
	 * @return object with art database properties or null if file not found or
	 * error occurred
	 */
	public static ArtDatabaseForm loadConfiguration(String artDatabaseFilePath) {
		ArtDatabaseForm artDatabaseForm = null;

		try {
			File artDatabaseFile = new File(artDatabaseFilePath);
			if (artDatabaseFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				artDatabaseForm = mapper.readValue(artDatabaseFile, ArtDatabaseForm.class);

				artDatabaseForm.setPassword(Encrypter.decrypt(artDatabaseForm.getPassword()));

				if (artDatabaseForm.getConnectionPoolTimeout() <= 0) {
					artDatabaseForm.setConnectionPoolTimeout(DEFAULT_CONNECTION_POOL_TIMEOUT);
				}

				final int DEFAULT_MAX_POOL_CONNECTIONS = 20;
				if (artDatabaseForm.getMaxPoolConnections() <= 0) {
					artDatabaseForm.setMaxPoolConnections(DEFAULT_MAX_POOL_CONNECTIONS);
				}
			} else {
				logger.info("ART Database configuration file not found");
			}

		} catch (Exception ex) {
			logger.error("Error", ex);
		}

		return artDatabaseForm;
	}

	/**
	 * Save art database configuration to file
	 *
	 * @param artDatabaseForm
	 * @param artDatabaseFilePath
	 */
	public static void SaveConfiguration(ArtDatabaseForm artDatabaseForm, String artDatabaseFilePath)
			throws FileNotFoundException, IOException {

		//obfuscate password field
		artDatabaseForm.setPassword(Encrypter.encrypt(artDatabaseForm.getPassword()));

		File artDatabaseFile = new File(artDatabaseFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(artDatabaseFile, artDatabaseForm);
	}
}

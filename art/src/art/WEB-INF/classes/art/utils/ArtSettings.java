package art.utils;

import art.servlets.ArtDBCP;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load and write art settings to file
 *
 * @author Enrico Liboni
 */
public class ArtSettings {

	final static Logger logger = LoggerFactory.getLogger(ArtSettings.class);
	Properties p;

	/**
	 *
	 */
	public ArtSettings() {
		p = new Properties();
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	public void setSetting(String key, String value) {
		p.setProperty(key, value);
	}

	/**
	 *
	 * @param key
	 * @return property value for the given key
	 */
	public String getSetting(String key) {
		return p.getProperty(key);
	}

	/**
	 *
	 * @param fileName
	 * @return store the properties to given file
	 */
	public boolean save(String fileName) {
		boolean success = false;

		try {
			FileOutputStream o = new FileOutputStream(fileName, false);
			try {
				p.store(o, "ART Settings File");
			} finally {
				o.close();
			}

			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}

	/**
	 * load settings from default location
	 *
	 * @return
	 */
	public boolean load() {
		String settingsFilePath = ArtDBCP.getSettingsFilePath();
		File settingsFile = new File(settingsFilePath);
		if (!settingsFile.exists()) {
			//art.properties doesn't exit. try art.props
			String sep = java.io.File.separator;
			settingsFilePath = ArtDBCP.getAppPath() + sep + "WEB-INF" + sep + "art.props";
		}
		
		return load(settingsFilePath);
	}

	/**
	 *
	 * @param fileName
	 * @return <code>true</code> if properties file loaded successfully
	 */
	public boolean load(String fileName) {
		boolean success = false;

		try {
			FileInputStream o = new FileInputStream(fileName);
			try {
				p.load(o);
			} finally {
				o.close();
			}

			success = true;
		} catch (FileNotFoundException e) {
			logger.warn("ART settings have not been defined", e);
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}
}

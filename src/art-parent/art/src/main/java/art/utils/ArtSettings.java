/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.servlets.ArtConfig;
import java.io.File;
import java.io.FileInputStream;
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
		return load(ArtConfig.getSettingsFilePath());
	}

	/**
	 *
	 * @param fileName
	 * @return <code>true</code> if properties file loaded successfully
	 */
	public boolean load(String fileName) {
		boolean success = false;

		try {
			File settingsFile = new File(fileName);
			if (settingsFile.exists()) {
				FileInputStream o = new FileInputStream(fileName);
				try {
					p.load(o);
				} finally {
					o.close();
				}

				success = true;
			} else {
				logger.warn("ART settings file not found: {}", fileName);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}
}

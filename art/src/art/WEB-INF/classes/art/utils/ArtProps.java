package art.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load and write art settings to file
 *
 * @author Enrico Liboni
 */
public class ArtProps {

	final static Logger logger = LoggerFactory.getLogger(ArtProps.class);
	Properties p;
	String artPropsDefaultName = "art.properties";

	/**
	 *
	 */
	public ArtProps() {
		p = new Properties();
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	public void setProp(String key, String value) {
		if (p != null) {
			p.setProperty(key, value);
		}
	}

	/**
	 *
	 * @param key
	 * @return property value for the given key
	 */
	public String getProp(String key) {
		return p.getProperty(key);
	}

	/**
	 *
	 * @return store the properties to the default file
	 */
	public boolean store() {
		return store(artPropsDefaultName);
	}

	/**
	 *
	 * @param fileName
	 * @return store the properties to given file
	 */
	public boolean store(String fileName) {
		boolean success = false;

		try {
			FileOutputStream o = new FileOutputStream(fileName, false);
			try {
				p.store(o, "ART Properties File");
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
	 *
	 * @return <code>true</code> if properties file loaded successfully
	 */
	public boolean load() {
		return load(artPropsDefaultName);
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
			logger.warn("The file {} has not been created yet", fileName);
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}

	/**
	 *
	 * @param fileName
	 */
	public void printProps(String fileName) {
		try {
			FileInputStream o = new FileInputStream(fileName);
			try {
				p.load(o);
			} finally {
				o.close();
			}
			Enumeration enumer = p.propertyNames();
			String propName, propValue;
			while (enumer.hasMoreElements()) {
				propName = (String) enumer.nextElement();
				propValue = p.getProperty(propName);
				System.out.println("Name:   " + propName);
				System.out.println(" Value: " + propValue);
			}

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
}

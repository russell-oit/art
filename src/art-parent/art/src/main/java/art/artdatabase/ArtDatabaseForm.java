package art.artdatabase;

import art.utils.ArtUtils;

/**
 * Class to act as a form backing bean for art database configuration
 * 
 * @author Timothy Anyona
 */
public class ArtDatabaseForm {
	
	private String driver;
	private String url;
	private String username;
	private String password;
	private int connectionPoolTimeout=ArtUtils.DEFAULT_CONNECTION_POOL_TIMEOUT;
	private String connectionTestSql;

	/**
	 * Get the value of connectionTestSql
	 *
	 * @return the value of connectionTestSql
	 */
	public String getConnectionTestSql() {
		return connectionTestSql;
	}

	/**
	 * Set the value of connectionTestSql
	 *
	 * @param connectionTestSql new value of connectionTestSql
	 */
	public void setConnectionTestSql(String connectionTestSql) {
		this.connectionTestSql = connectionTestSql;
	}

	/**
	 * Get the value of connectionPoolTimeout
	 *
	 * @return the value of connectionPoolTimeout
	 */
	public int getConnectionPoolTimeout() {
		return connectionPoolTimeout;
	}

	/**
	 * Set the value of connectionPoolTimeout
	 *
	 * @param connectionPoolTimeout new value of connectionPoolTimeout
	 */
	public void setConnectionPoolTimeout(int connectionPoolTimeout) {
		this.connectionPoolTimeout = connectionPoolTimeout;
	}

	/**
	 * Get the value of password
	 *
	 * @return the value of password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the value of password
	 *
	 * @param password new value of password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get the value of username
	 *
	 * @return the value of username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the value of username
	 *
	 * @param username new value of username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the value of url
	 *
	 * @return the value of url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the value of url
	 *
	 * @param url new value of url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get the value of driver
	 *
	 * @return the value of driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Set the value of driver
	 *
	 * @param driver new value of driver
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	
}

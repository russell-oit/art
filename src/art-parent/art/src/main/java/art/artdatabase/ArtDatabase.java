package art.artdatabase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Class to represent art database configuration
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtDatabase implements Serializable {

	private static final long serialVersionUID = 1L;
	private String driver;
	@NotBlank
	private String url;
	private String username;
	private String password;
	@JsonIgnore
	private boolean useBlankPassword; //only used for user interface logic
	private String testSql;
	private int connectionPoolTimeout;
	private int maxPoolConnections; //setting used by art database and all datasources
	private boolean jndi;

	/**
	 * Get the value of jndi
	 *
	 * @return the value of jndi
	 */
	public boolean isJndi() {
		return jndi;
	}

	/**
	 * Set the value of jndi
	 *
	 * @param jndi new value of jndi
	 */
	public void setJndi(boolean jndi) {
		this.jndi = jndi;
	}

	/**
	 * Get the value of useBlankPassword. only used for user interface logic
	 *
	 * @return the value of useBlankPassword
	 */
	public boolean isUseBlankPassword() {
		return useBlankPassword;
	}

	/**
	 * Set the value of useBlankPassword. only used for user interface logic
	 *
	 * @param useBlankPassword new value of useBlankPassword
	 */
	public void setUseBlankPassword(boolean useBlankPassword) {
		this.useBlankPassword = useBlankPassword;
	}

	/**
	 * Get the value of maxPoolConnections
	 *
	 * @return the value of maxPoolConnections
	 */
	public int getMaxPoolConnections() {
		return maxPoolConnections;
	}

	/**
	 * Set the value of maxPoolConnections
	 *
	 * @param maxPoolConnections new value of maxPoolConnections
	 */
	public void setMaxPoolConnections(int maxPoolConnections) {
		this.maxPoolConnections = maxPoolConnections;
	}

	/**
	 * Get the value of testSql
	 *
	 * @return the value of testSql
	 */
	public String getTestSql() {
		return testSql;
	}

	/**
	 * Set the value of testSql
	 *
	 * @param testSql new value of testSql
	 */
	public void setTestSql(String testSql) {
		this.testSql = testSql;
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

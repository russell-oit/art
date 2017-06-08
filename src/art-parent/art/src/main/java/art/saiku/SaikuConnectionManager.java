package art.saiku;

import art.datasource.Datasource;
import art.report.Report;
import art.report.ReportService;
import art.user.User;
import art.utils.MondrianHelper;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.olap4j.OlapConnection;
import org.saiku.datasources.connection.IConnectionManager;
import org.saiku.datasources.connection.ISaikuConnection;
import org.saiku.datasources.connection.SaikuOlapConnection;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaikuConnectionManager implements IConnectionManager {

	private static final Logger log = LoggerFactory.getLogger(SaikuConnectionManager.class);
	private Map<String, ISaikuConnection> connections = new HashMap<>();
	private Map<String, Properties> connectProperties;
	private final User user;
	private final String templatesPath;

	public SaikuConnectionManager(User user, String templatesPath) {
		this.user = user;
		//pass template instead of calling Config.getTemplatesPath() to avoid circular reference. Config references this class
		this.templatesPath = templatesPath;
	}

	public void init() throws SaikuOlapException {
		if (connectProperties == null) {
			connectProperties = new HashMap<>();
		} else {
			destroy();
		}

		try {
			ReportService reportService = new ReportService();
			int userId = user.getUserId();
			List<Report> reports = reportService.getAvailableSaikuConnectionReports(userId);
			MondrianHelper mondrianHelper = new MondrianHelper();
			for (Report report : reports) {
				String roles = mondrianHelper.getRolesString(report.getReportId(), user);
				Properties properties = getSaikuConnectProperties(report, roles, templatesPath);
				connectProperties.put(report.getName(), properties);
			}
		} catch (SQLException ex) {
			throw new SaikuOlapException(ex);
		}

		this.connections = getAllConnections();
	}

	protected ISaikuConnection getInternalConnection(String name, Properties properties)
			throws SaikuOlapException {

		ISaikuConnection con;

		if (!connections.containsKey(name)) {
			con = connect(name, properties);
			if (con != null) {
				connections.put(name, con);
			}

		} else {
			con = connections.get(name);
		}
		return con;
	}

	protected ISaikuConnection refreshInternalConnection(String name, Properties properties) {
		try {
			ISaikuConnection con = connections.remove(name);
			if (con != null) {
				con.clearCache();
				con.close();
			}
			return getInternalConnection(name, properties);
		} catch (Exception e) {
			log.error("Could not get internal connection", e);
		}
		return null;
	}

	private ISaikuConnection connect(String name, Properties properties) throws SaikuOlapException {
		if (properties == null) {
			throw new SaikuOlapException("properties must not be null");
		}

		try {
			ISaikuConnection con = new SaikuOlapConnection(name, properties);
			con.connect();
			if (con.initialized()) {
				return con;
			}
		} catch (Exception e) {
			log.error("Could not get connection", e);
		}

		return null;
	}

	public void destroy() {
		Map<String, OlapConnection> olapConnections = getCurrentOlapConnections();
		if (olapConnections != null && !olapConnections.isEmpty()) {
			for (OlapConnection con : olapConnections.values()) {
				try {
					if (!con.isClosed()) {
						con.close();
					}
				} catch (Exception e) {
					log.error("Could not close connection", e);
				}
			}
		}

		connections.clear();

		if (olapConnections != null) {
			olapConnections.clear();
		}

		if (connectProperties != null) {
			connectProperties.clear();
		}
	}

	public ISaikuConnection getConnection(String name) throws SaikuOlapException {
		Properties properties = connectProperties.get(name);
		ISaikuConnection con = getInternalConnection(name, properties);
		return con;
	}

	public void refreshAllConnections() {
		for (String name : connectProperties.keySet()) {
			refreshConnection(name);
		}
	}

	public void refreshConnection(String name) {
		Properties properties = connectProperties.get(name);
		refreshInternalConnection(name, properties);
	}

	public Map<String, ISaikuConnection> getAllConnections() throws SaikuOlapException {
		Map<String, ISaikuConnection> resultDs = new HashMap<>();

		for (String name : connectProperties.keySet()) {
			ISaikuConnection con = getConnection(name);
			if (con != null) {
				resultDs.put(name, con);
			}
		}

		return resultDs;
	}

	public OlapConnection getOlapConnection(String name) throws SaikuOlapException {
		ISaikuConnection con = getConnection(name);
		if (con != null) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				return (OlapConnection) o;
			}
		}
		return null;
	}

	public OlapConnection getExistingOlapConnection(String name) throws SaikuOlapException {
		ISaikuConnection con = connections.get(name);
		if (con != null) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				return (OlapConnection) o;
			}
		}
		return null;
	}

	public Map<String, OlapConnection> getAllOlapConnections() throws SaikuOlapException {
		Map<String, ISaikuConnection> saikuConnections = getAllConnections();
		Map<String, OlapConnection> olapConnections = new HashMap<>();
		for (ISaikuConnection con : saikuConnections.values()) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				olapConnections.put(con.getName(), (OlapConnection) o);
			}
		}

		return olapConnections;
	}

	public Map<String, OlapConnection> getCurrentOlapConnections() {
		Map<String, OlapConnection> olapConnections = new HashMap<>();
		for (ISaikuConnection con : connections.values()) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				olapConnections.put(con.getName(), (OlapConnection) o);
			}
		}

		return olapConnections;
	}

	/**
	 * Returns details that are to be used to make a saiku olap connection
	 *
	 * @param report the saiku connection report
	 * @param roles the roles string to use
	 * @param templatesPath the templates path for the schema xml file
	 * @return details that are to be used to make a saiku olap connection
	 */
	private Properties getSaikuConnectProperties(Report report, String roles,
			String templatesPath) {

		Properties properties = new Properties();

		Datasource datasource = report.getDatasource();

		properties.put(ISaikuConnection.USERNAME_KEY, datasource.getUsername());
		properties.put(ISaikuConnection.PASSWORD_KEY, datasource.getPassword());
		properties.put(ISaikuConnection.DRIVER_KEY, datasource.getDriver());

		String url = datasource.getUrl();

		String templateFileName = report.getTemplate();
		if (!StringUtils.containsIgnoreCase(url, ";Catalog")
				&& StringUtils.isNotBlank(templateFileName)) {
			if (!StringUtils.endsWith(url, ";")) {
				url += ";";
			}
			String catalogPath = templatesPath + templateFileName;
			url += "Catalog=" + catalogPath + ";";
		}

		if (StringUtils.isNotBlank(roles)) {
			if (!StringUtils.endsWith(url, ";")) {
				url += ";";
			}
			url += "Role='" + roles + "';";
		}

		properties.put(ISaikuConnection.URL_KEY, url);

		return properties;
	}
}

package art.saiku;

import art.datasource.Datasource;
import art.datasource.DatasourceService;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.olap4j.OlapConnection;
import org.saiku.datasources.connection.IConnectionManager;
import org.saiku.datasources.connection.SaikuOlapConnection;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class SaikuConnectionManager implements IConnectionManager {

	private static final Logger log = LoggerFactory.getLogger(SaikuConnectionManager.class);
	private Map<String, SaikuOlapConnection> connections = new HashMap<>();
	private Map<String, Datasource> ds;

	public void init() throws SaikuOlapException, SQLException {
		if (ds == null) {
			ds = new HashMap<>();
		} else {
			destroy();
			ds.clear();
		}

		DatasourceService datasourceService = new DatasourceService();
		List<Datasource> olapDatasources = datasourceService.getOlapDatasources();
		for (Datasource datasource : olapDatasources) {
			if (StringUtils.isNotBlank(datasource.getDriver())) {
				ds.put(datasource.getName(), datasource);
			}
		}
		this.connections = getAllConnections();
	}

	protected SaikuOlapConnection getInternalConnection(String name, Datasource datasource)
			throws SaikuOlapException {

		SaikuOlapConnection con;

		if (!connections.containsKey(name)) {
			con = connect(name, datasource);
			if (con != null) {
				connections.put(name, con);
			}

		} else {
			con = connections.get(name);
		}
		return con;
	}

	protected SaikuOlapConnection refreshInternalConnection(String name, Datasource datasource) {
		try {
			SaikuOlapConnection con = connections.remove(name);
			if (con != null) {
				con.clearCache();
			}
			return getInternalConnection(name, datasource);
		} catch (Exception e) {
			log.error("Could not get internal connection", e);
		}
		return null;
	}

	private SaikuOlapConnection connect(String name, Datasource datasource) throws SaikuOlapException {
		if (datasource != null) {

			try {
				SaikuOlapConnection con = new SaikuOlapConnection(name, datasource.getSaikuProperties());
				if (con.connect()) {
				}
				if (con.initialized()) {
					return con;
				}
			} catch (Exception e) {
				log.error("Could not get connection", e);
			}

			return null;
		}

		throw new SaikuOlapException("Cannot find connection: (" + name + ")");
	}

	public void destroy() throws SaikuOlapException, SQLException {
		Map<String, OlapConnection> saikuConnections = getAllOlapConnections();
		if (saikuConnections != null && !saikuConnections.isEmpty()) {
			for (OlapConnection con : saikuConnections.values()) {
				try {
					if (!con.isClosed()) {
						con.close();
					}
				} catch (Exception e) {
					log.error("Could not close connection", e);
				}
			}
		}
		if (saikuConnections != null) {
			saikuConnections.clear();
		}
	}

	public SaikuOlapConnection getConnection(String name) throws SaikuOlapException {
		Datasource datasource = ds.get(name);
		SaikuOlapConnection con = getInternalConnection(name, datasource);
		return con;
	}

	public void refreshAllConnections() {
		for (String name : ds.keySet()) {
			refreshConnection(name);
		}
	}

	public void refreshConnection(String name) {
		Datasource datasource = ds.get(name);
		refreshInternalConnection(name, datasource);
	}

	public Map<String, SaikuOlapConnection> getAllConnections() throws SaikuOlapException, SQLException {
		Map<String, SaikuOlapConnection> resultDs = new HashMap<>();

		for (String name : ds.keySet()) {
			SaikuOlapConnection con = getConnection(name);
			if (con != null) {
				resultDs.put(name, con);
			}
		}
		return resultDs;
	}

	public OlapConnection getOlapConnection(String name) throws SaikuOlapException {
		SaikuOlapConnection con = getConnection(name);
		if (con != null) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				return (OlapConnection) o;
			}
		} else {

		}
		return null;
	}

	public Map<String, OlapConnection> getAllOlapConnections() throws SaikuOlapException, SQLException {
		Map<String, SaikuOlapConnection> saikuConnections = getAllConnections();
		Map<String, OlapConnection> olapConnections = new HashMap<>();
		for (SaikuOlapConnection con : saikuConnections.values()) {
			Object o = con.getConnection();
			if (o != null && o instanceof OlapConnection) {
				olapConnections.put(con.getName(), (OlapConnection) o);
			}
		}

		return olapConnections;
	}
}

package art.artdatabase;

import art.datasource.DatasourceInfo;
import art.enums.ConnectionPoolLibrary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Represents the art database configuration
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtDatabase extends DatasourceInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private int maxPoolConnections; //setting used by art database and all report datasources
	private ConnectionPoolLibrary connectionPoolLibrary; //setting used by art database and all report datasources
	public static final int ART_DATABASE_DATASOURCE_ID = -1; //"datasource id" for the art database in the connection pool map
	private static final String ART_DATABASE_DATASOURCE_NAME = "ART Database"; //"datasource name" for the art database in the connection pool map

	@Override
	@JsonIgnore
	public int getDatasourceId() {
		return ART_DATABASE_DATASOURCE_ID;
	}

	@Override
	@JsonIgnore
	public String getName() {
		return ART_DATABASE_DATASOURCE_NAME;
	}
	
	/**
	 * @return the maxPoolConnections
	 */
	public int getMaxPoolConnections() {
		return maxPoolConnections;
	}

	/**
	 * @param maxPoolConnections the maxPoolConnections to set
	 */
	public void setMaxPoolConnections(int maxPoolConnections) {
		this.maxPoolConnections = maxPoolConnections;
	}

	/**
	 * @return the connectionPoolLibrary
	 */
	public ConnectionPoolLibrary getConnectionPoolLibrary() {
		return connectionPoolLibrary;
	}

	/**
	 * @param connectionPoolLibrary the connectionPoolLibrary to set
	 */
	public void setConnectionPoolLibrary(ConnectionPoolLibrary connectionPoolLibrary) {
		this.connectionPoolLibrary = connectionPoolLibrary;
	}

}

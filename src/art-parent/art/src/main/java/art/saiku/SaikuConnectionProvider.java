/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.saiku;

import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.service.olap.OlapDiscoverService;
import org.saiku.service.olap.ThinQueryService;

/**
 * Wrapper for classes required to provide a saiku connection and
 * related services for a user
 *
 * @author Timothy Anyona
 */
public class SaikuConnectionProvider {

	private SaikuConnectionManager connectionManager;
	private OlapMetaExplorer metaExplorer;
	private OlapDiscoverService discoverService;
	private ThinQueryService thinQueryService;

	/**
	 * @return the connectionManager
	 */
	public SaikuConnectionManager getConnectionManager() {
		return connectionManager;
	}

	/**
	 * @param connectionManager the connectionManager to set
	 */
	public void setConnectionManager(SaikuConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	/**
	 * @return the metaExplorer
	 */
	public OlapMetaExplorer getMetaExplorer() {
		return metaExplorer;
	}

	/**
	 * @param metaExplorer the metaExplorer to set
	 */
	public void setMetaExplorer(OlapMetaExplorer metaExplorer) {
		this.metaExplorer = metaExplorer;
	}

	/**
	 * @return the discoverService
	 */
	public OlapDiscoverService getDiscoverService() {
		return discoverService;
	}

	/**
	 * @param discoverService the discoverService to set
	 */
	public void setDiscoverService(OlapDiscoverService discoverService) {
		this.discoverService = discoverService;
	}

	/**
	 * @return the thinQueryService
	 */
	public ThinQueryService getThinQueryService() {
		return thinQueryService;
	}

	/**
	 * @param thinQueryService the thinQueryService to set
	 */
	public void setThinQueryService(ThinQueryService thinQueryService) {
		this.thinQueryService = thinQueryService;
	}
}

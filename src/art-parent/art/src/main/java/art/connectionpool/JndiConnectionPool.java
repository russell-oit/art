/*
 * Copyright (C) 2015 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.connectionpool;

import art.datasource.DatasourceInfo;
import art.utils.ArtUtils;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Timothy Anyona
 */
public class JndiConnectionPool extends ConnectionPool {

	@Override
	protected DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		try {
			//for jndi datasources, the url contains the jndi name/resource reference
			return ArtUtils.getJndiDataSource(datasourceInfo.getUrl());
		} catch (NamingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}

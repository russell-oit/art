/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.dbutils;

import java.util.Objects;
import javax.sql.DataSource;

/**
 * Class to encapsulate different connection pool implementations. To provide a
 * common interface for some properties which may be implemented differently
 * e.g. some libraries don't have a pool name facility so this wrapper enables
 * searching for a connection pool by name
 *
 * @author Timothy Anyona
 */
public class ConnectionPoolWrapper {

	private int poolId;
	private String poolName;
	private final DataSource pool; //javax.sql.DataSource implementation

	public ConnectionPoolWrapper(DataSource pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		this.pool = pool;
	}

	/**
	 * @return the poolId
	 */
	public int getPoolId() {
		return poolId;
	}

	/**
	 * @param poolId the poolId to set
	 */
	public void setPoolId(int poolId) {
		this.poolId = poolId;
	}

	/**
	 * @return the poolName
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * @param poolName the poolName to set
	 */
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	/**
	 * @return the pool
	 */
	public DataSource getPool() {
		return pool;
	}

}

/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.connection;

import art.utils.AjaxResponse;

/**
 * Class to facilitate ajax response after reset connection
 *
 * @author Timothy Anyona
 */
public class ResetConnectionResponse extends AjaxResponse {
	//can't return the whole datasource object as some properties (in the enhancedconnection) can't be converted to json

	private int poolSize;
	private int inUseCount;

	/**
	 * @return the poolSize
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @return the inUseCount
	 */
	public int getInUseCount() {
		return inUseCount;
	}

	/**
	 * @param inUseCount the inUseCount to set
	 */
	public void setInUseCount(int inUseCount) {
		this.inUseCount = inUseCount;
	}
}

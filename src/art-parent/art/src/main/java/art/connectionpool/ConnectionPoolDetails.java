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
package art.connectionpool;

import java.io.Serializable;

/**
 * Represents a connection pool's details, providing access to the pool's name,
 * size and so on. This class is used for displaying connection pool details in
 * the user interface.
 *
 * @author Timothy Anyona
 */
public class ConnectionPoolDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private int poolId;
	private String name;
	private int maxPoolSize;
	private Integer currentPoolSize;
	private Integer highestReachedPoolSize;
	private Integer inUseCount;
	private Long totalConnectionRequests;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the maxPoolSize
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * @param maxPoolSize the maxPoolSize to set
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * @return the currentPoolSize
	 */
	public Integer getCurrentPoolSize() {
		return currentPoolSize;
	}

	/**
	 * @param currentPoolSize the currentPoolSize to set
	 */
	public void setCurrentPoolSize(Integer currentPoolSize) {
		this.currentPoolSize = currentPoolSize;
	}

	/**
	 * @return the highestReachedPoolSize
	 */
	public Integer getHighestReachedPoolSize() {
		return highestReachedPoolSize;
	}

	/**
	 * @param highestReachedPoolSize the highestReachedPoolSize to set
	 */
	public void setHighestReachedPoolSize(Integer highestReachedPoolSize) {
		this.highestReachedPoolSize = highestReachedPoolSize;
	}

	/**
	 * @return the inUseCount
	 */
	public Integer getInUseCount() {
		return inUseCount;
	}

	/**
	 * @param inUseCount the inUseCount to set
	 */
	public void setInUseCount(Integer inUseCount) {
		this.inUseCount = inUseCount;
	}

	/**
	 * @return the totalConnectionRequests
	 */
	public Long getTotalConnectionRequests() {
		return totalConnectionRequests;
	}

	/**
	 * @param totalConnectionRequests the totalConnectionRequests to set
	 */
	public void setTotalConnectionRequests(Long totalConnectionRequests) {
		this.totalConnectionRequests = totalConnectionRequests;
	}
}

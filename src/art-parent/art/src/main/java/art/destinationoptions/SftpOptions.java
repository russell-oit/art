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
package art.destinationoptions;

import java.io.Serializable;

/**
 * Represents options for sftp destinations
 *
 * @author Timothy Anyona
 */
public class SftpOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer sessionConnectTimeoutSeconds;
	private Integer channelConnectTimeoutSeconds;
	private Integer serverAliveIntervalSeconds;

	/**
	 * @return the sessionConnectTimeoutSeconds
	 */
	public Integer getSessionConnectTimeoutSeconds() {
		return sessionConnectTimeoutSeconds;
	}

	/**
	 * @param sessionConnectTimeoutSeconds the sessionConnectTimeoutSeconds to
	 * set
	 */
	public void setSessionConnectTimeoutSeconds(Integer sessionConnectTimeoutSeconds) {
		this.sessionConnectTimeoutSeconds = sessionConnectTimeoutSeconds;
	}

	/**
	 * @return the channelConnectTimeoutSeconds
	 */
	public Integer getChannelConnectTimeoutSeconds() {
		return channelConnectTimeoutSeconds;
	}

	/**
	 * @param channelConnectTimeoutSeconds the channelConnectTimeoutSeconds to
	 * set
	 */
	public void setChannelConnectTimeoutSeconds(Integer channelConnectTimeoutSeconds) {
		this.channelConnectTimeoutSeconds = channelConnectTimeoutSeconds;
	}

	/**
	 * @return the serverAliveIntervalSeconds
	 */
	public Integer getServerAliveIntervalSeconds() {
		return serverAliveIntervalSeconds;
	}

	/**
	 * @param serverAliveIntervalSeconds the serverAliveIntervalSeconds to set
	 */
	public void setServerAliveIntervalSeconds(Integer serverAliveIntervalSeconds) {
		this.serverAliveIntervalSeconds = serverAliveIntervalSeconds;
	}

}

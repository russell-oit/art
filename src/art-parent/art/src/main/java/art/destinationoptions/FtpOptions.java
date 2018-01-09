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

/**
 * Represents options for ftp destinations
 *
 * @author Timothy Anyona
 */
public class FtpOptions {

	private Long controlKeepAliveTimeoutSeconds;
	private final Integer DEFAULT_CONNECT_TIMEOUT_SECONDS = 60;
	private Integer connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;
	private Integer defaultTimeoutSeconds;

	/**
	 * @return the controlKeepAliveTimeoutSeconds
	 */
	public Long getControlKeepAliveTimeoutSeconds() {
		return controlKeepAliveTimeoutSeconds;
	}

	/**
	 * @param controlKeepAliveTimeoutSeconds the controlKeepAliveTimeoutSeconds to set
	 */
	public void setControlKeepAliveTimeoutSeconds(Long controlKeepAliveTimeoutSeconds) {
		this.controlKeepAliveTimeoutSeconds = controlKeepAliveTimeoutSeconds;
	}

	/**
	 * @return the connectTimeoutSeconds
	 */
	public Integer getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	/**
	 * @param connectTimeoutSeconds the connectTimeoutSeconds to set
	 */
	public void setConnectTimeoutSeconds(Integer connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

	/**
	 * @return the defaultTimeoutSeconds
	 */
	public Integer getDefaultTimeoutSeconds() {
		return defaultTimeoutSeconds;
	}

	/**
	 * @param defaultTimeoutSeconds the defaultTimeoutSeconds to set
	 */
	public void setDefaultTimeoutSeconds(Integer defaultTimeoutSeconds) {
		this.defaultTimeoutSeconds = defaultTimeoutSeconds;
	}

}

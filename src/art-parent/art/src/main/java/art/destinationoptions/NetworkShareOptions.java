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
 * Represents options for network share destinations
 *
 * @author Timothy Anyona
 */
public class NetworkShareOptions implements Serializable {
	//https://github.com/hierynomus/smbj/blob/master/src/main/java/com/hierynomus/smbj/SmbConfig.java

	private static final long serialVersionUID = 1L;
	private boolean anonymousUser;
	private boolean guestUser;
	private Long timeoutSeconds;
	private Long socketTimeoutSeconds;
	private Boolean multiProtocolNegotiate;
	private Boolean dfsEnabled;
	private Boolean signingRequired;
	private Integer bufferSize;

	/**
	 * @return the anonymousUser
	 */
	public boolean isAnonymousUser() {
		return anonymousUser;
	}

	/**
	 * @param anonymousUser the anonymousUser to set
	 */
	public void setAnonymousUser(boolean anonymousUser) {
		this.anonymousUser = anonymousUser;
	}

	/**
	 * @return the guestUser
	 */
	public boolean isGuestUser() {
		return guestUser;
	}

	/**
	 * @param guestUser the guestUser to set
	 */
	public void setGuestUser(boolean guestUser) {
		this.guestUser = guestUser;
	}

	/**
	 * @return the timeoutSeconds
	 */
	public Long getTimeoutSeconds() {
		return timeoutSeconds;
	}

	/**
	 * @param timeoutSeconds the timeoutSeconds to set
	 */
	public void setTimeoutSeconds(Long timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	/**
	 * @return the socketTimeoutSeconds
	 */
	public Long getSocketTimeoutSeconds() {
		return socketTimeoutSeconds;
	}

	/**
	 * @param socketTimeoutSeconds the socketTimeoutSeconds to set
	 */
	public void setSocketTimeoutSeconds(Long socketTimeoutSeconds) {
		this.socketTimeoutSeconds = socketTimeoutSeconds;
	}

	/**
	 * @return the multiProtocolNegotiate
	 */
	public Boolean getMultiProtocolNegotiate() {
		return multiProtocolNegotiate;
	}

	/**
	 * @param multiProtocolNegotiate the multiProtocolNegotiate to set
	 */
	public void setMultiProtocolNegotiate(Boolean multiProtocolNegotiate) {
		this.multiProtocolNegotiate = multiProtocolNegotiate;
	}

	/**
	 * @return the dfsEnabled
	 */
	public Boolean getDfsEnabled() {
		return dfsEnabled;
	}

	/**
	 * @param dfsEnabled the dfsEnabled to set
	 */
	public void setDfsEnabled(Boolean dfsEnabled) {
		this.dfsEnabled = dfsEnabled;
	}

	/**
	 * @return the signingRequired
	 */
	public Boolean getSigningRequired() {
		return signingRequired;
	}

	/**
	 * @param signingRequired the signingRequired to set
	 */
	public void setSigningRequired(Boolean signingRequired) {
		this.signingRequired = signingRequired;
	}

	/**
	 * @return the bufferSize
	 */
	public Integer getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(Integer bufferSize) {
		this.bufferSize = bufferSize;
	}

}

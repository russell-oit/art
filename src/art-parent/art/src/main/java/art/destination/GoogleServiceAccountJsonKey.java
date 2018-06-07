/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.destination;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Represents a google service account json key
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleServiceAccountJsonKey implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String private_key;
	private String client_email;

	/**
	 * @return the private_key
	 */
	public String getPrivate_key() {
		return private_key;
	}

	/**
	 * @param private_key the private_key to set
	 */
	public void setPrivate_key(String private_key) {
		this.private_key = private_key;
	}

	/**
	 * @return the client_email
	 */
	public String getClient_email() {
		return client_email;
	}

	/**
	 * @param client_email the client_email to set
	 */
	public void setClient_email(String client_email) {
		this.client_email = client_email;
	}
	
}

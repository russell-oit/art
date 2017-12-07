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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * Represents options for website destinations
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebsiteOptions {

	private String usernameField = "username";
	private String passwordField = "password";
	private String fileField = "file";
	private String csrfTokenInputField;
	private String loginUrl;
	private String startUrl;
	private List<Map<String, String>> staticFields;
	private String csrfTokenCookie;
	private String csrfTokenOutputField;

	/**
	 * @return the usernameField
	 */
	public String getUsernameField() {
		return usernameField;
	}

	/**
	 * @param usernameField the usernameField to set
	 */
	public void setUsernameField(String usernameField) {
		this.usernameField = usernameField;
	}

	/**
	 * @return the passwordField
	 */
	public String getPasswordField() {
		return passwordField;
	}

	/**
	 * @param passwordField the passwordField to set
	 */
	public void setPasswordField(String passwordField) {
		this.passwordField = passwordField;
	}

	/**
	 * @return the fileField
	 */
	public String getFileField() {
		return fileField;
	}

	/**
	 * @param fileField the fileField to set
	 */
	public void setFileField(String fileField) {
		this.fileField = fileField;
	}

	/**
	 * @return the csrfTokenInputField
	 */
	public String getCsrfTokenInputField() {
		return csrfTokenInputField;
	}

	/**
	 * @param csrfTokenInputField the csrfTokenInputField to set
	 */
	public void setCsrfTokenInputField(String csrfTokenInputField) {
		this.csrfTokenInputField = csrfTokenInputField;
	}

	/**
	 * @return the loginUrl
	 */
	public String getLoginUrl() {
		return loginUrl;
	}

	/**
	 * @param loginUrl the loginUrl to set
	 */
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	/**
	 * @return the startUrl
	 */
	public String getStartUrl() {
		return startUrl;
	}

	/**
	 * @param startUrl the startUrl to set
	 */
	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	/**
	 * @return the staticFields
	 */
	public List<Map<String, String>> getStaticFields() {
		return staticFields;
	}

	/**
	 * @param staticFields the staticFields to set
	 */
	public void setStaticFields(List<Map<String, String>> staticFields) {
		this.staticFields = staticFields;
	}

	/**
	 * @return the csrfTokenCookie
	 */
	public String getCsrfTokenCookie() {
		return csrfTokenCookie;
	}

	/**
	 * @param csrfTokenCookie the csrfTokenCookie to set
	 */
	public void setCsrfTokenCookie(String csrfTokenCookie) {
		this.csrfTokenCookie = csrfTokenCookie;
	}

	/**
	 * @return the csrfTokenOutputField
	 */
	public String getCsrfTokenOutputField() {
		return csrfTokenOutputField;
	}

	/**
	 * @param csrfTokenOutputField the csrfTokenOutputField to set
	 */
	public void setCsrfTokenOutputField(String csrfTokenOutputField) {
		this.csrfTokenOutputField = csrfTokenOutputField;
	}

}

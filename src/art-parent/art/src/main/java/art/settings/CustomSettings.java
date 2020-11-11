/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Represents settings that aren't set from the user interface (for enhanced
 * security)
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSettings implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean showErrors = true; //whether exception details are shown in the user interface
	private boolean showErrorsApi = true; //whether exception details are shown when making api calls
	private boolean enableDirectParameterSubstitution = true; //whether direct parameter values in report sql is enabled - instead of using preparedstatements
	private String exportDirectory; //custom path for export files i.e. for files generated by reports and jobs
	private String workDirectory; //custom work directory for art files e.g. settings, templates
	private boolean checkExportFileAccess; //whether export files should be checked for user access before being accessed
	private boolean enableGroovySandbox = true; //whether to apply the groovy sandbox when running groovy scripts
	private boolean enableEmailing = true; //whether sending of emails is enabled
	private String jwtSecret;
	private String encryptionKey; //key for symmetric encryption within the application
	private EncryptionPassword encryptionPassword;
	private boolean allowRepositoryLogin = true;

	/**
	 * @return the showErrorsApi
	 */
	public boolean isShowErrorsApi() {
		return showErrorsApi;
	}

	/**
	 * @param showErrorsApi the showErrorsApi to set
	 */
	public void setShowErrorsApi(boolean showErrorsApi) {
		this.showErrorsApi = showErrorsApi;
	}

	/**
	 * @return the allowRepositoryLogin
	 */
	public boolean isAllowRepositoryLogin() {
		return allowRepositoryLogin;
	}

	/**
	 * @param allowRepositoryLogin the allowRepositoryLogin to set
	 */
	public void setAllowRepositoryLogin(boolean allowRepositoryLogin) {
		this.allowRepositoryLogin = allowRepositoryLogin;
	}

	/**
	 * @return the encryptionPassword
	 */
	public EncryptionPassword getEncryptionPassword() {
		return encryptionPassword;
	}

	/**
	 * @param encryptionPassword the encryptionPassword to set
	 */
	public void setEncryptionPassword(EncryptionPassword encryptionPassword) {
		this.encryptionPassword = encryptionPassword;
	}

	/**
	 * @return the encryptionKey
	 */
	public String getEncryptionKey() {
		return encryptionKey;
	}

	/**
	 * @param encryptionKey the encryptionKey to set
	 */
	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	/**
	 * @return the jwtSecret
	 */
	public String getJwtSecret() {
		return jwtSecret;
	}

	/**
	 * @param jwtSecret the jwtSecret to set
	 */
	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	/**
	 * @return the checkExportFileAccess
	 */
	public boolean isCheckExportFileAccess() {
		return checkExportFileAccess;
	}

	/**
	 * @param checkExportFileAccess the checkExportFileAccess to set
	 */
	public void setCheckExportFileAccess(boolean checkExportFileAccess) {
		this.checkExportFileAccess = checkExportFileAccess;
	}

	/**
	 * Get the value of workDirectory
	 *
	 * @return the value of workDirectory
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * Set the value of workDirectory
	 *
	 * @param workDirectory new value of workDirectory
	 */
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the showErrors
	 */
	public boolean isShowErrors() {
		return showErrors;
	}

	/**
	 * @param showErrors the showErrors to set
	 */
	public void setShowErrors(boolean showErrors) {
		this.showErrors = showErrors;
	}

	/**
	 * @return the enableDirectParameterSubstitution
	 */
	public boolean isEnableDirectParameterSubstitution() {
		return enableDirectParameterSubstitution;
	}

	/**
	 * @param enableDirectParameterSubstitution the
	 * enableDirectParameterSubstitution to set
	 */
	public void setEnableDirectParameterSubstitution(boolean enableDirectParameterSubstitution) {
		this.enableDirectParameterSubstitution = enableDirectParameterSubstitution;
	}

	/**
	 * @return the exportDirectory
	 */
	public String getExportDirectory() {
		return exportDirectory;
	}

	/**
	 * @param exportDirectory the exportDirectory to set
	 */
	public void setExportDirectory(String exportDirectory) {
		this.exportDirectory = exportDirectory;
	}

	/**
	 * @return the enableGroovySandbox
	 */
	public boolean isEnableGroovySandbox() {
		return enableGroovySandbox;
	}

	/**
	 * @param enableGroovySandbox the enableGroovySandbox to set
	 */
	public void setEnableGroovySandbox(boolean enableGroovySandbox) {
		this.enableGroovySandbox = enableGroovySandbox;
	}

	/**
	 * @return the enableEmailing
	 */
	public boolean isEnableEmailing() {
		return enableEmailing;
	}

	/**
	 * @param enableEmailing the enableEmailing to set
	 */
	public void setEnableEmailing(boolean enableEmailing) {
		this.enableEmailing = enableEmailing;
	}

}

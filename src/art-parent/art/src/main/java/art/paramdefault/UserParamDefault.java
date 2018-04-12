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
package art.paramdefault;

import art.parameter.Parameter;
import art.user.User;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user parameter default value
 * 
 * @author Timothy Anyona
 */
public class UserParamDefault implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String paramDefaultKey;
	private String value;
	private User user;
	private Parameter parameter;

	/**
	 * @return the paramDefaultKey
	 */
	public String getParamDefaultKey() {
		return paramDefaultKey;
	}

	/**
	 * @param paramDefaultKey the paramDefaultKey to set
	 */
	public void setParamDefaultKey(String paramDefaultKey) {
		this.paramDefaultKey = paramDefaultKey;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.paramDefaultKey);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UserParamDefault other = (UserParamDefault) obj;
		if (!Objects.equals(this.paramDefaultKey, other.paramDefaultKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserParamDefault{" + "paramDefaultKey=" + paramDefaultKey + '}';
	}
	
}

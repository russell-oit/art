/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.ruleValue;

import art.rule.Rule;
import art.usergroup.UserGroup;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class to represent user group rule values
 *
 * @author Timothy Anyona
 */
public class UserGroupRuleValue implements Serializable {

	private static final long serialVersionUID = 1L;
	private UserGroup userGroup;
	private Rule rule;
	private String ruleValue;
	private String ruleValueKey;

	/**
	 * Get the value of ruleValueKey
	 *
	 * @return the value of ruleValueKey
	 */
	public String getRuleValueKey() {
		return ruleValueKey;
	}

	/**
	 * Set the value of ruleValueKey
	 *
	 * @param ruleValueKey new value of ruleValueKey
	 */
	public void setRuleValueKey(String ruleValueKey) {
		this.ruleValueKey = ruleValueKey;
	}

	/**
	 * @return the userGroup
	 */
	public UserGroup getUserGroup() {
		return userGroup;
	}

	/**
	 * @param userGroup the userGroup to set
	 */
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	/**
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(Rule rule) {
		this.rule = rule;
	}

	/**
	 * @return the ruleValue
	 */
	public String getRuleValue() {
		return ruleValue;
	}

	/**
	 * @param ruleValue the ruleValue to set
	 */
	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Objects.hashCode(this.ruleValueKey);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UserGroupRuleValue other = (UserGroupRuleValue) obj;
		if (!Objects.equals(this.ruleValueKey, other.ruleValueKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserGroupRuleValue{" + "ruleValueKey=" + ruleValueKey + '}';
	}
}

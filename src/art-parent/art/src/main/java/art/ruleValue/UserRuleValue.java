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
package art.ruleValue;

import art.rule.Rule;
import art.user.User;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class to represent user rule values
 *
 * @author Timothy Anyona
 */
public class UserRuleValue implements Serializable {

	private static final long serialVersionUID = 1L;
	private User user;
	private Rule rule;
	private String ruleValue;
	private String ruleValueKey;

	/**
	 * @return the ruleValueKey
	 */
	public String getRuleValueKey() {
		return ruleValueKey;
	}

	/**
	 * @param ruleValueKey the ruleValueKey to set
	 */
	public void setRuleValueKey(String ruleValueKey) {
		this.ruleValueKey = ruleValueKey;
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
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.ruleValueKey);
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
		final UserRuleValue other = (UserRuleValue) obj;
		if (!Objects.equals(this.ruleValueKey, other.ruleValueKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserRuleValue{" + "ruleValueKey=" + ruleValueKey + '}';
	}
}

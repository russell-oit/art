/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents job types
 *
 * @author Timothy Anyona
 */
public enum JobType {

	EmailAttachment("EmailAttachment"), EmailInline("EmailInline"),
	Alert("Alert"), Publish("Publish"), JustRun("JustRun"),
	CondEmailAttachment("CondEmailAttachment"), CondEmailInline("CondEmailInline"),
	CondPublish("CondPublish"), CacheAppend("CacheAppend"),
	CacheInsert("CacheInsert"), Print("Print"), Burst("Burst");

	private final String value;

	private JobType(String value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns <code>true</code> if this is an email inline or conditional email
	 * inline job type
	 *
	 * @return <code>true</code> if this is an email inline or conditional email
	 * inline job type
	 */
	public boolean isEmailInline() {
		switch (this) {
			case EmailInline:
			case CondEmailInline:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is an email attachment or conditional
	 * email attachment job type
	 *
	 * @return <code>true</code> if this is an email attachment or conditional
	 * email attachment job type
	 */
	public boolean isEmailAttachment() {
		switch (this) {
			case EmailAttachment:
			case CondEmailAttachment:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a cache append or cache insert job
	 * type
	 *
	 * @return <code>true</code> if this is a cache append or cache insert job
	 * type
	 */
	public boolean isCache() {
		switch (this) {
			case CacheAppend:
			case CacheInsert:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a conditional email attachment,
	 * conditional email inline or conditional publish job type
	 *
	 * @return <code>true</code> if this is a conditional email attachment,
	 * conditional email inline or conditional publish job type
	 */
	public boolean isConditional() {
		switch (this) {
			case CondEmailAttachment:
			case CondEmailInline:
			case CondPublish:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is an email attachment, email inline,
	 * conditional email attachment or conditional email inline job type
	 *
	 * @return <code>true</code> if this is an email attachment, email inline,
	 * conditional email attachment or conditional email inline job type
	 */
	public boolean isEmail() {
		switch (this) {
			case EmailAttachment:
			case EmailInline:
			case CondEmailAttachment:
			case CondEmailInline:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a publish or conditional publish job
	 * type
	 *
	 * @return <code>true</code> if this is a publish or conditional publish job
	 * type
	 */
	public boolean isPublish() {
		switch (this) {
			case Publish:
			case CondPublish:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<JobType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it afterwards
		List<JobType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, null is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static JobType toEnum(String value) {
		return toEnum(value, null);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static JobType toEnum(String value, JobType defaultEnum) {
		for (JobType v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns this enum option's i18n message string
	 *
	 * @return this enum option's i18n message string
	 */
	public String getLocalizedDescription() {
		return "jobType.option." + value;
	}
}

/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
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
package art.utils;

import java.util.Date;
import java.util.Locale;
import org.owasp.encoder.Encode;
import org.springframework.context.MessageSource;

/**
 * Provides methods used with the display of config pages that use datatables
 * with ajax
 *
 * @author Timothy Anyona
 */
public class AjaxTableHelper {

	private final String newSpan;
	private final String updatedSpan;
	private final String activeSpan;
	private final String disabledSpan;

	public AjaxTableHelper(MessageSource messageSource, Locale locale) {
		String newText = messageSource.getMessage("page.text.new", null, locale);
		String updatedText = messageSource.getMessage("page.text.updated", null, locale);
		newSpan = "<span class='label label-success'>" + newText + "</span>";
		updatedSpan = "<span class='label label-success'>" + updatedText + "</span>";

		String activeText = messageSource.getMessage("activeStatus.option.active", null, locale);
		String disabledText = messageSource.getMessage("activeStatus.option.disabled", null, locale);
		activeSpan = "<span class='label label-success'>" + activeText + "</span>";
		disabledSpan = "<span class='label label-danger'>" + disabledText + "</span>";
	}

	/**
	 * Returns a record name to display, with an indication of whether the
	 * record is new or recently updated
	 *
	 * @param name the record name
	 * @param creationDate the creation date
	 * @param updateDate the update date
	 * @return the name to display in the datatables list
	 */
	public String processName(String name, Date creationDate, Date updateDate) {
		String encodedName = Encode.forHtml(name);

		final int NEW_OR_UPDATED_LIMIT_DAYS = 7;
		if (ArtUtils.daysUntilToday(creationDate) <= NEW_OR_UPDATED_LIMIT_DAYS) {
			encodedName += " " + newSpan;
		}
		if (ArtUtils.daysUntilToday(updateDate) <= NEW_OR_UPDATED_LIMIT_DAYS) {
			encodedName += " " + updatedSpan;
		}

		return encodedName;
	}

	/**
	 * Returns the active record span html
	 *
	 * @return the active record span html
	 */
	public String getActiveSpan() {
		return activeSpan;
	}

	/**
	 * Returns the disabled record span html
	 *
	 * @return the disabled record span html
	 */
	public String getDisabledSpan() {
		return disabledSpan;
	}

}

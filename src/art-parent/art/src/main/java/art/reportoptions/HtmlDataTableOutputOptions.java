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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Extra options for htmlDataTable report format
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HtmlDataTableOutputOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean pdf;

	/**
	 * @return the pdf
	 */
	public Boolean getPdf() {
		return pdf;
	}

	/**
	 * @param pdf the pdf to set
	 */
	public void setPdf(Boolean pdf) {
		this.pdf = pdf;
	}
}

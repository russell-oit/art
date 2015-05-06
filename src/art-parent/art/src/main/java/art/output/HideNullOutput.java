/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.DisplayNull;
import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

public class HideNullOutput extends TabularOutput {

	private final TabularOutput tabularOutput;

	public HideNullOutput(TabularOutput tabularOutput) {
		super();
		this.tabularOutput = tabularOutput;
	}

	@Override
	public void beginHeader() {
		tabularOutput.beginHeader();
	}

	@Override
	public void addHeaderCell(String value) {
		tabularOutput.addHeaderCell(value);
	}

	@Override
	public void addHeaderCellLeftAligned(String value) {
		tabularOutput.addHeaderCellLeftAligned(value);
	}

	@Override
	public void endHeader() {
		tabularOutput.endHeader();
	}

	@Override
	public void beginRows() {
		tabularOutput.beginRows();
	}

	@Override
	public void addCellString(String value) {
		if (value == null) {
			tabularOutput.addCellString("");
		} else {
			tabularOutput.addCellString(value);
		}
	}

	@Override
	public void addCellNumeric(Double value) {
		if (value == null) {
			if (ArtConfig.getSettings().getDisplayNull() == DisplayNull.NoNumbersAsBlank) {
				tabularOutput.addCellString(""); //display nulls as empty string
			} else {
				tabularOutput.addCellNumeric(0.0D); //display nulls as 0
			}
		} else {
			tabularOutput.addCellNumeric(value);
		}
	}

	@Override
	public void addCellDate(Date value) {
		tabularOutput.addCellDate(value);
	}

	@Override
	public boolean newRow() {
		return tabularOutput.newRow();
	}

	@Override
	public void endRows() {
		tabularOutput.endRows();
	}

}

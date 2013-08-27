/**
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

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

public class hideNullOutput implements ArtOutputInterface {

	private final ArtOutputInterface artOutputInterface;

	@Override
	public String getName() {
		return artOutputInterface.getName();
	}

	@Override
	public String getContentType() {
		return artOutputInterface.getContentType();
	}

	@Override
	public void setWriter(PrintWriter o) {
		artOutputInterface.setWriter(o);
	}

	@Override
	public void setQueryName(String s) {
		artOutputInterface.setQueryName(s);
	}

	@Override
	public void setFileUserName(String s) {
		artOutputInterface.setFileUserName(s);
	}

	@Override
	public void setMaxRows(int i) {
		artOutputInterface.setMaxRows(i);
	}

	@Override
	public void setColumnsNumber(int i) {
		artOutputInterface.setColumnsNumber(i);
	}

	@Override
	public void setExportPath(String s) {
		artOutputInterface.setExportPath(s);
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> params) {
		artOutputInterface.setDisplayParameters(params);
	}

	@Override
	public void beginHeader() {
		artOutputInterface.beginHeader();
	}

	@Override
	public void addHeaderCell(String s) {
		artOutputInterface.addHeaderCell(s);
	}
	
	@Override
	public void addHeaderCellLeft(String s) {
		artOutputInterface.addHeaderCellLeft(s);
	}

	@Override
	public void endHeader() {
		artOutputInterface.endHeader();
	}

	@Override
	public void beginLines() {
		artOutputInterface.beginLines();
	}

	@Override
	public void addCellString(String s) {
		if (s == null) {
			artOutputInterface.addCellString(" ");
		} else {
			artOutputInterface.addCellString(s);
		}
	}

	@Override
	public void addCellDouble(Double d) {
		if (d == null) {
			if (ArtConfig.isNullNumbersAsBlank()) {
				artOutputInterface.addCellString(" "); //display nulls as blank space
			} else {
				artOutputInterface.addCellDouble(0.0D); //display nulls as 0
			}
		} else {
			artOutputInterface.addCellDouble(d);
		}
	}

	@Override
	public void addCellLong(Long i) {
		if (i == null) {
			if (ArtConfig.isNullNumbersAsBlank()) {
				artOutputInterface.addCellString(" "); //display nulls as blank space
			} else {
				artOutputInterface.addCellLong(0L); //display nulls as 0
			}
		} else {
			artOutputInterface.addCellLong(i);
		}
	}

	@Override
	public void addCellDate(Date d) {
		artOutputInterface.addCellDate(d);
	}

	@Override
	public boolean newLine() {
		return artOutputInterface.newLine();
	}

	@Override
	public void endLines() {
		artOutputInterface.endLines();
	}

	@Override
	public String getFileName() {
		return artOutputInterface.getFileName();
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return artOutputInterface.isShowQueryHeaderAndFooter();
	}

	public hideNullOutput(ArtOutputInterface artOutputInterface) {
		super();
		this.artOutputInterface = artOutputInterface;
	}
}

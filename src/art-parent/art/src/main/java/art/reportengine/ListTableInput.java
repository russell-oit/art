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
package art.reportengine;

import java.util.ArrayList;
import java.util.List;
import net.sf.reportengine.in.AbstractTableInput;
import net.sf.reportengine.in.ColumnMetadata;
import net.sf.reportengine.in.ColumnMetadataHolder;

/**
 * ReportEngine input implementation for list data
 *
 * @author Timothy Anyona
 */
public class ListTableInput extends AbstractTableInput implements ColumnMetadataHolder {
	//https://github.com/humbletrader/katechaki/blob/master/src/main/java/net/sf/reportengine/in/TextTableInput.java
	//https://github.com/humbletrader/katechaki/blob/master/src/main/java/net/sf/reportengine/in/JdbcResultsetTableInput.java

	private final List<ColumnMetadata> columnMetadata = new ArrayList<>();
	private final List<List<Object>> data;
	private final int rowCount;
	private int currentRow;

	public ListTableInput(List<List<Object>> data, List<String> columnNames) {
		this.data = data;
		rowCount = data.size();
		for (String columnName : columnNames) {
			columnMetadata.add(new ColumnMetadata(columnName, columnName));
		}
	}

	@Override
	public List<Object> next() {
		List<Object> result = null;
		if (hasNext()) {
			result = data.get(currentRow++);
		}
		return result;
	}

	@Override
	public boolean hasNext() {
		if (currentRow < rowCount) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<ColumnMetadata> getColumnMetadata() {
		return columnMetadata;
	}

}

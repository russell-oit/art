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
package art.datatables;

import art.dbutils.DbService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Timothy Anyona
 */
public class DataTablesUtils {
	//https://github.com/DataTables/DataTablesSrc/blob/master/examples/server_side/scripts/ssp.class.php

	public static String orderClause(DataTablesRequest request,
			List<String> dbColumnNames, String primaryKey) {

		Objects.requireNonNull(request, "request must not be null");
		Objects.requireNonNull(dbColumnNames, "dbColumnNames must not be null");
		Objects.requireNonNull(primaryKey, "primaryKey must not be null");

		List<String> orderList = new ArrayList<>();
		for (Order order : request.getOrder()) {
			int index = order.getColumn();
			String direction = order.getDir();
			String dbColumnName = dbColumnNames.get(index);
			if (StringUtils.isNotBlank(dbColumnName)) {
				String orderColumn = dbColumnName;
				if (StringUtils.equals(direction, "desc")) {
					orderColumn += " DESC";
				}
				orderList.add(orderColumn);
			}
		}

		orderList.add(primaryKey);

		String orderClause = "ORDER BY " + StringUtils.join(orderList, ", ");
		return orderClause;
	}

	public static WhereClauseResult whereClause(DataTablesRequest request,
			List<String> dbColumnNames) {

		Objects.requireNonNull(request, "request must not be null");
		Objects.requireNonNull(dbColumnNames, "dbColumnNames must not be null");

		List<Object> valuesList = new ArrayList<>();

		List<String> globalSearchList = new ArrayList<>();
		List<Column> requestColumns = request.getColumns();
		String globalSearchValue = request.getSearch().getValue();
		if (StringUtils.isNotBlank(globalSearchValue)) {
			//https://stackoverflow.com/questions/8247970/using-like-wildcard-in-prepared-statement
			//https://www.brentozar.com/archive/2010/06/sargable-why-string-is-slow/
			String finalGlobalSearchValue = "%" + globalSearchValue + "%";
			for (int i = 0; i < requestColumns.size(); i++) {
				Column column = requestColumns.get(i);
				String dbColumnName = dbColumnNames.get(i);
				if (column.isSearchable() && StringUtils.isNotBlank(dbColumnName)) {
					String criteria = dbColumnName + " LIKE ?";
					globalSearchList.add(criteria);
					valuesList.add(finalGlobalSearchValue);
				}
			}
		}

		List<String> columnSearchList = new ArrayList<>();
		for (int i = 0; i < requestColumns.size(); i++) {
			Column column = requestColumns.get(i);
			String dbColumnName = dbColumnNames.get(i);
			String columnSearchValue = column.getSearch().getValue();
			if (column.isSearchable() && StringUtils.isNotBlank(dbColumnName)
					&& StringUtils.isNotBlank(columnSearchValue)) {
				String finalColumnSearchValue = "%" + columnSearchValue + "%";
				String criteria = dbColumnName + " LIKE ?";
				columnSearchList.add(criteria);
				valuesList.add(finalColumnSearchValue);
			}
		}

		String where = "";
		if (!globalSearchList.isEmpty()) {
			String globalSearch = StringUtils.join(globalSearchList, " OR ");
			where = "(" + globalSearch + ")";
		}

		if (!columnSearchList.isEmpty()) {
			String columnSearch = StringUtils.join(columnSearchList, " AND ");
			if (globalSearchList.isEmpty()) {
				where = columnSearch;
			} else {
				where += " AND " + columnSearch;
			}
		}

		if (StringUtils.isNotBlank(where)) {
			where = "WHERE " + where;
		}

		Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

		WhereClauseResult result = new WhereClauseResult();
		result.setWhereClause(where);
		result.setValues(valuesArray);

		return result;
	}

	public static WhereClauseResult count(DataTablesRequest request,
			List<String> dbColumnNames, String from,
			DataTablesResponse<?> response) throws SQLException {

		String sql;
		String selectCount = "SELECT COUNT(*)";

		DbService dbService = new DbService();

		sql = StringUtils.joinWith(" ", selectCount, from);
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number totalCountNumber = dbService.query(sql, h);
		long totalCount;
		if (totalCountNumber == null) {
			totalCount = 0;
		} else {
			totalCount = totalCountNumber.longValue();
		}
		response.setRecordsTotal(totalCount);

		WhereClauseResult whereClauseResult = DataTablesUtils.whereClause(request, dbColumnNames);
		String where = whereClauseResult.getWhereClause();
		Object[] values = whereClauseResult.getValues();

		sql = StringUtils.joinWith(" ", selectCount, from, where);
		ResultSetHandler<Number> h2 = new ScalarHandler<>();
		Number filteredCountNumber = dbService.query(sql, h2, values);
		long filteredCount;
		if (filteredCountNumber == null) {
			filteredCount = 0;
		} else {
			filteredCount = filteredCountNumber.longValue();
		}
		response.setRecordsFiltered(filteredCount);
		
		return whereClauseResult;
	}

}

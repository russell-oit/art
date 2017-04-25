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
package art.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates json output
 *
 * @author Timothy Anyona
 */
public class JsonOutput {

	private static final Logger logger = LoggerFactory.getLogger(JsonOutput.class);

	private boolean prettyPrint;

	/**
	 * @return the prettyPrint
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * @param prettyPrint the prettyPrint to set
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Returns resultset data in json representation. Result in the jsonString
	 * property is like: [{"ID":"1","NAME":"Tom","AGE":"24"},
	 * {"ID":"2","NAME":"Bob","AGE":"26"}]
	 *
	 * @param rs the resultset containing the data
	 * @return an object containing the json string representation of the data
	 * and the number of rows in the resultset
	 * @throws SQLException
	 * @throws JsonProcessingException
	 */
	public JsonOutputResult generateOutput(ResultSet rs) throws SQLException, JsonProcessingException {
		logger.debug("Entering generateOutput");

		Objects.requireNonNull(rs, "rs must not be null");

		JsonOutputResult result = new JsonOutputResult();

		//https://stackoverflow.com/questions/18960446/how-to-convert-a-java-resultset-into-json
		List<Map<String, Object>> rows = new ArrayList<>();
		List<ResultSetColumn> columns = new ArrayList<>();

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			ResultSetColumn column = new ResultSetColumn();

			String columnName = rsmd.getColumnLabel(i);
			column.setName(columnName);

			int sqlType = rsmd.getColumnType(i);

			String columnType;

			switch (sqlType) {
				case Types.NUMERIC:
				case Types.DECIMAL:
				case Types.FLOAT:
				case Types.REAL:
				case Types.DOUBLE:
				case Types.INTEGER:
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.BIGINT:
					columnType = "numeric";
					break;
				case Types.DATE:
					columnType = "date";
					break;
				case Types.TIME:
					columnType = "time";
					break;
				case Types.TIMESTAMP:
					columnType = "timestamp";
					break;
				default:
					columnType = "unhandled";
			}

			column.setType(columnType);

			columns.add(column);
		}

		result.setColumns(columns);

		int rowCount = 0;
		while (rs.next()) {
			rowCount++;
			Map<String, Object> row = new HashMap<>();
			for (int i = 1; i <= columnCount; ++i) {
				String columnName = rsmd.getColumnLabel(i);
				Object columnData = rs.getObject(i);;
				row.put(columnName, columnData);
			}
			rows.add(row);
		}

		ObjectMapper mapper = new ObjectMapper();
		//https://egkatzioura.wordpress.com/2013/01/22/spring-jackson-and-date-serialization/
		//http://wiki.fasterxml.com/JacksonFAQDateHandling
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		mapper.setDateFormat(df);
		String jsonString;
		if (prettyPrint) {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			jsonString = mapper.writeValueAsString(rows);
		} else {
			jsonString = mapper.writeValueAsString(rows);
		}

		result.setJsonData(jsonString);
		result.setRowCount(rowCount);

		return result;

	}

}

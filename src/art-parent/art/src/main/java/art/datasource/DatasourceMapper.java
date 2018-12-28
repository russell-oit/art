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
package art.datasource;

import art.enums.DatasourceType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps resultset to a Datasource object. Use public class in its own file for
 * reuse by DatasourceService and DbConnections
 *
 * @author Timothy Anyona
 */
public class DatasourceMapper extends BasicRowProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DatasourceMapper.class);

	@Override
	public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
		List<T> list = new ArrayList<>();
		while (rs.next()) {
			list.add(toBean(rs, type));
		}
		return list;
	}

	@Override
	public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
		Datasource datasource = new Datasource();

		datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
		datasource.setName(rs.getString("NAME"));
		datasource.setDescription(rs.getString("DESCRIPTION"));
		datasource.setDatasourceType(DatasourceType.toEnum(rs.getString("DATASOURCE_TYPE")));
		datasource.setJndi(rs.getBoolean("JNDI"));
		datasource.setDriver(rs.getString("DRIVER"));
		datasource.setUrl(rs.getString("URL"));
		datasource.setUsername(rs.getString("USERNAME"));
		datasource.setPassword(rs.getString("PASSWORD"));
		datasource.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
		datasource.setConnectionPoolTimeoutMins(rs.getInt("POOL_TIMEOUT"));
		datasource.setTestSql(rs.getString("TEST_SQL"));
		datasource.setActive(rs.getBoolean("ACTIVE"));
		datasource.setOptions(rs.getString("DATASOURCE_OPTIONS"));
		datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
		datasource.setCreatedBy(rs.getString("CREATED_BY"));
		datasource.setUpdatedBy(rs.getString("UPDATED_BY"));

		try {
			datasource.decryptPassword();
		} catch (Exception ex) {
			logger.error("Error. {}", datasource, ex);
		}

		return type.cast(datasource);
	}
}

/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.datasource;

import art.encryption.AesEncryptor;
import art.encryption.DesEncryptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.lang3.StringUtils;

/**
 * Maps resultset to a Datasource object. Use public class in its own file for
 * reuse by DatasourceService and DbConnections
 *
 * @author Timothy Anyona
 */
public class DatasourceMapper extends BasicRowProcessor {

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
		datasource.setJndi(rs.getBoolean("JNDI"));
		datasource.setDriver(rs.getString("DRIVER"));
		datasource.setUrl(rs.getString("URL"));
		datasource.setUsername(rs.getString("USERNAME"));
		datasource.setPassword(rs.getString("PASSWORD"));
		datasource.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
		datasource.setConnectionPoolTimeoutMins(rs.getInt("POOL_TIMEOUT"));
		datasource.setTestSql(rs.getString("TEST_SQL"));
		datasource.setActive(rs.getBoolean("ACTIVE"));
		datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
		datasource.setCreatedBy(rs.getString("CREATED_BY"));
		datasource.setUpdatedBy(rs.getString("UPDATED_BY"));

		//decrypt password
		String password = datasource.getPassword();
		String passwordAlgorithm = datasource.getPasswordAlgorithm();
		if (StringUtils.equalsIgnoreCase(passwordAlgorithm, "art")) {
			if (StringUtils.startsWith(password, "o:")) {
				password = DesEncryptor.decrypt(password.substring(2));
				datasource.setPassword(password);
			}
		} else if (StringUtils.equalsIgnoreCase(passwordAlgorithm, "aes")) {
			password = AesEncryptor.decrypt(password);
			datasource.setPassword(password);
		}

		return type.cast(datasource);
	}
}

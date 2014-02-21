/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;

/**
 *
 * @author Timothy Anyona
 */
public class DatasourceMapper extends BasicRowProcessor {

	@Override
	public List<Datasource> toBeanList(ResultSet rs, Class type) throws SQLException {
		List<Datasource> newlist = new ArrayList<Datasource>();
		while (rs.next()) {
			newlist.add(toBean(rs, type));
		}
		return newlist;
	}

	@Override
	public Datasource toBean(ResultSet rs, Class type) throws SQLException {
		Datasource datasource = new Datasource();

		datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
		datasource.setName(rs.getString("NAME"));
		datasource.setDescription(rs.getString("DESCRIPTION"));
		datasource.setDriver(rs.getString("DRIVER"));
		datasource.setUrl(rs.getString("URL"));
		datasource.setUsername(rs.getString("USERNAME"));
		datasource.setPassword(rs.getString("PASSWORD"));
		datasource.setConnectionPoolTimeout(rs.getInt("POOL_TIMEOUT"));
		datasource.setTestSql(rs.getString("TEST_SQL"));
		datasource.setActive(rs.getBoolean("ACTIVE"));
		datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

		return datasource;
	}

}

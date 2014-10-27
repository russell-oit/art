/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.output;

import art.dbutils.ArtDbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.beanutils.DynaBean;

/**
 * Executes queries in jxls speadsheets
 *
 * @author Timothy Anyona
 */
public class JxlsReportManager {

	Connection conn;

	/**
	 *
	 * @param conn
	 */
	public JxlsReportManager(Connection conn) {
		this.conn = conn;
	}

	public List<DynaBean> exec(String sql, Object... params) throws SQLException {
		List<DynaBean> rows = null;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			rs = ArtDbUtils.query(conn, ps, sql, params);
			RowSetDynaClass rsdc = new RowSetDynaClass(rs, false, true); //use lowercase properties = false, use column labels =true
			rows = rsdc.getRows();
		} finally {
			ArtDbUtils.close(rs, ps);
		}

		return rows;
	}
}

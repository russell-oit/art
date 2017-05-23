/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.saiku;

import art.report.Report;
import art.report.ReportService;
import art.saiku.web.rest.objects.resultset.QueryResult;
import art.saiku.web.rest.util.RestUtil;
import art.servlets.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.olap.query2.ThinQuery;
import org.saiku.service.olap.OlapDiscoverService;
import org.saiku.service.olap.ThinQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/rest/saiku/api/query")
public class QueryController {

	@PostMapping("/{queryName}")
	public ThinQuery createQuery(HttpServletRequest request) throws SQLException, IOException {
		ThinQuery query = new ThinQuery();
		query.setType(ThinQuery.Type.MDX);
		query.setMdx("select from Sales");

		ReportService reportService = new ReportService();
		Report report = reportService.getReport(366);
		String source = report.getReportSource();
		ObjectMapper mapper = new ObjectMapper();
		ThinQuery query2 = mapper.readValue(source, ThinQuery.class);
		return query2;
	}

	@PostMapping("/execute")
	public QueryResult execute(@RequestBody ThinQuery tq) throws Exception {
		OlapDiscoverService olapDiscoverService = new OlapDiscoverService();
		OlapMetaExplorer metaExplorer = Config.getSaikuMetaExplorer();
		olapDiscoverService.setMetaExplorer(metaExplorer);

		ThinQueryService thinQueryService = new ThinQueryService();
		thinQueryService.setOlapDiscoverService(olapDiscoverService);

		if (thinQueryService.isMdxDrillthrough(tq)) {
			Long start = (new Date()).getTime();
			ResultSet rs = thinQueryService.drillthrough(tq);
			QueryResult rsc = RestUtil.convert(rs);
			rsc.setQuery(tq);
			Long runtime = (new Date()).getTime() - start;
			rsc.setRuntime(runtime.intValue());
			return rsc;
		}

		QueryResult qr = RestUtil.convert(thinQueryService.execute(tq));
		ThinQuery tqAfter = thinQueryService.getContext(tq.getName()).getOlapQuery();
		qr.setQuery(tqAfter);
		return qr;
	}

}

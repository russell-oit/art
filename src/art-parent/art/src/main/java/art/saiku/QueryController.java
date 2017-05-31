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
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.saiku.olap.dto.SimpleCubeElement;
import org.saiku.olap.query2.ThinQuery;
import org.saiku.olap.util.SaikuProperties;
import org.saiku.service.olap.ThinQueryService;
import org.saiku.service.util.exception.SaikuServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/rest/saiku/api/query")
public class QueryController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private DiscoverHelper discoverHelper;

	@PostMapping("/{newQueryName}")
	public ThinQuery createQuery(@PathVariable("newQueryName") String newQueryName,
			HttpServletRequest request, HttpSession session,
			@RequestParam(value = "file", required = false) Integer reportId,
			@RequestParam(value = "json", required = false) String json)
			throws SQLException, IOException {

		//http://www.logicbig.com/tutorials/spring-framework/spring-web-mvc/spring-path-variable/
		String source;
		if (reportId != null) {
			Report report = reportService.getReport(reportId);
			source = report.getReportSource();
		} else if (json != null) {
			source = json;
		} else {
			throw new SaikuServiceException("Cannot create new query. json and file parameters both null.");
		}

		ObjectMapper mapper = new ObjectMapper();
		ThinQuery tq1 = mapper.readValue(source, ThinQuery.class);

		tq1.setName(newQueryName);

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);
		ThinQuery tq2 = thinQueryService.createQuery(tq1);

		return tq2;
	}

	@PostMapping("/execute")
	public QueryResult execute(@RequestBody ThinQuery tq, HttpSession session) throws Exception {
		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

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

	@DeleteMapping("/{queryname}/cancel")
	public void cancel(HttpSession session,
			@PathVariable("queryname") String queryName) throws SQLException {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);
		thinQueryService.cancel(queryName);
	}

	@PostMapping("/enrich")
	public ThinQuery enrich(ThinQuery tq, HttpSession session) throws Exception {
		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);
		ThinQuery updatedQuery = thinQueryService.updateQuery(tq);
		return updatedQuery;
	}

	@GetMapping("/{queryname}/result/metadata/hierarchies/{hierarchy}/levels/{level}")
	public List<SimpleCubeElement> getLevelMembers(HttpSession session,
			@PathVariable("queryname") String queryName,
			@PathVariable("hierarchy") String hierarchyName,
			@PathVariable("level") String levelName,
			@RequestParam(value = "result", defaultValue = "true") Boolean result,
			@PathVariable("search") String searchString,
			@RequestParam(value = "searchLimit", defaultValue = "-1") Integer searchLimit) {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);
		List<SimpleCubeElement> members = thinQueryService.getResultMetadataMembers(queryName, result, hierarchyName, levelName, searchString, searchLimit);
		return members;
	}

	@GetMapping("/{queryname}/export/xls")
	public ResponseEntity getQueryExcelExport(HttpSession session, HttpServletResponse response,
			@PathVariable("queryname") String queryName) throws IOException {

		String format = "flattened";
		String name = null;
		return generateExcelFile(session, response, queryName, format, name);
	}

	@GetMapping("/{queryname}/export/xls/{format}")
	public ResponseEntity getQueryExcelExportFormat(HttpSession session, HttpServletResponse response,
			@PathVariable("queryname") String queryName,
			@PathVariable("format") String format,
			@RequestParam(value = "exportname", defaultValue = "") String name) throws IOException {

		return generateExcelFile(session, response, queryName, format, name);
	}

	private ResponseEntity generateExcelFile(HttpSession session, HttpServletResponse response,
			String queryName, String format, String name) throws IOException {

		//http://www.n-k.de/2016/05/optional-path-variables-with-spring-boot-rest-mvc.html
		//https://opensourceforgeeks.blogspot.co.ke/2016/01/making-pathvariable-optional-in-spring.html
		//https://stackoverflow.com/questions/17821731/spring-mvc-how-to-indicate-whether-a-path-variable-is-required-or-not
		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		byte[] doc = thinQueryService.getExport(queryName, "xls", format);
		if (name == null || name.equals("")) {
			name = SaikuProperties.webExportExcelName + "." + SaikuProperties.webExportExcelFormat;
		}

		String cleanName = ArtUtils.cleanBaseFilename(name);
		String extension = SaikuProperties.webExportExcelFormat;
		String finalName = cleanName + "." + extension;
		String contentType;

		if (StringUtils.equalsIgnoreCase(extension, "xls")) {
			contentType = "application/vnd.ms-excel";
		} else if (StringUtils.equalsIgnoreCase(extension, "xlsx")) {
			contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else {
			throw new IllegalArgumentException("Unexpected excel file extension: " + extension);
		}

		//https://stackoverflow.com/questions/42767079/download-file-java-spring-rest-api/42768292
		//https://stackoverflow.com/questions/18634337/how-to-set-filename-containing-spaces-in-content-disposition-header
		//https://stackoverflow.com/questions/38141641/how-to-download-excel-file-by-using-spring-response
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
				.contentLength(doc.length)
				.contentType(MediaType.parseMediaType(contentType))
				.body(doc);

	}

}

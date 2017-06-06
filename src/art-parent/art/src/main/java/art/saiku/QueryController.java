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
import art.saiku.web.export.JSConverter;
import art.saiku.web.export.PdfReport;
import art.saiku.web.rest.objects.resultset.QueryResult;
import art.saiku.web.rest.util.RestUtil;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.saiku.olap.dto.SimpleCubeElement;
import org.saiku.olap.dto.resultset.CellDataSet;
import org.saiku.olap.query2.ThinQuery;
import org.saiku.olap.util.SaikuProperties;
import org.saiku.service.olap.ThinQueryService;
import org.saiku.service.olap.drillthrough.DrillThroughResult;
import org.saiku.service.util.exception.SaikuServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/saiku2/api/query")
public class QueryController {

	private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

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
	public ResponseEntity getQueryExcelExport(HttpSession session,
			@PathVariable("queryname") String queryName) throws IOException {

		String format = "flattened";
		String name = null;
		return generateExcelFile(session, queryName, format, name);
	}

	@GetMapping("/{queryname}/export/xls/{format}")
	public ResponseEntity getQueryExcelExportFormat(HttpSession session,
			@PathVariable("queryname") String queryName,
			@PathVariable("format") String format,
			@RequestParam(value = "exportname", defaultValue = "") String name) throws IOException {

		return generateExcelFile(session, queryName, format, name);
	}

	private ResponseEntity generateExcelFile(HttpSession session,
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

		//https://stackoverflow.com/questions/42767079/download-file-java-spring-rest-api/42768292
		//https://stackoverflow.com/questions/18634337/how-to-set-filename-containing-spaces-in-content-disposition-header
		//https://stackoverflow.com/questions/38141641/how-to-download-excel-file-by-using-spring-response
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
				.contentLength(doc.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(doc);
	}

	@GetMapping("/{queryname}/export/csv")
	public ResponseEntity getQueryCsvExport(HttpSession session,
			@PathVariable("queryname") String queryName) throws IOException {

		String format = "flattened";
		String name = null;
		return generateCsvFile(session, queryName, format, name);
	}

	@GetMapping("/{queryname}/export/csv/{format}")
	public ResponseEntity getQueryCsvExportFormat(HttpSession session,
			@PathVariable("queryname") String queryName,
			@PathVariable("format") String format,
			@RequestParam(value = "exportname", defaultValue = "") String name) throws IOException {

		return generateCsvFile(session, queryName, format, name);
	}

	private ResponseEntity generateCsvFile(HttpSession session,
			String queryName, String format, String name) {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		byte[] doc = thinQueryService.getExport(queryName, "csv", format);
		if (name == null || name.equals("")) {
			name = SaikuProperties.webExportCsvName;
		}

		String cleanName = ArtUtils.cleanBaseFilename(name);
		String finalName = cleanName + ".csv";

		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
				.contentLength(doc.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(doc);
	}

	@PostMapping("/{queryname}/zoomin")
	public ThinQuery zoomIn(HttpSession session,
			@PathVariable("queryname") String queryName,
			@RequestParam("selections") String positionListString) throws IOException {

		List<List<Integer>> realPositions = new ArrayList<>();
		if (StringUtils.isNotBlank(positionListString)) {
			ObjectMapper mapper = new ObjectMapper();
			String[] positions = mapper.readValue(positionListString,
					mapper.getTypeFactory().constructArrayType(String.class));
			if (positions != null && positions.length > 0) {
				for (String position : positions) {
					String[] rPos = position.split(":");
					List<Integer> cellPosition = new ArrayList<>();

					for (String p : rPos) {
						Integer pInt = Integer.parseInt(p);
						cellPosition.add(pInt);
					}
					realPositions.add(cellPosition);
				}
			}
		}

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		ThinQuery query = thinQueryService.zoomIn(queryName, realPositions);
		return query;
	}

	@GetMapping("/{queryname}/drillthrough")
	public QueryResult drillthrough(HttpSession session,
			@PathVariable("queryname") String queryName,
			@RequestParam(value = "maxrows", defaultValue = "100") Integer maxrows,
			@RequestParam("position") String position,
			@RequestParam("returns") String returns) throws Exception {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		QueryResult rsc;
		ResultSet rs = null;

		try {
			Long start = (new Date()).getTime();
			if (position == null) {
				rs = thinQueryService.drillthrough(queryName, maxrows, returns);
				rsc = RestUtil.convert(rs);
			} else {
				String[] positions = position.split(":");
				List<Integer> cellPosition = new ArrayList<>();

				for (String p : positions) {
					Integer pInt = Integer.parseInt(p);
					cellPosition.add(pInt);
				}
				DrillThroughResult drillthrough = thinQueryService.drillthroughWithCaptions(queryName, cellPosition, maxrows, returns);
				rsc = RestUtil.convert(drillthrough);
			}
			Long runtime = (new Date()).getTime() - start;
			rsc.setRuntime(runtime.intValue());
		} finally {
			if (rs != null) {
				Statement statement = null;
				try {
					statement = rs.getStatement();
				} catch (Exception e) {
					throw new SaikuServiceException(e);
				} finally {
					try {
						rs.close();
						if (statement != null) {
							statement.close();
						}
					} catch (Exception ee) {
						logger.error("Error", ee);
					}
				}
			}
		}

		return rsc;
	}

	@GetMapping("/{queryname}/drillthrough/export/csv")
	public ResponseEntity getDrillthroughExport(HttpSession session,
			@PathVariable("queryname") String queryName,
			@RequestParam(value = "maxrows", defaultValue = "100") Integer maxrows,
			@RequestParam("position") String position,
			@RequestParam("returns") String returns) {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		ResultSet rs = null;

		try {
			if (position == null) {
				rs = thinQueryService.drillthrough(queryName, maxrows, returns);
			} else {
				String[] positions = position.split(":");
				List<Integer> cellPosition = new ArrayList<>();

				for (String p : positions) {
					Integer pInt = Integer.parseInt(p);
					cellPosition.add(pInt);
				}

				rs = thinQueryService.drillthrough(queryName, cellPosition, maxrows, returns);
			}
			byte[] doc = thinQueryService.exportResultSetCsv(rs);
			String name = SaikuProperties.webExportCsvName;

			name += "-drillthrough";
			String cleanName = ArtUtils.cleanBaseFilename(name);
			String finalName = cleanName + ".csv";

			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
					.contentLength(doc.length)
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(doc);
		} finally {
			if (rs != null) {
				try {
					Statement statement = rs.getStatement();
					statement.close();
					rs.close();
				} catch (SQLException e) {
					throw new SaikuServiceException(e);
				}
			}
		}
	}

	@PostMapping("/{queryname}/export/pdf")
	public ResponseEntity exportPdfWithChart(HttpSession session,
			@PathVariable("queryname") String queryName) throws Exception {

		String format = null;
		String svg = "";
		String name = null;
		return generatePdfFile(session, queryName, format, svg, name);
	}

	@GetMapping("/{queryname}/export/pdf")
	public ResponseEntity exportPdf(HttpSession session,
			@PathVariable("queryname") String queryName) throws Exception {

		String format = null;
		String svg = null;
		String name = null;
		return generatePdfFile(session, queryName, format, svg, name);
	}

	@GetMapping("/{queryname}/export/pdf/{format}")
	public ResponseEntity exportPdfWithFormat(HttpSession session,
			@PathVariable("queryname") String queryName,
			@PathVariable("format") String format,
			@RequestParam("exportname") String name) throws Exception {

		String svg = null;
		return generatePdfFile(session, queryName, format, svg, name);
	}

	@PostMapping("/{queryname}/export/pdf/{format}")
	public ResponseEntity exportPdfWithChartAndFormat(HttpSession session,
			@PathVariable("queryname") String queryName,
			@PathVariable("format") String format,
			@RequestParam(value = "svg", defaultValue = "") String svg,
			@RequestParam("name") String name) throws Exception {

		return generatePdfFile(session, queryName, format, svg, name);
	}

	private ResponseEntity generatePdfFile(HttpSession session,
			String queryName, String format, String svg, String name) throws Exception {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		CellDataSet cellData = thinQueryService.getFormattedResult(queryName, format);
		QueryResult queryResult = RestUtil.convert(cellData);
		PdfReport pdf = new PdfReport();
		byte[] doc = pdf.createPdf(queryResult, svg);
		if (name == null || name.equals("")) {
			name = "export";
		}

		String cleanName = ArtUtils.cleanBaseFilename(name);
		String finalName = cleanName + ".pdf";

		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
				.contentLength(doc.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(doc);
	}

	@GetMapping("/{queryname}/export/html")
	public ResponseEntity exporQuerytHtml(HttpSession session,
			@PathVariable("queryname") String queryname,
			@PathVariable("format") String format,
			@RequestParam(value = "css", defaultValue = "false") Boolean css,
			@RequestParam(value = "tableonly", defaultValue = "false") Boolean tableonly,
			@RequestParam(value = "wrapcontent", defaultValue = "true") Boolean wrapcontent) throws IOException {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		ThinQuery tq = thinQueryService.getContext(queryname).getOlapQuery();
		return generateHtml(session, tq, format, css, tableonly, wrapcontent);
	}

	@PostMapping("/export/html")
	public ResponseEntity exportHtml(HttpSession session,
			@RequestBody ThinQuery tq,
			@PathVariable("format") String format,
			@RequestParam(value = "css", defaultValue = "false") Boolean css,
			@RequestParam(value = "tableonly", defaultValue = "false") Boolean tableonly,
			@RequestParam(value = "wrapcontent", defaultValue = "true") Boolean wrapcontent) throws IOException {

		return generateHtml(session, tq, format, css, tableonly, wrapcontent);
	}

	private ResponseEntity generateHtml(HttpSession session,
			ThinQuery tq, String format, Boolean css, Boolean tableonly, Boolean wrapcontent) throws IOException {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		CellDataSet cs;
		if (org.apache.commons.lang.StringUtils.isNotBlank(format)) {
			cs = thinQueryService.execute(tq, format);
		} else {
			cs = thinQueryService.execute(tq);
		}
		QueryResult qr = RestUtil.convert(cs);
		String content = JSConverter.convertToHtml(qr, wrapcontent);
		String html = "";
		if (!tableonly) {
			html += "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n";
			if (css) {
				html += "<style>\n";
				InputStream is = JSConverter.class.getResourceAsStream("saiku.table.full.css");
				String cssContent = IOUtils.toString(is);
				html += cssContent;
				html += "</style>\n";
			}
			html += "</head>\n<body><div class='workspace_results'>\n";
		}
		html += content;
		if (!tableonly) {
			html += "\n</div></body></html>";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_HTML)
				.body(html);
	}

	@PostMapping("/{queryname}/drillacross")
	public ThinQuery drillacross(HttpSession session,
			@PathVariable("queryname") String queryName,
			@RequestParam("position") String position,
			@RequestParam("drill") String returns) throws IOException {

		ThinQueryService thinQueryService = discoverHelper.getThinQueryService(session);

		String[] positions = position.split(":");
		List<Integer> cellPosition = new ArrayList<>();
		for (String p : positions) {
			Integer pInt = Integer.parseInt(p);
			cellPosition.add(pInt);
		}
		ObjectMapper mapper = new ObjectMapper();

		CollectionType ct
				= mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);

		JavaType st = mapper.getTypeFactory().uncheckedSimpleType(String.class);

		Map<String, List<String>> levels = mapper.readValue(returns, mapper.getTypeFactory().constructMapType(Map.class, st, ct));
		ThinQuery query = thinQueryService.drillacross(queryName, cellPosition, levels);
		return query;
	}
}

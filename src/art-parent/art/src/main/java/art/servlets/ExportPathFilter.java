/**
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
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
package art.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportPathFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ExportPathFilter.class);

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest srequest, ServletResponse sresponse,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) srequest;
		HttpServletResponse response = (HttpServletResponse) sresponse;

		String requestUri = request.getRequestURI();
		File requestPath = new File(requestUri);

		String filename = URLDecoder.decode(requestPath.getName(), "UTF-8");

		//ensure user has access to file. How to cater for old file name formats?
//		String baseName = FilenameUtils.getBaseName(filename);
//		String jobIdString = StringUtils.substringAfterLast(baseName, "-");
//		String toReportId = StringUtils.substringBeforeLast(baseName, "-");
//		String reportIdString = StringUtils.substringAfterLast(toReportId, "-");
//		int reportId = Integer.parseInt(reportIdString);
		if (requestUri.contains("/export/jobs/")) {
			filename = Config.getJobsExportPath() + filename;
		} else {
//			HttpSession session = request.getSession();
//			
//			User sessionUser = (User) session.getAttribute("sessionUser");
//			ReportService reportService = new ReportService();
//			try {
//				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
//					HttpServletResponse response = (HttpServletResponse) arg1;
//					request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
//					return;
//				}
//			} catch (SQLException ex) {
//				logger.error("Error", ex);
//			}

			filename = Config.getReportsExportPath() + filename;
		}
		File file = new File(filename);

		if (!file.exists()) {
			request.setAttribute("message", "reports.message.fileNotFound");
			request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
			return;
		}

		FileInputStream fs = new FileInputStream(file);
		OutputStream os = sresponse.getOutputStream();
		try {
			IOUtils.copyLarge(fs, os);
		} finally {
			IOUtils.closeQuietly(fs);
			try {
				os.flush();
			} catch (IOException ex) {
				logger.debug("Error flushing stream ", ex);
			}
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}

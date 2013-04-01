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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportPathFilter implements Filter {

	final static Logger logger = LoggerFactory.getLogger(ExportPathFilter.class);

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {

		if (ArtDBCP.isCustomExportDirectory()) {
			HttpServletRequest request = (HttpServletRequest) arg0;
			File requestPath = new File(request.getRequestURI());

			String filename = URLDecoder.decode(requestPath.getName(), "UTF-8");
			filename = ArtDBCP.getExportPath() + filename;
			File file = new File(filename);

			FileInputStream fs = new FileInputStream(file);
			OutputStream os = arg1.getOutputStream();
			try {
				IOUtils.copyLarge(fs, os);
			} finally {
				IOUtils.closeQuietly(fs);
				try {
					os.flush();
				} catch (IOException e) {
					logger.debug("Error flushing stream ", e);
				}
			}
		} else {
			arg2.doFilter(arg0, arg1);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}

/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for serving files to http output
 *
 * @author Timothy Anyona
 */
public class ExportHelper {

	private static final Logger logger = LoggerFactory.getLogger(ExportHelper.class);

	/**
	 * Outputs a file to the http response
	 *
	 * @param file the file
	 * @param response the http response
	 */
	public void serveFile(File file, HttpServletResponse response) {
		logger.debug("Entering serveFile");

		// Determine the file's content type
		//https://stackoverflow.com/questions/19711956/alternative-to-files-probecontenttype
		//https://odoepner.wordpress.com/2013/07/29/transparently-improve-java-7-mime-type-recognition-with-apache-tika/
		//https://dzone.com/articles/determining-file-types-java
		//http://www.rgagnon.com/javadetails/java-0487.html
		//https://howtodoinjava.com/spring/spring-mvc/spring-mvc-download-file-controller-example/
		Tika tika = new Tika();
		String filename = file.getName();
		String mimeType = tika.detect(filename);

		response.setContentType(mimeType);
		if (!StringUtils.containsIgnoreCase(mimeType, "html")) {
			//https://stackoverflow.com/questions/18634337/how-to-set-filename-containing-spaces-in-content-disposition-header
			response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		}
		
		//https://stackoverflow.com/questions/35639142/java-download-large-file-stops-unexpectedly
		//https://sourceforge.net/p/art/discussion/352129/thread/9c29eeb09e/?limit=25
		response.setHeader("Content-Length", String.valueOf(file.length()));

		FileInputStream fs = null;
		OutputStream os = null;

		try {
			fs = new FileInputStream(file);
			os = response.getOutputStream();
			IOUtils.copyLarge(fs, os);
		} catch (IOException ex) {
			logger.error("Error", ex);
		} finally {
			IOUtils.closeQuietly(fs);
			try {
				if (os != null) {
					os.flush();
				}
			} catch (IOException ex) {
				logger.debug("Error", ex);
			}
		}
	}

}

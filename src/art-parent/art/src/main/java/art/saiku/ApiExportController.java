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

import art.saiku.web.svg.Converter;
import art.utils.ArtUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpSession;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/api/export")
public class ApiExportController {
	//can't have two controllers with the same name. there is already another ExportController for serving art/export/report and art/export/job

	@PostMapping("/saiku/chart")
	public ResponseEntity<byte[]> exportChart(HttpSession session,
			@RequestParam(value = "type", defaultValue = "png") String type,
			@RequestParam("svg") String svg,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam("name") String name) throws IOException, TranscoderException {

		if (StringUtils.isBlank(svg)) {
			throw new IllegalArgumentException("svg must not be blank");
		}

		final InputStream in = new ByteArrayInputStream(svg.getBytes("UTF-8"));
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.flush();
		Converter converter = Converter.byType(type.toUpperCase());
		if (converter == null) {
			throw new IllegalStateException("No converter available for type: " + type);
		}
		converter.convert(in, out, size);
		byte[] b = out.toByteArray();

		if (name == null || name.equals("")) {
			name = "chart-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date());
		}

		String cleanName = ArtUtils.cleanBaseFilename(name);
		String finalName = cleanName + "." + converter.getExtension();

		//https://stackoverflow.com/questions/5690228/spring-mvc-how-to-return-image-in-responsebody
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=\"" + finalName + "\"")
				.contentLength(b.length)
				.contentType(MediaType.parseMediaType(converter.getContentType()))
				.body(b);
	}

}

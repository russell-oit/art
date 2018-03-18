/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.utils;

import art.general.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/**
 * Provides methods to be used with REST API calls
 * 
 * @author Timothy Anyona
 */
public class ApiHelper {
	
	/**
	 * Outputs an ApiResponse object to the http servlet response
	 * 
	 * @param apiResponse the ApiResponse object
	 * @param response the http servlet response
	 * @throws JsonProcessingException
	 * @throws IOException 
	 */
	public static void outputApiResponse(ApiResponse apiResponse,
			HttpServletResponse response) throws JsonProcessingException, IOException {
		
		String jsonString = ArtUtils.objectToJson(apiResponse);
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		response.getWriter().write(jsonString);
	}
	
}

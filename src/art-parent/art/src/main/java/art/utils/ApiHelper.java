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

import art.enums.ApiStatus;
import art.general.ApiResponse;
import art.servlets.Config;
import java.io.IOException;
import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Provides methods to be used with REST API calls
 *
 * @author Timothy Anyona
 */
public class ApiHelper {
	//https://www.baeldung.com/spring-response-entity

	private static final Logger logger = LoggerFactory.getLogger(ApiHelper.class);

	/**
	 * Outputs an ApiResponse object to the http servlet response
	 *
	 * @param apiResponse the ApiResponse object
	 * @param response the http servlet response
	 */
	public static void outputApiResponse(ApiResponse apiResponse,
			HttpServletResponse response) {

		try {
			String jsonString = ArtUtils.objectToJson(apiResponse);
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.getWriter().write(jsonString);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Outputs an error to the http servlet response
	 *
	 * @param response the http servlet response
	 * @param exception the exception
	 */
	public static void outputErrorResponse(HttpServletResponse response,
			Exception exception) {

		String message = exception.getMessage();
		outputErrorResponse(response, message);
	}

	/**
	 * Outputs an error to the http servlet response
	 *
	 * @param response the http servlet response
	 * @param message the error message
	 */
	public static void outputErrorResponse(HttpServletResponse response,
			String message) {

		try {
			ApiResponse apiResponse = new ApiResponse();
			
			apiResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			apiResponse.setArtStatus(ApiStatus.ERROR);

			if (Config.getCustomSettings().isShowErrorsApi()) {
				apiResponse.setMessage(message);
			}

			String jsonString = ArtUtils.objectToJson(apiResponse);
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.getWriter().write(jsonString);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Outputs a not found response to the http servlet response
	 *
	 * @param response the http servlet response
	 */
	public static void outputNotFoundResponse(HttpServletResponse response) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			
			apiResponse.setHttpStatus(HttpStatus.NOT_FOUND.value());
			apiResponse.setArtStatus(ApiStatus.RECORD_NOT_FOUND);

			String jsonString = ArtUtils.objectToJson(apiResponse);
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setStatus(HttpStatus.NOT_FOUND.value());
			response.getWriter().write(jsonString);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Outputs an OK response to the http servlet response
	 *
	 * @param response the http servlet response
	 * @param data the data to include in the response
	 */
	public static void outputOkResponse(HttpServletResponse response, Object data) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			
			apiResponse.setHttpStatus(HttpStatus.OK.value());
			apiResponse.setArtStatus(ApiStatus.OK);
			apiResponse.setData(data);

			String jsonString = ArtUtils.objectToJson(apiResponse);
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setStatus(HttpStatus.OK.value());
			response.getWriter().write(jsonString);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Returns a response entity object to use with an error response
	 *
	 * @param exception the exception
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getErrorResponseEntity(Exception exception) {
		String message = exception.getMessage();
		return getErrorResponseEntity(message);
	}

	/**
	 * Returns a response entity object to use with an error response
	 *
	 * @param message the error message
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getErrorResponseEntity(String message) {
		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		apiResponse.setArtStatus(ApiStatus.ERROR);

		if (Config.getCustomSettings().isShowErrorsApi()) {
			apiResponse.setMessage(message);
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with an OK response
	 *
	 * @return the response entity object
	 */
	public static ResponseEntity<?> getOkResponseEntity() {
		Object data = null;
		return getOkResponseEntity(data);
	}

	/**
	 * Returns a response entity object to use with an OK response
	 *
	 * @param data the data to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<?> getOkResponseEntity(Object data) {
		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.OK.value());
		apiResponse.setArtStatus(ApiStatus.OK);
		apiResponse.setData(data);
		
		return ResponseEntity.ok(apiResponse);
	}

	/**
	 * Returns a response entity object to use with an UNAUTHORIZED response
	 *
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getUnauthorizedResponseEntity() {
		ApiStatus artStatus = ApiStatus.UNAUTHORIZED;
		String message = null;
		return getUnauthorizedResponseEntity(artStatus, message);
	}

	/**
	 * Returns a response entity object to use with an UNAUTHORIZED response
	 *
	 * @param artStatus the art status to use
	 * @param message the message to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getUnauthorizedResponseEntity(
			ApiStatus artStatus, String message) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.UNAUTHORIZED.value());
		apiResponse.setArtStatus(artStatus);
		apiResponse.setMessage(message);
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with a record not found response
	 *
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getNotFoundResponseEntity() {
		String message = null;
		return getNotFoundResponseEntity(message);
	}

	/**
	 * Returns a response entity object to use with a record not found response
	 *
	 * @param message the message to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getNotFoundResponseEntity(
			String message) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.NOT_FOUND.value());
		apiResponse.setArtStatus(ApiStatus.RECORD_NOT_FOUND);
		apiResponse.setMessage(message);
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with a record not found response
	 *
	 * @param data the data to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getLinkedRecordsExistResponseEntity(
			Object data) {

		String message = null;
		return getLinkedRecordsExistResponseEntity(message, data);
	}

	/**
	 * Returns a response entity object to use with a record not found response
	 *
	 * @param message the message to include in the response
	 * @param data the data to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getLinkedRecordsExistResponseEntity(
			String message, Object data) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.CONFLICT.value());
		apiResponse.setArtStatus(ApiStatus.LINKED_RECORDS_EXIST);
		apiResponse.setMessage(message);
		apiResponse.setData(data);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with a record created response
	 *
	 * @param uri the uri of the new record
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getCreatedResponseEntity(URI uri) {
		Object data = null;
		return getCreatedResponseEntity(uri, data);
	}

	/**
	 * Returns a response entity object to use with a record created response
	 *
	 * @param uri the uri of the new record
	 * @param data the data to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getCreatedResponseEntity(URI uri,
			Object data) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.CREATED.value());
		apiResponse.setArtStatus(ApiStatus.OK);
		apiResponse.setData(data);
		
		return ResponseEntity.created(uri).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with a record exists response
	 *
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getRecordExistsResponseEntity() {
		String message = null;
		return getRecordExistsResponseEntity(message);
	}

	/**
	 * Returns a response entity object to use with a record exists response
	 *
	 * @param message the message to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getRecordExistsResponseEntity(
			String message) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.CONFLICT.value());
		apiResponse.setArtStatus(ApiStatus.RECORD_EXISTS);
		apiResponse.setMessage(message);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
	}

	/**
	 * Returns a response entity object to use with an invalid value response
	 *
	 * @param message the message to include in the response
	 * @return the response entity object
	 */
	public static ResponseEntity<ApiResponse> getInvalidValueResponseEntity(
			String message) {

		ApiResponse apiResponse = new ApiResponse();
		
		apiResponse.setHttpStatus(HttpStatus.BAD_REQUEST.value());
		apiResponse.setArtStatus(ApiStatus.INVALID_VALUE);
		apiResponse.setMessage(message);
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
	}

}

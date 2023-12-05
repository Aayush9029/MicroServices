package com.eecs3311.songmicroservice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class Utils {

	// Used to determine path that was called from within each REST route, you don't
	// need to modify this
	public static String getUrl(HttpServletRequest req) {
		String requestUrl = req.getRequestURL().toString();
		String queryString = req.getQueryString();

		if (queryString != null) {
			requestUrl += "?" + queryString;
		}
		return requestUrl;
	}

	// Sets the response status and data for a response from the server. You might
	// not always be able to use this function
	public static ResponseEntity<Map<String, Object>> setResponseStatus(Map<String, Object> response,
			DbQueryExecResult dbQueryExecResult, Object data) {
		HttpStatus status = HttpStatus.NOT_IMPLEMENTED; // default value of HTTP 501 NOT IMPLEMENTED
		switch (dbQueryExecResult) {
			case QUERY_OK:
				status = HttpStatus.OK;
				if (data != null) {
					response.put("data", data);
				}
				break;
			case QUERY_ERROR_NOT_FOUND:
				status = HttpStatus.NOT_FOUND;
				break;
			case QUERY_ERROR_GENERIC:
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
		}
		response.put("status", status);

		return ResponseEntity.status(status).body(response);
	}

	// print error messages, info, warning and error
	public static void log(String message, LogType type) {
		switch (type) {
			case ERROR:
				System.err.println("❌" + message);
				break;
			case INFO:
				System.out.println("✅" + message);
				break;
			case WARNING:
				System.out.println("⚠️" + message);
				break;
		}
	}

	// Utility method to create a map from DbQueryStatus
	public static Map<String, Object> makeMap(DbQueryStatus dbQueryStatus) {
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("message", dbQueryStatus.getMessage());
		responseMap.put("data", dbQueryStatus.getData());

		// Determine the HTTP status based on the DbQueryExecResult
		HttpStatus status = HttpStatus.NOT_IMPLEMENTED; // default value
		switch (dbQueryStatus.getdbQueryExecResult()) {
			case QUERY_OK:
				status = HttpStatus.OK;
				break;
			case QUERY_ERROR_NOT_FOUND:
				status = HttpStatus.NOT_FOUND;
				break;
			case QUERY_ERROR_GENERIC:
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
		}

		responseMap.put("status", status);
		return responseMap;
	}

}

enum LogType {
	ERROR,
	INFO,
	WARNING
}
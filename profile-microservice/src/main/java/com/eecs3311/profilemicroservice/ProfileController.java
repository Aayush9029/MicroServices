package com.eecs3311.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eecs3311.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";
	public static final String KEY_FRIEND_USER_NAME = "friendUserName";
	public static final String KEY_SONG_ID = "songId";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> addProfile(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String fullName = params.get(KEY_USER_FULLNAME);
		String password = params.get(KEY_USER_PASSWORD);

		DbQueryStatus dbQueryStatus = profileDriver.createUserProfile(userName, fullName, password);
		Map<String, Object> response = new HashMap<String, Object>();

		// if user already exists return 409 which is conflict with proper message
		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("message", "User already exists");
			return Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_GENERIC, null);
		}

		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/followFriend", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> followFriend(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String friendUserName = params.get(KEY_FRIEND_USER_NAME);

		DbQueryStatus dbQueryStatus = profileDriver.followFriend(userName, friendUserName);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getAllFriendFavouriteSongTitles(
			@PathVariable("userName") String userName, HttpServletRequest request) {

		if (isInputEmpty(userName)) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Invalid userName");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		DbQueryStatus dbQueryStatus = profileDriver.getAllSongFriendsLike(userName);
		if (dbQueryStatus == null) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Error retrieving data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/unfollowFriend", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> unfollowFriend(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String friendUserName = params.get(KEY_FRIEND_USER_NAME);

		DbQueryStatus dbQueryStatus = profileDriver.unfollowFriend(userName, friendUserName);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/likeSong", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> likeSong(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String songId = params.get(KEY_SONG_ID);
		Map<String, Object> response = new HashMap<String, Object>();

		if (!checkSongExistsInMongoDB(songId)) {
			Utils.log("Song does not exist in MongoDB", LogType.WARNING);
			response.put("message", "Song does not exist in MongoDB");
			return Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_NOT_FOUND, null);
		}

		DbQueryStatus dbQueryStatus = playlistDriver.likeSong(userName, songId);
		Utils.log("likeSong: " + dbQueryStatus.getdbQueryExecResult(), LogType.INFO);
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/unlikeSong", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> unlikeSong(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String songId = params.get(KEY_SONG_ID);
		Map<String, Object> response = new HashMap<String, Object>();

		DbQueryStatus dbQueryStatus = playlistDriver.unlikeSong(userName, songId);
		Utils.log("unlikeSong: " + dbQueryStatus.getdbQueryExecResult(), LogType.INFO);

		if (!checkSongExistsInMongoDB(songId)) {
			Utils.log("Song does not exist in MongoDB", LogType.WARNING);
			response.put("message", "Song does not exist in MongoDB");
			return Utils.setResponseStatus(response, DbQueryExecResult.QUERY_ERROR_NOT_FOUND, null);
		}

		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	private boolean checkSongExistsInMongoDB(String songId) {
		Utils.log("checkSongExistsInMongoDB " + "songId: " + songId, LogType.INFO);
		String songServiceUrl = "http://localhost:3001/getSongById/" + songId;
		Request request = new Request.Builder().url(songServiceUrl).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				return true;
			}
		} catch (IOException e) {
			Utils.log("Error checking if song exists in MongoDB: " + e.getMessage(), LogType.ERROR);
		}
		return false;
	}

	private Boolean isInputEmpty(String input) {
		return input == null || input.trim().isEmpty();
	}
}
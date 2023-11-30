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

		DbQueryStatus dbQueryStatus = playlistDriver.likeSong(userName, songId);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/unlikeSong", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> unlikeSong(@RequestBody Map<String, String> params,
			HttpServletRequest request) {

		String userName = params.get(KEY_USER_NAME);
		String songId = params.get(KEY_SONG_ID);

		DbQueryStatus dbQueryStatus = playlistDriver.unlikeSong(userName, songId);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	private Boolean isInputEmpty(String input) {
		return input == null || input.trim().isEmpty();
	}
}
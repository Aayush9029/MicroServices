package com.eecs3311.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	/**
	 * This method is partially implemented for you to follow as an example of
	 * how to complete the implementations of methods in the controller classes.
	 * 
	 * @param songId
	 * @param request
	 * @return
	 */
	@GetMapping("/getSongById/{songId}")
	public ResponseEntity<Map<String, Object>> getSongById(@PathVariable String songId, HttpServletRequest request) {
		System.out.println("🧑‍💻 getSongById called with songId: " + songId);
		Map<String, Object> response = new HashMap<>();
		response.put("path", Utils.getUrl(request));

		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@GetMapping("/getSongTitleById/{songId}")
	public ResponseEntity<Map<String, Object>> getSongTitleById(@PathVariable String songId,
			HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();
		response.put("path", Utils.getUrl(request));

		DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId);
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public ResponseEntity<Map<String, Object>> deleteSongById(@PathVariable String songId, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();
		response.put("path", Utils.getUrl(request));

		DbQueryStatus dbQueryStatus = songDal.deleteSongById(songId);
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);
	}

	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> addSong(@RequestBody Song song, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();
		response.put("path", Utils.getUrl(request));

		DbQueryStatus dbQueryStatus = songDal.addSong(song);
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
	}

	@RequestMapping(value = "/updateSongFavouritesCount", method = RequestMethod.PUT)
	public ResponseEntity<Map<String, Object>> updateFavouritesCount(@RequestBody Map<String, String> params,
			HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();
		response.put("path", Utils.getUrl(request));

		String songId = params.get("songId");
		boolean shouldDecrement = Boolean.parseBoolean(params.get("shouldDecrement"));
		DbQueryStatus dbQueryStatus = songDal.updateSongFavouritesCount(songId, shouldDecrement);
		return Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);
	}
}
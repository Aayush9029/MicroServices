package com.eecs3311.profilemicroservice;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import org.neo4j.driver.v1.*;
import okhttp3.*;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	private OkHttpClient client = new OkHttpClient();
	private String songServiceBaseUrl = "http://localhost:3001"; // Base URL of Song microservice

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			} catch (Exception e) {
				if (e.getMessage().contains("An equivalent constraint already exists")) {
					Utils.log(
							"INFO: Playlist constraint already exist (DB likely already initialized), should be OK to continue",
							LogType.WARNING);
					System.out.println(
							"INFO: Playlist constraint already exist (DB likely already initialized), should be OK to continue");
				} else {
					// something else, yuck, bye
					throw e;
				}
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		Utils.log("likeSong " + "userName: " + userName + ", songId: " + songId, LogType.INFO);
		if (!verifySongExists(songId)) {
			Utils.log("MONGODB: Song not found", LogType.WARNING);
			return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}

		try (Session session = driver.session()) {
			try (Transaction transaction = session.beginTransaction()) {
				String checkAlreadyLiked = "MATCH (u:profile {userName: $userName})-[:likes]->(s:song {songId: $songId}) RETURN s";
				if (!transaction.run(checkAlreadyLiked, parameters("userName", userName, "songId", songId)).list()
						.isEmpty()) {
					Utils.log("Song already liked by user", LogType.WARNING);
					return new DbQueryStatus("Song already liked by user", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				String query = "MATCH (u:profile {userName: $userName}), (s:song {songId: $songId}) MERGE (u)-[:likes]->(s)";
				transaction.run(query, parameters("userName", userName, "songId", songId));
				transaction.success();

				updateSongFavouritesCount(songId, true);
				Utils.log("Song liked successfully", LogType.INFO);
				return new DbQueryStatus("Song liked successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			Utils.log("Error liking song: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error liking song: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		Utils.log("unlikeSong " + "userName: " + userName + ", songId: " + songId, LogType.INFO);
		if (!verifySongExists(songId)) {
			Utils.log("MONGODB: Song not found", LogType.WARNING);
			return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}

		try (Session session = driver.session()) {
			try (Transaction transaction = session.beginTransaction()) {
				String checkAlreadyLiked = "MATCH (u:profile {userName: $userName})-[:likes]->(s:song {songId: $songId}) RETURN s";
				if (!(transaction.run(checkAlreadyLiked, parameters("userName", userName, "songId", songId)).list()
						.isEmpty())) {
					Utils.log("Song not liked by user", LogType.WARNING);
					return new DbQueryStatus("Song not liked by user", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}

				String query = "MATCH (u:profile {userName: $userName})-[r:likes]->(s:song {songId: $songId}) DELETE r";
				transaction.run(query, parameters("userName", userName, "songId", songId));
				transaction.success();

				updateSongFavouritesCount(songId, true);
				Utils.log("Song unliked successfully", LogType.INFO);
				return new DbQueryStatus("Song unliked successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			Utils.log("Error unliking song: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error unliking song: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	// Method to verify that a song exists in MongoDB
	private boolean verifySongExists(String songId) {
		Utils.log("checkSongExistsInMongoDB " + "songId: " + songId, LogType.INFO);
		String songServiceUrl = songServiceBaseUrl + "/getSongById/" + songId;
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

	// Method to update the song's like count in the Song microservice
	public void updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		String url = songServiceBaseUrl + "/updateSongFavouritesCount";
		MediaType JSON = MediaType.get("application/json; charset=utf-8");
		JSONObject jsonBody = new JSONObject();
		jsonBody.put("songId", songId);
		jsonBody.put("shouldDecrement", shouldDecrement);

		RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
		Request request = new Request.Builder().url(url).post(body).build();

		try (Response response = client.newCall(request).execute()) {
			// Handle the response
		} catch (IOException e) {
			// Handle exceptions
		}
	}

}

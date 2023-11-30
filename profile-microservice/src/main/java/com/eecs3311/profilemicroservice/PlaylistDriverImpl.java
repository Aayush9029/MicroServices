package com.eecs3311.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import static org.neo4j.driver.v1.Values.parameters;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			} catch (Exception e) {
				if (e.getMessage().contains("An equivalent constraint already exists")) {
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
		try (Session session = driver.session()) {
			try (Transaction transaction = session.beginTransaction()) {
				// Check if the song exists
				String songExistQuery = "MATCH (s:song {songId: $songId}) RETURN s";
				StatementResult songExistResult = transaction.run(songExistQuery, parameters("songId", songId));
				if (!songExistResult.hasNext()) {
					return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}

				// Check if already liked
				String alreadyLikedQuery = "MATCH (p:profile {userName: $userName})-[:created]->(pl:playlist)-[:includes]->(s:song {songId: $songId}) RETURN s";
				StatementResult alreadyLikedResult = transaction.run(alreadyLikedQuery,
						parameters("userName", userName, "songId", songId));
				if (alreadyLikedResult.hasNext()) {
					return new DbQueryStatus("Song already liked", DbQueryExecResult.QUERY_OK);
				}

				// Create relationship
				String likeQuery = "MATCH (p:profile {userName: $userName})-[:created]->(pl:playlist), (s:song {songId: $songId}) MERGE (pl)-[:includes]->(s)";
				transaction.run(likeQuery, parameters("userName", userName, "songId", songId));

				transaction.success();
				return new DbQueryStatus("Song liked successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Internal Error: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {

		try (Session session = driver.session()) {
			try (Transaction transaction = session.beginTransaction()) {
				// Check if the song is liked
				String likedQuery = "MATCH (p:profile {userName: $userName})-[:created]->(pl:playlist)-[r:includes]->(s:song {songId: $songId}) RETURN r";
				StatementResult likedResult = transaction.run(likedQuery,
						parameters("userName", userName, "songId", songId));
				if (!likedResult.hasNext()) {
					return new DbQueryStatus("Song not found in favorites", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}

				// Delete relationship
				String unlikeQuery = "MATCH (p:profile {userName: $userName})-[:created]->(pl:playlist)-[r:includes]->(s:song {songId: $songId}) DELETE r";
				transaction.run(unlikeQuery, parameters("userName", userName, "songId", songId));

				transaction.success();
				return new DbQueryStatus("Song unliked successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Internal Error: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}
}

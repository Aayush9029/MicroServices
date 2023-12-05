package com.eecs3311.profilemicroservice;

import java.util.*;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

import static org.neo4j.driver.v1.Values.parameters;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		Utils.log("Initializing Profile DB", LogType.INFO);

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			} catch (Exception e) {
				if (e.getMessage().contains("An equivalent constraint already exists")) {
					System.out.println(
							"INFO: Profile constraints already exist (DB likely already initialized), should be OK to continue");
				} else {
					Utils.log("ERROR: " + e.getMessage(), LogType.ERROR);
					// something else, yuck, bye
					throw e;
				}
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		Utils.log("Creating user profile", LogType.INFO);
		try (Session session = driver.session()) {
			String query = "CREATE (p:profile {userName: $userName, fullName: $fullName, password: $password})";
			session.run(query, parameters("userName", userName, "fullName", fullName, "password", password));
			Utils.log("Created user profile", LogType.INFO);
			return new DbQueryStatus("User created successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("Error creating user profile: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error creating user: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus followFriend(String userName, String friendUserName) {
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				// Check if the following relationship already exists
				String checkRelationQuery = "MATCH (a:profile {userName: $userName})-[r:follows]->(b:profile {userName: $friendUserName}) RETURN r";
				StatementResult checkResult = tx.run(checkRelationQuery,
						parameters("userName", userName, "friendUserName", friendUserName));

				if (checkResult.hasNext()) {
					tx.success();
					return new DbQueryStatus("Already following friend", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				// Create the follow relationship
				String query = "MATCH (a:profile {userName: $userName}), (b:profile {userName: $friendUserName}) MERGE (a)-[:follows]->(b)";
				tx.run(query, parameters("userName", userName, "friendUserName", friendUserName));
				tx.success();
				return new DbQueryStatus("Followed friend successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error following friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String friendUserName) {
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				// Check if the following relationship exists
				String checkRelationQuery = "MATCH (a:profile {userName: $userName})-[r:follows]->(b:profile {userName: $friendUserName}) RETURN r";
				StatementResult result = tx.run(checkRelationQuery,
						parameters("userName", userName, "friendUserName", friendUserName));

				if (!result.hasNext()) {
					tx.success();
					return new DbQueryStatus("Follow relationship not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}

				// Delete the relationship
				String query = "MATCH (a:profile {userName: $userName})-[r:follows]->(b:profile {userName: $friendUserName}) DELETE r";
				tx.run(query, parameters("userName", userName, "friendUserName", friendUserName));
				tx.success();
				return new DbQueryStatus("Unfollowed friend successfully", DbQueryExecResult.QUERY_OK);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error unfollowing friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		Utils.log("Retrieving all songs friends like", LogType.INFO);
		try (Session session = driver.session()) {
			// The query retrieves song IDs from friends' playlists
			String query = "MATCH (user:profile {userName: $userName})-[:follows]->(friend:profile)-[:likes]->(song:song) "
					+
					"RETURN song.songId AS songId";
			StatementResult result = session.run(query, parameters("userName", userName));

			List<String> songIds = new ArrayList<>();
			while (result.hasNext()) {
				Record record = result.next();
				String songId = record.get("songId").asString();
				songIds.add(songId);
				Utils.log("Retrieved song ID: " + songId, LogType.INFO);
			}

			Utils.log("Retrieved all songs friends like successfully", LogType.INFO);
			return new DbQueryStatus("Songs retrieved successfully", DbQueryExecResult.QUERY_OK, songIds);
		} catch (Exception e) {
			Utils.log("Error retrieving songs: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error retrieving songs: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

}

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
		Utils.log("Following friend", LogType.INFO);
		try (Session session = driver.session()) {
			String query = "MATCH (a:profile {userName: $userName}), (b:profile {userName: $friendUserName}) MERGE (a)-[:follows]->(b)";
			session.run(query, parameters("userName", userName, "friendUserName", friendUserName));
			Utils.log("Followed friend", LogType.INFO);
			return new DbQueryStatus("Followed friend successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("Error following friend: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error following friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String friendUserName) {
		Utils.log("Unfollowing friend", LogType.INFO);
		try (Session session = driver.session()) {
			String query = "MATCH (a:profile {userName: $userName})-[r:follows]->(b:profile {userName: $friendUserName}) DELETE r";
			session.run(query, parameters("userName", userName, "friendUserName", friendUserName));
			Utils.log("Unfollowed friend", LogType.INFO);
			return new DbQueryStatus("Unfollowed friend successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("Error unfollowing friend: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error unfollowing friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		Utils.log("Retrieving all songs friends like", LogType.INFO);
		try (Session session = driver.session()) {
			String query = "MATCH (p:profile {userName: $userName})-[:follows]->(:profile)-[:created]->(:playlist)-[:includes]->(s:song) RETURN s.songId";
			StatementResult result = session.run(query, parameters("userName", userName));

			List<String> songIds = new ArrayList<>();
			while (result.hasNext()) {
				Utils.log("Retrieved song", LogType.INFO);
				songIds.add(result.next().get("s.songId").asString());
			}

			Utils.log("Retrieved all songs friends like", LogType.INFO);

			return new DbQueryStatus("Songs retrieved successfully", DbQueryExecResult.QUERY_OK, songIds);
		} catch (Exception e) {
			Utils.log("Error retrieving songs: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error retrieving songs: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

}

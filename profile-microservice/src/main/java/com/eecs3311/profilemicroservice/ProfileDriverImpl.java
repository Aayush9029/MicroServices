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
					// something else, yuck, bye
					throw e;
				}
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		try (Session session = driver.session()) {
			String query = "CREATE (p:profile {userName: $userName, fullName: $fullName, password: $password})";
			session.run(query, parameters("userName", userName, "fullName", fullName, "password", password));
			return new DbQueryStatus("User created successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			return new DbQueryStatus("Error creating user: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus followFriend(String userName, String friendUserName) {
		try (Session session = driver.session()) {
			String query = "MATCH (a:profile {userName: $userName}), (b:profile {userName: $friendUserName}) MERGE (a)-[:follows]->(b)";
			session.run(query, parameters("userName", userName, "friendUserName", friendUserName));
			return new DbQueryStatus("Followed friend successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			return new DbQueryStatus("Error following friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String friendUserName) {
		try (Session session = driver.session()) {
			String query = "MATCH (a:profile {userName: $userName})-[r:follows]->(b:profile {userName: $friendUserName}) DELETE r";
			session.run(query, parameters("userName", userName, "friendUserName", friendUserName));
			return new DbQueryStatus("Unfollowed friend successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			return new DbQueryStatus("Error unfollowing friend: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		try (Session session = driver.session()) {
			String query = "MATCH (p:profile {userName: $userName})-[:follows]->(:profile)-[:created]->(:playlist)-[:includes]->(s:song) RETURN s.songId";
			StatementResult result = session.run(query, parameters("userName", userName));

			List<String> songIds = new ArrayList<>();
			while (result.hasNext()) {
				songIds.add(result.next().get("s.songId").asString());
			}

			return new DbQueryStatus("Songs retrieved successfully", DbQueryExecResult.QUERY_OK, songIds);
		} catch (Exception e) {
			return new DbQueryStatus("Error retrieving songs: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

}

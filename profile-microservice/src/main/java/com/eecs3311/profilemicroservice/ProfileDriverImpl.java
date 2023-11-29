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
			String node = "CREATE (nProfile:profile {userName: '" + userName + "', fullName: '" + fullName + "', password: '" + password + "'})" ;
			StatementResult sr  = session.run(node);


			}
		catch (Exception e) {
			return new DbQueryStatus("Error creating account: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		return new DbQueryStatus("Profile created", DbQueryExecResult.QUERY_OK);
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {

		try (Session session = driver.session()) {
			String match = "MATCH (p1:profile {userName: '" + userName + "'}), (p2:profile {userName: '" + frndUserName + "'})"  +
					"CREATE (p1)-[:follows]->(p2)" ;
			StatementResult sr  = session.run(match);
		}
		catch (Exception e) {
			return new DbQueryStatus("Error creating account: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		return new DbQueryStatus("Profile created", DbQueryExecResult.QUERY_OK);


	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {

		try (Session session = driver.session()) {
			String match = "MATCH (p1:profile {userName: '" + userName + "'}) -[r:follows]->(p2:profile {userName: '" + frndUserName + "'})"  +
					"DELETE r" ;
			StatementResult sr  = session.run(match);
		}
		catch (Exception e) {
			return new DbQueryStatus("Error creating account: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		return new DbQueryStatus("Profile created", DbQueryExecResult.QUERY_OK);

	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		return null;
	}
}

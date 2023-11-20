To set up your project for the EECS3311 course, involving a backend Java API with two microservices using MongoDB and Neo4J databases, follow these instructions closely. Ensure you adhere to the specific details provided in your course project description and the starter code.

### Java and Maven Setup

1. **Java Version**: Ensure you are using Java 1.8. This is crucial as using a different version may lead to compatibility issues.
2. **Maven**: Install the latest version of Maven. It is used for building and managing your Java-based project.

### MongoDB Setup

1. **Install MongoDB**: Download and install the latest version of MongoDB.
2. **Database Name**: Create a database named `eecs3311-test`.
3. **Collection**: Within the database, create a collection called `songs`.
4. **Port**: Ensure MongoDB is connected to port `27017`.

### Neo4J Setup

1. **Install Neo4J Desktop**: Download and install the latest version of Neo4J Desktop.
2. **Username and Password**: Set the username as `neo4j` and password as `12345678`.
3. **Connection Port**: Ensure the database is connected to port `7687`.
4. **Database Schema**:
   - Nodes: Should be exactly `profile`, `song`, and `playlist`.
   - Relationships: Must be `(:profile)-[:created]->(:playlist)`, `(:profile)-[:follows]->(:profile)`, and `(:playlist)-[:includes]->(:song)`.
   - Each `song` node must have a `songId` attribute.
   - The `playlist` name should be `{userName}-favorites`.

### Project Structure

1. **Profile Microservice**: Located under `projectf23/profile-microservice`.
2. **Song Microservice**: Located under `projectf23/song-microservice`.
3. **Controllers and Implementation Classes**:
   - Controller Classes (e.g., `ProfileController.java`, `SongController.java`) handle routing and responses.
   - Implementation Classes (e.g., `ProfileDriverImpl.java`, `PlaylistDriverImpl.java`, `SongDalImpl.java`) manage database operations and functions.

### Testing Your Code

1. **Running Services**: Use `mvn compile` and `mvn spring-boot:run` in the root directory of each microservice to compile and run the applications.
2. **Using CLI**: You can test your endpoints using `curl` commands in the command line.
3. **Using Postman (opional)**: Postman is recommended for testing HTTP endpoints, where you can specify request bodies, query parameters, headers, etc.

### Code Style and Imports

Ensure you follow the specified coding standards, naming conventions, and import rules as per your course guidelines.

### Additional Notes

- Do not alter the versions specified in `pom.xml`.
- Pay attention to error handling in your REST API endpoints. Use appropriate HTTP status codes.
- Remember, the design choices for the interactions and internal structure of both microservices are up to you, but keep efficiency and convenience in mind.

These setup instructions are based on the requirements and specifications provided in your course project description. Ensure to follow them precisely for a successful project setup.

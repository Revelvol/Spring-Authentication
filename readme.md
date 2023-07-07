# Spring JWT Authentication and User Information Backend

This project is a backend implementation for a Spring-based JWT authentication and user information service.

## Prerequisites

Before running the project, make sure you have the following:

- Docker and Docker Compose installed
- .env file with the following data (used by the docker-compose):

```
POSTGRES_USER=<your_postgres_username>
POSTGRES_PASSWORD=<your_postgres_password>
POSTGRES_DB=<your_postgres_database>
POSTGRES_LOCAL_PORT=5342
POSTGRES_DOCKER_PORT=5432
```
- fill up the application.properties on the resources folder in src and test: 
```
spring.datasource.url=jdbc:postgresql://db:$POSTGRES_DOCKER_PORT/$POSTGRES_DB
spring.datasource.username=$POSTGRES_USER
spring.datasource.password=$POSTGRES_PASS
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create
jwt.secret=$JWT_SECRET_KEY <recommended to be 256 bit HEX secret key>
```

## Running the Project

To run the project, follow these steps:

1. Clone the repository to your local machine.
2. Navigate to the project's root directory.
3. Ensure you have the .env file in the root directory with the required environment variables.
4. Open a terminal or command prompt.
5. Run the following command to start the project using Docker Compose:

`docker-compose up`

This command will build and start the Docker containers for the project.

6. The backend will be accessible at `http://localhost:{SPRING_LOCAL_PORT}`.

## Endpoints
Following Endpoint are available
### Authorization

- **POST /api/v1/auth/register**: Sign up a new user.
- **POST /api/v1/auth/authorization**: Retrieve a JWT token for authentication.

### User Information

- **GET /api/v1/user**: Retrieve the user's own information (requires JWT authentication).
- **POST /api/v1/user**: Create a new user (requires JWT authentication).
- **PUT /api/v1/user**: Update the user's information (requires JWT authentication).
- **PATCH /api/v1/user**: Partially update the user's information (requires JWT authentication).

### Other Endpoints

- **GET /api/v1/demo-controller**: Demo for authorization (requires a valid JWT token).
- **GET /**: Health check endpoint to ensure the API is running.

## Additional Configuration

For any additional configuration or customization, refer to the project's source code and configuration files.


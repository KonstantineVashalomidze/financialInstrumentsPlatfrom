# financialInstrumentsPlatfrom

A web application for subscribing to and unsubscribing from financial instruments, with live price updates from a mock data generator and a built-in chat feature. Built with Spring Boot, MongoDB, WebSocket, and a React frontend.

## Project layout

This repository does not use a single monorepo branch. Each part of the app lives on its own branch:

- **MainSpringBootApp** (this branch, default) - Spring Boot backend: JWT authentication, subscription API, WebSocket chat, MongoDB persistence
- **MockDataService** - Spring Boot microservice that generates mock instrument price data for the main app to consume
- **financial-instruments-platform** - React frontend (login, registration, dashboard, chat)

To run everything together with Docker Compose, clone all three branches into sibling directories matching the paths in `docker-compose.yml`:

```
some-folder/
  MainSpringBootApp/              (this branch)
  MockDataService/
  financial-instruments-platform/
```

## Running the backend (this branch)

Requires a JDK, Maven, and a MongoDB instance (e.g. MongoDB Atlas).

Set the required environment variables before running:

```
JWT_SECRET_KEY=<a random secret string>
MONGO_CONNECTION_STRING=<your MongoDB connection string>
```

Then build and run:

```bash
mvn clean package -DskipTests
java -jar target/MainSpringBootApp-1.0-SNAPSHOT.jar
```

The API runs on port 8081. Swagger UI is available at `/swagger-ui/index.html`.

## Running everything with Docker Compose

With the three branches cloned as sibling directories (see Project layout above) and `JWT_SECRET_KEY` / `MONGO_CONNECTION_STRING` set in your shell or a `.env` file next to `docker-compose.yml`:

```bash
docker-compose up --build
```

This starts the mock data service (port 8080), the backend (port 8081), and the React frontend (port 3000).

## License

See [LICENSE](LICENSE).

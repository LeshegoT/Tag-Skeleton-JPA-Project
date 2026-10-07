# TAG Skeleton Project

A reference Jakarta EE service for building TAG applications. It demonstrates REST endpoints, PostgreSQL persistence with Flyway, ActiveMQ command/event messaging, JSON/XML serialization, fixed-length message parsing, and container-based integration testing on Open Liberty.

## Project structure

| Module | Purpose |
| --- | --- |
| `tag-skeleton-project-messages` | Shared command/event contracts and serialization adapters. |
| `tag-skeleton-project-app` | WAR application containing REST resources, message handlers, persistence, migrations, and tests. |

## Prerequisites

- A JDK compatible with the TAG parent POMs
- Maven 3.x
- Docker (required for the MicroShed/Testcontainers integration tests)
- Access to the Maven repository that hosts the `za.co.sbg.tag` parent POMs and platform dependencies

For a locally running application, PostgreSQL and ActiveMQ must also be available. The default connection values are listed under [Configuration](#configuration).

## Build and test

Build all modules and run the test suite from the repository root:

```shell
mvn clean verify
```

Install the messages artifact and application into the local Maven repository:

```shell
mvn clean install
```

Run only unit tests:

```shell
mvn test
```

The integration suite uses MicroShed Testing and Testcontainers to start the application, PostgreSQL, and ActiveMQ. Ensure Docker is running before executing `verify`.

## Running locally

Start PostgreSQL and ActiveMQ, then run the application with the Open Liberty Maven goal supplied by the TAG application parent:

```shell
mvn -pl tag-skeleton-project-app -am liberty:dev
```

By default, the application listens on `http://localhost:8090`. Runtime values can be overridden with the environment variables below.

## Configuration

| Environment variable | Default | Description |
| --- | --- | --- |
| `APP_PORT` | `8090` | HTTP port |
| `APP_PORT_SECURE` | `18090` | HTTPS port |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `PACManDB` | Database name |
| `DB_USER` | `postgres` | Database user |
| `DB_PASSWORD` | `admin` | Database password |
| `BROKER_URL` | `tcp://localhost:61616` | ActiveMQ broker URL |
| `BROKER_USER` | `admin` | ActiveMQ user |
| `BROKER_PASSWORD` | `admin` | ActiveMQ password |

The application uses the PostgreSQL schema `skeleton`. Flyway applies migrations from `tag-skeleton-project-app/src/main/resources/db/migration` during startup.

The checked-in credentials are development defaults only; supply secrets through the deployment environment outside local development.

## REST API

The resource base path is `/tag/skeleton`. Dates use ISO-8601 offset date-time format, for example `2026-09-22T14:00:00+02:00`.

### Create or upsert a name

```shell
curl -X POST "http://localhost:8090/tag/skeleton/name?name=Example&date=2026-09-22T14%3A00%3A00%2B02%3A00"
```

Returns `201 Created`. Creating an existing name updates its date and marks it as updated.

### List names

```shell
curl "http://localhost:8090/tag/skeleton/name?isUpdated=false"
```

Use `isUpdated=true` to return updated records.

### Update a name

```shell
curl -X PUT "http://localhost:8090/tag/skeleton/name" \
  -H "Content-Type: application/json" \
  -d '{"name":"Example","date":"2026-09-23T09:00:00Z"}'
```

Returns `200 OK`, or `404 Not Found` if the name does not exist.

### Delete a name

```shell
curl -X DELETE "http://localhost:8090/tag/skeleton/name?name=Example"
```

Returns `204 No Content`, or `404 Not Found` if the name does not exist.

Create, update, and delete operations publish an `OutgoingXmlMessageV1` event.

## Messaging

The application consumes these ActiveMQ command queues:

- `command.za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1`
- `command.za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1`

The JSON handler optionally reads the `Country_ISO` message header. Processed commands publish either `OutgoingXmlMessageV1` or `OutgoingTextMessageV1`, according to the relevant processing path. Messaging content types and encodings are declared in `tag-skeleton-project-app/src/main/resources/config.yaml`.

## Database

The initial Flyway migration creates `skeleton.skeleton_name` with a unique name, an offset-aware date, and an `is_updated` flag. Hibernate schema generation is disabled, so schema changes should be made through new Flyway migrations.

## API documentation

MicroProfile OpenAPI is enabled in the Liberty configuration. With the application running, the generated OpenAPI document is available at:

```text
http://localhost:8090/openapi
```

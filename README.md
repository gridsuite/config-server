# Config Server

[![Actions Status](https://github.com/gridsuite/config-server/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/config-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Aconfig-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Aconfig-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

## Description

The **config-server** is a microservice of the [GridSuite](https://github.com/gridsuite) platform dedicated to **storing and managing user configuration parameters** per application.

It provides the following capabilities:

- **Store and retrieve configuration parameters** scoped by user and application name.
- **Read a single parameter** by user, application, and parameter name.
- **Create or update parameters** individually (by name) or in bulk (via a map of key/value pairs).
- **Emit change notifications** via RabbitMQ whenever a parameter is updated, so that other services or clients can react in real time.

---

## Technical Stack

- Spring Boot (WebFlux, Data R2DBC, Actuator, Cloud Stream)
- PostgreSQL (reactive via `r2dbc-postgresql`)
- Liquibase
- RabbitMQ via Spring Cloud Stream
- API documentation: OpenAPI / Swagger (`springdoc-openapi-starter-webflux-ui`)
- Micrometer / Prometheus

---

## Development Scripts

Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets. After you generated a changeset do not forget to add it to git and in `src/resources/db/changelog/db.changelog-master.yml`.

---

## Interactions with Other Microservices

```
┌──────────────────────┐
│    config-server     │
└──────────────────────┘
          ▼
    RabbitMQ (publishConfigUpdate-out-0)
          ▼
┌──────────────────────────┐
│config-notification-server│  (consumes parameter change events)
└──────────────────────────┘
```

The config-server does not call other microservices directly. It only publishes change events that downstream consumers (such as `config-notification-server`) can listen to.

---


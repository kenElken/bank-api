# AGENTS.md

## Project overview

This project is a Java Spring Boot bank branch API. The application allows users to register, create bank accounts, make internal transfers between accounts, and prepare inter-bank transfers through a Central Bank API integration.

The project uses:

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* PostgreSQL
* Hibernate
* Lombok
* Maven
* Swagger / OpenAPI

Main project files and folders:

* `src/main/java/` contains the application source code.
* `src/main/resources/` contains configuration files.
* `pom.xml` contains Maven dependencies and build configuration.
* Controllers expose REST API endpoints.
* Entities describe database tables.
* Repositories handle database access.
* Security-related classes handle API key based authentication.

Critical parts of the project:

* User registration and API key generation.
* Account creation and account number generation.
* Transfer logic and balance checks.
* Authentication and authorization.
* PostgreSQL database connection and entity mappings.
* Central Bank API integration.
* Existing API routes, because changing them can break Swagger tests or external clients.

Do not rewrite the whole project structure unless the task explicitly requires it.

## Development commands

Use these commands from the project root.

Run the application:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

Build the project:

```bash
mvn clean package
```

Clean generated build files:

```bash
mvn clean
```

If the project uses PostgreSQL locally, make sure the database is running before starting the application.

## Code style

* Follow the existing Spring Boot project structure.
* Keep controller, service, repository and entity responsibilities separate.
* Controllers should handle HTTP requests and responses.
* Business logic should not be placed directly into controllers if a service layer already exists or is appropriate.
* Repositories should only handle database access.
* Keep methods small and focused.
* Use clear and descriptive names for classes, methods and variables.
* Do not duplicate logic if an existing helper method or service already solves the same problem.
* Do not remove Lombok annotations unless there is a clear reason.
* Do not change public API routes without explicit confirmation.
* Do not rename entity fields, database fields, DTO fields or route paths unless the task requires it.
* Keep code readable and simple. Avoid over-engineering.

## Testing rules

* New functionality should include tests where practical.
* Bug fixes must include a regression test when possible. The test should fail before the fix and pass after the fix.
* Do not delete existing tests unless they are clearly obsolete and the reason is explained.
* Before marking work complete, run at least:

```bash
mvn test
```

* If the task changes application startup, dependencies, entities or configuration, also run:

```bash
mvn clean package
```

* If tests fail, do not mark the task complete before the reason is found and fixed.
* If a test cannot be run because of a missing database or environment setting, clearly explain what was not tested and why.

## Git workflow

* Never commit directly to `main`.
* Use a feature branch for every change.
* Keep changes small and focused.
* Do not mix unrelated fixes into the same change.
* Do not mark a task complete if tests or build fail.
* Every pull request should explain:

  * what was changed;
  * why it was changed;
  * how it was tested;
  * which files were affected.
* Before merging to `main`, the project should use technical protection where possible:

  * pull request review;
  * required status checks;
  * CI build;
  * branch protection rules.

The rules in this file help guide the AI assistant, but they do not technically block mistakes. GitHub branch protection, CI checks and code review are still needed.

## Scope control

* Only change files that are directly related to the task.
* Do not refactor unrelated code.
* Do not rename classes, routes, public methods, database fields or configuration keys unless explicitly requested.
* Do not remove existing functionality to make a new feature easier to implement.
* If a change requires editing more than 5 files, first explain why the change is necessary.
* Prefer small, reviewable changes over large rewrites.
* Do not replace working code with a completely different solution unless there is a clear reason.
* Do not change formatting across unrelated files.

## Security rules

* Never commit secrets, passwords, API keys, tokens or private credentials.
* Do not log sensitive user data, API keys or passwords.
* Validate user input on the server side.
* Do not disable authentication or authorization checks to make tests pass.
* Do not make protected endpoints public unless the task explicitly requires it.
* Do not weaken API key validation.
* Do not expose internal error details to API users.
* Use safe database access through Spring Data JPA repositories.
* Do not build SQL queries through unsafe string concatenation.
* Keep security-related changes small and clearly explained.

## Database rules

* Do not change entity mappings without checking how the database is affected.
* Do not rename database fields or entity fields unless the task explicitly requires it.
* Do not delete data-related code without explaining the impact.
* If a schema change is needed, explain:

  * what changes in the database;
  * whether existing data is affected;
  * how the change should be tested.
* Be careful with account balances, transfers and user data. These are critical parts of the project.

## Dependency rules

* Do not add new production dependencies without explaining why they are necessary.
* Prefer existing project dependencies.
* Check whether the same functionality already exists in the project before adding a new library.
* Do not update many dependency versions at once unless the task is specifically about dependency updates.
* After changing `pom.xml`, run:

```bash
mvn clean package
```

## Central Bank API integration rules

* Do not change Central Bank API integration logic unless the task requires it.
* Do not hard-code secret credentials.
* Do not remove error handling for failed external API calls.
* If an external request fails, the application should return a clear and safe error message.
* Inter-bank transfer changes must be tested carefully because they affect external communication and transfer status.

## Completion checklist

Before saying the task is complete, verify the following:

* [ ] I reviewed the existing code before making changes.
* [ ] I changed only files related to the task.
* [ ] I did not remove working functionality without a clear reason.
* [ ] I did not change public API routes unless requested.
* [ ] I did not add secrets, passwords, API keys or tokens.
* [ ] I validated user input where needed.
* [ ] I kept authentication and authorization checks in place.
* [ ] I ran `mvn test`.
* [ ] I ran `mvn clean package` if configuration, dependencies, entities or startup logic changed.
* [ ] I checked that no unrelated files were changed.
* [ ] I summarized what changed and why.

## When unsure

* Ask for clarification before changing architecture.
* Ask for clarification before changing database schema.
* Ask for clarification before changing authentication or authorization.
* Ask for clarification before changing public API endpoints.
* Ask for clarification before deleting files or removing existing functionality.
* If a requirement is ambiguous, propose a short plan before editing code.
* If tests fail and the cause is unclear, report the failure instead of guessing.
* If the safest solution is uncertain, choose the smallest possible change and explain the trade-off.

# Employee Directory API

A Spring Boot REST API for managing departments, employees, and skills.

## Technology

- Java 17
- Spring Boot 4.1.1
- Maven
- Spring Web, Spring Data JPA / Hibernate, Jakarta Bean Validation
- H2 in-memory database
- springdoc-openapi / Swagger UI
- JUnit 5, Mockito, Spring Boot Test and MockMvc

## Prerequisites

- JDK 17+
- Maven 3.8+

## Run locally

```bash
mvn clean test
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

- Swagger UI: http://localhost:8080/docs
- H2 console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: (empty)
  - openApi.yml : `C:\Study Materials\Spring Boot\employee directory\src\main\resources\static\openapi.yaml`

The database is in-memory, so its data is cleared when the application stops.

## Configuration

Values are externalised in `src/main/resources/env.properties`:
These values are injected in application.propertices then into the Project.
We can put the file (env.properties) outside of the project as well(inside docker container).

```properties
app.name=Employee Directory API
app.version=1.0.0
directory.max-skills-per-employee=10
pagination.default-page-size=20
```

`GET /api/info` exposes the configured application name and version.

## Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/departments` | Create department |
| GET | `/api/departments` | List departments |
| GET | `/api/departments/{id}` | Get department |
| PUT | `/api/departments/{id}` | Update department |
| DELETE | `/api/departments/{id}` | Delete department if it has no employees |
| GET | `/api/departments/{id}/employees` | List department employees |
| POST | `/api/skills` | Create skill |
| GET | `/api/skills` | List skills |
| POST | `/api/employees` | Create employee |
| GET | `/api/employees` | List/filter/paginate employees |
| GET | `/api/employees/{id}` | Get employee with department and skills |
| PUT | `/api/employees/{id}` | Update employee details, department and skills |
| DELETE | `/api/employees/{id}` | Delete employee |
| POST | `/api/employees/{id}/skills/{sid}` | Add skill |
| DELETE | `/api/employees/{id}/skills/{sid}` | Remove skill |
| GET | `/api/info` | Get application name/version |

Employee list parameters:
- `departmentId` (optional)
- `page` (zero-based, default `0`)
- `size` (defaults to `pagination.default-page-size`, max `200`)
- `sortBy` (`id`, `name`, `email`, or `joiningDate`; default `id`)
- `sortDir` (`asc` or `desc`; default `asc`)

Example: `/api/employees?departmentId=1&page=0&size=10&sortBy=name&sortDir=asc`

## Sample requests

Create a department:

```bash
curl -i -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name":"Backend","location":"Kolkata"}'
```

Create skills:

```bash
curl -i -X POST http://localhost:8080/api/skills \
  -H "Content-Type: application/json" \
  -d '{"name":"Java","category":"Language"}'

curl -i -X POST http://localhost:8080/api/skills \
  -H "Content-Type: application/json" \
  -d '{"name":"SQL","category":"Database"}'
```

Create an employee (use the IDs returned by the previous calls):

```bash
curl -i -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Rohit Sharma",
    "email":"Rohit.sharma@example.com",
    "designation":"Software Engineer",
    "joiningDate":"2024-01-10",
    "departmentId":1,
    "skillIds":[1,2]
  }'
```

List employees:

```bash
curl -i "http://localhost:8080/api/employees?page=0&size=20"
curl -i "http://localhost:8080/api/employees?departmentId=1"
```

Get one employee:

```bash
curl -i http://localhost:8080/api/employees/1
```

Add or remove a skill:

```bash
curl -i -X POST http://localhost:8080/api/employees/1/skills/2
curl -i -X DELETE http://localhost:8080/api/employees/1/skills/2
```

Get app info:

```bash
curl -i http://localhost:8080/api/info
```

## Error response

Errors use a consistent JSON shape:

```json
{
  "timestamp": "2026-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/departments"
}
```

- `400 Bad Request`: invalid body, validation or query parameters
- `404 Not Found`: requested department, employee or skill does not exist
- `409 Conflict`: duplicate name/email, skill limit exceeded, or department still has employees

## Design notes

- Controllers handle HTTP mapping; services contain business rules; repositories handle persistence.
- DTOs are used rather than serializing JPA entities directly, preventing recursive relationship serialization.
- An employee must reference an existing department.
- Skills are associated through the `employee_skill` join table.
- Department deletion is rejected while employees remain assigned to it.
- The maximum skill count is enforced on employee creation/update and when adding a skill.
- The employee `PUT` request is a full update and accepts the current `departmentId` and `skillIds`.

## Project layout

```text
src/main/java/com/example/employeedirectory/
  config/       # external configuration properties
  controller/   # REST endpoints
  dto/          # request and response models
  entity/       # JPA entities
  exception/    # domain exceptions and global handler
  repository/   # Spring Data JPA repositories
  service/      # business logic
src/test/java/  # service unit tests and MockMvc integration tests
openapi.yaml
```

## Docker

Build and run:

```bash
docker build -t employee-directory-v1.jar .
docker run --d -p 8080:8080 employee-directory-v1.jar
```

Because H2 is in-memory, application restarts reset the database.

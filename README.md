# Demo Account Mock Server

A Spring Boot application with embedded WireMock server for mocking the Demo Account API.

## Overview

This mock server provides a simulated endpoint for the Demo Account personal data API. It uses WireMock to serve predefined responses based on request matching rules.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (for Cloud Run deployment)
- Google Cloud SDK (for Cloud Run deployment)

## Project Structure

```
mock-server/
├── pom.xml
├── README.md
├── Dockerfile
├── deploy.sh
├── run.sh
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/acme/banking/demoaccount/
    │   │       └── DemoAccountApplication.java
    │   └── resources/
    │       ├── application.properties
    │       └── wiremock/
    │           └── mappings/
    │               └── mobile.api/
    │                   └── customers/
    │                       └── #{partnerId}#personal-data/
    │                           ├── max-mustermann.json  # now Hans Müller payload (id: 1234567891)
    │                           └── maria-musterfrau.json (id: 1234567890)
    └── test/
        └── java/
            └── com/acme/banking/demoaccount/
                └── DemoAccountApplicationIntegrationTest.java
```

## How to Run

### Local Development

#### Using Spring Boot Maven Plugin

```bash
mvn spring-boot:run
```

#### Using the run script

```bash
./run.sh
```

#### Building and Running the JAR

```bash
# Build the project
mvn clean package

# Run the JAR
java -jar target/demoaccount-1.0.0-SNAPSHOT.jar
```

The server will start on **port 8080** (or whatever is provided by the PORT environment variable in Cloud Run).

### Cloud Run Deployment

Deploy to Google Cloud Run:

```bash
# Ensure you're logged in and have a project set
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# Deploy
./deploy.sh
```

The deployment script will:
1. Build the application with Maven
2. Build a Docker image
3. Push to Google Container Registry
4. Deploy to Cloud Run

**Example deployed URL (yours will differ):** `https://mock-server-1041912723804.us-central1.run.app`

## Available Endpoints

### GET /customers/{partnerId}/personal-data

Returns personal data for a customer.

**Required Headers:**
- `deuba-client-id`: Must contain `-banking` (e.g., `pb-banking`, `mobile-banking`, `app-banking`)

**URL Pattern:**
- `/customers/.*/personal-data` (regex pattern, any partnerId is accepted)

**Example Requests:**

Hans Müller (explicit partner id):
```bash
curl -X GET http://localhost:8080/customers/1234567891/personal-data \
  -H "deuba-client-id: pb-banking" \
  -H "Accept: application/json"
```

Maria Musterfrau (explicit partner id):
```bash
curl -X GET http://localhost:8080/customers/1234567890/personal-data \
  -H "deuba-client-id: pb-banking" \
  -H "Accept: application/json"
```

Default (any other partner id returns the Hans Müller payload):
```bash
curl -X GET http://localhost:8080/customers/any-id/personal-data \
  -H "deuba-client-id: pb-banking" \
  -H "Accept: application/json"
```

**Example Response (Hans Müller):**
```json
{
  "firstname": "Hans",
  "lastname": "Müller",
  "academicTitle": "",
  "titleOfNobility": "",
  "fullName": "Hans Müller",
  "id": 1234567891,
  "dateOfBirth": "1996-01-01",
  "placeOfBirth": "",
  "nationality": "DEU",
  "maritalStatus": "married",
  "gender": "MALE",
  "registrationAddress": {
    "id": 1,
    "street": "Kurfürstendamm",
    "streetNumber": "100",
    "postalCode": "10711",
    "city": "Berlin"
  },
  "postalAddress": {
    "id": 1,
    "street": "Kurfürstendamm",
    "streetNumber": "100",
    "postalCode": "10711",
    "city": "Berlin"
  },
  "emailAddress": {
    "id": 0,
    "address": "hans.mueller@random.de",
    "type": "PRIVATE"
  },
  "phoneNumbers": {
    "private": {
      "id": 1,
      "countryCode": "+49",
      "number": "987654321"
    },
    "work": {
      "id": 1,
      "countryCode": "+49",
      "number": "444555666"
    },
    "mobile": {
      "id": 1,
      "countryCode": "+49",
      "number": "111222333"
    }
  }
}
```

**Example Response (Maria Musterfrau):**
```json
{
  "firstname": "Maria",
  "lastname": "Musterfrau",
  "academicTitle": "Dr.",
  "titleOfNobility": "",
  "fullName": "Dr. Maria Musterfrau",
  "id": 1234567890,
  "dateOfBirth": "1988-05-15",
  "placeOfBirth": "Berlin",
  "nationality": "DEU",
  "maritalStatus": "single",
  "gender": "FEMALE",
  "registrationAddress": {
    "id": 2,
    "street": "Friedrichstraße",
    "streetNumber": "45",
    "postalCode": "10117",
    "city": "Berlin"
  },
  "postalAddress": {
    "id": 2,
    "street": "Friedrichstraße",
    "streetNumber": "45",
    "postalCode": "10117",
    "city": "Berlin"
  },
  "emailAddress": {
    "id": 1,
    "address": "maria.musterfrau@mail.com",
    "type": "PRIVATE"
  },
  "phoneNumbers": {
    "private": {
      "id": 2,
      "countryCode": "+49",
      "number": "987654321"
    },
    "work": {
      "id": 2,
      "countryCode": "+49",
      "number": "333444555"
    },
    "mobile": {
      "id": 2,
      "countryCode": "+49",
      "number": "800000600"
    }
  }
}
```

> **Note:** Any partner ID other than `1234567890` will return the default Hans Müller response unless a specific mapping exists for that ID.

## How to Test

### Run All Tests

```bash
mvn test
```

### Run Integration Tests Only

```bash
mvn test -Dtest=DemoAccountApplicationIntegrationTest
```

## Request Matching Rules

The WireMock stub will match requests when:

1. **Method**: GET
2. **URL Pattern**: `/customers/.*/personal-data` (regex)
3. **Header**: `deuba-client-id` must **contain** `-banking`

Valid header examples:
- `pb-banking` ✓
- `mobile-banking` ✓
- `app-banking` ✓
- `invalid-header` ✗ (does not contain `-banking`)

## Configuration

### application.properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port (uses PORT env var in Cloud Run) |
| `wiremock.server.port` | 8080 | WireMock server port |
| `wiremock.root.dir` | (empty) | File system path for WireMock files (used in Docker) |
| `logging.level.com.github.tomakehurst.wiremock` | DEBUG | WireMock logging level |

## Adding New Stubs

To add new mock endpoints:

1. Create a new JSON file under `src/main/resources/wiremock/mappings/`
2. Follow the WireMock stub mapping format
3. Restart the application

Example stub format:
```json
{
  "priority": 4,
  "request": {
    "method": "GET",
    "urlPattern": "/your/endpoint/.*",
    "headers": {
      "your-header": {
        "contains": "value"
      }
    }
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "key": "value"
    }
  }
}
```

## Dependencies

- **Spring Boot 3.2.2**: Application framework
- **WireMock 3.12.1**: Mock server
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **Rest-Assured 5.4.0**: REST API testing

## Bruno Collection

A [Bruno](https://www.usebruno.com/) API collection is included for easy testing.

### Collection Structure
```
bruno/
├── bruno.json
├── environments/
│   ├── Local.bru
│   └── CloudRun.bru
└── customers/
    ├── Get Hans Mueller Personal Data.bru
    ├── Get Maria Musterfrau Personal Data.bru
    ├── Get Personal Data - Missing Header.bru
    └── Get Personal Data - Invalid Header.bru
```

### How to Use
1. Install [Bruno](https://www.usebruno.com/downloads)
2. Open Bruno and click "Open Collection"
3. Navigate to the `bruno/` folder in this project
4. Select the "Local" or "CloudRun" environment
5. Run requests or the entire collection

### Environment Variables

#### Local Environment
| Variable | Value | Description |
|----------|-------|-------------|
| `baseUrl` | `http://localhost:8080` | Local mock server URL |
| `clientId` | `pb-banking` | Default deuba-client-id header value |

#### CloudRun Environment
| Variable | Value | Description |
|----------|-------|-------------|
| `baseUrl` | `https://mock-server-1041912723804.us-central1.run.app` | Cloud Run URL |
| `clientId` | `pb-banking` | Default deuba-client-id header value |

## License

Internal use only.

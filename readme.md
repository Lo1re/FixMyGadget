# FixMyGadget

**A lightweight CRM system for gadget repair service centers.**

FixMyGadget is a Java application designed to simplify repair management for service centers working with smartphones, tablets, and smartwatches. It provides a centralized way to manage gadgets, customers, technicians, and repair orders.

## Features

- **Gadget Management** — maintain a catalog of gadgets with manufacturers, prices, screen dimensions, types, and additional features.
- **Customer Management** — create, update, search, and manage customer records with unique phone numbers.
- **Technician Management** — maintain technician information and manage repair assignments.
- **Repair Order Management** — create repair requests, describe problems, assign technicians, set repair costs, and track progress.
- **Repair Status Tracking** — manage repair orders through `ACCEPTED`, `IN_PROGRESS`, and `DONE` statuses.
- **Search and Filtering** — filter repair orders by status, technician, customer, gadget, and date range.
- **Sorting** — sort gadgets and repair orders by different columns using a custom QuickSort implementation.
- **Repair History** — view previous repairs associated with a customer or gadget.
- **Reports** — calculate revenue for a selected period and generate technician workload reports.
- **Import and Export** — save and load the gadget catalog using files.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Core application logic |
| JavaFX | Graphical user interface |
| PostgreSQL | Relational database |
| JDBC | Database connectivity |
| Maven | Dependency and build management |
| JUnit 5 | Unit testing |
| Mockito | Mocking dependencies in tests |
| Log4j2 + SLF4J | Application logging |

## Architecture

The project follows a layered architecture to separate business logic, database access, and the user interface.

- **Model** — domain entities and enumerations.
- **DAO** — database access through interfaces and JDBC implementations.
- **Service** — business logic and validation.
- **Facade** — a unified entry point for the user interface.
- **UI** — JavaFX controllers and views.
- **Database** — connection management and automatic schema initialization.
- **Tests** — unit tests for models, sorting, status transitions, and services.

## Design Patterns

The application uses several design patterns to keep the code organized and maintainable:

- **DAO** — separates database operations from business logic.
- **Builder** — simplifies the creation of complex objects.
- **Factory** — centralizes DAO instance creation.
- **Singleton** — manages the database connection provider.
- **Strategy** — encapsulates sorting and filtering criteria.
- **Facade** — provides a unified interface for application operations.

## Database

FixMyGadget uses PostgreSQL with a normalized relational schema designed around the following tables:

- `manufacturer` — gadget manufacturers.
- `gadget_type` — supported gadget types.
- `feature` — additional gadget features.
- `gadget` — gadget information.
- `gadget_feature` — relationships between gadgets and features.
- `customer` — customer information.
- `technician` — technician records.
- `repair_status` — available repair statuses.
- `repair_order` — repair requests and their details.

The database schema is initialized automatically when the application starts. Primary keys, foreign keys, unique constraints, and check constraints help maintain data integrity.

## Getting Started

### Prerequisites

Make sure the following are installed:

- JDK 17 or later
- Maven
- PostgreSQL

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/Lo1re/FixMyGadget.git
cd FixMyGadget
```

**2. Create the database**

Create an empty PostgreSQL database named `fixmygadget`:

```sql
CREATE DATABASE fixmygadget;
```

**3. Configure the database connection**

Update `src/main/resources/application.properties` with your PostgreSQL connection settings.

**4. Run the application**

```bash
mvn clean javafx:run
```

If the project uses a different JavaFX Maven configuration, run the application through your IDE or the configured Maven entry point.

The database tables and initial reference data are created automatically on startup.

## Testing

Run the test suite with:

```bash
mvn test
```

The tests cover core model functionality, object builders, QuickSort behavior, repair status transitions, and service-layer logic.

## Repair Workflow

```text
Create Repair Order
        |
        v
     ACCEPTED
        |
        v
    IN_PROGRESS
        |
        v
       DONE
```

A repair order must have a repair cost before it can be marked as `DONE`. Its creation date is assigned automatically, and its completion date is recorded when the repair is finished.

## Project Goals

FixMyGadget aims to provide a simple and maintainable solution for organizing repair service operations, reducing manual record-keeping, and keeping repair information in one place.

---

**FixMyGadget** — simple repair management, all in one place.
# 📚 Virtual Bookstore - Enterprise E-Commerce Platform

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-000000?style=for-the-badge&logo=keycloak&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=openapi-initiative&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

A highly scalable, full-stack enterprise e-commerce platform designed for software engineers. This system handles the full lifecycle of an online technical bookstore, featuring stateful order processing, centralized Identity and Access Management (IAM), contract-first API design, and interactive WebGL physics labs.

👉 [View Frontend Application Source](./frontend)

---

## 🔗 🏗️ System Architecture

The platform is designed using modern n-tier architecture principles, ensuring a clean separation of concerns across the stack:

*   **Presentation Layer (Angular 18+):** Built using modern Standalone Components, functional route guards, and RxJS for reactive state management. The UI features a custom glass-morphism design system.
*   **API Gateway & Routing:** Manages incoming client requests, serving as the entry point for the REST API.
*   **Business Logic Layer (Spring Boot):** Implements complex e-commerce rules (stock validation, price calculation) using specialized Service classes.
*   **Data Access Layer (Spring Data JPA):** Utilizes Hibernate for ORM mapping, communicating securely with the database. Data Transfer Objects (DTOs) and Entity mappings are handled automatically via **MapStruct**.
*   **Identity Provider (Keycloak):** An externalized OIDC server that handles user registration, session management, and issues signed JSON Web Tokens (JWTs).

---

## 🛠️ Technology Stack

### Backend
*   **Java 17+** & **Spring Boot 3.x**
*   **Spring Security** (OAuth2 Resource Server)
*   **Spring Data JPA** & **Hibernate**
*   **MapStruct** (Entity-DTO mapping)

### Frontend
*   **Angular** (Standalone Architecture)
*   **TypeScript**, HTML5, CSS3
*   **WebGL / Canvas** (For interactive engineering visual labs)

### Database & DevOps
*   **PostgreSQL** (Primary relational datastore)
*   **Docker & Docker Compose** (Containerized orchestration)
*   **Keycloak** (IAM & SSO)
*   **Swagger / OpenAPI 3.0** (API Documentation)

### Testing
*   **JUnit 5** & **Mockito** (Unit testing & mocking)
*   **MockMvc** (Controller & HTTP integration testing)
*   **Spring Security Test** (Mocking JWT contexts)

---

## ✨ Core Features & Business Logic

### 🔐 1. Advanced Security & RBAC
Authentication is strictly separated from the core application. Users authenticate against Keycloak, which returns a signed JWT stored securely in the browser's `localStorage`.
*   **Custom Functional Guards:** Angular route guards instantly decode JWT payloads to enforce `CUSTOMER` vs. `ADMIN` access at the UI level.
*   **Method-Level Security:** Backend endpoints are secured using `@PreAuthorize("hasRole('ADMIN')")` and custom ownership checks (`@ownershipSecurity.isSelf()`) to ensure users can only view or pay for their *own* orders.

### 🛒 2. Stateful E-Commerce Processing
*   **Stock Validation:** Order creation triggers transactional checks to ensure adequate book inventory. Stock is mathematically deducted in real-time.
*   **Relational Integrity:** Strict foreign-key constraints prevent the deletion of Categories if Books are assigned to them, and prevent the deletion of Books if they exist in a user's Order History.
*   **Automated Background Jobs:** A Spring `@Scheduled` chron job runs every 10 seconds to scan the database and automatically transition `SHIPPED` orders into `COMPLETED` statuses based on time thresholds.

### 📊 3. Administrative Dashboard
A dedicated, protected portal for system administrators providing:
*   Real-time system telemetry (Total Revenue, Active Orders, User Count).
*   Complete CRUD interfaces for managing the Book Catalog and Categories.
*   User management tools (Enable/Disable accounts, elevate roles to ADMIN).
*   Order fulfillment management (Updating order statuses from `PENDING` -> `SHIPPED`).

---

## 🗄️ Database Schema Design

The PostgreSQL database relies on a highly normalized relational structure:
*   **Users:** Stores identity references and roles.
*   **Categories:** Groups books logically (One-to-Many relationship with Books).
*   **Books:** Stores pricing, stock quantity, and metadata.
*   **Orders:** Tracks the transaction lifecycle (Customer ID, Total Price, Status, Date).
*   **OrderItems:** A join table tracking the exact quantity and historical price of specific books purchased within an Order (Many-to-One to both Books and Orders).

---

## 📋 API Documentation & Contracts

This project utilizes **Contract-First Design**. The backend exposes a live, interactive OpenAPI specification that can be directly imported into Postman for automated collection generation.

*   **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
*   **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Key Endpoints:
*   `POST /api/orders` - Initialize a new checkout transaction.
*   `POST /api/orders/{orderId}/pay` - Process payment (Validates ownership & pending status).
*   `GET /api/books` - Retrieve catalog (Publicly accessible).
*   `PATCH /api/users/{userId}` - Admin endpoint to modify user roles and activation status.

---

## 🚀 Local Deployment (Dockerized)

The entire system is containerized for reproducible local deployments.

### Prerequisites
*   [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running.
*   Node.js (v18+) & Angular CLI.
*   Java 17+ & Maven.

### 1. Spin up the Infrastructure
Run the following command from the project root to start PostgreSQL, Keycloak, and the Spring Boot API:
```bash
docker-compose up -d

2. Configure Identity Access (Keycloak)
Navigate to http://localhost:8081 (Keycloak Admin Console).

Log in with the default admin credentials defined in your docker-compose.

Import the realm-export.json (located in the /keycloak directory) to instantly provision the bookstore-realm, client IDs, roles, and test accounts.

3. Launch the Client Application
Open a new terminal, navigate to the frontend directory, and serve the Angular app:

Bash
cd frontend
npm install
ng serve
Navigate to http://localhost:4200 to interact with the Virtual Bookstore.

🧪 Testing Strategy
The backend is fortified with comprehensive automated tests ensuring business logic reliability:

Service Layer Tests: Mockito is heavily utilized to isolate business logic, validating expected exceptions (e.g., BookNotFoundException, UserNotFoundException) and verifying transactional math.

Controller Integration Tests: MockMvc is used to simulate HTTP requests, while SecurityContextHolder is explicitly mocked to test Role-Based Access Control paths without requiring a live Keycloak server during the build phase.


# NukkadSeva Backend

[![SpringBoot](https://img.shields.io/badge/SpringBoot-3.3.2-green.svg)](https://spring.io/) [![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://java.com/) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-green.svg)](https://www.postgresql.org/) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**NukkadSeva** is a comprehensive local services marketplace that connects service providers with customers in their neighborhood. This backend API powers the entire platform with robust authentication, service management, booking systems, and real-time communication features.

## 🏗️ Architecture Overview

The backend is built as a RESTful API using **Spring Boot 3.3.2** and **Java 17**. It utilizes **PostgreSQL** as its primary database. 

### Core Components
- **Framework**: Spring Boot 3.x
- **Authentication**: JWT-based authentication combined with Spring Security.
- **Database ORM**: Spring Data JPA / Hibernate.
- **Mapping**: MapStruct for translating entities to DTOs.
- **Storage**: Azure Blob Storage integration for profile pictures and documents.
- **Real-time Comms**: WebSockets for real-time booking notifications.
- **Email**: Integration with `spring-boot-starter-mail` and FreeMarker for email templates.
- **API Docs**: OpenAPI / Swagger UI.

## 🚀 Quick Start

### Prerequisites
- **Java 17**
- **Gradle** (or use the provided Gradle wrapper `gradlew`)
- **PostgreSQL** Database
- Azure Blob Storage Account (or modify application to use local storage mock)

### Configuration
1. Clone the repository
2. Set up your `.env` or application properties. Refer to `src/main/resources/application.yml` for required keys (e.g., `JWT_SECRET_KEY`, database URL, and Azure Connection Strings).

### Running the Application

Using Gradle wrapper:

```bash
# Build the project
./gradlew build

# Run the Spring Boot application
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.
The OpenAPI documentation is accessible at `http://localhost:8080/swagger-ui.html`.

### Testing from a Mobile Phone (Local Network)
If you want to test the application from your phone while running it on your PC:
1. Connect both devices to the same network (e.g., connect PC to your phone's hotspot).
2. Find your PC's IP Address using `hostname -I` (Linux/Mac) or `ipconfig` (Windows).
3. Run the backend normally (it automatically binds to `0.0.0.0` to accept external traffic).
4. Update frontend `baseURL`: Open `src/lib/api.ts` in the frontend project and set the API URL to your PC's IP:
   ```typescript
   baseURL: "http://<YOUR-PC-IP>:8080/api"
   ```
5. Run the frontend bound to all interfaces:
   ```bash
   npm run dev -- -H 0.0.0.0
   ```
6. Open your phone's browser and go to `http://<YOUR-PC-IP>:3000`.

## 📂 Project Structure

```
src/main/java/com/nukkadseva/nukkadsevabackend/
├── config/              # Configuration (Security, OpenAPI, Azure, etc.)
├── controller/          # REST API endpoints
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── exception/           # Custom exceptions and Global Exception Handler
├── mapper/              # MapStruct interfaces
├── repository/          # Spring Data JPA Repositories
├── security/            # JWT Filters and security entry points
├── service/             # Business Logic Interfaces & Implementations
└── util/                # Utility classes
```

## 🔌 API Overview

This platform provides dedicated endpoints for:
- `/api/public/**` - Unauthenticated access (e.g., searching for services)
- `/api/login`, `/api/register` - Authentication handlers
- Customers, Providers, and Admin specialized controllers.

Check `/swagger-ui.html` during runtime to inspect and test all API routes directly.

## 🤝 Contributing
1. Fork the feature branch
2. Ensure you adhere to standard Java/Spring Boot conventions
3. Submit a Pull Request.

---
**Built with ❤️ by the NukkadSeva Team**

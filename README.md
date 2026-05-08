# Car Wash Management System - Backend API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![JWT](https://img.shields.io/badge/JWT-Security-black.svg)

This is the backend RESTful API service for a comprehensive Car Wash and Valet Management System. It provides robust endpoints for user management, appointment scheduling, service pricing, and revenue tracking.

## Features
- **Role-Based Access Control (RBAC):** Distinct roles for Admin, Individual Customers, and Corporate Clients.
- **Secure Authentication:** Stateless authentication using JWT (JSON Web Tokens).
- **Appointment Management:** Sophisticated slot-based scheduling system with capacity handling and conflict resolution.
- **Dynamic Pricing:** Support for base prices, vehicle-type specific prices, and custom corporate client agreements.
- **Automated Email Notifications:** Real-time email alerts for appointment confirmations, cancellations, and status updates via Gmail SMTP.
- **Admin Dashboard Statistics:** Real-time calculation of revenue, popular services, and daily appointment metrics.

## Tech Stack
- **Framework:** Java 17, Spring Boot 3
- **Database:** PostgreSQL with Spring Data JPA / Hibernate
- **Security:** Spring Security + JWT
- **API Documentation:** Swagger UI / OpenAPI 3
- **Build Tool:** Maven

## Getting Started

1. Clone the repository
2. Set up your PostgreSQL database (default: `carwash_db`)
3. Copy `src/main/resources/application.properties.example` to `application.properties` and fill in your database credentials and Gmail App Password.
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access the Swagger API documentation at: `http://localhost:8080/swagger-ui.html`

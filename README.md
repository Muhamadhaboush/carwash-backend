# Car Wash Management System - Backend API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![JWT](https://img.shields.io/badge/JWT-Security-black.svg)

This is the backend RESTful API service for a comprehensive **Car Wash and Valet Management System**. Built for "Nyg Auto Garage", it digitalizes the entire workflow of an auto care facility, shifting from traditional paper-based bookings to a modern, automated system.

## Business Scope & Logic
Beyond just basic CRUD operations, this backend handles complex business logic tailored for the auto-care industry:
- **Vehicle-Based Dynamic Pricing:** Automatically calculates wash prices based on the customer's vehicle type (e.g., Sedan, SUV, Minivan) fetched from the relational database.
- **Corporate Agreements:** Handles special B2B pricing models where corporate users bypass upfront payments and are billed via custom predefined rates.
- **Valet & Self Drop-off Flows:** Differentiates appointment requirements. If a user selects "Valet", the system strictly validates and requires a registered pickup address.
- **Smart Scheduling Engine:** Prevents double-booking by checking service durations (e.g., 30 mins vs 60 mins), operating hours (08:00 - 20:00), closed days, and concurrent bay capacities.
- **Revenue Analytics:** Provides real-time financial metrics to the admin dashboard, exclusively calculating revenue from `COMPLETED` and `PAID` appointments.

## Technical Features
- **Role-Based Access Control (RBAC):** Distinct roles for Admin, Individual Customers, and Corporate Clients.
- **Secure Authentication:** Stateless authentication using JWT (JSON Web Tokens).
- **Automated Email Notifications:** Real-time email alerts for appointment confirmations, cancellations, and status updates via Gmail SMTP.

## Tech Stack
- **Framework:** Java 17, Spring Boot 3
- **Database:** PostgreSQL with Spring Data JPA / Hibernate
- **Security:** Spring Security + JWT
- **API Documentation:** Swagger UI / OpenAPI 3
- **Build Tool:** Maven

## Getting Started

1. Clone the repository
2. Set up your PostgreSQL database (default: `carwash_db`)
3. Copy `src/main/resources/application.properties.example` to `application.properties` and fill in your credentials.
4. Run the application:
   ```bash
   mvn clean spring-boot:run
   ```
5. Access the Swagger API documentation at: `http://localhost:8080/swagger-ui.html`

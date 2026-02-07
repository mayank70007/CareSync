# CareSync - Hospital Management System

A microservices-based hospital management system built with Spring Boot and Spring Cloud.

## Features

- Patient and Doctor Management
- Appointment Scheduling
- Medical Records Management
- Billing System
- Administrative Dashboard
- JWT Authentication
- Role-based Access Control

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.4.5, Spring Cloud
- **Database**: MySQL 8.0
- **Security**: Spring Security, JWT
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway

## Microservices Architecture

| Service | Port | Description |
|---------|------|-------------|
| Discovery Server | 8761 | Service Registry (Eureka) |
| API Gateway | 8080 | Single entry point for all requests |
| Identity Service | 8085 | Authentication & Authorization |
| Doctor Service | 8086 | Doctor management |
| Patient Service | 8088 | Patient management |
| Appointment Service | 8091 | Appointment scheduling |
| Medical Record Service | 8092 | Medical records |
| Billing Service | 8093 | Billing management |
| Dashboard Service | 8094 | Statistics & Analytics |

## Prerequisites

- Java 17 or higher
- MySQL 8.0
- Maven 3.6+

## Setup

### 1. Database Configuration

Create the required databases:

```sql
CREATE DATABASE identity_db;
CREATE DATABASE doctor_db;
CREATE DATABASE patient_db;
CREATE DATABASE appointment_db;
CREATE DATABASE medicalrecord_db;
CREATE DATABASE billing_db;
```

### 2. Start the Application

Run the startup script:
```cmd
run-all.cmd
```

Access the application at: `http://localhost:8080`

### 3. Stop the Application

Run the shutdown script:
```cmd
close-all.cmd
```

## Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Doctor | doctor | doctor123 |
| Patient | patient | patient123 |

## API Endpoints

All APIs are accessible through the Gateway at `http://localhost:8080`

- `POST /login` - User authentication
- `GET/POST /doctor` - Doctor operations
- `GET/POST /patient` - Patient operations
- `GET/POST /appointment` - Appointment operations
- `GET/POST /medicalrecord` - Medical record operations
- `GET/POST /bill` - Billing operations
- `GET /dashboard/stats` - System statistics

## Project Structure

```
CareSync/
├── microservices/
│   ├── discovery-server/
│   ├── gateway/
│   ├── identity-service/
│   ├── doctor-service/
│   ├── patient-service/
│   ├── appointment-service/
│   ├── medical-record-service/
│   ├── billing-service/
│   └── dashboard-service/
├── run-all.cmd
└── close-all.cmd
```

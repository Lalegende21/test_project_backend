# 📁 Document Management API

## Secure, QR Code–Driven & Collaborative Backend (Spring Boot 4)

### 🚀 Project Overview
This project is a production-ready Document Management REST API built with Spring Boot 4 and PostgreSQL, designed to handle secure document workflows, collaborative access control, and automatic document classification using QR Codes.

* The backend focuses on:
* Security-first design
* Fine-grained permissions
* Clean architecture
* Real-world business logic
This is not a demo project — it reflects enterprise-grade backend patterns.


### 🎯 Key Business Features

* Secure authentication using JWT stored in HttpOnly cookies
* Collaborative document access with roles & permissions
* Hierarchical classification system (unlimited depth)
* Automatic document capture & classification via QR Code
* Intelligent document validation workflow
* File server with controlled access
* User-based statistics and tracking


### 🛡️ Authentication & Security
#### Authentication
* JWT-based authentication
* Token stored in HttpOnly cookies (XSS protection)
* SameSite=Lax cookies (CSRF mitigation)
* Stateless API (no server sessions)
* Password hashing with BCrypt

#### Roles
* USER – Standard user
* ADMIN – Full system access

#### Security Stack
* Spring Security 7
* Custom JWT authentication filter
* Centralized exception handling
* Input validation (Jakarta Validation)
* Path traversal prevention
* Secure file handling


### 👥 Advanced Document Permissions
#### Document Roles
* OWNER
* EDITOR
* CONTRIBUTOR
* VALIDATOR
* VIEWER

#### Permissions
VIEW, EDIT, UPLOAD, DELETE, VALIDATE, MANAGE

#### Rules Enforced
* Only the OWNER can manage access
* OWNER role cannot be modified or removed
* ADMIN has full access to all documents
* Permissions verified automatically before every action
This system mirrors real enterprise document collaboration models.


### 📂 Classification System

#### Classification Plans & Folders
* Hierarchical folder structure (recursive, unlimited depth)
* Root folders & subfolders
* Strong data consistency rules
* Cascade deletion with orphan management

#### Folder Contents
* Business-defined content types (e.g. Invoice, Contract)
* Mandatory / optional content rules
* Many-to-Many folder ↔ content relationship
* Unique QR Code generated per content


### 🔲 QR Code–Driven Automation

QR Codes are used as business identifiers.

#### Capabilities
* QR generation (PNG, Base64, ZIP)
* High error correction (ZXing)
* Automatic content detection during upload
* Zero manual classification

#### QR Format
```js
CONTENT : {contentId}
```
This enables fast physical-to-digital document workflows.


### 📄 Document Management Workflow

#### Document Lifecycle
* BROUILLON – Created, no files
* EN_COURS – At least one file uploaded
* VALIDE – All mandatory files present

### Metadata
* Stored as JSONB (PostgreSQL)
* Flexible, schema-free extension
* Ideal for evolving business requirements


### 📎 File Upload & Automatic Capture

#### Upload Capabilities
* Single or batch upload
* Accepted formats: PDF, JPG, PNG
* Max size: 10MB
* QR Code auto-detection
* Secure file naming & storage

#### Validation Rules
* Upload disabled on validated documents
* Permission-based access (UPLOAD)
* Automatic content matching via QR Code


### ✅ Document Validation Logic

Before validation:
* Required contents are resolved from classification
* Uploaded pieces are checked
* Missing items returned explicitly

This ensures business completeness, not just technical success.

### 📊 User Statistics

Each user can track:
* Total documents
* Draft / In-progress / Validated documents
* Total uploaded pieces

All statistics are permission-aware.


### 🧱 Architecture & Code Quality
* Layered architecture (Controller / Service / Repository)
* DTO-based API contracts
* Centralized error handling
* Transactional boundaries
* Lazy loading optimized
* Clean separation of concerns

This codebase is maintainable, testable, and scalable.

### 🛠️ Tech Stack
* Spring Boot 4.0.1
* Spring Security 7
* Hibernate 7 / JPA
* PostgreSQL + JSONB
* JWT (jjwt)
* ZXing (QR Codes)
* Lombok
* Jakarta Validation

### 🧠 What This Project Demonstrates
* Real-world backend design skills
* Strong understanding of security
* Advanced access control modeling
* Complex business workflows
* Clean Spring Boot architecture
* Production-oriented mindset

### ▶️ How to Run Locally
This section explains how to run the backend locally for development or evaluation purposes.

#### ✅ Prerequisites
Make sure you have the following installed:
* Java 21+
* Maven 3.9+
* PostgreSQL 14+
* Git

#### 📦 Clone the Repository
```bash
git clone https://github.com/Lalegende21/test_project_backend.git
cd document-management-api
```

#### 🗄️ Database Setup (PostgreSQL)
Create a PostgreSQL database:
```sql
CREATE DATABASE classification;
```

#### ⚙️ Application Configuration
Edit application.yml:
```yaml
server:
port: 8080
servlet:
context-path: /api

spring:
datasource:
url: jdbc:postgresql://localhost:5432/classification
username: doc_user
password: password

jpa:
hibernate:
ddl-auto: update
show-sql: false

servlet:
multipart:
max-file-size: 10MB
max-request-size: 10MB

jwt:
secret: YOUR_BASE64_SECRET_KEY
expiration: 86400000

app:
file-storage-path: documents/

cors:
allowed-origins: http://localhost:3000
```

⚠️ The JWT secret must be Base64 encoded.
Example:
```bash
openssl rand -base64 64
```

#### ▶️ Run the Application
Using Maven:
```bash
mvn clean spring-boot:run
```

Or using the packaged JAR:
```bash
mvn clean package
java -jar target/document-management-api.jar
```

#### 🌐 API Access
Once started, the API is available at:
```bash
http://localhost:8080/api
```

Example health check:
```bash
GET http://localhost:8080/api/health
```

#### 📂 File Storage
Uploaded files are stored locally in:
```bash
/documents
```

Ensure the directory exists and is writable:
```bash
mkdir documents
chmod 755 documents
```

#### 🔐 Authentication Flow (Local)
1. Register a user
POST /auth/register

Or login directly with this credentials
* user:
```json
* {
"username": "user",
"password": "userPassword"
}
```
* admin
```json
{
"username": "admin",
"password": "adminPassword"
}
```
2. Login
POST /auth/login
→ JWT stored in HttpOnly cookie
3. Access secured endpoints
→ Cookie automatically sent by browser or HTTP client

#### 🧪 Testing with Postman / REST Client
* Enable cookies support
* No need to manually copy JWT
* CORS configured for http://localhost:5174
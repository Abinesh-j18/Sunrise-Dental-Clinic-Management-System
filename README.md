# Sunrise Dental Clinic Management System
**University Coursework — CIS6003 Advanced Programming (Task B)**

A computerized 3-tier distributed appointment and patient management system designed for Sunrise Dental Clinic staff in Colombo, resolving paper-based challenges (double bookings, lost patient records, and billing errors).

---

## 1. System Architecture (3-Tier Distributed System)

```
┌────────────────────────────────────────────────────────┐
│  Tier 1: Desktop Client (Java Swing + FlatLaf)        │
│  - Communicates STRICTLY over HTTP/JSON via java.net   │
│  - Uses SwingWorker for all asynchronous I/O           │
│  - Responsive (1440x900 default, fluid down to 1024x768│
└───────────────────────────┬────────────────────────────┘
                            │ REST / HTTP (JSON + Bearer Token)
                            ▼
┌────────────────────────────────────────────────────────┐
│  Tier 2: REST Service Layer (HttpServer + ThreadPool)  │
│  - Built-in com.sun.net.httpserver.HttpServer          │
│  - Executors.newFixedThreadPool(10) concurrency        │
│  - Factory Method Pattern (ReportFactory)              │
│  - JavaMail API Async Email Notification Service       │
│  - Token-Based In-Memory Session Security              │
└───────────────────────────┬────────────────────────────┘
                            │ JDBC via PreparedStatement/CallableStatement
                            ▼
┌────────────────────────────────────────────────────────┐
│  Tier 3: Data Access & MySQL Database (XAMPP)          │
│  - Singleton DatabaseConnectionManager                 │
│  - TRIGGER: Auto-generates APT-YYYY-XXXX number        │
│  - STORED PROCEDURE: CalculateInvoiceTotal             │
│  - FUNCTION: CheckDentistAvailability (No Overlaps)    │
└────────────────────────────────────────────────────────┘
```

---

## 2. Default Clinic Staff Accounts

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full administrative & analytics view |
| **Receptionist** | `receptionist1` | `admin123` | Patient booking, display, & billing |
| **Dentist** | `dr.silva` | `admin123` | Clinical schedule & treatment view |
| **Dentist** | `dr.perera` | `admin123` | Clinical schedule & treatment view |
| **Dentist** | `dr.fernando` | `admin123` | Clinical schedule & treatment view |

---

## 3. Database Setup (MySQL via XAMPP)

1. Start Apache and MySQL in the XAMPP Control Panel.
2. Import `database/schema.sql` into phpMyAdmin or run:
   ```cmd
   mysql -u root < database/schema.sql
   ```
3. Database `sunrisedb` includes:
   - `trg_before_insert_appointment` (Trigger)
   - `CalculateInvoiceTotal` (Stored Procedure)
   - `CheckDentistAvailability` (Stored Function)
   - Seed data for users, clinicians, treatments, and sample patients.

---

## 4. Building & Running the Application

### Running via NetBeans:
- Open the project `dental` in Apache NetBeans and click **Run Project** (F6).

### Running via Command Line (Ant):
```cmd
ant clean compile test jar
java -jar dist/dental.jar
```

### Running Standalone REST Server:
```cmd
java -cp dist/dental.jar dental.server.SunriseServer
```

---

## 5. TDD & Automated Testing

Run the full JUnit 4 test suite (28 tests across DAO, Service, and E2E tiers):
```cmd
ant test
```

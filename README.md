# Subscription Billing System (Backend)

A **production-oriented backend system** for handling **subscription-based recurring billing**, inspired by real-world payment platforms like Stripe.  
This project focuses on **correctness, reliability, and clean backend architecture**, not frontend polish.

---

## 🎯 Project Motive

Most billing projects online are shallow demos.  
This project is different.

The goal is to **design and implement a real subscription billing engine** that handles:

- recurring billing cycles
- invoices
- payment attempts
- retries with exponential backoff
- proration-ready design
- failure handling
- auditability

This project is built to:
- demonstrate **backend engineering depth**
- show **clean domain modeling**
- reflect **real-world system design thinking**
- be understandable and extensible by other developers

---

## 🧠 Key Engineering Principles

- **Modular Monolith Architecture** (not premature microservices)
- **Clear domain boundaries**
- **Database-driven correctness**
- **UTC-safe time handling**
- **Money-safe calculations**
- **No deprecated APIs**
- **Cloud-ready PostgreSQL design**

---

## 🏗 Architecture Overview

This is a **modular monolith** built with Spring Boot.

Each domain owns its own:
- controller (API layer)
- service (business logic)
- repository (data access)
- entities (persistence models)

## Controller → Service → Repository → Database 

No business logic leaks into controllers.  
No database logic leaks into services.

---

## 🧩 Current Domain Modules Implemented

### ✅ User
Represents a billing customer.

- UUID primary key (DB-generated)
- Email + status
- Audit timestamps

---

### ✅ Subscription (Design-ready)
Represents an active or inactive subscription to a plan.

- Linked to a user
- Tracks billing periods
- Designed for upgrades/downgrades & proration

---

### ✅ Invoice
Represents a billing cycle charge.

- One subscription → many invoices
- Money-safe `BigDecimal`
- Billing period tracking
- Proration support
- Paid / unpaid lifecycle

---

### ✅ Payment
Represents a payment attempt.

- Linked to an invoice
- Stripe PaymentIntent reference
- Tracks attempts & last attempt time
- Designed for idempotent retries

---

### ✅ Retry Queue
Handles failed payment retries.

- Exponential backoff ready
- Retry limits enforced
- Persistent retry state (survives restarts)

---

## 🗄 Database Design

- **Cloud PostgreSQL (Supabase)**
- UUID primary keys (`gen_random_uuid()`)
- `timestamptz` for all timestamps
- Explicit foreign key relationships
- Schema-first design (Flyway ready)

### Tables Implemented
- `users`
- `subscriptions`
- `invoices`
- `payments`
- `retry_queue`

---

## ⏱ Time & Money Handling (Critical)

- All timestamps stored in **UTC**
- Java uses `Instant`
- Monetary values use **`BigDecimal`**
- No floating-point arithmetic for billing

---

## 🔁 Retry Strategy (Designed)

Failed payments are:
1. Recorded
2. Added to retry queue
3. Retried using exponential backoff
4. Stopped after max retries
5. Marked for cancellation if unresolved

This mirrors real billing systems.

---

## 🧪 Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Hibernate 6**
- **PostgreSQL (Supabase)**
- **Spring Data JPA**
- **Flyway (planned)**
- **Lombok (safe usage only)**

---

## 🚧 What This Project Is NOT

- ❌ Not a CRUD demo
- ❌ Not frontend-focused
- ❌ Not microservices for buzzwords
- ❌ Not a Stripe wrapper clone

It is a **billing engine**, not a UI app.

---

## 🛣 Roadmap (Planned)

- Billing scheduler (recurring cycles)
- Stripe integration
- Webhook handling
- Proration calculation logic
- Subscription upgrades/downgrades
- API documentation
- Integration tests

---

## 👨‍💻 Who This Project Is For

- Backend engineers
- Recruiters evaluating system design skills
- Developers learning real billing systems
- Anyone interested in clean backend architecture

---

## 📌 Author

**Binod Biswal**  
Software Engineer | Backend & Cloud Enthusiast  

- GitHub: https://github.com/Binodsy  
- LinkedIn: https://linkedin.com/in/binod-biswal  

---

> This project prioritizes **engineering correctness over shortcuts**,  
> because billing systems don’t forgive mistakes.

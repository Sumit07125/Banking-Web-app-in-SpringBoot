# 🏦 Modern Banking System

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-green?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.0-blue?style=for-the-badge&logo=react)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

**A secure, full-stack banking application featuring a 3D immersive UI, real-time transaction processing, email notifications, and robust admin controls.**

[View Demo](#) · [Report Bug](https://github.com/Sumit07125/bankingsystem/issues) · [Request Feature](https://github.com/Sumit07125/bankingsystem/issues)

</div>

---

## 📖 Table of Contents

- [About the Project](#-about-the-project)
- [UI Preview](#-ui-preview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [Roadmap](#-roadmap)
- [Contact](#-contact)
- [Support](#-support)

---

## ✨ About the Project

The **Modern Banking System** is an industry-level full-stack application designed to simulate core banking operations with a focus on security, user experience, and modern web technologies.

Unlike traditional banking apps, this project integrates **GSAP animations** and **Three.js** 3D elements to create an engaging frontend, while relying on a robust **Spring Boot** backend for secure transaction handling, OTP authentication, and automated email notifications via SMTP.

**Who is this for?**
* **Developers** looking for a reference architecture for Spring Boot + React integration.
* **Students** needing a Capstone project with advanced features.
* **Recruiters** evaluating full-stack proficiency in Java and modern JavaScript.

---

## 🖼️ UI Preview

<div align="center"> 
  <img src="Screenshot 2025-12-11 180241.png" alt="Banking Dashboard UI" width="800" style="border-radius: 10px; box-shadow: 0px 4px 20px rgba(0,0,0,0.5);">
  <p><em>Interactive User Dashboard with Real-time Balance & Quick Actions</em></p>
  <br>
  <img src="Screenshot 2025-12-11 183243.png" alt="Transaction History" width="800" style="border-radius: 10px; box-shadow: 0px 4px 20px rgba(0,0,0,0.5);">
  <p><em>Comprehensive Transaction History & Statement Generation</em></p>
</div>

---

## 🔥 Key Features

### 👤 User Panel
* **Secure Authentication:** User login with PIN verification and session management.
* **Core Banking:** Deposit, Withdraw, and Transfer funds seamlessly.
* **Real-time Dashboard:** View live balance, recent transactions, and account details.
* **Digital Statements:** View mini-statements or download full transaction history as **CSV/PDF**.
* **Loan Management:** Apply for loans and view EMI schedules.
* **Debit Card:** View virtual debit card details with CVV masking.

### 🛡️ Security & Notifications
* **OTP Verification:** Email-based One-Time Passwords for sensitive actions (Delete Account, etc.).
* **Email Alerts:** Instant notifications for Credit, Debit, Loan Disbursal, and Account Creation via Gmail SMTP.
* **Role-Based Access:** Distinct flows for Users and Administrators.

### ⚡ Technical Highlights
* **Immersive UI:** Powered by `React Three Fiber` and `Locomotive Scroll` for smooth 3D effects.
* **Animations:** High-performance animations using `GSAP`.
* **PDF Generation:** Client-side PDF generation using `jspdf-autotable`.

---
## 🛠️ Tech Stack

| Category | Technology |
| --- | --- |
| **Frontend** | React 19, Three.js (@react-three/fiber), GSAP, Tailwind CSS, Axios |
| **Backend** | Java 17, Spring Boot 3.5.7, Spring Security (Custom), JavaMailSender |
| **Database** | MySQL 8.4, Spring Data JPA, Hibernate |
| **Tools** | Maven, npm, Git, Postman |
| **Testing** | JUnit 5, Spring Boot Test |

---

## 🚀 Getting Started

Follow these steps to set up the project locally.

### Prerequisites

* **Java JDK 17+**
* **Node.js & npm**
* **MySQL Server**
* **Maven**

### 1. Database Setup

Execute the following SQL script in your MySQL workbench or CLI:

```sql
CREATE DATABASE bank_manage;
USE bank_manage;
-- The application will auto-generate tables via Hibernate ddl-auto

```

### 2. Backend Setup

1. Navigate to the project root.
2. Update `src/main/resources/application.properties` with your credentials:
```properties
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_APP_PASSWORD

```


3. Run the application:
```bash
mvn spring-boot:run

```



### 3. Frontend Setup

1. Navigate to the frontend directory:
```bash
cd bankingsystem/frontend

```


2. Install dependencies:
```bash
npm install

```


3. Start the development server:
```bash
npm start

```


4. Open `http://localhost:3000` in your browser.

---

## 📂 Project Structure

```bash
bankingsystem/
├── src/main/java/org/example/bankingsystem/
│   ├── config/          # Web & Security Configurations
│   ├── controller/      # REST & View Controllers
│   ├── model/           # JPA Entities (Account, Transaction, Loan)
│   ├── repository/      # Data Access Interfaces
│   └── service/         # Business Logic (Email, OTP, Banking Ops)
├── src/main/resources/
│   ├── templates/       # Thymeleaf templates (legacy views)
│   └── application.properties
├── frontend/
│   ├── src/
│   │   ├── components/  # React Components (Dashboard, Login, 3D)
│   │   ├── context/     # Auth Context API
│   │   ├── services/    # Axios API calls
│   │   └── utils/       # PDF Generator helpers
│   └── package.json
├── pom.xml              # Maven Dependencies
└── README.md

```

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📌 Roadmap

* [x] Core Banking Modules (Deposit, Withdraw, Transfer)
* [x] Email Notification System
* [x] 3D Animated Landing Page
* [x] Admin Dashboard
* [ ] Integration with real Payment Gateway (Stripe/Razorpay)
* [ ] Mobile App (React Native)
* [ ] AI Chatbot for Customer Support
* [ ] Microservices Refactoring

---

## 📬 Contact

<div align="center">

**Sumit Mali**

<a href="mailto:sumitmali07125@gmail.com">
  <img src="https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white" alt="Gmail" />
</a>
<a href="https://github.com/Sumit07125">
  <img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" />
</a>
<a href="https://www.linkedin.com/in/sumitmali07/">
  <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" />
</a>

</div>

---

## ⭐ Support

If you found this project helpful or interesting, please give it a **Star** ⭐️!

---

<div align="center">
<h3>Built with ❤️ by Sumit</h3>
</div>

## ⚙️ System Architecture

The application follows a **Monolithic Client–Server Architecture** (Frontend served as static assets or via proxy), ensuring data consistency and ease of deployment.

```mermaid
graph TD
    A["Client (React 19)"] -->|REST API| B["Spring Boot Controllers"]
    B -->|Business Logic| C["Service Layer"]
    C -->|Uses| R["Repository Layer"]
    R -->|JPA / Hibernate| D[("MySQL Database")]
    C -->|SMTP| E["Email Server (Gmail)"]

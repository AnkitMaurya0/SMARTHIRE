<div align="center">

# 🤖 SmartHire
### AI-Based Resume Screening Job Portal

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Python](https://img.shields.io/badge/Python-3.x-yellow?style=for-the-badge&logo=python)
![Apache Tika](https://img.shields.io/badge/Apache%20Tika-2.9.1-red?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)

**SmartHire** is an intelligent job portal that uses AI and Machine Learning to automatically
screen resumes, calculate ATS scores, and rank candidates based on job requirements.

[Features](#-features) •
[Architecture](#-architecture) •
[Database](#-database-design) •
[Installation](#-installation) •
[Usage](#-usage) •
[API](#-project-structure) •
[Contributors](#-contributors)

</div>

---

## 📌 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Database Design](#-database-design)
- [Project Structure](#-project-structure)
- [Installation & Setup](#-installation--setup)
- [Usage Guide](#-usage-guide)
- [ML / ATS Scoring System](#-ml--ats-scoring-system)
- [Testing](#-testing)
- [Screenshots](#-screenshots)
- [Contributors](#-contributors)

---

## 📖 About the Project

SmartHire is a full-stack web application built with **Java Spring Boot** that automates
the hiring process for companies. HR professionals can post jobs, and students can apply
by uploading their resumes. The system automatically parses resumes using **Apache Tika**,
calculates an **ATS (Applicant Tracking System) score** using a **Python ML script**,
and ranks candidates based on their match with the job requirements.

### 🎯 Problem Statement

Traditional hiring processes are slow, manual, and biased.
HR teams spend hours reading hundreds of resumes for a single job posting.
SmartHire automates this process by intelligently screening resumes and
providing objective scores based on multiple factors.

### 💡 Solution

- Automatic resume parsing (PDF, DOC, DOCX)
- ML-based ATS scoring with 5 weighted factors
- Candidate ranking by score
- Real-time HR-Student messaging
- PDF report generation
- Google Meet interview scheduling

---

## ✨ Features

### For HR / Companies
| Feature | Description |
|---|---|
| 📋 Job Posting | Post jobs with required skills, experience, vacancies |
| 👥 Candidate Ranking | View candidates ranked by ATS score |
| 📊 ATS Score Breakdown | See matched and missing skills for each candidate |
| 🔄 Status Management | Update application status (Applied → Interview → Selected) |
| 💬 Direct Messaging | Message candidates directly through the platform |
| 📹 Interview Scheduling | Send Google Meet links to shortlisted candidates |
| 📥 Resume Download | Download candidate resumes directly |

### For Students
| Feature | Description |
|---|---|
| 🔍 Job Search | Browse and search jobs by title, skills, location |
| 📄 Resume Upload | Apply with PDF/DOC/DOCX resume |
| 📈 ATS Score | Get instant ATS score after applying |
| 🎯 ATS Checker | Check resume score against any job before applying |
| 📥 PDF Report | Download detailed ATS report with improvement tips |
| 💬 Messaging | Communicate with HR through platform |
| 📊 Application Tracker | Track all applications and their statuses |

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Primary programming language |
| Spring Boot | 3.3.0 | Web framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM |
| Apache Tika | 2.9.1 | Resume parsing (PDF/DOC) |
| iTextPDF | 5.5.13 | PDF report generation |
| Hibernate | 6.5.2 | ORM implementation |
| Maven | 3.9.x | Dependency management |

### Frontend
| Technology | Purpose |
|---|---|
| Thymeleaf | Server-side HTML templating |
| HTML5 / CSS3 | Structure and styling |
| JavaScript | Client-side interactions |

### Database
| Technology | Version | Purpose |
|---|---|---|
| MySQL | 8.0 | Primary relational database |
| HikariCP | 5.1.0 | Connection pooling |

### ML / Python
| Technology | Purpose |
|---|---|
| Python 3.x | ML script execution |
| scikit-learn concepts | TF-IDF inspired scoring |
| JSON | Data exchange between Java and Python |

---

## 🏗 Architecture
```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT BROWSER                       │
│              (HTML + CSS + JavaScript)                   │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP Requests
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  SPRING BOOT SERVER                      │
│                                                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │    Auth     │  │     HR      │  │    Student      │ │
│  │ Controller  │  │ Controller  │  │   Controller    │ │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘ │
│         │                │                   │          │
│  ┌──────▼──────────────────────────────────▼─────────┐  │
│  │              SERVICE LAYER                        │  │
│  │  UserService │ JobService │ ATSService │ MsgService│  │
│  └──────┬──────────────────────────────────┬─────────┘  │
│         │                                  │            │
│  ┌──────▼──────────────────────────────────▼─────────┐  │
│  │            REPOSITORY LAYER (JPA)                 │  │
│  └──────┬──────────────────────────────────┬─────────┘  │
│         │                                  │            │
│  ┌──────▼──────┐              ┌────────────▼──────────┐ │
│  │    MySQL    │              │   Python ML Script    │ │
│  │  Database   │              │   (ats_scorer.py)     │ │
│  └─────────────┘              └───────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Request Flow
```
User Request
    │
    ▼
Spring Security (Authentication Check)
    │
    ▼
Controller (Route Handler)
    │
    ▼
Service Layer (Business Logic)
    │
    ├──► Repository → MySQL Database
    │
    └──► ATSService → Python Script → JSON Response
                           │
                           ▼
                    ATS Score Calculated
                           │
                           ▼
                    Saved to Database
                           │
                           ▼
                    Response to User
```

---

## 🗄 Database Design

### Entity Relationship Diagram
```
┌──────────────────┐         ┌──────────────────────┐
│      users       │         │        jobs          │
├──────────────────┤         ├──────────────────────┤
│ id (PK)          │◄────────│ id (PK)              │
│ name             │  1    N │ title                │
│ email (UNIQUE)   │         │ description          │
│ password         │         │ required_skills      │
│ role (HR/STUDENT)│         │ experience_required  │
│ phone            │         │ vacancies            │
│ company          │         │ location             │
│ designation      │         │ job_type             │
│ college          │         │ salary_range         │
│ degree           │         │ posted_date          │
│ passing_year     │         │ closing_date         │
│ skills           │         │ hr_id (FK → users)   │
└──────────────────┘         └──────────────────────┘
         │                              │
         │ 1                            │ 1
         │                              │
         │ N                            │ N
         ▼                              ▼
┌──────────────────────────────────────────────────┐
│                   applications                    │
├──────────────────────────────────────────────────┤
│ id (PK)                                          │
│ student_id (FK → users)                          │
│ job_id (FK → jobs)                               │
│ resume_path                                      │
│ extracted_text (LONGTEXT)                        │
│ ats_score                                        │
│ matched_skills (TEXT)                            │
│ missing_skills (TEXT)                            │
│ status                                           │
│ applied_date                                     │
└──────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────┐
│                    messages                       │
├──────────────────────────────────────────────────┤
│ id (PK)                                          │
│ sender_id (FK → users)                           │
│ receiver_id (FK → users)                         │
│ content (TEXT)                                   │
│ meet_link                                        │
│ is_read                                          │
│ sent_at                                          │
│ job_id (FK → jobs)                               │
└──────────────────────────────────────────────────┘
```

### Tables Summary

| Table | Rows Description |
|---|---|
| `users` | Stores both HR and Student accounts with role differentiation |
| `jobs` | All job postings created by HR users |
| `applications` | Student job applications with ATS scores |
| `messages` | Real-time messages between HR and Students |

### MySQL Setup
```sql
-- Create database
CREATE DATABASE smarthire_db;
USE smarthire_db;

-- Tables are auto-created by Spring JPA (ddl-auto=update)
```

---

## 📁 Project Structure
```
SmartHire/
│
├── src/main/java/com/smarthire/
│   ├── SmartHireApplication.java        ← Main entry point
│   │
│   ├── config/
│   │   ├── SecurityConfig.java          ← Spring Security config
│   │   └── AppConfig.java               ← Password encoder bean
│   │
│   ├── controller/
│   │   ├── AuthController.java          ← /login /register /dashboard
│   │   ├── HRController.java            ← /hr/**
│   │   ├── StudentController.java       ← /student/**
│   │   └── MessageController.java       ← /messages/**
│   │
│   ├── model/
│   │   ├── User.java                    ← User entity
│   │   ├── Job.java                     ← Job entity
│   │   ├── Application.java             ← Application entity
│   │   └── Message.java                 ← Message entity
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── JobRepository.java
│   │   ├── ApplicationRepository.java
│   │   └── MessageRepository.java
│   │
│   └── service/
│       ├── UserService.java             ← UserDetailsService impl
│       ├── JobService.java
│       ├── ATSService.java              ← Resume parsing + ML scoring
│       └── MessageService.java
│
├── src/main/resources/
│   ├── templates/
│   │   ├── index.html                   ← Home page
│   │   ├── auth/
│   │   │   ├── login.html
│   │   │   └── register.html
│   │   ├── hr/
│   │   │   ├── dashboard.html
│   │   │   ├── post-job.html
│   │   │   ├── view-jobs.html
│   │   │   └── ranked-candidates.html
│   │   ├── student/
│   │   │   ├── dashboard.html
│   │   │   ├── jobs.html
│   │   │   ├── apply-job.html
│   │   │   ├── applied-jobs.html
│   │   │   └── ats-result.html
│   │   └── messages/
│   │       ├── inbox.html
│   │       └── chat.html
│   │
│   ├── static/
│   │   ├── css/style.css
│   │   └── js/main.js
│   │
│   └── application.properties
│
├── python-ml/
│   ├── ats_scorer.py                    ← ML ATS scoring script
│   └── requirements.txt
│
├── uploads/resumes/                     ← Uploaded resume files
├── pom.xml
└── README.md
```

---

## ⚙ Installation & Setup

### Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 21 |
| Maven | 3.9.x |
| MySQL | 8.0 |
| Python | 3.x |
| VS Code | Latest |

### Step 1 — Clone Repository
```bash
git clone https://github.com/AnkitMaurya0/smarthire.git
cd smarthire
```

### Step 2 — MySQL Setup
```sql
CREATE DATABASE smarthire_db;
```

### Step 3 — Configure application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smarthire_db
spring.datasource.username=root
spring.datasource.password=your_password

python.script.path=python-ml/ats_scorer.py
python.executable=python
```

### Step 4 — Run the Application
```bash
# Windows PowerShell
$env:JAVA_HOME="C:\Path\To\JDK21"
$env:PATH="C:\Path\To\JDK21\bin;$env:PATH"
mvn spring-boot:run
```

### Step 5 — Access Application
```
http://localhost:8080
```

---

## 📱 Usage Guide

### HR Workflow
```
1. Register as HR → Enter company details
2. Login → Redirected to HR Dashboard
3. Post Job → Add title, skills, vacancies
4. View Applications → See ranked candidates
5. Update Status → Applied → Interview → Selected
6. Message Candidate → Send Google Meet link
```

### Student Workflow
```
1. Register as Student → Enter college, degree details
2. Login → Redirected to Student Dashboard
3. Browse Jobs → Search by title/skills/location
4. Apply → Upload resume (PDF/DOC/DOCX)
5. View ATS Score → See matched/missing skills
6. ATS Checker → Check resume against any job
7. Download Report → Get PDF with improvement tips
8. Message HR → Communicate through platform
```

---

## 🤖 ML / ATS Scoring System

### How ATS Score is Calculated

The ATS score is calculated using a **Python ML script** that evaluates
5 different factors with weighted scoring:
```
Final Score = (Skills × 40%) +
              (Education × 20%) +
              (Experience × 15%) +
              (Keywords × 15%) +
              (Resume Quality × 10%)
```

### Factor Details

| Factor | Weight | What is Checked |
|---|---|---|
| Skills Match | 40% | Required skills found in resume |
| Education Match | 20% | Degree, college keywords present |
| Experience Match | 15% | Projects, internship, work experience |
| Keyword Match | 15% | Technical keywords overlap |
| Resume Quality | 10% | Contact, sections, completeness |

### Score Labels

| Score Range | Label |
|---|---|
| 75% - 100% | Excellent Match |
| 55% - 74% | Good Match |
| 35% - 54% | Average Match |
| 0% - 34% | Low Match |

### Java ↔ Python Integration
```java
// ATSService.java calls Python script via ProcessBuilder
ProcessBuilder pb = new ProcessBuilder(
    "python", "python-ml/ats_scorer.py",
    resumeText, requiredSkills
);
// Python returns JSON → Java parses score
```
```python
# ats_scorer.py returns JSON
{
    "score": 65.5,
    "skills_score": 80.0,
    "education_score": 60.0,
    "experience_score": 55.0,
    "keyword_score": 70.0,
    "quality_score": 50.0,
    "matched": ["java", "python"],
    "missing": ["spring boot", "mysql"],
    "label": "Good Match"
}
```

---

## 🧪 Testing

### Manual Testing Checklist

#### Authentication
- [x] Student Registration with all fields
- [x] HR Registration with company details
- [x] Login with correct credentials
- [x] Login with wrong credentials — error message
- [x] Role-based redirect after login
- [x] Logout functionality

#### HR Features
- [x] Post new job with all details
- [x] View all posted jobs
- [x] View ranked candidates by ATS score
- [x] Update candidate status
- [x] Message student from ranked candidates
- [x] Send Google Meet link

#### Student Features
- [x] Browse all available jobs
- [x] Search jobs by keyword
- [x] Apply with PDF resume
- [x] Apply with DOCX resume
- [x] Duplicate application prevention
- [x] View ATS score after applying
- [x] ATS Checker with job selection
- [x] Download PDF report
- [x] View application status updates
- [x] Message HR

#### Messaging
- [x] Send message HR to Student
- [x] Send message Student to HR
- [x] Inbox shows all conversations
- [x] Unread message count on dashboard
- [x] Google Meet link in chat

### Test Credentials
```
HR Account:
Email: hr@tcs.com
Password: test123

Student Account:
Email: student@test.com
Password: test123
```

---

## 📸 Screenshots

### Home Page
> AI-powered job portal landing page with features overview

### HR Dashboard
> Job statistics, posted jobs list, unread message count

### Student Dashboard
> Applications tracker, ATS scores, message notifications

### Ranked Candidates
> Candidates sorted by ATS score with matched/missing skills

### Chat System
> Real-time messaging with Google Meet link support

### ATS Score Result
> Detailed score breakdown with improvement suggestions

### PDF Report
> Downloadable ATS report with all details

---

## 🔒 Security

- **BCrypt** password encryption
- **Spring Security** role-based access control
- **HR routes** (`/hr/**`) — accessible only by HR role
- **Student routes** (`/student/**`) — accessible only by Student role
- **CSRF protection** enabled
- **Session management** via Spring Security

---

## 🚀 Deployment

The application is deployed and accessible at:

**[SmartHire - AI Resume Screening Portal](https://your-deployed-url.com)**

### Deployment Stack
- **Backend:** Railway / Render
- **Database:** MySQL (Cloud)
- **Frontend:** Served by Spring Boot (Thymeleaf)

---

## 🤝 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/AnkitMaurya0">
        <img src="https://github.com/AnkitMaurya0.png"
             width="100px"
             style="border-radius:50%"
             alt="Ankit Maurya"/>
        <br/>
        <strong>Ankit Maurya</strong>
      </a>
      <br/>
      <sub>Backend Development</sub>
      <br/>
      <sub>Java Spring Boot • Spring Security</sub>
      <br/>
      <sub>Database Design • Deployment</sub>
    </td>
    <td align="center">
      <a href="https://github.com/sonu4265kumar">
        <img src="https://github.com/sonu4265kumar.png"
             width="100px"
             style="border-radius:50%"
             alt="Sonu Kumar"/>
        <br/>
        <strong>Sonu Kumar</strong>
      </a>
      <br/>
      <sub>Frontend Development</sub>
      <br/>
      <sub>HTML • CSS • JavaScript</sub>
      <br/>
      <sub>UI/UX Design • Thymeleaf</sub>
    </td>
  </tr>
</table>

---

## 📄 License

This project is licensed under the **MIT License**.
```
MIT License

Copyright (c) 2026 Ankit Maurya & Sonu Kumar

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software.
```

---

<div align="center">



⭐ Star this repository if you found it helpful !

</div>

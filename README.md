# 🚀 API Automation Framework (Runtime API Tracking)

## 📌 Overview

This project is a **real-time API Automation & Monitoring Framework** built using Java, Selenium, and Chrome DevTools Protocol (CDP).

It captures and analyzes network API calls during UI execution, enabling deep visibility into backend behavior without modifying application code.

---

## 🎯 Key Objectives

* Track APIs across different application modules
* Validate API response status codes
* Measure API performance (response time)
* Generate detailed Excel reports
* Automate report distribution via Email
* Store reports centrally (OneDrive)

---

## 🛠️ Tech Stack

* Java
* Selenium WebDriver
* Chrome DevTools Protocol (CDP)
* TestNG
* Apache POI
* SMTP
* Maven

---

## ⚙️ Key Features

### ✅ Real-Time API Capture

* Captures APIs using Chrome DevTools
* Tracks URL, Method, Status Code, Response Time

### ✅ Smart API Filtering

* Captures: XHR, Fetch
* Ignores: CSS, JS, Images, WebSockets

### ✅ Module-wise Tracking

* Login
* Element Repository
* Test Case
* Execution
* Reports

### ✅ Excel Report Generation

* Build Info Sheet
* Summary Sheet
* Module-wise Sheets

### ✅ Response Validation

* 200 → Success
* 300 → Redirection
* 400 → Client Error
* 500 → Server Error

### ✅ OneDrive Integration

* Stores reports date-wise
* Auto file naming

### ✅ Email Automation

* Sends report via SMTP
* Supports multiple users

---

## 🧩 Project Structure

api_tracking/
│
├── Base/
├── Tests/
├── Pages/
├── Utils/

---

## ▶️ Execution Flow

1. Launch application
2. Capture APIs using DevTools
3. Perform UI actions
4. Generate Excel report
5. Store in OneDrive
6. Send Email

---

## 🏃‍♂️ Run Command

mvn clean test

---

## 📈 Use Cases

* API monitoring
* Performance tracking
* Regression testing
* Debugging backend issues

---

## 💡 Key Achievements

* Reduced manual effort
* Improved debugging efficiency
* Built scalable automation framework

---

## 👨‍💻 Author

Prateek U H
Java Backend Developer

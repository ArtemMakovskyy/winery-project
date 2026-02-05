# 🍷 "Stoic winery" 
Welcome to the **Stoic Winery** project — a full-stack wine store application with **frontend**, backend, admin panel, and observability features.

## ▶️ How to set up and start the project

### Soft requirements

- Docker
    
- Postman
    
- Git
    
- Browser

---
### Installation

Clone the repository from GitHub:

`git clone https://github.com/ArtemMakovskyy/winery-project.git`

Start Docker.

If necessary, configure the database and Telegram Bot parameters in the `.env` file.  
By default, the Telegram bot is disabled and all required parameters are already set.  
No additional configuration is required.

Navigate to the root directory of the project and run:

**First start:**

`docker-compose up`

**Next starts:**

`docker-compose up --no-build`

## 🚀 How to use the application

### 1️⃣ Wine Store Frontend (Customer UI)

`http://localhost:3000/#/products`

---

### 2️⃣ Wine Store Backend API

Swagger UI:

`http://localhost:8080/api/swagger-ui/index.html#/`

You can also use the API via **Postman**.

---

### 3️⃣ Winery Admin Panel (UI microservice)

The **Admin Panel** is a separate UI application for internal management tasks.

Admin Panel URL:

`http://localhost:8081`

It provides:

- wine management (create, delete, image upload)
    
- order management (view, mark as paid, delete)
    
- user role management (administrator only)
    

**Login as ADMINISTRATOR**

- **Email:** `admin12345@gmail.com`
    
- **Password:** `12345`
- 
**Login as MANAGER

- **Email:** `manager12345@gmail.com`
    
- **Password:** `12345`

---
## 🧩 Project structure overview

- **wine-site-frontend** — customer-facing React application
    
- **stoic-winery-api** — main backend REST API
    
- **winery-admin-ui** — separate admin UI microservice
    
- **observability stack** — Prometheus, Grafana, Loki, Tempo
	- **Grafana:** `http://localhost:3030`
	- **Prometheus:** `http://localhost:9090`
	- **Loki:** `http://localhost:3100`
	- **Tempo / Zipkin:** `http://localhost:9411`   
    
---
## 📚 Detailed project documentation

👓 **Stoic Winery site frontend**  
[https://github.com/ArtemMakovskyy/winery-project/blob/master/wine_site_project/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/master/wine_site_project/README.md)

👓 **Stoic Winery API backend**  
[https://github.com/ArtemMakovskyy/winery-project/blob/master/stoic-winery-api/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/master/stoic-winery-api/README.md)

👓 **Stoic Winery ADMIN PANEL**  
[https://github.com/ArtemMakovskyy/winery-project/blob/winery-admin-ui/winery-admin-ui/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/winery-admin-ui/winery-admin-ui/README.md)

---

## 🛠 Tech Stack

- **Language:** Java 21
    
- **Framework:** Spring Boot 3.2.1
    
- **Architecture:** REST, Modular Monolith
    
- **Database:** MySQL, H2 (dev), Liquibase migrations
    
- **Security:** Spring Security, JWT
    
- **Integration:** Telegram Bots
    
- **Observability:** Prometheus, Grafana, Loki, Tempo
    
- **Frontend:** React
    
- **Testing:** JUnit, Mockito, Testcontainers
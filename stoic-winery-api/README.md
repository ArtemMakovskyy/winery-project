# 🍷 "Stoic winery API - Backend" 

---

### 👓Stoic winery API backend project description

Development repository with all commits - https://github.com/ArtemMakovskyy/wine-store-app

With the Wine Store API, you can:

* View all wines that will be sorted by rating.
* View the wine card and its detailed information.
* Leave a review of the wine and give it a rating. When a user leaves a rating, it is calculated as the average score of
  the last hundred ratings plus 0.01 for each rating. If a user repeatedly submits a review for the same type of wine,
  the old review is deleted and a new one is added. This is done to eliminate duplication and cheating of ratings.
* View customer reviews and their ratings.
* Place an order for wine.
* Receive information about us in Telegram, send a message to the manager, receive links to sorted wine collections.
  Register by inserting your order number and receive order notifications. To correspond with the manager, the manager
  must first be registered in the database.
* As a store administrator, you can give the user manager rights.
* As a manager, you can: add wine cards, change their photos, and also delete wines.
* The administrator and manager require registration on the site; the user can use the resource without registration.
  When placing an order or adding a review, there will be a simplified registration.
* There is also a logout function.
* Once the API is running, you can use the controller's detailed documentation. Link to
  launch: http://localhost:8080/api/swagger-ui/index.html#/. Default admin credential: login - admin12345@gmail.com,
  password - 12345. Default manager credential: login - manager12345@gmail.com, password - 12345.

---

### 🖥️Technologies and tools used

- Programming Language: Java (version 17)
- Application Framework: Spring Boot (version 3.1.2)
- Dependency Management: Apache Maven
- REST
- Security
    - Spring Security
    - JSON Web Tokens (JWT)
- Spring Data JPA
- Spring MVC
- JSON Libraries:
    - Jackson Data Type for JSR310 (for handling dates in JSON)
    - Telegrambots (version 6.8.0) for Telegram integration
- Validation Libraries:
    - Spring Boot Starter Validation
    - Hibernate Validator (part of Spring Boot Starter Validation)
- Authentication and Security Libraries:
    - Spring Boot Starter Security
    - Spring Security Test (for security testing)
- Database Libraries:
    - Spring Boot Starter Data JPA
    - H2 Database (temporary database for development)
    - MySQL Connector (driver for MySQL)
- Database Migration Libraries:
    - Liquibase Core
    - Liquibase Migration Maven Plugin (version 4.23.1)
- Testing Libraries:
    - JUnit Jupiter
    - Testcontainers (for containerized testing)
    - Mockito
- API Documentation Tools:
    - Springdoc OpenAPI Starter Webmvc UI (version 2.1.0)
- JWT Processing Libraries:
    - jjwt-api
    - jjwt-impl (included in runtime scope)
    - jjwt-jackson (included in runtime scope)
- Additional Tools and Libraries:
    - Lombok (for reducing boilerplate code)
    - MapStruct (for convenient object mapping)
    - Spring Boot DevTools (for development convenience)
    - Maven Checkstyle Plugin (for code style checking)

---

### 🛠️ Observability

- **Logging**
  - SLF4J (Simple Logging Facade for Java)
  - Logback (default Spring Boot logging backend)
- **Metrics and Monitoring**
  - Micrometer (for application metrics)
  - Spring Boot Actuator (for exposing operational information)
- **Distributed Tracing**
  - OpenTelemetry SDK (for tracing)
  - Zipkin (for visualizing traces)
- **Visualization and Aggregation**
  - Prometheus (for scraping and storing metrics)
  - Grafana (for metrics dashboards)



### 🧭Describing functionalities of controllers

1. Authentication Controller:

- POST: localhost:8080/api/auth/login - get JWT tokens

#### Input email address and password to login.

 ```shell
{
  "email": "admin12345@gmail.com",
  "password": "12345"
}
   ```

- POST: api/auth/register

#### Save in database your: email, password, first name, last name and phone number

 ```shell
     {
    "email": "customer@email.com",
    "firstName": "firstName",
    "lastName": "lastName",
    "phoneNumber": "+380509876543",
    "password": "password1234",
    "repeatPassword": "password1234"
    }
   ```

- POST: localhost:8080/api/auth/logout

#### Logout user. Disable current token.

2. Users Controller: Endpoints for managing users

- PUT: localhost:8080/api/users/{id}/role - update user role

#### Update user role by user identification number. Only for ADMIN. Available roles: ROLE_CUSTOMER, ROLE_MANAGER, ROLE_ADMIN.

 ```shell
     {
      "role": "ROLE_MANAGER"
    }
   ```

3. Wines Controller: Endpoints to managing wines

- GET: localhost:8080/api/wines - Find all wines.
#### Find all wines. You can set pagination by: page, size, and sort parameters. By default, size 50, page = 0, sort by 'averageRatingScore,DESC' and after sort by 'id,DESC'. Pagination example: /wines?size=5&page=0&sort=id Available for all users.
- POST: localhost:8080/api/wines - Creating a new Wine with valid data. Available for manager12345@gmail.com
 ```shell
{
  "vendorCode": "MSD 2019",
  "qualityLevel": "string",
  "reserveType": "Limeted Edition Vine",
  "name": "Prince Trubetskoi Select Riesling",
  "shortName": "Prince Trubetskoi",
  "year": 2017,
  "tasteWine": "fish",
  "price": 25.59,
  "grape": "Riesling",
  "isDecantation": false,
  "wineType": "DRY | SEMI_DRY | MEDIUM_SWEET | SWEET",
  "strengthFrom": 10.9,
  "strengthTo": 11.8,
  "wineColor": "RED | ROSE | WHITE",
  "colorDescribing": "Deep red",
  "taste": "delicate, balanced, round, with a fruity and honey aftertaste.",
  "aroma": "soft, generous, multifaceted, with hints of tropical ",
  "gastronomy": "goes well with meat dishes, mature cheeses and stews ",
  "description": "description",
  "pictureLink": "Fill this link if you already have file into drive",
  "pictureLink2": "Fill this link if you already have file into drive"
}
   ```
- PATCH: localhost:8080/api/wines/{id}/image - Add an image into path. Available for manager12345@gmail.com. After you add or change photos, you will have to restart the app to view the changes.
 ```shell
{
  "imageA": "path to an image",
  "imageB": "path to an image"
}
   ```
- GET: localhost:8080//api/wines/{id} - Find existing wine by id. Available for all users.
- DELETE: /api/wines/{id} - Delete existing wine by id. Available for manager12345@gmail.com

4. Review management: Endpoints to managing reviews

- POST: localhost:8080/api/reviews - Adds a review to wine from a specific User. 
#### A specific user can't leave more than one review of one kind of wine. If a review already exists, an earlier review with a rating is deleted, new adds. If there is no user with first and last name, a new one is created. Users are compared by first and last name. Available for all users.
```shell
{
  "wineId": 1,
  "userFirstAndLastName": "Ivan Petrov",
  "message": "This is a great wine!",
  "rating": 5
}
   ```
- GET: localhost:8080/api/reviews/wines/{wineId} - Find all reviews by wine id.
#### Find all reviews by wine id, sort by reviewDate.DESC, size = 4, page = 0. Pagination example: /reviews/wine/{wineId}?size=5&page=0&sort=id Available for all users

5. Order management. Endpoints to managing orders

- GET: localhost:8080/api/orders - Find all orders.
#### Find all orders. Use size, page and sort for pagination. Pagination example: /orders?size=5&page=0&sort=id Available for all users
- POST: localhost:8080/api/orders - Adds an order for wine from a specific User.
####  Users are identified by first name, last name, and telephone number. If there is already a user in the database with first name, last name, and phone number, this user is linked to the order. Otherwise, the database is searched for a user by first and last name, and if one is found, a phone number is added to the found user and linked to the order. If in this case there is no user in the database, it is created anew. Available to all users.
```shell
{
  {
    "userFirstAndLastName": "Ivan Petrov",
    "email": "customer@email.com",
    "phoneNumber": "+380509876543",
    "createShoppingCardDto": {
      "purchaseObjects": [
        {
          "wineId": 2,
          "quantity": 1
        }
      ]
    },
    "createOrderDeliveryInformationDto": {
      "zipCode": "00000",
      "region": "Kyiv region",
      "city": "Kyiv",
      "street": "Lobanovskogo str, 13/1, ap. 16",
      "comment": "string"
    }
  }
}
   ```
- PATCH: localhost:8080/api/orders/{id}/paid - Set the status PAID for the order and set current data. Available for manager12345@gmail.com
- GET: localhost:8080/api/orders/{id} - Find order by id from database. Available for all users
- DELETE: localhost:8080/api/orders/{id} - Delete order by id from database. Available for manager12345@gmail.com
- GET: localhost:8080/api/orders/users/{userId} - Find all orders by user ID. Use size, page and sort for pagination. Pagination example: /orders/users/{userId}?size=5&page=0&sort=id Available for all users

6. Notifications Service (Telegram):
- Notifications about creation new order, changing payment status.
- Registration in api with enter order number.
- Uses Telegram API, Telegram Chats, and Bots.

# Observability

The project implements an observability system that enables real-time monitoring of metrics, logs, and traces. This allows for effective issue detection, performance analysis, and debugging.

## 🔧 Tools and Technologies Used

| Component                | Purpose |
|--------------------------|---------|
| **Spring Boot Actuator** | Exposes technical endpoints for gathering application state information |
| **Micrometer + Prometheus** | Collects metrics (CPU usage, memory, HTTP requests, etc.) |
| **Grafana** | Builds dashboards and visualizes metrics |
| **Loki** | Aggregates and displays logs from the Java application via Grafana |
| **Tempo** | Collects traces (distributed tracing) for request analysis across components |
| **Zipkin Brave** | A tracing tool (Zipkin format) with Tempo support |
| **Loki Logback Appender** | Sends logs from Spring Boot to Loki via Logback |

## 🚀 How to Use

### Open Grafana:
- URL: [http://localhost:3030](http://localhost:3030)
- By default, anonymous access is enabled with administrator privileges.

### Metrics:
1. Navigate to **Metrics → Prometheus**.
2. View relevant metrics such as:
- `http_server_requests_seconds_count`
- `jvm_memory_used_bytes`

### Logs:
1. Go to **Explore → Loki**.
2. Filter logs using queries:
- `{app="wine-store-api"}`
- `{level="ERROR"}`

### Traces:
1. Open **Traces → Tempo**.
2. Select a service and inspect the request path for **end-to-end tracing**.

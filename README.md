# 🍷 "Stoic winery" 

### ▶️How to set up and start the project

- **Soft requirements**
  - Docker
  - PostMan
  - Git
  - Browser
- **Instalation**
  - Clone the repository from github:
  ```shell
  git clone https://github.com/ArtemMakovskyy/winery-project.git
   ```
  - Start the Docker
  - If necessary, configure the database and set the TelegramBot parameters in the .env file.
    By default, the telegram bot is disabled and all the necessary parameters are set in the program.
    The program does not require additional settings.
  - Open a terminal and navigate to the root directory of your project
  - Into the terminal use command to build the container and start project.


First start:
  ```shell
  docker-compose up
   ```

Second start:
  ```shell
  docker-compose up --no-build
   ```
- First way to use the WINE SITE Frontend it is browser:
  http://localhost:3000/#/products

- Second way to use the WINE STORE Backend API it is SWAGGER:
  http://localhost:8080/api/swagger-ui/index.html#/

  password - 12345. Default manager credential: login - manager12345@gmail.com, password - 12345.
- Third way to use the WINE STORE Backend API it is PostMan

---
### 👓Stoic winery site frontend project description 
https://github.com/ArtemMakovskyy/winery-project/blob/master/wine_site_project/README.md

### 👓Stoic winery API backend project description
https://github.com/ArtemMakovskyy/winery-project/blob/master/stoic-winery-api/README.md

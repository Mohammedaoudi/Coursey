# AR-FirstAid - README

## Overview
This application is a robust learning platform designed to provide training in various fields, including First Aid and Anatomy. The platform includes Android and web clients, enabling participants to enroll in courses, take quizzes, and earn certifications upon successful completion. Advanced features include Augmented Reality (AR) modules for interactive learning and Artificial Intelligence (AI) tools to provide feedback on CPR training using video analysis. 

The platform is built on a microservices architecture and follows modern development and deployment practices, such as CI/CD pipelines, containerization, and observability.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Architecture](#architecture)
  - [Global Architecture](#global-architecture)
  - [CI/CD Pipeline Architecture](#cicd-pipeline-architecture)
- [Functionalities](#functionalities)
- [Project Structure](#project-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Docker Configuration](#docker-configuration)
- [Getting Started](#getting-started)
- [Deployment](#deployment)
- [Monitoring and Logging](#monitoring-and-logging)
- [Security Considerations](#security-considerations)
- [Demo](#demo)
- [Contributors](#contributors)
- [License](#license)

## Prerequisites

### Development Environment
- Java JDK 11 or higher
- Node.js 14.x or higher
- Android Studio 4.x or higher
- Git
- Docker and Docker Compose

### Infrastructure Requirements
- Jenkins server (port 8080)
- SonarQube server (port 9000)
- Nexus Repository (ports 5001-5003)
- Docker Registry
- Ubuntu servers for test and production environments

## Architecture
![WhatsApp Image 2024-12-29 à 18 03 19_f5265978](https://github.com/user-attachments/assets/9400c917-45c3-4545-bfdb-8221d45fa772)



### Global Architecture
The system consists of three main components:
- **Web Client**: A React-based frontend for both participants and administrators.
- **Mobile Client**: A native Android application tailored for participants.
- **Backend Services**: Microservices architecture to manage core functionalities like training content, quizzes, and certifications.

### CI/CD Pipeline Architecture
The CI/CD pipeline automates the build, testing, and deployment processes:
1. Developer pushes code to GitHub.
2. Jenkins triggers the build process.
3. Maven builds the project and runs unit tests.
4. SonarQube performs code analysis and quality checks.
5. Docker images are built and pushed to the Docker registry.
6. The application is deployed to test and production environments.
   ![image](https://github.com/user-attachments/assets/9249cd3e-d8fa-4123-885a-b3825e4e3238)


## Functionalities

### Participant Functionalities
- **Authentication**: Secure login via Keycloak using JWT and OpenID Connect.
- **Course Enrollment**: Browse and enroll in courses categorized by topics.
- **AR-Based Learning**: Participate in interactive AR training sessions.
- **AI-Powered Feedback**: Receive AI-driven feedback on CPR training performance.
- **Quiz and Certification**: Take quizzes after training and earn certifications.

### Administrator Functionalities
- **Course Management**: Create, update, and manage training courses.
- **Participant Monitoring**: Track participant progress and quiz results.

## Project Structure

```
├── backend/
│   ├── microservices/
│   ├── config/
│   └── docker-compose-local.yml
    └── Jenkinsfile
├── web-client/
│   ├── src/
│   └── Dockerfile
├── mobile-client/
   ├── app/
   └── Dockerfile
```

## CI/CD Pipeline

### Jenkins Pipeline Stages
1. **Source Code Management**:
   - GitHub repository integration.
   - Branch strategy: main and develop.

2. **Build & Test**:
   - Maven build and unit testing.
   - Integration testing.

3. **Code Quality**:
   - SonarQube analysis (port 9000).
   - Code coverage reports.
   - Quality gates enforcement.

4. **Artifact Management**:
   - Nexus Repository Manager for artifact storage.
   - Docker image storage and version management.

5. **Deployment**:
   - Deploy to test server (192.168.1.26).
   - Deploy to production server (192.168.1.66).
     ![WhatsApp Image 2024-12-29 à 14 44 37_2998a975](https://github.com/user-attachments/assets/16459a96-bf8e-4fed-a8c8-dfe21f93e6c4)


## Docker Configuration

### Docker Compose Configuration
The platform uses Docker Compose to manage its services:
- **Keycloak**: Authentication and user management.
- **MySQL**: Database for storing user data and training information.
- **Backend Microservices**: Handle course content, authentication, and notifications.
- **Observability Stack**: Includes Grafana, Prometheus, Loki, and Tempo.

A sample `docker-compose.yml` file is included for local setup.
```yaml
version: "3.8"
services:
  mysql-participant:
    image: docker.io/mysql:8.0
    container_name: mysql-participant
    environment:
      - MYSQL_DATABASE=first_aid_participant_bd
      - MYSQL_ROOT_PASSWORD=root
    volumes:
      - mysql_participant_data:/var/lib/mysql
    ports:
      - "3306:3306"
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 5

  mysql-training:
    image: docker.io/mysql:8.0
    container_name: mysql-training
    environment:
      - MYSQL_DATABASE=first_aid_training_bd
      - MYSQL_ROOT_PASSWORD=root
    volumes:
      - mysql_training_data:/var/lib/mysql
    ports:
      - "3307:3306"
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 5

  phpmyadmin:
    image: docker.io/phpmyadmin/phpmyadmin:latest
    container_name: phpmyadmin-firstaid
    ports:
      - "8089:80"
    networks:
      - app-network
    environment:
      PMA_HOSTS: mysql-participant,mysql-training
      PMA_PORTS: 3306,3306

  config-service:
    image: ${NEXUS_PRIVATE}/config-service:${VERSION:-latest}
    container_name: config-service
    ports:
      - "9999:9999"
    networks:
      - app-network
    depends_on:
      discovery-service:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/
    healthcheck:
      test: [ "CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9999/actuator/health" ]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  discovery-service:
    image: ${NEXUS_PRIVATE}/discovery-service:${VERSION:-latest}
    container_name: discovery-service
    ports:
      - "8761:8761"
    networks:
      - app-network
    healthcheck:
      test: [ "CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8761/actuator/health" ]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  gateway-service:
    image: ${NEXUS_PRIVATE}/gateway-service:${VERSION:-latest}
    container_name: gateway-service
    ports:
      - "8888:8888"
    networks:
      - app-network
    depends_on:
      - discovery-service
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-service:9999
      - SPRING_CLOUD_CONFIG_URI=http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/

  participant-service:
    image: ${NEXUS_PRIVATE}/participant-service:${VERSION:-latest}
    container_name: participant-service
    ports:
      - "8082:8082"
    networks:
      - app-network
    depends_on:
      mysql-participant:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-service:9999
      - SPRING_CLOUD_CONFIG_URI=http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/

  training-service:
    image: ${NEXUS_PRIVATE}/training-service:${VERSION:-latest}
    container_name: training-service
    ports:
      - "8081:8081"
    networks:
      - app-network
    depends_on:
      mysql-training:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_CONFIG_IMPORT=optional:configserver:http://config-service:9999
      - SPRING_CLOUD_CONFIG_URI=http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/

volumes:
  mysql_participant_data:
  mysql_training_data:

networks:
  app-network:
    driver: bridge
```


## Getting Started

### Backend Setup
```bash
# Clone repository
git clone https://github.com/Mohammedaoudi/Coursey.git

# Navigate to backend directory
cd backend

# Start local environment
docker-compose -f docker-compose-local.yml up -d
```

### Web Client Setup
```bash
# Navigate to web client directory
cd web

# Install dependencies
npm install

# Start development server
npm start
```

### Mobile Client Setup
```bash
# Open project in Android Studio
# Configure gradle settings
# Build and run on emulator/device
```

## Deployment

### Test Environment
- **Server**: 192.168.1.26
- **Docker Configuration**: `docker-compose.yml`
- **Deployment**: Automated via Jenkins.

### Production Environment
- **Server**: 192.168.1.66
- **Docker Configuration**: `docker-compose.prod.yml`
- **Deployment Process**: Manual approval with rollback options.

### Deployment Commands

#### Development
```bash
# Local development deployment
docker-compose -f docker-compose-local.yml up -d
```

#### Testing
```bash
# Test environment deployment
docker stack deploy -c docker-compose.yml app_stack
```

#### Production
```bash
# Production deployment
docker stack deploy -c docker-compose.yml app_stack
```

## Monitoring and Logging

- **Build Monitoring**: Jenkins pipelines track build status.
- **Code Quality Metrics**: SonarQube for static analysis and quality gates.
- **Container Logs**: Managed via Loki and visualized in Grafana.
- **Application-Level Logs**: Managed via Tempo.

## Security Considerations

- **HTTPS Enforcement**: Ensures secure communication.
- **JWT Authentication**: Secures API endpoints.
- **Docker Best Practices**: Minimizes vulnerabilities.
- **Network Segmentation**: Isolates test and production environments.

## Demo
A detailed video demonstration is available 


https://github.com/user-attachments/assets/51c95a58-9f6d-48e7-bdfe-e6c1aabdff61



https://github.com/user-attachments/assets/5a34f712-7041-489e-9f2f-f039457cf8cd



## Contributors

- **DAOUDI Mohammed**

## License
This project is licensed under the MIT License. See the LICENSE.md file for details.


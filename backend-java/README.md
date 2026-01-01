# Suduoku Backend
### Getting Started

1. Install `mvn`, `docker`,
    - Docker v28.0.4 used
    - Apache Maven v3.9.9 w/ java 25 used
2. Running via Maven
    -   `cd backend-java`
    -   `mvn clean package`
    -   `mvn clean compile exec:java`
3. Running via Docker
    -   `cd backend-java`
    -   `docker build -t suduoku-backend .`
    -   `docker run -p 8080:8080 suduoku-backend`

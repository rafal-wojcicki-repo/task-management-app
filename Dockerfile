ARG JAVA_VERSION=17

# Step 0: Build frontend (Node)
FROM node:18-alpine AS node-builder
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json* ./
COPY frontend/ ./
# Prefer reproducible install with lockfile, but fall back to `npm install` if none exists
RUN npm ci --silent || npm install --silent
RUN npm run build

# Step 1: Use microsoft JDK image with maven3 to build the backend and embed frontend build
FROM mcr.microsoft.com/openjdk/jdk:${JAVA_VERSION}-mariner AS builder
RUN tdnf install maven3 -y
WORKDIR /app
# Copy backend sources
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
# Copy frontend build into backend resources so Spring Boot will serve static files
RUN mkdir -p src/main/resources/static
COPY --from=node-builder /frontend/build ./src/main/resources/static

# Build the Spring Boot jar
RUN mvn clean package -DskipTests

# Step 2: Use microsoft JDK image for the final image
FROM mcr.microsoft.com/openjdk/jdk:${JAVA_VERSION}-mariner
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

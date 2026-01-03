# ===============================
# Build stage
# ===============================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copy Maven wrapper and config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline


# Copy source code
COPY src src

# Build Quarkus fast-jar
RUN ./mvnw -B package -DskipTests

# ===============================
# Runtime stage
# ===============================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built app from build stage
COPY --from=build /build/target/quarkus-app/ /app/

EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]

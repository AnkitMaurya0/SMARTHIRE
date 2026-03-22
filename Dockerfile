FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw -q dependency:go-offline -B

COPY src ./src
RUN ./mvnw -q clean package -DskipTests -B

EXPOSE 8080
CMD ["java", "-jar", "target/smarthire-0.0.1-SNAPSHOT.jar"]

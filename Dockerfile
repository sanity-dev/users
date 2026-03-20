# ============================================
# Etapa 1: Compilar con Maven
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================
# Etapa 2: Imagen de ejecución liviana
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR compilado
COPY --from=builder /app/target/*.jar app.jar

# Puerto del microservicio de usuarios
EXPOSE 8081

# Variables de entorno por defecto (se sobreescriben en docker-compose o EC2)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

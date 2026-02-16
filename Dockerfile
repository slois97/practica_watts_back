# --- ETAPA 1: BUILD (Compilación) ---
# Usamos una imagen que ya trae Maven y Java 21 listos
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos los archivos de configuración primero (para aprovechar la caché de Docker)
COPY pom.xml .
# Descargamos dependencias (esto hace que las siguientes builds sean más rápidas)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos el ejecutable, saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# --- ETAPA 2: RUN (Ejecución) ---
# Usamos la imagen final ligera de Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que usa tu app
EXPOSE 8080

# Arrancamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
# ── Étape 1 : build du .jar avec Maven ─────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Télécharge les dépendances séparément (mise en cache Docker : ne se refait
# que si pom.xml change, pas à chaque modif de code)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Étape 2 : image finale, légère (JRE seul, pas le JDK complet) ─────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar


# Render assigne son propre port via la variable d'environnement PORT.
ENV PORT=8080
EXPOSE 8080

# Réglages JVM adaptés aux 512MB de la VM Render gratuite
ENV JAVA_OPTS="-Xmx350m -Xss256k -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

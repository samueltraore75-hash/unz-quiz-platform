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

# Hugging Face Spaces route le trafic public vers le port 7860 du conteneur
# par défaut (voir app_port dans README.md). L'appli lit déjà server.port
# depuis la variable d'environnement PORT (voir application.properties),
# donc on se contente de la définir ici.
ENV PORT=7860
EXPOSE 7860

# Réglages JVM raisonnables (16GB RAM dispo sur Hugging Face Spaces CPU
# Basic, donc pas besoin de les serrer comme sur un hébergeur à 256MB) :
ENV JAVA_OPTS="-Xmx512m -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

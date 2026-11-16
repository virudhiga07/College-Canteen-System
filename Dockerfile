
FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests compile

EXPOSE 10000

CMD ["mvn", "-q", "exec:java", "-Dexec.mainClass=com.canteen.WebServer"]

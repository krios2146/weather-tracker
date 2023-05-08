FROM maven:3-eclipse-temurin-11-alpine AS build
WORKDIR /app
COPY . .
RUN mvn package

FROM tomcat
COPY --from=build /app/target/*.war $CATALINA_HOME/webapps/weather-tracker.war
CMD ["catalina.sh", "run"]

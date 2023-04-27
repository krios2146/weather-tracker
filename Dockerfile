FROM maven:latest AS build
WORKDIR /app
COPY . .
RUN mvn package

FROM tomcat
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/weather-tracker.war
EXPOSE 8080
CMD ["catalina.sh", "run"]

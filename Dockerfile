FROM maven:latest AS build
COPY . /app
WORKDIR /app
RUN mvn package

FROM tomcat:latest
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]

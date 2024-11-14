FROM openjdk:17-jdk-slim

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

RUN mkdir "deploy"
WORKDIR /deploy

COPY ./build/libs/CoffeeServiceProject-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "/deploy/app.jar"]
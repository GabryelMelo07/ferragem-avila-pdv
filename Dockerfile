FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-22-jdk -y
RUN apt-get install maven -y
COPY . .

RUN openssl genpkey -algorithm RSA -out ./backend/src/main/resources/app.key -pkeyopt rsa_keygen_bits:2048
RUN openssl rsa -pubout -in ./backend/src/main/resources/app.key -out ./backend/src/main/resources/app.pub

RUN mvn clean install 

FROM openjdk:22-jdk-slim

EXPOSE 8080

COPY --from=build /target/pdv-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]
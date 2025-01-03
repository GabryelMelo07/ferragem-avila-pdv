FROM openjdk:22-jdk-slim AS build

WORKDIR /backend

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY . .

RUN openssl genpkey -algorithm RSA -out ./src/main/resources/app.key -pkeyopt rsa_keygen_bits:2048
RUN openssl rsa -pubout -in ./src/main/resources/app.key -out ./src/main/resources/app.pub

RUN mvn clean install

FROM openjdk:22-jdk-slim

WORKDIR /backend

EXPOSE 8081

COPY --from=build /backend/target/pdv-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]
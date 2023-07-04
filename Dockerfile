FROM maven:3.8.5-openjdk-17-slim

WORKDIR /app

#copy and install the dependencies
#todo improve here to increase build performance by only copying necesarry file
COPY . .

RUN mvn clean install

CMD mvn spring-boot:run
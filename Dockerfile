FROM maven:3.8.5-openjdk-17-slim

WORKDIR /app

#copy and install the dependencies

COPY . .


#install and this also test the spring project, currently skip test because error from the setting
RUN mvn clean install

CMD mvn spring-boot:run
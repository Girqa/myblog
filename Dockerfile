FROM maven:3.9.9-eclipse-temurin-21-alpine as builder
LABEL authors="Reso11er"

WORKDIR /opt/app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src/

RUN mvn clean package

FROM tomcat:10-jre21 as servlet-container
ENV TOMCAT_HOME=/usr/local/tomcat

COPY --from=builder /opt/app/target/myblog.war $TOMCAT_HOME/webapps/myblog.war

EXPOSE 8080
ENTRYPOINT ["catalina.sh", "run"]
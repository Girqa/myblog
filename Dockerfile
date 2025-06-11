FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app/
COPY build/libs/myblog.jar /opt/app/myblog.jar

EXPOSE 8080 8000
ENTRYPOINT ["java", "-jar", "myblog.jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"]
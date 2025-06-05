FROM tomcat:10-jre21 as servlet-container
ENV TOMCAT_HOME=/usr/local/tomcat

COPY target/myblog.war $TOMCAT_HOME/webapps/myblog.war

EXPOSE 8080 8000
ENTRYPOINT ["catalina.sh", "run", "--", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"]
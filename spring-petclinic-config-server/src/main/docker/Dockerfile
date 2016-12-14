FROM java:8
VOLUME /tmp
ADD spring-petclinic-config-server-1.4.2.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8888
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
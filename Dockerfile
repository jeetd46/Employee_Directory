FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
ADD target/employee-directory-v1.jar employee-directory-v1.jar
ENTRYPOINT ["java","-jar","/employee-directory-v1.jar"]
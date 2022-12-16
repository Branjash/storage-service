FROM openjdk:11
LABEL maintainer="branko_rovcanin@epam.com"
VOLUME /storage-service
COPY ./target/storage-service-0.0.1-SNAPSHOT.jar /usr/app/
EXPOSE 8999
WORKDIR /usr/app
ENTRYPOINT ["java","-jar", "storage-service-0.0.1-SNAPSHOT.jar"]
FROM openjdk:17-alpine

EXPOSE 8286

COPY ./target/firmware-service-*.jar firmware-service.jar

RUN apk --no-cache add curl

RUN wget -c https://github.com/toitlang/toit/releases/download/v2.0.0-alpha.146/toit-linux.tar.gz -O - | tar -xz

RUN chmod +x /toit

ENTRYPOINT ["java", "-jar", "firmware-service.jar"]
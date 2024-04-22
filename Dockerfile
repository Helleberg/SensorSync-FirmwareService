FROM openjdk:17-alpine

EXPOSE 8286

COPY ./target/firmware-service-*.jar firmware-service.jar

RUN apk --no-cache add curl

RUN TOIT_VERSION=$( \
    curl --silent "https://api.github.com/repos/toitlang/toit/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $TOIT_VERSION \
    && wget -c https://github.com/toitlang/toit/releases/download/$TOIT_VERSION/toit-linux.tar.gz -O - | tar -xz

RUN chmod +x /toit

ENTRYPOINT ["java", "-jar", "firmware-service.jar"]
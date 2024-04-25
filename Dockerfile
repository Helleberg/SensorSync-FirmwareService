FROM ubuntu

EXPOSE 8286

WORKDIR /usr/src/service

ENV PATH="/usr/src/service/toit/bin:${PATH}"

COPY ./target/firmware-service-*.jar firmware-service.jar

COPY ./validate.toit validate.toit
COPY ./Makefile Makefile

RUN apt-get update && \
    apt-get install -y openjdk-17-jdk ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f \

ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/
RUN export JAVA_HOME

RUN apt-get update && apt-get install -y \
curl \
make \
wget

RUN TOIT_VERSION=$( \
    curl --silent "https://api.github.com/repos/toitlang/toit/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $TOIT_VERSION \
    && wget -c https://github.com/toitlang/toit/releases/download/$TOIT_VERSION/toit-linux.tar.gz -O - | tar -xz

RUN ATHENA_VERSION=$( \
    curl --silent "https://api.github.com/repos/Helleberg/SensorSync-AthenaContainer/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $ATHENA_VERSION \
    && wget -c https://api.github.com/repos/Helleberg/SensorSync-AthenaContainer/tarball/$ATHENA_VERSION -O - | tar -xz -C /athena

RUN chmod 777 /usr/src/service/athena
RUN chmod 777 /usr/src/service/toit
RUN chmod 777 /usr/src/service/validate.toit
RUN chmod 777 /usr/src/service/Makefile

ENTRYPOINT ["java", "-jar", "firmware-service.jar"]
FROM ubuntu

EXPOSE 8286

WORKDIR /usr/src/service

ENV PATH="/usr/src/service/toit/bin:${PATH}"

COPY ./target/firmware-service-*.jar firmware-service.jar

COPY ./Makefile Makefile

RUN mkdir /usr/src/service/toit_firmware

RUN mkdir /usr/src/service/jaguar

RUN apt-get update && apt-get install -y  \
    curl \
    make \
    wget

RUN apt-get update && \
    apt-get install -y openjdk-17-jdk ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f \

ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/
RUN export JAVA_HOME

RUN TOIT_VERSION=$( \
    curl --silent "https://api.github.com/repos/toitlang/toit/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $TOIT_VERSION \
    && wget -c https://github.com/toitlang/toit/releases/download/$TOIT_VERSION/toit-linux.tar.gz -O - | \
    tar -xz

RUN ATHENA_VERSION=$( \
    curl --silent "https://api.github.com/repos/Helleberg/SensorSync-AthenaContainer/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $ATHENA_VERSION \
    && wget -c https://api.github.com/repos/Helleberg/SensorSync-AthenaContainer/tarball/$ATHENA_VERSION -O - | \
    tar -xz \
    && mv /usr/src/service/Helleberg-SensorSync-AthenaContainer-* /usr/src/service/athena

RUN JAGUAR_VERSION=$( \
    curl --silent "https://api.github.com/repos/toitlang/jaguar/releases/latest" | \
    grep '"tag_name":' | \
    sed -E 's/.*"([^"]+)".*/\1/' \
    ) \
    && echo $JAGUAR_VERSION \
    && wget -c https://github.com/toitlang/jaguar/releases/download/$JAGUAR_VERSION/assets.tar.gz -O - | \
    tar -xz \
    && mv /usr/src/service/jaguar.snapshot /usr/src/service/jaguar

RUN chmod 777 /usr/src/service/athena
RUN chmod 777 /usr/src/service/jaguar
RUN chmod 777 /usr/src/service/toit
RUN chmod 777 /usr/src/service/Makefile

RUN cd /usr/src/service/athena/ && toit.pkg install


ENTRYPOINT ["java", "-jar", "firmware-service.jar"]
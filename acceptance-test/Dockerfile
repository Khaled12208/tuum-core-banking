FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y \
    curl \
    wget \
    unzip \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

RUN wget -O allure-2.24.0.tgz https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/2.24.0/allure-commandline-2.24.0.tgz && \
    tar -zxvf allure-2.24.0.tgz -C /opt/ && \
    ln -s /opt/allure-2.24.0/bin/allure /usr/bin/allure && \
    rm allure-2.24.0.tgz


WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ ./gradle/

COPY common-lib/ ./common-lib/

COPY acceptance-test/ ./acceptance-test/

RUN chmod +x ./gradlew

RUN ./gradlew :acceptance-test:compileJava :acceptance-test:compileTestJava -x test

EXPOSE 8085

RUN echo '#!/bin/bash\n\
echo "Waiting for services to be ready..."\n\
sleep 30\n\
echo "Running acceptance tests..."\n\
./gradlew :acceptance-test:test -Dbase.url="$BASE_URL" || true\n\
echo "Generating Allure report..."\n\
allure generate acceptance-test/build/allure-results -o acceptance-test/build/allure-report --clean\n\
echo "Starting Allure report server on port 8085..."\n\
cd acceptance-test/build/allure-report\n\
python3 -m http.server 8085\n\
' > /app/run-tests.sh && chmod +x /app/run-tests.sh

CMD ["/app/run-tests.sh"] 
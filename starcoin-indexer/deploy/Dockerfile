FROM openjdk:11 as build
WORKDIR /workspace/app

COPY ../../mvnw .
COPY ../../.mvn .mvn
COPY ../../pom.xml .
COPY ../../starcoin-commons starcoin-commons
COPY ../../starcoin-indexer starcoin-indexer
COPY settings.xml .

RUN --mount=type=cache,target=/root/.m2 ./mvnw --settings settings.xml install -DskipTests --quiet

FROM openjdk:11
ARG HOSTS
ARG NETWORK
ARG BG_TASK_JOBS
ARG SWAP_API_URL
ARG SWAP_CONTRACT_ADDR
ARG DS_URL
ARG DB_USER_NAME
ARG DB_PWD
ARG DB_SCHEMA
ARG MOVE_SCAN_API_URL

ENV HOSTS=$HOSTS
ENV NETWORK=$NETWORK
ENV BG_TASK_JOBS=$BG_TASK_JOBS
ENV SWAP_API_URL=$SWAP_API_URL
ENV SWAP_CONTRACT_ADDR=$SWAP_CONTRACT_ADDR
ENV DS_URL=$DS_URL
ENV DB_USER_NAME=$DB_USER_NAME
ENV DB_PWD=$DB_PWD
ENV DB_SCHEMA=$DB_SCHEMA
ENV MOVE_SCAN_API_URL=$MOVE_SCAN_API_URL


VOLUME /tmp
RUN mkdir logs
ARG DEPENDENCY=/workspace/app/starcoin-indexer/target/
COPY --from=build ${DEPENDENCY}/starcoin-indexer-1.1.jar /app/lib/indexer.jar
ENTRYPOINT ["java","-noverify","-jar","app/lib/indexer.jar", \
            "HOSTS=$HOSTS", \
            "NETWORK=$NETWORK", \
            "BG_TASK_JOBS=$BG_TASK_JOBS", \
            "SWAP_API_URL=$SWAP_API_URL", \
            "SWAP_CONTRACT_ADDR=$SWAP_CONTRACT_ADDR", \
            "DS_URL=$DS_URL", \
            "DB_USER_NAME=$DB_USER_NAME", \
            "DB_PWD=$DB_PWD", \
            "MOVE_SCAN_API_URL=$MOVE_SCAN_API_URL", \
            "DB_SCHEMA=$DB_SCHEMA" \
            ]
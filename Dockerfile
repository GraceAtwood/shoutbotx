FROM maven:3-openjdk-15 AS builder

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean test package appassembler:assemble

##############

FROM openjdk:15-alpine

COPY --from=builder /home/app/target/appassembler/ app

ENTRYPOINT ["app/bin/shoutbotx"]
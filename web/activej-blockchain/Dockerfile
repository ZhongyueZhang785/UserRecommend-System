FROM openjdk:8-jre-alpine

WORKDIR /app
COPY target/activejBlockChain.jar ./
EXPOSE 8080

ENTRYPOINT java $SYS_PROPS -jar activejBlockChain.jar
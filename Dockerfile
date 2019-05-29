FROM maven:3.5-jdk-8-alpine AS build
LABEL author="Jonathan Langens <jonathan.langens@tenforce.com>"
LABEL author="Wouter Dullaert <wouter.dullaert@tenforce.com>"

ENV COMPLIANCECHECKERVERSION=0.1
WORKDIR /app
COPY . /app
RUN mvn clean package

FROM openjdk:8-alpine
WORKDIR /app
CMD ["java", "-jar", "/app/compliance-checker.jar"]
ENV COMPLIANCECHECKERVERSION=0.1
ENV RULES_DIRECTORY=/rules
COPY rules /rules
RUN apk update && apk add --no-cache libc6-compat && \
  ln -s /lib/libc.musl-x86_64.so.1 /lib/ld-linux-x86-64.so.2
COPY --from=build /app/target/ComplianceChecker-${COMPLIANCECHECKERVERSION}.jar ./compliance-checker.jar

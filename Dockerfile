FROM maven:3.5-jdk-8-alpine AS build
LABEL author="Jonathan Langens <jonathan.langens@tenforce.com>"
LABEL author="Wouter Dullaert <wouter.dullaert@tenforce.com>"

ENV COMPLIANCECHECKERVERSION=0.1
WORKDIR /app
COPY . /app
RUN mvn clean package

FROM anapsix/alpine-java:8u202b08_jdk_unlimited
WORKDIR /app
ENV COMPLIANCECHECKERVERSION=0.1
ENV RULES_DIRECTORY=/rules
COPY rules /rules
COPY --from=build /app/target/ComplianceChecker-${COMPLIANCECHECKERVERSION}.jar ./compliance-checker.jar
CMD ["java", "-jar", "/app/compliance-checker.jar"]

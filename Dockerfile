FROM tomcat:9.0.6-jre8-slim

MAINTAINER Jonathan Langens <jonathan.langens@tenforce.com>

## install JDK 8

RUN apt-get update -y && \
    apt-get install -y \
      # jessie-backports \
      openjdk-8-jdk-headless \
      ca-certificates-java -y

## install maven

ARG MAVEN_VERSION=3.5.3
ARG USER_HOME_DIR="/root"
ARG SHA=b52956373fab1dd4277926507ab189fb797b3bc51a2a267a193c931fffad8408
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update && \
    apt-get install -y \
      curl procps \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

## copy the app source and build it
ADD . /app

ENV COMPLIANCECHECKERVERSION=0.1

RUN cd /app && mvn clean install

RUN cd /usr/local/tomcat/webapps/ROOT && rm -rf * && jar xvf /app/target/COMPLIANCECHECKER-$COMPLIANCECHECKERVERSION.war

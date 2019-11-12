# set the base image
FROM ubuntu:18.04

# author
MAINTAINER Omar Iraqi

# extra metadata
LABEL version="1.0"
LABEL description="Docker Image for Ubuntu, OpenJDK 8, Apache OFBiz and Immune Apps Framework"

# update sources list
RUN apt-get clean
RUN apt-get update
RUN apt-get install -y --no-install-recommends apt-utils

# install required software, one per line for better caching
RUN apt-get install -qy openjdk-8-jdk
RUN apt-get install -qy locales
RUN apt-get install -qy vim
RUN apt-get install -qy zip unzip
RUN apt-get install -qy git
WORKDIR /root
RUN git clone --progress --verbose https://github.com/apache/ofbiz-framework.git
WORKDIR /root/ofbiz-framework
RUN ./gradlew cleanAll loadAll --info
RUN ./gradlew jar
WORKDIR /root
RUN git clone --progress --verbose https://github.com/oiraqi/immune-apps.git
WORKDIR /root/immune-apps/framework
RUN ./gradlew agentLibs
WORKDIR /root

# cleanup
RUN apt-get -qy autoremove

# locales to UTF-8
RUN locale-gen "en_US.UTF-8" && /usr/sbin/update-locale LANG=en_US.UTF-8
ENV LC_ALL en_US.UTF-8
EXPOSE 8080:8080
EXPOSE 8443:8443

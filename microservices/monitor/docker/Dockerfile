# set the base image
FROM ubuntu:18.04

# author
LABEL maintainer="Omar Iraqi"

# extra metadata
LABEL version="1.0"
LABEL description="Docker Image for Ubuntu, OpenJDK 8 and Immunizer Monitoring Microservice"

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
RUN apt-get install -qy curl
WORKDIR /root
RUN curl -O https://downloads.apache.org/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz
RUN tar -xvzf spark-2.4.5-bin-hadoop2.7.tgz
RUN git clone --progress --verbose https://github.com/oiraqi/immunizer-monitor.git
RUN git clone --progress --verbose https://github.com/oiraqi/immunizer-analyze.git
WORKDIR /root/immunizer-monitor/scripts
RUN ./build.sh
WORKDIR /root

# cleanup
RUN apt-get -qy autoremove

# locales to UTF-8
RUN locale-gen "en_US.UTF-8" && /usr/sbin/update-locale LANG=en_US.UTF-8
ENV LC_ALL en_US.UTF-8

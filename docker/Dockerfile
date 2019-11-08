# set the base image
FROM ubuntu:18.04

# author
MAINTAINER Omar Iraqi

# extra metadata
LABEL version="1.0"
LABEL description="Docker Image for Ubuntu, OpenJDK 8, Git, curl"

# update sources list
RUN apt-get clean
RUN apt-get update
RUN apt-get install -y --no-install-recommends apt-utils

# install required software, one per line for better caching
RUN apt-get install -qy openjdk-8-jdk
RUN apt-get install -qy locales
RUN apt-get install -qy vim
RUN apt-get install -qy zip unzip
RUN apt-get install -qy curl
RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install gradle"
RUN apt-get install -qy git
WORKDIR /home
RUN git clone --progress --verbose https://github.com/oiraqi/immune-apps.git
#RUN cd immune-apps/apps
#RUN git clone --progress --verbose https://github.com/apache/ofbiz.git

# cleanup
RUN apt-get -qy autoremove

# locales to UTF-8
RUN locale-gen "en_US.UTF-8" && /usr/sbin/update-locale LANG=en_US.UTF-8
ENV LC_ALL en_US.UTF-8
EXPOSE 8080

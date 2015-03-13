FROM dockerfile/java:openjdk-7-jdk

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update; apt-get -y install software-properties-common
RUN add-apt-repository ppa:cwchien/gradle

RUN apt-get update; apt-get install -y gradle-2.1

ADD . /code
WORKDIR /code

RUN gradle clean build

VOLUME /code
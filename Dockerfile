FROM flurdy/oracle-java8:latest

ADD . /code
WORKDIR /code

# Install gradle
RUN ./gradlew --version

# Build
RUN ./gradlew build

VOLUME /code
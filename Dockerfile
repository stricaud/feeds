FROM openjdk:11-jdk-buster AS build

WORKDIR build
# Copy gradle resources
COPY build.gradle.kts gradle.properties settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
# Build deps
RUN ./gradlew build
# Copy everything else
COPY . .
# Build artifact
RUN ./gradlew distTar
RUN mv build/distributions/feeds-$(./gradlew showVersion -q -Prelease.quiet | cut -d' ' -f2).tar dist.tar

FROM adoptopenjdk/openjdk11:alpine-jre
ENV JAVA_OPTS=""
ENV FEEDS_OPTS="-Dconfig.file=/opt/feeds/conf/application.conf"

WORKDIR /opt/feeds
COPY --from=build build/dist.tar .
RUN tar xvf dist.tar --strip-components=1
RUN rm dist.tar

ENTRYPOINT /opt/feeds/bin/feeds

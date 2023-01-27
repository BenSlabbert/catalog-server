FROM alpine:3

COPY --from=catalog-base:latest /app/runtime /opt/java

WORKDIR /app

COPY target/item-1.0-SNAPSHOT.jar app.jar

COPY target/lib lib

ENV JAVA_OPTS="-Xmx32M -Xms32M"

CMD /opt/java/bin/java -jar app.jar "$JAVA_OPTS"

EXPOSE 8080

FROM azul/zulu-openjdk-alpine:19-latest as builder

RUN apk add --no-cache binutils

WORKDIR /app

# dynamically get the modules needed, might need to add jdk.crypto.ec
#RUN jdeps -R --print-module-deps --ignore-missing-deps --multi-release 17 -cp 'target/lib/*' target/catalog-0.0.1-SNAPSHOT.jar

RUN jlink \
         --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.net.http,java.prefs,java.scripting,java.security.jgss,java.security.sasl,java.sql.rowset,jdk.jfr,jdk.unsupported,jdk.crypto.ec \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output runtime

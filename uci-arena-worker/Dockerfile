FROM ghcr.io/graalvm/graalvm-community:21
COPY target/uci-arena-worker.jar /tmp
COPY logback.xml /tmp
CMD ["java","-Dlogback.configurationFile=/tmp/logback.xml", "-jar","/tmp/uci-arena-worker.jar"]

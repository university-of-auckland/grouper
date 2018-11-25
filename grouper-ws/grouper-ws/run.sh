#!/bin/bash

# mvn package jetty:run
# mvn -X -e package -Dlog4j.configuration=file:./conf/log4j.properties jetty:run -Djavax.net.ssl.trustStore=/Users/nick/.ssh/maven.jks #-Djava.net.ssl.trustStorePassword=""
# mvn -X -e package -Dlog4j.configuration=file:./conf/log4j.properties jetty:run 
# mvn -Dlicense.skip=true -X -e package -Dlog4j.configuration=file:./conf/log4j.properties jetty:run -Djavax.net.ssl.trustStore=/Users/nick/.ssh/maven.jks -Djava.net.ssl.trustStorePassword="changeit"

mvn clean -DskipTests=true -Dlicense.skip=true package -Dlog4j.configuration=file:./conf/log4j.properties jetty:run  -Dio.swagger.parser.util.RemoteUrl.trustAll=true

open http://localhost:8082/


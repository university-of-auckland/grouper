
all:	package copy resources deploy

package:
	mvn package -DskipTests
	mvn -f pom2.xml package -DskipTests

copy:
	cp target/grouper*-SNAPSHOT.jar target/grouper*-2.4.0-build*.jar

resources:
	mvn -f pom2.xml resources:resources -DskipTests

deploy:
	# mvn -f pom2.xml deploy -DartifactId=grouper-ui -Dversion=2.4.0-build14 -Dfile=target/grouper-ui-2.4.0-build.jar -DskipTests
	mvn -f target/pom2.xml deploy -DskipTests
	

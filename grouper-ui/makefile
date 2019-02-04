##
## jar
##

all:	package copy resources deploy

package:
	mvn package -DskipTests
	mvn -f target/pom2.xml package -DskipTests

copy:
	cp target/grouper*-SNAPSHOT.jar target/grouper-ui-2.4.0-build*.jar

resources:
	mvn -f pom2.xml resources:resources -DskipTests

archive:
	mkdir -p grouper-ui/dist/grouper
	cp -r webapp/WEB-INF/ grouper-ui/dist/grouper/WEB-INF
	mkdir -p grouper-ui/dist/grouper
	rm -f grouper-ui/dist/grouper/WEB-INF/web.xml
	tar cvf grouper-2.4.0.tar grouper-ui
	#tar -cvf arch.tar --transform 's,^,/usr/local/,S' ./webapp/WEB-INF
	gzip grouper-2.4.0.tar

deploy:
	export buildNumber=`grep buildNumber buildNumber.properties | grep = | cut -d= -f2`
	@echo ${buildNumber}
	# mvn -f pom2.xml deploy -DartifactId=grouper-ui -Dversion=2.4.0-build14 -Dfile=target/grouper-ui-2.4.0-build.jar -DskipTests
	# mvn -f target/pom2.xml deploy -DskipTests
	#mvn -f pom2_processed.xml deploy -DskipTests
	mvn deploy:deploy-file -DrepositoryId=uoa-central -DgroupId=edu.internet2.grouper -DartifactId=grouper-deploy -Dversion=2.4.0-build${buildNumber} -Dpackaging=tar.gz -DrepositoryId=uoa-releases -Durl=https://nexus.auckland.ac.nz/nexus/content/repositories/releases/ -Dfile=grouper-2.4.0.tar.gz
	git tag -f 2.4.0-build${buildNumber}
	git push -f --tags

##
## tar!!
##


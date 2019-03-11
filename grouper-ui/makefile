##
## jar
##

buildNumber0 := `grep buildNumber buildNumber.properties | grep = | cut -d= -f2`;
buildNumber := $(shell echo ${buildNumber0})

all:	package copy resources deploy

package:
	mvn package -DskipTests
	cp target/pom2.xml pom2_processed.xml
	mvn -f pom2_processed.xml package -DskipTests

copy:
	cp target/grouper*-SNAPSHOT.jar target/grouper-ui-2.4.0-build*.jar

resources:
	mvn -f pom2.xml resources:resources -DskipTests

deploy:
	buildNumber=`grep buildNumber buildNumber.properties | grep = | cut -d= -f2`
	@echo ${buildNumber}
	# mvn -f pom2.xml deploy -DartifactId=grouper-ui -Dversion=2.4.0-build14 -Dfile=target/grouper-ui-2.4.0-build.jar -DskipTests
	# mvn -f target/pom2.xml deploy -DskipTests
	#mvn -f pom2_processed.xml deploy -DskipTests
	mvn deploy:deploy-file -DrepositoryId=uoa-releases -DgroupId=edu.internet2.grouper -DartifactId=grouper-deploy -Dversion=2.4.0-build${buildNumber} -Dpackaging=tar.gz -Durl=https://nexus.auckland.ac.nz/nexus/content/repositories/releases/ -Dfile=grouper-2.4.0.tar.gz
	git tag -f 2.4.0-build${buildNumber}
	git push -f --tags

##
## tar!!
##
package_tar:
	# mvn -f pom2.xml clean package -DskipTests
	# cp target/pom2.xml pom2_processed.xml
	# mvn -f pom2_processed.xml package -DskipTests

archive:
	mkdir -p grouper-ui/dist/grouper
	cp -r webapp/WEB-INF/ grouper-ui/dist/grouper/WEB-INF
	mkdir -p grouper-ui/dist/grouper
	rm -f grouper-ui/dist/grouper/WEB-INF/web.xml
	mkdir -p grouper-ui/dist/grouper/WEB-INF/lib
	cp target/grouper-ui*SNAPSHOT.jar grouper-ui/dist/grouper/WEB-INF/lib/grouper-ui.jar
	mkdir -p grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/
	cp target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport.class grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport.class 
	cp target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group*.class grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/
	mkdir -p grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/
	cp target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembershipUpdateImportExport*.class grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/
	cp target/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.class grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/grouperUi/beans/ui/GroupContainer.class
	mkdir -p grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/
	cp target/classes/edu/internet2/middleware/grouper/ui/util/GrouperUiUtils*.class grouper-ui/dist/grouper/WEB-INF/classes/edu/internet2/middleware/grouper/ui/util/
	mkdir -p grouper-ui/dist/grouper/WEB-INF/classes/grouperText/
	cp ./conf/grouperText/grouper.text.en.us.base.properties grouper-ui/dist/grouper/WEB-INF/classes/grouperText/grouper.text.en.us.properties
	rm -f grouper-2.4.0.tar
	tar cvf grouper-2.4.0.tar grouper-ui
	#tar -cvf arch.tar --transform 's,^,/usr/local/,S' ./webapp/WEB-INF
	rm -f grouper-2.4.0.tar.gz
	gzip grouper-2.4.0.tar

deploy_tar:
	# export buildNumber=`grep buildNumber buildNumber.properties | grep = | cut -d= -f2`;
	@echo Deploying release $(buildNumber)
	mvn deploy:deploy-file -DrepositoryId=uoa-releases -DgroupId=edu.internet2.grouper -DartifactId=grouper-deploy -Dversion=2.4.0-build$(buildNumber) -Dpackaging=tar.gz -Durl=https://nexus.auckland.ac.nz/nexus/content/repositories/releases/ -Dfile=grouper-2.4.0.tar.gz
	git tag -f 2.4.0-build${buildNumber}

	git push -f --tags

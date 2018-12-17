
build:
	git checkout -- buildNumber.properties
	mvn clean compile -DskipTests

run:
	mvn spring-boot:run

deploy:	build
	mvn validate -P upload


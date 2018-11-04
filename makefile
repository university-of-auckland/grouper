
build:
	time mvn clean install  -f grouper-parent/pom.xml  -DskipTests -Dlicense.skip=true # -Dmaven.test.skip=true


build:
	time mvn clean install  -f grouper-parent/pom.xml  -DskipTests -Dlicense.skip=true
	# time mvn clean install  -f grouper-parent/pom.xml  -DskipTests -Dlicense.skip=true -Dmaven.test.skip=true

deploy:
	time mvn clean deploy  -f grouper-parent/pom.xml  -DskipTests -Dlicense.skip=true
	# time mvn clean deploy  -f grouper-parent/pom.xml  -DskipTests -Dlicense.skip=true -Dmaven.test.skip=true

site:
	time mvn site site:deploy -f grouper-parent/pom.xml -DskipTests -Dlicense.skip=true
	cd /tmp/grouper-parent
	open index.html


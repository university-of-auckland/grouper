#!/bin/bash

# mvn clean package -Dlicense.skip=true -DskipTests

# array=(2 )
array=( 4 )
for i in "${array[@]}"
do

#scp target/*.jar miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
#scp target/*.jar miva001@grpmgmttst0$i.its.auckland.ac.nz:/home/miva001
scp target/*.jar miva001@grpmgmtprd0$i.its.auckland.ac.nz:/home/miva001
#scp target/classes/edu/internet2/middleware/grouper/pspng/Provisioner.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
#scp target/classes/edu/internet2/middleware/grouper/pspng/Provisioner.class miva001@grpmgmttst0$i.its.auckland.ac.nz:/home/miva001
scp target/classes/edu/internet2/middleware/grouper/pspng/Provisioner.class miva001@grpmgmtprd0$i.its.auckland.ac.nz:/home/miva001

#echo run deploy_GROUPER-366.sh in ssh miva001@grpmgmtdev04.its.auckland.ac.nz
#echo run deploy_GROUPER-366.sh in ssh miva001@grpmgmttst0$i.its.auckland.ac.nz
echo run deploy_GROUPER-366.sh in ssh miva001@grpmgmtprd0$i.its.auckland.ac.nz
#ssh miva001@grpmgmttst0$i.its.auckland.ac.nz
#ssh miva001@grpmgmttst0$i.its.auckland.ac.nz
ssh miva001@grpmgmtprd0$i.its.auckland.ac.nz

done

#!/bin/bash

mvn clean package -Dlicense.skip=true -DskipTests

scp target/*.jar miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
scp target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001

array=(1 2 3 4 5 6 7 8 RetrieveGroupHelperResult)
for i in "${array[@]}"
do
    echo $i
    scp "target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2Group\$$i.class" miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
done

echo ssh miva001@grpmgmtdev04.its.auckland.ac.nz
ssh miva001@grpmgmtdev04.its.auckland.ac.nz

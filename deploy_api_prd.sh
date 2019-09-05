#!/bin/bash

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001

# 
#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport*.class miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/esb/consumer/EsbConsumer.class miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/esb/consumer/EsbConsumer.class miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/esb/consumer/EsbConsumer.class miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/esb/consumer/EsbConsumer.class miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001


# go to machines
ssh miva001@grpmgmtprd01.its.auckland.ac.nz
ssh miva001@grpmgmtprd02.its.auckland.ac.nz
ssh miva001@grpmgmtprd03.its.auckland.ac.nz
ssh miva001@grpmgmtprd04.its.auckland.ac.nz

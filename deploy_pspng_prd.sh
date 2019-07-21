#!/bin/bash

# prd01
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001

# prd02
#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport*.class miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd02.its.auckland.ac.nz:/home/miva001

# prd03
#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport*.class miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd03.its.auckland.ac.nz:/home/miva001

# prd04
#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport*.class miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtprd04.its.auckland.ac.nz:/home/miva001


#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembership*.class miva001@grpmgmtprd01.its.auckland.ac.nz:/home/miva001
ssh miva001@grpmgmtprd01.its.auckland.ac.nz
ssh miva001@grpmgmtprd02.its.auckland.ac.nz
ssh miva001@grpmgmtprd03.its.auckland.ac.nz
ssh miva001@grpmgmtprd04.its.auckland.ac.nz

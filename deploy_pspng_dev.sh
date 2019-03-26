#!/bin/bash

# dev03
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtdev03.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtdev03.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtdev03.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtdev03.its.auckland.ac.nz:/home/miva001

# dev04
#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2GroupImport*.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/grouper-pspng-2.4.0-SNAPSHOT.jar miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
scp ./grouper-misc/grouper-pspng/target/classes/edu/internet2/middleware/grouper/pspng/PspChangelogConsumerShim.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
scp ./grouper/target/classes/edu/internet2/middleware/grouper/changeLog/ChangeLogHelper.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001

scp ./grouper/target/grouper-2.4.0-SNAPSHOT.jar miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001


#scp ./grouper-ui/target/classes/edu/internet2/middleware/grouper/grouperUi/serviceLogic/SimpleMembership*.class miva001@grpmgmtdev04.its.auckland.ac.nz:/home/miva001
ssh miva001@grpmgmtdev03.its.auckland.ac.nz
ssh miva001@grpmgmtdev04.its.auckland.ac.nz

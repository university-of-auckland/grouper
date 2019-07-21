package edu.internet2.middleware.grouper.uoa;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import java.util.*;

/**
 * Created by wwan174 on 28/05/2019.
 */
@DisallowConcurrentExecution
public class UoAGroupCleanUp extends OtherJobBase {
    private static final Log LOG = GrouperUtil.getLog(UoAGroupCleanUp.class);

    private static final String CONFLICT_GROUP_STEM = "etc:exclusion";

    private static String confilctStemId;

    @Override
    public OtherJobOutput run(OtherJobInput otherJobInput){
        Set<Group> groups = new GroupFinder().assignParentStemId(getConfilctStemId()).assignStemScope(Stem.Scope.SUB).findGroups();
        LOG.debug("Exclusion groups size " + groups.size());
        if (groups != null && groups.size() > 0) {
            for (Group group : groups) {
                LOG.debug("group " + group + " has composite memberships " + group.getCompositeMembers());
                Set<Member> members = group.getCompositeMembers();
                LOG.debug("Members size " + members.size());
                if (members != null && members.size() > 0 ) { // the exclusion group should always be the second factor
                    Composite composite = group.getComposite(false);
                    if (composite != null) {
                        Group excludedGroup = composite.getRightGroup();
                        LOG.debug("exclusion group is " + excludedGroup);
                        for (Member member : members) {
                            UoAUtils.deleteMembership(excludedGroup, member.getSubject());
                            LOG.debug(member.getSubjectId() + " is deleted from " + excludedGroup);
                        }
                    }
                }
                LOG.debug("cleaned conflict membership for " + group);
            }
        }
        return null;
    }

    private static String getConfilctStemId() {
        if (confilctStemId == null){
            Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), CONFLICT_GROUP_STEM, false);
            if (stem != null) {
                confilctStemId = stem.getId();
            }
        }
        return confilctStemId;
    }

}

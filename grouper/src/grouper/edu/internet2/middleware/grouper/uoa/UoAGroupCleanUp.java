package edu.internet2.middleware.grouper.uoa;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.membership.MembershipType;
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

    private static final String CONFLICT_GROUP = "etc:uoa:conflict_group";

    private static String conflictGroupAttributeNameId ;

    @Override
    public OtherJobOutput run(OtherJobInput otherJobInput){
        Map<String, List<String>> conflictGroups = getConflictGroups();
        if (conflictGroups != null && conflictGroups.size() > 0) {
            for (String key : conflictGroups.keySet()) {
                Set<Membership> ownerGroupMemberships = UoAUtils.getUserMemberships(key, null); //both direct and  indirect
                LOG.debug(key + " has memberships " + ownerGroupMemberships.size());
                List<String> conflictGroupIds = conflictGroups.get(key);
                Set<Membership> conflictSubGroups = UoAUtils.getGourpMemberships(conflictGroupIds, null);
                for (Membership membership : conflictSubGroups) {
                    conflictGroupIds.add(membership.getMemberSubjectId());
                }
                LOG.debug("conflictGroupIds " + conflictGroupIds);
                Set<Membership> conflictGroupMemberships = UoAUtils.getUserMemberships(conflictGroupIds, MembershipType.IMMEDIATE);
                LOG.debug("conflictGroupMemberships " + conflictGroupMemberships.size());
                List<Membership> membershipsToBeRemoved = new ArrayList<>();
                for (Membership membership : conflictGroupMemberships) {
                    if (ownerGroupMemberships.stream().anyMatch(m -> m.getMemberSubjectId().equals(membership.getMemberSubjectId()))){
                        membershipsToBeRemoved.add(membership);
                    }
                }
                LOG.debug("membershipsToBeRemoved " + membershipsToBeRemoved.size());
                if (membershipsToBeRemoved.size() > 0) {
                    for (Membership membership : membershipsToBeRemoved) {
                        LOG.info("Removing conflict membership [" + membership.getMemberSubjectId() + ", "+ membership.getGroupName() + "]");
                        membership.delete();
                    }
                }else {
                    LOG.info("No conflict membership found for " + ownerGroupMemberships.iterator().next().getGroupName());
                }
            }
        }
        return null;
    }

    private Map<String, List<String>> getConflictGroups() {
        Map<String, List<String>> conflictGroups = UoAUtils.getGroupMap(getConflictGroupAttributeNameId());
        return conflictGroups;
    }

    private String getConflictGroupAttributeNameId() {
        if (conflictGroupAttributeNameId == null) {
            conflictGroupAttributeNameId = UoAUtils.getAttributeNameId(CONFLICT_GROUP);
        }
        return conflictGroupAttributeNameId;
    }


}

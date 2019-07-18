package edu.internet2.middleware.grouper.uoa;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;
import edu.internet2.middleware.grouper.changeLog.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.logging.Log;

import java.util.*;

/**
 * Created by wwan174 on 15/05/2019.
 */
public class UoARuleConsumer extends ChangeLogConsumerBase {

    private static final Log LOG = GrouperUtil.getLog(UoARuleConsumer.class);
    private static final String ATTRIBUTE_WAS_GROUP_REQUIRED = "etc:uoa:was_group_required";
    private static final String USER_MEMBERSHIP_SOURCE = "jdbc";
    private static final String ATTRIBUTE_COPY_MEMBER_REQUIRED ="etc:uoa:copy_member_to";

    private static String wasGroupAttributeNameId;
    private static Map<String, String> wasGroupMap;
    private static Set<GrouperDeprovisioningAffiliation> allAffiliations;
    private static Map<String, Group> deprovisionGroups;
    private static String copyMemberAttributeNameId;
    private static Map<String, List<String>> copyMemberGroupMap;

    private String groupName;
    private String sourceId;
    private String subjectId;

    @Override
    public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
                                        ChangeLogProcessorMetadata changeLogProcessorMetadata){
        long currentId = -1;

        try{
            for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
                currentId = changeLogEntry.getSequenceNumber();
                LOG.debug("currentId " + currentId);
                if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                    groupName = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
                    sourceId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
                    subjectId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
                    // handle wasGroup
                    if (getWasGroupMap() != null && getWasGroupMap().size() > 0){
                        handleWasGroupRule(changeLogEntry);
                    }
                    // handle deprovisioning affiliations
                    if (isAffilationGroup(groupName)){
                        handleDeprovisioningMembership(changeLogEntry);
                    }
                }else if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)){
                    groupName = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.groupName);
                    sourceId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.sourceId);
                    subjectId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
                    String groupId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.groupId);
                    LOG.debug("groupName " + groupName +", sourceId " + sourceId + ", subjectId " + subjectId + ", groupId " + groupId);
//                    if (sourceId != null && sourceId.equals(USER_MEMBERSHIP_SOURCE) && getCopyMemberGroupMap().containsKey(groupId)) {
                        handleCopyMembershipRule(changeLogEntry);
//                    }
                }

            }
        }catch (Exception e) {
            LOG.error("Error happened", e);
            return currentId - 1;
        }

        if (currentId == -1) {
            throw new RuntimeException("Couldn't process any records");
        }

        return currentId;
    }

    protected void handleWasGroupRule(ChangeLogEntry changeLogEntry) throws Exception{
        String groupId = getLabelValue(changeLogEntry,
                ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
        boolean processRequired = sourceId != null && sourceId.equals(USER_MEMBERSHIP_SOURCE) && getWasGroupMap().containsKey(groupId);

        if (processRequired){
            LOG.debug("Process was-group for changLog squence number " + changeLogEntry.getSequenceNumber());

            String subjectIdentifier = getLabelValue(changeLogEntry,
                        ChangeLogLabels.MEMBERSHIP_DELETE.subjectIdentifier0);
            Subject subject = getSubject(subjectId, sourceId);

            String logSubject = "[" + subjectId + ", " + subjectIdentifier + "]";
            if (subject == null) {
                LOG.debug("Subject " + logSubject + " dose not exist, skip" );
                return ;
            }
            // check if delete action is for future membership
            if (!addToWasGroupRequiered(groupId, subject)) {
                LOG.debug("Not need to add to was group of " + groupName + ", skip");
                return;
            }

            Group wasGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), getWasGroupMap().get(groupId), false);
            if (wasGroup != null) {
                wasGroup.addMember(subject, false);
                LOG.debug("Added subject " + logSubject + " to group " + wasGroup.getName());
            }else {
                LOG.warn("was-group for " + groupName + " does not exist");
            }
        }else {
            LOG.debug("Not a " + ATTRIBUTE_WAS_GROUP_REQUIRED + " group");
        }


    }

    private void handleDeprovisioningMembership(ChangeLogEntry entry) {
        boolean processRequired = sourceId != null && sourceId.equals(USER_MEMBERSHIP_SOURCE);
        if (processRequired) {
            LOG.debug("Process changLog squence number " + entry.getSequenceNumber());
            Group deprovisionGroup = getDeprovisionGroups().get(groupName);
            Subject subject = getSubject(subjectId, sourceId);
            if (deprovisionGroup != null && subject != null) {
                deprovisionGroup.addMember(subject, false);
                LOG.info("Added subject [" + subject.getId() + "] to deprovisioning group " + deprovisionGroup.getName());
            }
        }
    }

    /**
     *
     * @param changeLogEntry
     * @param changeLogLabel
     * @return label value
     */
    private String getLabelValue(ChangeLogEntry changeLogEntry,
                                 ChangeLogLabel changeLogLabel) {
        try {
            return changeLogEntry.retrieveValueForLabel(changeLogLabel);
        } catch (Exception e) {
            //cannot get value for label
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot get value for label: " + changeLogLabel.name());
            }
            return null;
        }
    }


    private static String getWasGroupAttributeNameId() {
        if (wasGroupAttributeNameId == null) {
            wasGroupAttributeNameId = UoAUtils.getAttributeNameId(ATTRIBUTE_WAS_GROUP_REQUIRED);
        }
        return wasGroupAttributeNameId;
    }

    private static Map<String, String> getWasGroupMap() {
        if (wasGroupMap == null) {
            wasGroupMap = new HashMap<String, String>();
            Map<String, List<String>> groupMaps = UoAUtils.getGroupMap(getWasGroupAttributeNameId());
            if (groupMaps != null) {
                for (String key : groupMaps.keySet()){
                    wasGroupMap.put(key, groupMaps.get(key).get(0));
                }
                LOG.info("wasGroupMap is " + wasGroupMap);
            }else {
                LOG.warn("No group has been assigned with attribute " + ATTRIBUTE_WAS_GROUP_REQUIRED + ", or attribute value is not set");
            }

        }
        return wasGroupMap;
    }

    private boolean addToWasGroupRequiered(String groupId, Subject subject) {
        Set<Membership> memberships = UoAUtils.getMemberships(Arrays.asList(groupId), UoAUtils.getSourceJDBC(), null, subjectId, false);
        for (Membership membership : memberships) {
            Membership thisMembership = membership;
            if (!thisMembership.isImmediate()) {
                thisMembership = new MembershipFinder().addGroup(membership.getViaGroup()).addSubject(subject)
                        .addField(FieldFinder.find("members",false)).findMembership(false);
            }
            if (thisMembership.isEnabled()){
                return false;
            }else if (thisMembership.getEnabledTimeDb() != null && thisMembership.getEnabledTimeDb() > new Date().getTime()){
                return false;
            }

        }
        return true;
    }

    private static Set<GrouperDeprovisioningAffiliation> getAllAffiliations() {
        if (allAffiliations == null) {
            allAffiliations = new HashSet<>();
            Map<String, GrouperDeprovisioningAffiliation> affiliations = GrouperDeprovisioningAffiliation.retrieveAllAffiliations();
            if (affiliations.size() > 0){
                allAffiliations.addAll(affiliations.values());
                LOG.debug("allAffiliations " + allAffiliations);
            }
        }
        return allAffiliations;
    }

    private boolean isAffilationGroup(String groupName) {
        if (getAllAffiliations().size() > 0){
            return allAffiliations.stream().anyMatch(a -> a.getGroupNameMeansInAffiliation().equals(groupName));
        }
        return false;
    }

    private Subject getSubject(String subjectId, String sourceId){
        SubjectFinder subjectFinder = new SubjectFinder().assignSubjectId(subjectId).assignSourceId(sourceId);
        return subjectFinder.findSubject();
    }

    private static Map<String, Group> getDeprovisionGroups() {
        if (deprovisionGroups == null) {
            deprovisionGroups = new HashMap<>();
            for (GrouperDeprovisioningAffiliation affiliation : getAllAffiliations()) {
                Group group = affiliation.getUsersWhoHaveBeenDeprovisionedGroup();
                deprovisionGroups.put(affiliation.getGroupNameMeansInAffiliation(), group);
                LOG.debug("deprovisionGroups " + deprovisionGroups);
            }
        }
        return deprovisionGroups;
    }

    private static Map<String, List<String>> getCopyMemberGroupMap() {
        if (copyMemberGroupMap == null) {
            copyMemberGroupMap = UoAUtils.getGroupMap(getCopyMemberAttributeNameId());
            LOG.debug("copyMemberGroupMap " + copyMemberGroupMap);
        }
        return copyMemberGroupMap;
    }

    private static String getCopyMemberAttributeNameId() {
        if (copyMemberAttributeNameId == null) {
            copyMemberAttributeNameId = UoAUtils.getAttributeNameId(ATTRIBUTE_COPY_MEMBER_REQUIRED);
        }
        return copyMemberAttributeNameId;
    }

    private void handleCopyMembershipRule(ChangeLogEntry changeLogEntry) throws Exception {
        String groupId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.groupId);
        boolean processRequired = sourceId != null && sourceId.equals(USER_MEMBERSHIP_SOURCE) && getCopyMemberGroupMap().containsKey(groupId) ;
        if (processRequired) {
            Subject subject = getSubject(subjectId, sourceId);
            LOG.debug("subject is " + subjectId);
            if (subject != null) {
                List<String> copyToGroupIds = getCopyMemberGroupMap().get(groupId);
                LOG.debug("copyToGroupIds " + copyToGroupIds);
                for (String copyToGroupId : copyToGroupIds) {
                    Group copyToGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), copyToGroupId, false);
                    LOG.debug("copyToGroup " + copyToGroup);
                    if (copyToGroup != null) {
                        copyToGroup.addMember(subject, false);
                        LOG.debug("Added subject [" + subject.getDescription() + "] to group " + copyToGroup.getName());
                    }
                }
            } else {
                LOG.warn("Subject " + subjectId + " doesn't exist");
            }
        }
    }
}

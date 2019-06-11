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

    private static String wasGroupAttributeNameId ;
    private static Map<String, String> wasGroupMap;
    private static Field memberField;
    private static Set<GrouperDeprovisioningAffiliation> allAffiliations;
    private static Map<String, Group> deprovisionGroups;

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
            if (isFutureMembership(groupId, subject)) {
                LOG.debug("Future membership removal, skip");
                return;
            }

            Group wasGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), getWasGroupMap().get(groupId), false);
            if (wasGroup != null) {
                wasGroup.addMember(subject, false);
                LOG.debug("Added subject [" + subjectId + ", " + subjectIdentifier + "] to group " + wasGroup.getName());
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


    private static Field getMemberField() {
        if (memberField == null){
            memberField = FieldFinder.find("members", false);
        }
        LOG.debug("memberField " + memberField);
        return memberField;
    }

    private boolean isFutureMembership(String groupId, Subject subject){
        // check if delete action is for future membership
        Membership currentMembership = new MembershipFinder().addGroupId(groupId).addSubject(subject)
                .findMembership(false);
        if (currentMembership != null && currentMembership.isImmediate()
                && currentMembership.getEnabledTimeDb() != null && currentMembership.getEnabledTimeDb() > new Date().getTime()){
            return true;
        }else if (currentMembership != null && !currentMembership.isImmediate()){
            Membership immidiateMembership = new MembershipFinder().addGroup(currentMembership.getViaGroup())
                    .addSubject(subject).assignHasEnabledDate(true).findMembership(false);
            if (immidiateMembership != null && immidiateMembership.getEnabledTimeDb() > new Date().getTime()) {
                return true;
            }
        }
        return false;
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
}

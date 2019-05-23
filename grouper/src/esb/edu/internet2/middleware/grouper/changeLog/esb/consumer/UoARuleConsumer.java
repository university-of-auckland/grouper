package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.changeLog.*;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
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

    @Override
    public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
                                        ChangeLogProcessorMetadata changeLogProcessorMetadata){
        long currentId = -1;

        try{
            for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
                currentId = changeLogEntry.getSequenceNumber();
                if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                    if (getWasGroupMap() != null && getWasGroupMap().size() > 0){
                        handleWasGroupRule(changeLogEntry);
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
        String sourceId = getLabelValue(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
        boolean processRequired = sourceId != null && sourceId.equals(USER_MEMBERSHIP_SOURCE);

        if (processRequired){
            LOG.debug("Process changLog squence number " + changeLogEntry.getSequenceNumber());
            String groupId = getLabelValue(changeLogEntry,
                    ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
            String groupName = getLabelValue(changeLogEntry,
                    ChangeLogLabels.MEMBERSHIP_DELETE.groupName);

            if (getWasGroupMap().containsKey(groupId)) {
                String subjectId = getLabelValue(changeLogEntry,
                        ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
                String subjectIdentifier = getLabelValue(changeLogEntry,
                        ChangeLogLabels.MEMBERSHIP_DELETE.subjectIdentifier0);
                SubjectFinder subjectFinder = new SubjectFinder().assignSubjectId(subjectId).assignSourceId(sourceId);
                Subject subject = subjectFinder.findSubject();

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

                Group wasGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(),
                        getWasGroupMap().get(groupId), false);

                if (wasGroup != null) {
                     MembershipFinder membershipFinder = new MembershipFinder().addGroup(wasGroup)
                                .addSubject(subject);
                        if (membershipFinder.findMembership(false) == null){
                            LOG.debug("Adding subject [" + subjectId + ", " + subjectIdentifier + "] to group " + wasGroup.getName());
                            Membership.internal_addImmediateMembership(GrouperSession.staticGrouperSession(), wasGroup, subject, getMemberField(), null, null, null);
                        }else {
                            LOG.debug("Subject [" + subjectId + ", " + subjectIdentifier + "] already in group " + wasGroup.getName());
                        }
                }else {
                    LOG.warn("was-group for " + groupName + " does not exist");
                }
            }else {
                LOG.debug("Not a " + ATTRIBUTE_WAS_GROUP_REQUIRED + " group");
            }

        }else {
            LOG.debug("Not a user direct membership change");
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


    protected static String getWasGroupAttributeNameId() {
        if (wasGroupAttributeNameId == null) {
            wasGroupAttributeNameId = getAttributeNameId(ATTRIBUTE_WAS_GROUP_REQUIRED);
        }
        return wasGroupAttributeNameId;
    }

    protected static Map<String, String> getWasGroupMap() {
        if (wasGroupMap == null) {
            wasGroupMap = new HashMap<String, String>();
            Map<String, List<String>> groupMaps = getGroupMap(getWasGroupAttributeNameId(), false);
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

    private static String getAttributeNameId(String attributeName) {
        AttributeDefName wasGroupAttribute = AttributeDefNameFinder.findByName(attributeName, false);
        String id = null;
        if (wasGroupAttribute != null) {
            id = wasGroupAttribute.getId();
        }else {
            LOG.warn("Attribute " + attributeName + " is not set" );
        }
        return id;
    }

    private static Map<String, List<String>> getGroupMap(String attributeNameId, boolean requireSubgroups) {
        Map<String, List<String>> groupMap = new HashMap<String, List<String>>();
        if (attributeNameId != null) {
            Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameId(attributeNameId);
            if (attributeAssigns != null && attributeAssigns.size() > 0) {
                for (AttributeAssign assign : attributeAssigns) {
                    if (assign.getOwnerGroupId() != null){
                        String ownerGroupId = assign.getOwnerGroupId();
                        AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder()
                                .addAttributeAssignId(assign.getId());
                        Set<AttributeAssignValue> values = attributeAssignValueFinder.findAttributeAssignValues();

                        if (values != null && values.size() > 0) {
                            List<String> groups = new ArrayList<String>();
                            for (AttributeAssignValue value : values) {
                                groups.add(value.getValueString());
                            }
                            groupMap.put(assign.getOwnerGroupId(), groups);
                            // get subgroups
                            if (requireSubgroups) {
                                Set<String> subgroupIds = getSubgroupIds(ownerGroupId);
                                if (subgroupIds.size() > 0) {
                                    for (String subgroupId : subgroupIds) {
                                        groupMap.put(subgroupId, groups);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return groupMap;
    }

    private static Set<String> getSubgroupIds(String groupId) {
        Set<Source> sources = new HashSet<Source>();
        sources.add(SubjectFinder.internal_getGSA());
        MembershipFinder membershipFinder = new MembershipFinder().addGroupId(groupId)
                .assignSources(sources).assignEnabled(true);
        Set<Object[]> results = membershipFinder.findMembershipsMembers();

        Set<String> subgroups = new HashSet<String>();
        if (results.size() > 0) {
            for (Object[] objArray : results){
                if (objArray.length > 0) {
                    Membership membership = (Membership) objArray[0];
                    String subgroupId = membership.getMember().getSubjectId();
                    subgroups.add(subgroupId);
                }
            }
        }
        return subgroups;
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
}

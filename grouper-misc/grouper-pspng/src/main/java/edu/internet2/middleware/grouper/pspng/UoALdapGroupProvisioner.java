package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.Subject;

import java.util.*;

/**
 * Created by wwan174 on 6/05/2019.
 */
public class UoALdapGroupProvisioner extends LdapGroupProvisioner {
    private static final String PSPNG_PROVISION_TO = "etc:pspng:provision_to";
    private static final String PSPNG_ACTIVEDIRECTORY = "pspng_activedirectory";

    private static final Set<ChangeLogTypeBuiltin> uoaChangelogTypesThatAreHandledIncrementally
            = new HashSet<>(Arrays.asList(
            ChangeLogTypeBuiltin.MEMBERSHIP_ADD,
            ChangeLogTypeBuiltin.MEMBERSHIP_DELETE));

    private static final Set<ChangeLogTypeBuiltin> uoaChangelogTypesThatAreHandledViaFullSync
            = new HashSet<>(Arrays.asList(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD));

    private static final Set<ChangeLogTypeBuiltin> uoaAllRelevantChangelogTypes
            = new HashSet<>();
    static {
        uoaAllRelevantChangelogTypes.addAll(uoaChangelogTypesThatAreHandledViaFullSync);
        uoaAllRelevantChangelogTypes.addAll(uoaChangelogTypesThatAreHandledIncrementally);
    }

    // Group-information needs to be flushed whenever anything changes, except memberships
    private static final Set<ChangeLogTypeBuiltin> uoaRelevantChangesThatNeedGroupCacheFlushing
            = new HashSet<>(uoaAllRelevantChangelogTypes);
    static {
        uoaRelevantChangesThatNeedGroupCacheFlushing.remove(ChangeLogTypeBuiltin.MEMBERSHIP_ADD);
        uoaRelevantChangesThatNeedGroupCacheFlushing.remove(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE);
    }

    public UoALdapGroupProvisioner(String provisionerName, LdapGroupProvisionerConfiguration config, boolean fullSyncMode) {
        super(provisionerName, config, fullSyncMode);

        LOG.debug("Constructing UoALdapGroupProvisioner: {}", provisionerName);
    }

    public static Class<? extends ProvisionerConfiguration> getPropertyClass() {
        return LdapGroupProvisionerConfiguration.class;
    }

    @Override
    protected boolean shouldWorkItemBeProcessed(ProvisioningWorkItem workItem){
        // Check if we're configured to ignore changes to internal (g:gsa) subjects
        // (default is that we do ignore such changes)
        if ( getConfig().areChangesToInternalGrouperSubjectsIgnored() ) {
            Subject subject = workItem.getSubject(this);
            if ( subject != null && subject.getSourceId().equalsIgnoreCase("g:gsa") ) {
                workItem.markAsSkipped("Ignoring event about a g:gsa subject");
                return false;
            }
        }

        // UoA customised code
        if ( !workItem.matchesChangelogType(uoaAllRelevantChangelogTypes) ) {
            LOG.debug("not a type in " + uoaAllRelevantChangelogTypes);
            workItem.markAsSkipped("Changelog type is not relevant to PSPNG provisioning - uoaAllRelevantChangelogTypes {}", uoaAllRelevantChangelogTypes);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected GrouperGroupInfo getGroupInfo(ProvisioningWorkItem workItem) {
        GrouperGroupInfo result = super.getGroupInfo(workItem);
        if (result == null && isPspAttributeAdd(workItem)){
            result = getGroupInfoOfAttributeValueAdd(workItem);
        }
        return result;
    }

    private boolean isPspAttributeAdd(ProvisioningWorkItem workItem) {
        return workItem.matchesChangelogType(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)
                && workItem.getAttributeName().equals(PSPNG_PROVISION_TO)
                && workItem.getChangelogEntry().retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value).equals(PSPNG_ACTIVEDIRECTORY);
    }

    private GrouperGroupInfo getGroupInfoOfAttributeValueAdd(ProvisioningWorkItem workItem){
        ChangeLogEntry entry = workItem.getChangelogEntry();
        String attributeAssignId = entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId);
        LOG.debug("attributeAssignId {}", attributeAssignId);
        GrouperGroupInfo result = null;
        if (attributeAssignId != null) {
            AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
                    .findById(attributeAssignId, true);
            if (attributeAssign != null) {
                Group group = attributeAssign.getOwnerGroup();
                if (group != null) {
                    result = getGroupInfoOfExistingGroup(group.getName());
                    workItem.groupName = group.getName();
                }
            }
        }
        return result;
    }

    @Override
    protected void flushCachesIfNecessary(List<ProvisioningWorkItem> allWorkItems)  throws PspException{
        for (ProvisioningWorkItem workItem : allWorkItems ) {
            // Skip irrelevant changelog entries
            if (!workItem.matchesChangelogType(uoaAllRelevantChangelogTypes)) {
                LOG.debug("{} not in uoaAllRelevantChangelogTypes {}", workItem, uoaAllRelevantChangelogTypes);
                continue;
            }

            // Skip changelog entries that don't need cache flushing
            if (!workItem.matchesChangelogType(uoaRelevantChangesThatNeedGroupCacheFlushing)) {
                LOG.debug("{} not in uoaRelevantChangesThatNeedGroupCacheFlushing {}", workItem, uoaRelevantChangesThatNeedGroupCacheFlushing);
                continue;
            }

            LOG.debug("flushCachesIfNecessary get group information");
            // We know we need to flush something from the cache. If the entry is group-specific,
            // we'll only flush that group
            GrouperGroupInfo groupInfo = getGroupInfo(workItem);
            LOG.debug("groupInfo for {} is {}",workItem.getChangelogEntry().getChangeLogType().getActionName(), groupInfo);
            if (workItem.matchesChangelogType(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)) {
                LOG.debug("attributeName {}", workItem.getAttributeName());
                LOG.debug("changeLog {}", workItem.getChangelogEntry().toStringReport(true));
            }

            if (groupInfo != null) {
                uncacheGroup(groupInfo, null);
            } else {
                // Flush everything and return
                uncacheAllGroups();
                return;
            }
        }
    }

    // we don't want that happen in any changes
    @Override
    public boolean workItemShouldBeHandledByFullSyncOfEverything(ProvisioningWorkItem workItem) {
        return false;
    }

}

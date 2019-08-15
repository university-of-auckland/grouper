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
        boolean shouldProcess = super.shouldWorkItemBeProcessed(workItem);
        if (shouldProcess) {
            // UoA customised code
            if (!workItem.matchesChangelogType(uoaAllRelevantChangelogTypes)) {
                LOG.info(workItem.getChangelogEntry().getChangeLogType() + " is not a type in " + uoaAllRelevantChangelogTypes + ", skip it");
                workItem.markAsSkipped("Changelog type is not relevant to PSPNG provisioning - uoaAllRelevantChangelogTypes {}", uoaAllRelevantChangelogTypes);
                shouldProcess = false;
            } else if (workItem.matchesChangelogType(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)) {
                if (!isPspAttributeAdd(workItem)) {
                    LOG.info(workItem.getChangelogEntry().getChangeLogType() + " is not attribute adding for pspng, skip it");
                    workItem.markAsSkipped("Changelog type is not relevant to PSPNG attribute update {}", PSPNG_PROVISION_TO);
                    shouldProcess = false;
                }
            }else {
                LOG.info("changeLog " + workItem.getChangelogEntry().getChangeLogType() + " will be processed !!!");
            }
        }
        return shouldProcess;
    }

    @Override
    protected GrouperGroupInfo getGroupInfo(ProvisioningWorkItem workItem) {
        GrouperGroupInfo groupInfo = super.getGroupInfo(workItem);
        if (groupInfo == null && isPspAttributeAdd(workItem)){
            setGroupNameOfAttributeValueAdd(workItem);
            groupInfo = super.getGroupInfo(workItem);
        }
        return groupInfo;
    }

    private boolean isPspAttributeAdd(ProvisioningWorkItem workItem) {
        return workItem.matchesChangelogType(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)
                && workItem.getAttributeName().equals(PSPNG_PROVISION_TO)
                && workItem.getChangelogEntry().retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value).equals(PSPNG_ACTIVEDIRECTORY);
    }

    private void setGroupNameOfAttributeValueAdd(ProvisioningWorkItem workItem){
        ChangeLogEntry entry = workItem.getChangelogEntry();
        String attributeAssignId = entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId);
        if (attributeAssignId != null) {
            AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
                    .findById(attributeAssignId, true);
            if (attributeAssign != null) {
                Group group = attributeAssign.getOwnerGroup();
                if (group != null) {
                    workItem.groupName = group.getName();
                }
            }
        }
        LOG.info("workItem group name " + workItem.groupName);
    }

    // we don't want that happen in any changes
    @Override
    public boolean workItemShouldBeHandledByFullSyncOfEverything(ProvisioningWorkItem workItem) {
        return false;
    }

    @Override
    protected void processIncrementalSyncEvent(ProvisioningWorkItem workItem) throws PspException {
        ChangeLogEntry entry = workItem.getChangelogEntry();

        if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD))
        {
            GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

            if ( grouperGroupInfo == null || grouperGroupInfo.hasGroupBeenDeleted() ) {
                workItem.markAsSkipped("Ignoring membership-add event for group that was deleted");
                return;
            }

            if ( !shouldGroupBeProvisioned(grouperGroupInfo) ) {
                workItem.markAsSkipped("Group %s is not selected to be provisioned", grouperGroupInfo);
                return;
            }

            LdapGroup tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
            Subject subject = workItem.getSubject(this);

            if ( subject == null ) {
                workItem.markAsSkippedAndWarn("Ignoring membership-add event because subject is no longer in grouper");
                return;
            }

            if ( subject.getTypeName().equalsIgnoreCase("group") ) {
                workItem.markAsSkipped("Nested-group membership skipped");
                return;
            }

            LdapUser tsUser = tsUserCache_shortTerm.get(subject);

            if ( config.needsTargetSystemUsers() && tsUser==null ) {
                workItem.markAsSkippedAndWarn("Skipped: subject doesn't exist in target system");
                return;
            }

            addMembership(grouperGroupInfo, tsGroup, subject, tsUser);
        }
        else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
        {
            GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

            if ( grouperGroupInfo==null || grouperGroupInfo.hasGroupBeenDeleted() ) {
                workItem.markAsSkipped("Ignoring membership-delete event for group that was deleted");
                return;
            }

            if ( !shouldGroupBeProvisioned(grouperGroupInfo) ) {
                workItem.markAsSkipped("Group %s is not selected to be provisioned", grouperGroupInfo);
                return;
            }

            LdapGroup tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
            Subject subject = workItem.getSubject(this);

            if ( subject == null ) {
                workItem.markAsSkippedAndWarn("Ignoring membership-delete event because subject is no longer in grouper");
                LOG.warn("Work item ignored: {}", workItem);
                return;
            }

            LdapUser tsUser = tsUserCache_shortTerm.get(subject);

            if ( config.needsTargetSystemUsers() && tsUser==null ) {
                workItem.markAsSkippedAndWarn("Skipped: subject doesn't exist in target system");
                return;
            }
            deleteMembership(grouperGroupInfo, tsGroup, subject, tsUser);
        } else {
            LOG.info(workItem.getChangelogEntry().getChangeLogType() + " not uoa incremental sync event");
            workItem.markAsSkipped("Skipped: " + workItem.getChangelogEntry().getChangeLogType() + " is not relevant to uoa incremental sync events");
        }
    }

}

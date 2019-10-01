package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.Subject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
        LOG.info("workItem group name " + workItem.groupName);
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

    @Override
    public List<ProvisioningWorkItem> filterWorkItems(List<ProvisioningWorkItem> workItems) throws PspException {
        List<ProvisioningWorkItem> result = super.filterWorkItems(workItems);
        LOG.info("super.filterWorkItems returns " + result.size() );
        List<String> fullSyncGroups = new ArrayList<>();
        for (ProvisioningWorkItem workItem : result) {
            // check if pspng attribute change is in the workItems
            if (isPspAttributeAdd(workItem)) {
                LOG.info("found full synch process underway for group " + workItem.groupName);
                fullSyncGroups.add(workItem.groupName);
            }
        }

        for (String group : fullSyncGroups) {
            LOG.info("skipping incremental work items in full synch group");

            workItems.forEach(item -> {
                if (item.groupName != null && item.groupName.equals(group)
                        && item.matchesChangelogType(uoaChangelogTypesThatAreHandledIncrementally)){
                    LOG.info("Ignoring work item " + item + " - group full sync is on the way");
                    item.markAsSkipped("Ignoring work item because group is on full sync");
                }
            });

            // delete incremental workItems for the group
            result.removeIf(item -> item.groupName != null && item.groupName.equals(group)
                    && item.matchesChangelogType(uoaChangelogTypesThatAreHandledIncrementally));
        }
        LOG.info("Result returned from UoALdapGroupProvisioner " + result.size());
        return result;
    }

    private String getDnFromLdif(String ldif) throws IOException {
        Reader reader = new StringReader(ldif);
        LdifReader ldifReader = new LdifReader(reader);
        SearchResult ldifResult = ldifReader.read();
        LdapEntry ldifEntry = ldifResult.getEntry();

        // Update DN to be relative to groupCreationBaseDn
        String actualDn = String.format("%s,%s", ldifEntry.getDn(),config.getGroupCreationBaseDn());
//        ldifEntry.setDn(actualDn);
        return actualDn;
    }

    @Override
    protected Map<GrouperGroupInfo, LdapGroup> fetchTargetSystemGroups(
            Collection<GrouperGroupInfo> grouperGroupsToFetch) throws PspException {
        LOG.debug("{} fetchTargetSystemGroups {}", getDisplayName(), grouperGroupsToFetch.size());
        Map<GrouperGroupInfo, LdapGroup> result = new HashMap<GrouperGroupInfo, LdapGroup>();
        if ( grouperGroupsToFetch.size() > config.getGroupSearch_batchSize() )
            throw new IllegalArgumentException("LdapGroupProvisioner.fetchTargetSystemGroups: invoked with too many groups to fetch");

        // If this is a full-sync provisioner, then we want to make sure we get the member attribute of the
        // group so we see all members.
        LOG.info("calling getLdapAttributesToFetch");
        String[] returnAttributes = getLdapAttributesToFetch();

        for (GrouperGroupInfo grouperGroup : grouperGroupsToFetch) {
            LOG.debug("{} fetch group {}", getDisplayName(), grouperGroup.getName());

            String ldifFromTemplate = getGroupLdifFromTemplate(grouperGroup);
            try {
                String dn = getDnFromLdif(ldifFromTemplate);
                LdapObject ldapObject = getLdapSystem().performLdapRead(dn, returnAttributes);

                result.put(grouperGroup, new LdapGroup(ldapObject));
            } catch (IOException e) {
                LOG.error("{} Exception caught, skip group {}", getDisplayName(), grouperGroup.getName(), e);
            }
        }
        LOG.debug("{}: Group match fetch result returned {} groups", getDisplayName(), result.size());
        return result;

    }

}

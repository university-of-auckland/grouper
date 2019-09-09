package edu.internet2.middleware.grouper.misc;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.pit.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;

import java.util.*;

public class UoASyncPITTables extends SyncPITTables {
    /** logger */
    private static final Log LOG = GrouperUtil.getLog(UoASyncPITTables.class);

    /** Whether or not to actually save updates */
    private boolean saveUpdates = true;

    /** Whether or not to log details */
    private boolean logDetails = true;

    /** Whether or not to send flattened notifications */
    private boolean sendFlattenedNotifications = true;

    /** Whether or not to send permission notifications */
    private boolean sendPermissionNotifications = true;

    /** whether or not to send flattened notifications for memberships */
    private boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);

    /** whether or not to send flattened notifications for privileges */
    private boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", false);

    /** whether there will be notifications for roles with permission changes */
    private boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);

    private static List<String> excludeDomainIds ;

//    private static List<String> excludeGroupNames;

    private static void initFilter() {
        if (excludeDomainIds == null /*|| excludeGroupNames == null*/) {
//            String[] groupNames = {"NAAdmin", "NAAdminGroups", "NetAccountSuperuser", "exportedUsers", "exportedUsers2", "outgoing", "outgoing-bulk", "outgoing-users",
//                    "ec.auckland.ac.nz:all","ecusers", "PasswordExpires", "PasswordExpiryExempt", "canAuthenticate", "RecentRemovals"};
//            excludeGroupNames = Arrays.asList(groupNames);
            String[] domainNames = {"1989.auckland.ac.nz",
                    "1996.auckland.ac.nz",
                    "1997.auckland.ac.nz",
                    "1998.auckland.ac.nz",
                    "1999.auckland.ac.nz",
                    "2000.auckland.ac.nz",
                    "2001.auckland.ac.nz",
                    "2002.auckland.ac.nz",
                    "2003.auckland.ac.nz",
                    "2004.auckland.ac.nz",
                    "2005.auckland.ac.nz",
                    "2006.auckland.ac.nz",
                    "2007.auckland.ac.nz",
                    "2008.auckland.ac.nz",
                    "2009.auckland.ac.nz",
                    "2010.auckland.ac.nz",
                    "2011.auckland.ac.nz",
                    "2012.auckland.ac.nz",
                    "2013.auckland.ac.nz",
                    "2014.auckland.ac.nz",
                    "2015.auckland.ac.nz",
                    "2016.auckland.ac.nz",
                    "2017.auckland.ac.nz",
                    "2018.auckland.ac.nz"};
            excludeDomainIds = new ArrayList<>();
            for (String domain : domainNames) {
                excludeDomainIds.add(StemFinder.internal_findByName(domain, true).getId());
            }
        }
//        LOG.info("excludeGroupNames " + excludeGroupNames.size());
        LOG.info("excludeDomainIds " + excludeDomainIds.size());
    }

    @Override
    public long processMissingActivePITMemberships() {
        initFilter();
        System.out.println("\n\nSearching for missing active point in time memberships");

        long totalProcessed = 0;
        long totalFailed = 0;
        long totalSkipped = 0;

        Set<Membership> mships = GrouperDAOFactory.getFactory().getPITMembership().findMissingActivePITMemberships();
        System.out.println("Found " + mships.size() + " missing active point in time memberships");

        for (Membership mship : mships) {

            LOG.info("Found missing point in time membership with ownerId: " + mship.getOwnerId() +
                    ", memberId: " + mship.getMemberUuid() + ", fieldId: " + mship.getFieldId());
            Group ownerGroup = mship.getOwnerGroup();
//            String ownerGroupName = ownerGroup.getExtension();
            String ownerGroupDomain = ownerGroup.getStemId();
            if (/*excludeGroupNames.contains(ownerGroupName) ||*/ excludeDomainIds.contains(ownerGroupDomain)){
                LOG.info("Skip membership for " + ownerGroup);
                totalSkipped ++;
                continue;
            }

            if (saveUpdates) {
                PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(mship.getFieldId(), true);
                PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(mship.getMemberUuid(), true);

                PITMembership pitMembership = new PITMembership();
                pitMembership.setId(GrouperUuid.getUuid());
                pitMembership.setSourceId(mship.getImmediateMembershipId());
                pitMembership.setMemberId(pitMember.getId());
                pitMembership.setFieldId(pitField.getId());
                pitMembership.setActiveDb("T");
                pitMembership.setStartTimeDb(System.currentTimeMillis() * 1000);

                if (mship.getOwnerGroupId() != null) {
                    pitMembership.setOwnerGroupId(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(mship.getOwnerGroupId(), true).getId());
                } else if (mship.getOwnerStemId() != null) {
                    pitMembership.setOwnerStemId(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(mship.getOwnerStemId(), true).getId());
                } else if (mship.getOwnerAttrDefId() != null) {
                    pitMembership.setOwnerAttrDefId(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(mship.getOwnerAttrDefId(), true).getId());
                } else {
                    throw new RuntimeException("Unexpected -- Membership with id " + mship.getUuid() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
                }

                if (!GrouperUtil.isEmpty(mship.getContextId())) {
                    pitMembership.setContextId(mship.getContextId());
                }

                if (sendFlattenedNotifications) {
                    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
                    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
                }

                if (sendPermissionNotifications) {
                    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
                }
                try {
                    LOG.info("pitMembership source id " + pitMembership.getSourceId() + ", start time " + pitMembership.getStartTime());
                    pitMembership.save();
                    totalProcessed++;
                } catch (Exception e) {
                    LOG.error("Failed to update missing membershipwith ownerId: " + mship.getOwnerId() +
                            ", memberId: " + mship.getMemberUuid() + ", fieldId: " + mship.getFieldId(), e);
                    totalFailed ++;
                }
            }

        }

        if (mships.size() > 0 && saveUpdates) {
            System.out.println("Done making " + totalProcessed + " updates, failed " + totalFailed + " updates, skipped " + totalSkipped);
        }

        return totalProcessed;
    }

    @Override
    public long processMissingActivePITGroupSetsSecondPass() {
        initFilter();
        System.out.println("\n\nSearching for missing active point in time group sets (second pass)");

        long totalProcessed = 0;
        long totalFailed = 0;
        long totalSkipped = 0;

            List<GroupSet> groupSets = new LinkedList<GroupSet>(GrouperDAOFactory.getFactory().getPITGroupSet().findMissingActivePITGroupSetsSecondPass());

            System.out.println("Found " + groupSets.size() + " missing active point in time group sets");

            Collections.sort(groupSets, new Comparator<GroupSet>() {

                public int compare(GroupSet o1, GroupSet o2) {
                    return ((Integer) o1.getDepth()).compareTo(o2.getDepth());
                }
            });

            for (GroupSet groupSet : groupSets) {

                LOG.info("Found missing point in time group set with id: " + groupSet.getId());

                String ownerGroupId = groupSet.getOwnerId();
                String memberGroupId = groupSet.getMemberId();
                Group ownerGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerGroupId, false);
                if (ownerGroup == null || memberGroupId == null) {
                    LOG.error("Cannot find owner or member group " + ownerGroupId + ", " + memberGroupId);
                    totalSkipped++;
                    continue;
                }
                String ownerGroupDomain = ownerGroup.getStemId();
//            String ownerGroupName = ownerGroup.getExtension();
                if (/*excludeGroupNames.contains(ownerGroupName) ||*/ excludeDomainIds.contains(ownerGroupDomain) || excludeDomainIds.contains(memberGroupId)) {
                    LOG.info("Skip groupSet for owner group " + ownerGroup + ", member group " + memberGroupId);
                    totalSkipped++;
                    continue;
                }
                if (saveUpdates) {
                    try {
                        // it's possible this was already taken care of... check
                        try {
                            PITGroupSet check = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getId(), false);
                            if (check != null) {
                                continue;
                            }
                        } catch (Exception e) {
                            LOG.error("Error on findBySourceIdActive for " + groupSet.getId());
                            continue;
                        }

                        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getFieldId(), true);
                        PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getMemberFieldId(), true);
                        PITGroupSet pitParent = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getParentId(), true); // throw exception

                        PITGroupSet pitGroupSet = new PITGroupSet();
                        pitGroupSet.setId(GrouperUuid.getUuid());
                        pitGroupSet.setSourceId(groupSet.getId());
                        pitGroupSet.setDepth(groupSet.getDepth());
                        pitGroupSet.setParentId(pitParent.getId());
                        pitGroupSet.setFieldId(pitField.getId());
                        pitGroupSet.setMemberFieldId(pitMemberField.getId());
                        pitGroupSet.setActiveDb("T");
                        pitGroupSet.setStartTimeDb(System.currentTimeMillis() * 1000);

                        if (groupSet.getOwnerGroupId() != null) {
                            PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getOwnerId(), true);
                            pitGroupSet.setOwnerGroupId(pitOwner.getId());
                        } else if (groupSet.getOwnerStemId() != null) {
                            PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getOwnerId(), true);
                            pitGroupSet.setOwnerStemId(pitOwner.getId());
                        } else if (groupSet.getOwnerAttrDefId() != null) {
                            PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getOwnerId(), true);
                            pitGroupSet.setOwnerAttrDefId(pitOwner.getId());
                        } else {
                            throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
                        }

                        if (groupSet.getMemberGroupId() != null) {
                            PITGroup pitMember = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getMemberId(), true);
                            pitGroupSet.setMemberGroupId(pitMember.getId());
                        } else if (groupSet.getMemberStemId() != null) {
                            PITStem pitMember = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getMemberId(), true);
                            pitGroupSet.setMemberStemId(pitMember.getId());
                        } else if (groupSet.getMemberAttrDefId() != null) {
                            PITAttributeDef pitMember = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getMemberId(), true);
                            pitGroupSet.setMemberAttrDefId(pitMember.getId());
                        } else {
                            throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an memberGroupId, memberStemId, or memberAttrDefId.");
                        }

                        if (!GrouperUtil.isEmpty(groupSet.getContextId())) {
                            pitGroupSet.setContextId(groupSet.getContextId());
                        }

                        if (sendFlattenedNotifications) {
                            pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
                            pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
                        }
                        pitGroupSet.saveOrUpdate();
                        totalProcessed++;
                    } catch (Exception e) {
                        LOG.error("Failed to process pit Group Set " + groupSet, e);
                        totalFailed++;

                    }
                }


            }

            if (groupSets.size() > 0 && saveUpdates) {
                System.out.println("Done making " + totalProcessed + " updates, failed " + totalFailed + ", skipped " + totalSkipped);
            }

        return totalProcessed;
    }



}

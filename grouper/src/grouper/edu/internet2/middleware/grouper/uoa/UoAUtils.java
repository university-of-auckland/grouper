package edu.internet2.middleware.grouper.uoa;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.subj.SubjectResolverFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import org.apache.commons.logging.Log;

import java.util.*;

/**
 * Created by wwan174 on 28/05/2019.
 */
public class UoAUtils {
    private static final Log LOG = GrouperUtil.getLog(UoAUtils.class);

    public static String getAttributeNameId(String attributeName) {
        AttributeDefName groupAttributeName = AttributeDefNameFinder.findByName(attributeName, false);
        String id = null;
        if (groupAttributeName != null) {
            id = groupAttributeName.getId();
        }else {
            LOG.warn("Attribute " + attributeName + " is not set" );
        }
        LOG.debug("Attribute " + attributeName + " id is " + id);
        return id;
    }

    public static Map<String, List<String>> getGroupMap(String attributeNameId) {
        Map<String, List<String>> groupMap = new HashMap<String, List<String>>();
        if (attributeNameId != null) {
            Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameId(attributeNameId);
            if (attributeAssigns != null && attributeAssigns.size() > 0) {
                for (AttributeAssign assign : attributeAssigns) {
                    if (assign.getOwnerGroupId() != null) {
                        String ownerGroupId = assign.getOwnerGroupId();
                        AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder()
                                .addAttributeAssignId(assign.getId());
                        Set<AttributeAssignValue> values = attributeAssignValueFinder.findAttributeAssignValues();
                        LOG.debug(assign.getOwnerGroup().getName() + " has attribute values " + values);
                        if (values != null && values.size() > 0) {
                            List<String> groups = new ArrayList<String>();
                            for (AttributeAssignValue value : values) {
                                Group theGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), value.getValueString(), false);
                                if (theGroup != null) {
                                    groups.add(theGroup.getId());
                                }else {
                                    LOG.error("Group [" + value.getValueString() + "] not exist");
                                }
                            }
                            if (groups.size() > 0) {
                                groupMap.put(ownerGroupId, groups);
                            }
                        }
                    }
                }
            }
        }
        return groupMap;
    }

    public static Source getSourceJDBC() {
        return SubjectResolverFactory.getInstance().getSource("jdbc");
    }

    public static Set<Membership> getUserMemberships(String groupId, MembershipType type) {
        return getMemberships(Arrays.asList(groupId), getSourceJDBC(), type);
    }

    public static Set<Membership> getUserMemberships(List<String> groupIds, MembershipType type) {
        return getMemberships(groupIds, getSourceJDBC(), type);
    }

    public static Set<Membership> getGourpMemberships(List<String> groupIds, MembershipType type) {
        return getMemberships(groupIds, SubjectFinder.internal_getGSA(), type);
    }

    public static Set<Membership> getMemberships (List<String> groupIds, Source source, MembershipType type) {
        Set<Source> sources = new HashSet<Source>();

        MembershipFinder membershipFinder = new MembershipFinder().assignGroupIds(groupIds)
                .assignField(FieldFinder.find("members",false)).assignEnabled(true);

        if (source != null) {
            sources.add(source);
            membershipFinder.assignSources(sources);
        }

        if (type != null) {
            membershipFinder.assignMembershipType(type);
        }

        Set<Object[]> results = membershipFinder.findMembershipsMembers();

        Set<Membership> memberships = new HashSet<Membership>();
        if (results.size() > 0) {
            for (Object[] objArray : results){
                if (objArray.length > 0) {
                    Membership membership = (Membership) objArray[0];
                    memberships.add(membership);
                }
            }
        }
        return memberships;
    }


}

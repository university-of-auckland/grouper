package edu.internet2.middleware.grouper.flat;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen 
 * $Id$
 */
@SuppressWarnings("serial")
public class FlatGroup extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {
  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** group id foreign key in grouper_groups table */
  public static final String COLUMN_GROUP_ID = "group_id";
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: groupId */
  public static final String FIELD_GROUP_ID = "groupId";


  /**
   * fields which are included in db version
   */
  /*
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_ID, FIELD_GROUP_ID);
  */
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_GROUP_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_FLAT_GROUPS = "grouper_flat_groups";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** group id foreign key in grouper_groups table*/
  private String groupId;
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }
  
  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return id
   */
  public String getId() {
    return id;
  }
  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return group id foreign key in grouper_groups table
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set group id foreign key in grouper_groups table
   * @param groupId
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

}

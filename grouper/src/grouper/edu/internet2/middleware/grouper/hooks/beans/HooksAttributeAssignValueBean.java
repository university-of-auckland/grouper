/*
 * @author mchyzer
 * $Id: HooksAttributeAssignBean.java 6923 2010-08-11 05:06:01Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeAssignValueBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeAssignValue */
  public static final String FIELD_ATTRIBUTE_ASSIGN_VALUE = "attributeAssignValue";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_VALUE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private AttributeAssignValue attributeAssignValue = null;
  
  /**
   * 
   */
  public HooksAttributeAssignValueBean() {
    super();
  }

  /**
   * @param theAttributeValue
   */
  public HooksAttributeAssignValueBean(AttributeAssignValue theAttributeValue) {
    this.attributeAssignValue = theAttributeValue;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeAssignValueBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

}

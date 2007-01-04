/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/** 
 * Source adapter for using Grouper groups as I2MI Subjects.
 * <p/>
 * <p>
 * This is an adapter I2MI Subjects of type <i>group</i>.  It allows
 * groups within a Group Groups Registry to be referenced as I2MI
 * Subjects.  
 * <p>
 * To use, add the following to your <i>sources.xml</i> file:
 * </p>
 * <pre class="eg">
 * &lt;source adapterClass="edu.internet2.middleware.grouper.GrouperSourceAdapter"&gt;
 *   &lt;id&gt;g:gsa&lt;/id&gt;
 *   &lt;name&gt;Grouper: Grouper Source Adapter&lt;/name&gt;
 *   &lt;type&gt;group&lt;/type&gt;
 * &lt;/source&gt;
 * </pre>
 * @author  blair christensen.
 * @version $Id: GrouperSourceAdapter.java,v 1.20 2007-01-04 17:17:45 blair Exp $
 */
public class GrouperSourceAdapter extends BaseSourceAdapter {

  // PRIVATE INSTANCE VARIABLES //
  private Set             _types  = new LinkedHashSet();
  private GrouperSession  rs      = null;


  // CONSTRUCTORS //

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter() {
    super();
  } // public GrouperSourceAdapter()

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter(String id, String name) {
    super(id, name);
  } // public GrouperSourceAdapter(id, name)


  // PUBLIC INSTANCE METHODS //

  /**
   * Get a {@link Group} subject by UUID.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   id  Group UUID
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public Subject getSubject(String id) 
    throws SubjectNotFoundException 
  {
    try {
      return new GrouperSubject( GroupFinder.findByUuid(this._getSession(), id) );
    }
    catch (Exception e) {
      throw new SubjectNotFoundException("subject not found: " + e.getMessage(), e);
    }
  } // public Subject getSubject(id)

  /**
   * Gets a {@link Group} subject by its name.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubjectByIdentifier(name, "group");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubjectByIdentifier(name, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   name  Group name
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public Subject getSubjectByIdentifier(String name) 
    throws SubjectNotFoundException 
  {
    try {
      return new GrouperSubject(
        GroupFinder.findByName(this._getSession(), name)
      );
    }
    catch (Exception e) {
      throw new SubjectNotFoundException(
        "subject not found: " + e.getMessage(), e
      );
    }
  } // public Subject getSubjectByIdentifier(name)

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * SourceAdapter  sa    = new GrouperSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject types supported by this source.
   */
  public Set getSubjectTypes() {
    if (_types.size() != 1) {
      _types.add( SubjectTypeEnum.valueOf("group") );
    }
    return _types;
  } // public Set getSubjectTypes()

  /** 
   * Initializes the Grouper source adapter.
   * <p/>
   * <p>
   * No initialization is currently performed by this adapter.
   * </p>
   * @throws  SourceUnavailableException
   */
  public void init() throws SourceUnavailableException {
    // Nothing
  } // public void init()

  /**
   * Searches for {@link Group} subjects by naming attributes.
   * <p/>
   * <p>
   * This method performs a fuzzy search on the <i>stem</i>,
   * <i>extension</i>, <i>displayExtension</i>, <i>name</i> and
   * <i>displayName</i> group attributes.
   * </p>
   * <pre class="eg">
   * // Use it within the Grouper API
   * Set subjects = SubjectFactory.search("admins");
   *
   * // Use it directly
   * Set subjects = source.search("admins");
   * </pre>
   */
  public Set search(String searchValue) {
    Set   subjs  = new LinkedHashSet();
    Stem  root   = StemFinder.findRootStem(this._getSession());
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        this._getSession(), new GroupNameFilter(searchValue, root)
      );
      Group     g;
      Iterator  iter  = gq.getGroups().iterator();
      while (iter.hasNext()){
        g = (Group) iter.next();
        subjs.add(g.toSubject()); 
      }
    }
    catch (QueryException eQ) {
      ErrorLog.error(GrouperSourceAdapter.class, E.GSA_SEARCH + eQ.getMessage());
    } 
    return subjs;
  } // public Set search(searchValue)


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private GrouperSession _getSession() {
    if (this.rs == null) {
      try {
        this.rs = GrouperSession.start( SubjectFinder.findRootSubject() );
      }
      catch (SessionException eS) {
        throw new GrouperRuntimeException(E.S_NOSTARTROOT + eS.getMessage());
      }
    }
    return this.rs;
  } // private GrouperSession _getSession()

}


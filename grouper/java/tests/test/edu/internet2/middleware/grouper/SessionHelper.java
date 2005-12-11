/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;

/**
 * {@link GrouperSession} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: SessionHelper.java,v 1.3 2005-12-11 06:28:39 blair Exp $
 */
public class SessionHelper {

  // Protected Class Methods

  // @return  A root {@link GrouperSession}
  protected static GrouperSession getRootSession() {
    return SessionHelper.getSession("GrouperSystem");
  } // protected static GrouperSession getRootSession()

  // Get a session by id
  // @return  A {@link GrouperSession}
  protected static GrouperSession getSession(String id) {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(id)
      );
      Assert.assertNotNull("s !null", s);
      Assert.assertNotNull("session_id !null", s.getSessionId());
      Assert.assertTrue("has sessionID", !s.getSessionId().equals(""));
      Assert.assertNotNull("subject !null", s.getSubject());
      Assert.assertTrue(
        "subject instanceof Subject", s.getSubject() instanceof Subject
      );
      Assert.assertNotNull("member !null", s.getMember());
      Assert.assertTrue(
        "member instanceof Member", s.getMember() instanceof Member
      );
      Assert.assertTrue(
        "subject id", s.getMember().getSubjectId().equals(id)
      );
      return s;
    }
    catch (SessionException e0) {
      Assert.fail(
        "failed to start session: " + e0.getMessage()
      );
    }
    catch (SubjectNotFoundException e1) {
      Assert.fail(
        "failed to find subject '" + id + "': " + e1.getMessage()
      );
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static GrouperSession getSession(id)

  // Get a session by id and type
  // @return  A {@link GrouperSession}
  protected static GrouperSession getSession(String id, String type) {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(id, type)
      );
      Assert.assertNotNull("s !null", s);
      Assert.assertNotNull("session_id !null", s.getSessionId());
      Assert.assertTrue("has sessionID", !s.getSessionId().equals(""));
      Assert.assertNotNull("subject !null", s.getSubject());
      Assert.assertTrue(
        "subject instanceof Subject", s.getSubject() instanceof Subject
      );
      Assert.assertNotNull("member !null", s.getMember());
      Assert.assertTrue(
        "member instanceof Member", s.getMember() instanceof Member
      );
      Assert.assertTrue(
        "subject id", s.getMember().getSubjectId().equals(id)
      );
      Assert.assertTrue(
        "subject type", s.getMember().getSubjectTypeId().equals(type)
      );
      Assert.assertTrue(
        "access priv klass",
        s.getAccessClass().equals("edu.internet2.middleware.grouper.GrouperAccessAdapter")
      );
      Assert.assertTrue(
        "naming priv klass",
        s.getNamingClass().equals("edu.internet2.middleware.grouper.GrouperNamingAdapter")
      );
      return s;
    }
    catch (SessionException e0) {
      Assert.fail(
        "failed to start session: " + e0.getMessage()
      );
    }
    catch (SubjectNotFoundException e1) {
      Assert.fail(
        "failed to find subject '" + id + "/" + type + "': " + e1.getMessage()
      );
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static GrouperSession getSession(id, type)
}


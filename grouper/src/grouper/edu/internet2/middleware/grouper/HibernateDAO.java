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
import  java.util.ArrayList;
import  java.util.Collection;
import  java.util.Iterator;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateDAO.java,v 1.4 2007-01-04 17:17:45 blair Exp $
 * @since   1.2.0
 */
class HibernateDAO {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Object create(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(obj);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
      return obj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Object create(obj)

  // @since   1.2.0
  protected static void delete(Collection c) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = c.iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void delete(c)

  // @since   1.2.0
  protected static void delete(Object obj) 
    throws  GrouperDAOException 
  {
    Collection c = new ArrayList();
    c.add(obj);
    delete(c);
  } // protected static void delete(obj)

  // @since   1.2.0
  protected static void update(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update(obj);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void update(obj)

} // class HibernateDAO


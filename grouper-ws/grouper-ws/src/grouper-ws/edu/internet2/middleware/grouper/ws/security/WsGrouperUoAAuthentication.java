/*
 * @author skoh023
 * $Id: WsGrouperUoAAuthentication.java,v 1.0 2016-06-17
 */
package edu.internet2.middleware.grouper.ws.security;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;


/**
 * University of Auckland custom authentication for Grouper as specified in grouper-ws.properties
 */
public class WsGrouperUoAAuthentication implements WsCustomAuthentication {

    /**
     *
     * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
     */
    public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
        throws RuntimeException {

        // use this to be the user connected, or the user act-as
        //String userIdLoggedIn = GrouperServiceJ2ee.retrieveUserPrincipalNameFromRequest();
        String userIdLoggedIn = httpServletRequest.getHeader("UOAid");

        return userIdLoggedIn;
    }
}
/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui.actions;

import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * Top level Strut's action which forward to the home page defined in the 
 * Struts Config file.
 * 
 * Sites can override the definition so a different tab is the default home
 * TODO: make a user preference?
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: HomeAction.java,v 1.2 2005-12-08 15:30:52 isgwb Exp $
 */
public class HomeAction extends org.apache.struts.action.Action {
//	------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_home = "home";

  //------------------------------------------------------------ Action Methods

  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
	  try {
	  String defaultUrl=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("default.browse.path");
		if(defaultUrl !=null && defaultUrl.startsWith("/")) {
			return new ActionForward(defaultUrl,true);
		}
	  }catch(MissingResourceException e) {
		//default.browse.path not set so use Struts config  
	  }
  	
    return mapping.findForward(FORWARD_home);
  }
}
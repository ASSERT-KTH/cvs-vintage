/*
 * $Header: /tmp/cvs-vintage/struts/src/tiles-documentation/org/apache/struts/webapp/tiles/template/DynTemplateAction.java,v 1.5 2004/03/14 06:23:53 sraeburn Exp $
 * $Revision: 1.5 $
 * $Date: 2004/03/14 06:23:53 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.webapp.tiles.template;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentDefinition;


public final class DynTemplateAction extends Action {


    // --------------------------------------------------------- Public Methods


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
      * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward execute(
				 ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws Exception {

    System.out.println( "Start dynamic definition" );
      // Create template definition
    ComponentDefinition definition = new ComponentDefinition();
    String path = "/tutorial";
      // set definition 'contents'
    //definition.setTemplate( "/tutorial/basic/myFramesetLayout.jsp" );
    definition.put( "title", "My first dynamic frameset page", true );
      // using type="string" is the same as direct=true
    definition.put( "header", path + "/common/header.jsp", "string", null );
    definition.put( "footer", path + "/common/footer.jsp", true );
    definition.put( "menu", path + "/basic/menu.jsp", true );
    definition.put( "body", path + "/basic/helloBody.jsp", true );

    System.out.println( "definition=" + definition );
      // Save our definition as a bean :
    request.setAttribute( "templateDefinition", definition );

	  return (mapping.findForward("success"));
    }

 }

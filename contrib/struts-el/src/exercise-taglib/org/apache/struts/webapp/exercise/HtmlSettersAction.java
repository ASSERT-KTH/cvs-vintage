/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/exercise-taglib/org/apache/struts/webapp/exercise/HtmlSettersAction.java,v 1.3 2004/03/14 07:15:06 sraeburn Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/14 07:15:06 $
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


package org.apache.struts.webapp.exercise;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;


/**
 * Do-nothing action that accepts the changes made automatically in our form
 * bean, and then returns control to the input form (if "Save" was pressed)
 * or the main menu (if "Cancel" was pressed).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2004/03/14 07:15:06 $
 */

public class HtmlSettersAction extends Action {


    /**
     * Forward to the input form if "Save" was pressed or the main menu
     * if "Cancel" was pressed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception Exception if business logic throws an exception
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {

        if (isCancelled(request))
            return (mapping.findForward("index"));
        else
            return (mapping.findForward("input"));

    }


}

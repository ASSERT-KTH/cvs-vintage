/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/exercise-taglib/org/apache/struts/webapp/exercise/DynaSetAction.java,v 1.2 2004/03/14 07:15:06 sraeburn Exp $
 * $Revision: 1.2 $
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
import org.apache.struts.action.DynaActionForm;

public class DynaSetAction extends Action
{
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {

        DynaActionForm dynaActionForm = (DynaActionForm) form;

        dynaActionForm.set("foo", "alpha");
        dynaActionForm.set("bar", "beta");

        return (mapping.findForward("success"));
    }
}

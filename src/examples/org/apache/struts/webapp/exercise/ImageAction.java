/*
 * $Header: /tmp/cvs-vintage/struts/src/examples/org/apache/struts/webapp/exercise/ImageAction.java,v 1.2 2004/03/14 06:23:52 sraeburn Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/14 06:23:52 $
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


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Read image from resource given as ActionMapping parameter
 * and copy to output stream.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/14 06:23:52 $
 */

public class ImageAction extends Action {


    /**
     * Read image from resource given as ActionMapping parameter
     * and copy to output stream.
     *
     * @exception java.lang.Exception on input/output error
     */
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

            // e.g. /$module/struts-power.gif
            // :FIXME: Should compute module
        String image = mapping.getParameter();

        byte[] buffer = new byte[2048];
        int bytesRead;
        InputStream input =
                getServlet().getServletContext().getResourceAsStream(image);
        OutputStream
                out = response.getOutputStream();

        while ((bytesRead = input.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();

        return null;
    }

}
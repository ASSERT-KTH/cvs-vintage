/*
 * Copyright 2002,2004 The Apache Software Foundation.
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

package org.apache.struts.faces.renderer;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p><code>Renderer</code> implementation for the <code>stylesheet</code> tag
 * from the <em>Struts-Faces Integration Library</em>.</p>
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/08 02:49:54 $
 */

public class StylesheetRenderer extends AbstractRenderer {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance for this class.</p>
     */
    private static Log log = LogFactory.getLog(StylesheetRenderer.class);


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Render a relative HTML <code>&lt;link&gt;</code> element for a
     * <code>text/css</code> stylesheet at the specified context-relative
     * path.</p>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent to be rendered
     *
     * @exception IOException if an input/output error occurs while rendering
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("link", component);
        writer.writeAttribute("rel", "stylesheet", null);
        writer.writeAttribute("type", "text/css", null);
        writer.writeURIAttribute
            ("href",
             context.getExternalContext().getRequestContextPath() +
             (String) component.getAttributes().get("path"), "path");
        writer.endElement("link");
        writer.writeText("\n", null);

    }



    // ------------------------------------------------------- Protected Methods


}

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
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.ResponseUtils;


/**
 * <p><code>Renderer</code> implementation for the <code>write</code> tag
 * from the <em>Struts-Faces Integration Library</em>.</p>
 *
 * @version $Revision: 1.6 $ $Date: 2004/07/08 01:11:28 $
 */

public class WriteRenderer extends AbstractRenderer {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance for this class.</p>
     */
    private static Log log = LogFactory.getLog(WriteRenderer.class);


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Encode the specified text to our response.</p>
     *
     * @param context FacesContext for the response we are creating
     * @param component Component to be rendered
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        ResponseWriter writer = context.getResponseWriter();
        String style =
            (String) component.getAttributes().get("style");
        String styleClass =
            (String) component.getAttributes().get("styleClass");
        if ((style != null) || (styleClass != null)) {
            writer.startElement("span", component);
            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
            writer.writeText("", null);
        }
        String text = getText(context, component);
        if (log.isTraceEnabled()) {
            log.trace("encodeEnd(" + component.getClientId(context) +
                      "," + text + ")");
        }
        writer.write(text);
        if ((style != null) || (styleClass != null)) {
            writer.endElement("span");
        }

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the text to be rendered for this component, optionally
     * filtered if requested.</p>
     *
     * @param context FacesContext for the response we are creating
     * @param component Component to be rendered
     */
    protected String getText(FacesContext context, UIComponent component) {

        String text = getAsString(context, component,
                                  ((ValueHolder) component).getValue());
        Boolean filter = (Boolean) component.getAttributes().get("filter");
        if (filter == null) {
            filter = Boolean.FALSE;
        }
        if (filter.booleanValue()) {
            return (ResponseUtils.filter(text));
        } else {
            return (text);
        }

    }


}

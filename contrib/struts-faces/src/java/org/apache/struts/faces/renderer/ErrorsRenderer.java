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
import java.util.Iterator;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.util.MessageResources;


/**
 * <p><code>Renderer</code> implementation for the <code>errors</code> tag
 * from the <em>Struts-Faces Integration Library</em>.</p>
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/08 02:49:54 $
 */

public class ErrorsRenderer extends AbstractRenderer {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance for this class.</p>
     */
    private static Log log = LogFactory.getLog(ErrorsRenderer.class);


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Render a combination of error messages from JavaServer Faces
     * <code>Validator</code>s, and Struts messages from form bean
     * <code>validate()</code> methods and corresponding business logic
     * error checks.</p>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent to be rendered
     *
     * @exception IOException if an input/output error occurs while rendering
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is null
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        // Look up availability of our predefined resource keys
        MessageResources resources = resources(context, component);
        Locale locale = context.getViewRoot().getLocale();
        boolean headerPresent = resources.isPresent(locale, "errors.header");
        boolean footerPresent = resources.isPresent(locale, "errors.footer");
        boolean prefixPresent = resources.isPresent(locale, "errors.prefix");
        boolean suffixPresent = resources.isPresent(locale, "errors.suffix");

        // Set up to render the error messages appropriately
        boolean headerDone = false;
        ResponseWriter writer = context.getResponseWriter();

        // Render any JavaServer Faces messages
        Iterator messages = context.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = (FacesMessage) messages.next();
            if (!headerDone) {
                if (headerPresent) {
                    writer.write
                        (resources.getMessage(locale, "errors.header"));
                }
                headerDone = true;
            }
            if (prefixPresent) {
                writer.write(resources.getMessage(locale, "errors.prefix"));
            }
            writer.write(message.getSummary());
            if (suffixPresent) {
                writer.write(resources.getMessage(locale, "errors.suffix"));
            }
        }

        // Render any Struts messages
        ActionErrors errors = (ActionErrors)
            context.getExternalContext().getRequestMap().get
            (Globals.ERROR_KEY);
        if (errors != null) {
            Iterator reports = errors.get();
            while (reports.hasNext()) {
                ActionError report = (ActionError) reports.next();
                if (!headerDone) {
                    writer = context.getResponseWriter();
                    if (headerPresent) {
                        writer.write
                            (resources.getMessage(locale, "errors.header"));
                    }
                    headerDone = true;
                }
                if (prefixPresent) {
                    writer.write
                        (resources.getMessage(locale, "errors.prefix"));
                }
                writer.write(resources.getMessage(locale, report.getKey(),
                                                  report.getValues()));
                if (suffixPresent) {
                    writer.write
                        (resources.getMessage(locale, "errors.suffix"));
                }
            }
        }

        // Append the list footer if needed
        if (headerDone && footerPresent) {
            writer.write(resources.getMessage(locale, "errors.footer"));
        }

    }



    // ------------------------------------------------------ Protected Methods


    /**
     * <p>Return the <code>MessageResources</code> bundle from which
     * we should return any Struts based error messages.  If no such
     * bundle can be located, return <code>null</code>.</p>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent to be rendered
     */
    protected MessageResources resources(FacesContext context,
                                         UIComponent component) {

        String bundle = (String) component.getAttributes().get("bundle");
        if (bundle == null) {
            bundle = Globals.MESSAGES_KEY;
        }
        return ((MessageResources)
                context.getExternalContext().getApplicationMap().get(bundle));

    }


}

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


import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;


/**
 * <p><code>Renderer</code> implementation for the <code>message</code> tag
 * from the <em>Struts-Faces Integration Library</em>.</p>
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/08 02:49:54 $
 */

public class MessageRenderer extends WriteRenderer {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance for this class.</p>
     */
    private static Log log = LogFactory.getLog(MessageRenderer.class);


    // ---------------------------------------------------------- Public Methods


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the message format String to be processed for this message.
     * </p>
     *
     * @param context FacesContext for the response we are creating
     * @param component Component to be rendered
     *
     * @exception IllegalArgumentException if no MessageResources bundle
     *  can be found
     * @exception IllegalArgumentException if no message key can be found
     */
    protected String getText(FacesContext context, UIComponent component) {

        // Look up the MessageResources bundle to be used
        String bundle = (String) component.getAttributes().get("bundle");
        if (bundle == null) {
            bundle = Globals.MESSAGES_KEY;
        }
        MessageResources resources = (MessageResources)
            context.getExternalContext().getApplicationMap().get(bundle);
        if (resources == null) { // FIXME - i18n
            throw new IllegalArgumentException("MessageResources bundle " +
                                               bundle + " not found");
        }

        // Look up the message key to be used
        Object value = component.getAttributes().get("key");
        if (value == null) {
            value = ((ValueHolder) component).getValue();
        }
        if (value == null) { // FIXME - i18n
            throw new NullPointerException("Component '" +
                                           component.getClientId(context) +
                                           "' has no current value");
        }
        String key = value.toString();

        // Build the substitution arguments list
        ArrayList list = new ArrayList();
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (!(kid instanceof UIParameter)) {
                continue;
            }
            list.add(((UIParameter) kid).getValue());
        }
        Object args[] = (Object[]) list.toArray(new Object[list.size()]);

        // Look up the requested message
        return (resources.getMessage(context.getViewRoot().getLocale(),
                                     key, args));

    }


}

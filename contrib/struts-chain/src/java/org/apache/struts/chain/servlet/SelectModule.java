/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.struts.chain.servlet;


import javax.servlet.http.HttpServletRequest;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.struts.Globals;
import org.apache.struts.chain.AbstractSelectModule;
import org.apache.struts.chain.Constants;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;


/**
 * <p>Cache the <code>ModuleConfig</code> and <code>MessageResources</code>
 * instances for the sub-application module to be used for processing
 * this request.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2004/03/08 02:50:54 $
 */

public class SelectModule extends AbstractSelectModule {


    // ------------------------------------------------------- Protected Methods


    protected String getPrefix(Context context) {

        // Identify the URI from which we will match a module prefix
        ServletWebContext swcontext = (ServletWebContext) context;
        HttpServletRequest request = swcontext.getRequest();
        String uri =
            (String) request.getAttribute(Constants.INCLUDE_SERVLET_PATH);
        if (uri == null) {
            uri = request.getServletPath();
        }
        if (uri == null) {
            throw new IllegalArgumentException
                ("No path information in request");
        }

        // Identify the module prefix for the current module
        String prefix = "";  // Initialize to default prefix
        String prefixes[] = (String[])
            swcontext.getApplicationScope().get(Globals.MODULE_PREFIXES_KEY);
        int lastSlash = 0;
        while (prefix.equals("") &&
               ((lastSlash = uri.lastIndexOf("/")) > 0)) {
            uri = uri.substring(0, lastSlash);
            for (int i = 0; i < prefixes.length; i++) {
                if (uri.equals(prefixes[i])) {
                    prefix = prefixes[i];
                    break;
                }
            }
        }

        return (prefix);

    }


}

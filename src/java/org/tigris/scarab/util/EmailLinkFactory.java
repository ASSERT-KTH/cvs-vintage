package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.fulcrum.localization.Localization;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Module;

/**
 * A factory for creating EmailLink's.  It defaults to creating
 * stock EmailLink's, but may be configured to return a subclass
 * of EmailLink.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: EmailLinkFactory.java,v 1.4 2004/04/07 21:18:34 dabbous Exp $
 */
public class EmailLinkFactory
{
    private static final String LINK_TOOL_KEY = 
        "scarab.email.link.classname";

    private static final Class linkClass;
    static
    {
        String className = Turbine.getConfiguration()
            .getString(LINK_TOOL_KEY, "");
        Class c = null;
        if (StringUtils.isNotEmpty(className))
        {
            try
            {
                c = Class.forName(className);
            }
            catch (Exception e)
            {
                Log.get().warn("Unable to to create '" + className + '\'', e);
            }
        }
        else
        {
            Log.get().info(LINK_TOOL_KEY +
                           " parameter exists, but has no value");
        }

        if (c == null)
        {
            c = EmailLink.class;
        }

        linkClass = c;
    }


    public static EmailLink getInstance(Module module)
    {
        EmailLink result = null;
        if (linkClass == null) 
        {
            result = new EmailLink(module);            
        }
        else 
        {
            try
            {
                result = (EmailLink)linkClass.newInstance();
                result.setCurrentModule(module);
            }
            catch (Exception e)
            {
                Log.get().warn("Unable to to create '" + linkClass.getName() + 
                               "'; will use default link tool.", e);
                result = new ErrorEmailLink(module);            
            }
        }
        return result;
    }

    static class ErrorEmailLink
        extends EmailLink
    {
        ErrorEmailLink()
        {
        }

        ErrorEmailLink(Module module)
        {
            super(module);
        }

        public String toString()
        {
            Module module = getCurrentModule();
            Locale locale = null;
            if (module != null) 
            {
                locale = module.getLocale();
            }
            if (locale == null) 
            {
                locale = ScarabConstants.DEFAULT_LOCALE;
            }
            return Localization.getString(ScarabConstants.DEFAULT_BUNDLE_NAME,
                                          locale,
                                          "EmailLinkError");
        }
    }
}

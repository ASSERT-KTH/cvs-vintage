package org.tigris.scarab.security;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
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
 * individuals on behalf of Collab.Net.
 */ 

import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.util.Log;

import org.tigris.scarab.util.ScarabConstants;

/**
 *  Returns an instance of the SearchIndex specified in Scarab.properties
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: SecurityFactory.java,v 1.1 2001/07/21 00:52:51 jmcnally Exp $
 */
public class SecurityFactory
{
    private static final ScarabSecurity baseSecurity;

    static
    {
        String pullClassName = Turbine.getConfiguration()
            .getString(ScarabSecurity.TOOL_KEY);
        if ( pullClassName == null || pullClassName.length() == 0 ) 
        {
            pullClassName = 
                "org.tigris.scarab.security.DefaultScarabSecurityPull";
        }
        
        String baseClassName = pullClassName
            .substring(0, pullClassName.length()-4);
        ScarabSecurity base = null;
        try
        {
            base = (ScarabSecurity)Class.forName(baseClassName).newInstance();
        }
        catch (Exception e)
        {
            base = new DefaultScarabSecurity();
            Log.warn("Security class was not specified, so maximum" +
                     " restrictions are in place.");
        }
        baseSecurity = base;
    }

    /**
     * Returns an instance of ScarabSecurity based on the security pull tool
     * specified in TurbineResources.properties. This instance has no
     * attributes so the same instance is always returned.
     *
     * @return a <code>ScarabSecurity</code> value
     */
    public static ScarabSecurity getInstance()
    {
        return baseSecurity;
    }

    /**
     * Returns an instance of ScarabSecurity based on the security pull tool
     * specified in TurbineResources.properties.  This instance will be
     * returned to a pool at the end of one request cycle.
     *
     * @return a <code>ScarabSecurity</code> value
     */
    public static ScarabSecurity getInstance(TemplateContext ctx)
    {
        return (ScarabSecurityPull)ctx
            .get(ScarabConstants.SECURITY_TOOL);

    }


}

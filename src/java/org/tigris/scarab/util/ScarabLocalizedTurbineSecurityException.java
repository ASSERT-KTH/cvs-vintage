package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.Localizable;

/**
 * A TurbineSecurityException ready for internationalization [in the Scarab L10N framework].
 *
 * @version $Id: ScarabLocalizedTurbineSecurityException.java,v 1.1 2004/05/01 19:10:51 dabbous Exp $
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */
public class ScarabLocalizedTurbineSecurityException extends TurbineSecurityException implements Localizable
{
    private final Throwable throwable;

    // may be null
    private ScarabLocalizationTool localizer;
    
    
    /**
     * Constructs a TurbineSecurityException wrapper for a given exception.
     * The wrapper is simply a container with no special
     * functionality.
     * @param e
     */
    public ScarabLocalizedTurbineSecurityException(final Throwable t)
    {
        super("");
        throwable = t;
        localizer = null;
    }
    
    /**
     * Set the localizer to be used in later calls to {@link #getLocalizedMessage()}
     * @param theLocalizer the localizer (may be <code>null</code>)
     */
     public void setLocalizer(final ScarabLocalizationTool theLocalizer)
     {
        localizer = theLocalizer;
     }
    
    /**
     * Delegator for the wrapped exceptions getMessage() method.
     * @return the wrapped exception's message
     */
    public String getMessage()
    {
        return throwable.getMessage();
    }


    /**
     * Localize this exception using the wrapped exception.
     * return the localized message, else return the message string
     * of the wrapped exception.
     * @param l10n
     * @return
     */
    public String getMessage(final ScarabLocalizationTool l10n)
    {
        return l10n.getMessage(throwable);
    }    
    
    /**
     * Return the localized message for that throwable, if a localizer
     * was defined using {@link #setLocalizer(ScarabLocalizationTool)}
     * @return the localized message.
     */
    public String getLocalizedMessage()
    {   
        if (localizer != null)
        {
            return getMessage(localizer);
        } 
        else
        {
            return super.getLocalizedMessage();
        }
    }

}

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

// Turbine
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.tools.localization.Localizable;

/**
    This class extends TurbineException and does not change its
    functionality.  It should be used to mark Scarab specific 
    exceptions. 
    In order to ensure localization of Exception messages,
    ScarabRuntimeException adds a new type of message, the L10NMessage.
    
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: ScarabRuntimeException.java,v 1.2 2004/12/02 21:32:50 dabbous Exp $
*/
public class ScarabRuntimeException extends RuntimeException implements Localizable
{
    /**
     * The exception message in non-localized form.
     * Further infos, see the {@link #getMessage(L10N) getmessage } methods below.
     */
    Localizable l10nMessage;
    
    /**
     * Placeholder for a nested exception (may be null)
     */
    Throwable nested;

    /**
     * Constructs a new <code>ScarabRuntimeException</code> with specified 
     * resource and no parameters.
     * @param theKey the l10n error key.
     */
    public ScarabRuntimeException(LocalizationKey theKey)
    {
         l10nMessage = new L10NMessage(theKey);
         nested      = null;
    }

    /**
     * Constructs a new <code>ScarabRuntimeException</code> with specified 
     * resource and a nested Throwable.
     * @param theKey the l10n error key.
     * @param aNested
     */
    public ScarabRuntimeException(LocalizationKey theKey, Throwable aNested)
    {
        super();
        l10nMessage = new L10NMessage(theKey, nested);
        nested      = aNested;
    }


    /**
     * Constructs a new <code>ScarabRuntimeException</code> with specified 
     * Localizable .
     * @param theL10nInstance the l10n error key.
     */
    public ScarabRuntimeException(Localizable theL10nInstance)
    {
         l10nMessage = theL10nInstance;
         nested      = null;
    }
 
    /**
     * Constructs a new <code>ScarabRuntimeException</code> with specified 
     * Localizable and a nested Throwable.
     * @param theL10nInstance the l10n error key.
     * @param aNested
     */
    public ScarabRuntimeException(Localizable theL10nInstance, Throwable aNested)
    {
        super();
        l10nMessage = theL10nInstance;
        nested      = aNested;
    }

    
    /**
     * Constructs a new <code>ScarabRuntimeException</code> with specified 
     * resource and a list of parameters.
     * @param theL10nInstance the l10n error key.
     * @param theParams
     */
    public ScarabRuntimeException (LocalizationKey theKey, Object[] theParams)
    {
        l10nMessage = new L10NMessage(theKey, theParams);
        nested      = null;
    }
 
    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource and one parameter.
     * @param theL10nInstance the l10n error key.
     * @param p1
     */
    public ScarabRuntimeException (LocalizationKey theKey, Object p1)
    {
        this(theKey, new Object[] {p1});
    }
 
    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource and two parameters.
     * @param theL10nInstance the l10n error key.
     * @param p1
     * @param p2
     */
    public ScarabRuntimeException (LocalizationKey theKey, Object p1, Object p2)
    {
        this(theKey, new Object[] {p1, p2});
    }
 
    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource and three parameters.
     * @param theL10nInstance the l10n error key.
     * @param p1
     * @param p2
     * @param p3
     */
    public ScarabRuntimeException (LocalizationKey theKey, Object p1, Object p2, Object p3)
    {
        this(theKey, new Object[] {p1, p2, p3});
    }
  

    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource, nested Throwable and an aritrary set of parameters.
     * 
     * @param theKey
     * @param nested
     * @param theParams
     */
    public ScarabRuntimeException (LocalizationKey theKey, Throwable nested, Object[] theParams)
    {
        this(new L10NMessage(theKey, theParams),nested);
    }

    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource, nested Throwable and one parameter.
     * @param theKey
     * @param nested
     * @param p1
     */
    public ScarabRuntimeException (LocalizationKey theKey, Throwable nested, Object p1)
    {
        this(new L10NMessage(theKey, p1),nested);
    }

    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource, nested Throwable and two parameters.
     * @param theKey
     * @param nested
     * @param p1
     * @param p2
     */
    public ScarabRuntimeException (LocalizationKey theKey, Throwable nested, Object p1, Object p2)
    {
        this(new L10NMessage(theKey, p1, p2),nested);
    }

    /**
     * convenience constructor: Constructs a new <code>ScarabRuntimeException</code>
     * with specified resource, nested Throwable and three parameters.
     * @param theKey
     * @param nested
     * @param p1
     * @param p2
     * @param p3
     */
    public ScarabRuntimeException (LocalizationKey theKey, Throwable nested, Object p1, Object p2, Object p3)
    {
        this(new L10NMessage(theKey, p1, p2, p3),nested);
    }

    /**
     * return the L10NInstance, or null, if no L10N key was given.
     * @return
     */
    public Localizable getL10nMessage()
    {
        return l10nMessage;
    }
    
    /** 
     * return the localized message by use of the
     * given ScarabLocalizationTool. For further infos see
     * {@link #getMessage() getMessage }
     *
     * @param l10n
     * @return
     */
    public String getMessage(ScarabLocalizationTool l10n)
    {
        String result;
        if (l10nMessage == null)
        {
            if (nested == null)
            {
                result = super.getMessage();
            }
            else
            {
                if (  nested instanceof ScarabRuntimeException )
                {
                    result = ((ScarabRuntimeException)nested).getMessage(l10n);
                }
                else if (  nested instanceof ScarabException )
                {
                    result = ((ScarabException)nested).getMessage(l10n);
                }
                else
                {
                    result = nested.getMessage();
                }
            }
        }
        else
        {
            result = l10nMessage.getMessage(l10n);
        }
        return result;
    }
 
    /**
     * return the localized message in english.
     * Note: It is preferrable to use 
     * {@link #getMessage(ScarabLocalizationTool) getMessage }
     *
     * @return localized english text
     */
    public String getMessage()
    {
        String result;
        if (l10nMessage == null)
        {
            if (nested == null)
            {
                result = super.getMessage();
            }
            else
            {
                result = nested.getMessage();
            }
        }
        else
        {
            result = l10nMessage.toString();
        }
        return result;
    }

}

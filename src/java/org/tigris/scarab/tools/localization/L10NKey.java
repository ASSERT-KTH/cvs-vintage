package org.tigris.scarab.tools.localization;

import org.tigris.scarab.tools.ScarabLocalizationTool;


/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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

/**
 * Default implementation of a Localization Key.
 * <p>
 * Acts as a simple wrapper around the real key. Instances are immutable.
 * 
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */
public final class L10NKey implements LocalizationKey, Localizable
{
    

    /**
     * The key. 
     * Only used internally from ScarabLocalizationTool.
     */
    private final String key;

    /**
     * @return the <code>String<code> representation of the key.
     * Only used from L10NKeySet to construct keys.
     * @param theKey
     */
    public String toString()
    {
        return key;
    }

    /**
     * Constructs a L10NKey instance using the specified key.
     * Only used internaly from ScarabLocalizationTool.
     * @param theKey the final <code>String<code> representation of the key
     */
    public L10NKey(String theKey)
    {
        key = theKey;
    }

    /* 
     * Return the string representation of this key for the DEFAULT_LOCALE
     * @see org.tigris.scarab.tools.localization.Localizable#getMessage()
     */
    public String getMessage()
    {
        ScarabLocalizationTool l10n = new ScarabLocalizationTool();
        l10n.init(ScarabLocalizationTool.DEFAULT_LOCALE);
        return getMessage(l10n);
    }

    /*
     * Return the string representation of this key for the goven locale.
     * @see org.tigris.scarab.tools.localization.Localizable#getMessage(org.tigris.scarab.tools.ScarabLocalizationTool)
     */
    public String getMessage(ScarabLocalizationTool l10n)
    {
        return l10n.get(this);
    }

}

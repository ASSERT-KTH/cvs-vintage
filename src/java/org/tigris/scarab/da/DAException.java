package org.tigris.scarab.da;

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

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.ScarabException;

/**
 * The unchecked exception thrown to indicate data access layer
 * problems.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 */
public class DAException
    extends ScarabException
{

    /**
     * Constructs a new <code>DAException</code> with specified 
     * detail message.
     *
     * @param msg the error message.
     */
    public DAException(LocalizationKey l10nKey)
    {
        super(l10nKey);
    }

    /**
     * Constructs a new <code>DAException</code> with specified 
     * detail message.
     *
     * @param msg the error message.
     */
    public DAException(L10NMessage l10nMessage)
    {
        super(l10nMessage);
    }

    /**
     * Constructs a new <code>DAException</code> with specified 
     * detail message.
     *
     * @param msg the error message.
     */
    public DAException(L10NMessage l10nMessage, Throwable nested)
    {
        super(l10nMessage, nested);
    }

    /**
     * Constructs a new <code>DAException</code> with specified 
     * resource and a list of parameters.
     * @param theKey the l10n error key.
     */
    public static ScarabException create(LocalizationKey theKey, Object[] theParams)
    {
        L10NMessage l10nMessage = new L10NMessage(theKey, theParams);
        return new DAException(l10nMessage);
    }

 
    /**
     * Convenience method: Constructs a new <code>DAException</code>
     * with specified resource, nested Throwable and an aritrary set of parameters.
     * @param theKey the l10n error key.
     */
    public static ScarabException create(LocalizationKey theKey, Throwable nested, Object[] theParams)
    {
        L10NMessage l10nMessage = new L10NMessage(theKey, theParams);
        ScarabException result = new DAException(l10nMessage, nested);
        return result;
    }
}

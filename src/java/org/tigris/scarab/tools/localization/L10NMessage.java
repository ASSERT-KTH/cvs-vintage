package org.tigris.scarab.tools.localization;


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


import org.tigris.scarab.tools.ScarabLocalizationTool;

/**
 * This class defines localizable messages. Each Resource  
 * may contain dynamic parameters ({$1}, {$2}, etc.) These
 * parameters get resolved, when the ResourceBundle is called
 * for the String representation of a specific Resource key.
 *
 * L10NMessage always contains an L10N key and may contain
 * an arbitrary set of parameters, which will be used as values
 * for the {$n} variables in the Resource.
 * 
 * Note: We check for two special cases:
 * <ol>
 *  <li>If a paramater is itself an L10NInstance, it will be resolved, 
 *      before it's value is used.</li>
 *  <li>If a parameter is a scarabException, it is resolved, before
 *      it's value is used.</li>
 *  <li>If a parameter is an ordinary Exception, it's stored message is
 *      retrieved via Exception.getMessage() before it's value is used.
 *  </li>
 * </ol>
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */
public class L10NMessage implements Localizable
{
    /**
     * The Localization key to be used.
     */
    LocalizationKey l10nKey;

    /**
     * The list of localization parameters. may contain L10NInstnaces and
     * Exceptions. May be null (no parameters)
     */
    private Object[] parameters;

    /**
     * Constructor always needs the L10N key for creation.
     * @param theKey
     */
    public L10NMessage(LocalizationKey theKey)
    {
        l10nKey = theKey;
        this.parameters = null;
    }

    /**
     * Constructor with parameters. theParameters is an array of objects
     * and may contain Exceptions and
     * L10NInstances.
     * @param theKey
     * @param theParameters
     */
    public L10NMessage(LocalizationKey theKey, Object[] theParameters)
    {
        l10nKey = theKey;
        this.parameters = theParameters;
    }

    /**
     * Convenience constructor with one extra parameter.
     * @param theKey
     * @param p1
     */
    public L10NMessage(LocalizationKey theKey, Object p1)
    {
        this(theKey, new Object[]{p1});
    }

    /**
     * Convenience constructor with two extra parameters.
     * @param theKey
     * @param p1
     * @param p2
     */
    public L10NMessage(LocalizationKey theKey, Object p1, Object p2)
    {
        this(theKey, new Object[]{p1, p2});
    }

    /**
     * Convenience constructor with three extra parameters.
     * @param theKey
     * @param p1
     * @param p2
     * @param p3
     */
    public L10NMessage(LocalizationKey theKey, Object p1, Object p2, Object p3)
    {
        this(theKey, new Object[]{p1, p2, p3});
    }

    /**
     * resolve the instance to the ScarabLocalizationTool.DEFAULT_LOCALE
     * Note: This method returns english messages independent of
     * any l10n settings. it is preferreable to use 
     * {@link resolve(ScarabLocalizationTool) }
     * @return the resolved String
     */
    public String getMessage()
    {
        ScarabLocalizationTool l10n = new ScarabLocalizationTool();
        l10n.init(ScarabLocalizationTool.DEFAULT_LOCALE);
        return getMessage(l10n);
    }

    /**
     * Format the message using the specified ScarabLocalizationTool instance. 
     * The parameters are resolved recursively if necessary.
     * @return a localized <code>String</code> representation of this message.
     */
    public String getMessage(final ScarabLocalizationTool l10n)
    {
        final int nbParameters = (parameters == null ? 0 : parameters.length);
        final Object[] formatedParameters = new Object[nbParameters];
        for (int index = 0; index < nbParameters; index++)
        {
            Object param = parameters[index];
            if (param instanceof Localizable)
            {
                formatedParameters[index] = 
                    ((Localizable) param).getMessage(l10n);
            }
            else if (param instanceof Throwable)
            {
                // Note: We can not simply keep the parameter
                // as is, because MessageFormat internally uses
                // toString() which in turn should call Throwable.getLocalizedMessage() 
                // but:
                // 1- Throwable.toString() is declared to use getMessage() even, 
                //    when it also implements getLocalizedMessage()
                // 2- Throwable.toString() also appends the class name, which we 
                //    don't want.
                //
                // so we force the localize message ourselves:
                Throwable t = (Throwable) param;
                formatedParameters[index] = t.getLocalizedMessage();
            }
            else
            {
                formatedParameters[index] = param;
            }
        }
        return l10n.format(l10nKey.toString(), formatedParameters);
    }

}

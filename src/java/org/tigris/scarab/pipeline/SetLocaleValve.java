package org.tigris.scarab.pipeline;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.fulcrum.localization.Localization;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.log4j.Logger;
import org.apache.turbine.RunData;
import org.apache.turbine.TurbineException;
import org.apache.turbine.pipeline.AbstractValve;
import org.apache.turbine.ValveContext;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.UserPreference;
import org.tigris.scarab.om.UserPreferenceManager;

/**
 * This valve checks for changes in the browser locale preference settings.
 * Every time the user changes their locale settings, Scarab will update
 * its internal database to reflect these changes. From then on the user
 * gets their emails in the new preferred language. 
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */
public class SetLocaleValve extends AbstractValve
{
    private static final Logger LOG = Logger.getLogger(SetLocaleValve.class);

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(RunData data, ValveContext context)
        throws IOException, TurbineException
    {
        ScarabUser user = (ScarabUser) data.getUser();

        // Ensure we only deal with exsiting users
        if (user != null && user.getUserId() != null)
        {
            try
            {
                String acceptLanguage = LocalizationService.ACCEPT_LANGUAGE;
                HttpServletRequest request = data.getRequest();
                String browserLocaleAsString =
                    request.getHeader(acceptLanguage);

                Locale userLocale = user.getLocale();
                Locale browserLocale =
                    Localization.getLocale(browserLocaleAsString);

                if (!userLocale.equals(browserLocale))
                {
                    storePreferredLocale(user, browserLocaleAsString);
                }
            }
            catch (Exception e)
            {
                LOG.warn(
                    "Could not set locale info for user ["
                        + user.getName()
                        + "]");
            }
        }
        // Pass control to the next Valve in the Pipeline
        context.invokeNext(data);
    }

    /**
     * Store the locale settings as received from browser into the
     * database.
     * @param user
     * @param localeAsString
     * @throws Exception
     */
    private void storePreferredLocale(ScarabUser user, String localeAsString)
        throws Exception
    {
    	// Change the settings in the database
        UserPreference pref =
            UserPreferenceManager.getInstance(user.getUserId());
        pref.setLocale(localeAsString);
        pref.save();

        // The internal user caches the current locale,
        // hence we need to modify the user's locale now.
		Locale locale = Localization.getLocale(localeAsString);
        user.setLocale(locale);
    }
}

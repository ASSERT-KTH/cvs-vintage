package org.apache.fulcrum.localization;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.fulcrum.TurbineServices;
import org.apache.turbine.services.yaaficomponent.YaafiComponentService;

/**
 * <p>Wrapper around the TurbineLocalization Service that makes it easy
 * to grab something from the service.</p>
 *
 * <p>Instead of typing:
 *
 * <blockquote><code><pre>
 * ((LocalizationService)TurbineServices.getInstance()
 *           .getService(LocalizationService.SERVICE_NAME))
 *     .getBundle(data)
 *     .getString(key)
 * </pre></code></blockquote>
 *
 * You need only type:
 *
 * <blockquote><code><pre>
 * Localization.getString(key)
 * </pre></code></blockquote>
 * </p>
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a>
 * @version $Id: Localization.java,v 1.3 2004/11/23 08:26:11 dep4b Exp $
 */
public abstract class Localization
{
    private static LocalizationService localizationService;
    /**
     * Pulls a string out of the LocalizationService with the default
     * locale values of what is defined in the
     * TurbineResources.properties file for the
     * locale.default.language and locale.default.country property
     * values.  If those cannot be found, then the JVM defaults are
     * used.
     *
     * @param key Name of string.
     * @return A localized String.
     */
    public static String getString(String key)
    {
        return getService().getBundle().getString(key);
    }

    /**
     * @see LocalizationService#getString(String, Locale, String)
     */
    public static String getString(Locale locale, String key)
    {
        return getService().getString(null, locale, key);
    }

    /**
     * Fetches the localized text from the specified bundle, ignoring
     * any default bundles.
     *
     * @see LocalizationService#getString(String, Locale, String)
     */
    public static String getString(String bundleName, Locale locale,
                                   String key)
    {
        return getService().getString(bundleName, locale, key);
    }

    /**
     * Convenience method that pulls a localized string off the
     * LocalizationService using the ResourceBundle based on HTTP
     * Accept-Language header in HttpServletRequest.
     *
     * @param req The HTTP request to parse the <code>Accept-Language</code> of.
     * @param key Name of string.
     * @return A localized string.
     */
    public static String getString(HttpServletRequest req, String key)
    {
        return getService().getBundle(req).getString(key);
    }

    /**
     * Convenience method that pulls a localized string off the
     * LocalizationService using the default ResourceBundle name
     * defined in the TurbineResources.properties file and the
     * specified language name in ISO format.
     *
     * @param key Name of string.
     * @param lang Desired language for the localized string.
     * @return A localized string.
     */
    public static String getString(String key, String lang)
    {
        return getBundle(getDefaultBundleName(), new Locale(lang, ""))
            .getString(key);
    }

    /**
     * Convenience method to get a ResourceBundle based on name.
     *
     * @param bundleName Name of bundle.
     * @return A localized ResourceBundle.
     */
    public static ResourceBundle getBundle(String bundleName)
    {
        return getService().getBundle(bundleName);
    }

    /**
     * Convenience method to get a ResourceBundle based on name and
     * HTTP Accept-Language header.
     *
     * @param bundleName Name of bundle.
     * @param languageHeader A String with the language header.
     * @return A localized ResourceBundle.
     */
    public static ResourceBundle getBundle(String bundleName,
                                           String languageHeader)
    {
        return getService().getBundle(bundleName, languageHeader);
    }

    /**
     * Convenience method to get a ResourceBundle based on name and
     * HTTP Accept-Language header in HttpServletRequest.
     *
     * @param req HttpServletRequest.
     * @return A localized ResourceBundle.
     */
    public static ResourceBundle getBundle(HttpServletRequest req)
    {
        return getService().getBundle(req);
    }

    /**
     * Convenience method to get a ResourceBundle based on name and
     * HTTP Accept-Language header in HttpServletRequest.
     *
     * @param bundleName Name of bundle.
     * @param req HttpServletRequest.
     * @return A localized ResourceBundle.
     */
    public static ResourceBundle getBundle(String bundleName,
                                           HttpServletRequest req)
    {
        return getService().getBundle(bundleName, req);
    }

    /**
     * Convenience method to get a ResourceBundle based on name and
     * Locale.
     *
     * @param bundleName Name of bundle.
     * @param locale A Locale.
     * @return A localized ResourceBundle.
     */
    public static ResourceBundle getBundle(String bundleName, Locale locale)
    {
        return getService().getBundle(bundleName, locale);
    }

    /**
     * This method sets the name of the default bundle.
     *
     * @param defaultBundle Name of default bundle.
     * @see LocalizationService#setBundle(String)
     */
    public static void setBundle(String defaultBundle)
    {
        getService().setBundle(defaultBundle);
    }

    /**
     * @see LocalizationService#getLocale(HttpServletRequest)
     */
    public static Locale getLocale(HttpServletRequest req)
    {
        return getService().getLocale(req);
    }

    /**
     * This method parses the <code>Accept-Language</code> header and
     * attempts to create a Locale out of it.
     *
     * @param languageHeader A String with the language header.
     * @return A Locale.
     */
    public static Locale getLocale(String languageHeader)
    {
        return getService().getLocale(languageHeader);
    }

    /**
     * @see LocalizationService#getDefaultBundleName()
     */
    public static String getDefaultBundleName()
    {
        return getService().getDefaultBundleName();
    }

    /**
     * @see LocalizationService#getDefaultCountry()
     */
    public static String getDefaultCountry()
    {
        return getService().getDefaultCountry();
    }

    /**
     * @see LocalizationService#getDefaultLanguage()
     */
    public static String getDefaultLanguage()
    {
        return getService().getDefaultLanguage();
    }

    /**
     * Gets the <code>LocalizationService</code> implementation.
     *
     * @return the LocalizationService implementation.
     */
    protected static final LocalizationService getService()
    {
        if (localizationService==null){
        try{
            YaafiComponentService yaafi = (YaafiComponentService) TurbineServices.getInstance().getService(
                YaafiComponentService.SERVICE_NAME);
            localizationService =  (LocalizationService) yaafi.lookup(LocalizationService.class.getName());
        } 
        catch (Exception e) {
            throw new RuntimeException("Problem looking up localization service", e);
        }
        }
        return localizationService;
    }
    
    public static void setLocalizationService(LocalizationService service){
        localizationService = service;
    }

    /**
     * @see LocalizationService#format(String, Locale, String, Object)
     */
    public static String format(String bundleName, Locale locale,
                                String key, Object arg1)
    {
        return getService().format(bundleName, locale, key, arg1);
    }

    /**
     * @see LocalizationService#format(String, Locale, String, Object, Object)
     */
    public static String format(String bundleName, Locale locale,
                                String key, Object arg1, Object arg2)
    {
        return getService().format(bundleName, locale, key, arg1, arg2);
    }

    /**
     * @see LocalizationService#format(String, Locale, String, Object[])
     */
    public static String format(String bundleName, Locale locale,
                                String key, Object[] args)
    {
        return getService().format(bundleName, locale, key, args);
    }


    // ---- Deprecated method(s) -------------------------------------------

    /**
     * @deprecated Use getString(Locale, String) instead.
     */
    public static String getString(String key, Locale locale)
    {
        return getString(locale, key);
    }


    /**
     * @deprecated Use getString(HttpServletRequest, String) instead.
     */
    public static String getString(String key, HttpServletRequest req)
    {
        return getString(req, key);
    }
}

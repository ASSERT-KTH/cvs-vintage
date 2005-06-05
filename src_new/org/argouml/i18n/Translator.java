// $Id: Translator.java,v 1.33 2005/06/05 15:13:08 linus Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.tigris.gef.util.Localizer;

/**
 * The API class to the localization. All localization calls goes through
 * this class.
 *
 * @author Jean-Hugues de Raigniac
 */
public final class Translator {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(Translator.class);

    /**
     * Where we search for bundles.
     */
    private static final String BUNDLES_PATH = "org.argouml.i18n";

    /**
     * Store bundles for current Locale.
     */
    private static Map bundles;

    /**
     * This class should only be used in a static constant so make
     * the constructor private.
     */
    private Translator() {
    }

    static {
        setLocale(new Locale(
                System.getProperty("user.language", "en"),
                System.getProperty("user.country", "")));
    }

    /**
     * Default Locale is set and resources Bundles are loaded.
     */
    public static void init () {
        Localizer.addResource("GefBase",
			      "org.tigris.gef.base.BaseResourceBundle");
        Localizer.addResource(
		"GefPres",
		"org.tigris.gef.presentation.PresentationResourceBundle");
        Localizer.addResource("UMLMenu",
			      "org.argouml.i18n.UMLResourceBundle");
    }

    /**
     * For Locale selection.<p>
     *
     * TODO: Detect the available locales from the available files.
     *
     * @return Locales used in ArgoUML
     */
    public static Locale[] getLocales() {
        return new Locale[] {
            new Locale("en", ""),
            new Locale("fr", ""),
            new Locale("es", ""),
            new Locale("nb", ""),
            new Locale("ru", ""),
            new Locale("zh", ""),
            new Locale("en", "GB"),
        };
    }

    /**
     * Change the current Locale.
     *
     * @param locale the new Locale
     */
    public static void setLocale(Locale locale) {
        Locale.setDefault(locale);
        bundles = new HashMap();
    }

    /**
     * Loads the bundle (if not already loaded).
     *
     * @param name The name of the bundle to load.
     */
    private static void loadBundle(String name) {
        if (bundles.containsKey(name)) {
            return;
        }
        String resource = BUNDLES_PATH + "." + name;
        ResourceBundle bundle = null;
        try {
            LOG.debug("Loading " + resource);
            bundle = ResourceBundle.getBundle(resource, Locale.getDefault());
        } catch (MissingResourceException e1) {
            LOG.debug("Resource " + resource + " not found.");
        }

        bundles.put(name, bundle);
    }

    /**
     * Calculate the name from the key.
     *
     * @param key The key to look up.
     * @return The name of the file or <code>null</code> if not possible.
     */
    private static String getName(String key) {
        if (key == null) {
            return null;
        }

        int indexOfDot = key.indexOf(".");
        if (indexOfDot > 0) {
            return key.substring(0, indexOfDot);
        }
        return null;
    }


    /**
     * Helper for those that don't want to give the bundle.<p>
     *
     * @param key The key to localize.
     * @return The localized String.
     */
    public static String localize(String key) {
        assert key != null;

        String name = getName(key);
        if (name == null) {
            return Localizer.localize("UMLMenu", key);
        }

        loadBundle(name);

        ResourceBundle bundle = (ResourceBundle) bundles.get(name);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            LOG.debug("Resource " + key + " not found.");
            return key;
        }
    }

    /**
     * Generates an localized String with arguments.<p>
     *
     * The localized string is a pattern to be processed by
     * {@link MessageFormat}.
     *
     * @param key the key to localize
     * @param args the args as Objects, inserted in the localized String
     * @return the localized String with inserted arguments
     */
    public static String messageFormat(String key, Object[] args) {
        return new MessageFormat(localize(key)).format(args);
    }
}

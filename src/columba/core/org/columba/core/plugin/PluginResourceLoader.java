//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.plugin;

import org.columba.core.main.MainInterface;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides internationalization support for plugins.
 *
 * @author fdietz
 */
public class PluginResourceLoader {
    protected String pluginId;
    protected File pluginFolder;
    protected Map map;

    /**
 *
 */
    public PluginResourceLoader(String pluginId) {
        this.pluginId = pluginId;
        pluginFolder = MainInterface.pluginManager.getFolder(pluginId);
        map = new HashMap();
    }

    public String getString(String propertyFile, String resource) {
        if (map.containsKey(propertyFile)) {
            // use already cached ResouceBundle class
            try {
                return ((ResourceBundle) map.get(propertyFile)).getString(resource);
            } catch (MissingResourceException ex) {
                return resource;
            }
        }

        URLClassLoader loader = null;
        // create File from directory and name of property file
        File rcFile = new File(pluginFolder, propertyFile);

        try {
            // initialize URL classloader with rcFile url
            URL[] urls = new URL[] {rcFile.toURL()};
            loader = new URLClassLoader(urls);
        } catch (MalformedURLException ex) {} //does not happen

        try {
            // create resource bundle
            // -> pass current locale and classloader
            ResourceBundle rb = ResourceBundle.getBundle(rcFile.getPath(),
                    Locale.getDefault(), loader);
            map.put(propertyFile, rb);
            
            // return string if available
            return rb.getString(resource);
        } catch (MissingResourceException ex) {
            return resource;
        }
    }
}

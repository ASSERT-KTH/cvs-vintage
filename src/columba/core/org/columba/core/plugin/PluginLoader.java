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

import org.columba.core.loader.ExternalClassLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.pluginhandler.*;
import org.columba.core.scripting.AbstractInterpreter;

import java.io.File;

import java.net.URL;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * PluginLoader handles the different kinds of classloaders.
 * <p>
 * Possible candidates can be java classes in Columba or outside
 * and classes, which are handled by the interpreter plugin.
 * (python classes at this time only)
 *
 *
 *  @author freddy
 */
public final class PluginLoader {

    private static final Logger LOG = Logger.getLogger("org.columba.core.plugin");

    /**
     * Constructor for PluginLoader.
     */
    private PluginLoader() {
    }

    /**
     * @param className                name of class
     * @param type                        type of plugin (java|jar|python)
     * @param file                        File which specifies the class/jar/py file
     * @param args                        arguments for the plugin
     * @return                                instance of plugin
     * @throws Exception
     */
    public static Object loadExternalPlugin(String className, String type,
        File file, Object[] args) throws Exception {
        if ((className == null) || (type == null) || (file == null)) {
            return null;
        }

        if (MainInterface.DEBUG) {
            LOG.fine("loading.. " + className);
        }

        if (type.equals("java") || type.equals("jar")) {
            // plugin-directory
            String path = file.getPath();

            List urlList = new Vector();

            URL newURL = new File(path).toURL();
            urlList.add(newURL);

            // we add every jar-file in /lib, too
            // plugin-directory
            File directory = file.getParentFile();

            File lib = new File(directory, "lib");

            if (lib.exists()) {
                File[] libList = lib.listFiles();

                for (int i = 0; i < libList.length; i++) {
                    File f = libList[i];

                    if (f.getName().endsWith(".jar")) {
                        // jar-file found
                        urlList.add(f.toURL());
                    }
                }
            }

            URL[] url = new URL[urlList.size()];

            for (int i = 0; i < urlList.size(); i++) {
                url[i] = (URL) urlList.get(i);
            }

            LOG.fine("url=" + newURL);

            return new ExternalClassLoader(url).instanciate(className, args);
        }

        InterpreterHandler handler = (InterpreterHandler) MainInterface.pluginManager.getHandler(
                "org.columba.core.interpreter");

        Object instance = handler.getInterpreter(type);

        if (instance != null) {
            AbstractInterpreter ip = (AbstractInterpreter) instance;

            String pythonFile = file.toString() + "/" + className;

            String pythonClass = className.substring(0,
                    className.length() - 3);

            Object i = ip.instanciate(pythonFile, pythonClass, args, "test");

            return i;
        }

        return null;
    }
}

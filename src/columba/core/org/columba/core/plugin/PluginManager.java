// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.gui.plugin.AbstractConfigPlugin;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.io.DiskIO;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * 
 * The plugin manager is the central place for all plugin related operations.
 * <p>
 * It manages all plugin handlers. Plugin handlers need to register at the
 * plugin handler.
 * <p>
 * On startup the plugin manager goes through all plugins found in the plugins
 * directory and registers them at the plugin handlers.
 * <p>
 * It offers a common set of operations all plugins share. These are: -
 * enable/disable plugin - get URL of readme.txt/readme.html file shipped with
 * plugin - get folder of plugin - get plugin.xml configuration
 * <p>
 * It therefore saves all plugin id's in a list. Additionally it uses a HashMap
 * to save all plugin folders.
 * 
 * @author fdietz
 */
public class PluginManager {

    private static final Logger LOG = Logger
            .getLogger("org.columba.core.plugin");

    Map elements;

    /**
     * 
     * Save all plugin directories in this <interface>Map </interface>. Use
     * plugin id as key, <class>File </class> storing the directory as value.
     *  
     */
    Map folders;

    /**
     * 
     * Save all plugin id's in this <interface>List </interface>.
     *  
     */
    List ids;

    Map jarFiles;

    /**
     * 
     * Save all plugin handlers in this <interface>Map </interface>. Use plugin
     * handler id as key, <interface>PluginHandler </interface> as
     * value
     *  
     */
    Hashtable pluginHandlers;

    /**
     * Constructor for PluginManager.
     */
    public PluginManager() {
        super();

        // init map
        pluginHandlers = new Hashtable(10);
    }

    public String addPlugin(File folder) {

        LOG.fine("registering plugin: " + folder);

        // load plugin.xml file
        // skip if it doesn't exist
        File xmlFile = new File(folder, "plugin.xml");

        if (xmlFile == null) { return null; }

        if (!xmlFile.exists()) { return null; }

        XmlIO config = new XmlIO();

        try {
            config.setURL(xmlFile.toURL());
        } catch (MalformedURLException mue) {
        }

        config.load();

        // determine plugin ID
        XmlElement element = config.getRoot().getElement("/plugin");
        String id = element.getAttribute("id");
        ids.add(id);
        elements.put(id, element);
        folders.put(id, folder);
        jarFiles.put(id, folder);

        XmlElement runtime = element.getElement("runtime");

        //String type = runtime.getAttribute("type");
        String jar = runtime.getAttribute("jar");

        if (jar != null) {
            jarFiles.put(id, new File(folder, jar));
        }

        LOG.fine("id: " + id);

        LOG.fine("jar: " + jar);

        XmlElement extension;
        String extensionPoint;

        // loop through all extensions this plugin uses
        // -> search the corresponding plugin handler
        // -> register the plugin at the plugin handler
        for (int j = 0; j < element.count(); j++) {
            extension = element.getElement(j);

            if (extension.getName().equals("extension")) {
                extensionPoint = extension.getAttribute("name");

                if (pluginHandlers.containsKey(extensionPoint)) {
                    // we have a plugin-handler for this kind of plugin
                    try {
                        AbstractPluginHandler handler = (AbstractPluginHandler) pluginHandlers
                                .get(extensionPoint);

                        File file = null;
                        file = folder;

                        LOG.fine("debug: " + file.toString());

                        handler.addExtension(id, extension);
                    } catch (Exception ex) {
                        LOG.severe(ex.getMessage());
                    }
                } else {
                    LOG.severe("No suitable plugin handler with name "
                            + extensionPoint + " found");
                }
            }
        }

        return id;
    }

    /**
     * Gets top level tree xml node of config.xml
     * <p>
     * This can be used in conjunction with {@link AbstractConfigPlugin}as an
     * easy way to configure plugins.
     * 
     * @param id
     *            id of plugin
     * @return top leve xml treenode
     */
    public XmlIO getConfiguration(String id) {
        try {
            File configFile = new File(getFolder(id), "config.xml");
            XmlIO io = new XmlIO(configFile.toURL());

            return io;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * @param id
     * 
     * @return directory of this plugin
     */
    public File getFolder(String id) {
        return (File) folders.get(id);
    }

    /**
     * 
     * get plugin handler
     * 
     * @param id
     *            ID of plugin handler
     * @return plugin handler
     * @throws PluginHandlerNotFoundException
     */
    public AbstractPluginHandler getHandler(String id)
            throws PluginHandlerNotFoundException {
        if (pluginHandlers.containsKey(id)) {
            return (AbstractPluginHandler) pluginHandlers.get(id);
        } else {
            LOG.severe("PluginHandler not found: " + id);

            throw new PluginHandlerNotFoundException(id);
        }
    }

    public Enumeration getHandlers() {
        return pluginHandlers.elements();
    }

    public List getIds() {
        return ids;
    }

    /**
     * @return URL of Readme.html, readme.txt, etc.
     */
    public URL getInfoURL(String id) {
        File pluginDirectory = getFolder(id);

        if (pluginDirectory == null) { return null; }

        try {
            // try all possible version of readme files...
            File infoFile = new File(pluginDirectory, "readme.html");

            if (!infoFile.exists()) {
                infoFile = new File(pluginDirectory, "readme.txt");
            }

            if (!infoFile.exists()) {
                infoFile = new File(pluginDirectory, "Readme.html");
            }

            if (!infoFile.exists()) {
                infoFile = new File(pluginDirectory, "Readme.txt");
            }

            if (infoFile.exists()) {
                LOG.fine("infofile-URL=" + infoFile.toURL());

                return infoFile.toURL();
            }
        } catch (MalformedURLException ex) {
            NotifyDialog d = new NotifyDialog();
            d.showDialog(ex);
        }

        return null;
    }

    /**
     * @param id
     * 
     * @return
     */
    public File getJarFile(String id) {
        return (File) jarFiles.get(id);
    }

    /**
     * @param id
     * 
     * @return parent xml treenode of "plugin.xml"
     */
    public XmlElement getPluginElement(String id) {
        String searchId;

        /*
         * int index = id.indexOf("$");
         * 
         * if (index != -1) searchId = id.substring(0, id.indexOf("$")); else
         * searchId = id;
         * 
         * return (XmlElement) elements.get(searchId);
         */
        return (XmlElement) elements.get(id);
    }

    public String getPluginType(String id) {
        XmlElement e = getPluginElement(id);
        XmlElement runtime = e.getElement("runtime");

        return runtime.getAttribute("type");
    }

    public void initPlugins() {
        // find all possible plugin directories
        File[] pluginFolders = PluginFinder.searchPlugins();

        folders = new HashMap();
        elements = new HashMap();
        jarFiles = new HashMap();
        ids = new Vector();

        // if no plugin directory exists -> return
        if (pluginFolders == null) { return; }

        // try to load all plugins
        for (int i = 0; i < pluginFolders.length; i++) {
            File folder = pluginFolders[i];
            addPlugin(folder);
        }
    }

    public void removeHandler(String id) {
        if (pluginHandlers.containsKey(id)) {
            AbstractPluginHandler h = (AbstractPluginHandler) pluginHandlers
                    .get(id);

            pluginHandlers.remove(id);

        }
    }

    /**
     * Add a list of handlers specified in path to the plugin manager.
     * 
     * @param path
     *            xml-file validating against pluginhandler.dtd
     */
    public void addHandlers(String path) {
        XmlIO xmlFile = new XmlIO(DiskIO.getResourceURL(path));
        xmlFile.load();

        XmlElement list = xmlFile.getRoot().getElement("handlerlist");
        Iterator it = list.getElements().iterator();
        while (it.hasNext()) {
            XmlElement child = (XmlElement) it.next();
            String id = child.getAttribute("id");
            String clazz = child.getAttribute("class");

            AbstractPluginHandler handler = null;
            try {
                Class c = Class.forName(clazz);

                handler = (AbstractPluginHandler) c.newInstance();

                registerHandler(handler);
            } catch (ClassNotFoundException e) {
                if (MainInterface.DEBUG) e.printStackTrace();
            } catch (InstantiationException e1) {
                if (MainInterface.DEBUG) e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                if (MainInterface.DEBUG) e1.printStackTrace();
            }

        }
    }

    /**
     * register plugin handler at plugin manager
     * 
     * @param handler
     */
    public void registerHandler(AbstractPluginHandler handler) {
        pluginHandlers.put(handler.getId(), handler);
        handler.setPluginManager(this);
    }

    /**
     * enable/disable plugin -> save changes in plugin.xml
     * 
     * @param b
     */
    public void setEnabled(String id, boolean b) {
        //get directory of plugin
        File folder = getFolder(id);

        // get plugin.xml of plugin
        File configFile = new File(folder, "plugin.xml");

        try {
            XmlIO io = new XmlIO(configFile.toURL());
            io.load();

            //get xml tree node
            XmlElement e = io.getRoot().getElement("/plugin");

            if (e == null) { return; }

            // update XmlElement reference in HashMap cache
            elements.put(id, e);

            // set enabled attribute
            e.addAttribute("enabled", Boolean.toString(b));

            io.save();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}

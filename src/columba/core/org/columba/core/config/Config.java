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
package org.columba.core.config;

import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.OSInfo;
import org.columba.core.xml.XmlElement;

import java.io.File;
import java.io.IOException;

import java.util.*;


/**
 * Main entrypoint for configuration management.
 * <p>
 * Example on how to get a {@link XmlElement} xml treenode:
 * <pre>
 * XmlElement gui = MainInterface.config.get("options").getElement("/options/gui");
 * </pre>
 * <p>
 * This would address the file <b>options.xml</b>. Following a little
 * example on how this file would look like:
 * <p>
 * <pre>
 * <?xml version="1.0" encoding="UTF-8"?>
 *  <options>
 *   <gui enabled="true">
 *    .. your options here
 *   </gui>
 *  </options>
 * </pre>
 * <p>
 * Note that all configuration file have default templates in
 * the /res directory in package org.columba.core.config.
 * <p>
 *
 * @author fdietz
 */
public class Config {
    protected OptionsXmlConfig optionsConfig;
    protected Map pluginList = new Hashtable();
    protected Map templatePluginList = new Hashtable();
    protected File path;
    protected File optionsFile;
    protected File toolsFile;

    /**
     * Creates a new configuration from the default directory.
     */
    public Config() {
        this(null);
    }

    /**
     * Creates a new configuration from the given directory.
     */
    public Config(File path) {
        if (path == null) {
            path = getDefaultConfigPath();
        }

        this.path = path;
        path.mkdir();
        optionsFile = new File(path, "options.xml");
        toolsFile = new File(path, "external_tools.xml");
    }

    /**
     * Returns the directory the configuration is located in.
     */
    public File getConfigDirectory() {
        return path;
    }

    /**
     * Method init.
     */
    public void init() {
        ShutdownManager.getShutdownManager().register(new Runnable() {
                public void run() {
                    try {
                        save();
                    } catch (Exception e) {
                        ColumbaLogger.log.severe(e.getMessage());
                    }
                }
            });

        ColumbaLogger.log.info("Loading configuration from " + path.toString());

        registerPlugin("core", optionsFile.getName(),
            new OptionsXmlConfig(optionsFile));

        registerPlugin("core", toolsFile.getName(),
            new DefaultXmlConfig(toolsFile));

        load();
    }

    /**
     * Method registerPlugin.
     * @param moduleName
     * @param id
     * @param configPlugin
     */
    public void registerPlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        File directory;

        if (moduleName.equals("core")) {
            directory = getConfigDirectory();
        } else {
            directory = new File(getConfigDirectory(), moduleName);
        }

        File destination = new File(directory, id);

        if (!destination.exists()) {
            String hstr = "org/columba/" + moduleName + "/config/" + id;

            try {
                DiskIO.copyResource(hstr, destination);
            } catch (IOException e) {
            }
        }

        if (!pluginList.containsKey(moduleName)) {
            Map map = new Hashtable();
            pluginList.put(moduleName, map);
        }

        addPlugin(moduleName, id, configPlugin);
    }

    public void registerTemplatePlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        String hstr = "org/columba/" + moduleName + "/config/" + id;
        configPlugin.setURL(DiskIO.getResourceURL(hstr));

        if (!templatePluginList.containsKey(moduleName)) {
            Map map = new Hashtable();
            templatePluginList.put(moduleName, map);
        }

        addTemplatePlugin(moduleName, id, configPlugin);
    }

    /**
     * Method getPlugin.
     * @param moduleName
     * @param id
     * @return DefaultXmlConfig
     */
    public DefaultXmlConfig getPlugin(String moduleName, String id) {
        if (pluginList.containsKey(moduleName)) {
            Map map = (Map) pluginList.get(moduleName);

            if (map.containsKey(id)) {
                DefaultXmlConfig plugin = (DefaultXmlConfig) map.get(id);

                return plugin;
            }
        }

        return null;
    }

    public DefaultXmlConfig getTemplatePlugin(String moduleName, String id) {
        if (templatePluginList.containsKey(moduleName)) {
            Map map = (Map) templatePluginList.get(moduleName);

            if (map.containsKey(id)) {
                DefaultXmlConfig plugin = (DefaultXmlConfig) map.get(id);

                return plugin;
            }
        }

        return null;
    }

    /**
     * Method addPlugin.
     * @param moduleName
     * @param id
     * @param configPlugin
     */
    public void addPlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        Map map = (Map) pluginList.get(moduleName);

        if (map != null) {
            map.put(id, configPlugin);
        }
    }

    public void addTemplatePlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        Map map = (Map) templatePluginList.get(moduleName);

        if (map != null) {
            map.put(id, configPlugin);
        }
    }

    /**
     * Method getPluginList.
     * @return List
     */
    public List getPluginList() {
        List list = new LinkedList();

        for (Iterator keys = pluginList.keySet().iterator(); keys.hasNext();) {
            String key = (String) keys.next();
            Map map = (Map) pluginList.get(key);

            if (map != null) {
                for (Iterator keys2 = map.keySet().iterator(); keys2.hasNext();) {
                    String key2 = (String) keys2.next();
                    DefaultXmlConfig plugin = (DefaultXmlConfig) map.get(key2);

                    list.add(plugin);
                }
            }
        }

        return list;
    }

    public List getTemplatePluginList() {
        List list = new LinkedList();

        for (Iterator keys = templatePluginList.keySet().iterator();
                keys.hasNext();) {
            String key = (String) keys.next();
            Map map = (Map) templatePluginList.get(key);

            if (map != null) {
                for (Iterator keys2 = map.keySet().iterator(); keys2.hasNext();) {
                    String key2 = (String) keys2.next();
                    DefaultXmlConfig plugin = (DefaultXmlConfig) map.get(key2);

                    list.add(plugin);
                }
            }
        }

        return list;
    }

    /**
     * Method save.
     */
    public void save() throws Exception {
        List list = getPluginList();

        for (Iterator it = list.iterator(); it.hasNext();) {
            DefaultXmlConfig plugin = (DefaultXmlConfig) it.next();

            if (plugin == null) {
                continue;
            }

            plugin.save();
        }
    }

    /**
     * Loads all plugins and template plugins.
     */
    protected void load() {
        List list = getPluginList();
        list.addAll(getTemplatePluginList());

        for (Iterator it = list.iterator(); it.hasNext();) {
            DefaultXmlConfig plugin = (DefaultXmlConfig) it.next();

            if (plugin == null) {
                continue;
            }

            plugin.load();
        }
    }

    public XmlElement get(String name) {
        DefaultXmlConfig xml = getPlugin("core", name + ".xml");

        return xml.getRoot();
    }

    /**
     * Method getOptionsMainInterface.config.
     * @return OptionsXmlConfig
     */
    public OptionsXmlConfig getOptionsConfig() {
        return (OptionsXmlConfig) getPlugin("core", optionsFile.getName());
    }

    /**
     * Returns the default configuration path. This value depends on the
     * underlying operating system. This method must never return null.
     */
    public static File getDefaultConfigPath() {
        if (OSInfo.isWindowsPlatform()) {
            return new File("config");
        } else {
            return new File(System.getProperty("user.home"), ".columba");
        }
    }
}

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
import org.columba.core.xml.XmlElement;

import java.io.File;
import java.io.IOException;

import java.util.*;

/**
 * Main entrypoint for configuration management.
 * <p>
 * Example on how to get a {@link XmlElement} xml treenode:
 * <pre>
 * XmlElement gui = Config.get("options").getElement("/options/gui");
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
    public static File inboxDirectory;
    public static File sentDirectory;
    public static File headerDirectory;
    public static File pop3Directory;
    public static String userDir;
    
    private static OptionsXmlConfig optionsConfig;
    private static Map pluginList = new Hashtable();
    private static Map templatePluginList = new Hashtable();
    private static File optionsFile;
    private static File toolsFile;

    /**
     * @see java.lang.Object#Object()
     */
    public Config() {
        optionsFile = new File(ConfigPath.getConfigDirectory(), "options.xml");

        DefaultConfig.registerPlugin("core", optionsFile.getName(),
            new OptionsXmlConfig(optionsFile));

        toolsFile = new File(ConfigPath.getConfigDirectory(),
                "external_tools.xml");

        DefaultConfig.registerPlugin("core", toolsFile.getName(),
            new DefaultXmlConfig(toolsFile));

        /*
        File file = new File(ConfigPath.getConfigDirectory(), "mail");


        if (file.exists() == false) {
                // convert to new config-schema

                Python.runResource(
                        "org/columba/mail/config/convert.py",
                        ConfigPath.getConfigDirectory().getPath());


        }
        */
    }

    /**
     * Method init.
     */
    public static void init() {
        File configDirectory = ConfigPath.getConfigDirectory();

        load();

        pop3Directory = new File(configDirectory, "mail/pop3server");

        if (!pop3Directory.exists()) {
            pop3Directory.mkdir();
        }
    }

    /**
     * Method registerPlugin.
     * @param moduleName
     * @param id
     * @param configPlugin
     */
    public static void registerPlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        if (!pluginList.containsKey(moduleName)) {
            Map map = new Hashtable();
            pluginList.put(moduleName, map);
        }

        addPlugin(moduleName, id, configPlugin);
    }

    public static void registerTemplatePlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
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
    public static DefaultXmlConfig getPlugin(String moduleName, String id) {
        if (pluginList.containsKey(moduleName)) {
            Map map = (Map) pluginList.get(moduleName);

            if (map.containsKey(id)) {
                DefaultXmlConfig plugin = (DefaultXmlConfig) map.get(id);

                return plugin;
            }
        }

        return null;
    }

    public static DefaultXmlConfig getTemplatePlugin(String moduleName,
        String id) {
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
    public static void addPlugin(String moduleName, String id,
        DefaultXmlConfig configPlugin) {
        Map map = (Map) pluginList.get(moduleName);

        if (map != null) {
            map.put(id, configPlugin);
        }
    }

    public static void addTemplatePlugin(String moduleName, String id,
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
    public static List getPluginList() {
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

    public static List getTemplatePluginList() {
        List list = new LinkedList();

        for (Iterator keys = templatePluginList.keySet().iterator(); keys.hasNext();) {
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
    public static void save() throws Exception {
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
    public static void load() {
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

    public static XmlElement get(String name) {
        DefaultXmlConfig xml = DefaultConfig.getPlugin("core", name + ".xml");

        return xml.getRoot();
    }

    /**
     * Method getOptionsConfig.
     * @return OptionsXmlConfig
     */
    public static OptionsXmlConfig getOptionsConfig() {
        return (OptionsXmlConfig) DefaultConfig.getPlugin("core",
            optionsFile.getName());
    }
}

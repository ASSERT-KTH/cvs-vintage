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
package org.columba.mail.folderoptions;

import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.xml.XmlElement;

import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.plugin.FolderOptionsPluginHandler;

import java.util.HashMap;
import java.util.Map;


/**
 * Controller used by {@link TableController} to handle all
 * folder-related option plugins.
 * <p>
 * Note, that every {@link MailFrameMediator} keeps its own
 * <code>FolderOptionsController<code>, which makes sure that
 * all plugins are singletons.
 *
 * @author fdietz
 */
public class FolderOptionsController {
    /**
     * Plugins executed before updating the table model
     * <p>
     * example:<b>
     * Sorting/Filtering state
     */
    public final static int STATE_BEFORE = 0;

    /**
     * Plugins executed after updating the table model
     * <p>
     * example:<br>
     * Selection of messages
     */
    public final static int STATE_AFTER = 1;

    /**
     * mail frame mediator
     */
    private MailFrameMediator mediator;

    /**
     * Stores all instanciated plugins for later re-use
     */
    private Map map;

    /**
     * plugin handler for instanciating folder options plugins
     */
    private FolderOptionsPluginHandler handler;

    /**
     * Constructor
     *
     * @param mediator      mail frame mediator
     */
    public FolderOptionsController(MailFrameMediator mediator) {
        this.mediator = mediator;

        // init map
        map = new HashMap();

        // init plugin handler
        try {
            handler = (FolderOptionsPluginHandler) MainInterface.pluginManager.getHandler(
                    "org.columba.mail.folderoptions");
        } catch (PluginHandlerNotFoundException e) {
            // TODO: show error dialoghere
            e.printStackTrace();
        }
    }

    /**
     * Get plugin with specific name.
     *
     * @param name      name of plugin
     * @return          instance of plugin
     */
    public AbstractFolderOptionsPlugin getPlugin(String name) {
        // check if this plugin was already loaded
        if (map.containsKey(name)) {
            // already loaded -> re-use it
            return (AbstractFolderOptionsPlugin) map.get(name);
        } else {
            AbstractFolderOptionsPlugin plugin = null;

            try {
                plugin = (AbstractFolderOptionsPlugin) handler.getPlugin(name,
                        new Object[] { mediator });
            } catch (Exception e) {
                // TODO: add error dialog
                e.printStackTrace();
            }

            // save plugin instance in map
            map.put(name, plugin);

            return plugin;
        }
    }

    /**
     * Load all folder options for this folder.
     *
     * @param folder        selected folder
     */
    public void load(MessageFolder folder, int state) {
        // get list of plugins
        String[] ids = handler.getPluginIdList(state);

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.loadOptionsFromXml(folder);
        }
    }

    /**
     * Save all folder options for this folder.
     *
     * @param folder        selected folder
     */
    public void save(MessageFolder folder) {
        // get list of plugins
        String[] ids = handler.getPluginIdList();

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.saveOptionsToXml(folder);
        }
    }

    /**
     * Load all folder options globally.
     *
     */
    public void load(int state) {
        // get list of plugins
        String[] ids = handler.getPluginIdList(state);

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.loadOptionsFromXml(null);
        }
    }

    /**
     * Get parent configuration node of plugin.
     * <p>
     * Example for the sorting plugin configuration node. This is
     * how it can be found in options.xml and tree.xml:<br>
     * <pre>
     *  <sorting column="Date" order="true" />
     * </pre>
     * <p>
     *
     * @param folder        selected folder
     * @param name          name of plugin (example: ColumnOptions)
     * @return              parent configuration node
     */
    public XmlElement getConfigNode(MessageFolder folder, String name) {
        XmlElement parent = null;
        boolean global = false;

        if ((folder == null) || (name == null)) {
            // if no folder was passed as argument, use global options
            parent = FolderItem.getGlobalOptions();
            global = true;
        } else {
            // use folder specific options
            parent = folder.getConfiguration().getFolderOptions();
            global = false;
        }

        // load plugin
        AbstractFolderOptionsPlugin plugin = getPlugin(name);
        XmlElement child = parent.getElement(plugin.getName());

        if (child == null) {
            // create default configuration
            child = plugin.createDefaultElement(global);
            parent.addElement(child);
        }

        if (global) {
            return child;
        }

        String overwrite = child.getAttribute("overwrite");

        // check if this folder is overwriting global options
        if ((overwrite != null) && (overwrite.equals("true"))) {
            // use folder-based options
            return child;
        } else {
            // use global options
            parent = FolderItem.getGlobalOptions();
            child = parent.getElement(plugin.getName());

            return child;
        }
    }

    /**
     * Create default settings for this folder.
     *
     * @param folder                selected folder
     */
    public void createDefaultSettings(MessageFolder folder) {
        FolderItem item = folder.getConfiguration();
        XmlElement parent = item.getElement("property");

        // use global settings
        String[] ids = handler.getPluginIdList();

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            XmlElement child = plugin.createDefaultElement(false);
            parent.addElement(child);
        }
    }

    /**
     * Returns true, if any option is overridden by folder.
     *
     * @param folder                selected folder
     * @return                                true, if any option is overridden. False, otherwise.
     */
    private boolean isOverwritingDefaults(MessageFolder folder) {
        FolderItem item = folder.getConfiguration();
        XmlElement parent = item.getElement("property");
        boolean result = false;

        for (int i = 0; i < parent.count(); i++) {
            XmlElement child = parent.getElement(i);
            String overwrite = child.getAttribute("overwrite");

            if (overwrite.equals("true")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Save global settings.
     * <p>
     * Method is called when shutting down Columba. Note, that
     * when a folder is selected which overwrites options, calling
     * save() will make his settings global. Which is wrong.
     *
     * @param folder                selected folder
     */
    public void saveGlobalSettings(AbstractFolder folder) {
        if (folder instanceof MessageFolder) {
            if (isOverwritingDefaults((MessageFolder) folder)) {
                // restore global settings in ui
                load(STATE_BEFORE);
            }
        }

        save(null);
    }
}

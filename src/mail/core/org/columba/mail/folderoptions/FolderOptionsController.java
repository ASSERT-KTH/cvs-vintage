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
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.plugin.FolderOptionsPluginHandler;

import java.util.HashMap;
import java.util.Map;


/**
 * @author fdietz
 */
public class FolderOptionsController {
    private MailFrameMediator mediator;
    private Map map;
    private FolderOptionsPluginHandler handler;

    public FolderOptionsController(MailFrameMediator mediator) {
        this.mediator = mediator;
        map = new HashMap();

        try {
            handler = (FolderOptionsPluginHandler) MainInterface.pluginManager.getHandler(
                    "org.columba.mail.folderoptions");
        } catch (PluginHandlerNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public AbstractFolderOptionsPlugin getPlugin(String name) {
        if (map.containsKey(name)) {
            // already loaded
            return (AbstractFolderOptionsPlugin) map.get(name);
        } else {
            AbstractFolderOptionsPlugin plugin = null;

            try {
                plugin = (AbstractFolderOptionsPlugin) handler.getPlugin(name,
                        new Object[] { mediator });
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            map.put(name, plugin);

            return plugin;
        }
    }

    public void load(Folder folder) {
        String[] ids = handler.getPluginIdList();

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.loadOptionsFromXml(folder);
        }
    }

    public void save(Folder folder) {
        String[] ids = handler.getPluginIdList();

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.saveOptionsToXml(folder);
        }
    }

    public void load() {
        String[] ids = handler.getPluginIdList();

        for (int i = 0; i < ids.length; i++) {
            AbstractFolderOptionsPlugin plugin = getPlugin(ids[i]);
            plugin.loadOptionsFromXml(null);
        }
    }

    public static XmlElement getConfigNode(Folder folder, String name) {
        XmlElement parent = null;
        boolean global = false;

        if (folder == null) {
            parent = FolderItem.getGlobalOptions();
            global = true;
        } else {
            parent = folder.getFolderItem().getFolderOptions();
            global = false;
        }

        XmlElement child = parent.getElement(name);

        if (global) {
            return child;
        }

        String overwrite = child.getAttribute("overwrite");

        if ((overwrite != null) && (overwrite.equals("true"))) {
            // use folder-based options
            return child;
        } else {
            // use global options
            parent = FolderItem.getGlobalOptions();
            child = parent.getElement(name);

            return child;
        }
    }
}

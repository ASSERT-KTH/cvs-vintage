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
import org.columba.core.logging.ColumbaLogger;

import java.io.File;

import java.util.List;
import java.util.Vector;


/**
 * Find all plugins in the users config-directory and Columba's
 * program folder.
 *
 * @author fdietz
 */
public class PluginFinder {
    /**
     * Constructor for PluginFinder.
     */
    public PluginFinder() {
        super();
    }

    /**
     * Get list of all possible plugin folders.
     *
     * @return        array of plugin folders
     */
    public static File[] searchPlugins() {
        List v = new Vector();
        File[] programList = null;
        File[] configList = null;

        File programFolder = new File("plugins");

        if (programFolder.exists()) {
            programList = programFolder.listFiles();
        } else {
            ColumbaLogger.log.fine("Folder \"plugins\" doesn't exist.");
        }

        File configFolder = new File(MainInterface.config.getConfigDirectory(), "plugins");

        if (configFolder.exists() == false) {
            configFolder.mkdir();
        }

        ColumbaLogger.log.info("config-folder path=" + configFolder.getPath());

        if (configFolder.exists()) {
            configList = configFolder.listFiles();
        } else {
            ColumbaLogger.log.fine("Folder \"plugins\" doesn't exist.");
        }

        if ((programList != null) && (configList != null)) {
            File[] result = new File[programList.length + configList.length];
            System.arraycopy(programList, 0, result, 0, programList.length);
            System.arraycopy(configList, 0, result, programList.length,
                configList.length);

            return result;
        } else if (programList != null) {
            return programList;
        } else if (configList != null) {
            return configList;
        }

        return null;
    }
}

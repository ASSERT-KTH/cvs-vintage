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

import org.columba.core.util.OSInfo;

import java.io.File;
import java.io.IOException;

/**
 * ConfigPath keeps the path to the user configuration directory.
 *
 * @author fdietz
 */
public class ConfigPath {
    public static File configDirectory;

    public ConfigPath() {
        if (OSInfo.isWindowsPlatform()) {
            // this os has no home directory
            // for example windows9x
            if (configDirectory == null) {
                configDirectory = new File("config");
            }
        } else {
            String userHome = new String(System.getProperty("user.home"));
            if (configDirectory == null) {
                configDirectory = new File(userHome, ".columba");
            }
        }
        configDirectory.mkdir();
    }

    public ConfigPath(String configPath) {
        configDirectory = new File(configPath);
        configDirectory.mkdir();
    }

    public static File getConfigDirectory() {
        return configDirectory;
    }
}

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
package org.columba.addressbook.config;

import org.columba.core.config.Config;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;

import java.io.File;


/**
 * @version         1.0
 * @author
 */
public class AddressbookConfig {
    public static final String MODULE_NAME = "addressbook";
    protected Config config;
    protected File path;

    //private File addressbookFile;
    private File addressbookOptionsFile;
    private File folderFile;

    /**
 * @see java.lang.Object#Object()
 */
    public AddressbookConfig(Config config) {
        this.config = config;
        path = new File(config.getConfigDirectory(), MODULE_NAME);
        DiskIO.ensureDirectory(path);

        /*
addressbookFile = new File(path, "addressbook.xml");
registerPlugin(
        addressbookFile.getName(),
        new DefaultXmlConfig(addressbookFile));
*/
        addressbookOptionsFile = new File(path, "options.xml");
        registerPlugin(addressbookOptionsFile.getName(),
            new DefaultXmlConfig(addressbookOptionsFile));

        folderFile = new File(path, "tree.xml");
        registerPlugin(folderFile.getName(), new DefaultXmlConfig(folderFile));

        File mainToolBarFile = new File(path, "main_toolbar.xml");
        registerPlugin(mainToolBarFile.getName(),
            new DefaultXmlConfig(mainToolBarFile));
    }

    public File getConfigDirectory() {
        return path;
    }

    public XmlElement get(String name) {
        DefaultXmlConfig xml = getPlugin(name + ".xml");

        return xml.getRoot();
    }

    /**
 * Method registerPlugin.
 * @param id
 * @param plugin
 */
    protected void registerPlugin(String id, DefaultXmlConfig plugin) {
        config.registerPlugin(MODULE_NAME, id, plugin);
    }

    /**
 * Method getPlugin.
 * @param id
 * @return DefaultXmlConfig
 */
    protected DefaultXmlConfig getPlugin(String id) {
        return config.getPlugin(MODULE_NAME, id);
    }
}

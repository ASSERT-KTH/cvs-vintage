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
package org.columba.core.pluginhandler;

import org.columba.core.externaltools.AbstractExternalToolsPlugin;
import org.columba.core.gui.externaltools.ExternalToolsWizardLauncher;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

import java.io.File;


/**
 * Provides an easy way to integrate external apps in Columba.
 * <p>
 * This includes a first-time assistant for the user. And a
 * configuration file "external_tools.xml" to store the options
 * of the external tools.
 * <p>
 * When using external commandline (already used examples are
 * aspell and GnuPG) tools, you should just use this handler
 * to get the location of the executable.
 * <p>
 * If the executable wasn't configured, yet a wizard will assist
 * the user in configuring the external tool.
 * If everything is correctly configured, it will just return
 * the path of the commandline tool as <code>File</code>.
 * <p>
 * <verbatim>
 *  File file = getLocationOfExternalTool("gpg");
 * </verbatim>
 *
 * <p>
 *
 * @see org.columba.core.plugin.external_tools.xml
 *
 * @author fdietz
 */
public class ExternalToolsPluginHandler extends AbstractPluginHandler {
    /**
     * @param id
     * @param config
     */
    public ExternalToolsPluginHandler() {
        super("org.columba.core.externaltools",
            "org/columba/core/plugin/external_tools.xml");

        parentNode = getConfig().getRoot().getElement("tools");
    }

    /**
     * Gets the location of an external commandline tool.
     *
     * @param toolID                id of tool
     * @return                        location of tool
     */
    public File getLocationOfExternalTool(String toolID) {
        AbstractExternalToolsPlugin plugin = null;

        try {
            plugin = (AbstractExternalToolsPlugin) getPlugin(toolID, null);
        } catch (Exception e1) {
            e1.printStackTrace();

            return null;
        }

        // check configuration
        XmlElement root = getConfiguration(toolID);

        if (root == null) {
            // create xml node
            XmlElement parent = MainInterface.config.get("external_tools")
                                                    .getElement("tools");
            XmlElement child = new XmlElement("tool");
            child.addAttribute("first_time", "true");
            child.addAttribute("name", toolID);
            parent.addElement(child);

            root = child;
        }

        boolean firsttime = false;

        if (root.getAttribute("first_time").equals("true")) {
            firsttime = true;
        }

        if (firsttime) {
            // start the configuration wizard
            ExternalToolsWizardLauncher launcher = new ExternalToolsWizardLauncher();
            launcher.launchWizard(toolID, true);

            if (launcher.isFinished()) {
                // ok, now the tool is initialized correctly
                XmlElement r = getConfiguration(toolID);
                File file = new File(r.getAttribute("location"));

                return file;
            }
        } else {
            String location = root.getAttribute("location");

            File file = new File(location);

            return file;
        }

        return null;
    }

    /**
     * Gets xml configuration of tool with id.
     *
     * @param id                id of tool
     * @return                        xml treenode
     */
    public XmlElement getConfiguration(String id) {
        XmlElement root = MainInterface.config.get("external_tools").getElement("tools");
        boolean firsttime = false;

        for (int i = 0; i < root.count(); i++) {
            XmlElement child = root.getElement(i);

            if (child.getAttribute("name").equals(id)) {
                return child;
            }
        }

        return null;
    }
}
